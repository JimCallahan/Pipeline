// $Id: QueueMgr.java,v 1.20 2004/10/28 17:06:13 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.message.*;
import us.temerity.pipeline.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   M G R                                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The manager of queue jobs. <P> 
 * 
 * 
 * 
 * @see QueueMgrClient
 * @see QueueMgrServer
 */
public
class QueueMgr
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new queue manager.
   * 
   * @param dir 
   *   The root queue directory.
   * 
   * @param jobPort 
   *   The network port listened to by the <B>pljobmgr</B><A>(1) daemons.
   */
  public
  QueueMgr
  (
   File dir,
   int jobPort
  )
  { 
    if(dir == null)
      throw new IllegalArgumentException("The root queue directory cannot be (null)!");
    pQueueDir = dir;

    if(jobPort < 0) 
      throw new IllegalArgumentException("Illegal port number (" + jobPort + ")!");
    pJobPort = jobPort;

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

      pPrivilegedUsers = new TreeSet<String>();

      pLicenseKeys   = new TreeMap<String,LicenseKey>();
      pSelectionKeys = new TreeMap<String,SelectionKey>();

      pHosts           = new TreeMap<String,QueueHost>(); 
      pLastSampleWrite = new Date();
      pSampleFileLock  = new Object();

      pHitList   = new ConcurrentLinkedQueue<Long>();
      pPaused    = new TreeSet<Long>();

      pWaiting = new ConcurrentLinkedQueue<Long>();
      pReady   = new TreeMap<Integer,TreeSet<Long>>(Collections.reverseOrder());

      pJobFileLocks = new TreeMap<Long,Object>();
      pJobs         = new TreeMap<Long,QueueJob>(); 

      pJobInfoFileLocks = new TreeMap<Long,Object>();
      pJobInfo          = new TreeMap<Long,QueueJobInfo>();

      pNodeJobIDs = new TreeMap<NodeID,TreeMap<File,Long>>(); 

      pJobGroupFileLocks = new TreeMap<Long,Object>();
      pJobGroups         = new TreeMap<Long,QueueJobGroup>();
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
      
      /* load the license keys if any exist */ 
      readLicenseKeys();

      /* load the selection keys if any exist */ 
      readSelectionKeys();

      /* load the hosts if any exist */ 
      readHosts();

      /* initialize the job related tables from disk files */ 
      initJobTables();
    }
    catch(Exception ex) {
      Logs.ops.severe(ex.getMessage());
      Logs.flush();
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
   * Initialize the job related tables from disk files.
   */ 
  private void 
  initJobTables() 
    throws PipelineException
  {
    /* read the existing queue jobs files (in oldest to newest order) */ 
    TreeMap<Long,String> running = new TreeMap<Long,String>();
    {
      File dir = new File(pQueueDir, "queue/jobs");
      File files[] = dir.listFiles(); 
      int wk;
      for(wk=0; wk<files.length; wk++) {
	if(files[wk].isFile()) {
	  try {
	    Long jobID = new Long(files[wk].getName());
	    QueueJob job = readJob(jobID);
	    QueueJobInfo info = readJobInfo(jobID);
	    assert((job != null) && (info != null));

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
	    Logs.glu.severe("Illegal job file encountered (" + files[wk] + ")!");
	  }
	  catch(PipelineException ex) {
	    Logs.ops.severe(ex.getMessage());
	  }
	}
      }
    }

    /* initialize the job groups */ 
    {
      File dir = new File(pQueueDir, "queue/job-groups");
      File files[] = dir.listFiles(); 
      int wk;
      for(wk=0; wk<files.length; wk++) {
	if(files[wk].isFile()) {
	  try {
	    Long groupID = new Long(files[wk].getName());
	    QueueJobGroup group = readJobGroup(groupID);
	    assert(group != null);
	    synchronized(pJobGroups) {
	      pJobGroups.put(groupID, group);
	    }
	  }
	  catch(NumberFormatException ex) {
	    Logs.ops.severe("Illegal job group file encountered (" + files[wk] + ")!");
	  }
	}
      }
    }
    
    /* garbage collect all jobs no longer referenced by a job group */ 
    garbageCollectJobs(new TaskTimer());

    /* start tasks to record the results of the already running jobs */ 
    for(Long jobID : running.keySet()) {
      boolean stillExists = false;
      synchronized(pJobs) {
	stillExists = pJobs.containsKey(jobID);
      }

      if(stillExists) {
	String hostname = running.get(jobID);
	
	WaitTask task = new WaitTask(hostname, jobID);
	task.start();
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S H U T D O W N                                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Shutdown the node manager. <P> 
   * 
   * It is crucial that this method be called when only a single thread is able to access
   * this instance!  In other words, after all request threads have already exited or by a 
   * restart during the construction of this instance.
   */
  public void  
  shutdown() 
  {
    /* write the last interval of samples to disk */ 
    {
      Date now = new Date();
      TreeMap<String,ResourceSampleBlock> dump = new TreeMap<String,ResourceSampleBlock>();
      for(String hname : pHosts.keySet()) {
	QueueHost host = pHosts.get(hname);
	if(host != null) {
	  switch(host.getStatus()) {
	  case Enabled:
	    {
	      ArrayList<ResourceSample> rs = new ArrayList<ResourceSample>();
	      for(ResourceSample s : host.getSamples()) {
		if(s.getTimeStamp().compareTo(pLastSampleWrite) > 0) 
		  rs.add(s);
	      }
	      
	      if(!rs.isEmpty() && 
		 (host.getNumProcessors() != null) && 
		 (host.getTotalMemory() != null) && 
		 (host.getTotalDisk() != null)) {
		
		ResourceSampleBlock block = 
		  new ResourceSampleBlock(host.getJobSlots(), 
					  host.getNumProcessors(), 
					  host.getTotalMemory(), 
					  host.getTotalDisk(), 
					  rs);
		dump.put(hname, block);
	      }
	    }
	  }
	}
      }

      if(!dump.isEmpty()) {
	try {
	  writeSamples(new TaskTimer(), now, dump);
	}
	catch(PipelineException ex) {
	  Logs.ops.severe(ex.getMessage());
	}
      }
    }

    /* remove the lock file */ 
    {
      File file = new File(pQueueDir, "queue/lock");
      file.delete();
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
    TaskTimer timer = new TaskTimer("QueueMgr.getPrivilegedUsers()");
    
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
  setPrivilegedUsers
  ( 
   MiscSetPrivilegedUsersReq req
  ) 
  {
    TaskTimer timer = new TaskTimer("MasterMgr.setPrivilegedUsers()");
    
    timer.aquire();
    synchronized(pPrivilegedUsers) {
      timer.resume();	

      pPrivilegedUsers.clear();
      pPrivilegedUsers.addAll(req.getUsers());

      return new SuccessRsp(timer);
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
   * Set the total number of licenses associated with the named license key. <P> 
   * 
   * @param req
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to set the total licenses.
   */ 
  public Object
  setTotalLicenses
  (
   QueueSetTotalLicensesReq req
  ) 
  {
    String kname = req.getKeyName();
    int total = req.getTotal();

    TaskTimer timer = 
      new TaskTimer("QueueMgr.setTotalLicenses(): " + kname + "[" + total + "]"); 
    timer.aquire();
    try {
      synchronized(pLicenseKeys) {
	timer.resume();
	
	LicenseKey key = pLicenseKeys.get(kname);
	if(key == null) 
	  throw new PipelineException
	    ("Unable to set the total number of licenses because no license key " + 
	     "named (" + kname + ") exists!");
	
	try {
	  key.setTotal(total);
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
  /*   L I C E N S E   K E Y S                                                              */
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
      synchronized(pSelectionKeys) {
	timer.resume();
	
	pSelectionKeys.remove(kname);
	writeSelectionKeys();

	return new SuccessRsp(timer);
      }
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }
  }  


     
  /*----------------------------------------------------------------------------------------*/
  /*   J O B   M A N A G E R   H O S T S                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current state of the hosts capable of executing jobs for the Pipeline queue.
   * 
   * @return
   *   <CODE>QueueGetHostsRsp</CODE> if successful.
   */ 
  public Object
  getHosts() 
  {
    TaskTimer timer = new TaskTimer();
    timer.aquire();
    synchronized(pHosts) {
      timer.resume();
      
      return new QueueGetHostsRsp(timer, pHosts);
    }
  }
  
  /**
   * Add a new execution host to the Pipeline queue. <P> 
   * 
   * The host will be added in a <CODE>Shutdown</CODE> state, unreserved and with no 
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
    timer.aquire();
    try {
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
	pHosts.put(hname, new QueueHost(hname));

	writeHosts();

	return new SuccessRsp(timer);
      }
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }    
  }

  /**
   * Remove the given existing execution hosts from the Pipeline queue. <P> 
   * 
   * The hosts must be in a <CODE>Shutdown</CODE> state before they can be removed. <P> 
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
    timer.aquire();
    try {
      synchronized(pHosts) {
	timer.resume();
	
	for(String hname : req.getHostnames()) {
	  QueueHost host = pHosts.get(hname);
	  if(host != null) {
	    switch(host.getStatus()) {
	    case Shutdown:
	    case Hung:
	      pHosts.remove(hname);
	      break;

	    default:
	      throw new PipelineException
		("Unable to remove host (" + hname + ") until it is Shutdown!");
	    }
	  }
	}

	writeHosts();

	return new SuccessRsp(timer);
      }
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
   * For the given hosts, the selection key biases are set to the values passed in the 
   * <CODE>biases</CODE> argument and all selection key biases not mentioned will be removed.
   * Hosts not mention in the <CODE>biases</CODE> argument will be unaltered. <P> 
   * 
   * For an detailed explanation of how selection keys are used to determine the assignment
   * of jobs to hosts, see {@link JobReqs JobReqs}. <P> 
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

    TreeSet<String> keys = null;
    timer.aquire();
    synchronized(pSelectionKeys) {
      timer.resume();
      keys = new TreeSet<String>(pSelectionKeys.keySet());
    }

    timer.aquire();
    try {
      synchronized(pHosts) {
	timer.resume();
	
	boolean modified = false;
	
	/* status */ 
	{
	  TreeMap<String,QueueHost.Status> table = req.getStatus();
	  if(table != null) {
	    for(String hname : table.keySet()) {
	      QueueHost.Status status = table.get(hname);
	      QueueHost host = pHosts.get(hname);
	      if((status != null) && (host != null) && (status != host.getStatus())) {
		switch(status) {
		case Disabled:
		case Enabled:
		  try {
		    JobMgrControlClient client = new JobMgrControlClient(hname, pJobPort);
		    client.verifyConnection();
		    client.disconnect();
		  }
		  catch(PipelineException ex) {
		    status = QueueHost.Status.Shutdown;
		  }	    	    
		}
		
		switch(status) {
		case Shutdown:
		  try {
		    JobMgrControlClient client = new JobMgrControlClient(hname, pJobPort);
		    client.shutdown();
		  }
		  catch(PipelineException ex) {
		  }	    
		}
		
		host.setStatus(status);
	      }
	    }
	  }
	}
	
	/* user reservations */ 
	{
	  TreeMap<String,String> table = req.getReservations();
	  if(table != null) {
	    for(String hname : table.keySet()) {
	      QueueHost host = pHosts.get(hname);
	      if(host != null) {
		host.setReservation(table.get(hname));
		modified = true;
	      }
	    }
	  }
	}

	/* job slots */ 
	{
	  TreeMap<String,Integer> table = req.getJobSlots();
	  if(table != null) {
	    for(String hname : table.keySet()) {
	      QueueHost host = pHosts.get(hname);
	      if(host != null) {
		host.setJobSlots(table.get(hname));
		modified = true;
	      }
	    }
	  }
	}
	
	/* selection key biases */ 
	{
	  TreeMap<String,TreeMap<String,Integer>> table = req.getBiases();
	  if(table != null) {
	    for(String hname : table.keySet()) {
	      QueueHost host = pHosts.get(hname);
	      if(host != null) {
		host.removeAllSelectionKeys();
		
		TreeMap<String,Integer> biases = table.get(hname);
		for(String kname : biases.keySet()) 
		  if(keys.contains(kname)) 
		    host.addSelectionKey(kname, biases.get(kname));
		
		modified = true;	      
	      }
	    }
	  }
	}
      
	/* write changes to disk */ 
	if(modified) 
	  writeHosts();
      }
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }    
    
    return new SuccessRsp(timer);
  }


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
    timer.aquire();
    try {
      timer.resume();
      
      ArrayList<ResourceSampleBlock> all = new ArrayList<ResourceSampleBlock>();
      {
	TreeMap<Long,ResourceSampleBlock> blocks = readSamples(timer, req.getHostname()); 
	all.addAll(blocks.values());

	Date latest = null;
	if(!blocks.isEmpty()) {
	  ResourceSampleBlock lastBlock = blocks.get(blocks.lastKey());
	  latest = lastBlock.getTimeStamp(lastBlock.getNumSamples()-1);
	}

	timer.aquire();
	synchronized(pHosts) {
	  timer.resume();
	  
	  QueueHost host = pHosts.get(req.getHostname());
	  ArrayList<ResourceSample> samples = new ArrayList<ResourceSample>();

	  if(latest != null) {
	    for(ResourceSample sample : host.getSamples()) {
	      if(sample.getTimeStamp().compareTo(latest) > 0) 
		samples.add(sample);
	    }
	  }
	  else {
	    samples.addAll(host.getSamples());
	  }
	  
	  if(!samples.isEmpty() && 
	     (host.getNumProcessors() != null) && 
	     (host.getTotalMemory() != null) && 
	     (host.getTotalDisk() != null)) {
	    
	    ResourceSampleBlock block = 
	      new ResourceSampleBlock(host.getJobSlots(), 
				      host.getNumProcessors(), 
				      host.getTotalMemory(), 
				      host.getTotalDisk(), 
				      samples);
	    all.add(block);
	  }	
	}
      }

      if(all.isEmpty()) 
	throw new PipelineException 
	  ("No resource usage information exists for host (" + req.getHostname() + ")!");

      return new QueueGetHostResourceSamplesRsp(timer, new ResourceSampleBlock(all));
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
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
    Date stamp = req.getTimeStamp();

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
	  
	  if((info != null) && (info.getSubmittedStamp().compareTo(stamp) > 0))
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
	  JobStatus js = 
	    new JobStatus(jobID, job.getNodeID(), state, 
			  job.getActionAgenda().getPrimaryTarget(), job.getSourceJobIDs());
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
	  JobStatus status = 
	    new JobStatus(jobID, job.getNodeID(), JobState.Running, 
			  job.getActionAgenda().getPrimaryTarget(), job.getSourceJobIDs());
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
   * Submit a job to be executed by the queue. <P> 
   * 
   * @param req 
   *   The job submission request.
   *    
   * @return 
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable submit the job.
   */ 
  public Object
  submitJob
  (
   QueueSubmitJobReq req
  )
  {
    // DEBUGGING
    try {
      GlueEncoder ge = new GlueEncoderImpl("QueueJob", req.getJob());
      Logs.glu.finest(ge.getText());
    }
    catch(GlueException ex) {
      Logs.glu.severe
	("Unable to generate a Glue format representation of the job!");
    }
    Logs.flush();
    // DEBUGGING
    
    QueueJob job = req.getJob();
    long jobID = job.getJobID();
    QueueJobInfo info = new QueueJobInfo(jobID);

    TaskTimer timer = new TaskTimer("QueueMgr.submitJob(): " + jobID);
    try {
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
      }
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
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

    return new SuccessRsp(timer);
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
   QueueKillJobsReq req
  )
  {
    TaskTimer timer = new TaskTimer("QueueMgr.killJobs()");

    for(Long jobID : req.getJobIDs())
      pHitList.add(jobID);

    return new SuccessRsp(timer);
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
   QueuePauseJobsReq req
  )
  {
    TaskTimer timer = new TaskTimer("QueueMgr.pauseJobs()");

    timer.aquire();
    synchronized(pPaused) {
      timer.resume();
      for(Long jobID : req.getJobIDs())
	pPaused.add(jobID);
    }

    return new SuccessRsp(timer);
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
   QueueResumeJobsReq req
  )
  {
    TaskTimer timer = new TaskTimer("QueueMgr.resumeJobs()");

    timer.aquire();
    synchronized(pPaused) {
      timer.resume();
      for(Long jobID : req.getJobIDs())
	pPaused.remove(jobID);
    }

    return new SuccessRsp(timer);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Notify the queue that a set of previously submitted jobs make up a job group.
   * 
   * @param req 
   *   The group jobs equest.
   *    
   * @return 
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable group the jobs. 
   */ 
  public Object
  groupJobs
  (
   QueueGroupJobsReq req
  )
  {
    // DEBUGGING
    try {
      GlueEncoder ge = new GlueEncoderImpl("QueueJobGroup", req.getJobGroup());
      Logs.glu.finest(ge.getText());
    }
    catch(GlueException ex) {
      Logs.glu.severe
	("Unable to generate a Glue format representation of the job group!");
    }
    Logs.flush();
    // DEBUGGING

    QueueJobGroup group = req.getJobGroup();
    long groupID = group.getGroupID();
    
    TaskTimer timer = new TaskTimer("QueueMgr.groupJobs(): " + groupID);
    try {
      timer.aquire();
      synchronized(pJobGroups) {
	timer.resume();
	writeJobGroup(group);
	pJobGroups.put(groupID, group);
      }
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }   

    return new SuccessRsp(timer);
  }

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
    timer.aquire();
    synchronized(pJobGroups) {
      timer.resume();
      try {
	TreeMap<Long,String> groupAuthors = req.getGroupAuthors();
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
      catch(PipelineException ex) {
	return new FailureRsp(timer, ex.getMessage());	  
      }   
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
      
      return new SuccessRsp(timer);
    }
  }

  /**
   * Delete all of the completed job groups in all working areas. <P> 
   * 
   * @return 
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to delete the job groups.
   */ 
  public Object
  deleteAllJobGroups() 
  {
    TaskTimer timer = new TaskTimer("QueueMgr.deleteAllJobGroups()");
    timer.aquire();
    synchronized(pJobGroups) {
      timer.resume();

      ArrayList<QueueJobGroup> dead = new ArrayList<QueueJobGroup>();
      for(Long groupID : pJobGroups.keySet()) 
	dead.add(pJobGroups.get(groupID));

      for(QueueJobGroup group : dead) {      
	try {
	  deleteCompletedJobGroup(timer, group);
	}
	catch(PipelineException ex) {
	}
      }

      return new SuccessRsp(timer);
    } 
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
    pJobGroups.remove(groupID);
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
    TaskTimer timer = new TaskTimer("QueueMgr.collector()");

    /* get the named of the currently enabled hosts 
         and the names of those enabled hosts for which total memory/disk isn't known */ 
    TreeSet<String> enabled = new TreeSet<String>();
    TreeSet<String> needsTotals = new TreeSet<String>();
    {
      timer.aquire();
      synchronized(pHosts) {
	timer.resume();
	for(String hname : pHosts.keySet()) {
	  QueueHost host = pHosts.get(hname);
	  switch(host.getStatus()) {
	  case Enabled:
	    enabled.add(hname);
	    
	    if((host.getNumProcessors() == null) || 
	       (host.getTotalMemory() == null) ||
	       (host.getTotalDisk() == null)) 
	      needsTotals.add(hname);
	  }
	}
      }
    }

    /* collect system resource usage samples from the enabled hosts */ 
    TreeSet<String> dead = new TreeSet<String>();
    TreeSet<String> hung = new TreeSet<String>();
    TreeMap<String,ResourceSample> samples = new TreeMap<String,ResourceSample>();
    TreeMap<String,Integer> numProcs = new TreeMap<String,Integer>();
    TreeMap<String,Long> totalMemory = new TreeMap<String,Long>();
    TreeMap<String,Long> totalDisk = new TreeMap<String,Long>();
    for(String hname : enabled) {
      try {
	JobMgrControlClient client = new JobMgrControlClient(hname, pJobPort);

	samples.put(hname, client.getResources());

 	if(needsTotals.contains(hname)) {
	  numProcs.put(hname, client.getNumProcessors());
 	  totalMemory.put(hname, client.getTotalMemory());
 	  totalDisk.put(hname, client.getTotalDisk());
	}

	client.disconnect();
      }
      catch(PipelineException ex) {
	Throwable cause = ex.getCause();
	if(cause instanceof SocketTimeoutException) {
	  hung.add(hname);
	  Logs.net.severe(ex.getMessage());
	  Logs.flush();
	}
	else 
	  dead.add(hname);
      }	    
    }

    /* should the last interval of samples be written to disk? */ 
    Date now = new Date();
    TreeMap<String,ResourceSampleBlock> dump = null;
    {
      if((now.getTime() - pLastSampleWrite.getTime()) > QueueHost.getSampleInterval())
	dump = new TreeMap<String,ResourceSampleBlock>();
    }

    /* update the hosts */ 
    timer.aquire();
    synchronized(pHosts) {
      timer.resume();
      for(String hname : dead) {
	QueueHost host = pHosts.get(hname);
	if(host != null) 
	  host.setStatus(QueueHost.Status.Shutdown);
      }

      for(String hname : hung) {
	QueueHost host = pHosts.get(hname);
	if(host != null) 
	  host.setStatus(QueueHost.Status.Hung);
      }

      for(String hname : samples.keySet()) {
	QueueHost host = pHosts.get(hname);
	if(host != null) {
	  switch(host.getStatus()) {
	  case Enabled:
	    {
	      ResourceSample sample = samples.get(hname);
	      if(sample != null)
		host.addSample(sample);

	      if(dump != null) {
		ArrayList<ResourceSample> rs = new ArrayList<ResourceSample>();
		for(ResourceSample s : host.getSamples()) {
		  if(s.getTimeStamp().compareTo(pLastSampleWrite) > 0) 
		    rs.add(s);
		}

		if(!rs.isEmpty() && 
		   (host.getNumProcessors() != null) && 
		   (host.getTotalMemory() != null) && 
		   (host.getTotalDisk() != null)) {

		  ResourceSampleBlock block = 
		    new ResourceSampleBlock(host.getJobSlots(), 
					    host.getNumProcessors(), 
					    host.getTotalMemory(), 
					    host.getTotalDisk(), 
					    rs);
		  dump.put(hname, block);
		}
	      }

	      Integer procs = numProcs.get(hname);
	      if(procs != null) 
		host.setNumProcessors(procs);

	      Long memory = totalMemory.get(hname);
	      if(memory != null) 
		host.setTotalMemory(memory);

	      Long disk = totalDisk.get(hname);
	      if(disk != null) 
		host.setTotalDisk(disk);
	    }
	  }
	}
      }
    }

    /* write the last interval of samples to disk */ 
    if(dump != null) {
      try {
	writeSamples(timer, now, dump);
      }
      catch(PipelineException ex) {
	Logs.ops.severe(ex.getMessage());
      }
      
      pLastSampleWrite = now;
    }

    /* cleanup any out-of-date sample files */ 
    cleanupSamples(timer);

    Logs.ops.finest(timer.toString()); 
    if(Logs.ops.isLoggable(Level.FINEST))
      Logs.flush();

    /* if we're ahead of schedule, take a nap */ 
    {
      long nap = sCollectorInterval - timer.getTotalDuration();
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
  /*   D I S P A T C H E R                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Perform one round of assigning jobs to available hosts. 
   */ 
  public void 
  dispatcher()
  {
    TaskTimer timer = new TaskTimer("QueueMgr.dispatcher()");
    
    //Logs.ops.finest("-------------------------------------------------------------------"); 
    //logJobLists("Pre-Dispatch:");

    /* kill/abort the jobs in the hit list */ 
    while(true) {
      Long jobID = pHitList.poll();
      if(jobID == null) 
	break;

      QueueJobInfo info = null;
      timer.aquire();
      synchronized(pJobInfo) {
	timer.resume();
	info = pJobInfo.get(jobID);
      }

      if(info != null) {
	switch(info.getState()) {
	case Queued:
	case Paused:
	  info.aborted();
	  try {
	    writeJobInfo(info);
	  }
	  catch(PipelineException ex) {
	    Logs.net.severe(ex.getMessage()); 
	    Logs.flush();
	  }
	  break; 

	case Running:
	  {
	    KillTask task = new KillTask(info.getHostname(), jobID);
	    task.start();
	  }
	}
      }
    }

    //logJobLists("After Kills:");

    /* process the waiting jobs: sorting jobs into killed/aborted, ready and waiting */ 
    {
      LinkedList<Long> waiting = new LinkedList<Long>();
      while(true) {
	Long jobID = pWaiting.poll();
	if(jobID == null) 
	  break;
	
	QueueJobInfo info = null;
	timer.aquire();
	synchronized(pJobInfo) {
	  timer.resume();
	  info = pJobInfo.get(jobID);
	}
	
	if(info != null) {
	  switch(info.getState()) {
	  case Queued:
	    {
	      if(pPaused.contains(jobID)) {
		timer.aquire();
		synchronized(pJobInfo) {
		  timer.resume();

		  info.paused();
		  try {
		    writeJobInfo(info);
		  }
		  catch(PipelineException ex) {
		    Logs.net.severe(ex.getMessage()); 
		    Logs.flush();
		  }
		}

		waiting.add(jobID);
		break;
	      }

	      QueueJob job = null;
	      timer.aquire();
	      synchronized(pJobs) {
		timer.resume();
		job = pJobs.get(jobID);
	      }

	      if(job != null) {
		boolean ready = true;
		boolean done = false;
		for(Long sjobID : job.getSourceJobIDs()) {
		  QueueJobInfo sinfo = null;
		  timer.aquire();
		  synchronized(pJobInfo) {
		    timer.resume();
		    sinfo = pJobInfo.get(sjobID);
		  }
		  
		  if(sinfo != null) {
		    switch(sinfo.getState()) {
		    case Queued:
		    case Paused:
		    case Running:
		      waiting.add(jobID);
		      ready = false;
		      done = true;
		      break;

		    case Aborted:
		    case Failed:
		      pHitList.add(jobID);
		      ready = false;
		      done = true;
		    }
		  }
		    
		  if(done) 
		    break;
		}

		if(ready) {
		  int priority = job.getJobRequirements().getPriority();
		  TreeSet<Long> ids = pReady.get(priority);
		  if(ids == null) {
		    ids = new TreeSet<Long>();
		    pReady.put(priority, ids);
		  }
		    
		  ids.add(jobID);
		}
	      }
	    }
	    break;

	  case Paused:
	    {
	      if(!pPaused.contains(jobID)) {
		timer.aquire();
		synchronized(pJobInfo) {
		  timer.resume();

		  info.resumed();
		  try {
		    writeJobInfo(info);
		  }
		  catch(PipelineException ex) {
		    Logs.net.severe(ex.getMessage()); 
		    Logs.flush();
		  }
		}
	      }

	      waiting.add(jobID);
	    }
	  }
	}
      }

      pWaiting.addAll(waiting);      
    }

    //logJobLists("After Sort:");

    /* process the ready jobs from highest to lowest priority */ 
    {
      TreeSet<Integer> empty = new TreeSet<Integer>();
      for(Integer priority : pReady.keySet()) {
	TreeSet<Long> jobIDs = pReady.get(priority);
	if(jobIDs == null) {
	  empty.add(priority);
	}
	else {
	  TreeSet<Long> processed = new TreeSet<Long>();
	  for(Long jobID : jobIDs) {
	    QueueJob job = null;
	    timer.aquire();
	    synchronized(pJobs) {
	      timer.resume();
	      job = pJobs.get(jobID);
	    }

	    QueueJobInfo info = null;
	    timer.aquire();
	    synchronized(pJobInfo) {
	      timer.resume();
	      info = pJobInfo.get(jobID);
	    }

	    if((job == null) || (info == null)) {
	      processed.add(jobID);
	    }
	    else {
	      switch(info.getState()) {
	      case Queued:
		if(pPaused.contains(jobID)) {
		  timer.aquire();
		  synchronized(pJobInfo) {
		    timer.resume();

		    info.paused();
		    try {
		      writeJobInfo(info);
		    }
		    catch(PipelineException ex) {
		      Logs.net.severe(ex.getMessage()); 
		      Logs.flush();
		    }
		  }

		  pWaiting.add(jobID);
		  processed.add(jobID);
		}
		else if(dispatchJob(job, info, timer)) {
		  processed.add(jobID);
		}
		break;

	      case Aborted:
		processed.add(jobID);
		break;
		
	      default:
		assert(false);
	      }
	    }
	  }
	  
	  jobIDs.removeAll(processed);
	  if(jobIDs.isEmpty()) 
	    empty.add(priority);
	}
      }

      for(Integer priority : empty) 
	pReady.remove(priority);
    }

    /* check for newly completed job groups */ 
    timer.aquire();
    synchronized(pJobGroups) {
      timer.resume();
      for(Long groupID : pJobGroups.keySet()) {
	QueueJobGroup group = pJobGroups.get(groupID);
	
	/* the job is not yet completed */ 
	if((group != null) && (group.getCompletedStamp() == null)) {
	  boolean done = true; 
	  Date latest = null;
	  for(Long jobID : group.getAllJobIDs()) {
	    QueueJobInfo info = null;
	    timer.aquire();
	    synchronized(pJobInfo) {
	      timer.resume();
	      info = pJobInfo.get(jobID);
	    }

	    if(info != null) {
	      switch(info.getState()) {
	      case Queued:
	      case Paused:
	      case Running:
		done = false;
		break;

	      default: 
		{
		  Date stamp = info.getCompletedStamp(); 
		  if(latest == null) 
		    latest = stamp; 
		  else if(latest.compareTo(stamp) < 0)
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
	      Logs.ops.severe(ex.getMessage());
	    }
	  }
	}
      }
    }

    //logJobLists("Post-Dispatch:");
    //Logs.ops.finest("-------------------------------------------------------------------"); 

    /* perform garbage collection of jobs at regular intervals */ 
    pDispatcherCycles++;
    if(pDispatcherCycles > sGarbageCollectAfter) {
      timer.suspend();

      TaskTimer gtimer = new TaskTimer("QueueMgr.garbageCollectJobs()");
      garbageCollectJobs(gtimer);

      timer.accum(gtimer);

      pDispatcherCycles = 0;
    }

    Logs.ops.finest(timer.toString()); 
    if(Logs.ops.isLoggable(Level.FINEST))
      Logs.flush();

    /* if we're ahead of schedule, take a nap */ 
    {
      long nap = sDispatcherInterval - timer.getTotalDuration();
      if(nap > 0) {
	try {
	  Thread.sleep(nap);
	}
	catch(InterruptedException ex) {
	}
      }
    }
  }

  /**
   * Attempt to find a job server which meets the requirements of the given job. <P> 
   * 
   * If a suitable job server is found, the job will be started on that server and a task
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
   * @param timer
   *   The task timer.
   * 
   * @return 
   *   Whether the job assigned to a server and started.
   */ 
  private boolean
  dispatchJob
  (
   QueueJob job, 
   QueueJobInfo info, 
   TaskTimer timer
  ) 
  {
    JobReqs jreqs = job.getJobRequirements();

    /* aquire the jobs license keys, 
         aborts early if unable to aquire all keys required by the job */ 
    TreeSet<String> aquiredLicenseKeys = new TreeSet<String>();
    {
      boolean available = true;
      timer.aquire();
      synchronized(pLicenseKeys) {
	timer.resume();
	for(String kname : jreqs.getLicenseKeys()) {
	  LicenseKey key = pLicenseKeys.get(kname);
	  if(key == null) {
	    available = false; 
	    break;
	  }
	  else {
	    if(key.aquire()) 
	      aquiredLicenseKeys.add(kname);
	    else {
	      available = false; 
	      break;
	    }
	  }
	}

	if(!available) {
	  for(String kname : aquiredLicenseKeys) 
	    pLicenseKeys.get(kname).release();
	  return false;
	}
      }
    }

    /* determine the name of the job server which meets the dynamic resource requirements
         and has the highest selection bias */ 
    String bestHost = null;
    {
      TreeSet<String> keys = new TreeSet<String>();
      timer.aquire();
      synchronized(pSelectionKeys) {
	timer.resume();
	keys.addAll(pSelectionKeys.keySet());
      }

      int maxBias = -1;
      timer.aquire();
      synchronized(pHosts) {
	timer.resume();
	for(String hostname : pHosts.keySet()) {
	  QueueHost host = pHosts.get(hostname);
	  switch(host.getStatus()) {
	  case Enabled:
	    {
	      String author = job.getActionAgenda().getNodeID().getAuthor();
	      Integer bias = host.computeJobBias(author, jreqs, keys);
	      if((bias != null) && (bias > maxBias)) {
		bestHost = hostname; 
		maxBias  = bias;
	      }
	    }
	  }
	}
      }

      if(bestHost == null) {
	timer.aquire();
	synchronized(pLicenseKeys) {
	  timer.resume();
	  for(String kname : aquiredLicenseKeys) 
	    pLicenseKeys.get(kname).release();
	}
	return false;
      }
    }

    /* start the job on the selected server */ 
    {
      JobMgrControlClient client = null;
      try {
	client = new JobMgrControlClient(bestHost, pJobPort);	
	int numJobs = client.jobStart(job);
	
	timer.aquire();
	synchronized(pJobInfo) {
	  timer.resume();
	  info.started(bestHost);
	  writeJobInfo(info);
	}
	
	timer.aquire();
	synchronized(pHosts) {
	  timer.resume();
	  QueueHost host = pHosts.get(bestHost);
	  ResourceSample sample = host.getLatestSample();
	  sample.setNumJobs(numJobs);
	}
      }
      catch (Exception ex) {
	Logs.net.severe(ex.getMessage()); 
	Logs.flush();

	timer.aquire();
	synchronized(pLicenseKeys) {
	  timer.resume();
	  for(String kname : aquiredLicenseKeys) 
	    pLicenseKeys.get(kname).release();
	}
	return false;
      }
      finally {
	if(client != null)
	  client.disconnect();
      }
    }

    /* start a task to collect the results of the execution */ 
    {
      WaitTask task = new WaitTask(bestHost, job.getJobID());
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
    for(Long jobID : dead) {
      timer.aquire();
      synchronized(pJobs) {
	timer.resume();
	try {
	  if(pJobs.remove(jobID) != null) 
	    deleteJobFile(jobID);
	}
	catch(PipelineException ex) {
	  Logs.ops.severe(ex.getMessage());
	  Logs.flush();
	}
      }

      timer.aquire();
      synchronized(pJobInfo) {
	timer.resume();
	try {
	  if(pJobInfo.remove(jobID) != null) 
	    deleteJobInfoFile(jobID);
	}
	catch(PipelineException ex) {
	  Logs.ops.severe(ex.getMessage());
	  Logs.flush();
	}
      }      
    }

    /* tell the job servers to cleanup any resources associated with the dead jobs */ 
    {
      CleanupJobResourcesTask task = new CleanupJobResourcesTask(live);
      task.start();
    }

    Logs.ops.finer(timer.toString()); 
    if(Logs.ops.isLoggable(Level.FINER))
      Logs.flush();
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
    if(!Logs.ops.isLoggable(Level.FINEST))
      return;

    StringBuffer buf = new StringBuffer();
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
      for(Integer priority : pReady.keySet()) {
	buf.append("    [" + priority + "]: ");
	TreeSet<Long> ids = pReady.get(priority);
	for(Long jobID : ids) 
	  buf.append(jobID + " ");
	buf.append("\n");
      }
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

    Logs.ops.finest(buf.toString());
    Logs.flush();
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
	Logs.glu.finer("Writing License Keys.");

	try {
	  String glue = null;
	  try {
	    ArrayList<LicenseKey> keys = 
	      new ArrayList<LicenseKey>(pLicenseKeys.values());
	    GlueEncoder ge = new GlueEncoderImpl("LicenseKeys", keys);
	    glue = ge.getText();
	  }
	  catch(GlueException ex) {
	    Logs.glu.severe
	      ("Unable to generate a Glue format representation of the license keys!");
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
	Logs.glu.finer("Reading License Keys.");

	ArrayList<LicenseKey> keys = null;
	try {
	  FileReader in = new FileReader(file);
	  GlueDecoder gd = new GlueDecoderImpl(in);
	  keys = (ArrayList<LicenseKey>) gd.getObject();
	  in.close();
	}
	catch(Exception ex) {
	  Logs.glu.severe
	    ("The license keys file (" + file + ") appears to be corrupted!");
	  Logs.flush();
	  
	  throw new PipelineException
	    ("I/O ERROR: \n" + 
	     "  While attempting to read the license keys file (" + file + ")...\n" + 
	   "    " + ex.getMessage());
	}
	assert(keys != null);
	
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
	Logs.glu.finer("Writing Selection Keys.");

	try {
	  String glue = null;
	  try {
	    ArrayList<SelectionKey> keys = 
	      new ArrayList<SelectionKey>(pSelectionKeys.values());
	    GlueEncoder ge = new GlueEncoderImpl("SelectionKeys", keys);
	    glue = ge.getText();
	  }
	  catch(GlueException ex) {
	    Logs.glu.severe
	      ("Unable to generate a Glue format representation of the selection keys!");
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
	Logs.glu.finer("Reading Selection Keys.");

	ArrayList<SelectionKey> keys = null;
	try {
	  FileReader in = new FileReader(file);
	  GlueDecoder gd = new GlueDecoderImpl(in);
	  keys = (ArrayList<SelectionKey>) gd.getObject();
	  in.close();
	}
	catch(Exception ex) {
	  Logs.glu.severe
	    ("The selection keys file (" + file + ") appears to be corrupted!");
	  Logs.flush();
	  
	  throw new PipelineException
	    ("I/O ERROR: \n" + 
	     "  While attempting to read the selection keys file (" + file + ")...\n" + 
	   "    " + ex.getMessage());
	}
	assert(keys != null);
	
	for(SelectionKey key : keys) 
	  pSelectionKeys.put(key.getName(), key);
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
	Logs.glu.finer("Writing Hosts.");

	try {
	  String glue = null;
	  try {
	    GlueEncoder ge = new GlueEncoderImpl("Hosts", pHosts);
	    glue = ge.getText();
	  }
	  catch(GlueException ex) {
	    Logs.glu.severe
	      ("Unable to generate a Glue format representation of the hosts!");
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
	Logs.glu.finer("Reading Hosts.");

	TreeMap<String,QueueHost> hosts = null;
	try {
	  FileReader in = new FileReader(file);
	  GlueDecoder gd = new GlueDecoderImpl(in);
	  hosts = (TreeMap<String,QueueHost>) gd.getObject();
	  in.close();
	}
	catch(Exception ex) {
	  Logs.glu.severe
	    ("The hosts file (" + file + ") appears to be corrupted!");
	  Logs.flush();
	  
	  throw new PipelineException
	    ("I/O ERROR: \n" + 
	     "  While attempting to read the hosts file (" + file + ")...\n" + 
	   "    " + ex.getMessage());
	}
	assert(hosts != null);
	
	pHosts.putAll(hosts);
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
   * @param samples 
   *   The resource usage samples indexed by fully resolved host name.
   * 
   * @throws PipelineException
   *   If unable to write the samples file.
   */ 
  private void 
  writeSamples
  (
   TaskTimer timer, 
   Date stamp, 
   TreeMap<String,ResourceSampleBlock> samples
  ) 
    throws PipelineException
  {
    timer.aquire();
    synchronized(pSampleFileLock) { 
      timer.resume();

      Logs.glu.finer("Writing Resource Samples: " + stamp.getTime());

      File dir = new File(pQueueDir, "queue/job-servers/samples");
      timer.aquire();
      synchronized(pMakeDirLock) {
	timer.resume();
	if(!dir.isDirectory())
	  if(!dir.mkdirs()) 
	    throw new PipelineException
	      ("Unable to create the samples directory (" + dir + ")!");
      }
         
      for(String hname : samples.keySet()) {    
	ResourceSampleBlock hsamples = samples.get(hname);
	File hdir = new File(dir, hname);
	synchronized(pMakeDirLock) {
	  if(!hdir.isDirectory())
	    if(!hdir.mkdirs()) 
	      throw new PipelineException
		("Unable to create the samples host directory (" + hdir + ")!");
	}
	  
	File file = new File(hdir, String.valueOf(stamp.getTime()));
	if(file.exists()) 
	  throw new PipelineException
	    ("Somehow the host resource samples file (" + file + ") already exists!"); 
	  
	try {
	  String glue = null;
	  try {
	    GlueEncoder ge = new GlueEncoderImpl("Samples", hsamples);
	    glue = ge.getText();
	  }
	  catch(GlueException ex) {
	    Logs.glu.severe
	      ("Unable to generate a Glue format representation of the resource samples!");
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
	     "  While attempting to write the resource samples file (" + file + ")...\n" + 
	     "    " + ex.getMessage());
	}
      }
    }
  }

  /**
   * Read all of the resource sample blocks from disk for the given host.
   * 
   * @param timer
   *   The task timer.
   * 
   * @param hostname
   *   The fully resolved name of the host.
   * 
   * @return
   *   The sample blocks indexed by the timestamp of the earliest sample in each block.
   * 
   * @throws PipelineException
   *   If unable to read the samples files.
   */ 
  private TreeMap<Long,ResourceSampleBlock>
  readSamples
  (
   TaskTimer timer, 
   String hostname
  ) 
    throws PipelineException
  {
    timer.aquire();
    synchronized(pSampleFileLock) { 
      timer.resume();

      TreeMap<Long,ResourceSampleBlock> blocks = new TreeMap<Long,ResourceSampleBlock>();
      File dir = new File(pQueueDir, "queue/job-servers/samples/" + hostname);
      if(!dir.isDirectory()) 
	return blocks;
	
      File files[] = dir.listFiles(); 
      int wk;
      for(wk=files.length-1; wk>=0; wk--) {
	File file = files[wk];
	if(file.isFile()) {
	  Logs.glu.finer("Reading Resource Samples: " + 
			 hostname + " [" + file.getName() + "]");
	  
	  ResourceSampleBlock block = null;
	  try {
	    FileReader in = new FileReader(file);
	    GlueDecoder gd = new GlueDecoderImpl(in);
	    block = (ResourceSampleBlock) gd.getObject();
	    in.close();
	  }
	  catch(Exception ex) {
	    Logs.glu.severe
	      ("The resource samples file (" + file + ") appears to be corrupted!");
	    Logs.flush();
	    
	    throw new PipelineException
	      ("I/O ERROR: \n" + 
	       "  While attempting to read the resource samples file (" + file + ")...\n" + 
	       "    " + ex.getMessage());
	  }
	  assert(block != null);
	  
	  blocks.put(block.getTimeStamp(block.getNumSamples()-1).getTime(), block);
	}
      }
    
      return blocks;
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
    if(pLastSampleWrite == null)
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
	      Long stamp = new Long(file.getName());
	      if((pLastSampleWrite.getTime() - stamp) > sSampleCleanupInterval) {
		Logs.glu.finer("Deleting Resource Sample File: " + file);
		if(!file.delete()) 
		  Logs.glu.severe
		    ("Unable to delete old resource sample file (" + file + ")!");
	      }
	    }
	    catch(NumberFormatException ex) {
	      Logs.glu.severe
		("Illegal resource sample file (" + file + ") encountered!");
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
      
      Logs.glu.finer("Writing Job: " + jobID);
      
      try {
	String glue = null;
	try {
	  GlueEncoder ge = new GlueEncoderImpl("Job", job);
	  glue = ge.getText();
	}
	catch(GlueException ex) {
	  Logs.glu.severe
	    ("Unable to generate a Glue format representation of the job!");
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
	Logs.glu.finer("Reading Job: " + jobID);
	
	QueueJob job = null;
	try {
	  FileReader in = new FileReader(file);
	  GlueDecoder gd = new GlueDecoderImpl(in);
	  job = (QueueJob) gd.getObject();
	  in.close();
	}
	catch(Exception ex) {
	  Logs.glu.severe
	    ("The job file (" + file + ") appears to be corrupted!");
	  Logs.flush();
	  
	  throw new PipelineException
	    ("I/O ERROR: \n" + 
	     "  While attempting to read the job file (" + file + ")...\n" + 
	   "    " + ex.getMessage());
	}
	
	return job;
      }
    }

    return null;
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

      Logs.glu.finer("Deleting Job: " + jobID);

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
      
      Logs.glu.finer("Writing Job Information: " + jobID);
      
      try {
	String glue = null;
	try {
	  GlueEncoder ge = new GlueEncoderImpl("JobInfo", info);
	  glue = ge.getText();
	}
	catch(GlueException ex) {
	  Logs.glu.severe
	    ("Unable to generate a Glue format representation of the job information!");
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
	Logs.glu.finer("Reading Job Information: " + jobID);
	
	QueueJobInfo info = null;
	try {
	  FileReader in = new FileReader(file);
	  GlueDecoder gd = new GlueDecoderImpl(in);
	  info = (QueueJobInfo) gd.getObject();
	  in.close();
	}
	catch(Exception ex) {
	  Logs.glu.severe
	    ("The job information file (" + file + ") appears to be corrupted!");
	  Logs.flush();
	  
	  throw new PipelineException
	    ("I/O ERROR: \n" + 
	     "  While attempting to read the job information file (" + file + ")...\n" + 
	     "    " + ex.getMessage());
	}
	
	return info;
      }
    }

    return null;
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

      Logs.glu.finer("Deleting Job Information: " + jobID);

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
      
      Logs.glu.finer("Writing Job Group: " + groupID);
      
      try {
	String glue = null;
	try {
	  GlueEncoder ge = new GlueEncoderImpl("JobGroup", group);
	  glue = ge.getText();
	}
	catch(GlueException ex) {
	  Logs.glu.severe
	    ("Unable to generate a Glue format representation of the job group!");
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
	Logs.glu.finer("Reading Job Group: " + groupID);
	
	QueueJobGroup group = null; 
	try {
	  FileReader in = new FileReader(file);
	  GlueDecoder gd = new GlueDecoderImpl(in);
	  group = (QueueJobGroup) gd.getObject();
	  in.close();
	}
	catch(Exception ex) {
	  Logs.glu.severe
	    ("The job group file (" + file + ") appears to be corrupted!");
	  Logs.flush();
	  
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

      Logs.glu.finer("Deleting Job Group: " + groupID);

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
   * Waits on a job to finish and records the results.
   */
  private 
  class WaitTask
    extends Thread
  {
    public 
    WaitTask
    (
     String hostname, 
     long jobID
    ) 
    {
      super("QueueMgr:WaitTask");

      pHostname = hostname; 
      pJobID = jobID;
    }

    public void 
    run() 
    {
      /* wait for the job to finish and collect the results */ 
      JobMgrControlClient client = null;
      QueueJobResults results = null;
      int[] numJobs = new int[1];
      try {
	client = new JobMgrControlClient(pHostname, pJobPort);	
	results = client.jobWait(pJobID, numJobs);
      }
      catch (Exception ex) {
	Logs.net.severe(ex.getMessage()); 
	Logs.flush();
      }
      finally {
	if(client != null)
	  client.disconnect();
      }

      /* update job information */ 
      synchronized(pJobInfo) {
	try {
	  QueueJobInfo info = pJobInfo.get(pJobID);
	  info.exited(results);
	  writeJobInfo(info);
	}
	catch (PipelineException ex) {
	  Logs.net.severe(ex.getMessage()); 
	  Logs.flush();
	}	
      }
      
      /* release any help license keys */ 
      {
	TreeSet<String> aquiredLicenseKeys = new TreeSet<String>();
	synchronized(pJobs) {
	  QueueJob job = pJobs.get(pJobID);
	  aquiredLicenseKeys.addAll(job.getJobRequirements().getLicenseKeys());
	}
	
	synchronized(pLicenseKeys) {
	  for(String kname : aquiredLicenseKeys) 
	    pLicenseKeys.get(kname).release();
	}
      }

      /* update the number of currently running jobs */ 
      synchronized(pHosts) {
	QueueHost host = pHosts.get(pHostname);
	if(host != null) {
	  ResourceSample sample = host.getLatestSample();
	  if(sample != null)
	    sample.setNumJobs(numJobs[0]);
	}
      }      
    }

    private String  pHostname; 
    private long    pJobID; 
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
      pJobID = jobID;
    }

    public void 
    run() 
    {
      JobMgrControlClient client = null;
      try {
	client = new JobMgrControlClient(pHostname, pJobPort);	
	client.jobKill(pJobID);
      }
      catch (Exception ex) {
	Logs.net.severe(ex.getMessage()); 
	Logs.flush();
      }
      finally {
	if(client != null)
	  client.disconnect();
      }
    }

    private String  pHostname; 
    private long    pJobID; 
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
	  client = new JobMgrControlClient(hname, pJobPort);	
	  client.cleanupResources(pJobIDs);
	}
	catch(Exception ex) {
	  Logs.net.severe(ex.getMessage()); 
	  Logs.flush();
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
   * The minimum time a cycle of the collector loop should take (in milliseconds).
   */ 
  private static final long  sCollectorInterval = 15000;  /* 15-second */ 

  /**
   * The maximum age of a sample file before it is deleted (in milliseconds).
   */ 
  private static final long  sSampleCleanupInterval = 86400000;  /* 24-hours */ 


  /**
   * The minimum time a cycle of the dispatcher loop should take (in milliseconds).
   */ 
  private static final long  sDispatcherInterval = 1000;  /* 1-second */ 

  /**
   * The number of dispatcher cycles between garbage collection of jobs.
   */ 
  private static final int  sGarbageCollectAfter = 600;  /* 10-minutes */ 



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The file system directory creation lock.
   */
  private Object pMakeDirLock;
 

  /**
   * The root queue directory.
   */ 
  private File  pQueueDir;

  /**
   * The network port listened to by the <B>pljobmgr</B>(1) daemon.
   */ 
  private int  pJobPort;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The cached names of the privileged users. <P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */ 
  private TreeSet<String>  pPrivilegedUsers;


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


  /*----------------------------------------------------------------------------------------*/

  /**
   * The per-host information indexed by fully resolved host name. <P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */
  private TreeMap<String,QueueHost>  pHosts; 

  /**
   * The timestamp of when the last set of resource samples was written to disk. <P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */ 
  private Date  pLastSampleWrite;

  /**
   * A lock which protects resource sample files from simulatenous reads and writes.
   */ 
  private Object  pSampleFileLock; 


  /*----------------------------------------------------------------------------------------*/
  
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
   * The IDs of the jobs which are ready to be run indexed by decending job priority. 
   * If a ready job has any source (upstream) jobs, they all must have a JobState of 
   * Finished before the job will be added to this table. <P> 
   * 
   * No locking is required, since this field is only accessed by the dispather() method.
   */ 
  private TreeMap<Integer,TreeSet<Long>>  pReady;

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

}

