// $Id: TextAreaLogHandler.java,v 1.1 2004/04/30 08:40:52 jim Exp $
  
package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.io.*; 
import java.util.*;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.text.*;

/*------------------------------------------------------------------------------------------*/
/*   T E X T   A R E A    L O G   H A N D L E R                                             */
/*------------------------------------------------------------------------------------------*/

/**
 * A {@link Handler Handler} of logging messages which publishes to a 
 * {@link JTextArea JTextArea}.
 */
public
class TextAreaLogHandler
  extends Handler
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new handler.
   * 
   * @param doc
   *   The target document model.
   */ 
  public 
  TextAreaLogHandler
  (
   JTextArea area 
  ) 
  {
    super();

    setFormatter(new LogFormatter());

    if(area == null) 
      throw new IllegalArgumentException("The text area cannot be (null)!");
    pTextArea = area;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   H A N D L E R   O V E R R I D E S                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Publish a LogRecord.
   */ 
  public void 	
  publish
  (
   LogRecord record
  )
  {
    pTextArea.append(getFormatter().format(record) + "\n");      
  }

  /**
   * Flush any buffered output.
   */ 
  public void 	
  flush()
  {}

  /**
   * Close the Handler and free all associated resources.
   */ 
  public void 
  close()
    throws SecurityException
  {}


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The target text area.
   */ 
  private JTextArea  pTextArea;

}
