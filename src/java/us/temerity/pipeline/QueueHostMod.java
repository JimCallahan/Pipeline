// $Id: QueueHostMod.java,v 1.8 2009/12/09 05:05:55 jesse Exp $

package us.temerity.pipeline;

import java.io.Serializable;

import us.temerity.pipeline.message.*;

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
   *   Change to the operational status of the host or <CODE>null</CODE> to leave unchanged.
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
   *   
   * @param hardware
   *   The name of the current hardware group or <code>null</code> if the host should not
   *   have a hardware group.
   *   
   * @param hardwareModified
   *   Whether the hardware group should be modified.
   *   
   * @param dispatch
   *   The name of the current dispatch control or <code>null</code> if the host should not
   *   have a dispatch control.
   *   
   * @param dispatchModified
   *   Whether the dispatch control should be modified.
   *   
   * @param userBalance
   *   The name of the current user balance group or <code>null</code> if the host should not
   *   have a user balance group.
   *   
   * @param userBalanceModified
   *   Whether the user balance group should be modified.
   *   
   * @param favorMethod 
   *   The job group favor method the host will use  
   *   or <CODE>null</CODE> to leave it unchanged.
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
    boolean hardwareModified,
    String dispatch,
    boolean dispatchModified,
    String userBalance,
    boolean userBalanceModified,
    JobGroupFavorMethod favorMethod
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
    
    pDispatchControl = dispatch;
    pDispatchControlModified = dispatchModified;
    
    pUserBalance = userBalance;
    pUserBalanceModified = userBalanceModified;
    
    pFavorMethod = favorMethod;
    
    pGroupState           = EditableState.Manual;
    pStatusState          = EditableState.Manual;
    pReservationState     = EditableState.Manual;
    pSlotState            = EditableState.Manual;
    pOrderState           = EditableState.Manual;
    pDispatchControlState = EditableState.Manual;
    pUserBalanceState     = EditableState.Manual;
    pFavorMethodState     = EditableState.Manual;
    
    pAllowedStatus = null;
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
    this(statusChange, null, false, null, null, null, false, null, 
         false, null, false, null, false, null, false, null);
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
   * Get the new favor method for this host.
   * 
   * @return 
   *   The favor method.
   */ 
  public synchronized JobGroupFavorMethod
  getFavorMethod() 
  {
    return pFavorMethod;
  }

  /**
   * Whether the favor method should be changed.
   */ 
  public synchronized boolean
  isFavorMethodModified()
  {
    return (pFavorMethod != null); 
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
   * Get the name of the new dispatch control. 
   * 
   * @return
   *   The dispatch control or 
   *   <CODE>null</CODE> if the host should not have a dispatch control.
   */ 
  public synchronized String
  getDispatchControl() 
  {
    return pDispatchControl;
  }

  /**
   * Whether a change in dispatch control is pending.
   */ 
  public synchronized boolean
  isDispatchControlModified() 
  {
    return pDispatchControlModified; 
  }
  
/*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the name of the new user balance group. 
   * 
   * @return
   *   The user balance group or 
   *   <CODE>null</CODE> if the host should not have a user balance group.
   */ 
  public synchronized String
  getUserBalanceGroup() 
  {
    return pUserBalance;
  }

  /**
   * Whether a change in the user balance group is pending.
   */ 
  public synchronized boolean
  isUserBalanceGroupModified() 
  {
    return pUserBalanceModified; 
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
  
  /*----------------------------------------------------------------------------------------*/
  
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
  
  /*----------------------------------------------------------------------------------------*/

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
  
  /*----------------------------------------------------------------------------------------*/

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
  
  /*----------------------------------------------------------------------------------------*/

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
  
  /**
   * Is the dispatch control of the host editable.
   */
  public EditableState 
  getDispatchControlState()
  {
    return pDispatchControlState;
  }

  /**
   * Set the editable state of the host's dispatch control 
   */
  public void 
  setDispatchControlState
  (
    EditableState dispatchState
  )
  {
    pDispatchControlState = dispatchState;
  }
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Is the user balance group of the host editable?
   */
  public EditableState 
  getUserBalanceState()
  {
    return pUserBalanceState;
  }

  /**
   * Set the editable state of the host's user balance group.
   */
  public void 
  setUserBalanceState
  (
    EditableState userState
  )
  {
    pDispatchControlState = userState;
  }

  /*----------------------------------------------------------------------------------------*/

  /**
   * Is the favor method of the host editable?
   */
  public EditableState 
  getFavorState()
  {
    return pFavorMethodState;
  }

  /**
   * Set the editable state of the host's user favor method.
   */
  public void 
  setFavorState
  (
    EditableState favorState
  )
  {
    pFavorMethodState = favorState;
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
    QueueHostStatusChange allowedStatus = null;
    {
      QueueHostStatus current = info.getStatus();
      QueueHostStatus sch = sched.getScheduledStatus(sname);
      if (sch != null) {
	if (current != sch &&
	  !(current == QueueHostStatus.Limbo || current == QueueHostStatus.Shutdown || 
	    current == QueueHostStatus.Terminating)) 
	  changedStatus = sch.toQueueHostStatusChange();
	else if (current == QueueHostStatus.Limbo || current == QueueHostStatus.Shutdown || 
	  current == QueueHostStatus.Terminating)
	  allowedStatus = sch.toQueueHostStatusChange();
      }
	
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
    
    /* Group */
    boolean dispatchChanged = false;
    String newDispatch= null;
    {
      String current = info.getDispatchControl();
      String sch = sched.getScheduledDispatchControl(sname);
     
      if (sch != null) {
        if (sch.equals(SelectionRule.aNone) && current != null)
          dispatchChanged = true;
        else if (current == null || !sch.equals(current)) {
          dispatchChanged = true;
          newDispatch = sch;
        }
      }
    }
    
    /* User Balance Group */
    boolean userChanged = false;
    String newUser = null;
    {
      String current = info.getBalanceGroup();
      String sch = sched.getScheduledUserBalance(sname);
     
      if (sch != null) {
        if (sch.equals(SelectionRule.aNone) && current != null)
          userChanged = true;
        else if (current == null || !sch.equals(current)) {
          userChanged = true;
          newUser = sch;
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
    
    /* favor method */
    JobGroupFavorMethod newFavor = null;
    {
      JobGroupFavorMethod current = info.getFavorMethod();
      JobGroupFavorMethod sch = sched.getScheduledFavorMethod(sname);
      
      if (sch != null && sch != current)
        newFavor = sch;
    }
    
    boolean scheduleChanged = false;
    {
      String current = info.getSelectionSchedule();
      if (current == null || !current.equals(sname))
	scheduleChanged = true;
    }
    
    QueueHostMod mod = 
      new QueueHostMod(changedStatus, newReservation, reserveChanged, 
	               newOrder, newSlots, newGroup, groupChanged, 
	               sname, scheduleChanged, null, false, 
	               newDispatch, dispatchChanged,
	               newUser, userChanged, newFavor);
    mod.pAllowedStatus = allowedStatus;
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
    mod.setDispatchControlState(sched.getScheduledDispatchControlState(sname));
    mod.setUserBalanceState(sched.getScheduledUserBalanceState(sname));
    mod.setFavorState(sched.getScheduledFavorMethodState(sname));
  }
  
  /**
   * Modify the first {@link QueueHostMod}, which should have been generated from a selection
   * schedule (using the
   * {@link #getModFromSchedule(SelectionScheduleMatrix, String, QueueHostInfo) 
   * getModFromSchedule} method) so that it will incorporate all allowable changes from the 
   * second {@link QueueHostMod}.<P>
   * 
   * Obviously any fields that the schedule is controlling will keep their original value.<P>
   * 
   * This method assumes that both values passed in are not <code>null</code>.
   */
  public static void
  combineMods
  (
    QueueHostMod scheduledMod,
    QueueHostMod otherMod
  )
  {
    /*
     * Cases where we want to override what the schedule says.
     * 1. If the schedule is not controlling the status.  duh.
     * 2. If the job server is being terminated.  This is always allowed.
     * 3. If the schedule has an allowable schedule value which is not being
     * set because the host is in an off state (either shutdown or hung) and
     * the value being set is the value the schedule would have set if it was
     * not ignoring its value.
     */
    if (otherMod.pStatusChange != null) {
      EditableState statusState = scheduledMod.getStatusState(); 
      if ( statusState != EditableState.Automatic) 
	scheduledMod.setStatus(otherMod.pStatusChange);
      else {
	if (otherMod.pStatusChange == QueueHostStatusChange.Terminate)
	  scheduledMod.setStatus(otherMod.pStatusChange);
	else if (otherMod.pStatusChange == scheduledMod.pAllowedStatus)
	  scheduledMod.setStatus(otherMod.pStatusChange);
      }
    }
    
    if (scheduledMod.getOrderState() != EditableState.Automatic)
      if (otherMod.pOrder != null)
	scheduledMod.pOrder = new Integer(otherMod.pOrder);
    
    if (scheduledMod.getSlotsState() != EditableState.Automatic)
      if (otherMod.pJobSlots != null)
	scheduledMod.pJobSlots = new Integer(otherMod.pJobSlots);
    
    if (scheduledMod.getFavorState() != EditableState.Automatic)
      if (otherMod.pFavorMethod != null)
        scheduledMod.pFavorMethod = otherMod.pFavorMethod;

    
    if (scheduledMod.getGroupState() != EditableState.Automatic) {
      if (otherMod.isSelectionGroupModified()) {
	scheduledMod.pSelectionGroupModified = true;
	if (otherMod.pSelectionGroup == null)
	  scheduledMod.pSelectionGroup = null;
	else
	  scheduledMod.pSelectionGroup = new String(otherMod.pSelectionGroup);
      }
    }
    
    if (scheduledMod.getDispatchControlState() != EditableState.Automatic) {
      if (otherMod.isDispatchControlModified()) {
        scheduledMod.pDispatchControlModified = true;
        if (otherMod.pDispatchControl == null)
          scheduledMod.pDispatchControl = null;
        else
          scheduledMod.pDispatchControl = new String(otherMod.pDispatchControl);
      }
    }
    
    if (scheduledMod.getUserBalanceState() != EditableState.Automatic) {
      if (otherMod.isUserBalanceGroupModified()) {
        scheduledMod.pUserBalanceModified = true;
        if (otherMod.pUserBalance == null)
          scheduledMod.pUserBalance = null;
        else
          scheduledMod.pUserBalance = new String(otherMod.pUserBalance);
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
   * The status that the schedule would set the status to if it was going to change it
   * but couldn't or <code>null</code> if the schedule doesn't want to change the 
   * statue or if it can successfully set the status.
   */
  private QueueHostStatusChange  pAllowedStatus;
  
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

  /**
   * The name of the new dispatch control or <CODE>null</CODE> if the host should not have a
   * dispatch control.
   */ 
  private String   pDispatchControl; 
  private boolean  pDispatchControlModified; 
  
  /**
   * The name of the new user balance group or <CODE>null</CODE> if the host should not have a
   * user balance group.
   */
  private String   pUserBalance; 
  private boolean  pUserBalanceModified; 
  
  /**
   * The name of the favor method or <CODE>null</CODE> if the favor method is not being
   * modified.
   */ 
  private JobGroupFavorMethod pFavorMethod; 
  
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

  /**
   * Is the dispatch control of the machine editable?
   * <p>
   * This is being set by the scheduler when the schedule on the machine is being applied. 
   */
  private EditableState pDispatchControlState;
  
  /**
   * Is the user balance group of the machine editable?
   * <p>
   * This is being set by the scheduler when the schedule on the machine is being applied. 
   */
  private EditableState pUserBalanceState;
  
  /**
   * Is the favor method of the machine editable?
   * <p>
   * This is being set by the scheduler when the schedule on the machine is being applied. 
   */
  private EditableState pFavorMethodState;

}
