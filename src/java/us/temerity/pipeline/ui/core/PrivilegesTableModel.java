// $Id: PrivilegesTableModel.java,v 1.3 2007/06/15 00:27:31 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.text.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   P R I V I L E G E S   G R O U P S   T A B L E   M O D E L                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A {@link SortableTableModel SortableTableModel} which contains both WorkGroup and 
 * Privilege information for each Pipeline user.
 */ 
public
class PrivilegesTableModel
  extends AbstractSortableTableModel
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
    super();
    
    /* initialize the fields */ 
    {
      pParent = parent;

      pPrivilegeDetails = new PrivilegeDetails();

      pUserNames        = new ArrayList<String>();
      pUserPrivileges   = new ArrayList<Privileges>();
      pGroupNames       = new ArrayList<String>();
      pGroupMemberships = new ArrayList<ArrayList<Boolean>>();
      
      pEditedPrivilegeIndices = new TreeSet<Integer>();
      pEditedMemberIndices    = new TreeSet<Integer>();
    }

    /* initialize the columns */ 
    { 
      pNumColumns = 6;

      {
	String names[] = { 
	  "Master Admin", "Developer", "Annotator", 
          "Queue Admin", "Queue Manager", "Node Manager"
	};
	pColumnNames = names;
      }

      {
	String desc[] = {
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

    pPrivRenderer = new JPrivilegesTableCellRenderer();

    pPrivEditor = new JBooleanTableCellEditor(120, JLabel.CENTER, false); 
    pPrivEditor.setSynthPrefix("Blue");

    pMemberRenderer = new JWorkGroupMemberTableCellRenderer();
    pMemberEditor   = new JWorkGroupMemberTableCellEditor(120);
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
    if((pSortColumn-5) >= pGroupNames.size()) 
      pSortColumn = 0;

    ArrayList<Comparable> values = new ArrayList<Comparable>();
    ArrayList<Integer> indices = new ArrayList<Integer>();
    int idx = 0;
    for(Privileges privs : pUserPrivileges) {
      Comparable value = null;
      switch(pSortColumn) {
      case 0:
	value = new Boolean(privs.isMasterAdmin()); 
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
	break;
      
      default:
	Boolean tf = pGroupMemberships.get(idx).get(pSortColumn-6);
	if(tf == null) 
	  value = "";
	else 
	  value = tf.toString();	  
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
    
    pParent.sortNamesTable(pRowToIndex);
  }

  /**
   * Copy the row sort order from another table model with the same number of rows.
   */ 
  public void
  externalSort
  (
   int[] rowToIndex
  ) 
  {
    pRowToIndex = rowToIndex.clone();
    fireTableDataChanged();     
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S O R T A B L E   T A B L E   M O D E L   O V E R R I D E S                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the width of the given column.
   */ 
  public int
  getColumnWidth
  (
   int col   
  )
  {
    return 120;
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
    if(col < 6) 
      return "Blue";
    return "";
  }

  /**
   * Returns the description of the column columnIndex used in tool tips.
   */ 
  public String 	
  getColumnDescription
  (
   int col
  ) 
  {
    if(col < 6)
      return super.getColumnDescription(col);
    return "Work group membership.";
  }
  
  /**
   * Get the renderer for the given column. 
   */ 
  public TableCellRenderer
  getRenderer
  (
   int col   
  )
  {
    if(col < 6)
      return pPrivRenderer; 
    return pMemberRenderer;
  }

  /**
   * Get the editor for the given column. 
   */ 
  public TableCellEditor
  getEditor
  (
   int col   
  )
  {
    if(col < 6) 
      return pPrivEditor; 
    return pMemberEditor;
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
    return Boolean.class;
  }
  
  /**
   * Returns the number of columns in the model.
   */ 
  public int
  getColumnCount()
  {
    return (pGroupNames.size() + 6);
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
    if(col < 6) 
      return super.getColumnName(col);
    return pGroupNames.get(col-6);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
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
   * @param privileges
   *   The privileges granted to the current user with respect to all other users.
   * 
   * @return 
   *   Modify whether a column should be visible indexed by column name. 
   */ 
  public TreeMap<String,Boolean>
  setPrivileges
  (
   WorkGroups groups, 
   TreeMap<String,Privileges> privileges,
   PrivilegeDetails details
  ) 
  {
    pUserNames.clear();
    if(groups != null) 
      pUserNames.addAll(groups.getUsers());

    pUserPrivileges.clear();
    for(String uname : pUserNames) {
      Privileges privs = null; 
      if(privileges != null) 
	privs = privileges.get(uname);
      if(privs == null) 
	privs = new Privileges();
      pUserPrivileges.add(privs);
    }

    TreeMap<String,Boolean> modified = new TreeMap<String,Boolean>();
    {
      TreeSet<String> obsolete = new TreeSet<String>(pGroupNames);
      TreeSet<String> newborn  = new TreeSet<String>(groups.getGroups());

      pGroupNames.clear();
      if(groups != null) {
	pGroupNames.addAll(newborn);
	newborn.removeAll(obsolete);
	obsolete.removeAll(pGroupNames);
      }
      
      for(String gname : obsolete)
	modified.put(gname, false);
    
      for(String gname : newborn)
	modified.put(gname, true);
    }
    
    int gsize = pGroupNames.size();
    pGroupMemberships.clear();
    for(String uname : pUserNames) {
      ArrayList<Boolean> members = new ArrayList<Boolean>(gsize);
      for(String gname : pGroupNames) 
	members.add(groups.isMemberOrManager(uname, gname));

      pGroupMemberships.add(members);
    }

    if(details != null) 
      pPrivilegeDetails = details; 
    
    pEditedPrivilegeIndices.clear();
    pEditedMemberIndices.clear();

    sort();

    return modified;
  }

  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the modified privileges indexed by user name.
   */ 
  public TreeMap<String,Privileges>
  getModifiedPrivileges() 
  {
    TreeMap<String,Privileges> privs = new TreeMap<String,Privileges>();

    for(Integer idx : pEditedPrivilegeIndices) 
      privs.put(pUserNames.get(idx), pUserPrivileges.get(idx));

    return privs;
  }

  /**
   * Get the modified work group memberships.
   */ 
  public DoubleMap<String,String,Boolean>  
  getModifiedWorkGroupMemberships() 
  {
    DoubleMap<String,String,Boolean> members = new DoubleMap<String,String,Boolean>();

    for(Integer idx : pEditedMemberIndices) {
      ArrayList<Boolean> flags = pGroupMemberships.get(idx);
      int wk;
      for(wk=0; wk<flags.size(); wk++) 
	members.put(pUserNames.get(idx), pGroupNames.get(wk), flags.get(wk));
    }

    return members;
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
    return pUserNames.size();
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

    Privileges privs = pUserPrivileges.get(srow);
    switch(col) {
    case 0:
      return true; 
      
    case 1:
    case 2:
    case 3:
    case 5:
      return new Boolean(!privs.isMasterAdmin());
      
    case 4:
      return new Boolean(!(privs.isMasterAdmin() || privs.isQueueAdmin()));
    }

    return true; 
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
      return new Boolean(privs.isMasterAdmin()); 
      
    case 1:
      return new Boolean(privs.isDeveloper());
      
    case 2:
      return new Boolean(privs.isAnnotator());
      
    case 3:
      return new Boolean(privs.isQueueAdmin()); 
      
    case 4:
      return new Boolean(privs.isQueueManager()); 
      
    case 5:
      return new Boolean(privs.isNodeManager()); 
    }

    return pGroupMemberships.get(srow).get(col-6);
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
    if(!pPrivilegeDetails.isMasterAdmin()) 
      return;

    int vrow = pRowToIndex[row];
    boolean edited = setValueAtHelper(value, vrow, col);
    
    int[] selected = pTable.getSelectedRows(); 
    int wk;
    for(wk=0; wk<selected.length; wk++) {
      int srow = pRowToIndex[selected[wk]];
      if(srow != vrow)
	  setValueAtHelper(value, srow, col);
    }
      
    if(edited) {
      fireTableDataChanged();
      pParent.doEdited();
    }
  }

  public boolean 
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
      privs.setMasterAdmin((Boolean) value);
      pEditedPrivilegeIndices.add(srow);
      break;
      
    case 1:
      privs.setDeveloper((Boolean) value);
      pEditedPrivilegeIndices.add(srow);
      break;
      
    case 2:
      privs.setAnnotator((Boolean) value);
      pEditedPrivilegeIndices.add(srow);
      break;
      
    case 3:
      privs.setQueueAdmin((Boolean) value);  
      pEditedPrivilegeIndices.add(srow);
      break;
      
    case 4:
      privs.setQueueManager((Boolean) value); 
      pEditedPrivilegeIndices.add(srow);
      break;
      
    case 5:
      privs.setNodeManager((Boolean) value); 
      pEditedPrivilegeIndices.add(srow);
      break;
      
    default:
      pGroupMemberships.get(srow).set(col-6, (Boolean) value);
      pEditedMemberIndices.add(srow);
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
   * The parent dialog.
   */ 
  private JManagePrivilegesDialog pParent;

  /**
   * The privileges granted to the current user with respect to all other users.
   */ 
  private PrivilegeDetails  pPrivilegeDetails; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The names of the users.
   */ 
  private ArrayList<String>  pUserNames;

  /**
   * The privileges for each user.
   */ 
  private ArrayList<Privileges>  pUserPrivileges;


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


  /*----------------------------------------------------------------------------------------*/

  /**
   * The indices of users which have had their privileges edited.
   */ 
  private TreeSet<Integer>  pEditedPrivilegeIndices; 

  /**
   * The indices of users which have had their work group memberships edited. 
   */ 
  private TreeSet<Integer>  pEditedMemberIndices; 


  /*----------------------------------------------------------------------------------------*/

  /** 
   * The cell render for work group memberships.
   */ 
  private JWorkGroupMemberTableCellRenderer  pMemberRenderer; 

  /**
   * The cell editor for work group memberships.
   */ 
  private JWorkGroupMemberTableCellEditor  pMemberEditor;


  /**
   * The cell render for administrative privileges.
   */ 
  private JBooleanTableCellRenderer  pPrivRenderer;

  /**
   * The cell editor for administrative privileges.
   */ 
  private JBooleanTableCellEditor  pPrivEditor;

}
