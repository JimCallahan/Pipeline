// $Id: QueueGetKeyNamesReq.java,v 1.1 2008/03/07 13:25:21 jim Exp $

package us.temerity.pipeline.message;

import java.io.Serializable;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   K E Y   N A M E S   R E Q                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the names of selection, license or hardware keys.
 */
public 
class QueueGetKeyNamesReq
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
  QueueGetKeyNamesReq
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

  private static final long serialVersionUID = 6232344716104916105L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to return only the names of keys that are settable by users..
   */ 
  private boolean pUserSettableOnly; 
}
