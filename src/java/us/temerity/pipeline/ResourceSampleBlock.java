// $Id: ResourceSampleBlock.java,v 1.5 2005/01/22 06:10:09 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   R E S O U R C E   S A M P L E   B L O C K                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A series of point samples of available queue host resources.
 */
public
class ResourceSampleBlock
  implements Glueable, Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  ResourceSampleBlock() 
  {}

  /**
   * Construct a new sample block.
   * 
   * @param slots
   *   The number of job slots.
   * 
   * @param procs 
   *   The number of processors.
   * 
   * @param memory 
   *   The total memory size (in bytes).
   * 
   * @param disk 
   *   The total temporary disk size (in bytes).
   * 
   * @param samples
   *   The resource samples (newest to oldest).
   */ 
  public 
  ResourceSampleBlock
  (
   int slots, 
   int procs, 
   long memory, 
   long disk, 
   Collection<ResourceSample> samples
  ) 
  {
    pJobSlots      = slots; 
    pNumProcessors = procs; 
    pTotalMemory   = memory; 
    pTotalDisk     = disk; 

    int num = samples.size();
    pTimeStamp  = new long[num];
    pNumJobs    = new int[num];
    pLoad       = new float[num];
    pMemory     = new long[num];
    pDisk       = new long[num];

    int wk = num - 1;
    for(ResourceSample sample : samples) {
      pTimeStamp[wk]  = sample.getTimeStamp().getTime();
      pNumJobs[wk]    = sample.getNumJobs();
      pLoad[wk]       = sample.getLoad();
      pMemory[wk]     = sample.getMemory();
      pDisk[wk]       = sample.getDisk();
      
      wk--;
    }
  }

  /**
   * Construct a new sample block.
   * 
   * @param blocks
   *   The resource sample blocks (oldest to newest).
   */ 
  public 
  ResourceSampleBlock
  (
   Collection<ResourceSampleBlock> blocks
  ) 
  {
    int num = 0;
    for(ResourceSampleBlock block : blocks) 
      num += block.getNumSamples();

    pTimeStamp  = new long[num];
    pNumJobs    = new int[num];
    pLoad       = new float[num];
    pMemory     = new long[num];
    pDisk       = new long[num];

    boolean first = true;
    int wk = 0;
    for(ResourceSampleBlock block : blocks) {
      if(first) {
	pJobSlots      = block.pJobSlots; 
	pNumProcessors = block.pNumProcessors;
	pTotalMemory   = block.pTotalMemory;
	pTotalDisk     = block.pTotalDisk;

	first = false;
      }

      int bk;
      for(bk=0; bk<block.getNumSamples(); bk++, wk++) {
	pTimeStamp[wk] = block.pTimeStamp[bk];
	pNumJobs[wk]   = block.pNumJobs[bk];
	pLoad[wk]      = block.pLoad[bk];
	pMemory[wk]    = block.pMemory[bk];
	pDisk[wk]      = block.pDisk[bk]; 
      }
    }
  }

   
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
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
   * Get the number of processors on the host.
   * 
   * @return 
   *   The number of processors.
   */ 
  public int 
  getNumProcessors() 
  {
    return pNumProcessors;
  }

  /**
   * Get the total amount of memory (in bytes) on the host.
   * 
   * @return 
   *   The memory size.
   */ 
  public long 
  getTotalMemory() 
  {
    return pTotalMemory;
  }
  
  /**
   * Get the total amount of temporary disk space (in bytes) on the host.
   * 
   * @return 
   *   The disk size.
   */ 
  public long 
  getTotalDisk() 
  {
    return pTotalDisk;
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the number of samples.
   */ 
  public int
  getNumSamples()
  {
    if(pTimeStamp != null) 
      return pTimeStamp.length;
    return 0;
  }

  /**
   * Get the time stamp of when the samples was recorded.
   * 
   * @param idx
   *   The sample index.
   */ 
  public Date
  getTimeStamp
  (
   int idx
  ) 
  {
    return new Date(pTimeStamp[idx]);
  }

  /**
   * Get the number of jobs running on the host.
   * 
   * @param idx
   *   The sample index.
   */ 
  public int
  getNumJobs
  (
   int idx
  ) 
  {
    return pNumJobs[idx]; 
  }

  /**
   * Get the system load.
   * 
   * @param idx
   *   The sample index.
   */ 
  public float 
  getLoad
  (
   int idx
  )  
  {
    return pLoad[idx];
  }
  
  /**
   * Get the amount of available free memory (in bytes).
   * 
   * @param idx
   *   The sample index.
   */ 
  public long 
  getMemory
  (
   int idx
  ) 
  {
    return pMemory[idx];
  }

  /**
   * Get the available free temporary disk space (in bytes).
   * 
   * @param idx
   *   The sample index.
   */ 
  public long 
  getDisk
  (
   int idx
  ) 
  {
    return pDisk[idx];
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
    encoder.encode("JobSlots", pJobSlots); 
    encoder.encode("NumProcessors", pNumProcessors); 
    encoder.encode("TotalMemory", pTotalMemory); 
    encoder.encode("TotalDisk", pTotalDisk); 
    
    encoder.encode("TimeStamp", pTimeStamp);
    encoder.encode("NumJobs", pNumJobs); 
    encoder.encode("Load", pLoad); 
    encoder.encode("Memory", pMemory); 
    encoder.encode("Disk", pDisk); 
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    Integer slots = (Integer) decoder.decode("JobSlots");
    if(slots == null) 
      throw new GlueException("The \"JobSlots\" was missing!");
    pJobSlots = slots;

    Integer procs = (Integer) decoder.decode("NumProcessors");
    if(procs == null) 
      throw new GlueException("The \"NumProcessors\" was missing!");
    pNumProcessors = procs;

    Long tmemory = (Long) decoder.decode("TotalMemory");
    if(tmemory == null) 
      throw new GlueException("The \"TotalMemory\" was missing!");
    pTotalMemory = tmemory;

    Long tdisk = (Long) decoder.decode("TotalDisk");
    if(tdisk == null) 
      throw new GlueException("The \"TotalDisk\" was missing!");
    pTotalDisk = tdisk;


    long[] stamp = (long[]) decoder.decode("TimeStamp");
    if(stamp == null) 
      throw new GlueException("The \"TimeStamp\" was missing!");
    pTimeStamp = stamp;

    int[] jobs = (int[]) decoder.decode("NumJobs");
    if(jobs == null) 
      throw new GlueException("The \"NumJobs\" was missing!");
    pNumJobs = jobs;

    float[] load = (float[]) decoder.decode("Load");
    if(load == null) 
      throw new GlueException("The \"Load\" was missing!");
    pLoad = load;

    long[] memory = (long[]) decoder.decode("Memory");
    if(memory == null) 
      throw new GlueException("The \"Memory\" was missing!");
    pMemory = memory;

    long[] disk = (long[]) decoder.decode("Disk");
    if(disk == null) 
      throw new GlueException("The \"Disk\" was missing!");
    pDisk = disk;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -547095830269839260L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The maximum number jobs the host may be assigned.
   */ 
  private int  pJobSlots; 

  /**
   * The number of processors on the host.
   */ 
  private int  pNumProcessors; 

  /**
   * The total amount of memory (in bytes) on the host.
   */ 
  private long  pTotalMemory;

  /**
   * The total amount of temporary disk space (in bytes) on the host.
   */ 
  private long  pTotalDisk;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The timestamp of when the samples was measured (oldest to newest).
   */ 
  private long[]  pTimeStamp;
  
  /**
   * The number of currently running jobs (oldest to newest).
   */ 
  private int[]  pNumJobs; 

  /**
   * The system load on the host (oldest to newest).
   */ 
  private float[]  pLoad; 

  /**
   * The available free memory (in bytes) on the host (oldest to newest).
   */ 
  private long[]  pMemory;

  /**
   * The available free temporary disk space (in bytes) on the host (oldest to newest).
   */ 
  private long[]  pDisk;
  


}
