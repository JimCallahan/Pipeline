// $Id: NotifyMgr.cc,v 1.1 2004/04/05 06:27:55 jim Exp $

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
  pLock.lock();
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
      NotifyTask* task = new NotifyTask(this, pRootDir);
      pTasks.push_back(task);
      
      task->spawn();
      
      if(!task->addDir(dir)) 
	FB::error("Unable to add a directory to freshly created NotifyTask!");
    }
  }
  pLock.unlock();      
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
  pLock.lock();
  {    
    TaskList::iterator iter;
    for(iter = pTasks.begin(); iter != pTasks.end(); iter++)
	(*iter)->removeDir(dir);
  }
  pLock.unlock();     
}
  


/*----------------------------------------------------------------------------------------*/
/*   T A S K   C O N T R O L                                                              */
/*----------------------------------------------------------------------------------------*/

/**
 * Order the task to exit as soon as possible.
 */ 
void
NotifyMgr::shutdown()
{
  pLock.lock();
  {    
    TaskList::iterator iter;
    for(iter = pTasks.begin(); iter != pTasks.end(); iter++) 
      (*iter)->shutdown();
  }
  pLock.unlock();  
}

/**
 * Wait for all tasks to exit.
 */ 
void
NotifyMgr::wait()
{
  pLock.lock();
  {    
    TaskList::iterator iter;
    for(iter = pTasks.begin(); iter != pTasks.end(); iter++) {
      int code = (*iter)->wait();
      
      // DEBUG 
      printf("NotifyTask[%d]: Exit Code = %d\n", (*iter)->getPID(), code);
      // DEBUG 
      
      delete (*iter);
    }
    pTasks.clear();
  }
  pLock.unlock();  
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
  pLock.lock();
  {    

    // DEBUG
    printf("MODIFIED: %s/%s\n", pRootDir, dir);
    // DEBUG

  }
  pLock.unlock();  
}


} // namespace Pipeline

