// $Id: FileCheckInReq.java,v 1.6 2004/07/07 13:21:45 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   C H E C K - I N   R E Q                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to perform the file system operations needed to create a new checked-in 
 * version in the file repository. <P> 
 * 
 * If the checksum files corresponding to the working files being checked-in are either 
 * missing or older than their respective working file, the checksums will be regenerated. <P>
 * 
 * If the working file is identical to the respective latest checked file, a symbolic link 
 * will be created in the file repository which points to the latest version of the file
 * which is a regular file.  This target file may be located in an earlier checked-in 
 * version if the file has not changed over the last several checked-in versions.  If the 
 * working file is different than the latest checked-in file, it will be copied into the 
 * file repository. <P> 
 * 
 * The checksum files corresponding to the working files are processed in a similar manner
 * as the working files.  If they are identical to the checked-in checksums, a symlink is 
 * created to the latest regular checksum file.  Otherwise the checksum is copied into the 
 * file repository.
 */
public
class FileCheckInReq
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
   *   The revision number of the new checked-in version.
   * 
   * @param latest 
   *   The revision number of the latest checked-in version.
   * 
   * @param fseqs 
   *   The primary and secondary file sequences associated with the working version.
   * 
   * @param isNovel
   *   Whether each file associated with the version contains new data not present in the
   *   previous checked-in version.
   */
  public
  FileCheckInReq
  (
   NodeID id, 
   VersionID vid, 
   VersionID latest, 
   TreeSet<FileSeq> fseqs, 
   TreeMap<FileSeq,boolean[]> isNovel
  )
  { 
    if(id == null) 
      throw new IllegalArgumentException("The working version ID cannot be (null)!");
    pNodeID = id;

    if(vid == null) 
      throw new IllegalArgumentException("The new revision number cannot be (null)!");
    pVersionID = vid;

    pLatestVersionID = latest;

    if(fseqs == null) 
      throw new IllegalArgumentException("The working file sequences cannot (null)!");
    pFileSeqs = fseqs;
    
    if(isNovel == null) 
      throw new IllegalArgumentException("The working file states cannot (null)!");
    pIsNovel = isNovel;
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
   * Gets the revision number of the new checked-in version.
   */
  public VersionID
  getVersionID() 
  {
    return pVersionID;
  }
  
  /**
   * Gets the revision number of the latest checked-in version.
   */
  public VersionID
  getLatestVersionID() 
  {
    return pLatestVersionID;
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
   * Gets whether each file associated with the version contains new data not present in the
   * previous checked-in version.
   */
  public TreeMap<FileSeq,boolean[]> 
  getIsNovel()
  {
    return pIsNovel;
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4676943881001731969L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier.
   */ 
  private NodeID  pNodeID;

  /**
   * The revision number of the new checked-in version.
   */
  private VersionID  pVersionID;

  /**
   * The revision number of the latest checked-in version.  
   */
  private VersionID  pLatestVersionID;

  /** 
   * The primary and secondary file sequences associated with the working version. 
   */
  private TreeSet<FileSeq>  pFileSeqs;

  /** 
   * Whether each file associated with the version contains new data not present in the
   * previous checked-in version.
   */
  private TreeMap<FileSeq,boolean[]>  pIsNovel;

}
  
