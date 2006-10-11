// $Id: JByteSizeTableCellRenderer.java,v 1.2 2006/10/11 22:45:41 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   B Y T E   S I Z E   T A B L E   C E L L   R E N D E R E R                              */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer for {@link JTable JTable} cells containing {@link Long} data representing 
 * an integer quantity of bytes.
 */ 
public
class JByteSizeTableCellRenderer
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
  JByteSizeTableCellRenderer
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
   int col
  )
  {
    Long size = (Long) value; 
    if(size != null) 
      setText(ByteSize.longToString(size));
    else 
      setText("-");

    setForeground(isSelected ? Color.yellow : Color.white);

    return this;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -690498372994587454L;

}
