// $Id: FileCheckOutReq.java,v 1.9 2004/11/17 13:33:50 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   C H E C K - O U T   R E Q                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to copy the files associated with a specific checked-in version to the  
 * given working version location. <P> 
 * 
 * Existing working files may be optionally overwritten.  The checksum files corresponding
 * to the checked-out files are also copied to the respective workinve version location.
 */
public
class FileCheckOutReq
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
   * @param vid 
   *   The revision number of the checked-in version to check-out.
   * 
   * @param fseqs 
   *   The primary and secondary file sequences associated with the checked-in version to 
   *   check-out.
   * 
   * @param isFrozen
   *   Whether the files associated with the working version should be symlinks to the 
   *   checked-in files instead of copies.
   * 
   * @param writeable
   *   Whether the working area files (if not frozen) should be made writable after being 
   *   checked-out.
   */
  public
  FileCheckOutReq
  (
   NodeID id, 
   VersionID vid, 
   TreeSet<FileSeq> fseqs, 
   boolean isFrozen, 
   boolean writeable 
  )
  { 
    if(id == null) 
      throw new IllegalArgumentException("The working version ID cannot be (null)!");
    pNodeID = id;

    if(vid == null) 
      throw new IllegalArgumentException("The check-out revision number cannot be (null)!");
    pVersionID = vid;

    if(fseqs == null) 
      throw new IllegalArgumentException("The check-out file sequences cannot (null)!");
    pFileSeqs = fseqs;

    pIsFrozen = isFrozen;
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
   * Gets the revision number of the checked-in version to check-out.
   */
  public VersionID
  getVersionID() 
  {
    return pVersionID;
  }
  
  /**
   * Gets the primary and secondary file sequences associated with the checked-in version to 
   * check-out.
   */
  public TreeSet<FileSeq>
  getFileSequences() 
  {
    return pFileSeqs;
  }

  /**
   * Get whether the files associated with the working version should be symlinks to the 
   * checked-in files instead of copies.
   */ 
  public boolean 
  isFrozen() 
  {
    return pIsFrozen; 
  }

  /**
   * Get whether the working area files (if not frozen) should be made writable after 
   * being checked-out.
   */ 
  public boolean 
  getWritable() 
  {
    return pWritable;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8945665170281636791L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier.
   */ 
  private NodeID  pNodeID;

  /**
   * The revision number of the checked-in version to check-out.
   */
  private VersionID  pVersionID;

  /** 
   * The primary and secondary file sequences associated with the checked-in version to 
   * check-out.
   */
  private TreeSet<FileSeq>  pFileSeqs;

  /**
   * Whether the files associated with the working version should be symlinks to the 
   * checked-in files instead of copies.
   */ 
  private boolean pIsFrozen; 

  /**
   * Whether the working area files should be made writable after being checked-out.
   */ 
  private boolean  pWritable; 

}
  
