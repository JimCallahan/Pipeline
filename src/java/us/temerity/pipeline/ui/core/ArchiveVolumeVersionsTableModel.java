// $Id: ArchiveVolumeVersionsTableModel.java,v 1.1 2005/04/03 01:54:23 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   A R C H I V E   V O L U M E   V E R S I O N S   T A B L E    M O D E L                 */
/*------------------------------------------------------------------------------------------*/

/**
 * A {@link SortableTableModel SortableTableModel} which contains information about the 
 * checked-in node versions contained in an archive volume. 
 */ 
public
class ArchiveVolumeVersionsTableModel
  extends AbstractSortableTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   */
  public 
  ArchiveVolumeVersionsTableModel() 
  {
    super();

    /* initialize the columns */ 
    { 
      pNumColumns = 4;

      {
	Class classes[] = { 
	  String.class, String.class, String.class, String.class
	}; 
	pColumnClasses = classes;
      }

      {
	String names[] = {
	  "Node Name", "Version", "Size", "Online" 
	};
	pColumnNames = names;
      }

      {
	String desc[] = {
	  "The fully resolved name of the node.", 
	  "The revision number of the checked-in version.", 
	  "The size (in bytes) of the files associated with the checked-in version.", 
	  "Whether the checked-in version is currently online."
	};
	pColumnDescriptions = desc;
      }

      {
	int widths[] = { 732, 80, 80, 80 };
	pColumnWidths = widths;
      }

      {
	TableCellRenderer renderers[] = {
	  new JSimpleTableCellRenderer(JLabel.LEFT), 
	  new JSimpleTableCellRenderer(JLabel.CENTER),
	  new JSimpleTableCellRenderer(JLabel.CENTER),
	  new JSimpleTableCellRenderer(JLabel.CENTER)
	};
	pRenderers = renderers;
      }

      {
	TableCellEditor editors[] = { 
	  null, null, null, null
	};
	pEditors = editors;
      }
    }

    pNames      = new ArrayList<String>();
    pVersionIDs = new ArrayList<VersionID>();
    pSizes      = new ArrayList<Long>();
    pOnline     = new ArrayList<Boolean>();
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
      break;

    case 2:
      for(Long size : pSizes) {
	value = size;
	if(value == null) 
	  value = new Long(0L);
	
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

    case 3:
      for(Boolean online : pOnline) {
	value = online;
	
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

  /**
   * Get whether the version at the given row is currently online.
   */
  public boolean 
  getIsOnline
  (
   int row
  ) 
  {
    return pOnline.get(pRowToIndex[row]);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the table data.
   * 
   * @param volume 
   *   The archive volume.
   * 
   * @param offline
   *   The revision numbers of all offline versions indexed by fully resolved node name.
   */ 
  public void
  setData
  (
   ArchiveVolume volume, 
   TreeMap<String,TreeSet<VersionID>> offline
  ) 
  {
    pNames.clear();
    pVersionIDs.clear();
    pSizes.clear();
    pOnline.clear();

    if((volume != null) && (offline != null)) {
      for(String name : volume.getNames()) {
	for(VersionID vid : volume.getVersionIDs(name)) {
	  pNames.add(name);
	  pVersionIDs.add(vid);
	  pSizes.add(volume.getSize(name, vid));
	  pOnline.add(!(offline.containsKey(name) && offline.get(name).contains(vid)));
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
      
    case 3:
      return (pOnline.get(irow) ? "YES" : "no");

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

  private static final long serialVersionUID = -8681747486282920783L;



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

  /**
   * Whether the node version is currently online.
   */ 
  private ArrayList<Boolean>  pOnline; 
}
