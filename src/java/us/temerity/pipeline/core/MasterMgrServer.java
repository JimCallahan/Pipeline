// $Id: MasterMgrServer.java,v 1.22 2004/10/31 20:01:55 jim Exp $

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
 * 
 * @see MasterMgr
 * @see MasterMgrClient
 */
public
class MasterMgrServer
  extends Thread
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new master manager server.
   * 
   * @param nodeDir 
   *   The root node directory.
   * 
   * @param nodePort 
   *   The network port to monitor for incoming connections.
   * 
   * @param prodDir 
   *   The root production directory.
   * 
   * @param fileHost 
   *   The name of the host running the <B>plfilemgr</B><A>(1) and <B>plnotify</B><A>(1) 
   *   daemons.
   * 
   * @param filePort 
   *   The network port listened to by the <B>plfilemgr</B><A>(1) daemon.
   * 
   * @param controlPort 
   *   The network port listened to by the <B>plnotify</B><A>(1) daemon for 
   *   control connections.
   * 
   * @param monitorPort 
   *   The network port listened to by the <B>plnotify</B><A>(1) daemon for 
   *   monitor connections.
   * 
   * @param queueHost
   *   The hostname running <B>plqueuemgr</B>(1).
   * 
   * @param queuePort
   *   The port number listened to by <B>plqueuemgr</B>(1) for incoming connections.
   */
  public
  MasterMgrServer
  (
   File nodeDir, 
   int nodePort, 
   File prodDir, 
   String fileHost, 
   int filePort,
   int controlPort, 
   int monitorPort, 
   String queueHost, 
   int queuePort
  )
  { 
    super("MasterMgrServer");
    init(nodeDir, nodePort, 
	 prodDir, fileHost, filePort, controlPort, monitorPort, 
	 queueHost, queuePort);
  }
  
  /** 
   * Construct a new master manager using the default root node directory and 
   * network ports.
   * 
   * The root node directory is specified by the <B>--node-dir</B>=<I>dir</I>
   * option to <B>plconfig</B>(1). <P> 
   * 
   * The network port to monitor for incoming connections is specified by the 
   * <B>--master-port</B>=<I>dir</I> option to <B>plconfig</B>(1). <P>
   * 
   * The root production directory is specified by the <B>--prod-dir</B>=<I>dir</I>
   * option to <B>plconfig</B>(1). <P> 
   * 
   * The file server hostname is specified by the <B>--file-host</B>=<I>host</I>
   * option to <B>plconfig</B>(1). <P>  
   * 
   * The network port listened to by the <B>plfilemgr</B><A>(1) daemon is specified 
   * by the <B>--file-port</B>=<I>dir</I> option to <B>plconfig</B>(1). <P>
   * 
   * The network port listened to by the <B>plnotify</B><A>(1) daemon is specified 
   * by the <B>--notify-control-port</B>=<I>num</I> and 
   * <B>--notify-monitor-port</B>=<I>num</I> options to <B>plconfig</B>(1). <P>
   * 
   * The queue server hostname is specified by the <B>--queue-host</B>=<I>host</I>
   * option to <B>plconfig</B>(1). <P>  
   * 
   * The port number listened to by <B>plqueuemgr</B>(1) for incoming connections is 
   * specified by the <B>--queue-port</B>=<I>num</I> option to <B>plconfig</B>(1). 
   */
  public
  MasterMgrServer() 
  { 
    super("MasterMgrServer");
    init(PackageInfo.sNodeDir, PackageInfo.sMasterPort, 
	 PackageInfo.sProdDir, PackageInfo.sFileServer, PackageInfo.sFilePort, 
	 PackageInfo.sNotifyControlPort, PackageInfo.sNotifyMonitorPort, 
	 PackageInfo.sQueueServer, PackageInfo.sQueuePort);
  }


  /*-- CONSTRUCTION HELPERS ----------------------------------------------------------------*/

  /**
   * Initialize a new instance.
   * 
   * @param nodeDir 
   *   The root node directory.
   * 
   * @param nodePort 
   *   The network port to monitor for incoming connections.
   * 
   * @param prodDir 
   *   The root production directory.
   * 
   * @param fileHost 
   *   The name of the host running the <B>plfilemgr</B><A>(1) and <B>plnotify</B><A>(1) 
   *   daemons.
   * 
   * @param filePort 
   *   The network port listened to by <B>plfilemgr</B><A>(1) daemon.
   * 
   * @param controlPort 
   *   The network port listened to by the <B>plnotify</B><A>(1) daemon for 
   *   control connections.
   * 
   * @param monitorPort 
   *   The network port listened to by the <B>plnotify</B><A>(1) daemon for 
   *   monitor connections.
   * 
   * @param queueHost
   *   The hostname running <B>plqueuemgr</B>(1).
   * 
   * @param queuePort
   *   The port number listened to by <B>plqueuemgr</B>(1) for incoming connections.
   */
  private synchronized void 
  init
  (
   File nodeDir, 
   int nodePort, 
   File prodDir, 
   String fileHost, 
   int filePort,
   int controlPort, 
   int monitorPort, 
   String queueHost, 
   int queuePort
  )
  { 
    pMasterMgr = new MasterMgr(nodeDir, prodDir, 
			       fileHost, filePort, controlPort, monitorPort, 
			       queueHost, queuePort);

    if(nodePort < 0) 
      throw new IllegalArgumentException("Illegal port number (" + nodePort + ")!");
    pPort = nodePort;

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
      InetSocketAddress saddr = new InetSocketAddress(pPort);
      server.bind(saddr, 100);

      Logs.net.fine("Listening on Port: " + pPort);
      Logs.net.info("Server Ready.");
      Logs.flush();

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
	Logs.net.finer("Shutting Down -- Waiting for tasks to complete...");
	Logs.flush();

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
	Logs.net.severe("Interrupted while shutting down!");
	Logs.flush();
      }
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
    catch (Exception ex) {
      Logs.net.severe(ex.getMessage());
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

      Logs.net.info("Server Shutdown.");
      Logs.flush();
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
	Logs.net.fine("Connection Opened: " + pSocket.getInetAddress());
	Logs.flush();

	boolean live = true;
	while(pSocket.isConnected() && live && !pShutdown.get()) {
	  InputStream in    = pSocket.getInputStream();
	  ObjectInput objIn = new ObjectInputStream(in);
	  MasterRequest kind  = (MasterRequest) objIn.readObject();
	  
	  OutputStream out    = pSocket.getOutputStream();
	  ObjectOutput objOut = new ObjectOutputStream(out);
	  
	  Logs.net.finer("Request [" + pSocket.getInetAddress() + "]: " + kind.name());	  
	  Logs.flush();

	  switch(kind) {
	  /*-- TOOLSETS --------------------------------------------------------------------*/
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
	      objOut.writeObject(pMasterMgr.getToolsetNames());
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


	  /*-- TOOLSET PACKAGES ------------------------------------------------------------*/
	  case GetToolsetPackageNames:
	    {
	      objOut.writeObject(pMasterMgr.getToolsetPackageNames());
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

	  case CreateToolsetPackage:
	    {
	      MiscCreateToolsetPackageReq req = 
		(MiscCreateToolsetPackageReq) objIn.readObject();
	      objOut.writeObject(pMasterMgr.createToolsetPackage(req));
	      objOut.flush(); 
	    }
	    break;


	  /*-- SUFFIX EDITORS --------------------------------------------------------------*/
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


	  /*-- PRIVILEGED USER STATUS ------------------------------------------------------*/
	  case GetPrivilegedUsers:
	    {
	      objOut.writeObject(pMasterMgr.getPrivilegedUsers());
	      objOut.flush(); 
	    }
	    break;

	  case GrantPrivileges:
	    {
	      MiscGrantPrivilegesReq req = (MiscGrantPrivilegesReq) objIn.readObject();
	      objOut.writeObject(pMasterMgr.grantPrivileges(req));
	      objOut.flush(); 
	    }
	    break;
	    
	  case RemovePrivileges:
	    {
	      MiscRemovePrivilegesReq req = (MiscRemovePrivilegesReq) objIn.readObject();
	      objOut.writeObject(pMasterMgr.removePrivileges(req));
	      objOut.flush(); 
	    }
	    break;


	  /*-- WORKING AREAS ---------------------------------------------------------------*/
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


	  /*-- NODE PATHS ------------------------------------------------------------------*/
	  case UpdatePaths:
	    {
	      NodeUpdatePathsReq req = (NodeUpdatePathsReq) objIn.readObject();
	      objOut.writeObject(pMasterMgr.updatePaths(req));
	      objOut.flush(); 
	    }
	    break;


	  /*-- WORKING VERSIONS ------------------------------------------------------------*/
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


	  /*-- CHECKED-IN VERSIONS ---------------------------------------------------------*/
	  case GetCheckedIn:
	    {
	      NodeGetCheckedInReq req = (NodeGetCheckedInReq) objIn.readObject();
	      objOut.writeObject(pMasterMgr.getCheckedInVersion(req));
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

	    
	  /*-- NODE STATUS -----------------------------------------------------------------*/
	  case Status:
	    {
	      NodeStatusReq req = (NodeStatusReq) objIn.readObject();
	      objOut.writeObject(pMasterMgr.status(req));
	      objOut.flush(); 
	    }
	    break;


	  /*-- REVISION CONTROL ------------------------------------------------------------*/
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

	  case RevertFiles:
	    {
	      NodeRevertFilesReq req = (NodeRevertFilesReq) objIn.readObject();
	      objOut.writeObject(pMasterMgr.revertFiles(req));
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


	  /*-- JOBS ------------------------------------------------------------------------*/
	  case SubmitJobs: 
	    {
	      NodeSubmitJobsReq req = (NodeSubmitJobsReq) objIn.readObject();
	      objOut.writeObject(pMasterMgr.submitJobs(req));
	      objOut.flush(); 
	    }
	    break;  


	  /*-- ADMINISTRATION --------------------------------------------------------------*/
	  case BackupDatabase: 
	    {
	      MiscBackupDatabaseReq req = (MiscBackupDatabaseReq) objIn.readObject();
	      objOut.writeObject(pMasterMgr.backupDatabase(req));
	      objOut.flush(); 
	    }
	    break;  


	  /*-- NETWORK CONNECTION ----------------------------------------------------------*/
	  case Disconnect:
	    live = false;
	    break;

	  case Shutdown:
	    Logs.net.warning("Shutdown Request Received: " + pSocket.getInetAddress());

	    // DEBUG 
	    pMasterMgr.logNodeTree();
	    // DEBUG
      
	    Logs.flush();
	    pShutdown.set(true);
	    break;	    

	  default:
	    assert(false);
	  }
	}
      }
      catch(AsynchronousCloseException ex) {
      }
      catch (IOException ex) {
	Logs.net.severe("IO problems on port (" + pPort + "):\n" + 
			ex.getMessage());
	ex.printStackTrace();
      }
      catch(ClassNotFoundException ex) {
	Logs.net.severe("Illegal object encountered on port (" + pPort + "):\n" + 
			ex.getMessage());	
      }
      catch (Exception ex) {
	Logs.net.severe(ex.getMessage());
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

      Logs.net.fine("Client Connection Closed.");
      Logs.flush();
    }
    
    private SocketChannel  pChannel; 
    private Socket         pSocket;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The shared master manager. 
   */
  private MasterMgr  pMasterMgr;

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

