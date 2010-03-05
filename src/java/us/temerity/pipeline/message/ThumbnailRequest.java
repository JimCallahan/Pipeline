// $Id: QueueRequest.java,v 1.36 2010/01/08 09:38:10 jim Exp $

package us.temerity.pipeline.message;

/*------------------------------------------------------------------------------------------*/
/*   T H U M B N A I L   R E Q U E S T                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The set of identifiers for thumbnail request messages which may be sent over a network 
 * connection from the <CODE>ThumbnailMgrClient</CODE> to instances to the 
 * <CODE>ThumbnailMgrServer</CODE>. <P> 
 * 
 * The protocol of communication between these thumbnail manager classes is for a 
 * <CODE>ThumbnailRequest</CODE> to be sent followed by a corresponding request
 * object which contains the needed request data.  The purpose of this enumeration is to 
 * make testing for which request is being send more efficient and cleaner on the 
 * <CODE>ThumbnailMgrServer</CODE> side of the connection.
 */
public
enum ThumbnailRequest
{
  /**
   * An instance of {@link ThumbnailRegisterImageReq} is next.
   */
  RegisterImage, 


  // ... 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Simple test of network connectivity.
   */ 
  Ping, 

  /**
   * No more requests will be send over this connection.
   */
  Disconnect,

  /**
   * An instance of {@link Thumbnail ShutdownOptionsReq} is next.
   */
  ShutdownOptions, 

  /**
   * Order the server to refuse any further requests and then to exit as soon as all
   * currently pending requests have be completed.
   */
  Shutdown;
 
}
