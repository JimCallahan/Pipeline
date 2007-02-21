// $Id: JQueueJobServersPanel.java,v 1.5 2007/02/21 00:58:38 jim Exp $

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
/*   Q U E U E   J O B   S E R V E R S   P A N E L                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A panel which displays the job groups, servers and slots.
 */ 
public 
class JQueueJobServersPanel
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
  JQueueJobServersPanel()
  {
    super();
    initUI();
  }

  /**
   * Construct a new panel with a working area view identical to the given panel.
   */
  public 
  JQueueJobServersPanel
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
      
      updateMenuToolTips();  
    }

    
    /* initialize the panel components */ 
    {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));  

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
	  JLabel label = new JLabel("Queue Servers:");
	  label.setName("DialogHeaderLabel");	
          pHeaderLabel = label;
	  
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
	  
	add(panel);
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
	
	add(panel);
      }

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

    updateJobs(false, null, null, null, null, null, null);
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
    return "Queue Servers";
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

    PanelGroup<JQueueJobServersPanel> panels = master.getQueueJobServersPanels();

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
    PanelGroup<JQueueJobServersPanel> panels = 
      UIMaster.getInstance().getQueueJobServersPanels();
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
   * Get the intervals of time not currently included in the resource samples cache. 
   */ 
  public TreeMap<String,DateInterval>
  getSampleIntervals() 
  {
    if(pHostsTablePanel != null)
      return pHostsTableModel.getSampleIntervals(); 
    return new TreeMap<String,DateInterval>();
  }

    

  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Update all panels which share the current update channel.
   */ 
  private void 
  updatePanels() 
  {
    PanelUpdater pu = new PanelUpdater(this);
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
   * @param filtered
   *   Whether the hosts displayed where filtered by the Queue Stats panel.
   * 
   * @param hosts
   *   The job server hosts indexed by fully resolved hostname.
   * 
   * @param samples
   *   The latest resource samples indexed by fully resolved hostname.
   * 
   * @param workGroups
   *   The names of the user work groups.
   * 
   * @param workUsers
   *   The names of the users with working areas and/or special privileges.
   * 
   * @param selectionGroups
   *   The valid selection group names. 
   * 
   * @param selectionSchedules
   *   The valid selection schedule names. 
   */ 
  public synchronized void 
  applyPanelUpdates
  (
   String author, 
   String view, 
   boolean filtered, 
   TreeMap<String,QueueHostInfo> hosts, 
   TreeMap<String,ResourceSampleCache> samples, 
   Set<String> workGroups, 
   Set<String> workUsers,
   TreeSet<String> selectionGroups, 
   TreeSet<String> selectionSchedules
  )
  {
    if(!pAuthor.equals(author) || !pView.equals(view)) 
      super.setAuthorView(author, view);    

    updateJobs(filtered, hosts, samples, 
               workGroups, workUsers, selectionGroups, selectionSchedules);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the jobs groups, servers and slots asynchronously.
   * 
   * @param filtered
   *   Whether the hosts displayed where filtered by the Queue Stats panel.
   * 
   * @param hosts
   *   The job server hosts indexex by fully resolved hostnames.
   * 
   * @param samples
   *   The latest resource samples indexed by fully resolved hostname.
   * 
   * @param workGroups
   *   The names of the user work groups.
   * 
   * @param workUsers
   *   The names of the work group members.
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
   boolean filtered, 
   TreeMap<String,QueueHostInfo> hosts, 
   TreeMap<String,ResourceSampleCache>  samples, 
   Set<String> workGroups, 
   Set<String> workUsers,
   TreeSet<String> selectionGroups, 
   TreeSet<String> selectionSchedules
  ) 
  {
    updatePrivileges();

    /* job server panel */ 
    if((hosts != null) && (selectionGroups != null) && (selectionSchedules != null)) {
      pHeaderLabel.setText("Queue Servers:" + 
                           (filtered ? ("  ( " + hosts.size() + " matched )") : ""));  

      pHostnamesTableModel.setHostnames(hosts.keySet());

      pHostsTableModel.setQueueHosts
	(hosts, samples, workGroups, workUsers, selectionGroups, selectionSchedules, 
	 pPrivilegeDetails);

      updateHostsHeaderButtons();
      pHostsTablePanel.tableStructureChanged();  
      pHostsTablePanel.adjustmentValueChanged(null);

      pHostsApplyItem.setEnabled(false);
      pHostsApplyButton.setEnabled(false);
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
    if(pHostsHeaderPanel.getMousePosition(true) != null) {
      pHostsHeaderPanel.requestFocusInWindow();
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
      (pHostsUpdateItem, prefs.getUpdate(),
       "Update the job servers.");
    updateMenuToolTip
      (pHostsHistoryItem, prefs.getJobServersHistory(),
       "Show the resource usage history for the selected servers.");
    updateMenuToolTip
      (pHostsApplyItem, prefs.getApplyChanges(),
       "Apply the changes to job server properties.");
    updateMenuToolTip
      (pHostsAddItem, prefs.getJobServersAdd(),
       "Add a new job server.");
    updateMenuToolTip
      (pHostsRemoveItem, prefs.getJobServersRemove(),
       "Remove the selected job servers.");
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
	  updateHostsMenu();
	  pHostsPopup.show(e.getComponent(), e.getX(), e.getY());
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
      if((prefs.getJobServersHistory() != null) &&
	 prefs.getJobServersHistory().wasPressed(e))
	doHostsHistory();
      else if((prefs.getApplyChanges() != null) &&
	      prefs.getApplyChanges().wasPressed(e))
	doHostsApply();
      else if((prefs.getJobServersAdd() != null) &&
	      prefs.getJobServersAdd().wasPressed(e))
	doHostsAdd();
      else if((prefs.getJobServersRemove() != null) &&
	      prefs.getJobServersRemove().wasPressed(e))
	doHostsRemove();
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

    UIMaster master = UIMaster.getInstance();
    master.showResourceUsageHistoryDialog(pGroupID, getSelectedHostnames());
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
      super("JQueueJobServersPanel:EditHostsTask");
      
      pChanges = pHostsTableModel.getHostChanges();
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();

      if(master.beginPanelOp(pGroupID, "Modifying Servers...")) {
	try {
	  QueueMgrClient client = master.getQueueMgrClient(pGroupID);
	  client.editHosts(pChanges); 
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	}
	finally {
	  master.endPanelOp(pGroupID, "Done.");
	}
      }
	
      updatePanels();
    }

    private TreeMap<String,QueueHostMod>  pChanges; 
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
      super("JQueueJobServersPanel:AddHostTask");

      pHostname = hostname;
    }
 
    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Adding Server...")) {
	try {
	  QueueMgrClient client = master.getQueueMgrClient(pGroupID);
	  client.addHost(pHostname);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	}
	finally {
	  master.endPanelOp(pGroupID, "Done.");
	}
      }
      
      updatePanels();
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
      super("JQueueJobServersPanel:RemoveHostsTask");

      pHostnames = hostnames;
    }
 
    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Removing Servers...")) {
	try {
	  QueueMgrClient client = master.getQueueMgrClient(pGroupID);
	  client.removeHosts(pHostnames);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	}
	finally {
	  master.endPanelOp(pGroupID, "Done.");
	}
      }
      
      updatePanels();
    }

    private TreeSet<String>  pHostnames; 
  }

 

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
   private static final long serialVersionUID = 6463217527581557966L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The canonical names of this host.
   */ 
  private TreeSet<String>  pLocalHostnames;


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
   * The panel title.
   */
  private JLabel  pHeaderLabel;

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


}
