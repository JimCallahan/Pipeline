// $Id: MasterMgr.java,v 1.12 2004/07/14 20:53:13 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.message.*;
import us.temerity.pipeline.toolset.*;
import us.temerity.pipeline.ui.NodeStyles;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.*;
import java.util.concurrent.atomic.*;
import java.util.logging.Level;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   M G R                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * The complete set of high-level operations supported by Pipeline. <P> 
 * 
 * <H3> Pipeline Nodes </H3>
 * 
 * This class is responsible for managing both working and checked-in versions of a nodes as
 * well as auxiliary node information such as downstream node connections. All methods of 
 * this class are thread-safe. <P> 
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
 *       working/<I>author</I>/<I>view</I>/ <BR>
 *       <DIV style="margin-left: 20px;">
 *         <I>fully-resolved-node-path</I>/ <BR>
 *         <DIV style="margin-left: 20px;">
 *           <I>node-name</I> <BR>
 *           [<I>node-name</I>.backup] <BR>
 *           ... <BR>
 *         </DIV> 
 *         ... <P>
 *       </DIV> 
 * 
 *       repository/ <BR>
 *       <DIV style="margin-left: 20px;">
 *         <I>fully-resolved-node-name</I>/ <BR>
 *         <DIV style="margin-left: 20px;">
 *           <I>revision-number</I> <BR>
 *           ... <BR>
 *         </DIV> 
 *         ... <P> 
 *       </DIV> 
 *  
 *      downstream/ <BR>
 *       <DIV style="margin-left: 20px;">
 *         <I>fully-resolved-node-path</I>/ <BR>
 *           <DIV style="margin-left: 20px;">
 *             <I>node-name</I> <BR>
 *           ... <BR> 
 *           </DIV>
 *           ... <P> 
 *       </DIV> 
 * 
 *      lock<P>
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
 *   The (<CODE>downstream</CODE>) subdirectory contains Glue translations of 
 *   {@link DownstreamLinks DownstreamLinks} instances saved in files (<I>node-name</I>)
 *   named after the last component of the node name.  Note that these files are only read 
 *   the first time a node is accessed and written only upon shutdown of the server. <P> 
 * 
 *   The node manager uses an empty file called (<CODE>lock</CODE>) written to the root 
 *   node directory (<I>node-dir</I>) to protect against multiple instances of 
 *   <CODE>MasterMgr</CODE> running simultaneously.  This file is created when the class is
 *   instantiated and removed when the instance is finalized.  If this file already exists
 *   the constructor will throw an exception and refuse to instantiate the class.  The 
 *   lock file may exist even if there are no running instances if there has been a 
 *   catastrophic failure of the Java VM.  In such cases, the file should be manually 
 *   removed. <P> 
 * </DIV> 
 * 
 * <H3> Toolsets </H3>
 * 
 * This class also manages Toolsets used by various Pipeline programs.  A Toolset is a named 
 * collection of shell environmental variable name/value pairs which make up the shell 
 * environment under which Editors, Actions and other auxillary OS level processes are 
 * run. <P>
 * 
 * In addition to providing the runtime representation of Toolsets, this class also provides 
 * the I/O operations necessary to maintain a persistent file system representation. The 
 * persistent storage of Toolset is organized as follows: <P> 
 * 
 * <DIV style="margin-left: 40px;">
 *   <I>node-dir</I>/toolsets/ <BR>
 *   <DIV style="margin-left: 20px;">
 *     packages/ <BR>
 *     <DIV style="margin-left: 20px;">
 *       <I>toolset-package</I>/<BR>
 *       <DIV style="margin-left: 20px;">
 *         <I>revision-number</I> <BR>
 *         ... <BR>
 *       </DIV> 
 *       ... <P>
 *     </DIV> 
 * 
 *     toolsets/<BR>
 *     <DIV style="margin-left: 20px;">
 *       <I>toolset</I> <BR>
 *       ... <P>
 *     </DIV> 
 * 
 *     default-toolset<BR>
 *     active-toolsets<P> 
 * 
 *   </DIV> 
 * 
 *   Where (<I>node-dir</I>) is the root of the persistent node storage area set by  
 *   <I>configure(1)</I> or as an agument to the constructor for this class. <P> 
 *    
 *   Toolsets are built from a set of Packages which contain the evironment needed by a 
 *   particular release of some application software.  Each release of an application 
 *   typically is associated with a version of a Toolset Package.  Packages are stored
 *   in files named after the Package version (<I>revision-number</I>) under a directory
 *   named for the package (<I>toolset-package</I>). <P> 
 * 
 *   Toolsets are stored in files named after the Toolset (<I>toolset</I>) in a seperate 
 *   directory.  Toolsets are built by merging the environments of several Packages.  The
 *   persistent files for Toolsets contains the specific names and versions of the Packages
 *   used to build the Toolset and a cooked environment which is the result of merging 
 *   the packages. <P> 
 * 
 *   The <CODE>default-toolset</CODE> file contains the name of the default active toolset.
 *   The default toolset is used for all newly created nodes unless explicitly overridden
 *   by the user. <P> 
 * 
 *   The <CODE>active-toolsets</CODE> file contains the names of the Toolsets which should
 *   be made visible to users.  All toolsets remain functional regarless of wheter they 
 *   are listed in the <CODE>active-toolsets</CODE> file.  The purpose of this active
 *   toolset list is to filter the toolsets shown to users to the set which are actively 
 *   being used since the number of toolsets can become large over time.  The active list 
 *   will contain all toolsets which are reasonable for an artist to be using for new nodes.
 * </DIV> 
 * 
 * <H3> Miscellaneous Files </H3>
 * 
 * This class also maintains a persistent list of privileged users.  Privileged users are 
 * allowed to perform operations which are restricted for normal users. In general privileged 
 * access is required when an operation is dangerous or involves making changes which affect 
 * all users. The "pipeline" user is always privileged. <P> 
 * 
 * The list of privileged users is maintained in a Glue format text file called: <P> 
 * 
 * <DIV style="margin-left: 40px;">
 *   <I>node-dir</I>/etc/privileged-users <P>
 * 
 *   Where (<I>node-dir</I>) is the root of the persistent node storage area set by  
 *   <I>configure(1)</I> or as an agument to the constructor for this class. <P> 
 * </DIV>
 * 
 * @see MasterMgrClient
 * @see MasterMgrServer
 * @see DownstreamLinks
 * @see FileMgr
 */
public
class MasterMgr
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
  MasterMgr
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
      pMakeDirLock = new Object();

      pDefaultToolsetLock = new Object();
      pDefaultToolset     = null;
      pActiveToolsets     = new TreeSet<String>();
      pToolsets           = new TreeMap<String,Toolset>();
      pToolsetPackages    = new TreeMap<String,TreeMap<VersionID,PackageVersion>>();

      pSuffixEditors = new TreeMap<String,TreeMap<String,SuffixEditor>>();

      pPrivilegedUsers = new TreeSet<String>();

      pWorkingAreaViews = new TreeMap<String,TreeSet<String>>();
      pNodeTreeRoot     = new NodeTreeEntry();

      pCheckedInLocks   = new HashMap<String,ReentrantReadWriteLock>();
      pCheckedInBundles = new HashMap<String,TreeMap<VersionID,CheckedInBundle>>();

      pWorkingLocks   = new HashMap<NodeID,ReentrantReadWriteLock>();
      pWorkingBundles = new HashMap<NodeID,WorkingBundle>();       

      pDownstreamLocks = new HashMap<String,ReentrantReadWriteLock>();
      pDownstream      = new HashMap<String,DownstreamLinks>();
    }

    /* perform startup I/O operations */ 
    {
      makeRootDirs();
      initToolsets();
      initPrivilegedUsers();
      rebuildDownstreamLinks();
      initNodeTree();
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
	("The root node directory (" + pNodeDir + ") does not exist!");
    
    ArrayList<File> dirs = new ArrayList<File>();
    dirs.add(new File(pNodeDir, "repository"));
    dirs.add(new File(pNodeDir, "working"));
    dirs.add(new File(pNodeDir, "toolsets/packages"));
    dirs.add(new File(pNodeDir, "toolsets/toolsets"));
    dirs.add(new File(pNodeDir, "etc"));
    dirs.add(new File(pNodeDir, "etc/suffix-editors"));

    synchronized(pMakeDirLock) {
      for(File dir : dirs) {
	if(!dir.isDirectory())
	  if(!dir.mkdirs()) 
	    throw new IllegalArgumentException
	      ("Unable to create the directory (" + dir + ")!");
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Load the toolset and toolset package indices.
   */ 
  private void 
  initToolsets()
  {
    try {
      readDefaultToolset();
      readActiveToolsets();

      /* initialize toolset keys */ 
      {
	File dir = new File(pNodeDir, "toolsets/toolsets");
	File files[] = dir.listFiles(); 
	int wk;
	for(wk=0; wk<files.length; wk++) {
	  if(files[wk].isFile()) 
	    pToolsets.put(files[wk].getName(), null);
	}
      }

      /* initialize package keys */ 
      {
	File dir = new File(pNodeDir, "toolsets/packages");
	File dirs[] = dir.listFiles(); 
	int dk;
	for(dk=0; dk<dirs.length; dk++) {
	  if(dirs[dk].isDirectory()) {
	    TreeMap<VersionID,PackageVersion> versions = 
	      new TreeMap<VersionID,PackageVersion>();

	    pToolsetPackages.put(dirs[dk].getName(), versions);
	   
	    File files[] = dirs[dk].listFiles(); 
	    int wk;
	    for(wk=0; wk<files.length; wk++) {
	      if(files[wk].isFile()) 
		versions.put(new VersionID(files[wk].getName()), null);
	    }
	  }
	}
      }
    }
    catch(PipelineException ex) {
      throw new IllegalArgumentException(ex.getMessage());
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Load the privileged users if any exist.
   */ 
  private void 
  initPrivilegedUsers()
  {
    try {
      readPrivilegedUsers();
    }
    catch(PipelineException ex) {
      throw new IllegalArgumentException(ex.getMessage());
    }
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Build the initial node name tree by searching the file system for node related files.
   */
  private void 
  initNodeTree()
  {
    {
      File dir = new File(pNodeDir, "repository");
      initCheckedInNodeTree(dir.getPath(), dir); 
    }

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

	  /* add all of the nodes in working area */ 
	  initWorkingNodeTree(views[vk].getPath(), author, view, views[vk]);

	  /* make sure empty working areas are added */ 
	  synchronized(pWorkingAreaViews) {
	    TreeSet<String> vs = pWorkingAreaViews.get(author);
	    if(vs == null) {
	      vs = new TreeSet<String>();
	      pWorkingAreaViews.put(author, vs);
	    }
	    vs.add(view);
	  }
	}
      }
    } 

    logNodeTree();
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
  initCheckedInNodeTree
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
      String full = dir.getPath();
      String path = full.substring(prefix.length());
      if(path.length() > 0) {
	try {
	  TreeMap<VersionID,CheckedInBundle> table = readCheckedInVersions(path);
	  for(CheckedInBundle bundle : table.values()) 
	    addCheckedInNodeTreePath(bundle.uVersion);
	}
	catch(PipelineException ex) {
	  throw new IllegalStateException(ex.getMessage());
	}
      }
    }
    else if(allDirs) {
      int wk;
      for(wk=0; wk<files.length; wk++) 
	initCheckedInNodeTree(prefix, files[wk]);
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
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param dir
   *   The current directory to process.
   */
  private void 
  initWorkingNodeTree
  (
   String prefix, 
   String author, 
   String view, 
   File dir
  ) 
  {
    File files[] = dir.listFiles(); 
    int wk;
    for(wk=0; wk<files.length; wk++) {
      if(files[wk].isDirectory()) 
	initWorkingNodeTree(prefix, author, view, files[wk]);
      else {
	String path = files[wk].getPath();
	if(!path.endsWith(".backup")) {
	  try {
	    NodeID nodeID = new NodeID(author, view, path.substring(prefix.length()));
	    NodeMod mod = readWorkingVersion(nodeID);
	    addWorkingNodeTreePath(nodeID, mod.getSequences());
	  }
	  catch(PipelineException ex) {
	    throw new IllegalStateException(ex.getMessage());
	  }
	}
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

    if(!pDownstream.isEmpty() && Logs.ops.isLoggable(Level.FINER)) { 
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
      String name = dir.getPath().substring(prefix.length());

      
      TreeMap<VersionID,CheckedInBundle> table = null;
      try {
	table = readCheckedInVersions(name);
      }
      catch(PipelineException ex) {
	Logs.ops.severe(ex.getMessage());
	Logs.flush();
	System.exit(1);
      }   

      for(VersionID vid : table.keySet()) {
	NodeVersion vsn = table.get(vid).uVersion;
	  
	{
	  DownstreamLinks dsl = pDownstream.get(name); 
	  if(dsl == null) {
	    dsl = new DownstreamLinks(name);
	    pDownstream.put(dsl.getName(), dsl);
	  }
	  
	  dsl.createCheckedIn(vid);
	}
	
	for(LinkVersion link : vsn.getSources()) {
	  DownstreamLinks dsl = pDownstream.get(link.getName());
	  if(dsl == null) {
	    dsl = new DownstreamLinks(link.getName());
	    pDownstream.put(dsl.getName(), dsl);
	  }
	  
	  dsl.addCheckedIn(link.getVersionID(), vsn.getName(), vsn.getVersionID());
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
	    {
	      DownstreamLinks dsl = pDownstream.get(id.getName());
	      if(dsl == null) {
		dsl = new DownstreamLinks(id.getName());
		pDownstream.put(dsl.getName(), dsl);
	      }
	    
	      dsl.createWorking(id);
	    }	    

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
   * Also sends a shutdown request to the <B>plfilemgr</B>(1) and <B>plnotify</B>(1) 
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

      pDefaultToolsetLock  = null;
      pDefaultToolset      = null;
      pActiveToolsets      = null;
      pToolsets            = null;
      pToolsetPackages     = null;
      
      pSuffixEditors       = null;
      
      pPrivilegedUsers     = null;

      pNodeTreeRoot        = null;
      pWorkingAreaViews    = null;

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
  /*   G E N E R A L                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the default toolset.
   * 
   * @return
   *   <CODE>MiscGetDefaultToolsetNameRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the default toolset name.
   */
  public Object
  getDefaultToolsetName() 
  {    
    TaskTimer timer = new TaskTimer();
    
    timer.aquire();
    synchronized(pDefaultToolsetLock) {
      timer.resume();	

      if(pDefaultToolset != null) 
	return new MiscGetDefaultToolsetNameRsp(timer, pDefaultToolset);
      else 
	return new FailureRsp(timer, "No default toolset is defined!");
    }
  }

  /**
   * Set the name of the default toolset.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to make the given toolset the default.
   */
  public Object
  setDefaultToolsetName
  (
   MiscSetDefaultToolsetNameReq req 
  ) 
  {   
    String tname = req.getName();

    TaskTimer timer = new TaskTimer("MasterMgr.setDefaultToolsetName(): " + tname);

    timer.aquire();
    synchronized(pToolsets) {
      timer.resume();

      if(!pToolsets.containsKey(tname)) 
	return new FailureRsp
	  (timer, 
	   "No toolset named (" + tname + ") exists to be made the default toolset!");
    }

    timer.aquire();
    synchronized(pDefaultToolsetLock) {
      timer.resume();	 
      
      pDefaultToolset = tname;

      try {
	writeDefaultToolset();
      }
      catch(PipelineException ex) {
	return new FailureRsp(timer, ex.getMessage());
      }
    }

    timer.aquire();
    synchronized(pActiveToolsets) {
      timer.resume();	 
      
      if(!pActiveToolsets.contains(tname)) {
	pActiveToolsets.add(tname);

	try {
	  writeActiveToolsets();
	}
	catch(PipelineException ex) {
	  return new FailureRsp(timer, ex.getMessage());
	}
      }
    }

    return new SuccessRsp(timer);
  }



  /**
   * Get the names of the currently active toolsets.
   * 
   * @return
   *   <CODE>MiscGetActiveToolsetNamesRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the toolset names.
   */
  public Object
  getActiveToolsetNames() 
  {    
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    synchronized(pActiveToolsets) {
      timer.resume();

      return new MiscGetActiveToolsetNamesRsp(timer, new TreeSet<String>(pActiveToolsets));
    }
  }

  /**
   * Set the active/inactive state of the toolset with the given name. <P> 
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to make the given toolset active.
   */
  public Object
  setToolsetActive
  (
   MiscSetToolsetActiveReq req 
  ) 
  {
    String tname = req.getName();

    TaskTimer timer = 
      new TaskTimer("MasterMgr.setActiveToolsetName(): " + 
		    tname + " [" + req.isActive() + "]");

    timer.aquire();
    synchronized(pToolsets) {
      timer.resume();

      if(!pToolsets.containsKey(tname)) 
	return new FailureRsp
	  (timer, 
	   "No toolset named (" + tname + ") exists to be made the active!");
    }

    boolean removed = false;

    timer.aquire();
    synchronized(pActiveToolsets) {
      timer.resume();	 

      boolean changed = false;
      if(req.isActive()) {
	if(!pActiveToolsets.contains(tname)) {
	  pActiveToolsets.add(tname);
	  changed = true;
	}
      }
      else {
	if(pActiveToolsets.contains(tname)) {
	  pActiveToolsets.remove(tname);
	  changed = true;
	  removed = true;
	}
      }

      if(changed) {
	try {
	  writeActiveToolsets();
	}
	catch(PipelineException ex) {
	  return new FailureRsp(timer, ex.getMessage());
	}
      }
    }
    
    if(removed) {
      timer.aquire();
      synchronized(pDefaultToolsetLock) {
	timer.resume();	 
	
	if((pDefaultToolset != null) && pDefaultToolset.equals(tname)) {
	  pDefaultToolset = null;
	
	  File file = new File(pNodeDir, "toolsets/default-toolset");
	  if(file.exists()) {
	    if(!file.delete())
	      return new FailureRsp
		(timer, "Unable to remove the old default toolset file (" + file + ")!");
	  }
	}
      }
    }

    return new SuccessRsp(timer);
  }



  /**
   * Get the names of all toolsets.
   * 
   * @return
   *   <CODE>MiscGetToolsetNamesRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the toolset names.
   */
  public Object
  getToolsetNames()
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    synchronized(pToolsets) {
      timer.resume();

      return new MiscGetToolsetNamesRsp(timer, new TreeSet<String>(pToolsets.keySet()));
    }    
  }

  /**
   * Get the toolset with the given name.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetToolsetRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to find the toolset.
   */
  public Object
  getToolset
  ( 
   MiscGetToolsetReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    synchronized(pToolsets) {
      timer.resume();
    
      Toolset tset = pToolsets.get(req.getName());
      if(tset == null) {
	try {
	  tset = readToolset(req.getName());
	}
	catch(PipelineException ex) {
	  return new FailureRsp(timer, ex.getMessage());
	}
      }
      assert(tset != null);

      return new MiscGetToolsetRsp(timer, tset);
    }
  }

  /**
   * Get the cooked toolset environment with the given name.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetToolsetEnvironmentRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to find the toolset.
   */
  public Object
  getToolsetEnvironment
  ( 
   MiscGetToolsetEnvironmentReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    synchronized(pToolsets) {
      timer.resume();

      Toolset tset = pToolsets.get(req.getName());
      if(tset == null) {
	try {
	  tset = readToolset(req.getName());
	}
	catch(PipelineException ex) {
	  return new FailureRsp(timer, ex.getMessage());
	}
      }
      assert(tset != null);
      
      TreeMap<String,String> env = null;
      if((req.getAuthor() != null) && (req.getView() != null)) 
	env = tset.getEnvironment(req.getAuthor(), req.getView());
      else if(req.getAuthor() != null)
	env = tset.getEnvironment(req.getAuthor());
      else 
	env = tset.getEnvironment();
	
      assert(env != null);

      return new MiscGetToolsetEnvironmentRsp(timer, req.getName(), env);
    }
  }

  /**
   * Create a new toolset from the given toolset packages.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to create the toolset and/or toolset packages.
   */
  public Object
  createToolset
  ( 
   MiscCreateToolsetReq req
  )  
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    synchronized(pToolsets) {
      timer.resume();

      if(pToolsets.containsKey(req.getName())) 
	return new FailureRsp
	  (timer, 
	   "Unable to create the toolset (" + req.getName() + ") because a toolset " + 
	   "already exists with that name!");

      /* lookup the packages */  
      ArrayList<PackageCommon> packages = new ArrayList<PackageCommon>();
      timer.aquire();
      synchronized(pToolsetPackages) {
	timer.resume();
      
	for(String pname : req.getPackages()) {
	  VersionID vid = req.getVersions().get(pname);
	  if(vid == null) 
	    return new FailureRsp
	      (timer, 
	       "Unable to create the toolset (" + req.getName() + ") because the " +
	       "revision number for package (" + pname + ") was missing!");

	  TreeMap<VersionID,PackageVersion> versions = pToolsetPackages.get(pname);
	  if((versions == null) || !versions.containsKey(vid))
	    return new FailureRsp
	      (timer, 
	       "Unable to create the toolset (" + req.getName() + ") because the " +
	       "package (" + pname + " v" + vid + ") does not exists!");

	  PackageVersion pkg = versions.get(vid);
	  if(pkg == null) {
	    try {
	      pkg = readToolsetPackage(pname, vid);
	    }
	    catch(PipelineException ex) {
	      return new FailureRsp(timer, ex.getMessage());
	    }
	  }
	  assert(pkg != null);

	  packages.add(pkg);
	}
      }
	  
      /* build the toolset */ 
      Toolset tset = 
	new Toolset(req.getAuthor(), req.getName(), packages, req.getDescription());
      if(tset.hasConflicts()) 
	return new FailureRsp
	  (timer, 
	   "Unable to create the toolset (" + req.getName() + ") due to conflicts between " + 
	   "the supplied packages!");

      try {
	writeToolset(tset);
      }
      catch(PipelineException ex) {
	return new FailureRsp(timer, ex.getMessage());	  
      }
      
      pToolsets.put(tset.getName(), tset);

      return new MiscCreateToolsetRsp(timer, tset);
    }    
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the names and revision numbers of all toolset packages.
   * 
   * @return
   *   <CODE>MiscGetToolsetPackageNamesRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the package names.
   */
  public Object
  getToolsetPackageNames()
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    synchronized(pToolsetPackages) {
      timer.resume();

      TreeMap<String,TreeSet<VersionID>> names = new TreeMap<String,TreeSet<VersionID>>();
      for(String name : pToolsetPackages.keySet()) 
	names.put(name, new TreeSet<VersionID>(pToolsetPackages.get(name).keySet()));

      return new MiscGetToolsetPackageNamesRsp(timer, names);
    }        
  }

  /**
   * Get the toolset package with the given name and revision number. 
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetToolsetPackageRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to find the toolset package.
   */
  public Object
  getToolsetPackage
  ( 
   MiscGetToolsetPackageReq req
  )  
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    synchronized(pToolsetPackages) {
      timer.resume();
    
      TreeMap<VersionID,PackageVersion> versions = pToolsetPackages.get(req.getName());
      if(versions == null) 
	return new FailureRsp
	  (timer, 
	   "No toolset package (" + req.getName() + " v" + req.getVersionID() + ") exists!");

      PackageVersion pkg = versions.get(req.getVersionID());
      if(pkg == null) {
	try {
	  pkg = readToolsetPackage(req.getName(), req.getVersionID());
	}
	catch(PipelineException ex) {
	  return new FailureRsp(timer, ex.getMessage());
	}
      }
      assert(pkg != null);

      return new MiscGetToolsetPackageRsp(timer, pkg);
    }    
  }

  /**
   * Create a new read-only package from the given modifiable package.
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to create the new package.
   */ 
  public Object
  createToolsetPackage
  (
   MiscCreateToolsetPackageReq req
  ) 
  {
    TaskTimer timer = new TaskTimer();
    
    timer.aquire();
    synchronized(pToolsetPackages) {
      timer.resume();

      String pname = req.getPackage().getName();
      VersionID nvid = null;
      TreeMap<VersionID,PackageVersion> versions = pToolsetPackages.get(pname);
      if(versions == null) {
	nvid = new VersionID();

	versions = new TreeMap<VersionID,PackageVersion>();
	pToolsetPackages.put(pname, versions);
      }
      else {
	assert(!versions.isEmpty());
	if(req.getLevel() == null) 
	  return new FailureRsp
	    (timer, 
	     "Unable to create the toolset package (" + pname + ") due to a " + 
	     "missing revision number increment level!");
	
	nvid = new VersionID(versions.lastKey(), req.getLevel());
      }

      PackageVersion pkg = 
	new PackageVersion(req.getAuthor(), req.getPackage(), nvid, req.getDescription());

      try {
	writeToolsetPackage(pkg);
      }
      catch(PipelineException ex) {
	return new FailureRsp(timer, ex.getMessage());	  
      }
      
      versions.put(pkg.getVersionID(), pkg);

      return new MiscCreateToolsetPackageRsp(timer, pkg);
    }
  }
   


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get default editor name for the given filename suffix and user. <P> 
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetEditorForSuffixRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the editor.
   */ 
  public Object
  getEditorForSuffix
  (
   MiscGetEditorForSuffixReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer();
    
    timer.aquire();
    synchronized(pSuffixEditors) {
      timer.resume();

      String author = req.getAuthor();

      TreeMap<String,SuffixEditor> editors = pSuffixEditors.get(author);
      if(editors == null)
	editors = pSuffixEditors.get(author);

      if(editors == null) {
	try {
	  editors = readSuffixEditors(author);
	}
	catch(PipelineException ex) {
	  return new FailureRsp(timer, ex.getMessage());
	}
      }
      assert(editors != null);

      String ename = null;
      SuffixEditor se = editors.get(req.getSuffix());
      if(se != null) 
	ename = se.getEditor();

      return new MiscGetEditorForSuffixRsp(timer, ename);
    }
  }

  /**
   * Get the filename suffix to default editor mappings for the given user. <P> 
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetSuffixEditorsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to get the mappings.
   */ 
  public Object
  getSuffixEditors
  (
    MiscGetSuffixEditorsReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer();
    
    timer.aquire();
    synchronized(pSuffixEditors) {
      timer.resume();
      String author = req.getAuthor();

      TreeMap<String,SuffixEditor> editors = pSuffixEditors.get(author);
      if(editors == null) 
	editors = pSuffixEditors.get(author);

      if(editors == null) {
	try {
	  editors = readSuffixEditors(author);

	  if((editors == null) && !author.equals("pipeline")) {
	    editors = pSuffixEditors.get("pipeline");
	    if(editors == null) 
	      editors = readSuffixEditors("pipeline");
	  }

	  if(editors == null) 
	    editors = new TreeMap<String,SuffixEditor>();
	}
	catch(PipelineException ex) {
	  return new FailureRsp(timer, ex.getMessage());
	}
      }
      assert(editors != null);

      return new MiscGetSuffixEditorsRsp(timer, new TreeSet<SuffixEditor>(editors.values()));
    }
  }

  /**
   * Set the filename suffix to default editor mappings for the given user. <P> 
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to set the mappings.
   */ 
  public Object
  setSuffixEditors
  (
    MiscSetSuffixEditorsReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer();
    
    timer.aquire();
    synchronized(pSuffixEditors) {
      timer.resume();

      try {
	writeSuffixEditors(req.getAuthor(), req.getEditors());
      }
      catch(PipelineException ex) {
	return new FailureRsp(timer, ex.getMessage());
      }

      TreeMap<String,SuffixEditor> editors = new TreeMap<String,SuffixEditor>();
      for(SuffixEditor se : req.getEditors()) 
	editors.put(se.getSuffix(), se);

      pSuffixEditors.put(req.getAuthor(), editors);

      return new SuccessRsp(timer);
    }
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the names of the privileged users. <P> 
   * 
   * @return
   *   <CODE>MiscGetPrivilegedUsersRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the privileged users.
   */ 
  public Object 
  getPrivilegedUsers()
  {
    TaskTimer timer = new TaskTimer();
    
    timer.aquire();
    synchronized(pPrivilegedUsers) {
      timer.resume();	

      TreeSet<String> users = new TreeSet<String>(pPrivilegedUsers);
      return new MiscGetPrivilegedUsersRsp(timer, users);
    }
  }
  
  /**
   * Grant the given user privileged access status. <P> 
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to grant the given user privileged status.
   */ 
  public Object 
  grantPrivileges
  ( 
   MiscGrantPrivilegesReq req 
  ) 
  {
    TaskTimer timer = 
      new TaskTimer("MasterMgr.grantPrivileges(): " + req.getAuthor());
    
    timer.aquire();
    synchronized(pPrivilegedUsers) {
      timer.resume();	

      if(!pPrivilegedUsers.contains(req.getAuthor())) {      
	pPrivilegedUsers.add(req.getAuthor());
	try {
	  writePrivilegedUsers();
	}
	catch(PipelineException ex) {
	  return new FailureRsp(timer, ex.getMessage());
	}      
      }

      return new SuccessRsp(timer);
    }
  }

  /**
   *  Remove the given user's privileged access status. <P> 
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to to remove the given user's privileges.
   */ 
  public Object 
  removePrivileges
  ( 
   MiscRemovePrivilegesReq req 
  ) 
  {
    TaskTimer timer = 
      new TaskTimer("MasterMgr.removePrivileges(): " + req.getAuthor());
    
    timer.aquire();
    synchronized(pPrivilegedUsers) {
      timer.resume();	

      if(pPrivilegedUsers.contains(req.getAuthor())) {      
	pPrivilegedUsers.remove(req.getAuthor());
	try {
	  writePrivilegedUsers();
	}
	catch(PipelineException ex) {
	  return new FailureRsp(timer, ex.getMessage());
	}      
      }

      return new SuccessRsp(timer);
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the table of current working area authors and views.
   * 
   * @return
   *   <CODE>NodeUpdatePathRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to register the inital working version.
   */
  public Object
  getWorkingAreas()
  {
    TaskTimer timer = new TaskTimer();
    
    timer.aquire();
    synchronized(pWorkingAreaViews) {
      timer.resume();	

      TreeMap<String,TreeSet<String>> views = new TreeMap<String,TreeSet<String>>();
      for(String author : pWorkingAreaViews.keySet()) 
	views.put(author, (TreeSet<String>) pWorkingAreaViews.get(author).clone());

      return new NodeGetWorkingAreasRsp(timer, views);
    }
  }

  /**
   * Create a new empty working area. <P> 
   * 
   * If the working area already exists, the operation is successful even though nothing
   * is actually done.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to create the given working area.
   */ 
  public Object 
  createWorkingArea
  ( 
   NodeCreateWorkingAreaReq req 
  ) 
  {
    TaskTimer timer = 
      new TaskTimer("MasterMgr.createWorkingArea(): " + 
		    req.getAuthor() + "|" + req.getView());
    
    timer.aquire();
    synchronized(pWorkingAreaViews) {
      timer.resume();	
      
      String author = req.getAuthor();
      String view   = req.getView();

      /* make sure it doesn't already exist */ 
      TreeSet<String> views = pWorkingAreaViews.get(author);
      if((views != null) && views.contains(view))
	return new SuccessRsp(timer);

      /* create the working area node directory */ 
      File dir = new File(pNodeDir, "working/" + author + "/" + view);
      synchronized(pMakeDirLock) {
	if(!dir.isDirectory()) {
	  if(!dir.mkdirs()) 
	    return new FailureRsp
	      (timer, 
	       "Unable to create the working area (" + view + ") for user (" + author + ")!");
	}
      }

      /* create the working area files directory */ 
      try {
	pFileMgrClient.createWorkingArea(author, view);
      }
      catch(PipelineException ex) {
	return new FailureRsp(timer, ex.getMessage());
      }

      /* add the view to the runtime table */ 
      if(views == null) {
	views = new TreeSet<String>();
	pWorkingAreaViews.put(author, views);
      }
      views.add(view);

      return new SuccessRsp(timer);
    }
  }  


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the immediate children of all node path components along the given path 
   * which are visible within a working area view owned by the user. <P> 
   * 
   * @param req 
   *   The update paths request.
   * 
   * @return
   *   <CODE>NodeUpdatePathsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to register the inital working version.
   */
  public Object
  updatePaths
  (
   NodeUpdatePathsReq req
  ) 
  {
    assert(req != null);

    TaskTimer timer = new TaskTimer();

    timer.aquire();
    synchronized(pNodeTreeRoot) {
      timer.resume();	
      
      NodeTreeComp rootComp = new NodeTreeComp();
      for(String path : req.getPaths()) {
	String comps[] = path.split("/"); 

	NodeTreeComp parentComp   = rootComp;
	NodeTreeEntry parentEntry = pNodeTreeRoot;
	int wk;
	for(wk=1; wk<comps.length; wk++) {
	  for(NodeTreeEntry entry : parentEntry.values()) {
	    if(!parentComp.containsKey(entry.getName())) {
	      NodeTreeComp comp = new NodeTreeComp(entry, req.getAuthor(), req.getView());
	      parentComp.put(comp.getName(), comp);
	    }
	  }
	
	  NodeTreeEntry entry = (NodeTreeEntry) parentEntry.get(comps[wk]); 
	  if(entry == null) {
	    parentEntry = null;
	    break;
	  }

	  NodeTreeComp comp = (NodeTreeComp) parentComp.get(comps[wk]);
	  assert(comp != null);
	  
	  parentEntry = entry;
	  parentComp  = comp;
	}
	
	if((parentEntry != null) && (parentComp != null)) {
	  for(NodeTreeEntry entry : parentEntry.values()) {
	    if(!parentComp.containsKey(entry.getName())) {
	      NodeTreeComp comp = new NodeTreeComp(entry, req.getAuthor(), req.getView());
	      parentComp.put(comp.getName(), comp);
	    }
	  }
	}
      }

      return new NodeUpdatePathsRsp(timer, req.getAuthor(), req.getView(), rootComp);
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
   *   <CODE>FailureRsp</CODE> if unable to retrieve the working version.
   */
  public Object
  getWorkingVersion
  ( 
   NodeGetWorkingReq req
  ) 
  {	 
    assert(req != null);
    TaskTimer timer = new TaskTimer();

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
    TaskTimer timer = new TaskTimer("MasterMgr.modifyProperties(): " + req.getNodeID());

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
    NodeID sourceID = new NodeID(targetID, source);

    TaskTimer timer = new TaskTimer("MasterMgr.link(): " + targetID + " to " + sourceID);

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
    NodeID sourceID = new NodeID(targetID, source);

    TaskTimer timer = new TaskTimer("MasterMgr.unlink(): " + targetID + " from " + sourceID);

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

  /**
   * Add a secondary file sequence to the given working version. <P> 
   * 
   * @param req 
   *   The add secondary request.
   *
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to add the secondary file sequence.
   */
  public Object
  addSecondary
  (
   NodeAddSecondaryReq req 
  ) 
  {
    assert(req != null);
    
    NodeID nodeID = req.getNodeID();
    FileSeq fseq  = req.getFileSequence();

    TaskTimer timer = new TaskTimer("MasterMgr.addSecondary(): " + nodeID);

    /* reserve the node name, 
         after verifying that it doesn't conflict with existing nodes */ 
    timer.aquire();
    synchronized(pNodeTreeRoot) {
      timer.resume();
      
      if(!isNodePathUnused(nodeID.getName(), fseq, true))
	return new FailureRsp
	  (timer, "Cannot add secondary file sequence (" + fseq + ") " + 
	   "to node (" + nodeID.getName() + ") because its name conflicts with " + 
	   "an existing node or one of its associated file sequences!");
      
      TreeSet<FileSeq> secondary = new TreeSet<FileSeq>();
      secondary.add(fseq);
      
      addWorkingNodeTreePath(nodeID, secondary);
      
      logNodeTree(); //DEBUG
    }

    timer.aquire();
    ReentrantReadWriteLock lock = getWorkingLock(nodeID);
    lock.writeLock().lock();
    try {
      timer.resume();	

      WorkingBundle bundle = getWorkingBundle(nodeID);
      if(bundle == null) 
	throw new PipelineException
	  ("Secondary file sequences can only be added to working versions of nodes!\n" + 
	   "No working version (" + nodeID + ") exists.");

      /* remove the link */ 
      NodeMod mod = new NodeMod(bundle.uVersion);
      mod.addSecondarySequence(fseq);
      
      /* write the new working version to disk */ 
      writeWorkingVersion(nodeID, mod);
      
      /* update the bundle */ 
      bundle.uVersion = mod;

      /* invalidate the cached per-file states */ 
      bundle.uFileStates      = null;
      bundle.uFileTimeStamps  = null;

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      
      // needs to remove the secondary file sequence from the NodeTreePath on failure...

      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      lock.writeLock().unlock();
    }    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C H E C K E D - I N   V E R S I O N S                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the checked-in version of the node with the given revision number. <P> 
   * 
   * @param req 
   *   The get checked-in version request.
   * 
   * @return
   *   <CODE>NodeGetCheckedInRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to retrieve the checked-in version.
   */
  public Object
  getCheckedInVersion
  ( 
   NodeGetCheckedInReq req
  ) 
  {	 
    assert(req != null);
    TaskTimer timer = new TaskTimer();

    String name = req.getName();
    VersionID vid = req.getVersionID();

    timer.aquire();
    ReentrantReadWriteLock lock = getCheckedInLock(name);
    lock.readLock().lock();
    try {
      timer.resume();	

      TreeMap<VersionID,CheckedInBundle> checkedIn = getCheckedInBundles(name);
      CheckedInBundle bundle = checkedIn.get(vid);
      if(bundle == null) 
	throw new PipelineException 
	  ("Somehow no checked-in version (" + vid + ") of node (" + name + ") exists!"); 

      return new NodeGetCheckedInRsp(timer, new NodeVersion(bundle.uVersion));
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      lock.readLock().unlock();
    }  
  }  

  /** 
   * Get the log messages associated with all checked-in versions of the given node.
   * 
   * @param req 
   *   The get history request.
   * 
   * @return
   *   <CODE>NodeGetHistoryRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to retrieve the log messages.
   */
  public Object
  getHistory
  ( 
   NodeGetHistoryReq req
  ) 
  {	 
    assert(req != null);
    TaskTimer timer = new TaskTimer();
  
    String name = req.getName();

    timer.aquire();
    ReentrantReadWriteLock lock = getCheckedInLock(name);
    lock.readLock().lock();
    try {
      timer.resume();	

      TreeMap<VersionID,CheckedInBundle> checkedIn = getCheckedInBundles(name);
      TreeMap<VersionID,LogMessage> history = new TreeMap<VersionID,LogMessage>();
      for(VersionID vid : checkedIn.keySet()) 
	history.put(vid, checkedIn.get(vid).uVersion.getLogMessage());

      return new NodeGetHistoryRsp(timer, name, history);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      lock.readLock().unlock();
    }  
  }  

  /** 
   * Get whether each file associated with each checked-in version of the given node 
   * contains new data not present in the previous checked-in versions. <P> 
   * 
   * @param req 
   *   The get file novelty request.
   * 
   * @return
   *   <CODE>NodeGetCheckedInFileNoveltyRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to retrieve the per-file novelty flags.
   */
  public Object
  getCheckedInFileNovelty
  ( 
   NodeGetCheckedInFileNoveltyReq req
  ) 
  {	 
    assert(req != null);
    TaskTimer timer = new TaskTimer();
  
    String name = req.getName();

    timer.aquire();
    ReentrantReadWriteLock lock = getCheckedInLock(name);
    lock.readLock().lock();
    try {
      timer.resume();	

      TreeMap<VersionID,CheckedInBundle> checkedIn = getCheckedInBundles(name);
      TreeMap<VersionID,TreeMap<FileSeq,boolean[]>> novelty = 
	new TreeMap<VersionID,TreeMap<FileSeq,boolean[]>>();

      for(VersionID vid : checkedIn.keySet()) {
	NodeVersion vsn = checkedIn.get(vid).uVersion;

	TreeMap<FileSeq,boolean[]> table = new TreeMap<FileSeq,boolean[]>();
	for(FileSeq fseq : vsn.getSequences()) 
	  table.put(fseq, vsn.isNovel(fseq));

	novelty.put(vid, table);
      }

      return new NodeGetCheckedInFileNoveltyRsp(timer, name, novelty);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      lock.readLock().unlock();
    }  
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
    assert(req != null);
    NodeID nodeID = req.getNodeID();

    TaskTimer timer = new TaskTimer();
    try {
      NodeStatus root = performNodeOperation(new NodeOp(), nodeID, timer);
      return new NodeStatusRsp(timer, nodeID, root);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }    
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
    return register(req, true);
  }

  /**
   * Register an initial working version of a node. <P> 
   * 
   * @param req 
   *   The node register request.
   *
   * @param checkName
   *   Whether to verify that the name of the new node is not already in use.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to register the inital working version.
   */
  private Object
  register
  (
   NodeRegisterReq req, 
   boolean checkName
  ) 
  {
    assert(req != null);

    /* node identifiers */ 
    String name   = req.getNodeMod().getName();
    NodeID nodeID = req.getNodeID();

    TaskTimer timer = new TaskTimer("MasterMgr.register(): " + nodeID);

    /* reserve the node name, 
         after verifying that it doesn't conflict with existing nodes */ 
    if(checkName) {
      timer.aquire();
      synchronized(pNodeTreeRoot) {
	timer.resume();
	
	if(!isNodePathUnused(name, req.getNodeMod().getPrimarySequence(), false)) 
	  return new FailureRsp
	    (timer, "Cannot register node (" + name + ") because its name conflicts with " + 
	     "an existing node or one of its associated file sequences!");
	
	addWorkingNodeTreePath(nodeID, req.getNodeMod().getSequences());
      }

      logNodeTree(); //DEBUG
    }

    timer.aquire();
    ReentrantReadWriteLock lock = getWorkingLock(nodeID);
    lock.writeLock().lock();
    try {
      timer.resume();

      /* write the new working version to disk */
      writeWorkingVersion(nodeID, req.getNodeMod());	

      /* create a working bundle for the new working version */ 
      synchronized(pWorkingBundles) {
	pWorkingBundles.put(nodeID, new WorkingBundle(req.getNodeMod()));
      }

      /* initialize the working downstream links */ 
      timer.aquire();
      ReentrantReadWriteLock downstreamLock = getDownstreamLock(nodeID.getName());
      downstreamLock.writeLock().lock();
      try {
	timer.resume();

	DownstreamLinks links = getDownstreamLinks(nodeID.getName()); 
	links.createWorking(nodeID);
      }
      finally {
	downstreamLock.writeLock().unlock();
      }      

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {

      // needs to remove the NodeTreePath on failure...

      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      lock.writeLock().unlock();
    }  
  }


  /*----------------------------------------------------------------------------------------*/

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

    TaskTimer timer = new TaskTimer("MasterMgr.revoke(): " + id);

    /* unlink the downstream working versions from the to be revoked working version */ 
    {
      timer.aquire();
      ReentrantReadWriteLock downstreamLock = getDownstreamLock(id.getName());
      downstreamLock.writeLock().lock();
      try {
	timer.resume();

	DownstreamLinks links = getDownstreamLinks(id.getName()); 
	assert(links != null);

	for(String target : links.getWorking(id)) {
	  NodeID targetID = new NodeID(id, target);

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

	  NodeID sourceID = new NodeID(id, source);
	  DownstreamLinks links = getDownstreamLinks(source); 
	  links.removeWorking(sourceID, id.getName());
 	}  
	finally {
	  downstreamLock.writeLock().unlock();
	}    
      }

      /* remove the node tree path */ 
      removeWorkingNodeTreePath(id);

      /* remove the associated files */ 
      if(req.removeFiles()) {
	pFileMgrClient.remove(id, mod);	
      }

      unmonitor(id);
      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      lock.writeLock().unlock();
    }    
  }


  /*----------------------------------------------------------------------------------------*/

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
    NodeID nid   = new NodeID(id, nname);

    TaskTimer timer = new TaskTimer("MasterMgr.rename(): " + id + " to " + nid);

    if(name.equals(nname)) 
      return new FailureRsp
	(timer, "Cannot rename node (" + name + ") to the same name!");

    /* determine the new file sequences */ 
    FileSeq primary = null;
    SortedSet<FileSeq> secondary = null;
    {
      timer.aquire();
      ReentrantReadWriteLock lock = getWorkingLock(id);
      lock.readLock().lock();
      try {
	timer.resume();
	
	WorkingBundle bundle = getWorkingBundle(id);
	NodeMod mod = bundle.uVersion;

	{
	  FileSeq fseq = mod.getPrimarySequence();
	  
	  FilePattern opat = fseq.getFilePattern();
	  FrameRange range = fseq.getFrameRange();
	  
	  File path = new File(nname);      
	  FilePattern pat = 
	    new FilePattern(path.getName(), opat.getPadding(), opat.getSuffix());

	  primary = new FileSeq(pat, range);
	}
	
	secondary = mod.getSecondarySequences();
      }
      catch(PipelineException ex) {
	return new FailureRsp(timer, ex.getMessage());
      }
      finally {
	lock.readLock().unlock();
      }
    }

    /* reserve the new node name, 
         after verifying that it doesn't conflict with existing nodes */ 
    timer.aquire();
    synchronized(pNodeTreeRoot) {
      timer.resume();

      if(!isNodePathUnused(nname, primary, false)) 
	return new FailureRsp
	  (timer, "Cannot rename node (" + name + ") to (" + nname + ") because the new " + 
	   "name conflicts with an existing node or one of its associated file sequences!");
      
      TreeSet<FileSeq> fseqs = new TreeSet<FileSeq>();
      fseqs.add(primary);
      fseqs.addAll(secondary);

      addWorkingNodeTreePath(nid, fseqs);

      logNodeTree(); //DEBUG
    }

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
	assert(links != null); 

	for(String target : links.getWorking(id)) {
	  NodeID targetID = new NodeID(id, target);

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
	Object obj = register(new NodeRegisterReq(nid, mod), false);
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
      if(req.renameFiles()) 
	pFileMgrClient.rename(id, bundle.uVersion, nname);	

      unmonitor(id);
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

      NodeID tid = new NodeID(id, target);
      LinkMod ndlink = new LinkMod(nname, dlink.getPolicy(), 
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

  /** 
   * Check-In the tree of nodes rooted at the given working version. <P> 
   * 
   * @param req 
   *   The node check-in request.
   *
   * @return
   *   <CODE>NodeStatusRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to the check-in the nodes.
   */ 
  public Object
  checkIn
  ( 
   NodeCheckInReq req 
  ) 
  {
    assert(req != null);
    NodeID nodeID = req.getNodeID();

    TaskTimer timer = new TaskTimer();
    try {
      NodeStatus root = performNodeOperation(new NodeCheckInOp(req), nodeID, timer);
      return new NodeStatusRsp(timer, nodeID, root);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, 
			    "Check-In operation aborted!\n\n" +
			    ex.getMessage());
    }    
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Check-Out the tree of nodes rooted at the given working version. <P> 
   * 
   * @param req 
   *   The node check-out request.
   *
   * @return
   *   <CODE>NodeStatusRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to the check-out the nodes.
   */ 
  public Object
  checkOut
  ( 
   NodeCheckOutReq req 
  ) 
  {
    assert(req != null);
    NodeID nodeID = req.getNodeID();

    TaskTimer timer = new TaskTimer();
    try {
      //
      // Add a check for running queue jobs related to the nodes to be checked-out and put 
      // hold on the submission of jobs for these nodes until the check-out is complete.
      //
      
      performCheckOut(true, nodeID, req.getVersionID(), req.keepNewer(), 
		      new LinkedList<String>(), new HashSet<String>(), timer);
      
      //
      // Release the queue job submission holds.
      // 
      
      NodeStatus root = performNodeOperation(new NodeOp(), nodeID, timer);
      return new NodeStatusRsp(timer, nodeID, root);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }    
  }

  /**
   * Recursively check-out the given node and all upstream nodes with with it is linked.
   *
   * If the <CODE>vid</CODE> argument is <CODE>null</CODE> then check-out the latest 
   * version. <P> 
   * 
   * @param isRoot
   *   Is this node the root of the check-out tree?
   * 
   * @param nodeID
   *   The unique working version identifier.
   * 
   * @param vid 
   *   The revision number of the node to check-out.
   * 
   * @param keepNewer
   *   Should upstream nodes which have a newer revision number than the version to be 
   *   checked-out be skipped? 
   * 
   * @param branch
   *   The names of the nodes from the root to this node.
   * 
   * @param seen
   *   The names of the previously processed nodes.
   * 
   * @param timer
   *   The shared task timer for this operation.
   * 
   * @throws PipelineException 
   *   If unable to perform the check-out operation.
   */
  private void 
  performCheckOut
  (
   boolean isRoot, 
   NodeID nodeID, 
   VersionID vid, 
   boolean keepNewer, 
   LinkedList<String> branch, 
   HashSet<String> seen, 
   TaskTimer timer   
  ) 
    throws PipelineException 
  {
    String name = nodeID.getName();

    /* check for circularity */ 
    checkBranchForCircularity(name, branch);

    /* skip nodes which have already been processed */ 
    if(seen.contains(name)) 
      return;

    /* push the current node onto the end of the branch */ 
    branch.addLast(name);

    timer.aquire();
    ReentrantReadWriteLock workingLock = getWorkingLock(nodeID);
    workingLock.writeLock().lock();
    ReentrantReadWriteLock checkedInLock = getCheckedInLock(name);
    checkedInLock.readLock().lock();
    try {
      timer.resume();	
      
      /* lookup versions */ 
      WorkingBundle working = null;
      TreeMap<VersionID,CheckedInBundle> checkedIn = null;
      {
	try {
	  working = getWorkingBundle(nodeID);
	}
	catch(PipelineException ex) {
	}

	try {
	  checkedIn = getCheckedInBundles(name);
	}
	catch(PipelineException ex) {
	  if(isRoot) 
	    throw new PipelineException
	      ("There are no checked-in versions of node (" + name + ") to check-out!");
	  else 
	    throw new PipelineException
	      ("Internal Error: " + ex.getMessage());
	}
	assert(checkedIn != null);
      }

      /* extract the working and the checked-in version to be checked-out */ 
      NodeMod work    = null;
      NodeVersion vsn = null;
      {
	if(working != null)
	  work = new NodeMod(working.uVersion);

	if(vid != null) {
	  CheckedInBundle bundle = checkedIn.get(vid);
	  if(bundle == null) 
	    throw new PipelineException 
	      ("Somehow no checked-in version (" + vid + ") of node (" + name + ") exists!"); 
	  vsn = new NodeVersion(bundle.uVersion);
	}
	else {
	  if(checkedIn.isEmpty())
	    throw new PipelineException
	      ("Somehow no checked-in versions of node (" + name + ") exist!"); 
	  CheckedInBundle bundle = checkedIn.get(checkedIn.lastKey());
	  vsn = new NodeVersion(bundle.uVersion);
	}
	assert(vsn != null);
      }

      /* mark having seen this node already */ 
      seen.add(name);
 
      /* skip the check-out if the existing working version is already newer than 
       * the version to be checked-out and the KeepNewer option is set */ 
      if(!isRoot && keepNewer && 
	 (work != null) && (work.getWorkingID().compareTo(vsn.getVersionID()) > 0)) {
	branch.removeLast();
	return;      
      }

      /* process the upstream nodes */ 
      for(LinkVersion link : vsn.getSources()) {
	NodeID lnodeID = new NodeID(nodeID, link.getName());
	performCheckOut(false, lnodeID, link.getVersionID(), keepNewer, branch, seen, timer);
      }

      /* get the current timestamp */ 
      Date timestamp = Dates.now(); 

      /* check-out the files */
      pFileMgrClient.checkOut(nodeID, vsn);
           
      /* create a new working version and write it to disk */ 
      NodeMod nwork = new NodeMod(vsn, timestamp);
      writeWorkingVersion(nodeID, nwork);

      /* initialize new working version */ 
      if(working == null) {
	/* register the node name */ 
	timer.aquire();
	synchronized(pNodeTreeRoot) {
	  timer.resume();
	  addWorkingNodeTreePath(nodeID, nwork.getSequences());
	}

	/* create a new working bundle */ 
	synchronized(pWorkingBundles) {
	  pWorkingBundles.put(nodeID, new WorkingBundle(nwork));
	}

	/* initialize the working downstream links */ 
	{
	  timer.aquire();
	  ReentrantReadWriteLock downstreamLock = getDownstreamLock(name);
	  downstreamLock.writeLock().lock();
	  try {
	    timer.resume();
	    
	    DownstreamLinks links = getDownstreamLinks(name); 
	    links.createWorking(nodeID);
	  }
	  finally {
	    downstreamLock.writeLock().unlock();
	  }      
	}
	  
	/* set the working downstream links from the upstream nodes to this node */ 
	for(LinkMod link : nwork.getSources()) {
	  String lname = link.getName();

	  timer.aquire();
	  ReentrantReadWriteLock downstreamLock = getDownstreamLock(lname);
	  downstreamLock.writeLock().lock();
	  try {
	    timer.resume();
	    
	    DownstreamLinks dsl = getDownstreamLinks(lname);
	    dsl.addWorking(new NodeID(nodeID, lname), name);
	  }  
	  finally {
	    downstreamLock.writeLock().unlock();
	  }     
	}
      }

      /* update existing working version */ 
      else {
	/* update the working bundle */ 
	working.uVersion        = nwork;
	working.uFileStates     = null;
	working.uFileTimeStamps = null;

	/* remove the downstream links from any obsolete upstream nodes */ 
	for(LinkMod link : work.getSources()) {
	  if(!nwork.getSourceNames().contains(link.getName())) {
	    String source = link.getName();
	    
	    timer.aquire();
	    ReentrantReadWriteLock downstreamLock = getDownstreamLock(source);
	    downstreamLock.writeLock().lock();  
	    try {
	      timer.resume();
	      
	      DownstreamLinks links = getDownstreamLinks(source); 
	      links.removeWorking(new NodeID(nodeID, source), name);
	    }
	    finally {
	      downstreamLock.writeLock().unlock();
	    }
	  }
	}  
      }
    }
    finally {
      checkedInLock.readLock().unlock();  
      workingLock.writeLock().unlock();
    }

    /* pop the current node off of the end of the branch */ 
    branch.removeLast();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   N O D E   P A T H   T R E E   H E L P E R S                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Add the given checked-in version to the node path tree. <P> 
   * 
   * Creates any branch components which do not already exist.
   * 
   * @param vsn
   *   The checked-in version of the node.
   */ 
  private void 
  addCheckedInNodeTreePath
  (
   NodeVersion vsn
  )
  {
    synchronized(pNodeTreeRoot) {
      String comps[] = vsn.getName().split("/"); 
      
      NodeTreeEntry parent = pNodeTreeRoot;
      int wk;
      for(wk=1; wk<(comps.length-1); wk++) {
	NodeTreeEntry entry = (NodeTreeEntry) parent.get(comps[wk]);
	if(entry == null) {
	  entry = new NodeTreeEntry(comps[wk]);
	  parent.put(entry.getName(), entry);
	}
	parent = entry;
      }
      
      String name = comps[comps.length-1];
      NodeTreeEntry entry = (NodeTreeEntry) parent.get(name);
      if(entry == null) {
	entry = new NodeTreeEntry(name, true);
	parent.put(entry.getName(), entry);
      }
      else {
	entry.setCheckedIn(true);
      }

      for(FileSeq fseq : vsn.getSequences())
	entry.addSequence(fseq);
    }
  }

  /**
   * Add the given working version to the node path tree. <P> 
   * 
   * Creates any branch components which do not already exist.
   * 
   * @param nodeID
   *   The unique working version identifier.
   * 
   * @param fseqs
   *   The file sequences associated with the working version.
   */ 
  private void 
  addWorkingNodeTreePath
  (
   NodeID nodeID, 
   SortedSet<FileSeq> fseqs
  )
  {
    synchronized(pWorkingAreaViews) {
      TreeSet<String> views = pWorkingAreaViews.get(nodeID.getAuthor());
      if(views == null) {
	views = new TreeSet<String>();
	pWorkingAreaViews.put(nodeID.getAuthor(), views);
      }
      views.add(nodeID.getView());
    }

    synchronized(pNodeTreeRoot) {
      String comps[] = nodeID.getName().split("/"); 
      
      NodeTreeEntry parent = pNodeTreeRoot;
      int wk;
      for(wk=1; wk<(comps.length-1); wk++) {
	NodeTreeEntry entry = (NodeTreeEntry) parent.get(comps[wk]);
	if(entry == null) {
	  entry = new NodeTreeEntry(comps[wk]);
	  parent.put(entry.getName(), entry);
	}
	parent = entry;
      }
      
      String name = comps[comps.length-1];
      NodeTreeEntry entry = (NodeTreeEntry) parent.get(name);
      if(entry == null) {
	entry = new NodeTreeEntry(name, false);
	parent.put(entry.getName(), entry);
      }
      
      entry.addWorking(nodeID.getAuthor(), nodeID.getView());
      
      if(fseqs != null) {
	for(FileSeq fseq : fseqs)
	  entry.addSequence(fseq);
      }
    }
  }

  /**
   * Remove the given working version from the node path tree. <P> 
   * 
   * Removes any branch components which become empty due to the working version removal.
   * 
   * @param nodeID
   *   The unique working version identifier.
   */ 
  private void 
  removeWorkingNodeTreePath
  (
   NodeID nodeID
  )
  {
    synchronized(pNodeTreeRoot) {
      String comps[] = nodeID.getName().split("/"); 
      
      Stack<NodeTreeEntry> stack = new Stack<NodeTreeEntry>();
      stack.push(pNodeTreeRoot);

      int wk;
      for(wk=1; wk<comps.length; wk++) {
	NodeTreeEntry entry = (NodeTreeEntry) stack.peek().get(comps[wk]);
	assert(entry != null);
	stack.push(entry);
      }

      NodeTreeEntry entry = stack.pop();
      assert(entry != null); 
      assert(entry.isLeaf());

      entry.removeWorking(nodeID.getAuthor(), nodeID.getView());
      if(!entry.hasWorking() && !entry.isCheckedIn()) {
	while(!stack.isEmpty()) {
	  NodeTreeEntry parent = stack.pop();
	  assert(!parent.isLeaf());
	  
	  parent.remove(entry.getName());
	  if(!parent.isEmpty()) 
	    break;

	  entry = parent;
	}
      }
    }
  }

  /** 
   * Is the given fully resolved node name unused?
   * 
   * @param name
   *   The fully resolved node name.
   * 
   * @param fseq
   *   The file sequence to test.
   * 
   * @param seqsOnly
   *   Only check the file sequences and ignore existing node name conflicts.
   */ 
  private boolean
  isNodePathUnused
  (
   String name, 
   FileSeq fseq, 
   boolean seqsOnly 
  ) 
  {
    synchronized(pNodeTreeRoot) {
      String comps[] = name.split("/"); 
      
      NodeTreeEntry parent = pNodeTreeRoot;
      int wk;
      for(wk=1; wk<comps.length; wk++) {
	NodeTreeEntry entry = (NodeTreeEntry) parent.get(comps[wk]);
	if(wk < (comps.length-1)) {
	  if(entry == null) 
	    return true;
	}
	else {
	  if(!seqsOnly && (entry != null)) 
	    return false;

	  for(NodeTreeEntry leaf : parent.values()) {
	    if(!leaf.isSequenceUnused(fseq)) 
	      return false;	      
	  }

	  return true;
	}

	parent = entry;
      }
      
      assert(false);
      return false;
    }
  }

  /**
   * Log the node tree contents.
   */ 
  public void 
  logNodeTree() 
  {
    synchronized(pNodeTreeRoot) {
      if(!pNodeTreeRoot.isEmpty() && Logs.ops.isLoggable(Level.FINER)) {
	StringBuffer buf = new StringBuffer(); 
	buf.append("Node Tree:\n");
	logNodeTreeHelper(pNodeTreeRoot, 1, buf);
	Logs.ops.finer(buf.toString());
      }
    }
  }

  private void 
  logNodeTreeHelper
  (
   NodeTreeEntry entry,
   int indent,
   StringBuffer buf
  ) 
  {
    String istr = null;
    {
      StringBuffer ibuf = new StringBuffer();
      int wk;
      for(wk=0; wk<indent; wk++) 
	ibuf.append("  ");
      istr = ibuf.toString();
    }

    buf.append(istr + "[" + entry.getName() + "]\n");

    if(entry.isLeaf()) {
      buf.append(istr + "  CheckedIn = " + entry.isCheckedIn() + "\n");
      if(entry.hasWorking()) {
	buf.append(istr + "  Working =\n");
	for(String author : entry.getWorkingAuthors()) {
	  buf.append(istr + "    " + author + ": ");
	  for(String view : entry.getWorkingViews(author)) 
	    buf.append(view + " ");
	  buf.append("\n");
	}
      }

      {
	Set<String> keys = entry.getSequences();
	if(!keys.isEmpty()) {
	  buf.append(istr + "  Sequences =\n");
	  for(String key : keys)   
	    buf.append(istr + "    " + key + "\n");
	  buf.append("\n");
	}
      }	
    }
    else {
      for(NodeTreeEntry child : entry.values()) 
	logNodeTreeHelper(child, indent+1, buf);
    }
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
    NodeID id = new NodeID(targetID, name);
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

  /** 
   * Check the current node branch for circularity. 
   * 
   * @param name
   *   The name of the current node.
   * 
   * @param branch
   *   The names of the nodes from the root to this node.
   * 
   * @throws PipelineException 
   *   If a circularity is detected.
   */ 
  private void 
  checkBranchForCircularity
  (
   String name, 
   LinkedList<String> branch   
  ) 
    throws PipelineException
  {
    if(!branch.contains(name)) 
      return;

    StringBuffer buf = new StringBuffer();
    buf.append("Link circularity detected: \n" + 
	       "  ");
    boolean found = false;
    for(String bname : branch) {
      if(bname.equals(name)) 
	found = true;
      if(found) 
	buf.append(bname + " -> ");
    }
    buf.append(name);

    throw new PipelineException(buf.toString());
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Recursively perfrorm a node status based operation on the tree of nodes rooted at the 
   * given working version. <P> 
   * 
   * The <CODE>nodeOp</CODE> argument is performed on all upstream nodes and detailed
   * post-operation status information is generated for these nodes.  Only undetailed 
   * status information is computed for the downstream nodes.
   * 
   * @param nodeOp
   *   The node operation.
   * 
   * @param nodeID
   *   The unique working version identifier.
   * 
   * @param timer
   *   The shared task timer for this operation.
   * 
   * @return 
   *   The root of the node status tree corresponding to the given working version.
   * 
   * @throws PipelineException 
   *   If unable to perform the node operation.
   */ 
  private NodeStatus 
  performNodeOperation
  (
   NodeOp nodeOp, 
   NodeID nodeID,
   TaskTimer timer
  ) 
    throws PipelineException
  {
    NodeStatus root = null;
    {
      HashMap<String,NodeStatus> table = new HashMap<String,NodeStatus>();
      performUpstreamNodeOp(nodeOp, nodeID, new LinkedList<String>(), table, timer);
      root = table.get(nodeID.getName());
      assert(root != null);
    }

    {
      VersionID vid = null;
      if(root.getDetails().getWorkingVersion() == null) 
	vid = root.getDetails().getLatestVersion().getVersionID();
      
      HashMap<String,NodeStatus> table = new HashMap<String,NodeStatus>();
      getDownstreamNodeStatus(root, nodeID, vid, new LinkedList<String>(), table, timer);
    }

    return root;
  }

  /**
   * Recursively perfrorm a node state based operation on the upstream tree of nodes 
   * rooted at the given working version.
   * 
   * The node state is computed before applying the operation and may also be modified by 
   * the operation.  The <CODE>table</CODE> argument will contain the state of the nodes
   * after application of the operation when this method returns.  If a 
   * <CODE>PipelineException</CODE> is thrown, the contents of <CODE>table</CODE> should 
   * be ignored.
   * 
   * @param nodeOp
   *   The node operation.
   * 
   * @param nodeID
   *   The unique working version identifier.
   * 
   * @param branch
   *   The names of the nodes from the root to this node.
   * 
   * @param table
   *   The previously computed states indexed by node name.
   * 
   * @param timer
   *   The shared task timer for this operation.
   * 
   * @throws PipelineException 
   *   If unable to perform the node operation.
   */ 
  private void 
  performUpstreamNodeOp
  (
   NodeOp nodeOp,
   NodeID nodeID, 
   LinkedList<String> branch, 
   HashMap<String,NodeStatus> table, 
   TaskTimer timer
  ) 
    throws PipelineException
  {
    String name = nodeID.getName();

    /* check for circularity */ 
    checkBranchForCircularity(name, branch);

    /* skip nodes which have already been processed */ 
    if(table.containsKey(name)) 
      return;

    /* push the current node onto the end of the branch */ 
    branch.addLast(name);
    
    timer.aquire();
    ReentrantReadWriteLock workingLock = getWorkingLock(nodeID);
    workingLock.writeLock().lock();
    ReentrantReadWriteLock checkedInLock = getCheckedInLock(name);
    if(nodeOp.writesCheckedIn())
      checkedInLock.writeLock().lock();
    else 
      checkedInLock.readLock().lock();
    try {
      timer.resume();	

      /* lookup versions */ 
      WorkingBundle working = null;
      TreeMap<VersionID,CheckedInBundle> checkedIn = null;
      {
	try {
	  working = getWorkingBundle(nodeID);
	}
	catch(PipelineException ex) {
	}

	try {
	  checkedIn = getCheckedInBundles(name);
	}
	catch(PipelineException ex) {
	}

	if((working == null) && (checkedIn == null)) 
	  throw new PipelineException
	    ("No node named (" + name + ") exists!");
      }

      /* extract the working, base checked-in version and latest checked-in versions 
	   while computing the version state */ 
      NodeMod work       = null;
      NodeVersion base   = null;
      NodeVersion latest = null;
      ArrayList<VersionID> versionIDs = new ArrayList<VersionID>();
      VersionState versionState = null;
      {
	if(checkedIn != null) {
	  if(checkedIn.isEmpty())
	    throw new PipelineException
	      ("Somehow no checked-in versions of node (" + name + ") exist!"); 
	  CheckedInBundle bundle = checkedIn.get(checkedIn.lastKey());
	  latest = new NodeVersion(bundle.uVersion);

	  versionIDs.addAll(checkedIn.keySet());
	}

	if(working != null) {
	  work = new NodeMod(working.uVersion);

	  VersionID workID = work.getWorkingID();
	  if(workID != null) {
	    assert(checkedIn != null);
	    CheckedInBundle bundle = checkedIn.get(workID);
	    if(bundle == null) 
	      throw new PipelineException
		("Somehow the checked-in version (" + workID + ") of node (" + name + 
		 ") used as the basis for working version (" + nodeID + ") did " + 
		 "not exist!");
	    base = new NodeVersion(bundle.uVersion);

	    if(base.getVersionID().equals(latest.getVersionID())) 
	      versionState = VersionState.Identical;
	    else 
	      versionState = VersionState.NeedsCheckOut;
	  }
	  else {
	    assert(checkedIn == null);
	    versionState = VersionState.Pending;
	  }
	}
	else {
	  assert(checkedIn != null);
	  versionState = VersionState.CheckedIn;
	}
      }
      assert(versionState != null);

      /* compute property state */ 
      PropertyState propertyState = null;
      switch(versionState) {
      case Pending:
	propertyState = PropertyState.Pending;
	break;

      case CheckedIn:
	propertyState = PropertyState.CheckedIn;
	break;

      case Identical:
      case NeedsCheckOut:
	if(work.identicalProperties(latest)) {
	  propertyState = PropertyState.Identical;
	}
	else {
	  switch(versionState) {
	  case Identical:
	    propertyState = PropertyState.Modified;
	    break;

	  case NeedsCheckOut:
	    if(work.identicalProperties(base)) 
	      propertyState = PropertyState.NeedsCheckOut;
	    else 
	      propertyState = PropertyState.Conflicted;
	    break;

	  default:
	    assert(false);
	  }
	}
      }	

      /* compute link state */
      LinkState linkState = null;
      switch(versionState) {
      case Pending:
	linkState = LinkState.Pending;
	break;

      case CheckedIn:
	linkState = LinkState.CheckedIn;
	break;

      case Identical:
      case NeedsCheckOut:
	if(work.identicalLinks(latest)) {
	  linkState = LinkState.Identical;
	}
	else {
	  switch(versionState) {
	  case Identical:
	    linkState = LinkState.Modified;
	    break;

	  case NeedsCheckOut:
	    if(work.identicalLinks(base)) 
	      linkState = LinkState.NeedsCheckOut;
	    else 
	      linkState = LinkState.Conflicted;
	    break;

	  default:
	    assert(false);
	  }
	}
      }	

      /* add the status stub */ 
      NodeStatus status = new NodeStatus(nodeID);
      table.put(name, status);

      /* process the upstream nodes */ 
      switch(versionState) {
      case CheckedIn:
	for(LinkVersion link : latest.getSources()) {
	  NodeID lnodeID = new NodeID(nodeID, link.getName());
	  performUpstreamNodeOp(nodeOp, lnodeID, branch, table, timer);

	  NodeStatus lstatus = table.get(link.getName());
	  assert(lstatus != null);

	  status.addSource(lstatus);
	  lstatus.addTarget(status);
	}
	break;

      default:
	for(LinkMod link : work.getSources()) {
	  NodeID lnodeID = new NodeID(nodeID, link.getName());
	  performUpstreamNodeOp(nodeOp, lnodeID, branch, table, timer);

	  NodeStatus lstatus = table.get(link.getName());
	  assert(lstatus != null);

	  status.addSource(lstatus);
	  lstatus.addTarget(status);
	}
      }

      /* get per-file FileStates and oldest last modification timestamps */ 
      TreeMap<FileSeq, FileState[]> fileStates = new TreeMap<FileSeq, FileState[]>(); 
      boolean[] anyMissing = null;
      Date[] fileTimeStamps = null;
      switch(versionState) {
      case CheckedIn:
	for(FileSeq fseq : latest.getSequences()) {
	  FileState fs[] = new FileState[fseq.numFrames()];

	  int wk;
	  for(wk=0; wk<fs.length; wk++) 
	    fs[wk] = FileState.CheckedIn;

	  fileStates.put(fseq, fs);

	  if(anyMissing == null) 
	    anyMissing = new boolean[fs.length];

	  if(fileTimeStamps == null) 
	    fileTimeStamps = new Date[fs.length];
	}
	break;

      default:
	{
	  if((working.uFileStates == null) || (working.uFileTimeStamps == null)) {
	    VersionID vid = null;
	    if(latest != null) 
	      vid = latest.getVersionID();

	    TreeMap<FileSeq, Date[]> stamps = new TreeMap<FileSeq, Date[]>();

	    pFileMgrClient.states(nodeID, work, versionState, vid, fileStates, stamps);

	    for(FileSeq fseq : stamps.keySet()) {
	      Date[] ts = stamps.get(fseq);

	      if(fileTimeStamps == null) 
		fileTimeStamps = new Date[ts.length];

	      int wk;
	      for(wk=0; wk<ts.length; wk++) {
		if((fileTimeStamps[wk] == null) || 
		   ((ts[wk] != null) && (fileTimeStamps[wk].compareTo(ts[wk]) < 0)))
		  fileTimeStamps[wk] = ts[wk];
	      }
	    }	      

	    working.uFileStates     = fileStates;
	    working.uFileTimeStamps = fileTimeStamps;
	  }
	  else {
	    fileStates     = working.uFileStates;
	    fileTimeStamps = working.uFileTimeStamps;
	  }

	  /* precompute whether any files are missing */ 
	  for(FileSeq fseq : fileStates.keySet()) {
	    FileState fs[] = fileStates.get(fseq);
	    
	    if(anyMissing == null) 
	      anyMissing = new boolean[fs.length];

	    int wk;
	    for(wk=0; wk<anyMissing.length; wk++) {
	      if(fs[wk] == FileState.Missing) 
		anyMissing[wk] = true;
	    }
	  }
	}
      }

      /* compute overall node state */ 
      OverallNodeState overallNodeState = null;
      switch(versionState) {
      case Pending:
	{
	  overallNodeState = OverallNodeState.Pending;

	  /* check for missing files */ 
	  for(FileState fs[] : fileStates.values()) {
	    int wk;
	    for(wk=0; wk<fs.length; wk++) {
	      if(fs[wk] == FileState.Missing) {
		overallNodeState = OverallNodeState.Missing;
		break;
	      }
	    }
	  }
	}	      
	break;

      case CheckedIn:
	overallNodeState = OverallNodeState.CheckedIn;
	break;

      default:
	{
	  /* check file states */ 
	  boolean anyNeedsCheckOutFs = false;
	  boolean anyModifiedFs      = false;
	  boolean anyConflictedFs    = false;
	  boolean anyMissingFs       = false;
	  for(FileState fs[] : fileStates.values()) {
	    int wk;
	    for(wk=0; wk<fs.length; wk++) {
	      switch(fs[wk]) {
	      case NeedsCheckOut:
	      case Obsolete:
		anyNeedsCheckOutFs = true;
		break;

	      case Modified:
	      case Added:
		anyModifiedFs = true;
		break; 

	      case Conflicted:
		anyConflictedFs = true;	
		break;

	      case Missing: 
		anyMissingFs = true;
	      }
	    }
	  }

	  /* combine states */ 
	  boolean anyNeedsCheckOut = 
	    ((versionState == VersionState.NeedsCheckOut) || 
	     (propertyState == PropertyState.NeedsCheckOut) || 
	     (linkState == LinkState.NeedsCheckOut) || 
	     anyNeedsCheckOutFs);

	  boolean anyModified = 
	    ((propertyState == PropertyState.Modified) || 
	     (linkState == LinkState.Modified) || 
	     anyModifiedFs);

	  boolean anyConflicted = 
	    ((propertyState == PropertyState.Conflicted) || 
	     (linkState == LinkState.Conflicted) || 
	     anyConflictedFs);

	  if(anyMissingFs) 
	    overallNodeState = OverallNodeState.Missing;
	  else if(anyConflicted || (anyNeedsCheckOut && anyModified))
	    overallNodeState = OverallNodeState.Conflicted;
	  else if(anyModified) 
	    overallNodeState = OverallNodeState.Modified;
	  else if(anyNeedsCheckOut) 
	    overallNodeState = OverallNodeState.NeedsCheckOut;
	  else {
	    assert(versionState == VersionState.Identical);
	    assert(propertyState == PropertyState.Identical);
	    assert(linkState == LinkState.Identical);
	    assert(!anyNeedsCheckOutFs);
	    assert(!anyModifiedFs);
	    assert(!anyConflictedFs);

	    /* the work and base version have the same set of links 
		 because (linkState == Identical) */
	    for(LinkVersion link : base.getSources()) {
	      NodeDetails ldetails = table.get(link.getName()).getDetails();

	      switch(ldetails.getOverallNodeState()) {
	      case Modified:
	      case ModifiedLinks:
	      case Conflicted:	
		if(link.getPolicy() != LinkPolicy.None) 
		  overallNodeState = OverallNodeState.ModifiedLinks;
		break;
		
	      case Identical:
	      case NeedsCheckOut:
		if(!link.getVersionID().equals(ldetails.getWorkingVersion().getWorkingID()))
		  overallNodeState = OverallNodeState.ModifiedLinks;
		break;
		
	      default:
		assert(false) : 
		  ("Upstream Node Overall State = " + ldetails.getOverallNodeState());
	      }
	      
	      if(overallNodeState != null)
		break;
	    }

	    if(overallNodeState == null)
	      overallNodeState = OverallNodeState.Identical;
	  }
	}
      }

      /* determine per-file QueueStates */  
      QueueState queueStates[] = null;
      switch(versionState) {
      case CheckedIn:
	{
	  queueStates = new QueueState[latest.getPrimarySequence().numFrames()];
	  int wk;
	  for(wk=0; wk<queueStates.length; wk++) 
	    queueStates[wk] = QueueState.Undefined;
	}
	break;

      default:
	{	    
	  int numFrames = work.getPrimarySequence().numFrames();
	  queueStates = new QueueState[numFrames];

	  QueueState ps[] = null;
	  {
	    // "ps[]" should be computed by querying the queue here.  
	    // 
	    // The returned QueueState arrays will only contain: Queued, Running, Failed 
	    // or (null).  A (null) value means either that no queue job could be found
	    // which generates the file or the last job has completed successfully.
	    // 
	    // The following stub code therefore simple indicates that no jobs exist.
	    
	    ps = new QueueState[numFrames];
	  }
	  
	  int wk;
	  for(wk=0; wk<queueStates.length; wk++) {
	    /* no regeneration action, 
	         therefore QueueState is always Finished */ 
	    if(!work.hasAction()) {
	      queueStates[wk] = QueueState.Finished;
	    }

	    /* there IS a regeneration action */ 
	    else {
	      /* check for active jobs */ 
	      if(ps[wk] != null) {
		switch(ps[wk]) {
		case Queued:
		case Running:
		case Failed:
		  queueStates[wk] = ps[wk];
		  break;
		  
		default:
		  assert(false);
		}
	      }
	      
	      if(queueStates[wk] == null) {
		/* check for missing files or if the working version has been modified since 
		   any of the primary/secondary files were created */ 
		if(anyMissing[wk] ||
		   (fileTimeStamps[wk].compareTo(work.getLastCriticalModification()) < 0)) {
		  queueStates[wk] = QueueState.Stale;
		}
		
		/* check upstream per-file dependencies */ 
		else {
		  for(LinkMod link : work.getSources()) {
		    if(link.getPolicy() == LinkPolicy.Both) {
		      NodeStatus lstatus = status.getSource(link.getName());
		      NodeDetails ldetails = lstatus.getDetails();
		      
		      QueueState lqs[] = ldetails.getQueueState();
		      Date lstamps[] = ldetails.getFileTimeStamps();
		      
		      switch(link.getRelationship()) {
		      case OneToOne:
			{
			  Integer offset = link.getFrameOffset();
			  int idx = wk+offset;
			  if(((idx >= 0) || (idx < lqs.length)) &&
			     ((lqs[idx] != QueueState.Finished) ||
			      (lstamps[idx] == null) || 
			      (fileTimeStamps[wk].compareTo(lstamps[idx]) < 0))) 
			    queueStates[wk] = QueueState.Stale;
			}
			break;
			
		      case All:
			{
			  int fk;
			  for(fk=0; fk<lqs.length; fk++) {
			    if((lqs[fk] != QueueState.Finished) ||
			       (lstamps[fk] == null) || 
			       (fileTimeStamps[wk].compareTo(lstamps[fk]) < 0)) {
			      queueStates[wk] = QueueState.Stale;
			      break;
			    }
			  }
			}
		      }		    
		    }
		    
		    if(queueStates[wk] != null) 
		      break;
		  }
		  
		  if(queueStates[wk] == null) 
		    queueStates[wk] = QueueState.Finished;
		}
	      }
	    }
	  }
	}
      }

      /* compute overall queue state */ 
      OverallQueueState overallQueueState = OverallQueueState.Undefined; 
      if(versionState != VersionState.CheckedIn) {
	boolean anyStale = false;
	boolean anyQueued = false;
	boolean anyRunning = false; 
	boolean anyFailed = false;
	
	int wk;
	for(wk=0; wk<queueStates.length; wk++) {
	  switch(queueStates[wk]) {
	  case Stale:
	    anyStale = true;
	    break;
	    
	  case Queued:
	    anyQueued = true;
	    break;
	    
	  case Running:
	    anyRunning = true;
	    break;
	    
	  case Failed:
	    anyFailed = true;
	  }
	}
	
	if(anyFailed) 
	  overallQueueState = OverallQueueState.Failed;
	else if(anyRunning) 
	  overallQueueState = OverallQueueState.Running;
	else if(anyQueued) 
	  overallQueueState = OverallQueueState.Queued;
	else if(anyStale) 
	  overallQueueState = OverallQueueState.Stale;
	else 
	  overallQueueState = OverallQueueState.Finished;
      }

      /* create the node details */
      NodeDetails details = 
	new NodeDetails(name, 
			work, base, latest, versionIDs, 
			overallNodeState, overallQueueState, 
			versionState, propertyState, linkState, 
			fileStates, fileTimeStamps, queueStates);

      /* add the details to the node's status */ 
      status.setDetails(details);

      /* peform the node operation -- may alter the status and/or status details */ 
      nodeOp.perform(status, timer);
    }
    finally {
      if(nodeOp.writesCheckedIn())
	checkedInLock.writeLock().unlock();
      else 
	checkedInLock.readLock().unlock();
      workingLock.writeLock().unlock();
    }


    /* pop the current node off of the end of the branch */ 
    branch.removeLast();
  } 

  /**
   * Recursively compute the state of all nodes downstream of the given node. <P> 
   * 
   * If the <CODE>vid</CODE> argument is not <CODE>null</CODE>, then follow the downstream
   * links associated with the checked-in version with this revision number.
   * 
   * @param root
   *   The status of the root node of the tree.
   * 
   * @param nodeID
   *   The unique working version identifier.
   * 
   * @param vid 
   *   The revision number of the checked-in node version.
   * 
   * @param branch
   *   The names of the nodes from the root to this node.
   * 
   * @param table
   *   The previously computed states indexed by node name.
   * 
   * @param timer
   *   The shared task timer for this operation.
   */ 
  private void 
  getDownstreamNodeStatus
  (
   NodeStatus root, 
   NodeID nodeID, 
   VersionID vid, 
   LinkedList<String> branch, 
   HashMap<String,NodeStatus> table, 
   TaskTimer timer
  ) 
    throws PipelineException
  {
    String name = nodeID.getName();

    /* check for circularity */ 
    checkBranchForCircularity(name, branch);

    /* skip nodes which have already been processed */ 
    if(table.containsKey(name)) 
      return;

    /* push the current node onto the end of the branch */ 
    branch.addLast(name);


    timer.aquire();
    ReentrantReadWriteLock lock = getDownstreamLock(nodeID.getName());
    lock.readLock().lock();
    try {
      timer.resume();

      /* add the status stub */ 
      NodeStatus status = root; 
      if(!root.getNodeID().equals(nodeID)) {
	status = new NodeStatus(nodeID);
	table.put(name, status);
      }

      /* process downstream nodes */ 
      DownstreamLinks dsl = getDownstreamLinks(nodeID.getName()); 
      assert(dsl != null);

      TreeSet<String> wlinks = dsl.getWorking(nodeID);
      if((wlinks != null) && (!wlinks.isEmpty())) {
	for(String lname : wlinks) {
	  getDownstreamNodeStatus(root, new NodeID(nodeID, lname), null, 
				  branch, table, timer);

	  NodeStatus lstatus = table.get(lname);
	  assert(lstatus != null);

	  status.addTarget(lstatus);
	  lstatus.addSource(status);
	}
      }
      else {
	TreeMap<String,VersionID> clinks = null;
	if(vid != null)
	  clinks = dsl.getCheckedIn(vid);
	else 
	  clinks = dsl.getLatestCheckedIn();

	if((clinks != null) && (!clinks.isEmpty())) {
	  for(String lname : clinks.keySet()) {
	    VersionID lvid = clinks.get(lname);
	    
	    getDownstreamNodeStatus(root, new NodeID(nodeID, lname), lvid, 
				    branch, table, timer);
	    
	    NodeStatus lstatus = table.get(lname);
	    assert(lstatus != null);
	    
	    status.addTarget(lstatus);
	    lstatus.addSource(status);
	  }
	}
      }
    }
    finally {
      lock.readLock().unlock();
    }

    /* pop the current node off of the end of the branch */ 
    branch.removeLast();
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
    /* lookup the bundles */ 
    TreeMap<VersionID,CheckedInBundle> table = null;
    synchronized(pCheckedInBundles) {
      table = pCheckedInBundles.get(name);
    }

    if(table != null) 
      return table;

    /* read in the bundles from disk */ 
    table = readCheckedInVersions(name);
    if(table == null) 
      throw new PipelineException
	("No checked-in versions exist for node (" + name + ")!");

    synchronized(pCheckedInBundles) {
      pCheckedInBundles.put(name, table);
    }    

    return table;
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

  /**
   * Notify the directory monitoring facility that the given working version no longer 
   * needs to be monitored.  If there are no remaining working versions being monitored 
   * for the directory containing the files associated with the working version, then 
   * cease monitoring the directory.
   */
  private void
  unmonitor
  (
   NodeID id
  )
    throws PipelineException 
  { 
    synchronized(pMonitored) {
      File dir = id.getWorkingParent();

      HashSet<NodeID> ids = pMonitored.get(dir);
      if(ids != null) {
	ids.remove(id);
	if(ids.isEmpty()) 
	  pNotifyControlClient.unmonitor(dir);
      }
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   I / O   H E L P E R S                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the default toolset to disk. <P> 
   * 
   * @throws PipelineException
   *   If unable to write the default toolset file.
   */ 
  private void 
  writeDefaultToolset() 
    throws PipelineException
  {
    synchronized(pDefaultToolsetLock) {
      File file = new File(pNodeDir, "toolsets/default-toolset");
      if(file.exists()) {
	if(!file.delete())
	  throw new PipelineException
	    ("Unable to remove the old default toolset file (" + file + ")!");
      }

      if(pDefaultToolset != null) {
	Logs.ops.finer("Writing Default Toolset.");

	try {
	  String glue = null;
	  try {
	    GlueEncoder ge = new GlueEncoderImpl("DefaultToolset", pDefaultToolset);
	    glue = ge.getText();
	  }
	  catch(GlueException ex) {
	    Logs.glu.severe
	      ("Unable to generate a Glue format representation of the default toolset!");
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
	     "  While attempting to write the default toolset file (" + file + ")...\n" + 
	     "    " + ex.getMessage());
	}
      }
    }
  }
  
  /**
   * Read the default toolsets from disk.
   * 
   * @throws PipelineException
   *   If unable to read the default toolset file.
   */ 
  private void 
  readDefaultToolset()
    throws PipelineException
  {
    synchronized(pDefaultToolsetLock) {
      pDefaultToolset = null;

      File file = new File(pNodeDir, "toolsets/default-toolset");
      if(file.isFile()) {
	Logs.ops.finer("Reading Default Toolset.");

	try {
	  FileReader in = new FileReader(file);
	  GlueDecoder gd = new GlueDecoderImpl(in);
	  pDefaultToolset = (String) gd.getObject();
	  in.close();
	}
	catch(Exception ex) {
	  Logs.glu.severe
	    ("The default toolset file (" + file + ") appears to be corrupted!");
	  Logs.flush();
	  
	  throw new PipelineException
	    ("I/O ERROR: \n" + 
	     "  While attempting to read the default toolset file (" + file + ")...\n" + 
	   "    " + ex.getMessage());
	}
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the active toolset to disk. <P> 
   * 
   * @throws PipelineException
   *   If unable to write the active toolset file.
   */ 
  private void 
  writeActiveToolsets() 
    throws PipelineException
  {
    synchronized(pActiveToolsets) {
      File file = new File(pNodeDir, "toolsets/active-toolsets");
      if(file.exists()) {
	if(!file.delete())
	  throw new PipelineException
	    ("Unable to remove the old active toolsets file (" + file + ")!");
      }

      if(!pActiveToolsets.isEmpty()) {
	Logs.ops.finer("Writing Active Toolsets.");

	try {
	  String glue = null;
	  try {
	    GlueEncoder ge = new GlueEncoderImpl("ActiveToolsets", pActiveToolsets);
	    glue = ge.getText();
	  }
	  catch(GlueException ex) {
	    Logs.glu.severe
	      ("Unable to generate a Glue format representation of the active toolsets!");
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
	     "  While attempting to write the active toolsets file (" + file + ")...\n" + 
	     "    " + ex.getMessage());
	}
      }
    }
  }
  
  /**
   * Read the active toolsets from disk.
   * 
   * @throws PipelineException
   *   If unable to read the active toolset file.
   */ 
  private void 
  readActiveToolsets()
    throws PipelineException
  {
    synchronized(pActiveToolsets) {
      pActiveToolsets.clear();

      File file = new File(pNodeDir, "toolsets/active-toolsets");
      if(file.isFile()) {
	Logs.ops.finer("Reading Active Toolsets.");

	TreeSet<String> tsets = null;
	try {
	  FileReader in = new FileReader(file);
	  GlueDecoder gd = new GlueDecoderImpl(in);
	  tsets = (TreeSet<String>) gd.getObject();
	  in.close();
	}
	catch(Exception ex) {
	  Logs.glu.severe
	    ("The active toolsets file (" + file + ") appears to be corrupted!");
	  Logs.flush();
	  
	  throw new PipelineException
	    ("I/O ERROR: \n" + 
	     "  While attempting to read the active toolsets file (" + file + ")...\n" + 
	   "    " + ex.getMessage());
	}

	pActiveToolsets.addAll(tsets);
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the given toolset to disk. <P> 
   * 
   * @param tset
   *   The toolset.
   * 
   * @throws PipelineException
   *   If unable to write the toolset file.
   */ 
  private void 
  writeToolset
  (
   Toolset tset
  ) 
    throws PipelineException
  {
    synchronized(pToolsets) {
      File file = new File(pNodeDir, "toolsets/toolsets/" + tset.getName());
      if(file.exists()) {
	throw new PipelineException
	  ("Unable to overrite the existing toolset file(" + file + ")!");
      }

      Logs.ops.finer("Writing Toolset: " + tset.getName());

      try {
	String glue = null;
	try {
	  GlueEncoder ge = new GlueEncoderImpl("Toolset", tset);
	  glue = ge.getText();
	}
	catch(GlueException ex) {
	  Logs.glu.severe
	    ("Unable to generate a Glue format representation of the toolset " + 
	     "(" + tset.getName() + ")!");
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
	   "  While attempting to write the toolset file (" + file + ")...\n" + 
	   "    " + ex.getMessage());
      }
    }
  }
  
  /**
   * Read the toolset with the given name from disk. <P> 
   * 
   * @param name
   *   The toolset name.
   * 
   * @throws PipelineException
   *   If unable to read the toolset file.
   */ 
  private Toolset
  readToolset
  (
   String name
  )
    throws PipelineException
  {
    synchronized(pToolsets) {
      File file = new File(pNodeDir, "toolsets/toolsets/" + name);
      if(!file.isFile()) 
	throw new PipelineException
	  ("No toolset file exists for toolset (" + name + ")!");

      Logs.ops.finer("Reading Toolset: " + name);

      Toolset tset = null;
      try {
	FileReader in = new FileReader(file);
	GlueDecoder gd = new GlueDecoderImpl(in);
	tset = (Toolset) gd.getObject();
	in.close();
      }
      catch(Exception ex) {
	Logs.glu.severe
	  ("The toolset file (" + file + ") appears to be corrupted!");
	Logs.flush();
	
	throw new PipelineException
	  ("I/O ERROR: \n" + 
	   "  While attempting to read the toolset file (" + file + ")...\n" + 
	   "    " + ex.getMessage());
      }
      assert(tset != null);
      assert(tset.getName().equals(name));

      pToolsets.put(name, tset);      

      return tset;
    }
  }

  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the given toolset package to disk. <P> 
   * 
   * @param pkg
   *   The toolset package.
   * 
   * @throws PipelineException
   *   If unable to write the toolset package file.
   */ 
  private void 
  writeToolsetPackage
  (
   PackageVersion pkg
  ) 
    throws PipelineException
  {
    synchronized(pToolsetPackages) {
      File dir = new File(pNodeDir, "toolsets/packages/" + pkg.getName());
      synchronized(pMakeDirLock) {
	if(!dir.isDirectory()) 
	  if(!dir.mkdirs()) 
	    throw new PipelineException
	      ("Unable to create toolset package directory (" + dir + ")!");
      }

      File file = new File(dir, pkg.getVersionID().toString());
      if(file.exists()) {
	throw new PipelineException
	  ("Unable to overrite the existing toolset package file(" + file + ")!");
      }

      Logs.ops.finer("Writing Toolset Package: " + pkg.getName() + " v" + pkg.getVersionID());

      try {
	String glue = null;
	try {
	  GlueEncoder ge = new GlueEncoderImpl("ToolsetPackage", pkg);
	  glue = ge.getText();
	}
	catch(GlueException ex) {
	  Logs.glu.severe
	    ("Unable to generate a Glue format representation of the toolset package " + 
	     "(" + pkg.getName() + " v" + pkg.getVersionID() + ")!");
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
	   "  While attempting to write the toolset package file (" + file + ")...\n" + 
	   "    " + ex.getMessage());
      }
    }
  }
  
  /**
   * Read the toolset package with the given name and revision number from disk.
   * 
   * @param name
   *   The toolset package name.
   * 
   * @param vid
   *   The revision number.
   * 
   * @throws PipelineException
   *   If unable to read the toolset package file.
   */ 
  private PackageVersion
  readToolsetPackage
  (
   String name, 
   VersionID vid
  )
    throws PipelineException
  {
    synchronized(pToolsetPackages) {
      File file = new File(pNodeDir, "toolsets/packages/" + name + "/" + vid);
      if(!file.isFile()) 
	throw new PipelineException
	  ("No toolset package file exists for package (" + name + " v" + vid + ")!");

      Logs.ops.finer("Reading Toolset Package: " + name + " v" + vid);

      PackageVersion pkg = null;
      try {
	FileReader in = new FileReader(file);
	GlueDecoder gd = new GlueDecoderImpl(in);
	pkg = (PackageVersion) gd.getObject();
	in.close();
      }
      catch(Exception ex) {
	Logs.glu.severe
	  ("The toolset package file (" + file + ") appears to be corrupted!");
	Logs.flush();
	
	throw new PipelineException
	  ("I/O ERROR: \n" + 
	   "  While attempting to read the toolset package file (" + file + ")...\n" + 
	   "    " + ex.getMessage());
      }
      assert(pkg != null);
      assert(pkg.getName().equals(name));
      assert(pkg.getVersionID().equals(vid));

      TreeMap<VersionID,PackageVersion> versions = pToolsetPackages.get(name);
      if(versions == null) {
	versions = new TreeMap<VersionID,PackageVersion>();
	pToolsetPackages.put(name, versions);
      }

      versions.put(vid, pkg);

      return pkg;
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the suffix editor mappings for the given user.
   * 
   * @param author
   *   The owner of suffix/editor mappings.
   * 
   * @param editors
   *   The suffix editors.
   * 
   * @throws PipelineException
   *   If unable to write the suffix editor mappings file.
   */ 
  private void 
  writeSuffixEditors
  (
   String author, 
   TreeSet<SuffixEditor> editors
  ) 
    throws PipelineException
  {
    synchronized(pSuffixEditors) {
      File file = new File(pNodeDir, "etc/suffix-editors/" + author); 
      if(file.exists()) {
	if(!file.delete())
	  throw new PipelineException
	    ("Unable to remove the old suffix editors file (" + file + ")!");
      }

      Logs.ops.finer("Writing Suffix Editors: " + author);

      try {
	String glue = null;
	try {
	  GlueEncoder ge = new GlueEncoderImpl("SuffixEditors", editors);
	  glue = ge.getText();
	}
	catch(GlueException ex) {
	  Logs.glu.severe
	    ("Unable to generate a Glue format representation of the suffix editors " + 
	     "for user (" + author + ")!");
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
	   "  While attempting to write the suffix editors file (" + file + ")...\n" + 
	   "    " + ex.getMessage());
      }
    }
  }
  
  /**
   * Read the suffix editor mappings for the given user.
   * 
   * @param author
   *   The owner of suffix/editor mappings.
   * 
   * @throws PipelineException
   *   If unable to read the suffix editor mappings file.
   */ 
  private TreeMap<String,SuffixEditor>
  readSuffixEditors
  (
   String author
  ) 
    throws PipelineException
  {
    synchronized(pSuffixEditors) {
      File file = new File(pNodeDir, "etc/suffix-editors/" + author); 
      if(!file.isFile()) 
	return null;

      Logs.ops.finer("Reading Suffix Editors: " + author);

      TreeSet<SuffixEditor> editors = null;
      try {
	FileReader in = new FileReader(file);
	GlueDecoder gd = new GlueDecoderImpl(in);
	editors = (TreeSet<SuffixEditor>) gd.getObject();
	in.close();
      }
      catch(Exception ex) {
	Logs.glu.severe
	  ("The suffix editors file (" + file + ") appears to be corrupted!");
	Logs.flush();
	
	throw new PipelineException
	  ("I/O ERROR: \n" + 
	   "  While attempting to read the suffix editors file (" + file + ")...\n" + 
	   "    " + ex.getMessage());
      }
      assert(editors != null);

      TreeMap<String,SuffixEditor> table = new TreeMap<String,SuffixEditor>();
      for(SuffixEditor se : editors) 
	table.put(se.getSuffix(), se);

      pSuffixEditors.put(author, table);

      return table;
    }
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the privileged users to disk. <P> 
   * 
   * @throws PipelineException
   *   If unable to write the privileged users file.
   */ 
  private void 
  writePrivilegedUsers() 
    throws PipelineException
  {
    synchronized(pPrivilegedUsers) {
      File file = new File(pNodeDir, "etc/privileged-users");
      if(file.exists()) {
	if(!file.delete())
	  throw new PipelineException
	    ("Unable to remove the old privileged users file (" + file + ")!");
      }
      
      if(!pPrivilegedUsers.isEmpty()) {
	Logs.ops.finer("Writing Privileged Users.");

	try {
	  String glue = null;
	  try {
	    GlueEncoder ge = new GlueEncoderImpl("PrivilegedUsers", pPrivilegedUsers);
	    glue = ge.getText();
	  }
	  catch(GlueException ex) {
	    Logs.glu.severe
	      ("Unable to generate a Glue format representation of the privileged users!");
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
	     "  While attempting to write the privileged users file (" + file + ")...\n" + 
	     "    " + ex.getMessage());
	}
      }
    }
  }
  
  /**
   * Read the privileged users from disk. <P> 
   * 
   * @throws PipelineException
   *   If unable to read the privileged users file.
   */ 
  private void 
  readPrivilegedUsers() 
    throws PipelineException
  {
    synchronized(pPrivilegedUsers) {
      pPrivilegedUsers.clear();

      File file = new File(pNodeDir, "etc/privileged-users");
      if(file.isFile()) {
	Logs.ops.finer("Reading Privileged Users.");

	TreeSet<String> users = null;
	try {
	  FileReader in = new FileReader(file);
	  GlueDecoder gd = new GlueDecoderImpl(in);
	  users = (TreeSet<String>) gd.getObject();
	  in.close();
	}
	catch(Exception ex) {
	  Logs.glu.severe
	    ("The privileged users file (" + file + ") appears to be corrupted!");
	  Logs.flush();
	  
	  throw new PipelineException
	    ("I/O ERROR: \n" + 
	     "  While attempting to read the privileged users file (" + file + ")...\n" + 
	   "    " + ex.getMessage());
	}
	assert(users != null);
	
	pPrivilegedUsers.addAll(users);
      }
    }
  }




  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the checked-in version to disk. <P> 
   * 
   * This method assumes that the write lock for the table of checked-in versions for
   * the node already been aquired.
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
	GlueEncoder ge = new GlueEncoderImpl("NodeVersion", vsn);
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
   * Read all of the checked-in versions of a node from disk. <P> 
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
   *   The checked-in versions or <CODE>null</CODE> if no files exists.
   * 
   * @throws PipelineException
   *   If the checked-in version files are corrupted in some manner.
   */ 
  private TreeMap<VersionID,CheckedInBundle>
  readCheckedInVersions
  (
   String name
  ) 
    throws PipelineException
  {
    File dir = new File(pNodeDir, "repository" + name + "/");
    if(!dir.isDirectory()) 
      return null;

    Logs.ops.finer("Reading Checked-In Versions: " + name);

    TreeMap<VersionID,CheckedInBundle> table = new TreeMap<VersionID,CheckedInBundle>();
    File files[] = dir.listFiles();
    int wk;
    for(wk=0; wk<files.length; wk++) {
      if(!files[wk].isFile()) 
	throw new PipelineException
	  ("Somehow the node version file (" + files[wk] + ") was not a regular file!");

      NodeVersion vsn = null;
      try {
	FileReader in = new FileReader(files[wk]);
	GlueDecoder gd = new GlueDecoderImpl(in);
	vsn = (NodeVersion) gd.getObject();
	in.close();
      }
      catch(Exception ex) {
	Logs.glu.severe
	  ("The checked-in version file (" + files[wk] + ") appears to be corrupted!");
	Logs.flush();
	
	throw new PipelineException
	  ("I/O ERROR: \n" + 
	   "  While attempting to read checked-in version (" + files[wk].getName() + ") of " +
	   "node (" + name + ") from file...\n" +
	   "    " + ex.getMessage());
      }

      if(table.containsKey(vsn.getVersionID()))
	throw new PipelineException
	  ("Somehow the version (" + vsn.getVersionID() + ") of node (" + name + ") " + 
	   "was represented by more than one file!");
      
      table.put(vsn.getVersionID(), new CheckedInBundle(vsn));
    }

    return table;
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
	GlueEncoder ge = new GlueEncoderImpl("NodeMod", mod);
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
    File file   = new File(pNodeDir, id.getWorkingPath().getPath());
    File backup = new File(file + ".backup");
    
    try {
      if(file.exists()) {
	Logs.ops.finer("Reading Working Version: " + id);

	try {
	  FileReader in = new FileReader(file);
	  GlueDecoder gd = new GlueDecoderImpl(in);
	  NodeMod mod = (NodeMod) gd.getObject();
	  in.close();
	  
	  return mod;
	}
	catch(Exception ex) {
	  Logs.glu.severe
	    ("The working version file (" + file + ") appears to be corrupted!");
	  Logs.flush();
	  
	  if(backup.exists()) {
	    Logs.ops.finer("Reading Working Version (Backup): " + id);

	    NodeMod mod = null;
	    try {
	      FileReader in = new FileReader(backup);
	      GlueDecoder gd = new GlueDecoderImpl(in);
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
	GlueEncoder ge = new GlueEncoderImpl("DownstreamLinks", links);
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
    File file = new File(pNodeDir, "downstream/" + name);
    
    try {
      if(file.exists()) {
	Logs.ops.finer("Reading Downstream Links: " + name);

	try {
	  FileReader in = new FileReader(file);
	  GlueDecoder gd = new GlueDecoderImpl(in);
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
  /*   N O D E   O P   C L A S S E S                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The no-op base class of all node operations.
   */
  private
  class NodeOp
  {  
    /** 
     * Construct a new operation.
     */
    public
    NodeOp()
    {}
    
    /**
     * Perform the status operation on the given node.
     * 
     * @param status 
     *   The pre-operation status of the node. 
     * 
     * @param timer
     *   The shared task timer for this operation.
     * 
     * @throws PipelineException 
     *   If unable to perform the operation.
     */ 
    public void 
    perform
    (
     NodeStatus status, 
     TaskTimer timer
    )
      throws PipelineException
    {}

    /**
     * Does this operation modify the checked-in versions of the node?
     */ 
    public boolean
    writesCheckedIn()
    {
      return false;
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * The node check-in operation. <P>
   */
  public 
  class NodeCheckInOp
    extends NodeOp
  {  
    /** 
     * Construct a new check-in operation.
     */
    public
    NodeCheckInOp
    ( 
     NodeCheckInReq req
    ) 
    {
      super();
      
      pRequest = req;
    }


    /**
     * Perform the check-in operation on the given node.
     * 
     * @param status 
     *   The pre-operation status of the node. 
     * 
     * @param timer
     *   The shared task timer for this operation.
     * 
     * @throws PipelineException 
     *   If unable to perform the operation.
     */ 
    public void 
    perform
    (
     NodeStatus status, 
     TaskTimer timer
    )
      throws PipelineException
    {
      String name = status.getName();
      NodeDetails details = status.getDetails();
      assert(details != null);

      /* make sure node is in a Finished state */ 
      if(details.getOverallQueueState() != OverallQueueState.Finished) {
	throw new PipelineException
	  ("The node (" + name + ") was in a " + details.getOverallQueueState() + 
	   " (" + NodeStyles.getQueueColorString(details.getOverallQueueState()) + ") " + 
	   "state.\n\n" +
	   "All nodes being Checked-In must be in a Finished (" + 
	   NodeStyles.getQueueColorString(OverallQueueState.Finished) + ") state.");
      }

      /* process the node */ 
      switch(details.getOverallNodeState()) {
      case Identical:
      case NeedsCheckOut:
	break;

      case CheckedIn:
	throw new PipelineException
	  ("No working version of node (" + name + ") exists to be checked-in.");

      case Conflicted:
	throw new PipelineException
	  ("The working version of node (" + name + ") was in a Conflicted state!\n\n" + 
	   "The conflicts must be resolved before this node or any downstream nodes with " + 
	   "which this node is linked can be checked-in.");

      case Pending:
      case Modified:
      case ModifiedLinks:
	{	
	  NodeID nodeID = status.getNodeID();

	  /* lookup bundles and determine the new revision number */ 
	  VersionID vid = null;
	  VersionID latestID = null;
	  TreeMap<VersionID,CheckedInBundle> checkedIn = null;
	  if(details.getOverallNodeState() == OverallNodeState.Pending) {
	    checkedIn = new TreeMap<VersionID,CheckedInBundle>();
	    vid = new VersionID();
	  }
	  else {
	    checkedIn = getCheckedInBundles(name);
	    latestID = checkedIn.lastKey();
	    vid = new VersionID(latestID, pRequest.getLevel());
	  }

	  WorkingBundle working = getWorkingBundle(nodeID);
	  NodeMod work = working.uVersion;

	  /* determine the checked-in revision numbers of the upstream nodes */ 
	  TreeMap<String,VersionID> lvids = new TreeMap<String,VersionID>();
	  for(NodeStatus lstatus : status.getSources()) {
	    VersionID lvid = lstatus.getDetails().getBaseVersion().getVersionID();
	    lvids.put(lstatus.getName(), lvid);
	  }
	  
	  /* build the file novelty table */ 
	  TreeMap<FileSeq,boolean[]> isNovel = new TreeMap<FileSeq,boolean[]>();
	  for(FileSeq fseq : working.uFileStates.keySet()) {
	    FileState[] states = working.uFileStates.get(fseq);
	    boolean flags[] = new boolean[states.length];

	    int wk;
	    for(wk=0; wk<states.length; wk++) {
	      switch(states[wk]) {
	      case Pending:
	      case Modified:
	      case Added:
		flags[wk] = true;
		break;

	      case Identical:
		flags[wk] = false;
		break;

	      default:
		throw new PipelineException
		  ("Somehow the working file (" + fseq.getFile(wk) + ") with a file state " + 
		   "of (" + states[wk].name() + ") was erroneously submitted for check-in!");
	      }
	    }

	    isNovel.put(fseq, flags);
	  }

	  /* check-in the files */ 
	  pFileMgrClient.checkIn(nodeID, work, vid, latestID, isNovel);

	  /* create a new checked-in version and write it disk */ 
	  NodeVersion vsn = 
	    new NodeVersion(work, vid, lvids, isNovel, 
			    pRequest.getNodeID().getAuthor(), pRequest.getMessage());

	  writeCheckedInVersion(vsn);

	  /* add the new version to the checked-in bundles */ 
	  if(details.getOverallNodeState() == OverallNodeState.Pending) {
	    synchronized(pCheckedInBundles) {
	      pCheckedInBundles.put(name, checkedIn);
	    }
	  }
	  checkedIn.put(vid, new CheckedInBundle(vsn));
	  
	  /* generate new file/queue states */ 
	  TreeMap<FileSeq,FileState[]> fileStates = new TreeMap<FileSeq,FileState[]>();
	  QueueState[] queueStates = null;
	  {
	    for(FileSeq fseq : working.uFileStates.keySet()) {
	      FileState fs[] = new FileState[fseq.numFrames()];
	      Date stamps[] = new Date[fseq.numFrames()];

	      if(queueStates == null) 
		queueStates = new QueueState[fs.length];
	      
	      int wk;
	      for(wk=0; wk<fs.length; wk++) 
		fs[wk] = FileState.Identical;
	      
	      fileStates.put(fseq, fs);
	    }

	    {
	      int wk;
	      for(wk=0; wk<queueStates.length; wk++) 
		queueStates[wk] = QueueState.Finished;
	    }
	  }
	  
	  /* create a new working version and write it to disk */ 
	  NodeMod nwork = new NodeMod(vsn, work.getLastCriticalModification());
	  writeWorkingVersion(nodeID, nwork);

	  /* update the working bundle */ 
	  working.uVersion    = nwork;
	  working.uFileStates = fileStates;

	  /* update the node status details */ 
	  NodeDetails ndetails = 
	    new NodeDetails(name, 
			    nwork, vsn, checkedIn.get(checkedIn.lastKey()).uVersion,
			    checkedIn.keySet(), 
			    OverallNodeState.Identical, OverallQueueState.Finished, 
			    VersionState.Identical, PropertyState.Identical, 
			    LinkState.Identical, 
			    fileStates, working.uFileTimeStamps, queueStates);

	  status.setDetails(ndetails);

	  /* update the node tree entry */ 
	  addCheckedInNodeTreePath(vsn);

	  /* initialize the downstream links for the new checked-in version of this node */ 
	  {
	    timer.aquire();
	    ReentrantReadWriteLock downstreamLock = getDownstreamLock(name);
	    downstreamLock.writeLock().lock();
	    try {
	      timer.resume();
	      
	      DownstreamLinks dsl = getDownstreamLinks(name);
	      dsl.createCheckedIn(vsn.getVersionID());
	    }
	    finally {
	      downstreamLock.writeLock().unlock();
	    }    
	  }

	  /* set the checked-in downstream links from the upstream nodes to this node */ 
	  for(LinkVersion link : vsn.getSources()) { 
	    String lname = link.getName();

	    timer.aquire();
	    ReentrantReadWriteLock downstreamLock = getDownstreamLock(lname);
	    downstreamLock.writeLock().lock();
	    try {
	      timer.resume();

	      DownstreamLinks dsl = getDownstreamLinks(lname);
	      dsl.addCheckedIn(link.getVersionID(), name, vsn.getVersionID());
	    }  
	    finally {
	      downstreamLock.writeLock().unlock();
	    }     
	  }
	}
      }
    }

    /**
     * Does this operation modify the checked-in versions of the node?
     */ 
    public boolean
    writesCheckedIn()
    {
      return true;
    }


    /**
     * The node check-in request.
     */ 
    private NodeCheckInReq  pRequest;
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

    
    /**
     * A table containing the relationship between individual files associated with the 
     * working and checked-in versions of this node indexed by working file sequence. <P> 
     * 
     * May be <CODE>null</CODE> if invalidated. If the entry for a file sequence is missing
     * from this table, then the <CODE>VersionState</CODE> was <CODE>CheckedIn</CODE> and 
     * no working files existed.
     */
    public TreeMap<FileSeq,FileState[]>  uFileStates;

    /**
     * A table containing the oldest last modification timestamp for each primary/secondary
     * file index. 
     * 
     * May be <CODE>null</CODE> if invalidated.  If an individual file timestamp is 
     * <CODE>null</CODE>, then the <CODE>FileState</CODE> for all of the primary/secondary 
     * files were <CODE>Missing</CODE> when the timestamp was being collected.
     */
    public Date[]  uFileTimeStamps;
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
      uVersion = vsn;
    }

    /**
     * The checked-in version of a node.
     */ 
    public NodeVersion  uVersion;

    
    // Add Task related stuff here later... 

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
      super("MasterMgr:DirtyTask");
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
	    TaskTimer timer = new TaskTimer("File State Invalidated: " + id);
	    timer.aquire();
	    ReentrantReadWriteLock lock = getWorkingLock(id);
	    lock.writeLock().lock();
	    try {
	      timer.resume();	
	      
	      WorkingBundle bundle = getWorkingBundle(id);
	      bundle.uFileStates      = null;
	      bundle.uFileTimeStamps  = null;
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


  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the default toolset.<P> <P> 
   * 
   * Access to the <CODE>pDefaultToolset</CODE> field should be protected by a synchronized 
   * block on the <CODE>pDefaultToolsetLock</CODE> field.
   */ 
  private Object pDefaultToolsetLock;
  private String pDefaultToolset;

  /**
   * The names of the active toolsets. <P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */ 
  private TreeSet<String> pActiveToolsets; 

  /**
   * The cached table of all toolsets indexed by toolset name. <P> 
   * 
   * All existing toolsets will have a key in this table, but the value may be 
   * null if the toolset is not currently cached.<P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */
  private TreeMap<String,Toolset>  pToolsets;

  /**
   * The cached table of all toolset packages indexed by package name and revision number. <P>
   * 
   * All existing package names and revision numbers will be included as keys in this table,
   * but the package value may be null if the package is not currently cached.<P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */ 
  private TreeMap<String,TreeMap<VersionID,PackageVersion>>  pToolsetPackages;


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The cached table of filename suffix to editor mappings indexed by author user name. <P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */ 
  private TreeMap<String,TreeMap<String,SuffixEditor>>  pSuffixEditors;



  /*----------------------------------------------------------------------------------------*/

  /**
   * The cached names of the privileged users. <P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */ 
  private TreeSet<String>  pPrivilegedUsers;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The table of working area view names indexed by author user name. <P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */ 
  private TreeMap<String,TreeSet<String>>  pWorkingAreaViews;

  /**
   * The root of the tree of node path components currently in use.
   * 
   * Access to this field should be protected by a synchronized block.
   */ 
  private NodeTreeEntry  pNodeTreeRoot;

  
  /*----------------------------------------------------------------------------------------*/

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
  
  
  /*----------------------------------------------------------------------------------------*/

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

