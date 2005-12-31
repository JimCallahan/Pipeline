// $Id: SelectionGroupsTableModel.java,v 1.1 2005/12/31 20:40:44 jim Exp $

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

      pSelectionGroups       = new ArrayList<SelectionGroup>();
      pSelectionKeys         = new ArrayList<String>();
      pSelectionDescriptions = new ArrayList<String>();
      
      pEditedIndices = new TreeSet<Integer>();
    }

    /* all columns are dynamic, just initialize the shared renderer/editor */ 
    pSelectionBiasRenderer = new JSimpleTableCellRenderer(JLabel.CENTER);
    pSelectionBiasEditor   = new JIntegerTableCellEditor(120, JLabel.CENTER);
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
      String kname = pSelectionKeys.get(pSortColumn);
      if(kname != null) 
	value = group.getBias(kname);
      
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
    return pSelectionDescriptions.get(col);
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
    return pSelectionBiasRenderer; 
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
    return pSelectionBiasEditor; 
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
    return Integer.class;
  }
  
  /**
   * Returns the number of columns in the model.
   */ 
  public int
  getColumnCount()
  {
    return pSelectionKeys.size();
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
    return pSelectionKeys.get(col);
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
   * @param isPrivileged
   *   Whether the current user is has privileged status.
   * 
   * @return 
   *   The names of the selection keys which are no longer supported.
   */ 
  public TreeSet<String> 
  setSelectionGroups
  (
   TreeMap<String,SelectionGroup> groups, 
   TreeMap<String,String> keys, 
   boolean isPrivileged
  ) 
  {
    pSelectionGroups.clear();
    if(groups != null)
      pSelectionGroups.addAll(groups.values());

    TreeSet<String> obsolete = new TreeSet<String>(pSelectionKeys);
    pSelectionKeys.clear();
    if(keys != null) {
      pSelectionKeys.addAll(keys.keySet());
      obsolete.removeAll(keys.keySet());
    }

    pSelectionDescriptions.clear();
    if(keys != null) 
      pSelectionDescriptions.addAll(keys.values());

    pIsPrivileged = isPrivileged;
    
    pEditedIndices.clear();

    sort();

    return obsolete;
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
    return pIsPrivileged; 
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
    String kname = pSelectionKeys.get(col);
    if(kname != null) 
      return group.getBias(kname);
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
    
    if(pIsPrivileged) {
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
    String kname = pSelectionKeys.get(col);
    if(kname != null) {
      Integer bias = (Integer) value;
      if(bias == null) 
	group.removeBias(kname);
      else 
	group.addBias(kname, bias);

      pEditedIndices.add(srow);
      return true;
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
   * Does the current user have privileged status?
   */ 
  private boolean  pIsPrivileged;

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

  /**
   * The shared renderer for all selection bias cells.
   */ 
  private TableCellRenderer  pSelectionBiasRenderer;
  
  /**
   * The shared renderer for all selection bias cells.
   */ 
  private TableCellEditor  pSelectionBiasEditor;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The indices of groups which have had their selection biases edited.
   */ 
  private TreeSet<Integer>  pEditedIndices; 

}
