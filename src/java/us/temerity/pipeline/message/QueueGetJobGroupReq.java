// $Id: QueueGetJobGroupReq.java,v 1.1 2004/08/22 22:05:43 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   J O B   G R O U P   R E Q                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the job group with the given ID. 
 */
public
class QueueGetJobGroupReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param groupID
   *   The unique job group identifier.
   */
  public
  QueueGetJobGroupReq
  (
   long groupID
  )
  { 
    pGroupID = groupID;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the unique job group identifier. 
   */
  public long
  getGroupID() 
  {
    return pGroupID; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2027828843214576463L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique job group identifier. 
   */ 
  private long  pGroupID; 

}

  
