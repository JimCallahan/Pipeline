// $Id: Task.hh,v 1.4 2004/04/09 17:55:12 jim Exp $

#ifndef PIPELINE_TASK_HH
#define PIPELINE_TASK_HH

#ifdef HAVE_CONFIG_H
#  include "config.h"
#endif

#ifdef HAVE_SYS_TYPES_H
#  include <sys/types.h>
#endif

#ifdef HAVE_SYS_WAIT_H
#  include <sys/wait.h>
#endif

#include <PackageInfo.hh>

namespace Pipeline {

/*------------------------------------------------------------------------------------------*/
/*   T A S K                                                                                */
/*                                                                                          */
/*    The abstract base class of thread tasks.                                              */
/*------------------------------------------------------------------------------------------*/

extern "C" 
inline int TaskLauncher(void *obj);

class Task
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
   */ 
  Task
  (
   const char* name = "UnknownTask"
  ) : 
    pName(strdup(name)), 
    pPID(-1), 
    pStack(NULL)
  {}



  /*----------------------------------------------------------------------------------------*/
  /*   D E S T R U C T O R                                                                  */
  /*----------------------------------------------------------------------------------------*/

  ~Task()
  {
    delete[] pName;

    if(pStack != NULL)
      delete[] pStack;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * 
   */ 
  const char* 
  getName() const
  {
    return pName;
  }

  /**
   * Get the OS process ID for the thread.
   */ 
  int 
  getPID() const 
  {
    return pPID;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   R U N                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Run the task. 
   */ 
  virtual int
  run() = 0;



  /*----------------------------------------------------------------------------------------*/
  /*   T H R E A D S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Spawn a new thread to execute the task.
   * 
   * param stackSize
   *   The size in bytes of the new thread's stack.
   */
  void 
  spawn
  (
   long stackSize = 65536
  ) 
    throw(std::runtime_error)
  {
    assert(stackSize > 0);
    char msg[1024];

    pStack = new char[stackSize];
    pPID = clone(TaskLauncher, (void*) (pStack+stackSize-1), CLONE_VM, (void*) this);
    if(pPID == -1) {
      sprintf(msg, "Unable to spawn task thread %s: %s", pName, strerror(errno));
      throw std::runtime_error(msg);
    }      
  }

  /**
   * Wait for the task thread to exit.
   * 
   * return 
   *   The exit code returned from the Task::run() method.
   */ 
  int 
  wait() const 
    throw(std::runtime_error)
  {
    assert(pPID > 0);
    char msg[1024];

    int status;
    if(waitpid(pPID, &status, __WCLONE) == -1) {
      switch(errno) {
      case ECHILD:
	return EXIT_SUCCESS;

      default:	
	sprintf(msg, "Unable to wait for %s[%d] to exit: %s", 
		pName, pPID, strerror(errno));
	throw std::runtime_error(msg);
      }
    }

    if(WIFEXITED(status))
      return WEXITSTATUS(status);
    else if(WIFSIGNALED(status)) {
      sprintf(msg, "%s[%d]: Exited due to signal (%d)!", 
	      pName, pPID, WTERMSIG(status));
      FB::warn(msg);
    }
    else if(WCOREDUMP(status)) {
      sprintf(msg, "%s[%d]: Core dumped!", pName, pPID);
      throw std::runtime_error(msg);
    }

    return EXIT_FAILURE;
  }



protected:
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the task.
   */ 
  char* pName; 

  /**
   * The OS process ID of the thread running the task.
   */ 
  int pPID; 

  /** 
   * The thread's stack.
   */
  char* pStack;
};


/** 
 * C-Style wrapper function used to spawn a thread which will run the Task using clone(2).
 * 
 * param obj
 *   The Task object.
 */ 
extern "C" 
inline int 
TaskLauncher
(
 void *obj
) 
{
  try {
    Task* task = (Task*) obj;
    return (task->run());
  } 
  catch(std::runtime_error e) {
    printf("TASK RUNTIME EXCEPTION: %s\n", e.what());
    return EXIT_FAILURE;
  }
  catch(std::exception e) {
    printf("UNCAUGHT TASK EXCEPTION: %s\n", e.what());
    return EXIT_FAILURE;
  }
}



} // namespace Pipeline

#endif
