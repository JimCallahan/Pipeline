// $Id: FileChangeModeReq.java,v 1.1 2004/11/03 18:16:31 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   C H A N G E   M O D E   R E Q                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to change the user write permission of all existing files associated with 
 * the given working version.
 */
public
class FileChangeModeReq
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
   *   The file sequences associated with the working version.
   * 
   * @param writeable
   *   Whether the working area files should be made writable by the owning user.
   */
  public
  FileChangeModeReq
  (
   NodeID id, 
   TreeSet<FileSeq> fseqs, 
   boolean writeable
  )
  { 
    if(id == null) 
      throw new IllegalArgumentException("The working version ID cannot be (null)!");
    pNodeID = id;

    if(fseqs == null) 
      throw new IllegalArgumentException("The working file sequences cannot (null)!");
    pFileSeqs = fseqs;

    pWritable = writeable;
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

  /**
   * Get whether the working area files should be made writable by the owning user.
   */ 
  public boolean 
  getWritable() 
  {
    return pWritable;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -1450653270538077223L;

  

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

  /**
   * Whether the working area files should be made writable by the owning user.
   */ 
  private boolean  pWritable; 
}
  
