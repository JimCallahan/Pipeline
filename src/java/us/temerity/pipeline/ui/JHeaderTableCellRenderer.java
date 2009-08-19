// $Id: JHeaderTableCellRenderer.java,v 1.3 2009/08/19 23:49:20 jim Exp $

package us.temerity.pipeline.ui;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   H E A D E R   T A B L E   C E L L   R E N D E R E R                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer for the header cells of a table.
 */ 
public
class JHeaderTableCellRenderer
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
  JHeaderTableCellRenderer
  (
   AbstractSortableTableModel model, 
   int col, 
   int align
  ) 
  {
    pModel  = model;
    pColumn = col;

    String colorPrefix = model.getColumnColorPrefix(col); 
    if((colorPrefix == null) || (colorPrefix.length() == 0)) 
      setName("TableHeaderRenderer");
    else
      setName(colorPrefix + "TableHeaderRenderer");

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
   int column
  )
  {
    setText((String) value); 
    return this;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O M P O N E N T   O V E R R I D E S                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Returns the string to be used as the tooltip for event.
   */ 
  @Override
  public String 
  getToolTipText
  (
   MouseEvent e
  )
  {
    return pModel.getColumnDescription(pColumn); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1316445099657212359L;



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent table model.
   */ 
  private AbstractSortableTableModel  pModel;

  /**
   * The column index into the model for this header.
   */ 
  private int pColumn;

}
