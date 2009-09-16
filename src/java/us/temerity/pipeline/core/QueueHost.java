// $Id: QueueHost.java,v 1.13 2009/09/16 03:54:40 jesse Exp $

package us.temerity.pipeline.core;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   H O S T                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Information about the available resources and job selection biases for a host which 
 * is capable of executing jobs on behalf of the Pipeline queue.
 */
public
class QueueHost
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
  QueueHost()
  { 
    init();
  }

  /**
   * Construct a new queue host.
   * 
   * @param name
   *   The fully resolved name of the host.
   */ 
  public
  QueueHost
  (
   String name   
  ) 
  {
    super(name);
    init();
  }

  /**
   * Construct from queue host information.
   * 
   * @param qinfo
   *   The queue host information data.
   */ 
  public
  QueueHost
  (
   QueueHostInfo qinfo
  ) 
  {
    super(qinfo.getName());
    init();

    pReservation = qinfo.getReservation();
    pOrder       = qinfo.getOrder();
    pJobSlots    = qinfo.getJobSlots();

    pSelectionSchedule = qinfo.getSelectionSchedule();
    pSelectionGroup    = qinfo.getSelectionGroup();
    pHardwareGroup     = qinfo.getHardwareGroup();
    pFavorMethod       = qinfo.getFavorMethod();
    pUserBalanceGroup  = qinfo.getUserBalanceGroup();
    pDispatchControl   = qinfo.getDispatchControl();
    
    pSlotState = qinfo.getSlotsState();
    pStatusState = qinfo.getStatusState();
    pOrderState = qinfo.getOrderState();
    pReservationState = qinfo.getReservationState();
    pGroupState = qinfo.getGroupState();
    pDispatchState = qinfo.getDispatchState();
    pFavorState = qinfo.getFavorState();
    pUserState = qinfo.getUserBalanceState();
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the instance.
   */ 
  private void 
  init() 
  {
    pStatus       = Status.Shutdown;
    pLastModified = System.currentTimeMillis(); 

    pHoldTimeStamps = new TreeMap<Long,Long>();
    
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


   
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current operational status of the host (QueueHostInfo format).
   */ 
  public synchronized QueueHostStatus 
  getInfoStatus() 
  {
    switch(pStatus) {
    case Enabled:
      return QueueHostStatus.Enabled;

    case Disabled:
      return QueueHostStatus.Disabled;

    case Shutdown:
      return QueueHostStatus.Shutdown;
      
    case Limbo:
      return QueueHostStatus.Limbo;

    default:
      throw new IllegalStateException();
    }
  }

  /**
   * Get the current operational status of the host.
   */ 
  public synchronized Status 
  getStatus() 
  {
    return pStatus;
  }

  /**
   * Set the current operational status of the host.
   */ 
  @SuppressWarnings("incomplete-switch")
  public synchronized void
  setStatus
  (
   Status status
  ) 
  {
    pStatus       = status;
    pLastModified = System.currentTimeMillis();

    switch(pStatus) {
    case Shutdown:
      pSample  = null; 
      pNumJobs = null;
      break;

    case Limbo:
      pLastLimbo = pLastModified;
    }
  }

  /**
   * Get the timestamp (milliseconds since midnight, January 1, 1970 UTC) of when the 
   * status was last modified.
   */ 
  public synchronized long
  getLastModified()
  {
    return pLastModified;
  }

  /**
   * Get the timestamp (milliseconds since midnight, January 1, 1970 UTC) of when the 
   * status was last changed to Limbo or <CODE>null</CODE> if the state has never been Limbo 
   * since the server was started.
   */ 
  public synchronized Long
  getLastLimbo()
  {
    return pLastLimbo; 
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
  }
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Is the reservation status of the host editable.
   */
  public synchronized EditableState 
  getReservationState()
  {
    return pReservationState;
  }


  /**
   * Sets whether the reservation status of the host is editable.
   */
  public synchronized void 
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
  public synchronized EditableState 
  getStatusState()
  {
    return pStatusState;
  }

  /**
   * Set whether the status of the host is editable.
   */
  public synchronized void 
  setStatusState
  (
    EditableState statusState
  )
  {
    pStatusState = statusState;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Is the order of the host editable.
   */
  public synchronized EditableState 
  getOrderState()
  {
    return pOrderState;
  }

  /**
   * Set whether the order of the host is editable.
   */
  public synchronized void 
  setOrderState
  (
    EditableState orderState
  )
  {
    pOrderState = orderState;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Is the number of slots on the host editable.
   */
  public synchronized EditableState 
  getSlotsState()
  {
    return pSlotState;
  }

  /**
   * Set whether the status of the host is editable.
   */
  public synchronized void 
  setSlotsState
  (
    EditableState slotState
  )
  {
    pSlotState = slotState;
  }
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Is the selection group on the host editable.
   */
  public synchronized EditableState 
  getGroupState()
  {
    return pGroupState;
  }

  /**
   * Set whether the status of the host is editable.
   */
  public synchronized void 
  setGroupState
  (
    EditableState groupState
  )
  {
    pGroupState = groupState;
  }

  /*----------------------------------------------------------------------------------------*/

  /**
   * Is the dispatch control on the host editable.
   */
  public synchronized EditableState 
  getDispatchState()
  {
    return pDispatchState;
  }

  /**
   * Set whether the dispatch control of the host is editable.
   */
  public synchronized void 
  setDispatchState
  (
    EditableState dispatchState
  )
  {
    pDispatchState = dispatchState;
  }

  /*----------------------------------------------------------------------------------------*/

  /**
   * Is the user balance group on the host editable.
   */
  public synchronized EditableState 
  getUserBalanceState()
  {
    return pUserState;
  }

  /**
   * Set whether the user balance state of the host is editable.
   */
  public synchronized void 
  setUserBalanceState
  (
    EditableState userState
  )
  {
    pUserState = userState;
  }
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Is the favor method on the host editable.
   */
  public synchronized EditableState 
  getFavorState()
  {
    return pFavorState;
  }

  /**
   * Set whether the favor method of the host is editable.
   */
  public synchronized void 
  setFavorState
  (
    EditableState favorState
  )
  {
    pFavorState = favorState;
  }
  
  /*----------------------------------------------------------------------------------------*/


  /**
   * Get job dispatching order for this host.
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
   * Set job dispatching order for this host.
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
   * Set the operating system of the host.
   * 
   * @param os 
   *   The operating system.
   */ 
  public synchronized void
  setOsType
  (
   OsType os
  ) 
  {
    pOsType = os;
  }


  /*----------------------------------------------------------------------------------------*/

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
   * Set the number of processors on the host.
   * 
   * @param procs 
   *   The number of processors.
   */ 
  public synchronized void
  setNumProcessors
  (
   Integer procs
  ) 
  {
    pNumProcessors = procs; 
  }


  /*----------------------------------------------------------------------------------------*/

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
   * Set the total amount of memory (in bytes) on the host.
   * 
   * @param memory 
   *   The memory size.
   */ 
  public synchronized void
  setTotalMemory
  (
   Long memory
  ) 
  {
    pTotalMemory = memory;
  }


  /*----------------------------------------------------------------------------------------*/
  
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

  /**
   * Set the total amount of temporary disk space (in bytes) on the host.
   * 
   * @param disk 
   *   The disk size.
   */ 
  public synchronized void
  setTotalDisk
  (
   Long disk
  ) 
  {
    pTotalDisk = disk;
  }



  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the timestamp (milliseconds since midnight, January 1, 1970 UTC) of when all 
   * ramp-up intervals will have expired.
   */ 
  public synchronized long
  getHold() 
  {
    long latest = 0L; 
    for(Long stamp : pHoldTimeStamps.values()) {
      if(stamp > latest)
	latest = stamp;
    }
    return latest;
  }

  /**
   * Get the 
   */ 
  public synchronized Set<Long> 
  getHeldJobIDs() 
  {
    return Collections.unmodifiableSet(pHoldTimeStamps.keySet());
  }

  /**
   * Update the hold timestamp for the given job based on the job's ramp-up interval.
   * 
   * @param jobID
   *   The unique job identifier.
   * 
   * @param interval
   *   The ramp-up interval (in seconds).
   */ 
  public synchronized void 
  setHold
  (
   long jobID, 
   int interval
  ) 
  {
    if(interval > 0) 
      pHoldTimeStamps.put(jobID, TimeStamps.now() + interval*1000L); 
  }

  /**
   * Cancel the hold for the given completed job.
   * 
   * @param jobID
   *   The unique job identifier.
   */ 
  public synchronized void 
  cancelHold
  (
   long jobID
  ) 
  {
    pHoldTimeStamps.remove(jobID);
  }
   
  /**
   * Cancel all holds.
   */ 
  public synchronized void 
  cancelHolds() 
  {
    pHoldTimeStamps.clear();
  }
   

  /*----------------------------------------------------------------------------------------*/

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

  /**
   * Set the latest system resource usage sample.
   * 
   * @param sample
   *   The sample of system resources.
   */ 
  public synchronized void 
  setLatestSample
  (
   ResourceSample sample
  ) 
  {
    pSample = sample; 
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
  }

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Set the count of currently running jobs on the host.<P> 
   * 
   * This method is called by the dispatcher each cycle which determines the number of 
   * running jobs by counting the number of MonitorTask threads registered to each host.
   * Since only a single job is dispatched to a host each cycle, keeping this internal
   * count will always be high enough to prevent too many jobs from being started while
   * reducing lock contention on the table of MonitorTask threads.
   */ 
  public synchronized void 
  setRunningJobs
  (
   int numJobs
  ) 
  {
    pNumJobs = numJobs;
  }

  /** 
   * Get the number of slots currently available. <P> 
   * 
   * This method may return zero, even when unused slots exist for the following reasons: <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   There are no system resource samples newer than the sample interval.<P>
   *   If the server is currently on hold due to job run-up. <P>
   * </DIV>
   */
  public synchronized int 
  getAvailableSlots() 
  {
    LogMgr lmgr = LogMgr.getInstance();

    ResourceSample sample = getLatestSample();
    boolean opsFinest = lmgr.isLoggable(LogMgr.Kind.Ops, LogMgr.Level.Finest);
    if(sample == null) {
      if(opsFinest) {
        lmgr.log(LogMgr.Kind.Ops, LogMgr.Level.Finest,
                 "Available Slots [" + getName() + "]:  No Samples Yet.  " + 
                 "Jobs = " + pNumJobs);      
      }

      return 0;
    }

    long now = TimeStamps.now();
    if((now - sample.getTimeStamp()) > PackageInfo.sCollectorInterval) {
      if(opsFinest) {
        lmgr.log(LogMgr.Kind.Ops, LogMgr.Level.Finest,
                 "Available Slots [" + getName() + "]:  Old Sample.  " + 
                 "Jobs = " + pNumJobs);      
      }

      return 0;
    }
    
    if(getHold() > now) { 
      if(opsFinest) {
        lmgr.log(LogMgr.Kind.Ops, LogMgr.Level.Finest,
                 "Available Slots [" + getName() + "]:  On Hold.  " + 
                 "Jobs = " + pNumJobs);  
      }

      return 0;
    }
    
    if(pNumJobs == null) { 
      lmgr.log(LogMgr.Kind.Ops, LogMgr.Level.Warning,
               "Available Slots [" + getName() + "]:  Unknown!"); 
      return 0; 
    }

    if(opsFinest) {
      lmgr.logAndFlush(LogMgr.Kind.Ops, LogMgr.Level.Finest,
                       "Available Slots [" + getName() + "]:  " + 
                       "Jobs = " + pNumJobs + "  " + 
                       "Slots = " + pJobSlots + "  " + 
                       "Free = " + (pJobSlots - pNumJobs));
    }

    return Math.max(pJobSlots - pNumJobs, 0);
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   C O N V E R S I O N                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Return a read-only form of information about the current status of a job server host.
   */ 
  public synchronized QueueHostInfo
  toInfo() 
  {
    return new QueueHostInfo
      (pName, getInfoStatus(), pReservation, pOrder, pJobSlots, 
       pOsType, pNumProcessors, pTotalMemory, pTotalDisk, 
       getHold(), pSample, pDispatchControl, pSelectionGroup, pSelectionSchedule, 
       pHardwareGroup, pUserBalanceGroup, pFavorMethod, pGroupState, pStatusState, 
       pReservationState, pOrderState, pSlotState, pDispatchState, pUserState, pFavorState); 
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
  /*   P U B L I C   C L A S S E S                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The operational status of the host.
   */ 
  public 
  enum Status 
  {
    /**
     * Communication is currenty established with the Job Manager running on the host and 
     * is available to run any new jobs which meet the selection criteria for the host.
     */ 
    Enabled, 

    /**
     * Communication is currenty established with the Job Manager running on the host but
     * it has been temporarily disabled from starting new jobs. <P> 
     * 
     * Jobs previously assigned to the host may continue running until they complete, but no 
     * new jobs will be assigned to this host.  The host will respond to requests to kill 
     * jobs currently running on the host. 
     */ 
    Disabled, 

    /**
     * There is no communication currently established with the Job Manager on this host 
     * nor will any effort be made to reestablish communication.<P> 
     * 
     * Note that this state only indicates that the Queue Manager will ignore this host
     * and not whether a Job Manager is actually running on the host.
     */ 
    Shutdown, 

    /**
     * Communication with the Job Manager has unexpectedly been lost and it has become
     * unresponsive to requests for network communication. <P> 
     * 
     * The Queue Manager will periodically attempt to restore communication and if successful
     * the Job Manager will return to an Enabled state.  Users may also manually attempt
     * to restore contact by Enabling the host. <P> 
     * 
     * Note that there are a variety of reasons for while a Job Manager may become Unknown
     * including: <P> 
     * <DIV style="margin-left: 40px;">
     *   The host is not powered on. <P> 
     *   There is some network communication failure or misconfiguration.<P> 
     *   The host operating system has gone down or is under extremely high load.<P> 
     *   The Job Manager has been killed and has not been restarted.<P> 
     * </DIV>
     */ 
    Limbo;


    /**
     * Get the list of all possible values.
     */ 
    public static ArrayList<Status>
    all() 
    {
      Status values[] = values();
      ArrayList<Status> all = new ArrayList<Status>(values.length);
      int wk;
      for(wk=0; wk<values.length; wk++)
	all.add(values[wk]);
      return all;
    }
    
    /**
     * Get the list of human friendly string representation for all possible values.
     */ 
    public static ArrayList<String>
    titles() 
    {
      ArrayList<String> titles = new ArrayList<String>();
      for(Status status : Status.all()) 
	titles.add(status.toString());
      return titles;
    }
  }
  
  private static final long serialVersionUID = -5965011973074654660L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The current operational status of the host.
   */ 
  private Status  pStatus;

  /**
   * The timestamp (milliseconds since midnight, January 1, 1970 UTC) of when the status 
   * was last modified.
   */ 
  private long  pLastModified; 

  /**
   * The timestamp (milliseconds since midnight, January 1, 1970 UTC) of when the status 
   * was last changed to Limbo or <CODE>null</CODE> if the state has never been Limbo since 
   * the server was started.
   */ 
  private Long  pLastLimbo; 

  /**
   * The name of the reserving user or <CODE>null</CODE> if the host is not reserved.
   */ 
  private String  pReservation;

  /**
   * The order in which job servers are processed by the dispatcher. 
   */ 
  private int  pOrder; 

  /**
   * The maximum number jobs the host may be assigned.
   */ 
  private int  pJobSlots; 
  

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
   * The timestamps of when the ramp-up intervals for jobs assigned to this host will 
   * have completed indexed by job ID.
   */ 
  private TreeMap<Long,Long>  pHoldTimeStamps;

  /**
   * The latest resource usage sample or <CODE>null</CODE> if no samples exist.
   */ 
  private ResourceSample  pSample;

  /**
   * The number of job currently running on the host or <CODE>null</CODE> if unknown.
   */ 
  private Integer  pNumJobs;


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The name of the current selection schedule or <CODE>null</CODE> if the choice of 
   * selection group is currently manual.
   */ 
  private String pSelectionSchedule; 

  /**
   * The name of the current selection group or <CODE>null</CODE> not a member of any 
   * selection group. 
   */ 
  private String pSelectionGroup;
  
  /**
   * The name of the current hardware group or <CODE>null</CODE> not a member of any 
   * hardware group. 
   */ 
  private String pHardwareGroup;
  
  /**
   * The job group favor method.
   */  
  private JobGroupFavorMethod  pFavorMethod;
  
  /**
   * The name of the current user balance group or <CODE>null</CODE> not a member of any 
   * user balance group. 
   */
  private String  pUserBalanceGroup;
  
  /**
   * The name of the current dispatch control or <code>null</code> if no dispatch control
   * is assigned to this host. 
   */
  private String  pDispatchControl;
  
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
   * Is the order of the machine editable?
   * <p>
   * This is being set by the scheduler when the schedule on the machine is being applied. 
   */
  private EditableState pOrderState;
  
  /**
   * Is the number of slots on the machine editable?
   * <p>
   * This is being set by the scheduler when the schedule on the machine is being applied. 
   */
  private EditableState pSlotState;
  
  /**
   * Is the selection group on the machine editable?
   * <p>
   * This is being set by the scheduler when the schedule on the machine is being applied. 
   */
  private EditableState pGroupState;
  
  /**
   * Is the dispatch control on the machine editable?
   * <p>
   * This is being set by the scheduler when the schedule on the machine is being applied. 
   */
  private EditableState pDispatchState;
  
  
  /**
   * Is the job group favor method on the machine editable?
   * <p>
   * This is being set by the scheduler when the schedule on the machine is being applied. 
   */
  private EditableState pFavorState;
  
  
  /**
   * Is the user balance group on the machine editable?
   * <p>
   * This is being set by the scheduler when the schedule on the machine is being applied. 
   */
  private EditableState pUserState;
}
