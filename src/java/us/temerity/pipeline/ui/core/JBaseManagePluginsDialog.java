// $Id: JBaseManagePluginsDialog.java,v 1.3 2005/01/07 07:09:49 jim Exp $

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
      pNewMenu = item;
      item.setActionCommand("new-menu");
      item.addActionListener(this);
      pPopup.add(item);  
   
      pPopup.addSeparator();

      item = new JMenuItem("Rename...");
      pRenameMenu = item;
      item.setActionCommand("rename-menu");
      item.addActionListener(this);
      pPopup.add(item);  

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
	    new DefaultMutableTreeNode(new PluginMenuLayout(), true);
	  DefaultTreeModel model = new DefaultTreeModel(root, true);
	  
	  JTree tree = new JFancyTree(model); 
	  pMenuLayoutTree = tree;
	  tree.setName("DarkTree");
	  
	  tree.setRootVisible(true);
	  tree.setCellRenderer(new JPluginMenuLayoutTreeCellRenderer());
	  tree.getSelectionModel().setSelectionMode
	    (TreeSelectionModel.SINGLE_TREE_SELECTION);
	  tree.setExpandsSelectedPaths(true);
	  
	  tree.addMouseListener(this);
	  tree.addTreeSelectionListener(this);
	  
	  {
	    JScrollPane scroll = new JScrollPane(tree);
	    
	    scroll.setMinimumSize(new Dimension(400, 100));
	    scroll.setPreferredSize(new Dimension(400, 500));
	    
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
	  pClearPluginButton = btn;
	  btn.setName("RightArrowButton");
	  
	  Dimension size = new Dimension(16, 16);
	  btn.setMinimumSize(size);
	  btn.setMaximumSize(size);
	  btn.setPreferredSize(size);
	  
	  btn.setActionCommand("clear-plugin");
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
	    
	    scroll.setMinimumSize(new Dimension(150, 100));
	    scroll.setPreferredSize(new Dimension(150, 500));
	    
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
    pClearPluginButton.setEnabled(false);

    pConfirmButton.setEnabled(pIsPrivileged);
    pApplyButton.setEnabled(pIsPrivileged);
  }
  
  /**
   * Get the plugin menu layout specified by the UI components.
   */
  protected PluginMenuLayout
  getMenuLayout() 
  {
    return pMenuLayout;
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
      pMenuLayoutTree.clearSelection();
      pMenuLayoutTree.addSelectionPath(tpath);
    }

    PluginMenuLayout pml = getSelectedPluginMenuLayout();
    if(pml != null) {
      pNewMenu.setEnabled(pml.isSubmenu());
      pRenameMenu.setEnabled(!pml.getTitle().equals("Plugin Menu"));
      pDeleteMenu.setEnabled(true);
    }
    else {
      pNewMenu.setEnabled(true);
      pRenameMenu.setEnabled(false);
      pDeleteMenu.setEnabled(false);
    }
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
    PluginMenuLayout pml = getSelectedPluginMenuLayout(); 
    PluginVersionData vdata = getSelectedPluginVersionData();

    if(e.getSource() == pMenuLayoutTree) {
      if((pml != null) && pml.isMenuItem()) {
	selectPluginVersion(pml.getName(), pml.getVersionID());
	vdata = getSelectedPluginVersionData();

	pSetPluginButton.setEnabled(true);
	pClearPluginButton.setEnabled(true);
      }
      else {
	pSetPluginButton.setEnabled(false);
	pClearPluginButton.setEnabled(false);
      }
    }

    if((pml != null) && (pml.isMenuItem() || (pml.size() == 0)) &&
       (vdata != null) && (vdata.getName() != null) && (vdata.getVersionID() != null)) {
      pSetPluginButton.setEnabled(true); 
    }
    else {
      pSetPluginButton.setEnabled(false);
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
    if(cmd.equals("new-menu")) 
      doNewMenu();
    if(cmd.equals("rename-menu")) 
      doRenameMenu();
    if(cmd.equals("delete-menu")) 
      doDeleteMenu();
    if(cmd.equals("set-plugin"))
      doSetPlugin();
    if(cmd.equals("clear-plugin"))
      doClearPlugin();
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
   * Add a new plugin layout menu.
   */ 
  public void 
  doNewMenu()
  {
    JNewNameDialog diag = 
      new JNewNameDialog(this, "New Menu", "New Menu Name:", null, "Add");

    diag.setVisible(true);
    if(diag.wasConfirmed()) {
      String name = diag.getName();
      if((name != null) && (name.length() > 0)) {
	DefaultMutableTreeNode mnode = getSelectedMenuLayoutNode();
	if(mnode == null) {
	  DefaultTreeModel model = (DefaultTreeModel) pMenuLayoutTree.getModel();
	  mnode = (DefaultMutableTreeNode) model.getRoot();
	}
	
	if(mnode != null) {
	  PluginMenuLayout pml = (PluginMenuLayout) mnode.getUserObject();
	  if(pml.isSubmenu()) {
	    pMenuLayoutTree.expandPath(new TreePath(mnode.getPath()));
	    pml.add(new PluginMenuLayout(name));
	    rebuildMenuLayout();
	  }
	}
      }
    }
  }

  /**
   * Rename the currently selected plugin layout menu.
   */ 
  public void 
  doRenameMenu()
  {
    PluginMenuLayout pml = getSelectedPluginMenuLayout();
    if(pml != null) {
      JNewNameDialog diag = 
	new JNewNameDialog(this, "Rename Menu", "Menu Name:", pml.getTitle(), "Add");

      diag.setVisible(true);
      if(diag.wasConfirmed()) {
	String name = diag.getName();
	if((name != null) && (name.length() > 0)) 
	  pml.setTitle(name);
      }
    }    
  }

  /**
   * Delete the currently selected plugin layout menu (and all submenus).
   */ 
  public void 
  doDeleteMenu()
  {
    DefaultMutableTreeNode mnode = getSelectedMenuLayoutNode();
    if(mnode != null) {
      DefaultMutableTreeNode pnode = (DefaultMutableTreeNode) mnode.getParent(); 
      if(pnode != null) {
	PluginMenuLayout parent = (PluginMenuLayout) pnode.getUserObject();
	PluginMenuLayout child  = (PluginMenuLayout) mnode.getUserObject();
	parent.remove(child);
      }
      else {
	pMenuLayout = new PluginMenuLayout();
      }

      rebuildMenuLayout();
    }
  }
 

  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the plugin version associated with the currently selected menu. 
   */ 
  public void 
  doSetPlugin()
  {
    PluginMenuLayout pml = getSelectedPluginMenuLayout(); 
    PluginVersionData vdata = getSelectedPluginVersionData();
    if((pml != null) && (pml.size() == 0) && 
       (vdata != null) && (vdata.getName() != null) && (vdata.getVersionID() != null)) {
      pml.setPlugin(vdata.getName(), vdata.getVersionID());
      rebuildMenuLayout();
    }
  }

  /**
   * Clear the plugin version associated with the currently selected menu. 
   */ 
  public void 
  doClearPlugin()
  {
    PluginMenuLayout pml = getSelectedPluginMenuLayout(); 
    if((pml != null) && pml.isMenuItem()) {
      pml.clearPlugin();
      rebuildMenuLayout();
    }
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the paths of the expanded tree nodes. 
   */ 
  private TreeSet<String> 
  getExpandedTreeNodes
  (
   JTree tree
  )
  {
    TreeSet<String> expanded = new TreeSet<String>();

    DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();   
    Enumeration e = tree.getExpandedDescendants(new TreePath(root.getPath()));
    if(e != null) {
      while(e.hasMoreElements()) {
	TreePath tpath = (TreePath) e.nextElement(); 
	expanded.add(treePathToString(tpath));
      }
    }

    return expanded;
  }

  /**
   * Expand the tree nodes with the given paths.
   */ 
  private void 
  rexpandTreeNodes
  (
   JTree tree, 
   TreeSet<String> expanded 
  )
  { 
    DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();   
    Enumeration e = root.depthFirstEnumeration();
    if(e != null) {
      while(e.hasMoreElements()) {
	DefaultMutableTreeNode mnode = (DefaultMutableTreeNode) e.nextElement(); 
	TreePath tpath = new TreePath(mnode.getPath());
	if(expanded.contains(treePathToString(tpath))) 
	  tree.expandPath(tpath);
      }
    }    
  }

  /**
   * Convert a tree path into a string.
   */ 
  private String
  treePathToString
  (
   TreePath tpath
  ) 
  {
    StringBuffer buf = new StringBuffer();

    Object[] path = tpath.getPath(); 
    int wk;
    for(wk=0; wk<path.length; wk++) 
      buf.append("/" + path[wk]);

    return buf.toString();
  }

  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Rebuild the menu layout tree nodes.
   */ 
  private void 
  rebuildMenuLayout() 
  {
    TreeSet<String> expanded = getExpandedTreeNodes(pMenuLayoutTree);

    DefaultMutableTreeNode root = rebuildMenuLayout(pMenuLayout, null);
    
    DefaultTreeModel model = (DefaultTreeModel) pMenuLayoutTree.getModel();
    model.setRoot(root);      

    rexpandTreeNodes(pMenuLayoutTree, expanded);
  }

  /**
   * Recursively rebuild the menu layout tree nodes.
   */ 
  private DefaultMutableTreeNode
  rebuildMenuLayout
  (
   PluginMenuLayout layout, 
   DefaultMutableTreeNode root
  ) 
  { 
    DefaultMutableTreeNode vnode = null;
    if(layout.isMenuItem()) {
      String name = layout.getName();
      VersionID vid = layout.getVersionID();

      TreeSet<VersionID> vids = pPluginVersions.get(name);
      if((vids == null) || !vids.contains(vid)) {
	name = null;
	vid = null;
      }

      vnode = new DefaultMutableTreeNode(layout, false);
      if(root != null) 
	root.add(vnode);
    }
    else {
      vnode = new DefaultMutableTreeNode(layout, true);
      if(root != null)
	root.add(vnode);

      for(PluginMenuLayout pml : layout) 
	rebuildMenuLayout(pml, vnode);
    }

    return vnode;
  }

  /**
   * Get the selected menu layout tree node.
   * 
   * @return 
   *   The node or <CODE>null</CODE> if none is selected.
   */ 
  private DefaultMutableTreeNode
  getSelectedMenuLayoutNode() 
  {
    TreePath mpath = pMenuLayoutTree.getSelectionPath();
    if(mpath != null) 
      return ((DefaultMutableTreeNode) mpath.getLastPathComponent());
    return null;
  }

  /**
   * Get the selected menu layout tree node user data.
   * 
   * @return 
   *   The data or <CODE>null</CODE> if none is selected.
   */ 
  private PluginMenuLayout
  getSelectedPluginMenuLayout() 
  {
    DefaultMutableTreeNode mnode = getSelectedMenuLayoutNode();
    if(mnode != null) 
      return ((PluginMenuLayout) mnode.getUserObject());
    return null;
  }




  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Rebuild the plugin version tree nodes 
   */ 
  private void 
  rebuildPluginVersions()
  {
    TreeSet<String> expanded = getExpandedTreeNodes(pMenuLayoutTree);

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

    rexpandTreeNodes(pMenuLayoutTree, expanded);
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

  /**
   * Get the selected plugin version tree node.
   * 
   * @return 
   *   The node or <CODE>null</CODE> if none is selected.
   */ 
  private DefaultMutableTreeNode
  getSelectedPluginVersionNode() 
  {
    TreePath mpath = pPluginVersionTree.getSelectionPath();
    if(mpath != null) 
      return ((DefaultMutableTreeNode) mpath.getLastPathComponent());
    return null;
  }

  /**
   * Get the selected plugin version tree node user data.
   * 
   * @return 
   *   The data or <CODE>null</CODE> if none is selected.
   */ 
  private PluginVersionData
  getSelectedPluginVersionData() 
  {
    DefaultMutableTreeNode mnode = getSelectedPluginVersionNode();
    if(mnode != null) 
      return ((PluginVersionData) mnode.getUserObject());
    return null;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P U B L I C   C L A S S E S                                                          */
  /*----------------------------------------------------------------------------------------*/

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
  private JMenuItem  pNewMenu; 
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
  private JButton  pClearPluginButton; 
     
  /**
   * The plugin versions tree.
   */ 
  private JTree  pPluginVersionTree;

}
