// $Id: NotifyMonitorTask.hh,v 1.1 2004/04/09 17:55:12 jim Exp $

#ifndef PIPELINE_NOTIFY_MONITOR_TASK_HH
#define PIPELINE_NOTIFY_MONITOR_TASK_HH

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

#include <Network.hh>
#include <NotifyConnectTask.hh>
#include <NotifyServerGn.hh>

namespace Pipeline {

/*------------------------------------------------------------------------------------------*/
/*   N O T I F Y   C O N T R O L   T A S K                                                  */
/*                                                                                          */
/*    Manages a network connection with the Java based NotifyMonitorClient class.           */
/*                                                                                          */
/*    The protocol consists one-way messages send from the NotifyMonitorClient to this      */
/*    class.  Each message is exactly 1032 bytes long and contains the following data:      */
/*                                                                                          */
/*    Add a Directory:                                                                      */
/*      0-7     "ADD_____"                                                                  */
/*      8-1031  dir  (all unused bytes == '\0')                                             */
/*                                                                                          */
/*    Remove a Directory:                                                                   */
/*      0-7     "REMOVE__"                                                                  */
/*      1-1031  dir  (all unused bytes == '\0')                                             */
/*                                                                                          */
/*    Shutdown the Server:                                                                  */
/*      0-7     "SHUTDOWN"                                                                  */
/*      1-1031  (all bytes == '\0')                                                         */
/*                                                                                          */
/*    All directory names will be relative to the root production directory.                */
/*------------------------------------------------------------------------------------------*/

template<int MAXDIRS>
class NotifyMonitorTask : public NotifyConnectTask
{
public:
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new task.
   * 
   * param mgr
   *   The notify task manager.
   * 
   * param sd
   *   The socket descriptor.
   */ 
  NotifyMonitorTask
  (
   NotifyMgr& mgr, 
   int sd
  ) :
    NotifyConnectTask("NotifyMonitorTask", mgr, sd), 
    pLockSet(mgr.getLockSet())
  {
    pHoldID = pLockSet.initLock();
    assert(pHoldID != -1);
    printf("NotifyMonitorTask::pHoldID = %d\n", pHoldID);
    pLockSet.lock(pHoldID);
    
    pLockID = pLockSet.initLock();
    assert(pLockID != -1);
    printf("NotifyMonitorTask::pLockID = %d\n", pLockID);

    pNumDirs = 0;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   D E S T R U C T O R                                                                  */
  /*----------------------------------------------------------------------------------------*/

  ~NotifyMonitorTask()
  {
    pLockSet.releaseLock(pHoldID);
    pLockSet.releaseLock(pLockID);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Add the given directory to the list of recently modified directories.
   */ 
  void 
  modified
  (
   const char* dir
  )
  {
    if(isFinished()) 
      return;

    char msg[1024];
    pLockSet.lock(pLockID);
    {
      sprintf(msg, "Modified: %s", dir);
      FB::threadMsg(msg, 4, pName, pPID);

      bool found = false;
      int wk;
      for(wk=0; wk<pNumDirs; wk++) {
	if(strcmp(pDirs[wk], dir) == 0) {
	  found = true;
	  break;
	}	  
      }
      
      sprintf(msg, "NumDirs: %d  Found: %s", pNumDirs, found ? "YES" : "no");
      FB::threadMsg(msg, 5, pName, pPID);
      

      if(found) {
	sprintf(msg, "Duplicate: %s", dir);
	FB::threadMsg(msg, 5, pName, pPID);	
      }
      else if(strlen(dir) > 1023)  {
	sprintf(msg, "Directory name too long (>1024): %s", dir);
	FB::warn(msg);
      }
      else {
	assert(pNumDirs < MAXDIRS);

	char* data = new char[1024];
	memset(data, 0, sizeof(data));
	strcpy(data, dir);

	pDirs[pNumDirs] = data;
	
	sprintf(msg, "Queued: %s [%d]", dir, pNumDirs);
	FB::threadMsg(msg, 5, pName, pPID);
	
	pNumDirs++;
      }
    }
    pLockSet.unlock(pLockID);
    pLockSet.unlock(pHoldID);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   T A S K                                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Run the task.
   */ 
  virtual int
  run()
  {
    char msg[1024];
    bool done = false;
    while(!done && !pMgr.isShutdown()) {
      pLockSet.lock(pHoldID);
      pLockSet.lock(pLockID);
      {
	int wk;
	for(wk=0; wk<pNumDirs; wk++) {
	  assert(pDirs[wk] != NULL);
	  
	  sprintf(msg, "Sending: %s", pDirs[wk]);
	  FB::threadMsg(msg, 4, pName, pPID);

	  if(Network::write(pSocket, pDirs[wk], 1024) != 1024) {
	    done = true;
	    break;
	  }	    
	  
	  delete[] (pDirs[wk]);
	  pDirs[wk] = NULL;
	}

	pNumDirs = 0;
      }
      pLockSet.unlock(pLockID);
    }

    FB::threadMsg("Connection Closed.", 3, pName, pPID);

    pIsFinished = true;
    return EXIT_SUCCESS;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/


  /**
   * The shared set of locks.
   */
  LockSet& pLockSet;


  /**
   * The ID of the lock which causes the task to wait for new modification events 
   * when the modified directory table is empty.
   */
  int pHoldID;

  /**
   * The ID of the lock which protects access to modified directories table.
   */
  int pLockID;
  
  /**
   * The table of modified directory names.
   * The count of recently modified directories.
   */
  char* pDirs[MAXDIRS];
  int   pNumDirs;

};


} // namespace Pipeline

#endif
