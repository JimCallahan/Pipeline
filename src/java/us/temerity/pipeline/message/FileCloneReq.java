// $Id: FileCloneReq.java,v 1.2 2005/03/30 22:42:10 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   C L O N E   R E Q                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Replace the primary files associated one node with the primary files of another node. <P>
 */
public
class FileCloneReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param sourceID
   *   The unique working version identifier of the node owning the files being copied. 
   * 
   * @param targetID
   *   The unique working version identifier of the node owning the files being replaced.
   * 
   * @param files
   *   The target files to copy indexed by corresponding source files.
   * 
   * @param writeable
   *   Whether the target node's files should be made writable.
   */
  public
  FileCloneReq
  (
   NodeID sourceID, 
   NodeID targetID,
   TreeMap<File,File> files, 
   boolean writeable   
  )
  { 
    if(sourceID == null) 
      throw new IllegalArgumentException
	("The source node ID cannot be (null)!");
    pSourceID = sourceID;

    if(targetID == null) 
      throw new IllegalArgumentException
	("The target node ID cannot be (null)!");
    pTargetID = targetID;

    if(files == null) 
      throw new IllegalArgumentException
	("The files cannot be (null)!");
    pFiles = files; 

    pWritable = writeable;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the unique working version identifier of the node owning the files being copied. 
   */
  public NodeID
  getSourceID() 
  {
    return pSourceID; 
  }

  /**
   * Gets the unique working version identifier of the node owning the files being replaced. 
   */
  public NodeID
  getTargetID() 
  {
    return pTargetID; 
  }

  /**
   * Gets the target files to copy indexed by corresponding source files.
   */
  public TreeMap<File,File> 
  getFiles() 
  {
    return pFiles; 
  }


  /**
   * Get ehether the target node's files should be made writable.
   */ 
  public boolean 
  getWritable() 
  {
    return pWritable;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2248366603615390460L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier of the node owning the files being copied.
   */ 
  private NodeID  pSourceID;

  /**
   * The unique working version identifier of the node owning the files being replaced. 
   */ 
  private NodeID  pTargetID;

  /**
   * The target files to copy indexed by corresponding source files.
   */ 
  private TreeMap<File,File>  pFiles; 


  /**
   * Whether the target node's files should be made writable.
   */ 
  private boolean  pWritable; 

}
  
