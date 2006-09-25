// $Id: JQueueJobBrowserPanel.java,v 1.23 2006/09/25 12:11:44 jim Exp $

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
  implements MouseListener, KeyListener, ActionListener, AdjustmentListener
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

      /* the canonical names of this host */ 
      pLocalHostnames = new TreeSet<String>();
      try {
	Enumeration nets = NetworkInterface.getNetworkInterfaces();  
	while(nets.hasMoreElements()) {
	  NetworkInterface net = (NetworkInterface) nets.nextElement();
	  Enumeration addrs = net.getInetAddresses();
	  while(addrs.hasMoreElements()) {
	    InetAddress addr = (InetAddress) addrs.nextElement();
	    if((addr instanceof Inet4Address) && !addr.isLoopbackAddress()) 
	      pLocalHostnames.add(addr.getCanonicalHostName());
	  }
	}
      }
      catch(Exception ex) {
	UIMaster.getInstance().showErrorDialog
	  ("Warning:",      
	   "Could not determine the name of this machine!");
      }
    }    

    /* initialize the popup menus */ 
    {
      JMenuItem item;
      	
      {
	pHostsPopup = new JPopupMenu();

	item = new JMenuItem("Update");
	pHostsUpdateItem = item;
	item.setActionCommand("update");
	item.addActionListener(this);
	pHostsPopup.add(item);

	item = new JMenuItem("History");
	pHostsHistoryItem = item;
	item.setActionCommand("hosts-history");
	item.addActionListener(this);
	pHostsPopup.add(item);

	pHostsPopup.addSeparator();

	item = new JMenuItem("Apply Changes");
	pHostsApplyItem = item;
	item.setActionCommand("hosts-apply");
	item.addActionListener(this);
	pHostsPopup.add(item);

	pHostsPopup.addSeparator();

	item = new JMenuItem("Add Server");
	pHostsAddItem = item;
	item.setActionCommand("hosts-add");
	item.addActionListener(this);
	pHostsPopup.add(item);
	
	item = new JMenuItem("Remove Server");
	pHostsRemoveItem = item;
	item.setActionCommand("hosts-remove");
	item.addActionListener(this);
	pHostsPopup.add(item);
      }

      {	
	pSlotsPopup = new JPopupMenu();

	item = new JMenuItem("Update");
	pSlotsUpdateItem = item;
	item.setActionCommand("update");
	item.addActionListener(this);
	pSlotsPopup.add(item);

	pSlotsPopup.addSeparator();
	
	item = new JMenuItem("View");
	pSlotsViewItem = item;
	item.setActionCommand("slots-edit");
	item.addActionListener(this);
	pSlotsPopup.add(item);
	
	pSlotsViewWithMenu = new JMenu("View With");
	pSlotsPopup.add(pSlotsViewWithMenu);
	
	item = new JMenuItem("View With Default");
	pSlotsViewWithDefaultItem = item;
	item.setActionCommand("slots-edit-with-default");
	item.addActionListener(this);
	pSlotsPopup.add(item);

	pSlotsPopup.addSeparator();

	item = new JMenuItem("Preempt Jobs");
	pSlotsPreemptItem = item;
	item.setActionCommand("slots-preempt-jobs");
	item.addActionListener(this);
	pSlotsPopup.add(item);

	item = new JMenuItem("Kill Jobs");
	pSlotsKillItem = item;
	item.setActionCommand("slots-kill-jobs");
	item.addActionListener(this);
	pSlotsPopup.add(item);
      }

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
      setLayout(new BorderLayout());

      JTabbedPanel tab = new JTabbedPanel();
      pTab = tab;

      /* job servers panel */ 
      {
	JPanel body = new JPanel();
	body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));  
      
	{
	  JPanel panel = new JPanel();
	  pHostsHeaderPanel = panel;
	  panel.setName("DialogHeader");	
	  
	  panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
	  
	  panel.addMouseListener(this); 
	  panel.setFocusable(true);
	  panel.addKeyListener(this);
	  panel.addMouseListener(new KeyFocuser(panel));
	  
	  {
	    JLabel label = new JLabel("Job Servers:");
	    label.setName("DialogHeaderLabel");	
	    
	    panel.add(label);	  
	  }
	  
	  panel.add(Box.createHorizontalGlue());

	  {
	    JToggleButton btn = new JToggleButton();		
	    pStatButton = btn;
	    btn.setName("StatButton");
	    
	    Dimension size = new Dimension(30, 10);
	    btn.setMinimumSize(size);
	    btn.setMaximumSize(size);
	    btn.setPreferredSize(size);
	    
	    btn.setSelected(true);
	    btn.setActionCommand("toggle-stat-columns");
	    btn.addActionListener(this);
	    
	    btn.setToolTipText(UIFactory.formatToolTip
	     ("Toggle display of the Status and OS columns."));
	    
	    panel.add(btn);
	  } 
	  
	  panel.add(Box.createRigidArea(new Dimension(15, 0)));

	  {
	    JToggleButton btn = new JToggleButton();		
	    pDynButton = btn;
	    btn.setName("DynButton");
	    
	    Dimension size = new Dimension(30, 10);
	    btn.setMinimumSize(size);
	    btn.setMaximumSize(size);
	    btn.setPreferredSize(size);
	    
	    btn.setSelected(true);
	    btn.setActionCommand("toggle-dyn-columns");
	    btn.addActionListener(this);
	    
	    btn.setToolTipText(UIFactory.formatToolTip
	     ("Toggle display of the System Load, Free Memory and Free Disk Space columns."));
	    
	    panel.add(btn);
	  } 
	  
	  panel.add(Box.createRigidArea(new Dimension(15, 0)));

	  {
	    JToggleButton btn = new JToggleButton();		
	    pJobButton = btn;
	    btn.setName("JobButton");
	    
	    Dimension size = new Dimension(30, 10);
	    btn.setMinimumSize(size);
	    btn.setMaximumSize(size);
	    btn.setPreferredSize(size);
	    
	    btn.setSelected(true);
	    btn.setActionCommand("toggle-job-columns");
	    btn.addActionListener(this);
	    
	    btn.setToolTipText(UIFactory.formatToolTip
	      ("Toggle display of the Jobs and Slots columns."));
	  
	    panel.add(btn);
	  } 

	  panel.add(Box.createRigidArea(new Dimension(15, 0)));

	  {
	    JToggleButton btn = new JToggleButton();		
	    pDspButton = btn;
	    btn.setName("DspButton");
	    
	    Dimension size = new Dimension(30, 10);
	    btn.setMinimumSize(size);
	    btn.setMaximumSize(size);
	    btn.setPreferredSize(size);
	    
	    btn.setSelected(true);
	    btn.setActionCommand("toggle-dsp-columns");
	    btn.addActionListener(this);
	    
	    btn.setToolTipText(UIFactory.formatToolTip
	      ("Toggle display of the Reservation, Order, Group and Schedule columns."));
	    
	    panel.add(btn);
	  } 
	  
	  panel.add(Box.createRigidArea(new Dimension(45, 0)));

	  {
	    JButton btn = new JButton();		
	    pHostsApplyButton = btn;
	    btn.setName("ApplyHeaderButton");
	    
	    Dimension size = new Dimension(19, 19);
	    btn.setMinimumSize(size);
	    btn.setMaximumSize(size);
	    btn.setPreferredSize(size);
	    
	    btn.setActionCommand("hosts-apply");
	    btn.addActionListener(this);
	    
	    panel.add(btn);
	  } 
	  
	  body.add(panel);
	}
	
	{
	  JPanel panel = new JPanel();
	  panel.setName("MainPanel");
	  panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));  
	  
	  panel.addMouseListener(this); 
	  panel.setFocusable(true);
	  panel.addKeyListener(this);
	  panel.addMouseListener(new KeyFocuser(panel));
	  
	  {
	    pHostsTableModel = new QueueHostsTableModel(this, pLocalHostnames);
	    pHostnamesTableModel = new QueueHostnamesTableModel(this, pHostsTableModel);
	  }

	  {	
	    Box vbox = new Box(BoxLayout.Y_AXIS);
	    vbox.setAlignmentX(0.5f);

	    {
	      JTablePanel tpanel =
		new JTablePanel(pHostnamesTableModel, 
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER, 
				ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
	      pHostnamesTablePanel = tpanel;
	      
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

	      vbox.add(tpanel);
	    }

	    vbox.add(Box.createRigidArea(new Dimension(0, 14)));

	    vbox.setMinimumSize(new Dimension(206, 30));
	    vbox.setMaximumSize(new Dimension(206, Integer.MAX_VALUE));

	    panel.add(vbox);
	  }

	  panel.add(Box.createRigidArea(new Dimension(2, 0)));

	  {	    
	    JTablePanel tpanel = new JTablePanel(pHostsTableModel);
	    pHostsTablePanel = tpanel;
	  
	    {
	      JScrollPane scroll = tpanel.getTableScroll();

	      scroll.setHorizontalScrollBarPolicy
		(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
	      
	      scroll.getVerticalScrollBar().addAdjustmentListener(this);
	  
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
	    
	  body.add(panel);
	}

        tab.addTab(body);

	/* synchronize the selected rows in the two hosts tables */ 
	{
	  JTable ntable = pHostnamesTablePanel.getTable();
	  JTable htable = pHostsTablePanel.getTable();

	  ntable.getSelectionModel().addListSelectionListener
	    (new TableSyncSelector(ntable, htable));

	  htable.getSelectionModel().addListSelectionListener
	    (new TableSyncSelector(htable, ntable));
	}
      }

      /* job slots panel */ 
      {
	JPanel body = new JPanel();
	body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));  
      
	{
	  JPanel panel = new JPanel();
	  pSlotsHeaderPanel = panel;
	  panel.setName("DialogHeader");	
	  
	  panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
	  
	  panel.addMouseListener(this); 
	  panel.setFocusable(true);
	  panel.addKeyListener(this);
	  panel.addMouseListener(new KeyFocuser(panel));
	  
	  {
	    JLabel label = new JLabel("Job Server Slots:");
	    label.setName("DialogHeaderLabel");	
	    
	    panel.add(label);	  
	  }
	  
	  panel.add(Box.createHorizontalGlue());

	  body.add(panel);
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
	    QueueSlotsTableModel model = new QueueSlotsTableModel(this, pLocalHostnames);
	    pSlotsTableModel = model;
	    
	    JTablePanel tpanel = new JTablePanel(model);
	    pSlotsTablePanel = tpanel;
	    
	    pSlotsListSelector = new SlotsListSelector();
	    ListSelectionModel smodel = tpanel.getTable().getSelectionModel();
	    smodel.addListSelectionListener(pSlotsListSelector);

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

	  body.add(panel);
	}

        tab.addTab(body);
      }

      /* job groups panel */ 
      {
	JPanel body = new JPanel();
	body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));  
      
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
	  
	  body.add(panel);
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
	  
	  body.add(panel);
	}

        tab.addTab(body);
      }
      
      add(tab);
    } 
 
    updateJobs(null, null, null, null, null, null);
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

    if(pJobGroups != null)
      updateAll();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get names of the selected hosts in the hosts table.
   */ 
  public TreeSet<String> 
  getSelectedHostnames() 
  {
    TreeSet<String> hostnames = new TreeSet<String>();
    int rows[] = pHostsTablePanel.getTable().getSelectedRows();
    int wk;
    for(wk=0; wk<rows.length; wk++) {
      String hname = pHostsTableModel.getHostname(rows[wk]);
      hostnames.add(hname);
    }

    return hostnames;
  }

  /**
   * Get IDs of the jobs running on the selected job server slots.
   */ 
  public TreeSet<Long> 
  getSelectedSlotJobIDs() 
  {
    TreeSet<Long> jobIDs = new TreeSet<Long>();
    int rows[] = pSlotsTablePanel.getTable().getSelectedRows();
    int wk;
    for(wk=0; wk<rows.length; wk++) {
      Long jobID = pSlotsTableModel.getJobID(rows[wk]);
      if(jobID != null) 
	jobIDs.add(jobID);
    }

    return jobIDs;
  }

  /**
   * Get ID of job running on the singly selected job server slot.
   */ 
  public Long
  getSelectedSlotJobID()
  {
    int rows[] = pSlotsTablePanel.getTable().getSelectedRows();
    if(rows.length == 1) 
      return pSlotsTableModel.getJobID(rows[0]);
    return null;
  }
  

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
  


  /*----------------------------------------------------------------------------------------*/

  /**
   * Reset the caches of toolset plugins and plugin menu layouts.
   */ 
  public void 
  clearPluginCache()
  {
    pSlotsEditorMenuToolset = null;
  }

  /**
   * Update the slots editor plugin menus.
   */ 
  private void 
  updateSlotsEditorMenus()
  {
    String toolset = null;
    {
      int row = pSlotsTablePanel.getTable().getSelectedRow();
      if(row != -1) {
	Long jobID = pSlotsTableModel.getJobID(row);
	if(jobID != null) {
	  JobStatus status = pJobStatus.get(jobID);
	  if(status != null) 
	    toolset = status.getToolset();
	}
      }
    }
	  
    if((toolset != null) && !toolset.equals(pSlotsEditorMenuToolset)) {
      UIMaster master = UIMaster.getInstance();
      master.rebuildEditorMenu(toolset, pSlotsViewWithMenu, this);
      
      pSlotsEditorMenuToolset = toolset;
    }
  }




  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Perform the initial update which restores the selected job groups synchronously.
   */ 
  public void 
  restoreSelection() 
  { 
    QueryTask task = new QueryTask();
    try {
      task.start();
      task.join();
    }
    catch(InterruptedException ex) {
      UIMaster.getInstance().showErrorDialog(ex);
    }
  }
 

  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the current jobs groups, servers and slots asynchronously.
   */ 
  public void 
  updateAll() 
  {
    if(UIMaster.getInstance().isRestoring())
      return;

    UIMaster.getInstance().getMasterMgrClient().invalidateCachedPrivilegeDetails();

    QueryTask task = new QueryTask();
    task.start();
  }

  /**
   * Update the jobs groups, servers and slots asynchronously.
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
   * @param jobStatus
   *   The job status indexed by job ID.
   * 
   * @param jobInfo
   *   The information about the running jobs indexed by job ID.
   * 
   * @param hosts
   *   The job server hosts indexex by fully resolved hostnames.
   * 
   * @param selectionGroups
   *   The valid selection group names. 
   * 
   * @param selectionSchedules
   *   The valid selection schedule names. 
   */ 
  public synchronized void
  updateJobs
  (
   String author, 
   String view, 
   TreeMap<Long,QueueJobGroup> groups, 
   TreeMap<Long,JobStatus> jobStatus, 
   TreeMap<Long,QueueJobInfo> jobInfo, 
   TreeMap<String,QueueHostInfo> hosts, 
   TreeSet<String> selectionGroups, 
   TreeSet<String> selectionSchedules
  ) 
  {
    if(!pAuthor.equals(author) || !pView.equals(view)) 
      super.setAuthorView(author, view);

    updateJobs(groups, jobStatus, jobInfo, hosts, selectionGroups, selectionSchedules);
  }

  /**
   * Update the jobs groups, servers and slots. 
   * 
   * @param groups
   *   The queue job groups indexe by job group ID.
   * 
   * @param jobStatus
   *   The job status indexed by job ID.
   * 
   * @param jobInfo
   *   The information about the running jobs indexed by job ID.
   * 
   * @param hosts
   *   The job server hosts indexex by fully resolved hostnames.
   * 
   * @param selectionGroups
   *   The valid selection group names. 
   * 
   * @param selectionSchedules
   *   The valid selection schedule names. 
   */ 
  public synchronized void
  updateJobs
  (
   TreeMap<Long,QueueJobGroup> groups, 
   TreeMap<Long,JobStatus> jobStatus, 
   TreeMap<Long,QueueJobInfo> jobInfo, 
   TreeMap<String,QueueHostInfo> hosts, 
   TreeSet<String> selectionGroups, 
   TreeSet<String> selectionSchedules
  ) 
  {
    updatePrivileges();

    /* update the groups and job status */ 
    {
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
      
      pJobStatus.clear();
      if(jobStatus != null) 
	pJobStatus.putAll(jobStatus);
    }
    
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

    /* job server panel */ 
    if((hosts != null) && (selectionGroups != null) && (selectionSchedules != null)) {
      pHostnamesTableModel.setHostnames(hosts.keySet());

      pHostsTableModel.setQueueHosts
	(hosts, selectionGroups, selectionSchedules, pPrivilegeDetails);

      updateHostsHeaderButtons();
      pHostsTablePanel.tableStructureChanged();  
      pHostsTablePanel.adjustmentValueChanged(null);

      pHostsApplyItem.setEnabled(false);
      pHostsApplyButton.setEnabled(false);
    }

    /* job slots panel */ 
    if((hosts != null) && (jobInfo != null)) {
      ListSelectionModel smodel = pSlotsTablePanel.getTable().getSelectionModel();
      smodel.removeListSelectionListener(pSlotsListSelector);
      {
	pSlotsTableModel.setSlots(hosts, pJobStatus, jobInfo);
      }
      smodel.addListSelectionListener(pSlotsListSelector);      
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

	/* if a single job group containing a single job was just selected, 
  	     get the viewer to update any attached job details panel with this job */ 
	Long justSelected = pGroupsListSelector.getJustSelected();
	if(justSelected != null) {
	  QueueJobGroup group = sgroups.get(justSelected);
	  if(group != null) {
	    SortedSet<Long> jobIDs = group.getJobIDs();
	    if(jobIDs.size() == 1)
	      panel.enableDetailsUpdate(jobIDs.first());
	  }
	}
	  
	/* update the viewer */ 
	panel.updateQueueJobs(pAuthor, pView, sgroups, pJobStatus);
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the job server hosts menu.
   */ 
  public void 
  updateHostsMenu() 
  {
    boolean selected = (pHostsTablePanel.getTable().getSelectedRowCount() > 0);
    pHostsHistoryItem.setEnabled(selected);
    pHostsAddItem.setEnabled(pPrivilegeDetails.isQueueAdmin()); 
    pHostsRemoveItem.setEnabled(pPrivilegeDetails.isQueueAdmin() && selected); 
  }

  /**
   * Update the job server slots menu.
   */ 
  public void 
  updateSlotsMenu() 
  {
    boolean single = (getSelectedSlotJobID() != null); 
    pSlotsViewItem.setEnabled(single); 
    pSlotsViewWithMenu.setEnabled(single); 
    if(single) 
      updateSlotsEditorMenus();
    pSlotsViewWithDefaultItem.setEnabled(single); 

    boolean selected = (!getSelectedSlotJobIDs().isEmpty()); 
    pSlotsPreemptItem.setEnabled(selected);
    pSlotsKillItem.setEnabled(selected);
  }

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
   * Update the sort of the hostnames table to match the hosts table.
   */ 
  public void
  sortHostnamesTable
  (
   int[] rowToIndex
  )
  {
    if(pHostnamesTableModel != null) 
      pHostnamesTableModel.externalSort(rowToIndex);
  }

  /** 
   * Update the sort of the hosts table to match the hostnames table.
   */ 
  public void
  sortHostsTable
  (
   int[] rowToIndex
  )
  {
    if(pHostsTableModel != null) 
      pHostsTableModel.externalSort(rowToIndex);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the state of the column visiblity buttons.
   */ 
  public void 
  updateHostsHeaderButtons()
  {
    {
      TreeSet<String> cnames = new TreeSet<String>();
      cnames.add("Status");
      cnames.add("OS");
    
      boolean selected = true;
      for(String cname : cnames) {
	if(!pHostsTablePanel.isColumnVisible(cname)) {
	  selected = false;
	  break;
	}
      }
      
      pStatButton.setSelected(selected);
    }

    {
      TreeSet<String> cnames = new TreeSet<String>();
      cnames.add("System Load");
      cnames.add("Free Memory");
      cnames.add("Free Disk Space");
    
      boolean selected = true;
      for(String cname : cnames) {
	if(!pHostsTablePanel.isColumnVisible(cname)) {
	  selected = false;
	  break;
	}
      }
      
      pDynButton.setSelected(selected);
    }

    {
      TreeSet<String> cnames = new TreeSet<String>();
      cnames.add("Jobs");
      cnames.add("Slots");
    
      boolean selected = true;
      for(String cname : cnames) {
	if(!pHostsTablePanel.isColumnVisible(cname)) {
	  selected = false;
	  break;
	}
      }
      
      pJobButton.setSelected(selected);
    }

    {
      TreeSet<String> cnames = new TreeSet<String>();
      cnames.add("Reservation");
      cnames.add("Order");
      cnames.add("Group");
      cnames.add("Schedule");

      boolean selected = true;
      for(String cname : cnames) {
	if(!pHostsTablePanel.isColumnVisible(cname)) {
	  selected = false;
	  break;
	}
      }

      pDspButton.setSelected(selected);
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
    switch(pTab.getSelectedIndex()) {
    case 0:
      if(pHostsHeaderPanel.getMousePosition(true) != null) {
	pHostsHeaderPanel.requestFocusInWindow();
	return true;
      }
      break;

    case 1:
      if(pSlotsHeaderPanel.getMousePosition(true) != null) {
	pSlotsHeaderPanel.requestFocusInWindow();
	return true;
      }
      else if(pSlotsTablePanel.getMousePosition(true) != null) {
	pSlotsTablePanel.requestFocusInWindow();
	return true;
      }
      else if(pSlotsTablePanel.getTable().getMousePosition(true) != null) {
	pSlotsTablePanel.getTable().requestFocusInWindow();
	return true;
      }
      break;
      
    case 2:
      if(pGroupsHeaderPanel.getMousePosition(true) != null) {
	pGroupsHeaderPanel.requestFocusInWindow();
	return true;
      }
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
      (pHostsUpdateItem, prefs.getUpdate(),
       "Update the job servers, slots and groups.");
    updateMenuToolTip
      (pHostsHistoryItem, prefs.getJobBrowserHostsHistory(),
       "Show the resource usage history for the selected servers.");
    updateMenuToolTip
      (pHostsApplyItem, prefs.getApplyChanges(),
       "Apply the changes to job server properties.");
    updateMenuToolTip
      (pHostsAddItem, prefs.getJobBrowserHostsAdd(),
       "Add a new job server.");
    updateMenuToolTip
      (pHostsRemoveItem, prefs.getJobBrowserHostsRemove(),
       "Remove the selected job servers.");
       
    updateMenuToolTip
      (pSlotsUpdateItem, prefs.getUpdate(),
       "Update the job servers, slots and groups.");
    updateMenuToolTip
      (pSlotsViewItem, prefs.getEdit(), 
       "View the target files of the selected job server slot.");
    updateMenuToolTip
      (pSlotsViewWithDefaultItem, prefs.getEdit(), 
       "View the target files of the selected job server slot using the default" + 
       "editor for the file type.");
    updateMenuToolTip
      (pSlotsPreemptItem, prefs.getPreemptJobs(), 
       "Preempt all jobs associated with the selected groups.");
    updateMenuToolTip
      (pSlotsKillItem, prefs.getKillJobs(), 
       "Kill all jobs associated with the selected groups.");

    updateMenuToolTip
      (pGroupsUpdateItem, prefs.getUpdate(),
       "Update the job servers, slots and groups.");
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
	  switch(pTab.getSelectedIndex()) {      
	  case 0:
	    updateHostsMenu();
	    pHostsPopup.show(e.getComponent(), e.getX(), e.getY());
	    break;
	    
	  case 1:
	    updateSlotsMenu();
	    pSlotsPopup.show(e.getComponent(), e.getX(), e.getY());
	    break;
	    
	  case 2:
	    updateGroupsMenu();
	    pGroupsPopup.show(e.getComponent(), e.getX(), e.getY());      
	  }
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
      updateAll();
    else {
      switch(pTab.getSelectedIndex()) {      
      case 0:
	if((prefs.getJobBrowserHostsHistory() != null) &&
	   prefs.getJobBrowserHostsHistory().wasPressed(e))
	  doHostsHistory();
	else if((prefs.getApplyChanges() != null) &&
		prefs.getApplyChanges().wasPressed(e))
	  doHostsApply();
	else if((prefs.getJobBrowserHostsAdd() != null) &&
		prefs.getJobBrowserHostsAdd().wasPressed(e))
	  doHostsAdd();
	else if((prefs.getJobBrowserHostsRemove() != null) &&
		prefs.getJobBrowserHostsRemove().wasPressed(e))
	  doHostsRemove();
	else 
	  unsupported = true;
	break;

      case 1:
	if((prefs.getKillJobs() != null) &&
	   prefs.getKillJobs().wasPressed(e))
	  doSlotsKillJobs();

	else if((prefs.getEdit() != null) &&
		prefs.getEdit().wasPressed(e)) 
	  doSlotsView();
	else if((prefs.getEditWithDefault() != null) &&
		prefs.getEditWithDefault().wasPressed(e))
	  doSlotsViewWithDefault();
	else 
	  unsupported = true;
	break;

      case 2:
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
      updateAll();

    else if(cmd.equals("hosts-history")) 
      doHostsHistory();
    else if(cmd.equals("hosts-apply")) 
      doHostsApply();
    else if(cmd.equals("hosts-add")) 
      doHostsAdd();
    else if(cmd.equals("hosts-remove")) 
      doHostsRemove();

    else if(cmd.equals("toggle-stat-columns"))
      doHostsToggleStatColumns();
    else if(cmd.equals("toggle-dyn-columns"))
      doHostsToggleDynColumns();
    else if(cmd.equals("toggle-job-columns"))
      doHostsToggleJobColumns();
    else if(cmd.equals("toggle-dsp-columns"))
      doHostsToggleDspColumns();

    else if(cmd.equals("slots-preempt-jobs")) 
      doSlotsPreemptJobs();

    else if(cmd.equals("slots-edit"))
      doSlotsView();
    else if(cmd.equals("slots-edit-with-default"))
      doSlotsViewWithDefault();
    else if(cmd.startsWith("slots-edit-with:"))
      doSlotsViewWith(cmd.substring(10));    

    else if(cmd.equals("slots-kill-jobs")) 
      doSlotsKillJobs();

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


  /*-- ADJUSTMENT LISTENER METHODS ---------------------------------------------------------*/

  /**
   * Invoked when the value of the adjustable has changed.
   */ 
  public void
  adjustmentValueChanged
  (
   AdjustmentEvent e
  )
  { 
    JViewport nview = pHostnamesTablePanel.getTableScroll().getViewport();
    JViewport hview = pHostsTablePanel.getTableScroll().getViewport();
    if((nview != null) && (hview != null)) {
      Point npos = nview.getViewPosition();    
      Point hpos = hview.getViewPosition();    
      
      if(npos.y != hpos.y) {
 	npos.y = hpos.y;
 	nview.setViewPosition(npos);
      }
    }  
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Signals that the hosts properties have been edited.
   */ 
  public void 
  doHostsEdited() 
  {
    pHostsApplyItem.setEnabled(true);
    pHostsApplyButton.setEnabled(true);
  }

  /**
   * Show the resource usage history dialogs for the selected hosts.
   */ 
  public void   
  doHostsHistory()
  {
    pHostsTablePanel.cancelEditing();

    GetHistoryTask task = new GetHistoryTask(getSelectedHostnames());
    task.start();      
  }

  /**
   * Apply the changes to server properties. 
   */ 
  public void 
  doHostsApply()
  {
    EditHostsTask task = new EditHostsTask();
    task.start();
  }

  /**
   * Add a new server. 
   */ 
  public void 
  doHostsAdd()
  {
    pHostsTablePanel.cancelEditing();

    JNewJobServerDialog diag = new JNewJobServerDialog(getTopFrame());
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
  public void 
  doHostsRemove()
  {
    pHostsTablePanel.cancelEditing();

    TreeSet<String> hostnames = getSelectedHostnames();    
    if(!hostnames.isEmpty()) {
      RemoveHostsTask task = new RemoveHostsTask(hostnames);
      task.start();
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Toggle display of the Status, Reservation and Order columns."
   */ 
  public void 
  doHostsToggleStatColumns()
  { 
    TreeSet<String> cnames = new TreeSet<String>();
    cnames.add("Status");
    cnames.add("OS");
    
    boolean isVisible = false;
    for(String cname : cnames) {
      if(!pHostsTablePanel.isColumnVisible(cname)) {
	isVisible = true;
	break;
      }
    }
    
    pHostsTablePanel.setColumnsVisible(cnames, isVisible);
  }

  /**
   * Toggle display of the System Load, Free Memory and Free Disk Space columns."
   */ 
  public void 
  doHostsToggleDynColumns()
  { 
    TreeSet<String> cnames = new TreeSet<String>();
    cnames.add("System Load");
    cnames.add("Free Memory");
    cnames.add("Free Disk Space");
    
    boolean isVisible = false;
    for(String cname : cnames) {
      if(!pHostsTablePanel.isColumnVisible(cname)) {
	isVisible = true;
	break;
      }
    }
    
    pHostsTablePanel.setColumnsVisible(cnames, isVisible);
  }

  /**
   * Toggle display of the Jobs and Slots columns.
   */ 
  public void 
  doHostsToggleJobColumns()
  {
    TreeSet<String> cnames = new TreeSet<String>();
    cnames.add("Jobs");
    cnames.add("Slots");
    
    boolean isVisible = false;
    for(String cname : cnames) {
      if(!pHostsTablePanel.isColumnVisible(cname)) {
	isVisible = true;
	break;
      }
    }
    
    pHostsTablePanel.setColumnsVisible(cnames, isVisible);
  }

  /**
   * Toggle display of the selection key bias columns.
   */ 
  public void 
  doHostsToggleDspColumns()
  {
    TreeSet<String> cnames = new TreeSet<String>();
      cnames.add("Reservation");
      cnames.add("Order");
      cnames.add("Group");
      cnames.add("Schedule");

    boolean isVisible = false;
    for(String cname : cnames) {
      if(!pHostsTablePanel.isColumnVisible(cname)) {
	isVisible = true;
	break;
      }
    }
    
    pHostsTablePanel.setColumnsVisible(cnames, isVisible);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Preempt the jobs running on the selected job server slots.
   */ 
  public void 
  doSlotsPreemptJobs()
  {
    TreeMap<String,TreeSet<Long>> jobs = new TreeMap<String,TreeSet<Long>>();
    for(Long jobID : getSelectedSlotJobIDs()) {
      JobStatus status = pJobStatus.get(jobID);
      String author = null;
      if(status != null) 
	author = status.getNodeID().getAuthor();
      
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

    if(!jobs.isEmpty()) {
      PreemptJobsTask task = new PreemptJobsTask(jobs);
      task.start();
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * View the target files of the job running on the selected slot.
   */ 
  private void 
  doSlotsView() 
  {
    Long jobID = getSelectedSlotJobID();
    if(jobID != null) {
      JobStatus status = pJobStatus.get(jobID);
      if(status != null) {
	ViewTask task = new ViewTask(status.getNodeID(), status.getTargetSequence());
	task.start();
      }
    }
  }

  /**
   * View the target files of the job running on the selected slot using the default 
   * editor for the file type.
   */ 
  private void 
  doSlotsViewWithDefault() 
  {
    Long jobID = getSelectedSlotJobID();
    if(jobID != null) {
      JobStatus status = pJobStatus.get(jobID);
      if(status != null) {
	ViewTask task = new ViewTask(status.getNodeID(), status.getTargetSequence(), true);
	task.start();
      }
    }
  }

  /**
   * View the target files of the job running on the selected slot with the given editor.
   */ 
  private void 
  doSlotsViewWith
  (
   String editor
  ) 
  {
    String parts[] = editor.split(":");
    assert(parts.length == 3);
    
    String ename   = parts[0];
    VersionID evid = new VersionID(parts[1]);
    String evendor = parts[2];

    Long jobID = getSelectedSlotJobID();
    if(jobID != null) {
      JobStatus status = pJobStatus.get(jobID);
      if(status != null) {
	ViewTask task = new ViewTask(status.getNodeID(), status.getTargetSequence(), false, 
				     ename, evid, evendor);
	task.start();
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Kill the jobs running on the selected job server slots.
   */ 
  public void 
  doSlotsKillJobs()
  {
    TreeMap<String,TreeSet<Long>> jobs = new TreeMap<String,TreeSet<Long>>();
    for(Long jobID : getSelectedSlotJobIDs()) {
      JobStatus status = pJobStatus.get(jobID);
      String author = null;
      if(status != null) 
	author = status.getNodeID().getAuthor();
      
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

    if(!jobs.isEmpty()) {
      KillJobsTask task = new KillJobsTask(jobs);
      task.start();
    }
  }


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
    updateAll();
  }
  
  /**
   * Show only job groups owned by the current view.
   */ 
  public void 
  doSingleViewFilter() 
  {
    pViewFilter = ViewFilter.SingleView;
    pViewFilterButton.setName(pViewFilter + "Button");
    updateAll();
  }
  
  /**
   * Show job groups from any view owned by the current user.
   */ 
  public void 
  doOwnedViewsFilter() 
  {
    pViewFilter = ViewFilter.OwnedViews;
    pViewFilterButton.setName(pViewFilter + "Button");
    updateAll();
  }
  
  /**
   * Show job groups from all views.
   */ 
  public void 
  doAllViewsFilter() 
  {
    pViewFilter = ViewFilter.AllViews;
    pViewFilterButton.setName(pViewFilter + "Button");
    updateAll();
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
    encoder.encode("SelectedTabIndex", pTab.getSelectedIndex());

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

    Integer idx = (Integer) decoder.decode("SelectedTabIndex");
    if(idx != null) 
      pTab.setSelectedIndex(idx);    

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
  class TableSyncSelector
    implements ListSelectionListener
  {
    public 
    TableSyncSelector
    (
     JTable source,
     JTable target
    ) 
    {
      pSourceTable = source;
      pTargetTable = target;
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
      
      DefaultListSelectionModel smodel = 
	(DefaultListSelectionModel) pTargetTable.getSelectionModel();

      ListSelectionListener[] listeners = smodel.getListSelectionListeners();
      {
	int wk;
	for(wk=0; wk<listeners.length; wk++) 
	  smodel.removeListSelectionListener(listeners[wk]);
      }

      smodel.clearSelection();

      {
	int rows[] = pSourceTable.getSelectedRows();
	int wk; 
	for(wk=0; wk<rows.length; wk++) 
	  smodel.addSelectionInterval(rows[wk], rows[wk]);	  
      }
      
      {
	int wk;
	for(wk=0; wk<listeners.length; wk++) 
	  smodel.addListSelectionListener(listeners[wk]);
      }

      pTargetTable.repaint();
    }

    private JTable pSourceTable;
    private JTable pTargetTable;
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
  
      updateAll();      
    }

    private Long  pJustSelected; 
  }


  /*----------------------------------------------------------------------------------------*/

  private 
  class SlotsListSelector
    implements ListSelectionListener
  {
    public 
    SlotsListSelector() 
    {}

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

      if(pGroupID > 0) {
	Long jobID = null;
	{
	  int rows[] = pSlotsTablePanel.getTable().getSelectedRows();
	  int wk;
	  for(wk=0; wk<rows.length; wk++) 
	    jobID = pSlotsTableModel.getJobID(rows[wk]);
	}
	
	GetJobInfoTask task = new GetJobInfoTask(pGroupID, jobID);
	task.start();
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current job information. 
   */ 
  private 
  class GetJobInfoTask
    extends Thread
  {
    public 
    GetJobInfoTask
    (
     int groupID,
     Long jobID
    ) 
    {
      super("JQueueJobBrowserPanel:GetJobInfoTask");

      pGroupID = groupID;
      pJobID   = jobID;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();      

      QueueJob job = null;
      QueueJobInfo info = null; 
      SubProcessExecDetails details = null; 
      if((pGroupID > 0) && (pJobID != null)) {
	if(master.beginPanelOp("Updating Job Details...")) {
	  try {
	    QueueMgrClient client = master.getQueueMgrClient();
	    job  = client.getJob(pJobID);
	    info = client.getJobInfo(pJobID);

	    String hostname = info.getHostname();
	    if(hostname != null) {
	      JobMgrClient jclient = new JobMgrClient(hostname);
	      details = jclient.getExecDetails(pJobID);
	    }
	  }
	  catch(PipelineException ex) {
	    master.showErrorDialog(ex);
	  }
	  finally {
	    master.endPanelOp("Done.");
	  }
	}
      }
	
      UpdateSlotsSelectionTask task = 
	new UpdateSlotsSelectionTask(pGroupID, job, info, details);
      SwingUtilities.invokeLater(task);
    }

    private int   pGroupID;
    private Long  pJobID; 
  }

  /** 
   * Update the JobViewer/JobDetails subpanels that the Job Slots selection has changed.
   */ 
  private
  class UpdateSlotsSelectionTask
    extends Thread
  {
    private 
    UpdateSlotsSelectionTask
    (
     int groupID, 
     QueueJob job, 
     QueueJobInfo info, 
     SubProcessExecDetails details
    ) 
    {
      super("JQueueJobBrowserPanel:UpdateSlotsSelectionTask");

      pGroupID     = groupID;
      pJob         = job; 
      pJobInfo     = info; 
      pExecDetails = details;
    }

    public void 
    run()     
    {
      UIMaster master = UIMaster.getInstance();

      /* update any connected JobViewer panel */ 
      if(pGroupID > 0) {
	PanelGroup<JQueueJobViewerPanel> panels = master.getQueueJobViewerPanels();
	JQueueJobViewerPanel panel = panels.getPanel(pGroupID);
	if(panel != null) {
	  if((pJob != null) && (pJobInfo != null))
	    panel.disableDetailsUpdate();
	  else 
	    panel.enableDetailsUpdate();
	}
      }

      /* update any connected JobDetails panel */ 
      if(pGroupID > 0) {
	PanelGroup<JQueueJobDetailsPanel> panels = master.getQueueJobDetailsPanels();
	JQueueJobDetailsPanel panel = panels.getPanel(pGroupID);
	if(panel != null) {
	  panel.updateJob(pAuthor, pView, pJob, pJobInfo, pExecDetails);
	}
      }
    }

    private int                    pGroupID;
    private QueueJob               pJob; 
    private QueueJobInfo           pJobInfo; 
    private SubProcessExecDetails  pExecDetails; 
  }



 
  /*----------------------------------------------------------------------------------------*/
 
  /** 
   * Query the queue manager.
   */ 
  private
  class QueryTask
    extends Thread
  {
    public 
    QueryTask() 
    {
      super("JQueueJobBrowserPanel:QueryTask");
    }
 
    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();

      TreeMap<Long,QueueJobGroup> groups = null;
      TreeMap<Long,JobStatus> jobStatus = null;
      TreeMap<Long,QueueJobInfo> jobInfo = null;
      TreeMap<String,QueueHostInfo> hosts = null;
      TreeSet<String> selectionGroups = null;
      TreeSet<String> selectionSchedules = null;

      if(master.beginPanelOp("Updating...")) {
	try {
	  QueueMgrClient client = master.getQueueMgrClient();
	  
	  groups     = client.getJobGroups(); 
	  jobStatus  = client.getJobStatus(new TreeSet<Long>(groups.keySet()));
	  jobInfo    = client.getRunningJobInfo();
	  hosts      = client.getHosts(); 

	  selectionGroups    = client.getSelectionGroupNames();
	  selectionSchedules = client.getSelectionScheduleNames();
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	}
	finally {
	  master.endPanelOp("Done.");
	}
      }
	
      UpdateTask task = new UpdateTask(groups, jobStatus, jobInfo, 
				       hosts, selectionGroups, selectionSchedules); 
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
     TreeMap<Long,JobStatus> jobStatus, 
     TreeMap<Long,QueueJobInfo> jobInfo, 
     TreeMap<String,QueueHostInfo> hosts, 
     TreeSet<String> selectionGroups, 
     TreeSet<String> selectionSchedules
    ) 
    {
      super("JQueueJobBrowserPanel:UpdateTask");

      pGroups = groups;
      pStatus = jobStatus; 
      pInfo   = jobInfo;
      pHosts  = hosts; 

      pSelectionGroups    = selectionGroups; 
      pSelectionSchedules = selectionSchedules; 
    }

    public void 
    run() 
    {
      /* enable/disable the DeleteCompleted button */ 
      switch(pViewFilter) {
      case SingleView:
      case OwnedViews:
	pDeleteCompletedButton.setEnabled(!isLocked());
	break;
	
      case AllViews:
	pDeleteCompletedButton.setEnabled(pPrivilegeDetails.isQueueAdmin());
      }

      /* update the panels */ 
      updateJobs(pGroups, pStatus, pInfo, pHosts, pSelectionGroups, pSelectionSchedules);
    }
    
    private TreeMap<Long,QueueJobGroup>   pGroups; 
    private TreeMap<Long,JobStatus>       pStatus; 
    private TreeMap<Long,QueueJobInfo>    pInfo; 
    private TreeMap<String,QueueHostInfo> pHosts;

    private TreeSet<String>  pSelectionGroups; 
    private TreeSet<String>  pSelectionSchedules;
  }
  
  
  /*----------------------------------------------------------------------------------------*/

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
     TreeSet<String> hostnames
    ) 
    {
      super("JQueueJobBrowserPanel:GetHistoryTask");

      pHostnames = hostnames; 
    }
    
    public void
    run()
    {
      UIMaster master = UIMaster.getInstance();
      QueueMgrClient qclient = master.getQueueMgrClient();
      
      TreeMap<String,ResourceSampleBlock> samples = new TreeMap<String,ResourceSampleBlock>();
      if(master.beginPanelOp()) {
	try {
	  for(String hname : pHostnames) {
	    try {
	      master.updatePanelOp("Loading History: " + hname);
	      ResourceSampleBlock block = qclient.getHostResourceSamples(hname);
	      if(block != null) 
		samples.put(hname, block);
	    }
	    catch(PipelineException ex) {
	      master.showErrorDialog(ex);
	    }
	  }
	}
	finally {
	  master.endPanelOp("Done.");
	}
      }
	  
      ShowHistoryTask task = new ShowHistoryTask(samples);
      SwingUtilities.invokeLater(task);
    }

    private TreeSet<String>  pHostnames;
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
     TreeMap<String,ResourceSampleBlock> samples
    ) 
    {
      super("JQueueJobBrowserPanel:ShowHistoryTask");

      pSamples = samples;
    }
    
    public void
    run()
    {
      UIMaster.getInstance().showResourceUsageHistoryDialog(pSamples);
    }

    private TreeMap<String,ResourceSampleBlock> pSamples;
  }


  /** 
   * Edit the properties of existing hosts.
   */ 
  private
  class EditHostsTask
    extends Thread
  {
    public 
    EditHostsTask() 
    {
      super("JQueueJobBrowserPanel:EditHostsTask");

      pStatus       = pHostsTableModel.getHostStatus();
      pReservations = pHostsTableModel.getHostReservations();
      pOrders       = pHostsTableModel.getHostOrders();
      pSlots   	    = pHostsTableModel.getHostSlots(); 

      pSelectionGroups    = pHostsTableModel.getHostSelectionGroups();
      pSelectionSchedules = pHostsTableModel.getHostSelectionSchedules();
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();

      if(master.beginPanelOp("Modifying Servers...")) {
	try {
	  QueueMgrClient client = master.getQueueMgrClient();
	  client.editHosts(pStatus, pReservations, pOrders, pSlots, 
			   pSelectionGroups, pSelectionSchedules);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	}
	finally {
	  master.endPanelOp("Done.");
	}
      }
	
      QueryTask task = new QueryTask();
      task.start();
    }

    private TreeMap<String,QueueHostStatusChange>  pStatus; 
    private TreeMap<String,String>                 pReservations; 
    private TreeMap<String,Integer>                pOrders;  
    private TreeMap<String,Integer>                pSlots;  
    private TreeMap<String,String>                 pSelectionGroups; 
    private TreeMap<String,String>                 pSelectionSchedules; 
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
      super("JQueueJobBrowserPanel:AddHostTask");

      pHostname = hostname;
    }
 
    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp("Adding Server...")) {
	try {
	  QueueMgrClient client = master.getQueueMgrClient();
	  client.addHost(pHostname);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	}
	finally {
	  master.endPanelOp("Done.");
	}
      }
      
      QueryTask task = new QueryTask();
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
      super("JQueueJobBrowserPanel:RemoveHostsTask");

      pHostnames = hostnames;
    }
 
    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp("Removing Servers...")) {
	try {
	  QueueMgrClient client = master.getQueueMgrClient();
	  client.removeHosts(pHostnames);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	}
	finally {
	  master.endPanelOp("Done.");
	}
      }
      
      QueryTask task = new QueryTask();
      task.start();
    }

    private TreeSet<String>  pHostnames; 
  }

 
  /*----------------------------------------------------------------------------------------*/

  /** 
   * View the given working file sequence with the given editor.
   */ 
  private
  class ViewTask
    extends Thread
  {
    public 
    ViewTask
    (
     NodeID nodeID, 
     FileSeq fseq
    ) 
    {
      this(nodeID, fseq, false, null, null, null);
    }

    public 
    ViewTask
    (
     NodeID nodeID, 
     FileSeq fseq, 
     boolean useDefault
    ) 
    {
      this(nodeID, fseq, useDefault, null, null, null);
    }

    public 
    ViewTask
    (
     NodeID nodeID, 
     FileSeq fseq,
     boolean useDefault,
     String ename,
     VersionID evid, 
     String evendor
    ) 
    {
      super("JQueueJobBrowserPanel:ViewTask");

      pNodeID        = nodeID;
      pFileSeq       = fseq; 
      pUseDefault    = useDefault;
      pEditorName    = ename;
      pEditorVersion = evid; 
      pEditorVendor  = evendor;       
    }

     public void 
     run() 
     {
       SubProcessLight proc = null;
       {
 	UIMaster master = UIMaster.getInstance();
 	if(master.beginPanelOp("Launching Node Editor...")) {
 	  try {
 	    MasterMgrClient client = master.getMasterMgrClient();

 	    NodeMod mod = client.getWorkingVersion(pNodeID);
 	    String author = pNodeID.getAuthor();
 	    String view = pNodeID.getView();

 	    /* create an editor plugin instance */ 
 	    BaseEditor editor = null;
 	    {
	      if(pEditorName != null) {
		PluginMgrClient pclient = PluginMgrClient.getInstance();
		editor = pclient.newEditor(pEditorName, pEditorVersion, pEditorVendor);
	      }
	      else if (pUseDefault) {
		FilePattern fpat = mod.getPrimarySequence().getFilePattern();
		String suffix = fpat.getSuffix();
		if(suffix != null) 
		  editor = client.getEditorForSuffix(suffix);
	      }

	      if(editor == null) 
		editor = mod.getEditor();
		
	      if(editor == null) 
		throw new PipelineException
		  ("No editor was specified for node (" + mod.getName() + ")!");
 	    }

 	    /* lookup the toolset environment */ 
 	    TreeMap<String,String> env = null;
 	    {
 	      String tname = mod.getToolset();
 	      if(tname == null) 
 		throw new PipelineException
 		  ("No toolset was specified for node (" + mod.getName() + ")!");

 	      /* passes author so that WORKING will correspond to the current view */ 
 	      env = client.getToolsetEnvironment(author, view, tname, PackageInfo.sOsType);
 	    }
	    
 	    /* get the primary file sequence */ 
 	    FileSeq fseq = null;
 	    File dir = null; 
 	    {
 	      Path path = null;
 	      {
		Path wpath = new Path(PackageInfo.sWorkPath, 
				      author + "/" + view + "/" + mod.getName());
		path = wpath.getParentPath();
 	      }

 	      fseq = new FileSeq(path.toString(), pFileSeq);
 	      dir = path.toFile();
 	    }
	    
 	    /* start the editor */ 
	    editor.makeWorkingDirs(dir);
 	    proc = editor.launch(fseq, env, dir);
 	  }
 	  catch(PipelineException ex) {
 	    master.showErrorDialog(ex);
 	    return;
 	  }
 	  finally {
 	    master.endPanelOp("Done.");
 	  }
 	}

 	/* wait for the editor to exit */ 
 	if(proc != null) {
 	  try {
 	    proc.join();
 	    if(!proc.wasSuccessful()) 
 	      master.showSubprocessFailureDialog("Editor Failure:", proc);
 	  }
 	  catch(InterruptedException ex) {
 	    master.showErrorDialog(ex);
 	  }
 	}
       }
     }

     private NodeID     pNodeID;
     private FileSeq    pFileSeq;
     private boolean    pUseDefault; 
     private String     pEditorName;
     private VersionID  pEditorVersion; 
     private String     pEditorVendor; 
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
      if(master.beginPanelOp()) {
	try {
	  for(NodeID nodeID : pTargets.keySet()) {
	    master.updatePanelOp("Resubmitting Jobs to the Queue: " + nodeID.getName());
	    master.getMasterMgrClient().resubmitJobs(nodeID, pTargets.get(nodeID), 
						     pBatchSize, pPriority, pRampUp, 
						     pSelectionKeys);
	  }
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp("Done.");
	}

	updateAll();
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
      if(master.beginPanelOp("Pausing Jobs...")) {
	try {
	  for(String author : pJobs.keySet()) {
	    TreeSet<Long> jobIDs = pJobs.get(author);
	    master.getQueueMgrClient().pauseJobs(author, jobIDs);
	  }
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp("Done.");
	}

	updateAll();
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
      if(master.beginPanelOp("Resuming Paused Jobs...")) {
	try {
	  for(String author : pJobs.keySet()) {
	    TreeSet<Long> jobIDs = pJobs.get(author);
	    master.getQueueMgrClient().resumeJobs(author, jobIDs);
	  }
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp("Done.");
	}

	updateAll();
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
      if(master.beginPanelOp("Preempting Jobs...")) {
	try {
	  for(String author : pJobs.keySet()) {
	    TreeSet<Long> jobIDs = pJobs.get(author);
	    master.getQueueMgrClient().preemptJobs(author, jobIDs);
	  }
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp("Done.");
	}

	updateAll();
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
      if(master.beginPanelOp("Killing Jobs...")) {
	try {
	  for(String author : pJobs.keySet()) {
	    TreeSet<Long> jobIDs = pJobs.get(author);
	    master.getQueueMgrClient().killJobs(author, jobIDs);
	  }
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp("Done.");
	}

	updateAll();
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
      if(master.beginPanelOp("Deleting Job Groups...")) {
	try {
	  master.getQueueMgrClient().deleteJobGroups(pGroups);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp("Done.");
	}

	updateAll();
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
      if(master.beginPanelOp("Deleting Job Groups...")) {
	try {
	  MasterMgrClient mclient = master.getMasterMgrClient();
	  QueueMgrClient qclient  = master.getQueueMgrClient();
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
	  master.endPanelOp("Done.");
	}

	updateAll();
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
   * The toolset used to build the editor menus.
   */ 
  private String  pGroupsEditorMenuToolset;
  private String  pSlotsEditorMenuToolset;

  /**
   * The working area view filter.
   */
  private ViewFilter  pViewFilter; 


  /*----------------------------------------------------------------------------------------*/

  /** 
   * The tabbed panel.
   */ 
  private JTabbedPanel  pTab;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The hosts popup menu.
   */ 
  private JPopupMenu  pHostsPopup;
  
  /**
   * The hosts popup menu items.
   */ 
  private JMenuItem  pHostsUpdateItem;
  private JMenuItem  pHostsHistoryItem;
  private JMenuItem  pHostsApplyItem;
  private JMenuItem  pHostsAddItem;
  private JMenuItem  pHostsRemoveItem;


  /**
   * The header panel.
   */ 
  private JPanel pHostsHeaderPanel; 

  /** 
   * Column display buttons.
   */ 
  private JToggleButton  pStatButton;
  private JToggleButton  pDynButton;
  private JToggleButton  pJobButton;
  private JToggleButton  pDspButton;

  /**
   * The button used to apply changes to the host properties.
   */ 
  private JButton  pHostsApplyButton; 


  /**
   * The job server names table model.
   */ 
  private QueueHostnamesTableModel  pHostnamesTableModel;

  /**
   * The job server names table panel.
   */ 
  private JTablePanel  pHostnamesTablePanel;


  /**
   * The job servers table model.
   */ 
  private QueueHostsTableModel  pHostsTableModel;

  /**
   * The job servers table panel.
   */ 
  private JTablePanel  pHostsTablePanel;


  /**
   * The container of the header buttons for the selection key columns.
   */ 
  private Box  pSelectionKeyHeaderBox; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The slots popup menu.
   */ 
  private JPopupMenu  pSlotsPopup;
  
  /**
   * The slots popup menu items.
   */ 
  private JMenuItem  pSlotsUpdateItem;
  private JMenuItem  pSlotsViewItem;
  private JMenuItem  pSlotsViewWithDefaultItem;
  private JMenuItem  pSlotsPreemptItem;
  private JMenuItem  pSlotsKillItem;

  /**
   * The view with submenu.
   */ 
  private JMenu  pSlotsViewWithMenu; 


  /**
   * The header panel.
   */ 
  private JPanel pSlotsHeaderPanel; 

  /**
   * The job slots table model.
   */ 
  private QueueSlotsTableModel  pSlotsTableModel;

  /**
   * The job slots table panel.
   */ 
  private JTablePanel  pSlotsTablePanel;

  /**
   * The list selection listener.
   */ 
  private SlotsListSelector  pSlotsListSelector;


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


  /*----------------------------------------------------------------------------------------*/

  /**
   * The canonical names of this host.
   */ 
  private TreeSet<String>  pLocalHostnames;


}
