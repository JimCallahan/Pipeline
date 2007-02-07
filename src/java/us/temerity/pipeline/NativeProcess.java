// $Id: NativeProcess.java,v 1.12 2007/02/07 21:07:23 jim Exp $

package us.temerity.pipeline;

import java.io.*; 

/*------------------------------------------------------------------------------------------*/
/*   N A T I V E   P R O C E S S                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A set of low-level JNI based methods for creating, montoring and killing an OS level 
 * subprocess. 
 */
public 
interface NativeProcess
{  
  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Is the OS level process currently running? 
   */ 
  public boolean
  isRunning();
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Provide the encrypted Windows password for the user which will own the OS level 
   * process. <P> 
   * 
   * This is only required on Windows systems where the process will be run by a user
   * other than the current user and must be called before the subprocess is started.
   * 
   * @param user
   *   The username which will own the OS level subprocess.
   * 
   * @param password
   *   The encrypted Windows password for the user.
   */ 
  public void 
  authorizeOnWindows
  (
   String user, 
   String password
  );


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Gets command line arguments of the OS level process.  
   * 
   * @return 
   *   The command line argument array.  The first element of this array contains the 
   *   name of the program.
   */
  public String[]
  getCommandLine();

  /** 
   * Gets the full command and arguments as a single <CODE>String</CODE>.
   */
  public String
  getCommand();


  /**
   * Gets the environment under which the OS level process is run.
   * 
   * @return 
   *   The array of name/value pairs of the form <CODE>"name=value"</CODE>.
   */ 
  public String[]
  getEnvironment();

  /** 
   * Gets the working directory. 
   */ 
  public File 
  getWorkingDir();


  /** 
   * Gets the ID assigned by the operating system to the process.
   * 
   * @return
   *   The OS process ID or <CODE>-1</CODE> if the process has not yet been started.
   */ 
  public int
  getPid();
  


  /*----------------------------------------------------------------------------------------*/
  /*   R U N   S T A T I S T I C S                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the number of seconds the process has been scheduled in user mode.
   * 
   * @return 
   *   The time in seconds or <CODE>null</CODE> if unknown.
   */
  public Double
  getUserTime();

  /**
   * Get the number of seconds the process has been scheduled in kernel mode.
   * 
   * @return 
   *   The time in seconds or <CODE>null</CODE> if unknown.
   */
  public Double
  getSystemTime();

  /**
   * Get the number of major faults which occured for the process which have required 
   * loading a memory page from disk.
   * 
   * @return 
   *   The number of faults or <CODE>null</CODE> if unknown.
   */
  public Long
  getPageFaults();

  /**
   * Get the maximum virtual memory size of the process.
   * 
   * @return 
   *   The size in bytes or <CODE>null</CODE> if unknown.
   */
  public Long
  getVirtualSize();
  
  /**
   * Get the maximum resident memory size of the process (in bytes).
   * 
   * @return 
   *   The size in bytes or <CODE>null</CODE> if unknown.
   */
  public Long
  getResidentSize() ;

  /**
   * Get the cumilative amount of memory swapped by the process (in bytes).
   * 
   * @return 
   *   The size in bytes or <CODE>null</CODE> if unknown.
   */
  public Long
  getSwappedSize();


  /*----------------------------------------------------------------------------------------*/
  /*   J O B   C O N T R O L                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Fork and execute a OS level process and wait for it to exit. <P> 
   * 
   * @return
   *   The OS exit code of the process. 
   * 
   * @throws IOException 
   *   If unable to execute the OS level process.
   */ 
  public int 
  exec() 
    throws IOException;

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
    throws IOException;

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
    throws IOException;
}
