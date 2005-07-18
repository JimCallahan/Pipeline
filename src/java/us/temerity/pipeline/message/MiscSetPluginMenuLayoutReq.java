// $Id: MiscSetPluginMenuLayoutReq.java,v 1.4 2005/07/18 01:34:08 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   S E T   P L U G I N   M E N U   L A Y O U T   R E Q                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to set the layout of a plugin menu associated with a toolset.
 */
public
class MiscSetPluginMenuLayoutReq
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
   * 
   * @param layout
   *   The heirarchical set of editor plugin menus.
   */
  public
  MiscSetPluginMenuLayoutReq
  (
   String name, 
   OsType os,
   PluginMenuLayout layout
  )
  {
    pName = name;

    if(os == null) 
      throw new IllegalArgumentException
	("The operating system cannot be (null)!");
    pOsType = os;
    
    if(layout == null) 
      throw new IllegalArgumentException
	("The heirarchical set of editor plugin menus.");
    pLayout = layout;
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

  /**
   * Gets the heirarchical set of editor plugin menus.
   */
  public PluginMenuLayout
  getLayout() 
  {
    return pLayout;
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1123581530859556634L;

  

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
  
  /**
   * The heirarchical set of editor plugin menus.
   */
  private PluginMenuLayout pLayout;

}
  
