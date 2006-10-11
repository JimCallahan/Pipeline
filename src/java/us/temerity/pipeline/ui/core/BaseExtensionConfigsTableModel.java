// $Id: BaseExtensionConfigsTableModel.java,v 1.1 2006/10/11 22:45:41 jim Exp $

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
/*   B A S E   E X T E N S I O N   C O N F I G S   T A B L E   M O D E L                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The base class of all table models for subclass instances of {@link ExtensionConfig}.
 */ 
public abstract
class BaseExtensionConfigsTableModel
  extends AbstractSortableTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   */
  public 
  BaseExtensionConfigsTableModel
  (
   String title,
   JManageServerExtensionsDialog parent
  ) 
  {
    super();

    /* initialize the fields */ 
    {
      pPrivilegeDetails = new PrivilegeDetails();  

      pParent = parent;
      pConfigs = new ArrayList<BaseExtensionConfig>();

      pModifiedIndices = new TreeSet<Integer>();
    }

    /* initialize the columns */ 
    { 
      pNumColumns = 6;

      {
	Class classes[] = { 
	  String.class, String.class, BaseExt.class, 
	  String.class, String.class, Boolean.class
	}; 
	pColumnClasses = classes;
      }

      {
	String names[] = { 
	  "Name", "Toolset", "Extension", "Version", "Vendor", "Enabled" 
	}; 
	pColumnNames = names;
      }

      {
	String desc[] = {
	  "The name of the " + title + " Extension configuration.", 
	  "The shell environment under which any spawned subprocess are run.", 
	  "The name of the " + title + " Extension plugin.", 
	  "The revision number of the " + title + " Extension plugin.", 
	  "The name of the " + title + " Extension plugin vendor.", 	  
	  "Whether the extension is currently active."
	};
	pColumnDescriptions = desc;
      }

      {
	int widths[] = { 
	  180, 120, 180, 120, 120, 120
	};
	pColumnWidths = widths;
      }

      {
	TableCellRenderer renderers[] = {
	  new JSimpleTableCellRenderer(JLabel.CENTER), 
	  new JSimpleTableCellRenderer(JLabel.CENTER), 
	  new JPluginTableCellRenderer(JLabel.CENTER), 
	  new JSimpleTableCellRenderer(JLabel.CENTER), 
	  new JSimpleTableCellRenderer(JLabel.CENTER), 
	  new JBooleanTableCellRenderer(JLabel.CENTER)
	};
	pRenderers = renderers;
      }

      {
	TableCellEditor editors[] = {
	  null, null, null, null, null, 
	  new JBooleanTableCellEditor(120, JLabel.CENTER, false)
	};
	pEditors = editors;
      }
    }
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
    for(BaseExtensionConfig config : pConfigs) {
      Comparable value = null;
      switch(pSortColumn) {
      case 0:
	value = config.getName();
	break;

      case 1:
	value = config.getToolset();
	break;

      case 2:
      case 3:
      case 4:
	try {
	  BaseExt ext = config.getExt();
	  if(ext != null) {
	    switch(pSortColumn) {
	    case 2:
	      value = ext.getName();
	      break;

	    case 3:
	      value = ext.getVersionID().toString();
	      break;

	    case 4:
	      value = ext.getVendor();
	    }
	  }
	  else {
	    value = "-";
	  }
	}
	catch(PipelineException ex) {
	  value = "-";
	}
	break;
	
      case 5:
	value = config.isEnabled();
	break;
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
  /*   T A B L E   M O D E L   O V E R R I D E S                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the number of rows in the model.
   */ 
  public int 
  getRowCount()
  {
    return pConfigs.size(); 
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
    return (pPrivilegeDetails.isMasterAdmin() && (col == 5));
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
    BaseExtensionConfig config = pConfigs.get(pRowToIndex[row]);
    switch(col) {
    case 0:
      return config.getName();
      
    case 1:
      return config.getToolset();

    case 2:
    case 3:
    case 4:
      try {
	BaseExt ext = config.getExt();
	switch(col) {
	case 2:
	  return ext;
	  
	case 3:
	  if(ext != null) 
	    return ext.getVersionID().toString();
	  else 
	    return null;

	case 4:
	  if(ext != null) 
	    return ext.getVendor();
	  else 
	    return null;

	default:
	  return null;
	}
      }
      catch(PipelineException ex) {
	return null;
      }

    case 5:
      return config.isEnabled();

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
    if(!pPrivilegeDetails.isMasterAdmin()) 
      return;

    int srow = pRowToIndex[row];
    BaseExtensionConfig config = pConfigs.get(srow); 
    switch(col) {
    case 5:
      config.setEnabled((Boolean) value);
      fireTableDataChanged();
      pModifiedIndices.add(srow);
      pParent.doEdited();
    }
  }

  

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
  private JManageServerExtensionsDialog pParent;

  /**
   * The privileges granted to the current user with respect to all other users.
   */ 
  protected PrivilegeDetails  pPrivilegeDetails; 

  /**
   * The names of the configurations.
   */ 
  protected ArrayList<BaseExtensionConfig>  pConfigs; 

  /**
   * The indice of modified configurations.
   */ 
  protected TreeSet<Integer>  pModifiedIndices; 
  
}
