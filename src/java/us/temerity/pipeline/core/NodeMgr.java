// $Id: NodeMgr.java,v 1.5 2004/03/26 19:12:15 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.message.*;

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

  /**
   * Initialize a new instance.
   */ 
  private void 
  init
  (
   File dir
  )
  { 
    if(dir == null)
      throw new IllegalArgumentException("The root node directory cannot be (null)!");
    pNodeDir = dir;

    pMakeDirLock      = new Object();
    pNodeNames        = new HashSet<String>();
    pCheckedInLocks   = new HashMap<String,ReentrantReadWriteLock>();
    pCheckedInBundles = new HashMap<String,TreeMap<VersionID,CheckedInBundle>>();
    pWorkingLocks     = new HashMap<NodeID,ReentrantReadWriteLock>();
    pWorkingBundles   = new HashMap<NodeID,WorkingBundle>(); 

    makeRootDirs();
    initNodeNames();
  }

  
  /**
   * Make sure that the root node directories exist.
   */ 
  private void 
  makeRootDirs() 
  {
    if(!pNodeDir.isDirectory()) 
      throw new IllegalArgumentException
	("The root node directory (" + pNodeDir + " does not exist!");
    
    ArrayList<File> dirs = new ArrayList<File>();
    dirs.add(new File(pNodeDir, "repository"));
    dirs.add(new File(pNodeDir, "working"));
    dirs.add(new File(pNodeDir, "comments"));
    dirs.add(new File(pNodeDir, "downstream"));

    synchronized(pMakeDirLock) {
      for(File dir : dirs) {
	if(!dir.isDirectory())
	  if(!dir.mkdir()) 
	    throw new IllegalArgumentException
	      ("Unable to create the directory (" + dir + ")!");
      }
    }
  }
  

  /**
   * Build the initial node name table by searching the file system for node related files.
   */
  private void 
  initNodeNames()
  {
    {
      File dir = new File(pNodeDir, "repository");
      initCheckedInNodeNames(dir.getPath(), dir); 
    }

    {
      File dir = new File(pNodeDir, "working");
      File authors[] = dir.listFiles(); 
      int ak;
      for(ak=0; ak<authors.length; ak++) {
	assert(authors[ak].isDirectory());
	
	File views[] = authors[ak].listFiles();  
	int vk;
	for(vk=0; vk<views.length; vk++) {
	  assert(views[vk].isDirectory());
	  initWorkingNodeNames(views[vk].getPath(), views[vk]);
	}
      }
    }

    // DEBUG 
    {
      System.out.print("Initial Node Names:\n");
      for(String name : pNodeNames) 
	System.out.print("  " + name + "\n");
      System.out.print("\n");
    }
    // DEBUG
  }

  /**
   * Recursively search the checked-in node directories for node names.
   */ 
  private void 
  initCheckedInNodeNames
  (
   String prefix, 
   File dir
  ) 
  {
    boolean allDirs  = true;
    boolean allFiles = true;

    File files[] = dir.listFiles(); 

    {
      int wk;
      for(wk=0; wk<files.length; wk++) {
	if(files[wk].isDirectory()) 
	  allFiles = false;
	else if(files[wk].isFile()) 
	  allDirs = false;
	else
	  assert(false);
      }
    }

    if(allFiles) {
      String path = dir.getPath();
      pNodeNames.add(path.substring(prefix.length()));
    }
    else if(allDirs) {
      int wk;
      for(wk=0; wk<files.length; wk++) 
	initCheckedInNodeNames(prefix, files[wk]);
    }
    else {
      assert(false);
    } 
  }
  
  /**
   * Recursively search the working node directories for node names.
   */
  private void 
  initWorkingNodeNames
  (
   String prefix, 
   File dir
  ) 
  {
    File files[] = dir.listFiles(); 
    int wk;
    for(wk=0; wk<files.length; wk++) {
      if(files[wk].isDirectory()) 
	initWorkingNodeNames(prefix, files[wk]);
      else {
	String path = files[wk].getPath();
	if(!path.endsWith(".backup"))
	  pNodeNames.add(path.substring(prefix.length()));
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   W O R K I N G   V E R S I O N S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the working version of the node.
   * 
   * @param req 
   *   The get working version request.
   * 
   * @return
   *   <CODE>NodeGetWorkingRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to register the inital working version.
   */
  public Object
  getWorkingVersion
  ( 
   NodeGetWorkingReq req
  ) 
  {	 
    if(req == null) 
      return new FailureRsp("The get working version request cannot be (null)!");

    String task = ("NodeMgr.getWorkingVersion(): " + req.getNodeID());

    Date start = new Date();
    long wait = 0;
    ReentrantReadWriteLock lock = getWorkingLock(req.getNodeID());
    lock.readLock().lock();
    try {
      wait  = (new Date()).getTime() - start.getTime();
      start = new Date();
      
      NodeMod mod = new NodeMod(getWorkingBundle(req.getNodeID()).uVersion);
      return new NodeGetWorkingRsp(req.getNodeID(), mod, wait, start);
    }
    catch(PipelineException ex) {
      if(wait > 0) 
	return new FailureRsp(task, ex.getMessage(), wait, start);
      else 
	return new FailureRsp(task, ex.getMessage(), start);
    }
    finally {
      lock.readLock().unlock();
    }  
  }  

  
  // ...


  /*----------------------------------------------------------------------------------------*/
  /*   R E V I S I O N   C O N T R O L                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Register an initial working version of a node. <P> 
   * 
   * @param req 
   *   The node register request.
   *
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to register the inital working version.
   */
  public Object
  register
  (
   NodeRegisterReq req
  ) 
  {
    if(req == null) 
      return new FailureRsp("The node register request cannot be (null)!");

    /* node identifiers */ 
    String name = req.getNodeMod().getName();

    /* reserve the node name, 
         after verifying that it doesn't conflict with existing nodes */ 
    synchronized(pNodeNames) {
      if(pNodeNames.contains(name))
	return new FailureRsp
	  ("Cannot register node (" + name + ") because a node with that name " + 
	   "already exists!");
      
      File path = new File(name);
      File parent = null;
      while((parent = path.getParentFile()) != null) {
	if(pNodeNames.contains(parent.getPath())) 
	  return new FailureRsp
	   ("Cannot register node (" + name + ") because its node path contains " +
	    "an existing node (" + parent + ")!");

	path = parent;
      }

      pNodeNames.add(name);
    }
    
    String task = ("NodeMgr.register(): " + req.getNodeID());

    Date start = new Date();
    long wait = 0;
    ReentrantReadWriteLock lock = getWorkingLock(req.getNodeID());
    lock.writeLock().lock();
    try {
      wait  = (new Date()).getTime() - start.getTime();
      start = new Date();

      /* write the new working version to disk */ 
      try {
	File file   = new File(pNodeDir, req.getNodeID().getWorkingPath().getPath());
	File backup = new File(file + ".backup");
	File dir    = file.getParentFile();
	
	synchronized(pMakeDirLock) {
	  if(!dir.isDirectory()) 
	    if(!dir.mkdirs()) 
	      throw new IOException
		("Unable to create working version directory (" + dir + ")!");
	}
	
	if(backup.exists())
	  if(!backup.delete()) 
	    throw new IOException
	      ("Unable to remove the backup working version file (" + backup + ")!");
	
	if(file.exists()) 
	  if(!file.renameTo(backup)) 
	    throw new IOException
	      ("Unable to backup the current working version file (" + file + ") to the " + 
	       "the file (" + backup + ")!");
	
	{
	  FileWriter out = new FileWriter(file);
	  GlueEncoder ge = new GlueEncoder("NodeMod", req.getNodeMod());
	  out.write(ge.getText());
	  out.flush();
	  out.close();
	}
      }
      catch(Exception ex) { 
	/* remove failed node name */ 
	synchronized(pNodeNames) {
	  pNodeNames.remove(name);
	}

	throw new PipelineException("INTERNAL ERROR:\n" + ex.getMessage());
      }

      /* create a working bundle for the new working version */ 
      synchronized(pWorkingBundles) {
	pWorkingBundles.put(req.getNodeID(), new WorkingBundle(req.getNodeMod()));
      }

      return new SuccessRsp(task, wait, start);
    }
    catch(PipelineException ex) {
      if(wait > 0) 
	return new FailureRsp(task, ex.getMessage(), wait, start);
      else 
	return new FailureRsp(task, ex.getMessage(), start);
    }
    finally {
      lock.writeLock().unlock();
    }  
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

      if(bundle == null) {
	
	// check for file... 


	throw new PipelineException
	  ("No working version of node (" + id.getName() + ") exists under the view (" + 
	   id.getView() + ") owned by user (" + id.getAuthor() + ")!");
      }

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
   * The file system directory creation lock.
   */
  private Object pMakeDirLock;
 
  /**
   * The root node directory.
   */ 
  private File  pNodeDir;


  /**
   * The fully resolved names of all nodes.
   */ 
  private HashSet<String> pNodeNames;

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

