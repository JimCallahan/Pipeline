// $Id: NodeMultiStatusRsp.java,v 1.3 2009/08/19 22:48:06 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   M U L T I   S T A T U S   R S P                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link NodeMultiStatusReq} request.
 */
public
class NodeMultiStatusRsp
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
   * @param status
   *   The status of the node roots.
   */
  public
  NodeMultiStatusRsp
  (
   TaskTimer timer, 
   TreeMap<String,NodeStatus> status
  )
  { 
    super(timer);

    if(status == null) 
      throw new IllegalArgumentException("The status of the node roots cannot be (null)!");
    pNodeStatus = status;    

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.multiStatus():\n  " + getTimer());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the status of the node roots.
   */
  public TreeMap<String,NodeStatus> 
  getNodeStatus() 
  {
    return pNodeStatus;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3562838367823891780L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The status of the node roots.
   */
  private TreeMap<String,NodeStatus>  pNodeStatus;

}
  
