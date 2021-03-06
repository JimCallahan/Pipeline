// $Id: SourceParamsTableModel.java,v 1.9 2009/08/19 23:42:47 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.ui.*;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   S O U R C E   P A R A M S   T A B L E   M O D E L                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A {@link SortableTableModel SortableTableModel} which contains the per-source action 
 * parameters associated with a node.
 */ 
public
class SourceParamsTableModel
  extends AbstractSortableTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   * 
   * @param parent
   *   The parent dialog.
   * 
   * @param isEditable
   *   Should the table allow editing of parameter values?
   * 
   * @param snames
   *   The fully resolved node name of the parent upstream node for each file sequence.
   * 
   * @param stitles
   *   The short name of the parent upstream node for each file sequence.
   * 
   * @param fseq
   *   The file sequences of the upstream nodes.  Entries which are <CODE>null</CODE> are
   *   primary file sequences.
   * 
   * @param action
   *   The parent action of the per-source parameters.
   */
  public 
  SourceParamsTableModel
  (
   JDialog parent, 
   boolean isEditable, 
   ArrayList<String> snames, 
   ArrayList<String> stitles, 
   ArrayList<FileSeq> fseqs, 
   BaseAction action  
  ) 
  {
    super();

    pIsEditable = isEditable;

    if(action == null) 
      throw new IllegalArgumentException("The action cannot be (null)!");
    pAction = action;

    if(!pAction.supportsSourceParams()) 
      throw new IllegalArgumentException("The action does not support source parameters!");

    /* initialize the columns */ 
    { 
      TreeMap<String,ActionParam> params = pAction.getInitialSourceParams();
      if(params == null) 
        throw new IllegalArgumentException
          ("The Action (" + action.getName() + ") claims to support source parameters, " + 
           "but does not actually create any source parameters."); 

      {
	pNumColumns = params.size() + 2;

	pColumnClasses      = new Class[pNumColumns];
	pColumnNames        = new String[pNumColumns];
	pColumnDescriptions = new String[pNumColumns];
	pParamNames         = new String[pNumColumns];
        pColumnWidthRanges  = new Vector3i[pNumColumns];
	pRenderers          = new TableCellRenderer[pNumColumns];
	pEditors            = new TableCellEditor[pNumColumns];
      }

      /* source node name */ 
      pColumnClasses[0]     = String.class;
      pColumnNames[0]       = "Source Node";
      pColumnWidthRanges[0] = new Vector3i(180, 240, Integer.MAX_VALUE); 
      pRenderers[0]         = new JSimpleTableCellRenderer(JLabel.CENTER);
      pEditors[0]           = null;

      /* source node name file pattern */ 
      pColumnClasses[1]     = String.class;
      pColumnNames[1]       = "File Sequence";
      pColumnWidthRanges[1] = new Vector3i(180, 240, Integer.MAX_VALUE); 
      pRenderers[1]         = new JSimpleTableCellRenderer(JLabel.CENTER);
      pEditors[1]           = null;

      /* unique node short and fully resolved names */ 
      ArrayList<String> ntitles = new ArrayList<String>();
      ArrayList<String> nnames = new ArrayList<String>();
      {
	TreeSet<String> names = new TreeSet<String>();
	names.addAll(snames);
	
	for(String name : names) {
	  nnames.add(name);
	  ntitles.add(stitles.get(snames.indexOf(name)));
	}
      }
      
      /* parameters */ 
      int col = 2;
      for(String pname : pAction.getSourceLayout()) {
	ActionParam aparam = params.get(pname);

	pColumnClasses[col]      = aparam.getClass();
	pColumnNames[col]        = aparam.getNameUI();
	pColumnDescriptions[col] = aparam.getDescription();
	pParamNames[col]         = aparam.getName(); 

	if(aparam instanceof BooleanActionParam) {
	  pColumnWidthRanges[col] = new Vector3i(160); 
	  pRenderers[col] = new JBooleanParamTableCellRenderer(JLabel.CENTER);
	  pEditors[col]   = new JBooleanParamTableCellEditor(160, JLabel.CENTER);
	}
	else if(aparam instanceof IntegerActionParam) {
	  pColumnWidthRanges[col] = new Vector3i(160); 
	  pRenderers[col] = new JSimpleTableCellRenderer(JLabel.CENTER);
	  pEditors[col]   = new JIntegerParamTableCellEditor(160, JLabel.CENTER);
	}
	else if(aparam instanceof ByteSizeActionParam) {
	  pColumnWidthRanges[col] = new Vector3i(160); 
	  pRenderers[col] = new JByteSizeParamTableCellRenderer(JLabel.CENTER);
	  pEditors[col]   = new JByteSizeParamTableCellEditor(160, JLabel.CENTER);
	}
	else if(aparam instanceof DoubleActionParam) {
	  pColumnWidthRanges[col] = new Vector3i(160); 
	  pRenderers[col] = new JSimpleTableCellRenderer(JLabel.CENTER);
	  pEditors[col]   = new JDoubleParamTableCellEditor(160, JLabel.CENTER);
	}
	else if(aparam instanceof Color3dActionParam) {
	  pColumnWidthRanges[col] = new Vector3i(160);
	  pRenderers[col] = new JColorParamTableCellRenderer(parent);
	  pEditors[col]   = new JColorParamTableCellEditor(parent, 160);
	}
 	else if(aparam instanceof Tuple2iActionParam) {
 	  pColumnWidthRanges[col] = new Vector3i(160);
 	  pRenderers[col] = new JTuple2iParamTableCellRenderer();
 	  pEditors[col]   = new JTuple2iParamTableCellEditor(160);
 	}
 	else if(aparam instanceof Tuple3iActionParam) {
 	  pColumnWidthRanges[col] = new Vector3i(240);
 	  pRenderers[col] = new JTuple3iParamTableCellRenderer();
 	  pEditors[col]   = new JTuple3iParamTableCellEditor(240);
 	}
 	else if(aparam instanceof Tuple2dActionParam) {
 	  pColumnWidthRanges[col] = new Vector3i(160);
 	  pRenderers[col] = new JTuple2dParamTableCellRenderer();
 	  pEditors[col]   = new JTuple2dParamTableCellEditor(160);
 	}
 	else if(aparam instanceof Tuple3dActionParam) {
 	  pColumnWidthRanges[col] = new Vector3i(240); 
 	  pRenderers[col] = new JTuple3dParamTableCellRenderer();
 	  pEditors[col]   = new JTuple3dParamTableCellEditor(240);
 	}
 	else if(aparam instanceof Tuple4dActionParam) {
 	  pColumnWidthRanges[col] = new Vector3i(360);
 	  pRenderers[col] = new JTuple4dParamTableCellRenderer();
 	  pEditors[col]   = new JTuple4dParamTableCellEditor(360);
 	}        
	else if(aparam instanceof StringActionParam) {
	  pColumnWidthRanges[col] = new Vector3i(160, 160, Integer.MAX_VALUE); 
	  pRenderers[col] = new JSimpleTableCellRenderer(JLabel.CENTER);
	  pEditors[col]   = new JStringParamTableCellEditor(160, JLabel.CENTER);
	}
	else if(aparam instanceof EnumActionParam) {
	  pColumnWidthRanges[col] = new Vector3i(160, 160, Integer.MAX_VALUE); 
	  pRenderers[col] = new JSimpleTableCellRenderer(JLabel.CENTER);
	  pEditors[col]   = new JEnumParamTableCellEditor(parent, 160);
	}
	else if(aparam instanceof LinkActionParam) {
	  pColumnWidthRanges[col] = new Vector3i(120, 240, Integer.MAX_VALUE); 
	  pRenderers[col] = new JLinkParamTableCellRenderer(ntitles, nnames);
	  pEditors[col]   = new JLinkParamTableCellEditor(parent, 240, ntitles, nnames);
	}

	col++;
      }
    }

    /* make modifiable copy of the source parameters in array form */ 
    {
      pNumRows = snames.size();

      pParams = new ActionParam[pNumRows][];

      pSourceNames = new String[pNumRows];
      snames.toArray(pSourceNames);

      pSourceTitles = new String[pNumRows];
      stitles.toArray(pSourceTitles);

      pFileSeqs = new FileSeq[pNumRows];
      fseqs.toArray(pFileSeqs);

      int row;
      for(row=0; row<pSourceNames.length; row++) {
	String sname = pSourceNames[row];
	FileSeq fseq = pFileSeqs[row];

	if(fseq == null) {
	  if(pAction.hasSourceParams(sname)) {
	    pParams[row] = new ActionParam[pNumColumns-1];
	    
	    int col = 0;
	    for(String pname : pAction.getSourceLayout()) {
	      ActionParam aparam = pAction.getSourceParam(sname, pname);
	      pParams[row][col] = (ActionParam) aparam.clone();
	      col++;
	    }
	  }
	}
	else {
	  FilePattern fpat = fseq.getFilePattern();
	  if(pAction.hasSecondarySourceParams(sname, fpat)) {
	    pParams[row] = new ActionParam[pNumColumns-1];
	    
	    int col = 0;
	    for(String pname : pAction.getSourceLayout()) {
	      ActionParam aparam = pAction.getSecondarySourceParam(sname, fpat, pname);
	      pParams[row][col] = (ActionParam) aparam.clone();
	      col++;
	    }
	  }
	}
      }
    }

    /* initial sort */ 
    sort();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Update the per-source action parameters of the given action based of the parameter values
   * stored in the table.
   */ 
  public void
  updateParams
  (
   BaseAction action 
  )
  {
    action.removeAllSourceParams();
    int row;
    for(row=0; row<pParams.length; row++) {
      String sname = pSourceNames[row];
      ActionParam params[] = pParams[row];
      if(params != null) {
	FileSeq fseq = pFileSeqs[row];
	if(fseq == null) {
	  action.initSourceParams(sname);
	  int col;
	  for(col=2; col<pNumColumns; col++) {
	    ActionParam param = params[col-2];
	    action.setSourceParamValue(sname, param.getName(), param.getValue());
	  }
	}
	else {
	  FilePattern fpat = fseq.getFilePattern();
	  action.initSecondarySourceParams(sname, fpat);
	  int col;
	  for(col=2; col<pNumColumns; col++) {
	    ActionParam param = params[col-2];
	    action.setSecondarySourceParamValue
	      (sname, fpat, param.getName(), param.getValue());
	  }
	}
      }
    }
  }

  /**
   * Add default valued source parameters for the given rows.
   */ 
  public void 
  addRowParams
  (
    int rows[]
  )
  {
    if((rows == null) || (rows.length == 0)) 
      return;

    int wk;
    for(wk=0; wk<rows.length; wk++) {
      int srow = pRowToIndex[rows[wk]];

      TreeMap<String,ActionParam> params = pAction.getInitialSourceParams();
      pParams[srow] = new ActionParam[params.size()];

      int col = 0;
      for(String pname : pAction.getSourceLayout()) {
	pParams[srow][col] = params.get(pname); 
	col++;
      }
    }

    fireTableDataChanged();
  }

  /**
   * Remove the source parameters for the given rows.
   */ 
  public void 
  removeRowParams
  (
   int rows[]
  ) 
  {
    if((rows == null) || (rows.length == 0)) 
      return;

    int wk;
    for(wk=0; wk<rows.length; wk++) {
      int srow = pRowToIndex[rows[wk]];
      pParams[srow] = null;      
    }

    fireTableDataChanged();
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
    for(idx=0; idx<pParams.length; idx++) {
      Comparable value = null;

      switch(pSortColumn) {
      case 0:
	value = pSourceTitles[idx];
	break;

      case 1:
	if(pFileSeqs[idx] != null) 
	  value = pFileSeqs[idx].toString();
	break;

      default:
	{
	  ActionParam params[] = pParams[idx];
	  if(params != null) 
	    value = params[pSortColumn-2].getValue();
	}
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
   * Returns true if the cell at rowIndex and columnIndex is editable.
   */ 
  public boolean 	
  isCellEditable
  (
   int row, 
   int col
  ) 
  {
    int srow = pRowToIndex[row];
    if((pParams[srow] != null) && (col > 1)) 
      return pIsEditable;

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
    int srow = pRowToIndex[row];

    switch(col) {
    case 0:
      return pSourceTitles[srow];

    case 1:
      if(pFileSeqs[srow] != null) 
	return pFileSeqs[srow].toString();
      else 
	return null;

    default:
      if(pParams[srow] != null) 
	return pParams[srow][col-2];
    }

    return null;
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
    assert(col > 1);

    Comparable val = (Comparable) value;
    int vrow = pRowToIndex[row];
    pParams[vrow][col-2].setValue(val);

    int[] selected = pTable.getSelectedRows(); 
    int wk;
    for(wk=0; wk<selected.length; wk++) {
      int srow = pRowToIndex[selected[wk]];
      if((srow != vrow) && (pParams[srow] != null)) 
	pParams[srow][col-2].setValue(val);	
    }

    fireTableDataChanged(); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1209158085921416181L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Is the entire table editable?
   */ 
  private boolean pIsEditable;

  /**
   * The parent action.
   */ 
  private BaseAction pAction; 


  /**
   * The fully resolved names of the parent upstream node for each file sequence.
   */ 
  private String  pSourceNames[];

  /**
   * The short names of the parent upstream node for each file sequence.
   */ 
  private String  pSourceTitles[];

  /**
   * The file sequences of the upstream nodes. Entries which are <CODE>null</CODE> are
   * primary file sequences.
   */ 
  private FileSeq  pFileSeqs[];

  /**
   * The source parameters indexed by: [row, col-2]. <P> 
   * 
   * The entire row may be <CODE>null</CODE> the source node for the row has no parameters.
   */ 
  private ActionParam[][]  pParams;


  /**
   * The parameter names of the columns.
   */ 
  private String  pParamNames[]; 

}
