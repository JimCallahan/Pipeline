// $Id: JFastTableCellRenderer.java,v 1.1 2009/08/19 23:49:20 jim Exp $

package us.temerity.pipeline.ui;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   F A S T   T A B L E   C E L L   R E N D E R E R                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * An abstract base class for all JLabel based TableCellRenderers which overrides JLabel
 * methods not needed by cell renderers for performance reasons. 
 */ 
public abstract
class JFastTableCellRenderer
  extends JLabel
  implements TableCellRenderer
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new renderer.
   */
  public 
  JFastTableCellRenderer() 
  {
    this(false); 
  }

  /**
   * Construct a new renderer.
   * 
   * @param dimUneditable
   *   Whether to render uneditable cells with a dimmed foreground color.
   */
  public 
  JFastTableCellRenderer
  (
   boolean dimUneditable
  ) 
  {
    pDimUneditable = dimUneditable; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   R E N D E R I N G                                                                    */
  /*----------------------------------------------------------------------------------------*/
          
  /**
   * Returns the component used for drawing the cell.
   */ 
  public abstract Component 	
  getTableCellRendererComponent
  (
   JTable table, 
   Object value, 
   boolean isSelected, 
   boolean hasFocus, 
   int row, 
   int column
  );

  /**
   * Set the foreground color based on selection and whether the cell is editable.
   */ 
  protected void 
  setBasicForeground
  (
   JTable table,
   boolean isSelected, 
   int row, 
   int col
  ) 
  {
    boolean fgSet = false; 
    if(pDimUneditable) {
      TableModel model = table.getModel();
      if(!model.isCellEditable(row, col)) {
        setForeground(isSelected ? sOffYellow : sOffWhite);
        fgSet = true;
      }
    }

    if(!fgSet)
      setForeground(isSelected ? Color.yellow : Color.white);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   J L A B E L   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The following methods are overridden as a performance measure to 
   * to prune code-paths are often called in the case of renders
   * but which we know are unnecessary.
   */

  /**
   * Overridden for performance reasons.
   * See the <a href="DefaultTableCellRenderer#override">Implementation Note</a> 
   * for more information.
   */
  @Override
  public boolean 
  isOpaque() 
  { 
    return true; 
  }

  /**
   * Overridden for performance reasons.
   * See the <a href="DefaultTableCellRenderer#override">Implementation Note</a> 
   * for more information.
   *
   * @since 1.5
   */
  @Override
  public void 
  invalidate() 
  {}

  /**
   * Overridden for performance reasons.
   * See the <a href="DefaultTableCellRenderer#override">Implementation Note</a> 
   * for more information.
   */
  @Override
  public void 
  validate() 
  {}

  /**
   * Overridden for performance reasons.
   * See the <a href="DefaultTableCellRenderer#override">Implementation Note</a> 
   * for more information.
   */
  @Override
  public void 
  revalidate() 
  {}

  /**
   * Overridden for performance reasons.
   * See the <a href="DefaultTableCellRenderer#override">Implementation Note</a> 
   * for more information.
   */
  @Override
  public void 
  repaint(long tm, int x, int y, int width, int height) 
  {}

  /**
   * Overridden for performance reasons.
   * See the <a href="DefaultTableCellRenderer#override">Implementation Note</a> 
   * for more information.
   */
  @Override
  public void 
  repaint(Rectangle r) 
  {}

  /**
   * Overridden for performance reasons.
   * See the <a href="DefaultTableCellRenderer#override">Implementation Note</a> 
   * for more information.
   *
   * @since 1.5
   */
  @Override
  public void 
  repaint() {}

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  public static final Color sOffYellow = new Color(0.7f, 0.7f, 0.00f);
  public static final Color sOffWhite  = new Color(0.7f, 0.7f, 0.7f);


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to render uneditable cells with a dimmed foreground color.
   */ 
  private boolean pDimUneditable; 
}
