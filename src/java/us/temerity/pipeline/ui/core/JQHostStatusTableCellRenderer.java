// $Id: JQHostStatusTableCellRenderer.java,v 1.3 2007/11/20 05:42:08 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;

import us.temerity.pipeline.EditableState;
import us.temerity.pipeline.QueueHostInfo;
import us.temerity.pipeline.ui.JSimpleTableCellRenderer;

/*------------------------------------------------------------------------------------------*/
/*   Q H O S T   S T A T U S   T A B L E   C E L L   R E N D E R E R                        */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer for JTable cells containing QueueHostInfo.getStatus() data. 
 */ 
public
class JQHostStatusTableCellRenderer
  extends JSimpleTableCellRenderer
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new renderer.
   */
  public 
  JQHostStatusTableCellRenderer
  (
   QueueHostsTableModel parent
  ) 
  {
    super(JLabel.CENTER);

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
      EditableState pEditState = qinfo.getStatusState();
   
      if (isSelected)
	foreground = Color.yellow;
    }

    setForeground(foreground);

    if(pParent.isHostStatusPending(row)) 
      setForeground(Color.cyan);

    return this;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2906183585360827735L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent table model.
   */ 
  private QueueHostsTableModel  pParent;

}
