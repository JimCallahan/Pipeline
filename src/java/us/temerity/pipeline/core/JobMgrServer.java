// $Id: JobMgrServer.java,v 1.31 2008/02/14 20:26:29 jim Exp $

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
/*   Q U E U E   M G R   S E R V E R                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * The server-side manager of job queries and operations. <P> 
 * 
 * This class handles network communication with {@link JobMgrClient JobMgrClient},
 * {@link JobMgrControlClient} and {@link JobMgrPlgControlClient} instances running on 
 * remote hosts.  This class listens for new connections from clients and creats a thread to 
 * manage each connection.  Each of these threads then listens for requests for job related 
 * operations and dispatches these requests to an underlying instance of the {@link JobMgr}
 * class.
 */
class JobMgrServer
  extends BaseMgrServer
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new job manager server.
   */
  public
  JobMgrServer()
  { 
    super("JobMgrServer");

    pTimer = new TaskTimer();

    pJobMgr = new JobMgr();
    pTasks  = new HashSet<HandlerTask>();
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
    ServerSocketChannel schannel = null;
    try {
      schannel = ServerSocketChannel.open();
      ServerSocket server = schannel.socket();
      InetSocketAddress saddr = new InetSocketAddress(PackageInfo.sJobPort);
      server.bind(saddr, 100);

      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Fine,
	 "Listening on Port: " + PackageInfo.sJobPort);
      pTimer.suspend();
      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Info,
	 "Server Ready.\n" + 
	 "  Started in " + TimeStamps.formatInterval(pTimer.getTotalDuration()));
      LogMgr.getInstance().flush();
      pTimer = new TaskTimer();

      CollectorTask collector = null;
      if(PackageInfo.sOsType == OsType.Unix) {
	collector = new CollectorTask();
	collector.start();
      }

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

      pJobMgr.killAll();

      try {
	if(collector != null) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Net, LogMgr.Level.Info,
	     "Waiting on Collector...");
	  LogMgr.getInstance().flush();

	  collector.join();
	}

	{
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Net, LogMgr.Level.Info,
	     "Waiting on Client Handlers...");
	  LogMgr.getInstance().flush();

	  synchronized(pTasks) {
	    for(HandlerTask task : pTasks) 
	      task.closeConnection();
	  }	
	
	  synchronized(pTasks) {
	    for(HandlerTask task : pTasks) 
	      task.join();
	  }
	}
      }
      catch(InterruptedException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Severe,
	   "Interrupted while shutting down!");
	LogMgr.getInstance().flush();
      }
    }
    catch (IOException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Severe,
         Exceptions.getFullMessage
	 ("IO problems on port (" + PackageInfo.sJobPort + "):", ex)); 
      LogMgr.getInstance().flush();  
    }
    catch (SecurityException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Severe,
         Exceptions.getFullMessage
	 ("The Security Manager doesn't allow listening to sockets!", ex)); 
      LogMgr.getInstance().flush();  
    }
    catch (Exception ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Severe,
	 Exceptions.getFullMessage(ex));
      LogMgr.getInstance().flush();  
    }
    finally {
      if(schannel != null) {
	try {
	  schannel.close();
	}
	catch (IOException ex) {
	}
      }
      
      PluginMgrClient.getInstance().disconnect();

      pTimer.suspend();
      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Info,
	 "Server Shutdown.\n" + 
	 "  Uptime " + TimeStamps.formatInterval(pTimer.getTotalDuration()));
      LogMgr.getInstance().flush();  
    }  
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
     SocketChannel channel
    ) 
    {
      super("JobMgrServer:HandlerTask");
      pChannel = channel;
    }

    public void 
    run() 
    {
      try {
	pSocket = pChannel.socket();
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Fine,
	   "Connection Opened: " + pSocket.getInetAddress());
	LogMgr.getInstance().flush();

	boolean first = true;
	boolean live = true;
	while(pSocket.isConnected() && live && !pShutdown.get()) {
	  InputStream in     = pSocket.getInputStream();
	  ObjectInput objIn  = new PluginInputStream(in);
	  Object obj         = objIn.readObject();

	  OutputStream out    = pSocket.getOutputStream();
	  ObjectOutput objOut = new ObjectOutputStream(out);
	  
	  if(first) {
	    String sinfo = 
	      ("Pipeline-" + PackageInfo.sVersion + " [" + PackageInfo.sRelease + "]");
	    
	    objOut.writeObject(sinfo);
	    objOut.flush(); 
	    
	    String cinfo = "Unknown"; 
	    if(obj instanceof String) 
	      cinfo = (String) obj;
	    
	    if(!sinfo.equals(cinfo)) {
	      LogMgr.getInstance().log
		(LogMgr.Kind.Net, LogMgr.Level.Warning,
		 "Connection from (" + pSocket.getInetAddress() + ") rejected due to a " + 
		 "mismatch in Pipeline release versions!\n" + 
		 "  Client = " + cinfo + "\n" +
		 "  Server = " + sinfo);	      
	      
	      live = false;
	    }
	      
	    first = false;
	  }
	  else {
            /* check time difference between client and server */ 
            checkTimeSync((Long) obj, pSocket); 

            /* dispatch request by kind */ 
	    JobRequest kind = (JobRequest) objIn.readObject();
	  
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Net, LogMgr.Level.Finer,
	       "Request [" + pSocket.getInetAddress() + "]: " + kind.name());	  
	    LogMgr.getInstance().flush();

	    switch(kind) {
	    /*-- EDITING -------------------------------------------------------------------*/
	    case EditAs:           
	      {
		JobEditAsReq req = (JobEditAsReq) objIn.readObject();
		objOut.writeObject(pJobMgr.editAs(req));
		objOut.flush(); 
	      }
	      break;


	    /*-- HOST RESOURCES ------------------------------------------------------------*/
	    case GetResources:
	      {
		objOut.writeObject(pJobMgr.getResources());
		objOut.flush(); 
	      }
	      break;

	    case GetOsType:
	      {
		objOut.writeObject(pJobMgr.getOsType());
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

	    
	    /*-- JOB EXECUTION -------------------------------------------------------------*/
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


	    /*-- JOB MANAGEMENT ------------------------------------------------------------*/
	    case CleanupResources:
	      {
		JobCleanupResourcesReq req = (JobCleanupResourcesReq) objIn.readObject();
		objOut.writeObject(pJobMgr.cleanupResources(req));
		objOut.flush(); 
	      }
	      break;

	    case CleanupPreemptedResources:
	      {
		JobCleanupPreemptedResourcesReq req = 
		  (JobCleanupPreemptedResourcesReq) objIn.readObject();
		objOut.writeObject(pJobMgr.cleanupPreemptedResources(req));
		objOut.flush(); 
	      }
	      break;

	    
	    /*-- EXEC DETAILS --------------------------------------------------------------*/
	    case GetExecDetails:
	      {
		JobGetExecDetailsReq req = (JobGetExecDetailsReq) objIn.readObject();
		objOut.writeObject(pJobMgr.getExecDetails(req));
		objOut.flush(); 
	      }
	      break;


	    /*-- JOB OUTPUT ----------------------------------------------------------------*/
	    case GetNumStdOutLines:
	      {
		JobGetNumStdOutLinesReq req = (JobGetNumStdOutLinesReq) objIn.readObject();
		objOut.writeObject(pJobMgr.getNumStdOutLines(req));
		objOut.flush(); 
	      }
	      break;

	    case GetStdOutLines:
	      {
		JobGetStdOutLinesReq req = (JobGetStdOutLinesReq) objIn.readObject();
		objOut.writeObject(pJobMgr.getStdOutLines(req));
		objOut.flush(); 
	      }
	      break;

	    case CloseStdOut:
	      {
		JobCloseStdOutReq req = (JobCloseStdOutReq) objIn.readObject();
		objOut.writeObject(pJobMgr.closeStdOut(req));
		objOut.flush(); 
	      }
	      break;


	    case GetNumStdErrLines:
	      {
		JobGetNumStdErrLinesReq req = (JobGetNumStdErrLinesReq) objIn.readObject();
		objOut.writeObject(pJobMgr.getNumStdErrLines(req));
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

	    case CloseStdErr:
	      {
		JobCloseStdErrReq req = (JobCloseStdErrReq) objIn.readObject();
		objOut.writeObject(pJobMgr.closeStdErr(req));
		objOut.flush(); 
	      }
	      break;

	    
	    /*-- NETWORK CONNECTION --------------------------------------------------------*/
	    case Disconnect:
	      live = false;
	      break;

	    case Shutdown:
	      LogMgr.getInstance().log
		(LogMgr.Kind.Net, LogMgr.Level.Warning,
		 "Shutdown Request Received: " + pSocket.getInetAddress());
	      LogMgr.getInstance().flush();
	      pShutdown.set(true);
	      break;	    

	    default:
	      throw new IllegalStateException("Unknown request ID (" + kind + ")!"); 
	    }
	  }
	}
      }
      catch(AsynchronousCloseException ex) {
      }
      catch (EOFException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Severe,
	   "Connection on port (" + PackageInfo.sJobPort + ") terminated abruptly!");	
      }
      catch (IOException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Severe,
           Exceptions.getFullMessage
	   ("IO problems on port (" + PackageInfo.sJobPort + "):", ex)); 
      }
      catch(ClassNotFoundException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Severe,
           Exceptions.getFullMessage
	   ("Illegal object encountered on port (" + PackageInfo.sJobPort + "):", ex)); 
      }
      catch (Exception ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Severe,
	   Exceptions.getFullMessage(ex));	
      }
      finally {
	closeConnection();

	if(!pShutdown.get()) {
	  synchronized(pTasks) {
	    pTasks.remove(this);
	  }
	}
      }

      LogMgr.getInstance().flush();
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

      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Fine,
	 "Client Connection Closed.");
      LogMgr.getInstance().flush();
    }

    private SocketChannel  pChannel; 
    private Socket         pSocket;
  }
  

  /**
   * Collects pre-proccess resource usage statistics.
   */
  private 
  class CollectorTask
    extends Thread
  {
    public 
    CollectorTask() 
    {
      super("JobMgrServer:CollectorTask");
    }

    public void 
    run() 
    {
      if(PackageInfo.sOsType != OsType.Unix)
	throw new IllegalStateException("The OS type must be Unix!");

      try {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Fine,
	   "Collector Started.");	
	LogMgr.getInstance().flush();
	
	while(!pShutdown.get()) {
	  pJobMgr.collector();
	}
      }
      catch (Exception ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Severe,
	   Exceptions.getFullMessage("Collector Failed:", ex)); 
	LogMgr.getInstance().flush();	
      }
      finally {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Fine,
	   "Collector Finished.");	
	LogMgr.getInstance().flush();
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Times server startup and uptime.
   */ 
  private TaskTimer  pTimer; 

  /**
   * The shared job manager. 
   */
  private JobMgr  pJobMgr;

  /**
   * The set of currently running tasks.
   */ 
  private HashSet<HandlerTask>  pTasks;
}

