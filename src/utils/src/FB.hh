// $Id: FB.hh,v 1.4 2004/04/09 17:55:12 jim Exp $

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
#include <LockSet.hh>

namespace Pipeline {

/*------------------------------------------------------------------------------------------*/
/*   F E E D B A C K                                                                        */
/*                                                                                          */
/*     Static class which provides central control over timing stats and warnings messages. */
/*------------------------------------------------------------------------------------------*/

class FB
{
public:
  /*----------------------------------------------------------------------------------------*/
  /*   I N I T I A L I Z A T I O N                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the output stream.
   * 
   * param out
   *   The output stream to use for messages.
   * 
   * param interactive
   *   Should the messages have low latency (at a small cost)?
   */ 
  static void 
  init
  (
   ostream& out,            
   bool interactive = true   
  ) 
  {
    /* initialize the locks */ 
    LockSet::init();
    sLock = LockSet::newLock();
    assert(sLock != NULL);

    LockSet::lock(sLock);
    {
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
    LockSet::unlock(sLock);
  }


  /**
   * Has the output stream and its associated locks and timers been initialized? 
   */ 
  static bool
  isInitialized() 
  {
    return ((sLock != NULL) && (sOut != NULL) && (sTimers != NULL));
  }



public:	
  /*----------------------------------------------------------------------------------------*/
  /*   E X T E R N A L   L O C K I N G                                                      */
  /*----------------------------------------------------------------------------------------*/
	
  /**
   * Lock the output stream.
   */ 
  static void
  lock()
  {
    LockSet::lock(sLock);
  }
  	
  /**
   * Get the output stream 
   */ 
  static ostream&
  getStream()
  {
    return (*sOut);
  }

  /**
   * Unlock the output stream.
   */ 
  static void
  unlock()
  {
    LockSet::unlock(sLock);
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   W A R N I N G   M E S S A G E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Will warning messages of the given verbosity level be reported?
   * 
   * param level
   *   The verbosity level.
   */ 
  static bool 										    
  hasWarnings
  (											    
   UInt32 level = 1       
  ) 											    
  {		
    bool tf = false;
    LockSet::lock(sLock);						    
    {
      tf = (sWarnings && (level <= sWarningLevel));
    }
    LockSet::unlock(sLock);	
    return tf;		
  }											    

	
  /** 
   * Get the current warning verbosity level.
   */ 
  static UInt32 									    
  getWarningLevel() 									    
  {											    
    return sWarningLevel;								    
  }											    
											    
  /**
   * Set the warning message verbosity level.
   * 
   * param tf
   *   Whether to generate any warning messages.
   * 
   * paral level
   *   The verbosity level.
   */ 						    
  static void 										    
  setWarnings										    
  (											    
   bool tf = true,       		    
   UInt32 level = 1     			    
  ) 											    
  {	
    assert(level > 0);

    LockSet::lock(sLock);						    
    {									    
      sWarnings     = tf; 
      sWarningLevel = level;
    }
    LockSet::unlock(sLock);
  }	

											    
  /**
   * Report a warning message.
   * 
   * param msg
   *  The message text.
   * 
   * paral level
   *   The verbosity level.
   */ 
  static void 										    
  warn											    
  (											    
   const char* msg,    					    
   UInt32 level = 1    		    
  ) 											    
  {											    
    assert(msg);									    

    LockSet::lock(sLock);						    
    {									    
      if(sWarnings && (level <= sWarningLevel)) {	
	assert(sOut);									    
	(*sOut) << "WARNING: " << msg << "\n";	
      }
    }
    LockSet::unlock(sLock);					    
  }											    
											    
										    
  											    
  /*----------------------------------------------------------------------------------------*/
  /*   S T A G E   S T A T S                                                                */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Will stage messages of the given verbosity level be reported?
   * 
   * param level
   *   The verbosity level.
   */ 										    
  static bool 										    
  hasStageStats										    
  (											    
   UInt32 level = 1      			    
  ) 											    
  {		
    bool tf = false;
    LockSet::lock(sLock);						    
    {
      tf = (sStageStats && (level <= sStatLevel));
    }
    LockSet::unlock(sLock);
    return tf;
  }											    
		
  /** 
   * Get the current stage verbosity level.
   */ 										    
  static UInt32 									    
  getStatLevel() 									    
  {											    
    return sStatLevel;									    
  }				
											    
  /**
   * Set the stage message verbosity level.
   * 
   * param tf
   *   Whether to generate any stage messages.
   * 
   * paral level
   *   The verbosity level.
   */ 								    
  static void 										    
  setStageStats										    
  (											    
   bool tf = true,        			    
   UInt32 level = 1      		    
  ) 											    
  {		
    LockSet::lock(sLock);						    
    {
      sStageStats = tf;									    
      sStatLevel = level;	
    }
    LockSet::unlock(sLock);								    
  }											    
			    
  /**
   * Start a new nested stage.
   * 
   * This is should not be used with threaded programs as the stage nesting level
   * will get mangled by multiple asynchronous threads.
   * 
   * param title
   *   The name of the stage.
   * 
   * param level 
   *   The verbosity level.
   */
  static void 										    
  stageBegin										    
  (											    
   const char* title, 
   UInt32 level = 1   
  ) 
    throw(std::runtime_error)	
  {											    
    assert(title);									    
    assert(sTimers);									    
	
    LockSet::lock(sLock);						    
    {				
      if(sStageStats && (level <= sStatLevel)) {
	sIndentLevel += 2;
	sTimerLevel++;	
								    
	if(sTimerLevel < 0) {							 
	  LockSet::unlock(sLock);
	  throw std::runtime_error
	    ("Somehow the stage nesting level has become negative!");	 
	}

	if(sTimerLevel >= sNumTimers) {
	  LockSet::unlock(sLock);							    
	  throw std::runtime_error
	    ("Somehow the maximum stage nesting level has been exceeded!");
	}

	sTimers[sTimerLevel].reset();							    
											    
	indent();							      
	assert(sOut);									    
	(*sOut) << title << "\n";
      }
    }
    LockSet::unlock(sLock);
  }
											    
  /**
   * Additional sub-stage progress messages.
   * 
   * param msg
   *  The message text.
   * 
   * param level
   *   The verbosity level.
   */ 						    
  static void										    
  stageMsg										    
  (											    
   const char* msg,     			    
   UInt32 level = 1    			    
  ) 											    
  {											    
    assert(msg);									    
	
    LockSet::lock(sLock);						    
    {
      if(sStageStats && (level <= sStatLevel)) {
	indent(1);
	assert(sOut);									    
	(*sOut) << msg << "\n";
      }
    }
    LockSet::unlock(sLock);		
  }											    
											    
  /**
   * Just a blank line.
   * 
   * param level
   *   The verbosity level.
   */ 								    
  static void										    
  stageMsg										    
  (											    
   UInt32 level = 1     		    
  )											    
  {	
    LockSet::lock(sLock);						    
    {
      if(sStageStats && (level <= sStatLevel)) {
	assert(sOut);									    
	(*sOut) << "\n";
      }
    }
    LockSet::unlock(sLock);
  }
											    
  /**
   * Get the time (in seconds) since the start of the stage.
   * 
   * This is should not be used with threaded programs as the stage nesting level
   * will get mangled by multiple asynchronous threads.
   */ 				    
  static double										    
  stageTime() 
    throw(std::runtime_error)
  {
    double time;

    LockSet::lock(sLock);						    
    {    
      if(sTimerLevel < 0) {
	LockSet::unlock(sLock);
	throw std::runtime_error
	  ("Somehow the stage nesting level has become negative!");	 
      }

      if(sTimerLevel >= sNumTimers) {
	LockSet::unlock(sLock);
	throw std::runtime_error
	  ("Somehow the maximum stage nesting level has been exceeded!"); 
      }
    											    
      assert(sTimers);									    
      time = sTimers[sTimerLevel].seconds();
    }
    LockSet::unlock(sLock);
    
    return time;
  }											    
											    
  /**
   * End of the current nested stage.
   * 
   * This is should not be used with threaded programs as the stage nesting level
   * will get mangled by multiple asynchronous threads.
   * 
   * param level 
   *   The verbosity level.
   */ 							    
  static void 										    
  stageEnd										    
  (											    
   UInt32 level = 1      		    
  )
    throw(std::runtime_error)
  {
    LockSet::lock(sLock);						    
    {				
      if(sStageStats && (level <= sStatLevel)) {
	if(sTimerLevel < 0) {							 
	  LockSet::unlock(sLock);
	  throw std::runtime_error
	    ("Somehow the stage nesting level has become negative!");	 
	}

	if(sTimerLevel >= sNumTimers) {
	  LockSet::unlock(sLock);							    
	  throw std::runtime_error
	    ("Somehow the maximum stage nesting level has been exceeded!");
	}
	
	indent();
	assert(sOut);									    
	assert(sTimers);
	(*sOut) << (sTimers[sTimerLevel]) << "\n\n" << std::flush;
											    
	sIndentLevel -= 2;   								    
											    
	if(sTimerLevel >= 0) 								    
	  sTimerLevel--;
	else {
	  LockSet::unlock(sLock);
	  throw std::runtime_error
	    ("Attempted to end a non-existent stage!");
	}
      }
    }
    LockSet::unlock(sLock);
  }  											    
											    

  /**
   * Non-stage related message with level based indentation.
   * 
   * Should be used for messages from threaded programs.
   * 
   * param msg
   *  The message text.
   * 
   * param level
   *   The verbosity level.
   * 
   * param name
   *   The title of the calling thread.
   * 
   * param pid
   *   The process ID of the calling thread.
   */ 						    
  static void										    
  threadMsg
  (											    
   const char* msg,
   UInt32 level = 1,
   const char* name = NULL,  
   Int32 pid = -1
  ) 											    
  {											    
    assert(msg);									    
	
    LockSet::lock(sLock);						    
    {
      if(sStageStats && (level <= sStatLevel)) {
	assert(sOut);

	Int32 wk;						   
	for(wk=0; wk<level; wk++) 			 
	  (*sOut) << "  ";	

	if(name != NULL) 
	  (*sOut) << name;

	if(pid != -1) 
	  (*sOut) << "[" << pid << "]";
	
	if((name != NULL) || (pid != -1)) 
	  (*sOut) << " - ";

	(*sOut) << msg << "\n";
      }
    }
    LockSet::unlock(sLock);		
  }								    


			
private:										    
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
							    
  											    
											    
private:										    
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The ID of the lock which protects access to the following internal variables.
   */  
  static LockSet::Lock*  sLock;


  /**
   * The output stream (cerr by default).
   */ 
  static ostream*  sOut;                 


  /** 
   * Should runtime warning messages be reported?
   */ 
  static bool  sWarnings;        
  
  /**
   * The level of verbosity of warning messages.
   */ 
  static UInt32  sWarningLevel;       

			
  /** 
   * Should stage timing statistics and messages be reported?
   */   
  static bool  sStageStats; 

  /**
   * The level of verbosity of stage timing statistics and messages.
   */      
  static UInt32  sStatLevel;     


  /**
   * The total numer of nested stages allowed.
   */ 
  static Int32  sNumTimers; 

  /**
   * The current stage nesting level.
   */
  static Int32  sTimerLevel;          

  /**
   * The stage timers.
   */  
  static Timer*  sTimers;            


  /**
   * The current nesting indentation.
   */    
  static Int32  sIndentLevel;         

};

} // namespace Pipeline

#endif
