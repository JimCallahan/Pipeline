// $Id: NodeGetOfflineVersionIDsReq.java,v 1.1 2005/03/14 16:08:21 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   G E T   O F F L I N E   V E R S I O N   I D S   R E Q                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the revision nubers of all offline checked-in versions of the given node.
 *
 * @see MasterMgr
 */
public
class NodeGetOfflineVersionIDsReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param name 
   *   The fully resolved node name.
   */
  public
  NodeGetOfflineVersionIDsReq
  (
   String name
  )
  { 
    if(name == null) 
      throw new IllegalArgumentException("The node name cannot be (null)!");
    pName = name;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the fully resolved node name.
   */
  public String
  getName() 
  {
    return pName;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 35040417729492343L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier.
   */ 
  private String  pName;

}
  
