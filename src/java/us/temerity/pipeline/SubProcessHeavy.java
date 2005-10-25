// $Id: SubProcessHeavy.java,v 1.6 2005/10/25 10:56:01 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.util.concurrent.atomic.*;
import java.io.*;

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
  {
    super(name);

    initHeavy(outFile, errFile);
    init(null, program.getPath(), args, new HashMap<String,String>(), PackageInfo.sTempDir);
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
   * "pipeline" user, this constructor will throw an 
   * {@link IllegalArgumentException IllegalArgumentException} if instantiated by any 
   * other user. <P>
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
  {
    super(name);

    initHeavy(outFile, errFile);

    if(user == null) 
      throw new IllegalArgumentException("The user name cannot be (null)!");
    if(PackageInfo.sUser.equals(user)) {
      init(null, program, args, env, dir);
    }
    else {
      HashMap<String,String> uenv = new HashMap<String,String>(env);
      String val = uenv.get("LD_LIBRARY_PATH");
      if(val != null) 
	uenv.put("PIPELINE_LD_LIBRARY_PATH", val);
      
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
   */ 
  private void 
  initHeavy
  (
   File outFile, 
   File errFile 
  ) 
  {
    if(outFile == null) 
      throw new IllegalArgumentException
	("The STDOUT output file cannot be (null)!");
    pStdOutFile = outFile;

    if(errFile == null) 
      throw new IllegalArgumentException
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
   */ 
  protected NativeProcess
  initNativeProcess
  (
   String[] cmd,       
   String[] env,      
   File dir       
  )  
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
    sProcStats.collect();
  }

  /**
   * Cease monitoring resource usage statistics for the native process and release 
   * any statistics resources associated with the process.
   */ 
  public void 
  freeStats() 
  {
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
    {
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
   *   The time in seconds or <CODE>null</CODE> if no statistics have been collected 
   *   for the given process.
   */
  public Double
  getSystemTime() 
  {
    double timeA = 0.0;
    {
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
   *   The number of faults or <CODE>null</CODE> if no statistics have been collected 
   *   for the given process.
   */
  public Long
  getPageFaults() 
  {
    return sProcStats.getPageFaults(pProc.getPid());
  }

  /**
   * Get the maximum virtual memory size of the process and its children in bytes.
   * 
   * @return 
   *   The size in bytes or <CODE>null</CODE> if no statistics have been collected 
   *   for the given process.
   */
  public Long
  getVirtualSize() 
  {
    return sProcStats.getVirtualSize(pProc.getPid());
  }

  /**
   * Get the maximum resident memory size of the process and its children in bytes.
   * 
   * @return 
   *   The size in bytes or <CODE>null</CODE> if no statistics have been collected 
   *   for the given process.
   */
  public Long
  getResidentSize() 
  {
    return sProcStats.getResidentSize(pProc.getPid());
  }

  /**
   * Get the cumilative amount of memory swapped by the process and its children in bytes.
   * 
   * @return 
   *   The size in bytes or <CODE>null</CODE> if no statistics have been collected 
   *   for the given process.
   */
  public Long
  getSwappedSize()  
  {
    return sProcStats.getSwappedSize(pProc.getPid());
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
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Sub, LogMgr.Level.Finest)) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Sub, LogMgr.Level.Fine,
	 getName() + " [command]: \"" + getCommand() + "\"");
      LogMgr.getInstance().log
	(LogMgr.Kind.Sub, LogMgr.Level.Finer,
	 getName() + " [working directory]: " + getWorkingDir());
      
      String[] env = getEnvironment();
      StringBuffer buf = new StringBuffer();
      buf.append(getName() + " [environment]:\n");
      int wk;
      for(wk=0; wk<env.length; wk++) 
	buf.append("  " + env[wk] + "\n");
      LogMgr.getInstance().log
	(LogMgr.Kind.Sub, LogMgr.Level.Finest,
	 buf.toString());
	
      LogMgr.getInstance().flush();
    }
      
    /* run the process... */ 
    String extraErrors = null;
    StatsTask stats = new StatsTask(getName());
    CloseStdInTask closeStdin = new CloseStdInTask(getName());
    try {
      /* start the output collection task */ 
      stats.start();

      /* start a task to close the STDIN task */ 
      closeStdin.start();

      /* launch the process wait for it to exit */ 
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
	assert(pExitCode != null);
	pIsFinished.set(true);
      }

      /* wait on theclose the STDIN task to finish... */ 
      closeStdin.join();
      
      /* wait on the collection task to finish... */ 
      stats.join();
    }
    catch (InterruptedException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Sub, LogMgr.Level.Warning,
	 getName() + " [interrupted]: " + ex.getMessage());

      stats.interrupt();
      pExitCode = -2;
    }

    assert(!closeStdin.isAlive());
    assert(!stats.isAlive());
    
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
      StringBuffer buf = new StringBuffer();
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
	  assert(pid != -1);
	  
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
  private static NativeProcessStats  sProcStats = new NativeProcessStats();



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



