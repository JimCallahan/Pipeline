// $Id: JobMgr.java,v 1.4 2004/08/22 21:55:19 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.message.*;
import us.temerity.pipeline.glue.*;
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
    /* initialize the fields */ 
    {
      pMakeDirLock  = new Object();
      pExecuteTasks = new TreeMap<Long,ExecuteTask>();
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

    ExecuteTask task = new ExecuteTask(job);

    timer.aquire();
    synchronized(pExecuteTasks) {
      timer.resume();
      pExecuteTasks.put(job.getJobID(), task);
    }

    task.start();

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
      
      assert(results != null);
      return new JobWaitRsp(req.getJobID(), timer, results);
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
   *   <CODE>FailureRsp</CODE> if unable to get the STDOUT of the job.
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
	File file = new File(pJobDir, req.getJobID() + "/stdout");
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
   *   <CODE>FailureRsp</CODE> if unable to get the STDERR of the job.
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
	File file = new File(pJobDir, req.getJobID() + "/stderr");
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
	
	File dir = new File(pJobDir, String.valueOf(jobID));
	try {
	  Logs.ops.finer("Preparing Job: " + jobID);
	  
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
	  
	  synchronized(pLock) {
	    pProc = pJob.getAction().prep(pJob.getActionAgenda());
	  }
	}
	catch(Exception ex) {
	  Logs.ops.severe("Job Prep Failed: " + jobID);
	  pResults = new QueueJobResults(ex);
	  
	  Logs.ops.finest("Writing Exception Stack as STDERR of Job: " + jobID);
	  {
	    File file = new File(dir, "stderr"); 
	    FileWriter out = new FileWriter(file);
	    out.write("Job Prep Failed!\n\n" + getFullMessage(ex));
	    out.flush();
	    out.close();
	  }
	  
	  return;
	}

	Logs.ops.finer("Started Job: " + jobID);
	{
	  pProc.start();
	  pProc.join();
	}
	Logs.ops.finer("Finished Job: " + jobID);

	synchronized(pLock) {
	  pResults = 
	    new QueueJobResults(pProc.getCommand(), pProc.getExitCode(), 
				pProc.getUserSecs(), pProc.getSystemSecs(), 
				pProc.getAverageResidentSize(), pProc.getMaxResidentSize(), 
				pProc.getAverageVirtualSize(), pProc.getMaxVirtualSize(), 
				pProc.getPageFaults());

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
	
	{
	  Logs.ops.finest("Writing STDOUT of Job: " + jobID);
	  
	  File file = new File(dir, "stdout"); 
	  FileWriter out = new FileWriter(file);
	  out.write(pProc.getStdOut());
	  out.flush();
	  out.close();
	}

	{
	  Logs.ops.finest("Writing STDERR of Job: " + jobID);

	  File file = new File(dir, "stderr"); 
	  FileWriter out = new FileWriter(file);
	  out.write(pProc.getStdErr());
	  out.flush();
	  out.close();
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

    private QueueJob        pJob; 
    private Object          pLock; 
    private SubProcess      pProc; 
    private QueueJobResults pResults; 
  }



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
  
}

