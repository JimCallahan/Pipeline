// $Id: JEditorSelectionTableCellEditor.java,v 1.1 2005/09/07 21:11:17 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   E D I T O R   S E L E C T I O N   T A B L E   C E L L   E D I T O R                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The editor for {@link JTable JTable} cells containing an Editor plugin instance. 
 */ 
public
class JEditorSelectionTableCellEditor
  extends AbstractCellEditor
  implements TableCellEditor, ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new renderer.
   * 
   * @param values
   *   The choice values.
   * 
   * @param width
   *   The horizontal size.
   */
  public 
  JEditorSelectionTableCellEditor
  (
   int width
  ) 
  {
    pField = UIMaster.getInstance().createEditorSelectionField(width);
    
    Dimension size = new Dimension(width, 19);
    pField.setMinimumSize(size);
    pField.setMaximumSize(size);
    pField.setPreferredSize(size);

    pField.addActionListener(this);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   U I                                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the plugin selection menus.
   */ 
  public void 
  updateMenus()
  {
    UIMaster master = UIMaster.getInstance();
    try {
      String tname = master.getMasterMgrClient().getDefaultToolsetName();
      master.updateEditorPluginField(tname, pField);
    }
    catch(PipelineException ex) {
      master.showErrorDialog(ex);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   R E N D E R I N G                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the value contained in the editor.
   */ 
  public Object 
  getCellEditorValue() 
  {
    BaseEditor editor = null;
    if(pField.getPluginName() != null) {
      try {
	PluginMgrClient pclient = PluginMgrClient.getInstance();
	editor = pclient.newEditor(pField.getPluginName(), 
				   pField.getPluginVersionID(), 
				   pField.getPluginVendor());
      }
      catch(PipelineException ex) {
      }
    }
    
    return editor; 
  }

  /**
   * Sets an initial value for the editor.
   */ 
  public Component 	
  getTableCellEditorComponent
  (
   JTable table, 
   Object value, 
   boolean isSelected, 
   int row, 
   int column
  )
  {
    pField.setPlugin((BaseEditor) value);
    
    return pField;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- ACTION LISTENER METHODS -------------------------------------------------------------*/

  /** 
   * Invoked when an action occurs. 
   */ 
  public void 
  actionPerformed
  (
   ActionEvent e
  ) 
  {
    fireEditingStopped();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6166400085298601724L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The collection field editor.
   */ 
  private JPluginSelectionField  pField;

}
