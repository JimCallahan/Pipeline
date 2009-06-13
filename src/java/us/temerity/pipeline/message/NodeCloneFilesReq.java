// $Id: NodeCloneFilesReq.java,v 1.4 2009/06/13 22:59:29 jesse Exp $

package us.temerity.pipeline.message;

import java.util.*;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   C L O N E   F I L E S   R E Q                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Replace the primary files associated one node with the primary files of another node.
 */
public
class NodeCloneFilesReq
  extends PrivilegedReq
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
    this(sourceID, targetID, null, null, null);
  }

  /** 
   * Constructs a new request. <P> 
   * 
   * @param sourceID
   *   The unique working version identifier of the node owning the files being copied. 
   * 
   * @param targetID
   *   The unique working version identifier of the node owning the files being replaced.
   *   
   * @param secondarySequences
   *   A map whose keys are the secondary sequences whose files will be copied, with the 
   *   corresponding file sequence in the target node as the value.  This can be set to
   *   <code>null</code> if there are no secondary sequences whose files should be copied.
   */
  public
  NodeCloneFilesReq
  (
   NodeID sourceID, 
   NodeID targetID,
   TreeMap<FileSeq, FileSeq> secondarySequences
  )
  {
    this(sourceID, targetID, secondarySequences, null, null);
  }

  
  /** 
   * Constructs a new request. <P> 
   * 
   * @param sourceID
   *   The unique working version identifier of the node owning the files being copied. 
   * 
   * @param targetID
   *   The unique working version identifier of the node owning the files being replaced.
   *   
   * @param secondarySequences
   *   A map whose keys are the secondary sequences whose files will be copied, with the 
   *   corresponding file sequence in the target node as the value.  This can be set to
   *   <code>null</code> if there are no secondary sequences whose files should be copied.
   *   
   * @param sourceRange
   *   The frame range in the source node that will be copied.  If this is <code>null</code>
   *   then the entire frame range of the node will be copied.  If this range ends up having 
   *   a different number of frames than the targetRange, this request will fail.
   * 
   * @param targetRange
   *   The frame range in the target node what will be copied to.  If this is 
   *   <code>null</code> then the entire frame range of the node will be copied to.  If this 
   *   range ends up having a different number of frames than the sourceRange, this request
   *   will fail.
   */
  public
  NodeCloneFilesReq
  (
   NodeID sourceID, 
   NodeID targetID,
   TreeMap<FileSeq, FileSeq> secondarySequences,
   FrameRange sourceRange,
   FrameRange targetRange
  )
  {
    super();

    if(sourceID == null) 
      throw new IllegalArgumentException
        ("The source node ID cannot be (null)!");
    pSourceID = sourceID;

    if(targetID == null) 
      throw new IllegalArgumentException
        ("The target node ID cannot be (null)!");
    pTargetID = targetID;
    
    pSecondarySequences = secondarySequences;
    
    pSourceRange = sourceRange;
    pTargetRange = targetRange;
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
  
  /**
   * Get the map whose keys are the secondary sequences whose files will be copied, with the 
   * corresponding file sequence in the target node as the value.
   */
  public TreeMap<FileSeq, FileSeq>
  getSecondarySequences()
  {
    return pSecondarySequences;
  }
  
  /**
   * Get the frame range in the source node that will be copied.
   * <p>
   * This can be <code>null</code> if the entire frame range should be copied.
   */
  public FrameRange
  getSourceRange()
  {
    return pSourceRange;
  }
  
  /**
   * Get the frame range in the target node that will be copied.
   * <p>
   * This can be <code>null</code> if the entire frame range should be copied.
   */
  public FrameRange
  getTargetRange()
  {
    return pTargetRange;
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

  /**
   * A map whose keys are the secondary sequences whose files will be copied, with the 
   * corresponding file sequence in the target node as the value.
   */
  private TreeMap<FileSeq, FileSeq> pSecondarySequences;
  
  /**
   * The frame range in the source that will be copied.
   */
  private FrameRange pSourceRange;
  
  /**
   * The frame range in the target that will be copied to.
   */
  private FrameRange pTargetRange;
}
  
