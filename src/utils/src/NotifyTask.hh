// $Id: NotifyTask.hh,v 1.2 2004/04/05 06:27:55 jim Exp $

#ifndef PIPELINE_NOTIFY_TASK_HH
#define PIPELINE_NOTIFY_TASK_HH

#ifdef HAVE_CONFIG_H
#  include "config.h"
#endif

#ifdef HAVE_SYS_TYPES_H
#  include <sys/types.h>
#endif

#ifdef HAVE_SYS_STAT_H
#  include <sys/stat.h>
#endif

#ifdef HAVE_UNISTD_H
#  include <unistd.h>
#endif

#ifdef HAVE_FCNTL_H
#  include <fcntl.h>
#endif

#ifdef HAVE_SIGNAL_H
#  include <signal.h>
#endif

#ifdef HAVE_ERRNO_H
#  include <errno.h>
#endif

#include <Task.hh>
#include <Lock.hh>
#include <NotifyMgr.hh>

namespace Pipeline {

class NotifyMgr;

/*------------------------------------------------------------------------------------------*/
/*   N O T I F Y   T A S K                                                                  */
/*                                                                                          */
/*    Monitors a set of local directories for file creation, modification, removal and      */
/*    renaming events which occur within the watched directories.                           */
/*------------------------------------------------------------------------------------------*/

class NotifyTask : public Task
{
public:
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
  NotifyTask
  (
   NotifyMgr* mgr, 
   const char* dir
  ) : 
    pMgr(mgr), 
    pRootDir(strdup(dir))
  {
    /* to prevent any directory adding or removing to go forward until everything has 
         been initialized in the run() method. */ 
    pLock.lock();

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
  /*   D E S T R U C T O R                                                                  */
  /*----------------------------------------------------------------------------------------*/

  ~NotifyTask()
  {
    delete[] pRootDir;

    assert(pNumAdd == 0);
    assert(pNumRemove == 0);
    assert(pNumDirs == 0);

    int wk;
    for(wk=0; wk<1024; wk++) 
      assert(pDirs[wk] == NULL);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Add the given directory to the set of directories monitored for change notification.
   * 
   * param dir
   *   The directory to add relative to the root watched directory.
   * 
   * return 
   *   Whether the directory was added.
   */
  bool
  addDir
  (
   const char* dir
  ) 
  {
    bool added = false;

    pLock.lock();
    {
      assert(dir != NULL);
      if((pNumAdd + pNumDirs) < 1021) {
	if(!containsDir(pAdd, pNumAdd, dir)) {
	  pAdd[pNumAdd] = strdup(dir);
	  pNumAdd++;
	  added = true;
	}
      }
    }
    pLock.unlock();

    return added;
  }
  
  /**
   * Remove the given directory from the set of directories monitored for change notification.
   * 
   * param dir
   *   The directory to add relative to the root watched directory.
   */
  void
  removeDir
  (
   const char* dir
  ) 
  {
    pLock.lock();
    {
      assert(pNumRemove < 1024);
      if(!containsDir(pRemove, pNumRemove, dir)) {
	pRemove[pNumRemove] = strdup(dir);
	pNumRemove++;
      }      
    }
    pLock.unlock();
  }
  



  /*----------------------------------------------------------------------------------------*/
  /*   T A S K                                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Order the task to exit as soon as possible.
   */ 
  void
  shutdown()
  {
    pLock.lock();
    pShutdown = true;
    pLock.unlock();
  }

  /**
   * Run the task.
   */ 
  virtual void
  run()
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
    pLock.unlock();

    /* ready for action */ 
    while(true) {
      pLock.lock();
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
	      printf("[%d] Added Directory: %s/%s (%d)\n", 
		     pPID, pRootDir, pAdd[wk], fd);
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
      pLock.unlock();
	
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
	  modified(pDirs[sinfo.si_fd]);
	}
      }

      /* check to see if the task has been ordered to exit */ 
      pLock.lock();
      {
	if(pShutdown) {
	  int wk;
	  for(wk=0; wk<pNumAdd; wk++) 
	    delete[] (pAdd[wk]);
	  pNumAdd = 0;

	  for(wk=0; wk<pNumRemove; wk++) 
	    delete[] (pRemove[wk]);
	  pNumRemove = 0;

	  /* close down all monitored directories */ 
	  for(wk=0; wk<1024; wk++) {
	    if(pDirs[wk] != NULL) {

	      /* cancel monitoring */ 
 	      if(fcntl(wk, F_NOTIFY, 0) == -1) {
 		sprintf(msg, "Unable to cancel notification for directory (%s): %s", 
			pDirs[wk], strerror(errno));
 		FB::error(msg);
 	      }
	  
	      /* release the file descriptor */ 
	      if(close(wk) == -1) {
		sprintf(msg, "Unable to close directory (%s) relative to (%s): %s", 
			pDirs[wk], pRootDir, strerror(errno));
		FB::error(msg);
	      }
	      
	      delete[] (pDirs[wk]);
	      pDirs[wk] = NULL;
	    }
	  }
	  pNumDirs = 0;

	  exit(EXIT_SUCCESS);
	}
      }
      pLock.unlock();
    }
  }

  
private:
  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Does the given table already contain the directory string?
   * 
   * param table
   *   The array of directory names.
   * 
   * param size
   *   The size of the table.
   * 
   * param dir
   *   The directory to test the table entries against.
   */ 
  bool
  containsDir
  (
   char** table,
   int size, 
   const char* dir
  ) 
  {
    return (findDir(table, size, dir) != -1);
  }

  /**
   * Find the indes in the given table of the given directory.
   * 
   * param table
   *   The array of directory names.
   * 
   * param size
   *   The size of the table.
   * 
   * param dir
   *   The directory to test the table entries against.
   * 
   * return 
   *   The index of the directory or -1 if the directory was not found.
   */ 
  int
  findDir
  (
   char** table,
   int size, 
   const char* dir
  ) 
  {
    int wk;
    for(wk=0; wk<size; wk++) 
      if((table[wk] != NULL) && strcmp(table[wk], dir) == 0) 
	return wk;

    return -1;
  }

  /**
   * Report the modified directory to the parent NodeMgr.
   */ 
  void
  modified
  (
   const char* dir
  ); 



private:
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent task manager.
   */
  NotifyMgr*  pMgr;



  /**
   * Protects access to the internal variables.
   */
  Lock  pLock;

  /**
   * A flag which schedules the exit of the thread as soon as possible.
   */ 
  bool pShutdown;

  /** 
   * The root directory of all watched directories.
   */
  const char* pRootDir;

  /**
   * Directories to remove from the monitored list 
   */ 
  char* pAdd[1024];
  int   pNumAdd;

  /**
   * Directories to remove from the monitored list.
   */ 
  char* pRemove[1024];
  int   pNumRemove;

  /**
   * The table of monitored directory names indexed by open file descriptor.
   */
  char* pDirs[1024];
  int   pNumDirs;
};


} // namespace Pipeline

#endif
