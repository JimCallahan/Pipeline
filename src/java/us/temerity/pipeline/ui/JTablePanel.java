// $Id: JTablePanel.java,v 1.7 2004/10/25 18:56:47 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*;

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
  implements ActionListener, AdjustmentListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new table panel.
   * 
   * @param model
   *   The table model being viewed.
   * 
   * @param width
   *   The horizontal size of each table column.
   * 
   * @param renderers
   *   The cell renderer for each column.
   * 
   * @param editors
   *   The cell editor for each column, can be <CODE>null</CODE> for uneditable columns.
   */ 
  public 
  JTablePanel
  (
   SortableTableModel model, 
   int width[], 
   TableCellRenderer renderers[], 
   TableCellEditor editors[]
  ) 
  {
    assert(model.getColumnCount() == width.length);
    assert(width.length == renderers.length);
    assert(width.length == editors.length);

    pTableModel = model;
    pEditors = editors;

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    int total = 0;
    {
      int wk;
      for(wk=0; wk<width.length; wk++) 
	total += width[wk];
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
	    
	    int wk;
	    for(wk=0; wk<width.length; wk++) {
	      {
		JButton btn = new JButton(pTableModel.getColumnName(wk));
		btn.setName("TableHeaderButton");
		
		{	    
		  Dimension size = new Dimension(width[wk], 23);
		  btn.setMinimumSize(size);
		  btn.setPreferredSize(size);
		  btn.setMaximumSize(size);
		}
		
		btn.addActionListener(this);
		btn.setActionCommand("sort-column:" + wk);	  

		btn.setFocusable(false);

		hbox.add(btn);
	      }
	    }

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
	
	int wk;
	for(wk=0; wk<width.length; wk++) {
	  TableColumn tcol = cmodel.getColumn(wk);
	  tcol.setCellRenderer(renderers[wk]);

	  if(pEditors[wk] != null) 
	    tcol.setCellEditor(pEditors[wk]);

	  tcol.setMinWidth(width[wk]);
	  tcol.setPreferredWidth(width[wk]);
	  tcol.setMaxWidth(width[wk]);
	}
      }
      
      table.setPreferredScrollableViewportSize(new Dimension(total, 300));
      table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }

    {
      JScrollPane scroll = new JScrollPane();
      pTableScroll = scroll;

      scroll.setViewportView(pTable);
      
      scroll.setHorizontalScrollBarPolicy
	(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      scroll.setVerticalScrollBarPolicy
	(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

      scroll.getHorizontalScrollBar().addAdjustmentListener(this);

      add(scroll);
    }
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
      int wk;
      for(wk=0; wk<pEditors.length; wk++) {
	if(pEditors[wk] != null)
	  pEditors[wk].stopCellEditing();
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
      int wk;
      for(wk=0; wk<pEditors.length; wk++) {
	if(pEditors[wk] != null)
	  pEditors[wk].cancelCellEditing();
      }
    }
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



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Sort the rows by the values in the given column number. 
   */ 
  protected void 
  doSort
  (
   int col
  )
  { 
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
   * The table model.
   */ 
  private SortableTableModel pTableModel;

  /**
   * The table.
   */ 
  private JTable  pTable;

  /**
   * The cell editors.
   */ 
  private TableCellEditor  pEditors[];

  /**
   * The scroll pane containing the table.
   */ 
  private JScrollPane  pTableScroll;

}
