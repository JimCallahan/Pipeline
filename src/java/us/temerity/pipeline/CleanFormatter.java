// $Id: CleanFormatter.java,v 1.1 2004/02/12 15:50:12 jim Exp $

package us.temerity.pipeline;

import java.util.Date;
import java.util.logging.*;
import java.text.*;

/*------------------------------------------------------------------------------------------*/
/*   C L E A N   F O R M A T T E R                                                          */
/*                                                                                          */
/*    A nicer formatter for logs.                                                           */
/*------------------------------------------------------------------------------------------*/

public
class CleanFormatter
  extends Formatter
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  CleanFormatter()
  {}


  /*----------------------------------------------------------------------------------------*/
  /*   F O R M A T                                                                          */
  /*----------------------------------------------------------------------------------------*/

  public String 
  format
  (
   LogRecord record  /* IN: record to format */ 
  ) 
  {
    SimpleDateFormat fmt = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy");
    String stamp = fmt.format(new Date(record.getMillis()));
    if(record.getLevel().equals(Level.SEVERE)) {
      return ("ERROR: " + stamp + sNL +
	      record.getMessage() + sNL);
    }
    else if(record.getLevel().equals(Level.WARNING)) {
      return ("WARNING: " + stamp + sNL +
	      record.getMessage() + sNL);
    }
    else if(record.getLevel().equals(Level.INFO)) {
      return (record.getMessage() + sNL);
    }
    else  {
      return ("DEBUG [" + record.getLevel().toString().toLowerCase() + "]: " + 
	      record.getMessage() + sNL);
    }
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  protected static String sNL = System.getProperty("line.separator", "\n");

}



