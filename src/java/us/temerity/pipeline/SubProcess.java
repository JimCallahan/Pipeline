// $Id: SubProcess.java,v 1.12 2004/04/24 22:32:29 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.util.logging.*;
import java.util.concurrent.atomic.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   S U B P R O C E S S                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A Java {@link Thread Thread} which manages an OS level subprocess. <P> 
 * 
 * @see NativeProcess
 */
public
class SubProcess
  extends Thread 
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
  SubProcess
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
  SubProcess
  (
   String name,      
   String program,      
   ArrayList<String> args, 
   Map<String, String> env,      
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
  SubProcess
  (
   String user,     
   String name,      
   String program,      
   ArrayList<String> args, 
   Map<String, String> env,      
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
      uenv.put("WORKING", PackageInfo.sWorkDir + "/" + user);

      String val = uenv.get("LD_LIBRARY_PATH");
      if(val != null) 
	uenv.put("PIPELINE_LD_LIBRARY_PATH", val);
      
      init(user, program, args, uenv, dir);
    }
  }


  /*-- INITIALIZATION HELPERS --------------------------------------------------------------*/

  private synchronized void 
  init
  (
   String user,    
   String prog,    
   ArrayList<String> args, 
   Map<String, String> env,      
   File dir    
  )
  {
    if((user != null) && (!PackageInfo.sUser.equals("pipeline"))) 
      throw new IllegalArgumentException
	("Only the (pipeline) user is allowed to run processes as another user!");
    pSubstituteUser = user;

    /* build environment */ 
    if(env == null) 
      throw new IllegalArgumentException("The environment cannot be (null)!");
    String[] procEnv = null;
    {
      procEnv = new String[env.size()];
      int wk = 0;
      for(String name : env.keySet()) {
	if(name == null) 
	  throw new IllegalArgumentException("Found a (null) environmental variable!");
	
	String value = env.get(name);
	if(value == null) 
	  value = "";
	
	procEnv[wk] = (name + "=" + value);
	wk++;
      }
    }
    
    /* build command-line */ 
    if(prog == null)
      throw new IllegalArgumentException("The program cannot (null)!");
    String[] procCmd = null;
    {
      File file = new File(prog);
      if(file.isAbsolute()) {
	if(!file.exists()) 
	  throw new IllegalArgumentException
	    ("The program (" + prog + ") does not exist!");
      }
      else {
	String path = env.get("PATH");
	if(path == null) 
	  throw new IllegalArgumentException
	    ("The program (" + prog + ") was not absolute and no PATH was provided in " +
	     "the environment!");
	  
	ExecPath epath = new ExecPath(path);
	File absolute = epath.which(prog);
	if(absolute == null) {
	  StringBuffer buf = new StringBuffer();
	  buf.append("The program (" + prog + ") was not absolute and could not be " +
		     "found using the PATH of given environment!\n\n" +
		     "The directories which make up the PATH are: \n");
	    
	  for(File edir : epath.getDirectories()) 
	    buf.append("  " + edir + "\n");
	  
	  throw new IllegalArgumentException(buf.toString());
	}
	
	file = absolute;
      }
      
      {
	ArrayList<String> cmd = new ArrayList<String>();
	
	if(user != null) {
	  cmd.add(PackageInfo.sInstDir + "/sbin/plrun");
	  cmd.add(user);
	  cmd.add(lookupUserID(user).toString());
	}
	
	cmd.add(file.getPath());
	cmd.addAll(args);
	
	procCmd = new String[cmd.size()];
	cmd.toArray(procCmd);
      }
    }
    
    /* working directory */ 
    {
      assert(dir != null);
      if(!dir.isDirectory()) 
	throw new IllegalArgumentException
	  ("The working directory (" + dir.getPath() + ") does not exist!");
    } 
    
    /* create low-level process */ 
    pProc       = new NativeProcess(procCmd, procEnv, dir);
    pIsFinished = new AtomicBoolean(false);
    
    /* output */ 
    pOutLines = new ArrayList<String>();
    pErrLines = new ArrayList<String>();
    
    /* resource statistics */ 
    pStatsInterval = new AtomicLong(500);
  }

  
  /**
   * Use the UNIX utility "id" to lookup the UID for the given username.  A static table
   * of user IDs is maintained to optimize lookups.
   * 
   * @param  
   *   The name of the user. 
   */
  private Integer
  lookupUserID
  (
   String user
  )
  {
    synchronized(sUserIDs) {    
      /* see if this user's ID has already been determined */ 
      {
	Integer uid = sUserIDs.get(user);
	if(uid != null) 
	  return uid;
      }

      /* lookup the UID for the user */ 
      ArrayList<String> args = new ArrayList<String>();
      args.add("--user");
      args.add(user);

      SubProcess proc = 
	new SubProcess("LookupUserID", "id", args, System.getenv(), PackageInfo.sTempDir);
      proc.start();
      try {
	proc.join();
      } 
      catch(InterruptedException ex) {
	throw new IllegalArgumentException
	  ("Unable to determine the UID for user (" + user + "):\n" +  
	   ex.getMessage());
      }

      Integer exitCode = proc.getExitCode();

      if((exitCode != null) && (exitCode == SUCCESS)) {
	String out = proc.getStdOut();
	try {
	  Integer uid = new Integer(out.trim());
	  sUserIDs.put(user, uid);
	  return uid;
	}
	catch(NumberFormatException ex) {
	  throw new IllegalArgumentException
	    ("Illegal UID (" + out + ") found for user (" + user + ")!");
	}
      }
      else {
	throw new IllegalArgumentException(
	  "Unable to determine the UID for user (" + user + ")!");
      }
    }
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Gets the OS exit code of the process. <P> 
   * 
   * This method should only be called after the SubProcess thread has finished.
   * 
   * @return
   *   The exit code or <CODE>null</CODE> if execution failed.
   */
  public Integer
  getExitCode() 
  {
    if(isAlive())
      throw new IllegalStateException("The subprocess still running!");
    return pExitCode;
  }

  /** 
   * Did the OS level process exit with a {@link #SUCCESS SUCCESS} exit code? <P> 
   * 
   * This method should only be called after the SubProcess thread has finished.
   */
  public boolean 
  wasSuccessful() 
  {
    if(isAlive())
      throw new IllegalStateException("The subprocess still running!");
    return (pExitCode == SUCCESS);
  }


  /** 
   * Gets command line arguments of the OS level process.  
   * 
   * @return 
   *   The command line argument array.  The first element of this array contains the 
   *   name of the program.
   */
  public String[]
  getCommandLine()
  {
    return pProc.getCommandLine();
  }

  /** 
   * Gets the full command and arguments as a single <CODE>String</CODE>.
   */
  public String
  getCommand() 
  {
    return pProc.getCommand();
  }

  /**
   * Gets the environment under which the OS level process is run.
   * 
   * @return 
   *   The array of name/value pairs of the form <CODE>"name=value"</CODE>.
   */ 
  public String[]
  getEnvironment()
  {
    return pProc.getEnvironment();
  }

  /** 
   * Gets the working directory. 
   */ 
  public File 
  getWorkingDir() 
  {
    return pProc.getWorkingDir();
  }

  
  /**
   * Gets the number of seconds the OS level process was running in user space. <P>
   * 
   * This method should only be called after the this thread has finished.
   */
  public double
  getUserSecs() 
  {
    return pProc.getUserSecs();
  }

  /**
   * Gets the number of seconds the OS level process was running in system 
   * (kernel) space. <P>
   * 
   * This method should only be called after the this thread has finished.
   */
  public double
  getSystemSecs() 
  {
    return pProc.getSystemSecs();
  }


  /**
   * Gets the number of hard page faults during execution of the OS level process.  
   * A hard page fault is a memory fault that required I/O operations. <P>
   * 
   * This method should only be called after the this thread has finished.
   */
  public long
  getPageFaults() 
  {
    return pProc.getPageFaults(); 
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   I / O                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Gets the current collected STDOUT output of the process as a single 
   * <CODE>String</CODE>.  
   */
  public String
  getStdOut() 
  {
    synchronized(pOutLines) {
      StringBuffer buf = new StringBuffer();
      for(String line : pOutLines) 
	buf.append(line + "\n");
      return buf.toString();
    }
  }

  /**
   * Get the current collected lines of captured STDOUT starting at the given line. <P>
   * 
   * The returned array contains a line of output in each element.  Incremental output
   * from a currently running process is possible by incrementing the <CODE>start</CODE>
   * argument based on the number of previously returned lines of output.
   *
   * @param start 
   *   The index of the first line of output to return.  
   * 
   * @return 
   *   The lines of captured output.
   */ 
  public synchronized String[]
  getStdOutLines
  (
   int start  
  ) 
  {
    synchronized(pOutLines) {
      int numLines = pOutLines.size() - start;
      if(numLines < 0) 
	throw new ArrayIndexOutOfBoundsException(start);

      String[] lines = new String[numLines];
      int wk;
      for(wk=0; wk<lines.length; wk++) {
	lines[wk] = pOutLines.get(start+wk);
      }

      return lines;
    }
  }   
    
  /** 
   * Gets the current collected STDERR output of the process as a single 
   * <CODE>String</CODE>.  
   */
  public String
  getStdErr() 
  {
    synchronized(pErrLines) {
      StringBuffer buf = new StringBuffer();
      for(String line : pErrLines) 
	buf.append(line + "\n");
      return buf.toString();
    }
  }

  /**
   * Get the current collected lines of captured STDERR starting at the given line. <P>
   * 
   * The returned array contains a line of output in each element.  Incremental output
   * from a currently running process is possible by incrementing the <CODE>start</CODE>
   * argument based on the number of previously returned lines of output.
   *
   * @param start 
   *   The index of the first line of output to return.  
   * 
   * @return 
   *   The lines of captured output.
   */ 
  public synchronized String[]
  getStdErrLines
  (
   int start  
  ) 
  {
    synchronized(pErrLines) {
      int numLines = pErrLines.size() - start;
      if(numLines < 0) 
	throw new ArrayIndexOutOfBoundsException(start);

      String[] lines = new String[numLines];
      int wk;
      for(wk=0; wk<lines.length; wk++) {
	lines[wk] = pOutLines.get(start+wk);
      }

      return lines;
    }
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
    return pProc.getAverageVirtualSize();
  }

  /**
   * Gets the maximum virtual memory size of the OS level process in kilobytes. <P>
   * 
   * This method should only be called after the this thread has finished.
   */
  public long
  getMaxVirtualSize() 
  {
    return pProc.getMaxVirtualSize();
  }

  
  /**
   * Gets the average resident memory size of the OS level process in kilobytes. <P>
   * 
   * This method should only be called after the this thread has finished.
   */
  public long
  getAverageResidentSize() 
  {
    return pProc.getAverageResidentSize();
  }

  /**
   * Gets the maximum resident memory size of the OS level process in kilobytes. <P>
   * 
   * This method should only be called after the this thread has finished.
   */
  public long
  getMaxResidentSize() 
  {
    return pProc.getMaxResidentSize();
  }
  



  /*----------------------------------------------------------------------------------------*/
  /*   T H R E A D   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Runs the OS level process. <P>
   * 
   * In addition to starting the process, several threads are started to collect the 
   * output and resource useage statistics of the process.  This method returns only 
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
      
    /* the monitoring tasks */ 
    StdOutTask stdout = new StdOutTask(getName());
    StdErrTask stderr = new StdErrTask(getName());
    StatsTask  stats  = new StatsTask(getName());
    try {
      /* start the monitoring tasks */ 
      stdout.start();
      stderr.start();
      stats.start();
      
      /* launch the process and collect the exit code */ 
      try {
	pExitCode = new Integer(pProc.exec());
      }
      catch(IOException ex) {
	Logs.sub.warning(ex.getMessage());
      }
      finally {
	pIsFinished.set(true);
      }
      
      /* wait on the tasks to finish... */ 
      stdout.join();
      stderr.join();
      stats.join();
    }
    catch (InterruptedException ex) {
      stdout.interrupt();
      stderr.interrupt();
      stats.interrupt();
    }
    
    assert(!stdout.isAlive());
    assert(!stderr.isAlive());
    assert(!stats.isAlive());
    
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


  /** 
   * Kill the OS level process and all of its child processes.
   */
  public void
  kill() 
  {
    signal(PosixSignal.SIGKILL);
  }
  
  /**
   * Send a POSIX signal to the OS level process and all of its child processes.
   * 
   * @param signal  
   *   The signal to send.
   * 
   * @throws IOException 
   *   If unable to sent the signal to the OS level process. 
   */ 
  public void
  signal
  (
   PosixSignal signal 
  ) 
  {
    /* abort early if the process has already finished */ 
    if(!isAlive()) 
      return;

    /* get the OS process ID */ 
    int pid = pProc.getPid();
    if(pid == -1) 
      throw new IllegalStateException("The subprocess has not been started!");

    /* search out all descendent processes of (pid) and kill them */ 
    Map<String,String> env = System.getenv();
    for(Integer dpid : buildHitList(pid)) {
      assert(dpid > 0);
      boolean wasSignalled = false;
      
      if(pSubstituteUser != null) {
	ArrayList<String> args = new ArrayList<String>();
	args.add("-s");
	args.add(signal.toString());
	args.add(dpid.toString());
	
	SubProcess killProc = 
	  new SubProcess(pSubstituteUser, "SignalSubProcess", 
			 "kill", args, env, PackageInfo.sTempDir);
	killProc.start(); 
	try {
	  killProc.join();
	} 
	catch(InterruptedException ex) {
	  Logs.sub.severe("Interrupted while trying to send signal (" + signal + ") " + 
			  "to process [" + dpid + "] using plrun(1):\n" +
			  "  " + ex.getMessage());
	}
	wasSignalled = true;
      }
      else {
	try {
	  pProc.signal(signal, dpid);
	  wasSignalled = true;
	}
	catch(IOException ex) {
	  Logs.sub.warning(
            "Unable to send signal (" + signal + ") to process [" + dpid + "]: " +
	    ex.getMessage());
	}
      }
      
      /* log it... */ 
      if(wasSignalled) 
	Logs.sub.fine("Sent signal (" + signal + ") to process [" + dpid + "].");
    }
  }

  
  private TreeSet<Integer>
  buildHitList
  (
   int pid
  ) 
  {
    /* build a table of child process IDs indexed by parent process ID */ 
    TreeMap<Integer,TreeSet<Integer>> PIDs = new TreeMap<Integer,TreeSet<Integer>>();
    {
      File proc = new File("/proc");
      String[] procs = proc.list(new ProcFilter());
      int wk;
      for(wk=0; wk<procs.length; wk++) {
	try {
	  FileReader reader = new FileReader("/proc/" + procs[wk] + "/stat");
	  StringBuffer buf = new StringBuffer();
	  while(true) {
	    int next = reader.read();
	    if(next == -1) 
	      break;
	    buf.append((char) next);
	  }
	  reader.close();
	  
	  String[] fields = buf.toString().split(" ");
	  Integer cpid = new Integer(fields[0]);
	  Integer ppid = new Integer(fields[3]);
	  
	  TreeSet<Integer> children = PIDs.get(ppid);
	  if(children == null) {
	    children = new TreeSet<Integer>();
	    PIDs.put(ppid, children);
	  }

	  children.add(cpid);
	}
	catch (FileNotFoundException ex) {
	}
	catch(IOException ex) {
	}
      }
    }

    /* build a list of all descendent processes from (pid) */ 
    TreeSet<Integer> dead = new TreeSet<Integer>();
    buildHitListHelper(new Integer(pid), PIDs, dead);
    return dead;
  }


  private void
  buildHitListHelper
  (
   Integer pid, 
   TreeMap<Integer,TreeSet<Integer>> PIDs, 
   TreeSet<Integer> dead 
  ) 
  {
    dead.add(pid);

    TreeSet<Integer> children = PIDs.get(pid);
    if(children != null) {
      for(Integer cpid : children) 
	buildHitListHelper(cpid, PIDs, dead);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * A filter which selects process filenames in the /proc filesystem. 
   */
  private 
  class ProcFilter
    implements FilenameFilter
  {
    public boolean 
    accept
    (
     File dir, 
     String name
    ) 
    {
      File file = new File(dir, name);
      return (file.isDirectory() && name.matches("[0-9]+"));
    }
  }
  
  
  /**
   * A thread to gather resource usage statistics for the native process. 
   */ 
  private  
  class StatsTask
    extends Thread
  {
    StatsTask
    (
      String name
    ) 
    {
      if(name == null)
	throw new IllegalArgumentException("The name cannot be (null)!");
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
	pProc.collectStats(pStatsInterval.get());
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
      if(name == null)
	throw new IllegalArgumentException("The name cannot be (null)!");
      pName = name;

      if(stream == null)
	throw new IllegalArgumentException("The output stream name cannot be (null)!");
      pStream = stream;
    }

    public abstract String 
    getNext()
      throws IOException;

    public abstract void 
    addLine
    (
     String line
    );

    public abstract void 
    closePipe()
      throws IOException;

    public void 
    run() 
    { 
      if(pIsFinished == null) 
	throw new IllegalStateException("The subprocess was never initialized!");

      Logs.sub.finest(pName + " [" + pStream + "]: thread started.");

      try {
	while(!pIsFinished.get() && !pProc.isRunning()) {
	  try {
	    Logs.sub.finest(pName + " [" + pStream + "]: waiting (" + 
			    sCollectionDelay + ") milliseconds to start collection.");
	    sleep(sCollectionDelay);
	  }
	  catch(InterruptedException ex) {
	    throw new IllegalStateException
	      (pName + " [" + pStream + "]: thread was interrupted while " + 
	       "waiting to start collection!");
	  }
	}

	Logs.sub.finest(pName + " [" + pStream + "]: collecting...");	

	int lineNum = 0;
	StringBuffer buf = new StringBuffer(1024);
	while(true) {
	  String next = getNext();
	  if(next == null) {
	    if(buf.length() > 0) {
	      String line = buf.toString();
	      addLine(line);
	      lineNum++;
	      
	      Logs.sub.finer(pName + " [" + pStream + "] " + lineNum + ": " + line);
	      Logs.flush();
	    }
	    break;
	  }
	  else {
	    char[] ca = next.toCharArray();
	    int wk;
	    for(wk=0; wk<ca.length; wk++) {
	      if(ca[wk] == '\n') {
		String line = buf.toString();
		addLine(line);
		lineNum++;

		Logs.sub.finer(pName + " [" + pStream + "] " + lineNum + ": " + line);
		Logs.flush();

		buf = new StringBuffer(1024);
	      }
	      else {
		buf.append(ca[wk]);
	      }
	    }
	  }
	}

	Logs.sub.finest(pName + " [" + pStream + "]: closed.");	
	Logs.flush();
	
	closePipe();
      }
      catch (IOException ex) {
	Logs.sub.severe("IO Error while reading from " + pName + " [" + pStream + "]\n" + 
			ex.getMessage());
      }

      Logs.sub.finest(pName + " [" + pStream + "]: thread finished.");
    }

    private String  pName;
    private String  pStream;
  }


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

    public String 
    getNext() 
      throws IOException
    {
      return pProc.readFromStdOut(1024);
    }

    public void
    addLine
    (
     String line
    )
    {
      synchronized(pOutLines) {
	pOutLines.add(line);
      }
    }

    public void 
    closePipe()
      throws IOException
    {
      pProc.closeStdOut();
    }
  };


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

    public String 
    getNext() 
      throws IOException
    {
      return pProc.readFromStdErr(1024);
    }

    public void
    addLine
    (
     String line
    )
    {
      synchronized(pErrLines) {
	pErrLines.add(line);
      }
    }

    public void 
    closePipe()
      throws IOException
    {
      pProc.closeStdErr();
    }
  };


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   C O N S T A N T S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The OS exit code value which indicates successful termination of the subprocess.
   */ 
  public static final int  SUCCESS  = 0;



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A table of cached UIDs indexed by username.
   */
  private static HashMap<String,Integer>  sUserIDs = new HashMap<String,Integer>();

  /**
   * The number of milliseconds between attempts to start collecting output from the 
   * subprocess. 
   */
  private static final long sCollectionDelay = 100;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The low-level native process. 
   */
  private NativeProcess  pProc;    

  /**
   * Has the <CODE>run</CODE> method been executed and returned?
   */
  private AtomicBoolean pIsFinished;

  /**
   * The native OS exit code.  May be <CODE>null</CODE> if the process has not yet
   * been run or has failed to run. 
   */ 
  private Integer  pExitCode;  

  /**
   * Name of the user to run the process under using <I>plrun(1)</I>. <P>
   * If <CODE>null</CODE>, the process is run as the current user directly. 
   */ 
  private String  pSubstituteUser;   

  /**
   * Lines of output collected from the STDOUT of the OS level process. 
   */ 
  private ArrayList<String>  pOutLines;      

  /**
   * Lines of output collected from the STDERR of the OS level process. 
   */ 
  private ArrayList<String>  pErrLines;    

  /**
   * The number of milliseconds between resource usage statistics queries. 
   */ 
  private AtomicLong  pStatsInterval; 

}



