// $Id: NotifyTask.hh,v 1.3 2004/04/06 08:58:52 jim Exp $

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
#include <LockSet.hh>
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
   NotifyMgr& mgr, 
   const char* dir
  );


  /*----------------------------------------------------------------------------------------*/
  /*   D E S T R U C T O R                                                                  */
  /*----------------------------------------------------------------------------------------*/

  ~NotifyTask()
  {
    delete[] pRootDir;
    pLockSet.releaseLock(pLockID);
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

    pLockSet.lock(pLockID);
    {
      assert(dir != NULL);
      if((pNumAdd + pNumDirs) < 1000) {
	if(!containsDir(pAdd, pNumAdd, dir)) {
	  pAdd[pNumAdd] = strdup(dir);
	  pNumAdd++;
	  added = true;
	}
      }
    }
    pLockSet.unlock(pLockID);

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
    pLockSet.lock(pLockID);
    {
      assert(pNumRemove < 1024);
      if(!containsDir(pRemove, pNumRemove, dir)) {
	pRemove[pNumRemove] = strdup(dir);
	pNumRemove++;
      }      
    }
    pLockSet.unlock(pLockID);
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   T A S K                                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Run the task.
   */ 
  virtual int
  run();


  
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



private:
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent task manager.
   */
  NotifyMgr&  pMgr;

  /**
   * The shared set of locks.
   */
  LockSet& pLockSet;


  /**
   * The ID of the lock which protects access to the following internal variables.
   */
  int pLockID;

  /** 
   * The root directory of all watched directories.
   */
  const char* pRootDir;

  /**
   * Directories to add to the monitored list.
   * The count of the number of directories waiting to be added.
   */ 
  char* pAdd[1024];
  int   pNumAdd;

  /**
   * Directories to remove from the monitored list.
   * The count of the number of directories waiting to be removed.
   */ 
  char* pRemove[1024];
  int   pNumRemove;

  /**
   * The table of monitored directory names indexed by open file descriptor.
   * The total number of actively monitored directories (i.e. not NULL).
   */
  char* pDirs[1024];
  int   pNumDirs;
};


} // namespace Pipeline

#endif
