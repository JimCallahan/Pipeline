// $Id: JColorTableCellRenderer.java,v 1.1 2007/07/31 14:58:14 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;


/*------------------------------------------------------------------------------------------*/
/*   C O L O R   T A B L E   C E L L   R E N D E R E R                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer for {@link JTable} cells containing {@link Color3d} data. 
 */ 
public
class JColorTableCellRenderer
  extends JLabel 
  implements TableCellRenderer
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new renderer.
   * 
   * @param owner
   *   The owning parent dialog.
   */
  public 
  JColorTableCellRenderer
  (
   Dialog owner
  ) 
  {
    super("-");

    setOpaque(true);
    setName("SimpleTableCellRenderer");
    setHorizontalAlignment(JLabel.CENTER);

    pColorField = new JColorField(owner, new Color3d());
    pColorField.setEnabled(false);
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
   int col
  )
  {
    Color3d color = (Color3d) value; 
    if(color != null) {
      pColorField.setValue(color); 
      return pColorField;
    }
    else {
      setForeground(isSelected ? Color.yellow : Color.white);
      return this;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7444431888680282404L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Displays the color if not <CODE>null</CODE>.
   */ 
  private JColorField  pColorField; 

}
