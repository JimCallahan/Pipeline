// $Id: MasterRequest.java,v 1.2 2004/05/23 19:48:55 jim Exp $

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
   * An instance of {@link MiscGetPrivilegedUsersReq MiscGetPrivilegedUsersReq} is next.
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
   * An instance of {@link NodeCreateWorkingAreaReq NodeCreateWorkingAreaReq} is next.
   */
  CreateWorkingArea,  

  /**
   * An instance of {@link NodeGetWorkingAreasReq NodeGetWorkingAreasReq} is next.
   */
  GetWorkingAreas,  


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


  /*----------------------------------------------------------------------------------------*/

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
