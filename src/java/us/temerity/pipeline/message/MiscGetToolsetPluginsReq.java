// $Id: MiscGetToolsetPluginsReq.java,v 1.1 2005/06/28 18:05:22 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   T O O L S E T   P L U G I N S   R E Q                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the plugins associated with all packages of a toolset.
 */
public
class MiscGetToolsetPluginsReq
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
  MiscGetToolsetPluginsReq
  (
   String name, 
   OsType os
  )
  {
    if(name == null) 
      throw new IllegalArgumentException
	("The package name cannot be (null)!");
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
   * Gets the toolset package name.
   */
  public String
  getName() 
  {
    return pName;
  }
  
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

  private static final long serialVersionUID = -7763789605100903079L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The toolset package name.
   */
  private String  pName;
  
  /**
   * The operating system type.
   */
  private OsType  pOsType; 
  
}
  
