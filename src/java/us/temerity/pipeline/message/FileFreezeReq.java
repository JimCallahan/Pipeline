// $Id: FileFreezeReq.java,v 1.2 2004/03/22 20:29:16 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   F R E E Z E   R E Q                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to replace the files associated with the working version with symlinks to the
 * respective files associated with a specific checked-in version. <P> 
 * 
 * The checksum files associated with the working version files will also be replaced by 
 * symlinks to the checked-in checksum files associated with the checked-in version.
 * 
 * @see NodeMgr
 * @see CheckSum
 */
public
class FileFreezeReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param id [<B>in</B>]
   *   The unique working version identifier.
   * 
   * @param vid [<B>in</B>]
   *   The revision number of the checked-in version which is the target of the symlinks.
   * 
   * @param fseqs [<B>in</B>]
   *   The primary and secondary file sequences associated with the working version.
   */
  public
  FileFreezeReq
  (
   NodeID id, 
   VersionID vid, 
   TreeSet<FileSeq> fseqs
  )
  { 
    if(id == null) 
      throw new IllegalArgumentException("The working version ID cannot be (null)!");
    pNodeID = id;

    if(vid == null) 
      throw new IllegalArgumentException("The revision number cannot be (null)!");
    pVersionID = vid;

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
   * Gets the revision number of the checked-in version which is the target of the symlinks.
   */
  public VersionID
  getVersionID() 
  {
    return pVersionID;
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

  private static final long serialVersionUID = -2246398459232737354L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier.
   */ 
  private NodeID  pNodeID;

  /**
   * The revision number of the checked-in version which is the target of the symlinks.
   */
  private VersionID  pVersionID;

  /** 
   * The primary and secondary file sequences associated with the working version. 
   */
  private TreeSet<FileSeq>  pFileSeqs;
}
  
