// $Id: NodeKillJobGroupReq.java,v 1.2 2004/08/22 22:04:34 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   K I L L   J O B   G R O U P   R E Q                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to kill all jobs which belong to the job group with the given ID. <P> 
 * 
 * @see MasterMgr
 */
public
class NodeKillJobGroupReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param groupID
   *   The unique job group identifier.
   */
  public
  NodeKillJobGroupReq
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
   * Gets the unique job group identifier.
   */
  public long
  getGroupID() 
  {
    return pGroupID; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7750367585512039309L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique job group identifier.
   */ 
  private long  pGroupID; 

}
  
