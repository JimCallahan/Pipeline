// $Id: NodeMgrClient.java,v 1.1 2004/03/26 04:38:06 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.core.*;
import us.temerity.pipeline.message.*;

import java.io.*;
import java.net.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   M G R   C L I E N T                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The client-side manager of node queries and operations. <P> 
 * 
 * This class handles network communication with the Pipeline master server daemon 
 * <A HREF="../../../../man/plmaster.html"><B>plmaster</B><A>(1).  This class represents the
 * interface used by all Pipeline client programs and end user tools to interact with the 
 * Pipeline system.
 */
public
class NodeMgrClient
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new node manager client.
   * 
   * @param hostname 
   *   The name of the host running <B>plmaster</B>(1).
   * 
   * @param port 
   *   The network port listened to by <B>plmaster</B>(1).
   */
  public
  NodeMgrClient
  ( 
   String hostname, 
   int port
  ) 
  {
    init(hostname, port);
  }

  /** 
   * Construct a new node manager client.
   * 
   * The hostname and port are set by <B>plconfig</B>(1).
   */
  public
  NodeMgrClient() 
  {
    init(PackageInfo.sMasterServer, PackageInfo.sMasterPort);
  }


  /*-- CONSTRUCTION HELPERS ----------------------------------------------------------------*/

  private synchronized void 
  init
  ( 
   String hostname, 
   int port
  ) 
  {
    if(hostname == null) 
      throw new IllegalArgumentException("The hostname argument cannot be (null)!");
    pHostname = hostname;

    if(port < 0) 
      throw new IllegalArgumentException("Illegal port number (" + port + ")!");
    pPort = port;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   R E V I S I O N   C O N T R O L                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Register an initial working version of a node. <P> 
   * 
   * The <CODE>mod</CODE> argument must have a node name which does not already exist and
   * does not match any of the path components of any existing node.  <P> 
   * 
   * The working version must be an inital version.  In other words, the 
   * {@link NodeMod#getWorkingID() NodeMod.getWorkingID} method must return 
   * <CODE>null</CODE>.  As an intial working version, the <CODE>mod</CODE> argument should
   * not contain any upstream dependency relationship information.
   *  
   * @param view 
   *   The name of the user's working area view. 
   *
   * @param mod
   *   The initial working version to register.
   * 
   * @throws PipelineException
   *   If unable to register the given node.
   */
  public void 
  register
  ( 
   String view, 
   NodeMod mod
  ) 
    throws PipelineException
  {
    verifyConnection();

    NodeID id = new NodeID(PackageInfo.sUser, view, mod.getName());
    NodeRegisterReq req = new NodeRegisterReq(id, mod);

    Object obj = performTransaction(NodeRequest.Register, req);

    if(obj instanceof SuccessRsp) {
    }
    else if(obj instanceof FailureRsp) {
      FailureRsp rsp = (FailureRsp) obj;
      throw new PipelineException(rsp.getMessage());	
    }
    else {
      shutdown();
      throw new PipelineException
	("Illegal response received from the NodeMgrServer instance!");
    }
  }



  // ...





  /*----------------------------------------------------------------------------------------*/
  /*   M I S C   O P S                                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Close the network connection if its is still connected.
   */
  public synchronized void 
  shutdown() 
  {
    if(pSocket == null)
      return;

    try {
      if(pSocket.isConnected()) {
	OutputStream out = pSocket.getOutputStream();
	ObjectOutput objOut = new ObjectOutputStream(out);
	objOut.writeObject(NodeRequest.Shutdown);
	objOut.flush(); 

	pSocket.close();
      }
    }
    catch (IOException ex) {
    }
    finally {
      pSocket = null;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Make sure the network connection to <B>plmaster</B>(1) has 
   * been established.  If the connection is down, try to reconnect.
   * 
   * @throws PipelineException
   *   If the connection is down and cannot be reestablished. 
   */
  private synchronized void 
  verifyConnection() 
    throws PipelineException 
  {
    if((pSocket != null) && pSocket.isConnected())
      return;

    try {
      pSocket = new Socket(pHostname, pPort);
    }
    catch (IOException ex) {
      throw new PipelineException
	("IO problems on port (" + pPort + "):\n" + 
	 ex.getMessage());
    }
    catch (SecurityException ex) {
      throw new PipelineException
	("The Security Manager doesn't allow socket connections!\n" + 
	 ex.getMessage());
    }
  }

  /**
   * Send the given file request to <B>plmaster</B>(1) and 
   * wait for the response.
   * 
   * @param kind 
   *   The kind of request being sent.
   * 
   * @param req 
   *   The request data.
   * 
   * @return
   *   The response from <B>plmaster</B>(1).
   * 
   * @throws PipelineException
   *   If unable to complete the transaction.
   */
  private synchronized Object
  performTransaction
  (
   NodeRequest kind, 
   Object req
  ) 
    throws PipelineException 
  {
    try {
      OutputStream out = pSocket.getOutputStream();
      ObjectOutput objOut = new ObjectOutputStream(out);
      objOut.writeObject(kind);
      objOut.writeObject(req);
      objOut.flush(); 

      InputStream in  = pSocket.getInputStream();
      ObjectInput objIn  = new ObjectInputStream(in);
      return (objIn.readObject());
    }
    catch(IOException ex) {
      shutdown();
      throw new PipelineException
	("IO problems on port (" + pPort + "):\n" + 
	 ex.getMessage());
    }
    catch(ClassNotFoundException ex) {
      shutdown();
      throw new PipelineException
	("Illegal object encountered on port (" + pPort + "):\n" + 
	 ex.getMessage());  
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The name of the host running <B>plmaster</B>(1).
   */
  private String  pHostname;

  /**
   * The network port listened to by <B>plmaster</B>(1).
   */
  private int  pPort;

  /**
   * The network socket connection.
   */
  private Socket  pSocket;

}

