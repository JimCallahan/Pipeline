// $Id: JTuple4dParamTableCellRenderer.java,v 1.1 2007/07/31 14:58:14 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   T U P L E   4 D   P A R A M   T A B L E   C E L L   R E N D E R E R                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer for {@link JTable} cells containing {@link Tuple4dParam} data. 
 */ 
public
class JTuple4dParamTableCellRenderer
  extends JTuple4dTableCellRenderer
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new renderer.
   */
  public 
  JTuple4dParamTableCellRenderer() 
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
    Tuple4dActionParam param = (Tuple4dActionParam) value; 

    Tuple4d tuple = null; 
    if(param != null) 
      tuple = param.getTupleValue();
    
    return super.getTableCellRendererComponent(table, tuple, isSelected, hasFocus, row, col);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6586514190253602986L;

}
