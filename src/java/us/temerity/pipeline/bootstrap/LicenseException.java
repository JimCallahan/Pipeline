// $Id: LicenseException.java,v 1.1 2004/03/22 03:12:45 jim Exp $

package us.temerity.pipeline.bootstrap;

/*------------------------------------------------------------------------------------------*/
/*   L I C E N S E   E X C E P T I O N                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Indicated a failure to validate the license for Pipeline.
 */
class LicenseException
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
  LicenseException()
  { 
    super(); 
  }

  /** 
   * Constructs a new exception with the specified detail message. 
   * The cause is not initialized, and may subsequently be initialized by a call to 
   * {@link #initCause(Throwable) initCause}.
   * 
   * @param message  [<B>in</B>]
   *   The detail message. The detail message is saved for later retrieval by the 
   *   {@link #getMessage() getMessage} method.
   */
  public
  LicenseException
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
   * @param message  [<B>in</B>]
   *   The detail message. The detail message is saved for later retrieval by the 
   *   {@link #getMessage() getMessage} method. 
   * 
   * @param cause  [<B>in</B>]
   *   The cause (which is saved for later retrieval by the {@link #getCause() getCause} 
   *   method). (A <CODE>null</CODE> value is permitted, and indicates that the cause is 
   *   nonexistent or unknown.)
   */
  public
  LicenseException
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
   * @param cause  [<B>in</B>]
   *   The cause (which is saved for later retrieval by the {@link #getCause() getCause} 
   *   method). (A <CODE>null</CODE> value is permitted, and indicates that the cause is 
   *   nonexistent or unknown.)
   */
  public
  LicenseException
  (
   Throwable cause
  ) 
  {
    super(cause);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6681762514416355573L;

}
  
