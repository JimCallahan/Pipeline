// $Id: QueueGetJobReq.java,v 1.2 2008/07/03 19:50:02 jesse Exp $

package us.temerity.pipeline.message.queue;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   J O B   R E Q                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the jobs with the given IDs.
 */
public
class QueueGetJobReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param jobIDs
   *   The list of unique job identifiers.
   */
  public
  QueueGetJobReq
  (
   Set<Long> jobIDs
  )
  { 
    pJobIDs = new TreeSet<Long>(jobIDs);
  }
  
  /** 
   * Constructs a new request. <P> 
   * 
   * @param jobID
   *   The unique job identifier.
   */
  public
  QueueGetJobReq
  (
   long jobID
  )
  { 
    pJobIDs = new TreeSet<Long>();
    pJobIDs.add(jobID);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the unique job identifier. 
   */
  public Set<Long>
  getJobIDs() 
  {
    return Collections.unmodifiableSet(pJobIDs); 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2242072943996847202L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique job identifiers. 
   */ 
  private TreeSet<Long>  pJobIDs; 

}

  
