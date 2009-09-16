// $Id: QueueHostInfo.java,v 1.11 2009/09/16 03:54:40 jesse Exp $

package us.temerity.pipeline;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   H O S T   I N F O                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The current and pending status of a job server host. 
 */
public
class QueueHostInfo
  extends Named
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */
  public
  QueueHostInfo()
  {
    pStatus = QueueHostStatus.Shutdown; 
    pHoldTimeStamp = 0L;
    pFavorMethod = JobGroupFavorMethod.None;
    pGroupState = EditableState.Manual;
    pStatusState = EditableState.Manual;
    pReservationState = EditableState.Manual;
    pSlotState = EditableState.Manual;
    pOrderState = EditableState.Manual;
    pDispatchState = EditableState.Manual;
    pFavorState = EditableState.Manual;
    pUserState = EditableState.Manual;
  }

  /**
   * Construct new set of information about a job server host.
   * 
   * @param name
   *   The fully resolved name of the host.
   * 
   * @param status
   *   The current operational status of the host.
   * 
   * @param reservation
   *   The name of the reserving user or <CODE>null</CODE> if the host is not reserved.
   * 
   * @param order 
   *   The order in which job servers are processed by the dispatcher.
   * 
   * @param slots 
   *   The maximum number jobs the host may be assigned.
   * 
   * @param os
   *   The operating system type.
   * 
   * @param numProcs
   *   The number of processors on the host or <CODE>null</CODE> if unknown.
   * 
   * @param totalMem
   *   The total amount of memory (in bytes) on the host 
   *   or <CODE>null</CODE> if unknown.
   * 
   * @param totalDisk
   *   The total amount of temporary disk space (in bytes) on the host or <CODE>null</CODE> 
   *   if unknown.
   * 
   * @param hold 
   *   The timestamp (milliseconds since midnight, January 1, 1970 UTC) of when all ramp-up 
   *   intervals will have expired or <CODE>null</CODE> for no holds.
   * 
   * @param sample
   *   The latest sample or <CODE>null</CODE> if there are no samples.
   * 
   * @param dispatchControl
   *   The name of the current dispatch control or <code>null</code> if not a member of any
   *   dispatch control.
   * 
   * @param group
   *   The name of the current selection group or <CODE>null</CODE> if not a member of any 
   *   selection group. 
   * 
   * @param schedule
   *   The name of the current selection schedule or <CODE>null</CODE> if the choice of 
   *   selection group is currently manual.
   *   
   * @param hardwareGroup
   *   The name of the current hardware group or <CODE>null</CODE> if not a member of any 
   *   hardware group.
   * 
   * @param userBalanceGroup
   *   The name of the current user balance group or <code>null</code> if not a member of any
   *   user balance group. 
   * 
   * @param favorMethod 
   *   The job group favor method.
   *   
   * @param groupState
   *   Is the selection group of this host editable. 
   *  
   * @param statusState
   *   Is the status of this host editable.
   *   
   * @param reservationState
   *   Is the reservation status of this host editable.
   *   
   * @param orderState
   *   Is the order of this host editable.
   *   
   * @param slotState
   *   Is the number of slots on this host editable.
   *   
   * @param dispatchState
   *   Is the dispatch control on this host editable.
   *   
   * @param userState
   *   Is the user balance group on this host editable.
   *   
   * @param favorState
   *   Is the job group favor method on this host editable.
   */ 
  public
  QueueHostInfo
  (
   String name, 
   QueueHostStatus status, 
   String reservation, 
   int order, 
   int slots, 
   OsType os, 
   Integer numProcs, 
   Long totalMem, 
   Long totalDisk, 
   Long hold, 
   ResourceSample sample,
   String dispatchControl,
   String group,
   String schedule,
   String hardwareGroup,
   String userBalanceGroup,
   JobGroupFavorMethod favorMethod,
   EditableState groupState,
   EditableState statusState,
   EditableState reservationState,
   EditableState orderState,
   EditableState slotState,
   EditableState dispatchState,
   EditableState userState,
   EditableState favorState
  ) 
  {
    super(name);

    initShortName();

    if(status == null) 
      throw new IllegalArgumentException("The status cannot be (null)!");
    pStatus = status;

    pReservation = reservation;
    pOrder = order;
    pJobSlots = slots;

    pOsType = os; 
    pNumProcessors = numProcs; 
    pTotalMemory = totalMem; 
    pTotalDisk = totalDisk;

    if(hold == null) 
      pHoldTimeStamp = 0L;
    else 
      pHoldTimeStamp = hold; 

    pSample = sample;

    pSelectionGroup = group; 
    pSelectionSchedule = schedule; 
    pHardwareGroup = hardwareGroup;
    pUserBalanceGroup = userBalanceGroup;
    pDispatchControl = dispatchControl;
    
    pFavorMethod = favorMethod;
    
    pGroupState = groupState;
    pStatusState = statusState;
    pReservationState = reservationState;
    pSlotState = slotState;
    pOrderState = orderState;
    pDispatchState = dispatchState;
    pFavorState = favorState;
    pUserState = userState;
  }

  /**
   * Initialize the short hostname from the fully resolved hostname.
   */
  private synchronized void
  initShortName() 
  {
    Matcher n = sNumericHostPattern.matcher(pName);
    Matcher s = sShortHostPattern.matcher(pName);
    if(!n.matches() && s.find() && (s.group().length() > 0))
      pShortName = s.group();
    else 
      pShortName = pName;
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether a change in the status of the host is pending.
   */ 
  public synchronized boolean
  isPending()
  {
    return (isStatusPending() || pReservationPending || pOrderPending || pJobSlotsPending ||
	    pSelectionSchedulePending || pSelectionGroupPending || pHardwareGroupPending ||
	    pFavorMethodPending || pDispatchControlPending || pUserBalanceGroupPending);
  }
   


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the short hostname without domain name suffix.
   */ 
  public synchronized String
  getShortName() 
  {
    return pShortName;
  }

  /**
   * Get the current operational status of the host.
   */ 
  public synchronized QueueHostStatus 
  getStatus() 
  {
    return pStatus;
  }

  /**
   * Set the operational status of the host.
   * 
   * @param status
   *   The current operational status of the host.
   */ 
  public synchronized void 
  setStatus
  (
   QueueHostStatus status
  ) 
  {
    pStatus = status;
  }

  /**
   * Whether a change in status is pending.
   */ 
  public synchronized boolean
  isStatusPending()
  {
    switch(pStatus) {
    case Enabling:
    case Disabling:
    case Terminating:
      return true;

    default:
      return false;
    }
  }

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the user who is currently reserving the host. <P> 
   * 
   * When a host is reserved, only jobs submitted by the reserving user will be assigned
   * to the host.
   * 
   * @return 
   *   The name of the reserving user or <CODE>null</CODE> if the host is not reserved.
   */ 
  public synchronized String
  getReservation() 
  {
    return pReservation;
  }

  /**
   * Reserve the host for the given user. <P> 
   * 
   * When a host is reserved, only jobs submitted by the reserving user will be assigned
   * to the host.
   * 
   * @param author
   *   The name of the user who is reserving the host or <CODE>null</CODE> to clear the
   *   the reservation.
   */ 
  public synchronized void
  setReservation
  (
   String author
  ) 
  {
    pReservation = author;
    pReservationPending = true;
  }

  /**
   * Whether a change in reservation is pending.
   */ 
  public synchronized boolean
  isReservationPending()
  {
    return pReservationPending;
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
  getDispatchState()
  {
    return pDispatchState;
  }

  /**
   * Set the dispatch control of the host.
   */
  public void 
  setDispatchState
  (
    EditableState dispatchState
  )
  {
    pDispatchState = dispatchState;
  }

  /*----------------------------------------------------------------------------------------*/

  /**
   * Is the favor method of the host editable.
   */
  public EditableState 
  getFavorState()
  {
    return pFavorState;
  }

  /**
   * Set the favor method state of the host.
   */
  public void 
  setFavorState
  (
    EditableState favorState
  )
  {
    pFavorState = favorState;
  }

  /*----------------------------------------------------------------------------------------*/

  /**
   * Is the user balance group of the host editable.
   */
  public EditableState 
  getUserBalanceState()
  {
    return pUserState;
  }

  /**
   * Set the user balance group state of the host.
   */
  public void 
  setUserBalanceState
  (
    EditableState userState
  )
  {
    pUserState = userState;
  }
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the job dispatching order for this host.
   * 
   * @return 
   *   The dispatch order.
   */ 
  public synchronized int 
  getOrder() 
  {
    return pOrder;
  }

  /**
   * Set the job dispatching order for this host.
   * 
   * @param order
   *   The dispatch order.
   */ 
  public synchronized void 
  setOrder
  (
   int order
  ) 
  {
    pOrder = order;
    pOrderPending = true;
  }

  /**
   * Whether a change in order is pending.
   */ 
  public synchronized boolean
  isOrderPending()
  {
    return pOrderPending;
  }

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the maximum number jobs the host may be assigned.
   * 
   * @return 
   *   The number of job slots.
   */ 
  public synchronized int 
  getJobSlots() 
  {
    return pJobSlots;
  }

  /**
   * Set the maximum number jobs the host may be assigned.
   * 
   * @param slots
   *   The number of job slots.
   */ 
  public synchronized void 
  setJobSlots
  (
   int slots
  ) 
  {
    if(slots < 0) 
      throw new IllegalArgumentException
	("The number of job slots (" + slots + ") cannot be negative!");
    pJobSlots = slots;
    pJobSlotsPending = true;
  }

  /**
   * Whether a change in job slots is pending.
   */ 
  public synchronized boolean
  isJobSlotsPending()
  {
    return pJobSlotsPending;
  }

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the operating system of the host.
   * 
   * @return 
   *   The operating system or <CODE>null</CODE> if unknown.
   */ 
  public synchronized OsType
  getOsType() 
  {
    return pOsType; 
  }

  /**
   * Get the number of processors on the host.
   * 
   * @return 
   *   The number of processors or <CODE>null</CODE> if unknown.
   */ 
  public synchronized Integer 
  getNumProcessors() 
  {
    return pNumProcessors;
  }

  /**
   * Get the total amount of memory (in bytes) on the host.
   * 
   * @return 
   *   The memory size or <CODE>null</CODE> if unknown.
   */ 
  public synchronized Long 
  getTotalMemory() 
  {
    return pTotalMemory;
  }

  /**
   * Get the total amount of temporary disk space (in bytes) on the host.
   * 
   * @return 
   *   The disk size or <CODE>null</CODE> if unknown.
   */ 
  public synchronized Long 
  getTotalDisk() 
  {
    return pTotalDisk;
  }

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the timestamp (milliseconds since midnight, January 1, 1970 UTC) of when all 
   * ramp-up intervals will have expired.
   */ 
  public synchronized long
  getHold() 
  {
    return pHoldTimeStamp;
  }

  /**
   * Get the latest system resource sample.
   * 
   * @return 
   *   The sample or <CODE>null</CODE> if there are no samples.
   */ 
  public synchronized ResourceSample
  getLatestSample() 
  {
    return pSample;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   J O B   S E L E C T I O N                                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the current selection schedule.
   * 
   * @return
   *   The schedule name or <CODE>null</CODE> if the choice of selection group is currently 
   *   manual.
   */ 
  public synchronized String
  getSelectionSchedule() 
  {
    return pSelectionSchedule;
  }

  /**
   * Set the name of the current selection schedule or <CODE>null</CODE> to clear.
   */ 
  public synchronized void
  setSelectionSchedule
  (
   String name
  ) 
  {
    pSelectionSchedule = name;
    pSelectionSchedulePending = true;
  }

  /**
   * Whether a change in selection schedule is pending.
   */ 
  public synchronized boolean
  isSelectionSchedulePending()
  {
    return pSelectionSchedulePending;
  }

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the name of the current selection group. 
   * 
   * @return
   *   The selection group or <CODE>null</CODE> not a member of any selection group.
   */ 
  public synchronized String
  getSelectionGroup() 
  {
    return pSelectionGroup;
  }

  /**
   * Set the name of the current selection group or <CODE>null</CODE> to clear.
   */ 
  public synchronized void
  setSelectionGroup
  (
   String name
  ) 
  {
    pSelectionGroup = name;
    pSelectionGroupPending = true;
  }

  /**
   * Whether a change in selection group is pending.
   */ 
  public synchronized boolean
  isSelectionGroupPending()
  {
    return pSelectionGroupPending;
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the name of the current hardware group. 
   * 
   * @return
   *   The hardware group or <CODE>null</CODE> not a member of any hardware group.
   */ 
  public synchronized String
  getHardwareGroup() 
  {
    return pHardwareGroup;
  }

  /**
   * Set the name of the current hardware group or <CODE>null</CODE> to clear.
   */ 
  public synchronized void
  setHardwareGroup
  (
   String name
  ) 
  {
    pHardwareGroup = name;
    pHardwareGroupPending = true;
  }

  /**
   * Whether a change in hardware group is pending.
   */ 
  public synchronized boolean
  isHardwareGroupPending()
  {
    return pHardwareGroupPending;
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the current favor method. 
   */ 
  public synchronized JobGroupFavorMethod
  getFavorMethod() 
  {
    return pFavorMethod;
  }

  /**
   * Set the favor method.
   * 
   * @throws IllegalArgumentException
   *   If a <code>null</code> value is passed in for favor method.
   */ 
  public synchronized void
  setFavorMethod
  (
    JobGroupFavorMethod favorMethod
  ) 
  {
    if(favorMethod == null) 
      throw new IllegalArgumentException
        ("The job group favor method cannot be (null)!");
    
    pFavorMethod = favorMethod;
    pFavorMethodPending = true;
  }

  /**
   * Whether a change in favor group is pending.
   */ 
  public synchronized boolean
  isFavorMethodPending()
  {
    return pFavorMethodPending;
  }  
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the current user balance group. 
   * 
   * @return
   *   The user balance group or <CODE>null</CODE> not a member of any user balance group.
   */ 
  public synchronized String
  getUserBalanceGroup() 
  {
    return pUserBalanceGroup;
  }

  /**
   * Set the name of the current user balance group or <CODE>null</CODE> to clear.
   */ 
  public synchronized void
  setUserBalanceGroup
  (
   String name
  ) 
  {
    pUserBalanceGroup = name;
    pUserBalanceGroupPending = true;
  }

  /**
   * Whether a change in user balance group is pending.
   */ 
  public synchronized boolean
  isUserBalanceGroupPending()
  {
    return pUserBalanceGroupPending;
  }
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the current dispatch control. 
   * 
   * @return
   *   The dispatch control or <CODE>null</CODE> not a member of any dispatch control.
   */ 
  public synchronized String
  getDispatchControl() 
  {
    return pDispatchControl;
  }

  /**
   * Set the name of the current dispatch control or <CODE>null</CODE> to clear.
   */ 
  public synchronized void
  setDispatchControl
  (
   String name
  ) 
  {
    pDispatchControl = name;
    pDispatchControlPending = true;
  }

  /**
   * Whether a change in dispatch control is pending.
   */ 
  public synchronized boolean
  isDispatchControlPending()
  {
    return pDispatchControlPending;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  @Override
  public void 
  toGlue
  ( 
   GlueEncoder encoder  
  ) 
    throws GlueException
  {
    super.toGlue(encoder); 
    
    if(pReservation != null) 
      encoder.encode("Reservation", pReservation);
     
    encoder.encode("Order", pOrder);
    encoder.encode("JobSlots", pJobSlots);

    encoder.encode("SelectionSchedule", pSelectionSchedule);
    encoder.encode("SelectionGroup", pSelectionGroup);
    encoder.encode("HardwareGroup", pHardwareGroup);
    encoder.encode("FavorMethod", pFavorMethod);
    encoder.encode("DispatchControl", pDispatchControl);
    encoder.encode("UserBalanceGroup", pUserBalanceGroup);
  }

  @Override
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    initShortName();

    String author = (String) decoder.decode("Reservation"); 
    if(author != null) 
      pReservation = author;

    Integer order = (Integer) decoder.decode("Order"); 
    if(order == null) 
      throw new GlueException("The \"Order\" was missing!");
    pOrder = order;

    Integer slots = (Integer) decoder.decode("JobSlots"); 
    if(slots == null) 
      throw new GlueException("The \"JobSlots\" was missing!");
    pJobSlots = slots;

    /* Allows null values since this was added in after QHI's already existed.  The default 
     * constructor method guarantees that there is a suitable default value.
     */
    JobGroupFavorMethod favorMethod = (JobGroupFavorMethod) decoder.decode("FavorMethod");
    if (favorMethod != null)
      pFavorMethod = favorMethod;

    pSelectionSchedule = (String) decoder.decode("SelectionSchedule"); 
    pSelectionGroup    = (String) decoder.decode("SelectionGroup");
    pHardwareGroup     = (String) decoder.decode("HardwareGroup");
    pDispatchControl   = (String) decoder.decode("DispatchControl");
    pUserBalanceGroup  = (String) decoder.decode("UserBalanceGroup");
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3815152450621478990L;

  /**
   * A regular expressions used to determine if a hostname is numeric and to match the 
   * first component (short name) of a non-numeric hostname.
   */ 
  private static final Pattern sNumericHostPattern = 
    Pattern.compile("([0-9])+\\.([0-9])+\\.([0-9])+\\.([0-9])+");

  private static final Pattern sShortHostPattern = 
    Pattern.compile("([^\\.])+");



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The short hostname without domain name suffix.
   */
  private String  pShortName; 
  
  /**
   * The current operational status of the host.
   */ 
  private QueueHostStatus  pStatus;

  /**
   * The name of the reserving user or <CODE>null</CODE> if the host is not reserved.
   */ 
  private String  pReservation;
  private boolean pReservationPending;

  /**
   * The order in which job servers are processed by the dispatcher. 
   */ 
  private int     pOrder; 
  private boolean pOrderPending; 

  /**
   * The maximum number jobs the host may be assigned.
   */ 
  private int     pJobSlots; 
  private boolean pJobSlotsPending; 

  /*----------------------------------------------------------------------------------------*/

  /**
   * The operating system type.
   */ 
  private OsType  pOsType; 

  /**
   * The number of processors on the host.
   */ 
  private Integer  pNumProcessors; 

  /**
   * The total amount of memory (in bytes) on the host.
   */ 
  private Long  pTotalMemory;

  /**
   * The total amount of temporary disk space (in bytes) on the host.
   */ 
  private Long  pTotalDisk;

  /*----------------------------------------------------------------------------------------*/

  /**
   * The timestamp (milliseconds since midnight, January 1, 1970 UTC) of when all ramp-up 
   * intervals will have expired.
   */ 
  private long pHoldTimeStamp;

  /**
   * The latest resource usage sample or <CODE>null</CODE> if no samples exist.
   */ 
  private ResourceSample  pSample;

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The name of the current selection group or <CODE>null</CODE> not a member of any 
   * selection group. 
   */ 
  private String  pSelectionGroup; 
  private boolean pSelectionGroupPending; 

  /**
   * The name of the current selection schedule or <CODE>null</CODE> if the choice of 
   * selection group is currently manual.
   */ 
  private String  pSelectionSchedule; 
  private boolean pSelectionSchedulePending;
  
  /**
   * The name of the current hardware group or <CODE>null</CODE> not a member of any 
   * hardware group. 
   */ 
  private String  pHardwareGroup; 
  private boolean pHardwareGroupPending;
  
  /**
   * The name of the current dispatch control or <code>null</code> if no dispatch control
   * is assigned to this host. 
   */
  private String  pDispatchControl;
  private boolean pDispatchControlPending;
  
  /**
   * The name of the current user balance group or <CODE>null</CODE> not a member of any 
   * user balance group. 
   */
  private String  pUserBalanceGroup;
  private boolean pUserBalanceGroupPending;
  
  /**
   * The favor method of the host.
   */
  private JobGroupFavorMethod pFavorMethod;
  private boolean             pFavorMethodPending;

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
   */
  private EditableState pDispatchState;

  /**
   * Is the favor group of the machine editable?
   */
  private EditableState pFavorState;
 
  /**
   * Is the user balance group of the machine editable?
   */
  private EditableState pUserState;
}