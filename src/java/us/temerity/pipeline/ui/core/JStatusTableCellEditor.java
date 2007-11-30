// $Id: JStatusTableCellEditor.java,v 1.1 2007/11/30 20:06:25 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JDialog;
import javax.swing.JTable;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.JDualCollectionTableCellEditor;

public 
class JStatusTableCellEditor
  extends JDualCollectionTableCellEditor
{
  public 
  JStatusTableCellEditor
  (
    QueueHostsTableModel parent,
    int width
  )
  {
    super(QueueHostStatusChange.titles(), getAllValues(), null, width);
    pParent = parent;
  }
  
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
   int column
  )
  {
    QueueHostInfo qinfo = pParent.getHostInfo(row);
    ArrayList<String> selectable = null;
    if (qinfo.getStatusState() != EditableState.Automatic) 
      selectable = QueueHostStatusChange.titles();
    else {
      QueueHostStatus status = qinfo.getStatus();
    }
  }
  
  
  private static ArrayList<String> 
  getAllValues()
  {
    ArrayList<String> dvals = new ArrayList<String>(QueueHostStatus.titles());
    dvals.addAll(QueueHostStatusChange.titles());
    return dvals;
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3371462403970791029L;
  
  private static final String[] sEnableOnly = {QueueHostStatusChange.Enable.toTitle(), QueueHostStatusChange.Terminate.toTitle()};
  private static final String[] sDisableOnly = {QueueHostStatusChange.Disable.toTitle(), QueueHostStatusChange.Terminate.toTitle()};

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent table model.
   */ 
  private QueueHostsTableModel  pParent;

}
