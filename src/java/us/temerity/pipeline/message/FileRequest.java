// $Id: FileRequest.java,v 1.13 2004/11/01 00:49:44 jim Exp $

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
   * An instance of {@link FileCreateWorkingAreaReq FileCreateWorkingAreaReq} is next.
   */
  CreateWorkingArea, 
  
  /**
   * An instance of {@link FileCheckInReq FileCheckInReq} is next.
   */
  CheckIn, 
  
  /**
   * An instance of {@link FileCheckOutReq FileCheckOutReq} is next.
   */
  CheckOut, 
  
  /**
   * An instance of {@link FileRevertReq FileRevertReq} is next.
   */
  Revert, 

  /**
   * An instance of {@link FileFreezeReq FileFreezeReq} is next.
   */
  Freeze, 
  
  /**
   * An instance of {@link FileUnfreezeReq FileUnfreezeReq} is next.
   */
  Unfreeze, 
  
  /**
   * An instance of {@link FileStateReq FileStateReq} is next.
   */
  State, 

  /**
   * An instance of {@link FileRemoveReq FileRemoveReq} is next.
   */
  Remove, 

  /**
   * An instance of {@link FileRemoveAllReq FileRemoveAllReq} is next.
   */
  RemoveAll, 

  /**
   * An instance of {@link FileRenameReq FileRenameReq} is next.
   */
  Rename, 

  /**
   * An instance of {@link FileDeleteCheckedInReq FileDeleteCheckedInReq} is next.
   */
  DeleteCheckedIn, 

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
