// $Id: FileRequest.java,v 1.2 2004/03/12 23:08:23 jim Exp $

package us.temerity.pipeline;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   R E Q U E S T                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The set of identifiers for file request messages which may be sent over a network 
 * connection from the {@link FileMgrClient FileMgrClient} to the 
 * {@link FileMgrServer FileMgrServer}.  <P> 
 * 
 * The protocol of communicaton between these file manager classes is for a 
 * <CODE>FileRequest</CODE> to be sent followed by a corresponding <CODE>File*Req</CODE> 
 * object which contains the needed request data.  The purpose of this enumeration is to 
 * make testing for which request is being send more efficient and cleaner on the 
 * <CODE>FileMgrServer</CODE> side of the connection.
 * 
 * @see FileCheckInReq
 * @see FileCheckOutReq
 * @see FileCheckSumReq
 * @see FileFreezeReq
 * @see FileUnfreezeReq
 * @see FileStateRsp
 */
public
enum FileRequest
{  
  /**
   * An instance of <CODE>FileCheckInReq</CODE> is next.
   */
  CheckIn, 
  
  /**
   * An instance of <CODE>FileCheckOutReq</CODE> is next.
   */
  CheckOut, 
  
  /**
   * An instance of <CODE>FileCheckSumReq</CODE> is next.
   */
  CheckSum, 
  
  /**
   * An instance of <CODE>FileFreezeReq</CODE> is next.
   */
  Freeze, 
  
  /**
   * An instance of <CODE>FileUnfreezeReq</CODE> is next.
   */
  Unfreeze, 
  
  /**
   * An instance of <CODE>FileStateReq</CODE> is next.
   */
  State, 

  /**
   * No more requests will be send over this connection.
   */
  Shutdown;
}
