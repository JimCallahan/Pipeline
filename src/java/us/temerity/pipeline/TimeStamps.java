// $Id: TimeStamps.java,v 1.1 2007/03/28 19:31:03 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.text.*;

/*------------------------------------------------------------------------------------------*/
/*   T I M E   S T A M P S                                                                  */
/*------------------------------------------------------------------------------------------*/

/**                                                                                   
 * A collection of static utility methods related to representing timestamps.
 */
public 
class TimeStamps
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T I O N                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current timestamp (milliseconds since midnight, January 1, 1970 UTC) rounded to 
   * the nearest second.
   */ 
  public static long
  now()
  {
    return ((System.currentTimeMillis() / sSecond) * sSecond);
  } 



  /*----------------------------------------------------------------------------------------*/
  /*   C O N V E R S I O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Convert a standardized string representation into a timestamp (milliseconds since 
   * midnight, January 1, 1970 UTC).<P>
   * 
   * The string must be in the format 
   * (<I>YYYY</I>-<I>MM</I>-<I>DD</I> <I>hh</I>:<I>mm</I>:<I>ss</I>) where: 
   * <DIV style="margin-left: 40px;">
   *   YYYY - The year.
   *   MM - The month of the year.
   *   DD - The day of the month.
   *   hh - The hour of the day (24 hour clock).
   *   mm - The minute of the hour.
   *   ss - The second.
   * </DIV>
   * All values are prefixed with zero where less than the specified number of digits.
   */ 
  public static Long
  parse
  (
   String date
  ) 
    throws ParseException
  {
    if((date == null) || date.equals("-"))
      return null;
    return sFormat.parse(date).getTime();
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Convert a timestamp (milliseconds since midnight, January 1, 1970 UTC) into a 
   * standardized string representation.<P> 
   * 
   * The generated string will have the format 
   * (<I>YYYY</I>-<I>MM</I>-<I>DD</I> <I>hh</I>:<I>mm</I>:<I>ss</I>) where: 
   * <DIV style="margin-left: 40px;">
   *   YYYY - The year.
   *   MM - The month of the year.
   *   DD - The day of the month.
   *   hh - The hour of the day (24 hour clock).
   *   mm - The minute of the hour.
   *   ss - The second.
   * </DIV>
   * All values are prefixed with zero where less than the specified number of digits. 
   */ 
  public static String
  format
  (
   Long stamp 
  ) 
  {
    if(stamp == null) 
      return "-";
    return sFormat.format(new Date(stamp)); 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Convert a time interval into a standardized string representation.<P> 
   */ 
  public static String
  formatInterval
  (
   long interval
  ) 
  {
    if(interval < 0) 
      return "-";

    long time = interval;

    long y = time / sYear; 
    time -= y / sYear; 

    long n = time / sMonth; 
    time -= n / sMonth; 

    long d = time / sDay; 
    time -= d * sDay;

    long h = time / sHour;
    time -= h * sHour;
    
    long m = time / sMinute;
    time -= m * sMinute;

    double s = ((double) time) / ((double) sSecond); 

    if(interval > sYear) 
      return String.format("%1$dyrs %2$dmons %3$ddays %4$dh %5$dm %6$.1fs", y, n, d, h, m, s); 
    else if(interval > sMonth) 
      return String.format("%1$dmons %2$ddays %3$dh %4$dm %5$.1fs", n, d, h, m, s); 
    else if(interval > sDay) 
      return String.format("%1$ddays %2$dh %3$dm %4$.1fs", d, h, m, s); 
    else if(interval > sHour) 
      return String.format("%1$dh %2$dm %3$.1fs", h, m, s); 
    else if(interval > sMinute)
      return String.format("%1$dm %2$.1fs", m, s); 
    else 
      return String.format("%1$.1fs", s);       
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Convert a timestamp (milliseconds since midnight, January 1, 1970 UTC) into a 
   * relative time expressed in a human friendly string representation. <P> 
   * 
   * The timestamp passed is assumed to be in the past with respect to the current time.
   */ 
  public static String
  formatHumanRelative
  (
   Long stamp
  ) 
  {
    return formatHumanRelative(stamp, System.currentTimeMillis()); 
  }

  /**
   * Convert a timestamp (milliseconds since midnight, January 1, 1970 UTC) into a 
   * relative time expressed in a human friendly string representation. <P> 
   * 
   * The timestamp passed is assumed to be in the past with respect to the given 
   * <CODE>now<CODE> time.
   * 
   * @param now
   *   The timestamp (milliseconds since midnight, January 1, 1970 UTC) of the current time.
   */ 
  public static String
  formatHumanRelative
  (
   Long stamp, 
   long now
  ) 
  {
    if(stamp == null) 
      return "-";

    long delta = now - stamp;
    if(delta < 0) 
      return "in the future!";
    
    long years = delta / sYear; 
    if(years > 1) 
      return (years + " years ago.");

    long months = delta / sMonth; 
    if(months > 1)
      return (months + " months ago."); 

    long days = delta / sDay;
    if(days > 1) 
      return (days + " days ago."); 
    
    long hours = (now - stamp) / sHour;
    if(hours > 1) 
      return (hours + " hours ago."); 

    long mins = (now - stamp) / sMinute;
    if(mins > 1) 
      return (mins + " mins ago."); 

    long secs = (now - stamp) / sSecond;
    return (secs + " sec ago."); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A standardized date formatter.
   */ 
  private static SimpleDateFormat  sFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 

  
  /**
   * Useful time intervals in milliseconds.
   */ 
  private static final long sSecond  = 1000L;
  private static final long sMinute  = 60000L;
  private static final long sHour    = 3600000L;
  private static final long sDay     = 86400000L;
  private static final long sWeek    = 604800000L;
  private static final long sMonth   = 2628000000L;
  private static final long sYear    = 31536000000L;

}



