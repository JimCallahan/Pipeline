// $Id: Lock.hh,v 1.1 2004/04/05 05:50:07 jim Exp $

#ifndef PIPELINE_LOCK_HH
#define PIPELINE_LOCK_HH

#ifdef HAVE_CONFIG_H
#  include "config.h"
#endif

#ifdef HAVE_SYS_TYPES_H
#  include <sys/types.h>
#endif

#ifdef HAVE_SYS_IPC_H
#  include <sys/ipc.h>
#endif

#ifdef HAVE_SYS_SEM_H
#  include <sys/sem.h>
#endif

#include <FB.hh>

namespace Pipeline {

/*------------------------------------------------------------------------------------------*/
/*   L O C K                                                                                */
/*                                                                                          */
/*     A mutual exclusion thread lock.                                                      */
/*------------------------------------------------------------------------------------------*/

class Lock
{
private: 
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  typedef union 
  {
    int val;			
    struct semid_ds *buf;	
    unsigned short int *array;
    struct seminfo *__buf;	
  } semun;


public:
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new lock.
   */ 
  Lock() 
  {
    char msg[1024];

    pSemID = semget(IPC_PRIVATE, 1, 0666);
    if(pSemID == -1) {
      sprintf(msg, "Unable to create Lock: %s", strerror(errno));
      FB::error(msg);
    }

    semun arg;
    arg.val = 1;
    if(semctl(pSemID, 0, SETVAL, arg) == -1) {
      sprintf(msg, "Unable to initialize Lock: %s", strerror(errno));
      FB::error(msg);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   D E S T R U C T O R                                                                  */
  /*----------------------------------------------------------------------------------------*/

  ~Lock()
  { 
    semun arg;
    if(semctl(pSemID, 0, IPC_RMID, arg) == -1) {
      char msg[1024];
      sprintf(msg, "Unable to remove Lock: %s", strerror(errno));
      FB::error(msg);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Aquire the lock.
   */
  void
  lock()
  {
    struct sembuf sb = {0, -1, 0}; 
    if(semop(pSemID, &sb, 1) == -1) {
      char msg[1024];
      sprintf(msg, "Unable to aquire Lock: %s", strerror(errno));
      FB::error(msg);
    }
  }

  /**
   * Release the lock.
   */
  void
  unlock()
  {
    struct sembuf sb = {0, 1, 0}; 
    if(semop(pSemID, &sb, 1) == -1) {
      char msg[1024];
      sprintf(msg, "Unable to release Lock: %s", strerror(errno));
      FB::error(msg);
    }
  }


protected:
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The semaphore set identifier.
   */ 
  int pSemID;

};


} // namespace Pipeline

#endif
