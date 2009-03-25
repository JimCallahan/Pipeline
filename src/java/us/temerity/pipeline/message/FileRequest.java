// $Id: FileRequest.java,v 1.27 2009/03/25 22:02:24 jim Exp $

package us.temerity.pipeline.message;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   R E Q U E S T                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The set of identifiers for file request messages which may be sent over a network 
 * connection from the <CODE>FileMgrClient</CODE> to the <CODE>FileMgrServer</CODE>.  <P> 
 * 
 * The protocol of communicaton between these file manager classes is for a 
 * <CODE>FileRequest</CODE> to be sent followed by a corresponding request
 * object which contains the needed request data.  The purpose of this enumeration is to 
 * make testing for which request is being send more efficient and cleaner on the 
 * <CODE>FileMgrServer</CODE> side of the connection.
 */
public
enum FileRequest
{
  /**
   * Get the current runtime controls. 
   */ 
  GetMasterControls,
  
  /**
   * An instance of {@link MiscSetMasterControlsReq} is next.
   */ 
  SetMasterControls,


  /*----------------------------------------------------------------------------------------*/

  /**
   * Make sure that the temporary directory exists.
   */ 
  ValidateScratchDir, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance of {@link FileCreateWorkingAreaReq} is next.
   */
  CreateWorkingArea, 
  
  /**
   * An instance of {@link FileRemoveWorkingAreaReq} is next.
   */
  RemoveWorkingArea, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance of {@link FileCheckInReq} is next.
   */
  CheckIn, 
  
  /**
   * An instance of {@link FileCheckOutReq} is next.
   */
  CheckOut, 
  
  /**
   * An instance of {@link FileRevertReq} is next.
   */
  Revert, 
  
  /**
   * An instance of {@link FileCloneReq} is next.
   */
  Clone, 
  
  /**
   * An instance of {@link FileStateReq} is next.
   */
  State, 

  /**
   * An instance of {@link FileRemoveReq} is next.
   */
  Remove, 

  /**
   * An instance of {@link FileRemoveAllReq} is next.
   */
  RemoveAll, 

  /**
   * An instance of {@link FileRenameReq} is next.
   */
  Rename, 

  /**
   * An instance of {@link FileChangeModeReq} is next.
   */
  ChangeMode,

  /**
   * An instance of {@link FileTouchAllReq} is next.
   */
  TouchAll,

  /**
   * An instance of {@link FileDeleteCheckedInReq} is next.
   */
  DeleteCheckedIn, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance of {@link FilePackNodesReq} is next.
   */
  PackNodes, 

  /**
   * An instance of {@link FileExtractBundleReq} is next.
   */
  ExtractBundle, 

  /**
   * An instance of {@link FileUnpackNodesReq} is next.
   */
  UnpackNodes, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance of {@link FileGetArchiveSizesReq} is next.
   */
  GetArchiveSizes, 

  /**
   * An instance of {@link FileArchiveReq} is next.
   */
  Archive, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance of {@link FileGetOfflineSizesReq} is next.
   */
  GetOfflineSizes, 

  /**
   * An instance of {@link FileOfflineReq} is next.
   */
  Offline, 

  /**
   * Get the fully resolved names and revision numbers of all offlined checked-in versions.
   */
  GetOfflined, 

  /**
   * An instance of {@link FileGetOfflinedNodeVersionsReq} is next.
   */
  GetOfflinedNodeVersions, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance of {@link FileExtractReq} is next.
   */
  Extract, 

  /**
   * An instance of {@link FileRestoreReq} is next.
   */
  Restore, 

  /**
   * An instance of {@link FileExtractCleanupReq} is next.
   */
  ExtractCleanup, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance of {@link FileExtractSiteVersionReq} is next.
   */
  ExtractSiteVersion, 

  /**
   * An instance of {@link FileSiteVersionReq} is next.
   */
  LookupSiteVersion, 

  /**
   * An instance of {@link FileSiteVersionReq} is next.
   */
  InsertSiteVersion, 
  

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
