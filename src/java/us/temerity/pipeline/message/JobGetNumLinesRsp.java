// $Id: JobGetNumLinesRsp.java,v 1.2 2005/01/22 01:36:36 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   G E T   N U M   L I N E S   R S P                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link GetNumStdOutLines GetNumStdOutLines} or 
 * {@link GetNumStdErrLines GetNumStdErrLines} request.
 */
public
class JobGetNumLinesRsp
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
   *   The current number of lines of output.
   */ 
  public
  JobGetNumLinesRsp
  (
   String title, 
   TaskTimer timer, 
   int numLines
  )
  { 
    super(timer);

    pNumLines = numLines;

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
   * Gets the current number of lines of output.
   */
  public int
  getNumLines() 
  {
    return pNumLines;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4457961732210500039L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The current number of lines of output.
   */ 
  private int  pNumLines;

}
  
