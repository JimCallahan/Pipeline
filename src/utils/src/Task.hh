// $Id: Task.hh,v 1.1 2004/04/05 05:50:07 jim Exp $

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
   */ 
  Task() : 
    pPID(-1), 
    pStack(NULL)
  {}



  /*----------------------------------------------------------------------------------------*/
  /*   D E S T R U C T O R                                                                  */
  /*----------------------------------------------------------------------------------------*/

  ~Task()
  {
    if(pStack != NULL)
      delete[] pStack;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the OS process ID for the thread.
   */ 
  int 
  getPID() 
  {
    return pPID;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   R U N                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Run the task. 
   */ 
  virtual void 
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
  {
    assert(stackSize > 0);
    char msg[1024];

    pStack = new char[stackSize];
    pPID = clone(TaskLauncher, (void*) (pStack+stackSize-1), CLONE_VM, (void*) this);
    if(pPID == -1) {
      sprintf(msg, "Unable to spawn task thread: %s", strerror(errno));
      FB::error(msg);
    }      

    printf("Spawned Thread (%d)\n", pPID);
  }

  /**
   * Wait for the task thread to exit.
   * 
   * return 
   *   The exit code returned from the Task::run() method.
   */ 
  int 
  wait()
  {
    assert(pPID > 0);
    char msg[1024];

    int status;
    if(waitpid(pPID, &status, __WCLONE) == -1) {
      sprintf(msg, "Unable to wait for task thread (%d): %s", pPID, strerror(errno));
      FB::error(msg);
    }

    if(WIFEXITED(status))
      return WEXITSTATUS(status);
    else if(WIFSIGNALED(status)) {
      sprintf(msg, "Task thread (%d) exited due to signal (%d)!", pPID, WTERMSIG(status));
      FB::error(msg);
    }
    else if(WCOREDUMP(status)) {
      sprintf(msg, "Task thread (%d) core dumped!", pPID);
      FB::error(msg);
    }

    return -1;
  }



protected:
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

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
  Task* task = (Task*) obj;
  task->run();

  assert(false);
  return 0;
}



} // namespace Pipeline

#endif
