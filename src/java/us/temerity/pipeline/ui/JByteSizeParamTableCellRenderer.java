// $Id: JByteSizeParamTableCellRenderer.java,v 1.1 2006/07/22 05:03:47 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   B Y T E   S I Z E   P A R A M   T A B L E   C E L L   R E N D E R E R                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer for {@link JTable JTable} cells containing 
 * {@link ByteSizeActionParam ByteSizeActionParam} data.
 */ 
public
class JByteSizeParamTableCellRenderer
  extends JByteSizeTableCellRenderer
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
  JByteSizeParamTableCellRenderer
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
    ByteSizeActionParam param = (ByteSizeActionParam) value; 

    Long size = null; 
    if(param != null) 
      size = param.getLongValue();
    
    return super.getTableCellRendererComponent(table, size, isSelected, hasFocus, row, col);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1563441552849562848L;

}
