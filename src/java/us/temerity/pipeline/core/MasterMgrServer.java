// $Id: MasterMgrServer.java,v 1.116 2010/01/22 00:14:33 jim Exp $

package us.temerity.pipeline.core;

import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.event.EditedNodeEvent;
import us.temerity.pipeline.message.*;
import us.temerity.pipeline.message.node.*;

/*------------------------------------------------------------------------------------------*/
/*   M A S T E R   M G R   S E R V E R                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The server-side network support for the Pipeline master server daemon. <P> 
 * 
 * This class handles network communication with {@link MasterMgrClient MasterMgrClient} 
 * instances running on remote hosts.  This class listens for new connections from  
 * <CODE>MasterMgrClient</CODE> instances and creats a thread to manage each connection.
 * Each of these threads then listens for requests for file system related operations 
 * and dispatches these requests to an underlying instance of the {@link MasterMgr MasterMgr}
 * class.
 */
class MasterMgrServer
  extends BaseMgrServer
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new master manager server.
   * 
   * @param app
   *   The application instance.
   * 
   * @param rebuildCache
   *   Whether to rebuild cache files and ignore existing lock files.
   * 
   * @param nodeReaderThreads
   *   The number of node reader threads to spawn.
   * 
   * @param nodeWriterThreads
   *   The number of node writer threads to spawn.
   * 
   * @param preserveOfflinedCache
   *   Whether to keep the offlined versions cache file after startup and reread instead of 
   *   rebuilding it during a database rebuild.
   * 
   * @param internalFileMgr
   *   Whether the file manager should be run as a thread of plmaster(1).
   * 
   * @param controls
   *   The runtime controls.
   * 
   * @throws PipelineException 
   *   If unable to properly initialize the server.
   */
  public
  MasterMgrServer
  (
   MasterApp app, 
   boolean rebuildCache,
   int nodeReaderThreads, 
   int nodeWriterThreads, 
   boolean preserveOfflinedCache, 
   boolean internalFileMgr,
   MasterControls controls
  )
    throws PipelineException 
  { 
    super("MasterMgrServer");

    pTimer = new TaskTimer();

    pMasterApp = app;

    pMasterMgr = new MasterMgr(rebuildCache, nodeReaderThreads, nodeWriterThreads, 
                               preserveOfflinedCache, internalFileMgr, controls); 

    pTasks = new TreeSet<HandlerTask>();
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
  @Override
  public void 
  run() 
  {
    try {
      pSocketChannel = ServerSocketChannel.open();
      ServerSocket server = pSocketChannel.socket();
      InetSocketAddress saddr = new InetSocketAddress(PackageInfo.sMasterPort);
      server.bind(saddr, 100);

      LogMgr.getInstance().logAndFlush
	(LogMgr.Kind.Net, LogMgr.Level.Fine,
	 "Listening on Port: " + PackageInfo.sMasterPort);
      pTimer.suspend();
      LogMgr.getInstance().logAndFlush
	(LogMgr.Kind.Net, LogMgr.Level.Info,
	 "Server Ready.\n" + 
	 "  Started in " + TimeStamps.formatInterval(pTimer.getTotalDuration()));
      pTimer = new TaskTimer();

      CacheGCTask cacheGC = new CacheGCTask();
      cacheGC.start();

      BackupSyncTask backupSync = new BackupSyncTask();
      backupSync.start();

      EventWriterTask ewriter = new EventWriterTask();
      ewriter.start();

      LicenseTask lic = new LicenseTask();
      lic.start();

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
	{
	  LogMgr.getInstance().logAndFlush
	    (LogMgr.Kind.Ops, LogMgr.Level.Info,
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

	{
	  LogMgr.getInstance().logAndFlush
	    (LogMgr.Kind.Ops, LogMgr.Level.Info,
	     "Waiting on License Validator...");
          
          lic.interrupt();
          lic.join();
	}

	{
	  LogMgr.getInstance().logAndFlush
	    (LogMgr.Kind.Ops, LogMgr.Level.Info,
	     "Waiting on Event Writer...");

	  ewriter.join();
	}

	{
	  LogMgr.getInstance().logAndFlush
	    (LogMgr.Kind.Ops, LogMgr.Level.Info,
	     "Waiting on Cache Garbage Collector...");
          
          cacheGC.interrupt();
          cacheGC.join();
        }

	{
	  LogMgr.getInstance().logAndFlush
	    (LogMgr.Kind.Ops, LogMgr.Level.Info,
	     "Waiting on Database Backup Synchronizer...");
          
          backupSync.interrupt();
          backupSync.join();
        }
      }
      catch(InterruptedException ex) {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Ops, LogMgr.Level.Severe,
	   "Interrupted while shutting down!");
      }
    }
    catch (IOException ex) {
      LogMgr.getInstance().logAndFlush
	(LogMgr.Kind.Net, LogMgr.Level.Severe,
         Exceptions.getFullMessage
	 ("IO problems on port (" + PackageInfo.sMasterPort + "):", ex)); 
    }
    catch (SecurityException ex) {
      LogMgr.getInstance().logAndFlush
	(LogMgr.Kind.Net, LogMgr.Level.Severe,
         Exceptions.getFullMessage
	 ("The Security Manager doesn't allow listening to sockets!", ex)); 
    }
    catch (Exception ex) {
      LogMgr.getInstance().log
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

      pMasterMgr.shutdown();

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
   * Handle an incoming connection from a <CODE>MasterMgrClient</CODE> instance.
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
      super("MasterMgrServer:HandlerTask");
      pChannel = channel;
      pRunningEditorIDs = new TreeSet<Long>(); 
    }

    @Override
    protected String
    verifyClient
    (
     String clientID
    )
    {
      if(!clientID.equals("MasterMgr")) {
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
	    MasterRequest kind = (MasterRequest) objIn.readObject();
	  
	    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finer)) {
	      LogMgr.getInstance().logAndFlush
		(LogMgr.Kind.Net, LogMgr.Level.Finer,
		 "Request [" + pSocket.getInetAddress() + "]: " + kind.name());	  
	    }

            try {
              switch(kind) {
              /*-- ADMINISTRATIVE PRIVILEGES ---------------------------------------------*/
              case GetWorkGroups:
                {
                  objOut.writeObject(pMasterMgr.getWorkGroups());
                  objOut.flush(); 
                }
                break;

              case SetWorkGroups:
                {
                  MiscSetWorkGroupsReq req = (MiscSetWorkGroupsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.setWorkGroups(req));
                  objOut.flush(); 
                }
                break;

              case GetPrivileges:
                {
                  objOut.writeObject(pMasterMgr.getPrivileges());
                  objOut.flush(); 
                }
                break;

              case EditPrivileges: 
                {
                  MiscEditPrivilegesReq req = (MiscEditPrivilegesReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.editPrivileges(req));
                  objOut.flush(); 
                }
                break;

              case GetPrivilegeDetails:
                {
                  MiscGetPrivilegeDetailsReq req = 
                    (MiscGetPrivilegeDetailsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getPrivilegeDetails(req));
                  objOut.flush(); 
                }
                break;


              /*-- LOGGING ---------------------------------------------------------------*/
              case GetLogControls:
                {
                  objOut.writeObject(pMasterMgr.getLogControls());
                  objOut.flush(); 
                }
                break;

              case SetLogControls:
                {
                  MiscSetLogControlsReq req = (MiscSetLogControlsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.setLogControls(req));
                  objOut.flush(); 
                }
                break;


              /*-- RUNTIME PARAMETERS ----------------------------------------------------*/
              case GetMasterControls:
                {
                  objOut.writeObject(pMasterMgr.getRuntimeControls());
                  objOut.flush(); 
                }
                break;

              case SetMasterControls:
                {
                  MiscSetMasterControlsReq req = 
                    (MiscSetMasterControlsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.setRuntimeControls(req));
                  objOut.flush(); 
                }
                break;


              /*-- TOOLSETS --------------------------------------------------------------*/
              case GetDefaultToolsetName:
                {
                  objOut.writeObject(pMasterMgr.getDefaultToolsetName());
                  objOut.flush(); 
                }
                break;

              case SetDefaultToolsetName:
                {
                  MiscSetDefaultToolsetNameReq req = 
                    (MiscSetDefaultToolsetNameReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.setDefaultToolsetName(req));
                  objOut.flush(); 
                }
                break;

              case GetActiveToolsetNames:
                {
                  objOut.writeObject(pMasterMgr.getActiveToolsetNames());
                  objOut.flush(); 
                }
                break;

              case SetToolsetActive:
                {
                  MiscSetToolsetActiveReq req = 
                    (MiscSetToolsetActiveReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.setToolsetActive(req));
                  objOut.flush(); 
                }
                break;

              case GetToolsetNames:
                {
                  MiscGetToolsetNamesReq req = 
                    (MiscGetToolsetNamesReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getToolsetNames(req));
                  objOut.flush(); 
                }
                break;

              case GetAllToolsetNames:
                {
                  objOut.writeObject(pMasterMgr.getAllToolsetNames());
                  objOut.flush(); 
                }
                break;

              case GetToolset:
                {
                  MiscGetToolsetReq req = (MiscGetToolsetReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getToolset(req));
                  objOut.flush(); 
                }
                break;

              case GetOsToolsets:
                {
                  MiscGetOsToolsetsReq req = (MiscGetOsToolsetsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getOsToolsets(req));
                  objOut.flush(); 
                }
                break;

              case GetToolsetEnvironment:
                {
                  MiscGetToolsetEnvironmentReq req = 
                    (MiscGetToolsetEnvironmentReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getToolsetEnvironment(req));
                  objOut.flush(); 
                }
                break;

              case CreateToolset:
                {
                  MiscCreateToolsetReq req = (MiscCreateToolsetReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.createToolset(req));
                  objOut.flush(); 
                }
                break;


              /*-- TOOLSET PACKAGES ------------------------------------------------------*/
              case GetToolsetPackageNames:
                {
                  MiscGetToolsetPackageNamesReq req = 
                    (MiscGetToolsetPackageNamesReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getToolsetPackageNames(req));
                  objOut.flush(); 
                }
                break;

              case GetAllToolsetPackageNames:
                {
                  objOut.writeObject(pMasterMgr.getAllToolsetPackageNames());
                  objOut.flush(); 
                }
                break;

              case GetToolsetPackage:
                {
                  MiscGetToolsetPackageReq req = 
                    (MiscGetToolsetPackageReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getToolsetPackage(req));
                  objOut.flush(); 
                }
                break;

              case GetToolsetPackages:
                {
                  MiscGetToolsetPackagesReq req = 
                    (MiscGetToolsetPackagesReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getToolsetPackages(req));
                  objOut.flush(); 
                }
                break;

              case CreateToolsetPackage:
                {
                  MiscCreateToolsetPackageReq req = 
                    (MiscCreateToolsetPackageReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.createToolsetPackage(req));
                  objOut.flush(); 
                }
                break;


              /*-- PLUGIN MENUS / LAYOUTS ------------------------------------------------*/
              case GetPluginMenuLayouts:
                {
                  MiscGetPluginMenuLayoutReq req = 
                    (MiscGetPluginMenuLayoutReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getPluginMenuLayouts(req));
                  objOut.flush(); 
                }
                break;

              case GetSelectPackagePlugins:
                {
                  MiscGetSelectPackagePluginsReq req = 
                    (MiscGetSelectPackagePluginsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getSelectPackagePlugins(req));
                  objOut.flush(); 
                }
                break;

              case GetEditorMenuLayout:
                {
                  MiscGetPluginMenuLayoutReq req = 
                    (MiscGetPluginMenuLayoutReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getEditorMenuLayout(req));
                  objOut.flush(); 
                }
                break;

              case SetEditorMenuLayout:
                {
                  MiscSetPluginMenuLayoutReq req = 
                    (MiscSetPluginMenuLayoutReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.setEditorMenuLayout(req));
                  objOut.flush(); 
                }
                break;

              case GetToolsetEditorPlugins:
                {
                  MiscGetToolsetPluginsReq req = 
                    (MiscGetToolsetPluginsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getToolsetEditorPlugins(req));
                  objOut.flush(); 
                }
                break;

              case GetPackageEditorPlugins:
                {
                  MiscGetPackagePluginsReq req = 
                    (MiscGetPackagePluginsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getPackageEditorPlugins(req));
                  objOut.flush(); 
                }
                break;

              case SetPackageEditorPlugins:
                {
                  MiscSetPackagePluginsReq req = 
                    (MiscSetPackagePluginsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.setPackageEditorPlugins(req));
                  objOut.flush(); 
                }
                break;

              /*------------------------------*/

              case GetComparatorMenuLayout:
                {
                  MiscGetPluginMenuLayoutReq req = 
                    (MiscGetPluginMenuLayoutReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getComparatorMenuLayout(req));
                  objOut.flush(); 
                }
                break;

              case SetComparatorMenuLayout:
                {
                  MiscSetPluginMenuLayoutReq req = 
                    (MiscSetPluginMenuLayoutReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.setComparatorMenuLayout(req));
                  objOut.flush(); 
                }
                break;

              case GetToolsetComparatorPlugins:
                {
                  MiscGetToolsetPluginsReq req = 
                    (MiscGetToolsetPluginsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getToolsetComparatorPlugins(req));
                  objOut.flush(); 
                }
                break;

              case GetPackageComparatorPlugins:
                {
                  MiscGetPackagePluginsReq req = 
                    (MiscGetPackagePluginsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getPackageComparatorPlugins(req));
                  objOut.flush(); 
                }
                break;

              case SetPackageComparatorPlugins:
                {
                  MiscSetPackagePluginsReq req = 
                    (MiscSetPackagePluginsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.setPackageComparatorPlugins(req));
                  objOut.flush(); 
                }
                break;

              /*------------------------------*/

              case GetActionMenuLayout:
                {
                  MiscGetPluginMenuLayoutReq req = 
                    (MiscGetPluginMenuLayoutReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getActionMenuLayout(req));
                  objOut.flush(); 
                }
                break;

              case SetActionMenuLayout:
                {
                  MiscSetPluginMenuLayoutReq req = 
                    (MiscSetPluginMenuLayoutReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.setActionMenuLayout(req));
                  objOut.flush(); 
                }
                break;

              case GetToolsetActionPlugins:
                {
                  MiscGetToolsetPluginsReq req = 
                    (MiscGetToolsetPluginsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getToolsetActionPlugins(req));
                  objOut.flush(); 
                }
                break;

              case GetPackageActionPlugins:
                {
                  MiscGetPackagePluginsReq req = 
                    (MiscGetPackagePluginsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getPackageActionPlugins(req));
                  objOut.flush(); 
                }
                break;

              case SetPackageActionPlugins:
                {
                  MiscSetPackagePluginsReq req = 
                    (MiscSetPackagePluginsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.setPackageActionPlugins(req));
                  objOut.flush(); 
                }
                break;

              /*------------------------------*/

              case GetToolMenuLayout:
                {
                  MiscGetPluginMenuLayoutReq req = 
                    (MiscGetPluginMenuLayoutReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getToolMenuLayout(req));
                  objOut.flush(); 
                }
                break;

              case SetToolMenuLayout:
                {
                  MiscSetPluginMenuLayoutReq req = 
                    (MiscSetPluginMenuLayoutReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.setToolMenuLayout(req));
                  objOut.flush(); 
                }
                break;

              case GetToolsetToolPlugins:
                {
                  MiscGetToolsetPluginsReq req = 
                    (MiscGetToolsetPluginsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getToolsetToolPlugins(req));
                  objOut.flush(); 
                }
                break;

              case GetPackageToolPlugins:
                {
                  MiscGetPackagePluginsReq req = 
                    (MiscGetPackagePluginsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getPackageToolPlugins(req));
                  objOut.flush(); 
                }
                break;

              case SetPackageToolPlugins:
                {
                  MiscSetPackagePluginsReq req = 
                    (MiscSetPackagePluginsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.setPackageToolPlugins(req));
                  objOut.flush(); 
                }
                break;

              /*------------------------------*/
	      
              case GetArchiverMenuLayout:
                {
                  MiscGetPluginMenuLayoutReq req = 
                    (MiscGetPluginMenuLayoutReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getArchiverMenuLayout(req));
                  objOut.flush(); 
                }
                break;

              case SetArchiverMenuLayout:
                {
                  MiscSetPluginMenuLayoutReq req = 
                    (MiscSetPluginMenuLayoutReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.setArchiverMenuLayout(req));
                  objOut.flush(); 
                }
                break;

              case GetToolsetArchiverPlugins:
                {
                  MiscGetToolsetPluginsReq req = 
                    (MiscGetToolsetPluginsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getToolsetArchiverPlugins(req));
                  objOut.flush(); 
                }
                break;

              case GetPackageArchiverPlugins:
                {
                  MiscGetPackagePluginsReq req = 
                    (MiscGetPackagePluginsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getPackageArchiverPlugins(req));
                  objOut.flush(); 
                }
                break;

              case SetPackageArchiverPlugins:
                {
                  MiscSetPackagePluginsReq req = 
                    (MiscSetPackagePluginsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.setPackageArchiverPlugins(req));
                  objOut.flush(); 
                }
                break;

              /*------------------------------*/
	      
              case GetMasterExtMenuLayout:
                {
                  MiscGetPluginMenuLayoutReq req = 
                    (MiscGetPluginMenuLayoutReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getMasterExtMenuLayout(req));
                  objOut.flush(); 
                }
                break;

              case SetMasterExtMenuLayout:
                {
                  MiscSetPluginMenuLayoutReq req = 
                    (MiscSetPluginMenuLayoutReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.setMasterExtMenuLayout(req));
                  objOut.flush(); 
                }
                break;

              case GetToolsetMasterExtPlugins:
                {
                  MiscGetToolsetPluginsReq req = 
                    (MiscGetToolsetPluginsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getToolsetMasterExtPlugins(req));
                  objOut.flush(); 
                }
                break;

              case GetPackageMasterExtPlugins:
                {
                  MiscGetPackagePluginsReq req = 
                    (MiscGetPackagePluginsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getPackageMasterExtPlugins(req));
                  objOut.flush(); 
                }
                break;

              case SetPackageMasterExtPlugins:
                {
                  MiscSetPackagePluginsReq req = 
                    (MiscSetPackagePluginsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.setPackageMasterExtPlugins(req));
                  objOut.flush(); 
                }
                break;


              /*------------------------------*/
	      
              case GetQueueExtMenuLayout:
                {
                  MiscGetPluginMenuLayoutReq req = 
                    (MiscGetPluginMenuLayoutReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getQueueExtMenuLayout(req));
                  objOut.flush(); 
                }
                break;

              case SetQueueExtMenuLayout:
                {
                  MiscSetPluginMenuLayoutReq req = 
                    (MiscSetPluginMenuLayoutReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.setQueueExtMenuLayout(req));
                  objOut.flush(); 
                }
                break;

              case GetToolsetQueueExtPlugins:
                {
                  MiscGetToolsetPluginsReq req = 
                    (MiscGetToolsetPluginsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getToolsetQueueExtPlugins(req));
                  objOut.flush(); 
                }
                break;

              case GetPackageQueueExtPlugins:
                {
                  MiscGetPackagePluginsReq req = 
                    (MiscGetPackagePluginsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getPackageQueueExtPlugins(req));
                  objOut.flush(); 
                }
                break;

              case SetPackageQueueExtPlugins:
                {
                  MiscSetPackagePluginsReq req = 
                    (MiscSetPackagePluginsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.setPackageQueueExtPlugins(req));
                  objOut.flush(); 
                }
                break;


              /*------------------------------*/
	      
              case GetAnnotationMenuLayout:
                {
                  MiscGetPluginMenuLayoutReq req = 
                    (MiscGetPluginMenuLayoutReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getAnnotationMenuLayout(req));
                  objOut.flush(); 
                }
                break;

              case SetAnnotationMenuLayout:
                {
                  MiscSetPluginMenuLayoutReq req = 
                    (MiscSetPluginMenuLayoutReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.setAnnotationMenuLayout(req));
                  objOut.flush(); 
                }
                break;

              case GetToolsetAnnotationPlugins:
                {
                  MiscGetToolsetPluginsReq req = 
                    (MiscGetToolsetPluginsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getToolsetAnnotationPlugins(req));
                  objOut.flush(); 
                }
                break;

              case GetPackageAnnotationPlugins:
                {
                  MiscGetPackagePluginsReq req = 
                    (MiscGetPackagePluginsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getPackageAnnotationPlugins(req));
                  objOut.flush(); 
                }
                break;

              case SetPackageAnnotationPlugins:
                {
                  MiscSetPackagePluginsReq req = 
                    (MiscSetPackagePluginsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.setPackageAnnotationPlugins(req));
                  objOut.flush(); 
                }
                break;


              /*------------------------------*/
	      
              case GetKeyChooserMenuLayout:
                {
                  MiscGetPluginMenuLayoutReq req = 
                    (MiscGetPluginMenuLayoutReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getKeyChooserMenuLayout(req));
                  objOut.flush(); 
                }
                break;

              case SetKeyChooserMenuLayout:
                {
                  MiscSetPluginMenuLayoutReq req = 
                    (MiscSetPluginMenuLayoutReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.setKeyChooserMenuLayout(req));
                  objOut.flush(); 
                }
                break;

              case GetToolsetKeyChooserPlugins:
                {
                  MiscGetToolsetPluginsReq req = 
                    (MiscGetToolsetPluginsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getToolsetKeyChooserPlugins(req));
                  objOut.flush(); 
                }
                break;

              case GetPackageKeyChooserPlugins:
                {
                  MiscGetPackagePluginsReq req = 
                    (MiscGetPackagePluginsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getPackageKeyChooserPlugins(req));
                  objOut.flush(); 
                }
                break;

              case SetPackageKeyChooserPlugins:
                {
                  MiscSetPackagePluginsReq req = 
                    (MiscSetPackagePluginsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.setPackageKeyChooserPlugins(req));
                  objOut.flush(); 
                }
                break;
	      
              /*------------------------------*/
              
              case GetBuilderCollectionMenuLayout:
                {
                  MiscGetPluginMenuLayoutReq req = 
                    (MiscGetPluginMenuLayoutReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getBuilderCollectionMenuLayout(req));
                  objOut.flush(); 
                }
                break;

              case SetBuilderCollectionMenuLayout:
                {
                  MiscSetPluginMenuLayoutReq req = 
                    (MiscSetPluginMenuLayoutReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.setBuilderCollectionMenuLayout(req));
                  objOut.flush(); 
                }
                break;

              case GetToolsetBuilderCollectionPlugins:
                {
                  MiscGetToolsetPluginsReq req = 
                    (MiscGetToolsetPluginsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getToolsetBuilderCollectionPlugins(req));
                  objOut.flush(); 
                }
                break;

              case GetPackageBuilderCollectionPlugins:
                {
                  MiscGetPackagePluginsReq req = 
                    (MiscGetPackagePluginsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getPackageBuilderCollectionPlugins(req));
                  objOut.flush(); 
                }
                break;

              case SetPackageBuilderCollectionPlugins:
                {
                  MiscSetPackagePluginsReq req = 
                    (MiscSetPackagePluginsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.setPackageBuilderCollectionPlugins(req));
                  objOut.flush(); 
                }
                break;


              /*-- SERVER EXTENSIONS -----------------------------------------------------*/
              case GetMasterExtension:
                {
                  objOut.writeObject(pMasterMgr.getMasterExtensions());
                  objOut.flush(); 
                }
                break;
	    
              case RemoveMasterExtension:
                {
                  MiscRemoveMasterExtensionReq req = 
                    (MiscRemoveMasterExtensionReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.removeMasterExtension(req));
                  objOut.flush();
                }
                break;

              case SetMasterExtension:
                {
                  MiscSetMasterExtensionReq req = 
                    (MiscSetMasterExtensionReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.setMasterExtension(req));
                  objOut.flush();
                }
                break;


              /*-- SUFFIX EDITORS --------------------------------------------------------*/
              case GetEditorForSuffix:
                {
                  MiscGetEditorForSuffixReq req = 
                    (MiscGetEditorForSuffixReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getEditorForSuffix(req));
                  objOut.flush(); 
                }
                break;
	    
              case GetSuffixEditors:
                {
                  MiscGetSuffixEditorsReq req = 
                    (MiscGetSuffixEditorsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getSuffixEditors(req));
                  objOut.flush(); 
                }
                break;

              case SetSuffixEditors:
                {
                  MiscSetSuffixEditorsReq req = (MiscSetSuffixEditorsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.setSuffixEditors(req));
                  objOut.flush(); 
                }
                break;


              /*-- WORKING AREAS ---------------------------------------------------------*/
              case GetWorkingAreas:
                {
                  objOut.writeObject(pMasterMgr.getWorkingAreas());
                  objOut.flush(); 
                }
                break;

              case GetWorkingAreasContaining:
                {
                  NodeGetByNameReq req = (NodeGetByNameReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getWorkingAreasContaining(req));
                  objOut.flush(); 
                }
                break;

              case CreateWorkingArea:
                {
                  NodeWorkingAreaReq req = (NodeWorkingAreaReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.createWorkingArea(req));
                  objOut.flush(); 
                }
                break;

              case RemoveWorkingArea:
                {
                  NodeWorkingAreaReq req = (NodeWorkingAreaReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.removeWorkingArea(req));
                  objOut.flush(); 
                }
                break;


              /*-- NODE PATHS ------------------------------------------------------------*/
              case GetNodeNames:
                {
                  NodeGetNodeNamesReq req = (NodeGetNodeNamesReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getNodeNames(req));
                  objOut.flush(); 
                }
                break;

              case UpdatePaths:
                {
                  NodeUpdatePathsReq req = (NodeUpdatePathsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.updatePaths(req));
                  objOut.flush(); 
                }
                break;

              case GetNodeOwning:
                {
                  NodeGetNodeOwningReq req = (NodeGetNodeOwningReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getNodeOwning(req));
                  objOut.flush(); 
                }
                break;
	   
              /*-- ANNOTATIONS -----------------------------------------------------------*/
              case GetAnnotation:
                {
                  NodeGetAnnotationReq req = (NodeGetAnnotationReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getAnnotation(req));
                  objOut.flush(); 
                }
                break; 

              case GetBothAnnotation:
                {
                  NodeGetBothAnnotationReq req = 
                    (NodeGetBothAnnotationReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getBothAnnotation(req));
                  objOut.flush(); 
                }
                break; 

              case GetAnnotations:
                {
                  NodeGetByNameReq req = (NodeGetByNameReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getAnnotations(req));
                  objOut.flush(); 
                }
                break;

              case GetBothAnnotations:
                {
                  NodeGetBothAnnotationsReq req = 
                    (NodeGetBothAnnotationsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getBothAnnotations(req));
                  objOut.flush(); 
                }
                break;
                
              case GetAllBothAnnotations:
                {
                  NodeGetAllBothAnnotationsReq req = 
                    (NodeGetAllBothAnnotationsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getAllBothAnnotations(req));
                  objOut.flush();
                }
                break;

              case AddAnnotation:
                {
                  NodeAddAnnotationReq req = (NodeAddAnnotationReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.addAnnotation(req));
                  objOut.flush(); 
                }
                break;

              case RemoveAnnotation:
                {
                  NodeRemoveAnnotationReq req = (NodeRemoveAnnotationReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.removeAnnotation(req));
                  objOut.flush(); 
                }
                break;

              case RemoveAnnotations:
                {
                  NodeRemoveAnnotationsReq req = 
                    (NodeRemoveAnnotationsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.removeAnnotations(req));
                  objOut.flush(); 
                }
                break;

              /*-- WORKING VERSIONS ------------------------------------------------------*/
              case GetWorkingNames:
                {
                  NodeWorkingAreaPatternReq req = 
                    (NodeWorkingAreaPatternReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getWorkingNames(req));
                  objOut.flush(); 
                }
                break;

              case GetWorkingRootNames:
                {
                  NodeWorkingAreaReq req = (NodeWorkingAreaReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getWorkingRootNames(req));
                  objOut.flush(); 
                }
                break;

              case GetWorking:
                {
                  NodeGetWorkingReq req = (NodeGetWorkingReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getWorkingVersion(req));
                  objOut.flush(); 
                }
                break;

              case ModifyProperties:
                {
                  NodeModifyPropertiesReq req = (NodeModifyPropertiesReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.modifyProperties(req));
                  objOut.flush(); 
                }
                break;

              case SetLastCTimeUpdate:
                {
                  NodeSetLastCTimeUpdateReq req = 
                    (NodeSetLastCTimeUpdateReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.setLastCTimeUpdate(req));
                  objOut.flush(); 
                }
                break;

              case Link:
                {
                  NodeLinkReq req = (NodeLinkReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.link(req));
                  objOut.flush(); 
                }
                break;

              case Unlink:
                {
                  NodeUnlinkReq req = (NodeUnlinkReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.unlink(req));
                  objOut.flush(); 
                }
                break;

              case AddSecondary:
                {
                  NodeAddSecondaryReq req = (NodeAddSecondaryReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.addSecondary(req));
                  objOut.flush(); 
                }
                break;

              case RemoveSecondary:
                {
                  NodeRemoveSecondaryReq req = (NodeRemoveSecondaryReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.removeSecondary(req));
                  objOut.flush(); 
                }
                break;


              /*-- CHECKED-IN VERSIONS ---------------------------------------------------*/
              case GetCheckedInNames:
                {
                  NodeGetNodeNamesReq req = (NodeGetNodeNamesReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getCheckedInNames(req));
                  objOut.flush(); 
                }
                break;
            
              case GetCheckedIn:
                {
                  NodeGetCheckedInReq req = (NodeGetCheckedInReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getCheckedInVersion(req));
                  objOut.flush(); 
                }
                break;
	    
              case GetAllCheckedIn:
                {
                  NodeGetByNameReq req = (NodeGetByNameReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getAllCheckedInVersions(req));
                  objOut.flush(); 
                }
                break;

              case GetCheckedInVersionIDs:
                {
                  NodeGetByNameReq req = (NodeGetByNameReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getCheckedInVersionIDs(req));
                  objOut.flush(); 
                }
                break;

              case GetIntermediateVersionIDs:
                {
                  NodeGetByNameReq req = (NodeGetByNameReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getIntermediateVersionIDs(req));
                  objOut.flush(); 
                }
                break;

              case GetHistory:
                {
                  NodeGetByNameReq req = (NodeGetByNameReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getHistory(req));
                  objOut.flush(); 
                }
                break;

              case GetCheckedInFileNovelty:
                {
                  NodeGetByNameReq req = (NodeGetByNameReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getCheckedInFileNovelty(req));
                  objOut.flush(); 
                }
                break;

              case GetCheckedInLinks:
                {
                  NodeGetByNameReq req = (NodeGetByNameReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getCheckedInLinks(req));
                  objOut.flush(); 
                }
                break;

              case GetDownstreamCheckedInLinks:
                {
                  NodeGetDownstreamCheckedInLinksReq req = 
                    (NodeGetDownstreamCheckedInLinksReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getDownstreamCheckedInLinks(req));
                  objOut.flush(); 
                }
                break;

	    
              /*-- NODE STATUS -----------------------------------------------------------*/
              case Status:
                {
                  NodeStatusReq req = (NodeStatusReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.status(req));
                  objOut.flush(); 
                }
                break;

              case MultiStatus:
                {
                  NodeMultiStatusReq req = (NodeMultiStatusReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.multiStatus(req));
                  objOut.flush(); 
                }
                break;

              case DownstreamStatus:
                {
                  NodeDownstreamStatusReq req = (NodeDownstreamStatusReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.downstreamStatus(req));
                  objOut.flush(); 
                }
                break;


              /*-- REVISION CONTROL ------------------------------------------------------*/
              case Register:
                {
                  NodeRegisterReq req = (NodeRegisterReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.register(req));
                  objOut.flush(); 
                }
                break;
	    
              case Release:
                {
                  NodeReleaseReq req = (NodeReleaseReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.release(req));
                  objOut.flush(); 
                }
                break;

              case Delete:
                {
                  NodeDeleteReq req = (NodeDeleteReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.delete(req));
                  objOut.flush(); 
                }
                break;
	    
              case RemoveFiles: 
                {
                  NodeRemoveFilesReq req = (NodeRemoveFilesReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.removeFiles(req));
                  objOut.flush(); 
                }
                break;

              case Rename:
                {
                  NodeRenameReq req = (NodeRenameReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.rename(req));
                  objOut.flush(); 
                }
                break;

              case Renumber:
                {
                  NodeRenumberReq req = (NodeRenumberReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.renumber(req));
                  objOut.flush(); 
                }
                break;

              case CheckIn:
                {
                  NodeCheckInReq req = (NodeCheckInReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.checkIn(req));
                  objOut.flush(); 
                }
                break;
	    
              case CheckOut:
                {
                  NodeCheckOutReq req = (NodeCheckOutReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.checkOut(req));
                  objOut.flush(); 
                }
                break;

              case Lock:
                {
                  NodeLockReq req = (NodeLockReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.lock(req));
                  objOut.flush(); 
                }
                break;

              case RevertFiles:
                {
                  NodeRevertFilesReq req = (NodeRevertFilesReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.revertFiles(req));
                  objOut.flush(); 
                }
                break;

              case CloneFiles:
                {
                  NodeCloneFilesReq req = (NodeCloneFilesReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.cloneFiles(req));
                  objOut.flush(); 
                }
                break;

              case Evolve:
                {
                  NodeEvolveReq req = (NodeEvolveReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.evolve(req));
                  objOut.flush(); 
                }
                break;

              /*-- CHECKSUMS ---------------------------------------------------------------*/
              case UpdateCheckSums: 
                {
                  NodeUpdateCheckSumsReq req = (NodeUpdateCheckSumsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.updateCheckSums(req));
                  objOut.flush(); 
                }
                break;

              /*-- NODE BUNDLES ----------------------------------------------------------*/
              case Pack:
                {
                  NodePackReq req = (NodePackReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.packNodes(req));
                  objOut.flush(); 
                }
                break;

              case ExtractBundle:
                {
                  NodeExtractBundleReq req = (NodeExtractBundleReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.extractBundle(req));
                  objOut.flush(); 
                }
                break;

              case Unpack:
                {
                  NodeUnpackReq req = (NodeUnpackReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.unpackNodes(req));
                  objOut.flush(); 
                }
                break;

              /*-- SITE VERSIONS ---------------------------------------------------------*/
              case ExtractSiteVersion:
                {
                  NodeExtractSiteVersionReq req = 
                    (NodeExtractSiteVersionReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.extractSiteVersion(req));
                  objOut.flush(); 
                }
                break;

              case LookupSiteVersion:
                {
                  NodeSiteVersionReq req = (NodeSiteVersionReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.lookupSiteVersion(req));
                  objOut.flush(); 
                }
                break;

              case IsSiteVersionInserted:
                {
                  NodeSiteVersionReq req = (NodeSiteVersionReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.isSiteVersionInserted(req));
                  objOut.flush(); 
                }
                break;

              case GetMissingSiteVersionRefs:
                {
                  NodeSiteVersionReq req = (NodeSiteVersionReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getMissingSiteVersionRefs(req));
                  objOut.flush(); 
                }
                break;

              case InsertSiteVersion:
                {
                  NodeSiteVersionReq req = (NodeSiteVersionReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.insertSiteVersion(req));
                  objOut.flush(); 
                }
                break;

              /*-- NODE EVENTS -----------------------------------------------------------*/
              case GetEvents: 
                {
                  NodeGetEventsReq req = (NodeGetEventsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getNodeEvents(req));
                  objOut.flush(); 
                }
                break;    

              case EditingStarted: 
                {
                  NodeEditingStartedReq req = (NodeEditingStartedReq) objIn.readObject();

                  TaskTimer timer = new TaskTimer(); 
                  try {
                    InetAddress addr = pSocket.getInetAddress();
                    String hostname = addr.getCanonicalHostName().toLowerCase(Locale.ENGLISH);

                    EditedNodeEvent event = req.getEvent(hostname);
                    Long editID = pMasterMgr.editingStarted(timer, event); 
                    pRunningEditorIDs.add(editID); 
		  
                    objOut.writeObject(new NodeEditingStartedRsp(timer, editID)); 
                  }
                  catch(Exception ex) {
                    objOut.writeObject(new FailureRsp(timer, ex.getMessage()));
                  }

                  objOut.flush(); 
                }
                break;    

              case EditingFinished: 
                {
                  NodeEditingFinishedReq req = (NodeEditingFinishedReq) objIn.readObject();

                  TaskTimer timer = new TaskTimer(); 
                  {
                    Long editID = req.getEditID();
                    pMasterMgr.editingFinished(timer, editID);
                    pRunningEditorIDs.remove(editID);
                  }
                  objOut.writeObject(new SuccessRsp(timer)); 
                  objOut.flush(); 
                }
                break;
	      
              case GetWorkingAreasEditing: 
                {
                  NodeGetByNameReq req = (NodeGetByNameReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getWorkingAreasEditing(req));
                  objOut.flush(); 
                }
                break;
	   
              /*-- JOBS ------------------------------------------------------------------*/
              case SubmitJobs: 
                {
                  NodeSubmitJobsReq req = (NodeSubmitJobsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.submitJobs(req));
                  objOut.flush(); 
                }
                break;  

              case ResubmitJobs: 
                {
                  NodeResubmitJobsReq req = (NodeResubmitJobsReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.resubmitJobs(req));
                  objOut.flush(); 
                }
                break;  

              case Vouch: 
                {
                  NodeVouchReq req = (NodeVouchReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.vouch(req));
                  objOut.flush(); 
                }
                break;

              case HasUnfinishedJobs:
                {
                  NodeWorkingAreaPatternReq req = 
                    (NodeWorkingAreaPatternReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.hasUnfinishedJobs(req));
                  objOut.flush(); 
                }
                break;

              case GetUnfinishedJobs:
                {
                  NodeWorkingAreaPatternReq req = 
                    (NodeWorkingAreaPatternReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getUnfinishedJobs(req));
                  objOut.flush(); 
                }
                break;


              /*-- ADMIN -----------------------------------------------------------------*/
              case BackupDatabase: 
                {
                  MiscBackupDatabaseReq req = (MiscBackupDatabaseReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.backupDatabase(req));
                  objOut.flush(); 
                }
                break;  

              /*-- ARCHIVE ---------------------------------------------------------------*/
              case ArchiveQuery: 
                {
                  MiscArchiveQueryReq req = (MiscArchiveQueryReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.archiveQuery(req));
                  objOut.flush(); 
                }
                break;  

              case GetArchivedSizes:
                {
                  MiscGetSizesReq req = (MiscGetSizesReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getArchivedSizes(req));
                  objOut.flush(); 
                }
                break;
	    
              case Archive: 
                {
                  MiscArchiveReq req = (MiscArchiveReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.archive(req));
                  objOut.flush(); 
                }
                break;  

              /*-- OFFLINE ---------------------------------------------------------------*/
              case OfflineQuery: 
                {
                  MiscOfflineQueryReq req = (MiscOfflineQueryReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.offlineQuery(req));
                  objOut.flush(); 
                }
                break;  

              case GetOfflineVersionIDs:
                {
                  NodeGetByNameReq req = (NodeGetByNameReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getOfflineVersionIDs(req));
                  objOut.flush(); 
                }
                break;

              case GetOfflineSizes:
                {
                  MiscGetSizesReq req = (MiscGetSizesReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getOfflineSizes(req));
                  objOut.flush(); 
                }
                break;

              case Offline: 
                {
                  MiscOfflineReq req = (MiscOfflineReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.offline(req));
                  objOut.flush(); 
                }
                break;


              /*-- RESTORE ---------------------------------------------------------------*/
              case RestoreQuery: 
                {
                  MiscRestoreQueryReq req = (MiscRestoreQueryReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.restoreQuery(req));
                  objOut.flush(); 
                }
                break; 

              case RequestRestore:
                {
                  MiscRequestRestoreReq req = (MiscRequestRestoreReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.requestRestore(req));
                  objOut.flush(); 
                }
                break;   

              case DenyRestore:
                {
                  MiscDenyRestoreReq req = (MiscDenyRestoreReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.denyRestore(req));
                  objOut.flush(); 
                }
                break;   

              case GetRestoreRequests:
                {
                  objOut.writeObject(pMasterMgr.getRestoreRequests());
                  objOut.flush(); 
                }
                break;
	    
              case GetRestoreSizes:
                {
                  MiscGetSizesReq req = (MiscGetSizesReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getRestoreSizes(req));
                  objOut.flush(); 
                }
                break;

              case Restore: 
                {
                  MiscRestoreReq req = (MiscRestoreReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.restore(req));
                  objOut.flush(); 
                }
                break;    


              /*-- ARCHIVE VOLUMES -------------------------------------------------------*/
              case GetArchivedOn:
                {
                  objOut.writeObject(pMasterMgr.getArchivedOn());
                  objOut.flush(); 
                }
                break;

              case GetRestoredOn:
                {
                  objOut.writeObject(pMasterMgr.getRestoredOn());
                  objOut.flush(); 
                }
                break;

              case GetArchivedOutput:
                {
                  MiscGetArchivedOutputReq req = 
                    (MiscGetArchivedOutputReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getArchivedOutput(req));
                  objOut.flush(); 
                }
                break;
	      
              case GetRestoredOutput:
                {
                  MiscGetRestoredOutputReq req = 
                    (MiscGetRestoredOutputReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getRestoredOutput(req));
                  objOut.flush(); 
                }
                break;
	      
              case GetArchivesContaining: 
                {
                  MiscGetArchivesContainingReq req = 
                    (MiscGetArchivesContainingReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getArchivesContaining(req));
                  objOut.flush(); 
                }
                break;    

              case GetArchive: 
                {
                  MiscGetArchiveReq req = (MiscGetArchiveReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.getArchive(req));
                  objOut.flush(); 
                }
                break;    


              /*-- USER INTERFACE --------------------------------------------------------*/
              case CreateInitialPanelLayout: 
                {
                  NodeWorkingAreaReq req = (NodeWorkingAreaReq) objIn.readObject();
                  objOut.writeObject(pMasterMgr.createInitialPanelLayout(req));
                  objOut.flush(); 
                }
                break;    


              /*-- NETWORK CONNECTION ----------------------------------------------------*/
              case Ping:
                {
                  objOut.writeObject(new SuccessRsp(new TaskTimer("Ping"))); 
                  objOut.flush(); 
                }
                break;

              case Disconnect:
		disconnect();
                break;

              case ShutdownOptions:
                {
                  MiscShutdownOptionsReq req = (MiscShutdownOptionsReq) objIn.readObject();
                  pMasterMgr.setShutdownOptions
                    (req.shutdownJobMgrs(), req.shutdownPluginMgr());
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
	   "Connection from (" + host + ":" + PackageInfo.sMasterPort + ") " + 
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
            "(" + host + ":" + PackageInfo.sMasterPort + "):", ex)); 
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
            "(" + host + ":" + PackageInfo.sMasterPort + "):", ex)); 
      }
      catch (Exception ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Severe,
	   Exceptions.getFullMessage(ex));
      }
      catch (LinkageError er) {
        LogMgr.getInstance().log
          (LogMgr.Kind.Net, LogMgr.Level.Severe,
           Exceptions.getFullMessage(er));
      }
      finally {
	closeConnection();

	for(Long editID : pRunningEditorIDs) 
	  pMasterMgr.editingFinished(new TaskTimer(), editID);

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

    private TreeSet<Long>  pRunningEditorIDs; 
  }
  
  /**
   * Node cache garbage collector. 
   */
  private 
  class CacheGCTask
    extends Thread
  {
    public 
    CacheGCTask() 
    {
      super("MasterMgrServer:CacheGCTask");
    }

    @Override
    public void 
    run() 
    {
      try {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Ops, LogMgr.Level.Fine,
	   "Cache Garbage Collector Started.");	

	while(!pShutdown.get()) {
	  pMasterMgr.cacheGC();
	}
      }
      catch (Exception ex) {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Ops, LogMgr.Level.Severe,
           Exceptions.getFullMessage("Cache Garbage Collector Failed:", ex)); 
      }
      finally {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Ops, LogMgr.Level.Fine,
	   "Cache Garbage Collector Finished.");	
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
	  pMasterMgr.backupSync(first);
          first = false;
	}
      }
      catch (Exception ex) {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Ops, LogMgr.Level.Severe,
           Exceptions.getFullMessage("Database Backup Synchronizer Failed:", ex)); 
      }
      finally {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Ops, LogMgr.Level.Fine,
	   "Database Backup Synchronizer Finished.");	
      }
    }
  }

  /**
   * Writes pending events to disk.
   */
  private 
  class EventWriterTask
    extends Thread
  {
    public 
    EventWriterTask() 
    {
      super("MasterMgrServer:EventWriterTask");
    }

    @Override
    public void 
    run() 
    {
      try {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Ops, LogMgr.Level.Fine,
	   "Event Writer Started.");	

	while(!pShutdown.get()) {
	  pMasterMgr.eventWriter();
	}
      }
      catch (Exception ex) {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Ops, LogMgr.Level.Severe,
	   Exceptions.getFullMessage("Event Writer Failed:", ex)); 
      }
      finally {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Ops, LogMgr.Level.Fine,
	   "Event Writer Finished.");	
      }
    }
  }

  /**
   * Periodic license expiration checker.
   */
  private 
  class LicenseTask
    extends Thread
  {
    public 
    LicenseTask() 
    {
      super("MasterMgrServer:LicenseTask");
    }

    @Override
    public void 
    run() 
    {
      while(!pShutdown.get()) {
	if(!pMasterApp.isLicenseValid()) {	  
	  LogMgr.getInstance().logAndFlush
	    (LogMgr.Kind.Ops, LogMgr.Level.Warning,
	     "License Expired Shutdown.");

          pMasterMgr.setShutdownOptions(true, true); 
          shutdown();
          return;
	}

	try {
          Long interval = pMasterApp.warnLicenseExpiration(); 
	  if(interval == null) 
            return;
          else 
            Thread.sleep(interval); 
	}
	catch(InterruptedException ex) {
	}	
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
   * The application instance.
   */ 
  private MasterApp  pMasterApp; 

  /**
   * The shared master manager. 
   */
  private MasterMgr  pMasterMgr;

  /**
   * The set of currently running tasks.
   */ 
  private TreeSet<HandlerTask>  pTasks;

}

