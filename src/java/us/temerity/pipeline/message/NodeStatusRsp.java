// $Id: NodeStatusRsp.java,v 1.7 2005/01/22 01:36:36 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;
import java.util.logging.*;

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
   *   The status of the node.
   */
  public
  NodeStatusRsp
  (
   TaskTimer timer, 
   NodeID id, 
   NodeStatus status
  )
  { 
    super(timer);

    if(id == null) 
      throw new IllegalArgumentException("The working version ID cannot be (null)!");
    pNodeID = id;

    if(status == null) 
      throw new IllegalArgumentException("The node status cannot be (null)!");
    pNodeStatus = status;    

    LogMgr.getInstance().log
(LogMgr.Kind.Net, LogMgr.Level.Finest,
"MasterMgr.status(): " + id + ":\n  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
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
   * Gets the status of the node.
   */
  public NodeStatus
  getNodeStatus() 
  {
    return pNodeStatus;
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
   * The status of the node.
   */
  private NodeStatus  pNodeStatus;

}
  
