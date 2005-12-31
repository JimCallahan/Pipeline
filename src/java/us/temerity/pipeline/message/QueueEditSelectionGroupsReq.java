// $Id: QueueEditSelectionGroupsReq.java,v 1.1 2005/12/31 20:42:59 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*-----------------------------------------------------------------------------------------------*/
/*   Q U E U E   E D I T   S E L E C T I O N  G R O U P   R E Q                                  */
/*-----------------------------------------------------------------------------------------------*/

/**
 * A request to change the selection key biases and preemption flags for the given 
 * selection groups. <P> 
 */
public 
class QueueEditSelectionGroupsReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param groups
   *   The selection groups to modify.
   */
  public
  QueueEditSelectionGroupsReq
  (
   Collection<SelectionGroup> groups
  )
  { 
    if(groups == null) 
      throw new IllegalArgumentException
	("The selection groups cannot be (null)!");
    pGroups = groups;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the selection groups to modify.
   */
  public Collection<SelectionGroup> 
  getSelectionGroups()
  {
    return pGroups; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -658680859843217398L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The selection groups to modify.
   */ 
  private Collection<SelectionGroup>  pGroups; 

}
  
