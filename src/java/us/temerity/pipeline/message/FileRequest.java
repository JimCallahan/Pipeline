// $Id: FileRequest.java,v 1.4 2004/03/15 19:11:33 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.FileMgrClient;
import us.temerity.pipeline.FileMgrServer;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   R E Q U E S T                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The set of identifiers for file request messages which may be sent over a network 
 * connection from the {@link FileMgrClient FileMgrClient} to the 
 * {@link FileMgrServer FileMgrServer}.  <P> 
 * 
 * The protocol of communicaton between these file manager classes is for a 
 * <CODE>FileRequest</CODE> to be sent followed by a corresponding request
 * object which contains the needed request data.  The purpose of this enumeration is to 
 * make testing for which request is being send more efficient and cleaner on the 
 * <CODE>FileMgrServer</CODE> side of the connection.
 * 
 * @see FileCheckInReq
 * @see FileCheckOutReq
 * @see FileCheckSumReq
 * @see FileFreezeReq
 * @see FileUnfreezeReq
 * @see FileStateReq
 */
public
enum FileRequest
{  
  /**
   * An instance of {@link FileCheckInReq FileCheckInReq} is next.
   */
  CheckIn, 
  
  /**
   * An instance of {@link FileCheckOutReq FileCheckOutReq} is next.
   */
  CheckOut, 
  
  /**
   * An instance of {@link FileCheckSumReq FileCheckSumReq} is next.
   */
  CheckSum, 
  
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
   * No more requests will be send over this connection.
   */
  Shutdown;
}
