// $Id: JBooleanTableCellEditor.java,v 1.3 2005/03/20 22:49:23 jim Exp $

package us.temerity.pipeline.ui;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   B O O L E A N   T A B L E   C E L L   E D I T O R                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The editor for {@link JTable JTable} cells containing {@link Boolean Boolean} data.
 */ 
public
class JBooleanTableCellEditor
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
  JBooleanTableCellEditor
  (
   int width, 
   int align
  ) 
  {
    this(width, align, true);
  }

  /**
   * Construct a new editor.
   * 
   * @param width
   *   The minimum and preferred width of the field.
   * 
   * @param align
   *   The horizontal alignment.
   * 
   * @param allowNull
   *   Whether null values are permissable.
   */
  public 
  JBooleanTableCellEditor
  (
   int width, 
   int align, 
   boolean allowNull
  ) 
  {
    pAllowNull = allowNull;

    ArrayList<String> values = new ArrayList<String>();
    values.add("YES");
    values.add("no");
    if(pAllowNull) 
      values.add("-");

    pField = UIFactory.createCollectionField(values, width);
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
    switch(pField.getSelectedIndex()) {
    case 0:
      return new Boolean(true);
      
    case 1:
      return new Boolean(false);

    default: 
      return (pAllowNull ? null : new Boolean(false));
    }
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
    Boolean tf = (Boolean) value;
    if(tf != null) 
      pField.setSelectedIndex(tf ? 0 : 1);
    else 
      pField.setSelectedIndex(pAllowNull ? 2 : 1);

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

  private static final long serialVersionUID = 8590236926042621248L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The boolean values editor.
   */ 
  protected JCollectionField  pField;

  /**
   * Whether <CODE>null</CODE> values are permissable.
   */ 
  private boolean  pAllowNull;

}
