// $Id: JManagerPanel.java,v 1.31 2004/07/25 03:09:22 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.core.BaseApp;
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
  implements Glueable, ComponentListener, ActionListener
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
	JMenu sub = new JMenu("Panel Type");   
	pPopup.add(sub);  
   
	item = new JMenuItem("Node Browser");
	item.setActionCommand("node-browser");
	item.addActionListener(this);
	sub.add(item);  
	
	item = new JMenuItem("Node Viewer");
	item.setActionCommand("node-viewer");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Node Details");
	item.setActionCommand("node-details");
	item.addActionListener(this);
	sub.add(item);  

// 	item = new JMenuItem("Node Links");
// 	item.setActionCommand("node-links");
// 	item.addActionListener(this);
// 	item.setEnabled(false); // FOR NOW 
// 	sub.add(item);  

	item = new JMenuItem("Node Files");
	item.setActionCommand("node-files");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Node History");
	item.setActionCommand("node-history");
	item.addActionListener(this);
	sub.add(item);  
	
	sub.addSeparator();

	item = new JMenuItem("Queue Servers");
	item.setActionCommand("queue-servers");
	item.addActionListener(this);
	item.setEnabled(false); // FOR NOW 
	sub.add(item);  

	item = new JMenuItem("Job Browser");
	item.setActionCommand("job-browser");
	item.addActionListener(this);
	item.setEnabled(false); // FOR NOW 
	sub.add(item);  

	item = new JMenuItem("Job Viewer");
	item.setActionCommand("job-viewer");
	item.addActionListener(this);
	item.setEnabled(false); // FOR NOW 
	sub.add(item);  

	item = new JMenuItem("Job Details");
	item.setActionCommand("job-details");
	item.addActionListener(this);
	item.setEnabled(false); // FOR NOW 
	sub.add(item);  

	sub.addSeparator();

	item = new JMenuItem("Task Timeline");
	item.setActionCommand("task-timeline");
	item.addActionListener(this);
	item.setEnabled(false); // FOR NOW 
	sub.add(item);  

	item = new JMenuItem("Task Details");
	item.setActionCommand("task-details");
	item.addActionListener(this);
	item.setEnabled(false); // FOR NOW    
	sub.add(item);  

	sub.addSeparator();

	item = new JMenuItem("None");
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
      item.setActionCommand("save-layout-as");
      item.addActionListener(this);
      pPopup.add(item);  
      
      {
	JMenu sub = new JMenu("Restore Layout");   
	pRestoreLayoutMenu = sub;

	pPopup.add(sub);  
      }

      item = new JMenuItem("Manage Layouts...");
      item.setActionCommand("manage-layouts");
      item.addActionListener(this);
      pPopup.add(item);  
      
      pPopup.addSeparator();

      item = new JMenuItem("Preferences...");
      item.setActionCommand("preferences");
      item.addActionListener(this);
      pPopup.add(item);  

      {
	JMenu sub = new JMenu("Admin");   
	pPopup.add(sub);  

	item = new JMenuItem("Default Editors...");
	item.setActionCommand("default-editors");
	item.addActionListener(this);
	sub.add(item);  

	sub.addSeparator();

	item = new JMenuItem("Users...");
	item.setActionCommand("manage-users");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Toolsets...");
	item.setActionCommand("manage-toolsets");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("License Keys...");
	item.setActionCommand("manage-license-keys");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Selection Keys...");
	item.setActionCommand("manage-selection-keys");
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
	item.setActionCommand("about");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("User Manual...");
	item.setEnabled(false);
	item.setActionCommand("user-anual");
	item.addActionListener(this);
	sub.add(item);  
	  
	sub.addSeparator();
	
	item = new JMenuItem("Home Page...");
	item.setActionCommand("home-page");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Support Forums...");
	item.setEnabled(false);
	item.setActionCommand("support-forums");
	item.addActionListener(this);
	sub.add(item);  
	  
	item = new JMenuItem("Bug Database...");
	item.setEnabled(false);
	item.setActionCommand("bug-database");
	item.addActionListener(this);
	sub.add(item);  

	sub.addSeparator();
	
	item = new JMenuItem("Site Configuration...");
	item.setActionCommand("site-configuration");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("License Agreement...");
	item.setEnabled(false);
	item.setActionCommand("license-agreement");
	item.addActionListener(this);
	sub.add(item);  
      }
      
      pPopup.addSeparator();
      pPopup.addSeparator();
      
      item = new JMenuItem("Quit...");
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
    

    /* panel title bar */ 
    {
      JPanel panel = new JPanel();
      pTitlePanel = panel;

      panel.setName("PanelBar");
      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS)); 

      panel.setMinimumSize(new Dimension(222, 29));
      panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 29));
      panel.setPreferredSize(new Dimension(222, 29));

      {
	PopupMenuAnchor anchor = new PopupMenuAnchor(this);
	panel.add(anchor);
      }

      panel.add(Box.createRigidArea(new Dimension(8, 0)));

      {
	GroupMenuAnchor anchor = new GroupMenuAnchor();
	pGroupMenuAnchor = anchor;
	panel.add(anchor);	
      }

      panel.add(Box.createRigidArea(new Dimension(4, 0)));

      {
	pOwnerViewField = UIMaster.createTextField(null, 120, JLabel.CENTER);
	panel.add(pOwnerViewField);
      }

      panel.add(Box.createRigidArea(new Dimension(4, 0)));

      {
	JLabel label = new JLabel();
	pLockedLight = label;

	Dimension size = new Dimension(19, 19);
	label.setMinimumSize(size);
	label.setMaximumSize(size);
	label.setPreferredSize(size);

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

	panel.add(btn);
      } 
    }

    addComponentListener(this); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

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


  /**
   * Update the components which make up the title panel to reflect the current state of 
   * the top level panel contents.
   */ 
  public void 
  updateTitlePanel()
  {
    if(pTopLevelPanel == null) 
      return; 

    pGroupMenuAnchor.setIcon(sGroupIcons[pTopLevelPanel.getGroupID()]);
    pOwnerViewField.setText(pTopLevelPanel.getAuthor() + " | " + pTopLevelPanel.getView());
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
    System.out.print("Action: " + e.getActionCommand() + "\n");

    /* dispatch event */ 
    String cmd = e.getActionCommand();
    if(cmd.equals("node-browser"))
      doNodeBrowserPanel();
    else if(cmd.equals("node-viewer"))
      doNodeViewerPanel();
    else if(cmd.equals("node-details"))
      doNodeDetailsPanel();

    // ...

    else if(cmd.equals("node-files"))
      doNodeFilesPanel();
    else if(cmd.equals("node-history"))
      doNodeHistoryPanel();

    // ...

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
    else if(cmd.equals("close-panel"))
      doClosePanel();

    /* owner|view */
    else if(cmd.equals("change-owner-view"))
      doChangeOwnerView();
    
    /* group */ 
    else if(cmd.startsWith("group:")) 
      doGroup(Integer.valueOf(cmd.substring(6)));
    
    /* UIMaster */ 
    else if(cmd.equals("save-layout"))
      UIMaster.getInstance().doSaveLayout();
    else if(cmd.equals("save-layout-as"))
      UIMaster.getInstance().showSaveLayoutDialog();
    else if(cmd.startsWith("restore-layout:")) 
      UIMaster.getInstance().doRestoreSavedLayout(cmd.substring(15));
    else if(cmd.equals("manage-layouts"))
      UIMaster.getInstance().showManageLayoutsDialog();
    else if(cmd.equals("default-editors"))
      UIMaster.getInstance().showDefaultEditorsDialog();
    else if(cmd.equals("manage-users"))
      UIMaster.getInstance().showManageUsersDialog();
    else if(cmd.equals("manage-toolsets"))
      UIMaster.getInstance().showManageToolsetsDialog();
    else if(cmd.equals("manage-license-keys"))
      UIMaster.getInstance().showManageLicenseKeysDialog();
    else if(cmd.equals("manage-selection-keys"))
      UIMaster.getInstance().showManageSelectionKeysDialog();
    else if(cmd.equals("shutdown"))
      doShutdownServer();
    else if(cmd.equals("preferences"))
      UIMaster.getInstance().showUserPrefsDialog();
    else if(cmd.equals("about"))
      UIMaster.getInstance().showAboutDialog();

    //...

    else if(cmd.equals("home-page"))
      BaseApp.showURL("http://www.temerity.us");
    else if(cmd.equals("support-forums"))
      BaseApp.showURL("http://www.temerity.us");  // FOR NOW...
    else if(cmd.equals("bug-database"))
      BaseApp.showURL("http://www.temerity.us");  // FOR NOW...
    else if(cmd.equals("site-configuration"))
      UIMaster.getInstance().showConfigDialog();

    //...

    else if(cmd.equals("quit"))
      UIMaster.getInstance().doQuit();    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Change the contents of this panel to a JNodeBrowserPanel. 
   */ 
  private void 
  doNodeBrowserPanel()
  {
    JTopLevelPanel dead = (JTopLevelPanel) removeContents();
    setContents(new JNodeBrowserPanel(dead));
    dead.setGroupID(0);
  }

  /**
   * Change the contents of this panel to a JNodeViewerPanel. 
   */ 
  private void 
  doNodeViewerPanel()
  {
    JTopLevelPanel dead = (JTopLevelPanel) removeContents();
    setContents(new JNodeViewerPanel(dead));
    dead.setGroupID(0);
  }

  /**
   * Change the contents of this panel to a JNodeDetailsPanel. 
   */ 
  private void 
  doNodeDetailsPanel()
  {
    JTopLevelPanel dead = (JTopLevelPanel) removeContents();
    setContents(new JNodeDetailsPanel(dead));
    dead.setGroupID(0);
  }

  /**
   * Change the contents of this panel to a JNodeFilesPanel. 
   */ 
  private void 
  doNodeFilesPanel()
  {
    JTopLevelPanel dead = (JTopLevelPanel) removeContents();
    setContents(new JNodeFilesPanel(dead));
    dead.setGroupID(0);
  }

  /**
   * Change the contents of this panel to a JNodeHistoryPanel. 
   */ 
  private void 
  doNodeHistoryPanel()
  {
    JTopLevelPanel dead = (JTopLevelPanel) removeContents();
    setContents(new JNodeHistoryPanel(dead));
    dead.setGroupID(0);
  }

  /**
   * Change the contents of this panel to a JEmptyPanel. 
   */ 
  private void 
  doEmptyPanel()
  {
    JTopLevelPanel dead = (JTopLevelPanel) removeContents();
    setContents(new JEmptyPanel(dead));
    dead.setGroupID(0);    
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
      JManagerPanel select = (JManagerPanel) tab.getSelectedComponent();

      JManagerPanel mgr = new JManagerPanel();
      mgr.setContents(new JEmptyPanel(pTopLevelPanel));

      tab.addTab(mgr);
    }
    
    /* create a new tabbed panel with the contents of this panel as its only tab */ 
    else {
      Component comp = removeContents();
      if(comp != null) {
	JManagerPanel mgr = new JManagerPanel();
	mgr.setContents(comp);

	JTabbedPanel tab = new JTabbedPanel();
	tab.addTab(mgr);

	setContents(tab);
      }
    }
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

      pTopLevelPanel.setGroupID(0);
    }

    /* remove this tab from the parent tabbed pane */ 
    else if(parent instanceof JTabbedPanel) {
      JTabbedPanel tab = (JTabbedPanel) parent;
      tab.remove(this);

      /* if empty, remove the tabbed pane as well */ 
      if(tab.getTabCount() == 0) {
	JManagerPanel grandpa = (JManagerPanel) tab.getParent();
	grandpa.setContents(new JEmptyPanel(pTopLevelPanel));
      }

      pTopLevelPanel.setGroupID(0);
    }

    // DEBUG
    else {
      System.out.print("Ignoring...\n");      
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
      new JOwnerViewDialog(pTopLevelPanel.getAuthor(), pTopLevelPanel.getView());
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
  private void 
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
   * Shutdown the <B>plfilemgr</B>(1), <B>plnotify</B>(1) and <B>plmaster</B>(1) daemons.
   */ 
  private void 
  doShutdownServer() 
  {
    JConfirmDialog confirm = new JConfirmDialog("Are you sure?");
    confirm.setVisible(true);

    if(confirm.wasConfirmed()) {
      UIMaster master = UIMaster.getInstance();
      try {
	master.getMasterMgrClient().shutdown();
      }
      catch(PipelineException ex) {
      }

      System.exit(0);
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
      setIcon(sMenuAnchorPressedIcon);

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
	pSaveLayoutItem.setEnabled(master.getLayoutName() != null);

	pRestoreLayoutMenu.removeAll();
	File dir = new File(PackageInfo.sHomeDir, PackageInfo.sUser + "/.pipeline/layouts");  
	if(!dir.isDirectory()) {
	  UIMaster.getInstance().showErrorDialog
	    ("Error:", "The saved layout directory (" + dir + ") was missing!");
	  pRestoreLayoutMenu.setEnabled(false);
	} 
	else {
	  rebuildRestoreMenu(dir, dir, pRestoreLayoutMenu);
	}
      }

      /* privileged status */ 
      {
	UIMaster master = UIMaster.getInstance();
	try {
	  pShutdownServerItem.setEnabled(master.getMasterMgrClient().isPrivileged(true));
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	}
      }
      
      pPopup.show(e.getComponent(), e.getX(), e.getY()); 
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
      setIcon(sMenuAnchorIcon);
    }

    public void
    popupMenuWillBecomeVisible(PopupMenuEvent e) {} 

    
    
    /*-- HELPERS ---------------------------------------------------------------------------*/

    /**
     * Recursively rebuild the restore layout menu.
     * 
     * @param root
     *   The root saved layout directory.
     * 
     * @param dir 
     *   The current directory.
     * 
     * @param menu
     *   The current parent menu.
     */ 
    private void 
    rebuildRestoreMenu
    (
     File root, 
     File dir,
     JMenu menu
    ) 
    {
      TreeMap<String,File> table = new TreeMap<String,File>();
      {
	File files[] = dir.listFiles();
	int wk;
	for(wk=0; wk<files.length; wk++) 
	  if(files[wk].isFile() || files[wk].isDirectory()) 
	    table.put(files[wk].getName(), files[wk]);
      }

      int rlen = root.getPath().length();
      for(String name : table.keySet()) {
	File file = table.get(name);
	if(file.isDirectory()) {
	  JMenu sub = new JMenu(name);
	  menu.add(sub);

	  rebuildRestoreMenu(root, file, sub);
	}
      }

      for(String name : table.keySet()) {
	File file = table.get(name);
	if(file.isFile()) {
	  JMenuItem item = new JMenuItem(name);
	  item.setActionCommand("restore-layout:" + file.getPath().substring(rlen));
	  item.addActionListener(pPanel);

	  menu.add(item);
	}
      }

      menu.setEnabled(menu.getItemCount() > 0);
    }


    /*-- INTERNALS -------------------------------------------------------------------------*/

    private static final long serialVersionUID = 2138270471079189817L;;

    private JManagerPanel  pPanel;
  }


  /**
   * A anchor icon which shows the group popup menu when pressed.
   */ 
  //private 
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
      int wk;
      for(wk=1; wk<10; wk++) 
	pGroupItems[wk].setEnabled(pTopLevelPanel.isGroupUnused(wk));
      
      pGroupPopup.show(e.getComponent(), e.getX(), e.getY()); 
    }

    /**
     * Invoked when a mouse button has been released on a component. 
     */ 
    public void 
    mouseReleased(MouseEvent e) {}


    /*-- INTERNALS -------------------------------------------------------------------------*/

    private static final long serialVersionUID = -4700928181653009212L; 
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


  private static Icon sMenuAnchorIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("MenuAnchorIcon.png"));

  private static Icon sMenuAnchorPressedIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("MenuAnchorPressedIcon.png"));


  private static Icon sGroupIcons[] = {
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

  private static Icon sGroupSelectedIcons[] = {
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

  private static Icon sGroupDisabledIcons[] = {
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


  private static Icon sLockedLightIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("LockedLightIcon.png"));

  private static Icon sLockedLightOnIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("LockedLightOnIcon.png"));



  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The title bar. 
   */ 
  private JPanel  pTitlePanel;


  /**
   * The panel layout popup menu.
   */ 
  private JPopupMenu  pPopup; 

  /**
   * The panel layout popup menu items.
   */ 
  private JMenuItem  pAddTabItem; 

  private JMenuItem  pAddLeftItem; 
  private JMenuItem  pAddRightItem; 
  private JMenuItem  pAddAboveItem; 
  private JMenuItem  pAddBelowItem; 

  private JMenuItem  pOwnerViewItem;

  private JMenuItem  pShutdownServerItem;

  /**
   * The load layout submenu.
   */ 
  private JMenuItem  pSaveLayoutItem;
  private JMenu      pRestoreLayoutMenu;


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
