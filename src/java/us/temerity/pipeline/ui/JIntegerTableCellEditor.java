// $Id: JIntegerTableCellEditor.java,v 1.2 2005/01/03 06:56:23 jim Exp $

package us.temerity.pipeline.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   I N T E G E R   T A B L E   C E L L   E D I T O R                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The editor for {@link JTable JTable} cells containing {@link Integer Integer} data.
 */ 
public
class JIntegerTableCellEditor
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
  JIntegerTableCellEditor
  (
   int width, 
   int align
  ) 
  {
    pField = UIFactory.createIntegerField(null, width, align);
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
    return pField.getValue();
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
    pField.setValue((Integer) value);
    
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

  private static final long serialVersionUID = 4204678430156487156L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The integer field editor.
   */ 
  protected JIntegerField  pField;

}
