// $Id: RestoreRequestTableModel.java,v 1.3 2007/03/28 20:07:15 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
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
      pNumColumns = 6;

      {
	Class classes[] = { 
	  String.class, String.class, String.class, String.class, String.class, String.class
	}; 
	pColumnClasses = classes;
      }

      {
	String names[] = {
	  "Node Name", "Version", "State", "Submitted", "Completed", "Archive Volume"
	};
	pColumnNames = names;
      }

      {
	String desc[] = {
	  "The fully resolved name of the node.", 
	  "The revision number of the checked-in version.", 
	  "The current state of the request.",
	  "When the request to restore the checked-in version was submitted.",
	  "When the either the checked-in version was restored or the request was denied.", 
	  "The name of the archive volume from which the checked-in version was restored."
	};
	pColumnDescriptions = desc;
      }

      {
	int widths[] = { 460, 80, 80, 180, 180, 240 };
	pColumnWidths = widths;
      }

      {
	TableCellRenderer renderers[] = {
	  new JSimpleTableCellRenderer(JLabel.LEFT), 
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
	  null, null, null, null, null, null, null
	};
	pEditors = editors;
      }
    }
    
    pNames    = new ArrayList<String>();
    pVersions = new ArrayList<VersionID>();    
    pRequests = new ArrayList<RestoreRequest>();
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
    for(RestoreRequest rr : pRequests) {
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
	value = rr.getSubmittedStamp();
	break;
	
      case 4:
	if(rr.getResolvedStamp() != null)
	  value = rr.getResolvedStamp();
	else 
	  value = new Long(0L);
	break;
	
      case 5:
	if(rr.getArchiveName() != null)
	  value = rr.getArchiveName();
	else 
	  value = "";
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
    return pRequests.size();
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
    RestoreRequest rr = pRequests.get(irow);
    switch(col) {
    case 0:
      return pNames.get(irow);
      
    case 1:
      return pVersions.get(irow);

    case 2: 
      return rr.getState().toTitle();

    case 3:
      return TimeStamps.format(rr.getSubmittedStamp());
	
    case 4:
      return TimeStamps.format(rr.getResolvedStamp());
      
    case 5:
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
