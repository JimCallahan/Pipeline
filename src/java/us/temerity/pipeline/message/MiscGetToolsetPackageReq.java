// $Id: MiscGetToolsetPackageReq.java,v 1.3 2006/09/29 03:03:21 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   T O O L S E T   P A C K A G E   R E Q                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the toolset package with the given name and revision number. 
 * 
 * @see MasterMgr
 */
public
class MiscGetToolsetPackageReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param name
   *   The toolset package name.
   * 
   * @param vid
   *   The revision number of the package.
   * 
   * @param os
   *   The operating system type.
   */
  public
  MiscGetToolsetPackageReq
  (
   String name, 
   VersionID vid,
   OsType os
  )
  {
    if(name == null) 
      throw new IllegalArgumentException
	("The package name cannot be (null)!");
    pName = name;

    if(vid == null) 
      throw new IllegalArgumentException
	("The revision number cannot be (null)!");
    pVersionID = vid;

    if(os == null) 
      throw new IllegalArgumentException
	("The operating system cannot be (null)!");
    pOsType = os;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the name of the toolset package.
   */ 
  public String
  getName() 
  {
    if(pName == null)
      throw new IllegalStateException(); 
    return pName;
  }
 
  /**
   * Get the revision number of the toolset package.
   */ 
  public VersionID
  getVersionID()
  {
    return pVersionID;
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

  private static final long serialVersionUID = -1880907439117226475L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the toolset package.
   */
  private String  pName;  

  /**
   * The revision number of the toolset package.
   */ 
  private VersionID  pVersionID;       

  /**
   * The operating system type.
   */
  private OsType  pOsType;  

}
  
