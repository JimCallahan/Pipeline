// $Id: MiscGetToolsetNamesReq.java,v 1.1 2005/06/10 16:14:22 jim Exp $

package us.temerity.pipeline.message.env;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   T O O L S E T   N A M E S   R E Q                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the names of all toolsets for the given operating system.
 * 
 * @see MasterMgr
 */
public
class MiscGetToolsetNamesReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param os
   *   The operating system type.
   */
  public
  MiscGetToolsetNamesReq
  (
   OsType os
  )
  {
    if(os == null) 
      throw new IllegalArgumentException
	("The operating system cannot be (null)!");
    pOsType = os;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the operating system type.
   */ 
  public OsType
  getOsType() 
  {
    return pOsType;
  }
 


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2336157626367396985L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The operating system type.
   */
  private OsType  pOsType;  

}
  
