// $Id: QueueMgr.java,v 1.6 2004/08/01 19:31:46 jim Exp $

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

      pLicenseKeys = new TreeMap<String,LicenseKey>();
      pSelectionKeys = new TreeMap<String,SelectionKey>();

      pHosts = new TreeMap<String,QueueHost>();
    }

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
	throw new IllegalStateException
	  ("Unable to create lock file (" + file + ")!");
      }
    }

    /* load the license keys if any exist */ 
    try {
      readLicenseKeys();
    }
    catch(PipelineException ex) {
      throw new IllegalArgumentException(ex.getMessage());
    }

    /* load the selection keys if any exist */ 
    try {
      readSelectionKeys();
    }
    catch(PipelineException ex) {
      throw new IllegalArgumentException(ex.getMessage());
    }

    /* load the hosts if any exist */ 
    try {
      readHosts();
    }
    catch(PipelineException ex) {
      throw new IllegalArgumentException(ex.getMessage());
    }    

    /* initialize the last sample timestamp */ 
    pLastSampleWrite = new Date();
    pSampleFileLock = new Object();
  } 

  /**
   * Make sure that the root queue directories exist.
   */ 
  private void 
  makeRootDirs() 
  {
    if(!pQueueDir.isDirectory()) 
      throw new IllegalArgumentException
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
	    throw new IllegalArgumentException
	      ("Unable to create the directory (" + dir + ")!");
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
	ArrayList<ResourceSampleBlock> blocks = readSamples(timer, req.getHostname()); 

	Date latest = null;
	if(!blocks.isEmpty()) 
	  latest = blocks.get(0).getTimeStamp(0);
	
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
	
	all.addAll(blocks);
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

    timer.aquire();
    {
      timer.resume();


      // ...

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
	Logs.ops.finer("Writing License Keys.");

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
	Logs.ops.finer("Reading License Keys.");

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
	Logs.ops.finer("Writing Selection Keys.");

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
	Logs.ops.finer("Reading Selection Keys.");

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
	Logs.ops.finer("Writing Hosts.");

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
	Logs.ops.finer("Reading Hosts.");

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

      Logs.ops.finer("Writing Resource Samples: " + stamp.getTime());

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
   * @throws PipelineException
   *   If unable to read the samples files.
   */ 
  private ArrayList<ResourceSampleBlock>
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

      ArrayList<ResourceSampleBlock> blocks = new ArrayList<ResourceSampleBlock>();
      File dir = new File(pQueueDir, "queue/job-servers/samples/" + hostname);
      if(!dir.isDirectory()) 
	return blocks;
	
      File files[] = dir.listFiles(); 
      int wk;
      for(wk=files.length-1; wk>=0; wk--) {
	File file = files[wk];
	if(file.isFile()) {
	  Logs.ops.finer("Reading Resource Samples: " + 
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
	  
	  blocks.add(block);
	}
      }
    
      return blocks;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The minimum time a cycle of the collector loop should take (in milliseconds).
   */ 
  private static final long  sCollectorInterval = 15000;  /* 15-second */ 

  /**
   * The minimum time a cycle of the dispatcher loop should take (in milliseconds).
   */ 
  private static final long  sDispatcherInterval = 1000;  /* 1-second */ 



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
   * The jobs waiting on one or more source (upstream) jobs to complete before they can be 
   * added to the ready queue.  This list also contains jobs newly submitted to the queue
   * which may not have any source nodes. <P> 
   * 
   * No locking is required.
   */ 
  private ConcurrentLinkedQueue<QueueJob>  pWaiting;

  /**
   * The job which are ready to be run in decending priority order.  If a ready job has 
   * any source (upstream) jobs, they all must have a QueueState of Finished before the 
   * job will be added to this queue. <P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */ 
  private LinkedList<QueueJob>  pReady;

  /**
   * The IDs of jobs which should be killed as soon as possible. 
   * 
   * No locking is required.
   */
  private ConcurrentLinkedQueue<Long>  pHitList;
  

  /**
   * The table of all existing QueueJob's indexed by job ID.  If a job exists, its job ID
   * will be a member of the key set of this table.  If the value for a valid key is 
   * <CODE>null</CODE>, then the QueueJob is currently stored on disk only and needs to 
   * be reloaded.  <P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */ 
  private TreeMap<Long,QueueJob>  pJobs; 

  /**
   * The table of status information for all existing QueueJob's indexed by job ID.  
   * If job information exists, its job ID will be a member of the key set of this table.  
   * If the value for a valid key is <CODE>null</CODE>, then the QueueJob is currently 
   * stored on disk only and needs to be reloaded. <P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */ 
  private TreeMap<Long,QueueJobInfo>  pJobInfo; 


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
   * This table is always populated regardless of whether the job and/or job information 
   * are currently loaded for a particular job. <P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */ 
  private TreeMap<NodeID,TreeMap<String,Long>>  pNodeJobIDs;

  
  /**
   * The groups of jobs indexed by job group ID. <P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */
  private TreeMap<Long,QueueJobGroup>  pJobGroups; 

  
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

}

