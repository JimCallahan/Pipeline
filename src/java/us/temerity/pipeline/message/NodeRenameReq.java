// $Id: NodeRenameReq.java,v 1.4 2004/07/18 21:32:30 jim Exp $

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
   * @param newName 
   *   The new fully resolved node name.
   * 
   * @param renameFiles
   *   Should the primary files associated with the working version be renamed?
   */
  public
  NodeRenameReq
  (
   NodeID id,
   String newName, 
   boolean renameFiles
  )
  { 
    if(id == null) 
      throw new IllegalArgumentException
	("The working version ID cannot be (null)!");
    pNodeID = id;

    if(newName == null) 
      throw new IllegalArgumentException
	("The new working version name cannot be (null)!");
    pNewName = newName;
    
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
   * Gets the new fully resolved node name.
   */
  public String
  getNewName() 
  {
    return pNewName;
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
   * The new fully resolved node name.
   */
  private String pNewName;

  /**
   * Should the primary files associated with the working version be renamed?
   */
  private boolean  pRenameFiles;

}
  
