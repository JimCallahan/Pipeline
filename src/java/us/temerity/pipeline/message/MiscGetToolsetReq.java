// $Id: MiscGetToolsetReq.java,v 1.2 2005/06/10 16:14:22 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   T O O L S E T   R E Q                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the OS specific toolset with the given name.
 * 
 * @see MasterMgr
 */
public
class MiscGetToolsetReq
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
   * 
   * @param os
   *   The operating system type.
   */
  public
  MiscGetToolsetReq
  (
   String name,
   OsType os
  )
  {
    if(name == null) 
      throw new IllegalArgumentException
	("The toolset name cannot be (null)!");
    pName = name;

    if(os == null) 
      throw new IllegalArgumentException
	("The operating system cannot be (null)!");
    pOsType = os;
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
 
  /**
   * Gets the operating system type.
   */ 
  public OsType
  getOsType() 
  {
    assert(pOsType != null);
    return pOsType;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1336594084894790705L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the toolset.
   */
  private String  pName;  

  /**
   * The operating system type.
   */
  private OsType  pOsType;  

}
  
