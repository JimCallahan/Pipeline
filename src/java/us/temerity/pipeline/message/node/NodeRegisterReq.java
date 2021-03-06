// $Id: NodeRegisterReq.java,v 1.6 2006/01/15 06:29:25 jim Exp $

package us.temerity.pipeline.message.node;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   R E G I S T E R   R E Q                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to register an initial working version of a node.
 * 
 * @see MasterMgr
 */
public
class NodeRegisterReq
  extends PrivilegedReq
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
    throws PipelineException
  { 
    super();

    if(id == null) 
      throw new IllegalArgumentException
	("The working version ID cannot be (null)!");
    pNodeID = id;

    if(mod == null) 
      throw new IllegalArgumentException
	("The intial working version cannot be (null)!");
    pNodeMod = mod;

    if(mod.getWorkingID() != null) 
      throw new PipelineException
	("The working version was not an initial working version!");
    
    if(mod.hasSources()) 
      throw new PipelineException
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

  private static final long serialVersionUID = -3527421380050702980L;

  

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
  
