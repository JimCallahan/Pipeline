// $Id: JStatusTableCellEditor.java,v 1.3 2007/12/05 04:51:31 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.awt.Component;
import java.util.*;

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
    pWidth = width;
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
    List<String> selectable = null;
    if (qinfo.getStatusState() != EditableState.Automatic) 
      selectable = QueueHostStatusChange.titles();
    else {
      String schedName = pParent.getCurrentScheduleName(row);
      if (schedName != null) {
	SelectionScheduleMatrix matrix = pParent.getSelectionScheduleMatrix();
	if(matrix != null) {
	  Set<String> schedNames = matrix.getScheduleNames();
	  if(schedNames.contains(schedName)) {
	    QueueHostStatus stat = matrix.getScheduledStatus(schedName);
	    switch (stat) {
	    case Enabled:
	      selectable = Arrays.asList(sEnableOnly);
	      break;
	    case Disabled:
	      selectable = Arrays.asList(sDisableOnly);
	      break;
	    }
	  }
	}
      }
    }
    if (selectable == null)
	selectable = QueueHostStatusChange.titles();
    createField(selectable, getAllValues(), null, pWidth);
    return super.getTableCellEditorComponent(table, value, isSelected, row, column);
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
  
  private int pWidth;

}
