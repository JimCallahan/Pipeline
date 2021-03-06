// $Id: QueueGetKeyDescriptionsReq.java,v 1.1 2008/03/07 13:25:21 jim Exp $

package us.temerity.pipeline.message.queue;

import java.io.Serializable;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   K E Y   D E S C R I P T I O N S   R E Q                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the names and descriptions of the currently defined selection, license 
 * or hardware keys.
 */
public 
class QueueGetKeyDescriptionsReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param userSettableOnly
   *   Whether to only return the keys that the user can set.
   */
  public
  QueueGetKeyDescriptionsReq
  (
    boolean userSettableOnly
  )
  {
    pUserSettableOnly = userSettableOnly;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to only return the keys that the user can set.
   */ 
  public boolean
  getUserSettableOnly() 
  {
    return pUserSettableOnly; 
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5515420256497769092L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to only return the keys that the user can set.
   */ 
  private boolean pUserSettableOnly; 

}
