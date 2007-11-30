// $Id: QueueHostMod.java,v 1.2 2007/11/30 20:14:23 jesse Exp $

package us.temerity.pipeline;

import java.io.Serializable;

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
   boolean scheduleModified,
   String hardware,
   boolean hardwareModified
  ) 
  {
    pStatusChange = statusChange;

    pReservation = reservation;
    pReservationModified = reservationModified; 

    pOrder    = order;
    pJobSlots = slots;

    pSelectionGroup = group; 
    pSelectionGroupModified = groupModified;
    
    pHardwareGroup = hardware; 
    pHardwareGroupModified = hardwareModified;

    pSelectionSchedule = schedule; 
    pSelectionScheduleModified = scheduleModified;
    
    pGroupState = EditableState.Manual;
    pStatusState = EditableState.Manual;
    pReservationState = EditableState.Manual;
    pSlotState = EditableState.Manual;
    pOrderState = EditableState.Manual;
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
    this(statusChange, null, false, null, null, null, false, null, false, null, false);
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
   * Get the name of the new hardware group. 
   * 
   * @return
   *   The hardware group or 
   *   <CODE>null</CODE> if the host should not be a member of any hardware group.
   */ 
  public synchronized String
  getHardwareGroup() 
  {
    return pHardwareGroup;
  }

  /**
   * Whether a change in hardware group is pending.
   */ 
  public synchronized boolean
  isHardwareGroupModified() 
  {
    return pHardwareGroupModified; 
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

  /**
   * Is the selection group of the host editable.
   */
  public EditableState
  getGroupState()
  {
    return pGroupState;
  }
  
  /**
   * @param groupState the groupState to set
   */
  public void 
  setGroupState
  (
    EditableState groupState
  )
  {
    pGroupState = groupState;
  }
  
  /**
   * Is the reservation status of the host editable.
   */
  public EditableState 
  getReservationState()
  {
    return pReservationState;
  }
  
  /**
   * @param reservationState the reservationState to set
   */
  public void 
  setReservationState
  (
    EditableState reservationState
  )
  {
    pReservationState = reservationState;
  }

  /**
   * Is the status of the host editable.
   */
  public EditableState
  getStatusState()
  {
    return pStatusState;
  }
  
  /**
   * @param statusState the statusState to set
   */
  public void 
  setStatusState
  (
    EditableState statusState
  )
  {
    pStatusState = statusState;
  }

  /**
   * Is the number of slots on the host editable.
   */
  public EditableState 
  getSlotsState()
  {
    return pSlotState;
  }
  
  /**
   * @param slotState the slotState to set
   */
  public void 
  setSlotsState
  (
    EditableState slotState
  )
  {
    pSlotState = slotState;
  }

  /**
   * Is the order of the host editable.
   */
  public EditableState 
  getOrderState()
  {
    return pOrderState;
  }

  /**
   * @param orderState the orderState to set
   */
  public void 
  setOrderState
  (
    EditableState orderState
  )
  {
    pOrderState = orderState;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   C O N S T R U C T O R                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Looks at a {@link SelectionScheduleMatrix} and {@link QueueHostInfo} and build a
   * {@link QueueHostMod} which could be used to make the Host adhere to the schedule.
   * <p>
   * This method assumes that all values passed in are not <code>null</code>.
   * 
   * @param sched
   *   The Matrix of all values for all schedules at a given time.
   * @param sname
   *   The name of the Schedule that this Mod will represent
   * @param info
   *   The current state of the host the schedule is being applied to.
   */
  public static QueueHostMod
  getModFromSchedule
  (
    SelectionScheduleMatrix sched,
    String sname,
    QueueHostInfo info
  )
  {
    /* Status */
    QueueHostStatusChange changedStatus = null;
    {
      QueueHostStatus current = info.getStatus();
      QueueHostStatus sch = sched.getScheduledStatus(sname);
      if (sch != null && current != sch &&
	  !(current == QueueHostStatus.Hung || current == QueueHostStatus.Shutdown || 
	  current == QueueHostStatus.Terminating)) 
	changedStatus = sch.toQueueHostStatusChange();
    }
    
    /* Group */
    boolean groupChanged = false;
    String newGroup = null;
    {
      String current = info.getSelectionGroup();
      String sch = sched.getScheduledGroup(sname);
     
      if (sch != null) {
	if (sch.equals(SelectionRule.aNone) && current != null)
	  groupChanged = true;
	else if (current == null || !sch.equals(current)) {
	  groupChanged = true;
	  newGroup = sch;
	}
      }
    }
    
    /* Order */
    Integer newOrder = null;
    {
      Integer current = info.getOrder();
      Integer sch = sched.getScheduledOrder(sname);
      
      if (sch != null && !sch.equals(current)) 
	newOrder = sch;
    }
    
    /* Slots */
    Integer newSlots = null;
    {
      Integer current = info.getJobSlots();
      Integer sch = sched.getScheduledSlots(sname);
      
      if (sch != null && !sch.equals(current)) 
	newSlots = sch;
    }
    
    /* Reservations */
    boolean reserveChanged = false;
    String newReservation = null;
    {
      String current = info.getReservation();
      boolean remove = sched.getScheduledReservation(sname);
      if (remove && current != null) {
	reserveChanged = true;
      }
    }
    
    boolean scheduleChanged = false;
    {
      String current = info.getSelectionSchedule();
      if (current != null && !current.equals(sname))
	scheduleChanged = true;
    
    }
    
    QueueHostMod mod = 
      new QueueHostMod(changedStatus, newReservation, reserveChanged, 
	               newOrder, newSlots, newGroup, groupChanged, 
	               sname, scheduleChanged, null, false);
    setModStateFromSched(mod, sched, sname);
    
    return mod;
  }
  
  /**
   * Apply the {@link EditableState} values in a {@link SelectionScheduleMatrix} at a given
   * time to a {@link QueueHostMod}.
   * <p>
   * This method assumes that the values passed in are not <code>null</code>.
   * 
   * @param mod
   *   The {@link QueueHostMod} to be modified.
   * @param sched
   *   The Matrix of all values for all schedules at a given time.
   * @param sname
   *   The name of the Schedule that this Mod will represent
   */
  public static void
  setModStateFromSched
  (
    QueueHostMod mod,
    SelectionScheduleMatrix sched,
    String sname
  )
  {
    mod.setGroupState(sched.getScheduledGroupState(sname));
    mod.setOrderState(sched.getScheduledOrderState(sname));
    mod.setSlotsState(sched.getScheduledSlotsState(sname));
    mod.setStatusState(sched.getScheduledStatusState(sname));
    mod.setReservationState(sched.getScheduledReservationState(sname));
  }
  
  /**
   * Modify the first {@link QueueHostMod}, which should have been generated from a selection
   * schedule (using the
   * {@link #getModFromSchedule(SelectionScheduleMatrix, String, QueueHostInfo) getModFromSchedule} method)
   * so that it will incorporate all allowable changes from the second {@link QueueHostMod}.
   * <p>
   * Obviously any fields that the schedule is controlling will keep their original value.
   * <p>
   * This method assumes that both values passed in are not <code>null</code>.
   */
  public static void
  combineMods
  (
    QueueHostMod scheduledMod,
    QueueHostMod otherMod
  )
  {
    if (scheduledMod.getStatusState() != EditableState.Automatic)
      if (otherMod.pStatusChange != null)
	scheduledMod.setStatus(otherMod.pStatusChange);
    
    if (scheduledMod.getOrderState() != EditableState.Automatic)
      if (otherMod.pOrder != null)
	scheduledMod.pOrder = new Integer(otherMod.pOrder);
    
    if (scheduledMod.getSlotsState() != EditableState.Automatic)
      if (otherMod.pJobSlots != null)
	scheduledMod.pJobSlots = new Integer(otherMod.pJobSlots);
    
    if (scheduledMod.getGroupState() != EditableState.Automatic) {
      if (otherMod.isSelectionGroupModified()) {
	scheduledMod.pSelectionGroupModified = true;
	if (otherMod.pSelectionGroup == null)
	  scheduledMod.pSelectionGroup = null;
	else
	  scheduledMod.pSelectionGroup = new String(otherMod.pSelectionGroup);
      }
    }
    
    if (scheduledMod.getReservationState() != EditableState.Automatic) {
      if (otherMod.isReservationModified()) {
	scheduledMod.pReservationModified = true;
	if (otherMod.pReservation == null)
	  scheduledMod.pReservation = null;
	else
	  scheduledMod.pReservation = new String(otherMod.pReservation);
      }
    }
    
    scheduledMod.pHardwareGroup = otherMod.pHardwareGroup;
    scheduledMod.pHardwareGroupModified = otherMod.pHardwareGroupModified;
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
   * The name of the new hardware group or 
   * <CODE>null</CODE> if the host should not be a member of any hardware group.
   */ 
  private String   pHardwareGroup; 
  private boolean  pHardwareGroupModified; 

  /**
   * The name of the new selection schedule or 
   * <CODE>null</CODE> if the choice of selection group should be manual.
   */ 
  private String   pSelectionSchedule; 
  private boolean  pSelectionScheduleModified;
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Is the reservation status of the machine editable?
   * <p>
   * This is being set by the scheduler when the schedule on the machine is being applied. 
   */
  private EditableState pReservationState;
  
  /**
   * Is the status of the machine editable?
   * <p>
   * This is being set by the scheduler when the schedule on the machine is being applied. 
   */
  private EditableState pStatusState;
  
  /**
   * Is the number of slots on the machine editable?
   * <p>
   * This is being set by the scheduler when the schedule on the machine is being applied. 
   */
  private EditableState pSlotState;
  
  /**
   * Is the order of the machine editable?
   * <p>
   * This is being set by the scheduler when the schedule on the machine is being applied. 
   */
  private EditableState pOrderState;
  
  /**
   * Is the selection group of the machine editable?
   * <p>
   * This is being set by the scheduler when the schedule on the machine is being applied. 
   */
  private EditableState pGroupState;

}
