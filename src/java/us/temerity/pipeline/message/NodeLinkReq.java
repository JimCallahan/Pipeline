// $Id: NodeLinkReq.java,v 1.1 2004/03/29 08:19:16 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   L I N K   R E Q                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to create or modify an existing link between the working versions.
 * 
 * @see NodeMgr
 */
public
class NodeLinkReq
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
   * @param link
   *   The link to the upstream version.
   */
  public
  NodeLinkReq
  (
   NodeID id, 
   LinkMod link
  )
  { 
    if(id == null) 
      throw new IllegalArgumentException
	("The downstream working version ID cannot be (null)!");
    pTargetID = id;

    if(link == null) 
      throw new IllegalArgumentException
	("The upstream link cannot be (null)!");
    pSourceLink = link;
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
   * Gets the link to the upstream version.
   */
  public LinkMod
  getSourceLink() 
  {
    return pSourceLink;
  }

    

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1789233220318866137L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier of the downstream version.
   */ 
  private NodeID  pTargetID;

  /**
   * The link to the upstream version.
   */
  private LinkMod  pSourceLink;

}
  
