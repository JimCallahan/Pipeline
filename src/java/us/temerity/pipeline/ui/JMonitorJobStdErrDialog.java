// $Id: JMonitorJobStdErrDialog.java,v 1.1 2004/09/05 06:54:56 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*; 

import java.util.*;
import javax.swing.*;

/*------------------------------------------------------------------------------------------*/
/*   M O N I T O R   J O B   S T D E R R   D I A L O G                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Monitor the STDERR output from a running job.
 */ 
public 
class JMonitorJobStdErrDialog
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
  JMonitorJobStdErrDialog
  (
   QueueJob job, 
   QueueJobInfo info
  ) 
  {
    super("Job Errors", "Errors", "Job Output:  STDERR", job, info);
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
    return client.getStdErrLines(jobID, start);
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4766003715486390179L;

}
