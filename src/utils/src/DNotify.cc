// $Id: DNotify.cc,v 1.1 2004/01/29 01:43:55 jim Exp $

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

#ifdef HAVE_FCNTL_H
#  include <fcntl.h>
#endif

#ifdef HAVE_SIGNAL_H
#  include <signal.h>
#endif


#include <PackageInfo.hh>

using namespace Pipeline;

/*------------------------------------------------------------------------------------------*/
/*   D N O T I F Y                                                                          */
/*                                                                                          */
/*                                                                                          */
/*------------------------------------------------------------------------------------------*/

class WatchedDir
{
public:
  WatchedDir() : 
    pDir(NULL), pFileDesc(-1)
  {}

  bool
  init
  (
   const char* dir
  ) 
  {
    assert(dir != NULL);
    struct stat buf;
    if((stat(dir, &buf) == -1) || (!S_ISDIR(buf.st_mode)))
      return false;

    int fd = open(dir, O_RDONLY);
    if(fd == -1) 
      return false;

    if(fcntl(fd, F_SETSIG, SIGRTMIN+4) == -1)
      return false;

    long args = DN_ACCESS | DN_MODIFY | DN_CREATE | DN_DELETE | DN_RENAME;
    if(fcntl(fd, F_NOTIFY, args | DN_MULTISHOT) < 0)
      return false;

    pDir      = strdup(dir);
    pFileDesc = fd;

    return true;
  }

  
  const char* 
  getDirectoryName() const 
  {
    return pDir;
  }

  int 
  getFileDescriptor() const 
  {
    return pFileDesc;
  }

protected:
  char* pDir;
  int   pFileDesc;
};


/* usage message */ 
void
usage()
{
  std::cerr << "usage: dnotify dir1 [dir2 ...]\n" 
	    << std::flush;
}


int
main
(
 int argc, 
 char **argv, 
 char **envp
)
{
  /* parse command line args */ 
  char msg[1024];
  FB::init(std::cout);
  FB::setWarnings(false);
  FB::setStageStats(false);
  
  if(argc<2) {
    usage();
    exit(EXIT_FAILURE);    
  }
  
  int numDirs = argc-1;

  /* block handling of SIGRTMIN+4 */
  sigset_t signalset;
  {
    sigemptyset(&signalset);
    sigaddset(&signalset, SIGRTMIN+4);

    if(sigprocmask(SIG_BLOCK, &signalset, NULL) == -1) {
      sprintf(msg, "Unable to block SIGRTMIN+4: %s", strerror(errno));
      FB::error(msg);
    }
  }

  /* build the table of watched directories */ 
  WatchedDir* watched = new WatchedDir[numDirs];
  {
    int wk;
    for(wk=0; wk<numDirs; wk++) {
      char dir[MAXPATHLEN];
      if(realpath(argv[wk+1], dir) == NULL) {
	sprintf(msg, "Bad input path: %s", argv[wk+1]);
	FB::error(msg);
      }
  
      if(!watched[wk].init(dir)) {
	sprintf(msg, "Unable to monitor: %s", dir);
	FB::error(msg);
      }
    }
  }

  /* listen for change signals */ 
  while(true) {
    siginfo_t sinfo;
    int signal = sigwaitinfo(&signalset, &sinfo);
    if(signal == -1) {
      sprintf(msg, "Bad signal: %s", strerror(errno));
      FB::error(msg);      
    }
    else if(signal == (SIGRTMIN+4)) {
      int wk;
      for(wk=0; wk<numDirs; wk++) {
	int fd = sinfo.si_fd;
	if(watched[wk].getFileDescriptor() == fd) {
	  std::cout << "Directory Changed: " << watched[wk].getDirectoryName() << "\n";
	}
      }
    }
  }
}
