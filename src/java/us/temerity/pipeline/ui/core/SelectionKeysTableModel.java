// $Id: SelectionKeysTableModel.java,v 1.5 2005/05/31 09:37:45 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   S E L E C T I O N   K E Y S   T A B L E   M O D E L                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A {@link SortableTableModel SortableTableModel} which contains a set of 
 * {@link SelectionKey SelectionKey} instances.
 */ 
public
class SelectionKeysTableModel
  extends AbstractSortableTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   */
  public 
  SelectionKeysTableModel()
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
	  "The name of the selection key.", 
	  "A short description of the use of the key.", 
	};
	pColumnDescriptions = desc;
      }

      {
	int widths[] = { 120, 360 };
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

    pSelectionKeys = new ArrayList<SelectionKey>();
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
    for(SelectionKey key : pSelectionKeys) {
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
    SelectionKey key = pSelectionKeys.get(pRowToIndex[row]);
    if(key != null) 
      return key.getName();
    return null;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the underlying set of editors.
   */ 
  public void
  setSelectionKeys
  (
   ArrayList<SelectionKey> keys
  ) 
  {
    pSelectionKeys.clear();
    pSelectionKeys.addAll(keys);

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
    return pSelectionKeys.size();
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
    SelectionKey key = pSelectionKeys.get(pRowToIndex[row]);
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

  private static final long serialVersionUID = -222254696141176166L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The underlying set of editors.
   */ 
  private ArrayList<SelectionKey> pSelectionKeys;


}
