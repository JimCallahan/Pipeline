// $Id: FileRenameReq.java,v 1.4 2006/06/25 23:30:32 jim Exp $

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
   * @param primary
   *   The primary file sequence associated with the working version.
   * 
   * @param secondary
   *   The secondary file sequences associated with the working version.
   * 
   * @param pattern
   *   The new fully resolved file pattern.
   */
  public
  FileRenameReq
  (
   NodeID id, 
   FileSeq primary, 
   SortedSet<FileSeq> secondary, 
   FilePattern pattern
  )
  { 
    if(id == null) 
      throw new IllegalArgumentException("The working version ID cannot be (null)!");
    pNodeID = id;

    if(primary == null) 
      throw new IllegalArgumentException("The primary working file sequence cannot (null)!");
    pPrimary = primary;

    if(secondary == null) 
      throw new IllegalArgumentException("The secondary file sequences cannot (null)!");
    pSecondary = secondary;

    if(pattern == null) 
      throw new IllegalArgumentException
	("The new file pattern cannot be (null)!");
    pPattern = pattern;
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
   * Gets the primary file sequence associated with the working version.
   */
  public FileSeq
  getPrimarySequence() 
  {
    return pPrimary; 
  }

  /**
   * Gets the secondary file sequences associated with the working version.
   */
  public SortedSet<FileSeq>
  getSecondarySequences() 
  {
    return pSecondary;
  }

  /**
   * Gets the new fully resolved file pattern.
   */
  public FilePattern
  getFilePattern() 
  {
    return pPattern; 
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
  private FileSeq             pPrimary; 
  private SortedSet<FileSeq>  pSecondary; 

  /**
   * The new fully resolved file pattern.
   */
  private FilePattern  pPattern;

}
  
