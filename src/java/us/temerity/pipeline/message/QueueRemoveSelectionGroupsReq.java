// $Id: QueueRemoveSelectionGroupsReq.java,v 1.1 2005/12/31 20:42:59 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   R E M O V E   S E L E C T I O N  G R O U P   R E Q                         */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to remove the given existing selection group. <P> 
 */
public 
class QueueRemoveSelectionGroupsReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param names
   *   The names of the selection groups. 
   */
  public
  QueueRemoveSelectionGroupsReq
  (
   TreeSet<String> names
  )
  { 
    if(names == null) 
      throw new IllegalArgumentException
	("The selection group names cannot be (null)!");
    pNames = names;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the names of the selection groups. 
   */
  public TreeSet<String>
  getNames() 
  {
    return pNames; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -658680859843217398L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The names of the selection groups. 
   */ 
  private TreeSet<String>  pNames; 

}
  
