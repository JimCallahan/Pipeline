// $Id: NotifyTask.cc,v 1.2 2004/04/06 08:58:52 jim Exp $

#include <NotifyTask.hh>

namespace Pipeline {

/*------------------------------------------------------------------------------------------*/
/*   N O T I F Y   T A S K                                                                  */
/*                                                                                          */
/*    Monitors a set of local directories for file creation, modification, removal and      */
/*    renaming events which occur within the watched directories.                           */
/*------------------------------------------------------------------------------------------*/

/*----------------------------------------------------------------------------------------*/
/*   C O N S T R U C T O R                                                                */
/*----------------------------------------------------------------------------------------*/

/**
 * Construct a new task.
 * 
 * param mgr
 *   The parent task manager.
 * 
 * param dir 
 *   The root directory of all watched directories.
 */ 
NotifyTask::NotifyTask
(
 NotifyMgr& mgr, 
 const char* dir
) : 
  Task("NotifyTask"), 
  pMgr(mgr), 
  pLockSet(mgr.getLockSet()), 
  pRootDir(strdup(dir))
{
  /* to prevent any directory adding or removing from going forward until everything has 
       been initialized at the start of the run() method. */ 
  pLockID = pLockSet.initLock();
  assert(pLockID != -1);
  pLockSet.lock(pLockID);

  int wk;
  for(wk=0; wk<1024; wk++) {
    pAdd[wk]    = NULL;
    pRemove[wk] = NULL;
    pDirs[wk]   = NULL;
  }

  pNumAdd    = 0;
  pNumRemove = 0;
  pNumDirs   = 0;
}

/*----------------------------------------------------------------------------------------*/
/*   T A S K                                                                              */
/*----------------------------------------------------------------------------------------*/

/**
 * Run the task.
 */ 
int
NotifyTask::run()
{
  char msg[1024];

  /* block handling of SIGRTMIN+4 */
  sigset_t signalset;
  {
    sigemptyset(&signalset);
    sigaddset(&signalset, SIGRTMIN+4);
 
    if(sigprocmask(SIG_BLOCK, &signalset, NULL) == -1) {
      sprintf(msg, "Unable to block directory change signal (SIGRTMIN+4): %s", 
	      strerror(errno));
      FB::error(msg);	
    }
  }
  
  /* unlock for the first time... */ 
  pLockSet.unlock(pLockID);
  
  /* ready for action */ 
  while(!pMgr.isShutdown()) {

    /* check for added or removed directories */ 
    pLockSet.lock(pLockID);
    {
      bool debugPrint = false; // DEBUG
      
      /* start monitoring the added directories */ 
      if(pNumAdd > 0) {
	int wk; 
	for(wk=0; wk<pNumAdd; wk++) {   
	  if(!containsDir(pDirs, 1024, pAdd[wk])) {
	    
	    char path[1024];
	    sprintf(path, "%s/%s", pRootDir, pAdd[wk]);
	    
	    /* get the file descriptor */ 
	    int fd = open(path, O_RDONLY);
	    if(fd == -1) {
	      sprintf(msg, "Unable to open directory (%s) relative to (%s): %s", 
		      pAdd[wk], pRootDir, strerror(errno));
	      FB::error(msg);
	    }
	    
	    /* add monitoring */ 
	    if(fcntl(fd, F_SETSIG, SIGRTMIN+4) == -1) {
	      sprintf(msg, "Unable to set signal (SIGRTMIN+4): %s", strerror(errno));
	      FB::error(msg);
	    }
	    
	    long args = DN_MODIFY | DN_CREATE | DN_DELETE | DN_RENAME | DN_ATTRIB;
	    if(fcntl(fd, F_NOTIFY, args | DN_MULTISHOT) == -1) {
	      sprintf(msg, "Unable to add notification for directory (%s): %s", 
		      pAdd[wk], strerror(errno));
	      FB::error(msg);
	    }
	    
	    /* add it to the tables */ 
	    pDirs[fd] = pAdd[wk];
	    pNumDirs++;
	    
	    // DEBUG
	    printf("%s[%d] Added Directory: %s/%s (%d)\n", 
		   pName, pPID, pRootDir, pAdd[wk], fd);
	    // DEBUG
	  }
	  else {
	    // DEBUG
	    printf("[%d] Existing Directory: %s/%s\n", 
		   pPID, pRootDir, pAdd[wk]);
	    // DEBUG
	    
	    delete[] (pAdd[wk]);
	  }
	  
	  pAdd[wk] = NULL;
	}
	
	pNumAdd = 0;
	
	debugPrint = true;  // DEBUG
      }
      
      /* stop monitoring the removed directories */ 
      if(pNumRemove > 0) {  
	int wk; 
	for(wk=0; wk<pNumRemove; wk++) {   
	  int fd = findDir(pDirs, 1024, pRemove[wk]);
	  if(fd != -1) {
	    
	    /* cancel monitoring */ 
	    if(fcntl(fd, F_NOTIFY, 0) == -1) {
	      sprintf(msg, "Unable to cancel notification for directory (%s): %s", 
		      pRemove[wk], strerror(errno));
	      FB::error(msg);
	    }
	    
	    /* release the file descriptor */ 
	    if(close(fd) == -1) {
	      sprintf(msg, "Unable to close directory (%s) relative to (%s): %s", 
		      pRemove[wk], pRootDir, strerror(errno));
	      FB::error(msg);
	    }
	    
	    /* clear the directory entry */ 
	    delete[] (pDirs[fd]);
	    pDirs[fd] = NULL;
	    pNumDirs--;
	    
	    // DEBUG
	    printf("[%d] Removed Directory: %s/%s (%d)\n", 
		   pPID, pRootDir, pRemove[wk], fd);
	    // DEBUG
	  }
	  
	  delete[] (pRemove[wk]);
	  pRemove[wk] = NULL;
	}
	
	pNumRemove = 0;
	
	debugPrint = true;  // DEBUG
      }
      
      // DEBUG
      if(debugPrint) {
	printf("MONITORED DIRS: %d\n", pNumDirs);
	int wk; 
	for(wk=0; wk<1024; wk++) {  
	  if(pDirs[wk] != NULL) {
	    printf("[%d]  %s/%s (%d)\n", 
		   pPID, pRootDir, pDirs[wk], wk);
	  }
	}
	printf("-------------------------------------------------------------------\n");
      }
      // DEBUG
      
    }
    pLockSet.unlock(pLockID);


    /* wait for modification signals */
    {
      sigset_t signalset;
      sigemptyset(&signalset);
      sigaddset(&signalset, SIGRTMIN+4);
      
      struct timespec ts = { 1, 0 };
      
      siginfo_t sinfo;
      if(sigtimedwait(&signalset, &sinfo, &ts) == -1) {
	switch(errno) {
	case EAGAIN:
	  break;
	  
	case EINTR:
	  sprintf(msg, "Bad signal: %s", strerror(errno));
	  FB::error(msg);
	  
	default:
	  FB::error("Internal Error!");
	}
      }
      else {
	// DEBUG
	printf("%s[%d]: Modified: %s/%s\n", 
	        pName, pPID, pRootDir, pDirs[sinfo.si_fd]);
	// DEBUG
    
	// ... push onto the queue to be sent over the network...
    
	pMgr.modified(pDirs[sinfo.si_fd]);
      }
    }
  }

  return EXIT_SUCCESS;
}


} // namespace Pipeline
