// $Id: TestLocks.cc,v 1.2 2004/04/09 17:55:12 jim Exp $

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
#include <FB.hh>

using namespace Pipeline;

/*------------------------------------------------------------------------------------------*/
/*   T E S T   L O C K S                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A test task.
 */ 
class IncTask : public Task
{
public:
  IncTask
  (
   LockSet::Lock* lock,
   int id, 
   int delay,
   int* val, 
   int* hist
  ) :
    pLock(lock), 
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

    char msg[1024];
    bool done = false;
    while(!done) {

      usleep(1000);

      int i;
      LockSet::lock(pLock);
      {
	pHist[pID]++;
	i = *pVal;

	if(i>100)
	  done = true;

	usleep(100);

	(*pVal) = i+1;
      }
      LockSet::unlock(pLock);
      
      sprintf(msg, "val = %d", i);
      FB::threadMsg(msg, 2, "IncTask", pPID);
    }

    return pID;
  }
  
private:
  LockSet::Lock*  pLock;

  int    pID;
  int*   pVal;
  int    pDelay;
  int*   pHist;
};


/**
 * Makes sure that the semaphore sets are cleaned up for: SIGHUP, SIGINT or SIGTERM
 */ 
void
handleSignal
(
 int signal
) 
{
  printf("Caught Signal: %s\n", strsignal(signal));
  LockSet::cleanup();
}

/**
 * The top-level function.
 */ 
int
main
(
 int argc, 
 char **argv, 
 char **envp
)
{
  /* initialize output and locks */ 
  FB::init(std::cout);
  FB::setStageStats(true, 10);
  FB::setWarnings(true, 10);
  
  try {
    char msg[1024];

    /* register signal handlers for: SIGHUP, SIGINT and SIGTERM */ 
    {
      struct sigaction sa;
      sa.sa_handler = handleSignal;
      
      int signals[3] = { SIGHUP, SIGINT, SIGTERM };
      int wk;
      for(wk=0; wk<3; wk++) {
	if(sigaction(signals[wk], &sa, NULL) == -1) {
	  sprintf(msg, "Unable to register the signal handler: %s", strerror(errno));
	  throw std::runtime_error(msg);
	}
      }
    }
    
    int numThreads = 10;
    int hist[SEMMSL];
    long delay[SEMMSL];
    {
       throw std::runtime_error("hi there!"); 

      LockSet::Lock* lock = LockSet::newLock();
      
      srand48(time(NULL));
      int val = 0;
      memset(hist, 0, sizeof(hist));
      memset(delay, 0, sizeof(delay));
      
      std::list<IncTask*> tasks;
      int wk;
      for(wk=0; wk<numThreads; wk++) {
	delay[wk] = lrand48() / 214748;
	
	IncTask* task = new IncTask(lock, wk, delay[wk], &val, hist);
	task->spawn();
	tasks.push_back(task);
      }
      
      std::list<IncTask*>::iterator iter;
      for(iter=tasks.begin(); iter != tasks.end(); iter++) {
	sprintf(msg, "Waiting on [%d]...", (*iter)->getPID());
	FB::threadMsg(msg, 1);
	
	int code = (*iter)->wait();
	sprintf(msg, "[%d] DONE -- Exit Code = %d", (*iter)->getPID(), code);
	FB::threadMsg(msg, 2);
      }
      
      LockSet::freeLock(lock);
    }
    
    sprintf(msg, "Histogram:");
    FB::threadMsg(msg, 1);
    int wk;
    for(wk=0; wk<numThreads; wk++) {
      sprintf(msg, "[%d] = %d (%ld)", wk, hist[wk], delay[wk]);
      FB::threadMsg(msg, 2);
    }
  }
   catch(const std::runtime_error& ex) {
     printf("RUNTIME ERROR: %s\n", ex.what());
     sleep(5);
     LockSet::cleanup();
     return EXIT_FAILURE;
   }
  catch(std::exception& ex) {
    printf("UNCAUGHT EXCEPTION: %s\n", ex.what());
    //     LockSet::cleanup();
    return EXIT_SUCCESS;
  }
  
  printf("NOTHING CAUGHT!\n");
  

//   LockSet::cleanup();
  return EXIT_SUCCESS;
}
