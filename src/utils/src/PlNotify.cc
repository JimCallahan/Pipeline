// $Id: PlNotify.cc,v 1.1 2004/04/05 05:50:07 jim Exp $

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

#ifdef HAVE_LIST
#  include <list>
#endif

#include <PackageInfo.hh>
#include <HtmlHelp.hh>
#include <NotifyMgr.hh>

using namespace Pipeline;

/*------------------------------------------------------------------------------------------*/
/*   P L   N O T I F Y                                                                      */
/*                                                                                          */
/*                                                                                          */
/*------------------------------------------------------------------------------------------*/

/* usage message */ 
void
usage()
{
  std::cerr << "USAGE:\n" 
	    << "  plnotify [options]\n" 
	    << "\n" 
	    << "  plnotify --help\n"
	    << "  plnotify --html-help\n"
	    << "  plnotify --version\n"
	    << "  plnotify --release-date\n"
	    << "  plnotify --copyright\n" 
	    << "  plnotify --license\n" 
	    << "\n" 
	    << "OPTIONS:\n" 
	    << "  [--control-port=NUM][--monitor-port=NUM]\n" 
	    << "  [--stats=LEVEL][--warnings=LEVEL]\n" 
	    << "\n"
	    << "Use \"plnotify --html-help\" to browse the full documentation.\n" 
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
  char msg[2048];
  int controlPort = PackageInfo::sNotifyControlPort;
  int monitorPort = PackageInfo::sNotifyMonitorPort;
  int statsLevel = -1;
  int warningsLevel = -1;
  switch(argc) {
  case 1:
    break;

  case 2:
    if(strcmp(argv[1], "--help") == 0) {
      usage();
      exit(EXIT_SUCCESS);
    }
    else if(strcmp(argv[1], "--html-help") == 0) {
      HtmlHelp::launch("pls");
    }
    else if(strcmp(argv[1], "--version") == 0) {
      std::cerr << PackageInfo::sVersion << "\n";
      exit(EXIT_SUCCESS);
    }
    else if(strcmp(argv[1], "--release-date") == 0) {
      std::cerr << PackageInfo::sRelease << "\n";
      exit(EXIT_SUCCESS);
    }
    else if(strcmp(argv[1], "--copyright") == 0) {
      std::cerr << PackageInfo::sCopyright << "\n";
      exit(EXIT_SUCCESS);
    }
    else if(strcmp(argv[1], "--license") == 0) {
      std::cerr << PackageInfo::sLicense << "\n";
      exit(EXIT_SUCCESS);
    }
  }

  {
    int i = 1;
    for(i=1; i<argc; i++) {
      if(strncmp(argv[i], "--control-port=", 15) == 0) 
	controlPort = atol(argv[i]+15);
      if(strncmp(argv[i], "--monitor-port=", 15) == 0) 
	monitorPort = atol(argv[i]+15);
      else if(strncmp(argv[i], "--stats=", 8) == 0) 
	statsLevel = atoi(argv[i]+8);
      else if(strncmp(argv[i], "--warnings=", 11) == 0) 
	warningsLevel = atoi(argv[i]+11);
      else if(strncmp(argv[i], "--", 2) == 0) {
	sprintf(msg, "Illegal option: %s", argv[i]);
	FB::error(msg);
      }
      else {
	sprintf(msg, "Illegal argument: %s", argv[i]);
	FB::error(msg);
      }
    }
  }


  /* initialize the loggers */ 
  {
    FB::init(std::cout);
    
    if(statsLevel > 0) 
      FB::setStageStats(true, statsLevel);
    else 
      FB::setStageStats(false);
    
    if(warningsLevel > 0) 
      FB::setWarnings(true, warningsLevel);
    else
      FB::setWarnings(false);
  }




  FB::stageBegin("Working...", 1);
  {
    NotifyMgr mgr("/usr/tmp");

    /* monitor some directories */ 
    int wk; 
    for(wk=0; wk<3000; wk++) {
      char dir[1024];
      sprintf(dir, "clone/%d", wk);
      mgr.addDir(dir);
    }

    /* unmonitor some directories */ 
    for(wk=0; wk<1500; wk+=2) {
      char dir[1024];
      sprintf(dir, "clone/%d", wk);
      mgr.removeDir(dir);
    }

    sleep(15);

    /* monitor some more directories */ 
    for(wk=0; wk<200; wk++) {
      char dir[1024];
      sprintf(dir, "clone/%d", wk);
      mgr.addDir(dir);
    }

    sleep(60);

    mgr.shutdown();
    mgr.wait();
  }
  FB::stageEnd(1);



  return EXIT_SUCCESS;
}
