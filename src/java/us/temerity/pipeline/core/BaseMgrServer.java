// $Id: BaseMgrServer.java,v 1.5 2009/02/13 04:51:08 jlee Exp $

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
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Base inner class to handle incoming connection from subclass BaseMgrClient instances.
   */
  protected
  class BaseHandlerTask
    extends Thread
  {
    public
    BaseHandlerTask
    (
     String name
    )
    {
      super(name);

      pFirst = true;
      pLive  = true;

      pServerRsp = "OK";
    }

    protected boolean
    isFirst()
    {
      return pFirst;
    }

    protected boolean
    isLive()
    {
      return pLive;
    }

    protected void
    disconnect()
    {
      pLive = false;
    }

    protected void
    verifyConnection
    (
     Object obj, 
     ObjectOutput objOut
    )
      throws IOException
    {
      String clientMsg = "";

      if(obj instanceof String)
	clientMsg = (String) obj;

      String[] parts = clientMsg.split(BaseMgrClient.sVerifyConnectionMessageDelim);

      if(parts.length != 2) {
	pServerRsp = 
	  "Connection from (" + pSocket.getInetAddress() + ") rejected due to " + 
	  "an invalid message format.  Expected: (Pipeline version+release)" + 
	  BaseMgrClient.sVerifyConnectionMessageDelim + "clientID.\n" + 
	  "Receieved: " + clientMsg;

	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Warning, 
           pServerRsp);

	pLive = false;
      }
      else {
	String cinfo    = parts[0];
	String clientID = parts[1];
	
	String sinfo = 
	  ("Pipeline-" + PackageInfo.sVersion + " [" + PackageInfo.sRelease + "]");

	if(!sinfo.equals(cinfo)) {
	  pServerRsp = 
	    "Connection from (" + pSocket.getInetAddress() + ") rejected due to a " + 
	    "mismatch in Pipeline release versions!\n" + 
	    "  Client = " + cinfo + "\n" + 
	    "  Server = " + sinfo;

	  LogMgr.getInstance().log
	    (LogMgr.Kind.Net, LogMgr.Level.Warning, 
	     pServerRsp);

	  pLive = false;
	}

	if(pLive)
	  verifyClient(clientID);
      }

      objOut.writeObject(pServerRsp);
      objOut.flush();

      pFirst = false;
    }

    protected void
    verifyClient
    (
     String clientID
    )
    {
    }

    private boolean  pFirst;
    private boolean  pLive;

    protected String  pServerRsp;

    protected SocketChannel  pChannel;
    protected Socket         pSocket;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Has the server been ordered to shutdown?
   */
  protected AtomicBoolean  pShutdown;

}

