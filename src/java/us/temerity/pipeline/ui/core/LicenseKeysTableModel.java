// $Id: LicenseKeysTableModel.java,v 1.5 2005/05/31 09:37:45 jim Exp $

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
/*   L I C E N S E   K E Y S   T A B L E   M O D E L                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A {@link SortableTableModel SortableTableModel} which contains a set of 
 * {@link LicenseKey LicenseKey} instances.
 */ 
public
class LicenseKeysTableModel
  extends AbstractSortableTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   */
  public 
  LicenseKeysTableModel
  (
   JManageLicenseKeysDialog parent
  ) 
  {
    super();
    
    pParent = parent;

    /* initialize the columns */ 
    { 
      pNumColumns = 7;

      {
	Class classes[] = { 
	  String.class, String.class, String.class,
	  Integer.class, Integer.class, Integer.class, Integer.class 
	}; 
	pColumnClasses = classes;
      }

      {
	String names[] = {
	  "Key", "Description", "Scheme", 
	  "Available", "Max Slots", "Max Hosts", "Max Host Slots"
	};
	pColumnNames = names;
      }

      {
	String desc[] = {
	  "The name of the license key.", 
	  "A short description of the use of the key.", 
	  "The scheme used to determine the number of available licenses.", 
	  "The number of available license keys.", 
	  "The maximum number of slots running a job which requires the license key.", 
	  "The maximum number of hosts which may run a job which requires the license key.", 
	  "The maximum number of slots which may run a job requiring the license key on a" + 
	  "single host."
	};
	pColumnDescriptions = desc;
      }

      {
	int widths[] = { 
	  120, 360, 120, 
	  120, 120, 120, 120
	};
	pColumnWidths = widths;
      }

      {
	TableCellRenderer renderers[] = {
	  new JSimpleTableCellRenderer(JLabel.CENTER), 
	  new JSimpleTableCellRenderer(JLabel.LEFT), 
	  new JSimpleTableCellRenderer(JLabel.CENTER),
	  new JSimpleTableCellRenderer(JLabel.CENTER),
	  new JSimpleTableCellRenderer(JLabel.CENTER),
	  new JSimpleTableCellRenderer(JLabel.CENTER),
	  new JSimpleTableCellRenderer(JLabel.CENTER)
	};
	pRenderers = renderers;
      }

      {
	TableCellEditor editors[] = {
	  null, 
	  null, 
	  new JCollectionTableCellEditor(LicenseScheme.titles(), 120),
	  null,
	  new JIntegerTableCellEditor(120, JLabel.CENTER),
	  new JIntegerTableCellEditor(120, JLabel.CENTER),
	  new JIntegerTableCellEditor(120, JLabel.CENTER)
	};
	pEditors = editors;
      }
    }

    pLicenseKeys = new ArrayList<LicenseKey>();
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
    for(LicenseKey key : pLicenseKeys) {
      Comparable value = null;
      switch(pSortColumn) {
      case 0:
	value = key.getName();
	break;

      case 1:
	value = key.getDescription(); 
	if(value == null) 
	  value = "";
	break;

      case 2:
	value = key.getScheme().toTitle();
	break;

      case 3:
	value = new Integer(key.getAvailable());
	break;

      case 4:
	value = key.getMaxSlots();
	if(value == null)
	  value = "";
	break;

      case 5:
	value = key.getMaxHosts();
	if(value == null)
	  value = "";
	break;

      case 6:
	value = key.getMaxHostSlots();
	if(value == null)
	  value = "";
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
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the name of license key at the given row.
   */
  public String 
  getName
  (
   int row
  ) 
  {
    LicenseKey key = pLicenseKeys.get(pRowToIndex[row]);
    if(key != null) 
      return key.getName();
    return null;
  }

  /**
   * Get license key at the given row.
   */
  public LicenseKey
  getLicenseKey
  (
   int row
  ) 
  {
    return pLicenseKeys.get(pRowToIndex[row]);
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the underlying set of editors.
   */ 
  public void
  setLicenseKeys
  (
   ArrayList<LicenseKey> keys, 
   boolean isPrivileged
  ) 
  {
    pLicenseKeys.clear();
    pLicenseKeys.addAll(keys);

    pIsPrivileged = isPrivileged;

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
    return pLicenseKeys.size();
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
    if(!pIsPrivileged) 
      return false;

    if(col == 2) 
      return true;

    boolean isEditable = false;
    LicenseKey key = pLicenseKeys.get(pRowToIndex[row]);
    switch(key.getScheme()) {
    case PerSlot:
      isEditable = (col == 4);
      break;
      
    case PerHost:
      isEditable = (col == 5);
      break;

    case PerHostSlot:
      isEditable = ((col == 5) || (col == 6));
      break;
    }

    return isEditable;
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
    LicenseKey key = pLicenseKeys.get(pRowToIndex[row]);
    switch(col) {
    case 0:
      return key.getName();

    case 1:
      return key.getDescription(); 

    case 2:
      return key.getScheme().toTitle();

    case 3:
      return key.getAvailable();

    case 4:
      return key.getMaxSlots();

    case 5:
      return key.getMaxHosts();

    case 6:
      return key.getMaxHostSlots();

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
    boolean edited = false;

    LicenseKey key = pLicenseKeys.get(pRowToIndex[row]);
    switch(col) {
    case 2:
      {
	LicenseScheme oldScheme = key.getScheme();

	LicenseScheme newScheme = null;
	{
	  String str = (String) value;
	  if(str.equals("Per Slot")) 
	    newScheme = LicenseScheme.PerSlot;
	  else if(str.equals("Per Host"))
	    newScheme = LicenseScheme.PerHost; 
	  else if(str.equals("Per Host Slot")) 
	    newScheme = LicenseScheme.PerHostSlot;
	}

	if((newScheme != null) && !oldScheme.equals(newScheme)) {
	  Integer oldMaxHosts = null;
	  switch(newScheme) {
	  case PerHost:
	  case PerHostSlot:
	    switch(oldScheme) {
	    case PerHost:
	    case PerHostSlot:
	      oldMaxHosts = key.getMaxHosts();
	    }
	  }

	  key.setScheme(newScheme);
	  if(oldMaxHosts != null) 
	    key.setMaxHosts(oldMaxHosts);
	  edited = true;
	}
      }
      break;

    case 4:
      switch(key.getScheme()) {
      case PerSlot:
	{
	  Integer maxSlots = (Integer) value;
	  if((maxSlots != null) && (maxSlots >= 0)) 
	    key.setMaxSlots(maxSlots);
	  edited = true;
	}
      }
      break;

    case 5:
      switch(key.getScheme()) {
      case PerHost:
      case PerHostSlot:
	{
	  Integer maxHosts = (Integer) value;
	  if((maxHosts != null) && (maxHosts >= 0)) 
	    key.setMaxHosts(maxHosts);
	  edited = true;
	}
      }
      break;

    case 6:
      switch(key.getScheme()) {
      case PerHostSlot:
	{
	  Integer maxHostSlots = (Integer) value;
	  if((maxHostSlots != null) && (maxHostSlots >= 0)) 
	    key.setMaxHostSlots(maxHostSlots);
	  edited = true;
	}
      }
      break;
      
    default:
      assert(false);
    }  

    if(edited) {
      fireTableDataChanged();
      pParent.doEdited(); 
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8973197792150585816L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Does the current user have privileged status?
   */ 
  private boolean  pIsPrivileged;

  /**
   * The underlying set of editors.
   */ 
  private ArrayList<LicenseKey> pLicenseKeys;

  /**
   * The parent dialog.
   */ 
  private JManageLicenseKeysDialog  pParent;

}
