// $Id: NodeCheckOutReq.java,v 1.2 2004/05/21 21:17:51 jim Exp $

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
   * @param keepNewer
   *   Should upstream nodes which have a newer revision number than the version to be 
   *   checked-out be skipped? 
   */
  public
  NodeCheckOutReq
  (
   NodeID id, 
   VersionID vid, 
   boolean keepNewer
  )
  { 
    if(id == null) 
      throw new IllegalArgumentException
	("The working version ID cannot be (null)!");
    pNodeID = id;

    pVersionID = vid;
    pKeepNewer = keepNewer;
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
   * Should upstream nodes which have a newer revision number than the version to be 
   * checked-out be skipped? 
   */ 
  public boolean 
  keepNewer()
  {
    return pKeepNewer;
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
   * Should upstream nodes which have a newer revision number than the version to be 
   * checked-out be skipped? 
   */ 
  private boolean  pKeepNewer; 

}
  
