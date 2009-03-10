// $Id: NodeCloneFilesReq.java,v 1.3 2009/03/10 16:47:05 jesse Exp $

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
    this(sourceID, targetID, null);
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

}
  
