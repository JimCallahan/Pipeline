// $Id: MasterMgr.java,v 1.182 2006/12/12 00:06:44 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.message.*;
import us.temerity.pipeline.toolset.*;
import us.temerity.pipeline.core.exts.*;
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
   * @param rebuildCache
   *   Whether to rebuild cache files and ignore existing lock files.
   * 
   * @param preserveOfflinedCache
   *   Whether to keep the offlined versions cache file after startup and reread instead of 
   *   rebuilding it during a database rebuild.
   * 
   * @param internalFileMgr
   *   Whether the file manager should be run as a thread of plmaster(1).
   * 
   * @param avgNodeSize
   *   The estimated memory size of a node version (in bytes).
   * 
   * @param minOverhead
   *   The minimum amount of memory overhead to maintain at all times.
   * 
   * @param maxOverhead
   *   The maximum amount of memory overhead required to be available after a node garbage
   *   collection.
   * 
   * @param nodeGCInterval
   *   The minimum time a cycle of the node cache garbage collector loop should 
   *   take (in milliseconds).
   * 
   * @param restoreCleanupInterval
   *   The maximum age of a resolved (Restored or Denied) restore request before it 
   *   is deleted (in milliseconds).
   * 
   * @throws PipelineException 
   *   If unable to properly initialize the manager.
   */
  public
  MasterMgr
  (
   boolean rebuildCache, 
   boolean preserveOfflinedCache, 
   boolean internalFileMgr, 
   long avgNodeSize, 
   long minOverhead, 
   long maxOverhead, 
   long nodeGCInterval, 
   long restoreCleanupInterval
  )
    throws PipelineException 
  { 
    pRebuildCache          = rebuildCache;
    pPreserveOfflinedCache = preserveOfflinedCache;
    pInternalFileMgr       = internalFileMgr;

    pNodeCacheSize = new AtomicLong(0L);

    if(avgNodeSize <= 2048L) 
      throw new PipelineException
	("The average node size (" + avgNodeSize + " bytes) must be at least 2K!");
    pAverageNodeSize = new AtomicLong(avgNodeSize); 

    if(minOverhead <= 8388608L) 
      throw new PipelineException 
	("The minimum memory overhead (" + minOverhead + " bytes) must at least 8M!"); 
    if(maxOverhead <= 16777216L) 
      throw new PipelineException 
	("The maximum memory overhead (" + maxOverhead + " bytes) must at least 16M!"); 
    if(maxOverhead <= minOverhead) 
      throw new PipelineException 
	("The maximum memory overhead (" + maxOverhead + " bytes) must greater-than the " + 
	 "minimum memory overhead (" + minOverhead + " bytes)!"); 
    pMinimumOverhead = new AtomicLong(minOverhead); 
    pMaximumOverhead = new AtomicLong(maxOverhead);

    if(nodeGCInterval < 15000L) 
      throw new PipelineException
	("The node garbage collection interval (" + nodeGCInterval + " ms) must be at " +
	 "least 15 seconds!");
    pNodeGCInterval  = new AtomicLong(nodeGCInterval); 

    if(restoreCleanupInterval < 3600000L) 
      throw new PipelineException
	("The restore cleanup interval (" + restoreCleanupInterval + " ms) must be at " + 
	 "least 1 hour!"); 
    pRestoreCleanupInterval = new AtomicLong(restoreCleanupInterval); 

    if(PackageInfo.sOsType != OsType.Unix)
      throw new IllegalStateException("The OS type must be Unix!");
    pNodeDir = PackageInfo.sNodePath.toFile();

    pShutdownJobMgrs   = new AtomicBoolean(false);
    pShutdownPluginMgr = new AtomicBoolean(false);

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Info,
       "Establishing Network Connections [PluginMgr FileMgr QueueMgr]...");
    LogMgr.getInstance().flush();

    {
      /* initialize the plugins */ 
      PluginMgrClient.init();

      /* initialize the internal file manager instance */ 
      if(pInternalFileMgr) {
	pFileMgrDirectClient = new FileMgrDirectClient();
      }
      /* make a connection to the remote file manager */ 
      else {
	pFileMgrNetClients = new Stack<FileMgrNetClient>();
	
	FileMgrNetClient fclient = (FileMgrNetClient) getFileMgrClient();
	try {
	  fclient.waitForConnection(1000, 5000);
	}
	finally {
	  freeFileMgrClient(fclient);
	}
      }
      
      /* make a connection to the queue manager */ 
      pQueueMgrClient = new QueueMgrControlClient();
      pQueueMgrClient.waitForConnection(1000, 5000);
    }

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Info,
       "Initializing [MasterMgr]...");
    LogMgr.getInstance().flush();

    /* Make sure that the root node directories exist. */ 
    makeRootDirs();

    /* validate startup state */ 
    if(pRebuildCache) {
      removeLockFile();
      removeDownstreamLinksCache(); 
      removeNodeTreeCache();
      removeArchivesCache();
    }
    else {
      File lock  = new File(pNodeDir, "lock");
      File dwns  = new File(pNodeDir, "downstream");
      File ntree = new File(pNodeDir, "etc/node-tree");
      if(lock.exists() || !dwns.exists() || !ntree.exists())
	throw new PipelineException 
	  ("Another plmaster(1) process may already running or was improperly shutdown!\n" + 
	   "If you are certain no other plmaster(1) process is running, you may restart " +
	   "plmaster(1) using the --rebuild option.");
    }
      
    /* create the lock file */ 
    createLockFile();

    /* initialize the fields */ 
    {
      pDatabaseLock = new ReentrantReadWriteLock();

      pAdminPrivileges = new AdminPrivileges();

      pArchiveFileLock    = new Object();
      pArchivedIn         = new TreeMap<String,TreeMap<VersionID,TreeSet<String>>>();
      pArchivedOn         = new TreeMap<String,Date>();
      pRestoredOn         = new TreeMap<String,TreeSet<Date>>();
      pOnlineOfflineLocks = new HashMap<String,ReentrantReadWriteLock>();
      pOfflined           = new TreeMap<String,TreeSet<VersionID>>();
      pRestoreReqs        = new TreeMap<String,TreeMap<VersionID,RestoreRequest>>();

      pDefaultToolsetLock = new Object();
      pDefaultToolset     = null;
      pActiveToolsets     = new TreeSet<String>();
      pToolsets           = new TreeMap<String,TreeMap<OsType,Toolset>>();
      pToolsetPackages    = new TripleMap<String,OsType,VersionID,PackageVersion>();

      pEditorMenuLayouts     = new TreeMap<String,PluginMenuLayout>();
      pComparatorMenuLayouts = new TreeMap<String,PluginMenuLayout>();
      pActionMenuLayouts     = new TreeMap<String,PluginMenuLayout>();
      pToolMenuLayouts       = new TreeMap<String,PluginMenuLayout>();
  
      pArchiverMenuLayouts       = new TreeMap<String,PluginMenuLayout>();
      pDefaultArchiverMenuLayout = new PluginMenuLayout();  // ???

      pMasterExtMenuLayouts  = new TreeMap<String,PluginMenuLayout>();
      pQueueExtMenuLayouts   = new TreeMap<String,PluginMenuLayout>();

      pPackageEditorPlugins     = new DoubleMap<String,VersionID,PluginSet>();
      pPackageComparatorPlugins = new DoubleMap<String,VersionID,PluginSet>();
      pPackageActionPlugins     = new DoubleMap<String,VersionID,PluginSet>();
      pPackageToolPlugins       = new DoubleMap<String,VersionID,PluginSet>();
      pPackageArchiverPlugins   = new DoubleMap<String,VersionID,PluginSet>();
      pPackageMasterExtPlugins  = new DoubleMap<String,VersionID,PluginSet>();
      pPackageQueueExtPlugins   = new DoubleMap<String,VersionID,PluginSet>();

      pMasterExtensions = new TreeMap<String,MasterExtensionConfig>();

      pSuffixEditors = new DoubleMap<String,String,SuffixEditor>();

      pWorkingAreaViews = new TreeMap<String,TreeSet<String>>();
      pNodeTree         = new NodeTree();

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
      initPrivileges();
      initArchives();
      initToolsets();
      initMasterExtensions();
      initWorkingAreas();
      initDownstreamLinks();
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
   * Load the work group and administrative privileges.
   */ 
  private void 
  initPrivileges()
    throws PipelineException
  {
    TaskTimer timer = new TaskTimer();
    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       "Loading Privileges...");   
    LogMgr.getInstance().flush();

    {
      pAdminPrivileges.readAll();
      updateAdminPrivileges();
    }

    timer.suspend();
    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Info,
       "  Loaded in " + Dates.formatInterval(timer.getTotalDuration()));
    LogMgr.getInstance().flush();
  }

  /**
   * Update the other servers with the latest copy of the administrative privileges.
   */ 
  private void 
  updateAdminPrivileges() 
    throws PipelineException
  {
    pQueueMgrClient.updateAdminPrivileges(pAdminPrivileges);    

    PluginMgrControlClient client = new PluginMgrControlClient();
    try {
      client.updateAdminPrivileges(pAdminPrivileges);
    }
    finally {
      client.disconnect();
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
    if(!pNodeDir.isDirectory()) 
      throw new PipelineException
	("The root node directory (" + pNodeDir + ") does not exist!");
    
    ArrayList<File> dirs = new ArrayList<File>();
    dirs.add(new File(pNodeDir, "repository"));
    dirs.add(new File(pNodeDir, "working"));
    dirs.add(new File(pNodeDir, "toolsets/packages"));
    dirs.add(new File(pNodeDir, "toolsets/toolsets"));
    dirs.add(new File(pNodeDir, "toolsets/plugins/packages"));
    dirs.add(new File(pNodeDir, "toolsets/plugins/toolsets"));
    dirs.add(new File(pNodeDir, "etc"));
    dirs.add(new File(pNodeDir, "etc/suffix-editors"));
    dirs.add(new File(pNodeDir, "archives/manifests"));
    dirs.add(new File(pNodeDir, "archives/output/archive"));
    dirs.add(new File(pNodeDir, "archives/output/restore"));

    pMakeDirLock = new Object();
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
   * Remove archive related cache files.
   */ 
  private void 
  removeArchivesCache()
  {
    File archivedIn = new File(pNodeDir, "archives/archived-in");
    if(archivedIn.exists())
      archivedIn.delete();

    File archivedOn = new File(pNodeDir, "archives/archived-on");
    if(archivedOn.exists())
      archivedOn.delete();
    
    File restoredOn = new File(pNodeDir, "archives/restored-on");
    if(restoredOn.exists())
      restoredOn.delete();
    
    if(!pPreserveOfflinedCache) 
      removeOfflinedCache();
  }

  /**
   * Remove offlined versions cache files.
   */ 
  private void 
  removeOfflinedCache()
  {
    File offlined = new File(pNodeDir, "archives/offlined");
    if(offlined.exists())
      offlined.delete();
  }

  /**
   * Load the archives.
   */ 
  private void 
  initArchives()
    throws PipelineException
  {
    if(pRebuildCache) {
      /* scan archive volume GLUE files */ 
      {
	TaskTimer timer = new TaskTimer();
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Info,
	   "Rebuilding ArchiveOn Cache...");   
	LogMgr.getInstance().flush();
	
	{
	  File dir = new File(pNodeDir, "archives/manifests");
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
	
	timer.suspend();
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Info,
	   "  Rebuilt in " + Dates.formatInterval(timer.getTotalDuration()));
	LogMgr.getInstance().flush();
      }
 
      /* scan restore output files */ 
      {
	TaskTimer timer = new TaskTimer();
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Info,
	   "Rebuilding RestoredOn Cache...");   
	LogMgr.getInstance().flush();

	{
	  File dir = new File(pNodeDir, "archives/output/restore");
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

	timer.suspend();
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Info,
	   "  Rebuilt in " + Dates.formatInterval(timer.getTotalDuration()));
	LogMgr.getInstance().flush();
      }

      /* rebuild (or reread) offlined versions cache */ 
      {
	File offlined = new File(pNodeDir, "archives/offlined");
	if(pPreserveOfflinedCache && offlined.exists()) {
	  readOfflined();
	}
	else {
	  TaskTimer timer = new TaskTimer();
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Ops, LogMgr.Level.Info,
	     "Rebuilding Offlined Cache...");    
	  LogMgr.getInstance().flush();

	  FileMgrClient fclient = getFileMgrClient();
	  try {
	    pOfflined = fclient.getOfflined();
	  }
	  finally {
	    freeFileMgrClient(fclient);
	  }

	  timer.suspend();
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Net, LogMgr.Level.Info,
	     "  Rebuilt in " + Dates.formatInterval(timer.getTotalDuration()));
	  LogMgr.getInstance().flush();
	}
      }
    }
    else {
      TaskTimer timer = new TaskTimer();
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Info,
	 "Loading Archive Caches...");   
      LogMgr.getInstance().flush();

      readArchivedIn();
      readArchivedOn();
      readRestoredOn();
      readOfflined();

      removeArchivesCache();

      timer.suspend();
      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Info,
	 "  Loaded in " + Dates.formatInterval(timer.getTotalDuration()));
      LogMgr.getInstance().flush();
    }

    readRestoreReqs();
  }
  

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Create the lock file 
   */ 
  private void 
  createLockFile()
    throws PipelineException 
  {
    File file = new File(pNodeDir, "lock");
    try {
      FileWriter out = new FileWriter(file);
      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException 
	  ("Unable to create lock file (" + file + ")!");
    }
  }

  /**
   * Remove the lock file.
   */
  private void 
  removeLockFile() 
  {
    File file = new File(pNodeDir, "lock");
    if(file.exists())
      file.delete();
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Load the toolset and toolset package indices.
   */ 
  private void 
  initToolsets()
    throws PipelineException
  {
    TaskTimer timer = new TaskTimer();
    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       "Loading Toolsets...");   
    LogMgr.getInstance().flush();

    readDefaultToolset();
    readActiveToolsets();

    /* initialize default plugin menu layouts */ 
    pDefaultEditorMenuLayout = 
      readPluginMenuLayout(null, "editor", 
			   pEditorMenuLayouts);

    pDefaultComparatorMenuLayout = 
      readPluginMenuLayout(null, "comparator", 
			   pComparatorMenuLayouts); 
    
    pDefaultActionMenuLayout = 
      readPluginMenuLayout(null, "action", 
			   pActionMenuLayouts); 
    
    pDefaultToolMenuLayout = 
      readPluginMenuLayout(null, "tool", 
			   pToolMenuLayouts); 
    
    pDefaultArchiverMenuLayout = 
      readPluginMenuLayout(null, "archiver", 
			   pArchiverMenuLayouts); 
    
    pDefaultMasterExtMenuLayout = 
      readPluginMenuLayout(null, "master extension", 
			   pMasterExtMenuLayouts); 
    
    pDefaultArchiverMenuLayout = 
      readPluginMenuLayout(null, "queue extension", 
			   pQueueExtMenuLayouts); 

    /* initialize toolsets */ 
    {
      File dir = new File(pNodeDir, "toolsets/toolsets");
      File tsets[] = dir.listFiles(); 
      int tk;
      for(tk=0; tk<tsets.length; tk++) {
	if(tsets[tk].isDirectory()) {
	  String tname = tsets[tk].getName();
	  boolean hasToolset = false;
	  for(OsType os : OsType.all()) {
	    File file = new File(tsets[tk], os.toString());
	    if(file.isFile()) {
	      hasToolset = true;

	      TreeMap<OsType,Toolset> toolsets = pToolsets.get(tname);
	      if(toolsets == null) {
		toolsets = new TreeMap<OsType,Toolset>();
		pToolsets.put(tname, toolsets);
	      }

	      toolsets.put(os, null);
	    }
	  }

	  if(hasToolset) {
	    readPluginMenuLayout(tname, "editor", 
				 pEditorMenuLayouts);

	    readPluginMenuLayout(tname, "comparator", 
				 pComparatorMenuLayouts); 

	    readPluginMenuLayout(tname, "action", 
				 pActionMenuLayouts); 

	    readPluginMenuLayout(tname, "tool", 
				 pToolMenuLayouts); 

	    readPluginMenuLayout(tname, "archiver", 
				 pArchiverMenuLayouts); 

	    readPluginMenuLayout(tname, "master extension", 
				 pMasterExtMenuLayouts); 

	    readPluginMenuLayout(tname, "queue extension", 
				 pQueueExtMenuLayouts); 
	  }
	}
      }
    }

    /* initialize package keys and plugin tables */ 
    {
      File dir = new File(pNodeDir, "toolsets/packages");
      File pkgs[] = dir.listFiles(); 
      int pk;
      for(pk=0; pk<pkgs.length; pk++) {
	if(pkgs[pk].isDirectory()) {
	  String pname = pkgs[pk].getName();
	  for(OsType os : OsType.all()) {
	    File osdir = new File(pkgs[pk], os.toString());
	    if(osdir.isDirectory()) {
	      File vsns[] = osdir.listFiles(); 
	      int vk;
	      for(vk=0; vk<vsns.length; vk++) {
		if(vsns[vk].isFile()) {
		  VersionID vid = new VersionID(vsns[vk].getName());

		  pToolsetPackages.put(pname, os, vid, null);

		  switch(os) {
		  case Unix:
		    readPackagePlugins(pname, vid, "editor", 
				       pPackageEditorPlugins);

		    readPackagePlugins(pname, vid, "comparator", 
				       pPackageComparatorPlugins);

		    readPackagePlugins(pname, vid, "action", 
				       pPackageActionPlugins);

		    readPackagePlugins(pname, vid, "tool", 
				       pPackageToolPlugins);

		    readPackagePlugins(pname, vid, "archiver", 
				       pPackageArchiverPlugins);

		    readPackagePlugins(pname, vid, "master extension", 
				       pPackageMasterExtPlugins);

		    readPackagePlugins(pname, vid, "queue extension", 
				       pPackageQueueExtPlugins);
		  }
		}
	      }
	    }
	  }
	}
      }
    }

    timer.suspend();
    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Info,
       "  Loaded in " + Dates.formatInterval(timer.getTotalDuration()));
    LogMgr.getInstance().flush();
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Initialize the server extensions. 
   */
  private void 
  initMasterExtensions()
    throws PipelineException
  {
    TaskTimer timer = new TaskTimer();
    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       "Loading Extensions...");   
    LogMgr.getInstance().flush();

    {
      readMasterExtensions();
      
      synchronized(pMasterExtensions) {
	for(MasterExtensionConfig config : pMasterExtensions.values()) 
	  doPostExtensionEnableTask(config);
      }
    }

    timer.suspend();
    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Info,
       "  Loaded in " + Dates.formatInterval(timer.getTotalDuration()));
    LogMgr.getInstance().flush();    
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the table of working area views.
   */ 
  private void 
  initWorkingAreas() 
  {
    TaskTimer timer = new TaskTimer();
    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       "Loading Working Areas...");   
    LogMgr.getInstance().flush();

    {
      File dir = new File(pNodeDir, "working");
      File authors[] = dir.listFiles(); 
      int ak;
      for(ak=0; ak<authors.length; ak++) {
	if(!authors[ak].isDirectory())
	  throw new IllegalStateException
	    ("Non-directory file found in the root working area directory!"); 
	String author = authors[ak].getName();
	
	File views[] = authors[ak].listFiles();  
	int vk;
	for(vk=0; vk<views.length; vk++) {
	  if(!views[vk].isDirectory())
	    throw new IllegalStateException
	      ("Non-directory file found in the user (" + author + ") root working " + 
	     "area directory!"); 
	  String view = views[vk].getName();
	  
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
    
    timer.suspend();
    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Info,
       "  Loaded in " + Dates.formatInterval(timer.getTotalDuration()));
    LogMgr.getInstance().flush();    
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Remove any existing node tree cache file.
   */ 
  private void 
  removeNodeTreeCache()
  {
    File file = new File(pNodeDir, "etc/node-tree");
    if(file.exists())
      file.delete();
  }

  /**
   * Build the initial node name tree by searching the file system for node related files.
   */
  private void 
  initNodeTree()
    throws PipelineException 
  {
    TaskTimer timer = new TaskTimer();
    if(pRebuildCache) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Info,
	 "Rebuilding Node Tree Cache...");    
      LogMgr.getInstance().flush();
      
      {
	File dir = new File(pNodeDir, "repository");
	initCheckedInNodeTree(dir.getPath(), dir); 
      }
      
      {
	File dir = new File(pNodeDir, "working");
	File authors[] = dir.listFiles(); 
	int ak;
	for(ak=0; ak<authors.length; ak++) {
	  if(!authors[ak].isDirectory())
	    throw new IllegalStateException
	      ("Non-directory file found in the root working area directory!"); 
	  String author = authors[ak].getName();
	  
	  File views[] = authors[ak].listFiles();  
	  int vk;
	  for(vk=0; vk<views.length; vk++) {
	    if(!views[vk].isDirectory())
	      throw new IllegalStateException
		("Non-directory file found in the user (" + author + ") root working " + 
		 "area directory!"); 
	    String view = views[vk].getName();
	    
	    initWorkingNodeTree(views[vk].getPath(), author, view, views[vk]);
	  }
	}
      } 

      timer.suspend();
      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Info,
	 "  Rebuilt in " + Dates.formatInterval(timer.getTotalDuration()));
      LogMgr.getInstance().flush();
    }
    else {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Info,
	 "Loading Node Tree Cache...");   
      LogMgr.getInstance().flush();

      pNodeTree.readGlueFile(new File(pNodeDir, "etc/node-tree"));
      removeNodeTreeCache();

      timer.suspend();
      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Info,
	 "  Loaded in " + Dates.formatInterval(timer.getTotalDuration()));
      LogMgr.getInstance().flush();
    }

    pNodeTree.logNodeTree();
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
	  throw new IllegalStateException(); 
      }
    }

    if(allFiles) {
      String full = dir.getPath();
      String path = full.substring(prefix.length());
      if(path.length() > 0) {
	TreeMap<VersionID,CheckedInBundle> table = readCheckedInVersions(path);
	for(CheckedInBundle bundle : table.values()) 
	  pNodeTree.addCheckedInNodeTreePath(bundle.getVersion());
      }
    }
    else if(allDirs) {
      int wk;
      for(wk=0; wk<files.length; wk++) 
	initCheckedInNodeTree(prefix, files[wk]);
    }
    else {
      throw new IllegalStateException(); 
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
   * Remove any existing downstream link files.
   */ 
  private void 
  removeDownstreamLinksCache() 
    throws PipelineException 
  {
    File dir = new File(pNodeDir, "downstream");
    if(dir.exists()) {
      ArrayList<String> args = new ArrayList<String>();
      args.add("--recursive");
      args.add("--force");
      args.add("downstream");
      
      Map<String,String> env = System.getenv();
      
      SubProcessLight proc = 
	new SubProcessLight("RemoveDownstreamLinks", "rm", args, env, pNodeDir);
      try {
	proc.start();
	proc.join();
	if(!proc.wasSuccessful()) 
	  throw new PipelineException
	    ("Unable to remove downstream links directory (" + dir + "):\n\n" + 
	     proc.getStdErr());
      }
      catch(InterruptedException ex) {
	throw new PipelineException
	  ("Interrupted while removing the downstream links directory (" + dir + ")!");
      }
    }
  }

  /** 
   * Rebuild the downstream links from the working and checked-in version of ALL nodes! 
   */ 
  private void 
  initDownstreamLinks()
    throws PipelineException 
  {
    if(!pRebuildCache) 
      return; 

    {
      File dir = new File(pNodeDir, "downstream");
      if(dir.isDirectory()) 
	throw new PipelineException
	  ("Somehow the downstream links directory (" + dir + ") already exitsts!");

      if(!dir.mkdir()) 
	throw new IllegalArgumentException
	  ("Unable to create the downstream links directory (" + dir + ")!");
    }

    TaskTimer timer = new TaskTimer();
    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       "Rebuilding Downstream Links Cache...");   
    LogMgr.getInstance().flush(); 

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
	if(!authors[ak].isDirectory())
	  throw new IllegalStateException
	    ("Non-directory file found in the root working area directory!"); 
	String author = authors[ak].getName();
	
	File views[] = authors[ak].listFiles();  
	int vk;
	for(vk=0; vk<views.length; vk++) {
	  if(!views[vk].isDirectory())
	    throw new IllegalStateException
	      ("Non-directory file found in the user (" + author + ") root working " + 
	       "area directory!"); 
	  String view = views[vk].getName();
	  collectWorkingDownstreamLinks(author, view, views[vk].getPath(), views[vk]);
	}
      }
    }

    if(!pDownstream.isEmpty() && 
       LogMgr.getInstance().isLoggable(LogMgr.Kind.Ops, LogMgr.Level.Finer)) { 
      StringBuilder buf = new StringBuilder(); 
      buf.append("Rebuilt Links:\n");
      for(String name : pDownstream.keySet()) 
	buf.append("  " + name + "\n");
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Finer,
	 buf.toString());
    }

    /* write cached downstream links */ 
    writeAllDownstreamLinks();

    timer.suspend();
    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Info,
       "  Rebuilt in " + Dates.formatInterval(timer.getTotalDuration()));
    LogMgr.getInstance().flush();
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
	  throw new IllegalStateException(); 
      }
    }

    if(allFiles) {
      String name = dir.getPath().substring(prefix.length());
      
      TreeMap<VersionID,CheckedInBundle> table = readCheckedInVersions(name);
      for(VersionID vid : table.keySet()) {
	NodeVersion vsn = table.get(vid).getVersion();
	  
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
      throw new IllegalStateException(); 
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
    /* close the connection to the file manager */ 
    if(!pInternalFileMgr && (pFileMgrNetClients != null)) {
      while(!pFileMgrNetClients.isEmpty()) {
	FileMgrNetClient client = pFileMgrNetClients.pop();
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

    /* write the cache files */ 
    try {
      writeArchivedIn();
      writeArchivedOn();
      writeRestoredOn();
      writeOfflined();

      for(DownstreamLinks links : pDownstream.values()) 
	writeDownstreamLinks(links);

      pNodeTree.writeGlueFile(new File(pNodeDir, "etc/node-tree"));
    }
    catch(Exception ex) {
      removeArchivesCache();
       
      try {
	removeDownstreamLinksCache();
      }
      catch(Exception ex2) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Severe,
	   ex2.getMessage());	
      }

      removeNodeTreeCache();      
    }

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

    /* shutdown extensions */ 
    {
      /* disable extensions */ 
      synchronized(pMasterExtensions) {
	for(MasterExtensionConfig config : pMasterExtensions.values()) 
	  doPreExtensionDisableTask(config);
      }

      /* wait for all extension tasks to complete */ 
      try {
	BaseExtTask.joinAll();
      }
      catch(InterruptedException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Severe,
	   "Interrupted while waiting for all Extension Tasks to complete:\n  " + 
	   ex.getMessage());
      }
    }

    /* remove the lock file */ 
    removeLockFile();
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
      try {
	removeDownstreamLinksCache();
      }
      catch(Exception ex2) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Severe,
	   ex2.getMessage());	
      }
    }
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   A D M I N I S T R A T I V E   P R I V I L E G E S                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the work groups used to determine the scope of administrative privileges.
   * 
   * @return
   *   <CODE>MiscGetWorkGroupsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the work groups.
   */ 
  public Object
  getWorkGroups()
  {
    TaskTimer timer = new TaskTimer("MasterMgr.getWorkGroups()");
    
    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      return pAdminPrivileges.getWorkGroups(timer);
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }
  
  /**
   * Set the work groups used to determine the scope of administrative privileges. <P> 
   * 
   * This operation requires Master Admin privileges 
   * (see {@link Privileges#isMasterAdmin isMasterAdmin 
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to set the work groups.
   */ 
  public Object
  setWorkGroups
  (
   MiscSetWorkGroupsReq req
  )
  {
    TaskTimer timer = new TaskTimer("MasterMgr.setWorkGroups()");

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      pAdminPrivileges.setWorkGroups(timer, req);
      updateAdminPrivileges();
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
   * Get the administrative privileges for all users.
   * 
   * @return
   *   <CODE>MiscGetPrivilegesRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the privileges.
   */ 
  public Object
  getPrivileges()
  {
    TaskTimer timer = new TaskTimer("MasterMgr.getPrivileges()");
    
    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      return pAdminPrivileges.getPrivileges(timer);
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }    
  }

  /**
   * Change the administrative privileges for the given users. <P> 
   * 
   * This operation requires Master Admin privileges.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to set the privileges.
   */ 
  public Object
  editPrivileges
  (
   MiscEditPrivilegesReq req
  )
  {
    TaskTimer timer = new TaskTimer("MasterMgr.editPrivileges()");

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      pAdminPrivileges.editPrivileges(timer, req);
      updateAdminPrivileges();
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
   * Get the privileges granted to a specific user with respect to all other users. 
   *
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetPrivilegeDetailsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the privileges.
   */
  public Object
  getPrivilegeDetails
  (
   MiscGetPrivilegeDetailsReq req
  )
  {
    TaskTimer timer = new TaskTimer("MasterMgr.getPrivilegesDetails()");
    
    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      return pAdminPrivileges.getPrivilegeDetails(timer, req);
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L O G G I N G                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current logging levels.
   * 
   * @return
   *   <CODE>MiscGetLogControlsRsp</CODE>.
   */ 
  public Object
  getLogControls() 
  {
    TaskTimer timer = new TaskTimer();

    LogControls lc = new LogControls();
    {
      LogMgr mgr = LogMgr.getInstance(); 
      lc.setLevel(LogMgr.Kind.Ext, mgr.getLevel(LogMgr.Kind.Ext));
      lc.setLevel(LogMgr.Kind.Glu, mgr.getLevel(LogMgr.Kind.Glu));
      lc.setLevel(LogMgr.Kind.Ops, mgr.getLevel(LogMgr.Kind.Ops));
      lc.setLevel(LogMgr.Kind.Mem, mgr.getLevel(LogMgr.Kind.Mem));
      lc.setLevel(LogMgr.Kind.Net, mgr.getLevel(LogMgr.Kind.Net));
      lc.setLevel(LogMgr.Kind.Plg, mgr.getLevel(LogMgr.Kind.Plg));
      lc.setLevel(LogMgr.Kind.Sub, mgr.getLevel(LogMgr.Kind.Sub));
    }

    return new MiscGetLogControlsRsp(timer, lc);
  }

  /**
   * Set the current logging levels.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE>.
   */ 
  public synchronized Object
  setLogControls
  (
   MiscSetLogControlsReq req
  ) 
  {
    TaskTimer timer = new TaskTimer();

    try {
      if(!pAdminPrivileges.isMasterAdmin(req)) 
	throw new PipelineException
	  ("Only a user with Master Admin privileges may change the logging levels!");

      LogControls lc = req.getControls(); 
      {
	LogMgr mgr = LogMgr.getInstance(); 
	
	{
	  LogMgr.Level level = lc.getLevel(LogMgr.Kind.Ext);
	  if(level != null) 
	    mgr.setLevel(LogMgr.Kind.Ext, level);
	}
	
	{
	  LogMgr.Level level = lc.getLevel(LogMgr.Kind.Glu);
	  if(level != null) 
	    mgr.setLevel(LogMgr.Kind.Glu, level);
	}
	
	{
	  LogMgr.Level level = lc.getLevel(LogMgr.Kind.Ops);
	  if(level != null) 
	    mgr.setLevel(LogMgr.Kind.Ops, level);
	}
	
	{
	  LogMgr.Level level = lc.getLevel(LogMgr.Kind.Mem);
	  if(level != null) 
	    mgr.setLevel(LogMgr.Kind.Mem, level);
	}
	
	{
	  LogMgr.Level level = lc.getLevel(LogMgr.Kind.Net);
	  if(level != null) 
	    mgr.setLevel(LogMgr.Kind.Net, level);
	}
	
	{
	  LogMgr.Level level = lc.getLevel(LogMgr.Kind.Plg);
	  if(level != null) 
	    mgr.setLevel(LogMgr.Kind.Plg, level);
	}
	
	{
	  LogMgr.Level level = lc.getLevel(LogMgr.Kind.Sub);
	  if(level != null) 
	    mgr.setLevel(LogMgr.Kind.Sub, level);
	}
      }
      
      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   R U N T I M E   P A R A M E T E R S                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current runtime performance controls.
   * 
   * @return
   *   <CODE>MiscGetMasterControlsRsp</CODE>.
   */ 
  public Object
  getRuntimeControls() 
  {
    TaskTimer timer = new TaskTimer();

    MasterControls controls = 
      new MasterControls(pAverageNodeSize.get(), 
			 pMinimumOverhead.get(), pMaximumOverhead.get(), 
			 pNodeGCInterval.get(), pRestoreCleanupInterval.get());

    return new MiscGetMasterControlsRsp(timer, controls);
  }

  /**
   * Set the current runtime performance controls.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE>.
   */ 
  public synchronized Object
  setRuntimeControls
  (
   MiscSetMasterControlsReq req
  ) 
  {
    TaskTimer timer = new TaskTimer();

    try {
      if(!pAdminPrivileges.isMasterAdmin(req)) 
	throw new PipelineException
	  ("Only a user with Master Admin privileges may change the runtime parameters!");

      MasterControls controls = req.getControls();

      {
	Long size = controls.getAverageNodeSize();
	if(size != null) 
	  pAverageNodeSize.set(size); 
      }

      {
	Long min = controls.getMinimumOverhead();
	if(min == null) 
	  min = pMinimumOverhead.get();

	Long max = controls.getMaximumOverhead();
	if(max == null) 
	  max = pMaximumOverhead.get();

	if(max <= min)
	  throw new PipelineException
	    ("The maximum memory overhead (" + max + " bytes) must greater-than the " + 
	     "minimum memory overhead (" + min + " bytes)!"); 

	pMinimumOverhead.set(min);
	pMaximumOverhead.set(max);
      }

      {
	Long interval = controls.getNodeGCInterval();
	if(interval != null) 
	  pNodeGCInterval.set(interval);
      }

      {
	Long interval = controls.getRestoreCleanupInterval();
	if(interval != null) 
	  pRestoreCleanupInterval.set(interval);
      }

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   T O O L S E T S                                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the default Unix toolset.
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
   * Set the name of the default Unix toolset.
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
      if(!pAdminPrivileges.isMasterAdmin(req)) 
	throw new PipelineException
	  ("Only a user with Master Admin privileges may set the default toolset!");

      synchronized(pToolsets) {
	timer.resume();
	
	if(pToolsets.get(tname) == null) 
	  throw new PipelineException 
	    ("No toolset named (" + tname + ") exists to be made the default toolset!");

	if(!pToolsets.get(tname).containsKey(OsType.Unix)) 
	  throw new PipelineException 
	    ("The toolset (" + tname + ") cannot be made the default toolset without a " +
	     "Unix implementation!");
      }
      
      timer.aquire();
      synchronized(pDefaultToolsetLock) {
	timer.resume();	 
	
	pDefaultToolset = tname;
	writeDefaultToolset();
      }
      
      timer.aquire();
      synchronized(pActiveToolsets) {
	timer.resume();	 
	
	if(!pActiveToolsets.contains(tname)) {
	  pActiveToolsets.add(tname);
	  writeActiveToolsets();
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

  /**
   * Get the names of the currently active Unix toolsets.
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
   * Set the active/inactive state of the Unix toolset with the given name. <P> 
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
    String tname  = req.getName();
    String active = (req.isActive() ? "active" : "inactive");

    TaskTimer timer = 
      new TaskTimer("MasterMgr.setActiveToolsetName(): " + tname + " [" + active + "]");

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      if(!pAdminPrivileges.isMasterAdmin(req)) 
	throw new PipelineException
	  ("Only a user with Master Admin privileges may change the active status " + 
	   "of a toolset!");

      synchronized(pToolsets) {
	timer.resume();
	
	if(pToolsets.get(tname) == null) 
	  throw new PipelineException 
	    ("No toolset named (" + tname + ") exists to be made " + active + "!");

	if(!pToolsets.get(tname).containsKey(OsType.Unix)) 
	  throw new PipelineException 
	    ("The toolset (" + tname + ") cannot be made " + active + " without a " +
	     "Unix implementation!");
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
	
	if(changed) 
	  writeActiveToolsets();
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
		throw new PipelineException
		  ("Unable to remove the old default toolset file (" + file + ")!");
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
      pDatabaseLock.readLock().unlock();
    }
  }

  /**
   * Get the names of all toolsets for the given operating system.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetToolsetNamesRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the toolset names.
   */
  public Object
  getToolsetNames
  ( 
   MiscGetToolsetNamesReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pToolsets) {
	timer.resume();

	OsType os = req.getOsType();
	TreeSet<String> names = new TreeSet<String>();
	for(String name : pToolsets.keySet()) {
	  if(pToolsets.get(name).containsKey(os))
	    names.add(name);
	}
	
	return new MiscGetToolsetNamesRsp(timer, names);
      }
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }    
  }

  /**
   * Get the names of all toolsets for all operating systems.
   * 
   * @return
   *   <CODE>MiscGetToolsetNamesRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the toolset names.
   */
  public Object
  getAllToolsetNames()
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pToolsets) {
	timer.resume();

	TreeMap<String,TreeSet<OsType>> names = new TreeMap<String,TreeSet<OsType>>();
	for(String name : pToolsets.keySet()) 
	  names.put(name, new TreeSet<OsType>(pToolsets.get(name).keySet()));
	
	return new MiscGetAllToolsetNamesRsp(timer, names);
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
    try {
      Toolset toolset = getToolset(req.getName(), req.getOsType(), timer);
      return new MiscGetToolsetRsp(timer, toolset);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
  }
  
  /**
   * Get all OS specific toolsets with the given name. 
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetOsToolsetsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to find the toolset.
   */
  public Object
  getOsToolsets
  ( 
   MiscGetOsToolsetsReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pToolsets) {
	timer.resume();

	String tname = req.getName();

	TreeMap<OsType,Toolset> toolsets = pToolsets.get(tname);
	if(toolsets == null) 
	  throw new PipelineException 
	    ("No toolset named (" + tname + ") exists!");
	
	for(OsType os : toolsets.keySet()) {
	  Toolset toolset = toolsets.get(os);
	  if(toolset == null) 
	    toolset = readToolset(tname, os);
	  if(toolset == null)
	    throw new IllegalStateException
	      ("Toolset for (" + os + ") OS cannot be (null)!");
	}

	return new MiscGetOsToolsetsRsp(timer, toolsets);
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
   * Get the toolset with the given name.
   * 
   * @param tname
   *   The name of the toolset.
   * 
   * @param os
   *   The operating system type.
   */
  private Toolset
  getToolset
  ( 
   String tname,
   OsType os, 
   TaskTimer timer 
  )
    throws PipelineException 
  {
    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pToolsets) {
	timer.resume();

	TreeMap<OsType,Toolset> toolsets = pToolsets.get(tname);
	if(toolsets == null) 
	  throw new PipelineException 
	    ("No toolset named (" + tname + ") exists!");
	    	
	if(!toolsets.containsKey(os)) 
	  throw new PipelineException 
	    ("No toolset named (" + tname + ") for the (" + os + ") " + 
	     "operating system exists!");

	Toolset toolset = toolsets.get(os);
	if(toolset == null) 
	  toolset = readToolset(tname, os);
	if(toolset == null)
	  throw new IllegalStateException
	    ("Toolset for (" + os + ") OS cannot be (null)!");

	return toolset;
      }
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }

  /**
   * Make sure the given toolset is currently cached.
   * 
   * @param tname
   *   The name of the toolset.
   */
  private void
  cacheToolset
  ( 
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

	TreeMap<OsType,Toolset> toolsets = pToolsets.get(tname);
	if(toolsets == null) 
	  throw new PipelineException 
	    ("No toolset named (" + tname + ") exists!");

	for(OsType os : toolsets.keySet()) {
	  Toolset toolset = toolsets.get(os);
	  if(toolset == null) 
	    toolset = readToolset(tname, os);
	  if(toolset == null)
	    throw new IllegalStateException
	      ("Toolset for (" + os + ") OS cannot be (null)!");
	}
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
    try {
      timer.resume();	

      TreeMap<String,String> env = 
	getToolsetEnvironment
	  (req.getAuthor(), req.getView(), req.getName(), req.getOsType(), timer);	
      
      return new MiscGetToolsetEnvironmentRsp(timer, req.getName(), env);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
  }

  /**
   * Get the cooked toolset environments for all operating systems specific to the given user 
   * and working area. <P> 
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
  private DoubleMap<OsType,String,String>
  getToolsetEnvironments
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
	
	TreeMap<OsType,Toolset> toolsets = pToolsets.get(tname);
	if(toolsets == null) 
	  throw new PipelineException 
	    ("No toolset named (" + tname + ") exists!");
	   
	DoubleMap<OsType,String,String> envs = new DoubleMap<OsType,String,String>();
 	
	for(OsType os : toolsets.keySet()) {
	  Toolset tset = toolsets.get(os);
	  if(tset == null) 
	    tset = readToolset(tname, os);
	  if(tset == null)
	    throw new IllegalStateException
	      ("Toolset for (" + os + ") OS cannot be (null)!");
	  
	  TreeMap<String,String> env = null;
	  if((author != null) && (view != null)) 
	    env = tset.getEnvironment(author, view, os);
	  else if(author != null)
	    env = tset.getEnvironment(author, os);
	  else 
	    env = tset.getEnvironment();
	  
	  if(env == null)
	    throw new IllegalStateException
	      ("Toolset environment for (" + os + ") OS cannot be (null)!");
	  envs.put(os, env);
	}

	return envs;
      }
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
   * @param os
   *   The operating system type.
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
   OsType os,
   TaskTimer timer
  ) 
    throws PipelineException
  {
    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pToolsets) {
	timer.resume();
	
	TreeMap<OsType,Toolset> toolsets = pToolsets.get(tname);
	if(toolsets == null) 
	  throw new PipelineException 
	    ("No toolset named (" + tname + ") exists!");
	    	
	if(!toolsets.containsKey(os)) 
	  throw new PipelineException 
	    ("No toolset named (" + tname + ") for the (" + os + ") " + 
	     "operating system exists!");

	Toolset tset = toolsets.get(os);
	if(tset == null) 
	  tset = readToolset(tname, os);
	if(tset == null)
	  throw new IllegalStateException
	    ("Toolset for (" + os + ") OS cannot be (null)!");
	
	TreeMap<String,String> env = null;
	if((author != null) && (view != null)) 
	  env = tset.getEnvironment(author, view, os);
	else if(author != null)
	  env = tset.getEnvironment(author, os);
	else 
	  env = tset.getEnvironment();
 	
	if(env == null)
	  throw new IllegalStateException
	    ("Toolset environment for (" + os + ") OS cannot be (null)!");
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
      if(!pAdminPrivileges.isMasterAdmin(req)) 
	throw new PipelineException
	  ("Only a user with Master Admin privileges may create new toolsets!");

      synchronized(pToolsets) {
	timer.resume();

	String tname = req.getName();
	OsType os    = req.getOsType();

	if((pToolsets.get(tname) != null) && pToolsets.get(tname).containsKey(os)) 
	  throw new PipelineException 
	    ("Unable to create the " + os + " toolset (" + tname + ") because a " + 
	     "toolset already exists with that name!");
	
	switch(os) {
	case Windows:
	case MacOS:
	  if((pToolsets.get(tname) == null) || 
	     !pToolsets.get(tname).containsKey(OsType.Unix)) 
	    throw new PipelineException
	      ("The Unix toolset must be created before a " + os + " toolset can be " + 
	       "added for (" + tname + ")!");
	}

	/* lookup the packages */  
	ArrayList<PackageCommon> packages = new ArrayList<PackageCommon>();
	timer.aquire();
	synchronized(pToolsetPackages) {
	  timer.resume();
	  
	  for(String pname : req.getPackages()) {
	    VersionID vid = req.getVersions().get(pname);
	    if(vid == null) 
	      throw new PipelineException 
		("Unable to create the " + os + " toolset (" + tname + ") because " +
		 "no revision number for package (" + pname + ") was supplied!");

	    packages.add(getToolsetPackage(pname, vid, os));	   
	  }
	}
	
	/* build the toolset */ 
	Toolset tset = 
	  new Toolset(req.getAuthor(), tname, packages, req.getDescription(), os);
	if(tset.hasConflicts()) 
	  return new FailureRsp
	    (timer, 
	     "Unable to create the toolset (" + tname + ") due to conflicts " + 
	     "between the supplied packages!");
	
	writeToolset(tset, os);

	TreeMap<OsType,Toolset> toolsets = pToolsets.get(tname);
	if(toolsets == null) {
	  toolsets = new TreeMap<OsType,Toolset>();
	  pToolsets.put(tname, toolsets);
	}

	toolsets.put(os, tset);
	
	return new MiscCreateToolsetRsp(timer, tset);
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
  
  /**
   * Get the names and revision numbers of all OS specific toolset packages.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetToolsetPackageNamesRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the package names.
   */
  public Object
  getToolsetPackageNames
  (
   MiscGetToolsetPackageNamesReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pToolsetPackages) {
	timer.resume();
	
	OsType os = req.getOsType();

	TreeMap<String,TreeSet<VersionID>> names = 
	  new TreeMap<String,TreeSet<VersionID>>();

	for(String name : pToolsetPackages.keySet()) {
	  if(pToolsetPackages.containsKey(name, os)) {
	    TreeSet<VersionID> vids = new TreeSet<VersionID>();
	    vids.addAll(pToolsetPackages.keySet(name, os));
	    names.put(name, vids);
	  }
	}
	
	return new MiscGetToolsetPackageNamesRsp(timer, names);
      }  
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }      
  }

  /**
   * Get the names and revision numbers of all toolset packages for all operating systems.
   * 
   * @return
   *   <CODE>MiscGetToolsetPackageNamesRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the package names.
   */
  public Object
  getAllToolsetPackageNames()
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pToolsetPackages) {
	timer.resume();
	
	DoubleMap<String,OsType,TreeSet<VersionID>> names = 
	  new DoubleMap<String,OsType,TreeSet<VersionID>>();

	for(String name : pToolsetPackages.keySet()) {
	  for(OsType os : pToolsetPackages.keySet(name)) {
	    TreeSet<VersionID> vids = new TreeSet<VersionID>();
	    vids.addAll(pToolsetPackages.keySet(name, os));
	    names.put(name, os, vids);
	  }
	}

	return new MiscGetAllToolsetPackageNamesRsp(timer, names);
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
	
	String pname  = req.getName();
	VersionID vid = req.getVersionID();
	OsType os     = req.getOsType();

	return new MiscGetToolsetPackageRsp(timer, getToolsetPackage(pname, vid, os));
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
   * Get multiple OS specific toolset packages. 
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetToolsetPackagesRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to find the toolset packages.
   */
  public Object
  getToolsetPackages
  ( 
   MiscGetToolsetPackagesReq req
  )  
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pToolsetPackages) {
	timer.resume();

	TripleMap<String,VersionID,OsType,PackageVersion> packages = 
	  new TripleMap<String,VersionID,OsType,PackageVersion>();

	DoubleMap<String,VersionID,TreeSet<OsType>> index = req.getPackages();
	for(String pname : index.keySet()) {
	  for(VersionID vid : index.keySet(pname)) {
	    for(OsType os : index.get(pname, vid)) {
	      packages.put(pname, vid, os, getToolsetPackage(pname, vid, os));
	    }
	  }
	}

	return new MiscGetToolsetPackagesRsp(timer, packages);
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
   * Get the OS specific toolset package with the given name and revision number. 
   * 
   * @param name
   *   The toolset package name.
   * 
   * @param vid
   *   The revision number of the package.
   * 
   * @param os
   *   The operating system type.
   * 
   * @throws PipelineException
   *   If unable to find the toolset package.
   */ 
  private PackageVersion
  getToolsetPackage
  (
   String pname, 
   VersionID vid, 
   OsType os
  ) 
    throws PipelineException 
  {
    TreeMap<OsType,TreeMap<VersionID,PackageVersion>> packages = pToolsetPackages.get(pname);
    if(packages == null) 
      throw new PipelineException 
	("No toolset package named (" + pname + ") exists!");
    
    TreeMap<VersionID,PackageVersion> versions = packages.get(os);	
    if(versions == null) 
      throw new PipelineException 
	("No version (" + vid + ") of the toolset package (" + pname + ") exists!");
    
    if(!versions.containsKey(vid)) 
      throw new PipelineException
	("No toolset package named (" + pname + " v" + vid + ") for the (" + os + ") " + 
	 "operating system exists!");

    PackageVersion pkg = versions.get(vid);
    if(pkg == null) 
      pkg = readToolsetPackage(pname, vid, os);
    if(pkg == null)
      throw new IllegalStateException
	("Toolset package for (" + os + ") OS cannot be (null)!");
    
    return pkg;
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
      if(!pAdminPrivileges.isMasterAdmin(req)) 
	throw new PipelineException
	  ("Only a user with Master Admin privileges may create new toolset packages!");

      synchronized(pToolsetPackages) {
	timer.resume();

	String pname    = req.getPackage().getName();
	OsType os       = req.getOsType();
	PackageMod pmod = req.getPackage();

	if(pmod.isEmpty() && (os != OsType.Unix))
	  throw new PipelineException 
	    ("Unable to create the " + os + " toolset package (" + pname + ") " + 
	     "until at least one environmental variable has been defined!"); 	

	TreeMap<OsType,TreeMap<VersionID,PackageVersion>> packages = 
	  pToolsetPackages.get(pname);

	switch(os) {
	case Windows:
	case MacOS:
	  if((packages == null) || !packages.containsKey(OsType.Unix)) 
	    throw new PipelineException
	      ("The Unix toolset package must be created before a " + os + " package can " +
	       "be added for (" + pname + ")!");
	}

	TreeMap<VersionID,PackageVersion> versions = null;
	if(packages != null) 
	  versions = packages.get(os);

	VersionID nvid = new VersionID();
	if(versions != null) {
	  if(versions.isEmpty())
	    throw new IllegalStateException();
	  
	  if(req.getLevel() == null) 
	    throw new PipelineException 
	      ("Unable to create the " + os + " toolset package (" + pname + ") " + 
	       "due to a missing revision number increment level!");
	  
	  nvid = new VersionID(versions.lastKey(), req.getLevel());
	}
	
	PackageVersion pkg = 
	  new PackageVersion(req.getAuthor(), pmod, nvid, req.getDescription());
	
	writeToolsetPackage(pkg, os);

	pToolsetPackages.put(pname, os, pkg.getVersionID(), pkg);
	
	return new MiscCreateToolsetPackageRsp(timer, pkg);
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
  /*   T O O L S E T   P L U G I N S  /  M E N U   L A Y O U T S                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the layout plugin menus for all plugin types associated with a toolset.
   *
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetPluginMenuLayoutsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the menu layout.
   */ 
  public Object 
  getPluginMenuLayouts
  ( 
   MiscGetPluginMenuLayoutReq req 
  ) 
  {
    String name = req.getName();

    TaskTimer timer = 
      new TaskTimer("MasterMgr.getPluginMenuLayout(): " + name);

    if(name == null) 
      return new FailureRsp(timer, "The toolset name cannot be (null)!");

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();	

      TreeMap<PluginType,PluginMenuLayout> layouts = 
	new TreeMap<PluginType,PluginMenuLayout>();

      timer.aquire();
      synchronized(pEditorMenuLayouts) {
	timer.resume();	

	PluginMenuLayout layout = pEditorMenuLayouts.get(name);
	if(layout != null) 
	  layouts.put(PluginType.Editor, new PluginMenuLayout(layout));
	else 
	  layouts.put(PluginType.Editor, new PluginMenuLayout());
      }

      timer.aquire();
      synchronized(pComparatorMenuLayouts) {
	timer.resume();	

	PluginMenuLayout layout = pComparatorMenuLayouts.get(name);
	if(layout != null) 
	  layouts.put(PluginType.Comparator, new PluginMenuLayout(layout));
	else 
	  layouts.put(PluginType.Comparator, new PluginMenuLayout());
      }

      timer.aquire();
      synchronized(pActionMenuLayouts) {
	timer.resume();	

	PluginMenuLayout layout = pActionMenuLayouts.get(name);
	if(layout != null) 
	  layouts.put(PluginType.Action, new PluginMenuLayout(layout));
	else 
	  layouts.put(PluginType.Action, new PluginMenuLayout());
      }

      timer.aquire();
      synchronized(pToolMenuLayouts) {
	timer.resume();	

	PluginMenuLayout layout = pToolMenuLayouts.get(name);
	if(layout != null) 
	  layouts.put(PluginType.Tool, new PluginMenuLayout(layout));
	else 
	  layouts.put(PluginType.Tool, new PluginMenuLayout());
      }

      timer.aquire();
      synchronized(pArchiverMenuLayouts) {
	timer.resume();	

	PluginMenuLayout layout = pArchiverMenuLayouts.get(name);
	if(layout != null) 
	  layouts.put(PluginType.Archiver, new PluginMenuLayout(layout));
	else 
	  layouts.put(PluginType.Archiver, new PluginMenuLayout());
      }

      timer.aquire();
      synchronized(pMasterExtMenuLayouts) {
	timer.resume();	

	PluginMenuLayout layout = pMasterExtMenuLayouts.get(name);
	if(layout != null) 
	  layouts.put(PluginType.MasterExt, new PluginMenuLayout(layout));
	else 
	  layouts.put(PluginType.MasterExt, new PluginMenuLayout());
      }

      timer.aquire();
      synchronized(pQueueExtMenuLayouts) {
	timer.resume();	

	PluginMenuLayout layout = pQueueExtMenuLayouts.get(name);
	if(layout != null) 
	  layouts.put(PluginType.QueueExt, new PluginMenuLayout(layout));
	else 
	  layouts.put(PluginType.QueueExt, new PluginMenuLayout());
      }
      
      return new MiscGetPluginMenuLayoutsRsp(timer, layouts);
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }

  /**
   * Get all types of plugins associated with the given packages.
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetSelectPackagePluginsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the plugins.
   */ 
  public Object
  getSelectPackagePlugins
  (
   MiscGetSelectPackagePluginsReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();

      TreeMap<String,TreeSet<VersionID>> packages = req.getPackages(); 

      TripleMap<String,VersionID,PluginType,PluginSet> allPlugins = 
	new TripleMap<String,VersionID,PluginType,PluginSet>();
      
      timer.aquire();
      synchronized(pPackageEditorPlugins) {
	timer.resume();
	for(String pname : packages.keySet()) {
	  for(VersionID pvid : packages.get(pname)) {
	    PluginSet plugins = pPackageEditorPlugins.get(pname, pvid);
	    if(plugins == null)
	      plugins = new PluginSet(); 

	    allPlugins.put(pname, pvid, PluginType.Editor, plugins);
	  }
	}
      }
      
      timer.aquire();
      synchronized(pPackageComparatorPlugins) {
	timer.resume();
	for(String pname : packages.keySet()) {
	  for(VersionID pvid : packages.get(pname)) {
	    PluginSet plugins = pPackageComparatorPlugins.get(pname, pvid);
	    if(plugins == null)
	      plugins = new PluginSet(); 

	    allPlugins.put(pname, pvid, PluginType.Comparator, plugins);
	  }
	}
      }
      
      timer.aquire();
      synchronized(pPackageActionPlugins) {
	timer.resume();
	for(String pname : packages.keySet()) {
	  for(VersionID pvid : packages.get(pname)) {
	    PluginSet plugins = pPackageActionPlugins.get(pname, pvid);
	    if(plugins == null)
	      plugins = new PluginSet(); 

	    allPlugins.put(pname, pvid, PluginType.Action, plugins);
	  }
	}
      }
      
      timer.aquire();
      synchronized(pPackageToolPlugins) {
	timer.resume();
	for(String pname : packages.keySet()) {
	  for(VersionID pvid : packages.get(pname)) {
	    PluginSet plugins = pPackageToolPlugins.get(pname, pvid);
	    if(plugins == null)
	      plugins = new PluginSet(); 

	    allPlugins.put(pname, pvid, PluginType.Tool, plugins);
	  }
	}
      }
      
      timer.aquire();
      synchronized(pPackageArchiverPlugins) {
	timer.resume();
	for(String pname : packages.keySet()) {
	  for(VersionID pvid : packages.get(pname)) {
	    PluginSet plugins = pPackageArchiverPlugins.get(pname, pvid);
	    if(plugins == null)
	      plugins = new PluginSet(); 

	    allPlugins.put(pname, pvid, PluginType.Archiver, plugins);
	  }
	}
      }
      
      timer.aquire();
      synchronized(pPackageMasterExtPlugins) {
	timer.resume();
	for(String pname : packages.keySet()) {
	  for(VersionID pvid : packages.get(pname)) {
	    PluginSet plugins = pPackageMasterExtPlugins.get(pname, pvid);
	    if(plugins == null)
	      plugins = new PluginSet(); 

	    allPlugins.put(pname, pvid, PluginType.MasterExt, plugins);
	  }
	}
      }
      
      timer.aquire();
      synchronized(pPackageQueueExtPlugins) {
	timer.resume();
	for(String pname : packages.keySet()) {
	  for(VersionID pvid : packages.get(pname)) {
	    PluginSet plugins = pPackageQueueExtPlugins.get(pname, pvid);
	    if(plugins == null)
	      plugins = new PluginSet(); 

	    allPlugins.put(pname, pvid, PluginType.QueueExt, plugins);
	  }
	}
      }

      return new MiscGetSelectPackagePluginsRsp(timer, allPlugins); 
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the layout of the editor plugin menu associated with a toolset.
   *
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetPluginMenuLayoutRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the menu layout.
   */ 
  public Object 
  getEditorMenuLayout
  ( 
   MiscGetPluginMenuLayoutReq req 
  ) 
  {
    String name = req.getName();

    TaskTimer timer = 
      new TaskTimer("MasterMgr.getEditorMenuLayout(): " + name);

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pEditorMenuLayouts) {
	timer.resume();	

	PluginMenuLayout layout = null;
	if(name == null) 
	  layout = pDefaultEditorMenuLayout;
	else 
	  layout = pEditorMenuLayouts.get(name);

	if(layout != null) 
	  return new MiscGetPluginMenuLayoutRsp(timer, new PluginMenuLayout(layout));
	else 
	  return new MiscGetPluginMenuLayoutRsp(timer, new PluginMenuLayout());
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
    String name = req.getName();

    TaskTimer timer = 
      new TaskTimer("MasterMgr.setEditorMenuLayout(): " + name);
    
    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      if(!pAdminPrivileges.isDeveloper(req)) 
	throw new PipelineException
	  ("Only a user with Developer privileges may set the editor menu layout!");

      synchronized(pEditorMenuLayouts) {
	timer.resume();	

	PluginMenuLayout layout = req.getLayout();

	if(name == null) {
	  pDefaultEditorMenuLayout = layout;
	}
	else {
	  if(layout == null) 
	    pEditorMenuLayouts.remove(name);
	  else 
	    pEditorMenuLayouts.put(name, layout);
	}

	writePluginMenuLayout(name, "editor", 
			      pEditorMenuLayouts, pDefaultEditorMenuLayout);

	return new SuccessRsp(timer);
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
   * Get the editor plugins associated with all packages of a toolset.
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetPackagePluginsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the plugins.
   */ 
  public Object
  getToolsetEditorPlugins
  (
   MiscGetToolsetPluginsReq req 
  ) 
  {
    String tname = req.getName();

    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      TreeMap<String,TreeSet<VersionID>> packages = new TreeMap<String,TreeSet<VersionID>>();
      synchronized(pToolsets) {
	timer.resume();

	try {
	  Toolset toolset = getToolset(tname, OsType.Unix, timer);	
	  int wk;
	  for(wk=0; wk<toolset.getNumPackages(); wk++) {
	    String pname = toolset.getPackageName(wk);
	    VersionID pvid = toolset.getPackageVersionID(wk);
	    
	    TreeSet<VersionID> vids = packages.get(pname);
	    if(vids == null) {
	      vids = new TreeSet<VersionID>();
	      packages.put(pname, vids);
	    }
	    
	    vids.add(pvid);	    
	  }
	}
	catch(PipelineException ex) {
	}
      }

      PluginSet plugins = new PluginSet();
      {
	timer.aquire();
	synchronized(pPackageEditorPlugins) {
	  timer.resume();
	  
	  for(String pname : packages.keySet()) {
	    for(VersionID pvid : packages.get(pname)) {
	      PluginSet pset = pPackageEditorPlugins.get(pname, pvid);
	      if(pset != null) 
		plugins.addAll(pset);
	    }
	  }
	}
      }

      return new MiscGetPackagePluginsRsp(timer, plugins); 
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }

  /**
   * Get the editor plugins associated with a toolset package.
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetPackagePluginsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the plugins.
   */ 
  public Object
  getPackageEditorPlugins
  (
   MiscGetPackagePluginsReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pPackageEditorPlugins) {
	timer.resume();
	
	PluginSet plugins = pPackageEditorPlugins.get(req.getName(), req.getVersionID());
	if(plugins == null)
	  plugins = new PluginSet(); 

	return new MiscGetPackagePluginsRsp(timer, plugins); 
      }
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }

  /**
   * Set the editor plugins associated with a toolset package.
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the plugins.
   */ 
  public Object
  setPackageEditorPlugins
  (
   MiscSetPackagePluginsReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      if(!pAdminPrivileges.isDeveloper(req)) 
	throw new PipelineException
	  ("Only a user with Developer privileges may change the editor plugins " + 
	   "associated with a toolset package!"); 

      synchronized(pPackageEditorPlugins) {
	timer.resume();
	
	if(req.getPlugins() == null)
	  pPackageEditorPlugins.remove(req.getName(), req.getVersionID());
	else 
	  pPackageEditorPlugins.put(req.getName(), req.getVersionID(), req.getPlugins());

	writePackagePlugins(req.getName(), req.getVersionID(), 
			    "editor", pPackageEditorPlugins);

	return new SuccessRsp(timer);
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
 
  /**
   * Get the layout of the comparator plugin menu associated with a toolset.
   *
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetPluginMenuLayoutRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the menu layout.
   */ 
  public Object 
  getComparatorMenuLayout
  ( 
   MiscGetPluginMenuLayoutReq req 
  ) 
  {
    String name = req.getName();

    TaskTimer timer = 
      new TaskTimer("MasterMgr.getComparatorMenuLayout(): " + name);

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pComparatorMenuLayouts) {
	timer.resume();	

	PluginMenuLayout layout = null;
	if(name == null) 
	  layout = pDefaultComparatorMenuLayout;
	else 
	  layout = pComparatorMenuLayouts.get(name);

	if(layout != null) 
	  return new MiscGetPluginMenuLayoutRsp(timer, new PluginMenuLayout(layout));
	else 
	  return new MiscGetPluginMenuLayoutRsp(timer, new PluginMenuLayout());
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
    String name = req.getName();

    TaskTimer timer = 
      new TaskTimer("MasterMgr.setComparatorMenuLayout(): " + name);
    
    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      if(!pAdminPrivileges.isDeveloper(req)) 
	throw new PipelineException
	  ("Only a user with Developer privileges may set the comparator menu layout!");

      synchronized(pComparatorMenuLayouts) {
	timer.resume();	

	PluginMenuLayout layout = req.getLayout();

	if(name == null) {
	  pDefaultComparatorMenuLayout = layout;
	}
	else {
	  if(layout == null) 
	    pComparatorMenuLayouts.remove(name);
	  else 
	    pComparatorMenuLayouts.put(name, layout);
	}

	writePluginMenuLayout(name, "comparator", 
			      pComparatorMenuLayouts, pDefaultComparatorMenuLayout);

	return new SuccessRsp(timer);
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
   * Get the comparator plugins associated with all packages of a toolset.
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetPackagePluginsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the plugins.
   */ 
  public Object
  getToolsetComparatorPlugins
  (
   MiscGetToolsetPluginsReq req 
  ) 
  {
    String tname = req.getName();

    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      TreeMap<String,TreeSet<VersionID>> packages = new TreeMap<String,TreeSet<VersionID>>();
      synchronized(pToolsets) {
	timer.resume();

	try {
	  Toolset toolset = getToolset(tname, OsType.Unix, timer);	
	  int wk;
	  for(wk=0; wk<toolset.getNumPackages(); wk++) {
	    String pname = toolset.getPackageName(wk);
	    VersionID pvid = toolset.getPackageVersionID(wk);
	    
	    TreeSet<VersionID> vids = packages.get(pname);
	    if(vids == null) {
	      vids = new TreeSet<VersionID>();
	      packages.put(pname, vids);
	    }
	    
	    vids.add(pvid);	    
	  }
	}
	catch(PipelineException ex) {
	}
      }

      PluginSet plugins = new PluginSet();
      {
	timer.aquire();
	synchronized(pPackageComparatorPlugins) {
	  timer.resume();
	  
	  for(String pname : packages.keySet()) {
	    for(VersionID pvid : packages.get(pname)) {
	      PluginSet pset = pPackageComparatorPlugins.get(pname, pvid);
	      if(pset != null) 
		plugins.addAll(pset);
	    }
	  }
	}
      }

      return new MiscGetPackagePluginsRsp(timer, plugins); 
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }

  /**
   * Get the comparator plugins associated with a toolset package.
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetPackagePluginsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the plugins.
   */ 
  public Object
  getPackageComparatorPlugins
  (
   MiscGetPackagePluginsReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pPackageComparatorPlugins) {
	timer.resume();
	
	PluginSet plugins = pPackageComparatorPlugins.get(req.getName(), req.getVersionID());
	if(plugins == null)
	  plugins = new PluginSet(); 

	return new MiscGetPackagePluginsRsp(timer, plugins); 
      }
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }

  /**
   * Set the comparator plugins associated with a toolset package.
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the plugins.
   */ 
  public Object
  setPackageComparatorPlugins
  (
   MiscSetPackagePluginsReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      if(!pAdminPrivileges.isDeveloper(req)) 
	throw new PipelineException
	  ("Only a user with Developer privileges may change the comparator plugins " + 
	   "associated with a toolset package!"); 

      synchronized(pPackageComparatorPlugins) {
	timer.resume();
	
	if(req.getPlugins() == null)
	  pPackageComparatorPlugins.remove(req.getName(), req.getVersionID());
	else 
	  pPackageComparatorPlugins.put(req.getName(), req.getVersionID(), req.getPlugins());

	writePackagePlugins(req.getName(), req.getVersionID(), 
			    "comparator", pPackageComparatorPlugins);

	return new SuccessRsp(timer);
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
 
  /**
   * Get the layout of the action plugin menu associated with a toolset.
   *
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetPluginMenuLayoutRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the menu layout.
   */ 
  public Object 
  getActionMenuLayout
  ( 
   MiscGetPluginMenuLayoutReq req 
  ) 
  {
    String name = req.getName();

    TaskTimer timer = 
      new TaskTimer("MasterMgr.getActionMenuLayout(): " + name);

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pActionMenuLayouts) {
	timer.resume();	

	PluginMenuLayout layout = null;
	if(name == null) 
	  layout = pDefaultActionMenuLayout;
	else 
	  layout = pActionMenuLayouts.get(name);

	if(layout != null) 
	  return new MiscGetPluginMenuLayoutRsp(timer, new PluginMenuLayout(layout));
	else 
	  return new MiscGetPluginMenuLayoutRsp(timer, new PluginMenuLayout());
      }
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }

  /**
   * Set the layout of the action plugin selection menu.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to set the menu layout.
   */ 
  public Object 
  setActionMenuLayout
  ( 
   MiscSetPluginMenuLayoutReq req 
  ) 
  {
    String name = req.getName();

    TaskTimer timer = 
      new TaskTimer("MasterMgr.setActionMenuLayout(): " + name);
    
    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      if(!pAdminPrivileges.isDeveloper(req)) 
	throw new PipelineException
	  ("Only a user with Developer privileges may set the action menu layout!");

      synchronized(pActionMenuLayouts) {
	timer.resume();	

	PluginMenuLayout layout = req.getLayout();

	if(name == null) {
	  pDefaultActionMenuLayout = layout;
	}
	else {
	  if(layout == null) 
	    pActionMenuLayouts.remove(name);
	  else 
	    pActionMenuLayouts.put(name, layout);
	}

	writePluginMenuLayout(name, "action", 
			      pActionMenuLayouts, pDefaultActionMenuLayout);

	return new SuccessRsp(timer);
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
   * Get the action plugins associated with all packages of a toolset.
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetPackagePluginsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the plugins.
   */ 
  public Object
  getToolsetActionPlugins
  (
   MiscGetToolsetPluginsReq req 
  ) 
  {
    String tname = req.getName();

    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      TreeMap<String,TreeSet<VersionID>> packages = new TreeMap<String,TreeSet<VersionID>>();
      synchronized(pToolsets) {
	timer.resume();

	try {
	  Toolset toolset = getToolset(tname, OsType.Unix, timer);	
	  int wk;
	  for(wk=0; wk<toolset.getNumPackages(); wk++) {
	    String pname = toolset.getPackageName(wk);
	    VersionID pvid = toolset.getPackageVersionID(wk);
	    
	    TreeSet<VersionID> vids = packages.get(pname);
	    if(vids == null) {
	      vids = new TreeSet<VersionID>();
	      packages.put(pname, vids);
	    }
	    
	    vids.add(pvid);	    
	  }
	}
	catch(PipelineException ex) {
	}
      }

      PluginSet plugins = new PluginSet();
      {
	timer.aquire();
	synchronized(pPackageActionPlugins) {
	  timer.resume();
	  
	  for(String pname : packages.keySet()) {
	    for(VersionID pvid : packages.get(pname)) {
	      PluginSet pset = pPackageActionPlugins.get(pname, pvid);
	      if(pset != null) 
		plugins.addAll(pset);
	    }
	  }
	}
      }

      return new MiscGetPackagePluginsRsp(timer, plugins); 
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }

  /**
   * Get the action plugins associated with a toolset package.
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetPackagePluginsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the plugins.
   */ 
  public Object
  getPackageActionPlugins
  (
   MiscGetPackagePluginsReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pPackageActionPlugins) {
	timer.resume();
	
	PluginSet plugins = pPackageActionPlugins.get(req.getName(), req.getVersionID());
	if(plugins == null)
	  plugins = new PluginSet(); 

	return new MiscGetPackagePluginsRsp(timer, plugins); 
      }
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }

  /**
   * Set the action plugins associated with a toolset package.
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the plugins.
   */ 
  public Object
  setPackageActionPlugins
  (
   MiscSetPackagePluginsReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      if(!pAdminPrivileges.isDeveloper(req)) 
	throw new PipelineException
	  ("Only a user with Developer privileges may change the action plugins " + 
	   "associated with a toolset package!"); 

      synchronized(pPackageActionPlugins) {
	timer.resume();
	
	if(req.getPlugins() == null)
	  pPackageActionPlugins.remove(req.getName(), req.getVersionID());
	else 
	  pPackageActionPlugins.put(req.getName(), req.getVersionID(), req.getPlugins());

	writePackagePlugins(req.getName(), req.getVersionID(), 
			    "action", pPackageActionPlugins);

	return new SuccessRsp(timer);
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
 
  /**
   * Get the layout of the tool plugin menu associated with a toolset.
   *
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetPluginMenuLayoutRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the menu layout.
   */ 
  public Object 
  getToolMenuLayout
  ( 
   MiscGetPluginMenuLayoutReq req 
  ) 
  {
    String name = req.getName();

    TaskTimer timer = 
      new TaskTimer("MasterMgr.getToolMenuLayout(): " + name);

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pToolMenuLayouts) {
	timer.resume();	

	PluginMenuLayout layout = null;
	if(name == null) 
	  layout = pDefaultToolMenuLayout;
	else 
	  layout = pToolMenuLayouts.get(name);

	if(layout != null) 
	  return new MiscGetPluginMenuLayoutRsp(timer, new PluginMenuLayout(layout));
	else 
	  return new MiscGetPluginMenuLayoutRsp(timer, new PluginMenuLayout());
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
    String name = req.getName();

    TaskTimer timer = 
      new TaskTimer("MasterMgr.setToolMenuLayout(): " + name);
    
    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      if(!pAdminPrivileges.isDeveloper(req)) 
	throw new PipelineException
	  ("Only a user with Developer privileges may set the tool menu layout!");

      synchronized(pToolMenuLayouts) {
	timer.resume();	

	PluginMenuLayout layout = req.getLayout();

	if(name == null) {
	  pDefaultToolMenuLayout = layout;
	}
	else {
	  if(layout == null) 
	    pToolMenuLayouts.remove(name);
	  else 
	    pToolMenuLayouts.put(name, layout);
	}

	writePluginMenuLayout(name, "tool", 
			      pToolMenuLayouts, pDefaultToolMenuLayout);

	return new SuccessRsp(timer);
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
   * Get the tool plugins associated with all packages of a toolset.
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetPackagePluginsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the plugins.
   */ 
  public Object
  getToolsetToolPlugins
  (
   MiscGetToolsetPluginsReq req 
  ) 
  {
    String tname = req.getName();

    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      TreeMap<String,TreeSet<VersionID>> packages = new TreeMap<String,TreeSet<VersionID>>();
      synchronized(pToolsets) {
	timer.resume();

	try {
	  Toolset toolset = getToolset(tname, OsType.Unix, timer);	
	  int wk;
	  for(wk=0; wk<toolset.getNumPackages(); wk++) {
	    String pname = toolset.getPackageName(wk);
	    VersionID pvid = toolset.getPackageVersionID(wk);
	    
	    TreeSet<VersionID> vids = packages.get(pname);
	    if(vids == null) {
	      vids = new TreeSet<VersionID>();
	      packages.put(pname, vids);
	    }
	    
	    vids.add(pvid);	    
	  }
	}
	catch(PipelineException ex) {
	}
      }

      PluginSet plugins = new PluginSet();
      {
	timer.aquire();
	synchronized(pPackageToolPlugins) {
	  timer.resume();
	  
	  for(String pname : packages.keySet()) {
	    for(VersionID pvid : packages.get(pname)) {
	      PluginSet pset = pPackageToolPlugins.get(pname, pvid);
	      if(pset != null) 
		plugins.addAll(pset);
	    }
	  }
	}
      }

      return new MiscGetPackagePluginsRsp(timer, plugins); 
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }

  /**
   * Get the tool plugins associated with a toolset package.
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetPackagePluginsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the plugins.
   */ 
  public Object
  getPackageToolPlugins
  (
   MiscGetPackagePluginsReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pPackageToolPlugins) {
	timer.resume();
	
	PluginSet plugins = pPackageToolPlugins.get(req.getName(), req.getVersionID());
	if(plugins == null)
	  plugins = new PluginSet(); 

	return new MiscGetPackagePluginsRsp(timer, plugins); 
      }
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }

  /**
   * Set the tool plugins associated with a toolset package.
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the plugins.
   */ 
  public Object
  setPackageToolPlugins
  (
   MiscSetPackagePluginsReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      if(!pAdminPrivileges.isDeveloper(req)) 
	throw new PipelineException
	  ("Only a user with Developer privileges may change the tool plugins " + 
	   "associated with a toolset package!"); 

      synchronized(pPackageToolPlugins) {
	timer.resume();
	
	if(req.getPlugins() == null)
	  pPackageToolPlugins.remove(req.getName(), req.getVersionID());
	else 
	  pPackageToolPlugins.put(req.getName(), req.getVersionID(), req.getPlugins());

	writePackagePlugins(req.getName(), req.getVersionID(), 
			    "tool", pPackageToolPlugins);

	return new SuccessRsp(timer);
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
 
  /**
   * Get the layout of the archiver plugin menu associated with a toolset.
   *
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetPluginMenuLayoutRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the menu layout.
   */ 
  public Object 
  getArchiverMenuLayout
  ( 
   MiscGetPluginMenuLayoutReq req 
  ) 
  {
    String name = req.getName();

    TaskTimer timer = 
      new TaskTimer("MasterMgr.getArchiverMenuLayout(): " + name);

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pArchiverMenuLayouts) {
	timer.resume();	

	PluginMenuLayout layout = null;
	if(name == null) 
	  layout = pDefaultArchiverMenuLayout;
	else 
	  layout = pArchiverMenuLayouts.get(name);

	if(layout != null) 
	  return new MiscGetPluginMenuLayoutRsp(timer, new PluginMenuLayout(layout));
	else 
	  return new MiscGetPluginMenuLayoutRsp(timer, new PluginMenuLayout());
      }
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }

  /**
   * Set the layout of the archiver plugin selection menu.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to set the menu layout.
   */ 
  public Object 
  setArchiverMenuLayout
  ( 
   MiscSetPluginMenuLayoutReq req 
  ) 
  {
    String name = req.getName();

    TaskTimer timer = 
      new TaskTimer("MasterMgr.setArchiverMenuLayout(): " + name);
    
    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      if(!pAdminPrivileges.isDeveloper(req)) 
	throw new PipelineException
	  ("Only a user with Developer privileges may set the archiver menu layout!");

      synchronized(pArchiverMenuLayouts) {
	timer.resume();	

	PluginMenuLayout layout = req.getLayout();

	if(name == null) {
	  pDefaultArchiverMenuLayout = layout;
	}
	else {
	  if(layout == null) 
	    pArchiverMenuLayouts.remove(name);
	  else 
	    pArchiverMenuLayouts.put(name, layout);
	}

	writePluginMenuLayout(name, "archiver", 
			      pArchiverMenuLayouts, pDefaultArchiverMenuLayout);

	return new SuccessRsp(timer);
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
   * Get the archiver plugins associated with all packages of a toolset.
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetPackagePluginsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the plugins.
   */ 
  public Object
  getToolsetArchiverPlugins
  (
   MiscGetToolsetPluginsReq req 
  ) 
  {
    String tname = req.getName();

    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      TreeMap<String,TreeSet<VersionID>> packages = new TreeMap<String,TreeSet<VersionID>>();
      synchronized(pToolsets) {
	timer.resume();

	try {
	  Toolset toolset = getToolset(tname, OsType.Unix, timer);	
	  int wk;
	  for(wk=0; wk<toolset.getNumPackages(); wk++) {
	    String pname = toolset.getPackageName(wk);
	    VersionID pvid = toolset.getPackageVersionID(wk);
	    
	    TreeSet<VersionID> vids = packages.get(pname);
	    if(vids == null) {
	      vids = new TreeSet<VersionID>();
	      packages.put(pname, vids);
	    }
	    
	    vids.add(pvid);	    
	  }
	}
	catch(PipelineException ex) {
	}
      }

      PluginSet plugins = new PluginSet();
      {
	timer.aquire();
	synchronized(pPackageArchiverPlugins) {
	  timer.resume();
	  
	  for(String pname : packages.keySet()) {
	    for(VersionID pvid : packages.get(pname)) {
	      PluginSet pset = pPackageArchiverPlugins.get(pname, pvid);
	      if(pset != null) 
		plugins.addAll(pset);
	    }
	  }
	}
      }

      return new MiscGetPackagePluginsRsp(timer, plugins); 
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }

  /**
   * Get the archiver plugins associated with a toolset package.
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetPackagePluginsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the plugins.
   */ 
  public Object
  getPackageArchiverPlugins
  (
   MiscGetPackagePluginsReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pPackageArchiverPlugins) {
	timer.resume();
	
	PluginSet plugins = pPackageArchiverPlugins.get(req.getName(), req.getVersionID());
	if(plugins == null)
	  plugins = new PluginSet(); 

	return new MiscGetPackagePluginsRsp(timer, plugins); 
      }
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }

  /**
   * Set the archiver plugins associated with a toolset package.
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the plugins.
   */ 
  public Object
  setPackageArchiverPlugins
  (
   MiscSetPackagePluginsReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      if(!pAdminPrivileges.isDeveloper(req)) 
	throw new PipelineException
	  ("Only a user with Developer privileges may change the archiver plugins " + 
	   "associated with a toolset package!"); 

      synchronized(pPackageArchiverPlugins) {
	timer.resume();
	
	if(req.getPlugins() == null)
	  pPackageArchiverPlugins.remove(req.getName(), req.getVersionID());
	else 
	  pPackageArchiverPlugins.put(req.getName(), req.getVersionID(), req.getPlugins());

	writePackagePlugins(req.getName(), req.getVersionID(), 
			    "archiver", pPackageArchiverPlugins);

	return new SuccessRsp(timer);
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
 
  /**
   * Get the layout of the master extension plugin menu associated with a toolset.
   *
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetPluginMenuLayoutRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the menu layout.
   */ 
  public Object 
  getMasterExtMenuLayout
  ( 
   MiscGetPluginMenuLayoutReq req 
  ) 
  {
    String name = req.getName();

    TaskTimer timer = 
      new TaskTimer("MasterMgr.getMasterExtMenuLayout(): " + name);

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pMasterExtMenuLayouts) {
	timer.resume();	

	PluginMenuLayout layout = null;
	if(name == null) 
	  layout = pDefaultMasterExtMenuLayout;
	else 
	  layout = pMasterExtMenuLayouts.get(name);

	if(layout != null) 
	  return new MiscGetPluginMenuLayoutRsp(timer, new PluginMenuLayout(layout));
	else 
	  return new MiscGetPluginMenuLayoutRsp(timer, new PluginMenuLayout());
      }
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }

  /**
   * Set the layout of the master extension plugin selection menu.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to set the menu layout.
   */ 
  public Object 
  setMasterExtMenuLayout
  ( 
   MiscSetPluginMenuLayoutReq req 
  ) 
  {
    String name = req.getName();

    TaskTimer timer = 
      new TaskTimer("MasterMgr.setMasterExtMenuLayout(): " + name);
    
    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      if(!pAdminPrivileges.isDeveloper(req)) 
	throw new PipelineException
	  ("Only a user with Developer privileges may set the master extension menu layout!");

      synchronized(pMasterExtMenuLayouts) {
	timer.resume();	

	PluginMenuLayout layout = req.getLayout();

	if(name == null) {
	  pDefaultMasterExtMenuLayout = layout;
	}
	else {
	  if(layout == null) 
	    pMasterExtMenuLayouts.remove(name);
	  else 
	    pMasterExtMenuLayouts.put(name, layout);
	}

	writePluginMenuLayout(name, "master extension", 
			      pMasterExtMenuLayouts, pDefaultMasterExtMenuLayout);

	return new SuccessRsp(timer);
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
   * Get the master extension plugins associated with all packages of a toolset.
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetPackagePluginsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the plugins.
   */ 
  public Object
  getToolsetMasterExtPlugins
  (
   MiscGetToolsetPluginsReq req 
  ) 
  {
    String tname = req.getName();

    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      TreeMap<String,TreeSet<VersionID>> packages = new TreeMap<String,TreeSet<VersionID>>();
      synchronized(pToolsets) {
	timer.resume();

	try {
	  Toolset toolset = getToolset(tname, OsType.Unix, timer);	
	  int wk;
	  for(wk=0; wk<toolset.getNumPackages(); wk++) {
	    String pname = toolset.getPackageName(wk);
	    VersionID pvid = toolset.getPackageVersionID(wk);
	    
	    TreeSet<VersionID> vids = packages.get(pname);
	    if(vids == null) {
	      vids = new TreeSet<VersionID>();
	      packages.put(pname, vids);
	    }
	    
	    vids.add(pvid);	    
	  }
	}
	catch(PipelineException ex) {
	}
      }

      PluginSet plugins = new PluginSet();
      {
	timer.aquire();
	synchronized(pPackageMasterExtPlugins) {
	  timer.resume();
	  
	  for(String pname : packages.keySet()) {
	    for(VersionID pvid : packages.get(pname)) {
	      PluginSet pset = pPackageMasterExtPlugins.get(pname, pvid);
	      if(pset != null) 
		plugins.addAll(pset);
	    }
	  }
	}
      }

      return new MiscGetPackagePluginsRsp(timer, plugins); 
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }

  /**
   * Get the master extension plugins associated with a toolset package.
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetPackagePluginsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the plugins.
   */ 
  public Object
  getPackageMasterExtPlugins
  (
   MiscGetPackagePluginsReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pPackageMasterExtPlugins) {
	timer.resume();
	
	PluginSet plugins = pPackageMasterExtPlugins.get(req.getName(), req.getVersionID());
	if(plugins == null)
	  plugins = new PluginSet(); 

	return new MiscGetPackagePluginsRsp(timer, plugins); 
      }
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }

  /**
   * Set the master extension plugins associated with a toolset package.
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the plugins.
   */ 
  public Object
  setPackageMasterExtPlugins
  (
   MiscSetPackagePluginsReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      if(!pAdminPrivileges.isDeveloper(req)) 
	throw new PipelineException
	  ("Only a user with Developer privileges may change the master extension plugins " + 
	   "associated with a toolset package!"); 

      synchronized(pPackageMasterExtPlugins) {
	timer.resume();
	
	if(req.getPlugins() == null)
	  pPackageMasterExtPlugins.remove(req.getName(), req.getVersionID());
	else 
	  pPackageMasterExtPlugins.put(req.getName(), req.getVersionID(), req.getPlugins());

	writePackagePlugins(req.getName(), req.getVersionID(), 
			    "master extension", pPackageMasterExtPlugins);

	return new SuccessRsp(timer);
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
 
  /**
   * Get the layout of the queue extension plugin menu associated with a toolset.
   *
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetPluginMenuLayoutRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the menu layout.
   */ 
  public Object 
  getQueueExtMenuLayout
  ( 
   MiscGetPluginMenuLayoutReq req 
  ) 
  {
    String name = req.getName();

    TaskTimer timer = 
      new TaskTimer("MasterMgr.getQueueExtMenuLayout(): " + name);

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pQueueExtMenuLayouts) {
	timer.resume();	

	PluginMenuLayout layout = null;
	if(name == null) 
	  layout = pDefaultQueueExtMenuLayout;
	else 
	  layout = pQueueExtMenuLayouts.get(name);

	if(layout != null) 
	  return new MiscGetPluginMenuLayoutRsp(timer, new PluginMenuLayout(layout));
	else 
	  return new MiscGetPluginMenuLayoutRsp(timer, new PluginMenuLayout());
      }
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }

  /**
   * Set the layout of the queue extension plugin selection menu.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to set the menu layout.
   */ 
  public Object 
  setQueueExtMenuLayout
  ( 
   MiscSetPluginMenuLayoutReq req 
  ) 
  {
    String name = req.getName();

    TaskTimer timer = 
      new TaskTimer("MasterMgr.setQueueExtMenuLayout(): " + name);
    
    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      if(!pAdminPrivileges.isDeveloper(req)) 
	throw new PipelineException
	  ("Only a user with Developer privileges may set the queue extension menu layout!");

      synchronized(pQueueExtMenuLayouts) {
	timer.resume();	

	PluginMenuLayout layout = req.getLayout();

	if(name == null) {
	  pDefaultQueueExtMenuLayout = layout;
	}
	else {
	  if(layout == null) 
	    pQueueExtMenuLayouts.remove(name);
	  else 
	    pQueueExtMenuLayouts.put(name, layout);
	}

	writePluginMenuLayout(name, "queue extension", 
			      pQueueExtMenuLayouts, pDefaultQueueExtMenuLayout);

	return new SuccessRsp(timer);
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
   * Get the queue extension plugins associated with all packages of a toolset.
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetPackagePluginsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the plugins.
   */ 
  public Object
  getToolsetQueueExtPlugins
  (
   MiscGetToolsetPluginsReq req 
  ) 
  {
    String tname = req.getName();

    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      TreeMap<String,TreeSet<VersionID>> packages = new TreeMap<String,TreeSet<VersionID>>();
      synchronized(pToolsets) {
	timer.resume();

	try {
	  Toolset toolset = getToolset(tname, OsType.Unix, timer);	
	  int wk;
	  for(wk=0; wk<toolset.getNumPackages(); wk++) {
	    String pname = toolset.getPackageName(wk);
	    VersionID pvid = toolset.getPackageVersionID(wk);
	    
	    TreeSet<VersionID> vids = packages.get(pname);
	    if(vids == null) {
	      vids = new TreeSet<VersionID>();
	      packages.put(pname, vids);
	    }
	    
	    vids.add(pvid);	    
	  }
	}
	catch(PipelineException ex) {
	}
      }

      PluginSet plugins = new PluginSet();
      {
	timer.aquire();
	synchronized(pPackageQueueExtPlugins) {
	  timer.resume();
	  
	  for(String pname : packages.keySet()) {
	    for(VersionID pvid : packages.get(pname)) {
	      PluginSet pset = pPackageQueueExtPlugins.get(pname, pvid);
	      if(pset != null) 
		plugins.addAll(pset);
	    }
	  }
	}
      }

      return new MiscGetPackagePluginsRsp(timer, plugins); 
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }

  /**
   * Get the queue extension plugins associated with a toolset package.
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetPackagePluginsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the plugins.
   */ 
  public Object
  getPackageQueueExtPlugins
  (
   MiscGetPackagePluginsReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pPackageQueueExtPlugins) {
	timer.resume();
	
	PluginSet plugins = pPackageQueueExtPlugins.get(req.getName(), req.getVersionID());
	if(plugins == null)
	  plugins = new PluginSet(); 

	return new MiscGetPackagePluginsRsp(timer, plugins); 
      }
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }

  /**
   * Set the queue extension plugins associated with a toolset package.
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the plugins.
   */ 
  public Object
  setPackageQueueExtPlugins
  (
   MiscSetPackagePluginsReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      if(!pAdminPrivileges.isDeveloper(req)) 
	throw new PipelineException
	  ("Only a user with Developer privileges may change the queue extension plugins " + 
	   "associated with a toolset package!"); 

      synchronized(pPackageQueueExtPlugins) {
	timer.resume();
	
	if(req.getPlugins() == null)
	  pPackageQueueExtPlugins.remove(req.getName(), req.getVersionID());
	else 
	  pPackageQueueExtPlugins.put(req.getName(), req.getVersionID(), req.getPlugins());

	writePackagePlugins(req.getName(), req.getVersionID(), 
			    "queue extension", pPackageQueueExtPlugins);

	return new SuccessRsp(timer);
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
  /*   S E R V E R   E X T E N S I O N S                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current master extension configurations. <P> 
   * 
   * @return
   *   <CODE>MiscGetMasterExtensionsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the extensions.
   */ 
  public Object
  getMasterExtensions() 
  {
    TaskTimer timer = new TaskTimer();
    
    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pMasterExtensions) {
	timer.resume();
	
	return new MiscGetMasterExtensionsRsp(timer, pMasterExtensions);
      }
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }
  
  /**
   * Remove an existing the master extension configuration. <P> 
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to remove the extension.
   */ 
  public Object
  removeMasterExtension
  (
   MiscRemoveMasterExtensionReq req
  ) 
  {
    String name = req.getExtensionName();

    TaskTimer timer = new TaskTimer("MasterMgr.removeMasterExtension(): " + name); 
    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      if(!pAdminPrivileges.isMasterAdmin(req))
	throw new PipelineException
	  ("Only a user with Master Admin privileges may remove a " + 
	   "master extension configuration!");

      synchronized(pMasterExtensions) {
	timer.resume();
	
	doPreExtensionDisableTask(pMasterExtensions.get(name));

	pMasterExtensions.remove(name);
	writeMasterExtensions();

	return new SuccessRsp(timer);
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
   * Add or modify an existing the master extension configuration. <P> 
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to set the extension.
   */ 
  public Object
  setMasterExtension
  (
   MiscSetMasterExtensionReq req
  ) 
  {
    MasterExtensionConfig config = req.getExtension();
    String name = config.getName();

    TaskTimer timer = new TaskTimer("MasterMgr.setMasterExtension(): " + name); 
    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      if(!pAdminPrivileges.isMasterAdmin(req))
	throw new PipelineException
	  ("Only a user with Master Admin privileges may add or modify " + 
	   "master extension configuration!");

      synchronized(pMasterExtensions) {
	timer.resume();
	
	doPreExtensionDisableTask(pMasterExtensions.get(name));

	pMasterExtensions.put(name, config); 
	writeMasterExtensions();

	doPostExtensionEnableTask(config); 

	return new SuccessRsp(timer);
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
   * Get new instances of all enabled master extension plugins indexed by 
   * extension configuration name. <P> 
   * 
   * This method also will pre-cook the toolset environments for all plugins which will be
   * spawning subprocesses.
   * 
   * @param timer
   *   The task timer.
   */ 
  private TreeMap<String,BaseMasterExt> 
  getMasterExts
  (
   TaskTimer timer
  ) 
    throws PipelineException
  {
    TreeMap<String,BaseMasterExt> table = new TreeMap<String,BaseMasterExt>();
    TreeMap<String,String> toolsetNames = new TreeMap<String,String>();

    /* instantiate the plugins */ 
    timer.aquire();
    synchronized(pMasterExtensions) {
      timer.resume();
	
      for(String cname : pMasterExtensions.keySet()) {
	MasterExtensionConfig config = pMasterExtensions.get(cname);
	if(config.isEnabled()) {
	  table.put(cname, config.getMasterExt());
	  toolsetNames.put(cname, config.getToolset());
	}
      }
    }

    /* cook the toolset environments (if needed by plugins) */ 
    for(String cname : table.keySet()) {
      BaseMasterExt ext = table.get(cname); 
      if(ext.needsEnvironment()) {
	String tname = toolsetNames.get(cname);
	ext.setEnvironment(getToolsetEnvironment(null, null, tname, OsType.Unix, timer));
      }
    }

    return table;
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Run the post-enable task (in the current thread) for the given master extension.
   */ 
  private void 
  doPostExtensionEnableTask
  ( 
   MasterExtensionConfig config
  ) 
  {
    if((config != null) && config.isEnabled()) {
      try {
	BaseMasterExt ext = config.getMasterExt();

	LogMgr.getInstance().log
	  (LogMgr.Kind.Ext, LogMgr.Level.Info,
	   "Enabling Server Extension: " + config.getName() + "\n" + 
	   "  Extension Plugin (" + ext.getName() + " v" + ext.getVersionID() + ") " + 
	   "from Vendor (" + ext.getVendor() + ")");

	if(ext.hasPostEnableTask()) 
	  ext.postEnableTask(); 
      }
      catch(PipelineException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ext, LogMgr.Level.Severe,
	   ex.getMessage()); 
      }
    }
  }

  /**
   * Run the pre-disable task (in the current thread) for the given master extension.
   */ 
  private void 
  doPreExtensionDisableTask
  ( 
   MasterExtensionConfig config
  ) 
  {
    if((config != null) && config.isEnabled()) {
      try {
	BaseMasterExt ext = config.getMasterExt();

	LogMgr.getInstance().log
	  (LogMgr.Kind.Ext, LogMgr.Level.Info,
	   "Disabling Server Extension: " + config.getName() + "\n" + 
	   "  Extension Plugin (" + ext.getName() + " v" + ext.getVersionID() + ") " + 
	   "from Vendor (" + ext.getVendor() + ")");

	if(ext.hasPreDisableTask()) 
	  ext.preDisableTask(); 
      }
      catch(PipelineException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ext, LogMgr.Level.Severe,
	   ex.getMessage()); 
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether there are any enabled extensions which support the given test.
   * 
   * @param timer
   *   The parent task timer. 
   * 
   * @param factory
   *   The master extension test factory.
   */
  public boolean
  hasAnyExtensionTests
  (
   TaskTimer timer, 
   MasterTestFactory factory
  ) 
  {
    timer.aquire(); 
    synchronized(pMasterExtensions) {
      timer.resume();

      for(MasterExtensionConfig config : pMasterExtensions.values()) {
	if(config.isEnabled()) {
	  try {
	    BaseMasterExt ext = config.getMasterExt();
	    if(factory.hasTest(ext)) 
	      return true;
	  }
	  catch(PipelineException ex) {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Ext, LogMgr.Level.Severe,
	       ex.getMessage()); 
	  }
	}
      }
    }

    return false;
  }
  
  /**
   * Create and start threads for all enabled extensions which support the given task.
   * 
   * @param timer
   *   The parent task timer. 
   * 
   * @param factory
   *   The master extension test factory.
   * 
   * @throws PipelineException 
   *   If any of the enabled extension tests fail.
   */
  public void 
  performExtensionTests
  (
   TaskTimer timer, 
   MasterTestFactory factory
  ) 
    throws PipelineException
  {
    timer.aquire(); 
    synchronized(pMasterExtensions) {
      timer.resume();

      for(MasterExtensionConfig config : pMasterExtensions.values()) {
	if(config.isEnabled()) {
	  BaseMasterExt ext = null;
	  try {
	    ext = config.getMasterExt();
	  }
	  catch(PipelineException ex) {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Ext, LogMgr.Level.Severe,
	       ex.getMessage()); 
	  }

	  if((ext != null) && factory.hasTest(ext)) 
	    factory.performTest(ext); 
	}
      }
    }
  }

  /**
   * Whether there are any enabled extensions which support the given task.
   * 
   * @param timer
   *   The parent task timer. 
   * 
   * @param factory
   *   The master extension task factory.
   */
  public boolean
  hasAnyExtensionTasks
  (
   TaskTimer timer, 
   MasterTaskFactory factory
  ) 
  {
    timer.aquire(); 
    synchronized(pMasterExtensions) {
      timer.resume();

      for(MasterExtensionConfig config : pMasterExtensions.values()) {
	if(config.isEnabled()) {
	  try {
	    BaseMasterExt ext = config.getMasterExt();
	    if(factory.hasTask(ext)) 
	      return true;
	  }
	  catch(PipelineException ex) {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Ext, LogMgr.Level.Severe,
	       ex.getMessage()); 
	  }
	}
      }
    }

    return false;
  }
  
  /**
   * Create and start threads for all enabled extensions which support the given task.
   * 
   * @param timer
   *   The parent task timer. 
   * 
   * @param factory
   *   The master extension task factory.
   */
  public void 
  startExtensionTasks
  (
   TaskTimer timer, 
   MasterTaskFactory factory
  ) 
  {
    timer.aquire(); 
    synchronized(pMasterExtensions) {
      timer.resume();

      for(MasterExtensionConfig config : pMasterExtensions.values()) {
	if(config.isEnabled()) {
	  try {
	    BaseMasterExt ext = config.getMasterExt();
	    if(factory.hasTask(ext)) 
	      factory.startTask(config, ext); 
	  }
	  catch(PipelineException ex) {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Ext, LogMgr.Level.Severe,
	       ex.getMessage()); 
	  }
	}
      }
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
	  
	  BaseEditor editor = null;
	  SuffixEditor se = editors.get(req.getSuffix());
	  if(se != null) 
	    editor = se.getEditor();
	  
	  return new MiscGetEditorForSuffixRsp(timer, editor); 
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
   * Get the filename suffix to default editor mappings for the given user are 
   * already loaded and cached.
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
      if(editors == null)
	throw new IllegalStateException("Editors cannot be (null)!"); 
      
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
  /*   W O R K I N G   A R E A S                                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the table of the working areas containing the given node. <P> 
   * 
   * @return
   *   <CODE>NodeUpdatePathRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the working areas.
   */
  public Object
  getWorkingAreasContaining
  (
   NodeGetWorkingAreasContainingReq req
  )
  {
    TaskTimer timer = new TaskTimer();
    
    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();	
	
      return new NodeGetWorkingAreasRsp(timer, pNodeTree.getViewsContaining(req.getName()));
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
    String author = req.getAuthor();
    String view   = req.getView();

    TaskTimer timer = new TaskTimer("MasterMgr.createWorkingArea(): " + author + "|" + view);
    
    /* pre-op tests */
    CreateWorkingAreaExtFactory factory = new CreateWorkingAreaExtFactory(author, view); 
    try {
      performExtensionTests(timer, factory);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();	

      if(!pAdminPrivileges.isNodeManaged(req, author)) 
	throw new PipelineException
	  ("Only a user with Node Manager privileges may create working areas owned " +
	   "by another user!");

      timer.aquire();
      synchronized(pWorkingAreaViews) {
	timer.resume();	

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
		(timer, "Unable to create the working area (" + view + ") for user " + 
		 "(" + author + ")!");
	  }
	}

	/* create the working area files directory */ 
	FileMgrClient fclient = getFileMgrClient();
	try {
	  fclient.createWorkingArea(author, view);
	}
	finally {
	  freeFileMgrClient(fclient);
	}
	
	/* add the view to the runtime table */ 
	if(views == null) {
	  views = new TreeSet<String>();
	  pWorkingAreaViews.put(author, views);
	}
	views.add(view);
      }
		
      /* post-op tasks */ 
      startExtensionTasks(timer, factory);
      
      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
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
    String author = req.getAuthor();
    String view   = req.getView();

    TaskTimer timer =  new TaskTimer("MasterMgr.removeWorkingArea(): " + author + "|" + view);

    /* pre-op tests */
    RemoveWorkingAreaExtFactory factory = new RemoveWorkingAreaExtFactory(author, view); 
    try {
      performExtensionTests(timer, factory);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }

    timer.aquire();
    pDatabaseLock.writeLock().lock();
    try {
      timer.resume();	
	
      if(!pAdminPrivileges.isNodeManaged(req, author)) 
	throw new PipelineException
	  ("Only a user with Node Manager privileges may remove working areas owned " +
	   "by another user!");

      /* make sure no working versions exist in the view */ 
      {
	TreeSet<String> matches = pNodeTree.getMatchingWorkingNodes(author, view, null);

	if(!matches.isEmpty()) {
	  StringBuilder buf = new StringBuilder();
	  buf.append
	    ("The working area view (" + view + ") owned by user (" + author + ") " + 
	     "cannot be removed because it still contains unreleased nodes!\n\n" + 
	     "The unreleased node are: ");
	  for(String name : matches) 
	    buf.append("\n  " + name);
	  throw new PipelineException(buf.toString());
	}
      }
      
      timer.aquire();
      synchronized(pWorkingAreaViews) {
	timer.resume();	

	/* abort if it doesn't exist */  
	TreeSet<String> views = pWorkingAreaViews.get(author);
	if((views == null) || !views.contains(view))
	  return new SuccessRsp(timer);

	/* determine whether to remove the user as well */
	boolean removeUser = false;
	if(view.equals("default")) {
	  if(views.size() > 1) 
	    throw new PipelineException
	      ("The default view (and therefore the user) can only be removed if it is " + 
	       "the only remaining view!");
	  removeUser = true;
	}

	/* remove the working area files directory */ 
	FileMgrClient fclient = getFileMgrClient();
	try {
	  fclient.removeWorkingArea(author, removeUser ? null : view);
	}
	finally {
	  freeFileMgrClient(fclient);
	}
      
	/* remove the empty working area database directory */ 
	File viewDir = new File(pNodeDir, "working/" + author + "/" + view);
	if(!viewDir.delete()) 
	  throw new PipelineException 
	    ("Unable to remove the working area view database directory (" + viewDir + ")!");

	/* remove view from the runtime table */ 
	views.remove(view);

	/* if no views remain, remove the user as well */ 
	if(views.isEmpty()) {
	  pWorkingAreaViews.remove(author);
	  
	  File userDir = new File(pNodeDir, "working/" + author);
	  if(!userDir.delete()) 
	    throw new PipelineException 
	      ("Unable to remove the working area user database directory " + 
	       "(" + userDir + ")!");
	}
      }
      
      /* post-op tasks */ 
      startExtensionTasks(timer, factory);

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
      try {
	Pattern pat = null;
	if(pattern != null) 
	  pat = Pattern.compile(pattern);
	
	TreeSet<String> matches = pNodeTree.getMatchingWorkingNodes(author, view, pat);

	return new NodeGetWorkingNamesRsp(timer, matches);
      }
      catch(PatternSyntaxException ex) {
	return new FailureRsp(timer, 
			      "Illegal Node Name Pattern:\n\n" + ex.getMessage());
      }
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }  

  /**
   * Get the table of current working area authors and views.
   * 
   * @return
   *   <CODE>NodeUpdatePathRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the working areas.
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
	  views.put(author, new TreeSet<String>(pWorkingAreaViews.get(author))); 
	
	return new NodeGetWorkingAreasRsp(timer, views);
      }
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Add the name of the working area containing node to those currently managed.
   * 
   * @param nodeID
   *   The unique working version identifier.
   */ 
  private void 
  addWorkingAreaForNode
  (
   NodeID nodeID
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
  }

  /**
   * Add the given working version to the node path tree. <P> 
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
    addWorkingAreaForNode(nodeID);    
    pNodeTree.addWorkingNodeTreePath(nodeID, fseqs);
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
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();	

      String author = req.getAuthor();
      String view   = req.getView();

      NodeTreeComp rootComp = pNodeTree.getUpdatedPaths(author, view, req.getPaths());
      
      return new NodeUpdatePathsRsp(timer, author, view, rootComp);
    }
    finally {
      pDatabaseLock.readLock().unlock();
    }
  }

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
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();	

      return new NodeGetNodeOwningRsp(timer, pNodeTree.getNodeOwning(req.getPath()));
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
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    ReentrantReadWriteLock lock = getWorkingLock(req.getNodeID());
    lock.readLock().lock();
    try {
      timer.resume();	
      
      NodeMod mod = new NodeMod(getWorkingBundle(req.getNodeID()).getVersion());
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
    NodeMod nmod = req.getNodeMod();

    TaskTimer timer = new TaskTimer("MasterMgr.modifyProperties(): " + nodeID);
    
    /* pre-op tests */
    ModifyPropertiesExtFactory factory = new ModifyPropertiesExtFactory(nodeID, nmod);
    try {
      performExtensionTests(timer, factory);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }

    timer.aquire();
    pDatabaseLock.readLock().lock();
    ReentrantReadWriteLock lock = getWorkingLock(nodeID);
    lock.writeLock().lock();
    try {
      timer.resume();
      
      if(!pAdminPrivileges.isNodeManaged(req, nodeID)) 
	throw new PipelineException
	  ("Only a user with Node Manager privileges may modify nodes owned by " + 
	   "another user!");

      /* get the working version */ 
      WorkingBundle bundle = getWorkingBundle(nodeID);
      NodeMod mod = new NodeMod(bundle.getVersion());
      if(mod.isFrozen()) 
	throw new PipelineException
	  ("The node properties of frozen node (" + nodeID + ") cannot be modified!");

      /* set the node properties */ 
      Date critical = mod.getLastCriticalModification();
      boolean wasActionEnabled = mod.isActionEnabled();
      if(mod.setProperties(nmod)) {

	/* make sure there are no active jobs, if this is a critical modification */ 
	if((critical.compareTo(mod.getLastCriticalModification()) < 0) &&
	   hasActiveJobs(nodeID, mod.getTimeStamp(), mod.getPrimarySequence()))
	  throw new PipelineException
	    ("Unable to modify critical properties of node (" + nodeID + ") " + 
	     "while there are active jobs associated with the node!");

	/* change working file write permissions? */ 
	if(wasActionEnabled != mod.isActionEnabled()) {
	  FileMgrClient fclient = getFileMgrClient();
	  try {
	    fclient.changeMode(nodeID, mod, !mod.isActionEnabled());
	    mod.updateLastCTimeUpdate();
	  }
	  finally {
	    freeFileMgrClient(fclient);
	  }
	}

	/* write the new working version to disk */ 
	writeWorkingVersion(nodeID, mod);

	/* update the bundle */ 
	bundle.setVersion(mod);
      }

      /* post-op tasks */ 
      startExtensionTasks(timer, factory);

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
    LinkMod slink   = req.getSourceLink();
    String source   = slink.getName();
    NodeID targetID = req.getTargetID();
    NodeID sourceID = new NodeID(targetID, source);

    TaskTimer timer = new TaskTimer("MasterMgr.link(): " + targetID + " to " + sourceID);

    /* pre-op tests */
    LinkExtFactory factory = 
      new LinkExtFactory
        (targetID.getAuthor(), targetID.getView(), targetID.getName(), source, 
	 slink.getPolicy(), slink.getRelationship(), slink.getFrameOffset());
    try {
      performExtensionTests(timer, factory);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }

    timer.aquire();
    pDatabaseLock.readLock().lock();
    ReentrantReadWriteLock targetLock = getWorkingLock(targetID);
    targetLock.writeLock().lock();
    ReentrantReadWriteLock downstreamLock = getDownstreamLock(source);
    downstreamLock.writeLock().lock();
    try {
      timer.resume();

      if(!pAdminPrivileges.isNodeManaged(req, targetID)) 
	throw new PipelineException
	  ("Only a user with Node Manager privileges may create links between nodes in " + 
	   "working areas owned by another user!");

      WorkingBundle bundle = getWorkingBundle(targetID);
      if(bundle == null) 
	throw new PipelineException
	  ("Only working versions of nodes can be linked!\n" + 
	   "No working version (" + targetID + ") exists for the downstream node.");

      NodeMod mod = new NodeMod(bundle.getVersion());
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
      mod.setSource(slink);
      
      /* write the new working version to disk */ 
      writeWorkingVersion(targetID, mod);
      
      /* update the bundle */ 
      bundle.setVersion(mod);

      /* update the downstream links of the source node */ 
      DownstreamLinks links = getDownstreamLinks(source); 
      links.addWorking(sourceID, targetID.getName());

      /* post-op tasks */ 
      startExtensionTasks(timer, factory);

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
    NodeID targetID = req.getTargetID();
    String source   = req.getSourceName();
    NodeID sourceID = new NodeID(targetID, source);

    TaskTimer timer = new TaskTimer("MasterMgr.unlink(): " + targetID + " from " + sourceID);

    /* pre-op tests */
    UnlinkExtFactory factory = 
      new UnlinkExtFactory
        (targetID.getAuthor(), targetID.getView(), targetID.getName(), source);
    try {
      performExtensionTests(timer, factory);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }

    timer.aquire();
    pDatabaseLock.readLock().lock();
    ReentrantReadWriteLock targetLock = getWorkingLock(targetID);
    targetLock.writeLock().lock();
    ReentrantReadWriteLock downstreamLock = getDownstreamLock(source);
    downstreamLock.writeLock().lock();
    try {
      timer.resume();	

      if(!pAdminPrivileges.isNodeManaged(req, targetID)) 
	throw new PipelineException
	  ("Only a user with Node Manager privileges may remove links between nodes in " + 
	   "working areas owned by another user!");

      WorkingBundle bundle = getWorkingBundle(targetID);
      if(bundle == null) 
	throw new PipelineException
	  ("Only working versions of nodes can be unlinked!\n" + 
	   "No working version (" + targetID + ") exists for the downstream node.");

      NodeMod mod = new NodeMod(bundle.getVersion());
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
      bundle.setVersion(mod);

      /* update the downstream links of the source node */ 
      DownstreamLinks links = getDownstreamLinks(source); 
      links.removeWorking(sourceID, targetID.getName());

      /* post-op tasks */ 
      startExtensionTasks(timer, factory);

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
    NodeID nodeID = req.getNodeID();
    FileSeq fseq  = req.getFileSequence();

    TaskTimer timer = new TaskTimer("MasterMgr.addSecondary(): " + nodeID);

    /* pre-op tests */
    AddSecondaryExtFactory factory = new AddSecondaryExtFactory(nodeID, fseq);
    try {
      performExtensionTests(timer, factory);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();

      if(!pAdminPrivileges.isNodeManaged(req, nodeID)) 
	throw new PipelineException
	  ("Only a user with Node Manager privileges may add secondary file sequences " + 
	   "to nodes in working areas owned by another user!");

      /* reserve the node name, 
           after verifying that it doesn't conflict with existing nodes */ 
      {
	pNodeTree.reserveSecondarySeqName(nodeID, fseq);
	addWorkingAreaForNode(nodeID);	
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

	NodeMod mod = new NodeMod(bundle.getVersion());
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
	bundle.setVersion(mod);
	
	/* post-op tasks */ 
	startExtensionTasks(timer, factory);

	return new SuccessRsp(timer);
      }
      catch(PipelineException ex) { 
	timer.aquire();
	pNodeTree.removeSecondaryWorkingNodeTreePath(nodeID, fseq);

	return new FailureRsp(timer, ex.getMessage());
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
    NodeID nodeID = req.getNodeID();
    FileSeq fseq  = req.getFileSequence();

    TaskTimer timer = new TaskTimer("MasterMgr.removeSecondary(): " + nodeID);

    /* pre-op tests */
    RemoveSecondaryExtFactory factory = new RemoveSecondaryExtFactory(nodeID, fseq);
    try {
      performExtensionTests(timer, factory);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }

    timer.aquire();
    pDatabaseLock.readLock().lock();
    ReentrantReadWriteLock lock = getWorkingLock(nodeID);
    lock.writeLock().lock();
    try {
      timer.resume();	

      if(!pAdminPrivileges.isNodeManaged(req, nodeID)) 
	throw new PipelineException
	  ("Only a user with Node Manager privileges may remove secondary file sequences " + 
	   "from nodes in working areas owned by another user!");

      WorkingBundle bundle = getWorkingBundle(nodeID);
      if(bundle == null) 
	throw new PipelineException
	  ("Secondary file sequences can only be remove from working versions of nodes!\n" + 
	   "No working version (" + nodeID + ") exists.");

      NodeMod mod = new NodeMod(bundle.getVersion());
      if(mod.isFrozen()) 
	throw new PipelineException
	  ("The secondary sequences of frozen node (" + nodeID + ") cannot be modified!");

      /* make sure there are no active jobs */ 
      if(hasActiveJobs(nodeID, mod.getTimeStamp(), mod.getPrimarySequence()))
	throw new PipelineException
	  ("Unable to remove secondary file sequences from the node (" + nodeID + ") " + 
	   "while there are active jobs associated with the node!");

      /* remove the sequence */ 
      mod.removeSecondarySequence(fseq);
      
      /* write the new working version to disk */ 
      writeWorkingVersion(nodeID, mod);
      
      /* update the bundle */ 
      bundle.setVersion(mod);

      /* remove the sequence from the node tree */ 
      pNodeTree.removeSecondaryWorkingNodeTreePath(nodeID, fseq);
      
      /* post-op tasks */ 
      startExtensionTasks(timer, factory);

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
    NodeID id   = req.getNodeID();
    String name = id.getName();

    FilePattern npat = req.getFilePattern();
    String nname     = npat.getPrefix();
    NodeID nid       = new NodeID(id, nname);

    TreeSet<FileSeq> oldSeqs = null;
    TreeSet<FileSeq> newSeqs = null;

    TaskTimer timer = new TaskTimer("MasterMgr.rename(): " + id + " to " + npat);

    /* pre-op tests */
    RenameExtFactory factory = new RenameExtFactory(id, npat, req.renameFiles());
    try {
      performExtensionTests(timer, factory);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();	

      if(!pAdminPrivileges.isNodeManaged(req, id)) 
	throw new PipelineException
	  ("Only a user with Node Manager privileges may rename nodes in working " + 
	   "areas owned by another user!");

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
	  NodeMod mod = bundle.getVersion();
	  oldSeqs = mod.getSequences();

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

	    primary   = new FileSeq(pat, range);
	    secondary = mod.getSecondarySequences();

	    newSeqs = new TreeSet<FileSeq>(); 
	    newSeqs.add(primary);
	    newSeqs.addAll(secondary);
	  }
	}
	catch(PipelineException ex) {
	  return new FailureRsp(timer, ex.getMessage());
	}
	finally {
	  lock.readLock().unlock();
	}
      }

      /* verifying that the new node name doesn't conflict with existing node and
         reserve the new name */ 
      try {
	pNodeTree.reserveRename(id, oldSeqs, nid, primary, secondary, newSeqs);
	addWorkingAreaForNode(nid);
      }
      catch(PipelineException ex) {
	return new FailureRsp(timer, ex.getMessage());
      }

      /* if the prefix is different, 
	   unlink the downstream working versions from the to be renamed working version 
	   while collecting the existing downstream links and source parameters */ 
      TreeMap<String,LinkMod> dlinks = null;
      TreeMap<String,String> singleLinkParams = null;
      TreeMap<String,Collection<ActionParam>> sourceParams = null;
      TreeMap<String,TreeMap<FilePattern,Collection<ActionParam>>> secondaryParams = null;
      if(!name.equals(nname)) {
	dlinks = new TreeMap<String,LinkMod>();
	singleLinkParams = new TreeMap<String,String>();
	sourceParams = new TreeMap<String,Collection<ActionParam>>();
	secondaryParams = new TreeMap<String,TreeMap<FilePattern,Collection<ActionParam>>>();

	timer.aquire();
	ReentrantReadWriteLock downstreamLock = getDownstreamLock(id.getName());
	downstreamLock.writeLock().lock();
	try {
	  timer.resume();
	  
	  DownstreamLinks links = getDownstreamLinks(id.getName()); 
	  if(links == null)
	    throw new IllegalStateException(); 
	  
	  for(String target : links.getWorking(id)) {
	    NodeID targetID = new NodeID(id, target);
	    
	    timer.aquire();
	    ReentrantReadWriteLock lock = getWorkingLock(targetID);
	    lock.readLock().lock();
	    try {
	      timer.resume();
	      
	      NodeMod targetMod = getWorkingBundle(targetID).getVersion();
	      LinkMod dlink = targetMod.getSource(name);
	      if(dlink != null) {
		dlinks.put(target, dlink);

		BaseAction targetAction = targetMod.getAction();
		if(targetAction != null) {
		  for(ActionParam aparam : targetAction.getSingleParams()) {
		    if(aparam instanceof LinkActionParam) {
		      LinkActionParam lparam = (LinkActionParam) aparam;
		      if(name.equals(lparam.getStringValue()))
			singleLinkParams.put(target, lparam.getName());
		    }
		  }

		  if(targetAction.getSourceNames().contains(name))
		    sourceParams.put(target, targetAction.getSourceParams(name));
		 
		  if(targetAction.getSecondarySourceNames().contains(name)) {
		    TreeMap<FilePattern,Collection<ActionParam>> sfparams = null;
		    for(FileSeq sfseq : secondary) {
		      FilePattern sfpat = sfseq.getFilePattern();
		      
		      Collection<ActionParam> sparams = 
			targetAction.getSecondarySourceParams(name, sfpat);

		      if(!sparams.isEmpty()) {
			if(sfparams == null) {
			  sfparams = new TreeMap<FilePattern,Collection<ActionParam>>();
			  secondaryParams.put(target, sfparams);
			}

			sfparams.put(sfpat, sparams);
		      }
		    }
		  }
		}
	      }
	    }
	    finally {
	      lock.readLock().unlock();
	    }  
	    
	    timer.suspend();
	    Object obj = unlink(new NodeUnlinkReq(targetID, id.getName()));
	    timer.accum(((TimedRsp) obj).getTimer());
	  }
	}
	finally {
	  downstreamLock.writeLock().unlock();
	}
      }

      {
	timer.aquire();
	ReentrantReadWriteLock lock = getWorkingLock(id);
	lock.writeLock().lock();
	ReentrantReadWriteLock nlock = getWorkingLock(nid);
	nlock.writeLock().lock();
	try {
	  timer.resume();
	  
	  WorkingBundle bundle = getWorkingBundle(id);
	  NodeMod omod = bundle.getVersion();
	  NodeMod nmod = new NodeMod(omod);
	  
	  nmod.rename(npat);
	  
	  if(name.equals(nname)) {
	    /* write the new working version to disk */ 
	    writeWorkingVersion(id, nmod);
	    
	    /* update the bundle */ 
	    bundle.setVersion(nmod);
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
	    for(LinkMod ulink : omod.getSources()) {
	      timer.suspend();
	      Object obj = link(new NodeLinkReq(nid, ulink));
	      timer.accum(((TimedRsp) obj).getTimer());
	      if(obj instanceof FailureRsp) {
		FailureRsp rsp = (FailureRsp) obj;
		throw new PipelineException(rsp.getMessage());
	      }
	    }

	    /* copy any per-source parameters from the old named node to the new named node */
	    BaseAction oaction = omod.getAction();
	    if((oaction != null) && 
	       oaction.supportsSourceParams() && 
	       (!oaction.getSourceNames().isEmpty() || 
		!oaction.getSecondarySourceNames().isEmpty())) {
	      
	      /* relookup the new working version to get the added links */ 
	      nmod = getWorkingBundle(nid).getVersion();
	      
	      /* get the current action related parameters */ 
	      {
		BaseAction naction = nmod.getAction(); 
		naction.setSourceParamValues(oaction);
		nmod.setAction(naction);
	      }
	      
	      /* update the new working version */ 
	      timer.suspend();
	      Object obj = modifyProperties(new NodeModifyPropertiesReq(nid, nmod));
	      timer.accum(((TimedRsp) obj).getTimer());
	      if(obj instanceof FailureRsp) {
		FailureRsp rsp = (FailureRsp) obj;
		throw new PipelineException(rsp.getMessage());
	      }	  
	    }
	    
	    /* release the old named node */ 
	    releaseHelper(id, false, false, timer);
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
	finally {
	  nlock.writeLock().unlock();
	  lock.writeLock().unlock();
	}  
      }

      /* if the prefix is different... */ 	   
      if(!name.equals(nname)) {
	/* reconnect the downstream nodes to the new named node */ 
	for(String target : dlinks.keySet()) {
	  LinkMod dlink = dlinks.get(target);
	  
	  NodeID targetID = new NodeID(id, target);
	  LinkMod ndlink = new LinkMod(nname, dlink.getPolicy(), 
				       dlink.getRelationship(), dlink.getFrameOffset());
	  
	  timer.suspend();
	  Object obj = link(new NodeLinkReq(targetID, ndlink));
	  timer.accum(((TimedRsp) obj).getTimer());
	  if(obj instanceof FailureRsp) {
	    FailureRsp rsp = (FailureRsp) obj;
	    throw new PipelineException(rsp.getMessage());
	  }
	}
	
	/* update downstream action parameters related to the new node name */ 
	{
	  TreeSet<String> targets = new TreeSet<String>();
	  targets.addAll(singleLinkParams.keySet());
	  targets.addAll(sourceParams.keySet());
	  targets.addAll(secondaryParams.keySet());

	  for(String target : targets) {
	    String lparamName = singleLinkParams.get(target);
	    Collection<ActionParam> aparams = sourceParams.get(target);
	    TreeMap<FilePattern,Collection<ActionParam>> sfparams = 
	      secondaryParams.get(target);
	    
	    if((lparamName != null) || 
	       ((aparams != null) && !aparams.isEmpty()) ||
	       ((sfparams != null) && !sfparams.isEmpty())) {

	      /* lookup the target working version */ 
	      NodeID targetID = new NodeID(id, target);
	      NodeMod targetMod = null;
	      {
		timer.aquire();
		ReentrantReadWriteLock lock = getWorkingLock(targetID);
		lock.readLock().lock();
		try {
		  timer.resume();
		  targetMod = new NodeMod(getWorkingBundle(targetID).getVersion());
		}
		finally {
		  lock.readLock().unlock();
		}  
	      }
	      
	      /* get the current action related parameters */ 
	      BaseAction action = targetMod.getAction(); 

	      /* set the value of the single link parameters to the new name if their 
		   previous value was the old name. */ 
	      if(lparamName != null) 
		action.setSingleParamValue(lparamName, nname);
	      	      
	      /* add per-source parameters previously set for the old name under the 
		   new name */ 
	      if((aparams != null) && !aparams.isEmpty()) {
		action.initSourceParams(nname);
		for(ActionParam aparam : aparams) 
		  action.setSourceParamValue(nname, aparam.getName(), aparam.getValue());
	      }

	      /* add per-source secondary parameters previously set for the old name 
	         under the new name */ 
	      if((sfparams != null) && !sfparams.isEmpty()) {
		for(FilePattern sfpat : sfparams.keySet()) {
		  action.initSecondarySourceParams(nname, sfpat);
		  for(ActionParam sparam : sfparams.get(sfpat)) 
		    action.setSecondarySourceParamValue
		      (nname, sfpat, sparam.getName(), sparam.getValue());
		}
	      }

	      /* update the action related parameters */ 
	      targetMod.setAction(action);
	      
	      /* modify the working version */ 
	      timer.suspend();
	      Object obj = modifyProperties(new NodeModifyPropertiesReq(targetID, targetMod));
	      timer.accum(((TimedRsp) obj).getTimer());
	      if(obj instanceof FailureRsp) {
		FailureRsp rsp = (FailureRsp) obj;
		throw new PipelineException(rsp.getMessage());
	      }	  
	    }
	  }
	}
      }

      /* post-op tasks */ 
      startExtensionTasks(timer, factory);

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      /* remove the new working node entry, primary and secondary sequences */ 
      pNodeTree.removeWorkingNodeTreePath(nid, newSeqs); 
      
      /* restore the old working node entry, primary and secondary sequences */ 
      addWorkingNodeTreePath(id, oldSeqs);

      /* abort */ 
      return new FailureRsp(timer, ex.getMessage());
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
    NodeID nodeID = req.getNodeID();
    String name = nodeID.getName();
    FrameRange range = req.getFrameRange();

    TaskTimer timer = new TaskTimer("MasterMgr.renumber(): " + nodeID + " [" + range + "]");

    /* pre-op tests */
    RenumberExtFactory factory = new RenumberExtFactory(nodeID, range, req.removeFiles());
    try {
      performExtensionTests(timer, factory);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }

    timer.aquire();
    pDatabaseLock.readLock().lock();
    ReentrantReadWriteLock lock = getWorkingLock(nodeID);
    lock.writeLock().lock(); 
    try {
      timer.resume();

      if(!pAdminPrivileges.isNodeManaged(req, nodeID)) 
	throw new PipelineException
	  ("Only a user with Node Manager privileges may renumber nodes owned by " + 
	   "another user!");
    
      WorkingBundle bundle = getWorkingBundle(nodeID);
      if(bundle == null) 
	throw new PipelineException
	  ("Only working versions of nodes may have their frame ranges renumbered!\n" + 
	   "No working version (" + nodeID + ") exists.");

      NodeMod mod = new NodeMod(bundle.getVersion());
      if(mod.isFrozen()) 
	throw new PipelineException
	  ("The frozen node (" + nodeID + ") cannot be renumbered!");
      
      /* renumber the file sequences */ 
      ArrayList<File> obsolete = mod.adjustFrameRange(range);

      /* write the new working version to disk */ 
      writeWorkingVersion(nodeID, mod);
      
      /* update the bundle */ 
      bundle.setVersion(mod);

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

      /* check for unfinished jobs associated with the obsolete files */ 
      if(!obsolete.isEmpty()) {
	TreeSet<Long> jobIDs = 
	  pQueueMgrClient.getUnfinishedJobsForNodeFiles(nodeID, obsolete);
	if(!jobIDs.isEmpty()) 
	  return new GetUnfinishedJobsForNodeFilesRsp(timer, jobIDs);
      }
      
      /* post-op tasks */ 
      startExtensionTasks(timer, factory);

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

      return new NodeGetCheckedInRsp(timer, new NodeVersion(bundle.getVersion()));
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
   * Get all of the checked-in versions of a node. <P> 
   * 
   * @param req 
   *   The get checked-in version request.
   * 
   * @return
   *   <CODE>NodeGetAllCheckedInRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to retrieve the checked-in versions.
   */
  public Object
  getAllCheckedInVersions
  ( 
   NodeGetAllCheckedInReq req
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

      TreeMap<VersionID,NodeVersion> versions = new TreeMap<VersionID,NodeVersion>();

      TreeMap<VersionID,CheckedInBundle> checkedIn = getCheckedInBundles(name);
      for(VersionID vid : checkedIn.keySet()) {
	CheckedInBundle bundle = checkedIn.get(vid);
	if(bundle == null) 
	  throw new PipelineException 
	    ("Somehow no checked-in version (" + vid + ") of node (" + name + ") exists!"); 

	versions.put(vid, new NodeVersion(bundle.getVersion()));
      }

      return new NodeGetAllCheckedInRsp(timer, versions); 
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
	history.put(vid, checkedIn.get(vid).getVersion().getLogMessage());

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
	  NodeVersion vsn = checkedIn.get(vid).getVersion();
	  
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

      DoubleMap<VersionID,String,LinkVersion> links = 
	new DoubleMap<VersionID,String,LinkVersion>(); 

      if(checkedIn != null) {
	for(VersionID vid : checkedIn.keySet()) {
	  NodeVersion vsn = checkedIn.get(vid).getVersion();
	  
	  for(LinkVersion link : vsn.getSources()) 
	    links.put(vid, link.getName(), link);
	}
      }
	
      return new NodeGetCheckedInLinksRsp(timer, links);
    }
    finally {
      lock.readLock().unlock();
      pDatabaseLock.readLock().unlock();
    }  
  }  

  /** 
   * Get the links from specific checked-in version to all other checked-in 
   * node versions downstream. 
   * 
   * @param req 
   *   The links request.
   * 
   * @return
   *   <CODE>NodeGetDownstreamCheckedInLinksRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to retrieve the checked-in links.
   */
  public Object
  getDownstreamCheckedInLinks
  ( 
   NodeGetDownstreamCheckedInLinksReq req
  ) 
  {	 
    TaskTimer timer = new TaskTimer();
  
    String name   = req.getName();
    VersionID vid = req.getVersionID();

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();

      TreeMap<String,VersionID> dnodes = null;
      {
	timer.aquire();
	ReentrantReadWriteLock downstreamLock = getDownstreamLock(name);
	downstreamLock.readLock().lock();
	try {
	  timer.resume();	

	  DownstreamLinks dlinks = getDownstreamLinks(name);
	  dnodes = dlinks.getCheckedIn(vid);
	}
	finally {
	  downstreamLock.readLock().unlock();
	}
      }
	
      DoubleMap<String,VersionID,LinkVersion> links = 
	new DoubleMap<String,VersionID,LinkVersion>();

      if(dnodes != null) {
	for(String dname : dnodes.keySet()) {
	  VersionID dvid = dnodes.get(dname);

	  timer.aquire();
	  ReentrantReadWriteLock lock = getCheckedInLock(dname);
	  lock.readLock().lock();
	  try {
	    timer.resume();

	    TreeMap<VersionID,CheckedInBundle> checkedIn = null;
	    try {
	      checkedIn = getCheckedInBundles(dname);
	    }
	    catch(PipelineException ex) {
	    }
	    
	    if(checkedIn != null) {
	      NodeVersion vsn = checkedIn.get(dvid).getVersion();
	      links.put(dname, dvid, vsn.getSource(name));
	    }
	  }
	  finally {
	    lock.readLock().unlock();
	  }
	}
      }
	
      return new NodeGetDownstreamCheckedInLinksRsp(timer, links);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
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
    NodeMod mod   = req.getNodeMod();

    TaskTimer timer = new TaskTimer("MasterMgr.register(): " + nodeID);

    /* pre-op tests */
    RegisterExtFactory factory = new RegisterExtFactory(nodeID, mod);
    try {
      performExtensionTests(timer, factory);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();	
      
      if(!pAdminPrivileges.isNodeManaged(req, nodeID)) 
	throw new PipelineException
	  ("Only a user with Node Manager privileges may register nodes owned " + 
	   "by another user!");

      /* reserve the node name, 
         after verifying that it doesn't conflict with existing nodes */ 
      if(checkName) {
	try {
	  pNodeTree.reserveNewName(nodeID, mod.getPrimarySequence(), mod.getSequences());
	  addWorkingAreaForNode(nodeID);
	}
	catch(PipelineException ex) {
	  return new FailureRsp(timer, ex.getMessage());
	}
      }
      
      timer.aquire();
      ReentrantReadWriteLock lock = getWorkingLock(nodeID);
      lock.writeLock().lock();
      try {
	timer.resume();
	
	/* write the new working version to disk */
	writeWorkingVersion(nodeID, mod);	
	
	/* create a working bundle for the new working version */ 
	synchronized(pWorkingBundles) {
	  HashMap<NodeID,WorkingBundle> table = pWorkingBundles.get(name);
	  if(table == null) {
	    table = new HashMap<NodeID,WorkingBundle>();
	    pWorkingBundles.put(name, table);
	  }
	  table.put(nodeID, new WorkingBundle(mod));
	}
	
	/* keep track of the change to the node version cache */ 
	incrementWorkingCounter(nodeID); 

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
	
	/* post-op tasks */ 
	startExtensionTasks(timer, factory);

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

  /**
   * Release the working versions of nodes and optionally remove the associated 
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
    String author = req.getAuthor();
    String view = req.getView();
    TreeSet<String> nodeNames = req.getNames();
    boolean removeFiles = req.removeFiles();

    TaskTimer timer = new TaskTimer("MasterMgr.release()");
    
    /* pre-op tests */
    ReleaseExtFactory factory = new ReleaseExtFactory(author, view, nodeNames, removeFiles);
    try {
      performExtensionTests(timer, factory);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();	
    
      if(!pAdminPrivileges.isNodeManaged(req, author)) 
	throw new PipelineException
	  ("Only a user with Node Manager privileges may release nodes in working " + 
	   "areas owned by another user!");

      /* determine the link relationships between the nodes being released */ 
      TreeMap<String,NodeLinks> all = new TreeMap<String,NodeLinks>();
      {
	for(String name : nodeNames) 
	  all.put(name, new NodeLinks(name));

	for(String name : all.keySet()) {
	  NodeLinks links = all.get(name);
	  NodeID nodeID = new NodeID(author, view, name);
	  
	  timer.aquire();
	  ReentrantReadWriteLock lock = getWorkingLock(nodeID);
	  lock.readLock().lock(); 
	  try {
	    timer.resume();
	    
	    WorkingBundle bundle = getWorkingBundle(nodeID);
	    if(bundle == null) 
	      throw new PipelineException
		("No working version (" + nodeID + ") exists to be released.");
	    
	    for(String sname : bundle.getVersion().getSourceNames()) {
	      NodeLinks slinks = all.get(sname);
	      if(slinks != null) {
		links.addSource(slinks);
		slinks.addTarget(links);
	      }
	    }
	  }
	  finally {
	    lock.readLock().unlock();
	  }    
	}
      }
      
      /* get the initial tree roots */ 
      TreeSet<String> roots = new TreeSet<String>();
      for(String name : all.keySet()) {
	NodeLinks links = all.get(name);
	if(!links.hasTargets()) 
	  roots.add(name);
      }

      /* release the nodes, roots first */ 
      TreeSet<String> failures = new TreeSet<String>();
      while(!roots.isEmpty()) {
	String name = roots.first();
	NodeLinks links = all.get(name);

	try {
	  releaseHelper(new NodeID(author, view, name), removeFiles, true, timer);
	}
	catch(PipelineException ex) {
	  failures.add(ex.getMessage());
	}

	for(String sname : links.getSourceNames()) {
	  NodeLinks slinks = all.get(sname);
	  slinks.removeTarget(links);
	  if(!slinks.hasTargets()) 
	    roots.add(sname);
	}
	
	roots.remove(name);
      }
      
      if(!failures.isEmpty()) {
	StringBuilder buf = new StringBuilder();
	buf.append("Unable to release all of the selected nodes!");
	for(String msg : failures) 
	  buf.append("\n\n" + msg);
	return new FailureRsp(timer, buf.toString());
      }

      /* post-op tasks */ 
      startExtensionTasks(timer, factory);

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      pDatabaseLock.readLock().unlock();
    } 
  }

  /**
   * Release a single working version of a node and optionally remove the associated 
   * working area files. <P> 
   * 
   * This method should only be called from inside a pDatabaseLock synchronized block
   * of code.
   *
   * @param id 
   *   The unique working version identifier.
   * 
   * @param removeFiles
   *   Should the files associated with the working version be deleted?
   * 
   * @param removeNodeTreePath
   *   Should the node tree path entries for the working version be removed? 
   */
  private void 
  releaseHelper
  (
   NodeID id, 
   boolean removeFiles, 
   boolean removeNodeTreePath, 
   TaskTimer timer
  )
    throws PipelineException 
  {
    String name = id.getName();

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
		throw new PipelineException(rsp.getMessage());
	      }
	    }
	  }
	}
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
      NodeMod mod = bundle.getVersion();
	
      /* kill any active jobs associated with the node */
      killActiveJobs(id, mod.getTimeStamp(), mod.getPrimarySequence());
	
      /* remove the bundle */ 
      synchronized(pWorkingBundles) {
	HashMap<NodeID,WorkingBundle> table = pWorkingBundles.get(name);
	table.remove(id);
      }
	
      /* keep track of the change to the node version cache */ 
      decrementWorkingCounter(id);

      /* remove the working version node file(s) */ 
      {
	File file   = new File(pNodeDir, id.getWorkingPath().toString());
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

	File root = new File(pNodeDir, 
			     "working/" + id.getAuthor() + "/" + id.getView());

	deleteEmptyParentDirs(root, new File(pNodeDir, 
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
      if(removeNodeTreePath) 
	pNodeTree.removeWorkingNodeTreePath(id, mod.getSequences());

      /* remove the associated files */ 
      if(removeFiles) {
	FileMgrClient fclient = getFileMgrClient();
	try {
	  fclient.removeAll(id, mod.getSequences());
	}
	finally {
	  freeFileMgrClient(fclient);
	}	
      }
    }
    finally {
      lock.writeLock().unlock();
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
    String name = req.getName();
    boolean removeFiles = req.removeFiles();

    TaskTimer timer = new TaskTimer("MasterMgr.delete(): " + name);
    
    /* pre-op tests */
    DeleteExtFactory factory = new DeleteExtFactory(name, removeFiles);
    try {
      performExtensionTests(timer, factory);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }

    timer.aquire();
    pDatabaseLock.writeLock().lock();
    try {
      timer.resume();	

      if(!pAdminPrivileges.isMasterAdmin(req)) 
	throw new PipelineException
	  ("Only a user with Master Admin privileges may delete nodes!"); 

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
      {
	boolean failed = false;
	StringBuilder buf = new StringBuilder();
	for(VersionID vid : checkedIn.keySet()) {
	  TreeMap<String,VersionID> dlinks = dsl.getCheckedIn(vid);
	  if(dlinks == null) 
	    throw new PipelineException
	      ("Somehow there was no downstream links entry for checked-in version " + 
	       "(" + vid + ") of node (" + name + ")!");
	  
	  if(!dlinks.isEmpty()) {
	    failed = true;
	    buf.append("\nChecked-in versions downstream of the (" + vid + ") version:\n");
	    for(String dname : dlinks.keySet()) 
	      buf.append("  " + dname + "  (" + dlinks.get(dname) + ")\n");
	  }
	}

	if(failed) {
	  throw new PipelineException
	    ("Cannot delete node (" + name + ") because links to the following " +
	     "checked-in versions exist in the repository:\n" + 
	     buf.toString());
	}
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
	  releaseHelper(nodeID, removeFiles, true, timer);
	  pWorkingLocks.remove(nodeID);
	}
	
	if(!pWorkingBundles.get(name).isEmpty())
	  throw new IllegalStateException(); 
	pWorkingBundles.remove(name);
      }
	
      /* delete the checked-in versions */ 
      if(!checkedIn.isEmpty()) {

	/* remove the downstream links to this node from the checked-in source nodes */ 
	for(VersionID vid : checkedIn.keySet()) {
	  NodeVersion vsn = checkedIn.get(vid).getVersion();
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
	  File file = new File(pNodeDir, "repository" + name + "/" + vid);
	  if(!file.delete())
	    throw new PipelineException
	      ("Unable to remove the checked-in version file (" + file + ")!");
	}

	/* remove the checked-in version node directory */
	{
	  File dir = new File(pNodeDir, "repository" + name);
	  File parent =  dir.getParentFile();
	  if(!dir.delete())
	    throw new PipelineException
	      ("Unable to remove the checked-in version directory (" + dir + ")!");
	  
	  deleteEmptyParentDirs(new File(pNodeDir, "repository"), parent);
	}	    
	
	/* remove the checked-in version entries */ 
	pCheckedInLocks.remove(name);
	pCheckedInBundles.remove(name);

	/* keep track of the change to the node version cache */ 
	for(VersionID vid : checkedIn.keySet()) 
	  decrementCheckedInCounter(name, vid);
      }

      /* remove the downstream links file and entry */ 
      {
	File file = new File(pNodeDir, "downstream" + name);
	if(file.isFile()) {
	  if(!file.delete())
	    throw new PipelineException
	      ("Unable to remove the downstream links file (" + file + ")!");
	}

	pDownstream.remove(name);
      }

      /* remove the leaf node tree entry */ 
      pNodeTree.removeNodeTreePath(name);

      /* post-op tasks */ 
      startExtensionTasks(timer, factory);

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
    NodeID nodeID = req.getNodeID();

    TaskTimer timer = new TaskTimer("MasterMgr.checkIn(): " + nodeID);

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();	

      if(!pAdminPrivileges.isNodeManaged(req, nodeID)) 
	throw new PipelineException
	  ("Only a user with Node Manager privileges may check-in nodes owned by " + 
	   "another user!");

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
   *   <CODE>GetUnfinishedJobsForNodesRsp</CODE> if running jobs prevent the check-out or 
   *   <CODE>FailureRsp</CODE> if unable to the check-out the nodes.
   */ 
  public Object
  checkOut
  ( 
   NodeCheckOutReq req 
  ) 
  {
    NodeID nodeID = req.getNodeID();

    TaskTimer timer = new TaskTimer("MasterMgr.checkOut(): " + nodeID);

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();	      

      if(!pAdminPrivileges.isNodeManaged(req, nodeID)) 
	throw new PipelineException
	  ("Only a user with Node Manager privileges may check-out nodes owned by " + 
	   "another user!");

      /* get the current status of the nodes */ 
      HashMap<String,NodeStatus> table = new HashMap<String,NodeStatus>();
      performUpstreamNodeOp(new NodeOp(), req.getNodeID(), false, 
			    new LinkedList<String>(), table, timer);

      /* determine all checked-in versions required by the check-out operation */ 
      TreeMap<String,TreeSet<VersionID>> requiredVersions = 
	new TreeMap<String,TreeSet<VersionID>>();
      {
	collectRequiredVersions
	  (true, nodeID, req.getVersionID(), false, req.getMode(), req.getMethod(), 
	   table, requiredVersions, new LinkedList<String>(), new HashSet<String>(), 
	   timer);
      }

      /* make sure no unfinished jobs associated with either the current upstream nodes, 
	 the upstream nodes which will be checked-out or the checked-out downstream nodes 
         currently exist */ 
      {
	TreeMap<String,FileSeq> fseqs = new TreeMap<String,FileSeq>();
	for(String source : requiredVersions.keySet()) {
	  NodeID snodeID = new NodeID(nodeID, source);
	  getDownstreamWorkingSeqs(snodeID, new LinkedList<String>(), fseqs, timer);
	}

	if(!fseqs.isEmpty()) {
	  TreeMap<String,TreeSet<Long>> jobIDs = 
	    pQueueMgrClient.getUnfinishedJobsForNodes
	    (nodeID.getAuthor(), nodeID.getView(), fseqs);
	  
	  if(!jobIDs.isEmpty()) 
	    return new GetUnfinishedJobsForNodesRsp(timer, jobIDs);
	}
      }

      /* lock online/offline status of all required nodes */ 
      timer.aquire();
      List<ReentrantReadWriteLock> onOffLocks = 
	onlineOfflineReadLock(requiredVersions.keySet());
      try {
	timer.resume();	
      
	/* get the names of any required versions which are currently offline */ 
	TreeMap<String,TreeSet<VersionID>> offlineVersions = 
	  new TreeMap<String,TreeSet<VersionID>>();
	{
	  for(String name : requiredVersions.keySet()) {

	    timer.aquire();
	    synchronized(pOfflined) {
	      timer.resume();

	      TreeSet<VersionID> offline = pOfflined.get(name);
	      
	      if(offline != null) {
		for(VersionID vid : requiredVersions.get(name)) {
		  if(offline.contains(vid)) {
		    TreeSet<VersionID> ovids = offlineVersions.get(name);
		    if(ovids == null) {
		      ovids = new TreeSet<VersionID>();
		      offlineVersions.put(name, ovids);
		    }
		    ovids.add(vid);
		  }
		}
	      }
	    }
	  }
	}

	/* abort if all required versions are not online */ 
	if(!offlineVersions.isEmpty()) {
	  StringBuilder buf = new StringBuilder();
	  {
	    buf.append
	      ("Unable to perform check-out because the following checked-in versions " + 
	       "are currently offline:\n\n");
	    for(String name : offlineVersions.keySet()) {
	      for(VersionID vid : offlineVersions.get(name)) 
		buf.append(name + " v" + vid + "\n");
	    }
	    buf.append("\n");
	  }
	  
	  Object obj = requestRestore(new MiscRequestRestoreReq(offlineVersions));
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

	/* are there enabled server extensions? */ 
	CheckOutExtFactory factory = new CheckOutExtFactory();
	boolean anyExtTests = hasAnyExtensionTests(timer, factory); 
	boolean anyExtTasks = hasAnyExtensionTasks(timer, factory); 

	/* check-out the nodes */ 
	performCheckOut
	  (true, nodeID, req.getVersionID(), false, anyExtTests, anyExtTasks, 
	   req.getMode(), req.getMethod(), 
	   table, new LinkedList<String>(), new HashSet<String>(), 
	   new HashSet<String>(), timer);
      }
      finally {
	onlineOfflineReadUnlock(onOffLocks);
      }

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
   * Recursively collect the names of the checked-in versions required by the 
   * check-out operation. 
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
   * @param isLocked
   *   Whether the current node should be checked-out locked.
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
   * @param requiredVersions
   *   The names and revision numbers of the checked-in versions required by the check-out 
   *   operation.
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
  collectRequiredVersions
  (
   boolean isRoot, 
   NodeID nodeID, 
   VersionID vid, 
   boolean isLocked, 
   CheckOutMode mode,
   CheckOutMethod method, 
   HashMap<String,NodeStatus> stable,
   TreeMap<String,TreeSet<VersionID>> requiredVersions, 
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
	performUpstreamNodeOp(new NodeOp(), nodeID, isLocked, 
			      new LinkedList<String>(), stable, timer);
	status = stable.get(name);
      }

      details = status.getDetails();
      if(details == null)
	throw new IllegalStateException(); 
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
	if(checkedIn == null)
	  throw new IllegalStateException(); 
      }

      /* extract the working and the checked-in version to be checked-out */ 
      NodeMod work    = null;
      NodeVersion vsn = null;
      {
	if(working != null)
	  work = new NodeMod(working.getVersion());

	if(vid != null) {
	  CheckedInBundle bundle = checkedIn.get(vid);
	  if(bundle == null) 
	    throw new PipelineException 
	      ("Somehow no checked-in version (" + vid + ") of node (" + name + ") exists!"); 
	  vsn = new NodeVersion(bundle.getVersion());
	}
	else {
	  if(checkedIn.isEmpty())
	    throw new PipelineException
	      ("Somehow no checked-in versions of node (" + name + ") exist!"); 
	  CheckedInBundle bundle = checkedIn.get(checkedIn.lastKey());
	  vsn = new NodeVersion(bundle.getVersion());
	}
	if(vsn == null)
	  throw new IllegalStateException(); 
      }

      /* mark having seen this node already */ 
      seen.add(name);
 
      /* determine the check-out method for upstream nodes */ 
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

	case KeepModified:
	  if(!isRoot && (work.getWorkingID().compareTo(vsn.getVersionID()) >= 0)) {
	    branch.removeLast();
	    return;
	  }
	}
      }

      /* process the upstream nodes */
      if(!isLocked) {
	for(LinkVersion link : vsn.getSources()) {
	  NodeID lnodeID = new NodeID(nodeID, link.getName());
	  collectRequiredVersions(false, lnodeID, link.getVersionID(), link.isLocked(), 
				  mode, checkOutMethod, 
				  stable, requiredVersions, branch, seen, timer);
	}
      }

      /* add checked-in version to the required versions */ 
      {
	TreeSet<VersionID> rvids = requiredVersions.get(name);
	if(rvids == null) {
	  rvids = new TreeSet<VersionID>();
	  requiredVersions.put(name, rvids);
	}
	rvids.add(vid);
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
   * @param isLocked
   *   Whether the current node should be checked-out locked.
   * 
   * @param hasExtTests
   *   Whether there are enabled server extension tests for this operation.
   * 
   * @param hasExtTasks
   *   Whether there are enabled server extension tasks for this operation.
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
   boolean isLocked, 
   boolean hasExtTests, 
   boolean hasExtTasks, 
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
	performUpstreamNodeOp(new NodeOp(), nodeID, isLocked, 
			      new LinkedList<String>(), stable, timer);
	status = stable.get(name);
      }

      details = status.getDetails();
      if(details == null)
	throw new IllegalStateException(); 
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
	if(checkedIn == null)
	  throw new IllegalStateException(); 
      }

      /* extract the working and the checked-in version to be checked-out */ 
      NodeMod work    = null;
      NodeVersion vsn = null;
      {
	if(working != null)
	  work = new NodeMod(working.getVersion());

	if(vid != null) {
	  CheckedInBundle bundle = checkedIn.get(vid);
	  if(bundle == null) 
	    throw new PipelineException 
	      ("Somehow no checked-in version (" + vid + ") of node (" + name + ") exists!"); 
	  vsn = new NodeVersion(bundle.getVersion());
	}
	else {
	  if(checkedIn.isEmpty())
	    throw new PipelineException
	      ("Somehow no checked-in versions of node (" + name + ") exist!"); 
	  CheckedInBundle bundle = checkedIn.get(checkedIn.lastKey());
	  vsn = new NodeVersion(bundle.getVersion());
	}
	if(vsn == null)
	  throw new IllegalStateException(); 
      }

      /* pre-op test */ 
      if(hasExtTests) {
	CheckOutExtFactory factory = 
	  new CheckOutExtFactory(nodeID, new NodeVersion(vsn), mode, method);
        performExtensionTests(timer, factory);
      }

      /* mark having seen this node already */ 
      seen.add(name);
 
      /* determine whether working files or links should be created */ 
      boolean isFrozen = false;
      CheckOutMethod checkOutMethod = method;   
      if(isLocked) {
	isFrozen = true;   
      }
      else {
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
      }

      /* see if the check-out should be skipped, 
           and if skipped whether the node should be marked dirty */ 
      if(work != null) {
	switch(mode) {
	case OverwriteAll:
	  if((details != null) && 
	     (details.getOverallNodeState() == OverallNodeState.Identical) && 
	     (details.getOverallQueueState() == OverallQueueState.Finished) && 
	     work.getWorkingID().equals(vsn.getVersionID())) {

	    switch(method) {
	    case Modifiable:
	      break;

	    case PreserveFrozen:
	    case AllFrozen:
	      if((work.isFrozen() == isFrozen) && (work.isLocked() == isLocked)) {
		branch.removeLast();
		return;
	      }
	      break;

	    case FrozenUpstream:
	      if(!isRoot && (work.isFrozen() == isFrozen)) {
		branch.removeLast();
		return;
	      }
	    }
	  }
	  break;

	case KeepModified:
	  if(!isRoot && (work.getWorkingID().compareTo(vsn.getVersionID()) >= 0)) {
	    branch.removeLast();

	    /* is the working version newer? */ 
	    if(work.getWorkingID().compareTo(vsn.getVersionID()) > 0) {
	      dirty.add(name);
	    }
	    /* or is the working version modified? */ 
	    else {
	      switch(details.getOverallNodeState()) {
	      case Identical:
	      case ModifiedLocks:
	      case NeedsCheckOut:
		break;

	      case Conflicted:
	      case ModifiedLinks:
	      case Modified:
	      case Missing:
	      case MissingNewer:
		dirty.add(name);
		break;

	      default:
		throw new IllegalStateException
		  ("The " + details.getOverallNodeState() + " should not be possible here!");
	      }
	    }

	    return;
	  }
	}
      }

      /* process the upstream nodes */ 
      if(!isLocked) {
	for(LinkVersion link : vsn.getSources()) {
	  NodeID lnodeID = new NodeID(nodeID, link.getName());
	  performCheckOut(false, lnodeID, link.getVersionID(), link.isLocked(), 
			  hasExtTests, hasExtTasks, 
			  mode, checkOutMethod, stable, branch, seen, dirty, timer);
	  
	  /* if any of the upstream nodes are dirty, 
  	     mark this node as dirty and make sure it isn't frozen */ 
	  if(dirty.contains(link.getName())) {
	    dirty.add(name);
	    isFrozen = false;
	  }
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
      NodeMod nwork = new NodeMod(vsn, timestamp, isFrozen, isLocked);
      writeWorkingVersion(nodeID, nwork);

      /* initialize new working version */ 
      if(working == null) {
	/* register the node name and sequences */ 
	addWorkingNodeTreePath(nodeID, nwork.getSequences());

	/* create a new working bundle */ 
	synchronized(pWorkingBundles) {
	  HashMap<NodeID,WorkingBundle> table = pWorkingBundles.get(name);
	  if(table == null) {
	    table = new HashMap<NodeID,WorkingBundle>();
	    pWorkingBundles.put(name, table);
	  }
	  table.put(nodeID, new WorkingBundle(nwork));
	}

	/* keep track of the change to the node version cache */ 
	incrementWorkingCounter(nodeID);

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
	/* unregister the old working node name and sequences,
	   register the new node name and sequences */ 
	pNodeTree.updateWorkingNodeTreePath
	  (nodeID, work.getSequences(), nwork.getSequences());
	
	/* update the working bundle */ 
	working.setVersion(nwork);

	/* remove the downstream links from any obsolete upstream nodes */ 
	for(LinkMod link : work.getSources()) {
	  if(isLocked || 
	     !nwork.getSourceNames().contains(link.getName())) {
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
      if(!isLocked) {
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

      /* post-op tasks */  
      if(hasExtTasks) 
	startExtensionTasks(timer, new CheckOutExtFactory(nodeID, new NodeMod(nwork)));
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
   * Lock the working version of a node to a specific checked-in version. <P> 
   * 
   * @param req 
   *   The node lock request.
   *
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to the lock the node.
   */ 
  public Object
  lock
  ( 
   NodeLockReq req 
  ) 
  {
    NodeID nodeID = req.getNodeID();
    String name = nodeID.getName();
    VersionID vid = req.getVersionID();

    TaskTimer timer = new TaskTimer("MasterMgr.lock(): " + nodeID);

    /* pre-op tests */
    LockExtFactory factory = new LockExtFactory(nodeID, vid); 
    try {
      performExtensionTests(timer, factory);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();	

      if(!pAdminPrivileges.isNodeManaged(req, nodeID)) 
	throw new PipelineException
	  ("Only a user with Node Manager privileges may lock nodes in working " + 
	   "areas owned by another user!");

      /* lock online/offline status of the node to lock */ 
      timer.aquire();
      ReentrantReadWriteLock onOffLock = getOnlineOfflineLock(name);
      onOffLock.readLock().lock();
      try {
	timer.resume();

	/* check if the target version is currently offline */ 
	boolean isOffline = false;
	{
	  timer.aquire();
	  synchronized(pOfflined) {
	    timer.resume();
	    TreeSet<VersionID> offline = pOfflined.get(name);
	    if((offline != null) && offline.contains(vid)) 
	      isOffline = true;
	  }
	}
	
	/* abort if the target version is offline */ 
	if(isOffline) {
	  StringBuilder buf = new StringBuilder();
	  buf.append
	    ("Unable to lock node (" + name + ") to checked-in version (" + vid + ") " + 
	     "because that version is currently offline!\n\n");

	  TreeSet<VersionID> ovids = new TreeSet<VersionID>();
	  ovids.add(vid);
	  
	  TreeMap<String,TreeSet<VersionID>> ovsns = new TreeMap<String,TreeSet<VersionID>>();
	  ovsns.put(name, ovids);

	  Object obj = requestRestore(new MiscRequestRestoreReq(ovsns));
	  if(obj instanceof FailureRsp) {
	    FailureRsp rsp = (FailureRsp) obj;
	    buf.append
	      ("The request to restore this offline version also failed:\n\n" + 
	       rsp.getMessage());	    
	  }
	  else {
	    buf.append
	      ("However, a request has been submitted to restore this offline version " + 
	       "so that it may be used once it has been brought back online.");
	  }
	  
	  throw new PipelineException(buf.toString());
	}

	/* lock the node */ 
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
	      throw new PipelineException
		("There are no checked-in versions of node (" + name + ") to lock!");
	    }
	    if(checkedIn == null)
	      throw new IllegalStateException(); 
	  }
	
	  /* extract the working and the checked-in versions */ 
	  NodeMod work = null;
	  NodeVersion vsn = null;
	  {
	    if(working != null)
	      work = new NodeMod(working.getVersion());
	  
	    if(vid == null) {
	      if(work == null) 
		throw new PipelineException
		  ("No working version of node (" + name + ") exists and no revision " + 
		   "number was specified for the lock operation!");
	      vid = work.getWorkingID();
	    }
	    if(vid == null)
	      throw new IllegalStateException(); 

	    CheckedInBundle bundle = checkedIn.get(vid);
	    if(bundle == null) 
	      throw new PipelineException
		("No checked-in version (" + vid + ") of node (" + name + ") exists!"); 
	    vsn = new NodeVersion(bundle.getVersion());
	    if(vsn == null)
	      throw new IllegalStateException(); 
	  }

	  /* make sure the checked-in version has no Reference links */ 
	  for(LinkVersion link : vsn.getSources()) {
	    if(link.getPolicy() == LinkPolicy.Reference) 
	      throw new PipelineException
		("Unable to lock node (" + name + ") because the checked-in version " + 
		 "of the node had a Reference link to node (" + link.getName() + ")!");
	  }

	  /* get the current timestamp */ 
	  Date timestamp = Dates.now(); 

	  {
	    FileMgrClient fclient = getFileMgrClient();
	    try {
	      /* remove the existing working area files before the check-out */ 
	      if(work != null) 
		fclient.removeAll(nodeID, work.getSequences());	

	      /* check-out the links to the checked-in files */
	      fclient.checkOut(nodeID, vsn, true);
	    }
	    finally {
	      freeFileMgrClient(fclient);
	    }
	  }

	  /* create a new working version and write it to disk */ 
	  NodeMod nwork = new NodeMod(vsn, timestamp, true, true);
	  writeWorkingVersion(nodeID, nwork);
	
	  /* initialize new working version */ 
	  if(working == null) {
	    /* register the node name */ 
	    addWorkingNodeTreePath(nodeID, nwork.getSequences());
	  
	    /* create a new working bundle */ 
	    synchronized(pWorkingBundles) {
	      HashMap<NodeID,WorkingBundle> table = pWorkingBundles.get(name);
	      if(table == null) {
		table = new HashMap<NodeID,WorkingBundle>();
		pWorkingBundles.put(name, table);
	      }
	      table.put(nodeID, new WorkingBundle(nwork));
	    }
	  
	    /* keep track of the change to the node version cache */ 
	    incrementWorkingCounter(nodeID);

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
	    working.setVersion(nwork);

	    /* remove the downstream links from all upstream nodes */ 
	    for(LinkMod link : work.getSources()) {
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
	  
	  /* post-op tasks */ 
	  startExtensionTasks(timer, factory);

	  return new SuccessRsp(timer);
	}
	catch(PipelineException ex) {
	  return new FailureRsp(timer, ex.getMessage());
	}
	finally {
	  checkedInLock.readLock().unlock();  
	  workingLock.readLock().unlock();
	}
      }
      finally {
	onOffLock.readLock().unlock();
      }
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, "Lock operation aborted!\n\n" + ex.getMessage());
    }  
    finally {
      pDatabaseLock.readLock().unlock();
    }  
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

    /* pre-op tests */
    RevertFilesExtFactory factory = new RevertFilesExtFactory(nodeID, files);
    try {
      performExtensionTests(timer, factory);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();	      
      
      if(!pAdminPrivileges.isNodeManaged(req, nodeID)) 
	throw new PipelineException
	  ("Only a user with Node Manager privileges may revert files associated with " + 
	   "nodes in working areas owned by another user!");

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

	  NodeMod mod = bundle.getVersion();
	  if(mod.isFrozen()) 
	    throw new PipelineException
	      ("The files associated with frozen node (" + nodeID + ") cannot be reverted!");

	  writeable = !mod.isActionEnabled();
	}
	finally {
	  lock.readLock().unlock();
	}
      }

      /* lock online/offline status of the node */ 
      timer.aquire();
      ReentrantReadWriteLock onOffLock = getOnlineOfflineLock(name);
      onOffLock.readLock().lock();
      try {
	timer.resume();	

	/* check whether the checked-in versions are currently online */ 
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
	    TreeMap<String,TreeSet<VersionID>> vsns = 
	      new TreeMap<String,TreeSet<VersionID>>();
	    vsns.put(name, ovids);
	    
	    StringBuilder buf = new StringBuilder();
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
      }
      finally {
	onOffLock.readLock().unlock();
      }

      /* post-op tasks */ 
      startExtensionTasks(timer, factory);

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

    /* pre-op tests */
    CloneFilesExtFactory factory = new CloneFilesExtFactory(sourceID, targetID);
    try {
      performExtensionTests(timer, factory);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();	      
      
      if(!pAdminPrivileges.isNodeManaged(req, targetID)) 
	throw new PipelineException
	  ("Only a user with Node Manager privileges may clone files associated with " + 
	   "nodes in working areas owned by another user!");

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

	  NodeMod mod = bundle.getVersion();
	  sourceSeq = mod.getPrimarySequence();
	}
	finally {
	  lock.readLock().unlock();
	}
      }
      
      FileSeq targetSeq = null;
      boolean writeable = true;
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

	  NodeMod mod = bundle.getVersion();
	  if(mod.isFrozen()) 
	    throw new PipelineException
	      ("The files associated with frozen node (" + targetID + ") " + 
	       "cannot be replaced!");

	  targetSeq = mod.getPrimarySequence();
	  writeable = !mod.isActionEnabled();
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
	
      /* post-op tasks */ 
      startExtensionTasks(timer, factory);

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
    String name = nodeID.getName();
    VersionID vid = req.getVersionID();

    TaskTimer timer = new TaskTimer("MasterMgr.evolve(): " + nodeID);

    /* pre-op tests */
    EvolveExtFactory factory = new EvolveExtFactory(nodeID, vid); 
    try {
      performExtensionTests(timer, factory);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();	

      if(!pAdminPrivileges.isNodeManaged(req, nodeID)) 
	throw new PipelineException
	  ("Only a user with Node Manager privileges may evolve nodes in working " + 
	   "areas owned by another user!");

      /* verify the checked-in revision number */ 
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

      /* lock online/offline status of the node */ 
      timer.aquire();
      ReentrantReadWriteLock onOffLock = getOnlineOfflineLock(name);
      onOffLock.readLock().lock();
      try {
	timer.resume();	

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
		   "because the version is currently offline.  However, a request has " + 
		   "been submitted to restore the version so that it may be used once " + 
		   "it has been brought back online.");
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
	  NodeMod mod = new NodeMod(bundle.getVersion());
	  if(mod.isFrozen()) 
	    throw new PipelineException
	    ("The frozen node (" + nodeID + ") cannot be evolved!");
	  mod.setWorkingID(vid);
	  
	  /* write the working version to disk */ 
	  writeWorkingVersion(nodeID, mod);
	  
	  /* update the bundle */ 
	  bundle.setVersion(mod);
	}
	finally {
	  lock.writeLock().unlock();
	}  
      }
      finally {
	onOffLock.readLock().unlock();
      }  

      /* post-op tasks */ 
      startExtensionTasks(timer, factory);
      
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
      
      if(!pAdminPrivileges.isQueueManaged(req, req.getNodeID()))
	throw new PipelineException
	  ("Only a user with Queue Manager privileges may submit jobs for nodes in " + 
	   "working areas owned by another user!");

      /* get the current status of the nodes */ 
      NodeStatus status = performNodeOperation(new NodeOp(), req.getNodeID(), timer);

      /* submit the jobs */ 
      return submitJobsCommon(status, req.getFileIndices(),
			      req.getBatchSize(), req.getPriority(), req.getRampUp(), 
			      req.getSelectionKeys(), req.getLicenseKeys(), 
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

      if(!pAdminPrivileges.isQueueManaged(req, req.getNodeID()))
	throw new PipelineException
	  ("Only a user with Queue Manager privileges may submit jobs for nodes in " + 
	   "working areas owned by another user!");

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
			      req.getSelectionKeys(), req.getLicenseKeys(), 
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
   * @param licenseKeys 
   *   Overrides the set of license keys required by them job associated with the root 
   *   node of the job submission.
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
   Set<String> licenseKeys,
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
		 true, batchSize, priority, rampUp, selectionKeys, licenseKeys,
		 extJobIDs, nodeJobIDs, upsJobIDs, rootJobIDs, jobs, 
		 timer);
      
      if(jobs.isEmpty()) 
	throw new PipelineException
	  ("No new jobs where generated for node (" + status + ") or any node upstream " +
	   "of this node!");
      
      /* generate the root target file sequence for the job group, 
	   sorting root IDs by target file sequence order */ 
      FileSeq targetSeq = null;
      ArrayList<Long> orderedRootIDs = new ArrayList<Long>();
      {
	TreeMap<String,TreeMap<Integer,Long>> rootOrder = 
	  new TreeMap<String,TreeMap<Integer,Long>>();

	FilePattern fpat = null;
	TreeSet<Integer> frames = new TreeSet<Integer>();
	{
	  for(Long jobID : rootJobIDs) {
	    QueueJob job = jobs.get(jobID);
	    FileSeq fseq = job.getActionAgenda().getPrimaryTarget();
	    
	    if(fpat == null) 
	      fpat = fseq.getFilePattern();

	    int start = 0;
	    FrameRange range = fseq.getFrameRange();
	    if(range != null) {
	      int fnums[] = range.getFrameNumbers();
	      int wk;
	      for(wk=0; wk<fnums.length; wk++) 
		frames.add(fnums[wk]);

	      start = range.getStart();
	    }

	    TreeMap<Integer,Long> frameOrder = rootOrder.get(fpat.toString());
	    if(frameOrder == null) {
	      frameOrder = new TreeMap<Integer,Long>();
	      rootOrder.put(fpat.toString(), frameOrder);
	    }
	    frameOrder.put(start, jobID);
	  }
	}

	if(frames.isEmpty()) 
	  targetSeq = new FileSeq(fpat, null);
	else 
	  targetSeq = new FileSeq(fpat, new FrameRange(frames));

	for(TreeMap<Integer,Long> frameOrder : rootOrder.values()) 
	  orderedRootIDs.addAll(frameOrder.values());
      }
      
      /* generate the list of external job IDs */ 
      TreeSet<Long> externalIDs = new TreeSet<Long>();
      for(QueueJob job : jobs.values()) 
	for(Long jobID : job.getSourceJobIDs()) 
	  if(!jobs.containsKey(jobID)) 
	    externalIDs.add(jobID);
      
      /* create the job group */ 
      QueueJobGroup group = 
	new QueueJobGroup(pNextJobGroupID++, status.getNodeID(), 
			  status.getDetails().getWorkingVersion().getToolset(), 
			  targetSeq, orderedRootIDs, externalIDs, 
			  new TreeSet<Long>(jobs.keySet()));

      /* update the job and group IDs file */ 
      writeNextIDs();
      
      /* submit the jobs and job group */ 
      pQueueMgrClient.submitJobs(group, jobs.values());
      
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
   * @param licenseKeys 
   *   Overrides the set of license keys required by them job associated with the root 
   *   node of the job submission.
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
   Set<String> licenseKeys,
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
    if(work.isLocked()) 
      return;

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
    if(jobIDs.length != queueStates.length)
      throw new IllegalStateException(); 

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
      
    case Subdivided:
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
		  if(extIDs[idx] == null)
		    throw new IllegalStateException(); 
		}
		break;

	      case Stale:
	      case Aborted:
	      case Failed:
		regen.add(idx);
		break;

	      case Undefined:
		throw new IllegalStateException(); 
	      }
	    }
	  }
	}

	/* group the frames into batches */ 
	if(!regen.isEmpty()) {
	  switch(work.getExecutionMethod()) {
	  case Subdivided:
	    {
	      int maxIdx = regen.last();
	      if(maxIdx == 0) {
		TreeSet<Integer> batch = new TreeSet<Integer>();
		batch.add(maxIdx);
		batches.add(batch);
	      }
	      else {
		ArrayList<Integer> sindices = new ArrayList<Integer>();
		if(regen.contains(0)) 
		  sindices.add(0);
		
		{
		  int e = (int) Math.floor(Math.log((double) maxIdx) / Math.log(2.0));
		  for(; e>=0; e--) {
		    int si = (int) Math.pow(2.0, (double) e);
		    int inc = si * 2;
		    int i;
		    for(i=si; i<=maxIdx; i+=inc) {
		      if(regen.contains(i)) 
			sindices.add(i);
		    }
		  }
		}

		for(Integer i : sindices) {
		  TreeSet<Integer> batch = new TreeSet<Integer>();
		  batch.add(i);
		  batches.add(batch);
		}
	      }
	    }
	    break;
	    
	  case Parallel:
	    {
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
      }
    }
    
    /* no batches to generate, skip processing upstream nodes */ 
    if(batches.isEmpty()) {
      return; 
    }

    /* generate jobs for each frame batch */ 
    else {
      if(work.isFrozen()) 
	throw new PipelineException
	  ("Cannot generate jobs for the frozen node (" + nodeID + ")!");
      
      for(TreeSet<Integer> batch : batches) {
	if(batch.isEmpty())
	  throw new IllegalStateException(); 
	
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
		       false, null, null, null, null, null, 
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
	  if(upsIDs != null) 
	    sourceIDs.addAll(upsIDs);

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
	  TreeMap<String,ActionInfo> actionInfos = new TreeMap<String,ActionInfo>();
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

	      BaseAction laction = lwork.getAction();
	      if(laction != null) {
		LinkMod slink = work.getSource(sname);
		if((slink != null) && (slink.getPolicy() == LinkPolicy.Dependency)) {
		  ActionInfo ainfo = new ActionInfo(laction, lwork.isActionEnabled());
		  actionInfos.put(sname, ainfo);
		}
	      }
	    }
	  }

	  cacheToolset(work.getToolset(), timer);

	  ActionAgenda agenda = 
	    new ActionAgenda(jobID, nodeID, 
			     primaryTarget, secondaryTargets, 
			     primarySources, secondarySources, actionInfos,  
			     work.getToolset());
	  
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

	    if(isRoot && (licenseKeys != null)) {
	      jreqs.removeAllLicenseKeys(); 
	      jreqs.addLicenseKeys(licenseKeys);
	    }
	  }

	  BaseAction action = work.getAction();
	  {
	    /* strip per-source parameters which do not correspond to secondary sequences
	       of the currently linked upstream nodes */ 
	    {
	      TreeMap<String,TreeSet<FilePattern>> dead = 
		new TreeMap<String,TreeSet<FilePattern>>();

	      for(String sname : action.getSecondarySourceNames()) {
		Set<FilePattern> fpats = action.getSecondarySequences(sname);
		if(!fpats.isEmpty()) {
		  NodeMod lmod = status.getSource(sname).getDetails().getWorkingVersion();
		  
		  TreeSet<FilePattern> live = new TreeSet<FilePattern>();
		  for(FileSeq fseq : lmod.getSecondarySequences()) 
		    live.add(fseq.getFilePattern());
		  
		  for(FilePattern fpat : fpats) {
		    if(!live.contains(fpat)) {
		      TreeSet<FilePattern> dpats = dead.get(sname);
		      if(dpats == null) {
			dpats = new TreeSet<FilePattern>();
			dead.put(sname, dpats);
		      }
		      dpats.add(fpat);
		    }
		  }
		}
	      }

	      for(String sname : dead.keySet()) {
		for(FilePattern fpat : dead.get(sname)) 
		  action.removeSecondarySourceParams(sname, fpat);
	      }
	    }
	  }

	  QueueJob job = 
	    new QueueJob(agenda, action, jreqs, sourceIDs);
		       
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
    if(work.isLocked()) 
      return;

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
		 false, null, null, null, null, null, 
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
   * {@link JobState#Preempted Preempted}, {@link JobState#Paused Paused} or 
   * {@link JobState#Running Running}.
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
	case Preempted:
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
   * {@link JobState#Preempted Preempted}, {@link JobState#Paused Paused} or 
   * {@link JobState#Running Running}.
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
	case Preempted:
	case Paused:
	case Running:
	  activeIDs.add(jobID);
	}
      }

      wk++;
    }

    if(!activeIDs.isEmpty()) 
      pQueueMgrClient.killJobs(activeIDs);
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
    TreeSet<Integer> indices = req.getIndices();

    TaskTimer timer = new TaskTimer("MasterMgr.removeFiles(): " + nodeID);

    /* pre-op tests */
    RemoveFilesExtFactory factory = new RemoveFilesExtFactory(nodeID, indices);
    try {
      performExtensionTests(timer, factory);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();	
      
      if(!pAdminPrivileges.isQueueManaged(req, nodeID))
	throw new PipelineException
	  ("Only a user with Queue Manager privileges may remove files associated with " +
	   "nodes in working areas owned by another user!");

      TreeSet<Long> activeIDs = new TreeSet<Long>();
      TreeSet<FileSeq> fseqs = new TreeSet<FileSeq>();
      
      timer.aquire();
      ReentrantReadWriteLock lock = getWorkingLock(nodeID);
      lock.readLock().lock();
      try {
	timer.resume();
	
	WorkingBundle bundle = getWorkingBundle(nodeID);
	NodeMod mod = bundle.getVersion();
	if(mod.isFrozen()) 
	  throw new PipelineException
	    ("The files associated with frozen node (" + nodeID + ") cannot be removed!");
	
	ArrayList<Long> jobIDs = new ArrayList<Long>();
	ArrayList<JobState> jobStates = new ArrayList<JobState>();
	pQueueMgrClient.getJobStates(nodeID, mod.getTimeStamp(), mod.getPrimarySequence(),
				     jobIDs, jobStates);
	
	if(indices == null) {
	  int wk = 0;
	  for(JobState state : jobStates) {
	    Long jobID = jobIDs.get(wk);
	    if((state != null) && (jobID != null)) {
	      switch(state) {
	      case Queued:
	      case Preempted:
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
	      case Preempted:
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
      finally {
	lock.readLock().unlock();
      }    
      if(fseqs == null)
	throw new IllegalStateException(); 
      
      if(!activeIDs.isEmpty()) 
	pQueueMgrClient.killJobs(activeIDs);

      {
	FileMgrClient fclient = getFileMgrClient();
	try {
	  fclient.removeAll(nodeID, fseqs);
	}
	finally {
	    freeFileMgrClient(fclient);
	}
      }
      
      /* post-op tasks */ 
      startExtensionTasks(timer, factory);

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
    File backupFile = req.getBackupFile();

    TaskTimer timer = new TaskTimer("MasterMgr.backupDatabase: " + backupFile);

    /* pre-op tests */
    BackupDatabaseExtFactory factory = new BackupDatabaseExtFactory(backupFile);
    try {
      performExtensionTests(timer, factory);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }

    timer.aquire();
    pDatabaseLock.writeLock().lock();
    try {
      timer.resume();	

      if(!pAdminPrivileges.isMasterAdmin(req))
	throw new PipelineException
	  ("Only a user with Master Admin privileges may backup the database!"); 

      /* write cached downstream links */ 
      writeAllDownstreamLinks();
      
      /* create the backup */ 
      {
	ArrayList<String> args = new ArrayList<String>();
	args.add("-zcvf");
	args.add(backupFile.toString());
	args.add("downstream"); 
	args.add("etc"); 
	args.add("repository"); 
	args.add("toolsets"); 
	args.add("working"); 
	
	Map<String,String> env = System.getenv();

	SubProcessLight proc = 
	  new SubProcessLight("BackupDatabase", "tar", args, env, pNodeDir);
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

      /* post-op tasks */ 
      startExtensionTasks(timer, factory);

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
      DoubleMap<String,String,TreeSet<String>> matches = null;
      try {
	Pattern pat = null;
	if(pattern != null) 
	  pat = Pattern.compile(pattern);
	
	matches = pNodeTree.getMatchingCheckedInNodes(pat);
      }
      catch(PatternSyntaxException ex) {
	return new FailureRsp
	  (timer, "Illegal Node Name Pattern:\n\n" + ex.getMessage());
      }
      
      /* lock online/offline status */ 
      timer.aquire();
      List<ReentrantReadWriteLock> onOffLocks = onlineOfflineReadLock(matches.keySet());
      try {
	timer.resume();	

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
		stamps.put(vid, checkedIn.get(vid).getVersion().getTimeStamp());
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
		
		/* get the timestamp of the latest archive containing the 
		   checked-in version */ 
		Date archived = null;
		if(lastArchive != null) {
		  timer.aquire();
		  synchronized(pArchivedOn) {
		    timer.resume();
		    archived = pArchivedOn.get(lastArchive);
		  }
		}
		
		Date checkedIn = stamps.get(vid);
		ArchiveInfo info = 
		  new ArchiveInfo(name, vid, checkedIn, archived, numArchives);
		archiveInfo.add(info);
	      }
	    }
	  }
	}
	
	return new MiscArchiveQueryRsp(timer, archiveInfo);
      }
      finally {
	onlineOfflineReadUnlock(onOffLocks);
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

      /* the checked-in node versions */ 
      TreeMap<String,TreeSet<VersionID>> versions = req.getVersions();

      /* get the file sequences for the given checked-in versions */ 
      TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>> fseqs = 
	new TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>>();
      {
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
	      vfseqs.put(vid, checkedIn.get(vid).getVersion().getSequences());
	  }
	  finally {
	    lock.readLock().unlock();
	  }
	}
      } 

      /* lock online/offline status */ 
      timer.aquire();
      List<ReentrantReadWriteLock> onOffLocks = onlineOfflineReadLock(versions.keySet());
      try {
	timer.resume();	

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
      finally {
	onlineOfflineReadUnlock(onOffLocks);
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
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();	

      if(!pAdminPrivileges.isMasterAdmin(req))
	throw new PipelineException
	  ("Only a user with Master Admin privileges may create archives of checked-in " +
	   "versions!"); 

      /* the archiver plugin to use */ 
      BaseArchiver archiver = req.getArchiver();

      /* the checked-in node versions */ 
      TreeMap<String,TreeSet<VersionID>> versions = req.getVersions();
      
      /* lock online/offline status of the nodes to be archived */ 
      timer.aquire();
      List<ReentrantReadWriteLock> onOffLocks = onlineOfflineReadLock(versions.keySet());
      try {
	timer.resume();	

	/* validate the file sequences to be archived */ 
	TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>> fseqs = 
	  new TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>>();
	TreeMap<String,TreeMap<VersionID,Long>> sizes = null;
	{
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

	    timer.aquire();
	    ReentrantReadWriteLock lock = getCheckedInLock(name);
	    lock.readLock().lock(); 
	    try {
	      timer.resume();

	      TreeMap<VersionID,CheckedInBundle> checkedIn = getCheckedInBundles(name);

	      synchronized(pOfflined) {
		TreeSet<VersionID> offline = pOfflined.get(name);
		TreeMap<VersionID,Long> vsizes = sizes.get(name);
		for(VersionID vid : versions.get(name)) {
		  if((offline != null) && offline.contains(vid)) 
		    throw new PipelineException 
		      ("The checked-in version (" + vid + ") of node (" + name + ") " + 
		       "cannot be archived because it is currently offline!");
		  
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
	      
		  fvsns.put(vid, bundle.getVersion().getSequences());
		}
	      }
	    }
	    finally {
	      lock.readLock().unlock();
	    }
	    
	    if(total > archiver.getCapacity()) 
	      throw new PipelineException
		("The total size of the files (" + total + " bytes) associated with the " +
		 "checked-in versions to be archived exceeded the capacity of the " + 
		 "archiver (" + archiver.getCapacity() + " bytes)!");
	  }
	}
      	
	/* get the toolset environment */ 
	String tname = req.getToolset();
	if(tname == null) {
	  synchronized(pDefaultToolsetLock) {
	    timer.resume();	
	    
	    if(pDefaultToolset != null) 
	      tname = pDefaultToolset;
	    else 
	      throw new PipelineException
		("No toolset was specified and no default toolset is defined!");
	  }
	}
	
	TreeMap<String,String> env = 
	  getToolsetEnvironment(null, null, tname, OsType.Unix, timer);
	
	/* the archive name and time stamp */ 
	Date stamp = new Date();
	String archiveName = (req.getPrefix() + "-" + stamp.getTime());
	synchronized(pArchivedOn) {
	  if(pArchivedOn.containsKey(archiveName)) 
	    throw new PipelineException 
	      ("Somehow an archive named (" + archiveName + ") already exists!");
	}
	
	/* pre-op tests */
	ArchiveExtFactory factory = 
	  new ArchiveExtFactory(archiveName, versions, archiver, tname);
	try {
	  performExtensionTests(timer, factory);
	}
	catch(PipelineException ex) {
	  return new FailureRsp(timer, ex.getMessage());
	}

	/* create the archive volume by runing the archiver plugin and save any 
	   STDOUT output */
	{
	  String output = null;
	  {
	    FileMgrClient fclient = getFileMgrClient();
	    try {
	      output = fclient.archive(archiveName, fseqs, archiver, env);
	    }
	    finally {
	      freeFileMgrClient(fclient);
	    }
	  }
	  
	  if(output != null) {
	    File file = new File(pNodeDir, 
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
	  ArchiveVolume vol = 
	    new ArchiveVolume(archiveName, stamp, fseqs, sizes, archiver, tname);
	  writeArchive(vol);
	  
	  synchronized(pArchivedOn) {
	    pArchivedOn.put(archiveName, stamp);
	  }
	}
	
	/* update the cached archive named for each checked-in version */
	synchronized(pArchivedIn) {
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
	}
	
	/* post-op tasks */ 
	startExtensionTasks(timer, factory);

	return new MiscArchiveRsp(timer, archiveName);
      }
      finally {
	onlineOfflineReadUnlock(onOffLocks);
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
      DoubleMap<String,String,TreeSet<String>> matches = null;
      try {
	Pattern pat = null;
	if(pattern != null) 
	  pat = Pattern.compile(pattern);
	
	matches = pNodeTree.getMatchingCheckedInNodes(pat);
      }
      catch(PatternSyntaxException ex) {
	throw new PipelineException 
	  ("Illegal Node Name Pattern:\n\n" + ex.getMessage());
      }
      
      /* lock online/offline status */ 
      timer.aquire();
      List<ReentrantReadWriteLock> onOffLocks = onlineOfflineReadLock(matches.keySet());
      try {
	timer.resume();	

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
		      NodeMod mod = bundle.getVersion();
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
      finally {
	onlineOfflineReadUnlock(onOffLocks);
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

      String name = req.getName();

      /* lock online/offline status of the node */ 
      timer.aquire();
      ReentrantReadWriteLock onOffLock = getOnlineOfflineLock(name);
      onOffLock.readLock().lock();
      try {
	timer.resume();	

	/* the currently offline revision numbers */ 
	TreeSet<VersionID> offlined = new TreeSet<VersionID>();
	timer.aquire();
	synchronized(pOfflined) {
	  timer.resume();
	  
	  if(pOfflined.get(name) != null)
	    offlined.addAll(pOfflined.get(name));
	}
	
	return new NodeGetOfflineVersionIDsRsp(timer, offlined);
      }
      finally {
	onOffLock.readLock().unlock();
      }      
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

      TreeMap<String,TreeSet<VersionID>> versions = req.getVersions();

      /* lock online/offline status of the nodes */ 
      timer.aquire();
      List<ReentrantReadWriteLock> onOffLocks = onlineOfflineReadLock(versions.keySet());
      try {
	timer.resume();	

	/* determine which files contribute the to offlined size */ 
	TreeMap<String,TreeMap<VersionID,TreeSet<File>>> contribute = 
	  new TreeMap<String,TreeMap<VersionID,TreeSet<File>>>();
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
		NodeVersion vsn = checkedIn.get(vid).getVersion();
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
      finally {
	onlineOfflineReadUnlock(onOffLocks);
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
    TreeMap<String,TreeSet<VersionID>> versions = req.getVersions();
      
    TaskTimer timer = new TaskTimer("MasterMgr.offline()");
    
    /* pre-op tests */
    OfflineExtFactory factory = new OfflineExtFactory(versions);
    try {
      performExtensionTests(timer, factory);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }

    timer.aquire();
    pDatabaseLock.readLock().lock();
    boolean cacheModified = false;
    try {
      timer.resume();	

      if(!pAdminPrivileges.isMasterAdmin(req))
	throw new PipelineException
	  ("Only a user with Master Admin privileges may offline checked-in versions!"); 
  
      /* write lock online/offline status */ 
      timer.aquire();
      List<ReentrantReadWriteLock> onOffLocks = onlineOfflineWriteLock(versions.keySet());
      try {
	timer.resume();	

	/* process each node */ 
	for(String name : versions.keySet()) {	
	  timer.aquire();
	  ArrayList<ReentrantReadWriteLock> workingLocks = 
	    new ArrayList<ReentrantReadWriteLock>();
	  {
	    TreeMap<String,TreeSet<String>> views = pNodeTree.getViewsContaining(name);
	    for(String author : views.keySet()) {
	      for(String view : views.get(author)) {
		NodeID nodeID = new NodeID(author, view, name);
		ReentrantReadWriteLock workingLock = getWorkingLock(nodeID);
		workingLock.readLock().lock();
		workingLocks.add(workingLock);
	      }
	    }
	  }

	  ReentrantReadWriteLock checkedInLock = getCheckedInLock(name);
	  checkedInLock.readLock().lock();  

	  try {
	    timer.resume();	
	    TreeMap<VersionID,CheckedInBundle> checkedIn = getCheckedInBundles(name);
	    
	    ArrayList<VersionID> vids = new ArrayList<VersionID>(checkedIn.keySet());
	    TreeMap<File,Boolean[]> novelty = noveltyByFile(checkedIn);

	    /* process the to be offlined versions */ 
	    TreeSet<VersionID> toBeOfflined = versions.get(name);
	    for(VersionID vid : toBeOfflined) { 

	      /* whether the version is currently online */
	      boolean isOnline = false;
	      synchronized(pOfflined) {
		TreeSet<VersionID> offlined = pOfflined.get(name);
		isOnline = ((offlined == null) || !offlined.contains(vid));
	      }

	      /* only process online versions */
	      if(isOnline) {
		CheckedInBundle bundle = checkedIn.get(vid);
		if(bundle == null) 
		  throw new PipelineException 
		    ("No checked-in version (" + vid + ") of node (" + name + ") exists!");
		NodeVersion vsn = checkedIn.get(vid).getVersion();
		int vidx = vids.indexOf(vid);
	    
		/* make sure at lease one archive volume contains the version */ 
		{
		  boolean hasBeenArchived = false;
		  synchronized(pArchivedIn) {
		    TreeMap<VersionID,TreeSet<String>> aversions = pArchivedIn.get(name);
		    if(aversions != null) {
		      TreeSet<String> archives = aversions.get(vid);
		      if((archives != null) && !archives.isEmpty()) 
			hasBeenArchived = true;
		    }
		  }

		  if(!hasBeenArchived) 
		    throw new PipelineException
		      ("The checked-in version (" + vid + ") of node (" + name + ") " + 
		       "cannot be offlined until it has been archived at least once!");
		}	    

		/* make sure it is not being referenced by an existing working version */ 
		{
		  TreeMap<String,TreeSet<String>> views = pNodeTree.getViewsContaining(name);
		  for(String author : views.keySet()) {
		    for(String view : views.get(author)) {
		      NodeID nodeID = new NodeID(author, view, name);
		      NodeMod mod = getWorkingBundle(nodeID).getVersion();
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
			  
			  synchronized(pOfflined) {
			    TreeSet<VersionID> offlined = pOfflined.get(name);
			    if((offlined != null) && offlined.contains(nvid)) {
			      selected = true;
			      break;
			    }
			    else if(isNovel[vk]) 
			      break;
			  }
			}
		      }

		      /* determine which versions need relocation */ 
		      if(selected) {
			int vk;
			for(vk=vidx+1; vk<isNovel.length; vk++) {
			  VersionID nvid = vids.get(vk);
		      
			  if((isNovel[vk] == null) || isNovel[vk]) 
			    break;
			  else {
			    synchronized(pOfflined) {
			      TreeSet<VersionID> offlined = pOfflined.get(name);
			      if((offlined == null) || !offlined.contains(nvid)) {
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
		synchronized(pOfflined) {
		  TreeSet<VersionID> offlined = pOfflined.get(name);
		  if(offlined == null) {
		    offlined = new TreeSet<VersionID>();
		    pOfflined.put(name, offlined);
		  }

		  offlined.add(vid);
		}
	      }
	    }
	  }
	  finally {
	    checkedInLock.readLock().unlock();  

	    Collections.reverse(workingLocks);
	    for(ReentrantReadWriteLock workingLock : workingLocks) 
	      workingLock.readLock().unlock();
	  }
	}

	cacheModified = true;

	/* post-op tasks */ 
	startExtensionTasks(timer, factory);

	return new SuccessRsp(timer);
      }
      finally {
	onlineOfflineWriteUnlock(onOffLocks);
      }  
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }  
    finally {
      pDatabaseLock.readLock().unlock();

      /* keep the offlined cache file up-to-date */ 
      if(cacheModified && pPreserveOfflinedCache) {
	try {
	  writeOfflined();
	}
	catch(PipelineException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Ops, LogMgr.Level.Warning,
	     ex.getMessage());
	  LogMgr.getInstance().flush();
	  
	  removeOfflinedCache();
	}
      }
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
      NodeVersion vsn = checkedIn.get(vid).getVersion();
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
    TreeMap<String,TreeSet<VersionID>> versions = req.getVersions();

    TaskTimer timer = new TaskTimer("MasterMgr.requestRestore()");

    /* pre-op tests */
    RequestRestoreExtFactory factory = new RequestRestoreExtFactory(versions);
    try {
      performExtensionTests(timer, factory);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }    

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      timer.resume();

      /* filter any versions which are not offline */ 
      TreeMap<String,TreeSet<VersionID>> vsns = new TreeMap<String,TreeSet<VersionID>>();
      {

	timer.aquire();
	List<ReentrantReadWriteLock> onOffLocks = onlineOfflineReadLock(versions.keySet());
	try {
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
	finally {
	  onlineOfflineReadUnlock(onOffLocks);
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
	
	/* post-op tasks */ 
	startExtensionTasks(timer, factory);

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
    TreeMap<String,TreeSet<VersionID>> versions = req.getVersions();
	
    TaskTimer timer = new TaskTimer("MasterMgr.denyRestore()");

    /* pre-op tests */
    DenyRestoreExtFactory factory = new DenyRestoreExtFactory(versions);
    try {
      performExtensionTests(timer, factory);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }    

    if(!pAdminPrivileges.isMasterAdmin(req))
      return new FailureRsp
	(timer, "Only a user with Master Admin privileges may deny restore requests!");

    timer.aquire();
    pDatabaseLock.readLock().lock();
    try {
      synchronized(pRestoreReqs) {
	timer.resume();

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
	
	/* post-op tasks */ 
	startExtensionTasks(timer, factory);

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
	      if((rr.getResolvedStamp().getTime()+pRestoreCleanupInterval.get()) < now) {
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

    if(!pAdminPrivileges.isMasterAdmin(req))
      return new FailureRsp
	(timer, 
	 "Only a user with Master Admin privileges may restore checked-in versions!"); 

    timer.aquire();
    pDatabaseLock.readLock().lock();
    boolean cacheModified = false;
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

      /* write lock online/offline status */ 
      timer.aquire();
      List<ReentrantReadWriteLock> onOffLocks = onlineOfflineWriteLock(versions.keySet());
      try {
	timer.resume();	

	/* validate the file sequences to be restored */ 
	TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>> fseqs = 
	  new TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>>();
	long total = 0L;
	for(String name : versions.keySet()) {
	  
	  timer.aquire();
	  ReentrantReadWriteLock checkedInLock = getCheckedInLock(name);
	  checkedInLock.readLock().lock();  
	  try {
	    timer.resume();	
	    TreeMap<VersionID,CheckedInBundle> checkedIn = getCheckedInBundles(name);

	  
	    for(VersionID vid : versions.get(name)) {
	      if(!vol.contains(name, vid)) 
		throw new PipelineException
		  ("The checked-in version (" + vid + ") of node (" + name + ") cannot be " +
		   "restored because it is not contained in the archive volume " + 
		   "(" + archiveName + ")!");
	      total += vol.getSize(name, vid);
	      
	      synchronized(pOfflined) {
		TreeSet<VersionID> offlined = pOfflined.get(name);
		if((offlined == null) || !offlined.contains(vid)) 
		  throw new PipelineException 
		    ("The checked-in version (" + vid + ") of node (" + name + ") cannot " + 
		     "be restored because it is currently online!");
	      }
	  
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
	      
	      fvsns.put(vid, bundle.getVersion().getSequences());
	    }
	  }
	  finally {
	    checkedInLock.readLock().unlock();  
	  }
	}

	/* get the toolset environment */ 
	String tname = req.getToolset();
	if(tname == null) 
	  tname = vol.getToolset();
	
	TreeMap<String,String> env = 
	  getToolsetEnvironment(null, null, tname, OsType.Unix, timer);
	
	/* pre-op tests */
	RestoreExtFactory factory = 
	  new RestoreExtFactory(archiveName, versions, archiver, tname);
	try {
	  performExtensionTests(timer, factory);
	}
	catch(PipelineException ex) {
	  return new FailureRsp(timer, ex.getMessage());
	}

	/* extract the versions from the archive volume by running the archiver plugin and 
	   save any STDOUT output */
	{
	  String output = null;
	  {
	    FileMgrClient fclient = getFileMgrClient();
	    try {
	      output = fclient.extract(archiveName, stamp, fseqs, archiver, env, total);
	    }
	    finally {
	      freeFileMgrClient(fclient);
	    }
	  }
	  
	  Date now = new Date();
	  File file = new File(pNodeDir, "archives/output/restore/" + 
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
	 
	  synchronized(pRestoredOn) {
	    TreeSet<Date> stamps = pRestoredOn.get(archiveName);
	    if(stamps == null) {
	      stamps = new TreeSet<Date>();
	      pRestoredOn.put(archiveName, stamps);
	    }
	    stamps.add(now);
	  }
	}
      
	/* move the extracted files into the respository */ 
	for(String name : versions.keySet()) {

	  timer.aquire();
	  ReentrantReadWriteLock checkedInLock = getCheckedInLock(name);
	  checkedInLock.readLock().lock();  
	  try {
	    timer.resume();	
	    TreeMap<VersionID,CheckedInBundle> checkedIn = getCheckedInBundles(name);

	    ArrayList<VersionID> vids = new ArrayList<VersionID>(checkedIn.keySet());
	    TreeMap<File,Boolean[]> novelty = noveltyByFile(checkedIn);
	    
	    for(VersionID vid : versions.get(name)) {
	      CheckedInBundle bundle = checkedIn.get(vid);
	      int vidx = vids.indexOf(vid);
	      
	      /* determine what files and/or links need to be modified */ 
	      TreeMap<File,TreeSet<VersionID>> symlinks = 
		new TreeMap<File,TreeSet<VersionID>>();
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
		      
		      synchronized(pOfflined) {
			TreeSet<VersionID> offlined = pOfflined.get(name);
			if((offlined == null) || !offlined.contains(nvid)) 
			  tvid = nvid;
		      }

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
		      else {
			synchronized(pOfflined) {
			  TreeSet<VersionID> offlined = pOfflined.get(name);
			  if((offlined == null) || !offlined.contains(nvid)) 
			    svids.add(nvid);
			}
		      }
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
	      synchronized(pOfflined) {
		TreeSet<VersionID> offlined = pOfflined.get(name);
		offlined.remove(vid);
		if(offlined.isEmpty()) {
		  offlined = null;
		  pOfflined.remove(name);
		}
	      }
	
	      /* update the restore requests */ 
	      synchronized(pRestoreReqs) {
		TreeMap<VersionID,RestoreRequest> reqs = pRestoreReqs.get(name);
		if(reqs != null) {
		  RestoreRequest rr = reqs.get(vid);
		  if(rr != null) 
		    rr.restored(archiveName);
		}
	      }
	    }
	  }
	  finally {
	    checkedInLock.readLock().unlock();  
	  }	  
	}

	cacheModified = true;

	/* post-op tasks */ 
	startExtensionTasks(timer, factory);

	return new SuccessRsp(timer);
      }
      finally {
	onlineOfflineWriteUnlock(onOffLocks);
      }
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

      pDatabaseLock.readLock().unlock();

      /* keep the offlined cache file up-to-date */ 
      if(cacheModified && pPreserveOfflinedCache) {
	try {
	  writeOfflined();
	}
	catch(PipelineException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Ops, LogMgr.Level.Warning,
	     ex.getMessage());
	  LogMgr.getInstance().flush();
	  
	  removeOfflinedCache();
	}
      }
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
      File file = new File(pNodeDir, 
			   "archives/output/archive/" + aname);
      String output = null;
      if(file.length() > 0) {
	FileReader in = new FileReader(file);
	  
	StringBuilder buf = new StringBuilder();
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
      File file = new File(pNodeDir, 
			   "archives/output/restore/" + aname + "-" + stamp.getTime());
      String output = null;
      if(file.length() > 0) {
	FileReader in = new FileReader(file);
	  
	StringBuilder buf = new StringBuilder();
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
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a default saved panel layout file. <P> 
   * 
   * The layout is copied from an initial default layout provided with the Pipeline release.
   * This layout is provided as a helpful starting point for new users when creating custom
   * layouts.  The created panels will be set to view the working area specified by the 
   * <CODE>author</CODE> and <CODE>view</CODE> parameters. 
   * 
   * @param req
   *   The request.
   * 
   * @return 
   *   <CODE>MiscCreateInitialPanelLayoutRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to load the layout.
   */ 
  public Object
  createInitialPanelLayout
  (
   MiscCreateInitialPanelLayoutReq req
  ) 
    throws PipelineException
  {
    TaskTimer timer = new TaskTimer();
    
    try {
      StringBuilder buf = new StringBuilder();
      try {
	File file = new File(pNodeDir, "etc/initial-panel-layout");
	FileReader in = new FileReader(file);
	char cs[] = new char[1024];
	while(true) {
	  int cnt = in.read(cs);
	  if(cnt == -1) 
	    break;
	  
	  buf.append(cs, 0, cnt);
	}
	in.close();
      }
      catch(IOException ex) {
	throw new PipelineException
	("I/O ERROR: \n" + 
	 "  While attempting to read the initial panel layout...\n" +
	 "    " + ex.getMessage());
      }

      String contents = 
	buf.toString().replaceFirst("@CALLING_AUTHOR@", 
				    req.getAuthor()).replaceFirst("@CALLING_VIEW@", 
								  req.getView());

      return new MiscCreateInitialPanelLayoutRsp(timer, contents);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
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
      StringBuilder buf = new StringBuilder();
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
      for(LinkMod link : bundle.getVersion().getSources()) 
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

    StringBuilder buf = new StringBuilder();
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
      performUpstreamNodeOp(nodeOp, nodeID, false, new LinkedList<String>(), table, timer);

      root = table.get(nodeID.getName());
      if(root == null)
	throw new IllegalStateException(); 

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
   * @param isTargetLinkLocked
   *   Whether a locked link from a checked-in target node to this node exists.
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
   boolean isTargetLinkLocked, 
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
	  latest = new NodeVersion(bundle.getVersion());

	  versionIDs.addAll(checkedIn.keySet());
	}

	if(working != null) {
	  work = new NodeMod(working.getVersion());

	  VersionID workID = work.getWorkingID();
	  if(workID != null) {
	    if(checkedIn == null)
	      throw new IllegalStateException(); 
	    CheckedInBundle bundle = checkedIn.get(workID);
	    if(bundle == null) 
	      throw new PipelineException
		("Somehow the checked-in version (" + workID + ") of node (" + name + 
		 ") used as the basis for working version (" + nodeID + ") did " + 
		 "not exist!");
	    base = new NodeVersion(bundle.getVersion());

	    if(base.getVersionID().equals(latest.getVersionID())) 
	      versionState = VersionState.Identical;
	    else 
	      versionState = VersionState.NeedsCheckOut;
	  }
	  else {
	    if(checkedIn != null)
	      throw new IllegalStateException(); 
	    versionState = VersionState.Pending;
	  }
	}
	else {
	  if(checkedIn == null)
	    throw new IllegalStateException(); 
	  versionState = VersionState.CheckedIn;
	}
      }
      if(versionState == null)
	throw new IllegalStateException(); 

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
	    throw new IllegalStateException(); 
	  }
	}
      }	

      /* add the status stub */ 
      NodeStatus status = new NodeStatus(nodeID);
      table.put(name, status);

      /* determine if the current working version is locked or frozen */ 
      boolean workIsLocked = ((work != null) && work.isLocked());
      boolean workIsFrozen = ((work != null) && work.isFrozen());
    
      /* if not locked, process the upstream nodes */ 
      Date missingStamp = new Date();
      switch(versionState) {
      case CheckedIn:
	if(!isTargetLinkLocked) {
	  for(LinkVersion link : latest.getSources()) {
	    NodeID lnodeID = new NodeID(nodeID, link.getName());
	    
	    performUpstreamNodeOp(nodeOp, lnodeID, link.isLocked(), branch, table, timer);
	    NodeStatus lstatus = table.get(link.getName());
	    
	    status.addSource(lstatus);
	    lstatus.addTarget(status);
	  }
	}
	break;
	
      default:
	if(!workIsLocked) {
	  for(LinkMod link : work.getSources()) {
	    NodeID lnodeID = new NodeID(nodeID, link.getName());
	    
	    performUpstreamNodeOp(nodeOp, lnodeID, false, branch, table, timer);
	    NodeStatus lstatus = table.get(link.getName());
	    
	    status.addSource(lstatus);
	    lstatus.addTarget(status);
	  }
	}
      }

      /* compute link state, 
	 whether the source node can be ignored when propogating staleness and 
	 whether the locked state of any of the common source nodes have changed */ 
      LinkState linkState = null;
      TreeSet<String> nonIgnoredSources = new TreeSet<String>();
      boolean modifiedLocks = false;
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
	  boolean workEqBase   = true;
	  boolean workEqLatest = true;
	  if(!workIsLocked) {
	    for(LinkMod link : work.getSources()) {
	      String lname = link.getName(); 

	      LinkVersion blink = base.getSource(lname); 
	      LinkVersion llink = latest.getSource(lname); 

	      NodeDetails sdetails = table.get(lname).getDetails();
	      VersionID svid = sdetails.getWorkingVersion().getWorkingID();
	   
	      if((blink == null) || 
		 !link.equals(blink) || 
		 !blink.getVersionID().equals(svid)) {
		workEqBase = false;
		nonIgnoredSources.add(lname);
	      }
	     
	      if((llink == null) || 
		 !link.equals(llink) || 
		 !llink.getVersionID().equals(svid)) {
		workEqLatest = false;
	      }
	     
 	      if(sdetails.getOverallNodeState() == OverallNodeState.ModifiedLocks) 
 		nonIgnoredSources.remove(lname);
	      
	      if(((blink != null) && 
		  (sdetails.getWorkingVersion().isLocked() != blink.isLocked())) ||
		 (sdetails.getOverallNodeState() == OverallNodeState.ModifiedLocks))
		modifiedLocks = true;
	    }
	  }

	  if(workEqLatest) {
	    linkState = LinkState.Identical;    
	  }
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
	  /* get the per-file states and timestamps */
	  TreeMap<FileSeq, Date[]> stamps = new TreeMap<FileSeq, Date[]>();
	  
	  /* query the file manager */
	  {	     
	    FileMgrClient fclient = getFileMgrClient();
	    try {
	      VersionID vid = null;
	      if(latest != null) 
		vid = latest.getVersionID();

	      Date critical = null;
	      if(work != null)
		critical = work.getLastCTimeUpdate(); 

	      fclient.states(nodeID, work, versionState, workIsFrozen, vid, critical, 
			     fileStates, stamps);

	      /* if frozen, all the files are just links so use the working time stamp */ 
	      if(workIsFrozen) {
		for(FileSeq fseq : work.getSequences()) {
		  Date ts[] = new Date[fseq.numFrames()];

		  int wk;
		  for(wk=0; wk<ts.length; wk++) 
		    ts[wk] = work.getTimeStamp();

		  stamps.put(fseq, ts);
		}
	      }
	    }
	    finally {
	      freeFileMgrClient(fclient);
	    }
	  }

	  /* get the newest/oldest of the timestamp for each file sequence index */ 
	  for(FileSeq fseq : stamps.keySet()) {
	    Date[] ts = stamps.get(fseq);
	    
	    if(newestStamps == null) 
	      newestStamps = new Date[ts.length];
	    
	    if(oldestStamps == null) 
	      oldestStamps = new Date[ts.length];
	    
	    int wk;
	    for(wk=0; wk<ts.length; wk++) {
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
	  else if(anyConflicted || (anyNeedsCheckOut && (anyModified || modifiedLocks)))
	    overallNodeState = OverallNodeState.Conflicted;
	  else if(anyModified) 
	    overallNodeState = OverallNodeState.Modified;
	  else if(anyNeedsCheckOut) {
	    if(!workIsLocked) {
	      for(LinkMod link : work.getSources()) {
		NodeDetails ldetails = table.get(link.getName()).getDetails();
		VersionID lvid = ldetails.getWorkingVersion().getWorkingID();
		
		switch(ldetails.getOverallNodeState()) {
		case ModifiedLocks:
		case ModifiedLinks:
		case Modified:		  
		case Conflicted:	
		case Missing:
		case MissingNewer:
		  overallNodeState = OverallNodeState.Conflicted;
		}
	      }
	    }

	    if(overallNodeState == null)
	      overallNodeState = OverallNodeState.NeedsCheckOut;
	  }
	  else {
	    if((versionState != VersionState.Identical) ||
	       (propertyState != PropertyState.Identical) ||
	       (linkState != LinkState.Identical) ||
	       anyNeedsCheckOutFs || anyModifiedFs || anyConflictedFs)
	      throw new IllegalStateException(); 

	    /* the work and base version have the same set of links 
		 because (linkState == Identical) */
	    if(!workIsLocked) {
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
		  if(!link.getVersionID().equals(lvid)) {
		    overallNodeState = OverallNodeState.ModifiedLinks;
		    LogMgr.getInstance().log
		      (LogMgr.Kind.Ops, LogMgr.Level.Warning, 
		       "This test should never be reached since LinkState should have " + 
		       "been Modified if this is true and the (anyModified) test " + 
		       "above should have been selected instead of this section!");
		  }
		}
	      }
	    }

	    if(overallNodeState == null) {
	      if(modifiedLocks) 
		overallNodeState = OverallNodeState.ModifiedLocks;
	      else
		overallNodeState = OverallNodeState.Identical;
	    }
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
	if(workIsLocked || workIsFrozen) {
	  int numFrames = work.getPrimarySequence().numFrames();
	  jobIDs      = new Long[numFrames];
	  queueStates = new QueueState[numFrames];

	  int wk;
	  for(wk=0; wk<queueStates.length; wk++) 
	    queueStates[wk] = QueueState.Finished;
	}
	else {
	  int numFrames = work.getPrimarySequence().numFrames();
	  jobIDs      = new Long[numFrames];
	  queueStates = new QueueState[numFrames];

	  JobState js[] = new JobState[numFrames];
	  {
	    ArrayList<Long> jIDs          = new ArrayList<Long>();
	    ArrayList<JobState> jobStates = new ArrayList<JobState>();

	    pQueueMgrClient.getJobStates(nodeID, work.getTimeStamp(), 
					 work.getPrimarySequence(), jIDs, jobStates);

	    if(jobIDs.length != jIDs.size())
	      throw new IllegalStateException(); 
	    jobIDs = (Long[]) jIDs.toArray(jobIDs);

	    if(js.length != jobStates.size())
	      throw new IllegalStateException(); 
	    js = (JobState[]) jobStates.toArray(js);
	  }
	  
	  int wk;
	  for(wk=0; wk<queueStates.length; wk++) {
	    /* there is no regeneration action or it is disabled, 
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
		case Preempted:
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
		case ModifiedLocks: 
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
			
			switch(ldetails.getOverallNodeState()) {
			case ModifiedLocks: 
			  break;
			  
			default:
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
				if(((idx >= 0) && (idx < lqs.length)) &&
				   ((lqs[idx] != QueueState.Finished) || 
				    lanyMissing[idx] || 
				    ((!lignored[idx] || nonIgnored) && (lstamps[idx] != null) &&
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
				     ((!lignored[fk] || nonIgnored) && (lstamps[fk] != null) &&
				      (oldestStamps[wk].compareTo(lstamps[fk]) < 0))) {
				    queueStates[wk] = QueueState.Stale;
				    break;
				  }
				}
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
	if(!workIsLocked) {
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
	if(workIsLocked) {
	  int wk;
	  for(wk=0; wk<queueStates.length; wk++) {
	    fileStamps[wk] = newestStamps[wk];
	    ignoreStamps[wk] = true;
	  }
	}
	else {
	  int wk;
	  for(wk=0; wk<queueStates.length; wk++) {
	    if(anyMissing[wk] || (newestStamps[wk] == null)) 
	      fileStamps[wk] = missingStamp;
	    else 
	      fileStamps[wk] = newestStamps[wk];

	    Date critical = work.getLastCriticalModification();
	    if(critical.compareTo(fileStamps[wk]) > 0) 
	      fileStamps[wk] = critical;

	    switch(overallNodeState) {
	    case Identical:
	    case NeedsCheckOut:
	    case ModifiedLocks:
	      ignoreStamps[wk] = true;
	      break;
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
    ReentrantReadWriteLock lock = getDownstreamLock(name);
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
      DownstreamLinks dsl = getDownstreamLinks(name); 
      if(dsl == null)
	throw new IllegalStateException(); 

      TreeSet<String> wlinks = dsl.getWorking(nodeID);
      if(wlinks != null) {
	for(String lname : wlinks) {
	  getDownstreamNodeStatus(root, new NodeID(nodeID, lname), null, 
				  branch, table, timer);

	  NodeStatus lstatus = table.get(lname);
	  if(lstatus == null)
	    throw new IllegalStateException(); 

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

	if(clinks != null) {
	  for(String lname : clinks.keySet()) {
	    VersionID lvid = clinks.get(lname);
	    
	    getDownstreamNodeStatus(root, new NodeID(nodeID, lname), lvid, 
				    branch, table, timer);
	    
	    NodeStatus lstatus = table.get(lname);
	    if(lstatus == null)
	      throw new IllegalStateException(); 
	    
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

  /**
   * Recursively find the names and primary file sequences of all nodes downstream of 
   * the given node which are currently checked-out into the given working area. <P> 
   * 
   * @param nodeID
   *   The unique working version identifier.
   * 
   * @param branch
   *   The names of the nodes from the root to this node.
   * 
   * @param fseqs
   *   The collected primary file sequences indexed by fully resolved names of the nodes.
   * 
   * @param timer
   *   The shared task timer for this operation.
   */ 
  private void 
  getDownstreamWorkingSeqs
  (
   NodeID nodeID, 
   LinkedList<String> branch, 
   TreeMap<String,FileSeq> fseqs,
   TaskTimer timer
  ) 
    throws PipelineException
  {
    String name = nodeID.getName();

    /* check for circularity */ 
    checkBranchForCircularity(name, branch);

    /* skip nodes which have already been processed */ 
    if(fseqs.containsKey(name)) 
      return;

    /* push the current node onto the end of the branch */ 
    branch.addLast(name);


    /* add the current node */ 
    boolean hasWorking = false;
    timer.aquire();
    ReentrantReadWriteLock workingLock = getWorkingLock(nodeID);
    workingLock.readLock().lock(); 
    try {
      timer.resume();
      
      WorkingBundle working = null;
      try {
	working = getWorkingBundle(nodeID);
      }
      catch(PipelineException ex) {
      }
      
      if(working != null) {
	hasWorking = true;
	FileSeq fseq = working.getVersion().getPrimarySequence();
	fseqs.put(name, fseq);
      }
    }
    finally {
      workingLock.readLock().unlock();
    }

    /* process downstream nodes */ 
    if(hasWorking) {
      timer.aquire();
      ReentrantReadWriteLock lock = getDownstreamLock(name);
      lock.readLock().lock();
      try {
	timer.resume();
	
	DownstreamLinks dsl = getDownstreamLinks(name); 
	if(dsl == null)
	  throw new IllegalStateException(); 
	
	TreeSet<String> wlinks = dsl.getWorking(nodeID);
	if(wlinks != null) {
	  for(String lname : wlinks) 
	    getDownstreamWorkingSeqs(new NodeID(nodeID, lname), branch, fseqs, timer);
	}
      }
      finally {
	lock.readLock().unlock();
      }
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
    if(pInternalFileMgr)
      return pFileMgrDirectClient;

    synchronized(pFileMgrNetClients) {
      if(pFileMgrNetClients.isEmpty()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Finest,
	   "Creating New File Manager Client.");
	LogMgr.getInstance().flush();

	return new FileMgrNetClient();
      }
      else {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Finest,
	   "Reusing File Manager Client: " + (pFileMgrNetClients.size()-1) + " inactive");
	LogMgr.getInstance().flush();

	return pFileMgrNetClients.pop();
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
    if(pInternalFileMgr)
      return;

    synchronized(pFileMgrNetClients) {
      pFileMgrNetClients.push((FileMgrNetClient) client);
      
      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Finest,
	 "Freed File Manager Client: " + pFileMgrNetClients.size() + " inactive");
      LogMgr.getInstance().flush();
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L O C K   H E L P E R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Lookup the online/offline lock for the node with the given name. 
   * 
   * @param name 
   *   The fully resolved node name
   */
  private ReentrantReadWriteLock
  getOnlineOfflineLock
  (
   String name
  ) 
  {
    synchronized(pOnlineOfflineLocks) {
      ReentrantReadWriteLock lock = pOnlineOfflineLocks.get(name);

      if(lock == null) { 
	lock = new ReentrantReadWriteLock();
	pOnlineOfflineLocks.put(name, lock);
      }

      return lock;
    }
  }

  /**
   * Aquire the read access to the online/offline locks of the given named nodes. <P> 
   * 
   * Insures that the locks are aquired in the lexical order of the node names.  The returned
   * list of locks is in reverse lexical order to that the locks will be unlocked in exactly
   * the opposite order.
   * 
   * @param names
   *   The fully resolved names of the nodes to lock.
   */ 
  private List<ReentrantReadWriteLock> 
  onlineOfflineReadLock
  (
   Collection<String> names
  ) 
  {
    TreeSet<String> sorted = new TreeSet<String>(names);
    ArrayList<ReentrantReadWriteLock> locks = new ArrayList<ReentrantReadWriteLock>();
    for(String name : sorted) {
      ReentrantReadWriteLock lock = getOnlineOfflineLock(name);
      lock.readLock().lock();
      locks.add(lock);
    }
    
    Collections.reverse(locks);

    return Collections.unmodifiableList(locks);
  }
  
  /**
   * Release the read access to the given online/offline locks.
   * 
   * @param locks
   *   The previously aquired read locks returned by the onlineOfflineReadLock() method.
   */ 
  private void 
  onlineOfflineReadUnlock
  (
   List<ReentrantReadWriteLock> locks
  ) 
  {
    for(ReentrantReadWriteLock lock : locks) 
      lock.readLock().unlock();
  }
  
  /**
   * Aquire the write access to the online/offline locks of the given named nodes. <P> 
   * 
   * Insures that the locks are aquired in the lexical order of the node names.  The returned
   * list of locks is in reverse lexical order to that the locks will be unlocked in exactly
   * the opposite order.
   * 
   * @param names
   *   The fully resolved names of the nodes to lock.
   */ 
  private List<ReentrantReadWriteLock> 
  onlineOfflineWriteLock
  (
   Collection<String> names
  ) 
  {
    TreeSet<String> sorted = new TreeSet<String>(names);
    ArrayList<ReentrantReadWriteLock> locks = new ArrayList<ReentrantReadWriteLock>();
    for(String name : sorted) {
      ReentrantReadWriteLock lock = getOnlineOfflineLock(name);
      lock.writeLock().lock();
      locks.add(lock);
    }
    
    Collections.reverse(locks);

    return Collections.unmodifiableList(locks);
  }
  
  /**
   * Release the write access to the given online/offline locks.
   * 
   * @param locks
   *   The previously aquired read locks returned by the onlineOfflineWriteLock() method.
   */ 
  private void 
  onlineOfflineWriteUnlock
  (
   List<ReentrantReadWriteLock> locks
  ) 
  {
    for(ReentrantReadWriteLock lock : locks) 
      lock.writeLock().unlock();
  }
  

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

    /* keep track of the change to the node version cache */ 
    for(VersionID vid : table.keySet()) 
      incrementCheckedInCounter(name, vid);

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
    
    /* keep track of the change to the node version cache */ 
    incrementWorkingCounter(id); 

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
  /*   N O D E   G A R B A G E   C O L L E C T O R                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Monitors the amount of memory held by the node version caches (both checked-in and
   * working) and the amount of free memory to determine when the size of the node cache
   * should be reduced.  When the cache needs to be reduced, the nodes with the oldest 
   * last accessed timestamps will be removed from the checked-in/working node bundle
   * tables.
   */ 
  public void 
  nodeGC() 
  {
    TaskTimer timer = new TaskTimer();

    /* estimate the amount of memory currently held in the node cache */ 
    long cacheMemory = pNodeCacheSize.get() * pAverageNodeSize.get();
    
    /* lookup the amount of memory currenting being used by the JVM */ 
    Runtime rt = Runtime.getRuntime();
    long freeMemory  = rt.freeMemory();
    long totalMemory = rt.totalMemory();
    long maxMemory   = rt.maxMemory();
    long overhead    = maxMemory - totalMemory + freeMemory;

    /* if the overhead is below the minimun, 
         first force a JVM garbage collection in order to see if enough overhead can 
	 by freed up without a node collection and recompute the memory stats */ 
    if(overhead < pMinimumOverhead.get()) {
      timer.suspend();
      TaskTimer tm = new TaskTimer("NodeGC [JVM Pre-GC]");
      {
	rt.gc();

	/* wait for the garbage collector to finish */ 
	tm.aquire();
	try {
	  Thread.sleep(3000);
	}
	catch(InterruptedException ex) {
	}
	finally {
	  tm.resume();
	}
	
	freeMemory  = rt.freeMemory();
	totalMemory = rt.totalMemory();
	maxMemory   = rt.maxMemory();
	overhead    = maxMemory - totalMemory + freeMemory;
      }
      LogMgr.getInstance().logSubStage
	(LogMgr.Kind.Mem, LogMgr.Level.Finer, 
	 tm, timer);
    }
    
    /* report the current memory statistics */ 
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Mem, LogMgr.Level.Finer)) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Mem, LogMgr.Level.Finer,
	 "Pre-GC Memory Stats:\n" + 
	 "  ---- JVM HEAP ----------------------\n" + 
	 "    Free = " + freeMemory + 
	             " (" + ByteSize.longToFloatString(freeMemory) + ")\n" + 
	 "   Total = " + totalMemory + 
	             " (" + ByteSize.longToFloatString(totalMemory) + ")\n" +
	 "     Max = " + maxMemory + 
	             " (" + ByteSize.longToFloatString(maxMemory) + ")\n" +
	 "  ---- OVERHEAD ----------------------\n" + 
	 "     Avl = " + overhead + 
	             " (" + ByteSize.longToFloatString(overhead) + ")\n" +
	 "     Min = " + pMinimumOverhead.get() + 
	             " (" + ByteSize.longToFloatString(pMinimumOverhead.get()) + ")\n" +
	 "     Max = " + pMaximumOverhead.get() + 
	             " (" + ByteSize.longToFloatString(pMaximumOverhead.get()) + ")\n" +
	 "  ---- NODE CACHE --------------------\n" + 
	 "   Cache = " + pNodeCacheSize.get() + " (node versions)\n" + 
	 "    Node = " + pAverageNodeSize.get() + " (bytes/version)\n" +
	 "     Mem = " + cacheMemory +
	             " (" + ByteSize.longToFloatString(cacheMemory) + ")\n" +
	 "  ------------------------------------");
      LogMgr.getInstance().flush();
    }

    /* nothing cached, so no reason to run the node garbage collector */ 
    if(pNodeCacheSize.get() == 0) {
      if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Mem, LogMgr.Level.Fine)) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Mem, LogMgr.Level.Fine,
	   "NodeGC: (empty cache)\n  " + timer); 
      }
    }
    else {
      /* if the amount of overhead is less than the minimum, 
	   free up enough node versions from the cache to raise the overhead up to 
	   the maximum */
      long exceeded = 0L;
      if(overhead < pMinimumOverhead.get()) {
	timer.suspend();
	TaskTimer tm = new TaskTimer("NodeGC [Reducing Node Cache]");
	{
	  /* the estimated number of nodes to free */ 
	  exceeded = (pMaximumOverhead.get() - overhead) / pAverageNodeSize.get();
	  
	  /* make sure we don't try to free more versions than are available to be freed */
	  if(exceeded > pNodeCacheSize.get()) {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Mem, LogMgr.Level.Warning,
	       "The maximum overhead (" + pMaximumOverhead.get() + ") cannot be achieved " + 
	       "even by freeing all (" + pNodeCacheSize.get() + ") versions from the node " + 
	       "cache!  Either the maximum overhead is set too high or the maximum heap " + 
	       "size (" + maxMemory + ") is set too low to provide a sufficent amount of " + 
	       "node cache needed for efficient operation."); 
	    
	    exceeded = pNodeCacheSize.get();
	  }
	  
	  tm.aquire();
	  pDatabaseLock.writeLock().lock();
	  try {
	    tm.resume();
	    
	    /* sort the cached versions by newest last access time */
	    long cached = 0L;
	    TreeMap<Long,String> sorted = new TreeMap<Long,String>();
	    {
	      TreeSet<String> names = new TreeSet<String>();
	      names.addAll(pCheckedInBundles.keySet());
	      names.addAll(pWorkingBundles.keySet());
	      
	      for(String name : names) {
		long newest = 0L;
		long count = 0L;
		{
		  TreeMap<VersionID,CheckedInBundle> table = pCheckedInBundles.get(name);
		  if(table != null) {
		    for(CheckedInBundle bundle : table.values()) {
		      newest = Math.max(newest, bundle.getLastAccess());
		      count++;
		    }
		  }
		}
		
		{
		  HashMap<NodeID,WorkingBundle> table = pWorkingBundles.get(name);
		  if(table != null) {
		    for(WorkingBundle bundle : table.values()) {
		      newest = Math.max(newest, bundle.getLastAccess());
		      count++;		
		    }
		  }
		}
	    
		sorted.put(newest, name);
		cached += count;
	      }
	    }

	    /* sanity check */ 
	    if(pNodeCacheSize.get() != cached) {
	      LogMgr.getInstance().log
		(LogMgr.Kind.Mem, LogMgr.Level.Warning,
		 "The number of nodes computed by analyzing the node cache " +
		 "(" + cached + ") did not match the node size counter " + 
		 "(" + pNodeCacheSize.get() + ")!  " + 
		 "Resetting the node size counter to actual cache size.");
	  
	      pNodeCacheSize.set(cached);
	    }

	    /* collect enough versions to lower the cache below the exceeded level */ 
	    long freed = 0L;
	    for(String name : sorted.values()) {
	      if(freed >= exceeded) 
		break;
	  
	      /* free working versions */ 
	      {
		HashMap<NodeID,WorkingBundle> table = pWorkingBundles.get(name);
		if(table != null) {
		  freed += table.size();
		  pWorkingBundles.remove(name);

		  for(NodeID id : table.keySet()) 
		    decrementWorkingCounter(id);
		}
	      }
	  
	      /* free checked-in versions */ 
	      {
		TreeMap<VersionID,CheckedInBundle> table = pCheckedInBundles.get(name);
		if(table != null) {
		  freed += table.size();
		  pCheckedInBundles.remove(name);

		  for(VersionID vid : table.keySet()) 
		    decrementCheckedInCounter(name, vid);
		}
	      }
	    }
	  }
	  finally {
	    pDatabaseLock.writeLock().unlock();
	  } 
	}
	LogMgr.getInstance().logSubStage
	  (LogMgr.Kind.Mem, LogMgr.Level.Finer, 
	   tm, timer);
      
	/* force another JVM garbage collection in order to determine how much memory 
	   was actually freed and adjust the average size of a node version */ 
	long avgNodeSize = 0L;
	{
	  timer.suspend();
	  TaskTimer tm2 = new TaskTimer("NodeGC [JVM Post-GC]");
	  {
	    rt.gc();
	  
	    /* wait for the garbage collector to finish */ 
	    tm2.aquire();
	    try {
	      Thread.sleep(3000);
	    }
	    catch(InterruptedException ex) {
	    }
	    finally {
	      tm2.resume();
	    }
	  
	    long oldOverhead = overhead; 

	    freeMemory  = rt.freeMemory();
	    totalMemory = rt.totalMemory();
	    maxMemory   = rt.maxMemory();
	    overhead    = maxMemory - totalMemory + freeMemory;

	    avgNodeSize = (overhead - oldOverhead) / exceeded;
	    if(avgNodeSize > 0L) {
	      long newSize = (long) (pAverageNodeSize.get()*0.85 + avgNodeSize*0.15);
	      if((newSize > 2048L) && (newSize < 16384L))
		pAverageNodeSize.set(newSize);
	    }

	    cacheMemory = pNodeCacheSize.get() * pAverageNodeSize.get();
	  }
	  LogMgr.getInstance().logSubStage
	    (LogMgr.Kind.Mem, LogMgr.Level.Finer, 
	     tm2, timer);
	}

	if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Mem, LogMgr.Level.Fine)) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Mem, LogMgr.Level.Fine,
	     "NodeGC: " + pNodeCacheSize.get() + "/" + exceeded + " (cached/freed)\n  " + 
	     timer); 
	}

	if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Mem, LogMgr.Level.Finer)) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Mem, LogMgr.Level.Finer,
	     "Post-GC Memory Stats:\n" + 
	     "  ---- JVM HEAP ----------------------\n" + 
	     "    Free = " + freeMemory + 
	                 " (" + ByteSize.longToFloatString(freeMemory) + ")\n" + 
	     "   Total = " + totalMemory + 
	                 " (" + ByteSize.longToFloatString(totalMemory) + ")\n" +
	     "     Max = " + maxMemory + 
	                 " (" + ByteSize.longToFloatString(maxMemory) + ")\n" +
	     "  ---- OVERHEAD ----------------------\n" + 
	     "     Avl = " + overhead + 
	                 " (" + ByteSize.longToFloatString(overhead) + ")\n" +
	     "     Min = " + pMinimumOverhead.get() + 
	                 " (" + ByteSize.longToFloatString(pMinimumOverhead.get()) + ")\n" +
	     "     Max = " + pMaximumOverhead.get() + 
           	         " (" + ByteSize.longToFloatString(pMaximumOverhead.get()) + ")\n" +
	     "  ---- NODE CACHE --------------------\n" + 
	     "   Cache = " + pNodeCacheSize.get() + " (node versions)\n" + 
	     "     Est = " + avgNodeSize + " (bytes/version)\n" + 
	     "    Node = " + pAverageNodeSize.get() + " (bytes/version)\n" +
	     "     Mem = " + cacheMemory +
	                 " (" + ByteSize.longToFloatString(cacheMemory) + ")\n" +
	     "  ------------------------------------");
	  LogMgr.getInstance().flush();
	}

	/* post-op tasks */ 
	if(exceeded > 0L) {
	  MasterTaskFactory factory = 
	    new NodeGarbageCollectExtFactory(pNodeCacheSize.get(), exceeded);
	  startExtensionTasks(timer, factory);			    
	}
      }
    }

    /* if we're ahead of schedule, take a nap */ 
    {
      timer.suspend();
      long nap = pNodeGCInterval.get() - timer.getTotalDuration();
      if(nap > 0) {
	try {
	  Thread.sleep(nap);
	}
	catch(InterruptedException ex) {
	}
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Record that a new working version has been added to the node cache.
   */ 
  private void 
  incrementWorkingCounter
  (
   NodeID nodeID
  ) 
  {
    pNodeCacheSize.getAndAdd(1L);

    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Mem, LogMgr.Level.Finest)) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Mem, LogMgr.Level.Finest,
	 "Cached Working Version: " + nodeID.getName() + 
	 " (" + nodeID.getAuthor() + "|" + nodeID.getView() + ")\n" + 
	 "  Total Cached = " + pNodeCacheSize.get()); 
    }
  }

  /**
   * Record that a working version has been freed from the node cache. 
   */ 
  private void 
  decrementWorkingCounter
  (
   NodeID nodeID
  ) 
  {
    pNodeCacheSize.getAndAdd(-1L);

    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Mem, LogMgr.Level.Finest)) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Mem, LogMgr.Level.Finest,
	 "Freed Working Version: " +  nodeID.getName() + 
	 " (" + nodeID.getAuthor() + "|" + nodeID.getView() + ")\n" + 
	 "  Total Cached = " + pNodeCacheSize.get()); 
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Record that a new checked-in version has been added to the node cache.
   */ 
  private void 
  incrementCheckedInCounter
  (
   String name, 
   VersionID vid
  ) 
  {
    pNodeCacheSize.getAndAdd(1L);

    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Mem, LogMgr.Level.Finest)) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Mem, LogMgr.Level.Finest,
	 "Cached Checked-In Version: " + name + " v" + vid + "\n" + 
	 "  Total Cached = " + pNodeCacheSize.get()); 
    }
  }

  /**
   * Record that a checked-in version has been freed from the node cache. 
   */ 
  private void 
  decrementCheckedInCounter
  (
   String name, 
   VersionID vid
  ) 
  {
    pNodeCacheSize.getAndAdd(-1L);

    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Mem, LogMgr.Level.Finest)) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Mem, LogMgr.Level.Finest,
	 "Freed Checked-In Version: " + name + " v" + vid + "\n" + 
	 "  Total Cached = " + pNodeCacheSize.get()); 
    }
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
      File file = new File(pNodeDir, "archives/manifests/" + archive.getName());
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
      File file = new File(pNodeDir, "archives/manifests/" + name);
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
	   "The archive file (" + file + ") appears to be corrupted:\n" + 
	   "  " + ex.getMessage());
	LogMgr.getInstance().flush();
	
	throw new PipelineException
	  ("I/O ERROR: \n" + 
	   "  While attempting to read the archive file (" + file + ")...\n" + 
	   "    " + ex.getMessage());
      }
      if((archive == null) || !archive.getName().equals(name))
	throw new IllegalStateException(); 

      return archive;
    }
  }
   


  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Write the cached names of the archive volumes indexed by the fully resolved node 
   * names and revision numbers of the checked-in versions contained in the archive. <P> 
   * 
   * @throws PipelineException
   *   If unable to write the cache file. 
   */ 
  private void 
  writeArchivedIn() 
    throws PipelineException
  {
    synchronized(pArchivedIn) {
      if(pArchivedIn.isEmpty()) 
	return;

      File file = new File(pNodeDir, "archives/archived-in");

      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Writing Archived In Cache...");
      
      try {
	String glue = null;
	try {
	  GlueEncoder ge = new GlueEncoderImpl("ArchivedIn", pArchivedIn);
	  glue = ge.getText();
	}
	catch(GlueException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	     "Unable to generate a Glue format representation of the archived in cache!");
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
	 "  While attempting to write the archive cache file...\n" + 
	 "    " + ex.getMessage());
      }
    }
  }
  
  /**
   * Read the cached names of the archive volumes indexed by the fully resolved node 
   * names and revision numbers of the checked-in versions contained in the archive. <P> 
   * 
   * @throws PipelineException
   *   If unable to read the cache file.
   */ 
  private void
  readArchivedIn()
    throws PipelineException
  {
    synchronized(pArchivedIn) {
      File file = new File(pNodeDir, "archives/archived-in");
      if(!file.isFile()) 
	return;

      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Reading Archived In Cache...");

      try {
	FileReader in = new FileReader(file);
	GlueDecoder gd = new GlueDecoderImpl(in);
	pArchivedIn.putAll
	  ((TreeMap<String,TreeMap<VersionID,TreeSet<String>>>) gd.getObject());
	in.close();
      }
      catch(Exception ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	   "The archived in cache file (" + file + ") appears to be corrupted:\n" + 
	   "  " + ex.getMessage());
	LogMgr.getInstance().flush();
	
	throw new PipelineException
	  ("I/O ERROR: \n" + 
	   "  While attempting to read the archived in cache file...\n" +
	   "    " + ex.getMessage());
      }
    }
  }
   

  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Write the cached timestamps of when each archive volume was created indexed by unique 
   * archive volume name.
   * 
   * @throws PipelineException
   *   If unable to write the cache file. 
   */ 
  private void 
  writeArchivedOn() 
    throws PipelineException
  {
    synchronized(pArchivedOn) {
      if(pArchivedOn.isEmpty()) 
	return;

      File file = new File(pNodeDir, "archives/archived-on");

      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Writing Archived On Cache...");
      
      try {
	String glue = null;
	try {
	  TreeMap<String,Long> archivedOn = new TreeMap<String,Long>();
	  for(String aname : pArchivedOn.keySet()) 
	    archivedOn.put(aname, pArchivedOn.get(aname).getTime());

	  GlueEncoder ge = new GlueEncoderImpl("ArchivedOn", archivedOn);
	  glue = ge.getText();
	}
	catch(GlueException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	     "Unable to generate a Glue format representation of the archived on cache!");
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
	 "  While attempting to write the archived on cache file...\n" + 
	 "    " + ex.getMessage());
      }
    }
  }
  
  /**
   * Read the cached timestamps of when each archive volume was created indexed by unique 
   * archive volume name.
   * 
   * @throws PipelineException
   *   If unable to read the cache file.
   */ 
  private void
  readArchivedOn()
    throws PipelineException
  {
    synchronized(pArchivedOn) {
      File file = new File(pNodeDir, "archives/archived-on");
      if(!file.isFile()) 
	return;

      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Reading Archived On Cache...");

      try {
	FileReader in = new FileReader(file);
	GlueDecoder gd = new GlueDecoderImpl(in);
	
	TreeMap<String,Long> archivedOn = (TreeMap<String,Long>) gd.getObject();
	for(String aname : archivedOn.keySet()) 
	  pArchivedOn.put(aname, new Date(archivedOn.get(aname)));	

	in.close();
      }
      catch(Exception ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	   "The archived on cache file (" + file + ") appears to be corrupted:\n" + 
	   "  " + ex.getMessage());
	LogMgr.getInstance().flush();
	
	throw new PipelineException
	  ("I/O ERROR: \n" + 
	   "  While attempting to read the archived on cache file...\n" +
	   "    " + ex.getMessage());
      }
    }
  }

   
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Write the cached timestamps of when each archive volume was created indexed by unique 
   * archive volume name.
   * 
   * @throws PipelineException
   *   If unable to write the cache file. 
   */ 
  private void 
  writeRestoredOn() 
    throws PipelineException
  {
    synchronized(pRestoredOn) {
      if(pRestoredOn.isEmpty()) 
	return;

      File file = new File(pNodeDir, "archives/restored-on");

      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Writing Restored On Cache...");
      
      try {
	String glue = null;
	try {
	  TreeMap<String,TreeSet<Long>> restoredOn = new TreeMap<String,TreeSet<Long>>();
	  for(String aname : pRestoredOn.keySet()) {
	    TreeSet<Long> stamps = new TreeSet<Long>();
	    restoredOn.put(aname, stamps);
	    for(Date stamp : pRestoredOn.get(aname)) 
	      stamps.add(stamp.getTime());
	  }
	      
	  GlueEncoder ge = new GlueEncoderImpl("RestoredOn", restoredOn);
	  glue = ge.getText();
	}
	catch(GlueException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	     "Unable to generate a Glue format representation of the restored on cache!");
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
	 "  While attempting to write the restored on cache file...\n" + 
	 "    " + ex.getMessage());
      }
    }
  }
  
  /**
   * Read the cached timestamps of when each archive volume was created indexed by unique 
   * archive volume name.
   * 
   * @throws PipelineException
   *   If unable to read the cache file.
   */ 
  private void
  readRestoredOn()
    throws PipelineException
  {
    synchronized(pRestoredOn) {
      File file = new File(pNodeDir, "archives/restored-on");
      if(!file.isFile()) 
	return;

      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Reading Restored On Cache...");

      try {
	FileReader in = new FileReader(file);
	GlueDecoder gd = new GlueDecoderImpl(in);

	TreeMap<String,TreeSet<Long>> restoredOn = 
	  (TreeMap<String,TreeSet<Long>>) gd.getObject();

	for(String aname : restoredOn.keySet()) {
	  TreeSet<Date> stamps = new TreeSet<Date>();
	  pRestoredOn.put(aname, stamps);
	  for(Long stamp : restoredOn.get(aname)) 
	    stamps.add(new Date(stamp));
	}

	in.close();
      }
      catch(Exception ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	   "The restored on cache file (" + file + ") appears to be corrupted:\n" + 
	   "  " + ex.getMessage());
	LogMgr.getInstance().flush();
	
	throw new PipelineException
	  ("I/O ERROR: \n" + 
	   "  While attempting to read the restored on cache file...\n" +
	   "    " + ex.getMessage());
      }
    }
  }
   

  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Write the cached fully resolved node names and revision numbers of the checked-in 
   * versions which are currently offline. <P> 
   * 
   * @throws PipelineException
   *   If unable to write the cache file. 
   */ 
  private void 
  writeOfflined() 
    throws PipelineException
  {
    synchronized(pOfflined) {
      if(pOfflined.isEmpty()) 
	return;

      File file = new File(pNodeDir, "archives/offlined");

      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Writing Offlined Cache...");
      
      try {
	String glue = null;
	try {
	  GlueEncoder ge = new GlueEncoderImpl("Offlined", pOfflined);
	  glue = ge.getText();
	}
	catch(GlueException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	     "Unable to generate a Glue format representation of the offlined cache!");
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
	 "  While attempting to write the offlined cache file...\n" + 
	 "    " + ex.getMessage());
      }
    }
  }
  
  /**
   * Read the cached fully resolved node names and revision numbers of the checked-in 
   * versions which are currently offline. <P> 
   * 
   * @throws PipelineException
   *   If unable to read the cache file.
   */ 
  private void
  readOfflined()
    throws PipelineException
  {
    synchronized(pOfflined) {
      File file = new File(pNodeDir, "archives/offlined");
      if(!file.isFile()) 
	return;

      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Reading Offlined Cache...");

      try {
	FileReader in = new FileReader(file);
	GlueDecoder gd = new GlueDecoderImpl(in);
	pOfflined.putAll((TreeMap<String,TreeSet<VersionID>>) gd.getObject());
	in.close();
      }
      catch(Exception ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	   "The offlined cache file (" + file + ") appears to be corrupted:\n" + 
	   "  " + ex.getMessage());
	LogMgr.getInstance().flush();
	
	throw new PipelineException
	  ("I/O ERROR: \n" + 
	   "  While attempting to read the offlined cache file...\n" +
	   "    " + ex.getMessage());
      }
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
      File file = new File(pNodeDir, "archives/restore-reqs");
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

      File file = new File(pNodeDir, "archives/restore-reqs");
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
	     "The restore requests file (" + file + ") appears to be corrupted:\n" + 
	     "  " + ex.getMessage());
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
      File file = new File(pNodeDir, "toolsets/default-toolset");
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

      File file = new File(pNodeDir, "toolsets/default-toolset");
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
	     "The default toolset file (" + file + ") appears to be corrupted:\n" + 
	     "  " + ex.getMessage());
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
      File file = new File(pNodeDir, "toolsets/active-toolsets");
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

      File file = new File(pNodeDir, "toolsets/active-toolsets");
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
	     "The active toolsets file (" + file + ") appears to be corrupted:\n" + 
	     "  " + ex.getMessage());
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
   * @param os
   *   The operating system type. 
   * 
   * @throws PipelineException
   *   If unable to write the toolset file.
   */ 
  private void 
  writeToolset
  (
   Toolset tset, 
   OsType os
  ) 
    throws PipelineException
  {
    synchronized(pToolsets) {
      File dir = new File(pNodeDir, "toolsets/toolsets/" + tset.getName());
      synchronized(pMakeDirLock) {
	if(!dir.isDirectory()) 
	  if(!dir.mkdir()) 
	    throw new PipelineException
	      ("Unable to create toolset directory (" + dir + ")!");
      }

      File file = new File(dir, os.toString());
      if(file.exists()) {
	throw new PipelineException
	  ("Unable to overrite the existing toolset file(" + file + ")!");
      }

      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Writing " + os + " Toolset: " + tset.getName());

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
   * @param os
   *   The operating system type. 
   * 
   * @throws PipelineException
   *   If unable to read the toolset file.
   */ 
  private Toolset
  readToolset
  (
   String tname, 
   OsType os
  )
    throws PipelineException
  {
    synchronized(pToolsets) {
      File file = new File(pNodeDir, "toolsets/toolsets/" + tname + "/" + os);
      if(!file.isFile()) 
	throw new PipelineException
	  ("No " + os + " toolset file exists for the toolset (" + tname + ")!");

      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Reading " + os + " Toolset: " + tname);

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
	   "The toolset file (" + file + ") appears to be corrupted:\n" + 
	   "  " + ex.getMessage());
	LogMgr.getInstance().flush();
	
	throw new PipelineException
	  ("I/O ERROR: \n" + 
	   "  While attempting to read the toolset file (" + file + ")...\n" + 
	   "    " + ex.getMessage());
      }
      if((tset == null) || !tset.getName().equals(tname))
	throw new IllegalStateException(); 

      TreeMap<OsType,Toolset> toolsets = pToolsets.get(tname);
      if(toolsets == null) {
	toolsets = new TreeMap<OsType,Toolset>();
	pToolsets.put(tname, toolsets);
      }

      toolsets.put(os, tset);      

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
   * @param os
   *   The operating system type. 
   * 
   * @throws PipelineException
   *   If unable to write the toolset package file.
   */ 
  private void 
  writeToolsetPackage
  (
   PackageVersion pkg, 
   OsType os
  ) 
    throws PipelineException
  {
    synchronized(pToolsetPackages) {
      File dir = new File(pNodeDir, "toolsets/packages/" + pkg.getName() + "/" + os);
      synchronized(pMakeDirLock) {
	if(!dir.isDirectory()) 
	  if(!dir.mkdirs()) 
	    throw new PipelineException
	      ("Unable to create toolset package directory (" + dir + ")!");
      }

      File file = new File(dir, pkg.getVersionID().toString());
      if(file.exists()) {
	throw new PipelineException
	  ("Unable to overrite the existing toolset package file (" + file + ")!");
      }

      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Writing " + os + " Toolset Package: " + pkg.getName() + " v" + pkg.getVersionID());

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
   * @param os
   *   The operating system type. 
   * 
   * @throws PipelineException
   *   If unable to read the toolset package file.
   */ 
  private PackageVersion
  readToolsetPackage
  (
   String name, 
   VersionID vid, 
   OsType os
  )
    throws PipelineException
  {
    synchronized(pToolsetPackages) {
      File file = new File(pNodeDir, "toolsets/packages/" + name + "/" + os + "/" + vid);
      if(!file.isFile()) 
	throw new PipelineException
	  ("No " + os + " toolset package file exists for package " + 
	   "(" + name + " v" + vid + ")!");

      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Reading " + os + " Toolset Package: " + name + " v" + vid);

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
	   "The toolset package file (" + file + ") appears to be corrupted:\n" + 
	   "  " + ex.getMessage());
	LogMgr.getInstance().flush();
	
	throw new PipelineException
	  ("I/O ERROR: \n" + 
	   "  While attempting to read the toolset package file (" + file + ")...\n" + 
	   "    " + ex.getMessage());
      }
      if((pkg == null) || !pkg.getName().equals(name) || !pkg.getVersionID().equals(vid))
	throw new IllegalStateException(); 

      pToolsetPackages.put(name, os, vid, pkg);

      return pkg;
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the master extension configurations to disk. <P> 
   * 
   * @throws PipelineException
   *   If unable to write the extensions file.
   */ 
  private void 
  writeMasterExtensions() 
    throws PipelineException
  {
    synchronized(pMasterExtensions) {
      File file = new File(pNodeDir, "etc/master-extensions"); 
      if(file.exists()) {
	if(!file.delete())
	  throw new PipelineException
	    ("Unable to remove the old master extensions file (" + file + ")!");
      }

      if(!pMasterExtensions.isEmpty()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Writing Master Extensions.");

	try {
	  String glue = null;
	  try {
	    GlueEncoder ge = new GlueEncoderImpl("MasterExtensions", pMasterExtensions);
	    glue = ge.getText();
	  }
	  catch(GlueException ex) {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	       "Unable to generate a Glue format representation of the master extensions!");
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
	     "  While attempting to write the master extensions file (" + file + ")...\n" + 
	     "    " + ex.getMessage());
	}
      }
    }
  }
  
  /**
   * Read the master extension configurations from disk.
   * 
   * @throws PipelineException
   *   If unable to read the extensions file.
   */ 
  private void 
  readMasterExtensions()
    throws PipelineException
  {
    synchronized(pMasterExtensions) {
      pMasterExtensions.clear();

      File file = new File(pNodeDir, "etc/master-extensions"); 
      if(file.isFile()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Reading Master Extensions.");

	TreeMap<String,MasterExtensionConfig> exts = null;
	try {
	  FileReader in = new FileReader(file);
	  GlueDecoder gd = new GlueDecoderImpl(in);
	  exts = (TreeMap<String,MasterExtensionConfig>) gd.getObject();
	  in.close();
	}
	catch(Exception ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	     "The default toolset file (" + file + ") appears to be corrupted:\n" + 
	     "  " + ex.getMessage());
	  LogMgr.getInstance().flush();
	  
	  throw new PipelineException
	    ("I/O ERROR: \n" + 
	     "  While attempting to read the master extensions file (" + file + ")...\n" + 
	     "    " + ex.getMessage());
	}

	if(exts != null)
	  pMasterExtensions.putAll(exts);
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the layout of plugin menus associated with a toolset to file.
   * 
   * @param name
   *   The name of the toolset.
   * 
   * @param ptype
   *   The type of plugins: editors, comparators, actions or tools
   * 
   * @param table
   *   The plugin menu layout table.
   *
   * @param defaultLayout
   *   The default menu layout.
   *
   * @throws PipelineException
   *   If unable to write the menu layuout file. 
   */ 
  private void 
  writePluginMenuLayout
  (
   String name, 
   String ptype,
   TreeMap<String,PluginMenuLayout> table, 
   PluginMenuLayout defaultLayout
  ) 
    throws PipelineException
  {
    String uptype = pluginTypeUpcaseHelper(ptype); 
    String fptype = pluginTypeFilenameHelper(ptype); 

    synchronized(table) {
      File dir = null;
      if(name != null) 
	dir = new File(pNodeDir, "toolsets/plugins/toolsets/" + name + "/Unix");
      else 
	dir = new File(pNodeDir, "etc/default-layouts/Unix");

      synchronized(pMakeDirLock) {
	if(!dir.isDirectory()) 
	  if(!dir.mkdirs()) 
	    throw new PipelineException
	      ("Unable to create toolset plugin menu directory (" + dir + ")!");
      }

      File file = new File(dir, fptype);

      PluginMenuLayout layout = null;
      if(name != null) 
	layout = table.get(name);
      else 
	layout = defaultLayout;

      if(layout == null) {
	if(file.exists())
	  if(!file.delete()) 
	    throw new PipelineException
	      ("Unable to remove obsolete toolset plugin menu file (" + file + ")!");
	return;
      }
	
      String tname = name;
      if(tname == null) 
	tname = "(default layout)";

      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Writing Toolset Plugin Menu: " + tname + " " + uptype);

      try {
	String glue = null;
	try {
	  GlueEncoder ge = new GlueEncoderImpl(uptype + "MenuLayout", layout);
	  glue = ge.getText();
	}
	catch(GlueException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	     "Unable to generate a Glue format representation of the " + ptype + 
	     " plugin menu layout associated with the toolset (" + tname + ")!");
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
	   "  While attempting to write the toolset plugin menu layout file " + 
	   "(" + file + ")...\n" + 
	   "    " + ex.getMessage());
      }
    }
  }
  
  /**
   * Read the layout of plugin menus associated with a toolset from file if it exists.
   * 
   * @param name
   *   The name of the toolset.
   * 
   * @param ptype
   *   The type of plugins: editors, comparators, actions or tools
   * 
   * @param table
   *   The plugin menu layout table.
   * 
   * @return 
   *   The read menu layout.
   * 
   * @throws PipelineException
   *   If unable to read an existing package plugins file.
   */ 
  private PluginMenuLayout
  readPluginMenuLayout
  (
   String name, 
   String ptype,
   TreeMap<String,PluginMenuLayout> table
  ) 
    throws PipelineException
  {
    String uptype = pluginTypeUpcaseHelper(ptype); 
    String fptype = pluginTypeFilenameHelper(ptype); 

    synchronized(table) {
      File file = null;
      if(name != null) 
	file = new File(pNodeDir, 
			"toolsets/plugins/toolsets/" + name + "/Unix/" + fptype);  
      else 
	file = new File(pNodeDir, "etc/default-layouts/Unix/" + fptype);

      if(!file.isFile())
	return null;

      String tname = name;
      if(tname == null) 
	tname = "(default layout)";

      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Reading Toolset Plugin Menu: " + tname + " " + uptype);

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
	   "The toolset plugin menu file (" + file + ") appears to be corrupted:\n" + 
	   "  " + ex.getMessage());
	LogMgr.getInstance().flush();
	
	throw new PipelineException
	  ("I/O ERROR: \n" + 
	   "  While attempting to read the toolset plugin menu file (" + file + ")...\n" + 
	   "    " + ex.getMessage());
      }
      if(layout == null)
	throw new IllegalStateException(); 

      if(name != null) 
	table.put(name, layout);

      return layout;
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the table of plugins associated with a toolset package to file.
   * 
   * @param name
   *   The name of the toolset package.
   * 
   * @param vid
   *   The package revision number
   * 
   * @param ptype
   *   The type of plugins: editor, comparator, action, tool, master extension or 
   *   queue extension.
   * 
   * @param table
   *   The plugins table.
   * 
   * @throws PipelineException
   *   If unable to write the plugins file. 
   */ 
  private void 
  writePackagePlugins
  (
   String name, 
   VersionID vid, 
   String ptype,
   DoubleMap<String,VersionID,PluginSet> table 
  ) 
    throws PipelineException
  {
    String uptype = pluginTypeUpcaseHelper(ptype); 
    String fptype = pluginTypeFilenameHelper(ptype); 

    synchronized(table) {
      File dir = new File(pNodeDir, 
			  "toolsets/plugins/packages/" + name + "/Unix/" + vid);
      synchronized(pMakeDirLock) {
	if(!dir.isDirectory()) 
	  if(!dir.mkdirs()) 
	    throw new PipelineException
	      ("Unable to create toolset plugins directory (" + dir + ")!");
      }

      File file = new File(dir, fptype);

      PluginSet plugins = table.get(name, vid);
      if((plugins == null) || plugins.isEmpty()) {
	if(file.exists())
	  if(!file.delete()) 
	    throw new PipelineException
	      ("Unable to remove obsolete toolset plugins file (" + file + ")!");
	return;
      }
	
      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Writing Toolset Package Plugins: " + name + " v" + vid + " " + uptype);

      try {
	String glue = null;
	try {
	  GlueEncoder ge = 
	    new GlueEncoderImpl("Package" + uptype + "Plugins", 
				(DoubleMap<String,String,TreeSet<VersionID>>) plugins);
	  glue = ge.getText();
	}
	catch(GlueException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	     "Unable to generate a Glue format representation of the " + ptype + 
	     " plugins associated with the toolset package (" + name + " v" + vid +")!");
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
	   "  While attempting to write the toolset package plugins file " + 
	   "(" + file + ")...\n" + 
	   "    " + ex.getMessage());
      }
    }
  }
  
  /**
   * Read the table of plugins associated with a toolset package from file if it exists.
   * 
   * @param name
   *   The name of the toolset package.
   * 
   * @param vid
   *   The package revision number
   * 
   * @param ptype
   *   The type of plugins: editors, comparators, actions or tools
   * 
   * @param table
   *   The plugins table.
   * 
   * @throws PipelineException
   *   If unable to read an existing package plugins file.
   */ 
  private void
  readPackagePlugins
  (
   String name, 
   VersionID vid, 
   String ptype,
   DoubleMap<String,VersionID,PluginSet> table 
  )
    throws PipelineException
  {
    String uptype = pluginTypeUpcaseHelper(ptype); 
    String fptype = pluginTypeFilenameHelper(ptype); 

    synchronized(table) {
      File file = 
	new File(pNodeDir, 
		 "toolsets/plugins/packages/" + name + "/Unix/" + vid + "/" + fptype);
      if(!file.isFile())
	return;

      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Reading Toolset Package Plugins: " + name + " v" + vid + " " + uptype);

      PluginSet plugins = null;
      try {
	FileReader in = new FileReader(file);
	GlueDecoder gd = new GlueDecoderImpl(in);
	Object data = gd.getObject();

	if(data instanceof PluginSet) 
	  plugins = (PluginSet) data;
	else {
	  /* backward compatibility for GLUE files written before PluginSet existed */ 
	  plugins = new PluginSet((DoubleMap<String,String,TreeSet<VersionID>>) data);
	}

	in.close();
      }
      catch(Exception ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	   "The package plugins file (" + file + ") appears to be corrupted:\n" + 
	   "  " + ex.getMessage());
	LogMgr.getInstance().flush();
	
	throw new PipelineException
	  ("I/O ERROR: \n" + 
	   "  While attempting to read the package plugins file (" + file + ")...\n" + 
	   "    " + ex.getMessage());
      }

      if(plugins == null)
	throw new IllegalStateException(); 
      table.put(name, vid, plugins);
    }
  }

  private String 
  pluginTypeUpcaseHelper
  (
   String ptype
  ) 
  {
    StringBuilder buf = new StringBuilder();

    char[] cs = ptype.toCharArray();
    int wk;
    boolean upcase = true;
    for(wk=0; wk<cs.length; wk++) {
      if(Character.isWhitespace(cs[wk])) {
	upcase = true;
      }
      else {
	buf.append(upcase ? Character.toUpperCase(cs[wk]) : cs[wk]);
	upcase = false;
      }
    }
    
    return buf.toString();
  }

  private String 
  pluginTypeFilenameHelper
  (
   String ptype
  ) 
  {
    return (ptype.replaceAll("\\p{Blank}", "-") + "s");
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
      File file = new File(pNodeDir, "etc/suffix-editors/" + author); 
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
	   "The suffix editors file (" + file + ") appears to be corrupted:\n" + 
	   "  " + ex.getMessage());
	LogMgr.getInstance().flush();
	
	throw new PipelineException
	  ("I/O ERROR: \n" + 
	   "  While attempting to read the suffix editors file (" + file + ")...\n" + 
	   "    " + ex.getMessage());
      }
      if(editors == null)
	throw new IllegalStateException(); 

      TreeMap<String,SuffixEditor> table = new TreeMap<String,SuffixEditor>();
      for(SuffixEditor se : editors) 
	table.put(se.getSuffix(), se);

      pSuffixEditors.put(author, table);

      return table;
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
    File file = new File(pNodeDir, "etc/next-ids");
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
    File file = new File(pNodeDir, "etc/next-ids");
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
	   "The job/group IDs file (" + file + ") appears to be corrupted:\n" + 
	   "  " + ex.getMessage());
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
    File dir = new File(pNodeDir, "repository" + name + "/");
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
	   "The checked-in version file (" + files[wk] + ") appears to be corrupted:\n" + 
	   "  " + ex.getMessage());
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

    File file   = new File(pNodeDir, id.getWorkingPath().toString());
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
    File file   = new File(pNodeDir, id.getWorkingPath().toString());
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
	    "The working version file (" + file + ") appears to be corrupted:\n" + 
	     "  " + ex.getMessage());
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
		"The backup working version file (" + backup + ") appears to be " + 
		 "corrupted:\n" +
		 "  " + ex.getMessage());
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
    File file = new File(pNodeDir, "downstream/" + links.getName());
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
    File file = new File(pNodeDir, "downstream/" + name);
    
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
	    "The downstream links file (" + file + ") appears to be corrupted:\n" + 
	     "  " + ex.getMessage());
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

      TaskTimer timer = new TaskTimer();
      CheckInExtFactory factory = new CheckInExtFactory();
      pHasExtTests = hasAnyExtensionTests(timer, factory);
      pHasExtTasks = hasAnyExtensionTasks(timer, factory);
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
      if(details == null)
	throw new IllegalStateException(); 

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
      case ModifiedLocks:
	{	
	  NodeID nodeID = status.getNodeID();

	  /* get working bundle */ 
	  WorkingBundle working = getWorkingBundle(nodeID);
	  NodeMod work = working.getVersion();
	  {
	    if(work.isLocked())
	      return;
	    
	    if(work.isFrozen()) 
	      throw new PipelineException
		("Somehow a frozen node (" + name + ") was erroneously " + 
		 "submitted for check-in!");
	  }

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

	  /* pre-op tests */
	  if(pHasExtTests) {
	    CheckInExtFactory factory = 
	      new CheckInExtFactory(nodeID, new NodeMod(work), 
				    pRequest.getLevel(), pRequest.getMessage());
	    performExtensionTests(timer, factory);
	  }

	  /* determine the checked-in revision numbers and locked status of 
	     the upstream nodes */ 
	  TreeMap<String,VersionID> lvids = new TreeMap<String,VersionID>();
	  TreeMap<String,Boolean> locked = new TreeMap<String,Boolean>();
	  for(NodeStatus lstatus : status.getSources()) {
	    NodeDetails ldetails = lstatus.getDetails();
	    lvids.put(lstatus.getName(), ldetails.getBaseVersion().getVersionID());
	    locked.put(lstatus.getName(), ldetails.getWorkingVersion().isLocked());
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
	    new NodeVersion(work, vid, lvids, locked, isNovel, 
			    pRequest.getNodeID().getAuthor(), pRequest.getMessage(), 
			    pRequest.getNodeID().getName(), pRootVersionID);

	  writeCheckedInVersion(vsn);

	  /* add the new version to the checked-in bundles */ 
	  if(details.getOverallNodeState() == OverallNodeState.Pending) {
	    synchronized(pCheckedInBundles) {
	      pCheckedInBundles.put(name, checkedIn);
	    }

	    /* keep track of the change to the node version cache */ 
	    incrementCheckedInCounter(name, vid);
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
	  NodeMod nwork = new NodeMod(vsn, work.getLastCriticalModification(), false, false);
	  writeWorkingVersion(nodeID, nwork);

	  /* update the working bundle */ 
	  working.setVersion(nwork);

	  /* update the node status details */ 
	  NodeDetails ndetails = 
	    new NodeDetails(name, 
			    nwork, vsn, checkedIn.get(checkedIn.lastKey()).getVersion(),
			    checkedIn.keySet(), 
			    OverallNodeState.Identical, OverallQueueState.Finished, 
			    VersionState.Identical, PropertyState.Identical, 
			    LinkState.Identical, 
			    fileStates, details.getFileTimeStamps(), ignoreStamps, 
			    jobIDs, queueStates);

	  status.setDetails(ndetails);

	  /* update the node tree entry */ 
	  pNodeTree.addCheckedInNodeTreePath(vsn);

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

	  /* post-op tasks */  
	  if(pHasExtTasks) 
	    startExtensionTasks(timer, new CheckInExtFactory(new NodeVersion(vsn)));
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

    /**
     * Are there any server extensions enabled for this operation.
     */ 
    private boolean  pHasExtTests;
    private boolean  pHasExtTasks;
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
      pVersion    = mod;
      pLastAccess = System.currentTimeMillis();
    }

    /**
     * Get the working version.
     */
    public NodeMod
    getVersion()
    {
      pLastAccess = System.currentTimeMillis();
      return pVersion;
    }
   
    /**
     * Set the working version.
     */
    public void
    setVersion
    (
     NodeMod mod
    )
    {
      pLastAccess = System.currentTimeMillis();
      pVersion    = mod; 
    }
   
    /**
     * Get the timestamp of when the version was last accessed.
     */
    public long
    getLastAccess()
    {
      return pLastAccess;
    }

    /**
     * The working version of a node. 
     */ 
    private NodeMod  pVersion;

    /**
     * The timestamp of when the version was last accessed.
     */ 
    private long  pLastAccess; 
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
      pVersion    = vsn;
      pLastAccess = System.currentTimeMillis();
    }

    /**
     * Get the checked-in version.
     */
    public NodeVersion
    getVersion()
    {
      pLastAccess = System.currentTimeMillis();
      return pVersion;
    }
   
    /**
     * Set the checked-in version.
     */
    public void
    setVersion
    (
     NodeVersion vsn
    )
    {
      pLastAccess = System.currentTimeMillis();
      pVersion    = vsn; 
    }
   
    /**
     * Get the timestamp of when the version was last accessed.
     */
    public long
    getLastAccess()
    {
      return pLastAccess;
    }

    /**
     * The checked-in version of a node.
     */ 
    public NodeVersion  pVersion;

    /**
     * The timestamp of when the version was last accessed.
     */ 
    private long  pLastAccess; 
  }


 
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The common back-end directories.
   * 
   * Since the master manager should always be run on a Unix system, these variables are 
   * always initialized to Unix specific paths.
   */
  private File pNodeDir; 

  
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

  /**
   * Whether to rebuild cache files and ignore existing lock files.
   */
  private boolean  pRebuildCache; 

  /**
   * Whether to keep the offlined versions cache file after startup and reread instead of 
   * rebuilding it during a database rebuild.
   */
  private boolean  pPreserveOfflinedCache; 


  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The combined work groups and adminstrative privileges.
   */ 
  private AdminPrivileges  pAdminPrivileges; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The maximum age of a resolved (Restored or Denied) restore request before it 
   * is deleted (in milliseconds).
   */ 
  private AtomicLong  pRestoreCleanupInterval; 
  

  /**
   * A lock which serializes access to the archive manifest file I/O operations.
   */ 
  private Object pArchiveFileLock; 

  /**	
   * The cached names of the archive volumes indexed by the fully resolved node names and 
   * revision numbers of the checked-in versions contained in the archive. <P> 
   * 
   * This table is rebuilt by scanning the archive GLUE files. <P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */
  private TreeMap<String,TreeMap<VersionID,TreeSet<String>>>  pArchivedIn;
  //private PathMap<TreeMap<VersionID,TreeSet<String>>>  pArchivedIn;

  /**
   * The timestamps of when each archive volume was created indexed by unique archive 
   * volume name.
   *	 
   * This table is rebuilt by scanning the archive GLUE files. <P> 

   * Access to this field should be protected by a synchronized block.
   */
  private TreeMap<String,Date>  pArchivedOn;

  /**
   * The timestamps of when versions from each archive volume was was created indexed by 
   * unique archive volume name.
   *	 
   * This table is rebuilt by scanning the restore output filenames. <P> 

   * Access to this field should be protected by a synchronized block.
   */
  private TreeMap<String,TreeSet<Date>>  pRestoredOn;

  /**
   * The per-node online/offline locks indexed by fully resolved node name. <P> 
   * 
   * These locks protect access to (and modification of) whether the versions of a node are 
   * currently online.  The per-node read-lock should be aquired for operations which 
   * require that the online/offline status not change during the operation.  The per-node 
   * write-lock should be aquired when changing the online/offline status of versions of a 
   * node.
   */
  private HashMap<String,ReentrantReadWriteLock>  pOnlineOfflineLocks;
  // private PathMap<ReentrantReadWriteLock>  pOnlineOfflineLocks;

  /**
   * The fully resolved node names and revision numbers of the checked-in versions which
   * are currently offline. <P> 
   * 
   * This table is rebuild by scanning the repository for empty directories. <P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */ 
  private TreeMap<String,TreeSet<VersionID>>  pOfflined;

  /**
   * The pending restore requests indexed by the fully resolved node names and 
   * revision numbers of the checked-in versions to restore. <P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */  
  private TreeMap<String,TreeMap<VersionID,RestoreRequest>>  pRestoreReqs;  
  //private PathMap<TreeMap<VersionID,RestoreRequest>>  pRestoreReqs;  


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
   * The cached table of all toolsets indexed by toolset name and operating system. <P> 
   * 
   * All existing toolsets will have a key in this table, but the toolset value may be 
   * (null) if the toolset is not currently cached.<P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */
  private TreeMap<String,TreeMap<OsType,Toolset>>  pToolsets;
  //private DoubleMap<String,OsType,Toolset>  pToolsets;

  /**
   * The cached table of all toolset packages indexed by package name, operating system 
   * and package revision number. <P> 
   * 
   * All existing packages will be included as keys in this table, but the package value 
   * may be (null) if the package is not currently cached.<P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */ 
  private TripleMap<String,OsType,VersionID,PackageVersion>  pToolsetPackages;


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The cached tables of plugin menu layouts associated with toolsets indexed by 
   * toolset name. <P> 
   * 
   * The cached default layouts for these operating systems.<P> 
   * 
   * Access to these fields should be protected by a synchronized block.  Access to the
   * default tables should synchronize on the non-default tables.
   */ 
  private TreeMap<String,PluginMenuLayout>  pEditorMenuLayouts;
  private PluginMenuLayout                  pDefaultEditorMenuLayout;

  private TreeMap<String,PluginMenuLayout>  pComparatorMenuLayouts;
  private PluginMenuLayout                  pDefaultComparatorMenuLayout;

  private TreeMap<String,PluginMenuLayout>  pActionMenuLayouts;
  private PluginMenuLayout                  pDefaultActionMenuLayout;

  private TreeMap<String,PluginMenuLayout>  pToolMenuLayouts;
  private PluginMenuLayout                  pDefaultToolMenuLayout;
  
  private TreeMap<String,PluginMenuLayout>  pArchiverMenuLayouts;
  private PluginMenuLayout                  pDefaultArchiverMenuLayout;

  private TreeMap<String,PluginMenuLayout>  pMasterExtMenuLayouts;
  private PluginMenuLayout                  pDefaultMasterExtMenuLayout;

  private TreeMap<String,PluginMenuLayout>  pQueueExtMenuLayouts;
  private PluginMenuLayout                  pDefaultQueueExtMenuLayout;

  /**
   * The cached tables of the vendors, names and versions of all plugins associated with a 
   * package indexed by package name and package revision number. <P> 
   * 
   * Access to these fields should be protected by a synchronized block.
   */ 
  private DoubleMap<String,VersionID,PluginSet>  pPackageEditorPlugins; 
  private DoubleMap<String,VersionID,PluginSet>  pPackageComparatorPlugins; 
  private DoubleMap<String,VersionID,PluginSet>  pPackageActionPlugins; 
  private DoubleMap<String,VersionID,PluginSet>  pPackageToolPlugins; 
  private DoubleMap<String,VersionID,PluginSet>  pPackageArchiverPlugins; 
  private DoubleMap<String,VersionID,PluginSet>  pPackageMasterExtPlugins; 
  private DoubleMap<String,VersionID,PluginSet>  pPackageQueueExtPlugins; 


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The table of the master extensions configurations.
   * 
   * Access to this field should be protected by a synchronized block.
   */ 
  private TreeMap<String,MasterExtensionConfig>  pMasterExtensions; 


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The cached table of filename suffix to editor mappings 
   * indexed by author user nameand file suffix. <P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */ 
  private DoubleMap<String,String,SuffixEditor>  pSuffixEditors;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The table of working area view names indexed by author user name. <P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */ 
  private TreeMap<String,TreeSet<String>>  pWorkingAreaViews;

  /**
   * Maintains the the current table of used node names. <P> 
   * 
   * All methods of this class are synchronized so no manual locking is required.
   */ 
  private NodeTree  pNodeTree; 


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The current number of node versions (checked-in and working) cached in memory.
   */ 
  private AtomicLong  pNodeCacheSize; 

  /**
   * The estimated memory size of a node version (in bytes).
   */ 
  private AtomicLong  pAverageNodeSize; 

  /**
   * The minimum and maximum amount of memory overhead which should be maintained at 
   * all times.  Overhead is defined as the difference between the max heap size and 
   * the total non-garbage heap allocation.  When the minimum amount of overhead is 
   * no longer available a node garbage collection will take place which frees enough
   * memory from the node version caches (checked-in and working) to raise the overhead
   * up to the maximum value.
   */ 
  private AtomicLong  pMinimumOverhead; 
  private AtomicLong  pMaximumOverhead; 

  /**
   * The minimum time a cycle of the node cache garbage collector loop should 
   * take (in milliseconds).
   */ 
  private AtomicLong  pNodeGCInterval; 


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
  //private PathMap<ReentrantReadWriteLock>  pCheckedInLocks;

  /**
   * The checked-in version related information of nodes indexed by fully resolved node 
   * name and revision number.
   */ 
  private HashMap<String,TreeMap<VersionID,CheckedInBundle>>  pCheckedInBundles;
  //private PathMap<TreeMap<VersionID,CheckedInBundle>>  pCheckedInBundles;


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
  // private PathMap<DoubleMap<String,String,ReentrantReadWriteLock>>  pWorkingLocks;

  /**
   * The working version related information of nodes indexed by fully resolved node 
   * name and working version node ID.
   */ 
  private HashMap<String,HashMap<NodeID,WorkingBundle>>  pWorkingBundles;
  /**
   * The working version related information of nodes indexed by fully resolved node 
   * path, working area author and view. 
   */ 
  // private PathMap<DoubleMap<String,String,WorkingBundle>>  pWorkingBundles;
 

  /**
   * The per-node downstream links locks indexed by fully resolved node name. <P> 
   * 
   * These locks protect the cached downstream links of each node. The per-node read-lock 
   * should be aquired for operations which will only access the downstream links of a node.
   * The per-node write-lock should be aquired when adding or removing links for a node.
   */
  private HashMap<String,ReentrantReadWriteLock>  pDownstreamLocks;
  //  private PathMap<ReentrantReadWriteLock>  pDownstreamLocks;
  
  /**
   * The table of downstream links indexed by fully resolved node name. <P> 
   * 
   * Access to this table should be protected by a synchronized block.
   */
  private HashMap<String,DownstreamLinks>  pDownstream;
  //private PathMap<DownstreamLinks>  pDownstream;
  
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the file manager should be run as a thread of plmaster(1).
   */ 
  private boolean  pInternalFileMgr; 
  
  /**
   * An internal instance of the file manager.
   * 
   * This field should not be access directly.  Instead a file manager connection should 
   * be obtained with the {@link #getFileMgrClient getFileMgrClient} method and returned
   * to the inactive pool with {@link #freeFileMgrClient freeFileMgrClient}.
   */
  private FileMgrDirectClient  pFileMgrDirectClient; 

  /**
   * A pool of inactive connections to the file manager daemon: <B>plfilemgr<B>(1). <P> 
   * 
   * This field should not be access directly.  Instead a file manager connection should 
   * be obtained with the {@link #getFileMgrClient getFileMgrClient} method and returned
   * to the inactive pool with {@link #freeFileMgrClient freeFileMgrClient}.
   */ 
  private Stack<FileMgrNetClient>  pFileMgrNetClients;
 

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

