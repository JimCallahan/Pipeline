// $Id: JManageJobServersDialog.java,v 1.3 2004/08/01 19:31:46 jim Exp $

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
import javax.swing.table.*;

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

    /* create dialog body components */ 
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

	int width[] = model.getColumnWidths(); 
	int total = 0;
	{
	  int wk;
	  for(wk=0; wk<width.length; wk++) 
	    total += width[wk];
	}
	
	{
	  Box box = new Box(BoxLayout.X_AXIS);
	  
	  {
	    Box hbox = new Box(BoxLayout.X_AXIS);

	    int wk;
	    for(wk=0; wk<width.length; wk++) {
	      String prefix = "";
	      if((wk > 2) && (wk < 6)) 
		prefix = "Blue";
	      else if((wk > 5) && (wk < 8)) 
		prefix = "Green"; 
	      
	      JButton btn = new JButton(pTableModel.getColumnName(wk));
	      btn.setName(prefix + "TableHeaderButton");
	      
	      {	    
		Dimension size = new Dimension(width[wk], 23);
		btn.setMinimumSize(size);
		btn.setPreferredSize(size);
		btn.setMaximumSize(size);
	      }
	      
	      btn.addActionListener(tpanel);
	      btn.setActionCommand("sort-column:" + wk);	  
	      
	      hbox.add(btn);
	    }
	    
	    Dimension size = new Dimension(total, 23); 
	    hbox.setMinimumSize(size);
	    hbox.setPreferredSize(size);
	    hbox.setMaximumSize(size);

	    box.add(hbox);
	  }

	  {
	    Box hbox = new Box(BoxLayout.X_AXIS);
	    pSelectionKeyHeaderBox = hbox; 
	  
	    box.add(hbox);
	  }	  
	  
	  tpanel.getHeaderViewport().setView(box);
	}

	body.add(tpanel);
      }


      String extra[][] = {
	null,
	{ "History", "history" }, 
	null,
	{ "Add",    "add" }, 
	{ "Remove", "remove" },
	{ "Update", "update" }
      };

      JButton btns[] = super.initUI("Manage Job Servers:", false, body, 
				    "Confirm", "Apply", extra, "Close");
      pAddButton    = btns[3];
      pRemoveButton = btns[4];

      doUpdate();
      pack();
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get names of the selected hosts.
   */ 
  public TreeSet<String> 
  getSelectedHostnames() 
  {
    TreeSet<String> hostnames = new TreeSet<String>();
    int rows[] = pTablePanel.getTable().getSelectedRows();
    int wk;
    for(wk=0; wk<rows.length; wk++) {
      String hname = (String) pTableModel.getValueAt(rows[wk], 0);
      hostnames.add(hname);
    }

    return hostnames;
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
    if(cmd.equals("history")) 
      doHistory();
    else if(cmd.equals("add")) 
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
   * Show the resource usage history dialogs for the selected hosts.
   */ 
  private void 
  doHistory()
  {
    pTablePanel.cancelEditing();

    TreeSet<String> hostnames = getSelectedHostnames();
    for(String hname : hostnames) {
      GetHistoryTask task = new GetHistoryTask(hname);
      task.start();      
    }
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

    TreeSet<String> hostnames = getSelectedHostnames();    
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
      pTableModel.fireTableStructureChanged(); 

      {
	TableColumnModel cmodel = pTablePanel.getTable().getColumnModel();
	
	int wk;
	for(wk=0; wk<8; wk++) {
	  TableColumn tcol = cmodel.getColumn(wk);

	  tcol.setCellRenderer(pTableModel.getRenderers()[wk]);

	  TableCellEditor editor = pTableModel.getEditors()[wk];
	  if(editor != null) 
	    tcol.setCellEditor(editor);

	  int width = pTableModel.getColumnWidths()[wk];
	  tcol.setMinWidth(width);
	  tcol.setPreferredWidth(width);
	  tcol.setMaxWidth(width);
	}

	wk = 8;
	for(String kname : pKeys) {
	  TableColumn tcol = cmodel.getColumn(wk);

	  tcol.setCellRenderer(new JSelectionBiasTableCellRenderer());
	  tcol.setCellEditor(new JSelectionBiasTableCellEditor());

	  tcol.setMinWidth(100);
	  tcol.setPreferredWidth(100);
	  tcol.setMaxWidth(100);

	  wk++;
	}
      }

      {
	pSelectionKeyHeaderBox.removeAll();
	
	int wk = 8;
	for(String kname : pKeys) {
	  JButton btn = new JButton(kname);
	  btn.setName("PurpleTableHeaderButton");
	  
	  {	    
	    Dimension size = new Dimension(100, 23);
	    btn.setMinimumSize(size);
	    btn.setPreferredSize(size);
	    btn.setMaximumSize(size);
	  }
	  
	  btn.addActionListener(pTablePanel);
	  btn.setActionCommand("sort-column:" + wk);	  
	  
	  pSelectionKeyHeaderBox.add(btn);

	  wk++;
	}

	Box parent = (Box) pSelectionKeyHeaderBox.getParent();
	parent.revalidate();
	parent.repaint();
      }
      
      pConfirmButton.setEnabled(false);
      pApplyButton.setEnabled(false);

      pAddButton.setEnabled(pIsPrivileged);
      pRemoveButton.setEnabled(pIsPrivileged);
    }

    private TreeMap<String,QueueHost>  pHosts;
    private TreeSet<String>            pKeys;
  }
  

  /** 
   * Get the full resource usage history.
   */ 
  private
  class GetHistoryTask
    extends Thread
  {
    public 
    GetHistoryTask
    (
     String hostname
    ) 
    {
      pHostname = hostname; 
    }
    
    public void
    run()
    {
      UIMaster master = UIMaster.getInstance();
      try {
	QueueMgrClient client = master.getQueueMgrClient();
	ResourceSampleBlock block = client.getHostResourceSamples(pHostname);

	ShowHistoryTask task = new ShowHistoryTask(pHostname, block);
	SwingUtilities.invokeLater(task);
      }
      catch(PipelineException ex) {
	master.showErrorDialog(ex);
      }
    }

    private String  pHostname;
  }

  /** 
   * Display the full resource usage history dialog.
   */ 
  private
  class ShowHistoryTask
    extends Thread
  {
    public 
    ShowHistoryTask
    (
     String hostname,
     ResourceSampleBlock block 
    ) 
    {
      pHostname = hostname; 
      pBlock    = block; 
    }
    
    public void
    run()
    {
      JJobServerHistoryDialog diag = new JJobServerHistoryDialog(pHostname, pBlock);
      diag.setVisible(true);  
    }

    private String               pHostname; 
    private ResourceSampleBlock  pBlock;
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
   * The container of the header buttons for the selection key columns.
   */ 
  private Box  pSelectionKeyHeaderBox; 

  /**
   * The panel buttons.
   */ 
  private JButton  pAddButton;
  private JButton  pRemoveButton;

}
