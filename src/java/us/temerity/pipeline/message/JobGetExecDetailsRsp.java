// $Id: JobGetExecDetailsRsp.java,v 1.1 2006/07/03 06:38:42 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   G E T   E X E C   D E T A I L S   R S P                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link JobGetExecDetailsReq JobGetExecDetailsReq} request.
 */
public
class JobGetExecDetailsRsp
  extends TimedRsp
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new response.
   * 
   * @param jobID
   *   The unique job identifier.
   *
   * @param timer 
   *   The timing statistics for a task.
   * 
   * @param details
   *   The job execution details.
   */ 
  public
  JobGetExecDetailsRsp
  (
   long jobID,
   TaskTimer timer, 
   SubProcessExecDetails details
  )
  { 
    super(timer);

    pExecDetails = details; 

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest, 
       "JobMgr.getExecDetails(); " + jobID + "\n  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the job execution details.
   */
  public SubProcessExecDetails
  getExecDetails() 
  {
    return pExecDetails; 
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8875027355988687946L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**j
   * The job execution details.
   */ 
  private SubProcessExecDetails  pExecDetails;

}
  
