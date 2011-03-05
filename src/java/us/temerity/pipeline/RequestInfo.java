// $Id: PrivilegedReq.java,v 1.1 2006/01/15 06:29:25 jim Exp $

package us.temerity.pipeline;

import java.io.*;
import java.util.*;
import java.net.*;

/*------------------------------------------------------------------------------------------*/
/*   R E Q U E S T   I N F O                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Information about the caller of a network client method.
 */
public
class RequestInfo
  implements Cloneable, Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   */
  public
  RequestInfo() 
  {
    pRequestor = PackageInfo.sUser; 

    Thread thread = Thread.currentThread();
    pThreadName = thread.getName();
    pThreadID   = thread.getId();

    StackTraceElement stack[] = thread.getStackTrace();
    if((stack != null) && (stack.length > 5))
      pStackTrace = Arrays.copyOfRange(stack, 5, stack.length);

    try {
      InetAddress addr = InetAddress.getLocalHost();
      if((addr instanceof Inet4Address) && !addr.isLoopbackAddress()) 
        pHostName = addr.getCanonicalHostName().toLowerCase(Locale.ENGLISH);
    }
    catch(Exception ex) {
      pHostName = null; 
    }
  }

  /** 
   * Copy constructor. 
   */
  public
  RequestInfo
  (
   RequestInfo info
  ) 
  {
    pRequestor  = info.getRequestor();
    pThreadName = info.getThreadName();
    pThreadID   = info.getThreadID();
    pStackTrace = info.getStackTrace();
    pHostName   = info.getHostName();
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the name of user making the request.
   */ 
  public String
  getRequestor() 
  {
    return pRequestor; 
  }

  /**
   * Get the requestor thread name.
   */
  public String
  getThreadName() 
  {
    return pThreadName;
  }

  /**
   * Gets the requestor thread ID.
   */
  public long
  getThreadID() 
  {
    return pThreadID;
  }

  /**
   * Gets the stack trace of the requestor.
   */ 
  public StackTraceElement[]
  getStackTrace() 
  {
    return Arrays.copyOf(pStackTrace, pStackTrace.length);
  }

  /**
   * Get the name of the host where the request originated.
   */
  public String
  getHostName() 
  {
    return pHostName; 
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C L O N E A B L E                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Return a deep copy of this object.
   */
  public Object 
  clone()
  {
    return new RequestInfo(this); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L O G G I N G                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Generate a log message string describing the details of the request call.
   */ 
  public String
  logMessage
  (
   LogMgr log, 
   LogMgr.Kind kind
  ) 
  {
    StringBuilder buf = new StringBuilder();

    if(log.isLoggable(kind, LogMgr.Level.Finest)) {
      buf.append
        ("\n  by User (" + pRequestor + ") " + 
         "in Thread (" + pThreadName + ":" + pThreadID + ") " + 
         "from Host (" + pHostName + ")");
    }
    
    if(log.isLoggable(kind, LogMgr.Level.Finest)) {
      if((pStackTrace != null) && (pStackTrace.length > 0)) {
        if(log.isLoggable(kind, LogMgr.Level.Detail)) {
          for(StackTraceElement elem : pStackTrace) 
            buf.append("\n  at " + elem); 
        }
        else {
          buf.append("\n  at " + pStackTrace[0]); 
        }
      }
      else {
        buf.append("\n  at Unknown");
      }
    }

    return buf.toString();
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1563231534847081820L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of user making the request.
   */
  private String  pRequestor; 

  /**
   * The requestor thread name.
   */
  private String  pThreadName;

  /**
   * The requestor thread ID.
   */
  private long  pThreadID;

  /**
   * The stack trace of the requestor.
   */
  private StackTraceElement[]  pStackTrace; 

  /**
   * The name of the host where the request originated.
   */
  private String  pHostName;

}
  
