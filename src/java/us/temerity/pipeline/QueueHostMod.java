// $Id: QueueHostMod.java,v 1.1 2006/11/21 19:59:30 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   H O S T   M O D                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Changes to the state of user modifiable properties of a job server host.
 */
public
class QueueHostMod
  implements Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a set of changes to job server state.
   * 
   * When a host is reserved, only jobs submitted by the reserving user (or group) will be 
   * assigned to the host.  The reservation can be cleared by setting the reserving user 
   * name to <CODE>null</CODE> for the host in the <CODE>reservations</CODE> argument. <P> 
   * 
   * Each host has a maximum number of jobs which it can be assigned at any one time 
   * regardless of the other limits on system resources.  This method allows the number
   * of slots to be changed for a number of hosts at once.  The number of slots cannot 
   * be negative and probably should not be set considerably higher than the number of 
   * CPUs on the host.  A good value is probably (1.5 * number of CPUs). <P>
   * 
   * The selection group contains the selection key biases and preemption flags used to 
   * compute the total selection score on the host.  If <CODE>null</CODE> is passed as the 
   * selection group for a host, then the host will behave as if a member of a selection 
   * group with no selection keys defined. <P> 
   * 
   * The selection schedule determines how selection groups are automatically changed based 
   * on a predefined schedule.  If <CODE>null</CODE> is passed as the name of the schedule for
   * a host, then the selection schedule will be cleared and the selection group will be 
   * specified manually. <P> 
   * 
   * For an detailed explanation of how selection keys are used to determine the assignment
   * of jobs to hosts, see {@link JobReqs JobReqs}. <P> 
   * 
   * @param statusChange
   *   Change to the operational status of the host 
   *   or <CODE>null</CODE> to leave unchanged.
   * 
   * @param reservation
   *   The name of the reserving user 
   *   or <CODE>null</CODE> if the host is not reserved or unchanged.
   * 
   * @param reservationModified
   *   Whether the reservation should be modified.
   * 
   * @param order 
   *   The order in which job servers are processed by the dispatcher
   *   or <CODE>null</CODE> to leave unchanged.
   * 
   * @param slots 
   *   The maximum number jobs the host may be assigned
   *   or <CODE>null</CODE> to leave unchanged.
   * 
   * @param group
   *   The name of the current selection group 
   *   or <CODE>null</CODE> not a member of any selection group. 
   * 
   * @param groupModified
   *   Whether the selection group should be modified.
   * 
   * @param schedule
   *   The name of the current selection schedule 
   *   or <CODE>null</CODE> if the choice of selection group is currently manual.
   * 
   * @param scheduleModified
   *   Whether the selection schedule should be modified.
   */ 
  public
  QueueHostMod
  (
   QueueHostStatusChange statusChange, 
   String reservation, 
   boolean reservationModified, 
   Integer order, 
   Integer slots, 
   String group, 
   boolean groupModified, 
   String schedule, 
   boolean scheduleModified
  ) 
  {
    pStatusChange = statusChange;

    pReservation = reservation;
    pReservationModified = reservationModified; 

    pOrder    = order;
    pJobSlots = slots;

    pSelectionGroup = group; 
    pSelectionGroupModified = groupModified;

    pSelectionSchedule = schedule; 
    pSelectionScheduleModified = scheduleModified;
  }

  /**
   * Construct a changes to only the job server status.
   */ 
  public
  QueueHostMod
  (
   QueueHostStatusChange statusChange
  ) 
  {
    this(statusChange, null, false, null, null, null, false, null, false);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the change to the operational status of the host.
   */ 
  public synchronized QueueHostStatusChange
  getStatus() 
  {
    return pStatusChange;
  }

  /**
   * Set a change to the operational status of the host.
   */ 
  public synchronized void
  setStatus
  (
   QueueHostStatusChange change
  ) 
  {
    pStatusChange = change;
  }

  /**
   * Whether the operational status of the host should be changed.
   */ 
  public synchronized boolean
  isStatusModified()
  {
    return (pStatusChange != null);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the user (or group) who should be reserving the host. <P> 
   * 
   * When a host is reserved, only jobs submitted by the reserving user will be assigned
   * to the host.
   * 
   * @return 
   *   The name of the reserving user or <CODE>null</CODE> if the host should not
   *   be reserved.
   */ 
  public synchronized String
  getReservation() 
  {
    return pReservation;
  }

  /**
   * Whether the reservation should be changed.
   */ 
  public synchronized boolean
  isReservationModified()
  {
    return pReservationModified; 
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the new job dispatching order for this host.
   * 
   * @return 
   *   The dispatch order.
   */ 
  public synchronized Integer
  getOrder() 
  {
    return pOrder;
  }

  /**
   * Whether the job dispatching order should be changed.
   */ 
  public synchronized boolean
  isOrderModified()
  {
    return (pOrder != null); 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the maximum number jobs the host should be assigned.
   * 
   * @return 
   *   The number of job slots.
   */ 
  public synchronized Integer
  getJobSlots() 
  {
    return pJobSlots;
  }

  /**
   * Whether the maximum number jobs the host should be changed.
   */ 
  public synchronized boolean
  isJobSlotsModified()
  {
    return (pJobSlots != null); 
  }

  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the name of the new selection group. 
   * 
   * @return
   *   The selection group or 
   *   <CODE>null</CODE> if the host should not be a member of any selection group.
   */ 
  public synchronized String
  getSelectionGroup() 
  {
    return pSelectionGroup;
  }

  /**
   * Whether a change in selection group is pending.
   */ 
  public synchronized boolean
  isSelectionGroupModified() 
  {
    return pSelectionGroupModified; 
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the new selection schedule.
   * 
   * @return
   *   The schedule name or 
   *   <CODE>null</CODE> if the choice of selection group should be manual.
   */ 
  public synchronized String
  getSelectionSchedule() 
  {
    return pSelectionSchedule;
  }

  /**
   * Whether a change in selection schedule is pending.
   */ 
  public synchronized boolean
  isSelectionScheduleModified() 
  {
    return pSelectionScheduleModified; 
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 104651846221860295L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Changes to the operational status of the host or <CODE>null</CODE> if unchanged.
   */ 
  private QueueHostStatusChange  pStatusChange;

  /**
   * The name of the reserving user or <CODE>null</CODE> if the host should not be reserved.
   */ 
  private String   pReservation;
  private boolean  pReservationModified;

  /**
   * The new job dispatching order for this host.
   */ 
  private Integer  pOrder; 

  /**
   * The maximum number jobs the host should be assigned.
   */ 
  private Integer  pJobSlots; 
  
  /**
   * The name of the new selection group or 
   * <CODE>null</CODE> if the host should not be a member of any selection group.
   */ 
  private String   pSelectionGroup; 
  private boolean  pSelectionGroupModified; 

  /**
   * The name of the new selection schedule or 
   * <CODE>null</CODE> if the choice of selection group should be manual.
   */ 
  private String   pSelectionSchedule; 
  private boolean  pSelectionScheduleModified; 

}
