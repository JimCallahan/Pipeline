// $Id: QueueMgrServer.java,v 1.28 2006/01/05 16:54:43 jim Exp $

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
 * The server-side manager of queue queries and operations. <P> 
 * 
 * This class handles network communication with {@link QueueMgrClient QueueMgrClient} 
 * and {@link QueueMgrControlClient QueueMgrControlClient} instances running on remote hosts.
 * This class listens for new connections from clients and creats a thread to manage each 
 * connection.  Each of these threads then listens for requests for queue related operations 
 * and dispatches these requests to an underlying instance of the {@link QueueMgr QueueMgr}
 * class.
 */
class QueueMgrServer
  extends Thread
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new queue manager server.
   */
  public
  QueueMgrServer()
  { 
    super("QueueMgrServer");

    pQueueMgr = new QueueMgr();

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
    ServerSocketChannel schannel = null;
    try {
      schannel = ServerSocketChannel.open();
      ServerSocket server = schannel.socket();
      InetSocketAddress saddr = new InetSocketAddress(PackageInfo.sQueuePort);
      server.bind(saddr, 100);

      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Fine,
	 "Listening on Port: " + PackageInfo.sQueuePort);
      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Info,
	 "Server Ready.");
      LogMgr.getInstance().flush();

      CollectorTask collector = new CollectorTask();
      collector.start();

      DispatcherTask dispatcher = new DispatcherTask();
      dispatcher.start();

      SchedulerTask scheduler = new SchedulerTask();
      scheduler.start();

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
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Finer,
	   "Shutting Down -- Waiting for tasks to complete...");
	LogMgr.getInstance().flush();

	collector.join();
	dispatcher.join();
	scheduler.join();

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
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Severe,
	   "Interrupted while shutting down!");
	LogMgr.getInstance().flush();
      }
    }
    catch (IOException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Severe,
	 "IO problems on port (" + PackageInfo.sQueuePort + "):\n" + 
	 getFullMessage(ex));
      LogMgr.getInstance().flush();
    }
    catch (SecurityException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Severe,
	 "The Security Manager doesn't allow listening to sockets!\n" + 
	 getFullMessage(ex));
      LogMgr.getInstance().flush();
    }
    catch (Exception ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Severe,
	 getFullMessage(ex));
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
      pQueueMgr.shutdown();

      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Info,
	 "Server Shutdown."); 
      LogMgr.getInstance().flush();  
    }
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
     SocketChannel channel
    ) 
    {
      super("QueueMgrServer:HandlerTask");
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
	    QueueRequest kind = (QueueRequest) obj;
	  
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Net, LogMgr.Level.Finer,
	       "Request [" + pSocket.getInetAddress() + "]: " + kind.name());	  
	    LogMgr.getInstance().flush();
	  
	    switch(kind) {
	    /*-- PRIVILEGED USER STATUS ----------------------------------------------------*/
	    case GetPrivilegedUsers:
	      {
		objOut.writeObject(pQueueMgr.getPrivilegedUsers());
		objOut.flush(); 
	      }
	      break;

	    case SetPrivilegedUsers:
	      {
		MiscSetPrivilegedUsersReq req = 
		  (MiscSetPrivilegedUsersReq) objIn.readObject();
		objOut.writeObject(pQueueMgr.setPrivilegedUsers(req));
		objOut.flush(); 
	      }
	      break;


	    /*-- LICENSE KEYS --------------------------------------------------------------*/
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

	    case SetMaxLicenses:
	      {
		QueueSetMaxLicensesReq req = 
		  (QueueSetMaxLicensesReq) objIn.readObject();
		objOut.writeObject(pQueueMgr.setMaxLicenses(req));
		objOut.flush(); 
	      }
	      break;


	    /*-- SELECTION KEYS ------------------------------------------------------------*/
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


	    /*-- SELECTION GROUPS ----------------------------------------------------------*/
	    case GetSelectionGroupNames:
	      {
		objOut.writeObject(pQueueMgr.getSelectionGroupNames());
		objOut.flush(); 
	      }
	      break;

	    case GetSelectionGroups:
	      {
		objOut.writeObject(pQueueMgr.getSelectionGroups());
		objOut.flush(); 
	      }
	      break;

	    case AddSelectionGroup:
	      {
		QueueAddSelectionGroupReq req = 
		  (QueueAddSelectionGroupReq) objIn.readObject();
		objOut.writeObject(pQueueMgr.addSelectionGroup(req));
		objOut.flush(); 
	      }
	      break;

	    case RemoveSelectionGroups:
	      {
		QueueRemoveSelectionGroupsReq req = 
		  (QueueRemoveSelectionGroupsReq) objIn.readObject();
		objOut.writeObject(pQueueMgr.removeSelectionGroups(req));
		objOut.flush(); 
	      }
	      break;

	    case EditSelectionGroups:
	      {
		QueueEditSelectionGroupsReq req = 
		  (QueueEditSelectionGroupsReq) objIn.readObject();
		objOut.writeObject(pQueueMgr.editSelectionGroups(req));
		objOut.flush(); 
	      }
	      break;


	    /*-- SELECTION SCHEDULES -------------------------------------------------------*/
	    case GetSelectionScheduleNames:
	      {
		objOut.writeObject(pQueueMgr.getSelectionScheduleNames());
		objOut.flush(); 
	      }
	      break;

	    case GetSelectionSchedules:
	      {
		objOut.writeObject(pQueueMgr.getSelectionSchedules());
		objOut.flush(); 
	      }
	      break;

	    case AddSelectionSchedule:
	      {
		QueueAddSelectionScheduleReq req = 
		  (QueueAddSelectionScheduleReq) objIn.readObject();
		objOut.writeObject(pQueueMgr.addSelectionSchedule(req));
		objOut.flush(); 
	      }
	      break;

	    case RemoveSelectionSchedules:
	      {
		QueueRemoveSelectionSchedulesReq req = 
		  (QueueRemoveSelectionSchedulesReq) objIn.readObject();
		objOut.writeObject(pQueueMgr.removeSelectionSchedules(req));
		objOut.flush(); 
	      }
	      break;

	    case EditSelectionSchedules:
	      {
		QueueEditSelectionSchedulesReq req = 
		  (QueueEditSelectionSchedulesReq) objIn.readObject();
		objOut.writeObject(pQueueMgr.editSelectionSchedules(req));
		objOut.flush(); 
	      }
	      break;


	    /*-- JOB MANAGER HOSTS ---------------------------------------------------------*/
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


	    /*-- JOBS ----------------------------------------------------------------------*/
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
	  
	    case GetRunningJobStatus:
	      {
		objOut.writeObject(pQueueMgr.getRunningJobStatus());
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
	    
	    case GetRunningJobInfo:
	      {
		objOut.writeObject(pQueueMgr.getRunningJobInfo());
		objOut.flush(); 
	      }
	      break;


	    case SubmitJobs:
	      {
		QueueSubmitJobsReq req = (QueueSubmitJobsReq) objIn.readObject();
		objOut.writeObject(pQueueMgr.submitJobs(req));
		objOut.flush(); 
	      }
	      break;
	    
	    case PreemptJobs:
	      {
		QueuePreemptJobsReq req = (QueuePreemptJobsReq) objIn.readObject();
		objOut.writeObject(pQueueMgr.preemptJobs(req));
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


	    case GroupJobs:
	      {
		QueueGroupJobsReq req = (QueueGroupJobsReq) objIn.readObject();
		objOut.writeObject(pQueueMgr.groupJobs(req));
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
	    
	    case DeleteJobGroups:
	      {
		QueueDeleteJobGroupsReq req = (QueueDeleteJobGroupsReq) objIn.readObject();
		objOut.writeObject(pQueueMgr.deleteJobGroups(req));
		objOut.flush(); 
	      }
	      break;

	    case DeleteViewJobGroups:
	      {
		QueueDeleteViewJobGroupsReq req = 
		  (QueueDeleteViewJobGroupsReq) objIn.readObject();
		objOut.writeObject(pQueueMgr.deleteViewJobGroups(req));
		objOut.flush(); 
	      }
	      break;
	    
	    case DeleteAllJobGroups:
	      {
		objOut.writeObject(pQueueMgr.deleteAllJobGroups());
		objOut.flush(); 
	      }
	      break;
	    


	    /*-- NETWORK CONNECTION --------------------------------------------------------*/
	    case Disconnect:
	      live = false;
	      break;

	    case ShutdownOptions:
	      {
		QueueShutdownOptionsReq req = (QueueShutdownOptionsReq) objIn.readObject();
		pQueueMgr.setShutdownOptions(req.shutdownJobMgrs());
	      }

	    case Shutdown:
	      LogMgr.getInstance().log
		(LogMgr.Kind.Net, LogMgr.Level.Warning,
		 "Shutdown Request Received: " + pSocket.getInetAddress());
	      LogMgr.getInstance().flush();
	      pShutdown.set(true);
	      break;	    

	    default:
	      assert(false);
	    }
	  }
	}
      }
      catch(AsynchronousCloseException ex) {
      }
      catch (EOFException ex) {
	InetAddress addr = pSocket.getInetAddress(); 
	String host = "???";
	if(addr != null) 
	  host = addr.getCanonicalHostName();
	
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Severe,
	   "Connection from (" + host + ":" + PackageInfo.sQueuePort + ") " + 
	   "terminated abruptly!");
      }
      catch (IOException ex) {
	InetAddress addr = pSocket.getInetAddress(); 
	String host = "???";
	if(addr != null) 
	  host = addr.getCanonicalHostName();

	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Severe,
	   "IO problems on connection from " + 
	   "(" + host + ":" + PackageInfo.sQueuePort + "):\n" + 
	   getFullMessage(ex));
      }
      catch(ClassNotFoundException ex) {
	InetAddress addr = pSocket.getInetAddress(); 
	String host = "???";
	if(addr != null) 
	  host = addr.getCanonicalHostName();

	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Severe,
	   "Illegal object encountered on connection from " + 
	   "(" + host + ":" + PackageInfo.sQueuePort + "):\n" + 
	   getFullMessage(ex));
      }
      catch (Exception ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Severe,
	   getFullMessage(ex));
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

      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Fine,
	 "Client Connection Closed.");
      LogMgr.getInstance().flush();
    }
    
    private SocketChannel  pChannel; 
    private Socket         pSocket;
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
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Fine,
	   "Collector Started.");	
	LogMgr.getInstance().flush();

	while(!pShutdown.get()) {
	  pQueueMgr.collector();
	}
      }
      catch (Exception ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Severe,
	   "Collector Failed: " + getFullMessage(ex));	
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
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Fine,
	   "Dispatcher Started.");	
	LogMgr.getInstance().flush();

	while(!pShutdown.get()) {
	  pQueueMgr.dispatcher();
	}
      }
      catch (Exception ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Severe,
	   "Dispatcher Failed: " + getFullMessage(ex));	
	LogMgr.getInstance().flush();
      }
      finally {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Fine,
	   "Dispatcher Finished.");	
	LogMgr.getInstance().flush();
      }
    }
  }

  /**
   * Assigns selection groups to hosts. 
   */
  private 
  class SchedulerTask
    extends Thread
  {
    public 
    SchedulerTask() 
    {
      super("QueueMgrServer:SchedulerTask");
    }

    public void 
    run() 
    {
      try {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Fine,
	   "Scheduler Started.");	
	LogMgr.getInstance().flush();

	while(!pShutdown.get()) {
	  pQueueMgr.scheduler();
	}
      }
      catch (Exception ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Severe,
	   "Scheduler Failed: " + getFullMessage(ex));	
	LogMgr.getInstance().flush();
      }
      finally {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Fine,
	   "Scheduler Finished.");	
	LogMgr.getInstance().flush();
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
   * Has the server been ordered to shutdown?
   */
  private AtomicBoolean  pShutdown;

  /**
   * The set of currently running tasks.
   */ 
  private HashSet<HandlerTask>  pTasks;
}

