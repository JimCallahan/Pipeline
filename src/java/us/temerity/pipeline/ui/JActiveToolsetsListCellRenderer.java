// $Id: JActiveToolsetsListCellRenderer.java,v 1.2 2004/06/02 21:34:00 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.border.*;

/*------------------------------------------------------------------------------------------*/
/*   A C T I V E   T O O L S E T S   L I S T   C E L L   R E N D E R E R                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer used for the {@link JList JList} cells containing the names of active 
 * toolsets maintained by a {@link JManageToolsetsDialog JManageToolsetsDialog} instance.
 */ 
public
class JActiveToolsetsListCellRenderer
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
  JActiveToolsetsListCellRenderer
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
