// $Id: PlNotify.cc,v 1.3 2004/04/06 08:58:52 jim Exp $

#ifdef HAVE_CONFIG_H
#  include "config.h"
#endif

#include <PackageInfo.hh>
#include <HtmlHelp.hh>
#include <NotifyMgr.hh>
#include <NotifyControlServer.hh>
#include <Network.hh>

using namespace Pipeline;

/*------------------------------------------------------------------------------------------*/
/*   P L   N O T I F Y                                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The usage message.
*/ 
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
	    << "  [--prod=DIR][--control-port=NUM][--monitor-port=NUM]\n" 
	    << "  [--stats=LEVEL][--warnings=LEVEL]\n" 
	    << "\n"
	    << "Use \"plnotify --html-help\" to browse the full documentation.\n" 
	    << std::flush;
}


/**
 * Makes sure that the exit functions are called for: SIGHUP, SIGINT or SIGTERM
 * 
 * This is mainly to make sure that the semaphores used by LockSet are properly 
 * released on exit of plnotify(1).
 */ 
void
handleSignal
(
 int signal
) 
{
  exit(EXIT_FAILURE);
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
  /* parse command line args */ 
  char msg[2048];
  const char* prodDir = PackageInfo::sProdDir;
  int controlPort = PackageInfo::sNotifyControlPort;
  int monitorPort = PackageInfo::sNotifyMonitorPort;
  int statsLevel = -1;
  int warningsLevel = -1;
  {
    int i = 1;
    for(i=1; i<argc; i++) {
      if(strcmp(argv[i], "--help") == 0) {
 	usage();
 	exit(EXIT_SUCCESS);
      }
      else if(strcmp(argv[i], "--html-help") == 0) {
 	HtmlHelp::launch("plnotify");
      }
      else if(strcmp(argv[i], "--version") == 0) {
 	std::cerr << PackageInfo::sVersion << "\n";
 	exit(EXIT_SUCCESS);
      }
      else if(strcmp(argv[i], "--release-date") == 0) {
 	std::cerr << PackageInfo::sRelease << "\n";
 	exit(EXIT_SUCCESS);
       }
      else if(strcmp(argv[i], "--copyright") == 0) {
 	std::cerr << PackageInfo::sCopyright << "\n";
 	exit(EXIT_SUCCESS);
      }
      else if(strcmp(argv[i], "--license") == 0) {
 	std::cerr << PackageInfo::sLicense << "\n";
 	exit(EXIT_SUCCESS);
      }
      else if(strncmp(argv[i], "--prod=", 7) == 0) {
 	prodDir = argv[i]+7; 
      }
      else if(strncmp(argv[i], "--control-port=", 15) == 0) {
 	controlPort = atol(argv[i]+15);
      }
      else if(strncmp(argv[i], "--monitor-port=", 15) == 0) {
 	monitorPort = atol(argv[i]+15);
      }
      else if(strncmp(argv[i], "--stats=", 8) == 0) {
	statsLevel = atoi(argv[i]+8);
      }
      else if(strncmp(argv[i], "--warnings=", 11) == 0) {
	warningsLevel = atoi(argv[i]+11);
      }
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


  /* register signal handlers for: SIGHUP, SIGINT and SIGTERM */ 
  {
    struct sigaction sa;
    sa.sa_handler = handleSignal;

    int signals[3] = { SIGHUP, SIGINT, SIGTERM };
    int wk;
    for(wk=0; wk<3; wk++) {
      if(sigaction(signals[wk], &sa, NULL) == -1) {
	sprintf(msg, "Unable to register the signal handler: %s", strerror(errno));
	FB::error(msg);
      }
    }
  }
  

  /* startup the notify, control and monitory threads */ 
  FB::stageBegin("Working...", 1);
  {
    NotifyMgr mgr(prodDir);

//     NotifyMonitorServer monitor(mgr, monitorPort);
//     monitor.spawn();

    NotifyControlServer control(mgr, controlPort);
    control.spawn();
 
    mgr.wait();
  }
  FB::stageEnd(1);

  return EXIT_SUCCESS;
}



