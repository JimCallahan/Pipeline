// $Id: ResourceSampleBlock.java,v 1.9 2006/02/27 17:54:52 jim Exp $

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

  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */
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
   *   The resource samples.
   */ 
  public 
  ResourceSampleBlock
  (
   int slots, 
   int procs, 
   long memory, 
   long disk, 
   ArrayList<ResourceSample> samples
  ) 
  {
    pJobSlots      = slots; 
    pNumProcessors = procs; 
    pTotalMemory   = memory; 
    pTotalDisk     = disk; 

    /* compute start/end time */ 
    {
      Date startStamp = null;
      Date endStamp   = null;
      for(ResourceSample sample : samples) {
	if((startStamp == null) || (sample.getTimeStamp().compareTo(startStamp) < 0))
	  startStamp = sample.getTimeStamp();

	if((endStamp == null) || (sample.getTimeStamp().compareTo(endStamp) > 0))
	  endStamp = sample.getTimeStamp();
      }

      {	
	Calendar cal = Calendar.getInstance();
	cal.setTime(startStamp);
	cal.set(Calendar.MILLISECOND, 0);
	cal.set(Calendar.SECOND, 0);
	pStartStamp = cal.getTime();
      }

      {	
	Calendar cal = Calendar.getInstance();
	cal.setTime(endStamp);
	cal.set(Calendar.MILLISECOND, 0);
	cal.set(Calendar.SECOND, 0);	
	pEndStamp = cal.getTime();
      }
	
      pLastStamp = endStamp; 
    }

    /* collate samples */ 
    {
      int num = ((int) ((pEndStamp.getTime() - pStartStamp.getTime()) / 60000L)) + 1;
      pNumJobs = new int[num];
      pLoad    = new float[num];
      pMemory  = new long[num];
      pDisk    = new long[num];

      int cnt[] = new int[num];
      for(ResourceSample sample : samples) {
	int idx = (int) ((sample.getTimeStamp().getTime() - pStartStamp.getTime()) / 60000L);

	pNumJobs[idx] += sample.getNumJobs();
	pLoad[idx]    += sample.getLoad();
	pMemory[idx]  += sample.getMemory();
	pDisk[idx]    += sample.getDisk();
	
	cnt[idx]++;
      }
      
      int idx;
      for(idx=0; idx<cnt.length; idx++) {
	if(cnt[idx] > 1) {
	  pNumJobs[idx] /= cnt[idx];
	  pLoad[idx]    /= (float) cnt[idx];
	  pMemory[idx]  /= (long) cnt[idx];
	  pDisk[idx]    /= (long) cnt[idx];
	}
      }
    }
  }

  /**
   * Construct a new sample block.
   * 
   * @param blocks
   *   The resource sample blocks.
   */ 
  public 
  ResourceSampleBlock
  (
   ArrayList<ResourceSampleBlock> blocks
  ) 
  {
    if(blocks.isEmpty()) 
      throw new IllegalArgumentException
	("There must be at least one initial resource usage block!");

    /* use the totals from the latest block */ 
    {
      ResourceSampleBlock block = blocks.get(blocks.size()-1);
      pJobSlots      = block.getJobSlots();
      pNumProcessors = block.getNumProcessors();
      pTotalMemory   = block.getTotalMemory();
      pTotalDisk     = block.getTotalDisk();
    }

    /* compute start/end time */ 
    for(ResourceSampleBlock block : blocks) {
      if((pStartStamp == null) || (block.getStartTimeStamp().compareTo(pStartStamp) < 0))
	pStartStamp = block.getStartTimeStamp();

      if((pEndStamp == null) || (block.getEndTimeStamp().compareTo(pEndStamp) > 0))
	pEndStamp = block.getEndTimeStamp();

      if((pLastStamp == null) || (block.getLastTimeStamp().compareTo(pLastStamp) > 0))
	pLastStamp = block.getLastTimeStamp();
    }

    /* collate samples */ 
    {
      int num = ((int) ((pEndStamp.getTime() - pStartStamp.getTime()) / 60000L)) + 1;
      pNumJobs = new int[num];
      pLoad    = new float[num];
      pMemory  = new long[num];
      pDisk    = new long[num];
    
      int cnt[] = new int[num];
      for(ResourceSampleBlock block : blocks) {
	Date startStamp = block.getStartTimeStamp();
	int offset = (int) ((startStamp.getTime() - pStartStamp.getTime()) / 60000L);

	int i;
	for(i=0; i<block.getNumSamples(); i++) {
	  int idx = i+offset;

	  pNumJobs[idx] += block.getNumJobs(i);
	  pLoad[idx]    += block.getLoad(i);
	  pMemory[idx]  += block.getMemory(i);
	  pDisk[idx]    += block.getDisk(i);
	
	  cnt[idx]++;
	}
      }
      
      int idx;
      for(idx=0; idx<cnt.length; idx++) {
	if(cnt[idx] > 1) {
	  pNumJobs[idx] /= cnt[idx];
	  pLoad[idx]    /= (float) cnt[idx];
	  pMemory[idx]  /= (long) cnt[idx];
	  pDisk[idx]    /= (long) cnt[idx];
	}
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
   * Get the timestamp of the beginning of the first per-minute average sample period.
   */ 
  public Date
  getStartTimeStamp() 
  {
    return pStartStamp;
  }

  /**
   * Get the timestamp of the beginning of the last per-minute average sample period.
   */ 
  public Date
  getEndTimeStamp() 
  {
    return pEndStamp;
  }

  
  /**
   * The timestamp of the last individual sample.
   */ 
  public Date
  getLastTimeStamp() 
  {
    return pLastStamp;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the number of 1-minute sample periods contained in this sample block.
   */ 
  public int
  getNumSamples()
  {
    if(pNumJobs != null) 
      return pNumJobs.length;
    return 0;
  }

  /**
   * Get the average number of jobs running on the host during the given per-minute sample
   * period.
   * 
   * @param idx
   *   The sample index (in minutes since the first sample period).
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
   * Get the average system load during the given per-minute sample period.
   * 
   * @param idx
   *   The sample index (in minutes since the first sample period).
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
   * Get the average amount of available free memory (in bytes) during the given 
   * per-minute sample period.
   * 
   * @param idx
   *   The sample index (in minutes since the first sample period).
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
   * Get the average available free temporary disk space (in bytes) during the given 
   * per-minute sample period.
   * 
   * @param idx
   *   The sample index (in minutes since the first sample period).
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
    
    encoder.encode("StartTimeStamp", pStartStamp.getTime());    
    encoder.encode("EndTimeStamp", pEndStamp.getTime());    

    encoder.encode("LastTimeStamp", pLastStamp.getTime());

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

    {
      Long stamp = (Long) decoder.decode("StartTimeStamp");
      if(stamp == null) 
	throw new GlueException("The \"StartTimeStamp\" was missing!");
      pStartStamp = new Date(stamp);
    }

    {
      Long stamp = (Long) decoder.decode("EndTimeStamp");
      if(stamp == null) 
	throw new GlueException("The \"EndTimeStamp\" was missing!");
      pEndStamp = new Date(stamp);
    }

    {
      Long stamp = (Long) decoder.decode("LastTimeStamp");
      if(stamp == null) 
	throw new GlueException("The \"LastTimeStamp\" was missing!");
      pLastStamp = new Date(stamp);
    }

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
   * The timestamp of the beginning of the first per-minute average sample period.
   */ 
  private Date  pStartStamp;
  
  /**
   * The timestamp of the beginning of the last per-minute average sample period.
   */ 
  private Date  pEndStamp;

  /**
   * The timestamp of the last individual sample.
   */ 
  private Date  pLastStamp;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The number of currently running jobs per-minute (oldest to newest).
   */ 
  private int[]  pNumJobs; 

  /**
   * The system load on the host per-minute (oldest to newest).
   */ 
  private float[]  pLoad; 

  /**
   * The available free memory per-minute (in bytes) on the host (oldest to newest).
   */ 
  private long[]  pMemory;

  /**
   * The available free temporary disk space (in bytes) on the host per-minute 
   * (oldest to newest).
   */ 
  private long[]  pDisk;

}
