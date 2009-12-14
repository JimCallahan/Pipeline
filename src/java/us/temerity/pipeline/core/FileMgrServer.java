// $Id: FileMgrServer.java,v 1.53 2009/12/14 21:48:22 jim Exp $

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
  extends BaseMgrServer
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new file manager server.
   * 
   * @param fileStatDir
   *   An alternative root production directory accessed via a different NFS mount point
   *   to provide an exclusively network for file status query traffic.  Setting this to 
   *   <CODE>null</CODE> will cause the default root production directory to be used instead.
   * 
   * @param checksumDir
   *   An alternative root production directory accessed via a different NFS mount point
   *   to provide an exclusively network for checksum generation traffic.  Setting this to 
   *   <CODE>null</CODE> will cause the default root production directory to be used instead.
   */
  public
  FileMgrServer
  (
   Path fileStatDir, 
   Path checksumDir
  ) 
  { 
    super("FileMgrServer");

    pFileMgr = new FileMgr(true, fileStatDir, checksumDir);
    pTasks   = new TreeSet<HandlerTask>();    
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
    try {
      pSocketChannel = ServerSocketChannel.open();
      ServerSocket server = pSocketChannel.socket();
      InetSocketAddress saddr = new InetSocketAddress(PackageInfo.sFilePort);
      server.bind(saddr, 100);
      
      LogMgr.getInstance().logAndFlush
	(LogMgr.Kind.Net, LogMgr.Level.Fine,
	 "Listening on Port: " + PackageInfo.sFilePort);
      LogMgr.getInstance().logAndFlush
	(LogMgr.Kind.Net, LogMgr.Level.Info,
	 "Server Ready.");
   
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
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Net, LogMgr.Level.Finer,
	   "Shutting Down -- Waiting for tasks to complete...");

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
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Net, LogMgr.Level.Severe,
	   "Interrupted while shutting down!");
      }
    }
    catch (IOException ex) {
      LogMgr.getInstance().logAndFlush
	(LogMgr.Kind.Net, LogMgr.Level.Severe,
         Exceptions.getFullMessage
	 ("IO problems on port (" + PackageInfo.sFilePort + "):", ex)); 
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

      LogMgr.getInstance().logAndFlush
	(LogMgr.Kind.Net, LogMgr.Level.Info,
	 "Server Shutdown.");    
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
    extends BaseHandlerTask
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

    @Override
    protected String
    verifyClient
    (
     String clientID
    )
    {
      if(!clientID.equals("FileMgrNet")) {
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

    public void 
    run() 
    {
      try {
	pSocket = pChannel.socket();
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Net, LogMgr.Level.Fine,
	   "Connection Opened: " + pSocket.getInetAddress());

	while(pSocket.isConnected() && isLive() && !pShutdown.get()) {
	  InputStream in    = pSocket.getInputStream();
	  ObjectInput objIn = new PluginInputStream(in);
	  Object obj        = objIn.readObject();
	  
	  OutputStream out    = pSocket.getOutputStream();
	  ObjectOutput objOut = new ObjectOutputStream(out);

	  if(isFirst())
	    verifyConnection(obj, objOut);
	  else {
            /* check time difference between client and server */ 
            checkTimeSync((Long) obj, pSocket); 

            /* dispatch request by kind */ 
	    FileRequest kind = (FileRequest) objIn.readObject();

	    LogMgr.getInstance().logAndFlush
	      (LogMgr.Kind.Net, LogMgr.Level.Finer,
	       "Request [" + pSocket.getInetAddress() + "]: " + kind.name());	  

            try {
              switch(kind) {
              /*-- RUNTIME PARAMETERS ----------------------------------------------------*/
              case GetMasterControls:
                {
                  objOut.writeObject(pFileMgr.getRuntimeControls());
                  objOut.flush(); 
                }
                break;

              case SetMasterControls:
                {
                  MiscSetMasterControlsReq req = 
                    (MiscSetMasterControlsReq) objIn.readObject();
                  objOut.writeObject(pFileMgr.setRuntimeControls(req));
                  objOut.flush(); 
                }
                break;

              /*-- WORKING VERSIONS --------------------------------------------------------*/
              case ValidateScratchDir: 
                {
                  objOut.writeObject(pFileMgr.validateScratchDir());
                  objOut.flush(); 
                }
                break;

              /*-- WORKING VERSIONS --------------------------------------------------------*/
              case CreateWorkingArea:
                {
                  FileCreateWorkingAreaReq req = 
                    (FileCreateWorkingAreaReq) objIn.readObject();
                  objOut.writeObject(pFileMgr.createWorkingArea(req));
                  objOut.flush(); 
                }
                break;

              case RemoveWorkingArea:
                {
                  FileRemoveWorkingAreaReq req = 
                    (FileRemoveWorkingAreaReq) objIn.readObject();
                  objOut.writeObject(pFileMgr.removeWorkingArea(req));
                  objOut.flush(); 
                }
                break;

              /*-- REVISION CONTROL --------------------------------------------------------*/
              case State:
                {
                  FileStateReq req = (FileStateReq) objIn.readObject();
                  objOut.writeObject(pFileMgr.states(req));
                  objOut.flush(); 
                }
                break;
	    
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

              case TouchAll:
                {
                  FileTouchAllReq req = (FileTouchAllReq) objIn.readObject();
                  objOut.writeObject(pFileMgr.touchAll(req));
                  objOut.flush(); 
                }
                break;

              case GetWorkingTimeStamps:
                {
                  FileGetWorkingTimeStampsReq req = 
                    (FileGetWorkingTimeStampsReq) objIn.readObject();
                  objOut.writeObject(pFileMgr.getWorkingTimeStamps(req)); 
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

              /*-- NODE BUNDLES ------------------------------------------------------------*/
              case PackNodes:
                {
                  FilePackNodesReq req = (FilePackNodesReq) objIn.readObject();
                  objOut.writeObject(pFileMgr.packNodes(req));
                  objOut.flush(); 
                }
                break;  

              case ExtractBundle:
                {
                  FileExtractBundleReq req = (FileExtractBundleReq) objIn.readObject();
                  objOut.writeObject(pFileMgr.extractBundle(req));
                  objOut.flush(); 
                }
                break;

              case UnpackNodes:
                {
                  FileUnpackNodesReq req = (FileUnpackNodesReq) objIn.readObject();
                  objOut.writeObject(pFileMgr.unpackNodes(req));
                  objOut.flush(); 
                }
                break;

              /*-- SITE VERSIONS -----------------------------------------------------------*/
              case ExtractSiteVersion:
                {
                  FileExtractSiteVersionReq req = 
                    (FileExtractSiteVersionReq) objIn.readObject();
                  objOut.writeObject(pFileMgr.extractSiteVersion(req));
                  objOut.flush(); 
                }
                break;

              case LookupSiteVersion:
                {
                  FileSiteVersionReq req = (FileSiteVersionReq) objIn.readObject();
                  objOut.writeObject(pFileMgr.lookupSiteVersion(req));
                  objOut.flush(); 
                }
                break;

              case InsertSiteVersion:
                {
                  FileSiteVersionReq req = (FileSiteVersionReq) objIn.readObject();
                  objOut.writeObject(pFileMgr.insertSiteVersion(req));
                  objOut.flush(); 
                }
                break;


              /*-- ARCHIVE -----------------------------------------------------------------*/
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

              /*-- OFFLINE -----------------------------------------------------------------*/
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

              case GetOfflinedNodeVersions:
                {
                  FileGetOfflinedNodeVersionsReq req = 
                    (FileGetOfflinedNodeVersionsReq) objIn.readObject();
                  objOut.writeObject(pFileMgr.getOfflinedNodeVersions(req)); 
                  objOut.flush(); 
                }
                break;

              /*-- RESTORE -----------------------------------------------------------------*/
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

              /*-- NETWORK CONNECTION ------------------------------------------------------*/
              case Disconnect:
		disconnect();
                break;

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
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Severe,
	   "Connection on port (" + PackageInfo.sFilePort + ") terminated abruptly!");	
      }
      catch (IOException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Severe,
           Exceptions.getFullMessage
	   ("IO problems on port (" + PackageInfo.sFilePort + "):", ex)); 
      }
      catch(ClassNotFoundException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Severe,
           Exceptions.getFullMessage
	   ("Illegal object encountered on port (" + PackageInfo.sFilePort + "):", ex)); 
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
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The shared file manager. 
   */
  private FileMgr  pFileMgr;
  
  /**
   * The set of currently running tasks.
   */ 
  private TreeSet<HandlerTask>  pTasks;
}

