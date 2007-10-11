// $Id: QueueMgrServer.java,v 1.48 2007/10/11 18:52:07 jesse Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;
import us.temerity.pipeline.core.exts.*;

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
  extends BaseMgrServer
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new queue manager server.
   * 
   * @param rebuild
   *   Whether to ignore existing lock files.
   * 
   * @param collectorBatchSize
   *   The maximum number of job servers per collection sub-thread.
   * 
   * @param dispatcherInterval
   *   The minimum time a cycle of the dispatcher loop should take (in milliseconds).
   * 
   * @param nfsCacheInterval
   *   The minimum time to wait before attempting a NFS directory attribute lookup operation
   *   after a file in the directory has been created by another host on the network 
   *   (in milliseconds).  Should be set to be slightly longer than the the NFS (acdirmax) 
   *   mount option for the root production directory on the host running the Queue Manager.
   */
  public
  QueueMgrServer
  (
   boolean rebuild, 
   int collectorBatchSize,
   long dispatcherInterval, 
   long nfsCacheInterval
  ) 
  { 
    super("QueueMgrServer");

    pTimer = new TaskTimer();
    pQueueMgr = new QueueMgr(this, rebuild, 
                             collectorBatchSize, dispatcherInterval, nfsCacheInterval);
    pTasks = new HashSet<HandlerTask>();
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
      LogMgr.getInstance().flush();

      CollectorTask collector = new CollectorTask();
      DispatcherTask dispatcher = new DispatcherTask();
      SchedulerTask scheduler = new SchedulerTask();

      MasterConnectTask connector = 
	new MasterConnectTask(collector, dispatcher, scheduler); 
      connector.start();

      HeapStatsTask heapStats = new HeapStatsTask();
      heapStats.start();

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
	heapStats.interrupt();
	heapStats.join();

	{
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Ops, LogMgr.Level.Info,
	     "Waiting on Collector...");
	  LogMgr.getInstance().flush();

	  collector.join();
	}

	{
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Ops, LogMgr.Level.Info,
	     "Waiting on Dispatcher...");
	  LogMgr.getInstance().flush();

	  dispatcher.join();
	}

	scheduler.interrupt();
	scheduler.join();

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

      pTimer.suspend();
      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Info,
	 "Server Shutdown.\n" + 
	 "  Uptime " + TimeStamps.formatInterval(pTimer.getTotalDuration()));
      LogMgr.getInstance().flush();  
    }
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S H U T D O W N                                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Shutdown the Queue Manager due to an internal failure.
   */ 
  public void 
  internalShutdown()
  {
    pShutdown.set(true);
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
            /* check time difference between client and server */  
            checkTimeSync((Long) obj, pSocket); 

            /* dispatch request by kind */ 
	    QueueRequest kind = (QueueRequest) objIn.readObject();
	  
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Net, LogMgr.Level.Finer,
	       "Request [" + pSocket.getInetAddress() + "]: " + kind.name());	  
	    LogMgr.getInstance().flush();
	  
	    switch(kind) {
	    /*-- ADMINISTRATIVE PRIVILEGES -------------------------------------------------*/
	    case UpdateAdminPrivileges: 
	      {
		MiscUpdateAdminPrivilegesReq req = 
		  (MiscUpdateAdminPrivilegesReq) objIn.readObject();
		objOut.writeObject(pQueueMgr.updateAdminPrivileges(req));
		objOut.flush(); 
	      }
	      break;


	    /*-- LOGGING -------------------------------------------------------------------*/
	    case GetLogControls:
	      {
		objOut.writeObject(pQueueMgr.getLogControls());
		objOut.flush(); 
	      }
	      break;

	    case SetLogControls:
	      {
		MiscSetLogControlsReq req = (MiscSetLogControlsReq) objIn.readObject();
		objOut.writeObject(pQueueMgr.setLogControls(req));
		objOut.flush(); 
	      }
	      break;


	    /*-- RUNTIME PARAMETERS --------------------------------------------------------*/
	    case GetQueueControls:
	      {
		objOut.writeObject(pQueueMgr.getRuntimeControls());
		objOut.flush(); 
	      }
	      break;
	      
	    case SetQueueControls:
	      {
		QueueSetQueueControlsReq req = (QueueSetQueueControlsReq) objIn.readObject();
		objOut.writeObject(pQueueMgr.setRuntimeControls(req));
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


	    /*-- SERVER EXTENSIONS ---------------------------------------------------------*/
	    case GetQueueExtension:
	      {
		objOut.writeObject(pQueueMgr.getQueueExtensions());
		objOut.flush(); 
	      }
	      break;
	    
	    case RemoveQueueExtension:
	      {
		QueueRemoveQueueExtensionReq req = 
		  (QueueRemoveQueueExtensionReq) objIn.readObject();
		objOut.writeObject(pQueueMgr.removeQueueExtension(req));
		objOut.flush();
	      }
	      break;

	    case SetQueueExtension:
	      {
		QueueSetQueueExtensionReq req = 
		  (QueueSetQueueExtensionReq) objIn.readObject();
		objOut.writeObject(pQueueMgr.setQueueExtension(req));
		objOut.flush();
	      }
	      break;


	    /*-- JOB MANAGER HOSTS ---------------------------------------------------------*/
	    case GetHosts:
	      {
		QueueGetHostsReq req = (QueueGetHostsReq) objIn.readObject();
		objOut.writeObject(pQueueMgr.getHosts(req));
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

	    case GetHostHistograms:
	      {
		QueueGetHostHistogramsReq req = 
		  (QueueGetHostHistogramsReq) objIn.readObject();
		objOut.writeObject(pQueueMgr.getHostHistograms(req));
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

	    case GetUnfinishedJobsForNodes:
	      {
		QueueGetUnfinishedJobsForNodesReq req = 
		  (QueueGetUnfinishedJobsForNodesReq) objIn.readObject();
		objOut.writeObject(pQueueMgr.getUnfinishedJobsForNodes(req));
		objOut.flush(); 
	      }
	      break;

	    case GetUnfinishedJobsForNodeFiles:
	      {
		QueueGetUnfinishedJobsForNodeFilesReq req = 
		  (QueueGetUnfinishedJobsForNodeFilesReq) objIn.readObject();
		objOut.writeObject(pQueueMgr.getUnfinishedJobsForNodeFiles(req));
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
		QueueJobsReq req = (QueueJobsReq) objIn.readObject();
		objOut.writeObject(pQueueMgr.preemptJobs(req));
		objOut.flush(); 
	      }
	      break;

	    case KillJobs:
	      {
		QueueJobsReq req = (QueueJobsReq) objIn.readObject();
		objOut.writeObject(pQueueMgr.killJobs(req));
		objOut.flush(); 
	      }
	      break;

	    case PauseJobs:
	      {
		QueueJobsReq req = (QueueJobsReq) objIn.readObject();
		objOut.writeObject(pQueueMgr.pauseJobs(req));
		objOut.flush(); 
	      }
	      break;

	    case ResumeJobs:
	      {
		QueueJobsReq req = (QueueJobsReq) objIn.readObject();
		objOut.writeObject(pQueueMgr.resumeJobs(req));
		objOut.flush(); 
	      }
	      break;
	      
	    case ChangeJobReqs:
	      {
		QueueJobReqsReq req = (QueueJobReqsReq) objIn.readObject();
	        objOut.writeObject(pQueueMgr.changeJobReqs(req));
	        objOut.flush();
	      }
	      break;


	    case PreemptNodeJobs:
	      {
		QueueNodeJobsReq req = (QueueNodeJobsReq) objIn.readObject();
		objOut.writeObject(pQueueMgr.preemptNodeJobs(req));
		objOut.flush(); 
	      }
	      break;

	    case KillNodeJobs:
	      {
		QueueNodeJobsReq req = (QueueNodeJobsReq) objIn.readObject();
		objOut.writeObject(pQueueMgr.killNodeJobs(req));
		objOut.flush(); 
	      }
	      break;

	    case PauseNodeJobs:
	      {
		QueueNodeJobsReq req = (QueueNodeJobsReq) objIn.readObject();
		objOut.writeObject(pQueueMgr.pauseNodeJobs(req));
		objOut.flush(); 
	      }
	      break;

	    case ResumeNodeJobs:
	      {
		QueueNodeJobsReq req = (QueueNodeJobsReq) objIn.readObject();
		objOut.writeObject(pQueueMgr.resumeNodeJobs(req));
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
		PrivilegedReq req = (PrivilegedReq) objIn.readObject();
		objOut.writeObject(pQueueMgr.deleteAllJobGroups(req));
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
	      throw new IllegalStateException("Unknown request ID (" + kind + ")!"); 
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
   * Establish connection back to plmaster(1) for toolset lookup purposes before
   * starting the other threads.
   */
  private 
  class MasterConnectTask
    extends Thread
  {
    public 
    MasterConnectTask
    (
     CollectorTask collector,
     DispatcherTask dispatcher,
     SchedulerTask scheduler
    ) 
    {
      super("QueueMgrServer:MasterConnectTask"); 
      pCollector = collector;
      pDispatcher = dispatcher;
      pScheduler = scheduler;
    }

    public void 
    run() 
    {
      try {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Info,
	   "Establishing Network Connections [MasterMgr]...");
	LogMgr.getInstance().flush();
   
	pQueueMgr.establishMasterConnection();

	pTimer.suspend();
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Info,
	   "Server Ready.\n" + 
	   "  Started in " + TimeStamps.formatInterval(pTimer.getTotalDuration()));
	LogMgr.getInstance().flush();
	pTimer = new TaskTimer();
	
	pCollector.start();
	pDispatcher.start();
	pScheduler.start();
      }
      catch (Exception ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Severe,
	   "Master Connector Failed: " + getFullMessage(ex));	
	LogMgr.getInstance().flush();
      }
    }

    private CollectorTask  pCollector;
    private DispatcherTask pDispatcher;
    private SchedulerTask  pScheduler;
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
	  (LogMgr.Kind.Col, LogMgr.Level.Fine,
	   "Collector Started.");	
	LogMgr.getInstance().flush();

	while(!pShutdown.get()) {
	  pQueueMgr.collector();
	}
      }
      catch (Exception ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Col, LogMgr.Level.Severe,
	   "Collector Failed: " + getFullMessage(ex));	
	LogMgr.getInstance().flush();	
      }
      finally {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Col, LogMgr.Level.Fine,
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
	  (LogMgr.Kind.Dsp, LogMgr.Level.Fine,
	   "Dispatcher Started.");	
	LogMgr.getInstance().flush();

	pQueueMgr.establishMasterConnection();

	while(!pShutdown.get()) {
	  pQueueMgr.dispatcher();
	}
      }
      catch (Exception ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Dsp, LogMgr.Level.Severe,
	   "Dispatcher Failed: " + getFullMessage(ex));	
	LogMgr.getInstance().flush();
      }
      finally {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Dsp, LogMgr.Level.Fine,
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
	  (LogMgr.Kind.Ops, LogMgr.Level.Fine,
	   "Scheduler Started.");	
	LogMgr.getInstance().flush();

	while(!pShutdown.get()) {
	  pQueueMgr.scheduler();
	}
      }
      catch (Exception ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Severe,
	   "Scheduler Failed: " + getFullMessage(ex));	
	LogMgr.getInstance().flush();
      }
      finally {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Fine,
	   "Scheduler Finished.");	
	LogMgr.getInstance().flush();
      }
    }
  }

  /**
   * Heap statistics reporting.
   */
  private 
  class HeapStatsTask
    extends Thread
  {
    public 
    HeapStatsTask() 
    {
      super("QueueMgrServer:HeapStatsTask");
    }

    public void 
    run() 
    {
      try {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Mem, LogMgr.Level.Fine,
	   "JVM Memory Statistics Started.");	
	LogMgr.getInstance().flush();

	while(!pShutdown.get()) {
	  pQueueMgr.heapStats();
	}
      }
      catch (Exception ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Mem, LogMgr.Level.Severe,
	   "JVM Memory Statistics Failed: " + getFullMessage(ex));	
	LogMgr.getInstance().flush();	
      }
      finally {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Mem, LogMgr.Level.Fine,
	   "JVM Memory Statistics Finished.");	
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
   * The shared queue manager. 
   */
  private QueueMgr  pQueueMgr;

  /**
   * The set of currently running tasks.
   */ 
  private HashSet<HandlerTask>  pTasks;
}

