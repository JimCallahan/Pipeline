// $Id: NodeStatusReq.java,v 1.4 2004/11/03 23:41:12 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   S T A T U S   R E Q                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to obtain the overall node status of the given node.
 * 
 * @see MasterMgr
 */
public
class NodeStatusReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param id 
   *   The unique working version identifier.
   * 
   * @param skipAssoc
   *   Whether to skip computing the status of all nodes on the upstream side of an 
   *   Association link.
   */
  public
  NodeStatusReq
  (
   NodeID id,
   boolean skipAssoc      
  )
  { 
    if(id == null) 
      throw new IllegalArgumentException
	("The working version ID cannot be (null)!");
    pNodeID = id;

    pSkipAssoc = skipAssoc; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the unique working version identifier.
   */
  public NodeID
  getNodeID() 
  {
    return pNodeID;
  }

  /**
   * Whether to skip computing the status of all nodes on the upstream side of an 
   * Association link.
   */
  public boolean 
  skipAssociations() 
  {
    return pSkipAssoc;
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8893807724022964651L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier.
   */ 
  private NodeID  pNodeID;

  /**
   * Whether to skip computing the status of all nodes on the upstream side of an 
   * Association link.
   */
  private boolean  pSkipAssoc;
  
}
  
