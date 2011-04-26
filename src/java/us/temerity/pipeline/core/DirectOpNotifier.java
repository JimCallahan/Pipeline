package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.NotifyRsp;
import us.temerity.pipeline.math.ExtraMath;

import java.io.*;
import java.net.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   D I R E C T   O P   N O T I F I E R                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A helper class used by server operations to communicate the progress of a potentially 
 * long and expensive operation back to clients by calling updating a set of monitors 
 * directly.
 */ 
public 
class DirectOpNotifier
  extends BaseOpNotifier
  implements OpNotifiable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a operation progress helper.
   */
  public
  DirectOpNotifier() 
  {
    super();
    
    pOpMonitors = new TreeMap<Long,OpMonitorable>();
    pNextOpMonitorID = 0L;
  }



  /*----------------------------------------------------------------------------------------*/
  /*  O P E R A T I O N   M O N I T O R I N G                                               */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Add a operation progress monitor.
   * 
   * @returns
   *   The unique ID used to remove the monitor.
   */
  public long 
  addMonitor
  (
   OpMonitorable monitor
  ) 
  {
    if(monitor == null) 
      throw new IllegalArgumentException("The operation monitor cannot be (null)!"); 

    long opID = pNextOpMonitorID++;
    pOpMonitors.put(opID, monitor);

    return opID;
  }
  
  /**
   * Remove an operation progress monitor.
   * 
   * @param monitorID
   *   The unique ID of the monitor.
   * 
   * @returns
   *   The removed monitor or <CODE>null</CODE> if none exists.
   */
  public OpMonitorable
  removeMonitor
  (
   long monitorID
  ) 
  {
    return pOpMonitors.remove(monitorID);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*  N O T I F I C A T I O N S                                                             */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Notify the client of progress.
   * 
   * @param timer
   *   The current task timer.
   * 
   * @param msg
   *   The progress message.
   */
  public void 
  notify
  (
   TaskTimer timer,
   String msg
  ) 
  {
    updateMonitors(new TaskTimer(timer, msg), null); 
  }

  /**
   * Notify the client of progress.
   * 
   * @param timer
   *   The current task timer.
   * 
   * @param msg
   *   The progress message.
   * 
   * @param percentage
   *   An update of the estimated percentage complete or 
   *   <CODE>null</CODE> if no estimate is available.
   */
  public void 
  notify
  (
   TaskTimer timer,
   String msg, 
   Float percentage
  ) 
  {
    updateMonitors(new TaskTimer(timer, msg), percentage); 
  }

  /**
   * Notify the client that a step has been completed.
   * 
   * @param timer
   *   The current task timer.
   */
  public void 
  step
  (
   TaskTimer timer
  ) 
  {
    steps(timer, null, 1L);
  }

  /**
   * Notify the client that a step has been completed.
   * 
   * @param timer
   *   The current task timer.
   * 
   * @param msg
   *   A message describing the step. 
   */
  public void 
  step
  (
   TaskTimer timer, 
   String msg   
  ) 
  {
    steps(timer, msg, 1L);
  }

  /**
   * Notify the client that a step has been completed.
   * 
   * @param timer
   *   The current task timer.
   * 
   * @param msg
   *   A message describing the step. 
   * 
   * @param completed
   *   The number of steps completed. 
   */
  public void 
  steps
  (
   TaskTimer timer, 
   String msg, 
   long completed
  ) 
  {
    incrementCompleted(completed); 
    updateMonitors(new TaskTimer(timer, msg), getPercentage()); 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update all of the operation monitors.
   * 
   * @param timer
   *   The current operation execution timer.
   * 
   * @param percentage
   *   An update of the estimated percentage complete or 
   *   <CODE>null</CODE> if no estimate is available.
   */
  private void 
  updateMonitors
  (
   TaskTimer timer, 
   Float percentage 
  )
  {
    for(OpMonitorable monitor : pOpMonitors.values()) 
      monitor.update(timer, percentage); 
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A table of named operation monitors.
   */
  private TreeMap<Long,OpMonitorable> pOpMonitors;
 
  /**
   * The unique ID to give the next monitor added.
   */ 
  private long pNextOpMonitorID;

}

