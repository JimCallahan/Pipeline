// $Id: TimeInterval.java,v 1.1 2007/03/28 19:31:03 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   T I M E   I N T E R V A L                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A specific interval of time, with millisecond precision.
 */
public
class TimeInterval
  implements Glueable, Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */
  public
  TimeInterval() 
  {}

  /** 
   * Construct a new time interval.
   * 
   * @param startStamp
   *   The timestamp (milliseconds since midnight, January 1, 1970 UTC) of the start of 
   *   the interval. 
   * 
   * @param endStamp
   *   The timestamp (milliseconds since midnight, January 1, 1970 UTC) of the end of 
   *   the interval. 
   */ 
  public
  TimeInterval
  (
   long startStamp, 
   long endStamp
  ) 
  {
    if(startStamp <= endStamp) {
      pStartStamp = startStamp; 
      pEndStamp   = endStamp; 
    }
    else {
      pStartStamp = endStamp; 
      pEndStamp   = startStamp; 
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Gets the timestamp (milliseconds since midnight, January 1, 1970 UTC) of the start of 
   * the interval. 
   */ 
  public long 
  getStartStamp() 
  {
    return pStartStamp; 
  }
  
  /**
   * Gets the timestamp (milliseconds since midnight, January 1, 1970 UTC) of the end of 
   * the interval. 
   */ 
  public long 
  getEndStamp() 
  {
    return pEndStamp; 
  }
  
  /**
   * Gets the duration of the interval (in milliseconds). 
   */ 
  public long
  getDuration() 
  {
    return (pEndStamp - pStartStamp);
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public void 
  toGlue
  ( 
   GlueEncoder encoder  
  ) 
    throws GlueException
  {
    encoder.encode("StartStamp", pStartStamp);
    encoder.encode("EndStamp", pEndStamp);
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    {
      Long stamp = (Long) decoder.decode("StartStamp"); 
      if(stamp == null) 
	throw new GlueException("The \"StartStamp\" was missing!");
      pStartStamp = stamp;
    }

    {
      Long stamp = (Long) decoder.decode("EndStamp"); 
      if(stamp == null) 
	throw new GlueException("The \"EndStamp\" was missing!");
      pEndStamp = stamp;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5377066219736012095L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The timestamp (milliseconds since midnight, January 1, 1970 UTC) of the start of the 
   * interval. 
   */ 
  private long pStartStamp; 
  
  /**
   * The timestamp (milliseconds since midnight, January 1, 1970 UTC) of the end of the 
   * interval. 
   */ 
  private long  pEndStamp; 
  
}



