// $Id: NotifyRequest.java,v 1.2 2004/05/21 21:17:51 jim Exp $

package us.temerity.pipeline.message;

/*------------------------------------------------------------------------------------------*/
/*   N O T I F Y  R E Q U E S T                                                             */
/*------------------------------------------------------------------------------------------*/

/**
 * The set of identifiers for request messages which may be sent over a network 
 * connection from the <CODE>NotifyControlClient</CODE> or <CODE>NotifyMonitorClient</CODE> 
 * to the <CODE>NotifyServer</CODE>.  <P> 
 */
public
enum NotifyRequest
{  
  /**
   * An instance of {@link NotifyMonitorReq NotifyMonitorReq} is next.
   */
  Monitor, 

  /**
   * An instance of {@link NotifyUnmonitorReq NotifyUnmonitorReq} is next.
   */
  Unmonitor, 

  /**
   * An instance of {@link NotifyWatchReq NotifyWatchReq} is next. 
   */
  Watch, 

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
