// $Id: JobStartReq.java,v 1.1 2004/07/28 19:10:23 jim Exp $

package us.temerity.pipeline.message;

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
   */
  public
  JobStartReq
  (
   QueueJob job
  )
  { 
    if(job == null) 
      throw new IllegalArgumentException("The queue job cannot be (null)!");
    pJob = job; 
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

}
  
