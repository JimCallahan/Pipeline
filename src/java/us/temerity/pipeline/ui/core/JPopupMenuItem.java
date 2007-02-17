// $Id: JPopupMenuItem.java,v 1.1 2007/02/17 11:46:01 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*;
import java.util.*;
import javax.swing.*;

/*------------------------------------------------------------------------------------------*/
/*   P O P U P   M E N U   I T E M                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A JMenuItem with a reference to the top-level popup menu containing the item.<P> 
 * 
 * When popup menus are nested several levels deep, there is no way to traverse back up
 * the Component hierarchy to find the top-level menu.  This class provides a reference to
 * this menu directly.
 */ 
public 
class JPopupMenuItem
  extends JMenuItem
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new menu item.
   * 
   * @param menu
   *   The top-level popup menu containing this item or <CODE>null</CODE> if unknown.
   */
  public 
  JPopupMenuItem
  (
   JPopupMenu menu,
   String text
  ) 
  {
    super(text);
    pTopLevelMenu = menu;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The top-level popup menu containing this item or <CODE>null</CODE> if unknown.
   */ 
  public JPopupMenu
  getTopLevelMenu()
  {
    return pTopLevelMenu;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -2810501295037671955L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The top-level popup menu containing this item or <CODE>null</CODE> if unknown.
   */ 
  private JPopupMenu  pTopLevelMenu;

}
