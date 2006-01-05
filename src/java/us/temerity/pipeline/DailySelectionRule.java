// $Id: DailySelectionRule.java,v 1.1 2006/01/05 16:54:43 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   D A I L Y   S E L E C T I O N   R U L E                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A selection schedule rule which has a repeated pattern for days of the week. 
 */
public
class DailySelectionRule
  extends IntervalSelectionRule
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new selection rule.
   */ 
  public
  DailySelectionRule()
  {  
    init();
  }

  /**
   * Copy constructor. 
   */ 
  public
  DailySelectionRule
  (
   SelectionRule rule
  )
  {
    super(rule);
    init();

    if(rule instanceof DailySelectionRule) {
      DailySelectionRule drule = (DailySelectionRule) rule;
      pActiveDays.addAll(drule.pActiveDays);
    }
  }


  /*----------------------------------------------------------------------------------------*/
  
  private void 
  init() 
  {
    pActiveDays = new TreeSet<Integer>();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Add the given local day of the week to the set of days when the rule is active.
   * 
   * @param day
   *   The day of the week (see {@link Calendar#DAY_OF_WEEK Calendar.DAY_OF_WEEK}).
   */ 
  public void 
  addWeekday
  (
   int day
  ) 
  { 
    switch(day) {
    case Calendar.SUNDAY:
    case Calendar.MONDAY:
    case Calendar.TUESDAY:
    case Calendar.WEDNESDAY:
    case Calendar.THURSDAY:
    case Calendar.FRIDAY:
    case Calendar.SATURDAY:
      break;

    default:
      throw new IllegalArgumentException
	("The given day of the week (" + day + ") is not valid!");
    }

    pActiveDays.add(day);
  }
  
  /**
   * Add the given local days of the week to the set of days when the rule is active.
   * 
   * @param days
   *   The days of the week (see {@link Calendar#DAY_OF_WEEK Calendar.DAY_OF_WEEK}).
   */ 
  public void 
  addWeekdays
  (
   TreeSet<Integer> days
  ) 
  { 
    for(Integer day : days) 
      addWeekday(day);
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Remove the given local day of the week from the set of days when the rule is active.
   * 
   * @param day
   *   The day of the week (see {@link Calendar#DAY_OF_WEEK Calendar.DAY_OF_WEEK}).
   */ 
  public void 
  removeWeekday
  (
   int day
  ) 
  { 
    switch(day) {
    case Calendar.SUNDAY:
    case Calendar.MONDAY:
    case Calendar.TUESDAY:
    case Calendar.WEDNESDAY:
    case Calendar.THURSDAY:
    case Calendar.FRIDAY:
    case Calendar.SATURDAY:
      break;

    default:
      throw new IllegalArgumentException
	("The given day of the week (" + day + ") is not valid!");
    }

    pActiveDays.remove(day);
  }

  /**
   * Remove the given local days of the week from the set of days when the rule is active.
   * 
   * @param days
   *   The days of the week (see {@link Calendar#DAY_OF_WEEK Calendar.DAY_OF_WEEK}).
   */ 
  public void 
  removeWeekdays
  (
   TreeSet<Integer> days
  ) 
  { 
    for(Integer day : days) 
      removeWeekday(day);
  }

  /**
   * Remove all days of the week from the set of days when the rule is active.
   */ 
  public void
  clearWeekdays() 
  {
    pActiveDays.clear();
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the days of the week when the rule is active.
   */ 
  public TreeSet<Integer> 
  getActiveWeekdays()
  {
    return new TreeSet<Integer>(pActiveDays);
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the days of the week when the rule is active.
   * 
   * @return
   *   An array of flags for each day of the week: [Sun, Mon, Tue, Wed, Thu, Fri, Sat]
   */ 
  public boolean[]
  getActiveFlags() 
  {
    boolean flags[] = {
      pActiveDays.contains(Calendar.SUNDAY),
      pActiveDays.contains(Calendar.MONDAY), 
      pActiveDays.contains(Calendar.TUESDAY), 
      pActiveDays.contains(Calendar.WEDNESDAY), 
      pActiveDays.contains(Calendar.THURSDAY), 
      pActiveDays.contains(Calendar.FRIDAY), 
      pActiveDays.contains(Calendar.SATURDAY)
    };
    
    return flags; 
  }

  /**
   * Set the days of the week when the rule is active.
   * 
   * @param flags
   *   An array of flags for each day of the week: [Sun, Mon, Tue, Wed, Thu, Fri, Sat]
   */ 
  public void
  setActiveFlags
  (
   boolean[] flags
  ) 
  {
    if(flags.length != 7) 
      throw new IllegalArgumentException
	("There must be exactly (7) elements to the active weekday flags array!");

    if(flags[0]) 
      pActiveDays.add(Calendar.SUNDAY);
    else
      pActiveDays.remove(Calendar.SUNDAY);

    if(flags[1]) 
      pActiveDays.add(Calendar.MONDAY);
    else
      pActiveDays.remove(Calendar.MONDAY);

    if(flags[2]) 
      pActiveDays.add(Calendar.TUESDAY);
    else
      pActiveDays.remove(Calendar.TUESDAY);

    if(flags[3]) 
      pActiveDays.add(Calendar.WEDNESDAY);
    else
      pActiveDays.remove(Calendar.WEDNESDAY);

    if(flags[4]) 
      pActiveDays.add(Calendar.THURSDAY);
    else
      pActiveDays.remove(Calendar.THURSDAY);

    if(flags[5]) 
      pActiveDays.add(Calendar.FRIDAY);
    else
      pActiveDays.remove(Calendar.FRIDAY);

    if(flags[6]) 
      pActiveDays.add(Calendar.SATURDAY);
    else
      pActiveDays.remove(Calendar.SATURDAY);
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/
  

  /**
   * Whether the rule is active on the given day of the week.
   * 
   * @param day
   *   The day of the week (see {@link Calendar#DAY_OF_WEEK Calendar.DAY_OF_WEEK}).
   */ 
  public boolean 
  isActive
  (
   int day
  ) 
  { 
    switch(day) {
    case Calendar.SUNDAY:
    case Calendar.MONDAY:
    case Calendar.TUESDAY:
    case Calendar.WEDNESDAY:
    case Calendar.THURSDAY:
    case Calendar.FRIDAY:
    case Calendar.SATURDAY:
      break;

    default:
      throw new IllegalArgumentException
	("The given day of the week (" + day + ") is not valid!");
    }

    return pActiveDays.contains(day);
  }

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

    cal.setTime(date);
    
    int dow = cal.get(Calendar.DAY_OF_WEEK);
    if(!pActiveDays.contains(dow))
      return false;

    int year  = cal.get(Calendar.YEAR);
    int month = cal.get(Calendar.MONTH);
    int day   = cal.get(Calendar.DAY_OF_MONTH);

    cal.set(year, month, day, pStartHour, pStartMinute); 
    Date startDate = cal.getTime();

    cal.set(year, month, day, pEndHour, pEndMinute); 
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
    return new DailySelectionRule(this);
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
    
    if(!pActiveDays.isEmpty()) 
      encoder.encode("ActiveDays", pActiveDays); 
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    TreeSet<Integer> days = (TreeSet<Integer>) decoder.decode("ActiveDays"); 
    if(days != null)
      pActiveDays = days;
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2807213326945978187L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The local days of the week (see {@link Calendar#DAY_OF_WEEK Calendar.DAY_OF_WEEK}) when 
   * the rule is active.
   */ 
  private TreeSet<Integer>  pActiveDays; 

}
