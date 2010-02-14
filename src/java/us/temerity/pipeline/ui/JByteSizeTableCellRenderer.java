// $Id: JByteSizeTableCellRenderer.java,v 1.3 2009/08/19 23:49:20 jim Exp $

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
  extends JFastTableCellRenderer
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
    this(align, false); 
  }

  /**
   * Construct a new renderer.
   * 
   * @param align
   *   The horizontal alignment.
   * 
   * @param asFloat
   *   Whether to display as a floating point value string.
   */
  public 
  JByteSizeTableCellRenderer
  (
   int align, 
   boolean asFloat
  ) 
  {
    setName("SimpleTableCellRenderer");
    setHorizontalAlignment(align);
    pAsFloat = asFloat;
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
    if(size != null) {
      if(pAsFloat) 
        setText(ByteSize.longToFloatString(size));
      else
        setText(ByteSize.longToString(size));
    }
    else {
      setText("-");
    }

    setForeground(isSelected ? Color.yellow : Color.white);

    return this;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -690498372994587454L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether to display as a floating point value string.
   */ 
  private boolean pAsFloat; 


}
