// $Id: QueueHost.java,v 1.1 2004/07/24 18:28:45 jim Exp $

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

    pMaxSamples = 50;
    pLoad   = new LinkedList<Float>();
    pMemory = new LinkedList<Long>();
    pDisk   = new LinkedList<Long>();
    
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
   * Get the last known sample of system load for the host.
   * 
   * @return 
   *   The system load or <CODE>null</CODE> if no samples exist.
   */ 
  public Float 
  getLoad() 
  {
    if(pLoad.isEmpty()) 
      return null;
    return pLoad.getLast();
  }

  /** 
   * Get all of the samples of system load for the host.
   * 
   * @return 
   *   The system load samples.  
   */ 
  public Float[]
  getLoadSamples() 
  {
    Float load[] = new Float[pLoad.size()];
    return (Float[]) pLoad.toArray(load);
  }

  
  /**
   * Get the last known sample of available free memory (in bytes) for the host.
   * 
   * @return 
   *   The free memory or <CODE>null</CODE> if no samples exist.
   */ 
  public Long 
  getMemory() 
  {
    if(pMemory.isEmpty()) 
      return null;
    return pMemory.getLast();
  }

  /** 
   * Get all of the samples of available free memory (in bytes) for the host.
   * 
   * @return 
   *   The system memory samples.  
   */ 
  public Long[]
  getMemorySamples() 
  {
    Long memory[] = new Long[pMemory.size()];
    return (Long[]) pMemory.toArray(memory);
  }


  /**
   * Get the last known sample of available free temporary disk space (in bytes) for the host.
   * 
   * @return 
   *   The free disk space or <CODE>null</CODE> if no samples exist.
   */ 
  public Long 
  getDisk() 
  {
    if(pDisk.isEmpty()) 
      return null;
    return pDisk.getLast();
  }

  /** 
   * Get all of the samples of available free temporary disk space (in bytes) for the host.
   * 
   * @return 
   *   The system disk samples.  
   */ 
  public Long[]
  getDiskSamples() 
  {
    Long disk[] = new Long[pDisk.size()];
    return (Long[]) pDisk.toArray(disk);
  }

  
  /**
   * Add a resource usage sample for the host.
   * 
   * @param load
   *   The system load.
   * 
   * @param memory
   *   The available free memory (in bytes).
   *
   * @param disk
   *   The available free temporary disk space (in bytes).
   */ 
  public void 
  addResourceSample
  (
   float load, 
   long memory, 
   long disk
  ) 
  {
    {
      pLoad.addLast(load);

      int extra = pLoad.size() - pMaxSamples;
      while(extra > 0) {
	pLoad.removeFirst();
	extra--;
      }
    }

    {
      pMemory.addLast(memory);
      
      int extra = pMemory.size() - pMaxSamples;
      while(extra > 0) {
	pMemory.removeFirst();
	extra--;
      }
    }

    {
      pDisk.addLast(disk);

      int extra = pDisk.size() - pMaxSamples;
      while(extra > 0) {
	pDisk.removeFirst();
	extra--;
      }
    }    
  }

  /**
   * Get the maximum number of resource usage samples retained for the host.
   */ 
  public int 
  getMaxSamples() 
  {
    return pMaxSamples;
  }

  /**
   * Set the maximum number of resource usage samples retained for the host.
   */ 
  public void
  setMaxSamples
  (
   int samples
  ) 
  {
    if(samples <= 0) 
      throw new IllegalArgumentException
	("The number of samples (" + samples + ") must be positive!");
    pMaxSamples = samples;

    {
      int extra = pLoad.size() - pMaxSamples;
      while(extra > 0) {
	pLoad.removeFirst();
	extra--;
      }
    }

    {
      int extra = pMemory.size() - pMaxSamples;
      while(extra > 0) {
	pMemory.removeFirst();
	extra--;
      }
    }

    {
      int extra = pDisk.size() - pMaxSamples;
      while(extra > 0) {
	pDisk.removeFirst();
	extra--;
      }
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
   * This method will return <CODE>null</CODE> if the host is unable to provide the 
   * system resourses specified by the job requirements or does not support all of the 
   * selection keys required.
   * 
   * @param jreqs
   *   The requirements that this host must meet in order to be eligable to run the job. 
   * 
   * @return 
   *   The combined selection bias or <CODE>null</CODE> if the host fails the requirements.
   */ 
  public Integer
  checkJobRequirements
  (
   JobReqs jreqs
  )
  {
    Float load = getLoad();
    if((load == null) || (load > jreqs.getMaxLoad())) 
      return null;

    Long mem = getMemory();
    if((mem == null) || (mem < jreqs.getMinMemory())) 
      return null;

    Long disk = getDisk();
    if((disk == null) || (disk < jreqs.getMinDisk())) 
      return null;

    int total = 0;
    for(String key : jreqs.getSelectionKeys()) {
      Integer bias = pSelectionBiases.get(key);
      if(bias == null) 
	return null;
      
      total += bias;
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
    
    encoder.encode("MaxSamples", pMaxSamples);

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

    Integer samples = (Integer) decoder.decode("MaxSamples"); 
    if(samples == null) 
      throw new GlueException("The \"MaxSamples\" was missing!");
    pMaxSamples = samples;

    TreeMap<String,Integer> biases = 
      (TreeMap<String,Integer>) decoder.decode("SelectionBiases"); 
    if(biases != null) 
      pSelectionBiases = biases;
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
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
   * The name of the reserving user or <CODE>null</CODE> if the host is not reserved.
   */ 
  private String  pReservation;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The maxmimum number of number of resource usage samples retained for the host.
   */ 
  private int  pMaxSamples;

  /**
   * The samples of system load for the host.
   */ 
  private LinkedList<Float>  pLoad; 

  /**
   * The samples of available free memory (in bytes) for the host.
   */ 
  private LinkedList<Long>  pMemory;

  /**
   * The samples of available free temporary disk space (in bytes) for the host.
   */ 
  private LinkedList<Long>   pDisk;
  

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The selection key biases of the host indexed by selection key name.
   */ 
  private TreeMap<String,Integer>  pSelectionBiases; 

}
