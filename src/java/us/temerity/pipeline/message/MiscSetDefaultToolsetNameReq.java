// $Id: MiscSetDefaultToolsetNameReq.java,v 1.1 2004/05/29 06:35:40 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   S E T   D E F A U L T   T O O L S E T   N A M E   R E Q                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to the default toolset name.
 * 
 * @see MasterMgr
 */
public
class MiscSetDefaultToolsetNameReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param name
   *   The name of the new default toolset.
   */
  public
  MiscSetDefaultToolsetNameReq
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
   * Gets the name of the toolset.
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

  private static final long serialVersionUID = 3783445856782332753L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the toolset.
   */
  private String  pName;  

}
  
