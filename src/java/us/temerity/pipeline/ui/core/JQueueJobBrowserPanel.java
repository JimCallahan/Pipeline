// $Id: JQueueJobBrowserPanel.java,v 1.24 2006/10/18 06:34:22 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;
import us.temerity.pipeline.glue.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   J O B   B R O W S E R   P A N E L                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A panel which displays the job groups, servers and slots.
 */ 
public 
class JQueueJobBrowserPanel
  extends JTopLevelPanel
  implements MouseListener, KeyListener, ActionListener
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

      pViewFilter = ViewFilter.SingleView; 
    }

    /* initialize the popup menus */ 
    {
      JMenuItem item;
      	
      {	
	pGroupsPopup = new JPopupMenu();

	item = new JMenuItem("Update");
	pGroupsUpdateItem = item;
	item.setActionCommand("update");
	item.addActionListener(this);
	pGroupsPopup.add(item);

	{
	  JMenu sub = new JMenu("View Filter");
	  pGroupsPopup.add(sub);

	  item = new JMenuItem("Single View");
	  pGroupsSingleViewItem = item;
	  item.setActionCommand("single-view");
	  item.addActionListener(this);
	  sub.add(item);
	
	  item = new JMenuItem("Owned Views");
	  pGroupsOwnedViewsItem = item;
	  item.setActionCommand("owned-views");
	  item.addActionListener(this);
	  sub.add(item);
	
	  item = new JMenuItem("All Views");
	  pGroupsAllViewsItem = item;
	  item.setActionCommand("all-views");
	  item.addActionListener(this);
	  sub.add(item);
	}

	pGroupsPopup.addSeparator();

	item = new JMenuItem("Queue Jobs");
	pGroupsQueueItem = item;
	item.setActionCommand("groups-queue-jobs");
	item.addActionListener(this);
	pGroupsPopup.add(item);

	item = new JMenuItem("Queue Jobs Special...");
	pGroupsQueueSpecialItem = item;
	item.setActionCommand("groups-queue-jobs-special");
	item.addActionListener(this);
	pGroupsPopup.add(item);

	item = new JMenuItem("Pause Jobs");
	pGroupsPauseItem = item;
	item.setActionCommand("groups-pause-jobs");
	item.addActionListener(this);
	pGroupsPopup.add(item);
	
	item = new JMenuItem("Resume Jobs");
	pGroupsResumeItem = item;
	item.setActionCommand("groups-resume-jobs");
	item.addActionListener(this);
	pGroupsPopup.add(item);
	
	item = new JMenuItem("Preempt Jobs");
	pGroupsPreemptItem = item;
	item.setActionCommand("groups-preempt-jobs");
	item.addActionListener(this);
	pGroupsPopup.add(item);
	
	item = new JMenuItem("Kill Jobs");
	pGroupsKillItem = item;
	item.setActionCommand("groups-kill-jobs");
	item.addActionListener(this);
	pGroupsPopup.add(item);

	pGroupsPopup.addSeparator();
	
	item = new JMenuItem("Delete Groups");
	pGroupsDeleteItem = item;
	item.setActionCommand("delete-group");
	item.addActionListener(this);
	pGroupsPopup.add(item);

	item = new JMenuItem("Delete Completed");
	pGroupsDeleteCompletedItem = item;
	item.setActionCommand("delete-completed");
	item.addActionListener(this);
	pGroupsPopup.add(item);
      }    

      updateMenuToolTips();  
    }

    
    /* initialize the panel components */ 
    {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));  
      
      {
	JPanel panel = new JPanel();
	pGroupsHeaderPanel = panel;
	panel.setName("DialogHeader");	
	  
	panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
	  
	panel.addMouseListener(this); 
	panel.setFocusable(true);
	panel.addKeyListener(this);
	panel.addMouseListener(new KeyFocuser(panel));
	  
	{
	  JLabel label = new JLabel("Job Groups:");
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
	  JButton btn = new JButton();		
	  pViewFilterButton = btn;
	  btn.setName(pViewFilter + "Button");
	    
	  Dimension size = new Dimension(19, 19);
	  btn.setMinimumSize(size);
	  btn.setMaximumSize(size);
	  btn.setPreferredSize(size);
	    
	  btn.setActionCommand("view-filter-changed"); 
	  btn.addActionListener(this);
	    
	  panel.add(btn);
	} 
	  
	add(panel);
      }
	
      {
	JPanel panel = new JPanel();
	panel.setName("MainPanel");
	panel.setLayout(new BorderLayout());
	  
	panel.addMouseListener(this); 
	panel.setFocusable(true);
	panel.addKeyListener(this);
	panel.addMouseListener(new KeyFocuser(panel));

	{
	  QueueJobGroupsTableModel model = new QueueJobGroupsTableModel();
	  pGroupsTableModel = model;
	    
	  pGroupsListSelector = new GroupsListSelector();

	  JGroupsTablePanel tpanel = new JGroupsTablePanel(model, pGroupsListSelector);
	  pGroupsTablePanel = tpanel;
	    
	  {
	    JScrollPane scroll = tpanel.getTableScroll();
	    scroll.addMouseListener(this); 
	    scroll.setFocusable(true);
	    scroll.addKeyListener(this);
	    scroll.addMouseListener(new KeyFocuser(scroll));
	  }

	  {
	    JTable table = tpanel.getTable();
	    table.addMouseListener(this); 
	    table.setFocusable(true);
	    table.addKeyListener(this);
	    table.addMouseListener(new KeyFocuser(table));
	  }

	  panel.add(tpanel);
	}
	  
	add(panel);
      }
    }
 
    updateJobs(null, null);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Get the title of this type of panel.
   */
  public String 
  getTypeName() 
  {
    return "Job Browser";
  }
  

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

    master.updateOpsBar();
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
   * Are the contents of the panel read-only. <P> 
   */ 
  public boolean
  isLocked() 
  {
    return (super.isLocked() && !pPrivilegeDetails.isQueueManaged(pAuthor)); 
  }

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

    updatePanels();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get IDs of the selected job groups. 
   */ 
  public TreeSet<Long> 
  getSelectedGroupIDs() 
  {
    TreeSet<Long> groupIDs = new TreeSet<Long>();
    int rows[] = pGroupsTablePanel.getTable().getSelectedRows();
    int wk;
    for(wk=0; wk<rows.length; wk++) {
      Long groupID = pGroupsTableModel.getGroupID(rows[wk]);
      groupIDs.add(groupID);
    }

    return groupIDs;
  }
  
  /**
   * Get the ID of a single just selected job group, otherwise return <CODE>null</CODE>.
   */ 
  public Long 
  getJustSelectedGroupID() 
  {
    if(pGroupsListSelector != null) 
      return pGroupsListSelector.getJustSelected();
    return null;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Perform the inial update after restoring a layout. 
   */ 
  public void 
  restoreSelections() 
  {
    updatePanels();
  }

  /**
   * Update all panels which share the current update channel.
   */ 
  private void 
  updatePanels() 
  {
    updatePanels(false);
  }

  /**
   * Update all panels which share the current update channel.
   */ 
  private void 
  updatePanels
  (
   boolean selectionOnly
  ) 
  {
    PanelUpdater pu = new PanelUpdater(this, selectionOnly);
    pu.start();
  }

  /**
   * Apply the updated information to this panel.
   * 
   * @param author
   *   Owner of the current working area.
   * 
   * @param view
   *   Name of the current working area view.
   * 
   * @param groups
   *   The queue job groups indexe by job group ID.
   * 
   * @param jobStatus
   *   The job status indexed by job ID.
   */
  public synchronized void 
  applyPanelUpdates
  (
   String author, 
   String view, 
   TreeMap<Long,QueueJobGroup> groups, 
   TreeMap<Long,JobStatus> jobStatus
  )
  {
    if(!pAuthor.equals(author) || !pView.equals(view)) 
      super.setAuthorView(author, view);    

    updateJobs(groups, jobStatus);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the jobs groups, servers and slots. 
   * 
   * @param groups
   *   The queue job groups indexe by job group ID.
   * 
   * @param jobStatus
   *   The job status indexed by job ID.
   */ 
  private synchronized void
  updateJobs
  (
   TreeMap<Long,QueueJobGroup> groups, 
   TreeMap<Long,JobStatus> jobStatus
  ) 
  {
    updatePrivileges();

    /* enable/disable the DeleteCompleted button */ 
    switch(pViewFilter) {
    case SingleView:
    case OwnedViews:
      pDeleteCompletedButton.setEnabled(!isLocked());
      break;
      
    case AllViews:
      pDeleteCompletedButton.setEnabled(pPrivilegeDetails.isQueueAdmin());
    }

    /* update the groups after applying the current groups filter */ 
    pJobGroups.clear();
    if(groups != null) {
      for(QueueJobGroup group : groups.values()) {
	NodeID nodeID = group.getNodeID();
	
	switch(pViewFilter) {
	case SingleView:
	  if(nodeID.getAuthor().equals(pAuthor) && nodeID.getView().equals(pView)) 
	    pJobGroups.put(group.getGroupID(), group);
	  break;
	  
	case OwnedViews:
	  if(nodeID.getAuthor().equals(pAuthor)) 
	    pJobGroups.put(group.getGroupID(), group);
	  break;
	  
	case AllViews:
	  pJobGroups.put(group.getGroupID(), group);
	}
      }
    }
    
    /* update the job status */ 
    pJobStatus.clear();
    if(jobStatus != null) 
      pJobStatus.putAll(jobStatus);
    
    /* update the groups table, 
         reselects any of the previously selected job groups which still exist */
    {
      JTable table = pGroupsTablePanel.getTable();
      ListSelectionModel smodel = table.getSelectionModel();

      smodel.removeListSelectionListener(pGroupsListSelector);
      { 
	pGroupsTableModel.setQueueJobGroups(pJobGroups, pJobStatus);
	
	TreeSet<Long> selected = new TreeSet<Long>();
	for(Long groupID : pSelectedIDs) {
	  int row = pGroupsTableModel.getGroupRow(groupID);
	  if(row != -1) {
	    table.addRowSelectionInterval(row, row);
	    selected.add(groupID);
	  }
	}

	pSelectedIDs.clear();
	pSelectedIDs.addAll(selected);
      }      
      smodel.addListSelectionListener(pGroupsListSelector);
    }
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the job groups menu.
   */ 
  public void 
  updateGroupsMenu() 
  {
    boolean selected = (pGroupsTablePanel.getTable().getSelectedRowCount() > 0);

    switch(pViewFilter) {
    case SingleView:
    case OwnedViews:
      pGroupsQueueItem.setEnabled(selected && !isLocked()); 
      pGroupsQueueSpecialItem.setEnabled(selected && !isLocked()); 
      pGroupsPauseItem.setEnabled(selected && !isLocked()); 
      pGroupsResumeItem.setEnabled(selected && !isLocked());
      pGroupsPreemptItem.setEnabled(selected && !isLocked());
      pGroupsKillItem.setEnabled(selected && !isLocked());
      pGroupsDeleteItem.setEnabled(selected && !isLocked());
      pGroupsDeleteCompletedItem.setEnabled(!isLocked());
      break;

    case AllViews:
      pGroupsQueueItem.setEnabled(selected); 
      pGroupsQueueSpecialItem.setEnabled(selected); 
      pGroupsPauseItem.setEnabled(selected); 
      pGroupsResumeItem.setEnabled(selected);
      pGroupsPreemptItem.setEnabled(selected);
      pGroupsKillItem.setEnabled(selected);
      pGroupsDeleteItem.setEnabled(selected);
      pGroupsDeleteCompletedItem.setEnabled(true);
    }
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Refocus keyboard events on this panel if it contains the mouse.
   * 
   * @return
   *   Whether the panel has received the focus.
   */ 
  public boolean 
  refocusOnPanel() 
  {
    if(pGroupsHeaderPanel.getMousePosition(true) != null) {
      pGroupsHeaderPanel.requestFocusInWindow();
      return true;
    }

    return false;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the panel to reflect new user preferences.
   */ 
  public void 
  updateUserPrefs() 
  {
    updateMenuToolTips();
  }

  /**
   * Update the menu item tool tips.
   */ 
  private void 
  updateMenuToolTips() 
  {
    UserPrefs prefs = UserPrefs.getInstance();
       
    updateMenuToolTip
      (pGroupsUpdateItem, prefs.getUpdate(),
       "Update the status of all jobs and job groups.");
    updateMenuToolTip
      (pGroupsSingleViewItem, prefs.getJobBrowserSingleViewFilter(),
      "Show only job groups owned by the current view.");
    updateMenuToolTip
      (pGroupsOwnedViewsItem, prefs.getJobBrowserOwnedViewsFilter(),
       "Show job groups from any view owned by the current user.");
    updateMenuToolTip
      (pGroupsAllViewsItem, prefs.getJobBrowserAllViewsFilter(),
       "Show job groups from all views.");
    updateMenuToolTip
      (pGroupsQueueItem, prefs.getQueueJobs(), 
       "Resubmit aborted and failed jobs to the queue for the selected groups.");
    updateMenuToolTip
      (pGroupsQueueSpecialItem, prefs.getQueueJobsSpecial(), 
       "Resubmit aborted and failed jobs to the queue for the selected groups with " + 
       "special job requirements.");
    updateMenuToolTip
      (pGroupsPauseItem, prefs.getPauseJobs(), 
       "Pause all jobs associated with the selected groups.");
    updateMenuToolTip
      (pGroupsResumeItem, prefs.getResumeJobs(), 
       "Resume execution of all jobs associated with the selected groups.");
    updateMenuToolTip
      (pGroupsPreemptItem, prefs.getPreemptJobs(), 
       "Preempt all jobs associated with the selected groups.");
    updateMenuToolTip
      (pGroupsKillItem, prefs.getKillJobs(), 
       "Kill all jobs associated with the selected groups.");
    updateMenuToolTip
      (pGroupsDeleteItem, prefs.getJobBrowserGroupsDelete(),
       "Delete the selected completed job groups.");
    updateMenuToolTip
      (pGroupsDeleteCompletedItem, prefs.getJobBrowserGroupsDeleteCompleted(),
       "Delete all completed job groups.");
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
  mouseEntered(MouseEvent e) {}

  /**
   * Invoked when the mouse exits a component. 
   */ 
  public void 
  mouseExited(MouseEvent e) {}
   
  /**
   * Invoked when a mouse button has been pressed on a component. 
   */
  public void 
  mousePressed
  (
   MouseEvent e
  ) 
  {
    /* manager panel popups */ 
    if(pManagerPanel.handleManagerMouseEvent(e)) 
      return;

    /* local popups */ 
    int mods = e.getModifiersEx();
    switch(e.getButton()) {
    case MouseEvent.BUTTON3:
      {
	int on1  = (MouseEvent.BUTTON3_DOWN_MASK);
	
	int off1 = (MouseEvent.BUTTON1_DOWN_MASK | 
		    MouseEvent.BUTTON2_DOWN_MASK | 
		    MouseEvent.SHIFT_DOWN_MASK |
		    MouseEvent.ALT_DOWN_MASK |
		    MouseEvent.CTRL_DOWN_MASK);

	/* BUTTON3: popup menus */ 
	if((mods & (on1 | off1)) == on1) {
	  updateGroupsMenu();
	  pGroupsPopup.show(e.getComponent(), e.getX(), e.getY());      
	}
	else {
	  Toolkit.getDefaultToolkit().beep();
	}
      }
    }
  }

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
    /* manager panel hotkeys */ 
    if(pManagerPanel.handleManagerKeyEvent(e)) 
      return;

    /* local hotkeys */ 
    boolean unsupported = false;
    UserPrefs prefs = UserPrefs.getInstance();
    if((prefs.getUpdate() != null) &&
       prefs.getUpdate().wasPressed(e))
      updatePanels();
    else {
      if((prefs.getJobBrowserToggleViewsFilter() != null) &&
	 prefs.getJobBrowserToggleViewsFilter().wasPressed(e))
	doViewFilterChanged();
      else if((prefs.getJobBrowserSingleViewFilter() != null) &&
	      prefs.getJobBrowserSingleViewFilter().wasPressed(e))
	doSingleViewFilter();
      else if((prefs.getJobBrowserOwnedViewsFilter() != null) &&
	      prefs.getJobBrowserOwnedViewsFilter().wasPressed(e))
	doOwnedViewsFilter();
      else if((prefs.getJobBrowserAllViewsFilter() != null) &&
	      prefs.getJobBrowserAllViewsFilter().wasPressed(e))
	doAllViewsFilter();
      
      else if((prefs.getQueueJobs() != null) &&
	      prefs.getQueueJobs().wasPressed(e))
	doGroupsQueueJobs();
      else if((prefs.getQueueJobsSpecial() != null) &&
	      prefs.getQueueJobsSpecial().wasPressed(e))
	doGroupsQueueJobsSpecial();
      else if((prefs.getPauseJobs() != null) &&
	      prefs.getPauseJobs().wasPressed(e))
	doGroupsPauseJobs();
      else if((prefs.getResumeJobs() != null) &&
	      prefs.getResumeJobs().wasPressed(e))
	doGroupsResumeJobs();
      else if((prefs.getKillJobs() != null) &&
	      prefs.getKillJobs().wasPressed(e))
	doGroupsKillJobs();
      else if((prefs.getJobBrowserGroupsDelete() != null) &&
	      prefs.getJobBrowserGroupsDelete().wasPressed(e))
	doGroupsDelete();
      else if((prefs.getJobBrowserGroupsDeleteCompleted() != null) &&
	      prefs.getJobBrowserGroupsDeleteCompleted().wasPressed(e))
	doGroupsDeleteCompleted();
      else 
	unsupported = true;
    }

    if(unsupported) {
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
    if(cmd.equals("update")) 
      updatePanels();

    else if(cmd.equals("view-filter-changed")) 
      doViewFilterChanged();
    else if(cmd.equals("single-view")) 
      doSingleViewFilter();
    else if(cmd.equals("owned-views")) 
      doOwnedViewsFilter();
    else if(cmd.equals("all-views")) 
      doAllViewsFilter();

    else if(cmd.equals("groups-queue-jobs")) 
      doGroupsQueueJobs();
    else if(cmd.equals("groups-queue-jobs-special")) 
      doGroupsQueueJobsSpecial();
    else if(cmd.equals("groups-pause-jobs")) 
      doGroupsPauseJobs();
    else if(cmd.equals("groups-pause-jobs")) 
      doGroupsPauseJobs();
    else if(cmd.equals("groups-resume-jobs")) 
      doGroupsResumeJobs();
    else if(cmd.equals("groups-preempt-jobs")) 
      doGroupsPreemptJobs();
    else if(cmd.equals("groups-kill-jobs")) 
      doGroupsKillJobs();

    else if(cmd.equals("delete-group")) 
      doGroupsDelete();
    else if(cmd.equals("delete-completed")) 
      doGroupsDeleteCompleted();
  }




  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Toggle the view filter between single view, owned views and all views. 
   */ 
  public void 
  doViewFilterChanged()
  {
    switch(pViewFilter) {
    case SingleView:
      pViewFilter = ViewFilter.OwnedViews;
      break;

    case OwnedViews:
      pViewFilter = ViewFilter.AllViews;
      break;

    case AllViews:
      pViewFilter = ViewFilter.SingleView;
    }

    pViewFilterButton.setName(pViewFilter + "Button");
    updatePanels();
  }
  
  /**
   * Show only job groups owned by the current view.
   */ 
  public void 
  doSingleViewFilter() 
  {
    pViewFilter = ViewFilter.SingleView;
    pViewFilterButton.setName(pViewFilter + "Button");
    updatePanels();
  }
  
  /**
   * Show job groups from any view owned by the current user.
   */ 
  public void 
  doOwnedViewsFilter() 
  {
    pViewFilter = ViewFilter.OwnedViews;
    pViewFilterButton.setName(pViewFilter + "Button");
    updatePanels();
  }
  
  /**
   * Show job groups from all views.
   */ 
  public void 
  doAllViewsFilter() 
  {
    pViewFilter = ViewFilter.AllViews;
    pViewFilterButton.setName(pViewFilter + "Button");
    updatePanels();
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Resubmit all aborted and failed jobs associated with the selected job groups.
   */ 
  public void 
  doGroupsQueueJobs()
  {
    TreeMap<NodeID,TreeSet<FileSeq>> targets = getQueuedFileSeqs();
    if(!targets.isEmpty()) {
      QueueJobsTask task = new QueueJobsTask(targets);
      task.start();
    }
  }

  /**
   * Resubmit all aborted and failed jobs associated with the selected job groups with
   * special job requirements.
   */ 
  public void 
  doGroupsQueueJobsSpecial()
  {
    TreeMap<NodeID,TreeSet<FileSeq>> targets = getQueuedFileSeqs();
    if(!targets.isEmpty()) {
      JQueueJobsDialog diag = UIMaster.getInstance().showQueueJobsDialog();
      if(diag.wasConfirmed()) {
	Integer batchSize = null;
	if(diag.overrideBatchSize()) 
	  batchSize = diag.getBatchSize();
	
	Integer priority = null;
	if(diag.overridePriority()) 
	  priority = diag.getPriority();
	
	Integer interval = null;
	if(diag.overrideRampUp()) 
	  interval = diag.getRampUp();
	  
	TreeSet<String> keys = null;
	if(diag.overrideSelectionKeys()) 
	  keys = diag.getSelectionKeys();
	
	QueueJobsTask task = new QueueJobsTask(targets, batchSize, priority, interval, keys);
	task.start();
      }
    }
  }

  /** 
   * Get the target file sequences of the aborted and failed selected root jobs.
   */ 
  private TreeMap<NodeID,TreeSet<FileSeq>> 
  getQueuedFileSeqs() 
  {
    TreeMap<NodeID,TreeSet<FileSeq>> targets = new TreeMap<NodeID,TreeSet<FileSeq>>();
    for(Long groupID : getSelectedGroupIDs()) {
      QueueJobGroup group = pJobGroups.get(groupID);
      if(group != null) {
	for(Long jobID : group.getRootIDs()) {
	  JobStatus status = pJobStatus.get(jobID);
	  NodeID targetID = null;
	  if(status != null) {
	    switch(status.getState()) {
	    case Aborted:
	    case Failed:
	      targetID = status.getNodeID();
	    }
	  }
      
	  if(targetID != null) {
	    String author = targetID.getAuthor();
	    if(author.equals(PackageInfo.sUser) || pPrivilegeDetails.isQueueManaged(author)) {

	      TreeSet<FileSeq> fseqs = targets.get(targetID);
	      if(fseqs == null) {
		fseqs = new TreeSet<FileSeq>();
		targets.put(targetID, fseqs);
	      }
	      
	      fseqs.add(status.getTargetSequence());
	    }
	  }
	}
      }
    }

    return targets;
  }

  /**
   * Pause all waiting jobs associated with the selected job groups.
   */ 
  public void 
  doGroupsPauseJobs()
  {
    TreeMap<String,TreeSet<Long>> jobs = new TreeMap<String,TreeSet<Long>>();
    for(Long groupID : getSelectedGroupIDs()) {
      QueueJobGroup group = pJobGroups.get(groupID);
      if(group != null) {
	for(Long jobID : group.getJobIDs()) {
	  JobStatus status = pJobStatus.get(jobID);
	  String author = null;
	  if(status != null) {
	    switch(status.getState()) {
	    case Queued:
	    case Preempted:
	      author = status.getNodeID().getAuthor();
	    }
	  }
      
	  if((author != null) && 
	     (author.equals(PackageInfo.sUser) || pPrivilegeDetails.isQueueManaged(author))) {
	    TreeSet<Long> paused = jobs.get(author);
	    if(paused == null) {
	      paused = new TreeSet<Long>();
	      jobs.put(author, paused);
	    }
	    paused.add(jobID);
	  }
	}
      }
    }

    if(!jobs.isEmpty()) {
      PauseJobsTask task = new PauseJobsTask(jobs);
      task.start();
    }
  }
  
  /**
   * Resume execution of all paused jobs associated with the selected job groups.
   */ 
  public void 
  doGroupsResumeJobs()
  {
    TreeMap<String,TreeSet<Long>> jobs = new TreeMap<String,TreeSet<Long>>();
    for(Long groupID : getSelectedGroupIDs()) {
      QueueJobGroup group = pJobGroups.get(groupID);
      if(group != null) {
	for(Long jobID : group.getJobIDs()) {
	  JobStatus status = pJobStatus.get(jobID);
	  String author = null;
	  if(status != null) {
	    switch(status.getState()) {
	    case Paused:
	      author = status.getNodeID().getAuthor();
	    }
	  }
      
	  if((author != null) &&
	     (author.equals(PackageInfo.sUser) || pPrivilegeDetails.isQueueManaged(author))) {
	    TreeSet<Long> resumed = jobs.get(author);
	    if(resumed == null) {
	      resumed = new TreeSet<Long>();
	      jobs.put(author, resumed);
	    }
	    resumed.add(jobID);
	  }
	}
      }
    }

    if(!jobs.isEmpty()) {
      ResumeJobsTask task = new ResumeJobsTask(jobs);
      task.start();
    }
  }

  /**
   * Preempt all jobs associated with the selected groups.
   */ 
  public void 
  doGroupsPreemptJobs()
  {
    TreeMap<String,TreeSet<Long>> jobs = new TreeMap<String,TreeSet<Long>>();
    for(Long groupID : getSelectedGroupIDs()) {
      QueueJobGroup group = pJobGroups.get(groupID);
      if(group != null) {
	for(Long jobID : group.getJobIDs()) {
	  JobStatus status = pJobStatus.get(jobID);
	  String author = null;
	  if(status != null) {
	    switch(status.getState()) {
	    case Paused:
	    case Queued:
	    case Preempted:
	    case Running:
	      author = status.getNodeID().getAuthor();
	    }
	  }
      
	  if((author != null) &&
	     (author.equals(PackageInfo.sUser) || pPrivilegeDetails.isQueueManaged(author))) {
	    TreeSet<Long> dead = jobs.get(author);
	    if(dead == null) {
	      dead = new TreeSet<Long>();
	      jobs.put(author, dead);
	    }
	    dead.add(jobID);
	  }
	}
      }
    }

    if(!jobs.isEmpty()) {
      PreemptJobsTask task = new PreemptJobsTask(jobs);
      task.start();
    }
  }

  /**
   * Kill all jobs associated with the selected groups.
   */ 
  public void 
  doGroupsKillJobs()
  {
    TreeMap<String,TreeSet<Long>> jobs = new TreeMap<String,TreeSet<Long>>();
    for(Long groupID : getSelectedGroupIDs()) {
      QueueJobGroup group = pJobGroups.get(groupID);
      if(group != null) {
	for(Long jobID : group.getJobIDs()) {
	  JobStatus status = pJobStatus.get(jobID);
	  String author = null;
	  if(status != null) {
	    switch(status.getState()) {
	    case Paused:
	    case Queued:
	    case Preempted:
	    case Running:
	      author = status.getNodeID().getAuthor();
	    }
	  }
      
	  if((author != null) &&
	     (author.equals(PackageInfo.sUser) || pPrivilegeDetails.isQueueManaged(author))) {
	    TreeSet<Long> dead = jobs.get(author);
	    if(dead == null) {
	      dead = new TreeSet<Long>();
	      jobs.put(author, dead);
	    }
	    dead.add(jobID);
	  }
	}
      }
    }

    if(!jobs.isEmpty()) {
      KillJobsTask task = new KillJobsTask(jobs);
      task.start();
    }
  }

  /**
   * Delete the selected completed job groups.
   */ 
  public void
  doGroupsDelete()
  { 
    TreeMap<Long,String> groups = new TreeMap<Long,String>();
    for(Long groupID : getSelectedGroupIDs()) {
      QueueJobGroup group = pJobGroups.get(groupID);
      if(group != null) {
	String author = group.getNodeID().getAuthor();
	if((author != null) &&
	   (author.equals(PackageInfo.sUser) || pPrivilegeDetails.isQueueManaged(author))) 
	  groups.put(groupID, author);
      }
    }

    if(!groups.isEmpty()) {
      DeleteJobGroupsTask task = new DeleteJobGroupsTask(groups);
      task.start();
    }
  }

  /**
   * Delete all completed job groups.
   */ 
  public void
  doGroupsDeleteCompleted()
  { 
    DeleteCompletedJobGroupsTask task = new DeleteCompletedJobGroupsTask(pViewFilter); 
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
    
    encoder.encode("ViewFilter", pViewFilter);

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
    ViewFilter filter = (ViewFilter) decoder.decode("ViewFilter");
    if(filter != null) {
      pViewFilter = filter; 
      pViewFilterButton.setName(pViewFilter + "Button");
    }

    if(UIMaster.getInstance().restoreSelections()) {
      TreeSet<Long> selected = (TreeSet<Long>) decoder.decode("SelectedIDs");
      if(selected != null) 
	pSelectedIDs.addAll(selected);
    }

    super.fromGlue(decoder);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The working area view filter.
   */
  protected 
  enum ViewFilter
  {  
    /**
     * Show only job groups owned by the current view.
     */
    SingleView, 

    /**
     * Show job groups from any view owned by the current user.
     */ 
    OwnedViews, 

    /**
     * Show job groups from all views.
     */ 
    AllViews; 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Makes the given component have the keyboard focuse when the mouse is over it.
   */ 
  private 
  class KeyFocuser
    extends MouseAdapter
  {
    KeyFocuser
    (
     Component comp
    ) 
    {
      pComp = comp;
    }

    /**
     * Invoked when the mouse enters a component. 
     */ 
    public void 
    mouseEntered
    (
     MouseEvent e
    ) 
    {
      pComp.requestFocusInWindow();
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

    private Component  pComp;
  }


  /*----------------------------------------------------------------------------------------*/

  private 
  class JGroupsTablePanel 
    extends JTablePanel 
  {
    public 
    JGroupsTablePanel
    (
     AbstractSortableTableModel model,
     GroupsListSelector selector
    ) 
    {
      super(model);

      pSelector = selector;
      ListSelectionModel smodel = getTable().getSelectionModel();
      smodel.addListSelectionListener(pSelector);
    }

    protected void 
    doSort
    (
     int col
    )
    { 
      ListSelectionModel smodel = getTable().getSelectionModel();
      smodel.removeListSelectionListener(pSelector);
      super.doSort(col);
      smodel.addListSelectionListener(pSelector);
    }

    private static final long serialVersionUID = 6636530854965182895L;

    private GroupsListSelector  pSelector; 
  }
  

  /*----------------------------------------------------------------------------------------*/

  private 
  class GroupsListSelector
    implements ListSelectionListener
  {
    public 
    GroupsListSelector() 
    {}

    /**
     * Gets the ID of the last selected job group if it was the only selection and was 
     * added to the selection since the last update or <CODE>null</CODE> if not.
     */ 
    public Long
    getJustSelected()
    {
      return pJustSelected;
    }

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
      
      int numPrevSelected = pSelectedIDs.size();

      /* update selected IDs */ 
      {
	pSelectedIDs.clear(); 
	int rows[] = pGroupsTablePanel.getTable().getSelectedRows();
	int wk;
	for(wk=0; wk<rows.length; wk++) {
	  Long groupID = pGroupsTableModel.getGroupID(rows[wk]);
	  pSelectedIDs.add(groupID);
	}
      }

      pJustSelected = null;    
      if((pSelectedIDs.size() == 1) && (numPrevSelected <= 1))
	pJustSelected = pSelectedIDs.first();
  
      updatePanels(true);      
    }

    private Long  pJustSelected; 
  }

 
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Resubmit jobs to the queue for the given file sequences.
   */ 
  private
  class QueueJobsTask
    extends Thread
  {
    public 
    QueueJobsTask
    (
     TreeMap<NodeID,TreeSet<FileSeq>> targets
    ) 
    {
      this(targets, null, null, null, null);
    }
    
    public 
    QueueJobsTask
    (
     TreeMap<NodeID,TreeSet<FileSeq>> targets,
     Integer batchSize, 
     Integer priority, 
     Integer rampUp, 
     TreeSet<String> selectionKeys
    ) 
    {
      super("JQueueJobsBrowserPanel:QueueJobsTask");

      pTargets       = targets;
      pBatchSize     = batchSize;
      pPriority      = priority; 
      pRampUp        = rampUp; 
      pSelectionKeys = selectionKeys;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID)) {
	try {
	  for(NodeID nodeID : pTargets.keySet()) {
	    master.updatePanelOp(pGroupID, 
				 "Resubmitting Jobs to the Queue: " + nodeID.getName());
	    MasterMgrClient client = master.getMasterMgrClient(pGroupID);
	    client.resubmitJobs
	      (nodeID, pTargets.get(nodeID), pBatchSize, pPriority, pRampUp, pSelectionKeys);
	  }
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp(pGroupID, "Done.");
	}

	updatePanels();
      }
    }

    private TreeMap<NodeID,TreeSet<FileSeq>>  pTargets;
    private Integer                           pBatchSize;
    private Integer                           pPriority;
    private Integer                           pRampUp; 
    private TreeSet<String>                   pSelectionKeys;
  }

  /** 
   * Pause the given jobs.
   */ 
  private
  class PauseJobsTask
    extends Thread
  {
    public 
    PauseJobsTask
    (
     TreeMap<String,TreeSet<Long>> jobs
    ) 
    {
      super("JQueueJobsBrowserPanel:PauseJobsTask");

      pJobs = jobs; 
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Pausing Jobs...")) {
	try {
	  for(String author : pJobs.keySet()) {
	    TreeSet<Long> jobIDs = pJobs.get(author);
	    master.getQueueMgrClient(pGroupID).pauseJobs(author, jobIDs);
	  }
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp(pGroupID, "Done.");
	}

	updatePanels();
      }
    }

    private TreeMap<String,TreeSet<Long>>  pJobs; 
  }

  /** 
   * Resume execution of the the given paused jobs.
   */ 
  private
  class ResumeJobsTask
    extends Thread
  {
    public 
    ResumeJobsTask
    (
     TreeMap<String,TreeSet<Long>> jobs
    ) 
    {
      super("JQueueJobsBrowserPanel:ResumeJobsTask");

      pJobs = jobs; 
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Resuming Paused Jobs...")) {
	try {
	  for(String author : pJobs.keySet()) {
	    TreeSet<Long> jobIDs = pJobs.get(author);
	    master.getQueueMgrClient(pGroupID).resumeJobs(author, jobIDs);
	  }
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp(pGroupID, "Done.");
	}

	updatePanels();
      }
    }

    private TreeMap<String,TreeSet<Long>>  pJobs; 
  }

  /** 
   * Preempt the given jobs.
   */ 
  private
  class PreemptJobsTask
    extends Thread
  {
    public 
    PreemptJobsTask
    (
     TreeMap<String,TreeSet<Long>> jobs
    ) 
    {
      super("JQueueJobsBrowserPanel:PreemptJobsTask");

      pJobs = jobs; 
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Preempting Jobs...")) {
	try {
	  for(String author : pJobs.keySet()) {
	    TreeSet<Long> jobIDs = pJobs.get(author);
	    master.getQueueMgrClient(pGroupID).preemptJobs(author, jobIDs);
	  }
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp(pGroupID, "Done.");
	}

	updatePanels();
      }
    }

    private TreeMap<String,TreeSet<Long>>  pJobs; 
  }

  /** 
   * Kill the given jobs.
   */ 
  private
  class KillJobsTask
    extends Thread
  {
    public 
    KillJobsTask
    (
     TreeMap<String,TreeSet<Long>> jobs
    ) 
    {
      super("JQueueJobsBrowserPanel:KillJobsTask");

      pJobs = jobs; 
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Killing Jobs...")) {
	try {
	  for(String author : pJobs.keySet()) {
	    TreeSet<Long> jobIDs = pJobs.get(author);
	    master.getQueueMgrClient(pGroupID).killJobs(author, jobIDs);
	  }
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp(pGroupID, "Done.");
	}

	updatePanels();
      }
    }

    private TreeMap<String,TreeSet<Long>>  pJobs; 
  }

  /** 
   * Delete the selected completed job groups.
   */ 
  private
  class DeleteJobGroupsTask
    extends Thread
  {
    public 
    DeleteJobGroupsTask
    (
     TreeMap<Long,String> groups
    ) 
    {
      super("JQueueJobsBrowserPanel:DeleteJobGroupsTask");

      pGroups = groups;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Deleting Job Groups...")) {
	try {
	  master.getQueueMgrClient(pGroupID).deleteJobGroups(pGroups);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp(pGroupID, "Done.");
	}

	updatePanels();
      }
    }

    private TreeMap<Long,String> pGroups;
  }

  /** 
   * Delete the completed job groups.
   */ 
  private
  class DeleteCompletedJobGroupsTask
    extends Thread
  {
    public 
    DeleteCompletedJobGroupsTask
    (
     ViewFilter filter
    ) 
    {
      super("JQueueJobsBrowserPanel:DeleteCompletedJobGroupsTask");

      pFilter = filter;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Deleting Job Groups...")) {
	try {
	  MasterMgrClient mclient = master.getMasterMgrClient(pGroupID);
	  QueueMgrClient qclient  = master.getQueueMgrClient(pGroupID);
	  switch(pFilter) {
	  case SingleView:
	    qclient.deleteViewJobGroups(pAuthor, pView);
	    break;
	    
	  case OwnedViews:
	    {
	      TreeMap<String,TreeSet<String>> all = mclient.getWorkingAreas();
	      TreeSet<String> views = all.get(pAuthor);
	      if(views != null) {
		for(String view : views) 
		  qclient.deleteViewJobGroups(pAuthor, view);
	      }
	    }
	    break;
	    
	  case AllViews:
	    qclient.deleteAllJobGroups();
	  }
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp(pGroupID, "Done.");
	}

	updatePanels();
      }
    }

    private ViewFilter  pFilter; 
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

  /**
   * The working area view filter.
   */
  private ViewFilter  pViewFilter; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The groups popup menu.
   */ 
  private JPopupMenu  pGroupsPopup;
  
  /**
   * The groups popup menu items.
   */ 
  private JMenuItem  pGroupsUpdateItem; 
  private JMenuItem  pGroupsSingleViewItem; 
  private JMenuItem  pGroupsOwnedViewsItem; 
  private JMenuItem  pGroupsAllViewsItem; 
  private JMenuItem  pGroupsQueueItem; 
  private JMenuItem  pGroupsQueueSpecialItem; 
  private JMenuItem  pGroupsPauseItem; 
  private JMenuItem  pGroupsResumeItem; 
  private JMenuItem  pGroupsPreemptItem;
  private JMenuItem  pGroupsKillItem;
  private JMenuItem  pGroupsDeleteItem;
  private JMenuItem  pGroupsDeleteCompletedItem;


  /**
   * The header panel.
   */ 
  private JPanel pGroupsHeaderPanel; 

  /**
   * Used to select the current view filter.
   */ 
  private JButton  pViewFilterButton;  

  /**
   * Deletes completed job groups when pressed.
   */ 
  private JButton  pDeleteCompletedButton;

  /**
   * The job groups table model.
   */ 
  private QueueJobGroupsTableModel  pGroupsTableModel;

  /**
   * The job groups table panel.
   */ 
  private JGroupsTablePanel  pGroupsTablePanel;

  /**
   * The list selection listener.
   */ 
  private GroupsListSelector  pGroupsListSelector;

}
