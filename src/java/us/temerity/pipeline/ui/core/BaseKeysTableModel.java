// $Id: BaseKeysTableModel.java,v 1.3 2009/08/19 23:42:47 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.ui.*;
import us.temerity.pipeline.math.*;

import java.util.*;
import javax.swing.*;
import javax.swing.table.*; 


/*------------------------------------------------------------------------------------------*/
/*   B A S E   K E Y S   T A B L E   M O D E L                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A {@link SortableTableModel SortableTableModel} which contains a set of 
 * {@link BaseKey} instances.
 */ 
public
class BaseKeysTableModel
  extends AbstractSortableTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   */
  public 
  BaseKeysTableModel
  (
   int descrWidth
  )
  {
    super();

    /* initialize the columns */ 
    { 
      pNumColumns = 5;

      {
        Class classes[] = { 
          String.class, String.class, BaseKeyChooser.class, String.class, String.class 
        }; 
        pColumnClasses = classes;
      }

      {
        String names[] = { "Key Name", "Description", "Plugin", "Version", "Vendor" };
        pColumnNames = names;
      }

      {
	String colors[] = { "", "", "Purple", "Purple", "Purple" };
	pColumnColorPrefix = colors; 
      }
      
      {
        String desc[] = {
          "The name of the selection key.", 
          "A short description of the use of the key.", 
          "The name of the KeyChooser plugin for this key.",
          "The revision number of the KeyChooser plugin.", 
          "The name of the KeyChooser plugin vendor."
        };
        pColumnDescriptions = desc;
      }

      {
        Vector3i ranges[] = {
          new Vector3i(140), 
          new Vector3i(180, descrWidth, Integer.MAX_VALUE), 
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
          new JSimpleTableCellRenderer("Purple", JLabel.CENTER), 
          new JSimpleTableCellRenderer("Purple", JLabel.CENTER)
        };
        pRenderers = renderers;
      }

      {
        TableCellEditor editors[] = {
          null, 
          null,
          null,
          null,
          null
        };
        pEditors = editors;
      }
    }

    pKeys = new ArrayList<BaseKey>();
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
    for(BaseKey key : pKeys) {
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
        if(plug != null)
          value = plug.getName();
        break;

      case 3:
        if(plug != null)
          value = plug.getVersionID();
        break;
      
      case 4:
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
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of key at the given row.
   */
  public String 
  getName
  (
   int row
  ) 
  {
    BaseKey key = pKeys.get(pRowToIndex[row]);
    if(key != null) 
      return key.getName();
    return null;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the underlying set of keys.
   */ 
  public void
  setKeys
  (
   ArrayList<BaseKey> keys
  ) 
  {
    pKeys.clear();
    if(keys != null) 
      pKeys.addAll(keys);

    pNumRows = pKeys.size();

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
    BaseKey key = pKeys.get(pRowToIndex[row]);
    BaseKeyChooser plug = key.getKeyChooser();
    switch(col) {
    case 0:
      return key.getName();

    case 1:
      return key.getDescription(); 
      
    case 2:
      return plug;
      
    case 3:
      if (plug == null)
        return "-";
      else
        return plug.getVersionID().toString();
      
    case 4:
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
   * Return the Key Chooser for the given row.
   */ 
  public BaseKeyChooser 
  getKeyChooser
  (
    int row  
  )
  {
    BaseKey key = pKeys.get(pRowToIndex[row]);
    BaseKeyChooser plug = key.getKeyChooser();
    return plug;
  }
  
  /**
   * Return the Key for the given row.
   */ 
  public BaseKey
  getKey
  (
    int row  
  )
  {
    return pKeys.get(pRowToIndex[row]);
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2622849154242207456L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The underlying set of editors.
   */ 
  private ArrayList<BaseKey> pKeys;


}
