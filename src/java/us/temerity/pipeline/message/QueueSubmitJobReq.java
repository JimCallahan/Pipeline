// $Id: QueueSubmitJobReq.java,v 1.1 2004/08/22 22:05:43 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   S U B M I T   J O B   R E Q                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A request submit a job to be executed by the queue. <P> 
 */
public
class QueueSubmitJobReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param job
   *   The queue job.
   */
  public
  QueueSubmitJobReq
  (
   QueueJob job
  )
  { 
    if(job == null) 
      throw new IllegalArgumentException("The job cannot be (null)!");
    pJob = job;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the queue job.
   */
  public QueueJob
  getJob() 
  {
    return pJob;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4486985434294192849L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The queue job.
   */ 
  private QueueJob  pJob; 

}

  
