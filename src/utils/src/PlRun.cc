// $Id: PlRun.cc,v 1.8 2007/01/17 23:17:50 jim Exp $

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
    uid_t uid = getuid();
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

  /* build a new argument list */ 
  char** argv2 = new char*[argc-2];
  {
    argv2[0] = NULL;
    {
      int wk = 0;
      size_t len = strlen(cmd);
      char* p = cmd+(len-1);
      while(wk < len) {
	if(*p == '/') {
	  argv2[0] = new char[wk + 1];
	  strcpy(argv2[0], p+1);
	  break;
	}

	wk++;
	p--;
      }

      if(argv2[0] == NULL)
	FB::error("the command argument was not absolute!");
    }

    int wk = 1;
    char** p = argv+4;
    while((*p) != NULL) {
      size_t len = strlen(*p);
      argv2[wk] = new char[len + 1];
      strcpy(argv2[wk], *p);

      wk++;
      p++;
    }

    argv2[wk] = NULL;
  }

  /* build a new environment */ 
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

	/* to get around ld.so ignoring LD_LIBRARY_PATH) */
	if((len > 24) && (strncmp(*p, "PIPELINE_LD_LIBRARY_PATH", 24) == 0)) {
	  envp2[wk] = new char[len - 8];
	  strcpy(envp2[wk], (*p)+9);  
	}
	
	/* replace with substituted username and home directory */ 
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

	/* copy everything else... */ 
	else {
	  envp2[wk] = new char[len + 1];
	  strcpy(envp2[wk], *p);
	}

	wk++;
	p++;
      }

      envp2[wk] = NULL;
    }
  }
  assert(envp2);

  /* debugging */ 
//   FB::stageBegin("Original");
//   {
//     FB::stageBegin("Arguments");
//     {
//       char** p = argv;
//       while((*p) != NULL) {
// 	FB::stageMsg(*p);
// 	p++;
//       }
//     }
//     FB::stageEnd();

//     FB::stageBegin("Environment");
//     {
//       char** p = envp;
//       while((*p) != NULL) {
// 	FB::stageMsg(*p);
// 	p++;
//       }
//     }
//     FB::stageEnd();
//   }
//   FB::stageEnd();

//   FB::stageBegin("Modified"); 
//   {
//     FB::stageBegin("Command");
//     {
//       FB::stageMsg(cmd);
//     }
//     FB::stageEnd();
  
//     FB::stageBegin("Arguments");
//     {
//       char** p = argv2;
//       while((*p) != NULL) {
// 	FB::stageMsg(*p);
// 	p++;
//       }
//     }
//     FB::stageEnd();

//     FB::stageBegin("Environment");
//     {
//       char** p = envp2;
//       while((*p) != NULL) {
// 	FB::stageMsg(*p);
// 	p++;
//       }
//     }
//     FB::stageEnd();
//   }
//   FB::stageEnd();
    
  /* execute the command */ 
  if(execve(cmd, argv2, envp2) == -1) {
    sprintf(msg, "unable to execute \"%s\": %s", cmd, strerror(errno));
    FB::error(msg);
  }
  
  /* execution should never get here! */ 
  assert(false);
  return EXIT_FAILURE;
}
