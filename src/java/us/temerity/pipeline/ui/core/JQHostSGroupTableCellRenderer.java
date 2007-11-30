// $Id: JQHostSGroupTableCellRenderer.java,v 1.3 2007/11/30 20:21:33 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;

import us.temerity.pipeline.EditableState;
import us.temerity.pipeline.QueueHostInfo;
import us.temerity.pipeline.ui.JSimpleTableCellRenderer;

/*------------------------------------------------------------------------------------------*/
/*   Q H O S T   S G R O U P   T A B L E   C E L L   R E N D E R E R                        */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer for JTable cells containing QueueHostInfo.getSelectionGroup() data.
 */ 
public
class JQHostSGroupTableCellRenderer
  extends JSimpleTableCellRenderer
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new renderer.
   */
  public 
  JQHostSGroupTableCellRenderer
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
    
    if((qinfo != null) && qinfo.isSelectionGroupPending()) 
      setForeground(Color.cyan);

    if (qinfo != null) {
      boolean editable = pParent.isHostEditable(qinfo.getName());
      EditableState pEditState = qinfo.getGroupState();
      
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
    return this;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7653466441229188153L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent table model.
   */ 
  private QueueHostsTableModel  pParent;

}
