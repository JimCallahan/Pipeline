// $Id: NodeRevertFilesReq.java,v 1.2 2006/01/15 06:29:25 jim Exp $

package us.temerity.pipeline.message.node;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   R E V E R T   F I L E S   R E Q                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to revert specific working area files to an earlier checked-in version of 
 * the files.
 * 
 * @see MasterMgr
 */
public
class NodeRevertFilesReq
  extends PrivilegedReq
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
   */
  public
  NodeRevertFilesReq
  (
   NodeID id, 
   TreeMap<String,VersionID> files
  )
  { 
    super();

    if(id == null) 
      throw new IllegalArgumentException
	("The working version ID cannot be (null)!");
    pNodeID = id;

    if(files == null) 
      throw new IllegalArgumentException
	("The files to revert cannot be (null)!");
    pFiles = files;
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

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2628813044587732280L;

  

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

}
  
