// $Id: PosixSignal.java,v 1.1 2004/03/22 03:12:34 jim Exp $

package us.temerity.pipeline.core;

/*------------------------------------------------------------------------------------------*/
/*   P O S I X   S I G N A L                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The signals described in the original POSIX.1 standard.  The valid code values of the 
 * signals for the i386 architecture can be obtained by calling the {@link #getCode getCode}
 * method.  See the <I>signal(7)</I> man page for details.
 * 
 * @see SubProcess
 * @see NativeProcess
 */
public
enum PosixSignal
{  
  /**
   * Hangup detected on controlling terminal or death of controlling process. <P> 
   * Default action is to terminate the process.
   */
  SIGHUP(1), 
  
  /**
   * Interrupt from keyboard. <P> 
   * Default action is to terminate the process.
   */
  SIGINT(2), 

  /**
   * Quit from keyboard. <P> 
   * Default action is to terminate the process and dump core.
   */
  SIGQUIT(3), 
  
  /**
   * Illegal Instruction. <P> 
   * Default action is to terminate the process and dump core.
   */  
  SIGILL(4),  
  
  /**
   * Abort signal from <I>abort(3)</I>. <P> 
   * Default action is to terminate the process and dump core.
   */  
  SIGABRT(6), 
  
  /**
   * Floating point exception. <P> 
   * Default action is to terminate the process and dump core.
   */   
  SIGFPE(8), 
  
  /**
   * Kill signal. <P> 
   * Default action is to terminate the process.
   */   
  SIGKILL(9), 
  
  /**
   * Invalid memory reference. <P> 
   * Default action is to terminate the process and dump core.
   */   
  SIGSEGV(11), 
  
  /**
   * Broken pipe: write to pipe with no readers. <P> 
   * Default action is to terminate the process.
   */   
  SIGPIPE(13), 
  
  /**
   * Timer signal from <I>alarm(2)</I>. <P> 
   * Default action is to terminate the process.
   */   
  SIGALRM(14), 
  
  /**
   * Termination signal. <P> 
   * Default action is to terminate the process.
   */   
  SIGTERM(15), 
  
  /**
   * User-defined signal 1. <P> 
   * Default action is to terminate the process.
   */    
  SIGUSR1(10), 
  
  /**
   * User-defined signal 2. <P> 
   * Default action is to terminate the process.
   */    
  SIGUSR2(12), 
  
  /**
   * Child stopped or terminated. <P> 
   * Default action is to ignore the signal.
   */    
  SIGCHLD(17), 
  
  /**
   * Continue if stopped. <P> 
   * No default action.
   */    
  SIGCONT(18), 
  
  /**
   * Stop the process. <P> 
   * Default action is to stop the process.
   */    
  SIGSTOP(19), 
  
  /**
   * Stop typed at tty. <P> 
   * Default action is to stop the process.
   */    
  SIGTSTP(20), 
  
  /**
   * Pending tty input for background process. <P> 
   * Default action is to stop the process.
   */    
  SIGTTIN(21), 
  
  /**
   * Pending tty output for background process. <P> 
   * Default action is to stop the process.
   */    
  SIGTTOU(22); 

  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  private
  PosixSignal
  (
   int code
  ) 
  { 
    pCode = code; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Gets the code value of this POSIX signal.
   */
  public int 
  getCode() 
  { 
    return pCode; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The code value of the POSIX signal.
   */ 
  private final int  pCode;

}
