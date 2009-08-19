// $Id: JBooleanTableCellRenderer.java,v 1.3 2009/08/19 23:49:20 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   B O O L E A N   T A B L E   C E L L   R E N D E R E R                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer for {@link JTable JTable} cells containing {@link Boolean Boolean} data.
 */ 
public
class JBooleanTableCellRenderer
  extends JSimpleTableCellRenderer
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new renderer.
   */
  public 
  JBooleanTableCellRenderer() 
  {
    this("", JLabel.CENTER, false); 
  }

  /**
   * Construct a new renderer.
   * 
   * @param align
   *   The horizontal alignment.
   */
  public 
  JBooleanTableCellRenderer
  (
   int align
  ) 
  {
    this("", align, false); 
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
  JBooleanTableCellRenderer
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
  JBooleanTableCellRenderer
  (
   String colorPrefix, 
   int align, 
   boolean dimUneditable
  ) 
  {
    super(colorPrefix, align, dimUneditable); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   R E N D E R I N G                                                                    */
  /*----------------------------------------------------------------------------------------*/
          
  /**
   * Returns the component used for drawing the cell.
   */ 
  @Override
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
    Boolean tf = (Boolean) value; 
    if(tf != null) 
      setText(tf ? "YES" : "no");
    else 
      setText("-");

    setBasicForeground(table, isSelected, row, col);

    return this;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1928412042050293411L;

}
