// $Id: BalanceGroupCalcTableModel.java,v 1.4 2009/12/16 04:13:33 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import us.temerity.pipeline.math.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   B A L A N C E   G R O U P   C A L C   T A B L E   M O D E L                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Table model for the calculated balance group values 
 */
public 
class BalanceGroupCalcTableModel
  extends AbstractSortableTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   * 
   * @param parent
   *   Parent dialog.
   */
  public 
  BalanceGroupCalcTableModel
  (
    JManageBalanceGroupsDialog parent
  ) 
  {
    super();
    
    /* initialize the fields */ 
    {
      pParent = parent;

      pNames        = new ArrayList<String>();
      pFairShares   = new ArrayList<Double>();
      pMaxShares    = new ArrayList<Double>();
      pMaxSlots     = new ArrayList<Integer>();
      pActualShares = new ArrayList<Double>();
    }
    
    /* all columns are dynamic, just initialize the shared renderers/editors */
    pDataRenderer = new JSimpleTableCellRenderer("Green", JLabel.CENTER);
    pNameRenderer = new JSimpleTableCellRenderer(JLabel.CENTER);
    pPercentRenderer = new JPercentTableCellRenderer("Green", JLabel.CENTER, false, 2);
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Set the table's information from the balance group info.
   * 
   * @param fairShares
   *   The calculated fair share values indexed by name.
   * @param actualShares
   *   The percent of the balance group each user has utilized.
   * @param maxShares
   *   The calculated max shares indexed by name.
   * @param maxSlots
   *   The total number of slots a user has a right to.
   */
  public void
  setCalculatedData
  (
    Map<String, Double> fairShares,
    Map<String, Double> actualShares,
    Map<String, Double> maxShares,
    Map<String, Integer> maxSlots
  )
  {
    TreeSet<String> names = new TreeSet<String>();
    names.addAll(fairShares.keySet());
    names.addAll(maxShares.keySet());
    
    pNames = new ArrayList<String>(names);
    pFairShares.clear();
    pMaxShares.clear();
    pMaxSlots.clear();
    pActualShares.clear();
    
    for (String name : pNames) {
      pFairShares.add(fairShares.get(name));
      pMaxShares.add(maxShares.get(name));
      pActualShares.add(actualShares.get(name));
      pMaxSlots.add(maxSlots.get(name));
    }
    
    pNumRows = pNames.size();
    
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
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S O R T I N G                                                                        */
  /*----------------------------------------------------------------------------------------*/

  @Override
  public void 
  sort()
  {
    IndexValue cells[] = new IndexValue[pNumRows]; 
    
    int size = pNames.size(); 
    for (int idx = 0; idx < size; idx++) {
      Comparable value = null;
      
      ColumnType type = ColumnType.typeFromOrdinal(pSortColumn);
      
      switch(type) {
      case Name:
        value = pNames.get(idx);
        break;
      case FairShare:
        value = pFairShares.get(idx);
        break;
      case MaxShare:
        value = pMaxShares.get(idx);
        break;
      case MaxSlots:
        value = pMaxSlots.get(idx);
        break;
      case ActualShare:
        value = pActualShares.get(idx);
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
    switch(ColumnType.typeFromOrdinal(col)) {
    case Name:
      return new Vector3i(140, 140, Integer.MAX_VALUE);
      
    case MaxSlots:
      return new Vector3i(80);
      
    case ActualShare:
      return new Vector3i(75);
      
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
    switch(ColumnType.typeFromOrdinal(col)) {
    case Name:
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
    switch(ColumnType.typeFromOrdinal(col)) {
    case Name:
      return "The name of the user.";
      
    case FairShare:
      return "The calculated percentage of the balance group that the user will receive " +
      	     "based on their individual bias and any group biases that they receive.";
      
    case MaxShare:
      return "The max percentage of the balance group the user can have at any one time.";
      
    case ActualShare:
      return "The percentage of the balance group that the user has used over the tracked " +
      	     "period.";
      
    case MaxSlots:
      return "The maximum number of slots that a user can have access to at one time in " +
      	     "the balance group.";
    }
    return null;
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
    switch(ColumnType.typeFromOrdinal(col)) {
    case Name:
      return pNameRenderer;
      
    case MaxShare:
    case ActualShare:
    case FairShare:
      return pPercentRenderer;

    default:
      return pDataRenderer; 
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
    return null;
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
    switch(ColumnType.typeFromOrdinal(col)) {
    case Name:
      return String.class;
      
    case MaxSlots:
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
    return ColumnType.values().length;
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
    switch(ColumnType.typeFromOrdinal(col)) {
    case Name:
        return "User";
    case FairShare:
      return "Fair";
    case MaxShare:
      return "Max";
    case MaxSlots:
      return "MaxSlots";
    case ActualShare:
      return "Actual";
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
    int vrow = pRowToIndex[row];
    switch(ColumnType.typeFromOrdinal(col)) {
    case Name:
      return pNames.get(vrow);

    case FairShare:
      return pFairShares.get(vrow);
    
    case ActualShare:
      return pActualShares.get(vrow);
      
    case MaxSlots:
      return pMaxSlots.get(vrow);
      
    case MaxShare:
      return pMaxShares.get(vrow);
      
    default:
      throw new IllegalStateException("Illegal index.");
    }
  }
  
 
  
  /*----------------------------------------------------------------------------------------*/
  /*   E N U M                                                                              */
  /*----------------------------------------------------------------------------------------*/
 
  private enum
  ColumnType
  {
    Name, FairShare, MaxShare, ActualShare, MaxSlots;
    
    public static ColumnType
    typeFromOrdinal
    (
      int i  
    )
    {
      return values()[i];
    }
    
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -5028179389451613934L;

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
   * The user/group name.
   */
  private ArrayList<String> pNames;
  
  /**
   * The calculated user shares.
   */
  private ArrayList<Double> pFairShares;
  
  /**
   * The max percentages.
   */
  private ArrayList<Double> pMaxShares;
  
  /**
   * The current usage of the farm for each user.
   */
  private ArrayList<Double> pActualShares;
  
  /**
   * The maximum number of slots each user can get.
   */
  private ArrayList<Integer> pMaxSlots;
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * The shared renderer for all shorter balance group data fields.
   */  
  private TableCellRenderer pDataRenderer;
  
  /**
   * The shared renderer for all percent balance group data fields.
   */
  private TableCellRenderer pPercentRenderer;
  
  /** 
   * The cell render for balance group names.
   */ 
  private TableCellRenderer  pNameRenderer;
}
