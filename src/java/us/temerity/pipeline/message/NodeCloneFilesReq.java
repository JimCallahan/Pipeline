// $Id: NodeCloneFilesReq.java,v 1.1 2005/03/30 20:37:29 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   C L O N E   F I L E S   R E Q                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Replace the primary files associated one node with the primary files of another node.
 */
public
class NodeCloneFilesReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param sourceID
   *   The unique working version identifier of the node owning the files being copied. 
   * 
   * @param targetID
   *   The unique working version identifier of the node owning the files being replaced.
   */
  public
  NodeCloneFilesReq
  (
   NodeID sourceID, 
   NodeID targetID
  )
  { 
    if(sourceID == null) 
      throw new IllegalArgumentException
	("The source node ID cannot be (null)!");
    pSourceID = sourceID;

    if(targetID == null) 
      throw new IllegalArgumentException
	("The target node ID cannot be (null)!");
    pTargetID = targetID;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the unique working version identifier of the node owning the files being copied. 
   */
  public NodeID
  getSourceID() 
  {
    return pSourceID; 
  }

  /**
   * Gets the unique working version identifier of the node owning the files being replaced. 
   */
  public NodeID
  getTargetID() 
  {
    return pTargetID; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8779293303217062803L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier of the node owning the files being copied.
   */ 
  private NodeID  pSourceID;

  /**
   * The unique working version identifier of the node owning the files being replaced. 
   */ 
  private NodeID  pTargetID;

}
  
