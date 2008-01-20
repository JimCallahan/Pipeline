// $Id: JTuple2iParamTableCellRenderer.java,v 1.2 2008/01/20 01:38:06 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   T U P L E   2 I   P A R A M   T A B L E   C E L L   R E N D E R E R                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer for {@link JTable} cells containing TupleParam<Tuple3i> data. 
 */ 
public
class JTuple2iParamTableCellRenderer
  extends JTuple2iTableCellRenderer
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new renderer.
   */
  public 
  JTuple2iParamTableCellRenderer() 
  {
    super(); 
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
    Tuple2iActionParam param = (Tuple2iActionParam) value; 

    Tuple2i tuple = null; 
    if(param != null) 
      tuple = param.getTupleValue();
    
    return super.getTableCellRendererComponent(table, tuple, isSelected, hasFocus, row, col);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 176856799930385268L;

}
