// $Id: JPackagesListCellRenderer.java,v 1.2 2004/06/03 09:29:16 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.laf.LookAndFeelLoader;
import us.temerity.pipeline.toolset.*;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.border.*;

/*------------------------------------------------------------------------------------------*/
/*   P A C K A G E S   L I S T   C E L L   R E N D E R E R                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer used for the {@link JList JList} cells containing 
 * {@link PackageCommon PackageCommon} values.
 */ 
public
class JPackagesListCellRenderer
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
  JPackagesListCellRenderer
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

    PackageCommon pkg = (PackageCommon) value;
    pLabel.setText(pkg.getName());
    if(pkg instanceof PackageVersion) 
      pExtraLabel.setText("(v" + ((PackageVersion) pkg).getVersionID().toString() + ")");
    else 
      pExtraLabel.setText("(working)");

    String tname = pDialog.getSelectedToolsetName();
    if(pDialog.isWorkingToolset(tname)) {
      if(pDialog.hasPackageConflicts(tname, index)) {
	pLabel.setForeground(isSelected ? Color.yellow : Color.cyan);

	pExtraLabel.setIcon(isSelected ? sConflictSelectedIcon : sConflictIcon);
	pExtraLabel.setForeground(isSelected ? Color.yellow : Color.cyan);
      }
      else {
	pExtraLabel.setIcon(isSelected ? sCheckSelectedIcon : sCheckIcon);
      }
    }
    else {
      pExtraLabel.setIcon(sBlankIcon);
    }

    return this;
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -805464928141235995L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent dialog.
   */
  private JManageToolsetsDialog  pDialog;

}
