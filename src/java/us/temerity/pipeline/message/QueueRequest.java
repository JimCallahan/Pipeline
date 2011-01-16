// $Id: QueueRequest.java,v 1.36 2010/01/08 09:38:10 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.message.queue.*;
import us.temerity.pipeline.message.simple.*;

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
  
  /**
   * An instance of {@link MiscPluginIDReq} is next.
   */
  NewKeyChooserInstalled,


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
   * Get the names and descriptions of the license keys.
   */ 
  GetLicenseKeyDescriptions,

  /**
   * Get the license keys.
   */ 
  GetLicenseKeys,

  /**
   * An instance {@link QueueAddLicenseKeyReq} is next.
   */
  AddLicenseKey, 
  
  /**
   * An instance {@link QueueRemoveLicenseKeyReq} is next.
   */
  RemoveLicenseKey, 

  /**
   * An instance {@link QueueSetMaxLicensesReq} is next.
   */
  SetMaxLicenses, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the selection keys.
   */ 
  GetSelectionKeyNames,

  /**
   * Get the names and descriptions of the selection keys.
   */ 
  GetSelectionKeyDescriptions,

  /**
   * Get the selection keys.
   */ 
  GetSelectionKeys,

  /**
   * An instance {@link QueueAddSelectionKeyReq} is next.
   */
  AddSelectionKey, 
  
  /**
   * An instance {@link QueueRemoveSelectionKeyReq} is next.
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
   * An instance {@link QueueAddByNameReq} is next.
   */
  AddSelectionGroup, 

  /**
   * An instance {@link QueueRemoveByNameReq} is next.
   */
  RemoveSelectionGroups, 

  /**
   * An instance {@link QueueEditSelectionGroupsReq} is next.
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
   * An instance {@link QueueAddByNameReq} is next.
   */
  AddSelectionSchedule, 

  /**
   * An instance {@link QueueRemoveByNameReq} is next.
   */
  RemoveSelectionSchedules, 

  /**
   * An instance {@link QueueEditSelectionSchedulesReq} 
   * is next.
   */
  EditSelectionSchedules, 

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the hardware keys.
   */ 
  GetHardwareKeyNames,

  /**
   * Get the names and descriptions of the hardware keys.
   */ 
  GetHardwareKeyDescriptions,

  /**
   * Get the hardware keys.
   */ 
  GetHardwareKeys,

  /**
   * An instance {@link QueueAddHardwareKeyReq QueueAddHardwareKeyReq} is next.
   */
  AddHardwareKey, 
  
  /**
   * An instance {@link QueueRemoveHardwareKeyReq QueueRemoveHardwareKeyReq} is next.
   */
  RemoveHardwareKey, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of all existing hardware groups. 
   */ 
  GetHardwareGroupNames, 

  /**
   * Get the current hardware biases for all existing hardware groups. 
   */ 
  GetHardwareGroups, 

  /**
   * An instance {@link QueueAddByNameReq} is next.
   */
  AddHardwareGroup, 

  /**
   * An instance {@link QueueRemoveByNameReq} is next.
   */
  RemoveHardwareGroups, 

  /**
   * An instance {@link QueueEditHardwareGroupsReq} is next.
   */
  EditHardwareGroups, 

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of all existing dispatch controls. 
   */ 
  GetDispatchControlNames, 

  /**
   * Get all the current dispatch controls. 
   */ 
  GetDispatchControls, 

  /**
   * An instance {@link QueueAddByNameReq} is next.
   */
  AddDispatchControl, 

  /**
   * An instance {@link QueueRemoveByNameReq} is next.
   */
  RemoveDispatchControls, 

  /**
   * An instance {@link QueueEditDispatchControlsReq} is next.
   */
  EditDispatchControls, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of all existing user balance groups. 
   */ 
  GetBalanceGroupNames, 

  /**
   * Get all the current user balance groups. 
   */ 
  GetBalanceGroups, 
  
  /**
   * Get a specific user balance group.
   */
  GetBalanceGroup,

  /**
   * An instance {@link QueueAddByNameReq} is next.
   */
  AddBalanceGroup, 

  /**
   * An instance {@link QueueRemoveByNameReq} is next.
   */
  RemoveBalanceGroups, 

  /**
   * An instance {@link QueueEditUserBalanceGroupsReq} is next.
   */
  EditBalanceGroups,
  
  /**
   * Get the current user usage of all the balance groups.
   */
  GetBalanceGroupUsage,


  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance of {@link QueueBackupDatabaseReq} is next.
   */
  BackupDatabase, 
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current queue extension configurations.
   */
  GetQueueExtension, 
  
  /**
   * An instance of {@link QueueRemoveQueueExtensionReq} is next.
   */
  RemoveQueueExtension, 
  
  /**
   * An instance of {@link QueueSetQueueExtensionReq} is next.
   */
  SetQueueExtension, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the hosts for which there are current notes.  
   */ 
  GetHostsWithNotes, 

  /**
   * Get all notes (if any) associated with the given host.
   */ 
  GetHostNote, 

  /**
   * Get the note (if any) associated with the given host and timestamp.
   */ 
  GetHostNotes, 

  /**
   * An instance of {@link QueueSetHostNoteReq} is next.
   */ 
  AddHostNote, 

  /**
   * Remove all notes (if any) associated with the given host.
   */ 
  RemoveHostNote, 

  /**
   * Remove the note (if any) associated with the given host and timestamp.
   */ 
  RemoveHostNotes, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current state of the hosts capable of executing jobs for the Pipeline queue.
   */ 
  GetHosts, 

  /**
   * An instance {@link QueueAddByNameReq} is next.
   */
  AddHost, 

  /**
   * An instance {@link QueueRemoveByNameReq} is next.
   */
  RemoveHosts, 

  /**
   * An instance {@link QueueEditHostsReq} is next.
   */
  EditHosts, 
  
  /**
   * An instance {@link QueueGetHostResourceSamplesReq} 
   * is next.
   */
  GetHostResourceSamples, 
  
  /**
   * An instance {@link QueueGetHostHistogramsReq} 
   * is next.
   */
  GetHostHistograms, 
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance {@link QueueGetJobStatesAndCheckSumsReq} is next.
   */
  GetJobStatesAndCheckSums, 

  /**
   * An instance {@link QueueGetJobStatesReq} is next.
   */
  GetJobStates, 

  /**
   * An instance {@link QueueGetUnfinishedJobsForNodesReq} 
   * is next.
   */
  GetUnfinishedJobsForNodes, 

  /**
   * An instance 
   * {@link QueueGetUnfinishedJobsForNodeFilesReq} 
   * is next.
   */
  GetUnfinishedJobsForNodeFiles, 

  /**
   * Get the JobStateDistribution of all jobs associated with the given job group IDs. 
   */
  GetJobStateDistribution, 

  /**
   * Get the JobStatus of all jobs associated with the given job group IDs. 
   */
  GetJobStatus, 

  /**
   * Get the JobStatus of all currently running jobs. 
   */
  GetRunningJobStatus, 

  /**
   * An instance {@link QueueGetJobReq} is next.
   */
  GetJob, 

  /**
   * An instance {@link QueueGetJobInfoReq} is next.
   */
  GetJobInfo, 

  /**
   * Get information about the currently running jobs.
   */ 
  GetRunningJobInfo, 


  /**
   * An instance {@link QueueSubmitJobsReq} is next.
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
   * Updates the keys set by plugins for the jobs with the given IDs.
   */
  UpdateJobKeys,
  
  /**
   * Check if all the jobs need to have their keys set by plugins update.
   */
  DoJobKeysNeedUpdate,
  
  /**
   * Get the timestamp that reflects the last time a keychoosers or a key that uses a 
   * keychooser was updated. 
   */
  GetChooserUpdateTime,
  
  /**
   * Updates the keys set by plugins for all the jobs.
   */
  UpdateAllJobKeys,
  
  /**
   * Change the Key State of all active jobs to Stale.
   */
  InvalidateAllJobKeys,

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
   * An instance {@link QueueGetJobGroupReq} is next.
   */
  GetJobGroup, 
  
  /**
   * An instance of {@link QueueGetJobGroupReq} is next.
   */
  GetJobGroupsByID,

  /**
   * An instance of {@link QueueGetJobGroupsReq} is next.
   */
  GetJobGroups, 
  
  /**
   * An instance of {@link SimpleSetReq} is next.
   */
  GetJobGroupsByUsers,

  /**
   * An instance {@link QueueDeleteJobGroupsReq} is next.
   */
  DeleteJobGroups, 

  /**
   * An instance {@link QueueDeleteViewJobGroupsReq} is next.
   */
  DeleteViewJobGroups, 
  
  /**
   * An instance of {@link SimpleSetReq} is next.
   */
  DeleteUsersJobGroups,
  
  /** 
   * Delete all of the completed job groups in all working areas. 
   */ 
  DeleteAllJobGroups,


  /*----------------------------------------------------------------------------------------*/

  /**
   * Simple test of network connectivity.
   */ 
  Ping, 

  /**
   * No more requests will be send over this connection.
   */
  Disconnect,

  /**
   * An instance of {@link QueueShutdownOptionsReq} is next.
   */
  ShutdownOptions, 

  /**
   * Order the server to refuse any further requests and then to exit as soon as all
   * currently pending requests have be completed.
   */
  Shutdown;
 
}
