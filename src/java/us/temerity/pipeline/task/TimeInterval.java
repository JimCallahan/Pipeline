// $Id: TimeInterval.java,v 1.2 2008/02/13 19:22:34 jesse Exp $

package us.temerity.pipeline.task;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   T I M E   I N T E R V A L                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * An immutable time interval.
 */
public
class TimeInterval
  implements Cloneable, Glueable, Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  TimeInterval()
  {}

  /**
   * Construct a new time interval.
   * 
   */ 
  public
  TimeInterval
  (
    Long startDate, 
    Long endDate
  ) 
  {
    if(startDate == null) 
      throw new IllegalArgumentException
	("The start date cannot be (null)!");

    if(endDate == null) 
      throw new IllegalArgumentException
	("The end date cannot be (null)!");

    if(startDate < endDate) 
      throw new IllegalArgumentException
	("The start date (" + startDate + ") must be earlier than the " + 
	 "end date (" + endDate + ")!"); 

    pStart = startDate;
    pEnd   = endDate;
  }

  /**
   * Copy constructor.
   */ 
  public 
  TimeInterval
  (
   TimeInterval tval
  ) 
  {
    pStart = tval.getStart();
    pEnd   = tval.getEnd();
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the beginning of the time interval.
   */ 
  public Long
  getStart() 
  {
    return pStart;
  }

  /**
   * Get the ending of the time interval.
   */ 
  public Long
  getEnd() 
  {
    return pEnd;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O B J E C T   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Indicates whether some other object is "equal to" this one.
   * 
   * @param obj 
   *   The reference object with which to compare.
   */
  public boolean
  equals
  (
   Object obj   
  )
  {
    if((obj != null) && (obj instanceof TimeInterval)) {
      TimeInterval tval = (TimeInterval) obj;
      return (pStart.equals(tval.pStart) && 
	      pEnd.equals(tval.pEnd));
    }
    return false;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C L O N E A B L E                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Return a deep copy of this object.
   */
  public Object 
  clone()
  {
    return new TimeInterval(this);
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
    encoder.encode("Start", pStart);
    encoder.encode("End",   pEnd);    
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    Long startDate = (Long) decoder.decode("Start");
    if(startDate == null) 
      throw new GlueException("The \"Start\" was missing or (null)!");
    pStart = startDate;

    Long endDate = (Long) decoder.decode("End");
    if(endDate == null) 
      throw new GlueException("The \"End\" was missing or (null)!");
    pEnd = endDate;
  }
 


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4620597959078996297L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The beginning of the time interval.
   */ 
  private Long  pStart; 

  /**
   * The ending of the time interval.
   */ 
  private Long  pEnd; 

}
