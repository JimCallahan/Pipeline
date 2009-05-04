// $Id: PluginMgrServer.java,v 1.20 2009/05/04 22:38:34 jim Exp $

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
/*   P L U G I N   M G R   S E R V E R                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The manager of Pipeline plugin classes. <P> 
 * 
 * This class handles network communication with {@link PluginMgrClient PluginMgrClient} 
 * instances running on remote hosts.  This class listens for new connections from  
 * <CODE>PluginMgrClient</CODE> instances and creats a thread to manage each connection.
 * Each of these threads then listens for requests for plugin related operations 
 * and dispatches these requests to an underlying instance of the {@link PluginMgr PluginMgr}
 * class.
 */
class PluginMgrServer
  extends BaseMgrServer
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new plugin manager server.
   */
  public
  PluginMgrServer()
  {
    this(null);
  }

  /** 
   * Construct a new plugin manager server.
   */
  public
  PluginMgrServer
  (
   File bootstrapDir
  )
  { 
    super("PluginMgrServer");

    pTimer     = new TaskTimer();
    pPluginMgr = new PluginMgr(bootstrapDir);
    pTasks     = new HashSet<HandlerTask>();    
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
      InetSocketAddress saddr = new InetSocketAddress(PackageInfo.sPluginPort);
      server.bind(saddr, 100);
      
      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Fine,
	 "Listening on Port: " + PackageInfo.sPluginPort);
      pTimer.suspend();
      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Info,
	 "Server Ready.\n" + 
	 "  Started in " + TimeStamps.formatInterval(pTimer.getTotalDuration()));
      LogMgr.getInstance().flush();
      pTimer = new TaskTimer();

      while(!pShutdown.get()) {
        try {
          HandlerTask task = new HandlerTask(pSocketChannel.accept()); 
          pTasks.add(task);
          task.start();	
        }
        catch(AsynchronousCloseException ex) {
        }
      }  

      try {
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
	 ("IO problems on port (" + PackageInfo.sPluginPort + "):", ex)); 
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
   * Handle an incoming connection from a <CODE>PluginMgrClient</CODE> instance.
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
      super("PluginMgrServer:HandlerTask");
      pChannel = channel;
    }

    @Override
    protected String
    verifyClient
    (
     String clientID
    )
    {
      if(pPluginMgr.isUpToDate()) {
	if(!clientID.equals("PluginMgr") && !clientID.equals("PluginMgrControl")) {
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
      else {
	if(!clientID.equals("PluginMgrControl")) {
	  String serverRsp = 
	    "Connection from (" + pSocket.getInetAddress() + ") rejected " + 
	    "because plpluginmgr is waiting for the installation of required plugins. " + 
	    "Only connections from plplugin will be accepted at this time.";

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

	pSessionID = -1;

	while(pSocket.isConnected() && isLive() && !pShutdown.get()) {
	  InputStream in     = pSocket.getInputStream();
	  ObjectInput objIn  = new ObjectInputStream(in);
	  Object obj         = objIn.readObject();

	  OutputStream out    = pSocket.getOutputStream();
	  ObjectOutput objOut = new ObjectOutputStream(out);
	  
	  if(isFirst())
	    verifyConnection(obj, objOut);
	  else {
            /* check time difference between client and server */ 
            checkTimeSync((Long) obj, pSocket); 

            /* dispatch request by kind */ 
	    PluginRequest kind = (PluginRequest) objIn.readObject();
	      
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Net, LogMgr.Level.Finer,
	       "Request [" + pSocket.getInetAddress() + "]: " + kind.name());	  
	    LogMgr.getInstance().flush();
	      
            try {
              switch(kind) {
              /*-- ADMINISTRATIVE PRIVILEGES -----------------------------------------------*/
              case UpdateAdminPrivileges: 
                {
                  MiscUpdateAdminPrivilegesReq req = 
                    (MiscUpdateAdminPrivilegesReq) objIn.readObject();
                  objOut.writeObject(pPluginMgr.updateAdminPrivileges(req));
                  objOut.flush(); 
                }
                break;

              /*-- PLUGINS -----------------------------------------------------------------*/
              case Update:
                {
                  PluginUpdateReq req = (PluginUpdateReq) objIn.readObject();
                  objOut.writeObject(pPluginMgr.update(req));
                  objOut.flush(); 
                }
                break;
		
              case Install:
                {
                  PluginInstallReq req = (PluginInstallReq) objIn.readObject();
                  objOut.writeObject(pPluginMgr.install(req));
                  objOut.flush(); 
                }
                break;

	      case Checksum:
		{
		  PluginChecksumReq req = (PluginChecksumReq) objIn.readObject();
		  objOut.writeObject(pPluginMgr.checksum(req));
		  objOut.flush();
		}
		break;

	      case ResourceInstall:
		{
		  PluginResourceInstallReq req = 
		    (PluginResourceInstallReq) objIn.readObject();

		  /* Prior to the plugin resource install, attempt to install the 
		     plugin in dry run mode.  This way the plugin can be validated 
		     before creating a scratch directory and any other processing. */

		  boolean isDryRun = req.getDryRun();
		  
		  /* If the request is in dry run mode, then immediately return 
		     response. */
		  if(isDryRun) {
		    objOut.writeObject(pPluginMgr.install(req));
		    objOut.flush();
		  }
		  else {
		    /* Construct a new PluginInstallReq where the dry run flag 
		       is set to true. */
		    PluginInstallReq dryRunInstallReq = 
		      new PluginInstallReq(req.getClassFile(), 
		                           req.getClassName(), 
					   req.getVersionID(), 
					   req.getContents(), 
					   req.getExternal(), 
					   req.getRename(), 
					   true);

		    Object dryRunInstallRsp = pPluginMgr.install(dryRunInstallReq);

		    /* If the dry run install results in a FailureRsp write the 
		       response to the object output stream. */
		    if(dryRunInstallRsp instanceof FailureRsp) {
		      objOut.writeObject(dryRunInstallRsp);
		      objOut.flush();
		    }
		    else {
		      /* The plugin has passed the plugin validation so we can 
		         begin the resource install phase. */
		      Object rsp = pPluginMgr.installResourcePrep(req);

		      /* The responses from the resource install can be a 
		         PluginResourceInstallRsp, FailureRsp, SuccessRsp.  
			 The PluginResourceInstallRsp is return if resource 
			 chunks needs to send in subsequent requests.  FailureRsp 
			 is returned if an exception was thrown.  SuccessRsp is 
			 returned if no resources needed to updated and the plugin 
			 class files were successfully loaded. */

		      /* If the resource install returned a PluginResourceInstallRsp 
		         then save the session ID. */
		      if(rsp instanceof PluginResourceInstallRsp) {
			PluginResourceInstallRsp installResourceRsp = 
			  (PluginResourceInstallRsp) rsp;

			pSessionID = installResourceRsp.getSessionID();
		      }

		      objOut.writeObject(rsp);
		      objOut.flush();
		    }
		  }
		}
		break;

	      case ResourceChunkInstall:
		{
		  PluginResourceChunkInstallReq req = 
		    (PluginResourceChunkInstallReq) objIn.readObject();

		  Object rsp = pPluginMgr.installResourceChunk(req);

		  if(rsp instanceof PluginResourceInstallRsp) {
		    PluginResourceInstallRsp installRsp = 
		      (PluginResourceInstallRsp) rsp;

		    pSessionID = installRsp.getSessionID();
		  }
		  else if(rsp instanceof SuccessRsp) {
		    pSessionID = -1;
		  }

		  objOut.writeObject(rsp);
		  objOut.flush();
		}
		break;
		
              case Disconnect:
		disconnect();
                break;
		
              case Shutdown:
                LogMgr.getInstance().log
                  (LogMgr.Kind.Net, LogMgr.Level.Warning,
                   "Shutdown Request Received: " + pSocket.getInetAddress());
                LogMgr.getInstance().flush();
                shutdown(); 
                break;	    
		
              default:
                throw new IllegalStateException("Unknown request ID (" + kind + ")!"); 
              }
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
	   "Connection on port (" + PackageInfo.sPluginPort + ") terminated abruptly!");
      }
      catch (IOException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Severe,
           Exceptions.getFullMessage
	   ("IO problems on port (" + PackageInfo.sPluginPort + "):", ex)); 
      }
      catch(ClassNotFoundException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Severe,
           Exceptions.getFullMessage
	   ("Illegal object encountered on port (" + PackageInfo.sPluginPort + "):", ex)); 
      }
      catch (Exception ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Severe,
	   Exceptions.getFullMessage(ex));
      }
      finally {
	if(pSessionID != -1) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Plg, LogMgr.Level.Finest, 
	     "An error has occurred during Resource Install.");

	  try {
	    pPluginMgr.cleanupResourceInstall(pSessionID);
	  }
	  catch(PipelineException ex) {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Plg, LogMgr.Level.Finest, 
	       ex.getMessage());
	  }
	}

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

    /**
     *
     */
    private long  pSessionID;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Times server startup and uptime.
   */ 
  private TaskTimer  pTimer; 

  /**
   * The shared plugin manager. 
   */
  private PluginMgr  pPluginMgr;
  
  /**
   * The set of currently running tasks.
   */ 
  private HashSet<HandlerTask>  pTasks;
}

