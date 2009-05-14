// $Id: QueueJobInfo.java,v 1.22 2009/05/14 23:30:43 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.util.regex.*; 
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
    pSubmittedStamp = System.currentTimeMillis();
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
    if(pHostname != null) 
      initShortHostname();

    pOsType   = info.pOsType;
    pResults  = info.pResults;
  }

  /**
   * Initialize the short hostname from the fully resolved hostname.
   */
  private synchronized void
  initShortHostname() 
  {
    Matcher n = sNumericHostPattern.matcher(pHostname);
    Matcher s = sShortHostPattern.matcher(pHostname);
    if(!n.matches() && s.find() && (s.group().length() > 0))
      pShortHostname = s.group();
    else 
      pShortHostname = pHostname;
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
  public synchronized long 
  getSubmittedStamp() 
  {
    return pSubmittedStamp;
  }
     
  /**
   * Get the timestamp (milliseconds since midnight, January 1, 1970 UTC) of when the job 
   * was started to a host for execution.
   * 
   * @return 
   *   The timestamp or <CODE>null</CODE> if the job was never started.
   */ 
  public synchronized Long
  getStartedStamp() 
  {
    return pStartedStamp;
  }
     
  /**
   * Get the timestamp (milliseconds since midnight, January 1, 1970 UTC) of when the job 
   * completed. 
   * 
   * @return 
   *   The timestamp or <CODE>null</CODE> if the job has not completed yet.
   */ 
  public synchronized Long
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
   * Get the short hostname (without domain name suffix) of the host assigned to execute 
   * the job.
   * 
   * @return 
   *   The hostname or <CODE>null</CODE> if the job was never assigned to a specific host.
   */ 
  public synchronized String
  getShortHostname() 
  {
    return pShortHostname;
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
   * 
   * @return
   *   The previous job state.
   */ 
  public synchronized JobState 
  paused()
  {
    JobState prev = pState;
    pState = JobState.Paused;
    return prev;
  }

  /**
   * Records that the job has resumed waiting. 
   * 
   * @return
   *   The previous job state.
   */ 
  public synchronized JobState
  resumed() 
  {
    JobState prev = pState;
    pState = JobState.Queued;
    return prev;
  }
  
  /**
   * Records the job has started execution. 
   * 
   * @param hostname
   *   The full name of the host executing the job.
   * 
   * @param os
   *   The operating system of the executing host.
   * 
   * @return
   *   The previous job state.
   */ 
  public synchronized JobState 
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
    initShortHostname();

    if(os == null) 
      throw new IllegalArgumentException
	("The operating system cannot be (null)!");
    pOsType = os; 

    pStartedStamp = System.currentTimeMillis();

    JobState prev = pState;
    pState = JobState.Running;
    return prev;
  }
  
  /**
   * Records that the job was aborted, but then resubmitted. 
   * 
   * @return
   *   The previous job state.
   */ 
  public synchronized JobState
  preempted() 
  {
    pHostname      = null;
    pShortHostname = null;
    pStartedStamp  = null;
    pOsType        = null;

    JobState prev = pState;
    pState = JobState.Preempted;
    return prev;
  }

  /**
   * Records that the job was aborted (cancelled) before it could be assigned to a 
   * host for execution.
   * 
   * @return
   *   The previous job state.
   */ 
  public synchronized JobState
  aborted() 
  {
    pCompletedStamp = System.currentTimeMillis();

    JobState prev = pState;
    pState = JobState.Aborted;
    return prev;
  }

  /**
   * Records the results of executing the job's regeneration action.
   * 
   * @param results
   *   The execution results.
   * 
   * @return
   *   The previous job state.
   */ 
  public synchronized JobState
  exited
  (
   QueueJobResults results
  ) 
  {
    pResults = results; 
    
    pCompletedStamp = System.currentTimeMillis();
    
    if(pState == JobState.Preempted)
      throw new IllegalStateException(); 

    JobState prev = pState;
    if((pResults != null) && (pResults.getExitCode() == BaseSubProcess.SUCCESS))
      pState = JobState.Finished;
    else 
      pState = JobState.Failed;
    return prev;
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
      pSubmittedStamp = stamp; 
    }

    {
      Long stamp = (Long) decoder.decode("StartedStamp"); 
      if(stamp != null) 
	pStartedStamp = stamp; 
    }

    {
      Long stamp = (Long) decoder.decode("CompletedStamp"); 
      if(stamp != null) 
	pCompletedStamp = stamp; 
    }

    {
      String host = (String) decoder.decode("Hostname"); 
      if(host != null) {
	pHostname = host;
        initShortHostname();
      }
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

  /**
   * A regular expressions used to determine if a hostname is numeric and to match the 
   * first component (short name) of a non-numeric hostname.
   */ 
  private static final Pattern sNumericHostPattern = 
    Pattern.compile("([0-9])+\\.([0-9])+\\.([0-9])+\\.([0-9])+");

  private static final Pattern sShortHostPattern = 
    Pattern.compile("([^\\.])+");



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
   * The timestamp (milliseconds since midnight, January 1, 1970 UTC) of when the job was 
   * submitted to the queue.
   */ 
  private long  pSubmittedStamp;

  /**
   * The timestamp (milliseconds since midnight, January 1, 1970 UTC) of when the job was 
   * started to a host for execution or <CODE>null</CODE> if not started yet.
   */ 
  private Long  pStartedStamp;

  /**
   * The timestamp (milliseconds since midnight, January 1, 1970 UTC) of when the job 
   * completed or <CODE>null</CODE> if not completed yet.
   * 
   * Completion may be due to the job being aborted or killed in addition to normal exit
   * of the action process.
   */ 
  private Long  pCompletedStamp;

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * The full name of the host assigned to execute the job or <CODE>null</CODE> if the 
   * job was never assigned to a specific host.
   */ 
  private String pHostname;   

  /**
   * The short hostname (without domain name suffix) of the host assigned to execute 
   * the job or <CODE>null</CODE> if the job was never assigned to a specific host.
   */ 
  private String pShortHostname;

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
