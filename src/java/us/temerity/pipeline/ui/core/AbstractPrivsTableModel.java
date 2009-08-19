// $Id: AbstractPrivsTableModel.java,v 1.1 2009/08/19 23:42:47 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.ui.*;

import java.text.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   P R I V  T A B L E   M O D E L                                               */
/*------------------------------------------------------------------------------------------*/

/**
 * An abstract base class for common methods and field of {@link PrivilegesTableModel}
 * and {@link WorkGroupsTableModel}.
 */ 
public abstract
class AbstractPrivsTableModel
  extends AbstractSortableTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   */
  public 
  AbstractPrivsTableModel
  (
   JManagePrivilegesDialog parent
  ) 
  {
    super();
    
    /* initialize the fields */ 
    {
      pParent = parent;
      pPrivilegeDetails = new PrivilegeDetails();
      pUserNames = new ArrayList<String>();
      pEditedRows = new TreeSet<Integer>();
    }

    pNameRenderer = new JSimpleTableCellRenderer(JLabel.CENTER);
  }
 

 
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the user at the given row.
   */ 
  public String
  getUserName
  (
   int row
  ) 
  {
    return pUserNames.get(pRowToIndex[row]);
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set table data.
   *
   * @param groups
   *   The current work groups used to determine the scope of administrative privileges.
   * 
   * @param details
   *   The privileges granted to the current user with respect to all other users.
   */ 
  protected void 
  setPrivileges
  (
   WorkGroups groups, 
   PrivilegeDetails details
  ) 
  {
    pUserNames.clear();
    if(groups != null) 
      pUserNames.addAll(groups.getUsers());

    pNumRows = pUserNames.size();

    if(details != null) 
      pPrivilegeDetails = details; 
    
    pEditedRows.clear();
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S O R T A B L E   T A B L E   M O D E L   O V E R R I D E S                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the range of widths (min, preferred, max) of the column. 
   */
  public Vector3i
  getColumnWidthRange
  (
   int col   
  )
  {
    switch(col) {
    case 0:
      return new Vector3i(180, 180, 360);
      
    default:
      return new Vector3i(120); 
    }
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
    switch(col) {
    case 0:
      return pNameRenderer;

    default:
      return pCellRenderer;
    }
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
    switch(col) {
    case 0:
      return null;

    default:
      return pCellEditor;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   T A B L E   M O D E L   O V E R R I D E S                                            */
  /*----------------------------------------------------------------------------------------*/

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
    if(!pPrivilegeDetails.isMasterAdmin()) 
      return;

    int vrow = pRowToIndex[row];
    boolean edited = setValueAtHelper(value, vrow, col);
    
    int[] selected = pTable.getSelectedRows(); 
    int wk;
    for(wk=0; wk<selected.length; wk++) {
      int srow = pRowToIndex[selected[wk]];
      if(srow != vrow)
        if(setValueAtHelper(value, srow, col))
          edited = true;

    }
      
    if(edited) {
      fireTableDataChanged();
      pParent.doEdited();
    }
  }

  /**
   * Sets the value in the cell at columnIndex and rowIndex to aValue.
   */ 
  protected abstract boolean 
  setValueAtHelper
  (
   Object value, 
   int srow, 
   int col
  );


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  //private static final long serialVersionUID = 



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent dialog.
   */ 
  private JManagePrivilegesDialog pParent;

  /**
   * The privileges granted to the current user with respect to all other users.
   */ 
  protected PrivilegeDetails  pPrivilegeDetails; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The names of the users.
   */ 
  protected ArrayList<String>  pUserNames;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The indices of rows which have been modified. 
   */ 
  protected TreeSet<Integer>  pEditedRows; 


  /*----------------------------------------------------------------------------------------*/

  /** 
   * The cell render for user names.
   */ 
  private TableCellRenderer  pNameRenderer; 


  /**
   * The cell render for administrative privileges.
   */ 
  protected TableCellRenderer  pCellRenderer;

  /**
   * The cell editor for administrative privileges.
   */ 
  protected TableCellEditor  pCellEditor;
}
