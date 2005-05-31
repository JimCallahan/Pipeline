// $Id: QueueRequest.java,v 1.13 2005/05/31 09:37:45 jim Exp $

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
   * Get the names of the privileged users.
   */
  GetPrivilegedUsers, 

  /**
   * An instance {@link MiscSetPrivilegedUsersReq MiscSetPrivilegedUsersReq} is next.
   */
  SetPrivilegedUsers,
  

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
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance {@link QueueGetJobStatesReq QueueGetJobStatesReq} is next.
   */
  GetJobStates, 

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
   * An instance {@link QueueSubmitJobReq QueueSubmitJobReq} is next.
   */
  SubmitJob, 

  /**
   * An instance {@link QueueKillJobsReq QueueKillJobsReq} is next.
   */
  KillJobs, 

  /**
   * An instance {@link QueuePauseJobsReq QueuePauseJobsReq} is next.
   */
  PauseJobs, 

  /**
   * An instance {@link QueueResumeJobsReq QueueResumeJobsReq} is next.
   */
  ResumeJobs, 


  /**
   * An instance {@link QueueGroupJobsReq QueueGroupJobsReq} is next.
   */
  GroupJobs, 

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
