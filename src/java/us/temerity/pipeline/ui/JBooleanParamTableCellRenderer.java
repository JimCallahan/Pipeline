// $Id: JBooleanParamTableCellRenderer.java,v 1.1 2005/03/20 22:48:00 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   B O O L E A N   P A R A M   T A B L E   C E L L   R E N D E R E R                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer for {@link JTable JTable} cells containing 
 * {@link BooleanActionParam BooleanActionParam} data.
 */ 
public
class JBooleanParamTableCellRenderer
  extends JBooleanTableCellRenderer
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
  JBooleanParamTableCellRenderer
  (
   int align
  ) 
  {
    super(align);
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
    BooleanActionParam param = (BooleanActionParam) value; 

    Boolean tf = null;
    if(param != null) 
      tf = param.getBooleanValue();
    
    return super.getTableCellRendererComponent(table, tf, isSelected, hasFocus, row, col);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5718844052814385968L;

}
