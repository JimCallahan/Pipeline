// $Id: JCollectionTableCellEditor.java,v 1.5 2006/01/02 20:45:23 jim Exp $

package us.temerity.pipeline.ui;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   C O L L E C T I O N   T A B L E   C E L L   E D I T O R                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The editor for {@link JTable JTable} cells who's {@link String String} value can 
 * only be one of the members of a {@link Collection Collection<String>}.
 */ 
public
class JCollectionTableCellEditor
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
  JCollectionTableCellEditor
  (
   Collection<String> values,
   int width
  ) 
  {
    this(values, null, width);
  }

  /**
   * Construct a new renderer.
   * 
   * @param values
   *   The choice values.
   * 
   * @param parent
   *   The parent dialog or <CODE>null</CODE> the field is not a child of a dialog.
   * 
   * @param width
   *   The horizontal size.
   */
  public 
  JCollectionTableCellEditor
  (
   Collection<String> values,
   JDialog parent, 
   int width
  ) 
  {
    pField = new JCollectionField(values, parent);
    
    Dimension size = new Dimension(width, 19);
    pField.setMinimumSize(size);
    pField.setMaximumSize(size);
    pField.setPreferredSize(size);

    pField.addActionListener(this);
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

  private static final long serialVersionUID = -7454634252700018659L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The collection field editor.
   */ 
  private JCollectionField  pField;

}
