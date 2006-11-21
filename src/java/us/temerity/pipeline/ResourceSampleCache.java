// $Id: ResourceSampleCache.java,v 1.1 2006/11/21 19:55:51 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   R E S O U R C E   S A M P L E   C A C H E                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A fixed size cache containing the last N resource samples collected.<P> 
 * 
 * Samples are stored in a ring buffer which is reused once more than N samples have been
 * added to the cache.  Samples will always be stored so that the samples can be retreived
 * in oldesst to newest order.  When new samples are added to the cache, they will be ignored
 * if they are older than the newest existing sample already cached.  Note that there are no 
 * guarentees that the interval between successive samples is a constant amount of time, only 
 * that samples are stored in increasing time value order.  So even though the cache can hold 
 * a fixed maximum number of samples, the duration between the oldest and newest sample can 
 * be any length of time.
 */
public
class ResourceSampleCache
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
  ResourceSampleCache() 
  {}

  /**
   * Construct a new set of rsource samples.
   * 
   * @param size
   *   The number of samples to retain.
   */
  public 
  ResourceSampleCache
  (
   int size
  )    
  {
    pStamp   = new long[size];
    pNumJobs = new int[size];
    pLoad    = new float[size];
    pMemory  = new long[size];
    pDisk    = new long[size];
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new cache just large enough to contain the samples collected during the 
   * given interval of time.
   * 
   * @param interval
   *   The sample time interval.
   * 
   * @return 
   *   The newly created cache or <CODE>null</CODE> if no samples exist within the given 
   *   interval.
   */ 
  public ResourceSampleCache
  cloneDuring
  (
   DateInterval interval
  ) 
  {
    int nsize = getNumSamplesDuring(interval); 
    if(nsize == 0) 
      return null;

    long oldest = interval.getStartStamp().getTime();
    long newest = interval.getEndStamp().getTime();

    ResourceSampleCache cache = new ResourceSampleCache(nsize);  
    
    int i = 0;
    int wk;
    int size = getNumSamples();
    for(wk=0; wk<size; wk++) {
      int idx = getSampleIndex(wk);
      long stamp = pStamp[idx];
      if((stamp > oldest) && (stamp <= newest)) {
	cache.pStamp[i]   = stamp;
	cache.pNumJobs[i] = pNumJobs[idx]; 
	cache.pLoad[i]    = pLoad[idx];    
	cache.pMemory[i]  = pMemory[idx];  
	cache.pDisk[i]    = pDisk[idx];    
	i++;
      }
    }

    cache.pReadIdx  = 0;
    cache.pWriteIdx = nsize - 1;

    return cache;
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Wheter any samples have been cached. 
   */ 
  public boolean
  hasSamples()
  {
    return ((pWriteIdx != null) && (pReadIdx != null)); 
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the number of samples currently cached. 
   */ 
  public int
  getNumSamples()
  {
    if((pWriteIdx == null) || (pReadIdx == null)) 
      return 0;

    if(pWriteIdx >= pReadIdx) 
      return (pWriteIdx - pReadIdx + 1);
    else 
      return (pWriteIdx + pStamp.length - pReadIdx + 1);
  }

  /**
   * Get the number of samples in the cache which where collected during the given 
   * interval of time.
   */ 
  public int
  getNumSamplesDuring
  (
   DateInterval interval
  ) 
  {
    long oldest = interval.getStartStamp().getTime();
    long newest = interval.getEndStamp().getTime();

    int cnt = 0;
    {
      int size = getNumSamples();
      int wk; 
      int idx; 
      for(wk=0; wk<size; wk++) {
	idx = getSampleIndex(wk); 
	long stamp = pStamp[idx];
	if((stamp > oldest) && (stamp <= newest))
	  cnt++;
      }
    }
    
    return cnt;
  }

  /**
   * Get the number of samples in the cache which where collected during the given 
   * interval of time.
   */ 
  public int
  getNumSamplesDuring
  (
   Date oldest, 
   Date newest
  ) 
  {
    return getNumSamplesDuring(oldest, newest); 
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Add a new resource sample to the cache. <P> 
   * 
   * Samples older than the newest currently cached sample will be ignored.
   */ 
  public void 
  addSample
  (
   long stamp, 
   int numJobs, 
   float load, 
   long memory, 
   long disk
  ) 
  {
    /* ignore samples older than the newest existing sample */ 
    if((pWriteIdx != null) && (stamp < pStamp[pWriteIdx]))
      return;

    if(pWriteIdx == null) 
      pWriteIdx = 0;
    else
      pWriteIdx = (pWriteIdx + 1) % pStamp.length;

    pStamp[pWriteIdx]   = stamp;
    pNumJobs[pWriteIdx] = numJobs; 
    pLoad[pWriteIdx]    = load; 
    pMemory[pWriteIdx]  = memory; 
    pDisk[pWriteIdx]    = disk; 

    if(pReadIdx == null) 
      pReadIdx = 0;
    else if(pWriteIdx == pReadIdx) 
      pReadIdx = (pReadIdx+1) % pStamp.length;    
  }
  
  /**
   * Add a new resource sample to the cache.<P> 
   * 
   * Samples older than the newest currently cached sample will be ignored.
   */ 
  public void 
  addSample
  (
   ResourceSample sample
  ) 
  {
    addSample(sample.getTimeStamp().getTime(), sample.getNumJobs(), sample.getLoad(), 
	      sample.getMemory(), sample.getDisk()); 
  }

  /**
   * Append all samples from the given cache to this cache.<P> 
   * 
   * Samples older than the newest currently cached sample will be ignored.
   */ 
  public void 
  addAllSamples
  (
   ResourceSampleCache cache 
  ) 
  {
    int size = cache.getNumSamples();
    int wk;    
    for(wk=0; wk<size; wk++) {
      int idx = cache.getSampleIndex(wk);
      addSample(cache.pStamp[idx], cache.pNumJobs[idx], cache.pLoad[idx], 
		cache.pMemory[idx], cache.pDisk[idx]);
    }
  }
  
  /**
   * Append all samples from the given cache within the given interval to this cache.<P> 
   * 
   * Samples older than the newest currently cached sample will be ignored.
   */ 
  public void 
  addAllSamplesDuring
  (
   ResourceSampleCache cache, 
   DateInterval interval 
  ) 
  {
    long oldest = interval.getStartStamp().getTime();
    long newest = interval.getEndStamp().getTime();

    int size = cache.getNumSamples();
    int wk;    
    for(wk=0; wk<size; wk++) {
      int idx = cache.getSampleIndex(wk);
      long stamp = cache.pStamp[idx];
      if((stamp > oldest) && (stamp <= newest)) 
	addSample(stamp, cache.pNumJobs[idx], cache.pLoad[idx], 
		  cache.pMemory[idx], cache.pDisk[idx]);
    }
  }
  
  /**
   * Remove any samples recorded before the given timestamp.
   */ 
  public void
  pruneSamplesBefore
  (
   Date stamp
  ) 
  {
    if((pWriteIdx == null) || (pReadIdx == null)) 
      return; 

    long oldest = stamp.getTime();
    int size = getNumSamples();
    int wk;
    int idx = pReadIdx; 
    for(wk=0; wk<size; wk++) {
      idx = getSampleIndex(wk); 

      if(pStamp[idx] > oldest) {
	pReadIdx = idx;
	return;
      }

      pStamp[idx]   = 0L;
      pNumJobs[idx] = 0; 
      pLoad[idx]    = 0.0f; 
      pMemory[idx]  = 0L;
      pDisk[idx]    = 0L;   
    }    

    pReadIdx  = null;
    pWriteIdx = null;
  }

  /**
   * Clear all samples.
   */ 
  public void 
  clearSamples() 
  {
    pReadIdx  = null;
    pWriteIdx = null;

    int wk;
    for(wk=0; wk<pStamp.length; wk++) {
      pStamp[wk]   = 0L;
      pNumJobs[wk] = 0; 
      pLoad[wk]    = 0.0f; 
      pMemory[wk]  = 0L;
      pDisk[wk]    = 0L;
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the sample at the given index.
   * 
   * @param idx
   *   The sample index.
   */ 
  public ResourceSample
  getSample
  (
   int idx
  ) 
  {
    int i = getSampleIndex(idx); 
    return new ResourceSample(new Date(pStamp[i]), 
			      pNumJobs[i], pLoad[i], pMemory[i], pDisk[i]);
  }

  /**
   * Get the latest sample.
   */ 
  public ResourceSample
  getLatestSample()
  {
    if(pWriteIdx == null) 
      return null;
    
    int i = pWriteIdx;
    return new ResourceSample(new Date(pStamp[i]), 
			      pNumJobs[i], pLoad[i], pMemory[i], pDisk[i]);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the timestamp of when the given sample was collected (in milliseconds since epoch).
   * 
   * @param idx
   *   The sample index.
   */ 
  public long
  getTime
  (
   int idx
  ) 
  {
    return pStamp[getSampleIndex(idx)];
  }

  /**
   * Get the timestamp of when the given sample was collected. 
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
    return new Date(pStamp[getSampleIndex(idx)]);
  }

  /**
   * Get the timestamp of the first sample 
   * or <CODE>null</CODE> if no samples are currently cached.
   */ 
  public Date
  getFirstTimeStamp() 
  {
    if(pReadIdx == null) 
      return null;
    return new Date(pStamp[pReadIdx]);
  }

  /**
   * Get the timestamp of last sample 
   * or <CODE>null</CODE> if no samples are currently cached.
   */ 
  public Date
  getLastTimeStamp() 
  {
    if(pWriteIdx == null) 
      return null;
    return new Date(pStamp[pWriteIdx]);
  }

  /**
   * The interval of time between the oldest and newest sample 
   * or <CODE>null</CODE> if no samples are currently cached.
   */ 
  public DateInterval
  getSampleInterval() 
  {
    Date first = getFirstTimeStamp(); 
    Date last  = getLastTimeStamp(); 
    if((first == null) || (last == null))
      return null;
    return new DateInterval(first, last); 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the number of jobs running on the host during the given sample period.
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
    return pNumJobs[getSampleIndex(idx)]; 
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
    return pLoad[getSampleIndex(idx)]; 
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
    return pMemory[getSampleIndex(idx)]; 
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
    return pDisk[getSampleIndex(idx)]; 
  }

  /**
   * Lookup the actual array index given a sample index.
   */ 
  private int
  getSampleIndex
  (
   int idx
  ) 
  {
    if(pReadIdx == null) 
      throw new IllegalStateException("No samples exist yet!");

    int i = (pReadIdx + idx) % pStamp.length;    
    if((idx < 0) || (idx >= pStamp.length) || (pStamp[i] == 0L)) 
      throw new IllegalArgumentException("Invalid sample index (" + idx + ")!");
    
    return i;
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
    encoder.encode("NumSamples", pStamp.length);

    if((pReadIdx != null) && (pWriteIdx != null)) {
      encoder.encode("ReadIdx", pReadIdx); 
      encoder.encode("WriteIdx", pWriteIdx); 

      encoder.encode("TimeStamp", pStamp); 
      encoder.encode("NumJobs", pNumJobs); 
      encoder.encode("Load", pLoad); 
      encoder.encode("Memory", pMemory); 
      encoder.encode("Disk", pDisk); 
    }
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    Integer size = (Integer) decoder.decode("NumSamples");
    if(size == null) 
      throw new GlueException("The \"NumSamples\" was missing!");
    
    pReadIdx  = (Integer) decoder.decode("ReadIdx");
    pWriteIdx = (Integer) decoder.decode("WriteIdx");

    if((pReadIdx != null) && (pWriteIdx != null)) {
      long[] stamp = (long[]) decoder.decode("TimeStamp");
      if(stamp == null) 
	throw new GlueException("The \"TimeStamp\" was missing!");
      if(stamp.length != size) 
	throw new GlueException("The number of samples in \"TimeStamp\" was incorrect!");
      pStamp = stamp;

      int[] jobs = (int[]) decoder.decode("NumJobs");
      if(jobs == null) 
	throw new GlueException("The \"NumJobs\" was missing!");
      if(jobs.length != size) 
	throw new GlueException("The number of samples in \"NumJobs\" was incorrect!");	
      pNumJobs = jobs;
      
      float[] load = (float[]) decoder.decode("Load");
      if(load == null) 
	throw new GlueException("The \"Load\" was missing!");
      if(load.length != size) 
	throw new GlueException("The number of samples in \"Load\" was incorrect!");	
      pLoad = load;
      
      long[] memory = (long[]) decoder.decode("Memory");
      if(memory == null) 
	throw new GlueException("The \"Memory\" was missing!");
      if(memory.length != size) 
	throw new GlueException("The number of samples in \"Memory\" was incorrect!");	
      pMemory = memory;
      
      long[] disk = (long[]) decoder.decode("Disk");
      if(disk == null) 
	throw new GlueException("The \"Disk\" was missing!");
      if(disk.length != size) 
	throw new GlueException("The number of samples in \"Disk\" was incorrect!");	
      pDisk = disk;
    }
    else {
      pStamp   = new long[size];
      pNumJobs = new int[size];
      pLoad    = new float[size];
      pMemory  = new long[size];
      pDisk    = new long[size];
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1496529595464541369L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The index of oldest sample or <CODE>null</CODE> if no samples exist yet.
   */ 
  private Integer  pReadIdx;
  
  /**
   * The index of the newest sample or <CODE>null</CODE> if no samples exist yet.
   */ 
  private Integer  pWriteIdx;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The time stamps of each sample.
   */ 
  private long[]  pStamp; 

  /**
   * The number of currently running jobs.
   */ 
  private int[]  pNumJobs; 

  /**
   * The system load on the host.
   */ 
  private float[]  pLoad; 

  /**
   * The available free memory on the host.
   */ 
  private long[]  pMemory;

  /**
   * The available free temporary disk space on the host.
   */ 
  private long[]  pDisk;

}
