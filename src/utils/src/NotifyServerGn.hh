// $Id: NotifyServerGn.hh,v 1.1 2004/04/09 17:55:12 jim Exp $

#ifndef PIPELINE_NOTIFY_SERVER_GN_HH
#define PIPELINE_NOTIFY_SERVER_GN_HH

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
#include <Network.hh>
#include <NotifyMgr.hh>

namespace Pipeline {

class NotifyMgr;

/*------------------------------------------------------------------------------------------*/
/*   N O T I F Y   S E R V E R   G N                                                        */
/*                                                                                          */
/*    Listens for network connections and spawns a task for each incoming connection.       */
/*------------------------------------------------------------------------------------------*/

template<class T> 
class NotifyServerGn : public Task
{
private: 
  /*----------------------------------------------------------------------------------------*/
  /*   T Y P E S                                                                            */
  /*----------------------------------------------------------------------------------------*/

  typedef std::list<T*>  TaskList;


public:
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new task.
   * 
   * param name
   *   The title of the server task.
   * 
   * param tname
   *   The title of the spawned tasks.
   * 
   * param mgr
   *   The notify task manager.
   * 
   * param port
   *   The network port number.
   */ 
  NotifyServerGn
  (
   const char* name, 
   const char* tname, 
   NotifyMgr& mgr, 
   int port
  ) :
    Task(name),
    pTaskName(strdup(tname)),
    pMgr(mgr), 
    pLockSet(mgr.getLockSet()), 
    pPort(port)
  { 
    pLockID = pLockSet.initLock();
    assert(pLockID != -1);
    printf("%s::pLockID = %d\n", name, pLockID);

    assert(pPort > 0);      
  }



  /*----------------------------------------------------------------------------------------*/
  /*   D E S T R U C T O R                                                                  */
  /*----------------------------------------------------------------------------------------*/

  ~NotifyServerGn()
  {
    delete[] pTaskName;
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

    FB::threadMsg("Started", 1, pName, pPID);
    {
      /* initialize the network socket */ 
      int sd = Network::socket();    
      Network::setReuseAddr(sd, true);
      Network::bind(sd, pPort);
      Network::listen(sd);

      {
	sprintf(msg, "Listening on Port: %d", pPort);
	FB::threadMsg(msg, 2, pName, pPID);
      }

      /* listen for incoming connections */ 
      while(true) {
	int csd = Network::accept(sd);
	if(pMgr.isShutdown())
	  break;

	/* spawn a task to handle the connection */ 
	pLockSet.lock(pLockID);
	{
	  T* task = new T(pMgr, csd);
	  pTasks.push_back(task);
	  
	 task->spawn();

	 FB::threadMsg("Connection Opened", 3, task->getName(), task->getPID());
	}
	pLockSet.unlock(pLockID);
      }

      /* wait for the tasks to complete */ 
      pLockSet.lock(pLockID);
      {
	typename TaskList::iterator iter;
	for(iter = pTasks.begin(); iter != pTasks.end(); iter++) {
	  int code = (*iter)->wait();
	  
	  char msg[1024];
	  sprintf(msg, "%s[%d] Exited = %d", pTaskName, (*iter)->getPID(), code);
	  FB::threadMsg(msg, 3, pName, pPID);
	  
	  delete (*iter);
	}
	pTasks.clear();
      }   
      pLockSet.unlock(pLockID);
    }
    FB::threadMsg("Finished", 1, pName, pPID);

    return EXIT_SUCCESS;
  }



protected:
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent task manager.
   */
  char* pTaskName;

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
