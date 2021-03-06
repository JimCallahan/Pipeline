// $Id: QueueAddLicenseKeyReq.java,v 1.3 2006/01/15 06:29:25 jim Exp $

package us.temerity.pipeline.message.queue;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   A D D   L I C E N S E   K E Y   R E Q                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to add a license key to the currently defined license keys. <P> 
 */
public
class QueueAddLicenseKeyReq
  extends PrivilegedReq
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
    super();
    
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
  
