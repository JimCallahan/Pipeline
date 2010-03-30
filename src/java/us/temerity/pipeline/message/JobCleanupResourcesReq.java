// $Id: JobCleanupResourcesReq.java,v 1.1 2004/09/03 02:02:02 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   C L E A N U P   R E S O U R C E S   R E Q                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to clean up obsolete job resources. <P> 
 */
public
class JobCleanupResourcesReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param jobIDs
   *   The IDs of all active jobs.
   */
  public
  JobCleanupResourcesReq
  (
   TreeSet<Long> jobIDs
  )
  { 
    pJobIDs = jobIDs;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the IDs of all active jobs.
   */
  public TreeSet<Long>
  getJobIDs()
  {
    return pJobIDs; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4752945837533410396L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The IDs of all active jobs.
   */ 
  private TreeSet<Long>  pJobIDs; 

}
  
