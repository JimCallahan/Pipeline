// $Id: TestPlNotifyControl.cc,v 1.1 2004/04/09 17:55:12 jim Exp $

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


void 
addDir
(
 int sd, 
 const char* dir
) 
{
  assert(strlen(dir) < 1024);

  char data[1032];
  memset((void*) data, 0, sizeof(data));

  strncpy(data, "ADD_____", 8);
  strcpy(data+8, dir);

  printf("DATA = %s\n", data);

  Network::write(sd, data, sizeof(data));
}


void 
removeDir
(
 int sd, 
 const char* dir
) 
{
  assert(strlen(dir) < 1024);

  char data[1032];
  memset((void*) data, 0, sizeof(data));

  strncpy(data, "REMOVE__", 8);
  strcpy(data+8, dir);

  printf("DATA = %s\n", data);

  Network::write(sd, data, sizeof(data));
}


void 
shutdown
(
 int sd
) 
{
  char data[1032];
  memset((void*) data, 0, sizeof(data));

  strncpy(data, "SHUTDOWN", 8);

  printf("DATA = %s\n", data);

  Network::write(sd, data, sizeof(data));
}


void 
closeConnect
(
 int sd
) 
{
  char data[1032];
  memset((void*) data, 0, sizeof(data));

  strncpy(data, "CLOSE___", 8);

  printf("DATA = %s\n", data);

  Network::write(sd, data, sizeof(data));
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

  /* monitor some directories */ 
  int wk; 
  for(wk=0; wk<30; wk++) {
    char dir[1024];
    sprintf(dir, "clone/%d", wk);
    addDir(sd, dir);
  }

//   sleep(15);

//   /* unmonitor some directories */ 
//   for(wk=0; wk<1500; wk+=2) {
//     char dir[1024];
//     sprintf(dir, "clone/%d", wk);
//     removeDir(sd, dir);
//   }
  
//   sleep(15);

//   /* monitor some more directories */ 
//   for(wk=0; wk<200; wk++) {
//     char dir[1024];
//     sprintf(dir, "clone/%d", wk);
//     addDir(sd, dir);
//   }

  sleep(30);

  closeConnect(sd);
  //  shutdown(sd);

  LockSet::cleanup();
  exit(EXIT_SUCCESS);
}
