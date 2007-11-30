// $Id: QueueAddHardwareGroupReq.java,v 1.1 2007/11/30 20:06:24 jesse Exp $

package us.temerity.pipeline.message;


/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   A D D   H A R D W A R E   G R U O P   R E Q                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to add a new hardware group. <P> 
 */
public 
class QueueAddHardwareGroupReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param name
   *   The name of the new hardware group. 
   */
  public
  QueueAddHardwareGroupReq
  (
   String name
  )
  { 
    super();

    if(name == null) 
      throw new IllegalArgumentException
	("The hardware group name cannot be (null)!");
    pName = name;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the name of the new hardware group. 
   */
  public String
  getName() 
  {
    return pName; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4887033052740475440L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the new hardware group. 
   */ 
  private String  pName; 

}
  
