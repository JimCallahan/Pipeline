// $Id: QueueHostnamesTableModel.java,v 1.2 2005/03/04 09:20:30 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*;
import us.temerity.pipeline.ui.*;

import java.text.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   H O S T N A M E S   T A B L E   M O D E L                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A {@link SortableTableModel SortableTableModel} which contains the names of a set of 
 * {@link QueueHost QueueHost} instances.
 */ 
public
class QueueHostnamesTableModel
  extends AbstractSortableTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   */
  public 
  QueueHostnamesTableModel
  (
   JQueueJobBrowserPanel parent
  ) 
  {
    super();
    
    /* initialize the fields */ 
    {
      pParent = parent;
      pHostnames = new ArrayList<String>();
    }

    /* initialize the columns */ 
    { 
      pNumColumns = 1;

      {
	Class classes[] = { String.class }; 
	pColumnClasses = classes;
      }

      {
	String names[] = { "Hostname" };
	pColumnNames = names;
      }

      {
	String desc[] = { "The fully resolved host name." };
	pColumnDescriptions = desc;
      }

      {
	int widths[] = { 200 };
	pColumnWidths = widths;
      }

      {
	TableCellRenderer renderers[] = { new JSimpleTableCellRenderer(JLabel.CENTER) };
	pRenderers = renderers;
      }

      {
	TableCellEditor editors[] = { null };
	pEditors = editors;
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S O R T I N G                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Sort the rows by the values in the current sort column and direction.
   */ 
  public void 
  sort()
  {
    ArrayList<Comparable> values = new ArrayList<Comparable>();
    ArrayList<Integer> indices = new ArrayList<Integer>();
    int idx = 0;
    for(String name : pHostnames) {      
      Comparable value = name;

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

    pParent.sortHostsTable(pRowToIndex);
  }

  /**
   * Copy the row sort order from another table model with the same number of rows.
   */ 
  public void
  externalSort
  (
   int[] rowToIndex
  ) 
  {
    pRowToIndex = rowToIndex.clone();
    fireTableDataChanged();     
  }


  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set table data.
   */ 
  public void
  setHostnames
  (
   Collection<String> hostnames
  ) 
  {
    pHostnames.clear();
    if(hostnames != null)
      pHostnames.addAll(hostnames);

    sort();
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
    return pHostnames.size();
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
    return pHostnames.get(pRowToIndex[row]);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3323325879540437864L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent panel.
   */ 
  private JQueueJobBrowserPanel pParent;
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * The names of the queue hosts.
   */ 
  private ArrayList<String> pHostnames;

}
