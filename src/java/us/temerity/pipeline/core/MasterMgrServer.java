// $Id: MasterMgrServer.java,v 1.62 2006/07/03 06:38:42 jim Exp $

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
  extends Thread
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
   * @param internalFileMgr
   *   Whether the file manager should be run as a thread of plmaster(1).
   * 
   * @param nodeCacheLimit
   *   The maximum number of node versions to cache in memory.
   * 
   * @throws PipelineException 
   *   If unable to properly initialize the server.
   */
  public
  MasterMgrServer
  (
   MasterApp app, 
   boolean rebuildCache, 
   boolean internalFileMgr, 
   long nodeCacheLimit
  )
    throws PipelineException 
  { 
    super("MasterMgrServer");

    pMasterApp = app;
    pMasterMgr = new MasterMgr(rebuildCache, internalFileMgr, nodeCacheLimit);

    pShutdown = new AtomicBoolean(false);
    pTasks    = new HashSet<HandlerTask>();
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
    ServerSocketChannel schannel = null;
    try {
      schannel = ServerSocketChannel.open();
      ServerSocket server = schannel.socket();
      InetSocketAddress saddr = new InetSocketAddress(PackageInfo.sMasterPort);
      server.bind(saddr, 100);

      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Fine,
	 "Listening on Port: " + PackageInfo.sMasterPort);
      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Info,
	 "Server Ready.");
      LogMgr.getInstance().flush();

      NodeGCTask nodeGC = new NodeGCTask();
      nodeGC.start();

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
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Finer,
	   "Shutting Down -- Waiting for tasks to complete...");
	LogMgr.getInstance().flush();

// 	lic.interrupt();
// 	lic.join();

	nodeGC.interrupt();
	nodeGC.join();

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
	 "IO problems on port (" + PackageInfo.sMasterPort + "):\n" + 
	 ex.getMessage());
      LogMgr.getInstance().flush();
    }
    catch (SecurityException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Severe,
	 "The Security Manager doesn't allow listening to sockets!\n" + 
	 ex.getMessage());
      LogMgr.getInstance().flush();
    }
    catch (Exception ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Severe,
	 ex.getMessage());
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

      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Info,
	 "Server Shutdown.");
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
	    MasterRequest kind = (MasterRequest) obj;
	  
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Net, LogMgr.Level.Finer,
	       "Request [" + pSocket.getInetAddress() + "]: " + kind.name());	  
	    LogMgr.getInstance().flush();

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

	    case GetWorkingNames:
	      {
		NodeGetWorkingNamesReq req = (NodeGetWorkingNamesReq) objIn.readObject();
		objOut.writeObject(pMasterMgr.getWorkingNames(req));
		objOut.flush(); 
	      }
	      break;


	    /*-- NODE PATHS ----------------------------------------------------------------*/
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


	    /*-- WORKING VERSIONS ----------------------------------------------------------*/
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

	    
	    /*-- NODE STATUS ---------------------------------------------------------------*/
	    case Status:
	      {
		NodeStatusReq req = (NodeStatusReq) objIn.readObject();
		objOut.writeObject(pMasterMgr.status(req));
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
	   "IO problems on connection from " + 
	   "(" + host + ":" + PackageInfo.sMasterPort + "):\n" + 
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
	   "(" + host + ":" + PackageInfo.sMasterPort + "):\n" + 
	   getFullMessage(ex));
      }
      catch (Exception ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Severe,
	   ex.getMessage());
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
	   "Node Garbage Collector Failed: " + getFullMessage(ex));	
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
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The application instance.
   */ 
  private MasterApp  pMasterApp; 

  /**
   * The shared master manager. 
   */
  private MasterMgr  pMasterMgr;

  /**
   * Has the server been ordered to shutdown?
   */
  private AtomicBoolean  pShutdown;

  /**
   * The set of currently running tasks.
   */ 
  private HashSet<HandlerTask>  pTasks;
}

