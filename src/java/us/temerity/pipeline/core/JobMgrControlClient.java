// $Id: JobMgrControlClient.java,v 1.18 2005/12/31 20:42:58 jim Exp $

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

    Object obj = performTransaction(JobRequest.GetResources, null, 15000);
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
   * Get the operating system type.
   * 
   * @throws PipelineException 
   *   If unable to contact the server.
   */ 
  public synchronized OsType
  getOsType() 
    throws PipelineException 
  {
    verifyConnection();

    Object obj = performTransaction(JobRequest.GetOsType, null, 15000);
    if(obj instanceof JobGetOsTypeRsp) {
      JobGetOsTypeRsp rsp = (JobGetOsTypeRsp) obj;
      return rsp.getOsType();
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

    Object obj = performTransaction(JobRequest.GetNumProcessors, null, 15000);
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

    Object obj = performTransaction(JobRequest.GetTotalMemory, null, 15000);
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

    Object obj = performTransaction(JobRequest.GetTotalDisk, null, 15000);
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
   *   If unable to contact the job server. 
   */ 
  public synchronized void
  jobStart
  (
   QueueJob job
  ) 
    throws PipelineException 
  {
    verifyConnection();

    JobStartReq req = new JobStartReq(job);

    Object obj = performTransaction(JobRequest.Start, req, 60000); 
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
  jobKill
  (
   long jobID 
  ) 
    throws PipelineException 
  {
    verifyConnection();
    
    JobKillReq req = new JobKillReq(jobID);
    
    Object obj = performTransaction(JobRequest.Kill, req, 60000); 
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
  jobWait
  (
   long jobID
  ) 
    throws PipelineException 
  {
    verifyConnection();
    
    JobWaitReq req = new JobWaitReq(jobID);
    
    Object obj = performLongTransaction(JobRequest.Wait, req, 15000, 60000);  
    if(obj instanceof JobWaitRsp) {
      JobWaitRsp rsp = (JobWaitRsp) obj;
      return rsp.getResults();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   J O B   M A N A G E M E N T                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Clean up obsolete job resources. <P> 
   * 
   * The <CODE>jobIDs</CODE> argument contains the ID of all jobs which are still being 
   * maintained by the the queue.  Any jobs not on this list are no longer reachable from
   * any job group and therefore all resources associated with the job should be deleted.
   * 
   * @param jobIDs
   *   The IDs of all active jobs.
   * 
   * @throws PipelineException 
   *   If unable to perform the cleanup.
   */ 
  public synchronized void
  cleanupResources
  (
   TreeSet<Long> jobIDs
  ) 
    throws PipelineException 
  {
    verifyConnection();
    
    JobCleanupResourcesReq req = new JobCleanupResourcesReq(jobIDs);
    
    Object obj = performTransaction(JobRequest.CleanupResources, req); 
    handleSimpleResponse(obj);    
  }

  /**
   * Clean up the resources associated with a preempted job. <P> 
   * 
   * @param jobID
   *   The ID of the preempted job. 
   * 
   * @throws PipelineException 
   *   If unable to perform the cleanup.
   */ 
  public synchronized void
  cleanupPreemptedResources
  (
   long jobID
  ) 
    throws PipelineException 
  {
    verifyConnection();
    
    JobCleanupPreemptedResourcesReq req = new JobCleanupPreemptedResourcesReq(jobID);
    
    Object obj = performTransaction(JobRequest.CleanupPreemptedResources, req); 
    handleSimpleResponse(obj);    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S E R V E R   M A N A G E M E N T                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the server is no longer reachable communication should no longer be attempted.<P>
   * 
   * Subclasses which wish to force performLongTransaction() to give up before receiving
   * a response should override this method to return <CODE>true</CODE>.
   */ 
  protected boolean 
  isServerUnreachable() 
  {
    Date stamp = sUnreachableServers.lastUnreachable(pHostname);
    return ((stamp != null) && (pLongTransactionStart != null) && 
	    (pLongTransactionStart.compareTo(stamp) < 0));
  }

  /**
   * Mark a server has being unreachable().
   * 
   * @param hostname
   *   The fully resolved name of the server host.
   */ 
  public static void 
  serverUnreachable
  (
   String hostname
  ) 
  {
    sUnreachableServers.unreachable(hostname);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the error message to be shown when the server cannot be contacted.
   */ 
  protected String
  getServerDownMessage()
  {
    return ("Unable to contact the the pljobmgr(1) daemon running on " +
	    "(" + pHostname + ") using port (" + pPort + ")!");
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static UnreachableServers  sUnreachableServers = new UnreachableServers();

}

