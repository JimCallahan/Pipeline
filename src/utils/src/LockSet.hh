// $Id: LockSet.hh,v 1.2 2004/04/09 17:55:12 jim Exp $

#ifndef PIPELINE_LOCK_SET_HH
#define PIPELINE_LOCK_SET_HH

#ifdef HAVE_CONFIG_H
#  include "config.h"
#endif

#ifdef HAVE_CASSERT
#  include <cassert>
#else
#  ifdef HAVE_ASSERT_H
#    include <assert.h>
#  endif
#endif

#ifdef HAVE_STDEXCEPT
#  include <stdexcept>
#endif

#ifdef HAVE_UNISTD_H
#  include <unistd.h>
#endif

#ifdef HAVE_CSTDLIB
#  include <cstdlib>
#else
#  ifdef HAVE_STDLIB_H
#    include <stdlib.h>
#  endif
#endif

#ifdef HAVE_CSTDIO
#  include <cstdio>
#else
#  ifdef HAVE_STDIO_H
#    include <stdio.h>
#  endif
#endif

#ifdef CSTRING_H
#  include <cstring>
#else 
#  ifdef HAVE_STRING_H
#    include <string.h>
#  endif
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

#ifdef HAVE_SYS_WAIT_H
#  include <sys/wait.h>
#endif

#ifdef HAVE_ERRNO_H
#  include <errno.h>
#endif

#define SEMMNI 128 
#define SEMMSL 250

namespace Pipeline {

/*------------------------------------------------------------------------------------------*/
/*   L O C K   S E T                                                                        */
/*                                                                                          */
/*     A manager of Locks.                                                                  */
/*------------------------------------------------------------------------------------------*/

extern "C" 
inline int LockSetCleanup(void *obj);

class LockSet
{
public:
  /*----------------------------------------------------------------------------------------*/
  /*   T Y P E S                                                                            */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Datastructure used by low-level semaphore operations.
   */ 
  typedef union {
    int val;			
    struct semid_ds *buf;	
    unsigned short int *array;
    struct seminfo *__buf;	
  } semun;

  class SemSet;
 
  /**
   * An opaque identifier of a lock.
   */ 
  class Lock
  {
  private:
    friend class LockSet;
    friend class LockSet::SemSet;

  private:
    Lock
    ( 
     int setIdx, 
     int setID, 
     int semID
    ) :
      pSetIdx(setIdx),
      pSetID(setID), 
      pSemID(semID)
    {}

  private:
    int pSetIdx;
    int pSetID;
    int pSemID;
  };


  /**
   * A manager of active semphores within a semaphore set.
   */ 
  class SemSet 
  {
  public:
    /** 
     * Construct a new semaphore set with the given index and ID.
     */ 
    SemSet
    (
     int setIdx, 
     int setID
    ) :
      pSetIdx(setIdx),
      pSetID(setID)
    {
      int wk;
      for(wk=0; wk<SEMMSL; wk++) 
	pLocks[wk] = NULL;
    }
    
    /** 
     * Destructor.
     */ 
    ~SemSet() 
    {
      int wk;
      for(wk=0; wk<SEMMSL; wk++) {
	if(pLocks[wk] != NULL) {
	  delete (pLocks[wk]);
	  pLocks[wk] = NULL;
	}
      }
    }
    

  public:
    /**
     * Get the ID of this semaphore set.
     */ 
    int 
    getID() 
    {
      return pSetID;
    }

    /**
     * Get the Lock object for the semphore with the given ID.
     */ 
    Lock* 
    getLock
    (
     int semID
    ) 
    {
      return pLocks[semID];
    }

    /**
     * Initialize the next available Lock.
     * 
     * return 
     *   The Lock or (NULL) if none are available.
     */ 
    Lock* 
    newLock() 
      throw(std::runtime_error)
    {
      char msg[1024];
      int wk;
      for(wk=0; wk<SEMMSL; wk++) {
	if(pLocks[wk] == NULL) {
	  semun arg;
	  arg.val = 1;
	  if(semctl(pSetID, wk, SETVAL, arg) == -1) {
	    sprintf(msg, "Unable to initialize semaphore (%d) from set (%d): %s", 
		    wk, pSetID, strerror(errno));
	    throw std::runtime_error(msg);
	  }

	  pLocks[wk] = new Lock(pSetIdx, pSetID, wk);
	  return (pLocks[wk]);
	}
      }
      
      return NULL;
    }

    /**
     * Release the Lock with the given semaphore ID.
     */ 
    void 
    freeLock
    (
     Lock* lock
    ) 
      throw(std::runtime_error)
    {
      assert(lock != NULL);
      assert(lock->pSemID >= 0);
      assert(lock->pSemID < SEMMSL);
      assert(lock->pSetIdx == pSetIdx);
      
      char msg[1024];
      semun arg;
      arg.val = 0;
      if(semctl(pSetID, lock->pSemID, SETVAL, arg) == -1) {
	sprintf(msg, "Unable to release semaphore (%d) from set (%d): %s", 
	       lock->pSemID, pSetID, strerror(errno));
	throw std::runtime_error(msg);
      }

      assert(pLocks[lock->pSemID] != NULL);
      pLocks[lock->pSemID] = NULL;

      delete lock;
    }


  private:
    int    pSetIdx;
    int    pSetID;
    Lock*  pLocks[SEMMSL];
  };
      


public: 
  /*----------------------------------------------------------------------------------------*/
  /*   I N I T I A L I Z A T I O N                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the semaphore set table and allocate the first semaphore set.
   *
   * This methods must be called before using any other LockSet methods and before any 
   * threads have been spawned.
   */ 
  static void
  init() 
    throw(std::runtime_error)
  {
    char msg[1024];

    /* intialize the semaphore sets table */ 
    {
      assert(sSemSets == NULL);
      sSemSets = new SemSet*[SEMMNI];
      int wk;
      for(wk=0; wk<SEMMNI; wk++) 
	sSemSets[wk] = NULL;
    }

    /* initialize the first semaphore set */ 
    SemSet* sset = NULL;
    {
      int setID = semget(IPC_PRIVATE, SEMMSL, 0666);
      if(setID == -1) {
	sprintf(msg, "Unable to create the initial semaphore set: %s", 
		strerror(errno));
	throw std::runtime_error(msg);
      }

      sset = new SemSet(0, setID);
      sSemSets[0] = sset;
    }
    
    /* initialize the master lock */ 
    sMasterLock = sset->newLock();

    /* initialize and the cleanup lock */ 
    sCleanupLock = sset->newLock();

    /* spawn a thread which will wait until the clean lock is unlocked to perform 
       final cleanup of the semaphore sets */ 
    {
      lock(sCleanupLock);

      int stackSize = 65536;
      char* stackMem = new char[stackSize];
      sCleanupPID = clone(LockSetCleanup, (void*) (stackMem+stackSize-1), CLONE_VM, 
			  (void*) sSemSets);
      if(sCleanupPID == -1) {
	sprintf(msg, "Unable to spawn LockSetCleanup thread: %s", 
		strerror(errno));
	throw std::runtime_error(msg);
      }     
    }
  }

  /**
   * Initiate a cleanup of LockSet.
   */ 
  static void 
  cleanup() 
    throw(std::runtime_error)
  {    
    if(sCleanupLock == NULL) 
      return;

    unlock(sCleanupLock);

    sSemSets     = NULL;
    sMasterLock  = NULL;
    sCleanupLock = NULL;  

    /* wait for cleanup thread to finish */ 
    if(sCleanupPID != -1) {
      char msg[1024];
      int status;
      if(waitpid(sCleanupPID, &status, __WCLONE) == -1) {
	switch(errno) {
	case ECHILD:
	  sCleanupPID = -1;
	  return;
	  
	default:	
	  sprintf(msg, "Unable to wait for LockSetCleanup[%d] to exit: %s", 
		  sCleanupPID, strerror(errno));
	  throw std::runtime_error(msg);
	}
      }

      if(WIFEXITED(status))
	return;
      else if(WIFSIGNALED(status)) {
	sprintf(msg, "LockSetCleanup[%d]: Exited due to signal (%d)!", 
		sCleanupPID, WTERMSIG(status));
	throw std::runtime_error(msg);
      }
      else if(WCOREDUMP(status)) {
	sprintf(msg, "LockSetCleanup[%d]: Core dumped!", sCleanupPID);
	throw std::runtime_error(msg);
      }
    }
  }


public:
  /*----------------------------------------------------------------------------------------*/
  /*   L O C K S                                                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Activate a new Lock.
   */ 
  static Lock*
  newLock() 
    throw(std::runtime_error)
  {
    if((sSemSets == NULL) || (sMasterLock == NULL))
      throw std::runtime_error("The LockSet has not been initialized!");

    Lock* lck = NULL;
    lock(sMasterLock);
    {
      {
	int wk;
	for(wk=0; wk<SEMMNI; wk++) {
	  if(sSemSets[wk] != NULL) {
	    lck = sSemSets[wk]->newLock();
	    if(lck != NULL) 
	      break;
	  }
	}
      }
    
      char msg[1024];
      if(lck == NULL) {
	int wk;
	for(wk=0; wk<SEMMNI; wk++) {
	  if(sSemSets[wk] == NULL) {
	    int setID = semget(IPC_PRIVATE, SEMMSL, 0666);
	    if(setID == -1) {
	      sprintf(msg, "Unable to create a new semaphore set: %s", 
		      strerror(errno));
	      throw std::runtime_error(msg);
	    }

	    sSemSets[wk] = new SemSet(wk, setID);
	    lck = sSemSets[wk]->newLock();
	    break;
	  }
	}
      }

      if(lck == NULL) 
	throw std::runtime_error("Unable to create any more semaphores!");
    }
    unlock(sMasterLock);

    // DEBUG
    printf("LockSet::newLock(): SetID = %d, SemaphoreID = %d\n", 
	   lck->pSetID, lck->pSemID);
    // DEBUG

    return lck;
  }

  /** 
   * Deactivate a Lock.
   * 
   * param lck
   *   The lock to deactivate.
   */ 
  static void 
  freeLock
  (
    Lock* lck
  ) 
  {
    assert(lck != NULL);

    // DEBUG
    printf("LockSet::freeLock(): SetIdx = %d, SetID = %d, SemaphoreID = %d\n", 
	   lck->pSetIdx, lck->pSetID, lck->pSemID);
    // DEBUG

    lock(sMasterLock);
    {
      assert(sSemSets[lck->pSetIdx] != NULL);
      sSemSets[lck->pSetIdx]->freeLock(lck);
      lck = NULL;
    }
    unlock(sMasterLock);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Aquire the lock.
   * 
   * param lck
   *   The lock to aquire.
   */
  static void
  lock
  (
   const Lock* lck
  )
    throw(std::runtime_error)
  {
    if(lck == NULL)
      throw std::runtime_error("Unable to lock NULL lock!");

    char msg[1024];
    struct sembuf sb = { lck->pSemID, -1, 0 }; 
    if(semop(lck->pSetID, &sb, 1) == -1) {
      sprintf(msg, "Unable to lock semaphore (%d) from set (%d): %s", 
	      lck->pSemID, lck->pSetID, strerror(errno));
      throw std::runtime_error(msg);
    }
  }

  /**
   * Release the lock.
   * 
   * param lck
   *   The lock to release.
   */
  static void
  unlock
  (
   const Lock* lck
  )
    throw(std::runtime_error)
  {
    if(lck == NULL)
      throw std::runtime_error("Unable to unlock NULL lock!");

    char msg[1024];
    struct sembuf sb = { lck->pSemID, 1, 0 }; 
    if(semop(lck->pSetID, &sb, 1) == -1) {
      sprintf(msg, "Unable to unlock semaphore (%d) from set (%d): %s", 
	      lck->pSemID, lck->pSetID, strerror(errno));
      throw std::runtime_error(msg);
    }
  }



private:
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The table of active semaphore sets.
   */ 
  static SemSet** sSemSets;

  /**
   * The lock used to protect the LockSet methods.
   */ 
  static Lock* sMasterLock;

  /**
   * The lock used to delay cleanup of the semaphore sets.
   */ 
  static Lock* sCleanupLock;

  /**
   * The process ID of the cleanup thread.
   */ 
  static int sCleanupPID;
};



/** 
 * C-Style wrapper function used to cleanup LockSet.
 * 
 * param obj
 *   The array of active SemSet objects.
 */ 
extern "C" 
inline int 
LockSetCleanup
(
 void *obj
)
{
  LockSet::SemSet** semSets = (LockSet::SemSet**) obj;

  // DEBUG
  printf("LockSetCleanup() spawned...\n");
  // DEBUG

  /* wait here for the cleanup lock to be unlocked */ 
  assert(semSets[0] != NULL);
  assert(semSets[0]->getLock(1) != NULL);
  LockSet::lock(semSets[0]->getLock(1));

  // DEBUG
  printf("Cleaning Semaphore Sets:\n");
  // DEBUG

  /* cleanup any active semaphore sets */ 
  {
    int wk;
    for(wk=0; wk<SEMMNI; wk++) {
      if(semSets[wk] != NULL) {
	
	// DEBUG
	printf("  Semaphore Set Cleaned: %d\n", semSets[wk]->getID());
	// DEBUG

	LockSet::semun arg;
	if(semctl(semSets[wk]->getID(), 0, IPC_RMID, arg) == -1) 
	  printf("WARNING: Unable to remove the semaphore set (%d): %s", 
		 semSets[wk]->getID(), strerror(errno));
	
	delete (semSets[wk]);
      }
    }
  }

  /* deallocate the semaphore sets table */ 
  delete[] semSets;
}


} // namespace Pipeline

#endif
