// $Id: NotifyMgr.hh,v 1.2 2004/04/05 06:27:55 jim Exp $

#ifndef PIPELINE_NOTIFY_MGR_HH
#define PIPELINE_NOTIFY_MGR_HH

#ifdef HAVE_CONFIG_H
#  include "config.h"
#endif

#ifdef HAVE_LIST
#  include <list>
#endif

#include <Lock.hh>
#include <NotifyTask.hh>

namespace Pipeline {

class NotifyTask;

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

class NotifyMgr 
{
private: 
  /*----------------------------------------------------------------------------------------*/
  /*   T Y P E S                                                                            */
  /*----------------------------------------------------------------------------------------*/

  typedef std::list<NotifyTask*> TaskList;

  
private:
  /*----------------------------------------------------------------------------------------*/
  /*   F R I E N D S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  friend class NotifyTask;


public:
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new notify task manager.
   * 
   * param dir 
   *   The root directory of all watched directories.
   */ 
  NotifyMgr
  ( 
   const char* dir
  ) : 
    pRootDir(strdup(dir))
  {
  }


  /*----------------------------------------------------------------------------------------*/
  /*   D E S T R U C T O R                                                                  */
  /*----------------------------------------------------------------------------------------*/

  ~NotifyMgr()
  {
    delete[] pRootDir;
    
    assert(pTasks.empty());
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
  addDir
  (
   const char* dir
  );
  
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
  );


  /*----------------------------------------------------------------------------------------*/
  /*   T A S K   C O N T R O L                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Order the task to exit as soon as possible.
   */ 
  void
  shutdown();

  /**
   * Wait for all tasks to exit.
   */ 
  void
  wait();



private:
  /*----------------------------------------------------------------------------------------*/
  /*   T A S K   H E L P E R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Add the given directory to the list of recently modified directories.
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
   * Protects access to the internal variables.
   */
  Lock  pLock;

  /** 
   * The root directory of all watched directories.
   */
  const char* pRootDir;

  /**
   * The list of managed tasks.
   */ 
  TaskList  pTasks;

};


} // namespace Pipeline

#endif
