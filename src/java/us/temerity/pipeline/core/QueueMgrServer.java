// $Id: QueueMgrServer.java,v 1.1 2004/07/21 07:15:01 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   M G R   S E R V E R                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * The server-side manager of queue queries and operations. <P> 
 * 
 * This class handles network communication with {@link QueueMgrClient QueueMgrClient} 
 * and {@link QueueMgrFullClient QueueMgrFullClient} instances running on remote hosts.  
 * This class listens for new connections from clients and creats a thread to manage each 
 * connection.  Each of these threads then listens for requests for queue related operations 
 * and dispatches these requests to an underlying instance of the {@link QueueMgr QueueMgr}
 * class.
 * 
 * @see QueueMgr
 * @see QueueMgrClient
 * @see QueueMgrFullClient
 */
public
class QueueMgrServer
  extends Thread
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new queue manager server.
   * 
   * @param dir 
   *   The root queue directory.
   * 
   * @param port 
   *   The network port to monitor for incoming connections.
   * 
   * @param jobPort 
   *   The network port listened to by the <B>pljobmgr</B><A>(1) daemons.
   */
  public
  QueueMgrServer
  (
   File dir, 
   int port,
   int jobPort
  )
  { 
    super("QueueMgrServer");
    init(dir, port, jobPort);
  }
  
  /** 
   * Construct a new queue manager using the default root queue directory and network
   * ports.
   * 
   * The root queue directory and network ports used are those specified by 
   * <B>plconfig(1)</B>.
   */
  public
  QueueMgrServer() 
  { 
    super("QueueMgrServer");
    init(PackageInfo.sQueueDir, PackageInfo.sQueuePort, PackageInfo.sJobPort);
  }


  /*-- CONSTRUCTION HELPERS ----------------------------------------------------------------*/

  /**
   * Initialize a new instance.
   * 
   * @param dir 
   *   The root queue directory.
   * 
   * @param port 
   *   The network port to monitor for incoming connections.
   * 
   * @param jobPort 
   *   The network port listened to by the <B>pljobmgr</B><A>(1) daemons.
   */ 
  private synchronized void 
  init
  (
   File dir, 
   int port, 
   int jobPort   
  )
  { 
    pQueueMgr = new QueueMgr(dir, jobPort);

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
   * Begin listening to the network port and spawn threads to process the queue management 
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
   * Handle an incoming connection from a <CODE>QueueMgrClient</CODE> instance.
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
      super("QueueMgrServer:HandlerTask");
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
	  QueueRequest kind  = (QueueRequest) objIn.readObject();
	  
	  OutputStream out    = pSocket.getOutputStream();
	  ObjectOutput objOut = new ObjectOutputStream(out);
	  
	  Logs.net.finer("Request [" + pSocket.getInetAddress() + "]: " + kind.name());	  
	  Logs.flush();

	  switch(kind) {

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
   * The shared queue manager. 
   */
  private QueueMgr  pQueueMgr;

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

