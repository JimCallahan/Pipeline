// $Id: NodeRemoveFilesReq.java,v 1.2 2004/09/03 11:00:48 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   R E M O V E   F I L E S   R E Q                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to remove the working area files associated with the given node. <P>
 * 
 * @see MasterMgr
 */
public
class NodeRemoveFilesReq
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
   * @param indices
   *   The file sequence indices of the files to remove or <CODE>null</CODE> to 
   *   remove all files.
   */
  public
  NodeRemoveFilesReq
  (
   NodeID id,
   TreeSet<Integer> indices
  )
  { 
    if(id == null) 
      throw new IllegalArgumentException
	("The working version ID cannot be (null)!");
    pNodeID = id;

    pIndices = indices; 
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
   * Get the file sequence indices to remove or <CODE>null</CODE> to remove all files.
   */ 
  public TreeSet<Integer>
  getIndices()
  {
    return pIndices;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5088222997746615642L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier.
   */ 
  private NodeID  pNodeID;

  /**
   * The file sequence indices to remove or <CODE>null</CODE> to remove all files.
   */ 
  private TreeSet<Integer>  pIndices; 
}
  
