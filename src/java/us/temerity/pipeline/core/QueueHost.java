// $Id: QueueHost.java,v 1.5 2006/11/21 20:00:04 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

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
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the instance.
   */ 
  private void 
  init() 
  {
    pStatus       = Status.Shutdown;
    pLastModified = new Date();

    pHoldTimeStamps = new TreeMap<Long,Date>();
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
      
    case Hung:
      return QueueHostStatus.Hung;

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
  public synchronized void
  setStatus
  (
   Status status
  ) 
  {
    pStatus       = status;
    pLastModified = new Date();

    switch(pStatus) {
    case Shutdown:
      pSample  = null; 
      pNumJobs = null;
      break;

    case Hung:
      pLastHung = pLastModified;
    }
  }

  /**
   * Get the timestamp of when the status was last modified.
   */ 
  public synchronized Date
  getLastModified()
  {
    return pLastModified;
  }

  /**
   * Get the timestamp of when the status was last changed to Hung or <CODE>null</CODE> if 
   * the state has never been Hung since the server was started.
   */ 
  public synchronized Date
  getLastHung()
  {
    return pLastHung; 
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
   * Get the timestamp of when all ramp-up intervals will have expired.
   */ 
  public synchronized Date
  getHold() 
  {
    Date latest = new Date(0L);
    for(Date stamp : pHoldTimeStamps.values()) {
      if(stamp.compareTo(latest) > 0)
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
    if(interval > 0) {
      Date stamp = new Date(Dates.now().getTime() + interval*1000L);
      pHoldTimeStamps.put(jobID, stamp);
    }
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

    if(pNumJobs == null) 
      pNumJobs = pSample.getNumJobs();
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Increment the number of running jobs.
   */ 
  public synchronized void 
  jobStarted()
  {
    if((pNumJobs == null) || (pNumJobs < 0))
      throw new IllegalStateException("Illegal job count (" + pNumJobs + ")!");
    pNumJobs++;

    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Finest,
       "Job Started [" + getName() + "]:  Jobs = " + pNumJobs);
    LogMgr.getInstance().flush();
  }

  /**
   * Decrement the number of running jobs.
   */ 
  public synchronized void 
  jobFinished()
  {
    if(pNumJobs != null) {
      pNumJobs--;

      if(pNumJobs < 0)
	throw new IllegalStateException("Illegal job count (" + pNumJobs + ")!");
    }

    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Finest,
       "Job Finished [" + getName() + "]:  Jobs = " + pNumJobs);
    LogMgr.getInstance().flush();
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
    ResourceSample sample = getLatestSample();
    if(sample == null) {
      LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Finest,
       "Available Slots [" + getName() + "]:  No Samples Yet.  Jobs = " + pNumJobs);      

      return 0;
    }

    Date now = Dates.now();
    if(((now.getTime() - sample.getTimeStamp().getTime()) > PackageInfo.sCollectorInterval) ||
       (getHold().compareTo(now) > 0)) {
      LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Finest,
       "Available Slots [" + getName() + "]:  On Hold.  Jobs = " + pNumJobs);      

      return 0;
    }
    
    if(pNumJobs == null) 
      throw new IllegalStateException("Illegal job count!"); 

    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Finest,
       "Available Slots [" + getName() + "]:  " + 
       "Jobs = " + pNumJobs + "  " + 
       "Slots = " + pJobSlots + "  " + 
       "Free = " + (pJobSlots - pNumJobs));
    LogMgr.getInstance().flush();

    return Math.max(pJobSlots - pNumJobs, 0);
  }

  /**
   * Determine whether a job with the given job requirements is eligible to run on this 
   * host. <P> 
   * 
   * This method will return <CODE>false</CODE> under the following conditions: <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   The host is reserved for another user. <P>
   *   The host has a higher system load than the job requires.<P> 
   *   The host has less than the required amount of free memory.<P>
   *   The host has less than the required amount of free temporary disk space.<P>
   * </DIV>
   * 
   * @param author
   *   The name of the user submitting the job.
   * 
   * @param privs
   *   The current admin privileges. 
   * 
   * @param jreqs
   *   The requirements that this host must meet in order to be eligable to run the job. 
   * 
   * @return 
   *   The combined selection bias or <CODE>null</CODE> if the host fails the requirements.
   */ 
  public synchronized boolean
  isEligible
  (
   String author, 
   AdminPrivileges privs,
   JobReqs jreqs
  )
  {
    if((pReservation != null) &&
       !(author.equals(pReservation) || privs.isWorkGroupMember(author, pReservation)))
      return false;

    ResourceSample sample = getLatestSample();
    if(sample == null) 
      return false;

    if((sample.getLoad() > jreqs.getMaxLoad()) ||
       (sample.getMemory() < jreqs.getMinMemory()) || 
       (sample.getDisk() < jreqs.getMinDisk()))
      return false;

    return true;
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
    return new QueueHostInfo(pName, getInfoStatus(), pReservation, pOrder, pJobSlots, 
			     pOsType, pNumProcessors, pTotalMemory, pTotalDisk, 
			     getHold(), pSample, pSelectionGroup, pSelectionSchedule); 
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
  /*   P U B L I C   C L A S S E S                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The operational status of the host.
   */ 
  public 
  enum Status 
  {
    /**
     * A <B>pljobmgr</B>(1) daemon is currently running on the host and is available to 
     * run new jobs which meet the selection criteria for the host.
     */ 
    Enabled, 

    /**
     * A <B>pljobmgr</B>(1) daemon is currently running on the host, but the host has 
     * been temporarily disabled. <P> 
     * 
     * Jobs previously assigned to the host may continue running until they complete, but no 
     * new jobs will be assigned to this host.  The host will respond to requests to kill 
     * jobs currently running on the host. 
     */ 
    Disabled, 

    /**
     * No <B>pljobmgr</B>(1) daemon is currently running on the host.  <P> 
     * 
     * No jobs will be assigned to this host until the <B>pljobmgr</B>(1) daemon is restarted.
     */ 
    Shutdown, 

    /**
     * A <B>pljobmgr</B>(1) daemon is currently running on the host, but is not responding
     * to network connections from clients. <P> 
     * 
     * This is probably an indication that something has gone wrong with daemon and it 
     * should be killed and restarted.  No jobs will be assigned to this host while it is
     * in this state.
     */ 
    Hung;


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



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5965011973074654660L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The current operational status of the host.
   */ 
  private Status  pStatus;

  /**
   * The timestamp of when the status was last modified.
   */ 
  private Date  pLastModified; 

  /**
   * The timestamp of when the status was last changed to Hung or <CODE>null</CODE> if 
   * the state has never been Hung since the server was started.
   */ 
  private Date  pLastHung; 

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
  private TreeMap<Long,Date>  pHoldTimeStamps;

  /**
   * The lastest resource usage sample or <CODE>null</CODE> if no samples exist.
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

}
