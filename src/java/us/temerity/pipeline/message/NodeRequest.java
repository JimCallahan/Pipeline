// $Id: NodeRequest.java,v 1.3 2004/03/28 00:49:43 jim Exp $

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
 * @see NodeRegisterReq
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
   * An instance of {@link NodeRegisterReq NodeRegisterReq} is next.
   */
  Register, 



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
