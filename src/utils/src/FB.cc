// $Id: FB.cc,v 1.1 2003/09/22 16:44:37 jim Exp $

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

ostream* FB::sOut                = NULL;
	 
bool     FB::sExitOnError        = true;

bool     FB::sWarnings           = false;
UInt32   FB::sWarningLevel       = 1;

bool     FB::sStageStats         = false; 
UInt32   FB::sStatLevel          = 1;   
	 
Int32    FB::sNumTimers          = 50;
Int32    FB::sTimerLevel         = -1;
Timer*   FB::sTimers             = NULL;
	 
Int32    FB::sIndentLevel        = -2;
	 
bool     FB::sProgress           = false;
Real32   FB::sProgressPercentage = 0.0f;
	  
} // namespace Pipeline
