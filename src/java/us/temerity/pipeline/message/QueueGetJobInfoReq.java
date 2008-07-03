// $Id: QueueGetJobInfoReq.java,v 1.2 2008/07/03 19:50:01 jesse Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   J O B   I N F O   R E Q                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get information about the current status of jobs in the queue. <P> 
 */
public
class QueueGetJobInfoReq
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
  QueueGetJobInfoReq
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
  QueueGetJobInfoReq
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

  private static final long serialVersionUID = 5344467206112489877L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique job identifiers. 
   */ 
  private TreeSet<Long>  pJobIDs; 

}

  
