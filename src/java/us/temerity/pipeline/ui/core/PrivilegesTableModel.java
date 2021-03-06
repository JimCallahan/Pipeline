// $Id: PrivilegesTableModel.java,v 1.5 2009/08/19 23:42:47 jim Exp $

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
/*   P R I V I L E G E S  T A B L E   M O D E L                                             */
/*------------------------------------------------------------------------------------------*/

/**
 * A {@link SortableTableModel SortableTableModel} which contains both WorkGroup and 
 * Privilege information for each Pipeline user.
 */ 
public
class PrivilegesTableModel
  extends AbstractPrivsTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   */
  public 
  PrivilegesTableModel
  (
   JManagePrivilegesDialog parent
  ) 
  {
    super(parent); 
    
    /* initialize the fields */ 
    {
      pUserPrivileges = new ArrayList<Privileges>();
    }

    /* initialize the columns */ 
    { 
      pNumColumns = 7;

      {
        Class cls[] = {
          String.class, 
          Boolean.class, Boolean.class, Boolean.class, 
          Boolean.class, Boolean.class, Boolean.class
        };
        
        pColumnClasses = cls;
      }

      {
	String names[] = { 
	  "User Name", 
          "Master Admin", "Developer", "Annotator", 
          "Queue Admin", "Queue Manager", "Node Manager"
	};
	pColumnNames = names;
      }

      {
	String colors[] = { 
          "", 
          "Blue", "Blue", "Blue",  
          "Blue", "Blue", "Blue"
        };
	pColumnColorPrefix = colors; 
      }

      {
	String desc[] = {
          "The names of each user.", 
	  "Whether the user has full administrative privileges.", 
	  "Whether the user has developer privileges.", 
	  "Whether the user has annotator privileges.", 
	  "Whether the user has queue administration privileges.", 
	  "Whether the user has queue manager privileges.", 
	  "Whether the user has node manager privileges.", 
	};
	pColumnDescriptions = desc;
      }
    }

    pCellRenderer = new JBooleanTableCellRenderer("Blue", JLabel.CENTER, true); 

    {
      JBooleanTableCellEditor editor = 
        new JBooleanTableCellEditor(120, JLabel.CENTER, false); 
      editor.setSynthPrefix("Blue");
      pCellEditor = editor;
    }
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
   * @param privileges
   *   The administrative privileges for all users.
   * 
   * @param details
   *   The privileges granted to the current user with respect to all other users.
   */ 
  public void 
  setPrivileges
  (
   WorkGroups groups, 
   TreeMap<String,Privileges> privileges,
   PrivilegeDetails details
  ) 
  {
    super.setPrivileges(groups, details); 

    pUserPrivileges.clear();
    for(String uname : pUserNames) {
      Privileges privs = null; 
      if(privileges != null) 
	privs = privileges.get(uname);
      if(privs == null) 
	privs = new Privileges();
      pUserPrivileges.add(privs);
    }

    sort();
  }

  /**
   * Get the modified privileges indexed by user name.
   */ 
  public TreeMap<String,Privileges>
  getModifiedPrivileges() 
  {
    TreeMap<String,Privileges> privs = new TreeMap<String,Privileges>();

    for(Integer idx : pEditedRows) 
      privs.put(pUserNames.get(idx), pUserPrivileges.get(idx));

    return privs;
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
      Privileges privs = pUserPrivileges.get(idx);
      switch(pSortColumn) {
      case 0:
	value = pUserNames.get(idx);
        break;
        
      case 1:
	value = new Boolean(privs.isDeveloper());
	break;

      case 2:
	value = new Boolean(privs.isAnnotator());
	break;

      case 3:
	value = new Boolean(privs.isQueueAdmin());  
	break;

      case 4:
	value = new Boolean(privs.isQueueManager()); 
	break;

      case 5:
	value = new Boolean(privs.isNodeManager()); 
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
    if(!pPrivilegeDetails.isMasterAdmin()) 
      return false;

    int srow = pRowToIndex[row];

    String uname = pUserNames.get(srow);
    if(uname.equals(PackageInfo.sPipelineUser))
      return false;

    Privileges privs = pUserPrivileges.get(srow);
    switch(col) {
    case 0:
      return false;

    case 1:
      return true; 
      
    case 2:
    case 3:
    case 4:
    case 6:
      return new Boolean(!privs.isMasterAdmin());
      
    case 5:
      return new Boolean(!(privs.isMasterAdmin() || privs.isQueueAdmin()));

    default:
      throw new IllegalArgumentException
        ("There is no such column (" + col + ")!"); 
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
    Privileges privs = pUserPrivileges.get(srow);
    switch(col) {
    case 0:
      return pUserNames.get(srow);

    case 1:
      return new Boolean(privs.isMasterAdmin()); 
      
    case 2:
      return new Boolean(privs.isDeveloper());
      
    case 3:
      return new Boolean(privs.isAnnotator());
      
    case 4:
      return new Boolean(privs.isQueueAdmin()); 
      
    case 5:
      return new Boolean(privs.isQueueManager()); 
      
    case 6:
      return new Boolean(privs.isNodeManager()); 

    default:
      throw new IllegalArgumentException
        ("There is no such column (" + col + ")!"); 
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
    Privileges privs = pUserPrivileges.get(srow);
    switch(col) {
    case 0:
      return false;

    case 1:
      privs.setMasterAdmin((Boolean) value);
      pEditedRows.add(srow);
      break;
      
    case 2:
      privs.setDeveloper((Boolean) value);
      pEditedRows.add(srow);
      break;
      
    case 3:
      privs.setAnnotator((Boolean) value);
      pEditedRows.add(srow);
      break;
      
    case 4:
      privs.setQueueAdmin((Boolean) value);  
      pEditedRows.add(srow);
      break;
      
    case 5:
      privs.setQueueManager((Boolean) value); 
      pEditedRows.add(srow);
      break;
      
    case 6:
      privs.setNodeManager((Boolean) value); 
      pEditedRows.add(srow);
    }
    
    return true;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 477295869489343622L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The privileges for each user.
   */ 
  private ArrayList<Privileges>  pUserPrivileges;

}
