// $Id: QueueGetJobStatusReq.java,v 1.1 2004/08/26 05:57:36 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   J O B   S T A T U S   R E Q                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get JobStatus of all jobs associated with the given job group IDs. 
 */
public
class QueueGetJobStatusReq
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
  QueueGetJobStatusReq
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

  private static final long serialVersionUID = -3834278173635328552L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier.
   */ 
  private TreeSet<Long> pGroupIDs; 


}
  
