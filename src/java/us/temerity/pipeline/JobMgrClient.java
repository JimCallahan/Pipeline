// $Id: JobMgrClient.java,v 1.4 2004/09/05 06:40:54 jim Exp $

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
   * 
   * @param port 
   *   The network port listened to by <B>pljobmgr</B>(1).
   */
  public
  JobMgrClient
  ( 
   String hostname, 
   int port
  ) 
  {
    super(hostname, port, 
	  JobRequest.Disconnect, null);
  }

  /** 
   * Construct a new job manager client. <P> 
   * 
   * The port used is specified by the <CODE><B>--job-port</B>=<I>num</I></CODE> option 
   * to <B>plconfig</B>(1).
   */
  public
  JobMgrClient
  (
   String hostname
  ) 
  {
    super(hostname, PackageInfo.sJobPort, 
	  JobRequest.Disconnect, null);
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
  /*   J O B   O U T P U T                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get current collected lines of captured STDOUT output from the given job starting 
   * at the given line. <P>
   * 
   * By keeping a count of the lines previously retrieved and using this count as the 
   * <CODE>start</CODE> argument, output can be incrementally retrieved from a running 
   * job. If no new output is available since the last call, the returned array will 
   * have zero length.  <P> 
   * 
   * If the last line of returned output is <CODE>null</CODE>, then the job has completed
   * and all output has been retrieved. <P> 
   * 
   * @param jobID
   *   The unique job identifier. 
   * 
   * @param start 
   *   The index of the first line of output to return.  
   * 
   * @throws PipelineException
   *   If unable to find a job with the given ID.
   */
  public synchronized String[] 
  getStdOutLines
  (
   long jobID, 
   int start
  ) 
    throws PipelineException  
  {
    verifyConnection();

    JobGetStdOutLinesReq req = new JobGetStdOutLinesReq(jobID, start);
    Object obj = performTransaction(JobRequest.GetStdOutLines, req);
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
   * Gets the current collected STDOUT output from the given job as a single 
   * <CODE>String</CODE>.  <P>
   * 
   * @param jobID
   *   The unique job identifier. 
   * 
   * @throws PipelineException
   *   If unable to find a job with the given ID.
   */
  public String
  getStdOut
  (
   long jobID
  ) 
    throws PipelineException      
  {
    verifyConnection();

    JobGetStdOutLinesReq req = new JobGetStdOutLinesReq(jobID, 0);
    Object obj = performTransaction(JobRequest.GetStdOutLines, req);
    if(obj instanceof JobOutputRsp) {
      JobOutputRsp rsp = (JobOutputRsp) obj;
      String lines[] = rsp.getLines();

      StringBuffer buf = new StringBuffer();
      int wk;
      for(wk=0; wk<lines.length; wk++) 
	if(lines[wk] != null) 
	  buf.append(lines[wk] + "\n");

      return buf.toString();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get current collected lines of captured STDERR output from the given job starting 
   * at the given line. <P>
   * 
   * By keeping a count of the lines previously retrieved and using this count as the 
   * <CODE>start</CODE> argument, output can be incrementally retrieved from a running 
   * job. If no new output is available since the last call, the returned array will 
   * have zero length.  <P> 
   * 
   * If the last line of returned output is <CODE>null</CODE>, then the job has completed
   * and all output has been retrieved. <P> 
   * 
   * @param jobID
   *   The unique job identifier. 
   * 
   * @param start 
   *   The index of the first line of output to return.  
   * 
   * @throws PipelineException
   *   If unable to find a job with the given ID.
   */
  public synchronized String[] 
  getStdErrLines
  (
   long jobID, 
   int start
  ) 
    throws PipelineException  
  {
    verifyConnection();

    JobGetStdErrLinesReq req = new JobGetStdErrLinesReq(jobID, start);
    Object obj = performTransaction(JobRequest.GetStdErrLines, req);
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
   * Gets the current collected STDERR output from the given job a single 
   * <CODE>String</CODE>.  <P>
   * 
   * @param jobID
   *   The unique job identifier. 
   * 
   * @throws PipelineException
   *   If unable to find a job with the given ID.
   */
  public String
  getStdErr
  (
   long jobID
  ) 
    throws PipelineException  
  {
    verifyConnection();

    JobGetStdErrLinesReq req = new JobGetStdErrLinesReq(jobID, 0);
    Object obj = performTransaction(JobRequest.GetStdErrLines, req);
    if(obj instanceof JobOutputRsp) {
      JobOutputRsp rsp = (JobOutputRsp) obj;
      String lines[] = rsp.getLines();

      StringBuffer buf = new StringBuffer();
      int wk;
      for(wk=0; wk<lines.length; wk++) 
	if(lines[wk] != null) 
	  buf.append(lines[wk] + "\n");

      return buf.toString();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }

}

