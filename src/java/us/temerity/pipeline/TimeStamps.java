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
    return ((System.currentTimeMillis() / 1000L) * 1000L);
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
   * standardized string representation.
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

  /**
   * Convert a time interval into a standardized string representation.
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

    long h = time / 3600000L;
    time -= h * 3600000L;
    
    long m = time / 60000L;
    time -= m * 60000L;

    double s = ((double) time) / 1000.0;
    
    if(interval > 3600000L) 
      return String.format("%1$dh %2$dm %3$.1fs", h, m, s); 
    else if(interval > 60000L)
      return String.format("%1$dm %2$.1fs", m, s); 
    else 
      return String.format("%1$.1fs", s);       
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A standardized date formatter.
   */ 
  private static SimpleDateFormat  sFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 

}



