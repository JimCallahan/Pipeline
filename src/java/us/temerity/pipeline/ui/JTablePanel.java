// $Id: JTablePanel.java,v 1.18 2006/12/14 02:39:05 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.border.*;

/*------------------------------------------------------------------------------------------*/
/*   T A B L E   P A N E L                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A panel containing a {@link JTable JTable} within a {@link JScrollPane JScrollPane} and 
 * various support UI components.
 */ 
public 
class JTablePanel 
  extends JPanel 
  implements ActionListener, AdjustmentListener, TableModelListener, MouseListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new table panel.
   * 
   * @param model
   *   The table model being viewed.
   */ 
  public 
  JTablePanel
  (
   AbstractSortableTableModel model
  ) 
  {
    this(model,  
	 ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED, 
	 ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);	 
  }

  /**
   * Construct a new table panel.
   * 
   * @param model
   *   The table model being viewed.
   * 
   * @param horzScrollPolicy
   *   The horizontal scrollbar policy.
   * 
   * @param vertScrollPolicy
   *   The vertical scrollbar policy.
   */ 
  public 
  JTablePanel
  (
   AbstractSortableTableModel model,
   int horzScrollPolicy,
   int vertScrollPolicy
  ) 
  {
    /* initialize fields */ 
    {
      pFirstUpdate = true;

      pTableModel = new FilteredTableModel(model);
      pTableModel.addTableModelListener(this);
    }

    /* create menus */ 
    {
      pHeaderPopup = new JPopupMenu();  
    }

    /* create panel components */ 
    {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

      int total = 0;
      {
	int col;
	for(col=0; col<model.getColumnCount(); col++) 
	  total += model.getColumnWidth(col);
      }

      {
	Box tbox = new Box(BoxLayout.X_AXIS);

	{
	  JPanel panel = new JPanel();
	  pHeaderPanel = panel;
	
	  panel.setName("TitleValuePanel");
	  panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
	
	  {
	    JViewport view = new JViewport();
	    pHeaderViewport = view;
	  
	    {
	      Box hbox = new Box(BoxLayout.X_AXIS);
	      pHeaderBox = hbox;

	      Dimension size = new Dimension(total, 23); 
	      hbox.setMinimumSize(size);
	      hbox.setPreferredSize(size);
	      hbox.setMaximumSize(size);
	    
	      view.setView(hbox);
	    }

	    panel.add(view);

	    Dimension size = panel.getPreferredSize();
	    panel.setMinimumSize(new Dimension(100, size.height));
	    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, size.height));
	  }
	
	  tbox.add(panel);
	}

	if(vertScrollPolicy == ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS)
	  tbox.add(Box.createRigidArea(new Dimension(15, 0)));

	add(tbox);
      }

      add(Box.createRigidArea(new Dimension(0, 1)));

      {
	JTable table = new JTable(pTableModel);
	pTable = table;

	pTableModel.setTable(table);

	table.setAutoCreateColumnsFromModel(true);
	table.setColumnSelectionAllowed(false);
      
	table.setShowHorizontalLines(false);
	table.setShowVerticalLines(false);
      
	table.setIntercellSpacing(new Dimension(3, 3));
	table.setRowHeight(22);
      
	table.setTableHeader(null);
      
	{
	  TableColumnModel cmodel = table.getColumnModel();
	
	  int col;
	  for(col=0; col<model.getColumnCount(); col++) {
	    TableColumn tcol = cmodel.getColumn(col);
	    tcol.setCellRenderer(model.getRenderer(col));

	    TableCellEditor editor = model.getEditor(col);
	    if(editor != null) 
	      tcol.setCellEditor(editor);

	    int width = model.getColumnWidth(col);
	    tcol.setMinWidth(width);
	    tcol.setPreferredWidth(width);
	    tcol.setMaxWidth(width);
	  }
	}
      
	table.setPreferredScrollableViewportSize(new Dimension(total, 300));
	table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      }

      {
	JScrollPane scroll = new JScrollPane();
	pTableScroll = scroll;

	scroll.setViewportView(pTable);
      
	scroll.setHorizontalScrollBarPolicy(horzScrollPolicy);
	scroll.setVerticalScrollBarPolicy(vertScrollPolicy);

	scroll.getHorizontalScrollBar().addAdjustmentListener(this);

	add(scroll);
      }
    }

    pTableModel.fireTableStructureChanged();
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   U S E R    I N T E R F A C E                                                         */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Stop any editing in progress and save the current value back to the model.
   */ 
  public void 
  stopEditing() 
  {
    if(pTable.isEditing()) {
      int col;
      for(col=0; col<pTableModel.getColumnCount(); col++) {
	TableCellEditor editor = pTableModel.getEditor(col);
	if(editor != null)
	  editor.stopCellEditing();
      }
    }
  }
  
  /**
   * Stop any editing in progress and discard the current value.
   */ 
  public void 
  cancelEditing() 
  {
    if(pTable.isEditing()) {
      int col;
      for(col=0; col<pTableModel.getColumnCount(); col++) {
	TableCellEditor editor = pTableModel.getEditor(col);
	if(editor != null)
	  editor.cancelCellEditing();
      }
    }
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the header menu.
   */ 
  public void 
  updateHeaderMenu() 
  {
    pHeaderPopup.removeAll();
    
    {
      JMenuItem item = new JMenuItem("Show All");
      item.setActionCommand("show-all-columns");
      item.addActionListener(this);
      pHeaderPopup.add(item);  
    }
      
    pHeaderPopup.addSeparator();

    for(String name : pTableModel.getAllColumnNames()) {
      JCheckBoxMenuItem item = new JCheckBoxMenuItem(name, pTableModel.isColumnVisible(name));
      item.setActionCommand("toggle-column:" + name);
      item.addActionListener(this);
      pHeaderPopup.add(item);  
    }
  }


  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Rebuild the table model to show only the visible columns.
   * 
   * @param modified
   *   Modify whether a column should be visible indexed by column name. 
   */ 
  public void 
  refilterColumns
  (
   TreeMap<String,Boolean> modified
  ) 
  {
    pTableModel.refilter(modified);
  }

  /**
   * Get the column index in the underlying unfiltered model which corresponds to the 
   * given index in the filtered table model.
   */ 
  public int 
  getColumnIndex 
  (
   int col
  ) 
  {
    return pTableModel.getColumnIndex(col); 
  }
  

  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Whether the column with the given name is currently visible.
   */ 
  public boolean 
  isColumnVisible
  (
   String name
  ) 
  {
    return pTableModel.isColumnVisible(name);
  }

  /**
   * Set whether the given column should be visible.
   */ 
  public void 
  setColumnVisible
  (
   String name, 
   boolean tf
  ) 
  {
    pTableModel.setColumnVisible(name, tf);
  }

  /**
   * Set whether the given columns should be visible.
   */ 
  public void 
  setColumnsVisible
  (
   TreeSet<String> names, 
   boolean tf
  ) 
  {
    pTableModel.setColumnsVisible(names, tf);
  }


  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Notify the table that the structure of the table model has changed.
   */ 
  public void 
  tableStructureChanged() 
  {
    pTableModel.fireTableStructureChanged();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the table.
   */ 
  public JTable
  getTable()
  {
    return pTable;
  }

  /**
   * Get the table scroll pane.
   */ 
  public JScrollPane
  getTableScroll()
  {
    return pTableScroll;
  }

  /**
   * Get the header viewport.
   */ 
  public JViewport
  getHeaderViewport() 
  {
    return pHeaderViewport;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- MOUSE LISTENER METHODS --------------------------------------------------------------*/

  /**
   * Invoked when the mouse button has been clicked (pressed and released) on a component. 
   */ 
  public void 
  mouseClicked(MouseEvent e) {}
   
  /**
   * Invoked when the mouse enters a component. 
   */
  public void 
  mouseEntered(MouseEvent e) {}
  
  /**
   * Invoked when the mouse exits a component. 
   */ 
  public void 
  mouseExited(MouseEvent e) {}

  /**
   * Invoked when a mouse button has been pressed on a component. 
   */
  public void 
  mousePressed
  (
   MouseEvent e 
  ) 
  {
    int mods = e.getModifiersEx();
    switch(e.getButton()) {
    case MouseEvent.BUTTON3:
      {
	int on1  = (MouseEvent.BUTTON3_DOWN_MASK);
	
	int off1 = (MouseEvent.BUTTON1_DOWN_MASK | 
		    MouseEvent.BUTTON2_DOWN_MASK | 
		    MouseEvent.SHIFT_DOWN_MASK |
		    MouseEvent.ALT_DOWN_MASK |
		    MouseEvent.CTRL_DOWN_MASK);
      
	/* BUTTON3: popup menu */ 
	if((mods & (on1 | off1)) == on1) {
	  updateHeaderMenu();
	  pHeaderPopup.show(e.getComponent(), e.getX(), e.getY());
	}
      }
    }    
  }

  /**
   * Invoked when a mouse button has been released on a component. 
   */ 
  public void 
  mouseReleased(MouseEvent e) {}


  /*-- ACTION LISTENER METHODS -------------------------------------------------------------*/

  /** 
   * Invoked when an action occurs. 
   */ 
  public void 
  actionPerformed
  (
   ActionEvent e
  ) 
  {
    String cmd = e.getActionCommand();
    if(cmd.startsWith("sort-column:")) 
      doSort(Integer.valueOf(cmd.substring(12)));
    else if(cmd.startsWith("toggle-column:")) 
      doToggleColumn((JCheckBoxMenuItem) e.getSource(), cmd.substring(14));
    else if(cmd.equals("show-all-columns")) 
      doShowAllColumns();
  }


  /*-- ADJUSTMENT LISTENER METHODS ---------------------------------------------------------*/

  /**
   * Invoked when the value of the adjustable has changed.
   */ 
  public void
  adjustmentValueChanged
  (
   AdjustmentEvent e
  )
  { 
    JViewport mview = pTableScroll.getViewport();
    if((mview != null) && (pHeaderViewport != null)) {
      Point mpos = mview.getViewPosition();    
      Point hpos = pHeaderViewport.getViewPosition();

      if(mpos.x != hpos.x) {
	hpos.x = mpos.x;
	pHeaderViewport.setViewPosition(hpos);
      }
    }  
  }


  /*-- TABLE MODEL LISTENER METHODS --------------------------------------------------------*/

  /**
   * This fine grain notification tells listeners the exact range of cells, rows, or columns 
   * that changed.
   */ 
  public void 
  tableChanged
  (
   TableModelEvent e
  )
  {    
    if(!pFirstUpdate && (e.getFirstRow() != TableModelEvent.HEADER_ROW))
      return;
    pFirstUpdate = false;

    pHeaderBox.removeAll();
    Box hbox = new Box(BoxLayout.X_AXIS); 

    TableColumnModel cmodel = pTable.getColumnModel();

    int total = 0;
    int col;
    for(col=0; col<pTableModel.getColumnCount(); col++) {
      int width = pTableModel.getColumnWidth(col);

      {
	JButton btn = new JButton(pTableModel.getColumnName(col));
	btn.setName(pTableModel.getColumnColorPrefix(col) + "TableHeaderButton");
		
	{	    
	  Dimension size = new Dimension(width, 23);
	  btn.setMinimumSize(size);
	  btn.setPreferredSize(size);
	  btn.setMaximumSize(size);
	}
		
	btn.addActionListener(this);
	btn.setActionCommand("sort-column:" + pTableModel.getColumnIndex(col));	  
	
	btn.addMouseListener(this);
	
	btn.setFocusable(false);
	
	btn.setToolTipText
	  (UIFactory.formatToolTip(pTableModel.getColumnDescription(col)));

	hbox.add(btn);
      }

      {      
	TableColumn tcol = cmodel.getColumn(col);
	tcol.setCellRenderer(pTableModel.getRenderer(col));
	
	TableCellEditor editor = pTableModel.getEditor(col);
	if(editor != null) 
	  tcol.setCellEditor(editor);
	
	tcol.setMinWidth(width);
	tcol.setPreferredWidth(width);
	tcol.setMaxWidth(width);
      }

      total += width; 
    }

    Dimension size = new Dimension(total, 23); 
    hbox.setMinimumSize(size);
    hbox.setPreferredSize(size);
    hbox.setMaximumSize(size);

    pHeaderViewport.setView(hbox);
    pHeaderBox = hbox;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Sort the rows by the values in the given column number. 
   * 
   * @param col
   *   The column number of the underlying unfiltered table model.
   */ 
  protected void 
  doSort
  (
   int col
  )
  { 
    if(pTable.getRowCount() == 0) 
      return;

    int[] selected = pTable.getSelectedRows();

    stopEditing();
    int[] preToPost = pTableModel.sortByColumn(col);

    int row;
    for(row=0; row<selected.length; row++) {
      int nrow = preToPost[selected[row]];
      pTable.addRowSelectionInterval(nrow, nrow);
    }
  }

  /**
   * Toggle the visibility of the given column.
   */ 
  private void 
  doToggleColumn
  (
   JCheckBoxMenuItem item, 
   String name
  ) 
  {
    pTableModel.setColumnVisible(name, item.isSelected());
  }

  /**
   * Make all columns visible.
   */ 
  public void 
  doShowAllColumns() 
  {
    pTableModel.setAllColumnsVisible();
  }
  
  



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private 
  class FilteredTableModel
    implements SortableTableModel
  {
    /*--------------------------------------------------------------------------------------*/
    /*   C O N S T R U C T O R                                                              */
    /*--------------------------------------------------------------------------------------*/
    
    /**
     * Construct a table model which shows selected columns from the given table model.
     */ 
    public 
    FilteredTableModel
    (
     AbstractSortableTableModel model
    ) 
    {
      super(); 
      
      pModel = model;

      pVisibleColumns = new TreeSet<String>();
      int col;
      for(col=0; col<pModel.getColumnCount(); col++) 
	pVisibleColumns.add(pModel.getColumnName(col));

      if(pVisibleColumns.size() < pModel.getColumnCount()) 
	throw new IllegalArgumentException
	  ("The names of the table model columns must be unique!");
      
      refilter();
    }

    
    /*--------------------------------------------------------------------------------------*/
    /*   F I L T E R I N G                                                                  */
    /*--------------------------------------------------------------------------------------*/

    /**
     * Rebuild the table model to show only the visible columns.
     */ 
    public void 
    refilter() 
    {
      refilter(null);
    }

    /**
     * Rebuild the table model to show only the visible columns.
     * 
     * @param modified
     *   Modify whether a column should be visible indexed by column name. 
     */ 
    public void 
    refilter
    (
     TreeMap<String,Boolean> modified
    ) 
    {
      if(modified != null) {
	for(String name : modified.keySet()) {
	  if(modified.get(name)) 
	    pVisibleColumns.add(name);
	  else 
	    pVisibleColumns.remove(name);
	}
      }

      pColumnIndices = new int[pVisibleColumns.size()];

      int idx, col;
      for(idx=0, col=0; col<pModel.getColumnCount(); col++) {
	String name = pModel.getColumnName(col);
	if(pVisibleColumns.contains(name)) {
	  pColumnIndices[idx] = col;
	  idx++;
	}
      }

      pModel.fireTableStructureChanged();
    }


    /*--------------------------------------------------------------------------------------*/

    /**
     * Whether the column with the given name is currently visible.
     */ 
    public boolean 
    isColumnVisible
    (
     String name
    ) 
    {
      return pVisibleColumns.contains(name);
    }

    /**
     * Set whether a column is visible.
     */ 
    public void 
    setColumnVisible
    (
     String name, 
     boolean tf
    ) 
    {
      boolean modified = false;
      if(tf && !pVisibleColumns.contains(name)) {
	int col;
	for(col=0; col<pModel.getColumnCount(); col++) {
	  if(pModel.getColumnName(col).equals(name)) {
	    pVisibleColumns.add(name);
	    modified = true;
	    break;
	  }
	}
      }
      else if(!tf && pVisibleColumns.contains(name) && (pVisibleColumns.size() > 1)) {
	pVisibleColumns.remove(name);
	modified = true;
      }
      
      if(modified) {
	refilter();	
	pModel.columnVisiblityChanged();
      }
    }

    /**
     * Set whether a column is visible.
     */ 
    public void 
    setColumnsVisible
    (
     TreeSet<String> names, 
     boolean tf
    ) 
    {
      boolean modified = false;
      if(tf) {
	for(String name : names) {
	  if(!pVisibleColumns.contains(name)) {
	    int col;
	    for(col=0; col<pModel.getColumnCount(); col++) {
	      if(pModel.getColumnName(col).equals(name)) {
		pVisibleColumns.add(name);
		modified = true;
		break;
	      }
	    }
	  }
	}
      }
      else {
	for(String name : names) {
	  if(pVisibleColumns.contains(name) && (pVisibleColumns.size() > 1)) {
	    pVisibleColumns.remove(name);
	    modified = true;
	  }
	}
      }

      if(modified) {
	refilter();	
	pModel.columnVisiblityChanged();
      }
    }

    /**
     * Set whether a column is visible.
     */ 
    public void 
    setAllColumnsVisible() 
    {
      int col;
      for(col=0; col<pModel.getColumnCount(); col++) 
	pVisibleColumns.add(pModel.getColumnName(col));
      
      refilter();
      pModel.columnVisiblityChanged();
    }


    /*--------------------------------------------------------------------------------------*/
    /*   A C C E S S                                                                        */
    /*--------------------------------------------------------------------------------------*/

    /**
     * Get the column index in the underlying unfiltered model which corresponds to the 
     * given index in this model.
     */ 
    public int 
    getColumnIndex
    (
     int col
    ) 
    {
      return pColumnIndices[col];
    }

    /**
     * Returns the names of all of the columns, whether visible or not.
     */ 
    public ArrayList<String>
    getAllColumnNames() 
    {
      ArrayList<String> names = new ArrayList<String>();
      int col;
      for(col=0; col<pModel.getColumnCount(); col++) 
	names.add(pModel.getColumnName(col));
      return names;
    }


    /*--------------------------------------------------------------------------------------*/

    /**
     * Set the parent table.
     */ 
    public void 
    setTable
    (
     JTable table
    ) 
    {
      pModel.setTable(table);
    }


    /*--------------------------------------------------------------------------------------*/

    /**
     * Get the width of the given column.
     */ 
    public int
    getColumnWidth
    (
     int col   
    )
    {
      return pModel.getColumnWidth(pColumnIndices[col]);
    }

    /**
     * Returns the color prefix used to determine the synth style of the header button for 
     * the given column.
     */ 
    public String 	
    getColumnColorPrefix
    (
     int col
    )
    {
      return pModel.getColumnColorPrefix(pColumnIndices[col]);
    }

    /**
     * Returns the description of the column columnIndex used in tool tips.
     */ 
    public String 	
    getColumnDescription
    (
     int col
    ) 
    {
      return pModel.getColumnDescription(pColumnIndices[col]);
    }

    /**
     * Get the renderer for the given column. 
     */ 
    public TableCellRenderer
    getRenderer
    (
     int col   
    )
    {
      return pModel.getRenderer(pColumnIndices[col]);
    }

    /**
     * Get the editor for the given column. 
     */ 
    public TableCellEditor
    getEditor
    (
     int col   
    )
    {
      return pModel.getEditor(pColumnIndices[col]);
    }



    /*--------------------------------------------------------------------------------------*/
    /*   S O R T I N G                                                                      */
    /*--------------------------------------------------------------------------------------*/

    /**
     * Sort the rows by the values in the given column number. <P> 
     * 
     * @return 
     *   The mapping of the pre-sort to post-sort row numbers.
     */ 
    public int[]
    sortByColumn
    (
     int col
    )
    {
      return pModel.sortByColumn(col);
    }

    /**
     * Sort the rows by the values in the current sort column and direction.
     */ 
    public void 
    sort()
    {
      pModel.sort();
    }


    /*--------------------------------------------------------------------------------------*/
    /*   S O R T A B L E   T A B L E   M O D E L   O V E R R I D E S                        */
    /*--------------------------------------------------------------------------------------*/
    
    /**
     * Returns the most specific superclass for all the cell values in the column.
     */
    public Class 	
    getColumnClass
    (
     int col
    )
    {
      return pModel.getColumnClass(pColumnIndices[col]);
    }

    /**
     * Returns the number of columns in the model.
     */ 
    public int
    getColumnCount()
    {
      return pColumnIndices.length;
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
      return pModel.getColumnName(pColumnIndices[col]);
    }



    /*--------------------------------------------------------------------------------------*/
    /*   T A B L E   M O D E L   O V E R R I D E S                                          */
    /*--------------------------------------------------------------------------------------*/

    /**
     * Adds a listener to the list that is notified each time a change to the data 
     * model occurs.
     */ 
    public void 
    addTableModelListener
    (
     TableModelListener l
    )
    {
      pModel.addTableModelListener(l);
    }
      
    /**
     * Removes a listener from the list that is notified each time a change to the data 
     * model occurs.
     */ 
    public void 
    removeTableModelListener
    (
     TableModelListener l
    )
    {
      pModel.removeTableModelListener(l);
    }

    
    /**
     * Notifies all listeners that the table's structure has changed.
     */ 
    public void 	
    fireTableStructureChanged()
    {
      pModel.fireTableStructureChanged();
    }
          
      
    /*--------------------------------------------------------------------------------------*/

    /**
     * Returns the number of rows in the model.
     */ 
    public int 
    getRowCount()
    {
      return pModel.getRowCount();
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
      return pModel.isCellEditable(row, pColumnIndices[col]);
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
      return pModel.getValueAt(row, pColumnIndices[col]);
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
      pModel.setValueAt(value, row, pColumnIndices[col]);
    }


    /*--------------------------------------------------------------------------------------*/
    /*   S T A T I C   I N T E R N A L S                                                    */
    /*--------------------------------------------------------------------------------------*/

    private static final long serialVersionUID = -3265762884032143354L;


    /*--------------------------------------------------------------------------------------*/
    /*   I N T E R N A L S                                                                  */
    /*--------------------------------------------------------------------------------------*/

    private AbstractSortableTableModel  pModel; 

    private int[]            pColumnIndices;
    private TreeSet<String>  pVisibleColumns; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6732073412889969988L;
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The column header.
   */ 
  private JPanel pHeaderPanel;

  /**
   * The scroll pane containing the column header.
   */ 
  private JViewport  pHeaderViewport;

  /**
   * The box containing the header buttons. 
   */ 
  private Box  pHeaderBox; 

  /**
   * The column show/hide menu.
   */ 
  private JPopupMenu   pHeaderPopup; 
  
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Is this the first update of the table model.
   */ 
  private boolean  pFirstUpdate; 

  /**
   * The table model.
   */ 
  private FilteredTableModel pTableModel;

  /**
   * The table.
   */ 
  private JTable  pTable;

  /**
   * The scroll pane containing the table.
   */ 
  private JScrollPane  pTableScroll;

}
