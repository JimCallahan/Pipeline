// $Id: QueueGetHardwareKeyNamesReq.java,v 1.1 2007/12/15 07:26:24 jesse Exp $

package us.temerity.pipeline.message;

import java.io.Serializable;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   H A R D W A R E   K E Y   N A M E S   R E Q                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the names of hardware keys.
 * <p>
 * The request can either be for all the names or just the names of the keys that users can 
 * set. 
 */
public 
class QueueGetHardwareKeyNamesReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param userSettableOnly
   *   Whether to only return a list of the hardware key names that the user can set.
   */
  public
  QueueGetHardwareKeyNamesReq
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
   * Get the user settable flag.
   */ 
  public boolean
  getUserSettableOnly() 
  {
    return pUserSettableOnly; 
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -687670747863043741L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to return only the names of keys that are settable by users..
   */ 
  private boolean pUserSettableOnly; 
}