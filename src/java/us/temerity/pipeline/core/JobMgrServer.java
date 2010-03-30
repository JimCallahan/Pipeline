// $Id: JobMgrServer.java,v 1.45 2010/01/14 04:18:32 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;
import us.temerity.pipeline.message.job.*;
import us.temerity.pipeline.message.simple.*;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   M G R   S E R V E R                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The server-side manager of job queries and operations. <P> 
 * 
 * This class handles network communication with {@link JobMgrClient JobMgrClient},
 * {@link JobMgrControlClient} and {@link JobMgrPlgControlClient} instances running on 
 * remote hosts.  This class listens for new connections from clients and creats a thread to 
 * manage each connection.  Each of these threads then listens for requests for job related 
 * operations and dispatches these requests to an underlying instance of the {@link JobMgr}
 * class.
 */
class JobMgrServer
  extends BaseMgrServer
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new job manager server.
   */
  public
  JobMgrServer()
  { 
    super("JobMgrServer");

    pTimer = new TaskTimer();

    pJobMgr = new JobMgr();
    pTasks  = new TreeSet<HandlerTask>();
  }
  
 

  /*----------------------------------------------------------------------------------------*/
  /*   T H R E A D   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Begin listening to the network port and spawn threads to process the job management 
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
      InetSocketAddress saddr = new InetSocketAddress(PackageInfo.sJobPort);
      server.bind(saddr, 100);

      LogMgr.getInstance().logAndFlush
	(LogMgr.Kind.Net, LogMgr.Level.Fine,
	 "Listening on Port: " + PackageInfo.sJobPort);
      pTimer.suspend();
      LogMgr.getInstance().logAndFlush
	(LogMgr.Kind.Net, LogMgr.Level.Info,
	 "Server Ready.\n" + 
	 "  Started in " + TimeStamps.formatInterval(pTimer.getTotalDuration()));
      pTimer = new TaskTimer();

      CollectorTask collector = null;
      if(PackageInfo.sOsType == OsType.Unix) {
	collector = new CollectorTask();
	collector.start();
      }

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

      pJobMgr.killAll();

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

	if(collector != null) {
	  LogMgr.getInstance().logAndFlush
	    (LogMgr.Kind.Ops, LogMgr.Level.Info,
	     "Waiting on Collector...");

	  collector.join();
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
	 ("IO problems on port (" + PackageInfo.sJobPort + "):", ex)); 
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
   * Handle an incoming connection from a <CODE>JobMgrClient</CODE> instance.
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
      super("JobMgrServer:HandlerTask");
      pChannel = channel;
    }

    @Override
    protected String
    verifyClient
    (
     String clientID
    )
    {
      if(!clientID.equals("JobMgr") && !clientID.equals("JobMgrControl")) {
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
	    JobRequest kind = (JobRequest) objIn.readObject();
	  
	    LogMgr.getInstance().logAndFlush
	      (LogMgr.Kind.Net, LogMgr.Level.Finer,
	       "Request [" + pSocket.getInetAddress() + "]: " + kind.name());	  

            try {
              switch(kind) {
              /*-- EDITING -----------------------------------------------------------------*/
              case EditAs:           
                {
                  JobEditAsReq req = (JobEditAsReq) objIn.readObject();
                  objOut.writeObject(pJobMgr.editAs(req));
                  objOut.flush(); 
                }
                break;


              /*-- HOST RESOURCES ----------------------------------------------------------*/
              case GetResources:
                {
                  objOut.writeObject(pJobMgr.getResources());
                  objOut.flush(); 
                }
                break;

              case GetOsType:
                {
                  objOut.writeObject(pJobMgr.getOsType());
                  objOut.flush(); 
                }
                break;

              case GetNumProcessors:
                {
                  objOut.writeObject(pJobMgr.getNumProcessors());
                  objOut.flush(); 
                }
                break;

              case GetTotalMemory:
                {
                  objOut.writeObject(pJobMgr.getTotalMemory());
                  objOut.flush(); 
                }
                break;

              case GetTotalDisk:
                {
                  objOut.writeObject(pJobMgr.getTotalDisk());
                  objOut.flush(); 
                }
                break;

	    
              /*-- JOB EXECUTION -----------------------------------------------------------*/
              case Start:
                {
                  JobStartReq req = (JobStartReq) objIn.readObject();
                  objOut.writeObject(pJobMgr.jobStart(req));
                  objOut.flush(); 
                }
                break;

              case Kill:
                {
                  JobKillReq req = (JobKillReq) objIn.readObject();
                  objOut.writeObject(pJobMgr.jobKill(req));
                  objOut.flush(); 
                }
                break;

              case Wait:
                {
                  JobWaitReq req = (JobWaitReq) objIn.readObject();
                  objOut.writeObject(pJobMgr.jobWait(req));
                  objOut.flush(); 
                }
                break;


              /*-- JOB MANAGEMENT ----------------------------------------------------------*/
              case CleanupResources:
                {
                  JobCleanupResourcesReq req = (JobCleanupResourcesReq) objIn.readObject();
                  objOut.writeObject(pJobMgr.cleanupResources(req));
                  objOut.flush(); 
                }
                break;

              case CleanupPreemptedResources:
                {
                  JobCleanupPreemptedResourcesReq req = 
                    (JobCleanupPreemptedResourcesReq) objIn.readObject();
                  objOut.writeObject(pJobMgr.cleanupPreemptedResources(req));
                  objOut.flush(); 
                }
                break;

	    
              /*-- EXEC DETAILS ------------------------------------------------------------*/
              case GetExecDetails:
                {
                  JobGetExecDetailsReq req = (JobGetExecDetailsReq) objIn.readObject();
                  objOut.writeObject(pJobMgr.getExecDetails(req));
                  objOut.flush(); 
                }
                break;


              /*-- JOB OUTPUT --------------------------------------------------------------*/
              case GetNumStdOutLines:
                {
                  JobGetNumStdOutLinesReq req = (JobGetNumStdOutLinesReq) objIn.readObject();
                  objOut.writeObject(pJobMgr.getNumStdOutLines(req));
                  objOut.flush(); 
                }
                break;

              case GetStdOutLines:
                {
                  JobGetStdOutLinesReq req = (JobGetStdOutLinesReq) objIn.readObject();
                  objOut.writeObject(pJobMgr.getStdOutLines(req));
                  objOut.flush(); 
                }
                break;

              case CloseStdOut:
                {
                  JobCloseStdOutReq req = (JobCloseStdOutReq) objIn.readObject();
                  objOut.writeObject(pJobMgr.closeStdOut(req));
                  objOut.flush(); 
                }
                break;


              case GetNumStdErrLines:
                {
                  JobGetNumStdErrLinesReq req = (JobGetNumStdErrLinesReq) objIn.readObject();
                  objOut.writeObject(pJobMgr.getNumStdErrLines(req));
                  objOut.flush(); 
                }
                break;

              case GetStdErrLines:
                {
                  JobGetStdErrLinesReq req = (JobGetStdErrLinesReq) objIn.readObject();
                  objOut.writeObject(pJobMgr.getStdErrLines(req));
                  objOut.flush(); 
                }
                break;

              case CloseStdErr:
                {
                  JobCloseStdErrReq req = (JobCloseStdErrReq) objIn.readObject();
                  objOut.writeObject(pJobMgr.closeStdErr(req));
                  objOut.flush(); 
                }
                break;

	    
              /*-- NETWORK CONNECTION ------------------------------------------------------*/
              case Ping:
                {
                  objOut.writeObject(new SuccessRsp(new TaskTimer("Ping"))); 
                  objOut.flush(); 
                }
                break;

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
	   "Connection on port (" + PackageInfo.sJobPort + ") terminated abruptly!");	
      }
      catch (IOException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Severe,
           Exceptions.getFullMessage
	   ("IO problems on port (" + PackageInfo.sJobPort + "):", ex)); 
      }
      catch(ClassNotFoundException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Severe,
           Exceptions.getFullMessage
	   ("Illegal object encountered on port (" + PackageInfo.sJobPort + "):", ex)); 
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

      LogMgr.getInstance().flush();
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
   * Collects pre-proccess resource usage statistics.
   */
  private 
  class CollectorTask
    extends Thread
  {
    public 
    CollectorTask() 
    {
      super("JobMgrServer:CollectorTask");
    }

    public void 
    run() 
    {
      if(PackageInfo.sOsType != OsType.Unix)
	throw new IllegalStateException("The OS type must be Unix!");

      try {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Ops, LogMgr.Level.Fine,
	   "Collector Started.");	
	
	while(!pShutdown.get()) {
	  pJobMgr.collector();
	}
      }
      catch (Exception ex) {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Ops, LogMgr.Level.Severe,
	   Exceptions.getFullMessage("Collector Failed:", ex)); 
      }
      finally {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Ops, LogMgr.Level.Fine,
	   "Collector Finished.");	
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
   * The shared job manager. 
   */
  private JobMgr  pJobMgr;

  /**
   * The set of currently running tasks.
   */ 
  private TreeSet<HandlerTask>  pTasks;
}

