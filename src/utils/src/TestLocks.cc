// $Id: TestLocks.cc,v 1.1 2004/04/06 08:58:09 jim Exp $

#ifdef HAVE_CONFIG_H
#  include "config.h"
#endif

#ifdef HAVE_UNISTD_H
#  include <unistd.h>
#endif

#ifdef HAVE_LIST
#  include <list>
#endif

#include <PackageInfo.hh>
#include <Task.hh>
#include <LockSet.hh>

using namespace Pipeline;

/*------------------------------------------------------------------------------------------*/
/*                                                                                          */
/*                                                                                          */
/*                                                                                          */
/*------------------------------------------------------------------------------------------*/

class IncTask : public Task
{
public:
  IncTask
  (
   LockSet& lockSet,
   int lockID, 
   int id, 
   int delay,
   int* val, 
   int* hist
  ) :
    pLockSet(lockSet), 
    pLockID(lockID),
    pID(id),
    pDelay(delay), 
    pVal(val),
    pHist(hist)
  {
  }

  ~IncTask() 
  {
  }

  virtual int
  run()
  {
    usleep(pDelay);

    bool done = false;
    while(!done) {

      usleep(1000);

      pLockSet.lock(pLockID);
      
      pHist[pID]++;
      
      int i = *pVal;

      usleep(100);

      i++;
      if(i>1000)
	done = true;
      printf("[%d] val = %d\n", pPID, i);

      (*pVal) = i;

      pLockSet.unlock(pLockID);
    }

    printf("[%d] finished\n", pPID);
    

    return pID;
  }
  
private:
  LockSet&  pLockSet;
  int       pLockID;

  int    pID;
  int*   pVal;
  int    pDelay;
  int*   pHist;
};



int
main
(
 int argc, 
 char **argv, 
 char **envp
)
{
  srand48(time(NULL));


  int hist[SEMMSL];
  long delay[SEMMSL];
  {
    LockSet lockSet;
    int lockID = lockSet.initLock();
    if(lockID == -1) 
      FB::error("Unable to intialize the Lock!\n");

    int val = 0;
    memset(hist, 0, sizeof(hist));
    memset(delay, 0, sizeof(delay));
    
    std::list<IncTask*> tasks;
    int wk;
    for(wk=0; wk<249; wk++) {
      delay[wk] = lrand48() / 214748;

      IncTask* task = new IncTask(lockSet, lockID, wk, delay[wk], &val, hist);
      task->spawn();
      tasks.push_back(task);
    }

    std::list<IncTask*>::iterator iter;
    for(iter=tasks.begin(); iter != tasks.end(); iter++) {
      printf("Waiting on [%d]...\n", (*iter)->getPID());
      int code = (*iter)->wait();
      printf("  [%d] DONE -- Exit Code = %d\n", (*iter)->getPID(), code);
    }

    lockSet.releaseLock(lockID);
  }

  
  printf("Histogram:\n");
  int wk;
  for(wk=0; wk<249; wk++) 
    printf("[%d] = %d (%ld)\n", wk, hist[wk], delay[wk]);

  
  exit(EXIT_SUCCESS);
}
