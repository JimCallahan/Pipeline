// $Id: QueueMgrControlClient.java,v 1.4 2004/09/03 01:56:23 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;

import java.io.*;
import java.net.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   M G R   C O N T R O L   C L I E N T                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A control connection to the Pipeline queue server daemon. <P> 
 * 
 * This class handles network communication with the Pipeline queue server daemon 
 * <A HREF="../../../../man/plmaster.html"><B>plqueuemgr</B><A>(1).  
 */
public
class QueueMgrControlClient
  extends QueueMgrClient
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new queue manager control client.
   * 
   * @param hostname 
   *   The name of the host running <B>plqueuemgr</B>(1).
   * 
   * @param port 
   *   The network port listened to by <B>plqueuemgr</B>(1).
   */
  public
  QueueMgrControlClient
  ( 
   String hostname, 
   int port
  ) 
  {
    super(hostname, port);
  }

  /** 
   * Construct a new queue manager control client. <P> 
   * 
   * The hostname and port used are those specified by the 
   * <CODE><B>--queue-host</B>=<I>host</I></CODE> and 
   * <CODE><B>--queue-port</B>=<I>num</I></CODE> options to <B>plconfig</B>(1).
   */
  public
  QueueMgrControlClient() 
  {
    super(PackageInfo.sQueueServer, PackageInfo.sQueuePort);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   J O B S                                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the jobs IDs and states for each primary file of the given working version. <P> 
   * 
   * The <CODE>fseq</CODE> argument is required because the frame range of the primary file
   * sequence may have been adjusted after the jobs where submitted. <P> 
   * 
   * If any of the members of the output <CODE>jobIDs</CODE> argument are <CODE>null</CODE>, 
   * then no job exists which regenerates that specific file.  Note that it is possible for 
   * one job to generate multiple files and therefore have its job ID repeated in the returned
   * <CODE>jobIDs</CODE> list. <P> 
   * 
   * Similarly, if any of the members of the output <CODE>states</CODE> argument are 
   * <CODE>null</CODE>, then no job exists which regenerates that specific file. <P> 
   * 
   * @param id
   *   The unique working version identifier.
   *
   * @param fseq
   *   The primary file sequence.
   * 
   * @param jobIDs
   *   An empty list which will be filled with the unique job identifiers of the latest 
   *   job which regenerates each file of the primary file sequence.
   * 
   * @param states
   *   An empty list which will be filled with the JobState of each file of the primary file 
   *   sequence.
   * 
   * @throws PipelineException
   *   If unable to determine the job IDs. 
   */
  public synchronized void 
  getJobStates
  (
   NodeID id, 
   FileSeq fseq, 
   ArrayList<Long> jobIDs, 
   ArrayList<JobState> states 
  ) 
    throws PipelineException  
  {
    verifyConnection();
    
    QueueGetJobStatesReq req = new QueueGetJobStatesReq(id, fseq);
    
    Object obj = performTransaction(QueueRequest.GetJobStates, req);
    if(obj instanceof QueueGetJobStatesRsp) {
      QueueGetJobStatesRsp rsp = (QueueGetJobStatesRsp) obj;
      jobIDs.addAll(rsp.getJobIDs());
      states.addAll(rsp.getStates());
    }
    else {
      handleFailure(obj);
    }        
  }

  /**
   * Submit a job to be executed by the queue. <P> 
   * 
   * @param job
   *   The queue job.
   * 
   * @throws PipelineException
   *   If unable to submit the job.
   */ 
  public synchronized void 
  submitJob
  (
   QueueJob job
  ) 
    throws PipelineException  
  {
    verifyConnection();

    QueueSubmitJobReq req = new QueueSubmitJobReq(job);
    Object obj = performTransaction(QueueRequest.SubmitJob, req); 
    handleSimpleResponse(obj);
  }

}

