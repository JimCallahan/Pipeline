// $Id: SubProcessLight.java,v 1.5 2005/01/22 06:10:09 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.util.concurrent.atomic.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   S U B P R O C E S S    L I G H T                                                       */
/*------------------------------------------------------------------------------------------*/

/**
 * A Java {@link Thread Thread} which manages an OS level subprocess. <P> 
 * 
 * Large amounts of OS level process output are not efficiently handled by this class
 * and will result in high rates of garbage generation and low overall performance.  The 
 * {@link SubProcessHeavy SubProcessHeavy} class should be used for thse kinds of 
 * processes. <P> 
 */
public
class SubProcessLight
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
   */ 
  public 
  SubProcessLight
  (
   String name,      
   File program,      
   ArrayList<String> args
  )
  {
    super(name);
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
   */ 
  public 
  SubProcessLight
  (
   String name,      
   String program,      
   ArrayList<String> args, 
   Map<String,String> env,      
   File dir
  )
  {
    super(name);
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
   */ 
  public 
  SubProcessLight
  (
   String user,     
   String name,      
   String program,      
   ArrayList<String> args, 
   Map<String,String> env,      
   File dir
  )
  {
    super(name);

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
    pOutput = new StringBuffer();
    pErrors = new StringBuffer();
    
    return (new NativeProcessLight(cmd, env, dir));
  }

  
   
  /*----------------------------------------------------------------------------------------*/
  /*   R U N   S T A T I S T I C S                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the number of seconds the process has been scheduled in user mode.
   * 
   * @return 
   *   The time in seconds or <CODE>null</CODE> if the process is still running.
   */
  public Double
  getUserTime() 
  {
    return ((NativeProcessLight) pProc).getUserTime();
  }

  /**
   * Get the number of seconds the process has been scheduled in kernel mode.
   * 
   * @return 
   *   The time in seconds or <CODE>null</CODE> if the process is still running.
   */
  public Double
  getSystemTime() 
  {
    return ((NativeProcessLight) pProc).getSystemTime();
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   I / O                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the collected STDOUT. <P>
   */ 
  public synchronized String
  getStdOut()
  {
    synchronized(pOutput) {
      return pOutput.toString();
    }
  }   

  /**
   * Get the collected STDERR. <P>
   */ 
  public synchronized String
  getStdErr()
  {
    synchronized(pErrors) {
      return pErrors.toString();
    }
  }   
   


  /*----------------------------------------------------------------------------------------*/
  /*   T H R E A D   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Runs the OS level process. <P>
   * 
   * In addition to starting the process, Java threads are started to collect the 
   * STDOUT and STDERR output of the process.  This method returns only 
   * after the OS level process has exited and the collection threads have finished. <P>
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
    StdOutTask stdout = new StdOutTask(getName());
    StdErrTask stderr = new StdErrTask(getName());
    try {
      /* start the output collection tasks */ 
      stdout.start();
      stderr.start();
      
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
      catch(Exception ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Sub, LogMgr.Level.Severe,
	   getName() + " [internal error]: " + getFullMessage(ex));
	LogMgr.getInstance().flush();

	pExitCode = -2;
      }
      finally {
	assert(pExitCode != null);
	pIsFinished.set(true);
      }
	
      /* wait on the collection tasks to finish... */ 
      stdout.join();
      stderr.join();
    }
    catch (InterruptedException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Sub, LogMgr.Level.Warning,
	 getName() + " [interrupted]: " + ex.getMessage());

      stdout.interrupt();
      stderr.interrupt();
      pExitCode = -3;
    }

    try {
      ((NativeProcessLight) pProc).closeStdIn();
    }
    catch(IOException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Sub, LogMgr.Level.Warning,
	 getName() + " [close STDIN]: " + ex.getMessage());
    }

    assert(!stdout.isAlive());
    assert(!stderr.isAlive());
    
    /* append any IOException messages to the STDERR output */ 
    if(extraErrors != null) {
      synchronized(pErrors) {
	pErrors.append(extraErrors);
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
   * The base class for threads which gather output from the OS level process.
   */ 
  private abstract 
  class OutTask
    extends Thread
  {
    public 
    OutTask
    (
     String name,  
     String stream      
    ) 
    {
      super(name + "[" + stream + "]");

      if(name == null)
	throw new IllegalArgumentException("The name cannot be (null)!");
      pName = name;

      if(stream == null)
	throw new IllegalArgumentException("The output stream name cannot be (null)!");
      pStream = stream;
    }

    public abstract boolean
    appendNext() 
      throws IOException;

    public abstract void 
    closePipe()
      throws IOException;

    public abstract String
    getText(); 

    public void 
    run() 
    { 
      if(pIsFinished == null) 
	throw new IllegalStateException("The subprocess was never initialized!");

      LogMgr.getInstance().log
	(LogMgr.Kind.Sub, LogMgr.Level.Finest,
	 pName + " [" + pStream + "]: thread started.");

      try {
	while(!pIsFinished.get() && !pProc.isRunning()) {
	  try {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Sub, LogMgr.Level.Finest,
	       pName + " [" + pStream + "]: waiting (" + 
	       sCollectionDelay + ") milliseconds to start collection.");
	    sleep(sCollectionDelay);
	  }
	  catch(InterruptedException ex) {
	    throw new IllegalStateException
	      (pName + " [" + pStream + "]: thread was interrupted while " + 
	       "waiting to start collection!");
	  }
	}

	LogMgr.getInstance().log
	  (LogMgr.Kind.Sub, LogMgr.Level.Finest,
	   pName + " [" + pStream + "]: collecting...");	

	while(appendNext());
	closePipe();

	if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Sub, LogMgr.Level.Finest)) {
	  String text = getText(); 
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Sub, LogMgr.Level.Finest,
	     pName + " [" + pStream + "]: " + 
	     ((text != null) ? ("\n" + text + "\n") : "(none)"));
	}

	LogMgr.getInstance().log
	  (LogMgr.Kind.Sub, LogMgr.Level.Finest,
	   pName + " [" + pStream + "]: closed.");	
	LogMgr.getInstance().flush();
      }
      catch (IOException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Sub, LogMgr.Level.Severe,
	   "IO Error while reading from " + pName + " [" + pStream + "]\n" + 
	   ex.getMessage());
      }

      LogMgr.getInstance().log
	(LogMgr.Kind.Sub, LogMgr.Level.Finest,
	 pName + " [" + pStream + "]: thread finished.");
    }

    private String  pName;
    private String  pStream;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * A thread to gather output from STDOUT of the OS level process. 
   */  
  private 
  class StdOutTask
    extends OutTask
  {
    public 
    StdOutTask
    (
     String name  
    ) 
    {
      super(name, "stdout");
    }

    public boolean
    appendNext() 
      throws IOException
    {
      String text = ((NativeProcessLight) pProc).readFromStdOut(1024);
      if(text == null) 
	return false;

      synchronized(pOutput) {
	pOutput.append(text);
      }

      return true;
    }

    public void 
    closePipe()
      throws IOException
    {
      ((NativeProcessLight) pProc).closeStdOut();
    }

    public String
    getText()
    {
      return getStdOut();
    }
  };


  /*----------------------------------------------------------------------------------------*/

  /**
   * A thread to gather output from STDERR of the OS level process. 
   */ 
  private 
  class StdErrTask
    extends OutTask
  {
    public 
    StdErrTask
    (
     String name 
    ) 
    {
      super(name, "stderr");
    }

    public boolean
    appendNext() 
      throws IOException
    {
      String text = ((NativeProcessLight) pProc).readFromStdErr(1024);
      if(text == null) 
	return false;

      synchronized(pErrors) {
	pErrors.append(text);
      }

      return true;
    }

    public void 
    closePipe()
      throws IOException
    {
      ((NativeProcessLight) pProc).closeStdErr();
    }

    public String
    getText()
    {
      return getStdErr();
    }
  };

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The collected STDOUT of the OS level process. 
   */ 
  private StringBuffer  pOutput; 

  /**
   * The collected STDOUT of the OS level process. 
   */ 
  private StringBuffer  pErrors; 

}



