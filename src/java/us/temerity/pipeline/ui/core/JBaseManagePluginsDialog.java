// $Id: JBaseManagePluginsDialog.java,v 1.2 2005/01/05 17:41:49 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*; 

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   M A N A G E   P L U G I N S   D I A L O G                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The base clase of all dialogs which edit the plugin selection menus.
 */ 
public abstract
class JBaseManagePluginsDialog
  extends JBaseDialog
  implements MouseListener, ActionListener, TreeSelectionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   * 
   * @param title
   *   The title of the dialog.
   */ 
  protected
  JBaseManagePluginsDialog
  (
   String title
  ) 
  {
    super(title, false);

    /* initialize fields */ 
    {
      pMenuLayout     = new PluginMenuLayout();
      pPluginVersions = new TreeMap<String,TreeSet<VersionID>>();
      pIsPrivileged   = false;
    }

    /* layout popup menu */ 
    {
      JMenuItem item;
      
      pPopup = new JPopupMenu();  
 
      item = new JMenuItem("New Menu...");
      pNewSubmenu = item;
      item.setActionCommand("new-submenu");
      item.addActionListener(this);
      pPopup.add(item);  
   
      item = new JMenuItem("New Item...");
      pNewMenuItem = item;
      item.setActionCommand("new-menu-item");
      item.addActionListener(this);
      pPopup.add(item);  
   
      pPopup.addSeparator();

      item = new JMenuItem("Rename...");
      pRenameMenu = item;
      item.setActionCommand("rename-menu");
      item.addActionListener(this);
      pPopup.add(item);  

      pPopup.addSeparator();

      item = new JMenuItem("Delete...");
      pDeleteMenu = item;
      item.setActionCommand("delete-menu");
      item.addActionListener(this);
      pPopup.add(item);  
    }

    /* create dialog body components */ 
    {
      Box body = new Box(BoxLayout.X_AXIS);

      body.add(Box.createRigidArea(new Dimension(20, 0)));

      {
	Box vbox = new Box(BoxLayout.Y_AXIS);
	
	vbox.add(Box.createRigidArea(new Dimension(0, 20)));
	
	vbox.add(UIFactory.createPanelLabel("Menu Layout:"));
	
	vbox.add(Box.createRigidArea(new Dimension(0, 4)));
	
	{
	  DefaultMutableTreeNode root = 
	    new DefaultMutableTreeNode(new MenuLayoutData(), true);
	  DefaultTreeModel model = new DefaultTreeModel(root, true);
	  
	  JTree tree = new JFancyTree(model); 
	  pMenuLayoutTree = tree;
	  tree.setName("DarkTree");
	  
	  tree.setCellRenderer(new JMenuLayoutTreeCellRenderer());
	  tree.getSelectionModel().setSelectionMode
	    (TreeSelectionModel.SINGLE_TREE_SELECTION);
	  tree.setExpandsSelectedPaths(true);
	  
	  tree.addMouseListener(this);
	  tree.addTreeSelectionListener(this);
	  
	  {
	    JScrollPane scroll = new JScrollPane(tree);
	    
	    Dimension size = new Dimension(sLWidth, sLHeight);
	    scroll.setMinimumSize(size);
	    scroll.setPreferredSize(size);
	    
	    scroll.setHorizontalScrollBarPolicy
	      (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	    scroll.setVerticalScrollBarPolicy
	      (ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
	    
	    vbox.add(scroll);
	  }
	  
	  vbox.add(Box.createRigidArea(new Dimension(0, 20)));
	  
	  body.add(vbox);
	}
      }
      
      body.add(Box.createRigidArea(new Dimension(4, 0)));
      
      {
	Box vbox = new Box(BoxLayout.Y_AXIS);
	
	vbox.add(Box.createVerticalGlue());
	
	{
	  JButton btn = new JButton();
	  pSetPluginButton = btn;
	  btn.setName("LeftArrowButton");
	  
	  Dimension size = new Dimension(16, 16);
	  btn.setMinimumSize(size);
	  btn.setMaximumSize(size);
	  btn.setPreferredSize(size);
	  
	  btn.setActionCommand("set-plugin");
	  btn.addActionListener(this);
	  
	  vbox.add(btn);
	} 
	
	vbox.add(Box.createRigidArea(new Dimension(0, 16)));
	
	{
	  JButton btn = new JButton();
	  pRemovePluginButton = btn;
	  btn.setName("RightArrowButton");
	  
	  Dimension size = new Dimension(16, 16);
	  btn.setMinimumSize(size);
	  btn.setMaximumSize(size);
	  btn.setPreferredSize(size);
	  
	  btn.setActionCommand("remove-plugin");
	  btn.addActionListener(this);
	  
	  vbox.add(btn);
	} 
	
	vbox.add(Box.createVerticalGlue());
	
	body.add(vbox);
      }
      
      body.add(Box.createRigidArea(new Dimension(4, 0)));
	
      {
	Box vbox = new Box(BoxLayout.Y_AXIS);
	
	vbox.add(Box.createRigidArea(new Dimension(0, 20)));
	
	vbox.add(UIFactory.createPanelLabel("Plugin Versions:"));
	
	vbox.add(Box.createRigidArea(new Dimension(0, 4)));
	
	{
	  DefaultMutableTreeNode root = 
	    new DefaultMutableTreeNode(new PluginVersionData(), true);
	  DefaultTreeModel model = new DefaultTreeModel(root, true);
	  
	  JTree tree = new JFancyTree(model); 
	  pPluginVersionTree = tree;
	  tree.setName("DarkTree");
	  
 	  tree.setCellRenderer(new JPluginVersionTreeCellRenderer());
	  tree.getSelectionModel().setSelectionMode
	    (TreeSelectionModel.SINGLE_TREE_SELECTION);
	  tree.setExpandsSelectedPaths(true);
	  
	  tree.addTreeSelectionListener(this);
	  
	  {
	    JScrollPane scroll = new JScrollPane(tree);
	    
	    Dimension size = new Dimension(sLWidth, sLHeight);
	    scroll.setMinimumSize(size);
	    scroll.setPreferredSize(size);
	    
	    scroll.setHorizontalScrollBarPolicy
	      (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	    scroll.setVerticalScrollBarPolicy
	      (ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
	    
	    vbox.add(scroll);
	  }
	  
	  vbox.add(Box.createRigidArea(new Dimension(0, 20)));
	  
	  body.add(vbox);
	}
      }

      body.add(Box.createRigidArea(new Dimension(20, 0)));

      super.initUI(title + ":", false, body, "Confirm", "Apply", null, "Close");
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the UI components to display the given plugin menu layout.
   * 
   * @param layout
   *   The current plugin menu layout.
   * 
   * @param plugins
   *   The names of versions of the loaded plugins.
   * 
   * @param isPrivileged
   *   Whether the current user is privileged.
   */ 
  protected void 
  updateMenuLayout
  (
   PluginMenuLayout layout, 
   TreeMap<String,TreeSet<VersionID>> plugins, 
   boolean isPrivileged
  ) 
  {
    pMenuLayout     = layout;
    pPluginVersions = plugins;
    pIsPrivileged   = isPrivileged;

    rebuildMenuLayout();
    rebuildPluginVersions();

    pSetPluginButton.setEnabled(false);
    pRemovePluginButton.setEnabled(false);

    pConfirmButton.setEnabled(pIsPrivileged);
    pApplyButton.setEnabled(pIsPrivileged);
  }

  
  /**
   * Get the plugin menu layout specified by the UI components.
   */
  protected PluginMenuLayout
  getMenuLayout() 
  {
    
    // ...
    

    return new PluginMenuLayout();
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the layout menu.
   *
   * @param tpath
   *   The path of the tree node under the mouse.
   */ 
  private void 
  updatePopupMenu
  (
   TreePath tpath
  ) 
  {    
    if(tpath != null) {
      pMenuLayoutTree.addSelectionPath(tpath);

      DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) tpath.getLastPathComponent();
      MenuLayoutData data = (MenuLayoutData) tnode.getUserObject();
  
      pNewSubmenu.setEnabled(!data.isItem());
      pNewMenuItem.setEnabled(!data.isItem());
      pRenameMenu.setEnabled(true);
      pDeleteMenu.setEnabled(true);

      return;
    }

    pNewSubmenu.setEnabled(false);
    pNewMenuItem.setEnabled(false);
    pRenameMenu.setEnabled(true);
    pDeleteMenu.setEnabled(true);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   C O M P O N E N T   O V E R R I D E S                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Shows or hides this component.
   */ 
  public void 
  setVisible
  (
   boolean isVisible
  )
  {
    if(!isVisible) {
      

    }

    super.setVisible(isVisible);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- TREE SELECTION LISTENER METHODS -----------------------------------------------------*/

  /**
   * Called whenever the value of the selection changes.
   */ 
  public void 
  valueChanged
  (
   TreeSelectionEvent e
  )
  { 
    MenuLayoutData mdata = null;
    {
      TreePath mpath = pMenuLayoutTree.getSelectionPath();
      if(mpath != null) {
	DefaultMutableTreeNode mnode = (DefaultMutableTreeNode) mpath.getLastPathComponent();
	mdata = (MenuLayoutData) mnode.getUserObject();
      }
    }

    PluginVersionData vdata = null;
    {
      TreePath vpath = pPluginVersionTree.getSelectionPath();
      if(vpath != null) {
	DefaultMutableTreeNode vnode = (DefaultMutableTreeNode) vpath.getLastPathComponent();
	vdata = (PluginVersionData) vnode.getUserObject();
      }
    }
    
    if(e.getSource() == pMenuLayoutTree) {
      if((mdata != null) && mdata.isItem() && 
	 (mdata.getName() != null) && (mdata.getVersionID() != null)) {
	selectPluginVersion(mdata.getName(), mdata.getVersionID());
	pRemovePluginButton.setEnabled(true);
      }
      else {
	pRemovePluginButton.setEnabled(false);
      }
    }
    else if(e.getSource() == pPluginVersionTree) {
      if((mdata != null) && mdata.isItem() && 
	 (vdata != null) && (vdata.getName() != null) && (vdata.getVersionID() != null)) {
	pSetPluginButton.setEnabled(true); 
      }
      else {
	pSetPluginButton.setEnabled(false);
      }
    }
  }


          
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
    if(!pIsPrivileged) 
      return;

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
	
	/* BUTTON3: popup menu */ 
	if((mods & (on1 | off1)) == on1) {
	  TreePath tpath = pMenuLayoutTree.getClosestPathForLocation(e.getX(), e.getY());
	  if(tpath != null) {
	    Rectangle bounds = pMenuLayoutTree.getPathBounds(tpath);
	    if(!bounds.contains(e.getX(), e.getY()))
	      tpath = null;
	  }

	  updatePopupMenu(tpath);
	  pPopup.show(e.getComponent(), e.getX(), e.getY());
	}
      }
    }
  }

  /**
   * Invoked when a mouse button has been released on a component. 
   */ 
  public void 
  mouseReleased(MouseEvent e) {} 


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
    super.actionPerformed(e);

    String cmd = e.getActionCommand();
    if(cmd.equals("new-submenu")) 
      doNewSubmenu();
    else if(cmd.equals("new-menu-item")) 
      doNewMenuItem();
    if(cmd.equals("rename-menu")) 
      doRenameMenu();
    if(cmd.equals("delete-menu")) 
      doDeleteMenu();
    if(cmd.equals("set-plugin"))
      doSetPlugin();
    if(cmd.equals("remove-plugin"))
      doRemovePlugin();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Apply changes and close. 
   */ 
  public void 
  doConfirm()
  {
    doApply();
    super.doConfirm();
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * 
   */ 
  public void 
  doNewSubmenu()
  {
    JNewNameDialog diag = new JNewNameDialog(this, "New Menu", "New Menu Name:", null, "Add");
    if(diag.wasConfirmed()) {
      String name = diag.getName();
      
      // .. .

    }
  }

  /**
   * 
   */ 
  public void 
  doNewMenuItem()
  {
    JNewNameDialog diag = new JNewNameDialog(this, "New Item", "New Item Name:", null, "Add");
    if(diag.wasConfirmed()) {
      String name = diag.getName();
      
      // .. .

    }
    
  }

  /**
   * 
   */ 
  public void 
  doRenameMenu()
  {
    
  }

  /**
   * 
   */ 
  public void 
  doDeleteMenu()
  {
    
  }
 

  /*----------------------------------------------------------------------------------------*/

  /**
   * 
   */ 
  public void 
  doSetPlugin()
  {
    
  }

  /**
   * 
   */ 
  public void 
  doRemovePlugin()
  {
    
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Rebuild the menu layout tree nodes.
   */ 
  private void 
  rebuildMenuLayout() 
  {
    DefaultMutableTreeNode root = 
      new DefaultMutableTreeNode(new MenuLayoutData(), true);
    
    for(PluginMenuLayout pml : pMenuLayout) 
      rebuildMenuLayout(pml, root);
    
    DefaultTreeModel model = (DefaultTreeModel) pMenuLayoutTree.getModel();
    model.setRoot(root);      
  }

  /**
   * Recursively rebuild the menu layout tree nodes.
   */ 
  private void 
  rebuildMenuLayout
  (
   PluginMenuLayout layout, 
   DefaultMutableTreeNode root
  ) 
  {
    if(layout.isItem()) {
      String name = layout.getName();
      VersionID vid = layout.getVersionID();

      TreeSet<VersionID> vids = pPluginVersions.get(name);
      if((vids == null) || !vids.contains(vid)) {
	name = null;
	vid = null;
      }

      DefaultMutableTreeNode vnode = 
	new DefaultMutableTreeNode(new MenuLayoutData(layout.getTitle(), name, vid), false);
      root.add(vnode);
    }
    else {
      DefaultMutableTreeNode vnode = 
	new DefaultMutableTreeNode(new MenuLayoutData(layout.getTitle()), true);
      root.add(vnode);

      for(PluginMenuLayout pml : layout) 
	rebuildMenuLayout(pml, vnode);
    }
  }



  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Rebuild the plugin version tree nodes 
   */ 
  private void 
  rebuildPluginVersions()
  {
    DefaultMutableTreeNode root = new DefaultMutableTreeNode(new PluginVersionData());

    for(String name : pPluginVersions.keySet()) {
      DefaultMutableTreeNode pnode = 
	new DefaultMutableTreeNode(new PluginVersionData(name), true);
      root.add(pnode);
      
      for(VersionID vid : pPluginVersions.get(name)) {
	DefaultMutableTreeNode vnode = 
	  new DefaultMutableTreeNode(new PluginVersionData(name, vid), false);
	pnode.add(vnode);
      }
    }
    
    DefaultTreeModel model = (DefaultTreeModel) pPluginVersionTree.getModel();
    model.setRoot(root);
  }

  /**
   * Select the plugin version tree node for the given plugin version.
   */ 
  private void 
  selectPluginVersion
  (
   String name, 
   VersionID vid
  ) 
  {
    pPluginVersionTree.clearSelection();

    DefaultTreeModel model = (DefaultTreeModel) pPluginVersionTree.getModel();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();	  
    int wk;
    for(wk=0; wk<root.getChildCount(); wk++) {
      DefaultMutableTreeNode pnode = (DefaultMutableTreeNode) root.getChildAt(wk);  

      int vk;
      for(vk=0; vk<pnode.getChildCount(); vk++) {
	DefaultMutableTreeNode vnode = (DefaultMutableTreeNode) pnode.getChildAt(vk); 
	PluginVersionData vdata = (PluginVersionData) vnode.getUserObject();
	TreePath vpath = new TreePath(vnode.getPath());

	if(name.equals(vdata.getName()) && vid.equals(vdata.getVersionID())) {
	  pPluginVersionTree.addSelectionPath(vpath);
	  return;
	}
      }
    }    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P U B L I C   C L A S S E S                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * User data of menu layout tree nodes.
   */ 
  public
  class MenuLayoutData
  {
    public 
    MenuLayoutData() 
    {}

    public 
    MenuLayoutData
    ( 
     String label
    )
    {
      pLabel = label;
    }

    public 
    MenuLayoutData
    ( 
     String label, 
     String name, 
     VersionID vid
    )
    {
      pLabel     = label;
      pIsItem    = true;
      pName      = name;
      pVersionID = vid; 
    }

    public String
    getLabel() 
    {
      return pLabel;
    }
    
    public boolean
    isItem()
    {
      return pIsItem;
    }

    public String
    getName() 
    {
      return pName;
    }

    public VersionID
    getVersionID()
    {
      return pVersionID;
    }    

    public String
    toString()
    {
      return pLabel;
    }

    private String     pLabel;
    private boolean    pIsItem; 
    private String     pName;
    private VersionID  pVersionID; 
  }

  /**
   * User data of plugin version tree nodes.
   */ 
  public
  class PluginVersionData
  {
    public 
    PluginVersionData() 
    {}

    public 
    PluginVersionData
    ( 
     String label
    )
    {
      pLabel = label;
    }

    public 
    PluginVersionData
    ( 
     String name, 
     VersionID vid
    )
    {
      pLabel     = ("v" + vid);
      pName      = name;
      pVersionID = vid; 
    }

    public String
    getLabel() 
    {
      return pLabel;
    }

    public String
    getName() 
    {
      return pName;
    }

    public VersionID
    getVersionID()
    {
      return pVersionID;
    }    

    public String
    toString()
    {
      return pLabel;
    }

    private String     pLabel;
    private String     pName;
    private VersionID  pVersionID; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  //private static final long serialVersionUID = 
  
  protected static final int  sLWidth  = 240;
  protected static final int  sLHeight = 400;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Does the current user have privileged status?
   */ 
  private boolean  pIsPrivileged;

  /**
   * The plugin menu layout.
   */ 
  private PluginMenuLayout  pMenuLayout; 
  
  /**
   * The plugin versions. 
   */ 
  private TreeMap<String,TreeSet<VersionID>>  pPluginVersions;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The layout menu.
   */ 
  private JPopupMenu  pPopup; 

  /**
   * Layout menu items.
   */ 
  private JMenuItem  pNewSubmenu; 
  private JMenuItem  pNewMenuItem; 
  private JMenuItem  pRenameMenu;
  private JMenuItem  pDeleteMenu;
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * The plugin menu layout tree.
   */ 
  private JTree  pMenuLayoutTree;
  
  /**
   * Plugin buttons.
   */ 
  private JButton  pSetPluginButton; 
  private JButton  pRemovePluginButton; 
     
  /**
   * The plugin versions tree.
   */ 
  private JTree  pPluginVersionTree;

}
