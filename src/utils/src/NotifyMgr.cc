// $Id: NotifyMgr.cc,v 1.3 2004/04/06 15:42:57 jim Exp $

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
    // DEBUG 
    char msg[1024];
    sprintf(msg, "MODIFIED: %s/%s", pRootDir, dir);
    FB::threadMsg(msg, 4);
    // DEBUG 

    
    // distribute the diretory to the NotifyMonitorTask(s)... 

  }
  pLockSet.unlock(pLockID);  
}


} // namespace Pipeline

