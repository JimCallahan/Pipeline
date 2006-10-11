// $Id: JManageServerExtensionsDialog.java,v 1.1 2006/10/11 22:45:41 jim Exp $

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
      pTab = new JTabbedPanel();

      /* master extensions panel */ 
      {
	JPanel body = new JPanel();
	body.setLayout(new BorderLayout());

	{ 
	  JPanel main = new JPanel();
 	  main.setName("MainDialogPanel");
	  main.setLayout(new BoxLayout(main, BoxLayout.X_AXIS));

	  {
	    Box vbox = new Box(BoxLayout.Y_AXIS);
	    
	    vbox.add(UIFactory.createPanelLabel("Master Manager Extensions:"));
	    
	    vbox.add(Box.createRigidArea(new Dimension(0, 4)));
	    
	    {
	      MasterExtensionConfigsTableModel model = 
		new MasterExtensionConfigsTableModel(this); 
	      pMasterTableModel = model;
	      
	      JTablePanel tpanel = new JTablePanel(model);
	      pMasterTablePanel = tpanel;
	    
	      ListSelectionModel smodel = tpanel.getTable().getSelectionModel();
	      smodel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	      
	      vbox.add(tpanel);
	    }
	    
	    main.add(vbox);
	  }

	  body.add(main);
	}

	pTab.add(body);
      }

      /* queue extensions panel */ 
      {
	JPanel body = new JPanel();
	body.setLayout(new BorderLayout());

	{ 
	  JPanel main = new JPanel();
 	  main.setName("MainDialogPanel");
	  main.setLayout(new BoxLayout(main, BoxLayout.X_AXIS));

	  {
	    Box vbox = new Box(BoxLayout.Y_AXIS);
	    
	    vbox.add(UIFactory.createPanelLabel("Queue Manager Extensions:"));
	    
	    vbox.add(Box.createRigidArea(new Dimension(0, 4)));
	    
	    {
	      QueueExtensionConfigsTableModel model = 
		new QueueExtensionConfigsTableModel(this); 
	      pQueueTableModel = model;
	      
	      JTablePanel tpanel = new JTablePanel(model);
	      pQueueTablePanel = tpanel;
	    
	      ListSelectionModel smodel = tpanel.getTable().getSelectionModel();
	      smodel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	      
	      vbox.add(tpanel);
	    }
	    
	    main.add(vbox);
	  }

	  body.add(main);
	}

	pTab.add(body);
      }

      String extra[][] = {
	{ "Add",    "add" }, 
	{ "Edit",   "edit" },
	{ "Remove", "remove" },
	null,
	{ "Update", "update" }
      };

      JButton btns[] = super.initUI("Manage Server Extensions:", pTab, 
				    null, null, extra, "Close");
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
    try {
      MasterMgrClient mclient = master.getMasterMgrClient();
      pPrivilegeDetails = mclient.getPrivilegeDetails();
      pMasterTableModel.setMasterExtensionConfigs
	(mclient.getMasterExtensionConfigs(), pPrivilegeDetails);

      QueueMgrClient qclient = master.getQueueMgrClient();
      pQueueTableModel.setQueueExtensionConfigs
	(qclient.getQueueExtensionConfigs(), pPrivilegeDetails);
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
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
	    try {
	      MasterMgrClient mclient = master.getMasterMgrClient();
	      mclient.setMasterExtensionConfig(config);
	      pMasterTableModel.setMasterExtensionConfigs
		(mclient.getMasterExtensionConfigs(), pPrivilegeDetails);
	    }
	    catch(PipelineException ex) {
	      showErrorDialog(ex);
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
	    try {
	      QueueMgrClient qclient = master.getQueueMgrClient();
	      qclient.setQueueExtensionConfig(config);
	      pQueueTableModel.setQueueExtensionConfigs
		(qclient.getQueueExtensionConfigs(), pPrivilegeDetails);
	    }
	    catch(PipelineException ex) {
	      showErrorDialog(ex);
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
	    try {
	      MasterMgrClient mclient = master.getMasterMgrClient();
	
	      if((oconfig != null) && !oconfig.getName().equals(config.getName()))
		mclient.removeMasterExtensionConfig(oconfig.getName());
	      
	      mclient.setMasterExtensionConfig(config);
	      pMasterTableModel.setMasterExtensionConfigs
		(mclient.getMasterExtensionConfigs(), pPrivilegeDetails);
	    }
	    catch(PipelineException ex) {
	      showErrorDialog(ex);
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
	    try {
	      QueueMgrClient qclient = master.getQueueMgrClient();

	      if((oconfig != null) && !oconfig.getName().equals(config.getName()))
		qclient.removeQueueExtensionConfig(oconfig.getName());
	      
	      qclient.setQueueExtensionConfig(config);
	      pQueueTableModel.setQueueExtensionConfigs
		(qclient.getQueueExtensionConfigs(), pPrivilegeDetails);
	    }
	    catch(PipelineException ex) {
	      showErrorDialog(ex);
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
	  MasterMgrClient mclient = master.getMasterMgrClient();

	  TreeMap<String,MasterExtensionConfig> configs = 
	    pMasterTableModel.getModifiedMasterExtensionConfigs();
	  for(MasterExtensionConfig config : configs.values())
	    mclient.setMasterExtensionConfig(config);

	  pMasterTableModel.setMasterExtensionConfigs
	    (mclient.getMasterExtensionConfigs(), pPrivilegeDetails);
	}
	break;

      case 1:
	{
	  QueueMgrClient qclient = master.getQueueMgrClient();

	  TreeMap<String,QueueExtensionConfig> configs = 
	    pQueueTableModel.getModifiedQueueExtensionConfigs();
	  for(QueueExtensionConfig config : configs.values())
	    qclient.setQueueExtensionConfig(config);
	  
	  pQueueTableModel.setQueueExtensionConfigs
	    (qclient.getQueueExtensionConfigs(), pPrivilegeDetails);
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
	    try {
	      MasterMgrClient mclient = master.getMasterMgrClient();
	      mclient.removeMasterExtensionConfig(config.getName());
	      pMasterTableModel.setMasterExtensionConfigs
		(mclient.getMasterExtensionConfigs(), pPrivilegeDetails);
	    }
	    catch(PipelineException ex) {
	      showErrorDialog(ex);
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
	    try {
	      QueueMgrClient qclient = master.getQueueMgrClient();
	      qclient.removeQueueExtensionConfig(config.getName());
	      pQueueTableModel.setQueueExtensionConfigs
		(qclient.getQueueExtensionConfigs(), pPrivilegeDetails);
	    }
	    catch(PipelineException ex) {
	      showErrorDialog(ex);
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
