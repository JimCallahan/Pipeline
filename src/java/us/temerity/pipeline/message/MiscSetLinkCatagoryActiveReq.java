// $Id: MiscSetLinkCatagoryActiveReq.java,v 1.1 2004/06/28 23:39:45 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   S E T   L I N K   C A T A G O R Y   A C T I V E   R E Q                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to change the active/inactive state of a link catagory.
 * 
 * @see MasterMgr
 */
public
class MiscSetLinkCatagoryActiveReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param name
   *   The name of the link catagory.
   * 
   * @param isActive
   *   Whether the link catagory should be active.
   */
  public
  MiscSetLinkCatagoryActiveReq
  (
   String name, 
   boolean isActive
  )
  {
    if(name == null) 
      throw new IllegalArgumentException
	("The link catagory name cannot be (null)!");
    pName = name;

    pIsActive = isActive;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the name of the link catagory.
   */ 
  public String
  getName() 
  {
    assert(pName != null);
    return pName;
  }

  /**
   * Whether the link catagory should be active.
   */ 
  public boolean
  isActive()
  {
    return pIsActive;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -177613012429276068L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the link catagory.
   */
  private String  pName;  

  
  /**
   * Whether the link catagory should be active.
   */ 
  private boolean  pIsActive;

}
  
