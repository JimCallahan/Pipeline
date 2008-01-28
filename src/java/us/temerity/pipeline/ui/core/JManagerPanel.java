// $Id: JManagerPanel.java,v 1.45 2008/01/28 11:58:51 jesse Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.core.BaseApp;
import us.temerity.pipeline.core.LockedGlueFile;
import us.temerity.pipeline.core.GlueLockException;
import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   M A N A G E R   P A N E L                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A manager of the high-level panel components which make up the main frame. <P> 
 * 
 * The contents of the panel may be changed by the user to one of a number of predefined 
 * Pipeline panel types.  The panel may be divided into two or more child 
 * <CODE>JManagerPanel</CODE>s using either a {@link JHorzSplitPanel JHorzSplitPanel},
 * {@link JVertSplitPanel JVertSplitPanel} or a {@link JTabbedPanel JTabbedPanel}.  Unless 
 * this panel is the root panel of the main frame, the user may also destroy the panel 
 * possibly causing the parent <CODE>JHorzSplitPanel</CODE>, <CODE>JVertSplitPanel</CODE> or 
 * <CODE>JTabedPanel</CODE> to be destroyed as well.
 */ 
public 
class JManagerPanel
  extends JPanel
  implements Glueable, ComponentListener, ActionListener, MouseListener, KeyListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a manager panel. 
   */
  public 
  JManagerPanel()
  {
    super();
    initUI();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the common user interface components.
   */ 
  private void 
  initUI()
  {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));   

    /* panel layout popup menu */ 
    {
      JMenuItem item;
      
      pPopup = new JPopupMenu();  
 
      {
	JMenu sub = new JMenu("New Window");   
	pPopup.add(sub);  
   
	item = new JMenuItem("Node Browser");
	pNodeBrowserWindowItem = item;
	item.setActionCommand("node-browser-window");
	item.addActionListener(this);
	sub.add(item);  
	
	item = new JMenuItem("Node Viewer");
	pNodeViewerWindowItem = item;
	item.setActionCommand("node-viewer-window");
	item.addActionListener(this);
	sub.add(item);  

	sub.addSeparator();

	item = new JMenuItem("Node Details");
	pNodeDetailsWindowItem = item;
	item.setActionCommand("node-details-window");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Node Files");
	pNodeFilesWindowItem = item;
	item.setActionCommand("node-files-window");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Node Links");
	pNodeLinksWindowItem = item;
	item.setActionCommand("node-links-window");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Node History");
	pNodeHistoryWindowItem = item;
	item.setActionCommand("node-history-window");
	item.addActionListener(this);
	sub.add(item);  
	
	sub.addSeparator();

	item = new JMenuItem("Node Annotations");
	pNodeAnnotationsWindowItem = item;
	item.setActionCommand("node-annotations-window");
	item.addActionListener(this);
	sub.add(item);  

	sub.addSeparator();

	item = new JMenuItem("Queue Stats");
	pJobServerStatsWindowItem = item;
	item.setActionCommand("job-server-stats-window");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Queue Servers");
	pJobServersWindowItem = item;
	item.setActionCommand("job-servers-window");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Queue Slots");
	pJobSlotsWindowItem = item;
	item.setActionCommand("job-slots-window");
	item.addActionListener(this);
	sub.add(item);  

	sub.addSeparator();

	item = new JMenuItem("Job Browser");
	pJobBrowserWindowItem = item;
	item.setActionCommand("job-browser-window");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Job Viewer");
	pJobViewerWindowItem = item;
	item.setActionCommand("job-viewer-window");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Job Details");
	pJobDetailsWindowItem = item;
	item.setActionCommand("job-details-window");
	item.addActionListener(this);
	sub.add(item);  

	sub.addSeparator();

	item = new JMenuItem("None");
	pEmptyWindowItem = item;
	item.setActionCommand("none-window");
	item.addActionListener(this);
	sub.add(item);
      }

      item = new JMenuItem("Rename Window");
      pRenameWindowItem = item;
      item.setActionCommand("rename-window");
      item.addActionListener(this);
      pPopup.add(item);  

      pPopup.addSeparator();
      
      {
	JMenu sub = new JMenu("Panel Type");   
	pPopup.add(sub);  
   
	item = new JMenuItem("Node Browser");
	pNodeBrowserPanelItem = item;
	item.setActionCommand("node-browser");
	item.addActionListener(this);
	sub.add(item);  
	
	item = new JMenuItem("Node Viewer");
	pNodeViewerPanelItem = item;
	item.setActionCommand("node-viewer");
	item.addActionListener(this);
	sub.add(item);  

	sub.addSeparator();

	item = new JMenuItem("Node Details");
	pNodeDetailsPanelItem = item;
	item.setActionCommand("node-details");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Node Files");
	pNodeFilesPanelItem = item;
	item.setActionCommand("node-files");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Node Links");
	pNodeLinksPanelItem = item;
	item.setActionCommand("node-links");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Node History");
	pNodeHistoryPanelItem = item;
	item.setActionCommand("node-history");
	item.addActionListener(this);
	sub.add(item);  
	
	sub.addSeparator();

	item = new JMenuItem("Node Annotations");
	pNodeAnnotationsPanelItem = item;
	item.setActionCommand("node-annotations");
	item.addActionListener(this);
	sub.add(item);  

	sub.addSeparator();

	item = new JMenuItem("Queue Stats");
	pJobServerStatsPanelItem = item;
	item.setActionCommand("job-server-stats");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Queue Servers");
	pJobServersPanelItem = item;
	item.setActionCommand("job-servers");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Queue Slots");
	pJobSlotsPanelItem = item;
	item.setActionCommand("job-slots");
	item.addActionListener(this);
	sub.add(item);  

	sub.addSeparator();

	item = new JMenuItem("Job Browser");
	pJobBrowserPanelItem = item;
	item.setActionCommand("job-browser");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Job Viewer");
	pJobViewerPanelItem = item;
	item.setActionCommand("job-viewer");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Job Details");
	pJobDetailsPanelItem = item;
	item.setActionCommand("job-details");
	item.addActionListener(this);
	sub.add(item);  

	sub.addSeparator();

	item = new JMenuItem("None");
	pEmptyPanelItem = item;
	item.setActionCommand("none");
	item.addActionListener(this);
	sub.add(item);
      }

      {
	JMenu sub = new JMenu("Panel Layout");   
	pPopup.add(sub);  
   
	item = new JMenuItem("Add Tab");
	pAddTabItem = item;
	item.setActionCommand("add-tab");
	item.addActionListener(this);
	sub.add(item);  
	
	item = new JMenuItem("Add Top Tab");
	pAddTopTabItem = item;
	item.setActionCommand("add-top-tab");
	item.addActionListener(this);
	sub.add(item);  
	
	sub.addSeparator();
	
	item = new JMenuItem("Add Left");
	pAddLeftItem = item;
	item.setActionCommand("add-left");
	item.addActionListener(this);
	sub.add(item);  
	
	item = new JMenuItem("Add Right");
	pAddRightItem = item;
	item.setActionCommand("add-right");
	item.addActionListener(this);
	sub.add(item);  

	sub.addSeparator();
	
	item = new JMenuItem("Add Above");
	pAddAboveItem = item;
	item.setActionCommand("add-above");
	item.addActionListener(this);
	sub.add(item);  
	
	item = new JMenuItem("Add Below");
	pAddBelowItem = item;
	item.setActionCommand("add-below");
	item.addActionListener(this);
	sub.add(item);  
      }

      item = new JMenuItem("Change Owner|View...");
      pOwnerViewItem = item;
      item.setActionCommand("change-owner-view");
      item.addActionListener(this);
      pPopup.add(item);  
	
      pPopup.addSeparator();

      item = new JMenuItem("Save Layout");
      pSaveLayoutItem = item;
      item.setActionCommand("save-layout");
      item.addActionListener(this);
      pPopup.add(item);  
      
      item = new JMenuItem("Save Layout As...");
      pSaveLayoutAsItem = item;
      item.setActionCommand("save-layout-as");
      item.addActionListener(this);
      pPopup.add(item);  
      
      {
	JMenu sub = new JMenu("Restore Layout");   
	pRestoreLayoutMenu = sub;

	pPopup.add(sub);  
      }

      {
	JMenu sub = new JMenu("Restore Layout (NS)");   
	pRestoreLayoutNoSelectMenu = sub;

	pPopup.add(sub);  
      }

      item = new JMenuItem("Manage Layouts...");
      pManageLayoutsItem = item;
      item.setActionCommand("manage-layouts");
      item.addActionListener(this);
      pPopup.add(item);  

      item = new JMenuItem("Reset Current...");
      pResetLayoutItem = item;
      item.setActionCommand("reset-layout");
      item.addActionListener(this);
      pPopup.add(item);  

      pPopup.addSeparator();

      item = new JMenuItem("Preferences...");
      pPreferencesItem = item;
      item.setActionCommand("preferences");
      item.addActionListener(this);
      pPopup.add(item);  

      item = new JMenuItem("Default Editors...");
      pDefaultEditorsItem = item;
      item.setActionCommand("default-editors");
      item.addActionListener(this);
      pPopup.add(item);  

      item = new JMenuItem("Update Plugins");
      pUpdatePluginsItem = item;
      item.setActionCommand("update-plugins");
      item.addActionListener(this);
      pPopup.add(item);  
      
      item = new JMenuItem("Launch Builders...");
      pLaunchBuilderItem = item;
      item.setActionCommand("launch-builders");
      item.addActionListener(this);
      pPopup.add(item);

      pPopup.addSeparator();

      {
	JMenu sub = new JMenu("Admin");   
	pPopup.add(sub);  

	item = new JMenuItem("User Privileges...");
	pManagePrivilegesItem = item;
	item.setActionCommand("manage-privileges");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Toolsets...");
	pManageToolsetsItem = item;
	item.setActionCommand("manage-toolsets");
	item.addActionListener(this);
	sub.add(item);  

	sub.addSeparator();

	item = new JMenuItem("License Keys...");
	pLicenseKeysItem = item;
	item.setActionCommand("manage-license-keys");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Selection Keys...");
	pSelectionKeysItem = item;
	item.setActionCommand("manage-selection-keys");
	item.addActionListener(this);
	sub.add(item);
	
	item = new JMenuItem("Hardware Keys...");
	pHardwareKeysItem = item;
	item.setActionCommand("manage-hardware-keys");
	item.addActionListener(this);
	sub.add(item); 

	sub.addSeparator();

	item = new JMenuItem("Server Extensions...");
	pServerExtensionsItem = item;
	item.setActionCommand("server-extensions");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Backup Database...");
	pBackupDatabaseItem = item;
	item.setActionCommand("backup-database");
	item.addActionListener(this);
	sub.add(item);  

	sub.addSeparator();

	item = new JMenuItem("Archive Tool...");
	pArchiveItem = item;
	item.setActionCommand("archive");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Offline Tool...");
	pOfflineItem = item;
	item.setActionCommand("offline");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Restore Tool...");
	pRestoreItem = item;
	item.setActionCommand("restore");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Archive Volumes...");
	pArchiveVolumesItem = item;
	item.setActionCommand("archive-volumes");
	item.addActionListener(this);
	sub.add(item);  

	sub.addSeparator();
	sub.addSeparator();

	item = new JMenuItem("Shutdown Server...");
	pShutdownServerItem = item;
	item.setActionCommand("shutdown");
	item.addActionListener(this);
	sub.add(item);  
      }

      {
	JMenu sub = new JMenu("Help");   
	pPopup.add(sub);  
	
	item = new JMenuItem("About Pipeline...");
	pAboutPipelineItem = item;
	item.setActionCommand("about");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Quick Reference...");
	pQuickReferenceItem = item;
	item.setActionCommand("quick-reference");
	item.addActionListener(this);
	sub.add(item);  
	  
	item = new JMenuItem("User Manual...");
	pUserManualItem = item;
	item.setActionCommand("user-manual");
	item.addActionListener(this);
	sub.add(item);  
	  
	sub.addSeparator();
	
	item = new JMenuItem("Home Page...");
	pHomePageItem = item;
	item.setActionCommand("home-page");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Support Forums...");
	pSupportFormumsItem = item;
	item.setActionCommand("support-forums");
	item.addActionListener(this);
	sub.add(item);  
	  
	item = new JMenuItem("Bug Database...");
	pBugDatabaseItem = item;
	item.setActionCommand("bug-database");
	item.addActionListener(this);
	sub.add(item);  

	sub.addSeparator();
	
	item = new JMenuItem("Site Configuration...");
	pSiteConfigurationItem = item;
	item.setActionCommand("site-configuration");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("License Agreement...");
	pLicenseAgreementItem = item;
	item.setEnabled(true);
	item.setActionCommand("license-agreement");
	item.addActionListener(this);
	sub.add(item);  
      }
      
      item = new JMenuItem("Log History...");
      pShowLogsItem = item;
      item.setActionCommand("show-logs");
      item.addActionListener(this);
      pPopup.add(item);  

      pPopup.addSeparator();
      pPopup.addSeparator();
      
      item = new JMenuItem("Quit...");
      pQuitItem = item;
      item.setActionCommand("quit");
      item.addActionListener(this);
      pPopup.add(item);  
    }


    /* group popup menu */ 
    {
      JMenuItem item;
      
      pGroupPopup = new JPopupMenu();  
      pGroupItems = new JMenuItem[10];

      int wk;
      for(wk=0; wk<10; wk++) {
	item = new JMenuItem();
	pGroupItems[wk] = item;

	item.setIcon(sGroupIcons[wk]);
	item.setDisabledIcon(sGroupDisabledIcons[wk]);
	item.setActionCommand("group:" + wk);
	item.addActionListener(this);

	pGroupPopup.add(item);  
      }
    }
    
    /* set the initial tool tips */ 
    updateMenuToolTips();
    
    /* panel title bar */ 
    {
      JPanel tpanel = new JPanel();
      pTitlePanel = tpanel;

      tpanel.setName("PanelBar");
      tpanel.setLayout(new BoxLayout(tpanel, BoxLayout.Y_AXIS)); 

      {
	JLabel label = UIFactory.createLabel("X", 120, JLabel.LEFT);
	pTypeLabel = label;

	label.setName("PanelTypeLabel");
	label.setVisible(UserPrefs.getInstance().getShowPanelLabels());

	label.setAlignmentX(0.5f);

	tpanel.add(label);
      }

      {
	JPanel panel = new JPanel();
	
	panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS)); 

	panel.setMinimumSize(new Dimension(222, 26));
	panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
	panel.setPreferredSize(new Dimension(222, 26));
	
	panel.addMouseListener(this);
	panel.setFocusable(true);
	panel.addKeyListener(this);
	panel.addMouseListener(new KeyFocuser());

	{
	  PopupMenuAnchor anchor = new PopupMenuAnchor(this);
	  pPopupMenuAnchor = anchor; 
	  
	  anchor.addMouseListener(this);
	  anchor.setToolTipText(UIFactory.formatToolTip("The main menu."));
	  panel.add(anchor);
	}
	
	panel.add(Box.createRigidArea(new Dimension(8, 0)));
	
	{
	  GroupMenuAnchor anchor = new GroupMenuAnchor();
	  pGroupMenuAnchor = anchor;
	  
	  anchor.addMouseListener(this);
	  anchor.setToolTipText(UIFactory.formatToolTip("The panel group selector."));
	  panel.add(anchor);	
	}
	
	panel.add(Box.createRigidArea(new Dimension(4, 0)));
	
	{
	  JTextField field = UIFactory.createTextField(null, 120, JLabel.CENTER);
	  pOwnerViewField = field;
	  
	  field.addMouseListener(this);
	  field.setToolTipText(UIFactory.formatToolTip
			       ("The working area Owner|View associated with the panel."));
	  panel.add(field);
	}
	
	panel.add(Box.createRigidArea(new Dimension(4, 0)));
	
	{
	  JLabel label = new JLabel();
	  pLockedLight = label;
	  
	  Dimension size = new Dimension(19, 19);
	  label.setMinimumSize(size);
	  label.setMaximumSize(size);
	  label.setPreferredSize(size);
	  
	  label.addMouseListener(this);
	  label.setToolTipText(UIFactory.formatToolTip
			       ("Indicator of whether the panel is locked (read-only)."));

	  panel.add(label);
	}
	
	panel.add(Box.createRigidArea(new Dimension(8, 0)));
	
	{
	  JButton btn = new JButton();
	  btn.setName("CloseButton");
	
	  Dimension size = new Dimension(15, 19);
	  btn.setMinimumSize(size);
	  btn.setMaximumSize(size);
	  btn.setPreferredSize(size);
	  
	  btn.setActionCommand("close-panel");
	  btn.addActionListener(this);
	  btn.addMouseListener(this);
	  
	  btn.setToolTipText(UIFactory.formatToolTip("Closes the panel."));
	  
	  panel.add(btn);
	} 

	tpanel.add(panel);
      }
    }

    addComponentListener(this); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the top-level frame containing this panel.
   */ 
  public Frame
  getTopFrame() 
  {
    Frame frame = null;
    {
      Container contain = getParent();
      while(true) {
	if(contain == null)
	  break;

	if(contain instanceof Frame) {
	  frame = (Frame) contain;
	  break;
	}
	
	contain = contain.getParent();
      }
    }
    
    assert(frame != null);
    return frame;
  }


  /**
   * Get the panel frame containing this panel.
   * 
   * @return 
   *   The frame or <CODE>null</CODE> if the containing window is not a JPanelFrame.
   */ 
  public JPanelFrame
  getPanelFrame() 
  {
    JPanelFrame frame = null;
    {
      Container contain = getParent();
      while(true) {
	if(contain == null)
	  break;

	if(contain instanceof JPanelFrame) {
	  frame = (JPanelFrame) contain;
	  break;
	}
	
	contain = contain.getParent();
      }
    }

    return frame;
  }


  /**
   * Get body component of the panel.
   */ 
  public Component
  getContents() 
  {
    int idx = getComponentCount()-1;
    if(idx == -1) 
      return null;
    return getComponent(idx);
  }

  /**
   * Set body component of the panel.
   * 
   * @param child
   *   The new child component.
   */ 
  public void 
  setContents
  (
   Component child
  ) 
  { 
    if(child == null)
      return;

    removeContents();

    if((child instanceof JHorzSplitPanel) || 
       (child instanceof JVertSplitPanel) || 
       (child instanceof JTabbedPanel)) {
      pTopLevelPanel = null;
    }
    else if(child instanceof JTopLevelPanel) {
      pTopLevelPanel = (JTopLevelPanel) child;
      pTopLevelPanel.setManager(this);

      updateTitlePanel();
      add(pTitlePanel);
    }
    else {
      assert(false);
    }

    add(child);
    
    validate();
    repaint();
  }
    
  /**
   * Remove the body component of the panel.
   * 
   * @return 
   *   The removed child or <CODE>null</CODE> if there was no child.
   */ 
  public Component
  removeContents() 
  { 
    Component body = getContents();
    removeAll();
    return body;
  }



  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Recursively remove the existing panels from their panel groups.
   */ 
  public void 
  releasePanelGroups()
  {
    if(pTopLevelPanel != null) {
      pTopLevelPanel.setGroupID(0);
      pTopLevelPanel.freeDisplayLists();
    }
    else {
      Component contents = getContents();
      if(contents instanceof JHorzSplitPanel) {
	JHorzSplitPanel split = (JHorzSplitPanel) contents;
	
	{
	  JManagerPanel mpanel = (JManagerPanel) split.getLeftComponent();
	  mpanel.releasePanelGroups();
	}
	
	{
	  JManagerPanel mpanel = (JManagerPanel) split.getRightComponent();
	  mpanel.releasePanelGroups();
	}
      }
      else if(contents instanceof JVertSplitPanel) { 
	JVertSplitPanel split = (JVertSplitPanel) contents;
	
	{
	  JManagerPanel mpanel = (JManagerPanel) split.getBottomComponent();
	  mpanel.releasePanelGroups();
	}
	
	{
	  JManagerPanel mpanel = (JManagerPanel) split.getTopComponent();
	  mpanel.releasePanelGroups();
	}
      }
      else if(contents instanceof JTabbedPanel) {
	JTabbedPanel tab = (JTabbedPanel) contents;
	
	int wk;
	for(wk=0; wk<tab.getTabCount(); wk++) {
	  JManagerPanel mpanel = (JManagerPanel) tab.getComponentAt(wk);
	  mpanel.releasePanelGroups();
	}
      }
    }
  }
  
  /**
   * Refocus keyboard events on the child panel which contains the mouse.
   * 
   * @return
   *   Whether a panel has received the focus.
   */ 
  public boolean 
  refocusOnChildPanel() 
  {
    if(pTitlePanel.getMousePosition(true) != null) {
      pTitlePanel.requestFocusInWindow();
      return true;
    }
    else if(pTopLevelPanel != null) {
      return pTopLevelPanel.refocusOnPanel();
    }
    else {
      Component contents = getContents();
      if(contents instanceof JHorzSplitPanel) {
	JHorzSplitPanel split = (JHorzSplitPanel) contents;
	
	{
	  JManagerPanel mpanel = (JManagerPanel) split.getLeftComponent();
	  if(mpanel.refocusOnChildPanel()) 
	    return true;
	}
	
	{
	  JManagerPanel mpanel = (JManagerPanel) split.getRightComponent();
	  if(mpanel.refocusOnChildPanel()) 
	    return true;
	}
      }
      else if(contents instanceof JVertSplitPanel) { 
	JVertSplitPanel split = (JVertSplitPanel) contents;
	
	{
	  JManagerPanel mpanel = (JManagerPanel) split.getBottomComponent();
	  if(mpanel.refocusOnChildPanel()) 
	    return true;
	}
	
	{
	  JManagerPanel mpanel = (JManagerPanel) split.getTopComponent();
	  if(mpanel.refocusOnChildPanel()) 
	    return true;
	}
      }
      else if(contents instanceof JTabbedPanel) {
	JTabbedPanel tab = (JTabbedPanel) contents;
	
	int wk;
	for(wk=0; wk<tab.getTabCount(); wk++) {
	  JManagerPanel mpanel = (JManagerPanel) tab.getComponentAt(wk);
	  if(mpanel.refocusOnChildPanel()) 
	    return true;
	}
      }
    }

    return false;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the components which make up the title panel to reflect the current state of 
   * the top level panel contents.
   */ 
  public void 
  updateTitlePanel()
  {
    if(pTopLevelPanel == null) 
      return; 

    pTypeLabel.setText(" " + pTopLevelPanel.getTypeName());

    pGroupMenuAnchor.setIcon(sGroupIcons[pTopLevelPanel.getGroupID()]);
    pOwnerViewField.setText(pTopLevelPanel.getTitle());
    pLockedLight.setIcon(pTopLevelPanel.isLocked() ? sLockedLightOnIcon : sLockedLightIcon);
  }

  /**
   * Recursively update all child panels to reflect new user preferences.
   */ 
  public void 
  updateUserPrefs() 
  {
    Component comp = getContents();
    if(comp != null) {
      if(comp instanceof JHorzSplitPanel) {
	JHorzSplitPanel panel = (JHorzSplitPanel) comp;

	JManagerPanel left = (JManagerPanel) panel.getLeftComponent();
	left.updateUserPrefs();

	JManagerPanel right = (JManagerPanel) panel.getRightComponent();
	right.updateUserPrefs();
      }
      else if(comp instanceof JVertSplitPanel) {
	JVertSplitPanel panel = (JVertSplitPanel) comp;

	JManagerPanel top = (JManagerPanel) panel.getTopComponent();
	top.updateUserPrefs();

	JManagerPanel bottom = (JManagerPanel) panel.getBottomComponent();
	bottom.updateUserPrefs();
      }
      else if(comp instanceof JTabbedPanel) {
	JTabbedPanel panel = (JTabbedPanel) comp;

	int wk;
	for(wk=0; wk<panel.getTabCount(); wk++) {
	  JManagerPanel tab = (JManagerPanel) panel.getComponentAt(wk);
	  tab.updateUserPrefs();
	}
      }
      else if(comp instanceof JTopLevelPanel) {
	JTopLevelPanel panel = (JTopLevelPanel) comp;
	panel.updateUserPrefs();

	pTypeLabel.setVisible(UserPrefs.getInstance().getShowPanelLabels());
      }
      else {
	assert(false);
      }
    }

    updateMenuToolTips();
  }

  /**
   * Update the menu item tool tips.
   */ 
  private void 
  updateMenuToolTips() 
  {
    UserPrefs prefs = UserPrefs.getInstance();

    /* windows */
    updateMenuToolTip
      (pNodeBrowserWindowItem, prefs.getManagerNodeBrowserWindow(), 
       "Create a new window containing a Node Browser panel.");
    updateMenuToolTip
      (pNodeViewerWindowItem, prefs.getManagerNodeViewerWindow(), 
       "Create a new window containing a Node Viewer panel.");

    updateMenuToolTip
      (pNodeDetailsWindowItem, prefs.getManagerNodeDetailsWindow(), 
       "Create a new window containing a Node Details panel.");
    updateMenuToolTip
      (pNodeFilesWindowItem, prefs.getManagerNodeFilesWindow(), 
       "Create a new window containing a Node Files panel.");
    updateMenuToolTip
      (pNodeLinksWindowItem, prefs.getManagerNodeLinksWindow(), 
       "Create a new window containing a Node Links panel.");
    updateMenuToolTip
      (pNodeHistoryWindowItem, prefs.getManagerNodeHistoryWindow(), 
       "Create a new window containing a Node History panel.");

    updateMenuToolTip
      (pNodeAnnotationsWindowItem, prefs.getManagerNodeAnnotationsWindow(), 
       "Create a new window containing a Node Annotations panel.");

    updateMenuToolTip
      (pJobServerStatsWindowItem, prefs.getManagerJobServerStatsWindow(), 
       "Create a new window containing a Queue Stats panel.");
    updateMenuToolTip
      (pJobServersWindowItem, prefs.getManagerJobServersWindow(), 
       "Create a new window containing a Queue Servers panel.");
    updateMenuToolTip
      (pJobSlotsWindowItem, prefs.getManagerJobSlotsWindow(), 
       "Create a new window containing a Queue Slots panel.");

    updateMenuToolTip
      (pJobBrowserWindowItem, prefs.getManagerJobBrowserWindow(), 
       "Create a new window containing a Job Browser panel.");
    updateMenuToolTip
      (pJobViewerWindowItem, prefs.getManagerJobViewerWindow(), 
       "Create a new window containing a Job Viewer panel.");
    updateMenuToolTip
      (pJobDetailsWindowItem, prefs.getManagerJobDetailsWindow(), 
       "Create a new window containing a Job Details panel.");
    updateMenuToolTip
      (pEmptyWindowItem, prefs.getManagerEmptyWindow(), 
       "Create a new window containing an empty panel.");

    updateMenuToolTip
      (pRenameWindowItem, prefs.getManagerRenameWindow(), 
       "Rename the current window.");

    /* panel type */ 
    updateMenuToolTip
      (pNodeBrowserPanelItem, prefs.getManagerNodeBrowserPanel(), 
       "Change the panel type to a Node Browser panel.");
    updateMenuToolTip
      (pNodeViewerPanelItem, prefs.getManagerNodeViewerPanel(), 
       "Change the panel type to a Node Viewer panel.");

    updateMenuToolTip
      (pNodeDetailsPanelItem, prefs.getManagerNodeDetailsPanel(), 
       "Change the panel type to a Node Details panel.");
    updateMenuToolTip
      (pNodeFilesPanelItem, prefs.getManagerNodeFilesPanel(), 
       "Change the panel type to a Node Files panel.");
    updateMenuToolTip
      (pNodeLinksPanelItem, prefs.getManagerNodeLinksPanel(), 
       "Change the panel type to a Node Links panel.");
    updateMenuToolTip
      (pNodeHistoryPanelItem, prefs.getManagerNodeHistoryPanel(), 
       "Change the panel type to a Node History panel.");

    updateMenuToolTip
      (pNodeAnnotationsPanelItem, prefs.getManagerNodeAnnotationsPanel(), 
       "Change the panel type to a Node Annotations panel.");

    updateMenuToolTip
      (pJobServerStatsPanelItem, prefs.getManagerJobServerStatsPanel(), 
       "Change the panel type to a Queue Stats panel.");
    updateMenuToolTip
      (pJobServersPanelItem, prefs.getManagerJobServersPanel(), 
       "Change the panel type to a Queue Servers panel.");
    updateMenuToolTip
      (pJobSlotsPanelItem, prefs.getManagerJobSlotsPanel(), 
       "Change the panel type to a Queue Slots panel.");

    updateMenuToolTip
      (pJobBrowserPanelItem, prefs.getManagerJobBrowserPanel(), 
       "Change the panel type to a Job Browser panel.");
    updateMenuToolTip
      (pJobViewerPanelItem, prefs.getManagerJobViewerPanel(), 
       "Change the panel type to a Job Viewer panel.");
    updateMenuToolTip
      (pJobDetailsPanelItem, prefs.getManagerJobDetailsPanel(), 
       "Change the panel type to a Job Details panel.");
    updateMenuToolTip
      (pEmptyPanelItem, prefs.getManagerEmptyPanel(), 
       "Change the panel type to an empty panel.");

    /* panel layout */ 
    updateMenuToolTip
      (pAddTabItem, prefs.getManagerAddTab(), 
       "Add a tabbed panel.");
    updateMenuToolTip
      (pAddTopTabItem, prefs.getManagerAddTopTab(), 
       "Add a tab to the containing tabbed panel.");
    updateMenuToolTip
      (pAddLeftItem, prefs.getManagerAddLeft(), 
       "Split the panel horizontally adding a new panel left.");
    updateMenuToolTip
      (pAddRightItem, prefs.getManagerAddRight(), 
       "Split the panel horizontally adding a new panel right.");
    updateMenuToolTip
      (pAddAboveItem, prefs.getManagerAddAbove(), 
       "Split the panel vertically adding a new panel above.");
    updateMenuToolTip
      (pAddBelowItem, prefs.getManagerAddBelow(), 
       "Split the panel vertically adding a new panel below.");
 
    updateMenuToolTip
      (pOwnerViewItem, prefs.getManagerChangeOwnerView(), 
       "Change the working area view of the panel.");
    
    /* layouts */ 
    updateMenuToolTip
      (pSaveLayoutItem, prefs.getSaveLayout(), 
       "Save the current panel layout.");
    updateMenuToolTip
      (pSaveLayoutAsItem, prefs.getSaveLayoutAs(), 
       "Save the current panel layout with a new name.");
    updateMenuToolTip
      (pManageLayoutsItem, prefs.getShowManageLayouts(), 
       "Manage the saved panel layouts."); 
    updateMenuToolTip
      (pResetLayoutItem, prefs.getResetLayout(), 
       "Reset the current layout to a standardized panel layout."); 
   
    /* admin */ 
    updateMenuToolTip
      (pPreferencesItem, prefs.getShowUserPrefs(), 
       "Edit the user preferences.");
    updateMenuToolTip
      (pDefaultEditorsItem, prefs.getShowDefaultEditors(), 
       "Manage the default editor for filename suffix."); 
    updateMenuToolTip
      (pUpdatePluginsItem, prefs.getUpdatePlugins(), 
       "Make sure that the latest plugins and plugin menus are being used.");
    updateMenuToolTip
      (pLaunchBuilderItem, prefs.getLaunchBuilders(), 
       "Opens up a dialog allowing the selection and invocation of all installed builders.");

    updateMenuToolTip
      (pManagePrivilegesItem, prefs.getShowManagePrivileges(), 
       "Manage the user privileges.");
    updateMenuToolTip
      (pManageToolsetsItem, prefs.getShowManageToolsets(), 
       "Manage the toolset environments.");

    updateMenuToolTip
      (pLicenseKeysItem, prefs.getShowManageLicenseKeys(), 
       "Manage the license keys.");
    updateMenuToolTip
      (pSelectionKeysItem, prefs.getShowManageSelectionKeys(), 
       "Manage the selection keys, groups and schedules.");
    updateMenuToolTip
      (pHardwareKeysItem, prefs.getShowManageHardwareKeys(), 
       "Manage the hardware keys and groups.");
    
    updateMenuToolTip
      (pServerExtensionsItem, prefs.getShowManageServerExtensions(), 
       "Manage the Master Server Extensions.");
    updateMenuToolTip
      (pBackupDatabaseItem, null, 
       "Backup the node database.");

    updateMenuToolTip
      (pArchiveItem, null, 
       "Create new node archive volumes.");
    updateMenuToolTip
      (pOfflineItem, null, 
       "Delete files associated with previously archived nodes.");
    updateMenuToolTip
      (pRestoreItem, null, 
       "Restore nodes from previously created archive volumes.");
    updateMenuToolTip
      (pArchiveVolumesItem, null, 
       "Browse the contents of previously created archive volumes.");
    updateMenuToolTip
      (pShutdownServerItem, null, 
       "Shutdown the Pipeline server daemons.");

    /* help */ 
    updateMenuToolTip
      (pAboutPipelineItem, prefs.getShowAbout(), 
       "Information about Pipeline.");
    updateMenuToolTip
      (pQuickReferenceItem, prefs.getShowQuickReference(), 
       "Display the node state quick reference page.");
    updateMenuToolTip
      (pUserManualItem, prefs.getShowUserManual(), 
       "Display the Pipeline User Manual.");
    updateMenuToolTip
      (pHomePageItem, prefs.getShowHomePage(), 
       "Display the Pipeline Home page.");
    updateMenuToolTip
      (pSupportFormumsItem, prefs.getShowSupportForums(), 
       "Display the Support Forums page.");
    updateMenuToolTip
      (pBugDatabaseItem, prefs.getShowBugDatabase(), 
       "Display the Bug Database page.");
    
    updateMenuToolTip
      (pSiteConfigurationItem, prefs.getShowConfig(), 
       "The local site configuration information.");
    updateMenuToolTip
      (pLicenseAgreementItem, prefs.getShowLicenseAgreement(), 
       "Display the Pipeline license agreement.");

    updateMenuToolTip
      (pShowLogsItem, null, 
       "Display a history of log messages.");

    updateMenuToolTip
      (pQuitItem, prefs.getQuit(), 
       "Quit.");
    
    /* panel groups */ 
    updateMenuToolTip
      (pGroupItems[0], prefs.getManagerGroup0(), 
       "No panel group.");
    updateMenuToolTip
      (pGroupItems[1], prefs.getManagerGroup1(), 
       "Set the panel group to (1).");
    updateMenuToolTip
      (pGroupItems[2], prefs.getManagerGroup2(), 
       "Set the panel group to (2).");
    updateMenuToolTip
      (pGroupItems[3], prefs.getManagerGroup3(), 
       "Set the panel group to (3).");
    updateMenuToolTip
      (pGroupItems[4], prefs.getManagerGroup4(), 
       "Set the panel group to (4).");
    updateMenuToolTip
      (pGroupItems[5], prefs.getManagerGroup5(), 
       "Set the panel group to (5).");
    updateMenuToolTip
      (pGroupItems[6], prefs.getManagerGroup6(), 
       "Set the panel group to (6).");
    updateMenuToolTip
      (pGroupItems[7], prefs.getManagerGroup7(), 
       "Set the panel group to (7).");
    updateMenuToolTip
      (pGroupItems[8], prefs.getManagerGroup8(), 
       "Set the panel group to (8).");
    updateMenuToolTip
      (pGroupItems[9], prefs.getManagerGroup9(), 
       "Set the panel group to (9).");
  }

  /**
   * Update the tool tip for the given menu item.
   */   
  private void 
  updateMenuToolTip
  (
   JMenuItem item, 
   HotKey key,
   String desc
  ) 
  {
    String text = null;
    if(UserPrefs.getInstance().getShowMenuToolTips()) {
      if(desc != null) {
	if(key != null) 
	  text = (desc + " <P>Hot Key = " + key);
	else 
	  text = desc;
      }
      else {
	text = ("Hot Key = " + key);
      }
    }
    
    if(text != null) 
      item.setToolTipText(UIFactory.formatToolTip(text));
    else 
      item.setToolTipText(null);
  }
  

  /**
   * Reset the caches of toolset plugins and plugin menu layouts.
   */ 
  public void 
  clearPluginCache()
  {
    Component comp = getContents();
    if(comp != null) {
      if(comp instanceof JHorzSplitPanel) {
	JHorzSplitPanel panel = (JHorzSplitPanel) comp;

	JManagerPanel left = (JManagerPanel) panel.getLeftComponent();
	left.clearPluginCache();

	JManagerPanel right = (JManagerPanel) panel.getRightComponent();
	right.clearPluginCache();
      }
      else if(comp instanceof JVertSplitPanel) {
	JVertSplitPanel panel = (JVertSplitPanel) comp;

	JManagerPanel top = (JManagerPanel) panel.getTopComponent();
	top.clearPluginCache();

	JManagerPanel bottom = (JManagerPanel) panel.getBottomComponent();
	bottom.clearPluginCache();
      }
      else if(comp instanceof JTabbedPanel) {
	JTabbedPanel panel = (JTabbedPanel) comp;

	int wk;
	for(wk=0; wk<panel.getTabCount(); wk++) {
	  JManagerPanel tab = (JManagerPanel) panel.getComponentAt(wk);
	  tab.clearPluginCache();
	}
      }
      else if(comp instanceof JTopLevelPanel) {
	JTopLevelPanel panel = (JTopLevelPanel) comp;
	panel.clearPluginCache();
      }
      else {
	assert(false);
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- COMPONENT LISTENER METHODS ----------------------------------------------------------*/

  /**
   * Invoked when the component has been made invisible. 
   */ 
  public void 	
  componentHidden
  (
   ComponentEvent e
  )
  {}
  
  /**
   * Invoked when the component's position changes. 
   */ 
  public void 	
  componentMoved
  (
   ComponentEvent e
  )
  {}

  /**
   * Invoked when the component's size changes. <P> 
   * 
   * This method is used to hide the body component when the panel collapsed by a parent
   * {@link JHorzSplitPanel JHorzSplitPanel} or {@link JVertSplitPanel JVertSplitPanel}.  
   * This is required to get around a rendering problem where heavyweight body components 
   * of this panel are incorrectly rendered over lightweight components still visible.  
   */ 
  public void 	
  componentResized
  (
   ComponentEvent e
  )
  {
    Component body = getContents();
    if(body != null) {
      if(body.isVisible()) {
	if((getWidth() == 0) || (getHeight() == 0)) {
	  body.setVisible(false);
	}
      }
      else {
	if((getWidth() > 0) && (getHeight() > 0)) {
	  body.setVisible(true);
	  validate();
	}
      }
    }
  }

  /**
   * Invoked when the component has been made visible. 
   */ 
  public void 	
  componentShown
  (
   ComponentEvent e
  )
  {}


  /*-- MOUSE LISTNER METHODS -------------------------------------------------------------*/
  
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
    handleManagerMouseEvent(e);
  }

  /**
   * Invoked when a mouse button has been released on a component. 
   */ 
  public void 
  mouseReleased(MouseEvent e) {}

  /**
   * Handle manager panel related mouse events.
   * 
   * @return 
   *   Whether the event was handled.
   */ 
  public boolean
  handleManagerMouseEvent
  (
   MouseEvent e 
  ) 
  {
    int mods = e.getModifiersEx();

    int on[] = {
      (MouseEvent.BUTTON3_DOWN_MASK |
       MouseEvent.ALT_DOWN_MASK),

      (MouseEvent.BUTTON3_DOWN_MASK |
       MouseEvent.CTRL_DOWN_MASK),

      (MouseEvent.BUTTON3_DOWN_MASK |
       MouseEvent.SHIFT_DOWN_MASK),

      (MouseEvent.BUTTON3_DOWN_MASK |
       MouseEvent.ALT_DOWN_MASK |
       MouseEvent.CTRL_DOWN_MASK),

      (MouseEvent.BUTTON3_DOWN_MASK |
       MouseEvent.ALT_DOWN_MASK |
       MouseEvent.SHIFT_DOWN_MASK),

      (MouseEvent.BUTTON3_DOWN_MASK |
       MouseEvent.CTRL_DOWN_MASK |
       MouseEvent.SHIFT_DOWN_MASK),

      (MouseEvent.BUTTON3_DOWN_MASK |
       MouseEvent.ALT_DOWN_MASK |
       MouseEvent.CTRL_DOWN_MASK |
       MouseEvent.SHIFT_DOWN_MASK)
    };

    int off[] = {
      (MouseEvent.BUTTON1_DOWN_MASK | 
       MouseEvent.BUTTON2_DOWN_MASK | 
       MouseEvent.SHIFT_DOWN_MASK |
       MouseEvent.CTRL_DOWN_MASK), 

      (MouseEvent.BUTTON1_DOWN_MASK | 
       MouseEvent.BUTTON2_DOWN_MASK | 
       MouseEvent.ALT_DOWN_MASK |
       MouseEvent.SHIFT_DOWN_MASK), 

      (MouseEvent.BUTTON1_DOWN_MASK | 
       MouseEvent.BUTTON2_DOWN_MASK | 
       MouseEvent.ALT_DOWN_MASK |
       MouseEvent.CTRL_DOWN_MASK), 

      (MouseEvent.BUTTON1_DOWN_MASK | 
       MouseEvent.BUTTON2_DOWN_MASK | 
       MouseEvent.SHIFT_DOWN_MASK), 

      (MouseEvent.BUTTON1_DOWN_MASK | 
       MouseEvent.BUTTON2_DOWN_MASK | 
       MouseEvent.CTRL_DOWN_MASK), 
      
      (MouseEvent.BUTTON1_DOWN_MASK | 
       MouseEvent.BUTTON2_DOWN_MASK | 
       MouseEvent.ALT_DOWN_MASK),

      (MouseEvent.BUTTON1_DOWN_MASK | 
       MouseEvent.BUTTON2_DOWN_MASK)
    }; 


    UserPrefs prefs = UserPrefs.getInstance();

    ArrayList<String> keys = new ArrayList<String>();
    keys.add("ALT");
    keys.add("CTRL");
    keys.add("SHIFT");
    keys.add("ALT+CTRL");
    keys.add("ALT+SHIFT");
    keys.add("CTRL+SHIFT");
    keys.add("ALT+CTRL+SHIFT");
    
    int mainIdx  = keys.indexOf(prefs.getMainMenuPopup());
    int groupIdx = keys.indexOf(prefs.getGroupMenuPopup());

    /* BUTTON3+keys: main manager popup menu */ 
    if((mainIdx > -1) && (mods & (on[mainIdx] | off[mainIdx])) == on[mainIdx]) {
      pPopupMenuAnchor.handleAnchorMouseEvent(e);
      return true;
    }

    /* BUTTON3+keys: panel group popup menu */ 
    else if((groupIdx > -1) && (mods & (on[groupIdx] | off[groupIdx])) == on[groupIdx]) {
      pGroupMenuAnchor.handleAnchorMouseEvent(e);
      return true;
    }

    return false;
  }


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
    handleManagerKeyEvent(e);
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

  /**
   * Handle manager panel related keyboard events.
   * 
   * @return 
   *   Whether the event was handled.
   */ 
  public boolean
  handleManagerKeyEvent
  (
   KeyEvent e 
  ) 
  {
    UserPrefs prefs = UserPrefs.getInstance(); 
    UIMaster master = UIMaster.getInstance(); 

    try {
      /* windows */
      if((prefs.getManagerNodeBrowserWindow() != null) &&
         prefs.getManagerNodeBrowserWindow().wasPressed(e)) {
        doNodeBrowserWindow();
        return true;
      }
      else if((prefs.getManagerNodeViewerWindow() != null) &&
              prefs.getManagerNodeViewerWindow().wasPressed(e)) {
        doNodeViewerWindow();
        return true;
      }

      else if((prefs.getManagerNodeDetailsWindow() != null) &&
              prefs.getManagerNodeDetailsWindow().wasPressed(e)) {
        doNodeDetailsWindow();
        return true;
      }
      else if((prefs.getManagerNodeFilesWindow() != null) &&
              prefs.getManagerNodeFilesWindow().wasPressed(e)) {
        doNodeFilesWindow();
        return true;
      }
      else if((prefs.getManagerNodeLinksWindow() != null) &&
              prefs.getManagerNodeLinksWindow().wasPressed(e)) {
        doNodeLinksWindow();
        return true;
      }
      else if((prefs.getManagerNodeHistoryWindow() != null) &&
              prefs.getManagerNodeHistoryWindow().wasPressed(e)) {
        doNodeHistoryWindow();
        return true;
      }

      else if((prefs.getManagerNodeAnnotationsWindow() != null) &&
              prefs.getManagerNodeAnnotationsWindow().wasPressed(e)) {
        doNodeAnnotationsWindow();
        return true;
      }

      else if((prefs.getManagerJobServerStatsWindow() != null) &&
              prefs.getManagerJobServerStatsWindow().wasPressed(e)) {
        doJobServerStatsWindow();
        return true;
      }
      else if((prefs.getManagerJobServersWindow() != null) &&
              prefs.getManagerJobServersWindow().wasPressed(e)) {
        doJobServersWindow();
        return true;
      }
      else if((prefs.getManagerJobSlotsWindow() != null) &&
              prefs.getManagerJobSlotsWindow().wasPressed(e)) {
        doJobSlotsWindow();
        return true;
      }

      else if((prefs.getManagerJobBrowserWindow() != null) &&
              prefs.getManagerJobBrowserWindow().wasPressed(e)) {
        doJobBrowserWindow();
        return true;
      }
      else if((prefs.getManagerJobViewerWindow() != null) &&
              prefs.getManagerJobViewerWindow().wasPressed(e)) {
        doJobViewerWindow();
        return true;
      }
      else if((prefs.getManagerJobDetailsWindow() != null) &&
              prefs.getManagerJobDetailsWindow().wasPressed(e)) {
        doJobDetailsWindow();
        return true;
      }

      else if((prefs.getManagerEmptyWindow() != null) &&
              prefs.getManagerEmptyWindow().wasPressed(e)) {
        doEmptyWindow();
        return true;
      }

      else if((prefs.getManagerRenameWindow() != null) &&
              prefs.getManagerRenameWindow().wasPressed(e)) {
        doRenameWindow();
        return true;
      }

      /* panels */ 
      else if((prefs.getManagerNodeBrowserPanel() != null) &&
              prefs.getManagerNodeBrowserPanel().wasPressed(e)) {
        doNodeBrowserPanel();
        return true;
      }
      else if((prefs.getManagerNodeViewerPanel() != null) &&
              prefs.getManagerNodeViewerPanel().wasPressed(e)) {
        doNodeViewerPanel();
        return true;
      }

      else if((prefs.getManagerNodeDetailsPanel() != null) &&
              prefs.getManagerNodeDetailsPanel().wasPressed(e)) {
        doNodeDetailsPanel();
        return true;
      }
      else if((prefs.getManagerNodeFilesPanel() != null) &&
              prefs.getManagerNodeFilesPanel().wasPressed(e)) {
        doNodeFilesPanel();
        return true;
      }
      else if((prefs.getManagerNodeLinksPanel() != null) &&
              prefs.getManagerNodeLinksPanel().wasPressed(e)) {
        doNodeLinksPanel();
        return true;
      }
      else if((prefs.getManagerNodeHistoryPanel() != null) &&
              prefs.getManagerNodeHistoryPanel().wasPressed(e)) {
        doNodeHistoryPanel();
        return true;
      }

      else if((prefs.getManagerNodeAnnotationsPanel() != null) &&
              prefs.getManagerNodeAnnotationsPanel().wasPressed(e)) {
        doNodeAnnotationsPanel();
        return true;
      }

      else if((prefs.getManagerJobServerStatsPanel() != null) &&
              prefs.getManagerJobServerStatsPanel().wasPressed(e)) {
        doJobServerStatsPanel();
        return true;
      }
      else if((prefs.getManagerJobServersPanel() != null) &&
              prefs.getManagerJobServersPanel().wasPressed(e)) {
        doJobServersPanel();
        return true;
      }
      else if((prefs.getManagerJobSlotsPanel() != null) &&
              prefs.getManagerJobSlotsPanel().wasPressed(e)) {
        doJobSlotsPanel();
        return true;
      }

      else if((prefs.getManagerJobBrowserPanel() != null) &&
              prefs.getManagerJobBrowserPanel().wasPressed(e)) {
        doJobBrowserPanel();
        return true;
      }
      else if((prefs.getManagerJobViewerPanel() != null) &&
              prefs.getManagerJobViewerPanel().wasPressed(e)) {
        doJobViewerPanel();
        return true;
      }
      else if((prefs.getManagerJobDetailsPanel() != null) &&
              prefs.getManagerJobDetailsPanel().wasPressed(e)) {
        doJobDetailsPanel();
        return true;
      }

      else if((prefs.getManagerEmptyPanel() != null) &&
              prefs.getManagerEmptyPanel().wasPressed(e)) {
        doEmptyPanel();
        return true;
      }

      /* layout */ 
      else if((prefs.getManagerAddTab() != null) &&
              prefs.getManagerAddTab().wasPressed(e)) {
        doAddTab();
        return true;
      }
      else if((prefs.getManagerAddTopTab() != null) &&
              prefs.getManagerAddTopTab().wasPressed(e)) {
        doAddTopTab();
        return true;
      }
      else if((prefs.getManagerAddLeft() != null) &&
              prefs.getManagerAddLeft().wasPressed(e)) {
        doAddLeft();
        return true;
      }
      else if((prefs.getManagerAddRight() != null) &&
              prefs.getManagerAddRight().wasPressed(e)) {
        doAddRight();
        return true;
      }
      else if((prefs.getManagerAddAbove() != null) &&
              prefs.getManagerAddAbove().wasPressed(e)) {
        doAddAbove();
        return true;
      }
      else if((prefs.getManagerAddBelow() != null) &&
              prefs.getManagerAddBelow().wasPressed(e)) {
        doAddBelow();
        return true;
      }
      else if((prefs.getManagerClosePanel() != null) &&
              prefs.getManagerClosePanel().wasPressed(e)) {
        doClosePanel();
        return true;
      }

      /* owner|view */
      else if((prefs.getManagerChangeOwnerView() != null) &&
              prefs.getManagerChangeOwnerView().wasPressed(e)) {
        doChangeOwnerView();
        return true;
      }

      /* panel group */ 
      else if((prefs.getManagerGroup0() != null) &&
              prefs.getManagerGroup0().wasPressed(e)) {
        doGroup(0);
        return true;
      }
      else if((prefs.getManagerGroup1() != null) &&
              prefs.getManagerGroup1().wasPressed(e)) {
        doGroup(1);
        return true;
      }
      else if((prefs.getManagerGroup2() != null) &&
              prefs.getManagerGroup2().wasPressed(e)) {
        doGroup(2);
        return true;
      }
      else if((prefs.getManagerGroup3() != null) &&
              prefs.getManagerGroup3().wasPressed(e)) {
        doGroup(3);
        return true;
      }
      else if((prefs.getManagerGroup4() != null) &&
              prefs.getManagerGroup4().wasPressed(e)) {
        doGroup(4);
        return true;
      }
      else if((prefs.getManagerGroup5() != null) &&
              prefs.getManagerGroup5().wasPressed(e)) {
        doGroup(5);
        return true;
      }
      else if((prefs.getManagerGroup6() != null) &&
              prefs.getManagerGroup6().wasPressed(e)) {
        doGroup(6);
        return true;
      }
      else if((prefs.getManagerGroup7() != null) &&
              prefs.getManagerGroup7().wasPressed(e)) {
        doGroup(7);
        return true;
      }
      else if((prefs.getManagerGroup8() != null) &&
              prefs.getManagerGroup8().wasPressed(e)) {
        doGroup(8);
        return true;
      }
      else if((prefs.getManagerGroup9() != null) &&
              prefs.getManagerGroup9().wasPressed(e)) {
        doGroup(9);
        return true;
      }

      /* UIMaster */ 
      else if((prefs.getSaveLayout() != null) &&
              prefs.getSaveLayout().wasPressed(e)) {
        master.doSaveLayout();
        return true;
      }
      else if((prefs.getSaveLayoutAs() != null) &&
              prefs.getSaveLayoutAs().wasPressed(e)) {
        master.showSaveLayoutDialog();
        return true;
      }
      else if((prefs.getShowManageLayouts() != null) &&
              prefs.getShowManageLayouts().wasPressed(e)) {
        master.showManageLayoutsDialog();
        return true;
      }
      else if((prefs.getSetDefaultLayout() != null) &&
              prefs.getSetDefaultLayout().wasPressed(e)) {
        master.doDefaultLayout();
        return true;
      }

      else if((prefs.getShowUserPrefs() != null) &&
              prefs.getShowUserPrefs().wasPressed(e)) {
        master.showUserPrefsDialog();
        return true;
      }
      else if((prefs.getShowDefaultEditors() != null) &&
              prefs.getShowDefaultEditors().wasPressed(e)) {
        master.showDefaultEditorsDialog();
        return true;
      }
      else if((prefs.getUpdatePlugins() != null) &&
              prefs.getUpdatePlugins().wasPressed(e)) {
        master.clearPluginCache();
        return true;
      }

      else if((prefs.getShowManagePrivileges() != null) &&
              prefs.getShowManagePrivileges().wasPressed(e)) {
        master.showManagePrivilegesDialog();
        return true;
      }
      else if((prefs.getShowManageToolsets() != null) &&
              prefs.getShowManageToolsets().wasPressed(e)) {
        master.showManageToolsetsDialog();
        return true;
      }

      else if((prefs.getShowManageLicenseKeys() != null) &&
              prefs.getShowManageLicenseKeys().wasPressed(e)) {
        master.showManageLicenseKeysDialog();
        return true;
      }
      else if((prefs.getShowManageSelectionKeys() != null) &&
              prefs.getShowManageSelectionKeys().wasPressed(e)) {
        master.showManageSelectionKeysDialog();
        return true;
      }

      else if((prefs.getShowManageServerExtensions() != null) &&
              prefs.getShowManageServerExtensions().wasPressed(e)) {
        master.showManageServerExtensionsDialog();
        return true;
      }

      else if((prefs.getQuit() != null) &&
              prefs.getQuit().wasPressed(e)) {
        master.doQuit();    
        return true;
      }

      /* help */ 
      else if((prefs.getShowAbout() != null) &&
              prefs.getShowAbout().wasPressed(e)) {
        master.showAboutDialog();
        return true;
      }
      else if((prefs.getShowQuickReference() != null) &&
              prefs.getShowQuickReference().wasPressed(e)) {
        BaseApp.showURL("http://temerity.us/products/pipeline/docs/reference/ref.php"); 
        return true;
      }
      else if((prefs.getShowUserManual() != null) &&
              prefs.getShowUserManual().wasPressed(e)) {
        BaseApp.showURL("http://temerity.us/products/pipeline/docs/PipelineManual.pdf");
        return true;
      }

      else if((prefs.getShowHomePage() != null) &&
              prefs.getShowHomePage().wasPressed(e)) {
        BaseApp.showURL("http://www.temerity.us/products/pipeline");
        return true; 
      }
      else if((prefs.getShowSupportForums() != null) &&
              prefs.getShowSupportForums().wasPressed(e)) {
        BaseApp.showURL("http://www.temerity.us/community/forums");
        return true;
      }
      else if((prefs.getShowBugDatabase() != null) &&
              prefs.getShowBugDatabase().wasPressed(e)) {
        BaseApp.showURL("http://www.temerity.us/community/forums/viewforum.php?f=12");  
        return true;
      }
      else if((prefs.getShowLicenseAgreement() != null) &&
              prefs.getShowLicenseAgreement().wasPressed(e)) {
        BaseApp.showURL("http//www.temerity.us/company/license.php");
        return true;
      }
    
      else if((prefs.getShowConfig() != null) &&
              prefs.getShowConfig().wasPressed(e)) {
        master.showConfigDialog();
        return true;
      }

      /* misc */ 
      else if((prefs.getNextTab() != null) &&
              prefs.getNextTab().wasPressed(e)) {
        doNextTab();
        return true;
      }
      else if((prefs.getPrevTab() != null) &&
              prefs.getPrevTab().wasPressed(e)) {
        doPrevTab();
        return true;
      }
    }
    catch(PipelineException ex) {
      master.showErrorDialog(ex);
    }

    return false;
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
    UIMaster master = UIMaster.getInstance(); 

    try {
      /* windows */ 
      String cmd = e.getActionCommand();
      if(cmd.equals("node-browser-window"))
        doNodeBrowserWindow();
      else if(cmd.equals("node-viewer-window"))
        doNodeViewerWindow();

      else if(cmd.equals("node-details-window"))
        doNodeDetailsWindow();
      else if(cmd.equals("node-files-window"))
        doNodeFilesWindow();
      else if(cmd.equals("node-links-window"))
        doNodeLinksWindow();
      else if(cmd.equals("node-history-window"))
        doNodeHistoryWindow();

      else if(cmd.equals("node-annotations-window"))
        doNodeAnnotationsWindow();

      else if(cmd.equals("job-server-stats-window"))
        doJobServerStatsWindow();
      else if(cmd.equals("job-servers-window"))
        doJobServersWindow();
      else if(cmd.equals("job-slots-window"))
        doJobSlotsWindow();

      else if(cmd.equals("job-browser-window"))
        doJobBrowserWindow();
      else if(cmd.equals("job-viewer-window"))
        doJobViewerWindow();
      else if(cmd.equals("job-details-window"))
        doJobDetailsWindow();

      else if(cmd.equals("none-window"))
        doEmptyWindow();

      else if(cmd.equals("rename-window"))
        doRenameWindow();

      /* panels */ 
      else if(cmd.equals("node-browser"))
        doNodeBrowserPanel();
      else if(cmd.equals("node-viewer"))
        doNodeViewerPanel();

      else if(cmd.equals("node-details"))
        doNodeDetailsPanel();
      else if(cmd.equals("node-files"))
        doNodeFilesPanel();
      else if(cmd.equals("node-links"))
        doNodeLinksPanel();
      else if(cmd.equals("node-history"))
        doNodeHistoryPanel();

      else if(cmd.equals("node-annotations"))
        doNodeAnnotationsPanel();

      else if(cmd.equals("job-server-stats"))
        doJobServerStatsPanel();
      else if(cmd.equals("job-servers"))
        doJobServersPanel();
      else if(cmd.equals("job-slots"))
        doJobSlotsPanel();

      else if(cmd.equals("job-browser"))
        doJobBrowserPanel();
      else if(cmd.equals("job-viewer"))
        doJobViewerPanel();
      else if(cmd.equals("job-details"))
        doJobDetailsPanel();

      else if(cmd.equals("none"))
        doEmptyPanel();
    
      /* layout */ 
      else if(cmd.equals("add-left"))
        doAddLeft();
      else if(cmd.equals("add-right"))
        doAddRight();
      else if(cmd.equals("add-above"))
        doAddAbove();
      else if(cmd.equals("add-below"))
        doAddBelow();
      else if(cmd.equals("add-tab"))
        doAddTab();
      else if(cmd.equals("add-top-tab"))
        doAddTopTab();
      else if(cmd.equals("close-panel"))
        doClosePanel();

      /* owner|view */
      else if(cmd.equals("change-owner-view"))
        doChangeOwnerView();
    
      /* panel group */ 
      else if(cmd.startsWith("group:")) 
        doGroup(Integer.valueOf(cmd.substring(6)));
    
      /* UIMaster */ 
      else if(cmd.equals("save-layout"))
        master.doSaveLayout();
      else if(cmd.equals("save-layout-as"))
        master.showSaveLayoutDialog();
      else if(cmd.startsWith("restore-layout:")) 
        master.doRestoreSavedLayout(new Path(cmd.substring(15)), true);
      else if(cmd.startsWith("restore-layout-no-select:")) 
        master.doRestoreSavedLayout(new Path(cmd.substring(25)), false);
      else if(cmd.equals("manage-layouts"))
        master.showManageLayoutsDialog();
      else if(cmd.equals("reset-layout"))
        master.doResetLayout();

      else if(cmd.equals("preferences"))
        master.showUserPrefsDialog();
      else if(cmd.equals("default-editors"))
        master.showDefaultEditorsDialog(); 
      else if(cmd.equals("update-plugins"))
        master.clearPluginCache();
      else if(cmd.equals("launch-builders"))
        master.showBuilderLaunchDialog();

      else if(cmd.equals("manage-privileges"))
        master.showManagePrivilegesDialog();
      else if(cmd.equals("manage-toolsets"))
        master.showManageToolsetsDialog();

      else if(cmd.equals("manage-license-keys"))
        master.showManageLicenseKeysDialog();
      else if(cmd.equals("manage-selection-keys"))
        master.showManageSelectionKeysDialog();
      else if(cmd.equals("manage-hardware-keys"))
        master.showManageHardwareKeysDialog();

      else if(cmd.equals("server-extensions"))
        master.showManageServerExtensionsDialog(); 
      else if(cmd.equals("backup-database"))
        master.showBackupDialog();

      else if(cmd.equals("archive"))
        master.showArchiveDialog();
      else if(cmd.equals("offline"))
        master.showOfflineDialog();
      else if(cmd.equals("restore"))
        master.showRestoreDialog();
      else if(cmd.equals("archive-volumes"))
        master.showArchiveVolumesDialog();
      else if(cmd.equals("shutdown"))  
        doShutdownServer();

      else if(cmd.equals("about"))
        master.showAboutDialog();
      else if(cmd.equals("quick-reference"))
        BaseApp.showURL("http://temerity.us/products/pipeline/docs/reference/ref.php");
      else if(cmd.equals("user-manual"))
        BaseApp.showURL("http://temerity.us/products/pipeline/docs/PipelineManual.pdf");

      else if(cmd.equals("home-page"))
        BaseApp.showURL("http://www.temerity.us");
      else if(cmd.equals("support-forums"))
        BaseApp.showURL("http://www.temerity.us/community/forums");
      else if(cmd.equals("bug-database"))
        BaseApp.showURL("http://www.temerity.us/community/forums/viewforum.php?f=12");  

      else if(cmd.equals("site-configuration"))
        master.showConfigDialog(); 
      else if(cmd.equals("license-agreement"))
        BaseApp.showURL("file:///" + PackageInfo.sInstPath + 
                        "/share/docs/legal/license.html");

      else if(cmd.equals("show-logs"))
        master.showLogsDialog(); 

      else if(cmd.equals("quit"))
        master.doQuit();    
    }
    catch(PipelineException ex) {
      master.showErrorDialog(ex);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Create a new secondary panel frame containing a JNodeBrowserPanel. 
   */ 
  private void 
  doNodeBrowserWindow() 
  {
    JPanelFrame frame = UIMaster.getInstance().createWindow();
    frame.setSize(300, 600);

    JManagerPanel mgr = frame.getManagerPanel();
    JNodeBrowserPanel panel = new JNodeBrowserPanel(pTopLevelPanel);
    mgr.setContents(panel); 

    updateFromNodeViewer(panel.getGroupID());

    frame.validate();
    frame.repaint();
  }

  /** 
   * Create a new secondary panel frame containing a JNodeViewerPanel. 
   */ 
  private void 
  doNodeViewerWindow() 
  {
    JPanelFrame frame = UIMaster.getInstance().createWindow();
    frame.setSize(600, 600);

    JManagerPanel mgr = frame.getManagerPanel();
    JNodeViewerPanel panel = new JNodeViewerPanel(pTopLevelPanel);
    mgr.setContents(panel); 

    updateFromNodeBrowser(panel.getGroupID());

    frame.validate();
    frame.repaint();
  }

  /** 
   * Create a new secondary panel frame containing a JNodeDetailsPanel. 
   */ 
  private void 
  doNodeDetailsWindow() 
  {
    JPanelFrame frame = UIMaster.getInstance().createWindow();
    frame.setSize(612, 752);

    JManagerPanel mgr = frame.getManagerPanel();
    JNodeDetailsPanel panel = new JNodeDetailsPanel(pTopLevelPanel);
    mgr.setContents(panel);

    updateFromNodeViewer(panel.getGroupID());

    frame.validate();
    frame.repaint();
  }

  /** 
   * Create a new secondary panel frame containing a JNodeFilesPanel. 
   */ 
  private void 
  doNodeFilesWindow() 
  {
    JPanelFrame frame = UIMaster.getInstance().createWindow();
    frame.setSize(612, 752);

    JManagerPanel mgr = frame.getManagerPanel();
    JNodeFilesPanel panel = new JNodeFilesPanel(pTopLevelPanel);
    mgr.setContents(panel); 
    
    updateFromNodeViewer(panel.getGroupID());

    frame.validate();
    frame.repaint();
  }

  /** 
   * Create a new secondary panel frame containing a JNodeLinksPanel. 
   */ 
  private void 
  doNodeLinksWindow() 
  {
    JPanelFrame frame = UIMaster.getInstance().createWindow();
    frame.setSize(612, 752);

    JManagerPanel mgr = frame.getManagerPanel();
    JNodeLinksPanel panel = new JNodeLinksPanel(pTopLevelPanel);
    mgr.setContents(panel); 
    
    updateFromNodeViewer(panel.getGroupID());

    frame.validate();
    frame.repaint();
  }

  /** 
   * Create a new secondary panel frame containing a JNodeHistoryPanel. 
   */ 
  private void 
  doNodeHistoryWindow() 
  {
    JPanelFrame frame = UIMaster.getInstance().createWindow();
    frame.setSize(612, 752);

    JManagerPanel mgr = frame.getManagerPanel();
    JNodeHistoryPanel panel = new JNodeHistoryPanel(pTopLevelPanel);
    mgr.setContents(panel); 

    updateFromNodeViewer(panel.getGroupID());

    frame.validate();
    frame.repaint();
  }

  /** 
   * Create a new secondary panel frame containing a JNodeAnnotationsPanel. 
   */ 
  private void 
  doNodeAnnotationsWindow() 
  {
    JPanelFrame frame = UIMaster.getInstance().createWindow();
    frame.setSize(375, 752);

    JManagerPanel mgr = frame.getManagerPanel();
    JNodeAnnotationsPanel panel = new JNodeAnnotationsPanel(pTopLevelPanel);
    mgr.setContents(panel);

    updateFromNodeViewer(panel.getGroupID());

    frame.validate();
    frame.repaint();
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Create a new secondary panel frame containing a JQueueJobServerStatsPanel. 
   */ 
  private void 
  doJobServerStatsWindow() 
  {
    JPanelFrame frame = UIMaster.getInstance().createWindow();
    frame.setSize(1137, 440);

    JManagerPanel mgr = frame.getManagerPanel();
    mgr.setContents(new JQueueJobServerStatsPanel(pTopLevelPanel));

    frame.validate();
    frame.repaint();
  }

  /** 
   * Create a new secondary panel frame containing a JQueueJobServersPanel. 
   */ 
  private void 
  doJobServersWindow() 
  {
    JPanelFrame frame = UIMaster.getInstance().createWindow();
    frame.setSize(1137, 350);

    JManagerPanel mgr = frame.getManagerPanel();
    mgr.setContents(new JQueueJobServersPanel(pTopLevelPanel));

    frame.validate();
    frame.repaint();
  }

  /** 
   * Create a new secondary panel frame containing a JQueueJobSlotsPanel. 
   */ 
  private void 
  doJobSlotsWindow() 
  {
    JPanelFrame frame = UIMaster.getInstance().createWindow();
    frame.setSize(1137, 350);

    JManagerPanel mgr = frame.getManagerPanel();
    mgr.setContents(new JQueueJobSlotsPanel(pTopLevelPanel));

    frame.validate();
    frame.repaint();
  }

  /** 
   * Create a new secondary panel frame containing a JQueueJobBrowserPanel. 
   */ 
  private void 
  doJobBrowserWindow() 
  {
    JPanelFrame frame = UIMaster.getInstance().createWindow();
    frame.setSize(1137, 350);

    JManagerPanel mgr = frame.getManagerPanel();
    mgr.setContents(new JQueueJobBrowserPanel(pTopLevelPanel));

    frame.validate();
    frame.repaint();
  }

  /** 
   * Create a new secondary panel frame containing a JQueueJobViewerPanel. 
   */ 
  private void 
  doJobViewerWindow() 
  {
    JPanelFrame frame = UIMaster.getInstance().createWindow();
    frame.setSize(600, 600);

    JManagerPanel mgr = frame.getManagerPanel();
    mgr.setContents(new JQueueJobViewerPanel(pTopLevelPanel));

    frame.validate();
    frame.repaint();
  }

  /** 
   * Create a new secondary panel frame containing a JQueueJobDetailsPanel. 
   */ 
  private void 
  doJobDetailsWindow() 
  {
    JPanelFrame frame = UIMaster.getInstance().createWindow();
    frame.setSize(497, 582);

    JManagerPanel mgr = frame.getManagerPanel();
    mgr.setContents(new JQueueJobDetailsPanel(pTopLevelPanel));

    frame.validate();
    frame.repaint();
  }


  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Create a new secondary panel frame containing a JEmptyPanel. 
   */ 
  private void 
  doEmptyWindow() 
  {
    JPanelFrame frame = UIMaster.getInstance().createWindow();

    JManagerPanel mgr = frame.getManagerPanel();
    mgr.setContents(new JEmptyPanel(pTopLevelPanel));

    frame.validate();
    frame.repaint();
  }


  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Change the name of the current window.
   */ 
  private void 
  doRenameWindow() 
  {
    JPanelFrame frame = getPanelFrame();
    if(frame != null) {
      JWindowRenameDialog diag = 
	new JWindowRenameDialog(getTopFrame(), frame.getWindowName());
      diag.setVisible(true);
      
      if(diag.wasConfirmed()) {
	String wname = diag.getName();
	if((wname != null) && (wname.length() > 0))
	frame.setWindowName(wname);
	else 
	  frame.setWindowName(null);
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Change the contents of this panel to a JNodeBrowserPanel. 
   */ 
  private void 
  doNodeBrowserPanel()
  {
    if(getContents() instanceof JNodeBrowserPanel) {
      Toolkit.getDefaultToolkit().beep();
      return; 
    }

    if(pTopLevelPanel.warnUnsavedChangesBeforeReplace()) 
      return;

    JTopLevelPanel dead = (JTopLevelPanel) removeContents();
    JNodeBrowserPanel panel = new JNodeBrowserPanel(dead);
    setContents(panel); 
    dead.setGroupID(0);
    dead.freeDisplayLists();
    refocusOnChildPanel();

    updateFromNodeViewer(panel.getGroupID());
  }

  /**
   * Change the contents of this panel to a JNodeViewerPanel. 
   */ 
  private void 
  doNodeViewerPanel()
  {
    if(getContents() instanceof JNodeViewerPanel) {
      Toolkit.getDefaultToolkit().beep();
      return; 
    }

    if(pTopLevelPanel.warnUnsavedChangesBeforeReplace()) 
      return;

    JTopLevelPanel dead = (JTopLevelPanel) removeContents();
    JNodeViewerPanel panel = new JNodeViewerPanel(dead);
    setContents(panel); 
    dead.setGroupID(0);
    dead.freeDisplayLists();
    refocusOnChildPanel();

    updateFromNodeBrowser(panel.getGroupID());
  }

  /**
   * Change the contents of this panel to a JNodeDetailsPanel. 
   */ 
  private void 
  doNodeDetailsPanel()
  {
    if(getContents() instanceof JNodeDetailsPanel) {
      Toolkit.getDefaultToolkit().beep();
      return; 
    }

    if(pTopLevelPanel.warnUnsavedChangesBeforeReplace()) 
      return;

    JTopLevelPanel dead = (JTopLevelPanel) removeContents();
    JNodeDetailsPanel panel = new JNodeDetailsPanel(dead);
    setContents(panel);
    dead.setGroupID(0);
    dead.freeDisplayLists();
    refocusOnChildPanel();
    
    updateFromNodeViewer(panel.getGroupID());
  }

  /**
   * Change the contents of this panel to a JNodeFilesPanel. 
   */ 
  private void 
  doNodeFilesPanel()
  {
    if(getContents() instanceof JNodeFilesPanel) {
      Toolkit.getDefaultToolkit().beep();
      return; 
    }

    if(pTopLevelPanel.warnUnsavedChangesBeforeReplace()) 
      return;

    JTopLevelPanel dead = (JTopLevelPanel) removeContents();
    JNodeFilesPanel panel = new JNodeFilesPanel(dead);
    setContents(panel);
    dead.setGroupID(0);
    dead.freeDisplayLists();
    refocusOnChildPanel();

    updateFromNodeViewer(panel.getGroupID());
  }

  /**
   * Change the contents of this panel to a JNodeLinksPanel. 
   */ 
  private void 
  doNodeLinksPanel()
  {
    if(getContents() instanceof JNodeLinksPanel) {
      Toolkit.getDefaultToolkit().beep();
      return; 
    }

    if(pTopLevelPanel.warnUnsavedChangesBeforeReplace()) 
      return;

    JTopLevelPanel dead = (JTopLevelPanel) removeContents();
    JNodeLinksPanel panel = new JNodeLinksPanel(dead);
    setContents(panel);
    dead.setGroupID(0);
    dead.freeDisplayLists();
    refocusOnChildPanel();

    updateFromNodeViewer(panel.getGroupID());
  }

  /**
   * Change the contents of this panel to a JNodeHistoryPanel. 
   */ 
  private void 
  doNodeHistoryPanel()
  {
    if(getContents() instanceof JNodeHistoryPanel) {
      Toolkit.getDefaultToolkit().beep();
      return; 
    }

    if(pTopLevelPanel.warnUnsavedChangesBeforeReplace()) 
      return;

    JTopLevelPanel dead = (JTopLevelPanel) removeContents();
    JNodeHistoryPanel panel = new JNodeHistoryPanel(dead);
    setContents(panel);
    dead.setGroupID(0);
    dead.freeDisplayLists();
    refocusOnChildPanel();

    updateFromNodeViewer(panel.getGroupID());
  }

  /**
   * Change the contents of this panel to a JNodeAnnotationsPanel. 
   */ 
  private void 
  doNodeAnnotationsPanel()
  {
    if(getContents() instanceof JNodeAnnotationsPanel) {
      Toolkit.getDefaultToolkit().beep();
      return; 
    }

    if(pTopLevelPanel.warnUnsavedChangesBeforeReplace()) 
      return;

    JTopLevelPanel dead = (JTopLevelPanel) removeContents();
    JNodeAnnotationsPanel panel = new JNodeAnnotationsPanel(dead);
    setContents(panel);
    dead.setGroupID(0);
    dead.freeDisplayLists();
    refocusOnChildPanel();
    
    updateFromNodeViewer(panel.getGroupID());
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Change the contents of this panel to a JQueueJobServerStatsPanel. 
   */ 
  private void 
  doJobServerStatsPanel()
  {
    if(getContents() instanceof JQueueJobServerStatsPanel) {
      Toolkit.getDefaultToolkit().beep();
      return; 
    }

    if(pTopLevelPanel.warnUnsavedChangesBeforeReplace()) 
      return;

    JTopLevelPanel dead = (JTopLevelPanel) removeContents();
    setContents(new JQueueJobServerStatsPanel(dead));
    dead.setGroupID(0);
    dead.freeDisplayLists();
    refocusOnChildPanel();   
  }

  /**
   * Change the contents of this panel to a JQueueJobServersPanel. 
   */ 
  private void 
  doJobServersPanel()
  {
    if(getContents() instanceof JQueueJobServersPanel) {
      Toolkit.getDefaultToolkit().beep();
      return; 
    }

    if(pTopLevelPanel.warnUnsavedChangesBeforeReplace()) 
      return;

    JTopLevelPanel dead = (JTopLevelPanel) removeContents();
    setContents(new JQueueJobServersPanel(dead));
    dead.setGroupID(0);
    dead.freeDisplayLists();
    refocusOnChildPanel();   
  }

  /**
   * Change the contents of this panel to a JQueueJobSlotsPanel. 
   */ 
  private void 
  doJobSlotsPanel()
  {
    if(getContents() instanceof JQueueJobSlotsPanel) {
      Toolkit.getDefaultToolkit().beep();
      return; 
    }

    if(pTopLevelPanel.warnUnsavedChangesBeforeReplace()) 
      return;

    JTopLevelPanel dead = (JTopLevelPanel) removeContents();
    setContents(new JQueueJobSlotsPanel(dead));
    dead.setGroupID(0);
    dead.freeDisplayLists();
    refocusOnChildPanel();   
  }

  /**
   * Change the contents of this panel to a JQueueJobBrowserPanel. 
   */ 
  private void 
  doJobBrowserPanel()
  {
    if(getContents() instanceof JQueueJobBrowserPanel) {
      Toolkit.getDefaultToolkit().beep();
      return; 
    }

    if(pTopLevelPanel.warnUnsavedChangesBeforeReplace()) 
      return;

    JTopLevelPanel dead = (JTopLevelPanel) removeContents();
    setContents(new JQueueJobBrowserPanel(dead));
    dead.setGroupID(0);
    dead.freeDisplayLists();
    refocusOnChildPanel();   
  }

  /**
   * Change the contents of this panel to a JQueueJobViewerPanel. 
   */ 
  private void 
  doJobViewerPanel()
  {
    if(getContents() instanceof JQueueJobViewerPanel) {
      Toolkit.getDefaultToolkit().beep();
      return; 
    }

    if(pTopLevelPanel.warnUnsavedChangesBeforeReplace()) 
      return;

    JTopLevelPanel dead = (JTopLevelPanel) removeContents();
    setContents(new JQueueJobViewerPanel(dead));
    dead.setGroupID(0);
    dead.freeDisplayLists();
    refocusOnChildPanel();
  }

  /**
   * Change the contents of this panel to a JQueueJobDetailsPanel. 
   */ 
  private void 
  doJobDetailsPanel()
  {
    if(getContents() instanceof JQueueJobDetailsPanel) {
      Toolkit.getDefaultToolkit().beep();
      return; 
    }

    if(pTopLevelPanel.warnUnsavedChangesBeforeReplace()) 
      return;

    JTopLevelPanel dead = (JTopLevelPanel) removeContents();
    setContents(new JQueueJobDetailsPanel(dead));
    dead.setGroupID(0);
    dead.freeDisplayLists();
    refocusOnChildPanel();
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Change the contents of this panel to a JEmptyPanel. 
   */ 
  private void 
  doEmptyPanel()
  {
    if(getContents() instanceof JEmptyPanel) {
      Toolkit.getDefaultToolkit().beep();
      return; 
    }

    if(pTopLevelPanel.warnUnsavedChangesBeforeReplace()) 
      return;

    JTopLevelPanel dead = (JTopLevelPanel) removeContents();
    setContents(new JEmptyPanel(dead));
    dead.setGroupID(0);  
    dead.freeDisplayLists();
    refocusOnChildPanel();  
  }



  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Split the panel horizontally adding a new empty panel on the left.
   */ 
  private void 
  doAddLeft()
  {
    JManagerPanel left = null;
    {
      left = new JManagerPanel();
      left.setContents(new JEmptyPanel(pTopLevelPanel));
    }

    JManagerPanel right = null;
    {    
      right = new JManagerPanel();
      right.setContents(removeContents());
    }

    setContents(new JHorzSplitPanel(left, right));
    refocusOnChildPanel();
  }

  /**
   * Split the panel horizontally adding a new empty panel on the right.
   */ 
  private void 
  doAddRight()
  {
    JManagerPanel left = null;
    {
      left = new JManagerPanel();
      left.setContents(removeContents());
    }

    JManagerPanel right = null;
    {    
      right = new JManagerPanel();
      right.setContents(new JEmptyPanel(pTopLevelPanel));
    }

    setContents(new JHorzSplitPanel(left, right));
    refocusOnChildPanel();
  }

  /**
   * Split the panel vertically adding a new empty panel above.
   */ 
  private void 
  doAddAbove()
  {
    JManagerPanel above = null;
    {
      above = new JManagerPanel();
      above.setContents(new JEmptyPanel(pTopLevelPanel));
    }

    JManagerPanel below = null;
    {    
      below = new JManagerPanel();
      below.setContents(removeContents());
    }

    setContents(new JVertSplitPanel(above, below));
    refocusOnChildPanel();
  }

  /**
   * Split the panel vertically adding a new empty panel below.
   */ 
  private void 
  doAddBelow()
  {
    JManagerPanel above = null;
    {
      above = new JManagerPanel();
      above.setContents(removeContents());
    }

    JManagerPanel below = null;
    {    
      below = new JManagerPanel();
      below.setContents(new JEmptyPanel(pTopLevelPanel));
    }

    setContents(new JVertSplitPanel(above, below));
    refocusOnChildPanel();
  }

  /**
   * Add a new empty tab to the child tabbed pane. <P> 
   * 
   * If the current child isn't already a tabbed pane, a new tabbed pane is created and
   * the current child is moved to the first tab of the new tabbed pane.
   */ 
  private void 
  doAddTab()
  { 
    Container parent = getParent();
    assert(parent != null);

    /* if the parent is already a tabbed pane, simply add another tab */ 
    if(parent instanceof JTabbedPanel) {
      JTabbedPanel tab = (JTabbedPanel) parent;

      JManagerPanel mgr = new JManagerPanel();
      mgr.setContents(new JEmptyPanel(pTopLevelPanel));

      tab.addTab(mgr);
    }
    
    /* create a new tabbed panel with the contents of this panel as its first tab */
    else {
      Component comp = removeContents();
      if(comp != null) {
	JManagerPanel mgr = new JManagerPanel();
	mgr.setContents(comp);

	JTabbedPanel tab = new JTabbedPanel();
	tab.addTab(mgr);

	{
	  JManagerPanel smgr = new JManagerPanel();
	  smgr.setContents(new JEmptyPanel(pTopLevelPanel));
	  tab.addTab(smgr);
	  tab.setSelectedIndex(1);
	}

	setContents(tab);
      }
    }

    refocusOnChildPanel();
  }

  /**
   * Add a new empty tab to the child tabbed pane. <P> 
   * 
   * If the current child isn't already a tabbed pane, a new tabbed pane is created and
   * the current child is moved to the first tab of the new tabbed pane.
   */ 
  private void 
  doAddTopTab()
  { 
    Container parent = getParent();
    while(true) {
      if((parent == null) || (parent instanceof JTabbedPanel))
	break;
      parent = parent.getParent();
    }

    /* if the parent is already a tabbed pane, simply add another tab */ 
    if((parent != null) && (parent instanceof JTabbedPanel)) {
      JTabbedPanel tab = (JTabbedPanel) parent;

      JManagerPanel mgr = new JManagerPanel();
      mgr.setContents(new JEmptyPanel(pTopLevelPanel));

      tab.addTab(mgr);
    }
    
    /* create a new tabbed panel with the contents of this panel as its first tab */
    else {
      Component comp = removeContents();
      if(comp != null) {
	JManagerPanel mgr = new JManagerPanel();
	mgr.setContents(comp);

	JTabbedPanel tab = new JTabbedPanel();
	tab.addTab(mgr);

	{
	  JManagerPanel smgr = new JManagerPanel();
	  smgr.setContents(new JEmptyPanel(pTopLevelPanel));
	  tab.addTab(smgr);
	  tab.setSelectedIndex(1);
	}

	setContents(tab);
      }
    }

    refocusOnChildPanel();
  }

  /**
   * Close this panel.
   */ 
  private void 
  doClosePanel()
  {
    Container parent = getParent();
    assert(parent != null);

    /* replace the parent split pane with the other child */ 
    if((parent instanceof JHorzSplitPanel) || (parent instanceof JVertSplitPanel)) {
      if(pTopLevelPanel.warnUnsavedChangesBeforeClose()) 
        return;

      Container sparent = null;
      Component live = null;
      if(parent instanceof JHorzSplitPanel) {
	JHorzSplitPanel split = (JHorzSplitPanel) parent;
	
	sparent = split.getParent();
	if(!(sparent instanceof JManagerPanel))
	  return;
	
	if(split.getLeftComponent() == this) 
	  live = split.getRightComponent();
	else if(split.getRightComponent() == this) 
	  live = split.getLeftComponent();
	else 
	  assert(false);
	
	split.removeAll();
      }
      else {
	JVertSplitPanel split = (JVertSplitPanel) parent;
	
	sparent = split.getParent();
	if(!(sparent instanceof JManagerPanel))
	  return;
	
	if(split.getTopComponent() == this) 
	  live = split.getBottomComponent();
	else if(split.getBottomComponent() == this) 
	  live = split.getTopComponent();
	else 
	  assert(false);	
	
	split.removeAll();
      }
      
      JManagerPanel liveMgr = (JManagerPanel) live;
      JManagerPanel grandpa = (JManagerPanel) sparent;
      grandpa.setContents(liveMgr.removeContents());
      grandpa.refocusOnChildPanel();

      pTopLevelPanel.setGroupID(0);
      pTopLevelPanel.freeDisplayLists();
    }

    /* remove this tab from the parent tabbed pane */ 
    else if(parent instanceof JTabbedPanel) {
      if(pTopLevelPanel.warnUnsavedChangesBeforeClose()) 
        return;

      JTabbedPanel tab = (JTabbedPanel) parent;
      tab.remove(this);

      /* if only one tab remains, 
	   remove the tabbed pane and replace it with the contents of the sole tab */ 
      if(tab.getTabCount() == 1) {
	JManagerPanel grandpa = (JManagerPanel) tab.getParent();
	JManagerPanel lastMgr = (JManagerPanel) tab.getComponentAt(0);
	grandpa.setContents(lastMgr.removeContents());
	grandpa.refocusOnChildPanel();
      } 
      else {
	refocusOnChildPanel();
      }

      pTopLevelPanel.setGroupID(0);
      pTopLevelPanel.freeDisplayLists();
    }
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Change the owner|view of this panel.
   */ 
  private void 
  doChangeOwnerView()
  {
    JOwnerViewDialog dialog = 
      new JOwnerViewDialog(getTopFrame(), 
			   pTopLevelPanel.getAuthor(), pTopLevelPanel.getView());
    dialog.setVisible(true);
    
    if(dialog.wasConfirmed()) {
      String author = dialog.getAuthor();
      String view   = dialog.getView();
      if((author != null) && (view != null)) {
	pTopLevelPanel.setAuthorView(author, view);
	updateTitlePanel();
      }
    }	
  }


  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Change the panel group.
   */ 
  public void 
  doGroup
  (
   int groupID
  )
  {
    pTopLevelPanel.setGroupID(groupID);
    pGroupMenuAnchor.setIcon(sGroupIcons[groupID]);
  }

 
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Switch to the next tabbed panel.
   */ 
  private void 
  doNextTab() 
  {
    JTabbedPanel tab = null;
    {
      Container parent = getParent();
      while(parent != null) {
	if(parent instanceof JTabbedPanel) {
	  tab = (JTabbedPanel) parent;
	  break;
	}
	parent = parent.getParent();
      }
    }

    if(tab != null) {
      int idx = tab.getSelectedIndex();      
      tab.setSelectedIndex((idx == (tab.getTabCount()-1)) ? 0 : idx+1);
    }
  }

  /**
   * Switch to the prev tabbed panel.
   */ 
  private void 
  doPrevTab() 
  {
    JTabbedPanel tab = null;
    {
      Container parent = getParent();
      while(parent != null) {
	if(parent instanceof JTabbedPanel) {
	  tab = (JTabbedPanel) parent;
	  break;
	}
	parent = parent.getParent();
      }
    }

    if(tab != null) {
      int idx = tab.getSelectedIndex();      
      tab.setSelectedIndex((idx == 0) ? (tab.getTabCount()-1) : idx-1);
    } 
  }


  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Shutdown the server daemons, close the network connections and exit.
   */ 
  private void 
  doShutdownServer() 
  {
    UIMaster master = UIMaster.getInstance();
     
    /* last chance to save toolsets/packages */ 
    if(!master.discardWorkingToolsets()) {
      master.showManageToolsetsDialog();
      return;
    }

    /* shutdown options */ 
    JShutdownDialog diag = new JShutdownDialog(getTopFrame());
    diag.setVisible(true);

    if(diag.wasConfirmed()) {
      master.doUponExit();
      master.getQueueMgrClient().disconnect();

      try {
	boolean jobMgrs   = diag.shutdownJobMgrs();
	boolean pluginMgr = diag.shutdownPluginMgr();
	if(jobMgrs || pluginMgr) 
	  master.getMasterMgrClient().shutdown(jobMgrs, pluginMgr);
	else 
	  master.getMasterMgrClient().shutdown();
      }
      catch(PipelineException ex) {
      }

      /* give the sockets time to disconnect cleanly */ 
      try {
	Thread.sleep(1000);
      }
      catch(InterruptedException ex) {
      }
      
      System.exit(0);
    }
  }

	
      
  /*----------------------------------------------------------------------------------------*/
  /*  H E L P E R S                                                                         */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Helper method for updating the panels for the given group from any 
   * existing Node Viewer panel.
   */ 
  private void 
  updateFromNodeViewer
  (
   int groupID
  ) 
  {
    if(groupID > 0) {
      PanelGroup<JNodeViewerPanel> panels = UIMaster.getInstance().getNodeViewerPanels();
      JNodeViewerPanel viewer = panels.getPanel(groupID);
      if(viewer != null) {
	PanelUpdater pu = new PanelUpdater(viewer);
	pu.execute();
      }
    }    
  }

  /**
   * Helper method for updating the panels for the given group from any 
   * existing Node Browser panel.
   */ 
  private void 
  updateFromNodeBrowser
  (
   int groupID
  ) 
  {
    if(groupID > 0) {
      PanelGroup<JNodeBrowserPanel> panels = UIMaster.getInstance().getNodeBrowserPanels();
      JNodeBrowserPanel browser = panels.getPanel(groupID);
      if(browser != null) {
	PanelUpdater pu = new PanelUpdater(browser, true);
	pu.execute();
      }
    }    
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L   C L A S S E S                                                       */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A anchor icon which shows the title bar popup menu when pressed.
   */ 
  public
  class PopupMenuAnchor
    extends JLabel
    implements MouseListener, PopupMenuListener
  {
    PopupMenuAnchor
    (
     JManagerPanel panel
    )
    {
      super();

      setIcon(sMenuAnchorIcon);

      Dimension size = new Dimension(14, 19);
      setMinimumSize(size);
      setMaximumSize(size);
      setPreferredSize(size);
      
      addMouseListener(this);
      pPopup.addPopupMenuListener(this);

      pPanel = panel;
    }


    /*-- MOUSE LISTENER METHODS ------------------------------------------------------------*/

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
      int mods = e.getModifiersEx();
      
      int on1  = (MouseEvent.BUTTON1_DOWN_MASK);		  
      
      int off1 = (MouseEvent.BUTTON1_DOWN_MASK | 
		  MouseEvent.BUTTON2_DOWN_MASK | 
		  MouseEvent.SHIFT_DOWN_MASK | 
		  MouseEvent.ALT_DOWN_MASK |
		  MouseEvent.CTRL_DOWN_MASK);

      int on2  = (MouseEvent.BUTTON3_DOWN_MASK);		  
      
      int off2 = (MouseEvent.BUTTON1_DOWN_MASK | 
		  MouseEvent.BUTTON2_DOWN_MASK | 
		  MouseEvent.SHIFT_DOWN_MASK | 
		  MouseEvent.ALT_DOWN_MASK |
		  MouseEvent.CTRL_DOWN_MASK);
      
      /* BUTTON3: popup menu */ 
      if(((mods & (on1 | off1)) == on1) ||
	 ((mods & (on2 | off2)) == on2)) {
	handleAnchorMouseEvent(e);
      }
    }

    /**
     * Invoked when a mouse button has been released on a component. 
     */ 
    public void 
    mouseReleased(MouseEvent e) {}
    
    /**
     * Handle popup anchor mouse events.
     */ 
    public void
    handleAnchorMouseEvent
    (
     MouseEvent e 
    ) 
    {
      setIcon(sMenuAnchorPressedIcon);

      /* the main window cannot be renamed */ 
      pRenameWindowItem.setEnabled(getPanelFrame() != null);

      /* only enable layout changes if there is enough space */ 
      {
	pAddTabItem.setEnabled(pPanel.getHeight() > 29+20);
	
	boolean horz = (pPanel.getWidth() > (222*2 + 11));
	pAddLeftItem.setEnabled(horz);
	pAddRightItem.setEnabled(horz);
	
	boolean vert = (pPanel.getHeight() > (29*2 + 10));
	pAddAboveItem.setEnabled(vert);
	pAddBelowItem.setEnabled(vert);
      }

      /* panel layout items */ 
      {
	UIMaster master = UIMaster.getInstance();
	pSaveLayoutItem.setEnabled(master.getLayoutPath() != null);

	pRestoreLayoutMenu.removeAll();
	pRestoreLayoutNoSelectMenu.removeAll();
	
	Path path = new Path(PackageInfo.getSettingsPath(), "layouts"); 
	File dir = path.toFile();
        if(!dir.isDirectory()) {
          String result = null;
          if(dir.mkdirs()) 
            result = "Recreated layouts root directory (" + dir + ")!"; 
          else
            result = "Unable to recreate layouts root directory (" + dir + ")!"; 

          UIMaster.getInstance().showErrorDialog
            ("Error:", "The saved layout directory (" + dir + ") was missing!\n\n" + result);
        }
        
        if(!dir.isDirectory()) {
          pRestoreLayoutMenu.setEnabled(false);
	  pRestoreLayoutNoSelectMenu.setEnabled(false);
	} 
	else {
	  rebuildRestoreMenu(path, new Path("/"), pRestoreLayoutMenu,         true);
	  rebuildRestoreMenu(path, new Path("/"), pRestoreLayoutNoSelectMenu, false);
	}
      }

      /* privileged status */ 
      {
 	UIMaster master = UIMaster.getInstance();
	MasterMgrClient client = master.getMasterMgrClient();
 	try {
 	  PrivilegeDetails privileges = client.getCachedPrivilegeDetails();
 	  pBackupDatabaseItem.setEnabled
	    (privileges.isMasterAdmin() && (PackageInfo.sOsType == OsType.Unix));
 	  pArchiveItem.setEnabled(privileges.isMasterAdmin());
 	  pOfflineItem.setEnabled(privileges.isMasterAdmin());
 	  pRestoreItem.setEnabled(privileges.isMasterAdmin());
 	  pShutdownServerItem.setEnabled(privileges.isMasterAdmin());
 	}
 	catch(PipelineException ex) {
 	  master.showErrorDialog(ex);
 	}
      } 
      
      pPopup.show(e.getComponent(), e.getX(), e.getY()); 
    }

    
    /*-- POPUP MENU LISTENER METHODS -------------------------------------------------------*/

    public void 	
    popupMenuCanceled(PopupMenuEvent e) {} 

    public void 	
    popupMenuWillBecomeInvisible
    (
     PopupMenuEvent e
    )
    {
      setIcon(sMenuAnchorIcon);
    }

    public void
    popupMenuWillBecomeVisible(PopupMenuEvent e) {} 

    
    
    /*-- HELPERS ---------------------------------------------------------------------------*/

    /**
     * Recursively rebuild the restore layout menu.
     * 
     * @param root
     *   The full abstract path to the root saved layout directory.
     * 
     * @param local 
     *   The current directory relative to root (null if none).
     * 
     * @param menu
     *   The current parent menu.
     * 
     * @param select
     *   Whether node and/or job group selections should be restored.
     */ 
    private void 
    rebuildRestoreMenu
    (
     Path root, 
     Path local,
     JMenu menu, 
     boolean select
    ) 
    {
      TreeSet<Path> subdirs = new TreeSet<Path>();
      TreeSet<String> layouts = new TreeSet<String>();
      {
	Path current = new Path(root, local);
	File files[] = current.toFile().listFiles();
	if(files != null) {
          int wk;
          for(wk=0; wk<files.length; wk++) {
            String name = files[wk].getName();
            if(files[wk].isDirectory()) 
              subdirs.add(new Path(local, name)); 
            else if(files[wk].isFile()) 
              layouts.add(name);
          }
        }
      }
      
      for(Path subdir : subdirs) {
	JMenu sub = new JMenu(subdir.getName());
	menu.add(sub);
	
	rebuildRestoreMenu(root, subdir, sub, select);
      }
      
      for(String lname : layouts) {
	JMenuItem item = new JMenuItem(lname);

	Path lpath = new Path(local, lname);
	item.setActionCommand
	  ((select ? "restore-layout:" : "restore-layout-no-select:") + lpath);
	item.addActionListener(pPanel);
	
	menu.add(item);
      }

      menu.setEnabled(menu.getItemCount() > 0);
    }



    /*-- INTERNALS -------------------------------------------------------------------------*/

    private static final long serialVersionUID = 2138270471079189817L;;

    private JManagerPanel  pPanel;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * A anchor icon which shows the group popup menu when pressed.
   */ 
  public
  class GroupMenuAnchor
    extends JLabel
    implements MouseListener
  {
    GroupMenuAnchor()
    {
      super();

      setIcon(sGroupIcons[0]);

      Dimension size = new Dimension(19, 19);
      setMinimumSize(size);
      setMaximumSize(size);
      setPreferredSize(size);
      
      addMouseListener(this);
    }


    /*-- MOUSE LISTENER METHODS ------------------------------------------------------------*/

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
      int mods = e.getModifiersEx();
      
      int on1  = (MouseEvent.BUTTON1_DOWN_MASK);		  
      
      int off1 = (MouseEvent.BUTTON1_DOWN_MASK | 
		  MouseEvent.BUTTON2_DOWN_MASK | 
		  MouseEvent.SHIFT_DOWN_MASK | 
		  MouseEvent.ALT_DOWN_MASK |
		  MouseEvent.CTRL_DOWN_MASK);

      int on2  = (MouseEvent.BUTTON3_DOWN_MASK);		  
      
      int off2 = (MouseEvent.BUTTON1_DOWN_MASK | 
		  MouseEvent.BUTTON2_DOWN_MASK | 
		  MouseEvent.SHIFT_DOWN_MASK | 
		  MouseEvent.ALT_DOWN_MASK |
		  MouseEvent.CTRL_DOWN_MASK);
      
      /* BUTTON3: popup menu */ 
      if(((mods & (on1 | off1)) == on1) ||
	 ((mods & (on2 | off2)) == on2)) {
	handleAnchorMouseEvent(e);
      }
    }

    /**
     * Invoked when a mouse button has been released on a component. 
     */ 
    public void 
    mouseReleased(MouseEvent e) {}

    /**
     * Handle popup anchor mouse events.
     */ 
    public void
    handleAnchorMouseEvent
    (
     MouseEvent e 
    ) 
    {
      int wk;
      for(wk=1; wk<10; wk++) 
	pGroupItems[wk].setEnabled(pTopLevelPanel.isGroupUnused(wk));
      
      pGroupPopup.show(e.getComponent(), e.getX(), e.getY()); 
    }


    /*-- INTERNALS -------------------------------------------------------------------------*/

    private static final long serialVersionUID = -4700928181653009212L; 
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Manages keyboard focus.                                           
   */ 
  public
  class KeyFocuser
    extends MouseAdapter
  {
    KeyFocuser() 
    {} 

    /**
     * Invoked when the mouse enters a component. 
     */
    public void 
    mouseEntered
    (
     MouseEvent e
    ) 
    {
      pTitlePanel.requestFocusInWindow();
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
      if(pTitlePanel.getMousePosition(true) == null) 
	KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  public void 
  toGlue
  ( 
   GlueEncoder encoder   
  ) 
    throws GlueException
  {
    encoder.encode("Contents", (Glueable) getContents());
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    Component contents = (Component) decoder.decode("Contents");
    if(contents == null) 
      throw new GlueException("The \"Contents\" was missing or (null)!");
    setContents(contents);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -3791561567661137439L;


  private static final Icon sMenuAnchorIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("MenuAnchorIcon.png"));

  private static final Icon sMenuAnchorPressedIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("MenuAnchorPressedIcon.png"));


  private static final Icon sGroupIcons[] = {
    new ImageIcon(LookAndFeelLoader.class.getResource("Group0.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("Group1.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("Group2.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("Group3.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("Group4.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("Group5.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("Group6.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("Group7.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("Group8.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("Group9.png"))
  };

  private static final Icon sGroupSelectedIcons[] = {
    new ImageIcon(LookAndFeelLoader.class.getResource("Group0Selected.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("Group1Selected.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("Group2Selected.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("Group3Selected.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("Group4Selected.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("Group5Selected.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("Group6Selected.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("Group7Selected.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("Group8Selected.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("Group9Selected.png"))
  };

  private static final Icon sGroupDisabledIcons[] = {
    new ImageIcon(LookAndFeelLoader.class.getResource("Group0Disabled.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("Group1Disabled.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("Group2Disabled.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("Group3Disabled.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("Group4Disabled.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("Group5Disabled.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("Group6Disabled.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("Group7Disabled.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("Group8Disabled.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("Group9Disabled.png"))
  };


  private static final Icon sLockedLightIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("LockedLightIcon.png"));

  private static final Icon sLockedLightOnIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("LockedLightOnIcon.png"));



  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The title bar. 
   */ 
  private JPanel  pTitlePanel;

  /** 
   * The panel type label.
   */ 
  private JLabel  pTypeLabel;


  /*----------------------------------------------------------------------------------------*/

  /** 
   * The anchor label for the popup menu.
   */
  private PopupMenuAnchor  pPopupMenuAnchor;

  /**
   * The panel layout popup menu.
   */ 
  private JPopupMenu  pPopup; 

  /**
   * The panel layout popup menu items.
   */ 
  private JMenuItem  pNodeBrowserWindowItem;
  private JMenuItem  pNodeViewerWindowItem;
  private JMenuItem  pNodeDetailsWindowItem;
  private JMenuItem  pNodeFilesWindowItem;
  private JMenuItem  pNodeLinksWindowItem;
  private JMenuItem  pNodeHistoryWindowItem; 
  private JMenuItem  pNodeAnnotationsWindowItem;
  private JMenuItem  pJobServerStatsWindowItem;
  private JMenuItem  pJobServersWindowItem;
  private JMenuItem  pJobSlotsWindowItem;
  private JMenuItem  pJobBrowserWindowItem;
  private JMenuItem  pJobViewerWindowItem;
  private JMenuItem  pJobDetailsWindowItem;
  private JMenuItem  pEmptyWindowItem;

  private JMenuItem  pRenameWindowItem;

  private JMenuItem  pNodeBrowserPanelItem;
  private JMenuItem  pNodeViewerPanelItem;
  private JMenuItem  pNodeDetailsPanelItem;
  private JMenuItem  pNodeFilesPanelItem;
  private JMenuItem  pNodeLinksPanelItem;
  private JMenuItem  pNodeHistoryPanelItem;
  private JMenuItem  pNodeAnnotationsPanelItem;
  private JMenuItem  pJobServerStatsPanelItem;
  private JMenuItem  pJobServersPanelItem;
  private JMenuItem  pJobSlotsPanelItem;
  private JMenuItem  pJobBrowserPanelItem;
  private JMenuItem  pJobViewerPanelItem;
  private JMenuItem  pJobDetailsPanelItem;
  private JMenuItem  pEmptyPanelItem;

  private JMenuItem  pAddTabItem; 
  private JMenuItem  pAddTopTabItem; 
  private JMenuItem  pAddLeftItem; 
  private JMenuItem  pAddRightItem; 
  private JMenuItem  pAddAboveItem; 
  private JMenuItem  pAddBelowItem;        

  private JMenuItem  pOwnerViewItem;

  private JMenuItem  pSaveLayoutItem;
  private JMenuItem  pSaveLayoutAsItem;
  private JMenuItem  pManageLayoutsItem;       
  private JMenuItem  pResetLayoutItem;       
  private JMenu      pRestoreLayoutMenu;
  private JMenu      pRestoreLayoutNoSelectMenu;

  private JMenuItem  pLaunchBuilderItem;
  private JMenuItem  pPreferencesItem;
  private JMenuItem  pDefaultEditorsItem;
  private JMenuItem  pUpdatePluginsItem;

  private JMenuItem  pManagePrivilegesItem;
  private JMenuItem  pManageToolsetsItem;
  private JMenuItem  pLicenseKeysItem;
  private JMenuItem  pSelectionKeysItem;
  private JMenuItem  pHardwareKeysItem;

  private JMenuItem  pServerExtensionsItem;
  private JMenuItem  pBackupDatabaseItem;

  private JMenuItem  pArchiveItem;
  private JMenuItem  pOfflineItem;
  private JMenuItem  pRestoreItem;
  private JMenuItem  pArchiveVolumesItem;
  private JMenuItem  pShutdownServerItem;

  private JMenuItem  pAboutPipelineItem;
  private JMenuItem  pQuickReferenceItem;
  private JMenuItem  pUserManualItem;
  private JMenuItem  pHomePageItem;
  private JMenuItem  pSupportFormumsItem;
  private JMenuItem  pBugDatabaseItem;
  private JMenuItem  pSiteConfigurationItem;
  private JMenuItem  pLicenseAgreementItem;
  private JMenuItem  pShowLogsItem;

  private JMenuItem  pQuitItem;


  /**
   * The group popup menu.
   */ 
  private JPopupMenu  pGroupPopup; 

  /**
   * The group popup menu items.
   */  
  private JMenuItem[]  pGroupItems;

  /** 
   * The anchor label for the group popup menu.
   */
  private GroupMenuAnchor  pGroupMenuAnchor;


  /**
   * Displays the owning author and name of the current working area view.
   */ 
  private JTextField  pOwnerViewField;

  /**
   * Indicates whether the contents of the panel is read-only.
   */ 
  private JLabel  pLockedLight;


  /**
   * The top level panel contents 
   * or <CODE>null</CODE> if the contents is not a top level panel.
   */ 
  private JTopLevelPanel  pTopLevelPanel;
  
}
