// $Id: NotifyMgr.cc,v 1.4 2004/04/09 17:55:12 jim Exp $

#include <NotifyMgr.hh>

namespace Pipeline {

/*------------------------------------------------------------------------------------------*/
/*   N O T I F Y   M G R                                                                    */
/*                                                                                          */
/*    Manages a set of threads (NotifyTask) which each monitor a set of local directories   */
/*    for file creation, modification, removal and renaming events which occur within the   */
/*    watched directories.  Due to the limit of 1024 open files per-process, there must be  */
/*    more than one thread if the number of directories being monitored exceeds this limit. */
/*    This class manages these threads and presets the user with a simple interface for     */
/*    interacting with the NotifyTask instances.                                            */
/*------------------------------------------------------------------------------------------*/

/*----------------------------------------------------------------------------------------*/
/*   C O N S T R U C T O R                                                                */
/*----------------------------------------------------------------------------------------*/

/**
 * Construct a new notify task manager.
 * 
 * param dir 
 *   The root directory of all watched directories.
 * 
 * param controlPort
 *   The control network port number.
 * 
 * param monitorPort
 *   The monitor network port number.
 */ 
NotifyMgr::NotifyMgr
( 
 const char* dir,
 int controlPort,
 int monitorPort
) : 
  pShutdown(false),
  pRootDir(strdup(dir))
{
  /* intialize the locks */ 
  pShutdownID = pLockSet.initLock();
  assert(pShutdownID != -1);
  printf("NotifyMgr::pShutdownID = %d\n", pShutdownID);
  pLockSet.lock(pShutdownID);

  pLockID = pLockSet.initLock();
  assert(pLockID != -1);
  printf("NotifyMgr::pLockID = %d\n", pLockID);

  /* start the control server */ 
  pControlServer = new NotifyControlServer(*this, controlPort);
  pControlServer->spawn();

  /* start the monitor server */ 
  pMonitorServer = new NotifyMonitorServer(*this, monitorPort);
  pMonitorServer->spawn();
}



/*----------------------------------------------------------------------------------------*/
/*   A C C E S S                                                                          */
/*----------------------------------------------------------------------------------------*/

/**
 * Add the given directory to the set of directories monitored for change notification.
 * 
 * param dir
 *   The directory to add relative to the root watched directory.
 */
void 
NotifyMgr::addDir
(
 const char* dir
) 
{
  if(pShutdown) 
    return;

  pLockSet.lock(pLockID);
  {
    bool added = false;
    {
      TaskList::iterator iter;
      for(iter = pTasks.begin(); iter != pTasks.end(); iter++) {
	if((*iter)->addDir(dir)) {
	  added = true;
	  break;
	}
      }
    }

    if(!added) {
      NotifyTask* task = new NotifyTask(*this, pRootDir);
      pTasks.push_back(task);
      
      task->spawn();

      FB::threadMsg("Started", 2, task->getName(), task->getPID());
      
      if(!task->addDir(dir)) 
	FB::error("Unable to add a directory to freshly created NotifyTask!");
    }
  }
  pLockSet.unlock(pLockID);
}
  
/**
 * Remove the given directory from the set of directories monitored for change notification.
 * 
 * param dir
 *   The directory to add relative to the root watched directory.
 */
void
NotifyMgr::removeDir
(
 const char* dir
) 
{
  if(pShutdown) 
    return;

  pLockSet.lock(pLockID);
  {    
    TaskList::iterator iter;
    for(iter = pTasks.begin(); iter != pTasks.end(); iter++)
	(*iter)->removeDir(dir);
  }
  pLockSet.unlock(pLockID);     
}
  


/*----------------------------------------------------------------------------------------*/
/*   T A S K   C O N T R O L                                                              */
/*----------------------------------------------------------------------------------------*/

/**
 * Wait for all tasks to exit.
 */ 
void
NotifyMgr::wait()
{
  FB::threadMsg("Waiting for Shutdown...", 1);
  pLockSet.lock(pShutdownID);

  pLockSet.lock(pLockID);
  {    
    TaskList::iterator iter;
    for(iter = pTasks.begin(); iter != pTasks.end(); iter++) {
      int code = (*iter)->wait();

      char msg[1024];
      sprintf(msg, "NotifyTask[%d] Exited = %d", (*iter)->getPID(), code);
      FB::threadMsg(msg, 2);
      
      delete (*iter);
    }
    pTasks.clear();
  }
  pLockSet.unlock(pLockID);  
}


/*----------------------------------------------------------------------------------------*/
/*   T A S K   H E L P E R S                                                              */
/*----------------------------------------------------------------------------------------*/

/**
 * Add the given directory to the list of recently modified directories.
 */ 
void 
NotifyMgr::modified
(
 const char* dir
)
{
  if(pShutdown) 
    return;

  pLockSet.lock(pLockID);
  {    
    pMonitorServer->modified(dir);  
  }
  pLockSet.unlock(pLockID);  
}


} // namespace Pipeline

