// $Id: Exceptions.java,v 1.1 2008/02/14 20:26:29 jim Exp $

package us.temerity.pipeline;

/*------------------------------------------------------------------------------------------*/
/*   E X C E P T I O N S                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Some static helper methods useful for exception handling. 
 */
public
class Exceptions
{
  /** 
   * Generate a string containing a detailed message explaining the the cause of an 
   * exception.<P> 
   * 
   * @param header  
   *   Some introductory text explaining the high-level reason for the exception or 
   *   <CODE>null<CODE> to omit the header.
   * 
   * @param cause  
   *   The exception being explained.  A <CODE>null</CODE> value is permitted and 
   *   indicates that the cause is nonexistent or unknown.
   * 
   * @param appendCause
   *   Whether to append the short exception message text to the returned message.
   *   This text will be generated by the expression (<CODE>cause==null ? null : 
   *   cause.toString()</CODE>), which typically contains the class and a brief explanation
   *   of the cause.
   * 
   * @param appendStack
   *   Whether to append the complete stack trace of when the exception occured to the 
   *   returned message.
   * 
   * @param return 
   *   The full message text. 
   */ 
  public static String 
  getFullMessage
  (
   String header, 
   Throwable cause,  
   boolean appendCause, 
   boolean appendStack
  ) 
  {
    StringBuilder buf = new StringBuilder();
     
    if(header != null) 
      buf.append(header + "\n\n"); 

    if(cause != null) {
      if(appendCause) {
        if(cause.getMessage() != null) 
          buf.append(cause.getMessage() + "\n\n"); 	
        else if(cause.toString() != null) 
          buf.append(cause.toString() + "\n\n"); 
      }
     
      if(appendStack) {
        buf.append("Stack Trace:\n");
        StackTraceElement stack[] = cause.getStackTrace();
        int wk;
        for(wk=0; wk<stack.length; wk++) 
          buf.append("  " + stack[wk].toString() + "\n");
      }
    }	
      
    return (buf.toString());
  }

  /** 
   * Generate a string containing a detailed message explaining the the cause of an 
   * exception including the full stack trace.<P>
   * 
   * Equivalent to calling <CODE>getFullMessage(header, cause, true, true)</CODE>.
   * 
   * @param header  
   *   Some introductory text explaining the high-level reason for the exception or 
   *   <CODE>null<CODE> to omit the header.
   * 
   * @param cause  
   *   The exception being explained.  A <CODE>null</CODE> value is permitted and 
   *   indicates that the cause is nonexistent or unknown.
   * 
   * @param return 
   *   The full message text. 
   */ 
  public static String 
  getFullMessage
  (
   String header, 
   Throwable cause
  ) 
  {
    return getFullMessage(header, cause, true, true);
  }
  
  /** 
   * Generate a string containing a detailed message explaining the the cause of an 
   * exception including the full stack trace.<P>
   * 
   * Equivalent to calling <CODE>getFullMessage(null, cause, true, true)</CODE>.
   * 
   * @param cause  
   *   The exception being explained.  A <CODE>null</CODE> value is permitted and 
   *   indicates that the cause is nonexistent or unknown.
   * 
   * @param return 
   *   The full message text. 
   */ 
  public static String 
  getFullMessage
  (
   Throwable cause
  ) 
  {
    return getFullMessage(null, cause, true, true);
  }
  
}
  
