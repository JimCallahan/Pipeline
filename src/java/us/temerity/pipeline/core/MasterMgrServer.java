// $Id: MasterMgrServer.java,v 1.84 2008/02/14 20:26:29 jim Exp $

package us.temerity.pipeline.core;

import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.util.HashSet;
import java.util.TreeSet;

import us.temerity.pipeline.*;
import us.temerity.pipeline.event.EditedNodeEvent;
import us.temerity.pipeline.message.*;

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
   * @param preserveOfflinedCache
   *   Whether to keep the offlined versions cache file after startup and reread instead of 
   *   rebuilding it during a database rebuild.
   * 
   * @param internalFileMgr
   *   Whether the file manager should be run as a thread of plmaster(1).
   * 
   * @param avgNodeSize
   *   The estimated memory size of a node version (in bytes).
   * 
   * @param minOverhead
   *   The minimum amount of memory overhead to maintain at all times.
   * 
   * @param maxOverhead
   *   The maximum amount of memory overhead required to be available after a node garbage
   *   collection.
   * 
   * @param nodeGCInterval
   *   The minimum time a cycle of the node cache garbage collector loop should 
   *   take (in milliseconds).
   * 
   * @param restoreCleanupInterval
   *   The maximum age of a resolved (Restored or Denied) restore request before it 
   *   is deleted (in milliseconds).
   * 
   * @throws PipelineException 
   *   If unable to properly initialize the server.
   */
  public
  MasterMgrServer
  (
   MasterApp app, 
   boolean rebuildCache, 
   boolean preserveOfflinedCache, 
   boolean internalFileMgr, 
   long avgNodeSize, 
   long minOverhead, 
   long maxOverhead, 
   long nodeGCInterval, 
   long restoreCleanupInterval
  )
    throws PipelineException 
  { 
    super("MasterMgrServer");

    pTimer = new TaskTimer();

    pMasterApp = app;
    pMasterMgr = 
      new MasterMgr(rebuildCache, preserveOfflinedCache, internalFileMgr,  
		    avgNodeSize, minOverhead, maxOverhead, nodeGCInterval, 
		    restoreCleanupInterval);

    pTasks = new HashSet<HandlerTask>();
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
    ServerSocketChannel schannel = null;
    try {
      schannel = ServerSocketChannel.open();
      ServerSocket server = schannel.socket();
      InetSocketAddress saddr = new InetSocketAddress(PackageInfo.sMasterPort);
      server.bind(saddr, 100);

      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Fine,
	 "Listening on Port: " + PackageInfo.sMasterPort);
      pTimer.suspend();
      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Info,
	 "Server Ready.\n" + 
	 "  Started in " + TimeStamps.formatInterval(pTimer.getTotalDuration()));
      LogMgr.getInstance().flush();
      pTimer = new TaskTimer();

      NodeGCTask nodeGC = new NodeGCTask();
      nodeGC.start();

      EventWriterTask ewriter = new EventWriterTask();
      ewriter.start();

//    LicenseTask lic = new LicenseTask();
//    lic.start();

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
// 	lic.interrupt();
// 	lic.join();

	{
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Net, LogMgr.Level.Info,
	     "Waiting on Event Writer...");
	  LogMgr.getInstance().flush();

	  ewriter.join();
	}

	nodeGC.interrupt();
	nodeGC.join();

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
	 ("IO problems on port (" + PackageInfo.sMasterPort + "):", ex)); 
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
    }
    finally {
      if(schannel != null) {
	try {
	  schannel.close();
	}
	catch (IOException ex) {
	}
      }

      pMasterMgr.shutdown();

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
   * Handle an incoming connection from a <CODE>MasterMgrClient</CODE> instance.
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
      super("MasterMgrServer:HandlerTask");
      pChannel = channel;
      pRunningEditorIDs = new TreeSet<Long>(); 
    }

    @Override
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
	    MasterRequest kind = (MasterRequest) objIn.readObject();
	  
	    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finer)) {
	      LogMgr.getInstance().log
		(LogMgr.Kind.Net, LogMgr.Level.Finer,
		 "Request [" + pSocket.getInetAddress() + "]: " + kind.name());	  
	      LogMgr.getInstance().flush();
	    }

	    switch(kind) {
	    /*-- ADMINISTRATIVE PRIVILEGES -------------------------------------------------*/
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


	    /*-- LOGGING -------------------------------------------------------------------*/
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


	    /*-- RUNTIME PARAMETERS --------------------------------------------------------*/
	    case GetMasterControls:
	      {
		objOut.writeObject(pMasterMgr.getRuntimeControls());
		objOut.flush(); 
	      }
	      break;

	    case SetMasterControls:
	      {
		MiscSetMasterControlsReq req = (MiscSetMasterControlsReq) objIn.readObject();
		objOut.writeObject(pMasterMgr.setRuntimeControls(req));
		objOut.flush(); 
	      }
	      break;


	    /*-- TOOLSETS ------------------------------------------------------------------*/
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


	    /*-- TOOLSET PACKAGES ----------------------------------------------------------*/
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
		MiscGetToolsetPackageReq req = (MiscGetToolsetPackageReq) objIn.readObject();
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


	    /*-- PLUGIN MENUS / LAYOUTS ----------------------------------------------------*/
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

	    /*----------------------------------*/

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

	    /*----------------------------------*/

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

	    /*----------------------------------*/

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

	    /*----------------------------------*/
	      
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

	    /*----------------------------------*/
	      
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


	    /*----------------------------------*/
	      
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


	    /*----------------------------------*/
	      
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


	    /*----------------------------------*/
	      
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
	      
            /*----------------------------------*/
              
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


	    /*-- SERVER EXTENSIONS ---------------------------------------------------------*/
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


	    /*-- SUFFIX EDITORS ------------------------------------------------------------*/
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


	    /*-- WORKING AREAS -------------------------------------------------------------*/
	    case GetWorkingAreas:
	      {
		objOut.writeObject(pMasterMgr.getWorkingAreas());
		objOut.flush(); 
	      }
	      break;

	    case GetWorkingAreasContaining:
	      {
		NodeGetWorkingAreasContainingReq req = 
		  (NodeGetWorkingAreasContainingReq) objIn.readObject();
		objOut.writeObject(pMasterMgr.getWorkingAreasContaining(req));
		objOut.flush(); 
	      }
	      break;

	    case CreateWorkingArea:
	      {
		NodeCreateWorkingAreaReq req = (NodeCreateWorkingAreaReq) objIn.readObject();
		objOut.writeObject(pMasterMgr.createWorkingArea(req));
		objOut.flush(); 
	      }
	      break;

	    case RemoveWorkingArea:
	      {
		NodeRemoveWorkingAreaReq req = (NodeRemoveWorkingAreaReq) objIn.readObject();
		objOut.writeObject(pMasterMgr.removeWorkingArea(req));
		objOut.flush(); 
	      }
	      break;


	    /*-- NODE PATHS ----------------------------------------------------------------*/
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
	   
	    /*-- ANNOTATIONS ---------------------------------------------------------------*/
            case GetAnnotation:
	      {
		NodeGetAnnotationReq req = (NodeGetAnnotationReq) objIn.readObject();
		objOut.writeObject(pMasterMgr.getAnnotation(req));
		objOut.flush(); 
	      }
	      break; 

            case GetAnnotations:
	      {
		NodeGetAnnotationsReq req = (NodeGetAnnotationsReq) objIn.readObject();
		objOut.writeObject(pMasterMgr.getAnnotations(req));
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
		NodeRemoveAnnotationsReq req = (NodeRemoveAnnotationsReq) objIn.readObject();
		objOut.writeObject(pMasterMgr.removeAnnotations(req));
		objOut.flush(); 
	      }
	      break;

	    /*-- WORKING VERSIONS ----------------------------------------------------------*/
	    case GetWorkingNames:
	      {
		NodeGetWorkingNamesReq req = (NodeGetWorkingNamesReq) objIn.readObject();
		objOut.writeObject(pMasterMgr.getWorkingNames(req));
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


	    /*-- CHECKED-IN VERSIONS -------------------------------------------------------*/
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
		NodeGetAllCheckedInReq req = (NodeGetAllCheckedInReq) objIn.readObject();
		objOut.writeObject(pMasterMgr.getAllCheckedInVersions(req));
		objOut.flush(); 
	      }
	      break;

	    case GetCheckedInVersionIDs:
	      {
		NodeGetCheckedInVersionIDsReq req = 
		  (NodeGetCheckedInVersionIDsReq) objIn.readObject();
		objOut.writeObject(pMasterMgr.getCheckedInVersionIDs(req));
		objOut.flush(); 
	      }
	      break;

	    case GetHistory:
	      {
		NodeGetHistoryReq req = (NodeGetHistoryReq) objIn.readObject();
		objOut.writeObject(pMasterMgr.getHistory(req));
		objOut.flush(); 
	      }
	      break;

	    case GetCheckedInFileNovelty:
	      {
		NodeGetCheckedInFileNoveltyReq req = 
		  (NodeGetCheckedInFileNoveltyReq) objIn.readObject();
		objOut.writeObject(pMasterMgr.getCheckedInFileNovelty(req));
		objOut.flush(); 
	      }
	      break;

	    case GetCheckedInLinks:
	      {
		NodeGetCheckedInLinksReq req = 
		  (NodeGetCheckedInLinksReq) objIn.readObject();
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

	    
	    /*-- NODE STATUS ---------------------------------------------------------------*/
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


	    /*-- REVISION CONTROL ----------------------------------------------------------*/
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

            /*-- NODE BUNDLES --------------------------------------------------------------*/
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


	    /*-- NODE EVENTS ---------------------------------------------------------------*/
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
		  String hostname = addr.getCanonicalHostName();

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
		NodeGetWorkingAreasEditingReq req = 
		  (NodeGetWorkingAreasEditingReq) objIn.readObject();
		objOut.writeObject(pMasterMgr.getWorkingAreasEditing(req));
		objOut.flush(); 
	      }
	      break;
	   
	    /*-- JOBS ----------------------------------------------------------------------*/
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

	    /*-- ADMIN ---------------------------------------------------------------------*/
	    case BackupDatabase: 
	      {
		MiscBackupDatabaseReq req = (MiscBackupDatabaseReq) objIn.readObject();
		objOut.writeObject(pMasterMgr.backupDatabase(req));
		objOut.flush(); 
	      }
	      break;  

	    /*-- ARCHIVE -------------------------------------------------------------------*/
	    case ArchiveQuery: 
	      {
		MiscArchiveQueryReq req = (MiscArchiveQueryReq) objIn.readObject();
		objOut.writeObject(pMasterMgr.archiveQuery(req));
		objOut.flush(); 
	      }
	      break;  

	    case GetArchiveSizes:
	      {
		MiscGetArchiveSizesReq req = (MiscGetArchiveSizesReq) objIn.readObject();
		objOut.writeObject(pMasterMgr.getArchiveSizes(req));
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

	    /*-- OFFLINE -------------------------------------------------------------------*/
	    case OfflineQuery: 
	      {
		MiscOfflineQueryReq req = (MiscOfflineQueryReq) objIn.readObject();
		objOut.writeObject(pMasterMgr.offlineQuery(req));
		objOut.flush(); 
	      }
	      break;  

	    case GetOfflineVersionIDs:
	      {
		NodeGetOfflineVersionIDsReq req = 
		  (NodeGetOfflineVersionIDsReq) objIn.readObject();
		objOut.writeObject(pMasterMgr.getOfflineVersionIDs(req));
		objOut.flush(); 
	      }
	      break;

	    case GetOfflineSizes:
	      {
		MiscGetOfflineSizesReq req = (MiscGetOfflineSizesReq) objIn.readObject();
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


	    /*-- RESTORE -------------------------------------------------------------------*/
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
		MiscGetRestoreSizesReq req = (MiscGetRestoreSizesReq) objIn.readObject();
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


	    /*-- ARCHIVE VOLUMES -----------------------------------------------------------*/
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
		MiscGetArchivedOutputReq req = (MiscGetArchivedOutputReq) objIn.readObject();
		objOut.writeObject(pMasterMgr.getArchivedOutput(req));
		objOut.flush(); 
	      }
	      break;
	      
	    case GetRestoredOutput:
	      {
		MiscGetRestoredOutputReq req = (MiscGetRestoredOutputReq) objIn.readObject();
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


	    /*-- USER INTERFACE ------------------------------------------------------------*/
	    case CreateInitialPanelLayout: 
	      {
		MiscCreateInitialPanelLayoutReq req = 
		  (MiscCreateInitialPanelLayoutReq) objIn.readObject();
		objOut.writeObject(pMasterMgr.createInitialPanelLayout(req));
		objOut.flush(); 
	      }
	      break;    


	    /*-- NETWORK CONNECTION --------------------------------------------------------*/
	    case Disconnect:
	      live = false;
	      break;

	    case ShutdownOptions:
	      {
		MiscShutdownOptionsReq req = (MiscShutdownOptionsReq) objIn.readObject();
		pMasterMgr.setShutdownOptions(req.shutdownJobMgrs(), req.shutdownPluginMgr());
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
	   "Connection from (" + host + ":" + PackageInfo.sMasterPort + ") " + 
	   "terminated abruptly!");
      }
      catch (IOException ex) {
	InetAddress addr = pSocket.getInetAddress(); 
	String host = "???";
	if(addr != null) 
	  host = addr.getCanonicalHostName();

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
	  host = addr.getCanonicalHostName();

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

      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Fine,
	 "Client Connection Closed.");
      LogMgr.getInstance().flush();
    }
    
    private SocketChannel  pChannel; 
    private Socket         pSocket;

    private TreeSet<Long>  pRunningEditorIDs; 
  }
  
  /**
   * Node cache garbage collector. 
   */
  private 
  class NodeGCTask
    extends Thread
  {
    public 
    NodeGCTask() 
    {
      super("MasterMgrServer:NodeGCTask");
    }

    @Override
    public void 
    run() 
    {
      try {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Mem, LogMgr.Level.Fine,
	   "Node Garbage Collector Started.");	
	LogMgr.getInstance().flush();

	while(!pShutdown.get()) {
	  pMasterMgr.nodeGC();
	}
      }
      catch (Exception ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Mem, LogMgr.Level.Severe,
           Exceptions.getFullMessage("Node Garbage Collector Failed:", ex)); 
	LogMgr.getInstance().flush();	
      }
      finally {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Mem, LogMgr.Level.Fine,
	   "Node Garbage Collector Finished.");	
	LogMgr.getInstance().flush();
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
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Fine,
	   "Event Writer Started.");	
	LogMgr.getInstance().flush();

	while(!pShutdown.get()) {
	  pMasterMgr.eventWriter();
	}
      }
      catch (Exception ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Severe,
	   Exceptions.getFullMessage("Event Writer Failed:", ex)); 
	LogMgr.getInstance().flush();
      }
      finally {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Fine,
	   "Event Writer Finished.");	
	LogMgr.getInstance().flush();
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
      /* check once a day */ 
      while(!pShutdown.get()) {
	try {
	  Thread.sleep(86400000L); 
	}
	catch(InterruptedException ex) {
	}	

	if(!pMasterApp.isLicenseValid()) {	  
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Net, LogMgr.Level.Warning,
	     "License Expired Shutdown.");
	  LogMgr.getInstance().flush();

	  pShutdown.set(true);
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
  private HashSet<HandlerTask>  pTasks;
}

