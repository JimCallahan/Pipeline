// $Id: JobMgr.java,v 1.3 2004/07/28 19:15:08 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.message.*;
import us.temerity.pipeline.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   M G R                                                                      */
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
    File dir = new File(PackageInfo.sTempDir, "pljobmgr");
    if(!dir.isDirectory())
      if(!dir.mkdirs()) 
	throw new IllegalArgumentException
	  ("Unable to create the temporary directory (" + dir + ")!");

    pExecuteTasks = new TreeMap<Long,ExecuteTask>();
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
    timer.aquire();
    try {
      /* count the number of running jobs */ 
      int numJobs = 0;
      synchronized(pExecuteTasks) {
	timer.resume();
	numJobs = pExecuteTasks.size();
      }

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
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to start the job.
   */ 
  public Object
  start
  (
   JobStartReq req 
  ) 
  {
    QueueJob job = req.getJob();

    TaskTimer timer = new TaskTimer("JobMgr.start(): " + job.getJobID()); 
    timer.aquire();
    try {
      timer.resume();

      BaseAction action = Plugins.newAction(job.getActionName());
      ExecuteTask task = new ExecuteTask(job, action);

      timer.aquire();
      synchronized(pExecuteTasks) {
	timer.resume();
	pExecuteTasks.put(job.getJobID(), task);
      }

      task.start();

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }
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
  kill
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
  wait
  (
   JobWaitReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer(); 
    timer.aquire();
    try {
      ExecuteTask task = null;
      synchronized(pExecuteTasks) {
	timer.resume();
	task = pExecuteTasks.get(req.getJobID());
      }

      if(task == null)
	throw new PipelineException("No job (" + req.getJobID() + ") exists on the server!");
      else {
	try {
	  task.join();
	}
	catch(InterruptedException ex) {
	  throw new PipelineException(ex);
	}
      }
      
      timer.aquire();
      synchronized(pExecuteTasks) {
	timer.resume();
	pExecuteTasks.remove(req.getJobID());
      }

      return new JobWaitRsp(req.getJobID(), timer, task.getResults());
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   J O B   O U T P U T                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get current collected lines of captured STDOUT output from the given job starting 
   * at the given line. <P>
  
   * @param req
   *   The job output request.
   * 
   * @return
   *   <CODE>JobOutputRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to start the job.
   */ 
  public Object
  getStdOutLines
  (
   JobGetStdOutLinesReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer(); 
    timer.aquire();
    try {
      ExecuteTask task = null;
      synchronized(pExecuteTasks) {
	timer.resume();
	task = pExecuteTasks.get(req.getJobID());
      }

      String lines[] = null;
      if(task != null) {
	lines = task.getStdOutLines(req.getStart());
      }
      else {
	File file = new File(PackageInfo.sTempDir, 
			     "pljobmgr/" + req.getJobID() + ".out");

	lines = readOutputFile(file, req.getStart());
      }
      assert(lines != null);

      return new JobOutputRsp("JobMgr.getStdOutLines(): " + req.getJobID(), timer, lines);
    }
    catch(IOException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }
  }
  
  /**
   * Get current collected lines of captured STDERR output from the given job starting 
   * at the given line. <P>
  
   * @param req
   *   The job output request.
   * 
   * @return
   *   <CODE>JobOutputRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to start the job.
   */ 
  public Object
  getStdErrLines
  (
   JobGetStdErrLinesReq req 
  ) 
  {
    TaskTimer timer = new TaskTimer(); 
    timer.aquire();
    try {
      ExecuteTask task = null;
      synchronized(pExecuteTasks) {
	timer.resume();
	task = pExecuteTasks.get(req.getJobID());
      }

      String lines[] = null;
      if(task != null) {
	lines = task.getStdErrLines(req.getStart());
      }
      else {
	File file = new File(PackageInfo.sTempDir, 
			     "pljobmgr/" + req.getJobID() + ".err");

	lines = readOutputFile(file, req.getStart());
      }
      assert(lines != null);

      return new JobOutputRsp("JobMgr.getStdErrLines(): " + req.getJobID(), timer, lines);
    }
    catch(IOException ex) {
      return new FailureRsp(timer, ex.getMessage());	  
    }
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Read the lines of output from a job previously written to disk.
   */ 
  private String[]
  readOutputFile
  (
   File file, 
   int start
  )
    throws IOException
  {
    ArrayList<String> lines = new ArrayList<String>();
    {
      FileReader reader = new FileReader(file);
      
      int cnt = 0;
      char cs[] = new char[1024];
      StringBuffer buf = new StringBuffer();
      while(true) {
	int n = reader.read(cs);
	if(n == -1) {
	  if(buf.length() > 0) {
	    cnt++;
	    if(cnt > start) 
	      lines.add(buf.toString());
	  }
	  break;
	}
	
	int wk;
	for(wk=0; wk<n; wk++) {
	  if(cs[wk] == '\n') {
	    cnt++;
	    if(cnt > start) 
	      lines.add(buf.toString());
	    buf = new StringBuffer();
	  }
	  else {
	    if(cnt >= start) 
	      buf.append(cs[wk]);
	  }
	}
      }
      
      reader.close();
    }
      
    String[] rtn = new String[lines.size()];
    return (String[]) lines.toArray(rtn);
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
     BaseAction action
    ) 
    {
      super("JobMgr:ExecuteTask[Job" + job.getJobID() + "]");

      pJob = job;
      pAction = action;
      pLock = new Object();
    }

    public QueueJobResults
    getResults()
      throws PipelineException
    {
      synchronized(pLock) {
	if(pException != null) 
	  throw new PipelineException(pException);
	
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

    public String[]
    getStdOutLines 
    (
     int start  
    ) 
    {
      synchronized(pLock) {
	if(pProc != null)
	  return pProc.getStdOutLines(start);
	else 
	  return null;
      }      
    }

    public String[]
    getStdErrLines 
    (
     int start  
    ) 
    {
      synchronized(pLock) {
	if(pProc != null)
	  return pProc.getStdErrLines(start);
	else 
	  return null;
      }      
    }

    public void 
    run() 
    {
      try {
	long jobID = pJob.getJobID();
	
	Logs.ops.finer("Started Job: " + jobID);

	synchronized(pLock) {
	  pProc = pAction.prep(pJob.getActionAgenda());
	}

	pProc.start();
	pProc.join();

	Logs.ops.finer("Finished Job: " + jobID);

	synchronized(pLock) {
	  pResults = 
	    new QueueJobResults(pProc.getCommand(), pProc.getExitCode(), 
				pProc.getUserSecs(), pProc.getSystemSecs(), 
				pProc.getAverageResidentSize(), pProc.getMaxResidentSize(), 
				pProc.getAverageVirtualSize(), pProc.getMaxVirtualSize(), 
				pProc.getPageFaults());
	}

	{
	  Logs.ops.finest("Writing STDOUT of Job: " + jobID);

	  File file = new File(PackageInfo.sTempDir, 
			       "pljobmgr/" + jobID + ".out");
	  FileWriter out = new FileWriter(file);
	  out.write(pProc.getStdOut());
	  out.flush();
	  out.close();
	}

	{
	  Logs.ops.finest("Writing STDERR of Job: " + jobID);

	  File file = new File(PackageInfo.sTempDir, 
			       "pljobmgr/" + jobID + ".err");
	  FileWriter out = new FileWriter(file);
	  out.write(pProc.getStdErr());
	  out.flush();
	  out.close();
	}
      }
      catch(Exception ex) {
	synchronized(pLock) {
	  pException = ex; 
	}
      }
    }
      
    private QueueJob        pJob; 
    private BaseAction      pAction; 

    private Object          pLock; 
    private SubProcess      pProc; 
    private QueueJobResults pResults; 
    private Exception       pException; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The job execution threads indexed by job ID. <P> 
   * 
   * Access to this field should be protected by a synchronized block.
   */ 
  private TreeMap<Long,ExecuteTask>  pExecuteTasks; 
  
}

