// $Id: JobMgr.java,v 1.14 2004/11/09 06:01:32 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.message.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.*;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.concurrent.locks.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   M G R                                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The manager of queue job execution on a specific host. <P> 
 * 
 * @see JobMgrClient
 * @see JobMgrControlClient
 * @see JobMgrServer
 */
public
class JobMgr
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new job manager.
   */
  public
  JobMgr()
  {
    /* initialize the fields */ 
    {
      pMakeDirLock  = new Object(); 
      pExecuteTasks = new TreeMap<Long,ExecuteTask>();
      pFileMonitors = new HashMap<File, FileMonitor>();
    }

    try {
      /* make sure that the root job directory exists */ 
      makeRootDir();
    }
    catch(Exception ex) {
      Logs.ops.severe(ex.getMessage());
      Logs.flush();
      System.exit(1);
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Make sure that the root job directory exists.
   */ 
  private void 
  makeRootDir() 
    throws PipelineException
  {
    synchronized(pMakeDirLock) {
      pJobDir = new File(PackageInfo.sTempDir, "pljobmgr");
      if(!pJobDir.isDirectory())
	if(!pJobDir.mkdirs()) 
	  throw new PipelineException
	    ("Unable to create the temporary directory (" + pJobDir + ")!");
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H O S T   R E S O U R C E S                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get a point sample of the currently available system resources.
   * 
   * @return
   *   <CODE>JobGetResourcesRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the system resources.
   */ 
  public Object
  getResources() 
  {
    TaskTimer timer = new TaskTimer();
    try {
      /* count the number of running jobs */ 
      int numJobs = 0;
      timer.aquire();
      synchronized(pExecuteTasks) {
	timer.resume();
	for(ExecuteTask task : pExecuteTasks.values()) 
	  if(task.isAlive())
	    numJobs++;
      }

      Logs.ops.finest("GetResources - Num Jobs: " + numJobs);

      /* system load (1-minute average) */ 
      float load = 0.0f;
      {
	FileReader reader = new FileReader("/proc/loadavg");
	
	char[] buf = new char[4];
	if(reader.read(buf, 0, 4) == 4) 
	  load = Float.parseFloat(String.valueOf(buf));
	
	reader.close();
      } 
      
      /* free memory (unused + file cache) */ 
      long memory = 0;
      {
	long free   = 0;
	long cached = 0;
	
	FileReader reader = new FileReader("/proc/meminfo");
	while(true) {
	  /* read a line */ 
	  StringBuffer buf = new StringBuffer();
	  while(true) {
	    int next = reader.read();
	    if(next == -1) 
	      break;

	    char c = (char) next;
	    if(c == '\n') 
	      break;

	    buf.append(c);
	  }
	  
	  String[] fields = buf.toString().split(" ");
	  if(fields[0].equals("MemFree:")) {
	    int wk;
	    for(wk=1; wk<fields.length; wk++) {
	      if(fields[wk].length() > 0) {
		free = Long.parseLong(fields[wk]);
		break;
	      }
	    }
	  }
	  else if(fields[0].equals("Cached:")) {
	    int wk;
	    for(wk=1; wk<fields.length; wk++) {
	      if(fields[wk].length() > 0) {
		cached = Long.parseLong(fields[wk]);
		break;
	      }
	    }
	    
	    break;
	  }
	}
	reader.close();
	
	memory = (free + cached) * 1024L;
      } 
      
      /* free temporary disk space */ 
      long disk = NativeFileSys.freeDiskSpace(PackageInfo.sTempDir);

      /* the resource sample */ 
      ResourceSample sample = new ResourceSample(numJobs, load, memory, disk);

      return new JobGetResourcesRsp(timer, sample);
    }
    catch(IOException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }
  }

  /**
   * Get the number of processors (CPUs).
   * 
   * @return
   *   <CODE>JobGetNumProcessorsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the number of CPUs.
   */ 
  public Object
  getNumProcessors() 
  {
    TaskTimer timer = new TaskTimer();
    timer.aquire();
    try {
      timer.resume();
  
      int procs = 0;
      {
	FileReader reader = new FileReader("/proc/cpuinfo");
	boolean done = false;
	while(!done) {
	  /* read a line */ 
	  StringBuffer buf = new StringBuffer();
	  while(true) {
	    int next = reader.read();
	    if(next == -1) {
	      done = true;
	      break;
	    }

	    char c = (char) next;
	    if(c == '\n') 
	      break;

	    buf.append(c);
	  }

	  String line = buf.toString();
	  if(line.startsWith("processor")) 
	    procs++;
	}
	reader.close();	
      }
      
      return new JobGetNumProcessorsRsp(timer, procs);
    }
    catch(IOException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }
  }

  /**
   * Get the total amount of system memory (in bytes).
   * 
   * @return
   *   <CODE>JobGetTotalMemoryRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the memory size.
   */ 
  public Object
  getTotalMemory() 
  {
    TaskTimer timer = new TaskTimer();
    timer.aquire();
    try {
      timer.resume();

      /* free memory (unused + file cache) */ 
      long memory = 0;
      {      
	FileReader reader = new FileReader("/proc/meminfo");
	while(true) {
	  /* read a line */ 
	  StringBuffer buf = new StringBuffer();
	  while(true) {
	    int next = reader.read();
	    if(next == -1) 
	      break;

	    char c = (char) next;
	    if(c == '\n') 
	      break;

	    buf.append(c);
	  }
	  
	  String[] fields = buf.toString().split(" ");
	  if(fields[0].equals("MemTotal:")) {
	    int wk;
	    for(wk=1; wk<fields.length; wk++) {
	      if(fields[wk].length() > 0) {
		memory = Long.parseLong(fields[wk]) * 1024L;
		break;
	      }
	    }

	    break;
	  }
	}
	reader.close();
      }

      return new JobGetTotalMemoryRsp(timer, memory);
    }
    catch(IOException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }
  }

  /**
   * Get the size of the temporary disk drive (in bytes).
   * 
   * @return
   *   <CODE>JobGetTotalDiskRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the memory size.
   */ 
  public Object
  getTotalDisk() 
  {
    TaskTimer timer = new TaskTimer();
    timer.aquire();
    try {
      timer.resume();
      long disk = NativeFileSys.totalDiskSpace(PackageInfo.sTempDir);
      return new JobGetTotalDiskRsp(timer, disk);
    }
    catch(IOException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   J O B   E X E C U T I O N                                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Start the execution of a job on the server.
   * 
   * @param req
   *   The job start request.
   * 
   * @return
   *   <CODE>JobStartRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to start the job.
   */ 
  public Object
  jobStart
  (
   JobStartReq req 
  ) 
  {
    QueueJob job = req.getJob();
    TaskTimer timer = new TaskTimer(); 

    ExecuteTask task = new ExecuteTask(job);
    int numJobs = 1;
    timer.aquire();
    synchronized(pExecuteTasks) {
      timer.resume();
      pExecuteTasks.put(job.getJobID(), task); 

      for(ExecuteTask etask : pExecuteTasks.values()) 
	if(etask.isAlive())
	  numJobs++;

      Logs.ops.finest("JobStart - Num Jobs: " + numJobs);
    }

    task.start();

    return new JobStartRsp(job.getJobID(), timer, numJobs);
  }

  /**
   * Kill the job with the given ID running on the server.
   * 
   * @param req
   *   The job kill request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to start the job.
   */ 
  public Object
  jobKill
  (
   JobKillReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer("JobMgr.kill(): " + req.getJobID()); 
    timer.aquire();
    ExecuteTask task = null;
    synchronized(pExecuteTasks) {
      timer.resume();
      task = pExecuteTasks.get(req.getJobID());
    }

    if(task != null) 
      task.kill();

    return new SuccessRsp(timer);
  }

  /**
   * Kills all jobs currently running on the host. 
   */
  public void  
  killAll() 
  {
    synchronized(pExecuteTasks) {
      for(Long jobID : pExecuteTasks.keySet()) {
	Logs.net.finest("Shutting Down -- Killing Job: " + jobID);
	ExecuteTask task = pExecuteTasks.get(jobID);
	task.kill();
      }
    }
  }

  /**
   * Wait for a job with the given ID to complete and return the results of the execution.
   * 
   * @param req
   *   The job wait request.
   * 
   * @return
   *   <CODE>JobWaitRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to start the job.
   */ 
  public Object
  jobWait
  (
   JobWaitReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer(); 
    try {
      timer.aquire();
      ExecuteTask task = null;
      synchronized(pExecuteTasks) {
	timer.resume();
	task = pExecuteTasks.get(req.getJobID());
      }

      QueueJobResults results = null;

      /* the execution task still exists */ 
      if(task != null) {
	try {
	  task.join();
	}
	catch(InterruptedException ex) {
	  throw new PipelineException(ex);
	}
      
	results = task.getResults();

	timer.aquire();
	synchronized(pExecuteTasks) {
	  timer.resume();
	  pExecuteTasks.remove(req.getJobID());
	}
      }

      /* job server may have been restarted, see if a results file exists */ 
      else {
	File file = new File(pJobDir, req.getJobID() + "/results");
	if(file.isFile()) {
	  Logs.ops.finer("Reading Job Results: " + req.getJobID());
	  
	  try {
	    FileReader in = new FileReader(file);
	    GlueDecoder gd = new GlueDecoderImpl(in);
	    results = (QueueJobResults) gd.getObject();
	    in.close();
	  }
	  catch(Exception ex) {
	    Logs.glu.severe
	      ("The job results file (" + file + ") appears to be corrupted!");
	    Logs.flush();
	  
	    throw new PipelineException
	      ("I/O ERROR: \n" + 
	       "  While attempting to read the job results file (" + file + ")...\n" + 
	       "    " + ex.getMessage());
	  }
	}
	else {
	  throw new PipelineException
	    ("No job (" + req.getJobID() + ") exists on the server!");
	}
      }
      

      /* count the number of running jobs */ 
      int numJobs = 0;
      timer.aquire();
      synchronized(pExecuteTasks) {
	timer.resume();
	for(ExecuteTask etask : pExecuteTasks.values()) 
	  if(etask.isAlive())
	    numJobs++;
      }

      Logs.ops.finest("JobWait - Num Jobs: " + numJobs);

      assert(results != null);
      return new JobWaitRsp(req.getJobID(), timer, results, numJobs);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   J O B   M A N A G E M E N T                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Clean up obsolete job resources. <P> 
   * 
   * The <CODE>jobIDs</CODE> argument contains the ID of all jobs which are still being 
   * maintained by the the queue.  Any jobs not on this list are no longer reachable from
   * any job group and therefore all resources associated with the job should be deleted.
   * 
   * @param req
   *   The cleanup request.
   *
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to cleanup.
   */ 
  public synchronized Object
  cleanupResources
  (
   JobCleanupResourcesReq req
  ) 
  {
    TaskTimer timer = new TaskTimer("JobMgr.cleanupResources()");

    TreeSet<Long> live = req.getJobIDs();
    Map<String,String> env = System.getenv();

    ArrayList<String> args = new ArrayList<String>();
    args.add("--recursive");
    args.add("--force");

    boolean removeFiles = false;
    {
      File files[] = pJobDir.listFiles(); 
      int wk;
      for(wk=0; wk<files.length; wk++) {
	File dir = files[wk];
	try {
	  if(dir.isDirectory()) {
	    Long jobID = new Long(dir.getName());
	    if(!live.contains(jobID)) {
	      boolean executing = false;
	      {
		timer.aquire();
		synchronized(pExecuteTasks) {
		  timer.resume();
		  executing = pExecuteTasks.containsKey(jobID);
		}
	      }
	      
	      if(!executing) {
		Logs.glu.finer("Cleaning Job: " + jobID);
		args.add(dir.getName());
		removeFiles = true;
	      }
	    }
	  }
	  else {
	    Logs.glu.severe
	      ("Illegal file encountered in the job output directory (" + dir + ")!");
	  }
	}
	catch(NumberFormatException ex) {
	  Logs.glu.severe("Illegal job output directory encountered (" + dir + ")!");
	}
      }
    }

    if(removeFiles) {
      try {
	SubProcessLight proc = 
	  new SubProcessLight("Remove-JobFiles", "rm", args, env, pJobDir);
	proc.start();
	
	try {
	  proc.join();
	  if(!proc.wasSuccessful()) 
	    throw new PipelineException
	    ("Unable to remove the output files:\n\n" + 
	     "  " + proc.getStdErr());	
	}
	catch(InterruptedException ex) {
	  throw new PipelineException
	    ("Interrupted while removing the output files!");
	}
      }
      catch(PipelineException ex) {
	Logs.ops.severe(ex.getMessage());
	return new FailureRsp(timer, ex.getMessage());
      }
    }
    
    return new SuccessRsp(timer);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   J O B   O U T P U T                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the current number of lines of STDOUT output from the given job. <P> 
   * 
   * @param req
   *   The job output request.
   * 
   * @return
   *   <CODE>JobNumLinesRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to get the requested STDOUT of the job.
   */ 
  public Object
  getNumStdOutLines
  (
   JobGetNumStdOutLinesReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer(); 
    try {
      File file = new File(pJobDir, req.getJobID() + "/stdout");
      FileMonitor fmon = lookupOrCreateFileMonitor(file, timer);

      timer.aquire();
      synchronized(fmon) {
	timer.resume();

	int num = fmon.getNumLines();
	return new JobGetNumLinesRsp
	  ("JobMgr.getNumStdOutLines(): " + req.getJobID(), timer, num);
      }
    }
    catch(IOException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }
  }
   

  /**
   * Get the contents of the given region of lines of the STDOUT output from the given job.
   * 
   * @param req
   *   The job output request.
   * 
   * @return
   *   <CODE>JobOutputRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to get the requested STDOUT of the job.
   */ 
  public Object
  getStdOutLines
  (
   JobGetStdOutLinesReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer(); 
    try {
      File file = new File(pJobDir, req.getJobID() + "/stdout");
      FileMonitor fmon = lookupOrCreateFileMonitor(file, timer);

      timer.aquire();
      synchronized(fmon) {
	timer.resume();

	String lines = fmon.getLines(req.getStart(), req.getLines());
	return new JobOutputRsp("JobMgr.getStdOutLines(): " + req.getJobID(), timer, lines);
      }
    }
    catch(IOException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }
  }

  /**
   * Release any server resources associated with monitoring the STDOUT output of the 
   * given job.
   * 
   * @param req
   *   The job output request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful.
   */ 
  public Object
  closeStdOut
  (
   JobCloseStdOutReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer("JobMgr.closeStdOut(): " + req.getJobID()); 

    File file = new File(pJobDir, req.getJobID() + "/stdout");
    closeFileMonitor(file, timer);

    return new SuccessRsp(timer);
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the current number of lines of STDERR output from the given job. <P> 
   * 
   * @param req
   *   The job output request.
   * 
   * @return
   *   <CODE>JobNumLinesRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to get the requested STDERR of the job.
   */ 
  public Object
  getNumStdErrLines
  (
   JobGetNumStdErrLinesReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer(); 
    try {
      File file = new File(pJobDir, req.getJobID() + "/stderr");
      FileMonitor fmon = lookupOrCreateFileMonitor(file, timer);

      timer.aquire();
      synchronized(fmon) {
	timer.resume();

	int num = fmon.getNumLines();
	return new JobGetNumLinesRsp
	  ("JobMgr.getNumStdErrLines(): " + req.getJobID(), timer, num);
      }
    }
    catch(IOException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }
  }
   

  /**
   * Get the contents of the given region of lines of the STDERR output from the given job.
   * 
   * @param req
   *   The job output request.
   * 
   * @return
   *   <CODE>JobOutputRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to get the requested STDERR of the job.
   */ 
  public Object
  getStdErrLines
  (
   JobGetStdErrLinesReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer(); 
    try {
      File file = new File(pJobDir, req.getJobID() + "/stderr");
      FileMonitor fmon = lookupOrCreateFileMonitor(file, timer);

      timer.aquire();
      synchronized(fmon) {
	timer.resume();

	String lines = fmon.getLines(req.getStart(), req.getLines());
	return new JobOutputRsp("JobMgr.getStdErrLines(): " + req.getJobID(), timer, lines);
      }
    }
    catch(IOException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }
  }

  /**
   * Release any server resources associated with monitoring the STDERR output of the 
   * given job.
   * 
   * @param req
   *   The job output request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful.
   */ 
  public Object
  closeStdErr
  (
   JobCloseStdErrReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer("JobMgr.closeStdErr(): " + req.getJobID()); 

    File file = new File(pJobDir, req.getJobID() + "/stderr");
    closeFileMonitor(file, timer);

    return new SuccessRsp(timer);
  }




  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Lookup (or create a new) file monitor for the given file.
   */ 
  private FileMonitor
  lookupOrCreateFileMonitor
  (
   File file, 
   TaskTimer timer
  ) 
  {
    timer.aquire();
    synchronized(pFileMonitors) { 
      timer.resume();
      
      FileMonitor fmon = pFileMonitors.get(file);
      if(fmon == null) {
	fmon = new FileMonitor(file);
	pFileMonitors.put(file, fmon);
      }

      return fmon;
    }
  }

  /**
   * Close and remove any existing file monitor for the given file.
   */ 
  private void
  closeFileMonitor
  (
   File file, 
   TaskTimer timer
  ) 
  {
    timer.aquire();
    synchronized(pFileMonitors) { 
      timer.resume();
      
      FileMonitor fmon = pFileMonitors.get(file);
      if(fmon != null) {
	try {
	  timer.aquire();
	  synchronized(fmon) {
	    timer.resume();

	    fmon.close();
	  }
	}
	catch(IOException ex) {
	}
	
	pFileMonitors.remove(file);
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   C O L L E C T O R                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Perform one round of collecting per-process resource usage statistics.
   */ 
  public void 
  collector()
  {
    TaskTimer timer = new TaskTimer("JobMgr.collector()");

    SubProcessHeavy.collectStats();

    Logs.ops.finest(timer.toString()); 
    if(Logs.ops.isLoggable(Level.FINEST))
      Logs.flush();
    
    /* if we're ahead of schedule, take a nap */ 
    {
      timer.suspend();
      long nap = sStatsInterval - timer.getTotalDuration();
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
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Execute the regeneration action.
   */
  private
  class ExecuteTask
    extends Thread
  {
    public 
    ExecuteTask
    (
     QueueJob job
    ) 
    {
      super("JobMgr:ExecuteTask[Job" + job.getJobID() + "]");

      pJob = job;
      pLock = new Object();
    }

    public QueueJobResults
    getResults()
    {
      synchronized(pLock) {
	assert(!isAlive());
	return pResults;
      }
    }

    public void 
    kill() 
    {
      Logs.ops.finer("Killing Job: " + pJob.getJobID());
      synchronized(pLock) {
	if(pProc != null) 
	  pProc.kill();
      }
    }

    public void 
    run() 
    {
      try {
	long jobID = pJob.getJobID();
	
	File dir = new File(pJobDir, String.valueOf(jobID));
	File outFile = new File(dir, "stdout");
	File errFile = new File(dir, "stderr");
	try {
	  Logs.ops.finer("Preparing Job: " + jobID);
	  
	  /* create the job scratch directory */ 
	  File scratch = new File(dir, "scratch");
	  synchronized(pMakeDirLock) {
	    if(dir.exists() || scratch.exists())
	      throw new IOException
		("Somehow the job directory (" + dir + ") already exists!");
	    
	    if(!scratch.mkdirs()) 
	      throw new IOException
		("Unable to create the job directory (" + dir + ")!");
	    
	    NativeFileSys.chmod(0777, scratch);	      
	  }

	  /* remove the target primary and secondary files */ 
	  {
	    ArrayList<String> args = new ArrayList<String>();
	    args.add("--force");

	    ActionAgenda agenda = pJob.getActionAgenda();
	    SortedMap<String,String> env = agenda.getEnvironment();
	    File wdir = agenda.getWorkingDir();

	    for(File file : agenda.getPrimaryTarget().getFiles()) {
	      File path = new File(wdir, file.getPath());
	      if(path.isFile()) 
		args.add(file.getPath());
	    }

	    for(FileSeq fseq : agenda.getSecondaryTargets()) {
	      for(File file : fseq.getFiles()) {
		File path = new File(wdir, file.getPath());
		if(path.isFile()) 
		  args.add(file.getPath());
	      }
	    }

	    if(args.size() > 1) {
	      SubProcessLight proc = 
		new SubProcessLight(agenda.getNodeID().getAuthor(), 
				    "RemoveTargets", "rm", args, env, wdir);
	      try {
		proc.start();
		proc.join();
		if(!proc.wasSuccessful()) 
		  throw new PipelineException
		    ("Unable to remove the target files of job (" + jobID + "):\n\n" + 
		     "  " + proc.getStdErr());	
	      }
	      catch(InterruptedException ex) {
		throw new PipelineException
		  ("Interrupted while removing the target files of job (" + jobID + ")!");
	      }
	    }
	  }
	  
	  /* create the job execution process */ 
	  synchronized(pLock) {
	    pProc = pJob.getAction().prep(pJob.getActionAgenda(), outFile, errFile);
	  }
	}
	catch(Exception ex) {
	  Logs.ops.severe("Job Prep Failed: " + jobID);
	  pResults = new QueueJobResults(ex);
	  
	  Logs.ops.finest
	    ("Appending the exception stack to the STDERR file (" + outFile + ") of " + 
	     "job (" + jobID + ")...");

	  try {
	    FileWriter out = new FileWriter(errFile, true);
	    out.write("Job Prep Failed!\n\n" + getFullMessage(ex));
	    out.flush();
	    out.close();
	  }
	  catch(IOException ex2) {
	    Logs.ops.severe
	      ("Could not append the Exception message to STDERR file (" + errFile + ")!");
	  }
	  
	  return;
	}

	/* run the job */ 
	Logs.ops.finer("Started Job: " + jobID);
	{
	  pProc.start();
	  pProc.join();
	}
	Logs.ops.finer("Finished Job: " + jobID);

	/* make any existing target primary and secondary files read-only */ 
	{
	  ArrayList<String> args = new ArrayList<String>();
	  args.add("uga-w");

	  ActionAgenda agenda = pJob.getActionAgenda();
	  SortedMap<String,String> env = agenda.getEnvironment();
	  File wdir = agenda.getWorkingDir();
	  
	  for(File file : agenda.getPrimaryTarget().getFiles()) {
	    File path = new File(wdir, file.getPath());
	    if(path.isFile()) 
	      args.add(file.getPath());
	  }
	  
	  for(FileSeq fseq : agenda.getSecondaryTargets()) {
	    for(File file : fseq.getFiles()) {
	      File path = new File(wdir, file.getPath());
	      if(path.isFile()) 
		args.add(file.getPath());
	    }
	  }
	  
	  if(args.size() > 1) {
	    SubProcessLight proc = 
	      new SubProcessLight(agenda.getNodeID().getAuthor(), 
				  "ReadOnlyTargets", "chmod", args, env, wdir);
	    try {
	      proc.start();
	      proc.join();
	      if(!proc.wasSuccessful()) 
		throw new PipelineException
		  ("Unable to make the target files of job (" + jobID + ") read-only:\n\n" + 
		   "  " + proc.getStdErr());	
	    }
	    catch(InterruptedException ex) {
	      throw new PipelineException
		("Interrupted while making the target files of job (" + jobID + ") " + 
		 "read-only!");
	    }
	  }
	}

	/* record the results */ 
	synchronized(pLock) {
	  pResults = 
	    new QueueJobResults(pProc.getCommand(), pProc.getExitCode(), 
				pProc.getUserTime(), pProc.getSystemTime(), 
				pProc.getVirtualSize(), pProc.getResidentSize(), 
				pProc.getSwappedSize(), pProc.getPageFaults());

	  File file = new File(dir, "results");
	  try {
	    String glue = null;
	    try {
	      GlueEncoder ge = new GlueEncoderImpl("Results", pResults);
	      glue = ge.getText();
	    }
	    catch(GlueException ex) {
	      Logs.glu.severe
		("Unable to generate a Glue format representation of the job results!");
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
	       "  While attempting to write the job results file (" + file + ")...\n" + 
	       "    " + ex.getMessage());
	  }
	}
      }
      catch(Exception ex2) {
	Logs.ops.severe(getFullMessage(ex2));
      }
    }

    private String 
    getFullMessage
    (
     Throwable ex
     ) 
    {
      StringBuffer buf = new StringBuffer();
      
      if(ex.getMessage() != null) 
	buf.append(ex.getMessage() + "\n\n"); 	
      else if(ex.toString() != null) 
      buf.append(ex.toString() + "\n\n"); 	
      
      buf.append("Stack Trace:\n");
      StackTraceElement stack[] = ex.getStackTrace();
      int wk;
      for(wk=0; wk<stack.length; wk++) 
	buf.append("  " + stack[wk].toString() + "\n");
      
      return (buf.toString());
    }

    private QueueJob         pJob; 
    private Object           pLock; 
    private SubProcessHeavy  pProc; 
    private QueueJobResults  pResults; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The minimum time a cycle of the process statistics collection loop should take 
   * (in milliseconds).
   */ 
  private static final long  sStatsInterval = 5000;  /* 5-second */ 
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The file system directory creation lock.
   */
  private Object pMakeDirLock;
 
  /**
   * The root job directory.
   */ 
  private File  pJobDir;


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The job execution threads indexed by job ID. <P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */ 
  private TreeMap<Long,ExecuteTask>  pExecuteTasks; 


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The open output file monitors indexed by filename.
   */
  private HashMap<File, FileMonitor>  pFileMonitors; 
  
}

