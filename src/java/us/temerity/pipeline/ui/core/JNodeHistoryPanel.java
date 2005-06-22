// $Id: JNodeHistoryPanel.java,v 1.11 2005/06/22 01:00:05 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;
import us.temerity.pipeline.glue.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.text.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   H I S T O R Y   P A N E L                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Displays the check-in log messages associated with a node. <P> 
 */ 
public  
class JNodeHistoryPanel
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
  JNodeHistoryPanel()
  {
    super();

    initUI();
  }

  /**
   * Construct a new panel with a working area view identical to the given panel.
   */
  public 
  JNodeHistoryPanel
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
      pEditorPlugins      = PluginMgrClient.getInstance().getEditors();
      pEditorMenuLayout   = new PluginMenuLayout();
      pRefreshEditorMenus = true; 
    }

    /* initialize the popup menus */ 
    {
      JMenuItem item;
      JMenu sub;
      
      pWorkingPopup   = new JPopupMenu();  
      pCheckedInPopup = new JPopupMenu(); 

      pEditItems            = new JMenuItem[2];
      pEditWithDefaultItems = new JMenuItem[2];
      pEditWithMenus        = new JMenu[2];

      JPopupMenu menus[] = { pWorkingPopup, pCheckedInPopup };
      int wk;
      for(wk=0; wk<menus.length; wk++) {
	item = new JMenuItem((wk == 1) ? "View" : "Edit");
	pEditItems[wk] = item;
	item.setActionCommand("edit");
	item.addActionListener(this);
	menus[wk].add(item);
	
	pEditWithMenus[wk] = new JMenu((wk == 1) ? "View With" : "Edit With");
	menus[wk].add(pEditWithMenus[wk]);

	item = new JMenuItem((wk == 1) ? "View With Default" : "Edit With Default");
	pEditWithDefaultItems[wk] = item;
	item.setActionCommand("edit-with-default");
	item.addActionListener(this);
	menus[wk].add(item);
      }
      
      {
	pWorkingPopup.addSeparator();
	
	item = new JMenuItem("Queue Jobs");
	pQueueJobsItem = item;
	item.setActionCommand("queue-jobs");
	item.addActionListener(this);
	pWorkingPopup.add(item);
	
	item = new JMenuItem("Queue Jobs Special...");
	pQueueJobsSpecialItem = item;
	item.setActionCommand("queue-jobs-special");
	item.addActionListener(this);
	pWorkingPopup.add(item);
	
	item = new JMenuItem("Pause Jobs");
	pPauseJobsItem = item;
	item.setActionCommand("pause-jobs");
	item.addActionListener(this);
	pWorkingPopup.add(item);
      
	item = new JMenuItem("Resume Jobs");
	pResumeJobsItem = item;
	item.setActionCommand("resume-jobs");
	item.addActionListener(this);
	pWorkingPopup.add(item);
	
	item = new JMenuItem("Kill Jobs");
	pKillJobsItem = item;
	item.setActionCommand("kill-jobs");
	item.addActionListener(this);
	pWorkingPopup.add(item);

	pWorkingPopup.addSeparator();

	item = new JMenuItem("Remove Files");
	pRemoveFilesItem = item;
	item.setActionCommand("remove-files");
	item.addActionListener(this);
	pWorkingPopup.add(item);
      }

      updateMenuToolTips();
    }

    /* initialize the panel components */ 
    {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));  

      /* header */ 
      {
	JPanel panel = new JPanel();	

	panel.setName("DialogHeader");	
	panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

	{
	  JLabel label = new JLabel();
	  pHeaderIcon = label;
	  
	  label.addMouseListener(this); 

	  panel.add(label);	  
	}
	
	panel.add(Box.createRigidArea(new Dimension(3, 0)));

	{
	  JLabel label = new JLabel("X");
	  pHeaderLabel = label;
	  
	  label.setName("DialogHeaderLabel");	       

	  panel.add(label);	  
	}

	panel.add(Box.createHorizontalGlue());
      
	add(panel);
      }

      add(Box.createRigidArea(new Dimension(0, 4)));

      /* full node name */ 
      {
	Box hbox = new Box(BoxLayout.X_AXIS);
	
	hbox.add(Box.createRigidArea(new Dimension(4, 0)));

	{
	  JTextField field = UIFactory.createTextField(null, 100, JLabel.LEFT);
	  pNodeNameField = field;
	  
	  field.setFocusable(true);
	  field.addKeyListener(this);
	  field.addMouseListener(this); 

	  hbox.add(field);
	}

	hbox.add(Box.createRigidArea(new Dimension(4, 0)));

	add(hbox);
      }
	
      add(Box.createRigidArea(new Dimension(0, 4)));
      
      {
	Box vbox = new Box(BoxLayout.Y_AXIS);
	pMessageBox = vbox;

	pTextAreas = new ArrayList<JTextArea>();

	{
	  JScrollPane scroll = new JScrollPane(vbox);
	  pScroll = scroll;
	  
	  scroll.setHorizontalScrollBarPolicy
	    (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	  scroll.setVerticalScrollBarPolicy
	    (ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

	  scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
	  scroll.getVerticalScrollBar().setUnitIncrement(23);

	  add(scroll);
	}
      }

      Dimension size = new Dimension(sSize+22, 120);
      setMinimumSize(size);
      setPreferredSize(size); 

      setFocusable(true);
      addKeyListener(this);
      addMouseListener(this); 
    }

    updateNodeStatus(null, null, null, null, null);
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
    return "Node History";
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

    PanelGroup<JNodeHistoryPanel> panels = master.getNodeHistoryPanels();

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
    PanelGroup<JNodeHistoryPanel> panels = UIMaster.getInstance().getNodeHistoryPanels();
    return panels.isGroupUnused(groupID);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Update the UI components to reflect the current check-in log messages.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param status
   *   The current node status.
   * 
   * @param editorPlugins
   *   The names of versions of the loaded editor plugins.   
   * 
   * @param editorLayout
   *   The menu layout for editor plugins.
   * 
   * @param history
   *   The check-in log messages.
   *
   * @param offline
   *   The revision numbers of the offline checked-in versions.
   */
  public synchronized void 
  updateNodeStatus
  (
   String author, 
   String view, 
   NodeStatus status,
   TreeMap<String,TreeSet<VersionID>> editorPlugins, 
   PluginMenuLayout editorLayout, 
   TreeMap<VersionID,LogMessage> history,
   TreeSet<VersionID> offline
  ) 
  {
    if(!pAuthor.equals(author) || !pView.equals(view)) 
      super.setAuthorView(author, view);

    updateNodeStatus(status, editorPlugins, editorLayout, history, offline);
  }

  /**
   * Update the UI components to reflect the current check-in log messages.
   * 
   * @param status
   *   The current node status.
   * 
   * @param editorPlugins
   *   The names of versions of the loaded editor plugins.   
   * 
   * @param editorLayout
   *   The menu layout for editor plugins.
   * 
   * @param history
   *   The check-in log messages.
   *
   * @param offline
   *   The revision numbers of the offline checked-in versions.
   */
  public synchronized void 
  updateNodeStatus
  (
   NodeStatus status,
   TreeMap<String,TreeSet<VersionID>> editorPlugins, 
   PluginMenuLayout editorLayout, 
   TreeMap<VersionID,LogMessage> history,
   TreeSet<VersionID> offline
  ) 
  {
    pStatus  = status;
    pHistory = history;

    NodeDetails details = null;
    if(pStatus != null) 
      details = pStatus.getDetails();

    {
      PluginMgrClient plg = PluginMgrClient.getInstance();

      if(editorPlugins != null) 
	pEditorPlugins = editorPlugins;
      else 
	pEditorPlugins = plg.getEditors();

      if(editorLayout != null) {
	pEditorMenuLayout = editorLayout; 
	pRefreshEditorMenus = true;
      }
      else {
	UIMaster master = UIMaster.getInstance(); 
	try {
	  pEditorMenuLayout = master.getMasterMgrClient().getEditorMenuLayout();
	  pRefreshEditorMenus = true;
	} 
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	}      
      }    
    }

    /* header */ 
    {
      {
	String name = "Blank-Normal";
	if(pStatus != null) {
	  if(details != null) {
	    if(details.getOverallNodeState() == OverallNodeState.NeedsCheckOut) {
	      VersionID wvid = details.getWorkingVersion().getWorkingID();
	      VersionID lvid = details.getLatestVersion().getVersionID();
	      switch(wvid.compareLevel(lvid)) {
	      case Major:
		name = ("NeedsCheckOutMajor-" + details.getOverallQueueState());
		break;
		
	      case Minor:
		name = ("NeedsCheckOut-" + details.getOverallQueueState());
		break;
		
	      case Micro:
		name = ("NeedsCheckOutMicro-" + details.getOverallQueueState());
	      }
	    }
	    else {
	      name = (details.getOverallNodeState() + "-" + details.getOverallQueueState());
	    }
	    
	    NodeMod mod = details.getWorkingVersion();
	    if((mod != null) && mod.isFrozen()) 
	      name = (name + "-Frozen-Normal");
	    else 
	      name = (name + "-Normal");
	  }
	  
	  pHeaderLabel.setText(pStatus.toString());
	  pNodeNameField.setText(pStatus.getName());
	}
	else {
	  pHeaderLabel.setText(null);
	  pNodeNameField.setText(null);
	}
	
	try {
	  pHeaderIcon.setIcon(TextureMgr.getInstance().getIcon(name));
	}
	catch(IOException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Tex, LogMgr.Level.Severe,
	     "Internal Error:\n" + 
	     "  " + ex.getMessage());
	  LogMgr.getInstance().flush();
	  System.exit(1);
	} 
      }
    }

    /* frozen node? */
    {
      pIsFrozen = false;
      if((details != null) && (details.getWorkingVersion() != null))
	pIsFrozen = details.getWorkingVersion().isFrozen();
    }

    /* check-in message history */ 
    {
      pMessageBox.removeAll();
      pTextAreas.clear();

      if(pHistory != null) {
	VersionID initial = new VersionID();
	
	ArrayList<VersionID> vids = new ArrayList<VersionID>(pHistory.keySet());
	Collections.reverse(vids);
	
	VersionID wvid = null;
	if((details != null) && (details.getWorkingVersion() != null)) 
	  wvid = details.getWorkingVersion().getWorkingID();	

	for(VersionID vid : vids) {
	  LogMessage msg = pHistory.get(vid);
	  
	  Color color  = Color.white;
	  if((wvid != null) && wvid.equals(vid)) 
	    color  = Color.cyan;

	  String rootName = msg.getRootName();
	  
	  boolean isLeaf = ((rootName == null) || 
			    ((rootName != null) && (!rootName.equals(pStatus.getName()))));

	  {
	    JPanel panel = new JPanel();
	    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));  

	    panel.setFocusable(true);
	    panel.addKeyListener(this);
	    panel.addMouseListener(this); 
	  
	    panel.add(Box.createRigidArea(new Dimension(0, 3)));
	  
	    {
	      Box hbox = new Box(BoxLayout.X_AXIS);
	      
	      {
		Box hbox2 = new Box(BoxLayout.X_AXIS);
		hbox2.add(Box.createRigidArea(new Dimension(10, 0)));
		
		{
		  JLabel label = new JLabel("v" + vid);
		  label.setForeground(color);
		  label.setToolTipText(UIFactory.formatToolTip
                    ("The revision number of the checked-in version."));
		  hbox2.add(label);
		}
		
		hbox2.add(Box.createHorizontalGlue());
		hbox2.add(Box.createRigidArea(new Dimension(4, 0)));
	      
		{
		  JLabel label = new JLabel(msg.getAuthor());
		  label.setForeground(color);
		  label.setToolTipText(UIFactory.formatToolTip
                    ("The name of the user who created the version."));
		  hbox2.add(label);
		}
		
		hbox2.add(Box.createRigidArea(new Dimension(4, 0)));
		hbox2.add(Box.createHorizontalGlue());
		
		{
		  JLabel label = new JLabel(Dates.format(msg.getTimeStamp()));
		  label.setForeground(color);
		  label.setToolTipText(UIFactory.formatToolTip
                    ("When the version was checked-in."));
		  hbox2.add(label);
		}
		
		hbox2.add(Box.createRigidArea(new Dimension(10, 0)));
		
		Dimension size = new Dimension(sSize, 19);
		hbox2.setMinimumSize(size);
		hbox2.setMaximumSize(size);
		hbox2.setPreferredSize(size);
		
		hbox.add(hbox2);
	      }
	      
	      hbox.add(Box.createHorizontalGlue());
	      
	      panel.add(hbox);
	    }
	    
	    panel.add(Box.createRigidArea(new Dimension(0, 3)));
	    
	    {
	      Box hbox = new Box(BoxLayout.X_AXIS);
	      
	      hbox.add(Box.createRigidArea(new Dimension(4, 0)));
	      
	      {
		JTextArea area = new JTextArea(msg.getMessage(), 0, 39);
		pTextAreas.add(area);
		
		area.setName(isLeaf ? "HistoryTextArea" : "HistoryTextAreaDark");
		area.setForeground(color);
		
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		
		area.setEditable(false);
		
		area.setFocusable(true);
		area.addKeyListener(this);
		area.addMouseListener(this); 
		
		hbox.add(area);
	      }
	      
	      hbox.add(Box.createRigidArea(new Dimension(4, 0)));
	      hbox.add(Box.createHorizontalGlue());
	      
	      panel.add(hbox);
	    }
	    
	    panel.add(Box.createRigidArea(new Dimension(0, 3)));

	    {
	      Box hbox = new Box(BoxLayout.X_AXIS);
	      
	      {
		Box hbox2 = new Box(BoxLayout.X_AXIS);
		
		hbox2.add(Box.createRigidArea(new Dimension(10, 0)));

		if(offline.contains(vid)) {
		  JLabel label = new JLabel("Offline");
		  label.setForeground(new Color(0.75f, 0.75f, 0.75f));
		  label.setToolTipText(UIFactory.formatToolTip
		    ("The checked-in version is currently offline."));
		  hbox2.add(label);
		}

		hbox2.add(Box.createHorizontalGlue());
		
		{
		  String text = null;
		  String tooltip = null;
		  if(rootName == null) {
		    text = "???";
		    tooltip = "The root check-in node is unknown.";
		  }
		  else if(rootName.equals(pStatus.getName())) {
		    text = "Check-In Root"; 
		    tooltip = "This node was the root of the check-in.";
		  }
		  else {
		    text = (rootName + "  v" + msg.getRootVersionID());
		    tooltip = "The name and revision number of the root check-in node.";
		  }
		  
		  JLabel label = new JLabel(text);
		  label.setForeground(new Color(0.75f, 0.75f, 0.75f));

		  label.setToolTipText(UIFactory.formatToolTip(tooltip));                    

		  hbox2.add(label);
		}
		
		hbox2.add(Box.createRigidArea(new Dimension(10, 0)));
		
		Dimension size = new Dimension(sSize, 19);
		hbox2.setMinimumSize(size);
		hbox2.setMaximumSize(size);
		hbox2.setPreferredSize(size);
		
		hbox.add(hbox2);
	      }
	      
	      hbox.add(Box.createHorizontalGlue());
	      
	      panel.add(hbox);
	    }
	  
	    panel.add(Box.createRigidArea(new Dimension(0, 3)));
	    
	    pMessageBox.add(panel);
	  }
	
	  if(!vid.equals(initial)) {
	    JPanel spanel = new JPanel();
	    spanel.setName("Spacer");
	    
	    spanel.setMinimumSize(new Dimension(sSize, 7));
	    spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 7));
	    spanel.setPreferredSize(new Dimension(sSize, 7));
	    
	    pMessageBox.add(spanel);
	  }
	}
      }
      
      {
	JPanel spanel = new JPanel();
	spanel.setName("Spacer");
	
	spanel.setMinimumSize(new Dimension(sSize, 7));
	spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
	spanel.setPreferredSize(new Dimension(sSize, 7));
	
	pMessageBox.add(spanel);
      }
    }
      
    pMessageBox.revalidate();

    SwingUtilities.invokeLater(new ScrollTask());
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the editor plugin menus.
   */ 
  private synchronized void 
  updateEditorMenus()
  {
    if(pRefreshEditorMenus) {
      int wk;
      for(wk=0; wk<pEditWithMenus.length; wk++) {
	pEditWithMenus[wk].removeAll();
	for(PluginMenuLayout pml : pEditorMenuLayout) 
	  pEditWithMenus[wk].add(buildPluginMenu(pml, "edit-with", pEditorPlugins));
      }
      
      pRefreshEditorMenus = false;
    }
  }
  
  /**
   * Recursively update a plugin menu.
   */ 
  private JMenuItem
  buildPluginMenu
  (
   PluginMenuLayout layout, 
   String prefix, 
   TreeMap<String,TreeSet<VersionID>> plugins
  ) 
  {
    JMenuItem item = null;
    if(layout.isMenuItem()) {
      item = new JMenuItem(layout.getTitle());
      item.setActionCommand(prefix + ":" + layout.getName() + ":" + layout.getVersionID());
      item.addActionListener(this);
   
      TreeSet<VersionID> vids = plugins.get(layout.getName());
      item.setEnabled((vids != null) && vids.contains(layout.getVersionID()));
    }
    else {
      JMenu sub = new JMenu(layout.getTitle()); 
      for(PluginMenuLayout pml : layout) 
	sub.add(buildPluginMenu(pml, prefix, plugins));
      item = sub;
    }

    return item;
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
       
    int wk;
    for(wk=0; wk<pEditItems.length; wk++) {
      updateMenuToolTip
	(pEditItems[wk], prefs.getEdit(), 
	 "Edit primary file sequences of the current primary selection.");

      updateMenuToolTip
	(pEditWithDefaultItems[wk], prefs.getEdit(), 
	 "Edit primary file sequences of the current primary selection using the default" + 
	 "editor for the file type.");
    }

    updateMenuToolTip
      (pQueueJobsItem, prefs.getQueueJobs(), 
       "Submit jobs to the queue for the current primary selection.");
    updateMenuToolTip
      (pQueueJobsSpecialItem, prefs.getQueueJobsSpecial(), 
       "Submit jobs to the queue for the current primary selection with special job " + 
       "requirements.");
    updateMenuToolTip
      (pPauseJobsItem, prefs.getPauseJobs(), 
       "Pause all jobs associated with the selected nodes.");
    updateMenuToolTip
      (pResumeJobsItem, prefs.getResumeJobs(), 
       "Resume execution of all jobs associated with the selected nodes.");
    updateMenuToolTip
      (pKillJobsItem, prefs.getKillJobs(), 
       "Kill all jobs associated with the selected nodes.");

    updateMenuToolTip
      (pRemoveFilesItem, prefs.getRemoveFiles(), 
       "Remove all the primary/secondary files associated with the selected nodes.");
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
    requestFocusInWindow();
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
  mousePressed
  (
   MouseEvent e
  )
  {
    /* manager panel popups */ 
    if(pManagerPanel.handleManagerMouseEvent(e)) 
      return;

    /* local mouse events */ 
    if(e.getSource() == pHeaderIcon) {
      if(pStatus == null) 
	return; 

      NodeDetails details = pStatus.getDetails();
      if(details == null) 
	return;

      NodeMod work = details.getWorkingVersion();
      NodeVersion latest = details.getLatestVersion();
      if((work != null) && !pIsFrozen) {
	updateEditorMenus();
	pWorkingPopup.show(e.getComponent(), e.getX(), e.getY());
      }
      else if(latest != null) {
	updateEditorMenus();
	pCheckedInPopup.show(e.getComponent(), e.getX(), e.getY());
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
    UserPrefs prefs = UserPrefs.getInstance();
    if((prefs.getEdit() != null) &&
       prefs.getEdit().wasPressed(e))
      doEdit();
    else if((prefs.getEditWithDefault() != null) &&
	    prefs.getEditWithDefault().wasPressed(e))
      doEditWithDefault();
    
    else if((prefs.getQueueJobs() != null) &&
	    prefs.getQueueJobs().wasPressed(e))
      doQueueJobs();
    else if((prefs.getQueueJobsSpecial() != null) &&
	    prefs.getQueueJobsSpecial().wasPressed(e))
      doQueueJobsSpecial();
    else if((prefs.getPauseJobs() != null) &&
	    prefs.getPauseJobs().wasPressed(e))
	doPauseJobs();
    else if((prefs.getResumeJobs() != null) &&
	    prefs.getResumeJobs().wasPressed(e))
      doResumeJobs();
    else if((prefs.getKillJobs() != null) &&
	      prefs.getKillJobs().wasPressed(e))
      doKillJobs();
    
    else if((prefs.getRemoveFiles() != null) &&
	    prefs.getRemoveFiles().wasPressed(e))
      doRemoveFiles();

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
    if(cmd.equals("edit"))
      doEdit();
    else if(cmd.equals("edit-with-default"))
      doEditWithDefault();
    else if(cmd.startsWith("edit-with:"))
      doEditWith(cmd.substring(10)); 

    else if(cmd.equals("queue-jobs"))
      doQueueJobs();
    else if(cmd.equals("queue-jobs-special"))
      doQueueJobsSpecial();
    else if(cmd.equals("pause-jobs"))
      doPauseJobs();
    else if(cmd.equals("resume-jobs"))
      doResumeJobs();
    else if(cmd.equals("kill-jobs"))
      doKillJobs();

    else if(cmd.equals("remove-files"))
      doRemoveFiles();        
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Edit/View the current node with the editor specified by the node version.
   */ 
  private void 
  doEdit() 
  {
    if(pStatus != null) {
      NodeDetails details = pStatus.getDetails();
      if(details != null) {
	NodeCommon com = details.getWorkingVersion();
	if(com == null) 
	  com = details.getLatestVersion();

	if(com != null) {
	  EditTask task = new EditTask(com);
	  task.start();
	}
      }
    }
  }

  /**   
   * Edit/View the primary selected node using the default editor for the file type.
   */ 
  private void 
  doEditWithDefault() 
  {
    if(pStatus != null) {
      NodeDetails details = pStatus.getDetails();
      if(details != null) {
	NodeCommon com = details.getWorkingVersion();
	if(com == null) 
	  com = details.getLatestVersion();

	if(com != null) {
	  EditTask task = new EditTask(com, true);
	  task.start();
	}
      }
    }
  }

  /**
   * Edit/View the current node with the given editor.
   */ 
  private void 
  doEditWith
  (
   String editor
  ) 
  {
    String ename = null;
    VersionID evid = null;
    String parts[] = editor.split(":");
    switch(parts.length) {
    case 1:
      ename = editor;
      break;

    case 2:
      ename = parts[0];
      evid = new VersionID(parts[1]);
      break;

    default:
      assert(false);
    }

    if(pStatus != null) {
      NodeDetails details = pStatus.getDetails();
      if(details != null) {
	NodeCommon com = details.getWorkingVersion();
	if(com == null) 
	  com = details.getLatestVersion();

	if(com != null) {
	  EditTask task = new EditTask(com, ename, evid);
	  task.start();
	}
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Queue jobs to the queue for the primary current node and all nodes upstream of it.
   */ 
  private void 
  doQueueJobs() 
  {
    if(pIsFrozen) 
      return;

    if(pStatus != null) {
      NodeDetails details = pStatus.getDetails();
      if(details != null) {
	QueueJobsTask task = new QueueJobsTask(pStatus.getName());
	task.start();
      }
    }
  }

  /**
   * Queue jobs to the queue for the primary current node and all nodes upstream of it
   * with special job requirements.
   */ 
  private void 
  doQueueJobsSpecial() 
  {
    if(pIsFrozen) 
      return;

    if(pStatus != null) {
      NodeDetails details = pStatus.getDetails();
      if(details != null) {
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

	  QueueJobsTask task = 
	    new QueueJobsTask(pStatus.getName(), batchSize, priority, interval, keys);
	  task.start();
	}
      }
    }
  }

  /**
   * Pause all waiting jobs associated with the current node.
   */ 
  private void 
  doPauseJobs() 
  {
    if(pIsFrozen) 
      return;

    TreeSet<Long> paused = new TreeSet<Long>();
    if(pStatus != null) {
      NodeDetails details = pStatus.getDetails();
      if(details != null) {
	Long[] jobIDs   = details.getJobIDs();
	QueueState[] qs = details.getQueueState();
	assert(jobIDs.length == qs.length);

	int wk;
	for(wk=0; wk<jobIDs.length; wk++) {
	  switch(qs[wk]) {
	  case Queued:
	    assert(jobIDs[wk] != null);
	    paused.add(jobIDs[wk]);
	  }
	}
      }
    }

    if(!paused.isEmpty()) {
      PauseJobsTask task = new PauseJobsTask(paused);
      task.start();
    }
  }

  /**
   * Resume execution of all paused jobs associated with the current node.
   */ 
  private void 
  doResumeJobs() 
  {
    if(pIsFrozen) 
      return;

    TreeSet<Long> resumed = new TreeSet<Long>();
    if(pStatus != null) {
      NodeDetails details = pStatus.getDetails();
      if(details != null) {
	Long[] jobIDs   = details.getJobIDs();
	QueueState[] qs = details.getQueueState();
	assert(jobIDs.length == qs.length);

	int wk;
	for(wk=0; wk<jobIDs.length; wk++) {
	  switch(qs[wk]) {
	  case Paused:
	    assert(jobIDs[wk] != null);
	    resumed.add(jobIDs[wk]);
	  }
	}
      }
    }

    if(!resumed.isEmpty()) {
      ResumeJobsTask task = new ResumeJobsTask(resumed);
      task.start();
    }
  }

  /**
   * Kill all jobs associated with the current node.
   */ 
  private void 
  doKillJobs() 
  {
    if(pIsFrozen) 
      return;

    TreeSet<Long> dead = new TreeSet<Long>();
    if(pStatus != null) {
      NodeDetails details = pStatus.getDetails();
      if(details != null) {
	Long[] jobIDs   = details.getJobIDs();
	QueueState[] qs = details.getQueueState();
	assert(jobIDs.length == qs.length);

	int wk;
	for(wk=0; wk<jobIDs.length; wk++) {
	  switch(qs[wk]) {
	  case Queued:
	  case Paused:
	  case Running:
	    assert(jobIDs[wk] != null);
	    dead.add(jobIDs[wk]);
	  }
	}
      }
    }

    if(!dead.isEmpty()) {
      KillJobsTask task = new KillJobsTask(dead);
      task.start();
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Remove all primary/secondary files associated with the current node.
   */ 
  private void 
  doRemoveFiles() 
  {
    if(pIsFrozen) 
      return;

    if(pStatus != null) {
      NodeDetails details = pStatus.getDetails();
      if(details != null) {
	RemoveFilesTask task = new RemoveFilesTask(pStatus.getName());
	task.start();
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Scrolls to the top check-in message.
   */ 
  private
  class ScrollTask
    extends Thread
  {
    public 
    ScrollTask()
    {
      super("JNodeHistoryPanel:ScrollTask");
    }

    public void 
    run() 
    {    
      for(JTextArea area : pTextAreas) {
	area.setRows(area.getLineCount());		
	
	Dimension size = area.getPreferredSize();
	area.setMinimumSize(size);
	area.setMaximumSize(size);
      }

      pScroll.getViewport().setViewPosition(new Point());
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Edit/View the primary file sequence of the given node version.
   */ 
  private
  class EditTask
    extends UIMaster.EditTask
  {
    public 
    EditTask
    (
     NodeCommon com
    ) 
    {
      UIMaster.getInstance().super(com, false, pAuthor, pView);
      setName("JNodeHistoryPanel:EditTask");
    }

    public 
    EditTask
    (
     NodeCommon com,
     boolean useDefault
    ) 
    {
      UIMaster.getInstance().super(com, useDefault, pAuthor, pView);
      setName("JNodeHistoryPanel:EditTask");
    }

    public 
    EditTask
    (
     NodeCommon com, 
     String ename, 
     VersionID evid
    ) 
    {
      UIMaster.getInstance().super(com, ename, evid, pAuthor, pView);
      setName("JNodeHistoryPanel:EditTask");
    }
  }


  
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Queue jobs to the queue for the given node.
   */ 
  private
  class QueueJobsTask
    extends UIMaster.QueueJobsTask
  {
    public 
    QueueJobsTask
    (
     String name
    ) 
    {
      this(name, null, null, null, null);
    }

    public 
    QueueJobsTask
    (
     String name, 
     Integer batchSize, 
     Integer priority, 
     Integer rampUp, 
     TreeSet<String> selectionKeys
    ) 
    {
      UIMaster.getInstance().super(name, pAuthor, pView, 
				   batchSize, priority, rampUp, selectionKeys);
      setName("JNodeHistoryPanel:QueueJobsTask");
    }

    protected void
    postOp() 
    {
      if(pGroupID > 0) {
	PanelGroup<JNodeViewerPanel> panels = UIMaster.getInstance().getNodeViewerPanels();
	JNodeViewerPanel viewer = panels.getPanel(pGroupID);
	if(viewer != null) 
	  viewer.updateRoots();
      } 
    }
  }

  /** 
   * Pause the given jobs.
   */ 
  private
  class PauseJobsTask
    extends UIMaster.PauseJobsTask
  {
    public 
    PauseJobsTask
    (
     TreeSet<Long> jobIDs
    ) 
    {
      UIMaster.getInstance().super(jobIDs, pAuthor, pView);
      setName("JNodeHistoryPanel:PauseJobsTask");

      pJobIDs = jobIDs; 
    }

    protected void
    postOp() 
    {
      if(pGroupID > 0) {
	PanelGroup<JNodeViewerPanel> panels = UIMaster.getInstance().getNodeViewerPanels();
	JNodeViewerPanel viewer = panels.getPanel(pGroupID);
	if(viewer != null) 
	  viewer.updateRoots();
      }
    }

    private TreeSet<Long>  pJobIDs; 
  }

  /** 
   * Resume execution of the the given paused jobs.
   */ 
  private
  class ResumeJobsTask
    extends UIMaster.ResumeJobsTask
  {
    public 
    ResumeJobsTask
    (
     TreeSet<Long> jobIDs
    ) 
    {
      UIMaster.getInstance().super(jobIDs, pAuthor, pView);
      setName("JNodeHistoryPanel:ResumeJobsTask");

      pJobIDs = jobIDs; 
    }

    protected void
    postOp() 
    {
      if(pGroupID > 0) {
	PanelGroup<JNodeViewerPanel> panels = UIMaster.getInstance().getNodeViewerPanels();
	JNodeViewerPanel viewer = panels.getPanel(pGroupID);
	if(viewer != null) 
	  viewer.updateRoots();
      }
    }

    private TreeSet<Long>  pJobIDs; 
  }

  /** 
   * Kill the given jobs.
   */ 
  private
  class KillJobsTask
    extends UIMaster.KillJobsTask
  {
    public 
    KillJobsTask
    (
     TreeSet<Long> jobIDs
    ) 
    {
      UIMaster.getInstance().super(jobIDs, pAuthor, pView);
      setName("JNodeHistoryPanel:KillJobsTask");

      pJobIDs = jobIDs; 
    }

    protected void
    postOp() 
    {
      if(pGroupID > 0) {
	PanelGroup<JNodeViewerPanel> panels = UIMaster.getInstance().getNodeViewerPanels();
	JNodeViewerPanel viewer = panels.getPanel(pGroupID);
	if(viewer != null) 
	  viewer.updateRoots();
      }
    }

    private TreeSet<Long>  pJobIDs; 
  }

  
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Remove the working area files associated with the given nodes.
   */ 
  private
  class RemoveFilesTask
    extends UIMaster.RemoveFilesTask
  {
    public 
    RemoveFilesTask
    (
     String name
    ) 
    {
      UIMaster.getInstance().super(name, pAuthor, pView);
      setName("JNodeHistoryPanel:RemoveFilesTask");
    }
    
    protected void
    postOp() 
    {
      if(pGroupID > 0) {
	PanelGroup<JNodeViewerPanel> panels = UIMaster.getInstance().getNodeViewerPanels();
	JNodeViewerPanel viewer = panels.getPanel(pGroupID);
	if(viewer != null) 
	  viewer.updateRoots();
      }      
    }    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 7763696064974680919L;
  
  private static final int  sSize = 564;


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The current node status.
   */ 
  private NodeStatus  pStatus;

  /**
   * The check-in log messages of the current node.
   */ 
  private TreeMap<VersionID,LogMessage>  pHistory;


  /*----------------------------------------------------------------------------------------*/

  /**
   * Cached names and version numbers of the loaded editor plugins. 
   */
  private TreeMap<String,TreeSet<VersionID>>  pEditorPlugins; 

  /**
   * The menu layout for editor plugins.
   */ 
  private PluginMenuLayout  pEditorMenuLayout;

  /**
   * Whether the Swing editor menus need to be rebuild from the menu layout.
   */ 
  private boolean pRefreshEditorMenus; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The working file popup menu.
   */ 
  private JPopupMenu  pWorkingPopup; 
  
  /**
   * The working file popup menu items.
   */ 
  private JMenuItem  pQueueJobsItem;
  private JMenuItem  pQueueJobsSpecialItem;
  private JMenuItem  pPauseJobsItem;
  private JMenuItem  pResumeJobsItem;
  private JMenuItem  pKillJobsItem;
  private JMenuItem  pRemoveFilesItem;  

  /**
   * The checked-in file popup menu.
   */ 
  private JPopupMenu  pCheckedInPopup; 

  /**
   * The edit with submenus.
   */ 
  private JMenuItem[]  pEditItems;
  private JMenuItem[]  pEditWithDefaultItems;
  private JMenu[]      pEditWithMenus; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The node name/state header.
   */ 
  private JLabel  pHeaderIcon;
  private JLabel pHeaderLabel;
  
  /**
   * The fully resolved node name field.
   */ 
  private JTextField pNodeNameField;

  /**
   * Whether the working version is frozen.
   */
  private boolean  pIsFrozen; 

  /**
   * The log message container.
   */ 
  private Box  pMessageBox;

  /**
   * The log message text areas.
   */
  private ArrayList<JTextArea>  pTextAreas; 

  /**
   * The scroll panel containing the messages.
   */ 
  private JScrollPane  pScroll; 

}
