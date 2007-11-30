// $Id: JQueueJobSlotsPanel.java,v 1.13 2007/11/30 20:14:26 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.*;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.JTablePanel;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   J O B   S L O T S   P A N E L                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A panel which displays the job groups, servers and slots.
 */ 
public 
class JQueueJobSlotsPanel
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
  JQueueJobSlotsPanel()
  {
    super();
    initUI();
  }

  /**
   * Construct a new panel with a working area view identical to the given panel.
   */
  public 
  JQueueJobSlotsPanel
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
      pJobStatus = new TreeMap<Long,JobStatus>();

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

      updateMenuToolTips();  
    }

    
    /* initialize the panel components */ 
    {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));  

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
	  JLabel label = new JLabel("Queue Slots:");
	  label.setName("DialogHeaderLabel");	
	    
	  panel.add(label);	  
	}
	  
	panel.add(Box.createHorizontalGlue());

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
	  QueueSlotsTableModel model = new QueueSlotsTableModel(this, pLocalHostnames);
	  pSlotsTableModel = model;
	    
	  JTablePanel tpanel = new JTablePanel(model);
	  pSlotsTablePanel = tpanel;
	    
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

    updateJobs(null, null, null);
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
    return "Queue Slots";
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

    PanelGroup<JQueueJobSlotsPanel> panels = master.getQueueJobSlotsPanels();

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
    PanelGroup<JQueueJobSlotsPanel> panels = 
      UIMaster.getInstance().getQueueJobSlotsPanels();
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
      master.rebuildEditorMenu(pGroupID, toolset, pSlotsViewWithMenu, this);
      
      pSlotsEditorMenuToolset = toolset;
    }
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
    updatePanels(null); 
  }

  /**
   * Update all panels which share the current update channel.
   * 
   * @param selected
   *   The ID of the job last selected in the job slots panel. 
   */ 
  private void 
  updatePanels
  (
   Long selected
  ) 
  {
    PanelUpdater pu = new PanelUpdater(this, selected);
    pu.execute();
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
   * @param jobStatus
   *   The job status indexed by job ID.
   * 
   * @param jobInfo
   *   The information about the running jobs indexed by job ID.
   * 
   * @param hosts
   *   The job server hosts indexex by fully resolved hostnames.
   */
  public synchronized void 
  applyPanelUpdates
  (
   String author, 
   String view, 
   TreeMap<Long,JobStatus> jobStatus, 
   TreeMap<Long,QueueJobInfo> jobInfo, 
   TreeMap<String,QueueHostInfo> hosts
  )
  {
    if(!pAuthor.equals(author) || !pView.equals(view)) 
      super.setAuthorView(author, view);    

    updateJobs(jobStatus, jobInfo, hosts); 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Perform any operations needed before an panel operation starts. <P> 
   * 
   * This method is run by the Swing Event thread.
   */ 
  public void 
  prePanelOp() 
  {
    super.prePanelOp(); 
    
    if(pSlotsTablePanel != null) 
      pSlotsTablePanel.getTable().setEnabled(false);
  }

  /**
   * Perform any operations needed after an panel operation has completed. <P> 
   * 
   * This method is run by the Swing Event thread.
   */ 
  public void 
  postPanelOp() 
  {
    if(pSlotsTablePanel != null) 
      pSlotsTablePanel.getTable().setEnabled(true);
    
    super.postPanelOp(); 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the jobs slots. 
   * 
   * @param jobStatus
   *   The job status indexed by job ID.
   * 
   * @param jobInfo
   *   The information about the running jobs indexed by job ID.
   * 
   * @param hosts
   *   The job server hosts indexex by fully resolved hostnames.
   */ 
  public synchronized void
  updateJobs
  (
   TreeMap<Long,JobStatus> jobStatus, 
   TreeMap<Long,QueueJobInfo> jobInfo, 
   TreeMap<String,QueueHostInfo> hosts
  ) 
  {
    updatePrivileges();

    pJobStatus.clear();
    if(jobStatus != null) 
      pJobStatus.putAll(jobStatus);

    if((hosts != null) && (jobInfo != null)) 
      pSlotsTableModel.setSlots(hosts, pJobStatus, jobInfo);
  }



  /*----------------------------------------------------------------------------------------*/

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
      (pSlotsUpdateItem, prefs.getUpdate(),
       "Update the job slots.");
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
    case MouseEvent.BUTTON1:
      {
	int on1  = (MouseEvent.BUTTON1_DOWN_MASK);
	
	int off1 = (MouseEvent.BUTTON2_DOWN_MASK | 
		    MouseEvent.BUTTON3_DOWN_MASK | 
		    MouseEvent.SHIFT_DOWN_MASK |
		    MouseEvent.ALT_DOWN_MASK |
		    MouseEvent.CTRL_DOWN_MASK);

	/* BUTTON1 (double click): send job to details panels */ 
	if((e.getClickCount() == 2) && ((mods & (on1 | off1)) == on1))
	  doDetails(e.getPoint());
      }
      break;

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
	  updateSlotsMenu();
	  pSlotsPopup.show(e.getComponent(), e.getX(), e.getY());
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
    else if((prefs.getKillJobs() != null) &&
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
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the job details panels with the slot under the given position.
   */ 
  private void
  doDetails
  (
   Point point
  ) 
  {
    int srow = pSlotsTablePanel.getTable().rowAtPoint(point);
    if(srow != -1) {
      Long jobID = pSlotsTableModel.getJobID(srow);
      if(jobID != null) 
	updatePanels(jobID);
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Preempt the jobs running on the selected job server slots.
   */ 
  public void 
  doSlotsPreemptJobs()
  {
    TreeSet<Long> jobs = getSelectedSlotJobIDs(); 
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
    TreeSet<Long> jobs = getSelectedSlotJobIDs(); 
    if(!jobs.isEmpty()) {
      KillJobsTask task = new KillJobsTask(jobs);
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
      super("JQueueJobSlotsPanel:ViewTask");

      pNodeID        = nodeID;
      pFileSeq       = fseq; 
      pUseDefault    = useDefault;
      pEditorName    = ename;
      pEditorVersion = evid; 
      pEditorVendor  = evendor;       
    }

    @SuppressWarnings("deprecation")
    public void 
    run() 
    {
      MasterMgrClient client = null;
      SubProcessLight proc = null;
      Long editID = null;
      {
 	UIMaster master = UIMaster.getInstance();
        boolean ignoreExitCode = false;
 	if(master.beginPanelOp(pGroupID, "Launching Node Editor...")) {
 	  try {
	    client = master.getMasterMgrClient(pGroupID);

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
		  ("No Editor plugin was specified for node (" + mod.getName() + ")!");

              if(!editor.supports(PackageInfo.sOsType)) 
                throw new PipelineException
                  ("The Editor plugin (" + editor.getName() + " v" + 
                   editor.getVersionID() + ") from the vendor (" + editor.getVendor() + ") " +
                   "does not support the " + PackageInfo.sOsType.toTitle() + " operating " + 
                   "system!");

              ignoreExitCode = editor.ignoreExitCode();
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
	    proc = editor.prep(PackageInfo.sUser, fseq, env, dir);
	    if(proc != null) 
	      proc.start();
	    else 
	      proc = editor.launch(fseq, env, dir);

	    editID = client.editingStarted(pNodeID, editor);
 	  }
 	  catch(PipelineException ex) {
 	    master.showErrorDialog(ex);
 	    return;
 	  }
 	  finally {
 	    master.endPanelOp(pGroupID, "Done.");
 	  }
 	}

 	/* wait for the editor to exit */ 
 	if(proc != null) {
 	  try {
 	    proc.join();
	    if(!proc.wasSuccessful() && !ignoreExitCode) 
 	      master.showSubprocessFailureDialog("Editor Failure:", proc);

	    if((client != null) && (editID != null))
	      client.editingFinished(editID);
 	  }
 	  catch(Exception ex) {
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
      this(targets, null, null, null, null, null, null, null, null, null);
    }
    
    public 
    QueueJobsTask
    (
     TreeMap<NodeID,TreeSet<FileSeq>> targets,
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

      pTargets       = targets;
      pBatchSize     = batchSize;
      pPriority      = priority; 
      pRampUp        = rampUp; 
      pMaxLoad       = maxLoad;
      pMinMemory     = minMemory;
      pMinDisk       = minDisk;
      pSelectionKeys = selectionKeys;
      pLicenseKeys   = licenseKeys;
      pHardwareKeys  = hardwareKeys;
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
	    client.resubmitJobs(nodeID, pTargets.get(nodeID), 
				pBatchSize, pPriority, pRampUp, 
				pMaxLoad, pMinMemory, pMinDisk,
				pSelectionKeys, pLicenseKeys, pHardwareKeys);
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
    private Float                             pMaxLoad;        
    private Long                              pMinMemory;              
    private Long                              pMinDisk;
    private TreeSet<String>                   pSelectionKeys;
    private TreeSet<String>                   pLicenseKeys;
    private TreeSet<String>                   pHardwareKeys;
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

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Preempting Jobs...")) {
	try {
	  master.getQueueMgrClient(pGroupID).preemptJobs(pJobIDs);
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

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Killing Jobs...")) {
	try {
	  master.getQueueMgrClient(pGroupID).killJobs(pJobIDs);
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

    private TreeSet<Long>  pJobIDs;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 2702668146242766587L;

  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The canonical names of this host.
   */ 
  private TreeSet<String>  pLocalHostnames;

  /**
   * The job status of the jobs which make up the job groups indexed by job ID. 
   */ 
  private TreeMap<Long,JobStatus>  pJobStatus;

  /**
   * The toolset used to build the editor menus.
   */ 
  private String  pSlotsEditorMenuToolset;


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

}
