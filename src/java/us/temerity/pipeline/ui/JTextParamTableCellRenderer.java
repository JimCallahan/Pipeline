// $Id: JTextParamTableCellRenderer.java,v 1.1 2004/06/22 19:44:54 jim Exp $

package us.temerity.pipeline.ui;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   T E X T   P A R A M   T A B L E   C E L L   R E N D E R E R                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer for {@link JTable JTable} cells containing 
 * {@link TextActionParam TextActionParam} data.
 */ 
public
class JTextParamTableCellRenderer
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
  JTextParamTableCellRenderer
  (
   boolean isEditable
  ) 
  {
    pIsEditable = isEditable;

    {
      JLabel label = new JLabel("-");
      pNullLabel = label;

      label.setOpaque(true);
      label.setName("SimpleTableCellRenderer");    
      label.setHorizontalAlignment(JLabel.CENTER);
    }

    {
      JLabel label = new JLabel();
      pLabel = label;
      
      label.setOpaque(true);
      label.setName("ValuePanelButtonLabel");    
      label.setHorizontalAlignment(JLabel.CENTER);
    }
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
    if(value != null) {
      pLabel.setText(pIsEditable ? "Edit..." : "View...");
      pLabel.setForeground(isSelected ? Color.yellow : Color.white);

      return pLabel;
    }
    else {
      pNullLabel.setForeground(isSelected ? Color.yellow : Color.white);

      return pNullLabel;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6323554088903456557L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the text parameter is editable.
   */ 
  private boolean  pIsEditable;

  /**
   * The label to use when the value is <CODE>null</CODE>.
   */ 
  private JLabel  pNullLabel;

  /**
   * The label to use when the value is not <CODE>null</CODE>.
   */ 
  private JLabel  pLabel;

}
