// $Id: RootNodesTableModel.java,v 1.1 2009/12/14 03:20:56 jim Exp $

package us.temerity.pipeline.plugin.CleanUpTool.v2_4_18;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.ui.*;

import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   R O O T   N O D E S   T A B L E   M O D E L                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A {@link SortableTableModel} which contains the names of root nodes and whether to
 * display them.
 */ 
public
class RootNodesTableModel
  extends AbstractSortableTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   */
  public 
  RootNodesTableModel() 
  {
    super();

    /* initialize the columns */ 
    { 
      pNumColumns = 2;

      {
	Class classes[] = { 
	  Boolean.class, String.class, 
	};
	pColumnClasses = classes;
      }

      {
	String names[] = {
	  "Show Root", "Node Name"
	};
	pColumnNames = names;
      }

      {
	String desc[] = {
	  "Whether to show the node network rooted at the given node in the Node " + 
          "Viewer panel.", 
          "The names of the most downstream nodes in the working area.", 
	};
	pColumnDescriptions = desc;
      }

      {
        Vector3i ranges[] = {
          new Vector3i(90), 
          new Vector3i(180, 540, Integer.MAX_VALUE), 
        };
        pColumnWidthRanges = ranges;
      }

      {
	TableCellRenderer renderers[] = {
	  new JBooleanTableCellRenderer(), 
	  new JSimpleTableCellRenderer(JLabel.LEFT)
	};
	pRenderers = renderers;
      }

      {
	TableCellEditor editors[] = { 
	  new JBooleanTableCellEditor(180, JLabel.CENTER, false), 
          null
	};
	pEditors = editors;
      }
    }

    pShowRoots = new ArrayList<Boolean>();
    pNodeNames = new ArrayList<String>();
    pNumRows = 0;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the table data.
   * 
   * @param names
   *   The names of the root nodes. 
   */ 
  public void
  setData
  (
   TreeSet<String> names
  ) 
  {
    pShowRoots.clear(); 
    pNodeNames.clear(); 
    
    for(String name : names) {
      pShowRoots.add(new Boolean(true));
      pNodeNames.add(name); 
    }

    pNumRows = pNodeNames.size(); 

    sort();
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the nodes with jobs to kill.
   */
  public TreeSet<String> 
  getRootNames()
  {
    TreeSet<String> results = new TreeSet<String>();

    int row=0;
    for(Boolean show : pShowRoots) {
      if((show != null) && show) 
        results.add(pNodeNames.get(row));
      row++;
    }

    return results;
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
    IndexValue cells[] = new IndexValue[pNumRows]; 

    int idx = 0;
    switch(pSortColumn) {
    case 0:
      for(Boolean show : pShowRoots) {
        cells[idx] = new IndexValue(idx, show); 
        idx++;
      }
      break;

    case 1:
      for(String name : pNodeNames) {
        cells[idx] = new IndexValue(idx, name); 
        idx++;
      }
    }

    Comparator<IndexValue> comp = 
      pSortAscending ? new AscendingIndexValue() : new DescendingIndexValue(); 
    Arrays.sort(cells, comp);

    pRowToIndex = new int[pNumRows];
    int row; 
    for(row=0; row<pNumRows; row++) 
      pRowToIndex[row] = cells[row].getIndex();       

    fireTableDataChanged();
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   T A B L E   M O D E L   O V E R R I D E S                                            */
  /*----------------------------------------------------------------------------------------*/

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
    return (col == 0);
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
    int irow = pRowToIndex[row];
    switch(col) {
    case 0:
      return pShowRoots.get(irow); 

    case 1:
      return pNodeNames.get(irow); 

    default:
      assert(false);
      return null;
    }
  }

  /**
   * Sets the value in the cell at columnIndex and rowIndex to aValue.
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
    int vrow = pRowToIndex[row];
    boolean edited = setValueAtHelper(value, vrow, col);

    {
      int[] selected = pTable.getSelectedRows(); 
      int wk;
      for(wk=0; wk<selected.length; wk++) {
	int srow = pRowToIndex[selected[wk]];
	if(srow != vrow)
	  if(setValueAtHelper(value, srow, col))
	    edited = true;
      }
    }
      
    if(edited) 
      fireTableDataChanged();
  }
  
  public boolean 
  setValueAtHelper
  (
    Object value, 
    int srow, 
    int col
  ) 
  {
    switch(col) {
    case 0:
      pShowRoots.set(srow, (Boolean) value); 
      return true;

    default:
      return false;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1677539670306516603L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to show the node network rooted at the given node in the Node Viewer panel.
   */ 
  private ArrayList<Boolean>  pShowRoots; 
   
  /**
   * The name of the node associated with the jobs.
   */ 
  private ArrayList<String>   pNodeNames;

}
