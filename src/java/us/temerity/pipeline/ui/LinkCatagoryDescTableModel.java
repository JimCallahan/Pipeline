// $Id: LinkCatagoryDescTableModel.java,v 1.1 2004/06/28 23:39:45 jim Exp $

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
/*   L I N K   C A T A G O R Y   D E S C    T A B L E   M O D E L                           */
/*------------------------------------------------------------------------------------------*/

/**
 * A {@link SortableTableModel SortableTableModel} which contains a set of 
 * {@link LinkCatagoryDesc LinkCatagoryDesc} instances.
 */ 
public
class LinkCatagoryDescTableModel
  extends SortableTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   */
  public 
  LinkCatagoryDescTableModel() 
  {
    super();
    
    /* initialize the columns */ 
    { 
      pNumColumns = 4;

      {
	Class classes[] = { String.class, String.class, String.class, String.class }; 
	pColumnClasses = classes;
      }

      {
	String names[] = { "Name", "Policy", "Description", "Active" };
	pColumnNames = names;
      }

      {
	int widths[] = { 150, 150, 500, 80 };
	pColumnWidths = widths;
      }

      {
	TableCellRenderer renderers[] = {
	  new JSimpleTableCellRenderer(JLabel.CENTER), 
	  new JSimpleTableCellRenderer(JLabel.CENTER), 
	  new JSimpleTableCellRenderer(JLabel.LEFT), 
	  new JSimpleTableCellRenderer(JLabel.CENTER)
	};
	pRenderers = renderers;
      }

      {
	TableCellEditor editors[] = { null, null, null, null };
	pEditors = editors;
      }
    }

    pLinkCatagories  = new ArrayList<LinkCatagoryDesc>();
    pActive = new TreeSet<String>();
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
    ArrayList<String> values = new ArrayList<String>();
    ArrayList<Integer> indices = new ArrayList<Integer>();
    int idx = 0;
    for(LinkCatagoryDesc lcd : pLinkCatagories) {
      String value = null;
      switch(pSortColumn) {
      case 0:
	value = lcd.getName();
	break;

      case 1:
	value = lcd.getPolicy().toTitle();
	break;

      case 2:
	value = lcd.getDescription(); 
	break;

      case 3:
	value = (pActive.contains(lcd.getName()) ? "YES" : "no");
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
   * Set the underlying link catagory descriptions and active status.
   */ 
  public void
  updateAll
  (
    TreeMap<String,LinkCatagoryDesc> table, 
    TreeSet<String> active
  ) 
  {
    pLinkCatagories.clear();
    pLinkCatagories.addAll(table.values());

    pActive.clear();
    pActive.addAll(active);

    sort();
  }

  /**
   * Get the names of the link catagories for the given rows.
   */ 
  public TreeSet<String>
  getRowNames
  (
   int rows[]
  ) 
  {
    TreeSet<String> names = new TreeSet<String>();

    if((rows != null) && (rows.length > 0)) {
      int wk;
      for(wk=0; wk<rows.length; wk++) 
	names.add(pLinkCatagories.get(pRowToIndex[rows[wk]]).getName());
    }

    return names;
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
    return pLinkCatagories.size();
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
    LinkCatagoryDesc lcd = pLinkCatagories.get(pRowToIndex[row]);
    switch(col) {
    case 0:
      return lcd.getName();
      
    case 1:
      return lcd.getPolicy().toTitle();
      
    case 2:
      return lcd.getDescription(); 
      
    case 3:
      return (pActive.contains(lcd.getName()) ? "YES" : "no");
    
    default:
      assert(false);
      return null;
    }    
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5747458727701610017L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The underlying link catagory descriptions.
   */ 
  private ArrayList<LinkCatagoryDesc> pLinkCatagories;

  /**
   * The names of the active link catagories.
   */ 
  private TreeSet<String>  pActive; 

}
