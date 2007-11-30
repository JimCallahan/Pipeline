// $Id: HardwareKeysTableModel.java,v 1.1 2007/11/30 20:06:25 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import us.temerity.pipeline.HardwareKey;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   H A R D W A R E   K E Y S   T A B L E   M O D E L                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A {@link SortableTableModel SortableTableModel} which contains a set of 
 * {@link HardwareKey} instances.
 */ 
public
class HardwareKeysTableModel
  extends AbstractSortableTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   */
  public 
  HardwareKeysTableModel()
  {
    super();

    /* initialize the columns */ 
    { 
      pNumColumns = 2;

      {
	Class classes[] = { String.class, String.class }; 
	pColumnClasses = classes;
      }

      {
	String names[] = {"Key", "Description" };
	pColumnNames = names;
      }

      {
	String desc[] = {
	  "The name of the hardware key.", 
	  "A short description of the use of the key.", 
	};
	pColumnDescriptions = desc;
      }

      {
	int widths[] = { 120, 708 };
	pColumnWidths = widths;
      }

      {
	TableCellRenderer renderers[] = {
	  new JSimpleTableCellRenderer(JLabel.CENTER), 
	  new JSimpleTableCellRenderer(JLabel.LEFT)
	};
	pRenderers = renderers;
      }

      {
	TableCellEditor editors[] = {
	  null, 
	  null
	};
	pEditors = editors;
      }
    }

    pHardwareKeys = new ArrayList<HardwareKey>();
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
    for(HardwareKey key : pHardwareKeys) {
      Comparable value = null;
      switch(pSortColumn) {
      case 0:
	value = key.getName();
	break;

      case 1:
	value = key.getDescription();  
	if(value == null) 
	  value = "";
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
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of license key at the given row.
   */
  public String 
  getName
  (
   int row
  ) 
  {
    HardwareKey key = pHardwareKeys.get(pRowToIndex[row]);
    if(key != null) 
      return key.getName();
    return null;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the underlying set of editors.
   */ 
  public void
  setHardwareKeys
  (
   ArrayList<HardwareKey> keys
  ) 
  {
    pHardwareKeys.clear();
    pHardwareKeys.addAll(keys);

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
    return pHardwareKeys.size();
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
    HardwareKey key = pHardwareKeys.get(pRowToIndex[row]);
    switch(col) {
    case 0:
      return key.getName();

    case 1:
      return key.getDescription(); 

    default:
      assert(false);
      return null;
    }    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2089378299938038139L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The underlying set of editors.
   */ 
  private ArrayList<HardwareKey> pHardwareKeys;


}
