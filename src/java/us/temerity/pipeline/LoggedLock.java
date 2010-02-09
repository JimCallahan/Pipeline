// $Id: NodeID.java,v 1.14 2008/06/15 01:59:49 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.util.concurrent.locks.*; 

/*------------------------------------------------------------------------------------------*/
/*   L O G G E D   L O C K                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Add automatic @{link LogMgr} based logging support to common locking operations to 
 * {@link ReentrantReadWriteLock} instances.
 */
public
class LoggedLock
  extends ReentrantReadWriteLock
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new lock.
   * 
   * @param title
   *   The title to give this particular lock in log messages.
   */
  public
  LoggedLock
  (
   String title
  ) 
  {
    if(title == null)
      throw new IllegalArgumentException("The lock title cannot be (null)!");         
    pTitle = title;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   L O C K I N G                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Acquire the read-lock.<P> 
   * 
   * Adds logging to the call of <CODE>readLock().lock()</CODE>.
   */ 
  public void 
  acquireReadLock() 
  { 
    logPreLock(true); 
    readLock().lock();
    logPostLock(true, true); 
  }
  
  /**
   * Release the read-lock.<P> 
   * 
   * Adds logging to the call of <CODE>readLock().unlock()</CODE>.
   */ 
  public void 
  releaseReadLock() 
  {
    readLock().unlock();
    logPostLock(true, false); 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Acquire the write-lock.<P> 
   * 
   * Adds logging to the call of <CODE>writeLock().lock()</CODE>.
   */ 
  public void 
  acquireWriteLock() 
  { 
    logPreLock(false); 
    writeLock().lock();
    logPostLock(false, true); 
  }
  
  /**
   * Release the write-lock.<P> 
   * 
   * Adds logging to the call of <CODE>writeLock().unlock()</CODE>.
   */ 
  public void 
  releaseWriteLock() 
  {
    writeLock().unlock();
    logPostLock(false, false); 
  }

  


  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Log the attempt to acquire a lock.
   * 
   * @param isReadLock
   *   Set to <CODE>true</CODE> for a read-lock and <CODE>false</CODE> for a write-lock.
   */ 
  private void
  logPreLock
  (
   boolean isReadLock
  ) 
  {
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Lck, LogMgr.Level.Finer)) {
      Thread thread = Thread.currentThread(); 
      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Lck, LogMgr.Level.Finer, 
         "Acquiring " + pTitle + " " + (isReadLock ? "Read" : "Write") + "-Lock in Thread " + 
         thread.getName() + "[" + thread.getId() + "] " + getCallInfo(thread)); 
    }
  }
   
  /**
   * Log the successful acquisition or release of a lock.
   * 
   * @param isReadLock
   *   Set to <CODE>true</CODE> for a read-lock and <CODE>false</CODE> for a write-lock.
   * 
   * @param isAcquired
   *   Set to <CODE>true</CODE> for acquisition and <CODE>false</CODE> for release.
   */ 
  private void
  logPostLock
  (
   boolean isReadLock, 
   boolean isAcquired
  ) 
  {
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Lck, LogMgr.Level.Fine)) {
      Thread thread = Thread.currentThread(); 
      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Lck, LogMgr.Level.Fine, 
         pTitle + " " + (isReadLock ? "Read" : "Write") + "-Lock " + 
         (isAcquired ? "Acquired" : "Released") + " in Thread " + 
         thread.getName() + "[" + thread.getId() + "] " + getCallInfo(thread));
    }
  }

  /**
   * Get a string containing information about the locking method call site.
   */ 
  private String
  getCallInfo
  (
   Thread thread
  ) 
  {
    String callInfo = "";
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Lck, LogMgr.Level.Finest)) {
      StackTraceElement stack[] = thread.getStackTrace(); 
      if((stack != null) && (stack.length > 4)) {
        if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Lck, LogMgr.Level.Detail)) {
          StringBuilder buf = new StringBuilder();
          
          int wk;
          for(wk=4; wk<stack.length; wk++) 
            buf.append("\n  at " + stack[wk].toString());
          
          callInfo = buf.toString();
        }
        else {
          callInfo = ("\n  at " + stack[4].toString());
        }
      }
      else {
        callInfo = "\n  at Unknown";
      }
    }

    return callInfo;
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 1622287682186312526L;
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The title to give this particular lock in log messages.
   */ 
  private String pTitle; 

}
