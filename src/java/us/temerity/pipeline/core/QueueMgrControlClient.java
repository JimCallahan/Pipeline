// $Id: QueueMgrControlClient.java,v 1.25 2009/12/16 04:13:33 jesse Exp $

package us.temerity.pipeline.core;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;
import us.temerity.pipeline.message.misc.*;
import us.temerity.pipeline.message.queue.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   M G R   C O N T R O L   C L I E N T                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A control connection to the Pipeline queue server daemon. <P> 
 * 
 * This class handles network communication with the Pipeline queue server daemon 
 * <A HREF="../../../../man/plqueuemgr.html"><B>plqueuemgr</B><A>(1).  
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
    this(false);
  }

  /** 
   * Construct a new queue manager control client.
   * 
   * @param forceLongTransactions
   *   Whether to treat all uses of {@link performTransaction} like 
   *   {@link performLongTransaction} with an infinite request timeout and a 60-second 
   *   response retry interval with infinite retries.
   */
  public
  QueueMgrControlClient
  (
   boolean forceLongTransactions   
  )
  {
    super(forceLongTransactions);
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
  /*   N E W   K E Y C H O O S E R                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Notify the queue manager that a new key chooser has been installed. <p>
   * 
   * This is trigger the queue manager to look at all the queue controls that use 
   * key-choosers, and if the chooser is being used by an existing control, set the flag that
   * will notify users that all jobs needs to have their key choosers rerun.  This can be done
   * with the {@link #updateAllJobKeys()} method.
   * 
   * @param pluginID
   *   The pluginID of the newly installed keychooser.
   *   
   * @throws PipelineException
   *   If unable to notify the queue manager of the update.
   */
  public synchronized void
  newKeyChooserInstalled
  (
    PluginID pluginID  
  )
    throws PipelineException
  {
    verifyConnection();
    
    MiscPluginIDReq req = new MiscPluginIDReq(pluginID);
    Object obj = performTransaction(QueueRequest.NewKeyChooserInstalled, req); 
    handleSimpleResponse(obj);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A D M I N I S T R A T I O N                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a set of database backup files. <P> 
   * 
   * The backup will not be perfomed until any currently running database operations have 
   * completed.  Once the databsae backup has begun, all new database operations will blocked
   * until the backup is complete.  The this reason, the backup should be performed during 
   * non-peak hours. <P> 
   * 
   * The database backup files will be automatically named: <P> 
   * <DIV style="margin-left: 40px;">
   *   plqueuemgr-db.<I>YYMMDD</I>.<I>HHMMSS</I>.tar<P>
   * </DIV>
   * 
   * Where <I>YYMMDD</I>.<I>HHMMSS</I> is the year, month, day, hour, minute and second of 
   * the backup.  The backup file is a <B>gzip</B>(1) compressed <B>tar</B>(1) archive of
   * the {@link Glueable GLUE} format files which make of the persistent storage of the
   * Pipeline database. <P> 
   * 
   * Only privileged users may create a database backup. <P> 
   * 
   * @param dir
   *   The full path to the directory to store backup files.  This path is will be 
   *   interpreted as local to the machine running the plqueuemgr daemon.
   * 
   * @param dateString
   *   The time of the backup encoded as a string.
   * 
   * @throws PipelineException 
   *   If unable to perform the backup.
   */ 
  public synchronized void
  backupDatabase
  (
   Path dir, 
   String dateString
  ) 
    throws PipelineException
  {
    verifyConnection();
    
    QueueBackupDatabaseReq req = new QueueBackupDatabaseReq(dir, dateString); 

    Object obj = performLongTransaction(QueueRequest.BackupDatabase, req, 15000, 60000);  
    handleSimpleResponse(obj);    
  } 

  

  /*----------------------------------------------------------------------------------------*/
  /*   J O B S                                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the jobs IDs and states for each primary file of the given working version as
   * well as any checksums already computed by the jobs newer than those currently cached.<P> 
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
   * @param latestUpdates
   *   The timestamps of each currently cached checksum indexed by primary/secondary file.
   * 
   * @param jobIDs
   *   An empty list which will be filled with the unique job identifiers of the latest 
   *   job which regenerates each file of the primary file sequence.
   * 
   * @param states
   *   An empty list which will be filled with the JobState of each file of the primary file 
   *   sequence.
   * 
   * @return 
   *   The newer primary/secondary file checksums.
   * 
   * @throws PipelineException
   *   If unable to determine the job IDs. 
   */
  public synchronized CheckSumCache
  getJobStatesAndCheckSums
  (
   NodeID nodeID, 
   long stamp, 
   FileSeq fseq, 
   TreeMap<String,Long> latestUpdates, 
   ArrayList<Long> jobIDs, 
   ArrayList<JobState> states 
  ) 
    throws PipelineException  
  {
    verifyConnection();
    
    QueueGetJobStatesAndCheckSumsReq req = 
      new QueueGetJobStatesAndCheckSumsReq(nodeID, stamp, fseq, latestUpdates);
    
    Object obj = performTransaction(QueueRequest.GetJobStatesAndCheckSums, req);
    if(obj instanceof QueueGetJobStatesAndCheckSumsRsp) {
      QueueGetJobStatesAndCheckSumsRsp rsp = (QueueGetJobStatesAndCheckSumsRsp) obj;
      jobIDs.addAll(rsp.getJobIDs());
      states.addAll(rsp.getStates());
      return rsp.getCheckSumCache();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }

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
   long stamp, 
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
  public synchronized MappedSet<String,Long>
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
    if(obj instanceof QueueGetUnfinishedJobsForNodesRsp) {
      QueueGetUnfinishedJobsForNodesRsp rsp = (QueueGetUnfinishedJobsForNodesRsp) obj;
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
    if(obj instanceof QueueGetUnfinishedJobsForNodeFilesRsp) {
      QueueGetUnfinishedJobsForNodeFilesRsp rsp = (QueueGetUnfinishedJobsForNodeFilesRsp) obj;
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
   * @param group
   *   The queue job group.
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
   QueueJobGroup group,
   Collection<QueueJob> jobs
  ) 
    throws PipelineException  
  {
    verifyConnection();

    QueueSubmitJobsReq req = new QueueSubmitJobsReq(group, new ArrayList<QueueJob>(jobs));
    Object obj = performTransaction(QueueRequest.SubmitJobs, req); 
    handleSimpleResponse(obj);
  }

}

