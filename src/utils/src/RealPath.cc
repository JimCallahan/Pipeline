/* $Id: RealPath.cc,v 1.3 2004/03/21 01:20:26 jim Exp $ */

#ifdef HAVE_CONFIG_H
#  include "config.h"
#endif

#ifdef HAVE_SYS_PARAM_H
#  include <sys/param.h>
#endif

#ifdef HAVE_CSTDLIB
#  include <cstdlib>
#else
#  ifdef HAVE_STDLIB_H
#    include <stdlib.h>
#  endif
#endif

#ifdef HAVE_CLIMITS
#  include <climits>
#else
#  ifdef HAVE_LIMITS_H
#    include <limits.h>
#  endif
#endif

#ifdef HAVE_UNISTD_H
#  include <unistd.h>
#endif

#include <PackageInfo.hh>
#include <HtmlHelp.hh>

using namespace Pipeline;

/*------------------------------------------------------------------------------------------*/
/*  R E A L P A T H                                                                         */
/*                                                                                          */
/*    Just a wrapper around realpath(2).                                                    */
/*------------------------------------------------------------------------------------------*/

/* usage message */ 
void
usage()
{
  std::cerr << "USAGE:\n"
	    << "  realpath path1 [path2 ...]\n"
	    << "\n" 
	    << "  realpath --help\n"
	    << "  realpath --html-help\n"
	    << "  realpath --version\n"
	    << "  realpath --release-date\n"
	    << "  realpath --copyright\n"   
	    << "\n"
	    << "\n"
	    << "Use \"realpath --html-help\" to browse the full documentation.\n" 
	    << std::flush;
}


int
main
(
 int argc, 
 char **argv
)
{
  /* parse command line args */ 
  char msg[1024];
  FB::init(std::cout);
  FB::setWarnings(false);
  FB::setStageStats(false);

  if (argc >= 2) {
    if(strcmp(argv[1], "--help") == 0) {
      usage();
      exit(EXIT_SUCCESS);
    }
    else if(strcmp(argv[1], "--html-help") == 0) {
      HtmlHelp::launch("realpath");
    }
    else if(strcmp(argv[1], "--version") == 0) {
      std::cerr << PackageInfo::sVersion << "\n";
      exit(EXIT_SUCCESS);
    }
    else if(strcmp(argv[1], "--release-date") == 0) {
      std::cerr << PackageInfo::sRelease << "\n";
      exit(EXIT_SUCCESS);
    }
    else if(strcmp(argv[1], "--copyright") == 0) {
      std::cerr << PackageInfo::sCopyright << "\n";
      exit(EXIT_SUCCESS);
    }
  }

  /* resolve the path(s) */ 
  unsigned wk; 
  for(wk=1; wk<argc; wk++) {
    char resolved[MAXPATHLEN];
    if(realpath(argv[wk], resolved) == NULL) {
      char msg[1024];
      sprintf(msg, "Bad input path: %s", argv[wk]);
      FB::error(msg);
    }
  
    printf("%s\n", resolved);
  }

  exit(EXIT_SUCCESS);
}
