// $Id: JTuple3dTableCellRenderer.java,v 1.1 2007/07/31 14:58:14 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;


/*------------------------------------------------------------------------------------------*/
/*   T U P L E   3 D   T A B L E   C E L L   R E N D E R E R                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer for {@link JTable} cells containing {@link Tuple3d} data. 
 */ 
public
class JTuple3dTableCellRenderer
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
  JTuple3dTableCellRenderer() 
  {
    super("-");

    setOpaque(true);
    setName("SimpleTableCellRenderer");
    setHorizontalAlignment(JLabel.CENTER);

    pField = new JTuple3dField();
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

    Tuple3d tuple = (Tuple3d) value; 
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

  private static final long serialVersionUID = 6997629161832240773L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Displays the tuple if not <CODE>null</CODE>.
   */ 
  private JTuple3dField  pField; 

}
