// $Id: SelectionGroupsTableModel.java,v 1.7 2009/08/19 23:42:47 jim Exp $

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
    pSelectionBiasRenderer = new JSimpleTableCellRenderer("Green", JLabel.CENTER);
    {
      JIntegerTableCellEditor bias = new JIntegerTableCellEditor(120, JLabel.CENTER);
      bias.setName("GreenEditableTextField");
      pSelectionBiasEditor = bias;
    }

    pNameRenderer = new JSimpleTableCellRenderer(JLabel.CENTER);

    pFavorRenderer = new JSimpleTableCellRenderer(JLabel.CENTER); 
    pFavorRenderer.setName("BlueTableCellRenderer");

    pFavorEditor = new JCollectionTableCellEditor(JobGroupFavorMethod.titles(), 140);
    pFavorEditor.setSynthPrefix("Blue");
  }
 

  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
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
   */ 
  public void 
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
    
    pNumRows = pSelectionGroups.size();

    pSelectionKeys.clear();
    pSelectionDescriptions.clear();
    if(keys != null) {
      for(Map.Entry<String,String> entry : keys.entrySet()) {
        pSelectionKeys.add(entry.getKey()); 
        pSelectionDescriptions.add(entry.getValue()); 
      }
    }

    pPrivilegeDetails = privileges;     

    pEditedIndices.clear();

    sort();
    fireTableStructureChanged(); 
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
    for(SelectionGroup group : pSelectionGroups) {
      Comparable value = null;
      switch(pSortColumn) {
      case 0:
	value = group.getName(); 
	break;

      case 1:
	value = group.getFavorMethod().toString();
	break;

      default:
	{
	  String kname = pSelectionKeys.get(pSortColumn-2);
	  if(kname != null) 
	    value = group.getBias(kname);
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
  public Vector3i
  getColumnWidthRange
  (
   int col   
  )
  {
    switch(col) {
    case 0:
    case 1:
      return new Vector3i(140);
      
    default:
      return new Vector3i(120);
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

    case 1:
      return "Blue"; 

    default:
      return "Green"; 
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
      return "The name of the selection group."; 
      
    case 1:
      return "The job group favor method."; 

    default:
      return pSelectionDescriptions.get(col-2);
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
      return pNameRenderer;

    case 1: 
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
      return null;

    case 1:
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
    case 1:
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
    return pSelectionKeys.size() + 2;
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
      return "Group Name"; 

    case 1: 
      return "Favor Groups";
      
    default:
      return pSelectionKeys.get(col-2);
    }
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
    SelectionGroup group = pSelectionGroups.get(pRowToIndex[row]);
    switch(col) {
    case 0:
      return group.getName();

    case 1:
      return group.getFavorMethod().toTitle();
      
    default:
      {
	String kname = pSelectionKeys.get(col-2);
	if(kname != null) 
	  return group.getBias(kname);
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
    SelectionGroup group = pSelectionGroups.get(srow);

    switch(col) {
    case 0:
      return false;

    case 1:
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
	String kname = pSelectionKeys.get(col-2);
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
   * The cell render for selection group names.
   */ 
  private TableCellRenderer  pNameRenderer; 


  /**
   * The renderer for the "Favor" cells.
   */ 
  private JSimpleTableCellRenderer  pFavorRenderer; 

  /**
   * The renderer for the "Favor" cells.
   */ 
  private JCollectionTableCellEditor  pFavorEditor; 
}
