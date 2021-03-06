// $Id: SuffixEditorTableModel.java,v 1.10 2009/08/19 23:42:47 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.ui.*;

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
  extends AbstractSortableTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   */
  public 
  SuffixEditorTableModel
  (
    int channel  
  ) 
  {
    super();
    
    /* initialize the columns */ 
    { 
      pNumColumns = 5;

      {
	Class classes[] = { 
	  String.class, String.class, BaseEditor.class, String.class, String.class 
	}; 
	pColumnClasses = classes;
      }

      {
	String names[] = { "Suffix", "Format Description", "Editor", "Version", "Vendor" };
	pColumnNames = names;
      }

      {
	String desc[] = {
	  "The filename suffix.", 
	  "A short description of the file format.", 
	  "The name of the default Editor plugin for this file format.",
	  "The revision number of the Editor plugin.", 
	  "The name of the Editor plugin vendor."
	};
	pColumnDescriptions = desc;
      }

      {
	Vector3i ranges[] = { 
          new Vector3i(80), 
          new Vector3i(180, 360, Integer.MAX_VALUE), 
          new Vector3i(120), 
          new Vector3i(120), 
          new Vector3i(120) 
        };
	pColumnWidthRanges = ranges;
      }

      {
	TableCellRenderer renderers[] = {
	  new JSimpleTableCellRenderer(JLabel.CENTER), 
	  new JSimpleTableCellRenderer(JLabel.LEFT), 
	  new JPluginTableCellRenderer(JLabel.CENTER), 
	  new JSimpleTableCellRenderer(JLabel.CENTER), 
	  new JSimpleTableCellRenderer(JLabel.CENTER)
	};
	pRenderers = renderers;
      }

      {
	pPluginCellEditor = new JEditorSelectionTableCellEditor(channel, 120);

	TableCellEditor editors[] = {
	  null, 
	  new JStringTableCellEditor(200, JLabel.LEFT), 
	  pPluginCellEditor,
	  null, 
	  null
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
  @SuppressWarnings("unchecked")
  @Override
  public void 
  sort()
  {
    IndexValue cells[] = new IndexValue[pNumRows]; 
    int idx = 0;
    for(SuffixEditor se : pSuffixEditors) {
      BaseEditor editor = se.getEditor();
      Comparable value = null;
      switch(pSortColumn) {
      case 0:
	value = se.getSuffix();
	break;

      case 1:
	value = se.getDescription(); 
	break;

      case 2:
	if(editor != null)
	  value = editor.getName();
	break;

      case 3:
	if(editor != null)
	  value = editor.getVersionID();
	break;

      case 4:
	if(editor != null)
	  value = editor.getVendor();
	break;
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
    if(editors != null) 
      pSuffixEditors.addAll(editors);

    pNumRows = pSuffixEditors.size();

    sort();

    pPluginCellEditor.updateMenus();
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
    return ((col > 0) && (col < 3));
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
    BaseEditor editor = se.getEditor();
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
      return editor; 
	
    case 3:
      {
	if(editor == null)
	  return "-";
	else 
	  return editor.getVersionID().toString();
      }

    case 4:
      {
	if(editor == null)
	  return "-";
	else 
	  return editor.getVendor();
      }

    default:
      assert(false);
      return null;
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
    int vrow = pRowToIndex[row];
    setValueAtHelper(value, vrow, col);

    int[] selected = pTable.getSelectedRows(); 
    int wk;
    for(wk=0; wk<selected.length; wk++) {
      int srow = pRowToIndex[selected[wk]];
      if(srow != vrow)
	setValueAtHelper(value, srow, col);
    }

    fireTableDataChanged();
  }

  public void 
  setValueAtHelper
  (
   Object value, 
   int srow, 
   int col
  ) 
  {
    SuffixEditor se = pSuffixEditors.get(srow);
    switch(col) {
    case 1:
      {
	String str = (String) value;
	if(str.length() == 0)
	  se.setDescription(null);
	else 
	  se.setDescription(str);
      }
      break;

    case 2:
      se.setEditor((BaseEditor) value);
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

  /**
   * The cell editor used to select the Editor plugins.
   */ 
  private JEditorSelectionTableCellEditor  pPluginCellEditor; 
}
