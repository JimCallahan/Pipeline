// $Id: NodeCheckInReq.java,v 1.1 2004/04/20 22:02:18 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   C H E C K - I N   R E Q                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to check-in the given node. 
 * 
 * @see NodeMgr
 */
public
class NodeCheckInReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * The <CODE>level</CODE> argument may be <CODE>null</CODE> if this is an initial 
   * working version.
   * 
   * @param id 
   *   The unique working version identifier.
   * 
   * @param msg 
   *   The check-in message text.
   * 
   * @param level  
   *   The revision number component level to increment.
   */
  public
  NodeCheckInReq
  (
   NodeID id, 
   String msg, 
   VersionID.Level level
  )
  { 
    if(id == null) 
      throw new IllegalArgumentException
	("The working version ID cannot be (null)!");
    pNodeID = id;

    if(msg == null) 
      throw new IllegalArgumentException
	("The check-in message cannot be (null)!");
    pMessage = msg;

    pLevel = level;
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
   * Get the check-in message text.
   */ 
  public String 
  getMessage() 
  {
    return pMessage;
  }

  /**
   * Get the revision number component level to increment.
   */ 
  public VersionID.Level
  getLevel() 
  {
    return pLevel;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8147485838439303943L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier.
   */ 
  private NodeID  pNodeID;

  /**
   * The check-in message text.
   */ 
  private String  pMessage;

  /**
   * The revision number component level to increment.
   */ 
  private VersionID.Level  pLevel;

}
  
