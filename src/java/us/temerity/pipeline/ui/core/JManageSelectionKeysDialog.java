// $Id: JManageSelectionKeysDialog.java,v 1.4 2005/03/18 16:33:53 jim Exp $

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
/*   M A N A G E   S E L E C T I O N   K E Y S   D I A L O G                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A dialog for adding, removing and changing the number of selections associated with 
 * selection keys.
 */ 
public 
class JManageSelectionKeysDialog
  extends JBaseDialog
  implements ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JManageSelectionKeysDialog() 
  {
    super("Manage Selection Keys", false);

    /* create dialog body components */ 
    { 
      JPanel body = new JPanel();
      body.setName("MainDialogPanel");

      body.setLayout(new BorderLayout());

      {
	SelectionKeysTableModel model = new SelectionKeysTableModel();
	pTableModel = model;

	JTablePanel tpanel = new JTablePanel(model);
	pTablePanel = tpanel;

	body.add(tpanel);
      }
    
      String extra[][] = {
	{ "Add",    "add" }, 
	{ "Remove", "remove" },
	{ "Update", "update" }
      };

      JButton btns[] = super.initUI("Manage Selection Keys:", false, body, 
				    null, null, extra, "Close");
      pAddButton    = btns[0];
      pRemoveButton = btns[1];

      updateSelectionKeys();
      pack();
    }

    pCreateDialog = new JCreateSelectionKeyDialog(this);
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the current selection keys and update the UI components.
   */ 
  public void 
  updateSelectionKeys() 
  { 
    UIMaster master = UIMaster.getInstance();
    QueueMgrClient client = master.getQueueMgrClient();
    try {
      pIsPrivileged = client.isPrivileged(false);
    }
    catch(PipelineException ex) {
      master.showErrorDialog(ex);
    }

    doUpdate();

    pAddButton.setEnabled(pIsPrivileged);
    pRemoveButton.setEnabled(pIsPrivileged);
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
    else if(cmd.equals("update")) 
      doUpdate();
    else 
      super.actionPerformed(e);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Add a filename suffix to the table.
   */ 
  private void 
  doAdd()
  {
    pTablePanel.stopEditing();

    pCreateDialog.setVisible(true);
      
    if(pCreateDialog.wasConfirmed()) {
      String kname = pCreateDialog.getKeyName();
      String desc  = pCreateDialog.getDescription();
      if((kname != null) && (kname.length() > 0) &&
	 (desc != null) && (desc.length() > 0)) {

	UIMaster master = UIMaster.getInstance();
	QueueMgrClient client = master.getQueueMgrClient();
	try {
	  client.addSelectionKey(new SelectionKey(kname, desc));
	  pTableModel.setSelectionKeys(client.getSelectionKeys());
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	}
      }
    }
  }
  
  /**
   * Remove the selected rows.
   */ 
  private void 
  doRemove() 
  {
    pTablePanel.cancelEditing();

    UIMaster master = UIMaster.getInstance();
    QueueMgrClient client = master.getQueueMgrClient();
    try {
      int rows[] = pTablePanel.getTable().getSelectedRows();
      int wk;
      for(wk=0; wk<rows.length; wk++) {
	String kname = (String) pTableModel.getValueAt(rows[wk], 0);
	client.removeSelectionKey(kname);
      }

      pTableModel.setSelectionKeys(client.getSelectionKeys());
    }
    catch(PipelineException ex) {
      master.showErrorDialog(ex);
    }
  }

  /*
   * Update the table with the current selection keys.
   */ 
  private void 
  doUpdate() 
  {
    pTablePanel.cancelEditing();

    UIMaster master = UIMaster.getInstance();
    QueueMgrClient client = master.getQueueMgrClient();
    try {
      pTableModel.setSelectionKeys(client.getSelectionKeys());
    }
    catch(PipelineException ex) {
      master.showErrorDialog(ex);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 252766954284232892L;
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Does the current user have privileged status?
   */ 
  private boolean  pIsPrivileged;

  /**
   * The selection keys table model.
   */ 
  private SelectionKeysTableModel  pTableModel;

  /**
   * The selection keys table panel.
   */ 
  private JTablePanel  pTablePanel;

  /**
   * The panel buttons.
   */ 
  private JButton  pAddButton;
  private JButton  pRemoveButton;

  /**
   * The new key creation dialog.
   */ 
  private JCreateSelectionKeyDialog pCreateDialog; 
}
