// $Id: NodeUnlinkReq.java,v 1.2 2004/05/21 21:17:51 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   U N L I N K   R E Q                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to destroy an existing link between the working versions.
 * 
 * @see MasterMgr
 */
public
class NodeUnlinkReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param id 
   *   The unique working version identifier of the downstream version.
   * 
   * @param name
   *   The fully resolved node name of the upstream version.
   */
  public
  NodeUnlinkReq
  (
   NodeID id, 
   String name
  )
  { 
    if(id == null) 
      throw new IllegalArgumentException
	("The downstream working version ID cannot be (null)!");
    pTargetID = id;

    if(name == null) 
      throw new IllegalArgumentException
	("The upstream version name cannot be (null)!");
    pSourceName = name;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the unique working version identifier of the downstream version.
   */
  public NodeID
  getTargetID() 
  {
    return pTargetID;
  }

  /**
   * Gets the fully resolved node name of the upstream version.
   */
  public String
  getSourceName() 
  {
    return pSourceName;
  }

    

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5069729008390364822L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier of the downstream version.
   */ 
  private NodeID  pTargetID;

  /**
   * The fully resolved node name of the upstream version.
   */
  private String  pSourceName;

}
  
