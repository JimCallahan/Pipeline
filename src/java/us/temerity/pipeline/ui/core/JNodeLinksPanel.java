// $Id: JNodeLinksPanel.java,v 1.1 2005/02/09 18:21:12 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.core.*;
import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.text.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   L I N K S   P A N E L                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Displays the upstream links associated with the working and checked-in versions of a 
 * node.
 */ 
public  
class JNodeLinksPanel
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
  JNodeLinksPanel()
  {
    super();

    initUI();
  }

  /**
   * Construct a new panel with a working area view identical to the given panel.
   */
  public 
  JNodeLinksPanel
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
      pLinks = new TreeMap<VersionID,TreeMap<String,LinkVersion>>();

      pEditorPlugins      = PluginMgrClient.getInstance().getEditors();
      pEditorMenuLayout   = new PluginMenuLayout();
      pRefreshEditorMenus = true; 

      pArrows             = new TreeMap<String,JLinkArrow>();
      pPolicyFields       = new TreeMap<String,JCollectionField>();
      pRelationshipFields = new TreeMap<String,JCollectionField>();
      pFrameOffsetFields  = new TreeMap<String,JIntegerField>();
      pLinkComps          = new TreeMap<String,ArrayList<JComponent[]>>();
    }

    /* initialize the popup menus */ 
    {
      JMenuItem item;
      JMenu sub;
      
      pWorkingPopup   = new JPopupMenu();  
      pCheckedInPopup = new JPopupMenu(); 

      pEditItems     = new JMenuItem[2];
      pEditWithMenus = new JMenu[2];

      {
	item = new JMenuItem("Apply Changes");
	pApplyItem = item;
	item.setActionCommand("apply");
	item.addActionListener(this);
	pWorkingPopup.add(item);

	pWorkingPopup.addSeparator();
      }

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
      
	{
	  JLabel label = new JLabel(sFrozenIcon);
	  pFrozenLabel = label;
	  panel.add(label);	  
	}

	{
	  JButton btn = new JButton();		
	  pApplyButton = btn;
	  btn.setName("ApplyHeaderButton");
		  
	  Dimension size = new Dimension(19, 19);
	  btn.setMinimumSize(size);
	  btn.setMaximumSize(size);
	  btn.setPreferredSize(size);
	  
	  btn.setActionCommand("apply");
	  btn.addActionListener(this);

	  btn.setToolTipText(UIFactory.formatToolTip
	    ("Replace the working links with the selected checked-in files."));

	  panel.add(btn);
	} 

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
	pLinksBox = vbox;

	{
	  JPanel spanel = new JPanel();
	  spanel.setName("Spacer");
	  
	  spanel.setMinimumSize(new Dimension(sTSize+sVSize+sCSize, 7));
	  spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
	  spanel.setPreferredSize(new Dimension(sTSize+sVSize+sCSize, 7));
	  
	  vbox.add(spanel);
	}

	{
	  JScrollPane scroll = new JScrollPane(vbox);
	  pScroll = scroll;
	  
	  scroll.setHorizontalScrollBarPolicy
	    (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	  scroll.setVerticalScrollBarPolicy
	    (ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

	  scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

	  add(scroll);
	}
      }

      Dimension size = new Dimension(sTSize+sVSize+sCSize+22, 120);
      setMinimumSize(size);
      setPreferredSize(size); 

      setFocusable(true);
      addKeyListener(this);
      addMouseListener(this); 
    }

    updateNodeStatus(null, null, null, null);
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
    return "Node Links";
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

    PanelGroup<JNodeLinksPanel> panels = master.getNodeLinksPanels();

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
    PanelGroup<JNodeLinksPanel> panels = UIMaster.getInstance().getNodeLinksPanels();
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
   * @param links
   *   The check-in links. 
   */
  public synchronized void 
  updateNodeStatus
  (
   String author, 
   String view, 
   NodeStatus status,
   TreeMap<String,TreeSet<VersionID>> editorPlugins, 
   PluginMenuLayout editorLayout, 
   TreeMap<VersionID,TreeMap<String,LinkVersion>> links
  ) 
  {
    if(!pAuthor.equals(author) || !pView.equals(view)) 
      super.setAuthorView(author, view);

    updateNodeStatus(status, editorPlugins, editorLayout, links);
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
   * @param links
   *   The check-in links. 
   */
  public synchronized void 
  updateNodeStatus
  (
   NodeStatus status,
   TreeMap<String,TreeSet<VersionID>> editorPlugins, 
   PluginMenuLayout editorLayout, 
   TreeMap<VersionID,TreeMap<String,LinkVersion>> links
  ) 
  {
    pStatus = status;

    pLinks.clear();
    if(links != null) 
      pLinks.putAll(links);

    NodeDetails details = null;
    if(pStatus != null) 
      details = pStatus.getDetails();

    NodeMod mod = null;
    if(details != null) 
      mod = details.getWorkingVersion();

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

      pFrozenLabel.setVisible(pIsFrozen);
      pApplyButton.setVisible(!pIsFrozen);
    }

    /* links */ 
    {
      pLinksBox.removeAll();
      pArrows.clear();
      pPolicyFields.clear();
      pRelationshipFields.clear();
      pFrameOffsetFields.clear();
      pLinkComps.clear();

      ArrayList<String> lnames = new ArrayList<String>();
      if(mod != null) {
	for(String lname : mod.getSourceNames()) 
	  lnames.add(lname);
      }

      for(VersionID vid : pLinks.keySet()) {
	for(String lname : pLinks.get(vid).keySet()) {
	  if(!lnames.contains(lname)) 
	    lnames.add(lname);
	}
      }
      
      for(String lname : lnames) {
	LinkMod link = null;
	if(mod != null) 
	  link = mod.getSource(lname);

	boolean isModifiable = ((link != null) && !pIsFrozen);

	Component comps[] = createCommonPanels();
	{
	  JPanel tpanel = (JPanel) comps[0];
	  JPanel vpanel = (JPanel) comps[1];

	  {
	    Dimension size = new Dimension(sVSize, 163);
	    vpanel.setMinimumSize(size);
	    vpanel.setMaximumSize(size);
	    vpanel.setPreferredSize(size);
	  }	  

	  {
	    tpanel.add(Box.createRigidArea(new Dimension(0, 52)));  
	    vpanel.add(Box.createRigidArea(new Dimension(0, 28)));  
	    
	    {
	      Box hbox = new Box(BoxLayout.X_AXIS);

	      hbox.add(Box.createHorizontalGlue());

	      JLinkArrow arrow = new JLinkArrow(link != null);
	      pArrows.put(lname, arrow);
	      hbox.add(arrow);

	      hbox.add(Box.createHorizontalGlue());

	      vpanel.add(hbox);
	    }

	    UIFactory.createTitledTextField(tpanel, "Revision Number:", sTSize, 
					    vpanel, "-", sVSize);
	
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);	    

	    {
	      ArrayList<String> values = new ArrayList<String>();
	      if(link != null) 
		values.addAll(LinkPolicy.titles());
	      else 
		values.add("-");	      

	      JCollectionField field = 
		UIFactory.createTitledCollectionField(tpanel, "Link Policy:", sTSize, 
						      vpanel, values, sVSize);

	      if(link != null) 
		field.setSelectedIndex(link.getPolicy().ordinal()); 

	      field.setEnabled(isModifiable); 
	      
	      field.addActionListener(this);
	      field.setActionCommand("policy-changed:" + lname);

	      pPolicyFields.put(lname, field);
	    }
	
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	    
	    {
	      ArrayList<String> values = new ArrayList<String>();
	      if(link != null) 
		values.addAll(LinkRelationship.titles()); 
	      else 
		values.add("-");
	      
	      JCollectionField field = 
		UIFactory.createTitledCollectionField(tpanel, "Link Relationship:", sTSize, 
						      vpanel, values, sVSize);

	      if(link != null) 
		field.setSelectedIndex(link.getRelationship().ordinal());

	      field.setEnabled(isModifiable); 
	      
	      field.addActionListener(this);
	      field.setActionCommand("relationship-changed:" + lname);

	      pRelationshipFields.put(lname, field);
	    }
	    
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	    
	    {
	      JIntegerField field = 
		UIFactory.createTitledIntegerField(tpanel, "Frame Offset:", sTSize, 
						   vpanel, null, sVSize);

	      Integer offset = null;
	      if(link != null) 
		offset = link.getFrameOffset();

	      if(offset != null) 
		field.setValue(offset);    

	      field.setEnabled(isModifiable); 

	      field.addActionListener(this);
	      field.setActionCommand("offset-changed:" + lname);

	      pFrameOffsetFields.put(lname, field);
	    }

	    UIFactory.addVerticalGlue(tpanel, vpanel);
	  }
	  
	  {
	    Box vbox = new Box(BoxLayout.Y_AXIS);
	    vbox.add(Box.createRigidArea(new Dimension(0, 3)));
	  
	    /* column header */ 
	    JViewport headerViewport = null;
	    {
	      JPanel panel = new JPanel();
	      
	      panel.setName("TitleValuePanel");
	      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
	      
	      {
		JViewport view = new JViewport();
		headerViewport = view; 
		
		{
		  Box hbox = new Box(BoxLayout.X_AXIS);
		  
		  VersionID bvid = null;
		  if(details.getBaseVersion() != null) 
		    bvid = details.getBaseVersion().getVersionID();
		  
		  ArrayList<VersionID> rvids = new ArrayList<VersionID>(pLinks.keySet());
		  Collections.reverse(rvids);
		  
		  for(VersionID vid : rvids) {
		    JButton btn = new JButton("v" + vid);
		    btn.setName("TableHeaderButton");
		    
		    if((bvid != null) && bvid.equals(vid)) 
		      btn.setForeground(Color.cyan);
		    
		    btn.addActionListener(this);
		    btn.setActionCommand("version-pressed:" + lname + ":" + vid);
		    
		    btn.setFocusable(false);
		    
		    Dimension size = new Dimension(70, 23);
		    btn.setMinimumSize(size);
		    btn.setMaximumSize(size);
		    btn.setPreferredSize(size);
		    
		    hbox.add(btn);
		  }
		  
		  Dimension size = new Dimension(70*pLinks.size(), 23); 
		  hbox.setMinimumSize(size);
		  hbox.setMaximumSize(size);
		  hbox.setPreferredSize(size);
		  
		  view.setView(hbox);
		}
		
		panel.add(view);
	      }
	      
	      panel.setMinimumSize(new Dimension(70, 29));
	      panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 29));
	      panel.setPreferredSize(new Dimension(70, 29));
	      
	      vbox.add(panel);	    
	    }
	    
	    /* table contents */ 
	    {
	      JLinksPanel panel = new JLinksPanel(this, lname, isModifiable);
	      
	      {
		JScrollPane scroll = new JScrollPane(panel);
		
		scroll.setHorizontalScrollBarPolicy
		  (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll.setVerticalScrollBarPolicy
		  (ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		
		scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
		
		Dimension size = new Dimension(70, 21);
		scroll.setMinimumSize(size);
	      
		{
		  AdjustLinkage linkage = 
		    new AdjustLinkage(scroll.getViewport(), headerViewport);
		  scroll.getHorizontalScrollBar().addAdjustmentListener(linkage);
		}
		
		vbox.add(scroll);
	      }
	    }

	    Box pbox = (Box) comps[2];
	    pbox.add(vbox);
	  }

	  JDrawer drawer = new JDrawer(lname + ":", (JComponent) comps[2], true);
	  drawer.setToolTipText(UIFactory.formatToolTip("Upstream link node name."));
	  pLinksBox.add(drawer);
	}
      }

      {
	JPanel spanel = new JPanel();
	spanel.setName("Spacer");
	  
	spanel.setMinimumSize(new Dimension(sTSize+sVSize+sCSize, 7));
	spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
	spanel.setPreferredSize(new Dimension(sTSize+sVSize+sCSize, 7));
	
	pLinksBox.add(spanel);
      }

      pLinksBox.revalidate();
    }

    pApplyButton.setEnabled(false);
    pApplyItem.setEnabled(false);
  }

  /**
   * Create the title/value panels.
   * 
   * @return 
   *   The title panel, value panel and containing box.
   */   
  private Component[]
  createCommonPanels()
  {
    Component comps[] = UIFactory.createTitledPanels();

    {
      JPanel panel = (JPanel) comps[0];
      panel.setFocusable(true);
      panel.addKeyListener(this);
      panel.addMouseListener(this); 
    }

    return comps;
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
       
    updateMenuToolTip
      (pApplyItem, prefs.getApplyChanges(),
       "Apply the changes to the working version.");

    int wk;
    for(wk=0; wk<pEditItems.length; wk++) {
      updateMenuToolTip
	(pEditItems[wk], prefs.getEdit(), 
	 "Edit primary file sequences of the current primary selection.");
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
    if((prefs.getApplyChanges() != null) &&
       prefs.getApplyChanges().wasPressed(e) && 
       pApplyButton.isEnabled())
      doApply();

    else if((prefs.getEdit() != null) &&
       prefs.getEdit().wasPressed(e))
      doEdit();
    
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
    if(cmd.equals("apply")) 
      doApply();

    else if(cmd.equals("edit"))
      doEdit();
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

    else 
      System.out.print("Action = " + cmd + "\n");
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Replace working files with the selected checked-in files.
   */ 
  private void 
  doApply()
  {
    if(pIsFrozen) 
      return;

    //....

    pApplyButton.setEnabled(false);
    pApplyItem.setEnabled(false);
	  
    //RevertTask task = new RevertTask(files);
    //task.start();
  }
  

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
   * A label used to represent to horizontal bars for links which are not novel.
   */ 
  private 
  class JLinkBar
    extends JLabel
  {
    public 
    JLinkBar
    (
     boolean isExtended
    ) 
    {
      super();

      pIsExtended = isExtended;
      setSelected(false);

      Dimension size = new Dimension(70, 19);
      setMinimumSize(size);
      setMaximumSize(size);
      setPreferredSize(size);
    }

    public boolean
    isExtended()
    {
      return pIsExtended;
    }

    public boolean
    isSelected()
    {
      return pIsSelected;
    }

    public void 
    setSelected
    (
     boolean tf
    ) 
    {
      pIsSelected = tf;
      if(pIsSelected) 
	setIcon(pIsExtended ? sFileBarExtendIconSelected : sFileBarIconSelected);
      else
	setIcon(pIsExtended ? sFileBarExtendIcon : sFileBarIcon);
    }

    private static final long serialVersionUID = -8182004303495407765L;

    protected boolean      pIsExtended;
    protected boolean      pIsSelected; 
  }
  
  private 
  class JExtraLinkBar
    extends JLinkBar
  {
    public 
    JExtraLinkBar
    (
     boolean isExtended
    ) 
    {
      super(isExtended);
    }

    public void 
    setSelected
    (
     boolean tf
    ) 
    {
      pIsSelected = tf;
      if(pIsSelected) 
	setIcon(pIsExtended ? sLinkBarExtendIconSelected : sLinkBarIconSelected);
      else
	setIcon(pIsExtended ? sLinkBarExtendIcon : sLinkBarIcon);
    }

    private static final long serialVersionUID = -8692287670599793208L;
  }

  /**
   * A label used to indicate links which will be replaced by the apply operation.
   */ 
  private 
  class JLinkArrow
    extends JLabel
  {
    public 
    JLinkArrow
    (
     boolean isActive
    ) 
    {
      super();

      pIsActive = isActive;
      setSelected(false);

      Dimension size = new Dimension(12, 22);
      setMinimumSize(size);
      setMaximumSize(size);
      setPreferredSize(size);
    }
    
    public boolean
    isSelected()
    {
      return pIsSelected;
    }

    public void 
    setSelected
    (
     boolean tf
    ) 
    {
      pIsSelected = tf;

      if(pIsActive) {
	if(pIsSelected) 
	  setIcon(sFileArrowIconSelected);
	else 
	  setIcon(sFileArrowIcon);
      }
      else {
	setIcon(sFileArrowIconDisabled);
      }
    }

    private static final long serialVersionUID = 1437991126704684880L;

    private boolean  pIsSelected; 
    private boolean  pIsActive;
  }

  /**
   * A JCheckBox used to select a checked-in link.
   */ 
  private 
  class JLinkCheckBox
    extends JCheckBox
  {
    public 
    JLinkCheckBox
    (
     LinkVersion link
    ) 
    {
      super();

      pLink = link;

      setName("FileCheck");
      setSelected(false);
      
      Dimension size = new Dimension(12, 19);
      setMinimumSize(size);
      setMaximumSize(size);
      setPreferredSize(size);
    }

    public LinkVersion
    getLink() 
    {
      return pLink; 
    }

    private static final long serialVersionUID = 1623467331258706403L;

    private LinkVersion  pLink; 
  }



  /*----------------------------------------------------------------------------------------*/
  
  /**
   * A panel which displays the current state and revision history of an upstream link.
   */ 
  private 
  class JLinksPanel 
    extends JPanel 
    implements Scrollable
  {
    /*--------------------------------------------------------------------------------------*/
    /*   C O N S T R U C T O R                                                              */
    /*--------------------------------------------------------------------------------------*/
  
    /**
     * Construct a new panel.
     * 
     * @param parent
     *   The parent panel.
     * 
     * @param name
     *   The fully resolved name of the upstream node.
     * 
     */ 
    public 
    JLinksPanel
    (
     JNodeLinksPanel parent,
     String lname, 
     boolean isModifiable
    ) 
    {
      super();
      
      ArrayList<VersionID> rvids = new ArrayList<VersionID>(pLinks.keySet());
      Collections.reverse(rvids);

      ArrayList<LinkVersion> links = new ArrayList<LinkVersion>();
      for(VersionID vid : rvids) 
	links.add(pLinks.get(vid).get(lname));

      ArrayList<JComponent[]> vcomps = new ArrayList<JComponent[]>(); 
      pLinkComps.put(lname, vcomps);

      {
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	
	{
	  Box hbox = new Box(BoxLayout.X_AXIS);

	  hbox.add(Box.createRigidArea(new Dimension(27, 0)));

	  int wk = 0;
	  for(VersionID vid : rvids) {
	    JComponent comps[] = null;
	    LinkVersion link = pLinks.get(vid).get(lname);
	    if(link != null) {
	      comps = new JComponent[5];
	      
	      { 
		LinkVersion rlink = null;
		if((wk+1) < links.size()) 
		  rlink = links.get(wk+1);
		
		LinkVersion llink = null;
		if(wk > 0) 
		  llink = links.get(wk-1);
		
		if(link.equals(rlink)) {
		  boolean isExtended = link.equals(llink); 
		  if(!isExtended) 
		    hbox.add(Box.createRigidArea(new Dimension(2, 0)));	

		  JLinkBar bar = new JLinkBar(isExtended);
		  comps[0] = bar; 

		  hbox.add(bar); 
		}
		else {
		  if(!link.equals(llink)) 
		    hbox.add(Box.createRigidArea(new Dimension(2, 0)));	

		  JLinkCheckBox check = new JLinkCheckBox(link);
		  comps[0] = check;
		  
		  if(isModifiable) {
		    check.addActionListener(parent);
		    check.setActionCommand("link-check:" + lname + ":" + vid);
		  }
		  else {
		    check.setEnabled(false);
		  }

		  hbox.add(check);
		  hbox.add(Box.createRigidArea(new Dimension(56, 0)));
		}
	      }
	    }
	    else {
	      hbox.add(Box.createRigidArea(new Dimension(70, 0)));
	    }
	  
	    vcomps.add(comps); 
	    wk++;
	  }

	  add(hbox);
	}

	add(Box.createRigidArea(new Dimension(0, 3)));
	    
	{
	  Box hbox = new Box(BoxLayout.X_AXIS);

	  hbox.add(Box.createRigidArea(new Dimension(1, 0)));

	  int wk = 0;
	  for(VersionID vid : rvids) {
	    LinkVersion link = pLinks.get(vid).get(lname);
	    if(link != null) {
	      JComponent comps[] = vcomps.get(wk);
	      
	      if(comps[0] instanceof JLinkCheckBox) {	      
		Box vbox = new Box(BoxLayout.Y_AXIS);
	      
		{
		  JTextField field = 
		    UIFactory.createTextField("v" + link.getVersionID(), 67, JLabel.CENTER);
		  comps[1] = field;

		  vbox.add(field);
		}
		
		vbox.add(Box.createRigidArea(new Dimension(0, 3)));
		
		{
		  String text = null;
		  switch(link.getPolicy()) {
		  case Reference: 
		    text = "Ref";
		    break;
		    
		  case Dependency:
		    text = "Dep";
		  }
		  
		  JTextField field = 
		    UIFactory.createTextField(text, 67, JLabel.CENTER);
		  comps[2] = field;
		  
		  vbox.add(field);
		}
		
		vbox.add(Box.createRigidArea(new Dimension(0, 3)));
		
		{
		  JTextField field = 
		    UIFactory.createTextField(link.getRelationship().toTitle(), 
					      67, JLabel.CENTER);
		  comps[3] = field;
		
		  vbox.add(field);
		}
		
		vbox.add(Box.createRigidArea(new Dimension(0, 3)));
	      
		{
		  String text = "-";
		  Integer offset = link.getFrameOffset();
		  if(offset != null) 
		    text = offset.toString();
		  
		  JTextField field = 
		  UIFactory.createTextField(text, 67, JLabel.CENTER);
		  comps[4] = field;
		  
		  vbox.add(field);
		}
		
		hbox.add(vbox);

		if((wk+1) < vcomps.size())
		  hbox.add(Box.createRigidArea(new Dimension(3, 0)));
	      }
	      else if(comps[0] instanceof JLinkBar) {
		JLinkBar vbar = (JLinkBar) comps[0];
		
		Box vbox = new Box(BoxLayout.Y_AXIS);

		boolean isExtended = vbar.isExtended();
		int bk;
		for(bk=0; bk<4; bk++) {
		  JExtraLinkBar bar = new JExtraLinkBar(isExtended);
		  comps[bk+1] = bar; 

		  vbox.add(bar);

		  if(bk<3) 
		    vbox.add(Box.createRigidArea(new Dimension(0, 3)));
		}

		hbox.add(vbox);
	      } 
	    }
	    else {
	      hbox.add(Box.createRigidArea(new Dimension(70, 84)));
	    }
	  
	    wk++;
	  }
	  
	  hbox.add(Box.createRigidArea(new Dimension(3, 0)));

	  add(hbox);
	}
	  
	Dimension size = new Dimension(70*pLinks.size(), 106);
	setMinimumSize(size);
	setMaximumSize(size);
	setPreferredSize(size);
	
	pViewportSize = new Dimension(70, 22);
      }
    }
  
    /*--------------------------------------------------------------------------------------*/
    /*   S C R O L L A B L E                                                                */
    /*--------------------------------------------------------------------------------------*/
    
    /**
     * Returns the preferred size of the viewport for a view component.
     */ 
    public Dimension 	
    getPreferredScrollableViewportSize()
    {
      return pViewportSize;
    }
    
    /**
     * Components that display logical rows or columns should compute the scroll increment 
     * that will completely expose one block of rows or columns, depending on the value of 
     * orientation.
     */ 
    public int 	
    getScrollableBlockIncrement
    (
     Rectangle visibleRect, 
     int orientation, 
     int direction
    )
    {
      return getScrollableUnitIncrement(visibleRect, orientation, direction);
    }

    /**
     * Components that display logical rows or columns should compute the scroll increment 
     * that will completely expose one new row or column, depending on the value of 
     * orientation.
     */ 
    public int 	
    getScrollableUnitIncrement
    (
     Rectangle visibleRect, 
     int orientation, 
     int direction
    )
    {
      switch(orientation) {
      case SwingConstants.VERTICAL:
	return 22;

      case SwingConstants.HORIZONTAL:
	return 70;

      default:
	assert(false);
	return 0;
      }
    }

    /**
     * Return true if a viewport should always force the height of this Scrollable to match 
     * the height of the viewport.
     */
    public boolean 
    getScrollableTracksViewportHeight()
    {
      return true;
    }

    /**
     * Return true if a viewport should always force the width of this Scrollable to match 
     * the width of the viewport.
     */ 
    public boolean 	
    getScrollableTracksViewportWidth()
    {
      return false;
    }


    /*--------------------------------------------------------------------------------------*/
    /*   S T A T I C   I N T E R N A L S                                                    */
    /*--------------------------------------------------------------------------------------*/

    private static final long serialVersionUID = 3431758112709330334L;


    /*--------------------------------------------------------------------------------------*/
    /*   I N T E R N A L S                                                                  */
    /*--------------------------------------------------------------------------------------*/

    /**
     * The preferred viewport dimensions.
     */ 
    private Dimension pViewportSize;
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
      UIMaster.getInstance().super(com, pAuthor, pView);
      setName("JNodeLinksPanel:EditTask");
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
      setName("JNodeLinksPanel:EditTask");
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
      setName("JNodeLinksPanel:QueueJobsTask");
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
      setName("JNodeLinksPanel:PauseJobsTask");

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
      setName("JNodeLinksPanel:ResumeJobsTask");

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
      setName("JNodeLinksPanel:KillJobsTask");

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
      setName("JNodeLinksPanel:RemoveFilesTask");
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
  
  private static final long serialVersionUID = 7734595313030641540L;

  private static final int  sTSize = 120;
  private static final int  sVSize = 156;
  private static final int  sCSize = 208;

  
  private static Icon sLinkBarIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("LinkBarIcon.png"));

  private static Icon sLinkBarExtendIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("LinkBarExtendIcon.png"));

  private static Icon sLinkBarIconSelected = 
    new ImageIcon(LookAndFeelLoader.class.getResource("LinkBarIconSelected.png"));

  private static Icon sLinkBarExtendIconSelected = 
    new ImageIcon(LookAndFeelLoader.class.getResource("LinkBarExtendIconSelected.png"));


  private static Icon sFileBarIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("FileBarIcon.png"));

  private static Icon sFileBarExtendIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("FileBarExtendIcon.png"));

  private static Icon sFileBarIconSelected = 
    new ImageIcon(LookAndFeelLoader.class.getResource("FileBarIconSelected.png"));

  private static Icon sFileBarExtendIconSelected = 
    new ImageIcon(LookAndFeelLoader.class.getResource("FileBarExtendIconSelected.png"));


  private static Icon sFrozenIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("FrozenIcon.png"));

  private static Icon sFileArrowIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("FileArrowIcon.png"));

  private static Icon sFileArrowIconSelected = 
    new ImageIcon(LookAndFeelLoader.class.getResource("FileArrowIconSelected.png"));

  private static Icon sFileArrowIconDisabled = 
    new ImageIcon(LookAndFeelLoader.class.getResource("FileArrowIconDisabled.png"));


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The current node status.
   */ 
  private NodeStatus  pStatus;

  /**
   * The upstream links of all checked-in versions of the given node.
   */ 
  private TreeMap<VersionID,TreeMap<String,LinkVersion>>  pLinks; 


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
  private JMenuItem  pApplyItem;  
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
  private JMenu[]      pEditWithMenus; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The node name/state header.
   */ 
  private JLabel pHeaderIcon;
  private JLabel pHeaderLabel;
  
  /**
   * The fully resolved node name field.
   */ 
  private JTextField pNodeNameField;

  /**
   * An icon which indicates whether the working version is frozen.
   */
  private boolean  pIsFrozen; 
  private JLabel   pFrozenLabel;

  /**
   * The button used to apply changes to the working version of the node.
   */ 
  private JButton  pApplyButton;

  /**
   * The link components box.
   */ 
  private Box  pLinksBox;

  /**
   * The link arrows indexed by fully resolved upstream node name.
   */ 
  private TreeMap<String,JLinkArrow>  pArrows; 

  /**
   * The link policy fields indexed by fully resolved upstream node name.
   */ 
  private TreeMap<String,JCollectionField>  pPolicyFields; 

  /**
   * The link relationship fields indexed by fully resolved upstream node name.
   */ 
  private TreeMap<String,JCollectionField>  pRelationshipFields; 

  /**
   * The frame offset indexed by fully resolved upstream node name.
   */ 
  private TreeMap<String,JIntegerField>  pFrameOffsetFields; 

  /**
   * The link novelty UI components indexed by upstream node name
   */ 
  private TreeMap<String,ArrayList<JComponent[]>>  pLinkComps;

  /**
   * The scroll panel containing the link components.
   */ 
  private JScrollPane  pScroll; 

}
