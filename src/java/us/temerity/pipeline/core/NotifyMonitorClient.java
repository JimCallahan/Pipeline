// $Id: NotifyMonitorClient.java,v 1.1 2004/04/11 19:25:29 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;

import java.io.*;
import java.net.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O T I F Y  M O N I T O R   C L I E N T                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * The client-side monitor of directory changes notifications. <P> 
 * 
 * Provides a monitoring connection to the Pipeline directory change notification daemon 
 * <A HREF="../../../../man/plnotify.html"><B>plnotify</B><A>(1) running on the file 
 * server. An instance of this class is used by the Pipeline master server daemon 
 * <A HREF="../../../../man/plmaster.html"><B>plmaster</B><A>(1) to communicate with 
 * <B>plnotify</B>(1).
 */
public
class NotifyMonitorClient
  extends BaseMgrClient
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new client.
   * 
   * @param hostname 
   *   The name of the host running <B>plnotify</B><A>(1).
   * 
   * @param port 
   *   The network port listened to by <B>plnotify</B><A>(1) for control connections.
   */
  public
  NotifyMonitorClient
  ( 
   String hostname, 
   int port
  ) 
  {
    super(hostname, port, 
	  NotifyRequest.Disconnect, NotifyRequest.Shutdown);
  }

  /** 
   * Construct a new client using the default hostname and port. <P> 
   * 
   * The hostname and port used are those specified by the 
   * <CODE><B>--file-host</B>=<I>host</I></CODE> and 
   * <CODE><B>--notify-monitor-port</B>=<I>num</I></CODE> options to <B>plconfig</B>(1).
   */
  public
  NotifyMonitorClient() 
  {
    super(PackageInfo.sFileServer, PackageInfo.sNotifyMonitorPort, 
	  NotifyRequest.Disconnect, NotifyRequest.Shutdown);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Wait for one or more of the monitored directories to be modified. <P> 
   * 
   * This method blocks until at least one of the monitored directories is modified. Any
   * monitored directories modified before the connection is established will not be 
   * reported.  Once a connection has been established, any modification will be reported 
   * regardless of the duration of time between calls of the <CODE>watch</CODE> method. 
   * A modified directory will only be reported once for each call to <CODE>watch</CODE> 
   * regardless of the number of modification between each call. <P> 
   * 
   * No assumptions should be made as to the order of directory modification except that 
   * directories reported by previous calls have been modified before directories reported 
   * by subsequent calls from the same <CODE>NotifyMonitor</CODE> instance. If there are
   * more than one <CODE>NotifyMonitor</CODE> instances connected to the same server, a
   * single directory modification may be reported to more than one instance.  No assumptions
   * should be made about which instance receives notification first.
   * 
   * @return
   *   The modified directories. 
   */ 
  public synchronized HashSet<File>
  watch() 
    throws PipelineException 
  {
    verifyConnection();

    Object obj = performTransaction(NotifyRequest.Watch, new NotifyWatchReq());
    if(obj instanceof NotifyWatchRsp) {
      NotifyWatchRsp rsp = (NotifyWatchRsp) obj;
      return (rsp.getDirs());
    }
    else {
      handleFailure(obj);
      return null;
    }
  }

}

