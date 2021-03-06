// $Id: BalanceGroupTableModel.java,v 1.4 2009/12/16 04:13:33 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   B A L A N C E   G R O U P   T A B L E   M O D E L                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Table model for user and group settings in a balance group. 
 */
public 
class BalanceGroupTableModel
  extends AbstractSortableTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   * 
   * @param tableType
   *   What sort of data is being put in this table.
   * 
   * @param parent
   *   Parent dialog.
   */
  public 
  BalanceGroupTableModel
  (
    TableType tableType,
    JManageBalanceGroupsDialog parent
  ) 
  {
    super();
    
    pTableType = tableType;
    
    /* initialize the fields */ 
    {
      pParent = parent;

      pPrivilegeDetails = new PrivilegeDetails();
     
      pNames = new ArrayList<String>();
      pBiases = new ArrayList<Integer>();
      pMaxShares = new ArrayList<Double>();
      pEditedIndices = new TreeSet<Integer>();
    }
    
    /* all columns are dynamic, just initialize the shared renderers/editors */
    pDataRenderer = new JSimpleTableCellRenderer("Green", JLabel.CENTER);
    pPercentRenderer = new JPercentTableCellRenderer("Green", JLabel.CENTER, 2);
    {
      JIntegerTableCellEditor editor = new JIntegerTableCellEditor(sDataWidth, JLabel.CENTER);
      editor.setName("GreenEditableTextField");
      pIntEditor = editor;
    }
    
    {
      JPercentTableCellEditor editor = new JPercentTableCellEditor(sDataWidth, JLabel.CENTER);
      editor.setName("GreenEditableTextField");
      pPercentEditor = editor;
    }
    
    pNameRenderer = new JSimpleTableCellRenderer(JLabel.CENTER);
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Set the table's information from the balance group info.
   * 
   * @param biases
   *   The bias values indexed by name.
   *   
   * @param maxShares
   *   The max shares indexed by name.
   *   
   * @param privileges
   *   The details of the administrative privileges granted to the current user.
   */
  public void
  setBalanceGroupData
  (
    Map<String, Integer> biases,
    Map<String, Double> maxShares,
    PrivilegeDetails privileges
  )
  {
    TreeSet<String> names = new TreeSet<String>();
    names.addAll(biases.keySet());
    names.addAll(maxShares.keySet());
    
    pNames = new ArrayList<String>(names);
    pBiases = new ArrayList<Integer>();
    pMaxShares = new ArrayList<Double>();
    
    for (String name : pNames) {
      pBiases.add(biases.get(name));
      pMaxShares.add(maxShares.get(name));
    }
    
    pPrivilegeDetails = privileges;
    
    pNumRows = pNames.size();
    
    pEditedIndices.clear();

    sort();
  }

  /**
   * Get all the names represented in this table.
   */
  public List<String>
  getNames()
  {
    return Collections.unmodifiableList(pNames);
  }
  
  /**
   * Get the current bias values from the table.
   */
  public TreeMap<String, Integer>
  getBiases()
  {
    TreeMap<String, Integer> toReturn = new TreeMap<String, Integer>();
    
    int size = pNames.size();
    for (int i = 0; i < size ; i++ ) {
      Integer share = pBiases.get(i);
      if (share != null)
        toReturn.put(pNames.get(i), share);
    }
    
    return toReturn;
  }

  /**
   * Get the current max share values from the table.
   */
  public TreeMap<String, Double>
  getMaxShares()
  {
    TreeMap<String, Double> toReturn = new TreeMap<String, Double>();
    
    int size = pNames.size();
    for (int i = 0; i < size ; i++ ) {
      Double max = pMaxShares.get(i);
      if (max!= null)
        toReturn.put(pNames.get(i), max);
    }
    
    return toReturn;
  }
  
  /**
   * Add new entries to the table model
   * 
   * @param names
   *   The list of names to add.
   *   
   * @param shareValue
   *   The default share value.
   *   
   * @param maxValue
   *   
   */
  public void
  addEntries
  (
    Set<String> names,
    Integer shareValue,
    Double maxValue
  )
  {
    boolean modified = false;
    for (String name : names) {
      if (!pNames.contains(name) ) {
        modified = true;
        pNames.add(name);
        pMaxShares.add(maxValue);
        pBiases.add(shareValue);
      }
    }
    if (modified) {
      pNumRows = pNames.size();
      pParent.doEdited();
      sort();
    }
  }
  
  /**
   * Remove entries from the table model.
   * 
   * @param rows
   *   The number of the rows to remove.
   */
  public void
  removeEntries
  (
    int[] rows
  )
  {
    TreeSet<Integer> indexes = new TreeSet<Integer>();
    for (int row : rows) {
      indexes.add(pRowToIndex[row]);
    }
    
    boolean modified = false;
    int removed = 0;
    for (int idx : indexes) {
      modified = true;
      int actualIndex = idx - removed;
      pNames.remove(actualIndex);
      pMaxShares.remove(actualIndex);
      pBiases.remove(actualIndex);
      removed++;
    }
    if (modified) {
      pNumRows = pNames.size();
      pParent.doEdited();
      sort();
    }
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S O R T I N G                                                                        */
  /*----------------------------------------------------------------------------------------*/

  @SuppressWarnings("unchecked")
  @Override
  public void 
  sort()
  {
    IndexValue cells[] = new IndexValue[pNumRows]; 
    
    int size = pNames.size(); 
    for (int idx = 0; idx < size; idx++) {
      Comparable value = null;
      switch(pSortColumn) {
      case 0:
        value = pNames.get(idx);
        break;
      case 1:
        value = pBiases.get(idx);
        break;
      case 2:
        value = pMaxShares.get(idx);
        break;
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
      return new Vector3i(140, 140, Integer.MAX_VALUE);
      
    default:
      return new Vector3i(sDataWidth);
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
      switch (pTableType) {
      case GROUP:
        return "The name of the group.";
      default:
        return "The name of the user.";
      }
      
      
    case 1:
      switch (pTableType) {
      case GROUP:
        return "The group bias.";
      default:
        return "The user bias.";
      }
      
    case 2:
      switch (pTableType) {
      case GROUP:
        return "The max share of the balance group the group can have";
      default:
        return "The max share of the balance group the user can have";
      }
      
      
    default:
      throw new IllegalStateException("Illegal index");
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

    case 1:
      return pDataRenderer;
      
    default:
      return pPercentRenderer; 
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
      
    case 1:
      return pIntEditor;

    default:
      return pPercentEditor; 
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
      
    case 1:
      return Integer.class;
      
    default:
      return Double.class;
    }
  }
  
  /**
   * Returns the number of columns in the model.
   */ 
  @Override
  public int
  getColumnCount()
  {
    return 3;
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
      switch (pTableType) {
      case GROUP:
        return "Group";
      default:
        return "User";
      }
       
    case 1:
      return "Bias";
    case 2:
      return "Max";
    default:
      throw new IllegalStateException("Illegal index.");
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
    int vrow = pRowToIndex[row];
    switch(col) {
    case 0:
      return pNames.get(vrow);
    
    case 1:
      return pBiases.get(vrow);
      
    case 2:
      return pMaxShares.get(vrow);
      
    default:
      throw new IllegalStateException("Illegal index.");
    }
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
    if(pPrivilegeDetails.isQueueAdmin()) {
      int vrow = pRowToIndex[row];
      boolean edited = setValueAtHelper(value, vrow, col);


      int[] selected = pTable.getSelectedRows(); 
      int wk;
      for(wk=0; wk<selected.length; wk++) {
        int srow = pRowToIndex[selected[wk]];
        if(srow != vrow)
          if (setValueAtHelper(value, srow, col))
            edited = true;

      }
      if(edited) {
        fireTableDataChanged();
        pParent.doEdited();
        pParent.updateFromTable();
      }
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
    switch(col) {
    case 0:
      return false;
      
    case 1:
      return setIntValue(pBiases, value, srow);
    case 2:
      return setDoubleValue(pMaxShares, value, srow);
    }
    return false;
  }
  
  private boolean
  setDoubleValue
  (
    ArrayList<Double> list,
    Object value, 
    int srow 
  )
  {
    Object oldValue = list.get(srow);
    if (oldValue == null) {
      if (value == null)
        return false;
      else {
        list.set(srow, JManageBalanceGroupsDialog.clampDouble((Double) value));
        pEditedIndices.add(srow);
        return true;
      }
    }
    else {
      if (value == null) {
        list.set(srow, null);
        pEditedIndices.add(srow);
        return true;
      }
      else {
        if (value.equals(oldValue)) 
          return false;
        else {
          list.set(srow, JManageBalanceGroupsDialog.clampDouble((Double) value));
          pEditedIndices.add(srow);
          return true;
        }
      }
    }    
  }
  
  private boolean
  setIntValue
  (
    ArrayList<Integer> list,
    Object value, 
    int srow 
  )
  {
    Object oldValue = list.get(srow);
    if (oldValue == null) {
      if (value == null)
        return false;
      else {
        list.set(srow, JManageBalanceGroupsDialog.clampInt((Integer) value));
        pEditedIndices.add(srow);
        return true;
      }
    }
    else {
      if (value == null) {
        list.set(srow, null);
        pEditedIndices.add(srow);
        return true;
      }
      else {
        if (value.equals(oldValue)) 
          return false;
        else {
          list.set(srow, JManageBalanceGroupsDialog.clampInt((Integer) value));
          pEditedIndices.add(srow);
          return true;
        }
      }
    }    
  }
  


  
  /*----------------------------------------------------------------------------------------*/
  /*   E N U M E R A T I O N S                                                              */
  /*----------------------------------------------------------------------------------------*/

  public static
  enum TableType
  {
    USER, GROUP
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -2130661542091790250L;

  private static final int sDataWidth = 65;
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Parent dialog.
   */
  private JManageBalanceGroupsDialog pParent; 
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * The details of the administrative privileges granted to the current user. 
   */ 
  private PrivilegeDetails  pPrivilegeDetails;
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The user/group name.
   */
  private ArrayList<String> pNames;
  
  /**
   * The queue shares.
   */
  private ArrayList<Integer> pBiases;
  
  /**
   * The max percentages.
   */
  private ArrayList<Double> pMaxShares;
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * What sort of data is this table representing
   */
  private TableType pTableType;
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * The indices of groups which have had their selection biases edited.
   */ 
  private TreeSet<Integer>  pEditedIndices;
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * The shared renderer for all double balance group data fields.
   */  
  private TableCellRenderer pDataRenderer;
  
  /**
   * The shared renderer for all percent balance group data fields.
   */
  private TableCellRenderer pPercentRenderer;
  
  /**
   * The shared editor for all balance group percent fields.
   */
  private TableCellEditor pPercentEditor;
  
  /**
   * The shared editor for all integer balance group data fields.
   */ 
  private TableCellEditor pIntEditor;

  /** 
   * The cell render for balance group names.
   */ 
  private TableCellRenderer  pNameRenderer;
}
