// $Id: NotifyControlTask.hh,v 1.2 2004/04/06 15:42:57 jim Exp $

#ifndef PIPELINE_NOTIFY_CONTROL_TASK_HH
#define PIPELINE_NOTIFY_CONTROL_TASK_HH

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
#include <Network.hh>

namespace Pipeline {

/*------------------------------------------------------------------------------------------*/
/*   N O T I F Y   C O N T R O L   T A S K                                                  */
/*                                                                                          */
/*    Manages a network connection with the Java based NotifyControlClient class.           */
/*                                                                                          */
/*    The protocol consists one-way messages send from the NotifyControlClient to this      */
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

class NotifyControlTask : public Task
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
  NotifyControlTask
  (
   NotifyMgr& mgr, 
   int sd
  ) :
    Task("NotifyControlTask"), 
    pMgr(mgr), 
    pSocket(sd)
  {}



  /*----------------------------------------------------------------------------------------*/
  /*   D E S T R U C T O R                                                                  */
  /*----------------------------------------------------------------------------------------*/

  ~NotifyControlTask()
  {}



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
    char data[1033];
    data[1033] = '\0';
    while(!pMgr.isShutdown()) {
      int num = Network::read(pSocket, data, 1032);
      if((num == -1) || (num < 1032)) {
	FB::warn("Illegible message recieved!");
	return EXIT_FAILURE;
      }

      if(strncmp(data, "ADD_____", 8) == 0) {
	sprintf(msg, "Add Directory: %s", data+8);
	FB::threadMsg(msg, 4, pName, pPID);

	pMgr.addDir(data+8); 
      }
      else if(strncmp(data, "REMOVE__", 8) == 0) {
	sprintf(msg, "Remove Directory: %s", data+8);
	FB::threadMsg(msg, 4, pName, pPID);

	pMgr.removeDir(data+8); 
      }
      else if(strncmp(data, "CLOSE___", 8) == 0) {
	FB::threadMsg("Close", 4, pName, pPID);
	break;
      }
      else if(strncmp(data, "SHUTDOWN", 8) == 0) {
	FB::threadMsg("Shutdown", 4, pName, pPID);

	pMgr.shutdown();
      }
      else {
	FB::warn("Illegal message recieved!");
	return EXIT_FAILURE;
      }
    }

    FB::threadMsg("Connection Closed.", 3, pName, pPID);

    return EXIT_SUCCESS;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   T H R E A D S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Spawn a new thread to execute the task.
   * 
   * Overrides Task::spawn to also share the file descriptors with its parent thread.
   * This is needed so that socket descriptors can be shared with the NotifyControlServer
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



private:
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

};


} // namespace Pipeline

#endif
