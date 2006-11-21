// $Id: BaseMgrClient.java,v 1.24 2006/11/21 19:57:11 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.message.*;

import java.io.*;
import java.net.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   M G R   C L I E N T                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The common base class of all manager clients.
 */
public
class BaseMgrClient
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new manager client.
   * 
   * @param hostname 
   *   The name of the host running the server.
   * 
   * @param port 
   *   The network port listened to by server.
   * 
   * @param disconnect
   *   The disconnect request enum.
   * 
   * @param shutdown
   *   The shutdown request enum.
   */
  public
  BaseMgrClient
  ( 
   String hostname, 
   int port, 
   Object disconnect, 
   Object shutdown
  ) 
  {
    if(hostname == null) 
      throw new IllegalArgumentException("The hostname argument cannot be (null)!");
    pHostname = hostname;

    if(port < 0) 
      throw new IllegalArgumentException("Illegal port number (" + port + ")!");
    pPort = port;

    if(disconnect == null) 
      throw new IllegalArgumentException("The disconnect request cannot be (null)!");
    pDisconnect = disconnect;

    pShutdown = shutdown;
  }



  /*----------------------------------------------------------------------------------------*/
  /*  C O N N E C T I O N                                                                   */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Make sure the network connection to the server instance has been established.  If the 
   * connection is down, try to reconnect.
   * 
   * @throws PipelineException
   *   If the connection is down and cannot be reestablished. 
   */
  public synchronized void 
  verifyConnection() 
    throws PipelineException 
  {
    /* police access to client methods from server extensions */ 
    {
      boolean illegal = false;
      Thread current = Thread.currentThread();
      if(current instanceof BaseExtThread) 
	illegal = true;
      else {
	StackTraceElement[] elem = current.getStackTrace();
	int wk; 
	for(wk=0; wk<elem.length; wk++) {
	  if(elem[wk].getMethodName().equals("performExtensionTests")) {
	    illegal = true;
	    break;
	  }
	}
      }

      if(illegal) 
	throw new PipelineException
	  ("Access to Pipeline server client methods is not allowed inside server " + 
	   "extension pre-operation tests or post-operation tasks!");
    }

    /* (re)estblish the connection */ 
    if((pSocket != null) && pSocket.isConnected())
      return;

    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Fine)) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Fine,
	 "Establishing Connection: " + pHostname + ":" + pPort); 
      LogMgr.getInstance().flush();
    }

    try {
      pSocket = new Socket();

      InetSocketAddress addr = new InetSocketAddress(pHostname, pPort);
      pSocket.connect(addr, 10000);
      
      {
	String cinfo = 
	  ("Pipeline-" + PackageInfo.sVersion + " [" + PackageInfo.sRelease + "]");

	pSocket.setSoTimeout(10000);

	OutputStream out = pSocket.getOutputStream();
	ObjectOutput objOut = new ObjectOutputStream(out);
	objOut.writeObject(cinfo);
	objOut.flush(); 

	InputStream in = pSocket.getInputStream();
	ObjectInput objIn = getObjectInput(in); 
	Object rsp = objIn.readObject();
	
	pSocket.setSoTimeout(0);
	
	String sinfo = "Unknown"; 
	if(rsp instanceof String) 
	  sinfo = (String) rsp;
	
	if(!sinfo.equals(cinfo)) {
	  disconnect();
	  throw new PipelineException 
	    (getServerDownMessage() + "\n  " + 
	     "Connection rejected due to a mismatch in Pipeline " + 
	     "release versions!\n" + 
	     "  Client = " + cinfo + "\n" +
	     "  Server = " + sinfo);
	}

	if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Fine)) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Net, LogMgr.Level.Fine,
	     "Connection Opened: " + pSocket.getInetAddress() + ":" + pPort); 
	  LogMgr.getInstance().flush();
	}
      }
    }
    catch(IOException ex) {
      disconnect();
      throw new PipelineException
	(getServerDownMessage() + "\n  " + 
	 ex.getMessage(), ex);
    }
    catch(ClassNotFoundException ex) {
      disconnect();
      throw new PipelineException
	("Illegal object encountered on port (" + pPort + "):\n" + 
	 ex.getMessage());  
    }
    catch(SecurityException ex) {
      throw new PipelineException
	("The Security Manager doesn't allow socket connections!\n" + 
	 ex.getMessage());
    }
  }

  /**
   * Attempt to establish the the network connection to the server instance.
   * 
   * @param tries
   *   The number of attempts to make before giving up.
   * 
   * @param delay
   *   The number of milliseconds to wait between tries.
   * 
   * @throws PipelineException 
   *   In unable to establish the connection.
   */ 
  public synchronized void 
  waitForConnection
  (
   long tries, 
   long delay
  ) 
    throws PipelineException 
  {
    long wk; 
    for(wk=0L; wk<tries; wk++) {
      try {
	verifyConnection();
	return;
      }
      catch(PipelineException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Warning, 
	   ex.getMessage());
	LogMgr.getInstance().flush();
      }

      try {
	Thread.sleep(5000);
      }
      catch(InterruptedException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Plg, LogMgr.Level.Warning, 
	   "Interrupted while attempting establish network connection!");
      }
    }

    throw new PipelineException 
      ("Giving up after (" + tries + ") attempts!");
  }

  /**
   * Close the network connection if its is still connected.
   */
  public synchronized void 
  disconnect() 
  {
    if(pSocket == null)
      return;

    try {
      if(pSocket.isConnected()) {
	OutputStream out = pSocket.getOutputStream();
	ObjectOutput objOut = new ObjectOutputStream(out);
	objOut.writeObject(pDisconnect);
	objOut.flush(); 

	pSocket.close();

	if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Fine)) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Net, LogMgr.Level.Fine,
	     "Connection Closed: " + pHostname + ":" + pPort); 
	  LogMgr.getInstance().flush();
	}
      }
    }
    catch (IOException ex) {
    }
    finally {
      pSocket = null;
    }
  }

  /**
   * Order the server to refuse any further requests and then to exit as soon as all
   * currently pending requests have be completed.
   */
  public synchronized void 
  shutdown() 
    throws PipelineException 
  {
    verifyConnection();

    try {
      OutputStream out = pSocket.getOutputStream();
      ObjectOutput objOut = new ObjectOutputStream(out);
      objOut.writeObject(pShutdown);
      objOut.flush(); 

      pSocket.close();
    }
    catch(IOException ex) {
      disconnect();
      throw new PipelineException
	("IO problems on port (" + pPort + "):\n" + 
	 ex.getMessage());
    }
    finally {
      pSocket = null;
    }
  }

  /**
   * Send the given request to the server instance, then close the connection
   * without waiting for a response.
   * 
   * @param kind 
   *   The kind of request being sent.
   * 
   * @param req 
   *   The request data or <CODE>null</CODE> if there is no request.
   */ 
  protected synchronized void 
  shutdownTransaction
  (
   Object kind, 
   Object req
  ) 
    throws PipelineException 
  {
    verifyConnection();
    try {
      OutputStream out = pSocket.getOutputStream();
      ObjectOutput objOut = new ObjectOutputStream(out);
      objOut.writeObject(kind);
      if(req != null) 
	objOut.writeObject(req);
      objOut.flush(); 

      try {
	Thread.sleep(1000);
      }
      catch(InterruptedException ex) {
      }

      pSocket.close();
    }
    catch(IOException ex) {
      disconnect();
      throw new PipelineException
	("IO problems on port (" + pPort + "):\n" + 
	 ex.getMessage(), ex);
    }
    finally {
      pSocket = null;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the object input given a socket input stream.
   */ 
  protected ObjectInput
  getObjectInput
  (
   InputStream in
  ) 
    throws IOException
  {
    return new ObjectInputStream(in);
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Send the given request to the server instance and wait for the response. <P> 
   * 
   * The socket timeout is set to 5-minutes. 
   * 
   * @param kind 
   *   The kind of request being sent.
   * 
   * @param req 
   *   The request data or <CODE>null</CODE> if there is no request.
   * 
   * @return
   *   The response from the server instance.
   * 
   * @throws PipelineException
   *   If unable to complete the transaction.
   */
  protected synchronized Object
  performTransaction
  (
   Object kind, 
   Object req
  ) 
    throws PipelineException 
  {
    return performTransaction(kind, req, 300000);
  }

  /**
   * Send the given request to the server instance and wait for the response.
   * 
   * @param kind 
   *   The kind of request being sent.
   * 
   * @param req 
   *   The request data or <CODE>null</CODE> if there is no request.
   * 
   * @param timeout
   *   The maximum amount of time this operation will block (in milliseconds) before 
   *   failing.
   * 
   * @return
   *   The response from the server instance.
   * 
   * @throws PipelineException
   *   If unable to complete the transaction.
   */
  protected synchronized Object
  performTransaction
  (
   Object kind, 
   Object req, 
   int timeout
  ) 
    throws PipelineException 
  {
    try {
      pSocket.setSoTimeout(timeout);

      TaskTimer timer = null;
      if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finer)) {
	timer = new TaskTimer("Recv [" + pSocket.getInetAddress() + ":" + pPort + "] " + 
			      kind.toString());
	timer.aquire();
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Finer,
	   "Send [" + pSocket.getInetAddress() + ":" + pPort + "]: " + kind.toString());
      }

      OutputStream out = pSocket.getOutputStream();
      ObjectOutput objOut = new ObjectOutputStream(out);
      objOut.writeObject(kind);
      if(req != null) 
	objOut.writeObject(req);
      objOut.flush(); 

      InputStream in  = pSocket.getInputStream();
      ObjectInput objIn  = getObjectInput(in); 
      Object rsp = objIn.readObject();

      if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finer)) {
	timer.resume();
	LogMgr.getInstance().logStage
	  (LogMgr.Kind.Net, LogMgr.Level.Finer,
	   timer); 
      }
      
      pSocket.setSoTimeout(0);

      return rsp; 
    }
    catch(IOException ex) {
      disconnect();
      throw new PipelineException
	("IO problems on port (" + pPort + "):\n" + 
	 ex.getMessage(), ex);
    }
    catch(ClassNotFoundException ex) {
      disconnect();
      throw new PipelineException
	("Illegal object encountered on port (" + pPort + "):\n" + 
	 ex.getMessage());  
    }
  }

  /**
   * Send the given request to the server instance and wait for the response which may
   * take a long time.  <P> 
   * 
   * @param kind 
   *   The kind of request being sent.
   * 
   * @param req 
   *   The request data or <CODE>null</CODE> if there is no request.
   * 
   * @param reqTimeout
   *   The maximum amount of time this operation will block (in milliseconds) while
   *   attempting to send the request before failing.
   * 
   * @param rspTimeout
   *   The maximum amount of time this operation will block (in milliseconds) while
   *   attempting to recieve the response before retrying.
   * 
   * @return
   *   The response from the server instance.
   * 
   * @throws PipelineException
   *   If unable to complete the transaction.
   */
  protected synchronized Object
  performLongTransaction
  (
   Object kind, 
   Object req, 
   int reqTimeout, 
   int rspTimeout
  ) 
    throws PipelineException 
  {
    pLongTransactionStart = new Date();

    try {
      pSocket.setSoTimeout(reqTimeout);

      TaskTimer timer = null;
      if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finer)) {
	timer = new TaskTimer("Recv [" + pSocket.getInetAddress() + ":" + pPort + "] " + 
			      kind.toString());
	timer.aquire();
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Finer,
	   "Send [" + pSocket.getInetAddress() + ":" + pPort + "]: " + kind.toString()); 
      }

      OutputStream out = pSocket.getOutputStream();
      ObjectOutput objOut = new ObjectOutputStream(out);
      objOut.writeObject(kind);
      if(req != null) 
	objOut.writeObject(req);
      objOut.flush(); 

      pSocket.setSoTimeout(rspTimeout);

      InputStream in  = pSocket.getInputStream();
      ObjectInput objIn = getObjectInput(in); 
      
      Object rsp = null;
      while(rsp == null) {
	try {
	  rsp = objIn.readObject();
	}
	catch(SocketTimeoutException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Net, LogMgr.Level.Finest,
	     "Socket Timeout [" + pSocket.getInetAddress() + "]: " + kind.toString() + "\n" +
	     " " + timer); 

	  if(isServerUnreachable() || (ex.bytesTransferred > 0)) 
	    throw ex;
	}
      }

      if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finer)) {
	timer.resume();
	LogMgr.getInstance().logStage
	  (LogMgr.Kind.Net, LogMgr.Level.Finer,
	   timer); 
      }

      pSocket.setSoTimeout(0);

      return rsp; 
    }
    catch(IOException ex) {
      disconnect();
      throw new PipelineException
	("IO problems on port (" + pPort + "):\n" + 
	 ex.getMessage(), ex);
    }
    catch(ClassNotFoundException ex) {
      disconnect();
      throw new PipelineException
	("Illegal object encountered on port (" + pPort + "):\n" + 
	 ex.getMessage());  
    }
  }

  /**
   * Whether the server is no longer reachable communication should no longer be attempted.<P>
   * 
   * Subclasses which wish to force performLongTransaction() to give up before receiving
   * a response should override this method to return <CODE>true</CODE>.
   */ 
  protected boolean 
  isServerUnreachable() 
  {
    return false;
  }

  /**
   * Handle the simple Success/Failure response.
   * 
   * @param obj
   *   The response from the server.
   */ 
  protected void 
  handleSimpleResponse
  ( 
   Object obj
  )
    throws PipelineException
  {
    if(!(obj instanceof SuccessRsp))
      handleFailure(obj);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Handle non-successful responses.
   * 
   * @param obj
   *   The response from the server.
   */ 
  protected void 
  handleFailure
  ( 
   Object obj
  )
    throws PipelineException
  {
    if(obj instanceof FailureRsp) {
      FailureRsp rsp = (FailureRsp) obj;
      throw new PipelineException(rsp.getMessage());	
    }
    else {
      disconnect();
      throw new PipelineException
	("Illegal response received from the server instance!");
    }
  }

  /**
   * Get the error message to be shown when the server cannot be contacted.
   */ 
  protected String
  getServerDownMessage()
  {
    return ("Unable to contact the server!");
  }
  



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The name of the host running the server.
   */
  protected String  pHostname;

  /**
   * The network port listened to by the server.
   */
  protected int  pPort;

  /**
   * The timestamp of when a long transaction was started.
   */ 
  protected Date  pLongTransactionStart;

  /**
   * The network socket connection.
   */
  private Socket  pSocket;

  /** 
   * The disconnect request.
   */ 
  private Object  pDisconnect;

  /** 
   * The shutdown request.
   */ 
  private Object  pShutdown;

}

