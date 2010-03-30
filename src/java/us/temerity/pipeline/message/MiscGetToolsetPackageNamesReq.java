// $Id: MiscGetToolsetPackageNamesReq.java,v 1.1 2005/06/10 16:14:22 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   T O O L S E T   P A C K A G E   N A M E S   R E Q                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the names and revision numbers of all OS specific toolset packages.
 * 
 * @see MasterMgr
 */
public
class MiscGetToolsetPackageNamesReq
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
  MiscGetToolsetPackageNamesReq
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

  private static final long serialVersionUID = 1011403488905016797L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The operating system type.
   */
  private OsType  pOsType;  

}
  
