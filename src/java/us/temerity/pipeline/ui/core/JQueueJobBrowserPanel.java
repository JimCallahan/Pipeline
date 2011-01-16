// $Id: JQueueJobBrowserPanel.java,v 1.47 2010/01/07 11:15:58 jim Exp $

package us.temerity.pipeline.ui.core;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Map.*;

import javax.swing.*;
import javax.swing.event.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.laf.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   J O B   B R O W S E R   P A N E L                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A panel which displays the job groups, servers and slots.
 */ 
public 
class JQueueJobBrowserPanel
  extends JTopLevelPanel
  implements MouseListener, KeyListener, ActionListener, PopupMenuListener
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
      pJobGroups    = new TreeMap<Long,QueueJobGroup>(); 
      pJobStateDist = new TreeMap<Long,double[]>();
      pSelectedIDs  = new TreeSet<Long>();

      pStatusFilter = StatusFilter.AllGroups;
      pViewFilter = NewViewFilter.MyJobs;
      pFilterOverride = null;
    }
    
    /* initialize the popup menus */
    pShowNodeChannelItems = new ArrayList<JMenuItem>();
    {
      JMenuItem item;
      	
      {	
	pGroupsPopup = new JPopupMenu();

	item = new JMenuItem("Update");
	pGroupsUpdateItem = item;
	item.setActionCommand("update");
	item.addActionListener(this);
	pGroupsPopup.add(item);

	item = new JMenuItem("Show Nodes");
	pGroupShowNodeItem = item;
	item.setActionCommand("show-node");
	item.addActionListener(this);
	pGroupsPopup.add(item);
	      
	{
	  JMenu showNodeMenu = new JMenu("Show Nodes...");
	  for (int i = 1; i < 10; i++) {
	    item = new JMenuItem("Channel " + i);
	    item.setActionCommand("show-node-" + i);
	    item.addActionListener(this);
	    pShowNodeChannelItems.add(item);
	    showNodeMenu.add(item);
	  }
	  pGroupsPopup.add(showNodeMenu);
	}
	
	pGroupsPopup.addSeparator();
	
	item = new JMenuItem("Clear Focus");
	pClearFocusItem = item;
	item.setActionCommand("clear-focus");
	item.addActionListener(this);
	pGroupsPopup.add(item);
	
	item = new JMenuItem("Monitor Groups");
        pMonitorGroupsItem = item;
        item.setActionCommand("monitor-groups");
        item.addActionListener(this);
        pGroupsPopup.add(item);
	
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

	item = new JMenuItem("Change Job Reqs");
	pGroupsChangeJobReqsItem = item;
	item.setActionCommand("groups-change-job-reqs");
	item.addActionListener(this);
	pGroupsPopup.add(item);
	
	item = new JMenuItem("Update Job Keys");
	pGroupsUpdateJobKeysItem = item;
	item.setActionCommand("groups-update-job-keys");
        item.addActionListener(this);
        pGroupsPopup.add(item);

	item = new JMenuItem("Update All Job Keys");
        pUpdateAllJobKeysItem = item;
        item.setActionCommand("update-all-job-keys");
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
    }
    
    {
      JRadioButtonMenuItem item;
      ButtonGroup bgroup = new ButtonGroup();
      
      pStatusButtons = new TreeMap<StatusFilter, JRadioButtonMenuItem>();
      
      {
        pStatusPopup = new JPopupMenu();
        
        item = new JRadioButtonMenuItem("Everything");
        item.setActionCommand("filter-all-jobs");
        item.addActionListener(this);
        pStatusPopup.add(item);
        bgroup.add(item);
        pStatusButtons.put(StatusFilter.AllGroups, item);
        bgroup.setSelected(item.getModel(), true);
        
        pStatusPopup.addSeparator();
        
        item = new JRadioButtonMenuItem("Any Failed");
        item.setActionCommand("filter-any-failed");
        item.addActionListener(this);
        pStatusPopup.add(item);
        bgroup.add(item);
        pStatusButtons.put(StatusFilter.AnyFailed, item);
        
        item = new JRadioButtonMenuItem("Any Limbo");
        item.setActionCommand("filter-any-limbo");
        item.addActionListener(this);
        pStatusPopup.add(item);
        bgroup.add(item);
        pStatusButtons.put(StatusFilter.AnyLimbo, item);
        
        item = new JRadioButtonMenuItem("Any Running");
        item.setActionCommand("filter-any-running");
        item.addActionListener(this);
        pStatusPopup.add(item);
        bgroup.add(item);
        pStatusButtons.put(StatusFilter.AnyRunning, item);
        
        item = new JRadioButtonMenuItem("Any Paused");
        item.setActionCommand("filter-any-paused");
        item.addActionListener(this);
        pStatusPopup.add(item);
        bgroup.add(item);
        pStatusButtons.put(StatusFilter.AnyPaused, item);
        
        pStatusPopup.addSeparator();
        
        item = new JRadioButtonMenuItem("All Completed");
        item.setActionCommand("filter-all-completed");
        item.addActionListener(this);
        pStatusPopup.add(item);
        bgroup.add(item);
        pStatusButtons.put(StatusFilter.AllCompleted, item);

        item = new JRadioButtonMenuItem("All Waiting");
        item.setActionCommand("filter-all-waiting");
        item.addActionListener(this);
        pStatusPopup.add(item);
        bgroup.add(item);
        pStatusButtons.put(StatusFilter.AllWaiting, item);
        
        item = new JRadioButtonMenuItem("All Terminated");
        item.setActionCommand("filter-all-terminated");
        item.addActionListener(this);
        pStatusPopup.add(item);
        bgroup.add(item);
        pStatusButtons.put(StatusFilter.AllTerminated, item);
      }
      
      pStatusButtonGroup = bgroup;
      pStatusPopup.addPopupMenuListener(this);
    }
    
    {
      JRadioButtonMenuItem item;
      ButtonGroup bgroup = new ButtonGroup();
      
      pViewButtons = new TreeMap<NewViewFilter, JRadioButtonMenuItem>();
      
      {
        pViewPopup = new JPopupMenu();
        
        item = new JRadioButtonMenuItem("My Views");
        item.setActionCommand("view-my-jobs");
        item.addActionListener(this);
        pViewPopup.add(item);
        bgroup.add(item);
        pViewButtons.put(NewViewFilter.MyJobs, item);
        bgroup.setSelected(item.getModel(), true);
        
        item = new JRadioButtonMenuItem("All Views");
        item.setActionCommand("view-all-jobs");
        item.addActionListener(this);
        pViewPopup.add(item);
        bgroup.add(item);
        pViewButtons.put(NewViewFilter.AllJobs, item);
        
        item = new JRadioButtonMenuItem("Current View");
        item.setActionCommand("view-working-area-jobs");
        item.addActionListener(this);
        pViewPopup.add(item);
        bgroup.add(item);
        pViewButtons.put(NewViewFilter.Default, item);

        pViewPopup.addSeparator();
        
        pViewUserMenu = new JMenu("By User");
        pViewPopup.add(pViewUserMenu);
        pViewUserButtons = new TreeMap<String, JRadioButtonMenuItem>();
        
        pViewGroupMenu = new JMenu("By Group");
        pViewPopup.add(pViewGroupMenu);
        pViewGroupButtons = new TreeMap<String, JRadioButtonMenuItem>();
        
         pViewPopup.addSeparator();
        
         item = new JRadioButtonMenuItem("Custom Users...");
         item.setActionCommand("view-custom-jobs");
         item.addActionListener(this);
         item.setEnabled(false);
         pViewPopup.add(item);
         bgroup.add(item);
         pViewButtons.put(NewViewFilter.CustomJobs, item);
      }

      pViewButtonGroup = bgroup;
      pViewPopup.addPopupMenuListener(this);
    }

    updateMenuToolTips();
    
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
	  pHeaderLabel = label;
	    
	  panel.add(label);	  
	}
	  
	panel.add(Box.createHorizontalGlue());
	
	{
          JLabel anchor = new JLabel();
          anchor.setIcon(sFilterAnchorIcon);

          Dimension size = new Dimension(39, 24);
          anchor.setMinimumSize(size);
          anchor.setMaximumSize(size);
          anchor.setPreferredSize(size);

          anchor.addMouseListener(this);
          
          panel.add(anchor);
          
          pStatusMenuLabel = anchor;
        }
	
	panel.add(Box.createRigidArea(new Dimension(10, 0)));
	
	{
	  JLabel anchor = new JLabel();
	  anchor.setIcon(sViewAnchorIcon);

	  Dimension size = new Dimension(39, 24);
	  anchor.setMinimumSize(size);
	  anchor.setMaximumSize(size);
	  anchor.setPreferredSize(size);

	  anchor.addMouseListener(this);

	  panel.add(anchor);

	  pViewMenuLabel = anchor;
	}

	panel.add(Box.createRigidArea(new Dimension(10, 0)));
	
	{
          JButton btn = new JButton();          
          pUpdateAllJobKeysButton = btn;
          btn.setName("StaleKeyChooserButton");
          btn.setToolTipText(UIFactory.formatToolTip(
            "Rerun the Key Chooser plugins for all existing jobs.  This is advisable after " + 
            "modifying or adding a new Key Chooser and wish to see its effects on existing " + 
            "job dispatch."));

          btn.setSelected(true);
            
          Dimension size = new Dimension(19, 19);
          btn.setMinimumSize(size);
          btn.setMaximumSize(size);
          btn.setPreferredSize(size);
            
          btn.setActionCommand("update-all-job-keys");
          btn.addActionListener(this);
          
          btn.setEnabled(false);
            
          panel.add(btn);
        } 
          
        panel.add(Box.createRigidArea(new Dimension(10, 0)));
	  
	{
	  JButton btn = new JButton();		
	  pDeleteCompletedButton = btn;
	  btn.setName("DeleteCompletedButton");
          btn.setToolTipText(UIFactory.formatToolTip(
            "Remove any completed Job Groups for which you have sufficient privileges."));
	    
	  btn.setSelected(true);
	    
	  Dimension size = new Dimension(19, 19);
	  btn.setMinimumSize(size);
	  btn.setMaximumSize(size);
	  btn.setPreferredSize(size);
	    
	  btn.setActionCommand("delete-completed");
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
 
    updatePanelHeader();
    updateJobs(null, null);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Get the title of this type of panel.
   */
  @Override
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
  @Override
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
  @Override
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
  @Override
  public boolean
  isLocked() 
  {
    return (super.isLocked() && !pPrivilegeDetails.isQueueManaged(pAuthor)); 
  }

  /**
   * Set the author and view.
   */ 
  @Override
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
      if(groupID != null) 
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

  /**
   * Select all job groups which are in the current working area.
   */ 
  public void 
  selectAllGroupsInWorkingArea()
  {
    JTable table = pGroupsTablePanel.getTable();
    ListSelectionModel smodel = table.getSelectionModel();
    
    smodel.removeListSelectionListener(pGroupsListSelector);
    { 
      pSelectedIDs.clear();
      table.clearSelection();
      
      int row; 
      for(row=0; row<table.getRowCount(); row++) {
        Long groupID = pGroupsTableModel.getGroupID(row);
        if(groupID != null) {
          QueueJobGroup group = pJobGroups.get(groupID);
          if(group != null) {
            NodeID nodeID = group.getNodeID();
            if(nodeID.getAuthor().equals(pAuthor) && nodeID.getView().equals(pView)) {
              pSelectedIDs.add(groupID);
              table.addRowSelectionInterval(row, row); 
            }
          }
        }
      }
    }      
    smodel.addListSelectionListener(pGroupsListSelector);

    updatePanels(); 
  }

  /**
   * Deselect the given job groups.
   */ 
  public void 
  deselectGroups
  (
   TreeSet<Long> groupIDs
  ) 
  {
    if(groupIDs.isEmpty()) 
      return;

    JTable table = pGroupsTablePanel.getTable();
    ListSelectionModel smodel = table.getSelectionModel();
    
    smodel.removeListSelectionListener(pGroupsListSelector);
    { 
      int rows[] = table.getSelectedRows();
      int wk;
      for(wk=0; wk<rows.length; wk++) {
	Long gid = pGroupsTableModel.getGroupID(rows[wk]);
	if((gid != null) && groupIDs.contains(gid)) 
	  table.removeRowSelectionInterval(wk, wk);
      }

      pSelectedIDs.removeAll(groupIDs); 
    }      
    smodel.addListSelectionListener(pGroupsListSelector);

    updatePanels(true);      
  }

  /**
   * Deselect all job groups. 
   */ 
  public void 
  deselectAllGroups() 
  {
    pGroupsTablePanel.getTable().clearSelection();
  }

  
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The working area view filter.
   */
  public NewViewFilter 
  getViewFilter()
  {
    return pViewFilter; 
  }

  /**
   * Get the name of the user being filtered on when the viewfilter is set to
   * {@link NewViewFilter#UserJobs}
   */
  public String
  getUserFilter()
  {
    return pUserFilter;
  }
  
  /**
   * Get the name of the group being filtered on when the viewfilter is set to
   * {@link NewViewFilter#GroupJobs}
   */
  public String
  getGroupFilter()
  {
    return pGroupFilter;
  }
  
  /**
   * Get the set of users being filtered on when the viewfilter is set to 
   * {@link NewViewFilter#CustomJobs}
   */
  public Set<String>
  getCustomFilter()
  {
    if (pCustomFilter == null)
      return null;
    else
      return Collections.unmodifiableSet(pCustomFilter);
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The set of job group ids which comprise the filter override or <code>null</code> if 
   * there is no filter override.
   */
  public Set<Long>
  getFilterOverride()
  {
    if (pFilterOverride == null)
      return null;
    else
      return Collections.unmodifiableSet(pFilterOverride);
  }
  
  /**
   * Set the job groups ids which comprise the filter override or <code>null</code> to clear 
   * the override. <p>
   * 
   * Note that this does NOT cause a panel update.  On order for this chane to appear to the 
   * user, a panel update needs to be trigger after this is called.
   * 
   * @param filterOverride
   *   The set of job group ids or <code>null</code>.
   */
  public void
  setFilterOverride
  (
    Set<Long> filterOverride
  )
  {
    if (filterOverride == null)
      pFilterOverride = null;
    else
      pFilterOverride = new TreeSet<Long>(filterOverride);
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
    pSelectionModified = false;
    pModifierPressed = false;

    if (pGroupID != 0) {
      PanelUpdater pu = new PanelUpdater(this, selectionOnly);
      pu.execute();
    }
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
   *   The queue job groups indexed by job group ID.
   * 
   * @param dist
   *   The distribution of job states indexed by job group ID.
   *   
   * @param doJobKeysNeedUpdate
   *   A boolean which reflects whether key choosers need to be rerun for all jobs in the 
   *   queue.
   *   
   * @param wusers
   *   The set of all the current users.
   *   
   * @param wgroups
   *   The set of all the current groups.
   */
  public synchronized void 
  applyPanelUpdates
  (
   String author, 
   String view, 
   TreeMap<Long,QueueJobGroup> groups, 
   TreeMap<Long,double[]> dist,
   boolean doJobKeysNeedUpdate,
   Set<String> wusers,
   Set<String> wgroups
  )
  {
    if(!pAuthor.equals(author) || !pView.equals(view)) 
      super.setAuthorView(author, view);

    pWorkUsers = wusers;
    pWorkGroups = wgroups;
    
    updateViewMenu();

    pDoJobKeysNeedUpdate = doJobKeysNeedUpdate;
    
    updatePanelHeader();
    
    updateJobs(groups, dist);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Perform any operations needed before an panel operation starts. <P> 
   * 
   * This method is run by the Swing Event thread.
   */ 
  @Override
  public void 
  prePanelOp() 
  {
    super.prePanelOp(); 
    
    if(pGroupsTablePanel != null) 
      pGroupsTablePanel.getTable().setEnabled(false);
  }

  /**
   * Perform any operations needed after an panel operation has completed. <P> 
   * 
   * This method is run by the Swing Event thread.
   */ 
  @Override
  public void 
  postPanelOp() 
  {
    if(pGroupsTablePanel != null) 
      pGroupsTablePanel.getTable().setEnabled(true);
    
    super.postPanelOp(); 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the jobs groups, servers and slots. 
   * 
   * @param groups
   *   The queue job groups indexe by job group ID.
   * 
   * @param dist
   *   The distribution of job states indexed by job group ID.
   */ 
  private synchronized void
  updateJobs
  (
   TreeMap<Long,QueueJobGroup> groups, 
   TreeMap<Long,double[]> dist
  ) 
  {
    updatePrivileges();

    /* enable/disable the DeleteCompleted button */ 
    switch(pViewFilter) {
    case MyJobs:
      pDeleteCompletedButton.setEnabled(true);
      break;

    case Default:
      pDeleteCompletedButton.setEnabled(!isLocked());
      break;
      
    default:
      pDeleteCompletedButton.setEnabled(pPrivilegeDetails.isQueueAdmin());
    }
    
    /* deal with the job keys need update button.*/
    if (!pDoJobKeysNeedUpdate) 
      pUpdateAllJobKeysButton.setEnabled(false);
    else if (pPrivilegeDetails.isQueueAdmin())
      pUpdateAllJobKeysButton.setEnabled(true);
    else
      pUpdateAllJobKeysButton.setEnabled(false);
    
    /* update the groups */ 
    pJobGroups.clear();
    if(groups != null) 
      pJobGroups.putAll(groups);

    /* update the job status */ 
    pJobStateDist.clear();
    if(dist != null) 
      pJobStateDist.putAll(dist);
    
    filterJobs();
  }
  
  /**
   * Update the job groups table, using the filtering information in the {@link StatusFilter} 
   * to determine which jobs to show.  <p>
   * 
   * Calling this method does not cause a server-roundtrip to update the job groups, so it can
   * be called very efficiently.
   */
  private void
  filterJobs()
  {
    TreeMap<Long, QueueJobGroup> groups = new TreeMap<Long, QueueJobGroup>();
    TreeMap<Long, double[]> dists = new TreeMap<Long, double[]>();
    
    for (Entry<Long, QueueJobGroup> entry : pJobGroups.entrySet()) {
      Long groupID = entry.getKey();
      double dist[] = pJobStateDist.get(groupID);
      switch (pStatusFilter) {
      case AllGroups:
        groups.put(groupID, entry.getValue());
        dists.put(groupID, dist);
        break;
      case AnyFailed:
        if (dist[JobState.Failed.ordinal()] > 0) {
          groups.put(groupID, entry.getValue());
          dists.put(groupID, dist);
        }
        break;
      case AnyLimbo:
        if (dist[JobState.Limbo.ordinal()] > 0) {
          groups.put(groupID, entry.getValue());
          dists.put(groupID, dist);
        }
        break;
      case AnyPaused:
        if (dist[JobState.Paused.ordinal()] > 0) {
          groups.put(groupID, entry.getValue());
          dists.put(groupID, dist);
        }
        break;
      case AnyRunning:
        if (dist[JobState.Running.ordinal()] > 0) {
          groups.put(groupID, entry.getValue());
          dists.put(groupID, dist);
        }
        break;
      case AllCompleted:
        if (dist[JobState.Limbo.ordinal()] == 0 &&
            dist[JobState.Paused.ordinal()] == 0 &&
            dist[JobState.Preempted.ordinal()] == 0 &&
            dist[JobState.Queued.ordinal()] == 0 &&
            dist[JobState.Running.ordinal()] == 0) {
          groups.put(groupID, entry.getValue());
          dists.put(groupID, dist);
        }
        break;
      case AllTerminated:
        if (dist[JobState.Limbo.ordinal()] == 0 &&
            dist[JobState.Paused.ordinal()] == 0 &&
            dist[JobState.Preempted.ordinal()] == 0 &&
            dist[JobState.Queued.ordinal()] == 0 &&
            dist[JobState.Running.ordinal()] == 0 &&
            dist[JobState.Finished.ordinal()] == 0) {
          groups.put(groupID, entry.getValue());
          dists.put(groupID, dist);
        }
       break;
      case AllWaiting:
        if (dist[JobState.Limbo.ordinal()] == 0 &&
            dist[JobState.Failed.ordinal()] == 0 &&
            dist[JobState.Aborted.ordinal()] == 0 &&
            dist[JobState.Finished.ordinal()] == 0 &&
            dist[JobState.Running.ordinal()] == 0) {
          groups.put(groupID, entry.getValue());
          dists.put(groupID, dist);
        }
        break;
      }
    }
    
    /* update the groups table, 
    reselects any of the previously selected job groups which still exist */
    JTable table = pGroupsTablePanel.getTable();
    ListSelectionModel smodel = table.getSelectionModel();

    smodel.removeListSelectionListener(pGroupsListSelector);
    { 
      pGroupsTableModel.setQueueJobGroups(groups, dists);

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
    
    updatePanelHeader();
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
    case Default:
      pGroupsQueueItem.setEnabled(selected && !isLocked()); 
      pGroupsQueueSpecialItem.setEnabled(selected && !isLocked()); 
      pGroupsPauseItem.setEnabled(selected && !isLocked()); 
      pGroupsResumeItem.setEnabled(selected && !isLocked());
      pGroupsPreemptItem.setEnabled(selected && !isLocked());
      pGroupsKillItem.setEnabled(selected && !isLocked());
      pGroupsChangeJobReqsItem.setEnabled(selected && !isLocked());
      pGroupsUpdateJobKeysItem.setEnabled(selected && !isLocked());
      pGroupsDeleteItem.setEnabled(selected && !isLocked());
      pGroupsDeleteCompletedItem.setEnabled(!isLocked());
      break;

    default:
      pGroupsQueueItem.setEnabled(selected); 
      pGroupsQueueSpecialItem.setEnabled(selected); 
      pGroupsPauseItem.setEnabled(selected); 
      pGroupsResumeItem.setEnabled(selected);
      pGroupsPreemptItem.setEnabled(selected);
      pGroupsKillItem.setEnabled(selected);
      pGroupsChangeJobReqsItem.setEnabled(selected);
      pGroupsUpdateJobKeysItem.setEnabled(selected);
      pGroupsDeleteItem.setEnabled(selected);
      pGroupsDeleteCompletedItem.setEnabled(true);
    }

    if (!pDoJobKeysNeedUpdate) 
      pUpdateAllJobKeysItem.setEnabled(false);
    else if (pPrivilegeDetails.isQueueAdmin())
      pUpdateAllJobKeysItem.setEnabled(true);
    else
      pUpdateAllJobKeysItem.setEnabled(false);
  }

  /**
   * Rebuild the view menu.
   */
  @SuppressWarnings("incomplete-switch")
  private void
  updateViewMenu()
  {
    String userSelected = null;
    String groupSelected = null;
    switch (pViewFilter) {
    case UserJobs:
      userSelected = pUserFilter;
      break;
    case GroupJobs:
      groupSelected = pGroupFilter;
      break;
    }
    
    for (JRadioButtonMenuItem item : pViewUserButtons.values())
      pViewButtonGroup.remove(item);
    
    for (JRadioButtonMenuItem item : pViewGroupButtons.values())
      pViewButtonGroup.remove(item);
    
    pViewUserButtons.clear();
    pViewGroupButtons.clear();
    
    pViewUserMenu.removeAll();
    pViewGroupMenu.removeAll();

    {
      ArrayList<TreeSet<String>> userMenus = generateSubmenus(pWorkUsers);
      if(!userMenus.isEmpty()) {
        for(TreeSet<String> ugroup : userMenus) {
          String ufirst = ugroup.first();
          String first3 = ufirst.substring(0, Math.min(3, ufirst.length()));

          String ulast  = ugroup.last(); 
          String last3  = ulast.substring(0, Math.min(3, ulast.length()));

          JMenu gsub = new JMenu(first3.toUpperCase() + "-" + last3.toUpperCase()); 
          pViewUserMenu.add(gsub);
          
          generateSubMenu(ugroup, gsub, userSelected, "view-user-", pViewUserButtons);
        }
      }
      else
        generateSubMenu(pWorkUsers, pViewUserMenu, userSelected, "view-user-", 
                        pViewUserButtons);
    }
    
    {
      ArrayList<TreeSet<String>> groupMenus = generateSubmenus(pWorkGroups);
      if(!groupMenus.isEmpty()) {
        for(TreeSet<String> ggroup : groupMenus) {
          String gfirst = ggroup.first();
          String first3 = gfirst.substring(0, Math.min(3, gfirst.length()));

          String glast  = ggroup.last(); 
          String last3  = glast.substring(0, Math.min(3, glast.length()));

          JMenu gsub = new JMenu(first3.toUpperCase() + "-" + last3.toUpperCase()); 
          pViewGroupMenu.add(gsub);
          
          generateSubMenu(ggroup, gsub, groupSelected, "view-group-", pViewGroupButtons);
        }
      }
      else
        generateSubMenu(pWorkGroups, pViewGroupMenu, groupSelected, "view-group-", 
                        pViewGroupButtons);
    }
  }
  
  /**
   * Update the header of the panel.
   */
  private void
  updatePanelHeader()
  {
    StringBuilder header = new StringBuilder("Queue Jobs: ");
    
    if (pFilterOverride != null)
      header.append("(Focused) ");
    
    if (pFilterOverride == null) {
      switch (pViewFilter) {
      case AllJobs:
        header.append("(View: All Jobs) ");
        break;
      case MyJobs:
        header.append("(View: My Jobs) ");
        break;
      case UserJobs:
        if (pUserFilter != null)
          header.append("(View: " + pUserFilter + "'s Jobs) ");
        break;
      case GroupJobs:
        if (pGroupFilter != null)
          header.append("(View: [[" + pGroupFilter + "]]'s Jobs) ");
        break;
      case Default:
        break;
      case CustomJobs:
        header.append("(View: Custom) ");
        break;
      }
    }
    
    switch(pStatusFilter) {
    case AllCompleted:
      header.append("(Filter: All Completed) ");
      break;
    case AllTerminated:
      header.append("(Filter: All Terminated) ");
      break;
    case AllWaiting:
      header.append("(Filter: All Waiting) ");
      break;
    case AnyFailed:
      header.append("(Filter: Any Failed) ");
      break;
    case AnyLimbo:
      header.append("(Filter: Any Limbo) ");
      break;
    case AnyPaused:
      header.append("(Filter: Any Paused) ");
      break;
    case AnyRunning:
      header.append("(Filter: Any Running) ");
      break;
    case AllGroups:
      break;
    }
    
    pHeaderLabel.setText(header.toString());
  }

  /**
   * Generate a submenu.
   * 
   * @param currentSelection
   *   The current selection or <code>null</code> if there is not a current selection among 
   *   the entries
   * 
   * @param entries
   *   The entries to put in the submenu
   * 
   * @param menu
   *   The menu the entries are being added to.
   *   
   * @param actionPrefix
   *   The prefix for the action command for the menu items.
   *   
   * @param buttonList
   *   The list of buttons to stash the new entries in.
   */
  private void generateSubMenu
  (
    Set<String> entries,
    JMenu menu,
    String currentSelection,
    String actionPrefix,
    TreeMap<String,JRadioButtonMenuItem> buttonList
  )
  {
    for(String entry : entries)  {
      JRadioButtonMenuItem item = new JRadioButtonMenuItem(entry);
      if (currentSelection != null && entry.equals(currentSelection))
        item.setSelected(true);
      item.setActionCommand(actionPrefix + entry);
      item.addActionListener(this);
      menu.add(item);
      pViewButtonGroup.add(item);
      buttonList.put(entry, item);
    }
  }
  
  /**
   * Group a set of values into reasonable submenus.
   * 
   * @param values
   *   The set of values to be grouped.
   * 
   * @return
   *   The values in the set grouped by submenu.
   */ 
  @SuppressWarnings("null")
  private ArrayList<TreeSet<String>> 
  generateSubmenus
  (
    Set<String> values  
  )
  {
    ArrayList<TreeSet<String>> toReturn = new ArrayList<TreeSet<String>>();

    if(values != null) {
      int numEntries = values.size();
      int maxPerMenu = 12;
      if(numEntries > maxPerMenu) {
        int numMenus = Math.max(numEntries / maxPerMenu, 2);
        int perMenu  = numEntries / numMenus;
        int extra    = numEntries % perMenu;
        
        int cnt = 0;
        int max = 0;
        TreeSet<String> egroup = null;
        for(String entry : values) {
          if(cnt == 0) {
            egroup = new TreeSet<String>();
            toReturn.add(egroup);
            
            max = perMenu - 1;
            if(extra > 0) 
              max++;
            extra--;
          }
          
          egroup.add(entry);
          cnt++;
          
          if(cnt > max) 
            cnt = 0;
        }
      }
    }

    return toReturn;
  }

  /*----------------------------------------------------------------------------------------*/
  /*----------------------------------------------------------------------------------------*/
  
  

  /**
   * Refocus keyboard events on this panel if it contains the mouse.
   * 
   * @return
   *   Whether the panel has received the focus.
   */ 
  @Override
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
  @Override
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
      (pGroupShowNodeItem, prefs.getShowNode(), 
       "Show the node which created the primary selected job in the Node Browser.");
    updateMenuToolTip
      (pViewButtons.get(NewViewFilter.MyJobs), prefs.getJobBrowserShowMyViews(), 
       "Show only job groups owned by the user running pipeline.");
    updateMenuToolTip
      (pViewButtons.get(NewViewFilter.AllJobs), prefs.getJobBrowserShowAllViews(), 
       "Show job groups from all views.");
    updateMenuToolTip
      (pViewButtons.get(NewViewFilter.CustomJobs), prefs.getJobBrowserShowCustomView(), 
       "Allow the selection of a group of custom users whose jobs will be viewed.");
    updateMenuToolTip
      (pViewButtons.get(NewViewFilter.Default), prefs.getJobBrowserShowCurrentView(), 
       "Show only job groups owned by the current working area.");
    updateMenuToolTip
      (pClearFocusItem, prefs.getJobBrowserClearFocus(),
       "Clear the current focus on a specific set of job groups.");
    updateMenuToolTip
      (pGroupsUpdateItem, prefs.getUpdate(),
       "Update the status of all jobs and job groups.");
    updateMenuToolTip
      (pMonitorGroupsItem, prefs.getJobBrowserMonitorGroups(),
       "Add the selected job groups to the job monitor panel.");
    updateMenuToolTip
      (pMonitorGroupsItem, prefs.getJobBrowserClearFocus(),
       "Clear the current focus on a specific set of job groups.");
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
      (pGroupsChangeJobReqsItem, prefs.getChangeJobReqs(), 
       "Changes the job requirements for all jobs associated with the selected groups.");
    updateMenuToolTip
      (pGroupsUpdateJobKeysItem, prefs.getUpdateJobKeys(), 
      "Reruns the key choosers for all jobs associated with the selected groups.");
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
	  if(UIFactory.getBeepPreference())
	    Toolkit.getDefaultToolkit().beep();
	}
      }
      break;
    case MouseEvent.BUTTON1: 
      {
        int on2  = (MouseEvent.BUTTON1_DOWN_MASK);
        
        int off2 = (MouseEvent.BUTTON2_DOWN_MASK | 
                    MouseEvent.BUTTON3_DOWN_MASK | 
                    MouseEvent.SHIFT_DOWN_MASK | 
                    MouseEvent.ALT_DOWN_MASK |
                    MouseEvent.CTRL_DOWN_MASK);
        if ((mods & (on2 | off2)) == on2) {
          if (e.getComponent() == pStatusMenuLabel) {
            
            pStatusMenuLabel.setIcon(sFilterAnchorPressedIcon);
            
            pStatusPopup.show(e.getComponent(), e.getX(), e.getY());
          }
          else if (e.getComponent() == pViewMenuLabel) {
            
            pViewMenuLabel.setIcon(sViewAnchorPressedIcon);
            
            pViewPopup.show(e.getComponent(), e.getX(), e.getY());
          }
        }
      }
    }
  }

  /**
   * Invoked when a mouse button has been released on a component. 
   */ 
  public void 
  mouseReleased(MouseEvent e) {}

  
  
  /*-- POPUP MENU LISTENER METHODS -------------------------------------------------------*/

  public void         
  popupMenuCanceled(PopupMenuEvent e) {} 

  public void         
  popupMenuWillBecomeInvisible
  (
   PopupMenuEvent e
  )
  {
    Object source = e.getSource();
    if (source == pStatusPopup)
      pStatusMenuLabel.setIcon(sFilterAnchorIcon);
    else if (source == pViewPopup)
      pViewMenuLabel.setIcon(sViewAnchorIcon);
  }

  public void
  popupMenuWillBecomeVisible(PopupMenuEvent e) {} 



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
      
      if ((prefs.getJobBrowserShowMyViews() != null) &&
          prefs.getJobBrowserShowMyViews().wasPressed(e))
        doViewMyJobs();
      else if ((prefs.getJobBrowserShowAllViews() != null) &&
               prefs.getJobBrowserShowAllViews().wasPressed(e))
        doViewAllJobs();
      else if ((prefs.getJobBrowserShowCurrentView() != null) &&
               prefs.getJobBrowserShowCurrentView().wasPressed(e))
        doViewWorkingAreaJobs();
      else if ((prefs.getJobBrowserShowCustomView() != null) &&
               prefs.getJobBrowserShowCustomView().wasPressed(e))
        doViewCustomJobs();
      
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
      else if((prefs.getChangeJobReqs() != null) &&
               prefs.getChangeJobReqs().wasPressed(e))
        doGroupsChangeJobsReqs();
      else if((prefs.getUpdateJobKeys() != null) &&
               prefs.getUpdateJobKeys().wasPressed(e))
        doGroupsUpdateJobKeys();
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
	if(UIFactory.getBeepPreference())
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

    else if(cmd.equals("show-node"))
      doShowNode(null);
    else if (cmd.startsWith("show-node-"))
      doShowNode(Character.digit(cmd.charAt(10), 10));
    
    else if (cmd.equals("view-my-jobs"))
      doViewMyJobs();
    else if (cmd.equals("view-all-jobs"))
      doViewAllJobs();
    else if (cmd.equals("view-working-area-jobs"))
      doViewWorkingAreaJobs();
    else if (cmd.equals("view-custom-jobs"))
      doViewCustomJobs();
    
    else if (cmd.startsWith("view-user-"))
      doViewUserJobs(cmd.substring(10));
    else if (cmd.startsWith("view-group-"))
      doViewGroupJobs(cmd.substring(11));
    
    else if(cmd.equals("filter-all-jobs"))
      doFilterAllJobs();
    else if(cmd.equals("filter-any-failed"))
      doFilterAnyFailed();
    else if(cmd.equals("filter-any-limbo"))
      doFilterAnyLimbo();
    else if(cmd.equals("filter-any-running"))
      doFilterAnyRunning();
    else if(cmd.equals("filter-any-paused"))
      doFilterAnyPaused();
    else if(cmd.equals("filter-all-completed"))
      doFilterAllCompleted();
    else if(cmd.equals("filter-all-waiting"))
      doFilterAllWaiting();
    else if(cmd.equals("filter-all-terminated"))
      doFilterAllTerminated();
    
    else if(cmd.equals("clear-focus"))
      doClearFocus();
    else if(cmd.equals("monitor-groups"))
      doMonitorGroups();

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
    else if(cmd.equals("groups-change-job-reqs"))
      doGroupsChangeJobsReqs();
    else if(cmd.equals("groups-update-job-keys"))
      doGroupsUpdateJobKeys();

    else if(cmd.equals("delete-group")) 
      doGroupsDelete();
    else if(cmd.equals("delete-completed")) 
      doGroupsDeleteCompleted();
    
    else if (cmd.equals("update-all-job-keys"))
      doUpdateAllJobKeys();
  }




  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Show job groups owned by the user running pipeline.
   */
  public void 
  doViewMyJobs()
  {
    pViewFilter = NewViewFilter.MyJobs;
    updatePanels();
  }
  
  /**
   * Show all job groups.
   */
  public void 
  doViewAllJobs()
  {
    pViewFilter = NewViewFilter.AllJobs;
    updatePanels();
  }
  
  /**
   * Show job groups owned by the specified user.
   * 
   * @param user
   *   The name of the user.
   */
  public void
  doViewUserJobs
  (
    String user  
  )
  {
    pViewFilter = NewViewFilter.UserJobs;
    pUserFilter = user;
    
    updatePanels();
  }
  
  /**
   * Show job groups owned by users in the specified group.
   * 
   * @param group
   *   The name of Pipeline workgroup.
   */
  public void
  doViewGroupJobs
  (
    String group
  )
  {
    pViewFilter = NewViewFilter.GroupJobs;
    pGroupFilter = group;
    
    updatePanels();
  }
  
  /**
   * Show jobs groups from the current working area.
   */
  public void
  doViewWorkingAreaJobs()
  {
    pViewFilter = NewViewFilter.Default;
    
    updatePanels();
  }
  
  /**
   * Display a dialog allowing the selection of a custom set of users whose jobs will be
   * displayed.
   */
  private void
  doViewCustomJobs()
  {
    if (pCustomUserFilterDialog == null)
      pCustomUserFilterDialog = 
        new JBooleanListDialog(this.getTopFrame(), "Custom User Filter", 
                               "Change the list of Custom Users:");

    
    pCustomUserFilterDialog.setFields(pWorkUsers);
    pCustomUserFilterDialog.setSelected(pCustomFilter);
    
    pCustomUserFilterDialog.setVisible(true);
    if (pCustomUserFilterDialog.wasConfirmed()) {
      pCustomFilter = pCustomUserFilterDialog.getSelected();
    }
    
    pViewFilter = NewViewFilter.CustomJobs;
    
    updatePanels();
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  public void
  doFilterAllJobs() 
  {
    pStatusFilter = StatusFilter.AllGroups;
    
    filterJobs();
  }
  
  public void
  doFilterAnyLimbo() 
  {
    pStatusFilter = StatusFilter.AnyLimbo;
    
    filterJobs();
  }
  
  public void
  doFilterAnyRunning() 
  {
    pStatusFilter = StatusFilter.AnyRunning;
    
    filterJobs();
  }
  
  public void
  doFilterAnyFailed() 
  {
    pStatusFilter = StatusFilter.AnyFailed;
    
    filterJobs();
  }
  
  public void
  doFilterAnyPaused() 
  {
    pStatusFilter = StatusFilter.AnyPaused;
    
    filterJobs();
  }
  
  public void
  doFilterAllCompleted() 
  {
    pStatusFilter = StatusFilter.AllCompleted;
    
    filterJobs();
  }
  
  public void
  doFilterAllWaiting() 
  {
    pStatusFilter = StatusFilter.AllWaiting;
    
    filterJobs();
  }
  
  public void
  doFilterAllTerminated() 
  {
    pStatusFilter = StatusFilter.AllTerminated;
    
    filterJobs();
  }
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Clear the filter override which causes the view filters to be ignored.
   */
  public void
  doClearFocus()
  {
    setFilterOverride(null);
    updatePanels(true);
  }
  
  /**
   * Add the currently selected job groups to the job monitor panel.
   */
  public void
  doMonitorGroups()
  {
    LinkedList<QueueJobGroup> groups = new LinkedList<QueueJobGroup>();
    for(Long groupID : getSelectedGroupIDs()) {
      QueueJobGroup group = pJobGroups.get(groupID);
      groups.add(group);
    }
    UIMaster.getInstance().monitorJobGroups(groups);
  }
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Resubmit all aborted and failed jobs associated with the selected job groups.
   */ 
  public void 
  doGroupsQueueJobs()
  {
    TreeMap<Long,QueueJobGroup> groups = new TreeMap<Long,QueueJobGroup>();
    for(Long groupID : getSelectedGroupIDs()) {
      QueueJobGroup group = pJobGroups.get(groupID);
      if(group != null) {
        String author = group.getNodeID().getAuthor();
        if((author != null) && 
           (author.equals(PackageInfo.sUser) || pPrivilegeDetails.isQueueManaged(author)))
          groups.put(groupID, group); 
      }
    }

    if(!groups.isEmpty()) {
      QueueJobsTask task = new QueueJobsTask(groups);
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
    TreeMap<Long,QueueJobGroup> groups = new TreeMap<Long,QueueJobGroup>();
    for(Long groupID : getSelectedGroupIDs()) {
      QueueJobGroup group = pJobGroups.get(groupID);
      if(group != null) {
        String author = group.getNodeID().getAuthor();
        if((author != null) && 
           (author.equals(PackageInfo.sUser) || pPrivilegeDetails.isQueueManaged(author)))
          groups.put(groupID, group); 
      }
    }
    
    if(!groups.isEmpty()) {
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
      
 	Float maxLoad = null;
 	if(diag.overrideMaxLoad())
 	  maxLoad = diag.getMaxLoad();
      
 	Long minMemory = null;
 	if(diag.overrideMinMemory())
 	  minMemory = diag.getMinMemory();
      
 	Long minDisk= null;
 	if(diag.overrideMinDisk())
 	  minDisk = diag.getMinDisk();
        
 	TreeSet<String> selectionKeys = null;
 	if(diag.overrideSelectionKeys()) 
 	  selectionKeys = diag.getSelectionKeys();

 	TreeSet<String> licenseKeys = null;
 	if(diag.overrideLicenseKeys()) 
 	  licenseKeys = diag.getLicenseKeys();
      
 	TreeSet<String> hardwareKeys = null;
 	if(diag.overrideHardwareKeys()) 
 	  hardwareKeys = diag.getHardwareKeys();
      
 	QueueJobsTask task = 
 	  new QueueJobsTask(groups, batchSize, priority, interval,
 	                    maxLoad, minMemory, minDisk,
 			    selectionKeys, licenseKeys, hardwareKeys);
 	task.start();
       }
     }
  }

  /**
   * Get the job IDs of all jobs which are members of the selected groups and for which
   * the current user has sufficient privileges to change their state.
   */
  private TreeSet<Long>
  getSelectedModifiableJobIDs() 
  { 
    TreeSet<Long> jobs = new TreeSet<Long>();
    for(Long groupID : getSelectedGroupIDs()) {
      QueueJobGroup group = pJobGroups.get(groupID);
      if(group != null) {
        String author = group.getNodeID().getAuthor();
        if((author != null) && 
           (author.equals(PackageInfo.sUser) || pPrivilegeDetails.isQueueManaged(author)))
          jobs.addAll(group.getJobIDs());
      }
    }
    
    return jobs;
  }

  /**
   * Pause all waiting jobs associated with the selected job groups.
   */ 
  public void 
  doGroupsPauseJobs()
  {
    TreeSet<Long> jobs = getSelectedModifiableJobIDs(); 
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
    TreeSet<Long> jobs = getSelectedModifiableJobIDs(); 
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
    TreeSet<Long> jobs = getSelectedModifiableJobIDs(); 
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
    TreeSet<Long> jobs = getSelectedModifiableJobIDs(); 
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
    String msg = null;
    switch(pViewFilter) {
    case Default:
      if((pAuthor != null) && !pAuthor.equals(PackageInfo.sUser) && 
         pPrivilegeDetails.isQueueManaged(pAuthor))
        msg = ("This will delete ALL complete Job Groups owned by (" + pAuthor + ") in " + 
               "the current working area!"); 
      break;

    case MyJobs:
      msg = "This will delete ALL completed Job Groups owned by you!"; 
      break;
      
    case UserJobs:
      if((pUserFilter != null) && !pUserFilter.equals(PackageInfo.sUser) && 
        pPrivilegeDetails.isQueueManaged(pUserFilter))
      msg = "This will delete ALL completed Job Groups owned by (" + pUserFilter + ")!"; 
      break;
    
    case GroupJobs:
      if((pGroupFilter != null) && pPrivilegeDetails.isQueueManager())
      msg = "This will delete ALL completed Job Groups owned by users in the group " +
      	    "(" + pGroupFilter + ")!"; 
      break;
      
    case CustomJobs:
      if((pCustomFilter != null) && pPrivilegeDetails.isQueueManager())
      msg = "This will delete ALL completed Job Groups owned by the following users " +
             pCustomFilter + " !"; 
      break;
      
      
    case AllJobs:
      msg = "This will delete ALL completed Job Groups for ALL users!"; 
    }

    if(msg != null) {
      JConfirmDialog diag = new JConfirmDialog(getTopFrame(), "Are you sure?", msg); 
      diag.setVisible(true);
      if(!diag.wasConfirmed()) 
        return;
    }

    DeleteCompletedJobGroupsTask task = new DeleteCompletedJobGroupsTask(pViewFilter); 
    task.start();
  }
  
  /** 
   * Change the job requirements for the given job groups.
   */ 
  public void
  doGroupsChangeJobsReqs()
  {
    JChangeJobReqsDialog diag = UIMaster.getInstance().showChangeJobReqDialog();
    if(diag.wasConfirmed()) {
      Integer priority = null;
      if(diag.overridePriority())
	priority = diag.getPriority();

      Integer rampUp = null;
      if(diag.overrideRampUp())
	rampUp = diag.getRampUp();

      Float maxLoad = null;
      if(diag.overrideMaxLoad())
	maxLoad = diag.getMaxLoad();

      Long minMemory = null;
      if(diag.overrideMinMemory())
	minMemory = diag.getMinMemory();

      Long minDisk = null;
      if(diag.overrideMinDisk())
	minDisk = diag.getMinDisk();

      Set<String> licenseKeys = null;
      if(diag.overrideLicenseKeys())
	licenseKeys = diag.getLicenseKeys();

      Set<String> selectionKeys = null;
      if(diag.overrideSelectionKeys())
	selectionKeys = diag.getSelectionKeys();

      Set<String> hardwareKeys = null;
      if(diag.overrideHardwareKeys())
	hardwareKeys = diag.getHardwareKeys();

      LinkedList<JobReqsDelta> change = new LinkedList<JobReqsDelta>();
      for(Long groupID : getSelectedGroupIDs()) {
	QueueJobGroup group = pJobGroups.get(groupID);
	if(group != null) {
	  for(Long jobID : group.getJobIDs()) {
            String author = group.getNodeID().getAuthor();
	    if((author != null) &&
	      (author.equals(PackageInfo.sUser) || 
	       pPrivilegeDetails.isQueueManaged(author))) {
	      JobReqsDelta newReq = 
                new JobReqsDelta(jobID, priority, rampUp, maxLoad, minMemory, minDisk, 
                                 licenseKeys, selectionKeys, hardwareKeys);
	      change.add(newReq);
	    }
	  }
	}
      }

      if(!change.isEmpty()) {
	ChangeJobReqsTask task = new ChangeJobReqsTask(change);
	task.start();
      }
    }
  }

  /**
   * Delete all completed job groups.
   */ 
  public void
  doGroupsUpdateJobKeys()
  {
    TreeSet<Long> changed = new TreeSet<Long>();
    for(Long groupID : getSelectedGroupIDs()) {
      QueueJobGroup group = pJobGroups.get(groupID);
      if(group != null) {
        for(Long jobID : group.getJobIDs()) {
          String author = group.getNodeID().getAuthor();
          if((author != null) &&
            (author.equals(PackageInfo.sUser) || pPrivilegeDetails.isQueueManaged(author))) {
            changed.add(jobID);
          }
        }
      }
    }
    if(!changed.isEmpty()) {
      UpdateJobKeysTask task = new UpdateJobKeysTask(changed);
      task.start();
    }
  }
  
  /**
   * Update the key choosers on all the jobs in the queue.
   */
  private void
  doUpdateAllJobKeys()
  {
    if (pPrivilegeDetails.isQueueAdmin()) {
      UpdateAllJobKeysTask task = new UpdateAllJobKeysTask();
      task.start();
    }
  }
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Show the node which created the primary selected job/group in the Node Viewer. 
   */ 
  private synchronized void 
  doShowNode
  (
    Integer channel  
  ) 
  {
    TreeSet<NodeID> nodeIDs = new TreeSet<NodeID>();
    
    if (pSelectedIDs != null) {
      for (Long groupID : pSelectedIDs)
        nodeIDs.add(pJobGroups.get(groupID).getNodeID());
    }
    
    int chan;
    if (channel == null)
      chan = Integer.parseInt(UserPrefs.getInstance().getDefaultNodeChannel());
    else
      chan = channel;
    UIMaster.getInstance().selectAndShowNodes(chan, nodeIDs);    
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  @Override
  public synchronized void 
  toGlue
  ( 
   GlueEncoder encoder   
  ) 
    throws GlueException
  {
    super.toGlue(encoder);
    
    encoder.encode("StatusFilter", pStatusFilter);
    encoder.encode("NewViewFilter", pViewFilter);
    encoder.encode("UserFilter", pUserFilter);
    encoder.encode("GroupFilter", pGroupFilter);
    encoder.encode("CustomFilter", pCustomFilter);
    encoder.encode("FilterOverride", pFilterOverride);
    

    if(!pSelectedIDs.isEmpty())
      encoder.encode("SelectedIDs", pSelectedIDs);
  }

  @SuppressWarnings("unchecked")
  @Override
  public synchronized void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    {
      NewViewFilter filter = (NewViewFilter) decoder.decode("NewViewFilter");
      if(filter != null)
        pViewFilter = filter;
    }

    {
      StatusFilter filter = (StatusFilter) decoder.decode("StatusFilter");
      if (filter != null)
        pStatusFilter = filter;
    }
    
    pStatusButtons.get(pStatusFilter).setSelected(true);
    
    pUserFilter = (String) decoder.decode("UserFilter");
    pGroupFilter = (String) decoder.decode("GroupFilter");
    
    pCustomFilter = (TreeSet<String>) decoder.decode("CustomFilter");
    pFilterOverride = (TreeSet<Long>) decoder.decode("FilterOverride");
    
    switch (pViewFilter) {
    case UserJobs:
      if (pUserFilter != null) {
        JRadioButtonMenuItem item = pViewUserButtons.get(pUserFilter);
        if (item != null)
          item.setSelected(true);
      }
      break;
    case GroupJobs:
      if (pGroupFilter != null) {
        JRadioButtonMenuItem item = pViewGroupButtons.get(pGroupFilter);
        if (item != null)
          item.setSelected(true);
      }
      break;
    case AllJobs:
    case Default:
    case MyJobs:
    case CustomJobs:
      pViewButtons.get(pViewFilter).setSelected(true);
    }
    
    updatePanelHeader();

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
  public static
  enum NewViewFilter
  {  
    /**
     * Show only job groups owned by the current view.
     */
    Default, 

    /**
     * Show job groups from any view owned by the current user.
     */ 
    MyJobs, 

    /**
     * Show job groups from all views.
     */ 
    AllJobs,
    
    /**
     * Show only the job groups associated with a specific user.
     */
    UserJobs,
    
    /**
     * Show only the job groups associated with a specific group of users. 
     */
    GroupJobs,
    
    /**
     * Show the job groups associated with a custom list of users. 
     */
    CustomJobs; 
  }
  
  /**
   * The working area view filter.
   * 
   * @deprecated
   *   This was replaced with the {@link NewViewFilter} but left in the code to prevent GLUE
   *   read errors.  This should never be used in the code.
   */
  @Deprecated
  private static
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

  /**
   * The job group status filter
   */
  public static
  enum StatusFilter 
  {
    /**
     * Show all job.
     */
    AllGroups,
    
    /**
     * Groups with at least one failed job.
     */
    AnyFailed,
    
    /**
     * Groups with at least one limbo job.
     */
    AnyLimbo,
    
    /**
     * Groups with at least one paused job.
     */
    AnyPaused,
    
    /**
     * Groups with at least one running job.
     */
    AnyRunning,
    
    /**
     * Groups where all the jobs are either Finished, Failed, or Aborted.
     */
    AllCompleted,
    
    /**
     * Groups where all the jobs are either Paused, Preempted, or Queued.
     */
    AllWaiting,
    
    /**
     * Groups where all jobs are either Failed or Aborted
     */
    AllTerminated
  }
  


  /*----------------------------------------------------------------------------------------*/

  /**
   * Makes the given component have the keyboard focus when the mouse is over it.
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
    @Override
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
    @Override
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

      JTable table = getTable();
      ListSelectionModel smodel = table.getSelectionModel();

      smodel.addListSelectionListener(pSelector);
      table.addMouseListener(pSelector); 
      table.addKeyListener(pSelector);
    }

    @Override
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
    extends MouseAdapter
    implements ListSelectionListener, KeyListener
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
      
      /* update selected IDs */ 
      {
	pSelectedIDs.clear(); 
	int rows[] = pGroupsTablePanel.getTable().getSelectedRows();
	int wk;
	for(wk=0; wk<rows.length; wk++) {
	  Long groupID = pGroupsTableModel.getGroupID(rows[wk]);
	  if(groupID != null) 
	    pSelectedIDs.add(groupID);
	}
      }

      pJustSelected = null;    
      if(pSelectedIDs.size() == 1) {
	pJustSelected = pSelectedIDs.first();
        updatePanels(true);
      }
      else {
        pSelectionModified = true;
      }
    }

    /**
     * invoked when a key has been pressed.
     */   
    public void 
    keyPressed
    (
     KeyEvent e
    ) 
    {
      switch(e.getKeyCode()) {
      case KeyEvent.VK_SHIFT:
      case KeyEvent.VK_CONTROL:
        pModifierPressed = true;
      }
    } 

    /**
     * Invoked when a key has been released.
     */ 
    public void 	
    keyReleased
    (
     KeyEvent e
    ) 
    {
      int mods = e.getModifiersEx();
      if(pSelectionModified) {
        switch(e.getKeyCode()) {
        case KeyEvent.VK_SHIFT:
        case KeyEvent.VK_CONTROL:
          {
            int on1  = 0;
            
            int off1 = (MouseEvent.SHIFT_DOWN_MASK |
                        MouseEvent.CTRL_DOWN_MASK);
            
            if((mods & (on1 | off1)) == on1) 
              updatePanels(true);
          }      
        }
      }
    }
    
    /**
     * Invoked when a key has been typed.
     */ 
    public void 	
    keyTyped(KeyEvent e) {} 

    /**
     * Invoked when a mouse button has been released on a component. 
     */ 
    @Override
    public void 
    mouseReleased
    (
     MouseEvent e
    ) 
    {    
      switch(e.getButton()) {
      case MouseEvent.BUTTON1:
        if(pSelectionModified && !pModifierPressed) 
          updatePanels(true);
      }
    }

    /**
     * Invoked when the mouse exits a component. 
     */ 
    @Override
    public void 
    mouseExited
    (
     MouseEvent e
    ) 
    {
      if(pSelectionModified) 
        updatePanels(true);
    }

    private Long pJustSelected; 
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
     TreeMap<Long,QueueJobGroup> groups
    ) 
    {
      this(groups, null, null, null, null, null, null, null, null, null);
    }
    
    public 
    QueueJobsTask
    (
     TreeMap<Long,QueueJobGroup> groups, 
     Integer batchSize, 
     Integer priority, 
     Integer rampUp, 
     Float maxLoad,              
     Long minMemory,              
     Long minDisk,  
     TreeSet<String> selectionKeys,
     TreeSet<String> licenseKeys,
     TreeSet<String> hardwareKeys
    ) 
    {
      super("JQueueJobsBrowserPanel:QueueJobsTask");

      pTargetGroups  = groups;
      pBatchSize     = batchSize;
      pPriority      = priority; 
      pRampUp        = rampUp; 
      pMaxLoad       = maxLoad;
      pMinMemory     = minMemory;
      pMinDisk       = minDisk;
      pSelectionKeys = selectionKeys;
      pSelectionKeys = licenseKeys;
      pHardwareKeys  = hardwareKeys;
    }

    @SuppressWarnings("incomplete-switch")
    @Override
    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID)) {
        MasterMgrClient mclient = master.acquireMasterMgrClient();
        QueueMgrClient  qclient = master.acquireQueueMgrClient();
        LinkedList<QueueJobGroup> allGroups = new LinkedList<QueueJobGroup>();
	try {
          master.updatePanelOp(pGroupID, "Updating Job Status..."); 
          TreeMap<Long,JobStatus> jstatus = 
            qclient.getJobStatus(new TreeSet<Long>(pTargetGroups.keySet()));

          for(Long groupID : pTargetGroups.keySet()) {
            QueueJobGroup group = pTargetGroups.get(groupID);
            NodeID targetID = group.getNodeID();
            TreeSet<FileSeq> targetSeqs = new TreeSet<FileSeq>();
            for(Long jobID : group.getRootIDs()) {
              JobStatus status = jstatus.get(jobID);
              if(status != null) {
                switch(status.getState()) {
                case Aborted:
                case Failed:
                  targetSeqs.add(status.getTargetSequence());
                }
              }
            }

            if(!targetSeqs.isEmpty()) {
              master.updatePanelOp(pGroupID, 
                                   "Resubmitting Jobs to the Queue: " + targetID.getName());
              LinkedList<QueueJobGroup> groups = 
                mclient.resubmitJobs
                  (targetID, targetSeqs, pBatchSize, pPriority, pRampUp,
                   pMaxLoad, pMinMemory, pMinDisk,
                   pSelectionKeys, pLicenseKeys, pHardwareKeys);
              allGroups.addAll(groups);
            }
          }
        }
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.releaseMasterMgrClient(mclient);
	  master.releaseQueueMgrClient(qclient);
	  master.endPanelOp(pGroupID, "Done.");
	  if (!allGroups.isEmpty())
	    master.monitorJobGroups(allGroups);
	}

	updatePanels();
      }
    }

    private TreeMap<Long,QueueJobGroup>       pTargetGroups; 
    private Integer                           pBatchSize;
    private Integer                           pPriority;
    private Integer                           pRampUp; 
    private Float                             pMaxLoad;        
    private Long                              pMinMemory;              
    private Long                              pMinDisk;
    private TreeSet<String>                   pSelectionKeys;
    private TreeSet<String>                   pLicenseKeys;
    private TreeSet<String>                   pHardwareKeys;
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
     TreeSet<Long> jobIDs
    ) 
    {
      super("JQueueJobsBrowserPanel:PauseJobsTask");

      pJobIDs = jobIDs;
    }

    @Override
    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Pausing Jobs...")) {
        QueueMgrClient client = master.acquireQueueMgrClient();
	try {
	  client.pauseJobs(pJobIDs);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.releaseQueueMgrClient(client);
	  master.endPanelOp(pGroupID, "Done.");
	}

	updatePanels();
      }
    }

    private TreeSet<Long>  pJobIDs;
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
     TreeSet<Long> jobIDs
    ) 
    {
      super("JQueueJobsBrowserPanel:ResumeJobsTask");

      pJobIDs = jobIDs; 
    }

    @Override
    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Resuming Paused Jobs...")) {
        QueueMgrClient client = master.acquireQueueMgrClient();
	try {
	  client.resumeJobs(pJobIDs);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.releaseQueueMgrClient(client);
	  master.endPanelOp(pGroupID, "Done.");
	}

	updatePanels();
      }
    }

    private TreeSet<Long>  pJobIDs;
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
     TreeSet<Long> jobIDs
    ) 
    {
      super("JQueueJobsBrowserPanel:PreemptJobsTask");

      pJobIDs = jobIDs; 
    }

    @Override
    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Preempting Jobs...")) {
        QueueMgrClient client = master.acquireQueueMgrClient();
	try {
	  client.preemptJobs(pJobIDs);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.releaseQueueMgrClient(client);
	  master.endPanelOp(pGroupID, "Done.");
	}

	updatePanels();
      }
    }

    private TreeSet<Long>  pJobIDs;
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
     TreeSet<Long> jobIDs
    ) 
    {
      super("JQueueJobsBrowserPanel:KillJobsTask");

      pJobIDs = jobIDs; 
    }

    @Override
    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Killing Jobs...")) {
        QueueMgrClient client = master.acquireQueueMgrClient();
	try {
	  client.killJobs(pJobIDs);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.releaseQueueMgrClient(client);
	  master.endPanelOp(pGroupID, "Done.");
	}

	updatePanels();
      }
    }

    private TreeSet<Long>  pJobIDs;
  }
  
  /** 
   * Change the job requirements for the given jobs.
   */ 
  private
  class ChangeJobReqsTask
    extends Thread
  {
    public 
    ChangeJobReqsTask
    (   
     LinkedList<JobReqsDelta> jobReqChanges
    ) 
    {
      super("JQueueJobsViewerPanel:ChangeJobReqsTask");

      pJobReqChanges = jobReqChanges;
    }

    @Override
    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Changing Job Reqs...")) {
        QueueMgrClient client = master.acquireQueueMgrClient();
	try {
	  client.changeJobReqs(pJobReqChanges);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.releaseQueueMgrClient(client);
	  master.endPanelOp(pGroupID, "Done.");
	}

	updatePanels();
      }
    }

    private LinkedList<JobReqsDelta> pJobReqChanges;
  }
  
  /** 
   * Rerun the key choosers for the given jobs.
   */ 
  private
  class UpdateJobKeysTask
    extends Thread
  {
    public 
    UpdateJobKeysTask
    (   
      TreeSet<Long> selectedJobs
    ) 
    {
      super("JQueueJobsViewerPanel:UpdateJobKeysTask");

      pSelectedJobs = selectedJobs;
    }

    @Override
    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Updating Job Keys...")) {
        QueueMgrClient client = master.acquireQueueMgrClient();
        try {
          client.updateJobKeys(pSelectedJobs);
        }
        catch(PipelineException ex) {
          master.showErrorDialog(ex);
          return;
        }
        finally {
          master.releaseQueueMgrClient(client);
          master.endPanelOp(pGroupID, "Done.");
        }

        updatePanels();
      }
    }

    private TreeSet<Long> pSelectedJobs;
  }

  /** 
   * Rerun the key choosers for the all jobs in the queue..
   */ 
  private class
  UpdateAllJobKeysTask
    extends Thread
  {
    @Override
    public void 
    run()
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Updating All Job Keys...")) {
        QueueMgrClient client = master.acquireQueueMgrClient();
        try {
          client.updateAllJobKeys();
        }
        catch(PipelineException ex) {
          master.showErrorDialog(ex);
          return;
        }
        finally {
          master.releaseQueueMgrClient(client);
          master.endPanelOp(pGroupID, "Done.");
        }

        updatePanels();
      }
    }
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

    @Override
    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Deleting Job Groups...")) {
        QueueMgrClient client = master.acquireQueueMgrClient();
	try {
	  client.deleteJobGroups(pGroups);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.releaseQueueMgrClient(client);
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
     NewViewFilter filter
    ) 
    {
      super("JQueueJobsBrowserPanel:DeleteCompletedJobGroupsTask");

      pFilter = filter;
    }

    @Override
    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Deleting Job Groups...")) {
        MasterMgrClient mclient = master.acquireMasterMgrClient();
        QueueMgrClient qclient  = master.acquireQueueMgrClient();
	try {
	  switch(pFilter) {
	  case Default:
	    qclient.deleteViewJobGroups(pAuthor, pView);
	    break;
	    
	  case MyJobs:
  	    {
  	      TreeSet<String> users = new TreeSet<String>();
  	      users.add(PackageInfo.sUser);
              qclient.deleteUsersJobGroups(users);
            }
            break;
          
	  case UserJobs:
	    {
	      if (pUserFilter != null) {
	        TreeSet<String> users = new TreeSet<String>();
	        users.add(pUserFilter);
	        qclient.deleteUsersJobGroups(users);
	      }
	    }
	    break;
	    
	  case GroupJobs:
	    {
	      if (pGroupFilter != null) {
	        TreeSet<String> users = mclient.getWorkGroups().getUsersInGroup(pGroupFilter);
	        if (users != null)
	          qclient.deleteUsersJobGroups(users);
	      }
	    }
	    break;
	    
	  case CustomJobs:
	    {
	      if (pCustomFilter != null) {
	        qclient.deleteUsersJobGroups(pCustomFilter);
	      }
	    }
	    break;
	    
	  case AllJobs:
	    qclient.deleteAllJobGroups();
	    break;
	  }
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.releaseMasterMgrClient(mclient);
	  master.releaseQueueMgrClient(qclient);
	  master.endPanelOp(pGroupID, "Done.");
	}

	updatePanels();
      }
    }

    private NewViewFilter  pFilter; 
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -507775432755443313L;

  private static final Icon sFilterAnchorIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("JobBrowserFilterButton.png"));

  private static final Icon sFilterAnchorPressedIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("JobBrowserFilterButtonPressed.png"));

  private static final Icon sViewAnchorIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("JobBrowserViewButton.png"));

  private static final Icon sViewAnchorPressedIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("JobBrowserViewButtonPressed.png"));


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The set of all current pipeline groups.
   */
  private Set<String> pWorkGroups;
  
  /**
   * The set of all current pipeline users.
   */
  private Set<String> pWorkUsers;
  
  /**
   * All job groups indexed by group ID. 
   */ 
  private TreeMap<Long,QueueJobGroup>  pJobGroups;
  
  /**
   * The distribution of job states for the jobs associated with each of the given 
   * job group IDs.
   */
  private TreeMap<Long,double[]>  pJobStateDist; 

  /**
   * The IDs of the selected job groups. 
   */ 
  private TreeSet<Long>  pSelectedIDs; 
  
  private StatusFilter pStatusFilter;

  /**
   * The working area view filter.
   */
  private NewViewFilter  pViewFilter; 
  
  /**
   * The name of the user being filtered on when the viewfilter is set to 
   * {@link NewViewFilter#UserJobs}
   */
  private String pUserFilter;
  
  /**
   * The name of the group being filtered on when the viewfilter is set to 
   * {@link NewViewFilter#GroupJobs}
   */
  private String pGroupFilter;
  
  /**
   * The set of users being filtered on when the viewfilter is set to 
   * {@link NewViewFilter#CustomJobs}
   */
  private TreeSet<String> pCustomFilter;
  
  /**
   * A specific list of job groups to display in the queue job browser panel, which will take
   * precedence over any other view filter.
   */
  private TreeSet<Long> pFilterOverride;

  /**
   * A boolean which reflects whether key choosers need to be rerun for all jobs in the 
   * queue.
   */
  private boolean pDoJobKeysNeedUpdate;

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Popup menu launched by the filter icon. 
   */
  private JPopupMenu pStatusPopup;
  private TreeMap<StatusFilter, JRadioButtonMenuItem> pStatusButtons;
  private ButtonGroup pStatusButtonGroup;
  
  /**
   * Icon to lauch the filter menu.
   */
  private JLabel pStatusMenuLabel;
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Popup menu launched by the filter icon. 
   */
  private JPopupMenu pViewPopup;
  private JMenu pViewUserMenu;
  private JMenu pViewGroupMenu;
  private TreeMap<NewViewFilter, JRadioButtonMenuItem> pViewButtons;
  private TreeMap<String, JRadioButtonMenuItem> pViewUserButtons;
  private TreeMap<String, JRadioButtonMenuItem> pViewGroupButtons;
  private ButtonGroup pViewButtonGroup;
  
  /**
   * Icon to lauch the filter menu.
   */
  private JLabel pViewMenuLabel;
  
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * The groups popup menu.
   */ 
  private JPopupMenu pGroupsPopup;
  
  /**
   * The groups popup menu items.
   */ 
  private JMenuItem  pGroupsUpdateItem; 

  private JMenuItem  pClearFocusItem;
  private JMenuItem  pMonitorGroupsItem;
  
  private JMenuItem  pGroupsQueueItem; 
  private JMenuItem  pGroupsQueueSpecialItem; 
  private JMenuItem  pGroupsPauseItem; 
  private JMenuItem  pGroupsResumeItem; 
  private JMenuItem  pGroupsPreemptItem;
  private JMenuItem  pGroupsKillItem;
  private JMenuItem  pGroupsChangeJobReqsItem;
  private JMenuItem  pGroupsUpdateJobKeysItem;
  private JMenuItem  pGroupsDeleteItem;
  private JMenuItem  pGroupsDeleteCompletedItem;
  private JMenuItem  pUpdateAllJobKeysItem;
  
  private JMenuItem  pGroupShowNodeItem;
  private ArrayList<JMenuItem> pShowNodeChannelItems;


  /**
   * The header panel.
   */ 
  private JPanel pGroupsHeaderPanel; 

  /**
   * The panel title.
   */
  private JLabel  pHeaderLabel;
  
  /**
   * Deletes completed job groups when pressed.
   */ 
  private JButton  pDeleteCompletedButton;
  
  /**
   * Button which updates all the job keys when pressed.
   */
  private JButton  pUpdateAllJobKeysButton;

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

  /**
   * Whether the selection has been modified after the last SHIFT/CTRL press.
   */ 
  private boolean  pSelectionModified; 

  /**
   * Whether the SHIFT/CTRL key is currently being pressed.
   */ 
  private boolean  pModifierPressed; 
  
  /**
   * Dialog that allows for the selection of a custom list of users.
   */
  private JBooleanListDialog pCustomUserFilterDialog;
}
