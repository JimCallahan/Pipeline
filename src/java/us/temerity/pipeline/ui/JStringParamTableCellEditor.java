// $Id: JStringParamTableCellEditor.java,v 1.1 2004/06/22 19:44:54 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   S T R I N G   P A R A M   T A B L E   C E L L   E D I T O R                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The editor for {@link JTable JTable} cells containing 
 * {@link StringActionParam StringActionParam} data.
 */ 
public
class JStringParamTableCellEditor
  extends JStringTableCellEditor
  implements TableCellEditor
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
  JStringParamTableCellEditor
  (
   int width, 
   int align
  ) 
  {
    super(width, align);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   R E N D E R I N G                                                                    */
  /*----------------------------------------------------------------------------------------*/

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
    StringActionParam param = (StringActionParam) value;
    pField.setText(param.getStringValue());
    
    return pField;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8989454051335707653L;

}
