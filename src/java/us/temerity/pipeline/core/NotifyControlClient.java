// $Id: NotifyControlClient.java,v 1.1 2004/04/11 19:25:29 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;

import java.io.*;
import java.net.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O T I F Y   C O N T R O L   C L I E N T                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * The client-side controller for directory change notification. <P> 
 * 
 * Provides control over the set of directories monitored by the directory change notification
 * daemon <A HREF="../../../../man/plnotify.html"><B>plnotify</B><A>(1) running on the file 
 * server. An instance of this class is used by the Pipeline master server daemon 
 * <A HREF="../../../../man/plmaster.html"><B>plmaster</B><A>(1) to communicate with 
 * <B>plnotify</B>(1).
 */
public
class NotifyControlClient
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
  NotifyControlClient
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
   * <CODE><B>--notify-control-port</B>=<I>num</I></CODE> options to <B>plconfig</B>(1).
   */
  public
  NotifyControlClient() 
  {
    super(PackageInfo.sFileServer, PackageInfo.sNotifyControlPort, 
	  NotifyRequest.Disconnect, NotifyRequest.Shutdown);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Begin monitoring the given directory.
   * 
   * @param dir
   *   The directory to monitor.
   */ 
  public synchronized void 
  monitor
  (
   File dir 
  ) 
    throws PipelineException 
  {
    verifyConnection();

    NotifyMonitorReq req = 
      new NotifyMonitorReq(dir);

    Object obj = performTransaction(NotifyRequest.Monitor, req);
    handleSimpleResponse(obj); 
  }

  /**
   * Cease monitoring the given directory.
   * 
   * @param dir
   *   The directory to quit monitoring.
   */ 
  public synchronized void 
  unmonitor
  (
   File dir 
  ) 
    throws PipelineException 
  {   
    verifyConnection();

    NotifyUnmonitorReq req = 
      new NotifyUnmonitorReq(dir);

    Object obj = performTransaction(NotifyRequest.Unmonitor, req);
    handleSimpleResponse(obj);  
  }

}

