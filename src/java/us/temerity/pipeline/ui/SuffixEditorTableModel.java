// $Id: SuffixEditorTableModel.java,v 1.8 2004/11/21 18:39:56 jim Exp $

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
    
    /* initialize the columns */ 
    { 
      pNumColumns = 3;

      {
	Class classes[] = { String.class, String.class, String.class }; 
	pColumnClasses = classes;
      }

      {
	String names[] = {"Suffix", "Format Description", "Editor" };
	pColumnNames = names;
      }

      {
	String desc[] = {
	  "The filename suffix.", 
	  "A short description of the file format.", 
	  "The name of the default Editor plugin for this file format."
	};
	pColumnDescriptions = desc;
      }

      {
	int widths[] = { 80, 600, 130 };
	pColumnWidths = widths;
      }

      {
	TableCellRenderer renderers[] = {
	  new JSimpleTableCellRenderer(JLabel.CENTER), 
	  new JSimpleTableCellRenderer(JLabel.LEFT), 
	  new JSimpleTableCellRenderer(JLabel.CENTER)
	};
	pRenderers = renderers;
      }

      {
	JCollectionTableCellEditor editor = null;
	{
	  PluginMgr mgr = PluginMgr.getInstance();
	  ArrayList<String> values = new ArrayList<String>(mgr.getEditors().keySet());
	  values.add("-");
	  editor = new JCollectionTableCellEditor(values, 130);
	}
	
	TableCellEditor editors[] = {
	  null, 
	  new JStringTableCellEditor(200, JLabel.LEFT), 
	  editor
	};
	pEditors = editors;
      }
    }

    pSuffixEditors = new ArrayList<SuffixEditor>();
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
    for(SuffixEditor se : pSuffixEditors) {
      Comparable value = null;
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
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the underlying set of editors.
   */ 
  public TreeSet<SuffixEditor>
  getSuffixEditors() 
  {
    return new TreeSet<SuffixEditor>(pSuffixEditors);
  }

  /**
   * Get the underlying set of editors.
   */ 
  public void
  setSuffixEditors
  (
    TreeSet<SuffixEditor> editors
  ) 
  {
    pSuffixEditors.clear();
    pSuffixEditors.addAll(editors);

    sort();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get index of the row which contains the given editor.
   */ 
  public int
  getRow
  (
   SuffixEditor se
  )
  {
    int idx = pSuffixEditors.indexOf(se);
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
      dead.add(pSuffixEditors.get(pRowToIndex[rows[wk]]));
    
    for(SuffixEditor se : dead) 
      pSuffixEditors.remove(se);

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
    return pSuffixEditors.size();
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
   * Returns the value for the cell at columnIndex and rowIndex.
   */ 
  public Object 	
  getValueAt
  (
   int row, 
   int col
  )
  {
    SuffixEditor se = pSuffixEditors.get(pRowToIndex[row]);
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
    String str = (String) value;

    int vrow = pRowToIndex[row];
    setValueAtHelper(str, vrow, col);

    int[] selected = pTable.getSelectedRows(); 
    int wk;
    for(wk=0; wk<selected.length; wk++) {
      int srow = pRowToIndex[selected[wk]];
      if(srow != vrow)
	setValueAtHelper(str, srow, col);
    }

    fireTableDataChanged();
  }

  public void 
  setValueAtHelper
  (
   String str, 
   int srow, 
   int col
  ) 
  {
    SuffixEditor se = pSuffixEditors.get(srow);
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



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The underlying set of editors.
   */ 
  private ArrayList<SuffixEditor> pSuffixEditors;



}
