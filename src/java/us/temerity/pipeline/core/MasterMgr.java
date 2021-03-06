// $Id: MasterMgr.java,v 1.336 2010/01/23 00:45:54 jim Exp $

package us.temerity.pipeline.core;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.Map.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;
import java.util.regex.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.core.exts.*;
import us.temerity.pipeline.event.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.message.*;
import us.temerity.pipeline.message.env.*;
import us.temerity.pipeline.message.misc.*;
import us.temerity.pipeline.message.node.*;
import us.temerity.pipeline.message.queue.*;
import us.temerity.pipeline.message.simple.*;
import us.temerity.pipeline.toolset.*;

/*------------------------------------------------------------------------------------------*/
/*   M A S T E R   M G R                                                                    */
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
 *       downstream/ <BR>
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
  extends BaseMgr
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
   * @param nodeReaderThreads
   *   The number of node reader threads to spawn.
   * 
   * @param nodeWriterThreads
   *   The number of node writer threads to spawn.
   * 
   * @param preserveOfflinedCache
   *   Whether to keep the offlined versions cache file after startup and reread instead of 
   *   rebuilding it during a database rebuild.
   * 
   * @param internalFileMgr
   *   Whether the file manager should be run as a thread of plmaster(1).
   * 
   * @param runtime
   *   The runtime controls.
   * 
   * @param session
   *   The session controls.
   * 
   * @throws PipelineException 
   *   If unable to properly initialize the manager.
   */
  public
  MasterMgr
  (
   boolean rebuildCache, 
   int nodeReaderThreads, 
   int nodeWriterThreads, 
   boolean preserveOfflinedCache, 
   boolean internalFileMgr, 
   MasterControls runtime, 
   SessionControls session
  )
    throws PipelineException 
  { 
    super(true); 

    pRebuildCache          = rebuildCache;
    pPreserveOfflinedCache = preserveOfflinedCache;
    pInternalFileMgr       = internalFileMgr;

    if(nodeReaderThreads < 1) 
      throw new PipelineException
        ("The number of node read threads (" + nodeReaderThreads + ") must be at least (1)!");
    pNodeReaderThreads = nodeReaderThreads;

    if(nodeWriterThreads < 1) 
      throw new PipelineException
        ("The number of node read threads (" + nodeWriterThreads + ") must be at least (1)!");
    pNodeWriterThreads = nodeWriterThreads;

    /* init runtime controls */ 
    {
      pMinFreeMemory   = new AtomicLong();
      pCacheGCInterval = new AtomicLong(); 
      pCacheFactor     = new AtomicReference<Double>(new Double(0.0)); 
      pCacheTrigger    = new CacheTrigger(0L); 
      
      pAnnotationCounters = new CacheCounters("Per-Node Annotations",     100L, 100L); 
      pCheckedInCounters  = new CacheCounters("Checked-In Node Versions", 500L, 500L); 
      pWorkingCounters    = new CacheCounters("Working Node Versions",    250L, 250L); 
      pCheckSumCounters   = new CacheCounters("Working Node CheckSums",   250L, 250L); 
      
      pRestoreCleanupInterval = new AtomicLong();
      pBackupSyncInterval     = new AtomicLong();

      setRuntimeControlsHelper(runtime);
    }

    pSessionControls = session;

    if(PackageInfo.sOsType != OsType.Unix)
      throw new IllegalStateException("The OS type must be Unix!");
    pNodeDir = PackageInfo.sNodePath.toFile();  // CHANGE THIS TO A Path!!!

    pShutdownJobMgrs   = new AtomicBoolean(false);
    pShutdownPluginMgr = new AtomicBoolean(false);

    /* BUG 2422 - Trailing Slash Breaks GLUE Parser */
    {
      Path p = new Path(new Path(PackageInfo.sNodePath, "etc"), "plfixbackslash.lock");
      File f = p.toFile();
      if(!f.exists()) 
        throw new PipelineException
          ("It appears that you have not yet run the \"plfixbackslash\" program to " + 
           "properly escape backslashes in existing GLUE format database files. " + 
           "See <http://temerity.us/community/forums/viewtopic.php?t=2422>."); 
    }

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Info,
       "Establishing Network Connections [PluginMgr FileMgr QueueMgr]...");

    {
      /* initialize the plugins */ 
      PluginMgrClient.init();

      /* initialize the internal file manager instance */ 
      if(pInternalFileMgr) {
	pFileMgrDirectClient = 
          new FileMgrDirectClient(runtime.getFileStatDir(), runtime.getCheckSumDir());
      }
      /* make a connection to the remote file manager */ 
      else {
	pFileMgrNetClients = new Stack<FileMgrNetClient>();
	
	FileMgrNetClient fclient = (FileMgrNetClient) acquireFileMgrClient();
	try {
	  fclient.waitForConnection(15000);
	}
	finally {
	  releaseFileMgrClient(fclient);
	}
      }
      
      /* make a connection to the queue manager */ 
      {
        pQueueMgrClients = new Stack<QueueMgrControlClient>();

        QueueMgrControlClient qclient = acquireQueueMgrClient();
        try {
          qclient.waitForConnection(15000);
        }
        finally {
          releaseQueueMgrClient(qclient);
        }
      }
    }

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Info,
       "Initializing [MasterMgr]...");

    /* Make sure that the root node directories exist. */ 
    makeRootDirs();

    /* validate startup state */ 
    if(pRebuildCache) {
      TaskTimer timer = 
        LogMgr.getInstance().taskBegin(LogMgr.Kind.Ops, "Removing Stale Caches...");

      removeLockFile();
      removeDownstreamLinksCache(); 
      removeNodeTreeCache();
      removeArchivesCache();

      LogMgr.getInstance().taskEnd(timer, LogMgr.Kind.Ops, "Removed"); 
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
      pDatabaseLock = new LoggedLock("Database");

      pAdminPrivileges = new AdminPrivileges();

      pNetworkLocks = new TreeMap<String,TrackedLock>();

      pArchiveFileLock    = new Object();
      pArchivedIn         = new TreeMap<String,TreeMap<VersionID,TreeSet<String>>>();
      pArchivedOn         = new TreeMap<String,Long>();
      pRestoredOn         = new TreeMap<String,TreeSet<Long>>();
      pOnlineOfflineLocks = new TreeMap<String,LoggedLock>();
      pOfflinedLock       = new Object();
      pOfflined           = null;
      pRestoreReqs        = new TreeMap<String,TreeMap<VersionID,RestoreRequest>>();

      pIntermediateTrigger = new Semaphore(0); 
      pIntermediate        = new TreeMap<String,TreeSet<VersionID>>(); 

      pBackupSyncTrigger = new Semaphore(0);
      pBackupSyncTarget  = new AtomicReference<Path>();

      pDefaultToolsetLock = new Object();
      pDefaultToolset     = null;
      pActiveToolsets     = new TreeSet<String>();
      pToolsets           = new TreeMap<String,TreeMap<OsType,Toolset>>();
      pToolsetPackages    = new TripleMap<String,OsType,VersionID,PackageVersion>();

      pEditorMenuLayouts            = new TreeMap<String,PluginMenuLayout>();
      pComparatorMenuLayouts        = new TreeMap<String,PluginMenuLayout>();
      pActionMenuLayouts            = new TreeMap<String,PluginMenuLayout>();
      pToolMenuLayouts              = new TreeMap<String,PluginMenuLayout>();  
      pArchiverMenuLayouts          = new TreeMap<String,PluginMenuLayout>();
      pMasterExtMenuLayouts         = new TreeMap<String,PluginMenuLayout>();
      pQueueExtMenuLayouts          = new TreeMap<String,PluginMenuLayout>();
      pAnnotationMenuLayouts        = new TreeMap<String,PluginMenuLayout>();
      pKeyChooserMenuLayouts        = new TreeMap<String,PluginMenuLayout>();
      pBuilderCollectionMenuLayouts = new TreeMap<String,PluginMenuLayout>();

      pPackageEditorPlugins            = new DoubleMap<String,VersionID,PluginSet>();
      pPackageComparatorPlugins        = new DoubleMap<String,VersionID,PluginSet>();
      pPackageActionPlugins            = new DoubleMap<String,VersionID,PluginSet>();
      pPackageToolPlugins              = new DoubleMap<String,VersionID,PluginSet>();
      pPackageArchiverPlugins          = new DoubleMap<String,VersionID,PluginSet>();
      pPackageMasterExtPlugins         = new DoubleMap<String,VersionID,PluginSet>();
      pPackageQueueExtPlugins          = new DoubleMap<String,VersionID,PluginSet>();
      pPackageAnnotationPlugins        = new DoubleMap<String,VersionID,PluginSet>();
      pPackageKeyChooserPlugins        = new DoubleMap<String,VersionID,PluginSet>();
      pPackageBuilderCollectionPlugins = new DoubleMap<String,VersionID,PluginSet>();

      pMasterExtensions = new TreeMap<String,MasterExtensionConfig>();

      pSuffixEditors = new DoubleMap<String,String,SuffixEditor>();

      pEventWriterInterval = new AtomicLong(5000L);
      pNodeEventFileLock   = new Object();
      pPendingEvents       = new ConcurrentLinkedQueue<BaseNodeEvent>();
      pNextEditorID        = 1L;
      pRunningEditors      = new TreeMap<Long,EditedNodeEvent>();

      pWorkingAreaViews = new TreeMap<String,TreeSet<String>>();
      pNodeTree         = new NodeTree();

      pAnnotationLocks = new TreeMap<String,LoggedLock>();
      pAnnotations     = new TreeMap<String,TreeMap<String,BaseAnnotation>>(); 
      pAnnotationsRead = new LinkedBlockingDeque<String>();

      pCheckedInLocks   = new TreeMap<String,LoggedLock>();
      pCheckedInBundles = new TreeMap<String,TreeMap<VersionID,CheckedInBundle>>();
      pCheckedInRead    = new LinkedBlockingDeque<String>();

      pWorkingLocks   = new TreeMap<NodeID,LoggedLock>();
      pWorkingBundles = new TreeMap<String,TreeMap<NodeID,WorkingBundle>>();    
      pWorkingRead    = new LinkedBlockingDeque<NodeID>();   

      pCheckSumLocks   = new TreeMap<NodeID,LoggedLock>();
      pCheckSumBundles = new DoubleMap<String,NodeID,CheckSumBundle>();    
      pCheckSumsRead   = new LinkedBlockingDeque<NodeID>();

      pDownstreamLocks = new TreeMap<String,LoggedLock>();
      pDownstream      = new TreeMap<String,DownstreamLinks>();

      pQueueSubmitLock = new Object();
    }

    /* perform startup I/O operations */ 
    try {
      initPrivileges();
      initArchives();
      initToolsets();
      initMasterExtensions();
      initWorkingAreas();
      initNodeDatabase(); 
      readNextIDs();
    }
    catch(Exception ex) {
      LogMgr.getInstance().logAndFlush
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 ex.getMessage());

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
    TaskTimer timer = 
      LogMgr.getInstance().taskBegin(LogMgr.Kind.Ops, "Loading Privileges...");   

    {
      pAdminPrivileges.readAll();
      updateAdminPrivileges();
    }

    LogMgr.getInstance().taskEnd(timer, LogMgr.Kind.Ops, "Loaded"); 
  }

  /**
   * Update the other servers with the latest copy of the administrative privileges.
   */ 
  private void 
  updateAdminPrivileges() 
    throws PipelineException
  {
    QueueMgrControlClient qclient = acquireQueueMgrClient();
    try {
      qclient.pushAdminPrivileges(pAdminPrivileges);    
    }
    finally {
      releaseQueueMgrClient(qclient);
    }

    PluginMgrControlClient pclient = new PluginMgrControlClient();
    try {
      pclient.pushAdminPrivileges(pAdminPrivileges);
    }
    finally {
      pclient.disconnect();
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
    dirs.add(new File(pNodeDir, "annotations"));
    dirs.add(new File(pNodeDir, "repository"));
    dirs.add(new File(pNodeDir, "working"));
    dirs.add(new File(pNodeDir, "toolsets/packages"));
    dirs.add(new File(pNodeDir, "toolsets/toolsets"));
    dirs.add(new File(pNodeDir, "toolsets/plugins/packages"));
    dirs.add(new File(pNodeDir, "toolsets/plugins/toolsets"));
    dirs.add(new File(pNodeDir, "etc"));
    dirs.add(new File(pNodeDir, "etc/suffix-editors"));
    dirs.add(new File(pNodeDir, "events/nodes"));
    dirs.add(new File(pNodeDir, "events/authors"));
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
      /* rebuild (or reread) offlined versions cache */ 
      {
	File offlined = new File(pNodeDir, "archives/offlined");
	if(pPreserveOfflinedCache && offlined.exists()) {
	  readOfflined();
	}
	else {
          LogMgr.getInstance().logAndFlush
            (LogMgr.Kind.Ops, LogMgr.Level.Info,
             "Starting Offlined Cache Rebuild (in background)...");    
          
          pRebuildOfflinedCacheTask = new RebuildOfflinedCacheTask();
          pRebuildOfflinedCacheTask.start(); 
	}
      }

      /* scan archive volume GLUE files */ 
      {
        TaskTimer timer = 
          LogMgr.getInstance().taskBegin(LogMgr.Kind.Ops, "Rebuilding ArchiveOn Cache...");   
	
	{
	  File dir = new File(pNodeDir, "archives/manifests");
	  File files[] = dir.listFiles(); 
          if(files != null) {
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
          else {
            LogMgr.getInstance().logAndFlush
              (LogMgr.Kind.Ops, LogMgr.Level.Severe,
               "Unable to determine the contents of the Archive Manifests directory " + 
               "(" + dir + ")!"); 
          }
        }
	
        LogMgr.getInstance().taskEnd(timer, LogMgr.Kind.Ops, "Rebuilt"); 
      }
 
      /* scan restore output files */ 
      {
        TaskTimer timer = 
          LogMgr.getInstance().taskBegin(LogMgr.Kind.Ops, "Rebuilding RestoredOn Cache...");

	{
	  File dir = new File(pNodeDir, "archives/output/restore");
	  File files[] = dir.listFiles(); 
          if(files != null) {
            int wk;
            for(wk=0; wk<files.length; wk++) {
              String fname = files[wk].getName();
              for(String aname : pArchivedOn.keySet()) {
                if(fname.startsWith(aname)) {
                  try {
                    Long stamp = Long.parseLong(fname.substring(aname.length()+1));
                    TreeSet<Long> stamps = pRestoredOn.get(aname);
                    if(stamps == null) {
                      stamps = new TreeSet<Long>();
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
          else {
            LogMgr.getInstance().logAndFlush
              (LogMgr.Kind.Ops, LogMgr.Level.Severe,
               "Unable to determine the contents of the Restore Output directory " + 
               "(" + dir + ")!"); 
          }
        }

        LogMgr.getInstance().taskEnd(timer, LogMgr.Kind.Ops, "Rebuilt"); 
      }
    }
    else {
      TaskTimer timer = 
        LogMgr.getInstance().taskBegin(LogMgr.Kind.Ops, "Loading Archive Caches...");   

      readOfflined();
      readArchivedIn();
      readArchivedOn();
      readRestoredOn();

      removeArchivesCache();

      LogMgr.getInstance().taskEnd(timer, LogMgr.Kind.Ops, "Loaded"); 
    }

    readRestoreReqs();
  }
  

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Create the lock file.
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
    TaskTimer timer = 
      LogMgr.getInstance().taskBegin(LogMgr.Kind.Ops, "Loading Toolsets...");   

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
    
    pDefaultQueueExtMenuLayout = 
      readPluginMenuLayout(null, "queue extension", 
			   pQueueExtMenuLayouts); 

    pDefaultAnnotationMenuLayout = 
      readPluginMenuLayout(null, "annotation", 
			   pAnnotationMenuLayouts); 

    pDefaultKeyChooserMenuLayout = 
      readPluginMenuLayout(null, "key chooser", 
			   pKeyChooserMenuLayouts); 
    
    pDefaultBuilderCollectionMenuLayout =
      readPluginMenuLayout(null, "builder collection", 
                           pBuilderCollectionMenuLayouts);

    /* initialize toolsets */ 
    {
      File dir = new File(pNodeDir, "toolsets/toolsets");
      File tsets[] = dir.listFiles(); 
      if(tsets != null) {
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

              readPluginMenuLayout(tname, "annotation", 
                                   pAnnotationMenuLayouts); 

              readPluginMenuLayout(tname, "key chooser", 
                                   pKeyChooserMenuLayouts);
	    
              readPluginMenuLayout(tname, "builder collection", 
                                   pBuilderCollectionMenuLayouts); 
            }
          }
        }
      }
      else {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Ops, LogMgr.Level.Severe,
           "Unable to determine the contents of the Toolsets directory (" + dir + ")!"); 
      }        
    }

    /* initialize package keys and plugin tables */ 
    {
      File dir = new File(pNodeDir, "toolsets/packages");
      File pkgs[] = dir.listFiles(); 
      if(pkgs != null) {
        int pk;
        for(pk=0; pk<pkgs.length; pk++) {
          if(pkgs[pk].isDirectory()) {
            String pname = pkgs[pk].getName();
            for(OsType os : OsType.all()) {
              File osdir = new File(pkgs[pk], os.toString());
              if(osdir.isDirectory()) {
                File vsns[] = osdir.listFiles(); 
                if(vsns != null) {
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
                        
                        readPackagePlugins(pname, vid, "annotation", 
                                           pPackageAnnotationPlugins);
                        
                        readPackagePlugins(pname, vid, "key chooser", 
                                           pPackageKeyChooserPlugins);
                        
                        readPackagePlugins(pname, vid, "builder collection", 
                                           pPackageBuilderCollectionPlugins);
                      }
                    }
                  }
                }
                else {
                  LogMgr.getInstance().logAndFlush
                    (LogMgr.Kind.Ops, LogMgr.Level.Severe,
                     "Unable to determine the contents of the Toolset Package Version " + 
                     "directory (" + osdir + ")!"); 
                }   
              }
            }
          }
        }
      }
      else {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Ops, LogMgr.Level.Severe,
           "Unable to determine the contents of the Toolset Packages directory " + 
           "(" + dir + ")!"); 
      }   
    }

    LogMgr.getInstance().taskEnd(timer, LogMgr.Kind.Ops, "Loaded"); 
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Initialize the server extensions. 
   */
  private void 
  initMasterExtensions()
    throws PipelineException
  {
    TaskTimer timer = 
      LogMgr.getInstance().taskBegin(LogMgr.Kind.Ops, "Loading Extensions...");   

    {
      readMasterExtensions();
      
      synchronized(pMasterExtensions) {
	for(MasterExtensionConfig config : pMasterExtensions.values()) 
	  doPostExtensionEnableTask(config);
      }
    }

    LogMgr.getInstance().taskEnd(timer, LogMgr.Kind.Ops, "Loaded"); 
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the table of working area views.
   */ 
  private void 
  initWorkingAreas() 
  {
    TaskTimer timer = 
      LogMgr.getInstance().taskBegin(LogMgr.Kind.Ops, "Loading Working Areas...");   

    {
      File dir = new File(pNodeDir, "working");
      File authors[] = dir.listFiles(); 
      if(authors != null) {
        int ak;
        for(ak=0; ak<authors.length; ak++) {
          if(!authors[ak].isDirectory())
            throw new IllegalStateException
              ("Non-directory file found in the root working area directory!"); 
          String author = authors[ak].getName();
	
          File views[] = authors[ak].listFiles();  
          if(views != null) {
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
          else {
            LogMgr.getInstance().logAndFlush
              (LogMgr.Kind.Ops, LogMgr.Level.Severe,
               "Unable to determine the contents of the Working Area directory " + 
               "(" + authors[ak] + ") for user (" + author + ")!");  
          }   
        }
      }
      else {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Ops, LogMgr.Level.Severe,
           "Unable to determine the contents of the Root Working Area directory " + 
           "(" + dir + ")!"); 
      }          
    }
    
    LogMgr.getInstance().taskEnd(timer, LogMgr.Kind.Ops, "Loaded"); 
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
   * Load or rebuild the initial node name tree and downstream links caches by recursively
   * searching the file system for node related files.
   */
  private void 
  initNodeDatabase() 
    throws PipelineException 
  {
    if(pRebuildCache) {
      {
        File dir = new File(pNodeDir, "downstream");
        if(dir.isDirectory()) 
          throw new PipelineException
            ("Somehow the downstream links directory (" + dir + ") already exists!");
        
        if(!dir.mkdir()) 
          throw new PipelineException
            ("Unable to create the downstream links directory (" + dir + ")!");
      }

      TaskTimer timer = 
        LogMgr.getInstance().taskBegin(LogMgr.Kind.Ops, "Rebuilding Node Related Caches...");

      initCheckedInNodeDatabase();
      pIntermediateTrigger.release(); 
      initWorkingNodeDatabase(); 

      pNodeTree.initHidden();

      LogMgr.getInstance().taskEnd(timer, LogMgr.Kind.Ops, "All Rebuilt"); 
    }
    else {
      TaskTimer timer = 
        LogMgr.getInstance().taskBegin(LogMgr.Kind.Ops, "Loading Node Tree Cache...");   

      pNodeTree.readGlueFile(new File(pNodeDir, "etc/node-tree"));
      removeNodeTreeCache();

      LogMgr.getInstance().taskEnd(timer, LogMgr.Kind.Ops, "Loaded"); 
    }
    
    pNodeTree.logNodeTree();
  }

  /**
   * Spawn multiple threads to read the checked-in node directories in order to rebuild
   * the cached for node names and downstream links.
   */ 
  private void 
  initCheckedInNodeDatabase() 
    throws PipelineException
  {
    LinkedBlockingQueue<String> found = new LinkedBlockingQueue<String>();
    AtomicBoolean uponError = new AtomicBoolean(false);
    AtomicBoolean doneScanning = new AtomicBoolean(false);

    CheckedInScannerTask scanner = new CheckedInScannerTask(found, uponError, doneScanning);
    scanner.start();
    
    ArrayList<CheckedInReaderTask> readers = new ArrayList<CheckedInReaderTask>();
    int wk;
    for(wk=0; wk<pNodeReaderThreads; wk++) {
      CheckedInReaderTask task = new CheckedInReaderTask(found, uponError, doneScanning);
      task.start();
      readers.add(task); 
    }

    try {
      scanner.join();
    }
    catch(InterruptedException ex) {
      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Ops, LogMgr.Level.Severe,
         "Interrupted while scanning checked-in node files:\n" + ex.getMessage());
      uponError.set(true); 
    }

    for(CheckedInReaderTask task : readers) {
      try {
        if(uponError.get()) 
          task.interrupt();
        task.join();
      }
      catch(InterruptedException ex) {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Ops, LogMgr.Level.Severe,
           "Interrupted while reading checked-in node files:\n" + ex.getMessage());
        uponError.set(true); 
      }
    }

    if(uponError.get())
      throw new PipelineException
        ("Checked-In Node Intialization Aborted."); 
  }

  /**
   * Spawn multiple threads to read the working node directories in order to rebuild
   * the cached for node names and downstream links.
   */ 
  private void 
  initWorkingNodeDatabase() 
    throws PipelineException
  {
    LinkedBlockingQueue<NodeID> found = new LinkedBlockingQueue<NodeID>();
    AtomicBoolean uponError = new AtomicBoolean(false);
    AtomicBoolean doneScanning = new AtomicBoolean(false);

    WorkingScannerTask scanner = new WorkingScannerTask(found, uponError, doneScanning);
    scanner.start();
    
    ArrayList<WorkingReaderTask> readers = new ArrayList<WorkingReaderTask>();
    int wk;
    for(wk=0; wk<pNodeReaderThreads; wk++) {
      WorkingReaderTask task = new WorkingReaderTask(found, uponError, doneScanning);
      task.start();
      readers.add(task); 
    }

    try {
      scanner.join();
    }
    catch(InterruptedException ex) {
      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Ops, LogMgr.Level.Severe,
         "Interrupted while scanning working node files:\n" + ex.getMessage());
      uponError.set(true); 
    }

    for(WorkingReaderTask task : readers) {
      try {
        if(uponError.get()) 
          task.interrupt();
        task.join();
      }
      catch(InterruptedException ex) {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Ops, LogMgr.Level.Severe,
           "Interrupted while reading working node files:\n" + ex.getMessage());
        uponError.set(true); 
      }
    }

    if(uponError.get()) 
      throw new PipelineException
        ("Working Node Intialization Aborted."); 
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
    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Info,
       "Closing Server Connections...");

    /* close the connection(s) to the file manager */ 
    if(!pInternalFileMgr && (pFileMgrNetClients != null)) {
      boolean first = true;
      while(!pFileMgrNetClients.isEmpty()) {
	FileMgrNetClient client = pFileMgrNetClients.pop();
	try {
          if(first) 
            client.shutdown();
          else
            client.disconnect();
          first = false;
	}
	catch(PipelineException ex) {
	  LogMgr.getInstance().logAndFlush
	    (LogMgr.Kind.Net, LogMgr.Level.Warning,
	     ex.getMessage());
	}
      }
    }

    /* close the connection(s) to the queue manager */ 
    if(pQueueMgrClients != null) {
      boolean first = true;
      while(!pQueueMgrClients.isEmpty()) {
	QueueMgrControlClient client = pQueueMgrClients.pop();
	try {
          if(first) 
            client.shutdown(pShutdownJobMgrs.get());
          else 
            client.disconnect();
          first = false;
	}
	catch(PipelineException ex) {
	  LogMgr.getInstance().logAndFlush
	    (LogMgr.Kind.Net, LogMgr.Level.Warning,
	     ex.getMessage());
	}
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
      LogMgr.getInstance().logAndFlush
	(LogMgr.Kind.Net, LogMgr.Level.Warning,
	 ex.getMessage());
    }

    /* give the sockets time to disconnect cleanly */ 
    try {
      Thread.sleep(1000);
    }
    catch(InterruptedException ex) {
    }

    /* write the cache files */ 
    try {
      {
        TaskTimer timer = 
          LogMgr.getInstance().taskBegin(LogMgr.Kind.Ops, "Writing Archive Caches...");

        writeArchivedIn();
        writeArchivedOn();
        writeRestoredOn();

        LogMgr.getInstance().taskEnd(timer, LogMgr.Kind.Ops, "Saved"); 
      }

      {
        boolean saveOffline = false;
        if(pRebuildOfflinedCacheTask != null) {
          if(pRebuildOfflinedCacheTask.isAlive()) {
            try {
              pRebuildOfflinedCacheTask.interrupt();
              pRebuildOfflinedCacheTask.join();  
            }
            catch(InterruptedException ex) {
            }
          }
          else {
            saveOffline = true;
          }
        }
        else {
          saveOffline = true;
        }
        
        if(saveOffline) {
          TaskTimer timer = 
            LogMgr.getInstance().taskBegin(LogMgr.Kind.Ops, "Writing Offline Cache...");

          writeOfflined();
          
          LogMgr.getInstance().taskEnd(timer, LogMgr.Kind.Ops, "Saved"); 
        }
      }

      {
        TaskTimer timer = 
          LogMgr.getInstance().taskBegin(LogMgr.Kind.Ops, "Writing Downstream Cache...");

        writeAllDownstreamLinks();

        LogMgr.getInstance().taskEnd(timer, LogMgr.Kind.Ops, "All Saved"); 
      }

      {
        TaskTimer timer = 
          LogMgr.getInstance().taskBegin(LogMgr.Kind.Ops, "Writing Node Tree Cache...");

        pNodeTree.writeGlueFile(new File(pNodeDir, "etc/node-tree"));

        LogMgr.getInstance().taskEnd(timer, LogMgr.Kind.Ops, "Saved"); 
      }
    }
    catch(Exception ex) {
      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Ops, LogMgr.Level.Severe,
         "Failed to Save Caches:\n  " + ex.getMessage());

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
      LogMgr.getInstance().logAndFlush
	(LogMgr.Kind.Ops, LogMgr.Level.Warning,
	 ex.getMessage());
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
   * This should only be called by {@link #shutdown shutdown}.
   * 
   * If any I/O problems are encountered, the entire downstream links directory is removed
   * so that it will be rebuilt from scratch the next time the server is started. 
   */ 
  private void 
  writeAllDownstreamLinks()
  {
    LinkedBlockingQueue<DownstreamLinks> found = 
      new LinkedBlockingQueue<DownstreamLinks>(pDownstream.values());
    AtomicBoolean uponError = new AtomicBoolean(false);

    ArrayList<DownstreamWriterTask> writers = new ArrayList<DownstreamWriterTask>();
    int wk;
    for(wk=0; wk<pNodeWriterThreads; wk++) {
      DownstreamWriterTask task = new DownstreamWriterTask(found, uponError);
      task.start();
      writers.add(task); 
    }

    for(DownstreamWriterTask task : writers) {
      try {
        if(uponError.get()) 
          task.interrupt();
        task.join();
      }
      catch(InterruptedException ex) {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Ops, LogMgr.Level.Severe,
           "Interrupted while writing downstream files:\n" + ex.getMessage());
        uponError.set(true); 
      }
    }

    /* remove the entire downstream directory on failure */ 
    if(uponError.get()) {
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
  /*   O F F L I N E D   H E L P E R S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  /** 
   * Whether the offlined cache is currently valid. 
   */ 
  private boolean
  isOfflineCacheValid()
  {
    synchronized(pOfflinedLock) {
      return (pOfflined != null);
    }
  }

  /** 
   * Determine the revision numbers (if any) of the offlined versions of the given node.<P> 
   * 
   * The onlineOffline read-lock at least should be acquired before calling this method.
   * 
   * @param timer
   *   The current operation timer.
   * 
   * @param name
   *   The fully resolved node name.
   * 
   * @return 
   *   The offlined revision number or (null) if none are offline.
   */ 
  private TreeSet<VersionID>
  getOfflinedVersions
  (
   TaskTimer timer,
   String name
  )
    throws PipelineException 
  { 
    if(isOfflineCacheValid()) {
      timer.acquire();
      synchronized(pOfflinedLock) {
        timer.resume();

        TreeSet<VersionID> offline = pOfflined.get(name);
        if(offline != null) 
          return new TreeSet<VersionID>(offline);
        else 
          return null;
      }
    }
    else {
      TreeSet<VersionID> offline = null;
      {
        FileMgrClient fclient = acquireFileMgrClient();
        try {
          offline = fclient.getOfflinedNodeVersions(name);
        }
        finally {
          releaseFileMgrClient(fclient);
        }
      }

      timer.acquire();
      LoggedLock lock = getCheckedInLock(name);
      lock.acquireReadLock();
      try {
        timer.resume();	
        
        TreeMap<VersionID,CheckedInBundle> checkedIn = getCheckedInBundles(name);
        for(Map.Entry<VersionID,CheckedInBundle> entry : checkedIn.entrySet()) {
          if(entry.getValue().getVersion().isIntermediate()) 
            offline.remove(entry.getKey());
        }
      }
      catch(PipelineException ex) {
        // ignore if no checked-in versions exist... 
      }
      finally {
        lock.releaseReadLock();
      }  

      if(!offline.isEmpty()) 
        return offline;
      else 
        return null;
    }
  }

  /**
   * Whether the given version of a node is offline.<P> 
   * 
   * The onlineOffline read-lock at least should be acquired before calling this method.
   * 
   * @param timer
   *   The current operation timer.
   * 
   * @param name
   *   The fully resolved node name.
   * 
   * @param vid
   *   The revision number to test.
   */ 
  private boolean 
  isOffline
  (
   TaskTimer timer,
   String name,
   VersionID vid
  ) 
    throws PipelineException 
  {
    TreeSet<VersionID> offlined = getOfflinedVersions(timer, name); 
    return ((offlined != null) && offlined.contains(vid));
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
  pullAdminPrivileges()
  {
    TaskTimer timer = new TaskTimer("MasterMgr.pullAdminPrivileges()");
    
    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      return pAdminPrivileges.getPullResponse(timer);
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }
  }
  
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
    
    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      return pAdminPrivileges.getWorkGroupsRsp(timer);
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }
  }
  
  /**
   * Set the work groups used to determine the scope of administrative privileges. <P> 
   * 
   * This operation requires Master Admin privileges 
   * (see {@link Privileges#isMasterAdmin isMasterAdmin} 
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

    /* pre-op tests */
    SetWorkGroupsExtFactory factory = new SetWorkGroupsExtFactory(req.getGroups());
    try {
      performExtensionTests(timer, factory);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      pAdminPrivileges.setWorkGroupsFromReq(timer, req);
      updateAdminPrivileges();

      /* post-op tasks */ 
      startExtensionTasks(timer, factory);

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      pDatabaseLock.releaseReadLock();
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
    
    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      return pAdminPrivileges.getPrivilegesRsp(timer);
    }
    finally {
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      pAdminPrivileges.editPrivilegesFromReq(timer, req);
      updateAdminPrivileges();
      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      pDatabaseLock.releaseReadLock();
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
    
    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      return pAdminPrivileges.getPrivilegeDetailsRsp(timer, req);
    }
    finally {
      pDatabaseLock.releaseReadLock();
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
      for(LogMgr.Kind kind : LogMgr.Kind.all()) 
        lc.setLevel(kind, mgr.getLevel(kind));
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
      LogMgr mgr = LogMgr.getInstance(); 
      for(LogMgr.Kind kind : LogMgr.Kind.all()) {
        LogMgr.Level level = lc.getLevel(kind);
        if(level != null) 
          mgr.setLevel(kind, level);
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

    try {
      MasterControls controls = null;
      {
        FileMgrClient fclient = acquireFileMgrClient();
        try {
          controls = fclient.getRuntimeControls(); 
        }
        finally {
          releaseFileMgrClient(fclient);
        }
      }
      controls.setMinFreeMemory(pMinFreeMemory.get());
      controls.setCacheGCInterval(pCacheGCInterval.get()); 
      controls.setCacheGCMisses(pCacheTrigger.getOpsUntil()); 
      controls.setCacheFactor(pCacheFactor.get()); 

      controls.setRepoCacheSize(pCheckedInCounters.getMinimum());    
      controls.setWorkCacheSize(pWorkingCounters.getMinimum());  
      controls.setCheckCacheSize(pCheckSumCounters.getMinimum());   
      controls.setAnnotCacheSize(pAnnotationCounters.getMinimum()); 

      controls.setRestoreCleanupInterval(pRestoreCleanupInterval.get()); 

      return new MiscGetMasterControlsRsp(timer, controls);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
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
      setRuntimeControlsHelper(controls);

      FileMgrClient fclient = acquireFileMgrClient();
      try {
        fclient.setRuntimeControls(controls);
      }
      finally {
        releaseFileMgrClient(fclient);
      }
      
      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
  }
  
  /**
   * Helper method for setting the controls directly and logging the changes.
   *
   * @param controls
   *   The runtime controls.
   */ 
  private void 
  setRuntimeControlsHelper
  (
   MasterControls controls
  )
    throws PipelineException
  {
    StringBuilder buf = new StringBuilder();
    buf.append
      ("--- MasterMgr Runtime Controls ---------------------------------------------\n");
    
    {
      Long bytes = controls.getMinFreeMemory();
      long maxMem = Runtime.getRuntime().maxMemory();  
      if(bytes != null) {
        long lower = (long) (maxMem * 0.1);
        long upper = (long) (maxMem * 0.4);
        
        if(bytes < lower)
          throw new PipelineException
            ("The minimum free memory (" + ByteSize.longToFloatString(bytes) + ") must " +
             "be at least 10% (" + ByteSize.longToFloatString(lower) + ") of " + 
             "the maximum heap size (" + ByteSize.longToFloatString(maxMem) + ")!");

        if(bytes > upper)
          throw new PipelineException
            ("The minimum free memory (" + ByteSize.longToFloatString(bytes) + ") cannot " +
             "be more than 40% (" + ByteSize.longToFloatString(upper) + ") of " + 
             "the maximum heap size (" +ByteSize.longToFloatString(maxMem) + ")!");
        
        pMinFreeMemory.set(bytes); 
      }
      else {
        pMinFreeMemory.set(maxMem / 7L);  // 14.2% of maximum
      }

      bytes = pMinFreeMemory.get();
      buf.append("       Min Free Memory : " + bytes + " " +
                 "(" + ByteSize.longToFloatString(bytes) + ")\n");
    }
    
    {
      Long gcInterval = controls.getCacheGCInterval();
      if(gcInterval != null) 
        pCacheGCInterval.set(gcInterval);
      gcInterval = pCacheGCInterval.get();
      buf.append("     Cache GC Interval : " + gcInterval + " " + 
                 "(" + TimeStamps.formatInterval(gcInterval) + ")\n");
    }
    
    {
      Long gcMisses = controls.getCacheGCMisses();     
      if(gcMisses != null) 
        pCacheTrigger.setOpsUntil(gcMisses);
      buf.append("       Cache GC Misses : " + pCacheTrigger.getOpsUntil() + "\n");
    }

    {
      Double cacheFactor = controls.getCacheFactor();       
      if(cacheFactor != null) 
        pCacheFactor.set(cacheFactor); 
      buf.append("          Cache Factor : " + pCacheFactor.get() + "\n"); 
    }

    {
      Long repoCacheSize = controls.getRepoCacheSize(); 
      if(repoCacheSize != null) 
        pCheckedInCounters.setLowerBounds(repoCacheSize);
      buf.append("       Repo Cache Size : " + pCheckedInCounters.getMinimum() + "\n"); 
    }

    {
      Long workCacheSize = controls.getWorkCacheSize(); 
      if(workCacheSize != null) 
        pWorkingCounters.setLowerBounds(workCacheSize);
      buf.append("       Work Cache Size : " + pWorkingCounters.getMinimum() + "\n"); 
    }

    {
      Long checkCacheSize = controls.getCheckCacheSize(); 
      if(checkCacheSize != null) 
        pCheckSumCounters.setLowerBounds(checkCacheSize);
      buf.append("      Check Cache Size : " + pCheckSumCounters.getMinimum() + "\n"); 
    }

    {
      Long annotCacheSize = controls.getAnnotCacheSize(); 
      if(annotCacheSize != null) 
        pAnnotationCounters.setLowerBounds(annotCacheSize);
      buf.append("      Annot Cache Size : " + pAnnotationCounters.getMinimum() + "\n"); 
    }

    {
      Long interval = controls.getRestoreCleanupInterval();
      if(interval != null) 
        pRestoreCleanupInterval.set(interval);
      interval = pRestoreCleanupInterval.get();
      buf.append("       Restore Cleanup : " + interval + " " + 
                 "(" + TimeStamps.formatInterval(interval) + ")\n");
    }
    
    {
      Long interval = controls.getBackupSyncInterval();
      if(interval != null) 
        pBackupSyncInterval.set(interval);
      interval = pBackupSyncInterval.get(); 
      buf.append("  Backup Sync Interval : " + interval + " " + 
                 "(" + TimeStamps.formatInterval(interval) + ")\n");
    }

    buf.append
      ("----------------------------------------------------------------------------"); 
    
    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       buf.toString());
  }


  /*----------------------------------------------------------------------------------------*/
  /*   L O C K S                                                                            */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Block until able to acquire a lock with the given name. <P> 
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to acquire the lock.
   */
  public Object
  acquireLock
  (
   MiscLockByNameReq req 
  ) 
  {   
    String name = req.getName();
        
    TaskTimer timer = 
      new TaskTimer("MasterMgr.acquireLock(): " + name);

    try {
      TrackedLock lock = null;
      timer.acquire();
      synchronized(pNetworkLocks) {
        timer.resume();	
        
        lock = pNetworkLocks.get(name);
        if(lock == null) {
          lock = new TrackedLock(name);
          pNetworkLocks.put(name, lock);
        }
      }
      
      return new MiscGetLockRsp(timer, lock.acquireLock(req));
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
  }

  /** 
   * Attempt to acquire a lock with the given name, returns immediately. 
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to acquire the lock.
   */
  public Object
  tryLock
  (
   MiscLockByNameReq req 
  ) 
  {   
    String name = req.getName();
        
    TaskTimer timer = 
      new TaskTimer("MasterMgr.tryLock(): " + name);

    TrackedLock lock = null;
    timer.acquire();
    synchronized(pNetworkLocks) {
      timer.resume();	
      
      lock = pNetworkLocks.get(name);
      if(lock == null) {
        lock = new TrackedLock(name);
        pNetworkLocks.put(name, lock);
      }
    }
    
    return new MiscGetLockRsp(timer, lock.tryLock(req));
  }

  /** 
   * Release a lock with the given name. 
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to release the lock.
   */
  public Object
  releaseLock
  (
   MiscReleaseLockReq req 
  ) 
  {   
    String name = req.getName();
    Long lockID = req.getLockID();

    TaskTimer timer = 
      new TaskTimer("MasterMgr.releaseLock(): " + name);

    try {
      TrackedLock lock = null;
      timer.acquire();
      synchronized(pNetworkLocks) {
        timer.resume();	
        
        lock = pNetworkLocks.get(name);
        if(lock == null) 
          throw new PipelineException
            ("No lock named (" + name + ") exists to be released!");
      }

      lock.releaseLock(req, lockID);  

      return new SuccessRsp(timer); 
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
  }

  /** 
   * Force the release of a lock with the given name.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to release the lock.
   */
  public Object
  breakLock
  (
   MiscBreakLockReq req 
  ) 
  {   
    String name = req.getName();

    TaskTimer timer = 
      new TaskTimer("MasterMgr.breakLock(): " + name);

    if(!pAdminPrivileges.isMasterAdmin(req))
      return new FailureRsp
        (timer, "Only a user with Master Admin privileges may break locks!"); 
      
    try {
      TrackedLock lock = null;
      timer.acquire();
      synchronized(pNetworkLocks) {
        timer.resume();	
        
        lock = pNetworkLocks.get(name);
        if(lock == null) 
          throw new PipelineException
            ("No lock named (" + name + ") exists to be released!");
      }

      LogMgr.getInstance().log
        (LogMgr.Kind.Ext, LogMgr.Level.Warning,
         "The lock (" + name + ") was forcibly released by the user " + 
         "(" + req.getRequestor() + ")!");

      lock.breakLock(req); 

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
  }

  /** 
   * Return whether the given lock is currently held.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine if the lock is held.
   */
  public Object
  isLocked
  (
   MiscLockByNameReq req 
  ) 
  {   
    String name = req.getName();

    TaskTimer timer = 
      new TaskTimer("MasterMgr.isLocked(): " + name);

    try {
      TrackedLock lock = null;
      timer.acquire();
      synchronized(pNetworkLocks) {
        timer.resume();	
        
        lock = pNetworkLocks.get(name);
        if(lock == null) 
          throw new PipelineException
            ("No lock named (" + name + ") exists!");
      }

      LogMgr.getInstance().log
        (LogMgr.Kind.Ext, LogMgr.Level.Warning,
         "The lock (" + name + ") was forcibly released by the user " + 
         "(" + req.getRequestor() + ")!");

      return new MiscIsLockedRsp(timer, lock.isLocked());
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
  }

  /** 
   * Return the names of all currently existing locks. <P>
   * 
   * @return
   *   <CODE>MiscGetLockNamesRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the locking state.
   */
  public Object
  getLockInfo() 
  {   
    TaskTimer timer = new TaskTimer();
    
    timer.acquire();
    synchronized(pNetworkLocks) {
      timer.resume();	
      TreeMap<String,RequestInfo> result = new TreeMap<String,RequestInfo>();
      for(TrackedLock lock : pNetworkLocks.values()) {
        if((lock != null) && lock.isLocked()) {
          RequestInfo info = lock.getLockerInfo(); 
          if(info != null) 
            result.put(lock.getName(), info);
        }
      }

      return new MiscGetLockInfoRsp(timer, result);
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   T O O L S E T S                                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the given toolset name actually exists.
   * 
   * @param tname
   *   The toolset name.
   */ 
  private boolean 
  isValidToolsetName
  (
   String tname
  ) 
  {
    synchronized(pToolsets) {
      return pToolsets.containsKey(tname); 
    }
  }

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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      if(!pAdminPrivileges.isDeveloper(req)) 
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
      
      timer.acquire();
      synchronized(pDefaultToolsetLock) {
	timer.resume();	 
	
	pDefaultToolset = tname;
	writeDefaultToolset();
      }
      
      timer.acquire();
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
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      synchronized(pActiveToolsets) {
	timer.resume();
	
	return new MiscGetActiveToolsetNamesRsp(timer, new TreeSet<String>(pActiveToolsets));
      }
    }
    finally {
      pDatabaseLock.releaseReadLock();
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
    String tname     = req.getName();
    boolean isActive = req.isActive();

    String active = (isActive ? "active" : "inactive");

    TaskTimer timer = 
      new TaskTimer("MasterMgr.setActiveToolsetName(): " + tname + " [" + active + "]");

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      if(!pAdminPrivileges.isDeveloper(req)) 
	throw new PipelineException
	  ("Only a user with Master Admin privileges may change the active status " + 
	   "of a toolset!");

      synchronized(pDefaultToolsetLock) {
	timer.resume();
        if((pDefaultToolset != null) && pDefaultToolset.equals(tname) && !isActive)
          throw new PipelineException
            ("The default toolset (" + tname + ") cannot be made inactive!");
      }

      timer.acquire();
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
      
      timer.acquire();
      synchronized(pActiveToolsets) {
	timer.resume();	 
	
	boolean changed = false;
	if(isActive) {
	  if(!pActiveToolsets.contains(tname)) {
	    pActiveToolsets.add(tname);
	    changed = true;
	  }
	}
	else {
	  if(pActiveToolsets.contains(tname)) {
	    pActiveToolsets.remove(tname);
	    changed = true;
	  }
	}
	
	if(changed) 
	  writeActiveToolsets();
      }
      
      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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
    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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
    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
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
    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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
    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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
    String author = req.getAuthor();
    String tname  = req.getName();
    String desc   = req.getDescription();
    OsType os     = req.getOsType();

    TaskTimer timer = new TaskTimer("MasterMgr.createToolset(): " + tname);
    
    /* lookup the packages */  
    Collection<PackageVersion> packages = new ArrayList<PackageVersion>();
    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
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
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }
      
    /* pre-op tests */
    CreateToolsetExtFactory factory = 
      new CreateToolsetExtFactory(author, tname, desc, packages, os); 
    try {
      performExtensionTests(timer, factory);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      if(!pAdminPrivileges.isDeveloper(req)) 
	throw new PipelineException
	  ("Only a user with Master Admin privileges may create new toolsets!");

      synchronized(pToolsets) {
	timer.resume();

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

	/* build the toolset */ 
	Toolset tset = 
          new Toolset(author, tname, new ArrayList<PackageCommon>(packages), desc, os);
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

	/* post-op tasks */ 
        startExtensionTasks(timer, new CreateToolsetExtFactory(tset, os));

	return new MiscCreateToolsetRsp(timer, tset);
      }    
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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
    String author         = req.getAuthor();
    PackageMod pmod       = req.getPackage();
    String pname          = pmod.getName();
    String desc           = req.getDescription();
    VersionID.Level level = req.getLevel(); 
    OsType os             = req.getOsType();

    TaskTimer timer = new TaskTimer("MasterMgr.createToolsetPackage(): " + pname);
    
    /* pre-op tests */
    CreateToolsetPackageExtFactory factory = 
      new CreateToolsetPackageExtFactory(author, pmod, desc, level, os); 
    try {
      performExtensionTests(timer, factory);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      if(!pAdminPrivileges.isDeveloper(req)) 
	throw new PipelineException
	  ("Only a user with Master Admin privileges may create new toolset packages!");

      synchronized(pToolsetPackages) {
	timer.resume();

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
	  
	  if(level == null) 
	    throw new PipelineException 
	      ("Unable to create the " + os + " toolset package (" + pname + ") " + 
	       "due to a missing revision number increment level!");
	  
	  nvid = new VersionID(versions.lastKey(), level);
	}
	
	PackageVersion pkg = new PackageVersion(author, pmod, nvid, desc); 
	
	writeToolsetPackage(pkg, os);

	pToolsetPackages.put(pname, os, pkg.getVersionID(), pkg);
	
	/* post-op tasks */ 
        startExtensionTasks(timer, new CreateToolsetPackageExtFactory(pkg, os));

	return new MiscCreateToolsetPackageRsp(timer, pkg);
      }
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }
    finally {
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	

      TreeMap<PluginType,PluginMenuLayout> layouts = 
	new TreeMap<PluginType,PluginMenuLayout>();

      timer.acquire();
      synchronized(pEditorMenuLayouts) {
	timer.resume();	

	PluginMenuLayout layout = pEditorMenuLayouts.get(name);
	if(layout != null) 
	  layouts.put(PluginType.Editor, new PluginMenuLayout(layout));
	else 
	  layouts.put(PluginType.Editor, new PluginMenuLayout());
      }

      timer.acquire();
      synchronized(pComparatorMenuLayouts) {
	timer.resume();	

	PluginMenuLayout layout = pComparatorMenuLayouts.get(name);
	if(layout != null) 
	  layouts.put(PluginType.Comparator, new PluginMenuLayout(layout));
	else 
	  layouts.put(PluginType.Comparator, new PluginMenuLayout());
      }

      timer.acquire();
      synchronized(pActionMenuLayouts) {
	timer.resume();	

	PluginMenuLayout layout = pActionMenuLayouts.get(name);
	if(layout != null) 
	  layouts.put(PluginType.Action, new PluginMenuLayout(layout));
	else 
	  layouts.put(PluginType.Action, new PluginMenuLayout());
      }

      timer.acquire();
      synchronized(pToolMenuLayouts) {
	timer.resume();	

	PluginMenuLayout layout = pToolMenuLayouts.get(name);
	if(layout != null) 
	  layouts.put(PluginType.Tool, new PluginMenuLayout(layout));
	else 
	  layouts.put(PluginType.Tool, new PluginMenuLayout());
      }

      timer.acquire();
      synchronized(pArchiverMenuLayouts) {
	timer.resume();	

	PluginMenuLayout layout = pArchiverMenuLayouts.get(name);
	if(layout != null) 
	  layouts.put(PluginType.Archiver, new PluginMenuLayout(layout));
	else 
	  layouts.put(PluginType.Archiver, new PluginMenuLayout());
      }

      timer.acquire();
      synchronized(pMasterExtMenuLayouts) {
	timer.resume();	

	PluginMenuLayout layout = pMasterExtMenuLayouts.get(name);
	if(layout != null) 
	  layouts.put(PluginType.MasterExt, new PluginMenuLayout(layout));
	else 
	  layouts.put(PluginType.MasterExt, new PluginMenuLayout());
      }

      timer.acquire();
      synchronized(pQueueExtMenuLayouts) {
	timer.resume();	

	PluginMenuLayout layout = pQueueExtMenuLayouts.get(name);
	if(layout != null) 
	  layouts.put(PluginType.QueueExt, new PluginMenuLayout(layout));
	else 
	  layouts.put(PluginType.QueueExt, new PluginMenuLayout());
      }
      
      timer.acquire();
      synchronized(pAnnotationMenuLayouts) {
	timer.resume();	

	PluginMenuLayout layout = pAnnotationMenuLayouts.get(name);
	if(layout != null) 
	  layouts.put(PluginType.Annotation, new PluginMenuLayout(layout));
	else 
	  layouts.put(PluginType.Annotation, new PluginMenuLayout());
      }
      
      timer.acquire();
      synchronized(pKeyChooserMenuLayouts) {
	timer.resume();	

	PluginMenuLayout layout = pKeyChooserMenuLayouts.get(name);
	if(layout != null) 
	  layouts.put(PluginType.KeyChooser, new PluginMenuLayout(layout));
	else 
	  layouts.put(PluginType.KeyChooser, new PluginMenuLayout());
      }
      
      timer.acquire();
      synchronized(pBuilderCollectionMenuLayouts) {
        timer.resume(); 

        PluginMenuLayout layout = pBuilderCollectionMenuLayouts.get(name);
        if(layout != null) 
          layouts.put(PluginType.BuilderCollection, new PluginMenuLayout(layout));
        else 
          layouts.put(PluginType.BuilderCollection, new PluginMenuLayout());
      }
      
      return new MiscGetPluginMenuLayoutsRsp(timer, layouts);
    }
    finally {
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();

      TreeMap<String,TreeSet<VersionID>> packages = req.getPackages(); 

      TripleMap<String,VersionID,PluginType,PluginSet> allPlugins = 
	new TripleMap<String,VersionID,PluginType,PluginSet>();
      
      timer.acquire();
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
      
      timer.acquire();
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
      
      timer.acquire();
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
      
      timer.acquire();
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
      
      timer.acquire();
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
      
      timer.acquire();
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
      
      timer.acquire();
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

      timer.acquire();
      synchronized(pPackageAnnotationPlugins) {
	timer.resume();
	for(String pname : packages.keySet()) {
	  for(VersionID pvid : packages.get(pname)) {
	    PluginSet plugins = pPackageAnnotationPlugins.get(pname, pvid);
	    if(plugins == null)
	      plugins = new PluginSet(); 

	    allPlugins.put(pname, pvid, PluginType.Annotation, plugins);
	  }
	}
      }

      timer.acquire();
      synchronized(pPackageKeyChooserPlugins) {
	timer.resume();
	for(String pname : packages.keySet()) {
	  for(VersionID pvid : packages.get(pname)) {
	    PluginSet plugins = pPackageKeyChooserPlugins.get(pname, pvid);
	    if(plugins == null)
	      plugins = new PluginSet(); 

	    allPlugins.put(pname, pvid, PluginType.KeyChooser, plugins);
	  }
	}
      }
      
      timer.acquire();
      synchronized(pPackageBuilderCollectionPlugins) {
        timer.resume();
        for(String pname : packages.keySet()) {
          for(VersionID pvid : packages.get(pname)) {
            PluginSet plugins = pPackageBuilderCollectionPlugins.get(pname, pvid);
            if(plugins == null)
              plugins = new PluginSet(); 

            allPlugins.put(pname, pvid, PluginType.BuilderCollection, plugins);
          }
        }
      }

      return new MiscGetSelectPackagePluginsRsp(timer, allPlugins); 
    }
    finally {
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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
    
    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
	timer.acquire();
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
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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
    
    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
	timer.acquire();
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
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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
    
    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
	timer.acquire();
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
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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
    
    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
	timer.acquire();
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
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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
    
    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
	timer.acquire();
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
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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
    
    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
	timer.acquire();
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
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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
    
    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
	timer.acquire();
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
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
    }
  }


  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Get the layout of the annotation plugin menu associated with a toolset.
   *
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetPluginMenuLayoutRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the menu layout.
   */ 
  public Object 
  getAnnotationMenuLayout
  ( 
   MiscGetPluginMenuLayoutReq req 
  ) 
  {
    String name = req.getName();

    TaskTimer timer = 
      new TaskTimer("MasterMgr.getAnnotationMenuLayout(): " + name);

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      synchronized(pAnnotationMenuLayouts) {
	timer.resume();	

	PluginMenuLayout layout = null;
	if(name == null) 
	  layout = pDefaultAnnotationMenuLayout;
	else 
	  layout = pAnnotationMenuLayouts.get(name);

	if(layout != null) 
	  return new MiscGetPluginMenuLayoutRsp(timer, new PluginMenuLayout(layout));
	else 
	  return new MiscGetPluginMenuLayoutRsp(timer, new PluginMenuLayout());
      }
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }
  }

  /**
   * Set the layout of the annotation plugin selection menu.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to set the menu layout.
   */ 
  public Object 
  setAnnotationMenuLayout
  ( 
   MiscSetPluginMenuLayoutReq req 
  ) 
  {
    String name = req.getName();

    TaskTimer timer = 
      new TaskTimer("MasterMgr.setAnnotationMenuLayout(): " + name);
    
    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      if(!pAdminPrivileges.isDeveloper(req)) 
	throw new PipelineException
	  ("Only a user with Developer privileges may set the annotation menu layout!");

      synchronized(pAnnotationMenuLayouts) {
	timer.resume();	

	PluginMenuLayout layout = req.getLayout();

	if(name == null) {
	  pDefaultAnnotationMenuLayout = layout;
	}
	else {
	  if(layout == null) 
	    pAnnotationMenuLayouts.remove(name);
	  else 
	    pAnnotationMenuLayouts.put(name, layout);
	}

	writePluginMenuLayout(name, "annotation", 
			      pAnnotationMenuLayouts, pDefaultAnnotationMenuLayout);

	return new SuccessRsp(timer);
      }
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }
  }

  /**
   * Get the annotation plugins associated with all packages of a toolset.
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetPackagePluginsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the plugins.
   */ 
  public Object
  getToolsetAnnotationPlugins
  (
   MiscGetToolsetPluginsReq req 
  ) 
  {
    String tname = req.getName();

    TaskTimer timer = new TaskTimer();

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
	timer.acquire();
	synchronized(pPackageAnnotationPlugins) {
	  timer.resume();
	  
	  for(String pname : packages.keySet()) {
	    for(VersionID pvid : packages.get(pname)) {
	      PluginSet pset = pPackageAnnotationPlugins.get(pname, pvid);
	      if(pset != null) 
		plugins.addAll(pset);
	    }
	  }
	}
      }

      return new MiscGetPackagePluginsRsp(timer, plugins); 
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }
  }

  /**
   * Get the annotation plugins associated with a toolset package.
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetPackagePluginsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the plugins.
   */ 
  public Object
  getPackageAnnotationPlugins
  (
   MiscGetPackagePluginsReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      synchronized(pPackageAnnotationPlugins) {
	timer.resume();
	
	PluginSet plugins = pPackageAnnotationPlugins.get(req.getName(), req.getVersionID());
	if(plugins == null)
	  plugins = new PluginSet(); 

	return new MiscGetPackagePluginsRsp(timer, plugins); 
      }
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }
  }

  /**
   * Set the annotation plugins associated with a toolset package.
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the plugins.
   */ 
  public Object
  setPackageAnnotationPlugins
  (
   MiscSetPackagePluginsReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      if(!pAdminPrivileges.isDeveloper(req)) 
	throw new PipelineException
	  ("Only a user with Developer privileges may change the annotation plugins " + 
	   "associated with a toolset package!"); 

      synchronized(pPackageAnnotationPlugins) {
	timer.resume();
	
	if(req.getPlugins() == null)
	  pPackageAnnotationPlugins.remove(req.getName(), req.getVersionID());
	else 
	  pPackageAnnotationPlugins.put(req.getName(), req.getVersionID(), req.getPlugins());

	writePackagePlugins(req.getName(), req.getVersionID(), 
			    "annotation", pPackageAnnotationPlugins);

	return new SuccessRsp(timer);
      }
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }
  }


  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Get the layout of the key chooser plugin menu associated with a toolset.
   *
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetPluginMenuLayoutRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the menu layout.
   */ 
  public Object 
  getKeyChooserMenuLayout
  ( 
   MiscGetPluginMenuLayoutReq req 
  ) 
  {
    String name = req.getName();

    TaskTimer timer = 
      new TaskTimer("MasterMgr.getKeyChooserMenuLayout(): " + name);

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      synchronized(pKeyChooserMenuLayouts) {
	timer.resume();	

	PluginMenuLayout layout = null;
	if(name == null) 
	  layout = pDefaultKeyChooserMenuLayout;
	else 
	  layout = pKeyChooserMenuLayouts.get(name);

	if(layout != null) 
	  return new MiscGetPluginMenuLayoutRsp(timer, new PluginMenuLayout(layout));
	else 
	  return new MiscGetPluginMenuLayoutRsp(timer, new PluginMenuLayout());
      }
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }
  }

  /**
   * Set the layout of the key chooser plugin selection menu.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to set the menu layout.
   */ 
  public Object 
  setKeyChooserMenuLayout
  ( 
   MiscSetPluginMenuLayoutReq req 
  ) 
  {
    String name = req.getName();

    TaskTimer timer = 
      new TaskTimer("MasterMgr.setKeyChooserMenuLayout(): " + name);
    
    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      if(!pAdminPrivileges.isDeveloper(req)) 
	throw new PipelineException
	  ("Only a user with Developer privileges may set the key chooser menu layout!");

      synchronized(pKeyChooserMenuLayouts) {
	timer.resume();	

	PluginMenuLayout layout = req.getLayout();

	if(name == null) {
	  pDefaultKeyChooserMenuLayout = layout;
	}
	else {
	  if(layout == null) 
	    pKeyChooserMenuLayouts.remove(name);
	  else 
	    pKeyChooserMenuLayouts.put(name, layout);
	}

	writePluginMenuLayout(name, "key chooser", 
			      pKeyChooserMenuLayouts, pDefaultKeyChooserMenuLayout);

	return new SuccessRsp(timer);
      }
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }
  }

  /**
   * Get the key chooser plugins associated with all packages of a toolset.
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetPackagePluginsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the plugins.
   */ 
  public Object
  getToolsetKeyChooserPlugins
  (
   MiscGetToolsetPluginsReq req 
  ) 
  {
    String tname = req.getName();

    TaskTimer timer = new TaskTimer();

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
	timer.acquire();
	synchronized(pPackageKeyChooserPlugins) {
	  timer.resume();
	  
	  for(String pname : packages.keySet()) {
	    for(VersionID pvid : packages.get(pname)) {
	      PluginSet pset = pPackageKeyChooserPlugins.get(pname, pvid);
	      if(pset != null) 
		plugins.addAll(pset);
	    }
	  }
	}
      }

      return new MiscGetPackagePluginsRsp(timer, plugins); 
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }
  }

  /**
   * Get the key chooser plugins associated with a toolset package.
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetPackagePluginsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the plugins.
   */ 
  public Object
  getPackageKeyChooserPlugins
  (
   MiscGetPackagePluginsReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      synchronized(pPackageKeyChooserPlugins) {
	timer.resume();
	
	PluginSet plugins = pPackageKeyChooserPlugins.get(req.getName(), req.getVersionID());
	if(plugins == null)
	  plugins = new PluginSet(); 

	return new MiscGetPackagePluginsRsp(timer, plugins); 
      }
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }
  }

  /**
   * Set the key chooser plugins associated with a toolset package.
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the plugins.
   */ 
  public Object
  setPackageKeyChooserPlugins
  (
   MiscSetPackagePluginsReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      if(!pAdminPrivileges.isDeveloper(req)) 
	throw new PipelineException
	  ("Only a user with Developer privileges may change the key chooser plugins " + 
	   "associated with a toolset package!"); 

      synchronized(pPackageKeyChooserPlugins) {
	timer.resume();
	
	if(req.getPlugins() == null)
	  pPackageKeyChooserPlugins.remove(req.getName(), req.getVersionID());
	else 
	  pPackageKeyChooserPlugins.put(req.getName(), req.getVersionID(), req.getPlugins());

	writePackagePlugins(req.getName(), req.getVersionID(), 
			    "key chooser", pPackageKeyChooserPlugins);

	return new SuccessRsp(timer);
      }
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the layout of the builder collection plugin menu associated with a toolset.
   *
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetPluginMenuLayoutRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the menu layout.
   */ 
  public Object 
  getBuilderCollectionMenuLayout
  ( 
   MiscGetPluginMenuLayoutReq req 
  ) 
  {
    String name = req.getName();

    TaskTimer timer = 
      new TaskTimer("MasterMgr.getBuilderCollectionMenuLayout(): " + name);

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      synchronized(pBuilderCollectionMenuLayouts) {
        timer.resume(); 

        PluginMenuLayout layout = null;
        if(name == null) 
          layout = pDefaultBuilderCollectionMenuLayout;
        else 
          layout = pBuilderCollectionMenuLayouts.get(name);

        if(layout != null) 
          return new MiscGetPluginMenuLayoutRsp(timer, new PluginMenuLayout(layout));
        else 
          return new MiscGetPluginMenuLayoutRsp(timer, new PluginMenuLayout());
      }
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }
  }

  /**
   * Set the layout of the builder collection plugin selection menu.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to set the menu layout.
   */ 
  public Object 
  setBuilderCollectionMenuLayout
  ( 
   MiscSetPluginMenuLayoutReq req 
  ) 
  {
    String name = req.getName();

    TaskTimer timer = 
      new TaskTimer("MasterMgr.setBuilderCollectionMenuLayout(): " + name);
    
    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      if(!pAdminPrivileges.isDeveloper(req)) 
        throw new PipelineException
          ("Only a user with Developer privileges may set the builder collection " + 
           "menu layout!");

      synchronized(pBuilderCollectionMenuLayouts) {
        timer.resume(); 

        PluginMenuLayout layout = req.getLayout();

        if(name == null) {
          pDefaultBuilderCollectionMenuLayout = layout;
        }
        else {
          if(layout == null) 
            pBuilderCollectionMenuLayouts.remove(name);
          else 
            pBuilderCollectionMenuLayouts.put(name, layout);
        }

        writePluginMenuLayout(name, "builder collection", 
                              pBuilderCollectionMenuLayouts, 
                              pDefaultBuilderCollectionMenuLayout);

        return new SuccessRsp(timer);
      }
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());      
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }
  }

  /**
   * Get the builder collection plugins associated with all packages of a toolset.
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetPackagePluginsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the plugins.
   */ 
  public Object
  getToolsetBuilderCollectionPlugins
  (
   MiscGetToolsetPluginsReq req 
  ) 
  {
    String tname = req.getName();

    TaskTimer timer = new TaskTimer();

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
        timer.acquire();
        synchronized(pPackageBuilderCollectionPlugins) {
          timer.resume();
          
          for(String pname : packages.keySet()) {
            for(VersionID pvid : packages.get(pname)) {
              PluginSet pset = pPackageBuilderCollectionPlugins.get(pname, pvid);
              if(pset != null) 
                plugins.addAll(pset);
            }
          }
        }
      }

      return new MiscGetPackagePluginsRsp(timer, plugins); 
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }
  }

  /**
   * Get the builder collection plugins associated with a toolset package.
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>MiscGetPackagePluginsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the plugins.
   */ 
  public Object
  getPackageBuilderCollectionPlugins
  (
   MiscGetPackagePluginsReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      synchronized(pPackageBuilderCollectionPlugins) {
        timer.resume();
        
        PluginSet plugins = 
          pPackageBuilderCollectionPlugins.get(req.getName(), req.getVersionID());
        if(plugins == null)
          plugins = new PluginSet(); 

        return new MiscGetPackagePluginsRsp(timer, plugins); 
      }
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }
  }

  /**
   * Set the builder collection plugins associated with a toolset package.
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the plugins.
   */ 
  public Object
  setPackageBuilderCollectionPlugins
  (
   MiscSetPackagePluginsReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      if(!pAdminPrivileges.isDeveloper(req)) 
        throw new PipelineException
          ("Only a user with Developer privileges may change the builder collection " + 
           "plugins associated with a toolset package!"); 

      synchronized(pPackageBuilderCollectionPlugins) {
        timer.resume();
        
        if(req.getPlugins() == null)
          pPackageBuilderCollectionPlugins.remove(req.getName(), req.getVersionID());
        else 
          pPackageBuilderCollectionPlugins.put
            (req.getName(), req.getVersionID(), req.getPlugins());

        writePackagePlugins(req.getName(), req.getVersionID(), 
                            "builder collection", pPackageBuilderCollectionPlugins);

        return new SuccessRsp(timer);
      }
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());      
    }
    finally {
      pDatabaseLock.releaseReadLock();
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
    
    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      synchronized(pMasterExtensions) {
	timer.resume();
	
	return new MiscGetMasterExtensionsRsp(timer, pMasterExtensions);
      }
    }
    finally {
      pDatabaseLock.releaseReadLock();
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

    if(!pAdminPrivileges.isMasterAdmin(req))
     return new FailureRsp
       (timer, "Only a user with Master Admin privileges may remove a " + 
        "master extension configuration!");

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
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
      pDatabaseLock.releaseReadLock();
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
    
    if(!pAdminPrivileges.isMasterAdmin(req))
      return new FailureRsp
        (timer, "Only a user with Master Admin privileges may add or modify " + 
         "master extension configuration!");

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
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
      pDatabaseLock.releaseReadLock();
    }
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
    timer.acquire(); 
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
    /* the enabled extensions which support the current test */ 
    LinkedList<BaseMasterExt> extensions = new LinkedList<BaseMasterExt>();

    timer.acquire(); 
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

          /* if the current extension has a test... */ 
	  if((ext != null) && factory.hasTest(ext)) {
            ext.setMasterMgrClient(new MasterMgrDirectLightClient(this));            
            extensions.add(ext);
          }
	}
      }
    }
    
    /* perform the enabled pre-op tests */ 
    for(BaseMasterExt ext : extensions) 
      factory.performTest(ext); 
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
    timer.acquire(); 
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
    timer.acquire(); 
    synchronized(pMasterExtensions) {
      timer.resume();

      for(MasterExtensionConfig config : pMasterExtensions.values()) {
	if(config.isEnabled()) {
	  try {
	    BaseMasterExt ext = config.getMasterExt();
	    if(factory.hasTask(ext)) {
              ext.setMasterMgrClient(new MasterMgrDirectLightClient(this));    
              factory.startTask(config, ext); 
            }
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
    
    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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
    
    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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
    
    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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
   NodeGetByNameReq req
  )
  {
    TaskTimer timer = new TaskTimer();
    
    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	
	
      return new NodeGetWorkingAreasRsp(timer, pNodeTree.getViewsContaining(req.getName()));
    }
    finally {
      pDatabaseLock.releaseReadLock();
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
   NodeWorkingAreaReq req 
  ) 
  {
    String author = req.getAuthor();
    String view   = req.getView();

    TaskTimer timer = new TaskTimer("MasterMgr.createWorkingArea(): " + author + "|" + view);

    try {
      if(author == null)
	throw new PipelineException("The author cannot be (null)!");

      if(view == null)
	throw new PipelineException("The view cannot be (null)!");

      if(!Identifiers.isExtendedNumerIdent(view))
	throw new PipelineException
	  ("The view (" + view + ") is invalid! " + 
	   "Valid view names start with (\"a\"-\"z\", \"A\"-\"Z\", \"0\"-\"9\")" + 
	   " followed by zero or more of the following characters: " + 
	   "\"a\"-\"z\", \"A\"-\"Z\", \"0\"-\"9\", \"_\", \"-\", \"~\", \".\"");
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    
    /* pre-op tests */
    CreateWorkingAreaExtFactory factory = new CreateWorkingAreaExtFactory(author, view); 
    try {
      performExtensionTests(timer, factory);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	

      /* add the owner as a user if not already added */ 
      if(pAdminPrivileges.addMissingUser(author)) {
        updateAdminPrivileges();
        
        WorkGroups groups = pAdminPrivileges.getWorkGroups();
        startExtensionTasks(timer, new SetWorkGroupsExtFactory(groups)); 
      }

      if(!pAdminPrivileges.isNodeManaged(req, author)) 
	throw new PipelineException
	  ("Only a user with Node Manager privileges may create working areas owned " +
	   "by another user!");

      /* create the working area */ 
      createWorkingAreaHelper(timer, author, view);
	
      /* post-op tasks */ 
      startExtensionTasks(timer, factory);
      
      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }
  }  

  /**
   * Create a new empty working area. <P> 
   * 
   * This should only be called from inside a pDatabaseLock.readLock().
   */ 
  private void 
  createWorkingAreaHelper
  ( 
   TaskTimer timer,    
   String author,
   String view 
  ) 
    throws PipelineException
  {
    timer.acquire();
    synchronized(pWorkingAreaViews) {
      timer.resume();	
      
      /* make sure it doesn't already exist */ 
      TreeSet<String> views = pWorkingAreaViews.get(author);
      if((views != null) && views.contains(view))
        return;
      
      /* create the working area node directory */ 
      File dir = new File(pNodeDir, "working/" + author + "/" + view);
      synchronized(pMakeDirLock) {
        if(!dir.isDirectory()) {
          if(!dir.mkdirs()) 
            throw new PipelineException
              ("Unable to create the working area (" + view + ") for user " + 
               "(" + author + ")!");
        }
      }
      
      /* create the working area files directory */ 
      FileMgrClient fclient = acquireFileMgrClient();
      try {
        fclient.createWorkingArea(author, view);
      }
      finally {
        releaseFileMgrClient(fclient);
      }
      
      /* add the view to the runtime table */ 
      if(views == null) {
        views = new TreeSet<String>();
        pWorkingAreaViews.put(author, views);
      }
      views.add(view);
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
   NodeWorkingAreaReq req 
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	
	
      if(!pAdminPrivileges.isNodeManaged(req, author)) 
	throw new PipelineException
	  ("Only a user with Node Manager privileges may remove working areas owned " +
	   "by another user!");

      /* release all working versions in the view */ 
      {
	TreeSet<String> matches = pNodeTree.getMatchingWorkingNodes(author, view, null);
        for(String name : matches) 
          releaseHelper(new NodeID(author, view, name), true, true, true, timer);
      }
      
      /* determine whether to abort early and/or remove the user as well */ 
      boolean removeUser = false;
      timer.acquire();
      synchronized(pWorkingAreaViews) {
	timer.resume();	

	/* abort if it doesn't exist */  
	TreeSet<String> views = pWorkingAreaViews.get(author);
	if((views == null) || !views.contains(view))
	  return new SuccessRsp(timer);

	/* determine whether to remove the user as well */
	if(view.equals("default")) {
	  if(views.size() > 1) 
	    throw new PipelineException
	      ("The default view (and therefore the user) can only be removed if it is " + 
	       "the only remaining view!");
	  removeUser = true;
	}
      }

      /* remove the working area production files directory */ 
      {
        FileMgrClient fclient = acquireFileMgrClient();
        try {
          fclient.removeWorkingArea(author, removeUser ? null : view);
        }
        finally {
          releaseFileMgrClient(fclient);
        }
      }

      /* clean up the database dirs... */ 
      timer.acquire();
      synchronized(pWorkingAreaViews) {
	timer.resume();	
	
	/* abort if it doesn't exist */  
        TreeSet<String> views = pWorkingAreaViews.get(author);
        if((views == null) || !views.contains(view))
          return new SuccessRsp(timer);

	/* remove working area view database directory (if empty) */ 
        {
          File dir = new File(pNodeDir, "working/" + author + "/" + view);
          File files[] = dir.listFiles();
          if((files != null) && (files.length == 0)) {
            if(!dir.delete()) 
              throw new PipelineException 
                ("Unable to remove the working area view database directory " + 
                 "(" + dir + ")!");

            /* remove view from the runtime table */ 
            views.remove(view);
          }
        }

	/* remove the working area user database directory (if empty) */ 
	if(views.isEmpty()) {
	  File dir = new File(pNodeDir, "working/" + author);  
          File files[] = dir.listFiles();
          if((files != null) && (files.length == 0)) {
            if(!dir.delete()) 
              throw new PipelineException 
                ("Unable to remove the working area user database directory " + 
                 "(" + dir + ")!");

            pWorkingAreaViews.remove(author);
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
      pDatabaseLock.releaseReadLock();
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
    
    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
      pDatabaseLock.releaseReadLock();
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
   * @param primary
   *   The primary file sequence associated with the working version.
   * 
   * @param fseqs
   *   The file sequences associated with the working version.
   */ 
  private void 
  addWorkingNodeTreePath
  (
   NodeID nodeID, 
   FileSeq primary, 
   SortedSet<FileSeq> fseqs
  )
  {
    addWorkingAreaForNode(nodeID);    
    pNodeTree.addWorkingNodeTreePath(nodeID, primary, fseqs);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   N O D E S                                                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of all nodes who's name matches the given search pattern.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>NodeGetNodeNamesRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> on failure.
   */ 
  public Object 
  getNodeNames
  ( 
   NodeGetNodeNamesReq req 
  ) 
  {
    String pattern = req.getPattern();

    TaskTimer timer = new TaskTimer("MasterMgr.getNodeNames(): [" + pattern + "]");

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	

      try {
	Pattern pat = null;
	if(pattern != null) 
	  pat = Pattern.compile(pattern);
	
	TreeSet<String> matches = pNodeTree.getMatchingNodes(pat);

	return new NodeGetNodeNamesRsp(timer, matches);
      }
      catch(PatternSyntaxException ex) {
	return new FailureRsp(timer, 
			      "Illegal Node Name Pattern:\n\n" + ex.getMessage());
      }
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }
  }  

  /**
   * Update the immediate children of all node path components along the given path 
   * which are visible within a working area view owned by the user. <P> 
   * 
   * @param req 
   *   The update paths request.
   * 
   * @return
   *   <CODE>NodeUpdatePathsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to get the path components.
   */
  public Object
  updatePaths
  (
   NodeUpdatePathsReq req
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	

      String author = req.getAuthor();
      String view   = req.getView();

      NodeTreeComp rootComp = 
        pNodeTree.getUpdatedPaths(author, view, req.getPaths(), req.showHidden());
      
      return new NodeUpdatePathsRsp(timer, author, view, rootComp);
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }
  }

  /**
   * Set the default show/hide display policy of a node path component.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable set the hidden status.
   */
  public Object
  setPathHidden
  (
   NodeSetPathHiddenReq req
  )
  {
    TaskTimer timer = new TaskTimer();

    if(!pAdminPrivileges.isMasterAdmin(req))
      return new FailureRsp
        (timer, "Only a user with Master Admin privileges may hide/show node paths!"); 
      
    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	

      if(pNodeTree.setHidden(req.getPath(), req.isHidden()))
        pNodeTree.updateHiddenFile(req.getPath(), req.isHidden());
      
      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	

      return new NodeGetNodeOwningRsp(timer, pNodeTree.getNodeOwning(req.getPath()));
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A N N O T A T I O N S                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get a specific annotation for the given node.<P> 
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>NodeGetAnnotationRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable determine the annotations.
   */
  public Object
  getAnnotation
  (
   NodeGetAnnotationReq req
  ) 
  {
    TaskTimer timer = new TaskTimer();

    String name  = req.getNodeName();
    String aname = req.getAnnotationName(); 

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();
    
      BaseAnnotation annot = getAnnotationHelper(timer, name, aname); 

      return new NodeGetAnnotationRsp(timer, annot);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }   
  }

  /**
   * Get a specific annotation for the given node.<P> 
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>NodeGetBothAnnotationRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable determine the annotations.
   */
  public Object
  getBothAnnotation
  (
   NodeGetBothAnnotationReq req
  ) 
  {
    TaskTimer timer = new TaskTimer();

    NodeID nodeID = req.getNodeID();
    String name   = nodeID.getName(); 
    String aname  = req.getAnnotationName(); 

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    LoggedLock lock = getWorkingLock(nodeID);
    lock.acquireReadLock();
    try {
      timer.resume();
    
      BaseAnnotation annot = getAnnotationHelper(timer, name, aname); 

      NodeMod mod = new NodeMod(getWorkingBundle(nodeID).getVersion());
      BaseAnnotation vannot = mod.getAnnotation(aname);
      if(vannot != null) 
        annot = vannot;
      
      return new NodeGetAnnotationRsp(timer, annot);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      lock.releaseReadLock();
      pDatabaseLock.releaseReadLock();
    }   
  }
  
  /**
   * Helper method to get a copy of a specific annotation for the given node.<P> 
   * 
   * This method assumes that the pDatabaseLock has been acquired before this
   * was called.
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @param aname 
   *   The name of the annotation. 
   * 
   * @return 
   *   The named annotation for the node or <CODE>null</CODE> if none exists. 
   */
  public BaseAnnotation
  getAnnotationHelper
  (
   TaskTimer timer,
   String name, 
   String aname
  ) 
    throws PipelineException 
  {
    timer.acquire();
    LoggedLock lock = getAnnotationsLock(name); 
    lock.acquireReadLock();
    try {
      timer.resume();
    
      BaseAnnotation annot = null;
      {
        TreeMap<String,BaseAnnotation> table = getAnnotationsTable(name);
        if(table != null) 
          annot = table.get(aname);
      }

      if(annot != null) 
        return (BaseAnnotation) annot.clone(); 
      return null;
    }
    finally {
      lock.releaseReadLock();
    }   
  }

  /**
   * Get all of the annotations for the given node.<P> 
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>NodeGetAnnotationsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable determine the annotations.
   */
  public Object
  getAnnotations
  (
   NodeGetByNameReq req
  ) 
  {
    TaskTimer timer = new TaskTimer();

    String name = req.getName();

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();

      TreeMap<String,BaseAnnotation> table = getAnnotationsHelper(timer, name);

      return new NodeGetAnnotationsRsp(timer, table);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }
  }
  
  /**
   * Get all of the annotations for the given node.<P> 
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>NodeGetAnnotationsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable determine the annotations.
   */
  public Object
  getBothAnnotations
  (
   NodeGetBothAnnotationsReq req
  ) 
  {
    TaskTimer timer = new TaskTimer();

    NodeID nodeID = req.getNodeID();
    String name   = nodeID.getName(); 

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    LoggedLock lock = getWorkingLock(nodeID);
    lock.acquireReadLock();
    try {
      timer.resume();

      TreeMap<String,BaseAnnotation> table = new TreeMap<String, BaseAnnotation>();
      
      TreeMap<String, BaseAnnotation> perNode = getAnnotationsHelper(timer, name); 
      if (perNode != null)
        table.putAll(perNode);

      // This can never be null
      NodeMod mod = new NodeMod(getWorkingBundle(nodeID).getVersion());
      table.putAll(mod.getAnnotations());

      return new NodeGetAnnotationsRsp(timer, table);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      lock.releaseReadLock();
      pDatabaseLock.releaseReadLock();
    }
  }
  
  /**
   * Get all of the annotations for the specified nodes.<P> 
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>NodeGetAllAnnotationsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable determine the annotations.
   */
  public Object
  getAllBothAnnotations
  (
    NodeGetAllBothAnnotationsReq req
  ) 
  {
    TaskTimer timer = new TaskTimer();

    TreeSet<NodeID> nodeIDs = req.getNodeIDs();
    
    DoubleMap<NodeID, String, BaseAnnotation> toReturn = 
      new DoubleMap<NodeID, String, BaseAnnotation>();

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    timer.resume();
    try {
      for (NodeID nodeID : nodeIDs) {
        timer.acquire();
        LoggedLock lock = getWorkingLock(nodeID);
        lock.acquireReadLock();
        try {
          timer.resume();
          String name = nodeID.getName();
          
          TreeMap<String,BaseAnnotation> table = new TreeMap<String, BaseAnnotation>();

          TreeMap<String, BaseAnnotation> perNode = getAnnotationsHelper(timer, name); 
          if (perNode != null)
            table.putAll(perNode);
          
          // This can never be null
          NodeMod mod = new NodeMod(getWorkingBundle(nodeID).getVersion());
          table.putAll(mod.getAnnotations());

          toReturn.put(nodeID, table);
        } 
        catch(PipelineException ex) {
          return new FailureRsp(timer, ex.getMessage());
        }
        finally {
          lock.releaseReadLock();
        }
      }
      return new NodeGetAllAnnotationsRsp(timer, toReturn);
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }
  }
  
  /**
   * Helper method for getting a deep copy of all of the annotations for a node.<P> 
   * 
   * This method assumes that the pDatabaseLock has been acquired before this
   * was called.
   * 
   * @param timer
   *   Timer associated with this task.
   * 
   * @param name
   *   The name of the node to get the annotations for.
   */
   private TreeMap<String, BaseAnnotation> 
   getAnnotationsHelper
   (
    TaskTimer timer,
    String name
   )
     throws PipelineException
   {
     timer.acquire();
     LoggedLock lock = getAnnotationsLock(name); 
     lock.acquireReadLock();
     try {
       timer.resume();
  
       TreeMap<String, BaseAnnotation> table = getAnnotationsTable(name);
       if(table != null) {
         TreeMap<String, BaseAnnotation> copy = new TreeMap<String, BaseAnnotation>();
         for(String aname : table.keySet()) 
           copy.put(aname, (BaseAnnotation) table.get(aname).clone());
         return copy; 
       }

       return null;
     }
     finally {
       lock.releaseReadLock();
     }  
   }
  
  /**
   * Add the given annotation to the set of current annotations for the given node.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to add the annotation.
   */
  public Object
  addAnnotation
  (
   NodeAddAnnotationReq req
  ) 
  {
    TaskTimer timer = new TaskTimer();

    String name          = req.getNodeName();
    String aname         = req.getAnnotationName();
    BaseAnnotation annot = req.getAnnotation();
    
    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();

      return addAnnotationHelper(req, timer, name, aname, annot);
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }  
  }
  
  private Object
  addAnnotationsHelper
  (
    PrivilegedReq req,
    TaskTimer timer,
    String name,
    TreeMap<String, BaseAnnotation> annots
  )
  {
    TreeMap<String, FailureRsp> errors = new TreeMap<String, FailureRsp>();
    for (String each : annots.keySet()) {
      TaskTimer child = new TaskTimer("MasterMgr.addAnnotationHelper()");
      timer.suspend();
      Object returned = addAnnotationHelper(req, child, name, each, annots.get(each));
      timer.accum(child);
      if (returned instanceof FailureRsp)
        errors.put(each, (FailureRsp) returned);
    }
    
    if (!errors.isEmpty()) {
      String msg = "An error occured while adding annotations to the node (" + name + ")\n";
      for (String each : errors.keySet()) {
        msg += ("  " + each + ": " + errors.get(each).getMessage() + "\n");
      }
      return new FailureRsp(timer, msg);
    }

    return new SuccessRsp(timer);
  }
  
  /**
   * Help method for adding an annotation to a node.<P> 
   * 
   * Assumes that the pDatabaseLock read lock has been acquired before it is called.
   * 
   * @param req
   *   The privileged req generating the annotation to be added.
   * 
   * @param timer
   *   Event timer.
   * 
   * @param name
   *   The name of the node to add the annotation to.
   * 
   * @param aname
   *   The name of the annotation.
   * 
   * @param annot
   *   The annotation being added.
   */
  private Object
  addAnnotationHelper
  (
    PrivilegedReq req,
    TaskTimer timer,
    String name,
    String aname,
    BaseAnnotation annot
  )
  {
    /* pre-op tests */
    AddAnnotationExtFactory factory = new AddAnnotationExtFactory(name, aname, annot);
    try {
      performExtensionTests(timer, factory);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }

    if(!annot.isContextual(AnnotationContext.PerNode))
      return new FailureRsp
        (timer, "The annotation plugin (" + annot.getName() + ") is not valid for the " + 
         AnnotationContext.PerNode + " context!");

    timer.acquire();

    LoggedLock lock = getAnnotationsLock(name); 
    lock.acquireWriteLock();
    try {
      timer.resume();
    
      TreeMap<String,BaseAnnotation> table = getAnnotationsTable(name);
      if(table == null) 
        table = new TreeMap<String,BaseAnnotation>();
      
      BaseAnnotation tannot = table.get(aname);
      if((tannot == null) || 
         !(tannot.getName().equals(annot.getName())) ||
         !(tannot.getVersionID().equals(annot.getVersionID())) ||
         !(tannot.getVendor().equals(annot.getVendor()))) {

        if(!pAdminPrivileges.isAnnotator(req) && !annot.isUserAddable()) 
          throw new PipelineException
            ("Only a user with Annotator privileges may add new annotations or replace " + 
             "existing annotations of with a different plugin Name, Version or Vendor!" +
             "The only exception to this is if the annotation has set the isUserAddable" +
             "flag.  The annotation that was being added does not set this flag."); 
        
        table.put(aname, annot);
      }
      else {
        tannot.setParamValues(annot, req.getRequestor(), 
                              pAdminPrivileges.getPrivilegeDetailsFromReq(req));
      }

      writeAnnotations(name, table);

      synchronized(pAnnotations) {
        pAnnotations.put(name, table);
      }

      /* post-op tasks */ 
      startExtensionTasks(timer, factory);

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      lock.releaseWriteLock();
    }  
  }

  /**
   * Remove a specific annotation from a node. 
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to remove the annotation.
   */
  public Object
  removeAnnotation
  (
   NodeRemoveAnnotationReq req
  ) 
  {
    TaskTimer timer = new TaskTimer();

    String name  = req.getNodeName();
    String aname = req.getAnnotationName();
    
    /* pre-op tests */
    RemoveAnnotationExtFactory factory = new RemoveAnnotationExtFactory(name, aname);
    try {
      performExtensionTests(timer, factory);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    LoggedLock lock = getAnnotationsLock(name); 
    lock.acquireWriteLock();
    try {
      timer.resume();
    
      TreeMap<String,BaseAnnotation> table = getAnnotationsTable(name);
      if(table == null) 
        throw new PipelineException
          ("No annotations exist for node (" + name + ")!"); 
      
      BaseAnnotation annot = table.get(aname);
      if(annot == null) 
        throw new PipelineException
          ("No annotation name (" + aname + ") exists for node (" + name + ")!"); 

      if(!pAdminPrivileges.isAnnotator(req) && !annot.isUserRemovable()) 
        throw new PipelineException
          ("Only a user with Annotator privileges may remove annotations from a node!" +
           "The only exception to this is if the annotation has set the isUserRemovable" +
           "flag.  The annotation that was being removed does not set this flag.");
            
      /* remove the annotation from the internal table */ 
      table.remove(aname); 

      /* if no annotations exist now, get rid of the entry too */ 
      if(table.isEmpty()) {  
        synchronized(pAnnotations) {
          pAnnotations.remove(name); 
        }
      }
      
      /* write the changes to disk, this will remove the file if table is empty */ 
      writeAnnotations(name, table);

      /* post-op tasks */ 
      startExtensionTasks(timer, factory);

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      lock.releaseWriteLock();
      pDatabaseLock.releaseReadLock();
    }  
  }

  /**
   * Remove all annotations from a node. 
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to remove the annotations.
   */
  public Object
  removeAnnotations
  (
   NodeRemoveAnnotationsReq req
  ) 
  {
    TaskTimer timer = new TaskTimer();

    String name = req.getNodeName();
    
    pDatabaseLock.acquireReadLock();
    try {
      return removeAnnotationsHelper(req, timer, name, false);
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }
  }
  
  /**
   * Help method for removing all the annotations from a given node.
   * <p>
   * This method assumes that the pDatabaseLock read lock has already been
   * acquired. 
   * 
   * @param req 
   *   The request.
   * @param timer
   *   Event timer.
   * @param name
   *   The name of the node.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to remove the annotations.
   */
  private Object
  removeAnnotationsHelper
  (
    PrivilegedReq req,
    TaskTimer timer,
    String name, 
    boolean ignorePrivileges
  )
  {
    timer.acquire();
    LoggedLock lock = getAnnotationsLock(name); 
    lock.acquireWriteLock();
    try {
      timer.resume();
    
      TreeMap<String,BaseAnnotation> table = getAnnotationsTable(name);
      if(table == null) 
        return new SuccessRsp(timer);
      
      ArrayList<String> removeList = new ArrayList<String>();

      /* pre-op tests */
      TreeMap<String, RemoveAnnotationExtFactory> factories = 
        new TreeMap<String, RemoveAnnotationExtFactory>();
      for(String aname : table.keySet()) {
        RemoveAnnotationExtFactory factory = new RemoveAnnotationExtFactory(name, aname);
        factories.put(aname, factory);
        performExtensionTests(timer, factory);
        BaseAnnotation annot = table.get(aname);

        if(ignorePrivileges ||
           pAdminPrivileges.isAnnotator(req) || 
           annot.isUserRemovable()) {
          removeList.add(aname);
        }
      }

      /* remove all annotations we are allowed to from the internal table */ 
      for(String remove : removeList)
        table.remove(remove);

      /* if no annotations exist now, get rid of the entry too */ 
      if(table.isEmpty()) {  
        synchronized(pAnnotations) {
          pAnnotations.remove(name); 
        }
      }

      /* write the changes to disk, this will remove the file if table is empty */ 
      writeAnnotations(name, table);

      /* post-op tasks */ 
      for(String aname : removeList) {
        RemoveAnnotationExtFactory factory = factories.get(aname); 
        if (factory != null)
          startExtensionTasks(timer, factory);
      }
      
      if(!table.isEmpty())
        throw new PipelineException
          ("Only a user with Annotator privileges may remove annotations from a node!" +
           "The only exception to this is if the annotation has set the isUserRemovable" +
           "flag.  The following annotations being removed did not set that flag:\n" +
           table.keySet());

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      lock.releaseWriteLock();
    }  
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   W O R K I N G   V E R S I O N S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the nodes in a working area for which have a name matching the 
   * given search pattern.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>NodeGetWorkingNamesRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine which working versions match the 
   *   pattern.
   */ 
  public Object 
  getWorkingNames
  ( 
   NodeWorkingAreaPatternReq req 
  ) 
  {
    String author  = req.getAuthor();
    String view    = req.getView();
    String pattern = req.getPattern();

    TaskTimer timer =  
      new TaskTimer("MasterMgr.getWorkingNames(): " + author + "|" + view + " " + 
                    "[" + pattern + "]");

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	

      try {
	Pattern pat = null;
	if(pattern != null) 
	  pat = Pattern.compile(pattern);
	
	TreeSet<String> matches = pNodeTree.getMatchingWorkingNodes(author, view, pat);
        
	return new NodeGetNodeNamesRsp(timer, matches);
      }
      catch(PatternSyntaxException ex) {
	return new FailureRsp(timer, 
			      "Illegal Node Name Pattern:\n\n" + ex.getMessage());
      }
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }
  }  

  /**
   * Get the names of the most downstream nodes in a working area.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>NodeGetWorkingNamesRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine which working versions are the roots.
   */ 
  public Object 
  getWorkingRootNames
  ( 
   NodeWorkingAreaReq req 
  ) 
  {
    String author  = req.getAuthor();
    String view    = req.getView();

    TaskTimer timer = 
      new TaskTimer("MasterMgr.getWorkingRootNames(): " + author + "|" + view);

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	

      TreeSet<String> roots = new TreeSet<String>(); 

      for(String name : pNodeTree.getMatchingWorkingNodes(author, view, null)) {
        timer.acquire();
        LoggedLock lock = getDownstreamLock(name);
        lock.acquireReadLock();
        try {
          timer.resume();
          
          DownstreamLinks dsl = getDownstreamLinks(name); 
          TreeSet<String> links = dsl.getWorking(author, view); 
          if((links == null) || links.isEmpty()) 
            roots.add(name);
        }
        finally {
          lock.releaseReadLock();
        }
      }

      return new NodeGetNodeNamesRsp(timer, roots);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }
  }  

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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    LoggedLock lock = getWorkingLock(req.getNodeID());
    lock.acquireReadLock();
    try {
      timer.resume();	
      
      NodeMod mod = new NodeMod(getWorkingBundle(req.getNodeID()).getVersion());
      return new NodeGetWorkingRsp(timer, req.getNodeID(), mod);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      lock.releaseReadLock();
      pDatabaseLock.releaseReadLock();
    }  
  }  

  /** 
   * Get the working versions for a set of nodes. <P> 
   * 
   * @param req 
   *   The get working version request.
   * 
   * @return
   *   <CODE>NodeGetMultiWorkingRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to retrieve the working version.
   */
  public Object
  getMultiWorkingVersion
  ( 
   NodeGetMultiWorkingReq req
  ) 
  {	 
    TaskTimer timer = new TaskTimer();

    String author = req.getAuthor(); 
    String view = req.getView(); 
    TreeSet<String> names = req.getNames();

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	

      TreeMap<String,NodeMod> mods = new TreeMap<String,NodeMod>(); 
      for(String name : names) {
        NodeID nodeID = new NodeID(author, view, name); 

        timer.acquire();
        LoggedLock lock = getWorkingLock(nodeID);
        lock.acquireReadLock();
        try {
          timer.resume();	
          
          NodeMod mod = new NodeMod(getWorkingBundle(nodeID).getVersion());
          mods.put(name, mod); 
        }
        catch(PipelineException ex) {
          // silently ignore missing nodes
        }
        finally {
          lock.releaseReadLock();
        }
      }
        
      return new NodeGetMultiWorkingRsp(timer, mods);
    }
    finally {
      pDatabaseLock.releaseReadLock();
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
    String name = nodeID.getName(); 
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    LoggedLock workingLock = getWorkingLock(nodeID);
    workingLock.acquireWriteLock();
    try {
      timer.resume();
      
      if(!pAdminPrivileges.isNodeManaged(req, nodeID)) 
	throw new PipelineException
	  ("Only a user with Node Manager privileges may modify nodes owned by " + 
	   "another user!");

      /* make sure the named toolset exits */ 
      if(!isValidToolsetName(nmod.getToolset()))
         throw new PipelineException
           ("Unable to modify the properties of node (" + nodeID + ") because its Toolset " + 
            "property (" + nmod.getToolset() + ") names a non-existent toolset!"); 

      /* get the working version */ 
      WorkingBundle bundle = getWorkingBundle(nodeID);
      NodeMod mod = new NodeMod(bundle.getVersion());
      if(mod.isFrozen()) 
	throw new PipelineException
	  ("The node properties of frozen node (" + nodeID + ") cannot be modified!");

      /* set the node properties */ 
      long critical = mod.getLastCriticalModification();
      boolean wasActionEnabled = mod.isActionEnabled();
      if(mod.setProperties(nmod)) {

	/* make sure there are no active jobs, if this is a critical modification */ 
	if((mod.getLastCriticalModification() > critical) &&
	   hasActiveJobs(nodeID, mod.getTimeStamp(), mod.getPrimarySequence()))
	  throw new PipelineException
	    ("Unable to modify critical properties of node (" + nodeID + ") " + 
	     "while there are active jobs associated with the node!");

        /* did the enabled state of the action change? */ 
	if(wasActionEnabled != mod.isActionEnabled()) {
	  FileMgrClient fclient = acquireFileMgrClient();
	  try {
            /* if its now user editable, we need to check-out an actual copy of the files 
               for any working area symlink, but leave the rest alone since they might have 
               been generated by some previous job or maybe even interactively at some
               point and therefore should be preserved */ 
            if(!mod.isActionEnabled() && !mod.isIntermediate()) {
              /* if there is a base version */ 
              VersionID bvid = mod.getWorkingID(); 
              if(bvid != null) {

                /* get the checked-in version */ 
                NodeVersion vsn = null;
                {
                  timer.acquire();
                  LoggedLock checkedInLock = getCheckedInLock(name);
                  checkedInLock.acquireReadLock();
                  try {
                    timer.resume();	
                    
                    TreeMap<VersionID,CheckedInBundle> checkedIn = null;
                    try {
                      checkedIn = getCheckedInBundles(name);
                    }
                    catch(PipelineException ex) {
                      throw new PipelineException
                        ("There are no checked-in versions of node (" + name + ") to " + 
                         "check-out!");
                    }
                    if(checkedIn == null)
                      throw new IllegalStateException(); 
                    
                    {
                      CheckedInBundle cbundle = checkedIn.get(bvid); 
                      if(cbundle == null) 
                        throw new PipelineException 
                          ("Somehow no checked-in version (" + bvid + ") of node " + 
                           "(" + name + ") exists to check-out!"); 
                      vsn = new NodeVersion(cbundle.getVersion());
                    }
                  }
                  finally {
                    checkedInLock.releaseReadLock();  
                  }
                }
              
                /* do a selective check-out */ 
                fclient.checkOutPrelinked(nodeID, vsn); 
              }
            }

            /* finally, make sure the permissions are set correctly */ 
            fclient.changeMode(nodeID, mod, !mod.isActionEnabled());
	    mod.updateLastCTimeUpdate();
	  }
	  finally {
	    releaseFileMgrClient(fclient);
	  }
	}

	/* write the new working version to disk */ 
	writeWorkingVersion(nodeID, mod);

	/* update the bundle */ 
	bundle.setVersion(mod);
      }

      /* record event */ 
      pPendingEvents.add(new PropsModifiedNodeEvent(nodeID));

      /* post-op tasks */ 
      startExtensionTasks(timer, factory);

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      workingLock.releaseWriteLock();
      pDatabaseLock.releaseReadLock();
    }      
  }
  
  /** 
   * Set the LastCTimeUpdate property of a working version. <P> 
   * 
   * 
   * @param req 
   *   The modify properties request.
   *
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to set LastCTimeUpdate on the working version.
   */
  public Object
  setLastCTimeUpdate
  (
   NodeSetLastCTimeUpdateReq req
  ) 
  {
    NodeID nodeID = req.getNodeID();
    String name = nodeID.getName(); 
    long stamp = req.getTimeStamp();

    TaskTimer timer = 
      new TaskTimer("MasterMgr.setCTimeUpdate(): " + nodeID + " [" + stamp + "]");
    
    if(!pAdminPrivileges.isMasterAdmin(req)) 
      return new FailureRsp
        (timer, "Only a user with Master Manager privileges may set the LastCTimeUpdate!"); 

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    LoggedLock workingLock = getWorkingLock(nodeID);
    workingLock.acquireWriteLock();
    try {
      timer.resume();

      /* get the working version */ 
      WorkingBundle bundle = getWorkingBundle(nodeID);
      NodeMod mod = new NodeMod(bundle.getVersion());
      if(mod.isFrozen()) 
	throw new PipelineException
	  ("The LastCTimeUpdate of frozen node (" + nodeID + ") cannot be modified!");
      
      mod.setLastCTimeUpdate(stamp); 

      /* write the new working version to disk */ 
      writeWorkingVersion(nodeID, mod);
      
      /* update the bundle */ 
      bundle.setVersion(mod);

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      workingLock.releaseWriteLock();
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    LoggedLock targetLock = getWorkingLock(targetID);
    targetLock.acquireWriteLock();
    LoggedLock downstreamLock = getDownstreamLock(source);
    downstreamLock.acquireWriteLock();
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
      
      /* prevent circular links */ 
      if(mod.getSource(source) == null)
	checkForCircularity(timer, source, targetID, 
			    new TreeSet<String>(), new Stack<String>()); 

      /* add (or modify) the link */ 
      mod.setSource(slink);
      
      /* write the new working version to disk */ 
      writeWorkingVersion(targetID, mod);
      
      /* update the bundle */ 
      bundle.setVersion(mod);

      /* update the downstream links of the source node */ 
      DownstreamLinks links = getDownstreamLinks(source); 
      links.addWorking(sourceID, targetID.getName());

      /* record event */ 
      pPendingEvents.add(new LinksModifiedNodeEvent(targetID));

      /* post-op tasks */ 
      startExtensionTasks(timer, factory);

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      downstreamLock.releaseWriteLock();
      targetLock.releaseWriteLock();
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    LoggedLock targetLock = getWorkingLock(targetID);
    targetLock.acquireWriteLock();
    LoggedLock downstreamLock = getDownstreamLock(source);
    downstreamLock.acquireWriteLock();
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

      /* record event */ 
      pPendingEvents.add(new LinksModifiedNodeEvent(targetID));

      /* post-op tasks */ 
      startExtensionTasks(timer, factory);

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      downstreamLock.releaseWriteLock();
      targetLock.releaseWriteLock();
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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

      timer.acquire();
      LoggedLock lock = getWorkingLock(nodeID);
      lock.acquireWriteLock();
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
	
	/* record event */ 
	pPendingEvents.add(new SeqsModifiedNodeEvent(nodeID));

	/* post-op tasks */ 
	startExtensionTasks(timer, factory);

	return new SuccessRsp(timer);
      }
      catch(PipelineException ex) { 
	timer.acquire();
	pNodeTree.removeSecondaryWorkingNodeTreePath(nodeID, fseq);

	return new FailureRsp(timer, ex.getMessage());
      }
      finally {
	lock.releaseWriteLock();
      }  
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    LoggedLock lock = getWorkingLock(nodeID);
    lock.acquireWriteLock();
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
      
      /* remove checksums for any obsolete files sequences */ 
      timer.acquire();
      LoggedLock clock = getCheckSumLock(nodeID);
      clock.acquireWriteLock();
      try {
        timer.resume();
        
        CheckSumBundle cbundle = getCheckSumBundle(nodeID); 
        cbundle.getCache().removeAllExcept(mod.getSequences()); 
        writeCheckSumCache(cbundle.getCache()); 
      }
      finally {
        clock.releaseWriteLock();
      }  

      /* record event */ 
      pPendingEvents.add(new SeqsModifiedNodeEvent(nodeID));

      /* post-op tasks */ 
      startExtensionTasks(timer, factory);

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      lock.releaseWriteLock();
      pDatabaseLock.releaseReadLock();
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
   * @param sessionID
   *   The unique network connection session ID.
   *
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to rename the working version.
   */
  public Object
  rename
  (
   NodeRenameReq req, 
   long sessionID
  ) 
  {
    NodeID id   = req.getNodeID();
    String name = id.getName();

    FilePattern npat = req.getFilePattern();
    String nname     = npat.getPrefix();
    NodeID nid       = new NodeID(id, nname);

    FileSeq oldPrimary = null;
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

    /* make sure the file manager can rename the files */ 
    {
      FileMgrClient fclient = acquireFileMgrClient();
      try {
        fclient.validateScratchDir();
      }
      catch(PipelineException ex) {
        return new FailureRsp(timer, ex.getMessage());
      }
      finally {
        releaseFileMgrClient(fclient);
      }
    }

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	

      if(!pAdminPrivileges.isNodeManaged(req, id)) 
	throw new PipelineException
	  ("Only a user with Node Manager privileges may rename nodes in working " + 
	   "areas owned by another user!");

      try {
	NodeCommon.validatePrimary(npat);
	NodeCommon.validateSuffix(npat.getSuffix());
      }
      catch(IllegalArgumentException ex) {
	return new FailureRsp(timer, "Unable to rename node to:\n\n" + npat + "\n\n" + 
                              ex.getMessage());
      }

      /* determine the new file sequences */ 
      FileSeq primary = null;
      SortedSet<FileSeq> secondary = null;
      {
	timer.acquire();
	LoggedLock lock = getWorkingLock(id);
	lock.acquireReadLock();
	try {
	  timer.resume();

	  WorkingBundle bundle = getWorkingBundle(id);
	  NodeMod mod = bundle.getVersion();
	  oldPrimary = mod.getPrimarySequence();
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
	  lock.releaseReadLock();
	}
      }


      /* verifying that the new node name doesn't conflict with existing node and
         reserve the new name */ 
      try {
	pNodeTree.reserveRename(id, oldPrimary, oldSeqs, nid, primary, secondary, newSeqs);
	addWorkingAreaForNode(nid);
      }
      catch(PipelineException ex) {
	return new FailureRsp(timer, ex.getMessage());
      }
      
      
      /* get the status of the node before the rename so we can reapply it after the
         rename is complete.  */
      boolean updateTimeStamps = false;
      {
        NodeStatus oldStatus = performNodeOperation(new NodeOp(), id, timer, sessionID);
        OverallQueueState qstate = oldStatus.getHeavyDetails().getOverallQueueState();
        if (qstate == OverallQueueState.Dubious || qstate == OverallQueueState.Stale)
          updateTimeStamps = true;
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

	timer.acquire();
	LoggedLock downstreamLock = getDownstreamLock(id.getName());
	downstreamLock.acquireWriteLock();
	try {
	  timer.resume();
	  
	  DownstreamLinks dsl = getDownstreamLinks(id.getName()); 
          if(dsl.hasWorking(id)) {
            for(String target : dsl.getWorking(id)) {
              NodeID targetID = new NodeID(id, target);
              
              timer.acquire();
              LoggedLock lock = getWorkingLock(targetID);
              lock.acquireReadLock();
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
                lock.releaseReadLock();
              }  
              
              timer.suspend();
              Object obj = unlink(new NodeUnlinkReq(targetID, id.getName()));
              timer.accum(((TimedRsp) obj).getTimer());
            }
          }
        }
	finally {
	  downstreamLock.releaseWriteLock();
	}
      }

      {
	timer.acquire();
	LoggedLock lock = getWorkingLock(id);
	lock.acquireWriteLock();
	LoggedLock nlock = getWorkingLock(nid);
	nlock.acquireWriteLock();
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
	    if(oaction != null) {
	      
	      /* relookup the new working version to get the added links */ 
	      nmod = getWorkingBundle(nid).getVersion();
	      
	      /* get the current action related parameters */ 
	      {
		BaseAction naction = nmod.getAction();
		/* This is necessary to preserve the LinkParam values that were cleared when
		 * all the sources were disconnected above.  Otherwise all the SingleParam
		 * except the LinkParams are copied, but the LinkParam values are missing*/
		naction.setSingleParamValues(oaction);
		
		if (oaction.supportsSourceParams() && 
                    (!oaction.getSourceNames().isEmpty() || 
                     !oaction.getSecondarySourceNames().isEmpty()))
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
	    releaseHelper(id, false, false, false, timer);
	  }
	  
	  /* rename the files */ 
	  if(req.renameFiles()) {
	    FileMgrClient fclient = acquireFileMgrClient();
	    try {
	      fclient.rename(id, omod, npat);  
	    }
	    finally {
	      releaseFileMgrClient(fclient);
	    }
	  }
	  
	  /* now need to update the timestamps in the node mod if the node should 
             be dubious or stale */
	  if(updateTimeStamps) {
	    nmod = getWorkingBundle(nid).getVersion();
	    nmod.updateLastCTimeUpdate();
	    nmod.initTimeStamps();

            /* write the new working version to disk */ 
            writeWorkingVersion(nid, nmod);

            /* no need to update the bundle here since "nmod" is live */ 
          }
	  
          /* copy checksums from old to new nodes */ 
          {
            /* retrieve and remove old cache */ 
            CheckSumCache ocache = null;
            {
              timer.acquire();
              LoggedLock clock = getCheckSumLock(id);
              clock.acquireWriteLock();
              try {
                timer.resume();
                
                CheckSumBundle cbundle = getCheckSumBundle(id);   
                ocache = cbundle.getCache(); 

                timer.acquire();
                synchronized(pCheckSumBundles) {
                  timer.resume();
                  pCheckSumBundles.remove(id.getName(), id); 
                }

                decrementCheckSumCounter(id); 
                removeCheckSumCache(id);
              }
              finally {
                clock.releaseWriteLock();
              } 
            }

            /* create and save new cache */ 
            {
              ArrayList<String> ofnames = new ArrayList<String>(); 
              for(Path path : omod.getPrimarySequence().getPaths()) 
                ofnames.add(path.toString()); 
                
              ArrayList<String> nfnames = new ArrayList<String>();
              {
                File path = new File(npat.getPrefix());
                FilePattern pat =
                  new FilePattern(path.getName(), npat.getPadding(), npat.getSuffix());
                FileSeq nfseq = new FileSeq(pat, omod.getPrimarySequence().getFrameRange());
                for(Path npath : nfseq.getPaths()) 
                  nfnames.add(npath.toString()); 
              }

              for(FileSeq fseq : omod.getSecondarySequences()) {
                for(Path path : fseq.getPaths()) {
                  ofnames.add(path.toString()); 
                  nfnames.add(path.toString()); 
                }
              }

              /* first copy checksums from old to new names, 
                   then lookup the timestamps of the newly named files and set the updated-on
                   stamps for the checksums accordingly */ 
              CheckSumCache ncache = new CheckSumCache(nid, nfnames, ofnames, ocache);
              {
                FileMgrClient fclient = acquireFileMgrClient();
                try {
                  ArrayList<Long> stamps = fclient.getWorkingTimeStamps(nid, nfnames); 
                  int wk;
                  for(wk=0; wk<stamps.size(); wk++) {
                    String fname = nfnames.get(wk);
                    Long stamp = stamps.get(wk);
                    if(stamp != null) 
                      ncache.replaceUpdatedOn(fname, stamp);
                    else 
                      ncache.remove(fname);
                  }
                }
                finally {
                  releaseFileMgrClient(fclient);
                }
              }
              
              /* update the save the checksum bundle to disk */ 
              timer.acquire();
              LoggedLock clock = getCheckSumLock(nid);
              clock.acquireWriteLock();
              try {
                timer.resume();
              
                CheckSumBundle cbundle = getCheckSumBundle(nid);   
                cbundle.setCache(ncache);  
                writeCheckSumCache(cbundle.getCache()); 
              }
              finally {
                clock.releaseWriteLock();
              } 
            }
          }
	}
	finally {
	  nlock.releaseWriteLock();
	  lock.releaseWriteLock();
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
		timer.acquire();
		LoggedLock lock = getWorkingLock(targetID);
		lock.acquireReadLock();
		try {
		  timer.resume();
		  targetMod = new NodeMod(getWorkingBundle(targetID).getVersion());
		}
		finally {
		  lock.releaseReadLock();
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
      
      /**
       * Removes the annotations. This is here because you want this to run after 
       * the post tasks have started in case it throws an error and aborts this
       * method.  If it does throw an error, it will be reported to the user as
       * an actual error, even though all that failed was the annotation removal. 
       */
      {
        TreeMap<String, BaseAnnotation> annots = getAnnotationsHelper(timer, name);
        if(annots != null) {
          TaskTimer child = new TaskTimer("MasterMgr.removeAnnotationHelper()");
          timer.suspend();
          Object removed = removeAnnotationsHelper(req, child, name, true);
          timer.accum(child);
          
          child = new TaskTimer("MasterMgr.addAnnotationHelper()");
          timer.suspend();
          Object added = addAnnotationsHelper(req, child, nname, annots);
          timer.accum(child);
          
          if (removed instanceof FailureRsp) {
            FailureRsp rsp = (FailureRsp) removed;
            throw new PipelineException
            ("After the node was successfully renamed, " +
              "a failure occured when trying to remove the annotations from the ex-node: " + 
              rsp.getMessage());
          }

          if (added instanceof FailureRsp) {
            FailureRsp rsp = (FailureRsp) removed;
            throw new PipelineException
            ("After the node was successfully renamed, " +
              "a failure occured when trying to add annotations to the new node: " + 
              rsp.getMessage());
          }
        }
      }

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      /* remove the new working node entry, primary and secondary sequences */ 
      pNodeTree.removeWorkingNodeTreePath(nid, newSeqs); 
      
      /* restore the old working node entry, primary and secondary sequences */ 
      addWorkingNodeTreePath(id, oldPrimary, oldSeqs);

      /* abort */ 
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    LoggedLock lock = getWorkingLock(nodeID);
    lock.acquireWriteLock(); 
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
	FileMgrClient fclient = acquireFileMgrClient();
	try {
	  fclient.remove(nodeID, obsolete);
	}
	finally {
	  releaseFileMgrClient(fclient);
	}
      }

      /* remove obsolete frames from checksum cache */ 
      timer.acquire();
      LoggedLock clock = getCheckSumLock(nodeID);
      clock.acquireWriteLock();
      try {
        timer.resume();
        
        CheckSumBundle cbundle = getCheckSumBundle(nodeID);   
        cbundle.getCache().removeAllExcept(mod.getSequences());
        writeCheckSumCache(cbundle.getCache()); 
      }
      finally {
        clock.releaseWriteLock();
      } 

      /* check for unfinished jobs associated with the obsolete files */ 
      if(!obsolete.isEmpty()) {
        QueueMgrControlClient qclient = acquireQueueMgrClient();
        try {
	  TreeSet<Long> jobIDs = qclient.getUnfinishedJobsForNodeFiles(nodeID, obsolete);
          if(!jobIDs.isEmpty()) 
            return new QueueGetUnfinishedJobsForNodeFilesRsp(timer, jobIDs);
        }
        finally {
          releaseQueueMgrClient(qclient);
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
      lock.releaseWriteLock();
      pDatabaseLock.releaseReadLock();
    }    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C H E C K E D - I N   V E R S I O N S                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the nodes with at least one checked-in version who's name matches 
   * the given search pattern.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>NodeGetCheckedInNamesRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> on failure.
   */ 
  public Object 
  getCheckedInNames
  ( 
   NodeGetNodeNamesReq req 
  ) 
  {
    String pattern = req.getPattern();  

    TaskTimer timer = new TaskTimer("MasterMgr.getCheckedInNames(): [" + pattern + "]");

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	

      try {
	Pattern pat = null;
	if(pattern != null) 
	  pat = Pattern.compile(pattern);
	
	TreeSet<String> matches = pNodeTree.getMatchingCheckedInNodes(pat);

	return new NodeGetNodeNamesRsp(timer, matches);
      }
      catch(PatternSyntaxException ex) {
	return new FailureRsp(timer, 
			      "Illegal Node Name Pattern:\n\n" + ex.getMessage());
      }
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }
  }  

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
   NodeGetByNameReq req
  ) 
  {
    String name = req.getName();
    TaskTimer timer = new TaskTimer("MasterMgr.getCheckedInVersionIDs(): " + name);

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    LoggedLock lock = getCheckedInLock(name);
    lock.acquireReadLock();
    try {
      timer.resume();	

      TreeMap<VersionID,CheckedInBundle> checkedIn = getCheckedInBundles(name);
      TreeSet<VersionID> vids = new TreeSet<VersionID>(checkedIn.keySet());

      return new NodeGetVersionIDsRsp(timer, vids);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      lock.releaseReadLock();
      pDatabaseLock.releaseReadLock();
    }  
  }  
    
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
  getMultiCheckedInVersionIDs
  ( 
   NodeGetByNamesReq req
  ) 
  {
    TreeSet<String> names = req.getNames();
    TaskTimer timer = new TaskTimer("MasterMgr.getCheckedInVersionIDs(): [multiple]");

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();
    
      MappedSet<String,VersionID> vids = new MappedSet<String,VersionID>();
      for(String name : names) {
        timer.acquire();
        LoggedLock lock = getCheckedInLock(name);
        lock.acquireReadLock();
        try {
          timer.resume();	
        
          TreeMap<VersionID,CheckedInBundle> checkedIn = getCheckedInBundles(name);
          vids.put(name, new TreeSet<VersionID>(checkedIn.keySet())); 
        }
        catch(PipelineException ex) {
          // silently ignore missing nodes
        }
        finally {
          lock.releaseReadLock();
        }
      }

      return new NodeGetMultiVersionIDsRsp(timer, vids);
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }  
  }  
    
  /** 
   * Get the revision numbers of all checked-in versions of a node which do not save 
   * intermediate (temporary) version of files in the repository. <P>
   * 
   * @param req 
   *   The get checked-in version request.
   * 
   * @return
   *   <CODE>NodeGetCheckedInVersionIDsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to retrieve the checked-in version.
   */
  public Object
  getIntermediateVersionIDs
  ( 
   NodeGetByNameReq req
  ) 
  {
    String name = req.getName();
    TaskTimer timer = new TaskTimer("MasterMgr.getIntermediateVersionIDs(): [mutiple]"); 

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    LoggedLock lock = getCheckedInLock(name);
    lock.acquireReadLock();
    try {
      timer.resume();	

      TreeMap<VersionID,CheckedInBundle> checkedIn = getCheckedInBundles(name);
      TreeSet<VersionID> vids = new TreeSet<VersionID>(); 
      for(Map.Entry<VersionID,CheckedInBundle> entry : checkedIn.entrySet()) {
        if(entry.getValue().getVersion().isIntermediate()) 
          vids.add(entry.getKey());
      }

      return new NodeGetVersionIDsRsp(timer, vids);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      lock.releaseReadLock();
      pDatabaseLock.releaseReadLock();
    }  
  }  
        
  /** 
   * Get the revision numbers of all checked-in versions of a node which do not save 
   * intermediate (temporary) version of files in the repository. <P>
   * 
   * @param req 
   *   The get checked-in version request.
   * 
   * @return
   *   <CODE>NodeGetCheckedInVersionIDsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to retrieve the checked-in version.
   */
  public Object
  getMultiIntermediateVersionIDs
  ( 
   NodeGetByNamesReq req
  ) 
  {
    TreeSet<String> names = req.getNames();
    TaskTimer timer = new TaskTimer("MasterMgr.getIntermediateVersionIDs(): [mutiple]");

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();
    
      MappedSet<String,VersionID> vids = new MappedSet<String,VersionID>();
      for(String name : names) {
        timer.acquire();
        LoggedLock lock = getCheckedInLock(name);
        lock.acquireReadLock();
        try {
          timer.resume();	

          TreeMap<VersionID,CheckedInBundle> checkedIn = getCheckedInBundles(name);
          for(Map.Entry<VersionID,CheckedInBundle> entry : checkedIn.entrySet()) {
            if(entry.getValue().getVersion().isIntermediate()) 
              vids.put(name, entry.getKey());
          }
        }
        catch(PipelineException ex) {
          // silently ignore missing nodes
        }
        finally {
          lock.releaseReadLock();
        }
      }

      return new NodeGetMultiVersionIDsRsp(timer, vids);
    }
    finally {
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    LoggedLock lock = getCheckedInLock(name);
    lock.acquireReadLock();
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
      lock.releaseReadLock();
      pDatabaseLock.releaseReadLock();
    }  
  }  
      
  /** 
   * Get the checked-in versions for each of the given nodes. <P> 
   * 
   * @param req 
   *   The get checked-in versions request.
   * 
   * @return
   *   <CODE>NodeGetMultiCheckedInRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to retrieve the checked-in versions.
   */
  public Object
  getMultiCheckedInVersions
  ( 
   NodeGetMultiCheckedInReq req
  ) 
  {	 
    TaskTimer timer = new TaskTimer();

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	

      DoubleMap<String,VersionID,NodeVersion> results =
        new DoubleMap<String,VersionID,NodeVersion>();

      MappedSet<String,VersionID> versions = req.getVersionIDs();
      for(String name : versions.keySet()) {

        timer.acquire();
        LoggedLock lock = getCheckedInLock(name);
        lock.acquireReadLock();
        try {
          timer.resume();	

          TreeMap<VersionID,CheckedInBundle> checkedIn = getCheckedInBundles(name);
          for(VersionID vid : versions.get(name)) {
            CheckedInBundle bundle = checkedIn.get(vid);
            if(bundle == null) 
              throw new PipelineException 
                ("Somehow no checked-in version (" + vid + ") of node " + 
                 "(" + name + ") exists!"); 
            
            results.put(name, vid, new NodeVersion(bundle.getVersion()));
          }
        }
        finally {
          lock.releaseReadLock();
        }
      }

      return new NodeGetMultiCheckedInRsp(timer, results); 
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      pDatabaseLock.releaseReadLock();
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
   NodeGetByNameReq req
  ) 
  {	 
    TaskTimer timer = new TaskTimer();

    String name = req.getName();

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    LoggedLock lock = getCheckedInLock(name);
    lock.acquireReadLock();
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
      lock.releaseReadLock();
      pDatabaseLock.releaseReadLock();
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
   NodeGetByNameReq req
  ) 
  {	 
    TaskTimer timer = new TaskTimer();
  
    String name = req.getName();

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    LoggedLock lock = getCheckedInLock(name);
    lock.acquireReadLock();
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
      lock.releaseReadLock();
      pDatabaseLock.releaseReadLock();
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
  getMultiHistory
  ( 
   NodeGetByNamesReq req
  ) 
  {	 
    TaskTimer timer = new TaskTimer();

    TreeSet<String> names = req.getNames();

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();

      DoubleMap<String,VersionID,LogMessage> history = 
        new DoubleMap<String,VersionID,LogMessage>();
      for(String name : names) {
        timer.acquire();
        LoggedLock lock = getCheckedInLock(name);
        lock.acquireReadLock();
        try {
          timer.resume();	

          TreeMap<VersionID,CheckedInBundle> checkedIn = getCheckedInBundles(name);
          for(VersionID vid : checkedIn.keySet()) 
            history.put(name, vid, checkedIn.get(vid).getVersion().getLogMessage());
        }
        catch(PipelineException ex) {
          // silently ignore missing nodes
        }
        finally {
          lock.releaseReadLock();
        }
      }

      return new NodeGetMultiHistoryRsp(timer, history);
    }
    finally {
      pDatabaseLock.releaseReadLock();
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
   NodeGetByNameReq req
  ) 
  {	 
    TaskTimer timer = new TaskTimer();
  
    String name = req.getName();

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    LoggedLock lock = getCheckedInLock(name);
    lock.acquireReadLock();
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
      lock.releaseReadLock();
      pDatabaseLock.releaseReadLock();
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
   NodeGetByNameReq req
  ) 
  {	 
    TaskTimer timer = new TaskTimer();
  
    String name = req.getName();

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    LoggedLock lock = getCheckedInLock(name);
    lock.acquireReadLock();
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
      lock.releaseReadLock();
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();

      MappedSet<String,VersionID> dnodes = null;
      {
	timer.acquire();
	LoggedLock downstreamLock = getDownstreamLock(name);
	downstreamLock.acquireReadLock();
	try {
	  timer.resume();	

	  DownstreamLinks dlinks = getDownstreamLinks(name);
	  dnodes = dlinks.getCheckedIn(vid);
	}
	finally {
	  downstreamLock.releaseReadLock();
	}
      }
	
      DoubleMap<String,VersionID,LinkVersion> links = 
	new DoubleMap<String,VersionID,LinkVersion>();

      if(dnodes != null) {
	for(String dname : dnodes.keySet()) {

	  timer.acquire();
	  LoggedLock lock = getCheckedInLock(dname);
	  lock.acquireReadLock();
	  try {
	    timer.resume();

	    TreeMap<VersionID,CheckedInBundle> checkedIn = getCheckedInBundles(dname);

            for(VersionID dvid : dnodes.get(dname)) {
	      NodeVersion vsn = checkedIn.get(dvid).getVersion();
	      links.put(dname, dvid, vsn.getSource(name));
	    }
	  }
	  finally {
	    lock.releaseReadLock();
	  }
	}
      }
	
      return new NodeGetDownstreamCheckedInLinksRsp(timer, links);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      pDatabaseLock.releaseReadLock();
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
   * @param opn
   *   The operation progress notifier.
   * 
   * @param sessionID
   *   The unique network connection session ID.
   *
   * @return
   *   <CODE>NodeStatusRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to get the status of the node.
   */ 
  public Object
  status
  ( 
   NodeStatusReq req,   
   OpNotifiable opn, 
   long sessionID
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();
      
      NodeOp nodeOp = null;
      if(!req.getLightweight()) 
        nodeOp = new StatusNodeOp(opn);

      NodeID nodeID = req.getNodeID();
      DownstreamMode dmode = req.getDownstreamMode();

      NodeStatus root = performNodeOperation(nodeOp, nodeID, dmode, timer, sessionID);

      return new NodeStatusRsp(timer, nodeID, root);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }    
    finally {
      pDatabaseLock.releaseReadLock();
    }
  }
  
  /** 
   * Get the status of multiple overlapping trees of nodes. <P> 
   * 
   * For each of the root nodes given, either a lightweight or heavyweight node status can 
   * be performed on the node and its upstream dependencies.  For nodes which are upstream of
   * multiple root nodes, heavyweight status is performed in preference to lightweight status.
   * The ability to specify different status modes at each root node means that the returned 
   * NodeStatus datastructures can contain mixtures of lightweight and heavyweight node 
   * details. See the {@link NodeDetails} class for more information about the information
   * available for each mode.<P> 
   * 
   * This method returns a {@link NodeStatus} instance for each of the given root nodes.  
   * A <CODE>NodeStatus</CODE> can be used access the status of all nodes (both upstream 
   * and downstream) linked to the given node.  The status information for the upstream 
   * nodes will also include detailed state and version information accessable by calling 
   * the {@link NodeStatus#getDetails NodeStatus.getDetails} method.<P> 
   * 
   * Note that when computing node status where the given root nodes share a large percentage
   * of thier upstream nodes, this method will be much more efficient than calling the single
   * node {@link #status status} method for each root node seperately. 
   * 
   * @param req 
   *   The node status request.
   * 
   * @param opn
   *   The operation progress notifier.
   * 
   * @param sessionID
   *   The unique network connection session ID.
   *
   * @return
   *   <CODE>NodeStatusRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to get the status of the node.
   */ 
  public Object
  multiStatus
  ( 
   NodeMultiStatusReq req,   
   OpNotifiable opn, 
   long sessionID
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();

      String author = req.getAuthor();
      String view = req.getView();
      TreeSet<String> rootNames = req.getRootNames();
      TreeSet<String> heavyNames = req.getHeavyNames();
      DownstreamMode dmode = req.getDownstreamMode();

      TreeMap<String,NodeStatus> results = new TreeMap<String,NodeStatus>();

      /* filter out missing root nodes */ 
      TreeSet<String> foundRoots = new TreeSet<String>();
      TreeSet<String> foundHeavy = new TreeSet<String>();
      {
        TreeSet<String> foundNames = new TreeSet<String>(); 
                
        TreeSet<String> allNames = new TreeSet<String>();
        allNames.addAll(rootNames);
        allNames.addAll(heavyNames);

        for(String name : allNames) {
          boolean found = false;

          NodeID nodeID = new NodeID(author, view, name); 
          timer.acquire();
          LoggedLock workingLock = getWorkingLock(nodeID);
          workingLock.acquireReadLock();
          LoggedLock checkedInLock = getCheckedInLock(name);
          checkedInLock.acquireReadLock();
          try {
            timer.resume();	
              
            try {
              getWorkingBundle(nodeID);
              found = true;
            }
            catch(PipelineException ex) {
            }

            if(!found) {
              try {
                getCheckedInBundles(name);
                found = true;
              }
              catch(PipelineException ex) {
              }
            }
          }
          finally {
            checkedInLock.releaseReadLock();  
            workingLock.releaseReadLock();
          }

          if(found) 
            foundNames.add(name); 
        }
        
        for(String name : rootNames) {
          if(foundNames.contains(name))
            foundRoots.add(name);
          else 
            results.put(name, null);
        }

        for(String name : heavyNames) {
          if(foundNames.contains(name))
            foundHeavy.add(name);          
        }
      }

      /* compute the node status */ 
      {
        TreeMap<String,NodeStatus> cache = new TreeMap<String,NodeStatus>();

        /* populate the cache with heavyweight status first */ 
        for(String name : foundHeavy) {
          boolean isRoot = foundRoots.contains(name);
          NodeID nodeID = new NodeID(author, view, name);
          DownstreamMode dm = isRoot ? dmode : DownstreamMode.None;
          NodeStatus status = 
            performNodeOperation(new StatusNodeOp(opn), nodeID, dm, cache, timer, sessionID);

          /* if its also one of the root nodes, add the original node status to the results */
          if(isRoot) 
            results.put(name, status);

          /* replace the cached root node status with a copy without targets, 
               this way the orignal's targets won't get stomped on when its looked up 
               from the cache during future performNodeOperation() calls */ 
          cache.put(name, new NodeStatus(status, false)); 
        }

        /* do lightweight status for all remaining roots */ 
        for(String name : foundRoots) {
          if(!foundHeavy.contains(name)) {
            NodeID nodeID = new NodeID(author, view, name);
            NodeStatus status = 
              performNodeOperation(null, nodeID, dmode, cache, timer, sessionID);

            /* add the original node status to the results */
            results.put(name, status);  

            /* replace the cached root node status with a copy without targets, 
                 this way the orignal's targets won't get stomped on when its looked up 
                 from the cache during future performNodeOperation() calls */ 
            cache.put(name, new NodeStatus(status, false)); 
          }
        }
      }

      return new NodeMultiStatusRsp(timer, results);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }    
    finally {
      pDatabaseLock.releaseReadLock();
    }
  }
  
  /** 
   * Get the downstream only status of multiple nodes. <P> 
   * 
   * For each of the root nodes given, a <CODE>NodeStatus</CODE> instance will be returned
   * which access the status of all nodes downstream linked to the given node according the 
   * the criteria specified by the given downtream mode. <P> 
   * 
   * This method returns a table containing {@link NodeStatus} instances for each of the 
   * given nodes in the <CODE>rootNames</CODE> set indexed by their fully resolved node names.
   * If status for a root node is requested and the node does not exist, then the entry in 
   * this table for the missing node will be <CODE>null</CODE>.  To enable partial completion 
   * of this method when specified both existing and missing root nodes, a PipelineException 
   * will not be thrown when only a subset of the nodes are missing. <P> 
   * 
   * Note that all returned <CODE>NodeStatus</CODE> instances will not contain any detailed
   * status information, just the minimal status and connectivity information. <P> 
   * 
   * @param req 
   *   The node status request.
   *
   * @return
   *   <CODE>NodeStatusRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to get the status of the node.
   */ 
  public Object
  downstreamStatus
  ( 
   NodeDownstreamStatusReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();

      String author = req.getAuthor();
      String view = req.getView();
      TreeSet<String> rootNames = req.getRootNames();
      DownstreamMode dmode = req.getDownstreamMode();

      TreeMap<String,NodeStatus> results = new TreeMap<String,NodeStatus>();

      /* filter out missing root nodes */ 
      TreeSet<String> foundRoots = new TreeSet<String>();
      {
        TreeSet<String> foundNames = new TreeSet<String>(); 

        for(String name : rootNames) {
          boolean found = false;

          NodeID nodeID = new NodeID(author, view, name); 
          timer.acquire();
          LoggedLock workingLock = getWorkingLock(nodeID);
          workingLock.acquireReadLock();
          LoggedLock checkedInLock = getCheckedInLock(name);
          checkedInLock.acquireReadLock();
          try {
            timer.resume();	
              
            try {
              getWorkingBundle(nodeID);
              found = true;
            }
            catch(PipelineException ex) {
            }

            if(!found) {
              try {
                getCheckedInBundles(name);
                found = true;
              }
              catch(PipelineException ex) {
              }
            }
          }
          finally {
            checkedInLock.releaseReadLock();  
            workingLock.releaseReadLock();
          }

          if(found) 
            foundNames.add(name); 
        }
        
        for(String name : rootNames) {
          if(foundNames.contains(name))
            foundRoots.add(name);
          else 
            results.put(name, null);
        }
      }

      /* compute downstream status for each root */ 
      for(String name : foundRoots) {
        NodeID nodeID = new NodeID(author, view, name);
        NodeStatus root = new NodeStatus(nodeID);
        getDownstreamNodeStatus(root, nodeID, dmode, timer);
        results.put(name, root);  
      }

      return new NodeMultiStatusRsp(timer, results);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }    
    finally {
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	
      
      if(!pAdminPrivileges.isNodeManaged(req, nodeID)) 
	throw new PipelineException
	  ("Only a user with Node Manager privileges may register nodes owned " + 
	   "by another user!");

      /* make sure the named toolset exits */ 
      if(!isValidToolsetName(mod.getToolset()))
         throw new PipelineException
           ("Unable to register the node (" + mod.getName()  + ") because its Toolset " + 
            "property (" + mod.getToolset() + ") names a non-existent toolset!"); 

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
      
      timer.acquire();
      LoggedLock lock = getWorkingLock(nodeID);
      lock.acquireWriteLock();
      try {
	timer.resume();
	
	/* write the new working version to disk */
	writeWorkingVersion(nodeID, mod);	
	
	/* create a working bundle for the new working version */ 
        timer.acquire();
	synchronized(pWorkingBundles) {
          timer.resume();
	  TreeMap<NodeID,WorkingBundle> table = pWorkingBundles.get(name);
	  if(table == null) {
	    table = new TreeMap<NodeID,WorkingBundle>();
	    pWorkingBundles.put(name, table);
	  }
	  table.put(nodeID, new WorkingBundle(mod));
	}
	
	/* keep track of the change to the node version cache */ 
	incrementWorkingCounter(nodeID); 

	/* record event */ 
	pPendingEvents.add(new RegisteredNodeEvent(nodeID));
	
	/* touch the files if the node mod has no action */
	if(mod.getAction() == null) {
	  FileMgrClient fclient =  acquireFileMgrClient();
	  try {
	    fclient.touchAll(nodeID, mod);
	  }
	  finally {
	    releaseFileMgrClient(fclient);
	  }
	}

        /* create an checksum cache for the new working version */ 
        {
          timer.acquire();
          LoggedLock clock = getCheckSumLock(nodeID);
          clock.acquireWriteLock();
          try {
            timer.resume();

            CheckSumBundle cbundle = getCheckSumBundle(nodeID);     
            writeCheckSumCache(cbundle.getCache()); 
          }
          finally {
            clock.releaseWriteLock();
          }  
        }

	/* post-op tasks */ 
	startExtensionTasks(timer, factory);
	
	NodeMod returnMod = new NodeMod(getWorkingBundle(req.getNodeID()).getVersion());
	return new NodeGetWorkingRsp(timer, req.getNodeID(), returnMod);
      }
      finally {
	lock.releaseWriteLock();
      }  
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      pDatabaseLock.releaseReadLock();
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
   * @param opn
   *   The operation progress notifier.
   * 
   * @param sessionID
   *   The unique network connection session ID.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to release the working version.
   */
  public Object
  release
  (
   NodeReleaseReq req, 
   OpNotifiable opn, 
   long sessionID 
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	
    
      if(!pAdminPrivileges.isNodeManaged(req, author)) 
	throw new PipelineException
	  ("Only a user with Node Manager privileges may release nodes in working " + 
	   "areas owned by another user!");

      opn.notify(timer, "Validating Nodes..."); 

      /* determine the link relationships between the nodes being released */ 
      TreeMap<String,NodeLinks> all = new TreeMap<String,NodeLinks>();
      {
	for(String name : nodeNames) 
	  all.put(name, new NodeLinks(name));

	for(String name : all.keySet()) {
	  NodeLinks links = all.get(name);
	  NodeID nodeID = new NodeID(author, view, name);
	  
	  timer.acquire();
	  LoggedLock lock = getWorkingLock(nodeID);
	  lock.acquireReadLock(); 
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
	    lock.releaseReadLock();
	  }    
	}
      }
      
      /* estimate the number of individual node releases */ 
      opn.setTotalSteps(all.size());

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
	  releaseHelper(new NodeID(author, view, name), removeFiles, true, true, timer);
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

        opn.step(timer, "Released: " + name); 
        checkForCancellation(sessionID);
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
      
      /* Removes the annotations from released Pending working versions.
       *
       * This is here because you want this to run after the post tasks have started 
       * in case it throws an error and aborts this method.  If it does throw an error, 
       * it will be reported to the user as an actual error, even though all that failed 
       * was the annotation removal. 
       */
      {
        TreeMap<String, FailureRsp> errors = new TreeMap<String, FailureRsp>();
        for (String name : nodeNames) {
          if (!pNodeTree.isNameCheckedIn(name)) {
            TaskTimer child = new TaskTimer("MasterMgr.removeAnnotationsHelper()");
            timer.suspend();
            Object returned = removeAnnotationsHelper(req, child, name, true);
            timer.accum(child);
            if (returned instanceof FailureRsp) {
              errors.put(name, (FailureRsp) returned);
            }
          }
        }
        if (!errors.isEmpty()) {
          String msg = 
            "After the nodes were successfully released, " +
            "a failure occured when trying to remove annotations from the nodes:\n ";
          for (String each : errors.keySet()) {
            FailureRsp rsp = errors.get(each);
            msg += ("Node (" + each + ") had the following error: " + rsp.getMessage());
          }
          throw new PipelineException(msg);
        }
      }
      
      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      pDatabaseLock.releaseReadLock();
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
   *   Whether the files associated with the working version be deleted.
   * 
   * @param removeNodeTreePath
   *   Whether the node tree path entries for the working version be removed.
   * 
   * @param removeCheckSumCache
   *   Whether to delete any existing checksum cache for the released node. 
   *
   * @param timer
   *   The task timer. 
   */
  private void 
  releaseHelper
  (
   NodeID nodeID, 
   boolean removeFiles, 
   boolean removeNodeTreePath,
   boolean removeCheckSumCache, 
   TaskTimer timer
  )
    throws PipelineException 
  {
    String name = nodeID.getName();

    /* unlink the downstream working versions from the to be released working version */ 
    {
      timer.acquire();
      LoggedLock downstreamLock = getDownstreamLock(name);
      downstreamLock.acquireWriteLock();
      try {
	timer.resume();
	  
	DownstreamLinks dsl = getDownstreamLinks(name); 
        TreeSet<String> targets = dsl.getWorking(nodeID);
        if(targets != null) {
          for(String target : targets) {
            NodeID targetID = new NodeID(nodeID, target);
            
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
      finally {
	downstreamLock.releaseWriteLock();
      }
    }
      
    timer.acquire();
    LoggedLock lock = getWorkingLock(nodeID);
    lock.acquireWriteLock();
    try {
      timer.resume();

      WorkingBundle bundle = getWorkingBundle(nodeID);
      if(bundle == null) 
	throw new PipelineException
	  ("No working version (" + nodeID + ") exists to be released.");
      NodeMod mod = bundle.getVersion();
	
      /* kill any active jobs associated with the node */
      killActiveJobs(nodeID, mod.getTimeStamp(), mod.getPrimarySequence());
	
      /* remove the bundle */ 
      timer.acquire();
      synchronized(pWorkingBundles) {
        timer.resume();
	TreeMap<NodeID,WorkingBundle> table = pWorkingBundles.get(name);
	table.remove(nodeID);
      }
      
      /* keep track of the change to the node version cache */ 
      decrementWorkingCounter(nodeID);

      /* remove the working version node file(s) */ 
      {
	File file   = new File(pNodeDir, nodeID.getWorkingPath().toString());
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

	File root = 
          new File(pNodeDir, "working/" + nodeID.getAuthor() + "/" + nodeID.getView());

	deleteEmptyParentDirs(root, 
                              new File(pNodeDir, nodeID.getWorkingParent().toString()));
      }
      
      /* update the downstream links of this node */ 
      {
	timer.acquire();	
	LoggedLock downstreamLock = getDownstreamLock(name);
	downstreamLock.acquireWriteLock();
	try {
	  timer.resume();
	    
	  DownstreamLinks links = getDownstreamLinks(name); 
	  links.removeAllWorking(nodeID);
	}  
	finally {
	  downstreamLock.releaseWriteLock();
	} 
      }
	
      /* update the downstream links of the source nodes */ 
      for(LinkMod link : mod.getSources()) {
	String source = link.getName();
	  
	timer.acquire();	
	LoggedLock downstreamLock = getDownstreamLock(source);
	downstreamLock.acquireWriteLock();
	try {
	  timer.resume();

	  NodeID sourceID = new NodeID(nodeID, source);
	  DownstreamLinks links = getDownstreamLinks(source); 
	  links.removeWorking(sourceID, name);
	}  
	finally {
	  downstreamLock.releaseWriteLock();
	}    
      }
	
      /* remove the node tree path */ 
      if(removeNodeTreePath) 
	pNodeTree.removeWorkingNodeTreePath(nodeID, mod.getSequences());

      /* remove the associated files */ 
      if(removeFiles) {
	FileMgrClient fclient = acquireFileMgrClient();
	try {
	  fclient.removeAll(nodeID, mod.getSequences());
	}
	finally {
	  releaseFileMgrClient(fclient);
	}	
      }
      
      /* remove the checksum cache for the released working version */ 
      if(removeCheckSumCache) {
        timer.acquire();
        LoggedLock clock = getCheckSumLock(nodeID);
        clock.acquireWriteLock();
        try {
          synchronized(pCheckSumBundles) {
            timer.resume();
            pCheckSumBundles.remove(name, nodeID); 
          }

          decrementCheckSumCounter(nodeID); 
          removeCheckSumCache(nodeID); 
        }
        finally {
          clock.releaseWriteLock();
        }  
      }

      /* record event */ 
      pPendingEvents.add(new ReleasedNodeEvent(nodeID));
    }
    finally {
      lock.releaseWriteLock();
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
    DeleteExtFactory factory = new DeleteExtFactory(req.getRequestor(), name, removeFiles);
    try {
      performExtensionTests(timer, factory);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }

    if(!pAdminPrivileges.isMasterAdmin(req)) 
      return new FailureRsp
        (timer, "Only a user with Master Admin privileges may delete nodes!"); 

    timer.acquire();
    if(!pDatabaseLock.tryWriteLock(sDeleteTimeout)) {
      return new FailureRsp
        (timer, "Giving up after attempting to Delete node (" + name + ") for " + 
         "(" + TimeStamps.formatInterval(sDeleteTimeout) + ").\n\n" + 
         "The Pipeline server is simply too busy at the moment to allow Delete " + 
         "operations to proceed without causing unacceptable delays for other " + 
         "users.  Please try again later, preferably during non-peak hours."); 
    }
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

      /* make sure none of the checked-in versions are required by downstream nodes */ 
      {
	boolean failed = false;
	StringBuilder buf = new StringBuilder();
	for(VersionID vid : checkedIn.keySet()) {
          if(dsl.hasCheckedIn(vid)) {
	    failed = true;
	    buf.append("\nChecked-in versions downstream of the (" + vid + ") version:\n");

            MappedSet<String,VersionID> dlinks = dsl.getCheckedIn(vid);
	    for(String dname : dlinks.keySet()) {
              for(VersionID dvid : dlinks.get(dname)) 
                buf.append("  " + dname + "  (" + dvid + ")\n");
            }
	  }
	}

	if(failed) {
	  throw new PipelineException
	    ("Cannot delete node (" + name + ") because links to the following " +
	     "checked-in versions exit in the repository:\n" + 
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
	  releaseHelper(nodeID, removeFiles, true, true, timer);
	  pWorkingLocks.remove(nodeID);
	}
	
	if(!pWorkingBundles.get(name).isEmpty())
	  throw new IllegalStateException(); 
	pWorkingBundles.remove(name);

	pCheckSumBundles.remove(name);
      }
	
      /* delete the checked-in versions */ 
      if(!checkedIn.isEmpty()) {

	/* remove the downstream links to this node from the checked-in source nodes */ 
        {
          TreeSet<String> snames = new TreeSet<String>();
          for(VersionID vid : checkedIn.keySet()) {
            NodeVersion vsn = checkedIn.get(vid).getVersion();
            snames.addAll(vsn.getSourceNames());
          }

          for(String sname : snames) {
            DownstreamLinks sdsl = getDownstreamLinks(sname);
            sdsl.removeAllCheckedIn(name);
          }
        }

	/* delete files associated with all checked-in versions of the node */ 
	{
	  FileMgrClient fclient = acquireFileMgrClient();
	  try {
	    fclient.deleteCheckedIn(name);
	  }
	  finally {
	    releaseFileMgrClient(fclient);
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
        decrementCheckedInCounters(name, checkedIn.keySet());
      }

      /* remove the downstream links file and entry */ 
      {
        removeDownstreamLinks(name); 
	pDownstream.remove(name);
      }

      /* remove the leaf node tree entry */ 
      pNodeTree.removeNodeTreePath(name);
      
      /* post-op tasks */ 
      startExtensionTasks(timer, factory);
      
      /* Removes the annotations. This is here because you want this to run after 
       * the post tasks have started in case it throws an error and aborts this
       * method.  If it does throw an error, it will be reported to the user as
       * an actual error, even though all that failed was the annotation removal. 
       */
      {
        TaskTimer child = new TaskTimer("MasterMgr.removeAnnotationsHelper()");
        timer.suspend();
        Object returned = removeAnnotationsHelper(req, child, name, true);
        timer.accum(child);
        
        if (returned instanceof FailureRsp) {
          FailureRsp rsp = (FailureRsp) returned;
          throw new PipelineException
            ("After the node was successfully deleted, " +
             "a failure occured when trying to remove the annotations from the ex-node: " + 
             rsp.getMessage());
        }
      }
      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      pDatabaseLock.releaseWriteLock();
    }
  }



  /*----------------------------------------------------------------------------------------*/

  /** 
   * Check-In the tree of nodes rooted at the given working version. <P> 
   * 
   * @param req 
   *   The node check-in request.
   *
   * @param opn
   *   The operation progress notifier.
   * 
   * @param sessionID
   *   The unique network connection session ID.
   *
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to the check-in the nodes.
   */ 
  public Object
  checkIn
  ( 
   NodeCheckInReq req, 
   OpNotifiable opn, 
   long sessionID
  ) 
  {
    NodeID nodeID = req.getNodeID();

    TaskTimer timer = new TaskTimer("MasterMgr.checkIn(): " + nodeID);

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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

	timer.acquire();
	LoggedLock lock = getCheckedInLock(name);
	lock.acquireReadLock();
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
	  lock.releaseReadLock();
	}
      }

      /* check-in the tree of nodes */ 
      performNodeOperation(new NodeCheckInOp(req, rootVersionID, opn), nodeID, 
                           timer, sessionID);
      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, 
			    "Check-In operation aborted!\n\n" +
			    ex.getMessage());
    }  
    finally {
      pDatabaseLock.releaseReadLock();
    }  
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Check-Out the tree of nodes rooted at the given working version. <P> 
   * 
   * @param req 
   *   The node check-out request.
   *
   * @param opn
   *   The operation progress notifier.
   * 
   * @param sessionID
   *   The unique network connection session ID.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or
   *   <CODE>GetUnfinishedJobsForNodesRsp</CODE> if running jobs prevent the check-out or 
   *   <CODE>FailureRsp</CODE> if unable to the check-out the nodes.
   */ 
  public Object
  checkOut
  ( 
   NodeCheckOutReq req, 
   OpNotifiable opn, 
   long sessionID
  ) 
  {
    NodeID nodeID = req.getNodeID();

    TaskTimer timer = new TaskTimer("MasterMgr.checkOut(): " + nodeID);

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	

      if(!pAdminPrivileges.isNodeManaged(req, nodeID)) 
	throw new PipelineException
	  ("Only a user with Node Manager privileges may check-out nodes owned by " + 
	   "another user!");

      opn.notify(timer, "Validating Nodes..."); 

      /* get the current status of the nodes */ 
      TreeMap<String,NodeStatus> table = new TreeMap<String,NodeStatus>();
      performUpstreamNodeOp(new NodeOp(), req.getNodeID(), false, true, 
			    new LinkedList<String>(), table, timer, sessionID);

      /* estimate the number of individual node check-outs */ 
      opn.setTotalSteps(table.size());

      /* determine all checked-in versions required by the check-out operation */ 
      TreeMap<String,TreeSet<VersionID>> requiredVersions = 
	new TreeMap<String,TreeSet<VersionID>>();
      {
	collectRequiredVersions
	  (true, nodeID, req.getVersionID(), false, req.getMode(), req.getMethod(), 
	   table, requiredVersions, new LinkedList<String>(), new TreeSet<String>(), 
	   timer, sessionID);
      }

      /* make sure no unfinished jobs associated with either the current upstream nodes, 
	 the upstream nodes which will be checked-out or the checked-out downstream nodes 
         currently exist */ 
      {
 	TreeMap<String,FileSeq> fseqs = new TreeMap<String,FileSeq>();
 	for(String source : requiredVersions.keySet()) {
 	  NodeID snodeID = new NodeID(nodeID, source);
 	  getDownstreamWorkingSeqs(snodeID, fseqs, timer);
 	}

 	if(!fseqs.isEmpty()) {
          QueueMgrControlClient qclient = acquireQueueMgrClient();
          try {
            MappedSet<String,Long> jobIDs = 
              qclient.getUnfinishedJobsForNodes(nodeID.getAuthor(), nodeID.getView(), fseqs);
            if(!jobIDs.isEmpty()) 
              return new QueueGetUnfinishedJobsForNodesRsp(timer, jobIDs);
          }
          finally {
            releaseQueueMgrClient(qclient);
          }
 	}
      }

      /* lock online/offline status of all required nodes */ 
      timer.acquire();
      List<LoggedLock> onOffLocks = 
	onlineOfflineReadLock(requiredVersions.keySet());
      try {
	timer.resume();	
      
	/* get the names of any required versions which are currently offline */ 
	TreeMap<String,TreeSet<VersionID>> offlineVersions = 
	  new TreeMap<String,TreeSet<VersionID>>();
	{
	  for(String name : requiredVersions.keySet()) {
            TreeSet<VersionID> offline = getOfflinedVersions(timer, name);
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
	  
	  Object obj = requestRestore(new MiscRequestRestoreReq(offlineVersions, req));
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
	   table, new LinkedList<String>(), new TreeSet<String>(), 
	   new TreeSet<String>(), timer, opn, sessionID);
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
      pDatabaseLock.releaseReadLock();
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
   *   The revision number of the node to check-out or <CODE>null</CODE> for latest.
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
   * @param sessionID
   *   The unique network connection session ID.
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
   TreeMap<String,NodeStatus> stable,
   TreeMap<String,TreeSet<VersionID>> requiredVersions, 
   LinkedList<String> branch, 
   TreeSet<String> seen, 
   TaskTimer timer,
   long sessionID
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
    NodeDetailsHeavy details = null;
    {
      NodeStatus status = stable.get(name);
      if(status == null) {
	performUpstreamNodeOp(new NodeOp(), nodeID, isLocked, true, 
			      new LinkedList<String>(), stable, timer, sessionID);
	status = stable.get(name);
      }

      details = status.getHeavyDetails();
      if(details == null)
	throw new IllegalStateException(); 
    }

    timer.acquire();
    LoggedLock workingLock = getWorkingLock(nodeID);
    workingLock.acquireReadLock();
    LoggedLock checkedInLock = getCheckedInLock(name);
    checkedInLock.acquireReadLock();
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
	  if((details.getOverallNodeState() == OverallNodeState.Identical) && 
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
				  stable, requiredVersions, branch, seen, timer, sessionID);
	}
      }

      /* add checked-in version to the required versions */ 
      {
	TreeSet<VersionID> rvids = requiredVersions.get(name);
	if(rvids == null) {
	  rvids = new TreeSet<VersionID>();
	  requiredVersions.put(name, rvids);
	}
	rvids.add(vsn.getVersionID()); 
      }
    }
    finally {
      checkedInLock.releaseReadLock();  
      workingLock.releaseReadLock();
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
   * @param opn
   *   The operation progress notifier.
   * 
   * @param sessionID
   *   The unique network connection session ID.
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
   TreeMap<String,NodeStatus> stable,
   LinkedList<String> branch, 
   TreeSet<String> seen, 
   TreeSet<String> dirty, 
   TaskTimer timer, 
   OpNotifiable opn, 
   long sessionID 
  ) 
    throws PipelineException 
  {
    String name = nodeID.getName();

    /* check for cancellation */
    checkForCancellation(sessionID);

    /* check for circularity */ 
    checkBranchForCircularity(name, branch);

    /* skip nodes which have already been processed */ 
    if(seen.contains(name)) 
      return;

    /* push the current node onto the end of the branch */ 
    branch.addLast(name);

    /* lookup or compute the node status */ 
    NodeDetailsHeavy details = null;
    {
      NodeStatus status = stable.get(name);
      if(status == null) {
	performUpstreamNodeOp(new NodeOp(), nodeID, isLocked, true,
			      new LinkedList<String>(), stable, timer, sessionID);
	status = stable.get(name);
      }

      details = status.getHeavyDetails();
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

    timer.acquire();
    LoggedLock workingLock = getWorkingLock(nodeID);
    workingLock.acquireWriteLock();
    LoggedLock checkedInLock = getCheckedInLock(name);
    checkedInLock.acquireReadLock();
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
	  if((details.getOverallNodeState() == OverallNodeState.Identical) && 
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
		  ("The " + details.getOverallNodeState() + " OverallNodeState should " + 
                   "not be possible here!");
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
			  mode, checkOutMethod, stable, branch, seen, dirty, 
                          timer, opn, sessionID);
	  
	  /* if any of the upstream nodes are dirty, 
  	     mark this node as dirty and make sure it isn't frozen */ 
	  if(dirty.contains(link.getName())) {
	    dirty.add(name);
	    isFrozen = false;
	  }
	}
      }

      /* get the current timestamp */ 
      long timestamp = TimeStamps.now(); 

      boolean filesCreated = false;
      {
	FileMgrClient fclient = acquireFileMgrClient();
	try {
	  /* remove the existing working area files before the check-out */ 
	  if(work != null) 
	    fclient.removeAll(nodeID, work.getSequences());	
	  
	  /* remove the to be checked-out working files,
	     if this is a dirty node with an enabled action */ 
	  if(vsn.isIntermediate() || (dirty.contains(name) && vsn.isActionEnabled())) {
	    fclient.removeAll(nodeID, vsn.getSequences());
	  }
	  /* otherwise, check-out the files */
	  else {
	    fclient.checkOut(nodeID, vsn, isFrozen || vsn.isActionEnabled());
            filesCreated = true;
	  }
	}
	finally {
	  releaseFileMgrClient(fclient);
	}
      }

      /* create a new working version and write it to disk */ 
      NodeMod nwork = 
        new NodeMod(vsn, timestamp, isFrozen && !vsn.isIntermediate(), isLocked, null, null);
      writeWorkingVersion(nodeID, nwork);

      /* initialize new working version */ 
      if(working == null) {
	/* register the node name and sequences */ 
	addWorkingNodeTreePath(nodeID, nwork.getPrimarySequence(), nwork.getSequences());

	/* create a new working bundle */ 
        timer.acquire();
	synchronized(pWorkingBundles) {
          timer.resume();
	  TreeMap<NodeID,WorkingBundle> table = pWorkingBundles.get(name);
	  if(table == null) {
	    table = new TreeMap<NodeID,WorkingBundle>();
	    pWorkingBundles.put(name, table);
	  }
	  table.put(nodeID, new WorkingBundle(nwork));
	}

	/* keep track of the change to the node version cache */ 
	incrementWorkingCounter(nodeID);
      }
	 
      /* update existing working version */ 
      else {
	/* unregister the old working node name and sequences,
	   register the new node name and sequences */ 
	pNodeTree.updateWorkingNodeTreePath
          (nodeID, work.getSequences(), nwork.getPrimarySequence(), nwork.getSequences());
	
	/* update the working bundle */ 
	working.setVersion(nwork);

	/* remove the downstream links from any obsolete upstream nodes */ 
	for(LinkMod link : work.getSources()) {
	  if(isLocked || !nwork.getSourceNames().contains(link.getName())) {
	    String source = link.getName();
	    
	    timer.acquire();
	    LoggedLock downstreamLock = getDownstreamLock(source);
	    downstreamLock.acquireWriteLock();  
	    try {
	      timer.resume();
	      
	      DownstreamLinks links = getDownstreamLinks(source); 
	      links.removeWorking(new NodeID(nodeID, source), name);
	    }
	    finally {
	      downstreamLock.releaseWriteLock();
	    }
	  }
	}  
      }

      /* set the working downstream links from the upstream nodes to this node */ 
      if(!isLocked) {
	for(LinkMod link : nwork.getSources()) {
	  String lname = link.getName();
	  
	  timer.acquire();
	  LoggedLock downstreamLock = getDownstreamLock(lname);
	  downstreamLock.acquireWriteLock();
	  try {
	    timer.resume();
	    
	    DownstreamLinks dsl = getDownstreamLinks(lname);
	    dsl.addWorking(new NodeID(nodeID, lname), name);
	  }  
	  finally {
	    downstreamLock.releaseWriteLock();
	  }     
	}
      }

      /* initialize the checksum cache for the new working version */ 
      {
        /* copy the checksums from the checked-in version without updated-on timestamps */ 
        CheckSumCache wcache = new CheckSumCache(nodeID, vsn); 
        
        /* lookup timestamps of the working files to set the checksum updated-on times */ 
        FileMgrClient fclient = acquireFileMgrClient();
        try {
          ArrayList<String> fnames = new ArrayList<String>();
          for(FileSeq fseq : vsn.getSequences()) {
            for(Path path : fseq.getPaths())
              fnames.add(path.toString());
          }
              
          ArrayList<Long> stamps = fclient.getWorkingTimeStamps(nodeID, fnames); 
          int wk;
          for(wk=0; wk<stamps.size(); wk++) {
            String fname = fnames.get(wk);
            Long stamp = stamps.get(wk);
            if(stamp != null) 
              wcache.replaceUpdatedOn(fname, stamp);
            else 
              wcache.remove(fname);
          }
        }
        finally {
          releaseFileMgrClient(fclient);
        }

        /* update the save the checksum bundle to disk */ 
        timer.acquire();
        LoggedLock clock = getCheckSumLock(nodeID);
        clock.acquireWriteLock();
        try { 
          timer.resume();

          CheckSumBundle cbundle = getCheckSumBundle(nodeID); 
          if(filesCreated) 
            cbundle.setCache(wcache); 
          writeCheckSumCache(cbundle.getCache()); 
        }
        finally {
          clock.releaseWriteLock();
        }  
      }

      /* record event */ 
      pPendingEvents.add(new CheckedOutNodeEvent(nodeID, vsn.getVersionID(), 
						 isFrozen, isLocked));

      /* post-op tasks */  
      if(hasExtTasks) 
	startExtensionTasks(timer, new CheckOutExtFactory(nodeID, new NodeMod(nwork)));
    }
    finally {
      checkedInLock.releaseReadLock();  
      workingLock.releaseWriteLock();

      opn.step(timer, "Checked-Out: " + name); 
    }

    /* pop the current node off of the end of the branch */ 
    branch.removeLast();
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Check-Out a single node into a working area, ignoring all upstream nodes. <P> 
   * 
   * @param req 
   *   The node check-out request.
   *
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or
   *   <CODE>NodeGetUnfinishedJobIDsRsp</CODE> if running jobs prevent the check-out or 
   *   <CODE>FailureRsp</CODE> if unable to the check-out the node.
   */ 
  public Object
  checkOutSolo
  ( 
   NodeCheckOutSoloReq req 
  ) 
  {
    NodeID nodeID = req.getNodeID();
    String name = nodeID.getName(); 

    TaskTimer timer = new TaskTimer("MasterMgr.checkOutSolo(): " + nodeID);

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	      

      if(!pAdminPrivileges.isNodeManaged(req, nodeID)) 
	throw new PipelineException
	  ("Only a user with Node Manager privileges may check-out nodes owned by " + 
	   "another user!");

      /* determine the version to check-out */ 
      VersionID vid = req.getVersionID();
      if(vid == null) {
        timer.acquire();  
        LoggedLock checkedInLock = getCheckedInLock(name);
        checkedInLock.acquireReadLock();
        try {
          timer.resume();	

          TreeMap<VersionID,CheckedInBundle> checkedIn = null;
          try {
            checkedIn = getCheckedInBundles(name);
            vid = checkedIn.lastKey();
          }
          catch(PipelineException ex) {
            throw new PipelineException
              ("There are no checked-in versions of node (" + name + ") to check-out!");
          }
          if(checkedIn == null)
            throw new IllegalStateException(); 
        }
        finally {
          checkedInLock.releaseReadLock();  
        }
      }

      /* make sure no unfinished jobs associated with the current node or the checked-out 
         downstream nodes currently exist */
      {
        TreeMap<String,FileSeq> fseqs = new TreeMap<String,FileSeq>();
        getDownstreamWorkingSeqs(nodeID, fseqs, timer);

 	if(!fseqs.isEmpty()) {
          QueueMgrControlClient qclient = acquireQueueMgrClient();
          try {
            MappedSet<String,Long> jobIDs = 
              qclient.getUnfinishedJobsForNodes(nodeID.getAuthor(), nodeID.getView(), fseqs);
            if(!jobIDs.isEmpty()) 
              return new QueueGetUnfinishedJobsForNodesRsp(timer, jobIDs);
          }
          finally {
            releaseQueueMgrClient(qclient);
          }
 	}
      }

      /* lock online/offline status of the node */ 
      timer.acquire();
      LoggedLock onOffLock = getOnlineOfflineLock(name);
      onOffLock.acquireReadLock();
      try {
	timer.resume();	
      
        /* make sure version being checked-out is online */ 
        if(isOffline(timer, name, vid)) {
          StringBuilder buf = new StringBuilder();
          
          buf.append
            ("Unable to perform check-out because the checked-in version (" + vid + ") " + 
             "is currently offline.\n\n");
          
          MappedSet<String,VersionID> offlineVersions = new MappedSet<String,VersionID>();
          offlineVersions.put(name, vid);
          Object obj = requestRestore(new MiscRequestRestoreReq(offlineVersions, req));
          if(obj instanceof FailureRsp) {
            FailureRsp rsp = (FailureRsp) obj;
            buf.append("The request to restore the offline version also failed.\n"); 
          }
          else {
            buf.append
              ("However, a request has been submitted to restore the offline version " + 
               "so that it may be used once they have been brought back online.");
          }
	  
          throw new PipelineException(buf.toString());
        }

        /* perform the check-out */ 
        {
          timer.acquire();
          LoggedLock workingLock = getWorkingLock(nodeID);
          workingLock.acquireWriteLock();
          LoggedLock checkedInLock = getCheckedInLock(name);
          checkedInLock.acquireReadLock();
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
                  ("There are no checked-in versions of node (" + name + ") to check-out!");
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
                    ("Somehow no checked-in version (" + vid + ") of node (" + name + ") " + 
                     "exists!"); 
                vsn = new NodeVersion(bundle.getVersion());
              }
              else {
                if(checkedIn.isEmpty())
                  throw new PipelineException
                    ("Somehow no checked-in versions of node (" + name + ") exist!"); 
                CheckedInBundle bundle = checkedIn.get(checkedIn.lastKey());
                vsn = new NodeVersion(bundle.getVersion());
              }
            }

            /* pre-op test */ 
            CheckOutExtFactory factory = new CheckOutExtFactory();
            if(hasAnyExtensionTests(timer, factory)) 
              performExtensionTests
                (timer, new CheckOutExtFactory(nodeID, new NodeVersion(vsn), null, null));
            
            /* get the current timestamp */ 
            long timestamp = TimeStamps.now(); 

            /* check-out the node's files */ 
            boolean filesCreated = false;
            {
              FileMgrClient fclient = acquireFileMgrClient();
              try {
                /* remove the existing working area files before the check-out */ 
                if(work != null) 
                  fclient.removeAll(nodeID, work.getSequences());	
                
                /* only check-out files if there isn't an action */ 
                if(!vsn.isActionEnabled()) {
                  fclient.checkOut(nodeID, vsn, false);
                  filesCreated = true;
                }
              }
              finally {
                releaseFileMgrClient(fclient);
              }
            }
            
            /* create a new working version and write it to disk */ 
            NodeMod nwork = new NodeMod(vsn, timestamp, false, false, null, null);
            nwork.removeAllSources();
            writeWorkingVersion(nodeID, nwork);

            /* initialize new working version */ 
            if(working == null) {
              /* register the node name and sequences */ 
              addWorkingNodeTreePath
                (nodeID, nwork.getPrimarySequence(), nwork.getSequences());

              /* create a new working bundle */ 
              timer.acquire();
              synchronized(pWorkingBundles) {
                timer.resume();
                TreeMap<NodeID,WorkingBundle> table = pWorkingBundles.get(name);
                if(table == null) {
                  table = new TreeMap<NodeID,WorkingBundle>();
                  pWorkingBundles.put(name, table);
                }
                table.put(nodeID, new WorkingBundle(nwork));
              }
              
              /* keep track of the change to the node version cache */ 
              incrementWorkingCounter(nodeID);
            }
	 
            /* update existing working version */ 
            else {
              /* unregister the old working node name and sequences,
                 register the new node name and sequences */ 
              pNodeTree.updateWorkingNodeTreePath
                (nodeID, work.getSequences(), nwork.getPrimarySequence(), 
                 nwork.getSequences());
	
              /* update the working bundle */ 
              working.setVersion(nwork);

              /* remove the downstream links from any existing upstream nodes */ 
              for(LinkMod link : work.getSources()) {
                String source = link.getName();
                
                timer.acquire();
                LoggedLock downstreamLock = getDownstreamLock(source);
                downstreamLock.acquireWriteLock();  
                try {
                  timer.resume();
                  
                  DownstreamLinks links = getDownstreamLinks(source); 
                  links.removeWorking(new NodeID(nodeID, source), name);
                }
                finally {
                  downstreamLock.releaseWriteLock();
                }
              }  
            }

            /* initialize the checksum cache for the new working version */ 
            {
              /* copy the checksums from the checked-in version without updated-on 
                 timestamps */ 
              CheckSumCache wcache = new CheckSumCache(nodeID, vsn); 
        
              /* lookup timestamps of the working files to set the checksum updated-on 
                 times */ 
              FileMgrClient fclient = acquireFileMgrClient();
              try {
                ArrayList<String> fnames = new ArrayList<String>();
                for(FileSeq fseq : vsn.getSequences()) {
                  for(Path path : fseq.getPaths())
                    fnames.add(path.toString());
                }
                
                ArrayList<Long> stamps = fclient.getWorkingTimeStamps(nodeID, fnames); 
                int wk;
                for(wk=0; wk<stamps.size(); wk++) {
                  String fname = fnames.get(wk);
                  Long stamp = stamps.get(wk);
                  if(stamp != null) 
                    wcache.replaceUpdatedOn(fname, stamp);
                  else 
                    wcache.remove(fname);
                }
              }
              finally {
                releaseFileMgrClient(fclient);
              }
              
              /* update the save the checksum bundle to disk */ 
              timer.acquire();
              LoggedLock clock = getCheckSumLock(nodeID);
              clock.acquireWriteLock();
              try { 
                timer.resume();
                
                CheckSumBundle cbundle = getCheckSumBundle(nodeID); 
                if(filesCreated) 
                  cbundle.setCache(wcache); 
                writeCheckSumCache(cbundle.getCache()); 
              }
              finally {
                clock.releaseWriteLock();
              }  
            }
            
            /* record event */ 
            pPendingEvents.add
              (new CheckedOutNodeEvent(nodeID, vsn.getVersionID(), false, false)); 
            
            /* post-op tasks */  
            if(hasAnyExtensionTasks(timer, factory)) 
              startExtensionTasks(timer, new CheckOutExtFactory(nodeID, new NodeMod(nwork)));
          }
          finally {
            checkedInLock.releaseReadLock();  
            workingLock.releaseWriteLock();
          }
        }
      }
      finally {
        onOffLock.releaseReadLock();
      }

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, 
			    "Check-Out operation aborted!\n\n" +
			    ex.getMessage());
    }   
    finally {
      pDatabaseLock.releaseReadLock();
    } 
  }



  /*----------------------------------------------------------------------------------------*/

  /** 
   * Lock the working version of a node to a specific checked-in version. <P> 
   * 
   * @param req 
   *   The node lock request.
   * 
   * @param sessionID
   *   The unique network connection session ID.
   *
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>GetUnfinishedJobsForNodesRsp</CODE> if running jobs prevent the lock or 
   *   <CODE>FailureRsp</CODE> if unable to the lock the node.
   */ 
  public Object
  lock
  ( 
   NodeLockReq req, 
   long sessionID
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	

      if(!pAdminPrivileges.isNodeManaged(req, nodeID)) 
	throw new PipelineException
	  ("Only a user with Node Manager privileges may lock nodes in working " + 
	   "areas owned by another user!");

      /* get the current status of the node being locked */ 
      NodeStatus status = performNodeOperation(new NodeOp(), nodeID, timer, sessionID);

      /* make sure that no downstream nodes have unfinished jobs associated with them */
      {
        TreeMap<String,FileSeq> fseqs = new TreeMap<String,FileSeq>();
        getDownstreamWorkingSeqs(nodeID, fseqs, timer);
        
 	if(!fseqs.isEmpty()) {
          QueueMgrControlClient qclient = acquireQueueMgrClient();
          try {
            MappedSet<String,Long> jobIDs = 
              qclient.getUnfinishedJobsForNodes(nodeID.getAuthor(), nodeID.getView(), fseqs);
            if(!jobIDs.isEmpty()) 
              return new QueueGetUnfinishedJobsForNodesRsp(timer, jobIDs);
          }
          finally {
            releaseQueueMgrClient(qclient);
          }
 	}
      }

      /* lock online/offline status of the node to lock */ 
      timer.acquire();
      LoggedLock onOffLock = getOnlineOfflineLock(name);
      onOffLock.acquireReadLock();
      try {
	timer.resume();

	/* abort if the target version is offline */ 
	if(isOffline(timer, name, vid)) {
	  StringBuilder buf = new StringBuilder();
	  buf.append
	    ("Unable to lock node (" + name + ") to checked-in version (" + vid + ") " + 
	     "because that version is currently offline!\n\n");

	  TreeSet<VersionID> ovids = new TreeSet<VersionID>();
	  ovids.add(vid);
	  
	  TreeMap<String,TreeSet<VersionID>> ovsns = new TreeMap<String,TreeSet<VersionID>>();
	  ovsns.put(name, ovids);

	  Object obj = requestRestore(new MiscRequestRestoreReq(ovsns, req));
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
	timer.acquire();
	LoggedLock workingLock = getWorkingLock(nodeID);
	workingLock.acquireWriteLock();
	LoggedLock checkedInLock = getCheckedInLock(name);
	checkedInLock.acquireReadLock();
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
	  }

          /* make sure the checked-in version does not have intermediate files */ 
          if(vsn.isIntermediate()) 
            throw new PipelineException
              ("Unable to lock node (" + name + ") because the checked-in version " + 
               "of the node is marked as having Intermediate Files which are not " + 
               "stored in the repository and therefore not available for locking!"); 

	  /* make sure the checked-in version has no Association/Reference links */ 
	  for(LinkVersion link : vsn.getSources()) {
            switch(link.getPolicy()) {
            case Association:
            case Reference:
	      throw new PipelineException
		("Unable to lock node (" + name + ") because the checked-in version " + 
		 "of the node had a " + link.getPolicy() + " link to node " + 
                 "(" + link.getName() + ")!");
            }
	  } 

	  /* get the timestamp to give the newly unlocked version */ 
          long timestamp = 0L; 
          {
            /* if locking it shouldn't make the downstream nodes Stale, 
                 then steal the newest per-file timestamp of the unlocked version */ 
            Long oldStamp = null; 
            NodeDetailsHeavy details = status.getHeavyDetails(); 
            switch(details.getOverallNodeState()) {
            case Identical:
            case NeedsCheckOut:
              {
                NodeMod omod = details.getWorkingVersion(); 
                if(!omod.isLocked() && vid.equals(omod.getWorkingID())) {
                  long fts[] = details.getUpdateTimeStamps();

                  int wk;
                  for(wk=0; wk<fts.length; wk++) {
                    if((oldStamp == null) || (oldStamp < fts[wk]))
                      oldStamp = fts[wk];
                  }
                }
              }
            }

            if(oldStamp != null) 
              timestamp = oldStamp;
            else 
              timestamp = TimeStamps.now(); 
          }

	  {
	    FileMgrClient fclient = acquireFileMgrClient();
	    try {
	      /* remove the existing working area files before the check-out */ 
	      if(work != null) 
		fclient.removeAll(nodeID, work.getSequences());	

	      /* check-out the links to the checked-in files */
	      fclient.checkOut(nodeID, vsn, true);   
	    }
	    finally {
	      releaseFileMgrClient(fclient);
	    }
	  }
	  
	  /* create a new working version and write it to disk */ 
	  NodeMod nwork = new NodeMod(vsn, timestamp, true, true, null, null);
	  writeWorkingVersion(nodeID, nwork);
	
	  /* initialize new working version */ 
	  if(working == null) {
	    /* register the node name */ 
	    addWorkingNodeTreePath(nodeID, nwork.getPrimarySequence(), nwork.getSequences());
	  
	    /* create a new working bundle */ 
            timer.acquire();
	    synchronized(pWorkingBundles) {
              timer.resume();
	      TreeMap<NodeID,WorkingBundle> table = pWorkingBundles.get(name);
	      if(table == null) {
		table = new TreeMap<NodeID,WorkingBundle>();
		pWorkingBundles.put(name, table);
	      }
	      table.put(nodeID, new WorkingBundle(nwork));
	    }
	  
	    /* keep track of the change to the node version cache */ 
	    incrementWorkingCounter(nodeID);
	  }
	
	  /* update existing working version */ 
	  else {
	    /* update the working bundle */ 
	    working.setVersion(nwork);

	    /* remove the downstream links from all upstream nodes */ 
	    for(LinkMod link : work.getSources()) {
	      String source = link.getName();
	      
	      timer.acquire();
	      LoggedLock downstreamLock = getDownstreamLock(source);
	      downstreamLock.acquireWriteLock();  
	      try {
		timer.resume();
	      
		DownstreamLinks links = getDownstreamLinks(source); 
		links.removeWorking(new NodeID(nodeID, source), name);
	      }
	      finally {
		downstreamLock.releaseWriteLock();
	      }
	    }
	  }
	  
          /* create or update the checksum cache for the new working version */ 
          {
            /* copy checksums from the checked-in version without updated-on timestamps */ 
            CheckSumCache wcache = new CheckSumCache(nodeID, vsn); 
        
            /* lookup timestamps of the working links to set the checksum updated-on times */ 
            FileMgrClient fclient = acquireFileMgrClient();
            try {
              ArrayList<String> fnames = new ArrayList<String>();
              for(FileSeq fseq : vsn.getSequences()) {
                for(Path path : fseq.getPaths())
                  fnames.add(path.toString());
              }
              
              ArrayList<Long> stamps = fclient.getWorkingTimeStamps(nodeID, fnames); 
              int wk;
              for(wk=0; wk<stamps.size(); wk++) {
                String fname = fnames.get(wk);
                Long stamp = stamps.get(wk);
                if(stamp != null) 
                  wcache.replaceUpdatedOn(fname, stamp);
                else 
                  wcache.remove(fname);
              }
            }
            finally {
              releaseFileMgrClient(fclient);
            }

            /* update the save the checksum bundle to disk */ 
            timer.acquire();
            LoggedLock clock = getCheckSumLock(nodeID);
            clock.acquireWriteLock();
            try {
              timer.resume();
              
              CheckSumBundle cbundle = getCheckSumBundle(nodeID); 
              cbundle.setCache(wcache); 
              writeCheckSumCache(cbundle.getCache()); 
            }
            finally {
              clock.releaseWriteLock();
            }  
          }

	  /* record event */ 
	  pPendingEvents.add(new CheckedOutNodeEvent(nodeID, vid, true, true)); 

	  /* post-op tasks */ 
	  startExtensionTasks(timer, factory);

	  return new SuccessRsp(timer);
	}
	catch(PipelineException ex) {
	  return new FailureRsp(timer, ex.getMessage());
	}
	finally {
	  checkedInLock.releaseReadLock();  
	  workingLock.releaseWriteLock();
	}
      }
      finally {
	onOffLock.releaseReadLock();
      }
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, "Lock operation aborted!\n\n" + ex.getMessage());
    }  
    finally {
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	      
      
      if(!pAdminPrivileges.isNodeManaged(req, nodeID)) 
	throw new PipelineException
	  ("Only a user with Node Manager privileges may revert files associated with " + 
	   "nodes in working areas owned by another user!");

      /* whether the files associated with the working version should be symlinks to the 
         checked-in files instead of copies */ 
      boolean isLinked = false;
      {
	timer.acquire(); 
	LoggedLock lock = getWorkingLock(nodeID);
	lock.acquireReadLock();
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

	  isLinked = mod.isActionEnabled();
	}
	finally {
	  lock.releaseReadLock();
	}
      }

      /* lock online/offline status of the node */ 
      timer.acquire();
      LoggedLock onOffLock = getOnlineOfflineLock(name);
      onOffLock.acquireReadLock();
      try {
	timer.resume();	

	/* check whether the checked-in versions are currently online */ 
	{
	  TreeSet<VersionID> ovids = new TreeSet<VersionID>();
	  
          TreeSet<VersionID> offline = getOfflinedVersions(timer, name);
          if(offline != null) {
            TreeSet<VersionID> vids = new TreeSet<VersionID>(files.values());
            for(VersionID vid : vids) {
              if(offline.contains(vid))
                ovids.add(vid);
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
	    
	    Object obj = requestRestore(new MiscRequestRestoreReq(vsns, req));
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
	  FileMgrClient fclient = acquireFileMgrClient();
	  try {
	    fclient.revert(nodeID, files, isLinked);
	  }
	  finally {
	    releaseFileMgrClient(fclient);
	  }
	}
      }
      finally {
	onOffLock.releaseReadLock();
      }

      /* copy checksums from the repository versions */ 
      {
        /* get the selected checksums from the checked in node versions */ 
        TreeMap<String,CheckSum> checksums = new TreeMap<String,CheckSum>();
        {
          MappedSet<VersionID,String> vfiles = new MappedSet<VersionID,String>(files); 
          for(VersionID vid : vfiles.keySet()) {

            timer.acquire();
            LoggedLock checkedInLock = getCheckedInLock(name);
            checkedInLock.acquireReadLock();
            try {
              timer.resume();

              TreeMap<VersionID,CheckedInBundle> checkedIn = null;
              try {
                checkedIn = getCheckedInBundles(name);
              }
              catch(PipelineException ex) {
                throw new PipelineException
                  ("There are no checked-in versions of node (" + name + ") to revert!");
              }
            
              for(String fname : vfiles.get(vid)) {
                CheckedInBundle bundle = checkedIn.get(vid);
                if(bundle != null) 
                  checksums.put(fname, bundle.getVersion().getCheckSum(fname));
              }
            }
            finally {
              checkedInLock.releaseReadLock();  
            }
          }
        }
        
        /* lookup timestamps of the working files */ 
        ArrayList<String> fnames = new ArrayList<String>(checksums.keySet());
        ArrayList<Long> stamps = null;
        FileMgrClient fclient = acquireFileMgrClient();
        try {
          stamps = fclient.getWorkingTimeStamps(nodeID, fnames); 
        }
        finally {
          releaseFileMgrClient(fclient);
        }

        /* update the save the checksum bundle to disk, 
             while setting the updated-in timestamps to match the reverted files */ 
        timer.acquire();
        LoggedLock clock = getCheckSumLock(nodeID);
        clock.acquireWriteLock();
        try {
          timer.resume();
          
          CheckSumBundle cbundle = getCheckSumBundle(nodeID); 
          CheckSumCache cache = cbundle.getCache(); 

          int wk;
          for(wk=0; wk<stamps.size(); wk++) {
            String fname = fnames.get(wk);
            Long stamp = stamps.get(wk);
            CheckSum sum = checksums.get(fname); 
            if((sum != null) && (stamp != null)) 
              cache.add(fname, new TransientCheckSum(sum, stamp+1L)); 
          }

          writeCheckSumCache(cache); 
        }
        finally {
          clock.releaseWriteLock();
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
      pDatabaseLock.releaseReadLock();
    }  
  }

  /*----------------------------------------------------------------------------------------*/

  /** 
   * Replace the primary and selected secondary files associated one node with the primary 
   * and selected secondary files of another node. <P>
   * 
   * The two nodes must have exactly the same number of files in their primary file sequences
   * or the operation will fail. <P> 
   * 
   * @param req 
   *   The clone files request.
   *
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to the clone the files or if a specified secondary 
   *   sequence does not exist.
   */ 
  public Object
  cloneFiles
  ( 
    NodeCloneFilesReq req 
  ) 
  {
    NodeID sourceID = req.getSourceID();
    NodeID targetID = req.getTargetID();
    MappedSet<FileSeq, FileSeq> secondaries = req.getSecondarySequences();

    TaskTimer timer = 
      new TaskTimer("MasterMgr.cloneFiles(): " + sourceID + " to " + targetID);

    TreeSet<FileSeq> bad = new TreeSet<FileSeq>();
    for (Entry<FileSeq, TreeSet<FileSeq>> entry : secondaries.entrySet() ) {
      if (entry.getValue() == null)
        bad.add(entry.getKey());
    }

    for (FileSeq each : bad)
      secondaries.remove(each);
    
    /* pre-op tests */
    CloneFilesExtFactory factory = new CloneFilesExtFactory(sourceID, targetID);
    try {
      performExtensionTests(timer, factory);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }

    /* make sure the file manager can clone the files */ 
    {
      FileMgrClient fclient = acquireFileMgrClient();
      try {
        fclient.validateScratchDir();
      }
      catch(PipelineException ex) {
        return new FailureRsp(timer, ex.getMessage());
      }
      finally {
        releaseFileMgrClient(fclient);
      }
    }

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	      
      
      if(!pAdminPrivileges.isNodeManaged(req, targetID)) 
	throw new PipelineException
	  ("Only a user with Node Manager privileges may clone files associated with " + 
	   "nodes in working areas owned by another user!");

      FileSeq sourceSeq = null;
      {
	timer.acquire(); 
	LoggedLock lock = getWorkingLock(sourceID);
	lock.acquireReadLock();
	try {
	  timer.resume();

	  WorkingBundle bundle = getWorkingBundle(sourceID);
	  if(bundle == null) 
	    throw new PipelineException
	      ("Only nodes with working versions can have their files cloned!");

	  NodeMod mod = bundle.getVersion();
	  sourceSeq = mod.getPrimarySequence();
	  if (secondaries != null) {
	    SortedSet<FileSeq> existingSec = mod.getSecondarySequences();
	    for (FileSeq secSeq : secondaries.keySet()) {
	      if (!existingSec.contains(secSeq))
	        throw new PipelineException
	          ("The secondary sequence (" + secSeq + ") does not appear to be a valid " +
	           "secondary sequence on the source node (" + sourceSeq + ")!");
	      
	    }
	  }
	}
	finally {
	  lock.releaseReadLock();
	}
      }
      
      FileSeq targetSeq = null;
      boolean writeable = true;
      {
	timer.acquire(); 
	LoggedLock lock = getWorkingLock(targetID);
	lock.acquireReadLock();
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
	  if (secondaries != null) {
	    SortedSet<FileSeq> existingSec = mod.getSecondarySequences();
	    for (TreeSet<FileSeq> secSeqs : secondaries.values()) {
	      for (FileSeq secSeq : secSeqs) {
	        if (!existingSec.contains(secSeq))
	          throw new PipelineException
  	            ("The secondary sequence (" + secSeq + ") does not appear to be a " +
  	             "valid secondary sequence on the target node (" + targetSeq + ")!");
	      }
	    }
	  }

	  writeable = !mod.isActionEnabled();
	}
	finally {
	  lock.releaseReadLock();
	}
      }
      
      {
        FrameRange fr = req.getSourceRange();
        if (fr != null)
          sourceSeq = new FileSeq(sourceSeq.getFilePattern(), fr);
      }
      
      {
        FrameRange fr = req.getTargetRange();
        if (fr != null)
          targetSeq = new FileSeq(targetSeq.getFilePattern(), fr);
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
      if (sourceSeq.numFrames() != targetSeq.numFrames())
        throw new PipelineException
          ("Unable to clone the files associated with node (" + sourceID + "), because " +
           "the file sequence associated with the node (" + sourceSeq + ") has a " +
           "different number of frames than the target file sequence (" + targetSeq + ")!");

      int numFrames = sourceSeq.numFrames();
      for (int i = 0; i < numFrames; i++) {
        files.put(sourceSeq.getFile(i), targetSeq.getFile(i));
        if (secondaries != null) {
          for (FileSeq sourceSec : secondaries.keySet()) {
            for (FileSeq targetSec : secondaries.get(sourceSec))
              files.put(sourceSec.getFile(i), targetSec.getFile(i));
          }
        }
      }

      /* clone the files */ 
      {
	FileMgrClient fclient = acquireFileMgrClient();
	try {
	  fclient.clone(sourceID, targetID, files, writeable);
	}
	finally {
	  releaseFileMgrClient(fclient);
	}
      }
	
      /* copy the checksums */ 
      {
        /* get a deep copy of the source node's cache to avoid lock contention issues */ 
        CheckSumCache ocache = null;
        {
          timer.acquire();
          LoggedLock clock = getCheckSumLock(sourceID);
          clock.acquireWriteLock();
          try {
            timer.resume();

            CheckSumBundle cbundle = getCheckSumBundle(sourceID);   
            ocache = new CheckSumCache(cbundle.getCache()); 
          }
          finally {
            clock.releaseWriteLock();
          }  
        }
        
        /* first copy checksums from original to cloned names, 
             then lookup the timestamps of the cloned files and set the updated-on
             stamps for the checksums accordingly */ 
        CheckSumCache ncache = null;
        {
          ArrayList<String> ofnames = new ArrayList<String>();
          ArrayList<String> nfnames = new ArrayList<String>();
          for(Map.Entry<File,File> entry : files.entrySet()) {
            ofnames.add(entry.getKey().getPath()); 
            nfnames.add(entry.getValue().getPath()); 
          }
          ncache = new CheckSumCache(targetID, nfnames, ofnames, ocache);

          FileMgrClient fclient = acquireFileMgrClient();
          try {
            ArrayList<Long> stamps = fclient.getWorkingTimeStamps(targetID, nfnames); 
            int wk;
            for(wk=0; wk<stamps.size(); wk++) {
              String fname = nfnames.get(wk);
              Long stamp = stamps.get(wk);
              if(stamp != null) 
                ncache.replaceUpdatedOn(fname, stamp);
              else 
                ncache.remove(fname);
            }
          }
          finally {
            releaseFileMgrClient(fclient);
          }
        }

        /* use them to initialize the target node's cache */ 
        {
          timer.acquire();
          LoggedLock clock = getCheckSumLock(targetID);
          clock.acquireWriteLock();
          try {
            timer.resume();

            CheckSumBundle cbundle = getCheckSumBundle(targetID);   
            cbundle.setCache(ncache);  
            writeCheckSumCache(cbundle.getCache()); 
          }
          finally {
            clock.releaseWriteLock();
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
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	

      if(!pAdminPrivileges.isNodeManaged(req, nodeID)) 
	throw new PipelineException
	  ("Only a user with Node Manager privileges may evolve nodes in working " + 
	   "areas owned by another user!");

      /* verify the checked-in revision number */ 
      {
	timer.acquire();
	LoggedLock lock = getCheckedInLock(name);
	lock.acquireReadLock();
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
	  lock.releaseReadLock();
	}  
      }

      /* lock online/offline status of the node */ 
      timer.acquire();
      LoggedLock onOffLock = getOnlineOfflineLock(name);
      onOffLock.acquireReadLock();
      try {
	timer.resume();	

	/* check whether the checked-in version is currently online */ 
        if(isOffline(timer, name, vid)) {
          TreeSet<VersionID> vids = new TreeSet<VersionID>();
          vids.add(vid);
	  
          TreeMap<String,TreeSet<VersionID>> vsns = 
              new TreeMap<String,TreeSet<VersionID>>();
          vsns.put(name, vids);
	  
          Object obj = requestRestore(new MiscRequestRestoreReq(vsns, req));
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
	
	/* change the checked-in version number for the working version */ 
	timer.acquire();
	LoggedLock lock = getWorkingLock(nodeID);
	lock.acquireWriteLock();
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
	  lock.releaseWriteLock();
	}  
      }
      finally {
	onOffLock.releaseReadLock();
      }  

      /* record event */ 
      pPendingEvents.add(new EvolvedNodeEvent(nodeID, vid)); 

      /* post-op tasks */ 
      startExtensionTasks(timer, factory);
      
      return new SuccessRsp(timer);    
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*  C H E C K S U M S                                                                     */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Sent updates to the checksums for files associated with the given set of working 
   * versions. <P> 
   * 
   * This is used by the Queue Manager to transmit checksums stored in QueueJobInfo
   * instances which are about to be garbage collected.
   *
   * @param req
   *   The request.
   * 
   * @return 
   *   <CODE>SuccessRsp</CODE> if successful or <CODE>FailureRsp</CODE> on failure.
   */ 
  public Object
  updateCheckSums
  (
   NodeUpdateCheckSumsReq req
  ) 
  {
    TaskTimer timer = new TaskTimer("MasterMgr.updateCheckSums()");

    TreeMap<NodeID,CheckSumCache> checksums = req.getCheckSums();
    if(checksums.isEmpty()) 
      return new SuccessRsp(timer);    

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	

      for(Map.Entry<NodeID,CheckSumCache> entry : checksums.entrySet()) {
        NodeID nodeID = entry.getKey();
        CheckSumCache qcache = entry.getValue();
        if(!qcache.isEmpty()) {
          timer.acquire();
          LoggedLock clock = getCheckSumLock(nodeID);
          clock.acquireWriteLock();
          try {
            timer.resume();
            
            CheckSumBundle cbundle = getCheckSumBundle(nodeID);   
            CheckSumCache cache = cbundle.getCache();
            
            cache.resetModified(); 
            cache.addAll(qcache);
            if(cache.wasModified()) {
              cbundle.setCache(cache); 
              writeCheckSumCache(cache); 
            }
          }
          finally {
            clock.releaseWriteLock();
          } 
        }
      }

      return new SuccessRsp(timer);    
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   N O D E   B U N D L E S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new node bundle (JAR achive) by packing up a tree of nodes from a working 
   * area rooted at the given node.<P> 
   *
   * If successful, this will create a new node bundle containing the node properties, 
   * links and associated working area data files for the entire tree of nodes rooted at 
   * the given node.  The node bundle will contain full copies of all files associated 
   * with these nodes regardless of whether they where checked-out modifiable, frozen or 
   * locked within the current working area.  All node metadata, including detailed 
   * information about the toolsets and toolset packages required, will be written to a 
   * GLUE file included in the node bundle. <P> 
   * 
   * The node bundle will always be written into the root directory of the working area 
   * containing the root node of the node tree being packed into the archive.  The name of
   * the archive is automatically generated based on the name of the root node and the 
   * time when the operation begins.  The full path to the create JAR file is returned by
   * this method if successfull. <P> 
   * 
   * Create a new node JAR archive by packing up a tree of nodes from a working area rooted 
   * at the given node.<P> 
   *
   * If the owner of the root node is different than the current user, this method 
   * will fail unless the current user has privileged access status. All nodes to be packed
   * into the JAR archive must be in a Finished (blue) state.  Any nodes not in a Finished
   * state will cause the entire operation to abort.<P> 
   * 
   * @param req 
   *   The pack request.
   *
   * @param opn
   *   The operation progress notifier.
   * 
   * @param sessionID
   *   The unique network connection session ID.
   *
   * @return
   *   <CODE>NodePackRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to create the node JAR archive.
   */ 
  public Object
  packNodes
  ( 
   NodePackReq req, 
   OpNotifiable opn, 
   long sessionID
  ) 
  {
    NodeID nodeID = req.getNodeID();
    String name = nodeID.getName();

    TaskTimer timer = new TaskTimer(); 

    /* pre-op tests */
    try {
      PackExtFactory factory = new PackExtFactory(nodeID); 
      performExtensionTests(timer, factory);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	

      if(!pAdminPrivileges.isNodeManaged(req, nodeID)) 
	throw new PipelineException
	  ("Only a user with Node Manager privileges may create a node JAR archive " + 
           "containing nodes from a working areas owned by another user!");

      opn.notify(timer, "Validating Nodes..."); 

      /* get the current status of the root node */ 
      NodeStatus status = performNodeOperation(new NodeOp(), nodeID, timer, sessionID);
      
      /* collecting validated working versions in the order they should be unpacked */ 
      LinkedList<NodeMod> nodes = new LinkedList<NodeMod>();
      validatePacked(status, nodes, new TreeSet<String>());

      /* get the associated node annotations */ 
      DoubleMap<String,String,BaseAnnotation> annotations = 
        new DoubleMap<String,String,BaseAnnotation>();
      for(NodeMod mod : nodes) {
        String nname = mod.getName();

        timer.acquire();
        LoggedLock lock = getAnnotationsLock(name); 
        lock.acquireReadLock();
        try {
          timer.resume();
    
          TreeMap<String,BaseAnnotation> table = getAnnotationsTable(nname);
          if(table != null) {
            for(String aname : table.keySet())
              annotations.put(nname, aname, table.get(aname));
          }
        }
        finally {
          lock.releaseReadLock();
        }
      }

      /* get the bundled toolsets and packages */ 
      DoubleMap<String,OsType,Toolset> bundledToolsets = 
        new DoubleMap<String,OsType,Toolset>();
      TripleMap<String,OsType,VersionID,PackageVersion> bundledPackages = 
        new TripleMap<String,OsType,VersionID,PackageVersion>();
      {
        timer.acquire();
        synchronized(pToolsets) {
          timer.resume();
          
          for(NodeMod mod : nodes) {
            String tname = mod.getToolset();
            if(!bundledToolsets.containsKey(tname)) {
              TreeMap<OsType,Toolset> toolsets = pToolsets.get(tname);
              for(OsType os : toolsets.keySet()) {
                Toolset toolset = toolsets.get(os);
                if(toolset == null) 
                  toolset = readToolset(tname, os);
                
                bundledToolsets.put(tname, os, toolset);
              }
            }
          }
        }

	timer.acquire();
	synchronized(pToolsetPackages) {
	  timer.resume();
	  
          for(String tname : bundledToolsets.keySet()) {
            for(OsType os : bundledToolsets.keySet(tname)) {
              Toolset tset = bundledToolsets.get(tname, os);

              int wk;
              for(wk=0; wk<tset.getNumPackages(); wk++) {
                String pname = tset.getPackageName(wk);
                VersionID vid = tset.getPackageVersionID(wk); 
                PackageVersion pkg = getToolsetPackage(pname, vid, os);
                bundledPackages.put(pname, os, vid, pkg);
              }
            }
          }
        }
      }
      
      /* create the node bundle and node JAR archive */ 
      NodeBundle bundle = null; 
      Path nodeArchive = null;
      {
        try {
          bundle = new NodeBundle(TimeStamps.now(), nodeID, nodes, annotations, 
                                  bundledToolsets, bundledPackages);
        }
        catch(Exception ex) {
          throw new PipelineException(ex);
        }

        FileMgrClient fclient = acquireFileMgrClient();
        long monitorID = fclient.addMonitor(new RelayOpMonitor(opn));
        try {
          nodeArchive = fclient.packNodes(bundle);
        }
        finally {
          fclient.removeMonitor(monitorID); 
          releaseFileMgrClient(fclient);
        }
      }

      /* post-op tasks */ 
      {
        PackExtFactory factory = new PackExtFactory(bundle); 
        startExtensionTasks(timer, factory);
      }
      
      return new NodePackRsp(timer, nodeID, nodeArchive);    
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }
  }

  /**
   * Recursively validate the nodes to pack while collecting the working versions in
   * depth-first order.
   */ 
  private void 
  validatePacked
  (
   NodeStatus status,  
   LinkedList<NodeMod> nodes,
   TreeSet<String> seen
  ) 
    throws PipelineException
  {
    for(NodeStatus sstatus : status.getSources()) 
      validatePacked(sstatus, nodes, seen);      

    if(seen.contains(status.getName()))
      return;

    seen.add(status.getName());

    NodeDetailsHeavy details = status.getHeavyDetails();
    if(details == null) 
      throw new PipelineException
        ("Cannot create a node bundle containing a checked-in node " + 
         "(" + status.getName() + ")!");
    
    if(details.getOverallQueueState() != OverallQueueState.Finished) 
      throw new PipelineException
        ("Cannot create a node bundle containing the node (" + status.getName() + ") " + 
         "unless its QueueState is Finished (blue)!");

    NodeMod mod = details.getWorkingVersion();
    if(mod == null) 
      throw new PipelineException
        ("Cannot create a node bundle containing the node (" + status.getName() + ") " + 
         "unless it has been checked-out!");

    nodes.add(mod);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Extract the node metadata from a node bundle containing a tree of nodes packed at 
   * another site. <P> 
   * 
   * This method is useful for querying for information about the toolsets being used by 
   * the nodes in the node bundle without unpacking them.  Using this information, a suitable
   * mapping of toolsets from the site which created the nodes to the toolsets at the local
   * site can be generated.  If this remapping of toolsets is not specified, than the default
   * toolset at the local site will be used for all unpacked nodes.  In many cases, this will
   * not be sufficient to insure that the nodes function properly after being unpacked.
   * 
   * @param req 
   *   The extract bundle request.
   *
   * @return
   *   <CODE>NodeExtractBundleRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to extract the node bundle.
   */ 
  public Object
  extractBundle
  ( 
   NodeExtractBundleReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer(); 

    try {    
      Path bundlePath = req.getPath();

      /* extract the node bundle */ 
      NodeBundle bundle = null; 
      {
        FileMgrClient fclient = acquireFileMgrClient();
        try {
          bundle = fclient.extractBundle(bundlePath);
        }
        finally {
          releaseFileMgrClient(fclient);
        }
      }

      return new NodeExtractBundleRsp(timer, bundle, bundlePath); 
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Unpack a node bundle containing a tree of nodes packed at another site into the given
   * working area.<P> 
   * 
   * @param req 
   *   The extract bundle request.
   *
   * @param opn
   *   The operation progress notifier.
   * 
   * @param sessionID
   *   The unique network connection session ID.
   *
   * @return
   *   <CODE>NodeUnpackReq</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to unpack the nodes from the node bundle.
   */ 
  public Object
  unpackNodes
  ( 
   NodeUnpackReq req, 
   OpNotifiable opn, 
   long sessionID
  ) 
  {
    Path bundlePath = req.getPath();

    String author = req.getAuthor();
    String view   = req.getView();

    boolean releaseOnError = req.getReleaseOnError();
    ActionOnExistence actOnExist = req.getActionOnExistence();
    TreeMap<String,VersionID> lockedVersions = req.getLockedVersions(); 
    TreeMap<String,String> toolsetRemap      = req.getToolsetRemap();
    TreeMap<String,String> selectionKeyRemap = req.getSelectionKeyRemap();
    TreeMap<String,String> licenseKeyRemap   = req.getLicenseKeyRemap();
    TreeMap<String,String> hardwareKeyRemap  = req.getHardwareKeyRemap();

    TaskTimer timer = new TaskTimer(); 

    /* pre-op tests */
    try {
      UnpackExtFactory factory = 
        new UnpackExtFactory(bundlePath, author, view, releaseOnError, actOnExist, 
                             toolsetRemap, 
                             selectionKeyRemap, licenseKeyRemap, hardwareKeyRemap, null);
      performExtensionTests(timer, factory);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	

      /* add the owner as a user if not already added */ 
      if(pAdminPrivileges.addMissingUser(author)) {
        updateAdminPrivileges();
        
        WorkGroups groups = pAdminPrivileges.getWorkGroups();
        startExtensionTasks(timer, new SetWorkGroupsExtFactory(groups)); 
      }

      if(!pAdminPrivileges.isNodeManaged(req, author)) 
	throw new PipelineException
	  ("Only a user with Node Manager privileges may unpack a node bundle " +
           "into a working area owned by another user!");

      opn.notify(timer, "Extracting Nodes..."); 

      /* make sure the working area exists */ 
      createWorkingAreaHelper(timer, author, view);

      /* extract the node bundle */ 
      NodeBundle bundle = null; 
      {
        FileMgrClient fclient = acquireFileMgrClient();
        try {
          bundle = fclient.extractBundle(bundlePath);
        }
        finally {
          releaseFileMgrClient(fclient);
        }
      }
      
      /* use the bundle builder to unpack the nodes, 
           determine which nodes where not conformed during the process */ 
      TreeSet<String> unconformed = new TreeSet<String>();
      {
        /* initialize the builder's parameters */ 
        MultiMap<String, String> cparams = new MultiMap<String, String>();
        {
          LinkedList<String> ckeys = new LinkedList<String>(); 
          ckeys.add("BundleBuilder");
          ckeys.add(BaseUtil.aUtilContext); 
          
          ckeys.add(UtilContextUtilityParam.aAuthor); 
          cparams.putValue(ckeys, author);
          ckeys.removeLast();
          
          ckeys.add(UtilContextUtilityParam.aView); 
          cparams.putValue(ckeys, view);
          ckeys.removeLast();
          
          timer.acquire();
          synchronized(pDefaultToolsetLock) {
            timer.resume();	
            
            ckeys.add(UtilContextUtilityParam.aToolset); 
            cparams.putValue(ckeys, pDefaultToolset);
            ckeys.removeLast(); 
          }
          ckeys.removeLast(); 

          ckeys.add(BaseBuilder.aActionOnExistence); 
          cparams.putValue(ckeys, actOnExist.toTitle()); 
          ckeys.removeLast(); 
          
          ckeys.add(BaseBuilder.aReleaseOnError); 
          cparams.putValue(ckeys, String.valueOf(releaseOnError)); 
          ckeys.removeLast(); 
        }
        
        opn.notify(timer, "Creating Local Nodes..."); 

        BuilderInformation info = new BuilderInformation(null, false, false, false, cparams);
        MasterMgrClient mclient = new MasterMgrClient();  // MAKE THIS DIRECT!!

        QueueMgrControlClient qclient = acquireQueueMgrClient();
        try {
          BaseBuilder builder = 
            new BundleBuilder(mclient, qclient, info, bundle, bundlePath, 
                              lockedVersions, toolsetRemap, 
                              selectionKeyRemap, licenseKeyRemap, hardwareKeyRemap);
          builder.run();
        } 
        finally {
          releaseQueueMgrClient(qclient);
        }

        switch(actOnExist) {
        case CheckOut:
        case Continue:
          unconformed.addAll(info.getNewStageInformation().getAllCheckedOutNodes());
          unconformed.addAll(info.getNewStageInformation().getSkippedNodes());
        }

        unconformed.addAll(lockedVersions.keySet());
      }
          
      /* determine which nodes have files which should NOT be unpacked: 
          if there are nodes which were just checked-out upstream or left unaltered, 
            then the files associated with unpacked nodes with enabled actions should 
            be skipped since they will need to be regenerated anyway */
      TreeSet<String> skipUnpack = new TreeSet<String>();
      skipUnpack.addAll(unconformed);
      if(!unconformed.isEmpty()) {
        NodeID rootID = new NodeID(author, view, bundle.getRootNodeID().getName());
        NodeStatus status = performNodeOperation(null, rootID, timer, sessionID);
        for(NodeMod mod : bundle.getWorkingVersions()) {
          if((mod.getAction() != null) && mod.isActionEnabled()) {
            String nname = mod.getName();
            NodeStatus nstatus = status.findUpstreamNamed(nname);
            if(nstatus != null) {
              for(String uname : unconformed) {
                if(nstatus.hasUpstreamNamed(uname)) {
                  skipUnpack.add(nname);
                  break;
                }
              }
            }
          }
        }
      }

      /* unpack the node data files */ 
      FileMgrClient fclient = acquireFileMgrClient();
      long monitorID = fclient.addMonitor(new RelayOpMonitor(opn));
      try {
        fclient.unpackNodes(bundlePath, bundle, author, view, skipUnpack); 
      }
      finally {
        fclient.removeMonitor(monitorID); 
        releaseFileMgrClient(fclient);
      }
      
      /* post-op tasks */ 
      {
        UnpackExtFactory factory = 
          new UnpackExtFactory(bundlePath, author, view, releaseOnError, actOnExist, 
                               toolsetRemap, 
                               selectionKeyRemap, licenseKeyRemap, hardwareKeyRemap, bundle); 
        startExtensionTasks(timer, factory);
      }
      
      return new SuccessRsp(timer);    
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }
  }


 
  /*----------------------------------------------------------------------------------------*/
  /*   S I T E   V E R S I O N S                                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Creates a TAR archive containing both files and metadata associated with a checked-in
   * version of a node suitable for transfer to a remote site.<P> 
   * 
   * @param req 
   *   The extract site version request.
   *
   * @return
   *   <CODE>NodeExtractSiteVersionRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to extract the site version.
   */ 
  public Object
  extractSiteVersion
  ( 
   NodeExtractSiteVersionReq req 
  ) 
  {
    String name = req.getName(); 
    VersionID vid = req.getVersionID();
    TreeSet<String> referenceNames = req.getReferenceNames();
    String localSiteName = req.getLocalSiteName();
    TreeSet<FileSeq> replaceSeqs = req.getReplaceSeqs();
    TreeMap<String,String> replacements = req.getReplacements();
    Path dir = req.getDir();
    String creator = req.getRequestor();

    TaskTimer timer = new TaskTimer(); 
    
    if(!pAdminPrivileges.isMasterAdmin(req)) 
      return new FailureRsp
        (timer, "Only a user with Master Admin privileges may extract site versions!"); 

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();
 
      /* lookup the node version */ 
      NodeVersion ovsn = null;
      {
        timer.acquire();
        LoggedLock lock = getCheckedInLock(name);
        lock.acquireReadLock();
        try {
          timer.resume();	

          CheckedInBundle bundle = getCheckedInBundles(name).get(vid);
          if(bundle == null) 
            throw new PipelineException 
              ("No checked-in version (" + vid + ") of node (" + name + ") exists!"); 
          
          ovsn = new NodeVersion(bundle.getVersion());
        }
        finally {
          lock.releaseReadLock();
        }
      }

      /* localize the version */ 
      Path npath = new Path(name);
      long stamp = System.currentTimeMillis(); 
      String tarName = (stamp + "-" + npath.getName() + ".tar"); 
      Path tarPath = new Path(dir, tarName); 
      NodeVersion vsn = 
        new NodeVersion(ovsn, referenceNames, localSiteName, stamp, creator, tarName);

      /* combine automatic replacements for references with the supplied replacements */ 
      TreeMap<String,String> repls = new TreeMap<String,String>();
      repls.put(name, siteLocalName(name, localSiteName));
      for(String sname : referenceNames) 
        repls.put(sname, siteLocalName(sname, localSiteName));
      if(replacements != null) 
        repls.putAll(replacements);

      /* fix the files and create the TAR archive */ 
      {
        FileMgrClient fclient = acquireFileMgrClient();
        try {
          fclient.extractSiteVersion(name, referenceNames, localSiteName, 
                                     replaceSeqs, repls, vsn, stamp, creator, tarPath); 
        }
        finally {
          releaseFileMgrClient(fclient);
        }
      }

      return new NodeExtractSiteVersionRsp(timer, name, vid, tarPath); 
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }
  }

  /** 
   * Generate a localized name for extracted nodes.
   */ 
  private String
  siteLocalName
  (
   String name, 
   String localSiteName
  ) 
  {
    Path orig = new Path(name); 
    Path fixed = new Path(new Path(orig.getParentPath(), localSiteName), orig.getName());
    return fixed.toString();
  }

  /*----------------------------------------------------------------------------------------*/

  /**
   * Lookup the NodeVersion contained within the extracted site version TAR archive.
   * 
   * @param req 
   *   The request.
   *
   * @return
   *   <CODE>NodeLookupSiteVersionRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the node version.
   */ 
  public Object
  lookupSiteVersion
  ( 
   NodeSiteVersionReq req 
  ) 
  {
    Path tarPath = req.getTarPath();

    TaskTimer timer = new TaskTimer(); 

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	

      /* get the node version from the TAR archive */ 
      NodeVersion vsn = null;
      {
        FileMgrClient fclient = acquireFileMgrClient();
        try {
          vsn = fclient.lookupSiteVersion(tarPath); 
        }
        finally {
          releaseFileMgrClient(fclient);
        }
      }

      return new NodeLookupSiteVersionRsp(timer, vsn);    
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the extracted node contained in the given TAR archive has already been inserted
   * into the node database.
   * 
   * @param req 
   *   The request.
   *
   * @return
   *   <CODE>NodeIsSiteVersionInsertedRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine if the node is inserted.
   */ 
  public Object
  isSiteVersionInserted
  ( 
   NodeSiteVersionReq req 
  ) 
  {
    Path tarPath = req.getTarPath();

    TaskTimer timer = new TaskTimer(); 

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	

      /* get the node version from the TAR archive */ 
      NodeVersion vsn = null;
      {
        FileMgrClient fclient = acquireFileMgrClient();
        try {
          vsn = fclient.lookupSiteVersion(tarPath); 
        }
        finally {
          releaseFileMgrClient(fclient);
        }
      }

      /* see if the version already exists in the database */ 
      boolean isInserted = false;
      {
        String name = vsn.getName();
        VersionID vid = vsn.getVersionID();

        timer.acquire();
        LoggedLock lock = getCheckedInLock(name);
        lock.acquireReadLock();
        try {
          timer.resume();	
          
          TreeMap<VersionID,CheckedInBundle> checkedIn = null; 
          try {
            checkedIn = getCheckedInBundles(name);
          }
          catch(PipelineException ex) {
          }
          
          isInserted = ((checkedIn != null) && checkedIn.containsKey(vid));
        }
        finally {
          lock.releaseReadLock();
        }  
      }

      return new SimpleBooleanRsp(timer, isInserted);    
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Checks each of the source nodes referenced by the extracted node contained in the 
   * given TAR archive and returns the names and versions of any of them that are not
   * already in the node database.<P> 
   * 
   * @param req 
   *   The request.
   *
   * @return
   *   <CODE>NodeGetMissingSiteVersionRefsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the missing versions.
   */ 
  public Object
  getMissingSiteVersionRefs
  ( 
   NodeSiteVersionReq req 
  ) 
  {
    Path tarPath = req.getTarPath();

    TaskTimer timer = new TaskTimer(); 

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	

      /* get the node version from the TAR archive */ 
      NodeVersion vsn = null;
      {
        FileMgrClient fclient = acquireFileMgrClient();
        try {
          vsn = fclient.lookupSiteVersion(tarPath); 
        }
        finally {
          releaseFileMgrClient(fclient);
        }
      }

      /* get the names and versions of any missing dependencies */ 
      TreeMap<String,VersionID> missing = new TreeMap<String,VersionID>(); 
      for(LinkVersion link : vsn.getSources()) {
        String sname = link.getName();
        VersionID svid = link.getVersionID();

        timer.acquire();
        LoggedLock lock = getCheckedInLock(sname);
        lock.acquireReadLock();
        try {
          timer.resume();	

          TreeMap<VersionID,CheckedInBundle> checkedIn = null; 
          try {
            checkedIn = getCheckedInBundles(sname);
          }
          catch(PipelineException ex) {
          }

          if((checkedIn == null) || !checkedIn.containsKey(svid)) 
            missing.put(sname, svid);
        }
        finally {
          lock.releaseReadLock();
        }  
      }

      return new NodeGetMissingSiteVersionRefsRsp(timer, missing);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Inserts a node version into the local node database previously extraced from a remote
   * site using the {@link #extractSiteVersion} method.<P> 
   * 
   * @param req 
   *   The request.
   *
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to insert the node.
   */ 
  public Object
  insertSiteVersion
  ( 
   NodeSiteVersionReq req 
  ) 
  {
    Path tarPath = req.getTarPath();

    TaskTimer timer = new TaskTimer(); 

    if(!pAdminPrivileges.isMasterAdmin(req)) 
      return new FailureRsp
        (timer, "Only a user with Master Admin privileges may insert site versions!"); 

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	

      /* get the node version from the TAR archive */ 
      NodeVersion vsn = null;
      String name = null; 
      VersionID vid = null; 
      {
        FileMgrClient fclient = acquireFileMgrClient();
        try {
          vsn  = fclient.lookupSiteVersion(tarPath); 
          name = vsn.getName();
          vid  = vsn.getVersionID();
        }
        finally {
          releaseFileMgrClient(fclient);
        }

        if(vsn.getCheckSums().isEmpty()) 
          throw new PipelineException 
            ("Unable to insert version (" + vid + ") of node (" + name + ") because " + 
             "it does not contain per-file checksum information.  CheckSums stored in " + 
             "the Node Version where introduced in Pipeline-2.4.9, so its likely that " +
             "you are attempting to insert a node from a pre-2.4.9 version of Pipeline!");
      }

      /* make sure the named toolset exits */ 
      if(!isValidToolsetName(vsn.getToolset()))
         throw new PipelineException
           ("Unable to insert version (" + vid + ") of node (" + name + ") because " + 
            "its Toolset property (" + vsn.getToolset() + ") names a toolset which does " + 
            "not exist at the local site!"); 

      /* insert the version into the database */ 
      {
        timer.acquire();
        LoggedLock lock = getCheckedInLock(name);
        lock.acquireWriteLock();
        try {
          timer.resume();	
          
          /* lookup the bundle */ 
          TreeMap<VersionID,CheckedInBundle> checkedIn = null; 
          try {
            checkedIn = getCheckedInBundles(name);
          }
          catch(PipelineException ex) {
          }
          
          /* make sure it doesn't already exist */ 
          if((checkedIn != null) && checkedIn.containsKey(vid))
            throw new PipelineException
              ("A checked-in version (" + vid + ") of node (" + name + ") already exists!"); 
          
          /* make sure all of the dependencies do exist */ 
          {
            TreeMap<String,VersionID> missing = new TreeMap<String,VersionID>(); 
            for(LinkVersion link : vsn.getSources()) {
              String sname = link.getName();
              VersionID svid = link.getVersionID();
              
              timer.acquire();
              LoggedLock slock = getCheckedInLock(sname);
              slock.acquireReadLock();
              try {
                timer.resume();	
                
                TreeMap<VersionID,CheckedInBundle> scheckedIn = null; 
                try {
                  scheckedIn = getCheckedInBundles(sname);
                }
                catch(PipelineException ex) {
                }
                
                if((scheckedIn == null) || !scheckedIn.containsKey(svid)) 
                  missing.put(sname, svid);
              }
              finally {
                slock.releaseReadLock();
              }  
            }

            if(!missing.isEmpty()) {
              StringBuilder buf = new StringBuilder(); 
              buf.append("Unable to insert version (" + vid + ") of node (" + name + ") " + 
                         "because the following dependencies of this node do not yet " + 
                         "exist in the node database:\n\n"); 

              for(String sname : missing.keySet()) 
                buf.append("  " + sname + " v" + missing.get(sname) + "\n");

              buf.append("\nYou must insert these missing versions before you will be " + 
                         "able to insert the target node.");

              throw new PipelineException(buf.toString());
            }
          }
          
          /* insert the files into the repository */ 
          {
            FileMgrClient fclient = acquireFileMgrClient();
            try {
              fclient.insertSiteVersion(tarPath); 
            }
            finally {
              releaseFileMgrClient(fclient);
            }
          }

          /* write the new version database entry to disk */ 
          writeCheckedInVersion(vsn);

          /* add the new version to the checked-in bundles */ 
          if(checkedIn == null) {
            checkedIn = new TreeMap<VersionID,CheckedInBundle>();

            synchronized(pCheckedInBundles) {
	      pCheckedInBundles.put(name, checkedIn);
	    }

	    /* keep track of the change to the node version cache */ 
	    incrementCheckedInCounter(name, vid);
          }
          checkedIn.put(vid, new CheckedInBundle(vsn));

	  /* update the node tree entry */ 
	  pNodeTree.addCheckedInNodeTreePath(vsn);

	  /* set the checked-in downstream links from the upstream nodes to this node */ 
	  for(LinkVersion link : vsn.getSources()) { 
	    String lname = link.getName();

	    timer.acquire();
	    LoggedLock downstreamLock = getDownstreamLock(lname);
	    downstreamLock.acquireWriteLock();
	    try {
	      timer.resume();

	      DownstreamLinks dsl = getDownstreamLinks(lname);
	      dsl.addCheckedIn(link.getVersionID(), name, vid);
	    }  
	    finally {
	      downstreamLock.releaseWriteLock();
	    }     
	  }
        }
        finally {
          lock.releaseWriteLock();
        }  
      }

      /* add the RemoteNode annotation */ 
      {      
        timer.suspend();

        PluginMgrClient client = PluginMgrClient.getInstance();
        BaseAnnotation annot = 
          client.newAnnotation("RemoteNode", new VersionID("2.4.5"), "Temerity");

        TaskTimer ctimer = new TaskTimer("MasterMgr.addAnnotationHelper()");
        Object rsp = addAnnotationHelper(req, ctimer, name, "RemoteNode", annot);
        if(rsp instanceof FailureRsp) {
          FailureRsp failure = (FailureRsp) rsp;
          throw new PipelineException
            ("Unable to add the RemoteNode annotation to the newly inserted node " + 
             "(" + name + "):\n" + failure.getMessage());
        }

        timer.accum(ctimer);
      }
      
      return new SuccessRsp(timer);    
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   N O D E   E V E N T S                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Retrieve the record of all significant operations involving the given nodes during
   * the specified time interval.
   * 
   * @param req 
   *   The request.
   *
   * @return
   *   <CODE>NodeGetEventsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to retrieve the events.
   */ 
  public Object
  getNodeEvents
  (
   NodeGetEventsReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer();

    try {
      MappedLinkedList<Long,BaseNodeEvent> events = 
	readNodeEvents(timer, req.getNames(), req.getUsers(), req.getInterval());
      return new NodeGetEventsRsp(timer, events);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Test whether the current user should be allowed to edit the given node.
   * 
   * @param req
   *   The requet.
   *
   * @param hostname
   *   The full name of the host on which the Editor will be run.
   * 
   * @return
   *   <CODE>SucessRsp</CODE> if editing is allowed or 
   *   <CODE>FailureRsp</CODE> if editing is not allowed.
   */ 
  public Object 
  editingTest
  (
   NodeEditingTestReq req, 
   String hostname 
  ) 
  {
    NodeID nodeID = req.getNodeID();
    PluginID editorID = req.getEditorID();
    String imposter = req.getImposter(); 

    TaskTimer timer = 
      new TaskTimer("MasterMgr.editingTest(): " + nodeID + 
                    ((imposter != null) ? (" by " + imposter) : ""));

    EditingTestExtFactory factory = 
      new EditingTestExtFactory(nodeID, editorID, hostname, imposter); 
    try {
      performExtensionTests(timer, factory);
      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
  }
   
  /**
   * Signal that an Editor plugin has started editing files associated with the 
   * given working version of a node.
   * 
   * @param timer
   *   The task timer.
   * 
   * @param event
   *   The editing event.
   * 
   * @return 
   *   The unique editing session identifier.
   */ 
  public long
  editingStarted
  (
   TaskTimer timer, 
   EditedNodeEvent event
  ) 
  {
    timer.acquire();
    synchronized(pRunningEditors) {
      timer.resume();	
      
      long editID = pNextEditorID;
      pRunningEditors.put(editID, event);
      pNextEditorID++;

      /* post-op tasks */ 
      startExtensionTasks(timer, new EditingStartedExtFactory(editID, event)); 

      return editID;
    }
  }
   
  /**
   * Signal that an Editor plugin has finished editing files associated with the 
   * working version of a node.
   * 
   * @param timer
   *   The task timer.
   * 
   * @param editID 
   *   The unique ID for the editing session.
   */ 
  public void 
  editingFinished
  (
   TaskTimer timer, 
   long editID  
  ) 
  {
    timer.acquire();
    synchronized(pRunningEditors) {
      timer.resume();	
      
      EditedNodeEvent event = pRunningEditors.remove(editID);
      if(event != null) {
	event.setFinishedStamp(System.currentTimeMillis()); 
	pPendingEvents.add(event);
      
        /* post-op tasks */ 
        startExtensionTasks(timer, new EditingFinishedExtFactory(editID, event)); 
      }
    }
  }

  /**
   * Get the table of the working areas in which the given node is currently being 
   * edited. 
   * 
   * @param req 
   *   The request.
   *
   * @return
   *   <CODE>NodeGetWorkingAreasRsp</CODE>.
   */
  public Object 
  getWorkingAreasEditing
  (
   NodeGetByNameReq req
  ) 
    throws PipelineException
  {
    TaskTimer timer = new TaskTimer();

    timer.acquire();
    synchronized(pRunningEditors) {
      timer.resume();
    
      TreeMap<String,TreeSet<String>> areas = new TreeMap<String,TreeSet<String>>();

      for(EditedNodeEvent event : pRunningEditors.values()) {
	if(event.getNodeName().equals(req.getName())) {
	  TreeSet<String> views = areas.get(event.getAuthor());
	  if(views == null) {
	    views = new TreeSet<String>();
	    areas.put(event.getAuthor(), views);
	  }

	  views.add(event.getView());
	}
      }

      return new NodeGetWorkingAreasRsp(timer, areas);
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
   * @param opn
   *   The operation progress notifier.
   * 
   * @param sessionID
   *   The unique network connection session ID.
   * 
   * @return 
   *   <CODE>NodeSubmitJobsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to submit the jobs.
   */ 
  public Object
  submitJobs
  ( 
   NodeSubmitJobsReq req, 
   OpNotifiable opn, 
   long sessionID
  )
  {
    TaskTimer timer = new TaskTimer();

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	
      
      if(!pAdminPrivileges.isQueueManaged(req, req.getNodeID()))
	throw new PipelineException
	  ("Only a user with Queue Manager privileges may submit jobs for nodes in " + 
	   "working areas owned by another user!");

      return submitJobGroupsCommon
        (req.getNodeID(), req.getFileIndices(), null, 
         req.getBatchSize(), req.getPriority(), req.getRampUp(),
         req.getMaxLoad(), req.getMinMemory(), req.getMinDisk(),
         req.getSelectionKeys(), req.getLicenseKeys(), req.getHardwareKeys(), 
         timer, opn, sessionID);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      pDatabaseLock.releaseReadLock();
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
   * @param opn
   *   The operation progress notifier.
   * 
   * @param sessionID
   *   The unique network connection session ID.
   * 
   * @return 
   *   <CODE>NodeSubmitJobsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to submit the jobs.
   */ 
  public Object
  resubmitJobs
  ( 
   NodeResubmitJobsReq req, 
   OpNotifiable opn, 
   long sessionID
  )
  {
    TaskTimer timer = new TaskTimer();

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	

      if(!pAdminPrivileges.isQueueManaged(req, req.getNodeID()))
	throw new PipelineException
	  ("Only a user with Queue Manager privileges may submit jobs for nodes in " + 
	   "working areas owned by another user!");

      /* submit the jobs */ 
      return submitJobGroupsCommon
        (req.getNodeID(), null, req.getTargetFileSequences(), 
         req.getBatchSize(), req.getPriority(), req.getRampUp(),
         req.getMaxLoad(), req.getMinMemory(), req.getMinDisk(),
         req.getSelectionKeys(), req.getLicenseKeys(), req.getHardwareKeys(), 
         timer, opn, sessionID);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }    
  }

  /**
   * Common code used by {@link #submitJobs submitJobs} and {@link #resubmitJobs resubmitJobs}
   * methods to submit a single job group.
   * 
   * @param rootNodeID
   *   The unique ID of the root node of the submission.
   * 
   * @param indices
   *   The file sequence indices of the files to regenerate or 
   *   <CODE>null</CODE> for all files.
   * 
   * @param targetSeqs
   *   The target primary file sequences to regenerate or 
   *   <CODE>null</CODE> to use the file "indices" parameter directly.
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
   * @param maxLoad
   *   Overrides the maximum system load allowed on an eligible host.
   * 
   * @param minMemory 
   *   Overrides the minimum amount of free memory (in bytes) required on an eligible host.
   * 
   * @param minDisk 
   *   Overrides the minimum amount of free temporary local disk space (in bytes) required 
   *   on an eligible host.
   * 
   * @param selectionKeys 
   *   Overrides the set of selection keys an eligable host is required to have for jobs 
   *   associated with the root node of the job submission.
   * 
   * @param licenseKeys 
   *   Overrides the set of license keys required by them job associated with the root 
   *   node of the job submission.
   * 
   * @param hardwareKeys 
   *   Overrides the set of hardware keys required by the job associated with the root 
   *   node of the job submission.
   * 
   * @param timer
   *   The task timer.
   * 
   * @param opn
   *   The operation progress notifier.
   * 
   * @param sessionID
   *   The unique network connection session ID.
   * 
   * @return 
   *   The <CODE>NodeSubmitJobsRsp</CODE> for the newly created job groups.
   */ 
  private NodeSubmitJobsRsp
  submitJobGroupsCommon
  (
   NodeID rootNodeID, 
   TreeSet<Integer> indices,
   TreeSet<FileSeq> targetSeqs, 
   Integer batchSize, 
   Integer priority, 
   Integer rampUp,
   Float maxLoad,              
   Long minMemory,              
   Long minDisk,  
   Set<String> selectionKeys,
   Set<String> licenseKeys,
   Set<String> hardwareKeys,
   TaskTimer timer, 
   OpNotifiable opn, 
   long sessionID   
  )
    throws PipelineException 
  {
    LinkedList<QueueJobGroup> jobGroups = new LinkedList<QueueJobGroup>();
    ArrayList<String> exceptions = new ArrayList<String>(); 
    {
      TreeSet<String> assocRoots = new TreeSet<String>();     
      NodeID nodeID = rootNodeID;

      /* cache the currently available selection, license and hardware keys */ 
      ArrayList<SelectionKey> allSelectionKeys = null;
      ArrayList<LicenseKey>   allLicenseKeys   = null;
      ArrayList<HardwareKey>  allHardwareKeys  = null;
      QueueMgrControlClient qclient = acquireQueueMgrClient();
      long timeStamp = System.currentTimeMillis();
      try {
        allSelectionKeys = qclient.getSelectionKeys(); 
        allLicenseKeys   = qclient.getLicenseKeys();   
        allHardwareKeys  = qclient.getHardwareKeys();  
      }
      finally {
        releaseQueueMgrClient(qclient);
      }
        
      /* as long as there are more root nodes to submit... */ 
      while((nodeID != null) || !assocRoots.isEmpty()) {
        
        String name = null;
        if(nodeID == null) {
          name = assocRoots.first();
          nodeID = new NodeID(rootNodeID, name);
        }
        
        /* single thread the status update and job submit process... */
        timer.acquire();
        synchronized(pQueueSubmitLock) { 
          timer.resume();

          /* get the current status of the root submit node */ 
          NodeStatus status = null;
          {
            TreeMap<String,NodeStatus> cache = new TreeMap<String,NodeStatus>();
            status = performNodeOperation(new StatusNodeOp(opn), nodeID, DownstreamMode.None, 
                                          cache, timer, sessionID); 
            
            /* set the number of nodes being processed */ 
            opn.setTotalSteps(cache.size());
          }

          opn.notify(timer, "Submitting Jobs..."); 

          /* compute file indices if not already specified */ 
          if(indices == null) {
            indices = new TreeSet<Integer>();  
            
            NodeMod work = status.getHeavyDetails().getWorkingVersion();
            if(work == null) 
              throw new PipelineException
                ("Cannot generate jobs for the checked-in node (" + status + ")!");
            
            /* compute the file indices for all of the given target file sequences */ 
            if(targetSeqs != null) {
              TreeMap<File,Integer> fileIndices = new TreeMap<File,Integer>();
              {
                FileSeq fseq = work.getPrimarySequence(); 
                int wk = 0;
                for(File file : fseq.getFiles()) {
                  fileIndices.put(file, wk);
                  wk++;
                }
              }
              
              for(FileSeq fseq : targetSeqs) {
                for(File file : fseq.getFiles()) {
                  Integer idx = fileIndices.get(file);
                  if(idx != null) 
                    indices.add(idx);
                }
              }
            }
            
            /* compute the file indices of all primary target files */
            else {  
              FileSeq fseq = work.getPrimarySequence(); 
              int wk; 
              for(wk=0; wk<fseq.numFrames(); wk++) 
                indices.add(wk);
            }
          }
          
          /* submit the jobs for the root node */ 
          QueueJobGroup group = null;
          if(rootNodeID.equals(nodeID)) {
            group = submitJobsCommon(status, indices, batchSize, priority, rampUp,
                                     maxLoad, minMemory, minDisk,
                                     selectionKeys, licenseKeys, hardwareKeys, timeStamp,
                                     allSelectionKeys, allLicenseKeys, allHardwareKeys, 
                                     assocRoots, exceptions, timer);
          }
          else {
            group = submitJobsCommon(status, indices, null, null, null,
                                     null, null, null,
                                     null, null, null, timeStamp, 
                                     allSelectionKeys, allLicenseKeys, allHardwareKeys, 
                                     assocRoots, exceptions, timer);
          }

          if(group != null) 
            jobGroups.add(group);
        }
        
        /* reset for the next root node (if any) */
        {
          nodeID  = null; 
          indices = null;

          if(name != null) 
            assocRoots.remove(name);           
        }
      }
    }

    if(jobGroups.isEmpty()) 
      throw new PipelineException
        ("No new jobs where generated for node (" + rootNodeID.getName() + ") or any " + 
         "node upstream of this node!");
    
    if (exceptions.size() > 0) {
      String msg = "";
      for (String each : exceptions)
        msg += each + "\n\n";
      
      throw new PipelineException
        ("While job submission was successful, the following errors occured during" +
         "KeyChooser execution.  These errors may effect the ability of the jobs on " +
         "the queue to run.\n\n" + msg);
    }
    
    return new NodeSubmitJobsRsp(timer, jobGroups);
  }

  /**
   * Common code used by {@link #submitJobs submitJobs} and {@link #resubmitJobs resubmitJobs}
   * methods to submit a single job group.
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
   * @param maxLoad
   *   Overrides the maximum system load allowed on an eligible host.
   * 
   * @param minMemory 
   *   Overrides the minimum amount of free memory (in bytes) required on an eligible host.
   * 
   * @param minDisk 
   *   Overrides the minimum amount of free temporary local disk space (in bytes) required 
   *   on an eligible host.
   * 
   * @param selectionKeys 
   *   Overrides the set of selection keys an eligable host is required to have for jobs 
   *   associated with the root node of the job submission.
   * 
   * @param licenseKeys 
   *   Overrides the set of license keys required by the job associated with the root 
   *   node of the job submission.
   * 
   * @param hardwareKeys 
   *   Overrides the set of hardware keys required by the job associated with the root 
   *   node of the job submission.
   * 
   * @param timeStamp
   *   The timestamp of the key chooser updates to write into the job file. 
   * 
   * @param allSelectionKeys
   *   A cache of all currently defined selection keys.
   * 
   * @param allLicenseKeys
   *   A cache of all currently defined license keys.
   *
   * @param allHardwareKeys
   *   A cache of all currently defined hardware keys.
   * 
   * @param assocRoots
   *   The names of nodes encountered on the upstream side of an Association link.
   * 
   * @param exceptions
   *   The set of exception thrown during job submission.
   * 
   * @param timer
   *   The task timer.
   * 
   * @return
   *   The newly create job group or 
   *   <CODE>null</CODE> if no jobs where submitted for the root node.
   */ 
  private QueueJobGroup
  submitJobsCommon
  (
   NodeStatus status, 
   TreeSet<Integer> indices,
   Integer batchSize, 
   Integer priority, 
   Integer rampUp,
   Float maxLoad,              
   Long minMemory,              
   Long minDisk,  
   Set<String> selectionKeys,
   Set<String> licenseKeys,
   Set<String> hardwareKeys,
   long timeStamp, 
   ArrayList<SelectionKey> allSelectionKeys, 
   ArrayList<LicenseKey> allLicenseKeys, 
   ArrayList<HardwareKey> allHardwareKeys,
   TreeSet<String> assocRoots, 
   ArrayList<String> exceptions,
   TaskTimer timer 
  )
    throws PipelineException 
  {
    /* generate jobs */ 
    TreeMap<NodeID,Long[]> extJobIDs = new TreeMap<NodeID,Long[]>();
    TreeMap<NodeID,Long[]> nodeJobIDs = new TreeMap<NodeID,Long[]>();
    TreeMap<NodeID,TreeSet<Long>> upsJobIDs = new TreeMap<NodeID,TreeSet<Long>>();
    TreeSet<Long> rootJobIDs = new TreeSet<Long>();
    TreeMap<Long,QueueJob> jobs = new TreeMap<Long,QueueJob>();
      
    Long jobGroupID = pNextJobGroupID++;
      
    submitJobs(status, jobGroupID, indices, 
               true, batchSize, priority, rampUp, maxLoad, minMemory, minDisk, 
               selectionKeys, licenseKeys, hardwareKeys, timeStamp,
               allSelectionKeys, allLicenseKeys, allHardwareKeys, 
               extJobIDs, nodeJobIDs, upsJobIDs, rootJobIDs, jobs, assocRoots, 
               exceptions, timer);
      
    if(jobs.isEmpty()) 
      return null; 
      
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
      new QueueJobGroup(jobGroupID, status.getNodeID(), 
                        status.getHeavyDetails().getWorkingVersion().getToolset(), 
                        targetSeq, orderedRootIDs, externalIDs, 
                        new TreeSet<Long>(jobs.keySet()));

    /* update the job and group IDs file */ 
    writeNextIDs();
      
    /* submit the jobs and job group */  
    QueueMgrControlClient qclient = acquireQueueMgrClient();
    try {
      qclient.submitJobs(group, jobs.values());
    }
    finally {
      releaseQueueMgrClient(qclient);
    }
      
    return group; 
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
   * @param jobGroupID
   *   The id of the job group that these jobs should belong to.
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
   * @param maxLoad
   *   Overrides the maximum system load allowed on an eligible host.
   * 
   * @param minMemory 
   *   Overrides the minimum amount of free memory (in bytes) required on an eligible host.
   * 
   * @param minDisk 
   *   Overrides the minimum amount of free temporary local disk space (in bytes) required 
   *   on an eligible host.
   * 
   * @param selectionKeys 
   *   Overrides the set of selection keys an eligable host is required to have for jobs 
   *   associated with the root node of the job submission.
   * 
   * @param licenseKeys 
   *   Overrides the set of license keys required by them job associated with the root 
   *   node of the job submission.
   *   
   * @param timeStamp
   *   The timestamp of the key chooser updates to write into the job file.
   * 
   * @param allSelectionKeys
   *   A cache of all currently defined selection keys.
   * 
   * @param allLicenseKeys
   *   A cache of all currently defined license keys.
   *
   * @param allHardwareKeys
   *   A cache of all currently defined hardware keys.
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
   * @param assocRoots
   *   The names of nodes encountered on the upstream side of an Association link.
   *
   * @param exceptions
   *   The set of exception thrown during job submission.
   * 
   * @param timer
   *   The task timer.
   */
  private void 
  submitJobs
  (
    NodeStatus status, 
    Long jobGroupID,
    TreeSet<Integer> indices, 
    boolean isRoot, 
    Integer batchSize, 
    Integer priority, 
    Integer rampUp,
    Float maxLoad,              
    Long minMemory,              
    Long minDisk,  
    Set<String> selectionKeys,
    Set<String> licenseKeys,
    Set<String> hardwareKeys,
    long timeStamp,
    ArrayList<SelectionKey> allSelectionKeys, 
    ArrayList<LicenseKey> allLicenseKeys, 
    ArrayList<HardwareKey> allHardwareKeys,
    TreeMap<NodeID,Long[]> extJobIDs,   
    TreeMap<NodeID,Long[]> nodeJobIDs,   
    TreeMap<NodeID,TreeSet<Long>> upsJobIDs, 
    TreeSet<Long> rootJobIDs,    
    TreeMap<Long,QueueJob> jobs, 
    TreeSet<String> assocRoots, 
    ArrayList<String> exceptions,
    TaskTimer timer 
  ) 
    throws PipelineException
  {
    NodeID nodeID = status.getNodeID();
    NodeDetailsHeavy details = status.getHeavyDetails();

    NodeMod work = details.getWorkingVersion();
    if(work == null) 
      throw new PipelineException
	("Cannot generate jobs for the checked-in node (" + status + ")!");
    if(work.isLocked()) 
      return;

    switch(details.getOverallQueueState()) {
    case Dubious:
      throw new PipelineException
        ("Cannot generate jobs for the node (" + status + ") while it is in a Dubious " + 
         "state!  You must Vouch for this node or those upstream originating the " + 
         "Dubious state before any jobs can be queued.");
    }

    /* collect upstream jobs for:
       + nodes without an action or with a disabled action
       + finished nodes with an enabled action and at least one upstream link which is 
          either a Reference or an Association */
    {
      boolean anyRef = false; 
      if(work.isActionEnabled()) {
        switch(details.getOverallQueueState()) {
        case Finished:
          for(LinkMod link : work.getSources()) {
            switch(link.getPolicy()) {
            case Association: 
            case Reference: 
              anyRef = true; 
              break;
            }
          }
        }
      }
      
      if(anyRef || !work.isActionEnabled()) {
        collectNoActionJobs(status, jobGroupID, isRoot, timeStamp,
                            allSelectionKeys, allLicenseKeys, allHardwareKeys, 
                            extJobIDs, nodeJobIDs, upsJobIDs, rootJobIDs, 
                            jobs, assocRoots, exceptions, timer);
        return;
      }
    }
    
    /* generate jobs for node */ 
    int numFrames = work.getPrimarySequence().numFrames();

    int bsize = 0; 
    if(work.getBatchSize() != null) 
      bsize = work.getBatchSize();
    if(isRoot && (batchSize != null)) 
      bsize = batchSize;
    
    Long[] jobIDs = details.getJobIDs();
    QueueState[] queueStates = details.getQueueStates();
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
            case Limbo:
	    case Queued:
	    case Paused:
	      finished = false;
	      break;
	      
            case Stale:
            case Aborted:
            case Failed:
	      finished = false;
	      running  = false;
              break;

            default:
              throw new PipelineException
                ("Somehow a per-frame QueueState of index (" + idx + ") of node " + 
                 "(" + nodeID + ") was " + queueStates[idx] + " even though the " + 
                 "OverallQueueState was " + details.getOverallQueueState() + "!"); 
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
              case Limbo:
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

              default:
                throw new PipelineException
                  ("Somehow a per-frame QueueState of index (" + idx + ") of node " + 
                   "(" + nodeID + ") was " + queueStates[idx] + " even though the " + 
                   "OverallQueueState was " + details.getOverallQueueState() + "!"); 
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
    
    /* no batches to generate so no need to try to create jobs for upstream nodes
         unless we find an association link */ 
    if(batches.isEmpty()) {
      findJobAssocRoots(status, assocRoots);
      return; 
    }

    /* generate jobs for each frame batch */ 
    else {
      if(work.isFrozen()) 
	throw new PipelineException
	  ("Cannot generate jobs for the frozen node (" + nodeID + ")!");
      
      boolean first = true;
      JobReqs jreqs = null;
      
      for(TreeSet<Integer> batch : batches) {
	if(batch.isEmpty())
	  throw new IllegalStateException(); 
	
	/* determine the frame indices of the source nodes depended on by the 
	   frames of this batch */
	TreeMap<String,TreeSet<Integer>> sourceIndices = 
	  new TreeMap<String,TreeSet<Integer>>();
        for(LinkMod link : work.getSources()) {
          switch(link.getPolicy()) {
          case Reference:
          case Dependency:
            {
              NodeStatus lstatus = status.getSource(link.getName());
              NodeMod lwork = lstatus.getHeavyDetails().getWorkingVersion();
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
                          ("The frame offset (" + link.getFrameOffset() + ") for the " + 
                           "link between target node (" + status + ") and source node " +
                           "(" + lstatus + ") overflows the frame range of the source " + 
                           "node!");
                      }
                    }
                    else {
                      frames.add(lidx);
                    }
                  }
                  
                  sourceIndices.put(link.getName(), frames);
                }
                break;
                
              case None: 
                throw new PipelineException
                  ("Somehow a non-Assocation link has a None relationship!");
              }
            }
          }
        }
      
	/* generate jobs for the source frames first */ 
	for(LinkMod link : work.getSources()) {  
          switch(link.getPolicy()) {
          case Association:
            assocRoots.add(link.getName());
            break;
            
          case Reference:
          case Dependency:
            {
              TreeSet<Integer> lindices = sourceIndices.get(link.getName());
              if((lindices != null) && (!lindices.isEmpty())) {
                NodeStatus lstatus = status.getSource(link.getName());
                submitJobs(lstatus, jobGroupID, lindices, 
                           false, null, null, null, null, null, null, null, null, null,
                           timeStamp, allSelectionKeys, allLicenseKeys, allHardwareKeys, 
                           extJobIDs, nodeJobIDs, upsJobIDs, rootJobIDs, 
                           jobs, assocRoots, exceptions, timer);
              }
            }
          }
	}

	/* determine the source job IDs */ 
	TreeSet<Long> sourceIDs = new TreeSet<Long>();
	for(LinkMod link : work.getSources()) {
          switch(link.getPolicy()) {
          case Reference:
          case Dependency:
            {
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
	      NodeMod lwork = lstatus.getHeavyDetails().getWorkingVersion();

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
	  if (first) {
	    jreqs = work.getJobRequirements();
	    
	    if(isRoot && (priority != null)) 
	      jreqs.setPriority(priority);

	    if(isRoot && (rampUp != null)) 
	      jreqs.setRampUp(rampUp);
	    
	    if(isRoot && (maxLoad != null)) 
	      jreqs.setMaxLoad(maxLoad);
	    
	    if(isRoot && (minMemory != null)) 
	      jreqs.setMinMemory(minMemory);
	    
	    if(isRoot && (minDisk != null)) 
	      jreqs.setMinDisk(minDisk);
	    
	    if(isRoot && (selectionKeys != null)) {
	      jreqs.removeAllSelectionKeys(); 
	      jreqs.addSelectionKeys(selectionKeys);
	    }
	    
	    if(isRoot && (hardwareKeys != null)) {
	      jreqs.removeAllHardwareKeys(); 
	      jreqs.addHardwareKeys(hardwareKeys);
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
		  NodeMod lmod = 
                    status.getSource(sname).getHeavyDetails().getWorkingVersion();
		  
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
	  
	  QueueJob job = new QueueJob(jobGroupID, agenda, action, jreqs, sourceIDs);

          if (first) {
            /* perform all server-side key calculations */
            TaskTimer subTimer = new TaskTimer("MasterMgr.adjustJobRequirements()");
            timer.suspend();
            try {
              ArrayList<String> keyExceptions = 
                adjustJobRequirements(subTimer, job.queryOnlyCopy(), jreqs, timeStamp,
                                      allSelectionKeys, allLicenseKeys, allHardwareKeys);
              job.setJobRequirements(jreqs);
              exceptions.addAll(keyExceptions);
            } 
            catch(PipelineException ex) {
              exceptions.add(ex.getMessage());
            }
            finally {
              timer.accum(subTimer);
            }
            first = false;
          }
                       
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
   * Change the given job requirements so that they are correct based on the
   * plugins that are contained in the selection, hardware, and license keys.
   * <p>
   * Note that this method will modifiy the job requirements that are passed in.  
   * If this is not desired behavior, a copy should be made of the 
   * job requirements before they are passed in.
   * 
   * @param timer
   *   An event time.
   * 
   * @param job
   *   The job that the requirements are being adjusted for.
   *
   * @param jreqs
   *   The current job requirements that are going to be modified.
   *   
   * @param updateTimestamp
   *   The time stamp to write into the job as its update time.
   * 
   * @param allSelectionKeys
   *   A cache of all currently defined selection keys.
   * 
   * @param allLicenseKeys
   *   A cache of all currently defined license keys.
   *
   * @param allHardwareKeys
   *   A cache of all currently defined hardware keys.
   *   
   * @return 
   *   A list of all the exceptions thrown during execution.
   */
  private ArrayList<String> 
  adjustJobRequirements
  (
    TaskTimer timer,
    QueueJob job,
    JobReqs jreqs, 
    long updateTimestamp,
    ArrayList<SelectionKey> allSelectionKeys, 
    ArrayList<LicenseKey> allLicenseKeys, 
    ArrayList<HardwareKey> allHardwareKeys
  )
    throws PipelineException
  {
    ArrayList<String> toReturn = new ArrayList<String>();
    
    /* lazily evaluate this only if necessary */
    TreeMap<String, BaseAnnotation> annots = null;
    NodeID nodeID = job.getNodeID();

    /* selection keys */
    {
      TreeSet<String> finalKeys = new TreeSet<String>();
      Set<String> currentKeys = jreqs.getSelectionKeys();

      for (SelectionKey key : allSelectionKeys) {
        String name = key.getName();
        if (!key.hasKeyChooser() && currentKeys.contains(name))
          finalKeys.add(name); 
        else if (key.hasKeyChooser()) {
          if (annots == null) {
            annots = getAnnotationsHelper(timer, nodeID.getName());
            if (annots == null)
              annots = new TreeMap<String, BaseAnnotation>();
          }
          try {
            if (key.getKeyChooser().computeIsActive(job, annots))
              finalKeys.add(name);
          }
          catch (Exception e) {
            toReturn.add(e.getMessage());
          }
        }
      }
      jreqs.removeAllSelectionKeys();
      jreqs.addSelectionKeys(finalKeys);
    }

    /* license keys */
    {
      TreeSet<String> finalKeys = new TreeSet<String>();
      Set<String> currentKeys = jreqs.getLicenseKeys();

      for (LicenseKey key : allLicenseKeys) {
        String name = key.getName();
        if (!key.hasKeyChooser() && currentKeys.contains(name))
          finalKeys.add(name); 
        else if (key.hasKeyChooser()) {
          if (annots == null) {
            annots = getAnnotationsHelper(timer, nodeID.getName());
            if (annots == null)
              annots = new TreeMap<String, BaseAnnotation>();
          }
          try {
            if (key.getKeyChooser().computeIsActive(job, annots))
              finalKeys.add(name);
          }
          catch (Exception e) {
            toReturn.add(e.getMessage());
          }
        }
      }
      jreqs.removeAllLicenseKeys();
      jreqs.addLicenseKeys(finalKeys);
    }

    /* hardware keys */
    {
      TreeSet<String> finalKeys = new TreeSet<String>();
      Set<String> currentKeys = jreqs.getHardwareKeys();

      for (HardwareKey key : allHardwareKeys) {
        String name = key.getName();
        if (!key.hasKeyChooser() && currentKeys.contains(name))
          finalKeys.add(name); 
        else if (key.hasKeyChooser()) {
          if (annots == null) {
            annots = getAnnotationsHelper(timer, nodeID.getName());
            if (annots == null)
              annots = new TreeMap<String, BaseAnnotation>();
          }
          try {
            if (key.getKeyChooser().computeIsActive(job, annots))
              finalKeys.add(name);
          }
          catch (Exception e) {
            toReturn.add(e.getMessage());
          }
        }
      }
      jreqs.removeAllHardwareKeys();
      jreqs.addHardwareKeys(finalKeys);
    }
    
    jreqs.setJobKeysUpdateTime(updateTimestamp);
    return toReturn;
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
   * @param jobGroupID
   *   The ID of the job group that will contain this jobs.
   * 
   * @param isRoot
   *   The this the root node of the job submission tree?
   *
   * @param timeStamp
   *   The timestamp of the key chooser updates to write into the job file.
   * 
   * @param allSelectionKeys
   *   A cache of all currently defined selection keys.
   * 
   * @param allLicenseKeys
   *   A cache of all currently defined license keys.
   *
   * @param allHardwareKeys
   *   A cache of all currently defined hardware keys.
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
   * @param assocRoots
   *   The names of nodes encountered on the upstream side of an Association link.
   *
   * @param timer
   *   The task timer.
   */
  private void 
  collectNoActionJobs
  (
   NodeStatus status, 
   Long jobGroupID,
   boolean isRoot, 
   long timeStamp,
   ArrayList<SelectionKey> allSelectionKeys, 
   ArrayList<LicenseKey> allLicenseKeys, 
   ArrayList<HardwareKey> allHardwareKeys,
   TreeMap<NodeID,Long[]> extJobIDs,   
   TreeMap<NodeID,Long[]> nodeJobIDs,   
   TreeMap<NodeID,TreeSet<Long>> upsJobIDs, 
   TreeSet<Long> rootJobIDs,    
   TreeMap<Long,QueueJob> jobs, 
   TreeSet<String> assocRoots,
   ArrayList<String> exceptions,
   TaskTimer timer 
  ) 
    throws PipelineException
  {
    NodeID nodeID = status.getNodeID();
    NodeDetailsHeavy details = status.getHeavyDetails();

    NodeMod work = details.getWorkingVersion();
    if(work == null) 
      throw new PipelineException
        ("Cannot generate jobs for the checked-in node (" + status + ")!");
    if(work.isLocked()) 
      return;

    /* check to see if we've already processes this node */ 
    if(upsJobIDs.containsKey(nodeID))
      return;

    /* add a new entry for this node */ 
    TreeSet<Long> jobIDs = new TreeSet<Long>();
    upsJobIDs.put(nodeID, jobIDs);
    
    /* submit and collect the IDs of the jobs associated with the upstream nodes, 
         but don't follow Association links */ 
    for(LinkMod link : work.getSources()) {
      switch(link.getPolicy()) {
      case Association:
        assocRoots.add(link.getName());
        break;

      case Reference:
      case Dependency:
        {
          NodeStatus lstatus = status.getSource(link.getName());
          NodeID lnodeID = lstatus.getNodeID();
          
          submitJobs(lstatus, jobGroupID, null, 
                     false, null, null, null, null, null, null, null, null, null,
                     timeStamp, allSelectionKeys, allLicenseKeys, allHardwareKeys, 
                     extJobIDs, nodeJobIDs, upsJobIDs, rootJobIDs, 
                     jobs, assocRoots, exceptions, timer);
           
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
      }
    }

    /* if this is the root node, make the collected jobs the root jobs */ 
    if(isRoot) 
      rootJobIDs.addAll(jobIDs);
  }

  /**
   * Search the upstream links of the current node for Associations and register the 
   * nodes on the upstream side of these links for further job processing.
   * 
   * @param status
   *   The current node status.
   * 
   * @param assocRoots
   *   The names of nodes encountered on the upstream side of an Association link.
   */ 
  private void 
  findJobAssocRoots
  (
   NodeStatus status, 
   TreeSet<String> assocRoots
  ) 
    throws PipelineException 
  {
    NodeMod work = status.getHeavyDetails().getWorkingVersion();
    if(work == null) 
      throw new PipelineException
	("Cannot generate jobs for the checked-in node (" + status + ")!");
    if(work.isLocked()) 
      return;

    for(LinkMod link : work.getSources()) {
      switch(link.getPolicy()) {
      case Association:
        assocRoots.add(link.getName());
        break;
        
      case Reference:
      case Dependency:
        findJobAssocRoots(status.getSource(link.getName()), assocRoots);
      }
    }
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
   long stamp, 
   FileSeq fseq
  )
    throws PipelineException 
  {
    ArrayList<Long> jobIDs = new ArrayList<Long>();
    ArrayList<JobState> jobStates = new ArrayList<JobState>();

    QueueMgrControlClient qclient = acquireQueueMgrClient();
    try {
      qclient.getJobStates(nodeID, stamp, fseq, jobIDs, jobStates);
    }
    finally {
      releaseQueueMgrClient(qclient);
    }

    int wk = 0;
    for(JobState state : jobStates) {
      Long jobID = jobIDs.get(wk);
      if((state != null) && (jobID != null)) {
	switch(state) {
	case Queued:     
	case Preempted:
	case Paused:
	case Running:
        case Limbo:
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
   long stamp, 
   FileSeq fseq
  )
    throws PipelineException 
  {
    QueueMgrControlClient qclient = acquireQueueMgrClient();
    try {
      ArrayList<Long> jobIDs = new ArrayList<Long>();
      ArrayList<JobState> jobStates = new ArrayList<JobState>();
      qclient.getJobStates(nodeID, stamp, fseq, jobIDs, jobStates);

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
        qclient.killJobs(activeIDs); 
    }
    finally {
      releaseQueueMgrClient(qclient);
    }
  }

    
  /*----------------------------------------------------------------------------------------*/
   
  /**
   * Vouch for the up-to-date status of the working area files associated with a node. <P>  
   * 
   * @param req 
   *   The submit jobs request.
   * 
   * @return 
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to kill the jobs.
   */ 
  public Object
  vouch
  (
   NodeVouchReq req
  ) 
  {
    NodeID nodeID = req.getNodeID();

    TaskTimer timer = new TaskTimer("MasterMgr.vouch(): " + nodeID);
   
    /* pre-op tests */
    VouchExtFactory factory = new VouchExtFactory(nodeID);
    try {
      performExtensionTests(timer, factory);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    LoggedLock lock = getWorkingLock(nodeID);
    lock.acquireWriteLock();
    try {
      timer.resume();
      
      if(!pAdminPrivileges.isQueueManaged(req, nodeID))
	throw new PipelineException
	  ("Only a user with Queue Manager privileges may vouch for files associated with " +
	   "nodes in working areas owned by another user!");

      /* get the working version */ 
      WorkingBundle bundle = getWorkingBundle(nodeID);
      NodeMod mod = new NodeMod(bundle.getVersion());
      if(mod.isFrozen()) 
	throw new PipelineException
	  ("You cannot vouch for a frozen node (" + nodeID + ")!"); 
      if(mod.isActionEnabled()) 
        throw new PipelineException
          ("You cannot vouch for a node (" + nodeID + ") with an enabled action!"); 
       
      /* touch the files */ 
      {
        FileMgrClient fclient = acquireFileMgrClient();
        try {
          fclient.touchAll(nodeID, mod);
        }
        finally {
          releaseFileMgrClient(fclient);
        }
      }
      
      /* write the new working version to disk */ 
      writeWorkingVersion(nodeID, mod);
      
      /* update the bundle */ 
      bundle.setVersion(mod);

      /* record event */ 
      pPendingEvents.add(new VouchedNodeEvent(nodeID));

      /* post-op tasks */ 
      startExtensionTasks(timer, factory);

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      lock.releaseWriteLock();
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	
      
      if(!pAdminPrivileges.isQueueManaged(req, nodeID))
	throw new PipelineException
	  ("Only a user with Queue Manager privileges may remove files associated with " +
	   "nodes in working areas owned by another user!");

      TreeSet<Long> activeIDs = new TreeSet<Long>();
      boolean hasLimbo = false;
      TreeSet<FileSeq> fseqs = new TreeSet<FileSeq>();
      
      timer.acquire();
      LoggedLock lock = getWorkingLock(nodeID);
      lock.acquireReadLock();
      try {
	timer.resume();
	
	WorkingBundle bundle = getWorkingBundle(nodeID);
	NodeMod mod = bundle.getVersion();
	if(mod.isFrozen()) 
	  throw new PipelineException
	    ("The files associated with frozen node (" + nodeID + ") cannot be removed!");
	
	ArrayList<Long> jobIDs = new ArrayList<Long>();
	ArrayList<JobState> jobStates = new ArrayList<JobState>();
        QueueMgrControlClient qclient = acquireQueueMgrClient();
        try {
          qclient.getJobStates(nodeID, mod.getTimeStamp(), mod.getPrimarySequence(),
                               jobIDs, jobStates);
        }
        finally {
          releaseQueueMgrClient(qclient);
        }
	
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
                break;

              case Limbo:
                hasLimbo = true;
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
                break;

              case Limbo:
                hasLimbo = true;
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
	lock.releaseReadLock();
      }    
      
      if(hasLimbo) 
        throw new PipelineException
          ("Some files associated with node (" + nodeID + ") cannot be removed because " + 
           "there are jobs in a Limbo state associated with these files!  You will need " + 
           "to either Enable or Terminate the job servers in Limbo where these jobs are " + 
           "running before the state of these jobs can be reliably determined."); 

      if(!activeIDs.isEmpty())  {
        QueueMgrControlClient qclient = acquireQueueMgrClient();
        try {
          qclient.killJobs(activeIDs); 
        }
        finally {
          releaseQueueMgrClient(qclient);
        }
      }

      {
	FileMgrClient fclient = acquireFileMgrClient();
	try {
	  fclient.removeAll(nodeID, fseqs);
	}
	finally {
          releaseFileMgrClient(fclient);
	}
      }

      /* clear the checksum caches for the removed files */ 
      {
        timer.acquire();
        LoggedLock clock = getCheckSumLock(nodeID);
        clock.acquireWriteLock();
        try {
          timer.resume();
          
          CheckSumBundle cbundle = getCheckSumBundle(nodeID);   
          cbundle.getCache().remove(fseqs); 
          writeCheckSumCache(cbundle.getCache()); 
        }
        finally {
          clock.releaseWriteLock();
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
      pDatabaseLock.releaseReadLock();
    }
  }


  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Whether the given working area contains nodes for which there are unfinished jobs 
   * currently in the queue.<P> 
   * 
   * A job is considered unfinished if it has as JobState of: Queued, Preempted, Paused, 
   * Running or Limbo.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to fulfill the request. 
   */ 
  public Object 
  hasUnfinishedJobs
  ( 
   NodeWorkingAreaPatternReq req 
  ) 
  {
    String author  = req.getAuthor();
    String view    = req.getView();
    String pattern = req.getPattern();

    TaskTimer timer =  
      new TaskTimer("MasterMgr.hasUnfinishedJobs(): " + author + "|" + view + " " + 
                    "[" + pattern + "]");

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	

      QueueMgrControlClient qclient = acquireQueueMgrClient();
      try {
	Pattern pat = null;
	if(pattern != null) 
	  pat = Pattern.compile(pattern);

        for(String name : pNodeTree.getMatchingWorkingNodes(author, view, pat)) {
          TreeMap<String,FileSeq> fseqs = new TreeMap<String,FileSeq>();
          {
            NodeID nodeID = new NodeID(author, view, name); 
            timer.acquire();
            LoggedLock lock = getWorkingLock(nodeID);
            lock.acquireReadLock();
            try {
              timer.resume();	
              
              fseqs.put(name, getWorkingBundle(nodeID).getVersion().getPrimarySequence());
            }
            catch(PipelineException ex) {
            }
            finally {
              lock.releaseReadLock();
            }  
          }
          
          /* this could be optimized with a new QueueMgrClient method which returns on the 
             first match of a unfinished job instead of building up the jobIDs... */ 
          if(!fseqs.isEmpty() && 
             !qclient.getUnfinishedJobsForNodes(author, view, fseqs).isEmpty())
            return new SimpleBooleanRsp(timer, true); 
        }
      }
      catch(PatternSyntaxException ex) {
	return new FailureRsp(timer, 
			      "Illegal Node Name Pattern:\n\n" + ex.getMessage());
      }
      finally {
        releaseQueueMgrClient(qclient);
      }

      return new SimpleBooleanRsp(timer, false); 
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }
  }

  /**
   * Get all unfinished jobs for the matching nodes contained in the given working area. <P> 
   * 
   * A job is considered unfinished if it has as JobState of: Queued, Preempted, Paused, 
   * Running or Limbo.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to fulfill the request. 
   */ 
  public Object 
  getUnfinishedJobs
  ( 
   NodeWorkingAreaPatternReq req 
  ) 
  {
    String author  = req.getAuthor();
    String view    = req.getView();
    String pattern = req.getPattern();

    TaskTimer timer =  
      new TaskTimer("MasterMgr.getUnfinishedJobs(): " + author + "|" + view + 
                    "[" + pattern + "]");

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	

      MappedSet<String,Long> jobIDs = new MappedSet<String,Long>();

      QueueMgrControlClient qclient = acquireQueueMgrClient();
      try {
	Pattern pat = null;
	if(pattern != null) 
	  pat = Pattern.compile(pattern);
        
        for(String name : pNodeTree.getMatchingWorkingNodes(author, view, pat)) {
          TreeMap<String,FileSeq> fseqs = new TreeMap<String,FileSeq>();
          {
            NodeID nodeID = new NodeID(author, view, name); 
            timer.acquire();
            LoggedLock lock = getWorkingLock(nodeID);
            lock.acquireReadLock();
            try {
              timer.resume();	
              
              fseqs.put(name, getWorkingBundle(nodeID).getVersion().getPrimarySequence());
            }
            catch(PipelineException ex) {
            }
            finally {
              lock.releaseReadLock();
            }  
          }
          
          if(!fseqs.isEmpty()) 
            jobIDs.putAll(qclient.getUnfinishedJobsForNodes(author, view, fseqs));
        }
      }
      catch(PatternSyntaxException ex) {
	return new FailureRsp(timer, 
			      "Illegal Node Name Pattern:\n\n" + ex.getMessage());
      }
      finally {
        releaseQueueMgrClient(qclient);
      }

      return new QueueGetUnfinishedJobsForNodesRsp(timer, jobIDs);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      pDatabaseLock.releaseReadLock();
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A D M I N I S T R A T I O N                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a set of database backup files. <P> 
   * 
   * The backup will not be perfomed until any currently running database operations have 
   * completed.  Once the databsae backup has begun, all new database operations will blocked
   * until the backup is complete.  The this reason, the backup should be performed during 
   * non-peak hours. <P> 
   * 
   * The database backup files will be automatically named: <P> 
   * <DIV style="margin-left: 40px;">
   *   plmaster-db.<I>YYMMDD</I>.<I>HHMMSS</I>.tar<P>
   *   plqueuemgr-db.<I>YYMMDD</I>.<I>HHMMSS</I>.tar<P>
   *   plpluginmgr-db.<I>YYMMDD</I>.<I>HHMMSS</I>.tar<P>
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
    TaskTimer timer = new TaskTimer("Initiating Backup"); 

    Path targetDir = req.getBackupDirectory();

    SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd.HHmmss");
    String dateStr = format.format(new Date());
    Path target = new Path(targetDir, "plmaster-db." + dateStr + ".tar"); 

    /* pre-op tests */
    BackupDatabaseExtFactory factory = new BackupDatabaseExtFactory(target.toFile());
    try {
      performExtensionTests(timer, factory);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }

    try {
      if(!pAdminPrivileges.isMasterAdmin(req))
        throw new PipelineException
          ("Only a user with Master Admin privileges may backup the database!"); 

      if(!targetDir.toFile().isDirectory()) 
        throw new PipelineException
          ("The backup directory (" + targetDir + ") is not valid!");

      pBackupSyncTarget.set(target); 
      pBackupSyncTrigger.release();

      if(req.withQueueMgr()) {
        QueueMgrControlClient qclient = acquireQueueMgrClient();
        try {
          qclient.backupDatabase(targetDir, dateStr);
        }
        finally {
          releaseQueueMgrClient(qclient);
        }
      }

      if(req.withPluginMgr()) {
        PluginMgrControlClient pclient = new PluginMgrControlClient();
        try {
          pclient.backupDatabase(targetDir, dateStr);
        }
        finally {
          pclient.disconnect();
        }
      }

      /* post-op tasks */ 
      startExtensionTasks(timer, factory);

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
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
   * @param opn
   *   The operation progress notifier.
   * 
   * @param sessionID 
   *   The unique network connection session ID.
   * 
   * @return 
   *   <CODE>MiscArchiveQueryRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to perform the query.
   */
  public Object
  archiveQuery
  (
   MiscArchiveQueryReq req, 
   OpNotifiable opn,
   long sessionID
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	

      if(!isOfflineCacheValid()) 
        throw new PipelineException 
          ("The ArchiveQuery operation will not be available until the offlined node " +
           "version cache has finished being rebuilt.");

      String pattern      = req.getPattern();
      Integer maxArchives = req.getMaxArchives();
      
      if((maxArchives != null) && (maxArchives < 1)) 
	throw new PipelineException
	  ("The maximum number of archive volumes containing the checked-in version " +
	   "(" + maxArchives + ") must be positive!");

      opn.notify(timer, "Pattern Search..."); 

      /* get the node names which match the pattern */ 
      TreeSet<String> matches = null;
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
      
      /* set the number of individual nodes to be queried */ 
      opn.setTotalSteps(matches.size());

      /* process the matching nodes */ 
      long cnt = 0L;
      ArrayList<ArchiveInfo> archiveInfo = new ArrayList<ArchiveInfo>();
      for(String name : matches) {
	  
        /* lock online/offline status */ 
        timer.acquire();
        LoggedLock onOffLock = getOnlineOfflineLock(name);
        onOffLock.acquireReadLock();
        try {
          timer.resume();	

	  /* get the revision numbers and creation timestamps of the included versions, 
               excluding all versions which are intermediate */ 
	  TreeMap<VersionID,Long> stamps = new TreeMap<VersionID,Long>();
	  {
	    timer.acquire();
	    LoggedLock lock = getCheckedInLock(name);
	    lock.acquireReadLock();  
	    try {
	      timer.resume();	
	      TreeMap<VersionID,CheckedInBundle> checkedIn = 
                getCheckedInBundles(name, false);
              
              for(Map.Entry<VersionID,CheckedInBundle> entry : checkedIn.entrySet()) {
                NodeVersion vsn = entry.getValue().getVersion();
                if(!vsn.isIntermediate()) 
                  stamps.put(entry.getKey(), vsn.getTimeStamp()); 
              }
	    }
	    catch(PipelineException ex) {
	      return new FailureRsp(timer, "Internal Error: " + ex.getMessage());
	    }
	    finally {
	      lock.releaseReadLock();  
	    }
	  }
	
	  /* process the matching checked-in versions */ 
	  for(VersionID vid : stamps.keySet()) {
	    
	    /* check whether the version is currently online */ 	    
	    if(!isOffline(timer, name, vid)) {
	      /* get the number of archives which already contain the checked-in version */ 
	      int numArchives = 0;
	      String lastArchive = null;
	      {
		timer.acquire();
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
		Long archived = null;
		if(lastArchive != null) {
		  timer.acquire();
		  synchronized(pArchivedOn) {
		    timer.resume();
		    archived = pArchivedOn.get(lastArchive);
		  }
		}
		
		Long checkedIn = stamps.get(vid);
		ArchiveInfo info = 
		  new ArchiveInfo(name, vid, checkedIn, archived, numArchives);
		archiveInfo.add(info);
	      }
	    }
	  }
	}
        finally {
          onOffLock.releaseReadLock();
        }
  
        long batch = 10L;
        if((cnt % batch) == 0L) {
          opn.steps(timer, "Queried: " + name, batch); 
          checkForCancellation(sessionID);
        }
        cnt++;
      }

      return new MiscArchiveQueryRsp(timer, archiveInfo);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }  
    finally {
      pDatabaseLock.releaseReadLock();
    }  
  }

  /**
   * Calculate the total size (in bytes) of the files associated with the given 
   * checked-in versions for archival purposes. <P> 
   * 
   * @param req
   *   The file sizes request.
   * 
   * @param opn
   *   The operation progress notifier.
   * 
   * @param sessionID 
   *   The unique network connection session ID.
   * 
   * @return
   *   <CODE>MiscGetArchiveSizesRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the file sizes.
   */ 
  public Object
  getArchivedSizes
  (
   MiscGetSizesReq req, 
   OpNotifiable opn,
   long sessionID
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	

      /* set the number of nodes to be processed */ 
      opn.setTotalSteps(req.getVersions().size());      

      /* process the nodes */ 
      DoubleMap<String,VersionID,Long> sizes = new DoubleMap<String,VersionID,Long>();
      for(Map.Entry<String,TreeSet<VersionID>> entry : req.getVersions().entrySet()) {
        String name = entry.getKey();
        TreeSet<VersionID> versions = entry.getValue(); 

        /* compute the sizes of the files */ 
        {
          /* lock online/offline status */ 
          timer.acquire();
          LoggedLock onOffLock = getOnlineOfflineLock(name);
          onOffLock.acquireReadLock();
          try {
            timer.resume();	

            /* get the file sequences for each checked-in version of the node */ 
            MappedSet<VersionID,FileSeq> vfseqs = new MappedSet<VersionID,FileSeq>();
            { 
              timer.acquire();
              LoggedLock lock = getCheckedInLock(name);
              lock.acquireReadLock(); 
              try {
                timer.resume();
                
                TreeMap<VersionID,CheckedInBundle> checkedIn = 
                  getCheckedInBundles(name, false);

                for(VersionID vid : versions) {
                  CheckedInBundle bundle = checkedIn.get(vid);
                  if(bundle != null) {
                    NodeVersion vsn = bundle.getVersion();
                    if(!vsn.isIntermediate()) 
                      vfseqs.put(vid, vsn.getSequences());
                  }
                }
              }
              finally {
                lock.releaseReadLock();
              }
            }
            
            /* compute the sizes of the files */ 
            {
              FileMgrClient fclient = acquireFileMgrClient();
              try {
                sizes.put(name, fclient.getArchiveSizes(name, vfseqs)); 
              }
              finally {
                releaseFileMgrClient(fclient);
              }
            }
          }
          finally {
            onOffLock.releaseReadLock();
          }
        }

        opn.step(timer, "Sized: " + name); 
        checkForCancellation(sessionID);
      }

      return new MiscGetSizesRsp(timer, sizes);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }  
    finally {
      pDatabaseLock.releaseReadLock();
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
   * @param opn
   *   The operation progress notifier.
   * 
   * @param sessionID 
   *   The unique network connection session ID.
   * 
   * @return 
   *   <CODE>MiscArchiveRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to archive the files.
   */
  public Object
  archive
  (
   MiscArchiveReq req, 
   OpNotifiable opn,
   long sessionID
  ) 
  {
    TaskTimer timer = new TaskTimer();

    /* make sure the file manager can archive files */ 
    {
      FileMgrClient fclient = acquireFileMgrClient();
      try {
        fclient.validateScratchDir();
      }
      catch(PipelineException ex) {
        return new FailureRsp(timer, ex.getMessage());
      }  
      finally {
        releaseFileMgrClient(fclient);
      }
    }

    if(!pAdminPrivileges.isMasterAdmin(req))
      return new FailureRsp
        (timer, "Only a user with Master Admin privileges may create archives of " + 
         "checked-in versions!"); 

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	

      if(!isOfflineCacheValid()) 
        throw new PipelineException 
          ("The Archive operation will not be available until the offlined node " +
           "version cache has finished being rebuilt.");

      opn.notify(timer, "Validating Nodes..."); 

      /* the archiver plugin to use */ 
      BaseArchiver archiver = req.getArchiver();

      /* the checked-in node versions */ 
      MappedSet<String,VersionID> versions = req.getVersions();

      /* lock online/offline status of the nodes to be archived */ 
      timer.acquire();
      List<LoggedLock> onOffLocks = onlineOfflineReadLock(versions.keySet());
      try {
	timer.resume();	

	/* validate the file sequences to be archived */ 
	TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>> fseqs = 
	  new TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>>();
	DoubleMap<String,VersionID,Long> sizes = null;
	{
	  /* recheck the sizes of the versions */ 
	  {
	    Object obj = getArchivedSizes(new MiscGetSizesReq(versions), opn, sessionID);
	    if(obj instanceof FailureRsp) {
	      FailureRsp rsp = (FailureRsp) obj;
	      throw new PipelineException(rsp.getMessage());	
	    }
	    else {
	      MiscGetSizesRsp rsp = (MiscGetSizesRsp) obj;
	      sizes = rsp.getSizes();
	    }
	  }
	  
	  /* make sure the versions exist, are not offline and are not intermediate */ 
	  long total = 0L;
	  for(String name : versions.keySet()) {

            checkForCancellation(sessionID);

	    timer.acquire();
	    LoggedLock lock = getCheckedInLock(name);
	    lock.acquireReadLock(); 
	    try {
	      timer.resume();

	      TreeMap<VersionID,CheckedInBundle> checkedIn = 
                getCheckedInBundles(name, false);

              TreeSet<VersionID> offline = getOfflinedVersions(timer, name); 
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
		
                if(bundle.getVersion().isIntermediate()) 
                  throw new PipelineException
                    ("The checked-in version (" + vid + ") of node (" + name + ") " + 
                     "cannot be archived because it is marked as having Intermediate " + 
                     "Files which are never stored in the repository!"); 
                
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
	    finally {
	      lock.releaseReadLock();
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
          timer.acquire();
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
	long stamp = System.currentTimeMillis();
	String archiveName = (req.getPrefix() + "-" + stamp);
	synchronized(pArchivedOn) {
	  if(pArchivedOn.containsKey(archiveName)) 
	    throw new PipelineException 
	      ("Somehow an archive named (" + archiveName + ") already exists!");
	}
	
	/* pre-op tests */
	ArchiveExtFactory factory = 
	  new ArchiveExtFactory(req.getRequestor(), archiveName, versions, archiver, tname);
	try {
	  performExtensionTests(timer, factory);
	}
	catch(PipelineException ex) {
	  return new FailureRsp(timer, ex.getMessage());
	}

        checkForCancellation(sessionID);

	/* create the archive volume by runing the archiver plugin and save any 
	   STDOUT output */
	{
	  String output = null;
	  {
	    FileMgrClient fclient = acquireFileMgrClient();
            long monitorID = fclient.addMonitor(new RelayOpMonitor(opn));
	    try {
              StringBuilder dryRunResults = null;
              if(req.isDryRun()) 
                dryRunResults = new StringBuilder();
              
	      output = fclient.archive(archiveName, fseqs, archiver, env, dryRunResults); 

              if(dryRunResults != null) 
                return new MiscArchiveRsp(timer, archiveName, dryRunResults.toString()); 
	    }
	    finally {
              fclient.removeMonitor(monitorID); 
	      releaseFileMgrClient(fclient);
	    }
	  }
	  
	  if(output != null) {
	    File file = new File(pNodeDir, "archives/output/archive/" + archiveName);
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

	return new MiscArchiveRsp(timer, archiveName, null);
      }
      finally {
	onlineOfflineReadUnlock(onOffLocks);
      }  
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }  
    finally {
      pDatabaseLock.releaseReadLock();
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
   * @param opn
   *   The operation progress notifier.
   * 
   * @param sessionID 
   *   The unique network connection session ID.
   * 
   * @return 
   *   <CODE>MiscOfflineQueryRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to perform the query.
   */
  public Object
  offlineQuery
  (
   MiscOfflineQueryReq req, 
   OpNotifiable opn, 
   long sessionID
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	

      if(!isOfflineCacheValid()) 
        throw new PipelineException 
          ("The OfflineQuery operation will not be available until the offlined node " +
           "version cache has finished being rebuilt.");

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

      opn.notify(timer, "Pattern Search..."); 

      /* get the node names which match the pattern */ 
      TreeSet<String> matches = null;
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
      
      /* set the number of individual nodes to be queried */ 
      opn.setTotalSteps(matches.size());

      /* lock online/offline status */ 
      timer.acquire();
      List<LoggedLock> onOffLocks = onlineOfflineReadLock(matches);
      try {
	timer.resume();	

	/* process the matching nodes */ 
        long cnt = 0L;
	ArrayList<OfflineInfo> offlineInfo = new ArrayList<OfflineInfo>();
	VersionID latestID = null;
	for(String name : matches) {
	  
	  /* get the revision numbers of the included versions */ 
	  TreeSet<VersionID> vids = new TreeSet<VersionID>();
	  {	    
	    timer.acquire();
	    LoggedLock lock = getCheckedInLock(name);
	    lock.acquireReadLock();  
	    try {
	      timer.resume();	
	      TreeMap<VersionID,CheckedInBundle> checkedIn = 
                getCheckedInBundles(name, false);

	      int wk = 0;
	      for(VersionID vid : checkedIn.keySet()) {
		if((exclude != null) && (wk >= (checkedIn.size() - exclude)))
		  break;
		
                /* ignore intermediate versions */ 
                if(!checkedIn.get(vid).getVersion().isIntermediate()) 
                  vids.add(vid);

		wk++;
	      }
	      
	      latestID = checkedIn.lastKey();
	    }
	    catch(PipelineException ex) {
	      return new FailureRsp(timer, "Internal Error: " + ex.getMessage());
	    }
	    finally {
	      lock.releaseReadLock();  
	    }
	  }

	  /* remove any already offline versions */ 
	  synchronized(pOfflinedLock)	{
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
	      timer.acquire();
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
	      Long archived = null;
	      if(lastArchive != null) {
		timer.acquire();
		synchronized(pArchivedOn) {
		  timer.resume();
		  archived = pArchivedOn.get(lastArchive);
		}
	      }
	      
	      /* get the number of working versions based on the checked-in version and 
		 the timestamp of when the latest working version was checked-out */ 
	      int numWorking = 0;
              Long checkedOut = null;
	      String lastAuthor = null;
	      String lastView = null;
	      boolean canOffline = true;
	      {	      
		TreeMap<String,TreeSet<String>> areas = pNodeTree.getViewsContaining(name);
		for(String author : areas.keySet()) {
		  TreeSet<String> views = areas.get(author);
		  for(String view : views) {
		    NodeID nodeID = new NodeID(author, view, name);
		    
		    timer.acquire();
		    LoggedLock lock = getWorkingLock(nodeID);
		    lock.acquireReadLock();
		    try {
		      timer.resume();	
		      
		      WorkingBundle bundle = getWorkingBundle(nodeID, false);
		      NodeMod mod = bundle.getVersion();
		      if(vid.equals(mod.getWorkingID())) {
			if((checkedOut == null) || (checkedOut < mod.getTimeStamp())) {
			  checkedOut = mod.getTimeStamp();
			  lastAuthor = author;
			  lastView   = view; 
			}
			
			canOffline = false;
			numWorking++;
		      }		      
		    } 
		    finally {
		      lock.releaseReadLock();
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

          long batch = 10L;
          if((cnt % batch) == 0L) {
            opn.steps(timer, "Queried: " + name, batch); 
            checkForCancellation(sessionID);
          }
          cnt++;
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
      pDatabaseLock.releaseReadLock();
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
   NodeGetByNameReq req
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	

      String name = req.getName();

      /* lock online/offline status of the node */ 
      timer.acquire();
      LoggedLock onOffLock = getOnlineOfflineLock(name);
      onOffLock.acquireReadLock();
      try {
	timer.resume();	

        TreeSet<VersionID> offlined = getOfflinedVersions(timer, name);
        if(offlined == null) 
          offlined = new TreeSet<VersionID>();

	return new NodeGetVersionIDsRsp(timer, offlined);
      }
      finally {
	onOffLock.releaseReadLock();
      }      
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }  
    finally {
      pDatabaseLock.releaseReadLock();
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
  getMultiOfflineVersionIDs
  (
   NodeGetByNamesReq req
  ) 
  {
    TaskTimer timer = new TaskTimer();

    TreeSet<String> names = req.getNames();

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	

      MappedSet<String,VersionID> offlined = new MappedSet<String,VersionID>();
      for(String name : names) {
        timer.acquire();
        LoggedLock onOffLock = getOnlineOfflineLock(name);
        onOffLock.acquireReadLock();
        try {
          timer.resume();	
          
          TreeSet<VersionID> offld = getOfflinedVersions(timer, name);
          if(offld == null) 
            offld = new TreeSet<VersionID>();
          
          offlined.put(name, offld); 
        }
        catch(PipelineException ex) {
          // silently ignore missing nodes
        }
        finally {
          onOffLock.releaseReadLock();
        }  
      }
      
      return new NodeGetMultiVersionIDsRsp(timer, offlined);
    }    
    finally {
      pDatabaseLock.releaseReadLock();
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
   * @param opn
   *   The operation progress notifier.
   * 
   * @param sessionID 
   *   The unique network connection session ID.
   * 
   * @return
   *   <CODE>MiscGetOfflineSizesRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the file sizes.
   */ 
  public Object
  getOfflineSizes
  (
   MiscGetSizesReq req, 
   OpNotifiable opn,
   long sessionID
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	

      if(!isOfflineCacheValid()) 
        throw new PipelineException 
          ("The GetOfflineSizes operation will not be available until the offlined node " +
           "version cache has finished being rebuilt.");

      /* set the number of nodes to be processed */ 
      opn.setTotalSteps(req.getVersions().size());    

      /* process the nodes */ 
      DoubleMap<String,VersionID,Long> sizes = new DoubleMap<String,VersionID,Long>();
      for(Map.Entry<String,TreeSet<VersionID>> entry : req.getVersions().entrySet()) {
        String name = entry.getKey();
        TreeSet<VersionID> toBeOfflined = entry.getValue(); 

        /* lock online/offline status */ 
        timer.acquire();
        LoggedLock onOffLock = getOnlineOfflineLock(name);
        onOffLock.acquireReadLock();
        try {
          timer.resume();	

          /* the currently offline revision numbers */ 
          TreeSet<VersionID> offlined = getOfflinedVersions(timer, name);
          if(offlined == null) 
            offlined = new TreeSet<VersionID>();

          /* determine which files contribute the to offlined size */ 
          MappedSet<VersionID,File> contribute = new MappedSet<VersionID,File>();
          {
            timer.acquire();
            LoggedLock lock = getCheckedInLock(name);
            lock.acquireReadLock(); 
            try {
              timer.resume();
                
              TreeMap<VersionID,CheckedInBundle> checkedIn = 
                getCheckedInBundles(name, false);

              ArrayList<VersionID> vids = new ArrayList<VersionID>(checkedIn.keySet());
                
              /* process the to be offlined versions */ 
              for(VersionID vid : toBeOfflined) {
                  
                /* ignore currently offline versions */ 
                if(!offlined.contains(vid)) {
                  CheckedInBundle bundle = checkedIn.get(vid);
                  if(bundle == null) 
                    throw new PipelineException 
                      ("No checked-in version (" + vid + ") of node (" + name + ") exists!");
                    
                  /* ignore intermediate versions */ 
                  if(!bundle.getVersion().isIntermediate()) {
                      
                    /* determine which files contributes to the offlined size */ 
                    TreeMap<File,Boolean[]> novelty = noveltyByFile(checkedIn);
                    for(File file : novelty.keySet()) {
                        
                      /* we are only concerned with files exist and are new */ 
                      Boolean[] isNovel = novelty.get(file);
                      int vidx = vids.indexOf(vid);
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
                            else if(!toBeOfflined.contains(nvid) && 
                                    !offlined.contains(nvid)) {
                              selected = false;
                              break;
                            }
                          }
                        }
                          
                        /* add the current file to those which contribute */ 
                        if(selected) 
                          contribute.put(vid, file);
                      }
                    }
                  }
                }
              }
            }
            finally {
              lock.releaseReadLock();
            }
          }

          /* compute the sizes of the files */
          {
            FileMgrClient fclient = acquireFileMgrClient();
            try {
              sizes.put(name, fclient.getOfflineSizes(name, contribute));
            }
            finally {
              releaseFileMgrClient(fclient);
            }
          }
        }
        finally {
          onOffLock.releaseReadLock();
        }

        opn.step(timer, "Sized: " + name);
        checkForCancellation(sessionID);
      }

      return new MiscGetSizesRsp(timer, sizes);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }  
    finally {
      pDatabaseLock.releaseReadLock();
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
   * @param opn
   *   The operation progress notifier.
   * 
   * @param sessionID 
   *   The unique network connection session ID.
   * 
   * @return 
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to remove the files.
   */
  public Object
  offline
  (
   MiscOfflineReq req, 
   OpNotifiable opn,
   long sessionID
  ) 
  {
    MappedSet<String,VersionID> versions = req.getVersions();
      
    TaskTimer timer = new TaskTimer("MasterMgr.offline()");
    
    /* pre-op tests */
    OfflineExtFactory factory = new OfflineExtFactory(req.getRequestor(), versions);
    try {
      performExtensionTests(timer, factory);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }

    if(!pAdminPrivileges.isMasterAdmin(req))
      return new FailureRsp
        (timer, "Only a user with Master Admin privileges may offline checked-in versions!"); 
  
    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      boolean cacheModified = false;
      try {
        timer.resume();	

        if(!isOfflineCacheValid()) 
          throw new PipelineException 
            ("The Offline operation will not be available until the offlined node " +
             "version cache has finished being rebuilt.");

        /* set the number of node versions to be processed */ 
        {
          long steps = 0L;
          for(TreeSet<VersionID> vids : versions.values()) 
            steps += vids.size();
          opn.setTotalSteps(steps);    
        }

        /* write lock online/offline status */ 
        timer.acquire();
        List<LoggedLock> onOffLocks = onlineOfflineWriteLock(versions.keySet());
        try {
          timer.resume();	
        
          StringBuilder dryRunResults = null;
          if(req.isDryRun()) 
            dryRunResults = new StringBuilder();

          /* process each node */ 
          for(String name : versions.keySet()) {	
            timer.acquire();

            ArrayList<LoggedLock> workingLocks = 
              new ArrayList<LoggedLock>();
            {
              TreeMap<String,TreeSet<String>> views = pNodeTree.getViewsContaining(name);
              for(String author : views.keySet()) {
                for(String view : views.get(author)) {
                  NodeID nodeID = new NodeID(author, view, name);
                  LoggedLock workingLock = getWorkingLock(nodeID);
                  workingLock.acquireReadLock();
                  workingLocks.add(workingLock);
                }
              }
            }

            LoggedLock checkedInLock = getCheckedInLock(name);
            checkedInLock.acquireReadLock();  
            try {
              timer.resume();	
              TreeMap<VersionID,CheckedInBundle> checkedIn = 
                getCheckedInBundles(name, false);
	    
              ArrayList<VersionID> vids = new ArrayList<VersionID>(checkedIn.keySet());
              TreeMap<File,Boolean[]> novelty = noveltyByFile(checkedIn);

              /* process the to be offlined versions */ 
              TreeSet<VersionID> toBeOfflined = versions.get(name);
              for(VersionID vid : toBeOfflined) { 

                /* only process online versions */
                if(!isOffline(timer, name, vid)) {
                  CheckedInBundle bundle = checkedIn.get(vid);
                  if(bundle == null) 
                    throw new PipelineException 
                      ("No checked-in version (" + vid + ") of node (" + name + ") exists!");
                  int vidx = vids.indexOf(vid);
	    
                  if(bundle.getVersion().isIntermediate()) 
                    throw new PipelineException
                      ("The checked-in version (" + vid + ") of node (" + name + ") " + 
                       "cannot be offlined because it is marked as having Intermediate " + 
                       "Files which are never stored in the repository!"); 

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
                    TreeMap<String,TreeSet<String>> views = 
                      pNodeTree.getViewsContaining(name);
                    for(String author : views.keySet()) {
                      for(String view : views.get(author)) {
                        NodeID nodeID = new NodeID(author, view, name);
                        NodeMod mod = getWorkingBundle(nodeID, false).getVersion();
                        if(vid.equals(mod.getWorkingID())) 
                          throw new PipelineException
                            ("The checked-in version (" + vid + ") of node (" + name + ") " +
                             "cannot be offlined because a working version currently " + 
                             "exists which references the checked-in version in the " + 
                             "working area (" + view + ") owned by user (" + author + ")!");
		  
                        if(vid.equals(checkedIn.lastKey())) 
                          throw new PipelineException
                            ("The latest checked-in version (" + vid + ") of node " + 
                             "(" + name + ") cannot be offlined because a working version " + 
                             "currently exists in the working area (" + view + ") owned " + 
                             "by user (" + author + ")!");
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
                            if(isOffline(timer, name, nvid)) {
                              selected = true;
                              break;
                            }
                            else if(isNovel[vk]) {
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
                            else if(!isOffline(timer, name, nvid)) {
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
                    FileMgrClient fclient = acquireFileMgrClient();
                    try {
                      fclient.offline(name, vid, symlinks, dryRunResults);
                    }
                    finally {
                      releaseFileMgrClient(fclient);
                    }
                  }

                  if(!req.isDryRun()) {
                    /* update the currently offlined revision numbers */ 
                    synchronized(pOfflinedLock) {
                      TreeSet<VersionID> offlined = pOfflined.get(name);
                      if(offlined == null) {
                        offlined = new TreeSet<VersionID>();
                        pOfflined.put(name, offlined);
                      }
                    
                      offlined.add(vid);
                    }
                  }
                }

                opn.step(timer, "Offlined: " + name + " (v" + vid + ")"); 
                checkForCancellation(sessionID);
              }
            }
            finally {
              checkedInLock.releaseReadLock();  

              Collections.reverse(workingLocks);
              for(LoggedLock workingLock : workingLocks) 
                workingLock.releaseReadLock();
            }
          }
        
          if(dryRunResults != null) 
            return new DryRunRsp(timer, dryRunResults.toString()); 

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
        /* keep the offlined cache file up-to-date */ 
        if(cacheModified && pPreserveOfflinedCache) {
          try {
            writeOfflined();
          }
          catch(PipelineException ex) {
            LogMgr.getInstance().logAndFlush
              (LogMgr.Kind.Ops, LogMgr.Level.Warning,
               ex.getMessage());
	  
            removeOfflinedCache();
          }
        }
      }
    }
    finally {
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	

      if(!isOfflineCacheValid()) 
        throw new PipelineException 
          ("The RestoreQuery operation will not be available until the offlined node " +
           "version cache has finished being rebuilt.");

      String pattern = req.getPattern();

      /* get versions which match the pattern */ 
      TreeMap<String,TreeSet<VersionID>> versions = new TreeMap<String,TreeSet<VersionID>>();
      {
        Pattern pat = null;
        if(pattern != null) 
          pat = Pattern.compile(pattern);
        
	timer.acquire();
	synchronized(pOfflinedLock) {
	  try {
	    timer.resume();
	    
            for(Map.Entry<String,TreeSet<VersionID>> entry : pOfflined.entrySet()) {
              String name = entry.getKey();
              if((pat == null) || pat.matcher(name).matches()) 
                versions.put(name, new TreeSet<VersionID>(entry.getValue()));
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
      pDatabaseLock.releaseReadLock();
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
    RequestRestoreExtFactory factory = 
      new RequestRestoreExtFactory(req.getRequestor(), versions);
    try {
      performExtensionTests(timer, factory);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }    

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      try {
        timer.resume();

        /* filter any versions which are not offline */ 
        TreeMap<String,TreeSet<VersionID>> vsns = new TreeMap<String,TreeSet<VersionID>>();
        {
          timer.acquire();
          List<LoggedLock> onOffLocks = onlineOfflineReadLock(versions.keySet());
          try {
            timer.resume();

            for(String name : versions.keySet()) {
              TreeSet<VersionID> offline = getOfflinedVersions(timer, name); 
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
          catch(PipelineException ex) {
            return new FailureRsp(timer, ex.getMessage());
          }  
          finally {
            onlineOfflineReadUnlock(onOffLocks);
          }
        }

        /* add the requests, replacing any current requests for the same versions */ 
        timer.acquire();
        synchronized(pRestoreReqs) {
          timer.resume();

          long now = System.currentTimeMillis();

          for(String name : vsns.keySet()) {
            TreeMap<VersionID,RestoreRequest> restores = pRestoreReqs.get(name);
            for(VersionID vid : vsns.get(name)) {
              RestoreRequest rr = new RestoreRequest(now, req.getRequestor());
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
          LogMgr.getInstance().logAndFlush
            (LogMgr.Kind.Net, LogMgr.Level.Warning,
             ex.getMessage());
        }
      }
    }
    finally {
      pDatabaseLock.releaseReadLock();
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
    DenyRestoreExtFactory factory = 
      new DenyRestoreExtFactory(req.getRequestor(), versions);
    try {
      performExtensionTests(timer, factory);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }    

    if(!pAdminPrivileges.isMasterAdmin(req))
      return new FailureRsp
	(timer, "Only a user with Master Admin privileges may deny restore requests!");

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
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
          LogMgr.getInstance().logAndFlush
            (LogMgr.Kind.Net, LogMgr.Level.Warning,
             ex.getMessage());
        }
      }
    }
    finally {
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
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
	      if((rr.getResolvedStamp() + pRestoreCleanupInterval.get()) < now) {
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
      pDatabaseLock.releaseReadLock();
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
   MiscGetSizesReq req
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();	
      
      MappedSet<String,VersionID> versions = req.getVersions();

      /* get the names of all archives which contain any of the given versions */ 
      TreeSet<String> anames = new TreeSet<String>();
      {
	timer.acquire();
	synchronized(pArchivedIn) {
	  timer.resume();

          for(Map.Entry<String,TreeSet<VersionID>> entry : versions.entrySet()) {
            String name = entry.getKey();

	    TreeMap<VersionID,TreeSet<String>> varchives = pArchivedIn.get(name);
	    if(varchives == null) 
	      throw new PipelineException 
		("Cannot compute restored size of checked-in node (" + name + ") " + 
		 "since no versions have ever been archived!");

	    for(VersionID vid : entry.getValue()) {
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
      DoubleMap<String,VersionID,Long> sizes = new DoubleMap<String,VersionID,Long>();
      {
	ArrayList<String> names   = new ArrayList<String>();
	ArrayList<VersionID> vids = new ArrayList<VersionID>();
        for(Map.Entry<String,TreeSet<VersionID>> entry : versions.entrySet()) {
          String name = entry.getKey();
          for(VersionID vid : entry.getValue()) {
	    names.add(name);
	    vids.add(vid);
	  }
	}
	
	for(String aname : anames) {
	  ArchiveVolume volume = readArchive(aname);
	  
	  ArrayList<String> rnames   = new ArrayList<String>();
	  ArrayList<VersionID> rvids = new ArrayList<VersionID>();

	  int wk;
	  for(wk=0; wk<names.size(); wk++) {
	    String name = names.get(wk);
	    VersionID vid = vids.get(wk);

	    if(volume.contains(name, vid)) {
	      long size = volume.getSize(name, vid);
              sizes.put(name, vid, size);
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
      pDatabaseLock.releaseReadLock();
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
   * @param opn
   *   The operation progress notifier.
   * 
   * @param sessionID 
   *   The unique network connection session ID.
   * 
   * @return 
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to find the archive.
   */
  public Object
  restore
  (
   MiscRestoreReq req, 
   OpNotifiable opn,
   long sessionID
  ) 
  {
    long stamp = System.currentTimeMillis();
    String archiveName = req.getName();
    TreeMap<String,TreeSet<VersionID>> versions = req.getVersions();
    BaseArchiver archiver = req.getArchiver();

    TaskTimer timer = new TaskTimer("MasterMgr.restore(): " + archiveName);

    /* make sure the file manager can archive files */ 
    {
      FileMgrClient fclient = acquireFileMgrClient();
      try {
        fclient.validateScratchDir();
      }
      catch(PipelineException ex) {
        return new FailureRsp(timer, ex.getMessage());
      }  
      finally {
        releaseFileMgrClient(fclient);
      }
    }

    if(!pAdminPrivileges.isMasterAdmin(req))
      return new FailureRsp
        (timer, "Only a user with Master Admin privileges may restore checked-in versions!"); 

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      boolean cacheModified = false;
      try {
        timer.resume();	

        if(!isOfflineCacheValid()) 
          throw new PipelineException 
            ("The Restore operation will not be available until the offlined node " +
             "version cache has finished being rebuilt.");

        opn.notify(timer, "Validating Nodes..."); 

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
        timer.acquire();
        List<LoggedLock> onOffLocks = onlineOfflineWriteLock(versions.keySet());
        try {
          timer.resume();	

          /* validate the file sequences to be restored */ 
          TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>> fseqs = 
            new TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>>();
          TreeMap<String,TreeMap<VersionID,SortedMap<String,CheckSum>>> checkSums = 
            new TreeMap<String,TreeMap<VersionID,SortedMap<String,CheckSum>>>(); 
          long total = 0L;
          for(String name : versions.keySet()) {
	  
            timer.acquire();
            LoggedLock checkedInLock = getCheckedInLock(name);
            checkedInLock.acquireReadLock();  
            try {
              timer.resume();	
              TreeMap<VersionID,CheckedInBundle> checkedIn = 
                getCheckedInBundles(name, false);
	  
              for(VersionID vid : versions.get(name)) {
                if(!vol.contains(name, vid)) 
                  throw new PipelineException
                    ("The checked-in version (" + vid + ") of node (" + name + ") cannot " + 
                     "be restored because it is not contained in the archive volume " + 
                     "(" + archiveName + ")!");
                total += vol.getSize(name, vid);
	      
                if(!isOffline(timer, name, vid)) 
                  throw new PipelineException 
                    ("The checked-in version (" + vid + ") of node (" + name + ") cannot " + 
                     "be restored because it is currently online!");
	  
                CheckedInBundle bundle = checkedIn.get(vid);
                if(bundle == null) 
                  throw new PipelineException 
                    ("No checked-in version (" + vid + ") of node (" + name + ") exists " + 
                     "to be restored!");

                NodeVersion vsn = bundle.getVersion();

                if(vsn.isIntermediate()) 
                  throw new PipelineException
                    ("The checked-in version (" + vid + ") of node (" + name + ") " + 
                     "cannot be restored because it is marked as having Intermediate " + 
                     "Files which are never stored in the repository!"); 

                {
                  TreeMap<VersionID,TreeSet<FileSeq>> fvsns = fseqs.get(name);
                  if(fvsns == null) {
                    fvsns = new TreeMap<VersionID,TreeSet<FileSeq>>();
                    fseqs.put(name, fvsns);
                  }
                
                  fvsns.put(vid, vsn.getSequences());
                }
              
                {
                  TreeMap<VersionID,SortedMap<String,CheckSum>> cvsns = checkSums.get(name); 
                  if(cvsns == null) {
                    cvsns = new TreeMap<VersionID,SortedMap<String,CheckSum>>();
                    checkSums.put(name, cvsns);
                  }

                  cvsns.put(vid, vsn.getCheckSums());
                }
              }
            }
            finally {
              checkedInLock.releaseReadLock();  
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
            new RestoreExtFactory(req.getRequestor(), archiveName, versions, archiver, tname);
          try {
            performExtensionTests(timer, factory);
          }
          catch(PipelineException ex) {
            return new FailureRsp(timer, ex.getMessage());
          }

          checkForCancellation(sessionID);

          /* extract the versions from the archive volume by running the archiver plugin and 
             save any STDOUT output */
          {
            String output = null;
            {
              FileMgrClient fclient = acquireFileMgrClient();
              long monitorID = fclient.addMonitor(new RelayOpMonitor(opn));
              try {
                StringBuilder dryRunResults = null;
                if(req.isDryRun()) 
                  dryRunResults = new StringBuilder();

                output = fclient.extract(archiveName, stamp, fseqs, checkSums, 
                                         archiver, env, total, dryRunResults);

                if(dryRunResults != null) 
                  return new DryRunRsp(timer, dryRunResults.toString()); 
              }
              finally {
                fclient.removeMonitor(monitorID); 
                releaseFileMgrClient(fclient);
              }
            }
	  
            long now = System.currentTimeMillis();
            File file = new File(pNodeDir, 
                                 "archives/output/restore/" + archiveName + "-" + now);
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
              TreeSet<Long> stamps = pRestoredOn.get(archiveName);
              if(stamps == null) {
                stamps = new TreeSet<Long>();
                pRestoredOn.put(archiveName, stamps);
              }
              stamps.add(now);
            }
          }
                
          /* set the number of node versions to be processed */ 
          {
            long steps = 0L;
            for(TreeSet<VersionID> vids : versions.values()) 
              steps += vids.size();
            opn.setTotalSteps(steps);    
          }

          /* move the extracted files into the respository */ 
          for(String name : versions.keySet()) {

            timer.acquire();
            LoggedLock checkedInLock = getCheckedInLock(name);
            checkedInLock.acquireReadLock();  
            try {
              timer.resume();	
              TreeMap<VersionID,CheckedInBundle> checkedIn =
                getCheckedInBundles(name, false);

              ArrayList<VersionID> vids = new ArrayList<VersionID>(checkedIn.keySet());
              TreeMap<File,Boolean[]> novelty = noveltyByFile(checkedIn);
	    
              for(VersionID vid : versions.get(name)) {
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
                        if(!isOffline(timer, name, nvid)) 
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
                        else if(!isOffline(timer, name, nvid)) 
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
                  FileMgrClient fclient = acquireFileMgrClient();
                  try {
                    fclient.restore(archiveName, stamp, name, vid, symlinks, targets);
                  }
                  finally {
                    releaseFileMgrClient(fclient);
                  }
                }

                /* update the currently offlined revision numbers */ 
                synchronized(pOfflinedLock) {
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

                opn.step(timer, "Restored: " + name + " (v" + vid + ")"); 
                checkForCancellation(sessionID);
              }
            }
            finally {
              checkedInLock.releaseReadLock();  
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
          LogMgr.getInstance().logAndFlush
            (LogMgr.Kind.Net, LogMgr.Level.Warning,
             ex.getMessage());
        }

        try {
          FileMgrClient fclient = acquireFileMgrClient();
          try {
            fclient.extractCleanup(archiveName, stamp);
          }
          finally {
            releaseFileMgrClient(fclient);
          }
        }
        catch(PipelineException ex) {
          LogMgr.getInstance().logAndFlush
            (LogMgr.Kind.Net, LogMgr.Level.Warning,
             ex.getMessage());
        }

        /* keep the offlined cache file up-to-date */ 
        if(cacheModified && pPreserveOfflinedCache) {
          try {
            writeOfflined();
          }
          catch(PipelineException ex) {
            LogMgr.getInstance().logAndFlush
              (LogMgr.Kind.Ops, LogMgr.Level.Warning,
               ex.getMessage());
	  
            removeOfflinedCache();
          }
        }
      }
    }
    finally {
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      synchronized(pArchivedOn) {
	timer.resume();

	return new MiscGetArchivedOnRsp(timer, pArchivedOn);
      }
    }
    finally {
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      synchronized(pRestoredOn) {
	timer.resume();

	return new MiscGetRestoredOnRsp(timer, pRestoredOn);
      }
    }
    finally {
      pDatabaseLock.releaseReadLock();
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
    long stamp = req.getTimeStamp();

    TaskTimer timer = 
      new TaskTimer("MasterMgr.getRestoredOutput(): " + aname + "-" + stamp);
    try {
      File file = new File(pNodeDir, "archives/output/restore/" + aname + "-" + stamp);
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      synchronized(pArchivedIn) {
	timer.resume();
	
        DoubleMappedSet<String,VersionID,String> archives = 
          new DoubleMappedSet<String,VersionID,String>();

        MappedSet<String,VersionID> vsns = req.getVersions();
        for(String name : vsns.keySet()) {
	  TreeMap<VersionID,TreeSet<String>> aversions = pArchivedIn.get(name);
          if(aversions != null) {
	    for(VersionID vid : vsns.get(name)) {
	      TreeSet<String> anames = aversions.get(vid);
	      if(anames != null) {
                for(String aname : anames)
                  archives.put(name, vid, aname);
              }
            }
          }
        }
	
	return new MiscGetArchivesContainingRsp(timer, archives);
      }
    }
    finally {
      pDatabaseLock.releaseReadLock();
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

    timer.acquire();
    pDatabaseLock.acquireReadLock();
    try {
      timer.resume();

      return new MiscGetArchiveRsp(timer, readArchive(req.getName()));
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }  
    finally {
      pDatabaseLock.releaseReadLock();
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
   NodeWorkingAreaReq req
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
   TreeSet<String> checked, 
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
    
    timer.acquire();
    NodeID id = new NodeID(targetID, name);
    LoggedLock lock = getWorkingLock(id);
    lock.acquireReadLock();
    try {
      timer.resume();

      WorkingBundle bundle = getWorkingBundle(id);
      if(bundle == null) 
	throw new PipelineException
	  ("Only working versions of nodes can be linked!\n" + 
	   "No working version (" + id + ") exists for the upstream node.");
      checked.add(name);

      /* only follow upstream links for non-locked nodes */ 
      NodeMod mod = bundle.getVersion();
      if(!mod.isLocked()) {
	branch.push(name);
	for(LinkMod link : mod.getSources()) 
	  checkForCircularity(timer, link.getName(), targetID, checked, branch);
	branch.pop();      
      }
    }
    finally {
      lock.releaseReadLock();
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
   * Check to see if the current network connection session has received a cancellation
   * request.
   * 
   * @param sessionID 
   *   The unique network connection session ID.
   * 
   * @throws PipelineException
   *   If the operation should be cancelled.
   */ 
  private void 
  checkForCancellation
  (
   Long sessionID
  )
    throws PipelineException
  {
    if((sessionID != null) && pSessionControls.isCancelled(sessionID)) 
      throw new PipelineException("Operation cancelled!");        
  }
      

  /*----------------------------------------------------------------------------------------*/

  /**
   * Recursively perfrorm a node status based operation on the tree of nodes rooted at the 
   * given working version. <P> 
   * 
   * The <CODE>nodeOp</CODE> argument is performed on all upstream nodes and detailed
   * post-operation status information is generated for these nodes.  If the 
   * <CODE>nodeOp</CODE> is <CODE>null</CODE>, then no operation will be performed and only
   * lightweight node status details will be generated for the upstream nodes. <P> 
   * 
   * No downstream node status will be reported (DownstreamMode.None).
   * 
   * @param nodeOp
   *   The node operation or <CODE>null</CODE> if only lightweight node details are required.
   * 
   * @param nodeID
   *   The unique working version identifier.
   * 
   * @param timer
   *   The shared task timer for this operation.
   * 
   * @param sessionID
   *   The unique network connection session ID.
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
   TaskTimer timer, 
   Long sessionID
  ) 
    throws PipelineException
  {
    return performNodeOperation(nodeOp, nodeID, DownstreamMode.None, timer, sessionID);
  }

  /**
   * Recursively perfrorm a node status based operation on the tree of nodes rooted at the 
   * given working version. <P> 
   * 
   * The <CODE>nodeOp</CODE> argument is performed on all upstream nodes and detailed
   * post-operation status information is generated for these nodes.  If the 
   * <CODE>nodeOp</CODE> is <CODE>null</CODE>, then no operation will be performed and only
   * lightweight node status details will be generated for the upstream nodes. <P> 
   * 
   * @param nodeOp
   *   The node operation or <CODE>null</CODE> if only lightweight node details are required.
   * 
   * @param nodeID
   *   The unique working version identifier.
   * 
   * @param dmode
   *   The criteria used to determine how downstream node status is reported.
   * 
   * @param timer
   *   The shared task timer for this operation.
   * 
   * @param sessionID
   *   The unique network connection session ID.
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
   DownstreamMode dmode, 
   TaskTimer timer, 
   Long sessionID
  ) 
    throws PipelineException
  {
    TreeMap<String,NodeStatus> cache = new TreeMap<String,NodeStatus>();
    return performNodeOperation(nodeOp, nodeID, dmode, cache, timer, sessionID); 
  }

  /**
   * Recursively perfrorm a node status based operation on the tree of nodes rooted at the 
   * given working version. <P> 
   * 
   * The <CODE>nodeOp</CODE> argument is performed on all upstream nodes and detailed
   * post-operation status information is generated for these nodes.  If the 
   * <CODE>nodeOp</CODE> is <CODE>null</CODE>, then no operation will be performed and only
   * lightweight node status details will be generated for the upstream nodes. <P> 
   * 
   * @param nodeOp
   *   The node operation or <CODE>null</CODE> if only lightweight node details are required.
   * 
   * @param nodeID
   *   The unique working version identifier.
   * 
   * @param dmode
   *   The criteria used to determine how downstream node status is reported.
   * 
   * @param timer
   *   The shared task timer for this operation.
   * 
   * @param table
   *   The previously computed states indexed by node name.
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
   DownstreamMode dmode, 
   TreeMap<String,NodeStatus> cache,
   TaskTimer timer,
   Long sessionID
  ) 
    throws PipelineException
  {
    NodeStatus root = null;
    {
      performUpstreamNodeOp(nodeOp, nodeID, false, false, 
                            new LinkedList<String>(), cache, 
                            timer, sessionID);

      root = cache.get(nodeID.getName());
      if(root == null)
	throw new IllegalStateException(); 

      if(nodeOp != null) {
        OverallQueueState qstate = root.getHeavyDetails().getOverallQueueState();
        validateStaleLinks(root, qstate == OverallQueueState.Finished);
      }
    }
    
    getDownstreamNodeStatus(root, nodeID, dmode, timer);
    
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
   *   The node operation or <CODE>null</CODE> if only lightweight node details are required.
   * 
   * @param nodeID
   *   The unique working version identifier.
   * 
   * @param isTargetLinkLocked
   *   Whether a locked link from a checked-in target node to this node exists.
   * 
   * @param ignoreAnnotations
   *   Whether to skip the lookup of node annotations when generating NodeDetails.
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
   * @param sessionID 
   *   The unique network connection session ID.
   * 
   * @throws PipelineException 
   *   If unable to perform the node operation.
   */ 
  //@SuppressWarnings("null")
  private void 
  performUpstreamNodeOp
  (
   NodeOp nodeOp,
   NodeID nodeID, 
   boolean isTargetLinkLocked, 
   boolean ignoreAnnotations, 
   LinkedList<String> branch, 
   TreeMap<String,NodeStatus> table, 
   TaskTimer timer,
   Long sessionID
  ) 
    throws PipelineException
  {
    String name = nodeID.getName();
    boolean isLightweight = (nodeOp == null);

    /* check for cancellation */
    if(!isLightweight) 
      checkForCancellation(sessionID);

    /* check for circularity */ 
    checkBranchForCircularity(name, branch);

    /* skip nodes which have already been processed */ 
    if(table.containsKey(name)) 
      return;

    /* push the current node onto the end of the branch */ 
    branch.addLast(name);
    
    /* node annotations */ 
    TreeMap<String,BaseAnnotation> annotations = null;
    if(!ignoreAnnotations) {
      annotations = new TreeMap<String,BaseAnnotation>();

      timer.acquire();
      LoggedLock lock = getAnnotationsLock(name); 
      lock.acquireReadLock();
      try {
        timer.resume();
      
        TreeMap<String,BaseAnnotation> annots = getAnnotationsTable(name);
        if(annots != null) {
          for(String aname : annots.keySet()) {
            BaseAnnotation annot = annots.get(aname);
            if(annot != null) 
              annotations.put(aname, (BaseAnnotation) annot.clone());
          }
        }
      }
      finally {
        lock.releaseReadLock();
      }   
    }

    timer.acquire();
    LoggedLock workingLock = getWorkingLock(nodeID);
    if(!isLightweight && nodeOp.writesWorking()) 
      workingLock.acquireWriteLock();
    else 
      workingLock.acquireReadLock();
    LoggedLock checkedInLock = getCheckedInLock(name);
    if(!isLightweight && nodeOp.writesCheckedIn())
      checkedInLock.acquireWriteLock();
    else 
      checkedInLock.acquireReadLock();
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
      long missingStamp = System.currentTimeMillis();
      switch(versionState) {
      case CheckedIn:
	if(!isTargetLinkLocked) {
	  for(LinkVersion link : latest.getSources()) {
	    NodeID lnodeID = new NodeID(nodeID, link.getName());
	    
	    performUpstreamNodeOp(nodeOp, lnodeID, link.isLocked(), ignoreAnnotations, 
                                  branch, table, timer, sessionID);
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
	    
	    performUpstreamNodeOp(nodeOp, lnodeID, false, ignoreAnnotations, 
                                  branch, table, timer, sessionID);
	    NodeStatus lstatus = table.get(link.getName());
	    
	    status.addSource(lstatus);
	    lstatus.addTarget(status);
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
        /* if locked, then the only difference can be whether there is a newer version */ 
        if(workIsLocked) {
          switch(versionState) {
          case Identical:
            linkState = LinkState.Identical;
            break;

          case NeedsCheckOut:
            linkState = LinkState.NeedsCheckOut;
          }
        }
        else {
          /* are the base and latest versions actually different? */ 
          boolean needsCheckOut = (versionState == VersionState.NeedsCheckOut);

          /* whether any of working version links changed with respect to the 
               base and latest checked-in versions? */ 
          boolean baseLinkModified   = false;
          boolean latestLinkModified = false;

          /* check working links */ 
          for(LinkMod link : work.getSources()) {
            String lname = link.getName(); 
            NodeDetailsLight sdetails = table.get(lname).getLightDetails();
            VersionID svid = sdetails.getWorkingVersion().getWorkingID();

            /* compare with base version links, 
                 skip if locked since it can't be different */
            if(!workIsLocked) {
              LinkVersion blink = base.getSource(lname); 

              /* are there additional, different or different version working links */ 
              if((blink == null) || 
                 !link.equals(blink) || 
                 !blink.getVersionID().equals(svid)) {
                baseLinkModified = true; 
              }
              
              /* has locking changed for any common links? */
              else if((blink != null) && 
                      (sdetails.getWorkingVersion().isLocked() != blink.isLocked())) {
                baseLinkModified = true;
              }
            }

            /* compare with latest version links, 
                 skip if not NeedsCheckOut since the base and latest versions are the same */ 
            if(needsCheckOut) {
              LinkVersion llink = latest.getSource(lname); 

              /* are there additional, different or different version working links */ 
              if((llink == null) || 
                 !link.equals(llink) || 
                 !llink.getVersionID().equals(svid)) {
                latestLinkModified = true; 
              }
              
              /* has locking changed for any common links? */
              else if((llink != null) && 
                      (sdetails.getWorkingVersion().isLocked() != llink.isLocked())) {
                latestLinkModified = true;
              }
            }
          }

          /* check for links on checked-in version not on working version */ 
          {
            Set<String> workSourceNames = work.getSourceNames();

            /* check for additional base links */ 
            for(LinkVersion link : base.getSources()) {
              String lname = link.getName(); 
              if(!workSourceNames.contains(lname)) {
                baseLinkModified = true;
                break;
              }
            }

            /* check for additional latest links */ 
            if(needsCheckOut) {
              for(LinkVersion link : latest.getSources()) {
                String lname = link.getName(); 
                if(!workSourceNames.contains(lname)) {
                  latestLinkModified = true;
                  break;
                }
              }
            }
          }

          /* compute the link state */ 
          if(needsCheckOut) {
            if(latestLinkModified) {
              if(baseLinkModified) 
                linkState = LinkState.Conflicted;
              else 
                linkState = LinkState.NeedsCheckOut;
            }
            else {
              linkState = LinkState.Identical;              
            }
          }
          else {
            if(baseLinkModified) 
              linkState = LinkState.Modified;
            else 
              linkState = LinkState.Identical;
          }
        }
      }

      /* if only lightweight node status details are required this time... */ 
      if(isLightweight) {
        NodeDetailsLight details = 
          new NodeDetailsLight(work, base, latest, versionIDs, 
                               versionState, propertyState, linkState); 
        status.setLightDetails(details);
        status.setAnnotations(annotations);
      }

      /* otherwise, we need to go on and compute the heavyweight per-file and queue 
           related node status information... */ 
      else {
        /* get per-file jobIDs, states and any checksum updates */ 
        Long jobIDs[] = null;
        JobState jobStates[] = null;
        switch(versionState) {
        case CheckedIn:
          {
            int numFrames = latest.getPrimarySequence().numFrames();
            jobIDs        = new Long[numFrames];
            jobStates     = new JobState[numFrames];
          }
          break;

        default:
          {
            int numFrames = work.getPrimarySequence().numFrames();
            jobIDs        = new Long[numFrames];
            jobStates     = new JobState[numFrames];
            
            if(!workIsFrozen) {
              ArrayList<Long> jids   = new ArrayList<Long>();
              ArrayList<JobState> js = new ArrayList<JobState>();
              
              timer.acquire();
              LoggedLock clock = getCheckSumLock(nodeID);
              clock.acquireWriteLock();
              try {
                timer.resume();
              
                CheckSumBundle cbundle = getCheckSumBundle(nodeID); 
                CheckSumCache cache = cbundle.getCache();

                CheckSumCache jcache = null;
                QueueMgrControlClient qclient = acquireQueueMgrClient();
                try {
                  jcache = qclient.getJobStatesAndCheckSums
                    (nodeID, work.getTimeStamp(), work.getPrimarySequence(), 
                     cache.getLatestUpdates(), jids, js);
                }
                finally {
                  releaseQueueMgrClient(qclient);
                }
                  
                if(!jcache.isEmpty()) {
                  cache.resetModified(); 
                  cache.addAll(jcache);
                  if(cache.wasModified()) {
                    cbundle.setCache(cache); 
                    writeCheckSumCache(cache); 
                  }
                } 
              }
              finally {
                clock.releaseWriteLock();
              }  
 
              if(jobIDs.length != jids.size())
                throw new IllegalStateException(); 
              jobIDs = jids.toArray(jobIDs);
              
              if(jobStates.length != js.size())
                throw new IllegalStateException(); 
              jobStates = js.toArray(jobStates);
            }
          }
        }

        /* get per-file FileStates and timestamps */ 
        TreeMap<FileSeq, FileState[]> fileStates = new TreeMap<FileSeq, FileState[]>();
        TreeMap<FileSeq,NativeFileInfo[]> fileInfos = new TreeMap<FileSeq,NativeFileInfo[]>();
        boolean[] anyMissing = null;
        Long[] newestStamps = null;
        Long[] oldestStamps = null;
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
            fileInfos.put(fseq, new NativeFileInfo[fs.length]);

            if(anyMissing == null) 
              anyMissing = new boolean[fs.length];

            if(newestStamps == null) 
              newestStamps = new Long[fs.length];	  

            if(oldestStamps == null) 
              oldestStamps = new Long[fs.length];
          }
          break;

        default:
          {
            /* query the file manager */
            {	     
              FileMgrClient fclient = acquireFileMgrClient();
              try {
                VersionID vid = null;
                if(latest != null) 
                  vid = latest.getVersionID();

                SortedMap<String,CheckSum> baseCheckSums = null;
                if(base != null) 
                  baseCheckSums = base.getCheckSums();
                
                SortedMap<String,CheckSum> latestCheckSums = null;
                if(latest != null) 
                  latestCheckSums = latest.getCheckSums();

                timer.acquire();
                LoggedLock clock = getCheckSumLock(nodeID);
                clock.acquireWriteLock();
                try {
                  timer.resume();

                  CheckSumBundle cbundle = getCheckSumBundle(nodeID); 

                  boolean binter = false;
                  if(base != null) 
                    binter = base.isIntermediate();

                  boolean linter = false;
                  if(latest != null) 
                    linter = latest.isIntermediate();

                  CheckSumCache updatedCheckSums = 
                    fclient.states(nodeID, work, versionState, jobStates, workIsFrozen, vid, 
                                   binter, baseCheckSums, linter, latestCheckSums, 
                                   cbundle.getCache(), fileStates, fileInfos);

                  if(updatedCheckSums.wasModified()) {
                    try {
                      cbundle.setCache(updatedCheckSums); 
                      writeCheckSumCache(updatedCheckSums); 
                    }
                    catch(PipelineException ex) {
                      LogMgr.getInstance().log
                        (LogMgr.Kind.Sum, LogMgr.Level.Warning, ex.getMessage());
                    }
                  }
                }
                finally {
                  clock.releaseWriteLock();
                }  
              }
              finally {
                releaseFileMgrClient(fclient);
              }
              
              /* if frozen, all the files are just links so use the working time stamp */ 
              if(workIsFrozen) {
                for(FileSeq fseq : work.getSequences()) {
                  NativeFileInfo infos[] = fileInfos.get(fseq);
                  if(infos == null) {
                    infos = new NativeFileInfo[fseq.numFrames()];
                    fileInfos.put(fseq, infos); 
                  }
                  
                  int wk;
                  for(wk=0; wk<infos.length; wk++) {
                    if(infos[wk] != null) 
                      infos[wk].setTimeStamp(work.getTimeStamp());
                    else 
                      infos[wk] = new NativeFileInfo(0, work.getTimeStamp(), true); 
                  }
                }
              }
              /* otherwise, correct the timestamps for any symlinks we've made 
                 during a previous check-in */ 
              else {
                for(FileSeq fseq : work.getSequences()) {                  
                  NativeFileInfo infos[] = fileInfos.get(fseq); 
                  if(infos != null) {
                    int wk=0;
                    for(Path path : fseq.getPaths()) {
                      if(infos[wk] != null) {
                        long ostamp = infos[wk].getTimeStamp();
                        infos[wk].setTimeStamp(work.correctStamp(path.toString(), ostamp));
                      }
                      wk++;
                    }
                  }
                }
              }
            }

            /* get the newest/oldest of the timestamp for each file sequence index */ 
            for(Map.Entry<FileSeq,NativeFileInfo[]> entry : fileInfos.entrySet()) {
              FileSeq fseq = entry.getKey();
              NativeFileInfo infos[] = entry.getValue(); 

              if(newestStamps == null) 
                newestStamps = new Long[infos.length];

              if(oldestStamps == null) 
                oldestStamps = new Long[infos.length];

              int wk;
              for(wk=0; wk<infos.length; wk++) {
                NativeFileInfo info = infos[wk];
                if(info != null) {
                  long stamp = info.getTimeStamp();
                  if((newestStamps[wk] == null) || (stamp > newestStamps[wk]))
                    newestStamps[wk] = stamp; 

                  if((oldestStamps[wk] == null) || (stamp < oldestStamps[wk]))
                    oldestStamps[wk] = stamp; 
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
            else if(anyModified) {
              if((propertyState == PropertyState.Identical) &&
                 (linkState == LinkState.Modified) && 
                 !anyModifiedFs) {
                overallNodeState = OverallNodeState.ModifiedLinks;
              }
              else {
                overallNodeState = OverallNodeState.Modified;
              }
            } 
            else if(anyNeedsCheckOut) {
              if(!workIsLocked) {
                for(LinkMod link : work.getSources()) {
                  NodeDetailsHeavy ldetails = table.get(link.getName()).getHeavyDetails();

                  switch(ldetails.getOverallNodeState()) {
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
              if(!workIsLocked) {
                switch(linkState) {
                case Identical:
                  for(LinkMod link : work.getSources()) {
                    NodeDetailsHeavy ldetails = table.get(link.getName()).getHeavyDetails();
                    VersionID lvid = ldetails.getWorkingVersion().getWorkingID();

                    switch(ldetails.getOverallNodeState()) {                      
                    case Modified:
                    case ModifiedLinks:
                    case Conflicted:	
                    case Missing:
                    case MissingNewer:
                      overallNodeState = OverallNodeState.ModifiedLinks;
                    }
                  }
                  break;

                default:
                  throw new IllegalStateException
                    ("A LinkState of (" + linkState + ") should not be possible here!");
                }
              }

              if(overallNodeState == null)
                overallNodeState = OverallNodeState.Identical;
            }
          }
        }

        /* determine per-file QueueStates */  
        QueueState queueStates[] = null;
        TreeSet<String> staleLinks = new TreeSet<String>(); 
        switch(versionState) {
        case CheckedIn:
          {
            int numFrames = latest.getPrimarySequence().numFrames();
            queueStates = new QueueState[numFrames];

            int wk;
            for(wk=0; wk<queueStates.length; wk++) 
              queueStates[wk] = QueueState.Undefined;
          }
          break;

        default:
          /* can we assume that the files are always going to be Finished? */ 
          {
            boolean alwaysFinished = false;
            if(workIsLocked) {
              alwaysFinished = true;
            }
            else {
              switch(overallNodeState) {
              case Identical:
              case NeedsCheckOut: 
                alwaysFinished = true;
              }
            }

            if(alwaysFinished) {
              int numFrames = work.getPrimarySequence().numFrames();
              queueStates = new QueueState[numFrames];
              
              int wk;
              for(wk=0; wk<queueStates.length; wk++) 
                queueStates[wk] = QueueState.Finished;
            }
          }
          
          /* ask the queue mananager for per-file job information */ 
          if(queueStates == null) {
            int numFrames = work.getPrimarySequence().numFrames();
            queueStates = new QueueState[numFrames];

            /* process each file to determine its QueueState... */ 
            int wk;
            for(wk=0; wk<queueStates.length; wk++) {

              /* if there is an enabled action, check for any jobs which are not Finished */ 
              if(work.isActionEnabled()) {
                if(jobStates[wk] != null) {
                  switch(jobStates[wk]) {
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

                  case Limbo:
                    queueStates[wk] = QueueState.Limbo;
                    break;

                  case Failed:
                    queueStates[wk] = QueueState.Failed;
                  }
                }
              }

              /* are any of the primary/secondary files missing? */ 
              if((queueStates[wk] == null) && anyMissing[wk]) {
                if(work.isActionEnabled())
                  queueStates[wk] = QueueState.Stale;
                else 
                  queueStates[wk] = QueueState.Dubious;
              }

              /* have any critical changes to the node properties been made since the 
                   primary/secondary files were created? */ 
              if((queueStates[wk] == null) && 
                 (oldestStamps[wk] < work.getLastCriticalModification())) { 
                if(work.isActionEnabled())
                  queueStates[wk] = QueueState.Stale;
              }

              /* have any critical changes to the node links been made since the 
                   primary/secondary files were created? */ 
              if((queueStates[wk] == null) && 
                 (oldestStamps[wk] < work.getLastCriticalSourceModification())) { 
                if(work.isActionEnabled())
                  queueStates[wk] = QueueState.Stale;
                else 
                  queueStates[wk] = QueueState.Dubious;
                
                for(LinkMod link : work.getSources()) { 
                  switch(link.getPolicy()) {
                  case Reference:
                  case Dependency:
                    staleLinks.add(link.getName());
                  }
                }                
              }

              /* otherwise, we need to check individual upstream per-file dependencies... */ 
              if(queueStates[wk] == null) { 
                for(LinkMod link : work.getSources()) {
                  if((!work.isActionEnabled() && 
                      (link.getPolicy() == LinkPolicy.Dependency)) ||
                     (work.isActionEnabled() && 
                      (link.getPolicy() != LinkPolicy.Association))) {
    
                    NodeStatus lstatus = status.getSource(link.getName());
                    NodeDetailsHeavy ldetails = lstatus.getHeavyDetails();
                    
                    long lstamps[]    = ldetails.getUpdateTimeStamps();
                    UpdateState lus[] = ldetails.getUpdateStates();
                      
                    boolean foundStaleLink = false;
                    switch(link.getRelationship()) {
                    case OneToOne:
                      {
                        Integer offset = link.getFrameOffset();
                        int idx = wk+offset;
                        if(((idx >= 0) && (idx < lus.length))) {
                          if((lus[idx] == UpdateState.Stale) || 
                             (lus[idx] == UpdateState.Dubious) ||
                             (oldestStamps[wk] < lstamps[idx])) {

                            if(work.isActionEnabled())
                              queueStates[wk] = QueueState.Stale;
                            else 
                              queueStates[wk] = QueueState.Dubious;

                            foundStaleLink = true;
                          }
                        }
                      }
                      break;
                        
                    case All:
                      {
                        int fk;
                        for(fk=0; fk<lus.length; fk++) {
                          if((lus[fk] == UpdateState.Stale) ||
                             (lus[fk] == UpdateState.Dubious) ||
                             (oldestStamps[wk] < lstamps[fk])) {
                              
                            if(work.isActionEnabled())
                              queueStates[wk] = QueueState.Stale;
                            else 
                              queueStates[wk] = QueueState.Dubious;
                            
                            foundStaleLink = true;
                          }
                        }
                      }
                      break;

                    case None: 
                      throw new PipelineException
                        ("Somehow a " + link.getPolicy() + " link has a None " + 
                         "relationship!");
                    }
		    
                    if(foundStaleLink)
                      staleLinks.add(link.getName());
                  }
                }
              }

              if(queueStates[wk] == null) 
                queueStates[wk] = QueueState.Finished;
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
          boolean anyDubious = false;

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
            case Limbo:
              anyRunning = true;
              break;

            case Aborted:
              anyAborted = true;
              break;

            case Failed:
              anyFailed = true;
              break;

            case Dubious:
              anyDubious = true;
            }
          }

          if(anyDubious) 
            overallQueueState = OverallQueueState.Dubious;
          else if(anyFailed) 
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
          else {
            overallQueueState = OverallQueueState.Finished;
          }
        }
        
        /**
         * Propagate staleness from upstream per-file dependencies.
         * 
         * ---------------------------------------------------------------------------------
         * Set the per-file update time stamps of each file to be the newest of:
         * 
         * + If the FileState is Missing, the time stamp of when the FileState was computed.
         * 
         * + The newest actual file time stamp.
         * 
         * + The LastCriticalModification timestamp of the current node.
         * 
         * + The LastCriticalSourceModification timestamp of the current node.
         * 
         * + The time stamp of any upstream file upon which the file depends via a 
         *   Reference link or any non-Finshed file depended on via a Dependency link.
         * 
         * ---------------------------------------------------------------------------------
         * Set the per-file UpdateState of each file to: 
         *  
         * + Undefined: if the VersionState is CheckedIn.
         * 
         * + Unknown: if the local QueueState is Finished and all upstream files upon which 
         *            the file depends via a Dependency/Reference link have an UpdateState 
         *            of Unknown.
         * 
         * + Stale: if the local QueueState is Stale, Queued, Paused, Aborted, Running or 
         *          Failed or if any upstream file via Dependency/Reference has UpdateState 
         *          of Stale.
         * 
         * + Dubious: if the local QueueState is Dubious or if any upstream file via 
         *            Dependency/Reference has UpdateState of Stale.
         */
        long[] updateStamps = new long[oldestStamps.length];
        UpdateState updateStates[] = new UpdateState[oldestStamps.length];
        switch(versionState) {
        case CheckedIn: 
          {
            int wk;
            for(wk=0; wk<updateStates.length; wk++) 
              updateStates[wk] = UpdateState.Undefined;
          }
          break;

        default:
          if(workIsLocked) {
            int wk;
            for(wk=0; wk<queueStates.length; wk++) {
              updateStamps[wk]   = newestStamps[wk];
              updateStates[wk] = UpdateState.Unknown;
            }
          }
          else {
            int wk;
            for(wk=0; wk<queueStates.length; wk++) {
              /* initial UpdateState */ 
              switch(queueStates[wk]) {
              case Dubious:
                updateStates[wk] = UpdateState.Dubious;
                break;
                
              case Finished:
                if(anyMissing[wk]) 
                  updateStates[wk] = UpdateState.Stale;                   
                else 
                  updateStates[wk] = UpdateState.Unknown; 
                break;
                
              default:
                updateStates[wk] = UpdateState.Stale;
              }
              
              /* newest of the timestamps */ 
              if(anyMissing[wk] || (newestStamps[wk] == null)) 
                updateStamps[wk] = missingStamp;
              else 
                updateStamps[wk] = newestStamps[wk];

              long criticalProps = work.getLastCriticalModification();
              if(criticalProps > updateStamps[wk])
                updateStamps[wk] = criticalProps;

              long criticalLinks = work.getLastCriticalSourceModification();
              if(criticalLinks > updateStamps[wk]) 
                updateStamps[wk] = criticalLinks;
                    
              /* process upstream per-file dependencies... */ 
              for(LinkMod link : work.getSources()) { 
                boolean isDepend = false;
                switch(link.getPolicy()) {
                case Dependency:
                  isDepend = true;

                case Reference:
                  {
                    NodeStatus lstatus = status.getSource(link.getName());
                    NodeDetailsHeavy ldetails = lstatus.getHeavyDetails();
                    
                    long lstamps[]    = ldetails.getUpdateTimeStamps();
                    UpdateState lus[] = ldetails.getUpdateStates();
                    QueueState lqs[]  = ldetails.getQueueStates();

                    boolean foundStaleLink = false;
                    switch(link.getRelationship()) {
                    case OneToOne:
                      {
                        Integer offset = link.getFrameOffset();
                        int idx = wk+offset;
                        if((idx >= 0) && (idx < lus.length)) {
                          if((lstamps[idx] > updateStamps[wk]) &&
                             (!isDepend || (isDepend && (lqs[idx] != QueueState.Finished)))) {
                            updateStamps[wk] = lstamps[idx];
                            foundStaleLink = true;
                          }

                          switch(lus[idx]) {
                          case Stale:
                            if(updateStates[wk] != UpdateState.Dubious) 
                              updateStates[wk] = UpdateState.Stale;
                            foundStaleLink = true;
                            break;

                          case Dubious:
                            updateStates[wk] = UpdateState.Dubious;
                            foundStaleLink = true;
                          }                          
                        }
                      }
                      break;
                      
                    case All:
                      {
                        int fk;
                        for(fk=0; fk<lus.length; fk++) {
                          if((lstamps[fk] > updateStamps[wk]) &&
                             (!isDepend || (isDepend && (lqs[fk] != QueueState.Finished)))) {
                            updateStamps[wk] = lstamps[fk];
                            foundStaleLink = true;
                          }

                          switch(lus[fk]) {
                          case Stale:
                            if(updateStates[wk] != UpdateState.Dubious) 
                              updateStates[wk] = UpdateState.Stale;
                            foundStaleLink = true;
                            break;

                          case Dubious:
                            updateStates[wk] = UpdateState.Dubious;
                            foundStaleLink = true;
                          }             
                        }
                      }
                      break;

                    case None: 
                      throw new PipelineException
                        ("Somehow a non-Association link has a None relationship!");
                    }

                    if(foundStaleLink)
                      staleLinks.add(link.getName());
                  }
                }
              }
            }

            for(String lname : staleLinks) 
              status.addStaleLink(lname); 
          }
        }
        
        /* create the node details */ 
        NodeDetailsHeavy details = 
          new NodeDetailsHeavy(work, base, latest, versionIDs, 
                               overallNodeState, overallQueueState, 
                               versionState, propertyState, linkState, 
                               fileStates, fileInfos, updateStamps,
                               jobIDs, queueStates, updateStates);

        /* add details and annotations to the node's status */ 
        status.setHeavyDetails(details);
        status.setAnnotations(annotations);

        /* peform the node operation -- may alter the status and/or status details */ 
        nodeOp.perform(status, timer);
      }
    }
    finally {
      if(!isLightweight && nodeOp.writesCheckedIn())
	checkedInLock.releaseWriteLock();
      else 
	checkedInLock.releaseReadLock(); 
      if(!isLightweight && nodeOp.writesWorking()) 
        workingLock.releaseWriteLock();
      else 
        workingLock.releaseReadLock();
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
   * 
   * @param finishedRoot
   *   Whether the root node of the state operation is in a Finished queue state.
   */ 
  private void 
  validateStaleLinks
  (
   NodeStatus status, 
   boolean finishedRoot
  )
  {
    String name = status.getName();

    /* whether there are downstream links which are stale, 
         no need to check if the root is Finished */ 
    boolean staleDownstream = false;
    if(!finishedRoot) {
      for(NodeStatus tstatus : status.getTargets()) {
        if(!tstatus.isStaleLink(name)) {
          NodeDetailsLight tdetails = tstatus.getLightDetails();
          if(tdetails != null) {
            NodeMod tmod = tdetails.getWorkingVersion(); 
            if(tmod != null) {
              LinkMod link = tmod.getSource(name);
              switch(link.getPolicy()) {
              case Reference:
              case Dependency:
                staleDownstream = true;
              }
            }
          }
        }

        if(staleDownstream)
          break;
      }
    }

    /* process the upstream links... */ 
    NodeMod mod = status.getLightDetails().getWorkingVersion(); 
    for(NodeStatus lstatus : status.getSources()) {
      boolean linkProcessed = false;
      if(mod != null) {
        LinkMod link = mod.getSource(lstatus.getName());
        switch(link.getPolicy()) {
        case Reference:
        case Dependency:
          {
            /* supress the link staleness if the root node is Finished 
               or if there are no downstream links which stale */ 
            if(finishedRoot || staleDownstream) 
              status.removeStaleLink(lstatus.getName());
            validateStaleLinks(lstatus, finishedRoot);
            linkProcessed = true;
          }
        }
      }

      /* an Association link or CheckedIn source node starts a new tree with its 
           own Finished root flag... */ 
      if(!linkProcessed) {
        OverallQueueState qstate = lstatus.getHeavyDetails().getOverallQueueState();  
        validateStaleLinks(lstatus, qstate == OverallQueueState.Finished);
      }
    }
  }

  /**
   * Compute the state of all nodes downstream of the given node. <P> 
   * 
   * @param root 
   *   The already computed node status for the root node.
   * 
   * @param nodeID
   *   The unique working version identifier.
   * 
   * @param dmode
   *   The criteria used to determine how downstream node status is reported.
   * 
   * @param timer
   *   The shared task timer for this operation.
   */ 
  private void
  getDownstreamNodeStatus
  (
   NodeStatus root, 
   NodeID nodeID, 
   DownstreamMode dmode, 
   TaskTimer timer
  ) 
    throws PipelineException
  {
    if(dmode == DownstreamMode.None) 
      return;

    String rname = root.getName();
    TreeMap<String,NodeStatus> table = new TreeMap<String,NodeStatus>();
    table.put(rname, root);

    LinkedList<String> branch = new LinkedList<String>();

    switch(dmode) {
    case CheckedInOnly:
    case All:
      {
        TreeSet<VersionID> vids = new TreeSet<VersionID>();

        timer.acquire();      
        String name = nodeID.getName();
        LoggedLock lock = getCheckedInLock(name);
        lock.acquireReadLock();
        try {
          timer.resume();	
        
          TreeMap<VersionID,CheckedInBundle> checkedIn = getCheckedInBundles(name);
          vids.addAll(checkedIn.keySet());
        }
        catch(PipelineException ex) {
        }
        finally {
          lock.releaseReadLock();
        }  

        for(VersionID vid : vids) 
          getCheckedInDownstreamNodeStatus(nodeID, vid, branch, table, timer);
      }
    }

    switch(dmode) {
    case WorkingOnly:
    case All:
      getWorkingDownstreamNodeStatus(nodeID, branch, table, timer);
    }
  }

  /**
   * Lookup the already cached node status or create a new one for the given node.
   * 
   * @param nodeID
   *   The unique working version identifier. 
   * 
   * @param table
   *   The previously computed states indexed by node name.
   */ 
  private NodeStatus
  lookupOrCreateNodeStatus
  (
   NodeID nodeID, 
   TreeMap<String,NodeStatus> table
  ) 
  {
    NodeStatus status = table.get(nodeID.getName()); 
    if(status == null) {
      status = new NodeStatus(nodeID); 
      table.put(nodeID.getName(), status);
    }

    return status; 
  }

  /**
   * Recursively process checked-in node versions downstream.
   * 
   * @param nodeID
   *   The unique working version identifier.
   * 
   * @param vid
   *   The revision number of the current checked-in node version being processed. 
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
   * @return
   *   The status of the current node if there exists one or more versions downstream which 
   *   are the latest version of their respective node. 
   */ 
  private NodeStatus
  getCheckedInDownstreamNodeStatus
  (
   NodeID nodeID, 
   VersionID vid, 
   LinkedList<String> branch, 
   TreeMap<String,NodeStatus> table, 
   TaskTimer timer
   ) 
    throws PipelineException
  {
    String name = nodeID.getName();

    /* check for circularity */ 
    checkBranchForCircularity(name, branch);
  
    /* push the current node onto the end of the branch */ 
    branch.addLast(name);

    /* get checked-in downstream links */ 
    TreeMap<VersionID,MappedSet<String,VersionID>> downstream = null;
    {
      timer.acquire();
      LoggedLock lock = getDownstreamLock(name);
      lock.acquireReadLock();
      try {
        timer.resume();
        
        DownstreamLinks dsl = getDownstreamLinks(name); 
        downstream = dsl.getAllCheckedIn();
      }
      finally {
        lock.releaseReadLock();
      }
    }

    /* returned status for this node */ 
    NodeStatus status = null;

    /* process downstream links from this checked-in version... */ 
    MappedSet<String,VersionID> links = downstream.get(vid);
    if(links != null) {
      for(String dname : links.keySet()) {
        for(VersionID dvid : links.get(dname)) {
          NodeID dnodeID = new NodeID(nodeID, dname);
          
          NodeStatus dstatus = 
            getCheckedInDownstreamNodeStatus(dnodeID, dvid, branch, table, timer);
          
          if(dstatus != null) {
            status = lookupOrCreateNodeStatus(nodeID, table);
            dstatus.addSource(status);
            status.addTarget(dstatus);
          }
        }
      }
    }

    /* if no downstream versions are the latest version, then check to see if this one is */ 
    if(status == null) {
      timer.acquire();
      LoggedLock lock = getCheckedInLock(name);
      lock.acquireReadLock();
      try {
        timer.resume();	
        
        TreeMap<VersionID,CheckedInBundle> checkedIn = getCheckedInBundles(name);
        if(checkedIn.lastKey().equals(vid))
          status = lookupOrCreateNodeStatus(nodeID, table);
      }
      finally {
        lock.releaseReadLock();
      }  
    }
    
    /* pop the current node off of the end of the branch */ 
    branch.removeLast();
    
    return status;
  } 

  /**
   * Recursively process working node versions downstream.
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
   * @return
   *   The status of the current node. 
   */ 
  private NodeStatus
  getWorkingDownstreamNodeStatus
  (
   NodeID nodeID, 
   LinkedList<String> branch, 
   TreeMap<String,NodeStatus> table, 
   TaskTimer timer
  ) 
    throws PipelineException
  {
    String name = nodeID.getName();

    /* check for circularity */ 
    checkBranchForCircularity(name, branch);
  
    /* push the current node onto the end of the branch */ 
    branch.addLast(name);

    /* get working downstream links */ 
    TreeSet<String> links = null;
    {
      timer.acquire();
      LoggedLock lock = getDownstreamLock(name);
      lock.acquireReadLock();
      try {
        timer.resume();
        
        DownstreamLinks dsl = getDownstreamLinks(name); 
        links = dsl.getWorking(nodeID); 
      }
      finally {
        lock.releaseReadLock();
      }
    }

    /* returned status for this node */ 
    NodeStatus status = lookupOrCreateNodeStatus(nodeID, table);

    /* process downstream links... */ 
    if(links !=  null) {
      for(String dname : links) {
        NodeID dnodeID = new NodeID(nodeID, dname);
        NodeStatus dstatus = getWorkingDownstreamNodeStatus(dnodeID, branch, table, timer);
        dstatus.addSource(status);
        status.addTarget(dstatus);
      }
    }

    /* pop the current node off of the end of the branch */ 
    branch.removeLast();

    return status;
  } 

  /**
   * Recursively find the names and primary file sequences of all nodes downstream of 
   * the given node which are currently checked-out into the given working area. <P> 
   * 
   * @param nodeID
   *   The unique working version identifier.
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
    TreeMap<String,FileSeq> fseqs,
    TaskTimer timer
   ) 
     throws PipelineException
   {
     NodeStatus status = null;
     {
       TreeMap<String,NodeStatus> table = new TreeMap<String,NodeStatus>();
       NodeStatus root = lookupOrCreateNodeStatus(nodeID, table);
       LinkedList<String> branch = new LinkedList<String>();
       status = getWorkingDownstreamNodeStatus(nodeID, branch, table, timer);
     }

     getDownstreamWorkingSeqsHelper(status, fseqs, timer);
   }

  /**
   * Use the previously computed node status to recursively find the names and primary 
   * file sequences of all nodes downstream of the given node which are currently 
   * checked-out into the given working area. <P> 
   * 
   * @param status
   *   The downtream node status. 
   * 
   * @param fseqs
   *   The collected primary file sequences indexed by fully resolved names of the nodes.
   * 
   * @param timer
   *   The shared task timer for this operation.
   */ 
  private void 
  getDownstreamWorkingSeqsHelper
  (
   NodeStatus status, 
   TreeMap<String,FileSeq> fseqs,
   TaskTimer timer
  ) 
    throws PipelineException
  {
    NodeID nodeID = status.getNodeID();
    String name = nodeID.getName();

    /* skip nodes which have already been processed */ 
    if(fseqs.containsKey(name)) 
      return;

    /* add the current node */ 
    timer.acquire();
    LoggedLock workingLock = getWorkingLock(nodeID);
    workingLock.acquireReadLock(); 
    try {
      timer.resume();
    
      WorkingBundle working = null;
      try {
 	working = getWorkingBundle(nodeID);
      }
      catch(PipelineException ex) {
      }
    
      if(working != null) {
 	FileSeq fseq = working.getVersion().getPrimarySequence();
 	fseqs.put(name, fseq);
      }
    }
    finally {
      workingLock.releaseReadLock();
    }
      
    /* process any downstream nodes... */ 
    for(NodeStatus dstatus : status.getTargets()) 
      getDownstreamWorkingSeqsHelper(dstatus, fseqs, timer);
  } 


        
  /*----------------------------------------------------------------------------------------*/
  /*   F I L E   M G R   H E L P E R S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get a connection to the file manager.
   */ 
  private FileMgrClient
  acquireFileMgrClient()
  {
    if(pInternalFileMgr)
      return pFileMgrDirectClient;

    synchronized(pFileMgrNetClients) {
      if(pFileMgrNetClients.isEmpty()) {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Net, LogMgr.Level.Finest,
	   "Creating New File Manager Client.");

	return new FileMgrNetClient();
      }
      else {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Net, LogMgr.Level.Finest,
	   "Reusing File Manager Client: " + (pFileMgrNetClients.size()-1) + " inactive");

	return pFileMgrNetClients.pop();
      }
    }
  }

  /**
   * Return an inactive connection to the file manager for reuse.
   */ 
  private void
  releaseFileMgrClient
  (
   FileMgrClient client
  )
  {
    if(pInternalFileMgr)
      return;

    synchronized(pFileMgrNetClients) {
      pFileMgrNetClients.push((FileMgrNetClient) client);
      
      LogMgr.getInstance().logAndFlush
	(LogMgr.Kind.Net, LogMgr.Level.Finest,
	 "Freed File Manager Client: " + pFileMgrNetClients.size() + " inactive");
    }
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   Q U E U E   M G R   H E L P E R S                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get a connection to the queue manager.
   */ 
  private QueueMgrControlClient
  acquireQueueMgrClient()
  {
    synchronized(pQueueMgrClients) {
      if(pQueueMgrClients.isEmpty()) {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Net, LogMgr.Level.Finest,
	   "Creating New Queue Manager Client.");

	return new QueueMgrControlClient();
      }
      else {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Net, LogMgr.Level.Finest,
	   "Reusing File Manager Client: " + (pQueueMgrClients.size()-1) + " inactive");

	return pQueueMgrClients.pop();
      }
    }
  }

  /**
   * Return an inactive connection to the file manager for reuse.
   */ 
  private void
  releaseQueueMgrClient
  (
   QueueMgrControlClient client
  )
  {
    synchronized(pQueueMgrClients) {
      pQueueMgrClients.push(client);
      
      LogMgr.getInstance().logAndFlush
	(LogMgr.Kind.Net, LogMgr.Level.Finest,
	 "Freed Queue Manager Client: " + pQueueMgrClients.size() + " inactive");
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
  private LoggedLock
  getOnlineOfflineLock
  (
   String name
  ) 
  {
    synchronized(pOnlineOfflineLocks) {
      LoggedLock lock = pOnlineOfflineLocks.get(name);

      if(lock == null) { 
	lock = new LoggedLock("OnlineOffline");
	pOnlineOfflineLocks.put(name, lock);
      }

      return lock;
    }
  }

  /**
   * Aquire the read access to the online/offline locks of the given named nodes. <P> 
   * 
   * Insures that the locks are acquired in the lexical order of the node names.  The returned
   * list of locks is in reverse lexical order to that the locks will be unlocked in exactly
   * the opposite order.
   * 
   * @param names
   *   The fully resolved names of the nodes to lock.
   */ 
  private List<LoggedLock> 
  onlineOfflineReadLock
  (
   Collection<String> names
  ) 
  {
    TreeSet<String> sorted = new TreeSet<String>(names);
    ArrayList<LoggedLock> locks = new ArrayList<LoggedLock>();
    for(String name : sorted) {
      LoggedLock lock = getOnlineOfflineLock(name);
      lock.acquireReadLock();
      locks.add(lock);
    }
    
    Collections.reverse(locks);

    return Collections.unmodifiableList(locks);
  }
  
  /**
   * Release the read access to the given online/offline locks.
   * 
   * @param locks
   *   The previously acquired read locks returned by the onlineOfflineReadLock() method.
   */ 
  private void 
  onlineOfflineReadUnlock
  (
   List<LoggedLock> locks
  ) 
  {
    for(LoggedLock lock : locks) 
      lock.releaseReadLock();
  }
  
  /**
   * Aquire the write access to the online/offline locks of the given named nodes. <P> 
   * 
   * Insures that the locks are acquired in the lexical order of the node names.  The returned
   * list of locks is in reverse lexical order to that the locks will be unlocked in exactly
   * the opposite order.
   * 
   * @param names
   *   The fully resolved names of the nodes to lock.
   */ 
  private List<LoggedLock> 
  onlineOfflineWriteLock
  (
   Collection<String> names
  ) 
  {
    TreeSet<String> sorted = new TreeSet<String>(names);
    ArrayList<LoggedLock> locks = new ArrayList<LoggedLock>();
    for(String name : sorted) {
      LoggedLock lock = getOnlineOfflineLock(name);
      lock.acquireWriteLock();
      locks.add(lock);
    }
    
    Collections.reverse(locks);

    return Collections.unmodifiableList(locks);
  }
  
  /**
   * Release the write access to the given online/offline locks.
   * 
   * @param locks
   *   The previously acquired read locks returned by the onlineOfflineWriteLock() method.
   */ 
  private void 
  onlineOfflineWriteUnlock
  (
   List<LoggedLock> locks
  ) 
  {
    for(LoggedLock lock : locks) 
      lock.releaseWriteLock();
  }
  

  /*----------------------------------------------------------------------------------------*/

  /** 
   * Lookup the lock for the table of annotations for the node with the 
   * given name. 
   * 
   * @param name 
   *   The fully resolved node name
   */
  private LoggedLock
  getAnnotationsLock
  (
   String name
  ) 
  {
    synchronized(pAnnotationLocks) {
      LoggedLock lock = pAnnotationLocks.get(name);

      if(lock == null) { 
	lock = new LoggedLock("Annotations");
	pAnnotationLocks.put(name, lock);
      }

      return lock;
    }
  }

  /** 
   * Lookup the lock for the table of checked-in version bundles for the node with the 
   * given name. 
   * 
   * @param name 
   *   The fully resolved node name
   */
  private LoggedLock
  getCheckedInLock
  (
   String name
  ) 
  {
    synchronized(pCheckedInLocks) {
      LoggedLock lock = pCheckedInLocks.get(name);

      if(lock == null) { 
	lock = new LoggedLock("CheckedInNode");
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
  private LoggedLock
  getWorkingLock
  (
   NodeID id
  ) 
  {
    synchronized(pWorkingLocks) {
      LoggedLock lock = pWorkingLocks.get(id);

      if(lock == null) { 
	lock = new LoggedLock("WorkingNode");
	pWorkingLocks.put(id, lock);
      }

      return lock;
    }
  }

  /** 
   * Lookup the lock for the checksum cache bundle with the given node id.
   * 
   * @param id 
   *   The unique working version identifier.
   */
  private LoggedLock
  getCheckSumLock
  (
   NodeID id
  ) 
  {
    synchronized(pCheckSumLocks) {
      LoggedLock lock = pCheckSumLocks.get(id);

      if(lock == null) { 
	lock = new LoggedLock("CheckSum");
	pCheckSumLocks.put(id, lock);
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
  private LoggedLock
  getDownstreamLock
  (
   String name
  ) 
  {
    synchronized(pDownstreamLocks) {
      LoggedLock lock = pDownstreamLocks.get(name);

      if(lock == null) { 
	lock = new LoggedLock("Downstream");
	pDownstreamLocks.put(name, lock);
      }

      return lock;
    }
  }
 


  /*----------------------------------------------------------------------------------------*/
  /*   B U N D L E   H E L P E R S                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the table of annotations for the node with the given name.
   *   
   * This method assumes that a read/write lock for the annotation has already been 
   * acquired.
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @return 
   *   The table of annotations or <CODE>null</CODE> if no annotations exist.
   */
  private TreeMap<String,BaseAnnotation> 
  getAnnotationsTable
  ( 
   String name
  ) 
    throws PipelineException
  {
    /* lookup the annotations */ 
    TreeMap<String,BaseAnnotation> table = null;
    synchronized(pAnnotations) {
      table = pAnnotations.get(name);
    }

    if(table != null) {
      pAnnotationCounters.hit();
      return table;
    }

    /* read in the annotations from disk */ 
    table = readAnnotations(name);
    if(table == null) 
      return null; 

    synchronized(pAnnotations) {
      pAnnotations.put(name, table);
    }    

    /* keep track of the change to the cache */ 
    incrementAnnotationCounter(name); 

    return table;
  }

  /**
   * Get the table of checked-in bundles for the node with the given name.
   * 
   * This method assumes that a read/write lock for the checked-in version has already been 
   * acquired.
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @param addToCache
   *   Whether new checked-in versions read from disk should be added to the node cache.
   */
  private TreeMap<VersionID,CheckedInBundle>
  getCheckedInBundles
  ( 
   String name
  ) 
    throws PipelineException
  {
    return getCheckedInBundles(name, true); 
  }

  /**
   * Get the table of checked-in bundles for the node with the given name.
   * 
   * This method assumes that a read/write lock for the checked-in version has already been 
   * acquired.
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @param addToCache
   *   Whether new checked-in versions read from disk should be added to the node cache.
   *   If set to (false), then the returned CheckedInBundles should be considered read-only.
   */
  private TreeMap<VersionID,CheckedInBundle>
  getCheckedInBundles
  ( 
   String name, 
   boolean addToCache
  ) 
    throws PipelineException
  {
    /* lookup the bundles */ 
    TreeMap<VersionID,CheckedInBundle> table = null;
    synchronized(pCheckedInBundles) {
      table = pCheckedInBundles.get(name);
    }

    if(table != null) {
      pCheckedInCounters.hits(table.size());
      return table;
    }

    /* read in the bundles from disk */ 
    table = readCheckedInVersions(name);
    if(table == null) 
      throw new PipelineException
	("No checked-in versions exist for node (" + name + ")!");

    if(addToCache) {
      synchronized(pCheckedInBundles) {
        pCheckedInBundles.put(name, table);
      }    
      
      /* keep track of the change to the cache */ 
      incrementCheckedInCounters(name, table.keySet());
    }

    return table;
  }

  /** 
   * Get the working bundle with the given working version ID.
   * 
   * This method assumes that a read/write lock for the working version has already been 
   * acquired.
   * 
   * @param nodeID 
   *   The unique working version identifier.
   */
  private WorkingBundle
  getWorkingBundle
  (
   NodeID nodeID
  )
    throws PipelineException
  {
    return getWorkingBundle(nodeID, true); 
  }

  /** 
   * Get the working bundle with the given working version ID.
   * 
   * This method assumes that a read/write lock for the working version has already been 
   * acquired.
   * 
   * @param nodeID 
   *   The unique working version identifier.
   * 
   * @param addToCache
   *   Whether new working versions read from disk should be added to the node cache.
   *   If set to (false), then the returned WorkingBundle should be considered read-only.
   */
  private WorkingBundle
  getWorkingBundle
  (
   NodeID nodeID,
   boolean addToCache
  )
    throws PipelineException
  { 
    if(nodeID == null) 
      throw new IllegalArgumentException("The working version ID cannot be (null)!");

    String name = nodeID.getName();

    /* lookup the bundle */ 
    WorkingBundle bundle = null;
    synchronized(pWorkingBundles) {
      TreeMap<NodeID,WorkingBundle> table = pWorkingBundles.get(name);
      if(table == null) {
	table = new TreeMap<NodeID,WorkingBundle>();
	pWorkingBundles.put(name, table);
      }
      else {
	bundle = table.get(nodeID);
      }
    }

    if(bundle != null) {
      pWorkingCounters.hit();
      return bundle;
    }

    /* read in the bundle from disk */ 
    NodeMod mod = readWorkingVersion(nodeID);
    if(mod == null) 
      throw new PipelineException
	("No working version of node (" + name + ") exists under the view " + 
         "(" + nodeID.getView() + ") owned by user (" + nodeID.getAuthor() + ")!");
    
    bundle = new WorkingBundle(mod);

    if(addToCache) {
      synchronized(pWorkingBundles) {
        pWorkingBundles.get(name).put(nodeID, bundle);
      }
      
      /* keep track of the change to the cache */ 
      incrementWorkingCounter(nodeID); 
    }

    return bundle;
  }

  /** 
   * Get the checksum cache bundle with the given working version ID.
   * 
   * This method assumes that a read/write lock for the checksum cache has already been 
   * acquired.
   * 
   * @param nodeID
   *   The unique working version identifier.
   */
  private CheckSumBundle
  getCheckSumBundle
  (
   NodeID nodeID
  )
    throws PipelineException
  {
    return getCheckSumBundle(nodeID, true); 
  }

  /** 
   * Get the checksum cache bundle with the given working version ID.
   * 
   * This method assumes that a read/write lock for the checksum cache has already been 
   * acquired.
   * 
   * @param nodeID
   *   The unique working version identifier.
   * 
   * @param addToCache
   *   Whether new working checksums read from disk should be added to the checksum cache.
   *   If set to (false), then the returned CheckSumBundle should be considered read-only.
   */
  private CheckSumBundle
  getCheckSumBundle
  (
   NodeID nodeID,
   boolean addToCache
  )
    throws PipelineException
  { 
    if(nodeID == null) 
      throw new IllegalArgumentException("The working version ID cannot be (null)!");

    String name = nodeID.getName();

    /* lookup the bundle */ 
    CheckSumBundle bundle = null;
    synchronized(pCheckSumBundles) {
      bundle = pCheckSumBundles.get(name, nodeID);
    }

    if(bundle != null) {
      pCheckSumCounters.hit();
      return bundle;
    }

    /* read in the bundle from disk */ 
    CheckSumCache cache = readCheckSumCache(nodeID);
    if(cache == null) 
      cache = new CheckSumCache(nodeID); 

    bundle = new CheckSumBundle(cache); 

    if(addToCache) {
      synchronized(pCheckSumBundles) {
        pCheckSumBundles.put(name, nodeID, bundle); 
      }

      /* keep track of the change to the cache */ 
      incrementCheckSumCounter(nodeID); 
    }

    return bundle;
  }

  /**
   * Get the downstream links cache for a node.
   * 
   * This method assumes that a read/write lock for the downstream links has already been 
   * acquired.
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
   * Monitors and manages the amount of memory held by various caches of node related 
   * information.
   */ 
  public void 
  cacheGC() 
  {
    TaskTimer timer = new TaskTimer("Cache Garbage Collector");

    /* lookup the amount of memory currently being used by the JVM */ 
    Runtime rt = Runtime.getRuntime();
    long minMemory  = pMinFreeMemory.get(); 
    long maxMemory  = rt.maxMemory();
    long freeMemory = maxMemory - rt.totalMemory() + rt.freeMemory();

    /* report the current memory statistics */ 
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Mem, LogMgr.Level.Fine)) {
      LogMgr.getInstance().logAndFlush
	(LogMgr.Kind.Mem, LogMgr.Level.Fine,
         "Memory Report:\n" + 
	 "  ---- JVM HEAP ----------------------\n" + 
	 "       Free = " + freeMemory + 
         " (" + ByteSize.longToFloatString(freeMemory) + ")\n" + 
         "    Minimum = " + minMemory + 
         " (" + ByteSize.longToFloatString(minMemory) + ")\n" +
	 "    Maximum = " + maxMemory + 
         " (" + ByteSize.longToFloatString(maxMemory) + ")\n" +
	 "  ------------------------------------");
    }

    boolean reduce = (freeMemory < minMemory); 
    long totalFreed = 0L; 

    /* checked-in garbage collection */ 
    {
      pCheckedInCounters.adjustBounds(reduce);
      pCheckedInCounters.logStats();
      pCheckedInCounters.resetHitMiss();

      if(reduce || pCheckedInCounters.hasExceededMax()) {
        timer.suspend();
        TaskTimer tm = new TaskTimer();

        long freed = 0L;
        while(pCheckedInCounters.hasExceededMin()) {
          String name = pCheckedInRead.poll();
          if(name == null) {
            pCheckedInCounters.emptySanityCheck();
            break;
          }
          
          tm.acquire();
          LoggedLock lock = getCheckedInLock(name);
          lock.acquireWriteLock();
          try {
            tm.resume();	
            
            TreeMap<VersionID,CheckedInBundle> checkedIn = null;
            tm.acquire();
            synchronized(pCheckedInBundles) {
              tm.resume();	
              checkedIn = pCheckedInBundles.remove(name); 
            }
            
            if(checkedIn != null) {
              decrementCheckedInCounters(name, checkedIn.keySet());
              freed += checkedIn.keySet().size();
            }
          }
          finally {
            lock.releaseWriteLock();
          }  
        }

        totalFreed += freed;
        
        tm.suspend();
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Mem, LogMgr.Level.Finer,
           pCheckedInCounters.getTitle() + " [GC]: " + 
           pCheckedInCounters.getCurrent() + "/" + freed + " (cached/freed)\n  " + tm);
        timer.accum(tm);
      }
    }

    /* working garbage collection */ 
    {
      pWorkingCounters.adjustBounds(reduce);
      pWorkingCounters.logStats();
      pWorkingCounters.resetHitMiss();
      
      if(reduce || pWorkingCounters.hasExceededMax()) {
        timer.suspend();
        TaskTimer tm = new TaskTimer();

        long freed = 0L;
        while(pWorkingCounters.hasExceededMin()) {
          NodeID nodeID = pWorkingRead.poll();
          if(nodeID == null) {
            pWorkingCounters.emptySanityCheck();
            break;
          }
          
          tm.acquire();
          LoggedLock workingLock = getWorkingLock(nodeID);
          workingLock.acquireWriteLock();
          try {
            tm.resume();	
            
            String name = nodeID.getName();
            boolean found = false;
            tm.acquire();
            synchronized(pWorkingBundles) {
              tm.resume();	
              TreeMap<NodeID,WorkingBundle> bundles = pWorkingBundles.get(name); 
              if(bundles != null) {
                bundles.remove(nodeID);
                if(bundles.isEmpty()) 
                  pWorkingBundles.remove(name); 
                found = true;
              }
            }

            if(found) {
              decrementWorkingCounter(nodeID); 
              freed++;
            }
          }
          finally {
            workingLock.releaseWriteLock();
          }  
        }
        
        totalFreed += freed;

        tm.suspend();
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Mem, LogMgr.Level.Finer,
           pWorkingCounters.getTitle() + " [GC]: " + 
           pWorkingCounters.getCurrent() + "/" + freed + " (cached/freed)\n  " + tm);
        timer.accum(tm);
      }
    }
    
    /* checksum garbage collection */ 
    {
      pCheckSumCounters.adjustBounds(reduce);
      pCheckSumCounters.logStats();
      pCheckSumCounters.resetHitMiss();

      if(reduce || pCheckSumCounters.hasExceededMax()) {
        timer.suspend();
        TaskTimer tm = new TaskTimer();

        long freed = 0L;
        while(pCheckSumCounters.hasExceededMin()) {
          NodeID nodeID = pCheckSumsRead.poll();
          if(nodeID == null) {
            pCheckSumCounters.emptySanityCheck();
            break;
          }
          
          tm.acquire();
          LoggedLock clock = getCheckSumLock(nodeID);
          clock.acquireWriteLock();
          try {
            tm.resume();	
            
            boolean found = false;
            tm.acquire();
            synchronized(pCheckSumBundles) {
              tm.resume();	
              CheckSumBundle bundle = pCheckSumBundles.remove(nodeID.getName(), nodeID);
              if(bundle != null) 
                found = true;
            }

            if(found) {
              decrementCheckSumCounter(nodeID); 
              freed++;
            }
          }
          finally {
            clock.releaseWriteLock();
          }  
        }
        
        totalFreed += freed;

        tm.suspend();
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Mem, LogMgr.Level.Finer,
           pCheckSumCounters.getTitle() + " [GC]: " + 
           pCheckSumCounters.getCurrent() + "/" + freed + " (cached/freed)\n  " + tm);
        timer.accum(tm);
      }
    }

    /* annotation garbage collection */ 
    {
      pAnnotationCounters.adjustBounds(reduce);
      pAnnotationCounters.logStats();
      pAnnotationCounters.resetHitMiss();

      if(reduce || pAnnotationCounters.hasExceededMax()) {
        timer.suspend();
        TaskTimer tm = new TaskTimer();

        long freed = 0L;
        while(pAnnotationCounters.hasExceededMin()) {
          String name = pAnnotationsRead.poll();
          if(name == null) {
            pAnnotationCounters.emptySanityCheck();
            break;
          }

          tm.acquire();
          LoggedLock clock = getAnnotationsLock(name);
          clock.acquireWriteLock();
          try {
            tm.resume();	
            
            boolean found = false;
            tm.acquire();
            synchronized(pAnnotations) {
              tm.resume();	
              found = (pAnnotations.remove(name) != null); 
            }

            if(found) {
              decrementAnnotationCounter(name); 
              freed++;
            }
          }
          finally {
            clock.releaseWriteLock();
          }  
        }
        
        totalFreed += freed;

        tm.suspend();
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Mem, LogMgr.Level.Finer,
           pAnnotationCounters.getTitle() + " [GC]: " + 
           pAnnotationCounters.getCurrent() + "/" + freed + " (cached/freed)\n  " + tm);
        timer.accum(tm);
      }
    }

    /* if we're ahead of schedule, take a nap */ 
    {
      LogMgr.getInstance().logStage
	(LogMgr.Kind.Mem, LogMgr.Level.Fine,
	 timer); 

      long nap = pCacheGCInterval.get() - timer.getTotalDuration();
      if(nap > 0) {
	try {
          pCacheTrigger.block(nap); 
	}
	catch(InterruptedException ex) {
	}
      }
      else {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Mem, LogMgr.Level.Finest,
	   "Cache Monitor: Overbudget by (" + (-nap) + ") msec...");
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Dump the contents of the checked-in cache.
   */ 
  private void 
  debugCheckedInCache() 
  {
    if(!LogMgr.getInstance().isLoggable(LogMgr.Kind.Mem, LogMgr.Level.Detail))
      return;

    StringBuilder buf = new StringBuilder(); 

    long readCnt = 0L;
    buf.append("--- Read Queue ---\n"); 
    for(String name : pCheckedInRead) {
      buf.append("  " + name + "\n"); 
      readCnt++;
    }
    
    long cachedCnt = 0L;
    buf.append("--- Cached ---\n"); 
    synchronized(pCheckedInBundles) {
      for(Map.Entry<String,TreeMap<VersionID,CheckedInBundle>> entry : 
            pCheckedInBundles.entrySet()) {
        String name = entry.getKey();
        int numVersions = entry.getValue().size();
        buf.append("  " + name + " = " + numVersions + " (versions)\n"); 
        cachedCnt += numVersions;
      }
    }
   
    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Mem, LogMgr.Level.Detail, 
       "\n" + 
       "--- Checked-In Cache ------------------------------------------------------------\n" + 
       "  Count = " + pCheckedInCounters.getCurrent() + " (versions)\n" + 
       "   Read = " + readCnt + " (nodes)\n" + 
       " Cached = " + cachedCnt + " (versions)\n" +
       buf.toString() + 
       "--- Checked-In Cache ------------------------------------------------------------"); 
  }

  /**
   * Record that a new checked-in version has been added to the cache.
   */ 
  private void 
  incrementCheckedInCounter
  (
   String name, 
   VersionID vid
  ) 
  {
    pCheckedInCounters.miss(); 
    pCheckedInRead.add(name);

    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Mem, LogMgr.Level.Finest)) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Mem, LogMgr.Level.Finest,
	 "Cached Checked-In Version: " + name + " v" + vid); 
    }

    debugCheckedInCache(); 
  }

  /**
   * Record that a new set of checked-in versions have been added to the cache.
   */ 
  private void 
  incrementCheckedInCounters
  (
   String name, 
   Set<VersionID> vids
  ) 
  {
    for(VersionID vid : vids) {
      pCheckedInCounters.miss(); 
      if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Mem, LogMgr.Level.Finest)) {
        LogMgr.getInstance().log
          (LogMgr.Kind.Mem, LogMgr.Level.Finest,
           "Cached Checked-In Version: " + name + " v" + vid); 
      }
    }

    pCheckedInRead.add(name);

    debugCheckedInCache(); 
  }

  /**
   * Record that a checked-in version has been freed from the cache. 
   */ 
  private void 
  decrementCheckedInCounters
  (
   String name, 
   Set<VersionID> vids
  ) 
  {
    for(VersionID vid : vids) {
      pCheckedInCounters.free(); 
      if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Mem, LogMgr.Level.Finest)) {
        LogMgr.getInstance().log
          (LogMgr.Kind.Mem, LogMgr.Level.Finest,
           "Freed Checked-In Version: " + name + " v" + vid); 
      }
    }

    debugCheckedInCache(); 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Dump the contents of the working cache.
   */ 
  private void 
  debugWorkingCache() 
  {
    if(!LogMgr.getInstance().isLoggable(LogMgr.Kind.Mem, LogMgr.Level.Detail))
      return;

    StringBuilder buf = new StringBuilder(); 

    long readCnt = 0L;
    buf.append("--- Read Queue ---\n"); 
    for(NodeID nodeID : pWorkingRead) {
      buf.append("  " + nodeID.getName() + " " + 
                 "(" + nodeID.getAuthor() + "|" + nodeID.getView() + ")\n"); 
      readCnt++;
    }
    
    long cachedCnt = 0L;
    buf.append("--- Cached ---\n"); 
    synchronized(pWorkingBundles) {
      for(Map.Entry<String,TreeMap<NodeID,WorkingBundle>> entry : 
            pWorkingBundles.entrySet()) {
        String name = entry.getKey();
        int numVersions = entry.getValue().size();
        buf.append("  " + name + " = " + numVersions + " (versions)\n"); 
        cachedCnt += numVersions;
      }
    }
   
    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Mem, LogMgr.Level.Detail, 
       "\n" + 
       "--- Working Cache ---------------------------------------------------------------\n" + 
       "  Count = " + pWorkingCounters.getCurrent() + " (versions)\n" + 
       "   Read = " + readCnt + " (versions)\n" + 
       " Cached = " + cachedCnt + " (versions)\n" +
       buf.toString() + 
       "--- Working Cache ---------------------------------------------------------------"); 
  }

  /**
   * Record that a new working version has been added to the cache.
   */ 
  private void 
  incrementWorkingCounter
  (
   NodeID nodeID
  ) 
  {
    pWorkingCounters.miss(); 
    pWorkingRead.add(nodeID);

    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Mem, LogMgr.Level.Finest)) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Mem, LogMgr.Level.Finest,
	 "Cached Working Version: " + nodeID.getName() + 
	 " (" + nodeID.getAuthor() + "|" + nodeID.getView() + ")"); 
    }

    debugWorkingCache();   
  }

  /**
   * Record that a working version has been freed from the cache. 
   */ 
  private void 
  decrementWorkingCounter
  (
   NodeID nodeID
  ) 
  {
    pWorkingCounters.free(); 

    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Mem, LogMgr.Level.Finest)) {   
      LogMgr.getInstance().log
	(LogMgr.Kind.Mem, LogMgr.Level.Finest,
	 "Freed Working Version: " +  nodeID.getName() + 
	 " (" + nodeID.getAuthor() + "|" + nodeID.getView() + ")"); 
    }
    
    debugWorkingCache();  
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Dump the contents of the checksum cache.
   */ 
  private void 
  debugCheckSumCache() 
  {
    if(!LogMgr.getInstance().isLoggable(LogMgr.Kind.Mem, LogMgr.Level.Detail))
      return;

    StringBuilder buf = new StringBuilder(); 

    long readCnt = 0L;
    buf.append("--- Read Queue ---\n"); 
    for(NodeID nodeID : pWorkingRead) {
      buf.append("  " + nodeID.getName() + " " + 
                 "(" + nodeID.getAuthor() + "|" + nodeID.getView() + ")\n"); 
      readCnt++;
    }
    
    long cachedCnt = 0L;
    buf.append("--- Cached ---\n"); 
    synchronized(pCheckSumBundles) {
      for(Map.Entry<String,TreeMap<NodeID,CheckSumBundle>> entry : 
            pCheckSumBundles.entrySet()) {
        String name = entry.getKey();
        int numVersions = entry.getValue().size();
        buf.append("  " + name + " = " + numVersions + " (versions)\n"); 
        cachedCnt += numVersions;
      }
    }
   
    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Mem, LogMgr.Level.Detail, 
       "\n" + 
       "--- CheckSum Cache --------------------------------------------------------------\n" + 
       "  Count = " + pCheckSumCounters.getCurrent() + " (versions)\n" + 
       "   Read = " + readCnt + " (versions)\n" + 
       " Cached = " + cachedCnt + " (versions)\n" +
       buf.toString() + 
       "--- CheckSum Cache --------------------------------------------------------------"); 
  }

  /**
   * Record that a new working version checksum has been added to the cache.
   */ 
  private void 
  incrementCheckSumCounter
  (
   NodeID nodeID
  ) 
  {
    pCheckSumCounters.miss(); 
    pCheckSumsRead.add(nodeID);

    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Mem, LogMgr.Level.Finest)) {  
      LogMgr.getInstance().log
	(LogMgr.Kind.Mem, LogMgr.Level.Finest,
	 "Cached Working CheckSum: " + nodeID.getName() + 
	 " (" + nodeID.getAuthor() + "|" + nodeID.getView() + ")"); 
    }
    
    debugCheckSumCache();
  }

  /**
   * Record that a working version checksum has been freed from the cache. 
   */ 
  private void 
  decrementCheckSumCounter
  (
   NodeID nodeID
  ) 
  {
    pCheckSumCounters.free(); 

    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Mem, LogMgr.Level.Finest)) { 
      LogMgr.getInstance().log
	(LogMgr.Kind.Mem, LogMgr.Level.Finest,
	 "Freed Working CheckSum: " +  nodeID.getName() + 
	 " (" + nodeID.getAuthor() + "|" + nodeID.getView() + ")"); 
    }
    
    debugCheckSumCache();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Dump the contents of the annotations cache.
   */ 
  private void 
  debugAnnotationCache() 
  {
    if(!LogMgr.getInstance().isLoggable(LogMgr.Kind.Mem, LogMgr.Level.Detail))
      return;

    StringBuilder buf = new StringBuilder(); 

    long readCnt = 0L;
    buf.append("--- Read Queue ---\n"); 
    for(String name : pAnnotationsRead) {
      buf.append("  " + name + "\n"); 
      readCnt++;
    }
    
    long cachedCnt = 0L;
    buf.append("--- Cached ---\n"); 
    synchronized(pAnnotations) {
      for(Map.Entry<String,TreeMap<String,BaseAnnotation>> entry : 
            pAnnotations.entrySet()) {
        String name = entry.getKey();
        buf.append("  " + name + "\n"); 
        cachedCnt++;
      }
    }
   
    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Mem, LogMgr.Level.Detail, 
       "\n" + 
       "--- Annotation Cache ------------------------------------------------------------\n" + 
       "  Count = " + pAnnotationCounters.getCurrent() + " (nodes)\n" + 
       "   Read = " + readCnt + " (nodes)\n" + 
       " Cached = " + cachedCnt + " (nodes)\n" +
       buf.toString() + 
       "--- Annotation Cache ------------------------------------------------------------"); 
  }

  /**
   * Record that a new set of annotations have been added to the cache.
   */ 
  private void 
  incrementAnnotationCounter
  (
   String name
  ) 
  {
    pAnnotationCounters.miss(); 
    pAnnotationsRead.add(name);

    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Mem, LogMgr.Level.Finest)) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Mem, LogMgr.Level.Finest,
	 "Cached Annotation: " + name); 
    }

    debugAnnotationCache();    
  }

  /**
   * Record that a new set of annotations have been freed from the cache. 
   */ 
  private void 
  decrementAnnotationCounter
  (
   String name
  ) 
  {
    pAnnotationCounters.free(); 

    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Mem, LogMgr.Level.Finest)) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Mem, LogMgr.Level.Finest,
	 "Freed Annotation: " + name); 
    }

    debugAnnotationCache();    
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   D A T A B A S E   B A C K U P                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Periodically synchronizes the database with a backup directory and optionally creates
   * tar archives of this backup directory.
   */ 
  public void 
  backupSync
  (
   boolean first
  ) 
  {
    /* initial delay the first time through */ 
    if(first) {
      try {
        pBackupSyncTrigger.tryAcquire(1, pBackupSyncInterval.get(), TimeUnit.MILLISECONDS);
      }
      catch(InterruptedException ex) {
        return;
      }
      pBackupSyncTrigger.drainPermits();
    }

    TaskTimer timer = new TaskTimer();

    Path backupTarget = pBackupSyncTarget.getAndSet(null);
    boolean doBackup = (backupTarget != null); 

    if(doBackup) {
      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Bak, LogMgr.Level.Info, 
         "Starting Database Backup..."); 
    }
    else {
      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Bak, LogMgr.Level.Fine, 
         "Starting Database Backup Synchronization...");
    }

    /* synchronize the backup directory with the database without locking, 
         this reduced the time needed to hold the lock */ 
    Boolean success = backupSyncHelper(false);
    if(success == null) 
      return;

    if(success && doBackup) {      
      /* synchronize the backup directory with the database locked */ 
      success = backupSyncHelper(true); 
      if(success == null) 
        return;

      if(!success) {
        String bname = backupTarget.getName();
        backupTarget = new Path(backupTarget.getParentPath(), 
                                bname.substring(0, bname.length()-4) + ".UNLOCKED.tar");
      }

      /* make a tarball out of the newly synced backup directory */ 
      try {
        TaskTimer tm = new TaskTimer("Database Backup Archive Created: " + backupTarget); 
        
        ArrayList<String> args = new ArrayList<String>();
        args.add("-cf");
        args.add(backupTarget.toOsString());
        args.add("pipeline");
        
        SubProcessLight proc = 
          new SubProcessLight("DatabaseBackupArchive", "tar", args, System.getenv(), 
                              PackageInfo.sMasterBackupPath.toFile());
        try {
          proc.start();
          proc.join();
          if(!proc.wasSuccessful()) 
            throw new PipelineException
              ("Unable to create the Database Backup Archive: " + backupTarget + "\n\n" + 
               "  " + proc.getStdErr());	
        }
        catch(InterruptedException ex) {
          proc.kill();
          
          LogMgr.getInstance().logAndFlush
            (LogMgr.Kind.Ops, LogMgr.Level.Warning, 
             "Interrupted while performing Database Backup Archive!"); 
          return;
        }
        
        LogMgr.getInstance().logStage(LogMgr.Kind.Bak, LogMgr.Level.Info, tm); 
      }
      catch(PipelineException ex) {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Bak, LogMgr.Level.Severe, 
           ex.getMessage());
      }
    }
    
    /* if we're ahead of schedule, take a nap */ 
    {
      timer.suspend();
      long nap = pBackupSyncInterval.get() - timer.getTotalDuration();
      if(nap > 0) {
	try {
          pBackupSyncTrigger.tryAcquire(1, nap, TimeUnit.MILLISECONDS);
	}
	catch(InterruptedException ex) {
	}
      }
      else {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Bak, LogMgr.Level.Finest,
	   "Database Backup Sync: Overbudget by " + 
           "(" + TimeStamps.formatInterval(-nap) + ")..."); 
      }
      pBackupSyncTrigger.drainPermits();
    }
  }

  /**
   * Synchronize the backup directory with the database. 
   * 
   * @param needsLock
   *   Whether the database-wide lock is required.
   * 
   * @return
   *   Whether the sync was successful, null if interrupted.
   */ 
  private Boolean 
  backupSyncHelper
  (
   boolean needsLock
  ) 
  {
    try {
      TaskTimer tm = new TaskTimer("Database Backup Synchronized " + 
                                   "(" + (needsLock ? "locked" : "live") + ")");
      tm.acquire();
      if(needsLock) {
        if(!pDatabaseLock.tryWriteLock(sBackupTimeout)) {
          LogMgr.getInstance().logAndFlush
            (LogMgr.Kind.Bak, LogMgr.Level.Warning,
             "Giving up attempting to acquire the Database Write-Lock needed to " + 
             "perform the Backup after " + 
             "(" + TimeStamps.formatInterval(sBackupTimeout) + ")."); 
          return false;
        }
      }
      try {
        tm.resume();	
        
        ArrayList<String> args = new ArrayList<String>();
        args.add("--archive");
        args.add("--quiet");
        args.add("--delete");
        args.add("--delete-excluded");
        args.add("--exclude=/pipeline/events");
        args.add("--exclude=/pipeline/lock");
        args.add("--exclude=/pipeline/plugins");
        args.add("--exclude=/pipeline/queue");
        args.add("pipeline");
        args.add(PackageInfo.sMasterBackupPath.toOsString() + "/");
        
        SubProcessLight proc = 
          new SubProcessLight("DatabaseBackupSync", "rsync", args, System.getenv(), 
                              PackageInfo.sNodePath.getParentPath().toFile());
        try {
          proc.start();
          proc.join();
          if(!proc.wasSuccessful()) 
            throw new PipelineException
              ("Unable to perform Database Backup Synchronization:\n\n" + 
               "  " + proc.getStdErr());	
        }
        catch(InterruptedException ex) {
          proc.kill();

          LogMgr.getInstance().logAndFlush
            (LogMgr.Kind.Bak, LogMgr.Level.Warning,
             "Interrupted while performing Database Backup Synchronization!"); 
          return null;
        }
        
        LogMgr.getInstance().logStage
          (LogMgr.Kind.Bak, needsLock ? LogMgr.Level.Info : LogMgr.Level.Fine, 
           tm); 
      }
      finally {
        if(needsLock) 
          pDatabaseLock.releaseWriteLock();
      }

      return true;
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Bak, LogMgr.Level.Severe, 
         ex.getMessage());
      return false;
    }  
  }



  /*----------------------------------------------------------------------------------------*/
  /*  N O D E   E V E N T   W R I T E R                                                     */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Write pending node events to disk. 
   */ 
  public void 
  eventWriter() 
  {
    TaskTimer timer = new TaskTimer("Event Writer");
  
    while(true) {
      BaseNodeEvent event = pPendingEvents.poll();
      if(event == null) 
	break;

      try {
	writeNodeEvent(event);
      }
      catch(PipelineException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Severe,
	   ex.getMessage()); 
      }
    }

    /* if we're ahead of schedule, take a nap */ 
    {
      LogMgr.getInstance().logStage
	(LogMgr.Kind.Ops, LogMgr.Level.Fine,
	 timer); 

      long nap = pEventWriterInterval.get() - timer.getTotalDuration();
      if(nap > 0) {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Ops, LogMgr.Level.Finest,
	   "Event Writer: Sleeping for (" + nap + ") msec...");
	try {
	  Thread.sleep(nap);
	}
	catch(InterruptedException ex) {
	}
      }
      else {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Ops, LogMgr.Level.Finest,
	   "Event Writer: Overbudget by (" + (-nap) + ") msec...");
      }
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
   * @param dir
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

	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Ops, LogMgr.Level.Finest,
	   "Deleting Empty Directory: " + tmp);

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
      if(file.exists()) 
	throw new PipelineException
	  ("Unable to overrite the existing archive file(" + file + ")!");
      
      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Writing Archive: " + archive.getName());
      
      try {
        GlueEncoderImpl.encodeFile("Archive", archive, file);
      }
      catch(GlueException ex) {
        throw new PipelineException(ex);
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
        archive = (ArchiveVolume) GlueDecoderImpl.decodeFile("Archive", file);
      }	
      catch(GlueException ex) {
        throw new PipelineException(ex);
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
        GlueEncoderImpl.encodeFile("ArchivedIn", pArchivedIn, file);
      }
      catch(GlueException ex) {
        throw new PipelineException(ex);
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

      pArchivedIn.clear();
      try {
       	pArchivedIn.putAll
          ((TreeMap<String,TreeMap<VersionID,TreeSet<String>>>) 
           GlueDecoderImpl.decodeFile("ArchivedIn", file));
      }	
      catch(GlueException ex) {
        throw new PipelineException(ex);
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
        TreeMap<String,Long> archivedOn = new TreeMap<String,Long>();
        for(String aname : pArchivedOn.keySet()) 
          archivedOn.put(aname, pArchivedOn.get(aname));

        GlueEncoderImpl.encodeFile("ArchivedOn", archivedOn, file);
      }
      catch(GlueException ex) {
        throw new PipelineException(ex);
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

      pArchivedOn.clear();
      try {
        pArchivedOn.putAll
          ((TreeMap<String,Long>) GlueDecoderImpl.decodeFile("ArchivedOn", file));
      }	
      catch(GlueException ex) {
        throw new PipelineException(ex);
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
          GlueEncoderImpl.encodeFile("RestoredOn", pRestoredOn, file);
      }
      catch(GlueException ex) {
        throw new PipelineException(ex);
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
  @SuppressWarnings("unchecked")
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

      pRestoredOn.clear();
      try {
        pRestoredOn.putAll
          ((TreeMap<String,TreeSet<Long>>) GlueDecoderImpl.decodeFile("RestoredOn", file));
      }	
      catch(GlueException ex) {
        throw new PipelineException(ex);
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
    synchronized(pOfflinedLock) {
      if(!isOfflineCacheValid()) {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Glu, LogMgr.Level.Warning,
           "Ignoring the request to write the Offlined Cache to disk because it is not " + 
           "currently valid.  Likely this is because it is currently in the process of " + 
           "being rebuilt."); 
        return;
      }
      
      if(pOfflined.isEmpty()) 
	return;

      File file = new File(pNodeDir, "archives/offlined");

      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Writing Offlined Cache...");
      
      try {
        GlueEncoderImpl.encodeFile("Offlined", pOfflined, file);
      }
      catch(GlueException ex) {
        throw new PipelineException(ex);
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
    synchronized(pOfflinedLock) {
      pOfflined = new TreeMap<String,TreeSet<VersionID>>();

      File file = new File(pNodeDir, "archives/offlined");
      if(!file.isFile()) 
	return;

      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Reading Offlined Cache...");

      try {
        pOfflined.putAll
          ((TreeMap<String,TreeSet<VersionID>>) GlueDecoderImpl.decodeFile("Offlined", file));
      }	
      catch(GlueException ex) {
        throw new PipelineException(ex);
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
          GlueEncoderImpl.encodeFile("RestoreReqs", pRestoreReqs, file);
        }
        catch(GlueException ex) {
          throw new PipelineException(ex);
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

        try {
          pRestoreReqs.putAll
            ((TreeMap<String,TreeMap<VersionID,RestoreRequest>>)
             GlueDecoderImpl.decodeFile("RestoreReqs", file));
        }	
        catch(GlueException ex) {
          throw new PipelineException(ex);
        }
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
          GlueEncoderImpl.encodeFile("DefaultToolset", pDefaultToolset, file);
        }
        catch(GlueException ex) {
          throw new PipelineException(ex);
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
          pDefaultToolset = (String) GlueDecoderImpl.decodeFile("DefaultToolset", file);
        }	
        catch(GlueException ex) {
          throw new PipelineException(ex);
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
          GlueEncoderImpl.encodeFile("ActiveToolsets", pActiveToolsets, file);
        }
        catch(GlueException ex) {
          throw new PipelineException(ex);
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
  @SuppressWarnings("unchecked")
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
        
        try {
          pActiveToolsets.addAll
            ((TreeSet<String>) GlueDecoderImpl.decodeFile("ActiveToolsets", file));
        }	
        catch(GlueException ex) {
          throw new PipelineException(ex);
        }
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
        GlueEncoderImpl.encodeFile("Toolset", tset, file);
      }
      catch(GlueException ex) {
        throw new PipelineException(ex);
      }
    }
  }
  
  /**
   * Read the toolset with the given name from disk. <P> 
   * 
   * @param tname
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
        tset = (Toolset) GlueDecoderImpl.decodeFile("Toolset", file);
      }	
      catch(GlueException ex) {
        throw new PipelineException(ex);
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
        GlueEncoderImpl.encodeFile("ToolsetPackage", pkg, file);
      }
      catch(GlueException ex) {
        throw new PipelineException(ex);
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
       pkg = (PackageVersion) GlueDecoderImpl.decodeFile("ToolsetPackage", file);
      }	
      catch(GlueException ex) {
        throw new PipelineException(ex);
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
          GlueEncoderImpl.encodeFile("MasterExtensions", pMasterExtensions, file);
        }
        catch(GlueException ex) {
          throw new PipelineException(ex);
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
  @SuppressWarnings("unchecked")
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
          exts = 
            (TreeMap<String,MasterExtensionConfig>) 
            GlueDecoderImpl.decodeFile("MasterExtensions", file);
        }	
        catch(GlueException ex) {
          throw new PipelineException(ex);
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
        GlueEncoderImpl.encodeFile(uptype + "MenuLayout", layout, file);
      }
      catch(GlueException ex) {
          throw new PipelineException(ex);
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
        layout = (PluginMenuLayout) GlueDecoderImpl.decodeFile(uptype + "MenuLayout", file);
      }	
      catch(GlueException ex) {
        throw new PipelineException(ex);
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
        GlueEncoderImpl.encodeFile
          ("Package" + uptype + "Plugins", 
           (DoubleMap<String,String,TreeSet<VersionID>>) plugins, file);
      }
      catch(GlueException ex) {
        throw new PipelineException(ex);
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
        Object data = GlueDecoderImpl.decodeFile("Package" + uptype + "Plugins", file);

	if(data instanceof PluginSet) 
	  plugins = (PluginSet) data;
	else {
	  /* backward compatibility for GLUE files written before PluginSet existed */ 
	  plugins = new PluginSet((DoubleMap<String,String,TreeSet<VersionID>>) data);
	}
      }	
      catch(GlueException ex) {
        throw new PipelineException(ex);
      }

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
        GlueEncoderImpl.encodeFile("SuffixEditors", editors, file);
      }
      catch(GlueException ex) {
        throw new PipelineException(ex);
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
  @SuppressWarnings("unchecked")
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
        editors = (TreeSet<SuffixEditor>) GlueDecoderImpl.decodeFile("SuffixEditors", file);
      }	
      catch(GlueException ex) {
        throw new PipelineException(ex);
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
   * Write a node event to disk. 
   * 
   * @param event
   *   The node event. 
   * 
   * @throws PipelineException
   *   If unable to write the event to file. 
   */ 
  private void 
  writeNodeEvent
  (
   BaseNodeEvent event
  ) 
    throws PipelineException
  {
    String today = sTodayFormat.format(event.getTimeStamp());

    File adir = new File(pNodeDir, "events/authors/" + event.getAuthor() + "/" + today);
    File ndir = new File(pNodeDir, "events/nodes" + event.getNodeName());
    
    synchronized(pMakeDirLock) {
      if(!adir.isDirectory())
	if(!adir.mkdirs()) 
	  throw new PipelineException
	    ("Unable to create the directory (" + adir + ")!");

      if(!ndir.isDirectory())
	if(!ndir.mkdirs()) 
	  throw new PipelineException
	    ("Unable to create the directory (" + ndir + ")!");
    }

    /* find a unique event filename */ 
    String fname = null;
    File afile = null; 
    File nfile = null;
    {
      String stamp = String.valueOf(event.getTimeStamp()); 

      int unique; 
      for(unique=0; unique<100; unique++) {
        fname = (stamp + "." + unique);
        afile = new File(adir, fname);
        nfile = new File(ndir, fname); 
        
        if(!nfile.isFile() && !afile.isFile())
          break;

        fname = null;
      }

      if(fname == null) 
        throw new PipelineException
          ("Unable to determine a unique filename for the node event in the " + 
           "(" + adir + ") and (" + ndir + ") directories for an event on (" + stamp + ") " + 
           "after trying 100 suffixes!"); 
    }

    LogMgr.getInstance().log
      (LogMgr.Kind.Glu, LogMgr.Level.Finer,
       "Writing Node Event File: " + nfile); 

    synchronized(pNodeEventFileLock) {
      try {
        GlueEncoderImpl.encodeFile("Event", event, nfile);
      }
      catch(GlueException ex) {
        throw new PipelineException(ex);
      }
      
      try {
	File rel = new File("../../../nodes" + event.getNodeName() + "/" + fname); 
	NativeFileSys.symlink(rel, afile);
      }
      catch(IOException ex) {
	throw new PipelineException
	  ("I/O ERROR: \n" + 
	   "  While attempting to link the node event file (" + afile + ")...\n" + 
	   "    " + ex.getMessage());
      }
    }
  }
  
  /**
   * Read all node events for the given nodes which where created within the given time
   * interval. 
   * 
   * @param timer
   *   The task timer.
   * 
   * @param names 
   *   Limit the events to those associated with the given fully resolved node names or
   *   <CODE>null</CODE> for all nodes.
   * 
   * @param users
   *   Limit the events to those generated by the given user names or
   *   <CODE>null</CODE> for all users.
   * 
   * @param interval
   *   Limit the events to those which occured within the given time interval or 
   *   <CODE>null</CODE> for all times.
   * 
   * @throws PipelineException
   *   If unable to read the node event files.
   */ 
  private MappedLinkedList<Long,BaseNodeEvent> 
  readNodeEvents
  (
   TaskTimer timer, 
   TreeSet<String> names, 
   TreeSet<String> users, 
   TimeInterval interval
  ) 
    throws PipelineException
  {
    long start  = 0L;
    long finish = Long.MAX_VALUE;
    if(interval != null) {
      start  = interval.getStartStamp();
      finish = interval.getEndStamp();
    }

    MappedLinkedList<Long,BaseNodeEvent> events = new MappedLinkedList<Long,BaseNodeEvent>();

    timer.acquire();
    synchronized(pNodeEventFileLock) {
      timer.resume();

      /* get the node files within the interval for the given nodes */ 
      MappedSet<Long,File> nameFiles = null; 
      if(names != null) {
	nameFiles = new MappedSet<Long,File>();
	for(String name : names) {
	  File dir = new File(pNodeDir, "events/nodes" + name);
	  scanNodeEventDir(dir, start, finish, nameFiles);
	}
      }

      /* get the user files within the interval for the given users */ 
      MappedSet<Long,File> userFiles = null; 
      if(users != null) {
	userFiles = new MappedSet<Long,File>();
	for(String user : users) 
	  scanUserNodeEventDirs(user, start, finish, userFiles);
      }

      /* determine the event files to read */ 
      MappedSet<Long,File> eventFiles = null;
      if(nameFiles == null) {
	/* all events within the interval */ 
	if(userFiles == null) {
	  eventFiles = new MappedSet<Long,File>();

	  File dir = new File(pNodeDir, "events/authors");
	  File subdirs[] = dir.listFiles();
          if(subdirs != null) {
            int wk;
            for(wk=0; wk<subdirs.length; wk++) {
              File sdir = subdirs[wk];
              if(sdir.isDirectory()) 
                scanUserNodeEventDirs(sdir.getName(), start, finish, eventFiles);
            }
          }
          else {
            LogMgr.getInstance().logAndFlush
              (LogMgr.Kind.Ops, LogMgr.Level.Severe,
               "Unable to determine the contents of the User Event directory " + 
               "(" + dir + ")!"); 
          }          
	}
	
	/* all events for the given users within the interval */ 
	else {
	  eventFiles = userFiles;
	}
      }
      else {
	/* all events for the given nodes within the interval */ 
	if(userFiles == null) {
	  eventFiles = nameFiles;
	}

	/* all events included in both the given nodes and users within the interval */ 
	else {
	  eventFiles = new MappedSet<Long,File>();
	  for(Long stamp : userFiles.keySet()) {
	    if(nameFiles.containsKey(stamp)) {
              for(File nfile : nameFiles.get(stamp))
                eventFiles.put(stamp, nfile); 
            }
	  }
	}
      }
      
      /* read the matching event files */ 
      for(Long stamp : eventFiles.keySet()) {
        for(File nfile : eventFiles.get(stamp)) {
          LogMgr.getInstance().log
            (LogMgr.Kind.Glu, LogMgr.Level.Finer,
             "Reading Node Event File: " + nfile); 

          BaseNodeEvent e = null;
          try {
            e = (BaseNodeEvent) GlueDecoderImpl.decodeFile("Event", nfile);
          }	
          catch(GlueException ex) {
            throw new PipelineException(ex);
          }
          
          if(e != null) 
            events.put(e.getTimeStamp(), e);
        }
      }
    }

    return events;
  }

  /**
   * Collect the names of the files within the given user node event subdirectories which are 
   * within the given time interval.
   */ 
  private void 
  scanUserNodeEventDirs
  (
   String user,
   long start, 
   long finish, 
   MappedSet<Long,File> found
  )  
  {
    GregorianCalendar calendar = new GregorianCalendar();

    File dir = new File(pNodeDir, "events/authors/" + user);
    File subdirs[] = dir.listFiles();
    if(subdirs != null) {
      int wk;
      for(wk=0; wk<subdirs.length; wk++) {
        File sdir = subdirs[wk];
        
        if(sdir.isDirectory()) {
          Long dstart  = null;
          Long dfinish = null;
          try {
            String sname = sdir.getName();
            int year  = Integer.parseInt(sname.substring(0, 4));
            int month = Integer.parseInt(sname.substring(4, 6));
            int day   = Integer.parseInt(sname.substring(6, 8));
	  
            calendar.clear();
            calendar.set(year, month-1, day);
            dstart = calendar.getTimeInMillis(); 
	  
            calendar.add(Calendar.DAY_OF_MONTH, 1); 
            dfinish = calendar.getTimeInMillis(); 
          }
          catch(Exception ex) {
            LogMgr.getInstance().logAndFlush
              (LogMgr.Kind.Glu, LogMgr.Level.Warning,
               "Illegal node event directory (" + sdir + ") encountered:\n" + 
               "  " + ex.getMessage());
          }
	
          if((dstart != null) && (dfinish != null) && 
             (start <= dfinish) && (finish >= dstart)) {
            scanNodeEventDir(sdir, start, finish, found);
          }
        }
      }
    }
    else {
      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Ops, LogMgr.Level.Severe,
         "Unable to determine the contents of the User Event subdirectory (" + dir + ") " + 
         "for user (" + user + ")!"); 
    }  
  }
 
  /** 
   * Collect the names of the files within the given node event directory which are 
   * within the given time interval.
   */
  private void 
  scanNodeEventDir
  (
   File dir, 
   long start, 
   long finish, 
   MappedSet<Long,File> found
  )
  {
    File files[] = dir.listFiles();
    if(files != null) {
      int wk;
      for(wk=0; wk<files.length; wk++) {
        File file = files[wk];
        if(file.isFile()) {
          String fname = file.getName();
          String parts[] = fname.split("\\.");

          Long stamp = null;
          if(parts.length >= 1) {
            try {
              stamp = Long.parseLong(parts[0]); 
            }
            catch(NumberFormatException ex) {
              LogMgr.getInstance().logAndFlush
                (LogMgr.Kind.Glu, LogMgr.Level.Warning,
                 "Illegal node event file (" + file + ") encountered:\n" + 
                 "  " + ex.getMessage());
            }
          }
          else {
            LogMgr.getInstance().logAndFlush
              (LogMgr.Kind.Glu, LogMgr.Level.Warning,
               "Illegal node event file (" + file + ") encountered:"); 
          }
	
          if((stamp != null) && (stamp >= start) && (stamp <= finish)) 
            found.put(stamp, file);
        }
      }
    }
    else {
      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Ops, LogMgr.Level.Severe,
         "Unable to determine the contents of the Node Event subdirectory (" + dir + ")!"); 
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
    if(file.isFile()) {
      if(!file.delete())
	throw new PipelineException
	  ("Unable to remove the old job/group IDs file (" + file + ")!");
    }

    LogMgr.getInstance().log
      (LogMgr.Kind.Glu, LogMgr.Level.Finer,
       "Writing Next IDs.");

    try {
      TreeMap<String,Long> table = new TreeMap<String,Long>();
      synchronized(pQueueSubmitLock) {
        table.put("JobID",      pNextJobID);
        table.put("JobGroupID", pNextJobGroupID);
      }
      
      GlueEncoderImpl.encodeFile("NextIDs", table, file);
    }
    catch(GlueException ex) {
      throw new PipelineException(ex);
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
    if(file.isFile()) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Reading Next IDs.");

      try {
        TreeMap<String,Long> table =
          (TreeMap<String,Long>) GlueDecoderImpl.decodeFile("NextIDs", file);

	synchronized(pQueueSubmitLock) {
	  pNextJobID      = table.get("JobID");
	  pNextJobGroupID = table.get("JobGroupID");
	}
        
	return;
      }	
      catch(GlueException ex) {
        throw new PipelineException(ex);
      }
    }
    
    synchronized(pQueueSubmitLock) {
      pNextJobID      = 1L;
      pNextJobGroupID = 1L;
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the node annotations to disk. <P> 
   * 
   * This method assumes that the write lock for the table of annotations for
   * the node already been acquired.
   * 
   * @param name
   *   The fully resolved node name.
   * 
   * @param annotations
   *   The annotations to write. 
   * 
   * @throws PipelineException
   *   If unable to write the annotations file or create the needed parent directories.
   */ 
  private void 
  writeAnnotations
  (
   String name, 
   TreeMap<String,BaseAnnotation> annotations
  ) 
    throws PipelineException
  {
    LogMgr.getInstance().log
      (LogMgr.Kind.Glu, LogMgr.Level.Finer,
       "Writing Annotations: " + name); 

    File file = new File(pNodeDir, "annotations" + name); 
    File dir  = file.getParentFile();

    try {
      synchronized(pMakeDirLock) {
	if(!dir.isDirectory()) 
	  if(!dir.mkdirs()) 
	    throw new IOException
	      ("Unable to create annotations directory (" + dir + ")!");
      }
      
      if(file.exists()) {
        if(!file.delete()) 
          throw new IOException
            ("Unable to overwrite the existing annotations file (" + file + ")!");
      }
      
      if(annotations.isEmpty()) 
        return;
      
      try {
        TreeMap<String,BaseAnnotation> table = new TreeMap<String,BaseAnnotation>();
        for(String aname : annotations.keySet()) 
          table.put(aname, new BaseAnnotation(annotations.get(aname)));
        
        GlueEncoderImpl.encodeFile("Annotations", table, file);
      }
      catch(GlueException ex) {
        throw new PipelineException(ex);
      }
    }
    catch(IOException ex) {
      throw new PipelineException
	("I/O ERROR: \n" + 
	 "  While attempting to write annotations for node (" + name + ") to file...\n" +
	 "    " + ex.getMessage());
    }
  }


  /**
   * Read annotations for a node from disk. <P> 
   * 
   * This method assumes that the write lock for the annotations has already been 
   * acquired.
   * 
   * @param name
   *   The fully resolved node name.
   * 
   * @return 
   *   The annotations or <CODE>null</CODE> if no annotation files exist.
   * 
   * @throws PipelineException
   *   If the annotations file is corrupted in some manner.
   */ 
  private TreeMap<String,BaseAnnotation> 
  readAnnotations
  (
   String name
  ) 
    throws PipelineException
  {
    File file = new File(pNodeDir, "annotations" + name);
    if(!file.isFile()) 
      return null;

    LogMgr.getInstance().log
      (LogMgr.Kind.Glu, LogMgr.Level.Finer,
       "Reading Annotations: " + name);

    TreeMap<String,BaseAnnotation> table = new TreeMap<String,BaseAnnotation>();
    try {
      table = 
        (TreeMap<String,BaseAnnotation>) GlueDecoderImpl.decodeFile("Annotations", file);
    }	
    catch(GlueException ex) {
      throw new PipelineException(ex);
    }
    
    TreeMap<String,BaseAnnotation> annotations = new TreeMap<String,BaseAnnotation>();
    {
      PluginMgrClient client = PluginMgrClient.getInstance();
      for(String aname : table.keySet()) {
        BaseAnnotation tannot = table.get(aname);
        BaseAnnotation annot = client.newAnnotation(tannot.getName(), 
                                                    tannot.getVersionID(), 
                                                    tannot.getVendor());
        annot.setParamValues(tannot);
        annotations.put(aname, annot);
      }
    }

    return annotations;
  }

      
  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the checked-in version to disk. <P> 
   * 
   * This method assumes that the write lock for the table of checked-in versions for
   * the node already been acquired.
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
    writeCheckedInVersion(vsn, false); 
  }

  /**
   * Write the checked-in version to disk. <P> 
   * 
   * This method assumes that the write lock for the table of checked-in versions for
   * the node already been acquired.
   * 
   * @param vsn
   *   The checked-in version to write.
   * 
   * @param allowOverwrite
   *   Whether allow replacement of an existing checked-in version file. 
   * 
   * @throws PipelineException
   *   If unable to write the checkedi-in version file or create the needed parent 
   *   directories.
   */ 
  private void 
  writeCheckedInVersion
  (
   NodeVersion vsn, 
   boolean allowOverwrite
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
      
      if(file.exists()) {
        if(allowOverwrite) {
          if(!file.delete()) 
            throw new IOException
              ("Unable to remove the existing checked-in version file (" + file + ") " + 
               "in order to replace it!"); 
        }
        else {
          throw new IOException
            ("Somehow a checked-in version file (" + file + ") already exists!");
        }
      }
      
      try {
        GlueEncoderImpl.encodeFile("NodeVersion", vsn, file);
      }
      catch(GlueException ex) {
        throw new PipelineException(ex);
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
   * acquired.
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
    if(files != null) {
      int wk;
      for(wk=0; wk<files.length; wk++) {
        if(!files[wk].isFile()) 
          throw new PipelineException
            ("Somehow the node version file (" + files[wk] + ") was not a regular file!");

        NodeVersion vsn = null;
        try {
          vsn = (NodeVersion) GlueDecoderImpl.decodeFile("NodeVersion", files[wk]);
        }	
        catch(GlueException ex) {
          throw new PipelineException(ex);
        }

        if(table.containsKey(vsn.getVersionID()))
          throw new PipelineException
            ("Somehow the version (" + vsn.getVersionID() + ") of node (" + name + ") " + 
             "was represented by more than one file!");
      
        /* insure that checksums are embedded */ 
        if(vsn.getCheckSums().isEmpty()) {
          LogMgr.getInstance().log
            (LogMgr.Kind.Sum, LogMgr.Level.Warning,
             "Adding per-file checksums to node version file: " + files[wk]); 
        
          try {
            TreeMap<String,CheckSum> checksums = new TreeMap<String,CheckSum>(); 
          
            Path cdir = 
              new Path(PackageInfo.sProdPath, 
                       "checksum/repository" + vsn.getName() + "/" + vsn.getVersionID());
            for(FileSeq fseq : vsn.getSequences()) {
              for(Path path : fseq.getPaths()) {
                Path cpath = new Path(cdir, path); 
                byte[] bytes = CheckSum.readBytes(cpath); 
                checksums.put(path.toString(), new CheckSum(bytes));
              }
            }
          
            NodeVersion fixed = new NodeVersion(vsn, checksums); 
            writeCheckedInVersion(fixed, true); 
            vsn = fixed;
          }
          catch(Exception ex) {
            LogMgr.getInstance().log
              (LogMgr.Kind.Sum, LogMgr.Level.Severe,
               (Exceptions.getFullMessage
                ("Unable to add per-file checksums to node version file: " + files[wk], ex)));
          }
        }

        table.put(vsn.getVersionID(), new CheckedInBundle(vsn));
      }
    }
    else {
      throw new PipelineException
        ("Unable to determine the contents of the node database directory (" + dir + ")!");
    }

    return table;
  }
      

  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the working version to disk. <P> 
   * 
   * This method assumes that the write lock for the working version has already been 
   * acquired.
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

    if (!id.getName().equals(mod.getName())) {
      PipelineException ex = new PipelineException
        ("Error trying to write the working glue file.  The name of the nodeID " +
         "(" + id.getName() + ") did not match the name contained in the nodeMod " +
         "(" + mod.getName() + ").");
      LogMgr.getInstance().logAndFlush
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 Exceptions.getFullMessage(ex));
      throw ex;
    }
      

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

      if(file.exists())  {
        if(backup.exists())
          if(!backup.delete()) 
            throw new IOException
              ("Unable to remove the backup working version file (" + backup + ")!");

        if(!file.renameTo(backup)) 
          throw new IOException
            ("Unable to backup the current working version file (" + file + ") to the " + 
             "the file (" + backup + ")!");
      }
            
      try {
        GlueEncoderImpl.encodeFile("NodeMod", mod, file);
      }
      catch(GlueException ex) {
        throw new PipelineException(ex);
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
   * acquired.
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
      if(file.isFile()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Reading Working Version: " + id);

	try { 
	  return ((NodeMod) GlueDecoderImpl.decodeFile("NodeMod", file));
	}
	catch(Exception ex) {
	  LogMgr.getInstance().logAndFlush
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	    "The working version file (" + file + ") appears to be corrupted:\n" + 
	     "  " + ex.getMessage());
	  
	  if(backup.isFile()) {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	       "Reading Working Version (Backup): " + id);

	    NodeMod mod = null;
	    try {
	      mod = (NodeMod) GlueDecoderImpl.decodeFile("NodeMod", backup);
	    }
	    catch(Exception ex2) {
	      LogMgr.getInstance().logAndFlush
		(LogMgr.Kind.Glu, LogMgr.Level.Severe,
		"The backup working version file (" + backup + ") appears to be " + 
		 "corrupted:\n" +
		 "  " + ex.getMessage());
	      
	      throw ex;
	    }
	    
	    LogMgr.getInstance().logAndFlush
	      (LogMgr.Kind.Glu, LogMgr.Level.Warning,
	       "Successfully recovered the working version from the backup file " + 
	       "(" + backup + ")\n" + 
	       "Renaming the backup to (" + file + ")!");
	    
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
	    LogMgr.getInstance().logAndFlush
	      (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	      "The backup working version file (" + backup + ") does not exist!");
	
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
   * Write the checksum cache for files associated with the working version to disk. <P> 
   * 
   * This method assumes that the write lock for the checksum cache version has already been 
   * acquired.
   * 
   * @param cache
   *   The checksum cache.
   * 
   * @throws PipelineException
   *   If unable to write the cache file or create the needed parent directories.
   */ 
  private void 
  writeCheckSumCache
  (
   CheckSumCache cache
  ) 
    throws PipelineException
  {
    NodeID nodeID = cache.getNodeID();

    LogMgr.getInstance().log
      (LogMgr.Kind.Glu, LogMgr.Level.Finer,
       "Writing Checksum Cache for Working Version: " + nodeID);

    Path ipath = new Path(nodeID.getName());
    File dir   = new File(pNodeDir, "checksum" + nodeID.getWorkingParent());
    File file  = new File(dir, ipath.getName()); 

    try {
      synchronized(pMakeDirLock) {
	if(!dir.isDirectory()) 
	  if(!dir.mkdirs()) 
	    throw new IOException
	      ("Unable to create working checksum directory (" + dir + ")!");
      }
      
      if(file.exists()) 
        if(!file.delete()) 
          throw new PipelineException
            ("Unable to overwrite the existing checksum cache file (" + file + ")!");

      try {
        GlueEncoderImpl.encodeFile("CheckSumCache", cache, file);
      }
      catch(GlueException ex) {
        throw new PipelineException(ex);
      }
    }
    catch(IOException ex) {
      throw new PipelineException
	("I/O ERROR: \n" + 
	 "  While attempting to write checksum cache for working version (" + nodeID + ") " + 
         "to file...\n" +
	 "    " + ex.getMessage());
    }
  }


  /**
   * Read the checksum cache for files associated with the working version from disk. <P> 
   * 
   * This method assumes that the write lock for the checksum cache has already been 
   * acquired.
   * 
   * @param nodeID 
   *   The unique working version identifier.
   * 
   * @return 
   *   The checksum cache or <CODE>null</CODE> if no cache file.
   * 
   * @throws PipelineException
   *   If the cache files are corrupted in some manner.
   */ 
  private CheckSumCache
  readCheckSumCache
  (
   NodeID nodeID
  ) 
    throws PipelineException
  {
    Path ipath = new Path(nodeID.getName());
    File dir   = new File(pNodeDir, "checksum" + nodeID.getWorkingParent());
    File file  = new File(dir, ipath.getName()); 
    
    if(!file.isFile())
      return null;

    LogMgr.getInstance().log
      (LogMgr.Kind.Glu, LogMgr.Level.Finer,
       "Reading Checksum Cache for Working Version: " + nodeID);
    
    try { 
      return ((CheckSumCache) GlueDecoderImpl.decodeFile("CheckSumCache", file));
    }
    catch(GlueException ex) {
      throw new PipelineException(ex);
    }
  }
   
  /**
   * Create a checksum cache for files associated with the working version from the
   * deprecated per-file binary checksum files. <P> 
   * 
   * This method assumes that the write lock for the checksum cache has already been 
   * acquired.
   * 
   * @param nodeID 
   *   The unique working version identifier.
   * 
   * @param mod 
   *   The working version of the node.
   * 
   * @return 
   *   The checksum cache or <CODE>null</CODE> if no cache file.
   * 
   * @throws PipelineException
   *   If the cache files are corrupted in some manner.
   */ 
  private void 
  upgradeDeprecatedCheckSumCache
  (
   NodeID nodeID, 
   NodeMod mod 
  ) 
    throws PipelineException
  {
    Path ipath = new Path(nodeID.getName());
    File dir   = new File(pNodeDir, "checksum" + nodeID.getWorkingParent());
    File file  = new File(dir, ipath.getName()); 
    
    if(file.isFile())
      return; 

    try {
      CheckSumCache cache = null;
      
      Path cdir = new Path(PackageInfo.sProdPath, "checksum" + nodeID.getWorkingParent());
      for(FileSeq fseq : mod.getSequences()) {
        for(Path path : fseq.getPaths()) {
          Path cpath = new Path(cdir, path); 
          NativeFileStat check = new NativeFileStat(cpath); 
          
          if(check.isFile()) {
            if(cache == null) {
              LogMgr.getInstance().log
                (LogMgr.Kind.Sum, LogMgr.Level.Warning,
                 "Adding per-file checksums to working cache file: " + file); 
              cache = new CheckSumCache(nodeID); 
            }
            
            byte[] bytes = CheckSum.readBytes(cpath); 
            long stamp = check.lastCriticalChange(mod.getLastCTimeUpdate());
            cache.add(path.toString(), new TransientCheckSum(bytes, stamp)); 
          }
        }
      }
        
      if(cache != null) 
        writeCheckSumCache(cache); 
    }
    catch(Exception ex) {
      LogMgr.getInstance().log
        (LogMgr.Kind.Sum, LogMgr.Level.Severe,
         (Exceptions.getFullMessage
          ("Unable to add per-file checksums to working checksum cache: " + file, ex)));
    }
  }
   
  /**
   * Remove the checksum cache for files associated with the working version to disk. <P> 
   * 
   * This method assumes that the write lock for the checksum cache has already been 
   * acquired.
   * 
   * @param nodeID 
   *   The unique working version identifier.
   * 
   * @param cache
   *   The checksum cache.
   * 
   * @throws PipelineException
   *   If unable to write the cache file or create the needed parent directories.
   */ 
  private void 
  removeCheckSumCache
  (
   NodeID nodeID
  ) 
    throws PipelineException
  {
    LogMgr.getInstance().log
      (LogMgr.Kind.Glu, LogMgr.Level.Finer,
       "Removing Checksum Cache for Working Version: " + nodeID);

    Path npath = new Path(nodeID.getName());
    File dir   = new File(pNodeDir, "checksum" + nodeID.getWorkingParent());
    File file  = new File(dir, npath.getName()); 
    File root  = new File(pNodeDir, "checksum/working/" + 
                          nodeID.getAuthor() + "/" + nodeID.getView());

    /* remove the checksum file */ 
    if(file.exists()) 
      if(!file.delete()) 
        throw new PipelineException
          ("Unable to remove the existing checksum cache file (" + file + ")!");

    /* clean up any now empty directories */ 
    deleteEmptyParentDirs(root, dir); 
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the downstream links to disk (if any exist). <P> 
   * 
   * This method assumes that the write lock for the downstream links has already been 
   * acquired.
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
    if(!links.hasAny()) {
      removeDownstreamLinks(links.getName());
      return;
    }

    File file = new File(pNodeDir, "downstream/" + links.getName());
    File dir  = file.getParentFile();
    
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

      try {
        GlueEncoderImpl.encodeFile("DownstreamLinks", links, file);
      }
      catch(GlueException ex) {
        throw new PipelineException(ex);
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
   * This method assumes that the write lock for the downstream links has already been 
   * acquired.
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
      if(file.isFile()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Reading Downstream Links: " + name);

        try {
          return ((DownstreamLinks) GlueDecoderImpl.decodeFile("DownstreamLinks", file));
        }	
        catch(GlueException ex) {
          throw new PipelineException(ex);
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


  /**
   * Remove the downstream link file. 
   * 
   * This method assumes that the write lock for the downstream links has already been 
   * acquired.
   * 
   * @param name
   *   The name of the node who's downstream links are to be deleted. 
   * 
   * @throws PipelineException
   *   If unable to delete the downstream links file.
   */ 
  private void 
  removeDownstreamLinks
  (
   String name
  ) 
    throws PipelineException
  {
    File file = new File(pNodeDir, "downstream" + name);
    if(file.isFile()) {
      if(!file.delete())
        throw new PipelineException
          ("Unable to remove the downstream links file (" + file + ")!");
    }
  }
    


  /*----------------------------------------------------------------------------------------*/
  /*   T A S K S                                                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Recursively search the checked-in node database directories adding the names of all
   * nodes found to a FIFO to be processed by multiple CheckedInReaderTask threads.
   */ 
  private 
  class CheckedInScannerTask
    extends Thread
  {
    public
    CheckedInScannerTask
    ( 
     LinkedBlockingQueue<String> found,
     AtomicBoolean uponError, 
     AtomicBoolean doneScanning
    ) 
    {
      super("MasterMgr:CheckedInScannerTask"); 

      pFound = found;
      pUponError = uponError;
      pDoneScanning = doneScanning; 
      pNumNodes = 0L;
    }

    @Override
    public void 
    run() 
    {
      TaskTimer timer = new TaskTimer(); 
      try {
        File root = new File(pNodeDir, "repository"); 
        collectNodeNames(root.getPath(), root);
      }
      catch(PipelineException ex) {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Ops, LogMgr.Level.Severe,
           ex.getMessage());
        pUponError.set(true);
        return;
      }
      catch(Exception ex) {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Ops, LogMgr.Level.Severe,
           Exceptions.getFullMessage("Checked-In Scan Aborted:", ex));
        pUponError.set(true);
        return;
      }
      finally {
        pDoneScanning.set(true);
      }

      LogMgr.getInstance().taskEnd
        (timer, LogMgr.Kind.Ops, 
         "Scanned " + ByteSize.longToFloatString(pNumVersions) + " Checked-In Versions"); 
    }

    private void 
    collectNodeNames
    (
     String prefix, 
     File dir
    ) 
      throws PipelineException
    {
      boolean allDirs  = true;
      boolean allFiles = true;
      
      File files[] = dir.listFiles(); 
      if(files != null) {
        for(File file : files) {
          if(file.isDirectory()) 
            allFiles = false;
          else if(file.isFile()) 
            allDirs = false;
          else {
            throw new PipelineException
              ("Unknown type of file (" + file + ") encountered in the checked-in node " + 
               "database!");
          }
        }
        
        if(allFiles) {
          String full = dir.getPath();  
          pFound.add(full.substring(prefix.length()));
          pNumNodes++;
          pNumVersions += files.length;
        }
        else if(allDirs) {
          for(File file : files) 
            collectNodeNames(prefix, file); 
        }
        else {
          throw new PipelineException
            ("Somehow, the checked-in node database directory (" + dir + ") contained " + 
             "mixtures of files and directories when it should have been either all files " + 
             "or all directories!"); 
        }
      }
      else {
        throw new PipelineException
          ("Unable to determine the contents of the Checked-In Node directory " + 
           "(" + dir + ")!"); 
      }         
    }

    private LinkedBlockingQueue<String> pFound;
    private AtomicBoolean pUponError;
    private AtomicBoolean pDoneScanning;
    private long pNumNodes;
    private long pNumVersions;
  }

  /**
   * Read the checked-in node database files for nodes previously identified by the 
   * CheckedInScannerTask thread to initialize the node naming and downstream caches.
   */ 
  private 
  class CheckedInReaderTask
    extends Thread
  {
    public
    CheckedInReaderTask
    ( 
     LinkedBlockingQueue<String> found,
     AtomicBoolean uponError, 
     AtomicBoolean doneScanning
    ) 
    {
      super("MasterMgr:CheckedInReaderTask"); 

      pFound = found;
      pUponError = uponError;
      pDoneScanning = doneScanning; 
    }

    @Override
    public void 
    run() 
    {
      TaskTimer timer = new TaskTimer(); 
      long counter = 0L;
      try {
        while(true) {
          String name = pFound.poll(10L, TimeUnit.MILLISECONDS);
          if(name == null) {
            if(pDoneScanning.get()) 
              break;
          }
          else {
            TreeMap<VersionID,CheckedInBundle> table = readCheckedInVersions(name);
            for(VersionID vid : table.keySet()) {
              NodeVersion vsn = table.get(vid).getVersion();
              
              for(LinkVersion link : vsn.getSources()) { 
                timer.acquire();
                synchronized(pDownstream) {
                  timer.resume();
                  
                  DownstreamLinks dsl = pDownstream.get(link.getName());
                  if(dsl == null) {
                    dsl = new DownstreamLinks(link.getName());
                    pDownstream.put(dsl.getName(), dsl);
                  }
                  
                  dsl.addCheckedIn(link.getVersionID(), name, vid);
                }
              }
              
              pNodeTree.addCheckedInNodeTreePath(vsn);
              
              if(vsn.isIntermediate()) {
                timer.acquire();
                synchronized(pIntermediate) {
                  timer.resume();
                  
                  TreeSet<VersionID> vids = pIntermediate.get(name); 
                  if(vids == null) {
                    vids = new TreeSet<VersionID>();
                    pIntermediate.put(name, vids);
                  }
                  
                  vids.add(vid);             
                }
              }
              
              counter++;
            }
          }
        }
      }
      catch(PipelineException ex) {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Ops, LogMgr.Level.Severe,
           ex.getMessage());
        pUponError.set(true);
        return;
      }
      catch(Exception ex) {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Ops, LogMgr.Level.Severe,
           Exceptions.getFullMessage("Checked-In Cache Rebuild Aborted:", ex));
        pUponError.set(true);
        return;
      }

      LogMgr.getInstance().taskEnd
        (timer, LogMgr.Kind.Ops, 
         "Processed " + ByteSize.longToFloatString(counter) + " Checked-In Versions"); 
    }

    private LinkedBlockingQueue<String> pFound;
    private AtomicBoolean pUponError;
    private AtomicBoolean pDoneScanning;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Recursively search the working node database directories adding the names of all
   * nodes found to a FIFO to be processed by multiple WorkingReaderTask threads.
   */ 
  private 
  class WorkingScannerTask
    extends Thread
  {
    public
    WorkingScannerTask
    ( 
     LinkedBlockingQueue<NodeID> found,
     AtomicBoolean uponError, 
     AtomicBoolean doneScanning
    ) 
    {
      super("MasterMgr:WorkingScannerTask"); 

      pFound = found;
      pUponError = uponError;
      pDoneScanning = doneScanning; 
      pCounter = 0L;
    }

    @Override
    public void 
    run() 
    {
      TaskTimer timer = new TaskTimer(); 
      try {
        File root = new File(pNodeDir, "working"); 
        
	File authors[] = root.listFiles(); 
        if(authors != null) {
          int ak;
          for(ak=0; ak<authors.length; ak++) {
            File afile = authors[ak];
            if(!afile.isDirectory()) 
              throw new PipelineException
                ("Non-directory file found (" + afile +") in the root working area " + 
                 "directory (" + root + ")!"); 

            String author = afile.getName();
	  
            File views[] = afile.listFiles();  
            if(views != null) {
              int vk;
              for(vk=0; vk<views.length; vk++) { 
                File vfile = views[vk];
                if(!vfile.isDirectory())
                  throw new PipelineException
                    ("Non-directory file found (" + vfile +") in the root working area " + 
                     "directory (" + root + ") for user (" + author + ")!"); 
                String view = vfile.getName();
                
                collectNodeIDs(author, view, vfile.getPath(), vfile);
              }
            }
            else {
              throw new PipelineException
                ("Unable to determine the contents of the Working Area directory " + 
                 "(" + afile + ") for user (" + author + ")!");  
            }
          }
        }
        else {
          throw new PipelineException
            ("Unable to determine the contents of the Root Working Area directory " + 
             "(" + root + ")!");
        }
      }
      catch(PipelineException ex) {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Ops, LogMgr.Level.Severe,
           ex.getMessage());
        pUponError.set(true);
        return;
      }
      catch(Exception ex) {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Ops, LogMgr.Level.Severe,
           Exceptions.getFullMessage("Working Scan Aborted:", ex));
        pUponError.set(true);
        return;
      }
      finally {
        pDoneScanning.set(true);
      }

      LogMgr.getInstance().taskEnd
        (timer, LogMgr.Kind.Ops, 
         "Scanned " + ByteSize.longToFloatString(pCounter) + " Working Versions"); 
    }

    private void 
    collectNodeIDs
    (
     String author, 
     String view, 
     String prefix, 
     File dir
    ) 
      throws PipelineException
    {
      File files[] = dir.listFiles(); 
      if(files != null) {
        for(File file : files) {
          if(file.isDirectory()) 
            collectNodeIDs(author, view, prefix, file);
          else {
            String path = file.getPath();
            if(!path.endsWith(".backup")) {
              String name = path.substring(prefix.length());
              NodeID nodeID = new NodeID(author, view, name);
              pFound.add(nodeID);
              pCounter++;
            }
          }
        }
      }
      else {
        throw new PipelineException
          ("Unable to determine the contents of the Working Area directory " + 
           "(" + dir + ") for user (" + author + ")!");  
      }
    }

    private LinkedBlockingQueue<NodeID> pFound;
    private AtomicBoolean pUponError;
    private AtomicBoolean pDoneScanning;
    private long pCounter;
  }

  /**
   * Read the working node database files for nodes previously identified by the 
   * WorkingScannerTask thread to initialize the node naming and downstream caches.
   */ 
  private 
  class WorkingReaderTask
    extends Thread
  {
    public
    WorkingReaderTask
    ( 
     LinkedBlockingQueue<NodeID> found,
     AtomicBoolean uponError, 
     AtomicBoolean doneScanning
    ) 
    {
      super("MasterMgr:WorkingReaderTask"); 

      pFound = found;
      pDoneScanning = doneScanning; 
      pUponError = uponError;
    }

    @Override
    public void 
    run() 
    {
      TaskTimer timer = new TaskTimer(); 
      long counter = 0L;
      try {
        while(true) {
          NodeID nodeID = pFound.poll(10L, TimeUnit.MILLISECONDS);
          if(nodeID == null) {
            if(pDoneScanning.get()) 
              break;
          }
          else {
            NodeMod mod = readWorkingVersion(nodeID); 
            if(mod != null) {
              CheckSumCache cache = readCheckSumCache(nodeID); 
              if(cache == null) 
                upgradeDeprecatedCheckSumCache(nodeID, mod); 
              
              for(LinkMod link : mod.getSources()) { 
                timer.acquire();
                synchronized(pDownstream) {
                  timer.resume();
                  
                  DownstreamLinks dsl = pDownstream.get(link.getName());
                  if(dsl == null) {
                    dsl = new DownstreamLinks(link.getName());
                    pDownstream.put(dsl.getName(), dsl);
                  }
                  
                  dsl.addWorking(nodeID.getAuthor(), nodeID.getView(), nodeID.getName()); 
                }  
              }
              
              addWorkingNodeTreePath(nodeID, mod.getPrimarySequence(), mod.getSequences());
              
              counter++;
            }
          }
        }
      }
      catch(PipelineException ex) {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Ops, LogMgr.Level.Severe,
           ex.getMessage());
        pUponError.set(true);
        return;
      }
      catch(Exception ex) {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Ops, LogMgr.Level.Severe,
           Exceptions.getFullMessage("Working Cache Rebuild Aborted:", ex));
        pUponError.set(true);
        return;
      }

      LogMgr.getInstance().taskEnd
        (timer, LogMgr.Kind.Ops, 
         "Processed " + ByteSize.longToFloatString(counter) + " Working Versions"); 
    }

    private LinkedBlockingQueue<NodeID> pFound;
    private AtomicBoolean pUponError;
    private AtomicBoolean pDoneScanning;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the currently cached downstream links from a FIFO at shutdown time.
   */ 
  private 
  class DownstreamWriterTask
    extends Thread
  {
    public
    DownstreamWriterTask
    ( 
     LinkedBlockingQueue<DownstreamLinks> found,
     AtomicBoolean uponError
    ) 
    {
      super("MasterMgr:DownstreamWriterTask"); 
      pFound = found;
      pUponError = uponError;
    }

    @Override
    public void 
    run() 
    {
      TaskTimer timer = new TaskTimer(); 
      long counter = 0L;
      try {
        while(true) {
          DownstreamLinks links = pFound.poll();
          if(links == null) 
            break;
          
          writeDownstreamLinks(links);

          counter++;
        }
      }
      catch(PipelineException ex) {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Ops, LogMgr.Level.Severe,
           ex.getMessage());
        pUponError.set(true);
        return;
      }
      catch(Exception ex) {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Ops, LogMgr.Level.Severe,
           Exceptions.getFullMessage("Downstream Links Write Aborted:", ex));
        pUponError.set(true);
        return;
      }

      LogMgr.getInstance().taskEnd
        (timer, LogMgr.Kind.Ops, 
         "Processed " + ByteSize.longToFloatString(counter) + " Nodes"); 
    }

    private LinkedBlockingQueue<DownstreamLinks> pFound;
    private AtomicBoolean pUponError;
  }


  /*----------------------------------------------------------------------------------------*/

  private 
  class RebuildOfflinedCacheTask
    extends Thread
  {
    /** 
     * Construct a new task.
     */
    public
    RebuildOfflinedCacheTask()
    {
      super("MasterMgr:RebuildOfflinedCacheTask"); 
    }

    public void 
    run() 
    {
      TaskTimer timer = new TaskTimer();
      try {
        
        FileMgrClient fclient = acquireFileMgrClient();
        try {
          /* scan the repository production data directories for offlined versions */ 
          TreeMap<String,TreeSet<VersionID>> offlined = fclient.getOfflined();

          /* wait until all checked-in node versions have been processed */ 
          pIntermediateTrigger.acquire();

          /* remove any intermediate node versions from those offlined */ 
          for(Map.Entry<String,TreeSet<VersionID>> entry : pIntermediate.entrySet()) {
            String name = entry.getKey();             
            TreeSet<VersionID> vids = offlined.get(name); 
            vids.removeAll(entry.getValue());
            if(vids.isEmpty()) 
              offlined.remove(name); 
          }

          pIntermediate = null;

          synchronized(pOfflinedLock) {
            pOfflined = offlined;
          }
        }
        finally {
          releaseFileMgrClient(fclient);
        }

        timer.suspend();
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Ops, LogMgr.Level.Info,
           "--- Offlined Task (Finished) ---\n" + 
           "  Offlined Cache Rebuild Succeeded.\n" + 
           "    Rebuilt in " + TimeStamps.formatInterval(timer.getTotalDuration()) + "\n" +
           "--------------------------------");
      }
      catch(InterruptedException ex) {
        timer.suspend();
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Ops, LogMgr.Level.Info,
           "--- Offlined Task (Finished) ---\n" + 
           "  Offlined Cache Rebuild Interrupted.\n" + 
           "    Time Spent " + TimeStamps.formatInterval(timer.getTotalDuration()) + "\n" + 
           "--------------------------------"); 
      }
      catch(Exception ex) {
        timer.suspend();
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Ops, LogMgr.Level.Info,
           "--- Offlined Task (Finished) ---\n" + 
           "  Offlined Cache Rebuild Aborted.\n" + 
           "    Time Spent " + TimeStamps.formatInterval(timer.getTotalDuration()) + "\n" +
           Exceptions.getFullMessage("Reason for Abort:", ex) + "\n" +
           "--------------------------------"); 
      }
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
     * Does this operation modify the current working version of the node?
     */ 
    public boolean
    writesWorking()
    {
      return false;
    }

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
   * The heavyweight client notifying status operation.
   */
  private
  class StatusNodeOp
    extends NodeOp
  {  
    /** 
     * Construct a new operation.
     */
    public
    StatusNodeOp
    (
     OpNotifiable opn
    ) 
    {
      super();

      pOpNotifier = opn;
    }

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
    @Override
    public void 
    perform
    (
     NodeStatus status, 
     TaskTimer timer
    )
      throws PipelineException
    {
      pOpNotifier.notify(timer, "Status: " + status.getName()); 
    }

    /**
     * The operation progress notifier.
     */
    protected OpNotifiable pOpNotifier; 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * The node check-in operation. <P>
   */
  public 
  class NodeCheckInOp
    extends StatusNodeOp
  {  
    /** 
     * Construct a new check-in operation.
     */
    public
    NodeCheckInOp
    ( 
     NodeCheckInReq req, 
     VersionID rootVersionID, 
     OpNotifiable opn
    ) 
    {
      super(opn);
      
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
    @Override
    public void 
    perform
    (
     NodeStatus status, 
     TaskTimer timer
    )
      throws PipelineException
    {
      String name = status.getName();
      NodeDetailsHeavy details = status.getHeavyDetails();
      if(details == null)
	throw new IllegalStateException(); 

      /* make sure node is in a Finished state */ 
      if(details.getOverallQueueState() != OverallQueueState.Finished) {
	throw new PipelineException
	  ("The node (" + name + ") was in a " + details.getOverallQueueState() + 
           " state.\n\n" +
	   "All nodes being Checked-In must be in a Finished state.");
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
	  NodeID nodeID  = status.getNodeID();
          NodeID rnodeID = pRequest.getNodeID();

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
          NodeVersion latest = null;
	  if(details.getOverallNodeState() == OverallNodeState.Pending) {
	    checkedIn = new TreeMap<VersionID,CheckedInBundle>();
	    vid = new VersionID();
	  }
	  else {
	    checkedIn = getCheckedInBundles(name);
	    latestID = checkedIn.lastKey();
            latest = checkedIn.get(latestID).getVersion();

	    VersionID.Level level = pRequest.getLevel();
	    if(level == null) 
	      level = VersionID.Level.Minor;
	    vid = new VersionID(latestID, level);
	  }

	  
	  //TODO need a toolset under development as well.
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
	      new CheckInExtFactory(rnodeID.getName(), nodeID, new NodeMod(work), 
				    pRequest.getLevel(), pRequest.getMessage());
	    performExtensionTests(timer, factory);
	  }

	  /* determine the checked-in revision numbers and locked status of 
	     the upstream nodes */ 
	  TreeMap<String,VersionID> lvids = new TreeMap<String,VersionID>();
	  TreeMap<String,Boolean> locked = new TreeMap<String,Boolean>();
	  for(NodeStatus lstatus : status.getSources()) {
	    NodeDetailsLight ldetails = lstatus.getLightDetails();   
	    lvids.put(lstatus.getName(), ldetails.getBaseVersion().getVersionID());
	    locked.put(lstatus.getName(), ldetails.getWorkingVersion().isLocked());
	  }
	  
	  /* build the file novelty table */ 
	  TreeMap<FileSeq,boolean[]> isNovel = new TreeMap<FileSeq,boolean[]>();
          {
            boolean allNovel = (!work.isIntermediate() && 
                                (latest != null) && latest.isIntermediate());
            
            for(FileSeq fseq : details.getFileSequences()) {
              FileState[] states = details.getFileStates(fseq);
              boolean flags[] = new boolean[states.length];

              int wk;
              for(wk=0; wk<states.length; wk++) {
                if(allNovel) {
                  flags[wk] = true;
                }
                else {
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
                      ("Somehow the working file (" + fseq.getFile(wk) + ") with a file " + 
                       "state of (" + states[wk].name() + ") was erroneously submitted " + 
                       "for check-in!");
                  }
                }
              }

              isNovel.put(fseq, flags);
            }
          }

	  /* check-in the files */ 
          TreeMap<String,CheckSum> checksums = null;
          TreeMap<String,Long[]> correctedStamps = work.getCorrectedStamps();
          TreeMap<FileSeq,NativeFileInfo[]> fileInfos = 
            new TreeMap<FileSeq,NativeFileInfo[]>();
	  {
	    FileMgrClient fclient = acquireFileMgrClient();
	    try {

              timer.acquire();
              LoggedLock clock = getCheckSumLock(nodeID);
              clock.acquireWriteLock();
              try {
                timer.resume();

                CheckSumBundle cbundle = getCheckSumBundle(nodeID); 

                CheckSumCache updatedCheckSums = 
                  fclient.checkIn(nodeID, work, vid, latestID, isNovel, 
                                  cbundle.getCache(), correctedStamps, fileInfos); 

                if(updatedCheckSums.wasModified()) {
                  try {
                    cbundle.setCache(updatedCheckSums); 
                    writeCheckSumCache(updatedCheckSums); 
                  }
                  catch(PipelineException ex) {
                    LogMgr.getInstance().log
                      (LogMgr.Kind.Sum, LogMgr.Level.Warning, ex.getMessage());
                  }
                }

                checksums = updatedCheckSums.getVersionCheckSums(); 
              }
              finally {
                clock.releaseWriteLock();
              }  
	    }
	    finally {
	      releaseFileMgrClient(fclient);
	    }
	  }

	  /* create a new checked-in version and write it disk */ 
	  NodeVersion vsn = 
	    new NodeVersion(work, vid, lvids, locked, isNovel, checksums, 
			    rnodeID.getAuthor(), pRequest.getMessage(), 
			    rnodeID.getName(), pRootVersionID, pRequest.getRequestor());

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
	  UpdateState[] updateStates = null;
	  {
	    for(FileSeq fseq : details.getFileSequences()) {
	      FileState fs[] = new FileState[fseq.numFrames()];

	      if(jobIDs == null) 
		jobIDs = new Long[fs.length];

	      if(queueStates == null) 
		queueStates = new QueueState[fs.length];

	      if(updateStates == null) 
		updateStates = new UpdateState[fs.length];

	      int wk;
	      for(wk=0; wk<fs.length; wk++) 
		fs[wk] = FileState.Identical;
	      
	      fileStates.put(fseq, fs);
	    }

	    {
	      int wk;
	      for(wk=0; wk<queueStates.length; wk++) {
		queueStates[wk]  = QueueState.Finished;
		updateStates[wk] = UpdateState.Unknown; 
              }
	    }
	  }

	  Long lastCTime = null;
          if (!work.isActionEnabled()) {
            lastCTime = work.getLastCTimeUpdate();
          }
	  
	  /* create a new working version and write it to disk */ 
	  NodeMod nwork = new NodeMod(vsn, work.getLastCriticalModification(), false, false, 
                                      lastCTime, correctedStamps);
	  writeWorkingVersion(nodeID, nwork);

	  /* update the working bundle */ 
	  working.setVersion(nwork);

          /* correct the timestamps for any symlinks we've made during the check-in */ 
          for(FileSeq fseq : work.getSequences()) {
            NativeFileInfo infos[] = fileInfos.get(fseq); 
            if(infos != null) {
              int wk=0;
              for(Path path : fseq.getPaths()) {
                if(infos[wk] != null) {
                  long ostamp = infos[wk].getTimeStamp();
                  infos[wk].setTimeStamp(nwork.correctStamp(path.toString(), ostamp));
                }
                wk++;
              }
            }
          }

	  /* update the node status details */ 
	  NodeDetailsHeavy ndetails = 
	    new NodeDetailsHeavy
                 (nwork, vsn, checkedIn.get(checkedIn.lastKey()).getVersion(), 
                  checkedIn.keySet(),
                  OverallNodeState.Identical, OverallQueueState.Finished, 
                  VersionState.Identical, PropertyState.Identical, LinkState.Identical, 
                  fileStates, fileInfos, details.getUpdateTimeStamps(), 
                  jobIDs, queueStates, updateStates);

	  status.setHeavyDetails(ndetails);

	  /* update the node tree entry */ 
	  pNodeTree.addCheckedInNodeTreePath(vsn);

	  /* set the checked-in downstream links from the upstream nodes to this node */ 
	  for(LinkVersion link : vsn.getSources()) { 
	    String lname = link.getName();

	    timer.acquire();
	    LoggedLock downstreamLock = getDownstreamLock(lname);
	    downstreamLock.acquireWriteLock();
	    try {
	      timer.resume();

	      DownstreamLinks dsl = getDownstreamLinks(lname);
	      dsl.addCheckedIn(link.getVersionID(), name, vsn.getVersionID());
	    }  
	    finally {
	      downstreamLock.releaseWriteLock();
	    }     
	  }

	  /* record event */ 
	  pPendingEvents.add(new CheckedInNodeEvent(nodeID, vid, pRequest.getLevel()));

	  /* post-op tasks */  
	  if(pHasExtTasks) 
	    startExtensionTasks(timer, new CheckInExtFactory(new NodeVersion(vsn)));

          pOpNotifier.notify(timer, "Checked-In: " + name);
	}
      }
    }

    /**
     * Does this operation modify the current working version of the node?
     */ 
    @Override
    public boolean
    writesWorking()
    {
      return true;
    }

    /**
     * Does this operation modify the checked-in versions of the node?
     */ 
    @Override
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
   * A set of counters for keeping track of the size and effectiveness of a database cache.
   */ 
  private class
  CacheCounters
  {
    /**
     * Construct a new cache counter.
     * 
     * @param title
     *   An identifier to use in log messages and warnings.
     * 
     * @param size
     *   The initial minimum size of the cache.
     * 
     * @param thresh
     *   The lower bounds of minimum size.
     */ 
    public 
    CacheCounters
    (
     String title, 
     long size, 
     long thresh
    ) 
    {
      pTitle = title; 
      pCurrent = 0L;

      if(thresh < 1L) 
        throw new IllegalArgumentException
          ("The threshold (" + thresh + ") must be positive!"); 
      pThreshold = thresh; 

      setLowerBounds(size); 
    }

    
    /*-- PREDICATES ------------------------------------------------------------------------*/
    
    public synchronized boolean
    hasExceededMin() 
    {
      return pCurrent > pMinimum;
    }

    public synchronized boolean
    hasExceededMax() 
    {
      return pCurrent >= pMaximum;
    }


    /*-- GETTERS ---------------------------------------------------------------------------*/

    public String
    getTitle()
    {
      return pTitle; 
    }

    public synchronized long
    getCurrent()
    {
      return pCurrent;
    }

    public synchronized long
    getHits()
    {
      return pHits; 
    }

    public synchronized long
    getMisses()
    {
      return pMisses; 
    }

    public synchronized long
    getMinimum()
    {
      return pMinimum;
    }

    public synchronized long
    getMaximum()
    {
      return pMaximum;
    }

    
    /*-- SETTERS ---------------------------------------------------------------------------*/

    /**
     * Set the minimum size of the cache to the given value and recompute the maximum based
     * on this new minimum.
     */ 
    public synchronized void
    setLowerBounds
    (
     long min
    )
    {
      double factor = pCacheFactor.get();
      pMinimum = Math.max(min, pThreshold); 
      pMaximum = Math.max(pMinimum+1, (long) (((double) pMinimum) / factor));
    }
    
    /**
     * Set the maximum size of the cache to the given value and recompute the minimum based
     * on this new maximum.
     */ 
    public synchronized void
    setUpperBounds
    (
     long max
    )
    {
      double factor = pCacheFactor.get();
      pMinimum = Math.max((long) (((double) max) * factor), pThreshold); 
      pMaximum = Math.max(pMinimum+1, (long) (((double) pMinimum) / factor));
    }
    
    /**
     * Dynamically adjust the min/max bounds of the cache based on usage.<P> 
     *  
     * @param reduce
     *   Whether to reduce the size of the cache (or increase it).
     */ 
    public synchronized void 
    adjustBounds
    (
     boolean reduce
    ) 
    {
      long omin = pMinimum; 
      long omax = pMaximum;

      if(reduce) 
        setUpperBounds(Math.min(pMinimum, pCurrent)); 
      else if(pCurrent > pMinimum) 
        setLowerBounds(pCurrent); 

      if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Mem, LogMgr.Level.Fine)) {
        if((omin != pMinimum) || (omax != pMaximum)) {
          double ratioMin = ((double) pMinimum) / ((double) omin);
          double deltaMin = (omin > pMinimum) ? -ratioMin+1.0 : ratioMin-1.0;
        
          double ratioMax = ((double) pMaximum) / ((double) omax);
          double deltaMax = (omax > pMaximum) ? -ratioMax+1.0 : ratioMax-1.0;

          LogMgr.getInstance().logAndFlush
            (LogMgr.Kind.Mem, LogMgr.Level.Fine, 
             pTitle + " [" + (reduce ? "Reduced" : "Increased") + "]: " + 
             "Min=" + omin + "->" + pMinimum + 
             "(" + String.format("%1$.1f", deltaMin*100.0) + "%)  " + 
             "Max=" + omax + "->" + pMaximum +
             "(" + String.format("%1$.1f", deltaMax*100.0) + "%)"); 
        }
      }
    }
      

    /*-- STATISTICS ------------------------------------------------------------------------*/

    public synchronized void
    resetHitMiss() 
    {
      pHits   = 0L; 
      pMisses = 0L;
    }

    public synchronized void 
    hit() 
    {
      pHits++; 
    }

    public synchronized void 
    hits
    (
     long amount
    ) 
    {
      pHits += amount; 
    }

    public synchronized long
    miss() 
    {
      pMisses++; 
      pCurrent++;
      pCacheTrigger.performed();
      return pCurrent; 
    }

    public synchronized long 
    free() 
    {
      pCurrent--;
      if(pCurrent < 0L) {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Mem, LogMgr.Level.Warning, 
           "Somehow the Current count of cached " + pTitle.toLowerCase() + " has become " +
           "negative!  Resetting it to zero..."); 
        pCurrent = 0L;
      }
      return pCurrent;
    }

    public synchronized void 
    emptySanityCheck() 
    {
      if(pCurrent > 0L)
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Mem, LogMgr.Level.Warning,
           "While attempting to reduce the " + pTitle.toLowerCase() + " cache size, the " + 
           "FIFO of objects to free ran out even though the counter still indicates that " + 
           "there are (" + pCurrent + ") objects being cached!  Resetting it to zero...");
      pCurrent = 0L;
    }

    public synchronized void 
    logStats() 
    {
      if(!LogMgr.getInstance().isLoggable(LogMgr.Kind.Mem, LogMgr.Level.Finer))
        return;

      long total = pHits + pMisses;
      double disk = (total > 0L) ? ((double) pMisses) / ((double) (total)) : 0.0;

      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Mem, LogMgr.Level.Finer,
         pTitle + ":\n" + 
	 "  ---- CACHE STATS -------------------\n" +
         "      Cached = " + pCurrent + "\n" + 
         "     Min/Max = " + pMinimum + "/" + pMaximum + "\n" + 
         "    Hit/Miss = " + pHits + "/" + pMisses + "\n" + 
         "        Disk = " + String.format("%1$.1f", disk*100.0) + "%\n" + 
	 "  ------------------------------------");
    }


    /*-- INTERNALS -------------------------------------------------------------------------*/

    private String pTitle; 
    private long pCurrent;
    private long pThreshold;
    private long pMinimum;
    private long pMaximum;
    private long pHits;
    private long pMisses;
  }

  /**
   * A helper class used to block the cache garbage collector thread until a certain number 
   * operations have been performed or a time interval expires.
   */ 
  private class
  CacheTrigger
  {
    public 
    CacheTrigger
    (
     long opsUntil
    )
    {
      pOpsLeft  = new Long(0L);
      pOpsUntil = new AtomicLong(opsUntil);
      pTrigger  = new Semaphore(0); 
    }

    public long
    getOpsUntil()
    {
      return pOpsUntil.get();
    }

    public void 
    setOpsUntil
    (
     long opsUntil     
    )
    {
      pOpsUntil.set(opsUntil); 
    }

    public void 
    performed()
    {
      synchronized(pOpsLeft) {
        pOpsLeft--;
        if(pOpsLeft < 0L) {
          pOpsLeft = pOpsUntil.get();
          pTrigger.release(); 
        }
      }
    }
    
    public void 
    block
    (
     long interval
    ) 
      throws InterruptedException
    {
      pTrigger.tryAcquire(1, interval, TimeUnit.MILLISECONDS);
    }

    private Long pOpsLeft;
    private AtomicLong pOpsUntil;
    private Semaphore  pTrigger; 
  }


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
      pVersion = mod;
    }

    /**
     * Get the working version.
     */
    public NodeMod
    getVersion()
    {
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
      pVersion = mod; 
    }
   
    /**
     * The working version of a node. 
     */ 
    private NodeMod  pVersion;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * The information related to checksums for the files associated with the working version 
   * of a node for a particular view owned by a particular user.
   */
  private class 
  CheckSumBundle
  {
    /** 
     * Construct a new checksum cache bundle. 
     */
    public 
    CheckSumBundle
    (
     NodeID nodeID
    ) 
    {
      pCache = new CheckSumCache(nodeID); 
    }

    /** 
     * Construct a new checksum cache bundle. 
     */
    public 
    CheckSumBundle
    (
     CheckSumCache cache
    ) 
    {
      pCache = cache; 
    }

    /**
     * Get the checksum cache.
     */
    public CheckSumCache
    getCache()
    {
      return pCache; 
    }
   
    /**
     * Set the checksum cache. 
     */
    public void
    setCache
    (
     CheckSumCache cache
    )
    {
      pCache = cache; 
    }
   
    /**
     * The checksum cache. 
     */ 
    private CheckSumCache  pCache; 
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
      pVersion = vsn;
    }

    /**
     * Get the checked-in version.
     */
    public NodeVersion
    getVersion()
    {
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
      pVersion = vsn; 
    }
   
    /**
     * The checked-in version of a node.
     */ 
    public NodeVersion  pVersion;
  }


 
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * A standardized date formatter.
   */ 
  private static SimpleDateFormat sTodayFormat = new SimpleDateFormat("yyyyMMdd"); 



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
   * be protected by this lock in read lock mode.  Any operation which require that the entire
   * contents of the database remain constant during the operation should acquire the write
   * mode lock. The scope of this lock should enclose all other locks for an operation. <P> 
   * 
   * This lock exists primary to support write-locked database backups and node deletion. <P> 
   */ 
  private LoggedLock  pDatabaseLock;

  /**
   * The amount of time to attempt to acquire the database write-lock during a
   * Delete operation before giving up. 
   */ 
  private static final long  sDeleteTimeout = 300000L;   /* 5-minutes */ 

  /**
   * The amount of time to attempt to acquire the database write-lock during a
   * Backup operation before giving up. 
   */ 
  private static final long  sBackupTimeout = 900000L;   /* 15-minutes */ 


  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Communicates network connection session related status with the MasterMgrSever.
   */ 
  private SessionControls pSessionControls;

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

  /**
   * The number of node reader threads to spawn during rebuild.
   */
  private int  pNodeReaderThreads; 

  /**
   * The number of node writer threads to spawn during shutdown.
   */
  private int  pNodeWriterThreads; 


  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The interval (in milliseconds) between the live synchronization of the database 
   * files associated with the Master Manager and optionally making a tarball of these 
   * backup copies.
   */ 
  private AtomicLong  pBackupSyncInterval; 

  /**
   * Used to allow interruptible blocking of the backup synchronzation task.
   */ 
  private Semaphore  pBackupSyncTrigger; 

  /**
   * When set, the path to the tarball in which the backup is archived. <P> 
   * 
   * If this is <CODE>null</CODE>, then the backup task just synchronizes the database
   * with the backup directory without holding locks.  If it has a value, then the 
   * synchronization is performed while holding the pDatabase.writeLock() and a tarball
   * is created with this given path.  The backup task will unset the value after
   * processing it.
   */ 
  private AtomicReference<Path>  pBackupSyncTarget; 
  

  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The combined work groups and adminstrative privileges.
   */ 
  private AdminPrivileges  pAdminPrivileges; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Table of locks provided for arbitrary user purposes, indexed by lock name.
   * 
   * Access to this field should be protected by a synchronized block.
   */
  private TreeMap<String,TrackedLock>  pNetworkLocks;


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
  //private DoubleMappedSet<String,VersionID,String>  pArchivedIn;  <--- Use this instead.

  /**
   * The timestamps of when each archive volume was created indexed by unique archive 
   * volume name.
   *	 
   * This table is rebuilt by scanning the archive GLUE files. <P> 

   * Access to this field should be protected by a synchronized block.
   */
  private TreeMap<String,Long>  pArchivedOn;

  /**
   * The timestamps of when versions from each archive volume was was created indexed by 
   * unique archive volume name.
   *	 
   * This table is rebuilt by scanning the restore output filenames. <P> 

   * Access to this field should be protected by a synchronized block.
   */
  private TreeMap<String,TreeSet<Long>>  pRestoredOn;
  //private MappedSet<String,Long>  pRestoredOn;   <--- Use this instead.

  /**
   * The per-node online/offline locks indexed by fully resolved node name. <P> 
   * 
   * These locks protect access to (and modification of) whether the versions of a node are 
   * currently online.  The per-node read-lock should be acquired for operations which 
   * require that the online/offline status not change during the operation.  The per-node 
   * write-lock should be acquired when changing the online/offline status of versions of a 
   * node.
   */
  private TreeMap<String,LoggedLock>  pOnlineOfflineLocks;

  /**
   * The fully resolved node names and revision numbers of the checked-in versions which
   * are currently offline.<P> 
   * 
   * This table is rebuild by scanning the repository for empty directories. <P> 
   * 
   * Access to pOfflined should be protected by a synchronized block on pOfflinedLock;
   * 
   * If (null), then the offlined cache is currently invalid and direct tests are required
   * to determine if a file is online or not.  Because of this, helper methods should be
   * used to determine if a node version which will take care of this instead of accessing
   * pOfflined directly.
   */ 
  private Object pOfflinedLock;
  private TreeMap<String,TreeSet<VersionID>>  pOfflined;
  //private MappedSet<String,VersionID> pOfflined;   <--- Use this instead.

  /**
   * The task responsible for rebuiling the offlined cache, if any.
   */
  private RebuildOfflinedCacheTask  pRebuildOfflinedCacheTask; 

  /**
   * The pending restore requests indexed by the fully resolved node names and 
   * revision numbers of the checked-in versions to restore. <P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */  
  private TreeMap<String,TreeMap<VersionID,RestoreRequest>>  pRestoreReqs;  
  //private TripleMap<String,VersionID,RestoreRequest>  pRestoreReqs;  <-- Use this instead.


  /**
   * Used to block the RebuildOfflinedCacheTask until the table of intermediate versions 
   * (pIntermediate) is complete.
   */ 
  private Semaphore  pIntermediateTrigger; 

  /**
   * The fully resolved node names and revision numbers of the checked-in versions which
   * are intermediate and therefore have no files.<P> 
   * 
   * This table is built up during a full rebuild to determine which of the offlined
   * versions determined by looking for empty repository directories are actually just 
   * intermediate.  After pOfflined is initialized, this field is no longer used and is 
   * set to <CODE>null</CODE>
   */ 
  private TreeMap<String,TreeSet<VersionID>> pIntermediate; 


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
  //private DoubleMap<String,OsType,Toolset>  pToolsets; <-- Use this instead.

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

  private TreeMap<String,PluginMenuLayout>  pAnnotationMenuLayouts;
  private PluginMenuLayout                  pDefaultAnnotationMenuLayout;

  private TreeMap<String,PluginMenuLayout>  pKeyChooserMenuLayouts;
  private PluginMenuLayout                  pDefaultKeyChooserMenuLayout;
  
  private TreeMap<String,PluginMenuLayout>  pBuilderCollectionMenuLayouts;
  private PluginMenuLayout                  pDefaultBuilderCollectionMenuLayout;
  

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
  private DoubleMap<String,VersionID,PluginSet>  pPackageAnnotationPlugins; 
  private DoubleMap<String,VersionID,PluginSet>  pPackageKeyChooserPlugins; 
  private DoubleMap<String,VersionID,PluginSet>  pPackageBuilderCollectionPlugins;

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
   * The minimum time a cycle of the event writer loop should take (in milliseconds).
   */ 
  private AtomicLong  pEventWriterInterval; 

  /**
   * Serializes access to the node event files.
   */ 
  private Object  pNodeEventFileLock; 

  /**
   * The events not yet written to disk. 
   * 
   * No locking is required.
   */
  private ConcurrentLinkedQueue<BaseNodeEvent>  pPendingEvents; 

  /**
   * The next available unique editing session identifier. 
   * 
   * Access to this field should be protected by a synchronized(pRunningEditors) block. 
   */ 
  private long  pNextEditorID;

  /**
   * The currently running Editors indexed by unique editing session ID. 
   * 
   * Access to this field should be protected by a synchronized block. 
   */                           
  private TreeMap<Long,EditedNodeEvent>  pRunningEditors; 
    

  /*----------------------------------------------------------------------------------------*/

  /**
   * The table of working area view names indexed by author user name. <P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */ 
  private TreeMap<String,TreeSet<String>>  pWorkingAreaViews;
  //private MappedSet<String,String>  pWorkingAreaViews; <-- Use this instead.

  /**
   * Maintains the the current table of used node names. <P> 
   * 
   * All methods of this class are synchronized so no manual locking is required.
   */ 
  private NodeTree  pNodeTree; 


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The maximum amount of time between cache garbage collections regardless of activity.
   */ 
  private AtomicLong  pCacheGCInterval; 

  /**
   * Used to block the cache garbage collector thread until a certain number operations have 
   * been performed or a time interval expires.
   */ 
  private CacheTrigger  pCacheTrigger; 

  /**
   * The minimum amount of heap memory (max - total + free) to maintain.
   */ 
  private AtomicLong  pMinFreeMemory;

  /**
   * The percentage [0-1] of the maximum size of a cache to set its minimum.
   */ 
  private AtomicReference<Double>  pCacheFactor;
  

  /**
   * The per-node locks indexed by fully resolved node name. <P> 
   * 
   * These locks protect the annotations for each node. The per-node read-lock should 
   * be acquired for operations which will only access the table of annotations for a node. 
   * The per-node write-lock should be acquired when adding new annotations, modifying or 
   * removing existing annotations for a node. 
   */
  private TreeMap<String,LoggedLock>  pAnnotationLocks;

  /**
   * The annotations associated with nodes indexed by fully resolved node name. 
   */ 
  private TreeMap<String,TreeMap<String,BaseAnnotation>>  pAnnotations;
  //private DoubleMap<String,String,BaseAnnotation>  pAnnotations;  <-- Use this instead.

  /**
   * A set of counters for keeping track of the size of effectiveness of the per-node
   * annotation cache.
   */ 
  private CacheCounters  pAnnotationCounters;

  /**
   * The fully resolved node names of annotations read from disk and placed into the
   * annotations cache in the order they were read. <P> 
   * 
   * When looking to reduce the size of the cache, the garbage collector will free 
   * entries from this FIFO in the order they were originally read.
   */ 
  private LinkedBlockingDeque<String>  pAnnotationsRead;


  /**
   * The per-node locks indexed by fully resolved node name. <P> 
   * 
   * These locks protect the checked-in versions of each node. The per-node read-lock should 
   * be acquired for operations which will only access the table of checked-in versions of a 
   * node.  The per-node write-lock should be acquired when adding new checked-in versions to
   * the table of checked-in versions for a node.  No existing checked-in bundle entries in 
   * these tables should ever be modified.
   */
  private TreeMap<String,LoggedLock>  pCheckedInLocks;

  /**
   * The checked-in version related information of nodes indexed by fully resolved node 
   * name and revision number.
   */ 
  private TreeMap<String,TreeMap<VersionID,CheckedInBundle>>  pCheckedInBundles;
  //private DoubleMap<String,VersionID,CheckedInBundle>  pCheckedInBundles;  <-- Use this 
  
  /**
   * A set of counters for keeping track of the size of effectiveness of the checked-in
   * node version cache. 
   */ 
  private CacheCounters  pCheckedInCounters;

  /**
   * The fully resolved node names of checked-in versions read from disk and placed into the
   * cache in the order they were read. <P> 
   * 
   * When looking to reduce the size of the cache, the garbage collector will free 
   * entries from this FIFO in the order they were originally read.
   */ 
  private LinkedBlockingDeque<String>  pCheckedInRead;


  /**
   * The per-working version locks indexed by working version node ID. <P> 
   * 
   * These locks protect the working version related information of nodes. The per-working
   * version read-lock should be acquired for operations which will only access this 
   * information. The per-working version write-lock should be acquired when creating new 
   * working versions, modifying the information associated with existing working versions 
   * or removing existing working versions.
   */
  private TreeMap<NodeID,LoggedLock>  pWorkingLocks;

  /**
   * The working version related information of nodes indexed by fully resolved node 
   * name and working version node ID.
   */ 
  private TreeMap<String,TreeMap<NodeID,WorkingBundle>>  pWorkingBundles;
  //private DoubleMap<String,NodeID,WorkingBundle>  pWorkingBundles;  <-- Use this instead.
 
  /**
   * A set of counters for keeping track of the size of effectiveness of the working
   * node version cache. 
   */ 
  private CacheCounters  pWorkingCounters;

  /**
   * The workig version IDs of the working node versions read from disk and placed into the
   * cache in the order they were read. <P> 
   * 
   * When looking to reduce the size of the cache, the garbage collector will free 
   * entries from this FIFO in the order they were originally read.
   */ 
  private LinkedBlockingDeque<NodeID>  pWorkingRead;


  /**
   * The per-working version checksum cache locks indexed by working version node ID. <P> 
   * 
   * These locks protect the checksum caches for files associated with the working versions
   * of nodes. The per-working version read-lock should be acquired for operations which will 
   * only access this information. The per-working version write-lock should be acquired when 
   * creating new checksum caches, modifying the contents of the cache or removing existing 
   * caches. 
   */
  private TreeMap<NodeID,LoggedLock>  pCheckSumLocks;
  
  /**
   * The checksum caches for files associated with each working version of a node 
   * indexed by fully resolved node name and working version node ID.
   */ 
  private DoubleMap<String,NodeID,CheckSumBundle>  pCheckSumBundles;
 
  /**
   * A set of counters for keeping track of the size of effectiveness of the working
   * version checksum cache. 
   */ 
  private CacheCounters  pCheckSumCounters;

  /**
   * The workig version IDs of the working version checksums read from disk and placed 
   * into the cache in the order they were read. <P> 
   * 
   * When looking to reduce the size of the cache, the garbage collector will free 
   * entries from this FIFO in the order they were originally read.
   */ 
  private LinkedBlockingDeque<NodeID>  pCheckSumsRead;


  /**
   * The per-node downstream links locks indexed by fully resolved node name. <P> 
   * 
   * These locks protect the cached downstream links of each node. The per-node read-lock 
   * should be acquired for operations which will only access the downstream links of a node.
   * The per-node write-lock should be acquired when adding or removing links for a node.
   */
  private TreeMap<String,LoggedLock>  pDownstreamLocks;
  
  /**
   * The table of downstream links indexed by fully resolved node name. <P> 
   * 
   * Access to this table should be protected by a synchronized block.
   */
  private TreeMap<String,DownstreamLinks>  pDownstream;

  
  
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
   * The connection to the queue manager daemon: <B>plqueuemgr</B>(1).
   */ 
  private Stack<QueueMgrControlClient> pQueueMgrClients;

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

