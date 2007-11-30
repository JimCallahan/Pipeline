// $Id: JQHostOrderTableCellRenderer.java,v 1.3 2007/11/30 20:14:25 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.awt.*;

import javax.swing.JLabel;
import javax.swing.JTable;

import us.temerity.pipeline.EditableState;
import us.temerity.pipeline.QueueHostInfo;
import us.temerity.pipeline.ui.JSimpleTableCellRenderer;

/*------------------------------------------------------------------------------------------*/
/*   Q H O S T   O R D E R   T A B L E   C E L L   R E N D E R E R                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer for JTable cells containing QueueHostInfo.getOrder() data.
 */ 
public
class JQHostOrderTableCellRenderer
  extends JSimpleTableCellRenderer
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new renderer.
   */
  public 
  JQHostOrderTableCellRenderer
  (
   QueueHostsTableModel parent
  ) 
  {
    super(JLabel.CENTER);
    setName("PurpleTableCellRenderer");

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
    
    if (qinfo != null) {
      boolean editable = pParent.isHostEditable(qinfo.getName());
      EditableState pEditState = qinfo.getOrderState();
      if (editable) {
	switch(pEditState) {
	case Manual:
	  setName("PurpleCheckTableCellRenderer");
	  break;
	case SemiAutomatic:
	  setName("PurpleConflictTableCellRenderer");
	  break;
	case Automatic:
	  setName("PurpleLockTableCellRenderer");
	}
      }
      else
	setName("PurpleTableCellRenderer");
    }

    if((qinfo != null) && qinfo.isOrderPending()) 
      setForeground(Color.cyan);

    return this;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6407089698571859588L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent table model.
   */ 
  private QueueHostsTableModel  pParent;

}
