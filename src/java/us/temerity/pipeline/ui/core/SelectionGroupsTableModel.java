// $Id: SelectionGroupsTableModel.java,v 1.4 2006/12/14 02:39:05 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.text.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   S E L E C T I O N   G R O U P S   T A B L E   M O D E L                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A {@link SortableTableModel SortableTableModel} which contains a set of 
 * {@link SelectionGroup SelectionGroup} instances.
 */ 
public
class SelectionGroupsTableModel
  extends AbstractSortableTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   */
  public 
  SelectionGroupsTableModel
  (
   JManageSelectionKeysDialog parent
  ) 
  {
    super();
    
    /* initialize the fields */ 
    {
      pParent = parent;

      pPrivilegeDetails = new PrivilegeDetails();    

      pSelectionGroups       = new ArrayList<SelectionGroup>();
      pSelectionKeys         = new ArrayList<String>();
      pSelectionDescriptions = new ArrayList<String>();
      
      pEditedIndices = new TreeSet<Integer>();
    }

    /* all columns are dynamic, just initialize the shared renderers/editors */ 
    pSelectionBiasRenderer = new JSimpleTableCellRenderer(JLabel.CENTER);
    pSelectionBiasEditor   = new JIntegerTableCellEditor(120, JLabel.CENTER);

    pFavorRenderer = new JSimpleTableCellRenderer(JLabel.CENTER); 
    pFavorRenderer.setName("BlueTableCellRenderer");

    pFavorEditor = new JCollectionTableCellEditor(JobGroupFavorMethod.titles(), 140);
    pFavorEditor.setSynthPrefix("Blue");
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
    int idx = 0;
    for(SelectionGroup group : pSelectionGroups) {
      Comparable value = null;
      switch(pSortColumn) {
      case 0:
	value = group.getFavorMethod().toString();
	break;

      default:
	{
	  String kname = pSelectionKeys.get(pSortColumn-1);
	  if(kname != null) 
	    value = group.getBias(kname);
	}
      }
      
      int wk;
      for(wk=0; wk<values.size(); wk++) {
	Comparable v = values.get(wk);
	if((v == null) || ((value != null) && (value.compareTo(v) > 0)))
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
    switch(col) {
    case 0:
      return 140;
      
    default:
      return 120;
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
      return "Blue"; 

    default:
      return "";
    }
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
    switch(col) {
    case 0:
      return "The job group favor method."; 

    default:
      return pSelectionDescriptions.get(col-1);
    }
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
    switch(col) {
    case 0:
      return pFavorRenderer; 
      
    default:
      return pSelectionBiasRenderer; 
    }
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
    switch(col) {
    case 0:
      return pFavorEditor; 
      
    default:
      return pSelectionBiasEditor; 
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
      return Integer.class;
    }
  }
  
  /**
   * Returns the number of columns in the model.
   */ 
  public int
  getColumnCount()
  {
    return pSelectionKeys.size() + 1;
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
      return "Favor Groups";
      
    default:
      return pSelectionKeys.get(col-1);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set table data.
   *
   * @param groups
   *   Current selection groups indexed by group name.
   * 
   * @param keys
   *   The valid selection key descriptions indexed by key name.
   * 
   * @param privileges
   *   The details of the administrative privileges granted to the current user. 
   * 
   * @return 
   *   Modify whether a column should be visible indexed by column name. 
   */ 
  public TreeMap<String,Boolean>
  setSelectionGroups
  (
   TreeMap<String,SelectionGroup> groups, 
   TreeMap<String,String> keys, 
   PrivilegeDetails privileges
  ) 
  {
    pSelectionGroups.clear();
    if(groups != null)
      pSelectionGroups.addAll(groups.values());
    
    TreeMap<String,Boolean> modified = new TreeMap<String,Boolean>();
    {
      TreeSet<String> obsolete = new TreeSet<String>(pSelectionKeys);
      TreeSet<String> newborn  = new TreeSet<String>(keys.keySet());

      pSelectionKeys.clear();
      if(keys != null) {
	pSelectionKeys.addAll(newborn);
	newborn.removeAll(obsolete);
	obsolete.removeAll(pSelectionKeys);
      }
      
      for(String gname : obsolete)
	modified.put(gname, false);
    
      for(String gname : newborn)
	modified.put(gname, true);
    }

    pSelectionDescriptions.clear();
    if(keys != null) 
      pSelectionDescriptions.addAll(keys.values());

    pPrivilegeDetails = privileges; 
    
    pEditedIndices.clear();

    sort();

    return modified;
  }

  
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Get the name of the selection group on the given row.
   */
  public String
  getGroupName
  (
   int row
  ) 
  { 
    SelectionGroup group = pSelectionGroups.get(pRowToIndex[row]);
    if(group != null) 
      return group.getName();
    return null;
  }

  /** 
   * Get the selection group on the given row.
   */
  public SelectionGroup
  getGroup
  (
   int row
  ) 
  { 
    return pSelectionGroups.get(pRowToIndex[row]);
  }

  /** 
   * Get the names of the selection groups in the current sorted order.
   */
  public ArrayList<String>
  getGroupNames() 
  { 
    ArrayList<String> names = new ArrayList<String>();

    int row;
    for(row=0; row<pSelectionGroups.size(); row++) 
      names.add(pSelectionGroups.get(pRowToIndex[row]).getName());

    return names;
  }
  
  /**
   * Get the modified selection groups. 
   */ 
  public ArrayList<SelectionGroup> 
  getModifiedGroups() 
  {
    ArrayList<SelectionGroup> edited = new ArrayList<SelectionGroup>();
    for(Integer idx : pEditedIndices) {
      SelectionGroup group = pSelectionGroups.get(idx);
      if(group != null) 
	edited.add(group); 
    }
    
    if(!edited.isEmpty()) 
      return edited;

    return null;
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the current selection key bias columns.
   */ 
  public TreeSet<String> 
  getSelectionKeys() 
  {
    TreeSet<String> names = new TreeSet<String>(pSelectionKeys);
    return names;
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
    return pSelectionGroups.size();
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
    return pPrivilegeDetails.isQueueAdmin(); 
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
    SelectionGroup group = pSelectionGroups.get(pRowToIndex[row]);

    switch(col) {
    case 0:
      return group.getFavorMethod().toTitle();
      
    default:
      {
	String kname = pSelectionKeys.get(col-1);
	if(kname != null) 
	  return group.getBias(kname);
      }
    }

    return null;
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
    boolean edited = setValueAtHelper(value, vrow, col);
    
    if(pPrivilegeDetails.isQueueAdmin()) {
      int[] selected = pTable.getSelectedRows(); 
      int wk;
      for(wk=0; wk<selected.length; wk++) {
	int srow = pRowToIndex[selected[wk]];
	if(srow != vrow)
	  setValueAtHelper(value, srow, col);
      }
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
    SelectionGroup group = pSelectionGroups.get(srow);

    switch(col) {
    case 0:
      {
	String favor = (String) value; 
	for(JobGroupFavorMethod method : JobGroupFavorMethod.all()) {
	  if(method.toTitle().equals(value)) {
	    group.setFavorMethod(method); 
	    pEditedIndices.add(srow);
	    return true;
	  }
	}
      }
      break;

    default:
      {
	String kname = pSelectionKeys.get(col-1);
	if(kname != null) {
	  Integer bias = (Integer) value;
	  if(bias == null) 
	    group.removeBias(kname);
	  else 
	    group.addBias(kname, bias);
	  
	  pEditedIndices.add(srow);
	  return true;
	}
      }
    }

    return false;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3123897997012488041L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent dialog.
   */ 
  private JManageSelectionKeysDialog pParent;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The details of the administrative privileges granted to the current user. 
   */ 
  private PrivilegeDetails  pPrivilegeDetails; 

  /**
   * The underlying set of selection groups.
   */ 
  private ArrayList<SelectionGroup> pSelectionGroups;

  /**
   * The names of the valid selection keys.
   */ 
  private ArrayList<String>  pSelectionKeys; 
  
  /**
   * The descriptions of the valid selection keys.
   */ 
  private ArrayList<String>  pSelectionDescriptions; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The indices of groups which have had their selection biases edited.
   */ 
  private TreeSet<Integer>  pEditedIndices; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The shared renderer for all selection bias cells.
   */ 
  private TableCellRenderer  pSelectionBiasRenderer;
  
  /**
   * The shared renderer for all selection bias cells.
   */ 
  private TableCellEditor  pSelectionBiasEditor;


  /**
   * The renderer for the "Favor" cells.
   */ 
  private JSimpleTableCellRenderer  pFavorRenderer; 

  /**
   * The renderer for the "Favor" cells.
   */ 
  private JCollectionTableCellEditor  pFavorEditor; 
}
