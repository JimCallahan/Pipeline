// $Id: JobMgrControlClient.java,v 1.1 2004/07/28 19:22:50 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;

import java.io.*;
import java.net.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   M G R   C O N T R O L   C L I E N T                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A control connection to a Pipeline job server daemon. <P> 
 * 
 * This class handles network communication with a Pipeline job server daemon 
 * <A HREF="../../../../man/plmaster.html"><B>pljobmgr</B><A>(1) running on one of the 
 * hosts which are capable of executing jobs for the Pipeline queue.  <P> 
 */
public
class JobMgrControlClient
  extends BaseMgrClient
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new job manager control client.
   * 
   * @param hostname 
   *   The name of the host running <B>pljobmgr</B>(1).
   * 
   * @param port 
   *   The network port listened to by <B>pljobmgr</B>(1).
   */
  public
  JobMgrControlClient
  ( 
   String hostname, 
   int port
  ) 
  {
    super(hostname, port, 
	  JobRequest.Disconnect, JobRequest.Shutdown);
  }

  /** 
   * Construct a new job manager control client. <P> 
   * 
   * The port used is specified by the <CODE><B>--job-port</B>=<I>num</I></CODE> option 
   * to <B>plconfig</B>(1).
   */
  public
  JobMgrControlClient
  (
   String hostname
  ) 
  {
    super(hostname, PackageInfo.sJobPort, 
	  JobRequest.Disconnect, JobRequest.Shutdown);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H O S T   R E S O U R C E S                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get a point sample of the currently available system resources.
   * 
   * @throws PipelineException 
   *   If unable to contact the server.
   */ 
  public synchronized ResourceSample
  getResources() 
    throws PipelineException 
  {
    verifyConnection();

    Object obj = performTransaction(JobRequest.GetResources, null);
    if(obj instanceof JobGetResourcesRsp) {
      JobGetResourcesRsp rsp = (JobGetResourcesRsp) obj;
      return rsp.getSample();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }

  /**
   * Get the number of processors (CPUs).
   * 
   * @throws PipelineException 
   *   If unable to contact the server.
   */ 
  public synchronized int
  getNumProcessors() 
    throws PipelineException 
  {
    verifyConnection();

    Object obj = performTransaction(JobRequest.GetNumProcessors, null);
    if(obj instanceof JobGetNumProcessorsRsp) {
      JobGetNumProcessorsRsp rsp = (JobGetNumProcessorsRsp) obj;
      return rsp.getProcessors();
    }
    else {
      handleFailure(obj);
      return 0;
    }        
  }

  /**
   * Get the total amount of system memory (in bytes).
   * 
   * @throws PipelineException 
   *   If unable to contact the server.
   */ 
  public synchronized long
  getTotalMemory() 
    throws PipelineException 
  {
    verifyConnection();

    Object obj = performTransaction(JobRequest.GetTotalMemory, null);
    if(obj instanceof JobGetTotalMemoryRsp) {
      JobGetTotalMemoryRsp rsp = (JobGetTotalMemoryRsp) obj;
      return rsp.getMemory();
    }
    else {
      handleFailure(obj);
      return 0;
    }        
  }

  /**
   * Get the size of the temporary disk drive (in bytes).
   * 
   * @throws PipelineException 
   *   If unable to contact the server.
   */ 
  public synchronized long
  getTotalDisk() 
    throws PipelineException 
  {
    verifyConnection();

    Object obj = performTransaction(JobRequest.GetTotalDisk, null);
    if(obj instanceof JobGetTotalDiskRsp) {
      JobGetTotalDiskRsp rsp = (JobGetTotalDiskRsp) obj;
      return rsp.getDisk();
    }
    else {
      handleFailure(obj);
      return 0;
    }        
  }



  /*----------------------------------------------------------------------------------------*/
  /*   J O B   E X E C U T I O N                                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Starts execution of the job on the server.
   * 
   * @param job 
   *   The job to execute.
   * 
   * @throws PipelineException 
   *   If unable to start the job.
   */ 
  public synchronized void 
  start
  (
   QueueJob job
  ) 
    throws PipelineException 
  {
    verifyConnection();

    JobStartReq req = new JobStartReq(job);

    Object obj = performTransaction(JobRequest.Start, req); 
    handleSimpleResponse(obj);
  }

  /**
   * Kill the job with the given ID running on the server.
   * 
   * @param jobID
   *   The unique job identifier.
   * 
   * @throws PipelineException 
   *   If unable to kill the job.
   */ 
  public synchronized void
  kill
  (
   int jobID 
  ) 
    throws PipelineException 
  {
    verifyConnection();
    
    JobKillReq req = new JobKillReq(jobID);
    
    Object obj = performTransaction(JobRequest.Kill, req); 
    handleSimpleResponse(obj);
  }
  
  /**
   * Wait for a job with the given ID to complete and return the results of the execution.
   * 
   * @param jobID
   *   The unique job identifier.
   * 
   * @throws PipelineException 
   *   If unable to determine the results.
   */ 
  public synchronized QueueJobResults
  wait
  (
   int jobID 
  ) 
    throws PipelineException 
  {
    verifyConnection();
    
    JobWaitReq req = new JobWaitReq(jobID);
    
    Object obj = performTransaction(JobRequest.Wait, req); 
    if(obj instanceof JobWaitRsp) {
      JobWaitRsp rsp = (JobWaitRsp) obj;
      return rsp.getResults();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }

}

