// $Id: DateInterval.java,v 1.1 2006/11/21 19:42:31 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   D A T E   I N T E R V A L                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A specific interval of time, with millisecond precision.
 */
public
class DateInterval
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
  DateInterval() 
  {}

  /** 
   * Construct a new time interval.
   * 
   * @param startStamp
   *   The timestamp of the start of the interval. 
   * 
   * @param endStamp
   *   The timestamp of the end of the interval. 
   */ 
  public
  DateInterval
  (
   Date startStamp, 
   Date endStamp
  ) 
  {
    if(startStamp == null) 
      throw new IllegalArgumentException("The start timestamp cannot be (null)!");

    if(endStamp == null) 
      throw new IllegalArgumentException("The end timestamp cannot be (null)!");

    if(startStamp.compareTo(endStamp) <= 0) {
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
   * Gets the timestamp of the start of the interval. 
   */ 
  public Date 
  getStartStamp() 
  {
    return pStartStamp; 
  }
  
  /**
   * Gets the timestamp of the end of the interval. 
   */ 
  public Date 
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
    return (pEndStamp.getTime() - pStartStamp.getTime());
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
    encoder.encode("StartStamp", pStartStamp.getTime());
    encoder.encode("EndStamp", pEndStamp.getTime());
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
      pStartStamp = new Date(stamp);
    }

    {
      Long stamp = (Long) decoder.decode("EndStamp"); 
      if(stamp == null) 
	throw new GlueException("The \"EndStamp\" was missing!");
      pEndStamp = new Date(stamp);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1147332633792836963L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The timestamp of the start of the interval. 
   */ 
  private Date  pStartStamp; 
  
  /**
   * The timestamp of the end of the interval. 
   */ 
  private Date  pEndStamp; 
  
}



