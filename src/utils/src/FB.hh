// $Id: FB.hh,v 1.6 2004/08/22 22:17:10 jim Exp $

#ifndef PIPELINE_FB_HH
#define PIPELINE_FB_HH

#include <AtomicTypes.hh>

#ifdef HAVE_CASSERT
#  include <cassert>
#else
#  ifdef HAVE_ASSERT_H
#    include <assert.h>
#  endif
#endif

#ifdef HAVE_IOSTREAM
#  include <iostream>
#else
#  ifdef HAVE_IOSTREAM_H
#    include <iostream.h>
#  endif
#endif

#ifdef HAVE_UNISTD_H
#  include <unistd.h>
#endif

#include <Timer.hh>

namespace Pipeline {

/*------------------------------------------------------------------------------------------*/
/*   F E E D B A C K                                                                        */
/*                                                                                          */
/*     Static class which provides central control over timing stats, warnings, errors and  */
/*     all other user messages.                                                             */
/*------------------------------------------------------------------------------------------*/

class FB
{
public:
  /*----------------------------------------------------------------------------------------*/
  /*   I N I T I A L I Z A T I O N                                                          */
  /*----------------------------------------------------------------------------------------*/

  /* initialize the output stream */ 
  static void 
  init
  (
   ostream& out,             /* the output stream to use for messages */ 
   bool interactive = true   /* should the messages have low latency (at a small cost) */ 
  ) 
  {
    /* reset to initial state */
    reset();

    /* this causes output to be flushed after each insertion,
       for better interactive monitoring at medium performance cost */ 
    assert(&out);
    assert(sOut == NULL);
    sOut = &out;
    assert(sOut);
    if(interactive) 
      (*sOut) << setiosflags(std::ios::unitbuf);

    /* create timers */ 
    assert(sTimers == NULL);
    sTimers = new Timer[sNumTimers];
    assert(sTimers);
  }


  /* has the output stream and timers been initialized? */ 
  static bool
  isInitialized() 
  {
    return ((sOut != NULL) && (sTimers != NULL));
  }



public:
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  											    
  /* get the output stream */ 								    
  static ostream&									    
  getOutput() 										    
  { 											    
    assert(sOut);									    
    return (*sOut);									    
  }											    
											    
  static UInt32 									    
  getWarningLevel() 									    
  {											    
    return sWarningLevel;								    
  }											    
											    
  static UInt32 									    
  getStatLevel() 									    
  {											    
    return sStatLevel;									    
  }											    
											    
											    
  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/
 											    
  static bool 										    
  hasStageStats										    
  (											    
   UInt32 level = 1         /* IN: verbosity level of message */ 			    
  ) 											    
  {											    
    return (sStageStats && (level <= sStatLevel));					    
  }											    
											    
											    
											    
  /*----------------------------------------------------------------------------------------*/
  /*   V E R B O S I T Y                                                                    */
  /*----------------------------------------------------------------------------------------*/
 											    
  /* generate runtime warning messages? */ 						    
  static void 										    
  setWarnings										    
  (											    
   bool tf = true,          /* IN: whether to generate messages */ 			    
   UInt32 level = 1         /* IN: verbosity level of messages */ 			    
  ) 											    
  {											    
    sWarnings = tf;									    
    sWarningLevel = level;								    
  }											    
											    
  /* generate stage timing stats? */ 							    
  static void 										    
  setStageStats										    
  (											    
   bool tf = true,          /* IN: whether to generate stats */ 			    
   UInt32 level = 1         /* IN: verbosity level of stats */ 				    
  ) 											    
  {											    
    sStageStats = tf;									    
    sStatLevel = level;									    
  }											    
											    
											    
											    
  /* generate progress percentage messages? */ 						    
  static void 										    
  setProgress										    
  (											    
   bool tf = true									    
  ) 											    
  {											    
    sProgress = tf;									    
  }											    
											    
  											    
  /*----------------------------------------------------------------------------------------*/
  /*   W A R N I N G   M E S S A G E S                                                      */
  /*----------------------------------------------------------------------------------------*/
 											    
  /* simple string warning message */ 							    
  static void 										    
  warn											    
  (											    
   const char* msg,     /* IN: warning message */  					    
   UInt32 level = 1     /* IN: verbosity level of warning */ 				    
  ) 											    
  {											    
    assert(msg);									    
											    
    if((!sWarnings) || (level > sWarningLevel) || (sOut == NULL))			    
      return; 										    
											    
    assert(sOut);									    
    (*sOut) << "\nWARNING: " << msg << "\n\n";						    
  }											    
											    
											    
  /*----------------------------------------------------------------------------------------*/
  /*   E R R O R   M E S S A G E S                                                          */
  /*----------------------------------------------------------------------------------------*/
											    
  /* exit program on error? */ 								    
  static void										    
  exitOnError										    
  (											    
   bool tf = true									    
  ) 											    
  {											    
    sExitOnError = tf;									    
  }											    
  											    
											    
  /* simple string error message (always goes to stderr) */ 				    
  static void 										    
  error											    
  (											    
   const char* msg = NULL   /* IN: error message */  					    
  ) 											    
  {											    
    if(sOut) 										    
      (*sOut) << std::flush;
											    
    std::cerr << "FATAL ERROR: " << (msg ? msg : "Abort!") << "\n";
											    
    if(sExitOnError) {		
      sleep(2);
      exit(EXIT_FAILURE);								    
    }											    
    else {										    
      recover();									    
    }											    
  }											    
											    
											    
  											    
  /*----------------------------------------------------------------------------------------*/
  /*   S T A G E   S T A T S                                                                */
  /*----------------------------------------------------------------------------------------*/
 											    
  /* start a new stage */ 								    
  static void 										    
  stageBegin										    
  (											    
   const char* title,       /* IN: stage title */  					    
   UInt32 level = 1         /* IN: verbosity level of stage */ 				    
  ) 											    
  {											    
    assert(title);									    
    assert(sTimers);									    
											    
    if((!sStageStats) || (level > sStatLevel) || (sOut == NULL))			    
      return;										    
											    
    sIndentLevel += 2;    								    
											    
    sTimerLevel++;									    
    if(sTimerLevel < 0)									    
      error("Somehow the stage nesting level has become negative!");			    
    if(sTimerLevel >= sNumTimers) 							    
      error("Somehow the maximum stage nesting level has been exceeded!");		    
    sTimers[sTimerLevel].reset();							    
											    
    indent();										    
    assert(sOut);									    
    (*sOut) << title << "\n";								    
  }											    
											    
											    
  /* additional sub-stage progress messages */ 						    
  static void										    
  stageMsg										    
  (											    
   const char* msg,         /* IN: progress message */  				    
   UInt32 level = 1         /* IN: verbosity level of message */ 			    
  ) 											    
  {											    
    assert(msg);									    
											    
    if((!sStageStats) || (level > sStatLevel) || (sOut == NULL))			    
      return;										    
											    
    indent(1);										    
    assert(sOut);									    
    (*sOut) << msg << "\n";								    
  }											    
											    
  /* just a blank line */ 								    
  static void										    
  stageMsg										    
  (											    
   UInt32 level = 1       /* IN: verbosity level of message */ 			    
  )											    
  {											    
    if((!sStageStats) || (level > sStatLevel) || (sOut == NULL))			    
      return;										    
											    
    (*sOut) << "\n";    								    
  }											    
											    
  /* indent and return the stream stage stats are reported on,				    
     WARNING: this should be protected by a conditional call to FB::hasStateStats()! */     
  static ostream& 									    
  stageStream										    
  (											    
   bool indentFirst = true,   /* IN: whether to indent */  				    
   UInt32 level = 1           /* IN: verbosity level of message */ 			    
  ) 											    
  {											    
    assert(sStageStats);								    
    assert(sStatLevel >= level);							    
    assert(sOut);									    
    if(indentFirst)									    
      indent(1);									    
    return (*sOut);									    
  }											    
											    
  /* get the time (in seconds) since the start of the stage */ 				    
  static double										    
  stageTime() 										    
  {											    
    if(sTimerLevel < 0)									    
      error("Somehow the stage nesting level has become negative!");			    
    if(sTimerLevel >= sNumTimers) 							    
      error("Somehow the maximum stage nesting level has been exceeded!");		    
    											    
    assert(sTimers);									    
    return sTimers[sTimerLevel].seconds();						    
  }											    
											    
  /* end of the current stage */ 							    
  static void 										    
  stageEnd										    
  (											    
   UInt32 level = 1       /* IN: verbosity level of message */ 			    
  )											    
  {											    
    if((!sStageStats) || (level > sStatLevel) || (sOut == NULL))			    
      return;										    
											    
    if(sTimerLevel < 0)									    
      error("Somehow the stage nesting level has become negative!");			    
    if(sTimerLevel >= sNumTimers) 							    
      error("Somehow the maximum stage nesting level has been exceeded!");		    
    											    
    indent();										    
    assert(sOut);									    
    assert(sTimers);									    
    (*sOut) << (sTimers[sTimerLevel]) << "\n\n" << std::flush;				    
											    
    sIndentLevel -= 2;   								    
											    
    if(sTimerLevel >= 0) 								    
      sTimerLevel--;									    
    else 										    
      error("Attempted to end a non-existent stage!");					    
  }  											    
											    

  /*----------------------------------------------------------------------------------------*/
  /*   M E S S A G E S                                                                      */
  /*----------------------------------------------------------------------------------------*/
 											    
  /* start a new indented message block */ 						       
  static void 										    
  msgBegin										    
  (											    
   const char* message,       /* IN: message */  					    
   UInt32 level = 1           /* IN: verbosity level of message */   
  ) 											    
  {											    
    assert(message);									    
											    
    if((!sStageStats) || (level > sStatLevel) || (sOut == NULL))    
      return;										    
			
    sIndentLevel += 1;

    indent();										    
    assert(sOut);									    
    (*sOut) << message << "\n";								    
  }											    
											    
  /* end of the current message block */ 			    
  static void 										    
  msgEnd										    
  (											    
   UInt32 level = 1          /* IN: verbosity level of message */ 			    
  )											    
  {											    
    if((!sStageStats) || (level > sStatLevel) || (sOut == NULL))			    
      return;										    
    											    
    indent();										    
    assert(sOut);									    
    (*sOut) << std::endl;			    
											    
    sIndentLevel -= 1;   								    
  }  											    
											    
										    
										    
  /*----------------------------------------------------------------------------------------*/
  /*   P R O G R E S S   P E R C E N T A G E                                                */
  /*----------------------------------------------------------------------------------------*/
 											    
  /* update percentage of task completed (always goes to stderr) */ 			    
  static void 										    
  progress										    
  (											    
   Real32 percent   /* percentage done: [0,1] range */ 					    
  ) 											    
  {											    
    if(!sProgress) 									    
      return; 										    
											    
    sProgressPercentage = percent;							    
    std::cerr << "ALF_PROGRESS " << ((Int32) (sProgressPercentage * 100.0f)) << "%\n";
											    
    /* keep the logs up-to-date with the progress percentage */ 			    
    if(sOut)										    
      (*sOut) << std::flush;	    
  }											    
											    
											    
 											    
protected:										    
  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/
											    
  static void										    
  indent										    
  (											    
   Int32 extra = 0									    
  )											    
  {											    
    assert(sOut);									    
    Int32 wk;										    
    for(wk=0; wk<(sIndentLevel+extra); wk++) 						    
      (*sOut) << " ";									    
  }											    
											    
  /* reset to initial state */								    
  static void										    
  reset() 										    
  {											    
    if(sOut)										    
      sOut->flush();									    
    sOut = NULL;									    
											    
    sExitOnError  = true;								    
											    
    sWarnings     = false;								    
    sWarningLevel = 1;									    
    		 									    
    sStageStats   = false; 								    
    sStatLevel    = 1;   								    
    											    
    sNumTimers  = 50;									    
    sTimerLevel = -1;									    
    if(sTimers) 									    
      delete[] sTimers;									    
    sTimers = NULL;									    
    		  									    
    sIndentLevel  = -2;									    
    											    
    sProgress           = false;							    
    sProgressPercentage = 0.0f;								    
  }											    
											    
   											    
  /* recover from an error, when sExitOnError is false */ 				    
  static void										    
  recover() 										    
  {											    
    if(sTimers) 									    
      delete[] sTimers;									    
    sTimers = new Timer[sNumTimers];							    
											    
    sIndentLevel = -2;									    
  }											    
  											    
											    
protected:										    
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  static ostream* sOut;                 /* the output stream (cerr by default) */ 

  static bool     sExitOnError;         /* exit program on error? */ 
		  
  static bool     sWarnings;            /* generate runtime warning messages? */ 
  static UInt32   sWarningLevel;        /* amount of verbosity in warning messages */ 
		  
  static bool     sStageStats;          /* generate stage timing stats? */ 
  static UInt32   sStatLevel;           /* amount of verbosity in stage timing stats */ 

  static Int32    sNumTimers;           /* total numer of nested stages allowed */ 
  static Int32    sTimerLevel;          /* current stage nesting level */ 
  static Timer*   sTimers;              /* the stage timers */  

  static Int32    sIndentLevel;         /* current nesting indentation */    

  static bool     sProgress;            /* generate progress percentage messages? */ 
  static Real32   sProgressPercentage;  /* percentage of task completed:  [0,1] range */ 

};

} // namespace Pipeline

#endif
