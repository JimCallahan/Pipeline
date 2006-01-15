// $Id: NodeRenameReq.java,v 1.6 2006/01/15 06:29:25 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   R E N A M E   R E Q                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to rename a working version which has not ever been checked-in.
 * 
 * @see MasterMgr
 */
public
class NodeRenameReq
  extends PrivilegedReq
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
   * @param pattern
   *   The new fully resolved file pattern.
   * 
   * @param renameFiles
   *   Should the primary files associated with the working version be renamed?
   */
  public
  NodeRenameReq
  (
   NodeID id,
   FilePattern pattern, 
   boolean renameFiles
  )
  { 
    super();

    if(id == null) 
      throw new IllegalArgumentException
	("The working version ID cannot be (null)!");
    pNodeID = id;

    if(pattern == null) 
      throw new IllegalArgumentException
	("The new file pattern cannot be (null)!");
    pPattern = pattern;
    
    pRenameFiles = renameFiles;
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
   * Gets the new fully resolved file pattern.
   */
  public FilePattern
  getFilePattern() 
  {
    return pPattern; 
  }
  
  /**
   * Should the primary files associated with the working version be renamed?
   */
  public boolean
  renameFiles()
  {
    return pRenameFiles;
  }

    

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4714676517455323809L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier.
   */ 
  private NodeID  pNodeID;

  /**
   * The new fully resolved file pattern.
   */
  private FilePattern  pPattern;

  /**
   * Should the primary files associated with the working version be renamed?
   */
  private boolean  pRenameFiles;

}
  
