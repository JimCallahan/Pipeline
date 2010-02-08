// $Id: NodeID.java,v 1.14 2008/06/15 01:59:49 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.event.*;

import java.util.*;
import java.io.*;
import java.util.concurrent.atomic.*; 
import java.util.concurrent.locks.*; 

/*------------------------------------------------------------------------------------------*/
/*   N O D E   L O C K                                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Adds methods to support fail-fast read-locks and additional information about attempts
 * to acquire write-locks for node related data.
 */
public
class NodeLock
  extends ReentrantReadWriteLock
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new lock.
   */
  public
  NodeLock() 
  {
    super();

    pWriteEvents = new TreeMap<Long,BaseNodeEvent>();
    pNextID = new AtomicLong();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L O C K I N G                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Acquire the write-lock after registering an event descriping the operation requiring
   * the lock. 
   * 
   * @param event
   *   The event describing the node operation underway.
   * 
   * @return 
   *   The unique ID assigned to the event attempting to acquire the lock.
   */ 
  public long
  acquireWriteLock
  (
   BaseNodeEvent event
  ) 
  {
    if(event == null) 
      throw new IllegalArgumentException("The event cannot be (null)!"); 

    long eventID = pNextID.getAndIncrement();
    synchronized(pWriteEvents) {
      pWriteEvents.put(eventID, event); 
    }

    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Lck, LogMgr.Level.Fine)) {
      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Lck, LogMgr.Level.Fine, 
         event.getBasicMsg() + " of " + event.getNodeName() + 
         ": Acquiring Write-Lock, Event[" + eventID + "]");
    }

    writeLock().lock();

    return eventID;
  }

  /**
   * Release the write-lock and remove the previously registered operation event. 
   * 
   * @param eventID
   *   The unique ID assigned to the event holding the lock.
   */ 
  public void 
  releaseWriteLock
  (
   long eventID
  ) 
  {
    writeLock().unlock();

    BaseNodeEvent event = null;
    synchronized(pWriteEvents) {
      event = pWriteEvents.remove(eventID); 
    }

    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Lck, LogMgr.Level.Fine)) {
      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Lck, LogMgr.Level.Fine, 
         event.getBasicMsg() + " of " + event.getNodeName() + 
         ": Released Write-Lock, Event[" + eventID + "]" );
    }
  }

  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Try to acquire the read-lock, but return immediately if unable to do.<P> 
   * 
   * @return
   *   If able to acquire the read-lock, <CODE>null</CODE> is returned.  Otherwise the 
   *   event describing the oldest operation holding the lock is returned. If no events
   *   have been registered, then an UnknownEvent will be returned.
   */ 
  public BaseNodeEvent
  attemptReadLock() 
  {
    if(readLock().tryLock()) 
      return null;

    Long eventID = null;
    BaseNodeEvent event = null;
    synchronized(pWriteEvents) {
      for(Map.Entry<Long,BaseNodeEvent> entry : pWriteEvents.entrySet()) {
        BaseNodeEvent e = entry.getValue();  
        if((event == null) || (e.getTimeStamp() < event.getTimeStamp())) {
          eventID = entry.getKey();
          event = e; 
        }
      }
    }

    if(event == null) 
      event = new UnknownNodeEvent();

    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Lck, LogMgr.Level.Fine)) {
      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Lck, LogMgr.Level.Fine, 
         event.getBasicMsg() + " of " + event.getNodeName() + " for " + 
         event.getDurationMsg() + ": Held Write-Lock, " + 
         "Event[" + ((eventID != null) ? eventID : "?") + "]");
    }

    return event; 
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   L O G G I N G                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Log the events describing all operations in progress which require the write-lock.
   */ 
  public void 
  logWriteLockEvents()
  {
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Lck, LogMgr.Level.Fine)) {
      synchronized(pWriteEvents) {
        for(Map.Entry<Long,BaseNodeEvent> entry : pWriteEvents.entrySet()) {
          Long eventID = entry.getKey();
          BaseNodeEvent event = entry.getValue();  
          LogMgr.getInstance().logAndFlush
            (LogMgr.Kind.Lck, LogMgr.Level.Fine, 
             event.getBasicMsg() + " of " + event.getNodeName() + " for " + 
             event.getDurationMsg() + ": Held Write-Lock, Event[" + eventID + "]");
        }
      }
    }
  }

 

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -373683499313084059L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The table of events which describe the operations attempting to acquire the write-lock.
   */
  private TreeMap<Long,BaseNodeEvent>  pWriteEvents; 

  /**
   * A unique ID to give to the next write event. 
   */ 
  private AtomicLong  pNextID; 

}

