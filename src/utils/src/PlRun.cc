// $Id: PlRun.cc,v 1.2 2003/12/17 04:49:12 jim Exp $

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
  std::cerr << "usage: plrun uid command [args...]\n"
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

  if(argc<3) {
    usage();
    exit(EXIT_FAILURE);    
  }

  /* make sure that only the "pipeline" user can run this program! */ 
  {
    uid_t uid  = getuid();
    uid_t gid  = getgid();
    if((uid != PackageInfo::sPipelineUID) || (gid != PackageInfo::sPipelineGID)) 
      FB::error("plrun can only be run by the \"pipeline.pipeline\" user!");	
  }

  /* change user */ 
  {
    if(argv[1] == NULL) 
      FB::error("somehow the UID was NULL!");
      
    long id = strtol(argv[1], (char**) NULL, 10);
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
	sprintf(msg, "unable to substitute user (%s)!", uid);
	FB::error(msg);
	
      default:
	FB::error("internal error!");
      }
    }
  }
  
  /* build a new environment (to get around ld.so ignoring LD_LIBRARY_PATH) */ 
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

    envp2 = new char*[cnt+1];
    {
      int wk = 0;
      char** p = envp;
      while((*p) != NULL) {
	if((strlen(*p) > 24) && (strncmp(*p, "PIPELINE_LD_LIBRARY_PATH", 24) == 0)) 
	  envp2[wk] = strdup((*p)+9);
	else 
	  envp2[wk] = strdup(*p);
	wk++;
	p++;
      }
    }
  }
  assert(envp2);

  /* execute the command */ 
  if(execve(argv[2], argv+2, envp2) == -1) {
    sprintf(msg, "unable to execute \"%s\": %s", argv[2], strerror(errno));
    FB::error(msg);
  }
  
  /* execution should never get here! */ 
  assert(false);
  return EXIT_FAILURE;
}
