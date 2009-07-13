// $Id: JActiveToolsetListCellRenderer.java,v 1.2 2009/07/13 17:16:23 jlee Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.border.*;

/*------------------------------------------------------------------------------------------*/
/*   A C T I V E   T O O L S E T    L I S T   C E L L   R E N D E R E R                     */
/*------------------------------------------------------------------------------------------*/

/**
 * 
 */ 
public
class JActiveToolsetListCellRenderer
  extends JExtraListCellRenderer
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new renderer.
   * 
   * @param dialog
   *   The parent dialog.
   */
  public 
  JActiveToolsetListCellRenderer
  (
   JManageToolsetsDialog dialog
  )
  {
    super();
    
    pDialog = dialog;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   R E N D E R I N G                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Generates the component to be displayed for JList cells.
   */ 
  public Component 
  getListCellRendererComponent
  (
   JList list,
   Object value,
   int index,
   boolean isSelected,
   boolean cellHasFocus
  )
  {
    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

    String name = (String) value;
    pLabel.setText(name);
    pExtraLabel.setText(pDialog.isDefaultToolset(name) ? "(default)" : null);

    revalidate();

    return this;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8406063358957874972L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent dialog.
   */
  private JManageToolsetsDialog  pDialog;

}
