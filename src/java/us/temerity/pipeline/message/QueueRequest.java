// $Id: QueueRequest.java,v 1.2 2004/07/24 18:25:44 jim Exp $

package us.temerity.pipeline.message;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   R E Q U E S T                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * The set of identifiers for queue request messages which may be sent over a network 
 * connection from the <CODE>QueueMgrClient</CODE> and <CODE>QueueMgrFullClient</CODE>
 * instances to the <CODE>QueueMgrServer</CODE>. <P> 
 * 
 * The protocol of communication between these queue manager classes is for a 
 * <CODE>QueueRequest</CODE> to be sent followed by a corresponding request
 * object which contains the needed request data.  The purpose of this enumeration is to 
 * make testing for which request is being send more efficient and cleaner on the 
 * <CODE>QueueMgrServer</CODE> side of the connection.
 */
public
enum QueueRequest
{
  /**
   * Get the names of the license keys.
   */ 
  GetLicenseKeyNames,

  /**
   * Get the license keys.
   */ 
  GetLicenseKeys,

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
   * No more requests will be send over this connection.
   */
  Disconnect,

  /**
   * Order the server to refuse any further requests and then to exit as soon as all
   * currently pending requests have be completed.
   */
  Shutdown;
 
}
