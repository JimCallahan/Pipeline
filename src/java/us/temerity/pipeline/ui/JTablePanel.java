// $Id: JTablePanel.java,v 1.1 2004/06/08 03:06:36 jim Exp $

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
  implements ActionListener
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
   * @param resize
   *   Whether each table column is resizeable.
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
   boolean resize[], 
   TableCellRenderer renderers[], 
   TableCellEditor editors[]
  ) 
  {
    assert(model.getColumnCount() == width.length);
    assert(width.length == resize.length);
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
      Box hbox = new Box(BoxLayout.X_AXIS);

      int wk;
      for(wk=0; wk<width.length; wk++) {
	{
	  JButton btn = new JButton(pTableModel.getColumnName(wk));
	  btn.setName("TableHeaderButton");

	  {
	    int extra = 0;
	    if((wk == 0) || (wk == (width.length-1)))
	      extra = 3;
	    
	    Dimension size = new Dimension(width[wk]+extra, 25);
	    btn.setMinimumSize(size);
	    btn.setPreferredSize(size);
	    
	    if(!resize[wk]) 
	      btn.setMaximumSize(size);
	    else 
	      btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
	  }

	  btn.addActionListener(this);
	  btn.setActionCommand("sort-column:" + wk);	  

	  hbox.add(btn);
	}

	if(wk<(width.length-1))
	  hbox.add(Box.createRigidArea(new Dimension(2, 0)));
      }

      hbox.add(Box.createRigidArea(new Dimension(17, 0)));
	
      add(hbox);
    }

    add(Box.createRigidArea(new Dimension(0, 1)));

    {
      JTable table = new JTable(pTableModel);
      pTable = table;

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

	  if(!resize[wk]) 
	    tcol.setMaxWidth(width[wk]);
	}
      }
    }

    {
      JScrollPane scroll = new JScrollPane(pTable);
      
      pTable.setPreferredScrollableViewportSize(new Dimension(total, 300));
      
      scroll.setHorizontalScrollBarPolicy
	(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      scroll.setVerticalScrollBarPolicy
	(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
      
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
    stopEditing();
    pTableModel.sortByColumn(col);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6732073412889969988L;
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

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

}
