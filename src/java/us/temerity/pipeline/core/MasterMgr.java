// $Id: MasterMgr.java,v 1.111 2005/04/03 07:04:36 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.message.*;
import us.temerity.pipeline.toolset.*;
import us.temerity.pipeline.ui.core.NodeStyles;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.util.concurrent.locks.*;
import java.util.concurrent.atomic.*;
import java.text.*;

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
 */
class MasterMgr
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new node manager.
   * 
   * @throws PipelineException 
   *   If unable to properly initialize the manager.
   */
  public
  MasterMgr()
    throws PipelineException 
  { 
    pShutdownJobMgrs   = new AtomicBoolean(false);
    pShutdownPluginMgr = new AtomicBoolean(false);

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Info,
       "Establishing Network Connections...");
    LogMgr.getInstance().flush();

    {
      /* initialize the plugins */ 
      PluginMgrClient.init();

      /* make a connection to the file manager */ 
      {
	pFileMgrClients = new Stack<FileMgrClient>();
	FileMgrNetClient client = new FileMgrNetClient();
	client.waitForConnection(1000, 5000);
	freeFileMgrClient(client);
      }
      
      /* make a connection to the queue manager */ 
      pQueueMgrClient = new QueueMgrControlClient();
      pQueueMgrClient.waitForConnection(1000, 5000);
    }

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Info,
       "Initializing...");
    LogMgr.getInstance().flush();

    /* create the lock file */ 
    {
      File file = new File(PackageInfo.sNodeDir, "lock");
      if(file.exists()) 
	throw new PipelineException 
	  ("Another node manager is already running!\n" + 
	   "If you are certain this is not the case, remove the lock file (" + file + ")!");

      try {
	FileWriter out = new FileWriter(file);
	out.close();
      }
      catch(IOException ex) {
	throw new PipelineException 
	  ("Unable to create lock file (" + file + ")!");
      }
    }

    /* startup initialization */ 
    init();
   }


  /*-- CONSTRUCTION HELPERS ----------------------------------------------------------------*/

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
  init()
  { 
    /* initialize the fields */ 
    {
      pDatabaseLock = new ReentrantReadWriteLock();
      pMakeDirLock  = new Object();

      pArchiveFileLock = new Object();
      pArchivedIn      = new TreeMap<String,TreeMap<VersionID,TreeSet<String>>>();
      pArchivedOn      = new TreeMap<String,Date>();
      pRestoredOn      = new TreeMap<String,TreeSet<Date>>();
      pOfflined        = new TreeMap<String,TreeSet<VersionID>>();
      pRestoreReqs     = new TreeMap<String,TreeMap<VersionID,RestoreRequest>>();

      pDefaultToolsetLock = new Object();
      pDefaultToolset     = null;
      pActiveToolsets     = new TreeSet<String>();
      pToolsets           = new TreeMap<String,Toolset>();
      pToolsetPackages    = new TreeMap<String,TreeMap<VersionID,PackageVersion>>();

      pSuffixEditors = new TreeMap<String,TreeMap<String,SuffixEditor>>();

      pPluginMenuLayoutLock = new Object();
      pEditorMenuLayout     = new PluginMenuLayout();
      pComparatorMenuLayout = new PluginMenuLayout();
      pToolMenuLayout       = new PluginMenuLayout();

      pPrivilegedUsers = new TreeSet<String>();

      pWorkingAreaViews = new TreeMap<String,TreeSet<String>>();
      pNodeTreeRoot     = new NodeTreeEntry();

      pCheckedInLocks   = new HashMap<String,ReentrantReadWriteLock>();
      pCheckedInBundles = new HashMap<String,TreeMap<VersionID,CheckedInBundle>>();

      pWorkingLocks   = new HashMap<NodeID,ReentrantReadWriteLock>();
      pWorkingBundles = new HashMap<String,HashMap<NodeID,WorkingBundle>>();       

      pDownstreamLocks = new HashMap<String,ReentrantReadWriteLock>();
      pDownstream      = new HashMap<String,DownstreamLinks>();

      pQueueSubmitLock = new Object();
    }

    /* perform startup I/O operations */ 
    try {
      makeRootDirs();
      initArchives();
      initToolsets();
      initPluginMenuLayouts();
      initPrivilegedUsers();
      rebuildDownstreamLinks();
      initNodeTree();
      readNextIDs();
    }
    catch(Exception ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 ex.getMessage());
      LogMgr.getInstance().flush();

      System.exit(1);
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Make sure that the root node directories exist.
   */ 
  private void 
  makeRootDirs() 
    throws PipelineException
  {
    if(!PackageInfo.sNodeDir.isDirectory()) 
      throw new PipelineException
	("The root node directory (" + PackageInfo.sNodeDir + ") does not exist!");
    
    ArrayList<File> dirs = new ArrayList<File>();
    dirs.add(new File(PackageInfo.sNodeDir, "repository"));
    dirs.add(new File(PackageInfo.sNodeDir, "working"));
    dirs.add(new File(PackageInfo.sNodeDir, "toolsets/packages"));
    dirs.add(new File(PackageInfo.sNodeDir, "toolsets/toolsets"));
    dirs.add(new File(PackageInfo.sNodeDir, "etc"));
    dirs.add(new File(PackageInfo.sNodeDir, "etc/suffix-editors"));
    dirs.add(new File(PackageInfo.sNodeDir, "archives/manifests"));
    dirs.add(new File(PackageInfo.sNodeDir, "archives/output/archive"));
    dirs.add(new File(PackageInfo.sNodeDir, "archives/output/restore"));

    synchronized(pMakeDirLock) {
      for(File dir : dirs) {
	if(!dir.isDirectory())
	  if(!dir.mkdirs()) 
	    throw new PipelineException
	      ("Unable to create the directory (" + dir + ")!");
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Load the archives.
   */ 
  private void 
  initArchives()
    throws PipelineException
  {
    /* scan archive volume GLUE files */ 
    {
      File dir = new File(PackageInfo.sNodeDir, "archives/manifests");
      File files[] = dir.listFiles(); 
      int wk;
      for(wk=0; wk<files.length; wk++) {
	ArchiveVolume archive = readArchive(files[wk].getName());
	
	for(String name : archive.getNames()) {
	  TreeMap<VersionID,TreeSet<String>> versions = pArchivedIn.get(name);
	  if(versions == null) {
	    versions = new TreeMap<VersionID,TreeSet<String>>();
	    pArchivedIn.put(name, versions);
	  }
	  
	  for(VersionID vid : archive.getVersionIDs(name)) { 
	    TreeSet<String> anames = versions.get(vid);
	    if(anames == null) {
	      anames = new TreeSet<String>();
	      versions.put(vid, anames);
	    }
	    
	    anames.add(archive.getName());
	  }
	}
	
	pArchivedOn.put(archive.getName(), archive.getTimeStamp());
      }
    }

    /* scan restore output files */ 
    {
      File dir = new File(PackageInfo.sNodeDir, "archives/output/restore");
      File files[] = dir.listFiles(); 
      int wk;
      for(wk=0; wk<files.length; wk++) {
	String fname = files[wk].getName();
	for(String aname : pArchivedOn.keySet()) {
	  if(fname.startsWith(aname)) {
	    try {
	      Date stamp = new Date(Long.parseLong(fname.substring(aname.length()+1)));
	      TreeSet<Date> stamps = pRestoredOn.get(aname);
	      if(stamps == null) {
		stamps = new TreeSet<Date>();
		pRestoredOn.put(aname, stamps);
	      }
	      stamps.add(stamp);
	    }
	    catch(NumberFormatException ex) {
	    }
	    break;
	  }
	}
      }    
    }

    {
      FileMgrClient fclient = getFileMgrClient();
      try {
	pOfflined = fclient.getOfflined();
      }
      finally {
	freeFileMgrClient(fclient);
      }
    }

    readRestoreReqs();
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Load the toolset and toolset package indices.
   */ 
  private void 
  initToolsets()
    throws PipelineException
  {
    readDefaultToolset();
    readActiveToolsets();

    /* initialize toolset keys */ 
    {
      File dir = new File(PackageInfo.sNodeDir, "toolsets/toolsets");
      File files[] = dir.listFiles(); 
      int wk;
      for(wk=0; wk<files.length; wk++) {
	if(files[wk].isFile()) 
	  pToolsets.put(files[wk].getName(), null);
      }
    }
    
    /* initialize package keys */ 
    {
      File dir = new File(PackageInfo.sNodeDir, "toolsets/packages");
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


  /*----------------------------------------------------------------------------------------*/

  /**
   * Load the plugin menu layouts.
   */ 
  private void 
  initPluginMenuLayouts()
    throws PipelineException
  {
    readEditorMenuLayout(); 
    readComparatorMenuLayout(); 
    readToolMenuLayout(); 
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Load the privileged users if any exist.
   */ 
  private void 
  initPrivilegedUsers()
    throws PipelineException
  {
    readPrivilegedUsers();	 
    pQueueMgrClient.setPrivilegedUsers(pPrivilegedUsers);
  }
  


  /*----------------------------------------------------------------------------------------*/

  /**
   * Build the initial node name tree by searching the file system for node related files.
   */
  private void 
  initNodeTree()
    throws PipelineException 
  {
    {
      File dir = new File(PackageInfo.sNodeDir, "repository");
      initCheckedInNodeTree(dir.getPath(), dir); 
    }

    {
      File dir = new File(PackageInfo.sNodeDir, "working");
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
    throws PipelineException
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
	TreeMap<VersionID,CheckedInBundle> table = readCheckedInVersions(path);
	for(CheckedInBundle bundle : table.values()) 
	  addCheckedInNodeTreePath(bundle.uVersion);
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
    throws PipelineException 
  {
    File files[] = dir.listFiles(); 
    int wk;
    for(wk=0; wk<files.length; wk++) {
      if(files[wk].isDirectory()) 
	initWorkingNodeTree(prefix, author, view, files[wk]);
      else {
	String path = files[wk].getPath();
	if(!path.endsWith(".backup")) {
	  NodeID nodeID = new NodeID(author, view, path.substring(prefix.length()));
	  NodeMod mod = readWorkingVersion(nodeID);
	  addWorkingNodeTreePath(nodeID, mod.getSequences());
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
    throws PipelineException 
  {
    {
      File dir = new File(PackageInfo.sNodeDir, "downstream");
      if(dir.isDirectory()) 
	return;

      if(!dir.mkdir()) 
	throw new IllegalArgumentException
	  ("Unable to create the directory (" + dir + ")!");
    }

    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Fine,
       "Rebuilding Downstream Links Cache...");   

    /* process checked-in versions */ 
    {
      File dir = new File(PackageInfo.sNodeDir, "repository");
      collectCheckedInDownstreamLinks(dir.getPath(), dir); 
    }

    /* process working versions */ 
    {
      File dir = new File(PackageInfo.sNodeDir, "working");
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

    if(!pDownstream.isEmpty() && 
       LogMgr.getInstance().isLoggable(LogMgr.Kind.Ops, LogMgr.Level.Finer)) { 
      StringBuffer buf = new StringBuffer(); 
      buf.append("Rebuilt Links:\n");
      for(String name : pDownstream.keySet()) 
	buf.append("  " + name + "\n");
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Finer,
	 buf.toString());
    }

    /* shutdown and restart the server */ 
    {
      /* write cached downstream links */ 
      writeAllDownstreamLinks();
      
      /* invalidate the fields */ 
      {
	pDatabaseLock = null;
	pMakeDirLock  = null;

	pArchiveFileLock = null;
	pArchivedIn      = null;
	pArchivedOn      = null;
	pOfflined        = null;
	pRestoreReqs     = null;

	pDefaultToolsetLock = null;
	pDefaultToolset     = null;
	pActiveToolsets     = null;
	pToolsets           = null;
	pToolsetPackages    = null;
	
	pSuffixEditors = null;

	pPluginMenuLayoutLock = null; 
	pEditorMenuLayout     = null;
	pComparatorMenuLayout = null;
	pToolMenuLayout       = null;

	pPrivilegedUsers = null;
	
	pNodeTreeRoot     = null;
	pWorkingAreaViews = null;

	pCheckedInLocks   = null;
	pCheckedInBundles = null;
	
	pWorkingLocks   = null;
	pWorkingBundles = null;
	
	pDownstreamLocks = null;
	pDownstream      = null;

	pQueueSubmitLock = null; 
      }
      
      /* reinitialize */ 
      init();
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
    throws PipelineException 
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
      
      TreeMap<VersionID,CheckedInBundle> table = readCheckedInVersions(name);
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
    throws PipelineException 
  {
    File files[] = dir.listFiles(); 
    int wk;
    for(wk=0; wk<files.length; wk++) {
      if(files[wk].isDirectory()) 
	collectWorkingDownstreamLinks(author, view, prefix, files[wk]);
      else {
	String path = files[wk].getPath();
	if(!path.endsWith(".backup")) {
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
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S H U T D O W N                                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the shutdown options.
   * 
   * @param shutdownJobMgrs
   *   Whether to command the queue manager to shutdown all job servers before exiting.
   * 
   * @param shutdownPluginMgr
   *   Whether to shutdown the plugin manager before exiting.
   */ 
  public void 
  setShutdownOptions
  (
   boolean shutdownJobMgrs, 
   boolean shutdownPluginMgr
  ) 
  {
    pShutdownJobMgrs.set(shutdownJobMgrs);
    pShutdownPluginMgr.set(shutdownPluginMgr);
  }

  /**
   * Shutdown the node manager. <P> 
   * 
   * Also sends a shutdown request to the <B>plfilemgr</B>(1) and <B>plqueuemgr</B>(1) 
   * daemons if there are live network connections to these daemons. <P> 
   * 
   * It is crucial that this method be called when only a single thread is able to access
   * this instance!  In other words, after all request threads have already exited or by a 
   * restart during the construction of this instance.
   */
  public void  
  shutdown() 
  {
    /* remove the lock file */ 
    {
      File file = new File(PackageInfo.sNodeDir, "lock");
      file.delete();
    }

    /* close the connection to the file manager */ 
    if(pFileMgrClients != null) {
      while(!pFileMgrClients.isEmpty()) {
	FileMgrClient client = pFileMgrClients.pop();
	try {
	  client.shutdown();
	}
	catch(PipelineException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Net, LogMgr.Level.Warning,
	     ex.getMessage());
	  LogMgr.getInstance().flush();
	}
      }
    }

    /* close the connection to the queue manager */ 
    if(pQueueMgrClient != null) {
      try {
	if(pShutdownJobMgrs.get()) 
	  pQueueMgrClient.shutdown(true);
	else 
	  pQueueMgrClient.shutdown();
      }
      catch(PipelineException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Warning,
	   ex.getMessage());
	LogMgr.getInstance().flush();
      }
    }
    
    /* close the connection to the plugin manager */ 
    try {
      if(pShutdownPluginMgr.get()) 
	PluginMgrClient.getInstance().shutdown();
      else 
	PluginMgrClient.getInstance().disconnect();
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Warning,
	 ex.getMessage());
      LogMgr.getInstance().flush();
    }

    /* give the sockets time to disconnect cleanly */ 
    try {
      Thread.sleep(500);
    }
    catch(InterruptedException ex) {
    }


    /* write cached downstream links */ 
    writeAllDownstreamLinks();

    /* write the job/group ID files */ 
    try {
      writeNextIDs();
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Warning,
	 ex.getMessage());
      LogMgr.getInstance().flush();
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
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 ex.getMessage());
      
      /* remove the entire downstream directory */ 
      {
	Map<String,String> env = System.getenv();
	
	ArrayList<String> args = new ArrayList<String>();
	args.add("--force");
	args.add("--recursive");
	args.add("downstream");
	
	SubProcessLight proc = 
	  new SubProcessLight("RemoveDownstreamLinks", "rm", args, env, PackageInfo.sNodeDir);
	try {
	  proc.start();
	  proc.join();
	}
	catch(InterruptedException ex2) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Ops, LogMgr.Level.Severe,
	     "Interrupted while removing the downstream directory " + 
	     "(" + PackageInfo.sNodeDir + "/downstream)!");
	}
	
	if(!proc.wasSuccessful()) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Ops, LogMgr.Level.Severe,
	     "Unable to removing the downstream directory " + 
	     "(" + PackageInfo.sNodeDir + "/downstream)!");
	}
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   T O O L S E T S                                                                      */
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
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pDefaultToolsetLock) {
	timer.resume();	
	
	if(pDefaultToolset != null) 
	  return new MiscGetDefaultToolsetNameRsp(timer, pDefaultToolset);
	else 
	  return new FailureRsp(timer, "No default toolset is defined!");
      }
    }
    finally {
      pDatabaseLock.readLock().unlock();
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
    pDatabaseLock.readLock().lock();
    try {
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
    finally {
      pDatabaseLock.readLock().unlock();
    }
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
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pActiveToolsets) {
	timer.resume();
	
	return new MiscGetActiveToolsetNamesRsp(timer, new TreeSet<String>(pActiveToolsets));
      }
    }
    finally {
      pDatabaseLock.readLock().unlock();
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
    pDatabaseLock.readLock().lock();
    try {
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
	    
	    File file = new File(PackageInfo.sNodeDir, "toolsets/default-toolset");
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
    finally {
      pDatabaseLock.readLock().unlock();
    }
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
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pToolsets) {
	timer.resume();
	
	return new MiscGetToolsetNamesRsp(timer, new TreeSet<String>(pToolsets.keySet()));
      }
    }
    finally {
      pDatabaseLock.readLock().unlock();
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
    pDatabaseLock.readLock().lock();
    try {
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
    finally {
      pDatabaseLock.readLock().unlock();
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
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();	

      TreeMap<String,String> env = 
	getToolsetEnvironment(req.getAuthor(), req.getView(), req.getName(), timer);	
      
      return new MiscGetToolsetEnvironmentRsp(timer, req.getName(), env);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }

  /**
   * Get the cooked toolset environment specific to the given user and working area. <P> 
   * 
   * If the <CODE>author</CODE> argument is not <CODE>null</CODE>, <CODE>HOME</CODE> and 
   * <CODE>USER</CODE> environmental variables will be added to the cooked environment. <P> 
   * 
   * If the <CODE>author</CODE> and <CODE>view</CODE> arguments are both not 
   * <CODE>null</CODE>, <CODE>HOME</CODE>, <CODE>USER</CODE> and <CODE>WORKING</CODE> 
   * environmental variables will be added to the cooked environment. <P> 
   * 
   * @param author
   *   The user owning the generated environment.
   * 
   * @param view 
   *   The name of the user's working area view.
   * 
   * @param tname
   *   The toolset name.
   * 
   * @param timer
   *   The task timer.
   * 
   * @throws PipelineException
   *   If unable to find the toolset.
   */ 
  private TreeMap<String,String>
  getToolsetEnvironment
  (
   String author, 
   String view,
   String tname, 
   TaskTimer timer 
  ) 
    throws PipelineException
  {
    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pToolsets) {
	timer.resume();
	
	Toolset tset = pToolsets.get(tname);
	if(tset == null) 
	  tset = readToolset(tname);
	assert(tset != null);
	
	TreeMap<String,String> env = null;
	if((author != null) && (view != null)) 
	  env = tset.getEnvironment(author, view);
	else if(author != null)
	  env = tset.getEnvironment(author);
	else 
	  env = tset.getEnvironment();
	
	assert(env != null);
	return env;
      }
    }
    finally {
      pDatabaseLock.readLock().unlock();
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
    pDatabaseLock.readLock().lock();
    try {
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
	     "Unable to create the toolset (" + req.getName() + ") due to conflicts " + 
	     "between the supplied packages!");
	
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
    finally {
      pDatabaseLock.readLock().unlock();
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
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pToolsetPackages) {
	timer.resume();
	
	TreeMap<String,TreeSet<VersionID>> names = new TreeMap<String,TreeSet<VersionID>>();
	for(String name : pToolsetPackages.keySet()) 
	  names.put(name, new TreeSet<VersionID>(pToolsetPackages.get(name).keySet()));
	
	return new MiscGetToolsetPackageNamesRsp(timer, names);
      }  
    }
    finally {
      pDatabaseLock.readLock().unlock();
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
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pToolsetPackages) {
	timer.resume();
	
	TreeMap<VersionID,PackageVersion> versions = pToolsetPackages.get(req.getName());
	if(versions == null) 
	  return new FailureRsp
	    (timer, "No toolset package (" + req.getName() + " v" + req.getVersionID() + 
	     ") exists!");

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
    finally {
      pDatabaseLock.readLock().unlock();
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
    pDatabaseLock.readLock().lock();
    try {
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
	      (timer, "Unable to create the toolset package (" + pname + ") due to a " + 
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
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   E D I T O R S                                                                        */
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
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pSuffixEditors) {
	timer.resume();
	
	try {
	  String author = req.getAuthor();
	  TreeMap<String,SuffixEditor> editors = getSuffixEditors(author);
	  
	  String ename = null;
	  SuffixEditor se = editors.get(req.getSuffix());
	  if(se != null) 
	    ename = se.getEditor();
	  
	  return new MiscGetEditorForSuffixRsp(timer, ename); 
	}
	catch(PipelineException ex) {
	  return new FailureRsp(timer, ex.getMessage());
	}
      }
    }
    finally {
      pDatabaseLock.readLock().unlock();
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
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pSuffixEditors) {
	timer.resume();
	
	try {
	  String author = req.getAuthor();
	  TreeSet<SuffixEditor> editors =	
	    new TreeSet<SuffixEditor>(getSuffixEditors(author).values());
	  return new MiscGetSuffixEditorsRsp(timer, editors);
	}
	catch(PipelineException ex) {
	  return new FailureRsp(timer, ex.getMessage());
	}
      }
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }

  /**
   * Get the filename suffix to default editor mappings for the given user. 
   */ 
  private TreeMap<String,SuffixEditor> 
  getSuffixEditors
  (
   String author
  ) 
    throws PipelineException
  {
    synchronized(pSuffixEditors) {
      TreeMap<String,SuffixEditor> editors = pSuffixEditors.get(author);
      if(editors == null) 
	editors = pSuffixEditors.get(author);
      
      if(editors == null) {
	editors = readSuffixEditors(author);
	
	if((editors == null) && !author.equals(PackageInfo.sPipelineUser)) {
	  editors = pSuffixEditors.get(PackageInfo.sPipelineUser);
	  if(editors == null) 
	    editors = readSuffixEditors(PackageInfo.sPipelineUser);
	}
      
      if(editors == null) 
	editors = new TreeMap<String,SuffixEditor>();
      }
      assert(editors != null);
      
      return editors;
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
    TaskTimer timer = new TaskTimer("MasterMgr.setSuffixEditors()");
    
    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
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
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P L U G I N   M E N U   L A Y O U T                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get layout of the editor plugin selection menu.
   *
   * @return
   *   <CODE>MiscGetPluginMenuLayoutRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the menu layout.
   */ 
  public Object 
  getEditorMenuLayout() 
  {
    TaskTimer timer = new TaskTimer("MasterMgr.getEditorMenuLayout()");

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pPluginMenuLayoutLock) {
	timer.resume();	
	
	PluginMenuLayout layout = new PluginMenuLayout(pEditorMenuLayout);
	return new MiscGetPluginMenuLayoutRsp(timer, layout);
      }
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }

  /**
   * Set the layout of the editor plugin selection menu.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to set the menu layout.
   */ 
  public Object 
  setEditorMenuLayout
  ( 
   MiscSetPluginMenuLayoutReq req 
  ) 
  {
    TaskTimer timer = 
      new TaskTimer("MasterMgr.setEditorMenuLayout()");
    
    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pPluginMenuLayoutLock) {
	timer.resume();	
	    
	pEditorMenuLayout = req.getLayout();

	try {
	  writeEditorMenuLayout();
	}
	catch(PipelineException ex) {
	  return new FailureRsp(timer, ex.getMessage());
	}      

	return new SuccessRsp(timer);
      }
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get layout of the comparator plugin selection menu.
   *
   * @return
   *   <CODE>MiscGetPluginMenuLayoutRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the menu layout.
   */ 
  public Object 
  getComparatorMenuLayout() 
  {
    TaskTimer timer = new TaskTimer("MasterMgr.getComparatorMenuLayout()");

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pPluginMenuLayoutLock) {
	timer.resume();	
	
	PluginMenuLayout layout = new PluginMenuLayout(pComparatorMenuLayout);
	return new MiscGetPluginMenuLayoutRsp(timer, layout);
      }
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }

  /**
   * Set the layout of the comparator plugin selection menu.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to set the menu layout.
   */ 
  public Object 
  setComparatorMenuLayout
  ( 
   MiscSetPluginMenuLayoutReq req 
  ) 
  {
    TaskTimer timer = 
      new TaskTimer("MasterMgr.setComparatorMenuLayout()");
    
    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pPluginMenuLayoutLock) {
	timer.resume();	
	    
	pComparatorMenuLayout = req.getLayout();

	try {
	  writeComparatorMenuLayout();
	}
	catch(PipelineException ex) {
	  return new FailureRsp(timer, ex.getMessage());
	}      

	return new SuccessRsp(timer);
      }
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get layout of the tool plugin selection menu.
   *
   * @return
   *   <CODE>MiscGetPluginMenuLayoutRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the menu layout.
   */ 
  public Object 
  getToolMenuLayout() 
  {
    TaskTimer timer = new TaskTimer("MasterMgr.getToolMenuLayout()");

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pPluginMenuLayoutLock) {
	timer.resume();	
	
	PluginMenuLayout layout = new PluginMenuLayout(pToolMenuLayout);
	return new MiscGetPluginMenuLayoutRsp(timer, layout);
      }
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }

  /**
   * Set the layout of the tool plugin selection menu.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to set the menu layout.
   */ 
  public Object 
  setToolMenuLayout
  ( 
   MiscSetPluginMenuLayoutReq req 
  ) 
  {
    TaskTimer timer = 
      new TaskTimer("MasterMgr.setToolMenuLayout()");
    
    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pPluginMenuLayoutLock) {
	timer.resume();	
	    
	pToolMenuLayout = req.getLayout();

	try {
	  writeToolMenuLayout();
	}
	catch(PipelineException ex) {
	  return new FailureRsp(timer, ex.getMessage());
	}      

	return new SuccessRsp(timer);
      }
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P R I V I L E G E D   U S E R S                                                      */
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
    TaskTimer timer = new TaskTimer("MasterMgr.getPrivilegedUsers()");
    
    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pPrivilegedUsers) {
	timer.resume();	
	
	TreeSet<String> users = new TreeSet<String>(pPrivilegedUsers);
	return new MiscGetPrivilegedUsersRsp(timer, users);
      }
    }
    finally {
      pDatabaseLock.readLock().unlock();
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
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pPrivilegedUsers) {
	timer.resume();	
	
	if(!pPrivilegedUsers.contains(req.getAuthor())) {      
	  pPrivilegedUsers.add(req.getAuthor());
	  try {
	    writePrivilegedUsers();
	    pQueueMgrClient.setPrivilegedUsers(pPrivilegedUsers);
	  }
	  catch(PipelineException ex) {
	    return new FailureRsp(timer, ex.getMessage());
	  }      
	}
	
	return new SuccessRsp(timer);
      }
    }
    finally {
      pDatabaseLock.readLock().unlock();
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
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pPrivilegedUsers) {
	timer.resume();	
	
	if(pPrivilegedUsers.contains(req.getAuthor())) {      
	  pPrivilegedUsers.remove(req.getAuthor());
	  try {
	    writePrivilegedUsers();
	    pQueueMgrClient.setPrivilegedUsers(pPrivilegedUsers);
	  }
	  catch(PipelineException ex) {
	    return new FailureRsp(timer, ex.getMessage());
	  }      
	}
	
	return new SuccessRsp(timer);
      }
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }


 
  /*----------------------------------------------------------------------------------------*/
  /*   W O R K I N G   A R E A S                                                            */
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
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pWorkingAreaViews) {
	timer.resume();	
	
	TreeMap<String,TreeSet<String>> views = new TreeMap<String,TreeSet<String>>();
	for(String author : pWorkingAreaViews.keySet()) 
	  views.put(author, (TreeSet<String>) pWorkingAreaViews.get(author).clone());
	
	return new NodeGetWorkingAreasRsp(timer, views);
      }
    }
    finally {
      pDatabaseLock.readLock().unlock();
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
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pWorkingAreaViews) {
	timer.resume();	
	
	String author = req.getAuthor();
	String view   = req.getView();
	
	/* make sure it doesn't already exist */ 
	TreeSet<String> views = pWorkingAreaViews.get(author);
	if((views != null) && views.contains(view))
	  return new SuccessRsp(timer);
	
	/* create the working area node directory */ 
	File dir = new File(PackageInfo.sNodeDir, "working/" + author + "/" + view);
	synchronized(pMakeDirLock) {
	  if(!dir.isDirectory()) {
	    if(!dir.mkdirs()) 
	      return new FailureRsp
		(timer, "Unable to create the working area (" + view + ") for user " + 
		 "(" + author + ")!");
	  }
	}

	/* create the working area files directory */ 
	try {
	  FileMgrClient fclient = getFileMgrClient();
	  try {
	    fclient.createWorkingArea(author, view);
	  }
	  finally {
	    freeFileMgrClient(fclient);
	  }
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
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }  

  /**
   * Remove an entire working area. <P> 
   * 
   * If the working area does not exist, the operation is successful even though nothing
   * is actually done.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to remove the given working area.
   */ 
  public Object 
  removeWorkingArea
  ( 
   NodeRemoveWorkingAreaReq req 
  ) 
  {
    TaskTimer timer = 
      new TaskTimer("MasterMgr.createWorkingArea(): " + 
		    req.getAuthor() + "|" + req.getView());

    if(req.getView().equals("default")) 
      return new FailureRsp(timer, "The default working area cannot be removed!");

    timer.aquire();
    pDatabaseLock.writeLock().lock();
    try {
      timer.resume();	

      String author = req.getAuthor();
      String view   = req.getView();
	
      /* abort if it doesn't exist */ 
      TreeSet<String> views = pWorkingAreaViews.get(author);
      if((views == null) || !views.contains(view))
	return new SuccessRsp(timer);

      /* make sure no working versions exist in the view */ 
      {
	TreeSet<String> matches = new TreeSet<String>();
	timer.aquire();
	synchronized(pNodeTreeRoot) {
	  timer.resume();

	  for(NodeTreeEntry entry : pNodeTreeRoot.values())
	    matchingWorkingNodes(author, view, null, "", entry, matches);
	}

	if(!matches.isEmpty()) {
	  StringBuffer buf = new StringBuffer();
	  buf.append
	    ("The working area view (" + view + " ownned by user (" + author + ") " + 
	     "cannot be removed because it still contains unreleased nodes!\n\n" + 
	     "The unreleased node are: ");
	  for(String name : matches) 
	    buf.append("\n  " + name);
	  return new FailureRsp(timer, buf.toString());
	}
      }

      /* remove the working area files directory */ 
      try {
	FileMgrClient fclient = getFileMgrClient();
	try {
	  fclient.removeWorkingArea(author, view);
	}
	finally {
	  freeFileMgrClient(fclient);
	}
      }
      catch(PipelineException ex) {
	return new FailureRsp(timer, ex.getMessage());
      }
      
      /* remove view from the runtime table */ 
      views.remove(view);
      
      return new SuccessRsp(timer);
    }
    finally {
      pDatabaseLock.writeLock().unlock();
    }
  }  

  /**
   * Get the names of the nodes in a working area for which have a name matching the 
   * given search pattern.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>NodeGetWorkingNamesRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to remove the given working area.
   */ 
  public Object 
  getWorkingNames
  ( 
   NodeGetWorkingNamesReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();	

      String author  = req.getAuthor();
      String view    = req.getView();
      String pattern = req.getPattern();  

      /* get the node names which match the pattern */ 
      TreeSet<String> matches = new TreeSet<String>();
      {
	timer.aquire();
	synchronized(pNodeTreeRoot) {
	  try {
	    timer.resume();
	    
	    Pattern pat = null;
	    if(pattern != null) 
	      pat = Pattern.compile(pattern);
	    
	    for(NodeTreeEntry entry : pNodeTreeRoot.values())
	      matchingWorkingNodes(author, view, pat, "", entry, matches);
	  }
	  catch(PatternSyntaxException ex) {
	    return new FailureRsp(timer, 
				  "Illegal Node Name Pattern:\n\n" + ex.getMessage());
	  }
	}
      }
                  
      return new NodeGetWorkingNamesRsp(timer, matches);
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }  



  /*----------------------------------------------------------------------------------------*/
  /*   N O D E   P A T H S                                                                  */
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
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pNodeTreeRoot) {
	timer.resume();	
	
	NodeTreeComp rootComp = new NodeTreeComp();
	TreeMap<String,Boolean> paths = req.getPaths();
	for(String path : paths.keySet()) {
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
	    boolean recursive = paths.get(path);
	    updatePathsBelow(req.getAuthor(), req.getView(), 
			     parentEntry, parentComp, recursive);
	  }
	}
	
	return new NodeUpdatePathsRsp(timer, req.getAuthor(), req.getView(), rootComp);
      }
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }

  private void 
  updatePathsBelow
  (
   String author, 
   String view, 
   NodeTreeEntry parentEntry, 
   NodeTreeComp parentComp,
   boolean recursive
  ) 
  {
    synchronized(pNodeTreeRoot) {
      for(NodeTreeEntry entry : parentEntry.values()) {
	if(!parentComp.containsKey(entry.getName())) {
	  NodeTreeComp comp = new NodeTreeComp(entry, author, view);
	  parentComp.put(comp.getName(), comp);
	  if(recursive) 
	    updatePathsBelow(author, view, entry, comp, true);
	}
      } 
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the node associated with the given file. <P> 
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>NodeGetNodeOwningRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the owning node.
   */
  public Object
  getNodeOwning
  (
   NodeGetNodeOwningReq req
  ) 
  {
    assert(req != null);

    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pNodeTreeRoot) {
	timer.resume();	
	
	String nodeName = null;
	{
	  String path = req.getPath();
	  String comps[] = path.split("/"); 
	  
	  NodeTreeEntry parentEntry = pNodeTreeRoot;
	  int wk;
	  for(wk=1; wk<(comps.length-1); wk++) {
	    NodeTreeEntry entry = (NodeTreeEntry) parentEntry.get(comps[wk]); 
	    if(entry == null) {
	      parentEntry = null;
	      break;
	    }
	    
	    parentEntry = entry;
	  }
	  
	  if((parentEntry != null) && !parentEntry.isLeaf())  {
	    String parts[] = comps[comps.length-1].split("\\.");

	    String prefix = null; 
	    String suffix = null; 
	    switch(parts.length) {
	    case 1:
	      prefix = parts[0];
	      break;

	    case 2: 
	      prefix = parts[0];
	      suffix = parts[1];
	      break;

	    case 3:
	      prefix = parts[0];
	      suffix = parts[2];
	    }	      

	    if(prefix != null) {
	      String key = (prefix + "|" + suffix);
	      for(String name : parentEntry.keySet()) {
		NodeTreeEntry entry = parentEntry.get(name);
		if(entry.isLeaf() && entry.getSequences().contains(key)) {
		  File file = new File(path);
		  nodeName = (file.getParent() + "/" + name);
		  break;
		}
	      }
	    }
	  }
	}

	return new NodeGetNodeOwningRsp(timer, nodeName);
      }
    }
    finally {
      pDatabaseLock.readLock().unlock();
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
    pDatabaseLock.readLock().lock();
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
      pDatabaseLock.readLock().unlock();
    }  
  }  

  /** 
   * Set the node properties of the working version of the node. <P> 
   * 
   * Node properties include: <BR>
   * 
   * <DIV style="margin-left: 40px;">
   *   The toolset environment under which editors and actions are run. <BR>
   *   The name of the editor plugin used to edit the data files associated with the node.<BR>
   *   The regeneration action and its single and per-source parameters. <BR>
   *   The overflow policy, execution method and job batch size. <BR> 
   *   The job requirements. <P>
   * </DIV> 
   * 
   * Note that any links to upstream nodes contain in the working version used to set node
   * properties will be ignored.  The {@link #link link} and {@link #unlink unlink} methods 
   * must be used to alter the connections between working node versions.
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
    NodeID nodeID = req.getNodeID();
    TaskTimer timer = new TaskTimer("MasterMgr.modifyProperties(): " + nodeID);

    timer.aquire();
    pDatabaseLock.readLock().lock();
    ReentrantReadWriteLock lock = getWorkingLock(nodeID);
    lock.writeLock().lock();
    try {
      timer.resume();

      /* get the working version */ 
      WorkingBundle bundle = getWorkingBundle(nodeID);
      NodeMod mod = new NodeMod(bundle.uVersion);
      if(mod.isFrozen()) 
	throw new PipelineException
	  ("The node properties of frozen node (" + nodeID + ") cannot be modified!");

      /* set the node properties */ 
      Date critical = mod.getLastCriticalModification();
      boolean wasActionEnabled = mod.isActionEnabled();
      if(mod.setProperties(req.getNodeMod())) {

	/* make sure there are no active jobs, if this is a critical modification */ 
	if((critical.compareTo(mod.getLastCriticalModification()) < 0) &&
	   hasActiveJobs(nodeID, mod.getTimeStamp(), mod.getPrimarySequence()))
	  throw new PipelineException
	    ("Unable to modify critical properties of node (" + nodeID + ") " + 
	     "while there are active jobs associated with the node!");

	/* write the new working version to disk */ 
	writeWorkingVersion(nodeID, mod);

	/* update the bundle */ 
	bundle.uVersion = mod;

	/* change working file write permissions? */ 
	if(wasActionEnabled != mod.isActionEnabled()) {
	  FileMgrClient fclient = getFileMgrClient();
	  try {
	    fclient.changeMode(nodeID, mod, !mod.isActionEnabled());
	  }
	  finally {
	    freeFileMgrClient(fclient);
	  }
	}
      }

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      lock.writeLock().unlock();
      pDatabaseLock.readLock().unlock();
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
    pDatabaseLock.readLock().lock();
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

      NodeMod mod = new NodeMod(bundle.uVersion);
      if(mod.isFrozen()) 
	throw new PipelineException
	  ("The upstream links of frozen node (" + targetID + ") cannot be modified!");

      /* make sure there are no active jobs */ 
      if(hasActiveJobs(targetID, mod.getTimeStamp(), mod.getPrimarySequence()))
	throw new PipelineException
	  ("Unable to change the links of target node (" + targetID + ") " + 
	   "while there are active jobs associated with the node!");
      
      /* add the link */ 
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
      pDatabaseLock.readLock().unlock();
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
    pDatabaseLock.readLock().lock();
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

      NodeMod mod = new NodeMod(bundle.uVersion);
      if(mod.isFrozen()) 
	throw new PipelineException
	  ("The upstream links of frozen node (" + targetID + ") cannot be modified!");

      /* make sure there are no active jobs */ 
      if(hasActiveJobs(targetID, mod.getTimeStamp(), mod.getPrimarySequence()))
	throw new PipelineException
	  ("Unable to change the links of target node (" + targetID + ") " + 
	   "while there are active jobs associated with the node!");

      /* remove the link */ 
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
      pDatabaseLock.readLock().unlock();
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

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      /* reserve the node name, 
           after verifying that it doesn't conflict with existing nodes */ 
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

	NodeMod mod = new NodeMod(bundle.uVersion);
	if(mod.isFrozen()) 
	  throw new PipelineException
	    ("The secondary sequences of frozen node (" + nodeID + ") cannot be modified!");
	
	/* make sure there are no active jobs */ 
	if(hasActiveJobs(nodeID, mod.getTimeStamp(), mod.getPrimarySequence()))
	  throw new PipelineException
	    ("Unable to add secondary file sequences to the node (" + nodeID + ") " + 
	     "while there are active jobs associated with the node!");
	
	/* add the secondary sequence */ 
	mod.addSecondarySequence(fseq);
	
	/* write the new working version to disk */ 
	writeWorkingVersion(nodeID, mod);
	
	/* update the bundle */ 
	bundle.uVersion = mod;
	
	return new SuccessRsp(timer);
      }
      catch(PipelineException ex) { 
	timer.aquire();
	removeSecondaryWorkingNodeTreePath(nodeID, fseq);
	return new FailureRsp(timer, ex.getMessage());
      }
      finally {
	lock.writeLock().unlock();
      }    
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }

  /**
   * Remove a secondary file sequence from the given working version. <P> 
   * 
   * @param req 
   *   The remove secondary request.
   *
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to remove the secondary file sequence.
   */
  public Object
  removeSecondary
  (
   NodeRemoveSecondaryReq req 
  ) 
  {
    assert(req != null);
    
    NodeID nodeID = req.getNodeID();
    FileSeq fseq  = req.getFileSequence();

    TaskTimer timer = new TaskTimer("MasterMgr.removeSecondary(): " + nodeID);

    timer.aquire();
    pDatabaseLock.readLock().lock();
    ReentrantReadWriteLock lock = getWorkingLock(nodeID);
    lock.writeLock().lock();
    try {
      timer.resume();	

      WorkingBundle bundle = getWorkingBundle(nodeID);
      if(bundle == null) 
	throw new PipelineException
	  ("Secondary file sequences can only be remove from working versions of nodes!\n" + 
	   "No working version (" + nodeID + ") exists.");

      NodeMod mod = new NodeMod(bundle.uVersion);
      if(mod.isFrozen()) 
	throw new PipelineException
	  ("The secondary sequences of frozen node (" + nodeID + ") cannot be modified!");

      /* make sure there are no active jobs */ 
      if(hasActiveJobs(nodeID, mod.getTimeStamp(), mod.getPrimarySequence()))
	throw new PipelineException
	  ("Unable to remove secondary file sequences from the node (" + nodeID + ") " + 
	   "while there are active jobs associated with the node!");

      /* remove the link */ 
      mod.removeSecondarySequence(fseq);
      
      /* write the new working version to disk */ 
      writeWorkingVersion(nodeID, mod);
      
      /* update the bundle */ 
      bundle.uVersion = mod;

      /* remove the sequence from the node tree */ 
      removeSecondaryWorkingNodeTreePath(nodeID, fseq);
      
      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      lock.writeLock().unlock();
      pDatabaseLock.readLock().unlock();
    }    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C H E C K E D - I N   V E R S I O N S                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the revision numbers of all checked-in versions of the given node. <P> 
   * 
   * @param req 
   *   The get checked-in version request.
   * 
   * @return
   *   <CODE>NodeGetCheckedInVersionIDsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to retrieve the checked-in version.
   */
  public Object
  getCheckedInVersionIDs
  ( 
   NodeGetCheckedInVersionIDsReq req
  ) 
  {
    String name = req.getName();
    TaskTimer timer = new TaskTimer("MasterMgr.getCheckedInVersionIDs(): " + name);

    timer.aquire();
    pDatabaseLock.readLock().lock();
    ReentrantReadWriteLock lock = getCheckedInLock(name);
    lock.readLock().lock();
    try {
      timer.resume();	

      TreeMap<VersionID,CheckedInBundle> checkedIn = getCheckedInBundles(name);
      TreeSet<VersionID> vids = new TreeSet<VersionID>(checkedIn.keySet());

      return new NodeGetCheckedInVersionIDsRsp(timer, vids);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      lock.readLock().unlock();
      pDatabaseLock.readLock().unlock();
    }  
  }  
      
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
    TaskTimer timer = new TaskTimer();

    String name = req.getName();
    VersionID vid = req.getVersionID();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    ReentrantReadWriteLock lock = getCheckedInLock(name);
    lock.readLock().lock();
    try {
      timer.resume();	

      TreeMap<VersionID,CheckedInBundle> checkedIn = getCheckedInBundles(name);

      if(vid == null) 
	vid = checkedIn.lastKey();

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
      pDatabaseLock.readLock().unlock();
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
    pDatabaseLock.readLock().lock();
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
      pDatabaseLock.readLock().unlock();
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
    pDatabaseLock.readLock().lock();
    ReentrantReadWriteLock lock = getCheckedInLock(name);
    lock.readLock().lock();
    try {
      timer.resume();	

      TreeMap<VersionID,CheckedInBundle> checkedIn = null;
      try {
	checkedIn = getCheckedInBundles(name);
      }
      catch(PipelineException ex) {
      }

      TreeMap<VersionID,TreeMap<FileSeq,boolean[]>> novelty = 
	new TreeMap<VersionID,TreeMap<FileSeq,boolean[]>>();

      if(checkedIn != null) {
	for(VersionID vid : checkedIn.keySet()) {
	  NodeVersion vsn = checkedIn.get(vid).uVersion;
	  
	  TreeMap<FileSeq,boolean[]> table = new TreeMap<FileSeq,boolean[]>();
	  for(FileSeq fseq : vsn.getSequences()) 
	    table.put(fseq, vsn.isNovel(fseq));
	  
	  novelty.put(vid, table);
	}
      }
	
      return new NodeGetCheckedInFileNoveltyRsp(timer, name, novelty);
    }
    finally {
      lock.readLock().unlock();
      pDatabaseLock.readLock().unlock();
    }  
  }  

  /** 
   * Get the upstream links of all checked-in versions of the given node.
   * 
   * @param req 
   *   The links request.
   * 
   * @return
   *   <CODE>NodeGetCheckedInLinksRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to retrieve the checked-in links.
   */
  public Object
  getCheckedInLinks
  ( 
   NodeGetCheckedInLinksReq req
  ) 
  {	 
    TaskTimer timer = new TaskTimer();
  
    String name = req.getName();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    ReentrantReadWriteLock lock = getCheckedInLock(name);
    lock.readLock().lock();
    try {
      timer.resume();	

      TreeMap<VersionID,CheckedInBundle> checkedIn = null;
      try {
	checkedIn = getCheckedInBundles(name);
      }
      catch(PipelineException ex) {
      }

      TreeMap<VersionID,TreeMap<String,LinkVersion>> links = 
	new TreeMap<VersionID,TreeMap<String,LinkVersion>>();

      if(checkedIn != null) {
	for(VersionID vid : checkedIn.keySet()) {
	  NodeVersion vsn = checkedIn.get(vid).uVersion;
	  
	  TreeMap<String,LinkVersion> table = new TreeMap<String,LinkVersion>();
	  for(LinkVersion link : vsn.getSources()) 
	    table.put(link.getName(), link);

	  links.put(vid, table);
	}
      }
	
      return new NodeGetCheckedInLinksRsp(timer, links);
    }
    finally {
      lock.readLock().unlock();
      pDatabaseLock.readLock().unlock();
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
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();

      NodeID nodeID = req.getNodeID();
      NodeStatus root = 
	performNodeOperation(new NodeOp(), nodeID, timer);
      return new NodeStatusRsp(timer, nodeID, root);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }    
    finally {
      pDatabaseLock.readLock().unlock();
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
    /* node identifiers */ 
    String name   = req.getNodeMod().getName();
    NodeID nodeID = req.getNodeID();

    TaskTimer timer = new TaskTimer("MasterMgr.register(): " + nodeID);

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();	

      /* reserve the node name, 
         after verifying that it doesn't conflict with existing nodes */ 
      if(checkName) {
	timer.aquire();
	synchronized(pNodeTreeRoot) {
	  timer.resume();
	  
	  if(!isNodePathUnused(name, req.getNodeMod().getPrimarySequence(), false)) 
	    return new FailureRsp
	      (timer, "Cannot register node (" + name + ") because its name conflicts " + 
	       "with an existing node or one of its associated file sequences!");
	
	  addWorkingNodeTreePath(nodeID, req.getNodeMod().getSequences());
	}
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
	  HashMap<NodeID,WorkingBundle> table = pWorkingBundles.get(name);
	  if(table == null) {
	    table = new HashMap<NodeID,WorkingBundle>();
	    pWorkingBundles.put(name, table);
	  }
	  table.put(nodeID, new WorkingBundle(req.getNodeMod()));
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
	return new FailureRsp(timer, ex.getMessage());
      }
      finally {
	lock.writeLock().unlock();
      }  
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Release the working version of a node and optionally remove the associated 
   * working area files. <P> 
   * 
   * @param req 
   *   The node release request.
   *
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to release the working version.
   */
  public Object
  release
  (
   NodeReleaseReq req
  ) 
  {
    assert(req != null);
    NodeID id = req.getNodeID();
    String name = id.getName();

    TaskTimer timer = new TaskTimer("MasterMgr.release(): " + id);

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();	

      /* unlink the downstream working versions from the to be released working version */ 
      {
	timer.aquire();
	ReentrantReadWriteLock downstreamLock = getDownstreamLock(name);
	downstreamLock.writeLock().lock();
	try {
	  timer.resume();
	  
	  DownstreamLinks links = getDownstreamLinks(name); 
	  if(links != null) {
	    TreeSet<String> targets = links.getWorking(id);
	    if(targets != null) {
	      for(String target : targets) {
		NodeID targetID = new NodeID(id, target);
		
		timer.suspend();
		Object obj = unlink(new NodeUnlinkReq(targetID, name));
		timer.accum(((TimedRsp) obj).getTimer());
		
		if(obj instanceof FailureRsp)  {
		  FailureRsp rsp = (FailureRsp) obj;
		  return new FailureRsp(timer, rsp.getMessage());
		}
	      }
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
	    ("No working version (" + id + ") exists to be released.");
	NodeMod mod = bundle.uVersion;
	
	/* kill any active jobs associated with the node */
	killActiveJobs(id, mod.getTimeStamp(), mod.getPrimarySequence());
	
	/* remove the bundle */ 
	synchronized(pWorkingBundles) {
	  HashMap<NodeID,WorkingBundle> table = pWorkingBundles.get(name);
	  table.remove(id);
	}
	
	/* remove the working version node file(s) */ 
	{
	  File file   = new File(PackageInfo.sNodeDir, id.getWorkingPath().getPath());
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

	  File root = new File(PackageInfo.sNodeDir, 
			       "working/" + id.getAuthor() + "/" + id.getView());

	  deleteEmptyParentDirs(root, new File(PackageInfo.sNodeDir, 
					       id.getWorkingParent().toString()));
	}
	
	/* update the downstream links of this node */ 
	{
	  boolean isRevoked = false;
	  
	  timer.aquire();	
	  ReentrantReadWriteLock downstreamLock = getDownstreamLock(name);
	  downstreamLock.writeLock().lock();
	  try {
	    timer.resume();
	    
	    DownstreamLinks links = getDownstreamLinks(name); 
	    links.releaseWorking(id);
	  }  
	  finally {
	    downstreamLock.writeLock().unlock();
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
	    links.removeWorking(sourceID, name);
	  }  
	  finally {
	    downstreamLock.writeLock().unlock();
	  }    
	}
	
	/* remove the node tree path */ 
	removeWorkingNodeTreePath(id);

	/* remove the associated files */ 
	if(req.removeFiles()) {
	  FileMgrClient fclient = getFileMgrClient();
	  try {
	    fclient.removeAll(id, mod.getSequences());
	  }
	  finally {
	    freeFileMgrClient(fclient);
	  }	
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
    finally {
      pDatabaseLock.readLock().unlock();
    } 
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Delete all working and checked-in versions of a node and optionally remove all  
   * associated working area files. <P> 
   * 
   * @param req 
   *   The node delete request.
   *
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to delete the node.
   */
  public Object
  delete
  (
   NodeDeleteReq req
  ) 
  {
    assert(req != null);
    String name = req.getName();
    
    TaskTimer timer = new TaskTimer("MasterMgr.delete(): " + name);
    timer.aquire();
    pDatabaseLock.writeLock().lock();
    try {
      timer.resume();	

      /* get the checked-in versions  */ 
      TreeMap<VersionID,CheckedInBundle> checkedIn = null;
      try {
	checkedIn = getCheckedInBundles(name);
      }
      catch(PipelineException ex) {
	checkedIn = new TreeMap<VersionID,CheckedInBundle>();
      }

      /* get the downstream links */ 
      DownstreamLinks dsl = getDownstreamLinks(name);

      /* make sure none of the checked-in versions are the source node of a link of 
           another node */ 
      for(VersionID vid : checkedIn.keySet()) {
	TreeMap<String,VersionID> dlinks = dsl.getCheckedIn(vid);
	if(dlinks == null) 
	  throw new PipelineException
	    ("Somehow there was no downstream links entry for checked-in version " + 
	     "(" + vid + ") of node (" + name + ")!");
	
	if(!dlinks.isEmpty()) 
	  throw new PipelineException
	    ("Cannot delete node (" + name + ") because the checked-in version " + 
	     "(" + vid + ") is linked to other nodes!");
      } 

      /* release all working versions of the node */ 
      {
	ArrayList<NodeID> dead = new ArrayList<NodeID>();
	for(String author : pWorkingAreaViews.keySet()) {
	  for(String view : pWorkingAreaViews.get(author)) {
	    NodeID nodeID = new NodeID(author, view, name);
	    try {
	      getWorkingBundle(nodeID);
	      dead.add(nodeID);
	    }
	    catch(PipelineException ex) {
	    }
	  }
	}

	for(NodeID nodeID : dead) {
	  timer.suspend();
	  Object obj = release(new NodeReleaseReq(nodeID, req.removeFiles()));
	  timer.accum(((TimedRsp) obj).getTimer());
	  if(obj instanceof FailureRsp) {
	    FailureRsp rsp = (FailureRsp) obj;
	    throw new PipelineException(rsp.getMessage());	
	  }

	  pWorkingLocks.remove(nodeID);
	}
	
	assert(pWorkingBundles.get(name).isEmpty());
	pWorkingBundles.remove(name);
      }
	
      /* delete the checked-in versions */ 
      if(!checkedIn.isEmpty()) {

	/* remove the downstream links to this node from the checked-in source nodes */ 
	for(VersionID vid : checkedIn.keySet()) {
	  NodeVersion vsn = checkedIn.get(vid).uVersion;
	  for(LinkVersion link : vsn.getSources()) {
	    DownstreamLinks ldsl = getDownstreamLinks(link.getName());
	    ldsl.deleteCheckedIn(link.getVersionID(), name);
	  }
	}

	/* delete files associated with all checked-in versions of the node */ 
	{
	  FileMgrClient fclient = getFileMgrClient();
	  try {
	    fclient.deleteCheckedIn(name);
	  }
	  finally {
	    freeFileMgrClient(fclient);
	  }
	}
	
	/* remove the checked-in version files */ 
	for(VersionID vid : checkedIn.keySet()) {
	  File file = new File(PackageInfo.sNodeDir, "repository" + name + "/" + vid);
	  if(!file.delete())
	    throw new PipelineException
	      ("Unable to remove the checked-in version file (" + file + ")!");
	}

	/* remove the checked-in version node directory */
	{
	  File dir = new File(PackageInfo.sNodeDir, "repository" + name);
	  File parent =  dir.getParentFile();
	  if(!dir.delete())
	    throw new PipelineException
	      ("Unable to remove the checked-in version directory (" + dir + ")!");
	  
	  deleteEmptyParentDirs(new File(PackageInfo.sNodeDir, "repository"), parent);
	}	    
	
	/* remove the checked-in version entries */ 
	pCheckedInLocks.remove(name);
	pCheckedInBundles.remove(name);
      }

      /* remove the downstream links file and entry */ 
      {
	File file = new File(PackageInfo.sNodeDir, "downstream" + name);
	if(file.isFile()) {
	  if(!file.delete())
	    throw new PipelineException
	      ("Unable to remove the downstream links file (" + file + ")!");
	}

	pDownstream.remove(name);
      }

      /* remove the leaf node tree entry */ 
      removeNodeTreePath(name);

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      pDatabaseLock.writeLock().unlock();
    }
  }

    
  /*----------------------------------------------------------------------------------------*/

  /**
   * Remove the working area files associated with the given node. <P>  
   * 
   * @param req 
   *   The submit jobs request.
   * 
   * @return 
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to kill the jobs.
   */ 
  public Object
  removeFiles
  (
   NodeRemoveFilesReq req
  ) 
  {
    NodeID nodeID = req.getNodeID();
    TaskTimer timer = new TaskTimer("MasterMgr.removeFiles(): " + nodeID);

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();	

      TreeSet<Long> activeIDs = new TreeSet<Long>();
      TreeSet<FileSeq> fseqs = new TreeSet<FileSeq>();
      
      timer.aquire();
      ReentrantReadWriteLock lock = getWorkingLock(nodeID);
      lock.readLock().lock();
      try {
	timer.resume();
	
	WorkingBundle bundle = getWorkingBundle(nodeID);
	NodeMod mod = bundle.uVersion;
	if(mod.isFrozen()) 
	  throw new PipelineException
	    ("The files associated with frozen node (" + nodeID + ") cannot be removed!");
	
	ArrayList<Long> jobIDs = new ArrayList<Long>();
	ArrayList<JobState> jobStates = new ArrayList<JobState>();
	pQueueMgrClient.getJobStates(nodeID, mod.getTimeStamp(), mod.getPrimarySequence(),
				     jobIDs, jobStates);
	
	TreeSet<Integer> indices = req.getIndices();
	if(indices == null) {
	  int wk = 0;
	  for(JobState state : jobStates) {
	    Long jobID = jobIDs.get(wk);
	    if((state != null) && (jobID != null)) {
	      switch(state) {
	      case Queued:
	      case Paused:
	      case Running:
		activeIDs.add(jobID);
	      }
	    }
	    
	    wk++;
	  }
	  
	  fseqs.addAll(mod.getSequences());
	}
	else {
	  for(Integer idx : indices) {
	    JobState state = jobStates.get(idx);
	    Long jobID = jobIDs.get(idx);
	    if((state != null) && (jobID != null)) {
	      switch(state) {
	      case Queued:
	      case Paused:
	      case Running:
		activeIDs.add(jobID);
	      }
	    }
	  }
	  
	  for(FileSeq fseq : mod.getSequences()) {
	    for(Integer idx : indices) 
	      fseqs.add(new FileSeq(fseq, idx));
	  }
	}
      }
      catch(PipelineException ex) {
	return new FailureRsp(timer, ex.getMessage());
      }    
      finally {
	lock.readLock().unlock();
      }    
      assert(fseqs != null);
      
      try {
	if(!activeIDs.isEmpty()) 
	  pQueueMgrClient.killJobs(nodeID.getAuthor(), activeIDs);

	{
	  FileMgrClient fclient = getFileMgrClient();
	  try {
	    fclient.removeAll(nodeID, fseqs);
	  }
	  finally {
	    freeFileMgrClient(fclient);
	  }
	}

	return new SuccessRsp(timer);  
      }
      catch(PipelineException ex) {
	return new FailureRsp(timer, ex.getMessage());
      }    
    }
    finally {
      pDatabaseLock.readLock().unlock();
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

    FilePattern npat = req.getFilePattern();
    String nname     = npat.getPrefix();
    NodeID nid       = new NodeID(id, nname);

    TaskTimer timer = new TaskTimer("MasterMgr.rename(): " + id + " to " + npat);

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();	

      try {
	NodeCommon.validateName(nname);
      }
      catch(IllegalArgumentException ex) {
	return new FailureRsp(timer, ex.getMessage());
      }

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

	  /* make sure its not frozen */ 
	  if(mod.isFrozen()) 
	    throw new PipelineException
	      ("The frozen node (" + id + ") cannot be renamed!");

	  /* make sure its an initial version */ 
	  if(mod.getWorkingID() != null) 
	    throw new PipelineException
	      ("Cannot rename node (" + name + ") because it is not an initial " + 
	       "working version!");

	  /* make sure there are no active jobs */ 
	  if(hasActiveJobs(id, mod.getTimeStamp(), mod.getPrimarySequence()))
	    throw new PipelineException
	      ("Unable to rename the node (" + id + ") while there are active " + 
	       "jobs associated with the node!");

	  {
	    FileSeq fseq = mod.getPrimarySequence();
	    FilePattern opat = fseq.getFilePattern();
	    if(opat.hasFrameNumbers() != npat.hasFrameNumbers()) 
	      throw new PipelineException
		("Unable to rename the node (" + id + "), because the new file pattern " + 
		 "(" + npat + ") " + (npat.hasFrameNumbers() ? "has" : "does NOT have") + 
		 " frame numbers and the old file pattern (" + opat + ") " +
		 (opat.hasFrameNumbers() ? "has" : "does NOT have") + " frame numbers!");

	    FrameRange range = fseq.getFrameRange();

	    File path = new File(nname);      
	    FilePattern pat = 
	      new FilePattern(path.getName(), npat.getPadding(), npat.getSuffix());

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

      /* if the prefix is different, 
	   reserve the new node name after verifying that it doesn't conflict with 
	   existing nodes */ 
      if(!name.equals(nname)) {
	timer.aquire();
	synchronized(pNodeTreeRoot) {
	  timer.resume();
	  
	  if(!isNodePathUnused(nname, primary, false)) 
	    return new FailureRsp
	      (timer, "Cannot rename node (" + name + ") to (" + nname + ") because the  " + 
	       "new name conflicts with an existing node or one of its associated file " +
	       "sequences!");

	  TreeSet<FileSeq> fseqs = new TreeSet<FileSeq>();
	  fseqs.add(primary);
	  fseqs.addAll(secondary);
	  
	  addWorkingNodeTreePath(nid, fseqs);
	}
      }

      /* if the prefix is different, 
	   unlink the downstream working versions from the to be renamed working version 
	   while collecting the existing downstream links */ 
      TreeMap<String,LinkMod> dlinks = null;
      if(!name.equals(nname)) {
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
	NodeMod omod = bundle.uVersion;
	NodeMod nmod = new NodeMod(omod);
	
	nmod.rename(npat);

	if(name.equals(nname)) {
	  /* write the new working version to disk */ 
	  writeWorkingVersion(id, nmod);
	  
	  /* update the bundle */ 
	  bundle.uVersion = nmod;
	}	
	else {
	  nmod.removeAllSources();

	  /* register the new named node */ 
	  {
	    Object obj = register(new NodeRegisterReq(nid, nmod), false);
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
	  
	  /* release the old named node */ 
	  {
	    timer.suspend();
	    Object obj = release(new NodeReleaseReq(id, false));
	    timer.accum(((TimedRsp) obj).getTimer());
	    if(obj instanceof FailureRsp) {
	      FailureRsp rsp = (FailureRsp) obj;
	      throw new PipelineException(rsp.getMessage());	
	    }
	  }
	}

	/* rename the files */ 
	if(req.renameFiles()) {
	  FileMgrClient fclient = getFileMgrClient();
	  try {
	    fclient.rename(id, omod, npat);
	  }
	  finally {
	    freeFileMgrClient(fclient);
	  }
	}
      }
      catch(PipelineException ex) {
	return new FailureRsp(timer, ex.getMessage());
      }
      finally {
	nlock.writeLock().unlock();
	lock.writeLock().unlock();
      }  

      /* if the prefix is different, 
	   reconnect the downstream nodes to the new named node */ 
      if(!name.equals(nname)) {
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
      }

      return new SuccessRsp(timer);
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Renumber the frame ranges of the file sequences associated with the given node. <P> 
   * 
   * @param req 
   *   The node renumber request.
   *
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to renumber the working version.
   */
  public Object
  renumber
  (
   NodeRenumberReq req
  ) 
  {
    assert(req != null);
    
    NodeID nodeID = req.getNodeID();
    String name = nodeID.getName();
    FrameRange range = req.getFrameRange();

    TaskTimer timer = new TaskTimer("MasterMgr.renumber(): " + nodeID + " [" + range + "]");

    timer.aquire();
    pDatabaseLock.readLock().lock();
    ReentrantReadWriteLock lock = getWorkingLock(nodeID);
    lock.writeLock().lock(); 
    try {
      timer.resume();
    
      WorkingBundle bundle = getWorkingBundle(nodeID);
      if(bundle == null) 
	throw new PipelineException
	  ("Only working versions of nodes may have their frame ranges renumbered!\n" + 
	   "No working version (" + nodeID + ") exists.");

      NodeMod mod = new NodeMod(bundle.uVersion);
      if(mod.isFrozen()) 
	throw new PipelineException
	  ("The frozen node (" + nodeID + ") cannot be renumbered!");
      
      /* renumber the file sequences */ 
      ArrayList<File> obsolete = mod.adjustFrameRange(range);

      /* write the new working version to disk */ 
      writeWorkingVersion(nodeID, mod);
      
      /* update the bundle */ 
      bundle.uVersion = mod;

      /* remove obsolete files... */ 
      if(req.removeFiles()) {
	FileMgrClient fclient = getFileMgrClient();
	try {
	  fclient.remove(nodeID, obsolete);
	}
	finally {
	  freeFileMgrClient(fclient);
	}
      }
      
      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      lock.writeLock().unlock();
      pDatabaseLock.readLock().unlock();
    }    
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Check-In the tree of nodes rooted at the given working version. <P> 
   * 
   * @param req 
   *   The node check-in request.
   *
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
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

    TaskTimer timer = new TaskTimer("MasterMgr.checkIn(): " + nodeID);

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();	

      /* make sure the latest action plugins are loaded */ 
      PluginMgrClient.getInstance().update();

      /* determine the revision number of the to be created version of the root node */ 
      VersionID rootVersionID = null;
      {      
	String name = nodeID.getName();

	timer.aquire();
	ReentrantReadWriteLock lock = getCheckedInLock(name);
	lock.readLock().lock();
	try {
	  timer.resume();
	  TreeMap<VersionID,CheckedInBundle> checkedIn = getCheckedInBundles(name);
	  VersionID latestID = checkedIn.lastKey();
	  
	  VersionID.Level level = req.getLevel();
	  if(level == null) 
	    level = VersionID.Level.Minor;
	  rootVersionID = new VersionID(latestID, level);
	}
	catch(PipelineException ex) {
	  rootVersionID = new VersionID();
	}
	finally {
	  lock.readLock().unlock();
	}
      }

      /* check-in the tree of nodes */ 
      performNodeOperation(new NodeCheckInOp(req, rootVersionID), nodeID, timer);
      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, 
			    "Check-In operation aborted!\n\n" +
			    ex.getMessage());
    }  
    finally {
      pDatabaseLock.readLock().unlock();
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
   *   <CODE>SuccessRsp</CODE> if successful or 
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

    TaskTimer timer = new TaskTimer("MasterMgr.checkOut(): " + nodeID);

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();	      

      /* get the current status of the nodes */ 
      HashMap<String,NodeStatus> table = new HashMap<String,NodeStatus>();
      performUpstreamNodeOp(new NodeOp(), req.getNodeID(), 
			    new LinkedList<String>(), table, timer);

      /* check whether any of the checked-in versions required are currently offline */ 
      {
	TreeMap<String,TreeSet<VersionID>> ovsns = new TreeMap<String,TreeSet<VersionID>>();
	validateCheckOut(true, nodeID, req.getVersionID(), req.getMode(), req.getMethod(), 
			 table, ovsns, new LinkedList<String>(), new HashSet<String>(), 
			 timer);
	
	if(!ovsns.isEmpty()) {
	  StringBuffer buf = new StringBuffer();
	  {
	    buf.append
	      ("Unable to perform check-out because the following checked-in versions " + 
	       "are currently offline:\n\n");
	    for(String name : ovsns.keySet()) {
	      for(VersionID vid : ovsns.get(name)) 
		buf.append(name + " v" + vid + "\n");
	    }
	    buf.append("\n");
	  }

	  Object obj = requestRestore(new MiscRequestRestoreReq(ovsns));
	  if(obj instanceof FailureRsp) {
	    FailureRsp rsp = (FailureRsp) obj;
	    buf.append
	      ("The request to restore these offline versions also failed:\n\n" + 
	       rsp.getMessage());	    
	  }
	  else {
	    buf.append
	      ("However, requests have been submitted to restore the offline versions " + 
	       "so that they may be used once they have been brought back online.");
	  }

	  throw new PipelineException(buf.toString());
	}
      }

      /* check-out the nodes */ 
      performCheckOut(true, nodeID, req.getVersionID(), req.getMode(), req.getMethod(), 
		      table, new LinkedList<String>(), new HashSet<String>(), 
		      new HashSet<String>(), timer);

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, 
			    "Check-Out operation aborted!\n\n" +
			    ex.getMessage());
    }   
    finally {
      pDatabaseLock.readLock().unlock();
    } 
  }

  /**
   * Recursively check for offline checked-in versions required by the check-out operation. 
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
   * @param mode
   *   The criteria used to determine whether nodes upstream of the root node of the check-out
   *   should also be checked-out.
   *
   * @param method
   *   The method for creating working area files/links from the checked-in files.
   *
   * @param stable
   *   The current node status indexed by fully resolved node name.
   * 
   * @param offlineVersions
   *   The names and revision numbers of the offline checked-in versions required by 
   *   the check-out operation.
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
  validateCheckOut
  (
   boolean isRoot, 
   NodeID nodeID, 
   VersionID vid, 
   CheckOutMode mode,
   CheckOutMethod method, 
   HashMap<String,NodeStatus> stable,
   TreeMap<String,TreeSet<VersionID>> offlineVersions, 
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

    /* lookup or compute the node status */ 
    NodeDetails details = null;
    {
      NodeStatus status = stable.get(name);
      if(status == null) {
	performUpstreamNodeOp(new NodeOp(), nodeID, 
			      new LinkedList<String>(), stable, timer);
	status = stable.get(name);
      }

      details = status.getDetails();
      assert(details != null);
    }

    timer.aquire();
    ReentrantReadWriteLock workingLock = getWorkingLock(nodeID);
    workingLock.readLock().lock();
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
 
      /* determine the check-out method for upstreadm nodes */ 
      CheckOutMethod checkOutMethod = method;      
      switch(method) {
      case PreserveFrozen:
	if((work != null) && work.isFrozen()) 
	  checkOutMethod = CheckOutMethod.AllFrozen;
      }

      /* see if the check-out should be skipped */ 
      if(work != null) {
	switch(mode) {
	case OverwriteAll:
	  if((details != null) && 
	     (details.getOverallNodeState() == OverallNodeState.Identical) && 
	     (details.getOverallQueueState() == OverallQueueState.Finished) && 
	     work.getWorkingID().equals(vsn.getVersionID())) {
	    branch.removeLast();
	    return;
	  }
	  break;

	case KeepNewer:
	  if(!isRoot && (work.getWorkingID().compareTo(vsn.getVersionID()) > 0)) {
	    branch.removeLast();
	    return;
	  }
	  break;

	case KeepModified:
	  if(!isRoot && (work.getWorkingID().compareTo(vsn.getVersionID()) >= 0)) {
	    branch.removeLast();
	    return;
	  }
	}
      }

      /* process the upstream nodes */
      for(LinkVersion link : vsn.getSources()) {
	NodeID lnodeID = new NodeID(nodeID, link.getName());
	validateCheckOut(false, lnodeID, link.getVersionID(), mode, checkOutMethod, 
			 stable, offlineVersions, branch, seen, timer);
      }

      /* check for offline versions */ 
      timer.aquire();
      synchronized(pOfflined) {
	timer.resume();
	
	TreeSet<VersionID> offline = pOfflined.get(name);
	if((offline != null) && offline.contains(vid)) {
	  TreeSet<VersionID> ovids = offlineVersions.get(name);
	  if(ovids == null) {
	    ovids = new TreeSet<VersionID>();
	    offlineVersions.put(name, ovids);
	  }
	  ovids.add(vid);
	}
      }
    }
    finally {
      checkedInLock.readLock().unlock();  
      workingLock.readLock().unlock();
    }

    /* pop the current node off of the end of the branch */ 
    branch.removeLast();
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
   * @param mode
   *   The criteria used to determine whether nodes upstream of the root node of the check-out
   *   should also be checked-out.
   *
   * @param method
   *   The method for creating working area files/links from the checked-in files.
   *
   * @param stable
   *   The current node status indexed by fully resolved node name.
   * 
   * @param branch
   *   The names of the nodes from the root to this node.
   * 
   * @param seen
   *   The names of the previously processed nodes.
   * 
   * @param dirty
   *   The names of the nodes which should cause downstream nodes with enabled actions
   *   to delete their working area files instead of replacing them with files from the
   *   repository.
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
   CheckOutMode mode,
   CheckOutMethod method, 
   HashMap<String,NodeStatus> stable,
   LinkedList<String> branch, 
   HashSet<String> seen, 
   HashSet<String> dirty, 
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

    /* lookup or compute the node status */ 
    NodeDetails details = null;
    {
      NodeStatus status = stable.get(name);
      if(status == null) {
	performUpstreamNodeOp(new NodeOp(), nodeID, 
			      new LinkedList<String>(), stable, timer);
	status = stable.get(name);
      }

      details = status.getDetails();
      assert(details != null);
    }

    /* make sure the node does have any active jobs */
    switch(details.getOverallQueueState()) {
    case Queued:
    case Paused:
    case Running:
      throw new PipelineException
	("The node (" + nodeID + ") cannot be checked-out while there are active " + 
	 "jobs associated with the node!");	      
    }

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
 
      /* determine whether working files or links should be created */ 
      boolean isFrozen = false;
      CheckOutMethod checkOutMethod = method;      
      switch(method) {
      case PreserveFrozen:
	if((work != null) && work.isFrozen()) {
	  checkOutMethod = CheckOutMethod.AllFrozen;
	  isFrozen = true;
	}
	break;

      case FrozenUpstream:
	isFrozen = !isRoot;
	break;

      case AllFrozen:
	isFrozen = true;
      }

      /* see if the check-out should be skipped, 
           and if skipped whether the node should be marked dirty */ 
      if(work != null) {
	switch(mode) {
	case OverwriteAll:
	  if((details != null) && 
	     (details.getOverallNodeState() == OverallNodeState.Identical) && 
	     (details.getOverallQueueState() == OverallQueueState.Finished) && 
	     work.getWorkingID().equals(vsn.getVersionID()) && 
	     (work.isFrozen() == isFrozen)) {
	    branch.removeLast();
	    return;
	  }
	  break;

	case KeepNewer:
	  if(!isRoot && (work.getWorkingID().compareTo(vsn.getVersionID()) > 0)) {
	    branch.removeLast();
	    dirty.add(name);
	    return;
	  }
	  break;

	case KeepModified:
	  if(!isRoot && (work.getWorkingID().compareTo(vsn.getVersionID()) >= 0)) {
	    branch.removeLast();
	    dirty.add(name);
	    return;
	  }
	}
      }

      /* process the upstream nodes */
      for(LinkVersion link : vsn.getSources()) {
	NodeID lnodeID = new NodeID(nodeID, link.getName());
	performCheckOut(false, lnodeID, link.getVersionID(), mode, checkOutMethod, stable, 
			branch, seen, dirty, timer);

	/* if any of the upstream nodes are dirty, 
  	     mark this node as dirty and make sure it isn't frozen */ 
	if(dirty.contains(link.getName())) {
	  dirty.add(name);
	  isFrozen = false;
	}
      }

      /* get the current timestamp */ 
      Date timestamp = Dates.now(); 

      {
	FileMgrClient fclient = getFileMgrClient();
	try {
	  /* remove the existing working area files before the check-out */ 
	  if(work != null) 
	    fclient.removeAll(nodeID, work.getSequences());	
	  
	  /* remove the to be checked-out working files,
	     if this is a dirty node with an enabled action */ 
	  if(dirty.contains(name) && vsn.isActionEnabled()) {
	    fclient.removeAll(nodeID, vsn.getSequences());
	  }
	  /* otherwise, check-out the files */
	  else {
	    fclient.checkOut(nodeID, vsn, isFrozen);
	  }
	}
	finally {
	  freeFileMgrClient(fclient);
	}
      }

      /* create a new working version and write it to disk */ 
      NodeMod nwork = new NodeMod(vsn, timestamp, isFrozen);
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
	  HashMap<NodeID,WorkingBundle> table = pWorkingBundles.get(name);
	  if(table == null) {
	    table = new HashMap<NodeID,WorkingBundle>();
	    pWorkingBundles.put(name, table);
	  }
	  table.put(nodeID, new WorkingBundle(nwork));
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
      }
	 
      /* update existing working version */ 
      else {
	/* update the working bundle */ 
	working.uVersion = nwork;

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
    finally {
      checkedInLock.readLock().unlock();  
      workingLock.writeLock().unlock();
    }

    /* pop the current node off of the end of the branch */ 
    branch.removeLast();
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Revert specific working area files to an earlier checked-in version of the files. <P> 
   * 
   * @param req 
   *   The revert files request.
   *
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to the revert the files.
   */ 
  public Object
  revertFiles
  ( 
   NodeRevertFilesReq req 
  ) 
  {
    NodeID nodeID = req.getNodeID();
    String name = nodeID.getName();
    TreeMap<String,VersionID> files = req.getFiles();

    TaskTimer timer = new TaskTimer("MasterMgr.revertFiles(): " + nodeID);

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();	      
      
      /* whether the working area files should be modifiable */ 
      boolean writeable = false;
      {
	timer.aquire(); 
	ReentrantReadWriteLock lock = getWorkingLock(nodeID);
	lock.readLock().lock();
	try {
	  timer.resume();

	  WorkingBundle bundle = getWorkingBundle(nodeID);
	  if(bundle == null) 
	    throw new PipelineException
	      ("Only nodes with working versions can have their files reverted!");

	  NodeMod mod = bundle.uVersion;
	  if(mod.isFrozen()) 
	    throw new PipelineException
	      ("The files associated with frozen node (" + nodeID + ") cannot be reverted!");

	  writeable = mod.isActionEnabled();
	}
	finally {
	  lock.readLock().unlock();
	}
      }

      /* check whether the checked-in version is currently online */ 
      {
	TreeSet<VersionID> ovids = new TreeSet<VersionID>();

	timer.aquire();
	synchronized(pOfflined) {
	  timer.resume();

	  TreeSet<VersionID> offline = pOfflined.get(name);
	  if(offline != null) {
	    TreeSet<VersionID> vids = new TreeSet<VersionID>(files.values());
	    for(VersionID vid : vids) {
	      if(offline.contains(vid))
		ovids.add(vid);
	    }
	  }
	}

	if(!ovids.isEmpty()) {
	  TreeMap<String,TreeSet<VersionID>> vsns = new TreeMap<String,TreeSet<VersionID>>();
	  vsns.put(name, ovids);

	  StringBuffer buf = new StringBuffer();
	  {
	    buf.append
	      ("Unable to revert files because the following checked-in versions " + 
	       "of node are currently offline:\n\n");
	    for(VersionID vid : ovids) 
	      buf.append(name + " v" + vid + "\n");
	    buf.append("\n");
	  }
	    
	  Object obj = requestRestore(new MiscRequestRestoreReq(vsns));
	  if(obj instanceof FailureRsp) {
	    FailureRsp rsp = (FailureRsp) obj;
	    buf.append
	      ("The request to restore these offline versions also failed:\n\n" + 
	       rsp.getMessage());	    
	  }
	  else {
	    buf.append
	      ("However, requests have been submitted to restore the offline versions " + 
	       "so that they may be used once they have been brought back online.");
	  }

	  throw new PipelineException(buf.toString());
	}
      }

      /* revert the files */ 
      {
	FileMgrClient fclient = getFileMgrClient();
	try {
	  fclient.revert(nodeID, files, writeable);
	}
	finally {
	  freeFileMgrClient(fclient);
	}
      }

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }  
    finally {
      pDatabaseLock.readLock().unlock();
    }  
  }

  /*----------------------------------------------------------------------------------------*/

  /** 
   * Replace the primary files associated one node with the primary files of another node. <P>
   * 
   * The two nodes must have exactly the same number of files in their primary file sequences
   * or the operation will fail. <P> 
   * 
   * @param req 
   *   The clone files request.
   *
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to the clone the files.
   */ 
  public Object
  cloneFiles
  ( 
   NodeCloneFilesReq req 
  ) 
  {
    NodeID sourceID = req.getSourceID();
    NodeID targetID = req.getTargetID();

    TaskTimer timer = 
      new TaskTimer("MasterMgr.cloneFiles(): " + sourceID + " to " + targetID);

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();	      
      
      FileSeq sourceSeq = null;
      {
	timer.aquire(); 
	ReentrantReadWriteLock lock = getWorkingLock(sourceID);
	lock.readLock().lock();
	try {
	  timer.resume();

	  WorkingBundle bundle = getWorkingBundle(sourceID);
	  if(bundle == null) 
	    throw new PipelineException
	      ("Only nodes with working versions can have their files cloned!");

	  NodeMod mod = bundle.uVersion;
	  sourceSeq = mod.getPrimarySequence();
	}
	finally {
	  lock.readLock().unlock();
	}
      }
      
      FileSeq targetSeq = null;
      boolean writeable = false;
      {
	timer.aquire(); 
	ReentrantReadWriteLock lock = getWorkingLock(targetID);
	lock.readLock().lock();
	try {
	  timer.resume();

	  WorkingBundle bundle = getWorkingBundle(targetID);
	  if(bundle == null) 
	    throw new PipelineException
	      ("Only nodes with working versions can have their files replaced!");

	  NodeMod mod = bundle.uVersion;
	  if(mod.isFrozen()) 
	    throw new PipelineException
	      ("The files associated with frozen node (" + targetID + ") " + 
	       "cannot be replaced!");

	  targetSeq = mod.getPrimarySequence();
	  writeable = mod.isActionEnabled();
	}
	finally {
	  lock.readLock().unlock();
	}
      }

      if(sourceSeq.hasFrameNumbers() != targetSeq.hasFrameNumbers()) 
	throw new PipelineException
	  ("Unable to clone the files associated with node (" + sourceID + "), because " +
	   "the file sequence associated with the node (" + sourceSeq + ") " + 
	   (sourceSeq.hasFrameNumbers() ? "has" : "does NOT have") + 
	   " frame numbers and the target file sequence (" + targetSeq + ") " +
	   (targetSeq.hasFrameNumbers() ? "has" : "does NOT have") + " frame numbers!");

      /* map the overlapping source to target files */ 
      TreeMap<File,File> files = new TreeMap<File,File>();
      if(sourceSeq.hasFrameNumbers()) {
	FrameRange sourceRange = sourceSeq.getFrameRange();
	FilePattern sourcePat = sourceSeq.getFilePattern();
	FilePattern targetPat = targetSeq.getFilePattern();
	int frames[] = targetSeq.getFrameRange().getFrameNumbers();
	int wk;
	for(wk=0; wk<frames.length; wk++) {
	  if(sourceRange.isValid(frames[wk])) 
	    files.put(sourcePat.getFile(frames[wk]), targetPat.getFile(frames[wk]));
	}
      }
      else {
	files.put(sourceSeq.getFile(0), targetSeq.getFile(0));
      }
      
      /* clone the files */ 
      {
	FileMgrClient fclient = getFileMgrClient();
	try {
	  fclient.clone(sourceID, targetID, files, writeable);
	}
	finally {
	  freeFileMgrClient(fclient);
	}
      }
	
      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }  
    finally {
      pDatabaseLock.readLock().unlock();
    }  
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Change the checked-in version upon which the working version is based without 
   * modifying the working version properties, links or associated files. <P> 
   * 
   * @param req 
   *   The evolve request.
   *
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to the evolve the node.
   */ 
  public Object
  evolve
  ( 
   NodeEvolveReq req 
  ) 
  {
    NodeID nodeID = req.getNodeID();
    TaskTimer timer = new TaskTimer("MasterMgr.evolve(): " + nodeID);

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();	

      /* verify the checked-in revision number */ 
      String name = nodeID.getName();
      VersionID vid = req.getVersionID();
      {
	timer.aquire();
	ReentrantReadWriteLock lock = getCheckedInLock(name);
	lock.readLock().lock();
	try {
	  timer.resume();	

	  TreeMap<VersionID,CheckedInBundle> checkedIn = getCheckedInBundles(name);
	  if(vid == null) 
	    vid = checkedIn.lastKey();
	  else if(!checkedIn.containsKey(vid))
	    throw new PipelineException 
	      ("No checked-in version (" + vid + ") of node (" + name + ") exists!"); 
	}
	catch(PipelineException ex) {
	  return new FailureRsp(timer, ex.getMessage());
	}
	finally {
	  lock.readLock().unlock();
	}  
      }

      /* check whether the checked-in version is currently online */ 
      {
	timer.aquire();
	synchronized(pOfflined) {
	  timer.resume();

	  TreeSet<VersionID> offline = pOfflined.get(name);
	  if((offline != null) && offline.contains(vid)) {
	    TreeSet<VersionID> vids = new TreeSet<VersionID>();
	    vids.add(vid);

	    TreeMap<String,TreeSet<VersionID>> vsns = 
	      new TreeMap<String,TreeSet<VersionID>>();
	    vsns.put(name, vids);

	    Object obj = requestRestore(new MiscRequestRestoreReq(vsns));
	    if(obj instanceof FailureRsp) {
	      FailureRsp rsp = (FailureRsp) obj;
	      throw new PipelineException
		("Unable to evolve to version (" + vid + ") of node (" + name + ") " +
		 "because the version is currently offline.  The request to restore " + 
		 "this version also failed:\n\n" + 
		 rsp.getMessage());
	    }
	    else {
	      throw new PipelineException
		("Unable to evolve to version (" + vid + ") of node (" + name + ")  " + 
		 "because the version is currently offline.  However, a request has been " + 
		 "submitted to restore the version so that it may be used once it has " + 
		 "been brought back online.");
	    }
	  }
	}
      }

      /* change the checked-in version number for the working version */ 
      timer.aquire();
      ReentrantReadWriteLock lock = getWorkingLock(nodeID);
      lock.writeLock().lock();
      try {
	timer.resume();

	/* set the revision number */ 
	WorkingBundle bundle = getWorkingBundle(nodeID);
	NodeMod mod = new NodeMod(bundle.uVersion);
	if(mod.isFrozen()) 
	  throw new PipelineException
	    ("The frozen node (" + nodeID + ") cannot be evolved!");
	mod.setWorkingID(vid);

	/* write the working version to disk */ 
	writeWorkingVersion(nodeID, mod);

	/* update the bundle */ 
	bundle.uVersion = mod;

	return new SuccessRsp(timer);
      }
      finally {
	lock.writeLock().unlock();
      }  
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   J O B   Q U E U E                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Submit the group of jobs needed to regenerate the selected {@link QueueState#Stale Stale}
   * files associated with the tree of nodes rooted at the given node. 
   *
   * @param req 
   *   The submit jobs request.
   * 
   * @return 
   *   <CODE>NodeSubmitJobsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to submit the jobs.
   */ 
  public Object
  submitJobs
  ( 
   NodeSubmitJobsReq req
  )
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();	

      /* get the current status of the nodes */ 
      NodeStatus status = performNodeOperation(new NodeOp(), req.getNodeID(), timer);

      /* submit the jobs */ 
      return submitJobsCommon(status, req.getFileIndices(),
			      req.getBatchSize(), req.getPriority(), req.getRampUp(), 
			      req.getSelectionKeys(), 
			      timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }    
  }

  /**
   * Resubmit the group of jobs needed to regenerate the selected 
   * {@link QueueState#Stale Stale} primary file sequences for the tree of nodes rooted at 
   * the given node. <P> 
   * 
   * This method is typically used to resubmit aborted or failed jobs.  The selected files
   * to regenerate are provided as target primary file sequences instead of file indices. 
   * The correct indices for each file defined by these target sequences will be computed
   * and new job batches will be submitted for these files. <P> 
   *
   * @param req 
   *   The submit jobs request.
   * 
   * @return 
   *   <CODE>NodeSubmitJobsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to submit the jobs.
   */ 
  public Object
  resubmitJobs
  ( 
   NodeResubmitJobsReq req
  )
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();	

      /* get the current status of the nodes */ 
      NodeStatus status = performNodeOperation(new NodeOp(), req.getNodeID(), timer);

      /* compute the file indices of the given target file sequences */ 
      TreeSet<Integer> indices = new TreeSet<Integer>();      
      {
	NodeDetails details = status.getDetails();
	if(details == null) 
	  throw new PipelineException
	    ("Cannot generate jobs for the checked-in node (" + status + ")!");
	
	TreeMap<File,Integer> fileIndices = new TreeMap<File,Integer>();
	{
	  NodeMod work = details.getWorkingVersion();
	  FileSeq fseq = work.getPrimarySequence(); 
	  int wk = 0;
	  for(File file : fseq.getFiles()) {
	    fileIndices.put(file, wk);
	    wk++;
	  }
	}

	for(FileSeq fseq : req.getTargetFileSequences()) {
	  for(File file : fseq.getFiles()) {
	    Integer idx = fileIndices.get(file);
	    if(idx != null) 
	      indices.add(idx);
	  }
	}
      }

      /* submit the jobs */ 
      return submitJobsCommon(status, indices, 
			      req.getBatchSize(), req.getPriority(), req.getRampUp(), 
			      req.getSelectionKeys(), timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }    
  }

  /**
   * Common code used by {@link #submitJobs submitJobs} and {@link #resubmitJobs resubmitJobs}
   * methods.
   * 
   * @param status
   *   The status of the tree of nodes. 
   * 
   * @param indices
   *   The file sequence indices of the files to regenerate.
   * 
   * @param batchSize 
   *   For parallel jobs, this overrides the maximum number of frames assigned to each job
   *   associated with the root node of the job submission.  
   * 
   * @param priority 
   *   Overrides the priority of jobs associated with the root node of the job submission 
   *   relative to other jobs.  
   * 
   * @param rampUp
   *   Overrides the ramp-up interval (in seconds) for the job.
   * 
   * @param selectionKeys 
   *   Overrides the set of selection keys an eligable host is required to have for jobs 
   *   associated with the root node of the job submission.
   * 
   * @param timer
   *   The task timer.
   */ 
  private NodeSubmitJobsRsp
  submitJobsCommon
  (
   NodeStatus status, 
   TreeSet<Integer> indices,
   Integer batchSize, 
   Integer priority, 
   Integer rampUp, 
   Set<String> selectionKeys, 
   TaskTimer timer 
  )
    throws PipelineException 
  {
    synchronized(pQueueSubmitLock) {
      /* generate jobs */ 
      TreeMap<NodeID,Long[]> extJobIDs = new TreeMap<NodeID,Long[]>();
      TreeMap<NodeID,Long[]> nodeJobIDs = new TreeMap<NodeID,Long[]>();
      TreeMap<NodeID,TreeSet<Long>> upsJobIDs = new TreeMap<NodeID,TreeSet<Long>>();
      TreeSet<Long> rootJobIDs = new TreeSet<Long>();
      TreeMap<Long,QueueJob> jobs = new TreeMap<Long,QueueJob>();
      
      submitJobs(status, indices, 
		 true, batchSize, priority, rampUp, selectionKeys, 
		 extJobIDs, nodeJobIDs, upsJobIDs, rootJobIDs, jobs, 
		 timer);
      
      if(jobs.isEmpty()) 
	throw new PipelineException
	  ("No new jobs where generated for node (" + status + ") or any node upstream " +
	   "of this node!");
      
      /* generate the root target file sequence for the job group */ 
      FileSeq targetSeq = null;
      {
	FilePattern fpat = null;
	TreeSet<Integer> frames = new TreeSet<Integer>();
	{
	  for(Long jobID : rootJobIDs) {
	    QueueJob job = jobs.get(jobID);
	    FileSeq fseq = job.getActionAgenda().getPrimaryTarget();

	    if(fpat == null) 
	      fpat = fseq.getFilePattern();

	    FrameRange range = fseq.getFrameRange();
	    if(range != null) {
	      int fnums[] = range.getFrameNumbers();
	      int wk;
	      for(wk=0; wk<fnums.length; wk++) 
		frames.add(fnums[wk]);
	    }
	  }
	}

	if(frames.isEmpty()) 
	  targetSeq = new FileSeq(fpat, null);
	else {
	  int step = Integer.MAX_VALUE;
	  {
	    Integer last = null;
	    for(Integer frame : frames) {
	      if(last != null) 
		step = Math.min(step, frame - last);
	      last = frame;
	    }
	  }

	  targetSeq = new FileSeq(fpat, new FrameRange(frames.first(), frames.last(), step));
	}
      }
      
      /* generate the list of external job IDs */ 
      TreeSet<Long> externalIDs = new TreeSet<Long>();
      for(QueueJob job : jobs.values()) 
	for(Long jobID : job.getSourceJobIDs()) 
	  if(!jobs.containsKey(jobID)) 
	    externalIDs.add(jobID);
      
      /* group the jobs */ 
      QueueJobGroup group = 
	new QueueJobGroup(pNextJobGroupID++, status.getNodeID(), 
			  targetSeq, rootJobIDs, externalIDs, 
			  new TreeSet<Long>(jobs.keySet()));
      pQueueMgrClient.groupJobs(group);
      
      /* submit the jobs */ 
      for(QueueJob job : jobs.values()) 
	pQueueMgrClient.submitJob(job);
      
      return new NodeSubmitJobsRsp(timer, group);
    }
  }  

  /**
   * Recursively submit jobs to the queue to regenerate the selected files. <P> 
   * 
   * The <CODE>batchSize</CODE>, <CODE>priority</CODE> or <CODE>selectionKeys</CODE> 
   * parameters (if not <CODE>null</CODE>) will override the settings when creating jobs 
   * associated with the root node of this submisssion.  However, the node will not be 
   * modified by this operation and all jobs associated with nodes upstream of the root node
   * of the submission will be unaffected. <P>
   * 
   * The <CODE>rootJobIDs</CODE>, <CODE>existingJobIDs</CODE>, <CODE>generatedJobIDs</CODE> 
   * and <CODE>jobs</CODE> arguments contain the results of the job submission process. <P>
   * 
   * @param status
   *   The current node status.
   * 
   * @param indices
   *   The file sequence indices of the files to regenerate.
   * 
   * @param isRoot
   *   The this the root node of the job submission tree?
   * 
   * @param batchSize 
   *   For parallel jobs, this overrides the maximum number of frames assigned to each job
   *   associated with the root node of the job submission.  
   * 
   * @param priority 
   *   Overrides the priority of jobs associated with the root node of the job submission 
   *   relative to other jobs.  
   * 
   * @param rampUp
   *   Overrides the ramp-up interval (in seconds) for the job.
   * 
   * @param selectionKeys 
   *   Overrides the set of selection keys an eligable host is required to have for jobs 
   *   associated with the root node of the job submission.
   * 
   * @param extJobIDs
   *   The per-file IDs of pre-existing jobs which will regenerate the files indexed 
   *   by working version node ID. 
   * 
   * @param nodeJobIDs
   *   The per-file IDs of the jobs generated by the job submission process indexed by 
   *   working version node ID. 
   * 
   * @param upsJobIDs
   *   The IDs of jobs upstream of node's without actions indexed by working version ID.
   * 
   * @param rootJobIDs
   *   The IDs of the jobs generated by the job submission process which are not a source 
   *   job by any of the other generated jobs.
   * 
   * @param jobs
   *   The table of jobs generated by the job submission process indexed by job ID.
   * 
   * @param timer
   *   The task timer.
   */
  private void 
  submitJobs
  (
   NodeStatus status, 
   TreeSet<Integer> indices,   
   boolean isRoot, 
   Integer batchSize, 
   Integer priority, 
   Integer rampUp, 
   Set<String> selectionKeys, 
   TreeMap<NodeID,Long[]> extJobIDs,   
   TreeMap<NodeID,Long[]> nodeJobIDs,   
   TreeMap<NodeID,TreeSet<Long>> upsJobIDs, 
   TreeSet<Long> rootJobIDs,    
   TreeMap<Long,QueueJob> jobs, 
   TaskTimer timer 
  ) 
    throws PipelineException
  {
    NodeID nodeID = status.getNodeID();

    NodeDetails details = status.getDetails();
    if(details == null) 
      throw new PipelineException
	("Cannot generate jobs for the checked-in node (" + status + ")!");
    
    NodeMod work = details.getWorkingVersion();

    /* collect upstream jobs (nodes without an action or with a disabled action) */ 
    if(!work.isActionEnabled()) {
      collectNoActionJobs(status, isRoot, 
			  extJobIDs, nodeJobIDs, upsJobIDs, rootJobIDs, 
			  jobs, timer);
      return;
    }
    
    /* generate jobs for node */ 
    int numFrames = work.getPrimarySequence().numFrames();

    int bsize = 0; 
    if(work.getBatchSize() != null) 
      bsize = work.getBatchSize();
    if(isRoot && (batchSize != null)) 
      bsize = batchSize;
    
    Long[] jobIDs = details.getJobIDs();
    QueueState[] queueStates = details.getQueueState();
    assert(jobIDs.length == queueStates.length);

    /* determine the frame batches */ 
    ArrayList<TreeSet<Integer>> batches = new ArrayList<TreeSet<Integer>>();
    switch(work.getExecutionMethod()) {
    case Serial:
      {
	/* does a job already exist or been generated for the node? */ 
	if((extJobIDs.get(nodeID) != null) || (nodeJobIDs.get(nodeID) != null))
	  return;

	/* are all frames Finished or Running/Queued/Paused? */ 
	boolean finished = true;
	boolean running  = true; 
	{
	  int idx;
	  for(idx=0; idx<queueStates.length; idx++) {
	    switch(queueStates[idx]) {
	    case Finished:
	      running = false;
	      break;
	      
	    case Running:
	    case Queued:
	    case Paused:
	      finished = false;
	      break;
	      
	    default:
	      finished = false;
	      running  = false;
	    }
	  }
	}


	if(running) {
	  extJobIDs.put(nodeID, jobIDs);
	}
	else if(!finished) {
	  TreeSet<Integer> frames = new TreeSet<Integer>(); 
	  int idx;
	  for(idx=0; idx<numFrames; idx++) 
	    frames.add(idx);
	  
	  batches.add(frames);
	}
      }
      break; 
      
    case Parallel:
      {
	/* determine which of the requested frames needs to be regenerated */ 
	TreeSet<Integer> regen = new TreeSet<Integer>();
	{
	  TreeSet<Integer> allIndices = new TreeSet<Integer>();
	  if(indices != null) 
	    allIndices.addAll(indices);
	  else {
	    int idx;
	    for(idx=0; idx<numFrames; idx++) 
	      allIndices.add(idx);
	  }

	  Long[] extIDs  = extJobIDs.get(nodeID);
	  Long[] njobIDs = nodeJobIDs.get(nodeID);

	  for(Integer idx : allIndices) {
	    if((idx < 0) || (idx >= numFrames)) 
	      throw new PipelineException
		("Illegal frame index (" + idx + ") given for node (" + nodeID + ") " + 
		 "during job submission!");

	    if((njobIDs == null) || (njobIDs[idx] == null)) {
	      switch(queueStates[idx]) {
	      case Finished:
		break;

	      case Running:
	      case Queued:
	      case Paused:
		{
		  if(extIDs == null) {
		    extIDs = new Long[numFrames];
		    extJobIDs.put(nodeID, extIDs);
		  }

		  if(extIDs[idx] == null) 
		    extIDs[idx] = jobIDs[idx];
		  assert(extIDs[idx] != null);
		}
		break;

	      case Stale:
	      case Aborted:
	      case Failed:
		regen.add(idx);
		break;

	      case Undefined:
		assert(false);
	      }
	    }
	  }
	}

	/* group the frames into batches */ 
	if(!regen.isEmpty()) {
	  TreeSet<Integer> batch = new TreeSet<Integer>();
	  for(Integer idx : regen) {
	    if(!batch.isEmpty() && 
	       (((bsize > 0) && (batch.size() >= bsize)) ||
		(idx > (batch.last()+1)))) {
	      batches.add(batch);
	      batch = new TreeSet<Integer>();
	    }	
	    
	    batch.add(idx);
	  }

	  batches.add(batch);
	}
      }
    }
    
    /* no batches to generate, collect upstream jobs */ 
    if(batches.isEmpty()) {
      collectNoActionJobs(status, isRoot, 
			  extJobIDs, nodeJobIDs, upsJobIDs, rootJobIDs, 
			  jobs, timer);
      return; 
    }

    /* generate jobs for each frame batch */ 
    else {
      if(work.isFrozen()) 
	throw new PipelineException
	  ("Cannot generate jobs for the frozen node (" + nodeID + ")!");
      
      for(TreeSet<Integer> batch : batches) {
	assert(!batch.isEmpty());
	
	/* determine the frame indices of the source nodes depended on by the 
	   frames of this batch */
	TreeMap<String,TreeSet<Integer>> sourceIndices = 
	  new TreeMap<String,TreeSet<Integer>>();
	{
	  for(LinkMod link : work.getSources()) {
	    NodeStatus lstatus = status.getSource(link.getName());
	    NodeDetails ldetails = lstatus.getDetails();
	    NodeMod lwork = ldetails.getWorkingVersion();
	    int lnumFrames = lwork.getPrimarySequence().numFrames();

	    switch(link.getRelationship()) {
	    case All:
	      {
		TreeSet<Integer> frames = new TreeSet<Integer>();
		int idx; 
		for(idx=0; idx<lnumFrames; idx++)
		  frames.add(idx);
		
		sourceIndices.put(link.getName(), frames);
	      }
	      break;
	      
	    case OneToOne:
	      {
		TreeSet<Integer> frames = new TreeSet<Integer>();
		for(Integer idx : batch) {
		  int lidx = idx + link.getFrameOffset();
		  
		  if((lidx < 0) || (lidx >= lnumFrames)) {
		    switch(work.getOverflowPolicy()) {
		    case Ignore:
		      break;
		      
		    case Abort:
		      throw new PipelineException
			("The frame offset (" + link.getFrameOffset() + ") for the link " +
			 "between target node (" + status + ") and source node " +
			 "(" + lstatus + ") overflows the frame range of the source node!");
		    }
		  }
		  else {
		    frames.add(lidx);
		  }
		}
		
		sourceIndices.put(link.getName(), frames);
	      }
	    }
	  }
	}
      
	/* generate jobs for the source frames first */ 
	for(LinkMod link : work.getSources()) {
	  TreeSet<Integer> lindices = sourceIndices.get(link.getName());
	  if((lindices != null) && (!lindices.isEmpty())) {
	    NodeStatus lstatus = status.getSource(link.getName());
	    submitJobs(lstatus, lindices, 
		       false, null, null, null, null, 
		       extJobIDs, nodeJobIDs, upsJobIDs, rootJobIDs, 
		       jobs, timer);
	  }
	}

	/* determine the source job IDs */ 
	TreeSet<Long> sourceIDs = new TreeSet<Long>();
	for(LinkMod link : work.getSources()) {
	  NodeStatus lstatus = status.getSource(link.getName());
	  NodeID lnodeID = lstatus.getNodeID();
	  
	  TreeSet<Long> upsIDs = upsJobIDs.get(lnodeID);
	  if(upsIDs != null) {
	    sourceIDs.addAll(upsIDs);
	  }
	  else {
	    TreeSet<Integer> lindices = sourceIndices.get(link.getName());
	    if((lindices != null) && !lindices.isEmpty()) {		  
	      Long[] nIDs = nodeJobIDs.get(lnodeID);
	      Long[] eIDs = extJobIDs.get(lnodeID);
	      
	      for(Integer idx : lindices) {
		if((nIDs != null) && (nIDs[idx] != null)) 
		  sourceIDs.add(nIDs[idx]);
		else if((eIDs != null) && (eIDs[idx] != null)) 
		  sourceIDs.add(eIDs[idx]);
	      }
	    }
	  }
	}
      
	/* generate a QueueJob for the batch */ 
	long jobID = pNextJobID++;
	{
	  FileSeq primaryTarget = 
	    new FileSeq(work.getPrimarySequence(), batch.first(), batch.last());

	  TreeSet<FileSeq> secondaryTargets = new TreeSet<FileSeq>();
	  for(FileSeq fseq : work.getSecondarySequences()) 
	    secondaryTargets.add(new FileSeq(fseq, batch.first(), batch.last()));

	  TreeMap<String,FileSeq> primarySources = new TreeMap<String,FileSeq>();
	  TreeMap<String,Set<FileSeq>> secondarySources = 
	    new TreeMap<String,Set<FileSeq>>();
	  for(String sname : sourceIndices.keySet()) {
	    TreeSet<Integer> lindices = sourceIndices.get(sname);
	    if((lindices != null) && !lindices.isEmpty()) {
	      NodeStatus lstatus = status.getSource(sname);
	      NodeDetails ldetails = lstatus.getDetails();
	      NodeMod lwork = ldetails.getWorkingVersion();

	      {
		FileSeq fseq = lwork.getPrimarySequence();
		primarySources.put(sname, 
				   new FileSeq(fseq, lindices.first(), lindices.last()));
	      }

	      {
		TreeSet<FileSeq> fseqs = new TreeSet<FileSeq>();
		for(FileSeq fseq : lwork.getSecondarySequences()) 
		  fseqs.add(new FileSeq(fseq, lindices.first(), lindices.last()));

		if(!fseqs.isEmpty()) 
		  secondarySources.put(sname, fseqs);
	      }	    
	    }
	  }

	  TreeMap<String,String> env = 
	    getToolsetEnvironment(nodeID.getAuthor(), nodeID.getView(), 
				  work.getToolset(), timer);

	  File dir = new File(PackageInfo.sProdDir, nodeID.getWorkingParent().getPath());

	  ActionAgenda agenda = 
	    new ActionAgenda(jobID, nodeID, 
			     primaryTarget, secondaryTargets, 
			     primarySources, secondarySources, 
			     work.getToolset(), env, dir);
	  
	  JobReqs jreqs = work.getJobRequirements();
	  {
	    if(isRoot && (priority != null)) 
	      jreqs.setPriority(priority);

	    if(isRoot && (rampUp != null)) 
	      jreqs.setRampUp(rampUp); 

	    if(isRoot && (selectionKeys != null)) {
	      jreqs.removeAllSelectionKeys(); 
	      jreqs.addSelectionKeys(selectionKeys);
	    }
	  }

	  QueueJob job = 
	    new QueueJob(agenda, work.getAction(), jreqs, sourceIDs);
		       
	  jobs.put(jobID, job);
	}

	/* if this is the root node, add the job to the set of root jobs */ 
	if(isRoot) 
	  rootJobIDs.add(jobID);

	/* update the node jobs table entries for the files which make up the batch */ 
	{
	  Long[] njobIDs = nodeJobIDs.get(nodeID);
	  if(njobIDs == null) {
	    njobIDs = new Long[numFrames];
	    nodeJobIDs.put(nodeID, njobIDs);
	  }
	
	  for(Integer idx : batch) 
	    njobIDs[idx] = jobID;
	}
      }
    }
  }

  /**
   * Collect the IDs of jobs upstream of a node without an Action or for a node with an 
   * Action which has already successfully finished.
   * 
   * The <CODE>rootJobIDs</CODE>, <CODE>existingJobIDs</CODE>, <CODE>generatedJobIDs</CODE> 
   * and <CODE>jobs</CODE> arguments contain the results of the job submission process. <P>
   * 
   * @param status
   *   The current node status.
   * 
   * @param isRoot
   *   The this the root node of the job submission tree?
   * 
   * @param extJobIDs
   *   The per-file IDs of pre-existing jobs which will regenerate the files indexed 
   *   by working version node ID. 
   * 
   * @param nodeJobIDs
   *   The per-file IDs of the jobs generated by the job submission process indexed by 
   *   working version node ID. 
   * 
   * @param upsJobIDs
   *   The IDs of jobs upstream of node's without actions indexed by working version ID.
   * 
   * @param rootJobIDs
   *   The IDs of the jobs generated by the job submission process which are not a source 
   *   job by any of the other generated jobs.
   * 
   * @param jobs
   *   The table of jobs generated by the job submission process indexed by job ID.
   * 
   * @param timer
   *   The task timer.
   */
  private void 
  collectNoActionJobs
  (
   NodeStatus status, 
   boolean isRoot, 
   TreeMap<NodeID,Long[]> extJobIDs,   
   TreeMap<NodeID,Long[]> nodeJobIDs,   
   TreeMap<NodeID,TreeSet<Long>> upsJobIDs, 
   TreeSet<Long> rootJobIDs,    
   TreeMap<Long,QueueJob> jobs, 
   TaskTimer timer 
  ) 
    throws PipelineException
  {
    NodeID nodeID = status.getNodeID();
    
    NodeDetails details = status.getDetails();
    if(details == null) 
      throw new PipelineException
	("Cannot generate jobs for the checked-in node (" + status + ")!");
    
    NodeMod work = details.getWorkingVersion();

    /* check to see if we've already processes this node */ 
    if(upsJobIDs.containsKey(nodeID))
      return;

    /* add a new entry for this node */ 
    TreeSet<Long> jobIDs = new TreeSet<Long>();
    upsJobIDs.put(nodeID, jobIDs);
    
    /* submit and collect the IDs of the jobs associated with the upstream nodes */ 
    for(LinkMod link : work.getSources()) {
      NodeStatus lstatus = status.getSource(link.getName());
      NodeID lnodeID = lstatus.getNodeID();

      submitJobs(lstatus, null, 
		 false, null, null, null, null, 
		 extJobIDs, nodeJobIDs, upsJobIDs, rootJobIDs, 
		 jobs, timer);
      
      /* external job IDs */ 
      {
	Long ids[] = extJobIDs.get(lnodeID);
	if(ids != null) {
	  int wk;
	  for(wk=0; wk<ids.length; wk++) {
	    if(ids[wk] != null) 
	      jobIDs.add(ids[wk]);
	  }
	}
      }
      
      /* generated job IDs (for nodes with actions) */ 
      {
	Long ids[] = nodeJobIDs.get(lnodeID);
	if(ids != null) {
	  int wk;
	  for(wk=0; wk<ids.length; wk++) {
	    if(ids[wk] != null) 
	      jobIDs.add(ids[wk]);
	  }
	}
      }
      
      /* collected upstream job IDs (for nodes without actions) */ 
      {
	TreeSet<Long> ids = upsJobIDs.get(lnodeID);
	if(ids != null) 
	    jobIDs.addAll(ids);
      }
    }

    /* if this is the root node, make the collected jobs the root jobs */ 
    if(isRoot) 
      rootJobIDs.addAll(jobIDs);
  }

  /**
   * Get the IDs of all active jobs associated with the given working version. <P> 
   * 
   * A job is considered active if it is {@link JobState#Queued Queued}, 
   * {@link JobState#Paused Paused} or {@link JobState#Running Running}.
   * 
   * @param nodeID
   *   The unique working version identifier. 
   * 
   * @param stamp
   *   The timestamp of when the working version was created.
   * 
   * @param fseq
   *   The primary file sequence.
   * 
   * @throws PipelineException
   *   If unable to determine the active job IDs.
   */ 
  private boolean
  hasActiveJobs
  ( 
   NodeID nodeID, 
   Date stamp, 
   FileSeq fseq
  )
    throws PipelineException 
  {
    ArrayList<Long> jobIDs = new ArrayList<Long>();
    ArrayList<JobState> jobStates = new ArrayList<JobState>();
    pQueueMgrClient.getJobStates(nodeID, stamp, fseq, jobIDs, jobStates);

    int wk = 0;
    for(JobState state : jobStates) {
      Long jobID = jobIDs.get(wk);
      if((state != null) && (jobID != null)) {
	switch(state) {
	case Queued:
	case Paused:
	case Running:
	  return true;
	}
      }

      wk++;
    }

    return false; 
  }

  /**
   * Kill any active jobs associated with the given working version. <P> 
   * 
   * A job is considered active if it is {@link JobState#Queued Queued}, 
   * {@link JobState#Paused Paused} or {@link JobState#Running Running}.
   * 
   * @param nodeID
   *   The unique working version identifier. 
   * 
   * @param stamp
   *   The timestamp of when the working version was created.
   * 
   * @param fseq
   *   The primary file sequence.
   * 
   * @throws PipelineException
   *   If unable to determine the active job IDs.
   */ 
  private void
  killActiveJobs
  ( 
   NodeID nodeID, 
   Date stamp, 
   FileSeq fseq
  )
    throws PipelineException 
  {
    ArrayList<Long> jobIDs = new ArrayList<Long>();
    ArrayList<JobState> jobStates = new ArrayList<JobState>();
    pQueueMgrClient.getJobStates(nodeID, stamp, fseq, jobIDs, jobStates);

    TreeSet<Long> activeIDs = new TreeSet<Long>();
    int wk = 0;
    for(JobState state : jobStates) {
      Long jobID = jobIDs.get(wk);
      if((state != null) && (jobID != null)) {
	switch(state) {
	case Queued:
	case Paused:
	case Running:
	  activeIDs.add(jobID);
	}
      }

      wk++;
    }

    if(!activeIDs.isEmpty()) 
      pQueueMgrClient.killJobs(nodeID.getAuthor(), activeIDs);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A D M I N I S T R A T I O N                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a database backup file. <P> 
   * 
   * The backup will not be perfomed until any currently running database operations have 
   * completed.  Once the databsae backup has begun, all new database operations will blocked
   * until the backup is complete.  The this reason, the backup should be performed during 
   * non-peak hours. <P> 
   * 
   * The database backup file will be named: <P> 
   * <DIV style="margin-left: 40px;">
   *   pipeline-db.<I>YYMMDD</I>.<I>HHMMSS</I>.tgz<P>
   * </DIV>
   * 
   * Where <I>YYMMDD</I>.<I>HHMMSS</I> is the year, month, day, hour, minute and second of 
   * the backup.  The backup file is a <B>gzip</B>(1) compressed <B>tar</B>(1) archive of
   * the {@link Glueable GLUE} format files which make of the persistent storage of the
   * Pipeline database. <P> 
   * 
   * Only privileged users may create a database backup. <P> 
   * 
   * @param req 
   *   The backup request.
   * 
   * @return 
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to perform the backup.
   */ 
  public Object
  backupDatabase
  ( 
   MiscBackupDatabaseReq req
  )
  {
    TaskTimer timer = new TaskTimer("MasterMgr.backupDatabase: " + req.getBackupFile());

    timer.aquire();
    pDatabaseLock.writeLock().lock();
    try {
      timer.resume();	

      /* write cached downstream links */ 
      writeAllDownstreamLinks();
      
      /* create the backup */ 
      {
	ArrayList<String> args = new ArrayList<String>();
	args.add("-zcvf");
	args.add(req.getBackupFile().toString());
	args.add("downstream"); 
	args.add("etc"); 
	args.add("repository"); 
	args.add("toolsets"); 
	args.add("working"); 
	
	Map<String,String> env = System.getenv();

	SubProcessLight proc = 
	  new SubProcessLight("BackupDatabase", "tar", args, env, PackageInfo.sNodeDir);
	try {
	  proc.start();
	  proc.join();
	  if(!proc.wasSuccessful()) 
	    throw new PipelineException
	      ("Unable to backing-up Pipeline database:\n\n" + 
	       "  " + proc.getStdErr());	
	}
	catch(InterruptedException ex) {
	  throw new PipelineException
	    ("Interrupted while backing-up Pipeline database!");
	}
      }


      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }  
    finally {
      pDatabaseLock.writeLock().unlock();
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get archive related information about the checked-in versions which match the 
   * given criteria. 
   * 
   * @param req 
   *   The query request.
   * 
   * @return 
   *   <CODE>MiscArchiveQueryRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to perform the query.
   */
  public Object
  archiveQuery
  (
   MiscArchiveQueryReq req
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();	

      String pattern      = req.getPattern();
      Integer maxArchives = req.getMaxArchives();
      
      if((maxArchives != null) && (maxArchives < 1)) 
	throw new PipelineException
	  ("The maximum number of archive volumes containing the checked-in version " +
	   "(" + maxArchives + ") must be positive!");

      /* get the node names which match the pattern */ 
      TreeMap<String,TreeMap<String,TreeSet<String>>> matches = 
	new TreeMap<String,TreeMap<String,TreeSet<String>>>();
      {
	timer.aquire();
	synchronized(pNodeTreeRoot) {
	  try {
	    timer.resume();
	    
	    Pattern pat = null;
	    if(pattern != null) 
	      pat = Pattern.compile(pattern);
	    
	    for(NodeTreeEntry entry : pNodeTreeRoot.values())
	      matchingCheckedInNodes(pat, "", entry, matches);
	  }
	  catch(PatternSyntaxException ex) {
	    return new FailureRsp(timer, 
				  "Illegal Node Name Pattern:\n\n" + ex.getMessage());
	  }
	}
      }
      
      /* process the matching nodes */ 
      ArrayList<ArchiveInfo> archiveInfo = new ArrayList<ArchiveInfo>();
      for(String name : matches.keySet()) {
	
	/* get the revision numbers and creation timestamps of the included versions */ 
	TreeMap<VersionID,Date> stamps = new TreeMap<VersionID,Date>();
	{
	  timer.aquire();
	  ReentrantReadWriteLock lock = getCheckedInLock(name);
	  lock.readLock().lock();  
	  try {
	    timer.resume();	
	    TreeMap<VersionID,CheckedInBundle> checkedIn = getCheckedInBundles(name);
	    for(VersionID vid : checkedIn.keySet()) 
	      stamps.put(vid, checkedIn.get(vid).uVersion.getTimeStamp());
	  }
	  catch(PipelineException ex) {
	    return new FailureRsp(timer, "Internal Error: " + ex.getMessage());
	  }
	  finally {
	    lock.readLock().unlock();  
	  }
	}
	
	/* process the matching checked-in versions */ 
	for(VersionID vid : stamps.keySet()) {
	  
	  /* check whether the version is currently online */ 
	  boolean isOnline = true;
	  {
	    timer.aquire();
	    synchronized(pOfflined) {
	      timer.resume();
	      TreeSet<VersionID> offline = pOfflined.get(name);
	      if(offline != null) 
		isOnline = !offline.contains(vid);
	    }
	  }

	  if(isOnline) {

	    /* get the number of archives which already contain the checked-in version */ 
	    int numArchives = 0;
	    String lastArchive = null;
	    {
	      timer.aquire();
	      synchronized(pArchivedIn) {
		timer.resume();
		
		TreeMap<VersionID,TreeSet<String>> versions = pArchivedIn.get(name);
		if(versions != null) {
		  TreeSet<String> archives = versions.get(vid);
		  if(archives != null) {
		    numArchives = archives.size();
		    lastArchive = archives.last();
		  }
		}
	      }
	    }
	    
	    /* only include the checked-in versions which aren't already members of the 
	       given maximum number of archive volumes */ 
	    if((maxArchives == null) || (numArchives <= maxArchives)) {
	      
	      /* get the timestamp of the latest archive containing the checked-in version */ 
	      Date archived = null;
	      if(lastArchive != null) {
		timer.aquire();
		synchronized(pArchivedOn) {
		  timer.resume();
		  archived = pArchivedOn.get(lastArchive);
		}
	      }
	      
	      Date checkedIn = stamps.get(vid);
	      ArchiveInfo info = new ArchiveInfo(name, vid, checkedIn, archived, numArchives);
	      archiveInfo.add(info);
	    }
	  }
	}
      }

      return new MiscArchiveQueryRsp(timer, archiveInfo);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }  
    finally {
      pDatabaseLock.readLock().unlock();
    }  
  }

  /**
   * Calculate the total size (in bytes) of the files associated with the given 
   * checked-in versions for archival purposes. <P> 
   * 
   * @param req
   *   The file sizes request.
   * 
   * @return
   *   <CODE>MiscGetArchiveSizesRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the file sizes.
   */ 
  public Object
  getArchiveSizes
  (
   MiscGetArchiveSizesReq req
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();	

      /* get the file sequences for the given checked-in versions */ 
      TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>> fseqs = 
	new TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>>();
      {
	TreeMap<String,TreeSet<VersionID>> versions = req.getVersions();
	for(String name : versions.keySet()) {
	  TreeMap<VersionID,TreeSet<FileSeq>> vfseqs = 
	    new TreeMap<VersionID,TreeSet<FileSeq>>();
	  fseqs.put(name, vfseqs);

	  timer.aquire();
	  ReentrantReadWriteLock lock = getCheckedInLock(name);
	  lock.readLock().lock(); 
	  try {
	    timer.resume();

	    TreeMap<VersionID,CheckedInBundle> checkedIn = getCheckedInBundles(name);
	    for(VersionID vid : versions.get(name)) 
	      vfseqs.put(vid, checkedIn.get(vid).uVersion.getSequences());
	  }
	  finally {
	    lock.readLock().unlock();
	  }
	}
      } 

      /* compute the sizes of the files */ 
      TreeMap<String,TreeMap<VersionID,Long>> sizes = null;
      {
	FileMgrClient fclient = getFileMgrClient();
	try {
	  sizes = fclient.getArchiveSizes(fseqs);
	}
	finally {
	  freeFileMgrClient(fclient);
	}
      }

      return new MiscGetSizesRsp(timer, sizes);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }  
    finally {
      pDatabaseLock.readLock().unlock();
    } 
  }

  /**
   * Archive the files associated with the given checked-in versions. <P> 
   * 
   * Only privileged users may create archives. <P> 
   * 
   * @param req 
   *   The archive request.
   * 
   * @return 
   *   <CODE>MiscArchiveRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to archive the files.
   */
  public Object
  archive
  (
   MiscArchiveReq req
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.writeLock().lock();
    try {
      timer.resume();	

      /* the archiver plugin to use */ 
      BaseArchiver archiver = req.getArchiver();

      /* validate the file sequences to be archived */ 
      TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>> fseqs = 
	new TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>>();
      TreeMap<String,TreeMap<VersionID,Long>> sizes = null;
      {
	TreeMap<String,TreeSet<VersionID>> versions = req.getVersions();

	/* recheck the sizes of the versions */ 
	{
	  Object obj = getArchiveSizes(new MiscGetArchiveSizesReq(versions));
	  if(obj instanceof FailureRsp) {
	    FailureRsp rsp = (FailureRsp) obj;
	    throw new PipelineException(rsp.getMessage());	
	  }
	  else {
	    MiscGetSizesRsp rsp = (MiscGetSizesRsp) obj;
	    sizes = rsp.getSizes();
	  }
	}

	/* make sure the versions exist and are not offline */ 
	long total = 0L;
	for(String name : versions.keySet()) {
	  TreeMap<VersionID,CheckedInBundle> checkedIn = getCheckedInBundles(name);
	  TreeSet<VersionID> offline = pOfflined.get(name);
	  TreeMap<VersionID,Long> vsizes = sizes.get(name);
	  for(VersionID vid : versions.get(name)) {
	    if((offline != null) && offline.contains(vid)) 
	      throw new PipelineException 
		("The checked-in version (" + vid + ") of node (" + name + ") cannot " + 
		 "be archived because it is currently offline!");
	    
	    CheckedInBundle bundle = checkedIn.get(vid);
	    if(bundle == null) 
	      throw new PipelineException 
		("No checked-in version (" + vid + ") of node (" + name + ") exists " + 
		 "to be archived!");

	    Long size = null;	    
	    if(vsizes != null) 
	      size = vsizes.get(vid);
	    if(size == null) 
	      throw new PipelineException
		("Unable to determine the size of the files associated with the " + 
		 "checked-in version (" + vid + ") of node (" + name + ")!");
	    total += size;
	    
	    TreeMap<VersionID,TreeSet<FileSeq>> fvsns = fseqs.get(name);
	    if(fvsns == null) {
	      fvsns = new TreeMap<VersionID,TreeSet<FileSeq>>();
	      fseqs.put(name, fvsns);
	    }
	    
	    fvsns.put(vid, bundle.uVersion.getSequences());
	  }
	}

	if(total > archiver.getCapacity()) 
	  throw new PipelineException
	    ("The total size of the files (" + total + " bytes) associated with the " +
	     "checked-in versions to be archived exceeded the capacity of the archiver " + 
	     "(" + archiver.getCapacity() + " bytes)!");
      }

      /* the archive name and time stamp */ 
      Date stamp = new Date();
      String archiveName = (req.getPrefix() + "-" + stamp.getTime());
      if(pArchivedOn.containsKey(archiveName)) 
	throw new PipelineException 
	  ("Somehow an archive named (" + archiveName + ") already exists!");

      /* create the archive volume by runing the archiver plugin and save any STDOUT output */
      {
	String output = null;
	{
	  FileMgrClient fclient = getFileMgrClient();
	  try {
	    output = fclient.archive(archiveName, fseqs, archiver);
	  }
	  finally {
	    freeFileMgrClient(fclient);
	  }
	}

	if(output != null) {
	  File file = new File(PackageInfo.sNodeDir, 
			       "archives/output/archive/" + archiveName);
	  try {
	    FileWriter out = new FileWriter(file);
	    out.write(output);
	    out.flush();
	    out.close();
	  }
	  catch(IOException ex) {
	    throw new PipelineException
	      ("I/O ERROR: \n" + 
	       "  While attempting to write the archive STDOUT file (" + file + ")...\n" + 
	       "    " + ex.getMessage());
	  }
	}
      }

      /* register the newly created archive */ 
      {
	ArchiveVolume vol = new ArchiveVolume(archiveName, stamp, fseqs, sizes, archiver);
	writeArchive(vol);
	pArchivedOn.put(archiveName, stamp);
      }

      /* update the cached archive named for each checked-in version */ 
      for(String name : fseqs.keySet()) {
	TreeMap<VersionID,TreeSet<String>> aversions = pArchivedIn.get(name);
	if(aversions == null) {
	  aversions = new TreeMap<VersionID,TreeSet<String>>();
	  pArchivedIn.put(name, aversions);
	}
	
	for(VersionID vid : fseqs.get(name).keySet()) {
	  TreeSet<String> anames = aversions.get(vid);
	  if(anames == null) {
	    anames = new TreeSet<String>();
	    aversions.put(vid, anames);
	  }

	  anames.add(archiveName);
	}
      }

      return new MiscArchiveRsp(timer, archiveName);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }  
    finally {
      pDatabaseLock.writeLock().unlock();
    }
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get offline related information about the checked-in versions which match the 
   * given criteria. <P> 
   * 
   * @param req 
   *   The query request.
   * 
   * @return 
   *   <CODE>MiscOfflineQueryRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to perform the query.
   */
  public Object
  offlineQuery
  (
   MiscOfflineQueryReq req
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();	

      String pattern      = req.getPattern();
      Integer exclude     = req.getExcludeLatest();
      Integer minArchives = req.getMinArchives();
      boolean unusedOnly  = req.getUnusedOnly();

      if((exclude != null) && (exclude < 0)) 
	throw new PipelineException
	  ("The number of latest checked-in versions of the node to exclude " + 
	   "(" + exclude + ") cannot be negative!");

      if((minArchives != null) && (minArchives < 0)) 
	throw new PipelineException
	  ("The minimum number of archive volumes containing the checked-in version " +
	   "(" + minArchives + ") cannot be negative!");

      /* get the node names which match the pattern */ 
      TreeMap<String,TreeMap<String,TreeSet<String>>> matches = 
	new TreeMap<String,TreeMap<String,TreeSet<String>>>();
      {
	timer.aquire();
	synchronized(pNodeTreeRoot) {
	  try {
	    timer.resume();
	    
	    Pattern pat = null;
	    if(pattern != null) 
	      pat = Pattern.compile(pattern);
	    
	    for(NodeTreeEntry entry : pNodeTreeRoot.values())
	      matchingCheckedInNodes(pat, "", entry, matches);
	  }
	  catch(PatternSyntaxException ex) {
	    throw new PipelineException 
	      ("Illegal Node Name Pattern:\n\n" + ex.getMessage());
	  }
	}
      }
      
      /* process the matching nodes */ 
      ArrayList<OfflineInfo> offlineInfo = new ArrayList<OfflineInfo>();
      VersionID latestID = null;
      for(String name : matches.keySet()) {
	
	/* get the revision numbers of the included versions */ 
	TreeSet<VersionID> vids = new TreeSet<VersionID>();
	{	    
	  timer.aquire();
	  ReentrantReadWriteLock lock = getCheckedInLock(name);
	  lock.readLock().lock();  
	  try {
	    timer.resume();	
	    TreeMap<VersionID,CheckedInBundle> checkedIn = getCheckedInBundles(name);
	    int wk = 0;
	    for(VersionID vid : checkedIn.keySet()) {
	      if((exclude != null) && (wk >= (checkedIn.size() - exclude)))
		break;
	      vids.add(vid);
	      wk++;
	    }

	    latestID = checkedIn.lastKey();
	  }
	  catch(PipelineException ex) {
	    return new FailureRsp(timer, "Internal Error: " + ex.getMessage());
	  }
	  finally {
	    lock.readLock().unlock();  
	  }
	}

	/* remove any already offline versions */ 
	synchronized(pOfflined)	{
	  TreeSet<VersionID> offlined = pOfflined.get(name);
	  if(offlined != null) {
	    for(VersionID ovid : offlined) 
	      vids.remove(ovid);
	  }	  
	}
	
	/* process the matching checked-in versions */ 
	for(VersionID vid : vids) {

	  /* get the number of archives which already contain the checked-in version */ 
	  int numArchives = 0;
	  String lastArchive = null;
	  {
	    timer.aquire();
	    synchronized(pArchivedIn) {
	      timer.resume();
	      
	      TreeMap<VersionID,TreeSet<String>> versions = pArchivedIn.get(name);
	      if(versions != null) {
		TreeSet<String> archives = versions.get(vid);
		if(archives != null) {
		  numArchives = archives.size();
		  lastArchive = archives.last();
		}
	      }
	    }
	  }

	  /* only include the checked-in versions are members of at least the given 
	     minimum number of archives  */ 
	  if((minArchives == null) || (numArchives >= minArchives)) {

	    /* get the timestamp of the latest archive containing the checked-in version */ 
	    Date archived = null;
	    if(lastArchive != null) {
	      timer.aquire();
	      synchronized(pArchivedOn) {
		timer.resume();
		archived = pArchivedOn.get(lastArchive);
	      }
	    }
	    
	    /* get the number of working versions based on the checked-in version and 
	       the timestamp of when the latest working version was checked-out */ 
	    int numWorking = 0;
	    Date checkedOut = null;
	    String lastAuthor = null;
	    String lastView = null;
	    boolean canOffline = true;
	    {	      
	      TreeMap<String,TreeSet<String>> areas = matches.get(name);
	      for(String author : areas.keySet()) {
		TreeSet<String> views = areas.get(author);
		for(String view : views) {
		  NodeID nodeID = new NodeID(author, view, name);
		  
		  timer.aquire();
		  ReentrantReadWriteLock lock = getWorkingLock(nodeID);
		  lock.readLock().lock();
		  try {
		    timer.resume();	

		    WorkingBundle bundle = getWorkingBundle(nodeID);
		    NodeMod mod = bundle.uVersion;
		    if(vid.equals(mod.getWorkingID())) {
		      if((checkedOut == null) || 
			 (checkedOut.compareTo(mod.getTimeStamp()) < 0)) {
			checkedOut = mod.getTimeStamp();
			lastAuthor = author;
			lastView   = view; 
		      }

		      canOffline = false;
		      numWorking++;
		    }		      
		  } 
		  finally {
		    lock.readLock().unlock();
		  } 

		  if(vid.equals(latestID))
		    canOffline = false;
		}
	      }
	    }

	    /* only include checked-in version which do not have more than the given
	       maximum number of working versions based on the checked-in version */ 
	    if(!unusedOnly || (unusedOnly && canOffline)) {
	      OfflineInfo info = 
		new OfflineInfo(name, vid, checkedOut, lastAuthor, lastView, numWorking, 
				archived, numArchives, canOffline);
	      offlineInfo.add(info);
	    }
	  }
	}	
      }

      return new MiscOfflineQueryRsp(timer, offlineInfo);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }    
    finally {
      pDatabaseLock.readLock().unlock();
    }  
  }

  /**
   * Get the revision nubers of all offline checked-in versions of the given node. <P> 
   * 
   * @param req
   *   The file sizes request.
   * 
   * @return
   *   <CODE>NodeGetOfflineVersionIDsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the file sizes.
   */ 
  public Object
  getOfflineVersionIDs
  (
   NodeGetOfflineVersionIDsReq req
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();	

      /* the currently offline revision numbers */ 
      TreeSet<VersionID> offlined = new TreeSet<VersionID>();
      timer.aquire();
      synchronized(pOfflined) {
	timer.resume();
	
	String name = req.getName();
	if(pOfflined.get(name) != null)
	  offlined.addAll(pOfflined.get(name));
      }
      
      return new NodeGetOfflineVersionIDsRsp(timer, offlined);
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }  
  }

  /**
   * Calculate the total size (in bytes) of the files associated with the given 
   * checked-in versions for offlining purposes. <P> 
   * 
   * File sizes reflect the actual amount of bytes that will be freed from disk if the 
   * given checked-in versions are offlined.  A file will only contribute to this freed
   * size if it a regular file and there are no symbolic links from later online versions 
   * which target and which are not associated with the given versions. <P> 
   * 
   * @param req
   *   The file sizes request.
   * 
   * @return
   *   <CODE>MiscGetOfflineSizesRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the file sizes.
   */ 
  public Object
  getOfflineSizes
  (
   MiscGetOfflineSizesReq req
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();	

      /* determine which file contribute the to offlines size */ 
      TreeMap<String,TreeMap<VersionID,TreeSet<File>>> contribute = 
	new TreeMap<String,TreeMap<VersionID,TreeSet<File>>>();
      {
	TreeMap<String,TreeSet<VersionID>> versions = req.getVersions();
	for(String name : versions.keySet()) {

	  /* the currently offline revision numbers */ 
	  TreeSet<VersionID> offlined = new TreeSet<VersionID>();
	  timer.aquire();
	  synchronized(pOfflined) {
	    timer.resume();

	    if(pOfflined.get(name) != null)
	      offlined.addAll(pOfflined.get(name));
	  }

	  timer.aquire();
	  ReentrantReadWriteLock lock = getCheckedInLock(name);
	  lock.readLock().lock(); 
	  try {
	    timer.resume();

	    TreeMap<VersionID,CheckedInBundle> checkedIn = getCheckedInBundles(name);
	    ArrayList<VersionID> vids = new ArrayList<VersionID>(checkedIn.keySet());

	    /* process the to be offlined versions */ 
	    TreeSet<VersionID> toBeOfflined = versions.get(name);
	    for(VersionID vid : toBeOfflined) {

	      /* ignore currently offline versions */ 
	      if(!offlined.contains(vid)) {
		CheckedInBundle bundle = checkedIn.get(vid);
		if(bundle == null) 
		  throw new PipelineException 
		    ("No checked-in version (" + vid + ") of node (" + name + ") exists!");
		NodeVersion vsn = checkedIn.get(vid).uVersion;
		int vidx = vids.indexOf(vid);
		
		/* determine which files contributes to the offlined size */ 
		TreeMap<File,Boolean[]> novelty = noveltyByFile(checkedIn);
		for(File file : novelty.keySet()) {

		  /* we are only concerned with files exist and are new */ 
		  Boolean[] isNovel = novelty.get(file);
		  if((isNovel[vidx] != null) && isNovel[vidx]) {

		    /* step through later versions to determine whether the current file
		       should contribute to the offlined size */ 
		    boolean selected = true;
		    {
		      int vk;
		      for(vk=vidx+1; vk<isNovel.length; vk++) {
			VersionID nvid = vids.get(vk);
			
			if((isNovel[vk] == null) || isNovel[vk]) {
			  break;
			}
			else if(!toBeOfflined.contains(nvid) && !offlined.contains(nvid)) {
			  selected = false;
			  break;
			}
		      }
		    }

		    /* add the current file to those which contribute to the offlined size */ 
		    if(selected) {
		      TreeMap<VersionID,TreeSet<File>> cversions = contribute.get(name);
		      if(cversions == null) {
			cversions = new TreeMap<VersionID,TreeSet<File>>();
			contribute.put(name, cversions);
		      }

		      TreeSet<File> cfiles = cversions.get(vid);
		      if(cfiles == null) {
			cfiles = new TreeSet<File>();
			cversions.put(vid, cfiles);
		      }

		      cfiles.add(file);
		    }
		  }
		}
	      }
	    }
	  }
	  finally {
	    lock.readLock().unlock();
	  }
	}
      }

      /* compute the sizes of the files */
      TreeMap<String,TreeMap<VersionID,Long>> sizes = null;
      {
	FileMgrClient fclient = getFileMgrClient();
	try {
	  sizes = fclient.getOfflineSizes(contribute);
	}
	finally {
	  freeFileMgrClient(fclient);
	}
      }
 
      return new MiscGetSizesRsp(timer, sizes);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }  
    finally {
      pDatabaseLock.readLock().unlock();
    }  
  }

  /**
   * Remove the repository files associated with the given checked-in versions. <P> 
   * 
   * All checked-in versions to be offlined must have prevously been included in at least
   * one archive. <P> 
   * 
   * The offline operation will not be perfomed until any currently running database 
   * operations have completed.  Once the offline operation has begun, all new database 
   * operations will blocked until the offline operation is complete.  The this reason, 
   * this should be performed during non-peak hours. <P> 
   * 
   * Only privileged users may offline checked-in versions. <P> 
   * 
   * @param req 
   *   The offline request.
   * 
   * @return 
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to remove the files.
   */
  public Object
  offline
  (
   MiscOfflineReq req
  ) 
  {
    TaskTimer timer = new TaskTimer("MasterMgr.offline()");

    timer.aquire();
    pDatabaseLock.writeLock().lock();
    try {
      timer.resume();	
  
      TreeMap<String,TreeSet<VersionID>> versions = req.getVersions();
      for(String name : versions.keySet()) {
	TreeSet<VersionID> offlined = pOfflined.get(name);	
	TreeMap<VersionID,CheckedInBundle> checkedIn = getCheckedInBundles(name);
	ArrayList<VersionID> vids = new ArrayList<VersionID>(checkedIn.keySet());
	TreeMap<File,Boolean[]> novelty = noveltyByFile(checkedIn);

	/* process the to be offlined versions */ 
	TreeSet<VersionID> toBeOfflined = versions.get(name);
	for(VersionID vid : toBeOfflined) { 

	  /* ignore currently offline versions */
	  if((offlined == null) || !offlined.contains(vid)) {
	    CheckedInBundle bundle = checkedIn.get(vid);
	    if(bundle == null) 
	      throw new PipelineException 
		("No checked-in version (" + vid + ") of node (" + name + ") exists!");
	    NodeVersion vsn = checkedIn.get(vid).uVersion;
	    int vidx = vids.indexOf(vid);
	    
	    /* make sure at lease one archive volume contains the version */ 
	    {
	      boolean hasBeenArchived = false;
	      TreeMap<VersionID,TreeSet<String>> aversions = pArchivedIn.get(name);
	      if(aversions != null) {
		TreeSet<String> archives = aversions.get(vid);
		if((archives != null) && !archives.isEmpty()) 
		  hasBeenArchived = true;
	      }

	      if(!hasBeenArchived) 
		throw new PipelineException
		  ("The checked-in version (" + vid + ") of node (" + name + ") cannot be " + 
		   "offlined until it has been archived at least once!");
	    }	    

	    /* make sure it is not being referenced by an existing working version */ 
	    {
	      TreeMap<String,TreeSet<String>> views = getViewsContaining(name);
	      for(String author : views.keySet()) {
		for(String view : views.get(author)) {
		  NodeID nodeID = new NodeID(author, view, name);
		  NodeMod mod = getWorkingBundle(nodeID).uVersion;
		  if(vid.equals(mod.getWorkingID())) 
		    throw new PipelineException
		      ("The checked-in version (" + vid + ") of node (" + name + ") " + 
		       "cannot be offlined because a working version currently exists " + 
		       "which references the checked-in version in the working area " + 
		       "(" + view + ") owned by user (" + author + ")!");
		  
		  if(vid.equals(checkedIn.lastKey())) 
		    throw new PipelineException
		      ("The latest checked-in version (" + vid + ") of node " + 
		       "(" + name + ") cannot be offlined because a working version " + 
		       "currently exists in the working area (" + view + ") owned by " + 
		       "user (" + author + ")!");
		}
	      }
	    }

	    /* determine which symlinks target the to be offlined files */ 
	    TreeMap<File,TreeSet<VersionID>> symlinks = 
	      new TreeMap<File,TreeSet<VersionID>>();
	    {
	      for(File file : novelty.keySet()) {
		Boolean[] isNovel = novelty.get(file);

		/* the file exists for this version */ 
		if(isNovel[vidx] != null) {

		  /* determine whether later files/symlinks needs to be relocated */ 
		  boolean selected = false;
		  if(isNovel[vidx])
		    selected = true;
		  else {
		    int vk;
		    for(vk=vidx-1; vk>=0; vk--) {
		      VersionID nvid = vids.get(vk);
		      if((offlined != null) && offlined.contains(nvid)) {
			selected = true;
			break;
		      }
		      else if(isNovel[vk]) 
			break;
		    }
		  }

		  /* determine which versions need relocation */ 
		  if(selected) {
		    int vk;
		    for(vk=vidx+1; vk<isNovel.length; vk++) {
		      VersionID nvid = vids.get(vk);
		      
		      if((isNovel[vk] == null) || isNovel[vk]) 
			break;
		      else if((offlined == null) || !offlined.contains(nvid)) {
			TreeSet<VersionID> svids = symlinks.get(file);
			if(svids == null) {
			  svids = new TreeSet<VersionID>();
			  symlinks.put(file, svids);
			}
			svids.add(nvid);
		      }
		    }
		  }
		}
	      }
	    }
	      	    
	    /* offline the files */ 	
	    {
	      FileMgrClient fclient = getFileMgrClient();
	      try {
		fclient.offline(name, vid, symlinks);
	      }
	      finally {
		freeFileMgrClient(fclient);
	      }
	    }

	    /* update the currently offlined revision numbers */ 
	    {
	      if(offlined == null) {
		offlined = new TreeSet<VersionID>();
		pOfflined.put(name, offlined);
	      }

	      offlined.add(vid);
	    }
	  }
	}
      }

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }  
    finally {
      pDatabaseLock.writeLock().unlock();
    }
  }

  /**
   * Rearrange the per-file novelty flags to be indexed by filename and version index. <P> 
   * 
   * No locking is performed!  This method should only be called from either 
   * {@link #getOfflineSize getOfflineSize} or {@link #offline offline}!
   */ 
  private TreeMap<File,Boolean[]>
  noveltyByFile 
  (
   TreeMap<VersionID,CheckedInBundle> checkedIn
  ) 
  {
    TreeMap<File,Boolean[]> novelty = new TreeMap<File,Boolean[]>();

    int numVersions = checkedIn.keySet().size();
    int vk = 0;
    for(VersionID vid : checkedIn.keySet()) {
      NodeVersion vsn = checkedIn.get(vid).uVersion;
      for(FileSeq fseq : vsn.getSequences()) {
	boolean isNovel[] = vsn.isNovel(fseq);
	int fk = 0;
	for(File file : fseq.getFiles()) {
	  Boolean[] novel = novelty.get(file);
	  if(novel == null) {
	    novel = new Boolean[numVersions];
	    novelty.put(file, novel);
	  }
	  novel[vk] = isNovel[fk];

	  fk++;
	}
      }

      vk++;
    }

    return novelty;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names and revision numbers of the offline checked-in versions who's names 
   * match the given criteria. <P> 
   * 
   * @param req 
   *   The query request.
   * 
   * @return 
   *   <CODE>MiscRestoreQueryRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to perform the query.
   */
  public Object
  restoreQuery
  (
   MiscRestoreQueryReq req
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();	

      String pattern = req.getPattern();

      /* get versions which match the pattern */ 
      TreeMap<String,TreeSet<VersionID>> versions = new TreeMap<String,TreeSet<VersionID>>();
      {
	timer.aquire();
	synchronized(pOfflined) {
	  try {
	    timer.resume();
	    
	    Pattern pat = null;
	    if(pattern != null) 
	      pat = Pattern.compile(pattern);
	    
	    for(String name : pOfflined.keySet()) {
	      if((pat == null) || pat.matcher(name).matches()) 
		versions.put(name, new TreeSet<VersionID>(pOfflined.get(name)));
	    }
	  }
	  catch(PatternSyntaxException ex) {
	    throw new PipelineException 
	      ("Illegal Node Name Pattern:\n\n" + ex.getMessage());
	  }
	}
      }
      
      return new MiscRestoreQueryRsp(timer, versions);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }    
    finally {
      pDatabaseLock.readLock().unlock();
    }  
  }

  /**
   * Submit a request to restore the given set of checked-in versions.
   *
   * @param req
   *   The request.
   * 
   * @return 
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to submit the request
   */ 
  public Object  
  requestRestore
  (
   MiscRequestRestoreReq req
  ) 
  {
    TaskTimer timer = new TaskTimer("MasterMgr.requestRestore()");

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();

      /* filter any versions which are not offline */ 
      TreeMap<String,TreeSet<VersionID>> vsns = new TreeMap<String,TreeSet<VersionID>>();
      {
	TreeMap<String,TreeSet<VersionID>> versions = req.getVersions();

	timer.aquire();
	synchronized(pOfflined) {
	  timer.resume();
	  for(String name : versions.keySet()) {
	    TreeSet<VersionID> offline = pOfflined.get(name);
	    if(offline != null) {
	      for(VersionID vid : versions.get(name)) {
		if(offline.contains(vid)) {
		  TreeSet<VersionID> vids = vsns.get(name);
		  if(vids == null) {
		    vids = new TreeSet<VersionID>();
		    vsns.put(name, vids);
		  }
		  vids.add(vid);
		}
	      }
	    }
	  }	  
	}
      }

      /* add the requests, replacing any current requests for the same versions */ 
      timer.aquire();
      synchronized(pRestoreReqs) {
	timer.resume();

	Date now = new Date();

	for(String name : vsns.keySet()) {
	  TreeMap<VersionID,RestoreRequest> restores = pRestoreReqs.get(name);
	  for(VersionID vid : vsns.get(name)) {
	    RestoreRequest rr = new RestoreRequest(now);
	    if(restores == null) {
	      restores = new TreeMap<VersionID,RestoreRequest>();
	      pRestoreReqs.put(name, restores);
	    }
	    restores.put(vid, rr);
	  }
	}
	
	return new SuccessRsp(timer);
      }
    }
    finally {
      try {
	writeRestoreReqs();
      }
      catch(PipelineException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Warning,
	   ex.getMessage());
	LogMgr.getInstance().flush();
      }

      pDatabaseLock.readLock().unlock();
    }
  }

  /**
   * Deny the request to restore the given set of checked-in versions.
   *
   * @param req
   *   The request.
   * 
   * @return 
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to submit the request
   */ 
  public Object  
  denyRestore
  (
   MiscDenyRestoreReq req
  ) 
  {
    TaskTimer timer = new TaskTimer("MasterMgr.denyRestore()");

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pRestoreReqs) {
	timer.resume();

	TreeMap<String,TreeSet<VersionID>> versions = req.getVersions();
	for(String name : versions.keySet()) {
	  TreeMap<VersionID,RestoreRequest> reqs = pRestoreReqs.get(name);
	  if(reqs != null) {
	    for(VersionID vid : versions.get(name)) {
	      RestoreRequest rr = reqs.get(vid);
	      if(rr != null) {
		switch(rr.getState()) {
		case Pending: 
		  rr.denied();
		}
	      }
	    }
	  }	
	}
	
	return new SuccessRsp(timer);
      }
    }
    finally {
      try {
	writeRestoreReqs();
      }
      catch(PipelineException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Warning,
	   ex.getMessage());
	LogMgr.getInstance().flush();
      }

      pDatabaseLock.readLock().unlock();
    }
  }

  /**
   * Get the names and revision numbers of the checked-in versions which users have 
   * requested to be restored from an previously created archive. <P> 
   * 
   * @return 
   *   <CODE>MiscGetRestoreRequestsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the requests.
   */
  public Object
  getRestoreRequests()   
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pRestoreReqs) {
	timer.resume();

	TreeMap<String,TreeSet<VersionID>> dead = new TreeMap<String,TreeSet<VersionID>>();
	long now = System.currentTimeMillis(); 
	for(String name : pRestoreReqs.keySet()) {
	  for(VersionID vid : pRestoreReqs.get(name).keySet()) {
	    RestoreRequest rr = pRestoreReqs.get(name).get(vid);
	    switch(rr.getState()) {
	    case Restored:
	    case Denied:
	      if((rr.getResolvedStamp().getTime()+sRestoreCleanupInterval) < now) {
		TreeSet<VersionID> vids = dead.get(name);
		if(vids == null) {
		  vids = new TreeSet<VersionID>();
		  dead.put(name, vids);
		}
		vids.add(vid);
	      }
	    }
	  }
	}
	
	if(!dead.isEmpty()) {
	  for(String name : dead.keySet()) {
	    TreeMap<VersionID,RestoreRequest> reqs = pRestoreReqs.get(name);
	    for(VersionID vid : dead.get(name)) 
	      reqs.remove(vid);

	    if(reqs.isEmpty()) 
	      pRestoreReqs.remove(name);
	  }

	  writeRestoreReqs();
	}
	
	return new MiscGetRestoreRequestsRsp(timer, pRestoreReqs);
      }
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }  
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }

  /**
   * Calculate the total size (in bytes) of the files associated with the given 
   * checked-in versions for restoration purposes. <P> 
   * 
   * File sizes reflect the total amount of bytes of disk space that will be need to be 
   * available in order to restore the given offline checked-in versions.  The actual 
   * amount of disk space used after the completion of the restore operation may be less
   * than this amount if some of the restored files are identical to the corresponding
   * files in an earlier online version.  <P> 
   * 
   * @param req
   *   The file sizes request.
   * 
   * @return
   *   <CODE>MiscGetRestoreSizesRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the file sizes.
   */ 
  public Object
  getRestoreSizes
  (
   MiscGetRestoreSizesReq req
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();	

      TreeMap<String,TreeSet<VersionID>> versions = req.getVersions();

      /* get the names of all archives which contain any of the given versions */ 
      TreeSet<String> anames = new TreeSet<String>();
      {
	timer.aquire();
	synchronized(pArchivedIn) {
	  timer.resume();

	  for(String name : versions.keySet()) {
	    TreeMap<VersionID,TreeSet<String>> varchives = pArchivedIn.get(name);
	    if(varchives == null) 
	      throw new PipelineException 
		("Cannot compute restored size of checked-in node (" + name + ") " + 
		 "since no versions have ever been archived!");

	    for(VersionID vid : versions.get(name)) {
	      TreeSet<String> vanames = varchives.get(vid);
	      if(vanames == null) 
		throw new PipelineException 
		("Cannot compute restored size of checked-in version (" + vid + ") of " + 
		 "node (" + name + ") since it has been archived!");
	      
	      anames.addAll(vanames);
	    }
	  }
	}
      }
	  
      /* lookup the sizes of the versions from the archives */ 
      TreeMap<String,TreeMap<VersionID,Long>> sizes = 
	new TreeMap<String,TreeMap<VersionID,Long>>();
      {
	ArrayList<String> names = new ArrayList<String>();
	ArrayList<VersionID> vids = new ArrayList<VersionID>();
	for(String name : versions.keySet()) {
	  for(VersionID vid : versions.get(name)) {
	    names.add(name);
	    vids.add(vid);
	  }
	}
	
	for(String aname : anames) {
	  ArchiveVolume volume = readArchive(aname);
	  
	  ArrayList<String> rnames = new ArrayList<String>();
	  ArrayList<VersionID> rvids = new ArrayList<VersionID>();

	  int wk;
	  for(wk=0; wk<names.size(); wk++) {
	    String name = names.get(wk);
	    VersionID vid = vids.get(wk);

	    if(volume.contains(name, vid)) {
	      long size = volume.getSize(name, vid);
	    
	      TreeMap<VersionID,Long> vsizes = sizes.get(name);
	      if(vsizes == null) {
		vsizes = new TreeMap<VersionID,Long>();
		sizes.put(name, vsizes);
	      }
	    
	      vsizes.put(vid, size);		
	    }
	    else {
	      rnames.add(name);
	      rvids.add(vid);
	    }
	  }

	  if(rnames.isEmpty()) 
	    break;

	  names = rnames; 
	  vids  = rvids;
	}
      }

      return new MiscGetSizesRsp(timer, sizes);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }  
    finally {
      pDatabaseLock.readLock().unlock();
    } 
  }
  
  /**
   * Restore the given checked-in versions from the given archive volume. <P> 
   * 
   * Only privileged users may restore checked-in versions. <P> 
   * 
   * @param req
   *   The request.
   * 
   * @return 
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to find the archive.
   */
  public Object
  restore
  (
   MiscRestoreReq req
  ) 
  {
    Date stamp = new Date();
    String archiveName = req.getName();
    TreeMap<String,TreeSet<VersionID>> versions = req.getVersions();
    BaseArchiver archiver = req.getArchiver();

    TaskTimer timer = new TaskTimer("MasterMgr.restore(): " + archiveName);

    timer.aquire();
    pDatabaseLock.writeLock().lock();
    try {
      timer.resume();	

      /* get the archive volume manifest */ 
      ArchiveVolume vol = readArchive(archiveName);

      /* validate the archiver plugin */ 
      {
	BaseArchiver varchiver = vol.getArchiver();
	if(archiver == null) 
	  archiver = varchiver;
	else if(!archiver.getName().equals(varchiver.getName()))
	  throw new PipelineException
	    ("The archiver plugin (" + archiver.getName() + ") did not match the " +
	     "archiver plugin (" + varchiver.getName() + ") used to create the archive " + 
	     "volume (" + archiveName + ")!");
	else if(!archiver.getVersionID().equals(varchiver.getVersionID())) 
	  throw new PipelineException
	    ("The version (" + archiver.getVersionID() + ") of the archiver plugin " + 
	     "(" + archiver.getName() + ") did not match the version " + 
	     "(" + varchiver.getVersionID() + ") of the archiver plugin " + 
	     "(" + varchiver.getName() + ") used to create the archive volume " + 
	     "(" + archiveName + ")!");	
      }

      /* validate the file sequences to be restored */ 
      TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>> fseqs = 
	new TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>>();
      long total = 0L;
      for(String name : versions.keySet()) {
	TreeSet<VersionID> offlined = pOfflined.get(name);
	TreeMap<VersionID,CheckedInBundle> checkedIn = getCheckedInBundles(name);

	for(VersionID vid : versions.get(name)) {
	  if(!vol.contains(name, vid)) 
	    throw new PipelineException
	      ("The checked-in version (" + vid + ") of node (" + name + ") cannot be " +
	       "restored because it is not contained in the archive volume " + 
	       "(" + archiveName + ")!");
	  total += vol.getSize(name, vid);
	    
	  if((offlined == null) || !offlined.contains(vid)) 
	    throw new PipelineException 
	      ("The checked-in version (" + vid + ") of node (" + name + ") cannot " + 
	       "be restored because it is currently online!");
	  
	  CheckedInBundle bundle = checkedIn.get(vid);
	  if(bundle == null) 
	    throw new PipelineException 
	      ("No checked-in version (" + vid + ") of node (" + name + ") exists " + 
	       "to be restored!");

	  TreeMap<VersionID,TreeSet<FileSeq>> fvsns = fseqs.get(name);
	  if(fvsns == null) {
	    fvsns = new TreeMap<VersionID,TreeSet<FileSeq>>();
	    fseqs.put(name, fvsns);
	  }
	    
	  fvsns.put(vid, bundle.uVersion.getSequences());
	}
      }

      /* extract the versions from the archive volume by running the archiver plugin and 
	 save any STDOUT output */
      {
	String output = null;
	{
	  FileMgrClient fclient = getFileMgrClient();
	  try {
	    output = fclient.extract(archiveName, stamp, fseqs, archiver, total);
	  }
	  finally {
	    freeFileMgrClient(fclient);
	  }
	}

	Date now = new Date();
	File file = new File(PackageInfo.sNodeDir, "archives/output/restore/" + 
			     archiveName + "-" + now.getTime());
	try {
	  FileWriter out = new FileWriter(file);
	  
	  if(output != null) 
	    out.write(output);
	  
	  out.flush();
	  out.close();
	}
	catch(IOException ex) {
	  throw new PipelineException
	    ("I/O ERROR: \n" + 
	     "  While attempting to write the archive STDOUT file (" + file + ")...\n" + 
	     "    " + ex.getMessage());
	}
	
	TreeSet<Date> stamps = pRestoredOn.get(archiveName);
	if(stamps == null) {
	  stamps = new TreeSet<Date>();
	  pRestoredOn.put(archiveName, stamps);
	}
	stamps.add(now);
      }
      
      /* move the extracted files into the respository */ 
      for(String name : versions.keySet()) {
	TreeSet<VersionID> offlined = pOfflined.get(name);
	TreeMap<VersionID,CheckedInBundle> checkedIn = getCheckedInBundles(name);
	ArrayList<VersionID> vids = new ArrayList<VersionID>(checkedIn.keySet());
	TreeMap<File,Boolean[]> novelty = noveltyByFile(checkedIn);

	for(VersionID vid : versions.get(name)) {
	  CheckedInBundle bundle = checkedIn.get(vid);
	  int vidx = vids.indexOf(vid);

	  /* determine what files and/or links need to be modified */ 
	  TreeMap<File,TreeSet<VersionID>> symlinks = new TreeMap<File,TreeSet<VersionID>>();
	  TreeMap<File,VersionID> targets = new TreeMap<File,VersionID>();
	  for(File file : novelty.keySet()) {
	    Boolean[] isNovel = novelty.get(file);
	    
	    /* the file exists for this version */ 
	    if(isNovel[vidx] != null) {

	      /* determine revision number of the earliest online version of the file 
		 which will be used as the target of all symlinks for this fill */ 
	      VersionID tvid = vid;
	      if(!isNovel[vidx]) {
		int vk;
		for(vk=vidx-1; vk>=0; vk--) {
		  VersionID nvid = vids.get(vk);
		  if((offlined == null) || !offlined.contains(nvid)) 
		    tvid = nvid;
		  if(isNovel[vk]) 
		    break;
		}
	      }
	     
	      /* move the file and relocate all later links to target this version */ 
	      if(tvid.equals(vid)) {
		TreeSet<VersionID> svids = symlinks.get(file);
		if(svids == null) {
		  svids = new TreeSet<VersionID>();
		  symlinks.put(file, svids);
		}

		int vk;
		for(vk=vidx+1; vk<isNovel.length; vk++) {
		  VersionID nvid = vids.get(vk);
		  if((isNovel[vk] == null) || isNovel[vk]) 
		    break;
		  else if((offlined == null) || !offlined.contains(nvid)) 
		    svids.add(nvid);
		}
	      }

	      /* create a symlink to the earliest online version of the file */ 
	      else {
		targets.put(file, tvid);
	      }
	    }
	  }
 
	  /* restore the files */ 
	  {
	    FileMgrClient fclient = getFileMgrClient();
	    try {
	      fclient.restore(archiveName, stamp, name, vid, symlinks, targets);
	    }
	    finally {
	      freeFileMgrClient(fclient);
	    }
	  }

	  /* update the currently offlined revision numbers */ 
	  {
	    offlined.remove(vid);
	    if(offlined.isEmpty()) {
	      offlined = null;
	      pOfflined.remove(name);
	    }
	  }
	
	  /* update the restore requests */ 
	  {
	    TreeMap<VersionID,RestoreRequest> reqs = pRestoreReqs.get(name);
	    if(reqs != null) {
	      RestoreRequest rr = reqs.get(vid);
	      if(rr != null) 
		rr.restored(archiveName);
	    }
	  }
	}
      }

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }  
    finally {
      try {
	writeRestoreReqs();
      }
      catch(PipelineException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Warning,
	   ex.getMessage());
	LogMgr.getInstance().flush();	
      }

      try {
	FileMgrClient fclient = getFileMgrClient();
	try {
	  fclient.extractCleanup(archiveName, stamp);
	}
	finally {
	  freeFileMgrClient(fclient);
	}
      }
      catch(PipelineException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Warning,
	   ex.getMessage());
	LogMgr.getInstance().flush();
      }

      pDatabaseLock.writeLock().unlock();
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names and creation timestamps of all existing archives. <P> 
   * 
   * @return 
   *   <CODE>MiscGetArchivedOnRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> on failure.
   */
  public Object
  getArchivedOn() 
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pArchivedOn) {
	timer.resume();

	return new MiscGetArchivedOnRsp(timer, pArchivedOn);
      }
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }

  /**
   * Get the names and restore timestamps of all existing archives. <P> 
   * 
   * @return 
   *   <CODE>MiscGetRestoredOnRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> on failure.
   */
  public Object
  getRestoredOn() 
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pRestoredOn) {
	timer.resume();

	return new MiscGetRestoredOnRsp(timer, pRestoredOn);
      }
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }

  /**
   * Get the STDOUT output from running the Archiver plugin during the creation of the 
   * given archive volume.
   *
   * @param req
   *   The request.
   * 
   * @return 
   *   <CODE>MiscGetArchiverOutputRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> on failure.
   */ 
  public Object
  getArchivedOutput
  ( 
   MiscGetArchivedOutputReq req
  ) 
  {
    String aname = req.getName();
    TaskTimer timer = new TaskTimer("MasterMgr.getArchivedOutput(): " + aname);

    try {
      File file = new File(PackageInfo.sNodeDir, 
			   "archives/output/archive/" + aname);
      String output = null;
      if(file.length() > 0) {
	FileReader in = new FileReader(file);
	  
	StringBuffer buf = new StringBuffer();
	char[] cs = new char[4096];
	while(true) {
	  int cnt = in.read(cs);
	  if(cnt == -1) 
	    break;
	  
	  buf.append(cs, 0, cnt);
	}
	
	output = buf.toString();
      }
      
      return new MiscGetArchiverOutputRsp(timer, output);
    }
    catch(IOException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
  }

  /**
   * Get the STDOUT output from running the Archiver plugin during the restoration of the 
   * given archive volume at the given time.
   *
   * @param req
   *   The request.
   * 
   * @return 
   *   <CODE>MiscGetArchiverOutputRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> on failure.
   */ 
  public Object
  getRestoredOutput
  ( 
   MiscGetRestoredOutputReq req
  ) 
  {
    String aname = req.getName();
    Date stamp = req.getTimeStamp();

    TaskTimer timer = 
      new TaskTimer("MasterMgr.getRestoredOutput(): " + aname + "-" + stamp.getTime());
    try {
      File file = new File(PackageInfo.sNodeDir, 
			   "archives/output/restore/" + aname + "-" + stamp.getTime());
      String output = null;
      if(file.length() > 0) {
	FileReader in = new FileReader(file);
	  
	StringBuffer buf = new StringBuffer();
	char[] cs = new char[4096];
	while(true) {
	  int cnt = in.read(cs);
	  if(cnt == -1) 
	    break;
	  
	  buf.append(cs, 0, cnt);
	}
	
	output = buf.toString();
      }
      
      return new MiscGetArchiverOutputRsp(timer, output);
    }
    catch(IOException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
  }

  /**
   * Get the names of the archive volumes containing the given checked-in versions. <P> 
   * 
   * @param req
   *   The request.
   * 
   * @return 
   *   <CODE>MiscGetArchivesContainingRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the containing archives.
   */
  public Object
  getArchivesContaining
  (
   MiscGetArchivesContainingReq req
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pArchivedIn) {
	timer.resume();
	
	TreeMap<String,TreeMap<VersionID,TreeSet<String>>> archives = 
	  new TreeMap<String,TreeMap<VersionID,TreeSet<String>>>();

	TreeMap<String,TreeSet<VersionID>> vsns = req.getVersions();
	for(String name : vsns.keySet()) {
	  TreeMap<VersionID,TreeSet<String>> aversions = pArchivedIn.get(name);
	  if(aversions != null) {
	    for(VersionID vid : vsns.get(name)) {
	      TreeSet<String> anames = aversions.get(vid);
	      if(anames != null) {
		TreeMap<VersionID,TreeSet<String>> oaversions = archives.get(name);
		if(oaversions == null) {
		  oaversions = new TreeMap<VersionID,TreeSet<String>>();
		  archives.put(name, oaversions);
		}
		oaversions.put(vid, anames);
	      }
	    }
	  }
	}
	
	return new MiscGetArchivesContainingRsp(timer, archives);
      }
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }
  
  /**
   * Get the complete information about the archive with the given name. <P>
   * 
   * @param req
   *   The request.
   * 
   * @return 
   *   <CODE>MiscGetArchiveRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to find the archive.
   */
  public Object
  getArchive
  (
   MiscGetArchiveReq req
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();

      return new MiscGetArchiveRsp(timer, readArchive(req.getName()));
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }  
    finally {
      pDatabaseLock.readLock().unlock();
    }
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
   * Remove all entries for the given node. <P> 
   * 
   * Removes any branch components which become empty due to the node entry removal.
   * 
   * @param name
   *   The fully resolved node name.
   */ 
  private void
  removeNodeTreePath
  (
   String name
  )
  {
    synchronized(pNodeTreeRoot) {
      String comps[] = name.split("/"); 
      
      Stack<NodeTreeEntry> stack = new Stack<NodeTreeEntry>();
      stack.push(pNodeTreeRoot);

      int wk;
      for(wk=1; wk<comps.length; wk++) {
	NodeTreeEntry entry = (NodeTreeEntry) stack.peek().get(comps[wk]);
	if(entry == null)
	  return;
	stack.push(entry);
      }

      NodeTreeEntry entry = stack.pop();
      assert(entry != null); 
      assert(entry.isLeaf());

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

  /**
   * Remove the given secondary file sequence of the working version from the node 
   * path tree. <P> 
   * 
   * @param nodeID
   *   The unique working version identifier.
   * 
   * @param fseq
   *   The secondary file sequence to remove.
   */ 
  private void 
  removeSecondaryWorkingNodeTreePath
  (
   NodeID nodeID, 
   FileSeq fseq
  )
  {
    synchronized(pNodeTreeRoot) {
      String comps[] = nodeID.getName().split("/"); 
      
      NodeTreeEntry parent = pNodeTreeRoot;
      int wk;
      for(wk=1; wk<(comps.length-1); wk++) 
	parent = (NodeTreeEntry) parent.get(comps[wk]);
      
      String name = comps[comps.length-1];
      NodeTreeEntry entry = (NodeTreeEntry) parent.get(name);
      entry.removeSequence(fseq);
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

	  for(NodeTreeEntry child : parent.values()) {
	    if(child.isLeaf() && !child.isSequenceUnused(fseq)) 
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
   * Get the names of the working area views containing the given node.
   * 
   * @param name
   *   The fully resolved node name.
   * 
   * @return
   *   The named of the working area views indexed by owning user name.
   */ 
  private TreeMap<String,TreeSet<String>> 
  getViewsContaining
  (
   String name
  ) 
  {
    TreeMap<String,TreeSet<String>> views = new TreeMap<String,TreeSet<String>>();
    
    synchronized(pNodeTreeRoot) {
      String comps[] = name.split("/"); 
      
      NodeTreeEntry parent = pNodeTreeRoot;
      int wk;
      for(wk=1; wk<comps.length; wk++) {
	NodeTreeEntry entry = (NodeTreeEntry) parent.get(comps[wk]);
	if(entry == null) 
	  return views;
	parent = entry;
      }
      
      for(String author : parent.getWorkingAuthors()) 
	views.put(author, new TreeSet<String>(parent.getWorkingViews(author)));
    }

    return views;
  }

  /**
   * Log the node tree contents.
   */ 
  public void 
  logNodeTree() 
  {
    synchronized(pNodeTreeRoot) {
      if(!pNodeTreeRoot.isEmpty() && 
	 LogMgr.getInstance().isLoggable(LogMgr.Kind.Ops, LogMgr.Level.Finer)) {
	StringBuffer buf = new StringBuffer(); 
	buf.append("Node Tree:\n");
	logNodeTreeHelper(pNodeTreeRoot, 1, buf);
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Finer,
	   buf.toString());
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

      validateStaleLinks(root);
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

      /* add the status stub */ 
      NodeStatus status = new NodeStatus(nodeID);
      table.put(name, status);

      /* process the upstream nodes */ 
      Date missingStamp = new Date();
      switch(versionState) {
      case CheckedIn:
	for(LinkVersion link : latest.getSources()) {
	  NodeID lnodeID = new NodeID(nodeID, link.getName());

	  performUpstreamNodeOp(nodeOp, lnodeID, branch, table, timer);
	  NodeStatus lstatus = table.get(link.getName());
	      
	  status.addSource(lstatus);
	  lstatus.addTarget(status);
	}
	break;

      default:
	for(LinkMod link : work.getSources()) {
	  NodeID lnodeID = new NodeID(nodeID, link.getName());

	  performUpstreamNodeOp(nodeOp, lnodeID, branch, table, timer);
	  NodeStatus lstatus = table.get(link.getName());

	  status.addSource(lstatus);
	  lstatus.addTarget(status);
	}
      }

      /* compute link state and 
	 whether the source node can be ignore when propogating staleness */ 
      LinkState linkState = null;
      TreeSet<String> nonIgnoredSources = new TreeSet<String>();
      switch(versionState) {
      case Pending:
	linkState = LinkState.Pending;
	break;
	
      case CheckedIn:
	linkState = LinkState.CheckedIn;
	  break;
	  
      case Identical:
      case NeedsCheckOut:
	{
	  boolean workEqBase = true;
	  for(LinkMod link : work.getSources()) {
	    String lname = link.getName(); 
	    LinkVersion blink = base.getSource(lname); 

	    if((blink == null) || !link.equals(blink)) {
	      workEqBase = false;
	      nonIgnoredSources.add(lname);
	    }
	    else {
	      NodeDetails ldetails = table.get(link.getName()).getDetails();
	      VersionID lvid = ldetails.getWorkingVersion().getWorkingID();
	      if(!blink.getVersionID().equals(lvid)) {
		workEqBase = false;
		nonIgnoredSources.add(lname);
	      }
	    }
	  }

	  if(work.identicalLinks(latest)) 
	    linkState = LinkState.Identical;
	  else {
	    switch(versionState) {
	    case Identical:
	      linkState = LinkState.Modified;
	      break;
	      
	    case NeedsCheckOut:
	      if(workEqBase) 
		linkState = LinkState.NeedsCheckOut;
	      else 
		linkState = LinkState.Conflicted;
	    }
	  }
	}
      }

      /* get per-file FileStates and timestamps */ 
      TreeMap<FileSeq, FileState[]> fileStates = new TreeMap<FileSeq, FileState[]>(); 
      boolean[] anyMissing = null;
      Date[] newestStamps = null;
      Date[] oldestStamps = null;
      switch(versionState) {
      case CheckedIn:
	/* if checked-in, all files must be CheckedIn, no files can be Missing and all 
	   timestamps must be (null) */ 
	for(FileSeq fseq : latest.getSequences()) {
	  FileState fs[] = new FileState[fseq.numFrames()];

	  int wk;
	  for(wk=0; wk<fs.length; wk++) 
	    fs[wk] = FileState.CheckedIn;

	  fileStates.put(fseq, fs);

	  if(anyMissing == null) 
	    anyMissing = new boolean[fs.length];
	  
	  if(newestStamps == null) 
	    newestStamps = new Date[fs.length];	  

	  if(oldestStamps == null) 
	    oldestStamps = new Date[fs.length];
	}
	break;

      default:
	{
	  /* query the file manager for per-file states */ 
	  VersionID vid = null;
	  if(latest != null) 
	    vid = latest.getVersionID();
	  
	  TreeMap<FileSeq, Date[]> stamps = new TreeMap<FileSeq, Date[]>();
	  
	  {
	    FileMgrClient fclient = getFileMgrClient();
	    try {
	      fclient.states(nodeID, work, versionState, work.isFrozen(), 
			     vid, fileStates, stamps);
	    }
	    finally {
	      freeFileMgrClient(fclient);
	    }
	  }

	  /* get the newest/oldest of the timestamp for each file sequence index */ 
	  {
	    Date stamp = null;
	    if(work.isFrozen()) 
	      stamp = work.getTimeStamp();

	    for(FileSeq fseq : stamps.keySet()) {
	      Date[] ts = stamps.get(fseq);
	      
	      if(newestStamps == null) 
		newestStamps = new Date[ts.length];
	      
	      if(oldestStamps == null) 
		oldestStamps = new Date[ts.length];
	      
	      int wk;
	      for(wk=0; wk<ts.length; wk++) {
		if(stamp != null) {
		  newestStamps[wk] = stamp; 
		  oldestStamps[wk] = stamp;
		}
		else {
		  /* the newest among the primary/secondary files for the index */ 
		  if((newestStamps[wk] == null) || 
		     ((ts[wk] != null) && (ts[wk].compareTo(newestStamps[wk]) > 0)))
		    newestStamps[wk] = ts[wk];
		  
		  /* the oldest among the primary/secondary files for the index */ 
		  if((oldestStamps[wk] == null) || 
		     ((ts[wk] != null) && (ts[wk].compareTo(oldestStamps[wk]) < 0)))
		    oldestStamps[wk] = ts[wk];
		}
	      }
	    }
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
	    overallNodeState = 
	      (anyNeedsCheckOut ? OverallNodeState.MissingNewer : OverallNodeState.Missing);
	  else if(anyConflicted || (anyNeedsCheckOut && anyModified))
	    overallNodeState = OverallNodeState.Conflicted;
	  else if(anyModified) 
	    overallNodeState = OverallNodeState.Modified;
	  else if(anyNeedsCheckOut) {
	    for(LinkMod link : work.getSources()) {
	      NodeDetails ldetails = table.get(link.getName()).getDetails();
	      VersionID lvid = ldetails.getWorkingVersion().getWorkingID();

	      switch(ldetails.getOverallNodeState()) {
	      case Modified:
	      case ModifiedLinks:
	      case Conflicted:	
	      case Missing:
	      case MissingNewer:
		overallNodeState = OverallNodeState.Conflicted;

 	      case Identical:
 	      case NeedsCheckOut:
		{
		  LinkVersion blink = base.getSource(link.getName());
		  if((blink == null) || !blink.getVersionID().equals(lvid)) 
		    overallNodeState = OverallNodeState.Conflicted;
		}
	      }
	    }

	    if(overallNodeState == null)
	      overallNodeState = OverallNodeState.NeedsCheckOut;
	  }
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
	      VersionID lvid = ldetails.getWorkingVersion().getWorkingID();
	      
	      switch(ldetails.getOverallNodeState()) {
	      case Modified:
	      case ModifiedLinks:
	      case Conflicted:	
	      case Missing:
	      case MissingNewer:
		overallNodeState = OverallNodeState.ModifiedLinks;
		break;
		
 	      case Identical:
 	      case NeedsCheckOut:
 		if(!link.getVersionID().equals(lvid)) 
 		  overallNodeState = OverallNodeState.ModifiedLinks;
	      }
	    }

	    if(overallNodeState == null)
	      overallNodeState = OverallNodeState.Identical;
	  }
	}
      }

      /* determine per-file QueueStates */  
      Long jobIDs[] = null;
      QueueState queueStates[] = null;
      switch(versionState) {
      case CheckedIn:
	{
	  int numFrames = latest.getPrimarySequence().numFrames();
	  jobIDs      = new Long[numFrames];
	  queueStates = new QueueState[numFrames];

	  int wk;
	  for(wk=0; wk<queueStates.length; wk++) 
	    queueStates[wk] = QueueState.Undefined;
	}
	break;

      default:
	{	    
	  int numFrames = work.getPrimarySequence().numFrames();
	  jobIDs      = new Long[numFrames];
	  queueStates = new QueueState[numFrames];

	  JobState js[] = new JobState[numFrames];
	  {
	    ArrayList<Long> jIDs          = new ArrayList<Long>();
	    ArrayList<JobState> jobStates = new ArrayList<JobState>();

	    pQueueMgrClient.getJobStates(nodeID, work.getTimeStamp(), 
					 work.getPrimarySequence(), jIDs, jobStates);

	    assert(jobIDs.length == jIDs.size());
	    jobIDs = (Long[]) jIDs.toArray(jobIDs);

	    assert(js.length == jobStates.size());
	    js = (JobState[]) jobStates.toArray(js);
	  }
	  
	  int wk;
	  for(wk=0; wk<queueStates.length; wk++) {
	    /* the regeneration action is disabled or does not exist, 
	         therefore QueueState is always Finished */ 
	    if(!work.isActionEnabled()) {
	      queueStates[wk] = QueueState.Finished;
	    }

	    /* there IS an enabled regeneration action */ 
	    else {
	      /* check for active jobs */ 
	      if(js[wk] != null) {
		switch(js[wk]) {
		case Queued:
		  queueStates[wk] = QueueState.Queued;
		  break;

		case Paused:
		  queueStates[wk] = QueueState.Paused;
		  break;

		case Aborted:
		  queueStates[wk] = QueueState.Aborted;
		  break;

		case Running:
		  queueStates[wk] = QueueState.Running;
		  break;

		case Failed:
		  queueStates[wk] = QueueState.Failed;
		  break;

		case Finished:
		  break;
		}
	      }
	      
	      if(queueStates[wk] == null) {
		switch(overallNodeState) {
		case Identical: 
		  queueStates[wk] = QueueState.Finished;
		  break;
		  
		default:
		  /* check for missing files or if the working version has been modified 
		     since the oldest of the primary/secondary files were created */ 
		  if(anyMissing[wk] ||
		     (oldestStamps[wk].compareTo(work.getLastCriticalModification()) < 0)) {
		    queueStates[wk] = QueueState.Stale;
		  }
		
		  /* check upstream per-file dependencies */ 
		  else {
		    for(LinkMod link : work.getSources()) {
		      if(link.getPolicy() == LinkPolicy.Dependency) {
			NodeStatus lstatus = status.getSource(link.getName());
			NodeDetails ldetails = lstatus.getDetails();
			
			QueueState lqs[] = ldetails.getQueueState();
			Date lstamps[] = ldetails.getFileTimeStamps();
		      
			boolean lanyMissing[] = null;
			for(FileSeq lfseq : ldetails.getFileStateSequences()) {
			  FileState lfs[] = ldetails.getFileState(lfseq);
	    
			  if(lanyMissing == null) 
			    lanyMissing = new boolean[lfs.length];
			  
			  int mk;
			  for(mk=0; mk<lanyMissing.length; mk++) {
			    if(lfs[mk] == FileState.Missing) 
			      lanyMissing[mk] = true;
			  }
			}

			switch(link.getRelationship()) {
			case OneToOne:
			  {
			    Integer offset = link.getFrameOffset();
			    int idx = wk+offset;
			    if(((idx >= 0) && (idx < lqs.length)) &&
			       ((lqs[idx] != QueueState.Finished) || 
				lanyMissing[idx] || 
				((lstamps[idx] != null) &&
				 (oldestStamps[wk].compareTo(lstamps[idx]) < 0))))
			      queueStates[wk] = QueueState.Stale;
			  }
			  break;
			
		      case All:
			{
			  int fk;
			  for(fk=0; fk<lqs.length; fk++) {
			    if((lqs[fk] != QueueState.Finished) || 
			       lanyMissing[fk] || 
			       ((lstamps[fk] != null) &&
				(oldestStamps[wk].compareTo(lstamps[fk]) < 0))) {
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
      }

      /* compute overall queue state */ 
      OverallQueueState overallQueueState = OverallQueueState.Undefined; 
      if(versionState != VersionState.CheckedIn) {
	boolean anyStale = false;
	boolean anyPaused = false;
	boolean anyQueued = false;
	boolean anyRunning = false; 
	boolean anyAborted = false;
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

	  case Paused:
	    anyPaused = true;
	    break;
	    
	  case Running:
	    anyRunning = true;
	    break;

	  case Aborted:
	    anyAborted = true;
	    break;
	    
	  case Failed:
	    anyFailed = true;
	  }
	}
	
	if(anyFailed) 
	  overallQueueState = OverallQueueState.Failed;
	else if(anyAborted) 
	  overallQueueState = OverallQueueState.Aborted;
	else if(anyRunning) 
	  overallQueueState = OverallQueueState.Running;
	else if(anyPaused) 
	  overallQueueState = OverallQueueState.Paused;
	else if(anyQueued) 
	  overallQueueState = OverallQueueState.Queued;
	else if(anyStale) 
	  overallQueueState = OverallQueueState.Stale;
	else 
	  overallQueueState = OverallQueueState.Finished;
      }

      /**
       * Before updating the timestamps of the files associated with this node, determine 
       * if staleness will be propogated from each upstream link. <P> 
       * 
       * Staleness is propgated if the timestamp of any upstream file upon which any of this
       * node's files depend (through a Reference/Dependency link) is newer than the dependent
       * files.  Timestamps for these upstream nodes will be ignore if the ignore timestamp
       * flag is set for the upstream file and the upstream node is not a member of the 
       * nonIgnoredSources set. <P> 
       *        
       * These upstream timestamp have been previously modified to propogate staleness
       * of those nodes further upstream.
       */ 
      switch(versionState) {
      case CheckedIn:
	break;

      default:
	{
	  int wk;
	  for(wk=0; wk<queueStates.length; wk++) {
	    for(LinkMod link : work.getSources()) {
	      boolean staleLink = false;

	      switch(overallQueueState) {
	      case Running:
		break;

	      default:	
		if((link.getPolicy() == LinkPolicy.Reference) || work.isActionEnabled()) {
		  NodeStatus lstatus = status.getSource(link.getName());
		  NodeDetails ldetails = lstatus.getDetails();
		  switch(ldetails.getOverallQueueState()) {
		  case Finished:
		    {
		      QueueState lqs[]   = ldetails.getQueueState();
		      Date lstamps[]     = ldetails.getFileTimeStamps();
		      boolean lignored[] = ldetails.ignoreTimeStamps();

		      boolean nonIgnored = nonIgnoredSources.contains(link.getName());

		      boolean lanyMissing[] = null;
		      for(FileSeq lfseq : ldetails.getFileStateSequences()) {
			FileState lfs[] = ldetails.getFileState(lfseq);
			    
			if(lanyMissing == null) 
			  lanyMissing = new boolean[lfs.length];
			    
			int mk;
			for(mk=0; mk<lanyMissing.length; mk++) {
			  if(lfs[mk] == FileState.Missing) 
			    lanyMissing[mk] = true;
			}
		      }
			  
		      switch(link.getRelationship()) {
		      case OneToOne:
			{
			  Integer offset = link.getFrameOffset();
			  int idx = wk+offset;
			  if((idx >= 0) && (idx < lqs.length)) {
			    if(anyMissing[wk] || lanyMissing[idx] || 
			       ((!lignored[idx] || nonIgnored) && 
				(oldestStamps[wk].compareTo(lstamps[idx]) < 0)))
			      staleLink = true;
			  }
			}
			break;
			    
		      case All:
			{
			  int fk;
			  for(fk=0; fk<lqs.length; fk++) {
			    if(anyMissing[wk] || lanyMissing[fk] || 
			       ((!lignored[fk] || nonIgnored) && 
				(oldestStamps[wk].compareTo(lstamps[fk]) < 0)))
			      staleLink = true;
			  }
			}
		      }
		    }
		    break;
			
		  default:
		    staleLink = true;
		  }
		}
	      }

	      if(staleLink) 
		status.addStaleLink(link.getName());
	    }
	  }
	}
      }

      /**
       * Also mark the changed links as propogating staleness, even though no timestamps 
       * will be propogated, so that it is clear which of the existing links have been 
       * modified from the base version.
       */ 
      switch(linkState) {
      case Pending:
      case CheckedIn:
	break;

      default:
	for(LinkMod link : work.getSources()) {
	  LinkVersion blink = base.getSource(link.getName());
	  if((blink == null) || !link.equals(blink)) 
	    status.addStaleLink(link.getName());
	}
      }

      /**
       * Propagate staleness by setting the per-file time stamps of each file to be 
       * the newest of:
       * 
       *   + The newest actual file time stamp.
       * 
       *   + If the FileState is Missing, the time stamp of when the FileState was computed.
       * 
       *   + The last critical modification timestamp of the current node.
       * 
       *   + The time stamp of any upstream file upon which the file depends through a 
       *     Reference link or a Dependency link if the current node does not have a disabled
       *     action.  These upstream time stamp have been previously modified to propogate 
       *     staleness of those nodes further upstream.  Upstream per-file time stamps which 
       *     are (null) should be ignored.
       */
      Date[] fileStamps = new Date[oldestStamps.length];
      boolean[] ignoreStamps = new boolean[oldestStamps.length];
      switch(versionState) {
      case CheckedIn:
	break;

      default:
	{
	  int wk;
	  for(wk=0; wk<queueStates.length; wk++) {
	    if(anyMissing[wk]) 
	      fileStamps[wk] = missingStamp;
	    else 
	      fileStamps[wk] = newestStamps[wk];

	    Date critical = work.getLastCriticalModification();
	    if(critical.compareTo(fileStamps[wk]) > 0)
	      fileStamps[wk] = critical;

	    switch(overallNodeState) {
	    case Identical:
	    case NeedsCheckOut:
	      ignoreStamps[wk] = true;
	    }

	    for(LinkMod link : work.getSources()) { 
	      if((link.getPolicy() == LinkPolicy.Reference) || work.isActionEnabled()) {
		NodeStatus lstatus = status.getSource(link.getName());
		NodeDetails ldetails = lstatus.getDetails();
		
		QueueState lqs[]   = ldetails.getQueueState();
		Date lstamps[]     = ldetails.getFileTimeStamps();
		boolean lignored[] = ldetails.ignoreTimeStamps();
		
		boolean nonIgnored = nonIgnoredSources.contains(link.getName());
		
		switch(link.getRelationship()) {
		case OneToOne:
		  {
		    Integer offset = link.getFrameOffset();
		    int idx = wk+offset;
		    
		    if((idx >= 0) && (idx < lqs.length)) {
		      if(!lignored[idx] || nonIgnored) {
			ignoreStamps[wk] = false;
			if(lstamps[idx].compareTo(fileStamps[wk]) > 0)
			  fileStamps[wk] = lstamps[idx];
		      }
		    }
		  }
		  break;
		  
		case All:
		  {
		    int fk;
		    for(fk=0; fk<lqs.length; fk++) {
		      if(!lignored[fk] || nonIgnored) {
			ignoreStamps[wk] = false;
			if(lstamps[fk].compareTo(fileStamps[wk]) > 0) 
			  fileStamps[wk] = lstamps[fk];
		      }
		    }
		  }
		}
	      }
	    }
	  }
	}
      }
	
      /* create the node details */
      NodeDetails details = 
	new NodeDetails(name, 
			work, base, latest, versionIDs, 
			overallNodeState, overallQueueState, 
			versionState, propertyState, linkState, 
			fileStates, fileStamps, ignoreStamps, 
			jobIDs, queueStates);

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
   * Recursively traverse the upstream nodes removing the propagate staleness flags
   * from any links for which all Dependency/Reference downstream links do not also have 
   * the propagate staleness flag. 
   * 
   * @param status
   *   The status of the current node.
   */ 
  private void 
  validateStaleLinks
  (
   NodeStatus status
  )
  {
    boolean nonStale = false;
    for(NodeStatus tstatus : status.getTargets()) {
      if(!tstatus.isStaleLink(status.getName())) {
	NodeDetails tdetails = tstatus.getDetails();
	if(tdetails != null) {
	  NodeMod tmod = tdetails.getWorkingVersion(); 
	  if(tmod != null) 
	    nonStale = true;
	}
      }

      if(nonStale)
	break;
    }

    for(NodeStatus lstatus : status.getSources()) {
      if(nonStale) 
	status.removeStaleLink(lstatus.getName());
      validateStaleLinks(lstatus);
    }
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
  /*   F I L E   M G R   H E L P E R S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get a connection to the file manager.
   */ 
  private FileMgrClient
  getFileMgrClient()
  {
    synchronized(pFileMgrClients) {
      if(pFileMgrClients.isEmpty()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Finest,
	   "Creating New File Manager Client.");
	LogMgr.getInstance().flush();

	return new FileMgrNetClient();
      }
      else {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Finest,
	   "Reusing File Manager Client: " + (pFileMgrClients.size()-1) + " inactive");
	LogMgr.getInstance().flush();

	return pFileMgrClients.pop();
      }
    }
  }

  /**
   * Return an inactive connection to the file manager for reuse.
   */ 
  private void
  freeFileMgrClient
  (
   FileMgrClient client
  )
  {
    synchronized(pFileMgrClients) {
      pFileMgrClients.push(client);
      
      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Finest,
	 "Freed File Manager Client: " + pFileMgrClients.size() + " inactive");
      LogMgr.getInstance().flush();
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

    String name = id.getName();

    /* lookup the bundle */ 
    WorkingBundle bundle = null;
    synchronized(pWorkingBundles) {
      HashMap<NodeID,WorkingBundle> table = pWorkingBundles.get(name);
      if(table == null) {
	table = new HashMap<NodeID,WorkingBundle>();
	pWorkingBundles.put(name, table);
      }
      else {
	bundle = table.get(id);
      }
    }

    if(bundle != null) {
      return bundle;
    }

    /* read in the bundle from disk */ 
    NodeMod mod = readWorkingVersion(id);
    if(mod == null) 
      throw new PipelineException
	("No working version of node (" + name + ") exists under the view (" + 
	 id.getView() + ") owned by user (" + id.getAuthor() + ")!");
    
    bundle = new WorkingBundle(mod);

    synchronized(pWorkingBundles) {
      pWorkingBundles.get(name).put(id, bundle);
    }
    
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
  /*   I / O   H E L P E R S                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Recursively remove all empty directories at or above the given directory.
   * 
   * @param root
   *   The delete operation should stop at this directory regardles of whether it is empty.
   * 
   * @param parent
   *   The start directory of the delete operation.
   */ 
  public void 
  deleteEmptyParentDirs
  (
   File root, 
   File dir
  ) 
    throws PipelineException
  { 
    synchronized(pMakeDirLock) {
      File tmp = dir;
      while(true) {
	if((tmp == null) || tmp.equals(root) || !tmp.isDirectory())
	  break;
	
	File files[] = tmp.listFiles();
	if((files == null) || (files.length > 0)) 
	  break;
	
	File parent = tmp.getParentFile();

	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Finest,
	   "Deleting Empty Directory: " + tmp);
	LogMgr.getInstance().flush();

	if(!tmp.delete()) 
	  throw new PipelineException
	    ("Unable to delete the empty directory (" + tmp + ")!");

	tmp = parent;
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Write the given archive information to disk. <P> 
   * 
   * @param archive
   *   The archive information.
   * 
   * @throws PipelineException
   *   If unable to write the archive file. 
   */ 
  private void 
  writeArchive
  (
   ArchiveVolume archive
  ) 
    throws PipelineException
  {
    synchronized(pArchiveFileLock) {
      File file = new File(PackageInfo.sNodeDir, "archives/manifests/" + archive.getName());
      if(file.exists()) {
	throw new PipelineException
	  ("Unable to overrite the existing archive file(" + file + ")!");
      }
      
      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Writing Archive: " + archive.getName());
      
      try {
	String glue = null;
	try {
	  GlueEncoder ge = new GlueEncoderImpl("Archive", archive);
	  glue = ge.getText();
	}
	catch(GlueException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	     "Unable to generate a Glue format representation of the archive " + 
	     "(" + archive.getName() + ")!");
	  LogMgr.getInstance().flush();
	  
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
	 "  While attempting to write the archive file (" + file + ")...\n" + 
	 "    " + ex.getMessage());
      }
    }
  }
  
  /**
   * Read the archive information with the given name from disk. <P> 
   * 
   * @param name
   *   The archive name.
   * 
   * @throws PipelineException
   *   If unable to read the archive file.
   */ 
  private ArchiveVolume
  readArchive
  (
   String name
  )
    throws PipelineException
  {
    synchronized(pArchiveFileLock) {
      File file = new File(PackageInfo.sNodeDir, "archives/manifests/" + name);
      if(!file.isFile()) 
	throw new PipelineException
	  ("No file exists for archive (" + name + ")!");

      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Reading Archive: " + name);

      ArchiveVolume archive = null;
      try {
	FileReader in = new FileReader(file);
	GlueDecoder gd = new GlueDecoderImpl(in);
	archive = (ArchiveVolume) gd.getObject();
	in.close();
      }
      catch(Exception ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	   "The archive file (" + file + ") appears to be corrupted!");
	LogMgr.getInstance().flush();
	
	throw new PipelineException
	  ("I/O ERROR: \n" + 
	   "  While attempting to read the archive file (" + file + ")...\n" + 
	   "    " + ex.getMessage());
      }
      assert(archive != null);
      assert(archive.getName().equals(name));

      return archive;
    }
  }
   

  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the offline versions table to disk. <P> 
   * 
   * @throws PipelineException
   *   If unable to write the offline versions file.
   */ 
  private void 
  writeRestoreReqs() 
    throws PipelineException
  {
    synchronized(pRestoreReqs) {
      File file = new File(PackageInfo.sNodeDir, "archives/restore-reqs");
      if(file.exists()) {
	if(!file.delete())
	  throw new PipelineException
	    ("Unable to remove the old restore requests file (" + file + ")!");
      }

      if(!pRestoreReqs.isEmpty()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Writing Restore Requests.");

	try {
	  String glue = null;
	  try {
	    GlueEncoder ge = new GlueEncoderImpl("RestoreReqs", pRestoreReqs);
	    glue = ge.getText();
	  }
	  catch(GlueException ex) {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	       "Unable to generate a Glue format representation of the restore requests!");
	    LogMgr.getInstance().flush();
	    
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
	     "  While attempting to write the restore requests file (" + file + ")...\n" + 
	     "    " + ex.getMessage());
	}
      }
    }
  }
  
  /**
   * Read the restore requests table from disk.
   * 
   * @throws PipelineException
   *   If unable to read the restore requests file.
   */ 
  private void 
  readRestoreReqs()
    throws PipelineException
  {
    synchronized(pRestoreReqs) {
      pRestoreReqs.clear();

      File file = new File(PackageInfo.sNodeDir, "archives/restore-reqs");
      if(file.isFile()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Reading Restore Requests.");

	TreeMap<String,TreeMap<VersionID,RestoreRequest>> requests = null;
	try {
	  FileReader in = new FileReader(file);
	  GlueDecoder gd = new GlueDecoderImpl(in);
	  requests = (TreeMap<String,TreeMap<VersionID,RestoreRequest>>) gd.getObject();
	  in.close();
	}
	catch(Exception ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	     "The restore requests file (" + file + ") appears to be corrupted!");
	  LogMgr.getInstance().flush();
	  
	  throw new PipelineException
	    ("I/O ERROR: \n" + 
	     "  While attempting to read the restore requests file (" + file + ")...\n" + 
	     "    " + ex.getMessage());
	}

	pRestoreReqs.putAll(requests);
      }
    }
  }


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
      File file = new File(PackageInfo.sNodeDir, "toolsets/default-toolset");
      if(file.exists()) {
	if(!file.delete())
	  throw new PipelineException
	    ("Unable to remove the old default toolset file (" + file + ")!");
      }

      if(pDefaultToolset != null) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Writing Default Toolset.");

	try {
	  String glue = null;
	  try {
	    GlueEncoder ge = new GlueEncoderImpl("DefaultToolset", pDefaultToolset);
	    glue = ge.getText();
	  }
	  catch(GlueException ex) {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	       "Unable to generate a Glue format representation of the default toolset!");
	    LogMgr.getInstance().flush();
	    
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

      File file = new File(PackageInfo.sNodeDir, "toolsets/default-toolset");
      if(file.isFile()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Reading Default Toolset.");

	try {
	  FileReader in = new FileReader(file);
	  GlueDecoder gd = new GlueDecoderImpl(in);
	  pDefaultToolset = (String) gd.getObject();
	  in.close();
	}
	catch(Exception ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	     "The default toolset file (" + file + ") appears to be corrupted!");
	  LogMgr.getInstance().flush();
	  
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
      File file = new File(PackageInfo.sNodeDir, "toolsets/active-toolsets");
      if(file.exists()) {
	if(!file.delete())
	  throw new PipelineException
	    ("Unable to remove the old active toolsets file (" + file + ")!");
      }

      if(!pActiveToolsets.isEmpty()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Writing Active Toolsets.");

	try {
	  String glue = null;
	  try {
	    GlueEncoder ge = new GlueEncoderImpl("ActiveToolsets", pActiveToolsets);
	    glue = ge.getText();
	  }
	  catch(GlueException ex) {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	       "Unable to generate a Glue format representation of the active toolsets!");
	    LogMgr.getInstance().flush();
	    
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

      File file = new File(PackageInfo.sNodeDir, "toolsets/active-toolsets");
      if(file.isFile()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Reading Active Toolsets.");

	TreeSet<String> tsets = null;
	try {
	  FileReader in = new FileReader(file);
	  GlueDecoder gd = new GlueDecoderImpl(in);
	  tsets = (TreeSet<String>) gd.getObject();
	  in.close();
	}
	catch(Exception ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	     "The active toolsets file (" + file + ") appears to be corrupted!");
	  LogMgr.getInstance().flush();
	  
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
      File file = new File(PackageInfo.sNodeDir, "toolsets/toolsets/" + tset.getName());
      if(file.exists()) {
	throw new PipelineException
	  ("Unable to overrite the existing toolset file(" + file + ")!");
      }

      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Writing Toolset: " + tset.getName());

      try {
	String glue = null;
	try {
	  GlueEncoder ge = new GlueEncoderImpl("Toolset", tset);
	  glue = ge.getText();
	}
	catch(GlueException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	     "Unable to generate a Glue format representation of the toolset " + 
	     "(" + tset.getName() + ")!");
	  LogMgr.getInstance().flush();
	  
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
      File file = new File(PackageInfo.sNodeDir, "toolsets/toolsets/" + name);
      if(!file.isFile()) 
	throw new PipelineException
	  ("No toolset file exists for toolset (" + name + ")!");

      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Reading Toolset: " + name);

      Toolset tset = null;
      try {
	FileReader in = new FileReader(file);
	GlueDecoder gd = new GlueDecoderImpl(in);
	tset = (Toolset) gd.getObject();
	in.close();
      }
      catch(Exception ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	   "The toolset file (" + file + ") appears to be corrupted!");
	LogMgr.getInstance().flush();
	
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
      File dir = new File(PackageInfo.sNodeDir, "toolsets/packages/" + pkg.getName());
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

      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Writing Toolset Package: " + pkg.getName() + " v" + pkg.getVersionID());

      try {
	String glue = null;
	try {
	  GlueEncoder ge = new GlueEncoderImpl("ToolsetPackage", pkg);
	  glue = ge.getText();
	}
	catch(GlueException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	     "Unable to generate a Glue format representation of the toolset package " + 
	     "(" + pkg.getName() + " v" + pkg.getVersionID() + ")!");
	  LogMgr.getInstance().flush();
	  
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
      File file = new File(PackageInfo.sNodeDir, "toolsets/packages/" + name + "/" + vid);
      if(!file.isFile()) 
	throw new PipelineException
	  ("No toolset package file exists for package (" + name + " v" + vid + ")!");

      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Reading Toolset Package: " + name + " v" + vid);

      PackageVersion pkg = null;
      try {
	FileReader in = new FileReader(file);
	GlueDecoder gd = new GlueDecoderImpl(in);
	pkg = (PackageVersion) gd.getObject();
	in.close();
      }
      catch(Exception ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	   "The toolset package file (" + file + ") appears to be corrupted!");
	LogMgr.getInstance().flush();
	
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
      File file = new File(PackageInfo.sNodeDir, "etc/suffix-editors/" + author); 
      if(file.exists()) {
	if(!file.delete())
	  throw new PipelineException
	    ("Unable to remove the old suffix editors file (" + file + ")!");
      }

      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Writing Suffix Editors: " + author);

      try {
	String glue = null;
	try {
	  GlueEncoder ge = new GlueEncoderImpl("SuffixEditors", editors);
	  glue = ge.getText();
	}
	catch(GlueException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	     "Unable to generate a Glue format representation of the suffix editors " + 
	     "for user (" + author + ")!");
	  LogMgr.getInstance().flush();
	  
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
      File file = new File(PackageInfo.sNodeDir, "etc/suffix-editors/" + author); 
      if(!file.isFile()) 
	return null;

      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Reading Suffix Editors: " + author);

      TreeSet<SuffixEditor> editors = null;
      try {
	FileReader in = new FileReader(file);
	GlueDecoder gd = new GlueDecoderImpl(in);
	editors = (TreeSet<SuffixEditor>) gd.getObject();
	in.close();
      }
      catch(Exception ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	   "The suffix editors file (" + file + ") appears to be corrupted!");
	LogMgr.getInstance().flush();
	
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
   * Write the layout of the editor plugin selection menu to disk. <P> 
   * 
   * @throws PipelineException
   *   If unable to write the menu layout file.
   */ 
  private void 
  writeEditorMenuLayout() 
    throws PipelineException
  {
    synchronized(pPluginMenuLayoutLock) {
      File file = new File(PackageInfo.sNodeDir, "etc/editor-menu-layout");
      if(file.exists()) {
	if(!file.delete())
	  throw new PipelineException
	    ("Unable to remove the old editor menu layout file (" + file + ")!");
      }
      
      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Writing Editor Menu Layout.");

      try {
	String glue = null;
	try {
	  GlueEncoder ge = new GlueEncoderImpl("EditorMenuLayout", pEditorMenuLayout);
	  glue = ge.getText();
	}
	catch(GlueException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	     "Unable to generate a Glue format representation of the editor menu layout!");
	  LogMgr.getInstance().flush();
	  
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
	   "  While attempting to write the editor menu layout file (" + file + ")...\n" + 
	   "    " + ex.getMessage());
      }
    }
  }
  
  /**
   * Read the layout of the editor plugin selection menu from disk. <P> 
   * 
   * @throws PipelineException
   *   If unable to read the menu layout file.
   */ 
  private void 
  readEditorMenuLayout () 
    throws PipelineException
  {
    synchronized(pPluginMenuLayoutLock) {
      File file = new File(PackageInfo.sNodeDir, "etc/editor-menu-layout");
      if(file.isFile()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Reading Editor Menu Layout.");

	PluginMenuLayout layout = null;
	try {
	  FileReader in = new FileReader(file);
	  GlueDecoder gd = new GlueDecoderImpl(in);
	  layout = (PluginMenuLayout) gd.getObject();
	  in.close();
	}
	catch(Exception ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	     "The editor menu layout file (" + file + ") appears to be corrupted!");
	  LogMgr.getInstance().flush();
	  
	  throw new PipelineException
	    ("I/O ERROR: \n" + 
	     "  While attempting to read the editor menu layout file (" + file + ")...\n" + 
	   "    " + ex.getMessage());
	}
	assert(layout != null);
	
	pEditorMenuLayout = layout;
      }
      else {
	pEditorMenuLayout = new PluginMenuLayout();
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the layout of the comparator plugin selection menu to disk. <P> 
   * 
   * @throws PipelineException
   *   If unable to write the menu layout file.
   */ 
  private void 
  writeComparatorMenuLayout() 
    throws PipelineException
  {
    synchronized(pPluginMenuLayoutLock) {
      File file = new File(PackageInfo.sNodeDir, "etc/comparator-menu-layout");
      if(file.exists()) {
	if(!file.delete())
	  throw new PipelineException
	    ("Unable to remove the old comparator menu layout file (" + file + ")!");
      }
      
      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Writing Comparator Menu Layout.");

      try {
	String glue = null;
	try {
	  GlueEncoder ge = new GlueEncoderImpl("ComparatorMenuLayout", pComparatorMenuLayout);
	  glue = ge.getText();
	}
	catch(GlueException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	     "Unable to generate a Glue format representation of the comparator " + 
	     "menu layout!");
	  LogMgr.getInstance().flush();
	  
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
	   "  While attempting to write the comparator menu layout file " + 
	   "(" + file + ")...\n" + 
	   "    " + ex.getMessage());
      }
    }
  }
  
  /**
   * Read the layout of the comparator plugin selection menu from disk. <P> 
   * 
   * @throws PipelineException
   *   If unable to read the menu layout file.
   */ 
  private void 
  readComparatorMenuLayout () 
    throws PipelineException
  {
    synchronized(pPluginMenuLayoutLock) {
      File file = new File(PackageInfo.sNodeDir, "etc/comparator-menu-layout");
      if(file.isFile()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Reading Comparator Menu Layout.");

	PluginMenuLayout layout = null;
	try {
	  FileReader in = new FileReader(file);
	  GlueDecoder gd = new GlueDecoderImpl(in);
	  layout = (PluginMenuLayout) gd.getObject();
	  in.close();
	}
	catch(Exception ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	     "The comparator menu layout file (" + file + ") appears to be corrupted!");
	  LogMgr.getInstance().flush();
	  
	  throw new PipelineException
	    ("I/O ERROR: \n" + 
	     "  While attempting to read the comparator menu layout file " + 
	     "(" + file + ")...\n" + 
	   "    " + ex.getMessage());
	}
	assert(layout != null);
	
	pComparatorMenuLayout = layout;
      }
      else {
	pComparatorMenuLayout = new PluginMenuLayout();
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the layout of the tool plugin selection menu to disk. <P> 
   * 
   * @throws PipelineException
   *   If unable to write the menu layout file.
   */ 
  private void 
  writeToolMenuLayout() 
    throws PipelineException
  {
    synchronized(pPluginMenuLayoutLock) {
      File file = new File(PackageInfo.sNodeDir, "etc/tool-menu-layout");
      if(file.exists()) {
	if(!file.delete())
	  throw new PipelineException
	    ("Unable to remove the old tool menu layout file (" + file + ")!");
      }
      
      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Writing Tool Menu Layout.");

      try {
	String glue = null;
	try {
	  GlueEncoder ge = new GlueEncoderImpl("ToolMenuLayout", pToolMenuLayout);
	  glue = ge.getText();
	}
	catch(GlueException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	     "Unable to generate a Glue format representation of the tool menu layout!");
	  LogMgr.getInstance().flush();
	  
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
	   "  While attempting to write the tool menu layout file (" + file + ")...\n" + 
	   "    " + ex.getMessage());
      }
    }
  }
  
  /**
   * Read the layout of the tool plugin selection menu from disk. <P> 
   * 
   * @throws PipelineException
   *   If unable to read the menu layout file.
   */ 
  private void 
  readToolMenuLayout () 
    throws PipelineException
  {
    synchronized(pPluginMenuLayoutLock) {
      File file = new File(PackageInfo.sNodeDir, "etc/tool-menu-layout");
      if(file.isFile()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Reading Tool Menu Layout.");

	PluginMenuLayout layout = null;
	try {
	  FileReader in = new FileReader(file);
	  GlueDecoder gd = new GlueDecoderImpl(in);
	  layout = (PluginMenuLayout) gd.getObject();
	  in.close();
	}
	catch(Exception ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	     "The tool menu layout file (" + file + ") appears to be corrupted!");
	  LogMgr.getInstance().flush();
	  
	  throw new PipelineException
	    ("I/O ERROR: \n" + 
	     "  While attempting to read the tool menu layout file (" + file + ")...\n" + 
	   "    " + ex.getMessage());
	}
	assert(layout != null);
	
	pToolMenuLayout = layout;
      }
      else {
	pToolMenuLayout = new PluginMenuLayout();
      }
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
      File file = new File(PackageInfo.sNodeDir, "etc/privileged-users");
      if(file.exists()) {
	if(!file.delete())
	  throw new PipelineException
	    ("Unable to remove the old privileged users file (" + file + ")!");
      }
      
      if(!pPrivilegedUsers.isEmpty()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Writing Privileged Users.");

	try {
	  String glue = null;
	  try {
	    GlueEncoder ge = new GlueEncoderImpl("PrivilegedUsers", pPrivilegedUsers);
	    glue = ge.getText();
	  }
	  catch(GlueException ex) {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	       "Unable to generate a Glue format representation of the privileged users!");
	    LogMgr.getInstance().flush();
	    
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

      File file = new File(PackageInfo.sNodeDir, "etc/privileged-users");
      if(file.isFile()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Reading Privileged Users.");

	TreeSet<String> users = null;
	try {
	  FileReader in = new FileReader(file);
	  GlueDecoder gd = new GlueDecoderImpl(in);
	  users = (TreeSet<String>) gd.getObject();
	  in.close();
	}
	catch(Exception ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	     "The privileged users file (" + file + ") appears to be corrupted!");
	  LogMgr.getInstance().flush();
	  
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
   * Write next job/group ID to disk.
   * 
   * @throws PipelineException
   *   If unable to write the file.
   */ 
  private void 
  writeNextIDs() 
    throws PipelineException
  {
    File file = new File(PackageInfo.sNodeDir, "etc/next-ids");
    if(file.exists()) {
      if(!file.delete())
	throw new PipelineException
	  ("Unable to remove the old job/group IDs file (" + file + ")!");
    }
    
    LogMgr.getInstance().log
      (LogMgr.Kind.Glu, LogMgr.Level.Finer,
       "Writing Next IDs.");

    try {
      String glue = null;
      try {
	TreeMap<String,Long> table = new TreeMap<String,Long>();
	synchronized(pQueueSubmitLock) {
	  table.put("JobID",      pNextJobID);
	  table.put("JobGroupID", pNextJobGroupID);
	}

	GlueEncoder ge = new GlueEncoderImpl("NextIDs", table);
	glue = ge.getText();
      }
      catch(GlueException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	   "Unable to generate a Glue format representation of the job/group IDs!");
	LogMgr.getInstance().flush();
	
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
	 "  While attempting to write the job/group IDs file (" + file + ")...\n" + 
	 "    " + ex.getMessage());
    }
  }
  
  /**
   * Read a next job/group ID from disk.
   * 
   * @throws PipelineException
   *   If unable to read the file.
   */ 
  private void 
  readNextIDs() 
    throws PipelineException 
  {
    File file = new File(PackageInfo.sNodeDir, "etc/next-ids");
    if(file.exists()) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Reading Next IDs.");
      
      try {
	FileReader in = new FileReader(file);
	GlueDecoder gd = new GlueDecoderImpl(in);
	TreeMap<String,Long> table = (TreeMap<String,Long>) gd.getObject();
	in.close();

	synchronized(pQueueSubmitLock) {
	  pNextJobID      = table.get("JobID");
	  pNextJobGroupID = table.get("JobGroupID");
	}

	return;
      }
      catch(Exception ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	   "The job/group IDs file (" + file + ") appears to be corrupted!");
	LogMgr.getInstance().flush();
	
	throw new PipelineException
	  ("I/O ERROR: \n" + 
	   "  While attempting to read the job/group IDs file (" + file + ")...\n" + 
	   "    " + ex.getMessage());
      }
    }
    
    synchronized(pQueueSubmitLock) {
      pNextJobID      = 1L;
      pNextJobGroupID = 1L;
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
    LogMgr.getInstance().log
      (LogMgr.Kind.Glu, LogMgr.Level.Finer,
       "Writing Checked-In Version: " + 
       vsn.getName() + " (" + vsn.getVersionID() + ")");

    File file = new File(PackageInfo.sNodeDir, 
			 "repository/" + vsn.getName() + "/" + vsn.getVersionID());
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
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	   "Unable to generate a Glue format representation of checked-in " + 
	   "version (" + vsn.getVersionID() + ") of node (" + vsn.getName() + ")!");
	LogMgr.getInstance().flush();
	
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
    File dir = new File(PackageInfo.sNodeDir, "repository" + name + "/");
    if(!dir.isDirectory()) 
      return null;

    LogMgr.getInstance().log
      (LogMgr.Kind.Glu, LogMgr.Level.Finer,
       "Reading Checked-In Versions: " + name);

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
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	   "The checked-in version file (" + files[wk] + ") appears to be corrupted!");
	LogMgr.getInstance().flush();
	
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
    LogMgr.getInstance().log
      (LogMgr.Kind.Glu, LogMgr.Level.Finer,
       "Writing Working Version: " + id);

    File file   = new File(PackageInfo.sNodeDir, id.getWorkingPath().getPath());
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
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	   "Unable to generate a Glue format representation of working " + 
	   "version (" + id + ")!");
	LogMgr.getInstance().flush();
	
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
    File file   = new File(PackageInfo.sNodeDir, id.getWorkingPath().getPath());
    File backup = new File(file + ".backup");
    
    try {
      if(file.exists()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Reading Working Version: " + id);

	try {
	  FileReader in = new FileReader(file);
	  GlueDecoder gd = new GlueDecoderImpl(in);
	  NodeMod mod = (NodeMod) gd.getObject();
	  in.close();
	  
	  return mod;
	}
	catch(Exception ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	    "The working version file (" + file + ") appears to be corrupted!");
	  LogMgr.getInstance().flush();
	  
	  if(backup.exists()) {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	       "Reading Working Version (Backup): " + id);

	    NodeMod mod = null;
	    try {
	      FileReader in = new FileReader(backup);
	      GlueDecoder gd = new GlueDecoderImpl(in);
	      mod = (NodeMod) gd.getObject();
	      in.close();
	    }
	    catch(Exception ex2) {
	      LogMgr.getInstance().log
		(LogMgr.Kind.Glu, LogMgr.Level.Severe,
		"The backup working version file (" + backup + ") appears to be corrupted!");
	      LogMgr.getInstance().flush();
	      
	      throw ex;
	    }
	    
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	       "Successfully recovered the working version from the backup file " + 
	       "(" + backup + ")\n" + 
	       "Renaming the backup to (" + file + ")!");
	    LogMgr.getInstance().flush();
	    
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
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	      "The backup working version file (" + backup + ") does not exist!");
	    LogMgr.getInstance().flush();
	
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
   * If no working or checked-in versions exist for the node, the downstream links file
   * associated with the node will be removed instead of being written.  See the 
   * @{link DownstreamLinks#hasLinks DownstreamLinks.hasLink} method for details. <P> 
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
    File file = new File(PackageInfo.sNodeDir, "downstream/" + links.getName());
    File dir  = file.getParentFile();

    if(!links.hasLinks()) {
      if(file.isFile()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Removing Obsolete Downstream Links: " + links.getName());

	if(!file.delete()) 
	  throw new PipelineException
	    ("Unable to delete obsolete downstream links file (" + file + ")!");
      }

      return;
    }
    
    try {
      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Writing Downstream Links: " + links.getName());

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
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	   "Unable to generate a Glue format representation of the downstream links " + 
	   "for (" + links.getName() + ")!");
	LogMgr.getInstance().flush();
	
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
    File file = new File(PackageInfo.sNodeDir, "downstream/" + name);
    
    try {
      if(file.exists()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Reading Downstream Links: " + name);

	try {
	  FileReader in = new FileReader(file);
	  GlueDecoder gd = new GlueDecoderImpl(in);
	  DownstreamLinks links = (DownstreamLinks) gd.getObject();
	  in.close();
	  
	  return links;
	}
	catch(Exception ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe,  
	    "The downstream links file (" + file + ") appears to be corrupted!");
	  LogMgr.getInstance().flush();
	
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
  /*   N O D E   T R E E   H E L P E R S                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Recursively check the names of all nodes in the given working area against the given 
   * regular expression pattern.
   * 
   * @param author
   *   The name of the user owning the working area.
   * 
   * @param view 
   *   The name of the working area.
   * 
   * @param pattern
   *   The regular expression used to match the fully resolved node name.
   * 
   * @param path
   *   The current partial node name path.
   * 
   * @param entry
   *   The current node tree entry. 
   * 
   * @param matches
   *   The fully resolved names of the matching nodes.
   * 
   * @return 
   *   The names of the working areas indexed by fully resolved node name and author.
   */ 
  private void 
  matchingWorkingNodes
  (
   String author, 
   String view, 
   Pattern pattern, 
   String path,
   NodeTreeEntry entry, 
   TreeSet<String> matches
  ) 
  {
    String name = (path + "/" + entry.getName());
    if(entry.isLeaf() && entry.hasWorking(author, view)) {
      if((pattern == null) || pattern.matcher(name).matches()) 
	matches.add(name);
    }
    else {
      for(NodeTreeEntry child : entry.values())
	matchingWorkingNodes(author, view, pattern, name, child, matches);
    }
  }

  /** 
   * Recursively check the names of all nodes with at least one checked-in versions 
   * against the given regular expression pattern.
   * 
   * @param pattern
   *   The regular expression used to match the fully resolved node name.
   * 
   * @param path
   *   The current partial node name path.
   * 
   * @param entry
   *   The current node tree entry. 
   * 
   * @return 
   *   The names of the working areas indexed by fully resolved node name and author.
   */ 
  private void 
  matchingCheckedInNodes
  ( 
   Pattern pattern, 
   String path,
   NodeTreeEntry entry,
   TreeMap<String,TreeMap<String,TreeSet<String>>> matches
  ) 
  {
    String name = (path + "/" + entry.getName());
    if(entry.isLeaf() && entry.isCheckedIn()) {
      if((pattern == null) || pattern.matcher(name).matches()) {
	TreeMap<String,TreeSet<String>> areas = new TreeMap<String,TreeSet<String>>();
	for(String author : entry.getWorkingAuthors()) 
	  areas.put(author, new TreeSet<String>(entry.getWorkingViews(author)));
	matches.put(name, areas);
      }
    }
    else {
      for(NodeTreeEntry child : entry.values())
	matchingCheckedInNodes(pattern, name, child, matches);
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
     NodeCheckInReq req, 
     VersionID rootVersionID
    ) 
    {
      super();
      
      pRequest       = req;
      pRootVersionID = rootVersionID; 
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

      case Missing:
      case MissingNewer:
	throw new PipelineException
	  ("The working version of node (" + name + ") was in a " + 
	   details.getOverallNodeState() + " state!\n\n" + 
	   "The missing files must be created or regenerated before the node can be " +
	   "checked-in.");

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

	    VersionID.Level level = pRequest.getLevel();
	    if(level == null) 
	      level = VersionID.Level.Minor;
	    vid = new VersionID(latestID, level);
	  }

	  WorkingBundle working = getWorkingBundle(nodeID);
	  NodeMod work = working.uVersion;

	  if(work.isFrozen()) 
	    throw new PipelineException
	      ("Somehow a frozen node (" + name + ") was erroneously " + 
	       "submitted for check-in!");

	  /* make sure the action is NOT under development */ 
	  {
	    work.updateAction();

	    BaseAction action = work.getAction();
	    if((action != null) && action.isUnderDevelopment()) {
	      throw new PipelineException 
		("The node (" + name + ") cannot be checked-in because its Action plugin " +
		 "(" + action.getName() + " v" + action.getVersionID() + ") is currently " +
		 "under development!");
	    }
	  }

	  /* determine the checked-in revision numbers of the upstream nodes */ 
	  TreeMap<String,VersionID> lvids = new TreeMap<String,VersionID>();
	  for(NodeStatus lstatus : status.getSources()) {
	    VersionID lvid = lstatus.getDetails().getBaseVersion().getVersionID();
	    lvids.put(lstatus.getName(), lvid);
	  }
	  
	  /* build the file novelty table */ 
	  TreeMap<FileSeq,boolean[]> isNovel = new TreeMap<FileSeq,boolean[]>();

 	  for(FileSeq fseq : details.getFileStateSequences()) {
 	    FileState[] states = details.getFileState(fseq);
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
	  {
	    FileMgrClient fclient = getFileMgrClient();
	    try {
	      fclient.checkIn(nodeID, work, vid, latestID, isNovel);
	    }
	    finally {
	      freeFileMgrClient(fclient);
	    }
	  }

	  /* create a new checked-in version and write it disk */ 
	  NodeVersion vsn = 
	    new NodeVersion(work, vid, lvids, isNovel, 
			    pRequest.getNodeID().getAuthor(), pRequest.getMessage(), 
			    pRequest.getNodeID().getName(), pRootVersionID);

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
	  Long[] jobIDs = null;
	  QueueState[] queueStates = null;
	  boolean[] ignoreStamps = null;
	  {
	    for(FileSeq fseq : details.getFileStateSequences()) {
	      FileState fs[] = new FileState[fseq.numFrames()];
	      Date stamps[] = new Date[fseq.numFrames()];

	      if(jobIDs == null) 
		jobIDs = new Long[fs.length];

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

	    {
	      ignoreStamps = new boolean[queueStates.length];
	      int wk;
	      for(wk=0; wk<ignoreStamps.length; wk++) 
		ignoreStamps[wk] = true;
	    }
	  }

	  /* create a new working version and write it to disk */ 
	  NodeMod nwork = new NodeMod(vsn, work.getLastCriticalModification(), false);
	  writeWorkingVersion(nodeID, nwork);

	  /* update the working bundle */ 
	  working.uVersion    = nwork;

	  /* update the node status details */ 
	  NodeDetails ndetails = 
	    new NodeDetails(name, 
			    nwork, vsn, checkedIn.get(checkedIn.lastKey()).uVersion,
			    checkedIn.keySet(), 
			    OverallNodeState.Identical, OverallQueueState.Finished, 
			    VersionState.Identical, PropertyState.Identical, 
			    LinkState.Identical, 
			    fileStates, details.getFileTimeStamps(), ignoreStamps, 
			    jobIDs, queueStates);

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

    /**
     * The revision number of the new version of the root node created by the check-in 
     * operation.
     */ 
    private VersionID  pRootVersionID; 
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
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The maximum age of a resolved (Restored or Denied) restore request before it 
   * is deleted (in milliseconds).
   */ 
  private static final long  sRestoreCleanupInterval = 172800000L;  /* 48-hours */ 
  
 
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Whether to command the queue manager to shutdown all job servers before exiting.
   */ 
  private AtomicBoolean  pShutdownJobMgrs; 
    
  /**
   * Whether to shutdown the plugin manager before exiting.
   */ 
  private AtomicBoolean  pShutdownPluginMgr;


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The master database lock. <P> 
   * 
   * All operations which will access any data which is backed by the filesystem should 
   * be protected this lock in read lock mode.  Any operation which require that the entire
   * contents of the database remain constant during the operation should aquire the write
   * mode lock. The scope of this lock should enclose all other locks for an operation. <P> 
   * 
   * This lock exists primary to support write-locked database backups and node deletion. <P> 
   */ 
  private ReentrantReadWriteLock  pDatabaseLock;

  /**
   * The file system directory creation lock.
   */
  private Object  pMakeDirLock;


  /*----------------------------------------------------------------------------------------*/

  /**
   * A lock which serializes access to the archive file I/O operations.
   */ 
  private Object pArchiveFileLock; 

  /**	
   * The cached names of the archive volumes indexed by the fully resolved node names and 
   * revision numbers of the checked-in versions contained in the archive. <P> 
   * 
   * This table is rebuilt by scanning the archive GLUE files upon startup but is not itself
   * written to disk. <P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */
  private TreeMap<String,TreeMap<VersionID,TreeSet<String>>>  pArchivedIn;

  /**
   * The timestamps of when each archive volume was created indexed by unique archive 
   * volume name.
   *	 
   * This table is rebuilt by scanning the archive GLUE files upon startup but is not itself
   * written to disk. <P> 
   */
  private TreeMap<String,Date>  pArchivedOn;

  /**
   * The timestamps of when versions from each archive volume was was created indexed by 
   * unique archive volume name.
   *	 
   * This table is rebuilt by scanning the restore output filenames upon startup but is 
   * not itself written to disk. <P> 
   */
  private TreeMap<String,TreeSet<Date>>  pRestoredOn;

  /**
   * The fully resolved node names and revision numbers of the checked-in versions which
   * are currently offline. <P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */ 
  private TreeMap<String,TreeSet<VersionID>>  pOfflined;

  /**
   * Thre pending restore requests indexed by the fully resolved node names and 
   * revision numbers of the checked-in versions to restore. <P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */  
  private TreeMap<String,TreeMap<VersionID,RestoreRequest>>  pRestoreReqs;  


  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the default toolset.<P> 
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
   * The plugin menu layouts lock.
   * 
   * Access to the <CODE>pEditorMenuLayout</CODE> and <CODE>pToolMenuLayout</CODE> fields 
   * should be protected by a synchronized block on this field.
   */ 
  private Object pPluginMenuLayoutLock; 

  /**
   * The cached layout of the editor plugin selection menu or <CODE>null</CODE> if none 
   * is defined.
   */ 
  private PluginMenuLayout  pEditorMenuLayout;
   
  /**
   * The cached layout of the comparator plugin selection menu or <CODE>null</CODE> if none 
   * is defined.
   */ 
  private PluginMenuLayout  pComparatorMenuLayout;
   
  /**
   * The cached layout of the tool plugin selection menu or <CODE>null</CODE> if none 
   * is defined.
   */ 
  private PluginMenuLayout  pToolMenuLayout;


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
   * The working version related information of nodes indexed by fully resolved node 
   * name and working version node ID.
   */ 
  private HashMap<String,HashMap<NodeID,WorkingBundle>>  pWorkingBundles;
 

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
   * A pool of inactive connections to the file manager daemon: <B>plfilemgr<B>(1). <P> 
   * 
   * This field should not be access directly.  Instead a file manager connection should 
   * be obtained with the {@link #getFileMgrClient getFileMgrClient} method and returned
   * to the inactive pool with {@link #freeFileMgrClient freeFileMgrClient}.
   */ 
  private Stack<FileMgrClient>  pFileMgrClients;
 

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The connection to the queue manager daemon: <B>plqueuemgr<B>(1).
   */ 
  private QueueMgrControlClient  pQueueMgrClient;
  

  /**
   * A lock used to serialize submissions of jobs to the queue.
   */ 
  private Object pQueueSubmitLock; 

  /**
   * The next available QueueJob identifier. <P> 
   * 
   * Access to this field should be protected by a synchronized(pQueueSubmitLock) block.
   */ 
  private long  pNextJobID; 
  
  /**
   * The next available QueueJobGroup identifier. <P> 
   * 
   * Access to this field should be protected by a synchronized(pQueueSubmitLock) block.
   */ 
  private long  pNextJobGroupID; 

}

