// $Id: JobOutputRsp.java,v 1.1 2004/07/28 19:10:23 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   O U T P U T   R S P                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link JobGetStdOutLinesReq JobGetStdOutLinesReq} or 
 * {@link JobGetStdErrLinesReq JobGetStdErrLinesReq} request.
 */
public
class JobOutputRsp
  extends TimedRsp
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new response.
   * 
   * @param title
   *   Log message title.
   * 
   * @param timer 
   *   The timing statistics for a task.
   * 
   * @param lines
   *   The lines of output.
   */ 
  public
  JobOutputRsp
  (
   String title, 
   TaskTimer timer, 
   String[] lines
  )
  { 
    super(timer);

    if(lines == null) 
      throw new IllegalArgumentException("The output lines cannot be (null)!");
    pLines = lines;

    Logs.net.finest(title + "\n  " + getTimer());
    if(Logs.net.isLoggable(Level.FINEST))
      Logs.flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the output lines.
   */
  public String[]
  getLines() 
  {
    return pLines;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5945183411207800623L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The output lines.
   */ 
  private String[]  pLines;

}
  
