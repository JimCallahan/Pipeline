// $Id: NodeMgr.java,v 1.13 2004/04/14 20:12:27 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.message.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.*;
import java.util.concurrent.atomic.*;

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
 * 
 *     lock<P>
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
 *   {@link NodeMod NodeMod} instances saved in files (<I>node-name</I>) named after the last
 *   component of the node name.  These working version files are organized under 
 *   subdirectories named after the particular user's working area view 
 *   (<I>author</I>/<I>view</I>) owning the working version.  There may also exist a backup 
 *   files (<I>node-name</I>.backup) containing the previously saved state of the 
 *   <CODE>NodeMod</CODE> instances for castastrophic recovery purposes. <P> 
 * 
 *   The (<CODE>repository</CODE>) subdirectory contains Glue translations of 
 *   {@link NodeVersion NodeVersion} instances saved in files (<I>revision-number</I>) 
 *   named after the revision numbers of the respective checked-in versions. <P> 
 * 
 *   The (<CODE>comments</CODE>) subdirectory contains Glue translations of 
 *   {@link LogMessage LogMessage} instances saved in files (<I>time-stamp</I>) named for the 
 *   time stamp of when the respective change comment was written. <P> 
 *  
 *   The (<CODE>downstream</CODE>) subdirectory contains Glue translations of 
 *   {@link DownstreamLinks DownstreamLinks} instances saved in files (<I>node-name</I>)
 *   named after the last component of the node name.  Note that these files are only read 
 *   the first time a node is accessed and written only upon shutdown of the server. <P> 
 * 
 *   The node manager uses an empty file called (<CODE>lock</CODE>) written to the root 
 *   node directory (<I>node-dir</I>) to protect against multiple instances of 
 *   <CODE>NodeMgr</CODE> running simultaneously.  This file is created when the class is
 *   instantiated and removed when the instance is finalized.  If this file already exists
 *   the constructor will throw an exception and refuse to instantiate the class.  The 
 *   lock file may exist even if there are no running instances if there has been a 
 *   catastrophic failure of the Java VM.  In such cases, the file should be manually 
 *   removed. <P> 
 * </DIV> 
 * 
 * @see NodeMgrClient
 * @see NodeMgrServer
 * @see NodeMod
 * @see NodeVersion
 * @see LogMessage
 * @see DownstreamLinks
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
   * @param nodeDir 
   *   The root node directory.
   * 
   * @param prodDir 
   *   The root production directory.
   * 
   * @param fileHostname 
   *   The name of the host running the <B>plfilemgr</B><A>(1) and <B>plnotify</B><A>(1) 
   *   daemons.
   * 
   * @param filePort 
   *   The network port listened to by the <B>plfilemgr</B><A>(1) daemon.
   * 
   * @param controlPort 
   *   The network port listened to by the <B>plnotify</B><A>(1) daemon for 
   *   control connections.
   * 
   * @param monitorPort 
   *   The network port listened to by the <B>plnotify</B><A>(1) daemon for 
   *   monitor connections.
   */
  public
  NodeMgr
  (
   File nodeDir, 
   File prodDir, 
   String fileHostname, 
   int filePort, 
   int controlPort, 
   int monitorPort
  )
  { 
    init(nodeDir, prodDir);

    /* make a connection to the file manager daemon */ 
    pFileMgrClient = new FileMgrClient(fileHostname, filePort);

    /* initialize the monitored directory table */ 
    pMonitored = new HashMap<File,HashSet<NodeID>>();

    /* make a control connection to the directory notification daemon */ 
    pNotifyControlClient = new NotifyControlClient(fileHostname, controlPort);

    /* make a monitor connection to the directory notification daemon and 
        start a task to listen for directory change notifcations */ 
    pDirtyTask = new DirtyTask(fileHostname, monitorPort);
    pDirtyTask.start();
  }


  /*-- CONTRUCTION HELPERS -----------------------------------------------------------------*/

  /**
   * Initialize a new instance.
   * 
   * @param nodeDir 
   *   The root node directory.
   * 
   * @param prodDir 
   *   The root production directory.
   */ 
  private void 
  init
  (
   File nodeDir, 
   File prodDir
  )
  { 
    if(nodeDir == null)
      throw new IllegalArgumentException("The root node directory cannot be (null)!");
    pNodeDir = nodeDir;

    if(prodDir == null)
      throw new IllegalArgumentException("The root production directory cannot be (null)!");
    pProdDir = prodDir;

    /* remove the lock file */ 
    {
      File file = new File(pNodeDir, "lock");
      if(file.exists()) 
	throw new IllegalStateException
	  ("Another node manager is already running!\n" + 
	   "If you are certain this is not the case, remove the lock file (" + file + ")!");

      try {
	FileWriter out = new FileWriter(file);
	out.close();
      }
      catch(IOException ex) {
	throw new IllegalStateException
	  ("Unable to create lock file (" + file + ")!");
      }
    }

    /* initialize the fields */ 
    {
      pMakeDirLock      = new Object();
      pNodeNames        = new HashSet<String>();
      pCheckedInLocks   = new HashMap<String,ReentrantReadWriteLock>();
      pCheckedInBundles = new HashMap<String,TreeMap<VersionID,CheckedInBundle>>();
      pWorkingLocks     = new HashMap<NodeID,ReentrantReadWriteLock>();
      pWorkingBundles   = new HashMap<NodeID,WorkingBundle>();       
      pDownstreamLocks  = new HashMap<String,ReentrantReadWriteLock>();
      pDownstream       = new HashMap<String,DownstreamLinks>();
    }

    /* perform startup I/O operations */ 
    {
      makeRootDirs();
      rebuildDownstreamLinks();
      initNodeNames();
    }
  }


  /*----------------------------------------------------------------------------------------*/

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

    synchronized(pMakeDirLock) {
      for(File dir : dirs) {
	if(!dir.isDirectory())
	  if(!dir.mkdir()) 
	    throw new IllegalArgumentException
	      ("Unable to create the directory (" + dir + ")!");
      }
    }
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Build the initial node name table by searching the file system for node related files.
   */
  private void 
  initNodeNames()
  {
    if(!pNodeNames.isEmpty()) 
      return;

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

    if(!pNodeNames.isEmpty()) {
      StringBuffer buf = new StringBuffer(); 
      buf.append("Node Names:\n");
      for(String name : pNodeNames) 
	buf.append("  " + name + "\n");
      Logs.ops.finer(buf.toString());
    }
  }

  /**
   * Recursively search the checked-in node directories for node names. <P> 
   * 
   * No locks are aquired because this method is only called by the constructor.
   * 
   * @param prefix 
   *   The root directory of checked-in versions.
   * 
   * @param dir
   *   The current directory to process.
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
   * Recursively search the working node directories for node names. <P> 
   * 
   * No locks are aquired because this method is only called by the constructor.
   * 
   * @param prefix 
   *   The root directory of a particular user's view. 
   * 
   * @param dir
   *   The current directory to process.
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

  /** 
   * If the downstream links directory is missing, rebuild the downstream links from 
   * the working and checked-in version of ALL nodes! 
   */ 
  private void 
  rebuildDownstreamLinks()
  {
    {
      File dir = new File(pNodeDir, "downstream");
      if(dir.isDirectory()) 
	return;

      if(!dir.mkdir()) 
	throw new IllegalArgumentException
	  ("Unable to create the directory (" + dir + ")!");
    }

    Logs.ops.fine("Rebuilding Downstream Links Cache...");   

    /* process checked-in versions */ 
    {
      File dir = new File(pNodeDir, "repository");
      collectCheckedInDownstreamLinks(dir.getPath(), dir); 
    }

    /* process working versions */ 
    {
      File dir = new File(pNodeDir, "working");
      File authors[] = dir.listFiles(); 
      int ak;
      for(ak=0; ak<authors.length; ak++) {
	assert(authors[ak].isDirectory());
	String author = authors[ak].getName();
	
	File views[] = authors[ak].listFiles();  
	int vk;
	for(vk=0; vk<views.length; vk++) {
	  assert(views[vk].isDirectory());
	  String view = views[vk].getName();
	  collectWorkingDownstreamLinks(author, view, views[vk].getPath(), views[vk]);
	}
      }
    }

    if(!pDownstream.isEmpty()) { 
      StringBuffer buf = new StringBuffer(); 
      buf.append("Rebuilt Links:\n");
      for(String name : pDownstream.keySet()) 
	buf.append("  " + name + "\n");
      Logs.ops.finer(buf.toString());
    }

    /* shutdown and restart the server */ 
    {
      File nodeDir = pNodeDir;
      File prodDir = pProdDir;

      shutdown();

      init(nodeDir, prodDir);
    }
  }

  /**
   * Recursively search the checked-in node directories for downstream links.<P> 
   * 
   * No locks are aquired because this method is only called by the constructor.
   * 
   * @param prefix 
   *   The root directory of checked-in versions.
   * 
   * @param dir
   *   The current directory to process.
   */ 
  private void 
  collectCheckedInDownstreamLinks
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
      int wk;
      for(wk=0; wk<files.length; wk++) {
	File path = new File(files[wk].getPath().substring(prefix.length()));
	String name = path.getParent();
	VersionID vid = new VersionID(path.getName());
      
	try {
	  NodeVersion vsn = readCheckedInVersion(name, vid);
	  if(vsn == null) 
	    throw new PipelineException
	      ("I/O ERROR:\n" + 
	       "  Somehow the checked-in version (" + vid + ") of node (" + name + ") " + 
	       "was missing!");

	  for(LinkVersion link : vsn.getSources()) {
	    DownstreamLinks dsl = pDownstream.get(link.getName());
	    if(dsl == null) {
	      dsl = new DownstreamLinks(link.getName());
	      pDownstream.put(dsl.getName(), dsl);
	    }
	    
	    dsl.addCheckedIn(link.getVersionID(), vsn.getName(), vsn.getVersionID());
	  }
	}
	catch(PipelineException ex) {
	  Logs.ops.severe(ex.getMessage());
	  Logs.flush();
	  System.exit(1);
	}      
      }
    }
    else if(allDirs) {
      int wk;
      for(wk=0; wk<files.length; wk++) 
	collectCheckedInDownstreamLinks(prefix, files[wk]);
    }
    else {
      assert(false);
    } 
  }
  
  /**
   * Recursively search the working node directories for for downstream links.<P> 
   * 
   * No locks are aquired because this method is only called by the constructor.
   * 
   * @param author 
   *   The of the user which owns the working version..
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param prefix 
   *   The root directory of a particular user's view. 
   * 
   * @param dir
   *   The current directory to process.
   */
  private void 
  collectWorkingDownstreamLinks
  (
   String author, 
   String view, 
   String prefix, 
   File dir
  ) 
  {
    File files[] = dir.listFiles(); 
    int wk;
    for(wk=0; wk<files.length; wk++) {
      if(files[wk].isDirectory()) 
	collectWorkingDownstreamLinks(author, view, prefix, files[wk]);
      else {
	String path = files[wk].getPath();
	if(!path.endsWith(".backup")) {
	  try {
	    NodeID id = new NodeID(author, view, path.substring(prefix.length()));
	    NodeMod mod = readWorkingVersion(id);
	    if(mod == null) 
	      throw new PipelineException
		("I/O ERROR:\n" + 
		 "  Somehow the working version (" + id + ") was missing!");
	    
	    for(LinkMod link : mod.getSources()) {
	      DownstreamLinks dsl = pDownstream.get(link.getName());
	      if(dsl == null) {
		dsl = new DownstreamLinks(link.getName());
		pDownstream.put(dsl.getName(), dsl);
	      }
	    
	      dsl.addWorking(new NodeID(author, view, link.getName()), mod.getName());
	    }
	  }
	  catch(PipelineException ex) {
	    Logs.ops.severe(ex.getMessage());
	    Logs.flush();
	    System.exit(1);
	  }      
	}
      }
    }
  }
  



  /*----------------------------------------------------------------------------------------*/
  /*   S H U T D O W N                                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Shutdown the node manager. <P> 
   * 
   * Also sends a shutdown request to the <B>plfilemgr</B>(1) and <B>plnotify<B>(1) 
   * daemons if there are live network connectins to these daemons. <P> 
   * 
   * It is crucial that this method be called when only a single thread is able to access
   * this instance!  In other words, after all request threads have already exited or by a 
   * restart during the construction of this instance.
   */
  public void  
  shutdown() 
  {
    /* write cached downstream links */ 
    writeAllDownstreamLinks();

    /* remove the lock file */ 
    {
      File file = new File(pNodeDir, "lock");
      file.delete();
    }

    /* close the connection to the file manager */ 
    if(pFileMgrClient != null) {
      try {
	pFileMgrClient.shutdown();
      }
      catch(PipelineException ex) {
	Logs.net.warning(ex.getMessage());
      }
    }

    /* close the control connection to the directory change notification daemon */ 
    if(pNotifyControlClient != null) {
      try {
	pNotifyControlClient.shutdown(); 
      }
      catch(PipelineException ex) {
	Logs.net.warning(ex.getMessage());
      }
    }
      
    /* shutdown task listening to the monitor connection to the directory change 
        notification daemon */ 
    if(pDirtyTask != null) {
      pDirtyTask.shutdown();

      try {
	pDirtyTask.join();
      }
      catch(InterruptedException ex) {
	Logs.net.severe("Interrupted while waiting on the DirtyTask to complete!");
      }
    }

    /* invalidate the fields */ 
    {
      pMakeDirLock         = null;
      pNodeDir             = null;
      pNodeNames           = null;
      pCheckedInLocks      = null;
      pCheckedInBundles    = null;
      pWorkingLocks        = null;
      pWorkingBundles      = null;
      pDownstreamLocks     = null;
      pDownstream          = null;
      pFileMgrClient       = null;
      pMonitored           = null;
      pNotifyControlClient = null;
      pDirtyTask           = null;
    }
  }

  /**
   * Write all of the cached downstream links to disk. <P> 
   * 
   * No locks are aquired because this method is only called by {@link #shutdown shutdown} 
   * when only a single thread should be able to access this instance.
   * 
   * If any I/O problems are encountered, the entire downstream links directory is removed
   * so that it will be rebuilt from scratch the next time the server is started. 
   */ 
  private void 
  writeAllDownstreamLinks()
  {
    try {
      for(DownstreamLinks links : pDownstream.values()) 
	writeDownstreamLinks(links);
    }
    catch(PipelineException ex) {
      Logs.ops.severe(ex.getMessage());
      
      /* remove the entire downstream directory */ 
      {
	Map<String,String> env = System.getenv();
	
	ArrayList<String> args = new ArrayList<String>();
	args.add("--force");
	args.add("--recursive");
	args.add("downstream");
	
	SubProcess proc = 
	  new SubProcess("RemoveDownstreamLinks", "rm", args, env, pNodeDir);
	proc.start();
	
	try {
	  proc.join();
	}
	catch(InterruptedException ex2) {
	  Logs.ops.severe("Interrupted while removing the downstream directory " + 
			  "(" + pNodeDir + "/downstream)!");
	}
	
	if(!proc.wasSuccessful()) {
	  Logs.ops.severe("Unable to removing the downstream directory " + 
			  "(" + pNodeDir + "/downstream)!");
	}
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
    assert(req != null);
    TaskTimer timer = new TaskTimer("NodeMgr.getWorkingVersion(): " + req.getNodeID());

    timer.aquire();
    ReentrantReadWriteLock lock = getWorkingLock(req.getNodeID());
    lock.readLock().lock();
    try {
      timer.resume();	
      
      NodeMod mod = new NodeMod(getWorkingBundle(req.getNodeID()).uVersion);
      return new NodeGetWorkingRsp(timer, req.getNodeID(), mod);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      lock.readLock().unlock();
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
   * Note that any existing upstream dependency relationship information contain in the
   * working version being copied will be ignored.  The {@link #link link} and
   * {@link #unlink unlink} methods must be used to alter the connections 
   * between working node versions.
   * 
   * @param req 
   *   The modify properties request.
   *
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to modify the properties of the working version.
   */
  public Object
  modifyProperties
  (
   NodeModifyPropertiesReq req
  ) 
  {
    assert(req != null);
    TaskTimer timer = new TaskTimer("NodeMgr.modifyProperties(): " + req.getNodeID());

    timer.aquire();
    ReentrantReadWriteLock lock = getWorkingLock(req.getNodeID());
    lock.writeLock().lock();
    try {
      timer.resume();

      /* set the node properties */ 
      WorkingBundle bundle = getWorkingBundle(req.getNodeID());
      NodeMod mod = new NodeMod(bundle.uVersion);
      Date critical = mod.getLastCriticalModification();
      if(mod.setProperties(req.getNodeMod())) {

	/* write the new working version to disk */ 
	writeWorkingVersion(req.getNodeID(), mod);

	/* update the bundle */ 
	bundle.uVersion = mod;

	/* invalidate states */ 
	bundle.uOverallNodeState = null;
	bundle.uPropertyState    = null;	
	if(critical.compareTo(mod.getLastCriticalModification()) < 0) 
	  bundle.uQueueStates = null;
      }

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      lock.writeLock().unlock();
    }      
  }
  
  /**
   * Create or modify an existing link between the working versions. <P> 
   * 
   * @param req 
   *   The node link request.
   *
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to link the working versions.
   */
  public Object
  link
  (
   NodeLinkReq req 
  ) 
  {
    assert(req != null);

    NodeID targetID = req.getTargetID();
    String source   = req.getSourceLink().getName();
    NodeID sourceID = new NodeID(targetID.getAuthor(), targetID.getView(), source);

    TaskTimer timer = new TaskTimer("NodeMgr.link(): " + targetID + " to " + sourceID);

    timer.aquire();
    ReentrantReadWriteLock targetLock = getWorkingLock(targetID);
    targetLock.writeLock().lock();
    ReentrantReadWriteLock downstreamLock = getDownstreamLock(source);
    downstreamLock.writeLock().lock();
    try {
      timer.resume();

      WorkingBundle bundle = getWorkingBundle(targetID);
      if(bundle == null) 
	throw new PipelineException
	  ("Only working versions of nodes can be linked!\n" + 
	   "No working version (" + targetID + ") exists for the downstream node.");

      /* add the link */ 
      NodeMod mod = new NodeMod(bundle.uVersion);
      if(mod.getSource(source) == null) 
	checkForCircularity(timer, source, targetID, 
			    new HashSet<String>(), new Stack<String>()); 
      mod.setSource(req.getSourceLink());
      
      /* write the new working version to disk */ 
      writeWorkingVersion(req.getTargetID(), mod);
      
      /* update the bundle */ 
      bundle.uVersion = mod;

      /* invalidate states */ 
      bundle.uOverallNodeState = null;
      bundle.uDependState      = null;

      /* update the downstream links of the source node */ 
      DownstreamLinks links = getDownstreamLinks(source); 
      links.addWorking(sourceID, targetID.getName());

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      downstreamLock.writeLock().unlock();
      targetLock.writeLock().unlock();
    }    
  }

  /**
   * Destroy an existing link between the working versions. <P> 
   * 
   * @param req 
   *   The node unlink request.
   *
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to unlink the working versions.
   */
  public Object
  unlink
  (
   NodeUnlinkReq req 
  ) 
  {
    assert(req != null);
   
    NodeID targetID = req.getTargetID();
    String source   = req.getSourceName();
    NodeID sourceID = new NodeID(targetID.getAuthor(), targetID.getView(), source);

    TaskTimer timer = new TaskTimer("NodeMgr.unlink(): " + targetID + " from " + sourceID);

    timer.aquire();
    ReentrantReadWriteLock targetLock = getWorkingLock(targetID);
    targetLock.writeLock().lock();
    ReentrantReadWriteLock downstreamLock = getDownstreamLock(source);
    downstreamLock.writeLock().lock();
    try {
      timer.resume();	

      WorkingBundle bundle = getWorkingBundle(targetID);
      if(bundle == null) 
	throw new PipelineException
	  ("Only working versions of nodes can be unlinked!\n" + 
	   "No working version (" + targetID + ") exists for the downstream node.");

      /* remove the link */ 
      NodeMod mod = new NodeMod(bundle.uVersion);
      mod.removeSource(source);
      
      /* write the new working version to disk */ 
      writeWorkingVersion(req.getTargetID(), mod);
      
      /* update the bundle */ 
      bundle.uVersion = mod;
      
      /* invalidate states */ 
      bundle.uOverallNodeState = null;
      bundle.uDependState      = null;

      /* update the downstream links of the source node */ 
      DownstreamLinks links = getDownstreamLinks(source); 
      links.removeWorking(sourceID, targetID.getName());

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      downstreamLock.writeLock().unlock();
      targetLock.writeLock().unlock();
    }    
  }

  /*----------------------------------------------------------------------------------------*/
  /*   N O D E   S T A T U S                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the current overall status of the given node. <P> 
   * 
   * The returned <CODE>NodeStatus</CODE> can be used to access the status of all nodes 
   * reachable through both upstream and downstream connections from the given node.
   * 
   * @param req 
   *   The node status request.
   *
   * @return
   *   <CODE>NodeStatusRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to get the status of the node.
   */ 
  public Object
  status
  ( 
   NodeStatusReq req 
  ) 
  {
    
    return new FailureRsp(new TaskTimer(), "Not implemented yet.");

  } 
    


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
    assert(req != null);
    TaskTimer timer = new TaskTimer("NodeMgr.register(): " + req.getNodeID());

    /* node identifiers */ 
    String name = req.getNodeMod().getName();

    /* reserve the node name, 
         after verifying that it doesn't conflict with existing nodes */ 
    timer.aquire();
    synchronized(pNodeNames) {
      timer.resume();
      
      if(pNodeNames.contains(name))
	return new FailureRsp
	  (timer, "Cannot register node (" + name + ") because a node with that name " + 
	   "already exists!");
      
      File path = new File(name);
      File parent = null;
      while((parent = path.getParentFile()) != null) {
	if(pNodeNames.contains(parent.getPath())) 
	  return new FailureRsp
	   (timer, "Cannot register node (" + name + ") because its node path contains " +
	    "an existing node (" + parent + ")!");

	path = parent;
      }

      pNodeNames.add(name);
    }

    timer.aquire();
    ReentrantReadWriteLock lock = getWorkingLock(req.getNodeID());
    lock.writeLock().lock();
    try {
      timer.resume();

      /* write the new working version to disk */ 
      try {
	writeWorkingVersion(req.getNodeID(), req.getNodeMod());
      }
      catch(PipelineException ex) { 
	synchronized(pNodeNames) {
	  pNodeNames.remove(name);
	}
	throw ex;
      }

      /* create a working bundle for the new working version */ 
      synchronized(pWorkingBundles) {
	pWorkingBundles.put(req.getNodeID(), new WorkingBundle(req.getNodeMod()));
      }

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      lock.writeLock().unlock();
    }  
  } 

  /**
   * Revoke a working version of a node which has never checked-in. <P> 
   * 
   * This operation is provided to allow users to remove nodes which they have previously 
   * registered, but which they no longer want to keep or share with other users.  If a 
   * working version is successfully revoked, all node connections to the revoked node 
   * will be also be removed.
   * 
   * @param req 
   *   The node revoke request.
   *
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to revoke the working version.
   */
  public Object
  revoke
  (
   NodeRevokeReq req
  ) 
  {
    assert(req != null);
    
    NodeID id = req.getNodeID();

    TaskTimer timer = new TaskTimer("NodeMgr.revoke(): " + id);

    /* unlink the downstream working versions from the to be revoked working version */ 
    {
      timer.aquire();
      ReentrantReadWriteLock downstreamLock = getDownstreamLock(id.getName());
      downstreamLock.writeLock().lock();
      try {
	timer.resume();

	DownstreamLinks links = getDownstreamLinks(id.getName()); 
	for(String target : links.getWorking(id)) {
	  NodeID targetID = new NodeID(id.getAuthor(), id.getView(), target);

	  timer.suspend();
	  Object obj = unlink(new NodeUnlinkReq(targetID, id.getName()));
	  timer.accum(((TimedRsp) obj).getTimer());

	  if(obj instanceof FailureRsp)  {
	    FailureRsp rsp = (FailureRsp) obj;
	    return new FailureRsp(timer, rsp.getMessage());
	  }
	}
      }
      catch(PipelineException ex) {
	return new FailureRsp(timer, ex.getMessage());
      }
      finally {
	downstreamLock.writeLock().unlock();
      }
    }

    timer.aquire();
    ReentrantReadWriteLock lock = getWorkingLock(id);
    lock.writeLock().lock();
    try {
      timer.resume();

      WorkingBundle bundle = getWorkingBundle(id);
      if(bundle == null) 
	throw new PipelineException
	  ("No working version (" + id + ") exists to be revoked.");
      
      NodeMod mod = bundle.uVersion;
      if(mod.getWorkingID() != null) 
	throw new PipelineException
	  ("The working version (" + id + ") cannot be revoked because checked-in versions " +
	   "of the node already exist!");

      /* remove the bundle */ 
      synchronized(pWorkingBundles) {
	pWorkingBundles.remove(id);
      }

      /* remove the working version file(s) */ 
      {
	File file   = new File(pNodeDir, id.getWorkingPath().getPath());
	File backup = new File(file + ".backup");

	if(file.isFile()) {
	  if(!file.delete())
	    throw new PipelineException
	      ("Unable to remove the working version file (" + file + ")!");
	}
	else {
	  throw new PipelineException
	    ("Somehow the working version file (" + file + ") did not exist!");
	}

	if(backup.isFile()) {
	  if(!backup.delete())
	    throw new PipelineException      
	      ("Unable to remove the backup working version file (" + backup + ")!");
	}
      }

      /* remove the downstream links of the revoked node */ 
      synchronized(pDownstream) {
	pDownstream.remove(id.getName());
      }

      /* remove the downstream link file */ 
      {
	File file = new File(pNodeDir, "downstream/" + id.getName());
	if(file.isFile()) {
	  if(!file.delete())
	    throw new PipelineException
	      ("Unable to remove the downstream links file (" + file + ")!");
	}
      }

      /* update the downstream links of the source nodes */ 
      for(LinkMod link : mod.getSources()) {
	String source = link.getName();

	timer.aquire();	
	ReentrantReadWriteLock downstreamLock = getDownstreamLock(source);
	downstreamLock.writeLock().lock();
	try {
	  timer.resume();

	  NodeID sourceID = new NodeID(id.getAuthor(), id.getView(), source);
	  DownstreamLinks links = getDownstreamLinks(source); 
	  links.removeWorking(sourceID, id.getName());
 	}  
	finally {
	  downstreamLock.writeLock().unlock();
	}    
      }

      /* remove the associated files */ 
      if(req.removeFiles()) {
	pFileMgrClient.remove(id, mod);	
      }

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      lock.writeLock().unlock();
    }    
  }

  /**
   * Rename a working version of a node which has never checked-in. <P> 
   * 
   * This operation allows a user to change the name of a previously registered node before 
   * it is checked-in. If a working version is successfully renamed, all node connections 
   * will be preserved.
   * 
   * @param req 
   *   The node rename request.
   *
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to rename the working version.
   */
  public Object
  rename
  (
   NodeRenameReq req
  ) 
  {
    assert(req != null);
    
    NodeID id   = req.getNodeID();
    String name = id.getName();

    String nname = req.getNewName();
    NodeID nid   = new NodeID(id.getAuthor(), id.getView(), nname);

    TaskTimer timer = new TaskTimer("NodeMgr.rename(): " + id + " to " + nid);

    /* unlink the downstream working versions from the to be renamed working version 
         while collecting the existing downstream links */ 
    TreeMap<String,LinkMod> dlinks = null;
    {
      dlinks = new TreeMap<String,LinkMod>();

      timer.aquire();
      ReentrantReadWriteLock downstreamLock = getDownstreamLock(id.getName());
      downstreamLock.writeLock().lock();
      try {
	timer.resume();

	DownstreamLinks links = getDownstreamLinks(id.getName()); 
	for(String target : links.getWorking(id)) {
	  NodeID targetID = new NodeID(id.getAuthor(), id.getView(), target);

	  timer.aquire();
	  ReentrantReadWriteLock lock = getWorkingLock(targetID);
	  lock.readLock().lock();
	  try {
	    timer.resume();

	    LinkMod dlink = getWorkingBundle(targetID).uVersion.getSource(name);
	    if(dlink != null) 
	      dlinks.put(target,dlink);
	  }
	  finally {
	    lock.readLock().unlock();
	  }  
	  
	  timer.suspend();
	  Object obj = unlink(new NodeUnlinkReq(targetID, id.getName()));
	  timer.accum(((TimedRsp) obj).getTimer());
	}
      }
      catch(PipelineException ex) {
	return new FailureRsp(timer, ex.getMessage());
      }
      finally {
	downstreamLock.writeLock().unlock();
      }
    }

    timer.aquire();
    ReentrantReadWriteLock lock = getWorkingLock(id);
    lock.writeLock().lock();
    ReentrantReadWriteLock nlock = getWorkingLock(nid);
    nlock.writeLock().lock();
    try {
      timer.resume();

      WorkingBundle bundle = getWorkingBundle(id);
      NodeMod mod = new NodeMod(bundle.uVersion);
      mod.rename(nname);
      mod.removeAllSources();

      /* register the new named node */ 
      {
	Object obj = register(new NodeRegisterReq(nid, mod));
	if(obj instanceof FailureRsp) {
	  FailureRsp rsp = (FailureRsp) obj;
	  throw new PipelineException(rsp.getMessage());	
	}
      }

      /* reconnect the upstream nodes to the new named node */ 
      for(LinkMod ulink : bundle.uVersion.getSources()) {
	timer.suspend();
	Object obj = link(new NodeLinkReq(nid, ulink));
	timer.accum(((TimedRsp) obj).getTimer());
	if(obj instanceof FailureRsp) {
	  FailureRsp rsp = (FailureRsp) obj;
	  return new FailureRsp(timer, rsp.getMessage());
	}
      }

      /* revoke the old named node */ 
      {
	timer.suspend();
	Object obj = revoke(new NodeRevokeReq(id, false));
	timer.accum(((TimedRsp) obj).getTimer());
	if(obj instanceof FailureRsp) {
	  FailureRsp rsp = (FailureRsp) obj;
	  throw new PipelineException(rsp.getMessage());	
	}
      }

      /* rename the files */ 
      if(req.renameFiles()) {
	pFileMgrClient.rename(id, bundle.uVersion, nname);	
      }
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      nlock.writeLock().unlock();
      lock.writeLock().unlock();
    }  

    /* reconnect the downstream nodes to the new named node */ 
    for(String target : dlinks.keySet()) {
      LinkMod dlink = dlinks.get(target);

      NodeID tid = new NodeID(id.getAuthor(), id.getView(), target);
      LinkMod ndlink = new LinkMod(nname, dlink.getCatagory(), 
				   dlink.getRelationship(), dlink.getFrameOffset());

      timer.suspend();
      Object obj = link(new NodeLinkReq(tid, ndlink));
      timer.accum(((TimedRsp) obj).getTimer());
      if(obj instanceof FailureRsp) {
	FailureRsp rsp = (FailureRsp) obj;
	return new FailureRsp(timer, rsp.getMessage());
      }
    }

    return new SuccessRsp(timer);
  }




  /*----------------------------------------------------------------------------------------*/
  /*   N O D E   T R A V E R S A L   H E L P E R S                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Recursively check upstream links for any references to the source node. <P> 
   * 
   * @param name
   *   The next upstream node to check.
   * 
   * @param targetID
   *   The unique working version identifier of the target node of the proposed link.
   * 
   * @param checked
   *   The set of previously checked node names.
   * 
   * @param branch
   *   The stack of nodes in this search branch.
   * 
   * @throws PipelineException 
   *   If a potential link circularity is detected.
   */ 
  private void 
  checkForCircularity
  ( 
   TaskTimer timer, 
   String name,
   NodeID targetID, 
   HashSet<String> checked, 
   Stack<String> branch
  ) 
    throws PipelineException 
  {
    if(checked.contains(name)) 
      return;

    if(targetID.getName().equals(name)) {
      StringBuffer buf = new StringBuffer();
      buf.append("Potential link circularity detected: \n" + 
		 "  " + targetID.getName() + " -> ");
      for(String bname : branch) 
	buf.append(bname + " -> ");
      buf.append(targetID.getName());
      throw new PipelineException(buf.toString());
    }
    
    timer.aquire();
    NodeID id = new NodeID(targetID.getAuthor(), targetID.getView(), name);
    ReentrantReadWriteLock lock = getWorkingLock(id);
    lock.readLock().lock();
    try {
      timer.resume();

      WorkingBundle bundle = getWorkingBundle(id);
      if(bundle == null) 
	throw new PipelineException
	  ("Only working versions of nodes can be linked!\n" + 
	   "No working version (" + id + ") exists for the upstream node.");
      
      checked.add(name);
      branch.push(name);
      for(LinkMod link : bundle.uVersion.getSources()) 
	checkForCircularity(timer, link.getName(), targetID, checked, branch);
      branch.pop();      
    }
    finally {
      lock.readLock().unlock();
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

  /** 
   * Lookup the lock for the downstream links for the node with the given name.
   * 
   * @param name 
   *   The fully resolved node name
   */
  private ReentrantReadWriteLock
  getDownstreamLock
  (
   String name
  ) 
  {
    synchronized(pDownstreamLocks) {
      ReentrantReadWriteLock lock = pDownstreamLocks.get(name);

      if(lock == null) { 
	lock = new ReentrantReadWriteLock();
	pDownstreamLocks.put(name, lock);
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
   * This method assumes that a read/write lock for the checked-in version has already been 
   * aquired.
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
   * This method assumes that a read/write lock for the working version has already been 
   * aquired.
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
    WorkingBundle bundle = null;
    synchronized(pWorkingBundles) {
      bundle = pWorkingBundles.get(id);
    }

    if(bundle != null) {
      monitor(id);
      return bundle;
    }

    /* read in the bundle from disk */ 
    NodeMod mod = readWorkingVersion(id);
    if(mod == null) 
      throw new PipelineException
	("No working version of node (" + id.getName() + ") exists under the view (" + 
	 id.getView() + ") owned by user (" + id.getAuthor() + ")!");
    
    bundle = new WorkingBundle(mod);

    synchronized(pWorkingBundles) {
      pWorkingBundles.put(id, bundle);
    }
    
    monitor(id);
    return bundle;
  }


  /**
   * Get the downstream links for a node.
   * 
   * This method assumes that a read/write lock for the downstream links has already been 
   * aquired.
   * 
   * @param name
   *   The fully resolved node name.
   */ 
  private DownstreamLinks
  getDownstreamLinks
  ( 
   String name
  ) 
    throws PipelineException
  {
    if(name == null) 
      throw new IllegalArgumentException("The node name cannot be (null)!");

    DownstreamLinks links = null;
    synchronized(pDownstream) {
      links = pDownstream.get(name);
    }

    if(links != null) 
      return links;
      
    links = readDownstreamLinks(name);
    if(links == null) 
      links = new DownstreamLinks(name);

    synchronized(pDownstream) {
      pDownstream.put(name, links);
    }

    return links;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   D I R E C T O R Y   N O T I F I C A T I O N S                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Make sure that the directory containing the files associated with the 
   * working version is being monitored.
   */
  private void
  monitor
  (
   NodeID id
  )
    throws PipelineException 
  { 
    synchronized(pMonitored) {
      File dir = id.getWorkingParent();

      HashSet<NodeID> ids = pMonitored.get(dir);
      if(ids == null) {
	ids = new HashSet<NodeID>();
	pMonitored.put(dir, ids);
      }

      if(!ids.contains(id)) {
	ids.add(id);
	pNotifyControlClient.monitor(dir);
      }
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   I / O   H E L P E R S                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the checked-in version to disk. <P> 
   * 
   * This method assumes that the write lock for the checked-in version has already been 
   * aquired.
   * 
   * @param vsn
   *   The checked-in version to write.
   * 
   * @throws PipelineException
   *   If unable to write the checkedi-in version file or create the needed parent 
   *   directories.
   */ 
  private void 
  writeCheckedInVersion
  (
   NodeVersion vsn
  ) 
    throws PipelineException
  {
    Logs.ops.finer("Writing Checked-In Version: " + 
		   vsn.getName() + " (" + vsn.getVersionID() + ")");

    File file = new File(pNodeDir, "repository/" + vsn.getName() + "/" + vsn.getVersionID());
    File dir  = file.getParentFile();

    try {
      synchronized(pMakeDirLock) {
	if(!dir.isDirectory()) 
	  if(!dir.mkdirs()) 
	    throw new IOException
	      ("Unable to create checked-in version directory (" + dir + ")!");
      }
      
      if(file.exists()) 
	throw new IOException
	  ("Somehow a checked-in version file (" + file + ") already exists!");
      
      String glue = null;
      try {
	GlueEncoder ge = new GlueEncoder("NodeVersion", vsn);
	glue = ge.getText();
      }
      catch(GlueException ex) {
	Logs.glu.severe
	  ("Unable to generate a Glue format representation of checked-in " + 
	   "version (" + vsn.getVersionID() + ") of node (" + vsn.getName() + ")!");
	Logs.flush();
	
	throw new IOException(ex.getMessage());
      }
      
      {
	FileWriter out = new FileWriter(file);
	out.write(glue);
	out.flush();
	out.close();
      }
    }
    catch(IOException ex) {
      throw new PipelineException
	("I/O ERROR: \n" + 
	 "  While attempting to write checked-in version (" + vsn.getVersionID() + ") " + 
	 "of node (" + vsn.getName() + ") to file...\n" +
	 "    " + ex.getMessage());
    }
  }


  /**
   * Read the checked-in version from disk. <P> 
   * 
   * This method assumes that the write lock for the checked-in version has already been 
   * aquired.
   * 
   * @param name
   *   The fully resolved node name.
   * 
   * @param vid
   *   The revision number of the checked-in version.
   * 
   * @return 
   *   The checked-in version or <CODE>null</CODE> if no file exists.
   * 
   * @throws PipelineException
   *   If the checked-in version files are corrupted in some manner.
   */ 
  private NodeVersion
  readCheckedInVersion
  (
   String name, 
   VersionID vid
  ) 
    throws PipelineException
  {
    Logs.ops.finer("Reading Checked-In Version: " + name + " (" + vid + ")");

    File file = new File(pNodeDir, "repository/" + name + "/" + vid);
    
    try {
      if(file.exists()) {
	try {
	  FileReader in = new FileReader(file);
	  GlueDecoder gd = new GlueDecoder(in);
	  NodeVersion vsn = (NodeVersion) gd.getObject();
	  in.close();
	  
	  return vsn;
	}
	catch(Exception ex) {
	  Logs.glu.severe
	    ("The checked-in version file (" + file + ") appears to be corrupted!");
	  Logs.flush();
	  
	  throw ex;
	}
      }

      return null;
    }
    catch(Exception ex) {
      throw new PipelineException
	("I/O ERROR: \n" + 
	 "  While attempting to read checked-in version (" + vid + ") of node " + 
	 "(" + name + ") from file...\n" +
	 "    " + ex.getMessage());
    }
  }
      

  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the working version to disk. <P> 
   * 
   * This method assumes that the write lock for the working version has already been 
   * aquired.
   * 
   * @param id 
   *   The unique working version identifier.
   * 
   * @param mod
   *   The working version to write.
   * 
   * @throws PipelineException
   *   If unable to write the working version file or create the needed parent directories.
   */ 
  private void 
  writeWorkingVersion
  (
   NodeID id,
   NodeMod mod 
  ) 
    throws PipelineException
  {
    Logs.ops.finer("Writing Working Version: " + id);

    File file   = new File(pNodeDir, id.getWorkingPath().getPath());
    File backup = new File(file + ".backup");
    File dir    = file.getParentFile();

    try {
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
      
      String glue = null;
      try {
	GlueEncoder ge = new GlueEncoder("NodeMod", mod);
	glue = ge.getText();
      }
      catch(GlueException ex) {
	Logs.glu.severe
	  ("Unable to generate a Glue format representation of working " + 
	   "version (" + id + ")!");
	Logs.flush();
	
	throw new IOException(ex.getMessage());
      }
      
      {
	FileWriter out = new FileWriter(file);
	out.write(glue);
	out.flush();
	out.close();
      }
    }
    catch(IOException ex) {
      throw new PipelineException
	("I/O ERROR: \n" + 
	 "  While attempting to write working version (" + id + ") to file...\n" +
	 "    " + ex.getMessage());
    }
  }


  /**
   * Read the working version from disk. <P> 
   * 
   * This method assumes that the write lock for the working version has already been 
   * aquired.
   * 
   * @param id 
   *   The unique working version identifier.
   * 
   * @return 
   *   The working version or <CODE>null</CODE> if no file or backup file exists.
   * 
   * @throws PipelineException
   *   If the working version files are corrupted in some manner.
   */ 
  private NodeMod
  readWorkingVersion
  (
   NodeID id
  ) 
    throws PipelineException
  {
    Logs.ops.finer("Reading Working Version: " + id);

    File file   = new File(pNodeDir, id.getWorkingPath().getPath());
    File backup = new File(file + ".backup");
    
    try {
      if(file.exists()) {
	try {
	  FileReader in = new FileReader(file);
	  GlueDecoder gd = new GlueDecoder(in);
	  NodeMod mod = (NodeMod) gd.getObject();
	  in.close();
	  
	  return mod;
	}
	catch(Exception ex) {
	  Logs.glu.severe
	    ("The working version file (" + file + ") appears to be corrupted!");
	  Logs.flush();
	  
	  if(backup.exists()) {
	    NodeMod mod = null;
	    try {
	      FileReader in = new FileReader(backup);
	      GlueDecoder gd = new GlueDecoder(in);
	      mod = (NodeMod) gd.getObject();
	      in.close();
	    }
	    catch(Exception ex2) {
	      Logs.glu.severe
		("The backup working version file (" + backup + ") appears to be corrupted!");
	      Logs.flush();
	      
	      throw ex;
	    }
	    
	    Logs.glu.severe
	      ("Successfully recovered the working version from the backup file " + 
	       "(" + backup + ")\n" + 
	       "Renaming the backup to (" + file + ")!");
	    Logs.flush();
	    
	    if(!file.delete()) 
	      throw new IOException
		("Unable to remove the corrupted working version file (" + file + ")!");
	    
	    if(!backup.renameTo(file)) 
	      throw new IOException
		("Unable to replace the corrupted working version file (" + file + ") " + 
		 "with the valid backup file (" + backup + ")!");
	    
	    return mod;
	  }
	  else {
	    Logs.glu.severe
	      ("The backup working version file (" + backup + ") does not exist!");
	    Logs.flush();
	
	    throw ex;
	  }
	}
      }

      return null;
    }
    catch(Exception ex) {
      throw new PipelineException
	("I/O ERROR: \n" + 
	 "  While attempting to read working version (" + id + ") from file...\n" +
	 "    " + ex.getMessage());
    }
  }
      

  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the downstream links to disk. <P> 
   * 
   * This method assumes that the write lock for the downstream links has already been 
   * aquired.
   * 
   * @param links
   *   The downstream links to write.
   * 
   * @throws PipelineException
   *   If unable to write the downstream links file or create the needed parent directories.
   */ 
  private void 
  writeDownstreamLinks
  (
   DownstreamLinks links
  ) 
    throws PipelineException
  {
    Logs.ops.finer("Writing Downstream Links: " + links.getName());

    File file = new File(pNodeDir, "downstream/" + links.getName());
    File dir  = file.getParentFile();
    
    try {
      synchronized(pMakeDirLock) {
	if(!dir.isDirectory()) 
	  if(!dir.mkdirs()) 
	    throw new IOException
	      ("Unable to create downstream links directory (" + dir + ")!");
      } 

      String glue = null;
      try {
	GlueEncoder ge = new GlueEncoder("DownstreamLinks", links);
	glue = ge.getText();
      }
      catch(GlueException ex) {
	Logs.glu.severe
	  ("Unable to generate a Glue format representation of the downstream links " + 
	   "for (" + links.getName() + ")!");
	Logs.flush();
	
	throw new IOException(ex.getMessage());
      }
      
      {
	FileWriter out = new FileWriter(file);
	out.write(glue);
	out.flush();
	out.close();
      }
    }
    catch(IOException ex) {
      throw new PipelineException
	("I/O ERROR: \n" + 
	 "  While attempting to write downstream links for (" + links.getName() + ") " + 
	 "to file...\n" +
	 "    " + ex.getMessage());
    }
  }

  /**
   * Read the downstream links from disk. <P> 
   * 
   * This method assumes that it was called from within a synchronized(pDownstream) block.
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @return 
   *   The downstream links or <CODE>null</CODE> if no downstream links file exists.
   * 
   * @throws PipelineException
   *   If the downstream links file is corrupted in some manner.
   */ 
  private DownstreamLinks
  readDownstreamLinks
  (
   String name
  ) 
    throws PipelineException
  {
    Logs.ops.finer("Reading Downstream Links: " + name);

    File file = new File(pNodeDir, "downstream/" + name);
    
    try {
      if(file.exists()) {
	try {
	  FileReader in = new FileReader(file);
	  GlueDecoder gd = new GlueDecoder(in);
	  DownstreamLinks links = (DownstreamLinks) gd.getObject();
	  in.close();
	  
	  return links;
	}
	catch(Exception ex) {
	  Logs.glu.severe
	    ("The downstream links file (" + file + ") appears to be corrupted!");
	  Logs.flush();
	
	  throw ex;
	}
      }

      return null;
    }
    catch(Exception ex) {
      throw new PipelineException
	("I/O ERROR: \n" + 
	 "  While attempting to read the downstream links for (" + name + ") from file...\n" +
	 "    " + ex.getMessage());
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
    }


    /**
     * The working version of a node. 
     */ 
    public NodeMod  uVersion;


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
     * May be <CODE>null</CODE> if invalidated.  If the entry for a file sequence is missing
     * from this table, then the <CODE>FileState</CODE> for that file sequence has been
     * invalidated.
     */
    public TreeMap<FileSeq,FileState[]>  uFileStates;

    /**
     * The status of individual files associated with the working version of the node 
     * with respect to the queue jobs which generate them. <P> 
     * 
     * May be <CODE>null</CODE> if invalidated.  If the entry for a file sequence is missing
     * from this table, then the <CODE>QueueState</CODE> for that file sequence has been
     * invalidated.
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
      uComments = new TreeMap<Date,LogMessage>();
    }

    /**
     * The checked-in version of a node.
     */ 
    public NodeVersion  uVersion;

    /**
     * The change comments associated with the checked-in version indexed by 
     * the timestamp of the comment.
     */ 
    public TreeMap<Date,LogMessage>  uComments;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Opens a monitor connection to the <B>plnotify<B>(1) daemon and invalidates the cached
   * node state information of the <CODE>WorkingBundles</CODE> who's associated files live
   * in the directories which have been modified.
   */ 
  private
  class DirtyTask
    extends Thread
  {
    DirtyTask
    (
     String hostname, 
     int port
    ) 
    {
      super("NodeMgr:DirtyTask");
      pClient = new NotifyMonitorClient(hostname, port);
      pShutdown = new AtomicBoolean(false);
    }
    
    public void 
    shutdown()
    {
      pShutdown.set(true);
    }

    public void 
    run() 
    { 
      Logs.net.fine("DirtyTask Started.");
      Logs.flush();

      while(!pShutdown.get()) {
	try {
	  HashSet<File> dirs = pClient.watch();

	  HashSet<NodeID> dirty = new HashSet<NodeID>();
	  for(File dir : dirs) {
	    synchronized(pMonitored) {
	      HashSet<NodeID> ids = pMonitored.get(dir);
	      if(ids != null) {
		for(NodeID id : ids) 
		  dirty.add(id);
	      }
	    }
	  }
	  
	  for(NodeID id : dirty) {
	    TaskTimer timer = new TaskTimer("DirtyTask -- Node State Invalidated: " + id);
	    timer.aquire();
	    ReentrantReadWriteLock lock = getWorkingLock(id);
	    lock.writeLock().lock();
	    try {
	      timer.resume();	

	      WorkingBundle bundle = getWorkingBundle(id);
	      bundle.uOverallNodeState  = null;
	      bundle.uOverallQueueState = null;
	      bundle.uFileStates        = null;
	    }
	    catch(PipelineException ex) {
	      Logs.net.warning("DirtyTask: " + ex.getMessage());	      
	    }	    
	    finally {
	      lock.writeLock().unlock();
	    }    	  
	    
	    timer.suspend();
	    Logs.net.finest(timer.toString());
	  }
	}
	catch(PipelineException ex) {
	  Logs.net.warning("DirtyTask: " + ex.getMessage());
	}
      }

      Logs.net.fine("DirtyTask Shutdown.");
      Logs.flush();
    }

    private NotifyMonitorClient pClient;
    private AtomicBoolean       pShutdown;
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
 

  /**
   * The per-node downstream links locks indexed by fully resolved node name. <P> 
   * 
   * These locks protect the cached downstream links of each node. The per-node read-lock 
   * should be aquired for operations which will only access the downstream links of a node.
   * The per-node write-lock should be aquired when adding or removing links for a node.
   */
  private HashMap<String,ReentrantReadWriteLock>  pDownstreamLocks;
  
  /**
   * The table of downstream links indexed by fully resolved node name. <P> 
   * 
   * Access to this table should be protected by a synchronized block.
   */
  private HashMap<String,DownstreamLinks>  pDownstream;

  
  /**
   * The root production directory.
   */ 
  private File  pProdDir;

  /**
   * The connection to the file manager daemon: <B>plfilemgr<B>(1).
   */ 
  private FileMgrClient  pFileMgrClient;

  /**
   * The set of working versions who's associated files are being monitored indexed 
   * by the parent directory of these files relative to the root production directory. <P> 
   * 
   * Access to this table should be protected by a synchronized block.
   */
  private HashMap<File,HashSet<NodeID>> pMonitored;

  /** 
   * The control connection to the directory notification daemon: <B>plnotify<B>(1).
   */ 
  private NotifyControlClient  pNotifyControlClient;

  /** 
   * The task which listens to the monitor connection to the directory notification 
   * daemon: <B>plnotify<B>(1).
   */ 
  private DirtyTask  pDirtyTask;  

}

