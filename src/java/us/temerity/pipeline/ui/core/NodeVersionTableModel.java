// $Id: NodeVersionTableModel.java,v 1.8 2009/08/19 23:42:47 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
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
  extends AbstractSortableTableModel
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
      pNumColumns = 2;

      {
	Class classes[] = { 
	  String.class, String.class
	}; 
	pColumnClasses = classes;
      }

      {
	String names[] = {
	  "Node Name", "Version"
	};
	pColumnNames = names;
      }

      {
	String desc[] = {
	  "The fully resolved name of the node.", 
	  "The revision number of the checked-in version." 
	};
	pColumnDescriptions = desc;
      }

      {
        Vector3i ranges[] = {
          new Vector3i(180, 540, Integer.MAX_VALUE), 
          new Vector3i(80)
        };
        pColumnWidthRanges = ranges;
      }

      {
	TableCellRenderer renderers[] = {
	  new JSimpleTableCellRenderer(JLabel.LEFT), 
	  new JSimpleTableCellRenderer(JLabel.CENTER)
	};
	pRenderers = renderers;
      }

      {
	TableCellEditor editors[] = { 
	  null, null
	};
	pEditors = editors;
      }
    }

    pNames      = new ArrayList<String>();
    pVersionIDs = new ArrayList<VersionID>();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of version at the given row.
   */
  public String 
  getName
  (
   int row
  ) 
  {
    return pNames.get(pRowToIndex[row]);
  }

  /**
   * Get the revision number of the version at the given row.
   */
  public VersionID 
  getVersionID
  (
   int row
  ) 
  {
    return pVersionIDs.get(pRowToIndex[row]);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the fully resolved node names and revision numbers for all rows.
   */ 
  public TreeMap<String,TreeSet<VersionID>>
  getData()
  {
    TreeMap<String,TreeSet<VersionID>> versions = 
      new TreeMap<String,TreeSet<VersionID>>();

    int wk;
    for(wk=0; wk<pNames.size(); wk++) {
      String name = pNames.get(wk);
      TreeSet<VersionID> vids = versions.get(name);
      if(vids == null) {
	vids = new TreeSet<VersionID>();
	versions.put(name, vids);
      }
      vids.add(pVersionIDs.get(wk));
    }

    return versions;
  }

  /**
   * Get the fully resolved node names and revision numbers for the given rows.
   */ 
  public TreeMap<String,TreeSet<VersionID>>
  getData
  (
   int[] rows
  )
  {
    TreeSet<Integer> included = new TreeSet<Integer>();
    int wk;
    for(wk=0; wk<rows.length; wk++) 
      included.add(pRowToIndex[rows[wk]]);

    TreeMap<String,TreeSet<VersionID>> versions = 
      new TreeMap<String,TreeSet<VersionID>>();

    for(wk=0; wk<pNames.size(); wk++) {
      if(included.contains(wk)) {
	String name = pNames.get(wk);
	TreeSet<VersionID> vids = versions.get(name);
	if(vids == null) {
	  vids = new TreeSet<VersionID>();
	  versions.put(name, vids);
	}
	vids.add(pVersionIDs.get(wk));
      }
    }

    return versions;
  }

  /**
   * Get the fully resolved node names and revision numbers for all rows 
   * except the given rows. 
   */ 
  public TreeMap<String,TreeSet<VersionID>>
  getDataExcept
  (
   int[] rows
  )
  {
    TreeSet<Integer> excluded = new TreeSet<Integer>();
    int wk;
    for(wk=0; wk<rows.length; wk++) 
      excluded.add(pRowToIndex[rows[wk]]);

    TreeMap<String,TreeSet<VersionID>> versions = 
      new TreeMap<String,TreeSet<VersionID>>();

    for(wk=0; wk<pNames.size(); wk++) {
      if(!excluded.contains(wk)) {
	String name = pNames.get(wk);
	TreeSet<VersionID> vids = versions.get(name);
	if(vids == null) {
	  vids = new TreeSet<VersionID>();
	  versions.put(name, vids);
	}
	vids.add(pVersionIDs.get(wk));
      }
    }

    return versions;
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
   TreeMap<String,TreeSet<VersionID>> data 
  ) 
  {
    pNames.clear();
    pVersionIDs.clear();
    if(data != null) {
      for(String name : data.keySet()) {
	TreeSet<VersionID> vids	= data.get(name);
	for(VersionID vid : vids) {
	  pNames.add(name);
	  pVersionIDs.add(vid);
	}
      }
    }

    pNumRows = pNames.size(); 

    sort();
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
    Comparable value = null;
    int idx = 0;
    switch(pSortColumn) {
    case 0:
      for(String name : pNames) {
	value = name;
        cells[idx] = new IndexValue(idx, value); 
	idx++;
      }
      break;

    case 1:
      for(VersionID vid : pVersionIDs) {
	value = vid;
        cells[idx] = new IndexValue(idx, value); 
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
      
    default:
      assert(false);
      return null;
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

}
