// $Id: JobOutputRsp.java,v 1.4 2005/01/22 06:10:09 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

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
   *   The requested lines of output.
   */ 
  public
  JobOutputRsp
  (
   String title, 
   TaskTimer timer, 
   String lines
  )
  { 
    super(timer);

    if(lines == null) 
      throw new IllegalArgumentException("The output lines cannot be (null)!");
    pLines = lines;

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       title + "\n  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets requested lines of output.
   */
  public String
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
   * The requested lines of output.
   */ 
  private String  pLines;

}
  
