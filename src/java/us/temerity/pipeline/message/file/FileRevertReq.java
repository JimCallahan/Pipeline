// $Id: FileRevertReq.java,v 1.3 2009/07/11 10:54:21 jim Exp $

package us.temerity.pipeline.message.file;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   R E V E R T   R E Q                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to revert specific working area files to an earlier checked-in version of 
 * the files.
 * 
 * @see MasterMgr
 */
public
class FileRevertReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param id 
   *   The unique working version identifier.
   * 
   * @param files
   *   The table of checked-in file revision numbers indexed by file name.
   * 
   * @param isLinked
   *   Whether the files associated with the working version should be symlinks to the 
   *   checked-in files instead of copies.
   */
  public
  FileRevertReq
  (
   NodeID id, 
   TreeMap<String,VersionID> files, 
   boolean isLinked
  )
  { 
    if(id == null) 
      throw new IllegalArgumentException
	("The working version ID cannot be (null)!");
    pNodeID = id;

    if(files == null) 
      throw new IllegalArgumentException
	("The files to revert cannot be (null)!");
    pFiles = files;

    pIsLinked = isLinked;
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
   * Get the table of checked-in file revision numbers indexed by file name.
   */ 
  public TreeMap<String,VersionID>
  getFiles() 
  {
    return pFiles;
  }

  /**
   * Get whether the files associated with the working version should be symlinks to the 
   * checked-in files instead of copies.
   */ 
  public boolean 
  isLinked() 
  {
    return pIsLinked; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7064549770272479550L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier.
   */ 
  private NodeID  pNodeID;

  /**
   * The table of checked-in file revision numbers indexed by file name.
   */ 
  private TreeMap<String,VersionID>  pFiles;

  /**
   * Whether the files associated with the working version should be symlinks to the 
   * checked-in files instead of copies.
   */ 
  private boolean pIsLinked; 

}
  
