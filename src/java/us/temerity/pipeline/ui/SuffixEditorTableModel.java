// $Id: SuffixEditorTableModel.java,v 1.1 2004/06/08 03:06:36 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   S U F F I X   E D I T O R   T A B L E   M O D E L                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A {@link SortableTableModel SortableTableModel} which contains a set of 
 * {@link SuffixEditor SuffixEditor} instances.
 */ 
public
class SuffixEditorTableModel
  extends SortableTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   */
  public 
  SuffixEditorTableModel()
  {
    super();

    pEditors = new ArrayList<SuffixEditor>();
   
    pSortColumn    = 0;
    pSortAscending = true;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Sort the rows by the values in the given column number. 
   */ 
  public void 
  sortByColumn
  (
   int col
  ) 
  {
    pSortAscending = (pSortColumn == col) ? !pSortAscending : true;
    pSortColumn    = col;
    
    sort();
  }

  /**
   * Sort the rows by the values in the current sort column and direction.
   */ 
  private void 
  sort()
  {

    ArrayList<String> values = new ArrayList<String>();
    ArrayList<Integer> indices = new ArrayList<Integer>();
    int idx = 0;
    for(SuffixEditor se : pEditors) {
      String value = null;
      switch(pSortColumn) {
      case 0:
	value = se.getSuffix();
	break;

      case 1:
	value = se.getDescription(); 
	if(value == null) 
	  value = "";
	break;

      case 2:
	value = se.getEditor();
	if(value == null)
	  value = "-";
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
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the underlying set of editors.
   */ 
  public TreeSet<SuffixEditor>
  getEditors() 
  {
    return new TreeSet<SuffixEditor>(pEditors);
  }

  /**
   * Get the underlying set of editors.
   */ 
  public void
  setEditors
  (
    TreeSet<SuffixEditor> editors
  ) 
  {
    pEditors.clear();
    pEditors.addAll(editors);

    sort();
  }


  /**
   * Get index of the row which contains the given editor.
   */ 
  public int
  getRow
  (
   SuffixEditor se
  )
  {
    int idx = pEditors.indexOf(se);
    if(idx != -1) {
      int row;
      for(row=0; row<pRowToIndex.length; row++) 
	if(pRowToIndex[row] == idx) 
	  return row;
    }

    return -1;
  }


  /**
   * Remove the rows with the given indices.
   */ 
  public void 
  removeRows
  (
   int rows[]
  ) 
  {
    if((rows == null) || (rows.length == 0)) 
      return;

    TreeSet<SuffixEditor> dead = new TreeSet<SuffixEditor>();
    int wk;
    for(wk=0; wk<rows.length; wk++) 
      dead.add(pEditors.get(pRowToIndex[rows[wk]]));
    
    for(SuffixEditor se : dead) 
      pEditors.remove(se);

    fireTableDataChanged();
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
    return String.class;
  }

  /**
   * Returns the number of rows in the model.
   */ 
  public int 
  getRowCount()
  {
    return pEditors.size();
  }

  /**
   * Returns the number of columns in the model.
   */ 
  public int
  getColumnCount()
  {
    return 3;
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
    return sColumnNames[col];
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
    SuffixEditor se = pEditors.get(pRowToIndex[row]);
    switch(col) {
    case 0:
      return se.getSuffix();

    case 1:
      {
	String desc = se.getDescription(); 
	if(desc == null) 
	  return "";
	else 
	  return desc;
      }

    case 2:
      {
	String editor = se.getEditor();
	if(editor == null)
	  return "-";
	else 
	  return editor;
      }

    default:
      assert(false);
      return null;
    }    
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
    return (col != 0);
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
    SuffixEditor se = pEditors.get(pRowToIndex[row]);
    String str = (String) value;

    switch(col) {
    case 1:
      if(str.length() == 0)
	se.setDescription(null);
      else 
	se.setDescription(str);
      break;

    case 2:
      if(str.equals("-")) 
	se.setEditor(null);
      else 
	se.setEditor(str);
      break;
      
    default:
      assert(false);
    }  
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6364186076302362940L;


  /**
   * The table column titles.
   */ 
  private final String sColumnNames[] = {
    "Suffix", 
    "Format Description", 
    "Editor"
  };


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The underlying set of editors.
   */ 
  private ArrayList<SuffixEditor> pEditors;


  /**
   * Editor indices for each displayed row number.
   */ 
  private int[] pRowToIndex;   

  /**
   * The number of the column used to sort rows.
   */ 
  private int pSortColumn;

  /**
   * Sort in ascending order?
   */ 
  private boolean pSortAscending;

}
