// $Id: OfflineCandidateTableModel.java,v 1.4 2007/03/28 20:07:15 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
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
 * {@link OfflineInfo OfflineInfo} instances which are candidates for offlining.
 */ 
public
class OfflineCandidateTableModel
  extends AbstractSortableTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   */
  public 
  OfflineCandidateTableModel() 
  {
    super();

    /* initialize the columns */ 
    { 
      pNumColumns = 8;

      {
	Class classes[] = { 
	  String.class, String.class, String.class, Integer.class, 
	  String.class, String.class, Integer.class, String.class
	}; 
	pColumnClasses = classes;
      }

      {
	String names[] = {
	  "Node Name", "Version", "Archived On", "Archives", 
	  "Checked-Out", "Owner|View", "Working", "Unused", 
	};
	pColumnNames = names;
      }

      {
	String desc[] = {
	  "The fully resolved name of the node.", 
	  "The revision number of the checked-in version.", 
	  "When the checked-in version was last archived.", 
	  "The number of archive volumes which contain the checked-in version.", 
	  "When the checked-in version was last checked-out.", 
	  "The working area where the version was last checked-out.", 
	  "The number of working versions based on the checked-in version existing in " +
	  "all working areas.", 
	  "Whether the checked-in version can be offlined."
	};
	pColumnDescriptions = desc;
      }

      {
	int widths[] = { 360, 80, 180, 80, 180, 180, 80, 80 };
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
    
    pInfos = new ArrayList<OfflineInfo>();
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
    for(OfflineInfo info : pInfos) {
      switch(pSortColumn) {
      case 0:
	value = info.getName();
	break;
	
      case 1:
	value = info.getVersionID();
	break;

      case 2:
	if(info.getArchivedStamp() != null)
	  value = info.getArchivedStamp();
	else 
	  value = new Long(0L);
	break;
	
      case 3:
	value = new Integer(info.numArchives());
	break;
	
      case 4:
	if(info.getCheckedOutStamp() != null)
	  value = info.getCheckedOutStamp();
	else 
	  value = new Long(0L);
	break;

      case 5:
	if((info.getAuthor() != null) && (info.getView() != null)) 
	  value = (info.getAuthor() + "|" + info.getView());
	else 
	  value = "";
	break;
	
      case 6:
	value = new Integer(info.numWorking());
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
   * Get the node names and revision numbers for the given offlinable rows.
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

      OfflineInfo info = pInfos.get(idx);
      if(info.canOffline()) {
	String name = info.getName();
	TreeSet<VersionID> versions = table.get(name);
	if(versions == null) {
	  versions = new TreeSet<VersionID>();
	  table.put(name, versions);
	}
	versions.add(info.getVersionID());
      }
    }

    return table;
  }
  
  /**
   * Get the node names and revision numbers for all offlinable rows. 
   */ 
  public TreeMap<String,TreeSet<VersionID>>
  getVersions() 
  {
    TreeMap<String,TreeSet<VersionID>> table = new TreeMap<String,TreeSet<VersionID>>();

    for(OfflineInfo info : pInfos) {
      if(info.canOffline()) {
	String name = info.getName();
	TreeSet<VersionID> versions = table.get(name);
	if(versions == null) {
	  versions = new TreeSet<VersionID>();
	  table.put(name, versions);
	}
	versions.add(info.getVersionID());
      }
    }

    return table;
  }

  /**
   * Set the table data.
   * 
   * @param info
   *   The archive information. 
   */ 
  public void
  setOfflineInfo
  (
   ArrayList<OfflineInfo> infos
  ) 
  {
    pInfos.clear();

    if(infos != null) 
      pInfos.addAll(infos);

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
    return pInfos.size();
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
    OfflineInfo info = pInfos.get(irow);
    switch(col) {
    case 0:
      return info.getName();
      
    case 1:
      return info.getVersionID();

    case 2:
      return TimeStamps.format(info.getArchivedStamp());

    case 3:
      return String.valueOf(info.numArchives());

    case 4:
      return TimeStamps.format(info.getCheckedOutStamp());
      
    case 5:
      if((info.getAuthor() != null) && (info.getView() != null)) 
	return (info.getAuthor() + "|" + info.getView());
      else 
	return null; 

    case 6:
      return String.valueOf(info.numWorking());
      
    case 7:
      return (info.canOffline() ? "YES" : "no");

    default:
      assert(false);
      return null;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2070602647052424009L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The archive information.
   */ 
  private ArrayList<OfflineInfo> pInfos;

}
