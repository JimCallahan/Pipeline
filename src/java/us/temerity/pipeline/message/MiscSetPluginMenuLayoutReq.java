// $Id: MiscSetPluginMenuLayoutReq.java,v 1.7 2007/01/02 21:59:08 jim Exp $

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
  extends PrivilegedReq
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
   * @param layout
   *   The heirarchical set of editor plugin menus.
   */
  public
  MiscSetPluginMenuLayoutReq
  (
   String name, 
   PluginMenuLayout layout
  )
  {
    super();

    pName = name;

    if(layout == null) 
      throw new IllegalArgumentException
	("The heirarchical set of editor plugin menus cannot be (null).");
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
   * The heirarchical set of editor plugin menus.
   */
  private PluginMenuLayout pLayout;

}
  
