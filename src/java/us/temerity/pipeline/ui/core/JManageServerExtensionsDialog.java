// $Id: JManageServerExtensionsDialog.java,v 1.4 2009/08/19 23:53:51 jim Exp $

package us.temerity.pipeline.ui.core;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   M A N A G E   S E R V E R   E X T E N S I O N S   D I A L O G                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A dialog for managing server extension configurations.
 */ 
public 
class JManageServerExtensionsDialog
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
  JManageServerExtensionsDialog() 
  {
    super("Manage Server Extensions");

    pPrivilegeDetails = new PrivilegeDetails();

    /* create dialog body components */ 
    { 
      setLayout(new BorderLayout());

      JTabbedPanel tab = new JTabbedPanel();
      pTab = tab;

      /* master extensions panel */ 
      {
	JPanel body = new JPanel();
	body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));  

	{
	  JPanel panel = new JPanel();
	  panel.setName("DialogHeader");	
	  
	  panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
	  
	  {
	    JLabel label = new JLabel("Manage Master Manager Extensions:");
	    label.setName("DialogHeaderLabel");	
	    
	    panel.add(label);	  
	  }
	  
	  panel.add(Box.createHorizontalGlue());

	  body.add(panel);
	}

	{ 
	  JPanel panel = new JPanel();
 	  panel.setName("MainPanel");
	  panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
         
          {
            MasterExtensionConfigsTableModel model = 
              new MasterExtensionConfigsTableModel(this); 
            pMasterTableModel = model;
	      
            JTablePanel tpanel = new JTablePanel(model);
            pMasterTablePanel = tpanel;
	    
            ListSelectionModel smodel = tpanel.getTable().getSelectionModel();
            smodel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

	    panel.add(tpanel);
	  }

	  body.add(panel);
	}

	tab.addTab(body);
      }
      
      /* queue extensions panel */ 
      {
	JPanel body = new JPanel();
	body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));  

	{
	  JPanel panel = new JPanel();
	  panel.setName("DialogHeader");	
	  
	  panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
	  
	  {
	    JLabel label = new JLabel("Manage Queue Manager Extensions:");
	    label.setName("DialogHeaderLabel");	
	    
	    panel.add(label);	  
	  }
	  
	  panel.add(Box.createHorizontalGlue());

	  body.add(panel);
	}

	{ 
	  JPanel panel = new JPanel();
 	  panel.setName("MainPanel");
	  panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
         
          {
            QueueExtensionConfigsTableModel model = 
              new QueueExtensionConfigsTableModel(this); 
            pQueueTableModel = model;
	      
            JTablePanel tpanel = new JTablePanel(model);
            pQueueTablePanel = tpanel;
            
            ListSelectionModel smodel = tpanel.getTable().getSelectionModel();
            smodel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

	    panel.add(tpanel);
	  }

	  body.add(panel);
	}

	tab.addTab(body);
      }

      String extra[][] = {
	{ "Add",    "add" }, 
	{ "Edit",   "edit" },
	{ "Remove", "remove" },
	null,
	{ "Update", "update" }
      };

      JButton btns[] = super.initUI(null, tab, null, null, extra, "Close");
      pAddButton    = btns[0];
      pEditButton   = btns[1];
      pRemoveButton = btns[2];

      updateAll();
      pack();
    }

    pMasterDetailsDialog = new JMasterExtensionConfigDialog(this);
    pQueueDetailsDialog  = new JQueueExtensionConfigDialog(this);
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the current master extension configurations and update the UI components.
   */ 
  public void 
  updateAll() 
  { 
    UIMaster master = UIMaster.getInstance();
    MasterMgrClient mclient = master.acquireMasterMgrClient();
    QueueMgrClient qclient = master.acquireQueueMgrClient();
    try {
      pPrivilegeDetails = mclient.getPrivilegeDetails();
      pMasterTableModel.setMasterExtensionConfigs
	(mclient.getMasterExtensionConfigs(), pPrivilegeDetails);

      pQueueTableModel.setQueueExtensionConfigs
	(qclient.getQueueExtensionConfigs(), pPrivilegeDetails);
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
    }
    finally {
      master.releaseMasterMgrClient(mclient);
      master.releaseQueueMgrClient(qclient);
    }

    pAddButton.setEnabled(pPrivilegeDetails.isMasterAdmin());
    pEditButton.setEnabled(pPrivilegeDetails.isMasterAdmin());
    pRemoveButton.setEnabled(pPrivilegeDetails.isMasterAdmin());
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
    else if(cmd.equals("edit")) 
      doEdit();
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
   * Add an extension. 
   */ 
  private void 
  doAdd()
  {
    UIMaster master = UIMaster.getInstance();
    switch(pTab.getSelectedIndex()) {    
    case 0:
      {
	pMasterDetailsDialog.newMasterExtensionConfig();
	pMasterDetailsDialog.setVisible(true);
      
	if(pMasterDetailsDialog.wasConfirmed()) {
	  MasterExtensionConfig config = pMasterDetailsDialog.getMasterExtensionConfig(); 
	  if(config != null) {
	    MasterMgrClient mclient = master.acquireMasterMgrClient();
	    try {
	      mclient.setMasterExtensionConfig(config);
	      pMasterTableModel.setMasterExtensionConfigs
		(mclient.getMasterExtensionConfigs(), pPrivilegeDetails);
	    }
	    catch(PipelineException ex) {
	      showErrorDialog(ex);
	    }
	    finally {
	      master.releaseMasterMgrClient(mclient);
	    }
	  }
	}
      }
      break;

    case 1:
      {
	pQueueDetailsDialog.newQueueExtensionConfig();
	pQueueDetailsDialog.setVisible(true);
      
	if(pQueueDetailsDialog.wasConfirmed()) {
	  QueueExtensionConfig config = pQueueDetailsDialog.getQueueExtensionConfig(); 
	  if(config != null) {
	    QueueMgrClient qclient = master.acquireQueueMgrClient();
	    try {
	      qclient.setQueueExtensionConfig(config);
	      pQueueTableModel.setQueueExtensionConfigs
		(qclient.getQueueExtensionConfigs(), pPrivilegeDetails);
	    }
	    catch(PipelineException ex) {
	      showErrorDialog(ex);
	    }
	    finally {
	      master.releaseQueueMgrClient(qclient);
	    }
	  }
	}
      }
    }
  }
  
  /**
   * Edit an existing extension.
   */ 
  private void 
  doEdit()
  {
    UIMaster master = UIMaster.getInstance();
    switch(pTab.getSelectedIndex()) {    
    case 0:
      {
	int row = pMasterTablePanel.getTable().getSelectedRow();
	if(row == -1) 
	  return;

	MasterExtensionConfig oconfig = pMasterTableModel.getMasterExtensionConfig(row); 
	pMasterDetailsDialog.setMasterExtensionConfig(oconfig); 
	pMasterDetailsDialog.setVisible(true);
	
	if(pMasterDetailsDialog.wasConfirmed()) {
	  MasterExtensionConfig config = pMasterDetailsDialog.getMasterExtensionConfig(); 
	  if(config != null) {
	    MasterMgrClient mclient = master.acquireMasterMgrClient();
	    try {
	      if((oconfig != null) && !oconfig.getName().equals(config.getName()))
		mclient.removeMasterExtensionConfig(oconfig.getName());
	      
	      mclient.setMasterExtensionConfig(config);
	      pMasterTableModel.setMasterExtensionConfigs
		(mclient.getMasterExtensionConfigs(), pPrivilegeDetails);
	    }
	    catch(PipelineException ex) {
	      showErrorDialog(ex);
	    }
	    finally {
	      master.releaseMasterMgrClient(mclient);
	    }
	  }
	}
      }
      break;   

    case 1:
      {
	int row = pQueueTablePanel.getTable().getSelectedRow();
	if(row == -1) 
	  return;

	QueueExtensionConfig oconfig = pQueueTableModel.getQueueExtensionConfig(row); 
	pQueueDetailsDialog.setQueueExtensionConfig(oconfig); 
	pQueueDetailsDialog.setVisible(true);
	
	if(pQueueDetailsDialog.wasConfirmed()) {
	  QueueExtensionConfig config = pQueueDetailsDialog.getQueueExtensionConfig(); 
	  if(config != null) {
	    QueueMgrClient qclient = master.acquireQueueMgrClient();
	    try {

	      if((oconfig != null) && !oconfig.getName().equals(config.getName()))
		qclient.removeQueueExtensionConfig(oconfig.getName());
	      
	      qclient.setQueueExtensionConfig(config);
	      pQueueTableModel.setQueueExtensionConfigs
		(qclient.getQueueExtensionConfigs(), pPrivilegeDetails);
	    }
	    catch(PipelineException ex) {
	      showErrorDialog(ex);
	    }
	    finally {
	      master.releaseQueueMgrClient(qclient);
	    }
	  }
	}
      }
    }
  }
  
  /**
   * Upload edits to the table to the server.
   */ 
  public void 
  doEdited()
  {
    UIMaster master = UIMaster.getInstance();
    try {
      switch(pTab.getSelectedIndex()) {    
      case 0:
	{
	  MasterMgrClient mclient = master.acquireMasterMgrClient();

	  try {
	    TreeMap<String,MasterExtensionConfig> configs = 
	      pMasterTableModel.getModifiedMasterExtensionConfigs();
	    for(MasterExtensionConfig config : configs.values())
	      mclient.setMasterExtensionConfig(config);

	    pMasterTableModel.setMasterExtensionConfigs
	      (mclient.getMasterExtensionConfigs(), pPrivilegeDetails);
	  }
	  finally {
	    master.releaseMasterMgrClient(mclient);
	  }
	}
	break;

      case 1:
	{
	  QueueMgrClient qclient = master.acquireQueueMgrClient();

	  try {
	    TreeMap<String,QueueExtensionConfig> configs = 
	      pQueueTableModel.getModifiedQueueExtensionConfigs();
	    for(QueueExtensionConfig config : configs.values())
	      qclient.setQueueExtensionConfig(config);

	    pQueueTableModel.setQueueExtensionConfigs
	      (qclient.getQueueExtensionConfigs(), pPrivilegeDetails);
	  }
	  finally {
	    master.releaseQueueMgrClient(qclient);
	  }
	}
      }
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
    }
  }
  
  /**
   * Remove the selected rows.
   */ 
  private void 
  doRemove() 
  {
    UIMaster master = UIMaster.getInstance();
    switch(pTab.getSelectedIndex()) {    
    case 0:
      {
	int row = pMasterTablePanel.getTable().getSelectedRow();
	if(row == -1) 
	  return;
    
	MasterExtensionConfig config = pMasterTableModel.getMasterExtensionConfig(row);
	if(config != null) {
	  JConfirmDialog diag = new JConfirmDialog(this, "Are you sure?"); 
	  diag.setVisible(true);
	  if(diag.wasConfirmed()) {
	    MasterMgrClient mclient = master.acquireMasterMgrClient();
	    try {
	      mclient.removeMasterExtensionConfig(config.getName());
	      pMasterTableModel.setMasterExtensionConfigs
		(mclient.getMasterExtensionConfigs(), pPrivilegeDetails);
	    }
	    catch(PipelineException ex) {
	      showErrorDialog(ex);
	    }
	    finally {
	      master.releaseMasterMgrClient(mclient);
	    }
	  }
	}
      }
      break;   
      
    case 1:
      {
	int row = pQueueTablePanel.getTable().getSelectedRow();
	if(row == -1) 
	  return;

	QueueExtensionConfig config = pQueueTableModel.getQueueExtensionConfig(row);
	if(config != null) {
	  JConfirmDialog diag = new JConfirmDialog(this, "Are you sure?"); 
	  diag.setVisible(true);
	  if(diag.wasConfirmed()) {
	    QueueMgrClient qclient = master.acquireQueueMgrClient();
	    try {
	      qclient.removeQueueExtensionConfig(config.getName());
	      pQueueTableModel.setQueueExtensionConfigs
		(qclient.getQueueExtensionConfigs(), pPrivilegeDetails);
	    }
	    catch(PipelineException ex) {
	      showErrorDialog(ex);
	    }
	    finally {
	      master.releaseQueueMgrClient(qclient);
	    }
	  }
	}
      }
    }
  }

  /*
   * Update the table with the current extensions.
   */ 
  private void 
  doUpdate() 
  {
    updateAll();
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


  /*----------------------------------------------------------------------------------------*/

  /**
   * The master extension configs table model.
   */ 
  private MasterExtensionConfigsTableModel  pMasterTableModel;

  /**
   * The master extension configs table panel.
   */ 
  private JTablePanel  pMasterTablePanel;

  /**
   * The add/edit configuration dialog.
   */ 
  private JMasterExtensionConfigDialog  pMasterDetailsDialog; 


  /**
   * The queue extension configs table model.
   */ 
  private QueueExtensionConfigsTableModel  pQueueTableModel;

  /**
   * The queue extension configs table panel.
   */ 
  private JTablePanel  pQueueTablePanel;

  /**
   * The add/edit configuration dialog.
   */ 
  private JQueueExtensionConfigDialog  pQueueDetailsDialog; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The containing tabbed panel.
   */ 
  private JTabbedPanel  pTab; 

  /**
   * The panel buttons.
   */ 
  private JButton  pAddButton;
  private JButton  pEditButton;
  private JButton  pRemoveButton;

}
