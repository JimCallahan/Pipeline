// $Id: NativeProcessLight.java,v 1.9 2007/03/18 02:17:16 jim Exp $

package us.temerity.pipeline;

import java.io.*; 
import java.math.*;
import java.nio.*; 
import java.security.*;
import java.security.spec.*;
import java.security.interfaces.*;
import java.util.concurrent.atomic.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.crypto.interfaces.*;


/*------------------------------------------------------------------------------------------*/
/*   N A T I V E   P R O C E S S   L I G H T                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A set of low-level JNI based methods for creating, montoring and killing an OS level 
 * subprocess which generates a minimal amount of output to STDOUT/STDERR. <P> 
 * 
 * Large amounts of OS level process output are not efficiently handled by this class
 * and will result in high rates of garbage generation and low overall performance.  The 
 * {@link NativeProcessHeavy NativeProcessHeavy} class should be used for thse kinds of 
 * processes. <P> 
 * 
 * Normally, the {@link SubProcessLight SubProcessLight} class is used instead of this class 
 * to interact with OS level processes.  The <CODE>SubProces</CODE> class manages several 
 * threads to collect output and in general provides a higher level interface to OS level 
 * processes.
 */
public
class NativeProcessLight
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
   * @throws PipelineException
   *   If unable to initialize the OS level process.
   */ 
  public 
  NativeProcessLight
  (
   String[] cmd,       
   String[] env,      
   File dir          
  )
    throws PipelineException
  {
    loadLibrary();

    if(cmd == null)
      throw new PipelineException("The command line arguments cannot be (null)!");
    if(cmd.length == 0) 
      throw new PipelineException
	("The command line arguments must contain at least the name of the program to run!");
    pCmd = cmd;

    if(env == null)
      throw new PipelineException("The environment cannot be (null)!");
    pEnv = env;
    
    if(dir == null)
      throw new PipelineException("The working directory cannot (null)!");
    pWorkDir = dir;

    pIsRunning = new AtomicBoolean(false);

    pID = new AtomicInteger(-1);

    pStdInFileDesc  = -1;
    pStdOutFileDesc = -1; 
    pStdErrFileDesc = -1;
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
    StringBuilder buf = new StringBuilder();
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
    if(pIsRunning.get()) 
      return null;
    return (((double) pUTime) / 100.0);
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
    if(pIsRunning.get()) 
      return null;
    return (((double) pSTime) / 100.0);
  }

  /**
   * Get the number of major faults which occured for the process which have required 
   * loading a memory page from disk.
   * 
   * @return 
   *   The number of faults or <CODE>null</CODE> if unknown.
   */
  public Long
  getPageFaults() 
  { 
    if(pIsRunning.get()) 
      return null;
    return pPageFaults;
  }

  /**
   * Get the maximum virtual memory size of the process.
   * 
   * @return 
   *   The size in bytes or <CODE>null</CODE> if unknown.
   */
  public Long
  getVirtualSize() 
  {
    if(pIsRunning.get()) 
      return null;
    return pVirtualSize;
  }
  
  /**
   * Get the maximum resident memory size of the process (in bytes).
   * 
   * @return 
   *   The size in bytes or <CODE>null</CODE> if unknown.
   */
  public Long
  getResidentSize() 
  {
    if(pIsRunning.get()) 
      return null;
    return pResidentSize;
  }

  /**
   * Get the cumilative amount of memory swapped by the process (in bytes).
   * 
   * @return 
   *   The size in bytes or <CODE>null</CODE> if unknown.
   */
  public Long
  getSwappedSize()  
  {
    if(pIsRunning.get()) 
      return null;
    return pSwappedSize;
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
    return execNativeLight(pCmd, pEnv, pWorkDir.toString());
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
   * Read up to the given number of characters from the STDOUT of the OS level process. 
   * 
   * @param size 
   *   The maximum number of characters to read.
   * 
   * @return 
   *   The available output on STDOUT of the OS level process or <CODE>null</CODE> on EOF.  
   *   Note that the size of the <CODE>String</CODE> read may be smaller than the
   *   <CODE>size</CODE> argument. */ 
  public native String
  readFromStdOut
  (
   int size  
  ) 
    throws IOException;   

  /** 
   * Close the STDOUT pipe. 
   */ 
  public native void  
  closeStdOut() 
    throws IOException;
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Read up to the given number of characters from the STDERR of the OS level process. 
   * 
   * @param size 
   *   The maximum number of characters to read.
   * 
   * @return 
   *   The available output on STDERR of the OS level process or <CODE>null</CODE> on EOF.  
   *   Note that the size of the <CODE>String</CODE> read may be smaller than the
   *   <CODE>size</CODE> argument. */ 
  public native String
  readFromStdErr
  (
   int size  
  ) 
    throws IOException;

  /**
   * Close the STDERR pipe. 
   */ 
  public native void  
  closeStdErr() 
    throws IOException;


 
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
   * @return
   *   The OS exit code of the process. 
   * 
   * @throws IOException 
   *   If unable to execute the OS level process.
   */ 
  private native int
  execNativeLight
  (
   String[] cmd, 
   String[] env, 
   String dir    
  )
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
   * The native OS file descriptor for the STDOUT pipe. 
   */  
  private int  pStdOutFileDesc; 

  /**
   * The native OS file descriptor for the STDERR pipe. 
   */ 
  private int  pStdErrFileDesc; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The number of jiffies (1/100th of a second) the process has been scheduled in 
   * user mode.
   */
  private long  pUTime;

  /**
   * The number of jiffies (1/100th of a second) the process has been scheduled in 
   * kernel mode.
   */
  private long  pSTime;
  
  /**
   * The number of major faults the process has made which have required loading a 
   * memory page from disk.
   */
  private long  pPageFaults;      

  /**
   * The virtual memory size of the process in bytes.
   */ 
  private long  pVirtualSize;

  /**
   * The resident memory set size of the process in bytes.
   */ 
  private long pResidentSize;

  /**
   * The cumilative amount of memory swapped by the process in bytes.
   */ 
  private long pSwappedSize;
}
