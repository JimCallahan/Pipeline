// $Id: FileMgrServer.java,v 1.4 2004/03/15 19:07:59 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.message.*;

import java.io.*;
import java.net.*;
import java.util.*;

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
 * @see FileMgrClient
 * @see FileMgr
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
   * @param dir [<B>in</B>]
   *   The root production directory.
   * 
   * @param port [<B>in</B>]
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
   * Construct a new file manager.
   * 
   * The root production directory is set by the <CODE>--with-prod=DIR</CODE> 
   * option to <I>configure(1)</I>.
   * 
   * The network port is set by the <CODE>--with-file-port=DIR</CODE> 
   * option to <I>configure(1)</I>.
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
    try {
      ServerSocket server = new ServerSocket(pPort, 100);
      Logs.net.fine("Listening on Port: " + pPort);
      Logs.flush();

      while(true) {
	Socket socket = server.accept();
	HandlerTask task = new HandlerTask(socket);
	task.start();	
      }
    }
    catch (IOException ex) {
      Logs.net.severe("IO problems on port (" + pPort + "):\n" + 
		      ex.getMessage());
    }
    catch (SecurityException ex) {
      Logs.net.severe("The Security Manager doesn't allow listening to sockets!\n" + 
		      ex.getMessage());
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
	while(pSocket.isConnected() && live) {
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
	    break;
	    
	  case CheckSum:
	    {
	      FileCheckSumReq req = (FileCheckSumReq) objIn.readObject();
	      objOut.writeObject(pFileMgr.refreshCheckSums(req));
	      objOut.flush(); 
	    }
	    break;
	    
	  case Freeze:
	  case Unfreeze:
	    break;
	    
	  case State:
	    {
	      FileStateReq req = (FileStateReq) objIn.readObject();
	      objOut.writeObject(pFileMgr.computeFileStates(req));
	      objOut.flush(); 
	    }
	    break;
	    
	  case Shutdown:
	    live = false;
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
  
}

