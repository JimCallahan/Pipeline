// $Id: NodeSetLastCTimeUpdateReq.java,v 1.1 2010/01/22 00:14:33 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   S E T   L A S T   C T I M E   U P D A T E   R E Q                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Set the LastCTimeUpdate property of a working version. <P> 
 */
public
class NodeSetLastCTimeUpdateReq
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
   * @param stamp
   *   The new timestamp to give the LastCTimeUpdate property.
   */
  public
  NodeSetLastCTimeUpdateReq
  (
   NodeID id, 
   long stamp
  )
  { 
    super();

    if(id == null) 
      throw new IllegalArgumentException
	("The working version ID cannot be (null)!");
    pNodeID = id;

    if(stamp <= 0L) 
      throw new IllegalArgumentException
        ("The timestamp must be positive!"); 
    pTimeStamp = stamp;
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
   * Gets the new timestamp to give the LastCTimeUpdate property.
   */
  public long
  getTimeStamp() 
  {
    return pTimeStamp; 
  }

    

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  ///  private static final long serialVersionUID = ;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier.
   */ 
  private NodeID  pNodeID;

  /**
   * The new timestamp to give the LastCTimeUpdate property.
   */
  private long pTimeStamp; 

}
  
