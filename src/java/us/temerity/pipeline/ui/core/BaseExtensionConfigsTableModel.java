// $Id: BaseExtensionConfigsTableModel.java,v 1.2 2009/08/19 23:42:47 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
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
	  "Config Name", "Toolset", "Extension", "Version", "Vendor", "Enabled" 
	}; 
	pColumnNames = names;
      }

      {
	String colors[] = { "", "", "Purple", "Purple", "Purple", "" };
	pColumnColorPrefix = colors; 
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
        Vector3i ranges[] = {
          new Vector3i(180), 
          new Vector3i(120), 
          new Vector3i(180), 
          new Vector3i(120), 
          new Vector3i(120), 
          new Vector3i(120)
        };
        pColumnWidthRanges = ranges;
      }

      {
	TableCellRenderer renderers[] = {
	  new JSimpleTableCellRenderer(JLabel.CENTER), 
	  new JSimpleTableCellRenderer(JLabel.CENTER), 
	  new JPluginTableCellRenderer(JLabel.CENTER), 
	  new JSimpleTableCellRenderer("Purple", JLabel.CENTER), 
	  new JSimpleTableCellRenderer("Purple", JLabel.CENTER), 
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
    IndexValue cells[] = new IndexValue[pNumRows]; 
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
	      value = ext.getVersionID();
	      break;

	    case 4:
	      value = ext.getVendor();
	    }
	  }
	}
	catch(PipelineException ex) {
	  value = null;
	}
	break;
	
      case 5:
	value = config.isEnabled();
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
  /*   T A B L E   M O D E L   O V E R R I D E S                                            */
  /*----------------------------------------------------------------------------------------*/

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
