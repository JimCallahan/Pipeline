// $Id: QueueGetJobStatesReq.java,v 1.3 2007/03/28 19:56:42 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   J O B   S T A T E S   R E Q                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the jobs IDs and states for each primary file of the given working 
 * version. <P> 
 */
public
class QueueGetJobStatesReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param id
   *   The unique working version identifier.
   *
   * @param stamp
   *   The timestamp of when the working version was created.
   * 
   * @param fseq
   *   The primary file sequence.
   */
  public
  QueueGetJobStatesReq
  (
   NodeID id, 
   long stamp, 
   FileSeq fseq
  )
  { 
    if(id == null) 
      throw new IllegalArgumentException("The node ID cannot be (null)!");
    pNodeID = id;

    pTimeStamp = stamp;

    if(id == null) 
      throw new IllegalArgumentException("The primary file sequence cannot be (null)!");    
    pFileSeq = fseq; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the unique working version identifier.
   */
  public NodeID
  getNodeID() 
  {
    return pNodeID; 
  }

  /**
   * Get when the working version was created.
   */ 
  public long
  getTimeStamp() 
  {
    return pTimeStamp;
  }

  /**
   * Get the primary file sequence.
   */
  public FileSeq
  getFileSeq() 
  {
    return pFileSeq; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8731338585550002217L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier.
   */ 
  private NodeID  pNodeID; 

  /** 
   * The timestamp of when the version was created.
   */
  private long  pTimeStamp;

  /**
   * The primary file sequence.
   */ 
  private FileSeq  pFileSeq; 

}
  
