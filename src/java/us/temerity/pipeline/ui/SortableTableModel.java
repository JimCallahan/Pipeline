// $Id: SortableTableModel.java,v 1.2 2004/06/28 00:19:40 jim Exp $

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
class SortableTableModel
  extends AbstractTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   */
  public 
  SortableTableModel()   
  {
    super();

    pSortColumn    = 0;
    pSortAscending = true;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the widths of the columns.
   */ 
  public int[] 
  getColumnWidths() 
  {
    return pColumnWidths;
  }

  /**
   * Get the renderers for each column. 
   */ 
  public TableCellRenderer[] 
  getRenderers() 
  {
    return pRenderers;
  }

  /**
   * Get the renderers for each column. 
   */ 
  public TableCellEditor[] 
  getEditors() 
  {
    return pEditors;
  }




  /*----------------------------------------------------------------------------------------*/
  /*   S O R T I N G                                                                        */
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
  protected abstract void 
  sort();




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
   * The render for each column
   */ 
  protected TableCellRenderer  pRenderers[]; 
  
  /**
   * The editor for each column
   */ 
  protected TableCellEditor  pEditors[]; 
}
