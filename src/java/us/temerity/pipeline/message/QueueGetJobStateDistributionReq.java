// $Id: QueueGetJobStateDistributionReq.java,v 1.1 2009/05/14 23:30:43 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   J O B   S T A T E   D I S T R I B U T I O N   R E Q                */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the distribution of job states for the jobs associated with each of 
 * the given job group IDs.
 */
public
class QueueGetJobStateDistributionReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param groupIDs
   *   The unique job group IDs.
   */
  public
  QueueGetJobStateDistributionReq
  (
   TreeSet<Long> groupIDs
  )
  { 
    if(groupIDs == null) 
      throw new IllegalArgumentException("The job group IDs cannot be (null)!");
    pGroupIDs = groupIDs;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the unique job group IDs.
   */
  public TreeSet<Long>
  getGroupIDs() 
  {
    return pGroupIDs; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3071307209168035861L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier.
   */ 
  private TreeSet<Long> pGroupIDs; 


}
  
