// $Id: QueueAddLicenseKeyReq.java,v 1.1 2004/07/24 18:28:45 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   A D D   L I C E N S E   K E Y   R E Q                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to add a license key to the currently defined license keys. <P> 
 * 
 * @see MasterMgr
 * @see QueueMgr
 */
public
class QueueAddLicenseKeyReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param key
   *   The license key to add.
   */
  public
  QueueAddLicenseKeyReq
  (
   LicenseKey key
  )
  { 
    if(key == null) 
      throw new IllegalArgumentException
	("The license key cannot be (null)!");
    pKey = key;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the license key to add. 
   */
  public LicenseKey
  getLicenseKey() 
  {
    return pKey;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3149863259644127965L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The license key to add.
   */ 
  private LicenseKey pKey;

}
  
