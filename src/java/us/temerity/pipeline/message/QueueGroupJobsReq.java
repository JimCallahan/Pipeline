// $Id: QueueGroupJobsReq.java,v 1.1 2004/08/22 22:05:43 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G R O U P   J O B S   R E Q                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to notify the queue that a set of previously submitted jobs make up a job group.
 */
public
class QueueGroupJobsReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param group
   *   The queue job group.
   */
  public
  QueueGroupJobsReq
  (
   QueueJobGroup group
  )
  { 
    if(group == null) 
      throw new IllegalArgumentException("The job group cannot be (null)!");
    pJobGroup = group;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the queue group.
   */
  public QueueJobGroup
  getJobGroup() 
  {
    return pJobGroup;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2819969724147212302L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The queue job group.
   */ 
  private QueueJobGroup  pJobGroup; 

}

  
