// $Id: QueueEditHardwareGroupsReq.java,v 1.1 2007/11/30 20:06:25 jesse Exp $

package us.temerity.pipeline.message;

import java.util.Collection;

import us.temerity.pipeline.HardwareGroup;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   E D I T   H A R D W A R E   G R O U P   R E Q                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to change the hardware key values for the given hardware groups.
 */
public 
class QueueEditHardwareGroupsReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param groups
   *   The hardware groups to modify.
   */
  public
  QueueEditHardwareGroupsReq
  (
   Collection<HardwareGroup> groups
  )
  { 
    super();

    if(groups == null) 
      throw new IllegalArgumentException
	("The hardware groups cannot be (null)!");
    pGroups = groups;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the selection groups to modify.
   */
  public Collection<HardwareGroup> 
  getHardwareGroups()
  {
    return pGroups; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8526475266574715644L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The hardware groups to modify.
   */ 
  private Collection<HardwareGroup>  pGroups; 

}
  
