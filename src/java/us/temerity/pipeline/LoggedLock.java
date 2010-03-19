// $Id: NodeID.java,v 1.14 2008/06/15 01:59:49 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.util.concurrent.*;
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

    if(title.equals(sReadHolderLockTitle)) {
      pReadLockHolders = new TreeMap<String,StackTraceElement[]>();
    }
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N I T I A L I Z A T I O N                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  static {
    String title = null;
    try {
      title = System.getProperty("pipeline.loglock.held");
    }
    catch(Exception ex) {
    }

    sReadHolderLockTitle = title; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L O C K I N G                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Attempt to acquire the read-lock, with a timeout.<P> 
   * 
   * Adds logging to the call of <CODE>readLock().tryLock()</CODE>.
   * 
   * @param timeout
   *   The number of milliseconds to wait to acquire the lock before giving up.
   * 
   * @return 
   *   Whether the lock was acquired.
   */ 
  public boolean
  tryReadLock
  (
   long timeout
  ) 
  { 
    boolean success = false; 
    try {
      logPreLock(true); 
      if(readLock().tryLock() || 
         readLock().tryLock(timeout, TimeUnit.MILLISECONDS))
        success = true; 
    }
    catch(InterruptedException ex) {
      Thread thread = Thread.currentThread(); 
      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Bak, LogMgr.Level.Warning,
         "Interrupted while attempting to Acquire " + pTitle + " Read-Lock in Thread " + 
         thread.getName() + "[" + thread.getId() + "] " + getCallInfo(thread));
    }

    if(success) { 
      addReadLockHolder();
      logPostLock(true, true); 
    }
    else {
      logLockFail(true);
    }

    return success; 
  }
  
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
    addReadLockHolder();
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
    removeReadLockHolder();
    logPostLock(true, false); 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Attempt to acquire the write-lock, with a timeout.<P> 
   * 
   * Adds logging to the call of <CODE>writeLock().tryLock()</CODE>.
   * 
   * @param timeout
   *   The number of milliseconds to wait to acquire the lock before giving up.
   * 
   * @return 
   *   Whether the lock was acquired.
   */ 
  public boolean
  tryWriteLock
  (
   long timeout
  ) 
  { 
    boolean success = false; 
    try {
      logPreLock(false); 
      if(writeLock().tryLock() || 
         writeLock().tryLock(timeout, TimeUnit.MILLISECONDS))
        success = true; 
    }
    catch(InterruptedException ex) {
      Thread thread = Thread.currentThread(); 
      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Bak, LogMgr.Level.Warning,
         "Interrupted while attempting to Acquire " + pTitle + " Write-Lock in Thread " + 
         thread.getName() + "[" + thread.getId() + "]" + getCallInfo(thread));
    }

    if(success) 
      logPostLock(false, true); 
    else
      logLockFail(false);

    return success; 
  }
  
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
  protected void
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
         thread.getName() + "[" + thread.getId() + "]" + getCallInfo(thread)); 

      if(pReadLockHolders != null) {
        synchronized(pReadLockHolders) {
          for(Map.Entry<String,StackTraceElement[]> entry : pReadLockHolders.entrySet()) {
            LogMgr.getInstance().logAndFlush
              (LogMgr.Kind.Lck, LogMgr.Level.Finer, 
               "Read-Lock Held in Thread " + entry.getKey() + 
               getCallInfo(entry.getValue(), 3)); 
          }
        }
      }
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
  protected void
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
         thread.getName() + "[" + thread.getId() + "]" + getCallInfo(thread));
    }
  }

  /**
   * Log the failure to acquire a lock.
   * 
   * @param isReadLock
   *   Set to <CODE>true</CODE> for a read-lock and <CODE>false</CODE> for a write-lock.
   */ 
  protected void
  logLockFail
  (
   boolean isReadLock
  ) 
  {
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Lck, LogMgr.Level.Fine)) {
      Thread thread = Thread.currentThread(); 
      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Lck, LogMgr.Level.Fine, 
         "Failed to Acquire " + pTitle + " " + (isReadLock ? "Read" : "Write") + "-Lock " + 
         "in Thread " + thread.getName() + "[" + thread.getId() + "]" + getCallInfo(thread));
    }
  }

  /**
   * Get a string containing information about the locking method call site.
   */ 
  protected String
  getCallInfo
  (
   Thread thread
  ) 
  {
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Lck, LogMgr.Level.Finest)) 
      return getCallInfo(thread.getStackTrace(), 4); 
    return "";
  }
  
  /**
   * Get a string containing information about the locking method call site.
   */ 
  protected String
  getCallInfo
  (
   StackTraceElement stack[], 
   int level 
  ) 
  {
    String callInfo = "";
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Lck, LogMgr.Level.Finest)) {
      if((stack != null) && (stack.length > level)) {
        if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Lck, LogMgr.Level.Detail)) {
          StringBuilder buf = new StringBuilder();
          
          int wk;
          for(wk=level; wk<stack.length; wk++) 
            buf.append("\n  at " + stack[wk].toString());
          
          callInfo = buf.toString();
        }
        else {
          callInfo = ("\n  at " + stack[level].toString());
        }
      }
      else {
        callInfo = "\n  at Unknown";
      }
    }

    return callInfo;
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Add the current thread and stack to those currently holding the read-lock.
   */ 
  private void 
  addReadLockHolder()
  {
    if(pReadLockHolders != null) {
      synchronized(pReadLockHolders) { 
        Thread thread = Thread.currentThread(); 
        pReadLockHolders.put(thread.getName() + "[" + thread.getId() + "]", 
                             thread.getStackTrace());
      }
    }
  }

  /**
   * Remove the current thread from those currently holding the read-lock.
   */ 
  private void 
  removeReadLockHolder()
  {
    if(pReadLockHolders != null) {
      synchronized(pReadLockHolders) {
        Thread thread = Thread.currentThread(); 
        pReadLockHolders.remove(thread.getName() + "[" + thread.getId() + "]"); 
      }
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 1622287682186312526L;
  
  /**
   * The title of the lock for which read-lock holders should be logged.
   */ 
  private static final String  sReadHolderLockTitle; 


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The title to give this particular lock in log messages.
   */ 
  private String pTitle; 

  /**
   * The calling stack traces of all threads currently holding a read-lock indexed by
   * the thread name[ID].
   */ 
  private TreeMap<String,StackTraceElement[]>  pReadLockHolders;
  
}
