// $Id: JSimpleTableCellRenderer.java,v 1.4 2009/08/19 23:49:20 jim Exp $

package us.temerity.pipeline.ui;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   S I M P L E    T A B L E   C E L L   R E N D E R E R                                   */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer for {@link JTable JTable} cells containing data convertable to a 
 * {@link String String}.
 */ 
public
class JSimpleTableCellRenderer
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
  JSimpleTableCellRenderer
  (
   int align
  ) 
  {
    this(null, align, false); 
  }

  /**
   * Construct a new renderer.
   * 
   * @param colorPrefix
   *   The Synth color prefix to give the component name.
   * 
   * @param align
   *   The horizontal alignment.
   */
  public 
  JSimpleTableCellRenderer
  (
   String colorPrefix, 
   int align
  ) 
  {
    this(colorPrefix, align, false); 
  }

  /**
   * Construct a new renderer.
   * 
   * @param colorPrefix
   *   The Synth color prefix to give the component name.
   * 
   * @param align
   *   The horizontal alignment.
   * 
   * @param dimUneditable
   *   Whether to render uneditable cells with a dimmed foreground color.
   */
  public 
  JSimpleTableCellRenderer
  (
   String colorPrefix, 
   int align, 
   boolean dimUneditable   
  ) 
  {
    super(dimUneditable); 

    if((colorPrefix == null) || (colorPrefix.length() == 0)) 
      setName("SimpleTableCellRenderer");
    else
      setName(colorPrefix + "TableCellRenderer");

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
    String text = null;
    if(value != null) 
      text = value.toString();

    if(text != null) 
      setText(text);
    else 
      setText("-");

    setBasicForeground(table, isSelected, row, col);

    return this;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7930267239670931454L;


}
