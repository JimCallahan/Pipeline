// $Id: TestPlNotifyMonitor.cc,v 1.1 2004/04/09 17:55:12 jim Exp $

#ifdef HAVE_CONFIG_H
#  include "config.h"
#endif

#include <PackageInfo.hh>
#include <Network.hh>
#include <LockSet.hh>

using namespace Pipeline;

/*------------------------------------------------------------------------------------------*/
/*   P L   N O T I F Y                                                                      */
/*                                                                                          */
/*                                                                                          */
/*------------------------------------------------------------------------------------------*/


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
  /* initialize the LockSet table */ 
  LockSet::init();

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

  /* initialize the loggers */ 
  FB::init(std::cout);
  FB::setStageStats(true, 10);
  FB::setWarnings(true, 10);

  /* open a connection to plnotify(1) */ 
  int sd = Network::socket();
  Network::connect(sd, "24.193.206.192", PackageInfo::sNotifyMonitorPort);

  FB::threadMsg("Connection Opened.", 0);

  char msg[1024];
  char data[1024];
  while(true) {
    //sleep(5);

    int num = Network::read(sd, data, sizeof(data));
    if(num == -1) {
      FB::threadMsg("Connection Closed.", 0);
      LockSet::cleanup();
      exit(EXIT_SUCCESS);
    }
    else if(num < 1024) {
      FB::warn("Illegible message recieved!");
      break;
    }

    sprintf(msg, "Modified: %s", data);
    FB::threadMsg(msg, 1);
  }

  LockSet::cleanup();
  exit(EXIT_FAILURE);
}
