// $Id: JDualCollectionTableCellEditor.java,v 1.2 2007/11/30 20:14:25 jesse Exp $

package us.temerity.pipeline.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.*;
import javax.swing.table.TableCellEditor;

/*------------------------------------------------------------------------------------------*/
/*   D U A L   C O L L E C T I O N   T A B L E   C E L L   E D I T O R                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The editor for {@link JTable JTable} cells who's {@link String String} value can 
 * only be selected from a subset of the values which can be displayed. 
 */ 
public
class JDualCollectionTableCellEditor
  extends AbstractCellEditor
  implements TableCellEditor, ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new renderer.
   * 
   * @param selectValues
   *   The values which can be selected using a pull-down menu.
   * 
   * @param displayValues
   *   The values which can be displayed in the field.
   * 
   * @param width
   *   The horizontal size.
   */
  public 
  JDualCollectionTableCellEditor
  (
   Collection<String> selectValues, 
   Collection<String> displayValues, 
   int width
  ) 
  {
    this(selectValues, displayValues, null, width);
  }

  /**
   * Construct a new renderer.
   * 
   * @param selectValues
   *   The values which can be selected using a pull-down menu.
   * 
   * @param displayValues
   *   The values which can be displayed in the field.
   * 
   * @param parent
   *   The parent dialog or <CODE>null</CODE> the field is not a child of a dialog.
   * 
   * @param width
   *   The horizontal size.
   */
  public 
  JDualCollectionTableCellEditor
  (
   Collection<String> selectValues, 
   Collection<String> displayValues, 
   JDialog parent, 
   int width
  ) 
  {
    createField(selectValues, displayValues, parent, width);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L O O K  &  F E E L                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Sets the prefix prepended to the name of the component.
   */
  public void 
  setSynthPrefix
  (
   String prefix
  ) 
  {
    pField.setSynthPrefix(prefix);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   R E N D E R I N G                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Updates the field in the renderer.
   * 
   * @param selectValues
   *   The values which can be selected using a pull-down menu.
   * 
   * @param displayValues
   *   The values which can be displayed in the field.
   * 
   * @param parent
   *   The parent dialog or <CODE>null</CODE> the field is not a child of a dialog.
   * 
   * @param width
   *   The horizontal size.
   */
  public void
  createField
  (
    Collection<String> selectValues, 
    Collection<String> displayValues, 
    JDialog parent, 
    int width
  )
  {
    pField = new JDualCollectionField(selectValues, displayValues, parent);
    
    Dimension size = new Dimension(width, 19);
    pField.setMinimumSize(size);
    pField.setMaximumSize(size);
    pField.setPreferredSize(size);

    pField.addActionListener(this);
  }
  
  /**
   * Returns the value contained in the editor.
   */ 
  public Object 
  getCellEditorValue() 
  {
    return pField.getSelected();
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
    pField.setSelected((String) value);
    
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

  private static final long serialVersionUID = 4438937894390715487L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The collection field editor.
   */ 
  private JCollectionField  pField;

}
