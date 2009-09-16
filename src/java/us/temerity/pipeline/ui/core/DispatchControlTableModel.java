// $Id: DispatchControlTableModel.java,v 1.1 2009/09/16 03:54:40 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.ui.*;
import us.temerity.pipeline.ui.AbstractSortableTableModel.*;

/*------------------------------------------------------------------------------------------*/
/*   D I S P A T C H   C O N T R O L   T A B L E   M O D E L                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A {@link SortableTableModel} which contains a set of {@link DispatchControl} instances.
 */ 
public 
class DispatchControlTableModel
  extends AbstractSortableTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   */
  public 
  DispatchControlTableModel
  (
    JManageDispatchControlsDialog parent
  ) 
  {
    super();
    
    /* initialize the fields */ 
    {
      pParent = parent;

      pPrivilegeDetails = new PrivilegeDetails();    

      pDispatchControls = new ArrayList<DispatchControl>();
      
      pEditedIndices = new TreeSet<Integer>();
    }
    
    /* all columns are dynamic, just initialize the shared renderers/editors */ 
    pCriteriaRenderer = new JSimpleTableCellRenderer("Green", JLabel.CENTER);
    pNameRenderer     = new JSimpleTableCellRenderer(JLabel.CENTER);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set table data.
   *
   * @param controls
   *   Current dispatch controls indexed by group name.
   * 
   * @param privileges
   *   The details of the administrative privileges granted to the current user. 
   */ 
  public void 
  setDispatchControls
  (
    TreeMap<String, DispatchControl> controls, 
    PrivilegeDetails privileges
  ) 
  {
    pDispatchControls.clear();
    if(controls!= null)
      pDispatchControls.addAll(controls.values());
    
    pNumRows = pDispatchControls.size();

    pPrivilegeDetails = privileges;     

    pEditedIndices.clear();

    sort();
    fireTableStructureChanged(); 
  }
  
  /** 
   * Get the name of the dispatch control on the given row.
   */
  public String
  getControlName
  (
    int row
  ) 
  { 
    DispatchControl control = pDispatchControls.get(pRowToIndex[row]);
    if(control!= null) 
      return control.getName();
    return null;
  }
  
  /** 
   * Get the dispatch control on the given row.
   */
  public DispatchControl
  getControl
  (
    int row
  ) 
  { 
    return pDispatchControls.get(pRowToIndex[row]);
  }
  
  /**
   * Get the modified dispatch controls. 
   */ 
  public ArrayList<DispatchControl> 
  getModifiedControls() 
  {
    ArrayList<DispatchControl> edited = new ArrayList<DispatchControl>();
    for(Integer idx : pEditedIndices) {
      DispatchControl control = pDispatchControls.get(idx);
      if(control != null) 
        edited.add(control); 
    }
    
    if(!edited.isEmpty()) 
      return edited;

    return null;
  }
  
  /*----------------------------------------------------------------------------------------*/
  /*   S O R T I N G                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Sort the rows by the values in the current sort column and direction.
   */ 
  @Override
  public void 
  sort()
  {
    IndexValue cells[] = new IndexValue[pNumRows]; 
    int idx = 0;
    for(DispatchControl control : pDispatchControls) {
      Comparable value = null;
      switch(pSortColumn) {
      case 0:
        value = control.getName(); 
        break;
        
      default:
        {
          value = 
            control.getCriteria().toArray(new DispatchCriteria[0])[pSortColumn-1].toString();
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
  /*   E D I T I N G                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Move the criteria in the specified column up one level in the control in the given row.
   * 
   * @param col
   *   The column containing the criteria to move.
   * 
   * @param row
   *   The row the control being edited is in.
   */
  public void
  moveUp
  (
    int col,
    int row
  )
  {
    DispatchControl control = getControl(row);
    DispatchCriteria crit = control.getCriteria(col - 1);
    control.moveUp(crit);
    
    pEditedIndices.add(row);
    pParent.doEdited();

    sort();
  }
  
  /**
   * Move the criteria in the specified column down one level in the control in the given row.
   * 
   * @param col
   *   The column containing the criteria to move.
   * 
   * @param row
   *   The row the control being edited is in.
   */
  public void
  moveDown
  (
    int col,
    int row
  )
  {
    DispatchControl control = getControl(row);
    DispatchCriteria crit = control.getCriteria(col - 1);
    control.moveDown(crit);
    
    pEditedIndices.add(row);
    pParent.doEdited();
    
    sort();
  }

  /**
   * Move the criteria in the specified column to the top in the control in the given row.
   * 
   * @param col
   *   The column containing the criteria to move.
   * 
   * @param row
   *   The row the control being edited is in.
   */
  public void
  makeTop
  (
    int col,
    int row
  )
  {
    DispatchControl control = getControl(row);
    DispatchCriteria crit = control.getCriteria(col - 1);
    control.makeTop(crit);
    
    pEditedIndices.add(row);
    pParent.doEdited();
    
    sort();
  }
  
  /**
   * Move the criteria in the specified column to the bottom in the control in the given row.
   * 
   * @param col
   *   The column containing the criteria to move.
   * 
   * @param row
   *   The row the control being edited is in.
   */
  public void
  makeBottom
  (
    int col,
    int row
  )
  {
    DispatchControl control = getControl(row);
    DispatchCriteria crit = control.getCriteria(col - 1);
    control.makeBottom(crit);
    
    pEditedIndices.add(row);
    pParent.doEdited();
    
    sort();
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
      return "The name of the dispatch criteria."; 
      
    default:
      return "Right-click to move criteria up or down in this control.";
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
      return pCriteriaRenderer; 
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
    return String.class; 
  }
  
  /**
   * Returns the number of columns in the model.
   */ 
  @Override
  public int
  getColumnCount()
  {
    return DispatchCriteria.values().length + 1;
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
      return "Control Name"; 

    default:
      return "Criteria " + (col - 1);
    }
  }
  
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
    DispatchControl control = pDispatchControls.get(pRowToIndex[row]);
    switch(col) {
    case 0:
      return control.getName();
      
    default:
      {
        return control.getCriteria().toArray(new DispatchCriteria[0])[col-1];
      }
    }
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 207829936796595423L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent dialog.
   */ 
  private JManageDispatchControlsDialog pParent;

  /*----------------------------------------------------------------------------------------*/

  /**
   * The details of the administrative privileges granted to the current user. 
   */ 
  private PrivilegeDetails  pPrivilegeDetails;
  
  /**
   * The underlying set of dispatch controls.
   */ 
  private ArrayList<DispatchControl> pDispatchControls;

  /*----------------------------------------------------------------------------------------*/

  /**
   * The indices of groups which have had their criteria edited.
   */ 
  private TreeSet<Integer>  pEditedIndices; 

  /*----------------------------------------------------------------------------------------*/

  /**
   * Renderer for the different criteria.
   */
  private JSimpleTableCellRenderer pCriteriaRenderer;
  
  /** 
   * The cell render for dispatch control names.
   */ 
  private TableCellRenderer  pNameRenderer; 

}
