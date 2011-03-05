// $Id: NodeGetByNameReq.java,v 1.1 2009/09/01 10:59:39 jim Exp $

package us.temerity.pipeline.message.misc;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.TrackedReq;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   B R E A K   L O C K   R E Q                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to release a lock.
 *
 * @see MasterMgr
 */
public
class MiscBreakLockReq
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
  MiscBreakLockReq
  (
   String name
  )
  { 
    super(); 

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

  private static final long serialVersionUID = 6773899959908752751L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique lock name.
   */ 
  private String  pName;

}
  
