// $Id: TestPlNotifyShutdown.cc,v 1.1 2004/04/09 17:55:12 jim Exp $

#ifdef HAVE_CONFIG_H
#  include "config.h"
#endif

#include <PackageInfo.hh>
#include <Network.hh>
#include <LockSet.hh>

using namespace Pipeline;

/*------------------------------------------------------------------------------------------*/
/*   P L   N O T I F Y   C O N T R O L                                                      */
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
  Network::connect(sd, "24.193.206.192", PackageInfo::sNotifyControlPort);

  {
    char data[1032];
    memset((void*) data, 0, sizeof(data));
    strncpy(data, "SHUTDOWN", 8);
    printf("DATA = %s\n", data);    
    Network::write(sd, data, sizeof(data));
  }

  LockSet::cleanup();
  exit(EXIT_SUCCESS);
}
