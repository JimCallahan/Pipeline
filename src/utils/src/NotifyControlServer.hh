// $Id: NotifyControlServer.hh,v 1.1 2004/04/06 08:58:09 jim Exp $

#ifndef PIPELINE_NOTIFY_CONTROL_SERVER_HH
#define PIPELINE_NOTIFY_CONTROL_SERVER_HH

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
#include <NotifyMgr.hh>
#include <NotifyControlTask.hh>

namespace Pipeline {

/*------------------------------------------------------------------------------------------*/
/*   N O T I F Y   C O N T R O L   S E R V E R                                              */
/*                                                                                          */
/*    Listens for notify control network connections and spawns a NotifyControlTask task    */
/*    for each incoming connection.                                                         */
/*------------------------------------------------------------------------------------------*/

class NotifyControlServer : public Task
{
private: 
  /*----------------------------------------------------------------------------------------*/
  /*   T Y P E S                                                                            */
  /*----------------------------------------------------------------------------------------*/

  typedef std::list<NotifyControlTask*>  TaskList;


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
   * param port
   *   The network port number.
   */ 
  NotifyControlServer
  (
   NotifyMgr& mgr, 
   int port
  ) :
    Task("NotifyControlServer"), 
    pMgr(mgr), 
    pLockSet(mgr.getLockSet()), 
    pPort(port)
  { 
    pLockID = pLockSet.initLock();
    assert(pLockID != -1);

    assert(pPort > 0);      
  }



  /*----------------------------------------------------------------------------------------*/
  /*   D E S T R U C T O R                                                                  */
  /*----------------------------------------------------------------------------------------*/

  ~NotifyControlServer()
  {
    pLockSet.releaseLock(pLockID);
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

    FB::stageBegin("Starting Control Server...");
    {
      /* initialize the network socket */ 
      int sd = Network::socket();    
      Network::setReuseAddr(sd, true);
      Network::bind(sd, pPort);
      Network::listen(sd);

      {
	char msg[1024];
	sprintf(msg, "Listening on Port: %d", pPort);
	FB::stageMsg(msg);
      }

      /* listen for incoming connections */ 
      while(true) {
	int csd = Network::accept(sd);
	if(pMgr.isShutdown())
	  break;

	/* spawn a task to handle the connection */ 
	pLockSet.lock(pLockID);
	{
	 NotifyControlTask* task = new NotifyControlTask(pMgr, csd);
	 pTasks.push_back(task);
	  
	 task->spawn();
	}
	pLockSet.unlock(pLockID);
      }

      /* wait for the tasks to complete */ 
      pLockSet.lock(pLockID);
      {
	TaskList::iterator iter;
	for(iter = pTasks.begin(); iter != pTasks.end(); iter++) {
	  int code = (*iter)->wait();

	  // DEBUG 
	  printf("NotifyControlTask[%d]: Exit Code = %d\n", (*iter)->getPID(), code);
	  // DEBUG 
	  
	  delete (*iter);
	}
	pTasks.clear();
      }   
      pLockSet.unlock(pLockID);
    }
    FB::stageEnd();

    return EXIT_SUCCESS;
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
   * The network port number.
   */ 
  int pPort;


  /**
   * The ID of the lock which protects access to the task list.
   */
  int pLockID;

  /**
   * The list of managed tasks.
   */ 
  TaskList  pTasks;

};


} // namespace Pipeline

#endif
