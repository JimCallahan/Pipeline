// $Id: ArchiveVolumeSimpleTableModel.java,v 1.3 2009/08/19 23:42:47 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
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
        Vector3i ranges[] = {
          new Vector3i(180, 240, Integer.MAX_VALUE), 
          new Vector3i(180)
        };
        pColumnWidthRanges = ranges;
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
  /*   A C C E S S                                                                          */
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

    pNumRows = pVolumes.size(); 

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
    for(ArchiveVolume volume : pVolumes) {
      Comparable value = null;
      switch(pSortColumn) {
      case 0:
	value = volume.getName();
	break;
	
      case 1:
	value = volume.getTimeStamp();
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
      return TimeStamps.format(volume.getTimeStamp());

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
