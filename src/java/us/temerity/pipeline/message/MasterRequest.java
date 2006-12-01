// $Id: MasterRequest.java,v 1.51 2006/12/01 18:33:41 jim Exp $

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
   * Get the work groups used to determine the scope of administrative privileges.
   */
  GetWorkGroups, 
  
  /**
   * An instance of {@link MiscSetWorkGroupsReq} is next.
   */
  SetWorkGroups, 


  /**
   * Get the administrative privileges for all users.
   */  
  GetPrivileges,

  /**
   * An instance of {@link MiscEditPrivilegesReq} is next.
   */
  EditPrivileges, 

  
  /**
   * Get the privileges granted to a specific user with respect to all other users.
   */ 
  GetPrivilegeDetails, 


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
   * Get the current logging levels.
   */ 
  GetMasterControls,
  
  /**
   * An instance of {@link MiscSetMasterControlsReq} is next.
   */ 
  SetMasterControls,
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the default toolset.
   */
  GetDefaultToolsetName, 

  /**
   * An instance of {@link MiscSetDefaultToolsetNameReq} is next.
   */
  SetDefaultToolsetName, 

  /**
   * Get the names of the currently active toolsets.
   */
  GetActiveToolsetNames, 

  /**
   * An instance of {@link MiscSetToolsetActiveReq} is next.
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
   * An instance of {@link MiscGetToolsetReq} is next.
   */
  GetToolset, 

  /**
   * An instance of {@link MiscGetOsToolsetsReq} is next.
   */
  GetOsToolsets, 

  /**
   * An instance of {@link MiscGetToolsetEnvironmentReq} is next.
   */
  GetToolsetEnvironment, 

  /**
   * An instance of {@link MiscCreateToolsetReq} is next.
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
   * An instance of {@link MiscGetToolsetPackageReq} is next.
   */
  GetToolsetPackage, 

  /**
   * An instance of {@link MiscGetToolsetPackagesReq} is next.
   */
  GetToolsetPackages, 

  /**
   * An instance of {@link MiscCreateToolsetPackageReq} is next.
   */
  CreateToolsetPackage, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance of {@link MiscGetPluginMenuLayoutsRsp} is next.
   */
  GetPluginMenuLayouts, 

  /**
   * An instance of {@link MiscGetSelectPackagePluginsReq} is next.
   */
  GetSelectPackagePlugins, 


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
   * An instance of {@link MiscGetToolsetPluginsReq} is next.
   */
  GetToolsetEditorPlugins, 
  
  /**
   * An instance of {@link MiscGetPackagePluginsReq} is next.
   */
  GetPackageEditorPlugins, 
  
  /**
   * An instance of {@link MiscGetPackagePluginsReq} is next.
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
   * An instance of {@link MiscGetToolsetPluginsReq} is next.
   */
  GetToolsetComparatorPlugins, 
  
  /**
   * An instance of {@link MiscGetPackagePluginsReq} is next.
   */
  GetPackageComparatorPlugins, 
  
  /**
   * An instance of {@link MiscGetPackagePluginsReq} is next.
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
   * An instance of {@link MiscGetToolsetPluginsReq} is next.
   */
  GetToolsetActionPlugins, 
  
  /**
   * An instance of {@link MiscGetPackagePluginsReq} is next.
   */
  GetPackageActionPlugins, 
  
  /**
   * An instance of {@link MiscGetPackagePluginsReq} is next.
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
   * An instance of {@link MiscGetToolsetPluginsReq} is next.
   */
  GetToolsetToolPlugins, 
  
  /**
   * An instance of {@link MiscGetPackagePluginsReq} is next.
   */
  GetPackageToolPlugins, 
  
  /**
   * An instance of {@link MiscGetPackagePluginsReq} is next.
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
   * An instance of {@link MiscGetToolsetPluginsReq} is next.
   */
  GetToolsetArchiverPlugins, 
  
  /**
   * An instance of {@link MiscGetPackagePluginsReq} is next.
   */
  GetPackageArchiverPlugins, 
  
  /**
   * An instance of {@link MiscGetPackagePluginsReq} is next.
   */
  SetPackageArchiverPlugins, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get layout of the master extension plugin selection menu associated with a toolset.
   */
  GetMasterExtMenuLayout, 
  
  /**
   * Set layout of the master extension plugin selection menu associated with a toolset.
   */
  SetMasterExtMenuLayout, 

  /**
   * An instance of {@link MiscGetToolsetPluginsReq} is next.
   */
  GetToolsetMasterExtPlugins, 
  
  /**
   * An instance of {@link MiscGetPackagePluginsReq} is next.
   */
  GetPackageMasterExtPlugins, 
  
  /**
   * An instance of {@link MiscGetPackagePluginsReq} is next.
   */
  SetPackageMasterExtPlugins, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get layout of the master extension plugin selection menu associated with a toolset.
   */
  GetQueueExtMenuLayout, 
  
  /**
   * Set layout of the master extension plugin selection menu associated with a toolset.
   */
  SetQueueExtMenuLayout, 

  /**
   * An instance of {@link MiscGetToolsetPluginsReq} is next.
   */
  GetToolsetQueueExtPlugins, 
  
  /**
   * An instance of {@link MiscGetPackagePluginsReq} is next.
   */
  GetPackageQueueExtPlugins, 
  
  /**
   * An instance of {@link MiscGetPackagePluginsReq} is next.
   */
  SetPackageQueueExtPlugins, 


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the current master extension configurations.
   */
  GetMasterExtension, 
  
  /**
   * An instance of {@link MiscRemoveMasterExtension} is next.
   */
  RemoveMasterExtension, 
  
  /**
   * An instance of {@link MiscSetMasterExtension} is next.
   */
  SetMasterExtension, 
  

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * An instance of {@link MiscGetEditorForSuffixReq} is next.
   */
  GetEditorForSuffix, 
  
  /**
   * An instance of {@link MiscGetSuffixEditorsReq} is next.
   */
  GetSuffixEditors, 
  
  /**
   * An instance of {@link MiscSetSuffixEditorsReq} is next.
   */
  SetSuffixEditors, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance {@link QueueAddLicenseKeyReq} is next.
   */
  AddLicenseKey, 
  
  /**
   * An instance {@link QueueRemoveLicenseKeyReq} is next.
   */
  RemoveLicenseKey, 

  /**
   * An instance {@link QueueSetTotalLicensesReq} is next.
   */
  SetTotalLicenses, 
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the table of current working area authors and views.
   */
  GetWorkingAreas,  

  /**
   * An instance of {@link NodeGetWorkingAreasContaining} is next.
   */
  GetWorkingAreasContaining,  

  /**
   * An instance of {@link NodeCreateWorkingAreaReq} is next.
   */
  CreateWorkingArea,  

  /**
   * An instance of {@link NodeRemoveWorkingAreaReq} is next.
   */
  RemoveWorkingArea,  

  /**
   * An instance of {@link NodeGetWorkingNamesReq} is next.
   */
  GetWorkingNames, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance of {@link NodeUpdatePathsReq} is next.
   */
  UpdatePaths, 

  /**
   * An instance of {@link NodeGetNodeOwningReq} is next.
   */
  GetNodeOwning, 
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance of {@link NodeGetWorkingReq} is next.
   */
  GetWorking, 

  /**
   * An instance of {@link NodeModifyPropertiesReq} is next.
   */
  ModifyProperties, 

  /**
   * An instance of {@link NodeLinkReq} is next.
   */
  Link, 

  /**
   * An instance of {@link NodeUnlinkReq} is next.
   */
  Unlink, 

  /**
   * An instance of {@link NodeAddSecondaryReq} is next.
   */
  AddSecondary, 
 
  /**
   * An instance of {@link NodeRemoveSecondaryReq} is next.
   */
  RemoveSecondary, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance of {@link NodeGetCheckedInReq} is next.
   */
  GetCheckedIn, 

  /**
   * An instance of {@link NodeGetAllCheckedInReq} is next.
   */
  GetAllCheckedIn, 

  /**
   * An instance of {@link NodeGetCheckedInVersionIDsReq} 
   * is next.
   */
  GetCheckedInVersionIDs, 

  /**
   * An instance of {@link NodeGetHistoryReq} is next.
   */
  GetHistory, 

  /**
   * An instance of {@link NodeGetCheckedInFileNoveltyReq} is next.
   */
  GetCheckedInFileNovelty, 

  /**
   * An instance of {@link NodeGetCheckedInLinksReq} is next.
   */
  GetCheckedInLinks, 

  /**
   * An instance of {@link NodeGetDownstreamCheckedInLinksReq} is next.
   */
  GetDownstreamCheckedInLinks, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance of {@link NodeStatusReq} is next.
   */
  Status, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance of {@link NodeRegisterReq} is next.
   */
  Register, 

  /**
   * An instance of {@link NodeReleaseReq} is next.
   */
  Release, 

  /**
   * An instance of {@link NodeDeleteReq} is next.
   */
  Delete, 

  /**
   * An instance of {@link NodeRemoveFilesReq} is next.
   */
  RemoveFiles, 

  /**
   * An instance of {@link NodeRenameReq} is next.
   */
  Rename, 

  /**
   * An instance of {@link NodeRenumberReq} is next.
   */
  Renumber, 

  /**
   * An instance of {@link NodeCheckInReq} is next.
   */
  CheckIn, 

  /**
   * An instance of {@link NodeCheckOutReq} is next.
   */
  CheckOut, 

  /**
   * An instance of {@link NodeLockReq} is next.
   */
  Lock, 

  /**
   * An instance of {@link NodeRevertFilesReq} is next.
   */
  RevertFiles, 

  /**
   * An instance of {@link NodeCloneFilesReq} is next.
   */
  CloneFiles, 

  /**
   * An instance of {@link NodeEvolveReq} is next.
   */
  Evolve, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance of {@link NodeSubmitJobsReq} is next.
   */
  SubmitJobs, 

  /**
   * An instance of {@link NodeResubmitJobsReq} is next.
   */
  ResubmitJobs, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance of {@link MiscBackupDatabaseReq} is next.
   */
  BackupDatabase, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance of {@link MiscArchiveQueryReq} is next.
   */
  ArchiveQuery, 

  /**
   * An instance of {@link MiscGetArchiveSizesReq} is next.
   */
  GetArchiveSizes, 

  /**
   * An instance of {@link MiscArchiveReq} is next.
   */
  Archive, 
  


  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance of {@link MiscOfflineQueryReq} is next.
   */
  OfflineQuery, 

  /**
   * An instance of {@link NodeGetOfflineVersionIDsReq} is next.
   */
  GetOfflineVersionIDs, 

  /**
   * An instance of {@link MiscGetOfflineSizesReq} is next.
   */
  GetOfflineSizes, 

  /**
   * An instance of {@link MiscOfflineReq} is next.
   */
  Offline, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance of {@link MiscRestoreQueryReq} is next.
   */
  RestoreQuery, 

  /**
   * An instance of {@link MiscRequestRestoreReq} is next.
   */
  RequestRestore, 
  
  /**
   * An instance of {@link MiscDenyRestoreReq} is next.
   */
  DenyRestore, 

  /**
   * An instance of {@link MiscGetRestoreRequestsReq} is next.
   */
  GetRestoreRequests, 

  /**
   * An instance of {@link MiscGetRestoreSizesReq} is next.
   */
  GetRestoreSizes, 

  /**
   * An instance of {@link MiscRestoreReq} is next.
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
   * An instance of {@link MiscGetArchivesContainingReq} is next.
   */
  GetArchivesContaining, 

  /**
   * An instance of {@link MiscGetArchiveReq} is next.
   */
  GetArchive, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance of {@link MiscCreateInitialPanelLayoutReq} is next.
   */
  CreateInitialPanelLayout, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * No more requests will be send over this connection.
   */
  Disconnect,

  /**
   * An instance of {@link MiscShutdownOptionsReq} is next.
   */
  ShutdownOptions, 

  /**
   * Order the server to refuse any further requests and then to exit as soon as all
   * currently pending requests have be completed.
   */
  Shutdown;
 
}
