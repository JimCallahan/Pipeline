// $Id: QueueMgr.java,v 1.120 2009/07/06 10:25:26 jim Exp $

package us.temerity.pipeline.core;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Map.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.exts.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.message.*;
import us.temerity.pipeline.toolset.Toolset;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   M G R                                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The manager of queue jobs. <P>
 */
class QueueMgr
  extends BaseMgr
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new queue manager.
   * 
   * @param server
   *   The parent queue manager server.
   * 
   * @param rebuild
   *   Whether to ignore existing lock files.
   * 
   * @param collectorBatchSize
   *   The maximum number of job servers per collection sub-thread.
   * 
   * @param dispatcherInterval
   *   The minimum time a cycle of the dispatcher loop should take (in milliseconds).
   * 
   * @param nfsCacheInterval
   *   The minimum time to wait before attempting a NFS directory attribute lookup operation
   *   after a file in the directory has been created by another host on the network 
   *   (in milliseconds).  This should be set to the same value as the NFS (acdirmax) 
   *   mount option for the root production directory on the host running the Queue Manager.
   */
  public
  QueueMgr
  (
   QueueMgrServer server,
   boolean rebuild, 
   int collectorBatchSize,
   long dispatcherInterval, 
   long nfsCacheInterval
  ) 
  { 
    super(true); 

    pServer = server;
    pRebuild = rebuild;

    pCollectorBatchSize = new AtomicInteger(collectorBatchSize);
    pDispatcherInterval = new AtomicLong(dispatcherInterval);
    pNfsCacheInterval   = new AtomicLong(nfsCacheInterval);

    if(PackageInfo.sOsType != OsType.Unix)
      throw new IllegalStateException("The OS type must be Unix!");
    pQueueDir = PackageInfo.sQueuePath.toFile();

    pShutdownJobMgrs = new AtomicBoolean(false);

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Info,
       "Initializing [MasterMgr]...");

    init();
  }


  /*-- CONSTRUCTION HELPERS ----------------------------------------------------------------*/

  /**
   * Initialize a new instance.
   */ 
  private void 
  init()
  { 
    /* initialize the fields */ 
    {
      pMakeDirLock = new Object(); 

      pAdminPrivileges = new AdminPrivileges();
      pMasterMgrClient = new MasterMgrClient();

      pLicenseKeys = new TreeMap<String,LicenseKey>();

      pHardwareKeys         = new TreeMap<String, HardwareKey>();
      pHardwareGroups       = new TreeMap<String, HardwareGroup>();
      pHardwareChanged      = new AtomicBoolean(true);
      pDispHardwareKeyNames = new TreeSet<String>(); 
      pDispHardwareGroups   = new TreeMap<String,HardwareGroup>();
      pHardwareProfiles     = new TreeMap<Flags,HardwareProfile>();      

      pSelectionKeys         = new TreeMap<String,SelectionKey>();
      pSelectionGroups       = new TreeMap<String,SelectionGroup>();
      pSelectionSchedules    = new TreeMap<String,SelectionSchedule>();
      pSelectionChanged      = new AtomicBoolean(true);
      pDispSelectionKeyNames = new TreeSet<String>(); 
      pDispSelectionGroups   = new TreeMap<String,SelectionGroup>();
      pSelectionProfiles     = new TreeMap<Flags,SelectionProfile>();

      pQueueExtensions = new TreeMap<String,QueueExtensionConfig>();

      pToolsets = new DoubleMap<String,OsType,Toolset>();

      pHostsMod           = new TreeMap<String,QueueHostMod>(); 
      pOsTypeChanges      = new TreeMap<String,OsType>();
      pNumProcChanges     = new TreeMap<String,Integer>();
      pTotalMemoryChanges = new TreeMap<String,Long>();
      pTotalDiskChanges   = new TreeMap<String,Long>();
      pHosts              = new TreeMap<String,QueueHost>(); 
      pHostsInfo          = new TreeMap<String,QueueHostInfo>();
      pOrderedHosts       = new QueueHost[128];

      pLastSampleWritten = new AtomicLong(0L);
      pSamples           = new TreeMap<String,ResourceSampleCache>();
      pSampleFileLock    = new Object();

      pPreemptList = new LinkedBlockingDeque<Long>();
      pHitList     = new LinkedBlockingDeque<Long>();
      pPause       = new TreeSet<Long>();
      pResume      = new TreeSet<Long>();

      pWaiting  = new LinkedBlockingDeque<Long>();
      pReady    = new TreeMap<Long,JobProfile>();
      pJobRanks = new JobRank[1024]; 
      pRunning  = new DoubleMap<String,Long,MonitorTask>(); 

      pJobFileLocks = new TreeMap<Long,Object>();
      pJobs         = new TreeMap<Long,QueueJob>();
      
      pJobReqsChanges = new TreeMap<Long, JobReqs>();

      pJobInfoFileLocks = new TreeMap<Long,Object>();
      pJobInfo          = new TreeMap<Long,QueueJobInfo>();

      pNodeJobIDs = new TreeMap<NodeID,TreeMap<File,Long>>(); 

      pJobGroupFileLocks = new TreeMap<Long,Object>();
      pJobGroups         = new TreeMap<Long,QueueJobGroup>(); 

      pJobCounters = new QueueJobCounters(); 
    }

    try {
      /* make sure that the root queue directories exist */ 
      makeRootDirs();

      /* validate startup state */ 
      if(pRebuild) {  
        removeLockFile();   
      }
      else {
	File file = new File(pQueueDir, "queue/lock");
	if(file.exists()) 
	  throw new IllegalStateException
	    ("Another queue manager may already be running!\n" + 
	     "If you are certain this is not the case, restart using the --rebuild option!");
      }

      /* create the lock file */ 
      createLockFile();


      /* load and initialize the server extensions */ 
      initQueueExtensions();

      /* load the license and selection keys */ 
      initLicenseSelectionKeys(); 

      /* load the hosts if any exist */ 
      initHosts();
      
      /* run the scheduler to get everything all lined up and correct */
//      TaskTimer timer = new TaskTimer("Scheduler");
//      doScheduler(timer);
      
      /* initialize the job related tables from disk files */ 
      initJobTables();
    }
    catch(Exception ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 ex.getMessage());

      System.exit(1);
    }
  } 


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Create the lock file.
   */ 
  private void 
  createLockFile()
    throws PipelineException 
  {
    File file = new File(pQueueDir, "queue/lock");
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
    File file = new File(pQueueDir, "queue/lock");
    if(file.exists())
      file.delete();
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Make sure that the root queue directories exist.
   */ 
  private void 
  makeRootDirs() 
    throws PipelineException
  {
    if(!pQueueDir.isDirectory()) 
      throw new PipelineException
	("The root node directory (" + pQueueDir + ") does not exist!");
    
    ArrayList<File> dirs = new ArrayList<File>();
    dirs.add(new File(pQueueDir, "queue"));
    dirs.add(new File(pQueueDir, "queue/etc"));
    dirs.add(new File(pQueueDir, "queue/jobs"));
    dirs.add(new File(pQueueDir, "queue/job-info"));
    dirs.add(new File(pQueueDir, "queue/job-groups"));
    dirs.add(new File(pQueueDir, "queue/job-servers/samples"));

    synchronized(pMakeDirLock) {
      for(File dir : dirs) {
	if(!dir.isDirectory())
	  if(!dir.mkdirs()) 
	    throw new PipelineException
	      ("Unable to create the directory (" + dir + ")!");
      }
    }
  }

  /**
   * Initialize the server extensions. 
   */
  private void 
  initQueueExtensions()
    throws PipelineException
  {
    TaskTimer timer = new TaskTimer();
    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       "Loading Extensions...");   
    LogMgr.getInstance().flush();

    {
      readQueueExtensions();
      
      synchronized(pQueueExtensions) {
	for(QueueExtensionConfig config : pQueueExtensions.values()) 
	  doPostExtensionEnableTask(config);
      }
    }

    timer.suspend();
    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       "  Loaded in " + TimeStamps.formatInterval(timer.getTotalDuration()));
    LogMgr.getInstance().flush();    
  }

  /**
   * Initialize the license and selection keys. 
   */ 
  private void 
  initLicenseSelectionKeys() 
    throws PipelineException
  {
    TaskTimer timer = new TaskTimer();
    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       "Loading License/Selection/Hardware Keys...");   
    LogMgr.getInstance().flush();

    /* load the license keys if any exist */ 
    readLicenseKeys();
    
    /* load the hardware keys if any exist */ 
    readHardwareKeys();
    readHardwareGroups();
    
    /* load the selection keys, groups and schedules if any exist */ 
    readSelectionKeys();
    readSelectionGroups();
    readSelectionSchedules();

    timer.suspend();
    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       "  Loaded in " + TimeStamps.formatInterval(timer.getTotalDuration()));
    LogMgr.getInstance().flush();    
  }

  /**
   * Initialize the job server hosts. 
   */ 
  private void 
  initHosts() 
    throws PipelineException
  {
    TaskTimer timer = new TaskTimer();
    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       "Loading Job Servers Info...");   
    LogMgr.getInstance().flush();

    readHosts();
    
    timer.suspend();
    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       "  Loaded in " + TimeStamps.formatInterval(timer.getTotalDuration()));
    LogMgr.getInstance().flush();    
  }

  /**
   * Initialize the job related tables from disk files.
   */ 
  private void 
  initJobTables() 
    throws PipelineException
  {
    /* read the existing queue jobs files (in oldest to newest order) */ 
    TreeMap<Long,String> running = new TreeMap<Long,String>();
    {
      TaskTimer timer = new TaskTimer();
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Info,
	 "Loading Jobs...");   
      LogMgr.getInstance().flush();

      File dir = new File(pQueueDir, "queue/jobs");
      File files[] = dir.listFiles(); 
      int wk;
      for(wk=0; wk<files.length; wk++) {
	if(files[wk].isFile()) {
	  try {
	    Long jobID = new Long(files[wk].getName());
	    QueueJob job = readJob(jobID);
	    QueueJobInfo info = readJobInfo(jobID);

	    /* initialize the table of working area files to the jobs which create them */ 
	    {
	      ActionAgenda agenda = job.getActionAgenda();
	      NodeID nodeID = agenda.getNodeID();
	      FileSeq fseq = agenda.getPrimaryTarget();
	      
	      synchronized(pNodeJobIDs) {
		TreeMap<File,Long> table = pNodeJobIDs.get(nodeID);
		if(table == null) {
		  table = new TreeMap<File,Long>();
		  pNodeJobIDs.put(nodeID, table);
		}
		
		for(File file : fseq.getFiles()) 
		  table.put(file, jobID);
	      }
	    }
	    
	    /* determine if the job is still active */ 
	    switch(info.getState()) {
	    case Queued:
	    case Preempted:
	    case Paused:
	      pWaiting.add(jobID);
	      break;
	      
	    case Running:
              info.limbo();
              writeJobInfo(info);
	    case Limbo:
	      running.put(jobID, info.getHostname());
	    }
	    
	    synchronized(pJobs) {
	      pJobs.put(jobID, job);
	    }
	    
            synchronized(pJobInfo) {
              pJobInfo.put(jobID, info); 
            }
	  }
	  catch(NumberFormatException ex) {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	       "Illegal job file encountered (" + files[wk] + ")!");
	  }
	  catch(PipelineException ex) {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Ops, LogMgr.Level.Severe,
	       ex.getMessage());
	  }
	}
      }

      timer.suspend();
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Info,
	 "  Loaded in " + TimeStamps.formatInterval(timer.getTotalDuration()));
      LogMgr.getInstance().flush();    
    }

    /* initialize the job groups */ 
    {
      TaskTimer timer = new TaskTimer();
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Info,
	 "Loading Job Groups...");   
      LogMgr.getInstance().flush();

      File dir = new File(pQueueDir, "queue/job-groups");
      File files[] = dir.listFiles(); 
      int wk;
      for(wk=0; wk<files.length; wk++) {
	if(files[wk].isFile()) {
	  try {
	    Long groupID = new Long(files[wk].getName());
	    QueueJobGroup group = readJobGroup(groupID);
	    if(group == null) 
	      throw new IllegalStateException("The job group cannot be (null)!");
	    synchronized(pJobGroups) {
	      pJobGroups.put(groupID, group);
	    }
	  }
	  catch(NumberFormatException ex) {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Ops, LogMgr.Level.Severe,
	       "Illegal job group file encountered (" + files[wk] + ")!");
	  }
	}
      }

      timer.suspend();
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Info,
	 "  Loaded in " + TimeStamps.formatInterval(timer.getTotalDuration()));
      LogMgr.getInstance().flush();    
    }

    /* garbage collect all jobs no longer referenced by a job group */ 
    {
      TaskTimer timer = new TaskTimer();
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Info,
	 "Cleaning Old Jobs...");   
      LogMgr.getInstance().flush();

      garbageCollectJobs(timer);
      
      timer.suspend();
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Info,
	 "  Cleaned in " + TimeStamps.formatInterval(timer.getTotalDuration()));
      LogMgr.getInstance().flush();    
    }

    /* initialize the job counters */ 
    {
      TaskTimer timer = new TaskTimer();
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Info,
	 "Initializing Job Counters...");   
      LogMgr.getInstance().flush();

      synchronized(pJobGroups) {
	for(QueueJobGroup group : pJobGroups.values()) 
	  pJobCounters.initCounters(timer, group);
      } 
      
      synchronized(pJobInfo) {
	for(QueueJobInfo info : pJobInfo.values())
	  pJobCounters.update(timer, null, info);
      }

      timer.suspend();
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Info,
	 "  Initialized in " + TimeStamps.formatInterval(timer.getTotalDuration()));
      LogMgr.getInstance().flush();    
    }

    /* create tasks to manage the previously started jobs which will be started when
       the host is re-Enabled */ 
    {
      TaskTimer timer = new TaskTimer();
      for(Long jobID : running.keySet()) {
        boolean stillExists = false;
        synchronized(pJobs) {
          stillExists = pJobs.containsKey(jobID);
        }
        
        if(stillExists) {
          String hostname = running.get(jobID);
          
          QueueJob job = null;
          synchronized(pJobs) {
            job = pJobs.get(jobID);
          }
          
          OsType os = null;
          { 
            QueueJobInfo info = getJobInfo(timer, jobID);
            if(info != null) 
              os = info.getOsType();
          }
          
          /* attempt to aquire the licenses already being used by the job */ 
          TreeSet<String> acquiredKeys = new TreeSet<String>();
          synchronized(pLicenseKeys) {
            for(String kname : job.getJobRequirements().getLicenseKeys()) {
              LicenseKey key = pLicenseKeys.get(kname);
              if(key != null) {
                if(key.acquire(hostname)) 
                  acquiredKeys.add(kname);
                else {
                  LogMgr.getInstance().log
                    (LogMgr.Kind.Ops, LogMgr.Level.Warning,
                     "Unable to aquire a (" + key.getName() + ") license key for the " + 
                     "job (" + jobID + ") already running on (" + hostname + ")!");
                }
              }            
            }
          }

          monitorJob(new MonitorTask(hostname, os, job, acquiredKeys)); 
        }
      }
    }
  }

  /**
   * Establish a connection back to the Master Manager daemon.
   */ 
  public void 
  establishMasterConnection()
  {
    try {
      pMasterMgrClient.waitForConnection(1000, 5000);
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 "Unable to (re)connect to the Master Manager daemon!\n" + 
	 "  " + ex.getMessage());
      
      pServer.shutdown();
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S H U T D O W N                                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the shutdown options.
   * 
   * @param shutdownJobMgrs
   *   Whether to shutdown all job servers before exiting.
   */ 
  public void 
  setShutdownOptions
  (
   boolean shutdownJobMgrs
  ) 
  {
    pShutdownJobMgrs.set(shutdownJobMgrs);
  }

  /**
   * Shutdown the queue manager. <P> 
   * 
   * It is crucial that this method be called when only a single thread is able to access
   * this instance!  In other words, after all request threads have already exited or by a 
   * restart during the construction of this instance.
   */
  public void  
  shutdown() 
  {
    /* shutdown all job servers */ 
    if(pShutdownJobMgrs.get()) { 
      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Info,
	 "Shutting Down Job Servers..."); 
      LogMgr.getInstance().flush();

      for(String hname : pHosts.keySet()) {
	QueueHost host = pHosts.get(hname);
	if(host != null) {
	  try {
	    JobMgrControlClient client = new JobMgrControlClient(hname);
	    client.shutdown();
	  }
	  catch(PipelineException ex) {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Net, LogMgr.Level.Warning,
	       ex.getMessage());
	  }
	}
      }
    }

    /* write the last interval of samples to disk */ 
    {
      TaskTimer tm = new TaskTimer();  
      try {
	writeSamples(tm, true); 
      }
      catch(PipelineException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Severe,
	   ex.getMessage());
      }
    }

    /* shutdown extensions */ 
    {
      /* disable extensions */ 
      synchronized(pQueueExtensions) {
	for(QueueExtensionConfig config : pQueueExtensions.values()) 
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


  /*----------------------------------------------------------------------------------------*/
  /*   A D M I N I S T R A T I V E   P R I V I L E G E S                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the work groups and administrative privileges from the MasterMgr.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to update the privileges.
   */ 
  public Object
  updateAdminPrivileges
  (
   MiscUpdateAdminPrivilegesReq req
  ) 
  {
    TaskTimer timer = new TaskTimer("QueueMgr.updateAdminPrivileges()");
    timer.aquire();
    try {
      synchronized(pHosts) {
	timer.resume();

	{
	  timer.aquire();
	  pAdminPrivileges.updateAdminPrivileges(timer, req);
	}
	
	boolean modified = false;
	for(QueueHost host : pHosts.values()) {
	  String res = host.getReservation();
	  if((res != null) && !pAdminPrivileges.isValidName(res)) {
	    host.setReservation(null); 
	    modified = true;
	  }
	}
	
	if(modified) 
	  writeHosts();
      }
      
      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
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
      lc.setLevel(LogMgr.Kind.Glu, mgr.getLevel(LogMgr.Kind.Glu));
      lc.setLevel(LogMgr.Kind.Ops, mgr.getLevel(LogMgr.Kind.Ops));
      lc.setLevel(LogMgr.Kind.Mem, mgr.getLevel(LogMgr.Kind.Mem));
      lc.setLevel(LogMgr.Kind.Net, mgr.getLevel(LogMgr.Kind.Net));
      lc.setLevel(LogMgr.Kind.Plg, mgr.getLevel(LogMgr.Kind.Plg));
      lc.setLevel(LogMgr.Kind.Dsp, mgr.getLevel(LogMgr.Kind.Dsp));
      lc.setLevel(LogMgr.Kind.Sel, mgr.getLevel(LogMgr.Kind.Sel));
      lc.setLevel(LogMgr.Kind.Job, mgr.getLevel(LogMgr.Kind.Job));
      lc.setLevel(LogMgr.Kind.Col, mgr.getLevel(LogMgr.Kind.Col));
      lc.setLevel(LogMgr.Kind.Sch, mgr.getLevel(LogMgr.Kind.Sch));
      lc.setLevel(LogMgr.Kind.Ext, mgr.getLevel(LogMgr.Kind.Ext));

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
	  LogMgr.Level level = lc.getLevel(LogMgr.Kind.Dsp);
	  if(level != null) 
	    mgr.setLevel(LogMgr.Kind.Dsp, level);
	}

	{
	  LogMgr.Level level = lc.getLevel(LogMgr.Kind.Sel);
	  if(level != null) 
	    mgr.setLevel(LogMgr.Kind.Sel, level);
	}

	{
	  LogMgr.Level level = lc.getLevel(LogMgr.Kind.Job);
	  if(level != null) 
	    mgr.setLevel(LogMgr.Kind.Job, level);
	}

	{
	  LogMgr.Level level = lc.getLevel(LogMgr.Kind.Col);
	  if(level != null) 
	    mgr.setLevel(LogMgr.Kind.Col, level);
	}

	{
	  LogMgr.Level level = lc.getLevel(LogMgr.Kind.Sch);
	  if(level != null) 
	    mgr.setLevel(LogMgr.Kind.Sch, level);
	}

	{
	  LogMgr.Level level = lc.getLevel(LogMgr.Kind.Ext);
	  if(level != null) 
	    mgr.setLevel(LogMgr.Kind.Ext, level);
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
   *   <CODE>QueueGetQueueControlsRsp</CODE>.
   */ 
  public Object
  getRuntimeControls() 
  {
    TaskTimer timer = new TaskTimer();

    QueueControls controls = new QueueControls(pCollectorBatchSize.get(), 
                                               pDispatcherInterval.get(), 
                                               pNfsCacheInterval.get());

    return new QueueGetQueueControlsRsp(timer, controls);
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
   QueueSetQueueControlsReq req
  ) 
  {
    TaskTimer timer = new TaskTimer();

    try {
      if(!pAdminPrivileges.isMasterAdmin(req)) 
	throw new PipelineException
	  ("Only a user with Master Admin privileges may change the runtime parameters!");

      QueueControls controls = req.getControls();

      {
	Integer size = controls.getCollectorBatchSize();
	if(size != null) 
	  pCollectorBatchSize.set(size); 
      }

      {
	Long interval = controls.getDispatcherInterval();
	if(interval != null) 
	  pDispatcherInterval.set(interval);
      }

      {
	Long interval = controls.getNfsCacheInterval();
	if(interval != null) 
	  pNfsCacheInterval.set(interval);
      }

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   L I C E N S E   K E Y S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the currently defined license keys. <P>  
   *
   * @param userSettableOnly
   *   Only return the names of the keys that the user can set.
   *   
   * @return
   *   <CODE>QueueGetKeyNamesRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to get the key names.
   */
  public Object
  getLicenseKeyNames
  (
    boolean userSettableOnly  
  )  
  {
    TaskTimer timer = new TaskTimer();
    timer.aquire();
    synchronized(pLicenseKeys) {
      timer.resume();
      
      TreeSet<String> names = null;
      if (!userSettableOnly)
        names = new TreeSet<String>(pLicenseKeys.keySet());
      else {
        names = new TreeSet<String>();
        for (String name : pLicenseKeys.keySet())
          if (!pLicenseKeys.get(name).hasKeyChooser())
            names.add(name);
      }
      
      return new QueueGetKeyNamesRsp(timer, names);
    }
  }

  /**
   * Get the names and descriptions of the currently defined license keys. <P>  
   *
   * @param userSettableOnly
   *   Only return the names of the keys that the user can set.
   *   
   * @return
   *   <CODE>QueueGetKeyDescriptionsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to get the key names.
   */
  public Object
  getLicenseKeyDescriptions
  (
    boolean userSettableOnly  
  )  
  {
    TaskTimer timer = new TaskTimer();
    timer.aquire();
    synchronized(pLicenseKeys) {
      timer.resume();
      
      TreeMap<String,String> results = new TreeMap<String,String>();
      for (String name : pLicenseKeys.keySet()) {
        LicenseKey key = pLicenseKeys.get(name); 
        if(!userSettableOnly || !key.hasKeyChooser())
          results.put(name, key.getDescription());
      }
      
      return new QueueGetKeyDescriptionsRsp(timer, results);
    }
  }

  /**
   * Get the currently defined license keys. <P>  
   * 
   * @return
   *   <CODE>QueueGetLicenseKeysRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to get the keys.
   */
  public Object
  getLicenseKeys() 
  {
    TaskTimer timer = new TaskTimer();
    timer.aquire();
    synchronized(pLicenseKeys) {
      timer.resume();
      
      ArrayList<LicenseKey> keys = new ArrayList<LicenseKey>(pLicenseKeys.values());
      
      return new QueueGetLicenseKeysRsp(timer, keys);
    }
  }

  /**
   * Add the given license key to the currently defined license keys. <P> 
   * 
   * If a license key already exists which has the same name as the given key, it will be 
   * silently overridden by this operation. <P> 
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to add the key.
   */ 
  public Object
  addLicenseKey
  (
   QueueAddLicenseKeyReq req
  ) 
  {
    LicenseKey key = req.getLicenseKey();

    TaskTimer timer = new TaskTimer("QueueMgr.addLicenseKey(): " + key.getName());
    timer.aquire();
    try {
      if(!pAdminPrivileges.isQueueAdmin(req))
	throw new PipelineException
	  ("Only a user with Queue Admin privileges may add license keys!");

      synchronized(pLicenseKeys) {
	timer.resume();

	pLicenseKeys.put(key.getName(), key);
	writeLicenseKeys();

	return new SuccessRsp(timer);
      }
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }
  }

  /**
   * Remove the license key with the given name from currently defined license keys. <P> 
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to remove the key.
   */ 
  public Object
  removeLicenseKey
  (
   QueueRemoveLicenseKeyReq req
  ) 
  {
    String kname = req.getKeyName();

    TaskTimer timer = new TaskTimer("QueueMgr.removeLicenseKey(): " + kname); 
    timer.aquire();
    try {
      if(!pAdminPrivileges.isQueueAdmin(req))
	throw new PipelineException
	  ("Only a user with Queue Admin privileges may remove license keys!");

      synchronized(pLicenseKeys) {
	timer.resume();
	
	pLicenseKeys.remove(kname);
	writeLicenseKeys();

	return new SuccessRsp(timer);
      }
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }
  }  
  
  /**
   * Set the licensing scheme and maximum number of licenses associated with a 
   * license key. <P> 
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to set the maximum licenses.
   */ 
  public Object
  setMaxLicenses
  (
   QueueSetMaxLicensesReq req
  ) 
  {
    String kname = req.getKeyName();
    LicenseScheme scheme = req.getScheme();
    Integer maxSlots = req.getMaxSlots();
    Integer maxHosts = req.getMaxHosts();
    Integer maxHostSlots = req.getMaxHostSlots();

    String msg = null;
    switch(scheme) {
    case PerSlot:
      msg = maxSlots.toString();
      break;

    case PerHost:
      msg = maxHosts.toString();
      break;

    case PerHostSlot:
      msg = (maxHosts + "/" + maxHostSlots);
    }

    TaskTimer timer = 
      new TaskTimer("QueueMgr.setMaxLicenses(): " + kname + "[" + scheme + ": " + msg + "]");
    timer.aquire();
    try {
      if(!pAdminPrivileges.isQueueAdmin(req))
	throw new PipelineException
	  ("Only a user with Queue Admin privileges may modify the number of license keys!");

      synchronized(pLicenseKeys) {
	timer.resume();
	
	LicenseKey key = pLicenseKeys.get(kname);
	if(key == null) 
	  throw new PipelineException
	    ("Unable to set the maximum number of licenses because no license key " + 
	     "named (" + kname + ") exists!");
	
	try {
	  key.setScheme(scheme);
	  key.setMaxSlots(maxSlots);
	  key.setMaxHosts(maxHosts);
	  key.setMaxHostSlots(maxHostSlots);
	}
	catch(IllegalArgumentException ex) {
	  throw new PipelineException(ex.getMessage());	  
	}   

	writeLicenseKeys();

	return new SuccessRsp(timer);
      }
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }   
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S E L E C T I O N   K E Y S                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the currently defined selection keys. <P>  
   *
   * @param userSettableOnly
   *   Only return the names of the keys that the user can set.
   *   
   * @return
   *   <CODE>QueueGetKeyNamesRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to get the key names.
   */
  public Object
  getSelectionKeyNames
  (
    boolean userSettableOnly  
  )  
  {
    TaskTimer timer = new TaskTimer();
    timer.aquire();
    synchronized(pSelectionKeys) {
      timer.resume();
      
      TreeSet<String> names = null;
      if (!userSettableOnly)
        names = new TreeSet<String>(pSelectionKeys.keySet());
      else {
        names = new TreeSet<String>();
        for (String name : pSelectionKeys.keySet())
          if (!pSelectionKeys.get(name).hasKeyChooser())
            names.add(name);
      }
      
      return new QueueGetKeyNamesRsp(timer, names);
    }
  }

  /**
   * Get the names and descriptions of the currently defined selection keys. <P>  
   *
   * @param userSettableOnly
   *   Only return the names of the keys that the user can set.
   *   
   * @return
   *   <CODE>QueueGetKeyDescriptionsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to get the key names.
   */
  public Object
  getSelectionKeyDescriptions
  (
    boolean userSettableOnly  
  )  
  {
    TaskTimer timer = new TaskTimer();
    timer.aquire();
    synchronized(pSelectionKeys) {
      timer.resume();
      
      TreeMap<String,String> results = new TreeMap<String,String>();
      for (String name : pSelectionKeys.keySet()) {
        SelectionKey key = pSelectionKeys.get(name); 
        if(!userSettableOnly || !key.hasKeyChooser())
          results.put(name, key.getDescription());
      }
      
      return new QueueGetKeyDescriptionsRsp(timer, results);
    }
  }

  /**
   * Get the currently defined selection keys. <P>  
   * 
   * @return
   *   <CODE>QueueGetSelectionKeysRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to get the keys.
   */
  public Object
  getSelectionKeys() 
  {
    TaskTimer timer = new TaskTimer();
    timer.aquire();
    synchronized(pSelectionKeys) {
      timer.resume();
      
      ArrayList<SelectionKey> keys = new ArrayList<SelectionKey>(pSelectionKeys.values());
      
      return new QueueGetSelectionKeysRsp(timer, keys);
    }
  }
  
  /**
   * Add the given selection key to the currently defined selection keys. <P> 
   * 
   * If a selection key already exists which has the same name as the given key, it will be 
   * silently overridden by this operation. <P> 
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to add the key.
   */ 
  public Object
  addSelectionKey
  (
   QueueAddSelectionKeyReq req
  ) 
  {
    SelectionKey key = req.getSelectionKey();

    TaskTimer timer = new TaskTimer("QueueMgr.addSelectionKey(): " + key.getName());
    timer.aquire();
    try {
      if(!pAdminPrivileges.isQueueAdmin(req))
	throw new PipelineException
	  ("Only a user with Queue Admin privileges may add selection keys!");

      synchronized(pSelectionKeys) {
	timer.resume();

        pSelectionChanged.set(true);
	pSelectionKeys.put(key.getName(), key);
	writeSelectionKeys();

	return new SuccessRsp(timer);
      }
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }
  }

  /**
   * Remove the selection key with the given name from currently defined selection keys. <P> 
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to remove the key.
   */ 
  public Object
  removeSelectionKey
  (
   QueueRemoveSelectionKeyReq req
  ) 
  {
    String kname = req.getKeyName();

    TaskTimer timer = new TaskTimer("QueueMgr.removeSelectionKey(): " + kname); 
    timer.aquire();
    try {
      if(!pAdminPrivileges.isQueueAdmin(req))
	throw new PipelineException
	  ("Only a user with Queue Admin privileges may remove selection keys!");

      synchronized(pSelectionGroups) {
	boolean modified = false;
	synchronized(pSelectionKeys) {
	  timer.resume();
	
          pSelectionChanged.set(true);

	  {
	    pSelectionKeys.remove(kname);
	    writeSelectionKeys();
	  }
	  
	  for(SelectionGroup sg : pSelectionGroups.values()) {
	    if(sg.getBias(kname) != null) {
	      sg.removeBias(kname);
	      modified = true;
	    }
	  }
	}

	if(modified) 
	  writeSelectionGroups();
      }

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }
  }  


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the names of all existing selection groups. 
   * 
   * @return
   *   <CODE>QueueGetSelectionGroupNamesRsp</CODE> if successful.
   */ 
  public Object
  getSelectionGroupNames() 
  {
    TaskTimer timer = new TaskTimer();
    timer.aquire();
    synchronized(pSelectionGroups) {
      timer.resume();

      TreeSet<String> names = new TreeSet<String>(pSelectionGroups.keySet());
      return new QueueGetSelectionGroupNamesRsp(timer, names);
    }
  }
  
  /**
   * Get the current selection biases for all existing selection groups. 
   * 
   * @return
   *   <CODE>QueueGetSelectionGroupsRsp</CODE> if successful.
   */ 
  public Object
  getSelectionGroups() 
  {
    TaskTimer timer = new TaskTimer();
    timer.aquire();
    synchronized(pSelectionGroups) {
      timer.resume();
      
      return new QueueGetSelectionGroupsRsp(timer, pSelectionGroups);
    }
  }
  
  /**
   * Add a new selection group. <P> 
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to add the host.
   */ 
  public Object
  addSelectionGroup
  (
   QueueAddSelectionGroupReq req
  ) 
  {
    String name = req.getName();
    TaskTimer timer = new TaskTimer("QueueMgr.addSelectionGroup(): " + name);
    timer.aquire();
    try {
      if(!pAdminPrivileges.isQueueAdmin(req))
	throw new PipelineException
	  ("Only a user with Queue Admin privileges may add selection groups!"); 

      if(name.equals("Favor Groups")) 
	throw new PipelineException
	  ("The name (Favor Groups) is reserved and cannot be used as the name of a " + 
	   "Selection Group!");

      synchronized(pSelectionGroups) {
	timer.resume();
	
	if(pSelectionGroups.containsKey(name)) 
	  throw new PipelineException
	    ("A selection group named (" + name + ") already exists!");

        pSelectionChanged.set(true);
	pSelectionGroups.put(name, new SelectionGroup(name));

	writeSelectionGroups();

	return new SuccessRsp(timer);
      }
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }    
  }

  /**
   * Remove the given existing selection group. <P> 
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to remove the selection group.
   */ 
  public Object
  removeSelectionGroups
  (
   QueueRemoveSelectionGroupsReq req
  ) 
  {
    TreeSet<String> names = req.getNames();

    TaskTimer timer = new TaskTimer("QueueMgr.removeSelectionGroups():");
    timer.aquire();
    try {
      if(!pAdminPrivileges.isQueueAdmin(req))
	throw new PipelineException
	  ("Only a user with Queue Admin privileges may remove selection groups!"); 

      synchronized(pHosts) {
	synchronized(pSelectionSchedules) {
	  synchronized(pSelectionGroups) {
	    timer.resume();
	
            pSelectionChanged.set(true);

	    {
	      for(String name : names)
		pSelectionGroups.remove(name);
	      
	      writeSelectionGroups();
	    }
	    
	    {
	      boolean modified = false;
	      for(QueueHost host : pHosts.values()) {
		String gname = host.getSelectionGroup();
		if((gname != null) && names.contains(gname)) {
		  host.setSelectionGroup(null);
		  modified = true;
		}
	      }
	      
	      if(modified) 
		writeHosts();
	    }

	    {
	      boolean modified = false;
	      for(SelectionSchedule schedule : pSelectionSchedules.values()) 
		modified = schedule.clearInvalidGroups(names);
	      
	      if(modified) 
		writeSelectionSchedules();
	    }
	  }
	}
      }

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }    
  }

  /**
   * Change the selection key biases for the given selection groups. <P> 
   * 
   * For an detailed explanation of how selection keys are used to determine the assignment
   * of jobs to hosts, see {@link JobReqs JobReqs}. <P> 
   * 
   * @param req 
   *   The request.
   *    
   * @return 
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to change the selection groups.
   */ 
  public Object
  editSelectionGroups
  (
   QueueEditSelectionGroupsReq req
  ) 
  {
    TaskTimer timer = new TaskTimer("QueueMgr.editSelectionGroups()");

    timer.aquire();
    try {
      if(!pAdminPrivileges.isQueueAdmin(req))
	throw new PipelineException
	  ("Only a user with Queue Admin privileges may edit selection groups!");

      synchronized(pSelectionGroups) {
	synchronized(pSelectionKeys) {
	  timer.resume();
	
          pSelectionChanged.set(true);

	  for(SelectionGroup sg : req.getSelectionGroups()) {
	    /* strip any obsolete selection keys */ 
	    TreeSet<String> dead = new TreeSet<String>();
	    for(String key : sg.getKeys()) {
	      if(!pSelectionKeys.containsKey(key)) 
		dead.add(key);
	    }
	    for(String key : dead) 
	      sg.removeBias(key);

	    /* update the group */ 
	    pSelectionGroups.put(sg.getName(), sg);
	  }
	}
      
	writeSelectionGroups();
      }
    
      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }    
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the names of all existing selection schedules. 
   * 
   * @return
   *   <CODE>QueueGetSelectionScheduleNamesRsp</CODE> if successful.
   */ 
  public Object
  getSelectionScheduleNames() 
  {
    TaskTimer timer = new TaskTimer();
    timer.aquire();
    synchronized(pSelectionSchedules) {
      timer.resume();

      TreeSet<String> names = new TreeSet<String>(pSelectionSchedules.keySet());
      return new QueueGetSelectionScheduleNamesRsp(timer, names);
    }
  }
  
  /**
   * Get the existing selection schedules. 
   * 
   * @return
   *   <CODE>QueueGetSelectionSchedulesRsp</CODE> if successful.
   */ 
  public Object
  getSelectionSchedules() 
  {
    TaskTimer timer = new TaskTimer();
    timer.aquire();
    synchronized(pSelectionSchedules) {
      timer.resume();
      
      return new QueueGetSelectionSchedulesRsp(timer, pSelectionSchedules);
    }
  }
  
  /**
   * Add a new selection schedule. <P> 
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to add the host.
   */ 
  public Object
  addSelectionSchedule
  (
   QueueAddSelectionScheduleReq req
  ) 
  {
    String name = req.getName();
    TaskTimer timer = new TaskTimer("QueueMgr.addSelectionSchedule(): " + name);
    timer.aquire();
    try {
      if(!pAdminPrivileges.isQueueAdmin(req))
	throw new PipelineException
	  ("Only a user with Queue Admin privileges may add selection schedules!"); 

      synchronized(pSelectionSchedules) {
	timer.resume();
	
	if(pSelectionSchedules.containsKey(name)) 
	  throw new PipelineException
	    ("A selection schedule named (" + name + ") already exists!");
	pSelectionSchedules.put(name, new SelectionSchedule(name));

	writeSelectionSchedules();

	return new SuccessRsp(timer);
      }
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }    
  }

  /**
   * Remove the given existing selection schedule. <P> 
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to remove the selection schedule.
   */ 
  public Object
  removeSelectionSchedules
  (
   QueueRemoveSelectionSchedulesReq req
  ) 
  {
    TreeSet<String> names = req.getNames();

    TaskTimer timer = new TaskTimer("QueueMgr.removeSelectionSchedules():");
    timer.aquire();
    try {
      if(!pAdminPrivileges.isQueueAdmin(req))
	throw new PipelineException
	  ("Only a user with Queue Admin privileges may remove selection schedules!"); 

      synchronized(pHosts) {
	boolean modified = false;
	synchronized(pSelectionSchedules) {
	  timer.resume();

	  {
	    for(String name : names)
	      pSelectionSchedules.remove(name);
	    
	    writeSelectionSchedules();
	  }
	
	  for(QueueHost host : pHosts.values()) {
	    String sname = host.getSelectionSchedule();
	    if((sname != null) && names.contains(sname)) {
	      host.setSelectionSchedule(null);
	      modified = true;
	    }
	  }
	}

	if(modified) 
	  writeHosts();
      }
      
      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }    
  }

  /**
   * Modify the given selection schedules. <P> 
   * 
   * @param req 
   *   The request.
   *    
   * @return 
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to change the selection schedules.
   */ 
  public Object
  editSelectionSchedules
  (
   QueueEditSelectionSchedulesReq req
  ) 
  {
    TaskTimer timer = new TaskTimer("QueueMgr.editSelectionSchedules()");

    timer.aquire();
    try {
      if(!pAdminPrivileges.isQueueAdmin(req))
	throw new PipelineException
	  ("Only a user with Queue Admin privileges may edit selection keys!");

      synchronized(pSelectionSchedules) {
	synchronized(pSelectionGroups) {
	  timer.resume();

	  for(SelectionSchedule schedule : req.getSelectionSchedules()) {
	    schedule.validateGroups(pSelectionGroups.keySet());
	    pSelectionSchedules.put(schedule.getName(), schedule);
	  }
	}
      
	writeSelectionSchedules();
	DemandSchedulerTask task = new DemandSchedulerTask();
	task.start();
      }
    
      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }    
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   H A R D W A R E   K E Y S                                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the currently defined hardware keys. <P>  
   *
   * @param userSettableOnly
   *   Only return the names of the keys that the user can set.
   *   
   * @return
   *   <CODE>QueueGetKeyNamesRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to get the key names.
   */
  public Object
  getHardwareKeyNames
  (
    boolean userSettableOnly  
  )  
  {
    TaskTimer timer = new TaskTimer();
    timer.aquire();
    synchronized(pHardwareKeys) {
      timer.resume();
      
      TreeSet<String> names = null;
      if (!userSettableOnly)
        names = new TreeSet<String>(pHardwareKeys.keySet());
      else {
        names = new TreeSet<String>();
        for (String name : pHardwareKeys.keySet())
          if (!pHardwareKeys.get(name).hasKeyChooser())
            names.add(name);
      }
      
      return new QueueGetKeyNamesRsp(timer, names);
    }
  }

  /**
   * Get the names and descriptions of the currently defined hardware keys. <P>  
   *
   * @param userSettableOnly
   *   Only return the names of the keys that the user can set.
   *   
   * @return
   *   <CODE>QueueGetKeyDescriptionsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to get the key names.
   */
  public Object
  getHardwareKeyDescriptions
  (
    boolean userSettableOnly  
  )  
  {
    TaskTimer timer = new TaskTimer();
    timer.aquire();
    synchronized(pHardwareKeys) {
      timer.resume();
      
      TreeMap<String,String> results = new TreeMap<String,String>();
      for (String name : pHardwareKeys.keySet()) {
        HardwareKey key = pHardwareKeys.get(name); 
        if(!userSettableOnly || !key.hasKeyChooser())
          results.put(name, key.getDescription());
      }
      
      return new QueueGetKeyDescriptionsRsp(timer, results);
    }
  }

  /**
   * Get the current hardware keys. 
   * 
   * @return
   *   <CODE>QueueGetHardwareKeysRsp</CODE> if successful.
   */ 
  public Object
  getHardwareKeys() 
  {
    TaskTimer timer = new TaskTimer();
    timer.aquire();
    synchronized(pHardwareKeys) {
      timer.resume();
      
      ArrayList<HardwareKey> keys = new ArrayList<HardwareKey>(pHardwareKeys.values());
      
      return new QueueGetHardwareKeysRsp(timer, keys);
    }
  }
  
  /**
   * Add the given hardware key to the currently defined hardware keys. <P> 
   * 
   * If a hardware key already exists which has the same name as the given key, it will be 
   * silently overridden by this operation. <P> 
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to add the key.
   */ 
  public Object
  addHardwareKey
  (
   QueueAddHardwareKeyReq req
  ) 
  {
    HardwareKey key = req.getHardwareKey();

    TaskTimer timer = new TaskTimer("QueueMgr.addHardwareKey(): " + key.getName());
    timer.aquire();
    try {
      if(!pAdminPrivileges.isQueueAdmin(req))
	throw new PipelineException
	  ("Only a user with Queue Admin privileges may add hardware keys!");

      synchronized(pHardwareKeys) {
	timer.resume();

        pHardwareChanged.set(true);
	pHardwareKeys.put(key.getName(), key);
	writeHardwareKeys();

	return new SuccessRsp(timer);
      }
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }
  }
  
  /**
   * Remove the hardware key with the given name from currently defined hardware keys. <P> 
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to remove the key.
   */ 
  public Object
  removeHardwareKey
  (
   QueueRemoveHardwareKeyReq req
  ) 
  {
    String kname = req.getKeyName();

    TaskTimer timer = new TaskTimer("QueueMgr.removeHardwareKey(): " + kname); 
    timer.aquire();
    try {
      if(!pAdminPrivileges.isQueueAdmin(req))
	throw new PipelineException
	  ("Only a user with Queue Admin privileges may remove hardware keys!");

      synchronized(pHardwareGroups) {
	boolean modified = false;
	synchronized(pHardwareKeys) {
	  timer.resume();
	
          pHardwareChanged.set(true);

	  {
	    pHardwareKeys.remove(kname);
	    writeHardwareKeys();
	  }
	  
	  for(HardwareGroup hg : pHardwareGroups.values()) {
	    if(hg.hasKey(kname)) {
	      hg.removeKey(kname);
	      modified = true;
	    }
	  }
	}

	if(modified) 
	  writeHardwareGroups();
      }

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }
  }  


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the names of all existing hardware groups. 
   * 
   * @return
   *   <CODE>QueueGetHardwareGroupNamesRsp</CODE> if successful.
   */ 
  public Object
  getHardwareGroupNames() 
  {
    TaskTimer timer = new TaskTimer();
    timer.aquire();
    synchronized(pHardwareGroups) {
      timer.resume();

      TreeSet<String> names = new TreeSet<String>(pHardwareGroups.keySet());
      return new QueueGetHardwareGroupNamesRsp(timer, names);
    }
  }
  
  /**
   * Get the current hardware key values for all existing hardware groups. 
   * 
   * @return
   *   <CODE>QueueGetHardwareGroupsRsp</CODE> if successful.
   */ 
  public Object
  getHardwareGroups() 
  {
    TaskTimer timer = new TaskTimer();
    timer.aquire();
    synchronized(pHardwareGroups) {
      timer.resume();
      
      return new QueueGetHardwareGroupsRsp(timer, pHardwareGroups);
    }
  }
  
  /**
   * Add a new hardware group. <P> 
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to add the host.
   */ 
  public Object
  addHardwareGroup
  (
   QueueAddHardwareGroupReq req
  ) 
  {
    String name = req.getName();
    TaskTimer timer = new TaskTimer("QueueMgr.addHardwareGroup(): " + name);
    timer.aquire();
    try {
      if(!pAdminPrivileges.isQueueAdmin(req))
	throw new PipelineException
	  ("Only a user with Queue Admin privileges may add hardware groups!"); 

      synchronized(pHardwareGroups) {
	timer.resume();
	
	if(pHardwareGroups.containsKey(name)) 
	  throw new PipelineException
	    ("A hardware group named (" + name + ") already exists!");

        pHardwareChanged.set(true);
	pHardwareGroups.put(name, new HardwareGroup(name));

	writeHardwareGroups();

	return new SuccessRsp(timer);
      }
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }    
  }

  /**
   * Remove the given existing hardware group. <P> 
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to remove the selection group.
   */ 
  public Object
  removeHardwareGroups
  (
   QueueRemoveHardwareGroupsReq req
  ) 
  {
    TreeSet<String> names = req.getNames();

    TaskTimer timer = new TaskTimer("QueueMgr.removeHardwareGroups():");
    timer.aquire();
    try {
      if(!pAdminPrivileges.isQueueAdmin(req))
	throw new PipelineException
	  ("Only a user with Queue Admin privileges may remove hardware groups!"); 

      synchronized(pHosts) {
        synchronized(pHardwareGroups) {
          timer.resume();
          
          pHardwareChanged.set(true);
          
          {
            for(String name : names)
              pHardwareGroups.remove(name);
            
            writeHardwareGroups();
          }
	  
          {
            boolean modified = false;
            for(QueueHost host : pHosts.values()) {
              String gname = host.getHardwareGroup();
              if((gname != null) && names.contains(gname)) {
                host.setHardwareGroup(null);
                modified = true;
              }
            }
	    
            if(modified) 
              writeHosts();
          }
        }
      }

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }    
  }

  /**
   * Change the hardware key values for the given hardware groups. <P> 
   * 
   * For an detailed explanation of how hardware keys are used to determine the assignment
   * of jobs to hosts, see {@link JobReqs JobReqs}. <P> 
   * 
   * @param req 
   *   The request.
   *    
   * @return 
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to change the selection groups.
   */ 
  public Object
  editHardwareGroups
  (
   QueueEditHardwareGroupsReq req
  ) 
  {
    TaskTimer timer = new TaskTimer("QueueMgr.editHardwareGroups()");

    timer.aquire();
    try {
      if(!pAdminPrivileges.isQueueAdmin(req))
	throw new PipelineException
	  ("Only a user with Queue Admin privileges may edit hardware groups!");

      synchronized(pHardwareGroups) {
	synchronized(pHardwareKeys) {
	  timer.resume();
	
          pHardwareChanged.set(true);

	  for(HardwareGroup hg : req.getHardwareGroups()) {
	    /* strip any obsolete hardware keys */ 
	    TreeSet<String> dead = new TreeSet<String>();
	    for(String key : hg.getKeys()) {
	      if(!pHardwareKeys.containsKey(key)) 
		dead.add(key);
	    }
	    for(String key : dead) 
	      hg.removeKey(key);

	    /* update the group */ 
	    pHardwareGroups.put(hg.getName(), hg);
	  }
	}
      
	writeHardwareGroups();
      }
    
      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }    
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S E R V E R   E X T E N S I O N S                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current queue extension configurations. <P> 
   * 
   * @return
   *   <CODE>MiscGetQueueExtensionsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the extensions.
   */ 
  public Object
  getQueueExtensions() 
  {
    TaskTimer timer = new TaskTimer();
    
    timer.aquire();
    synchronized(pQueueExtensions) {
      timer.resume();

      return new QueueGetQueueExtensionsRsp(timer, pQueueExtensions);
    }
  }
  
  /**
   * Remove an existing the queue extension configuration. <P> 
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to remove the extension.
   */ 
  public Object
  removeQueueExtension
  (
   QueueRemoveQueueExtensionReq req
  ) 
  {
    String name = req.getExtensionName();

    TaskTimer timer = new TaskTimer("QueueMgr.removeQueueExtension(): " + name); 
    timer.aquire();
    try {
      if(!pAdminPrivileges.isQueueAdmin(req))
	throw new PipelineException
	  ("Only a user with Queue Admin privileges may remove a " + 
	   "queue extension configuration!");

      synchronized(pQueueExtensions) {
	timer.resume();
	
	doPreExtensionDisableTask(pQueueExtensions.get(name));

	pQueueExtensions.remove(name);
	writeQueueExtensions();

	return new SuccessRsp(timer);
      }
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }
  }  
  
  /**
   * Add or modify an existing the queue extension configuration. <P> 
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to set the extension.
   */ 
  public Object
  setQueueExtension
  (
   QueueSetQueueExtensionReq req
  ) 
  {
    QueueExtensionConfig config = req.getExtension();
    String name = config.getName();

    TaskTimer timer = new TaskTimer("QueueMgr.setQueueExtension(): " + name); 
    timer.aquire();
    try {
      if(!pAdminPrivileges.isQueueAdmin(req))
	throw new PipelineException
	  ("Only a user with Queue Admin privileges may add or modify " + 
	   "queue extension configuration!");

      synchronized(pQueueExtensions) {
	timer.resume();

	doPreExtensionDisableTask(pQueueExtensions.get(name));

	pQueueExtensions.put(name, config); 
	writeQueueExtensions();

	doPostExtensionEnableTask(config); 

	return new SuccessRsp(timer);
      }
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }
  }  

  /**
   * Get new instances of all enabled queue extension plugins indexed by 
   * extension configuration name. <P> 
   * 
   * This method also will pre-cook the toolset environments for all plugins which will be
   * spawning subprocesses.
   * 
   * @param timer
   *   The task timer.
   */ 
  private TreeMap<String,BaseQueueExt> 
  getQueueExts
  (
   TaskTimer timer
  ) 
    throws PipelineException
  {
    TreeMap<String,BaseQueueExt> table = new TreeMap<String,BaseQueueExt>();
    TreeMap<String,String> toolsetNames = new TreeMap<String,String>();

    /* instantiate the plugins */ 
    timer.aquire();
    synchronized(pQueueExtensions) {
      timer.resume();
	
      for(String cname : pQueueExtensions.keySet()) {
	QueueExtensionConfig config = pQueueExtensions.get(cname);
	if(config.isEnabled()) {
	  table.put(cname, config.getQueueExt());
	  toolsetNames.put(cname, config.getToolset());
	}
      }
    }

    if(!table.isEmpty()) {
      /* fetch any toolsets not already cached */ 
      {
	TreeSet<String> tnames = new TreeSet<String>();
	for(String cname : table.keySet()) {
	  BaseQueueExt ext = table.get(cname);
	  if(ext.needsEnvironment()) 
	    tnames.add(toolsetNames.get(cname));
	}
      
	fetchToolsets(tnames, timer);
      }

      /* cook the toolset environments (if needed by plugins) */ 
      timer.aquire();
      synchronized(pToolsets) {
	timer.resume();

	for(String cname : table.keySet()) {
	  BaseQueueExt ext = table.get(cname); 
	  if(ext.needsEnvironment()) {
	    String tname = toolsetNames.get(cname);
	    Toolset tset = pToolsets.get(tname).get(OsType.Unix);
	    ext.setEnvironment(tset.getEnvironment());
	  }
	}
      }
    }

    return table;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Run the post-enable task (in the current thread) for the given queue extension.
   */ 
  private void 
  doPostExtensionEnableTask
  ( 
   QueueExtensionConfig config
  ) 
  {
    if((config != null) && config.isEnabled()) {
      try {
	BaseQueueExt ext = config.getQueueExt();

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
   * Run the pre-disable task (in the current thread) for the given queue extension.
   */ 
  private void 
  doPreExtensionDisableTask
  ( 
   QueueExtensionConfig config
  ) 
  {
    if((config != null) && config.isEnabled()) {
      try {
	BaseQueueExt ext = config.getQueueExt();

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
   * Create and start threads for all enabled extensions which support the given task.
   * 
   * @param timer
   *   The parent task timer. 
   * 
   * @param factory
   *   The queue extension test factory.
   * 
   * @throws PipelineException 
   *   If any of the enabled extension tests fail.
   */
  public void 
  performExtensionTests
  (
   TaskTimer timer, 
   QueueTestFactory factory
  ) 
    throws PipelineException
  {
    timer.aquire(); 
    synchronized(pQueueExtensions) {
      timer.resume();

      for(QueueExtensionConfig config : pQueueExtensions.values()) {
	if(config.isEnabled()) {
	  BaseQueueExt ext = null;
	  try {
	    ext = config.getQueueExt();
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
   *   The queue extension task factory.
   */
  public boolean
  hasAnyExtensionTasks
  (
   TaskTimer timer, 
   QueueTaskFactory factory
  ) 
  {
    timer.aquire(); 
    synchronized(pQueueExtensions) {
      timer.resume();

      for(QueueExtensionConfig config : pQueueExtensions.values()) {
	if(config.isEnabled()) {
	  try {
	    BaseQueueExt ext = config.getQueueExt();
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
   *   The queue extension task factory.
   */
  public void 
  startExtensionTasks
  (
   TaskTimer timer, 
   QueueTaskFactory factory
  ) 
  {
    timer.aquire(); 
    synchronized(pQueueExtensions) {
      timer.resume();

      for(QueueExtensionConfig config : pQueueExtensions.values()) {
	if(config.isEnabled()) {
	  try {
	    BaseQueueExt ext = config.getQueueExt();
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
  /*   J O B   M A N A G E R   H O S T S                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current state of the hosts capable of executing jobs for the Pipeline queue.
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>QueueGetHostsRsp</CODE> if successful
   *   <CODE>FailureRsp</CODE> if unable to get the hosts.
   */ 
  public Object
  getHosts
  (
   QueueGetHostsReq req
  ) 
  {
    TaskTimer timer = new TaskTimer();

    /* determine which histograms are to be used to filter the hosts */ 
    HistogramSpec[] active = null; 
    {
      QueueHostHistogramSpecs specs = req.getSpecs();
      if(specs != null) {
	updateHistogramSpecs(timer, specs); 

	active = new HistogramSpec[12];
	active[0]  = specs.getStatusSpec(); 
	active[1]  = specs.getOsTypeSpec(); 
	active[2]  = specs.getLoadSpec(); 
	active[3]  = specs.getMemorySpec(); 
	active[4]  = specs.getDiskSpec(); 
	active[5]  = specs.getNumJobsSpec(); 
	active[6]  = specs.getSlotsSpec(); 
	active[7]  = specs.getReservationSpec(); 
	active[8]  = specs.getOrderSpec(); 
	active[9]  = specs.getSelectionGroupsSpec(); 
	active[10] = specs.getSelectionSchedulesSpec(); 
	active[11] = specs.getHardwareGroupsSpec(); 

	boolean anyIncluded = false;
	int wk;
	for(wk=0; wk<active.length; wk++) {
	  if(active[wk].anyIncluded() && !active[wk].allIncluded()) 
	    anyIncluded = true;
	  else 
	    active[wk] = null;
	}

	/* if there are no included catagories for any histogram, 
	     just return all hosts */ 
	if(!anyIncluded) 
	  active = null;
      }
    }

    /* process the hosts */ 
    timer.aquire();
    synchronized(pHostsInfo) {
      timer.resume();

      TreeMap<String,QueueHostInfo> hosts = new TreeMap<String,QueueHostInfo>(); 
      if(active == null) {
	hosts.putAll(pHostsInfo);
      }
      else {
	for(String hname : pHostsInfo.keySet()) {
	  QueueHostInfo qinfo = pHostsInfo.get(hname);
	  ResourceSample sample = qinfo.getLatestSample(); 

	  boolean included = true;
	  int wk;
	  for(wk=0; wk<active.length; wk++) {
	    if(active[wk] != null) {
	      switch(wk) {
	      case 0:
		if(!active[wk].isIncludedItem(qinfo.getStatus()))
		  included = false;
		break;
		
	      case 1:
		{
		  OsType os = qinfo.getOsType(); 
		  if((os == null) || !active[wk].isIncludedItem(os))
		    included = false;
		}
		break;
		
	      case 2:
		if((sample == null) || !active[wk].isIncludedItem(sample.getLoad()))
		  included = false;
		break;
		
	      case 3:
		if((sample == null) || !active[wk].isIncludedItem(sample.getMemory()))
		  included = false;
		break;
		
	      case 4:
		if((sample == null) || !active[wk].isIncludedItem(sample.getDisk()))
		  included = false;
		break;
		
	      case 5:
		if((sample == null) || !active[wk].isIncludedItem(sample.getNumJobs()))
		  included = false;
		break;
		
	      case 6:
		if(!active[wk].isIncludedItem(qinfo.getJobSlots()))
		  included = false;
		break;
		
	      case 7:
		{
		  String res = qinfo.getReservation();
		  if(res == null) 
		    res = "-";
		  if(!active[wk].isIncludedItem(res))
		    included = false;
		}
		break;
		
	      case 8:
		if(!active[wk].isIncludedItem(qinfo.getOrder()))
		  included = false;
		break;
		
	      case 9:
		{
		  String group = qinfo.getSelectionGroup();
		  if(group == null) 
		    group = "-";
		  if(!active[wk].isIncludedItem(group))
		    included = false;
		}
		break;
		
	      case 10:
		{
		  String sched = qinfo.getSelectionSchedule();
		  if(sched == null) 
		    sched = "-";
		  if(!active[wk].isIncludedItem(sched))
		    included = false;
		}
                break; 

	      case 11:
		{
		  String group = qinfo.getHardwareGroup();
		  if(group == null) 
		    group = "-";
		  if(!active[wk].isIncludedItem(group))
		    included = false;
		}
	      }
	    }

	    if(!included) 
	      break;
	  }
	
	  if(included) 
	    hosts.put(hname, qinfo);
	}	
      }

      return  new QueueGetHostsRsp(timer, hosts);
    }
  }
  
  /**
   * Add a new execution host to the Pipeline queue. <P> 
   * 
   * The host will be added in a <CODE>Offline</CODE> state, unreserved and with no 
   * selection key biases.  If a host already exists with the given name, an exception 
   * will be thrown instead. <P> 
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to add the host.
   */ 
  public Object
  addHost
  (
   QueueAddHostReq req
  ) 
  {
    String hname = req.getHostname();
    TaskTimer timer = new TaskTimer("QueueMgr.addHost(): " + hname);

    AddHostExtFactory factory = new AddHostExtFactory(hname); 
    try {
      performExtensionTests(timer, factory);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }

    timer.aquire();
    try {
      if(!pAdminPrivileges.isQueueAdmin(req))
	throw new PipelineException
	  ("Only a user with Queue Admin privileges may add hosts!"); 

      synchronized(pHosts) {
	timer.resume();
	
	try {
	  hname = InetAddress.getByName(hname).getCanonicalHostName();
          hname = hname.toLowerCase(Locale.ENGLISH);
	}
	catch(UnknownHostException ex) {
	  throw new PipelineException
	    ("The host (" + hname + ") could not be reached or is illegal!");
	}
	
	if(pHosts.containsKey(hname)) 
	  throw new PipelineException
	    ("The host (" + hname + ") is already a job server!");
	
	pHosts.put(hname, new QueueHost(hname));
	writeHosts();
      }
	
      updateHostsInfo(timer);

      /* post-add host tasks */ 
      startExtensionTasks(timer, factory);

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }    
  }

  /**
   * Remove the given existing execution hosts from the Pipeline queue. <P> 
   * 
   * The hosts must be in a <CODE>Offline</CODE> state before they can be removed. <P> 
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to remove the hosts.
   */ 
  public Object
  removeHosts
  (
   QueueRemoveHostsReq req
  ) 
  {
    TaskTimer timer = new TaskTimer("QueueMgr.removeHosts():");

    TreeSet<String> deadHosts = new TreeSet<String>();
    try {
      /* filter out non-existent hosts */ 
      TreeSet<String> hostnames = new TreeSet<String>();
      {
	timer.aquire();
	synchronized(pHosts) {
	  timer.resume();
	  for(String hname : req.getHostnames()) {
	    if(pHosts.containsKey(hname)) 
	      hostnames.add(hname);
	  }
	}
      }
      
      /* pre-remove hosts tests */
      performExtensionTests(timer, new RemoveHostsExtFactory(hostnames));

      if(!pAdminPrivileges.isQueueAdmin(req))
	throw new PipelineException
	  ("Only a user with Queue Admin privileges may remove hosts!"); 

      boolean modified = false;
      try {
	/* remove the hosts */ 
	timer.aquire();
	synchronized(pHosts) {
	  timer.resume();
	
	  for(String hname : hostnames) {
	    QueueHost host = pHosts.get(hname);
	    if(host != null) {
	      switch(host.getStatus()) {
	      case Shutdown:
		modified = true;
		pHosts.remove(hname);
		deadHosts.add(hname);
		JobMgrControlClient.serverUnreachable(hname);
		break;
		
	      default:
		throw new PipelineException
		  ("Unable to remove host (" + hname + ") until it is Shutdown!");
	      }
	    }
	  }
	  
	  writeHosts();
	}

	/* remove the resource samples cached for the hosts */ 
	timer.aquire();
	synchronized(pSamples) {
	  timer.resume();

	  for(String hname : hostnames)
	    pSamples.remove(hname);
	}
      }
      finally {
	if(modified)       
	  updateHostsInfo(timer);

	/* post-remove hosts tasks */ 
	if(!deadHosts.isEmpty()) 
	  startExtensionTasks(timer, new RemoveHostsExtFactory(deadHosts)); 
      }

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }    
  }

  
  /**
   * Change the status, user reservations, job slots and/or selection key biases
   * of the given hosts. <P> 
   * 
   * Any of the arguments may be <CODE>null</CODE> if no changes are do be made for
   * the type of host property the argument controls. <P> 
   * 
   * A <B>pljobmgr<B>(1) daemon must be running on a host before its status can be 
   * changed to <CODE>Disabled</CODE> or <CODE>Enabled</CODE>.  If <B>plqueuemgr<B>(1) cannot
   * establish a network connection to a <B>pljobmgr<B>(1) daemon running on the host, the 
   * status will be overridden and changed to <CODE>Shutdown</CODE>.
   * 
   * If the new status for a host is <CODE>Shutdown</CODE> and there is a <B>pljobmgr<B>(1) 
   * daemon running on the host, it will be stopped and any job it is currently running will
   * be killed. <P> 
   * 
   * When a host is reserved, only jobs submitted by the reserving user will be assigned
   * to the host.  The reservation can be cleared by setting the reserving user name to
   * <CODE>null</CODE> for the host in the <CODE>reservations</CODE> argument. <P> 
   * 
   * Each host has a maximum number of jobs which it can be assigned at any one time 
   * regardless of the other limits on system resources.  This method allows the number
   * of slots to be changed for a number of hosts at once.  The number of slots cannot 
   * be negative and probably should not be set considerably higher than the number of 
   * CPUs on the host.  A good value is probably (1.5 * number of CPUs). <P>
   * 
   * @param req 
   *   The edit hosts request.
   *    
   * @return 
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to change the host status.
   */ 
  public Object
  editHosts
  (
   QueueEditHostsReq req
  ) 
  {
    TaskTimer timer = new TaskTimer("QueueMgr.editHosts()");

    try {
      TreeMap<String,QueueHostMod> changes = req.getChanges();

      if(!pAdminPrivileges.isQueueAdmin(req)) {
	TreeSet<String> localHostnames = req.getLocalHostnames();
	for(String hname : changes.keySet()) {
	  if(!localHostnames.contains(hname)) 
	    throw new PipelineException
	      ("Only a user with Queue Admin privileges may may edit the properties of a " + 
	       "job server (" + hname + ") which is not the local host!");
	}
      }
      
      TreeMap<String,QueueHostMod> finalChanges = new TreeMap<String, QueueHostMod>();
      
      SelectionScheduleMatrix matrix = null;
      
      /* This block looks at all the pending mods and checks to see if they are
       * modifying the schedule property of any of the hosts.  If the schedule is
       * being modified, then we need to create a QueueHostMod that accurately reflects
       * changes caused by the schedule and then add in any changes from the user
       * submitted mod that are still applicable.  
       */
      timer.aquire();
      synchronized(pHostsInfo) {
	synchronized(pSelectionSchedules) {
	  timer.resume();
	  matrix = new SelectionScheduleMatrix(pSelectionSchedules, System.currentTimeMillis());
	}
	Set<String> scheduleNames = matrix.getScheduleNames();
	for(String hname : changes.keySet()) {
	  QueueHostMod mod = changes.get(hname);
	  boolean edited = false;
	  QueueHostInfo info = pHostsInfo.get(hname);
	  
	  String scheduleName = null;
	  
	  String currentSched = null;
	  if (info != null )
	    currentSched = info.getSelectionSchedule();
	  
	  if (mod.isSelectionScheduleModified()) {
	    String newSched = mod.getSelectionSchedule();
	    if (newSched != null && scheduleNames.contains(newSched)) 
	      scheduleName = newSched;
	  } 
	  else if (currentSched != null && scheduleNames.contains(currentSched)) 
	    scheduleName = currentSched;
	  
	  if (scheduleName != null && info != null) {
	    QueueHostMod schedMod = 
	      QueueHostMod.getModFromSchedule(matrix, scheduleName, info);
	    QueueHostMod.combineMods(schedMod, mod);
	    finalChanges.put(hname, schedMod);
	    edited = true;
	  }
	  if (!edited)
	    finalChanges.put(hname, mod);
	}
      } // synchronized(pHostsInfo) 

      timer.aquire();
      synchronized(pHostsMod) {
	timer.resume();
	pHostsMod.putAll(finalChanges);
      }

      updatePendingHostChanges(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }    
    
    return new SuccessRsp(timer);
  }

  /**
   * Update the read-only cache of job server info
   */ 
  private void 
  updateHostsInfo
  (
   TaskTimer timer
  ) 
  {
    timer.aquire();
    synchronized(pHostsInfo) {
      synchronized(pHosts) {
	timer.resume();  
	
	pHostsInfo.clear();
	for(String hname : pHosts.keySet()) 
	  pHostsInfo.put(hname, pHosts.get(hname).toInfo());
      }
    }
      
    updatePendingHostChanges(timer); 
  }

  /**
   * Update the client copy of the host info table to reflect pending changes to the 
   * hosts which have not yet been applied.
   */ 
  private void 
  updatePendingHostChanges
  (
   TaskTimer timer
  ) 
  {
    timer.aquire();
    synchronized(pHostsInfo) {
      synchronized(pHostsMod) {
	timer.resume();
      
	for(String hname : pHostsMod.keySet()) {
	  QueueHostInfo qinfo = pHostsInfo.get(hname);
	  if(qinfo != null) {
	    QueueHostMod qmod = pHostsMod.get(hname);

	    if(qmod.isStatusModified()) {
	      switch(qmod.getStatus()) {
	      case Enable:
		qinfo.setStatus(QueueHostStatus.Enabling);
		break;
		
	      case Disable:
		qinfo.setStatus(QueueHostStatus.Disabling);
		break;
		
	      case Terminate:
		qinfo.setStatus(QueueHostStatus.Terminating);
	      }
	    }

	    if(qmod.isReservationModified()) 
	      qinfo.setReservation(qmod.getReservation()); 

	    if(qmod.isOrderModified()) 
	      qinfo.setOrder(qmod.getOrder()); 

	    if(qmod.isJobSlotsModified()) 
	      qinfo.setJobSlots(qmod.getJobSlots()); 

	    if(qmod.isSelectionGroupModified()) 
	      qinfo.setSelectionGroup(qmod.getSelectionGroup());
	    
	    if(qmod.isHardwareGroupModified()) 
	      qinfo.setHardwareGroup(qmod.getHardwareGroup()); 

	    if(qmod.isSelectionScheduleModified()) 
	      qinfo.setSelectionSchedule(qmod.getSelectionSchedule());
	    
	    if (qinfo.getSelectionSchedule() != null) {
	      qinfo.setGroupState(qmod.getGroupState());
	      qinfo.setOrderState(qmod.getOrderState());
	      qinfo.setSlotsState(qmod.getSlotsState());
	      qinfo.setStatusState(qmod.getStatusState());
	      qinfo.setReservationState(qmod.getReservationState());
	    }
	    else {
	      qinfo.setGroupState(EditableState.Manual);
	      qinfo.setOrderState(EditableState.Manual);
	      qinfo.setSlotsState(EditableState.Manual);
	      qinfo.setStatusState(EditableState.Manual);
	      qinfo.setReservationState(EditableState.Manual);
	    }
	  }
	}
      }
    }
  }

  /**
   * Apply the changes previously requested to the hosts.
   */ 
  private void 
  applyHostEdits  
  (
   TaskTimer timer
  ) 
    throws PipelineException
  {
    /* for post-modify hosts tasks */ 
    TreeSet<String> modifiedHosts = null;
    if(hasAnyExtensionTasks(timer, new ModifyHostsExtFactory()))
       modifiedHosts = new TreeSet<String>();

    timer.aquire();
    synchronized(pHosts) {
      timer.resume();

      timer.suspend();
      TaskTimer tm = new TaskTimer("Dispatcher [Host Status Changes]");
      boolean diskModified = false;  
      long now = System.currentTimeMillis();
      {
	/* attempt to re-Enable previously Unknown servers */ 
	for(QueueHost host : pHosts.values()) {
	  switch(host.getStatus()) {
	  case Limbo:
	    if((host.getLastModified() + sReenableInterval) < now) {
              LogMgr.getInstance().log
                (LogMgr.Kind.Net, LogMgr.Level.Info,
                 "Attempting to re-Enable the Job Manager (" + host.getName() + ") which " +
                 "was previously Limbo after waiting for (" + sReenableInterval + ") msec."); 

	      setHostStatus(host, QueueHost.Status.Enabled);	      
	      if(modifiedHosts != null) 
		modifiedHosts.add(host.getName());
	    }
	  }
	}
	  
	TreeMap<String, QueueHostMod> changes = new TreeMap<String, QueueHostMod>();
	/* status */ 
	{
	  /* make a copy of pending status changes before attempting network communication 
	     so that the pHostsMod lock will be held for only a short amount of time */
	  TreeMap<String,QueueHostStatusChange> statusChanges = null;
	  {
	    tm.aquire();
	    synchronized(pHostsMod) {
	      tm.resume();
	      statusChanges = new TreeMap<String,QueueHostStatusChange>();
	      for(String hname : pHostsMod.keySet()) {
		QueueHostMod qmod = pHostsMod.get(hname);
		changes.put(hname, qmod);
		if(qmod.isStatusModified()) {
		  statusChanges.put(hname, qmod.getStatus());
		}
	      }
              pHostsMod.clear();
	    }
	  }

	  if(statusChanges != null) {
	    for(String hname : statusChanges.keySet()) {
	      QueueHostStatusChange change = statusChanges.get(hname);
	      QueueHost host = pHosts.get(hname);
	      if((change != null) && (host != null)) {
                switch(change) {
		case Enable:
                  setHostStatus(host, QueueHost.Status.Enabled);
                  break;
		  
		case Disable:
                  setHostStatus(host, QueueHost.Status.Disabled);
                  break;
		  
		case Terminate:
                  setHostStatus(host, QueueHost.Status.Shutdown);
		}

                if(modifiedHosts != null) 
                  modifiedHosts.add(hname);
              }
            }
          }
	}

	/* cancel holds on non-enabled servers */ 
	for(QueueHost host : pHosts.values()) {
	  switch(host.getStatus()) {
	  case Shutdown:
	    host.cancelHolds();
	  }
	}	

	/* other host property changes... */
	for(String hname : changes.keySet()) {
	  QueueHost host = pHosts.get(hname);
	  if(host != null) {
	    QueueHostMod qmod = changes.get(hname);

	    /* user reservations */ 
	    if(qmod.isReservationModified()) {
	      host.setReservation(qmod.getReservation());
	      if(modifiedHosts != null) 
	        modifiedHosts.add(hname);
	      diskModified = true;
	    }

	    /* job orders */ 
	    if(qmod.isOrderModified()) {
	      host.setOrder(qmod.getOrder());
	      if(modifiedHosts != null) 
	        modifiedHosts.add(hname);
	      diskModified = true;
	    }

	    /* job slots */ 
	    if(qmod.isJobSlotsModified()) {
	      host.setJobSlots(qmod.getJobSlots()); 
	      if(modifiedHosts != null) 
	        modifiedHosts.add(hname);
	      diskModified = true;
	    }

	    /* selection groups */ 
	    if(qmod.isSelectionGroupModified()) {
	      tm.aquire();
	      synchronized(pSelectionGroups) {
	        tm.resume();

	        String name = qmod.getSelectionGroup(); 
	        if((name == null) || pSelectionGroups.containsKey(name)) {
	          host.setSelectionGroup(name);
	          if(modifiedHosts != null) 
	            modifiedHosts.add(hname);
	          diskModified = true;
	        }
	      }
	    }

	    /* hardware groups */ 
	    if(qmod.isHardwareGroupModified()) {
	      tm.aquire();
	      synchronized(pHardwareGroups) {
	        tm.resume();

	        String name = qmod.getHardwareGroup(); 
	        if((name == null) || pHardwareGroups.containsKey(name)) {
	          host.setHardwareGroup(name);
	          if(modifiedHosts != null) 
	            modifiedHosts.add(hname);
	          diskModified = true;
	        }
	      }
	    }	      

	    /* selection schedules */ 
	    if(qmod.isSelectionScheduleModified()) {
	      tm.aquire();
	      synchronized(pSelectionSchedules) {
	        tm.resume();

	        String name = qmod.getSelectionSchedule();
	        if((name == null) || pSelectionSchedules.containsKey(name)) {
	          host.setSelectionSchedule(name);
	          if(modifiedHosts != null) 
	            modifiedHosts.add(hname);
	          diskModified = true;
	        }
	      }
	    }

	    /* Editable States */
	    {
	      host.setGroupState(qmod.getGroupState());
	      host.setOrderState(qmod.getOrderState());
	      host.setReservationState(qmod.getReservationState());
	      host.setSlotsState(qmod.getSlotsState());
	      host.setStatusState(qmod.getStatusState());
	    }
	  }
	}
	    

	/* write changes to host database file disk */ 
	if(diskModified) 
	  writeHosts();
      }
      LogMgr.getInstance().logSubStage
	(LogMgr.Kind.Dsp, LogMgr.Level.Finest,
	 tm, timer); 
    }


    /* apply collector generated changes to the hosts */ 
    {
      timer.suspend();
      TaskTimer tm = new TaskTimer("Dispatcher [Collector Changes]");
      tm.aquire();
      synchronized(pHosts) {
	tm.resume();
	
	/* latest resource samples */ 
	tm.aquire();
	synchronized(pSamples) {
	  tm.resume();
	  
	  for(String hname : pSamples.keySet()) {
	    QueueHost host = pHosts.get(hname);
	    if(host != null) {
	      switch(host.getStatus()) {
	      case Enabled:
	      case Disabled:
		{
		  ResourceSampleCache cache = pSamples.get(hname);
		  if(cache != null) {
		    Long lastStamp = cache.getLastTimeStamp();
		    ResourceSample sample = host.getLatestSample();
		    if((lastStamp != null) &&
                       ((sample == null) || (sample.getTimeStamp() < lastStamp)))
                      host.setLatestSample(cache.getLatestSample());
		  }
		}
	      }
	    }
	  }
	}
	
	/* operating system type */ 
	tm.aquire();
	synchronized(pOsTypeChanges) {
	  tm.resume();
	  
	  for(String hname : pOsTypeChanges.keySet()) {
	    QueueHost host = pHosts.get(hname);
	    if(host != null) {
	      switch(host.getStatus()) {
	      case Enabled:
	      case Disabled:
		host.setOsType(pOsTypeChanges.get(hname));
		if(modifiedHosts != null) 
		  modifiedHosts.add(hname);
	      }
	    }
	  }
	  
	  pOsTypeChanges.clear();
	}
	
	/* number of processors */ 
	tm.aquire();
	synchronized(pNumProcChanges) {
	  tm.resume();
	  
	  for(String hname : pNumProcChanges.keySet()) {
	    QueueHost host = pHosts.get(hname);
	    if(host != null) {
	      switch(host.getStatus()) {
	      case Enabled:
	      case Disabled:
		host.setNumProcessors(pNumProcChanges.get(hname));
		if(modifiedHosts != null) 
		  modifiedHosts.add(hname);
	      }
	    }
	  }
	  
	  pNumProcChanges.clear();
	}
	
	/* total memory */ 
	tm.aquire();
	synchronized(pTotalMemoryChanges) {
	  tm.resume();
	  
	  for(String hname : pTotalMemoryChanges.keySet()) {
	    QueueHost host = pHosts.get(hname);
	    if(host != null) {
	      switch(host.getStatus()) {
	      case Enabled:
	      case Disabled:
		host.setTotalMemory(pTotalMemoryChanges.get(hname));
		if(modifiedHosts != null) 
		  modifiedHosts.add(hname);
	      }
	    }
	  }

	  pTotalMemoryChanges.clear();
	}
	
	/* total disk */ 
	tm.aquire();
	synchronized(pTotalDiskChanges) {
	  tm.resume();
	  
	  for(String hname : pTotalDiskChanges.keySet()) {
	    QueueHost host = pHosts.get(hname);
	    if(host != null) {
	      switch(host.getStatus()) {
	      case Enabled:
	      case Disabled:
		host.setTotalDisk(pTotalDiskChanges.get(hname));
		  if(modifiedHosts != null) 
		    modifiedHosts.add(hname);
	      }
	    }
	  }
	  
	  pTotalDiskChanges.clear();
	}
      }
      LogMgr.getInstance().logSubStage
	(LogMgr.Kind.Dsp, LogMgr.Level.Finest,
	 tm, timer); 
    }
    
    /* post-modify hosts tasks */ 
    if((modifiedHosts != null) && !modifiedHosts.isEmpty()) {
      TreeMap<String,QueueHostInfo> mhosts = new TreeMap<String,QueueHostInfo>();
      {
	timer.aquire();
	synchronized(pHosts) {
	  timer.resume();
	  
	  for(String hname : modifiedHosts) {
	    QueueHost host = pHosts.get(hname);
	    if(host != null) 
	      mhosts.put(hname, host.toInfo());
	  }
	}
      }
      
      startExtensionTasks(timer, new ModifyHostsExtFactory(mhosts));
    }
  }

  /**
   * Helper for setting host status which interrupts any tasks waiting on the host.<P> 
   * 
   * This method should only be called from inside a synchronized(pHosts) block!
   */ 
  private void 
  setHostStatus
  (
   QueueHost host, 
   QueueHost.Status status
  ) 
  {
    String hname = host.getName(); 

    /* if its already Limbo or Shutdown we can ignore request to make it Limbo */ 
    switch(status) {
    case Limbo:
      switch(host.getStatus()) {
      case Limbo:
      case Shutdown:
        return;
      }
    }                            

    QueueHost.Status newStatus = status;
    switch(newStatus) {
    case Enabled:
      {
        /* test connection before changing the state */ 
        JobMgrControlClient client = null;
        try {
          client = new JobMgrControlClient(hname);
          client.verifyConnection();
          
          /* restart the MonitorTask threads for any jobs still running on the host */ 
          resumeMonitoringJobs(hname); 
        }
        catch(Exception ex) {
          newStatus = QueueHost.Status.Limbo;

          String header = 
            ("Unable to establish contact with the Job Manager (" + hname + ") in order " + 
             "to Enable it."); 
          String msg = header;
          if(!(ex instanceof PipelineException))
            msg = Exceptions.getFullMessage(header, ex);
          LogMgr.getInstance().log(LogMgr.Kind.Net, LogMgr.Level.Warning, msg); 
        }
        finally {
          if(client != null) 
            client.disconnect(); 
        }
      }
      break;

    case Shutdown:
      {
        /* attempt a clean shutdown */ 
        JobMgrControlClient client = null;
        try {
          client = new JobMgrControlClient(hname);
          client.shutdown();
        }
        catch(Exception ex) {
          String header = 
            ("Unable to contact with the Job Manager (" +  hname + ") to tell it to " + 
             "perform a clean Shutdown, but the server will be marked as Shutdown anyway."); 
          String msg = header;
          if(!(ex instanceof PipelineException))
            msg = Exceptions.getFullMessage(header, ex);
          LogMgr.getInstance().log(LogMgr.Kind.Net, LogMgr.Level.Warning, msg); 
        }
        finally {
          if(client != null) 
            client.disconnect(); 
        }
      }
    }

    /* change the status */ 
    host.setStatus(newStatus);

    /* if shutting down, make sure all MonitorTask threads are running so that any Limbo 
         jobs will finish with a Failed state */ 
    switch(newStatus) {
    case Shutdown:
      resumeMonitoringJobs(hname); 
    }

    /* if we're out of contact, cause all Job Mananger clients to break out of long 
         transactions at the first timeout */ 
    switch(newStatus) {
    case Shutdown:
    case Limbo:
      JobMgrControlClient.serverUnreachable(hname); 
    }    
  }
  

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the full system resource usage history of the given host.
   * 
   * @param req 
   *   The resource samples request.
   *    
   * @return 
   *   <CODE>QueueGetHostResourceSamplesRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the host resouces.
   */ 
  public Object
  getHostResourceSamples
  (
   QueueGetHostResourceSamplesReq req
  )
  {
    TaskTimer timer = new TaskTimer();

    TreeMap<String,ResourceSampleCache> samples = new TreeMap<String,ResourceSampleCache>();

    timer.aquire();
    synchronized(pSamples) {
      timer.resume();
      
      TreeMap<String,TimeInterval> intervals = req.getIntervals(); 
      for(String hname : intervals.keySet()) {
	TimeInterval interval = intervals.get(hname);
	if(interval != null) {
	  ResourceSampleCache cache = pSamples.get(hname);

	  /* see if any of the samples are in the runtime cache */ 
	  int liveSamples = 0;
	  Long first = null;
	  if(cache != null) {
	    liveSamples = cache.getNumSamplesDuring(interval);
	    first = cache.getFirstTimeStamp();
	  }
		  
	  /* load earlier sampls from disk? */ 
	  ResourceSampleCache tcache = null;
	  if(!req.runtimeOnly() && 
	     ((first == null) || (interval.getStartStamp() < first)))
	    tcache = readSamples(timer, hname, interval, liveSamples);

	  /* combine runtime and newly read disk samples */ 
	  if(tcache != null) {
	    if(liveSamples > 0) 
	      tcache.addAllSamplesDuring(cache, interval);
	    samples.put(hname, tcache);
	  }
	  else if((cache != null) && (liveSamples > 0)) {
	    ResourceSampleCache ncache = cache.cloneDuring(interval);
	    if(ncache != null) 
	      samples.put(hname, ncache); 
	  }
	}
      }
    }
      
    return new QueueGetHostResourceSamplesRsp(timer, samples);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets frequency distribution data for significant catagories of information shared 
   * by all job server hosts.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>QueueGetHostHistogramsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to compute the histograms.
   */ 
  public Object
  getHostHistograms
  (
   QueueGetHostHistogramsReq req
  ) 
  {
    TaskTimer timer = new TaskTimer();

    /* create the empty histograms from the specs */ 
    QueueHostHistogramSpecs specs = req.getSpecs();
    updateHistogramSpecs(timer, specs); 
    QueueHostHistograms hists = new QueueHostHistograms(specs);

    /* sort the host information into histogram catagories */ 
    timer.aquire();
    synchronized(pHostsInfo) {
      timer.resume();
      
      for(QueueHostInfo qinfo : pHostsInfo.values()) 
	hists.catagorize(qinfo);

      return new QueueGetHostHistogramsRsp(timer, hists);
    }
  }
  
  /**
   * Update the catagories for the dynamically determined histograms.
   */ 
  private void
  updateHistogramSpecs
  (
   TaskTimer timer, 
   QueueHostHistogramSpecs specs
  ) 
  {
    if(specs == null) 
      return;

    TreeSet<String> usedReservations = new TreeSet<String>();
    TreeSet<Integer> usedOrders = new TreeSet<Integer>();
    {
      timer.aquire();
      synchronized(pHostsInfo) {
	timer.resume();
	
	for(QueueHostInfo qinfo : pHostsInfo.values()) {
	  String reserve = qinfo.getReservation();
	  if(reserve != null) 
	    usedReservations.add(reserve); 
	  
	  usedOrders.add(qinfo.getOrder());
	}
      }
    }
      
    {
      TreeSet<HistogramRange> ranges = new TreeSet<HistogramRange>();
      ranges.add(new HistogramRange("-"));
      for(String reserve : usedReservations) 
	ranges.add(new HistogramRange(reserve));
      
      HistogramSpec oldSpec = specs.getReservationSpec();
      HistogramSpec newSpec = new HistogramSpec("Reserved", ranges);
      for(HistogramRange range : oldSpec.getIncluded())
	newSpec.setIncluded(range, true);
      
      specs.setReservationSpec(newSpec); 
    }
    
    {
      TreeSet<HistogramRange> ranges = new TreeSet<HistogramRange>();
      for(Integer order : usedOrders) 
	ranges.add(new HistogramRange(order));

      if(ranges.isEmpty()) 
        ranges.add(new HistogramRange(0));
      
      HistogramSpec oldSpec = specs.getOrderSpec();
      HistogramSpec newSpec = new HistogramSpec("Order", ranges);
      for(HistogramRange range : oldSpec.getIncluded())
	newSpec.setIncluded(range, true);
      
      specs.setOrderSpec(newSpec); 
    }
    
    {
      TreeSet<HistogramRange> ranges = new TreeSet<HistogramRange>();
      ranges.add(new HistogramRange("-"));
      synchronized(pSelectionGroups) {
	for(String sname : pSelectionGroups.keySet())
	  ranges.add(new HistogramRange(sname));
      }
      
      HistogramSpec oldSpec = specs.getSelectionGroupsSpec();
      HistogramSpec newSpec = new HistogramSpec("SelectionGroups", ranges);
      for(HistogramRange range : oldSpec.getIncluded())
	newSpec.setIncluded(range, true);
      
      specs.setSelectionGroupsSpec(newSpec); 
    }
    
    {
      TreeSet<HistogramRange> ranges = new TreeSet<HistogramRange>();
      ranges.add(new HistogramRange("-"));
      synchronized(pSelectionSchedules) {
	for(String sname : pSelectionSchedules.keySet())
	  ranges.add(new HistogramRange(sname));
      }
      
      HistogramSpec oldSpec = specs.getSelectionSchedulesSpec();
      HistogramSpec newSpec = new HistogramSpec("SelectionSchedules", ranges); 
      for(HistogramRange range : oldSpec.getIncluded())
	  newSpec.setIncluded(range, true);
      
      specs.setSelectionSchedulesSpec(newSpec); 
    }

    {
      TreeSet<HistogramRange> ranges = new TreeSet<HistogramRange>();
      ranges.add(new HistogramRange("-"));
      synchronized(pHardwareGroups) {
	for(String sname : pHardwareGroups.keySet())
	  ranges.add(new HistogramRange(sname));
      }
      
      HistogramSpec oldSpec = specs.getHardwareGroupsSpec();
      HistogramSpec newSpec = new HistogramSpec("HardwareGroups", ranges);
      for(HistogramRange range : oldSpec.getIncluded())
	newSpec.setIncluded(range, true);
      
      specs.setHardwareGroupsSpec(newSpec); 
    }
    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   J O B S                                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the per-file JobState of each file associated with the given working version and 
   * primary file sequence. <P> 
   * 
   * Any jobs which were submitted before the working version was created will be treated 
   * as if they didn't exist.  
   * 
   * @param req 
   *   The job states request.
   *    
   * @return 
   *   <CODE>QueueGetJobStatesRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the job states.
   */ 
  public Object
  getJobStates
  (
   QueueGetJobStatesReq req
  )
  {
    TaskTimer timer = new TaskTimer();

    NodeID nodeID = req.getNodeID();
    long stamp = req.getTimeStamp();

    TreeMap<File,Long> nodeJobIDs = new TreeMap<File,Long>();
    timer.aquire();
    synchronized(pNodeJobIDs) {
      timer.resume();
      TreeMap<File,Long> table = pNodeJobIDs.get(nodeID);
      if(table != null) 
	nodeJobIDs.putAll(table);
    }
	  
    timer.aquire();  
    synchronized(pJobInfo) {
      timer.resume();

      FileSeq fseq = req.getFileSeq();
      int frames = fseq.numFrames();
      
      ArrayList<Long>     jobIDs = new ArrayList<Long>(frames);
      ArrayList<JobState> states = new ArrayList<JobState>(frames);
      
      for(File file : fseq.getFiles()) {
	Long jobID = nodeJobIDs.get(file);	  
	JobState jstate = null;
	{
	  QueueJobInfo info = null;
	  if(jobID != null) 
	    info = pJobInfo.get(jobID);	   
	  
	  if((info != null) && (info.getSubmittedStamp() > stamp))
	    jstate = info.getState();
	  else 
	    jobID = null;
	}
	jobIDs.add(jobID);
	states.add(jstate);
      }
      
      return new QueueGetJobStatesRsp(timer, nodeID, jobIDs, states);
    }
  }

  /**
   * Get the job IDs of unfinished jobs associated with the given nodes.
   * 
   * @param req 
   *   The request.
   *    
   * @return 
   *   <CODE>GetUnfinishedJobsForNodesRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the job IDs.
   */ 
  public Object
  getUnfinishedJobsForNodes
  (
   QueueGetUnfinishedJobsForNodesReq req
  )
  {
    TaskTimer timer = new TaskTimer("QueueMgr.getUnfinishedJobsForNodes()");

    String author = req.getAuthor();
    String view   = req.getView();

    TreeMap<String,FileSeq> fseqs = req.getFileSeqs();

    TreeMap<File,Long> nodeJobIDs = new TreeMap<File,Long>();
    timer.aquire();
    synchronized(pNodeJobIDs) {
      timer.resume();
      for(String name : fseqs.keySet()) {
	TreeMap<File,Long> table = pNodeJobIDs.get(new NodeID(author, view, name));
	if(table != null) 
	  nodeJobIDs.putAll(table);
      }
    }
	  
    TreeMap<String,TreeSet<Long>> jobIDs = new TreeMap<String,TreeSet<Long>>();

    timer.aquire();  
    synchronized(pJobInfo) {
      timer.resume();

      for(String name : fseqs.keySet()) {
	for(File file : fseqs.get(name).getFiles()) {
	  Long jobID = nodeJobIDs.get(file);
	  if(jobID != null) {
	    QueueJobInfo info = pJobInfo.get(jobID);	   
	    if(info != null) {
	      switch(info.getState()) {
	      case Queued:
	      case Preempted:
	      case Paused:
	      case Running:
	      case Limbo:
		{
		  TreeSet<Long> ids = jobIDs.get(name);
		  if(ids == null) {
		    ids = new TreeSet<Long>();
		    jobIDs.put(name, ids);
		  }

		  ids.add(jobID);
		}
	      }
	    }
	  }
	}
      }
    }
      
    return new GetUnfinishedJobsForNodesRsp(timer, jobIDs);
  }

  /**
   * Get the job IDs of unfinished jobs which will regenerate the given files of a 
   * working node.
   * 
   * @param req 
   *   The request.
   *    
   * @return 
   *   <CODE>GetUnfinishedJobsForNodeFilesRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the job IDs.
   */ 
  public Object
  getUnfinishedJobsForNodeFiles
  (
   QueueGetUnfinishedJobsForNodeFilesReq req
  )
  {
    TaskTimer timer = new TaskTimer("QueueMgr.getUnfinishedJobsForNodeFiles()");

    TreeMap<File,Long> nodeJobIDs = new TreeMap<File,Long>();
    timer.aquire();
    synchronized(pNodeJobIDs) {
      timer.resume();
      TreeMap<File,Long> table = pNodeJobIDs.get(req.getNodeID());
      if(table != null) 
	nodeJobIDs.putAll(table);
    }
    
    TreeSet<Long> jobIDs = new TreeSet<Long>();

    timer.aquire();  
    synchronized(pJobInfo) {
      timer.resume();

      for(File file : req.getFiles()) {
	Long jobID = nodeJobIDs.get(file);
	if(jobID != null) {
	  QueueJobInfo info = pJobInfo.get(jobID);	   
	  if(info != null) {
	    switch(info.getState()) {
	    case Queued:
	    case Preempted:
	    case Paused:
	    case Running:
	    case Limbo:
	      jobIDs.add(jobID);
	    }
	  }
	}
      }
    }
      
    return new GetUnfinishedJobsForNodeFilesRsp(timer, jobIDs);
  }

  /**
   * Get the distribution of job states for the jobs associated with each of the given 
   * job group IDs.
   * 
   * @param req 
   *   The request.
   * 
   * @return 
   *   <CODE>QueueGetJobStateDistributionRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the job states.
   */ 
  public Object
  getJobStateDistribution
  (
   QueueGetJobStateDistributionReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer();
    
    TreeMap<Long,double[]> dist = new TreeMap<Long,double[]>();
    for(Long groupID : req.getGroupIDs()) 
      dist.put(groupID, pJobCounters.getDistribution(timer, groupID));
    
    return new QueueGetJobStateDistributionRsp(timer, dist);
  }

  /**
   * Get the JobStatus of all jobs associated with the given job group IDs. 
   * 
   * @param req 
   *   The job status request.
   * 
   * @return 
   *   <CODE>QueueGetJobStatusRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the job status.
   */ 
  public Object
  getJobStatus
  (
   QueueGetJobStatusReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer();

    TreeSet<Long> jobIDs = new TreeSet<Long>();
    timer.aquire();
    synchronized(pJobGroups) {
      timer.resume();
      for(Long groupID : req.getGroupIDs()) {
	QueueJobGroup group = pJobGroups.get(groupID);
	if(group != null) 
	  jobIDs.addAll(group.getAllJobIDs());
      }
    }

    TreeMap<Long,JobState> states = new TreeMap<Long,JobState>();
    timer.aquire();  
    synchronized(pJobInfo) {
      timer.resume();
      for(Long jobID : jobIDs) {
	QueueJobInfo info = pJobInfo.get(jobID);
	if(info != null) 
	  states.put(jobID, info.getState());
      }
    }
	
    timer.aquire();  
    synchronized(pJobs) {
      timer.resume();
      TreeMap<Long,JobStatus> status = new TreeMap<Long,JobStatus>();
      for(Long jobID : jobIDs) {
	QueueJob job = pJobs.get(jobID);	
	JobState state = states.get(jobID);
	if((job != null) && (state != null)) {
	  ActionAgenda agenda = job.getActionAgenda();
	  JobStatus js = 
	    new JobStatus(jobID, job.getNodeID(), state, agenda.getToolset(), 
			  agenda.getPrimaryTarget(), job.getSourceJobIDs());
	  status.put(jobID, js);
	}
      }
      
      return new QueueGetJobStatusRsp(timer, status);
    }
  }

  /**
   * Get the JobStatus of all currently running jobs. <P> 
   * 
   * @return
   *   <CODE>QueueGetJobStatusRsp</CODE> if successful.
   */ 
  public Object
  getRunningJobStatus() 
  {
    TaskTimer timer = new TaskTimer();

    TreeMap<Long,JobState> jobStates = new TreeMap<Long,JobState>();
    timer.aquire();  
    synchronized(pJobInfo) {
      timer.resume();
      for(Map.Entry<Long,QueueJobInfo> entry : pJobInfo.entrySet()) {
        Long jobID = entry.getKey();
        QueueJobInfo info = entry.getValue(); 
	switch(info.getState()) {
	case Running:
        case Limbo:
	  jobStates.put(jobID, info.getState()); 
	}
      }
    }
	
    timer.aquire();  
    synchronized(pJobs) {
      timer.resume();
      TreeMap<Long,JobStatus> running = new TreeMap<Long,JobStatus>();
      for(Map.Entry<Long,JobState> entry : jobStates.entrySet()) {
        Long jobID = entry.getKey();
	QueueJob job = pJobs.get(jobID);	
	if(job != null) {
	  ActionAgenda agenda = job.getActionAgenda();
	  JobStatus status = 
	    new JobStatus(jobID, job.getNodeID(), entry.getValue(), agenda.getToolset(), 
			  agenda.getPrimaryTarget(), job.getSourceJobIDs());
	  running.put(jobID, status);
	}
      }
      
      return new QueueGetJobStatusRsp(timer, running);
    }
  }

  /**
   * Get the jobs with the given IDs. <P> 
   * 
   * @param req 
   *   The job request.
   *    
   * @return 
   *   <CODE>QueueGetJobRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the job.
   */ 
  public Object
  getJobs
  (
   QueueGetJobReq req
  )
  {
    TaskTimer timer = new TaskTimer();
    timer.aquire();
    synchronized(pJobs) {
      timer.resume();
      try {
        Set<Long> jobIDs = req.getJobIDs();
        TreeMap<Long, QueueJob> toReturn = new TreeMap<Long, QueueJob>();
        for (Long jobID : jobIDs) {
          QueueJob job = pJobs.get(jobID);
          if(job == null) 
            throw new PipelineException
              ("No job (" + jobID + ") exists!");
          toReturn.put(jobID, job);
        }
	if (toReturn.size() == 1)
	  return new QueueGetJobRsp(timer, toReturn.get(toReturn.firstKey()));
	return new QueueGetJobRsp(timer, toReturn);
      }
      catch(PipelineException ex) {
	return new FailureRsp(timer, ex.getMessage());	  
      }   
    }
  }

  /**
   * Get information about the current status of jobs in the queue. <P> 
   * 
   * @param req 
   *   The job info request.
   *    
   * @return 
   *   <CODE>QueueGetJobInfoRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the job info.
   */ 
  public Object
  getJobInfos
  (
   QueueGetJobInfoReq req
  )
  {
    TaskTimer timer = new TaskTimer();
    timer.aquire();
    synchronized(pJobInfo) {
      timer.resume();
      try {
        Set<Long> jobIDs = req.getJobIDs();
        TreeMap<Long, QueueJobInfo> toReturn = new TreeMap<Long, QueueJobInfo>();
        for (Long jobID : jobIDs) {  
          QueueJobInfo info = pJobInfo.get(jobID);
          if(info == null) 
            throw new PipelineException
              ("No information is available for job (" + jobID + ") exists!");
          toReturn.put(jobID, info);
        }
        if (toReturn.size() == 1)
          return new QueueGetJobInfoRsp(timer, toReturn.get(toReturn.firstKey()));
        return new QueueGetJobInfoRsp(timer, toReturn);
      }
      catch(PipelineException ex) {
	return new FailureRsp(timer, ex.getMessage());	  
      }   
    }
  }

  /**
   * Get information about the currently running jobs. <P> 
   * 
   * @return
   *   <CODE>QueueGetRunningJobInfoRsp</CODE> if successful.
   */ 
  public Object
  getRunningJobInfo() 
  {
    TaskTimer timer = new TaskTimer();
    TreeMap<Long,QueueJobInfo> running = new TreeMap<Long,QueueJobInfo>();

    timer.aquire();  
    synchronized(pJobInfo) {
      timer.resume(); 
      for(Map.Entry<Long,QueueJobInfo> entry : pJobInfo.entrySet()) {
        Long jobID = entry.getKey();
        QueueJobInfo info = entry.getValue(); 
        switch(info.getState()) {
        case Running:
        case Limbo:
          running.put(jobID, info);
        }
      }
    }

    return new QueueGetRunningJobInfoRsp(timer, running);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Submit a set of jobs to be executed by the queue. <P> 
   * 
   * @param req 
   *   The job submission request.
   *    
   * @return 
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable submit the jobs.
   */ 
  public Object
  submitJobs
  (
   QueueSubmitJobsReq req
  )
  {
    TaskTimer timer = new TaskTimer("QueueMgr.submitJobs():");

    try {
      QueueJobGroup group = req.getJobGroup();
      timer.aquire();
      synchronized(pJobGroups) {
	timer.resume();
	writeJobGroup(group);
	pJobGroups.put(group.getGroupID(), group); 
	pJobCounters.initCounters(timer, group);
      }

      TreeMap<Long,QueueJob> taskJobs = new TreeMap<Long,QueueJob>();
      for(QueueJob job : req.getJobs()) {
	long jobID = job.getJobID();
	QueueJobInfo info = new QueueJobInfo(jobID);

	taskJobs.put(jobID, job);
	
	timer.aquire();
	synchronized(pJobs) {
	  timer.resume();
	  writeJob(job);
	  pJobs.put(jobID, job);
	}
	
	timer.aquire();
	synchronized(pJobInfo) {
	  timer.resume();
	  writeJobInfo(info);
	  pJobInfo.put(jobID, info);
	  pJobCounters.update(timer, null, info);
	} 
	
	{
	  ActionAgenda agenda = job.getActionAgenda();
	  NodeID nodeID = agenda.getNodeID();
	  FileSeq fseq = agenda.getPrimaryTarget();
	  
	  timer.aquire();
	  synchronized(pNodeJobIDs) {
	    timer.resume();
	    TreeMap<File,Long> table = pNodeJobIDs.get(nodeID);
	    if(table == null) {
	      table = new TreeMap<File,Long>();
	      pNodeJobIDs.put(nodeID, table);
	    }
	    
	    for(File file : fseq.getFiles()) 
	      table.put(file, jobID);
	  }
	}
	
	pWaiting.add(jobID);
      }

      /* post-submit tasks */ 
      startExtensionTasks(timer, new SubmitJobsExtFactory(group, taskJobs));

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }     
  }

  /*----------------------------------------------------------------------------------------*/

  /**
   * Preempt the jobs with the given IDs. <P> 
   * 
   * @param req 
   *   The request.
   *    
   * @return 
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable preempt the jobs. 
   */ 
  public Object
  preemptJobs
  (
   QueueJobsReq req
  )
  {
    TaskTimer timer = new TaskTimer("QueueMgr.preemptJobs()");

    try {
      boolean unprivileged = false; 

      timer.aquire();
      synchronized(pJobs) {
	timer.resume();
      
	for(Long jobID : req.getJobIDs()) {
	  QueueJob job = pJobs.get(jobID);
	  if(job != null) {
	    String author = job.getActionAgenda().getNodeID().getAuthor();
	    if(pAdminPrivileges.isQueueManaged(req, author)) 
	      pPreemptList.add(jobID);
	    else 
	      unprivileged = true;
	  }
	}
      }

      if(unprivileged)
	throw new PipelineException
	  ("Only a user with Queue Admin privileges may preempt jobs owned by another user!");

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }     
  }

  /**
   * Kill the jobs with the given IDs. <P> 
   * 
   * @param req 
   *   The request.
   *    
   * @return 
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable kill the jobs. 
   */ 
  public Object
  killJobs
  (
   QueueJobsReq req
  )
  {
    TaskTimer timer = new TaskTimer("QueueMgr.killJobs()");

    try {
      boolean unprivileged = false; 

      timer.aquire();
      synchronized(pJobs) {
	timer.resume();
      
	for(Long jobID : req.getJobIDs()) {
	  QueueJob job = pJobs.get(jobID);
	  if(job != null) {
	    String author = job.getActionAgenda().getNodeID().getAuthor();
	    if(pAdminPrivileges.isQueueManaged(req, author)) 
	      pHitList.add(jobID);
	    else 
	      unprivileged = true;
	  }
	}
      }

      if(unprivileged)
	throw new PipelineException
	  ("Only a user with Queue Admin privileges may kill jobs owned by another user!");

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }     
  }

  /**
   * Pause the jobs with the given IDs. <P> 
   * 
   * @param req 
   *   The request.
   *    
   * @return 
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable pause the jobs. 
   */ 
  public Object
  pauseJobs
  (
   QueueJobsReq req
  )
  {
    TaskTimer timer = new TaskTimer("QueueMgr.pauseJobs()");

    try {
      boolean unprivileged = false; 

      timer.aquire();
      synchronized(pJobs) {
        timer.resume();
      
	for(Long jobID : req.getJobIDs()) {
	  QueueJob job = pJobs.get(jobID);
	  if(job != null) {
            String author = job.getActionAgenda().getNodeID().getAuthor();
            if(pAdminPrivileges.isQueueManaged(req, author)) {
              QueueJobInfo info = null;
              timer.aquire();
              synchronized(pJobInfo) {
                timer.resume();
                info = pJobInfo.get(jobID);	 
              }
  
              if(info != null) {
                switch(info.getState()) {
                case Queued:
                case Preempted:
                  synchronized(pPause) {
                    pPause.add(jobID);
                  }
                }

                synchronized(pResume) {
                  pResume.remove(jobID);
                }
              }
            }
	    else {
	      unprivileged = true;
            }
	  }
	}
      }

      if(unprivileged)
	throw new PipelineException
	  ("Only a user with Queue Admin privileges may pause jobs owned by another user!");

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }     
  }

  /**
   * Resume execution of the jobs with the given IDs. <P> 
   * 
   * @param req 
   *   The request.
   *    
   * @return 
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable resume the jobs. 
   */ 
  public Object
  resumeJobs
  (
   QueueJobsReq req
  )
  {
    TaskTimer timer = new TaskTimer("QueueMgr.resumeJobs()");

    try {
      boolean unprivileged = false; 

      timer.aquire();
      synchronized(pJobs) {
	timer.resume();
      
	for(Long jobID : req.getJobIDs()) {
	  QueueJob job = pJobs.get(jobID);
	  if(job != null) {
	    String author = job.getActionAgenda().getNodeID().getAuthor();
	    if(pAdminPrivileges.isQueueManaged(req, author)) { 
              QueueJobInfo info = null;
              timer.aquire();
              synchronized(pJobInfo) {
                timer.resume();
                info = pJobInfo.get(jobID);	 
              }
  
              if(info != null) {
                switch(info.getState()) {
                case Paused: 
                  synchronized(pResume) {
                    pResume.add(jobID);
                  }
                }

                synchronized(pPause) {
                  pPause.remove(jobID);
                }
              }
            }
            else {
	      unprivileged = true;
            }
	  }
	}
      }

      if(unprivileged)
	throw new PipelineException
	  ("Only a user with Queue Admin privileges may resume jobs owned by another user!");

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }     
  }
  
  /**
   * Change the job requirements of the jobs with the given IDs. <P> 
   * 
   * @param req 
   *   The request.
   *    
   * @return 
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable resume the jobs. 
   */ 
  public Object
  changeJobReqs
  (
    QueueJobReqsReq req
  )
  {
    TaskTimer timer = new TaskTimer("QueueMgr.changeJobReqs()");
    ArrayList<String> exceptions = new ArrayList<String>();

    TreeMap<Long, QueueJob> jobs = new TreeMap<Long, QueueJob>();
    TreeSet<NodeID> nodeIDs = new TreeSet<NodeID>();
    TreeMap<String, Boolean> privileges = new TreeMap<String, Boolean>();
    
    LinkedList<JobReqsDelta> changes = req.getJobReqsChanges();
    
    timer.aquire();
    synchronized(pJobs) {
      timer.resume();
      for(JobReqsDelta delta : changes) {
        long id = delta.getJobID();
        QueueJob job = pJobs.get(id);
        if (job != null) {
          QueueJob copy = job.queryOnlyCopy(); 
          jobs.put(id, copy);
          NodeID nodeID = copy.getNodeID();
          nodeIDs.add(nodeID);
          String author = nodeID.getAuthor();
          if (!privileges.containsKey(author))
            privileges.put(author, pAdminPrivileges.isQueueManaged(req, author));
        }
      }
    }
    
    TreeMap<String, BaseKeyChooser> selectionKeys = 
      new TreeMap<String, BaseKeyChooser>();
    TreeMap<String, BaseKeyChooser> licenseKeys = 
      new TreeMap<String, BaseKeyChooser>();
    TreeMap<String, BaseKeyChooser> hardwareKeys = 
      new TreeMap<String, BaseKeyChooser>();
    
    synchronized(pSelectionKeys) {
      for (Entry<String, SelectionKey> entry : pSelectionKeys.entrySet()) {
        selectionKeys.put(entry.getKey(), entry.getValue().getKeyChooser());
      }
    }
    
    synchronized(pLicenseKeys) {
      for (Entry<String, LicenseKey> entry : pLicenseKeys.entrySet()) {
        licenseKeys.put(entry.getKey(), entry.getValue().getKeyChooser());
      }
    }
    
    synchronized(pHardwareKeys) {
      for (Entry<String, HardwareKey> entry : pHardwareKeys.entrySet()) {
        hardwareKeys.put(entry.getKey(), entry.getValue().getKeyChooser());
      }
    }
    
    try {
      DoubleMap<NodeID, String, BaseAnnotation> annots = 
        pMasterMgrClient.getAnnotations(nodeIDs);

      boolean unprivileged = false; 

      for(JobReqsDelta delta : changes ) {
        long jobID = delta.getJobID();
        QueueJob job = jobs.get(jobID);
        if(job != null) {
          String author = job.getNodeID().getAuthor();
          if(privileges.get(author)) {
            JobReqs reqs = new JobReqs(job.getJobRequirements(), delta);
            try {
              TreeMap<String, BaseAnnotation> annot = annots.get(job.getNodeID());
              TaskTimer subTimer = new TaskTimer("QueueMgr.adjustJobRequirements()");
              timer.suspend();
              ArrayList<String> msgs = 
                adjustJobRequirements(subTimer, job, reqs, annot, 
                                      selectionKeys, hardwareKeys, licenseKeys);
              exceptions.addAll(msgs);
              timer.accum(subTimer);
            }
            catch (PipelineException ex) {
              exceptions.add(ex.getMessage());
            }

            timer.aquire();
            synchronized (pJobReqsChanges) {
              timer.resume();
              pJobReqsChanges.put(jobID, reqs);
            }
          }
          else 
            unprivileged = true;
        }
      }

      if(unprivileged)
	 exceptions.add
	  ("Some jobs did not have their requirements changed due to lack of Queue Admin " + 
           "or Queue Manager privileges!");
      
      if (exceptions.size() > 0) {
        String msg = "";
        for (String each : exceptions)
          msg += each + "\n\n";
        
        throw new PipelineException
          ("While changing job requirements was successful, the following errors occured " +
           "during KeyChooser execution.  These errors may effect the ability of the jobs " +
           "on the queue to run.\n\n" + msg);
      }

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }     
  }
  
  /**
   * Updates the auto-calculated keys for the jobs with the given IDs. <P> 
   * 
   * @param req 
   *   The request.
   *    
   * @return 
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable resume the jobs. 
   */ 
  public Object
  updateJobKeys
  (
    QueueJobsReq req
  )
  {
    TaskTimer timer = new TaskTimer("QueueMgr.updateJobKeys()");
    ArrayList<String> exceptions = new ArrayList<String>();
    
    TreeMap<Long, QueueJob> jobs = new TreeMap<Long, QueueJob>();
    TreeSet<Long> ids = req.getJobIDs();
    TreeSet<NodeID> nodeIDs = new TreeSet<NodeID>();
    
    TreeMap<String, Boolean> privileges = new TreeMap<String, Boolean>();
    
    timer.aquire();
    synchronized(pJobs) {
      timer.resume();
      for (Long id : ids) {
        QueueJob job = pJobs.get(id);
        if (job != null) {
          QueueJob copy = job.queryOnlyCopy(); 
          jobs.put(id, copy);
          NodeID nodeID = copy.getNodeID();
          nodeIDs.add(nodeID);
          String author = nodeID.getAuthor();
          if (!privileges.containsKey(author))
            privileges.put(author, pAdminPrivileges.isQueueManaged(req, author));
        }
      }
    }
    
    TreeMap<String, BaseKeyChooser> selectionKeys = 
      new TreeMap<String, BaseKeyChooser>();
    TreeMap<String, BaseKeyChooser> licenseKeys = 
      new TreeMap<String, BaseKeyChooser>();
    TreeMap<String, BaseKeyChooser> hardwareKeys = 
      new TreeMap<String, BaseKeyChooser>();
    
    synchronized(pSelectionKeys) {
      for (Entry<String, SelectionKey> entry : pSelectionKeys.entrySet()) {
        selectionKeys.put(entry.getKey(), entry.getValue().getKeyChooser());
      }
    }
    
    synchronized(pLicenseKeys) {
      for (Entry<String, LicenseKey> entry : pLicenseKeys.entrySet()) {
        licenseKeys.put(entry.getKey(), entry.getValue().getKeyChooser());
      }
    }
    
    synchronized(pHardwareKeys) {
      for (Entry<String, HardwareKey> entry : pHardwareKeys.entrySet()) {
        hardwareKeys.put(entry.getKey(), entry.getValue().getKeyChooser());
      }
    }
    
    try {
      DoubleMap<NodeID, String, BaseAnnotation> annots = 
        pMasterMgrClient.getAnnotations(nodeIDs);
      
      boolean unprivileged = false; 
      for(Entry<Long, QueueJob> entry : jobs.entrySet()) {
        QueueJob job = entry.getValue();
        String author = job.getNodeID().getAuthor();
        if(privileges.get(author)) {
          JobReqs reqs = (JobReqs) job.getJobRequirements().clone();
          try {
            TaskTimer subTimer = new TaskTimer("QueueMgr.adjustJobRequirements()");
            timer.suspend();
            TreeMap<String, BaseAnnotation> annot = annots.get(job.getNodeID());
            ArrayList<String> msgs = 
              adjustJobRequirements(subTimer, job, reqs, annot, 
                                    selectionKeys, hardwareKeys, licenseKeys); 
            exceptions.addAll(msgs);
            timer.accum(subTimer);
          }
          catch (PipelineException ex) {
            exceptions.add(ex.getMessage());
          }
          timer.aquire();
          synchronized (pJobReqsChanges) {
            timer.resume();
            pJobReqsChanges.put(entry.getKey(), reqs);
          }
        }
        else 
          unprivileged = true;
      }

      if(unprivileged)
        exceptions.add
         ("Some jobs did not have their permissions changed due to lack of Queue Admin or" +
          "Queue Manager privileges!");
      
      if (exceptions.size() > 0) {
        String msg = "";
        for (String each : exceptions)
          msg += each + "\n\n";
        
        throw new PipelineException
          ("While updating job keys was successful, the following errors occured during " + 
           "KeyChooser execution.  These errors may effect the ability of the jobs on the " + 
           "queue to run.\n\n" + msg);
      }

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());      
    }     
  }
  
  /**
   * Change the given job requirements so that they are correct based on the
   * plugins that are contained in the selection, hardware, and license keys.
   * <p>
   * Note that this method will modify the job requirements that are passed in.  
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
   * @param annots
   *   The list of annotations associated with the nodeID of the current job.  
   *   Should NEVER be <code>null</code>
   *   
   * @param selectionKeys
   *   A map of the selection key choosers indexed by the selection key name.
   *   
   * @param hardwareKeys
   *   A map of the hardware key choosers indexed by the hardware key name.
   *   
   * @param licenseKeys
   *   A map of the license key choosers indexed by the license key name.
   *   
   * @return
   *   A list of the exceptions thrown during key chooser execution.
   */
  private ArrayList<String> 
  adjustJobRequirements
  (
    TaskTimer timer,
    QueueJob job,
    JobReqs jreqs,
    TreeMap<String, BaseAnnotation> annots,
    TreeMap<String, BaseKeyChooser> selectionKeys,
    TreeMap<String, BaseKeyChooser> hardwareKeys,
    TreeMap<String, BaseKeyChooser> licenseKeys
  )
    throws PipelineException
  {
    ArrayList<String> toReturn = new ArrayList<String>();
    
    /* Selection Keys */
    {
      TreeSet<String> finalKeys = new TreeSet<String>();
      Set<String> currentKeys = jreqs.getSelectionKeys();

      for (String name : selectionKeys.keySet()) {
        BaseKeyChooser kc = selectionKeys.get(name);
        if (kc == null && currentKeys.contains(name))
          finalKeys.add(name); 
        else if (kc != null) {
          try {
            if (kc.computeIsActive(job, annots))
              finalKeys.add(name);
          }
          catch (PipelineException e) {
            toReturn.add(e.getMessage());
          }
        }
      }
      jreqs.removeAllSelectionKeys();
      jreqs.addSelectionKeys(finalKeys);
    }

    /* License Keys */
    {
      TreeSet<String> finalKeys = new TreeSet<String>();
      Set<String> currentKeys = jreqs.getLicenseKeys();

      for (String name : licenseKeys.keySet()) {
        BaseKeyChooser kc = licenseKeys.get(name);
        if (kc == null && currentKeys.contains(name))
          finalKeys.add(name); 
        else if (kc != null) {
          try {
            if (kc.computeIsActive(job, annots))
              finalKeys.add(name);
          }
          catch (PipelineException e) {
            toReturn.add(e.getMessage());
          }
        }
      }
      jreqs.removeAllLicenseKeys();
      jreqs.addLicenseKeys(finalKeys);
    }

    /* Hardware Keys */
    {
      TreeSet<String> finalKeys = new TreeSet<String>();
      Set<String> currentKeys = jreqs.getHardwareKeys();

      for (String name : hardwareKeys.keySet()) {
        BaseKeyChooser kc = hardwareKeys.get(name);
        if (kc == null && currentKeys.contains(name))
          finalKeys.add(name); 
        else if (kc != null) {
          try {
            if (kc.computeIsActive(job, annots))
              finalKeys.add(name);
          }
          catch (PipelineException e) {
            toReturn.add(e.getMessage());
          }
        }
      }
      jreqs.removeAllHardwareKeys();
      jreqs.addHardwareKeys(finalKeys);
    }
    return toReturn;
  }
  


  /*----------------------------------------------------------------------------------------*/

  /**
   * Preempt all jobs associated with the given working version.
   * 
   * @param req 
   *   The request.
   *    
   * @return 
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable preempt the jobs. 
   */ 
  public Object
  preemptNodeJobs
  (
   QueueNodeJobsReq req
  )
  {
    TaskTimer timer = new TaskTimer("QueueMgr.preemptNodeJobs()");

    try {
      NodeID nodeID = req.getNodeID();

      if(!pAdminPrivileges.isQueueManaged(req, nodeID.getAuthor()))
	throw new PipelineException
	  ("Only a user with Queue Admin privileges may preempt jobs owned by another user!");

      /* lookup the jobs which create the node's primary files */ 
      TreeSet<Long> jobIDs = new TreeSet<Long>();
      {
        timer.aquire();
        synchronized(pNodeJobIDs) {
          timer.resume();
          
          TreeMap<File,Long> table = pNodeJobIDs.get(nodeID);
          if(table != null)
            jobIDs.addAll(table.values());
        }
      }

      /* see which ones are currently running or just about to run */ 
      TreeSet<Long> running = new TreeSet<Long>();
      {
        timer.aquire();  
        synchronized(pJobInfo) {
          timer.resume();
          
          for(Long jobID : jobIDs) {
            QueueJobInfo info = pJobInfo.get(jobID);	   
            if(info != null) {
              switch(info.getState()) {
              case Queued:
              case Paused:
              case Running:
                running.add(jobID);
              }
            }
          }
        }
      }

      /* mark them for preemption */ 
      timer.aquire();
      synchronized(pJobs) {
	timer.resume();
      
	for(Long jobID : running) {
	  QueueJob job = pJobs.get(jobID);
	  if(job != null) 
            pPreemptList.add(jobID);
	}
      }

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }     
  }

  /**
   * Kill all jobs associated with the given working version.
   * 
   * @param req 
   *   The request.
   *    
   * @return 
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable kill the jobs. 
   */ 
  public Object
  killNodeJobs
  (
   QueueNodeJobsReq req
  )
  {
    TaskTimer timer = new TaskTimer("QueueMgr.killNodeJobs()");

    try {
      NodeID nodeID = req.getNodeID();

      if(!pAdminPrivileges.isQueueManaged(req, nodeID.getAuthor()))
        throw new PipelineException
         ("Only a user with Queue Admin privileges may kill jobs owned by another user!");
         
      /* lookup the jobs which create the node's primary files */ 
      TreeSet<Long> jobIDs = new TreeSet<Long>();
      {
        timer.aquire();
        synchronized(pNodeJobIDs) {
          timer.resume();
          
          TreeMap<File,Long> table = pNodeJobIDs.get(nodeID);
          if(table != null)
            jobIDs.addAll(table.values());
        }
      }

      /* see which ones are still unfinished */ 
      TreeSet<Long> unfinished = new TreeSet<Long>();
      {
        timer.aquire();  
        synchronized(pJobInfo) {
          timer.resume();
          
          for(Long jobID : jobIDs) {
            QueueJobInfo info = pJobInfo.get(jobID);	   
            if(info != null) {
              switch(info.getState()) {
              case Queued:
              case Preempted:
              case Paused:
              case Running:
                unfinished.add(jobID);
              }
            }
          }
        }
      }

      /* mark them for death */ 
      timer.aquire();
      synchronized(pJobs) {
	timer.resume();
      
	for(Long jobID : unfinished) {
	  QueueJob job = pJobs.get(jobID);
	  if(job != null) 
            pHitList.add(jobID);
	}
      }

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }     
  }

  /**
   * Pause all jobs associated with the given working version.<P> 
   * 
   * @param req 
   *   The request.
   *    
   * @return 
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable pause the jobs. 
   */ 
  public Object
  pauseNodeJobs
  (
   QueueNodeJobsReq req
  )
  {
    TaskTimer timer = new TaskTimer("QueueMgr.pauseNodeJobs()");

    try {
      NodeID nodeID = req.getNodeID();

      if(!pAdminPrivileges.isQueueManaged(req, nodeID.getAuthor()))
	throw new PipelineException
	  ("Only a user with Queue Admin privileges may pause jobs owned by another user!");

      /* lookup the jobs which create the node's primary files */ 
      TreeSet<Long> jobIDs = new TreeSet<Long>();
      {
        timer.aquire();
        synchronized(pNodeJobIDs) {
          timer.resume();
          
          TreeMap<File,Long> table = pNodeJobIDs.get(nodeID);
          if(table != null)
            jobIDs.addAll(table.values());
        }
      }

      /* mark them to be paused */ 
      timer.aquire();
      synchronized(pJobs) {
	timer.resume();
      
	for(Long jobID : jobIDs) {
	  QueueJob job = pJobs.get(jobID);
	  if(job != null) {
            QueueJobInfo info = null;
            timer.aquire();
            synchronized(pJobInfo) {
              timer.resume();
              info = pJobInfo.get(jobID);	 
            }

            if(info != null) {
              switch(info.getState()) {
              case Queued:
              case Preempted:
                synchronized(pPause) {
                  pPause.add(jobID);
                }
              }

              synchronized(pResume) {
                pResume.remove(jobID);
              }
            }
          }
	}
      }

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }     
  }
  
  /**
   * Resume execution of all paused jobs associated with the given working version.<P> 
   * 
   * @param req 
   *   The request.
   *    
   * @return 
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable resume the jobs. 
   */ 
  public Object
  resumeNodeJobs
  (
   QueueNodeJobsReq req
  )
  {
    TaskTimer timer = new TaskTimer("QueueMgr.resumeNodeJobs()");

    try {
      NodeID nodeID = req.getNodeID();

      if(!pAdminPrivileges.isQueueManaged(req, nodeID.getAuthor()))
	throw new PipelineException
	  ("Only a user with Queue Admin privileges may resume jobs owned by another user!");
      
      /* lookup the jobs which create the node's primary files */ 
      TreeSet<Long> jobIDs = new TreeSet<Long>();
      {
        timer.aquire();
        synchronized(pNodeJobIDs) {
          timer.resume();
          
          TreeMap<File,Long> table = pNodeJobIDs.get(nodeID);
          if(table != null)
            jobIDs.addAll(table.values());
        }
      }

      /* mark them for resumption */ 
      timer.aquire();
      synchronized(pJobs) {
	timer.resume();
      
	for(Long jobID : jobIDs) {
	  QueueJob job = pJobs.get(jobID);
	  if(job != null) {
            QueueJobInfo info = null;
            timer.aquire();
            synchronized(pJobInfo) {
              timer.resume();
              info = pJobInfo.get(jobID);	 
            }

            if(info != null) {
              switch(info.getState()) {
              case Paused:
                synchronized(pResume) {
                  pResume.add(jobID);
                }
              }

              synchronized(pPause) {
                pPause.remove(jobID);
              }
            }
          }
        }
      }

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }     
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the job group with the given ID.
   * 
   * @param req 
   *   The job group request.
   *    
   * @return 
   *   <CODE>QueueGetJobGroupRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the job group.
   */ 
  public Object
  getJobGroup
  (
   QueueGetJobGroupReq req
  )
  {
    TaskTimer timer = new TaskTimer();
    timer.aquire();
    synchronized(pJobGroups) {
      timer.resume();
      try {
	Long groupID = req.getGroupID();
	QueueJobGroup group = pJobGroups.get(groupID);
	if(group == null) 
	  throw new PipelineException
	    ("No job group (" + groupID + ") exists!");

	return new QueueGetJobGroupRsp(timer, group);
      }
      catch(PipelineException ex) {
	return new FailureRsp(timer, ex.getMessage());	  
      }   
    }
  }

  /**
   * Get the job groups which match the following working area pattern.
   * 
   * @param req 
   *   The job groups request.
   *    
   * @return 
   *   <CODE>QueueGetJobGroupsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the job groups.
   */ 
  public Object
  getJobGroups
  (
   QueueGetJobGroupsReq req
  )
  {
    TaskTimer timer = new TaskTimer();

    String author = req.getAuthor();
    String view   = req.getView();

    timer.aquire();
    synchronized(pJobGroups) {
      timer.resume();
      TreeMap<Long,QueueJobGroup> groups = new TreeMap<Long,QueueJobGroup>();
      for(Long groupID : pJobGroups.keySet()) {
	QueueJobGroup group = pJobGroups.get(groupID);
        if(group != null) {
          if((author == null) ||
             (author.equals(group.getNodeID().getAuthor()) &&
              ((view == null) || 
               view.equals(group.getNodeID().getView())))) {
            groups.put(groupID, group);
          }
        }
      }
      
      return new QueueGetJobGroupsRsp(timer, groups);
    }
  }

  /**
   * Delete the completed job groups. <P> 
   * 
   * @param req 
   *   The delete group request.
   *    
   * @return 
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to delete the job groups.
   */ 
  public Object
  deleteJobGroups
  (
   QueueDeleteJobGroupsReq req
  )
  {
    TaskTimer timer = new TaskTimer("QueueMgr.deleteJobGroups()");
    try {
      TreeMap<Long,String> groupAuthors = req.getGroupAuthors();
      
      TreeSet<String> authors = new TreeSet<String>(groupAuthors.values());
      for(String author : authors) {
	if(!pAdminPrivileges.isQueueManaged(req, author))
	  throw new PipelineException
	    ("Only a user with Queue Manager privileges may delete job groups owned " + 
	     "by another user!");
      }

      timer.aquire();
      synchronized(pJobGroups) {
	timer.resume();

	for(Long groupID : groupAuthors.keySet()) {
	  QueueJobGroup group = pJobGroups.get(groupID);
	  if(group == null) 
	    throw new PipelineException
	      ("No job group (" + groupID + ") exists!");

	  String author = groupAuthors.get(groupID);
	  if(!group.getNodeID().getAuthor().equals(author)) 
	    throw new PipelineException
	      ("The author (" + group.getNodeID().getAuthor() + ") of group " + 
	       "(" + groupID + ") did not match the specified author (" + author + ")!");

	  deleteCompletedJobGroup(timer, group);
	}

	return new SuccessRsp(timer);
      }
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }   
  }

  /**
   * Delete all of the completed job groups created in the given working area. <P> 
   * 
   * @param req 
   *   The delete group request.
   * 
   * @return 
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to delete the job groups.
   */ 
  public Object
  deleteViewJobGroups
  (
   QueueDeleteViewJobGroupsReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer("QueueMgr.deleteViewJobGroups()");

    try {
      if(!pAdminPrivileges.isQueueManaged(req, req.getAuthor()))
	throw new PipelineException
	  ("Only a user with Queue Manager privileges may delete job groups owned " + 
	   "by another user!");

      timer.aquire();
      synchronized(pJobGroups) {
	timer.resume();
	
	ArrayList<QueueJobGroup> dead = new ArrayList<QueueJobGroup>();
	{
	  String author = req.getAuthor();
	  String view   = req.getView();
	  for(Long groupID : pJobGroups.keySet()) {
	    QueueJobGroup group = pJobGroups.get(groupID);
	    NodeID nodeID = group.getNodeID();
	    if(nodeID.getAuthor().equals(author) && nodeID.getView().equals(view)) 
	      dead.add(group);
	  }
	}
	
	for(QueueJobGroup group : dead) {
	  try {
	    deleteCompletedJobGroup(timer, group);
	  }
	  catch(PipelineException ex) {
	  }
	}
      }

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }   
  }

  /**
   * Delete all of the completed job groups in all working areas. <P> 
   * 
   * @param req 
   *   The request.
   * 
   * @return 
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to delete the job groups.
   */ 
  public Object
  deleteAllJobGroups
  (
   PrivilegedReq req
  ) 
  {
    TaskTimer timer = new TaskTimer("QueueMgr.deleteAllJobGroups()");

    timer.aquire();
    synchronized(pJobGroups) {
      timer.resume();
      
      ArrayList<QueueJobGroup> dead = new ArrayList<QueueJobGroup>();
      for(Long groupID : pJobGroups.keySet()) 
	dead.add(pJobGroups.get(groupID));
	
      for(QueueJobGroup group : dead) {   
	if(pAdminPrivileges.isQueueManaged(req, group.getNodeID())) {
	  try {
	    deleteCompletedJobGroup(timer, group);
	  }
	  catch(PipelineException ex) {
	  }
	}
      }
    }
	
    return new SuccessRsp(timer);
  }

  /**
   * Delete the completed job group with the given ID.
   * 
   * @throws PipelineExceptio
   *   If the job group is not completed.
   */ 
  private void 
  deleteCompletedJobGroup
  (    
   TaskTimer timer,
   QueueJobGroup group
  ) 
    throws PipelineException 
  {
    Long groupID = group.getGroupID();

    timer.aquire();  
    synchronized(pJobInfo) {
      timer.resume();
      for(Long jobID : group.getAllJobIDs()) {
	QueueJobInfo info = pJobInfo.get(jobID);
	if(info != null) {
	  switch(info.getState()) {
	  case Queued:
	  case Preempted:
	  case Paused:
	  case Running:
          case Limbo:
	    throw new PipelineException
	      ("Cannot delete job group (" + groupID + ") until all of its jobs " + 
	       "have completed!");
	  }
	}
      }
    }
    
    deleteJobGroupFile(groupID);

    timer.aquire();
    synchronized(pJobGroups) {
      timer.resume();
      
      pJobGroups.remove(groupID);
      pJobCounters.removeCounters(timer, group);
    }

    /* post-delete group task */ 
    startExtensionTasks(timer, new DeleteJobGroupExtFactory(group));
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O L L E C T O R                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Perform one round of collecting per-host system resource usage samples. 
   */ 
  public void 
  collector()
  {
    TaskTimer timer = new TaskTimer("Collector");

    /* get the names of the currently enabled/disabled hosts */
    TreeSet<String> needsCollect = new TreeSet<String>();
    TreeSet<String> needsTotals = new TreeSet<String>();
    {
      timer.suspend();
      TaskTimer tm = new TaskTimer("Collector [Find Enabled]");
      tm.aquire();
      synchronized(pHostsInfo) {
        tm.resume();
	for(String hname : pHostsInfo.keySet()) {
	  QueueHostInfo qinfo = pHostsInfo.get(hname);
	  switch(qinfo.getStatus()) {
	  case Enabled:	 
	    needsCollect.add(hname);
	    if((qinfo.getNumProcessors() == null) || 
	       (qinfo.getTotalMemory() == null) ||
	       (qinfo.getTotalDisk() == null)) 
	      needsTotals.add(hname);
	  }	  
	}
      }
      LogMgr.getInstance().logSubStage
	(LogMgr.Kind.Col, LogMgr.Level.Finer,
	 tm, timer);
    }

    /* collect system resource usage samples and other stats from the hosts */ 
    {
      ArrayList<SubCollectorTask> cthreads = new ArrayList<SubCollectorTask>();
      {
	timer.suspend();
	TaskTimer tm = new TaskTimer("Collector [Network]");

	/* spawn collection threads */ 
	if(!needsCollect.isEmpty()) {
	  SubCollectorTask thread = null;
	  int wk = 0;
	  int id = 0;
	  for(String hname : needsCollect) {
	    if((wk % pCollectorBatchSize.get()) == 0) {
	      if(thread != null) {
		cthreads.add(thread);
		thread.start();
	      }
	      
	      thread = new SubCollectorTask(id);
	      id++;
	    }
	    
	    thread.addCollect(hname);
	    if(needsTotals.contains(hname)) 
	      thread.addTotals(hname);
	    
	    wk++;
	  }
	
	  cthreads.add(thread);
	  thread.start();
	}
	
	/* wait for all to finish */ 
	for(SubCollectorTask thread : cthreads) {
	  try {
	    thread.join();
	  }
	  catch(InterruptedException ex) {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Col, LogMgr.Level.Severe,
	       "Interrupted while collecting resource information.");
	    LogMgr.getInstance().flush();
	  }
	}
	LogMgr.getInstance().logSubStage
	  (LogMgr.Kind.Col, LogMgr.Level.Finer,
	   tm, timer); 
      }
      
      /* collect the data from all threads and to update pending change tables */ 
      {
	timer.suspend();
	TaskTimer tm = new TaskTimer("Collector [Pending Changes]");
	for(SubCollectorTask thread : cthreads) {
	  {
	    boolean hasSamples = false;

	    /* cache the latest resource samples */ 
	    TreeMap<String,ResourceSample> samples = thread.getSamples();
	    if(!samples.isEmpty()) {
	      tm.aquire();
	      synchronized(pSamples) {
		tm.resume();

		for(String hname : samples.keySet()) {
		  ResourceSample sample = samples.get(hname);
		  if(sample != null) {
		    ResourceSampleCache cache = pSamples.get(hname);
		    if(cache == null) {
		      cache = new ResourceSampleCache(sCollectedSamples);
		      pSamples.put(hname, cache);
		    }
		
		    cache.addSample(sample);
		    hasSamples = true;
		  }
		}
	      }
	    }

	    /* initialize the sample output stamp */ 
	    if(hasSamples && (pLastSampleWritten.get() == 0L)) {
	      long oldest = Long.MAX_VALUE;
	      for(String hname : samples.keySet()) {
		ResourceSample sample = samples.get(hname);
		if(sample != null) 
		  oldest = Math.min(oldest, sample.getTimeStamp());
	      }
	      pLastSampleWritten.set(oldest); 
	    }
	  }

	  {
	    TreeMap<String,OsType> osTypes = thread.getOsTypes();
	    if(!osTypes.isEmpty()) {
	      tm.aquire();
	      synchronized(pOsTypeChanges) {
		tm.resume();
		
		pOsTypeChanges.putAll(osTypes);
	      }
	    }
	  }

	  {
	    TreeMap<String,Integer> numProcs = thread.getNumProcs();
	    if(!numProcs.isEmpty()) {
	      tm.aquire();
	      synchronized(pNumProcChanges) {
		tm.resume();
		
		pNumProcChanges.putAll(numProcs);
	      }
	    }
	  }

	  {
	    TreeMap<String,Long> totalMemory = thread.getTotalMemory();
	    if(!totalMemory.isEmpty()) {
	      tm.aquire();
	      synchronized(pTotalMemoryChanges) {
		tm.resume();
		
		pTotalMemoryChanges.putAll(totalMemory);
	      }
	    }
	  }

	  {
	    TreeMap<String,Long> totalDisk = thread.getTotalDisk();
	    if(!totalDisk.isEmpty()) {
	      tm.aquire();
	      synchronized(pTotalDiskChanges) {
		tm.resume();
		
		pTotalDiskChanges.putAll(totalDisk);
	      }
	    }
	  }
	}
	LogMgr.getInstance().logSubStage
	  (LogMgr.Kind.Col, LogMgr.Level.Finer,
	   tm, timer); 
      }
    }

    /* when enough samples have been collected, write them disk... */ 
    if(pLastSampleWritten.get() > 0L) { 
      long now = System.currentTimeMillis();
      long sinceLastWrite = now - pLastSampleWritten.get();

      if(sinceLastWrite > (PackageInfo.sCollectorInterval * sCollectedSamples)) {
	timer.suspend();
	TaskTimer tm = new TaskTimer("Collector [Write Samples]");  
	try {
	  writeSamples(tm, false); 
	}
	catch(PipelineException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Col, LogMgr.Level.Severe,
	     ex.getMessage());
	}
	finally {
	  pLastSampleWritten.set(now); 
	}
	LogMgr.getInstance().logSubStage
	  (LogMgr.Kind.Col, LogMgr.Level.Finer,
	   tm, timer); 
      }

      /* cleanup any out-of-date sample files */ 
      {
	timer.suspend();
	TaskTimer tm = new TaskTimer("Collector [Clean Samples]");
	{
	  cleanupSamples(tm);
	}
	LogMgr.getInstance().logSubStage
	  (LogMgr.Kind.Col, LogMgr.Level.Finer,
	   tm, timer); 
      }
    }

    /* if we're ahead of schedule, take a nap */ 
    {
      LogMgr.getInstance().logStage
	(LogMgr.Kind.Col, LogMgr.Level.Fine,
	 timer); 

      long nap = PackageInfo.sCollectorInterval - timer.getTotalDuration();
      if(nap > 0) {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Col, LogMgr.Level.Finer,
	   "Collector: Sleeping for (" + nap + ") msec...");

	try {
	  Thread.sleep(nap);
	}
	catch(InterruptedException ex) {
	}
      }
      else {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Col, LogMgr.Level.Finer,
	   "Collector: Overbudget by (" + (-nap) + ") msec...");
      }

      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Col, LogMgr.Level.Finer,
         "\n-----------------------------------------------------------------------------\n");
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   D I S P A T C H E R                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Perform one round of assigning jobs to available hosts. 
   */ 
  public void 
  dispatcher()
  {
    TaskTimer timer = new TaskTimer("Dispatcher");

    /* apply any pending modifications to the job servers prior to dispatch */ 
    dspApplyHostEdits(timer);

    /* kill/abort the jobs in the hit list */ 
    dspKillAbort(timer); 
    
    /* kill and requeue running jobs on the preempt list */ 
    dspPreempt(timer); 
    
    /* the IDs of jobs which need to have their JobProfile recomputed */ 
    TreeSet<Long> changedIDs = new TreeSet<Long>();
    
    /* apply the pending changes to the job requirements for jobs */
    dspChangeJobReqs(timer, changedIDs); 

    /* he names of the toolsets used by jobs which are ready to run */ 
    TreeSet<String> readyToolsets = new TreeSet<String>();

    /* process the waiting jobs: sorting jobs into killed/aborted, ready and waiting */ 
    dspSortWaiting(timer, readyToolsets, changedIDs); 

    /* retrieve any toolsets required by newly ready jobs which are not already cached */
    dspToolsets(timer, readyToolsets); 

    /* update the read-only cache of job server info before acquiring any potentially
       long duration locks on the pHosts table */ 
    dspUpdateHostsInfo(timer); 

    /* process the available job server slots in dispatch order */
    dspDispatchTotal(timer, changedIDs); 

    /* check for newly completed job groups */ 
    dspUpdateJobGroups(timer); 

    /* filter any jobs not ready for execution from the ready list */
    dspFilterUnready(timer); 

    /* perform garbage collection of jobs at regular intervals */ 
    pDispatcherCycles++;
    if(pDispatcherCycles > sGarbageCollectAfter) {
      timer.suspend();
      TaskTimer tm = new TaskTimer("Dispatcher [Garbage Collect]");
      {
	garbageCollectJobs(tm);
      }
      LogMgr.getInstance().logSubStage
	(LogMgr.Kind.Dsp, LogMgr.Level.Finer, 
	 tm, timer);

      pDispatcherCycles = 0;
    }

    /* if we're ahead of schedule, take a nap */ 
    {
      LogMgr.getInstance().logStage
	(LogMgr.Kind.Dsp, LogMgr.Level.Fine,
	 timer); 

      long nap = pDispatcherInterval.get() - timer.getTotalDuration();
      if(nap > 0) {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Dsp, LogMgr.Level.Finer,
	   "Dispatcher: Sleeping for (" + nap + ") msec...");
	try {
	  Thread.sleep(nap);
	}
	catch(InterruptedException ex) {
	}
      }
      else {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Dsp, LogMgr.Level.Finer,
	   "Dispatcher: Overbudget by (" + (-nap) + ") msec...");
      }

      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Dsp, LogMgr.Level.Finer,
         "\n-----------------------------------------------------------------------------\n");
    }
  }

  /**
   * Apply any pending modifications to the job servers prior to dispatch.<P> 
   * 
   * This should only be called from the dispatcher() method!
   *
   * @param timer
   *   The overall dispatcher timer.
   */ 
  private void 
  dspApplyHostEdits
  (
   TaskTimer timer
  ) 
  {
    timer.suspend();
    TaskTimer tm = new TaskTimer("Dispatcher [Apply Host Edits]");
    try {
      applyHostEdits(tm);
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().log
        (LogMgr.Kind.Dsp, LogMgr.Level.Severe,
         ex.getMessage()); 
    }
    LogMgr.getInstance().logSubStage
      (LogMgr.Kind.Dsp, LogMgr.Level.Finer, 
       tm, timer);
  }

  /**
   * Kill/abort the jobs in the hit list.<P> 
   * 
   * This should only be called from the dispatcher() method!
   *
   * @param timer
   *   The overall dispatcher timer.
   */ 
  private void 
  dspKillAbort 
  (
   TaskTimer timer
  ) 
  {
    timer.suspend();
    TaskTimer tm = new TaskTimer("Dispatcher [Kill/Abort]");
    while(true) {
      Long jobID = pHitList.poll();
      if(jobID == null) 
        break;
      
      QueueJob job = null;
      tm.aquire();
      synchronized(pJobs) {
        tm.resume();
        job = pJobs.get(jobID);
      }
      
      QueueJobInfo info = getJobInfo(tm, jobID);       
      if(info != null) {
        boolean aborted = false;
        switch(info.getState()) {
        case Queued:
        case Preempted:
        case Paused:
          {
            JobState prevState = info.aborted();
            pJobCounters.update(tm, prevState, info);
            try {
              writeJobInfo(info);
            }
            catch(PipelineException ex) {
              LogMgr.getInstance().log
                (LogMgr.Kind.Dsp, LogMgr.Level.Severe,
                 ex.getMessage()); 
            }
            aborted = true;
          }
          break; 
	  
        case Running:
          {
            KillTask task = new KillTask(info.getHostname(), jobID);
            task.start();
            
            aborted = true;
          }
        }
        
        /* post-abort tasks */ 
        if(aborted) 
          startExtensionTasks(tm, new JobAbortedExtFactory(job));
      }
    }
    LogMgr.getInstance().logSubStage
      (LogMgr.Kind.Dsp, LogMgr.Level.Finer, 
       tm, timer);
  }

  /**
   * Kill and requeue running jobs on the preempt list.<P> 
   * 
   * This should only be called from the dispatcher() method!
   *
   * @param timer
   *   The overall dispatcher timer.
   */ 
  private void 
  dspPreempt
  (
   TaskTimer timer
  ) 
  {
    timer.suspend();
    TaskTimer tm = new TaskTimer("Dispatcher [Preempt]");
    while(true) {
      Long jobID = pPreemptList.poll();
      if(jobID == null) 
        break;
      
      QueueJob job = null;
      tm.aquire();
      synchronized(pJobs) {
        tm.resume();
        job = pJobs.get(jobID);
      }
      
      QueueJobInfo info = getJobInfo(tm, jobID);       
      if(info != null) {
        switch(info.getState()) {
        case Running:
          {
            QueueJobInfo preemptedInfo = new QueueJobInfo(info);
            
            {
              String hostname = info.getHostname();
              
              JobState prevState = info.preempted();
              pJobCounters.update(tm, prevState, info);
              try {
                writeJobInfo(info);
              }
              catch(PipelineException ex) {
                LogMgr.getInstance().log
		  (LogMgr.Kind.Dsp, LogMgr.Level.Severe,
		   ex.getMessage()); 
              }
              
              KillTask task = new KillTask(hostname, jobID);
              task.start();
            }

            /* post-preempt tasks */ 
            startExtensionTasks(tm, new JobPreemptedExtFactory(job, preemptedInfo));
          }
        }
      }
    }
    LogMgr.getInstance().logSubStage
      (LogMgr.Kind.Dsp, LogMgr.Level.Finer, 
       tm, timer);
  }

  /**
   * Apply the pending changes to the job requirements for jobs.<P> 
   * 
   * This should only be called from the dispatcher() method!
   * 
   * @param timer
   *   The overall dispatcher timer.
   * 
   * @param changedIDs
   *   The IDs of jobs which need to have their JobProfile recomputed.
   */ 
  private void
  dspChangeJobReqs
  (
   TaskTimer timer, 
   TreeSet<Long> changedIDs
  ) 
  {
    timer.suspend();
    TaskTimer tm = new TaskTimer("Dispatcher [Change Job Requirements]");
    
    while (true) {
      long jobID;
      JobReqs reqs;
      
      tm.aquire();
      synchronized (pJobReqsChanges) {
        tm.resume();
        if (pJobReqsChanges.isEmpty())
          break;
        jobID = pJobReqsChanges.firstKey();
        reqs = pJobReqsChanges.remove(jobID);
      }
      
      QueueJobInfo info = getJobInfo(tm, jobID);       
      if(info != null) {
        switch(info.getState()) {
        case Paused:
        case Preempted:
        case Queued:
          tm.aquire();
          synchronized(pJobs) {
            tm.resume();
            QueueJob job = pJobs.get(jobID);
            if(job != null) {
              job.setJobRequirements(reqs);
              changedIDs.add(jobID);
            }
          }
          break;
        }
      }
    }
    LogMgr.getInstance().logSubStage
      (LogMgr.Kind.Dsp, LogMgr.Level.Finer, 
       tm, timer);
  }

  /** 
   * Process the waiting jobs: sorting jobs into killed/aborted, ready and waiting.<P> 
   * 
   * This should only be called from the dispatcher() method!
   *
   * @param timer
   *   The overall dispatcher timer.
   * 
   * @param readyToolets
   *   The names of the toolsets used by jobs which are ready to run.
   * 
   * @param changedIDs
   *   The IDs of jobs which need to have their JobProfile recomputed.
   */ 
  private void 
  dspSortWaiting
  (
   TaskTimer timer, 
   TreeSet<String> readyToolsets,
   TreeSet<Long> changedIDs
  ) 
  {
    timer.suspend();
    TaskTimer tm = new TaskTimer("Dispatcher [Sort Waiting]");

    /* process the waiting jobs */ 
    LinkedList<Long> stillWaiting = new LinkedList<Long>();
    while(true) {
      Long jobID = pWaiting.poll();
      if(jobID == null) 
        break;
	
      QueueJobInfo info = getJobInfo(tm, jobID); 	
      if(info != null) {
        /* resume jobs marked to be resumed */ 
        switch(info.getState()) {
        case Paused:
          {
            boolean resumeJob = false;
            synchronized(pResume) {
              resumeJob = pResume.remove(jobID); 
            }

            if(resumeJob) {
              JobState prevState = info.resumed();
              pJobCounters.update(tm, prevState, info);
              try {
                writeJobInfo(info);
              }
              catch(PipelineException ex) {
                LogMgr.getInstance().log
                  (LogMgr.Kind.Dsp, LogMgr.Level.Severe,
                   ex.getMessage()); 
              }
            }
          }
        }

        /* pause jobs marked to be paused */ 
        switch(info.getState()) {
        case Queued:
        case Preempted:
          {
            boolean pauseJob = false;
            synchronized(pPause) {
              pauseJob = pPause.remove(jobID); 
            }

            if(pauseJob) {
              JobState prevState = info.paused();
              pJobCounters.update(tm, prevState, info);
              try {
                writeJobInfo(info);
              }
              catch(PipelineException ex) {
                LogMgr.getInstance().log
                  (LogMgr.Kind.Dsp, LogMgr.Level.Severe,
                   ex.getMessage()); 
              }
            }
          }
        }

        /* sort into ready, waiting and done */ 
        switch(info.getState()) {
        case Queued:	     
        case Preempted:
          {
            QueueJob job = null;
            tm.aquire();
            synchronized(pJobs) {
              tm.resume();
              job = pJobs.get(jobID);
            }

            if(job == null) {
              LogMgr.getInstance().logAndFlush
                (LogMgr.Kind.Dsp, LogMgr.Level.Warning,
                 "While processing the job (" + jobID + ") in the waiting list, job entry " + 
                 "could be found!  Removing it from the waiting list."); 
            }
            else {           
              /* determine whether the job is ready for execution */ 
              boolean waitingOnUpstream = false; 
              boolean abortDueToUpstream = false;
              for(Long sjobID : job.getSourceJobIDs()) {
                QueueJobInfo sinfo = getJobInfo(tm, sjobID); 		  
                if(sinfo == null) {
                  LogMgr.getInstance().logAndFlush
                    (LogMgr.Kind.Dsp, LogMgr.Level.Warning,
                     "While processing the job (" + sjobID + ") dependeny of job " + 
                     "(" + jobID + "), no job status information could be found!  " + 
                     "Aborting the job.");
                  pHitList.add(sjobID);
                  abortDueToUpstream = true;
                }
                else {
                  switch(sinfo.getState()) {	   
                  case Queued:
                  case Preempted:
                  case Paused:
                  case Running:
                  case Limbo:
                    waitingOnUpstream = true;
                    break;

                  case Aborted:
                  case Failed:
                    abortDueToUpstream = true;
                  }
                }
              }
		
              if(abortDueToUpstream) {
                /* abort this job, because its dependencies have been aborted or failed */ 
                pHitList.add(jobID);
              }
              else if(waitingOnUpstream) {
                /* keep the job on the waiting list */ 
                stillWaiting.add(jobID);
              }
              else {
                /* add it to the ready list, re-profile it and 
                   insure that its toolset has already be cached */ 
                pReady.put(jobID, null);
                changedIDs.add(jobID);
                readyToolsets.add(job.getActionAgenda().getToolset());
              }
            }
          }
          break;

        case Paused:
          /* keep paused jobs on the waiting list */ 
          stillWaiting.add(jobID);
          break;

        case Aborted:
        case Running:
        case Limbo:
        case Finished:
        case Failed:
          /* jobs in these states should not be still waiting to run... */ 
        }
      }
    }
    pWaiting.addAll(stillWaiting);      

    LogMgr.getInstance().logSubStage
      (LogMgr.Kind.Dsp, LogMgr.Level.Finer, 
       tm, timer);
  }

  /**
   * Retrieve any toolsets required by newly ready jobs which are not already cached.<P> 
   * 
   * This should only be called from the dispatcher() method!
   *
   * @param timer
   *   The overall dispatcher timer.
   * 
   * @param readyToolets
   *   The names of the toolsets used by jobs which are ready to run.
   */ 
  private void 
  dspToolsets
  (
   TaskTimer timer, 
   TreeSet<String> readyToolsets
  ) 
  {
    timer.suspend();
    TaskTimer tm = new TaskTimer("Dispatcher [Toolsets]");
    {
      fetchToolsets(readyToolsets, tm);
    }
    LogMgr.getInstance().logSubStage
      (LogMgr.Kind.Dsp, LogMgr.Level.Finer, 
       tm, timer);
  }

  /**
   * Update the read-only cache of job server info before acquiring any potentially
   * long duration locks on the pHosts table.<P> 
   * 
   * This should only be called from the dispatcher() method!
   *
   * @param timer
   *   The overall dispatcher timer.
   */ 
  private void 
  dspUpdateHostsInfo
  (
   TaskTimer timer
  ) 
  {
    timer.suspend();
    TaskTimer tm = new TaskTimer("Dispatcher [Update Hosts Info]");
    {
      updateHostsInfo(tm);
    }
    LogMgr.getInstance().logSubStage
      (LogMgr.Kind.Dsp, LogMgr.Level.Finer, 
       tm, timer);
  }

  /**
   * Process the available job server slots in dispatch order.<P> 
   * 
   * This should only be called from the dispatcher() method!
   *
   * @param timer
   *   The overall dispatcher timer.
   *
   * @param changedIDs
   *   The IDs of jobs which need to have their JobProfile recomputed.
   */ 
  private void 
  dspDispatchTotal
  (
   TaskTimer timer,
   TreeSet<Long> changedIDs   
  ) 
  {
    timer.suspend();
    TaskTimer dtm = new TaskTimer("Dispatcher [Dispatch Total]");
      
    /* if there are changes to the seletion/hardware keys, we need to make new profiles */
    boolean reprofileAllJobs = dsptValidateProfiles(dtm);

    /* create any selection, hardware or job profiles needed before ranking jobs */ 
    dsptReprofileReadyJobs(dtm, reprofileAllJobs, changedIDs); 

    /* resize rank array (if necessary) */ 
    int readyCnt = dsptResizeJobRanks(dtm); 

    int slotCnt = 0; 
    if(readyCnt > 0) {

      int enabledCnt = 0;
      dtm.aquire();
      synchronized(pHosts) {
        dtm.resume();

        /* set the count of running jobs based on the number of MonitorTasks registered */ 
        synchronized(pRunning) {
          for(Map.Entry<String,QueueHost> entry : pHosts.entrySet()) {
            String hostname = entry.getKey(); 
            QueueHost host = entry.getValue();

            int numRunning = 0;
            {
              Set<Long> jobIDs = pRunning.keySet(hostname); 
              if(jobIDs != null) 
                numRunning = jobIDs.size();
            }

            host.setRunningJobs(numRunning);
          }
        }
	
        /* resize ordered hosts array (if necessary) */ 
        dsptResizeOrderedHosts(dtm); 
        
        /* sort the enabled host based on their Order property */ 
        enabledCnt = dsptOrderHosts(dtm); 
      }

      /* process the hosts */ 
      int hk;
      for(hk=0; hk<enabledCnt; hk++) {
        QueueHost host = pOrderedHosts[hk];
        String hostname = host.getName(); 
        int slots = host.getAvailableSlots();

        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Dsp, LogMgr.Level.Finest,
           "Initial Slots [" + hostname + "]: Free = " + slots + "\n");
        
        dtm.suspend();
        TaskTimer stm = new TaskTimer
          ("Dispatcher [Dispatch Host - " + hostname + "]");
        
        /* dispatch one slot per-host each dispatcher cycle, if available... */ 
        if(slots > 0) {

          /* fill the pJobRanks array with entries for all jobs which qualify */
          int jobCnt = dsptQualifyJobs(stm, host); 

          /* rank the jobs by sorting the portion of the rank array just populated */ 
          dsptRankJobs(stm, hostname, jobCnt); 
          
          /* attempt to dispatch a job to the slot:
             in order of selection score, favor pending/engaged, job priority and age */ 
          dsptAssignJob(stm, host, jobCnt); 
          slotCnt++;

          LogMgr.getInstance().logAndFlush
            (LogMgr.Kind.Dsp, LogMgr.Level.Finest,
             "Updated Slots [" + hostname + "]:  Free = " + slots + "\n");
        } 

        LogMgr.getInstance().logSubStage
          (LogMgr.Kind.Dsp, LogMgr.Level.Finer, 
           stm, dtm); 
      }
    }
    LogMgr.getInstance().logSubStage
      (LogMgr.Kind.Dsp, LogMgr.Level.Finer, 
       dtm, timer);

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Dsp, LogMgr.Level.Finer,
       "Processed (" + readyCnt + ") jobs while dispatching (" + slotCnt + ") slots in " +
       "(" + dtm.getTotalDuration() + ") msec.");
  }

  /**
   * Determine if there are changes to the seletion/hardware keys which will require 
   * new profiles.<P> 
   * 
   * This should only be called from the dspDispatchTotal() method!
   * 
   * @param dtm
   *   The core dispatcher timer.
   */ 
  private boolean
  dsptValidateProfiles
  (
   TaskTimer dtm   
  ) 
  {
    boolean reprofileAllJobs = false;

    if(pSelectionChanged.get()) {
      pDispSelectionKeyNames.clear();
      pDispSelectionGroups.clear();
      pSelectionProfiles.clear();
      reprofileAllJobs = true;
      
      dtm.aquire();
      synchronized(pSelectionGroups) {
        synchronized(pSelectionKeys) {
          dtm.resume();
          
          pDispSelectionKeyNames.addAll(pSelectionKeys.keySet()); 
          
          for(SelectionGroup group : pSelectionGroups.values()) 
            pDispSelectionGroups.put(group.getName(), new SelectionGroup(group)); 
          
          pSelectionChanged.set(false);
        }
      }
      
      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Dsp, LogMgr.Level.Finest,
         "Cleared all Selection Profiles.");
    }
    
    if(pHardwareChanged.get()) {
      pDispHardwareKeyNames.clear();
      pDispHardwareGroups.clear();
      pHardwareProfiles.clear();
      reprofileAllJobs = true;
      
      dtm.aquire();
      synchronized(pHardwareGroups) {
        synchronized(pHardwareKeys) {
          dtm.resume();
          
          pDispHardwareKeyNames.addAll(pHardwareKeys.keySet()); 
          
          for(HardwareGroup group : pHardwareGroups.values()) 
            pDispHardwareGroups.put(group.getName(), new HardwareGroup(group)); 
          
          pHardwareChanged.set(false);
        }
      }
      
      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Dsp, LogMgr.Level.Finest,
         "Cleared all Hardware Profiles.");
    }

    return reprofileAllJobs;
  }

  /**
   * Create any selection, hardware or job profiles needed before ranking jobs.<P> 
   * 
   * This should only be called from the dspDispatchTotal() method!
   *
   * @param dtm
   *   The core dispatcher timer.
   * 
   * @param reprofileAllJobs
   *   Whether to ignore changedIDs and just reprofile everything.
   * 
   * @param changedIDs
   *   The IDs of jobs which need to have their JobProfile recomputed.
   */ 
  private void 
  dsptReprofileReadyJobs
  (
   TaskTimer dtm, 
   boolean reprofileAllJobs, 
   TreeSet<Long> changedIDs   
  ) 
  {
    if(!pReady.isEmpty()) {
      dtm.suspend();
      TaskTimer tm = new TaskTimer("Dispatcher [Reprofile Ready Jobs]");

      /* do we need to process only the new and modified jobs or all of them? */ 
      TreeSet<Long> reprofileIDs = new TreeSet<Long>();
      if(reprofileAllJobs) {
        reprofileIDs.addAll(pReady.keySet());
      }
      else {
        for(Long jobID : changedIDs) {
          if(pReady.containsKey(jobID)) 
            reprofileIDs.add(jobID);
        }
      }
        
      int jobCnt = 0;
      int selectionCnt = 0;
      int hardwareCnt = 0;
      for(Long jobID : reprofileIDs) {
        tm.aquire();
        synchronized(pJobs) {
          tm.resume();
          QueueJob job = pJobs.get(jobID);
          if(job != null) {
            JobReqs jreqs = job.getJobRequirements();
            ActionAgenda jagenda = job.getActionAgenda();

            SelectionProfile selectionProfile = null;
            {
              Flags flags = jreqs.getSelectionFlags(pDispSelectionKeyNames); 
              selectionProfile = pSelectionProfiles.get(flags); 
              if(selectionProfile == null) {
                selectionProfile = 
                  new SelectionProfile(pDispSelectionKeyNames, pDispSelectionGroups, jreqs); 
                pSelectionProfiles.put(flags, selectionProfile); 
                selectionCnt++;
              }
            }

            HardwareProfile hardwareProfile = null;
            {
              Flags flags = jreqs.getHardwareFlags(pDispHardwareKeyNames); 
              hardwareProfile = pHardwareProfiles.get(flags); 
              if(hardwareProfile == null) {
                hardwareProfile = 
                  new HardwareProfile(pDispHardwareKeyNames, pDispHardwareGroups, jreqs); 
                pHardwareProfiles.put(flags, hardwareProfile); 
                hardwareCnt++;
              }
            }

            JobProfile profile = null;
            {
              QueueJobInfo info = getJobInfo(tm, jobID); 
              if(info != null) {
                tm.aquire();
                synchronized(pToolsets) {
                  tm.resume();
                  String tname = jagenda.getToolset();
                  TreeMap<OsType,Toolset> supports = pToolsets.get(tname); 
                  if(supports != null) {
                    profile = new JobProfile(job, info, selectionProfile, hardwareProfile, 
                                             supports.keySet());   
                  }
                }
              }
            }
            
            if(profile != null) {
              pReady.put(jobID, profile);  
              jobCnt++;
            }
          }
        }
      }

      LogMgr.getInstance().logSubStage
        (LogMgr.Kind.Dsp, LogMgr.Level.Finer, 
         tm, dtm); 

      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Dsp, LogMgr.Level.Finer,
         "New Profiles: Jobs(" + jobCnt + ") " + 
         "Selection(" + selectionCnt + ") Hardware(" + hardwareCnt + ")"); 
        
      /* print the profile details */ 
      if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Dsp, LogMgr.Level.Finest)) {
        StringBuilder buf = new StringBuilder();
          
        buf.append("\nSelection Keys:\n"); 
        for(String key : pDispSelectionKeyNames) 
          buf.append("  " + key + "\n"); 
          
        buf.append("Selection Profiles:\n"); 
        for(Flags flags : pSelectionProfiles.keySet()) {
          SelectionProfile profile = pSelectionProfiles.get(flags); 
          buf.append("  " + flags + ":\n");
          buf.append("    (null) = " + profile.getScore(null) + "\n"); 
          for(String gname : pDispSelectionGroups.keySet())
            buf.append("    " + gname + " = " + profile.getScore(gname) + "\n"); 
        }
          
        buf.append("Hardware Keys:\n"); 
        for(String key : pDispHardwareKeyNames) 
          buf.append("  " + key + "\n"); 
          
        buf.append("Hardware Profiles:\n"); 
        for(Flags flags : pHardwareProfiles.keySet()) {
          HardwareProfile profile = pHardwareProfiles.get(flags); 
          buf.append("  " + flags + ":\n");
          buf.append("    (null) = " + profile.isEligible(null) + "\n"); 
          for(String gname : pDispHardwareGroups.keySet())
            buf.append("    " + gname + " = " + profile.isEligible(gname) + "\n"); 
        }
          
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Dsp, LogMgr.Level.Finest, buf.toString());
      }
    }
  }

  /**
   * Resize rank array (if necessary).<P> 
   * 
   * This should only be called from the dspDispatchTotal() method!
   *
   * @param dtm
   *   The core dispatcher timer.
   * 
   * @return 
   *   The number of jobs ready to be dispatched.
   */ 
  private int 
  dsptResizeJobRanks
  (
   TaskTimer dtm
  ) 
  {
    int readyCnt = 0;
    if(!pReady.isEmpty()) {
      readyCnt = pReady.size();
      if(pJobRanks.length < readyCnt) {
        dtm.suspend();
        TaskTimer tm = new TaskTimer("Dispatcher [Resize Job Ranks]");
        
        JobRank[] copy = Arrays.copyOf(pJobRanks, (int)(((float) readyCnt) * 1.5));
        pJobRanks = copy;
        
        LogMgr.getInstance().logSubStage
          (LogMgr.Kind.Dsp, LogMgr.Level.Finer, 
           tm, dtm); 
        
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Dsp, LogMgr.Level.Finer,
           "Job Ranks now has (" + pJobRanks.length + ") possible entries."); 
      }
    }

    return readyCnt;
  }

  /**
   * Resize ordered hosts array (if necessary).<P> 
   * 
   * This should only be called from the dspDispatchTotal() method!
   *
   * @param dtm
   *   The core dispatcher timer.
   */ 
  private void
  dsptResizeOrderedHosts
  (
   TaskTimer dtm
  ) 
  {
    if(!pHosts.isEmpty()) {
      int hostsCnt = pHosts.size();
      if(pOrderedHosts.length < hostsCnt) {
        dtm.suspend();
        TaskTimer tm = new TaskTimer("Dispatcher [Resize Ordered Hosts]");
        
        QueueHost[] copy = Arrays.copyOf(pOrderedHosts, (int)(((float) hostsCnt) * 1.5));
        pOrderedHosts = copy;
        
        LogMgr.getInstance().logSubStage
          (LogMgr.Kind.Dsp, LogMgr.Level.Finer, 
           tm, dtm); 
        
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Dsp, LogMgr.Level.Finer,
           "Ordered Hosts now has (" + pOrderedHosts.length + ") possible entries."); 
      }
    }
  }

  /**
   * Sort the enabled host based on their Order property.<P> 
   * 
   * This should only be called from the dspDispatchTotal() method!
   *
   * @param dtm
   *   The core dispatcher timer.
   * 
   * @return 
   *   The number of enabled hosts in pOrderedHosts.
   */ 
  private int 
  dsptOrderHosts
  (
   TaskTimer dtm
  ) 
  {
    dtm.suspend();
    TaskTimer tm = new TaskTimer("Dispatcher [Order Hosts]");
    
    int enabledCnt = 0;
    for(Map.Entry<String,QueueHost> entry : pHosts.entrySet()) {
      QueueHost host = entry.getValue();
      switch(host.getStatus()) {
      case Enabled:
        pOrderedHosts[enabledCnt] = host;
        enabledCnt++;
      }
    }

    Arrays.sort(pOrderedHosts, 0, enabledCnt, new HostOrderComparator());
    
    LogMgr.getInstance().logSubStage
      (LogMgr.Kind.Dsp, LogMgr.Level.Finer, 
       tm, dtm); 

    return enabledCnt; 
  }

  /**
   * Fill the pJobRanks array with entries for all jobs which qualify.<P> 
   * 
   * This should only be called from the dspDispatchTotal() method!
   *
   * @param dtm
   *   The per-slot dispatcher timer.
   * 
   * @param host
   *   The current host.
   * 
   * @return 
   *   The number of jobs which qualify for the slot.
   */ 
  private int 
  dsptQualifyJobs
  (
   TaskTimer stm, 
   QueueHost host
  ) 
  {
    LogMgr lmgr = LogMgr.getInstance(); 
    boolean selFine  = lmgr.isLoggable(LogMgr.Kind.Sel, LogMgr.Level.Fine);
    boolean selFiner = lmgr.isLoggable(LogMgr.Kind.Sel, LogMgr.Level.Finer);

    String hostMsg = null;
    if(selFine) 
      hostMsg = ("[" + host.getName() + "]"); 

    stm.suspend();
    TaskTimer tm = new TaskTimer
      ("Dispatcher [Qualify Jobs - " + host.getName() + "]");

    /* the number of jobs that qualify */ 
    int jobCnt = 0; 

    /* cache of favor method per selection group */ 
    TreeMap<String,JobGroupFavorMethod> favors = new TreeMap<String,JobGroupFavorMethod>();
    tm.aquire();
    synchronized(pSelectionGroups) {
      tm.resume();
      for(String gname : pSelectionGroups.keySet()) 
        favors.put(gname, pSelectionGroups.get(gname).getFavorMethod());
    }

    /* cache latest resource samples */ 
    ResourceSample sample = host.getLatestSample();
    if(sample != null) {
     
      if(selFine) 
        lmgr.logAndFlush
          (LogMgr.Kind.Sel, LogMgr.Level.Fine, 
           hostMsg + ": Qualifying Jobs..."); 

      /* cache other per-hosts information */ 
      String reservation = host.getReservation(); 
      OsType os = host.getOsType();

      /* process all ready jobs */ 
      for(Map.Entry<Long,JobProfile> entry : pReady.entrySet()) {
        Long jobID = entry.getKey();
        JobProfile profile = entry.getValue();

        String jobMsg = null;
        if(selFine) 
          jobMsg = (" - " + hostMsg + jobID + ": "); 

        /* skip those without a profile */ 
        if(profile != null) {
        
          /* make sure the slot provides the required hardware keys */ 
          String hwGroup = host.getHardwareGroup();
          HardwareProfile hwProfile = profile.getHardwareProfile();
          if(hwProfile != null) {
            if(hwProfile.isEligible(hwGroup)) {
          
              /* lookup the selection score */ 
              String selGroup = host.getSelectionGroup();
              SelectionProfile selProfile = profile.getSelectionProfile();
              if(selProfile != null) {
                Integer score = selProfile.getScore(selGroup); 
                if(score != null) {

                  /* make sure the host provides the type of operating system, reservation and 
                     dynamic resources required by the job */                
                  if(profile.isEligible(sample, os, reservation, pAdminPrivileges)) {
                    
                    /* compute the percentage of jobs within the job group
                       which are engaged/pending according to the policy of 
                       the slots selection group (if any) */ 
                    double percent = 0.0;
                    {
                      String gname = host.getSelectionGroup();
                      if(gname != null) {
                        JobGroupFavorMethod favor = favors.get(gname); 
                        if(favor != null) {
                          switch(favor) {
                          case MostEngaged:
                            percent = pJobCounters.percentEngaged(tm, jobID);
                            break;
                            
                          case MostPending:
                            percent = pJobCounters.percentPending(tm, jobID);
                          }
                        }
                      }
                    }
                    
                    /* create a new rank entry for the job */ 
                    {
                      if(pJobRanks[jobCnt] == null) 
                        pJobRanks[jobCnt] = new JobRank();
                      
                      pJobRanks[jobCnt].update(jobID, score, percent, 
                                               profile.getPriority(), 
                                               profile.getTimeStamp()); 
                      jobCnt++; 
                    }
                  }

                  if(selFiner) {
                    lmgr.log(LogMgr.Kind.Sel, LogMgr.Level.Finer, 
                             jobMsg + profile.getEligibilityMsg
                                        (sample, os, reservation, pAdminPrivileges)); 
                  }
                }
                else if(selFiner) {
                  lmgr.logAndFlush
                    (LogMgr.Kind.Sel, LogMgr.Level.Finer, 
                     jobMsg + "Selection group (" + selGroup + ") did not provide required " + 
                     "keys.");
                }
              }
              else if(selFiner) {
                lmgr.logAndFlush
                  (LogMgr.Kind.Sel, LogMgr.Level.Finer, 
                   jobMsg + "No selection profile generated yet."); 
              }
            }
            else if(selFiner) {
              lmgr.logAndFlush
                (LogMgr.Kind.Sel, LogMgr.Level.Finer, 
                 jobMsg + "Hardware group (" + hwGroup + ") did not provide required keys.");
            }
          }
          else if(selFiner) {
            lmgr.logAndFlush
              (LogMgr.Kind.Sel, LogMgr.Level.Finer, 
               jobMsg + "No hardware profile generated yet."); 
          }              
        }
        else if(selFiner) {
          lmgr.logAndFlush
            (LogMgr.Kind.Sel, LogMgr.Level.Finer, 
             jobMsg + "No job profile generated yet."); 
        }
      }
    }
    else if(selFine) {
      lmgr.logAndFlush
        (LogMgr.Kind.Sel, LogMgr.Level.Fine, 
         hostMsg + ": No resource samples collected yet."); 
    }

    LogMgr.getInstance().logSubStage
      (LogMgr.Kind.Dsp, LogMgr.Level.Finer, 
       tm, stm);

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Dsp, LogMgr.Level.Finer,
       "Qualified (" + jobCnt + ") jobs for the slot."); 

    return jobCnt;
  }

  /**
   * Rank the jobs by sorting the portion of the rank array just populated.<P> 
   * 
   * This should only be called from the dspDispatchTotal() method!
   *
   * @param stm
   *   The per-slot dispatcher timer.
   * 
   * @param hostname
   *   The name of the current host.
   * 
   * @param jobCnt
   *   The number of jobs which qualify for the slot.
   */ 
  private void 
  dsptRankJobs
  (
   TaskTimer stm, 
   String hostname, 
   int jobCnt
  ) 
  {
    stm.suspend();
    TaskTimer tm = new TaskTimer
      ("Dispatcher [Rank Jobs - " + hostname + "]");
    
    Arrays.sort(pJobRanks, 0, jobCnt);

    LogMgr.getInstance().logSubStage
      (LogMgr.Kind.Dsp, LogMgr.Level.Finer, 
       tm, stm);

    /* selection logging... */       
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Sel, LogMgr.Level.Finest)) {
      StringBuilder buf = new StringBuilder(); 

      buf.append("[" + hostname + "]: Ranking Jobs...\n" +
                 "  JobID(Rank): Score Percent Priority Date Time"); 

      int wk; 
      for(wk=0; wk<jobCnt; wk++) 
        buf.append("\n  " + pJobRanks[wk].selectionLogMsg(wk)); 

      LogMgr.getInstance().logAndFlush(LogMgr.Kind.Sel, LogMgr.Level.Finest, buf.toString()); 
    }
    else if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Sel, LogMgr.Level.Fine)) {
      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Sel, LogMgr.Level.Fine, 
         "[" + hostname + "]: Ranking Jobs..."); 
    }
  }

  /**
   * Attempt to dispatch a job to the slot in order of selection score, 
   * favor pending/engaged, job priority and age.<P> 
   * 
   * This should only be called from the dspDispatchTotal() method!
   *
   * @param stm
   *   The per-slot dispatcher timer.
   * 
   * @param host
   *   The current host.
   * 
   * @param jobCnt
   *   The number of jobs which qualify for the slot.
   */ 
  private void 
  dsptAssignJob
  (
   TaskTimer stm, 
   QueueHost host, 
   int jobCnt
  ) 
  {
    LogMgr lmgr = LogMgr.getInstance(); 
    boolean selFiner = lmgr.isLoggable(LogMgr.Kind.Sel, LogMgr.Level.Finer);

    String hostMsg = null;
    if(selFiner) 
      hostMsg = ("[" + host.getName() + "] - "); 

    stm.suspend();
    TaskTimer tm = new TaskTimer
      ("Dispatcher [Assign Job - " + host.getName() + "]");
    
    TreeSet<Long> notReady = new TreeSet<Long>();

    boolean jobDispatched = false;
    int jk;
    for(jk=0; jk<jobCnt && !jobDispatched; jk++) {
      long jobID = pJobRanks[jk].getJobID();
              
      QueueJob job = null;
      tm.aquire();
      synchronized(pJobs) {
        tm.resume();
        job = pJobs.get(jobID);
      }
              
      QueueJobInfo info = getJobInfo(tm, jobID); 
              
      if(job == null) {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Dsp, LogMgr.Level.Warning,
           "While attempting to dispatch the job (" + jobID + ") from the ranked jobs, " + 
           "no job entry could be found!  Removing it from the ready list."); 
        notReady.add(jobID);
      }
      else if(info == null) {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Dsp, LogMgr.Level.Warning,
           "While attempting to dispatch the job (" + jobID + ") from the ranked jobs, " + 
           "no job status information could be found!  Removing it from the ready list."); 
        notReady.add(jobID);
      }
      else {
        switch(info.getState()) {
        case Queued:
        case Preempted:
          /* attempt to dispatch the ready to execute job */ 
          if(dispatchJob(job, info, host, tm)) {
            notReady.add(jobID);
            jobDispatched = true;
          }
          break;

        case Aborted:
          notReady.add(jobID);
          break;
		
        default:
          {
            String msg = 
              ("While attempting to dispatch job (" + jobID + ") from the ranked jobs, " + 
               "it was in an unexpected (" + info.getState() + ") state.  Removing it " + 
               "from the ready list");

            switch(info.getState()) {
            case Paused:
              LogMgr.getInstance().logAndFlush
                (LogMgr.Kind.Dsp, LogMgr.Level.Warning,
                 msg + " and putting it back into the waiting list.");
              notReady.add(jobID);
              pWaiting.add(jobID);
              break;

            case Running:
            case Limbo:
            case Finished:
            case Failed:
              LogMgr.getInstance().logAndFlush
                (LogMgr.Kind.Dsp, LogMgr.Level.Warning,
                 msg + "."); 
              notReady.add(jobID);
            }
          }
        }
      }

      if(selFiner && jobDispatched) 
        lmgr.logAndFlush
          (LogMgr.Kind.Sel, LogMgr.Level.Finer, 
           hostMsg + jobID + "(" + jk + "): DISPATCHED!");
    }

    for(Long jobID : notReady) 
      pReady.remove(jobID);

    LogMgr.getInstance().logSubStage
      (LogMgr.Kind.Dsp, LogMgr.Level.Finer, 
       tm, stm);

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Dsp, LogMgr.Level.Finer,
       "Processed (" + jk + ") jobs " + 
       (jobDispatched ? "before assigning one" : 
        "but was unable to assign any") + " to the slot.");
  }

  /**
   * Attempt to dispatch the job on the given server. <P> 
   * 
   * If all license keys can be obtained, the job will be started on the server and a task
   * will be started to monitor the jobs progress. <P> 
   * 
   * This should only be called from the dsptAssignJob() method!
   * 
   * @param job
   *   The queue job.
   * 
   * @param info
   *   The job information.
   * 
   * @param host
   *   The job server.
   * 
   * @param timer
   *   The task timer.
   * 
   * @return 
   *   Whether the job was started. 
   */ 
  private boolean
  dispatchJob
  (
   QueueJob job, 
   QueueJobInfo info, 
   QueueHost host, 
   TaskTimer timer
  ) 
  {
    LogMgr lmgr = LogMgr.getInstance(); 
    boolean selFiner = lmgr.isLoggable(LogMgr.Kind.Sel, LogMgr.Level.Finer);

    JobReqs jreqs = job.getJobRequirements();
    
    /* aquire the jobs license keys, 
         aborts early if unable to aquire all keys required by the job */ 
    TreeSet<String> aquiredKeys = new TreeSet<String>();
    {
      boolean available = true;
      timer.aquire();
      synchronized(pLicenseKeys) {
 	timer.resume();
 	for(String kname : jreqs.getLicenseKeys()) {
 	  LicenseKey key = pLicenseKeys.get(kname);
 	  if(key != null) {
 	    if(key.acquire(host.getName())) 
 	      aquiredKeys.add(kname);
 	    else {
 	      available = false; 
 	      break;
 	    }
 	  }
 	}
	
 	if(!available) {
 	  for(String kname : aquiredKeys) {
	    LicenseKey key = pLicenseKeys.get(kname);
	    if(key != null) 
	      key.release(host.getName());
	  }

          if(selFiner) {
            lmgr.logAndFlush
              (LogMgr.Kind.Sel, LogMgr.Level.Finer,
               "[" + host.getName() + "] - " + job.getJobID() + ": " + 
               "Unable to acquire all license keys needed by job."); 
          }

 	  return false;
 	}
      }
    }

    /* lookup the toolset for the job and cook the environment */ 
    DoubleMap<OsType,String,String> envs = new DoubleMap<OsType,String,String>();
    {
      String tname  = job.getActionAgenda().getToolset();
      NodeID nodeID = job.getNodeID();
      String author = nodeID.getAuthor();
      String view   = nodeID.getView();

      timer.aquire();
      synchronized(pToolsets) {
	timer.resume();
	
	TreeMap<OsType,Toolset> toolsets = pToolsets.get(tname);
	if(toolsets == null) 
	  throw new IllegalStateException("The toolsets cannot be (null)!");

	for(OsType os : toolsets.keySet()) {
	  Toolset tset = toolsets.get(os);
	  if(tset == null) 
	    throw new IllegalStateException("The toolset cannot be (null)!");
	  
	  TreeMap<String,String> env = null;
          switch(os) {
          case Windows:
            {
              env = tset.getEnvironment(PackageInfo.sPipelineUser, os);  
              Path working = new Path(PackageInfo.getWorkPath(os), author + "/" + view);
              env.put("WORKING", working.toOsString(os));
            }
            break;

          default:
            env = tset.getEnvironment(author, view, os);         
          }
	  
	  if(env == null) 
	    throw new IllegalStateException("The environment cannot be (null)!");
	  envs.put(os, env);
	}
      }
    }

    /* mark the job as being started on the selected server
         technically this hasn't actually happened yet, but will happen shortly... */ 
    {
      timer.aquire();
      synchronized(pJobInfo) {
	timer.resume();

        JobState prevState = info.started(host.getName(), host.getOsType());
	pJobCounters.update(timer, prevState, info);
	try {
	  writeJobInfo(info);
	}
	catch (PipelineException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Ops, LogMgr.Level.Severe,
	     ex.getMessage()); 
	}
      }

      host.setHold(job.getJobID(), jreqs.getRampUp());
    }

    /* start a task to contact the job server to the job 
         and collect the results of the execution */ 
    {
      MonitorTask task = 
        new MonitorTask(host.getName(), host.getOsType(), job, aquiredKeys, envs);
      monitorJob(task); 
      task.start();
    }

    return true;
  }

  /**
   * Filter any jobs not ready for execution from the ready list.<P> 
   * 
   * This should only be called from the dispatcher() method!
   *
   * @param timer
   *   The overall dispatcher timer.
   */ 
  private void 
  dspFilterUnready
  (
   TaskTimer timer
  ) 
  {
    timer.suspend();
    TaskTimer tm = new TaskTimer("Dispatcher [Filter Unready]");

    /* process the ready jobs */ 
    TreeSet<Long> notReady = new TreeSet<Long>();
    for(Long jobID : pReady.keySet()) {

      QueueJobInfo info = getJobInfo(tm, jobID); 
      if(info == null) {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Dsp, LogMgr.Level.Warning,
           "While processing the job (" + jobID + ") in the ready list, no job status " + 
           "information could be found!  Removing it from the ready list."); 
        notReady.add(jobID);
      }
      else {
        switch(info.getState()) {
        case Queued:
        case Preempted:
          {
            boolean pauseJob = false;
            synchronized(pPause) {
              pauseJob = pPause.remove(jobID); 
            }

            /* pause ready jobs marked to be paused */ 
            if(pauseJob) {
              JobState prevState = info.paused();
              pJobCounters.update(tm, prevState, info);
              try {
                writeJobInfo(info);
              }
              catch(PipelineException ex) {
                LogMgr.getInstance().log
                  (LogMgr.Kind.Dsp, LogMgr.Level.Severe,
                   ex.getMessage()); 
              }		
              
              pWaiting.add(jobID);
              notReady.add(jobID);
            }
          }
          break;
	  
        case Paused:
          LogMgr.getInstance().logAndFlush
            (LogMgr.Kind.Dsp, LogMgr.Level.Warning,
             "While processing the job (" + jobID + ") in the ready list, it was in an " + 
             "unexpected (" + info.getState() + ") state.  Removing it from the ready " + 
             "list putting it back on the waiting list."); 
          pWaiting.add(jobID);
          notReady.add(jobID);
          break;

        default:
          /* any job not in a Queued or Preempted state cannot be ready to run */ 
          notReady.add(jobID);
        }
      }
    }

    /* clean up all unready jobs found */ 
    for(Long jobID : notReady) 
      pReady.remove(jobID);

    LogMgr.getInstance().logSubStage
      (LogMgr.Kind.Dsp, LogMgr.Level.Finer, 
       tm, timer);
  }

  /**
   * Check for newly completed job groups.<P> 
   * 
   * This should only be called from the dispatcher() method!
   *
   * @param timer
   *   The overall dispatcher timer.
   */ 
  private void 
  dspUpdateJobGroups
  (
   TaskTimer timer
  ) 
  {
    timer.suspend();
    TaskTimer tm = new TaskTimer("Dispatcher [Update Job Groups]");

    tm.aquire();
    synchronized(pJobGroups) {
      tm.resume();
      for(Long groupID : pJobGroups.keySet()) {
        QueueJobGroup group = pJobGroups.get(groupID);
	
        /* the job is not yet completed */ 
        if((group != null) && (group.getCompletedStamp() == null)) {
          boolean done = true; 
          Long latest = null;
          for(Long jobID : group.getAllJobIDs()) {
            QueueJobInfo info = getJobInfo(tm, jobID); 
            if(info != null) {
              switch(info.getState()) {
              case Queued:
              case Preempted:
              case Paused:
              case Running:
              case Limbo:
                done = false;
                break;

              default: 
                {
                  Long stamp = info.getCompletedStamp(); 
                  if((stamp != null) && 
                     ((latest == null) || (stamp > latest)))
                    latest = stamp;
                }
              }
            }
          }

          /* update the completed group */ 
          if(done && (latest != null)) {
            group.completed(latest);
            try {
              writeJobGroup(group);
            }
            catch(PipelineException ex) {
              LogMgr.getInstance().log
                (LogMgr.Kind.Dsp, LogMgr.Level.Severe,
                 ex.getMessage());
            }
          }
        }
      }
    }
    LogMgr.getInstance().logSubStage
      (LogMgr.Kind.Dsp, LogMgr.Level.Finer, 
       tm, timer);
  }
  
  /**
   * Garbage collect all jobs no longer referenced by a job group.
   */ 
  private void 
  garbageCollectJobs
  (
   TaskTimer timer
  ) 
  {
    /* get the IDs of all jobs still referenced by a job group */ 
    TreeSet<Long> live = new TreeSet<Long>();
    {
      timer.aquire();
      synchronized(pJobGroups) {
	timer.resume();
	for(QueueJobGroup group : pJobGroups.values()) 
	  live.addAll(group.getAllJobIDs());
      }
    }

    /* get the IDs of the jobs which should be deleted */ 
    TreeSet<Long> dead = new TreeSet<Long>();
    {
      timer.aquire();
      synchronized(pJobs) {
	timer.resume();
	for(Long jobID : pJobs.keySet()) 
	  if(!live.contains(jobID))
	    dead.add(jobID);
      }

      timer.aquire();
      synchronized(pJobInfo) {
	timer.resume();
	for(Long jobID : pJobInfo.keySet()) 
	  if(!live.contains(jobID))
	    dead.add(jobID);
      }
    }
      
    /* delete the dead jobs */ 
    TreeMap<Long,QueueJob> deadJobs = new TreeMap<Long,QueueJob>();
    TreeMap<Long,QueueJobInfo> deadInfos = new TreeMap<Long,QueueJobInfo>();    
    for(Long jobID : dead) {
      timer.aquire();
      synchronized(pJobs) {
	timer.resume();
	try {
	  QueueJob job = pJobs.remove(jobID);
	  if(job != null) {
	    deleteJobFile(jobID);
	    deadJobs.put(jobID, job);
	  }
	}
	catch(PipelineException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Ops, LogMgr.Level.Severe,
	     ex.getMessage());
	}
      }

      timer.aquire();
      synchronized(pJobInfo) {
	timer.resume();
	try {
	  QueueJobInfo info = pJobInfo.remove(jobID);
	  if(info != null) {
	    deleteJobInfoFile(jobID);
	    deadInfos.put(jobID, info);
	  }
	}
	catch(PipelineException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Ops, LogMgr.Level.Severe,
	     ex.getMessage());
	}
      }      
    }

    /* tell the job servers to cleanup any resources associated with the dead jobs */ 
    {
      CleanupJobResourcesTask task = new CleanupJobResourcesTask(live);
      task.start();
    }

    /* post-cleanup task */ 
    if(!deadJobs.isEmpty() && !deadInfos.isEmpty())  
      startExtensionTasks(timer, new CleanupJobsExtFactory(deadJobs, deadInfos));

    LogMgr.getInstance().logStage
      (LogMgr.Kind.Ops, LogMgr.Level.Finer,
       timer); 
  }

  /**
   * Dump the current state of the jobs. <P> 
   * 
   * This should only be called from the dispatcher() method!
   */ 
  private void 
  logJobLists
  (
   String title
  ) 
  {
    if(!LogMgr.getInstance().isLoggable(LogMgr.Kind.Ops, LogMgr.Level.Finest))
      return;

    StringBuilder buf = new StringBuilder();
    buf.append("JOBS: " + title + "\n");
	       
    buf.append("  HitList: ");
    for(Long jobID : pHitList) 
      buf.append(jobID + " ");
    buf.append("\n");

    buf.append("  Waiting: ");
    for(Long jobID : pWaiting) 
      buf.append(jobID + " ");
    buf.append("\n");

    synchronized(pReady) {
      buf.append("  Ready:\n");
      for(Long jobID : pReady.keySet()) 
	buf.append(jobID + " ");
      buf.append("\n");
    }
    
    synchronized(pJobInfo) {
      buf.append("  Job Info:\n");
      for(Long jobID : pJobInfo.keySet()) {
	buf.append("    " + jobID + ": ");

	QueueJobInfo info = pJobInfo.get(jobID);
	if(info != null) {
	  buf.append(info.getState());
	  String host = info.getHostname();
	  if(host != null) 
	    buf.append(" on " + host);
	}
	buf.append("\n");
      }
    }

    buf.append("\n");

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Ops, LogMgr.Level.Finest,
       buf.toString());
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Register a MonitorTask thread to manage a running job.
   */ 
  private void 
  monitorJob
  (
   MonitorTask task
  ) 
  {
    synchronized(pRunning) {
      MonitorTask existing = pRunning.get(task.getHostname(), task.getJobID()); 
      if(existing != null) {
        LogMgr.getInstance().log
          (LogMgr.Kind.Ops, LogMgr.Level.Severe,
           "Somehow there was already a registered MonitorTask thread for the " + 
           "job (" + task.getJobID() + ") running on host (" + task.getHostname() + ") " + 
           "when attempting to register another thread for the job!"); 
      }
      
      pRunning.put(task.getHostname(), task.getJobID(), task); 
    } 
  }

  /**
   * Unregister the existing MonitorTask thread due to completion of the job.
   */ 
  private void 
  unmonitorJob
  (
   MonitorTask task
  )
  {
    synchronized(pRunning) {
      MonitorTask existing = pRunning.remove(task.getHostname(), task.getJobID()); 
      if(existing == null) {
        LogMgr.getInstance().log
          (LogMgr.Kind.Ops, LogMgr.Level.Severe,
           "Somehow there was no registered MonitorTask thread for the job " +
           "(" + task.getJobID() + ") running on host (" + task.getHostname() + ") when " + 
           "attempting to unregister the thread for the job!"); 
      }
    }
  }

  /**
   * Replace the existing running MonitorTask with a copy of the thread which is not
   * yet running in response to loosing contact with the host running the managed job.
   */ 
  private void 
  remonitorJob
  (
   MonitorTask task
  ) 
  {
    synchronized(pRunning) {
      MonitorTask existing = pRunning.remove(task.getHostname(), task.getJobID()); 
      if(task != existing) {
        LogMgr.getInstance().log
          (LogMgr.Kind.Ops, LogMgr.Level.Severe,
           "Somehow the MonitorTask already registered for job (" + task.getJobID() + ") " + 
           "running on host (" + task.getHostname() + ") did not match the task " + 
           "attempting to re-register itself for the job!"); 
      }
      
      MonitorTask ntask = task.remonitorClone();
      pRunning.put(ntask.getHostname(), ntask.getJobID(), ntask); 
    }
  }

  /**
   * Start any MonitorTask threads registered to the host but which are not currently
   * running.
   */ 
  private void 
  resumeMonitoringJobs
  (
   String hostname
  ) 
  {
    synchronized(pRunning) {
      Set<Long> jobIDs = pRunning.keySet(hostname);
      if(jobIDs != null) {
        for(Long jobID : jobIDs) {
          MonitorTask task = pRunning.get(hostname, jobID);
          if((task != null) && !task.isAlive())
            task.start(); 
        }
      }
    } 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S C H E D U L E R                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Perform one round of scheduling the assignment of selection groups to job servers. 
   */ 
  public void
  scheduler()
  {
    TaskTimer timer = new TaskTimer("Scheduler");
    
    doScheduler(timer);
    
    /* if we're ahead of schedule, take a nap */ 
    {
      LogMgr.getInstance().logStage
	(LogMgr.Kind.Sch, LogMgr.Level.Fine,
	 timer); 

      long nap = sSchedulerInterval - timer.getTotalDuration();
      if(nap > 0) {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Sch, LogMgr.Level.Finer,
	   "Scheduler: Sleeping for (" + nap + ") msec...");

	try {
	  Thread.sleep(nap);
	}
	catch(InterruptedException ex) {
	}
      }
      else {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Sch, LogMgr.Level.Finer,
	   "Scheduler: Overbudget by (" + (-nap) + ") msec...");
      }	

      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Sch, LogMgr.Level.Finer,
         "\n-----------------------------------------------------------------------------\n");
    }
  }
  
  /**
   * Method which actually does the scheduling.
   * <p>
   * Broken out in case it ever needs to be called by itself.
   * @param timer
   */
  private void 
  doScheduler
  (
    TaskTimer timer  
  )
  {
    /* precompute current groups for each schedule */ 
    SelectionScheduleMatrix matrix = null;
    {
      timer.suspend();
      TaskTimer tm = new TaskTimer("Scheduler [Compute Groups]");
      tm.aquire();
      synchronized(pSelectionSchedules) {
	tm.resume();
        long now = System.currentTimeMillis();
        matrix = new SelectionScheduleMatrix(pSelectionSchedules, now);
      }
      LogMgr.getInstance().logSubStage
	(LogMgr.Kind.Sch, LogMgr.Level.Finer, 
	 tm, timer);
    }
    
    /* update the hosts */ 
    {
      timer.suspend();
      TaskTimer utm = new TaskTimer("Scheduler [Update HostMods]");      
      utm.aquire();
      synchronized(pHostsInfo) {
	synchronized(pHostsMod) {
	  utm.resume();

	  for(String hname : pHostsInfo.keySet()) {
	    QueueHostInfo host = pHostsInfo.get(hname);

	    String sname = host.getSelectionSchedule();

	    if(sname != null) {

	      QueueHostMod scheduleMod = 
		QueueHostMod.getModFromSchedule(matrix, sname, host);

	      /* Selection Group */
	      if (scheduleMod.isSelectionGroupModified())
		LogMgr.getInstance().logAndFlush
		(LogMgr.Kind.Sch, LogMgr.Level.Finest,
		 "Scheduler [" + hname + "]: " + 
		 "Selection Group = " + scheduleMod.getSelectionGroup() + 
		 " (" + host.getSelectionGroup()+ ")\n");
	      if (scheduleMod.isStatusModified())
		LogMgr.getInstance().logAndFlush
		(LogMgr.Kind.Sch, LogMgr.Level.Finest,
		 "Scheduler [" + hname + "]: " + 
		 "Server Status = " + scheduleMod.getStatus().toTitle() + 
		 " (" + host.getStatus().toTitle() + ")\n");
	      if (scheduleMod.isReservationModified())
		LogMgr.getInstance().logAndFlush
		(LogMgr.Kind.Sch, LogMgr.Level.Finest,
		 "Scheduler [" + hname + "]: " + 
		 "Remove Reservation\n");
	      if (scheduleMod.isOrderModified())
		LogMgr.getInstance().logAndFlush
		(LogMgr.Kind.Sch, LogMgr.Level.Finest,
		 "Scheduler [" + hname + "]: " + 
		 "Order = " + scheduleMod.getOrder() + 
		 " (" + host.getOrder() + ")\n");
	      if (scheduleMod.isJobSlotsModified()) 
		LogMgr.getInstance().logAndFlush
		(LogMgr.Kind.Sch, LogMgr.Level.Finest,
		 "Scheduler [" + hname + "]: " + 
		 "Slots = " + scheduleMod.getJobSlots() + 
		 " (" + host.getJobSlots() + ")\n");
	      
	      QueueHostMod existing = pHostsMod.get(hname);
	      if (existing != null)
	        QueueHostMod.combineMods(scheduleMod, existing);
	      pHostsMod.put(hname, scheduleMod);
	    }
	  }
	  LogMgr.getInstance().logSubStage
	  (LogMgr.Kind.Sch, LogMgr.Level.Finer, 
	    utm, timer);
	}
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   J V M   M E M O R Y   S T A T I S T I C S                                            */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Report the current JVM heap statistics.
   */ 
  public void 
  heapStats() 
  {
    TaskTimer timer = new TaskTimer();

    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Mem, LogMgr.Level.Fine)) {
      Runtime rt = Runtime.getRuntime();
      long freeMemory  = rt.freeMemory();
      long totalMemory = rt.totalMemory();
      long maxMemory   = rt.maxMemory();
      long overhead    = maxMemory - totalMemory + freeMemory;
      
      LogMgr.getInstance().log
	(LogMgr.Kind.Mem, LogMgr.Level.Fine,
	 "Memory Stats:\n" + 
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
	 "  ------------------------------------");
      LogMgr.getInstance().flush();
    }

    /* if we're ahead of schedule, take a nap */ 
    {
      timer.suspend();
      long nap = sHeapStatsInterval - timer.getTotalDuration();
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
  /*   T O O L S E T S   H E L P E R S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Retrieve any of the given toolset which are not already cached.
   */ 
  private void 
  fetchToolsets
  (
   TreeSet<String> tnames, 
   TaskTimer timer
  ) 
  {
    timer.aquire();
    synchronized(pToolsets) {
      timer.resume();
      for(String tname : tnames) {
	if(!pToolsets.containsKey(tname)) {
	  while(true) {
	    try {
	      pToolsets.put(tname, pMasterMgrClient.getOsToolsets(tname));	
	      break;
	    }
	    catch(PipelineException ex) {
	      LogMgr.getInstance().log
		(LogMgr.Kind.Net, LogMgr.Level.Severe,
		 ex.getMessage()); 
	      
	      LogMgr.getInstance().logAndFlush
		(LogMgr.Kind.Net, LogMgr.Level.Info,
		 "Reestablishing Network Connections...");
	      
	      establishMasterConnection();
	    }
	  }
	}
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   J O B   H E L P E R S                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the job information for the given job ID.
   */ 
  private QueueJobInfo
  getJobInfo
  (
   TaskTimer tm, 
   long jobID
  ) 
  {
    tm.aquire(); 
    synchronized(pJobInfo) {
      tm.resume();
      return pJobInfo.get(jobID);
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   I / O   H E L P E R S                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the license keys to disk. <P> 
   * 
   * @throws PipelineException
   *   If unable to write the license keys file.
   */ 
  private void 
  writeLicenseKeys() 
    throws PipelineException
  {
    synchronized(pLicenseKeys) {
      File file = new File(pQueueDir, "queue/etc/license-keys");
      if(file.exists()) {
	if(!file.delete())
	  throw new PipelineException
	    ("Unable to remove the old license keys file (" + file + ")!");
      }
      
      if(!pLicenseKeys.isEmpty()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Writing License Keys.");

        try { 
          ArrayList<LicenseKey> keys = new ArrayList<LicenseKey>(pLicenseKeys.values());
          GlueEncoderImpl.encodeFile("LicenseKeys", keys, file);
        }
	catch(GlueException ex) {
	  throw new PipelineException(ex);
        }
      }
    }
  }
  
  /**
   * Read the license keys from disk. <P> 
   * 
   * @throws PipelineException
   *   If unable to read the license keys file.
   */ 
  private void 
  readLicenseKeys() 
    throws PipelineException
  {
    synchronized(pLicenseKeys) {
      pLicenseKeys.clear();

      File file = new File(pQueueDir, "queue/etc/license-keys");
      if(file.isFile()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Reading License Keys.");

	ArrayList<LicenseKey> keys = null;
	try {
          keys = (ArrayList<LicenseKey>) GlueDecoderImpl.decodeFile("LicenseKeys", file);
        }
        catch(GlueException ex) {
          throw new PipelineException(ex);
        }

	if(keys == null) 
	  throw new IllegalStateException("The license keys cannot be (null)!");

	for(LicenseKey key : keys) 
	  pLicenseKeys.put(key.getName(), key);
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the hardware keys to disk. <P> 
   * 
   * @throws PipelineException
   *   If unable to write the hardware keys file.
   */ 
  private void 
  writeHardwareKeys() 
    throws PipelineException
  {
    synchronized(pHardwareKeys) {
      File file = new File(pQueueDir, "queue/etc/hardware-keys");
      if(file.exists()) {
	if(!file.delete())
	  throw new PipelineException
	    ("Unable to remove the old hardware keys file (" + file + ")!");
      }
      
      if(!pHardwareKeys.isEmpty()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Writing Hardware Keys.");

        try {
          ArrayList<HardwareKey> keys = 
            new ArrayList<HardwareKey>(pHardwareKeys.values());
          GlueEncoderImpl.encodeFile("HardwareKeys", keys, file);
        }
        catch(GlueException ex) {
          throw new PipelineException(ex);
        }
      }
    }
  }
  
  /**
   * Read the hardware keys from disk. <P> 
   * 
   * @throws PipelineException
   *   If unable to read the hardware keys file.
   */ 
  private void 
  readHardwareKeys() 
    throws PipelineException
  {
    synchronized(pHardwareKeys) {
      pHardwareChanged.set(true);
      pHardwareKeys.clear();

      File file = new File(pQueueDir, "queue/etc/hardware-keys");
      if(file.isFile()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Reading Hardware Keys.");

        ArrayList<HardwareKey> keys = null;
        try {
          keys = (ArrayList<HardwareKey>) GlueDecoderImpl.decodeFile("HardwareKeys", file);
        }	
        catch(GlueException ex) {
          throw new PipelineException(ex);
        }
        
	if(keys == null) 
	  throw new IllegalStateException("The hardware keys cannot be (null)!");

	for(HardwareKey key : keys) 
	  pHardwareKeys.put(key.getName(), key);
      }
    }
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the hardware groups to disk. <P> 
   * 
   * @throws PipelineException
   *   If unable to write the hardware groups file.
   */ 
  private void 
  writeHardwareGroups() 
    throws PipelineException
  {
    synchronized(pHardwareGroups) {
      File file = new File(pQueueDir, "queue/etc/hardware-groups");
      if(file.exists()) {
	if(!file.delete())
	  throw new PipelineException
	    ("Unable to remove the old hardware groups file (" + file + ")!");
      }
      
      if(!pHardwareGroups.isEmpty()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Writing Hardware Groups.");

        try {
          ArrayList<HardwareGroup> groups = 
            new ArrayList<HardwareGroup>(pHardwareGroups.values());
          GlueEncoderImpl.encodeFile("HardwareGroups", groups, file);
        }
        catch(GlueException ex) {
          throw new PipelineException(ex);
        }
      }
    }
  }
  
  /**
   * Read the hardware groups from disk. <P> 
   * 
   * @throws PipelineException
   *   If unable to read the hardware groups file.
   */ 
  private void 
  readHardwareGroups() 
    throws PipelineException
  {
    synchronized(pHardwareGroups) {
      pHardwareChanged.set(true);
      pHardwareGroups.clear();

      File file = new File(pQueueDir, "queue/etc/hardware-groups");
      if(file.isFile()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Reading Selection Groups.");

	ArrayList<HardwareGroup> groups = null;
        try {
          groups = 
            (ArrayList<HardwareGroup>) GlueDecoderImpl.decodeFile("HardwareGroups", file);
        }	
        catch(GlueException ex) {
          throw new PipelineException(ex);
        }
        
	if(groups == null) 
	  throw new IllegalStateException("The hardware groups cannot be (null)!");

	for(HardwareGroup key : groups) 
	  pHardwareGroups.put(key.getName(), key);
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the selection keys to disk. <P> 
   * 
   * @throws PipelineException
   *   If unable to write the selection keys file.
   */ 
  private void 
  writeSelectionKeys() 
    throws PipelineException
  {
    synchronized(pSelectionKeys) {
      File file = new File(pQueueDir, "queue/etc/selection-keys");
      if(file.exists()) {
	if(!file.delete())
	  throw new PipelineException
	    ("Unable to remove the old selection keys file (" + file + ")!");
      }
      
      if(!pSelectionKeys.isEmpty()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Writing Selection Keys.");

        try {
          ArrayList<SelectionKey> keys = 
            new ArrayList<SelectionKey>(pSelectionKeys.values());
          GlueEncoderImpl.encodeFile("SelectionKeys", keys, file);
        }
        catch(GlueException ex) {
          throw new PipelineException(ex);
        }
      }
    }
  }
  
  /**
   * Read the selection keys from disk. <P> 
   * 
   * @throws PipelineException
   *   If unable to read the selection keys file.
   */ 
  private void 
  readSelectionKeys() 
    throws PipelineException
  {
    synchronized(pSelectionKeys) {
      pSelectionChanged.set(true);
      pSelectionKeys.clear();

      File file = new File(pQueueDir, "queue/etc/selection-keys");
      if(file.isFile()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Reading Selection Keys.");

	ArrayList<SelectionKey> keys = null;
        try {
          keys = (ArrayList<SelectionKey>) GlueDecoderImpl.decodeFile("SelectionKeys", file);
        }	
        catch(GlueException ex) {
          throw new PipelineException(ex);
        }

	if(keys == null) 
	  throw new IllegalStateException("The selection keys cannot be (null)!");
	
	for(SelectionKey key : keys) 
	  pSelectionKeys.put(key.getName(), key);
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the selection groups to disk. <P> 
   * 
   * @throws PipelineException
   *   If unable to write the selection groups file.
   */ 
  private void 
  writeSelectionGroups() 
    throws PipelineException
  {
    synchronized(pSelectionGroups) {
      File file = new File(pQueueDir, "queue/etc/selection-groups");
      if(file.exists()) {
	if(!file.delete())
	  throw new PipelineException
	    ("Unable to remove the old selection groups file (" + file + ")!");
      }
      
      if(!pSelectionGroups.isEmpty()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Writing Selection Groups.");

        try {
          ArrayList<SelectionGroup> groups = 
            new ArrayList<SelectionGroup>(pSelectionGroups.values());
          GlueEncoderImpl.encodeFile("SelectionGroups", groups, file);
        }
        catch(GlueException ex) {
          throw new PipelineException(ex);
        }
      }
    }
  }
  
  /**
   * Read the selection groups from disk. <P> 
   * 
   * @throws PipelineException
   *   If unable to read the selection groups file.
   */ 
  private void 
  readSelectionGroups() 
    throws PipelineException
  {
    synchronized(pSelectionGroups) {
      pSelectionChanged.set(true);
      pSelectionGroups.clear();

      File file = new File(pQueueDir, "queue/etc/selection-groups");
      if(file.isFile()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Reading Selection Groups.");

	ArrayList<SelectionGroup> groups = null;
        try {
          groups = 
            (ArrayList<SelectionGroup>) GlueDecoderImpl.decodeFile("SelectionGroups", file);
        }	
        catch(GlueException ex) {
          throw new PipelineException(ex);
        }

	if(groups == null) 
	  throw new IllegalStateException("The selection groups cannot be (null)!");

	for(SelectionGroup key : groups) 
	  pSelectionGroups.put(key.getName(), key);
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the selection schedules to disk. <P> 
   * 
   * @throws PipelineException
   *   If unable to write the selection schedules file.
   */ 
  private void 
  writeSelectionSchedules() 
    throws PipelineException
  {
    synchronized(pSelectionSchedules) {
      File file = new File(pQueueDir, "queue/etc/selection-schedules");
      if(file.exists()) {
	if(!file.delete())
	  throw new PipelineException
	    ("Unable to remove the old selection schedules file (" + file + ")!");
      }
      
      if(!pSelectionSchedules.isEmpty()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Writing Selection Schedules.");

        try {
          ArrayList<SelectionSchedule> schedules = 
            new ArrayList<SelectionSchedule>(pSelectionSchedules.values());
          GlueEncoderImpl.encodeFile("SelectionSchedules", schedules, file);
        }
        catch(GlueException ex) {
          throw new PipelineException(ex);
        }
      }
    }
  }
  
  /**
   * Read the selection schedules from disk. <P> 
   * 
   * @throws PipelineException
   *   If unable to read the selection schedules file.
   */ 
  private void 
  readSelectionSchedules() 
    throws PipelineException
  {
    synchronized(pSelectionSchedules) {
      pSelectionSchedules.clear();

      File file = new File(pQueueDir, "queue/etc/selection-schedules");
      if(file.isFile()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Reading Selection Schedules.");

	ArrayList<SelectionSchedule> schedules = null;
        try {
          schedules = 
            (ArrayList<SelectionSchedule>) 
            GlueDecoderImpl.decodeFile("SelectionSchedules", file);
        }	
        catch(GlueException ex) {
          throw new PipelineException(ex);
        }

	if(schedules == null) 
	  throw new IllegalStateException("The selection schedules cannot be (null)!");

	for(SelectionSchedule key : schedules) 
	  pSelectionSchedules.put(key.getName(), key);
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the queue extension configurations to disk. <P> 
   * 
   * @throws PipelineException
   *   If unable to write the extensions file.
   */ 
  private void 
  writeQueueExtensions() 
    throws PipelineException
  {
    synchronized(pQueueExtensions) {
      File file = new File(pQueueDir, "queue/etc/queue-extensions"); 
      if(file.exists()) {
	if(!file.delete())
	  throw new PipelineException
	    ("Unable to remove the old queue extensions file (" + file + ")!");
      }

      if(!pQueueExtensions.isEmpty()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Writing Queue Extensions.");

        try {
          GlueEncoderImpl.encodeFile("QueueExtensions", pQueueExtensions, file);
        }
        catch(GlueException ex) {
          throw new PipelineException(ex);
        }
      }
    }
  }
  
  /**
   * Read the queue extension configurations from disk.
   * 
   * @throws PipelineException
   *   If unable to read the extensions file.
   */ 
  private void 
  readQueueExtensions()
    throws PipelineException
  {
    synchronized(pQueueExtensions) {
      pQueueExtensions.clear();

      File file = new File(pQueueDir, "queue/etc/queue-extensions"); 
      if(file.isFile()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Reading Queue Extensions.");

	TreeMap<String,QueueExtensionConfig> exts = null;
        try {
          exts = 
            (TreeMap<String,QueueExtensionConfig>) 
            GlueDecoderImpl.decodeFile("QueueExtensions", file);
        }	
        catch(GlueException ex) {
          throw new PipelineException(ex);
        }

	if(exts != null)
	  pQueueExtensions.putAll(exts);
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Write per-host information to disk. <P> 
   * 
   * @throws PipelineException
   *   If unable to write the hosts file.
   */ 
  private void 
  writeHosts() 
    throws PipelineException
  {
    synchronized(pHosts) {
      File file = new File(pQueueDir, "queue/job-servers/hosts");
      if(file.exists()) {
	if(!file.delete())
	  throw new PipelineException
	    ("Unable to remove the old hosts file (" + file + ")!");
      }
      
      if(!pHosts.isEmpty()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Writing Hosts.");

        try {
          TreeMap<String,QueueHostInfo> infos = new TreeMap<String,QueueHostInfo>();
          for(QueueHost host : pHosts.values()) 
            infos.put(host.getName(), host.toInfo());
          GlueEncoderImpl.encodeFile("Hosts", infos, file);
        }
        catch(GlueException ex) {
          throw new PipelineException(ex);
        }
      }
    }
  }
  
  /**
   * Read the per-host information from disk. <P> 
   * 
   * @throws PipelineException
   *   If unable to read the hosts file.
   */ 
  private void 
  readHosts() 
    throws PipelineException
  {
    synchronized(pHosts) {
      pHosts.clear();

      File file = new File(pQueueDir, "queue/job-servers/hosts");
      if(file.isFile()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Reading Hosts.");

	TreeMap<String,QueueHostInfo> infos = null;
        try {
          infos = (TreeMap<String,QueueHostInfo>) GlueDecoderImpl.decodeFile("Hosts", file);
        }	
        catch(GlueException ex) {
          throw new PipelineException(ex);
        }

	if(infos == null) 
	  throw new IllegalStateException("The host info cannot be (null)!");
	
	for(QueueHostInfo qinfo : infos.values()) 
	  pHosts.put(qinfo.getName(), new QueueHost(qinfo));
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Write given interval of system resource samples for the Enabled hosts to disk. <P> 
   * 
   * @param timer
   *   The task timer.
   * 
   * @param stamp
   *   The timestamp of the end of the sample interval.
   * 
   * @param compact
   *   Whether to create a copy of the cache with no empty entries and write this instead
   *   of the original cache.
   * 
   * @throws PipelineException
   *   If unable to write the samples file.
   */ 
  private void 
  writeSamples
  (
   TaskTimer timer, 
   boolean compact
  ) 
    throws PipelineException
  {
    /* determine whether to make a compact copy of the live samples */ 
    TreeMap<String,ResourceSampleCache> samples = null;
    if(compact || hasAnyExtensionTasks(timer, new ResourceSamplesExtFactory()))
      samples = new TreeMap<String,ResourceSampleCache>();

    timer.aquire();
    synchronized(pSamples) { 
      synchronized(pSampleFileLock) { 
	timer.resume();

	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Writing Resource Samples..."); 

	File dir = new File(pQueueDir, "queue/job-servers/samples");
	timer.aquire();
	synchronized(pMakeDirLock) {
	  timer.resume();
	  if(!dir.isDirectory())
	    if(!dir.mkdirs()) 
	      throw new PipelineException
		("Unable to create the samples directory (" + dir + ")!");
	}
         
	long last = pLastSampleWritten.get();

	for(String hname : pSamples.keySet()) {    
	  ResourceSampleCache cache = pSamples.get(hname);
	  cache.pruneSamplesBefore(last);
	  if(cache.hasSamples()) {
	    ResourceSampleCache gcache = cache;
	    if(samples != null) {
	      gcache = cache.cloneDuring(new TimeInterval(last, cache.getLastTimeStamp()));
	      samples.put(hname, gcache);
	    }

	    File hdir = new File(dir, hname);
	    synchronized(pMakeDirLock) {
	      if(!hdir.isDirectory())
		if(!hdir.mkdirs()) 
		  throw new PipelineException
		    ("Unable to create the samples host directory (" + hdir + ")!");
	    }
	  
	    String fname = 
              (cache.getFirstTimeStamp() + ":" + 
               cache.getLastTimeStamp() + ":" + 
               cache.getNumSamples());

	    File file = new File(hdir, fname);
	    if(file.exists()) 
	      throw new PipelineException
		("Somehow the host resource samples file (" + file + ") already exists!"); 
	    
            try {
              GlueEncoderImpl.encodeFile("Samples", gcache, file);
            }
            catch(GlueException ex) {
              throw new PipelineException(ex);
            }
	  }
	}
      }
    }

    /* post-resource samples tasks */ 
    if((samples != null) && (!samples.isEmpty()))
      startExtensionTasks(timer, new ResourceSamplesExtFactory(samples));
  }

  /**
   * Read the resource sample blocks from disk for the given host collected within the 
   * given interval. 
   * 
   * @param timer
   *   The task timer.
   * 
   * @param hostname
   *   The fully resolved name of the host.
   * 
   * @param interval
   *   The time interval to read.
   * 
   * @param extraSamples
   *   The number of additional samples to allocate to the cache.
   * 
   * @return 
   *   The samples or <CODE>null</CODE> if no samples exist within the interval.
   */ 
  private ResourceSampleCache
  readSamples
  (
   TaskTimer timer, 
   String hostname, 
   TimeInterval interval, 
   int extraSamples
  ) 
  {
    timer.aquire();
    synchronized(pSampleFileLock) { 
      timer.resume();

      File dir = new File(pQueueDir, "queue/job-servers/samples/" + hostname);
      if(!dir.isDirectory()) 
	return null;
	
      long first = interval.getStartStamp(); 
      long last  = interval.getEndStamp();

      int numSamples = extraSamples; 
      TreeSet<File> sfiles = new TreeSet<File>();
      {
	File files[] = dir.listFiles(); 
	int wk;
	for(wk=files.length-1; wk>=0; wk--) {
	  File file = files[wk];
	  if(file.isFile()) {
	    try {
	      String parts[] = file.getName().split(":");
	      if(parts.length != 3) 
		throw new NumberFormatException(); 

	      Long fstamp  = new Long(parts[0]);
	      Long lstamp  = new Long(parts[1]);
	      Integer size = new Integer(parts[2]);

	      if((fstamp <= last) && (lstamp >= first)) {
		sfiles.add(file);
		numSamples += size;
	      }
	    }
	    catch(NumberFormatException ex) {
	      LogMgr.getInstance().log
		(LogMgr.Kind.Glu, LogMgr.Level.Severe,
		 "Ignoring illegally named resource sample file (" + file + ")!"); 
	    }	    
	  }
	}
      }
	      
      if(sfiles.isEmpty()) 
	return null;

      ResourceSampleCache tcache = new ResourceSampleCache(numSamples);
      for(File file : sfiles) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Reading Resource Samples: " + 
	   hostname + " [" + file.getName() + "]");

	ResourceSampleCache cache = null; 
        try {
          cache = (ResourceSampleCache) GlueDecoderImpl.decodeFile("Samples", file);
        }	
        catch(GlueException ex) {
          LogMgr.getInstance().log(LogMgr.Kind.Glu, LogMgr.Level.Warning, ex.getMessage()); 
        }
      
	if(cache != null) 
	  tcache.addAllSamples(cache); 
      }

      if(tcache.getNumSamples() == 0) 
	return null;
	
      return tcache;
    }
  }

  /**
   * Delete any out-of-date sample files.
   * 
   * @param timer
   *   The task timer.
   */ 
  private void 
  cleanupSamples
  (
   TaskTimer timer
  ) 
  {
    if(pLastSampleWritten.get() == 0L)
      return;
    
    timer.aquire();
    synchronized(pSampleFileLock) { 
      timer.resume();

      File sdir = new File(pQueueDir, "queue/job-servers/samples"); 
      if(!sdir.isDirectory()) 
	return;

      File sfiles[] = sdir.listFiles(); 
      int sk;
      for(sk=0; sk<sfiles.length; sk++) {
	File dir = sfiles[sk];
	if(dir.isDirectory()) {
	  File files[] = dir.listFiles(); 
	  int wk;
	  for(wk=0; wk<files.length; wk++) {
	    File file = files[wk];
	    try {
	      String parts[] = file.getName().split(":");
	      if(parts.length != 3) 
		throw new NumberFormatException();

	      Long stamp = new Long(parts[1]);
	      if((pLastSampleWritten.get() - stamp) > PackageInfo.sSampleCleanupInterval) {
		LogMgr.getInstance().log
		  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
		   "Deleting Resource Sample File: " + file);
		if(!file.delete()) 
		  LogMgr.getInstance().log
		    (LogMgr.Kind.Glu, LogMgr.Level.Severe,
		     "Unable to delete old resource sample file (" + file + ")!");
	      }
	    }
	    catch(NumberFormatException ex) {
	      LogMgr.getInstance().log
		(LogMgr.Kind.Glu, LogMgr.Level.Severe,
		 "Illegal resource sample file (" + file + ") encountered!");
	    }
	  }
	}
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the lock for reading/writing the QueueJob file for the given ID. 
   */ 
  private Object
  getJobFileLock
  (
   long jobID
  ) 
  {
    synchronized(pJobFileLocks) {
      Object lock = pJobFileLocks.get(jobID);
      if(lock == null) {
	lock = new Object();
	pJobFileLocks.put(jobID, lock);
      }

      return lock;
    }
  }

  /**
   * Write job to disk. <P> 
   * 
   * @param job
   *    The queue job.
   * 
   * @throws PipelineException
   *    If unable to write the job file.
   */ 
  private void
  writeJob
  (
   QueueJob job
  ) 
    throws PipelineException
  {
    long jobID = job.getJobID();
    Object lock = getJobFileLock(jobID);
    synchronized(lock) {
      File file = new File(pQueueDir, "queue/jobs/" + jobID);
      if(file.exists()) {
	throw new PipelineException
	  ("Somehow the job file (" + file + ") already exists!");
      }
      
      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Writing Job: " + jobID);
      
      try {
        GlueEncoderImpl.encodeFile("Job", job, file);
      }
      catch(GlueException ex) {
        throw new PipelineException(ex);
      }
    }
  }
  
  /**
   * Read the job from disk. <P> 
   * 
   * @param jobID
   *   The unique job identifier
   * 
   * @return 
   *   The queue job or <CODE>null</CODE> if none exists.
   * 
   * @throws PipelineException
   *   If unable to read the job file.
   */ 
  private QueueJob
  readJob
  (
   Long jobID
  ) 
    throws PipelineException
  {
    Object lock = getJobFileLock(jobID);
    synchronized(lock) {
      File file = new File(pQueueDir, "queue/jobs/" + jobID);
      if(file.isFile()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Reading Job: " + jobID);

        try {
          return ((QueueJob) GlueDecoderImpl.decodeFile("Job", file));
        }	
        catch(GlueException ex) {
          throw new PipelineException(ex);
        }
      }
      else {
	throw new PipelineException
	  ("Somehow for job (" + jobID + "), no job file (" + file + ") exists!");
      }
    }
  }

  /**
   * Delete the job file.
   * 
   * @throws PipelineException
   *   If unable to delete the file.
   */  
  private void
  deleteJobFile
  (
   Long jobID
  ) 
    throws PipelineException
  {
    Object lock = getJobFileLock(jobID);
    synchronized(lock) {
      File file = new File(pQueueDir, "queue/jobs/" + jobID); 

      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Deleting Job: " + jobID);

      if(!file.isFile()) 
	throw new PipelineException
	  ("The job file (" + file + ") was missing!");
      
      if(!file.delete()) 
	throw new PipelineException
	  ("Unable to delete the job file (" + file + ")!");
    }
  }



  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the lock for reading/writing the QueueJobInfo file for the given ID. 
   */ 
  private Object
  getJobInfoFileLock
  (
   long jobID
  ) 
  {
    synchronized(pJobInfoFileLocks) {
      Object lock = pJobInfoFileLocks.get(jobID);
      if(lock == null) {
	lock = new Object();
	pJobInfoFileLocks.put(jobID, lock);
      }

      return lock;
    }
  }

  /**
   * Write job information to disk. <P> 
   * 
   * @param info
   *    The job status information.
   * 
   * @throws PipelineException
   *    If unable to write the job info file.
   */ 
  private void 
  writeJobInfo
  (
   QueueJobInfo info
  ) 
    throws PipelineException
  {
    long jobID = info.getJobID();
    Object lock = getJobInfoFileLock(jobID);
    synchronized(lock) {
      File file = new File(pQueueDir, "queue/job-info/" + jobID);
      if(file.exists()) {
	if(!file.delete())
	  throw new PipelineException
	    ("Unable to remove the old job information file (" + file + ")!");
      }
      
      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Writing Job Information: " + jobID);
      
      try {
        GlueEncoderImpl.encodeFile("JobInfo", info, file);
      }
      catch(GlueException ex) {
        throw new PipelineException(ex);
      }
    }
  }

  /**
   * Read the job information from disk. <P> 
   * 
   * @param jobID
   *   The unique job identifier
   * 
   * @return 
   *   The job information or <CODE>null</CODE> if none exists.
   * 
   * @throws PipelineException
   *   If unable to read the job info file.
   */ 
  private QueueJobInfo
  readJobInfo
  (
   Long jobID
  ) 
    throws PipelineException
  {
    Object lock = getJobInfoFileLock(jobID);
    synchronized(lock) {
      File file = new File(pQueueDir, "queue/job-info/" + jobID);
      if(file.isFile()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Reading Job Information: " + jobID);

        try {
          return((QueueJobInfo) GlueDecoderImpl.decodeFile("JobInfo", file));
        }	
        catch(GlueException ex) {
          throw new PipelineException(ex);
        }
      }
      else {
	throw new PipelineException
	  ("Somehow for job (" + jobID + "), no job info file (" + file + ") exists!");
      }
    }
  }

  /**
   * Delete the job information file.
   * 
   * @throws PipelineException
   *   If unable to delete the file.
   */  
  private void
  deleteJobInfoFile
  (
   Long jobID
  ) 
    throws PipelineException
  {
    Object lock = getJobInfoFileLock(jobID);
    synchronized(lock) {
      File file = new File(pQueueDir, "queue/job-info/" + jobID); 

      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Deleting Job Information: " + jobID);

      if(!file.isFile()) 
	throw new PipelineException
	  ("The job information file (" + file + ") was missing!");
      
      if(!file.delete()) 
	throw new PipelineException
	  ("Unable to delete the job information file (" + file + ")!");
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the lock for reading/writing the QueueJobGroup file for the given ID. 
   */ 
  private Object
  getJobGroupFileLock
  (
   long groupID
  ) 
  {
    synchronized(pJobGroupFileLocks) {
      Object lock = pJobGroupFileLocks.get(groupID);
      if(lock == null) {
	lock = new Object();
	pJobGroupFileLocks.put(groupID, lock);
      }

      return lock;
    }
  }
  /**
   * Write job group to disk. <P> 
   * 
   * @param group
   *    The job group.
   * 
   * @throws PipelineException
   *    If unable to write the job group file.
   */ 
  private void
  writeJobGroup
  (
   QueueJobGroup group
  ) 
    throws PipelineException
  {
    long groupID = group.getGroupID();
    Object lock = getJobGroupFileLock(groupID);
    synchronized(lock) {
      File file = new File(pQueueDir, "queue/job-groups/" + groupID);
      if(file.exists()) {
	if(!file.delete())
	  throw new PipelineException
	    ("Unable to remove the old job group file (" + file + ")!");
      }
      
      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Writing Job Group: " + groupID);
      
      try {
        GlueEncoderImpl.encodeFile("JobGroup", group, file);
      }
      catch(GlueException ex) {
        throw new PipelineException(ex);
      }
    }
  }
  
  /**
   * Read the job group from disk. <P> 
   * 
   * @return 
   *   The job group or <CODE>null</CODE> if none exists.
   * 
   * @throws PipelineException
   *   If unable to read the job group file.
   */ 
  private QueueJobGroup
  readJobGroup
  (
   Long groupID
  ) 
    throws PipelineException
  {
    Object lock = getJobGroupFileLock(groupID);
    synchronized(lock) {
      File file = new File(pQueueDir, "queue/job-groups/" + groupID);
      if(file.isFile()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Reading Job Group: " + groupID);
	
        try {
          return ((QueueJobGroup) GlueDecoderImpl.decodeFile("JobGroup", file));
        }	
        catch(GlueException ex) {
          throw new PipelineException(ex);
        }
      }
    }

    return null;
  }

  /**
   * Delete the job group file.
   * 
   * @throws PipelineException
   *   If unable to delete the file.
   */  
  private void
  deleteJobGroupFile
  (
   Long groupID
  ) 
    throws PipelineException
  {
    Object lock = getJobGroupFileLock(groupID);
    synchronized(lock) {
      File file = new File(pQueueDir, "queue/job-groups/" + groupID);

      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Deleting Job Group: " + groupID);

      if(!file.isFile()) 
	throw new PipelineException
	  ("The job group file (" + file + ") was missing!");
      
      if(!file.delete()) 
	throw new PipelineException
	  ("Unable to delete the job group file (" + file + ")!");
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Comparing host order.
   */
  private 
  class HostOrderComparator
    implements Comparator<QueueHost>
  {
    public 
    HostOrderComparator() 
    {}

    /**
     * Compares its two arguments for order.
     */ 
    public int 	
    compare
    (
     QueueHost hostA, 
     QueueHost hostB
    )
    {
      return (hostA.getOrder() - hostB.getOrder());
    }      
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Runs the scheduler after selection schedules have been modified
   */
  private 
  class DemandSchedulerTask
    extends Thread
  {
    /*-- THREAD RUN ------------------------------------------------------------------------*/

    public void 
    run() 
    {
      TaskTimer timer = new TaskTimer("Demand Scheduler");
      doScheduler(timer);
      LogMgr.getInstance().logStage
	(LogMgr.Kind.Sch, LogMgr.Level.Fine,
	 timer); 
    }
  }
  
  /**
   * Collects resource stats from a subset of the enabled job servers.
   */
  private 
  class SubCollectorTask
    extends Thread
  {
    public 
    SubCollectorTask
    (
     int id
    ) 
    {
      super("QueueMgr:SubCollectorTask");

      pID = id;

      pNeedsCollect = new TreeSet<String>();
      pNeedsTotals = new TreeSet<String>();

      pSamples = new TreeMap<String,ResourceSample>();
      pOsTypes = new TreeMap<String,OsType>();
      pNumProcs = new TreeMap<String,Integer>();
      pTotalMemory = new TreeMap<String,Long>();
      pTotalDisk = new TreeMap<String,Long>();
    }


    /*-- ACCESSORS -------------------------------------------------------------------------*/

    public void 
    addCollect
    (
     String hname
    ) 
    {
      pNeedsCollect.add(hname);
    }

    public void 
    addTotals
    (
     String hname
    ) 
    {
      pNeedsTotals.add(hname);
    }

    public TreeMap<String,ResourceSample>
    getSamples() 
    {
      return pSamples;
    }

    public TreeMap<String,OsType>
    getOsTypes() 
    {
      return pOsTypes;
    }

    public TreeMap<String,Integer>
    getNumProcs() 
    {
      return pNumProcs;
    }

    public TreeMap<String,Long>
    getTotalMemory()
    {
      return pTotalMemory;
    }

    public TreeMap<String,Long>
    getTotalDisk()
    {
      return pTotalDisk;
    }


    /*-- THREAD RUN ------------------------------------------------------------------------*/

    public void 
    run() 
    {
      TaskTimer timer = new TaskTimer("SubCollector " + pID + " [Total]");
      for(String hname : pNeedsCollect) {
	timer.suspend();
	TaskTimer tm = new TaskTimer("SubCollector " + pID + " [" + hname + "]");
        JobMgrControlClient client = null;
	try {
          client = new JobMgrControlClient(hname);
	  ResourceSample sample = client.getResources();
	  pSamples.put(hname, sample);
	  
	  if(pNeedsTotals.contains(hname)) {
	    pOsTypes.put(hname, client.getOsType());
	    pNumProcs.put(hname, client.getNumProcessors());
	    pTotalMemory.put(hname, client.getTotalMemory());
	    pTotalDisk.put(hname, client.getTotalDisk());
	  }
	}
	catch(Exception ex) {
          tm.aquire();
          synchronized(pHosts) {
            tm.resume();
            QueueHost host = pHosts.get(hname);
            if(host != null) 
              setHostStatus(host, QueueHost.Status.Limbo);
          }

          String header = 
            ("Lost contact with the Job Manager (" + hname + ") while attempting " + 
             "to collect resource samples."); 
          String msg = header;
          if(!(ex instanceof PipelineException))
            msg = Exceptions.getFullMessage(header, ex);
          LogMgr.getInstance().log(LogMgr.Kind.Net, LogMgr.Level.Warning, msg); 
	}
        finally { 
          if(client != null)
	  client.disconnect();
        }
	LogMgr.getInstance().logSubStage
	  (LogMgr.Kind.Col, LogMgr.Level.Finest,
	   tm, timer);
      }
      LogMgr.getInstance().logStage
	(LogMgr.Kind.Col, LogMgr.Level.Finer,
         timer); 
    }


    /*-- INTERNALS -------------------------------------------------------------------------*/

    private int  pID; 
    private TaskTimer  pTimer; 

    private TreeSet<String>  pNeedsCollect; 
    private TreeSet<String>  pNeedsTotals; 

    private TreeMap<String,ResourceSample>  pSamples;
    private TreeMap<String,OsType>          pOsTypes; 
    private TreeMap<String,Integer>         pNumProcs; 
    private TreeMap<String,Long>            pTotalMemory; 
    private TreeMap<String,Long>            pTotalDisk; 
  }

  /**
   * Monitors the progress of a job from start to finish. 
   */
  private 
  class MonitorTask
    extends Thread
  {
    /* just wait on an existing job */ 
    public 
    MonitorTask
    (
     String hostname, 
     OsType os, 
     QueueJob job, 
     TreeSet<String> aquiredKeys
    ) 
    {
      this(hostname, os, job, aquiredKeys, null);
    }

    /* start a job on the given server and wait for it to finish */ 
    public 
    MonitorTask
    (
     String hostname, 
     OsType os, 
     QueueJob job, 
     TreeSet<String> acquiredKeys, 
     DoubleMap<OsType,String,String> envs
    ) 
    {
      super("QueueMgr:MonitorTask");

      pHostname     = hostname; 
      pHostOsType   = os; 
      pJob          = job; 
      pAcquiredKeys = acquiredKeys; 
      pCookedEnvs   = envs; 
    }


    /*-- ACCESSORS -------------------------------------------------------------------------*/

    public String
    getHostname() 
    {
      return pHostname; 
    }

    public long
    getJobID() 
    {
      return pJob.getJobID(); 
    }
    
    public MonitorTask
    remonitorClone() 
    {
      return new MonitorTask(pHostname, pHostOsType, pJob, pAcquiredKeys); 
    }
    
    
    
    /*-- THREAD RUN ------------------------------------------------------------------------*/

    public void 
    run() 
    {
      /* whether we've put the job into Limbo state and will resume monitoring it later */ 
      boolean remonitored = false;

      try {
        long jobID = getJobID();

        TaskTimer timer = new TaskTimer("Monitor - Job " + jobID);

        /* attempt to start the job on the selected server, 
	   no environment means the job has been started previously */  
        QueueJobInfo startedInfo = null;
        boolean balked = false;
        if(pCookedEnvs != null) {
          timer.suspend();
          TaskTimer tm = new TaskTimer("Monitor - Job " + jobID + " [Start]"); 

          JobMgrPlgControlClient client = new JobMgrPlgControlClient(pHostname); 
          boolean reported = false;
          try {
            /* perform pre-start file system tasks */ 
            preStartFileOps();

            /* start the job */ 
            try {
              client.jobStart(pJob, pCookedEnvs); 
            }
            catch(Exception ex2) { 
              tm.aquire();
              synchronized(pHosts) {
                tm.resume();
                QueueHost host = pHosts.get(pHostname);
                if(host != null) 
                  setHostStatus(host, QueueHost.Status.Limbo);
              }
              
              String header = 
                ("Lost contact with the Job Manager (" + pHostname + ") while attempting " + 
                 "to start the job (" + jobID + ").");
              String msg = header;
              if(!(ex2 instanceof PipelineException))
                msg = Exceptions.getFullMessage(header, ex2);
              LogMgr.getInstance().log(LogMgr.Kind.Net, LogMgr.Level.Warning, msg); 
              reported = true;
              throw ex2;
            }
 
            /* make a copy of the job information to hand to extensions */ 
            startedInfo = new QueueJobInfo(getJobInfo(tm, jobID));
          }
          catch (Exception ex) {
            if(!reported) {
              LogMgr.getInstance().log
                (LogMgr.Kind.Job, LogMgr.Level.Warning,
                 Exceptions.getFullMessage
                   ("Failed to start job (" + jobID + ") on Job Manager (" + pHostname + ")!",
                    ex));
            }
	  
            /* treat a failure to start as a preemption */ 
            tm.aquire(); 
            synchronized(pJobInfo) {
              tm.resume();

              QueueJobInfo info = pJobInfo.get(jobID);
              JobState prevState = info.preempted();
              pJobCounters.update(tm, prevState, info);
              try {
                writeJobInfo(info);
              }
              catch(PipelineException ex2) {
                LogMgr.getInstance().log
                  (LogMgr.Kind.Job, LogMgr.Level.Severe, 
                   ex2.getMessage()); 
              }
            }

            balked = true;
          }
          finally {
            if(client != null)
              client.disconnect();
          }

          LogMgr.getInstance().logSubStage
            (LogMgr.Kind.Job, LogMgr.Level.Finer, 
             tm, timer);
        }
        
        /* resume waiting on a previously started job */ 
        else {
          timer.suspend();
          TaskTimer tm = new TaskTimer("Monitor - Job " + jobID + " [Restart]"); 

          tm.aquire(); 
          synchronized(pJobInfo) {
            tm.resume();
            
            QueueJobInfo info = pJobInfo.get(jobID);
            JobState prevState = info.restarted();
            pJobCounters.update(tm, prevState, info);
            try {
              writeJobInfo(info);
            }
            catch(PipelineException ex) {
              LogMgr.getInstance().log
                (LogMgr.Kind.Job, LogMgr.Level.Severe, 
                 ex.getMessage()); 
            }
          }

          LogMgr.getInstance().logSubStage
            (LogMgr.Kind.Job, LogMgr.Level.Finer, 
             tm, timer);
        }

        /* post-started tasks */ 
        if(startedInfo != null) 
          startExtensionTasks(timer, new JobStartedExtFactory(pJob, startedInfo));
      
        /* if job was successfully started... */  
        QueueJobInfo finishedInfo = null;
        boolean preempted = false;
        if(!balked) {
          timer.suspend();
          TaskTimer tm = new TaskTimer("Monitor - Job " + jobID + " [Wait]"); 

          /* wait for the job to finish and collect the results */ 
          tm.aquire(); 
          QueueJobResults results = null;
          Boolean isTerminal = null;
          {
            JobMgrControlClient client = new JobMgrControlClient(pHostname);
            try {
              /* wait for the job to finish */ 
              try {
                results = client.jobWait(jobID);
              }
              catch(Exception ex2) { 
                synchronized(pHosts) {
                  QueueHost host = pHosts.get(pHostname);
                  if(host != null) 
                    setHostStatus(host, QueueHost.Status.Limbo);
                }

                String header = 
                  ("Lost contact with the Job Manager (" + pHostname + ") while waiting " + 
                   "on the results of executing the job (" + jobID + ")."); 
                String msg = header;
                if(!(ex2 instanceof PipelineException))
                  msg = Exceptions.getFullMessage(header, ex2);
                LogMgr.getInstance().log(LogMgr.Kind.Net, LogMgr.Level.Warning, msg); 

                /* if the job manager is already shutdown or missing,
                     then the job must die too */
                isTerminal = true;
                synchronized(pHosts) {
                  QueueHost host = pHosts.get(pHostname);
                  if(host != null) {
                    switch(host.getStatus()) {
                    case Enabled:
                    case Disabled:
                    case Limbo:
                      isTerminal = false; 
                    }
                  }
                }

                /* if the job manager might still be alive, 
                     don't give up on the job just yet.. */ 
                if(!isTerminal) {
                  remonitorJob(this); 
                  remonitored = true;
                }
              }

              /* perform post-completion file system tasks */ 
              if(!remonitored) 
                postFinishFileOps();
            }
            catch(Exception ex) {
              LogMgr.getInstance().log
                (LogMgr.Kind.Job, LogMgr.Level.Warning,
                 Exceptions.getFullMessage
                   ("Failed to wait for the results of the executing job (" + jobID + ")!", 
                    ex));
            }
            finally {
              client.disconnect();
            }
          }
          tm.resume();
	
          /* update job information */
          QueueJobInfo info = getJobInfo(tm, jobID); 
          if(info != null) {
            try {            
              switch(info.getState()) {
              case Preempted: 
                preempted = true;
                break;
              
              default:
                {
                  JobState prevState = null;
                  if(isTerminal != null) {
                    if(isTerminal) {
                      LogMgr.getInstance().log
                        (LogMgr.Kind.Net, LogMgr.Level.Warning, 
                         "Treating job (" + jobID + ") as Failed because the Job Manager " + 
                         "(" + pHostname + ") was manually Shutdown before establishing " + 
                         "whether the job had completed."); 
                      
                      prevState = info.exited(null);
                    }
                    else {
                      prevState = info.limbo();
                    }
                  }
                  else {
                    prevState = info.exited(results);
                  }
                  pJobCounters.update(tm, prevState, info);
                  if(!remonitored) 
                    finishedInfo = new QueueJobInfo(info);
                }
              }
              writeJobInfo(info);
            }
            catch (PipelineException ex) {
              LogMgr.getInstance().log
                (LogMgr.Kind.Job, LogMgr.Level.Severe,
                 ex.getMessage()); 
            }	
          }
          else {
            LogMgr.getInstance().log
              (LogMgr.Kind.Job, LogMgr.Level.Severe,
               "The job (" + jobID + ") has " + 
               (remonitored ? " become in Limbo" : "completed") + 
               ", but somehow there was no QueueJobInfo entry to update!"); 
          }
	
          LogMgr.getInstance().logSubStage
            (LogMgr.Kind.Job, LogMgr.Level.Finer, 
             tm, timer);
        }

        /* abort eary since we are not done monitoring the job yet... */ 
        if(remonitored) 
          return;

        /* clean up... */ 
        {
          timer.suspend();
          TaskTimer tm = new TaskTimer("Monitor - Job " + jobID + " [Cleanup]"); 

          /* release any license keys */ 
          tm.aquire();
          synchronized(pLicenseKeys) {
            tm.resume();

            for(String kname : pAcquiredKeys) {
              LicenseKey key = pLicenseKeys.get(kname);
              if(key != null) 
                key.release(pHostname);
            }
          }
	
          /* release any ramp-up holds */ 
          tm.aquire();
          synchronized(pHosts) {
            tm.resume();

            QueueHost host = pHosts.get(pHostname);
            if(host != null)
              host.cancelHold(jobID);
          }      
	
          /* if the job was preempted, 
	     clean up the obsolete data files on the jobs server */ 
          if(preempted) {
            tm.aquire();
            JobMgrControlClient client = new JobMgrControlClient(pHostname);	
            try {
              client.cleanupPreemptedResources(jobID);
            }
            catch (Exception ex) {
              LogMgr.getInstance().log
                (LogMgr.Kind.Job, LogMgr.Level.Severe,
                 ex.getMessage()); 
            }
            finally {
              if(client != null)
                client.disconnect();
            }
            tm.resume();
          }

          /* if balked or preempted, 
	     put job back on the list of jobs waiting to be run */ 
          if(balked || preempted)
            pWaiting.add(jobID);

          LogMgr.getInstance().logSubStage
            (LogMgr.Kind.Job, LogMgr.Level.Finer, 
             tm, timer);	
        }

        /* post-execution tasks */ 
        if(balked) 
          startExtensionTasks(timer, new JobBalkedExtFactory(pJob, pHostname));
        else if(finishedInfo != null) 
          startExtensionTasks(timer, new JobFinishedExtFactory(pJob, finishedInfo));

        LogMgr.getInstance().logStage
          (LogMgr.Kind.Job, LogMgr.Level.Fine,
           timer); 
      }
      finally {
        if(!remonitored) 
          unmonitorJob(this); 
      }
    }

    /*-- HELPERS ---------------------------------------------------------------------------*/

    /**
     * Perform pre-start file system tasks.
     */ 
    private void 
    preStartFileOps()
      throws PipelineException
    {
      long jobID = getJobID();
      
      ActionAgenda agenda = pJob.getActionAgenda();
      String author = agenda.getNodeID().getAuthor();
      Path wpath = new Path(PackageInfo.sProdPath, agenda.getNodeID().getWorkingParent());
      Path tpath = new Path(PackageInfo.sTargetPath, Long.toString(jobID));
      Map<String,String> env = System.getenv();

      /* create the windows job target directory */ 
      switch(pHostOsType) {
      case Windows:
        if(!tpath.toFile().isDirectory()) {
          ArrayList<String> args = new ArrayList<String>();
          args.add("--parents");
          args.add("--mode=755");
          args.add(tpath.toOsString()); 

          SubProcessLight proc = 
            new SubProcessLight("MakeWinTargetDir", "mkdir", 
                                args, env, PackageInfo.sTempPath.toFile()); 
          try {
            proc.start();
            proc.join();
            if(!proc.wasSuccessful()) 
              throw new PipelineException
                ("Unable to create the Windows job target directory (" + tpath + "):\n\n  " + 
                 proc.getStdErr());
          }
          catch(InterruptedException ex) {
            throw new PipelineException
              ("Interrupted while creating Windows job target directory (" + tpath + ")!");
          }
        }
      }

      /* make sure the target directory exists */ 
      if(!wpath.toFile().isDirectory()) {
        ArrayList<String> args = new ArrayList<String>();
        args.add("--parents");
        args.add("--mode=755");
        args.add(wpath.toOsString());
        
        SubProcessLight proc = 
          new SubProcessLight(agenda.getNodeID().getAuthor(), 
                              "MakeWorkingDir", "mkdir", 
                              args, env, PackageInfo.sTempPath.toFile()); 
        try {
          proc.start();
          proc.join();
          if(!proc.wasSuccessful()) 
            throw new PipelineException
              ("Unable to create the target working area directory (" + wpath + "):\n\n  " + 
               proc.getStdErr());
        }
        catch(InterruptedException ex) {
          throw new PipelineException
            ("Interrupted while creating target working area directory (" + wpath + ")!");
        }
      }
      
      /* remove the target primary and secondary files */ 
      {
        ArrayList<String> preOpts = new ArrayList<String>();
        preOpts.add("--force");
        
        ArrayList<String> args = new ArrayList<String>();
        for(Path target : agenda.getPrimaryTarget().getPaths()) 
          args.add(target.toOsString());
        
        for(FileSeq fseq : agenda.getSecondaryTargets()) {
          for(Path target : fseq.getPaths())
            args.add(target.toOsString()); 
        }
        
        if(!args.isEmpty()) {
          LinkedList<SubProcessLight> procs = 
            SubProcessLight.createMultiSubProcess
            (agenda.getNodeID().getAuthor(), 
             "RemoveTargets", "rm", preOpts, args, env, wpath.toFile());

          try {
            for(SubProcessLight proc : procs) {
              proc.start();
              proc.join();
              if(!proc.wasSuccessful()) 
                throw new PipelineException
                  ("Unable to remove the target files of job (" + jobID + "):\n\n" + 
                   "  " + proc.getStdErr());	
            }
          }
          catch(InterruptedException ex) {
            throw new PipelineException
              ("Interrupted while removing the target files of job (" + jobID + ")!");
          }
        }
      }

      /* create symlinks for all job target files in the working directory which point
         to the (currently non-existant) files in the the windows job target directory */ 
      switch(pHostOsType) {
      case Windows:
        {
          ArrayList<String> targets = new ArrayList<String>();
          {
            for(Path target : agenda.getPrimaryTarget().getPaths()) {
              Path path = new Path(tpath, target); 
              targets.add(path.toOsString()); 
            }
            
            for(FileSeq fseq : agenda.getSecondaryTargets()) {
              for(Path target : fseq.getPaths()) {
                Path path = new Path(tpath, target); 
                targets.add(path.toOsString()); 
              }
            }
          }

          for(String target : targets) {
            ArrayList<String> args = new ArrayList<String>();
            args.add("-s");
            args.add(target);
            args.add(".");

            SubProcessLight proc = 
              new SubProcessLight(agenda.getNodeID().getAuthor(), 
                                  "WinTarget-Symlink", "ln", args, env, wpath.toFile());
            try {
	      proc.start();
	      proc.join();
	      if(!proc.wasSuccessful()) 
		throw new PipelineException
		  ("Unable to create a symbolic link to the Windows target file " + 
                   "(" + target + ") for the job (" + jobID + "):\n\n" + 
		   proc.getStdErr());
            }
            catch(InterruptedException ex) {
              throw new PipelineException
                ("Interrupted while creating a symbolic link to the Windows target file " + 
                 "(" + target + ") for the job (" + jobID + ")!"); 
            }
          }
        }
      }
    }
    
    /**
     * Perform post-completion file system tasks.
     */ 
    private void 
    postFinishFileOps()
      throws PipelineException
    {
      long jobID = getJobID();

      ActionAgenda agenda = pJob.getActionAgenda();
      String author = agenda.getNodeID().getAuthor();
      Path wpath = new Path(PackageInfo.sProdPath, agenda.getNodeID().getWorkingParent());
      Path tpath = new Path(PackageInfo.sTargetPath, Long.toString(jobID));
      Map<String,String> env = System.getenv();

      /* replace the sylinks from the working directory to the windows job target directory */
      switch(pHostOsType) {
      case Windows:
        /* remove the symlinks */ 
        {
	  ArrayList<String> preOpts = new ArrayList<String>();
	  preOpts.add("--force");
          
          ArrayList<String> args = new ArrayList<String>();
          for(Path target : agenda.getPrimaryTarget().getPaths())
            args.add(target.toOsString()); 
          
          for(FileSeq fseq : agenda.getSecondaryTargets()) {
            for(Path target : fseq.getPaths()) 
              args.add(target.toOsString()); 
          }
          
          LinkedList<SubProcessLight> procs = 
            SubProcessLight.createMultiSubProcess
            (agenda.getNodeID().getAuthor(), 
             "WinTarget-RemoveLinks", "rm", preOpts, args, env, wpath.toFile());

	  try {
	    for(SubProcessLight proc : procs) {
	      proc.start();
	      proc.join();
	      if(!proc.wasSuccessful()) 
		throw new PipelineException
		  ("Unable to remove the symbolic links to the Windows target files " + 
                   "for the job (" + jobID + "):\n\n" + 
		   proc.getStdErr());	
	    }
	  }
	  catch(InterruptedException ex) {
	    throw new PipelineException
	      ("Interrupted while removing the symbolic links to the Windows target files " + 
               "for the job (" + jobID + ")!");
	  }
        }

        /* copy the target files from the Windows temporary directory */ 
        {
	  ArrayList<String> preOpts = new ArrayList<String>();
	  preOpts.add("--remove-destination");
	  preOpts.add("--target-directory=" + wpath.toOsString());

	  ArrayList<Path> missing = new ArrayList<Path>();
	  ArrayList<String> args = new ArrayList<String>();
          for(Path target : agenda.getPrimaryTarget().getPaths()) {
            Path path = new Path(tpath, target); 
            if(path.toFile().isFile())
              args.add(path.toOsString()); 
            else 
              missing.add(path); 
          }
          
          for(FileSeq fseq : agenda.getSecondaryTargets()) {
            for(Path target : fseq.getPaths()) {
              Path path = new Path(tpath, target); 
              if(path.toFile().isFile())
                args.add(path.toOsString()); 
              else 
                missing.add(path); 
            }
          }          

          if(!args.isEmpty()) {
            LinkedList<SubProcessLight> procs = 
              SubProcessLight.createMultiSubProcess
              (agenda.getNodeID().getAuthor(), 
               "WinTarget-CopyTargets", "cp", preOpts, args, env, tpath.toFile());

            try {
              for(SubProcessLight proc : procs) {
                proc.start();
                proc.join();
                if(!proc.wasSuccessful()) 
                  throw new PipelineException
                    ("Unable to copy the Windows target files to the working area " + 
                     "directory for job (" + jobID + "):\n\n" + 
                     proc.getStdErr());	
              }
            }
            catch(InterruptedException ex) {
              throw new PipelineException
                ("Interrupted while copying the Windows target files to the working area " + 
                 "directory for job (" + jobID + ")!"); 
            }
          }

          /* try again after letting the NFS cache expire... */ 
          if(!missing.isEmpty()) {
            long nap = pNfsCacheInterval.get();
            if(nap > 0) {
              LogMgr.getInstance().logAndFlush
                (LogMgr.Kind.Sub, LogMgr.Level.Fine,
                 "WinTarget-CopyTargets: Sleeping for (" + nap + ") msec...");
              try {
                Thread.sleep(nap+100L);  /* a little extra for good measure */
              }
              catch(InterruptedException ex) {
              }
              
              args.clear(); 
              for(Path path : missing) { 
                if(path.toFile().isFile())
                  args.add(path.toOsString()); 
              }

              if(!args.isEmpty()) {
                LinkedList<SubProcessLight> procs = 
                  SubProcessLight.createMultiSubProcess
                  (agenda.getNodeID().getAuthor(), 
                   "WinTarget-CopyTargets[2]", "cp", preOpts, args, env, tpath.toFile());
                
                try {
                  for(SubProcessLight proc : procs) {
                    proc.start();
                    proc.join();
                    if(!proc.wasSuccessful()) 
                      throw new PipelineException
                        ("Unable to copy the Windows target files to the working area " + 
                         "directory for job (" + jobID + "):\n\n" + 
                         proc.getStdErr());	
                  }
                }
                catch(InterruptedException ex) {
                  throw new PipelineException
                    ("Interrupted while copying the Windows target files to the working " + 
                     "area directory for job (" + jobID + ")!"); 
                }
              }
            }
          }
        }
      }

      /* make any existing target primary and secondary files read-only */ 
      {
        ArrayList<String> preOpts = new ArrayList<String>();
        preOpts.add("uga-w");
          
        ArrayList<Path> missing = new ArrayList<Path>();
        ArrayList<String> args = new ArrayList<String>();
        for(Path target : agenda.getPrimaryTarget().getPaths()) {
          Path path = new Path(wpath, target); 
          if(path.toFile().isFile()) 
            args.add(target.toOsString()); 
          else 
            missing.add(path); 
        }
        
        for(FileSeq fseq : agenda.getSecondaryTargets()) {
          for(Path target : fseq.getPaths()) {
            Path path = new Path(wpath, target); 
            if(path.toFile().isFile()) 
              args.add(target.toOsString()); 
            else 
              missing.add(path); 
          }
        }
        
        if(!args.isEmpty()) {
          LinkedList<SubProcessLight> procs = 
            SubProcessLight.createMultiSubProcess
            (agenda.getNodeID().getAuthor(), 
             "ReadOnlyTargets", "chmod", preOpts, args, env, wpath.toFile());
          
          try {
            for(SubProcessLight proc : procs) {
              proc.start();
              proc.join();
              if(!proc.wasSuccessful()) 
                throw new PipelineException
                  ("Unable to make the target files of job (" + jobID + ") " + 
                   "read-only:\n\n" + 
                   "  " + proc.getStdErr());	
            }
          }
          catch(InterruptedException ex) {
            throw new PipelineException
              ("Interrupted while making the target files of job (" + jobID + ") " + 
               "read-only!");
          }
        }

        /* try again after letting the NFS cache expire... */ 
        switch(pHostOsType) {
        case Windows:
          /* no need to try again on Windows since the WinTarget-CopyTargets task above
             would have already found any files being hidden by the NFS cache */ 
          break;

        default:
          if(!missing.isEmpty()) {
            long nap = pNfsCacheInterval.get();
            if(nap > 0) {
              LogMgr.getInstance().logAndFlush
                (LogMgr.Kind.Sub, LogMgr.Level.Fine,
                 "ReadOnlyTargets: Sleeping for (" + nap + ") msec...");
              try {
                Thread.sleep(nap+100L);  /* a little extra for good measure */
              }
              catch(InterruptedException ex) {
              }
              
              args.clear(); 
              for(Path path : missing) { 
                if(path.toFile().isFile())
                  args.add(path.toOsString()); 
              }
              
              if(!args.isEmpty()) {
                LinkedList<SubProcessLight> procs = 
                  SubProcessLight.createMultiSubProcess
                  (agenda.getNodeID().getAuthor(), 
                   "ReadOnlyTargets[2]", "chmod", preOpts, args, env, wpath.toFile());
                
                try {
                  for(SubProcessLight proc : procs) {
                    proc.start();
                    proc.join();
                    if(!proc.wasSuccessful()) 
                      throw new PipelineException
                        ("Unable to make the target files of job (" + jobID + ") " + 
                         "read-only:\n\n" + 
                         "  " + proc.getStdErr());	
                  }
                }
                catch(InterruptedException ex) {
                  throw new PipelineException
                    ("Interrupted while making the target files of job (" + jobID + ") " + 
                     "read-only!");
                }
              }
            }
          }
        }
      }
      
      /* remove the windows job target directory (and its contents) */ 
      switch(pHostOsType) {
      case Windows:
        {
          ArrayList<String> args = new ArrayList<String>();
          args.add("--force");
          args.add("--recursive");
          args.add(tpath.toOsString());
          
          SubProcessLight proc = 
            new SubProcessLight("RemoveWinTargetDir", "rm", 
                                args, env, PackageInfo.sTempPath.toFile()); 
          try {
            proc.start();
            proc.join();
            if(!proc.wasSuccessful()) 
              throw new PipelineException
                ("Unable to remove the Windows job target directory (" + tpath + "):\n\n  " + 
                 proc.getStdErr());
          }
          catch(InterruptedException ex) {
            throw new PipelineException
              ("Interrupted while removing Windows job target directory (" + tpath + ")!");
          }
        }
      }
    }


    /*-- INTERNALS -------------------------------------------------------------------------*/

    private String                           pHostname; 
    private OsType                           pHostOsType; 
    private QueueJob                         pJob; 
    private TreeSet<String>                  pAcquiredKeys; 
    private DoubleMap<OsType,String,String>  pCookedEnvs; 
  }


  /**
   * Kills the given job running on a job server.
   */
  private 
  class KillTask
    extends Thread
  {
    public 
    KillTask
    (
     String hostname, 
     long jobID
    ) 
    {      
      super("QueueMgr:KillTask");

      pHostname = hostname; 
      pJobID    = jobID;
    }

    @Override
    public void 
    run() 
    {
      JobMgrControlClient client = null;
      try {
	client = new JobMgrControlClient(pHostname);	
	client.jobKill(pJobID);
      }
      catch (Exception ex) {
        synchronized(pHosts) {
          QueueHost host = pHosts.get(pHostname);
          if(host != null) 
            setHostStatus(host, QueueHost.Status.Limbo);
        }

        String header = 
          ("Lost contact with the Job Manager (" + pHostname + ") while attempting " + 
           "to direct it to kill the job (" + pJobID + ")."); 
        String msg = header;
        if(!(ex instanceof PipelineException))
          msg = Exceptions.getFullMessage(header, ex);
        LogMgr.getInstance().log(LogMgr.Kind.Net, LogMgr.Level.Warning, msg); 
      }
      finally { 
	if(client != null)
	  client.disconnect();
      }
    }

    private String   pHostname; 
    private long     pJobID; 
  }

  /**
   * Tell the job servers to cleanup any resources associated with dead jobs.
   */
  private 
  class CleanupJobResourcesTask
    extends Thread
  {
    public 
    CleanupJobResourcesTask
    (
     TreeSet<Long> live
    ) 
    {      
      super("QueueMgr:CleanupJobResourcesTask");

      pJobIDs = live;
    }

    public void 
    run() 
    {
      TreeSet<String> hnames = new TreeSet<String>();
      synchronized(pHosts) {
	for(String hname : pHosts.keySet()) {
	  QueueHost host = pHosts.get(hname);
	  switch(host.getStatus()) {
	  case Disabled:
	  case Enabled:
	    hnames.add(hname);
	  }
	}
      }

      for(String hname : hnames) {	  
	JobMgrControlClient client = null;
	try {
	  client = new JobMgrControlClient(hname);	
	  client.cleanupResources(pJobIDs);
	}
	catch(Exception ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Job, LogMgr.Level.Severe,
	     ex.getMessage()); 
	}
	finally {
	  if(client != null)
	    client.disconnect();
	}
      }
    }

    private TreeSet<Long>  pJobIDs; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The maximum number of samples cached in memory.
   */ 
  private static final int  sCollectedSamples = 960;   /* 4-hours */ 

  /**
   * The interval between attempts to automatically re-Enable previously Limbo job servers. 
   */ 
  private static final long  sReenableInterval = 900000L;  /* 15-minutes */ 

  /**
   * The number of dispatcher cycles between garbage collection of jobs.
   */ 
  private static final int  sGarbageCollectAfter = 600;  /* 10-minutes */ 

  /**
   * The minimum time a cycle of the scheduler loop should take (in milliseconds).
   */ 
  private static final long  sSchedulerInterval = 300000L;  /* 5-minutes */ 

  /**
   * The time (in milliseconds) between reports of the JVM heap statistics.
   */ 
  private static long  sHeapStatsInterval = 900000L;  /* 15-minutes */



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent queue manager server. 
   */
  private QueueMgrServer  pServer; 

  /**
   * Whether to shutdown all job servers before exiting.
   */ 
  private AtomicBoolean  pShutdownJobMgrs; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The common back-end directories.
   * 
   * Since the queue manager should always be run on a Unix system, these variables are always
   * initialized to Unix specific paths.
   */
  private File pQueueDir;

  /**
   * The file system directory creation lock.
   */
  private Object pMakeDirLock;
 
  /**
   * Whether to ignore existing lock files.
   */
  private boolean  pRebuild; 


  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The combined work groups and administrative privileges.
   */ 
  private AdminPrivileges  pAdminPrivileges; 

  /**
   * The network interface to the <B>plmaster</B>(1) daemon.
   */ 
  private MasterMgrClient  pMasterMgrClient;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The cached table of license keys indexed by license key name.
   * 
   * Access to this field should be protected by a synchronized block.
   */ 
  private TreeMap<String,LicenseKey>  pLicenseKeys; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The cached table of hardware keys indexed by hardware key name.
   * 
   * Access to this field should be protected by a synchronized block.
   */ 
  private TreeMap<String,HardwareKey>  pHardwareKeys; 
  
  /**
   * The cached table of hardware groups indexed by group name.
   * 
   * Access to this field should be protected by a synchronized block.
   */ 
  private TreeMap<String,HardwareGroup>  pHardwareGroups; 

  /**
   * Whether there have been any changes to hardware keys or groups since the last time
   * hardware profiles where regenerated. 
   */ 
  private AtomicBoolean pHardwareChanged; 

  /**
   * A copy of names of the pHardwareKeys used without locking by the dispatcher.  
   * The copy is updated at the beginning of the dispatcher cycle if pHardwareChanged is 
   * (true). 
   * 
   * No locking is required, since this field is only accessed by the Dispatcher thread.
   */ 
  private TreeSet<String>  pDispHardwareKeyNames; 

  /**
   * A deep copy of pHardwareGroups used without locking by the dispatcher.  The copy is
   * updated at the beginning of the dispatcher cycle if pHardwareChanged is (true). 
   * 
   * No locking is required, since this field is only accessed by the Dispatcher thread.
   */ 
  private TreeMap<String,HardwareGroup>  pDispHardwareGroups; 

  /**
   * Whether each of the unique combinations of hardware keys required by jobs are 
   * satisfied by each hardware group.
   * 
   * No locking is required, since this field is only accessed by the Dispatcher thread.
   */ 
  private TreeMap<Flags,HardwareProfile>  pHardwareProfiles;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The cached table of selection keys indexed by selection key name.
   * 
   * Access to this field should be protected by a synchronized block.
   */ 
  private TreeMap<String,SelectionKey>  pSelectionKeys; 

  /**
   * The cached table of selection groups indexed by group name.
   * 
   * Access to this field should be protected by a synchronized block.
   */ 
  private TreeMap<String,SelectionGroup>  pSelectionGroups; 

  /**
   * The cached table of selection groups indexed by schedule name.
   * 
   * Access to this field should be protected by a synchronized block.
   */ 
  private TreeMap<String,SelectionSchedule>  pSelectionSchedules; 

  /**
   * Whether there have been any changes to selection keys or groups since the last time
   * selection profiles where regenerated. 
   */ 
  private AtomicBoolean pSelectionChanged; 

  /**
   * A copy of names of the pSelectionKeys used without locking by the dispatcher.  
   * The copy is updated at the beginning of the dispatcher cycle if pSelectionChanged is 
   * (true). 
   * 
   * No locking is required, since this field is only accessed by the Dispatcher thread.
   */ 
  private TreeSet<String>  pDispSelectionKeyNames; 

  /**
   * A deep copy of pSelectionGroups used without locking by the dispatcher.  The copy is
   * updated at the beginning of the dispatcher cycle if pSelectionChanged is (true). 
   * 
   * No locking is required, since this field is only accessed by the Dispatcher thread.
   */ 
  private TreeMap<String,SelectionGroup>  pDispSelectionGroups; 

  /**
   * Whether each of the unique combinations of selection keys required by jobs are 
   * satisfied by each selection group.
   * 
   * No locking is required, since this field is only accessed by the Dispatcher thread.
   */ 
  private TreeMap<Flags,SelectionProfile>  pSelectionProfiles;


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The table of the queue extensions configurations.
   * 
   * Access to this field should be protected by a synchronized block.
   */ 
  private TreeMap<String,QueueExtensionConfig>  pQueueExtensions; 


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The cached toolsets indexed by toolset name and operating system. <P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */
  private TreeMap<String,TreeMap<OsType,Toolset>>  pToolsets;
  

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Pending changes to per-host status and other user modifiable properties 
   * indexed by by fully resolved host name. <P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */ 
  private TreeMap<String,QueueHostMod>  pHostsMod; 

  /**
   * Pending changes to per-host OS type 
   * indexed by by fully resolved host name.<P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */ 
  private TreeMap<String,OsType>  pOsTypeChanges; 
  
  /**
   * Pending changes to per-host number of processors 
   * indexed by by fully resolved host name.<P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */ 
  private TreeMap<String,Integer>  pNumProcChanges; 
  
  /**
   * Pending changes to per-host total memory size
   * indexed by by fully resolved host name.<P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */ 
  private TreeMap<String,Long>  pTotalMemoryChanges; 
  
  /**
   * Pending changes to per-host total disk size
   * indexed by by fully resolved host name.<P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */ 
  private TreeMap<String,Long>  pTotalDiskChanges; 
  

  /**
   * The per-host information indexed by fully resolved host name. <P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */
  private TreeMap<String,QueueHost>  pHosts; 

  /**
   * An reusable array containing the sorted (according to Order) QueueHost entries from 
   * the pHosts table.
   * 
   * The host entries are updated at each cylce of the dispatcher and resorted.  If the 
   * number of hosts is larger than the current size of the array, it resized automatically.
   * 
   * No locking is required, since this field is only accessed by the Dispatcher thread.
   */ 
  private QueueHost[]  pOrderedHosts; 

  /**
   * Last read-only copy of per-host status information indexed by fully resolved host name. 
   * <P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */
  private TreeMap<String,QueueHostInfo>  pHostsInfo; 
  

  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The maximum number of job servers per collection sub-thread.
   */ 
  private AtomicInteger pCollectorBatchSize; 

  /**
   * Fixed size ring buffers containing the last N resource samples
   * indexed by by fully resolved host name.<P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */ 
  private TreeMap<String,ResourceSampleCache>  pSamples; 

  /**
   * The timestamp of when the last set of resource samples was written to disk. <P> 
   * 
   * Will have a value of (0L) until the first sample is collected.
   */ 
  private AtomicLong  pLastSampleWritten;

  /**
   * A lock which protects resource sample files from simulatenous reads and writes.
   */ 
  private Object  pSampleFileLock; 



  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The minimum time to wait before attempting a NFS directory attribute lookup operation
   * after a file in the directory has been created by another host on the network 
   * (in milliseconds).  This should be set to the same value as the NFS (acdirmax) 
   * mount option for the root production directory on the host running the Queue Manager.
   */ 
  private AtomicLong  pNfsCacheInterval; 

  /**
   * The minimum time a cycle of the dispatcher loop should take (in milliseconds).
   */ 
  private AtomicLong  pDispatcherInterval; 

  /**
   * The IDs of jobs which should be preempted as soon as possible. 
   * 
   * No locking is required.
   */
  private LinkedBlockingDeque<Long>  pPreemptList;
  
  /**
   * The IDs of jobs which should be killed as soon as possible. 
   * 
   * No locking is required.
   */
  private LinkedBlockingDeque<Long>  pHitList;
  
  /**
   * The IDs of Queued/Preempted jobs which should be Paused at the next opportunity.
   * 
   * Access to this field should be protected by a synchronized block.
   */
  private TreeSet<Long>  pPause;

  /**
   * The IDs of Paused jobs which should be Resumed at the next opportunity.
   * 
   * Access to this field should be protected by a synchronized block.
   */
  private TreeSet<Long>  pResume;

  /**
   * The IDs of jobs waiting on one or more source (upstream) jobs to complete before they 
   * can be added to the ready queue.  This list also contains the IDs of jobs newly 
   * submitted to the queue which may not have any source nodes. <P> 
   * 
   * No locking is required.
   */ 
  private LinkedBlockingDeque<Long>  pWaiting;

  /**
   * The profiles of the jobs which are ready to be run indexed by their job ID.  If the value
   * for a given key is (null) then it will means the job requirements have changed and the 
   * job should be reprofiled at the start of the dispatcher cycle.<P> 
   * 
   * If a ready job has any source (upstream) jobs, they all must have a JobState of 
   * Finished before the job will be added to this table. <P> 
   * 
   * No locking is required, since this field is only accessed by the Dispatcher thread.
   */ 
  private TreeMap<Long,JobProfile>  pReady;

  /**
   * An array of reusable objects which encapsulates all factors which contribute to the 
   * ranking of jobs with respect to a particular slot. <P> 
   * 
   * This array and the JobRank instances in it are updated, sorted and recycled for the
   * dispatch of each slot.  If there are more quailified ready jobs than elements in this
   * array, it is resized copying all existing elements over to the new array.  If a (null)
   * element is encountered, a new JobRank is instantiated and inserted into the array on 
   * demand.
   * 
   * No locking is required, since this field is only accessed by the Dispatcher thread.
   */ 
  private JobRank[]  pJobRanks; 

  /**
   * The MonitorTask threads which manage the running jobs indexed by hostname and job ID.<P> 
   * 
   * The number of jobID's per host is the only accurate record of how many jobs we should
   * consider to be already running on a host. <P> 
   * 
   * If a thread entry exists but is not running, then it means that we lost contact with 
   * the host running the job after starting the job.  When the host is re-Enabled, the 
   * MonitorTask thread should be started.<P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */ 
  private DoubleMap<String,Long,MonitorTask>  pRunning;

  /**
   * The number of dispatcher cycles since the last garbage collection of jobs.
   */ 
  private int  pDispatcherCycles; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The locks for reading/writing individual QueueJob files. 
   */ 
  private TreeMap<Long,Object>  pJobFileLocks; 

  /**
   * The table of all existing QueueJob's indexed by job ID. <P> 
   * 
   * This table is initialized from the job files on startup and all changes to the table
   * are immediately written to disk. <P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */ 
  private TreeMap<Long,QueueJob>  pJobs; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The locks for reading/writing individual QueueJobInfo files. 
   */ 
  private TreeMap<Long,Object>  pJobInfoFileLocks;

  /**
   * The table of status information for all existing QueueJob's indexed by job ID.  <P> 
   * 
   * This table is initialized from the job info files on startup and all changes to the 
   * table are immediately written to disk. <P> 
   * 
   * Access to this field should be protected by a synchronized block when iterating, adding
   * or removing elements.  However the elements themselves have synchronized methods so after
   * being looked-up their methods can be called without holding this lock.
   */ 
  private TreeMap<Long,QueueJobInfo>  pJobInfo;
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * A table of pending changes to the JobReqs of QueueJobs. <p>
   * 
   * Access to this field should be protected by a synchronized block.
   */
  private TreeMap<Long,JobReqs>  pJobReqsChanges;

  /*----------------------------------------------------------------------------------------*/

  /**
   * The IDs of the latest jobs associated with each working version indexed by node ID and 
   * primary file name.  If the node ID for a working version is missing from the key
   * set, then no jobs exist which are associated with that working version.  If the 
   * job ID for a particular primary filename is missing then no job exists which will
   * regenerate that file. <P> 
   * 
   * Note that it is possible for several files to be generated by the same QueueJob, 
   * therefore job IDs may be repeated in per-file table for a particular working version 
   * of a node.  Also, only the newest job for a particular file is stored in this table
   * and the association with older jobs is not retained. <P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */ 
  private TreeMap<NodeID,TreeMap<File,Long>>  pNodeJobIDs;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The locks for reading/writing individual QueueJobGroup files. 
   */ 
  private TreeMap<Long,Object>  pJobGroupFileLocks;
  
  /**
   * The groups of jobs indexed by job group ID. <P> 
   *
   * This table is initialized from the job group files on startup and all changes to the 
   * table are immediately written to disk. <P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */
  private TreeMap<Long,QueueJobGroup>  pJobGroups; 


  /*----------------------------------------------------------------------------------------*/
     
  /**
   * Counts of the Running, Finished and total number of jobs in each job group per job.
   * 
   * No locking is required to access pJobCounters as all locking is handled internally
   * by this class.
   */ 
  private QueueJobCounters  pJobCounters; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Lock Nesting Order (to prevent deadlock):
   * 
   * synchronized(pSelectionGroups) {
   *   synchronized(pSelectionKeys) {
   *     ...
   *   }
   * }
   *
   * synchronized(pHostsInfo) {
   *   synchronized(pSelectionSchedules) {
   *     ...
   *   }
   *   synchronized(pHostsMod) {
   *     ...
   *   }
   *   synchronized(pHosts) {
   *     ...
   *   }
   * }
   * 
   * synchronized(pJobs) {
   *   synchronized(pJobReqsChanges) {
   *     synchronized(pSelectionKeys) {
   *         ...
   *     }
   *     synchronized(pHardwareKeys) {
   *       ...
   *     }
   *     synchronized(pLicenseKeys) {
   *         ...
   *     }
   *   }
   * }
   * 
   * synchronized(pHosts) {
   *   synchronized(pRunning) {
   *     ...
   *   }
   *   synchronized(pJobs) {
   *	 synchronized(pToolsets) {
   *	   synchronized(pSelectionGroups) {
   *         ...
   *       }
   *     }
   *   }
   *   synchronized(pJobInfo) {
   *     ...
   *   }
   *   synchronized(pHostsMod) {
   *     synchronized(pSelectionGroups) {
   *       ...
   *     }
   *     synchronized(pSelectionSchedules) {
   *       ...
   *     }
   *   }
   *   synchronized(pSamples) {
   *     ...
   *   }
   *   synchronized(pOsTypeChanges) {
   *     ...
   *   }
   *   synchronized(pNumProcChanges) {
   *     ...
   *   }
   *   synchronized(pTotalMemoryChanges) {
   *     ...
   *   }
   *   synchronized(pTotalDiskChanges) {
   *     ...
   *   }
   * }
   * 
   * synchronized(pJobInfo) {
   *   synchronized(pNodeJobIDs) {
   *     ...
   *   }
   * }
   * 
   * synchronized(pJobGroups) {
   *   synchronized(pJobInfo) {
   *     ...
   *   }
   * }
   * 
   * synchronized(pSamples) {
   *   synchronized(pSampleFileLock) { 
   *     synchronized(pMakeDirLock) {
   *       ...
   *    } 
   *   }
   * }
   */

}

