// $Id: BaseSubProcess.java,v 1.28 2008/05/12 04:07:49 jim Exp $

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
   * 
   * @throws PipelineException
   *   If unable to initialize the OS level process.
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
    throws PipelineException
  {
    if(!PackageInfo.sUser.equals(user)) {
      if((user != null) && (PackageInfo.sOsType == OsType.Windows))
        throw new PipelineException("Not supported on Windows!");

      pSubstituteUser = user;
    }

    /* build environment */ 
    if(env == null) 
      throw new PipelineException("The environment cannot be (null)!");
    TreeMap<String, String> nenv = new TreeMap<String,String>(env);
    String[] procEnv = null;
    {
      /* if the process is to be run by the same user as that which owns the parent process
	 and the X11 authority and display settings do not already exist in the child 
	 process environment, then clone their values from the parent environment */ 
      if(user == null) {
	switch(PackageInfo.sOsType) {
	case Unix:
	  if(!nenv.containsKey("XAUTHORITY")) {
	    String xauth = System.getenv("XAUTHORITY");
	    if((xauth != null) && (xauth.length() > 0))
	      nenv.put("XAUTHORITY", xauth);
	  }
	  
	  if(!nenv.containsKey("DISPLAY")) {
	    String display = System.getenv("DISPLAY");
	    if((display != null) && (display.length() > 0))
	      nenv.put("DISPLAY", display);
	  }
	}
      }

      procEnv = new String[nenv.size()];
      int wk = 0;
      for(String name : nenv.keySet()) {
	if(name == null) 
	  throw new PipelineException("Found a (null) environmental variable!");
	
	String value = nenv.get(name);
	if(value == null) 
	  value = "";
	
	procEnv[wk] = (name + "=" + value);
	wk++;
      }
    }
    
    /* build command-line */ 
    if(prog == null)
      throw new PipelineException("The program cannot (null)!");
    String[] procCmd = null;
    {
      File file = new File(prog);
      if(file.isAbsolute()) {
	if(!file.exists()) 
	  throw new PipelineException
	    ("The program (" + prog + ") does not exist!");
      }
      else {
	String path = nenv.get("PATH");

	if(path == null) {
	  switch(PackageInfo.sOsType) {
	  case Windows:
	    path = nenv.get("Path");
	  }
	}

	if(path == null) 
	  throw new PipelineException
	    ("The program (" + prog + ") was not absolute and no PATH was provided in " +
	     "the environment!");
	  
	ExecPath epath = new ExecPath(path);
	File absolute = epath.which(prog);
	if(absolute == null) {
	  StringBuilder buf = new StringBuilder();
	  buf.append("The program (" + prog + ") was not absolute and could not be " +
		     "found using the PATH of the given environment!\n\n" +
		     "The directories which make up the PATH are: \n");
	    
	  for(File edir : epath.getDirectories()) 
	    buf.append("  " + edir + "\n");
	  
	  throw new PipelineException(buf.toString());
	}
	
	file = absolute;
      }
      
      {
	ArrayList<String> cmd = new ArrayList<String>();
	
	if(user != null) {
          Path path = null; 
          switch(PackageInfo.sOsType) {
	  case Unix:
            path = new Path(PackageInfo.sInstPath, 
                            PackageInfo.sOsType + "-" + PackageInfo.sArchType + "-Opt" +
                            "/sbin/plrun");
            break;

	  case MacOS:
            path = new Path("/Library/Application Support/PipelineJobManager-" + 
                            PackageInfo.sCustomer + "-" + PackageInfo.sVersion + "-" + 
                            PackageInfo.sCustomerProfile + "/plrun");
          }

          if(path != null) {
            cmd.add(path.toOsString()); 
            cmd.add(user);
            cmd.add(lookupUserID(user).toString());
          }
	}
	
	cmd.add(file.getPath());

	int cnt = 0;
	for(String arg : args) {
	  if(arg == null) 
	    throw new PipelineException
	      ("The argument number (" + cnt + ") given for the program (" + prog + ") " + 
	       "was (null)! Subprocess arguments cannot contain (null) values.\n");

	  if(arg.length() == 0) 
	    throw new PipelineException
	      ("The argument number (" + cnt + ") given for the program (" + prog + ") " + 
	       "was an empty string!  All subprocess arguments must contain at least one " + 
	       "character.");
	  
	  cmd.add(arg);
	  cnt++;
	}
	
	procCmd = new String[cmd.size()];
	cmd.toArray(procCmd);
      }
    }
    
    /* working directory */ 
    {
      if(dir == null) 
	throw new PipelineException
	  ("The working directory cannot be (null)!");
      
      if(!dir.isDirectory()) 
	throw new PipelineException
	  ("The working directory (" + dir.getPath() + ") does not exist!");
    } 

    /* create low-level process */ 
    pProc = initNativeProcess(procCmd, procEnv, dir);
    pHasStarted = new AtomicBoolean(false);
    pIsFinished = new AtomicBoolean(false);

    /* save the execution details */ 
    pExecDetails = new SubProcessExecDetails(pProc.getCommand(), nenv);
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
  protected abstract NativeProcess
  initNativeProcess
  (
   String[] cmd,       
   String[] env,      
   File dir       
  )
    throws PipelineException;
  
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
    throws PipelineException
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
      args.add("-u");
      args.add(user);

      SubProcessLight proc = 
	new SubProcessLight("LookupUserID", "id", args, 
			    System.getenv(), PackageInfo.sTempPath.toFile());
      try {
	proc.start();
	proc.join();
      } 
      catch(InterruptedException ex) {
	throw new PipelineException
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
	  throw new PipelineException
	    ("Illegal UID (" + out + ") found for user (" + user + ")!");
	}
      }
      else {
	throw new PipelineException(
	  "Unable to determine the UID for user (" + user + ")!");
      }
    }
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether the underlying subprocess has been started.
   */
  public boolean
  hasStarted() 
  {
    return pHasStarted.get();
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

  /**
   * Get the full execution details. 
   */ 
  public SubProcessExecDetails
  getExecDetails()
  {
    return pExecDetails;
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
      if(dpid <= 0)
	throw new IllegalStateException(); 
      boolean wasSignalled = false;
      
      if(pSubstituteUser != null) {
	switch(PackageInfo.sOsType) {
	case Unix: 
	case MacOS:
	  {
	    ArrayList<String> args = new ArrayList<String>();
	    args.add("-s");
	    args.add(signal.toString());
	    args.add(dpid.toString());

	    try {
              SubProcessLight killProc = 
                new SubProcessLight(pSubstituteUser, "SignalSubProcess", 
                                    "kill", args, env, PackageInfo.sTempPath.toFile());
              killProc.start(); 
	      killProc.join();

              wasSignalled = true;
	    } 
	    catch(InterruptedException ex) {
	      LogMgr.getInstance().log
		(LogMgr.Kind.Sub, LogMgr.Level.Severe,
		 "Interrupted while trying to send signal (" + signal + ") " + 
		 "to process [" + dpid + "] using plrun(1):\n" +
		 "  " + ex.getMessage());
	    }
            catch(PipelineException ex) {
              LogMgr.getInstance().log
                (LogMgr.Kind.Sub, LogMgr.Level.Warning,
                 "Unable to send signal (" + signal + ") to process [" + dpid + "]: " +
                 ex.getMessage());
            }
	  }
	  break;

	case Windows:
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
   * Scan the /proc filesystem to determine the process IDs of all processes 
   * created by the process with the given ID. <P> 
   * 
   * Due to a lack support for a /proc filesystem on Mac OS X, this method will always return
   * a list containing only the single process ID passed as its argument.
   */ 
  private TreeSet<Integer>
  buildHitList
  (
   int pid
  ) 
  {
    TreeSet<Integer> dead = new TreeSet<Integer>();

    switch(PackageInfo.sOsType) {
    case Unix: 
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
	      StringBuilder buf = new StringBuilder();
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
	buildHitListHelper(pid, PIDs, dead);
      }
      break;

    case Windows:
    case MacOS:
      dead.add(pid);
      break;
    }

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

  

  /*----------------------------------------------------------------------------------------*/
  /*   D E B U G G I N G                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Generate a message detailing the exact command, working directory and environment which
   * would be used if the subprocess was executed. 
   */ 
  public String
  getDryRunInfo() 
  {
    return getDryRunInfo(true, true);
  }

  /**
   * Generate a message detailing the exact command, working directory and environment which
   * would be used if the subprocess was executed. 
   * 
   * @param showWorkingDir
   *   Whether to add working directory information to the returned message.
   * 
   * @param showEnvironment
   *   Whether to add full environmental variable information to the returned message.
   */ 
  public String
  getDryRunInfo
  (
   boolean showWorkingDir, 
   boolean showEnvironment
  ) 
  {
    StringBuilder buf = new StringBuilder();
    
    buf.append(getName() + " [command]: \"" + getCommand() + "\""); 

    if(showWorkingDir) 
      buf.append("\n" + getName() + " [working directory]: " + getWorkingDir()); 
    
    if(showEnvironment) {
      buf.append("\n" + getName() + " [environment]:\n");

      String[] env = getEnvironment();
      int wk;
      for(wk=0; wk<env.length; wk++) 
        buf.append("\n  " + env[wk]);
    }

    return buf.toString();
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
   * Whether the underlying subprocess has been started.
   */
  protected AtomicBoolean  pHasStarted; 

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
  protected String  pSubstituteUser;   

  /**
   * The full execution details. 
   */ 
  private SubProcessExecDetails pExecDetails; 


}



