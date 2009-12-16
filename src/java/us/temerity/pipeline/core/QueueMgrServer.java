// $Id: QueueMgrServer.java,v 1.79 2009/12/16 04:13:33 jesse Exp $

package us.temerity.pipeline.core;

import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;

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
   * @param jobReaderThreads
   *   The number of job reader threads to spawn.
   * 
   * @param controls
   *   The runtime controls.
   */
  public
  QueueMgrServer
  (
   boolean rebuild, 
   Integer jobReaderThreads,
   QueueControls controls
  ) 
    throws PipelineException 
  { 
    super("QueueMgrServer");

    pTimer = new TaskTimer();
    pQueueMgr = new QueueMgr(this, rebuild, jobReaderThreads, controls); 
    pTasks = new TreeSet<HandlerTask>();
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
  @Override
  public void 
  run() 
  {
    try {
      pSocketChannel = ServerSocketChannel.open();
      ServerSocket server = pSocketChannel.socket();
      InetSocketAddress saddr = new InetSocketAddress(PackageInfo.sQueuePort);
      server.bind(saddr, 100);

      LogMgr.getInstance().logAndFlush
	(LogMgr.Kind.Net, LogMgr.Level.Fine,
	 "Listening on Port: " + PackageInfo.sQueuePort);

      CollectorTask collector = new CollectorTask();
      DispatcherTask dispatcher = new DispatcherTask();
      SchedulerTask scheduler = new SchedulerTask();
      WriterTask writer = new WriterTask();
      BalancerTask balancer = new BalancerTask();
      BackupSyncTask backupSync = new BackupSyncTask();

      MasterConnectTask connector = 
	new MasterConnectTask(collector, dispatcher, scheduler, writer, balancer, backupSync); 
      connector.start();

      HeapStatsTask heapStats = new HeapStatsTask();
      heapStats.start();

      while(!pShutdown.get()) {
        try {
          HandlerTask task = new HandlerTask(pSocketChannel.accept()); 
          synchronized(pTasks) {
            pTasks.add(task);
          }
          task.start();	
        }
        catch(AsynchronousCloseException ex) {
        }
        catch(ClosedChannelException ex) {
        }
      }

      try {
	heapStats.interrupt();
	heapStats.join();
	
	{
          LogMgr.getInstance().logAndFlush
            (LogMgr.Kind.Ops, LogMgr.Level.Info,
             "Waiting on Balancer...");

          balancer.interrupt();
          balancer.join();
        }

	{
	  LogMgr.getInstance().logAndFlush
	    (LogMgr.Kind.Ops, LogMgr.Level.Info,
	     "Waiting on Collector...");

	  collector.join();
	}

	{
	  LogMgr.getInstance().logAndFlush
	    (LogMgr.Kind.Ops, LogMgr.Level.Info,
	     "Waiting on Dispatcher...");

	  dispatcher.join();
	}
	
	{
          LogMgr.getInstance().logAndFlush
            (LogMgr.Kind.Ops, LogMgr.Level.Info,
             "Waiting on Writer...");

          writer.interrupt();
          writer.join();
        }

	scheduler.interrupt();
	scheduler.join();

	{
	  LogMgr.getInstance().logAndFlush
	    (LogMgr.Kind.Ops, LogMgr.Level.Info,
	     "Waiting on Database Backup Synchronizer...");
          
          backupSync.interrupt();
          backupSync.join();
        }

	{
	  LogMgr.getInstance().logAndFlush
	    (LogMgr.Kind.Net, LogMgr.Level.Info,
	     "Waiting on Client Handlers...");
	  
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
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Net, LogMgr.Level.Severe,
	   "Interrupted while shutting down!");
      }
    }
    catch (IOException ex) {
      LogMgr.getInstance().logAndFlush
	(LogMgr.Kind.Net, LogMgr.Level.Severe,
         Exceptions.getFullMessage
	 ("IO problems on port (" + PackageInfo.sQueuePort + "):", ex)); 
    }
    catch (SecurityException ex) {
      LogMgr.getInstance().logAndFlush
	(LogMgr.Kind.Net, LogMgr.Level.Severe,
         Exceptions.getFullMessage
	 ("The Security Manager doesn't allow listening to sockets!", ex)); 
    }
    catch (Exception ex) {
      LogMgr.getInstance().logAndFlush
	(LogMgr.Kind.Net, LogMgr.Level.Severe,
	 Exceptions.getFullMessage(ex));
    }
    finally {
      if(pSocketChannel != null) {
	try {
          ServerSocket socket = pSocketChannel.socket(); 
          if(socket != null) 
            socket.close();
          pSocketChannel.close();
	}
	catch (IOException ex) {
	}
      }

      PluginMgrClient.getInstance().disconnect();
      
      {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Ops, LogMgr.Level.Info,
           "Writing Any Jobs Remaining in Cache...");
        pQueueMgr.writer(false);
      }

      pQueueMgr.shutdown();

      pTimer.suspend();
      LogMgr.getInstance().logAndFlush
	(LogMgr.Kind.Net, LogMgr.Level.Info,
	 "Server Shutdown.\n" + 
	 "  Uptime " + TimeStamps.formatInterval(pTimer.getTotalDuration()));
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
    extends BaseHandlerTask
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

    @Override
    protected String
    verifyClient
    (
     String clientID
    )
    {
      if(!clientID.equals("QueueMgr")) {
	String serverRsp = 
	  "Connection from (" + pSocket.getInetAddress() + ") rejected " + 
	  " due to invalid client ID (" + clientID + ")";

	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Warning, 
	   serverRsp);

	disconnect();

	return serverRsp;
      }
      else {
	return "OK";
      }
    }

    @Override
    public void 
    run() 
    {
      try {
	pSocket = pChannel.socket();
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Net, LogMgr.Level.Fine,
	   "Connection Opened: " + pSocket.getInetAddress());

	while(pSocket.isConnected() && isLive() && !pShutdown.get()) {
	  InputStream in     = pSocket.getInputStream();
	  ObjectInput objIn  = new PluginInputStream(in);
	  Object obj         = objIn.readObject();

	  OutputStream out    = pSocket.getOutputStream();
	  ObjectOutput objOut = new ObjectOutputStream(out);
	  
	  if(isFirst())
	    verifyConnection(obj, objOut);
	  else {
            /* check time difference between client and server */  
            checkTimeSync((Long) obj, pSocket); 

            /* dispatch request by kind */ 
	    QueueRequest kind = (QueueRequest) objIn.readObject();
	  
	    LogMgr.getInstance().logAndFlush
	      (LogMgr.Kind.Net, LogMgr.Level.Finer,
	       "Request [" + pSocket.getInetAddress() + "]: " + kind.name());	  
	  
            try {
              switch(kind) {
              /*-- ADMINISTRATIVE PRIVILEGES -----------------------------------------------*/
              case UpdateAdminPrivileges: 
                {
                  MiscUpdateAdminPrivilegesReq req = 
                    (MiscUpdateAdminPrivilegesReq) objIn.readObject();
                  objOut.writeObject(pQueueMgr.updateAdminPrivileges(req));
                  objOut.flush(); 
                }
                break;
                
              /*-- KEYCHOOSER UPDATE ------------------------------------------------------*/

              case NewKeyChooserInstalled:
                {
                  MiscPluginIDReq req =
                    (MiscPluginIDReq) objIn.readObject();
                  objOut.writeObject(pQueueMgr.newKeyChooserInstalled(req));
                  objOut.flush();
                }
                break;

              /*-- LOGGING -----------------------------------------------------------------*/
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


              /*-- RUNTIME PARAMETERS ------------------------------------------------------*/
              case GetQueueControls:
                {
                  objOut.writeObject(pQueueMgr.getRuntimeControls());
                  objOut.flush(); 
                }
                break;
	      
              case SetQueueControls:
                {
                  QueueSetQueueControlsReq req = 
                    (QueueSetQueueControlsReq) objIn.readObject();
                  objOut.writeObject(pQueueMgr.setRuntimeControls(req));
                  objOut.flush(); 
                }
                break;


              /*-- LICENSE KEYS ------------------------------------------------------------*/
              case GetLicenseKeyNames:
                {
                  QueueGetKeyNamesReq req = 
                    (QueueGetKeyNamesReq) objIn.readObject();
                  boolean settable = req.getUserSettableOnly();
                  objOut.writeObject(pQueueMgr.getLicenseKeyNames(settable));
                  objOut.flush(); 
                }
                break;

              case GetLicenseKeyDescriptions:
                {
                  QueueGetKeyDescriptionsReq req = 
                    (QueueGetKeyDescriptionsReq) objIn.readObject();
                  boolean settable = req.getUserSettableOnly();
                  objOut.writeObject(pQueueMgr.getLicenseKeyDescriptions(settable)); 
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


              /*-- SELECTION KEYS ----------------------------------------------------------*/
              case GetSelectionKeyNames:
                {
                  QueueGetKeyNamesReq req = 
                    (QueueGetKeyNamesReq) objIn.readObject();
                  boolean settable = req.getUserSettableOnly();
                  objOut.writeObject(pQueueMgr.getSelectionKeyNames(settable));
                  objOut.flush(); 
                }
                break;

              case GetSelectionKeyDescriptions:
                {
                  QueueGetKeyDescriptionsReq req = 
                    (QueueGetKeyDescriptionsReq) objIn.readObject();
                  boolean settable = req.getUserSettableOnly();
                  objOut.writeObject(pQueueMgr.getSelectionKeyDescriptions(settable)); 
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


              /*-- SELECTION GROUPS --------------------------------------------------------*/
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
                  QueueAddByNameReq req = 
                    (QueueAddByNameReq) objIn.readObject();
                  objOut.writeObject(pQueueMgr.addSelectionGroup(req));
                  objOut.flush(); 
                }
                break;

              case RemoveSelectionGroups:
                {
                  QueueRemoveByNameReq req = 
                    (QueueRemoveByNameReq) objIn.readObject();
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


              /*-- SELECTION SCHEDULES -----------------------------------------------------*/
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
                  QueueAddByNameReq req = 
                    (QueueAddByNameReq) objIn.readObject();
                  objOut.writeObject(pQueueMgr.addSelectionSchedule(req));
                  objOut.flush(); 
                }
                break;

              case RemoveSelectionSchedules:
                {
                  QueueRemoveByNameReq req = 
                    (QueueRemoveByNameReq) objIn.readObject();
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
	      
              /*-- HARDWARE KEYS -----------------------------------------------------------*/
              case GetHardwareKeyNames:
                {
                  QueueGetKeyNamesReq req = 
                    (QueueGetKeyNamesReq) objIn.readObject();
                  boolean settable = req.getUserSettableOnly();
                  objOut.writeObject(pQueueMgr.getHardwareKeyNames(settable));
                  objOut.flush(); 
                }
                break;

              case GetHardwareKeyDescriptions:
                {
                  QueueGetKeyDescriptionsReq req = 
                    (QueueGetKeyDescriptionsReq) objIn.readObject();
                  boolean settable = req.getUserSettableOnly();
                  objOut.writeObject(pQueueMgr.getHardwareKeyDescriptions(settable)); 
                  objOut.flush(); 
                }
                break;

              case GetHardwareKeys:
                {
                  objOut.writeObject(pQueueMgr.getHardwareKeys());
                  objOut.flush(); 
                }
                break;

              case AddHardwareKey:
                {
                  QueueAddHardwareKeyReq req = 
                    (QueueAddHardwareKeyReq) objIn.readObject();
                  objOut.writeObject(pQueueMgr.addHardwareKey(req));
                  objOut.flush(); 
                }
                break;

              case RemoveHardwareKey:
                {
                  QueueRemoveHardwareKeyReq req = 
                    (QueueRemoveHardwareKeyReq) objIn.readObject();
                  objOut.writeObject(pQueueMgr.removeHardwareKey(req));
                  objOut.flush(); 
                }
                break;


              /*-- HARDWARE GROUPS ---------------------------------------------------------*/
              case GetHardwareGroupNames:
                {
                  objOut.writeObject(pQueueMgr.getHardwareGroupNames());
                  objOut.flush(); 
                }
                break;

              case GetHardwareGroups:
                {
                  objOut.writeObject(pQueueMgr.getHardwareGroups());
                  objOut.flush(); 
                }
                break;

              case AddHardwareGroup:
                {
                  QueueAddByNameReq req = 
                    (QueueAddByNameReq) objIn.readObject();
                  objOut.writeObject(pQueueMgr.addHardwareGroup(req));
                  objOut.flush(); 
                }
                break;

              case RemoveHardwareGroups:
                {
                  QueueRemoveByNameReq req = 
                    (QueueRemoveByNameReq) objIn.readObject();
                  objOut.writeObject(pQueueMgr.removeHardwareGroups(req));
                  objOut.flush(); 
                }
                break;

              case EditHardwareGroups:
                {
                  QueueEditHardwareGroupsReq req = 
                    (QueueEditHardwareGroupsReq) objIn.readObject();
                  objOut.writeObject(pQueueMgr.editHardwareGroups(req));
                  objOut.flush(); 
                }
                break;

              /*-- DISPATCH CONTROLS -------------------------------------------------------*/
              case GetDispatchControlNames:
                {
                  objOut.writeObject(pQueueMgr.getDispatchControlNames());
                  objOut.flush(); 
                }
                break;

              case GetDispatchControls:
                {
                  objOut.writeObject(pQueueMgr.getDispatchControls());
                  objOut.flush(); 
                }
                break;

              case AddDispatchControl:
                {
                  QueueAddByNameReq req = 
                    (QueueAddByNameReq) objIn.readObject();
                  objOut.writeObject(pQueueMgr.addDispatchControl(req));
                  objOut.flush(); 
                }
                break;

              case RemoveDispatchControls:
                {
                  QueueRemoveByNameReq req = 
                    (QueueRemoveByNameReq) objIn.readObject();
                  objOut.writeObject(pQueueMgr.removeDispatchControls(req));
                  objOut.flush(); 
                }
                break;

              case EditDispatchControls:
                {
                  QueueEditDispatchControlsReq req = 
                    (QueueEditDispatchControlsReq) objIn.readObject();
                  objOut.writeObject(pQueueMgr.editDispatchControls(req));
                  objOut.flush(); 
                }
                break;
                
              /*-- USER BALANCE GROUPS -----------------------------------------------------*/
              case GetBalanceGroupNames:
                {
                  objOut.writeObject(pQueueMgr.getBalanceGroupNames());
                  objOut.flush(); 
                }
                break;

              case GetBalanceGroups:
                {
                  objOut.writeObject(pQueueMgr.getBalanceGroups());
                  objOut.flush(); 
                }
                break;
                
              case GetBalanceGroup:
                {
                  QueueGetByNameReq req = 
                    (QueueGetByNameReq) objIn.readObject();
                  objOut.writeObject(pQueueMgr.getBalanceGroup(req.getName()));
                  objOut.flush(); 
                }
                break;

              case AddBalanceGroup:
                {
                  QueueAddByNameReq req = 
                    (QueueAddByNameReq) objIn.readObject();
                  objOut.writeObject(pQueueMgr.addBalanceGroup(req));
                  objOut.flush(); 
                }
                break;

              case RemoveBalanceGroups:
                {
                  QueueRemoveByNameReq req = 
                    (QueueRemoveByNameReq) objIn.readObject();
                  objOut.writeObject(pQueueMgr.removeBalanceGroups(req));
                  objOut.flush(); 
                }
                break;

              case EditBalanceGroups:
                {
                  QueueEditUserBalanceGroupsReq req = 
                    (QueueEditUserBalanceGroupsReq) objIn.readObject();
                  objOut.writeObject(pQueueMgr.editBalanceGroups(req));
                  objOut.flush(); 
                }
                break;
                
              case GetBalanceGroupUsage:
                {
                  objOut.writeObject(pQueueMgr.getBalanceGroupUsage());
                  objOut.flush();
                }
                break;

              /*-- ADMIN -----------------------------------------------------------------*/
              case BackupDatabase: 
                {
                  QueueBackupDatabaseReq req = (QueueBackupDatabaseReq) objIn.readObject();
                  objOut.writeObject(pQueueMgr.backupDatabase(req));
                  objOut.flush(); 
                }
                break;  

              /*-- SERVER EXTENSIONS -------------------------------------------------------*/
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


              /*-- JOB MANAGER DATA -------------------------------------------------------*/
              case GetHosts:
                {
                  QueueGetHostsReq req = (QueueGetHostsReq) objIn.readObject();
                  objOut.writeObject(pQueueMgr.getHosts(req));
                  objOut.flush(); 
                }
                break;

              case AddHost:
                {
                  QueueAddByNameReq req = (QueueAddByNameReq) objIn.readObject();
                  objOut.writeObject(pQueueMgr.addHost(req));
                  objOut.flush(); 
                }
                break;

              case RemoveHosts:
                {
                  QueueRemoveByNameReq req = 
                    (QueueRemoveByNameReq) objIn.readObject();
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


              /*-- JOBS --------------------------------------------------------------------*/
              case GetJobStatesAndCheckSums:
                {
                  QueueGetJobStatesAndCheckSumsReq req = 
                    (QueueGetJobStatesAndCheckSumsReq) objIn.readObject();
                  objOut.writeObject(pQueueMgr.getJobStatesAndCheckSums(req));
                  objOut.flush(); 
                }
                break; 

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

              case GetJobStateDistribution:
                {
                  QueueGetJobStateDistributionReq req = 
                    (QueueGetJobStateDistributionReq) objIn.readObject();
                  objOut.writeObject(pQueueMgr.getJobStateDistribution(req));
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
                  objOut.writeObject(pQueueMgr.getJobs(req));
                  objOut.flush(); 
                }
                break;
	    
              case GetJobInfo:
                {
                  QueueGetJobInfoReq req = (QueueGetJobInfoReq) objIn.readObject();
                  objOut.writeObject(pQueueMgr.getJobInfos(req));
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

              case UpdateJobKeys:
                {
                  QueueJobsReq req = (QueueJobsReq) objIn.readObject();
                  objOut.writeObject(pQueueMgr.updateJobKeys(req));
                  objOut.flush();
                }
                break;
                
              case DoJobKeysNeedUpdate:
                {
                  objOut.writeObject(pQueueMgr.doJobKeysNeedUpdate());
                  objOut.flush();
                }
                break;
                
              case UpdateAllJobKeys:
                {
                  PrivilegedReq req = (PrivilegedReq) objIn.readObject();
                  objOut.writeObject(pQueueMgr.updateAllJobKeys(req));
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
                  QueueGetJobGroupsReq req = (QueueGetJobGroupsReq) objIn.readObject();
                  objOut.writeObject(pQueueMgr.getJobGroups(req));
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

              /*-- NETWORK CONNECTION ------------------------------------------------------*/
              case Disconnect:
		disconnect();
                break;

              case ShutdownOptions:
                {
                  QueueShutdownOptionsReq req = (QueueShutdownOptionsReq) objIn.readObject();
                  pQueueMgr.setShutdownOptions(req.shutdownJobMgrs());
                } 
                // fallthrough to Shutdown case is intentional here!

              case Shutdown:
                LogMgr.getInstance().logAndFlush
                  (LogMgr.Kind.Net, LogMgr.Level.Warning,
                   "Shutdown Request Received: " + pSocket.getInetAddress());
                shutdown(); 
                break;	    

              default:
                throw new IllegalStateException("Unknown request ID (" + kind + ")!"); 
              }
            }
            catch(ClosedChannelException ex) {
              LogMgr.getInstance().log
                (LogMgr.Kind.Net, LogMgr.Level.Severe, 
                 "Connection closed by client while performing: " + kind.name());
            }
            catch(Exception opex) {
              String msg = ("Internal Error while performing: " + kind.name() + "\n\n" + 
                            Exceptions.getFullMessage(opex)); 

              LogMgr.getInstance().log
                (LogMgr.Kind.Net, LogMgr.Level.Severe, msg);
              
              if(objOut != null) {
                TaskTimer timer = new TaskTimer();
                timer.aquire();
                objOut.writeObject(new FailureRsp(timer, msg));
                objOut.flush(); 
              }
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
	  host = addr.getCanonicalHostName().toLowerCase(Locale.ENGLISH);
	
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Severe,
	   "Connection from (" + host + ":" + PackageInfo.sQueuePort + ") " + 
	   "terminated abruptly!");
      }
      catch (IOException ex) {
	InetAddress addr = pSocket.getInetAddress(); 
	String host = "???";
	if(addr != null) 
	  host = addr.getCanonicalHostName().toLowerCase(Locale.ENGLISH);

	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Severe,
           Exceptions.getFullMessage
	   ("IO problems on connection from " + 
            "(" + host + ":" + PackageInfo.sQueuePort + "):", ex)); 
      }
      catch(ClassNotFoundException ex) {
	InetAddress addr = pSocket.getInetAddress(); 
	String host = "???";
	if(addr != null) 
	  host = addr.getCanonicalHostName().toLowerCase(Locale.ENGLISH);

	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Severe,
           Exceptions.getFullMessage
	   ("Illegal object encountered on connection from " + 
            "(" + host + ":" + PackageInfo.sQueuePort + "):", ex)); 
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

      LogMgr.getInstance().logAndFlush
	(LogMgr.Kind.Net, LogMgr.Level.Fine,
	 "Client Connection Closed.");
    }
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
     SchedulerTask scheduler,
     WriterTask writer,
     BalancerTask balancer, 
     BackupSyncTask backupSync
    ) 
    {
      super("QueueMgrServer:MasterConnectTask"); 
      pCollector = collector;
      pDispatcher = dispatcher;
      pScheduler = scheduler;
      pWriter = writer;
      pBalancer = balancer;
      pBackupSync = backupSync;
    }

    @Override
    public void 
    run() 
    {
      try {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Net, LogMgr.Level.Info,
	   "Establishing Network Connections [MasterMgr]...");
   
	pQueueMgr.establishMasterConnection();

	pTimer.suspend();
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Net, LogMgr.Level.Info,
	   "Server Ready.\n" + 
	   "  Started in " + TimeStamps.formatInterval(pTimer.getTotalDuration()));
	pTimer = new TaskTimer();
	
	pCollector.start();
	pDispatcher.start();
	pScheduler.start();
	pWriter.start();
	pBalancer.start();
	pBackupSync.start();
      }
      catch (Exception ex) {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Net, LogMgr.Level.Severe,
	   Exceptions.getFullMessage("Master Connector Failed:", ex)); 
      }
    }

    private CollectorTask  pCollector;
    private DispatcherTask pDispatcher;
    private SchedulerTask  pScheduler;
    private WriterTask     pWriter;
    private BalancerTask   pBalancer;
    private BackupSyncTask pBackupSync;
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

    @Override
    public void 
    run() 
    {
      try {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Col, LogMgr.Level.Fine,
	   "Collector Started.");	

	while(!pShutdown.get()) {
	  pQueueMgr.collector();
	}
      }
      catch (Exception ex) {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Col, LogMgr.Level.Severe,
	   Exceptions.getFullMessage("Collector Failed:", ex)); 
      }
      finally {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Col, LogMgr.Level.Fine,
	   "Collector Finished.");	
      }
    }
  }
  
  /**
   * Collect and calculate the shares of the queue that users are using. 
   */
  private
  class BalancerTask
    extends Thread
  {
    public
    BalancerTask()
    {
      super("QueueMgrServer:BalancerTask");
    }
    
    @Override
    public void 
    run() 
    {
      try {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Usr, LogMgr.Level.Fine,
           "Balancer Started.");       

        while(!pShutdown.get()) {
          pQueueMgr.balancer();
        }
      }
      catch (Exception ex) {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Usr, LogMgr.Level.Severe,
           Exceptions.getFullMessage("Balancer Failed:", ex)); 
      }
      finally {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Usr, LogMgr.Level.Fine,
           "Balancer Finished.");      
      }
    }
  }

  /**
   * Write out jobs whose requirements have been updated.
   */
  private 
  class WriterTask
    extends Thread
  {
    public 
    WriterTask() 
    {
      super("QueueMgrServer:WriterTask");
    }

    @Override
    public void 
    run() 
    {
      try {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Wri, LogMgr.Level.Fine,
           "Job Writer Started.");       

        while(!pShutdown.get()) {
          pQueueMgr.writer(true);
        }
      }
      catch (Exception ex) {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Wri, LogMgr.Level.Severe,
           Exceptions.getFullMessage("Job Writer Failed:", ex)); 
      }
      finally {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Wri, LogMgr.Level.Fine,
           "Job Writer Finished.");      
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

    @Override
    public void 
    run() 
    {
      try {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Dsp, LogMgr.Level.Fine,
	   "Dispatcher Started.");	

	pQueueMgr.establishMasterConnection();

	while(!pShutdown.get()) {
	  pQueueMgr.dispatcher();
	}
      }
      catch (Exception ex) {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Dsp, LogMgr.Level.Severe,
	   Exceptions.getFullMessage("Dispatcher Failed:", ex)); 
      }
      finally {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Dsp, LogMgr.Level.Fine,
	   "Dispatcher Finished.");	
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

    @Override
    public void 
    run() 
    {
      try {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Ops, LogMgr.Level.Fine,
	   "Scheduler Started.");	

	while(!pShutdown.get()) {
	  pQueueMgr.scheduler();
	}
      }
      catch (Exception ex) {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Ops, LogMgr.Level.Severe,
	   Exceptions.getFullMessage("Scheduler Failed:", ex)); 
      }
      finally {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Ops, LogMgr.Level.Fine,
	   "Scheduler Finished.");	
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

    @Override
    public void 
    run() 
    {
      try {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Mem, LogMgr.Level.Fine,
	   "JVM Memory Statistics Started.");	

	while(!pShutdown.get()) {
	  pQueueMgr.heapStats();
	}
      }
      catch (Exception ex) {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Mem, LogMgr.Level.Severe,
	   Exceptions.getFullMessage("JVM Memory Statistics Failed:", ex)); 
      }
      finally {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Mem, LogMgr.Level.Fine,
	   "JVM Memory Statistics Finished.");	
      }
    }
  }

  /**
   * Database backup synchronization and archiving.
   */
  private 
  class BackupSyncTask
    extends Thread
  {
    public 
    BackupSyncTask() 
    {
      super("MasterMgrServer:BackupSyncTask");
    }

    @Override
    public void 
    run() 
    {
      try {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Ops, LogMgr.Level.Fine,
	   "Database Backup Synchronizer Started.");	

        boolean first = true;
	while(!pShutdown.get()) {
	  pQueueMgr.backupSync(first);
          first = false;
	}
      }
      catch (Exception ex) {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Mem, LogMgr.Level.Severe,
           Exceptions.getFullMessage("Database Backup Synchronizer Failed:", ex)); 
      }
      finally {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Mem, LogMgr.Level.Fine,
	   "Database Backup Synchronizer Finished.");	
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
  private TreeSet<HandlerTask>  pTasks;
}

