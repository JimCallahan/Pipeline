// $Id: HtmlHelp.hh,v 1.4 2008/10/09 03:07:13 jim Exp $

#ifndef PIPELINE_HTML_HELP_HH
#define PIPELINE_HTML_HELP_HH 

#ifdef HAVE_CONFIG_H
#  include "config.h"
#endif

#ifdef HAVE_CASSERT
#  include <cassert>
#else
#  ifdef HAVE_ASSERT_H
#    include <assert.h>
#  endif
#endif

#ifdef HAVE_CSTDIO
#  include <cstdio>
#else
#  ifdef HAVE_STDIO_H
#    include <stdio.h>
#  endif
#endif

#ifdef HAVE_UNISTD_H
#  include <unistd.h>
#endif

#ifdef HAVE_CSTRING_H
#  include <cstring>
#else 
#  ifdef HAVE_STRING_H
#    include <string.h>
#  endif
#endif

namespace Pipeline {

#ifdef HAVE_IOSTREAM
  using std::ostream;
#endif

/*------------------------------------------------------------------------------------------*/
/*   H T M L   H E L P                                                                      */
/*                                                                                          */
/*     A launcher of mozilla(1) for viewing help documentation.                             */
/*------------------------------------------------------------------------------------------*/

class HtmlHelp
{
public:
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/
											    
  static void
  launch
  (
   const char* name   /* IN: the name of the application */ 
  )										    
  {											    
    char buf[1024];
    sprintf(buf, "openURL(file:%s/man/%s.html, new-tab)", PackageInfo::sDocsDir, name); 
    
    char* args[4]; 
    args[0] = strdup("mozilla");
    args[1] = strdup("-remote");
    args[2] = strdup(buf);
    args[3] = NULL;
    
    execvp("mozilla", args);

    exit(EXIT_FAILURE);
  }											    
											    
};

} // namespace Pipeline

#endif  
