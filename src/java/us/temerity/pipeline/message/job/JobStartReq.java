// $Id: JobStartReq.java,v 1.2 2006/07/03 06:38:42 jim Exp $

package us.temerity.pipeline.message.job;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   S T A R T   R E Q                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to start execution of a job on the server.
 */
public
class JobStartReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param job
   *   The queue job to start. 
   * 
   * @param envs  
   *   The cooked toolset environments indexed by operating system type.
   */
  public
  JobStartReq
  (
   QueueJob job, 
   DoubleMap<OsType,String,String> envs
  )
  { 
    if(job == null) 
      throw new IllegalArgumentException("The queue job cannot be (null)!");
    pJob = job; 

    if(envs == null) 
      throw new IllegalArgumentException("The cooked environments cannot be (null)!");
    pCookedEnvs = envs; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the queue job.
   */
  public QueueJob
  getJob()
  {
    return pJob; 
  }

  /**
   * Gets the cooked toolset environments indexed by operating system type.
   */
  public DoubleMap<OsType,String,String>
  getCookedEnvs()
  {
    return pCookedEnvs; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3749831052229551495L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The queue job.
   */ 
  private QueueJob  pJob; 

  /**
   * The cooked toolset environments indexed by operating system type.
   */ 
  private DoubleMap<OsType,String,String>  pCookedEnvs; 

}
  
