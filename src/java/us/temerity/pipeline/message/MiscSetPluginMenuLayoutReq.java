// $Id: MiscSetPluginMenuLayoutReq.java,v 1.1 2005/01/05 09:44:00 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   S E T   P L U G I N   M E N U   L A Y O U T   R E Q                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to set the layout of a plugin selection menu.
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
   * @param layout
   *   The heirarchical set of menus for selection of a specific plugin version.
   */
  public
  MiscSetPluginMenuLayoutReq
  (
   PluginMenuLayout layout
  )
  {
    if(layout == null) 
      throw new IllegalArgumentException("The plugin menu layuout cannot be (null)!");
    pLayout = layout;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the heirarchical set of menus for selection of a specific plugin version.
   */
  public PluginMenuLayout
  getLayout() 
  {
    return pLayout;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2165237128319558747L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The heirarchical set of menus for selection of a specific plugin version.
   */ 
  private PluginMenuLayout  pLayout;

}
  
