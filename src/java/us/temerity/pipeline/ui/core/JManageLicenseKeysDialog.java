// $Id: JManageLicenseKeysDialog.java,v 1.9 2007/12/16 12:22:09 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.JTablePanel;
import us.temerity.pipeline.ui.JTopLevelDialog;

/*------------------------------------------------------------------------------------------*/
/*   M A N A G E   L I C E N S E   K E Y S   D I A L O G                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A dialog for adding, removing and changing the number of licenses associated with 
 * license keys.
 */ 
public 
class JManageLicenseKeysDialog
  extends JTopLevelDialog
  implements ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JManageLicenseKeysDialog() 
  {
    super("Manage License Keys");

    pPrivilegeDetails = new PrivilegeDetails();

    /* create dialog body components */ 
    { 
      JPanel body = new JPanel();
      body.setName("MainDialogPanel");

      body.setLayout(new BorderLayout());

      {
	LicenseKeysTableModel model = new LicenseKeysTableModel(this);
	pTableModel = model;

	JTablePanel tpanel = new JTablePanel(model);
	pTablePanel = tpanel;

	body.add(tpanel);
      }
    
      String extra[][] = {
	null,
	{ "Add",    "add" }, 
	{ "Remove", "remove" },
	{ "Update", "update" },
	{ "Edit", "edit" }
      };

      JButton btns[] = 
	super.initUI("Manage License Keys:", body, "Confirm", "Apply", extra, "Close");

      pAddButton    = btns[1];
      pRemoveButton = btns[2];
      pEditButton   = btns[4];

      updateLicenseKeys();
      pack();
    }

    pCreateDialog     = new JCreateLicenseKeyDialog(this);
    pKeyDetailsDialog = new JKeyChooserConfigDialog(this, "License");
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the current license keys and update the UI components.
   */ 
  public void 
  updateLicenseKeys() 
  { 
    UIMaster master = UIMaster.getInstance();
    MasterMgrClient mclient = master.getMasterMgrClient();
    QueueMgrClient qclient = master.getQueueMgrClient();
    try {
      pPrivilegeDetails = mclient.getPrivilegeDetails();
      pTableModel.setLicenseKeys(qclient.getLicenseKeys(), pPrivilegeDetails);
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
    }

    pAddButton.setEnabled(pPrivilegeDetails.isQueueAdmin());
    pRemoveButton.setEnabled(pPrivilegeDetails.isQueueAdmin());
    pConfirmButton.setEnabled(false);
    pApplyButton.setEnabled(false);
    pEditButton.setEnabled(pPrivilegeDetails.isQueueAdmin());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- ACTION LISTENER METHODS -------------------------------------------------------------*/

  /** 
   * Invoked when an action occurs. 
   */ 
  @Override
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
    else if(cmd.equals("edit")) 
      doEdit();
    else 
      super.actionPerformed(e);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Apply changes and close. 
   */ 
  @Override
  public void 
  doConfirm()
  {
    setTotalKeys();
    super.doConfirm();
  }
  
  /**
   * Apply changes. 
   */ 
  @Override
  public void 
  doApply()
  {
    setTotalKeys();
    doUpdate();
  }

  /**
   * Set the total keys for all license keys in the table.
   */ 
  private void 
  setTotalKeys() 
  {
    UIMaster master = UIMaster.getInstance();
    QueueMgrClient client = master.getQueueMgrClient();
    try {
      int wk;
      for(wk=0; wk<pTableModel.getRowCount(); wk++) {
	LicenseKey key = pTableModel.getLicenseKey(wk);
	if(key != null) 
	  client.setMaxLicenses(key.getName(), key.getScheme(), 
				key.getMaxSlots(), key.getMaxHosts(), key.getMaxHostSlots());
      }
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
    }
  }

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
	  LicenseKey key = new LicenseKey(kname, desc, LicenseScheme.PerSlot, 0, null, null);
	  client.addLicenseKey(key);
			       
	  pTableModel.setLicenseKeys(client.getLicenseKeys(), pPrivilegeDetails);
	}
	catch(PipelineException ex) {
	  showErrorDialog(ex);
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
	String kname = pTableModel.getName(rows[wk]);
	client.removeLicenseKey(kname);
      }

      pTableModel.setLicenseKeys(client.getLicenseKeys(), pPrivilegeDetails);
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
    }
  }

  /**
   * Update the table with the current license keys.
   */ 
  private void 
  doUpdate() 
  {
    pTablePanel.cancelEditing();
    updateLicenseKeys();
  }
  
  /**
   * Launch a dialog to add or remove a KeyChooser plugin to the key.
   */
  private void
  doEdit()
  {
    UIMaster master = UIMaster.getInstance();
    int row = pTablePanel.getTable().getSelectedRow();
    if(row == -1) 
      return;

    BaseKeyChooser oplugin = pTableModel.getKeyChooser(row);
    pKeyDetailsDialog.setKeyChooser(oplugin);
    pKeyDetailsDialog.setVisible(true);
    
    if(pKeyDetailsDialog.wasConfirmed()) {
      BaseKeyChooser plugin = pKeyDetailsDialog.getKeyChooser();
      LicenseKey key = pTableModel.getKey(row);
      if(key != null) {
        try {
          QueueMgrClient qclient = master.getQueueMgrClient();
          LicenseKey newKey = 
            new LicenseKey(key.getName(), key.getDescription(), plugin,
                           key.getScheme(), key.getMaxSlots(), key.getMaxHosts(), key.getMaxHostSlots()); 
          qclient.addLicenseKey(newKey);
          
          pTableModel.setLicenseKeys(qclient.getLicenseKeys(), pPrivilegeDetails);
        }
        catch(PipelineException ex) {
          showErrorDialog(ex);
        }
      }
    }
  }

  /**
   * Enable the Confirm/Apply buttons in response to a license key being edited.
   */ 
  public void 
  doEdited() 
  {
    pConfirmButton.setEnabled(true);
    pApplyButton.setEnabled(true);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3973876541330561316L;
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The details of the administrative privileges granted to the current user. 
   */ 
  private PrivilegeDetails  pPrivilegeDetails; 


  /**
   * The license keys table model.
   */ 
  private LicenseKeysTableModel  pTableModel;

  /**
   * The license keys table panel.
   */ 
  private JTablePanel  pTablePanel;

  /**
   * The panel buttons.
   */ 
  private JButton  pAddButton;
  private JButton  pRemoveButton;
  private JButton  pEditButton;

  /**
   * The new key creation dialog.
   */ 
  private JCreateLicenseKeyDialog  pCreateDialog; 
  
  /**
   * The add/edit configuration dialog.
   */ 
  private JKeyChooserConfigDialog  pKeyDetailsDialog; 
}
