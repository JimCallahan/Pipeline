// $Id: JManagerPanel.java,v 1.11 2004/05/05 20:59:19 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*;
import java.awt.event.*;
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
 * <CODE>JManagerPanel</CODE>s using either a {@link JSplitPane JSplitPane} or a 
 * {@link JTabbedPane JTabbedPane}.  Unless this panel is the root panel of the main frame,
 * the user may also destroy the panel possibly causing the parent <CODE>JSplitPane</CODE> 
 * or <CODE>JTabedPane</CODE> to be destroyed as well.
 */ 
public 
class JManagerPanel
  extends JPanel
  implements ComponentListener, ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a manager panel. 
   */
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
	pNodeBrowserItem = item;
	item.setActionCommand("node-browser");
	item.addActionListener(this);
	sub.add(item);  
	
	item = new JMenuItem("Node Viewer");
	pNodeViewerItem = item;
	item.setActionCommand("node-viewer");
	item.addActionListener(this);
	sub.add(item);  
	
	sub.addSeparator();

	item = new JMenuItem("Node Details");
	pNodeDetailsItem = item;
	item.setActionCommand("node-details");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Node Links");
	pNodeLinksItem = item;
	item.setActionCommand("node-links");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Node Files");
	pNodeFilesItem = item;
	item.setActionCommand("node-files");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Node History");
	pNodeHistoryItem = item;
	item.setActionCommand("node-history");
	item.addActionListener(this);
	sub.add(item);  
	
	sub.addSeparator();

	item = new JMenuItem("Queue Manager");
	pQueueManagerItem = item;
	item.setActionCommand("queue-manager");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Job Details");
	pJobDetailsItem = item;
	item.setActionCommand("job-details");
	item.addActionListener(this);
	sub.add(item);  

	sub.addSeparator();

	item = new JMenuItem("Task Timeline");
	pTaskTimelineItem = item;
	item.setActionCommand("task-timeline");
	item.addActionListener(this);
	sub.add(item);  

	item = new JMenuItem("Task Details");
	pTaskDetailsItem = item;
	item.setActionCommand("task-details");
	item.addActionListener(this);
	sub.add(item);  

	sub.addSeparator();

	item = new JMenuItem("None");
	pNoneItem = item;
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

      pPopup.addSeparator();

      item = new JMenuItem("Change Owner|View...");
      pOwnerViewItem = item;
      item.setActionCommand("change-owner-view");
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
	item.setActionCommand("group-" + wk);
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
	JTextField field = new JTextField();
	pOwnerViewField = field;

	Dimension size = new Dimension(120, 19);
	field.setMinimumSize(size);
	field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
	field.setPreferredSize(size);

	field.setHorizontalAlignment(JLabel.CENTER);
	field.setEditable(false);
	
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

    UIMaster.getInstance().addManager(this);
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

    if((child instanceof JSplitPanel) || (child instanceof JTabbedPane)) {
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


  /*----------------------------------------------------------------------------------------*/

  /**
   * Disable new client/server operations until the current operation is complete.
   */ 
  public void 
  disableOps() 
  {
    if(pTopLevelPanel != null) 
      pTopLevelPanel.disableOps();
  }

  /**
   * Reenable client/server operations.
   */ 
  public void 
  enableOps() 
  {
    if(pTopLevelPanel != null) 
      pTopLevelPanel.enableOps();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- COMPONENT LISTNER METHODS -----------------------------------------------------------*/

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
   * {@link JSplitPanel JSplitPanel}.  This is required to get around a rendering problem 
   * where heavyweight body components of this panel are incorrectly rendered over
   * lightweight components still visible.  
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
    if(e.getActionCommand().equals("node-browser"))
      doNodeBrowserPanel();
    else if(e.getActionCommand().equals("node-viewer"))
      doNodeViewerPanel();

    // ...

    else if(e.getActionCommand().equals("none"))
      doEmptyPanel();

    
    /* layout */ 
    else if(e.getActionCommand().equals("add-left"))
      doAddLeft();
    else if(e.getActionCommand().equals("add-right"))
      doAddRight();
    else if(e.getActionCommand().equals("add-above"))
      doAddAbove();
    else if(e.getActionCommand().equals("add-below"))
      doAddBelow();
    else if(e.getActionCommand().equals("add-tab"))
      doAddTab();
    else if(e.getActionCommand().equals("close-panel"))
      doClosePanel();

    /* owner|view */
    else if(e.getActionCommand().equals("change-owner-view"))
      doChangeOwnerView();
    
    /* group */ 
    else if(e.getActionCommand().startsWith("group-")) 
      doGroup(e.getActionCommand());
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

    setContents(new JSplitPanel(JSplitPane.HORIZONTAL_SPLIT, left, right));
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

    setContents(new JSplitPanel(JSplitPane.HORIZONTAL_SPLIT, left, right));
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

    setContents(new JSplitPanel(JSplitPane.VERTICAL_SPLIT, above, below));
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

    setContents(new JSplitPanel(JSplitPane.VERTICAL_SPLIT, above, below));
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
    if(parent instanceof JTabbedPane) {
      JTabbedPane tab = (JTabbedPane) parent;
      JManagerPanel select = (JManagerPanel) tab.getSelectedComponent();

      JManagerPanel mgr = new JManagerPanel();
      mgr.setContents(new JEmptyPanel(pTopLevelPanel));

      tab.addTab(null, sTabIcon, mgr);
    }
    
    /* create a new tabbed panel with the contents of this panel as its only tab */ 
    else {
      Component comp = removeContents();
      if(comp != null) {
	JManagerPanel mgr = new JManagerPanel();
	mgr.setContents(comp);

	JTabbedPane tab = new JTabbedPane();
	tab.addTab(null, sTabIcon, mgr);

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
    if(parent instanceof JSplitPanel) {
      JSplitPanel split = (JSplitPanel) parent;

      Container sparent = split.getParent();
      if(!(sparent instanceof JManagerPanel))
	return;

      Component live = null;
      switch(split.getOrientation()) {
      case JSplitPane.HORIZONTAL_SPLIT:
	if(split.getLeftComponent() == this) 
	  live = split.getRightComponent();
	else if(split.getRightComponent() == this) 
	  live = split.getLeftComponent();
	else 
	  assert(false);
	break;

      case JSplitPane.VERTICAL_SPLIT:
	if(split.getTopComponent() == this) 
	  live = split.getBottomComponent();
	else if(split.getBottomComponent() == this) 
	  live = split.getTopComponent();
	else 
	  assert(false);	
	break;

      default:
	assert(false);
      }
      assert(live != null);
      
      split.removeAll();
      
      JManagerPanel liveMgr = (JManagerPanel) live;
      JManagerPanel grandpa = (JManagerPanel) sparent;
      grandpa.setContents(liveMgr.removeContents());

      pTopLevelPanel.setGroupID(0);
      UIMaster.getInstance().removeManager(liveMgr);
      UIMaster.getInstance().removeManager(this);
    }

    /* remove this tab from the parent tabbed pane */ 
    else if(parent instanceof JTabbedPane) {
      JTabbedPane tab = (JTabbedPane) parent;
      tab.remove(this);

      /* if empty, remove the tabbed pane as well */ 
      if(tab.getTabCount() == 0) {
	JManagerPanel grandpa = (JManagerPanel) tab.getParent();
	grandpa.setContents(new JEmptyPanel(pTopLevelPanel));
      }

      pTopLevelPanel.setGroupID(0);
      UIMaster.getInstance().removeManager(this);
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
    UIMaster master = UIMaster.getInstance();

    TreeMap<String,TreeSet<String>> working = null;
    try {
      working = master.getNodeMgrClient().getWorkingAreas(); 
      
      // DEBUG 
      {
	TreeSet<String> views = new TreeSet<String>();
	views.add("default");
	views.add("texturing");
	views.add("modeling");
	views.add("animation");
	working.put("joe", views);
      }
      // DEBUG 
    }
    catch(PipelineException ex) {
      master.showErrorDialog(ex);
      return;
    }

    JOwnerViewDialog dialog = 
      new JOwnerViewDialog(pTopLevelPanel.getAuthor(), pTopLevelPanel.getView(), working);
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
   String command
  )
  {
    int groupID = Integer.valueOf(command.substring(6, 7));
    pTopLevelPanel.setGroupID(groupID);
    pGroupMenuAnchor.setIcon(sGroupIcons[groupID]);
  }



  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L   C L A S S E S                                                       */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A anchor icon which shows the title bar popup menu when pressed.
   */ 
  private 
  class PopupMenuAnchor
    extends JLabel
    implements MouseListener, PopupMenuListener
  {
    PopupMenuAnchor
    (
     JPanel panel
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


    /*-- MOUSE LISTNER METHODS -------------------------------------------------------------*/

    public void 
    mouseClicked(MouseEvent e) {}
    
    public void 
    mouseEntered(MouseEvent e) {}

    public void 
    mouseExited(MouseEvent e) {}

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
      
      pPopup.show(e.getComponent(), e.getX(), e.getY()); 
    }

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


    /*-- INTERNALS -------------------------------------------------------------------------*/

    private static final long serialVersionUID = 2138270471079189817L;;

    private JPanel  pPanel;
  }


  /**
   * A anchor icon which shows the group popup menu when pressed.
   */ 
  private 
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


    /*-- MOUSE LISTNER METHODS -------------------------------------------------------------*/

    public void 
    mouseClicked(MouseEvent e) {}
    
    public void 
    mouseEntered(MouseEvent e) {}

    public void 
    mouseExited(MouseEvent e) {}

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

    public void 
    mouseReleased(MouseEvent e) {}


    /*-- INTERNALS -------------------------------------------------------------------------*/

    private static final long serialVersionUID = -4700928181653009212L; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -3791561567661137439L;


  private static Icon sTabIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("TabIcon.png"));


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
  private JMenuItem  pNodeBrowserItem;
  private JMenuItem  pNodeViewerItem;
  private JMenuItem  pNodeDetailsItem;
  private JMenuItem  pNodeLinksItem;
  private JMenuItem  pNodeFilesItem;
  private JMenuItem  pNodeHistoryItem;
  private JMenuItem  pQueueManagerItem;
  private JMenuItem  pJobDetailsItem;
  private JMenuItem  pTaskTimelineItem;
  private JMenuItem  pTaskDetailsItem;
  private JMenuItem  pNoneItem;

  private JMenuItem  pAddTabItem; 

  private JMenuItem  pAddLeftItem; 
  private JMenuItem  pAddRightItem; 
  private JMenuItem  pAddAboveItem; 
  private JMenuItem  pAddBelowItem; 
  private JMenuItem  pOwnerViewItem;


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
