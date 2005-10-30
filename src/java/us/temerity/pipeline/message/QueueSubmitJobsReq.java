// $Id: QueueSubmitJobsReq.java,v 1.1 2005/10/30 10:01:32 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   S U B M I T   J O B S   R E Q                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A request submit a set of jobs to be executed by the queue. <P> 
 */
public
class QueueSubmitJobsReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param jobs
   *   The queue jobs.
   */
  public
  QueueSubmitJobsReq
  (
   ArrayList<QueueJob> jobs
  )
  { 
    if(jobs == null) 
      throw new IllegalArgumentException("The job cannot be (null)!");

    if(jobs.isEmpty()) 
      throw new IllegalArgumentException("At least one job must be submitted!");
      
    pJobs = jobs;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the queue jobs.
   */
  public ArrayList<QueueJob>
  getJobs() 
  {
    return pJobs;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4486985434294192849L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The queue jobs.
   */ 
  private ArrayList<QueueJob>  pJobs; 

}

  
