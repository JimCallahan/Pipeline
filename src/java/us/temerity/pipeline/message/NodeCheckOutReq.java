// $Id: NodeCheckOutReq.java,v 1.3 2004/10/21 01:23:26 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   C H E C K - O U T   R E Q                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to check-out a specific version of the given node. 
 * 
 * @see MasterMgr
 */
public
class NodeCheckOutReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * If the <CODE>vid</CODE> argument is <CODE>null</CODE> then check-out the latest 
   * version. <P> 
   * 
   * @param id 
   *   The unique working version identifier.
   * 
   * @param vid 
   *   The revision number of the node to check-out.
   * 
   * @param mode
   *   The criteria used to determine whether nodes upstream of the root node of the check-out
   *   should also be checked-out.
   */
  public
  NodeCheckOutReq
  (
   NodeID id, 
   VersionID vid, 
   CheckOutMode mode
  )
  { 
    if(id == null) 
      throw new IllegalArgumentException
	("The working version ID cannot be (null)!");
    pNodeID = id;

    pVersionID = vid;
    pMode      = mode;
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
   * Get the revision number to check-out.
   * 
   * @return
   *   The revision number or <CODE>null</CODE> for the latest version.
   */ 
  public VersionID
  getVersionID()
  {
    return pVersionID;
  }

  /**
   * The criteria used to determine whether nodes upstream of the root node of the check-out
   * should also be checked-out.
   */ 
  public CheckOutMode 
  getMode()
  {
    return pMode;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2743956830463920732L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier.
   */ 
  private NodeID  pNodeID;

  /**
   * The revision number of this version. 
   */ 
  private VersionID  pVersionID;       

  /**
   * The criteria used to determine whether nodes upstream of the root node of the check-out
   * should also be checked-out.
   */ 
  private CheckOutMode  pMode; 

}
  
