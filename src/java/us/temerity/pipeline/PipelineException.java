// $Id: PipelineException.java,v 1.4 2008/02/14 00:57:35 jesse Exp $

package us.temerity.pipeline;

/*------------------------------------------------------------------------------------------*/
/*   P I P E L I N E   E X C E P T I O N                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * High level Pipeline error events which should be reported to the user. <P> 
 * 
 * These exceptions may be generated for a variety of non-fatal failure conditions, but 
 * should always contain a message designed to be easily understandable by the user.  
 * In other words, they shouldn't require the user to understand any of the internals of 
 * Pipeline to interpret the message.
 */
public
class PipelineException
  extends Exception 
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new exception with <CODE>null</CODE> as its detail message. 
   * The cause is not initialized, and may subsequently be initialized by a call to 
   * {@link #initCause(Throwable) initCause}.
   */
  public
  PipelineException()
  { 
    super(); 
  }

  /** 
   * Constructs a new exception with the specified detail message. 
   * The cause is not initialized, and may subsequently be initialized by a call to 
   * {@link #initCause(Throwable) initCause}.
   * 
   * @param message  
   *   The detail message. The detail message is saved for later retrieval by the 
   *   {@link #getMessage() getMessage} method.
   */
  public
  PipelineException
  (
   String message
  ) 
  { 
    super(message); 
  }

  /** 
   * Constructs a new exception with the specified detail message and cause. <P> 
   * 
   * Note that the detail message associated with <CODE>cause</CODE> is <I>not</I> 
   * automatically incorporated in this exception's detail message.
   * 
   * @param message  
   *   The detail message. The detail message is saved for later retrieval by the 
   *   {@link #getMessage() getMessage} method. 
   * 
   * @param cause  
   *   The cause (which is saved for later retrieval by the {@link #getCause() getCause} 
   *   method). (A <CODE>null</CODE> value is permitted, and indicates that the cause is 
   *   nonexistent or unknown.)
   */
  public
  PipelineException
  (
   String message, 
   Throwable cause
  ) 
  { 
    super(message, cause);
  }

  /** 
   * Constructs a new exception with the specified cause and a detail message of 
   * (<CODE>cause==null ? null : cause.toString()</CODE>) which typically contains the 
   * class and detail message of cause. This constructor is useful for exceptions that 
   * are little more than wrappers for other throwables.
   * 
   * @param cause  
   *   The cause (which is saved for later retrieval by the {@link #getCause() getCause} 
   *   method). (A <CODE>null</CODE> value is permitted, and indicates that the cause is 
   *   nonexistent or unknown.)
   */
  public
  PipelineException
  (
   Throwable cause
  ) 
  {
    super(cause);
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   M E T H O D S                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new exception from the specified cause that builds a detailed error message, 
   * including the stack trace.
   * 
   * @param cause  
   *   The cause (which is saved for later retrieval by the {@link #getCause() getCause} 
   *   method). 
   *   
   *  @return
   *    A Pipeline Exception that contains a detailed message about why the cause was thrown
   *    including a stack trace.
   */
  public static PipelineException
  getDetailedException
  (
    Throwable cause
  )
  {
    StringBuilder buf = new StringBuilder();
    
    if(cause.getMessage() != null) 
      buf.append(cause.getMessage() + "\n\n");     
    else if(cause.toString() != null) 
      buf.append(cause.toString() + "\n\n");       
      
    buf.append("Stack Trace:\n");
    StackTraceElement stack[] = cause.getStackTrace();
    int wk;
    for(wk=0; wk<stack.length; wk++) 
      buf.append("  " + stack[wk].toString() + "\n");
    
    return new PipelineException(buf.toString(), cause);
  }

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3962218832440351208L;

}
  
