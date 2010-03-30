// $Id: NodeRemoveSecondaryReq.java,v 1.2 2006/01/15 06:29:25 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   R E M O V E   S E C O N D A R Y   R E Q                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to remove a secondary file sequence to the given working version.
 * 
 * @see MasterMgr
 */
public
class NodeRemoveSecondaryReq
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
   * @param fseq
   *   The secondary file sequence to remove.
   */
  public
  NodeRemoveSecondaryReq
  (
   NodeID id, 
   FileSeq fseq
  )
  { 
    super();

    if(id == null) 
      throw new IllegalArgumentException
	("The working version ID cannot be (null)!");
    pNodeID = id;

    if(fseq == null) 
      throw new IllegalArgumentException
	("The secondary file sequence cannot be (null)!");
    pFileSeq = fseq;
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
   * Gets the secondary file sequence to remove.
   */
  public FileSeq
  getFileSequence()
  {
    return pFileSeq; 
  }

    

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -9150515050853308364L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier.
   */ 
  private NodeID  pNodeID;

  /**
   * The secondary file sequence to remove.
   */
  private FileSeq  pFileSeq;

}
  
