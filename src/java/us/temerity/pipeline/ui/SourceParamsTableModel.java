// $Id: SourceParamsTableModel.java,v 1.1 2004/06/22 19:44:54 jim Exp $

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
   * 
   * @param status
   *   The current node status.
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
      TreeMap<String,BaseActionParam> params = pAction.getInitialSourceParams();

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
      for(BaseActionParam aparam : params.values()) {
	pColumnClasses[col] = aparam.getClass();
	pColumnNames[col]   = aparam.getNameUI();
	pParamNames[col]    = aparam.getName(); 

	if(aparam instanceof IntegerActionParam) {
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
	else if(aparam instanceof TextActionParam) {
	  pColumnWidths[col]  = 160;
	  pRenderers[col]     = new JTextParamTableCellRenderer(pIsEditable);
	  pEditors[col]       = new JTextParamTableCellEditor(pIsEditable, 160);
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

      pParams = new BaseActionParam[numRows][];

      pSourceNames  = new String[numRows];
      snames.toArray(pSourceNames);

      pSourceTitles = new String[numRows];
      stitles.toArray(pSourceTitles);

      int row;
      for(row=0; row<pSourceNames.length; row++) {
	Collection<BaseActionParam> params = pAction.getSourceParams(pSourceNames[row]);
	if(!params.isEmpty()) {
	  assert(params.size() == (pNumColumns-1));
	  pParams[row] = new BaseActionParam[params.size()];

	  int col = 0;
	  for(BaseActionParam aparam : params) {
	    pParams[row][col] = (BaseActionParam) aparam.clone();
	    col++;
	  }
	}
      }
    }

    /* initial sort */ 
    {
      pSortColumn    = 0;
      pSortAscending = true;
      sort();
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Sort the rows by the values in the given column number. 
   */ 
  public void 
  sortByColumn
  (
   int col
  ) 
  {
    pSortAscending = (pSortColumn == col) ? !pSortAscending : true;
    pSortColumn    = col;
    
    sort();
  }

  /**
   * Sort the rows by the values in the current sort column and direction.
   */ 
  private void 
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
	BaseActionParam params[] = pParams[row];
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
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the widths of the columns.
   */ 
  public int[] 
  getColumnWidths() 
  {
    return pColumnWidths;
  }

  /**
   * Get the renderers for each column. 
   */ 
  public TableCellRenderer[] 
  getRenderers() 
  {
    return pRenderers;
  }

  /**
   * Get the renderers for each column. 
   */ 
  public TableCellEditor[] 
  getEditors() 
  {
    return pEditors;
  }



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
      BaseActionParam params[] = pParams[row];
      if(params != null) {
	action.initSourceParams(sname);
	int col;
	for(col=1; col<pNumColumns; col++) {
	  BaseActionParam param = params[col-1];
	  action.setSourceParamValue(sname, param.getName(), param.getValue());
	}
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/

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
      TreeMap<String,BaseActionParam> params = pAction.getInitialSourceParams();
      pParams[srow] = new BaseActionParam[params.size()];
      int col = 0;
      for(BaseActionParam aparam : params.values()) {
	pParams[srow][col] = aparam;
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
   * Returns the most specific superclass for all the cell values in the column.
   */
  public Class 	
  getColumnClass
  (
   int col
  )
  {
    return pColumnClasses[col];
  }

  /**
   * Returns the number of rows in the model.
   */ 
  public int 
  getRowCount()
  {
    return pParams.length;
  }

  /**
   * Returns the number of columns in the model.
   */ 
  public int
  getColumnCount()
  {
    return pNumColumns;
  }

  /**
   * Returns the name of the column at columnIndex.
   */ 
  public String 	
  getColumnName
  (
   int col
  ) 
  {
    return pColumnNames[col];
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
    if((pParams[srow] != null) && (col > 0)) {
      if(pIsEditable || (pParams[srow][col-1] instanceof TextActionParam)) 
	return true;
    }

    return false;
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
    pParams[pRowToIndex[row]][col-1].setValue((Comparable) value);
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
  private BaseActionParam[][]  pParams;


  /**
   * The number of columns.
   */ 
  private int pNumColumns;

  /**
   * The type of each column.
   */ 
  private Class  pColumnClasses[]; 

  /**
   * The UI names of the columns
   */ 
  private String  pColumnNames[]; 

  /**
   * The parameter names of the columns.
   */ 
  private String  pParamNames[]; 
  
  /**
   * The widths of the columns
   */ 
  private int  pColumnWidths[]; 
    
  /**
   * The render for each column
   */ 
  private TableCellRenderer  pRenderers[]; 
  
  /**
   * The editor for each column
   */ 
  private TableCellEditor  pEditors[]; 


  /**
   * Param row indices for each displayed row number.
   */ 
  private int[] pRowToIndex;   

  /**
   * The number of the column used to sort rows.
   */ 
  private int pSortColumn;

  /**
   * Sort in ascending order?
   */ 
  private boolean pSortAscending;

}
