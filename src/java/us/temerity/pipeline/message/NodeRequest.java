// $Id: NodeRequest.java,v 1.8 2004/04/24 22:41:22 jim Exp $

package us.temerity.pipeline.message;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   R E Q U E S T                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The set of identifiers for file request messages which may be sent over a network 
 * connection from the <CODE>NodeMgrClient</CODE> to the <CODE>NodeMgrServer</CODE>.  <P> 
 * 
 * The protocol of communicaton between these file manager classes is for a 
 * <CODE>FileRequest</CODE> to be sent followed by a corresponding request
 * object which contains the needed request data.  The purpose of this enumeration is to 
 * make testing for which request is being send more efficient and cleaner on the 
 * <CODE>NodeMgrServer</CODE> side of the connection.
 * 
 * @see NodeGetWorkingReq
 * @see NodeModifyPropertiesReq
 * @see NodeLinkReq
 * @see NodeUnlinkReq
 * 
 * @see NodeStatusReq
 * @see NodeRegisterReq
 * @see NodeRevokeReq
 * @see NodeRenameReq
 * @see NodeCheckInReq
 * @see NodeCheckOutReq
 * 
 */
public
enum NodeRequest
{  
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

  // ...



  /**
   * An instance of {@link NodeStatusReq NodeStatusReq} is next.
   */
  Status, 

  /**
   * An instance of {@link NodeRegisterReq NodeRegisterReq} is next.
   */
  Register, 

  /**
   * An instance of {@link NodeRevokeReq NodeRevokeReq} is next.
   */
  Revoke, 

  /**
   * An instance of {@link NodeRenameReq NodeRenameReq} is next.
   */
  Rename, 

  /**
   * An instance of {@link NodeCheckInReq NodeCheckInReq} is next.
   */
  CheckIn, 

  /**
   * An instance of {@link NodeCheckOutReq NodeCheckOutReq} is next.
   */
  CheckOut, 

  // ...



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
