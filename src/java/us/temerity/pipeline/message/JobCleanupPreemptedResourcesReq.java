// $Id: JobCleanupPreemptedResourcesReq.java,v 1.1 2005/12/31 20:42:59 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   C L E A N U P   P R E E M P T E D   R E S O U R C E S   R E Q                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to clean up the resources associated with a preempted job.
 */
public
class JobCleanupPreemptedResourcesReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param jobID
   *   The ID of the preempted job. 
   */
  public
  JobCleanupPreemptedResourcesReq
  (
   long jobID
  ) 
  { 
    pJobID = jobID;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the ID of the preempted job. 
   */
  public long
  getJobID()
  {
    return pJobID; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7787388365775175824L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The ID of the preempted job. 
   */ 
  private long  pJobID; 

}
  
