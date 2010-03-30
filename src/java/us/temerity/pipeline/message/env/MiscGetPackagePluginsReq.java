// $Id: MiscGetPackagePluginsReq.java,v 1.2 2006/05/07 21:30:13 jim Exp $

package us.temerity.pipeline.message.env;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   P A C K A G E   P L U G I N S   R E Q                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the plugins associated with a toolset package.
 */
public
class MiscGetPackagePluginsReq
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
   */
  public
  MiscGetPackagePluginsReq
  (
   String name, 
   VersionID vid
  )
  {
    if(name == null) 
      throw new IllegalArgumentException
	("The package name cannot be (null)!");
    pName = name;

    if(vid == null) 
      throw new IllegalArgumentException
	("The package revision number cannot be (null)!");
    pVersionID = vid;
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
   * Gets the revision number of the package.
   */
  public VersionID
  getVersionID() 
  {
    return pVersionID;
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5134807231245004407L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The toolset package name.
   */
  private String  pName;
  
  /**
   * The revision number of the package.
   */
  private VersionID  pVersionID;
  
}
  
