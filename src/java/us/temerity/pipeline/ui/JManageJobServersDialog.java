// $Id: JManageJobServersDialog.java,v 1.1 2004/07/28 19:22:50 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.core.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.text.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   M A N A G E   J O B   S E R V E R S   D I A L O G                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A dialog for monitoring, adding, removing and modifing the servers which execute jobs 
 * on behalf of the Pipeline queue.
 */ 
public  
class JManageJobServersDialog
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
  JManageJobServersDialog() 
  {
    super("Manage Job Servers", false);

    /* icreate dialog body components */ 
    {
      JPanel body = new JPanel();
      body.setName("MainDialogPanel");

      body.setLayout(new BorderLayout());
      
      {
	QueueHostsTableModel model = new QueueHostsTableModel(this);
	pTableModel = model;

	JTablePanel tpanel =
	  new JTablePanel(model, model.getColumnWidths(), 
			  model.getRenderers(), model.getEditors());
	pTablePanel = tpanel;

	body.add(tpanel);
      }

      String extra[][] = {
	null,
	{ "Add",    "add" }, 
	{ "Remove", "remove" },
	{ "Update", "update" }
      };

      JButton btns[] = super.initUI("Manage Job Servers:", false, body, 
				    "Confirm", "Apply", extra, "Close");
      pAddButton    = btns[1];
      pRemoveButton = btns[2];

      doUpdate();
      pack();
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the UI components to reflect current state of the job servers. 
   */
  public void 
  updateJobServers() 
  {
    doUpdate();
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
    else if(cmd.equals("update")) 
      doUpdate();
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
    EditHostsTask task = new EditHostsTask(false);
    task.start();

    super.doConfirm();
  }
  
  /**
   * Apply changes. 
   */ 
  public void 
  doApply()
  {
    EditHostsTask task = new EditHostsTask(true);
    task.start();
  }

  /**
   * Add a new server. 
   */ 
  private void 
  doAdd()
  {
    pTablePanel.cancelEditing();

    JNewJobServerDialog diag = new JNewJobServerDialog(this);
    diag.setVisible(true);
    
    if(diag.wasConfirmed()) {
      String hname = diag.getName();
      if((hname != null) && (hname.length() > 0)) {
	AddHostTask task = new AddHostTask(hname);
	task.start();    
      }
    }
  }

  /**
   * Remove the servers on the selected rows.
   */ 
  private void 
  doRemove() 
  {
    pTablePanel.cancelEditing();

    TreeSet<String> hostnames = new TreeSet<String>();
    int rows[] = pTablePanel.getTable().getSelectedRows();
    int wk;
    for(wk=0; wk<rows.length; wk++) {
      String hname = (String) pTableModel.getValueAt(rows[wk], 0);
      hostnames.add(hname);
    }
    
    if(!hostnames.isEmpty()) {
      RemoveHostsTask task = new RemoveHostsTask(hostnames);
      task.start();
    }
  }

  /*
   * Update the table with the server information.
   */ 
  private void 
  doUpdate() 
  {
    GetHostsTask task = new GetHostsTask();
    task.start();    
  }

  /**
   * Enable the Confirm/Apply buttons in response to a host being edited.
   */ 
  public void 
  doEdited() 
  {
    pConfirmButton.setEnabled(true);
    pApplyButton.setEnabled(true);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Get the current state of the job server hosts.
   */ 
  private
  class GetHostsTask
    extends Thread
  {
    public 
    GetHostsTask() 
    {
      super("JManageJobServersDialog:GetHostsTask");
    }
 
    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      TreeMap<String,QueueHost> hosts = null;
      TreeSet<String> keys = null;
      boolean isPrivileged = false;
      try {
	QueueMgrClient client = master.getQueueMgrClient();
	hosts = client.getHosts(); 
	keys = client.getSelectionKeyNames();
	
	isPrivileged = master.getMasterMgrClient().isPrivileged(false);
      }
      catch(PipelineException ex) {
	master.showErrorDialog(ex);
      }
	
      UpdateTask task = new UpdateTask(hosts, keys, isPrivileged);
      SwingUtilities.invokeLater(task);
    }
  }
  
  /** 
   * Add a new job server.
   */ 
  private
  class AddHostTask
    extends Thread
  {
    public 
    AddHostTask
    (
     String hostname
    ) 
    {
      super("JManageJobServersDialog:AddHostTask");

      pHostname = hostname;
    }
 
    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      try {
	QueueMgrClient client = master.getQueueMgrClient();
	client.addHost(pHostname);
      }
      catch(PipelineException ex) {
	master.showErrorDialog(ex);
      }
      
      GetHostsTask task = new GetHostsTask();
      task.start();
    }

    private String  pHostname; 
  }

  /** 
   * Remove a new job server.
   */ 
  private
  class RemoveHostsTask
    extends Thread
  {
    public 
    RemoveHostsTask
    (
     TreeSet<String> hostnames
    ) 
    {
      super("JManageJobServersDialog:RemoveHostsTask");

      pHostnames = hostnames;
    }
 
    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      try {
	QueueMgrClient client = master.getQueueMgrClient();
	client.removeHosts(pHostnames);
      }
      catch(PipelineException ex) {
	master.showErrorDialog(ex);
      }
      
      GetHostsTask task = new GetHostsTask();
      task.start();
    }

    private TreeSet<String>  pHostnames; 
  }

  /** 
   * Edit the properties of existing hosts.
   */ 
  private
  class EditHostsTask
    extends Thread
  {
    public 
    EditHostsTask
    (
     boolean update
    ) 
    {
      super("JManageJobServersDialog:RemoveHostsTask");
      
      pUpdate = update; 

      pStatus       = pTableModel.getHostStatus();
      pReservations = pTableModel.getHostReservations();
      pSlots   	    = pTableModel.getHostSlots(); 
      pBiases       = pTableModel.getHostBiases(); 
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      try {
	QueueMgrClient client = master.getQueueMgrClient();
	client.editHosts(pStatus, pReservations, pSlots, pBiases); 
      }
      catch(PipelineException ex) {
	master.showErrorDialog(ex);
      }
      
      if(pUpdate) {
	GetHostsTask task = new GetHostsTask();
	task.start();
      }
    }

    private boolean  pUpdate; 

    private TreeMap<String,QueueHost.Status>         pStatus; 
    private TreeMap<String,String>                   pReservations; 
    private TreeMap<String,Integer>                  pSlots;  
    private TreeMap<String,TreeMap<String,Integer>>  pBiases;       
  }

  /** 
   * Update the UI components with the new job server hosts information.
   */ 
  private
  class UpdateTask
    extends Thread
  {
    public 
    UpdateTask
    (
     TreeMap<String,QueueHost> hosts, 
     TreeSet<String> keys, 
     boolean isPrivileged
    ) 
    {
      super("JManageJobServersDialog:UpdateTask");

      pHosts = hosts; 
      pKeys  = keys; 

      pIsPrivileged = isPrivileged;
    }
 
    public void 
    run() 
    {
      pTableModel.setQueueHosts(pHosts, pKeys, pIsPrivileged);

      // update the Locked icon of the JManagerPanel base on pIsPrivileged... 

      pConfirmButton.setEnabled(false);
      pApplyButton.setEnabled(false);

      pAddButton.setEnabled(pIsPrivileged);
      pRemoveButton.setEnabled(pIsPrivileged);
    }

    private TreeMap<String,QueueHost>  pHosts;
    private TreeSet<String>            pKeys;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 8533790804117777204L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Does the current user have privileged status?
   */ 
  private boolean  pIsPrivileged;

  /**
   * The job servers table model.
   */ 
  private QueueHostsTableModel  pTableModel;

  /**
   * The job servers table panel.
   */ 
  private JTablePanel  pTablePanel;

  /**
   * The panel buttons.
   */ 
  private JButton  pAddButton;
  private JButton  pRemoveButton;

}
