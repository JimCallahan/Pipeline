// $Id: QueueRemoveLicenseKeyReq.java,v 1.2 2004/07/25 03:07:33 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   R E M O V E   L I C E N S E   K E Y   R E Q                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to remove a license key to the currently defined license keys. <P> 
 */
public
class QueueRemoveLicenseKeyReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param kname
   *   The name of the license key to remove.
   */
  public
  QueueRemoveLicenseKeyReq
  (
   String kname
  )
  { 
    if(kname == null) 
      throw new IllegalArgumentException
	("The license key name cannot be (null)!");
    pKeyName = kname;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the name of the license key to remove. 
   */
  public String
  getKeyName() 
  {
    return pKeyName;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7129022594562977096L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the license key to remove.
   */ 
  private String pKeyName;

}
  
