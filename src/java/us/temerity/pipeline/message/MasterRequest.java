// $Id: MasterRequest.java,v 1.35 2005/03/30 20:37:29 jim Exp $

package us.temerity.pipeline.message;

/*------------------------------------------------------------------------------------------*/
/*   M A S T E R   R E Q U E S T                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The set of identifiers for file request messages which may be sent over a network 
 * connection from the <CODE>MasterClient</CODE> to the <CODE>MasterMgrServer</CODE>.  <P> 
 * 
 * The protocol of communicaton between these file manager classes is for a 
 * <CODE>MasterRequest</CODE> to be sent followed by a corresponding request
 * object which contains the needed request data.  The purpose of this enumeration is to 
 * make testing for which request is being send more efficient and cleaner on the 
 * <CODE>MasterMgrServer</CODE> side of the connection.
 */
public
enum MasterRequest
{  
  /**
   * Get the name of the default toolset.
   */
  GetDefaultToolsetName, 

  /**
   * An instance of {@link MiscSetDefaultToolsetNameReq MiscSetDefaultToolsetNameReq} is next.
   */
  SetDefaultToolsetName, 

  /**
   * Get the names of the currently active toolsets.
   */
  GetActiveToolsetNames, 

  /**
   * An instance of {@link MiscSetToolsetActiveReq MiscSetToolsetActiveReq} is next.
   */
  SetToolsetActive, 

  /**
   * Get the names of all toolsets.
   */
  GetToolsetNames, 

  /**
   * An instance of {@link MiscGetToolsetReq MiscGetToolsetReq} is next.
   */
  GetToolset, 

  /**
   * An instance of {@link MiscGetToolsetEnvironmentReq MiscGetToolsetEnvironmentReq} is next.
   */
  GetToolsetEnvironment, 

  /**
   * An instance of {@link MiscCreateToolsetReq MiscCreateToolsetReq} is next.
   */
  CreateToolset, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names and revision numbers of all toolset packages.
   */
  GetToolsetPackageNames, 

  /**
   * An instance of {@link MiscGetToolsetPackageReq MiscGetToolsetPackageReq} is next.
   */
  GetToolsetPackage, 

  /**
   * An instance of {@link MiscCreateToolsetPackageReq MiscCreateToolsetPackageReq} is next.
   */
  CreateToolsetPackage, 
  

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * An instance of {@link MiscGetEditorForSuffixReq MiscGetEditorForSuffixReq} is next.
   */
  GetEditorForSuffix, 
  
  /**
   * An instance of {@link MiscGetSuffixEditorsReq MiscGetSuffixEditorsReq} is next.
   */
  GetSuffixEditors, 
  
  /**
   * An instance of {@link MiscSetSuffixEditorsReq MiscSetSuffixEditorsReq} is next.
   */
  SetSuffixEditors, 

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get layout of the editor plugin selection menu.
   */
  GetEditorMenuLayout, 
  
  /**
   * Set layout of the editor plugin selection menu.
   */
  SetEditorMenuLayout, 
  

  /**
   * Get layout of the comparator plugin selection menu.
   */
  GetComparatorMenuLayout, 
  
  /**
   * Set layout of the comparator plugin selection menu.
   */
  SetComparatorMenuLayout, 
  

  /**
   * Get layout of the tool plugin selection menu.
   */
  GetToolMenuLayout, 
  
  /**
   * Set layout of the tool plugin selection menu.
   */
  SetToolMenuLayout, 
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance {@link QueueAddLicenseKeyReq QueueAddLicenseKeyReq} is next.
   */
  AddLicenseKey, 
  
  /**
   * An instance {@link QueueRemoveLicenseKeyReq QueueRemoveLicenseKeyReq} is next.
   */
  RemoveLicenseKey, 

  /**
   * An instance {@link QueueSetTotalLicensesReq QueueSetTotalLicensesReq} is next.
   */
  SetTotalLicenses, 
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the privileged users.
   */
  GetPrivilegedUsers, 

  /**
   * An instance of {@link MiscGrantPrivilegesReq MiscGrantPrivilegesReq} is next.
   */
  GrantPrivileges, 

  /**
   * An instance of {@link MiscRemovePrivilegesReq MiscRemovePrivilegesReq} is next.
   */
  RemovePrivileges, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the table of current working area authors and views.
   */
  GetWorkingAreas,  

  /**
   * An instance of {@link NodeCreateWorkingAreaReq NodeCreateWorkingAreaReq} is next.
   */
  CreateWorkingArea,  

  /**
   * An instance of {@link NodeRemoveWorkingAreaReq NodeRemoveWorkingAreaReq} is next.
   */
  RemoveWorkingArea,  

  /**
   * An instance of {@link NodeGetWorkingNamesReq NodeGetWorkingNamesReq} is next.
   */
  GetWorkingNames, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance of {@link NodeUpdatePathsReq NodeUpdatePathsReq} is next.
   */
  UpdatePaths, 

  /**
   * An instance of {@link NodeGetNodeOwningReq NodeGetNodeOwningReq} is next.
   */
  GetNodeOwning, 
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance of {@link NodeGetWorkingReq NodeGetWorkingReq} is next.
   */
  GetWorking, 

  /**
   * An instance of {@link NodeModifyPropertiesReq NodeModifyPropertiesReq} is next.
   */
  ModifyProperties, 

  /**
   * An instance of {@link NodeLinkReq NodeLinkReq} is next.
   */
  Link, 

  /**
   * An instance of {@link NodeUnlinkReq NodeUnlinkReq} is next.
   */
  Unlink, 

  /**
   * An instance of {@link NodeAddSecondaryReq NodeAddSecondaryReq} is next.
   */
  AddSecondary, 
 
  /**
   * An instance of {@link NodeRemoveSecondaryReq NodeRemoveSecondaryReq} is next.
   */
  RemoveSecondary, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance of {@link NodeGetCheckedInReq NodeGetCheckedInReq} is next.
   */
  GetCheckedIn, 

  /**
   * An instance of {@link NodeGetCheckedInVersionIDsReq NodeGetCheckedInVersionIDsReq} 
   * is next.
   */
  GetCheckedInVersionIDs, 

  /**
   * An instance of {@link NodeGetHistoryReq NodeGetHistoryReq} is next.
   */
  GetHistory, 

  /**
   * An instance of {@link NodeGetCheckedInFileNoveltyReq NodeGetCheckedInFileNoveltyReq} 
   * is next.
   */
  GetCheckedInFileNovelty, 

  /**
   * An instance of {@link NodeGetCheckedInLinksReq NodeGetCheckedInLinksReq} is next.
   */
  GetCheckedInLinks, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance of {@link NodeStatusReq NodeStatusReq} is next.
   */
  Status, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance of {@link NodeRegisterReq NodeRegisterReq} is next.
   */
  Register, 

  /**
   * An instance of {@link NodeReleaseReq NodeReleaseReq} is next.
   */
  Release, 

  /**
   * An instance of {@link NodeDeleteReq NodeDeleteReq} is next.
   */
  Delete, 

  /**
   * An instance of {@link NodeRemoveFilesReq NodeRemoveFilesReq} is next.
   */
  RemoveFiles, 

  /**
   * An instance of {@link NodeRenameReq NodeRenameReq} is next.
   */
  Rename, 

  /**
   * An instance of {@link NodeRenumberReq NodeRenumberReq} is next.
   */
  Renumber, 

  /**
   * An instance of {@link NodeCheckInReq NodeCheckInReq} is next.
   */
  CheckIn, 

  /**
   * An instance of {@link NodeCheckOutReq NodeCheckOutReq} is next.
   */
  CheckOut, 

  /**
   * An instance of {@link NodeRevertFilesReq NodeRevertFilesReq} is next.
   */
  RevertFiles, 

  /**
   * An instance of {@link NodeCloneFilesReq NodeCloneFilesReq} is next.
   */
  CloneFiles, 

  /**
   * An instance of {@link NodeEvolveReq NodeEvolveReq} is next.
   */
  Evolve, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance of {@link NodeSubmitJobsReq NodeSubmitJobsReq} is next.
   */
  SubmitJobs, 

  /**
   * An instance of {@link NodeResubmitJobsReq NodeResubmitJobsReq} is next.
   */
  ResubmitJobs, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance of {@link MiscBackupDatabaseReq MiscBackupDatabaseReq} is next.
   */
  BackupDatabase, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance of {@link MiscArchiveQueryReq MiscArchiveQueryReq} is next.
   */
  ArchiveQuery, 

  /**
   * An instance of {@link MiscGetArchiveSizesReq MiscGetArchiveSizesReq} is next.
   */
  GetArchiveSizes, 

  /**
   * An instance of {@link MiscArchiveReq MiscArchiveReq} is next.
   */
  Archive, 
  


  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance of {@link MiscOfflineQueryReq MiscOfflineQueryReq} is next.
   */
  OfflineQuery, 

  /**
   * An instance of {@link NodeGetOfflineVersionIDsReq NodeGetOfflineVersionIDsReq} is next.
   */
  GetOfflineVersionIDs, 

  /**
   * An instance of {@link MiscGetOfflineSizesReq MiscGetOfflineSizesReq} is next.
   */
  GetOfflineSizes, 

  /**
   * An instance of {@link MiscOfflineReq MiscOfflineReq} is next.
   */
  Offline, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance of {@link MiscRestoreQueryReq MiscRestoreQueryReq} is next.
   */
  RestoreQuery, 

  /**
   * An instance of {@link MiscRequestRestoreReq MiscRequestRestoreReq} is next.
   */
  RequestRestore, 
  
  /**
   * An instance of {@link MiscDenyRestoreReq MiscDenyRestoreReq} is next.
   */
  DenyRestore, 

  /**
   * An instance of {@link MiscGetRestoreRequestsReq MiscGetRestoreRequestsReq} is next.
   */
  GetRestoreRequests, 

  /**
   * An instance of {@link MiscGetRestoreSizesReq MiscGetRestoreSizesReq} is next.
   */
  GetRestoreSizes, 

  /**
   * An instance of {@link MiscRestoreReq MiscRestoreReq} is next.
   */
  Restore, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names and creation timestamps of all existing archives.
   */
  GetArchiveIndex, 

  /**
   * An instance of {@link MiscGetArchivesContainingReq MiscGetArchivesContainingReq} is next.
   */
  GetArchivesContaining, 

  /**
   * An instance of {@link MiscGetArchiveReq MiscGetArchiveReq} is next.
   */
  GetArchive, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * No more requests will be send over this connection.
   */
  Disconnect,

  /**
   * An instance of {@link MiscShutdownOptionsReq MiscShutdownOptionsReq} is next.
   */
  ShutdownOptions, 

  /**
   * Order the server to refuse any further requests and then to exit as soon as all
   * currently pending requests have be completed.
   */
  Shutdown;
 
}
