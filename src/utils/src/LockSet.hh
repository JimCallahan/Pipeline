// $Id: LockSet.hh,v 1.1 2004/04/06 08:55:49 jim Exp $

#ifndef PIPELINE_LOCK_SET_HH
#define PIPELINE_LOCK_SET_HH

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

#ifdef HAVE_ERRNO_H
#  include <errno.h>
#endif

#define SEMMSL 250

namespace Pipeline {

extern "C" 
inline void 
LockSetCleanup
(
 int exitCode, 
 void *lockSetID
);

/*------------------------------------------------------------------------------------------*/
/*   L O C K   S E T                                                                        */
/*                                                                                          */
/*     A manager of Locks.                                                                  */
/*------------------------------------------------------------------------------------------*/

class LockSet
{
public: 
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
   * Construct a new set of locks.
   */ 
  LockSet() 
  {
    pSemSetID = semget(IPC_PRIVATE, SEMMSL, 0666);
    if(pSemSetID == -1) {
      printf("FATAL ERROR: Unable to create the LockSet: %s", strerror(errno));
      exit(EXIT_FAILURE);
    }

    {
      int* id = new int;
      (*id) = pSemSetID;
      on_exit(LockSetCleanup, id);
    }

    {
      semun arg;
      arg.val = 1;
      if(semctl(pSemSetID, 0, SETVAL, arg) == -1) {
	printf("FATAL ERROR:Unable to initialize Lock (0): %s", strerror(errno));
	exit(EXIT_FAILURE);
      }

      pLocks[0] = true;
    }

    lock(0);
    {
      int wk;
      for(wk=0; wk<SEMMSL; wk++) 
	pLocks[wk] = false;
    }
    unlock(0);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   D E S T R U C T O R                                                                  */
  /*----------------------------------------------------------------------------------------*/

  ~LockSet()
  {}



  /*----------------------------------------------------------------------------------------*/
  /*   L O C K S                                                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the next available unused lock for use.
   * 
   * return 
   *   The ID of the lock or (-1) if there are no unused locks available.
   */
  int
  initLock()
  {
    int lockID = -1;

    lock(0);
    {
      int wk;
      for(wk=1; wk<SEMMSL; wk++) {
	if(!pLocks[wk]) {
	  lockID = wk;

	  pLocks[lockID] = true;
	  
	  semun arg;
	  arg.val = 1;
	  if(semctl(pSemSetID, lockID, SETVAL, arg) == -1) {
	    printf("FATAL ERROR: Unable to initialize Lock (%d): %s", 
		    lockID, strerror(errno));
	    exit(EXIT_FAILURE);
	  }

	  break;
	}	  
      }
    }
    unlock(0);
    
    return lockID;
  }

  /** 
   * Return the lock to the pool of unused locks.
   * 
   * param lockID
   *   The ID of the lock to release.
   */ 
  void
  releaseLock
  (
   int lockID
  ) 
  {
    assert(lockID > 0);
    assert(lockID < SEMMSL);

    lock(0);
    {
      if(pLocks[lockID]) {
	semun arg;
	arg.val = 0;
	if(semctl(pSemSetID, lockID, SETVAL, arg) == -1) {
	  printf("FATAL ERROR: Unable to release Lock (%d): %s", 
		  lockID, strerror(errno));
	  exit(EXIT_FAILURE);
	}
	
	pLocks[lockID] = false;
      }	  
    }
    unlock(0);
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   L O C K I N G                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Aquire the lock.
   * 
   * param lockID
   *   The ID of the lock to aquire.
   */
  void
  lock
  (
   int lockID
  )
  {
    assert(lockID >= 0);
    assert(lockID < SEMMSL);
    
    struct sembuf sb = {lockID, -1, 0}; 
    if(semop(pSemSetID, &sb, 1) == -1) {
      printf("FATAL ERROR: Unable to aquire Lock (%d): %s", 
	      lockID, strerror(errno));
      exit(EXIT_FAILURE);
    }
  }

  /**
   * Release the lock.
   * 
   * param lockID
   *   The ID of the lock to release.
   */
  void
  unlock
  (
   int lockID
  )
  {
    assert(lockID >= 0);
    assert(lockID < SEMMSL);

    struct sembuf sb = {lockID, 1, 0}; 
    if(semop(pSemSetID, &sb, 1) == -1) {
      printf("FATAL ERROR: Unable to release Lock (%d): %s", 
	     lockID, strerror(errno));
      exit(EXIT_FAILURE);
    }
  }


private:
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The semaphore set identifier.
   */ 
  int pSemSetID;

  /**
   * The table of active locks.
   */ 
  bool pLocks[SEMMSL];

};



/** 
 * C-Style exit(3) cleanup function.
 * 
 * param exitCode
 *   Argument to exit(3).
 * 
 * param lockSetID
 *   The ID of the lock set to cleanup.
 */ 
extern "C" 
inline void 
LockSetCleanup
(
 int exitCode, 
 void *lockSetID
) 
{
  int* id = (int*) lockSetID;

  // DEBUG
  printf("Removing the semaphore set (%d)!\n", *id);
  // DEBUG

  LockSet::semun arg;
  if(semctl(*id, 0, IPC_RMID, arg) == -1) 
    printf("ERROR: Unable to remove the semaphore set (%d): %s", *id, strerror(errno));

  delete id;
}

} // namespace Pipeline

#endif
