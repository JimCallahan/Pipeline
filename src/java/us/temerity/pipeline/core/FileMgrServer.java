// $Id: FileMgrServer.java,v 1.8 2004/04/12 22:36:43 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   M G R   S E R V E R                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The server-side manager of file system queries and operations. <P> 
 * 
 * This class handles network communication with {@link FileMgrClient FileMgrClient} 
 * instances running on remote hosts.  This class listens for new connections from  
 * <CODE>FileMgrClient</CODE> instances and creats a thread to manage each connection.
 * Each of these threads then listens for requests for file system related operations 
 * and dispatches these requests to an underlying instance of the {@link FileMgr FileMgr}
 * class.
 * 
 * @see FileMgr
 * @see FileMgrClient
 */
public
class FileMgrServer
  extends Thread
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new file manager server.
   * 
   * @param dir 
   *   The root production directory.
   * 
   * @param port 
   *   The network port to monitor for incoming connections.
   */
  public
  FileMgrServer
  (
   File dir, 
   int port
  )
  { 
    init(dir, port);
  }
  
  /** 
   * Construct a new file manager using the default root production directory and 
   * network port.
   * 
   * The root production directory and network port used are those specified by 
   * <B>plconfig(1)</B>.
   */
  public
  FileMgrServer() 
  { 
    init(PackageInfo.sProdDir, PackageInfo.sFilePort);
  }


  /*-- CONSTRUCTION HELPERS ----------------------------------------------------------------*/

  private synchronized void 
  init
  (
   File dir, 
   int port
  )
  { 
    pFileMgr = new FileMgr(dir);

    if(port < 0) 
      throw new IllegalArgumentException("Illegal port number (" + port + ")!");
    pPort = port;

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

      Logs.net.fine("Server Shutdown.");    
      Logs.flush();  
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
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Handle an incoming connection from a <CODE>FileMgrClient</CODE> instance.
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
	  FileRequest kind  = (FileRequest) objIn.readObject();
	  
	  OutputStream out    = pSocket.getOutputStream();
	  ObjectOutput objOut = new ObjectOutputStream(out);
	  
	  Logs.net.finer("Request [" + pSocket.getInetAddress() + "]: " + kind.name());	  
	  Logs.flush();

	  switch(kind) {
	  case CheckIn:
	    {
	      FileCheckInReq req = (FileCheckInReq) objIn.readObject();
	      objOut.writeObject(pFileMgr.checkIn(req));
	      objOut.flush(); 
	    }
	    break;

	  case CheckOut:
	    {
	      FileCheckOutReq req = (FileCheckOutReq) objIn.readObject();
	      objOut.writeObject(pFileMgr.checkOut(req));
	      objOut.flush(); 
	    }
	    break;
	    
	  case Freeze:
	    {
	      FileFreezeReq req = (FileFreezeReq) objIn.readObject();
	      objOut.writeObject(pFileMgr.freeze(req));
	      objOut.flush(); 
	    }
	    break;

	  case Unfreeze:
	    {
	      FileUnfreezeReq req = (FileUnfreezeReq) objIn.readObject();
	      objOut.writeObject(pFileMgr.unfreeze(req));
	      objOut.flush(); 
	    }
	    break;
	    
	  case State:
	    {
	      FileStateReq req = (FileStateReq) objIn.readObject();
	      objOut.writeObject(pFileMgr.states(req));
	      objOut.flush(); 
	    }
	    break;
	    
	  case Remove:
	    {
	      FileRemoveReq req = (FileRemoveReq) objIn.readObject();
	      objOut.writeObject(pFileMgr.remove(req));
	      objOut.flush(); 
	    }
	    break;
	    
	  case Rename:
	    {
	      FileRenameReq req = (FileRenameReq) objIn.readObject();
	      objOut.writeObject(pFileMgr.rename(req));
	      objOut.flush(); 
	    }
	    break;

	  case Disconnect:
	    live = false;
	    break;

	  case Shutdown:
	    Logs.net.info("Shutdown Request Received: " + pSocket.getInetAddress());
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
   * The shared file manager. 
   */
  private FileMgr  pFileMgr;

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

