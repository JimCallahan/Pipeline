// $Id: NodeGetWorkingRsp.java,v 1.8 2005/01/22 06:10:10 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

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

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.getWorkingVersion(): " + id + ":\n  " + getTimer());
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
  
