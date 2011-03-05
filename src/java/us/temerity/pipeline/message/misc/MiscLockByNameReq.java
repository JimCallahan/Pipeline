// $Id: NodeGetByNameReq.java,v 1.1 2009/09/01 10:59:39 jim Exp $

package us.temerity.pipeline.message.misc;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.TrackedReq; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   L O C K   B Y   N A M E   R E Q                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to acquire a lock with the given name.
 *
 * @see MasterMgr
 */
public
class MiscLockByNameReq
  extends TrackedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param name 
   *   The unique lock name.
   */
  public
  MiscLockByNameReq
  (
   String name
  )
  { 
    if(name == null) 
      throw new IllegalArgumentException("The name cannot be (null)!");
    pName = name;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the unique lock name.
   */
  public String
  getName() 
  {
    return pName;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 9047525714384182583L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique lock name.
   */ 
  private String  pName;

}
  
