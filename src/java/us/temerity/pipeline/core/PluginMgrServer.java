// $Id: PluginMgrServer.java,v 1.1 2005/01/15 02:56:32 jim Exp $

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
/*   P L U G I N   M G R   S E R V E R                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The manager of Pipeline plugin classes. <P> 
 * 
 * This class handles network communication with {@link PluginMgrClient PluginMgrClient} 
 * instances running on remote hosts.  This class listens for new connections from  
 * <CODE>PluginMgrClient</CODE> instances and creats a thread to manage each connection.
 * Each of these threads then listens for requests for plugin related operations 
 * and dispatches these requests to an underlying instance of the {@link PluginMgr PluginMgr}
 * class.
 * 
 * @see PluginMgr
 * @see PluginMgrClient
 */
public
class PluginMgrServer
  extends Thread
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new plugin manager server.
   * 
   * @param port 
   *   The network port to monitor for incoming connections.
   */
  public
  PluginMgrServer
  (
   int port
  )
  { 
    super("PluginMgrServer");

    pPluginMgr = new PluginMgr();

    if(port < 0) 
      throw new IllegalArgumentException("Illegal port number (" + port + ")!");
    pPort = port;

    pShutdown = new AtomicBoolean(false);
    pTasks    = new HashSet<HandlerTask>();    
  }
  
  /** 
   * Construct a new file manager using the default root production directory and 
   * network port.
   * 
   * The root production directory and network port used are those specified by 
   * <B>plconfig(1)</B>.
   */
  public
  PluginMgrServer() 
  { 
    this(PackageInfo.sFilePort);
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
    ServerSocketChannel schannel = null;
    try {
      schannel = ServerSocketChannel.open();
      ServerSocket server = schannel.socket();
      InetSocketAddress saddr = new InetSocketAddress(pPort);
      server.bind(saddr, 100);
      
      Logs.net.fine("Listening on Port: " + pPort);
      Logs.net.info("Server Ready.");
      Logs.flush();

      schannel.configureBlocking(false);
      while(!pShutdown.get()) {
	SocketChannel channel = schannel.accept();
	if(channel != null) {
	  HandlerTask task = new HandlerTask(channel);
	  pTasks.add(task);
	  task.start();	
	}
	else {
	  Thread.sleep(PackageInfo.sServerSleep);
	}
      }

      try {
	Logs.net.finer("Shutting Down -- Waiting for tasks to complete...");
	Logs.flush();
	synchronized(pTasks) {
	  for(HandlerTask task : pTasks) 
	    task.closeConnection();
	}

	synchronized(pTasks) {
	  for(HandlerTask task : pTasks) 
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
    catch (Exception ex) {
      Logs.net.severe(ex.getMessage());
    }
    finally {
      if(schannel != null) {
	try {
	  schannel.close();
	}
	catch (IOException ex) {
	}
      }

      Logs.net.info("Server Shutdown.");    
      Logs.flush();  
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Handle an incoming connection from a <CODE>PluginMgrClient</CODE> instance.
   */
  private 
  class HandlerTask
    extends Thread
  {
    public 
    HandlerTask
    (
     SocketChannel channel
    ) 
    {
      super("PluginMgrServer:HandlerTask");
      pChannel = channel;
    }

    public void 
    run() 
    {
      try {
	pSocket = pChannel.socket();
	Logs.net.fine("Connection Opened: " + pSocket.getInetAddress());
	Logs.flush();

	boolean live = true;
	while(pSocket.isConnected() && live && !pShutdown.get()) {
	  InputStream in     = pSocket.getInputStream();
	  ObjectInput objIn  = new ObjectInputStream(in);
	  PluginRequest kind = (PluginRequest) objIn.readObject();
	  
	  OutputStream out    = pSocket.getOutputStream();
	  ObjectOutput objOut = new ObjectOutputStream(out);
	  
	  Logs.net.finer("Request [" + pSocket.getInetAddress() + "]: " + kind.name());	  
	  Logs.flush();

	  switch(kind) {
	  case Update:
	    {
	      PluginUpdateReq req = (PluginUpdateReq) objIn.readObject();
	      objOut.writeObject(pPluginMgr.update(req));
	      objOut.flush(); 
	    }
	    break;

	  case Install:
	    {
	      PluginInstallReq req = (PluginInstallReq) objIn.readObject();
	      objOut.writeObject(pPluginMgr.install(req));
	      objOut.flush(); 
	    }
	    break;

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
      catch(AsynchronousCloseException ex) {
      }
      catch (EOFException ex) {
	Logs.net.severe("Connection on port (" + pPort + ") terminated abruptly!");	
      }
      catch (IOException ex) {
	Logs.net.severe("IO problems on port (" + pPort + "):\n" + 
			ex.getMessage());
      }
      catch(ClassNotFoundException ex) {
	Logs.net.severe("Illegal object encountered on port (" + pPort + "):\n" + 
			ex.getMessage());	
      }
      catch (Exception ex) {
	Logs.net.severe(ex.getMessage());
      }
      finally {
	closeConnection();

	if(!pShutdown.get()) {
	  synchronized(pTasks) {
	    pTasks.remove(this);
	  }
	}
      }
    }

    public void 
    closeConnection() 
    {
      if(!pChannel.isOpen()) 
	return;

      try {
	pChannel.close();
      }
      catch(IOException ex) {
      }

      Logs.net.fine("Client Connection Closed.");
      Logs.flush();
    }
    
    private SocketChannel  pChannel; 
    private Socket         pSocket;     
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The shared plugin manager. 
   */
  private PluginMgr  pPluginMgr;

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

