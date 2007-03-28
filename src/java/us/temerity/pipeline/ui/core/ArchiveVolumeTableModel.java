// $Id: ArchiveVolumeTableModel.java,v 1.2 2007/03/28 20:07:15 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   A R C H I V E   V O L U M E   T A B L E   M O D E L                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A {@link SortableTableModel SortableTableModel} which contains archive volume information.
 */ 
public
class ArchiveVolumeTableModel
  extends AbstractSortableTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   */
  public 
  ArchiveVolumeTableModel() 
  {
    super();

    /* initialize the columns */ 
    { 
      pNumColumns = 5;

      {
	Class classes[] = { 
	  String.class, Long.class, Integer.class, Integer.class, Boolean.class
	};
	pColumnClasses = classes;
      }

      {
	String names[] = {
	  "Archive Volume", "Created", "Contains", "Restores", "Use" 
	};
	pColumnNames = names;
      }

      {
	String desc[] = {
	  "The name of the archive volume.", 
	  "When the archive volume was created.",
	  "The number of checked-in versions selected to be restored which are contained " + 
	  "in the archive volume.", 
	  "The number of checked-in version which will be restored from the archive " + 
	  "volume due to the current choice of volumes to restore.",
	  "Whether to use the archive volume to restore checked-in versions."
	};
	pColumnDescriptions = desc;
      }

      {
	int widths[] = { 240, 180, 80, 80, 80 };
	pColumnWidths = widths;
      }

      {
	TableCellRenderer renderers[] = {
	  new JSimpleTableCellRenderer(JLabel.CENTER), 
	  new JSimpleTableCellRenderer(JLabel.CENTER),
	  new JSimpleTableCellRenderer(JLabel.CENTER),
	  new JSimpleTableCellRenderer(JLabel.CENTER),
	  new JBooleanTableCellRenderer(JLabel.CENTER)
	};
	pRenderers = renderers;
      }

      {
	TableCellEditor editors[] = { 
	  null, null, null, null, null
	};
	pEditors = editors;
      }
    }

    pNames      = new ArrayList<String>();
    pVersionIDs = new ArrayList<VersionID>();

    pRestoreVersions = new TreeMap<String,TreeMap<String,TreeSet<VersionID>>>();

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
	break;
	
      case 2: 
	value = pContains[idx];
	break;

      case 3:
	value = pRestores[idx];
	
      case 4:
	value = pUseVolume[idx];
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


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the archive volumes chosen for use in the restore operation.
   */ 
  public TreeSet<String> 
  getChosenArchives()
  {
    TreeSet<String> chosen = new TreeSet<String>();

    int wk = 0;
    for(ArchiveVolume vol : pVolumes) {
      if(pUseVolume[wk] && (pRestores[wk] > 0))
	chosen.add(vol.getName());
      wk++;
    }

    return chosen;
  }

  /**
   * Choose the given archive volumes for use in the restore operation.
   */ 
  public void 
  setChosenArchives
  (
   TreeSet<String> names
  ) 
  {
    int wk = 0;
    for(ArchiveVolume vol : pVolumes) {
      pUseVolume[wk] = ((pIsUnique[wk]) || names.contains(vol.getName()));
      wk++;
    }    

    recomputeRestores();
  }

  /**
   * Get the number of checked-in versions which will be restored by all chosen 
   * archive volumes. 
   */ 
  public int
  getChosenCount() 
  {
    int cnt = 0;

    if(pUseVolume != null) {
      int wk;
      for(wk=0; wk<pUseVolume.length; wk++) {
	if(pUseVolume[wk]) 
	  cnt += pRestores[wk];
      }
    }

    return cnt;
  }

  /**
   * Get the fully resolved names and revision numbers of the checked-in versions to 
   * restore, indexed by the name of the archive volume to use.
   */ 
  public TreeMap<String,TreeMap<String,TreeSet<VersionID>>> 
  getRestoreVersions()
  {
    return pRestoreVersions;
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the table data.
   * 
   * @param contains
   *   The names of the archives containing the checked-in versions indexed by 
   *   fully resolved node name and revision number.
   * 
   * @param volumes
   *   The archive volumes.
   */ 
  public void
  setData
  (
   TreeMap<String,TreeMap<VersionID,TreeSet<String>>> contains, 
   Collection<ArchiveVolume> volumes
  ) 
  {
    pNames.clear();
    pVersionIDs.clear();
    pVolumes.clear();

    if((contains != null) && (volumes != null)) {
      TreeSet<String> unique = new TreeSet<String>();
      for(String name : contains.keySet()) {
	for(VersionID vid : contains.get(name).keySet()) {
	  pNames.add(name);
	  pVersionIDs.add(vid);
	  
	  TreeSet<String> anames = contains.get(name).get(vid);
	  if(anames.size() == 1) 
	    unique.add(anames.first());
	}
      }
      
      pVolumes.addAll(volumes);

      int size = pVolumes.size();
      pIsUnique   = new boolean[size];
      pContains   = new int[size];
      pRestores   = new int[size];
      pUseVolume  = new boolean[size];
      
      int idx = 0;
      for(ArchiveVolume volume : pVolumes) {
	if(unique.contains(volume.getName())) {
	  pIsUnique[idx]  = true; 
	  pUseVolume[idx] = true;
	}
	
	pContains[idx] = 0;
	int wk;
	for(wk=0; wk<pNames.size(); wk++) {
	  if(volume.contains(pNames.get(wk), pVersionIDs.get(wk)))
	    pContains[idx]++;
	}
	
	idx++;
      }
    }
    else {
      pContains   = new int[0];
      pRestores   = new int[0];
      pUseVolume  = new boolean[0];
    }
          
    recomputeRestores();
    sort();
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

  /**
   * Recompute the version restore counts based on the current contents of the table.
   */ 
  private void 
  recomputeRestores() 
  {
    {
      pRestoreVersions.clear();

      int wk;
      for(wk=0; wk<pRestores.length; wk++) 
	pRestores[wk] = 0;
    }

    TreeMap<Long,Integer> ordered = new TreeMap<Long,Integer>();
    {
      int wk = 0;
      for(ArchiveVolume volume : pVolumes) {
	ordered.put(volume.getTimeStamp(), wk);
	wk++;
      }
    }
    
    ArrayList<String> names = new ArrayList<String>(pNames);
    ArrayList<VersionID> vids = new ArrayList<VersionID>(pVersionIDs);
    
    for(Integer idx : ordered.values()) {
      if(pUseVolume[idx]) {
	ArchiveVolume volume = pVolumes.get(idx);

	ArrayList<String> rnames = new ArrayList<String>();
	ArrayList<VersionID> rvids = new ArrayList<VersionID>();
	
	int wk;
	for(wk=0; wk<names.size(); wk++) {
	  String name = names.get(wk);
	  VersionID vid = vids.get(wk);
	  
	  if(volume.contains(name, vid)) {
	    TreeMap<String,TreeSet<VersionID>> versions = 
	    pRestoreVersions.get(volume.getName());

	    if(versions == null) {
	      versions = new TreeMap<String,TreeSet<VersionID>>();
	      pRestoreVersions.put(volume.getName(), versions);
	    }
	    
	    TreeSet<VersionID> vvids = versions.get(name);
	    if(vvids == null) {
	      vvids = new TreeSet<VersionID>();
	      versions.put(name, vvids);
	    }
	    
	    vvids.add(vid);
	    
	    pRestores[idx]++;
	  }
	  else {
	    rnames.add(name);
	    rvids.add(vid);
	  }
	}
	
	if(rnames.isEmpty()) 
	  break;
	
	names = rnames; 
	vids  = rvids;
      }
    }

    {
      int wk;
      for(wk=0; wk<pRestores.length; wk++) 
	if((pRestores[wk] == 0) && !pIsUnique[wk]) 
	  pUseVolume[wk] = false;
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
      return TimeStamps.format(volume.getTimeStamp());

    case 2: 
      return pContains[irow];

    case 3:
      return pRestores[irow];
	
    case 4:
      return pUseVolume[irow];

    default:
      assert(false);
      return null;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7511122747425377008L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved node names of the checked-in versions to restore.
   */ 
  private ArrayList<String>  pNames; 

  /**
   * The revision numbers of the checked-in versions to restore.
   */ 
  private ArrayList<VersionID>  pVersionIDs; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether one or more of the selected checked-in versions is only contained in each 
   * archive volume;
   */ 
  private boolean[]  pIsUnique; 

  /**
   * The number of selected checked-in versions contained in each archive volume.
   */ 
  private int[]  pContains; 

  /**
   * The number of selected checked-in versions which will be restored by each archive 
   * volume. 
   */ 
  private int[]  pRestores;

  /**
   * The fully resolved node names and revision numbers to restore indexed by the 
   * name of the archive volume to use in the restore operation.
   */ 
  private TreeMap<String,TreeMap<String,TreeSet<VersionID>>>  pRestoreVersions;

  /**
   * Whether each archive volume should be used in the restore operation.
   */ 
  private boolean[]  pUseVolume;

  /**
   * The archive volumes.
   */ 
  private ArrayList<ArchiveVolume>  pVolumes; 
}
