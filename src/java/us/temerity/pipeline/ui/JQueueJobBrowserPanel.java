// $Id: JQueueJobBrowserPanel.java,v 1.5 2004/09/03 02:01:02 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.glue.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   J O B   B R O W S E R   P A N E L                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A panel which displays the existing @{link QueueJobGroup QueueJobGroup}.
 */ 
public 
class JQueueJobBrowserPanel
  extends JTopLevelPanel
  implements MouseListener, KeyListener, ListSelectionListener, ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new panel with the default working area view.
   */
  public 
  JQueueJobBrowserPanel()
  {
    super();
    initUI();
  }

  /**
   * Construct a new panel with a working area view identical to the given panel.
   */
  public 
  JQueueJobBrowserPanel
  (
   JTopLevelPanel panel
  )
  {
    super(panel);
    initUI();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the common user interface components.
   */ 
  private void 
  initUI()
  {
    /* initialize fields */ 
    {
      pJobGroups   = new TreeMap<Long,QueueJobGroup>(); 
      pJobStatus   = new TreeMap<Long,JobStatus>();
      pSelectedIDs = new TreeSet<Long>();
    }    
    
    /* initialize the panel components */ 
    {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));  
      
      {
	JPanel panel = new JPanel();
	pHeaderPanel = panel;
	panel.setName("DialogHeader");	
	
	panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

	panel.addMouseListener(this); 
	panel.setFocusable(true);
	panel.addKeyListener(this);
      
	{
	  JLabel label = new JLabel("Job Browser:");
	  label.setName("DialogHeaderLabel");	
	  
	  panel.add(label);	  
	}
	
	panel.add(Box.createHorizontalGlue());
	
	{
	  JButton btn = new JButton();		
	  pDeleteCompletedButton = btn;
	  btn.setName("DeleteCompletedButton");

	  btn.setSelected(true);
		  
	  Dimension size = new Dimension(19, 19);
	  btn.setMinimumSize(size);
	  btn.setMaximumSize(size);
	  btn.setPreferredSize(size);
	  
	  btn.setActionCommand("delete-completed");
	  btn.addActionListener(this);
	  
	  panel.add(btn);
	} 

	panel.add(Box.createRigidArea(new Dimension(10, 0)));

	{
	  JToggleButton btn = new JToggleButton();		
	  pFilterViewsButton = btn;
	  btn.setName("ViewsButton");

	  btn.setSelected(true);
		  
	  Dimension size = new Dimension(19, 19);
	  btn.setMinimumSize(size);
	  btn.setMaximumSize(size);
	  btn.setPreferredSize(size);
	  
	  btn.setActionCommand("filter-views");
	  btn.addActionListener(this);
	  
	  panel.add(btn);
	} 

	add(panel);
      }
      
      {
	JPanel panel = new JPanel();
	panel.setName("MainPanel");
	
	panel.setLayout(new BorderLayout());
	
	{
	  QueueJobGroupsTableModel model = new QueueJobGroupsTableModel();
	  pTableModel = model;
	  
	  JTablePanel tpanel =
	    new JTablePanel(model, model.getColumnWidths(), 
			    model.getRenderers(), model.getEditors());
	  pTablePanel = tpanel;

	  ListSelectionModel smodel = tpanel.getTable().getSelectionModel();
	  smodel.addListSelectionListener(this);

	  panel.add(tpanel);
	}
	
	add(panel);
      }
    }

    updateQueueJobs(null, null);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Set the group ID. <P> 
   * 
   * Group ID values must be in the range: [1-9]
   * 
   * @param groupID
   *   The new group ID or (0) for no group assignment.
   */ 
  public void
  setGroupID
  (
   int groupID
  )
  {
    UIMaster master = UIMaster.getInstance();

    PanelGroup<JQueueJobBrowserPanel> panels = master.getQueueJobBrowserPanels();

    if(pGroupID > 0)
      panels.releaseGroup(pGroupID);

    pGroupID = 0;
    if((groupID > 0) && panels.isGroupUnused(groupID)) {
      panels.assignGroup(this, groupID);
      pGroupID = groupID;
    }
  }

  /**
   * Is the given group currently unused for this type of panel.
   */ 
  public boolean
  isGroupUnused
  (
   int groupID
  ) 
  {
    PanelGroup<JQueueJobBrowserPanel> panels = 
      UIMaster.getInstance().getQueueJobBrowserPanels();
    return panels.isGroupUnused(groupID);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the author and view.
   */ 
  public synchronized void 
  setAuthorView
  (
   String author, 
   String view 
  ) 
  {
    super.setAuthorView(author, view);    

    if(pJobGroups != null)
      doUpdate();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Update the job groups and status.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param groups
   *   The queue job groups indexe by job group ID.
   * 
   * @param status
   *   The job status indexed by job ID.
   */ 
  public synchronized void
  updateQueueJobs
  (
   String author, 
   String view, 
   TreeMap<Long,QueueJobGroup> groups, 
   TreeMap<Long,JobStatus> status
  ) 
  {
    if(!pAuthor.equals(author) || !pView.equals(view)) 
      super.setAuthorView(author, view);

    updateQueueJobs(groups, status);
  }

  /**
   * Update the job groups and status.
   * 
   * @param groups
   *   The queue job groups indexe by job group ID.
   * 
   * @param status
   *   The job status indexed by job ID.
   */ 
  public synchronized void
  updateQueueJobs
  (
   TreeMap<Long,QueueJobGroup> groups, 
   TreeMap<Long,JobStatus>     status
  ) 
  {
    /* update the groups and job status */ 
    {
      if((groups != null) && pFilterViewsButton.isSelected()) {
	pJobGroups = new TreeMap<Long,QueueJobGroup>(); 
	for(QueueJobGroup group : groups.values()) {
	  NodeID nodeID = group.getNodeID();
	  if(nodeID.getAuthor().equals(pAuthor) && nodeID.getView().equals(pView)) 
	    pJobGroups.put(group.getGroupID(), group);
	}
      }
      else {
	pJobGroups.clear();
	if(groups != null) 
	  pJobGroups.putAll(groups);
      }
      
      pJobStatus.clear();
      if(status != null) 
	pJobStatus.putAll(status);
    }
    
    /* update the table model, 
         reselects any of the previously selected job groups which still exist */ 
    {
      JTable table = pTablePanel.getTable();
      ListSelectionModel smodel = table.getSelectionModel();

      smodel.removeListSelectionListener(this);
      { 
	pTableModel.setQueueJobGroups(pJobGroups, pJobStatus);
	
	TreeSet<Long> selected = new TreeSet<Long>();
	for(Long groupID : pSelectedIDs) {
	  int row = pTableModel.getGroupRow(groupID);
	  if(row != -1) {
	    table.addRowSelectionInterval(row, row);
	    selected.add(groupID);
	  }
	}

	pSelectedIDs.clear();
	pSelectedIDs.addAll(selected);
      }      
      smodel.addListSelectionListener(this);
    }

    /* update any connected JobViewer */ 
    if(pGroupID > 0) {
      UIMaster master = UIMaster.getInstance();
      PanelGroup<JQueueJobViewerPanel> panels = master.getQueueJobViewerPanels();
      JQueueJobViewerPanel panel = panels.getPanel(pGroupID);
      if(panel != null) {
	TreeMap<Long,QueueJobGroup> sgroups = new TreeMap<Long,QueueJobGroup>();
	for(Long groupID : pSelectedIDs) {
	  QueueJobGroup group = pJobGroups.get(groupID);
	  if(group != null) 
	    sgroups.put(groupID, group);
	}
	  
	panel.updateQueueJobs(pAuthor, pView, sgroups, pJobStatus);
	panel.updateManagerTitlePanel();
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- MOUSE LISTENER METHODS --------------------------------------------------------------*/

  /**
   * Invoked when the mouse button has been clicked (pressed and released) on a component. 
   */ 
  public void 
  mouseClicked(MouseEvent e) {}
   
  /**
   * Invoked when the mouse enters a component. 
   */
  public void 
  mouseEntered
  (
   MouseEvent e
  ) 
  {
    pHeaderPanel.requestFocusInWindow();
  }

  /**
   * Invoked when the mouse exits a component. 
   */ 
  public void 
  mouseExited
  (
   MouseEvent e
  ) 
  {
    KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
  }
   
  /**
   * Invoked when a mouse button has been pressed on a component. 
   */
  public void 
  mousePressed(MouseEvent e) {}

  /**
   * Invoked when a mouse button has been released on a component. 
   */ 
  public void 
  mouseReleased(MouseEvent e) {}



  /*-- KEY LISTENER METHODS ----------------------------------------------------------------*/

  /**
   * invoked when a key has been pressed.
   */   
  public void 
  keyPressed
  (
   KeyEvent e
  )
  {
    UserPrefs prefs = UserPrefs.getInstance();

    if((prefs.getJobBrowserUpdate() != null) &&
       prefs.getJobBrowserUpdate().wasPressed(e))
      doUpdate();

    else {
      switch(e.getKeyCode()) {
      case KeyEvent.VK_SHIFT:
      case KeyEvent.VK_ALT:
      case KeyEvent.VK_CONTROL:
	break;
      
      default:
	Toolkit.getDefaultToolkit().beep();
      }
    }
  }

  /**
   * Invoked when a key has been released.
   */ 
  public void 	
  keyReleased(KeyEvent e) {}

  /**
   * Invoked when a key has been typed.
   */ 
  public void 	
  keyTyped(KeyEvent e) {} 



  /*-- LIST SELECTION LISTENER METHODS -----------------------------------------------------*/

  /**
   * Called whenever the value of the selection changes.
   */ 
  public void 	
  valueChanged
  (
   ListSelectionEvent e
  )
  {
    if(e.getValueIsAdjusting()) 
      return;

    /* update selected IDs */ 
    {
      pSelectedIDs.clear(); 
      int rows[] = pTablePanel.getTable().getSelectedRows();
      int wk;
      for(wk=0; wk<rows.length; wk++) {
	Long groupID = (Long) pTableModel.getValueAt(rows[wk], 0);
	pSelectedIDs.add(groupID);
      }
    }
      
    doUpdate();      
  }
          

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
    if(cmd.equals("filter-views")) 
      doUpdate();
    else if(cmd.equals("delete-completed")) 
      doDeleteCompleted();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the status of all jobs and job groups.
   */ 
  public void
  doUpdate()
  { 
    /* enable/disable the DeleteCompleted button */ 
    if(pFilterViewsButton.isSelected())
      pDeleteCompletedButton.setEnabled(!pIsLocked);
    else {
      boolean privileged = false;
      UIMaster master = UIMaster.getInstance();
      try {
	privileged = master.getMasterMgrClient().isPrivileged();
      }
      catch(PipelineException ex) {
	master.showErrorDialog(ex);
      }

      pDeleteCompletedButton.setEnabled(privileged);
    }
    
    /* update the jobs */ 
    GetJobsTask task = new GetJobsTask();
    task.start();
  }

  /**
   * Delete all completed job groups.
   */ 
  public void
  doDeleteCompleted()
  { 
    DeleteJobGroupsTask task = new DeleteJobGroupsTask(!pFilterViewsButton.isSelected());
    task.start();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  public synchronized void 
  toGlue
  ( 
   GlueEncoder encoder   
  ) 
    throws GlueException
  {
    super.toGlue(encoder);

    encoder.encode("FilterView", pFilterViewsButton.isSelected());

    if(!pSelectedIDs.isEmpty())
      encoder.encode("SelectedIDs", pSelectedIDs);
  }

  public synchronized void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    Boolean filter = (Boolean) decoder.decode("FilterView");
    if(filter != null) 
      pFilterViewsButton.setSelected(filter);

    TreeSet<Long> selected = (TreeSet<Long>) decoder.decode("SelectedIDs");
    if(selected != null) 
      pSelectedIDs.addAll(selected);

    super.fromGlue(decoder);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Get the current job groups and job states.
   */ 
  private
  class GetJobsTask
    extends Thread
  {
    public 
    GetJobsTask() 
    {
      super("JQueueJobBrowserPanel:GetJobsTask");
    }
 
    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();

      TreeMap<Long,QueueJobGroup> groups = null;
      TreeMap<Long,JobStatus> status = null;
      if(master.beginPanelOp("Updating Jobs...")) {
	try {
	  QueueMgrClient client = master.getQueueMgrClient();
	  groups = client.getJobGroups(); 
	  TreeSet<Long> groupIDs = new TreeSet<Long>(groups.keySet());
	  status = client.getJobStatus(groupIDs);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	}
	finally {
	  master.endPanelOp("Done.");
	}
      }
	
      UpdateTask task = new UpdateTask(groups, status);
      SwingUtilities.invokeLater(task);
    }
  }
  
  /** 
   * Update the UI components with the new job group and job state information.
   */ 
  private
  class UpdateTask
    extends Thread
  {
    public 
    UpdateTask
    (
     TreeMap<Long,QueueJobGroup> groups, 
     TreeMap<Long,JobStatus> status
    ) 
    {
      super("JQueueJobBrowserPanel:UpdateTask");

      pGroups = groups;
      pStatus = status; 
    }

    public void 
    run() 
    {
      updateQueueJobs(pGroups, pStatus);
    }
    
    private TreeMap<Long,QueueJobGroup>  pGroups; 
    private TreeMap<Long,JobStatus>      pStatus; 
  }

  /** 
   * Delete the completed job groups.
   */ 
  private
  class DeleteJobGroupsTask
    extends Thread
  {
    public 
    DeleteJobGroupsTask
    (
     boolean allViews
    ) 
    {
      super("JQueueJobsBrowserPanel:DeleteJobGroupsTask");

      pAllViews = allViews;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp("Deleting Job Groups...")) {
	try {
	  if(pAllViews) 
	    master.getQueueMgrClient().deleteAllJobGroups();
	  else 
	    master.getQueueMgrClient().deleteViewJobGroups(pAuthor, pView);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp("Done.");
	}

	doUpdate();
      }
    }

    private boolean  pAllViews; 
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -507775432755443313L;

  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * All job groups indexed by group ID. 
   */ 
  private TreeMap<Long,QueueJobGroup>  pJobGroups;
  
  /**
   * The job status of the jobs which make up the job groups indexed by job ID. 
   */ 
  private TreeMap<Long,JobStatus>  pJobStatus;

  /**
   * The IDs of the selected job groups. 
   */ 
  private TreeSet<Long>  pSelectedIDs; 



  /*----------------------------------------------------------------------------------------*/

  /**
   * The header panel.
   */ 
  private JPanel pHeaderPanel; 

  /**
   * Used to select whether job groups should be filtered by the current owner|view. 
   */ 
  private JToggleButton  pFilterViewsButton;  

  /**
   * Deletes completed job groups when pressed.
   */ 
  private JButton  pDeleteCompletedButton;

  /**
   * The job groups table model.
   */ 
  private QueueJobGroupsTableModel  pTableModel;

  /**
   * The job groups table panel.
   */ 
  private JTablePanel  pTablePanel;


}
