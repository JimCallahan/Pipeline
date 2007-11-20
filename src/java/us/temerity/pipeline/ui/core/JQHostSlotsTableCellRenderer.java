// $Id: JQHostSlotsTableCellRenderer.java,v 1.2 2007/11/20 05:42:08 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;

import us.temerity.pipeline.EditableState;
import us.temerity.pipeline.QueueHostInfo;
import us.temerity.pipeline.ui.JSimpleTableCellRenderer;

/*------------------------------------------------------------------------------------------*/
/*   Q H O S T   S L O T S   T A B L E   C E L L   R E N D E R E R                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer for JTable cells containing QueueHostInfo.getSlots() data.
 */ 
public
class JQHostSlotsTableCellRenderer
  extends JSimpleTableCellRenderer
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new renderer.
   */
  public 
  JQHostSlotsTableCellRenderer
  (
   QueueHostsTableModel parent
  ) 
  {
    super(JLabel.CENTER);
    setName("GreenTableCellRenderer");

    pParent = parent; 
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
    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    
    QueueHostInfo qinfo = pParent.getHostInfo(row);
    
    Color foreground = Color.white;
    
    if (qinfo != null) {
      boolean editable = pParent.isHostEditable(qinfo.getName());
      EditableState pEditState = qinfo.getSlotState();
   
      if (isSelected)
	foreground = Color.yellow;
    }

    setForeground(foreground);
    if((qinfo != null) && qinfo.isJobSlotsPending()) 
      setForeground(Color.cyan);

    return this;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7259418422466973352L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent table model.
   */ 
  private QueueHostsTableModel  pParent;

}
