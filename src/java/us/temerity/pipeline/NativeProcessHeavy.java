// $Id: NativeProcessHeavy.java,v 1.2 2004/11/04 01:21:06 jim Exp $

package us.temerity.pipeline;

import java.io.*; 
import java.util.concurrent.atomic.*;

/*------------------------------------------------------------------------------------------*/
/*   N A T I V E   P R O C E S S   H E A V Y                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A set of low-level JNI based methods for creating, montoring and killing an OS level 
 * subprocess which potentially may generate large amounts of output to STDOUT/STDERR. <P> 
 * 
 * The STDOUT/STDERR of the OS level process are redirected to disk files when the process
 * is started bypassing the Java runtime.  This provides the maximum possible IO throughput
 * for long running and IO intensive processes.  The 
 * {@link NativeProcessLight NativeProcessLight} class is a better fit for short lived 
 * processes where low latency access to minimal amounts of output is desired. <P> 
 * 
 * System resource usage information can also collected for the subprocess using the 
 * {@link #collectStats collectStats} method. <P> 
 * 
 * Normally, the {@link SubProcessHeavy SubProcessHeavy} class is used instead of this class 
 * to interact with OS level processes.  The <CODE>SubProces</CODE> class manages several 
 * threads to collect output and in general provides a higher level interface to OS level 
 * processes.
 */
public
class NativeProcessHeavy
  extends Native
  implements NativeProcess
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct an OS level process with the given command line arguments.  The process is 
   * not actually started until the {@link #exec exec} method is called.
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
   * @param outFile 
   *   The file to which all STDOUT output is redirected.
   * 
   * @param errFile 
   *   The file to which all STDERR output is redirected.
   */ 
  public 
  NativeProcessHeavy
  (
   String[] cmd,       
   String[] env,      
   File dir, 
   File outFile, 
   File errFile          
  )
  {
    loadLibrary();

    if(cmd == null)
      throw new IllegalArgumentException("The command line arguments cannot be (null)!");
    if(cmd.length == 0) 
      throw new IllegalArgumentException
	("The command line arguments must contain at least the name of the program to run!");
    pCmd = cmd;

    if(env == null)
      throw new IllegalArgumentException("The environment cannot be (null)!");
    pEnv = env;
    
    if(dir == null)
      throw new IllegalArgumentException("The working directory cannot (null)!");
    pWorkDir = dir;

    pIsRunning = new AtomicBoolean(false);

    pID = new AtomicInteger(-1);

    pStdInFileDesc = -1;

    pStdOutFile = outFile;
    pStdErrFile = errFile; 

    pProcStatsLock = new Object();    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Is the OS level process currently running? 
   */ 
  public boolean
  isRunning() 
  {
    return pIsRunning.get();
  }
 
  /** 
   * Sets whether the OS level process is currently running.
   */ 
  private void 
  setIsRunning  
  (
   boolean tf
  )    
  {
    pIsRunning.set(tf);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

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
    return pCmd;
  }

  /** 
   * Gets the full command and arguments as a single <CODE>String</CODE>.
   */
  public String
  getCommand() 
  {
    StringBuffer buf = new StringBuffer();
    int wk;
    for(wk=0; wk<pCmd.length-1; wk++) 
      buf.append(pCmd[wk] + " ");
    buf.append(pCmd[wk]);

    return buf.toString();
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
    return pEnv;
  }

  /** 
   * Gets the working directory. 
   */ 
  public File 
  getWorkingDir() 
  {
    return pWorkDir;
  }


  /** 
   * Gets the ID assigned by the operating system to the process.
   * 
   * @return
   *   The OS process ID or <CODE>-1</CODE> if the process has not yet been started.
   */ 
  public int
  getPid() 
  {
    return pID.get();
  }
  
  /** 
   * Sets the ID assigned by the operating system to the process.
   */ 
  private void  
  setPid
  (
   int id
  ) 
  {
    pID.set(id);
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Gets the number of seconds the OS level process ran in user space. 
   */
  public double
  getUserSecs() 
  {
    if(pIsRunning.get()) 
      throw new IllegalStateException("The process is still running!");

    return ((double) pUserSecs) + (((double) pUserMSecs) / 1000000.0);
  }

  /**
   * Gets the number of seconds the OS level process ran in system (kernel) space. 
   */
  public double
  getSystemSecs() 
  {
    if(pIsRunning.get()) 
      throw new IllegalStateException("The process is still running!");

    return ((double) pSystemSecs) + (((double) pSystemMSecs) / 1000000.0);
  }

  /**
   * Gets the number of hard page faults during execution of the OS level process. 
   * A hard page fault is a memory fault that required I/O operations.
   */
  public long
  getPageFaults() 
  {
    if(pIsRunning.get()) 
      throw new IllegalStateException("The process is still running!");

    return pPageFaults;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   J O B   C O N T R O L                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Fork and execute a OS level process and wait for it to exit.
   * 
   * @return
   *   The OS exit code of the process. 
   * 
   * @throws IOException 
   *   If unable to execute the OS level process.
   */ 
  public synchronized int 
  exec() 
    throws IOException
  {
    return execNativeHeavy(pCmd, pEnv, pWorkDir.toString(), 
			   pStdOutFile.toString(), pStdErrFile.toString());
  }

  /**
   * Send a POSIX signal to the OS level process. 
   * 
   * @param signal  
   *   The signal to send.
   * 
   * @param pid  
   *   The OS process ID of the subprocess to signal.
   * 
   * @throws IOException 
   *   If unable to sent the signal to the OS level process. 
   */ 
  public void
  signal
  (
   PosixSignal signal,
   int pid
  )
    throws IOException
  {
    signalNative(signal.getCode(), pid);
  }

  /**
   * Kills the OS level process. 
   * 
   * @param pid  
   *   The OS process ID of the subprocess to signal.
   * 
   * @throws IOException 
   *   If unable to sent the {@link PosixSignal#SIGKILL SIGKILL} signal to the OS 
   *   level process. 
   */ 
  public void
  kill
  (
   int pid
  )
    throws IOException
  {
    signal(PosixSignal.SIGKILL, pid);
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
   * Collect resource usage statistics for running OS level process at regular intervals.
   * Does not return until the OS level process has exited.
   * 
   * @param millis 
   *   The number of milliseconds between samples.
   *
   * @throws IOException 
   *   If unable to collect statistics for the OS level process. 
   */
  public void 
  collectStats
  (
   long millis  
  ) 
    throws IOException    
  {
    synchronized(pProcStatsLock) {
      pAvgVMem    = 0;
      pMaxVMem    = 0;
      pAvgResMem  = 0;
      pMaxResMem  = 0;
      pMemSamples = 0;
    }

    try {
      while(true) {
	synchronized(pProcStatsLock) {
	  if(!collectStatsNative()) 
	    break;
	} 
	Thread.sleep(millis);
      }
    }
    catch(InterruptedException ex) {
    }
  }


  /**
   * Gets the average virtual memory size of the OS level process in kilobytes.
   */
  public long
  getAverageVirtualSize() 
  {
    synchronized(pProcStatsLock) {
      if(pMemSamples > 0) 
	return (pAvgVMem / pMemSamples);
    } 
    return 0;
  }

  /**
   * Gets the maximum virtual memory size of the OS level process in kilobytes.
   */
  public long
  getMaxVirtualSize() 
  {
    synchronized(pProcStatsLock) {
      return pMaxVMem;
    }
  }


  /**
   * Gets the average resident memory size of the OS level process in kilobytes.
   */
  public long
  getAverageResidentSize() 
  {
    synchronized(pProcStatsLock) {
      if(pMemSamples > 0) 
	return (pAvgResMem / pMemSamples);
    }
    return 0;
  }

  /**
   * Gets the maximum resident memory size of the OS level process in kilobytes.
   */
  public long
  getMaxResidentSize() 
  {
    synchronized(pProcStatsLock) {
      return pMaxResMem;
    }
  }


 
  /*----------------------------------------------------------------------------------------*/
  /*   N A T I V E    H E L P E R S                                                         */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Send a POSIX signal to the OS level process. 
   * 
   * @param signal  
   *   The signal to send.
   * 
   * @param pid  
   *   The OS process ID of the subprocess to signal.
   * 
   * @throws IOException 
   *   If unable to sent the signal to the OS level process. 
   */ 
  private native void
  signalNative
  (
   int signal,
   int pid
  )
    throws IOException;

  /** 
   * Fork and execute a OS level process and wait for it to exit.
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
   * @param outFile 
   *   The file to which all STDOUT output is redirected.
   * 
   * @param errFile 
   *   The file to which all STDERR output is redirected.
   * 
   * @return
   *   The OS exit code of the process. 
   * 
   * @throws IOException 
   *   If unable to execute the OS level process.
   */ 
  private native int
  execNativeHeavy
  (
   String[] cmd, 
   String[] env, 
   String dir, 
   String outFile, 
   String errFile 
  )
    throws IOException;


  /**
   * Collect resource usage statistics for running OS level process.
   * 
   * @return 
   *   Whether statistics where successfully collected.
   * 
   * @throws IOException 
   *   If unable to collect statistics for the OS level process. 
   */
  private native boolean 
  collectStatsNative()
    throws IOException;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**   
   * The command line arguments used to launch the OS level process.  The first element
   * of the array <CODE>cmd[0]</CODE> is the name of the program to run.
   */
  private String[]  pCmd;        

  /**   
   * The environment under which the OS level process is run.  Consist of environmental 
   * variable name/value pairs of the form <CODE>"name=value"</CODE>.
   */
  private String[]  pEnv;       
  
  /**   
   * The working directory of the OS level process.
   */
  private File  pWorkDir;    


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the OS level process currently running? 
   */ 
  private AtomicBoolean  pIsRunning;  

  /**
   * The native OS process ID 
   */   
  private AtomicInteger  pID;        
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * The native OS file descriptor for the STDIN pipe. 
   */ 
  private int  pStdInFileDesc;  

  /**
   * The file to which all STDOUT output is redirected.
   */  
  private File pStdOutFile; 

  /**
   * The file to which all STDERR output is redirected.
   */ 
  private File pStdErrFile; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The number of seconds the OS level process was running in user space. 
   */
  private long  pUserSecs;  

  /**
   * The number of microseconds the OS level process was running in user space. 
   */      
  private long  pUserMSecs;       

  /**
   * The number of seconds the OS level process was running in system (kernel) space. 
   */
  private long  pSystemSecs;      

  /**
   * The number of microseconds the OS level process was running in system (kernel) space. 
   */
  private long  pSystemMSecs;     

  /**
   * The number of hard page faults during execution of the OS level process. <P> 
   * A hard page fault is a memory fault that required I/O operations.
   */
  private long  pPageFaults;      


  /*----------------------------------------------------------------------------------------*/

  /**
   * A synchronization lock for process statistics.
   */
  private Object pProcStatsLock;       

  /**
   * The average virtual memory size of the OS level process in kilobytes.
   */
  private long pAvgVMem;  

  /**
   * The maximum virtual memory size of the OS level process in kilobytes.
   */       
  private long pMaxVMem;         

  /**
   * The average resident memory size of the OS level process in kilobytes.
   */
  private long pAvgResMem;   

  /**
   * The maximum resident memory size of the OS level process in kilobytes.
   */    
  private long pMaxResMem;       

  /**
   * The number of hard page faults during execution of the OS level process. <P> 
   * A hard page fault is a memory fault that required I/O operations.
   */
  private long pMemSamples;      

}
