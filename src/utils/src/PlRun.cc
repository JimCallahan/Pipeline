// $Id: PlRun.cc,v 1.7 2006/12/21 10:37:41 jim Exp $

#ifdef HAVE_CONFIG_H
#  include "config.h"
#endif

#ifdef HAVE_SYS_TYPES_H
#  include <sys/types.h>
#endif

#ifdef HAVE_SYS_STAT_H
#  include <sys/stat.h>
#endif

#ifdef HAVE_UNISTD_H
#  include <unistd.h>
#endif

#ifdef HAVE_ERRNO_H
#  include <errno.h>
#endif

#include <PackageInfo.hh>

using namespace Pipeline;

/*------------------------------------------------------------------------------------------*/
/*   P L R U N                                                                              */
/*                                                                                          */
/*     A utility program used internally by the Pipeline queue to allow the "pipeline"      */
/*     user to run job processes as other users.  This executable has "setuid" root         */
/*     permissions, but will exit immediately if run by any user except "pipeline".         */
/*------------------------------------------------------------------------------------------*/

/* usage message */ 
void
usage()
{
  std::cerr << "usage: plrun username uid command [args...]\n"
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

  if(argc<4) {
    usage();
    exit(EXIT_FAILURE);    
  }

  /* make sure that only the "pipeline" user can run this program! */ 
  {
    uid_t uid  = getuid();
    if(uid != PackageInfo::sPipelineUID) {
      sprintf(msg, "This program can only be run by the (%s) user!", 
	      PackageInfo::sPipelineUser);
      FB::error(msg);	
    }
      
    uid_t gid  = getgid();
    if(gid != PackageInfo::sPipelineGID) {
      sprintf(msg, "This program can only be run by the (%s) group!", 
	      PackageInfo::sPipelineGroup);
      FB::error(msg);	
    }
  }

  /* unpack arguments */ 
  char* username = argv[1];
  char* uidStr   = argv[2];
  char* cmd      = argv[3];

  /* change user */ 
  {
    if(uidStr == NULL) 
      FB::error("somehow the UID was NULL!");
      
    long id = strtol(uidStr, (char**) NULL, 10);
    if(id < 0)       
      FB::error("the UID must be positive!");
    else if(id == 0) 
      FB::error("the UID cannot be 0 (root)!");
    else if(id > INT_MAX) 
      FB::error("the UID was outside the valid range!");
  
    uid_t uid = (uid_t) id;
    if(setuid(uid) != 0) {
      switch(errno) {
      case EPERM:
	sprintf(msg, "unable to substitute to user with UID (%s)!", uidStr);
	FB::error(msg);
	
      default:
	FB::error("internal error!");
      }
    }
  }
  
  /* build a new environment: (to get around ld.so ignoring LD_LIBRARY_PATH) */ 
  char** envp2 = NULL;
  {
    int cnt = 0;
    {
      char** p = envp;
      while((*p) != NULL) {
	cnt++;
	p++;
      }
    }

    char* homedir = new char[strlen(PackageInfo::sNativeHomeDir) + 7];
    strcpy(homedir, "HOME="); 
    strcat(homedir, PackageInfo::sNativeHomeDir);
    strcat(homedir, "/");

    char* phomedir = new char[strlen(homedir) + strlen("pipeline") + 1];
    strcpy(phomedir, homedir);
    strcat(phomedir, "pipeline");

    envp2 = new char*[cnt+1];
    {
      int wk = 0;
      char** p = envp;
      while((*p) != NULL) {
	size_t len = strlen(*p);
	if((len > 24) && (strncmp(*p, "PIPELINE_LD_LIBRARY_PATH", 24) == 0)) {
	  envp2[wk] = strdup((*p)+9);
	}
	else if(strcmp(*p, "USER=pipeline") == 0) {
	  envp2[wk] = new char[strlen(username) + 6];
	  strcpy(envp2[wk], "USER=");
	  strcat(envp2[wk], username);
	}
	else if(strcmp(*p, phomedir) == 0) {
	  envp2[wk] = new char[strlen(homedir) + strlen(username) + 1];
	  strcpy(envp2[wk], homedir);
	  strcat(envp2[wk], username);
	}
	else {
	  envp2[wk] = strdup(*p);
	}

	wk++;
	p++;
      }

      envp2[cnt] = NULL;
    }
  }
  assert(envp2);

  /* execute the command */ 
  if(execve(cmd, argv+3, envp2) == -1) {
    sprintf(msg, "unable to execute \"%s\": %s", cmd, strerror(errno));
    FB::error(msg);
  }
  
  /* execution should never get here! */ 
  assert(false);
  return EXIT_FAILURE;
}
