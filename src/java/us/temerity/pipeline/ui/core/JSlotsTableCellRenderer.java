// $Id: JSlotsTableCellRenderer.java,v 1.1 2005/03/05 02:29:23 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.ui.*;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   S L O T S    T A B L E   C E L L   R E N D E R E R                                     */
/*------------------------------------------------------------------------------------------*/

/**
 * A {@link JSimpleTableCellRenderer JSimpleTableCellRenderer} which varies its foreground
 * color depending on whether the host owning the slot is enabled or disabled.
 */ 
public
class JSlotsTableCellRenderer
  extends JSimpleTableCellRenderer
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
  JSlotsTableCellRenderer
  (
   QueueSlotsTableModel model,
   int align
  ) 
  {
    super(align);
    pTableModel = model;
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
    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

    if(!pTableModel.isSlotEnabled(row) && !isSelected) 
      setForeground(new Color(0.75f, 0.75f, 0.75f));

    return this;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2925171376361907647L;


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The parent table model.
   */ 
  private QueueSlotsTableModel  pTableModel;

}
