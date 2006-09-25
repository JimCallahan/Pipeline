// $Id: JSourceParamsDialog.java,v 1.6 2006/09/25 12:11:44 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

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
   * @param owner
   *   The parent frame.
   * 
   * @param isEditable
   *   Should the dialog allow editing of parameter values?
   * 
   * @param title
   *   The short name of the parent node.
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
  JSourceParamsDialog
  (
   Frame owner,      
   boolean isEditable, 
   String title, 
   ArrayList<String> snames, 
   ArrayList<String> stitles, 
   ArrayList<FileSeq> fseqs, 
   BaseAction action    
  )    
  {
    super(owner, isEditable ? "Edit Source Parameters" : "View Source Parameters");

    /* create dialog body components */ 
    { 
      JPanel body = new JPanel();
      body.setName("MainDialogPanel");

      body.setLayout(new BorderLayout());

      {
	SourceParamsTableModel model = 
	  new SourceParamsTableModel(this, isEditable, snames, stitles, fseqs, action);
	pTableModel = model;

	JTablePanel tpanel = new JTablePanel(model);
	pTablePanel = tpanel;

	{
	  int total = 3;
	  int col;
	  for(col=0; col<model.getColumnCount(); col++) 
	    total += model.getColumnWidth(col) + 3;

	  
	  if(total > 1000)
	    tpanel.getTable().setPreferredScrollableViewportSize(new Dimension(1000, 300));
	}

	body.add(tpanel);
      }
    
      String header = ("View Source Parameters:  " + title);
      String confirm = null;
      String extra[][] = null;
      String cancel = "Close";
      if(isEditable) {
	header = ("Edit Source Parameters:  " + title);

	confirm = "Confirm";

	String str[][] = {
	  null,
	  { "Add",    "add" }, 
	  { "Remove", "remove" }
	};
	extra = str;

	cancel = "Cancel";
      }
	 
      super.initUI(header, body, confirm, null, extra, cancel);

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
    String cmd = e.getActionCommand();
    if(cmd.equals("add")) 
      doAdd();
    else if(cmd.equals("remove")) 
      doRemove();
    else 
      super.actionPerformed(e);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Apply changes and close. 
   */ 
  public void 
  doConfirm() 
  {
    pTablePanel.stopEditing();
    super.doConfirm();
  }

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
