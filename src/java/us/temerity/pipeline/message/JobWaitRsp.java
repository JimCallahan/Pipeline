// $Id: JobWaitRsp.java,v 1.2 2004/08/23 03:07:10 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   W A I T   R S P                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link JobWaitReq JobWaitReq} request.
 */ 
public
class JobWaitRsp
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
   * @param results
   *   The execution results or <CODE>null</CODE> if the job was never executed.
   * 
   * @param jobs
   *   The number of currently running jobs.
   */ 
  public
  JobWaitRsp
  (
   long jobID,
   TaskTimer timer, 
   QueueJobResults results, 
   int jobs
  )
  { 
    super(timer);

    pResults = results; 
    pNumJobs = jobs;

    Logs.net.finest("JobMgr.wait(): " + jobID + "\n  " + getTimer());
    if(Logs.net.isLoggable(Level.FINEST))
      Logs.flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the execution results.
   */
  public QueueJobResults
  getResults()
  {
    return pResults;
  }
  
  /**
   * Get the number of jobs running on the host.
   */ 
  public int
  getNumJobs() 
  {
    return pNumJobs; 
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7864068873410009351L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The the execution results.
   */ 
  private QueueJobResults  pResults; 

  /**
   * The number of currently running jobs.
   */ 
  private int  pNumJobs; 
}
  
