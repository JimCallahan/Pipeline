// $Id: NodeVersionTableModel.java,v 1.7 2005/03/23 20:46:24 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
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
	int widths[] = { 540, 80 };
	pColumnWidths = widths;
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
