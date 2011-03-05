// $Id: DownstreamLinks.java,v 1.12 2008/09/29 19:02:17 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.TrackedReq;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.Semaphore;

/*------------------------------------------------------------------------------------------*/
/*   T R A C K E D   L O C K                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A ReentrantLock which manages a unique lock ID to help prevent the release of a lock by
 * other than the thread which originally acquired the lock.
 */
public
class TrackedLock
   
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new lock.
   * 
   * @param name
   *   The unique name of the lock.
   */ 
  public
  TrackedLock
  (
   String name
  ) 
  {
    if(name == null) 
      throw new IllegalArgumentException("The name cannot be (null)!");
    pName = name;

    pSema = new Semaphore(1, true);
    pLockID = new AtomicLong(-1L);
    pLockerLock = new Object();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/
 
  /** 
   * Whether the lock is currently held. <P> 
   * 
   * Should be used for debugging purposes only.
   */ 
  public boolean
  isLocked()
  {
    return (pSema.availablePermits() > 0);
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the name of this lock. 
   */ 
  public String
  getName() 
  {
    if(pName == null)
      throw new IllegalStateException(); 
    return pName;
  }

  /** 
   * Details about the current holder of the lock. <P> 
   * 
   * Should be used for debugging purposes only.
   * 
   * @return
   *   Then locker information or <CODE>null</CODE> if not held.
   */ 
  public RequestInfo
  getLockerInfo()
  {
    synchronized(pLockerLock) {
      if(pLockerInfo != null) 
        return new RequestInfo(pLockerInfo);
      return null;
    }
  }
  
  
 
  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Block until able to acquire a lock. <P> 
   * 
   * @name req
   *   The network request.
   * 
   * @return 
   *   The unique ID required to release the lock. 
   * 
   * @throws PipelineException
   *   If the calling thread is interrupted.
   */ 
  public Long
  acquireLock
  (
   TrackedReq req
  ) 
    throws PipelineException
  {
    trackPreAcquire(req);

    try {
      pSema.acquire();
    }
    catch(InterruptedException ex) {
      throw new PipelineException("Somehow the current thread was interrupted!"); 
    }

    trackAcquired(req);

    Long id = sRandom.nextLong();
    pLockID.set(id);
    return id; 
  }

  /** 
   * Attempt to acquire a lock, returns immediately. 
   * 
   * @name req
   *   The network request.
   * 
   * @return 
   *  The unique ID required to release the lock or 
   *  <CODE>null</CODE> if unable to immediately acquire the lock. 
   */ 
  public Long 
  tryLock
  (
   TrackedReq req
  ) 
  {
    trackPreAcquire(req);

    if(!pSema.tryAcquire()) 
      return null;

    trackAcquired(req);

    Long id = sRandom.nextLong();
    pLockID.set(id);
    return id; 
  }

  /** 
   * Release a lock. 
   * 
   * @name req
   *   The network request.
   * 
   * @param lockID 
   *  The unique ID of the lock obtained when it was acquired. 
   * 
   * @throws PipelineException
   *   If an illegal lock ID is given.
   */ 
  public void 
  releaseLock
  (
   TrackedReq req,
   Long lockID
  )
    throws PipelineException
  {
    if(lockID == null) 
      throw new PipelineException
        ("The lock ID cannot be (null)!");
    
    if(pLockID.get() != lockID) 
      throw new PipelineException
        ("The lock ID (" + lockID + ") given does not match that used to acquire the lock!"); 


    pLockID.set(-1L);
    pSema.release(); 

    trackReleased(req);
  }
  
  /** 
   * Force the release of a lock.
   * 
   * @name req
   *   The network request.
   * 
   * @throws PipelineException
   *   If an illegal lock ID is given.
   */ 
  public void 
  breakLock
  (
   TrackedReq req
  ) 
  {
    pLockID.set(-1L);
    pSema.release(); 

    trackReleased(req);
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Log the attempt to acquire lock.
   * 
   * @name req
   *   The network request.
   */
  private void 
  trackPreAcquire
  (
   TrackedReq req
  ) 
  {
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Nlk, LogMgr.Level.Finer)) {
      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Nlk, LogMgr.Level.Fine, 
         "Acquiring (" + pName + ") Client Lock" + 
         req.getRequestInfo().logMessage(LogMgr.getInstance(), LogMgr.Kind.Nlk));
    }
  }

  /**
   * Log the acquisition of a lock and save caller name and stack trace.
   * 
   * @name req
   *   The network request.
   */
  private void 
  trackAcquired
  (
   TrackedReq req
  ) 
  {
    RequestInfo info = req.getRequestInfo(); 
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Nlk, LogMgr.Level.Fine)) {
      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Nlk, LogMgr.Level.Fine, 
         "Client Lock (" + pName + ") Acquired" + 
         info.logMessage(LogMgr.getInstance(), LogMgr.Kind.Nlk));
    }

    synchronized(pLockerLock) {
      pLockerInfo = info;
    }
  }

  /**
   * Log the release of a lock and clear caller name and stack trace.
   * 
   * @name req
   *   The network request.
   */
  private void 
  trackReleased
  (
   TrackedReq req
  ) 
  {
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Nlk, LogMgr.Level.Fine)) {
      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Nlk, LogMgr.Level.Fine, 
         "Client Lock (" + pName + ") Released" +
         req.getRequestInfo().logMessage(LogMgr.getInstance(), LogMgr.Kind.Nlk)); 
    }    

    synchronized(pLockerLock) {
      pLockerInfo = null;
    }
  }




  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3219681887472141354L;   

  private static Random sRandom = new Random();
                     


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique name of the lock. 
   */
  private String  pName; 
  
  /**
   * The unique given to the last thread which acquired the lock.
   */
  private Semaphore  pSema;
  
  /**
   * The unique given to the last thread which acquired the lock.
   */
  private AtomicLong  pLockID; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Lock which protects access to the following fields.
   */ 
  private Object  pLockerLock;
  
  /**
   * The stack trace of the current client holding the lock.
   */
  private  RequestInfo  pLockerInfo; 

}

