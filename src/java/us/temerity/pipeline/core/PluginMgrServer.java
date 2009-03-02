// $Id: PluginMgrServer.java,v 1.17 2009/03/02 00:18:48 jlee Exp $

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
    ServerSocketChannel schannel = null;
    try {
      schannel = ServerSocketChannel.open();
      ServerSocket server = schannel.socket();
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
      if(schannel != null) {
	try {
	  schannel.close();
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
		
              case Disconnect:
		disconnect();
                break;
		
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

