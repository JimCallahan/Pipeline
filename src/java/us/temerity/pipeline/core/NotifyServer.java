// $Id: NotifyServer.java,v 1.3 2004/04/12 22:39:05 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/*------------------------------------------------------------------------------------------*/
/*   N O T I F Y   S E R V E R                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * The server-side monitor of directory changes notifications. <P> 
 * 
 * This class handles network communication with 
 * {@link NotifyControlClient NotifyControlClient} and 
 * {@link NotifyControlClient NotifyControlClient} and instances running on remote hosts.  
 * This class listens for new connections from these client instances and creats a thread to 
 * manage each connection. Each of these threads then listens for requests and dispatches 
 * these requests to an underlying instances of {@link DNotify DNotify} which do the actual
 * directory monitoring. <P> 
 * 
 * @see DNotify
 * @see NotifyControlClient
 * @see NotifyMonitorClient
 */
public
class NotifyServer
  extends Thread
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new directory change notification server.
   * 
   * @param controlPort 
   *   The network port to monitor for incoming <CODE>NotifyControlClient</CODE> connections.
   * 
   * @param monitorPort 
   *   The network port to monitor for incoming <CODE>NotifyMonitorClient</CODE> connections.
   */
  public
  NotifyServer
  (
   int controlPort, 
   int monitorPort
  )
  { 
    init(controlPort, monitorPort);
  }
  
  /** 
   * Construct a new directory change notification server using the default network ports.
   * 
   * The network ports used are those specified by the 
   * <B>--notify-control-port</B>=<I>num</I> and 
   * <B>--notify-monitor-port</B>=<I>num</I> options to <B>plconfig</B>(1).
   */
  public
  NotifyServer() 
  { 
    init(PackageInfo.sNotifyControlPort, PackageInfo.sNotifyMonitorPort);
  }


  /*-- CONSTRUCTION HELPERS ----------------------------------------------------------------*/

  /**
   * Initialize a new instance.
   * 
   * @param controlPort 
   *   The network port to monitor for incoming <CODE>NotifyControlClient</CODE> connections.
   * 
   * @param monitorPort 
   *   The network port to monitor for incoming <CODE>NotifyMonitorClient</CODE> connections.
   */
  private synchronized void 
  init
  (
   int controlPort, 
   int monitorPort 
  )
  { 
    if(controlPort < 0) 
      throw new IllegalArgumentException
	("Illegal control port number (" + controlPort + ")!");
    pControlPort = controlPort;

    if(monitorPort < 0) 
      throw new IllegalArgumentException
	("Illegal monitor port number (" + monitorPort + ")!");
    pMonitorPort = monitorPort;

    pShutdown  = new AtomicBoolean(false);   
    pInitLatch = new CountDownLatch(1);
    
    pMonitorTasks = new HashSet<MonitorHandlerTask>();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Begin monitoring a directory.
   * 
   * @param req 
   *   The monitor request.
   *
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to monitor the directory.
   */ 
  private Object
  monitor
  (
   NotifyMonitorReq req
  ) 
  {
    try {
      pInitLatch.await();
    }
    catch(InterruptedException ex) {
      return new FailureRsp(new TaskTimer(), ex.getMessage());
    }

    try {
      pDNotify.monitor(req.getDirectory());
      return new SuccessRsp(new TaskTimer());
    }
    catch(IOException ex) {
      return new FailureRsp(new TaskTimer(), ex.getMessage());
    }
  }
 
  /**
   * Cease monitoring a directory.
   * 
   * @param req 
   *   The unmonitor request.
   *
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to unmonitor the directory.
   */ 
  private Object
  unmonitor
  (
   NotifyUnmonitorReq req
  ) 
  {
    try {
      pInitLatch.await();
    }
    catch(InterruptedException ex) {
      return new FailureRsp(new TaskTimer(), ex.getMessage());
    }

    try {
      pDNotify.unmonitor(req.getDirectory());
      return new SuccessRsp(new TaskTimer());
    }
    catch(IOException ex) {
      return new FailureRsp(new TaskTimer(), ex.getMessage());
    }
  }

 

  /*----------------------------------------------------------------------------------------*/
  /*   T H R E A D   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Spawns the threads which listen to the control and monitor network ports. <P> 
   * 
   * This will only return when the server is shutdown or there is an unrecoverable error.
   */
  public void 
  run() 
  {
    try {
      ControlTask control = new ControlTask();
      control.start();

      MonitorTask monitor = new MonitorTask();
      monitor.start();

      try {
	pDNotify = new DNotify(); 
	pInitLatch.countDown();
	
	while(!pShutdown.get()) {
	  File dir = pDNotify.watch(10);
	  if(dir != null) {
	    synchronized(pMonitorTasks) {
	      for(MonitorHandlerTask task : pMonitorTasks) 
		task.addDir(dir);
	    }
	  }
	}
	
	Logs.net.finer("Server Shutting Down...");
	Logs.flush();
      }
      catch(IOException ex) {
	Logs.net.severe(ex.getMessage());
      }

      Logs.net.finer("Waiting for Control Server to complete...");
      Logs.flush();

      try {
	control.join();
      }
      catch(InterruptedException ex) {
	Logs.net.severe
	  ("Interrupted while waiting for the master control thread to complete!");
      }

      Logs.net.finer("Waiting for Monitor Server to complete...");
      Logs.flush();

      try {
	monitor.join();
      }
      catch(InterruptedException ex) {
	Logs.net.severe
	  ("Interrupted while waiting for the master monitor thread to complete!");
      }
    }
    finally {
      Logs.net.fine("Server Shutdown.");
      Logs.flush();
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Listens to the control network port and spawns threads to process the notify control 
   * requests received over the connection. 
   */ 
  private
  class ControlTask
    extends Thread
  {
    ControlTask() 
    {
      pTasks = new HashSet<ControlHandlerTask>();
    }

    public void 
    run() 
    {
      ServerSocket server = null;
      try {
	server = new ServerSocket(pControlPort, 100);
	Logs.net.fine("Listening for Control Connections on Port: " + pControlPort);
	Logs.flush();
	
	server.setSoTimeout(PackageInfo.sServerTimeOut);

	while(!pShutdown.get()) {
	  try {
	    Socket socket = server.accept();
	    
	    ControlHandlerTask task = new ControlHandlerTask(socket, pTasks);
	    pTasks.add(task);
	    task.start();	
	  }
	  catch(SocketTimeoutException ex) {
	    //Logs.net.finest("Timeout: accept()");
	  }
	}

	try {
	  Logs.net.finer("Shutting Down Control Threads -- Waiting for tasks to complete...");
	  Logs.flush();
	  for(ControlHandlerTask task : pTasks) {
	    task.join();
	  }
	}
	catch(InterruptedException ex) {
	  Logs.net.severe("Interrupted while shutting down control threads!");
	  Logs.flush();
	}
	
	Logs.net.fine("Control Server Shutdown.");    
	Logs.flush();  
      }
      catch (IOException ex) {
	Logs.net.severe("IO problems on port (" + pControlPort + "):\n" + 
			ex.getMessage());
	Logs.flush();
      }
      catch (SecurityException ex) {
	Logs.net.severe("The Security Manager doesn't allow listening to sockets!\n" + 
			ex.getMessage());
	Logs.flush();
      }
      finally {
	if(server != null) {
	  try {
	    server.close();
	  }
	  catch (IOException ex) {
	  }
	}
      }
    }

    private HashSet<ControlHandlerTask>  pTasks;
  }


  /**
   * Handle an incoming control connection from a <CODE>NotifyControlClient</CODE> instance.
   */
  private 
  class ControlHandlerTask
    extends Thread
  {
    public 
    ControlHandlerTask
    (
     Socket socket,
     HashSet<ControlHandlerTask> tasks
    ) 
    {
      pSocket = socket;
      pTasks  = tasks;
    }

    public void 
    run() 
    {
      try {
	Logs.net.fine("Control Connection Opened: " + pSocket.getInetAddress());
	Logs.flush();

	boolean live = true;
	while(pSocket.isConnected() && live && !pShutdown.get()) {
	  InputStream in     = pSocket.getInputStream();
	  ObjectInput objIn  = new ObjectInputStream(in);
	  NotifyRequest kind = (NotifyRequest) objIn.readObject();
	  
	  OutputStream out    = pSocket.getOutputStream();
	  ObjectOutput objOut = new ObjectOutputStream(out);
	  
	  Logs.net.finer("Control Request [" + pSocket.getInetAddress() + "]: " + 
			 kind.name());	  
	  Logs.flush();
  
	  switch(kind) {
	  case Monitor:
	    {
	      NotifyMonitorReq req = (NotifyMonitorReq) objIn.readObject();
	      objOut.writeObject(monitor(req));
	      objOut.flush(); 
	    }
	    break;
	  
	  case Unmonitor:
	    {
	      NotifyUnmonitorReq req = (NotifyUnmonitorReq) objIn.readObject();
	      objOut.writeObject(unmonitor(req));
	      objOut.flush(); 
	    }
	    break;
	  
	  case Watch:
	    Logs.net.severe("A Watch request was sent to the Control Server!");
	    live = false;
	    break;

	  case Disconnect:
	    live = false;
	    break;

	  case Shutdown:
	    Logs.net.info("Shutdown Request Received: " + pSocket.getInetAddress());
	    Logs.flush();
	    pShutdown.set(true);
	    break;	    

	  default:
	    assert(false);
	  }
	}
      }
      catch (IOException ex) {
	Logs.net.severe("IO problems on port (" + pControlPort + "):\n" + 
			ex.getMessage());
      }
      catch(ClassNotFoundException ex) {
	Logs.net.severe("Illegal object encountered on port (" + pControlPort + "):\n" + 
			ex.getMessage());	
      }
      finally {
	try {
	  pSocket.close();
	}
	catch(IOException ex) {
	}

	Logs.net.fine("Control Connection Closed: " + pSocket.getInetAddress());
	Logs.flush();
	
	if(!pShutdown.get()) {
	  synchronized(pTasks) {
	    pTasks.remove(this);
	  }
	}
      }
    }
    
    private Socket                       pSocket;
    private HashSet<ControlHandlerTask>  pTasks;
  }



  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Listens to the monitor network port and spawns threads to process the notify monitor 
   * requests received over the connection. 
   */ 
  private
  class MonitorTask
    extends Thread
  {
    MonitorTask() 
    {}

    public void 
    run() 
    {
      ServerSocket server = null;
      try {
	server = new ServerSocket(pMonitorPort, 100);
	Logs.net.fine("Listening for Monitor Connections on Port: " + pMonitorPort);
	Logs.flush();
	
	server.setSoTimeout(PackageInfo.sServerTimeOut);

	while(!pShutdown.get()) {
	  try {
	    Socket socket = server.accept();
	    
	    MonitorHandlerTask task = new MonitorHandlerTask(socket);
	    synchronized(pMonitorTasks) {
	      pMonitorTasks.add(task);
	    }
	    task.start();	
	  }
	  catch(SocketTimeoutException ex) {
	    //Logs.net.finest("Timeout: accept()");
	  }
	}

	try {
	  Logs.net.finer("Shutting Down Monitor Threads -- Waiting for tasks to complete...");
	  Logs.flush();
	  for(MonitorHandlerTask task : pMonitorTasks) {
	    task.shutdown();
	    task.join();
	  }
	}
	catch(InterruptedException ex) {
	  Logs.net.severe("Interrupted while shutting down monitor threads!");
	  Logs.flush();
	}
	
	Logs.net.fine("Monitor Server Shutdown.");    
	Logs.flush();  
      }
      catch (IOException ex) {
	Logs.net.severe("IO problems on port (" + pMonitorPort + "):\n" + 
			ex.getMessage());
	Logs.flush();
      }
      catch (SecurityException ex) {
	Logs.net.severe("The Security Manager doesn't allow listening to sockets!\n" + 
			ex.getMessage());
	Logs.flush();
      }
      finally {
	if(server != null) {
	  try {
	    server.close();
	  }
	  catch (IOException ex) {
	  }
	}
      }
    }
  }

  /**
   * Handle an incoming monitor connection from a <CODE>NotifyMonitorClient</CODE> instance.
   */
  private 
  class MonitorHandlerTask
    extends Thread
  {
    public 
    MonitorHandlerTask
    (
     Socket socket
    ) 
    {
      pSocket = socket;
      pDirs   = new HashSet<File>();
      pGate   = new Semaphore(1, true);
    }

    public void 
    shutdown() 
    {
      synchronized(pDirs) {
	if(pDirs.isEmpty())
	  pGate.release();
      }
    }

    public void 
    addDir
    (
     File dir
    ) 
    {
      synchronized(pDirs) {
	boolean empty = pDirs.isEmpty();
	pDirs.add(dir);
	if(empty)
	  pGate.release();
      }
    }

    public void 
    run() 
    {
      try {
	Logs.net.fine("Monitor Connection Opened: " + pSocket.getInetAddress());
	Logs.flush();

	boolean live = true;
	while(pSocket.isConnected() && live && !pShutdown.get()) {
	  InputStream in     = pSocket.getInputStream();
	  ObjectInput objIn  = new ObjectInputStream(in);
	  NotifyRequest kind = (NotifyRequest) objIn.readObject();
	  
	  OutputStream out    = pSocket.getOutputStream();
	  ObjectOutput objOut = new ObjectOutputStream(out);
	  
	  Logs.net.finer("Monitor Request [" + pSocket.getInetAddress() + "]: " + 
			 kind.name());	  
	  Logs.flush();
  
	  switch(kind) {
	  case Monitor:
	    Logs.net.severe("A Monitor request was sent to the Monitor Server!");
	    live = false;
	    break;
	  
	  case Unmonitor:
	    Logs.net.severe("A Unmonitor request was sent to the Monitor Server!");
	    live = false;
	    break;
	  
	  case Watch:
	    {
	      NotifyWatchReq req = (NotifyWatchReq) objIn.readObject();

	      try {
		pGate.acquire();
	      }
	      catch(InterruptedException ex) {
	      }

	      HashSet<File> dirs = null;
	      synchronized(pDirs) {
		dirs = new HashSet<File>(pDirs);
		pDirs.clear();
	      }

	      Logs.net.finest("Num Dirs Modified = " + dirs.size());

	      objOut.writeObject(new NotifyWatchRsp(new TaskTimer(), dirs));
	      objOut.flush(); 
	    }
	    break;

	  case Disconnect:
	    live = false;
	    break;

	  case Shutdown:
	    Logs.net.warning("Shutdown Request Received: " + pSocket.getInetAddress());
	    Logs.flush();
	    pShutdown.set(true);
	    break;	    

	  default:
	    assert(false);
	  }
	}
      }
      catch (IOException ex) {
	Logs.net.severe("IO problems on port (" + pMonitorPort + "):\n" + 
			ex.getMessage());
      }
      catch(ClassNotFoundException ex) {
	Logs.net.severe("Illegal object encountered on port (" + pMonitorPort + "):\n" + 
			ex.getMessage());	
      }
      finally {
	try {
	  pSocket.close();
	}
	catch(IOException ex) {
	}

	Logs.net.fine("Monitor Connection Closed: " + pSocket.getInetAddress());
	Logs.flush();
	
	if(!pShutdown.get()) {
	  synchronized(pMonitorTasks) {
	    pMonitorTasks.remove(this);
	  }
	}
      }
    }


    private Socket         pSocket;
    private HashSet<File>  pDirs;
    private Semaphore      pGate;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The network port number the server listens to for incoming control connections.
   */
  private int  pControlPort;

  /**
   * The network port number the server listens to for incoming monitor connections.
   */
  private int  pMonitorPort;

  /**
   * Has the server been ordered to shutdown?
   */
  private AtomicBoolean  pShutdown;


  /**
   * Protects access to pDNotify before it has been initialized;
   */
  private CountDownLatch  pInitLatch;

  /**
   * The directory change notification monitor.
   */
  private DNotify  pDNotify;

  
  /**
   * The set of threads handling active monitor connections.
   */ 
  private HashSet<MonitorHandlerTask>  pMonitorTasks;

}

