// $Id: PlCommon.hh,v 1.1 2003/08/20 00:08:22 jim Exp $

#ifndef PIPELINE_PLCOMMON_HH
#define PIPELINE_PLCOMMON_HH

#ifdef HAVE_CONFIG_H
#  include "config.h"
#endif

#ifdef CSTRING_H
#  include <cstring>
#else 
#  ifdef HAVE_STRING_H
#    include <string.h>
#  endif
#endif

namespace Pipeline {

/*------------------------------------------------------------------------------------------*/
/*   P L C O M M O N                                                                        */
/*                                                                                          */
/*     Common support classes for plput(1) and plget(1).                                    */
/*------------------------------------------------------------------------------------------*/

class PathPair
{
public:
  PathPair
  (
   bool isLink, 
   char* work,
   char* repo
  ) : 
    uIsLink(isLink), 
    uWork(strdup(work)),
    uRepo(strdup(repo))
  {
    assert(uWork);
    assert(uRepo);
  }

  ~PathPair()
  {
    assert(uWork);
    delete[] uWork;

    assert(uRepo);
    delete[] uRepo;
  }

public:
  const bool uIsLink;         /* create a symbilic link instead of copying */ 
  const char* uWork;          /* absolute path to working area (or previous repo) file */ 
  const char* uRepo;          /* absolute path to repository file */ 
};


struct StringCmp
{
  bool operator()
  (
   const char* a, 
   const char* b
  ) const
  {
    return strcmp(a, b) < 0;
  }
};

} // namespace Pipeline

#endif


