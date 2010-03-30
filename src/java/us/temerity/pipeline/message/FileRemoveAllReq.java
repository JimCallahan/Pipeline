// $Id: FileRemoveAllReq.java,v 1.1 2004/07/18 21:38:18 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   R E M O V E   A L L   R E Q                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to remove all of the files associated with the given working version.
 */
public
class FileRemoveAllReq
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
   * @param fseqs 
   *   The primary and secondary file sequences associated with the working version.
   */
  public
  FileRemoveAllReq
  (
   NodeID id, 
   TreeSet<FileSeq> fseqs
  )
  { 
    if(id == null) 
      throw new IllegalArgumentException("The working version ID cannot be (null)!");
    pNodeID = id;

    if(fseqs == null) 
      throw new IllegalArgumentException("The working file sequences cannot (null)!");
    pFileSeqs = fseqs;
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
   * Gets the primary and secondary file sequences associated with the working version.
   */
  public TreeSet<FileSeq>
  getFileSequences() 
  {
    return pFileSeqs;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 6330366019365114378L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier.
   */ 
  private NodeID  pNodeID;

  /** 
   * The primary and secondary file sequences associated with the working version. 
   */
  private TreeSet<FileSeq>  pFileSeqs;
}
  
