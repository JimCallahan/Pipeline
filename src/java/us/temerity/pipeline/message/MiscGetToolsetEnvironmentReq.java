// $Id: MiscGetToolsetEnvironmentReq.java,v 1.1 2004/06/02 21:30:06 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   T O O L S E T   E N V I R O N M E N T   R E Q                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the cooked toolset environment with the given name.
 * 
 * @see MasterMgr
 */
public
class MiscGetToolsetEnvironmentReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param name
   *   The toolset name.
   */
  public
  MiscGetToolsetEnvironmentReq
  (
   String name
  )
  {
    if(name == null) 
      throw new IllegalArgumentException
	("The toolset name cannot be (null)!");
    pName = name;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the name of the toolset;
   */ 
  public String
  getName() 
  {
    assert(pName != null);
    return pName;
  }
 


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3461016757882146279L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the toolset.
   */
  private String  pName;  

}
  
