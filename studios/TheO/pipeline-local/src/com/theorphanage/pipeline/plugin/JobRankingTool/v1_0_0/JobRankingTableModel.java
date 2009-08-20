// $Id: JobRankingTableModel.java,v 1.2 2009/08/20 19:44:07 jesse Exp $

package com.theorphanage.pipeline.plugin.JobRankingTool.v1_0_0;

import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   R A N K I N G   T A B L E   M O D E L                                          */
/*------------------------------------------------------------------------------------------*/

@SuppressWarnings("unchecked")
public 
class JobRankingTableModel
  extends AbstractSortableTableModel
{

  public 
  JobRankingTableModel
  (
    TreeMap<Long, String> jobNames,
    TreeMap<Long, Long> jobSubmissionsTimes,
    TreeMap<Long, Integer> jobPriorities  
  )
  {
    super();
    
    /* initialize the columns */ 
    { 
      pNumColumns = 4;
      {
        Class classes[] = { 
          Long.class, 
          String.class,
          Integer.class,
          String.class
        };
        pColumnClasses = classes;
      }
      {
        String names[] = { 
          "Job ID", 
          "TargetNode",
          "Priority",
          "Submission Time"
        };
        pColumnNames = names;
      }
      
      {
        String desc[] = {
          "The unique job identifier.", 
          "The name of the node associated with the jobs.",
          "The priority value for the job",
          "When the job was submitted to the farm."
        };
        pColumnDescriptions = desc;
      }
      
      {
        Vector3i widths[] = 
          { new Vector3i(80), new Vector3i(540), new Vector3i(80), new Vector3i(160)};
        pColumnWidthRanges = widths;
      }
      
      {
        TableCellRenderer renderers[] = {
          new JSimpleTableCellRenderer(SwingConstants.CENTER), 
          new JSimpleTableCellRenderer(SwingConstants.CENTER),
          new JSimpleTableCellRenderer(SwingConstants.CENTER),
          new JSimpleTableCellRenderer(SwingConstants.CENTER)
        };
        pRenderers = renderers;
      }
      
      {
        TableCellEditor editors[] = { 
          null, 
          null, 
          new JIntegerTableCellEditor(80, SwingConstants.CENTER),
          null,
        };
        pEditors = editors;
      }
    }
    
    int numRows = jobNames.size();
    
    pJobIDs = new Long[numRows];
    pJobNodeNames = new String[numRows];
    pJobSubmissionsTimes = new Long[numRows];
    pJobPriorities = new Integer[numRows];
    
    int row = 0;
    for (Long id : jobPriorities.keySet()) {
      pJobIDs[row] = id;
      pJobNodeNames[row] = jobNames.get(id);
      pJobSubmissionsTimes[row] = jobSubmissionsTimes.get(id);
      pJobPriorities[row] = jobPriorities.get(id);
      row++;
    }
    sort();
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S O R T I N G                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Sort the rows by the values in the current sort column and direction.
   */ 
  @SuppressWarnings({ "null" })
  @Override
  public void 
  sort()
  {
    ArrayList<Comparable> values = new ArrayList<Comparable>();
    ArrayList<Integer> indices = new ArrayList<Integer>();
    int idx;
    for(idx=0; idx<pJobIDs.length; idx++) {
      Comparable value = null;
      
      Long submitTime = pJobSubmissionsTimes[idx];
      Integer priority = pJobPriorities[idx];
      
      switch(pSortColumn) {
      case 0:
        value = pJobIDs[idx];
        break;
      case 1:
        value = pJobNodeNames[idx];
        break;
      case 2:
        {
          double temp = (double)priority + (1 - (double)submitTime / (double) Long.MAX_VALUE); 
          value = temp;
          break;
        }
      case 3:
        {
          double temp = (double)submitTime + (1 - (double)priority / (double) Integer.MAX_VALUE);
          value = temp;
          break;
        }
      }
      int wk;
      for(wk=0; wk<values.size(); wk++) {
        if(value.compareTo(values.get(wk)) > 0) 
          break;
      }
      values.add(wk, value);
      indices.add(wk, idx);
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

  @Override
  public int 
  getRowCount()
  {
    return pJobIDs.length;
  }

  @Override
  public Object 
  getValueAt
  (
    int rowIndex,
    int columnIndex
  )
  {
    int srow = pRowToIndex[rowIndex];
    
    switch (columnIndex) {
    case 0:
      return pJobIDs[srow];
    case 1:
      return pJobNodeNames[srow];
    case 2:
      return pJobPriorities[srow];
    case 3:
      return TimeStamps.format(pJobSubmissionsTimes[srow]);
    default:
      assert(false);
      return null;
    }
  }
  
  /**
   * Returns true if the cell at rowIndex and columnIndex is editable.
   */ 
  @Override
  public boolean        
  isCellEditable
  (
   int row, 
   int col
  ) 
  {
    if (col == 2)
      return true;

    return false;
  }
  
  /**
   * Sets the value in the cell at columnIndex and rowIndex to a value.
   */ 
  @Override
  public void 
  setValueAt
  (
   Object value, 
   int row, 
   int col
  ) 
  {
    if (col == 2 ) {
      int vrow = pRowToIndex[row];
      boolean edited = setValueAtHelper(value, vrow);

      {
        int[] selected = pTable.getSelectedRows(); 
        int wk;
        for(wk=0; wk<selected.length; wk++) {
          int srow = pRowToIndex[selected[wk]];
          if(srow != vrow)
            if (setValueAtHelper(value, srow))
              edited = true;
        }
      }

      if(edited) 
        fireTableDataChanged();
    }
  }

  private boolean 
  setValueAtHelper
  (
    Object value,
    int vrow
  )
  {
    Integer current = pJobPriorities[vrow];
    if (current == (Integer) value)
      return false;

    pJobPriorities[vrow] = (Integer) value;
    return true;
  }
  
  public TreeMap<Long, Integer>
  getPriorities()
  {
    TreeMap<Long, Integer> toReturn = new TreeMap<Long, Integer>();
    
    for (int i = 0; i < pJobIDs.length; i++) {
      toReturn.put(pJobIDs[i], pJobPriorities[i]);
    }
    
    return toReturn;
  }

  private static final long serialVersionUID = 8856931702830740071L;
  
  
  private Long[] pJobIDs;
  private Long[] pJobSubmissionsTimes;
  private Integer[] pJobPriorities;
  private String[] pJobNodeNames;
  
}
