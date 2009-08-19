// $Id: JTablePanel.java,v 1.20 2009/08/19 23:42:47 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.plaf.*;

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
  implements MouseListener, TableModelListener, ChangeListener
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

      pTableModel = model;
      pTableModel.addTableModelListener(this);
    }

    /* create panel components */ 
    {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

      Vector3i total = new Vector3i(6); 
      {
	int col;
	for(col=0; col<model.getColumnCount(); col++) 
	  total.add(model.getColumnWidthRange(col));
      }

      {
	Box tbox = new Box(BoxLayout.X_AXIS);

	{
	  JPanel panel = new JPanel();
	  pHeaderPanel = panel;
	
	  panel.setName("TitleValuePanel");
	  panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
          
          pHeaderViewport = new JViewport();
          panel.add(pHeaderViewport); 

          panel.setMinimumSize(new Dimension(29, 29));
          panel.setPreferredSize(new Dimension(total.y(), 29));
          panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 29));
	
	  tbox.add(panel);
	}

	if(vertScrollPolicy == ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS)
	  tbox.add(Box.createRigidArea(new Dimension(15, 0)));

	add(tbox);
      }

      add(Box.createRigidArea(new Dimension(0, 1)));

      {
        pTable = new JFancyTable(this, pTableModel);                                 
        pTableScroll = 
          UIFactory.createScrollPane(pTable, horzScrollPolicy, vertScrollPolicy,
                                     null, new Dimension(total.y(), 22*10+6), null); 
        pTableScroll.getViewport().addChangeListener(this); 
        add(pTableScroll);
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
    case MouseEvent.BUTTON1:
      {
	int on1  = (MouseEvent.BUTTON1_DOWN_MASK);
	
	int off1 = (MouseEvent.BUTTON2_DOWN_MASK | 
		    MouseEvent.BUTTON3_DOWN_MASK | 
		    MouseEvent.SHIFT_DOWN_MASK |
		    MouseEvent.ALT_DOWN_MASK |
		    MouseEvent.CTRL_DOWN_MASK);
      
	/* BUTTON1: sort */ 
	if((mods & (on1 | off1)) == on1) {
          JTableHeader header = pTable.getTableHeader();
          int col = header.columnAtPoint(e.getPoint());
          if(col != -1) 
            doSort(col); 
	}
      }
    }    
  }

  /**
   * Invoked when a mouse button has been released on a component. 
   */ 
  public void 
  mouseReleased(MouseEvent e) {}




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
    /* HEADER_ROW means that the whole model changed */ 
    if(!pFirstUpdate && (e.getFirstRow() != TableModelEvent.HEADER_ROW))
      return;
    pFirstUpdate = false;
 
    TableColumnModel cmodel = pTable.getColumnModel();
    int col;
    for(col=0; col<pTableModel.getColumnCount(); col++) {
      TableColumn tcol = cmodel.getColumn(col);
      tcol.setHeaderRenderer(new JHeaderTableCellRenderer(pTableModel, col, JLabel.CENTER));
      tcol.setCellRenderer(pTableModel.getRenderer(col));
   
      TableCellEditor editor = pTableModel.getEditor(col);
      if(editor != null) 
        tcol.setCellEditor(editor);
   
      Vector3i wrange = pTableModel.getColumnWidthRange(col); 
      tcol.setMinWidth(wrange.x()); 
      tcol.setPreferredWidth(wrange.y());
      tcol.setMaxWidth(wrange.z()); 
    }

    revalidate(); 
    repaint(); // IS THIS NEEDED?
  }


  /*-- CHANGE LISTENER METHODS -------------------------------------------------------------*/

  /**
   * Invoked when the target of the listener has changed its state.
   */
  public void 
  stateChanged
  (
   ChangeEvent e
  )
  {
    if(!e.getSource().equals(pTableScroll.getViewport())) 
      return; 

    /* synchronize the scrolling of the header viewport with the main table viewport */ 
    Point p = pTableScroll.getViewport().getViewPosition();
    pHeaderViewport.setViewPosition(new Point(p.x, 0));
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

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private 
  class JFancyTable
    extends JTable
  {
    /*--------------------------------------------------------------------------------------*/
    /*   C O N S T R U C T O R                                                              */
    /*--------------------------------------------------------------------------------------*/
    
    public 
    JFancyTable
    (
     JTablePanel parent,
     AbstractSortableTableModel model
    ) 
    {
      super(model);

      model.setTable(this);
      
      setAutoCreateColumnsFromModel(true);
      setColumnSelectionAllowed(false);
      
      setShowHorizontalLines(false);
      setShowVerticalLines(false);
      
      setIntercellSpacing(new Dimension(3, 3));
      setRowHeight(22);
      
      Vector3i total = new Vector3i(); 
      {
	int col;
	for(col=0; col<model.getColumnCount(); col++) 
	  total.add(model.getColumnWidthRange(col));
      }

      {
        JTableHeader header = getTableHeader();
        header.setResizingAllowed(true); 
        
        header.addMouseListener(parent);
        
        header.setMinimumSize(new Dimension(total.x(), 23)); 
        header.setPreferredSize(new Dimension(total.y(), 23)); 
        header.setMaximumSize(new Dimension(total.z(), 23)); 
      }
      
      {
        TableColumnModel cmodel = getColumnModel();
	
        int col;
        for(col=0; col<model.getColumnCount(); col++) {
          TableColumn tcol = cmodel.getColumn(col);
  
          tcol.setHeaderRenderer(new JHeaderTableCellRenderer(model, col, JLabel.CENTER));
          tcol.setCellRenderer(model.getRenderer(col));
          
          TableCellEditor editor = model.getEditor(col);
          if(editor != null) 
            tcol.setCellEditor(editor);
          
          Vector3i wrange = model.getColumnWidthRange(col); 
          total.add(wrange); 
          
          tcol.setMinWidth(wrange.x()); 
          tcol.setPreferredWidth(wrange.y());
          tcol.setMaxWidth(wrange.z()); 
          
          tcol.setResizable(true); 
        }
      }
      
      setPreferredScrollableViewportSize(new Dimension(total.y(), 300));
      setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
    } 
    

    /*--------------------------------------------------------------------------------------*/
    /*   J T A B L E   O V E R R I D E S                                                    */
    /*--------------------------------------------------------------------------------------*/
    
    /**
     * Setup the column header properly.
     */ 
    protected void 
    configureEnclosingScrollPane() 
    {
      Border border = pTableScroll.getBorder();
      if((border == null) || (border instanceof UIResource)) {
        Border scrollPaneBorder = UIManager.getBorder("Table.scrollPaneBorder");
        if(scrollPaneBorder != null) 
          pTableScroll.setBorder(scrollPaneBorder);
      }

      pHeaderViewport.setView(getTableHeader()); 
    }


    /*--------------------------------------------------------------------------------------*/
    /*   S T A T I C   I N T E R N A L S                                                    */
    /*--------------------------------------------------------------------------------------*/

    static final long serialVersionUID = -3743182943228777155L;
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
   * The viewport containing the column header.
   */ 
  private JViewport  pHeaderViewport;


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether this the first update of the table model.
   */ 
  private boolean  pFirstUpdate; 

  /**
   * The table model.
   */ 
  private AbstractSortableTableModel pTableModel;

  /**
   * The table.
   */ 
  private JFancyTable  pTable;

  /**
   * The scroll pane containing the table.
   */ 
  private JScrollPane  pTableScroll;

}
