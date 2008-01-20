// $Id: JTuple4dTableCellRenderer.java,v 1.2 2008/01/20 01:38:06 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;


/*------------------------------------------------------------------------------------------*/
/*   T U P L E   4 D   T A B L E   C E L L   R E N D E R E R                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer for {@link JTable} cells containing {@link Tuple4d} data. 
 */ 
public
class JTuple4dTableCellRenderer
  extends JLabel 
  implements TableCellRenderer
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new renderer.
   */
  public 
  JTuple4dTableCellRenderer() 
  {
    super("-");

    setOpaque(true);
    setName("SimpleTableCellRenderer");
    setHorizontalAlignment(JLabel.CENTER);

    pField = new JTuple4dField();
    pField.setEnabled(false);
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
    Color fg = (isSelected ? Color.yellow : Color.white);

    Tuple4d tuple = (Tuple4d) value; 
    if(tuple != null) {
      pField.setValue(tuple); 
      pField.setForeground(fg);
      return pField;
    }
    else {
      setForeground(fg);
      return this;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6478217448165799497L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Displays the tuple if not <CODE>null</CODE>.
   */ 
  private JTuple4dField  pField; 

}
