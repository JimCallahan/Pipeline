// $Id: JMonitorJobStdOutDialog.java,v 1.1 2004/09/05 06:54:56 jim Exp $

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
   * Get the current collected lines of captured output from the job server for the given 
   * job starting at the given line. 
   * 
   * @param client
   *   The job manager connection.
   * 
   * @param jobID
   *   The unique job identifier. 
   * 
   * @param start 
   *   The index of the first line of output to return.  
   */
  protected String[]
  getOutputLines
  (
   JobMgrClient client,
   long jobID, 
   int start   
  )
    throws PipelineException
  {
    return client.getStdOutLines(jobID, start);
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3288002428601097722L;

}
