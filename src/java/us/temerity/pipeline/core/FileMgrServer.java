// $Id: FileMgrServer.java,v 1.30 2005/03/30 20:37:29 jim Exp $

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
/*   F I L E   M G R   S E R V E R                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The server-side manager of file system queries and operations. <P> 
 * 
 * This class handles network communication with {@link FileMgrClient FileMgrClient} 
 * instances running on remote hosts.  This class listens for new connections from  
 * <CODE>FileMgrClient</CODE> instances and creats a thread to manage each connection.
 * Each of these threads then listens for requests for file system related operations 
 * and dispatches these requests to an underlying instance of the {@link FileMgr FileMgr}
 * class.
 */
class FileMgrServer
  extends Thread
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new file manager server.
   * 
   * @param dir 
   *   The root production directory.
   * 
   * @param port 
   *   The network port to monitor for incoming connections.
   */
  public
  FileMgrServer
  (
   File dir, 
   int port
  )
  { 
    super("FileMgrServer");

    pFileMgr = new FileMgr(dir);

    if(port < 0) 
      throw new IllegalArgumentException("Illegal port number (" + port + ")!");
    pPort = port;

    pShutdown = new AtomicBoolean(false);
    pTasks    = new HashSet<HandlerTask>();    
  }
  
  /** 
   * Construct a new file manager using the default root production directory and 
   * network port.
   * 
   * The root production directory and network port used are those specified by 
   * <B>plconfig(1)</B>.
   */
  public
  FileMgrServer() 
  { 
    this(PackageInfo.sProdDir, PackageInfo.sFilePort);
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
      
      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Fine,
	 "Listening on Port: " + pPort);
      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Info,
	 "Server Ready.");
      LogMgr.getInstance().flush();

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
	 "IO problems on port (" + pPort + "):\n" + 
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
   * Handle an incoming connection from a <CODE>FileMgrClient</CODE> instance.
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
      super("FileMgrServer:HandlerTask");
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
	  InputStream in    = pSocket.getInputStream();
	  ObjectInput objIn = new PluginInputStream(in);
	  Object obj        = objIn.readObject();
	  
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
	    FileRequest kind = (FileRequest) obj;

	    LogMgr.getInstance().log
	      (LogMgr.Kind.Net, LogMgr.Level.Finer,
	       "Request [" + pSocket.getInetAddress() + "]: " + kind.name());	  
	    LogMgr.getInstance().flush();

	    switch(kind) {
	    /*-- WORKING VERSIONS ----------------------------------------------------------*/
	    case CreateWorkingArea:
	      {
		FileCreateWorkingAreaReq req = (FileCreateWorkingAreaReq) objIn.readObject();
		objOut.writeObject(pFileMgr.createWorkingArea(req));
		objOut.flush(); 
	      }
	      break;


	    case RemoveWorkingArea:
	      {
		FileRemoveWorkingAreaReq req = (FileRemoveWorkingAreaReq) objIn.readObject();
		objOut.writeObject(pFileMgr.removeWorkingArea(req));
		objOut.flush(); 
	      }
	      break;

            /*-- REVISION CONTROL ----------------------------------------------------------*/
	    case CheckIn:
	      {
		FileCheckInReq req = (FileCheckInReq) objIn.readObject();
		objOut.writeObject(pFileMgr.checkIn(req));
		objOut.flush(); 
	      }
	      break;

	    case CheckOut:
	      {
		FileCheckOutReq req = (FileCheckOutReq) objIn.readObject();
		objOut.writeObject(pFileMgr.checkOut(req));
		objOut.flush(); 
	      }
	      break;

	    case Revert:
	      {
		FileRevertReq req = (FileRevertReq) objIn.readObject();
		objOut.writeObject(pFileMgr.revert(req));
		objOut.flush(); 
	      }
	      break;

	    case Clone:
	      {
		FileCloneReq req = (FileCloneReq) objIn.readObject();
		objOut.writeObject(pFileMgr.clone(req));
		objOut.flush(); 
	      }
	      break;
	    
	    case State:
	      {
		FileStateReq req = (FileStateReq) objIn.readObject();
		objOut.writeObject(pFileMgr.states(req));
		objOut.flush(); 
	      }
	      break;
	    
	    case Remove:
	      {
		FileRemoveReq req = (FileRemoveReq) objIn.readObject();
		objOut.writeObject(pFileMgr.remove(req));
		objOut.flush(); 
	      }
	      break;
	    
	    case RemoveAll:
	      {
		FileRemoveAllReq req = (FileRemoveAllReq) objIn.readObject();
		objOut.writeObject(pFileMgr.removeAll(req));
		objOut.flush(); 
	      }
	      break;
	    
	    case Rename:
	      {
		FileRenameReq req = (FileRenameReq) objIn.readObject();
		objOut.writeObject(pFileMgr.rename(req));
		objOut.flush(); 
	      }
	      break;

	    case ChangeMode:
	      {
		FileChangeModeReq req = (FileChangeModeReq) objIn.readObject();
		objOut.writeObject(pFileMgr.changeMode(req));
		objOut.flush(); 
	      }
	      break;

	    case DeleteCheckedIn:
	      {
		FileDeleteCheckedInReq req = (FileDeleteCheckedInReq) objIn.readObject();
		objOut.writeObject(pFileMgr.deleteCheckedIn(req));
		objOut.flush(); 
	      }
	      break;

	    /*-- ARCHIVE -------------------------------------------------------------------*/
	    case GetArchiveSizes:
	      {
		FileGetArchiveSizesReq req = (FileGetArchiveSizesReq) objIn.readObject();
		objOut.writeObject(pFileMgr.getArchiveSizes(req));
		objOut.flush(); 
	      }
	      break;
	    
	    case Archive:
	      {
		FileArchiveReq req = (FileArchiveReq) objIn.readObject();
		objOut.writeObject(pFileMgr.archive(req));
		objOut.flush(); 
	      }
	      break;

	    /*-- OFFLINE -------------------------------------------------------------------*/
	    case GetOfflineSizes:
	      {
		FileGetOfflineSizesReq req = (FileGetOfflineSizesReq) objIn.readObject();
		objOut.writeObject(pFileMgr.getOfflineSizes(req));
		objOut.flush(); 
	      }
	      break;

	    case Offline:
	      {
		FileOfflineReq req = (FileOfflineReq) objIn.readObject();
		objOut.writeObject(pFileMgr.offline(req));
		objOut.flush(); 
	      }
	      break;

	    case GetOfflined:
	      {
		objOut.writeObject(pFileMgr.getOfflined());
		objOut.flush(); 
	      }
	      break;

            /*-- RESTORE -------------------------------------------------------------------*/
	    case Extract:
	      {
		FileExtractReq req = (FileExtractReq) objIn.readObject();
		objOut.writeObject(pFileMgr.extract(req));
		objOut.flush(); 
	      }
	      break;

	    case Restore:
	      {
		FileRestoreReq req = (FileRestoreReq) objIn.readObject();
		objOut.writeObject(pFileMgr.restore(req));
		objOut.flush(); 
	      }
	      break;

	    case ExtractCleanup:
	      {
		FileExtractCleanupReq req = (FileExtractCleanupReq) objIn.readObject();
		objOut.writeObject(pFileMgr.extractCleanup(req));
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
	      assert(false);
	    }
	  }
	}
      }
      catch(AsynchronousCloseException ex) {
      }
      catch (EOFException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Severe,
	   "Connection on port (" + pPort + ") terminated abruptly!");	
      }
      catch (IOException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Severe,
	   "IO problems on port (" + pPort + "):\n" + 
	   ex.getMessage());
      }
      catch(ClassNotFoundException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Severe,
	   "Illegal object encountered on port (" + pPort + "):\n" + 
	   ex.getMessage());	
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
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The shared file manager. 
   */
  private FileMgr  pFileMgr;

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

