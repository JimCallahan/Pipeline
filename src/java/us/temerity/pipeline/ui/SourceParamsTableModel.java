// $Id: SourceParamsTableModel.java,v 1.7 2004/11/18 09:16:58 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
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
  extends SortableTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   * 
   * @param isEditable
   *   Should the table allow editing of parameter values?
   * 
   * @param stitles
   *   The short names of the upstream nodes.
   * 
   * @param snames
   *   The fully resolved node names of the upstream nodes.
   * 
   * @param action
   *   The parent action of the per-source parameters.
   */
  public 
  SourceParamsTableModel
  (
   boolean isEditable, 
   ArrayList<String> stitles, 
   ArrayList<String> snames, 
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

      {
	pNumColumns = params.size() + 1;

	pColumnClasses = new Class[pNumColumns];
	pColumnNames   = new String[pNumColumns];
	pParamNames    = new String[pNumColumns];
	pColumnWidths  = new int[pNumColumns];
	pRenderers     = new TableCellRenderer[pNumColumns];
	pEditors       = new TableCellEditor[pNumColumns];
      }

      /* source node name */ 
      pColumnClasses[0] = String.class;
      pColumnNames[0]   = "Source Node";
      pColumnWidths[0]  = 240;
      pRenderers[0]     = new JSimpleTableCellRenderer(JLabel.LEFT);
      pEditors[0]       = null;

      /* parameters */ 
      int col = 1;
      for(String pname : pAction.getSourceGroup().getParamNames()) {
	ActionParam aparam = params.get(pname);

	pColumnClasses[col] = aparam.getClass();
	pColumnNames[col]   = aparam.getNameUI();
	pParamNames[col]    = aparam.getName(); 

	if(aparam instanceof BooleanActionParam) {
	  pColumnWidths[col]  = 160;
	  pRenderers[col]     = new JBooleanTableCellRenderer(JLabel.CENTER);
	  pEditors[col]       = new JBooleanParamTableCellEditor(160, JLabel.CENTER);
	}
	else if(aparam instanceof IntegerActionParam) {
	  pColumnWidths[col]  = 160;
	  pRenderers[col]     = new JSimpleTableCellRenderer(JLabel.CENTER);
	  pEditors[col]       = new JIntegerParamTableCellEditor(160, JLabel.CENTER);
	}
	else if(aparam instanceof DoubleActionParam) {
	  pColumnWidths[col]  = 160;
	  pRenderers[col]     = new JSimpleTableCellRenderer(JLabel.CENTER);
	  pEditors[col]       = new JDoubleParamTableCellEditor(160, JLabel.CENTER);
	}
	else if(aparam instanceof StringActionParam) {
	  pColumnWidths[col]  = 160;
	  pRenderers[col]     = new JSimpleTableCellRenderer(JLabel.CENTER);
	  pEditors[col]       = new JStringParamTableCellEditor(160, JLabel.CENTER);
	}
	else if(aparam instanceof EnumActionParam) {
	  pColumnWidths[col]  = 160;
	  pRenderers[col]     = new JSimpleTableCellRenderer(JLabel.CENTER);
	  pEditors[col]       = new JEnumParamTableCellEditor(160);
	}
	else if(aparam instanceof LinkActionParam) {
	  pColumnWidths[col]  = 240;
	  pRenderers[col]     = new JLinkParamTableCellRenderer(stitles, snames);
	  pEditors[col]       = new JLinkParamTableCellEditor(240, stitles, snames);
	}

	col++;
      }
    }

    /* make modifiable copy of the source parameters in array form */ 
    {
      assert(snames.size() == stitles.size());
      int numRows = snames.size();

      pParams = new ActionParam[numRows][];

      pSourceNames  = new String[numRows];
      snames.toArray(pSourceNames);

      pSourceTitles = new String[numRows];
      stitles.toArray(pSourceTitles);

      Set<String> sources = pAction.getSourceNames();

      int row;
      for(row=0; row<pSourceNames.length; row++) {
	String sname = pSourceNames[row];

	if(sources.contains(sname)) {
	  pParams[row] = new ActionParam[pNumColumns-1];

	  int col = 0;
	  for(String pname : pAction.getSourceGroup().getParamNames()) {
	    ActionParam aparam = pAction.getSourceParam(sname, pname);
	    pParams[row][col] = (ActionParam) aparam.clone();
	    col++;
	  }
	}
      }
    }

    /* initial sort */ 
    sort();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Sort the rows by the values in the current sort column and direction.
   */ 
  protected void 
  sort()
  {
    ArrayList<Comparable> values = new ArrayList<Comparable>();
    ArrayList<Integer> indices = new ArrayList<Integer>();
    int row;
    for(row=0; row<pParams.length; row++) {
      Comparable value = null;

      if(pSortColumn == 0) {
	value = pSourceTitles[row];
      }
      else {
	ActionParam params[] = pParams[row];
	if(params != null) 
	  value = params[pSortColumn-1].getValue();
      }

      int wk;
      for(wk=0; wk<values.size(); wk++) {
	Comparable cvalue = values.get(wk);
	if(value == null) {
	  if(cvalue == null)
	    break;
	}
	else if((cvalue != null) && (value.compareTo(cvalue) > 0)) 
	  break;
      }

      values.add(wk, value);
      indices.add(wk, row);
    }

    pRowToIndex = new int[indices.size()];
    int wk; 
    if(pSortAscending) {
      for(wk=0; wk<pRowToIndex.length; wk++) 
	pRowToIndex[wk] = indices.get(wk);
    }
    else {
      for(wk=0, row=indices.size()-1; wk<pRowToIndex.length; wk++, row--) 
	pRowToIndex[wk] = indices.get(row);
    }

    fireTableDataChanged();    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
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
	action.initSourceParams(sname);
	int col;
	for(col=1; col<pNumColumns; col++) {
	  ActionParam param = params[col-1];
	  action.setSourceParamValue(sname, param.getName(), param.getValue());
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
      for(String pname : pAction.getSourceGroup().getParamNames()) {
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
  /*   T A B L E   M O D E L   O V E R R I D E S                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the number of rows in the model.
   */ 
  public int 
  getRowCount()
  {
    return pParams.length;
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
    int srow = pRowToIndex[row];
    if((pParams[srow] != null) && (col > 0)) 
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

    if(col == 0) 
      return pSourceTitles[srow];

    if(pParams[srow] != null) 
      return pParams[srow][col-1];

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
    assert(col > 0);
    Comparable val = (Comparable) value;
    int vrow = pRowToIndex[row];
    pParams[vrow][col-1].setValue(val);

    int[] selected = pTable.getSelectedRows(); 
    int wk;
    for(wk=0; wk<selected.length; wk++) {
      int srow = pRowToIndex[selected[wk]];
      if((srow != vrow) && (pParams[srow] != null)) 
	pParams[srow][col-1].setValue(val);	
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
   * The fully resolved names of the source nodes.
   */ 
  private String  pSourceNames[];

  /**
   * The short source node names.
   */ 
  private String  pSourceTitles[];

  /**
   * The source parameters indexed by: [row, col-1]. <P> 
   * 
   * The entire row may be <CODE>null</CODE> the source node for the row has no parameters.
   */ 
  private ActionParam[][]  pParams;

  /**
   * The parameter names of the columns.
   */ 
  private String  pParamNames[]; 

}
