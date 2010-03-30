// $Id: NodeStatusReq.java,v 1.7 2008/09/29 19:02:18 jim Exp $

package us.temerity.pipeline.message.node;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   S T A T U S   R E Q                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to obtain the overall node status of the given node.
 * 
 * @see MasterMgr
 */
public
class NodeStatusReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param nodeID 
   *   The unique working version identifier.
   * 
   * @param lightweight
   *   Whether to report only lightweight node status detail information for the 
   *   upstream nodes.
   * 
   * @param dmode
   *   The criteria used to determine how downstream node status is reported.
   */
  public
  NodeStatusReq
  (
   NodeID nodeID, 
   boolean lightweight,
   DownstreamMode dmode   
  )
  { 
    if(nodeID == null) 
      throw new IllegalArgumentException
	("The working version ID cannot be (null)!");
    pNodeID = nodeID;

    pLightweight = lightweight;

    if(dmode == null) 
      throw new IllegalArgumentException
	("The downstream mode cannot be (null)!");
    pDownstreamMode = dmode;
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
   * Whether to report only lightweight node status detail information for the upstream nodes.
   */
  public boolean
  getLightweight() 
  {
    return pLightweight;
  }

  /**
   * The criteria used to determine how downstream node status is reported.
   */ 
  public DownstreamMode 
  getDownstreamMode()
  {
    return pDownstreamMode;
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8893807724022964651L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier.
   */ 
  private NodeID  pNodeID;
  
  /**
   * Whether to report only lightweight node status detail information for the upstream nodes.
   */
  private boolean pLightweight; 
  
  /**
   * The criteria used to determine how downstream node status is reported.
   */ 
  private DownstreamMode pDownstreamMode;
  
}
  
