// $Id: JFileNoveltyTableCellEditor.java,v 1.1 2009/08/19 23:51:00 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   N O V E L T Y   T A B L E   C E L L   E D I T O R                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The editor for {@link JTable} cells containing a file novelty boolean value.
 */ 
public
class JFileNoveltyTableCellEditor
  extends AbstractCellEditor
  implements TableCellEditor, ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new renderer.
   * 
   * @param channel
   *   The update channel to use
   * 
   * @param width
   *   The horizontal size.
   */
  public 
  JFileNoveltyTableCellEditor
  (
   FileSeqTableModel model
  ) 
  {
    pModel = model;
    
    pCheckBox = new JCheckBox();
    pCheckBox.setName("FileCheck");
    pCheckBox.addActionListener(this); 

    Dimension size = new Dimension(70, 19);
    pCheckBox.setMinimumSize(size);
    pCheckBox.setMaximumSize(size);
    pCheckBox.setPreferredSize(size);
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   C E L L   E D I T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns true if the editing cell should be selected, false otherwise
   */
  public boolean 
  shouldSelectCell
  (
   EventObject anEvent
  )
  {
    return false;
  }

  /**
   * Returns the value contained in the editor.
   */ 
  public Object 
  getCellEditorValue() 
  {
    return new Boolean(pCheckBox.isSelected()); 
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   T A B L E   C E L L   E D I T O R                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Sets an initial value for the editor.
   */ 
  public Component 	
  getTableCellEditorComponent
  (
   JTable table, 
   Object value, 
   boolean isSelected, 
   int row, 
   int col
  )
  {
    FileSeqTableModel.IconState state = pModel.getIconState(row, col);
    if(state == null) 
      return pCheckBox; 

    switch(state) {
    case Check:
    case CheckPicked:
      pCheckBox.setName(isSelected ? "FileCheckSelected" : "FileCheck"); 
      break;

    case CheckExtLeft:
    case CheckPickedExtLeft:
      pCheckBox.setName(isSelected ? "FileCheckExtLeftSelected" : "FileCheckExtLeft"); 
      break;

    case CheckExtRight:
    case CheckPickedExtRight:
      pCheckBox.setName(isSelected ? "FileCheckExtRightSelected" : "FileCheckExtRight"); 
      break;

    case CheckExtBoth:
    case CheckPickedExtBoth:
      pCheckBox.setName(isSelected ? "FileCheckExtBothSelected" : "FileCheckExtBoth"); 
      break;

    default:
      throw new IllegalArgumentException
        ("The (" + state + ") state should not be possible here!"); 
    }

    pCheckBox.setSelected((Boolean) value);

    return pCheckBox;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- ACTION LISTENER METHODS -------------------------------------------------------------*/

  public void 
  actionPerformed
  (
   ActionEvent e
  ) 
  {
    fireEditingStopped();
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4746025491264523460L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent talble model;
   */ 
  private FileSeqTableModel pModel; 

  /**
   * The check box used to edit the cell.
   */ 
  private JCheckBox  pCheckBox; 
}
