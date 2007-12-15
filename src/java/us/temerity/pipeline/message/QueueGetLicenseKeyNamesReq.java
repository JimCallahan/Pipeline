// $Id: QueueGetLicenseKeyNamesReq.java,v 1.1 2007/12/15 07:26:24 jesse Exp $

package us.temerity.pipeline.message;

import java.io.Serializable;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   L I C E N S E   K E Y   N A M E S   R E Q                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the names of license keys.
 * <p>
 * The request can either be for all the names or just the names of the keys that users can 
 * set. 
 */
public 
class QueueGetLicenseKeyNamesReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param userSettableOnly
   *   Whether to only return a list of the license key names that the user can set.
   */
  public
  QueueGetLicenseKeyNamesReq
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

  private static final long serialVersionUID = 6816845447774059412L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to return only the names of keys that are settable by users..
   */ 
  private boolean pUserSettableOnly; 
}
