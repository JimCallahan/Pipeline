// $Id: NodeMgrServer.java,v 1.11 2004/04/24 22:41:22 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   M G R   S E R V E R                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The server-side manager of node queries and operations. <P> 
 * 
 * This class handles network communication with {@link NodeMgrClient NodeMgrClient} 
 * instances running on remote hosts.  This class listens for new connections from  
 * <CODE>NodeMgrClient</CODE> instances and creats a thread to manage each connection.
 * Each of these threads then listens for requests for file system related operations 
 * and dispatches these requests to an underlying instance of the {@link NodeMgr NodeMgr}
 * class.
 * 
 * @see NodeMgr
 * @see NodeMgrClient
 */
public
class NodeMgrServer
  extends Thread
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new node manager server.
   * 
   * @param nodeDir 
   *   The root node directory.
   * 
   * @param nodePort 
   *   The network port to monitor for incoming connections.
   * 
   * @param prodDir 
   *   The root production directory.
   * 
   * @param fileHostname 
   *   The name of the host running the <B>plfilemgr</B><A>(1) and <B>plnotify</B><A>(1) 
   *   daemons.
   * 
   * @param filePort 
   *   The network port listened to by the <B>plfilemgr</B><A>(1) daemon.
   * 
   * @param controlPort 
   *   The network port listened to by the <B>plnotify</B><A>(1) daemon for 
   *   control connections.
   * 
   * @param monitorPort 
   *   The network port listened to by the <B>plnotify</B><A>(1) daemon for 
   *   monitor connections.
   */
  public
  NodeMgrServer
  (
   File nodeDir, 
   int nodePort, 
   File prodDir, 
   String fileHostname, 
   int filePort,
   int controlPort, 
   int monitorPort
  )
  { 
    super("NodeMgrServer");
    init(nodeDir, nodePort, prodDir, fileHostname, filePort, controlPort, monitorPort);
  }
  
  /** 
   * Construct a new node manager using the default root node directory and 
   * network ports.
   * 
   * The root node directory is specified by the <B>--node-dir</B>=<I>dir</I>
   * option to <B>plconfig</B>(1). <P> 
   * 
   * The file server hostname is specified by the <B>--file-host</B>=<I>host</I>
   * option to <B>plconfig</B>(1). <P>  
   * 
   * The network ports used are those specified by the 
   * <B>--master-port</B>=<I>num</I>, <B>--file-port</B>=<I>num</I>, 
   * <B>--notify-control-port</B>=<I>num</I> and 
   * <B>--notify-monitor-port</B>=<I>num</I> options to <B>plconfig</B>(1).
   */
  public
  NodeMgrServer() 
  { 
    super("NodeMgrServer");
    init(PackageInfo.sNodeDir, PackageInfo.sMasterPort, 
	 PackageInfo.sProdDir, PackageInfo.sFileServer, PackageInfo.sFilePort, 
	 PackageInfo.sNotifyControlPort, PackageInfo.sNotifyMonitorPort);
  }


  /*-- CONSTRUCTION HELPERS ----------------------------------------------------------------*/

  /**
   * Initialize a new instance.
   * 
   * @param nodeDir 
   *   The root node directory.
   * 
   * @param nodePort 
   *   The network port to monitor for incoming connections.
   * 
   * @param prodDir 
   *   The root production directory.
   * 
   * @param fileHostname 
   *   The name of the host running the <B>plfilemgr</B><A>(1) and <B>plnotify</B><A>(1) 
   *   daemons.
   * 
   * @param filePort 
   *   The network port listened to by <B>plfilemgr</B><A>(1) daemon.
   * 
   * @param controlPort 
   *   The network port listened to by the <B>plnotify</B><A>(1) daemon for 
   *   control connections.
   * 
   * @param monitorPort 
   *   The network port listened to by the <B>plnotify</B><A>(1) daemon for 
   *   monitor connections.
   */
  private synchronized void 
  init
  (
   File nodeDir, 
   int nodePort, 
   File prodDir, 
   String fileHostname, 
   int filePort,
   int controlPort, 
   int monitorPort
  )
  { 
    pNodeMgr = new NodeMgr(nodeDir, prodDir, 
			   fileHostname, filePort, controlPort, monitorPort);

    if(nodePort < 0) 
      throw new IllegalArgumentException("Illegal port number (" + nodePort + ")!");
    pPort = nodePort;

    pShutdown = new AtomicBoolean(false);
    pTasks    = new HashSet<HandlerTask>();
  }

 

  /*----------------------------------------------------------------------------------------*/
  /*   T H R E A D   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Begin listening to the network port and spawn threads to process the file management 
   * requests received over the connection. <P>
   * 
   * This will only return if there is an unrecoverable error.
   */
  public void 
  run() 
  {
    ServerSocket server = null;
    try {
      server = new ServerSocket(pPort, 100);
      Logs.net.fine("Listening on Port: " + pPort);
      Logs.flush();

      server.setSoTimeout(PackageInfo.sServerTimeOut);

      while(!pShutdown.get()) {
	try {
	  Socket socket = server.accept();
	  
	  HandlerTask task = new HandlerTask(socket);
	  pTasks.add(task);
	  task.start();	
	}
	catch(SocketTimeoutException ex) {
	  //Logs.net.finest("Timeout: accept()");
	}
      }

      try {
	Logs.net.finer("Shutting Down -- Waiting for tasks to complete...");
	Logs.flush();
	for(HandlerTask task : pTasks) {
	  task.join();
	}
      }
      catch(InterruptedException ex) {
	Logs.net.severe("Interrupted while shutting down!");
	Logs.flush();
      }
    }
    catch (IOException ex) {
      Logs.net.severe("IO problems on port (" + pPort + "):\n" + 
		      ex.getMessage());
      Logs.flush();
    }
    catch (SecurityException ex) {
      Logs.net.severe("The Security Manager doesn't allow listening to sockets!\n" + 
		      ex.getMessage());
      Logs.flush();
    }
    finally {
      if(server != null) {
	try {
	  server.close();
	}
	catch (IOException ex) {
	}
      }

      pNodeMgr.shutdown();

      Logs.net.fine("Server Shutdown.");
      Logs.flush();
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Handle an incoming connection from a <CODE>NodeMgrClient</CODE> instance.
   */
  private 
  class HandlerTask
    extends Thread
  {
    public 
    HandlerTask
    (
     Socket socket
    ) 
    {
      super("NodeMgrServer:HandlerTask");
      pSocket = socket;
    }

    public void 
    run() 
    {
      try {
	Logs.net.fine("Connection Opened: " + pSocket.getInetAddress());
	Logs.flush();

	boolean live = true;
	while(pSocket.isConnected() && live && !pShutdown.get()) {
	  InputStream in    = pSocket.getInputStream();
	  ObjectInput objIn = new ObjectInputStream(in);
	  NodeRequest kind  = (NodeRequest) objIn.readObject();
	  
	  OutputStream out    = pSocket.getOutputStream();
	  ObjectOutput objOut = new ObjectOutputStream(out);
	  
	  Logs.net.finer("Request [" + pSocket.getInetAddress() + "]: " + kind.name());	  
	  Logs.flush();

	  switch(kind) {
	  case GetWorking:
	    {
	      NodeGetWorkingReq req = (NodeGetWorkingReq) objIn.readObject();
	      objOut.writeObject(pNodeMgr.getWorkingVersion(req));
	      objOut.flush(); 
	    }
	    break;

	  case ModifyProperties:
	    {
	      NodeModifyPropertiesReq req = (NodeModifyPropertiesReq) objIn.readObject();
	      objOut.writeObject(pNodeMgr.modifyProperties(req));
	      objOut.flush(); 
	    }
	    break;

	  case Link:
	    {
	      NodeLinkReq req = (NodeLinkReq) objIn.readObject();
	      objOut.writeObject(pNodeMgr.link(req));
	      objOut.flush(); 
	    }
	    break;

	  case Unlink:
	    {
	      NodeUnlinkReq req = (NodeUnlinkReq) objIn.readObject();
	      objOut.writeObject(pNodeMgr.unlink(req));
	      objOut.flush(); 
	    }
	    break;
	    
	  // ...

	    

	  case Status:
	    {
	      NodeStatusReq req = (NodeStatusReq) objIn.readObject();
	      objOut.writeObject(pNodeMgr.status(req));
	      objOut.flush(); 
	    }
	    break;

	  case Register:
	    {
	      NodeRegisterReq req = (NodeRegisterReq) objIn.readObject();
	      objOut.writeObject(pNodeMgr.register(req));
	      objOut.flush(); 
	    }
	    break;
	    
	  case Revoke:
	    {
	      NodeRevokeReq req = (NodeRevokeReq) objIn.readObject();
	      objOut.writeObject(pNodeMgr.revoke(req));
	      objOut.flush(); 
	    }
	    break;
	    
	  case Rename:
	    {
	      NodeRenameReq req = (NodeRenameReq) objIn.readObject();
	      objOut.writeObject(pNodeMgr.rename(req));
	      objOut.flush(); 
	    }
	    break;

	  case CheckIn:
	    {
	      NodeCheckInReq req = (NodeCheckInReq) objIn.readObject();
	      objOut.writeObject(pNodeMgr.checkIn(req));
	      objOut.flush(); 
	    }
	    break;
	    
	  case CheckOut:
	    {
	      NodeCheckOutReq req = (NodeCheckOutReq) objIn.readObject();
	      objOut.writeObject(pNodeMgr.checkOut(req));
	      objOut.flush(); 
	    }
	    break;
	    
	  // ...
	    

	  case Disconnect:
	    live = false;
	    break;

	  case Shutdown:
	    Logs.net.warning("Shutdown Request Received: " + pSocket.getInetAddress());
	    Logs.flush();
	    pShutdown.set(true);
	    break;	    

	  default:
	    assert(false);
	  }
	}
      }
      catch (IOException ex) {
	Logs.net.severe("IO problems on port (" + pPort + "):\n" + 
			ex.getMessage());
      }
      catch(ClassNotFoundException ex) {
	Logs.net.severe("Illegal object encountered on port (" + pPort + "):\n" + 
			ex.getMessage());	
      }
      finally {
	try {
	  pSocket.close();
	}
	catch(IOException ex) {
	}

	Logs.net.fine("Connection Closed: " + pSocket.getInetAddress());
	Logs.flush();

	if(!pShutdown.get()) {
	  synchronized(pTasks) {
	    pTasks.remove(this);
	  }
	}
      }
    }
    
    private Socket pSocket;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The shared node manager. 
   */
  private NodeMgr  pNodeMgr;

  /**
   * The network port number the server listens to for incoming connections.
   */
  private int  pPort;

  /**
   * Has the server been ordered to shutdown?
   */
  private AtomicBoolean  pShutdown;

  /**
   * The set of currently running tasks.
   */ 
  private HashSet<HandlerTask>  pTasks;
}

