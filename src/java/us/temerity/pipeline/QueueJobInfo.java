// $Id: QueueJobInfo.java,v 1.2 2004/07/28 19:13:57 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.util.logging.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   J O B  I N F O                                                             */
/*------------------------------------------------------------------------------------------*/

/**
 * Information about the current status of a QueueJob in the Pipeline queue.
 */
public
class QueueJobInfo
  implements Glueable, Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  QueueJobInfo()
  {}

  /**
   * Construct a new job information.
   * 
   * @param jobID
   *   The unique job identifier.
   */ 
  public
  QueueJobInfo
  (
   long jobID
  ) 
  {
    if(jobID < 0) 
      throw new IllegalArgumentException
	("The job ID (" + jobID + ") must be positive!");
    pJobID = jobID;

    pState = QueueState.Queued;
    pSubmittedStamp = Dates.now();
  }

  
   
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the unique job identifier.
   */ 
  public long 
  getJobID() 
  {
    return pJobID;
  }

  /**
   * Get the queue state of the job.
   */
  public QueueState
  getState() 
  {
    return pState;
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the timestamp of when the job was submitted to the queue.
   */ 
  public Date 
  getSubmittedStamp() 
  {
    return pSubmittedStamp;
  }
     
  /**
   * Get the timestamp of when the job was started to a host for execution.
   * 
   * @return 
   *   The timestamp or <CODE>null</CODE> if the job was never started.
   */ 
  public Date 
  getStartedStamp() 
  {
    return pStartedStamp;
  }
     
  /**
   * Get the timestamp of when the job completed. 
   * 
   * @return 
   *   The timestamp or <CODE>null</CODE> if the job has not completed yet.
   */ 
  public Date 
  getCompletedStamp() 
  {
    return pCompletedStamp;
  }
  

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The full name of the host assigned to execute the job.
   * 
   * @return 
   *   The hostname or <CODE>null</CODE> if the job was never assigned to a specific host.
   */ 
  public String 
  getHostname() 
  {
    return pHostname;   
  }
  
  /**
   * The results of executing the job's regeneration action.
   * 
   * @return 
   *   The results or <CODE>null</CODE> if the job was never executed.
   */ 
  public QueueJobResults
  getResults() 
  {
    return pResults; 
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A G E S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Records the job has started execution. 
   * 
   * @param hostname
   *   The full name of the host executing the job.
   */ 
  public void 
  started
  (
   String hostname
  ) 
  {
    if(hostname == null) 
      throw new IllegalArgumentException
	("The hostname cannot be (null)!");
    pHostname = hostname; 

    pStartedStamp = Dates.now();
    pState = QueueState.Running;
  }
  
  /**
   * Records that the job was aborted (cancelled) before it could be assigned to a 
   * host for execution.
   */ 
  public void 
  aborted() 
  {
    pCompletedStamp = Dates.now();
    pState = QueueState.Aborted;
  }

  /**
   * Records the results of executing the job's regeneration action.
   * 
   * @param results
   *   The execution results.
   */ 
  public void 
  exited
  (
   QueueJobResults results
  ) 
  {
    pResults = results; 

    pCompletedStamp = Dates.now();

    if(pResults == null) 
      pState = QueueState.Aborted;
    else if(pResults.getExitCode() == SubProcess.SUCCESS)
      pState = QueueState.Finished;
    else 
      pState = QueueState.Failed;
  }




  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public void 
  toGlue
  ( 
   GlueEncoder encoder  
  ) 
    throws GlueException
  {
    encoder.encode("JobID", pJobID);
    encoder.encode("State", pState);

    {
      encoder.encode("SubmittedStamp", pSubmittedStamp);
      
      if(pStartedStamp != null) 
	encoder.encode("StartedStamp", pStartedStamp);

      if(pCompletedStamp != null) 
	encoder.encode("CompletedStamp", pCompletedStamp);
    }
    
    if(pResults != null) 
      encoder.encode("Results", pResults);
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    Long jobID = (Long) decoder.decode("JobID"); 
    if(jobID == null) 
      throw new GlueException("The \"JobID\" was missing!");
    pJobID = jobID;
        
    QueueState state = (QueueState) decoder.decode("State"); 
    if(state == null) 
      throw new GlueException("The \"State\" was missing!");
    pState = state;

    {
      Date date = (Date) decoder.decode("SubmittedStamp"); 
      if(date == null) 
	throw new GlueException("The \"SubmittedStamp\" was missing!");
      pSubmittedStamp = date;
    }

    {
      Date date = (Date) decoder.decode("StartedStamp"); 
      if(date != null) 
	pStartedStamp = date;
    }

    {
      Date date = (Date) decoder.decode("CompletedStamp"); 
      if(date != null) 
	pCompletedStamp = date;
    }

    {
      String host = (String) decoder.decode("Hostname"); 
      if(host != null) 
	pHostname = host;
    }

    {
      QueueJobResults results = (QueueJobResults) decoder.decode("Results"); 
      if(results != null) 
	pResults = results;
    }
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4135404054158504196L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique job identifier.
   */ 
  private long  pJobID;

  /**
   * The queue status of the job. 
   */
  private QueueState  pState;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The timestamp of when the job was submitted to the queue.
   */ 
  private Date  pSubmittedStamp;

  /**
   * The timestamp of when the job was started to a host for execution.
   */ 
  private Date  pStartedStamp;

  /**
   * The timestamp of when the job completed. <P> 
   * 
   * Completion may be due to the job being aborted or killed in addition to normal exit
   * of the action process.
   */ 
  private Date  pCompletedStamp;

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * The full name of the host assigned to execute the job or <CODE>null</CODE> if the 
   * job was never assigned to a specific host.
   */ 
  private String pHostname;   

  /**
   * The results of executing the job's regeneration action or <CODE>null</CODE> if the 
   * job was never executed.
   */ 
  private QueueJobResults  pResults;

  
}
