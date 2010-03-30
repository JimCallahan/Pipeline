// $Id: NodeGetAllBothAnnotationsReq.java,v 1.1 2009/05/18 06:31:49 jesse Exp $

package us.temerity.pipeline.message;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   G E T   A L L   B O T H   A N N O T A T I O N S   R E Q                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the all of the annotations for the given nodes.
 */
public
class NodeGetAllBothAnnotationsReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param ids 
   *   The unique working version identifier.
   */
  public
  NodeGetAllBothAnnotationsReq
  (
    Set<NodeID> ids
  )
  { 
    if(ids == null) 
      throw new IllegalArgumentException("The set of working version ID cannot be (null)!");
    pNodeIDs = new TreeSet<NodeID>(ids);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the set of unique working version identifiers.
   */
  public TreeSet<NodeID>
  getNodeIDs() 
  {
    return pNodeIDs;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5317641918759694590L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier.
   */ 
  private TreeSet<NodeID>  pNodeIDs;

}
  
