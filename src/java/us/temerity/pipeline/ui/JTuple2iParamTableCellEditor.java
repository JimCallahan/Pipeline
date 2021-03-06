// $Id: JTuple2iParamTableCellEditor.java,v 1.1 2007/07/31 14:58:14 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   T U P L E   2 I   P A R A M   T A B L E   C E L L   E D I T O R                        */
/*------------------------------------------------------------------------------------------*/

/**
 * The editor for {@link JTable JTable} cells containing {@link Tuple2iActionParam} data.
 */ 
public
class JTuple2iParamTableCellEditor
  extends JTuple2iTableCellEditor
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
   */
  public 
  JTuple2iParamTableCellEditor
  (
   int width
  ) 
  {
    super(width); 
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
    Tuple2iActionParam param = (Tuple2iActionParam) value;
    pField.setValue(param.getTupleValue());
    
    return pField;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6887975767306119480L;

}
