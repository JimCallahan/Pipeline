// $Id: PlNotify.cc,v 1.7 2004/04/11 16:28:42 jim Exp $

#ifdef HAVE_CONFIG_H
#  include "config.h"
#endif

#ifdef HAVE_SYS_TYPES_H
#  include <sys/types.h>
#endif

#ifdef HAVE_SYS_STAT_H
#  include <sys/stat.h>
#endif

#ifdef HAVE_SYS_RESOURCE_H
#  include <sys/resource.h>
#endif

#ifdef HAVE_UNISTD_H
#  include <unistd.h>
#endif

#ifdef HAVE_CLIMITS
#  include <climits>
#else
#  ifdef HAVE_LIMITS_H
#    include <limits.h>
#  endif
#endif

#ifdef HAVE_SYS_TYPES_H
#  include <sys/types.h>
#endif

#ifdef HAVE_ERRNO_H
#  include <errno.h>
#endif

#include <PackageInfo.hh>

using namespace Pipeline;

/*------------------------------------------------------------------------------------------*/
/*   P L N O T I F Y                                                                        */
/*------------------------------------------------------------------------------------------*/

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

  /* make sure that only the "pipeline" user can run this program! */ 
  {
    uid_t uid  = getuid();
    uid_t gid  = getgid();
    if(gid != PackageInfo::sPipelineGID) 
      FB::error("This program can only be run by the \"pipeline\" group!");	
  }

  /* raise the maximum number of open files */ 
  struct rlimit rlim;
  rlim.rlim_cur = 32768;
  rlim.rlim_max = 32768;
  if(setrlimit(RLIMIT_NOFILE, &rlim) == -1) {
    sprintf(msg, "Unable to raise the monitored directory limit up to (%d): %s", 
	    rlim.rlim_max, strerror(errno)); 
    FB::error(msg);
  }

  /* change user */ 
  {
    uid_t uid = (uid_t) PackageInfo::sPipelineUID;
    if(setuid(uid) != 0) {
      switch(errno) {
      case EPERM:
	sprintf(msg, "Unable to substitute user (%s)!", uid);
	FB::error(msg);
	
      default:
	FB::error("INTERNAL ERROR!");
      }
    }
  }

  /* command */ 
  char cmd[1042];
  sprintf(cmd, "%s/bin/java", PackageInfo::sJavaHome);

  /* build command-line arguments */ 
  int argc2 = 0;
  char** argv2 = NULL;
  {
    int jcnt = 1;
    {
      const char* p = PackageInfo::sJavaRuntimeFlags;
      while((*p) != '\0') {
	if((*p) == ' ') 
	  jcnt++;
	p++;
      }
    }
    
    argc2 = argc+jcnt+5;
    argv2 = new char*[argc2+1];
    argv2[0] = cmd;
    
    char props[1024];
    sprintf(props, "-Djava.util.logging.config.file=%s/share/logger.properties", 
	    PackageInfo::sInstDir);
    argv2[1] = props;

    int wk = 2;
    {
      const char* p = PackageInfo::sJavaRuntimeFlags;
      const char* q = PackageInfo::sJavaRuntimeFlags;
      while(true) {
	if(((*q) == ' ') || ((*q) == '\0')) {
	  int size = q - p + 1;
	  argv2[wk] = new char[size+1];
	  strncpy(argv2[wk], p, size-1);
	  argv2[wk][size] = '\0';
	  wk++;

	  if((*q) == '\0')
	    break;

	  q++;	  
	  p = q;
	}

	q++;
      }
    }

    argv2[wk] = "-cp";
    wk++;

    char jar[1024];
    sprintf(jar, "%s/lib/api.jar", PackageInfo::sInstDir);
    argv2[wk] = jar;
    wk++;

    argv2[wk] = "us/temerity/pipeline/bootstrap/Main";
    wk++;

    argv2[wk] = "us.temerity.pipeline.core.NotifyApp";
    wk++;

    int i;
    for(i=1; i<argc; i++, wk++) 
      argv2[wk] = argv[i];

    argv2[argc2] = NULL;
  }

  
  /* execute the command */ 
  if(execv(cmd, argv2) == -1) {
    sprintf(msg, "Unable to execute (%s): %s", cmd, strerror(errno));
    FB::error(msg);
  }

  /* execution should never get here! */ 
  assert(false);
  return EXIT_FAILURE;
}
