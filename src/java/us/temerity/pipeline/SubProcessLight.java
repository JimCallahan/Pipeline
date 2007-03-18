// $Id: SubProcessLight.java,v 1.16 2007/03/18 02:17:16 jim Exp $

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
  /*   S T A T I C   I N I T I A L I Z A T I O N                                            */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Initialize fields which must be determined at runtime.
   */ 
  static {
    switch(PackageInfo.sOsType) {
    case Unix:
    case MacOS:
      sArgMax = 131072L - 1024L - 256L;  
      // env + args + 1024(MAX_PATH_LEN) + 256? < 128k("getconf ARG_MAX")
      break;

    default:
      sArgMax = 32768L;
    }
  }



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
   * @throws PipelineException
   *   If unable to initialize the OS level process.
   */ 
  public 
  SubProcessLight
  (
   String name,      
   File program,      
   ArrayList<String> args
  )
    throws PipelineException
  {
    super(name);
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
   * @throws PipelineException
   *   If unable to initialize the OS level process.
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
    throws PipelineException
  {
    super(name);
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
   * @throws PipelineException
   *   If unable to initialize the OS level process. 
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
    throws PipelineException
  {
    super(name);

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

      HashMap<String,String> uenv = new HashMap<String,String>(env); 
      switch(PackageInfo.sOsType) {
      case Unix:
      case MacOS:
	{
	  String val = uenv.get("LD_LIBRARY_PATH");
	  if(val != null) 
	    uenv.put("PIPELINE_LD_LIBRARY_PATH", val);
	}
      }
      
      init(user, program, args, uenv, dir);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Create one or more OS level subprocess for a program with a possibly large number
   * of arguments which may be executed as the current user in several passes. <P> 
   * 
   * The calling conventions of the program must have the following form: 
   * <DIV style="margin-left: 40px;">
   *   program [pre-options] arg1 ... argN [post-options]
   * </DIV> <P> 
   * 
   * Where the program can be invoked with one or more of the (<I>arg</I>) options using 
   * the same (<I>pre/post-options</I>) in each invocation with identical results.  This 
   * method will attempt to invoke the program with as many arguments as possible without 
   * exceeding the maximum size of command line arguments for the underlying operating 
   * system.<P> 
   *
   * The Windows operating system is not supported by this method.
   * 
   * @param name 
   *   Name to give the created threads.
   * 
   * @param program
   *   The name of program to execute as an OS level subprocess.  
   * 
   * @param preOpts
   *   The fixed length pre-option arguments.
   * 
   * @param args 
   *   The variable length arguments.
   * 
   * @param postOpts
   *   The fixed length pre-option arguments.
   * 
   * @param env  
   *   The environment under which the OS level processes are run.  
   * 
   * @param dir  
   *   The working directory of the OS level processes.  
   * 
   * @throws PipelineException
   *   If unable to initialize the OS level process. 
   */
  public static LinkedList<SubProcessLight>
  createMultiSubProcess
  (
   String name, 
   String program, 
   ArrayList<String> preOpts, 
   ArrayList<String> args, 
   ArrayList<String> postOpts, 
   Map<String,String> env,      
   File dir
  ) 
    throws PipelineException
  {
    if(PackageInfo.sOsType.equals(OsType.Windows)) 
      throw new PipelineException
	("This method does now support the Windows operating system!"); 

    LinkedList<SubProcessLight> procs = new LinkedList<SubProcessLight>();
    if(args.isEmpty())
      return procs; 

    for(ArrayList<String> margs : splitMultiArgs(preOpts, args, postOpts, env))
      procs.add(new SubProcessLight(name, program, margs, env, dir));
    
    return procs;
  }

  /**
   * Create one or more OS level subprocess for a program with a possibly large number
   * of arguments which may be executed as the current user in several passes. <P> 
   * 
   * The calling conventions of the program must have the following form: 
   * <DIV style="margin-left: 40px;">
   *   program [pre-options] arg1 ... argN 
   * </DIV> <P> 
   * 
   * Where the program can be invoked with one or more of the (<I>arg</I>) options using 
   * the same (<I>pre-options</I>) in each invocation with identical results.  This 
   * method will attempt to invoke the program with as many arguments as possible without 
   * exceeding the maximum size of command line arguments for the underlying operating 
   * system.<P> 
   *
   * The Windows operating system is not supported by this method.
   * 
   * @param name 
   *   Name to give the created threads.
   * 
   * @param program
   *   The name of program to execute as an OS level subprocess.  
   * 
   * @param preOpts
   *   The fixed length pre-option arguments.
   * 
   * @param args 
   *   The variable length arguments.
   * 
   * @param env  
   *   The environment under which the OS level processes are run.  
   * 
   * @param dir  
   *   The working directory of the OS level processes.   
   * 
   * @throws PipelineException
   *   If unable to initialize the OS level process.
   */
  public static LinkedList<SubProcessLight>
  createMultiSubProcess
  (
   String name, 
   String program, 
   ArrayList<String> preOpts, 
   ArrayList<String> args, 
   Map<String,String> env,      
   File dir
  ) 
    throws PipelineException
  {
    return createMultiSubProcess(name, program, 
				 preOpts, args, new ArrayList<String>(), 
				 env, dir);
  }

  /**
   * Create one or more OS level subprocess for a program with a possibly large number
   * of arguments which may be executed as the given user in several passes. <P> 
   * 
   * The calling conventions of the program must have the following form: 
   * <DIV style="margin-left: 40px;">
   *   program [pre-options] arg1 ... argN [post-options]
   * </DIV> <P> 
   * 
   * Where the program can be invoked with one or more of the (<I>arg</I>) options using 
   * the same (<I>pre/post-options</I>) in each invocation with identical results.  This 
   * method will attempt to invoke the program with as many arguments as possible without 
   * exceeding the maximum size of command line arguments for the underlying operating 
   * system.<P> 
   * 
   * The Windows operating system is not supported by this method.
   *
   * @param user  
   *   The username which will own the OS level subprocesses.
   * 
   * @param name 
   *   Name to give the created threads.
   * 
   * @param program
   *   The name of program to execute as an OS level subprocess.  
   * 
   * @param preOpts
   *   The fixed length pre-option arguments.
   * 
   * @param args 
   *   The variable length arguments.
   * 
   * @param postOpts
   *   The fixed length pre-option arguments.
   * 
   * @param env  
   *   The environment under which the OS level processes are run.  
   * 
   * @param dir  
   *   The working directory of the OS level processes.  
   * 
   * @throws PipelineException
   *   If unable to initialize the OS level process. 
   */
  public static LinkedList<SubProcessLight>
  createMultiSubProcess
  (
   String user,  
   String name, 
   String program, 
   ArrayList<String> preOpts, 
   ArrayList<String> args, 
   ArrayList<String> postOpts, 
   Map<String,String> env,      
   File dir
  ) 
    throws PipelineException
  {
    if(PackageInfo.sOsType.equals(OsType.Windows)) 
      throw new PipelineException
	("This method does now support the Windows operating system!"); 

    LinkedList<SubProcessLight> procs = new LinkedList<SubProcessLight>();
    if(args.isEmpty())
      return procs; 

    for(ArrayList<String> margs : splitMultiArgs(preOpts, args, postOpts, env))
      procs.add(new SubProcessLight(user, name, program, margs, env, dir));
    
    return procs;
  }

  /**
   * Create one or more OS level subprocess for a program with a possibly large number
   * of arguments which may be executed as the given user in several passes. <P> 
   * 
   * The calling conventions of the program must have the following form: 
   * <DIV style="margin-left: 40px;">
   *   program [pre-options] arg1 ... argN 
   * </DIV> <P> 
   * 
   * Where the program can be invoked with one or more of the (<I>arg</I>) options using 
   * the same (<I>pre-options</I>) in each invocation with identical results.  This 
   * method will attempt to invoke the program with as many arguments as possible without 
   * exceeding the maximum size of command line arguments for the underlying operating 
   * system.<P> 
   * 
   * The Windows operating system is not supported by this method.
   *
   * @param user  
   *   The username which will own the OS level subprocesses.
   * 
   * @param name 
   *   Name to give the created threads.
   * 
   * @param program
   *   The name of program to execute as an OS level subprocess.  
   * 
   * @param preOpts
   *   The fixed length pre-option arguments.
   * 
   * @param args 
   *   The variable length arguments.
   * 
   * @param env  
   *   The environment under which the OS level processes are run.  
   * 
   * @param dir  
   *   The working directory of the OS level processes.  
   * 
   * @throws PipelineException
   *   If unable to initialize the OS level process. 
   */
  public static LinkedList<SubProcessLight>
  createMultiSubProcess
  (
   String user,  
   String name, 
   String program, 
   ArrayList<String> preOpts, 
   ArrayList<String> args, 
   Map<String,String> env,      
   File dir
  ) 
    throws PipelineException
  {
    return createMultiSubProcess(user, name, program, 
				 preOpts, args, new ArrayList<String>(), 
				 env, dir);
  }

  /**
   * Split into multiple invocations which do not exceed the argument length limits.
   */ 
  private static LinkedList<ArrayList<String>> 
  splitMultiArgs
  (
   ArrayList<String> preOpts, 
   ArrayList<String> multiOpts, 
   ArrayList<String> postOpts, 
   Map<String,String> env   
  ) 
  {
    LinkedList<ArrayList<String>> margs = new LinkedList<ArrayList<String>>();

    long envLen = 0L;
    for(String key : env.keySet()) {
      envLen += key.length() + 2;

      String value = env.get(key);
      if(value != null) 
	envLen += value.length();
    }

    long preLen = 0L;
    for(String opt : preOpts) 
      preLen += opt.length() + 1;
    
    long postLen = 0L;
    for(String opt : postOpts) 
      postLen += opt.length() + 1;
    
    long total = preLen + postLen + envLen; 
    ArrayList<String> args = null; 
    for(String mopt : multiOpts) {
      int len = mopt.length() + 1;
      
      if((args == null) || ((total + len) > sArgMax)) {
	if(args != null) {
	  for(String opt : postOpts) 
	    args.add(opt);
	}
	
	args = new ArrayList<String>();
	margs.add(args);
	
	total = preLen + postLen + envLen;
	for(String opt : preOpts) 
	  args.add(opt);
      }
      
      total += len;
      args.add(mopt);	  
    }
    
    for(String opt : postOpts) 
      args.add(opt);

    return margs; 
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

  /**
   * Get the number of major faults which occured for the process which have required 
   * loading a memory page from disk.
   * 
   * @return 
   *   The number of faults or <CODE>null</CODE> if the process is still running.
   */
  public Long
  getPageFaults() 
  { 
    return ((NativeProcessLight) pProc).getPageFaults();
  }

  /**
   * Get the maximum virtual memory size of the process.
   * 
   * @return 
   *   The size in bytes or <CODE>null</CODE> if the process is still running.
   */
  public Long
  getVirtualSize() 
  {
    return ((NativeProcessLight) pProc).getVirtualSize();
  }
  
  /**
   * Get the maximum resident memory size of the process (in bytes).
   * 
   * @return 
   *   The size in bytes or <CODE>null</CODE> if the process is still running.
   */
  public Long
  getResidentSize() 
  {
    return ((NativeProcessLight) pProc).getResidentSize();
  }

  /**
   * Get the cumilative amount of memory swapped by the process (in bytes).
   * 
   * @return 
   *   The size in bytes or <CODE>null</CODE> if the process is still running.
   */
  public Long
  getSwappedSize()  
  {
    return ((NativeProcessLight) pProc).getSwappedSize();
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
      
    /* run the process... */ 
    String extraErrors = null;
    StdOutTask stdout = new StdOutTask(getName());
    StdErrTask stderr = new StdErrTask(getName());
    CloseStdInTask closeStdin = new CloseStdInTask(getName());
    try {
      /* start the output collection tasks */ 
      stdout.start();
      stderr.start();
      
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
      catch(Exception ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Sub, LogMgr.Level.Severe,
	   getName() + " [internal error]: " + getFullMessage(ex));
	LogMgr.getInstance().flush();

	pExitCode = -2;
      }
      finally {
	if(pExitCode == null)
	  throw new IllegalStateException(); 
	pIsFinished.set(true);
      }
	
      /* wait on theclose the STDIN task to finish... */ 
      closeStdin.join();

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

    if(closeStdin.isAlive() || stdout.isAlive() || stderr.isAlive())
      throw new IllegalStateException(); 

    /* append any IOException messages to the STDERR output */ 
    if(extraErrors != null) {
      synchronized(pErrors) {
	pErrors.append(extraErrors);
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

	((NativeProcessLight) pProc).closeStdIn();

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
   * The maximum length of all command line arguments passed to a subprocess.
   */ 
  private static final long sArgMax;



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



