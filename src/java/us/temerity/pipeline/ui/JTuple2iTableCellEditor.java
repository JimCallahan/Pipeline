// $Id: JTuple2iTableCellEditor.java,v 1.1 2007/07/31 14:58:14 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.math.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   T U P L E   2 I   T A B L E   C E L L   E D I T O R                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The editor for {@link JTable} cells containing color data. 
 */ 
public
class JTuple2iTableCellEditor
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
  JTuple2iTableCellEditor
  (
   int width
  ) 
  { 
    pField = UIFactory.createTuple2iField(new Tuple2i(), width);
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
    pField.setValue((Tuple2i) value);
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

  private static final long serialVersionUID = 5654678868006692637L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The integer field editor.
   */ 
  protected JTuple2iField  pField;

}
