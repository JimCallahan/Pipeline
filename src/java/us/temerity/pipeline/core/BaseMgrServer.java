// $Id: BaseMgrServer.java,v 1.4 2008/02/14 20:26:29 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   M G R   S E R V E R                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Base class of all manager daemon server classes.
 */
class BaseMgrServer
  extends Thread
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new queue manager server.
   */
  public
  BaseMgrServer
  (
   String name
  )
  { 
    super(name); 

    pShutdown = new AtomicBoolean(false);
  }
 


  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Check time difference between client and server 
   */ 
  protected void 
  checkTimeSync
  (
   Long stamp, 
   Socket socket
  ) 
  {
    long deltaT = stamp - System.currentTimeMillis();

    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Tim, LogMgr.Level.Finer)) {
      LogMgr.getInstance().log
        (LogMgr.Kind.Tim, LogMgr.Level.Finer,
         "Time Delta [" + socket.getInetAddress() + "]: " + deltaT);
    }

    if(Math.abs(deltaT) > PackageInfo.sMaxClockDelta) 
      LogMgr.getInstance().log
        (LogMgr.Kind.Tim, LogMgr.Level.Warning,
         "The clock on client [" + socket.getInetAddress() + "] is out-of-sync with the " + 
         "clock on this server by (" + deltaT + ") milliseconds!\n" + 
         "This is likely a symptom a broken or misconfigured NTP service and should be " + 
         "fixed immediately!");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Has the server been ordered to shutdown?
   */
  protected AtomicBoolean  pShutdown;

}

