// $Id: ArchiveCandidateTableModel.java,v 1.2 2005/02/07 14:52:59 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*;
import us.temerity.pipeline.ui.*;

import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   A R C H I V E   C A N D I D A T E   T A B L E   M O D E L                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A {@link SortableTableModel SortableTableModel} which contains 
 * {@link ArchivalInfo ArchivalInfo} instances which are candidates for archiving.
 */ 
public
class ArchiveCandidateTableModel
  extends SortableTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   */
  public 
  ArchiveCandidateTableModel() 
  {
    super();

    /* initialize the columns */ 
    { 
      pNumColumns = 8;

      {
	Class classes[] = { 
	  String.class, String.class, String.class, String.class, Integer.class, 
	  String.class, Integer.class, String.class
	}; 
	pColumnClasses = classes;
      }

      {
	String names[] = {
	  "Node Name", "Version", "Checked-In On", "Checked-Out On", "Working", 
	  "Archived On", "Archives", "Unused"
	};
	pColumnNames = names;
      }

      {
	String desc[] = {
	  "The fully resolved name of the node.", 
	  "The revision number of the checked-in version.", 
	  "When the checked-in version was created.", 
	  "When the checked-in version was last checked-out.", 
	  "The number of working versions based on the checked-in version existing in " +
	  "all working areas.", 
	  "When the checked-in version was last archived.", 
	  "The number of archives which contain the checked-in version.", 
	  "Whether the checked-in version can be offlined."
	};
	pColumnDescriptions = desc;
      }

      {
	int widths[] = { 360, 80, 180, 180, 80, 180, 80, 80 };
	pColumnWidths = widths;
      }

      {
	TableCellRenderer renderers[] = {
	  new JSimpleTableCellRenderer(JLabel.LEFT), 
	  new JSimpleTableCellRenderer(JLabel.CENTER), 
	  new JSimpleTableCellRenderer(JLabel.CENTER), 
	  new JSimpleTableCellRenderer(JLabel.CENTER), 
	  new JSimpleTableCellRenderer(JLabel.CENTER), 
	  new JSimpleTableCellRenderer(JLabel.CENTER), 
	  new JSimpleTableCellRenderer(JLabel.CENTER),
	  new JSimpleTableCellRenderer(JLabel.CENTER)
	};
	pRenderers = renderers;
      }

      {
	TableCellEditor editors[] = { 
	  null, null, null, null, null, null, null, null
	};
	pEditors = editors;
      }
    }

    pNames      = new ArrayList<String>();
    pVersionIDs = new ArrayList<VersionID>();
    pInfos      = new ArrayList<ArchivalInfo>();
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

    default:
      for(ArchivalInfo info : pInfos) {
	switch(pSortColumn) {
	case 2:
	  value = info.getCheckedInStamp(); 
	  break;

	case 3:
	  if(info.getCheckedOutStamp() != null)
	    value = info.getCheckedOutStamp();
	  else 
	    value = new Date(0L);
	  break;

	case 4:
	  value = new Integer(info.numWorking());
	  break;

	case 5:
	  if(info.getArchivedStamp() != null)
	    value = info.getArchivedStamp();
	  else 
	    value = new Date(0L);
	  break;

	case 6:
	  value = new Integer(info.numArchives());
	  break;

	case 7:
	  value = (info.canOffline() ? "YES" : "no");
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
   * Get the node names and revision numbers for the given rows. 
   */ 
  public TreeMap<String,TreeSet<VersionID>>
  getVersions
  (
   int[] rows
  )
  {
    TreeMap<String,TreeSet<VersionID>> table = new TreeMap<String,TreeSet<VersionID>>();

    int wk;
    for(wk=0; wk<rows.length; wk++) {
      int idx = pRowToIndex[rows[wk]];
    
      String name = pNames.get(idx);
      TreeSet<VersionID> versions = table.get(name);
      if(versions == null) {
	versions = new TreeSet<VersionID>();
	table.put(name, versions);
      }
      versions.add(pVersionIDs.get(idx));
    }

    return table;
  }
  
  /**
   * Get the node names and revision numbers for all rows. 
   */ 
  public TreeMap<String,TreeSet<VersionID>>
  getVersions() 
  {
    TreeMap<String,TreeSet<VersionID>> table = new TreeMap<String,TreeSet<VersionID>>();

    int idx;
    for(idx=0; idx<pNames.size(); idx++) {
      String name = pNames.get(idx);
      TreeSet<VersionID> versions = table.get(name);
      if(versions == null) {
	versions = new TreeSet<VersionID>();
	table.put(name, versions);
      }
      versions.add(pVersionIDs.get(idx));
    }

    return table;
  }

  /**
   * Get the node names and revision numbers for the given offlinable rows.
   */ 
  public TreeMap<String,TreeSet<VersionID>>
  getOfflineVersions
  (
   int[] rows
  )
  {
    TreeMap<String,TreeSet<VersionID>> table = new TreeMap<String,TreeSet<VersionID>>();

    int wk;
    for(wk=0; wk<rows.length; wk++) {
      int idx = pRowToIndex[rows[wk]];
    
      if(pInfos.get(idx).canOffline()) {
	String name = pNames.get(idx);
	TreeSet<VersionID> versions = table.get(name);
	if(versions == null) {
	  versions = new TreeSet<VersionID>();
	  table.put(name, versions);
	}
	versions.add(pVersionIDs.get(idx));
      }
    }

    return table;
  }

  /**
   * Set table data.
   * 
   * @param info
   *   The archival query information. 
   */ 
  public void
  setArchivalInfo
  (
   TreeMap<String,TreeMap<VersionID,ArchivalInfo>> info
  ) 
  {
    pNames.clear();
    pVersionIDs.clear();
    pInfos.clear();

    if(info != null) {
      for(String name : info.keySet()) {
	TreeMap<VersionID,ArchivalInfo> versions = info.get(name);
	for(VersionID vid : versions.keySet()) {
	  pNames.add(name);
	  pVersionIDs.add(vid);
	pInfos.add(versions.get(vid));
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
      return Dates.format(pInfos.get(irow).getCheckedInStamp());
      
    case 3:
      return Dates.format(pInfos.get(irow).getCheckedOutStamp());

    case 4:
      return String.valueOf(pInfos.get(irow).numWorking());
      
    case 5:
      return Dates.format(pInfos.get(irow).getArchivedStamp());

    case 6:
      return String.valueOf(pInfos.get(irow).numArchives());
      
    case 7:
      return (pInfos.get(irow).canOffline() ? "YES" : "no");
      
    default:
      assert(false);
      return null;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1953663862680540941L;



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
   * The archive information.
   */ 
  private ArrayList<ArchivalInfo> pInfos;

}
