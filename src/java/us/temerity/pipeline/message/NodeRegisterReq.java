// $Id: NodeRegisterReq.java,v 1.1 2004/03/26 04:38:06 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   R E G I S T E R   R E Q                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to register an initial working version of a node.
 * 
 * @see NodeMgr
 */
public
class NodeRegisterReq
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
   *   The initial working version to register.
   */
  public
  NodeRegisterReq
  (
   NodeID id, 
   NodeMod mod   
  )
  { 
    if(id == null) 
      throw new IllegalArgumentException("The working version ID cannot be (null)!");
    pNodeID = id;

    if(mod == null) 
      throw new IllegalArgumentException("The intial working version cannot be (null)!");
    pNodeMod = mod;
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
   * Gets the intial working version to register.
   */
  public NodeMod
  getNodeMod() 
  {
    return pNodeMod;
  }
    

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  //private static final long serialVersionUID = -2246398459232737354L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier.
   */ 
  private NodeID  pNodeID;

  /**
   * The intial working version to register.
   */
  private NodeMod  pNodeMod;

}
  
