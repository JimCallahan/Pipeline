// $Id: QueuePauseJobsReq.java,v 1.1 2004/08/30 02:48:15 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   P A U S E   J O B S   R E Q                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to pause the jobs with the given IDs. <P> 
 */
public
class QueuePauseJobsReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param jobIDs
   *   The unique job identifiers.
   */
  public
  QueuePauseJobsReq
  (
   TreeSet<Long> jobIDs
  )
  { 
    if(jobIDs == null) 
      throw new IllegalArgumentException("The job IDs cannot be (null)!");
    pJobIDs = jobIDs;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the unique job identifiers.
   */
  public TreeSet<Long>
  getJobIDs() 
  {
    return pJobIDs;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5657453990449505644L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique job identifiers.
   */ 
  private TreeSet<Long>  pJobIDs;

}

  
