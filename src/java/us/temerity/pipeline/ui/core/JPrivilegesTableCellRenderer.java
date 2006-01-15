// $Id: JPrivilegesTableCellRenderer.java,v 1.1 2006/01/15 06:29:26 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.ui.*;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   P R I V I L E G E S   T A B L E   C E L L   R E N D E R E R                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer for {@link JTable JTable} cells containing {@link Boolean Boolean} data.
 */ 
public
class JPrivilegesTableCellRenderer
  extends JBooleanTableCellRenderer
  implements TableCellRenderer
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new renderer.
   */
  public 
  JPrivilegesTableCellRenderer()
  {
    super(JLabel.CENTER);
    setName("BlueTableCellRenderer");
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

    TableModel model = table.getModel();
    if(model.isCellEditable(row, col)) 
      setForeground(isSelected ? Color.yellow : Color.white);
    else 
      setForeground(isSelected ? sOffYellow : sOffWhite);

    return this;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5834642381400657503L;

  private static final Color sOffYellow = new Color(0.8f, 0.8f, 0.0f);
  private static final Color sOffWhite  = new Color(0.8f, 0.8f, 0.8f);

}
