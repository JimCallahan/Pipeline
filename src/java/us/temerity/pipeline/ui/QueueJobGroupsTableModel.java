// $Id: QueueJobGroupsTableModel.java,v 1.3 2004/08/30 02:54:30 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*;

import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   J O B   G R O U P S   T A B L E   M O D E L                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A {@link SortableTableModel SortableTableModel} which contains a set of 
 * {@link QueueJobGroup QueueJobGroup} instances.
 */ 
public
class QueueJobGroupsTableModel
  extends SortableTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   */
  public 
  QueueJobGroupsTableModel() 
  {
    super();

    /* initialize the columns */ 
    { 
      pNumColumns = 5;

      {
	Class classes[] = { 
	  Long.class, int[].class, String.class, 
	  String.class, String.class
	}; 
	pColumnClasses = classes;
      }

      {
	String names[] = {
	  "ID", "Status", "Target Files", "Target Node", "Owner|View"
	};
	pColumnNames = names;
      }

      {
	int widths[] = { 60, 120, 180, 360, 180 };
	pColumnWidths = widths;
      }

      {
	TableCellRenderer renderers[] = {
	  new JSimpleTableCellRenderer(JLabel.CENTER), 
	  new JJobStatesTableCellRenderer(), 
	  new JSimpleTableCellRenderer(JLabel.CENTER), 
	  new JSimpleTableCellRenderer(JLabel.LEFT), 
	  new JSimpleTableCellRenderer(JLabel.CENTER)
	};
	pRenderers = renderers;
      }

      {
	TableCellEditor editors[] = { 
	  null, null, null, null, null
	};
	pEditors = editors;
      }
    }

    pQueueJobGroups = new ArrayList<QueueJobGroup>();
    pStateCounts    = new TreeMap<Long,int[]>();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S O R T I N G                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Sort the rows by the values in the current sort column and direction.
   */ 
  protected void 
  sort()
  {
    ArrayList<Comparable> values = new ArrayList<Comparable>();
    ArrayList<Integer> indices = new ArrayList<Integer>();
    int idx = 0;
    for(QueueJobGroup group : pQueueJobGroups) {
      Comparable value = null;
      switch(pSortColumn) {
      case 0:
	value = new Long(group.getGroupID());
	break;

      case 1:
	{
	  int[] counts = pStateCounts.get(group.getGroupID());
	  StringBuffer buf = new StringBuffer();
	  int wk;
	  for(wk=0; wk<counts.length; wk++) 
	    buf.append(counts[wk]);

	  value = buf.toString();
	}
	break;

      case 2:
	value = group.getRootPattern();
	break;

      case 3:
	value = group.getNodeID().getName();
	break;

      case 4:
	value = (group.getNodeID().getAuthor() + "|" + group.getNodeID().getView());
      }
      
      int wk;
      for(wk=0; wk<values.size(); wk++) {
	if(value.compareTo(values.get(wk)) > 0) 
	  break;
      }
      values.add(wk, value);
      indices.add(wk, idx);

      idx++;
    }

    pRowToIndex = new int[indices.size()];
    int wk; 
    if(pSortAscending) {
      for(wk=0; wk<pRowToIndex.length; wk++) 
	pRowToIndex[wk] = indices.get(wk);
    }
    else {
      for(wk=0, idx=indices.size()-1; wk<pRowToIndex.length; wk++, idx--) 
	pRowToIndex[wk] = indices.get(idx);
    }

    fireTableDataChanged();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set table data.
   * 
   * @param groups
   *   The queue job groups indexe by job group ID.
   * 
   * @param status
   *   The job status indexed by job ID.
   */ 
  public void
  setQueueJobGroups
  (
   TreeMap<Long,QueueJobGroup> groups, 
   TreeMap<Long,JobStatus> status
  ) 
  {
    pQueueJobGroups.clear();
    pStateCounts.clear();
    if((groups != null) && (status != null)) {
      pQueueJobGroups.addAll(groups.values());

      int size = JobState.all().size();
      for(QueueJobGroup group : pQueueJobGroups) {
	int[] counts = new int[size];
	for(Long jobID : group.getJobIDs()) {
	  JobStatus js = status.get(jobID);
	  if(js != null) 
	    counts[js.getState().ordinal()]++;
	}
	
	pStateCounts.put(group.getGroupID(), counts);
      }
    }

    sort();
  }
  
  /**
   * Get the table row index which contains the job group with the given ID or <CODE>-1</CODE>
   * if the group cannot be found. 
   */ 
  public int 
  getGroupRow
  (
   long groupID
  )
  {
    int row; 
    for(row=0; row<pRowToIndex.length; row++) {
      QueueJobGroup group = pQueueJobGroups.get(pRowToIndex[row]);
      if((group != null) && (group.getGroupID() == groupID))
	return row;
    }

    return -1; 
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   T A B L E   M O D E L   O V E R R I D E S                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the number of rows in the model.
   */ 
  public int 
  getRowCount()
  {
    return pQueueJobGroups.size();
  }

  /**
   * Returns true if the cell at rowIndex and columnIndex is editable.
   */ 
  public boolean 	
  isCellEditable
  (
   int row, 
   int col
  ) 
  {
    return false; 
  }

  /**
   * Returns the value for the cell at columnIndex and rowIndex.
   */ 
  public Object 	
  getValueAt
  (
   int row, 
   int col
  )
  {
    QueueJobGroup group = pQueueJobGroups.get(pRowToIndex[row]);
    switch(col) {
    case 0:
      return new Long(group.getGroupID());
      
    case 1:
      return pStateCounts.get(group.getGroupID());

    case 2:
      return group.getRootPattern();
      
    case 3:
      return group.getNodeID().getName();

    case 4:
      return (group.getNodeID().getAuthor() + "|" + group.getNodeID().getView());
      
    default:
      assert(false);
      return null;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -93844522399458365L; 



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The underlying set of job groups.
   */ 
  private ArrayList<QueueJobGroup> pQueueJobGroups;

  /**
   * The number of jobs having each possible job state for each job group.
   */ 
  private TreeMap<Long,int[]> pStateCounts; 

}
