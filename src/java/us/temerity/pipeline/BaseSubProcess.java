// $Id: BaseSubProcess.java,v 1.4 2005/01/22 06:10:09 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.util.concurrent.atomic.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   S U B P R O C E S S                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The common functionality shared by the {@link SubProcessLight SubProcessLight} and
 * {@link SubProcessHeavy SubProcessHeavy} classes. <P> 
 * 
 * These classes manaage a OS level subprocess with several Java threads.
 */
public abstract
class BaseSubProcess
  extends Thread 
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create an OS level subprocess. <P>
   * 
   * @param name  
   *   The name of the new thread.
   */ 
  protected 
  BaseSubProcess
  (
   String name
  )      
  {
    super(name);
  }



  /*-- INITIALIZATION HELPERS --------------------------------------------------------------*/

  /**
   * Initialized the fields. <P> 
   * 
   * @param user  
   *   The username which will own the OS level subprocess.
   * 
   * @param prog
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
  protected synchronized void 
  init
  (
   String user,    
   String prog,    
   ArrayList<String> args, 
   Map<String, String> env,      
   File dir    
  )
  {
    if((user != null) && (!PackageInfo.sUser.equals(PackageInfo.sPipelineUser))) 
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
		     "found using the PATH of the given environment!\n\n" +
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
	  String plrun = "plrun";
	  if(PackageInfo.sNativeSubdir != null) 
	    plrun = (PackageInfo.sNativeSubdir + "/plrun");
	  cmd.add(PackageInfo.sInstDir + "/sbin/" + plrun);

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
    pProc = initNativeProcess(procCmd, procEnv, dir);
    pIsFinished = new AtomicBoolean(false);
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
  protected abstract NativeProcess
  initNativeProcess
  (
   String[] cmd,       
   String[] env,      
   File dir       
  );
  
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

      SubProcessLight proc = 
	new SubProcessLight("LookupUserID", "id", args, 
			    System.getenv(), PackageInfo.sTempDir);
      try {
	proc.start();
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
    return ((pExitCode != null) && (pExitCode == SUCCESS));
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

  

  /*----------------------------------------------------------------------------------------*/
  /*   P R O C C E S S   M A N A G E M E N T                                                */
  /*----------------------------------------------------------------------------------------*/

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
	
	SubProcessLight killProc = 
	  new SubProcessLight(pSubstituteUser, "SignalSubProcess", 
			      "kill", args, env, PackageInfo.sTempDir);
	killProc.start(); 
	try {
	  killProc.join();
	} 
	catch(InterruptedException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Sub, LogMgr.Level.Severe,
	     "Interrupted while trying to send signal (" + signal + ") " + 
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
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Sub, LogMgr.Level.Warning,
	     "Unable to send signal (" + signal + ") to process [" + dpid + "]: " +
	     ex.getMessage());
	}
      }
      
      /* log it... */ 
      if(wasSignalled) 
	LogMgr.getInstance().log
	  (LogMgr.Kind.Sub, LogMgr.Level.Fine,
	   "Sent signal (" + signal + ") to process [" + dpid + "].");
    }
  }
  
  /** 
   * Scan the proc filesystem to determine the process IDs of all processes 
   * created by the process with the given ID.
   */ 
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

  /** 
   * Add all descendent processes of the given process to the hit list.
   */ 
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

  /** 
   * Generate a string containing both the exception message and stack trace. 
   * 
   * @param ex 
   *   The thrown exception.   
   */ 
  protected String 
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
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   C O N S T A N T S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The OS exit code value which indicates successful termination of the subprocess.
   */ 
  public static final int  SUCCESS = 0;



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The number of milliseconds between attempts to begin collecting output from the 
   * subprocess. 
   */
  protected static final long sCollectionDelay = 100;

  /**
   * A table of cached UIDs indexed by username.
   */
  protected static HashMap<String,Integer>  sUserIDs = new HashMap<String,Integer>();



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The low-level native process. 
   */
  protected NativeProcess  pProc;    

  /**
   * Has the <CODE>run</CODE> method been executed and returned?
   */
  protected AtomicBoolean pIsFinished;

  /**
   * The native OS exit code.  May be <CODE>null</CODE> if the process has not yet
   * been run or has failed to run. 
   */ 
  protected Integer  pExitCode;  

  /**
   * Name of the user to run the process under using <I>plrun(1)</I>. <P>
   * If <CODE>null</CODE>, the process is run as the current user directly. 
   */ 
  private String  pSubstituteUser;   


}



