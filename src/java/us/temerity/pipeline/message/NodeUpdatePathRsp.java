// $Id: NodeUpdatePathRsp.java,v 1.1 2004/05/02 12:13:34 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   U P D A T E   P A T H   R S P                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link NodeUpdatePathReq NodeUpdatePathReq} request.
 */
public
class NodeUpdatePathRsp
  extends TimedRsp
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new response.
   * 
   * @param timer 
   *   The timing statistics for a task.
   * 
   * @param id 
   *   The unique working path identifier.
   * 
   * @param rootComp
   *   The root node path component.
   */ 
  public
  NodeUpdatePathRsp
  (
   TaskTimer timer, 
   NodeID id, 
   NodeTreeComp rootComp
  )
  { 
    super(timer);

    if(id == null) 
      throw new IllegalArgumentException("The working path ID cannot be (null)!");
    pNodeID = id;

    if(rootComp == null) 
      throw new IllegalArgumentException("The root node path component cannot be (null)!");
    pRootComp = rootComp;

    Logs.net.finest("NodeMgr.updatePath(): " + id + ":\n  " + getTimer());
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
  
  /**
   * Gets the root node path component.
   */
  public NodeTreeComp
  getRootComp() 
  {
    return pRootComp;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6393998855686540613L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working path identifier.
   */ 
  private NodeID  pNodeID;

  /**
   * The root node path component.
   */
  private NodeTreeComp pRootComp;
  
}
  
