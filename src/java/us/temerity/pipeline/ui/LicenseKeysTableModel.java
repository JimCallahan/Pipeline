// $Id: LicenseKeysTableModel.java,v 1.5 2004/11/21 18:39:56 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   L I C E N S E   K E Y S   T A B L E   M O D E L                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A {@link SortableTableModel SortableTableModel} which contains a set of 
 * {@link LicenseKey LicenseKey} instances.
 */ 
public
class LicenseKeysTableModel
  extends SortableTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   */
  public 
  LicenseKeysTableModel
  (
   JManageLicenseKeysDialog parent
  ) 
  {
    super();
    
    pParent = parent;

    /* initialize the columns */ 
    { 
      pNumColumns = 4;

      {
	Class classes[] = { String.class, String.class, Integer.class, Integer.class }; 
	pColumnClasses = classes;
      }

      {
	String names[] = {"Key", "Description", "Available", "Total" };
	pColumnNames = names;
      }

      {
	String desc[] = {
	  "The name of the license key.", 
	  "A short description of the use of the key.", 
	  "The number of available license keys.", 
	  "The total number of license keys."
	};
	pColumnDescriptions = desc;
      }

      {
	int widths[] = { 160, 450, 80, 80 };
	pColumnWidths = widths;
      }

      {
	TableCellRenderer renderers[] = {
	  new JSimpleTableCellRenderer(JLabel.CENTER), 
	  new JSimpleTableCellRenderer(JLabel.LEFT), 
	  new JSimpleTableCellRenderer(JLabel.CENTER),
	  new JSimpleTableCellRenderer(JLabel.CENTER)
	};
	pRenderers = renderers;
      }

      {
	TableCellEditor editors[] = {
	  null, 
	  null, 
	  null, 
	  new JIntegerTableCellEditor(80, JLabel.CENTER)
	};
	pEditors = editors;
      }
    }

    pLicenseKeys = new ArrayList<LicenseKey>();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S O R T I N G                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Sort the rows by the values in the current sort column and direction.
   */ 
  protected void 
  sort()
  {
    ArrayList<Comparable> values = new ArrayList<Comparable>();
    ArrayList<Integer> indices = new ArrayList<Integer>();
    int idx = 0;
    for(LicenseKey key : pLicenseKeys) {
      Comparable value = null;
      switch(pSortColumn) {
      case 0:
	value = key.getName();
	break;

      case 1:
	value = key.getDescription(); 
	if(value == null) 
	  value = "";
	break;

      case 2:
	value = new Integer(key.getAvailable());
	break;

      case 3:
	value = new Integer(key.getTotal());
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
   * Get the underlying set of editors.
   */ 
  public void
  setLicenseKeys
  (
   ArrayList<LicenseKey> keys, 
   boolean isPrivileged
  ) 
  {
    pLicenseKeys.clear();
    pLicenseKeys.addAll(keys);

    pIsPrivileged = isPrivileged;

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
    return pLicenseKeys.size();
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
    return ((col == 3) && pIsPrivileged);
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
    LicenseKey key = pLicenseKeys.get(pRowToIndex[row]);
    switch(col) {
    case 0:
      return key.getName();

    case 1:
      return key.getDescription(); 

    case 2:
      {
	Integer available = key.getAvailable();
	return available; 
      }

    case 3:
      {
	Integer total = key.getTotal();
	return total; 
      }

    default:
      assert(false);
      return null;
    }    
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
    switch(col) {
    case 3:
      {
	Integer total = (Integer) value;
	if(total >= 0) {
	  int vrow = pRowToIndex[row];
	  pLicenseKeys.get(vrow).setTotal(total);

	  int[] selected = pTable.getSelectedRows(); 
	  int wk;
	  for(wk=0; wk<selected.length; wk++) {
	    int srow = pRowToIndex[selected[wk]];
	    if(srow != vrow) 
	      pLicenseKeys.get(srow).setTotal(total);
	  }
	}

	fireTableDataChanged();
	pParent.doEdited(); 
      }
      break;
      
    default:
      assert(false);
    }  
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8973197792150585816L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Does the current user have privileged status?
   */ 
  private boolean  pIsPrivileged;

  /**
   * The underlying set of editors.
   */ 
  private ArrayList<LicenseKey> pLicenseKeys;

  /**
   * The parent dialog.
   */ 
  private JManageLicenseKeysDialog  pParent;

}
