// $Id: NodeSubmitJobsReq.java,v 1.1 2004/08/04 01:43:45 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   S U B M I T   J O B S   R E Q                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to Submit the group of jobs needed to regenerate the selected 
 * {@link QueueState#Stale Stale} files associated with the tree of nodes rooted at the 
 * given node. <P> 
 * 
 * @see MasterMgr
 */
public
class NodeSubmitJobsReq
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
   *   The file sequence indices of the files to regenerate or <CODE>null</CODE> to 
   *   regenerate all <CODE>Stale</CODE> or <CODE>Missing</CODE> files.
   */
  public
  NodeSubmitJobsReq
  (
   NodeID id, 
   int[] indices   
  )
    throws PipelineException
  { 
    if(id == null) 
      throw new IllegalArgumentException
	("The working version ID cannot be (null)!");
    pNodeID = id;

    pFileIndices = indices; 
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
   * Gets the file sequence indices of the files to regenerate or <CODE>null</CODE> to 
   * regenerate all <CODE>Stale</CODE> or <CODE>Missing</CODE> files.
   */
  public int[]
  getFileIndices() 
  {
    return pFileIndices; 
  }

    

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6307564113360285494L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier.
   */ 
  private NodeID  pNodeID;

  /**
   * The file sequence indices of the files to regenerate or <CODE>null</CODE> to 
   * regenerate all <CODE>Stale</CODE> or <CODE>Missing</CODE> files.
   */
  private int[]  pFileIndices; 

}
  
