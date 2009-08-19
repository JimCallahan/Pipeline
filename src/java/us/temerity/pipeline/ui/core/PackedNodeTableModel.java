// $Id: PackedNodeTableModel.java,v 1.2 2009/08/19 23:42:47 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.ui.*;

import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   P A C K E D   N O D E   T A B L E    M O D E L                                         */
/*------------------------------------------------------------------------------------------*/

/**
 * A {@link SortableTableModel} which contains information about nodes packed into a node
 * bundle.
 */ 
public
class PackedNodeTableModel
  extends AbstractSortableTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   */
  public 
  PackedNodeTableModel() 
  {
    super();

    /* initialize the columns */ 
    { 
      pNumColumns = 3;

      {
	Class classes[] = { 
	  String.class, Boolean.class, String.class, 
	}; 
	pColumnClasses = classes;
      }

      {
	String names[] = {
	  "Node Name", "Locked", "Version"
	};
	pColumnNames = names;
      }

      {
	String desc[] = {
	  "The fully resolved name of the node.",
          "Whether the node is locked in the bundle.", 
	  "The local revision number to use for a locked node.", 
	};
	pColumnDescriptions = desc;
      }

      {
        Vector3i ranges[] = {
          new Vector3i(120, 500, Integer.MAX_VALUE), 
          new Vector3i(80),
          new Vector3i(80)
        };
        pColumnWidthRanges = ranges;
      }

      {
	TableCellRenderer renderers[] = {
	  new JSimpleTableCellRenderer(JLabel.LEFT), 
	  new JBooleanTableCellRenderer(JLabel.CENTER), 
	  new JSimpleTableCellRenderer(JLabel.CENTER)
	};
	pRenderers = renderers;
      }

      {
        ArrayList<String> values = new ArrayList<String>();
        values.add("-"); 

	TableCellEditor editors[] = { 
	  null, null, new JLockedVersionTableCellEditor(values, this, 80)
	};
	pEditors = editors;
      }
    }

    pNames    = new ArrayList<String>();
    pIsLocked = new ArrayList<Boolean>(); 
    pLockedID = new ArrayList<VersionID>(); 

    pLockableVersionIDs = new TreeMap<String,TreeSet<VersionID>>(); 
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the node at the given row.
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
   * Get whether the the node at the given row is locked in the node bundle. 
   */
  public Boolean
  isLocked
  (
   int row
  ) 
  {
    return pIsLocked.get(pRowToIndex[row]);
  }

  /**
   * Get the local checked-in revision number to use for a locked node at the given row.
   */
  public VersionID 
  getLockedID
  (
   int row
  ) 
  {
    return pLockedID.get(pRowToIndex[row]);
  }

  /**
   * Get the local checked-in revision numbers for all locked nodes. 
   */
  public TreeMap<String,VersionID> 
  getAllLockedIDs() 
  {
    TreeMap<String,VersionID> locked = new TreeMap<String,VersionID>();

    int row;
    for(row=0; row<pRowToIndex.length; row++) {
      int irow = pRowToIndex[row];
      if(pIsLocked.get(irow)) {
        String name = pNames.get(irow);
        VersionID vid = pLockedID.get(irow);
        if(vid != null) 
          locked.put(name, vid); 
      }
    }

    return locked; 
  }

  /**
   * Get the possible checked-in versions to lock for the node at the given row.
   */
  public TreeSet<VersionID> 
  getLockableVersionIDs
  (
   int row
  ) 
  {
    String name = pNames.get(pRowToIndex[row]);
    if(name != null) 
      return pLockableVersionIDs.get(name);
    return null;
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the table data.
   * 
   * @param names
   *   The names of nodes contained in the bundle.
   * 
   * @param locked
   *   The names of locked nodes.
   * 
   * @param versionIDs
   *   The possible checked-in revision numbers for locked nodes.
   */ 
  public void
  setData
  (
   TreeSet<String> names,
   TreeSet<String> locked, 
   TreeMap<String,TreeSet<VersionID>> versionIDs
  ) 
  {
    pNames.clear();
    pIsLocked.clear();
    pLockedID.clear();
    pLockableVersionIDs.clear();
    for(String name : names) {
      pNames.add(name); 

      boolean isLocked = locked.contains(name);
      pIsLocked.add(isLocked);
      
      VersionID lvid = null;
      if(isLocked) {
        TreeSet<VersionID> vids = versionIDs.get(name); 
        if((vids != null) && !vids.isEmpty()) {
          pLockableVersionIDs.put(name, vids);
          lvid = vids.last();
        }
      }  
      pLockedID.add(lvid);
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
      for(Boolean locked : pIsLocked) {
	value = locked;
        cells[idx] = new IndexValue(idx, value); 
	idx++;
      }
      break;

    case 2:
      for(VersionID vid : pLockedID) {
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
    int irow = pRowToIndex[row];
    switch(col) {
    case 2:
      if(pIsLocked.get(irow)) {
        String name = pNames.get(irow);
        if(name != null) 
          return (pLockableVersionIDs.get(name) != null);
      }
      return false;

    default:
      return false; 
    }
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
      return pIsLocked.get(irow);
      
    case 2: 
      if(pIsLocked.get(irow)) {
        VersionID vid = pLockedID.get(irow); 
        if(vid != null) 
          return vid.toString(); 
        return "Missing";
      }
      else {
        return "-";
      }

    default:
      assert(false);
      return null;
    }
  }

  /**
   * Sets the value in the cell at columnIndex and rowIndex to aValue.
   */ 
  public void 
  setValueAt
  (
   Object value, 
   int row, 
   int col
  ) 
  {
    int vrow = pRowToIndex[row];
    setValueAtHelper(value, vrow, col);

    int[] selected = pTable.getSelectedRows(); 
    int wk;
    for(wk=0; wk<selected.length; wk++) {
      int srow = pRowToIndex[selected[wk]];
      if(srow != vrow)
	setValueAtHelper(value, srow, col);
    }

    fireTableDataChanged();
  }

  public void 
  setValueAtHelper
  (
   Object value, 
   int srow, 
   int col
  ) 
  {
    switch(col) {
    case 2:
      if(pIsLocked.get(srow)) {
        String name = pNames.get(srow);
        TreeSet<VersionID> vids = pLockableVersionIDs.get(name);
        String vstr = (String) value;
        if((vstr != null) && !vstr.equals("-")) {
          VersionID vid = new VersionID(vstr); 
          if(vids.contains(vid)) 
            pLockedID.set(srow, vid); 
        }
        else {
          pLockedID.set(srow, null); 
        }
      }
      break;
      
    default:
      assert(false);
    }  
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6233987526571153500L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved node names.
   */ 
  private ArrayList<String> pNames;
  
  /**
   * Whether each node is locked in the node bundle.
   */ 
  private ArrayList<Boolean> pIsLocked; 

  /**
   * The local checked-in revision to use for a locked node 
   * or <CODE>null</CODE> if the node is not locked or no local checked-in versions exist.
   */ 
  private ArrayList<VersionID> pLockedID; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The possible checked-in revision numbers for locked nodes.
   */ 
  private TreeMap<String,TreeSet<VersionID>>  pLockableVersionIDs;

}
