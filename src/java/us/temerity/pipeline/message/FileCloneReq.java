// $Id: FileCloneReq.java,v 1.1 2005/03/30 20:37:29 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   C L O N E   R E Q                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Replace the primary files associated one node with the primary files of another node. <P>
 */
public
class FileCloneReq
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
   * @param sourceSeq
   *   The primary file sequence associated with the source node. 
   * 
   * @param targetID
   *   The unique working version identifier of the node owning the files being replaced.
   * 
   * @param targetSeq
   *   The primary file sequence associated with the taret node. 
   * 
   * @param writeable
   *   Whether the target node's files should be made writable.
   */
  public
  FileCloneReq
  (
   NodeID sourceID,
   FileSeq sourceSeq, 
   NodeID targetID,
   FileSeq targetSeq, 
   boolean writeable   
  )
  { 
    if(sourceID == null) 
      throw new IllegalArgumentException
	("The source node ID cannot be (null)!");
    pSourceID = sourceID;

    if(sourceSeq == null) 
      throw new IllegalArgumentException
	("The source file sequence cannot be (null)!");
    pSourceSeq = sourceSeq;

    if(targetID == null) 
      throw new IllegalArgumentException
	("The target node ID cannot be (null)!");
    pTargetID = targetID;

    if(targetSeq == null) 
      throw new IllegalArgumentException
	("The target file sequence cannot be (null)!");
    pTargetSeq = targetSeq;

    pWritable = writeable;
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
   * Gets the primary file sequence associated with the source node. 
   */
  public FileSeq
  getSourceSeq() 
  {
    return pSourceSeq; 
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
   * Gets primary file sequence associated with the target node. 
   */
  public FileSeq
  getTargetSeq() 
  {
    return pTargetSeq; 
  }


  /**
   * Get ehether the target node's files should be made writable.
   */ 
  public boolean 
  getWritable() 
  {
    return pWritable;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2248366603615390460L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier of the node owning the files being copied.
   */ 
  private NodeID  pSourceID;

  /**
   * The primary file sequence associated with the source node. 
   */ 
  private FileSeq  pSourceSeq; 

  /**
   * The unique working version identifier of the node owning the files being replaced. 
   */ 
  private NodeID  pTargetID;

  /**
   * The primary file sequence associated with the target node. 
   */ 
  private FileSeq  pTargetSeq; 


  /**
   * Whether the target node's files should be made writable.
   */ 
  private boolean  pWritable; 

}
  
