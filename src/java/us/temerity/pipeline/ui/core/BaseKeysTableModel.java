// $Id: BaseKeysTableModel.java,v 1.2 2007/12/16 12:22:09 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.util.ArrayList;

import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import us.temerity.pipeline.BaseKey;
import us.temerity.pipeline.BaseKeyChooser;
import us.temerity.pipeline.ui.*;

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
  BaseKeysTableModel()
  {
    super();

    /* initialize the columns */ 
    { 
      pNumColumns = 5;

      {
        Class classes[] = { 
          String.class, String.class, BaseKeyChooser.class, String.class, String.class  }; 
        pColumnClasses = classes;
      }

      {
        String names[] = {"Key", "Description", "Plugin", "Version", "Vendor"  };
        pColumnNames = names;
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
        int widths[] = { 120, 600, 120, 120, 120 };
        pColumnWidths = widths;
      }

      {
        TableCellRenderer renderers[] = {
          new JSimpleTableCellRenderer(SwingConstants.CENTER), 
          new JSimpleTableCellRenderer(SwingConstants.LEFT),
          new JPluginTableCellRenderer(SwingConstants.CENTER), 
          new JSimpleTableCellRenderer(SwingConstants.CENTER), 
          new JSimpleTableCellRenderer(SwingConstants.CENTER)
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
    ArrayList<Comparable> values = new ArrayList<Comparable>();
    ArrayList<Integer> indices = new ArrayList<Integer>();
    int idx = 0;
    for(BaseKey key : pKeys) {
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
        {
          BaseKeyChooser plug = key.getKeyChooser();
          if (plug == null)
            value = "-";
          else
            value = plug.getName();
          break;
        }
        
      case 3:
        {
          BaseKeyChooser plug = key.getKeyChooser();
          if (plug == null)
            value = "-";
          else
            value = plug.getVersionID().toString();
          break;
        }
      
      case 4:
        {
          BaseKeyChooser plug = key.getKeyChooser();
          if (plug == null)
            value = "-";
          else
            value = plug.getVendor();
          break;
        }
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
    pKeys.addAll(keys);

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
    return pKeys.size();
  }

  /**
   * Returns true if the cell at rowIndex and columnIndex is editable.
   */ 
  @Override
  public boolean        
  isCellEditable
  (
   @SuppressWarnings("unused")
   int row,
   @SuppressWarnings("unused")
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
