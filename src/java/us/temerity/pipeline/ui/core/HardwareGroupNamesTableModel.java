// $Id: HardwareGroupNamesTableModel.java,v 1.1 2007/11/30 20:06:25 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import us.temerity.pipeline.HardwareGroup;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   H A R D W A R E   G R O U P   N A M E S   T A B L E   M O D E L                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A {@link SortableTableModel SortableTableModel} which contains the names of the existing
 * {@link HardwareGroup} instances.
 */ 
public
class HardwareGroupNamesTableModel
  extends AbstractSortableTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   */
  public 
  HardwareGroupNamesTableModel
  (
   JManageHardwareKeysDialog parent
  ) 
  {
    super();
    
    /* initialize the fields */ 
    {
      pParent = parent;
      pNames = new ArrayList<String>();
    }

    /* initialize the columns */ 
    { 
      pNumColumns = 1;

      {
	Class classes[] = { String.class }; 
	pColumnClasses = classes;
      }

      {
	String names[] = { "Group" };
	pColumnNames = names;
      }

      {
	String desc[] = { "The hardware group name." };
	pColumnDescriptions = desc;
      }

      {
	int widths[] = { 180 };
	pColumnWidths = widths;
      }

      {
	TableCellRenderer renderers[] = { new JSimpleTableCellRenderer(JLabel.CENTER) };
	pRenderers = renderers;
      }

      {
	TableCellEditor editors[] = { null };
	pEditors = editors;
      }
    }
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
    for(String name : pNames) {      
      Comparable value = name;

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

    pParent.sortGroupsTable(pRowToIndex);
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
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set table data.
   */ 
  public void
  setNames
  (
   Collection<String> names
  ) 
  {
    pNames.clear();
    if(names != null)
      pNames.addAll(names);

    sort();
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
    return pNames.size();
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
    return pNames.get(pRowToIndex[row]);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7621642149771928163L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent dialog.
   */ 
  private JManageHardwareKeysDialog pParent;
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * The names of the groups.
   */ 
  private ArrayList<String> pNames;

}
