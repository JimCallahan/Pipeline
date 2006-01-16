// $Id: QueueMgrControlClient.java,v 1.15 2006/01/16 04:11:12 jim Exp $

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
class QueueMgrControlClient
  extends QueueMgrClient
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new queue manager control client.
   */
  public
  QueueMgrControlClient() 
  {
    super();
  }



  /*----------------------------------------------------------------------------------------*/
  /*  C O N N E C T I O N                                                                   */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Order the server to refuse any further requests and then to exit as soon as all
   * currently pending requests have be completed.
   * 
   * @param shutdownJobMgrs
   *   Whether to shutdown all job servers before exiting.
   */
  public synchronized void 
  shutdown
  (
   boolean shutdownJobMgrs
  ) 
    throws PipelineException 
  {
    verifyConnection();

    QueueShutdownOptionsReq req = new QueueShutdownOptionsReq(shutdownJobMgrs);
    shutdownTransaction(QueueRequest.ShutdownOptions, req); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A D M I N I S T R A T I V E   P R I V I L E G E S                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the work groups and administrative privileges from the MasterMgr.
   * 
   * @param privs
   *   The privileges. 
   * 
   * @throws PipelineException
   *   If unable to update the privileges.
   */ 
  public synchronized void 
  updateAdminPrivileges
  (
   AdminPrivileges privs
  ) 
    throws PipelineException 
  {
    verifyConnection();

    MiscUpdateAdminPrivilegesReq req = privs.getUpdateRequest();
    Object obj = performTransaction(QueueRequest.UpdateAdminPrivileges, req); 
    handleSimpleResponse(obj);
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
   * @param nodeID
   *   The unique working version identifier.
   *
   * @param stamp
   *   The timestamp of when the working version was created.
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
   NodeID nodeID, 
   Date stamp, 
   FileSeq fseq, 
   ArrayList<Long> jobIDs, 
   ArrayList<JobState> states 
  ) 
    throws PipelineException  
  {
    verifyConnection();
    
    QueueGetJobStatesReq req = new QueueGetJobStatesReq(nodeID, stamp, fseq);
    
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
   * Get the job IDs of unfinished jobs which will regenerate the files of the given 
   * working nodes.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param fseqs
   *   The primary file sequences indexed by fully resolved names of the nodes.
   * 
   * @return 
   *   The unfinished job IDs indexed by node name.
   */ 
  public synchronized TreeMap<String,TreeSet<Long>>
  getUnfinishedJobsForNodes
  (
   String author, 
   String view, 
   TreeMap<String,FileSeq> fseqs
  )
    throws PipelineException  
  {
    verifyConnection();
    
    QueueGetUnfinishedJobsForNodesReq req = 
      new QueueGetUnfinishedJobsForNodesReq(author, view, fseqs); 
    
    Object obj = performTransaction(QueueRequest.GetUnfinishedJobsForNodes, req);
    if(obj instanceof GetUnfinishedJobsForNodesRsp) {
      GetUnfinishedJobsForNodesRsp rsp = (GetUnfinishedJobsForNodesRsp) obj;
      return rsp.getJobIDs();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }

  /**
   * Get the job IDs of unfinished jobs which will regenerate the given files of a 
   * working node.
   * 
   * @param nodeID
   *   The unique working version identifier.
   * 
   * @param files
   *   The specific files to check.
   * 
   * @return 
   *   The unfinished job IDs. 
   */ 
  public synchronized TreeSet<Long>
  getUnfinishedJobsForNodeFiles
  (
   NodeID nodeID, 
   ArrayList<File> files 
  )
    throws PipelineException  
  {
    verifyConnection();
    
    QueueGetUnfinishedJobsForNodeFilesReq req = 
      new QueueGetUnfinishedJobsForNodeFilesReq(nodeID, files);
    
    Object obj = performTransaction(QueueRequest.GetUnfinishedJobsForNodeFiles, req);
    if(obj instanceof GetUnfinishedJobsForNodeFilesRsp) {
      GetUnfinishedJobsForNodeFilesRsp rsp = (GetUnfinishedJobsForNodeFilesRsp) obj;
      return rsp.getJobIDs();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }

  /**
   * Submit a set of jobs to be executed by the queue. <P> 
   * 
   * @param jobs
   *   The queue jobs.
   * 
   * @throws PipelineException
   *   If unable to submit the jobs.
   */ 
  public synchronized void 
  submitJobs
  (
   Collection<QueueJob> jobs
  ) 
    throws PipelineException  
  {
    verifyConnection();

    QueueSubmitJobsReq req = new QueueSubmitJobsReq(new ArrayList<QueueJob>(jobs));
    Object obj = performTransaction(QueueRequest.SubmitJobs, req); 
    handleSimpleResponse(obj);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Notify the queue that a set of previously submitted jobs make up a job group.
   * 
   * @param group
   *   The queue job group.
   * 
   * @throws PipelineException
   *   If unable to group the jobs.
   */ 
  public synchronized void 
  groupJobs
  (
   QueueJobGroup group
  ) 
    throws PipelineException  
  {
    verifyConnection();

    QueueGroupJobsReq req = new QueueGroupJobsReq(group);
    Object obj = performTransaction(QueueRequest.GroupJobs, req); 
    handleSimpleResponse(obj);
  }

}

