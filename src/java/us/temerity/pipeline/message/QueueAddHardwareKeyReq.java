// $Id: QueueAddHardwareKeyReq.java,v 1.1 2007/11/30 20:06:25 jesse Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.HardwareKey;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   A D D   H A R D W A R E   K E Y   R E Q                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to add a hardware key to the currently defined hardware keys. <P> 
 */
public
class QueueAddHardwareKeyReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param key
   *   The hardware key to add.
   */
  public
  QueueAddHardwareKeyReq
  (
   HardwareKey key
  )
  { 
    super();

    if(key == null) 
      throw new IllegalArgumentException
	("The hardware key cannot be (null)!");
    pKey = key;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the hardware key to add. 
   */
  public HardwareKey
  getHardwareKey() 
  {
    return pKey;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6704240690299305530L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The hardware key to add.
   */ 
  private HardwareKey pKey;

}
  
