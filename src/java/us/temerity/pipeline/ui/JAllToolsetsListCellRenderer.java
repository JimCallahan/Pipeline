// $Id: JAllToolsetsListCellRenderer.java,v 1.3 2004/11/17 13:33:51 jim Exp $

package us.temerity.pipeline.ui;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.border.*;

/*------------------------------------------------------------------------------------------*/
/*   A L L   T O O L S E T S   L I S T   C E L L   R E N D E R E R                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer used for the {@link JList JList} cells.
 */ 
public
class JAllToolsetsListCellRenderer
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
  JAllToolsetsListCellRenderer
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
    if(pDialog.isWorkingToolset(value.toString())) {
      pExtraLabel.setText("(working)");
      if(pDialog.hasPackageConflicts(value.toString())) {
	pLabel.setForeground(isSelected ? Color.yellow : Color.cyan);

	pExtraLabel.setIcon(isSelected ? sConflictSelectedIcon : sConflictIcon);
	pExtraLabel.setForeground(isSelected ? Color.yellow : Color.cyan);
      }
      else {
	pExtraLabel.setIcon(isSelected ? sCheckSelectedIcon : sCheckIcon);
      }
    }
    else {
      pExtraLabel.setText(null);
      pExtraLabel.setIcon(null);
    }
    
    return this;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -708863842600070381L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent dialog.
   */
  private JManageToolsetsDialog  pDialog;

}
