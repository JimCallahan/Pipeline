// $Id: FileSeqTableModel.java,v 1.1 2009/08/19 23:42:47 jim Exp $

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
/*   F I L E   S E Q   T A B L E   M O D E L                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A {@link SortableTableModel} which contains the revision history for a file sequence.
 */ 
public
class FileSeqTableModel
  extends AbstractSortableTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   * 
   * @param parent
   *   The parent panel.
   * 
   * @param fseq
   *   The full working file sequence. 
   * 
   * @param isFrozen
   *   Whether the node is in a Frozen state.
   * 
   * @param isReadOnly
   *   Whether the working version is editable given its current state and the users 
   *   level of privileges.
   * 
   * @param vids
   *   The revision numbers (newest to oldest) of the versions to display. 
   * 
   * @param offline
   *   The revision numbers of the offline checked-in versions.
   * 
   * @param singles
   *   The single file sequences for all files to display.
   * 
   * @param enabled
   *   The single file sequences which have defined states.
   * 
   * @param fstates
   *   The file states of the working files.
   * 
   * @param finfos
   *   The per-file status information of the working files.
   * 
   * @param qstates
   *   The queue states of the working files.
   *
   * @param novel
   *   The per-version (newest to oldest) file novelty flags indexed by filename.
   */
  public 
  FileSeqTableModel
  (
   JFileSeqPanel parent, 
   FileSeq fseq, 
   boolean isFrozen, 
   boolean isReadOnly, 
   ArrayList<VersionID> vids,
   SortedSet<VersionID> offline, 
   ArrayList<FileSeq> singles,
   SortedSet<FileSeq> enabled, 
   TreeMap<FileSeq,FileState> fstates, 
   TreeMap<FileSeq,NativeFileInfo> finfos, 
   TreeMap<FileSeq,QueueState> qstates,
   SortedMap<FileSeq,Boolean[]> novel
  )
  {
    super();

    /* initialize the fields */ 
    {     
      pParent = parent; 

      pFileSeq    = fseq; 
      pIsFrozen   = isFrozen;
      pIsReadOnly = isReadOnly;

      pVersionIDs = vids.toArray(new VersionID[0]);
      pNumCols = pVersionIDs.length;

      {
        pIsOffline = new boolean[pNumCols];
        int col;
        for(col=0; col<pNumCols; col++) 
          pIsOffline[col] = offline.contains(pVersionIDs[col]); 
      }
      
      pSingles = singles.toArray(new FileSeq[0]);
      pNumRows = pSingles.length; 

      pSeqIndices         = new Integer[pNumRows];
      pIsEnabled          = new boolean[pNumRows];
      pFileStates         = new FileState[pNumRows];
      pFileInfos          = new NativeFileInfo[pNumRows];
      pQueueStates        = new QueueState[pNumRows];
      pIsNovel            = new Boolean[pNumRows][];
      pSelectedVersionIDs = new VersionID[pNumRows];
      pRowToIndex         = new int[pNumRows];

      TreeMap<Path,Integer> ptoi = new TreeMap<Path,Integer>();
      {
        int i = 0;
        for(Path path : fseq.getPaths()) {
          ptoi.put(path, i);
          i++;
        }
      }

      int row; 
      for(row=0; row<pNumRows; row++) {
        FileSeq sfseq = pSingles[row];

        pSeqIndices[row]     = ptoi.get(sfseq.getPath(0));
        pIsEnabled[row]      = enabled.contains(sfseq); 
        pFileStates[row]     = fstates.get(sfseq); 
        pFileInfos[row]      = finfos.get(sfseq); 
        pQueueStates[row]    = qstates.get(sfseq); 

        Boolean[] flags = novel.get(sfseq); 
        if(flags != null) {
          if(flags.length != pNumCols) 
            throw new IllegalArgumentException
              ("Somehow the number of file novelty flags (" + flags.length + ") was not " + 
               "the same as the number of versions (" + pNumCols + ") for the " + 
               "file (" + pSingles[row] + ") of file sequence (" + fseq + ")!"); 
          
          pIsNovel[row] = flags;
        }
      }
    }

    /* initialize the columns */ 
    {
      pStateRenderer = new JFileStateTableCellRenderer(this); 
      pNameRenderer  = new JFileNameTableCellRenderer(this);
      pCellRenderer  = new JFileNoveltyTableCellRenderer(this); 

      pCellEditor = new JFileNoveltyTableCellEditor(this); 
    }

    /* initial sorting */ 
    {
      pSortColumn = 1;
      sort(); 
    }
  }

 
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the single frame file sequence at the given row.
   */
  public FileSeq
  getFileSeq
  (
   int row
  ) 
  {
    return pSingles[pRowToIndex[row]];
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the single frame index into the working file sequence at the given row 
   * or <CODE>null</CODE> if outside the working file sequence.
   */
  public Integer
  getFileIndex
  (
   int row
  ) 
  {
    return pSeqIndices[pRowToIndex[row]];
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the single file sequence has a defined state.
   */
  public boolean 
  isEnabled
  (
   int row
  ) 
  {
    return pIsEnabled[pRowToIndex[row]];
  }


  /*----------------------------------------------------------------------------------------*/

  
  /**
   * Get the novelty of file version for a given cell. 
   * 
   * @return 
   *   True = IsNovel, False = NotNovel, Null = Nonexistent
   */
  public Boolean 
  getNovelty
  (
   int row, 
   int col
  ) 
  {
    return getNoveltyUnsorted(pRowToIndex[row], col); 
  }

  /**
   * Get the novelty of file version for a given cell. 
   * 
   * @return 
   *   True = IsNovel, False = NotNovel, Null = Nonexistent
   */
  private Boolean 
  getNoveltyUnsorted
  (
   int row, 
   int col
  ) 
  {
    if(col < 4) 
      return null;

    Boolean[] flags = pIsNovel[row];
    if(flags == null) 
      return null;
    return flags[col-4];
  }


  /**
   * Get the revision number of a file novelty column. 
   */
  public VersionID
  getNoveltyColumnVersion
  (
   int col
  ) 
  {
    return pVersionIDs[col-4];
  }  

  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the icon used to represent the novelty for a given cell.
   */
  public IconState
  getIconState
  (
   int row, 
   int col
  )
  {
    return getIconStateUnsorted(pRowToIndex[row], col); 
  }

  /**
   * Get the icon used to represent the novelty for a given cell.
   */
  private IconState
  getIconStateUnsorted
  (
   int row, 
   int col
  ) 
  {
    if(col < 4) 
      return null;

    int ncol = col-4;

    boolean isEnabled = pIsEnabled[row];
    boolean isOffline = pIsOffline[ncol]; 

    Boolean novel = getNoveltyUnsorted(row, col);
    boolean isMissing = (novel == null);

    boolean isExtLeft = false;
    if(!isMissing && (ncol > 0)) {
      Boolean n = getNoveltyUnsorted(row, col-1);
      if(n != null)
        isExtLeft = !n;
    }

    boolean isExtRight = false;
    boolean isOfflineRight = false;
    if(!isMissing && (ncol < (pNumCols-1))) {
      Boolean n = getNoveltyUnsorted(row, col+1);
      if(n != null) 
        isExtRight = !novel;

      isOfflineRight = pIsOffline[ncol+1];
    }

    boolean isCheck = (!isMissing && !isOffline && (novel || isOfflineRight));

    VersionID vid = pSelectedVersionIDs[row];
    boolean isPicked = pVersionIDs[ncol].equals(vid); 
    
    if(isMissing) {
      return IconState.Missing;
    }
    else if(isCheck) {
      if(isEnabled) {
        if(isPicked) {
          if(isExtLeft && isExtRight) 
            return IconState.CheckPickedExtBoth;
          else if(isExtLeft) 
            return IconState.CheckPickedExtLeft; 
          else if(isExtRight) 
            return IconState.CheckPickedExtRight; 
          else 
            return IconState.CheckPicked; 
        }
        else {
          if(isExtLeft && isExtRight) 
            return IconState.CheckExtBoth;
          else if(isExtLeft) 
            return IconState.CheckExtLeft; 
          else if(isExtRight) 
            return IconState.CheckExtRight; 
          else 
            return IconState.Check; 
        }
      }
      else {
        if(isExtLeft && isExtRight) 
          return IconState.CheckDisabledExtBoth;
        else if(isExtLeft) 
          return IconState.CheckDisabledExtLeft; 
        else if(isExtRight) 
          return IconState.CheckDisabledExtRight; 
        else 
          return IconState.CheckDisabled; 
      }
    }
    else if(isOffline) {
      if(isExtLeft && isExtRight) 
        return IconState.OfflineExtBoth;
      else if(isExtLeft) 
        return IconState.OfflineExtLeft; 
      else if(isExtRight) 
        return IconState.OfflineExtRight; 
      else 
        return IconState.Offline; 
    }
    else {
      if(isExtLeft) 
        return IconState.BarExtBoth; 
      else 
        return IconState.BarExtRight; 
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the revision number of the checked-in to revert the file at the given row.
   * 
   * @return 
   *   The revision number or <CODE>null</CODE> to leave working file as-is.
   */
  public VersionID
  getRevertVersionID
  (
   int row
  ) 
  {
    return pSelectedVersionIDs[pRowToIndex[row]];
  }

  /**
   * Get the names and revision numbers of the file selected for reversion. 
   */
  public TreeMap<String,VersionID> 
  getFilesToRevert()
  {
    TreeMap<String,VersionID> results = new TreeMap<String,VersionID>();

    int wk;
    for(wk=0; wk<pNumRows; wk++) {
      if(pIsEnabled[wk]) {
        VersionID vid = pSelectedVersionIDs[wk]; 
        if(vid != null) 
          results.put(pSingles[wk].getPath(0).toOsString(), vid);
      }
    }

    return results;
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
    int idx;
    for(idx=0; idx<pNumRows; idx++) {
      Comparable value = null;
      switch(pSortColumn) {
      case 0:
	{
          FileState fstate  = pFileStates[idx];
          QueueState qstate = pQueueStates[idx];
          value = ((qstate != null) ? qstate : "") + "-" + ((fstate != null) ? fstate : "");
	}
        break;

      case 1:
        {
          FileSeq fseq = pSingles[idx]; 
          if(fseq.hasFrameNumbers())
            value = fseq.getFrameRange().getStart(); 
        }
        break;

      case 2: 
        if (pFileInfos[idx] != null)
          value = pFileInfos[idx].getFileSize();
        break;

      case 3: 
        if (pFileInfos[idx] != null)
          value = pFileInfos[idx].getTimeStamp();
        break;

      default:
        value = getIconStateUnsorted(idx, pSortColumn); 
      }

      cells[idx] = new IndexValue(idx, value); 
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
   * Returns the most specific superclass for all the cell values in the column.
   */
  @Override
  public Class 	
  getColumnClass
  (
   int col
  )
  {
    switch(col) {
    case 0:
    case 1:
    case 2:
    case 3:
      return String.class;

    default:
      return Boolean.class;
    }
  }
  
  /**
   * Returns the number of columns in the model.
   */ 
  @Override
  public int
  getColumnCount()
  {
    return pNumCols+4;
  }

  /**
   * Returns the name of the column at columnIndex.
   */ 
  @Override
  public String 	
  getColumnName
  (
   int col
  ) 
  {
    switch(col) {
    case 0:
      return "";

    case 1:
      return "File Name";

    case 2: 
      return "File Size"; 
      
    case 3: 
      return "Modified"; 

    default:
      return pVersionIDs[col-4].toString();
    }
  }

  /**
   * Get the range of widths (min, preferred, max) of the column. 
   */
  @Override
  public Vector3i
  getColumnWidthRange
  (
   int col   
  )
  {
    switch(col) {
    case 0:
      return new Vector3i(25);

    case 1:
      return new Vector3i(80, 160, 800);

    case 2:
      return new Vector3i(80); 

    case 3:
      return new Vector3i(120); 

    default:
      return new Vector3i(70); 
    }
  }

  /**
   * Returns the color prefix used to determine the synth style of the header button for 
   * the given column.
   */ 
  @Override
  public String 	
  getColumnColorPrefix
  (
   int col
  )
  {
    return "";
  }

  /**
   * Returns the description of the column columnIndex used in tool tips.
   */ 
  @Override
  public String 	
  getColumnDescription
  (
   int col
  ) 
  {
    switch(col) {
    case 0:
      return ("File/Queue states for each file in the sequence.");

    case 1:
      return ("Names of each file in the sequence."); 

    case 2:
      return ("Sizes (in bytes) of each file in the sequence."); 

    case 3:
      return ("When each file in the sequence was last modified."); 

    default:
      return ("Files associated with version (" + pVersionIDs[col-4] + ") of node."); 
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the renderer for the given column. 
   */ 
  @Override
  public TableCellRenderer
  getRenderer
  (
   int col   
  )
  {
    switch(col) {
    case 0:
      return pStateRenderer; 

    case 1:
    case 2:
    case 3: 
      return pNameRenderer;

    default:
      return pCellRenderer;
    }
  }

  /**
   * Get the editor for the given column. 
   */ 
  @Override
  public TableCellEditor
  getEditor
  (
   int col   
  )
  {
    if(col < 4) 
      return null;
    return pCellEditor;
  }


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
    if(col < 4) 
      return false;

    int ncol = col-4;

    if(pIsReadOnly)
      return false;
    
    int vrow = pRowToIndex[row];
    if(!pIsEnabled[vrow]) 
      return false; 

    boolean isOffline = pIsOffline[ncol]; 
    Boolean novel = pIsNovel[vrow][ncol];
    boolean isMissing = (novel == null);

    boolean isOfflineRight = false;
    if(!isMissing && (ncol < (pNumCols-1))) 
      isOfflineRight = pIsOffline[ncol+1];

    return (!isMissing && !isOffline && (novel || isOfflineRight));
  }

  /**
   * Returns the value for the cell at columnIndex and rowIndex.
   */ 
  @Override
  public Object 	
  getValueAt
  (
   int row, 
   int col
  )
  {
    int vrow = pRowToIndex[row];
    switch(col) {
    case 0:
      {
        boolean isSelectable = false;
	String prefix = "Blank-";
        FileState fstate = pFileStates[vrow];
        QueueState qstate = pQueueStates[vrow];
	if((fstate != null) && (qstate != null)) {
	  prefix = (fstate + "-" + qstate + (pIsFrozen ? "-Frozen-" : "-"));
	  isSelectable = (fstate != FileState.CheckedIn);
	}
        else if(pIsEnabled[vrow]) {
          prefix = "Lightweight-";
          isSelectable = true;
        }

        return prefix;
      }

    case 1:
      {
        String str = "-";
        if(pSingles[vrow] != null) 
          str = pSingles[vrow].getPath(0).toString(); 
        return str;
      }

    case 2:
      {
        NativeFileInfo info = pFileInfos[vrow];
        if(info != null) {
          long size = info.getFileSize();
          if(info.isSymlink()) 
            return ("[" + ByteSize.longToFloatString(size) + "]"); 
          else
            return ByteSize.longToFloatString(size); 
        }
        else {
          return "-";
        }
      }

    case 3:
      {
        NativeFileInfo info = pFileInfos[vrow];
        if(info != null) 
          return TimeStamps.formatHumanRelative(info.getTimeStamp()); 
        else 
          return "-";
      }

    default:
      return pVersionIDs[col-4].equals(pSelectedVersionIDs[vrow]);
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
    int vrow = pRowToIndex[row];
    boolean edited = setValueAtHelper(value, vrow, col); 

    {
      int[] selected = pTable.getSelectedRows(); 
      int wk;
      for(wk=0; wk<selected.length; wk++) {
	int srow = pRowToIndex[selected[wk]];
	if(srow != vrow)
	  if (setValueAtHelper(value, srow, col))
	    edited = true;
      }
    }
      
    if(edited) {
      pParent.unsavedChange("Revert Selected Files"); 
      fireTableDataChanged();
    }
  }

  /**
   * Sets the value in the cell at columnIndex and rowIndex to the given value.
   */ 
  public boolean 
  setValueAtHelper
  (
   Object value, 
   int srow, 
   int col
  ) 
  {  
    if(col < 4) 
      return false;

    boolean edited = false;
    Boolean isSelected = (Boolean) value;
    if(isSelected != null) {
      if(isSelected) {
        int ck = col;
        while(ck < pNumCols+4) {
          Boolean novel = getNoveltyUnsorted(srow, ck); 
          if(novel == null) 
            return false; 

          if(novel || ((ck+1 < pNumCols+4) && pIsOffline[ck-4+1])) {
            pSelectedVersionIDs[srow] = pVersionIDs[ck-4];
            return true; 
          }

          ck++;
        }
      }
      else {
        pSelectedVersionIDs[srow] = null;
        return true;
      }
    }
    
    return false;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  public enum
  IconState
  {
    Check, 
    CheckExtLeft, 
    CheckExtRight, 
    CheckExtBoth, 

    CheckPicked, 
    CheckPickedExtLeft, 
    CheckPickedExtRight, 
    CheckPickedExtBoth, 

    CheckDisabled, 
    CheckDisabledExtLeft, 
    CheckDisabledExtRight, 
    CheckDisabledExtBoth, 

    Offline, 
    OfflineExtLeft, 
    OfflineExtRight, 
    OfflineExtBoth, 

    BarExtRight, 
    BarExtBoth, 

    Missing;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3072881670853766514L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent panel.
   */ 
  private JFileSeqPanel pParent; 
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * The full working file sequence. 
   */ 
  private FileSeq  pFileSeq; 

  /**
   * Whether the node is in a Frozen state.
   */ 
  private boolean pIsFrozen;

  /**
   * Whether the working version is editable given its current state and the users 
   * level of privileges.
   */ 
  private boolean pIsReadOnly; 

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * The number of columns in the table.
   */ 
  private int pNumCols; 

  /**
   * The revision numbers of the checked-in versions (newest to oldest) for each column.
   */ 
  private VersionID  pVersionIDs[]; 

  /**
   * Whether each checked-in version is offline for each column.
   */ 
  private boolean  pIsOffline[]; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The file (as a single file sequence) for each row.
   */ 
  private FileSeq  pSingles[]; 

  /**
   * The file index in the working file sequence of each row 
   * or <CODE>null</CODE> if the rows single file is outside the working file sequence.
   */ 
  private Integer  pSeqIndices[]; 

  /**
   * Whether each row (file sequence) can be selected for a file revert operation.
   */ 
  private boolean  pIsEnabled[]; 

  /**
   * The file states of each row (may be null).
   */ 
  private FileState  pFileStates[]; 

  /**
   * The file status information of each row (may be null).
   */ 
  private NativeFileInfo  pFileInfos[]; 

  /**
   * The queue states of each row (may be null).
   */ 
  private QueueState  pQueueStates[]; 

  /**
   * The revision number of the version selected for a file revert operation for each row
   * or <CODE>null</CODE> if none is selected.
   */ 
  private VersionID  pSelectedVersionIDs[];


  /*----------------------------------------------------------------------------------------*/

  /**
   * The file novelty state for each cell indexed as (row, col): 
   *   true = IsNovel, false = NotNovel, null = Nonexistent
   */ 
  private Boolean  pIsNovel[][];


  /*----------------------------------------------------------------------------------------*/

  /**
   * The shared renderers for all cells in the table.
   */ 
  private TableCellRenderer pStateRenderer;    
  private TableCellRenderer pNameRenderer;             
  private TableCellRenderer pCellRenderer; 

  /**
   * The shared editors for all cells in the table.
   */ 
  private TableCellEditor pCellEditor; 

}
