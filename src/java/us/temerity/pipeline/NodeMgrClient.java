// $Id: NodeMgrClient.java,v 1.4 2004/03/29 08:14:42 jim Exp $

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
   * Construct a new node manager client. <P> 
   * 
   * The hostname and port used are those specified by the 
   * <CODE><B>--master-host</B>=<I>host</I></CODE> and 
   * <CODE><B>--master-port</B>=<I>num</I></CODE> options to <B>plconfig</B>(1).
   */
  public
  NodeMgrClient() 
  {
    init(PackageInfo.sMasterServer, PackageInfo.sMasterPort);
  }


  /*-- CONSTRUCTION HELPERS ----------------------------------------------------------------*/

  private void 
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
  /*   W O R K I N G   V E R S I O N S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the working version of the node. <P> 
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param name 
   *   The fully resolved node name.
   *
   * @throws PipelineException
   *   If unable to retrieve the working version.
   */
  public synchronized NodeMod
  getWorkingVersion
  ( 
   String view, 
   String name
  ) 
    throws PipelineException
  {
    verifyConnection();
	 
    NodeID id = new NodeID(PackageInfo.sUser, view, name);
    NodeGetWorkingReq req = new NodeGetWorkingReq(id);

    Object obj = performTransaction(NodeRequest.GetWorking, req);

    if(obj instanceof NodeGetWorkingRsp) {
      NodeGetWorkingRsp rsp = (NodeGetWorkingRsp) obj;
      return rsp.getNodeMod();      
    }
    else if(obj instanceof FailureRsp) {
      FailureRsp rsp = (FailureRsp) obj;
      throw new PipelineException(rsp.getMessage());	
    }
    else {
      disconnect();
      throw new PipelineException
	("Illegal response received from the NodeMgrServer instance!");
    }
  }  

  /** 
   * Set the node properties of the working version of the node. <P> 
   * 
   * Node properties include: <BR>
   * 
   * <DIV style="margin-left: 40px;">
   *   The file patterns and frame ranges of primary and secondary file sequences. <BR>
   *   The toolset environment under which editors and actions are run. <BR>
   *   The name of the editor plugin used to edit the data files associated with the node.<BR>
   *   The regeneration action and its single and per-dependency parameters. <BR>
   *   The job requirements. <BR>
   *   The IgnoreOverflow and IsSerial flags. <BR>
   *   The job batch size. <P> 
   * </DIV> 
   * 
   * Note that any existing upstream node link information contained in the
   * <CODE>mod</CODE> argument will be ignored.  The {@link #link link} and
   * {@link #unlink unlink} methods must be used to alter the connections 
   * between working node versions.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param mod 
   *   The working version containing the node property information to copy.
   * 
   * @throws PipelineException
   *   If unable to set the node properties.
   */
  public synchronized void 
  modifyProperties
  ( 
   String view, 
   NodeMod mod   
  ) 
    throws PipelineException
  {
    verifyConnection();

    NodeID id = new NodeID(PackageInfo.sUser, view, mod.getName());
    NodeModifyPropertiesReq req = new NodeModifyPropertiesReq(id, mod);

    Object obj = performTransaction(NodeRequest.ModifyProperties, req);

    if(obj instanceof SuccessRsp) {
    }
    else if(obj instanceof FailureRsp) {
      FailureRsp rsp = (FailureRsp) obj;
      throw new PipelineException(rsp.getMessage());	
    }
    else {
      disconnect();
      throw new PipelineException
	("Illegal response received from the NodeMgrServer instance!");
    }
  }

  /**
   * Create or modify an existing link between the working versions. <P> 
   * 
   * If the <CODE>relationship</CODE> argument is <CODE>OneToOne</CODE> then the 
   * <CODE>offset</CODE> argument must not be <CODE>null</CODE>.  For all other 
   * link relationships, the <CODE>offset</CODE> argument must be <CODE>null</CODE>.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param target 
   *   The fully resolved name of the downstream node to connect.
   * 
   * @param source 
   *   The fully resolved name of the upstream node to connect.
   * 
   * @param catagory 
   *   The named classification of the link's node state propogation policy.
   * 
   * @param relationship 
   *   The nature of the relationship between files associated with the source and 
   *   target nodes. 
   * 
   * @param offset 
   *   The frame index offset.
   * 
   * @throws PipelineException
   *   If unable to create or modify the link.
   */
  public synchronized void 
  link
  (
   String view, 
   String target, 
   String source,
   LinkCatagory catagory,   
   LinkRelationship relationship,  
   Integer offset
  ) 
    throws PipelineException
  {
    verifyConnection();

    NodeID id = new NodeID(PackageInfo.sUser, view, target);
    LinkMod link = new LinkMod(source, catagory, relationship, offset);
    NodeLinkReq req = new NodeLinkReq(id, link);

    Object obj = performTransaction(NodeRequest.Link, req);

    if(obj instanceof SuccessRsp) {
    }
    else if(obj instanceof FailureRsp) {
      FailureRsp rsp = (FailureRsp) obj;
      throw new PipelineException(rsp.getMessage());	
    }
    else {
      disconnect();
      throw new PipelineException
	("Illegal response received from the NodeMgrServer instance!");
    }
  } 

  /**
   * Destroy an existing link between the working versions. <P> 
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param target 
   *   The fully resolved name of the downstream node to disconnect.
   * 
   * @param source 
   *   The fully resolved name of the upstream node to disconnect.
   * 
   * @throws PipelineException
   *   If unable to destroy the link.
   */
  public synchronized void 
  unlink
  (
   String view, 
   String target, 
   String source
  )
    throws PipelineException
  {
    verifyConnection();

    NodeID id = new NodeID(PackageInfo.sUser, view, target);
    NodeUnlinkReq req = new NodeUnlinkReq(id, source);

    Object obj = performTransaction(NodeRequest.Link, req);

    if(obj instanceof SuccessRsp) {
    }
    else if(obj instanceof FailureRsp) {
      FailureRsp rsp = (FailureRsp) obj;
      throw new PipelineException(rsp.getMessage());	
    }
    else {
      disconnect();
      throw new PipelineException
	("Illegal response received from the NodeMgrServer instance!");
    }
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
   * not contain any upstream node link information.
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
  public synchronized void 
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
      disconnect();
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
  disconnect() 
  {
    if(pSocket == null)
      return;

    try {
      if(pSocket.isConnected()) {
	OutputStream out = pSocket.getOutputStream();
	ObjectOutput objOut = new ObjectOutputStream(out);
	objOut.writeObject(NodeRequest.Disconnect);
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
      objOut.writeObject(NodeRequest.Shutdown);
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
   * Send the given node request to <B>plmaster</B>(1) and 
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
      disconnect();
      throw new PipelineException
	("IO problems on port (" + pPort + "):\n" + 
	 ex.getMessage());
    }
    catch(ClassNotFoundException ex) {
      disconnect();
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

