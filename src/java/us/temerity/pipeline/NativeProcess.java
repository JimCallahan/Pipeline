// $Id: NativeProcess.java,v 1.9 2004/10/28 15:55:23 jim Exp $

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
  
  /**
   * Gets the number of seconds the OS level process ran in user space. 
   */
  public double
  getUserSecs();

  /**
   * Gets the number of seconds the OS level process ran in system (kernel) space. 
   */
  public double
  getSystemSecs();

  /**
   * Gets the number of hard page faults during execution of the OS level process. 
   * A hard page fault is a memory fault that required I/O operations.
   */
  public long
  getPageFaults();



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
