// $Id: JManagerPanel.java,v 1.55 2004/12/01 23:03:10 jim Exp $

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
	item.setActionCommand("node-browser-window");
	item.addActionListener(this);
	sub.add(item);  
	
	item = new JMenuItem("Node Viewer");
	item.setActionCommand("node-viewer-window");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Node Details");
	item.setActionCommand("node-details-window");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Node Files");
	item.setActionCommand("node-files-window");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Node History");
	item.setActionCommand("node-history-window");
	item.addActionListener(this);
	sub.add(item);  
	
	sub.addSeparator();

	item = new JMenuItem("Job Browser");
	item.setActionCommand("job-browser-window");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Job Viewer");
	item.setActionCommand("job-viewer-window");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Job Details");
	item.setActionCommand("job-details-window");
	item.addActionListener(this);
	sub.add(item);  

	sub.addSeparator();

	item = new JMenuItem("Task Timeline");
	item.setActionCommand("task-timeline-window");
	item.addActionListener(this);
	item.setEnabled(false); // FOR NOW 
	sub.add(item);  

	item = new JMenuItem("Task Details");
	item.setActionCommand("task-details-window");
	item.addActionListener(this);
	item.setEnabled(false); // FOR NOW    
	sub.add(item);  

	sub.addSeparator();

	item = new JMenuItem("None");
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

	item = new JMenuItem("Node Files");
	item.setActionCommand("node-files");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Node History");
	item.setActionCommand("node-history");
	item.addActionListener(this);
	sub.add(item);  
	
	sub.addSeparator();

	item = new JMenuItem("Job Browser");
	item.setActionCommand("job-browser");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Job Viewer");
	item.setActionCommand("job-viewer");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Job Details");
	item.setActionCommand("job-details");
	item.addActionListener(this);
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

      item = new JMenuItem("Default Editors...");
      item.setActionCommand("default-editors");
      item.addActionListener(this);
      pPopup.add(item);  

      pPopup.addSeparator();

      {
	JMenu sub = new JMenu("Admin");   
	pPopup.add(sub);  

	item = new JMenuItem("Users...");
	item.setActionCommand("manage-users");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Toolsets...");
	item.setActionCommand("manage-toolsets");
	item.addActionListener(this);
	sub.add(item);  

	sub.addSeparator();

	item = new JMenuItem("License Keys...");
	item.setActionCommand("manage-license-keys");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Selection Keys...");
	item.setActionCommand("manage-selection-keys");
	item.addActionListener(this);
	sub.add(item);  

	sub.addSeparator();

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

	item = new JMenuItem("Restore Tool...");
	pRestoreItem = item;
	item.setActionCommand("restore");
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

	item = new JMenuItem("Quick Reference...");
	item.setActionCommand("quick-reference");
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
	item.setActionCommand("support-forums");
	item.addActionListener(this);
	sub.add(item);  
	  
	item = new JMenuItem("Bug Database...");
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
      JPanel tpanel = new JPanel();
      pTitlePanel = tpanel;

      tpanel.setName("PanelBar");
      tpanel.setLayout(new BoxLayout(tpanel, BoxLayout.Y_AXIS)); 

      {
	JLabel label = UIMaster.createLabel("X", 120, JLabel.LEFT);
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
	  anchor.setToolTipText(UIMaster.formatToolTip("The main menu."));
	  panel.add(anchor);
	}
	
	panel.add(Box.createRigidArea(new Dimension(8, 0)));
	
	{
	  GroupMenuAnchor anchor = new GroupMenuAnchor();
	  pGroupMenuAnchor = anchor;
	  
	  anchor.addMouseListener(this);
	  anchor.setToolTipText(UIMaster.formatToolTip("The panel group selector."));
	  panel.add(anchor);	
	}
	
	panel.add(Box.createRigidArea(new Dimension(4, 0)));
	
	{
	  JTextField field = UIMaster.createTextField(null, 120, JLabel.CENTER);
	  pOwnerViewField = field;
	  
	  field.addMouseListener(this);
	  field.setToolTipText(UIMaster.formatToolTip
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
	  label.setToolTipText(UIMaster.formatToolTip
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
	  
	  btn.setToolTipText(UIMaster.formatToolTip("Closes the panel."));
	  
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
   * Get the frame containing this panel.
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
    else if((prefs.getManagerNodeHistoryWindow() != null) &&
	    prefs.getManagerNodeHistoryWindow().wasPressed(e)) {
      doNodeHistoryWindow();
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
    else if((prefs.getManagerNodeHistoryPanel() != null) &&
	    prefs.getManagerNodeHistoryPanel().wasPressed(e)) {
      doNodeHistoryPanel();
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
    else if((prefs.getManagerAddTab() != null) &&
	    prefs.getManagerAddTab().wasPressed(e)) {
      doAddTab();
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
      UIMaster.getInstance().doSaveLayout();
      return true;
    }
    else if((prefs.getShowManageLayouts() != null) &&
	    prefs.getShowManageLayouts().wasPressed(e)) {
      UIMaster.getInstance().showManageLayoutsDialog();
      return true;
    }

    else if((prefs.getShowUserPrefs() != null) &&
	    prefs.getShowUserPrefs().wasPressed(e)) {
      UIMaster.getInstance().showUserPrefsDialog();
      return true;
    }
    else if((prefs.getShowDefaultEditors() != null) &&
	    prefs.getShowDefaultEditors().wasPressed(e)) {
      UIMaster.getInstance().showDefaultEditorsDialog();
      return true;
    }
//     else if((prefs.getShowManageJobServers() != null) &&
// 	    prefs.getShowManageJobServers().wasPressed(e)) {
//       UIMaster.getInstance().showManageJobServersDialog();
//       return true;
//     }

    else if((prefs.getShowManageUsers() != null) &&
	    prefs.getShowManageUsers().wasPressed(e)) {
      UIMaster.getInstance().showManageUsersDialog();
      return true;
    }
    else if((prefs.getShowManageToolsets() != null) &&
	    prefs.getShowManageToolsets().wasPressed(e)) {
      UIMaster.getInstance().showManageToolsetsDialog();
      return true;
    }
    else if((prefs.getShowManageLicenseKeys() != null) &&
	    prefs.getShowManageLicenseKeys().wasPressed(e)) {
      UIMaster.getInstance().showManageLicenseKeysDialog();
      return true;
    }
    else if((prefs.getShowManageSelectionKeys() != null) &&
	    prefs.getShowManageSelectionKeys().wasPressed(e)) {
      UIMaster.getInstance().showManageSelectionKeysDialog();
      return true;
    }

    else if((prefs.getQuit() != null) &&
	    prefs.getQuit().wasPressed(e)) {
      UIMaster.getInstance().doQuit();    
      return true;
    }

    /* help */ 
    else if((prefs.getShowAbout() != null) &&
	    prefs.getShowAbout().wasPressed(e)) {
      UIMaster.getInstance().showAboutDialog();
      return true;
    }
    else if((prefs.getShowQuickReference() != null) &&
	    prefs.getShowQuickReference().wasPressed(e)) {
      BaseApp.showURL("file:///" + PackageInfo.sDocsDir + "/manuals/quick-reference.html");
      return true;
    }

    else if((prefs.getShowHomePage() != null) &&
	    prefs.getShowHomePage().wasPressed(e)) {
      BaseApp.showURL("http://www.temerity.us");
      return true;
    }
    else if((prefs.getShowSupportForums() != null) &&
	    prefs.getShowSupportForums().wasPressed(e)) {
      BaseApp.showURL("http://www.temerity.us/forums");
      return true;
    }
    else if((prefs.getShowBugDatabase() != null) &&
	    prefs.getShowBugDatabase().wasPressed(e)) {
      BaseApp.showURL("http://www.temerity.us/bugs");  
      return true;
    }
    
    else if((prefs.getShowConfig() != null) &&
	    prefs.getShowConfig().wasPressed(e)) {
      UIMaster.getInstance().showConfigDialog();
      return true;
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
    else if(cmd.equals("node-history-window"))
      doNodeHistoryWindow();

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
    else if(cmd.equals("node-history"))
      doNodeHistoryPanel();

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
      UIMaster.getInstance().doSaveLayout();
    else if(cmd.equals("save-layout-as"))
      UIMaster.getInstance().showSaveLayoutDialog();
    else if(cmd.startsWith("restore-layout:")) 
      UIMaster.getInstance().doRestoreSavedLayout(cmd.substring(15));
    else if(cmd.equals("manage-layouts"))
      UIMaster.getInstance().showManageLayoutsDialog();

    else if(cmd.equals("preferences"))
      UIMaster.getInstance().showUserPrefsDialog();
    else if(cmd.equals("default-editors"))
      UIMaster.getInstance().showDefaultEditorsDialog();
//     else if(cmd.equals("manage-job-servers"))
//       UIMaster.getInstance().showManageJobServersDialog();

    else if(cmd.equals("manage-users"))
      UIMaster.getInstance().showManageUsersDialog();
    else if(cmd.equals("manage-toolsets"))
      UIMaster.getInstance().showManageToolsetsDialog();
    else if(cmd.equals("manage-license-keys"))
      UIMaster.getInstance().showManageLicenseKeysDialog();
    else if(cmd.equals("manage-selection-keys"))
      UIMaster.getInstance().showManageSelectionKeysDialog();
    else if(cmd.equals("backup-database"))
      UIMaster.getInstance().showBackupDialog();
    else if(cmd.equals("archive"))
      UIMaster.getInstance().showArchiveDialog();
    else if(cmd.equals("restore"))
      UIMaster.getInstance().showRestoreDialog();
    else if(cmd.equals("shutdown"))
      doShutdownServer();

    else if(cmd.equals("about"))
      UIMaster.getInstance().showAboutDialog();
    else if(cmd.equals("quick-reference"))
      BaseApp.showURL("file:///" + PackageInfo.sDocsDir + "/manuals/quick-reference.html");

    else if(cmd.equals("home-page"))
      BaseApp.showURL("http://www.temerity.us");
    else if(cmd.equals("support-forums"))
      BaseApp.showURL("http://www.temerity.us/forums");
    else if(cmd.equals("bug-database"))
      BaseApp.showURL("http://www.temerity.us/bugs");  

    else if(cmd.equals("site-configuration"))
      UIMaster.getInstance().showConfigDialog();

    else if(cmd.equals("quit"))
      UIMaster.getInstance().doQuit();    
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
    mgr.setContents(new JNodeBrowserPanel(pTopLevelPanel));
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
    mgr.setContents(new JNodeViewerPanel(pTopLevelPanel));
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

    updateNodeSubpanels(panel.getGroupID());
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
    
    updateNodeSubpanels(panel.getGroupID());
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

    updateNodeSubpanels(panel.getGroupID());
  }


  /*----------------------------------------------------------------------------------------*/

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
      JWindowRenameDialog diag = new JWindowRenameDialog(frame.getWindowName());
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

    JTopLevelPanel dead = (JTopLevelPanel) removeContents();
    setContents(new JNodeBrowserPanel(dead));
    dead.setGroupID(0);
    refocusOnChildPanel();
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

    JTopLevelPanel dead = (JTopLevelPanel) removeContents();
    setContents(new JNodeViewerPanel(dead));
    dead.setGroupID(0);
    refocusOnChildPanel();
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

    JTopLevelPanel dead = (JTopLevelPanel) removeContents();
    JNodeDetailsPanel panel = new JNodeDetailsPanel(dead);
    setContents(panel);
    dead.setGroupID(0);
    refocusOnChildPanel();
    
    updateNodeSubpanels(panel.getGroupID());
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

    JTopLevelPanel dead = (JTopLevelPanel) removeContents();
    JNodeFilesPanel panel = new JNodeFilesPanel(dead);
    setContents(panel);
    dead.setGroupID(0);
    refocusOnChildPanel();

    updateNodeSubpanels(panel.getGroupID());
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

    JTopLevelPanel dead = (JTopLevelPanel) removeContents();
    JNodeHistoryPanel panel = new JNodeHistoryPanel(dead);
    setContents(panel);
    dead.setGroupID(0);
    refocusOnChildPanel();

    updateNodeSubpanels(panel.getGroupID());
  }

  /**
   * Helper method for updating all node subpanels for the given group.
   */ 
  private void 
  updateNodeSubpanels
  (
   int groupID
  ) 
  {
    if(groupID > 0) {
      PanelGroup<JNodeViewerPanel> panels = UIMaster.getInstance().getNodeViewerPanels();
      JNodeViewerPanel viewer = panels.getPanel(groupID);
      if(viewer != null) 
	viewer.updateSubPanels(false);
    }    
  }


  /*----------------------------------------------------------------------------------------*/
  
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

    JTopLevelPanel dead = (JTopLevelPanel) removeContents();
    setContents(new JQueueJobBrowserPanel(dead));
    dead.setGroupID(0);
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

    JTopLevelPanel dead = (JTopLevelPanel) removeContents();
    setContents(new JQueueJobViewerPanel(dead));
    dead.setGroupID(0);
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

    JTopLevelPanel dead = (JTopLevelPanel) removeContents();
    setContents(new JQueueJobDetailsPanel(dead));
    dead.setGroupID(0);
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

    JTopLevelPanel dead = (JTopLevelPanel) removeContents();
    setContents(new JEmptyPanel(dead));
    dead.setGroupID(0);  
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
      JManagerPanel select = (JManagerPanel) tab.getSelectedComponent();

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
    }

    /* remove this tab from the parent tabbed pane */ 
    else if(parent instanceof JTabbedPanel) {
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
   * Shutdown the server daemons, close the network connections and exit.
   */ 
  private void 
  doShutdownServer() 
  {
    JConfirmDialog confirm = new JConfirmDialog("Are you sure?");
    confirm.setVisible(true);

    if(confirm.wasConfirmed()) {
      UIMaster master = UIMaster.getInstance();

      master.getQueueMgrClient().disconnect();

      try {
	master.getMasterMgrClient().shutdown();
      }
      catch(PipelineException ex) {
      }

      /* give the sockets time to disconnect cleanly */ 
      try {
	Thread.sleep(500);
      }
      catch(InterruptedException ex) {
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
	  boolean isPrivileged = master.getMasterMgrClient().isPrivileged(true);
	  pBackupDatabaseItem.setEnabled(isPrivileged);
	  pArchiveItem.setEnabled(isPrivileged);
	  pRestoreItem.setEnabled(isPrivileged);
	  pShutdownServerItem.setEnabled(isPrivileged);
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
  private JMenuItem  pRenameWindowItem; 
  
  private JMenuItem  pAddTabItem; 

  private JMenuItem  pAddLeftItem; 
  private JMenuItem  pAddRightItem; 
  private JMenuItem  pAddAboveItem; 
  private JMenuItem  pAddBelowItem; 

  private JMenuItem  pOwnerViewItem;

  private JMenuItem  pBackupDatabaseItem;
  private JMenuItem  pArchiveItem;
  private JMenuItem  pRestoreItem;
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
