// $Id: JEnumParamTableCellEditor.java,v 1.1 2004/06/22 19:44:54 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;


/*------------------------------------------------------------------------------------------*/
/*   E N U M   P A R A M   T A B L E   C E L L   E D I T O R                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The editor for {@link JTable JTable} cells containing 
 * {@link EnumActionParam EnumActionParam} data.
 */ 
public
class JEnumParamTableCellEditor
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
  JEnumParamTableCellEditor
  (
   int width
  ) 
  {
    ArrayList<String> values = new ArrayList<String>();
    values.add("-");

    pField = UIMaster.createCollectionField(values, width);
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
    return (pParam.getValueOfIndex(pField.getSelectedIndex()));
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
    pParam = (EnumActionParam) value;
    pField.setValues(pParam.getTitles());
    pField.setSelectedIndex(((Enum) pParam.getValue()).ordinal());
    
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

  private static final long serialVersionUID = -6818994795707099564L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The enum parameter being edited.
   */ 
  private EnumActionParam  pParam;

  /**
   * The enum field editor.
   */ 
  private JCollectionField  pField;

}
