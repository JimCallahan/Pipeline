// $Id: JTextParamTableCellEditor.java,v 1.1 2004/06/22 19:44:54 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   T E X T   P A R A M   T A B L E   C E L L   E D I T O R                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The editor for {@link JTable JTable} cells containing 
 * {@link TextActionParam TextActionParam} data.
 */ 
public
class JTextParamTableCellEditor
  extends AbstractCellEditor
  implements TableCellEditor, ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new editor.
   * 
   * @param width
   *   The minimum and preferred width of the field.
   */
  public 
  JTextParamTableCellEditor
  (
   boolean isEditable, 
   int width
  ) 
  {
    pIsEditable = isEditable;

    {
      JButton btn = new JButton(pIsEditable ? "Edit..." : "View...");
      pButton = btn;
      
      btn.setName("ValuePanelButton");
      btn.setRolloverEnabled(false);
      btn.setFocusable(false);
      
      Dimension size = new Dimension(width, 19);
      btn.setMinimumSize(size);
      btn.setPreferredSize(size);
      btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
      
      btn.addActionListener(this);
      btn.setActionCommand("edit");
    }

    pDialog = new JTextDialog(pIsEditable);
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
    return pText;
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
    TextActionParam param = (TextActionParam) value;
    pText = param.getStringValue();

    pDialog.updateText((pIsEditable ? "Edit:" : "View:") + "  " + param.getNameUI(), pText);
    
    return pButton;
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
    if(e.getActionCommand().equals("edit")) {
      pDialog.setVisible(true);      
      if(pDialog.wasConfirmed()) 
	pText = pDialog.getText();
      
      fireEditingStopped();
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2883925330869149663L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Is the text parameter value editable.
   */ 
  private boolean pIsEditable;

  /**
   * The button.
   */ 
  private JButton  pButton;

  /**
   * The text being edited.
   */ 
  private String  pText;

  /**
   * The dialog the text action parameter value.
   */ 
  private JTextDialog  pDialog;

  

}
