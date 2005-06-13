// $Id: JPackageListCellRenderer.java,v 1.1 2005/06/13 16:05:01 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.laf.LookAndFeelLoader;
import us.temerity.pipeline.toolset.*;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.border.*;

/*------------------------------------------------------------------------------------------*/
/*   P A C K A G E    L I S T   C E L L   R E N D E R E R                                   */
/*------------------------------------------------------------------------------------------*/

/**
 * 
 */ 
public
class JPackageListCellRenderer
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
  JPackageListCellRenderer
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
    PackageListData data = (PackageListData) value;

    pLabel.setText(data.getName());
    if(data.getVersionID() != null) 
      pExtraLabel.setText("(v" + data.getVersionID() + ")");
    else 
      pExtraLabel.setText("(working)");

    Toolset toolset = pDialog.getSelectedActiveToolset();
    if(toolset == null)
      toolset = pDialog.getSelectedToolset();
    
    if(toolset.isFrozen()) {
      pExtraLabel.setIcon(sBlankIcon);
    }
    else {
      if(toolset.isPackageConflicted(index)) {
	pLabel.setForeground(isSelected ? Color.yellow : Color.cyan);

	pExtraLabel.setIcon(isSelected ? sConflictSelectedIcon : sConflictIcon);
	pExtraLabel.setForeground(isSelected ? Color.yellow : Color.cyan);
      }
      else {
	pExtraLabel.setIcon(isSelected ? sCheckSelectedIcon : sCheckIcon);
      }
    }

    return this;
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6977367015331800747L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent dialog.
   */
  private JManageToolsetsDialog  pDialog;

}
