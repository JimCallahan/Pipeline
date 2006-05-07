// $Id: MiscSetPackagePluginsReq.java,v 1.4 2006/05/07 21:30:13 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   S E T   P A C K A G E   P L U G I N S   R E Q                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to set the plugins associated with a toolset package.
 */
public
class MiscSetPackagePluginsReq
  extends PrivilegedReq
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
   * @param plugins
   *   The vendors, names and revision numbers of the associated editor plugins.
   */
  public
  MiscSetPackagePluginsReq
  (
   String name, 
   VersionID vid, 
   DoubleMap<String,String,TreeSet<VersionID>> plugins
  )
  {
    super();

    if(name == null) 
      throw new IllegalArgumentException
	("The package name cannot be (null)!");
    pName = name;

    if(vid == null) 
      throw new IllegalArgumentException
	("The package revision number cannot be (null)!");
    pVersionID = vid;

    if(plugins == null) 
      throw new IllegalArgumentException("The associated plugins cannot be (null)!");
    pPlugins = plugins;
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
  
  /**
   * Gets the vendors, names and revision numbers of the associated plugins.
   */
  public DoubleMap<String,String,TreeSet<VersionID>>
  getPlugins() 
  {
    return pPlugins;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -672715010346885082L;

  

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
  
  /**
   * The vendors, names and revision numbers of the associated plugins.
   */ 
  private DoubleMap<String,String,TreeSet<VersionID>>  pPlugins;

}
  
