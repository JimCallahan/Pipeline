// $Id: ResourceSampleBlock.java,v 1.1 2004/08/01 15:48:53 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.util.logging.*;
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
   * Construct a new sample.
   * 
   * @param samples
   *   The resource samples. 
   */ 
  public 
  ResourceSampleBlock
  (
   List<ResourceSample> samples
  ) 
  {
    int num = samples.size();
    pTimeStamp  = new long[num];
    pNumJobs    = new int[num];
    pLoad       = new float[num];
    pMemory     = new long[num];
    pDisk       = new long[num];

    int wk = 0;
    for(ResourceSample sample : samples) {
      pTimeStamp[wk]  = sample.getTimeStamp().getTime();
      pNumJobs[wk]    = sample.getNumJobs();
      pLoad[wk]       = sample.getLoad();
      pMemory[wk]     = sample.getMemory();
      pDisk[wk]       = sample.getDisk();
      
      wk++;
    }
  }

   
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
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

  //private static final long serialVersionUID =



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The timestamp of when the samples was measured.
   */ 
  private long[]  pTimeStamp;
  
  /**
   * The number of currently running jobs.
   */ 
  private int[]  pNumJobs; 

  /**
   * The system load on the host.
   */ 
  private float[]  pLoad; 

  /**
   * The available free memory (in bytes) on the host.
   */ 
  private long[]  pMemory;

  /**
   * The available free temporary disk space (in bytes) on the host.
   */ 
  private long[]  pDisk;
  

}
