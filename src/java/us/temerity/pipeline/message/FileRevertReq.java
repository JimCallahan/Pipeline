// $Id: FileRevertReq.java,v 1.2 2004/11/03 18:16:31 jim Exp $

package us.temerity.pipeline.message;

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
   * @param writeable
   *   Whether the reverted working area files should be made writable.
   */
  public
  FileRevertReq
  (
   NodeID id, 
   TreeMap<String,VersionID> files, 
   boolean writeable   
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
   * Get the table of checked-in file revision numbers indexed by file name.
   */ 
  public TreeMap<String,VersionID>
  getFiles() 
  {
    return pFiles;
  }

  /**
   * Get whether the reverted working area files should be made writable.
   */ 
  public boolean 
  getWritable() 
  {
    return pWritable;
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
   * Whether the reverted working area files should be made writable.
   */ 
  private boolean  pWritable; 

}
  
