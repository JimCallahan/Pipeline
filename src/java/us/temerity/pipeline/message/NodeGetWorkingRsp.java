// $Id: NodeGetWorkingRsp.java,v 1.6 2004/06/14 22:31:33 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   G E T   W O R K I N G   R S P                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link NodeGetWorkingReq NodeGetWorkingReq} request.
 */
public
class NodeGetWorkingRsp
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
   * @param mod
   *   The working version.
   */
  public
  NodeGetWorkingRsp
  (
   TaskTimer timer, 
   NodeID id, 
   NodeMod mod
  )
  { 
    super(timer);

    if(id == null) 
      throw new IllegalArgumentException("The working version ID cannot be (null)!");
    pNodeID = id;

    if(mod == null) 
      throw new IllegalArgumentException("The working version cannot be (null)!");
    pNodeMod = mod;

    Logs.net.finest("MasterMgr.getWorkingVersion(): " + id + ":\n  " + getTimer());
    if(Logs.net.isLoggable(Level.FINEST))
      Logs.flush();
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



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5359955962985974211L;

  

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

}
  
