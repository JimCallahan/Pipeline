// $Id: Timer.hh,v 1.1 2003/09/22 16:44:37 jim Exp $

#ifndef PIPELINE_TIMER_HH
#define PIPELINE_TIMER_HH 

#ifdef HAVE_CONFIG_H
#  include "config.h"
#endif

#ifdef HAVE_SYS_TIME_H
#  include <sys/time.h>
#endif

#ifdef HAVE_TIME_H
#  include <time.h>
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

#ifdef HAVE_IOSTREAM
#  include <iostream>
#else
#  ifdef HAVE_IOSTREAM_H
#    include <iostream.h>
#  endif
#endif

#ifdef HAVE_IOMANIP
#  include <iomanip>
#else
#  ifdef HAVE_IOMANIP_H
#    include <iomanip.h>
#  endif
#endif

namespace Pipeline {

#ifdef HAVE_IOSTREAM
  using std::ostream;
#endif

/*------------------------------------------------------------------------------------------*/
/*   T I M E R                                                                              */
/*                                                                                          */
/*     A portable low-resolution timer, mostly for printing progress messages.              */
/*------------------------------------------------------------------------------------------*/

class Timer
{
public:
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/
											    
  /* construct uninitialized */								    
  Timer()										    
  {											    
    reset();										    
  }											    
											    
											    
  /*----------------------------------------------------------------------------------------*/
  /*   D E S T R U C T O R                                                                  */
  /*----------------------------------------------------------------------------------------*/
											    
  ~Timer()										    
  {}											    
											    
											    
  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/
											    
  /* restart the timer */								    
  void											    
  reset()										    
  {											    
    gettimeofday(&pStart, NULL);							    
  }											    
											    
											    
  /* time in seconds since the start (or reset) of the timer */				    
  Real64										    
  seconds() const 									    
  {											    
    struct timeval tm;									    
											    
    gettimeofday(&tm, NULL);								    
											    
    tm.tv_sec  -= pStart.tv_sec;							    
    tm.tv_usec -= pStart.tv_usec;							    
    											    
    if(tm.tv_usec < 0) {								    
      tm.tv_sec--;									    
      tm.tv_usec += 1000000;								    
    }											    
    											    
    return (((Real64) tm.tv_sec) + (((Real64) tm.tv_usec) / 1000000.0));		    
  }											    
											    
											    
											    
  /*----------------------------------------------------------------------------------------*/
  /*  I / O                                                                                 */
  /*----------------------------------------------------------------------------------------*/
											    
  friend ostream&									    
  operator<< 										    
  (											    
   ostream& out, 									    
   Timer& timer										    
  )											    
  {											    
    char buf[1024];   									    
    struct timeval tm;									    
											    
    gettimeofday(&tm, NULL);								    
    tm.tv_sec  -= timer.pStart.tv_sec;							    
    tm.tv_usec -= timer.pStart.tv_usec;							    
											    
    if(tm.tv_usec < 0) {								    
      tm.tv_sec--;									    
      tm.tv_usec += 1000000;								    
    }											    
											    
    time_t hrs = tm.tv_sec / 3600;							    
    time_t min = (tm.tv_sec / 60) - (hrs * 60);						    
    time_t sec = tm.tv_sec - (hrs * 3600) - (min * 60);					    
											    
    sprintf(buf, "%02ld:%02ld:%02ld.%02ld", 						    
	    (long) hrs, (long) min, (long) sec, (long) (tm.tv_usec / 10000));		    
											    
    out << buf;										    
											    
    return out;										    
  }											    
    											    
protected:										    
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  struct timeval  pStart;       /* time at which the timer was started */

};

} // namespace Pipeline

#endif  
