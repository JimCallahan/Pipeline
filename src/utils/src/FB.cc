// $Id: FB.cc,v 1.3 2004/04/09 17:55:12 jim Exp $

#include <FB.hh>

namespace Pipeline {

#ifdef HAVE_IOSTREAM
  using std::ostream;
#endif

/*------------------------------------------------------------------------------------------*/
/*   F E E D B A C K                                                                        */
/*                                                                                          */
/*     Static class which provides central control over timing stats, warnings, errors and  */
/*     all other user messages.                                                             */
/*------------------------------------------------------------------------------------------*/

LockSet::Lock* FB::sLock = NULL;

ostream* FB::sOut = NULL;
	 
bool   FB::sWarnings     = false;
UInt32 FB::sWarningLevel = 1;

bool   FB::sStageStats = false; 
UInt32 FB::sStatLevel  = 1;   
	 
Int32  FB::sNumTimers  = 50;
Int32  FB::sTimerLevel = -1;
Timer* FB::sTimers     = NULL;
	 
Int32  FB::sIndentLevel = -2;
	  
} // namespace Pipeline
