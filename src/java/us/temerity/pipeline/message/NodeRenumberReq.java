// $Id: NodeRenumberReq.java,v 1.2 2006/01/15 06:29:25 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   R E N U M B E R   R E Q                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to renumber of the frame ranges of the given node.
 * 
 * @see MasterMgr
 */
public
class NodeRenumberReq
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
   * @param range
   *   The new frame range.
   * 
   * @param removeFiles
   *   Whether to remove files from the old frame range which are no longer part of the new 
   *   frame range.
   */
  public
  NodeRenumberReq
  (
   NodeID id,
   FrameRange range, 
   boolean removeFiles
  )
  { 
    super();

    if(id == null) 
      throw new IllegalArgumentException
	("The working version ID cannot be (null)!");
    pNodeID = id;

    if(range == null) 
      throw new IllegalArgumentException
	("The new frame range cannot be (null)!");
    pFrameRange = range;
    
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
   * Get the new frame range.
   */ 
  public FrameRange
  getFrameRange() 
  {
    return pFrameRange;
  }
  
  /**
   * Get whether to remove files from the old frame range which are no longer part of the new 
   * frame range.
   */
  public boolean
  removeFiles() 
  {
    return pRemoveFiles;
  }

    

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4799441642316691354L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier.
   */ 
  private NodeID  pNodeID;

  /**
   * The new frame range.
   */
  private FrameRange  pFrameRange; 
  
  /**
   * Whether to remove files from the old frame range which are no longer part of the new 
   * frame range.
   */
  private boolean  pRemoveFiles;

}
  
