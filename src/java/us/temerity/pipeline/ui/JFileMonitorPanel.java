// $Id: JFileMonitorPanel.java,v 1.4 2009/07/01 16:43:14 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*; 

import java.io.*;
import javax.swing.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   M O N I T O R   P A N E L                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A panel containing a scrollable text area which displays a portion of a file.
 */ 
public 
class JFileMonitorPanel 
  extends JBaseMonitorPanel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new panel.
   * 
   * @param file
   *   The file to monitor.
   */ 
  public 
  JFileMonitorPanel
  (
   File file
  ) 
  {
    super();

    pFileMonitor = new FileMonitor(file);
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the current number of lines which may potentially be viewed.
   * 
   * @return 
   *   The number of lines or <CODE>null</CODE> if unable to determine the number of lines.
   */
  protected Integer
  getNumLines()
  {
    try {
      return pFileMonitor.getNumLines();
    }
    catch(IOException ex) {
      return null;
    }
  }

  /** 
   * Get the current text for the given region of lines. <P> 
   * 
   * @param start
   *   The line number of the first line of text.
   * 
   * @param lines
   *   The number of lines of text to retrieve. 
   * 
   * @return 
   *   The text or <CODE>null</CODE> if unable to retreive the text. 
   */
  protected String
  getLines
  (
   int start, 
   int lines
  )
  {
    try {
      return pFileMonitor.getLines(start, lines);
    }
    catch(IOException ex) {
      return null;
    }
  } 

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 990542431535717346L;

  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The monitored file.
   */
  private FileMonitor  pFileMonitor; 

}
