// $Id: QueueMgrServer.java,v 1.10 2004/08/30 01:39:23 jim Exp $

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
 * and {@link QueueMgrControlClient QueueMgrControlClient} instances running on remote hosts.
 * This class listens for new connections from clients and creats a thread to manage each 
 * connection.  Each of these threads then listens for requests for queue related operations 
 * and dispatches these requests to an underlying instance of the {@link QueueMgr QueueMgr}
 * class.
 * 
 * @see QueueMgr
 * @see QueueMgrClient
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
      Logs.net.info("Server Ready.");
      Logs.flush();

      server.setSoTimeout(PackageInfo.sServerTimeOut);

      CollectorTask collector = new CollectorTask();
      collector.start();

      DispatcherTask dispatcher = new DispatcherTask();
      dispatcher.start();

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

	collector.join();
	dispatcher.join();

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

      pQueueMgr.shutdown();

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
	  /*-- PRIVILEGED USER STATUS ------------------------------------------------------*/
	  case GetPrivilegedUsers:
	    {
	      objOut.writeObject(pQueueMgr.getPrivilegedUsers());
	      objOut.flush(); 
	    }
	    break;

	  case SetPrivilegedUsers:
	    {
	      MiscSetPrivilegedUsersReq req = (MiscSetPrivilegedUsersReq) objIn.readObject();
	      objOut.writeObject(pQueueMgr.setPrivilegedUsers(req));
	      objOut.flush(); 
	    }
	    break;


	  /*-- LICENSE KEYS ----------------------------------------------------------------*/
	  case GetLicenseKeyNames:
	    {
	      objOut.writeObject(pQueueMgr.getLicenseKeyNames());
	      objOut.flush(); 
	    }
	    break;

	  case GetLicenseKeys:
	    {
	      objOut.writeObject(pQueueMgr.getLicenseKeys());
	      objOut.flush(); 
	    }
	    break;

	  case AddLicenseKey:
	    {
	      QueueAddLicenseKeyReq req = 
		(QueueAddLicenseKeyReq) objIn.readObject();
	      objOut.writeObject(pQueueMgr.addLicenseKey(req));
	      objOut.flush(); 
	    }
	    break;

	  case RemoveLicenseKey:
	    {
	      QueueRemoveLicenseKeyReq req = 
		(QueueRemoveLicenseKeyReq) objIn.readObject();
	      objOut.writeObject(pQueueMgr.removeLicenseKey(req));
	      objOut.flush(); 
	    }
	    break;

	  case SetTotalLicenses:
	    {
	      QueueSetTotalLicensesReq req = 
		(QueueSetTotalLicensesReq) objIn.readObject();
	      objOut.writeObject(pQueueMgr.setTotalLicenses(req));
	      objOut.flush(); 
	    }
	    break;


	  /*-- SELECTION KEYS --------------------------------------------------------------*/
	  case GetSelectionKeyNames:
	    {
	      objOut.writeObject(pQueueMgr.getSelectionKeyNames());
	      objOut.flush(); 
	    }
	    break;

	  case GetSelectionKeys:
	    {
	      objOut.writeObject(pQueueMgr.getSelectionKeys());
	      objOut.flush(); 
	    }
	    break;

	  case AddSelectionKey:
	    {
	      QueueAddSelectionKeyReq req = 
		(QueueAddSelectionKeyReq) objIn.readObject();
	      objOut.writeObject(pQueueMgr.addSelectionKey(req));
	      objOut.flush(); 
	    }
	    break;

	  case RemoveSelectionKey:
	    {
	      QueueRemoveSelectionKeyReq req = 
		(QueueRemoveSelectionKeyReq) objIn.readObject();
	      objOut.writeObject(pQueueMgr.removeSelectionKey(req));
	      objOut.flush(); 
	    }
	    break;


	  /*-- JOB MANAGER HOSTS -----------------------------------------------------------*/
	  case GetHosts:
	    {
	      objOut.writeObject(pQueueMgr.getHosts());
	      objOut.flush(); 
	    }
	    break;

	  case AddHost:
	    {
	      QueueAddHostReq req = (QueueAddHostReq) objIn.readObject();
	      objOut.writeObject(pQueueMgr.addHost(req));
	      objOut.flush(); 
	    }
	    break;

	  case RemoveHosts:
	    {
	      QueueRemoveHostsReq req = (QueueRemoveHostsReq) objIn.readObject();
	      objOut.writeObject(pQueueMgr.removeHosts(req));
	      objOut.flush(); 
	    }
	    break;

	  case EditHosts:
	    {
	      QueueEditHostsReq req = (QueueEditHostsReq) objIn.readObject();
	      objOut.writeObject(pQueueMgr.editHosts(req));
	      objOut.flush(); 
	    }
	    break;

	  case GetHostResourceSamples:
	    {
	      QueueGetHostResourceSamplesReq req = 
		(QueueGetHostResourceSamplesReq) objIn.readObject();
	      objOut.writeObject(pQueueMgr.getHostResourceSamples(req));
	      objOut.flush(); 
	    }
	    break;


	  /*-- JOBS ------------------------------------------------------------------------*/
	  case GetJobStates:
	    {
	      QueueGetJobStatesReq req = (QueueGetJobStatesReq) objIn.readObject();
	      objOut.writeObject(pQueueMgr.getJobStates(req));
	      objOut.flush(); 
	    }
	    break; 

	  case GetJobStatus:
	    {
	      QueueGetJobStatusReq req = (QueueGetJobStatusReq) objIn.readObject();
	      objOut.writeObject(pQueueMgr.getJobStatus(req));
	      objOut.flush(); 
	    }
	    break;
	  
	  case GetJob:
	    {
	      QueueGetJobReq req = (QueueGetJobReq) objIn.readObject();
	      objOut.writeObject(pQueueMgr.getJob(req));
	      objOut.flush(); 
	    }
	    break;
	    
	  case GetJobInfo:
	    {
	      QueueGetJobInfoReq req = (QueueGetJobInfoReq) objIn.readObject();
	      objOut.writeObject(pQueueMgr.getJobInfo(req));
	      objOut.flush(); 
	    }
	    break;

	  case GetJobGroup:
	    {
	      QueueGetJobGroupReq req = (QueueGetJobGroupReq) objIn.readObject();
	      objOut.writeObject(pQueueMgr.getJobGroup(req));
	      objOut.flush(); 
	    }
	    break;

	  case GetJobGroups:
	    {
	      objOut.writeObject(pQueueMgr.getJobGroups());
	      objOut.flush(); 
	    }
	    break;

	  case SubmitJob:
	    {
	      QueueSubmitJobReq req = (QueueSubmitJobReq) objIn.readObject();
	      objOut.writeObject(pQueueMgr.submitJob(req));
	      objOut.flush(); 
	    }
	    break;
	    
	  case GroupJobs:
	    {
	      QueueGroupJobsReq req = (QueueGroupJobsReq) objIn.readObject();
	      objOut.writeObject(pQueueMgr.groupJobs(req));
	      objOut.flush(); 
	    }
	    break;
	    
	  case KillJobs:
	    {
	      QueueKillJobsReq req = (QueueKillJobsReq) objIn.readObject();
	      objOut.writeObject(pQueueMgr.killJobs(req));
	      objOut.flush(); 
	    }
	    break;

	  case PauseJobs:
	    {
	      QueuePauseJobsReq req = (QueuePauseJobsReq) objIn.readObject();
	      objOut.writeObject(pQueueMgr.pauseJobs(req));
	      objOut.flush(); 
	    }
	    break;

	  case ResumeJobs:
	    {
	      QueueResumeJobsReq req = (QueueResumeJobsReq) objIn.readObject();
	      objOut.writeObject(pQueueMgr.resumeJobs(req));
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
	InetAddress addr = pSocket.getInetAddress(); 
	String host = "?";
	if(addr != null) 
	  host = addr.getCanonicalHostName();

	Logs.net.severe("IO problems on connection from " + 
			"(" + host + ":" + pPort + "):\n" + 
			getFullMessage(ex));
      }
      catch(ClassNotFoundException ex) {
	InetAddress addr = pSocket.getInetAddress(); 
	String host = "?";
	if(addr != null) 
	  host = addr.getCanonicalHostName();

	Logs.net.severe("Illegal object encountered on connection from " + 
			"(" + host + ":" + pPort + "):\n" + 
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
    }
    
    private Socket pSocket;
  }
  

  /**
   * Collects per-host system resource information.
   */
  private 
  class CollectorTask
    extends Thread
  {
    public 
    CollectorTask() 
    {
      super("QueueMgrServer:CollectorTask");
    }

    public void 
    run() 
    {
      try {
	while(!pShutdown.get()) {
	  pQueueMgr.collector();
	}
      }
      catch (Exception ex) {
	Logs.net.severe(getFullMessage(ex));	
	Logs.flush();
      }
    }
  }

  /**
   * Assigns jobs to available hosts.
   */
  private 
  class DispatcherTask
    extends Thread
  {
    public 
    DispatcherTask() 
    {
      super("QueueMgrServer:DispatcherTask");
    }

    public void 
    run() 
    {
      try {
	while(!pShutdown.get()) {
	  pQueueMgr.dispatcher();
	}
      }
      catch (Exception ex) {
	Logs.net.severe(getFullMessage(ex));	
	Logs.flush();
      }
    }
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

