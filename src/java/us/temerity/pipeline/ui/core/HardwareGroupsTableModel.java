// $Id: HardwareGroupsTableModel.java,v 1.1 2007/11/30 20:06:25 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.util.*;

import javax.swing.JLabel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import us.temerity.pipeline.HardwareGroup;
import us.temerity.pipeline.PrivilegeDetails;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   H A R D W A R E   G R O U P S   T A B L E   M O D E L                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A {@link SortableTableModel SortableTableModel} which contains a set of 
 * {@link HardwareGroup} instances.
 */ 
public
class HardwareGroupsTableModel
  extends AbstractSortableTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   */
  public 
  HardwareGroupsTableModel
  (
   JManageHardwareKeysDialog parent
  ) 
  {
    super();
    
    /* initialize the fields */ 
    {
      pParent = parent;

      pPrivilegeDetails = new PrivilegeDetails();    

      pHardwareGroups       = new ArrayList<HardwareGroup>();
      pHardwareKeys         = new ArrayList<String>();
      pHardwareDescriptions = new ArrayList<String>();
      
      pEditedIndices = new TreeSet<Integer>();
    }

    /* all columns are dynamic, just initialize the shared renderers/editors */ 
    pHardwareValueRenderer = new JBooleanTableCellRenderer(JLabel.CENTER);
    pHardwareValueEditor   = new JBooleanTableCellEditor(120, JLabel.CENTER);
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
    for(HardwareGroup group : pHardwareGroups) {
      Comparable value = null;
      String kname = pHardwareKeys.get(pSortColumn);
      if(kname != null) 
	value = group.hasKey(kname);

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
    return pHardwareDescriptions.get(col);
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
    return pHardwareValueRenderer; 
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
    return pHardwareValueEditor; 
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
    return pHardwareKeys.size();
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
    return pHardwareKeys.get(col);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set table data.
   *
   * @param groups
   *   Current hardware groups indexed by group name.
   * 
   * @param keys
   *   The valid hardware key descriptions indexed by key name.
   * 
   * @param privileges
   *   The details of the administrative privileges granted to the current user. 
   * 
   * @return 
   *   Modify whether a column should be visible indexed by column name. 
   */ 
  public TreeMap<String,Boolean>
  setHardwareGroups
  (
   TreeMap<String,HardwareGroup> groups, 
   TreeMap<String,String> keys, 
   PrivilegeDetails privileges
  ) 
  {
    pHardwareGroups.clear();
    if(groups != null)
      pHardwareGroups.addAll(groups.values());
    
    TreeMap<String,Boolean> modified = new TreeMap<String,Boolean>();
    {
      TreeSet<String> obsolete = new TreeSet<String>(pHardwareKeys);
      TreeSet<String> newborn  = new TreeSet<String>(keys.keySet());

      pHardwareKeys.clear();
      if(keys != null) {
	pHardwareKeys.addAll(newborn);
	newborn.removeAll(obsolete);
	obsolete.removeAll(pHardwareKeys);
      }
      
      for(String gname : obsolete)
	modified.put(gname, false);
    
      for(String gname : newborn)
	modified.put(gname, true);
    }

    pHardwareDescriptions.clear();
    if(keys != null) 
      pHardwareDescriptions.addAll(keys.values());

    pPrivilegeDetails = privileges; 
    
    pEditedIndices.clear();

    sort();

    return modified;
  }

  
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Get the name of the hardware group on the given row.
   */
  public String
  getGroupName
  (
   int row
  ) 
  { 
    HardwareGroup group = pHardwareGroups.get(pRowToIndex[row]);
    if(group != null) 
      return group.getName();
    return null;
  }

  /** 
   * Get the hardware group on the given row.
   */
  public HardwareGroup
  getGroup
  (
   int row
  ) 
  { 
    return pHardwareGroups.get(pRowToIndex[row]);
  }

  /** 
   * Get the names of the selection groups in the current sorted order.
   */
  public ArrayList<String>
  getGroupNames() 
  { 
    ArrayList<String> names = new ArrayList<String>();

    int row;
    for(row=0; row<pHardwareGroups.size(); row++) 
      names.add(pHardwareGroups.get(pRowToIndex[row]).getName());

    return names;
  }
  
  /**
   * Get the modified hardware groups. 
   */ 
  public ArrayList<HardwareGroup> 
  getModifiedGroups() 
  {
    ArrayList<HardwareGroup> edited = new ArrayList<HardwareGroup>();
    for(Integer idx : pEditedIndices) {
      HardwareGroup group = pHardwareGroups.get(idx);
      if(group != null) 
	edited.add(group); 
    }
    
    if(!edited.isEmpty()) 
      return edited;

    return null;
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the current hardware key value columns.
   */ 
  public TreeSet<String> 
  getHardwareKeys() 
  {
    TreeSet<String> names = new TreeSet<String>(pHardwareKeys);
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
    return pHardwareGroups.size();
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
    HardwareGroup group = pHardwareGroups.get(pRowToIndex[row]);

    String kname = pHardwareKeys.get(col);
    if(kname != null) 
      return group.hasKey(kname);

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
    HardwareGroup group = pHardwareGroups.get(srow);
    String kname = pHardwareKeys.get(col);
    if(kname != null) {
      Boolean hasKey = (Boolean) value;
      if(hasKey) 
	group.addKey(kname);
      else 
	group.removeKey(kname);

      pEditedIndices.add(srow);
      return true;
    }

    return false;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8629959079674635228L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent dialog.
   */ 
  private JManageHardwareKeysDialog pParent;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The details of the administrative privileges granted to the current user. 
   */ 
  private PrivilegeDetails  pPrivilegeDetails; 

  /**
   * The underlying set of hardware groups.
   */ 
  private ArrayList<HardwareGroup> pHardwareGroups;

  /**
   * The names of the valid hardware keys.
   */ 
  private ArrayList<String>  pHardwareKeys; 
  
  /**
   * The descriptions of the valid hardware keys.
   */ 
  private ArrayList<String>  pHardwareDescriptions; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The indices of groups which have had their values edited.
   */ 
  private TreeSet<Integer>  pEditedIndices; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The shared renderer for all hardware value cells.
   */ 
  private TableCellRenderer  pHardwareValueRenderer;
  
  /**
   * The shared renderer for all hardware value cells.
   */ 
  private TableCellEditor  pHardwareValueEditor;

}
