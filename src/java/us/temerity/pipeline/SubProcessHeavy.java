// $Id: SubProcessHeavy.java,v 1.1 2004/10/28 15:55:23 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.util.logging.*;
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

    pStatsInterval = new AtomicLong(500);
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
   * Write the given string data to the STDIN of the OS level process. 
   * 
   * @param input 
   *   The data to write to the STDIN of the OS level process.
   * 
   * @return
   *   The number of characters written. 
   */ 
  public native int
  writeToStdIn
  (
   String input
  ) 
    throws IOException;

  /** 
   * Close the STDIN pipe. 
   */ 
  public native void  
  closeStdIn() 
    throws IOException;


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
   * Gets the number of milliseconds between resource usage statistics queries. 
   */ 
  public long
  getStatsInterval() 
  {
    return pStatsInterval.get();
  }

  /** 
   * Sets the number of milliseconds between resource usage statistics queries. 
   */ 
  public void 
  setStatsInterval
  (
   long millis
  ) 
  {
    if(millis <= 0)
      throw new IllegalArgumentException
	("Resource statistics collection interval (" + millis + ") must be positive!");
    pStatsInterval.set(millis);
  }


  /**
   * Gets the average virtual memory size of the OS level process in kilobytes. <P>
   * 
   * This method should only be called after the this thread has finished.
   */
  public long
  getAverageVirtualSize() 
  {
    return ((NativeProcessHeavy) pProc).getAverageVirtualSize();
  }

  /**
   * Gets the maximum virtual memory size of the OS level process in kilobytes. <P>
   * 
   * This method should only be called after the this thread has finished.
   */
  public long
  getMaxVirtualSize() 
  {
    return ((NativeProcessHeavy) pProc).getMaxVirtualSize();
  }

  
  /**
   * Gets the average resident memory size of the OS level process in kilobytes. <P>
   * 
   * This method should only be called after the this thread has finished.
   */
  public long
  getAverageResidentSize() 
  {
    return ((NativeProcessHeavy) pProc).getAverageResidentSize();
  }

  /**
   * Gets the maximum resident memory size of the OS level process in kilobytes. <P>
   * 
   * This method should only be called after the this thread has finished.
   */
  public long
  getMaxResidentSize() 
  {
    return ((NativeProcessHeavy) pProc).getMaxResidentSize();
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
    if(Logs.sub.isLoggable(Level.FINEST)) {
      Logs.sub.fine(getName() + " [command]: \"" + getCommand() + "\"");
      Logs.sub.finer(getName() + " [working directory]: " + getWorkingDir());
      
      String[] env = getEnvironment();
      StringBuffer buf = new StringBuffer();
      buf.append(getName() + " [environment]:\n");
      int wk;
      for(wk=0; wk<env.length; wk++) 
	buf.append("  " + env[wk] + "\n");
      Logs.sub.finest(buf.toString());
	
      Logs.flush();
    }
      
    /* run the process... */ 
    String extraErrors = null;
    StatsTask stats = new StatsTask(getName());
    try {
      /* start the output collection task */ 
      stats.start();
      
      /* launch the process wait for it to exit */ 
      try {
	pExitCode = new Integer(pProc.exec());
      }
      catch(IOException ex) {
	Logs.sub.warning(getName() + " [exec failed]: " + ex.getMessage());	  
	
	extraErrors = ex.getMessage();
	pExitCode = -1;
      }
      finally {
	assert(pExitCode != null);
	pIsFinished.set(true);
      }
      
      /* wait on the collection task to finish... */ 
      stats.join();
    }
    catch (InterruptedException ex) {
      Logs.sub.warning(getName() + " [interrupted]: " + ex.getMessage());

      stats.interrupt();
      pExitCode = -2;
    }
    
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
	Logs.sub.severe(getName() + ": Could not append the IOException " + 
			"message to STDERR file (" + pStdErrFile + ")!");
      }
    }

    if(Logs.sub.isLoggable(Level.FINE)) {
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
      
      Logs.sub.fine(buf.toString());
      Logs.flush();
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A thread to gather resource usage statistics for the native process. 
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

      Logs.sub.finest(pName + " [stats]: thread started.");

      try {
	while(!pIsFinished.get() && !pProc.isRunning()) {
	  try {
	    Logs.sub.finest(pName + " [stats]: waiting (" + 
			    sCollectionDelay + ") milliseconds to start collection.");
	    sleep(sCollectionDelay);
	  }
	  catch(InterruptedException ex) {
	    Logs.sub.severe(pName + " [stats]: thread was interrupted while " + 
			    "waiting to start collection!");
	    Logs.sub.finest(pName + " [stats]: thread finished.");
	    return;
	  }
	}

	Logs.sub.finest(pName + " [stats]: collecting...");
	((NativeProcessHeavy) pProc).collectStats(pStatsInterval.get());
      }
      catch (IOException ex) {
	Logs.sub.severe("IO Error while collection resource usage statistics from " + 
			pName + " [stats]\n" + 
			ex.getMessage());
      }
      
      Logs.sub.finest(pName + " [stats]: thread finished.");
    }
    
    private String  pName;
  }



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

  /**
   * The number of milliseconds between resource usage statistics queries. 
   */ 
  private AtomicLong  pStatsInterval; 

}



