// $Id: QueueHostInfo.java,v 1.1 2006/07/02 00:27:49 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   H O S T   I N F O                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Read-only information about the current and/or pending status of a job server host. 
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
    pHoldTimeStamp = new Date(0L); 
    pSamples = new ArrayList<ResourceSample>();
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
   *   The name of the reserving user 
   *   or <CODE>null</CODE> if the host is not reserved.
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
   *   The number of processors on the host 
   *   or <CODE>null</CODE> if unknown.
   * 
   * @param totalMem
   *   The total amount of memory (in bytes) on the host 
   *   or <CODE>null</CODE> if unknown.
   * 
   * @param totalDisk
   *   The total amount of temporary disk space (in bytes) on the host 
   *   or <CODE>null</CODE> if unknown.
   * 
   * @param hold 
   *   The timestamp of when all ramp-up intervals will have expired.
   * 
   * @param samples
   *   The system resource usage samples (newest to oldest).
   * 
   * @param schedule
   *   The name of the current selection schedule 
   *   or <CODE>null</CODE> if the choice of selection group is currently manual.
   * 
   * @param group
   *   The name of the current selection group 
   *   or <CODE>null</CODE> not a member of any selection group. 
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
   Date hold, 
   Collection<ResourceSample> samples, 
   String schedule, 
   String group
  ) 
  {
    super(name);

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
      pHoldTimeStamp = new Date(0L);
    else 
      pHoldTimeStamp = hold; 

    if(samples == null) 
      pSamples = new ArrayList<ResourceSample>();
    else 
      pSamples = new ArrayList<ResourceSample>(samples);

    pSelectionSchedule = schedule; 
    pSelectionGroup = group; 
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether a change in reservation is pending.
   */ 
  public boolean
  isPending()
  {
    return (isStatusPending() || pReservationPending || pOrderPending || pJobSlotsPending ||
	    pSelectionSchedulePending || pSelectionGroupPending);
  }
   


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current operational status of the host.
   */ 
  public QueueHostStatus 
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
  public void 
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
  public boolean
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
  public String
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
  public void
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
  public boolean
  isReservationPending()
  {
    return pReservationPending;
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the job dispatching order for this host.
   * 
   * @return 
   *   The dispatch order.
   */ 
  public int 
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
  public void 
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
  public boolean
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
  public int 
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
  public void 
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
  public boolean
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
  public OsType
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
  public Integer 
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
  public Long 
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
  public Long 
  getTotalDisk() 
  {
    return pTotalDisk;
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the timestamp of when all ramp-up intervals will have expired.
   */ 
  public Date
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
  public ResourceSample
  getLatestSample() 
  {
    if(!pSamples.isEmpty()) 
      return pSamples.get(0);
    return null;
  }

  /**
   * Get all of the system resource samples within the sample window.
   * 
   * @return 
   *   The sample or <CODE>null</CODE> if there are no samples.
   */ 
  public List<ResourceSample> 
  getSamples() 
  {
    return Collections.unmodifiableList(pSamples);
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
  public String
  getSelectionSchedule() 
  {
    return pSelectionSchedule;
  }

  /**
   * Set the name of the current selection schedule or <CODE>null</CODE> to clear.
   */ 
  public void
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
  public boolean
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
  public String
  getSelectionGroup() 
  {
    return pSelectionGroup;
  }

  /**
   * Set the name of the current selection group or <CODE>null</CODE> to clear.
   */ 
  public void
  setSelectionGroup
  (
   String name
  ) 
  {
    pSelectionGroup = name;
    pSelectionGroupPending = true;
  }

  /**
   * Whether a change in selection schedule is pending.
   */ 
  public boolean
  isSelectionGroupPending()
  {
    return pSelectionGroupPending;
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
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
  }

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

    pSelectionSchedule = (String) decoder.decode("SelectionSchedule"); 
    pSelectionGroup    = (String) decoder.decode("SelectionGroup"); 
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3815152450621478990L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
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
   * The timestamp of when all ramp-up intervals will have expired.
   */ 
  private Date pHoldTimeStamp;

  /**
   * The system resource usage samples (newest to oldest).
   */ 
  private ArrayList<ResourceSample>  pSamples;


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The name of the current selection schedule or <CODE>null</CODE> if the choice of 
   * selection group is currently manual.
   */ 
  private String  pSelectionSchedule; 
  private boolean pSelectionSchedulePending; 

  /**
   * The name of the current selection group or <CODE>null</CODE> not a member of any 
   * selection group. 
   */ 
  private String  pSelectionGroup; 
  private boolean pSelectionGroupPending; 

}
