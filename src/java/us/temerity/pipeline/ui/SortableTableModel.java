// $Id: SortableTableModel.java,v 1.5 2005/03/04 09:20:30 jim Exp $

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
 * A table models which supports sorting of its columns and provides the necessary
 * information required by {@link JTablePanel JTablePanel}.
 */ 
public 
interface SortableTableModel
  extends TableModel
{
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
  );


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the width of the given column.
   */ 
  public int
  getColumnWidth
  (
   int col   
  );

  /**
   * Returns the color prefix used to determine the synth style of the header button for 
   * the given column.
   */ 
  public String 	
  getColumnColorPrefix
  (
   int col
  );

  /**
   * Returns the description of the column columnIndex used in tool tips.
   */ 
  public String 	
  getColumnDescription
  (
   int col
  );

  /**
   * Get the renderer for the given column. 
   */ 
  public TableCellRenderer
  getRenderer
  (
   int col   
  );

  /**
   * Get the editor for the given column. 
   */ 
  public TableCellEditor
  getEditor
  (
   int col   
  );


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
  );

  /**
   * Sort the rows by the values in the current sort column and direction.
   */ 
  public void 
  sort();


}
