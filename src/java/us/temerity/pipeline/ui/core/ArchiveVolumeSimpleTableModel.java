// $Id: ArchiveVolumeSimpleTableModel.java,v 1.1 2005/04/03 01:54:23 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   A R C H I V E   V O L U M E   S I M P L E   T A B L E   M O D E L                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A {@link SortableTableModel SortableTableModel} which contains archive volume information.
 */ 
public
class ArchiveVolumeSimpleTableModel
  extends AbstractSortableTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   */
  public 
  ArchiveVolumeSimpleTableModel() 
  {
    super();

    /* initialize the columns */ 
    { 
      pNumColumns = 2;

      {
	Class classes[] = { 
	  String.class, Long.class, 
	};
	pColumnClasses = classes;
      }

      {
	String names[] = {
	  "Archive Volume", "Created"
	};
	pColumnNames = names;
      }

      {
	String desc[] = {
	  "The name of the archive volume.", 
	  "When the archive volume was created.",
	};
	pColumnDescriptions = desc;
      }

      {
	int widths[] = { 240, 180 };
	pColumnWidths = widths;
      }

      {
	TableCellRenderer renderers[] = {
	  new JSimpleTableCellRenderer(JLabel.CENTER), 
	  new JSimpleTableCellRenderer(JLabel.CENTER)
	};
	pRenderers = renderers;
      }

      {
	TableCellEditor editors[] = { 
	  null, null
	};
	pEditors = editors;
      }
    }

    pVolumes = new ArrayList<ArchiveVolume>();
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
    Comparable value = null;

    int idx = 0;
    for(ArchiveVolume volume : pVolumes) {
      switch(pSortColumn) {
      case 0:
	value = volume.getName();
	break;
	
      case 1:
	value = volume.getTimeStamp();
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
   * Set the table data.
   * 
   * @param volumes
   *   The archive volumes.
   */ 
  public void
  setData
  (
   Collection<ArchiveVolume> volumes
  ) 
  {
    pVolumes.clear();

    if(volumes != null) 
      pVolumes.addAll(volumes);

    sort();
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the archive volume at the given row.
   */
  public String 
  getName
  (
   int row
  ) 
  {
    ArchiveVolume vol = pVolumes.get(pRowToIndex[row]);
    if(vol != null) 
      return vol.getName();
    return null;
  }


  /**
   * Get the archive volumes on the given rows.
   */ 
  public ArrayList<ArchiveVolume> 
  getArchiveVolumes
  (
   int[] rows
  )
  {
    ArrayList<ArchiveVolume> volumes = new ArrayList<ArchiveVolume>();

    int wk;
    for(wk=0; wk<rows.length; wk++) {
      int idx = pRowToIndex[rows[wk]];
      volumes.add(pVolumes.get(idx));
    }

    return volumes;
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
    return pVolumes.size();
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
    int irow = pRowToIndex[row];
    ArchiveVolume volume = pVolumes.get(irow);
    switch(col) {
    case 0:
      return volume.getName();
      
    case 1:
      return Dates.format(volume.getTimeStamp());

    default:
      assert(false);
      return null;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6305320064435263503L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The archive volumes.
   */ 
  private ArrayList<ArchiveVolume>  pVolumes; 
}
