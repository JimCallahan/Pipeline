// $Id: JPluginTableCellRenderer.java,v 1.1 2005/09/07 21:11:17 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   P L U G I N   T A B L E   C E L L   R E N D E R E R                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer for {@link JTable JTable} cells containing BasePlugin instances. 
 */ 
public
class JPluginTableCellRenderer
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
  JPluginTableCellRenderer
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
    String text = null;
    if(value != null) {
      BasePlugin plugin = (BasePlugin) value;
      text = plugin.getName();
    }

    if(text != null) 
      setText(text);
    else 
      setText("-");

    setForeground(isSelected ? Color.yellow : Color.white);

    return this;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6805929000148512450L;

}
