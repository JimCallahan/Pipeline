// $Id: TestPlNotify.cc,v 1.2 2004/04/06 15:42:57 jim Exp $

#ifdef HAVE_CONFIG_H
#  include "config.h"
#endif

#include <PackageInfo.hh>
#include <Network.hh>

using namespace Pipeline;

/*------------------------------------------------------------------------------------------*/
/*   P L   N O T I F Y                                                                      */
/*                                                                                          */
/*                                                                                          */
/*------------------------------------------------------------------------------------------*/

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



int
main
(
 int argc, 
 char **argv, 
 char **envp
)
{
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

  sleep(60);

  //closeConnect(sd);
  shutdown(sd);
}
