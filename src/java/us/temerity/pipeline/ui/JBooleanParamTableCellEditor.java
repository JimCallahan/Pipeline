// $Id: JBooleanParamTableCellEditor.java,v 1.1 2004/09/12 19:04:04 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   B O O L E A N   P A R A M   T A B L E   C E L L   E D I T O R                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The editor for {@link JTable JTable} cells containing 
 * {@link BooleanActionParam BooleanActionParam} data.
 */ 
public
class JBooleanParamTableCellEditor
  extends JBooleanTableCellEditor
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
  JBooleanParamTableCellEditor
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
    BooleanActionParam param = (BooleanActionParam) value;

    Boolean tf = null;
    if(param != null) 
      tf = param.getBooleanValue();

    return super.getTableCellEditorComponent(table, tf, isSelected, row, column);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8899601013945533423L;

}
