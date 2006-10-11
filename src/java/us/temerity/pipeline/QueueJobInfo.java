// $Id: QueueJobInfo.java,v 1.14 2006/10/11 22:45:40 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
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

  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */
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

    pState = JobState.Queued;
    pSubmittedStamp = new Date();
  }

  /**
   * Copy constructor. 
   *
   * @param info
   *   The job information to copy. 
   */ 
  public
  QueueJobInfo
  (
   QueueJobInfo info
  ) 
  {
    pJobID = info.pJobID;
    pState = info.pState; 

    pSubmittedStamp = info.pSubmittedStamp;
    pStartedStamp   = info.pStartedStamp;
    pCompletedStamp = info.pCompletedStamp;
    
    pHostname = info.pHostname; 
    pOsType   = info.pOsType;
    pResults  = info.pResults;
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
   * Get the status of the job in the queue.
   */
  public synchronized JobState
  getState() 
  {
    return pState;
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the timestamp of when the job was submitted to the queue.
   */ 
  public synchronized Date 
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
  public synchronized Date 
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
  public synchronized Date 
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
  public synchronized String 
  getHostname() 
  {
    return pHostname;   
  }
  
  /**
   * The operating system type of the host assigned to execute the job.
   * 
   * @return 
   *   The OS or <CODE>null</CODE> if the job was never assigned to a specific host.
   */ 
  public synchronized OsType 
  getOsType() 
  {
    return pOsType;   
  }
  
  /**
   * The results of executing the job's regeneration action.
   * 
   * @return 
   *   The results or <CODE>null</CODE> if the job was never executed.
   */ 
  public synchronized QueueJobResults
  getResults() 
  {
    return pResults; 
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A G E S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Records that the job has been paused.
   */ 
  public synchronized void 
  paused()
  {
    pState = JobState.Paused;
  }

  /**
   * Records that the job has resumed waiting. 
   */ 
  public synchronized void 
  resumed() 
  {
    pState = JobState.Queued;
  }
  
  /**
   * Records the job has started execution. 
   * 
   * @param hostname
   *   The full name of the host executing the job.
   * 
   * @param os
   *   The operating system of the executing host.
   */ 
  public synchronized void 
  started
  (
   String hostname, 
   OsType os
  ) 
  {
    if(hostname == null) 
      throw new IllegalArgumentException
	("The hostname cannot be (null)!");
    pHostname = hostname; 

    if(os == null) 
      throw new IllegalArgumentException
	("The operating system cannot be (null)!");
    pOsType = os; 

    pStartedStamp = new Date();
    pState = JobState.Running;
  }
  
  /**
   * Records that the job was aborted, but then resubmitted. 
   */ 
  public synchronized void 
  preempted() 
  {
    pHostname = null;
    pStartedStamp = null;
    pOsType = null;

    pState = JobState.Preempted;
  }

  /**
   * Records that the job was aborted (cancelled) before it could be assigned to a 
   * host for execution.
   */ 
  public synchronized void 
  aborted() 
  {
    pCompletedStamp = new Date();
    pState = JobState.Aborted;
  }

  /**
   * Records the results of executing the job's regeneration action.
   * 
   * @param results
   *   The execution results.
   */ 
  public synchronized void 
  exited
  (
   QueueJobResults results
  ) 
  {
    pResults = results; 
    
    pCompletedStamp = new Date();
    
    if(pState == JobState.Preempted)
      throw new IllegalStateException(); 

    if((pResults != null) && (pResults.getExitCode() == BaseSubProcess.SUCCESS))
      pState = JobState.Finished;
    else 
      pState = JobState.Failed;
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
      encoder.encode("SubmittedStamp", pSubmittedStamp.getTime());
      
      if(pStartedStamp != null) 
	encoder.encode("StartedStamp", pStartedStamp.getTime());

      if(pCompletedStamp != null) 
	encoder.encode("CompletedStamp", pCompletedStamp.getTime());
    }

    if(pHostname != null)
      encoder.encode("Hostname", pHostname);

    if(pOsType != null)
      encoder.encode("OsType", pOsType);

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
        
    JobState state = (JobState) decoder.decode("State"); 
    if(state == null) 
      throw new GlueException("The \"State\" was missing!");
    pState = state;

    {
      Long stamp = (Long) decoder.decode("SubmittedStamp"); 
      if(stamp == null) 
	throw new GlueException("The \"SubmittedStamp\" was missing!");
      pSubmittedStamp = new Date(stamp);
    }

    {
      Long stamp = (Long) decoder.decode("StartedStamp"); 
      if(stamp != null) 
	pStartedStamp = new Date(stamp);
    }

    {
      Long stamp = (Long) decoder.decode("CompletedStamp"); 
      if(stamp != null) 
	pCompletedStamp = new Date(stamp);
    }

    {
      String host = (String) decoder.decode("Hostname"); 
      if(host != null) 
	pHostname = host;
    }

    {
      OsType os = (OsType) decoder.decode("OsType"); 
      if(os != null) 
	pOsType = os; 
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
   * The status of the job in the queue.
   */
  private JobState  pState;


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
   * The operating system type of the host assigned to execute the job or <CODE>null</CODE> 
   * if the job was never assigned to a specific host.
   */ 
  private OsType pOsType; 

  /**
   * The results of executing the job's regeneration action or <CODE>null</CODE> if the 
   * job was never executed.
   */ 
  private QueueJobResults  pResults;

  
}
