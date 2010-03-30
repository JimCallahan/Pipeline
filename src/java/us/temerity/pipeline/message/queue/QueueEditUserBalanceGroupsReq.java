// $Id: QueueEditUserBalanceGroupsReq.java,v 1.1 2009/09/16 03:54:40 jesse Exp $

package us.temerity.pipeline.message.queue;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   E D I T   U S E R   B A L A N C E   G R O U P S   R E Q                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to change the user weightings for the given user balance groups. <P> 
 */
public 
class QueueEditUserBalanceGroupsReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param groups
   *   The dispatch controls to modify.
   */
  public
  QueueEditUserBalanceGroupsReq
  (
    Collection<UserBalanceGroup> groups
  )
  { 
    super();

    if(groups == null) 
      throw new IllegalArgumentException
	("The user balance groups cannot be (null)!");
    pGroups = groups;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the user balance groups to modify.
   */
  public Collection<UserBalanceGroup> 
  getUserBalanceGroups()
  {
    return pGroups; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4039362720793831537L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The user balance groups to modify.
   */ 
  private Collection<UserBalanceGroup> pGroups ; 
}