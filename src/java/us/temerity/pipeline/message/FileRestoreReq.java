// $Id: FileRestoreReq.java,v 1.2 2005/03/23 00:35:23 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   R E S T O R E   R E Q                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to move the files extracted from the archive volume into the repository.
 */ 
public
class FileRestoreReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param archiveName 
   *   The name of the archive volume to create.
   * 
   * @param stamp
   *   The timestamp of the start of the restore operation.
   * 
   * @param name
   *   The fully resolved node name. 
   * 
   * @param vid
   *   The revision number.
   * 
   * @param symlinks
   *   The revision numbers of the existing checked-in symlinks which should target the 
   *   restored file indexed by restored filename.
   * 
   * @param targets
   *   The revision number of the targets of the restored symlinks indexed by restored 
   *   symlink filename.
   */
  public
  FileRestoreReq
  (
   String archiveName, 
   Date stamp, 
   String name, 
   VersionID vid, 
   TreeMap<File,TreeSet<VersionID>> symlinks, 
   TreeMap<File,VersionID> targets
  )
  {
    if(archiveName == null) 
      throw new IllegalArgumentException
	("The volume name cannot be (null)!");
    pArchiveName = archiveName; 

    if(stamp == null) 
      throw new IllegalArgumentException
	("The timestamp cannot be (null)!");
    pTimeStamp = stamp;

    if(name == null) 
      throw new IllegalArgumentException
	("The node name cannot be (null)!");
    pName = name;

    if(vid == null) 
      throw new IllegalArgumentException
	("The revision number cannot be (null)!");
    pVersionID = vid;

    if(symlinks == null) 
      throw new IllegalArgumentException
	("The symlinks cannot be (null)!");
    pSymlinks = symlinks;

    if(targets == null) 
      throw new IllegalArgumentException
	("The targets cannot be (null)!");
    pTargets = targets;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the archive volume to restore.
   */ 
  public String
  getArchiveName()
  {
    return pArchiveName; 
  }

  /**
   * Get the timestamp of the start of the restore operation.
   */ 
  public Date
  getTimeStamp() 
  {
    return pTimeStamp; 
  }

  /**
   * Gets the fully resolved node name.
   */
  public String
  getName() 
  {
    return pName; 
  }
  
  /**
   * Gets the revision number.
   */
  public VersionID
  getVersionID() 
  {
    return pVersionID;
  }
  
  /**
   * Get the revision numbers of the existing checked-in symlinks which should target 
   * the restored file indexed by restored filename.
   */ 
  public TreeMap<File,TreeSet<VersionID>>
  getSymlinks()
  {
    return pSymlinks;
  }

  /**
   * Get the revision number of the targets of the restored symlinks indexed by  
   * restored symlink filename.
   */ 
  public TreeMap<File,VersionID>
  getTargets()
  {
    return pTargets;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1711204453036517212L;
  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the archive volume to create.
   */ 
  private String pArchiveName; 

  /**
   * The timestamp of the start of the restore operation.
   */ 
  private Date  pTimeStamp; 

  /**
   * The fully resolved node name. 
   */ 
  private String pName; 

  /**
   * The revision number.
   */ 
  private VersionID  pVersionID; 

  /**
   * The revision numbers of the existing checked-in symlinks which should target 
   * the restored file indexed by restored filename.
   */ 
  private TreeMap<File,TreeSet<VersionID>>  pSymlinks; 

  /**
   * The revision number of the targets of the restored symlinks indexed by 
   * restored symlink filename.
   */ 
  private TreeMap<File,VersionID>  pTargets; 

}
  
