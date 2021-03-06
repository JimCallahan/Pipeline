// $Id: JQueueJobServersPanel.java,v 1.21 2009/09/16 03:54:40 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*; 

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.JTablePanel;
import us.temerity.pipeline.ui.UIFactory;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   J O B   S E R V E R S   P A N E L                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A panel which displays the job groups, servers and slots.
 */ 
public 
class JQueueJobServersPanel
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
      Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();  
      while(nets.hasMoreElements()) {
	NetworkInterface net = nets.nextElement();
	Enumeration<InetAddress> addrs = net.getInetAddresses();
	while(addrs.hasMoreElements()) {
	  InetAddress addr = addrs.nextElement();
	  if((addr instanceof Inet4Address) && !addr.isLoopbackAddress()) 
	    pLocalHostnames.add(addr.getCanonicalHostName().toLowerCase(Locale.ENGLISH));
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
	
	item = new JMenuItem("Notes");
	pHostsNotesItem = item;
	item.setActionCommand("hosts-notes");
	item.addActionListener(this);
	pHostsPopup.add(item);
	
	pHostsPopup.addSeparator();
	
	item = new JMenuItem("Apply Changes");
	pApplyItem = item;
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
	  JButton btn = new JButton();		
	  pApplyButton = btn;
	  btn.setName("ApplyHeaderButton");
          btn.setEnabled(false);
	    
	  Dimension size = new Dimension(30, 30);
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
          QueueHostsTableModel model = new QueueHostsTableModel(this, pLocalHostnames);
          pHostsTableModel = model;
  
	  JTablePanel tpanel = new JTablePanel(model);
	  pHostsTablePanel = tpanel;
	  
	  {
	    JScrollPane scroll = tpanel.getTableScroll();
	    
	    scroll.setHorizontalScrollBarPolicy
	      (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
	    
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

    updateJobs(false, null, null, null, null, null, null, null, null, null);
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
    return "Queue Servers";
  }
  
  /**
   * Get a matrix of all the values for all the {@link SelectionSchedule SelectionSchedules}
   * at the time of the last update.
   */
  public SelectionScheduleMatrix
  getSelectionScheduleMatrix()
  {
    return pMatrix;
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
  @Override
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
  public TreeMap<String,TimeInterval>
  getSampleIntervals() 
  {
    if(pHostsTablePanel != null)
      return pHostsTableModel.getSampleIntervals(); 
    return new TreeMap<String,TimeInterval>();
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
    if (pGroupID != 0) {
      PanelUpdater pu = new PanelUpdater(this);
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
   *  @param hardwareGroups
   *   The valid hardware group names.
   * 
   * @param selectionSchedules
   *   The valid selection schedule names. 
   *   
   * @param dispatchControls
   *   The valid dispatch control names.
   *   
   * @param matrix
   *   The matrix used to determine which schedule maps to what group.
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
   TreeSet<String> selectionSchedules,
   TreeSet<String> hardwareGroups,
   TreeSet<String> dispatchControls,
   TreeSet<String> userBalanceGroups,
   SelectionScheduleMatrix matrix
  )
  {
    if(!pAuthor.equals(author) || !pView.equals(view)) 
      super.setAuthorView(author, view);  
    
    pMatrix = matrix;

    updateJobs(filtered, hosts, samples, 
               workGroups, workUsers, selectionGroups, selectionSchedules, 
               hardwareGroups, dispatchControls, userBalanceGroups);
  }

  /**
   * Register the name of a panel property which has just been modified.
   */ 
  @Override
  public void
  unsavedChange
  (
   String name
  )
  {
    pApplyButton.setEnabled(true);
    pApplyItem.setEnabled(true);

    pApplyButton.setToolTipText(UIFactory.formatToolTip
      ("Apply the changes to server properties."));

    super.unsavedChange(name); 
  }

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
    
    if(pHostsTablePanel != null) 
      pHostsTablePanel.getTable().setEnabled(false);
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
    if(pHostsTablePanel != null) 
      pHostsTablePanel.getTable().setEnabled(true);
    
    pApplyButton.setEnabled(false);
    pApplyItem.setEnabled(false);

    pApplyButton.setToolTipText(UIFactory.formatToolTip
      ("There are no unsaved changes to Apply at this time.")); 

    super.postPanelOp(); 
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
   *   
   * @param hardwareGroups
   *   The valid hardware group names.
   *   
   * @param dispatchControls
   *   The valid dispatch control names.
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
    TreeSet<String> selectionSchedules,
    TreeSet<String> hardwareGroups,
    TreeSet<String> dispatchControls,
    TreeSet<String> userBalanceGroups
  ) 
  {
    updatePrivileges();

    /* job server panel */ 
    if((hosts != null) && (selectionGroups != null) && (selectionSchedules != null) && 
       (hardwareGroups != null ) && (dispatchControls != null)) {
      pHeaderLabel.setText("Queue Servers:" + 
                           (filtered ? ("  ( " + hosts.size() + " matched )") : ""));  

      TreeMap<String,String> names = new TreeMap<String,String>();
      UserPrefs prefs = UserPrefs.getInstance();
      for(QueueHostInfo qinfo : hosts.values()) {
        names.put(qinfo.getName(), 
                  prefs.getShowFullHostnames() ? qinfo.getName() : qinfo.getShortName());
      }

      pHostsTableModel.setQueueHosts(names, hosts, samples, workGroups, workUsers, 
                                     selectionGroups, selectionSchedules, hardwareGroups,
                                     dispatchControls, userBalanceGroups, pPrivilegeDetails);
      
      pHostsTablePanel.tableStructureChanged();  
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
    pHostsNotesItem.setEnabled(selected);
    pHostsAddItem.setEnabled(pPrivilegeDetails.isQueueAdmin()); 
    pHostsRemoveItem.setEnabled(pPrivilegeDetails.isQueueAdmin() && selected); 
  }


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
      (pHostsUpdateItem, prefs.getUpdate(),
       "Update the job servers.");
    updateMenuToolTip
      (pHostsHistoryItem, prefs.getJobServersHistory(),
       "Show the resource usage history for the selected servers.");
    updateMenuToolTip
      (pHostsNotesItem, prefs.getJobServersNotes(),
       "Show the operational notes for the selected servers.");
    updateMenuToolTip
      (pApplyItem, prefs.getApplyChanges(),
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
	  if(UIFactory.getBeepPreference())
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
	doApply();
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

    else if(cmd.equals("hosts-history")) 
      doHostsHistory();
    else if(cmd.equals("hosts-notes")) 
      doHostsNotes();
    else if(cmd.equals("hosts-apply")) 
      doApply();
    else if(cmd.equals("hosts-add")) 
      doHostsAdd();
    else if(cmd.equals("hosts-remove")) 
      doHostsRemove();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

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
   * Show the operational notes for the selected hosts.
   */ 
  public void   
  doHostsNotes()
  {
    pHostsTablePanel.cancelEditing();

    UIMaster master = UIMaster.getInstance();
    master.showServerNotesDialog(getSelectedHostnames());
  }

  /**
   * Apply the changes to server properties. 
   */ 
  @Override
  public void 
  doApply()
  {
    super.doApply();

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

    @Override
    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();

      if(master.beginPanelOp(pGroupID, "Modifying Servers...")) {
        QueueMgrClient client = master.acquireQueueMgrClient();
	try {
	  client.editHosts(pChanges); 
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	}
	finally {
	  master.releaseQueueMgrClient(client);
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
 
    @Override
    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Adding Server...")) {
        QueueMgrClient client = master.acquireQueueMgrClient();
	try {
	  client.addHost(pHostname);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	}
	finally {
	  master.releaseQueueMgrClient(client);
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
 
    @Override
    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Removing Servers...")) {
        QueueMgrClient client = master.acquireQueueMgrClient();
	try {
	  client.removeHosts(pHostnames);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	}
	finally {
	  master.releaseQueueMgrClient(client);
	  master.endPanelOp(pGroupID, "Done.");
	}
      }
      
      updatePanels();
    }

    private TreeSet<String>  pHostnames; 
  }
  
  private enum
  LastSort
  {
    NAMES, DATA
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
  private JMenuItem  pHostsNotesItem;
  private JMenuItem  pApplyItem;
  private JMenuItem  pHostsAddItem;
  private JMenuItem  pHostsRemoveItem;


  /**
   * The header panel.
   */ 
  private JPanel  pHostsHeaderPanel; 

  /**
   * The panel title.
   */
  private JLabel  pHeaderLabel;

  /**
   * The button used to apply changes to the host properties.
   */ 
  private JButton  pApplyButton; 


  /**
   * The job servers table model.
   */ 
  private QueueHostsTableModel  pHostsTableModel;

  /**
   * The job servers table panel.
   */ 
  private JTablePanel  pHostsTablePanel;

  /**
   * The matrix representing all the selection schedule values at the time of the 
   * last update.
   */
  private SelectionScheduleMatrix  pMatrix;

  /**
   *  Keep track of which table was the last to be sorted, so the correct sort can be used 
   *  after an update.  
   */
  private LastSort  pLastSort;
}
