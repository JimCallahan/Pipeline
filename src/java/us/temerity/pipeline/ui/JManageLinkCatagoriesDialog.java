// $Id: JManageLinkCatagoriesDialog.java,v 1.1 2004/06/28 23:39:45 jim Exp $

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
/*   M A N A G E   L I N K   C A T A G O R I E S   D I A L O G                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A dialog for managing the link catagories.
 */ 
public 
class JManageLinkCatagoriesDialog
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
  JManageLinkCatagoriesDialog() 
  {
    super("Manage Link Catagories", false);

    /* create dialog body components */ 
    { 
      JPanel body = new JPanel();
      body.setName("MainDialogPanel");

      body.setLayout(new BorderLayout());

      {
	LinkCatagoryDescTableModel model = new LinkCatagoryDescTableModel();
	pTableModel = model;

	JTablePanel tpanel =
	  new JTablePanel(model, model.getColumnWidths(), 
			  model.getRenderers(), model.getEditors());
	pTablePanel = tpanel;

	body.add(tpanel);
      }

      String extra[][] = {
	{ "Add", "add", },
	null, 
	{ "Activate", "activate", },
	{ "Deactivate", "deactivate", }
      };
      
      JButton btns[] = 
	super.initUI("Manage Link Catagories:", false, body, null, null, extra, "Close");
      
      pAddButton        = btns[0]; 
      pActivateButton   = btns[2];     
      pDeactivateButton = btns[3];

      updateAll();
      pack();
    }

    pCreateDialog = new JCreateLinkCatagoryDialog(this);
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Update all of UI components to reflect the current state of the link catagories.
   */ 
  public void 
  updateAll() 
  {
    UIMaster master = UIMaster.getInstance();
    try {      
      MasterMgrClient client = master.getMasterMgrClient();

      pIsPrivileged = client.isPrivileged(false);
      pAddButton.setEnabled(pIsPrivileged);
      pActivateButton.setEnabled(pIsPrivileged);
      pDeactivateButton.setEnabled(pIsPrivileged);

      TreeMap<String,LinkCatagoryDesc> table = client.getLinkCatagoryDesc();
      TreeSet<String> active = client.getActiveLinkCatagoryNames();

      pTableModel.updateAll(table, active);
    }
    catch(PipelineException ex) {
      master.showErrorDialog(ex);
    }
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
      doAddLinkCatagory();
    else if(cmd.equals("activate")) 
      doSetActivate(true);
    else if(cmd.equals("deactivate")) 
      doSetActivate(false);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Add a link catagory.
   */ 
  public void 
  doAddLinkCatagory()
  {
    pCreateDialog.setVisible(true);
    
    if(pCreateDialog.wasConfirmed()) {
      String name = pCreateDialog.getName();
      LinkPolicy policy = pCreateDialog.getPolicy();

      String desc = pCreateDialog.getDescription();
      assert((desc != null) && (desc.length() > 0));
      
      UIMaster master = UIMaster.getInstance();
      try {
	master.getMasterMgrClient().createLinkCatagory(name, policy, desc);
      }
      catch(PipelineException ex) {
	master.showErrorDialog(ex);
	return;
      }
      
      updateAll();
    }
  }

  /**
   * Activate/Deactivate the link catagories for the selected rows.
   */ 
  public void 
  doSetActivate
  (
   boolean isActive
  ) 
  {
    UIMaster master = UIMaster.getInstance();
    try {
      MasterMgrClient client = master.getMasterMgrClient();

      int rows[] = pTablePanel.getTable().getSelectedRows();
      for(String name : pTableModel.getRowNames(rows)) 
	client.setLinkCatagoryActive(name, isActive);
    }
    catch(PipelineException ex) {
      master.showErrorDialog(ex);
      return;
    }
    
    updateAll();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5122535062084505911L;
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Does the current user have privileged status?
   */ 
  private boolean  pIsPrivileged;


  /**
   * The link catagories table model.
   */ 
  private LinkCatagoryDescTableModel   pTableModel;

  /**
   * The editors table panel.
   */ 
  private JTablePanel  pTablePanel;


  /**
   * The dialog buttons.
   */ 
  private JButton pAddButton;
  private JButton pActivateButton;
  private JButton pDeactivateButton;

  /**
   * The dialog for creating new link catagories.
   */ 
  private JCreateLinkCatagoryDialog  pCreateDialog;

}
