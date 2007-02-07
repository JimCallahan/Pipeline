// $Id: JobEditAsFailedRsp.java,v 1.1 2007/02/07 21:15:14 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   E D I T   A S   F A I L E D   R S P                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The details of how an EditAs request failed.
 */ 
public
class JobEditAsFailedRsp
  extends TimedRsp
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new response.
   *
   * @param timer 
   *   The timing statistics for a task.
   * 
   * @param proc
   *   The failed process. 
   */ 
  public
  JobEditAsFailedRsp
  (
   TaskTimer timer, 
   SubProcessLight proc 
  )
  { 
    super(timer);

    if(proc == null)
      throw new IllegalArgumentException("The operating system type cannoy be (null)!");
    pResults = new Object[4]; 
    pResults[0] = proc.getExitCode(); 
    pResults[1] = proc.getCommand(); 
    pResults[2] = proc.getStdOut(); 
    pResults[3] = proc.getStdErr();

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest, 
       getTimer().toString());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the process execution results: [exit-code, command-line, stdout, stderr]
   */
  public Object[]
  getResults() 
  {
    return pResults;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8485563918573844706L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The process execution results: [exit-code, command-line, stdout, stderr]
   */ 
  private Object[]  pResults; 

}
  
