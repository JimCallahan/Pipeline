// $Id: NodeRequest.java,v 1.1 2004/03/26 04:38:06 jim Exp $

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
 * @see NodeRegisterReq
 */
public
enum NodeRequest
{  
  /**
   * An instance of {@link NodeRegisterReq NodeRegisterReq} is next.
   */
  Register, 

  // ...

  /**
   * No more requests will be send over this connection.
   */
  Shutdown;
}
