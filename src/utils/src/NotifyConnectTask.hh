// $Id: NotifyConnectTask.hh,v 1.1 2004/04/09 17:55:12 jim Exp $

#ifndef PIPELINE_NOTIFY_CONNECT_TASK_HH
#define PIPELINE_NOTIFY_CONNECT_TASK_HH

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

namespace Pipeline {

class NotifyMgr;

/*------------------------------------------------------------------------------------------*/
/*   N O T I F Y   C O N N E C T   T A S K                                                  */
/*                                                                                          */
/*    Manages a network connection.                                                         */
/*------------------------------------------------------------------------------------------*/

class NotifyConnectTask : public Task
{
public:
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new task.
   * 
   * param name
   *   The title of the task.
   * 
   * param mgr
   *   The notify task manager.
   * 
   * param sd
   *   The socket descriptor.
   */ 
  NotifyConnectTask
  (
   const char* name, 
   NotifyMgr& mgr, 
   int sd
  ) :
    Task(name), 
    pMgr(mgr), 
    pSocket(sd),
    pIsFinished(false)
  {}



  /*----------------------------------------------------------------------------------------*/
  /*   D E S T R U C T O R                                                                  */
  /*----------------------------------------------------------------------------------------*/

  ~NotifyConnectTask()
  {}



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Has the task been completed.
   */
  bool
  isFinished() 
  {
    return pIsFinished;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   T H R E A D S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Spawn a new thread to execute the task.
   * 
   * Overrides Task::spawn to also share the file descriptors with its parent thread.
   * This is needed so that socket descriptors can be shared with the NotifyServerGn<T>
   * instance which spawned this instance.
   * 
   * param stackSize
   *   The size in bytes of the new thread's stack.
   */
  void 
  spawn
  (
   long stackSize = 65536
  ) 
  {
    assert(stackSize > 0);
    char msg[1024];

    pStack = new char[stackSize];
    pPID = clone(TaskLauncher, (void*) (pStack+stackSize-1), CLONE_VM | CLONE_FILES, 
		 (void*) this);
    if(pPID == -1) {
      sprintf(msg, "Unable to spawn task thread: %s", strerror(errno));
      FB::error(msg);
    } 
  }



protected:
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent task manager.
   */
  NotifyMgr&  pMgr;


  /**
   * The socket descriptor.
   */ 
  int pSocket;

  /**
   * Has the task been completed.
   */ 
  bool pIsFinished;

};


} // namespace Pipeline

#endif
