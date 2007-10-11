// $Id: QueueRequest.java,v 1.24 2007/10/11 18:52:07 jesse Exp $

package us.temerity.pipeline.message;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   R E Q U E S T                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * The set of identifiers for queue request messages which may be sent over a network 
 * connection from the <CODE>QueueMgrClient</CODE> and <CODE>QueueMgrFullClient</CODE>
 * instances to the <CODE>QueueMgrServer</CODE>. <P> 
 * 
 * The protocol of communication between these queue manager classes is for a 
 * <CODE>QueueRequest</CODE> to be sent followed by a corresponding request
 * object which contains the needed request data.  The purpose of this enumeration is to 
 * make testing for which request is being send more efficient and cleaner on the 
 * <CODE>QueueMgrServer</CODE> side of the connection.
 */
public
enum QueueRequest
{
  /**
   * An instance of {@link MiscUpdateAdminPrivilegesReq MiscUpdateAdminPrivilegesReq} is next.
   */
  UpdateAdminPrivileges, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current logging levels.
   */ 
  GetLogControls,
  
  /**
   * An instance of {@link MiscSetLogControlsReq} is next.
   */ 
  SetLogControls,
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current queueging levels.
   */ 
  GetQueueControls,
  
  /**
   * An instance of {@link QueueSetQueueControlsReq} is next.
   */ 
  SetQueueControls,
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the license keys.
   */ 
  GetLicenseKeyNames,

  /**
   * Get the license keys.
   */ 
  GetLicenseKeys,

  /**
   * An instance {@link QueueAddLicenseKeyReq QueueAddLicenseKeyReq} is next.
   */
  AddLicenseKey, 
  
  /**
   * An instance {@link QueueRemoveLicenseKeyReq QueueRemoveLicenseKeyReq} is next.
   */
  RemoveLicenseKey, 

  /**
   * An instance {@link QueueSetMaxLicensesReq QueueSetMaxLicensesReq} is next.
   */
  SetMaxLicenses, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the selection keys.
   */ 
  GetSelectionKeyNames,

  /**
   * Get the selection keys.
   */ 
  GetSelectionKeys,

  /**
   * An instance {@link QueueAddSelectionKeyReq QueueAddSelectionKeyReq} is next.
   */
  AddSelectionKey, 
  
  /**
   * An instance {@link QueueRemoveSelectionKeyReq QueueRemoveSelectionKeyReq} is next.
   */
  RemoveSelectionKey, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of all existing selection groups. 
   */ 
  GetSelectionGroupNames, 

  /**
   * Get the current selection biases for all existing selection groups. 
   */ 
  GetSelectionGroups, 

  /**
   * An instance {@link QueueAddSelectionGroupReq QueueAddSelectionGroupReq} is next.
   */
  AddSelectionGroup, 

  /**
   * An instance {@link QueueRemoveSelectionGroupsReq QueueRemoveSelectionGroupsReq} is next.
   */
  RemoveSelectionGroups, 

  /**
   * An instance {@link QueueEditSelectionGroupsReq QueueEditSelectionGroupsReq} is next.
   */
  EditSelectionGroups, 
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of all existing selection schedules. 
   */ 
  GetSelectionScheduleNames, 

  /**
   * Get the existing selection schedules. 
   */ 
  GetSelectionSchedules, 

  /**
   * An instance {@link QueueAddSelectionScheduleReq QueueAddSelectionScheduleReq} is next.
   */
  AddSelectionSchedule, 

  /**
   * An instance {@link QueueRemoveSelectionSchedulesReq QueueRemoveSelectionSchedulesReq} 
   * is next.
   */
  RemoveSelectionSchedules, 

  /**
   * An instance {@link QueueEditSelectionSchedulesReq QueueEditSelectionSchedulesReq} 
   * is next.
   */
  EditSelectionSchedules, 
  

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the current queue extension configurations.
   */
  GetQueueExtension, 
  
  /**
   * An instance of {@link QueueRemoveQueueExtension} is next.
   */
  RemoveQueueExtension, 
  
  /**
   * An instance of {@link QueueSetQueueExtension} is next.
   */
  SetQueueExtension, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current state of the hosts capable of executing jobs for the Pipeline queue.
   */ 
  GetHosts, 

  /**
   * An instance {@link QueueAddHostReq QueueAddHostReq} is next.
   */
  AddHost, 

  /**
   * An instance {@link QueueRemoveHostsReq QueueRemoveHostsReq} is next.
   */
  RemoveHosts, 

  /**
   * An instance {@link QueueEditHostsReq QueueEditHostsReq} is next.
   */
  EditHosts, 
  
  /**
   * An instance {@link QueueGetHostResourceSamplesReq QueueGetHostResourceSamplesReq} 
   * is next.
   */
  GetHostResourceSamples, 
  
  /**
   * An instance {@link QueueGetHostHistogramsReq QueueGetHostHistogramsReq} 
   * is next.
   */
  GetHostHistograms, 
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance {@link QueueGetJobStatesReq QueueGetJobStatesReq} is next.
   */
  GetJobStates, 

  /**
   * An instance {@link QueueGetUnfinishedJobsForNodesReq QueueGetUnfinishedJobsForNodesReq} 
   * is next.
   */
  GetUnfinishedJobsForNodes, 

  /**
   * An instance 
   * {@link QueueGetUnfinishedJobsForNodeFilesReq QueueGetUnfinishedJobsForNodeFilesReq} 
   * is next.
   */
  GetUnfinishedJobsForNodeFiles, 

  /**
   * Get the JobStatus of all jobs associated with the given job group IDs. 
   */
  GetJobStatus, 

  /**
   * Get the JobStatus of all currently running jobs. 
   */
  GetRunningJobStatus, 

  /**
   * An instance {@link QueueGetJobReq QueueGetJobReq} is next.
   */
  GetJob, 

  /**
   * An instance {@link QueueGetJobInfoReq QueueGetJobInfoReq} is next.
   */
  GetJobInfo, 

  /**
   * Get information about the currently running jobs.
   */ 
  GetRunningJobInfo, 


  /**
   * An instance {@link QueueSubmitJobsReq QueueSubmitJobsReq} is next.
   */
  SubmitJobs, 

  /**
   * Kill and requeue the jobs with the given IDs. <P> 
   */
  PreemptJobs, 

  /**
   * Kill the jobs with the given IDs. <P> 
   */
  KillJobs, 

  /**
   * Pause the jobs with the given IDs. <P> 
   */
  PauseJobs, 

  /**
   * Resume execution of the paused jobs with the given IDs. <P> 
   */
  ResumeJobs, 

  /**
   * Changes the Job Requirements of the the jobs with the given IDs.
   */
  ChangeJobReqs,


  /**
   * Kill and requeue all jobs associated with the given working version.<P> 
   */
  PreemptNodeJobs, 

  /**
   * Kill all jobs associated with the given working version.<P> 
   */
  KillNodeJobs, 

  /**
   * Pause all jobs associated with the given working version.<P> 
   */
  PauseNodeJobs, 

  /**
   * Resume execution of all paused jobs associated with the given working version.<P> 
   */
  ResumeNodeJobs, 


  /**
   * An instance {@link QueueGetJobGroupReq QueueGetJobGroupReq} is next.
   */
  GetJobGroup, 

  /**
   * Get all of the existing the job groups.
   */
  GetJobGroups, 

  /**
   * An instance {@link QueueDeleteJobGroupsReq QueueDeleteJobGroupsReq} is next.
   */
  DeleteJobGroups, 

  /**
   * An instance {@link QueueDeleteViewJobGroupsReq QueueDeleteViewJobGroupsReq} is next.
   */
  DeleteViewJobGroups, 
  
  /** 
   * Delete all of the completed job groups in all working areas. 
   */ 
  DeleteAllJobGroups,


  /*----------------------------------------------------------------------------------------*/

  /**
   * No more requests will be send over this connection.
   */
  Disconnect,

  /**
   * An instance of {@link QueueShutdownOptionsReq QueueShutdownOptionsReq} is next.
   */
  ShutdownOptions, 

  /**
   * Order the server to refuse any further requests and then to exit as soon as all
   * currently pending requests have be completed.
   */
  Shutdown;
 
}
