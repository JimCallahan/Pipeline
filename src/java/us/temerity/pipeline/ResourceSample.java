// $Id: ResourceSample.java,v 1.1 2004/07/28 19:22:50 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.util.logging.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   R E S O U R C E   S A M P L E                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A point sample of available queue host resources.
 */
public
class ResourceSample
  implements Glueable, Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  ResourceSample() 
  {}

  /**
   * Construct a new sample.
   * 
   * @param jobs
   *   The number of currently running jobs.
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
  public 
  ResourceSample
  (
   int jobs, 
   float load, 
   long memory, 
   long disk
  )  
  {
    pTimeStamp = new Date(); 

    pNumJobs   = jobs;
    pLoad      = load; 
    pMemory    = memory; 
    pDisk      = disk;
  }



   
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the time stamp of when the sample was recorded.
   */ 
  public Date 
  getTimeStamp() 
  {
    return pTimeStamp;
  }

  /**
   * Get the number of jobs running on the host.
   */ 
  public int
  getNumJobs() 
  {
    return pNumJobs; 
  }

  /**
   * Get the system load.
   */ 
  public float 
  getLoad() 
  {
    return pLoad;
  }
  
  /**
   * Get the amount of available free memory (in bytes).
   */ 
  public long 
  getMemory() 
  {
    return pMemory;
  }

  /**
   * Get the available free temporary disk space (in bytes).
   */ 
  public long 
  getDisk() 
  {
    return pDisk;
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
    encoder.encode("TimeStamp", pTimeStamp.getTime());
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
    Long stamp = (Long) decoder.decode("TimeStamp");
    if(stamp == null) 
      throw new GlueException("The \"TimeStamp\" was missing!");
    pTimeStamp = new Date(stamp);

    Integer jobs = (Integer) decoder.decode("NumJobs");
    if(jobs == null) 
      throw new GlueException("The \"NumJobs\" was missing!");
    pNumJobs = jobs;

    Long memory = (Long) decoder.decode("Memory");
    if(memory == null) 
      throw new GlueException("The \"Memory\" was missing!");
    pMemory = memory;

    Long disk = (Long) decoder.decode("Memory");
    if(disk == null) 
      throw new GlueException("The \"Memory\" was missing!");
    pMemory = disk;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3380307201487190647L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The timestamp of when the samples was measured.
   */ 
  private Date  pTimeStamp;
  
  /**
   * The number of currently running jobs.
   */ 
  private int  pNumJobs; 

  /**
   * The system load on the host.
   */ 
  private float  pLoad; 

  /**
   * The available free memory (in bytes) on the host.
   */ 
  private long  pMemory;

  /**
   * The available free temporary disk space (in bytes) on the host.
   */ 
  private long  pDisk;
  

}
