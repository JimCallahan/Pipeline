// $Id: JQueueJobBrowserPanel.java,v 1.14 2004/12/07 04:55:17 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.glue.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.net.*;
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

      /* the canonical names of this host */ 
      pLocalHostnames = new TreeSet<String>();
      try {
	Enumeration nets = NetworkInterface.getNetworkInterfaces();  
	while(nets.hasMoreElements()) {
	  NetworkInterface net = (NetworkInterface) nets.nextElement();
	  Enumeration addrs = net.getInetAddresses();
	  while(addrs.hasMoreElements()) {
	    InetAddress addr = (InetAddress) addrs.nextElement();
	    String ip = addr.getHostAddress();
	    if(!ip.equals("127.0.0.1")) 
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
	item.setActionCommand("update");
	item.addActionListener(this);
	pSlotsPopup.add(item);

	pSlotsPopup.addSeparator();

	item = new JMenuItem("Kill Jobs");
	pSlotsKillItem = item;
	item.setActionCommand("slots-kill-jobs");
	item.addActionListener(this);
	pSlotsPopup.add(item);
      }

      {	
	pGroupsPopup = new JPopupMenu();

	item = new JMenuItem("Update");
	item.setActionCommand("update");
	item.addActionListener(this);
	pGroupsPopup.add(item);

	item = new JMenuItem("Show This View");
	pGroupsFilterViewsItem = item;
	item.setActionCommand("toggle-filter-views");
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
	    Box vbox = new Box(BoxLayout.Y_AXIS);
	    vbox.setAlignmentX(0.5f);

	    {
	      QueueHostnamesTableModel model = new QueueHostnamesTableModel(this);
	      pHostnamesTableModel = model;
	      
	      JTablePanel tpanel =
		new JTablePanel(model, model.getColumnWidths(), 
				model.getRenderers(), model.getEditors(), 
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

	      vbox.add(tpanel);
	    }

	    vbox.add(Box.createRigidArea(new Dimension(0, 14)));

	    vbox.setMinimumSize(new Dimension(206, 30));
	    vbox.setMaximumSize(new Dimension(206, Integer.MAX_VALUE));

	    panel.add(vbox);
	  }

	  panel.add(Box.createRigidArea(new Dimension(2, 0)));

	  {	    
	    QueueHostsTableModel model = new QueueHostsTableModel(this, pLocalHostnames);
	    pHostsTableModel = model;

	    JTablePanel tpanel =
	      new JTablePanel(model, model.getColumnWidths(), 
			      model.getRenderers(), model.getEditors());
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
	    
	    panel.add(tpanel);
	    
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
		  if((wk > 1) && (wk < 5)) 
		    prefix = "Blue";
		  else if((wk > 4) && (wk < 7)) 
		    prefix = "Green"; 
		  
		  JButton btn = new JButton(pHostsTableModel.getColumnName(wk));
		  btn.setName(prefix + "TableHeaderButton");
		  
		  {	    
		    Dimension size = new Dimension(width[wk], 23);
		    btn.setMinimumSize(size);
		    btn.setPreferredSize(size);
		    btn.setMaximumSize(size);
		  }
		  
		  btn.addActionListener(tpanel);
		  btn.setActionCommand("sort-column:" + wk);	  
		  
		  btn.setFocusable(false);
		  
		  btn.setToolTipText
		    (UIMaster.formatToolTip(pHostsTableModel.getColumnDescription(wk)));
		  
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
	    
	    JTablePanel tpanel =
	      new JTablePanel(model, model.getColumnWidths(), 
			      model.getRenderers(), model.getEditors());
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
	    JToggleButton btn = new JToggleButton();		
	    pFilterViewsButton = btn;
	    btn.setName("ViewsButton");
	    
	    btn.setSelected(true);
	    
	    Dimension size = new Dimension(19, 19);
	    btn.setMinimumSize(size);
	    btn.setMaximumSize(size);
	    btn.setPreferredSize(size);
	    
	    btn.setActionCommand("update");
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
	    
	    JTablePanel tpanel =
	      new JTablePanel(model, model.getColumnWidths(), 
			      model.getRenderers(), model.getEditors());
	    pGroupsTablePanel = tpanel;
	    
	    pGroupsListSelector = new GroupsListSelector();
	    ListSelectionModel smodel = tpanel.getTable().getSelectionModel();
	    smodel.addListSelectionListener(pGroupsListSelector);
	    
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
 
    updateJobs(null, null, null, null, null);
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
      Long jobID = (Long) pSlotsTableModel.getValueAt(rows[wk], 1);
      if(jobID != null) 
	jobIDs.add(jobID);
    }

    return jobIDs;
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
      Long groupID = (Long) pGroupsTableModel.getValueAt(rows[wk], 0);
      groupIDs.add(groupID);
    }

    return groupIDs;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Update the jobs groups, servers and slots. 
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
   * @param keys
   *   The valid selection key descriptions indexed by key name.
   */ 
  public synchronized void
  updateJobs
  (
   String author, 
   String view, 
   TreeMap<Long,QueueJobGroup> groups, 
   TreeMap<Long,JobStatus> jobStatus, 
   TreeMap<Long,QueueJobInfo> jobInfo, 
   TreeMap<String,QueueHost> hosts, 
   TreeMap<String,String> keys
  ) 
  {
    if(!pAuthor.equals(author) || !pView.equals(view)) 
      super.setAuthorView(author, view);

    updateJobs(groups, jobStatus, jobInfo, hosts, keys);
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
   * @param keys
   *   The valid selection key descriptions indexed by key name.
   */ 
  public synchronized void
  updateJobs
  (
   TreeMap<Long,QueueJobGroup> groups, 
   TreeMap<Long,JobStatus> jobStatus, 
   TreeMap<Long,QueueJobInfo> jobInfo, 
   TreeMap<String,QueueHost> hosts, 
   TreeMap<String,String> keys
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
    if((hosts != null) && (keys != null)) {
      pHostnamesTableModel.setHostnames(hosts.keySet());

      pHostsTableModel.setQueueHosts(hosts, keys, pIsPrivileged);
      pHostsTableModel.fireTableStructureChanged(); 

      {
	TableColumnModel cmodel = pHostsTablePanel.getTable().getColumnModel();
	
	int wk;
	for(wk=0; wk<7; wk++) {
	  TableColumn tcol = cmodel.getColumn(wk);
	  
	  tcol.setCellRenderer(pHostsTableModel.getRenderers()[wk]);
	  
	  TableCellEditor editor = pHostsTableModel.getEditors()[wk];
	  if(editor != null) 
	    tcol.setCellEditor(editor);
	  
	  int width = pHostsTableModel.getColumnWidths()[wk];
	  tcol.setMinWidth(width);
	  tcol.setPreferredWidth(width);
	  tcol.setMaxWidth(width);
	}
	  
	wk = 7;
	for(String kname : keys.keySet()) {
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
	
	int wk = 7;
	for(String kname : keys.keySet()) {
	  JButton btn = new JButton(kname);
	  btn.setName("PurpleTableHeaderButton");
	  
	  {	    
	    Dimension size = new Dimension(100, 23);
	    btn.setMinimumSize(size);
	    btn.setPreferredSize(size);
	    btn.setMaximumSize(size);
	  }
	  
	  btn.addActionListener(pHostsTablePanel);
	  btn.setActionCommand("sort-column:" + wk);	  
	  
	  btn.setFocusable(false);
	  
	  btn.setToolTipText
	    (UIMaster.formatToolTip(pHostsTableModel.getColumnDescription(wk)));

	  pSelectionKeyHeaderBox.add(btn);
	  
	  wk++;
	}
	
	Box parent = (Box) pSelectionKeyHeaderBox.getParent();
	parent.revalidate();
	parent.repaint();
      }
      
      pHostsApplyItem.setEnabled(false);
      pHostsApplyButton.setEnabled(false);
    }

    /* job slots panel */ 
    if((hosts != null) && (jobInfo != null)) {
      ListSelectionModel smodel = pSlotsTablePanel.getTable().getSelectionModel();
      smodel.removeListSelectionListener(pSlotsListSelector);
      {
	pSlotsTableModel.setSlots(hosts, pJobStatus, jobInfo, pIsPrivileged);
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
	panel.updateQueueJobs(pAuthor, pView, sgroups, pJobStatus, pIsPrivileged);
	panel.updateManagerTitlePanel();
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
    pHostsAddItem.setEnabled(pIsPrivileged); 
    pHostsRemoveItem.setEnabled(pIsPrivileged && selected); 
  }

  /**
   * Update the job server slots menu.
   */ 
  public void 
  updateSlotsMenu() 
  {
    boolean selected = (pSlotsTablePanel.getTable().getSelectedRowCount() > 0);
    pSlotsKillItem.setEnabled(selected);
  }

  /**
   * Update the job groups menu.
   */ 
  public void 
  updateGroupsMenu() 
  {
    pGroupsFilterViewsItem.setText
      (pFilterViewsButton.isSelected() ? "Show All Views" : "Show This View");
    
    boolean selected = (pGroupsTablePanel.getTable().getSelectedRowCount() > 0);
    pGroupsQueueItem.setEnabled(selected); 
    pGroupsQueueSpecialItem.setEnabled(selected); 
    pGroupsPauseItem.setEnabled(selected); 
    pGroupsResumeItem.setEnabled(selected);
    pGroupsKillItem.setEnabled(selected);
    pGroupsDeleteItem.setEnabled(selected);

    pGroupsDeleteCompletedItem.setEnabled
      (pFilterViewsButton.isSelected() ? !pIsLocked : pIsPrivileged);
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
    if((prefs.getJobBrowserUpdate() != null) &&
       prefs.getJobBrowserUpdate().wasPressed(e))
      doUpdate();
    else {
      switch(pTab.getSelectedIndex()) {      
      case 0:
	if((prefs.getJobBrowserHostsHistory() != null) &&
	   prefs.getJobBrowserHostsHistory().wasPressed(e))
	  doHostsHistory();
	else if((prefs.getJobBrowserHostsApply() != null) &&
		prefs.getJobBrowserHostsApply().wasPressed(e))
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
	if((prefs.getJobBrowserSlotsKillJobs() != null) &&
	   prefs.getJobBrowserSlotsKillJobs().wasPressed(e))
	  doSlotsKillJobs();
	else 
	  unsupported = true;
	break;

      case 2:
	if((prefs.getJobBrowserToggleFilterViews() != null) &&
	   prefs.getJobBrowserToggleFilterViews().wasPressed(e))
	  doToggleFilterViews();
	else if((prefs.getJobBrowserGroupsQueueJobs() != null) &&
		prefs.getJobBrowserGroupsQueueJobs().wasPressed(e))
	  doGroupsQueueJobs();
	else if((prefs.getJobBrowserGroupsQueueJobsSpecial() != null) &&
		prefs.getJobBrowserGroupsQueueJobsSpecial().wasPressed(e))
	  doGroupsQueueJobsSpecial();
	else if((prefs.getJobBrowserGroupsPauseJobs() != null) &&
		prefs.getJobBrowserGroupsPauseJobs().wasPressed(e))
	  doGroupsPauseJobs();
	else if((prefs.getJobBrowserGroupsResumeJobs() != null) &&
		prefs.getJobBrowserGroupsResumeJobs().wasPressed(e))
	  doGroupsResumeJobs();
	else if((prefs.getJobBrowserGroupsKillJobs() != null) &&
		prefs.getJobBrowserGroupsKillJobs().wasPressed(e))
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
      doUpdate();

    else if(cmd.equals("hosts-history")) 
      doHostsHistory();
    else if(cmd.equals("hosts-apply")) 
      doHostsApply();
    else if(cmd.equals("hosts-add")) 
      doHostsAdd();
    else if(cmd.equals("hosts-remove")) 
      doHostsRemove();

    else if(cmd.equals("slots-kill-jobs")) 
      doSlotsKillJobs();

    else if(cmd.equals("toggle-filter-views")) 
      doToggleFilterViews();

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
   * Update the status of all jobs groups, servers and slots. 
   */ 
  public void
  doUpdate()
  { 
    /* recheck privileged status */ 
    {
      pIsPrivileged = false; 
      UIMaster master = UIMaster.getInstance();
      try {
	pIsPrivileged = master.getMasterMgrClient().isPrivileged();
      }
      catch(PipelineException ex) {
	master.showErrorDialog(ex);
      }
    }

    /* enable/disable the DeleteCompleted button */ 
    pDeleteCompletedButton.setEnabled
      (pFilterViewsButton.isSelected() ? !pIsLocked : pIsPrivileged);
    
    /* update the panels */ 
    QueryTask task = new QueryTask();
    task.start();
  }


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

    TreeSet<String> hostnames = getSelectedHostnames();
    for(String hname : hostnames) {
      GetHistoryTask task = new GetHistoryTask(hname);
      task.start();      
    }
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

    JNewJobServerDialog diag = new JNewJobServerDialog();
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
      
      if((author != null) && (pIsPrivileged || author.equals(PackageInfo.sUser))) {
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
   * Toggle the filter views state.
   */ 
  public void 
  doToggleFilterViews() 
  {
    pFilterViewsButton.setSelected(!pFilterViewsButton.isSelected());
    doUpdate();
  }
  
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
      
	  if((targetID != null) && 
	     (pIsPrivileged || targetID.getAuthor().equals(PackageInfo.sUser))) {
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
	      author = status.getNodeID().getAuthor();
	    }
	  }
      
	  if((author != null) && (pIsPrivileged || author.equals(PackageInfo.sUser))) {
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
      
	  if((author != null) && (pIsPrivileged || author.equals(PackageInfo.sUser))) {
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
	    case Running:
	      author = status.getNodeID().getAuthor();
	    }
	  }
      
	  if((author != null) && (pIsPrivileged || author.equals(PackageInfo.sUser))) {
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
	if((author != null) && (pIsPrivileged || author.equals(PackageInfo.sUser))) 
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
    DeleteCompletedJobGroupsTask task =
      new DeleteCompletedJobGroupsTask(!pFilterViewsButton.isSelected());
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
	  Long groupID = (Long) pGroupsTableModel.getValueAt(rows[wk], 0);
	  pSelectedIDs.add(groupID);
	}
      }

      pJustSelected = null;    
      if((pSelectedIDs.size() == 1) && (numPrevSelected <= 1))
	pJustSelected = pSelectedIDs.first();
  
      doUpdate();      
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
	    jobID = (Long) pSlotsTableModel.getValueAt(rows[wk], 1);
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
      pGroupID = groupID;
      pJobID   = jobID;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();      

      QueueJob     job  = null;
      QueueJobInfo info = null; 
      if((pGroupID > 0) && (pJobID != null)) {
	if(master.beginPanelOp("Updating Job Details...")) {
	  try {
	    QueueMgrClient client = master.getQueueMgrClient();
	    job  = client.getJob(pJobID);
	    info = client.getJobInfo(pJobID);
	  }
	  catch(PipelineException ex) {
	    master.showErrorDialog(ex);
	  }
	  finally {
	    master.endPanelOp("Done.");
	  }
	}
      }
	
      UpdateSlotsSelectionTask task = new UpdateSlotsSelectionTask(pGroupID, job, info);
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
     QueueJobInfo info
    ) 
    {
      super("JQueueJobBrowserPanel:UpdateSlotsSelectionTask");

      pGroupID = groupID;
      pJob     = job; 
      pJobInfo = info; 
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
	  panel.updateJob(pAuthor, pView, pJob, pJobInfo);
	  panel.updateManagerTitlePanel();
	}
      }
    }

    private int           pGroupID;
    private QueueJob      pJob; 
    private QueueJobInfo  pJobInfo; 
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
      TreeMap<String,QueueHost> hosts = null;
      TreeMap<String,String> keys = null;

      if(master.beginPanelOp("Updating...")) {
	try {
	  QueueMgrClient client = master.getQueueMgrClient();

	  groups    = client.getJobGroups(); 
	  jobStatus = client.getJobStatus(new TreeSet<Long>(groups.keySet()));
	  jobInfo   = client.getRunningJobInfo();
	  hosts     = client.getHosts(); 

	  keys = new TreeMap<String,String>();
	  for(SelectionKey key : client.getSelectionKeys()) 
	    keys.put(key.getName(), key.getDescription());
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	}
	finally {
	  master.endPanelOp("Done.");
	}
      }
	
      UpdateTask task = new UpdateTask(groups, jobStatus, jobInfo, hosts, keys);
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
     TreeMap<String,QueueHost> hosts, 
     TreeMap<String,String> keys
    ) 
    {
      super("JQueueJobBrowserPanel:UpdateTask");

      pGroups = groups;
      pStatus = jobStatus; 
      pInfo   = jobInfo;
      pHosts  = hosts; 
      pKeys   = keys;
    }

    public void 
    run() 
    {
      updateJobs(pGroups, pStatus, pInfo, pHosts, pKeys);
    }
    
    private TreeMap<Long,QueueJobGroup>  pGroups; 
    private TreeMap<Long,JobStatus>      pStatus; 
    private TreeMap<Long,QueueJobInfo>   pInfo; 
    private TreeMap<String,QueueHost>    pHosts;
    private TreeMap<String,String>       pKeys;
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
     String hostname
    ) 
    {
      pHostname = hostname; 
    }
    
    public void
    run()
    {
      UIMaster master = UIMaster.getInstance();

      ResourceSampleBlock block = null;
      if(master.beginPanelOp("Server History...")) {
	try {
	  QueueMgrClient client = master.getQueueMgrClient();
	  block = client.getHostResourceSamples(pHostname);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	}
	finally {
	  master.endPanelOp("Done.");
	}
      }
	  
      if(block != null) {
	ShowHistoryTask task = new ShowHistoryTask(pHostname, block);
	SwingUtilities.invokeLater(task);
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
      pSlots   	    = pHostsTableModel.getHostSlots(); 
      pBiases       = pHostsTableModel.getHostBiases(); 
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();

      if(master.beginPanelOp("Modifying Servers...")) {
	try {
	  QueueMgrClient client = master.getQueueMgrClient();
	  client.editHosts(pStatus, pReservations, pSlots, pBiases); 
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

    private TreeMap<String,QueueHost.Status>         pStatus; 
    private TreeMap<String,String>                   pReservations; 
    private TreeMap<String,Integer>                  pSlots;  
    private TreeMap<String,TreeMap<String,Integer>>  pBiases;       
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

	doUpdate();
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

	doUpdate();
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

	doUpdate();
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

	doUpdate();
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

	doUpdate();
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
     boolean allViews
    ) 
    {
      super("JQueueJobsBrowserPanel:DeleteCompletedJobGroupsTask");

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

  /**
   * Does the current user have privileged status?
   */ 
  private boolean  pIsPrivileged;


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
  private JMenuItem  pHostsHistoryItem;
  private JMenuItem  pHostsApplyItem;
  private JMenuItem  pHostsAddItem;
  private JMenuItem  pHostsRemoveItem;


  /**
   * The header panel.
   */ 
  private JPanel pHostsHeaderPanel; 

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
  private JMenuItem  pSlotsKillItem;


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
  private JMenuItem  pGroupsFilterViewsItem; 
  private JMenuItem  pGroupsQueueItem; 
  private JMenuItem  pGroupsQueueSpecialItem; 
  private JMenuItem  pGroupsPauseItem; 
  private JMenuItem  pGroupsResumeItem; 
  private JMenuItem  pGroupsKillItem;
  private JMenuItem  pGroupsDeleteItem;
  private JMenuItem  pGroupsDeleteCompletedItem;


  /**
   * The header panel.
   */ 
  private JPanel pGroupsHeaderPanel; 

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
  private QueueJobGroupsTableModel  pGroupsTableModel;

  /**
   * The job groups table panel.
   */ 
  private JTablePanel  pGroupsTablePanel;

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
