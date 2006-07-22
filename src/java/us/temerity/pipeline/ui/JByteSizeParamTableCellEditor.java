// $Id: JByteSizeParamTableCellEditor.java,v 1.1 2006/07/22 05:03:47 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   B Y T E   S I Z E   P A R A M   T A B L E   C E L L   E D I T O R                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The editor for {@link JTable JTable} cells containing 
 * {@link ByteSizeActionParam ByteSizeActionParam} data.
 */ 
public
class JByteSizeParamTableCellEditor
  extends JByteSizeTableCellEditor
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
  JByteSizeParamTableCellEditor
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
    ByteSizeActionParam param = (ByteSizeActionParam) value;
    pField.setValue(param.getLongValue());
    
    return pField;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2081807892546835850L;

}
