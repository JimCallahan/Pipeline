// $Id: JobMgrServer.java,v 1.6 2004/08/30 01:39:23 jim Exp $

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
 * The server-side manager of job queries and operations. <P> 
 * 
 * This class handles network communication with {@link JobMgrClient JobMgrClient} 
 * and {@link JobMgrControlClient JobMgrControlClient} instances running on remote hosts.  
 * This class listens for new connections from clients and creats a thread to manage each 
 * connection.  Each of these threads then listens for requests for job related operations 
 * and dispatches these requests to an underlying instance of the {@link JobMgr JobMgr}
 * class.
 * 
 * @see JobMgr
 * @see JobMgrClient
 * @see JobMgrControlClient
 */
public
class JobMgrServer
  extends Thread
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new job manager server.
   * 
   * @param port 
   *   The network port to monitor for incoming connections.
   */
  public
  JobMgrServer
  (
   int port
  )
  { 
    super("JobMgrServer");
    init(port);
  }
  
  /** 
   * Construct a new job manager using the given network port.
   * 
   * The network port used is that specified by <B>plconfig(1)</B>.
   */
  public
  JobMgrServer() 
  { 
    super("JobMgrServer");
    init(PackageInfo.sJobPort);
  }


  /*-- CONSTRUCTION HELPERS ----------------------------------------------------------------*/

  /**
   * Initialize a new instance.
   * 
   * @param port 
   *   The network port to monitor for incoming connections.
   */ 
  private synchronized void 
  init
  (
   int port
  )
  { 
    pJobMgr = new JobMgr();

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
   * Begin listening to the network port and spawn threads to process the job management 
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
      Logs.net.info("Server Ready.");
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

      pJobMgr.killAll();

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
		      getFullMessage(ex));
    }
    catch (SecurityException ex) {
      Logs.net.severe("The Security Manager doesn't allow listening to sockets!\n" + 
		      getFullMessage(ex));
    }
    catch (Exception ex) {
      Logs.net.severe(getFullMessage(ex));
    }
    finally {
      if(server != null) {
	try {
	  server.close();
	}
	catch (IOException ex) {
	}
      }

      Logs.net.info("Server Shutdown.");  
    }  

    Logs.flush();  
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Generate a string containing both the exception message and stack trace. 
   * 
   * @param ex 
   *   The thrown exception.   
   */ 
  private String 
  getFullMessage
  (
   Throwable ex
  ) 
  {
    StringBuffer buf = new StringBuffer();
     
    if(ex.getMessage() != null) 
      buf.append(ex.getMessage() + "\n\n"); 	
    else if(ex.toString() != null) 
      buf.append(ex.toString() + "\n\n"); 	
      
    buf.append("Stack Trace:\n");
    StackTraceElement stack[] = ex.getStackTrace();
    int wk;
    for(wk=0; wk<stack.length; wk++) 
      buf.append("  " + stack[wk].toString() + "\n");
   
    return (buf.toString());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Handle an incoming connection from a <CODE>JobMgrClient</CODE> instance.
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
      super("JobMgrServer:HandlerTask");
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
	  JobRequest kind  = (JobRequest) objIn.readObject();
	  
	  OutputStream out    = pSocket.getOutputStream();
	  ObjectOutput objOut = new ObjectOutputStream(out);
	  
	  Logs.net.finer("Request [" + pSocket.getInetAddress() + "]: " + kind.name());	  
	  Logs.flush();

	  switch(kind) {
          /*-- HOST RESOURCES --------------------------------------------------------------*/
	  case GetResources:
	    {
	      objOut.writeObject(pJobMgr.getResources());
	      objOut.flush(); 
	    }
	    break;

	  case GetNumProcessors:
	    {
	      objOut.writeObject(pJobMgr.getNumProcessors());
	      objOut.flush(); 
	    }
	    break;

	  case GetTotalMemory:
	    {
	      objOut.writeObject(pJobMgr.getTotalMemory());
	      objOut.flush(); 
	    }
	    break;

	  case GetTotalDisk:
	    {
	      objOut.writeObject(pJobMgr.getTotalDisk());
	      objOut.flush(); 
	    }
	    break;

	    
          /*-- JOB EXECUTION ---------------------------------------------------------------*/
	  case Start:
	    {
	      JobStartReq req = (JobStartReq) objIn.readObject();
	      objOut.writeObject(pJobMgr.jobStart(req));
	      objOut.flush(); 
	    }
	    break;

	  case Kill:
	    {
	      JobKillReq req = (JobKillReq) objIn.readObject();
	      objOut.writeObject(pJobMgr.jobKill(req));
	      objOut.flush(); 
	    }
	    break;

	  case Wait:
	    {
	      JobWaitReq req = (JobWaitReq) objIn.readObject();
	      objOut.writeObject(pJobMgr.jobWait(req));
	      objOut.flush(); 
	    }
	    break;

	    
	  /*-- JOB OUTPUT ------------------------------------------------------------------*/
	  case GetStdOutLines:
	    {
	      JobGetStdOutLinesReq req = (JobGetStdOutLinesReq) objIn.readObject();
	      objOut.writeObject(pJobMgr.getStdOutLines(req));
	      objOut.flush(); 
	    }
	    break;

	  case GetStdErrLines:
	    {
	      JobGetStdErrLinesReq req = (JobGetStdErrLinesReq) objIn.readObject();
	      objOut.writeObject(pJobMgr.getStdErrLines(req));
	      objOut.flush(); 
	    }
	    break;

	    
	  /*-- NETWORK CONNECTION ----------------------------------------------------------*/
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
			getFullMessage(ex));
      }
      catch(ClassNotFoundException ex) {
	Logs.net.severe("Illegal object encountered on port (" + pPort + "):\n" + 
			getFullMessage(ex));	
      }
      catch (Exception ex) {
	Logs.net.severe(getFullMessage(ex));	
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

      Logs.flush();
    }
    
    private Socket pSocket;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The shared job manager. 
   */
  private JobMgr  pJobMgr;

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

