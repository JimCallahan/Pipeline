// $Id: NodeDeleteReq.java,v 1.1 2004/10/31 20:02:11 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   D E L E T E   R E Q                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to delete the given working version.
 * 
 * @see MasterMgr
 */
public
class NodeDeleteReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param name
   *   The fully resolved name of the node. 
   * 
   * @param removeFiles
   *   Should the files associated with the working version be deleted?
   */
  public
  NodeDeleteReq
  (
   String name, 
   boolean removeFiles
  )
  { 
    if(name == null) 
      throw new IllegalArgumentException
	("The node name cannot be (null)!");
    pName = name;

    pRemoveFiles = removeFiles;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the fully resolved name of the node. 
   */
  public String
  getName() 
  {
    return pName;
  }
  
  /**
   * Should the files associated with the working versions be deleted?
   */
  public boolean
  removeFiles()
  {
    return pRemoveFiles;
  }
    

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2513706957481448393L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved name of the node. 
   */ 
  private String  pName; 

  /**
   * Should the files associated with the working version be deleted?
   */
  private boolean  pRemoveFiles;

}
  
