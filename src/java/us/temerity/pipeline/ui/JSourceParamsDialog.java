// $Id: JSourceParamsDialog.java,v 1.1 2004/06/22 19:44:54 jim Exp $

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
/*   S O U R C E   P A R A M S   D I A L O G                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A dialog for editing/viewing per-source action parameters.
 */ 
public 
class JSourceParamsDialog
  extends JBaseDialog
  implements ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   * 
   * @param isEditable
   *   Should the dialog allow editing of parameter values?
   * 
   * @param title
   *   The short name of the parent node.
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
  JSourceParamsDialog
  (
   boolean isEditable, 
   String title, 
   ArrayList<String> stitles, 
   ArrayList<String> snames, 
   BaseAction action    
  )    
  {
    super(isEditable ? "Edit Source Parameters" : "View Source Parameters", true);

    /* create dialog body components */ 
    { 
      JPanel body = new JPanel();
      body.setName("MainDialogPanel");

      body.setLayout(new BorderLayout());

      {
	SourceParamsTableModel model = 
	  new SourceParamsTableModel(isEditable, stitles, snames, action);
	pTableModel = model;

	JTablePanel tpanel = 
	  new JTablePanel(model, model.getColumnWidths(), 
			  model.getRenderers(), model.getEditors());
	pTablePanel = tpanel;

	{
	  int total = 3;
	  int widths[] = model.getColumnWidths();
	  int wk;
	  for(wk=0; wk<widths.length; wk++)
	    total += widths[wk] + 3;
	  
	  if(total > 1000)
	    tpanel.getTable().setPreferredScrollableViewportSize(new Dimension(1000, 300));
	}

	body.add(tpanel);
      }
    
      String header = ("View Source Parameters:" + "  " + title);
      String confirm = null;
      String extra[][] = null;
      String cancel = "Close";
      if(isEditable) {
	header = ("Edit Source Parameters:" + title);

	confirm = "Confirm";

	String str[][] = {
	  null,
	  { "Add",    "add" }, 
	  { "Remove", "remove" }
	};
	extra = str;

	cancel = "Cancel";
      }
	 
      super.initUI(header, true, body, confirm, null, extra, cancel);

      pack();
    }
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
    pTableModel.updateParams(action);
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
    super.actionPerformed(e);

    String cmd = e.getActionCommand();
    if(cmd.equals("add")) 
      doAdd();
    else if(cmd.equals("remove")) 
      doRemove();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Add a filename suffix to the table.
   */ 
  public void 
  doAdd()
  {
    pTablePanel.stopEditing();

    pTableModel.addRowParams(pTablePanel.getTable().getSelectedRows());
  }

  /**
   * Remove the selected rows.
   */ 
  public void 
  doRemove() 
  {
    pTablePanel.cancelEditing();

    pTableModel.removeRowParams(pTablePanel.getTable().getSelectedRows());
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6125997753088879955L;
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parameters table model.
   */ 
  private SourceParamsTableModel pTableModel;

  /**
   * The table panel.
   */ 
  private JTablePanel  pTablePanel;

}
