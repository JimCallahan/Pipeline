// $Id: NodeMgrClient.java,v 1.14 2004/05/07 21:04:16 jim Exp $

package us.temerity.pipeline;

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
    if(hostname == null) 
      throw new IllegalArgumentException("The hostname argument cannot be (null)!");
    pHostname = hostname;

    if(port < 0) 
      throw new IllegalArgumentException("Illegal port number (" + port + ")!");
    pPort = port;
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
    pHostname = PackageInfo.sMasterServer;
    pPort     = PackageInfo.sMasterPort;
  }



  /*----------------------------------------------------------------------------------------*/
  /*  C O N N E C T I O N                                                                   */
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
   * Order the <B>plmaster</B>(1) daemon to refuse any further requests and then to exit 
   * as soon as all currently pending requests have be completed. <P> 
   * 
   * If successfull, <B>plmaster</B>(1) will also shutdown both the <B>plfilemgr<B>(1) and 
   * <B>plnotify</B>(1) daemons as part of its shutdown procedure.
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
  /*   G E N E R A L                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /* 
   * Get the table of current working area authors and views
   *
   * @return 
   *   The table of working area view names indexed by author user name.
   *
   * @throws PipelineException
   *   If unable to determine the working areas.
   */
  public synchronized TreeMap<String,TreeSet<String>>
  getWorkingAreas() 
    throws PipelineException
  {
    verifyConnection();
	 
    NodeGetWorkingAreasReq req = new NodeGetWorkingAreasReq();

    Object obj = performTransaction(NodeRequest.GetWorkingAreas, req);
    if(obj instanceof NodeGetWorkingAreasRsp) {
      NodeGetWorkingAreasRsp rsp = (NodeGetWorkingAreasRsp) obj;
      return rsp.getTable();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }

  /* 
   * Update the immediate children of all node path components along the given paths
   * which are visible within a working area view owned by the given user. <P> 
   * 
   * @param author 
   *   The of the user which owns the working version..
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param paths 
   *   The set of fully resolved node paths to update.
   * 
   * @throws PipelineException
   *   If unable to update the node paths.
   */
  public synchronized NodeTreeComp
  updatePaths
  (
   String author, 
   String view, 
   TreeSet<String> paths
  ) 
    throws PipelineException
  {
    verifyConnection();
	 
    NodeUpdatePathsReq req = new NodeUpdatePathsReq(author, view, paths);

    Object obj = performTransaction(NodeRequest.UpdatePaths, req);
    if(obj instanceof NodeUpdatePathsRsp) {
      NodeUpdatePathsRsp rsp = (NodeUpdatePathsRsp) obj;
      return rsp.getRootComp();
    }
    else {
      handleFailure(obj);
      return null;
    }
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
    else {
      handleFailure(obj);
      return null;
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
    handleSimpleResponse(obj);
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
    handleSimpleResponse(obj);
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

    Object obj = performTransaction(NodeRequest.Unlink, req);
    handleSimpleResponse(obj);
  } 



  /*----------------------------------------------------------------------------------------*/
  /*   N O D E   S T A T U S                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the status of the tree of nodes rooted at the given node. <P> 
   * 
   * In addition to providing node status information for the given node, the returned 
   * <CODE>NodeStatus</CODE> instance can be used access the status of all nodes (both 
   * upstream and downstream) linked to the given node.  The status information for the 
   * upstream nodes will also include detailed state and version information which is 
   * accessable by calling the {@link NodeStatus#getDetails NodeStatus.getDetails} method.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @throws PipelineException
   *   If unable to determine the status of the node.
   */ 
  public NodeStatus
  status
  ( 
   String author, 
   String view, 
   String name   
  ) 
    throws PipelineException
  {
    verifyConnection();
 
    NodeID id = new NodeID(author, view, name);
    NodeStatusReq req = new NodeStatusReq(id);

    Object obj = performTransaction(NodeRequest.Status, req);
    if(obj instanceof NodeStatusRsp) {
      NodeStatusRsp rsp = (NodeStatusRsp) obj;
      return rsp.getNodeStatus();
    }
    else {
      handleFailure(obj);
       return null;
    }
  } 

  /**
   * Get the status of the tree of nodes rooted at the given node. <P> 
   * 
   * Identical to the {@link NodeStatus(String,String,String) NodeStatus} method which 
   * takes an author name except that the current user is used as the author.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @throws PipelineException
   *   If unable to determine the status of the node.
   */ 
  public NodeStatus
  status
  ( 
   String view, 
   String name   
  ) 
    throws PipelineException
  {
    return status(PackageInfo.sUser, view, name);
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
    handleSimpleResponse(obj);
  }

  /**
   * Revoke a working version of a node which has never checked-in. <P> 
   * 
   * This operation is provided to allow users to remove nodes which they have previously 
   * registered, but which they no longer want to keep or share with other users. If a 
   * working version is successfully revoked, all node connections to the revoked node 
   * will be also be removed. <P> 
   * 
   * In addition to removing the working version of the node, this operation can also 
   * delete the files associated with the working version if the <CODE>removeFiles</CODE>
   * argument is <CODE>true</CODE>.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param name 
   *   The fully resolved node name.
   *
   * @param removeFiles 
   *   Should the files associated with the working version be deleted?
   *
   * @throws PipelineException 
   *   If unable to revoke the given node.
   */ 
  public void 
  revoke
  ( 
   String view, 
   String name, 
   boolean removeFiles
  ) 
    throws PipelineException
  {
    verifyConnection();

    NodeID id = new NodeID(PackageInfo.sUser, view, name);
    NodeRevokeReq req = new NodeRevokeReq(id, removeFiles);

    Object obj = performTransaction(NodeRequest.Revoke, req);
    handleSimpleResponse(obj);
  } 

  /**
   * Rename a working version of a node which has never checked-in. <P> 
   * 
   * This operation allows a user to change the name of a previously registered node before 
   * it is checked-in. If a working version is successfully renamed, all node connections 
   * will be preserved. <P> 
   * 
   * In addition to changing the name of the working version, this operation can also 
   * rename the files associated with the working version to match the new node name if 
   * the <CODE>renameFiles</CODE> argument is <CODE>true</CODE>.  The primary file sequence
   * will be renamed to have a prefix which is identical to the last component of the 
   * <CODE>newName</CODE> argument.  The secondary file sequence prefixes will remain
   * unchanged. Both primary and secondary file sequences will be moved into the working 
   * directory based on the new node name.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param oldName 
   *   The current fully resolved node name.
   * 
   * @param newName 
   *   The new fully resolved node name.
   * 
   * @param renameFiles 
   *   Should the files associated with the working version be renamed?
   * 
   * @throws PipelineException 
   *   If unable to rename the given node or its associated primary files.
   */ 
  public void 
  rename
  ( 
   String view, 
   String oldName, 
   String newName,
   boolean renameFiles
  ) 
    throws PipelineException
  {
    verifyConnection();

    NodeID id = new NodeID(PackageInfo.sUser, view, oldName);
    NodeRenameReq req = new NodeRenameReq(id, newName, renameFiles);

    Object obj = performTransaction(NodeRequest.Rename, req);
    handleSimpleResponse(obj);
  } 

  /** 
   * Check-In the tree of nodes rooted at the given working version. <P> 
   * 
   * The check-in operation proceeds in a depth-first manner checking-in the most upstream
   * nodes first.  The check-in operation aborts at the first failure of a particular node. 
   * It is therefore possible for the overall check-in to fail after already succeeding for 
   * some set of upstream nodes. <P> 
   * 
   * The returned <CODE>NodeStatus</CODE> instance can be used access the status of all 
   * nodes (both upstream and downstream) linked to the given node.  The status information 
   * for the upstream nodes will also include detailed state and version information which is 
   * accessable by calling the {@link NodeStatus#getDetails NodeStatus.getDetails} method.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @param msg 
   *   The check-in message text.
   * 
   * @param level  
   *   The revision number component level to increment.
   * 
   * @return 
   *   The post check-in status of tree of nodes linked to the given node.
   * 
   * @throws PipelineException
   *   If unable to check-in the nodes.
   */ 
  public NodeStatus
  checkIn
  ( 
   String view, 
   String name, 
   String msg, 
   VersionID.Level level   
  ) 
    throws PipelineException
  {
    verifyConnection();

    NodeID id = new NodeID(PackageInfo.sUser, view, name);
    NodeCheckInReq req = new NodeCheckInReq(id, msg, level);

    Object obj = performTransaction(NodeRequest.CheckIn, req);
    if(obj instanceof NodeStatusRsp) {
      NodeStatusRsp rsp = (NodeStatusRsp) obj;
      return rsp.getNodeStatus();
    }
    else {
      handleFailure(obj);
      return null;
    }
  } 
  
  /** 
   * Check-Out the tree of nodes rooted at the given working version. <P> 
   * 
   * If the <CODE>vid</CODE> argument is <CODE>null</CODE> then check-out the latest 
   * version. <P>
   * 
   * The returned <CODE>NodeStatus</CODE> instance can be used access the status of all 
   * nodes (both upstream and downstream) linked to the given node.  The status information 
   * for the upstream nodes will also include detailed state and version information which is 
   * accessable by calling the {@link NodeStatus#getDetails NodeStatus.getDetails} method.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @param vid 
   *   The revision number of the node to check-out.
   * 
   * @param keepNewer
   *   Should upstream nodes which have a newer revision number than the version to be 
   *   checked-out be skipped? 
   * 
   * @return 
   *   The post check-out status of tree of nodes linked to the given node.
   * 
   * @throws PipelineException
   *   If unable to check-out the nodes.
   */ 
  public NodeStatus
  checkOut
  ( 
   String view, 
   String name, 
   VersionID vid, 
   boolean keepNewer
  ) 
    throws PipelineException
  {
    verifyConnection();

    NodeID id = new NodeID(PackageInfo.sUser, view, name);
    NodeCheckOutReq req = new NodeCheckOutReq(id, vid, keepNewer);

    Object obj = performTransaction(NodeRequest.CheckOut, req);
    if(obj instanceof NodeStatusRsp) {
      NodeStatusRsp rsp = (NodeStatusRsp) obj;
      return rsp.getNodeStatus();
    }
    else {
      handleFailure(obj);
      return null;
    }
  } 


  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Make sure the network connection to the server instance has been established.  If the 
   * connection is down, try to reconnect.
   * 
   * @throws PipelineException
   *   If the connection is down and cannot be reestablished. 
   */
  protected synchronized void 
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
   * Send the given request to the server instance and wait for the response.
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
    try {
      OutputStream out = pSocket.getOutputStream();
      ObjectOutput objOut = new ObjectOutputStream(out);
      objOut.writeObject(kind);
      if(req != null) 
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




  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The name of the host running <B>plfilemgr</B>(1).
   */
  private String  pHostname;

  /**
   * The network port listened to by <B>plfilemgr</B>(1).
   */
  private int  pPort;

  /**
   * The network socket connection.
   */
  private Socket  pSocket;

}

