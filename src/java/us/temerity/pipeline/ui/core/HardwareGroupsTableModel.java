// $Id: HardwareGroupsTableModel.java,v 1.5 2009/08/19 23:42:47 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.HardwareGroup;
import us.temerity.pipeline.PrivilegeDetails;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.ui.*;

import java.util.*;

import javax.swing.JLabel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;


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

    pNameRenderer = new JSimpleTableCellRenderer(JLabel.CENTER);

    pHardwareValueRenderer = new JBooleanTableCellRenderer("Green", JLabel.CENTER);
    {
      JBooleanTableCellEditor editor = 
        new JBooleanTableCellEditor(120, JLabel.CENTER);
      editor.setSynthPrefix("Green"); 
      pHardwareValueEditor = editor;
    }
  }
 

  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
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
   */ 
  public void 
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
    
    pNumRows = pHardwareGroups.size();

    pHardwareKeys.clear();
    pHardwareDescriptions.clear();
    if(keys != null) {
      for(Map.Entry<String,String> entry : keys.entrySet()) {
        pHardwareKeys.add(entry.getKey()); 
        pHardwareDescriptions.add(entry.getValue()); 
      }
    }

    pPrivilegeDetails = privileges; 
    
    pEditedIndices.clear();

    sort(); 
    fireTableStructureChanged(); 
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
  /*   S O R T I N G                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Sort the rows by the values in the current sort column and direction.
   */ 
  @SuppressWarnings("unchecked")
  @Override
  public void 
  sort()
  {
    IndexValue cells[] = new IndexValue[pNumRows]; 
    int idx = 0;
    for(HardwareGroup group : pHardwareGroups) {
      Comparable value = null;
      switch(pSortColumn) {
      case 0:
	value = group.getName(); 
	break;

      default:
        {
          String kname = pHardwareKeys.get(pSortColumn-1);
          if(kname != null) 
            value = group.hasKey(kname); 
        }
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
  /*   S O R T A B L E   T A B L E   M O D E L   O V E R R I D E S                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the range of widths (min, preferred, max) of the column. 
   */ 
  @Override
  public Vector3i
  getColumnWidthRange
  (
   int col   
  )
  {
    switch(col) {
    case 0:
      return new Vector3i(140);
      
    default:
      return new Vector3i(120);
    }
  }

  /**
   * Returns the color prefix used to determine the synth style of the header button for 
   * the given column.
   */ 
  @Override
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

  /**
   * Returns the description of the column columnIndex used in tool tips.
   */ 
  @Override
  public String 	
  getColumnDescription
  (
   int col
  ) 
  {
    switch(col) {
    case 0:
      return "The name of the selection group."; 
      
    default:
      return pHardwareDescriptions.get(col-1);
    }
  }
  
  /**
   * Get the renderer for the given column. 
   */ 
  @Override
  public TableCellRenderer
  getRenderer
  (
   int col   
  )
  {
    switch(col) {
    case 0:
      return pNameRenderer;

    default:
      return pHardwareValueRenderer; 
    }
  }

  /**
   * Get the editor for the given column. 
   */ 
  @Override
  public TableCellEditor
  getEditor
  (
   int col   
  )
  {
    switch(col) {
    case 0:
      return null; 

    default:
      return pHardwareValueEditor; 
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   T A B L E   M O D E L   O V E R R I D E S                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the most specific superclass for all the cell values in the column.
   */
  @SuppressWarnings("unchecked")
  @Override
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
  @Override
  public int
  getColumnCount()
  {
    return pHardwareKeys.size() + 1;
  }

  /**
   * Returns the name of the column at columnIndex.
   */ 
  @Override
  public String 	
  getColumnName
  (
   int col
  ) 
  {
    switch(col) {
    case 0:
      return "Group Name"; 

    default:
      return pHardwareKeys.get(col-1);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   T A B L E   M O D E L   O V E R R I D E S                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns true if the cell at rowIndex and columnIndex is editable.
   */ 
  @Override
  public boolean 	
  isCellEditable
  (
   int row, 
   int col
  ) 
  {
    switch(col) {
    case 0:
      return false;

    default:
      return pPrivilegeDetails.isQueueAdmin(); 
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
    HardwareGroup group = pHardwareGroups.get(pRowToIndex[row]);
    switch(col) {
    case 0:
      return group.getName();

    default:
      {
        String kname = pHardwareKeys.get(col-1);
        if(kname != null) 
          return group.hasKey(kname);
      }
    }
        
    return null;
  }

  /**
   * Sets the value in the cell at columnIndex and rowIndex to aValue.
   */ 
  @Override
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
          if (setValueAtHelper(value, srow, col))
            edited = true;

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
    switch(col) {
    case 0:
      return false;

    default:
      {
        String kname = pHardwareKeys.get(col-1);
        if(kname != null) {
          Boolean hasKey = (Boolean) value;
          if(hasKey) 
            group.addKey(kname);
          else 
            group.removeKey(kname);
          
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
   * The cell render for hardware group names.
   */ 
  private TableCellRenderer  pNameRenderer; 

  /**
   * The shared renderer for all hardware value cells.
   */ 
  private TableCellRenderer  pHardwareValueRenderer;
  
  /**
   * The shared renderer for all hardware value cells.
   */ 
  private TableCellEditor  pHardwareValueEditor;

}
