// $Id: JTuple3iTableCellRenderer.java,v 1.3 2009/08/19 23:49:20 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;


/*------------------------------------------------------------------------------------------*/
/*   T U P L E   3 I   T A B L E   C E L L   R E N D E R E R                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer for {@link JTable} cells containing {@link Tuple3i} data. 
 */ 
public
class JTuple3iTableCellRenderer
  extends JFastTableCellRenderer
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new renderer.
   */
  public 
  JTuple3iTableCellRenderer() 
  {
    setText("-");
    setName("SimpleTableCellRenderer");
    setHorizontalAlignment(JLabel.CENTER);

    pField = new JTuple3iField();
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

    Tuple3i tuple = (Tuple3i) value; 
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

  private static final long serialVersionUID = 2777621120046307965L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Displays the tuple if not <CODE>null</CODE>.
   */ 
  private JTuple3iField  pField; 

}
