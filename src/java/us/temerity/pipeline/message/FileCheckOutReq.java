// $Id: FileCheckOutReq.java,v 1.8 2004/11/03 18:16:31 jim Exp $

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
   * @param writeable
   *   Whether the working area files should be made writable after being checked-out.
   */
  public
  FileCheckOutReq
  (
   NodeID id, 
   VersionID vid, 
   TreeSet<FileSeq> fseqs, 
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
   * Get whether the working area files should be made writable after being checked-out.
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
   * Whether the working area files should be made writable after being checked-out.
   */ 
  private boolean  pWritable; 

}
  
