// $Id: CloneTest.cc,v 1.1 2004/04/03 18:42:04 jim Exp $

#ifdef HAVE_CONFIG_H
#  include "config.h"
#endif

#ifdef HAVE_ASSERT_H
#  include <assert.h>
#endif

#ifdef HAVE_CSTDLIB
#  include <cstdlib>
#else
#  ifdef HAVE_STDLIB_H
#    include <stdlib.h>
#  endif
#endif

#ifdef HAVE_CLIMITS
#  include <climits>
#else
#  ifdef HAVE_LIMITS_H
#    include <limits.h>
#  endif
#endif

#ifdef HAVE_UNISTD_H
#  include <unistd.h>
#endif

#ifdef HAVE_STRING_H
#  include <string.h>
#endif

#ifdef HAVE_ERRNO_H
#  include <errno.h>
#endif

#ifdef HAVE_SYS_TYPES_H
#  include <sys/types.h>
#endif

#ifdef HAVE_SYS_STAT_H
#  include <sys/stat.h>
#endif

#ifdef HAVE_SYS_PARAM_H
#  include <sys/param.h>
#endif

#ifdef HAVE_SYS_WAIT_H
#  include <sys/wait.h>
#endif

#ifdef HAVE_FCNTL_H
#  include <fcntl.h>
#endif

#ifdef HAVE_SIGNAL_H
#  include <signal.h>
#endif


#include <PackageInfo.hh>

using namespace Pipeline;

/*------------------------------------------------------------------------------------------*/
/*   C L O N E   T E S T                                                                    */
/*                                                                                          */
/*                                                                                          */
/*------------------------------------------------------------------------------------------*/


class Task
{
public:
  Task
  (
   const char* base,
   int id, 
   int start, 
   int num
  ) : pBase(strdup(base)), pID(id), pStart(start), pNum(num)
  {}

  ~Task() 
  {
    delete[] pBase;
  }

  int
  run()
  {
    printf("[%d]: Started\n", pID);
    
    {
      long nap = lrand48() / (2147483648UL / 1);
      printf("[%d]: Sleeping for %ld (secs)\n", pID, nap);
      sleep(nap);
    }    

    /* block handling of SIGRTMIN+4 */
    sigset_t signalset;
    {
      sigemptyset(&signalset);
      sigaddset(&signalset, SIGRTMIN+4);
      
      if(sigprocmask(SIG_BLOCK, &signalset, NULL) == -1) {
	printf("Unable to block directory change signal (SIGRTMIN+4): %s\n", strerror(errno));
	return 0;
      }
    }

    /* begin monitoring the directories */ 
    char* dirs[1024];
    {
      char dir[1024];
      int wk, idx;
      for(wk=0; wk<pNum; wk++) {
	sprintf(dir, "%s/%d", pBase, wk+pStart);
	
	/* get the file descriptor */ 
	int fd = open(dir, O_RDONLY);
	if(fd == -1) {
	  printf("Unable to open directory: %s", strerror(errno));
	  return 0;
	}
	dirs[fd] = strdup(dir);

	/* setup monitoring */ 
	if(fcntl(fd, F_SETSIG, SIGRTMIN+4) == -1) {
	  printf("Unable to set signal (SIGRTMIN+4): %s\n", strerror(errno));
	  return 0;
	}
	
	long args = DN_MODIFY | DN_CREATE | DN_DELETE | DN_RENAME | DN_ATTRIB;
	if(fcntl(fd, F_NOTIFY, args | DN_MULTISHOT) == -1) {
	  printf("Unable to set directory notification: %s\n", strerror(errno));
	  return 0;
	}
	
	//printf("[%d]: Monitoring %s (%d)\n", pID, dir, fd);
      }
    }


    /* wait for modification signals */
    {
      printf("[%d]: Watching...\n", pID);

      while(true) {
	sigset_t signalset;
	sigemptyset(&signalset);
	sigaddset(&signalset, SIGRTMIN+4);
	
	siginfo_t sinfo;
	if(sigwaitinfo(&signalset, &sinfo) == -1) {
	  sprintf("Bad signal: %s\n", strerror(errno));
	  return 0;   
	}
	
	printf("[%d]: Modified: %s (%d)\n", pID, dirs[sinfo.si_fd], sinfo.si_fd);
      }
    }

    return 1;
  }

protected:
  const char*  pBase;

  int  pID;
  int  pStart;
  int  pNum;
};


extern "C" 
int 
taskWrapper
(
 void *obj
) 
{
  Task* task = (Task*) obj;
  return (task->run());
}



int
main
(
 int argc, 
 char **argv, 
 char **envp
)
{
  int threads   = 3; 
  int stackSize = 65536;

  int* pids     = new int[threads];
  char** stacks = new char*[threads];

  srand48(time(NULL)); 

  /* close the STDIN and STDERR */ 
  close(0);
  close(2);

  int wk; 
  for(wk=0; wk<threads; wk++) {
    Task* task = new Task("/usr/tmp/clone", wk, wk*1023, 1023);

    stacks[wk] = new char[65536];
    pids[wk]   = clone(taskWrapper, (void*) (stacks[wk]+stackSize-1), CLONE_VM, (void*) task);
  }

  for(wk=0; wk<threads; wk++) {
    printf("Waiting[%d]: %d\n", wk, pids[wk]);

    int status;
    if(waitpid(pids[wk], &status, __WCLONE) == -1) 
      printf("ERROR: %s\n", strerror(errno));
    
    if(WIFEXITED(status)) 
      printf("[%d]: SUCCESS\n", wk);
    else 
      printf("[%d]: FAILURE(%d)\n", wk, WEXITSTATUS(status));
  }

  printf("ALL DONE!\n");

  delete[] pids;
}
