// $Id: JBooleanTableCellRenderer.java,v 1.1 2004/09/12 19:04:04 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   B O O L E A N   T A B L E   C E L L   R E N D E R E R                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer for {@link JTable JTable} cells containing {@link Boolean Boolean} data.
 */ 
public
class JBooleanTableCellRenderer
  extends JLabel 
  implements TableCellRenderer
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new renderer.
   * 
   * @param align
   *   The horizontal alignment.
   */
  public 
  JBooleanTableCellRenderer
  (
   int align
  ) 
  {
    setOpaque(true);
    setName("SimpleTableCellRenderer");

    setHorizontalAlignment(align);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   R E N D E R I N G                                                                    */
  /*----------------------------------------------------------------------------------------*/
          
  /**
   * Returns the component used for drawing the cell.
   */ 
  public Component 	
  getTableCellRendererComponent
  (
   JTable table, 
   Object value, 
   boolean isSelected, 
   boolean hasFocus, 
   int row, 
   int column
  )
  {
    BooleanActionParam param = (BooleanActionParam) value; 

    Boolean tf = null;
    if(param != null) 
      tf = param.getBooleanValue();

    if(tf != null) 
      setText(tf ? "YES" : "no");
    else 
      setText("-");

    setForeground(isSelected ? Color.yellow : Color.white);

    return this;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1928412042050293411L;

}
