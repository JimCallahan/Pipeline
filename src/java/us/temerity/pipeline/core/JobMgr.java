// $Id: JobMgr.java,v 1.40 2007/03/18 02:29:16 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.glue.io.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   M G R                                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The manager of queue job execution on a specific host. <P> 
 */
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
    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Info,
       "Initializing [JobMgr]...");
    LogMgr.getInstance().flush();

    /* initialize the fields */ 
    {
      pExecuteTasks = new TreeMap<Long,ExecuteTask>();
      pFileMonitors = new HashMap<File, FileMonitor>();
    }

    /* make sure that the root job directory exists */ 
    try {
      Path path = new Path(PackageInfo.sTempPath, "pljobmgr");
      pJobDir = path.toFile();
      if(!pJobDir.isDirectory())
	if(!pJobDir.mkdirs()) 
	  throw new PipelineException
	    ("Unable to create the temporary directory (" + pJobDir + ")!");
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
  /*   E D I T O R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Launch an Editor plugin to edit the given files as the specified user.
   * 
   * @param req
   *   The job start request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to start the editor.
   */
  public synchronized Object
  editAs
  ( 
   JobEditAsReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer("JobMgr.editAs()");

    try {
      BaseEditor editor = req.getEditor();
      SubProcessLight proc = editor.prep(req.getAuthor(), req.getFileSeq(), 
					 req.getEnvironment(), req.getWorkingDir());
      if(proc == null) 
	throw new PipelineException
	  ("The Editor (" + editor.getName() + " v" + editor.getVersionID() + ") from the " + 
	   "vendor (" + editor.getVendor() + ") does not implement the Editor.prep() " + 
	   "method and therefore cannot be run as another user!");
      
      proc.start();
      proc.join();
      if(!proc.wasSuccessful()) 
	return new JobEditAsFailedRsp(timer, proc);

      return new SuccessRsp(timer);
    }
    catch(Exception ex) {
      return new FailureRsp(timer, ex.getMessage());
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

      /* system load (1-minute average) */ 
      float load = NativeOS.getLoadAverage();
      
      /* free memory (unused + file cache) */ 
      long memory = NativeOS.getFreeMemory();
      
      /* free temporary disk space */ 
      long disk = NativeFileSys.freeDiskSpace(PackageInfo.sTempPath.toFile());

      /* the resource sample */ 
      ResourceSample sample = new ResourceSample(numJobs, load, memory, disk);

      return new JobGetResourcesRsp(timer, sample);
    }
    catch(IOException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }
  }

  /**
   * Get the operating system type.
   * 
   * @return
   *   <CODE>JobGetOsTypeRsp</CODE> always.
   */ 
  public Object
  getOsType() 
  {
    TaskTimer timer = new TaskTimer();
    return new JobGetOsTypeRsp(timer, PackageInfo.sOsType); 
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
      int procs = NativeOS.getNumProcessors();
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
      long total = NativeOS.getTotalMemory();
      return new JobGetTotalMemoryRsp(timer, total);
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
      long disk = NativeFileSys.totalDiskSpace(PackageInfo.sTempPath.toFile());
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
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to start the job.
   */ 
  public Object
  jobStart
  (
   JobStartReq req 
  ) 
  {
    QueueJob job = req.getJob();
    DoubleMap<OsType,String,String> envs = req.getCookedEnvs();

    TaskTimer timer = new TaskTimer("JobMgr.start(): " + job.getJobID());

    /* create the job scratch directory */ 
    try {	
      File dir = new File(pJobDir, String.valueOf(job.getJobID()));

      /* job was previously started, 
	   use existing results instead of starting a new process */ 
      if(dir.exists()) 
	return new SuccessRsp(timer);

      File scratch = new File(dir, "scratch");      
      if(!scratch.mkdirs()) 
	throw new IOException
	  ("Unable to create the job directory (" + dir + ")!");

      switch(PackageInfo.sOsType) {
      case Unix:
      case MacOS:
        NativeFileSys.chmod(0777, scratch);
      }
    }
    catch(IOException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }

    /* start the job execution task */ 
    timer.aquire();
    synchronized(pExecuteTasks) {
      timer.resume();
      
      ExecuteTask task = new ExecuteTask(job, envs);
      task.start();

      pExecuteTasks.put(job.getJobID(), task); 
    }

    return new SuccessRsp(timer);
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
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Finest,
	   "Shutting Down -- Killing Job: " + jobID);
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
   *   <CODE>FailureRsp</CODE> if unable to find the job.
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
	int cycles = 0; 
	while(task.isAlive()) {
	  try {
	    task.join(15000);
	    cycles++;
	  }
	  catch(InterruptedException ex) {
	    throw new PipelineException(ex);
	  }

	  LogMgr.getInstance().log
	    (LogMgr.Kind.Ops, LogMgr.Level.Finest,
	     "Job (" + req.getJobID() + "): " + 
	     "WAITING for (" + cycles + ") loops...");
	}
	
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Finest, 
	   "Job (" + req.getJobID() + "): " + 
	   "COMPLETED after (" + cycles + ") loops...");
	
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
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Ops, LogMgr.Level.Finer,
	     "Reading Job Results: " + req.getJobID());
	  
	  try {
	    GlueDecoder gd = new GlueDecoderImpl(file);
	    results = (QueueJobResults) gd.getObject();
	  }
	  catch(Exception ex) {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	       "The job results file (" + file + ") appears to be corrupted!");
	    LogMgr.getInstance().flush();
	  
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
      
      if(results == null) 
	throw new IllegalStateException("Job results cannot be (null)!"); 
      return new JobWaitRsp(req.getJobID(), timer, results);
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 ex.getMessage());
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
    
    ArrayList<File> deadDirs = new ArrayList<File>(); 
    {
      TreeSet<Long> live = req.getJobIDs();

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
		LogMgr.getInstance().log
		  (LogMgr.Kind.Glu, LogMgr.Level.Finer,
		   "Cleaning Job: " + jobID);
		deadDirs.add(dir);
	      }
	    }
	  }
	  else {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Glu, LogMgr.Level.Severe, 
	       "Illegal file encountered in the job output directory (" + dir + ")!");
	  }
	}
	catch(NumberFormatException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	     "Illegal job output directory encountered (" + dir + ")!");
	}
      }
    }

    if(!deadDirs.isEmpty()) {
      try {
	Map<String,String> env = System.getenv();

	switch(PackageInfo.sOsType) {
	case Unix:
	case MacOS:
	  {
	    ArrayList<String> args = new ArrayList<String>();
	    args.add("-rf");

	    for(File dir : deadDirs) 
	      args.add(dir.getName()); 
	    
	    SubProcessLight proc = 
	      new SubProcessLight("Remove-JobFiles", "rm", args, env, pJobDir);
	    try {
	      proc.start();
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
	  break;

	case Windows:
	  for(File dir : deadDirs) {
	    ArrayList<String> args = new ArrayList<String>();	
	    args.add("/c"); 
	    args.add("\"rmdir \"" + dir + "\" /s /q\"");
         
	    SubProcessLight proc = 
	      new SubProcessLight("Remove-JobFiles", "cmd.exe", args, env, 
				  PackageInfo.sTempPath.toFile()); 
	    try {
	      proc.start();
	      proc.join();
	      if(!proc.wasSuccessful()) 
		throw new PipelineException
		  ("Unable to remove the output files in directory (" + dir + "):\n\n" + 
		   "  " + proc.getStdErr());	
	    }
	    catch(InterruptedException ex) {
	      throw new PipelineException
		("Interrupted while removing the output files!");
	    }
	  }
	}
      }
      catch(PipelineException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Severe,
	   ex.getMessage());
	return new FailureRsp(timer, ex.getMessage());
      }
    }
    
    return new SuccessRsp(timer);
  }

  /**
   * Clean up the resources associated with a preempted job. <P> 
   * 
   * @param req
   *   The cleanup request.
   *
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to cleanup.
   */ 
  public synchronized Object
  cleanupPreemptedResources
  (
   JobCleanupPreemptedResourcesReq req
  ) 
  {
    TaskTimer timer = new TaskTimer("JobMgr.cleanupPreemptedResources()");

    File dir = new File(pJobDir, String.valueOf(req.getJobID()));
    if(dir.isDirectory()) {
      String program = null;
      ArrayList<String> args = new ArrayList<String>();
      switch(PackageInfo.sOsType) {
      case Unix:
      case MacOS:
	program = "rm";
	args.add("-f");
	args.add(dir.toString());
	break;
	
      case Windows:
	program = "cmd.exe"; 	
	args.add("/c"); 
	args.add("\"rmdir \"" + dir + "\" /s /q\"");
      }

      try {
	Map<String,String> env = System.getenv();

	SubProcessLight proc = 
	  new SubProcessLight("Remove-PreemptedJobFiles", program, args, env, 
			      PackageInfo.sTempPath.toFile()); 
	try {
	  proc.start();
	  proc.join();
	  if(!proc.wasSuccessful()) 
	    throw new PipelineException
	      ("Unable to remove the output files in directory (" + dir + "):\n\n" + 
	       "  " + proc.getStdErr());	
	}
	catch(InterruptedException ex) {
	  throw new PipelineException
	    ("Interrupted while removing the output files!");
	}
      }
      catch(PipelineException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Severe,
	   ex.getMessage());
	return new FailureRsp(timer, ex.getMessage());
      }
    }
    
    return new SuccessRsp(timer);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   E X E C U T I O N   D E T A I L S                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the execution details for a given job.
   * 
   * @param req
   *   The execution details request.
   * 
   * @return
   *   <CODE>JobGetExecDetailsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable find the details for the job.
   */ 
  public Object
  getExecDetails
  (
   JobGetExecDetailsReq req 
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

      SubProcessExecDetails details = null;

      /* the execution task still exists */ 
      if(task != null) {
	details = task.getExecDetails(); 
      }

      /* job server may have been restarted, see if a details file exists */ 
      else {
	File file = new File(pJobDir, req.getJobID() + "/details");
	if(file.isFile()) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Ops, LogMgr.Level.Finer,
	     "Reading Job Execution Details: " + req.getJobID());
	  
	  try {
	    GlueDecoder gd = new GlueDecoderImpl(file);
	    details = (SubProcessExecDetails) gd.getObject();
	  }
	  catch(Exception ex) {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	       "The job execution details file (" + file + ") appears to be corrupted!");
	    LogMgr.getInstance().flush();
	  
	    throw new PipelineException
	      ("I/O ERROR: \n" + 
	       "  While attempting to read the job execution details file " + 
	       "(" + file + ")...\n" + 
	       "    " + ex.getMessage());
	  }
	}
	else {
	  throw new PipelineException
	    ("No job (" + req.getJobID() + ") exists on the server!");
	}
      }
      
      if(details == null) 
	throw new IllegalStateException("Job details cannot be (null)!"); 
      return new JobGetExecDetailsRsp(req.getJobID(), timer, details); 
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 ex.getMessage());
      return new FailureRsp(timer, ex.getMessage());	  
    }
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

    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Finest,
       timer.toString()); 
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Ops, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
    
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
     QueueJob job, 
     DoubleMap<OsType,String,String> envs
    ) 
    {
      super("JobMgr:ExecuteTask[Job" + job.getJobID() + "]");

      pJob        = job;
      pCookedEnvs = envs; 
    }

    public synchronized QueueJobResults
    getResults()
    {
      return pResults;
    }

    public synchronized SubProcessExecDetails
    getExecDetails()
    {
      return pExecDetails; 
    }

    public void 
    kill() 
    {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Finer,
	 "Killing Job: " + pJob.getJobID());

      SubProcessHeavy proc = pProc;
      if(proc != null) 
	proc.kill();
    }

    public void 
    run() 
    {
      try {
	long jobID = pJob.getJobID();

	ActionAgenda agenda = new ActionAgenda(pJob.getActionAgenda(), pCookedEnvs);
	SortedMap<String,String> env = agenda.getEnvironment();
	
	File dir = new File(pJobDir, String.valueOf(jobID));
	File scratch = new File(dir, "scratch");
	File outFile = new File(dir, "stdout");
	File errFile = new File(dir, "stderr");
	try {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Ops, LogMgr.Level.Finer,
	     "Preparing Job: " + jobID);
	  
	  /* create the job execution process */ 
	  pProc = pJob.getAction().prep(agenda, outFile, errFile);
	  if(pProc == null) 
	    throw new PipelineException
	      ("The prep() method of the Action (" + pJob.getAction().getName() + ") " + 
	       "returned (null) instead of a the expected SubProcessHeavy instance!");
	}
	catch(Exception ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Ops, LogMgr.Level.Severe,
	     "Job Prep Failed: " + jobID);

	  {
	    String msg = ("Job Prep Failed: " + ex.getMessage() + "\n" + 
			  "(see Error log for details)");
	    recordExecDetails(new SubProcessExecDetails(msg, env), dir);
	  }

	  {
	    String msg = ("Job Prep Failed!\n\n" + getFullMessage(ex));
	    
	    {
	      LogMgr.getInstance().log
		(LogMgr.Kind.Ops, LogMgr.Level.Finest, 
		 "Appending the exception stack to the STDOUT file (" + outFile + ") of " + 
		 "job (" + jobID + ")...");
	      
	      try {
		FileWriter out = new FileWriter(outFile, true);
		out.write(msg);
		out.flush();
		out.close();
	      }
	      catch(IOException ex2) {
		LogMgr.getInstance().log
		  (LogMgr.Kind.Ops, LogMgr.Level.Severe, 
		   "Could not append the Exception message to STDOUT file " + 
		   "(" + outFile + ")!");
	      }
	    }	  
	    
	    {
	      LogMgr.getInstance().log
		(LogMgr.Kind.Ops, LogMgr.Level.Finest, 
		 "Appending the exception stack to the STDERR file (" + errFile + ") of " + 
		 "job (" + jobID + ")...");
	      
	      try {
		FileWriter out = new FileWriter(errFile, true);
		out.write(msg);
		out.flush();
		out.close();
	      }
	      catch(IOException ex2) {
		LogMgr.getInstance().log
		  (LogMgr.Kind.Ops, LogMgr.Level.Severe, 
		   "Could not append the Exception message to STDERR file " + 
		   "(" + errFile + ")!");
	      }
	    }
	  }

	  recordResults(new QueueJobResults(ex), dir);
	    	  
	  return;
	}

	/* save the execution details */ 
	recordExecDetails(pProc.getExecDetails(), dir); 

	/* run the job */ 
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Finer,
	   "Started Job: " + jobID);
        {
	  pProc.start();
	  
	  int cycles = 0; 
	  while(pProc.isAlive()) {
	    try {
	      pProc.join(15000);
	      cycles++;
	    }
	    catch(InterruptedException ex) {
	      throw new PipelineException(ex);
	    }

	    LogMgr.getInstance().log
	      (LogMgr.Kind.Ops, LogMgr.Level.Finest, 
	       "Process for Job (" + jobID + "): " + 
	       "WAITING for (" + cycles + ") loops...");
	  }
	    
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Ops, LogMgr.Level.Finest, 
	     "Process for Job (" + jobID + "): " + 
	     "COMPLETED after (" + cycles + ") loops...");
	}

	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Finer,
	   "Finished Job: " + jobID);
      
	/* record the results */ 
	{
	  QueueJobResults results = 
	    new QueueJobResults(pProc.getExitCode(), 
				pProc.getUserTime(), pProc.getSystemTime(), 
				pProc.getVirtualSize(), pProc.getResidentSize(), 
				pProc.getSwappedSize(), pProc.getPageFaults());

	  recordResults(results, dir);
	}
      }
      catch(Exception ex2) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Severe,
	   getFullMessage(ex2));
      }
    }

    private String 
    getFullMessage
    (
     Throwable ex
    ) 
    {
      StringBuilder buf = new StringBuilder();
      
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

    private synchronized void 
    recordExecDetails
    (
     SubProcessExecDetails details,
     File dir
    ) 
      throws PipelineException
    {
      File file = new File(dir, "details");
      try {
	String glue = null;
	try {
	  GlueEncoder ge = new GlueEncoderImpl("Details", details);
	  glue = ge.getText();
	}
	catch(GlueException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe, 
	     "Unable to generate a Glue format representation of the job execution details!");
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
	   "  While attempting to write the job execution details file (" + file + ")...\n" + 
	   "    " + ex.getMessage());
      }
      finally {
	pExecDetails = details; 
      }
    }

    private synchronized void 
    recordResults
    (
     QueueJobResults results, 
     File dir
    ) 
      throws PipelineException
    {
      File file = new File(dir, "results");
      try {
	String glue = null;
	try {
	  GlueEncoder ge = new GlueEncoderImpl("Results", results);
	  glue = ge.getText();
	}
	catch(GlueException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Glu, LogMgr.Level.Severe, 
	     "Unable to generate a Glue format representation of the job results!");
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
	   "  While attempting to write the job results file (" + file + ")...\n" + 
	   "    " + ex.getMessage());
      }
      finally {
	pResults = results;
      }
    }


    private QueueJob  pJob;

    private DoubleMap<OsType,String,String>  pCookedEnvs; 

    private SubProcessHeavy        pProc; 
    private SubProcessExecDetails  pExecDetails; 
    private QueueJobResults        pResults; 
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

