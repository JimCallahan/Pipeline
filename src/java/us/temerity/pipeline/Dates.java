// $Id: Dates.java,v 1.2 2004/10/25 17:46:19 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.text.*;

/*------------------------------------------------------------------------------------------*/
/*   D A T E S                                                                              */
/*------------------------------------------------------------------------------------------*/

/**                                                                                   
 * A collection of static utility methods related to dates and timestamps.
 */
public 
class Dates      
{  
  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current timestamp rounded to the nearest second.
   */ 
  public static Date
  now()
  {
    Date stamp = new Date();
    long secs = stamp.getTime() / 1000L;
    return (new Date(secs * 1000L));
  } 

  /**
   * Convert a timestamp to a standardized string representation.
   */ 
  public static String
  format
  (
   Date date
  ) 
  {
    return sFormat.format(date);
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



