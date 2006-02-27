// $Id: IntervalSelectionRule.java,v 1.2 2006/02/27 17:59:16 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;
import java.text.*;

/*------------------------------------------------------------------------------------------*/
/*   I N T E R V A L   S E L E C T I O N   R U L E                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The common base class of rules which specify a period of time when selection group 
 * should be assigned to a set of job servers. 
 */
public
class IntervalSelectionRule
  extends SelectionRule
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
  IntervalSelectionRule()
  {
    init();
  }

  /**
   * Copy constructor. 
   */ 
  protected 
  IntervalSelectionRule
  (
   SelectionRule rule
  )
  {
    super(rule);
    init();

    if(rule instanceof IntervalSelectionRule) {
      IntervalSelectionRule irule = (IntervalSelectionRule) rule;

      pStartHour   = irule.pStartHour;
      pStartMinute = irule.pStartMinute;

      pEndHour   = irule.pEndHour;
      pEndMinute = irule.pEndMinute;
    }
  }

  
  /*----------------------------------------------------------------------------------------*/
 
  private void 
  init() 
  {
    pStartHour   = 0;
    pStartMinute = 0; 

    pEndHour   = 23; 
    pEndMinute = 59; 
  }

   
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the hour of the day and minute in local time when the rule begins.
   * 
   * @param hour
   *   The hour of the day (see {@link Calendar#HOUR Calendar.HOUR}).
   * 
   * @param minute 
   *   The minute of the hour (see {@link Calendar#MINUTE Calendar.MINUTE}).
   */ 
  public void 
  setStartTime
  (
   int hour, 
   int minute
  ) 
  {
    if((hour < 0) || (hour > 23)) 
      throw new IllegalArgumentException
	("The hour of the day must be in the [0, 23] range!");

    if((minute < 0) || (minute > 59)) 
      throw new IllegalArgumentException
	("The minute of the hour must be in the [0, 59] range!");

    pStartHour   = hour;
    pStartMinute = minute;
  }

  /** 
   * Get the hour of the day (see {@link Calendar#HOUR Calendar.HOUR}) in local time when 
   * the rule begins.
   */ 
  public int
  getStartHour()
  {
    return pStartHour;
  }
  
  /** 
   * Get the minute of the hour (see {@link Calendar#MINUTE Calendar.MINUTE}) in local 
   * time when the rule begins.
   */ 
  public int
  getStartMinute()
  {
    return pStartMinute;
  }

  /**
   * Get the string representation of the local time the rule begins.
   */
  public String
  getStartTimeString() 
  {
    DecimalFormat fmt = new DecimalFormat("00");
    return (fmt.format(pStartHour) + ":" + fmt.format(pStartMinute));
  }
  


  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the hour of the day and minute in local time when the rule ends.
   * 
   * @param hour
   *   The hour of the day (see {@link Calendar#HOUR Calendar.HOUR}).
   * 
   * @param minute 
   *   The minute of the hour (see {@link Calendar#MINUTE Calendar.MINUTE}).
   */ 
  public void 
  setEndTime
  (
   int hour, 
   int minute
  ) 
  {
    if((hour < 0) || (hour > 23)) 
      throw new IllegalArgumentException
	("The hour of the day must be in the [0, 23] range!");

    if((minute < 0) || (minute > 59)) 
      throw new IllegalArgumentException
	("The minute of the hour must be in the [0, 59] range!");

    pEndHour   = hour;
    pEndMinute = minute;
  }

  /** 
   * Get the hour of the day (see {@link Calendar#HOUR Calendar.HOUR}) in local time when 
   * the rule ends.
   */ 
  public int
  getEndHour()
  {
    return pEndHour;
  }
  
  /** 
   * Get the minute of the hour (see {@link Calendar#MINUTE Calendar.MINUTE}) in local 
   * time when the rule ends.
   */ 
  public int
  getEndMinute()
  {
    return pEndMinute;
  }
  
  /**
   * Get the string representation of the local time the rule ends.
   */
  public String
  getEndTimeString() 
  {
    DecimalFormat fmt = new DecimalFormat("00");
    return (fmt.format(pEndHour) + ":" + fmt.format(pEndMinute));
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
    super.toGlue(encoder); 
    
    encoder.encode("StartHour", pStartHour);    
    encoder.encode("StartMinute", pStartMinute);
    
    encoder.encode("EndHour", pEndHour);    
    encoder.encode("EndMinute", pEndMinute);
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    try {
      {
	Integer hour = (Integer) decoder.decode("StartHour"); 
	if(hour == null) 
	  throw new GlueException
	    ("The start hour was missing!");
	
	Integer minute = (Integer) decoder.decode("StartMinute"); 
	if(minute == null) 
	  throw new GlueException
	    ("The start minute was missing!");
	
	setStartTime(hour, minute);
      }

      {
	Integer hour = (Integer) decoder.decode("EndHour"); 
	if(hour == null) 
	  throw new GlueException
	    ("The end hour was missing!");
	
	Integer minute = (Integer) decoder.decode("EndMinute"); 
	if(minute == null) 
	  throw new GlueException
	    ("The end minute was missing!");
	
	setEndTime(hour, minute);
      }
    }
    catch(IllegalArgumentException ex) {
      throw new GlueException(ex);
    }
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4496291843070313646L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The hour of the day [0, 23] and minute [0, 59] in local time when the rule begins.
   */ 
  protected int  pStartHour;
  protected int  pStartMinute;

  /**
   * The hour of the day [0, 23] and minute [0, 59] in local time when the rule ends. <P> 
   * 
   * If the end time is earlier than the start time, then it refers to the following day.
   */
  protected int  pEndHour;
  protected int  pEndMinute;

}
