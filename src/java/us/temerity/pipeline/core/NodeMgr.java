// $Id: NodeMgr.java,v 1.3 2004/03/25 02:14:45 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   M G R                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * The complete set of high-level node operations supported by Pipeline. <P> 
 * 
 * This class is responsible for managing both working and checked-in versions of a nodes as
 * well as auxiliary node information such as change comments and upstream/downstream node 
 * connections. All methods of this class are thread-safe. <P> 
 * 
 * In addition to providing the runtime representation of nodes, this class also provides 
 * the I/O operations necessary to maintain a persistent file system representation for the
 * nodes.  The following details the file system layout of node related information managed 
 * by this class.  <P>
 * 
 * The persistent storage of nodes: <P> 
 * 
 * <DIV style="margin-left: 40px;">
 *   <I>node-dir</I>/ <BR>
 *     <DIV style="margin-left: 20px;">
 *     working/<I>author</I>/<I>view</I>/ <BR>
 *       <DIV style="margin-left: 20px;">
 *       <I>fully-resolved-node-path</I>/ <BR>
 *         <DIV style="margin-left: 20px;">
 *         <I>node-name</I> <BR>
 *         [<I>node-name</I>.backup] <BR>
 *         ... <BR>
 *       </DIV> 
 *       ... <P>
 *     </DIV> 
 * 
 *     repository/ <BR>
 *     <DIV style="margin-left: 20px;">
 *       <I>fully-resolved-node-name</I>/ <BR>
 *       <DIV style="margin-left: 20px;">
 *         <I>revision-number</I> <BR>
 *         ... <BR>
 *       </DIV> 
 *       ... <P> 
 *     </DIV> 
 *
 *     comments/ <BR>
 *     <DIV style="margin-left: 20px;">
 *       <I>fully-resolved-node-name</I>/ <BR>
 *       <DIV style="margin-left: 20px;">
 *         <I>revision-number</I>/ <BR>
 *         <DIV style="margin-left: 20px;">
 *           <I>time-stamp</I> <BR>
 *           ... <BR>
 *         </DIV>
 *         ... <BR> 
 *       </DIV> 
 *       ... <P> 
 *     </DIV> 
 *  
 *     downstream/ <BR>
 *     <DIV style="margin-left: 20px;">
 *       <I>fully-resolved-node-path</I>/ <BR>
 *         <DIV style="margin-left: 20px;">
 *           <I>node-name</I> <BR>
 *         ... <BR> 
 *         </DIV>
 *         ... <P> 
 *     </DIV> 
 *   </DIV> 
 * 
 *   Where (<I>node-dir</I>) is the root of the persistent node storage area set by  
 *   <I>configure(1)</I> or as an agument to the constructor for this class. Each of the 
 *   subdirectories of this top level node directory contain Glue format text files associated
 *   with one of the runtime classes used to represent nodes. These files are group under 
 *   directories named after the (<I>fully-resolved-node-name</I>) or 
 *   (<I>fully-resolved-node-path</I>/<I>node-name</I>) of the nodes associated with the 
 *   runtime instances. <P> 
 * 
 *   The (<CODE>working</CODE>) subdirectory contains Glue translations of 
 *   {@link NodeMod NodeMod} instances saved in files (<I>view</I>) named after the working 
 *   area view owning the working version.  There may also exist a backup files 
 *   (<I>view</I>.backup) containing the previously saved state of the <CODE>NodeMod</CODE> 
 *   instances. <P> 
 * 
 *   The (<CODE>repository</CODE>) subdirectory contains Glue translations of 
 *   {@link NodeVersion NodeVersion} instances saved in files (<I>revision-number</I>) 
 *   named after the revision numbers of the respective checked-in versions. <P> 
 * 
 *   The (<CODE>comments</CODE>) subdirectory contains Glue translations of 
 *   {@link LogMessage LogMessage} instances saved in files (<I>time-stamp</I>) named for the 
 *   time stamp of when the respective change comment was written. <P> 
 *  
 *   Finally, the (<CODE>downstream</CODE>) subdirectory contains Glue translations of 
 *   tables containing downstream node connection information.  The downstream connections 
 *   of both working version and checked-in versions are stored in a file (<I>node-name</I>) 
     named after the node. These files contain cached node connection 
 *   information that can be regenerated at any time from the <CODE>NodeMod</CODE> and 
 *   <CODE>NodeVersion</CODE> data. The purpose of these files is to prevent having to 
 *   load all of the nodes in order to determine downstream connection information. This
 *   is more efficient in terms of memory usage, disk I/O and processor cycles. 
 *   Note that these files are only read the first time a node is accessed and written only 
 *   upon shutdown of the server. <P> 
 * </DIV> 
 * 
 * @see NodeMod
 * @see NodeVersion
 * @see LogMessage
 * @see FileMgr
 */
public
class NodeMgr
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new node manager.
   * 
   * @param dir 
   *   The root node directory.
   */
  public
  NodeMgr
  (
   File dir
  )
  { 
    init(dir);
  }
  
  /** 
   * Construct a new node manager using the default root node directory.
   */
  public
  NodeMgr() 
  { 
    init(PackageInfo.sNodeDir);
  }


  /*-- CONTRUCTION HELPERS -----------------------------------------------------------------*/

  private synchronized void 
  init
  (
   File dir
  )
  { 
    if(dir == null)
      throw new IllegalArgumentException("The root node directory cannot be (null)!");
    pNodeDir = dir;

    pCheckedInLocks   = new HashMap<String,ReentrantReadWriteLock>();
    pCheckedInBundles = new HashMap<String,TreeMap<VersionID,CheckedInBundle>>();

    pWorkingLocks   = new HashMap<NodeID,ReentrantReadWriteLock>();
    pWorkingBundles = new HashMap<NodeID,WorkingBundle>(); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   W O R K I N G   V E R S I O N S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the working version of the node.
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
   *   If unable to retrieve the working version.
   */
  public NodeMod
  getNodeMod
  ( 
   String author, 
   String view, 
   String name
  ) 
    throws PipelineException
  {	 
    NodeID id = new NodeID(author, view, name);
    WorkingBundle bundle = getWorkingBundle(id);
      
    ReentrantReadWriteLock lock = getWorkingLock(id);
    lock.readLock().lock();
    try {
      return new NodeMod(bundle.uVersion);      
    }
    finally {
      lock.readLock().unlock();
    }  
  }  


  /*----------------------------------------------------------------------------------------*/

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
   * Note that any existing upstream dependency relationship information contain in the
   * <CODE>mod</CODE> argument will be ignored.  The {@link #linkNodes linkNodes} and
   * {@link #unlinkNodes unlinkNodes} methods must be used to alter the connections 
   * between working node versions.
   * 
   * @param author 
   *   The name of the user which owns the working version.
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
  public void 
  modifyNodeProperties
  ( 
   String author, 
   String view, 
   NodeMod mod   
  ) 
    throws PipelineException
  {
    NodeID id = new NodeID(author, view, mod.getName());
    WorkingBundle bundle = getWorkingBundle(id);

    ReentrantReadWriteLock lock = getWorkingLock(id);
    lock.writeLock().lock();
    try {
      if(bundle.uVersion.setProperties(mod)) {
	bundle.uOverallNodeState = null;
	bundle.uPropertyState    = null;
      }
    }
    finally {
      lock.writeLock().unlock();
    }  
  } 

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create (or modify an existing) all-to-all dependency connection between the working 
   * versions of the given nodes under the given view owned by the given user. 
   * 
   * @param author 
   *   The name of the user which owns the working versions.
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
   * @throws PipelineException
   *   If unable to create (or modify) the connection.
   */
  public void 
  linkNodes
  (
   String author, 
   String view, 
   String target, 
   String source
  ) 
    throws PipelineException
  {
      
    throw new PipelineException("Not implemented yet.");
     
  } 
   
  /**
   * Create (or modify an existing) one-to-one dependency connection between the working 
   * versions of the given nodes under the given view owned by the given user.
   * 
   * @param author 
   *   The name of the user which owns the working versions.
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
   * @param offset 
   *   The frame index offset of source file indices from target file indices.
   * 
   * @throws PipelineException
   *   If unable to create (or modify) the connection.
   */
  public void 
  linkNodes
  (
   String author, 
   String view, 
   String target, 
   String source, 
   int offset
  ) 
    throws PipelineException
  {

    throw new PipelineException("Not implemented yet.");

  } 
   
  
  /**
   * Destroy an existing dependency connection between the working versions of the 
   * given nodes under the given view owned by the given user.
   * 
   * @param author 
   *   The name of the user which owns the working versions.
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
   *   If unable to destroy the connection.
   */
  public void 
  unlinkNodes
  (
   String author, 
   String view, 
   String target, 
   String source
  )
    throws PipelineException
  {
    
    throw new PipelineException("Not implemented yet.");

  } 


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Disables modification of the working version of a node under the given view owned 
   * by the given user. <P>  
   * 
   * This operation can only be performed on nodes for which the working version is 
   * identical to the checked-in version upon which it is based.  However, it is not 
   * required that the working version is based on the latest checked-in version. <P> 
   * 
   * This operation makes all node properties and dependency (upstream node connection) 
   * information read-only.  In addition, all of the working files associated with this
   * node are replaced with symbolic links to the respective read-only checked-in files 
   * upon which the working files are based.  Note that these working and checked-in files 
   * must have been identical to each other prior to being replaced in order for this 
   * operation to succeed. <P>
   * 
   * The result of this operation is that a particular working version and its associated
   * files will remain in exactly the same state until the node is later unfrozen.  This 
   * can be desirable for nodes which have been checked-in and whose files will be accessed 
   * by regeneration actions which rely on these files remaining unaltered. <P> 
   * 
   * Nodes must be unfrozen using the {@link #unfreeze unfreeze} method before any other 
   * node operations which would result in changes to the working version or associated 
   * files can be performed on the node.
   * 
   * @param author 
   *   The name of the user which owns the working version being frozen.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @throws PipelineException
   *   If unable to sucessfully complete the freeze operation.
   */ 
  public void 
  freeze
  ( 
   String author, 
   String view, 
   String name
  )
    throws PipelineException
  {
    
    throw new PipelineException("Not implemented yet.");
      
  } 

  /** 
   * Enables modification of a working version of a node under the given view owned 
   * by the given user. <P>  
   * 
   * This operation can only be performed on nodes with working versions which have
   * previously been frozen using the {@link #freeze freeze} method. <P> 
   * 
   * This operation makes all node properties and dependency (upstream node connection) 
   * information modifiable once again.  In addition, the symbolic links previous created 
   * for the working version when the node was frozen are replaced by regular file copies 
   * of the checked-in files which were the targets of these symbolic links.  This allows
   * the working files to once again be modified by regeneration actions or manual 
   * user editing. 
   * 
   * @param author 
   *   The name of the user which owns the working version being unfrozen.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @throws PipelineException
   *   If unable to sucessfully complete the unfreeze operation.
   */ 
  public void 
  unfreeze
  ( 
   String author, 
   String view, 
   String name
  )
    throws PipelineException
  {

    throw new PipelineException("Not implemented yet.");

  } 



  /*----------------------------------------------------------------------------------------*/
  /*   C H E C K E D - I N   V E R S I O N S                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the revision numbers of the checked-in versions of the given node.
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @return
   *   The revision numbers in ascending order.
   */
  public TreeSet<VersionID>
  getRevisionNumbers
  ( 
   String name
  ) 
    throws PipelineException
  {
    TreeMap<VersionID,CheckedInBundle> table = getCheckedInBundles(name);

    ReentrantReadWriteLock lock = getCheckedInLock(name);
    lock.readLock().lock();
    try {
      return new TreeSet(table.keySet());
    }
    finally {
      lock.readLock().unlock();
    }
  } 

  /** 
   * Get the revision number of the latest checked-in version of the given node.
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @return
   *   The revision numbers in ascending order.
   */
  public VersionID
  getLatestRevisionNumber
  ( 
   String name
  )
    throws PipelineException
  {
    TreeMap<VersionID,CheckedInBundle> table = getCheckedInBundles(name);

    ReentrantReadWriteLock lock = getCheckedInLock(name);
    lock.readLock().lock();
    try {
      return table.lastKey();
    }
    finally {
      lock.readLock().unlock();
    }
  } 


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the checked-in version of the node with the given revision number.
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @param vid 
   *   The revision number of the checked-in version.
   */
  public NodeVersion
  getNodeVersion
  ( 
   String name, 
   VersionID vid
  ) 
    throws PipelineException
  {
    TreeMap<VersionID,CheckedInBundle> table = getCheckedInBundles(name);

    ReentrantReadWriteLock lock = getCheckedInLock(name);
    lock.readLock().lock();
    try {
      CheckedInBundle bundle = table.get(vid);
      if(bundle == null) 
	throw new PipelineException
 	  ("No version (" + vid + ") exist for node (" + name + ")!");

      return bundle.uVersion;
    }
    finally {
      lock.readLock().unlock();
    }
  } 
  
  /** 
   * Get the latest checked-in version of the node.
   * 
   * @param name 
   *   The fully resolved node name.
   */
  public NodeVersion
  getLatestNodeVersion
  ( 
   String name
  ) 
    throws PipelineException
  {
    TreeMap<VersionID,CheckedInBundle> table = getCheckedInBundles(name);

    ReentrantReadWriteLock lock = getCheckedInLock(name);
    lock.readLock().lock();
    try {
      CheckedInBundle bundle = table.get(table.lastKey());
      if(bundle == null) 
	throw new PipelineException
	  ("No checked-in versions exist for node (" + name + ")!");

      return bundle.uVersion;
    }
    finally {
      lock.readLock().unlock();
    }
  } 

   
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the change comment messages associated a specific checked-in version of the given 
   * node.
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @param vid 
   *   The revision number of the checked-in version.
   * 
   * @return 
   *   The list of messages in order of message timestamp.
   */
  public ArrayList<LogMessage>
  getChangeComments
  ( 
   String name, 
   VersionID vid
  ) 
    throws PipelineException
  {
    TreeMap<VersionID,CheckedInBundle> table = getCheckedInBundles(name);

    ReentrantReadWriteLock lock = getCheckedInLock(name);
    lock.readLock().lock();
    try {
      CheckedInBundle bundle = table.get(vid);
      if(bundle == null) 
	throw new PipelineException
 	  ("No version (" + vid + ") exist for node (" + name + ")!");

      return new ArrayList(bundle.uComments.values());
    }
    finally {
      lock.readLock().unlock();
    }
  } 
   
  /** 
   * Add a new change comment messages to a checked-in version of the given node.
   * 
   * @param author 
   *   The name of the user submitting the message.
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @param vid 
   *   The revision number of the checked-in version.
   * 
   * @param msg 
   *   The change comment message text.
   */
  public void
  addChangeComment
  ( 
   String author, 
   String name, 
   VersionID vid, 
   String msg
  )
    throws PipelineException
  {
   
    throw new PipelineException("Not implemented yet.");

  } 



  /*----------------------------------------------------------------------------------------*/
  /*   N O D E   S T A T U S                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the current status of a node under the given view owned by the given
   * user. <P> 
   * 
   * The returned <CODE>NodeStatus</CODE> object will contain references to the 
   * <CODE>NodeStatus</CODE> of all upstream and downstream trees of nodes connected to 
   * the given node.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param name 
   *   The fully resolved node name.
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
    
    throw new PipelineException("Not implemented yet.");

  } 
    
  


  /*----------------------------------------------------------------------------------------*/
  /*   R E V I S I O N   C O N T R O L                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Register an initial working version of a node under the given view owned by the 
   * given user. <P> 
   * 
   * The <CODE>mod</CODE> argument must have a node name which does not already exist and
   * does not match any of the path components of any existing node.  <P> 
   * 
   * The working version must be an inital version.  In other words, the 
   * {@link NodeMod#getWorkingID() NodeMod.getWorkingID} method must return 
   * <CODE>null</CODE>.  As an intial working version, the <CODE>mod</CODE> argument should
   * not contain any upstream dependency relationship information.
   *  
   * @param author 
   *   The name of the user which will own the working version.
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
   String author,
   String view, 
   NodeMod mod 
  ) 
    throws PipelineException
  {
    
    throw new PipelineException("Not implemented yet.");

  } 

  /**
   * Revoke a previously registered but never checked-in node under the given view owned 
   * by the given user. <P> 
   * 
   * This operation is provided to allow users to remove nodes which they have previously 
   * registered, but which they no longer want to keep or share with other users.  In 
   * addition to removing the working version of the node, this operation also deletes any
   * files associated with the nodes and should therefore be used with caution.
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
   *   If unable to revoke the given node.
   */ 
  public void 
  revoke
  ( 
   String author,
   String view, 
   String name
  ) 
    throws PipelineException
  {
    
    throw new PipelineException("Not implemented yet.");

  } 

  /**
   * Rename a previously registered but never checked-in node under the given view owned 
   * by the given user. <P> 
   * 
   * This operation allows a user to change the name of a previously registered node before 
   * it is checked-in.  In addition to changing the name of the working version, this 
   * operation also renames the files which make up the primary file sequence associated 
   * with the node to match the new node name.
   * 
   * @param author 
   *   The name of the user which owns the working version.
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
   * @throws PipelineException 
   *   If unable to rename the given node or its associated primary files.
   */ 
  public void 
  rename
  ( 
   String author,
   String view, 
   String oldName, 
   String newName
  ) 
    throws PipelineException
  {
    
    throw new PipelineException("Not implemented yet.");

  } 

  /** 
   * Create a new revision of a node based on a working version under the given view owned 
   * by the given user. <P> 
   * 
   * In addition to creating a new node version, this method will make a copy of any  
   * modified data files associated with the working version and place them in the 
   * file repository.
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
   * @param msg 
   *   The check-in log message text.
   * 
   * @param level 
   *   The component level of the revision number of the previous latest version to 
   *   increment in order to generate the revision number for the new version.
   * 
   * @throws PipelineException
   *   If unable to sucessfully complete the check-in operation.
   */  
  public void
  checkIn
  (
   String author,
   String view, 
   String name, 
   String msg, 
   VersionID.Level level
  )
    throws PipelineException
  {
    
    throw new PipelineException("Not implemented yet.");

  }  

  /** 
   * Create a new working version (or update an existing working version) under the given 
   * view owned by the given user for the named node based on an existing checked-in 
   * version. <P> 
   * 
   * @param author 
   *   The name of the user which owns the working version being created (or updated).
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @param vid 
   *   The revision number of version to check-out or <CODE>null</CODE> for the latest 
   *   checked-in version.
   * 
   * @throws PipelineException
   *   If unable to sucessfully complete the check-out operation.
   */  
  public void
  checkOut
  (
   String author, 
   String view, 
   String name, 
   VersionID vid
  )
    throws PipelineException
  {

    throw new PipelineException("Not implemented yet.");
      
  }  

  /** 
   * Remove the working version of the named node under the given view owned by the 
   * given user. <P> 
   * 
   * In addition, this operation also removes any working area files (or links) associated 
   * with the released node. <P> 
   * 
   * Unless the <CODE>override</CODE> argument is set to <CODE>true</CODE>, this operation 
   * will throw an exception if the working version of the node does not have an 
   * <CODE>OverallNodeState</CODE> of {@link OverallNodeState#Identical Identical}.  This is 
   * behavior is designed to prevent accidental loss of node information and associated data 
   * files. Therefore, setting the <CODE>override</CODE> argument to <CODE>false</CODE> is 
   * strongly recommended. <P> 
   * 
   * This operation should be performed on nodes once they are no longer being used
   * to free up node database and file system resources.  
   * 
   * @param author 
   *   The name of the user which owns the working version being released.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @param override 
   *   Ignore all safety precautions and release the node regardless of its state?
   * 
   * @throws PipelineException
   *   If unable to sucessfully complete the release operation.
   */ 
  public void 
  release
  ( 
   String author, 
   String view, 
   String name, 
   boolean override 
  )
    throws PipelineException
  {
    
    throw new PipelineException("Not implemented yet.");

  } 
   
  /** 
   * Deletes all working versions for all views and users as well as all checked-in 
   * versions of the given node. <P> 
   * 
   * In addition, all files associated with the node both in user working areas 
   * and in the file repository will also be deleted. This operation is provided to 
   * allow system administrators to destroy badly named nodes which never should have 
   * been checked-in in the first place. <P> 
   * 
   * Because of the extreemly dangerous and destructive nature of this operation, only 
   * users with priviledged status should be allowed to execute this method. 
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @throws PipelineException
   *   If unable to sucessfully destroy the node.
   */ 
  public void 
  destroy
  ( 
   String name
  )
    throws PipelineException
  {
    
    throw new PipelineException("Not implemented yet.");

  } 



  /*----------------------------------------------------------------------------------------*/
  /*   L O C K   H E L P E R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Lookup the lock for the table of checked-in version bundles for the node with the 
   * given name. 
   * 
   * @param name 
   *   The fully resolved node name
   */
  private ReentrantReadWriteLock
  getCheckedInLock
  (
   String name
  ) 
  {
    synchronized(pCheckedInLocks) {
      ReentrantReadWriteLock lock = pCheckedInLocks.get(name);

      if(lock == null) { 
	lock = new ReentrantReadWriteLock();
	pCheckedInLocks.put(name, lock);
      }

      return lock;
    }
  }

  /** 
   * Lookup the lock for the working bundle with the given node id.
   * 
   * @param id 
   *   The unique working version identifier.
   */
  private ReentrantReadWriteLock
  getWorkingLock
  (
   NodeID id
  ) 
  {
    synchronized(pWorkingLocks) {
      ReentrantReadWriteLock lock = pWorkingLocks.get(id);

      if(lock == null) { 
	lock = new ReentrantReadWriteLock();
	pWorkingLocks.put(id, lock);
      }

      return lock;
    }
  }
 


  /*----------------------------------------------------------------------------------------*/
  /*   B U N D L E   H E L P E R S                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the table of checked-in bundles for the node with the given name.
   * 
   * @param name 
   *   The fully resolved node name.
   */
  private TreeMap<VersionID,CheckedInBundle>
  getCheckedInBundles
  ( 
   String name
  ) 
    throws PipelineException
  {
    TreeMap<VersionID,CheckedInBundle> table = null;
    synchronized(pCheckedInBundles) {
      table = pCheckedInBundles.get(name);
      if(table == null) 
	throw new PipelineException("No checked-in versions exist for node: " + name);

      return table;
    }
  }

  /** 
   * Get the working bundle with the given working version ID.
   * 
   * @param id 
   *   The unique working version identifier.
   */
  private WorkingBundle
  getWorkingBundle
  (
   NodeID id
  )
    throws PipelineException
  { 
    if(id == null) 
      throw new IllegalArgumentException("The working version ID cannot be (null)!");
      
    /* lookup the bundle */ 
    synchronized(pWorkingBundles) {
      WorkingBundle bundle = pWorkingBundles.get(id);
      if(bundle == null)
	throw new PipelineException
	  ("No working version of node (" + id.getName() + ") exists under the view (" + 
	   id.getView() + ") owned by user (" + id.getAuthor() + ")!");

      return bundle;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The information related to a working version of a node for a particular view owned by
   * a particular user.
   */
  private class 
  WorkingBundle
  {
    /** 
     * Construct a new working version bundle. 
     */
    public 
    WorkingBundle
    (
     NodeMod mod
    ) 
    {
      uVersion = mod;
      uTargets = new TreeSet<String>();
    }


    /**
     * The working version of a node. 
     */ 
    public NodeMod  uVersion;

    /** 
     * The fully resolved names of the downstream nodes connected to the working version.
     */
    public TreeSet<String>  uTargets;


    /*--------------------------------------------------------------------------------------*/
    /* The following are the cached node states for this working version.  States with a    */
    /* value of (null) have either never been computed or have been invalidated since the   */
    /* last time they were computed for the associated working version.                     */
    /*--------------------------------------------------------------------------------------*/

    /** 
     * The overall revision control state of the working version of the node. <P> 
     * 
     * May be <CODE>null</CODE> if invalidated.
     */
    public OverallNodeState uOverallNodeState;
    
    /** 
     * The overall file and job queue state of this working version of the node.<P> 
     * 
     * May be <CODE>null</CODE> if invalidated.
     */
    public OverallQueueState uOverallQueueState;

    /**
     * The relationship between the revision numbers of this working version and 
     * the checked-in versions of the node.<P> 
     * 
     * May be <CODE>null</CODE> if invalidated.
     */
    public VersionState  uVersionState;

    /**
     * The relationship between the values of the node properties associated with this 
     * working version and the checked-in versions of the node. <P> 
     * 
     * May be <CODE>null</CODE> if invalidated.
     */
    public PropertyState  uPropertyState;

    /**
     * A comparison of the dependency information (upstream node connections) of this 
     * working version and the latest checked-in version of the node. <P> 
     * 
     * May be <CODE>null</CODE> if invalidated.
     */
    public DependState  uDependState;
    
    /**
     * A table containing the relationship between individual files associated with the 
     * working and checked-in versions of this node indexed by working file sequence. <P> 
     * 
     * May be <CODE>null</CODE> if invalidated.
     */
    public TreeMap<FileSeq,FileState[]>  uFileStates;

    /**
     * The status of individual files associated with the working version of the node 
     * with respect to the queue jobs which generate them. <P> 
     * 
     * May be <CODE>null</CODE> if invalidated.
     */
    public TreeMap<FileSeq,QueueState[]>  uQueueStates;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * The information related to a particular checked-in version of a node.
   */
  private class 
  CheckedInBundle
  {
    /** 
     * Construct a new checked-in version bundle. 
     */
    public 
    CheckedInBundle
    (
     NodeVersion vsn
    ) 
    {
      uVersion  = vsn;
      uTargets  = new TreeMap<String,VersionID>(); 
      uComments = new TreeMap<Date,LogMessage>();
    }

    /**
     * The checked-in version of a node.
     */ 
    public NodeVersion  uVersion;

    /**
     * The fully resolved names and revision numbers of the downstream node connections 
     * for the checked-in version.
     */ 
    public TreeMap<String,VersionID>  uTargets; 

    /**
     * The change comments associated with the checked-in version indexed by 
     * the timestamp of the comment.
     */ 
    public TreeMap<Date,LogMessage>  uComments;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The root node directory.
   */ 
  private File  pNodeDir;


  /**
   * The per-node locks indexed by fully resolved node name. <P> 
   * 
   * These locks protect the checked-in versions of each node. The per-node read-lock should 
   * be aquired for operations which will only access the table of checked-in versions of a 
   * node.  The per-node write-lock should be aquired when adding new checked-in versions to
   * the table of checked-in versions for a node.  No existing checked-in bundle entries in 
   * these tables should ever be modified.
   */
  private HashMap<String,ReentrantReadWriteLock>  pCheckedInLocks;

  /**
   * The checked-in version related information of nodes indexed by fully resolved node 
   * name and revision number.
   */ 
  private HashMap<String,TreeMap<VersionID,CheckedInBundle>>  pCheckedInBundles;


  /**
   * The per-working version locks indexed by working version node ID. <P> 
   * 
   * These locks protect the working version related information of nodes. The per-working
   * version read-lock should be aquired for operations which will only access this 
   * information. The per-working version write-lock should be aquired when creating new 
   * working versions, modifying the information associated with existing working versions 
   * or removing existing working versions.
   */
  private HashMap<NodeID,ReentrantReadWriteLock>  pWorkingLocks;

  /**
   * The working version related information of nodes indexed by working version node ID.
   */ 
  private HashMap<NodeID,WorkingBundle>  pWorkingBundles;
 
}

