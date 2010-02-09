// $Id: RestoreRequestTableModel.java,v 1.4 2009/08/19 23:42:47 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.ui.*;

import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   R E S T O R E   R E Q U E S T   T A B L E   M O D E L                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A {@link SortableTableModel SortableTableModel} which contains 
 * {@link RestoreRequest RestoreRequest} instances.
 */ 
public
class RestoreRequestTableModel
  extends AbstractSortableTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   */
  public 
  RestoreRequestTableModel() 
  {
    super();

    /* initialize the columns */ 
    { 
      pNumColumns = 7;

      {
	Class classes[] = { 
	  String.class, String.class, String.class, String.class, String.class, 
          String.class, String.class
	}; 
	pColumnClasses = classes;
      }

      {
	String names[] = {
	  "Node Name", "Version", "State", "User", "Submitted", "Completed", "Archive Volume"
	};
	pColumnNames = names;
      }

      {
	String desc[] = {
	  "The fully resolved name of the node.", 
	  "The revision number of the checked-in version.", 
	  "The current state of the request.",
          "The name of the user which submitted the restore request.", 
	  "When the request to restore the checked-in version was submitted.",
	  "When the either the checked-in version was restored or the request was denied.", 
	  "The name of the archive volume from which the checked-in version was restored."
	};
	pColumnDescriptions = desc;
      }

      {
        Vector3i ranges[] = {
          new Vector3i(180, 460, Integer.MAX_VALUE), 
          new Vector3i(80), 
          new Vector3i(80), 
          new Vector3i(80, 120, Integer.MAX_VALUE), 
          new Vector3i(180), 
          new Vector3i(180),
          new Vector3i(240) 
        };
        pColumnWidthRanges = ranges;
      }

      {
	TableCellRenderer renderers[] = {
	  new JSimpleTableCellRenderer(JLabel.LEFT), 
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
    
    pNames    = new ArrayList<String>();
    pVersions = new ArrayList<VersionID>();    
    pRequests = new ArrayList<RestoreRequest>();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the node names and revision numbers for the given Pending rows.
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

      RestoreRequest rr = pRequests.get(idx);
      switch(rr.getState()) {
      case Pending:
	{
	  String name = pNames.get(idx);
	  TreeSet<VersionID> versions = table.get(name);
	  if(versions == null) {
	    versions = new TreeSet<VersionID>();
	    table.put(name, versions);
	  }
	  versions.add(pVersions.get(idx));
	}
      }
    }

    return table;
  }
  
  /**
   * Get the node names and revision numbers for all Pending rows. 
   */ 
  public TreeMap<String,TreeSet<VersionID>>
  getVersions() 
  {
    TreeMap<String,TreeSet<VersionID>> table = new TreeMap<String,TreeSet<VersionID>>();

    int idx = 0;
    for(RestoreRequest rr : pRequests) {
      switch(rr.getState()) {
      case Pending:
	{
	  String name = pNames.get(idx);
	  TreeSet<VersionID> versions = table.get(name);
	  if(versions == null) {
	    versions = new TreeSet<VersionID>();
	    table.put(name, versions);
	  }
	  versions.add(pVersions.get(idx));
	}
      }
      
      idx++;
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
  setRestoreRequests
  (
   TreeMap<String,TreeMap<VersionID,RestoreRequest>> reqs
  ) 
  {
    pNames.clear();
    pVersions.clear();
    pRequests.clear();
    if(reqs != null) {
      for(String name : reqs.keySet()) {
	TreeMap<VersionID,RestoreRequest> vreqs = reqs.get(name);
	for(VersionID vid : vreqs.keySet()) {
	  pNames.add(name);
	  pVersions.add(vid);
	  pRequests.add(vreqs.get(vid));
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
    int idx = 0;
    for(RestoreRequest rr : pRequests) {
      Comparable value = null;
      switch(pSortColumn) {
      case 0:
	value = pNames.get(idx);
	break;
	
      case 1:
	value = pVersions.get(idx);
	break;
	
      case 2: 
	value = rr.getState().toTitle();
	break;

      case 3: 
	value = rr.getRequestor();
	break;

      case 4:
	value = rr.getSubmittedStamp();
	break;
	
      case 5:
        value = rr.getResolvedStamp();
	break;
	
      case 6:
        value = rr.getArchiveName();
      }
     
      cells[idx] = new IndexValue(idx, value); 
      idx++;
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
    RestoreRequest rr = pRequests.get(irow);
    switch(col) {
    case 0:
      return pNames.get(irow);
      
    case 1:
      return pVersions.get(irow);

    case 2: 
      return rr.getState().toTitle();

    case 3: 
      return rr.getRequestor();

    case 4:
      return TimeStamps.format(rr.getSubmittedStamp());
	
    case 5:
      return TimeStamps.format(rr.getResolvedStamp());
      
    case 6:
      return rr.getArchiveName();
      
    default:
      assert(false);
      return null;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7511122747425377008L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved node names of the restore requests. 
   */ 
  private ArrayList<String>  pNames; 

  /**
   * The revision numbers of the restore requests.
   */ 
  private ArrayList<VersionID>  pVersions; 

  /**
   * The restore requests. 
   */ 
  private ArrayList<RestoreRequest>  pRequests;

}
