// $Id: QueueRemoveHardwareKeyReq.java,v 1.1 2007/11/30 20:06:25 jesse Exp $

package us.temerity.pipeline.message;


/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   R E M O V E   H A R D W A R E   K E Y   R E Q                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to remove a hardware key to the currently defined hardware keys. <P> 
 */
public
class QueueRemoveHardwareKeyReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param kname
   *   The name of the hardware key to remove.
   */
  public
  QueueRemoveHardwareKeyReq
  (
   String kname
  )
  { 
    super();

    if(kname == null) 
      throw new IllegalArgumentException
	("The hardware key name cannot be (null)!");
    pKeyName = kname;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the name of the hardware key to remove. 
   */
  public String
  getKeyName() 
  {
    return pKeyName;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5971545612159798853L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the hardware key to remove.
   */ 
  private String pKeyName;

}
  
