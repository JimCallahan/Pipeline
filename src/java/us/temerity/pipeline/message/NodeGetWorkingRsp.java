// $Id: NodeGetWorkingRsp.java,v 1.1 2004/03/26 19:13:41 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   S T A T E   R S P                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link NodeGetWorkingReq NodeGetWorkingReq} request.
 */
public
class NodeGetWorkingRsp
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new response.
   * 
   * @param mod
   *   The working version.
   */
  public
  NodeGetWorkingRsp
  (
   NodeID id, 
   NodeMod mod, 
   long wait, 
   Date start
  )
  { 
    if(id == null) 
      throw new IllegalArgumentException("The working version ID cannot be (null)!");
    pNodeID = id;

    if(mod == null) 
      throw new IllegalArgumentException("The intial working version cannot be (null)!");
    pNodeMod = mod;

    pWait   = wait;
    pActive = (new Date()).getTime() - start.getTime();

    Logs.net.finest("NodeMgr.getWorkingVersion(): " + id + ": " +
		    pWait + "/" + pActive + " (msec) wait/active");
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
   * Gets the working version of the node.
   */
  public NodeMod
  getNodeMod() 
  {
    return pNodeMod;
  }
  
  /**
   * Gets the number of milliseconds spent waiting to aquire the needed locks.
   */
  public long 
  getWaitTime() 
  {
    return pWait;
  }

  /**
   * Gets the number of milliseconds spent fufilling the request.
   */
  public long
  getActiveTime() 
  {
    return pActive;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7806634912469985967L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier.
   */ 
  private NodeID  pNodeID;

  /**
   * The working version of the node.
   */
  private NodeMod  pNodeMod;

  /*
   * The number of milliseconds spent waiting to aquire the needed locks.
   */ 
  private long  pWait;

  /**
   * The number of milliseconds spent fufilling the request.
   */ 
  private long  pActive; 

}
  
