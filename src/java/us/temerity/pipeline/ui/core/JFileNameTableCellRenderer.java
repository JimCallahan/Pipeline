// $Id: JFileNameTableCellRenderer.java,v 1.1 2009/08/19 23:49:20 jim Exp $

package us.temerity.pipeline.ui.core;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;

import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   N A M E   T A B L E   C E L L   R E N D E R E R                              */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer for {@link JTable} cell containing a individual file sequence file name. 
 */ 
public
class JFileNameTableCellRenderer
  extends JSimpleTableCellRenderer
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new renderer.
   */
  public 
  JFileNameTableCellRenderer
  (
   FileSeqTableModel model
  ) 
  {
    super(JLabel.CENTER); 
    pModel = model;
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
    
    if(!pModel.isEnabled(row))
      setForeground(Color.LIGHT_GRAY);

    return this;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8764166681293328973L;


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent table model.
   */ 
  private FileSeqTableModel pModel;

}
