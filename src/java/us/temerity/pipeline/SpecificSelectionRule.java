// $Id: SpecificSelectionRule.java,v 1.1 2006/01/05 16:54:43 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;
import java.text.*;

/*------------------------------------------------------------------------------------------*/
/*   S P E C I F I C   S E L E C T I O N   R U L E                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A selection schedule rule which is active only on a specific date.
 */
public
class SpecificSelectionRule
  extends IntervalSelectionRule
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new selection rule.
   */ 
  public
  SpecificSelectionRule() 
  {
    init();
  }

  /**
   * Copy constructor. 
   */ 
  public
  SpecificSelectionRule
  (
   SelectionRule rule
  )
  {
    super(rule);
    init();

    if(rule instanceof SpecificSelectionRule) {
      SpecificSelectionRule srule = (SpecificSelectionRule) rule;

      pStartYear  = srule.pStartYear;
      pStartMonth = srule.pStartMonth;
      pStartDay   = srule.pStartDay;  
    } 
  }


  /*----------------------------------------------------------------------------------------*/
 
  private void 
  init() 
  {
    Calendar cal = new GregorianCalendar();
    cal.setTime(new Date());
    
    pStartYear  = cal.get(Calendar.YEAR);
    pStartMonth = cal.get(Calendar.MONTH);
    pStartDay   = cal.get(Calendar.DAY_OF_MONTH);    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set local date when the rule begins. 
   * 
   * @param year
   *   The year [2006+].
   * 
   * @param month
   *   The month of the year (see {@link Calendar#MONTH Calendar.MONTH})
   * 
   * @param day
   *   The day of the month (see {@link Calendar#DAY_OF_MONTH Calendar.DAY_OF_MONTH})
   */ 
  public void 
  setStartDate
  (
   int year,
   int month, 
   int day
  ) 
  { 
    if(year < 2006)
      throw new IllegalArgumentException
	("The year must be at least (2006)!"); 

    if((month < 0) || (month > 11)) 
      throw new IllegalArgumentException
	("The month of the year must be in the [0, 11] range!");

    if(day < 1)
      throw new IllegalArgumentException
	("The day of the month must be at least (1)."); 

    switch(month) {
    case 1:
      if(day > 28) 
	throw new IllegalArgumentException
	  ("The day of the month must be in the [1, 28] range!");
      break;

    case 3:
    case 5:
    case 8:
    case 10:
      if(day > 28) 
	throw new IllegalArgumentException
	  ("The day of the month must be in the [1, 30] range!");
      break;

    default:
      if(day > 28) 
	throw new IllegalArgumentException
	  ("The day of the month must be in the [1, 31] range!");
    }

    pStartYear  = year;
    pStartMonth = month; 
    pStartDay   = day; 
  }

  /** 
   * Get the year [2006+] when the rule begins.
   */ 
  public int
  getStartYear()
  {
    return pStartYear;
  }
  
  /** 
   * Get the month of the year (see {@link Calendar#MONTH Calendar.MONTH}) when the rule 
   * begins.
   */ 
  public int
  getStartMonth()
  {
    return pStartMonth;
  }
  
  /** 
   * Get the day of the month (see {@link Calendar#DAY_OF_MONTH Calendar.DAY_OF_MONTH}) 
   * when the rule begins.
   */ 
  public int
  getStartDay()
  {
    return pStartDay;
  }
  
  /**
   * Get the string representation of the local date when the rule begins.
   */
  public String
  getStartDateString() 
  {
    DecimalFormat fmt = new DecimalFormat("00");
    return (pStartYear + "-" + fmt.format(pStartMonth+1) + "-" + fmt.format(pStartDay));
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the rule is active during the given point in time.
   */ 
  public boolean
  isActive
  (
   Date date
  )
  {
    Calendar cal = new GregorianCalendar();

    cal.set(pStartYear, pStartMonth, pStartDay, pStartHour, pStartMinute); 
    Date startDate = cal.getTime();

    cal.set(pStartYear, pStartMonth, pStartDay, pEndHour, pEndMinute); 
    if((pEndHour < pStartHour) ||
       ((pEndHour == pStartHour) && (pEndMinute < pStartMinute))) 
      cal.add(Calendar.DAY_OF_MONTH, 1);
    Date endDate = cal.getTime();
    
    return ((date.compareTo(startDate) >= 0) && (date.compareTo(endDate) < 0));
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
    return new SpecificSelectionRule(this);
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
    
    encoder.encode("StartYear", pStartYear);    
    encoder.encode("StartMonth", pStartMonth);
    encoder.encode("StartDay", pStartDay);
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
      Integer year = (Integer) decoder.decode("StartYear"); 
      if(year == null) 
	throw new GlueException
	  ("The start year was missing!");
      
      Integer month = (Integer) decoder.decode("StartMonth"); 
      if(month == null) 
	throw new GlueException
	    ("The start month was missing!");
      
      Integer day = (Integer) decoder.decode("StartDay"); 
      if(day == null) 
	throw new GlueException
	    ("The start day was missing!");
      
      setStartDate(year, month, day);
    }
    catch(IllegalArgumentException ex) {
      throw new GlueException(ex);
    }
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3892145007992139830L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The hour of the year [2006+], month [0, 11] and day of the month [1, 31] in local time 
   * when the rule begins.
   */ 
  private int pStartYear;
  private int pStartMonth;
  private int pStartDay;

}
