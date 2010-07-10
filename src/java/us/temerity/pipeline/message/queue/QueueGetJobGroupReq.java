// $Id: QueueGetJobGroupReq.java,v 1.1 2004/08/22 22:05:43 jim Exp $

package us.temerity.pipeline.message.queue;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   J O B   G R O U P   R E Q                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the job group with the given IDs. 
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
    pGroupIDs = new TreeSet<Long>();
    pGroupIDs.add(groupID);
  }
  
  /** 
   * Constructs a new request. <P> 
   * 
   * @param groupIDs
   *   The set of unique job group identifier.
   */
  public
  QueueGetJobGroupReq
  (
    Set<Long> groupIDs
  )
  {
    if (groupIDs == null || groupIDs.isEmpty())
      throw new IllegalArgumentException
        ("The groupIDs parameter must be a valid set and contain at least one ID.");
    pGroupIDs = new TreeSet<Long>();
    pGroupIDs.addAll(groupIDs);
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the unique job group identifiers. 
   */
  public TreeSet<Long>
  getGroupIDs() 
  {
    return pGroupIDs; 
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
  private TreeSet<Long> pGroupIDs; 
}

