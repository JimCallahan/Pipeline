// $Id: NodeUpdatePathReq.java,v 1.1 2004/05/02 12:13:34 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   U P D A T E   P A T H   R E Q                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to update the immediate children of all node path components along the 
 * given path.
 * 
 * @see NodeMgr
 */
public
class NodeUpdatePathReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param id 
   *   The unique working path identifier.
   */
  public
  NodeUpdatePathReq
  (
   NodeID id
  )
  { 
    if(id == null) 
      throw new IllegalArgumentException("The working path ID cannot be (null)!");
    pNodeID = id;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the unique working path identifier.
   */
  public NodeID
  getNodeID() 
  {
    return pNodeID;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1887700495103310854L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier.
   */ 
  private NodeID  pNodeID;

}
  
