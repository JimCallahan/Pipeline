// $Id: JMonitorJobStdOutDialog.java,v 1.2 2004/10/28 15:55:24 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*; 

import java.util.*;
import javax.swing.*;

/*------------------------------------------------------------------------------------------*/
/*   M O N I T O R   J O B   S T D O U T   D I A L O G                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Monitor the STDOUT output from a running job.
 */ 
public 
class JMonitorJobStdOutDialog
  extends JBaseMonitorJobOutputDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   * 
   * @param job
   *   The queue job.
   * 
   * @param info
   *   The current job status information. 
   */ 
  public 
  JMonitorJobStdOutDialog
  (
   QueueJob job, 
   QueueJobInfo info
  ) 
  {
    super("Job Output", "Output", "Job Output:  STDOUT", job, info);
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current number of lines of STDOUT output from the given job. <P> 
   * 
   * @param client
   *   The job manager connection.
   * 
   * @param jobID
   *   The unique job identifier. 
   *    
   * @throws PipelineException
   *   If unable to find a job with the given ID.
   */ 
  public int
  getNumLinesMonitor
  (
   JobMgrClient client,
   long jobID
  ) 
    throws PipelineException  
  {
    return client.getNumStdOutLines(jobID);
  }
  
  
  /**
   * Get the contents of the given region of lines of the STDOUT output from the given job. 
   * 
   * @param client
   *   The job manager connection.
   * 
   * @param jobID
   *   The unique job identifier. 
   * 
   * @param start
   *   The line number of the first line of text.
   * 
   * @param lines
   *   The number of lines of text to retrieve. 
   * 
   * @throws PipelineException
   *   If unable to find a job with the given ID.
   */
  public synchronized String
  getLinesMonitor
  (
   JobMgrClient client,
   long jobID, 
   int start, 
   int lines
  ) 
    throws PipelineException  
  {
    return client.getStdOutLines(jobID, start, lines);
  }

  /**
   * Release any server resources associated with monitoring the STDOUT output of the 
   * given job.
   * 
   * @param client
   *   The job manager connection.
   * 
   * @param jobID
   *   The unique job identifier. 
   * 
   * @throws PipelineException
   *   If unable to find a job with the given ID.
   */
  public void
  closeMonitor
  (
   JobMgrClient client,
   long jobID
  ) 
    throws PipelineException  
  {
    client.closeStdOut(jobID);
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3288002428601097722L;

}
