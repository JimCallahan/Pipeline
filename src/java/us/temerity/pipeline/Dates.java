// $Id: Dates.java,v 1.1 2004/07/14 20:58:04 jim Exp $

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


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A standardized date formatter.
   */ 
  private static SimpleDateFormat  sFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 

}



