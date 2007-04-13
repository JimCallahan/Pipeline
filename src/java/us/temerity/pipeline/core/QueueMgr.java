// $Id: QueueMgr.java,v 1.88 2007/04/13 12:59:56 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.glue.io.*;
import us.temerity.pipeline.message.*;
import us.temerity.pipeline.toolset.*;
import us.temerity.pipeline.core.exts.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   M G R                                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The manager of queue jobs. <P>
 */
class QueueMgr
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
   * @param collectorBatchSize
   *   The maximum number of job servers per collection sub-thread.
   * 
   * @param dispatcherInterval
   *   The minimum time a cycle of the dispatcher loop should take (in milliseconds).
   */
  public
  QueueMgr
  (
   QueueMgrServer server,
   int collectorBatchSize,
   long dispatcherInterval
  ) 
  { 
    pServer = server;

    pCollectorBatchSize = new AtomicInteger(collectorBatchSize);
    pDispatcherInterval = new AtomicLong(dispatcherInterval);

    if(PackageInfo.sOsType != OsType.Unix)
      throw new IllegalStateException("The OS type must be Unix!");
    pQueueDir = PackageInfo.sQueuePath.toFile();

    pShutdownJobMgrs = new AtomicBoolean(false);

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       "Initializing [QueueMgr]..."); 

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

      pSelectionKeys      = new TreeMap<String,SelectionKey>();
      pSelectionGroups    = new TreeMap<String,SelectionGroup>();
      pSelectionSchedules = new TreeMap<String,SelectionSchedule>();
      
      pQueueExtensions = new TreeMap<String,QueueExtensionConfig>();

      pToolsets = new DoubleMap<String,OsType,Toolset>();

      pHostsMod           = new TreeMap<String,QueueHostMod>(); 
      pHungChanges        = new TreeSet<String>();
      pOsTypeChanges      = new TreeMap<String,OsType>();
      pNumProcChanges     = new TreeMap<String,Integer>();
      pTotalMemoryChanges = new TreeMap<String,Long>();
      pTotalDiskChanges   = new TreeMap<String,Long>();
      pHosts              = new TreeMap<String,QueueHost>(); 
      pHostsInfo          = new TreeMap<String,QueueHostInfo>();

      pLastSampleWritten = new AtomicLong(0L);
      pSamples           = new TreeMap<String,ResourceSampleCache>();
      pSampleFileLock    = new Object();

      pPreemptList = new ConcurrentLinkedQueue<Long>();
      pHitList     = new ConcurrentLinkedQueue<Long>();
      pPaused      = new TreeSet<Long>();

      pWaiting       = new ConcurrentLinkedQueue<Long>();
      pReady         = new TreeSet<Long>();

      pJobFileLocks = new TreeMap<Long,Object>();
      pJobs         = new TreeMap<Long,QueueJob>(); 

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

      /* create the lock file */ 
      {
	File file = new File(pQueueDir, "queue/lock");
	if(file.exists()) 
	  throw new IllegalStateException
	    ("Another queue manager is already running!\n" + 
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

      /* load and initialize the server extensions */ 
      initQueueExtensions();

      /* load the license and selection keys */ 
      initLicenseSelectionKeys(); 

      /* load the hosts if any exist */ 
      initHosts();

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
       "Loading License/Selection Keys...");   
    LogMgr.getInstance().flush();

    /* load the license keys if any exist */ 
    readLicenseKeys();
    
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
	      pWaiting.add(jobID);
	      break;
	      
	    case Paused:
	      pPaused.add(jobID);
	      pWaiting.add(jobID);
	      break;
	      
	    case Running:
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
	  pJobCounters.update(timer, info);
      }

      timer.suspend();
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Info,
	 "  Initialized in " + TimeStamps.formatInterval(timer.getTotalDuration()));
      LogMgr.getInstance().flush();    
    }

    /* start tasks to record the results of the already running jobs */ 
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
	synchronized(pJobInfo) {
          QueueJobInfo info = pJobInfo.get(jobID);
          if(info != null) 
            os = info.getOsType();
	}

        /* attempt to aquire the licenses already being used by the job */ 
	TreeSet<String> aquiredKeys = new TreeSet<String>();
        synchronized(pLicenseKeys) {
          for(String kname : job.getJobRequirements().getLicenseKeys()) {
            LicenseKey key = pLicenseKeys.get(kname);
            if(key != null) {
              if(key.aquire(hostname)) 
                aquiredKeys.add(kname);
              else {
                LogMgr.getInstance().log
                  (LogMgr.Kind.Ops, LogMgr.Level.Warning,
                   "Unable to aquire a (" + key.getName() + ") license key for the " + 
                   "job (" + jobID + ") already running on (" + hostname + ")!");
              }
            }            
          }
        }

	MonitorTask task = new MonitorTask(hostname, os, job, aquiredKeys);
	task.start();
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
      
      pServer.internalShutdown();
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
    {
      File file = new File(pQueueDir, "queue/lock");
      file.delete();
    }
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

    QueueControls controls = 
      new QueueControls(pCollectorBatchSize.get(), pDispatcherInterval.get());

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
   * @return
   *   <CODE>QueueGetLicenseKeyNamesRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to get the key names.
   */
  public Object
  getLicenseKeyNames() 
  {
    TaskTimer timer = new TaskTimer();
    timer.aquire();
    synchronized(pLicenseKeys) {
      timer.resume();
      
      TreeSet<String> names = new TreeSet<String>(pLicenseKeys.keySet());
      
      return new QueueGetLicenseKeyNamesRsp(timer, names);
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
   * @return
   *   <CODE>QueueGetSelectionKeyNamesRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to get the key names.
   */
  public Object
  getSelectionKeyNames() 
  {
    TaskTimer timer = new TaskTimer();
    timer.aquire();
    synchronized(pSelectionKeys) {
      timer.resume();
      
      TreeSet<String> names = new TreeSet<String>(pSelectionKeys.keySet());
      
      return new QueueGetSelectionKeyNamesRsp(timer, names);
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

	active = new HistogramSpec[11];
	active[0]  = specs.getStatusSpec(); 
	active[1]  = specs.getOsTypeSpec(); 
	active[2]  = specs.getLoadSpec(); 
	active[3]  = specs.getMemorySpec(); 
	active[4]  = specs.getDiskSpec(); 
	active[5]  = specs.getNumJobsSpec(); 
	active[6]  = specs.getSlotsSpec(); 
	active[7]  = specs.getReservationSpec(); 
	active[8]  = specs.getOrderSpec(); 
	active[9]  = specs.getGroupsSpec(); 
	active[10] = specs.getSchedulesSpec(); 

	boolean anyIncluded = false;
	int wk;
	for(wk=0; wk<active.length; wk++) {
	  if(active[wk].anyIncluded() && !active[wk].allIncluded()) 
	    anyIncluded = true;
	  else 
	    active[wk] = null;
	}

	/* if there are no included catagoried for any histogram, 
	     just return all hosts */ 
	if(!anyIncluded) 
	  active = null;
      }
    }

    /* proces the hosts */ 
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
	}
	catch(UnknownHostException ex) {
	  throw new PipelineException
	    ("The host (" + hname + ") could not be reached or is illegal!");
	}
	
	if(pHosts.containsKey(hname)) 
	  throw new PipelineException
	    ("The host (" + hname + ") is already a job server!");
	
	/* pre-add host tests */
	performExtensionTests(timer, factory);
	
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
      /* filter out non-existant hosts */ 
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
	      case Hung:
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

      timer.aquire();
      synchronized(pHostsMod) {
	timer.resume();
	pHostsMod.putAll(changes);
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

	    if(qmod.isSelectionScheduleModified()) 
	      qinfo.setSelectionSchedule(qmod.getSelectionSchedule()); 
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
	/* attempt to re-Enable previously Hung servers */ 
	for(QueueHost host : pHosts.values()) {
	  switch(host.getStatus()) {
	  case Hung:
	    if((host.getLastModified() + sUnhangInterval) < now) {
	      setHostStatus(host, QueueHost.Status.Enabled);	      
	      if(modifiedHosts != null) 
		modifiedHosts.add(host.getName());
	    }
	  }
	}
	   
	/* status */ 
	{
	  /* make a copy of pending status changes before attempting network communication 
	     so that the pHostsMod lock will be held for only a short amount of time */ 
	  TreeMap<String,QueueHostStatusChange> changes = null;
	  {
	    tm.aquire();
	    synchronized(pHostsMod) {
	      tm.resume();
	      changes = new TreeMap<String,QueueHostStatusChange>(); 
	      for(String hname : pHostsMod.keySet()) {
		QueueHostMod qmod = pHostsMod.get(hname);
		if(qmod.isStatusModified()) 
		  changes.put(hname, qmod.getStatus());
	      }
	    }
	  }

	  if(changes != null) {
	    for(String hname : changes.keySet()) {
	      QueueHostStatusChange change = changes.get(hname);
	      QueueHost host = pHosts.get(hname);
	      if((change != null) && (host != null)) {
		switch(change) {
		case Enable:
		case Disable:
		  try {
		    JobMgrControlClient client = new JobMgrControlClient(hname);
		    client.verifyConnection();
		    client.disconnect();
		  }
		  catch(PipelineException ex) {
		    change = QueueHostStatusChange.Terminate;
		  }	    	    
		}
		
		switch(change) {
		case Terminate:
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
		
		switch(change) {
		case Enable:
		  setHostStatus(host, QueueHost.Status.Enabled);
		  if(modifiedHosts != null) 
		    modifiedHosts.add(hname);
		  break;
		  
		case Disable:
		  setHostStatus(host, QueueHost.Status.Disabled);
		  if(modifiedHosts != null) 
		    modifiedHosts.add(hname);
		  break;
		  
		case Terminate:
		  setHostStatus(host, QueueHost.Status.Shutdown);
		  if(modifiedHosts != null) 
		    modifiedHosts.add(hname);
		}
	      }
	    }	      
	  }	  
	}

	/* hung servers */ 
	tm.aquire();
	synchronized(pHungChanges) {
	  tm.resume();

	  for(String hname : pHungChanges) {
	    QueueHost host = pHosts.get(hname);
	    if(host != null) {
	      switch(host.getStatus()) {
	      case Enabled:
		{
		  Long lastHung = host.getLastHung();
		  if((lastHung == null) || 
		     ((lastHung + sDisableInterval) < now)) 
		    setHostStatus(host, QueueHost.Status.Hung);
		  else 
		    setHostStatus(host, QueueHost.Status.Disabled);
		  if(modifiedHosts != null) 
		    modifiedHosts.add(hname);
		}
	      }
	    }
	  }

	  pHungChanges.clear();
	}

	/* cancel holds on non-enabled servers */ 
	for(QueueHost host : pHosts.values()) {
	  switch(host.getStatus()) {
	  case Disabled:
	  case Hung:
	  case Shutdown:
	    host.cancelHolds();
	  }
	}	

	/* other host property changes... */
	tm.aquire();
	synchronized(pHostsMod) {
	  tm.resume();

	  for(String hname : pHostsMod.keySet()) {
	    QueueHost host = pHosts.get(hname);
	    if(host != null) {
	      QueueHostMod qmod = pHostsMod.get(hname);

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
	    }
	  }
	    
	  pHostsMod.clear(); 
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
   * Helper for setting host status which interrupts any tasks waiting on the host.
   */ 
  private void 
  setHostStatus
  (
   QueueHost host, 
   QueueHost.Status status
  ) 
  {
    switch(status) {
    case Shutdown:
      JobMgrControlClient.serverUnreachable(host.getName());
    }
    
    host.setStatus(status);
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
      
      HistogramSpec oldSpec = specs.getGroupsSpec();
      HistogramSpec newSpec = new HistogramSpec("Groups", ranges);
      for(HistogramRange range : oldSpec.getIncluded())
	newSpec.setIncluded(range, true);
      
      specs.setGroupsSpec(newSpec); 
    }
    
    {
      TreeSet<HistogramRange> ranges = new TreeSet<HistogramRange>();
      ranges.add(new HistogramRange("-"));
      synchronized(pSelectionSchedules) {
	for(String sname : pSelectionSchedules.keySet())
	  ranges.add(new HistogramRange(sname));
      }
      
      HistogramSpec oldSpec = specs.getSchedulesSpec();
      HistogramSpec newSpec = new HistogramSpec("Schedules", ranges); 
      for(HistogramRange range : oldSpec.getIncluded())
	  newSpec.setIncluded(range, true);
      
      specs.setSchedulesSpec(newSpec); 
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
	      jobIDs.add(jobID);
	    }
	  }
	}
      }
    }
      
    return new GetUnfinishedJobsForNodeFilesRsp(timer, jobIDs);
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

    TreeSet<Long> jobIDs = new TreeSet<Long>();
    timer.aquire();  
    synchronized(pJobInfo) {
      timer.resume();
      for(Long jobID : pJobInfo.keySet()) {
	QueueJobInfo info = pJobInfo.get(jobID);
	switch(info.getState()) {
	case Running:
	  jobIDs.add(jobID);
	}
      }
    }
	
    timer.aquire();  
    synchronized(pJobs) {
      timer.resume();
      TreeMap<Long,JobStatus> running = new TreeMap<Long,JobStatus>();
      for(Long jobID : jobIDs) {
	QueueJob job = pJobs.get(jobID);	
	if(job != null) {
	  ActionAgenda agenda = job.getActionAgenda();
	  JobStatus status = 
	    new JobStatus(jobID, job.getNodeID(), JobState.Running, agenda.getToolset(), 
			  agenda.getPrimaryTarget(), job.getSourceJobIDs());
	  running.put(jobID, status);
	}
      }
      
      return new QueueGetJobStatusRsp(timer, running);
    }
  }

  /**
   * Get the job with the given ID. <P> 
   * 
   * @param req 
   *   The job request.
   *    
   * @return 
   *   <CODE>QueueGetJobRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the job.
   */ 
  public Object
  getJob
  (
   QueueGetJobReq req
  )
  {
    TaskTimer timer = new TaskTimer();
    timer.aquire();
    synchronized(pJobs) {
      timer.resume();
      try {
	Long jobID = req.getJobID();
	QueueJob job = pJobs.get(jobID);	
	if(job == null) 
	  throw new PipelineException
	    ("No job (" + jobID + ") exists!");
	
	return new QueueGetJobRsp(timer, job);
      }
      catch(PipelineException ex) {
	return new FailureRsp(timer, ex.getMessage());	  
      }   
    }
  }

  /**
   * Get information about the current status of a job in the queue. <P> 
   * 
   * @param req 
   *   The job info request.
   *    
   * @return 
   *   <CODE>QueueGetJobInfoRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the job info.
   */ 
  public Object
  getJobInfo
  (
   QueueGetJobInfoReq req
  )
  {
    TaskTimer timer = new TaskTimer();
    timer.aquire();
    synchronized(pJobInfo) {
      timer.resume();
      try {
	Long jobID = req.getJobID();
	QueueJobInfo info = pJobInfo.get(jobID);
	if(info == null) 
	  throw new PipelineException
	    ("No information is available for job (" + jobID + ")!");

	return new QueueGetJobInfoRsp(timer, info);
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
      for(Long jobID : pJobInfo.keySet()) {
	QueueJobInfo info = pJobInfo.get(jobID);
	if(info != null) {
	  switch(info.getState()) {
	  case Running:
	    running.put(jobID, info);
	  }
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
	  pJobCounters.update(timer, info);
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

  /**
   * Preempt the jobs with the given IDs. <P> 
   * 
   * @param req 
   *   The preempt jobs request.
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
   *   The kill jobs request.
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
	    if(pAdminPrivileges.isQueueManaged(req, author)) 
	      pPaused.add(jobID);
	    else 
	      unprivileged = true;
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
	    if(pAdminPrivileges.isQueueManaged(req, author)) 
	      pPaused.remove(jobID);
	    else 
	      unprivileged = true;
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
   * Get all of the existing job groups.
   * 
   * @return 
   *   <CODE>QueueGetJobGroupsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the job groups.
   */ 
  public Object
  getJobGroups() 
  {
    TaskTimer timer = new TaskTimer();
    timer.aquire();
    synchronized(pJobGroups) {
      timer.resume();
      TreeMap<Long,QueueJobGroup> groups = new TreeMap<Long,QueueJobGroup>();
      for(Long groupID : pJobGroups.keySet()) {
	QueueJobGroup group = pJobGroups.get(groupID);
	if(group != null) 
	  groups.put(groupID, group);
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
	      (LogMgr.Kind.Net, LogMgr.Level.Severe,
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
	    TreeSet<String> dead = thread.getDead();
	    if(!dead.isEmpty()) {
	      tm.aquire();
	      synchronized(pHostsMod) {
		tm.resume();
		
		for(String hname : dead) {
		  QueueHostMod qmod = pHostsMod.get(hname);
		  if(qmod == null) {
		    qmod = new QueueHostMod(QueueHostStatusChange.Terminate);
		    pHostsMod.put(hname, qmod);
		  }
		  else {
		    qmod.setStatus(QueueHostStatusChange.Terminate);
		  }
		}
	      }
	    }
	  }

	  {	
	    TreeSet<String> hung = thread.getHung();
	    if(!hung.isEmpty()) {
	      tm.aquire();
	      synchronized(pHungChanges) {
		tm.resume();
		
		pHungChanges.addAll(hung);
	      }
	    }
	  }
	  
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
	    (LogMgr.Kind.Dsp, LogMgr.Level.Severe,
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
	TaskTimer tm = new TaskTimer("Dispatcher [Clean Samples]");
	{
	  cleanupSamples(tm);
	}
	LogMgr.getInstance().logSubStage
	  (LogMgr.Kind.Dsp, LogMgr.Level.Finer,
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
	  (LogMgr.Kind.Col, LogMgr.Level.Finest,
	   "Collector: Sleeping for (" + nap + ") ms...");

	try {
	  Thread.sleep(nap);
	}
	catch(InterruptedException ex) {
	}
      }
      else {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Col, LogMgr.Level.Finest,
	   "Collector: Overbudget by (" + (-nap) + ") ms...");
      }
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
    {
      timer.suspend();
      TaskTimer tm = new TaskTimer("Dispatcher [Apply Host Edits]");
      try {
	applyHostEdits(tm);
      }
      catch(PipelineException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Severe,
	   ex.getMessage()); 
      }
      LogMgr.getInstance().logSubStage
	(LogMgr.Kind.Dsp, LogMgr.Level.Finer, 
	 tm, timer);
    }

    /* kill/abort the jobs in the hit list */ 
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

	QueueJobInfo info = null;
	tm.aquire();
	synchronized(pJobInfo) {
	  tm.resume();
	  info = pJobInfo.get(jobID);
	}
	
	if(info != null) {
	  boolean aborted = false;
	  switch(info.getState()) {
	  case Queued:
	  case Preempted:
	  case Paused:
	    info.aborted();
	    try {
	      writeJobInfo(info);
	    }
	    catch(PipelineException ex) {
	      LogMgr.getInstance().log
		(LogMgr.Kind.Net, LogMgr.Level.Severe,
		 ex.getMessage()); 
	    }
	    pJobCounters.update(tm, info);
	    aborted = true;
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
    
    /* kill and requeue running jobs on the preempt list */  
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

	QueueJobInfo info = null;
	tm.aquire();
	synchronized(pJobInfo) {
	  tm.resume();
	  info = pJobInfo.get(jobID);
	}

	if(info != null) {
	  switch(info.getState()) {
	  case Running:
	    {
	      QueueJobInfo preemptedInfo = new QueueJobInfo(info);

	      {
		String hostname = info.getHostname();
		
		info.preempted();
		try {
		  writeJobInfo(info);
		}
		catch(PipelineException ex) {
		  LogMgr.getInstance().log
		  (LogMgr.Kind.Ops, LogMgr.Level.Severe,
		   ex.getMessage()); 
		}
		
		pJobCounters.update(tm, info);

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

    /* process the waiting jobs: sorting jobs into killed/aborted, ready and waiting */ 
    TreeSet<String> readyToolsets = new TreeSet<String>();
    {
      timer.suspend();
      TaskTimer tm = new TaskTimer("Dispatcher [Sort Waiting]");
      LinkedList<Long> waiting = new LinkedList<Long>();
      while(true) {
	Long jobID = pWaiting.poll();
	if(jobID == null) 
	  break;
	
	QueueJobInfo info = null;
	tm.aquire();
	synchronized(pJobInfo) {
	  tm.resume();
	  info = pJobInfo.get(jobID);
	}
	
	if(info != null) {
	  switch(info.getState()) {
	  case Queued:	     
	  case Preempted:
	    {
	      /* pause waiting jobs marked to be paused */  
	      if(pPaused.contains(jobID)) {
		tm.aquire();
		synchronized(pJobInfo) {
		  tm.resume();

		  info.paused();
		  try {
		    writeJobInfo(info);
		  }
		  catch(PipelineException ex) {
		    LogMgr.getInstance().log
		      (LogMgr.Kind.Ops, LogMgr.Level.Severe,
		       ex.getMessage()); 
		  }

		  pJobCounters.update(tm, info);
		}

		waiting.add(jobID);
		break;
	      }
	      
	      QueueJob job = null;
	      tm.aquire();
	      synchronized(pJobs) {
		tm.resume();
		job = pJobs.get(jobID);
	      }

	      /* determine whether the job is ready for execution */ 
	      if(job != null) {
		boolean waitingOnUpstream = false; 
		boolean abortDueToUpstream = false;
		for(Long sjobID : job.getSourceJobIDs()) {
		  QueueJobInfo sinfo = null;
		  tm.aquire();
		  synchronized(pJobInfo) {
		    tm.resume();
		    sinfo = pJobInfo.get(sjobID);
		  }
		  
		  if(sinfo != null) {
		    switch(sinfo.getState()) {	   
		    case Queued:
		    case Preempted:
		    case Paused:
		    case Running:
		      waitingOnUpstream = true;
		      break;

		    case Aborted:
		    case Failed:
		      abortDueToUpstream = true;
		    }
		  }
		}
		
		if(abortDueToUpstream) 
		  pHitList.add(jobID);
		else if(waitingOnUpstream) 
		  waiting.add(jobID);
		else {
		  pReady.add(jobID);
		  readyToolsets.add(job.getActionAgenda().getToolset());
		}
	      }
	    }
	    break;

	  case Paused:
	    /* resume previously paused jobs marked to be resumed */ 
	    {
	      if(!pPaused.contains(jobID)) {
		tm.aquire();
		synchronized(pJobInfo) {
		  tm.resume();

		  info.resumed();
		  try {
		    writeJobInfo(info);
		  }
		  catch(PipelineException ex) {
		    LogMgr.getInstance().log
		      (LogMgr.Kind.Ops, LogMgr.Level.Severe,
		       ex.getMessage()); 
		  }

		  pJobCounters.update(tm, info);
		}
	      }

	      waiting.add(jobID);
	    }
	  }
	}
      }
      pWaiting.addAll(waiting);      

      LogMgr.getInstance().logSubStage
	(LogMgr.Kind.Dsp, LogMgr.Level.Finer, 
	 tm, timer);
    }

    /* retrieve any toolsets required by newly ready jobs which are not already cached */ 
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

    /* update the read-only cache of job server info before aquiring any potentially
       long duration locks on the pHosts table */ 
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

    /* process the available job server slots in dispatch order */ 
    {
      timer.suspend();
      TaskTimer dtm = new TaskTimer("Dispatcher [Dispatch Total]");

      TreeSet<String> keys = new TreeSet<String>();
      dtm.aquire();
      synchronized(pSelectionKeys) {
	dtm.resume();
	keys.addAll(pSelectionKeys.keySet());
      }

      dtm.aquire();
      synchronized(pHosts) {
	dtm.resume();
	
	/* sort hosts by dispatch order */ 
	ArrayList<String> hostsInOrder = new ArrayList<String>();
	{
	  dtm.suspend();
	  TaskTimer tm = new TaskTimer("Dispatcher [Order Hosts]");

	  TreeMap<Integer,TreeSet<String>> inOrder = 
	    new TreeMap<Integer,TreeSet<String>>();
	  for(String hostname : pHosts.keySet()) {
	    QueueHost host = pHosts.get(hostname);
	    TreeSet<String> names = inOrder.get(host.getOrder());
	    if(names == null) {
	      names = new TreeSet<String>();
	      inOrder.put(host.getOrder(), names);
	    }
	    names.add(hostname);
	  }
	  
	  for(TreeSet<String> names : inOrder.values()) 
	    hostsInOrder.addAll(names);

	  LogMgr.getInstance().logSubStage
	    (LogMgr.Kind.Dsp, LogMgr.Level.Finer, 
	     tm, dtm); 
	}

	for(String hostname : hostsInOrder) {
	  if(pReady.isEmpty()) 
	    break;

	  QueueHost host = pHosts.get(hostname);
	  switch(host.getStatus()) {
	  case Enabled:
	    {
	      int slots = host.getAvailableSlots();

	      LogMgr.getInstance().logAndFlush
		(LogMgr.Kind.Ops, LogMgr.Level.Finest,
		 "Initial Slots [" + hostname + "]: Free = " + slots + "\n");
    
	      dtm.suspend();
	      TaskTimer stm = new TaskTimer
		("Dispatcher [Dispatch Host - " + hostname + "]");

	      while(slots > 0) {
		/* rank job IDs by selection score, priority and submission stamp */
		ArrayList<Long> rankedJobIDs = new ArrayList<Long>();
		{
		  stm.suspend();
		  TaskTimer tm = new TaskTimer
		    ("Dispatcher [Rank Jobs - " + hostname + ":" + slots + "]");

		  /* job ID indexed by selection score, percent, priority and timestamp */ 
		TreeMap<Integer,TreeMap<Double,TreeMap<Integer,TreeMap<Long,Long>>>> byScore =
		  new TreeMap<Integer,TreeMap<Double,TreeMap<Integer,TreeMap<Long,Long>>>>();

		  for(Long jobID : pReady) {
		    /* selection score */ 
		    TreeMap<Double,TreeMap<Integer,TreeMap<Long,Long>>> byPercent = null;
		    {
		      Integer score = null;
		      tm.aquire();
		      synchronized(pJobs) {
			tm.resume();
			QueueJob job = pJobs.get(jobID);
			if(job != null) {
			  ActionAgenda jagenda = job.getActionAgenda();
			  String author = jagenda.getNodeID().getAuthor();
			  JobReqs jreqs = job.getJobRequirements();

			  boolean supportsOsToolset = false;
			  tm.aquire();
			  synchronized(pToolsets) {
			    tm.resume();
			    String tname = jagenda.getToolset();
			    supportsOsToolset = 
			      (pToolsets.containsKey(tname) && 
			       pToolsets.get(tname).containsKey(host.getOsType()));
			  }

			  if(supportsOsToolset && 
			     job.getAction().supports(host.getOsType()) &&
			     host.isEligible(author, pAdminPrivileges, jreqs)) {

			    if(jreqs.getSelectionKeys().isEmpty()) 
			      score = 0;
			    else {
			      String gname = host.getSelectionGroup();
			      if(gname != null) {
				tm.aquire();
				synchronized(pSelectionGroups) {
				  tm.resume();
				  SelectionGroup sg = pSelectionGroups.get(gname);
				  if(sg == null) 
				    score = 0;
				  else 
				    score = sg.computeSelectionScore(jreqs, keys);
				}
			      }
			    }
			  }
			}
		      }
		    
		      if(score != null) {
			byPercent = byScore.get(score);
			if(byPercent == null) {
			  byPercent = 
			    new TreeMap<Double,TreeMap<Integer,TreeMap<Long,Long>>>();
			  byScore.put(score, byPercent);
			}
		      }
		    }
		    
		    /* percent engaged/pending */ 
		    TreeMap<Integer,TreeMap<Long,Long>> byPriority = null;
		    if(byPercent != null) {
		      double percent = 0.0;
		      {
			String gname = host.getSelectionGroup();
			if(gname != null) {
			  tm.aquire();
			  synchronized(pSelectionGroups) {
			    tm.resume();
			    SelectionGroup sg = pSelectionGroups.get(gname);
			    if(sg != null) {
			      switch(sg.getFavorMethod()) {
			      case MostEngaged:
				percent = pJobCounters.percentEngaged(tm, jobID);
				break;

			      case MostPending:
				percent = pJobCounters.percentPending(tm, jobID);
			      }
			    }
			  }
			}
		      }

		      byPriority = byPercent.get(percent);
		      if(byPriority == null) {
			byPriority = new TreeMap<Integer,TreeMap<Long,Long>>();
			byPercent.put(percent, byPriority);
		      }
		    }

		    /* job priority */ 
		    TreeMap<Long,Long> byAge = null;
		    if(byPriority != null) {
		      Integer priority = null;
		      tm.aquire();
		      synchronized(pJobs) {
			tm.resume();
			QueueJob job = pJobs.get(jobID);
			if(job != null) 
			  priority = job.getJobRequirements().getPriority();
		      }

		      if(priority != null) {
			byAge = byPriority.get(priority);
			if(byAge == null) {
			  byAge = new TreeMap<Long,Long>();
			  byPriority.put(priority, byAge);
			}
		      }
		    }

		    /* submission date */
		    if(byAge != null) {
		      Long stamp = null;
		      tm.aquire();
		      synchronized(pJobInfo) {
			tm.resume();
			QueueJobInfo info = pJobInfo.get(jobID);
			if(info != null) 
			  stamp = info.getSubmittedStamp();
		      }
		      
		      if(stamp != null) 
			byAge.put(stamp, jobID);
		    }
		  }

		  /* process into a simple list of ranked job IDs */ 
		  {
		    LinkedList<Integer> scores = new LinkedList<Integer>(byScore.keySet());
		    Collections.reverse(scores);

		    for(Integer score : scores) {

		      TreeMap<Double,TreeMap<Integer,TreeMap<Long,Long>>> byPercent = 
			byScore.get(score);
		      LinkedList<Double> percents =  
			new LinkedList<Double>(byPercent.keySet());
		      Collections.reverse(percents);
		      
		      for(Double percent : percents) {
			
			TreeMap<Integer,TreeMap<Long,Long>> byPriority = 
			  byPercent.get(percent); 
			LinkedList<Integer> priorities = 
			  new LinkedList<Integer>(byPriority.keySet());
			Collections.reverse(priorities);
			
			for(Integer priority : priorities) {
			  for(Long jobID : byPriority.get(priority).values()) 
			    rankedJobIDs.add(jobID);
			}
		      }
		    }
		  }
		  LogMgr.getInstance().logSubStage
		    (LogMgr.Kind.Dsp, LogMgr.Level.Finer, 
		     tm, stm);
		}

		/* attempt to dispatch a job to the slot:
		     in order of selection score, job priority and age */ 
		TreeSet<Long> processed = new TreeSet<Long>();
		{
		  stm.suspend();
		  TaskTimer tm = new TaskTimer
		    ("Dispatcher [Assign Job - " + hostname + ":" + slots + "]");

		  boolean jobDispatched = false;
		  for(Long jobID : rankedJobIDs) {
		    if(jobDispatched) 
		      break;

		    QueueJob job = null;
		    tm.aquire();
		    synchronized(pJobs) {
		      tm.resume();
		      job = pJobs.get(jobID);
		    }

		    QueueJobInfo info = null;
		    {
		      tm.aquire();
		      synchronized(pJobInfo) {
			tm.resume();
			info = pJobInfo.get(jobID);
		      }
		    }

		    if((job == null) || (info == null)) {
		      processed.add(jobID);
		    }
		    else {
		      switch(info.getState()) {
		      case Queued:
		      case Preempted:
			/* pause ready jobs marked to be paused */ 
			if(pPaused.contains(jobID)) {
			  tm.aquire();
			  synchronized(pJobInfo) {
			    tm.resume();
			    
			    info.paused();
			    try {
			      writeJobInfo(info);
			    }
			    catch(PipelineException ex) {
			      LogMgr.getInstance().log
				(LogMgr.Kind.Ops, LogMgr.Level.Severe,
				 ex.getMessage()); 
			    }

			    pJobCounters.update(tm, info);
			  }
			  
			  pWaiting.add(jobID);
			  processed.add(jobID);
			}
			
			/* try to dispatch the job */ 
			else if(dispatchJob(job, info, host, tm)) {
			  processed.add(jobID);
			  jobDispatched = true;
			}
			break;
		    
		      /* skip aborted jobs */ 
		      case Aborted:
			processed.add(jobID);
			break;
			
		      default:
			throw new IllegalStateException
			  ("Unexpected job state (" + info.getState() + ")!");
		      }
		    }
		  }
		  LogMgr.getInstance().logSubStage
		    (LogMgr.Kind.Dsp, LogMgr.Level.Finer, 
		     tm, stm);
		}

		for(Long jobID : processed) 
		  pReady.remove(jobID);

		/* next slot, if any are available */ 
		slots = Math.min(slots-1, host.getAvailableSlots());

		LogMgr.getInstance().logAndFlush
		  (LogMgr.Kind.Ops, LogMgr.Level.Finest,
		   "Updated Slots [" + hostname + "]:  Free = " + slots + "\n");
	      }
	      LogMgr.getInstance().logSubStage
		(LogMgr.Kind.Dsp, LogMgr.Level.Finer, 
		 stm, dtm); 
	    }
	  }
	}
      }
      LogMgr.getInstance().logSubStage
	(LogMgr.Kind.Dsp, LogMgr.Level.Finer, 
	 dtm, timer);
    }

    /* filter any jobs not ready for execution from the ready list */ 
    {
      timer.suspend();
      TaskTimer tm = new TaskTimer("Dispatcher [Filter Unready]");
      TreeSet<Long> processed = new TreeSet<Long>();
      for(Long jobID : pReady) {
	tm.aquire();
	synchronized(pJobInfo) {
	  tm.resume();

	  QueueJobInfo info = pJobInfo.get(jobID);
	  if(info == null) {
	    processed.add(jobID);
	  }
	  else {
	    switch(info.getState()) {
	    case Queued:
	    case Preempted:
	      /* pause ready jobs marked to be paused */ 
	      if(pPaused.contains(jobID)) {
		info.paused();
		try {
		  writeJobInfo(info);
		}
		catch(PipelineException ex) {
		  LogMgr.getInstance().log
		    (LogMgr.Kind.Ops, LogMgr.Level.Severe,
		     ex.getMessage()); 
		}
		
		pJobCounters.update(tm, info);

		pWaiting.add(jobID);
		processed.add(jobID);
	      }
	      break;
	  
	    /* strip any not ready */ 
	    default:
	      processed.add(jobID);
	    }
	  }
	}
      }

      for(Long jobID : processed) 
	pReady.remove(jobID);

      LogMgr.getInstance().logSubStage
	(LogMgr.Kind.Dsp, LogMgr.Level.Finer, 
	 tm, timer);
    }

    /* check for newly completed job groups */ 
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
	      QueueJobInfo info = null;
	      tm.aquire();
	      synchronized(pJobInfo) {
		tm.resume();
		info = pJobInfo.get(jobID);
	      }

	      if(info != null) {
		switch(info.getState()) {
		case Queued:
		case Preempted:
		case Paused:
		case Running:
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
		  (LogMgr.Kind.Ops, LogMgr.Level.Severe,
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
	  (LogMgr.Kind.Dsp, LogMgr.Level.Finest,
	   "Dispatcher: Sleeping for (" + nap + ") ms...");
	try {
	  Thread.sleep(nap);
	}
	catch(InterruptedException ex) {
	}
      }
      else {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Dsp, LogMgr.Level.Finest,
	   "Dispatcher: Overbudget by (" + (-nap) + ") ms...");
      }
    }
  }

  /**
   * Attempt to dispatch the job on the given server. <P> 
   * 
   * If all license keys can be obtained, the job will be started on the server and a task
   * will be started to monitor the jobs progress. <P> 
   * 
   * This should only be called from the dispatcher() method!
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
 	    if(key.aquire(host.getName())) 
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
	  if((author != null) && (view != null)) 
	    env = tset.getEnvironment(author, view, os);
	  else if(author != null)
	    env = tset.getEnvironment(author, os);
	  else 
	    env = tset.getEnvironment();
	  
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

	info.started(host.getName(), host.getOsType());
	try {
	  writeJobInfo(info);
	}
	catch (PipelineException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Ops, LogMgr.Level.Severe,
	     ex.getMessage()); 
	}

	pJobCounters.update(timer, info);
      }

      host.setHold(job.getJobID(), jreqs.getRampUp());
      host.jobStarted();
    }

    /* start a task to contact the job server to the job 
         and collect the results of the execution */ 
    {
      MonitorTask task = 
        new MonitorTask(host.getName(), host.getOsType(), job, aquiredKeys, envs);
      task.start();
    }

    return true;
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
      for(Long jobID : pReady) 
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
  /*   S C H E D U L E R                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Perform one round of sheduling the assignment of selection groups to job servers. 
   */ 
  public void 
  scheduler()
  {
    TaskTimer timer = new TaskTimer("Scheduler");

    /* precompute current groups for each schedule */ 
    TreeMap<String,String> scheduledGroups = new TreeMap<String,String>();
    {
      timer.suspend();
      TaskTimer tm = new TaskTimer("Scheduler [Compute Groups]");
      tm.aquire();
      synchronized(pSelectionSchedules) {
	tm.resume();
	
        long now = System.currentTimeMillis();
	for(SelectionSchedule sched : pSelectionSchedules.values()) 
	  scheduledGroups.put(sched.getName(), sched.activeGroup(now));
      }
      LogMgr.getInstance().logSubStage
	(LogMgr.Kind.Sch, LogMgr.Level.Finer, 
	 tm, timer);
    }
    
    /* update the hosts */ 
    {
      timer.suspend();
      TaskTimer utm = new TaskTimer("Scheduler [Update Hosts]");      
      utm.aquire();
      synchronized(pHosts) {
	boolean modified = false;
	synchronized(pSelectionGroups) {
	  utm.resume();
	  
	  for(String hname : pHosts.keySet()) {
	    QueueHost host = pHosts.get(hname);
	    
	    String sname = host.getSelectionSchedule();
	    String gname = host.getSelectionGroup();
	    if(sname != null) {
	      String name = scheduledGroups.get(sname);
	      if(!pSelectionGroups.containsKey(name)) 
		name = null;
	      
	      if(!(((name == null) && (gname == null)) ||
		   ((name != null) && (name.equals(gname))))) {
		host.setSelectionGroup(name);
		modified = true;
		
		LogMgr.getInstance().logAndFlush
		  (LogMgr.Kind.Sch, LogMgr.Level.Finest,
		   "Scheduler [" + hname + "]: " + 
		   "Selection Group = " + name + " (" + gname + ")\n");
	      }
	    }
	  }
	}

	/* write changes to disk */ 
	if(modified) {
	  utm.suspend();
	  TaskTimer tm = new TaskTimer("Scheduler [Write Hosts]");  
	  try {
	    writeHosts();
	  }
	  catch(PipelineException ex) {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Ops, LogMgr.Level.Severe,
	       ex.getMessage());
	  }
	  LogMgr.getInstance().logSubStage
	    (LogMgr.Kind.Sch, LogMgr.Level.Finest, 
	     tm, utm); 
	}
      }
      LogMgr.getInstance().logSubStage
	(LogMgr.Kind.Sch, LogMgr.Level.Finer, 
	 utm, timer);
    }

    /* if we're ahead of schedule, take a nap */ 
    {
      LogMgr.getInstance().logStage
	(LogMgr.Kind.Sch, LogMgr.Level.Fine,
	 timer); 

      long nap = sSchedulerInterval - timer.getTotalDuration();
      if(nap > 0) {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Sch, LogMgr.Level.Finest,
	   "Scheduler: Sleeping for (" + nap + ") ms...");

	try {
	  Thread.sleep(nap);
	}
	catch(InterruptedException ex) {
	}
      }
      else {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Sch, LogMgr.Level.Finest,
	   "Scheduler: Overbudget by (" + (-nap) + ") ms...");
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
	  String glue = null;
	  try {
	    ArrayList<LicenseKey> keys = 
	      new ArrayList<LicenseKey>(pLicenseKeys.values());
	    GlueEncoder ge = new GlueEncoderImpl("LicenseKeys", keys);
	    glue = ge.getText();
	  }
	  catch(GlueException ex) {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	       "Unable to generate a Glue format representation of the license keys!");
	    
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
	     "  While attempting to write the license keys file (" + file + ")...\n" + 
	     "    " + ex.getMessage());
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
	  GlueDecoder gd = new GlueDecoderImpl(file); 
	  keys = (ArrayList<LicenseKey>) gd.getObject();
	}
	catch(Exception ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	     "The license keys file (" + file + ") appears to be corrupted:\n" + 
	     "  " + ex.getMessage());
	  
	  throw new PipelineException
	    ("I/O ERROR: \n" + 
	     "  While attempting to read the license keys file (" + file + ")...\n" + 
	     "    " + ex.getMessage());
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
	  String glue = null;
	  try {
	    ArrayList<SelectionKey> keys = 
	      new ArrayList<SelectionKey>(pSelectionKeys.values());
	    GlueEncoder ge = new GlueEncoderImpl("SelectionKeys", keys);
	    glue = ge.getText();
	  }
	  catch(GlueException ex) {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	       "Unable to generate a Glue format representation of the selection keys!");
	    
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
	     "  While attempting to write the selection keys file (" + file + ")...\n" + 
	     "    " + ex.getMessage());
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
      pSelectionKeys.clear();

      File file = new File(pQueueDir, "queue/etc/selection-keys");
      if(file.isFile()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Reading Selection Keys.");

	ArrayList<SelectionKey> keys = null;
	try {
	  GlueDecoder gd = new GlueDecoderImpl(file);
	  keys = (ArrayList<SelectionKey>) gd.getObject();
	}
	catch(Exception ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	     "The selection keys file (" + file + ") appears to be corrupted:\n" + 
	     "  " + ex.getMessage());
	  
	  throw new PipelineException
	    ("I/O ERROR: \n" + 
	     "  While attempting to read the selection keys file (" + file + ")...\n" + 
	     "    " + ex.getMessage());
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
	  String glue = null;
	  try {
	    ArrayList<SelectionGroup> groups = 
	      new ArrayList<SelectionGroup>(pSelectionGroups.values());
	    GlueEncoder ge = new GlueEncoderImpl("SelectionGroups", groups);
	    glue = ge.getText();
	  }
	  catch(GlueException ex) {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	       "Unable to generate a Glue format representation of the selection groups!");
	    
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
	     "  While attempting to write the selection groups file (" + file + ")...\n" + 
	     "    " + ex.getMessage());
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
      pSelectionGroups.clear();

      File file = new File(pQueueDir, "queue/etc/selection-groups");
      if(file.isFile()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
	   "Reading Selection Groups.");

	ArrayList<SelectionGroup> groups = null;
	try {
	  GlueDecoder gd = new GlueDecoderImpl(file);
	  groups = (ArrayList<SelectionGroup>) gd.getObject();
	}
	catch(Exception ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	     "The selection groups file (" + file + ") appears to be corrupted:\n" + 
	     "  " + ex.getMessage());
	  
	  throw new PipelineException
	    ("I/O ERROR: \n" + 
	     "  While attempting to read the selection groups file (" + file + ")...\n" + 
	     "    " + ex.getMessage());
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
	  String glue = null;
	  try {
	    ArrayList<SelectionSchedule> schedules = 
	      new ArrayList<SelectionSchedule>(pSelectionSchedules.values());
	    GlueEncoder ge = new GlueEncoderImpl("SelectionSchedules", schedules);
	    glue = ge.getText();
	  }
	  catch(GlueException ex) {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	       "Unable to generate a Glue format representation of the selection schedules!");
	    
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
	     "  While attempting to write the selection schedules file (" + file + ")...\n" + 
	     "    " + ex.getMessage());
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
	  GlueDecoder gd = new GlueDecoderImpl(file);
	  schedules = (ArrayList<SelectionSchedule>) gd.getObject();
	}
	catch(Exception ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	     "The selection schedules file (" + file + ") appears to be corrupted:\n" + 
	     "  " + ex.getMessage());
	  
	  throw new PipelineException
	    ("I/O ERROR: \n" + 
	     "  While attempting to read the selection schedules file (" + file + ")...\n" + 
	     "    " + ex.getMessage());
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
	  String glue = null;
	  try {
	    GlueEncoder ge = new GlueEncoderImpl("QueueExtensions", pQueueExtensions);
	    glue = ge.getText();
	  }
	  catch(GlueException ex) {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	       "Unable to generate a Glue format representation of the queue extensions!");
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
	     "  While attempting to write the queue extensions file (" + file + ")...\n" + 
	     "    " + ex.getMessage());
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
	  GlueDecoder gd = new GlueDecoderImpl(file);
	  exts = (TreeMap<String,QueueExtensionConfig>) gd.getObject();
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
	  String glue = null;
	  try {
	    TreeMap<String,QueueHostInfo> infos = new TreeMap<String,QueueHostInfo>();
	    for(QueueHost host : pHosts.values()) 
	      infos.put(host.getName(), host.toInfo());

	    GlueEncoder ge = new GlueEncoderImpl("Hosts", infos);
	    glue = ge.getText();
	  }
	  catch(GlueException ex) {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	       "Unable to generate a Glue format representation of the hosts!");
	    
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
	     "  While attempting to write the hosts file (" + file + ")...\n" + 
	     "    " + ex.getMessage());
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
	  GlueDecoder gd = new GlueDecoderImpl(file);
	  infos = (TreeMap<String,QueueHostInfo>) gd.getObject();
	}
	catch(Exception ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	     "The hosts file (" + file + ") appears to be corrupted:\n" + 
	     "  " + ex.getMessage());
	  
	  throw new PipelineException
	    ("I/O ERROR: \n" + 
	     "  While attempting to read the hosts file (" + file + ")...\n" + 
	     "    " + ex.getMessage());
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
	      String glue = null;
	      try {
		GlueEncoder ge = new GlueEncoderImpl("Samples", gcache);
		glue = ge.getText();
	      }
	      catch(GlueException ex) {
		LogMgr.getInstance().log
		  (LogMgr.Kind.Glu, LogMgr.Level.Severe,
		   "Unable to generate a Glue format representation of the " + 
		   "resource samples!");
		
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
		 "  While attempting to write the resource samples file " + 
		 "(" + file + ")...\n" + 
		 "    " + ex.getMessage());
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
	  GlueDecoder gd = new GlueDecoderImpl(file);
	  cache = (ResourceSampleCache) gd.getObject();
	}
	catch(Exception ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	     "Ignoring corrupted resource sample file (" + file + "):\n" + 
	     "  " + ex.getMessage());
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
	String glue = null;
	try {
	  GlueEncoder ge = new GlueEncoderImpl("Job", job);
	  glue = ge.getText();
	}
	catch(GlueException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	    "Unable to generate a Glue format representation of the job!");
	  
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
	   "  While attempting to write the job file (" + file + ")...\n" + 
	   "    " + ex.getMessage());
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
	
	QueueJob job = null;
	try {
	  GlueDecoder gd = new GlueDecoderImpl(file);
	  job = (QueueJob) gd.getObject();
	}
	catch(Exception ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	     "The job file (" + file + ") appears to be corrupted:\n" + 
	     "  " + ex.getMessage());
	  
	  throw new PipelineException
	    ("I/O ERROR: \n" + 
	     "  While attempting to read the job file (" + file + ")...\n" + 
	     "    " + ex.getMessage());
	}
	
	return job;
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
	String glue = null;
	try {
	  GlueEncoder ge = new GlueEncoderImpl("JobInfo", info);
	  glue = ge.getText();
	}
	catch(GlueException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	     "Unable to generate a Glue format representation of the job information!");
	  
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
	   "  While attempting to write the job information file (" + file + ")...\n" + 
	   "    " + ex.getMessage());
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
	
	QueueJobInfo info = null;
	try {
	  GlueDecoder gd = new GlueDecoderImpl(file);
	  info = (QueueJobInfo) gd.getObject();
	}
	catch(Exception ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	     "The job information file (" + file + ") appears to be corrupted:\n" + 
	     "  " + ex.getMessage());
	  
	  throw new PipelineException
	    ("I/O ERROR: \n" + 
	     "  While attempting to read the job information file (" + file + ")...\n" + 
	     "    " + ex.getMessage());
	}
	
	return info;
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
	String glue = null;
	try {
	  GlueEncoder ge = new GlueEncoderImpl("JobGroup", group);
	  glue = ge.getText();
	}
	catch(GlueException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	     "Unable to generate a Glue format representation of the job group!");
	  
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
	   "  While attempting to write the job group file (" + file + ")...\n" + 
	   "    " + ex.getMessage());
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
	
	QueueJobGroup group = null; 
	try {
	  GlueDecoder gd = new GlueDecoderImpl(file);
	  group = (QueueJobGroup) gd.getObject();
	}
	catch(Exception ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	     "The job group file (" + file + ") appears to be corrupted:\n" + 
	     "  " + ex.getMessage());
	  
	  throw new PipelineException
	    ("I/O ERROR: \n" + 
	     "  While attempting to read the job group file (" + file + ")...\n" + 
	     "    " + ex.getMessage());
	}
	
	return group; 
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

      pDead = new TreeSet<String>();
      pHung = new TreeSet<String>();
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


    public TreeSet<String>
    getDead() 
    {
      return pDead;
    }

    public TreeSet<String>
    getHung() 
    {
      return pHung;
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
	try {
	  JobMgrControlClient client = new JobMgrControlClient(hname);
	  ResourceSample sample = client.getResources();
	  pSamples.put(hname, sample);
	  
	  if(pNeedsTotals.contains(hname)) {
	    pOsTypes.put(hname, client.getOsType());
	    pNumProcs.put(hname, client.getNumProcessors());
	    pTotalMemory.put(hname, client.getTotalMemory());
	    pTotalDisk.put(hname, client.getTotalDisk());
	  }
	  
	  client.disconnect();
	}
	catch(PipelineException ex) {
	  Throwable cause = ex.getCause();
	  if(cause instanceof SocketTimeoutException) {
	    pHung.add(hname);
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Net, LogMgr.Level.Severe,
	       ex.getMessage());
	    LogMgr.getInstance().flush();
	  }
	  else {
	    pDead.add(hname);
	  }
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

    private TreeSet<String>                 pDead; 
    private TreeSet<String>                 pHung; 
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
     TreeSet<String> aquiredKeys, 
     DoubleMap<OsType,String,String> envs
    ) 
    {
      super("QueueMgr:MonitorTask");

      pHostname    = hostname; 
      pHostOsType  = os; 
      pJob         = job; 
      pAquiredKeys = aquiredKeys; 
      pCookedEnvs  = envs; 
    }

    public void 
    run() 
    {
      long jobID = pJob.getJobID();

      TaskTimer timer = new TaskTimer("Monitor - Job " + jobID);

      /* attempt to start the job on the selected server, 
	   no environment means the job has been started previously */  
      QueueJobInfo startedInfo = null;
      boolean balked = false;
      if(pCookedEnvs != null) {
	timer.suspend();
	TaskTimer tm = new TaskTimer("Monitor - Job " + jobID + " [Start]"); 

	JobMgrPlgControlClient client = new JobMgrPlgControlClient(pHostname); 
	try {
          /* perform pre-start file system tasks */ 
          preStartFileOps();

          /* start the job */ 
	  client.jobStart(pJob, pCookedEnvs); 
 
	  tm.aquire(); 
	  synchronized(pJobInfo) {
	    tm.resume();
	    startedInfo = new QueueJobInfo(pJobInfo.get(jobID));
	  }
	}
	catch (Exception ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Job, LogMgr.Level.Severe,
	     ex.getMessage()); 
	  
	  Throwable cause = ex.getCause();
	  if(cause instanceof SocketTimeoutException) {
	    tm.aquire(); 
	    synchronized(pHosts) {
	      tm.resume();

	      QueueHost host = pHosts.get(pHostname);
	      if(host != null) {
                long now = System.currentTimeMillis();
		Long lastHung = host.getLastHung();
		if((lastHung == null) || ((lastHung + sDisableInterval) < now)) 
		  setHostStatus(host, QueueHost.Status.Hung);
		else 
		  setHostStatus(host, QueueHost.Status.Disabled);
	      }
	    }
	  }
	  
	  /* treat a failure to start as a preemption */ 
	  tm.aquire(); 
	  synchronized(pJobInfo) {
	    tm.resume();

	    QueueJobInfo info = pJobInfo.get(jobID);
	    info.preempted();
	    try {
	      writeJobInfo(info);
	    }
	    catch(PipelineException ex2) {
	      LogMgr.getInstance().log
		(LogMgr.Kind.Job, LogMgr.Level.Severe, 
		 ex.getMessage()); 
	    }

	    pJobCounters.update(tm, info);
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
	int tries = 0;
	boolean done = false;
	while(!done) {
	  JobMgrControlClient client = new JobMgrControlClient(pHostname);
	  try {
            /* wait for the job to finish */ 
	    results = client.jobWait(jobID);

            /* perform post-completion file system tasks */ 
            postFinishFileOps();

	    done = true;
	  }
	  catch(PipelineException ex) {
	    Throwable cause = ex.getCause();
	    if(cause instanceof SocketTimeoutException) {
	      tries++;
	      String msg = null;
	      if(tries < sMaxWaitReconnects) {
		msg = 
		  ("Unable to retrieved results for job (" + jobID + ") from " + 
		   "(" + pHostname + ") before the connection timed-out.\n" +
		   "Attempting to reconnect " + 
		   "(" + tries + " of " + sMaxWaitReconnects + ")..."); 
	      }
	      else {
		msg = 
		  ("Giving up retrieved results for job (" + jobID + ") from " +
		   "(" + pHostname + ") after making (" + tries + ") attempts!\n" + 
		   "Marking the job as Failed!");
		done = true;
	      }
	      
	      LogMgr.getInstance().log
		(LogMgr.Kind.Job, LogMgr.Level.Warning, msg);
	    }
	    else {
	      done = true;
	      LogMgr.getInstance().log
		(LogMgr.Kind.Job, LogMgr.Level.Severe,
		 ex.getMessage()); 
	    }
	  }
	  catch(Exception ex) {
	    done = true;
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Job, LogMgr.Level.Severe,
	       ex.getMessage()); 
	  }
	  finally {
	    client.disconnect();
	  }
	}
	tm.resume();
	
	/* update job information */
	tm.aquire(); 
	synchronized(pJobInfo) {
	  tm.resume();

	  try {
	    QueueJobInfo info = pJobInfo.get(jobID);
	    switch(info.getState()) {
	    case Preempted: 
	      preempted = true;
	      break;
	      
	    default:
	      info.exited(results);
	      pJobCounters.update(tm, info);
	      finishedInfo = new QueueJobInfo(info);
	    }
	    writeJobInfo(info);
	  }
	  catch (PipelineException ex) {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Job, LogMgr.Level.Severe,
	       ex.getMessage()); 
	  }	
	}
	
	LogMgr.getInstance().logSubStage
	  (LogMgr.Kind.Job, LogMgr.Level.Finer, 
	   tm, timer);
      }

      /* clean up... */ 
      {
	timer.suspend();
	TaskTimer tm = new TaskTimer("Monitor - Job " + jobID + " [Cleanup]"); 

	/* release any license keys */ 
	tm.aquire();
	synchronized(pLicenseKeys) {
	  tm.resume();

	  for(String kname : pAquiredKeys) {
	    LicenseKey key = pLicenseKeys.get(kname);
	    if(key != null) 
	      key.release(pHostname);
	  }
	}
	
	/* update the number of currently running jobs and release any ramp-up holds */ 
	tm.aquire();
	synchronized(pHosts) {
	  tm.resume();

	  QueueHost host = pHosts.get(pHostname);
	  if(host != null) {
	    host.jobFinished();
	    host.cancelHold(jobID);
	  }
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

    /**
     * Perform pre-start file system tasks.
     */ 
    private void 
    preStartFileOps()
      throws PipelineException
    {
      long jobID = pJob.getJobID();
      
      ActionAgenda agenda = new ActionAgenda(pJob.getActionAgenda(), pCookedEnvs);
      String author = agenda.getNodeID().getAuthor();
      Path wpath = new Path(PackageInfo.sProdPath, agenda.getNodeID().getWorkingParent());
      Path tpath = new Path(PackageInfo.sTargetPath, Long.toString(jobID));
      SortedMap<String,String> env = agenda.getEnvironment();

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
      long jobID = pJob.getJobID();

      ActionAgenda agenda = new ActionAgenda(pJob.getActionAgenda(), pCookedEnvs);
      String author = agenda.getNodeID().getAuthor();
      Path wpath = new Path(PackageInfo.sProdPath, agenda.getNodeID().getWorkingParent());
      Path tpath = new Path(PackageInfo.sTargetPath, Long.toString(jobID));
      SortedMap<String,String> env = agenda.getEnvironment();

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

	  ArrayList<String> args = new ArrayList<String>();
          for(Path target : agenda.getPrimaryTarget().getPaths()) {
            Path path = new Path(tpath, target); 
            if(path.toFile().isFile())
              args.add(path.toOsString()); 
          }
          
          for(FileSeq fseq : agenda.getSecondaryTargets()) {
            for(Path target : fseq.getPaths()) {
              Path path = new Path(tpath, target); 
              if(path.toFile().isFile())
                args.add(path.toOsString()); 
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
        }
      }

      /* make any existing target primary and secondary files read-only */ 
      {
        ArrayList<String> preOpts = new ArrayList<String>();
        preOpts.add("uga-w");
          
        ArrayList<String> args = new ArrayList<String>();
        for(Path target : agenda.getPrimaryTarget().getPaths()) {
          Path path = new Path(wpath, target); 
          if(path.toFile().isFile()) 
            args.add(target.toOsString()); 
        }
        
        for(FileSeq fseq : agenda.getSecondaryTargets()) {
          for(Path target : fseq.getPaths()) {
            Path path = new Path(wpath, target); 
            if(path.toFile().isFile()) 
              args.add(target.toOsString()); 
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

    private String                           pHostname; 
    private OsType                           pHostOsType; 
    private QueueJob                         pJob; 
    private TreeSet<String>                  pAquiredKeys; 
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

    public void 
    run() 
    {
      JobMgrControlClient client = null;
      try {
	client = new JobMgrControlClient(pHostname);	
	client.jobKill(pJobID);
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
   * The interval between attempts to automatically enable Hung job servers. 
   */ 
  private static final long  sUnhangInterval = 600000L;  /* 10-minutes */ 

  /**
   * Job servers Hung more than once within this interval will be changed to Disabled.
   */ 
  private static final long  sDisableInterval = 3600000L;  /* 60-minutes */ 

  /**
   * The number of dispatcher cycles between garbage collection of jobs.
   */ 
  private static final int  sGarbageCollectAfter = 600;  /* 10-minutes */ 

  /**
   * The minimum time a cycle of the scheduler loop should take (in milliseconds).
   */ 
  private static final long  sSchedulerInterval = 300000L;  /* 5-minutes */ 

  /**
   * The maximum number of times to attempt to reconnect to a job server while waiting
   * on the results of a job. 
   */ 
  private static final int  sMaxWaitReconnects = 5;

  /**
   * The time (in milliseconds) between reports of the JVM heap statistics.
   */ 
  private static long  sHeapStatsInterval = 900000L;  /* 15-minutes */



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent queue mananger server. 
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
 

  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The combined work groups and adminstrative privileges.
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
   * Pending hosts marked as hung.<P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */ 
  private TreeSet<String> pHungChanges;

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
   * The minimum time a cycle of the dispatcher loop should take (in milliseconds).
   */ 
  private AtomicLong  pDispatcherInterval; 

  /**
   * The IDs of jobs which should be preempted as soon as possible. 
   * 
   * No locking is required.
   */
  private ConcurrentLinkedQueue<Long>  pPreemptList;
  
  /**
   * The IDs of jobs which should be killed as soon as possible. 
   * 
   * No locking is required.
   */
  private ConcurrentLinkedQueue<Long>  pHitList;
  
  /**
   * The IDs of jobs which are currently paused.
   * 
   * Access to this field should be protected by a synchronized block.
   */
  private TreeSet<Long>  pPaused;

  /**
   * The IDs of jobs waiting on one or more source (upstream) jobs to complete before they 
   * can be added to the ready queue.  This list also contains the IDs of jobs newly 
   * submitted to the queue which may not have any source nodes. <P> 
   * 
   * No locking is required.
   */ 
  private ConcurrentLinkedQueue<Long>  pWaiting;

  /**
   * The IDs of the jobs which are ready to be run. <P> 
   * 
   * If a ready job has any source (upstream) jobs, they all must have a JobState of 
   * Finished before the job will be added to this table. <P> 
   * 
   * No locking is required, since this field is only accessed by the dispather() method.
   */ 
  private TreeSet<Long>  pReady;

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
   * Access to this field should be protected by a synchronized block.
   */ 
  private TreeMap<Long,QueueJobInfo>  pJobInfo; 


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
   * Counts of the Running, Finished and total number of jobs in each job group.
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
   *   synchronized(pHostsMod) {
   *     ...
   *   }
   *   synchronized(pHosts) {
   *     ...
   *   }
   * }
   * 
   * synchronized(pHosts) {
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
   *   synchronized(pHungChanges) {
   *     ...
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
   *     }
   *   }
   * }
   */

}

