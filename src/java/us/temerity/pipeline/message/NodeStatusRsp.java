// $Id: NodeStatusRsp.java,v 1.2 2004/04/15 00:10:09 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   S T A T U S   R S P                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link NodeStatusReq NodeStatusReq} request.
 */
public
class NodeStatusRsp
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
   *   The unique working version identifier.
   * 
   * @param status
   *   The compact summary of the state of node.
   */
  public
  NodeStatusRsp
  (
   TaskTimer timer, 
   NodeID id, 
   NodeSummary summary
  )
  { 
    super(timer);

    if(id == null) 
      throw new IllegalArgumentException("The working version ID cannot be (null)!");
    pNodeID = id;

    if(summary == null) 
      throw new IllegalArgumentException("The node state summary cannot be (null)!");
    pNodeSummary = summary;    

    Logs.net.finest("NodeMgr.status(): " + id + ":\n  " + getTimer());
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
   * Gets the compact summary of the state of node.
   */
  public NodeSummary
  getNodeSummary() 
  {
    return pNodeSummary;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3521634607879885654L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier.
   */ 
  private NodeID  pNodeID;

  /**
   * The compact summary of the state of node.
   */
  private NodeSummary  pNodeSummary;

}
  
