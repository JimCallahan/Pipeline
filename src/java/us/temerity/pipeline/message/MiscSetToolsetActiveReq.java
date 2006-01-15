// $Id: MiscSetToolsetActiveReq.java,v 1.2 2006/01/15 06:29:25 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   S E T   T O O L S E T   A C T I V E   R E Q                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to change the active/inactive state of a toolset.
 * 
 * @see MasterMgr
 */
public
class MiscSetToolsetActiveReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param name
   *   The name of the toolset.
   * 
   * @param isActive
   *   Whether the toolset should be active.
   */
  public
  MiscSetToolsetActiveReq
  (
   String name, 
   boolean isActive
  )
  {
    super();
    
    if(name == null) 
      throw new IllegalArgumentException
	("The toolset name cannot be (null)!");
    pName = name;

    pIsActive = isActive;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the name of the toolset.
   */ 
  public String
  getName() 
  {
    assert(pName != null);
    return pName;
  }

  /**
   * Whether the toolset should be active.
   */ 
  public boolean
  isActive()
  {
    return pIsActive;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 897875706229593861L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the toolset.
   */
  private String  pName;  

  
  /**
   * Whether the toolset should be active.
   */ 
  private boolean  pIsActive;

}
  
