// $Id: WorkGroupsTableModel.java,v 1.1 2009/08/19 23:42:47 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.ui.*;

import java.text.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   W O R K   G R O U P S   T A B L E   M O D E L                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A {@link SortableTableModel SortableTableModel} which WorkGroup and information for 
 * each Pipeline user.
 */ 
public
class WorkGroupsTableModel
  extends AbstractPrivsTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   */
  public 
  WorkGroupsTableModel
  (
   JManagePrivilegesDialog parent
  ) 
  {
    super(parent); 
    
    /* initialize the fields */ 
    {
      pGroupNames       = new ArrayList<String>();
      pGroupMemberships = new ArrayList<ArrayList<Boolean>>();
    }

    pCellRenderer = new JWorkGroupMemberTableCellRenderer(); 
    pCellEditor   = new JWorkGroupMemberTableCellEditor(120); 
  }
 

  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set table data.
   *
   * @param groups
   *   The current work groups used to determine the scope of administrative privileges.
   * 
   * @param details
   *   The privileges granted to the current user with respect to all other users.
   */ 
  public void
  setWorkGroups
  (
   WorkGroups groups, 
   PrivilegeDetails details
  ) 
  {
    super.setPrivileges(groups, details); 

    pGroupNames.clear();
    if(groups != null) 
      pGroupNames.addAll(groups.getGroups());

    int gsize = pGroupNames.size();
    pGroupMemberships.clear();
    for(String uname : pUserNames) {
      ArrayList<Boolean> members = new ArrayList<Boolean>(gsize);
      for(String gname : pGroupNames) 
	members.add(groups.isMemberOrManager(uname, gname));

      pGroupMemberships.add(members);
    }

    sort();   
    fireTableStructureChanged(); 
  }

  /**
   * Get the modified work group memberships.
   */ 
  public DoubleMap<String,String,Boolean>  
  getModifiedWorkGroupMemberships() 
  {
    DoubleMap<String,String,Boolean> members = new DoubleMap<String,String,Boolean>();

    for(Integer idx : pEditedRows) {
      ArrayList<Boolean> flags = pGroupMemberships.get(idx);
      int ck;
      for(ck=0; ck<flags.size(); ck++) 
	members.put(pUserNames.get(idx), pGroupNames.get(ck), flags.get(ck));
    }

    return members;
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
    int idx;
    for(idx=0; idx<pNumRows; idx++) {
      Comparable value = null;
      switch(pSortColumn) {
      case 0:
	value = pUserNames.get(idx);
	break;

      default:
	value = pGroupMemberships.get(idx).get(pSortColumn-1);
      }
      
      cells[idx] = new IndexValue(idx, value); 
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
  /*   S O R T A B L E   T A B L E   M O D E L   O V E R R I D E S                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the description of the column columnIndex used in tool tips.
   */ 
  public String 	
  getColumnDescription
  (
   int col
  ) 
  {
    switch(col) {
    case 0:
      return "The names of each user."; 

    default:
      return "Work group membership.";
    }
  }
  
  /**
   * Returns the color prefix used to determine the synth style of the header button for 
   * the given column.
   */ 
  public String 	
  getColumnColorPrefix
  (
   int col
  )
  { 
    switch(col) {
    case 0:
      return ""; 

    default:
      return "Green"; 
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   T A B L E   M O D E L   O V E R R I D E S                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the most specific superclass for all the cell values in the column.
   */
  public Class 	
  getColumnClass
  (
   int col
  )
  {
    switch(col) {
    case 0:
      return String.class; 

    default:
      return Boolean.class;
    }
  }
  
  /**
   * Returns the number of columns in the model.
   */ 
  public int
  getColumnCount()
  {
    return (pGroupNames.size() + 1);
  }

  /**
   * Returns the name of the column at columnIndex.
   */ 
  public String 	
  getColumnName
  (
   int col
  ) 
  {
    switch(col) {
    case 0:
      return "User Name";

    default:
      return pGroupNames.get(col-1);
    }
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
    if(!pPrivilegeDetails.isMasterAdmin()) 
      return false;

    int srow = pRowToIndex[row];

    String uname = pUserNames.get(srow);
    if(uname.equals(PackageInfo.sPipelineUser))
      return false;

    switch(col) {
    case 0:
      return false;
      
    default:
      return true; 
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
    int srow = pRowToIndex[row];
    switch(col) {
    case 0:
      return pUserNames.get(srow);

    default:
      return pGroupMemberships.get(srow).get(col-1);
    }
  }

  /**
   * Sets the value in the cell at columnIndex and rowIndex to aValue.
   */ 
  protected boolean 
  setValueAtHelper
  (
   Object value, 
   int srow, 
   int col
  ) 
  {
    switch(col) {
    case 0:
      return false;
   
    default:
      pGroupMemberships.get(srow).set(col-1, (Boolean) value);
      pEditedRows.add(srow);
    }
    
    return true;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3298562617566392573L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The names of the work groups.
   */ 
  private ArrayList<String>  pGroupNames; 

  /**
   * The memberships of users in work groups indexed by user (row) and group (col-6).<P>
   * 
   * Where (true) = Manager, (false) = Member and (null) = Not part of group.
   */ 
  private ArrayList<ArrayList<Boolean>>  pGroupMemberships; 


}
