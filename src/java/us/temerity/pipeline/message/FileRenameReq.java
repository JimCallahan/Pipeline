// $Id: FileRenameReq.java,v 1.1 2004/03/30 22:19:18 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   R E N A M E   R E Q                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to rename the primary sequence files associated with the given working version.
 */
public
class FileRenameReq
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
   * @param fseqs 
   *   The primary and secondary file sequences associated with the working version.
   */
  public
  FileRenameReq
  (
   NodeID id, 
   TreeSet<FileSeq> fseqs, 
   String newName
  )
  { 
    if(id == null) 
      throw new IllegalArgumentException("The working version ID cannot be (null)!");
    pNodeID = id;

    if(fseqs == null) 
      throw new IllegalArgumentException("The working file sequences cannot (null)!");
    pFileSeqs = fseqs;

    if(newName == null) 
      throw new IllegalArgumentException("The new node name cannot be (null)!");
    pNewName = newName;
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
   * Gets the new fully resolved node name.
   */
  public String
  getNewName() 
  {
    return pNewName;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 6660115779864437868L;

  

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
   * The new fully resolved node name.
   */
  private String  pNewName;

}
  
