// $Id: NodeRevokeReq.java,v 1.1 2004/03/30 22:19:18 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   R E V O K E   R E Q                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to revoke a working version which has not ever been checked-in.
 * 
 * @see NodeMgr
 */
public
class NodeRevokeReq
  implements Serializable
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
   * @param removeFiles
   *   Should the files associated with the working version be deleted?
   */
  public
  NodeRevokeReq
  (
   NodeID id,
   boolean removeFiles
  )
  { 
    if(id == null) 
      throw new IllegalArgumentException
	("The downstream working version ID cannot be (null)!");
    pNodeID = id;

    pRemoveFiles = removeFiles;
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
   * Should the files associated with the working version be deleted?
   */
  public boolean
  removeFiles()
  {
    return pRemoveFiles;
  }

    

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1107991782923860807L;
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier.
   */ 
  private NodeID  pNodeID;

  /**
   * Should the files associated with the working version be deleted?
   */
  private boolean  pRemoveFiles;

}
  
