// $Id: LicenseKeysTableModel.java,v 1.8 2009/08/19 23:42:47 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.ui.*;

import java.util.*;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;


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

    /* initialize the fields */ 
    {
      pParent = parent;

      pPrivilegeDetails = new PrivilegeDetails();   
    }

    /* initialize the columns */ 
    { 
      pNumColumns = 10;

      {
	Class classes[] = { 
	  String.class, String.class, String.class,
	  Integer.class, Integer.class, Integer.class, Integer.class,
	  BaseKeyChooser.class, String.class, String.class
	}; 
	pColumnClasses = classes;
      }

      {
	String names[] = {
	  "Key Name", "Description", "Scheme", 
	  "Available", "Max Slots", "Max Hosts", "Max Host Slots",
	  "Plugin", "Version", "Vendor"
	};
	pColumnNames = names;
      }

      {
	String colors[] = { 
          "", "", "Blue", 
          "Green", "Green", "Green", "Green", 
          "Purple", "Purple", "Purple" 
        };
	pColumnColorPrefix = colors; 
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
	  "single host.",
	  "The name of the KeyChooser plugin for this key.",
          "The revision number of the KeyChooser plugin.", 
          "The name of the KeyChooser plugin vendor."
	};
	pColumnDescriptions = desc;
      }

      {
        Vector3i ranges[] = {
          new Vector3i(140), 
          new Vector3i(180, 360, Integer.MAX_VALUE), 
          new Vector3i(120), 
          new Vector3i(120), 
          new Vector3i(120), 
          new Vector3i(120), 
          new Vector3i(120), 
          new Vector3i(120), 
          new Vector3i(120), 
          new Vector3i(120)
        };
        pColumnWidthRanges = ranges;
      }

      {
	TableCellRenderer renderers[] = {
	  new JSimpleTableCellRenderer(SwingConstants.CENTER), 
	  new JSimpleTableCellRenderer(SwingConstants.LEFT), 
	  new JSimpleTableCellRenderer("Blue", SwingConstants.CENTER),
	  new JSimpleTableCellRenderer("Green", SwingConstants.CENTER, true),
	  new JSimpleTableCellRenderer("Green", SwingConstants.CENTER, true),
	  new JSimpleTableCellRenderer("Green", SwingConstants.CENTER, true),
	  new JSimpleTableCellRenderer("Green", SwingConstants.CENTER, true),
          new JPluginTableCellRenderer(SwingConstants.CENTER), 
          new JSimpleTableCellRenderer("Purple", SwingConstants.CENTER), 
          new JSimpleTableCellRenderer("Purple", SwingConstants.CENTER)
	};
	pRenderers = renderers;
      }

      {
        
        JCollectionTableCellEditor scheme = 
          new JCollectionTableCellEditor(LicenseScheme.titles(), 120); 
        scheme.setSynthPrefix("Blue"); 

        JIntegerTableCellEditor counts = 
          new JIntegerTableCellEditor(120, SwingConstants.CENTER);
        counts.setName("GreenEditableTextField");

	TableCellEditor editors[] = {
	  null, null, scheme, 
	  null, counts, counts, counts, 
          null, null, null
	};
	pEditors = editors;
      }
    }

    pLicenseKeys = new ArrayList<LicenseKey>();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
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
   * 
   * @param keys
   *   The license key names.
   * 
   * @param privileges
   *   The details of the administrative privileges granted to the current user. 
   */ 
  public void
  setLicenseKeys
  (
   ArrayList<LicenseKey> keys, 
   PrivilegeDetails privileges
  ) 
  {
    pLicenseKeys.clear();
    if(keys != null) 
      pLicenseKeys.addAll(keys);

    pNumRows = pLicenseKeys.size();

    pPrivilegeDetails = privileges; 

    sort();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S O R T I N G                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Sort the rows by the values in the current sort column and direction.
   */ 
  @Override
  public void 
  sort()
  {
    IndexValue cells[] = new IndexValue[pNumRows]; 
    int idx = 0;
    for(LicenseKey key : pLicenseKeys) {
      Comparable value = null;
      BaseKeyChooser plug = key.getKeyChooser();
      switch(pSortColumn) {
      case 0:
	value = key.getName();
	break;

      case 1:
	value = key.getDescription(); 
	break;

      case 2:
	value = key.getScheme().toTitle();
	break;

      case 3:
	value = new Integer(key.getAvailable());
	break;

      case 4:
	value = key.getMaxSlots();
	break;

      case 5:
	value = key.getMaxHosts();
	break;

      case 6:
	value = key.getMaxHostSlots();
	break;

      case 7:
        if(plug != null) 
          value = plug.getName();
        break;

      case 8:
        if(plug != null) 
          value = plug.getVersionID().toString();
        break;
        
      case 9:
        if(plug != null) 
          value = plug.getVendor();
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
  @Override
  public boolean 	
  isCellEditable
  (
   int row, 
   int col
  ) 
  {
    if(!pPrivilegeDetails.isQueueAdmin()) 
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
    BaseKeyChooser plug = key.getKeyChooser();
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

    case 7:
      return plug;
      
    case 8:
      if (plug == null)
        return "-";
      else
        return plug.getVersionID().toString();
      
    case 9:
      if (plug == null)
        return "-";
      else
        return plug.getVendor();


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
  
  /**
   * Return the Key Chooser for the given row.
   */ 
  public BaseKeyChooser 
  getKeyChooser
  (
    int row  
  )
  {
    BaseKey key = pLicenseKeys.get(pRowToIndex[row]);
    BaseKeyChooser plug = key.getKeyChooser();
    return plug;
  }
  
  /**
   * Return the Key for the given row.
   */ 
  public LicenseKey
  getKey
  (
    int row  
  )
  {
    return pLicenseKeys.get(pRowToIndex[row]);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8973197792150585816L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent dialog.
   */ 
  private JManageLicenseKeysDialog  pParent;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The details of the administrative privileges granted to the current user. 
   */ 
  private PrivilegeDetails  pPrivilegeDetails; 

  /**
   * The underlying set of editors.
   */ 
  private ArrayList<LicenseKey> pLicenseKeys;

}
