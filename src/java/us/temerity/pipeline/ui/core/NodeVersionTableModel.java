// $Id: NodeVersionTableModel.java,v 1.1 2005/01/03 06:56:24 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*;
import us.temerity.pipeline.ui.*;

import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   V E R S I O N   T A B L E   M O D E L                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A {@link SortableTableModel SortableTableModel} which contains node versions.
 */ 
public
class NodeVersionTableModel
  extends SortableTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   */
  public 
  NodeVersionTableModel() 
  {
    super();

    /* initialize the columns */ 
    { 
      pNumColumns = 3;

      {
	Class classes[] = { 
	  String.class, String.class, String.class
	}; 
	pColumnClasses = classes;
      }

      {
	String names[] = {
	  "Node Name", "Version", "Size"
	};
	pColumnNames = names;
      }

      {
	String desc[] = {
	  "The fully resolved name of the node.", 
	  "The revision number of the checked-in version.", 
	  "The size (in bytes) of the files associated with the checked-in version."
	};
	pColumnDescriptions = desc;
      }

      {
	int widths[] = { 416, 80, 80 };
	pColumnWidths = widths;
      }

      {
	TableCellRenderer renderers[] = {
	  new JSimpleTableCellRenderer(JLabel.LEFT), 
	  new JSimpleTableCellRenderer(JLabel.CENTER),
	  new JSimpleTableCellRenderer(JLabel.CENTER)
	};
	pRenderers = renderers;
      }

      {
	TableCellEditor editors[] = { 
	  null, null, null
	};
	pEditors = editors;
      }
    }

    pNames      = new ArrayList<String>();
    pVersionIDs = new ArrayList<VersionID>();
    pSizes      = new ArrayList<Long>();
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
    Comparable value = null;

    int idx = 0;
    switch(pSortColumn) {
    case 0:
      for(String name : pNames) {
	value = name;
      
	int wk;
	for(wk=0; wk<values.size(); wk++) {
	  if(value.compareTo(values.get(wk)) > 0) 
	    break;
	}
	values.add(wk, value);
	indices.add(wk, idx);

	idx++;
      }
      break;

    case 1:
      for(VersionID vid : pVersionIDs) {
	value = vid;
      
	int wk;
	for(wk=0; wk<values.size(); wk++) {
	  if(value.compareTo(values.get(wk)) > 0) 
	    break;
	}
	values.add(wk, value);
	indices.add(wk, idx);

	idx++;
      }
      break;

    case 2:
      for(Long size : pSizes) {
	value = size;
      
	int wk;
	for(wk=0; wk<values.size(); wk++) {
	  if(value.compareTo(values.get(wk)) > 0) 
	    break;
	}
	values.add(wk, value);
	indices.add(wk, idx);

	idx++;
      }
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
   * Get the file sizes indexed by fully resolved node name and revision number.
   */ 
  public TreeMap<String,TreeMap<VersionID,Long>>
  getData()
  {
    TreeMap<String,TreeMap<VersionID,Long>> table = 
      new TreeMap<String,TreeMap<VersionID,Long>>();

    int wk;
    for(wk=0; wk<pNames.size(); wk++) {
      String name = pNames.get(wk);
      TreeMap<VersionID,Long> versions = table.get(name);
      if(versions == null) {
	versions = new TreeMap<VersionID,Long>();
	table.put(name, versions);
      }
      versions.put(pVersionIDs.get(wk), pSizes.get(wk));
    }

    return table;
  }

  /**
   * Get the file sizes indexed by fully resolved node name and revision number 
   * for all rows except the given rows. 
   */ 
  public TreeMap<String,TreeMap<VersionID,Long>>
  getDataExcept
  (
   int[] rows
  )
  {
    TreeSet<Integer> exclude = new TreeSet<Integer>();
    int wk;
    for(wk=0; wk<rows.length; wk++) 
      exclude.add(pRowToIndex[rows[wk]]);
      
    TreeMap<String,TreeMap<VersionID,Long>> table = 
      new TreeMap<String,TreeMap<VersionID,Long>>();

    for(wk=0; wk<pNames.size(); wk++) {
      if(!exclude.contains(wk)) {
	String name = pNames.get(wk);
	TreeMap<VersionID,Long> versions = table.get(name);
	if(versions == null) {
	  versions = new TreeMap<VersionID,Long>();
	  table.put(name, versions);
	}
	versions.put(pVersionIDs.get(wk), pSizes.get(wk));
      }
    }

    return table;
  }

  /**
   * Set file sizes indexed by fully resolved node name and revision number.
   * 
   * @param data 
   *   The table data.
   */ 
  public void
  setData
  (
   TreeMap<String,TreeMap<VersionID,Long>> data
  ) 
  {
    pNames.clear();
    pVersionIDs.clear();
    pSizes.clear();

    if(data != null) {
      for(String name : data.keySet()) {
	TreeMap<VersionID,Long> versions = data.get(name);
	for(VersionID vid : versions.keySet()) {
	  pNames.add(name);
	  pVersionIDs.add(vid);
	  pSizes.add(versions.get(vid));
	}
      }
    }

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
    return pNames.size();
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
    int irow = pRowToIndex[row];
    switch(col) {
    case 0:
      return pNames.get(irow);
      
    case 1:
      return pVersionIDs.get(irow).toString();
      
    case 2:
      return formatLong(pSizes.get(irow));
      
    default:
      assert(false);
      return null;
    }
  }

  /**
   * Generates a formatted string representation of a large integer number.
   */ 
  private String
  formatLong
  (
   Long value
  ) 
  {
    if(value == null) 
      return "-";

    if(value < 1024) {
      return value.toString();
    }
    else if(value < 1048576) {
      double k = ((double) value) / 1024.0;
      return String.format("%1$.1fK", k);
    }
    else if(value < 1073741824) {
      double m = ((double) value) / 1048576.0;
      return String.format("%1$.1fM", m);
    }
    else {
      double g = ((double) value) / 1073741824.0;
      return String.format("%1$.1fG", g);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7616499335525120627L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved node names.
   */ 
  private ArrayList<String> pNames;

  /**
   * The revision numbers.
   */ 
  private ArrayList<VersionID> pVersionIDs;

  /**
   * The total sizes of the files associated with the node version.
   */ 
  private ArrayList<Long> pSizes;

}
