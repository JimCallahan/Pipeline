// $Id: AbstractSortableTableModel.java,v 1.1 2005/03/04 09:20:30 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   S O R T A B L E   T A B L E   M O D E L                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The abstract base class of table models which support sorting of their columns.
 */ 
public abstract 
class AbstractSortableTableModel
  extends AbstractTableModel
  implements SortableTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   */
  public 
  AbstractSortableTableModel()   
  {
    super();

    pSortColumn    = 0;
    pSortAscending = true;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the parent table.
   */ 
  public void 
  setTable
  (
   JTable table
  ) 
  {
    pTable = table;
  }


  /**
   * Get the width of the given column.
   */ 
  public int
  getColumnWidth
  (
   int col   
  )
  {
    return pColumnWidths[col];
  }

  /**
   * Returns the color prefix used to determine the synth style of the header button for 
   * the given column.
   */ 
  public String 	
  getColumnColorPrefix
  (
   int col
  )
  {
    if(pColumnColorPrefix != null) 
      return pColumnColorPrefix[col];
    return "";
  }

  /**
   * Returns the description of the column columnIndex used in tool tips.
   */ 
  public String 	
  getColumnDescription
  (
   int col
  ) 
  {
    return pColumnDescriptions[col];
  }

  /**
   * Get the renderer for the given column. 
   */ 
  public TableCellRenderer
  getRenderer
  (
   int col   
  )
  {
    return pRenderers[col];
  }

  /**
   * Get the editor for the given column. 
   */ 
  public TableCellEditor
  getEditor
  (
   int col   
  )
  {
    return pEditors[col];
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S O R T I N G                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Sort the rows by the values in the given column number. <P> 
   * 
   * @return 
   *   The mapping of the pre-sort to post-sort row numbers.
   */ 
  public int[]
  sortByColumn
  (
   int col
  )
  {
    int[] idxToRow = new int[pRowToIndex.length];
    {
      int row;
      for(row=0; row<pRowToIndex.length; row++) 
	idxToRow[pRowToIndex[row]] = row;
    }

    pSortAscending = (pSortColumn == col) ? !pSortAscending : true;
    pSortColumn    = col;
    
    sort();

    int[] preToPost = new int[pRowToIndex.length];
    {
      int row;
      for(row=0; row<pRowToIndex.length; row++) 
	preToPost[idxToRow[pRowToIndex[row]]] = row;
    }

    return preToPost;
  }


  /**
   * Sort the rows by the values in the current sort column and direction.
   */ 
  public abstract void 
  sort();



  /*----------------------------------------------------------------------------------------*/
  /*   C O L U M N   V I S I B I L I T Y                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Notifies the model that columns visible to the user have changed.
   */ 
  public void 
  columnVisiblityChanged()
  {
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
    return pColumnClasses[col];
  }
  
  /**
   * Returns the number of columns in the model.
   */ 
  public int
  getColumnCount()
  {
    return pNumColumns;
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
    return pColumnNames[col];
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * The parent table.
   */ 
  protected JTable  pTable; 
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Param row indices for each displayed row number.
   */ 
  protected int[] pRowToIndex;   

  /**
   * The number of the column used to sort rows.
   */ 
  protected int pSortColumn;

  /**
   * Sort in ascending order?
   */ 
  protected boolean pSortAscending;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The number of columns.
   */ 
  protected int pNumColumns;

  /**
   * The type of each column.
   */ 
  protected Class  pColumnClasses[]; 

  /**
   * The UI names of the columns
   */ 
  protected String  pColumnNames[]; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The widths of the columns
   */ 
  protected int  pColumnWidths[]; 

  /**
   * The color prefix used to determine the synth style of the header button for 
   * the given column or <CODE>null</CODE> if all columns use the default style.
   */ 
  protected String  pColumnColorPrefix[]; 

  /**
   * The description of the columns used in tool tips.
   */ 
  protected String  pColumnDescriptions[];

  /**
   * The render for each column
   */ 
  protected TableCellRenderer  pRenderers[]; 
  
  /**
   * The editor for each column
   */ 
  protected TableCellEditor  pEditors[]; 
}
