// $Id: MasterRequest.java,v 1.19 2004/10/09 16:54:40 jim Exp $

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



  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance of {@link NodeUpdatePathsReq NodeUpdatePathsReq} is next.
   */
  UpdatePaths, 


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
   * An instance of {@link NodeEvolveReq NodeEvolveReq} is next.
   */
  Evolve, 

  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance of {@link NodeSubmitJobsReq NodeSubmitJobsReq} is next.
   */
  SubmitJobs, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * No more requests will be send over this connection.
   */
  Disconnect,

  /**
   * Order the server to refuse any further requests and then to exit as soon as all
   * currently pending requests have be completed.
   */
  Shutdown;
 
}
