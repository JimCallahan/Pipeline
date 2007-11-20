// $Id: JQHostReservationTableCellRenderer.java,v 1.2 2007/11/20 05:42:07 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;

import us.temerity.pipeline.EditableState;
import us.temerity.pipeline.QueueHostInfo;
import us.temerity.pipeline.ui.JSimpleTableCellRenderer;

/*------------------------------------------------------------------------------------------*/
/*   Q H O S T   R E S E R V A T I O N   T A B L E   C E L L   R E N D E R E R              */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer for JTable cells containing QueueHostInfo.getReservation() data.
 */ 
public
class JQHostReservationTableCellRenderer
  extends JSimpleTableCellRenderer
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new renderer.
   */
  public 
  JQHostReservationTableCellRenderer
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
    
    Color foreground = Color.white;
    
    if (qinfo != null) {
      boolean editable = pParent.isHostEditable(qinfo.getName());
      EditableState pEditState = qinfo.getReservationState();
      
      if (isSelected)
	foreground = Color.yellow;
    }

    setForeground(foreground);
    
    if((qinfo != null) && qinfo.isReservationPending()) 
      setForeground(Color.cyan);

    return this;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3733001574912740331L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent table model.
   */ 
  private QueueHostsTableModel  pParent;

}
