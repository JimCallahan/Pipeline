// $Id: JStringTableCellEditor.java,v 1.2 2005/01/03 06:56:23 jim Exp $

package us.temerity.pipeline.ui;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   S T R I N G    T A B L E   C E L L   E D I T O R                                       */
/*------------------------------------------------------------------------------------------*/

/**
 * The editor for {@link JTable JTable} cells containing simple {@link String String} data.
 */ 
public
class JStringTableCellEditor
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
   * 
   * @param align
   *   The horizontal alignment.
   */
  public 
  JStringTableCellEditor
  (
   int width, 
   int align
  ) 
  {
    pField = UIFactory.createEditableTextField(null, width, align);
    pField.addActionListener(this);
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
    String text = pField.getText();
    if((text != null) && (text.length() > 0)) 
      return text;
    return null;
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
    pField.setText((String) value);
    
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
    KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
    fireEditingStopped();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 884865993745973338L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The text field editor.
   */ 
  protected JTextField  pField;

}
