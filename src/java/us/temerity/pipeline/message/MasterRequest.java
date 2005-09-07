// $Id: MasterRequest.java,v 1.40 2005/09/07 21:11:16 jim Exp $

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
   * Get the names of all OS specific toolsets.
   */
  GetToolsetNames, 

  /**
   * Get the names of all toolsets for all operating systems.
   */
  GetAllToolsetNames, 

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
   * Get the names and revision numbers of all OS specific toolset packages.
   */
  GetToolsetPackageNames, 

  /**
   * Get the names and revision numbers of all toolset packages for all operating systems.
   */
  GetAllToolsetPackageNames, 

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
   * Get layout of the editor plugin selection menu associated with a toolset.
   */
  GetEditorMenuLayout, 
  
  /**
   * Set layout of the editor plugin selection menu associated with a toolset.
   */
  SetEditorMenuLayout, 

  /**
   * An instance of {@link MiscGetToolsetPluginsReq MiscGetToolsetPluginsReq} is next.
   */
  GetToolsetEditorPlugins, 
  
  /**
   * An instance of {@link MiscGetPackagePluginsReq MiscGetPackagePluginsReq} is next.
   */
  GetPackageEditorPlugins, 
  
  /**
   * An instance of {@link MiscGetPackagePluginsReq MiscGetPackagePluginsReq} is next.
   */
  SetPackageEditorPlugins, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get layout of the comparator plugin selection menu associated with a toolset.
   */
  GetComparatorMenuLayout, 
  
  /**
   * Set layout of the comparator plugin selection menu associated with a toolset.
   */
  SetComparatorMenuLayout, 

  /**
   * An instance of {@link MiscGetToolsetPluginsReq MiscGetToolsetPluginsReq} is next.
   */
  GetToolsetComparatorPlugins, 
  
  /**
   * An instance of {@link MiscGetPackagePluginsReq MiscGetPackagePluginsReq} is next.
   */
  GetPackageComparatorPlugins, 
  
  /**
   * An instance of {@link MiscGetPackagePluginsReq MiscGetPackagePluginsReq} is next.
   */
  SetPackageComparatorPlugins, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get layout of the action plugin selection menu associated with a toolset.
   */
  GetActionMenuLayout, 
  
  /**
   * Set layout of the action plugin selection menu associated with a toolset.
   */
  SetActionMenuLayout, 

  /**
   * An instance of {@link MiscGetToolsetPluginsReq MiscGetToolsetPluginsReq} is next.
   */
  GetToolsetActionPlugins, 
  
  /**
   * An instance of {@link MiscGetPackagePluginsReq MiscGetPackagePluginsReq} is next.
   */
  GetPackageActionPlugins, 
  
  /**
   * An instance of {@link MiscGetPackagePluginsReq MiscGetPackagePluginsReq} is next.
   */
  SetPackageActionPlugins, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get layout of the tool plugin selection menu associated with a toolset.
   */
  GetToolMenuLayout, 
  
  /**
   * Set layout of the tool plugin selection menu associated with a toolset.
   */
  SetToolMenuLayout, 

  /**
   * An instance of {@link MiscGetToolsetPluginsReq MiscGetToolsetPluginsReq} is next.
   */
  GetToolsetToolPlugins, 
  
  /**
   * An instance of {@link MiscGetPackagePluginsReq MiscGetPackagePluginsReq} is next.
   */
  GetPackageToolPlugins, 
  
  /**
   * An instance of {@link MiscGetPackagePluginsReq MiscGetPackagePluginsReq} is next.
   */
  SetPackageToolPlugins, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get layout of the archiver plugin selection menu associated with a toolset.
   */
  GetArchiverMenuLayout, 
  
  /**
   * Set layout of the archiver plugin selection menu associated with a toolset.
   */
  SetArchiverMenuLayout, 

  /**
   * An instance of {@link MiscGetToolsetPluginsReq MiscGetToolsetPluginsReq} is next.
   */
  GetToolsetArchiverPlugins, 
  
  /**
   * An instance of {@link MiscGetPackagePluginsReq MiscGetPackagePluginsReq} is next.
   */
  GetPackageArchiverPlugins, 
  
  /**
   * An instance of {@link MiscGetPackagePluginsReq MiscGetPackagePluginsReq} is next.
   */
  SetPackageArchiverPlugins, 


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
  GetArchivedOn, 

  /**
   * Get the names and restoration timestamps of all existing archives.
   */
  GetRestoredOn, 

  /**
   * Get the STDOUT output from running the Archiver plugin during the creation of the 
   * given archive volume.
   */
  GetArchivedOutput, 

  /**
   * Get the STDOUT output from running the Archiver plugin during the restoration of the 
   * given archive volume at the given time.
   */
  GetRestoredOutput, 

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
