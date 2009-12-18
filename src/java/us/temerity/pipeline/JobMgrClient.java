// $Id: JobMgrClient.java,v 1.15 2009/12/18 07:20:55 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.message.*;
import us.temerity.pipeline.toolset.*;

import java.io.*;
import java.net.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   M G R   C L I E N T                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A connection to a Pipeline job server daemon. <P> 
 * 
 * This class handles network communication with a Pipeline job server daemon 
 * <A HREF="../../../../man/plmaster.html"><B>pljobmgr</B><A>(1) running on one of the 
 * hosts which are capable of executing jobs for the Pipeline queue.  <P> 
 * 
 * The methods provided by this class are all of an information gathering nature and cannot 
 * affect the behavior of the contacted <B>pljobmgr</B><A>(1) daemon or the Pipeline queue
 * in general.  <P> 
 * 
 * The {@link QueueMgrClient QueueMgrClient} class provides methods for altering the behavior 
 * of the queue. <P> 
 * 
 * The {@link MasterMgrClient MasterMgrClient} class can be used to schedule new jobs (see 
 * {@link MasterMgrClient#submitJobs submitJobs}. 
 */
public
class JobMgrClient
  extends BaseMgrClient
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new job manager client.
   * 
   * @param hostname 
   *   The name of the host running <B>pljobmgr</B>(1).
   */
  public
  JobMgrClient
  ( 
   String hostname
  ) 
  {
    this(hostname, false); 
  }

  /** 
   * Construct a new job manager client.
   * 
   * @param hostname 
   *   The name of the host running <B>pljobmgr</B>(1).
   * 
   * @param forceLongTransactions
   *   Whether to treat all uses of {@link #performTransaction} like 
   *   {@link #performLongTransaction} with an infinite request timeout and a 60-second 
   *   response retry interval with infinite retries.
   */
  public
  JobMgrClient
  ( 
   String hostname,
   boolean forceLongTransactions   
  ) 
  {
    super(hostname, PackageInfo.sJobPort, forceLongTransactions, 
	  JobRequest.Disconnect, null, "JobMgr");
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*  C O N N E C T I O N                                                                   */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Order the server to refuse any further requests and then to exit as soon as all
   * currently pending requests have be completed. <P> 
   * 
   * This method is disabled by JobMgrClient.
   */
  public synchronized void 
  shutdown() 
    throws PipelineException 
  {
    throw new PipelineException
      ("The shutdown request is disabled for JobMgrClient!");
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   E X E C U T I O N   D E T A I L S                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the execution details for a given job.
   * 
   * @param jobID
   *   The unique job identifier. 
   *    
   * @throws PipelineException
   *   If unable to find a job with the given ID.
   */ 
  public synchronized SubProcessExecDetails
  getExecDetails
  (
   long jobID
  ) 
    throws PipelineException  
  {
    verifyConnection();
    
    JobGetExecDetailsReq req = new JobGetExecDetailsReq(jobID);
    Object obj = performTransaction(JobRequest.GetExecDetails, req, 2000);
    if(obj instanceof JobGetExecDetailsRsp) {
      JobGetExecDetailsRsp rsp = (JobGetExecDetailsRsp) obj;
      return rsp.getExecDetails();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }



  /*----------------------------------------------------------------------------------------*/
  /*   J O B   O U T P U T                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current number of lines of STDOUT output from the given job. <P> 
   * 
   * @param jobID
   *   The unique job identifier. 
   *    
   * @throws PipelineException
   *   If unable to find a job with the given ID.
   */ 
  public synchronized int
  getNumStdOutLines
  (
   long jobID
  ) 
    throws PipelineException  
  {
    verifyConnection();
    
    JobGetNumStdOutLinesReq req = new JobGetNumStdOutLinesReq(jobID);
    Object obj = performTransaction(JobRequest.GetNumStdOutLines, req, 3000);
    if(obj instanceof JobGetNumLinesRsp) {
      JobGetNumLinesRsp rsp = (JobGetNumLinesRsp) obj;
      return rsp.getNumLines();
    }
    else {
      handleFailure(obj);
      return 0;
    }        
  }
    
  /**
   * Get the contents of the given region of lines of the STDOUT output from the given job. 
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
  getStdOutLines
  (
   long jobID, 
   int start, 
   int lines
  ) 
    throws PipelineException  
  {
    verifyConnection();

    JobGetStdOutLinesReq req = new JobGetStdOutLinesReq(jobID, start, lines);
    Object obj = performTransaction(JobRequest.GetStdOutLines, req, 3000);
    if(obj instanceof JobOutputRsp) {
      JobOutputRsp rsp = (JobOutputRsp) obj;
      return rsp.getLines();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }

  /**
   * Release any server resources associated with monitoring the STDOUT output of the 
   * given job.
   * 
   * @param jobID
   *   The unique job identifier. 
   * 
   * @throws PipelineException
   *   If unable to find a job with the given ID.
   */
  public synchronized void
  closeStdOut
  (
   long jobID
  ) 
    throws PipelineException  
  {
    verifyConnection();
    
    JobCloseStdOutReq req = new JobCloseStdOutReq(jobID);
    Object obj = performTransaction(JobRequest.CloseStdOut, req, 3000);
    handleSimpleResponse(obj);    
  }
  


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current number of lines of STDERR output from the given job. <P> 
   * 
   * @param jobID
   *   The unique job identifier. 
   * 
   * @throws PipelineException
   *   If unable to find a job with the given ID.
   */ 
  public synchronized int
  getNumStdErrLines
  (
   long jobID
  ) 
    throws PipelineException  
  {
    verifyConnection();
    
    JobGetNumStdErrLinesReq req = new JobGetNumStdErrLinesReq(jobID);
    Object obj = performTransaction(JobRequest.GetNumStdErrLines, req, 3000);
    if(obj instanceof JobGetNumLinesRsp) {
      JobGetNumLinesRsp rsp = (JobGetNumLinesRsp) obj;
      return rsp.getNumLines();
    }
    else {
      handleFailure(obj);
      return 0;
    }        
  }
    
  /**
   * Get the contents of the given region of lines of the STDERR output from the given job. 
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
  getStdErrLines
  (
   long jobID, 
   int start, 
   int lines
  ) 
    throws PipelineException  
  {
    verifyConnection();

    JobGetStdErrLinesReq req = new JobGetStdErrLinesReq(jobID, start, lines);
    Object obj = performTransaction(JobRequest.GetStdErrLines, req, 3000);
    if(obj instanceof JobOutputRsp) {
      JobOutputRsp rsp = (JobOutputRsp) obj;
      return rsp.getLines();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }

  /**
   * Release any server resources associated with monitoring the STDERR output of the 
   * given job.
   * 
   * @param jobID
   *   The unique job identifier. 
   * 
   * @throws PipelineException
   *   If unable to find a job with the given ID.
   */
  public synchronized void
  closeStdErr
  (
   long jobID
  ) 
    throws PipelineException  
  {
    verifyConnection();
    
    JobCloseStdErrReq req = new JobCloseStdErrReq(jobID);
    Object obj = performTransaction(JobRequest.CloseStdErr, req, 3000);
    handleSimpleResponse(obj);    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Handle non-successful responses.
   * 
   * @param obj
   *   The response from the server.
   */ 
  protected void 
  handleFailure
  ( 
   Object obj
  )
    throws PipelineException
  {
    if(obj instanceof FailureRsp) {
      FailureRsp rsp = (FailureRsp) obj;
      throw new PipelineException
        ("The pljobmgr(1) daemon running on (" + pHostname + ") reports:\n\n" + 
         rsp.getMessage());	
    }
    else {
      disconnect();
      throw new PipelineException
	("Illegal response received from the pljobmgr(1) running on (" + pHostname + ")!");
    }
  }

  /**
   * Get the error message to be shown when the server cannot be contacted.
   */ 
  protected String
  getServerDownMessage()
  {
    return ("Unable to contact the the pljobmgr(1) daemon running on " +
	    "(" + pHostname + ") using port (" + pPort + ")!");
  }

}

