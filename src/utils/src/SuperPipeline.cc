// $Id: SuperPipeline.cc,v 1.1 2003/02/10 16:09:49 jim Exp $

#ifdef HAVE_CONFIG_H
#  include "config.h"
#endif

#ifdef HAVE_SYS_TYPES_H
#  include <sys/types.h>
#endif

#ifdef HAVE_UNISTD_H
#  include <unistd.h>
#endif

#ifdef HAVE_PWD_H
#  include <pwd.h>
#endif

#include <PackageInfo.hh>

using namespace Phoenix;
using namespace Phoenix::Core;

/*------------------------------------------------------------------------------------------*/
/*   S U P E R   P I P E L I N E                                                            */
/*                                                                                          */
/*     A wrapper binary for pipeline(1) with super-user privileges.   This binary is        */
/*     provided to allow web servers to operate as if they where specific users when        */
/*     using pipeline(1).  When properly configured and installed, this binary should not   */ 
/*     present any security risks, as it uses only absolute paths to the java(1) binary     */ 
/*     and pipeline's JAR file.                                                             */
/*------------------------------------------------------------------------------------------*/

/* usage message */ 
void
usage()
{
  std::cerr << "usage: super-pipeline user working-directory [pipeline options]\n"
	    << std::flush;
}


/* the program */   
int
main
(
 int argc, 
 char **argv
)
{
  /* parse command line args */ 
  FB::init(std::cout);
  FB::setWarnings(false);
  FB::setStageStats(false);

  if(argc < 3) {
    usage();
    exit(EXIT_FAILURE);
  }
  const char* user = argv[1];
  const char* cwd  = argv[2];
  
  /* lookup UID/GID for given username */ 
  uid_t uid;
  gid_t gid;  
  {
    struct passwd* pw = getpwnam(user);
    if(pw == NULL) {
      char msg[1024];
      sprintf(msg, "Unable to find an /etc/passwd entry for \"%s\"!", user);
      FB::error(msg);
    }
    uid = pw->pw_uid;
    gid = pw->pw_gid;
  }

  /* change effective user/group */ 
  if(setgid(gid) == -1) 
    FB::error("Unable to change Group-ID!");
  if(setuid(uid) == -1) 
    FB::error("Unable to change User-ID!");
  

  /* change working directory */ 
  if(chdir(cwd) == -1) {
    char msg[1024];
    sprintf(msg, "Unable to change working directory to \"%s\"!", cwd);
    FB::error(msg);
  }


  /* build pipeline(1) args */ 
  char** args = new char*[argc+3];

  /* the java interpreter */ 
  args[0] = strdup(Pipeline::PackageInfo::sJava);

  /* java runtime options */ 
  {
    char tmp[1024];
    sprintf(tmp, "-Djava.util.logging.config.file=%s/pipeline/share/logger.properties", 
	    Pipeline::PackageInfo::sBaseDir);
    args[1] = strdup(tmp);
    
    args[2] = strdup("-cp");
    
    sprintf(tmp, "%s/pipeline/share/pipeline.jar:%s", 
	    Pipeline::PackageInfo::sBaseDir, Pipeline::PackageInfo::sJDBC);
    args[3] = strdup(tmp);
    
    args[4] = strdup("pipeline/ClientApp");
  }
  
  /* copy the remaining args */ 
  int wk; 
  for(wk=3; wk<argc; wk++) 
    args[wk+2] = argv[wk];
  
  args[argc+2] = NULL;
  
  
  /* overlay the process */ 
  execv(Pipeline::PackageInfo::sJava, args);
  
  /* this should NEVER be reached! */ 
  FB::error("Unable to execute pipeline(1)!");
}
