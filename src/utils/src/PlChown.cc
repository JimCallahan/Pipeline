// $Id: PlChown.cc,v 1.1 2009/07/11 10:54:21 jim Exp $

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

#ifdef HAVE_CSTRING_H
#  include <cstring>
#else 
#  ifdef HAVE_STRING_H
#    include <string.h>
#  endif
#endif

#ifdef HAVE_LIMITS_H
#  include <limits.h>
#endif

#include <PackageInfo.hh>

using namespace Pipeline;

/*------------------------------------------------------------------------------------------*/
/*   P L  C H O W N                                                                         */
/*                                                                                          */
/*     A utility program used internally by the Pipeline file manager to change the         */
/*     ownership of files newly moved into the repository to the "pipeline" admin user.     */
/*     This executable has "setuid" root permissions, but will exit immediately if run by   */
/*     any user except "pipeline".                                                          */
/*------------------------------------------------------------------------------------------*/

/* usage message */ 
void
usage()
{
  std::cerr << "usage: plchown target-dir\n"
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

  /* make sure that only the "pipeline" user can run this program! */ 
  {
    uid_t uid = getuid();
    if(uid != PackageInfo::sPipelineUID) {
      sprintf(msg, "This program can only be run by the (%s) user!", 
	      PackageInfo::sPipelineUser);
      FB::error(msg);	
    }
  }

  /* make sure that only the "pipeline" group can run this program! */ 
  {
    gid_t gid = getgid();
    if(gid != PackageInfo::sPipelineGID) {
      sprintf(msg, "This program can only be run by the (%s) group!", 
	      PackageInfo::sPipelineGroup);
      FB::error(msg);	
    }
  }

  /* validate args */ 
  char* targetDir = argv[1];
  {
    size_t rlen = strlen(PackageInfo::sRepoDir); 
    size_t tlen = strlen(targetDir);
    if((tlen < rlen) || strncmp(targetDir, PackageInfo::sRepoDir, rlen) != 0) {
      sprintf(msg, "The target directory (%s) was not a subdirectory of the repository (%s)!", 
	      targetDir, PackageInfo::sRepoDir);
      FB::error(msg);	
    }
  }

  /* build a new argument list */ 
  const char* cmd = "/bin/chown"; 
  char** argv2 = new char*[5];  
  {
    argv2[0] = new char[strlen(cmd) + 1];
    strcpy(argv2[0], cmd); 

    argv2[1] = new char[strlen(PackageInfo::sPipelineUser) + 
                        strlen(PackageInfo::sPipelineGroup) + 1];
    strcpy(argv2[1], PackageInfo::sPipelineUser); 
    strcat(argv2[1], ":"); 
    strcat(argv2[1], PackageInfo::sPipelineGroup); 

    const char* rec = "--recursive"; 
    argv2[2] = new char[strlen(rec) + 1];
    strcpy(argv2[2], rec); 

    argv2[3] = new char[strlen(targetDir)+1];
    argv2[3] = strcpy(argv2[3], targetDir); 
    
    argv2[4] = NULL;
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

    envp2 = new char*[cnt+1];
    {
      int wk = 0;
      char** p = envp;
      while((*p) != NULL) {
	size_t len = strlen(*p);

	/* To get around the dynamic linker stripping LD_LIBRARY_PATH (Linux) or
           DYLD_LIBRARY_PATH (Mac OS X) from the environment due to plrun(1) being 
           a setuid program.  Pipeline copies the dynamic library search path to the 
           PIPELINE_LD_LIBRARY_PATH temporary variable so that this code can restore 
           it before exec'ing the target program. */
	if((len > 24) && (strncmp(*p, "PIPELINE_LD_LIBRARY_PATH", 24) == 0)) {
	  envp2[wk] = new char[len - 8];
	  strcpy(envp2[wk], (*p)+9);  
        }
        else if((len > 26) && (strncmp(*p, "PIPELINE_DYLD_LIBRARY_PATH", 26) == 0)) {
          envp2[wk] = new char[len - 8];
	  strcpy(envp2[wk], (*p)+9);            
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

//   /* debugging */ 
//    FB::stageBegin("Original");
//    {
//      FB::stageBegin("Arguments");
//      {
//        char** p = argv;
//        while((*p) != NULL) {
//  	FB::stageMsg(*p);
//  	p++;
//        }
//      }
//      FB::stageEnd();

//      FB::stageBegin("Environment");
//      {
//        char** p = envp;
//        while((*p) != NULL) {
//  	FB::stageMsg(*p);
//  	p++;
//        }
//      }
//      FB::stageEnd();
//    }
//    FB::stageEnd();

//    FB::stageBegin("Modified"); 
//    {
//      FB::stageBegin("Command");
//      {
//        FB::stageMsg(cmd); 
//      }
//      FB::stageEnd();

//      FB::stageBegin("Arguments");
//      {
//        char** p = argv2;
//        while((*p) != NULL) {
//  	FB::stageMsg(*p);
//  	p++;
//        }
//      }
//      FB::stageEnd();

//      FB::stageBegin("Environment");
//      {
//        char** p = envp2;
//        while((*p) != NULL) {
//  	FB::stageMsg(*p);
//  	p++;
//        }
//      }
//      FB::stageEnd();
//    }
//    FB::stageEnd();
    
  /* execute the command */ 
  if(execve(cmd, argv2, envp2) == -1) {
    sprintf(msg, "unable to execute \"%s\": %s", cmd, strerror(errno));
    FB::error(msg);
  }
  
  /* execution should never get here! */ 
  assert(false);
  return EXIT_FAILURE;
}
