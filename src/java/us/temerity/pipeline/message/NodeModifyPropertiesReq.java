// $Id: NodeModifyPropertiesReq.java,v 1.1 2004/03/28 00:49:56 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M O D I F Y   P R O P E R T I E S   R E Q                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to set the node properties of the working version of the node. <P> 
 * 
 * @see NodeMgr
 */
public
class NodeModifyPropertiesReq
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
   * @param mod
   *   The working version containing the node property information to copy.
   */
  public
  NodeModifyPropertiesReq
  (
   NodeID id, 
   NodeMod mod   
  )
  { 
    if(id == null) 
      throw new IllegalArgumentException
	("The working version ID cannot be (null)!");
    pNodeID = id;

    if(mod == null) 
      throw new IllegalArgumentException
	("The intial working version cannot be (null)!");
    pNodeMod = mod;

    if(mod.getWorkingID() != null) 
      throw new IllegalArgumentException
	("The working version was not an initial working version!");
    
    if(mod.hasSources()) 
      throw new IllegalArgumentException
	("The working version already has links to upstream nodes!");
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
   * Gets the working version containing the node property information to copy.
   */
  public NodeMod
  getNodeMod() 
  {
    return pNodeMod;
  }

    

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  // private static final long serialVersionUID = -3527421380050702980L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier.
   */ 
  private NodeID  pNodeID;

  /**
   * The working version containing the node property information to copy.
   */
  private NodeMod  pNodeMod;

}
  
