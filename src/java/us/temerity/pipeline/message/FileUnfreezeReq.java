// $Id: FileUnfreezeReq.java,v 1.2 2004/03/16 16:13:55 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   U N F R E E Z E   R E Q                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to replace the symlinks associated with the working version with copies of the 
 * respective checked-in files which are the current targets of the symlinks. <P> 
 * 
 * The checksum symlinks associated with the working version files will also be replaced by 
 * copies of the checked-in checksum files which are the current targets of the checksum 
 * symlinks.
 * 
 * @see NodeMgr
 */
public
class FileUnfreezeReq
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
  FileUnfreezeReq
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

  private static final long serialVersionUID = -646063136941905676L;

  

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
  
