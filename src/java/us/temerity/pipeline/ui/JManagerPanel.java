// $Id: JManagerPanel.java,v 1.9 2004/05/03 04:28:25 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

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
  implements ActionListener
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

    pAuthor = PackageInfo.sUser;
    pView   = "default";

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

    /* panel title bar */ 
    {
      JPanel panel = new JPanel();
      pTitlePanel = panel;

      panel.setName("PanelBar");
      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS)); 

      panel.setMinimumSize(new Dimension(230, 29));
      panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 29));
      panel.setPreferredSize(new Dimension(230, 29));

      {
	JMenuAnchor anchor = new JMenuAnchor(this);
	pPopup.addPopupMenuListener(anchor);
	panel.add(anchor);
      }

      panel.add(Box.createRigidArea(new Dimension(16, 0)));

      {
	JToggleButton btn = new JToggleButton();
	btn.setName("TargetButton");

	Dimension size = new Dimension(15, 19);
	btn.setMinimumSize(size);
	btn.setMaximumSize(size);
	btn.setPreferredSize(size);
	
	btn.setActionCommand("target-panel");
        btn.addActionListener(this);

	panel.add(btn);
      }

      panel.add(Box.createRigidArea(new Dimension(4, 0)));

      {
	JTextField field = new JTextField(pAuthor + " | " + pView);
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
	JLabel label = new JLabel(sLockedLightIcon);
	pLockedLight = label;
	
	Dimension size = new Dimension(16, 19);
	label.setMinimumSize(size);
	label.setMaximumSize(size);
	label.setPreferredSize(size);
	
	panel.add(label);
      }

      panel.add(Box.createRigidArea(new Dimension(16, 0)));

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

    UIMaster.getInstance().addManager(this);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Is the contents of the panel read-only.
   */ 
  public boolean
  isLocked() 
  {
    return pIsLocked;
  }

  /**
   * Change the locked state of the panel.
   */ 
  public void 
  setLocked
  ( 
   boolean tf
  ) 
  {
    pIsLocked = tf;
    pLockedLight.setIcon(pIsLocked ? sLockedLightOnIcon : sLockedLightIcon);
  }


  /** 
   * Get the name of user which owns the working area view.
   */ 
  public String
  getAuthor() 
  {
    return pAuthor; 
  }

  /** 
   * Get the name of the working area view.
   */
  public String
  getView()
  {
    return pView;
  }

  /**
   * Copy the author and view from the given manager panel.
   */ 
  public void 
  setAuthorView
  (
   JManagerPanel mgr
  ) 
  {
    setAuthorView(mgr.getAuthor(), mgr.getView());
  }

  /**
   * Set the author and view.
   */ 
  public void 
  setAuthorView
  (
   String author, 
   String view 
  ) 
  {
    if((author != null) && (view != null)) {
      pAuthor = author;
      pView   = view;
      
      pOwnerViewField.setText(pAuthor + " | " + pView);
      
      setLocked(!pAuthor.equals(PackageInfo.sUser));
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Set sole child component of the panel.
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

    if(!((child instanceof JSplitPanel) ||
	 (child instanceof JTabbedPane))) {
      add(pTitlePanel);
    }
    
    // .. set pSelectorCombo
    
    add(child);
    
    validate();
    repaint();
  }
    
  /**
   * Remove the sole child component of the panel.
   * 
   * @return 
   *   The removed child or <CODE>null</CODE> if there was no child.
   */ 
  public Component
  removeContents() 
  { 
    int idx = getComponentCount()-1;
    if(idx == -1) 
      return null;
    
    Component old = getComponent(idx);
    removeAll();
    return old;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Show the popup menu.
   */ 
  public void 
  showPopup
  (
   MouseEvent e
  )
  {
    /* only enable layout changes if there is enough space */ 
    {
      pAddTabItem.setEnabled(getHeight() > 29+20);
      
      boolean horz = (getWidth() > (230*2 + 11));
      pAddLeftItem.setEnabled(horz);
      pAddRightItem.setEnabled(horz);
      
      boolean vert = (getHeight() > (29*2 + 10));
      pAddAboveItem.setEnabled(vert);
      pAddBelowItem.setEnabled(vert);
    }

    pPopup.show(e.getComponent(), e.getX(), e.getY()); 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Disable new client/server operations until the current operation is complete.
   */ 
  public void 
  disableOps() 
  {

    // ...

  }

  /**
   * Reenable client/server operations.
   */ 
  public void 
  enableOps() 
  {

    // ...

  }



  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

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
    if(e.getActionCommand().equals("add-left"))
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

    // ...

    else if(e.getActionCommand().equals("change-owner-view"))
      doChangeOwnerView();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
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
      left.setContents(new JEmptyPanel());
      left.setAuthorView(this);
    }

    JManagerPanel right = null;
    {    
      right = new JManagerPanel();
      right.setContents(removeContents());
      right.setAuthorView(this);
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
      left.setAuthorView(this);
    }

    JManagerPanel right = null;
    {    
      right = new JManagerPanel();
      right.setContents(new JEmptyPanel());
      right.setAuthorView(this);
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
      above.setContents(new JEmptyPanel());
      above.setAuthorView(this);
    }

    JManagerPanel below = null;
    {    
      below = new JManagerPanel();
      below.setContents(removeContents());
      below.setAuthorView(this);
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
      above.setAuthorView(this);
    }

    JManagerPanel below = null;
    {    
      below = new JManagerPanel();
      below.setContents(new JEmptyPanel());
      below.setAuthorView(this);
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
      mgr.setContents(new JEmptyPanel());
      mgr.setAuthorView(select);

      tab.addTab(null, sTabIcon, mgr);
    }
    
    /* create a new tabbed panel with the contents of this panel as its only tab */ 
    else {
      Component comp = removeContents();
      if(comp != null) {
	JManagerPanel mgr = new JManagerPanel();
	mgr.setContents(comp);
	mgr.setAuthorView(this);

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
      grandpa.setAuthorView(liveMgr);

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
	grandpa.setContents(new JEmptyPanel());
	grandpa.setAuthorView(this);
      }

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
      working = master.getWorkingAreas(); 
      
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

    JOwnerViewDialog dialog = new JOwnerViewDialog(pAuthor, pView, working);
    dialog.setVisible(true);
    
    if(dialog.wasConfirmed())
      setAuthorView(dialog.getAuthor(), dialog.getView());
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -3791561567661137439L;


  static private Icon sTabIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("TabIcon.png"));


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
   * The popup menu items.
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
   * The combo box used to select the panel type.
   */ 
  private JTextField  pOwnerViewField;

  /** 
   * The name of user which owns the working area view associated with this panel.
   */
  private String  pAuthor;

  /** 
   * The name of the working area view associated with this panel.
   */
  private String  pView;


  /**
   * The light indicating that the contents of the panel is read-only.
   */ 
  private JLabel  pLockedLight;
  
  /**
   * Whether the contents of the panel is read-only.
   */   
  private boolean  pIsLocked;

}
