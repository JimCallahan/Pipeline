// $Id: QueueHost.java,v 1.4 2004/10/16 23:32:31 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.util.logging.*;
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


  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the instance.
   */ 
  private void 
  init() 
  {
    pStatus = Status.Shutdown;

    pSamples = new LinkedList<ResourceSample>();
    
    pSelectionBiases = new TreeMap<String,Integer>();
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current operational status of the host.
   */ 
  public Status 
  getStatus() 
  {
    return pStatus;
  }

  /**
   * Set the current operational status of the host.
   */ 
  public void
  setStatus
  (
   Status status
  ) 
  {
    pStatus = status;

    switch(pStatus) {
    case Shutdown:
    case Disabled:
      pSamples.clear();
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
  }
  

  /*----------------------------------------------------------------------------------------*/

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
   * Set the the number of processors on the host.
   * 
   * @param procs 
   *   The number of processors.
   */ 
  public void
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
  public Long 
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
  public void
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
  public Long 
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
  public void
  setTotalDisk
  (
   Long disk
  ) 
  {
    pTotalDisk = disk;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the maximum interval of time (in milliseconds) that system resource samples 
   * are retained.
   */
  public static long
  getSampleInterval()
  {
    return sSampleInterval;
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
      return pSamples.getFirst();
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
    pruneSamples();
    return Collections.unmodifiableList(pSamples);
  }
  
  /**
   * Add a system resource usage sample.
   * 
   * @param sample
   *   The sample of system resources.
   */ 
  public void 
  addSample
  (
   ResourceSample sample
  ) 
  {
    assert(pSamples.isEmpty() || 
	   (pSamples.getFirst().getTimeStamp().compareTo(sample.getTimeStamp()) < 0));

    pSamples.addFirst(sample);
  }

  /**
   * Remove all samples older than the sample interval.
   */ 
  private void
  pruneSamples()
  {
    long start = 0;
    {
      ResourceSample sample = getLatestSample();
      if(sample == null) 
	return;
      start = sample.getTimeStamp().getTime();
    }

    boolean strip = false;
    Iterator<ResourceSample> iter = pSamples.listIterator(0); 
    while(iter.hasNext()) {
      ResourceSample sample = iter.next();
      if(!strip && ((start - sample.getTimeStamp().getTime()) > sSampleInterval))
	strip = true;

      if(strip) 
	iter.remove();
    }
  }


  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the selection keys for the host.
   */
  public Set<String>
  getSelectionKeys()
  {
    return Collections.unmodifiableSet(pSelectionBiases.keySet());
  }

  /**
   * Get the bias for the given selection key.
   * 
   * @param key
   *   The name of the selection key.
   * 
   * @return 
   *   The bias for the given key or <CODE>null</CODE> if the key is not defined.
   */ 
  public Integer
  getSelectionBias
  (
   String key
  ) 
  {
    return pSelectionBiases.get(key);
  }
  
  /**
   * Add (or replace) the bias for the given selection key.
   * 
   * @param key
   *   The name of the selection key.
   * 
   * @param bias 
   *   The selection bias for the key: [-100,100]
   */ 
  public void 
  addSelectionKey
  (
   String key, 
   int bias
  ) 
  {
    if((bias < -100) || (bias > 100)) 
      throw new IllegalArgumentException
	("The selection bias (" + bias + ") must be in the range: [-100,100]!");
    pSelectionBiases.put(key, bias);
  }
  
  /** 
   * Remove the selection key and bias for the named key.
   *
   * @param key 
   *    The name of the selection key to remove.
   */
  public void
  removeSelectionKey
  (
   String key
  ) 
  {
    pSelectionBiases.remove(key);
  }
  
  /** 
   * Remove all selection keys.
    */
  public void
  removeAllSelectionKeys() 
  {
    pSelectionBiases.clear();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   J O B   S E L E C T I O N                                                            */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Get the combined bias for a job with the given job requirements. <P> 
   * 
   * This method will return <CODE>null</CODE> under the following conditions: <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   The host is reserved for another user.
   *   All of the slots are already filled by running jobs.<P>
   *   There are no system resource samples newer than the sample interval.<P>
   *   The host is unable to provide the system resourses specified by the job 
   *   requirements. <P>
   *   The host does not support all of the selection keys required.<P>
   * 
   * @param author
   *   The name of the user submitting the job.
   * 
   * @param jreqs
   *   The requirements that this host must meet in order to be eligable to run the job. 
   * 
   * @param keys 
   *   The names of the valid selection keys.
   * 
   * @return 
   *   The combined selection bias or <CODE>null</CODE> if the host fails the requirements.
   */ 
  public Integer
  computeJobBias
  (
   String author, 
   JobReqs jreqs, 
   TreeSet<String> keys
  )
  {
    ResourceSample sample = getLatestSample();
    if(sample == null) 
      return null;

    if((pReservation != null) && (!pReservation.equals(author)))
      return null;

    long now = Dates.now().getTime();
    if(((now - sample.getTimeStamp().getTime()) > sSampleInterval) ||
       (sample.getNumJobs() >= pJobSlots) ||
       (sample.getLoad() > jreqs.getMaxLoad()) ||
       (sample.getMemory() < jreqs.getMinMemory()) || 
       (sample.getDisk() < jreqs.getMinDisk()))
      return null;

    int total = 0;
    for(String key : jreqs.getSelectionKeys()) {
      if(keys.contains(key)) {
	Integer bias = pSelectionBiases.get(key);
	if(bias == null) 
	  return null;
      
	total += bias;
      }
    }

    return total;
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
     
    encoder.encode("JobSlots", pJobSlots);

    if(!pSelectionBiases.isEmpty()) 
      encoder.encode("SelectionBiases", pSelectionBiases);
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

    Integer slots = (Integer) decoder.decode("JobSlots"); 
    if(slots == null) 
      throw new GlueException("The \"JobSlots\" was missing!");
    pJobSlots = slots;

    TreeMap<String,Integer> biases = 
      (TreeMap<String,Integer>) decoder.decode("SelectionBiases"); 
    if(biases != null) 
      pSelectionBiases = biases;
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
     * No <B>pljobmgr</B>(1) daemon is currently running on the host.  <P> 
     * 
     * No jobs will be assigned to this host until the <B>pljobmgr</B>(1) daemon is restarted.
     */ 
    Shutdown, 

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
     * A <B>pljobmgr</B>(1) daemon is currently running on the host and is available to 
     * run new jobs which meet the selection criteria for the host.
     */ 
    Enabled; 

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

  /**
   * The maximum interval of time (in milliseconds) for which samples are retained.
   */ 
  private static final long  sSampleInterval = 1800000;  /* 30-minutes */ 



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The current operational status of the host.
   */ 
  private Status  pStatus;

  /**
   * The name of the reserving user or <CODE>null</CODE> if the host is not reserved.
   */ 
  private String  pReservation;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The maximum number jobs the host may be assigned.
   */ 
  private int  pJobSlots; 

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
   * The system resource usage samples (newest to oldest).
   */ 
  private LinkedList<ResourceSample>  pSamples;


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The selection key biases of the host indexed by selection key name.
   */ 
  private TreeMap<String,Integer>  pSelectionBiases; 

}
