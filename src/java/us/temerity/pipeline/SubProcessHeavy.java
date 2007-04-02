// $Id: SubProcessHeavy.java,v 1.16 2007/04/02 10:46:12 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.util.concurrent.atomic.*;
import java.io.*;
import java.nio.*;
import java.math.*;


/*------------------------------------------------------------------------------------------*/
/*   S U B P R O C E S S   H E A V Y                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A Java {@link Thread Thread} which manages an OS level subprocess. <P> 
 * 
 * The STDOUT/STDERR of the OS level process are redirected to disk files when the process
 * is started bypassing the Java runtime.  This provides the maximum possible IO throughput
 * for long running and IO intensive processes.  The 
 * {@link SubProcessLight SubProcessLight} class is a better fit for short lived 
 * processes where low latency access to minimal amounts of output is desired. <P> 
 * 
 * @see NativeProcess
 */
public
class SubProcessHeavy
  extends BaseSubProcess
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create an OS level subprocess which will be executed as the current user. <P>
   * 
   * The <CODE>program</CODE> must be an absolute filesystem path.  The subprocess will be 
   * executed in an empty environment.  The working directory of the subprocess will be 
   * "/usr/tmp".
   * 
   * @param name  
   *   The name of the new thread.
   * 
   * @param program  
   *   The absolute filesystem path to the program to execute. 
   * 
   * @param args  
   *   The command line arguments of the program to execute.
   * 
   * @param outFile 
   *   The file to which all STDOUT output is redirected.
   * 
   * @param errFile 
   *   The file to which all STDERR output is redirected.
   * 
   * @throws PipelineException
   *   If unable to initialize the OS level process.
   */ 
  public
  SubProcessHeavy
  (
   String name,      
   File program,      
   ArrayList<String> args, 
   File outFile, 
   File errFile 
  )
    throws PipelineException
  {
    super(name);

    initHeavy(outFile, errFile);
    init(null, program.getPath(), args, 
	 new HashMap<String,String>(), PackageInfo.sTempPath.toFile());
  }

  /**
   * Create an OS level subprocess which will be executed as the current user. <P> 
   * 
   * The <CODE>program</CODE> can be an absolute filesystem path, a filesystem path relative 
   * to the working directory or a simple program name reachable through the environmental 
   * variable PATH. The environment <CODE>env</CODE> consists of a table of environmental 
   * variable name/value pairs.  Typically, this environment is corresponds to a Toolset.
   * 
   * @param name  
   *   The name of the new thread.
   * 
   * @param program  
   *   The name of program to execute as an OS level subprocess.  
   * 
   * @param args  
   *   The command line arguments of the program to execute.
   * 
   * @param env  
   *   The environment under which the OS level process is run.  
   * 
   * @param dir  
   *   The working directory of the OS level process.   
   * 
   * @param outFile 
   *   The file to which all STDOUT output is redirected.
   * 
   * @param errFile 
   *   The file to which all STDERR output is redirected.
   * 
   * @throws PipelineException
   *   If unable to initialize the OS level process.
   */ 
  public 
  SubProcessHeavy
  (
   String name,      
   String program,      
   ArrayList<String> args, 
   Map<String,String> env,      
   File dir, 
   File outFile, 
   File errFile 
  )
    throws PipelineException
  {
    super(name);

    initHeavy(outFile, errFile);
    init(null, program, args, env, dir);
  }

  /**
   * Create an OS level subprocess which will be executed as the given user. <P> 
   * 
   * The Pipeline utility program <I>plrun(1)</I> is used to run the subprocess as another 
   * user. Due to the fact that the <I>plrun(1)</I> utility can only be run by the 
   * "pipeline" user, this constructor will throw a {@link PipelineArgumentException} if 
   * instantiated by any other user. Running a subprocess as another user is not supported 
   * on Windows.<P>
   * 
   * The <CODE>program</CODE> can be an absolute filesystem path, a filesystem path relative 
   * to the working directory or a simple program name reachable through the environmental 
   * variable PATH. The environment <CODE>env</CODE> consists of a table of environmental 
   * variable name/value pairs.  Typically, this environment is corresponds to a Toolset. 
   * 
   * @param user  
   *   The username which will own the OS level subprocess.
   * 
   * @param name  
   *   The name of the new thread.
   * 
   * @param program  
   *   The name of program to execute as an OS level subprocess.  
   * 
   * @param args  
   *   The command line arguments of the program to execute.
   * 
   * @param env  
   *   The environment under which the OS level process is run.  
   * 
   * @param dir  
   *   The working directory of the OS level process.   
   * 
   * @param outFile 
   *   The file to which all STDOUT output is redirected.
   * 
   * @param errFile 
   *   The file to which all STDERR output is redirected.
   * 
   * @throws PipelineException
   *   If unable to initialize the OS level process.
   */ 
  public 
  SubProcessHeavy
  (
   String user,     
   String name,      
   String program,      
   ArrayList<String> args, 
   Map<String,String> env,      
   File dir, 
   File outFile, 
   File errFile 
  )
    throws PipelineException
  {
    super(name);

    initHeavy(outFile, errFile);

    if(user == null) 
      throw new PipelineException("The user name cannot be (null)!");

    if(PackageInfo.sUser.equals(user)) {
      init(null, program, args, env, dir);
    }
    else {
      if(!PackageInfo.sUser.equals(PackageInfo.sPipelineUser))
	throw new PipelineException
	  ("Only the (" + PackageInfo.sPipelineUser + ") may run a process as " + 
	   "another user!");

      /* To get around the dynamic linker stripping LD_LIBRARY_PATH (Linux) or
         DYLD_LIBRARY_PATH (Mac OS X) from the environment due to plrun(1) being 
         a setuid program.  We copy the dynamic library search path to the 
         PIPELINE_LD_LIBRARY_PATH temporary variable so that plrun(1) can restore 
         it before exec'ing the target program. */
      HashMap<String,String> uenv = new HashMap<String,String>(env); 
      {
        switch(PackageInfo.sOsType) {
        case Unix:
        case MacOS:
          {
            String path = uenv.get("LD_LIBRARY_PATH");
            if(path != null) 
              uenv.put("PIPELINE_LD_LIBRARY_PATH", path); 
          }
        }
        
        switch(PackageInfo.sOsType) {
        case MacOS:
          {
            String path = uenv.get("DYLD_LIBRARY_PATH");
            if(path != null) 
              uenv.put("PIPELINE_DYLD_LIBRARY_PATH", path); 
          }
        }
      }
      
      init(user, program, args, uenv, dir);
    }
  }



  /*-- INITIALIZATION HELPERS --------------------------------------------------------------*/

  /**
   * Initialize the heavy-only fields.
   * 
   * @param outFile 
   *   The file to which all STDOUT output is redirected.
   * 
   * @param errFile 
   *   The file to which all STDERR output is redirected.
   * 
   * @throws PipelineException
   *   If unable to initialize the OS level process.
   */ 
  private void 
  initHeavy
  (
   File outFile, 
   File errFile 
  ) 
    throws PipelineException
  {
    if(outFile == null) 
      throw new PipelineException
	("The STDOUT output file cannot be (null)!");
    pStdOutFile = outFile;

    if(errFile == null) 
      throw new PipelineException
	("The STDERR output file cannot be (null)!");
    pStdErrFile = errFile;
  }

  /**
   * Instantiate the underlying native process. <P> 
   *
   * @param cmd 
   *   The command line arguments used to launch the OS level process.  The first element
   *   of the array <CODE>cmd[0]</CODE> is the name of the program to run.
   * 
   * @param env 
   *   The environment under which the OS level process is run.  The <CODE>env</CODE> 
   *   argument must consist of environmental variable name/value pairs of the form 
   *   <CODE>"name=value"</CODE>.
   * 
   * @param dir 
   *   The working directory of the OS level process.
   * 
   * @return 
   *   The newly instantiated native process.
   * 
   * @throws PipelineException
   *   If unable to initialize the OS level process.
   */ 
  protected NativeProcess
  initNativeProcess
  (
   String[] cmd,       
   String[] env,      
   File dir       
  )  
    throws PipelineException
  {
    return (new NativeProcessHeavy(cmd, env, dir, pStdOutFile, pStdErrFile));
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   I / O                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the file to which all STDOUT output is redirected.
   */ 
  public File 
  getStdOutFile() 
  {
    return pStdOutFile; 
  }

  /**
   * Get the file to which all STDERR output is redirected.
   */ 
  public File 
  getStdErrFile() 
  {
    return pStdErrFile; 
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   R U N   S T A T I S T I C S                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Collect process resource usage statistics for the native processes and their children.
   */ 
  public static void 
  collectStats()
  {
    if(sProcStats != null) 
      sProcStats.collect();
  }

  /**
   * Cease monitoring resource usage statistics for the native process and release 
   * any statistics resources associated with the process.
   */ 
  public void 
  freeStats() 
  {
    if(sProcStats != null)     
      sProcStats.unmonitor(pProc.getPid());
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the number of seconds the process and its children have been scheduled in 
   * user mode.
   * 
   * @return 
   *   The time in seconds.
   */
  public Double
  getUserTime() 
  {
    double timeA = 0.0;
    if(sProcStats != null) {  
      Double time = sProcStats.getUserTime(pProc.getPid());
      if(time != null)
	timeA = time;
    }

    double timeB = 0.0;
    {
      Double time = pProc.getUserTime();
      if(time != null)
	timeB = time;
    }

    return Math.max(timeA, timeB);
  }
  
  /**
   * Get the number of seconds the process and its children have been scheduled in 
   * kernel mode.
   * 
   * @return 
   *   The time in seconds.
   */
  public Double
  getSystemTime() 
  {
    double timeA = 0.0;
    if(sProcStats != null) { 
      Double time = sProcStats.getSystemTime(pProc.getPid());
      if(time != null)
	timeA = time;
    }

    double timeB = 0.0;
    {
      Double time = pProc.getSystemTime();
      if(time != null)
	timeB = time;
    }

    return Math.max(timeA, timeB);
  }
  
  /**
   * Get the number of major faults which occured for the process and its children which 
   * have required loading a memory page from disk.
   * 
   * @return 
   *   The number of faults.
   */
  public Long
  getPageFaults() 
  { 
    long faultsA = 0L;
    if(sProcStats != null) {
      Long faults = sProcStats.getPageFaults(pProc.getPid());
      if(faults != null) 
	faultsA = faults;
    }

    long faultsB = 0L;
    {
      Long faults = pProc.getPageFaults();
      if(faults != null) 
	faultsB = faults;
    }

    return Math.max(faultsA, faultsB);
  }

  /**
   * Get the maximum virtual memory size of the process and its children in bytes.
   * 
   * @return 
   *   The size in bytes.
   */
  public Long
  getVirtualSize() 
  {
    long sizeA = 0L;
    if(sProcStats != null) {
      Long size = sProcStats.getVirtualSize(pProc.getPid());
      if(size != null) 
	sizeA = size;
    }

    long sizeB = 0L;
    {
      Long size = pProc.getVirtualSize();
      if(size != null) 
	sizeB = size;
    }

    return Math.max(sizeA, sizeB);
  }

  /**
   * Get the maximum resident memory size of the process and its children in bytes.
   * 
   * @return 
   *   The size in bytes.
   */
  public Long
  getResidentSize() 
  {
    long sizeA = 0L;
    if(sProcStats != null) {
      Long size = sProcStats.getResidentSize(pProc.getPid());
      if(size != null) 
	sizeA = size;
    }

    long sizeB = 0L;
    {
      Long size = pProc.getResidentSize();
      if(size != null) 
	sizeB = size;
    }

    return Math.max(sizeA, sizeB);
  }

  /**
   * Get the cumilative amount of memory swapped by the process and its children in bytes.
   * 
   * @return 
   *   The size in bytes.
   */
  public Long
  getSwappedSize()  
  {
    long sizeA = 0L;
    if(sProcStats != null) {
      Long size = sProcStats.getSwappedSize(pProc.getPid());
      if(size != null) 
	sizeA = size;
    }

    long sizeB = 0L;
    {
      Long size = pProc.getSwappedSize();
      if(size != null) 
	sizeB = size;
    }

    return Math.max(sizeA, sizeB);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   T H R E A D   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Runs the OS level process. <P>
   * 
   * In addition to starting the process, a thread is started to collect the resource 
   * useage statistics of the process at regular intervals.  This method returns only 
   * after the OS level process has exited and the collection thread has finished. <P>
   * 
   * Once this thread has finished, the exit code of the OS level process can be 
   * obtained by calling the {@link #getExitCode getExitCode} method.
   */
  public void 
  run() 
  {
    if(pIsFinished == null) 
      throw new IllegalStateException("The subprocess was never initialized!");

    if(pIsFinished.get()) 
      throw new IllegalStateException("The subprocess thread was already run!");

    /* log it... */ 
    LogMgr.getInstance().log
      (LogMgr.Kind.Sub, LogMgr.Level.Fine,
       getName() + " [command]: \"" + getCommand() + "\"");
    LogMgr.getInstance().log
      (LogMgr.Kind.Sub, LogMgr.Level.Finer,
       getName() + " [working directory]: " + getWorkingDir());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Sub, LogMgr.Level.Finest)) {
      String[] env = getEnvironment();
      StringBuilder buf = new StringBuilder();
      buf.append(getName() + " [environment]:\n");
      int wk;
      for(wk=0; wk<env.length; wk++) 
	buf.append("  " + env[wk] + "\n");
      LogMgr.getInstance().log
	(LogMgr.Kind.Sub, LogMgr.Level.Finest,
	 buf.toString());
	
      LogMgr.getInstance().flush();
    }

    /* the process collection task */ 
    StatsTask stats = null;
    switch(PackageInfo.sOsType) {
    case Unix:
      stats = new StatsTask(getName());
    }

    /* run the process... */ 
    String extraErrors = null;
    CloseStdInTask closeStdin = new CloseStdInTask(getName());
    try {
      /* start the process collection task */ 
      if(stats != null) 
	stats.start();

      /* start a task to close the STDIN task */ 
      closeStdin.start();

      /* launch the process wait for it to exit */ 
      pHasStarted.set(true);
      try {
	pExitCode = new Integer(pProc.exec());
      }
      catch(IOException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Sub, LogMgr.Level.Warning,
	   getName() + " [exec failed]: " + ex.getMessage());	  
	
	extraErrors = ex.getMessage();
	pExitCode = -1;
      }
      finally {
	if(pExitCode == null)
	  throw new IllegalStateException(); 
	pIsFinished.set(true);
      }

      /* wait for the tasks to finish... */ 
      closeStdin.join();
      if(stats != null) 
	stats.join();
    }
    catch (InterruptedException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Sub, LogMgr.Level.Warning,
	 getName() + " [interrupted]: " + ex.getMessage());

      if(stats != null)
	stats.interrupt();

      pExitCode = -2;
    }

    if(closeStdin.isAlive()) 
      throw new IllegalStateException(); 
    if((stats != null) && stats.isAlive())
      throw new IllegalStateException(); 
    
    /* append any IOException messages to the STDERR output */ 
    if(extraErrors != null) {
      try {
	FileWriter err = new FileWriter(pStdErrFile, true);
	err.write(extraErrors);
	err.flush();
	err.close();
      }
      catch(IOException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Sub, LogMgr.Level.Severe,
	   getName() + ": Could not append the IOException " + 
	   "message to STDERR file (" + pStdErrFile + ")!");
      }
    }

    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Sub, LogMgr.Level.Fine)) {
      StringBuilder buf = new StringBuilder();
      buf.append(getName() + " [exit]: ");
      
      if(pExitCode != null) {
	if(pExitCode == SUCCESS) 
	  buf.append("SUCCESS");
	else 
	  buf.append("FAILED with code (" + pExitCode + ")");
      }
      else {
	buf.append("ABORTED");
      }
      
      LogMgr.getInstance().log
	(LogMgr.Kind.Sub, LogMgr.Level.Fine,
	 buf.toString());
      LogMgr.getInstance().flush();
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A thread which waits for the process to start and then registers the process with 
   * the process resource usage statistics collection task.
   */ 
  protected 
  class StatsTask
    extends Thread
  {
    StatsTask
    (
      String name
    ) 
    {
      super(name + "[stats]");

      if(name == null)
	throw new IllegalArgumentException("The thread name cannot be (null)!");
      pName = name;
    }

    public void 
    run()
    {
      if(PackageInfo.sOsType != OsType.Unix)
	throw new IllegalStateException("The OS type must be Unix!");

      if(pIsFinished == null) 
	throw new IllegalStateException("The subprocess was never initialized!");

      LogMgr.getInstance().log
	(LogMgr.Kind.Sub, LogMgr.Level.Finest,
	 pName + " [stats]: thread started.");

      try {
	while(!pIsFinished.get() && !pProc.isRunning()) {
	  try {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Sub, LogMgr.Level.Finest,
	       pName + " [stats]: waiting (" + 
	       sCollectionDelay + ") milliseconds to start collection.");
	    sleep(sCollectionDelay);
	  }
	  catch(InterruptedException ex) {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Sub, LogMgr.Level.Severe,
	       pName + " [stats]: thread was interrupted while " + 
	       "waiting to start collection!");
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Sub, LogMgr.Level.Finest,
	       pName + " [stats]: thread finished.");
	    return;
	  }
	}

	if(!pIsFinished.get()) {
	  int pid = pProc.getPid();
	  if(pid == -1)
	    throw new IllegalStateException(); 
	  
	  sProcStats.monitor(pid);
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Sub, LogMgr.Level.Finer,
	     pName + " [stats]: monitoring process resource usage statistics.");
	}
      }
      catch (Exception ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Sub, LogMgr.Level.Severe,
	   pName + " [stats]:\n" + 
	   ex.getMessage());
      }
      
      LogMgr.getInstance().log
	(LogMgr.Kind.Sub, LogMgr.Level.Finest,
	 pName + " [stats]: thread finished.");
    }
    
    private String  pName;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * A thread which waits for the process to start and then closes the STDIN pipe.
   */ 
  protected 
  class CloseStdInTask
    extends Thread
  {
    CloseStdInTask
    (
      String name
    ) 
    {
      super(name);

      if(name == null)
	throw new IllegalArgumentException("The thread name cannot be (null)!");
      pName = name;
    }

    public void 
    run()
    {
      if(pIsFinished == null) 
	throw new IllegalStateException("The subprocess was never initialized!");

      LogMgr.getInstance().log
	(LogMgr.Kind.Sub, LogMgr.Level.Finest,
	 pName + " [stdin]: thread started.");

      try {
	while(!pIsFinished.get() && !pProc.isRunning()) {
	  try {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Sub, LogMgr.Level.Finest,
	       pName + " [stdin]: waiting (" + 
	       sCollectionDelay + ") milliseconds to close STDIN.");
	    sleep(sCollectionDelay);
	  }
	  catch(InterruptedException ex) {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Sub, LogMgr.Level.Severe,
	       pName + " [stdin]: thread was interrupted while " + 
	       "waiting to close STDIN!");
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Sub, LogMgr.Level.Finest,
	       pName + " [stdin]: thread finished.");
	    return;
	  }
	}

	((NativeProcessHeavy) pProc).closeStdIn();

	LogMgr.getInstance().log
	  (LogMgr.Kind.Sub, LogMgr.Level.Finest,
	   pName + " [stdin]: closed.");
      }
      catch (Exception ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Sub, LogMgr.Level.Severe,
	   pName + " [stdin]:\n" + 
	   ex.getMessage());
      }
      
      LogMgr.getInstance().log
	(LogMgr.Kind.Sub, LogMgr.Level.Finest,
	 pName + " [stdin]: thread finished.");
    }
    
    private String  pName;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The per-process resource usage statistics collector.
   */ 
  private static NativeProcessStats sProcStats = 
    ((PackageInfo.sOsType == OsType.Unix) ? new NativeProcessStats() : null);



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The file to which all STDOUT output is redirected. <P> 
   * 
   * This is identical to <CODE>pProc.getStdOutFile()</CODE> and is only duplicated due
   * to complexities of native process field initialization.
   */  
  private File pStdOutFile; 

  /**
   * The file to which all STDERR output is redirected. <P> 
   * 
   * This is identical to <CODE>pProc.getStdErrFile()</CODE> and is only duplicated due
   * to complexities of native process field initialization.
   */ 
  private File pStdErrFile; 

}



