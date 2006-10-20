// $Id: JBaseToolsetPluginsPanel.java,v 1.7 2006/10/20 21:01:35 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*; 
import us.temerity.pipeline.toolset.*; 
import us.temerity.pipeline.math.*; 

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   T O O L S E T   P L U G I N S   P A N E L                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The base clase of all panels which edit the plugin menu layuouts associated with a toolset.
 */ 
public abstract
class JBaseToolsetPluginsPanel
  extends JPanel
  implements MouseListener, MouseMotionListener, ActionListener, TreeSelectionListener
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
  JBaseToolsetPluginsPanel
  (
   String title, 
   JManageToolsetsDialog dialog,
   JManageToolsetPluginsDialog parent
  ) 
  {
    /* initialize fields */ 
    {
      pMenuLayout     = new PluginMenuLayout();
      pPluginVersions = new TripleMap<String,String,VersionID,TreeSet<OsType>>();

      pPrivilegeDetails = new PrivilegeDetails(); 

      pDialog = dialog; 
      pParent = parent;
    }

    /* layout popup menu */ 
    {
      JMenuItem item;
      
      pPopup = new JPopupMenu();  
 
      item = new JMenuItem("New Menu/Item...");
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

      pPopup.addSeparator();

      item = new JMenuItem("Move Up");
      pMoveUpMenu = item;
      item.setActionCommand("move-up-menu");
      item.addActionListener(this);
      pPopup.add(item);  

      item = new JMenuItem("Move Down");
      pMoveDownMenu = item;
      item.setActionCommand("move-down-menu");
      item.addActionListener(this);
      pPopup.add(item);  
    }

    /* create dialog body components */ 
    {
      setLayout(new BorderLayout());
      
      JPanel panel = new JPanel();
      panel.setName("MainPanel");
      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));  

      panel.add(Box.createRigidArea(new Dimension(20, 0)));

      {
	Box vbox = new Box(BoxLayout.Y_AXIS);
	
	vbox.add(Box.createRigidArea(new Dimension(0, 20)));
	
	vbox.add(UIFactory.createPanelLabel(title + " Menu Layout:"));
	
	vbox.add(Box.createRigidArea(new Dimension(0, 4)));
	
	{
	  DefaultMutableTreeNode root = 
	    new DefaultMutableTreeNode(new PluginMenuLayout(), true);
	  DefaultTreeModel model = new DefaultTreeModel(root, true);
	  
	  JTree tree = new JFancyTree(model); 
	  pMenuLayoutTree = tree;
	  tree.setName("DarkTree");
	  
	  tree.setRootVisible(true);
	  tree.setCellRenderer(new JPluginMenuLayoutTreeCellRenderer(this));
	  tree.getSelectionModel().setSelectionMode
	    (TreeSelectionModel.SINGLE_TREE_SELECTION);
	  tree.setExpandsSelectedPaths(true);
	  
	  tree.addMouseListener(this);
	  tree.addMouseMotionListener(this);
	  tree.addTreeSelectionListener(this);
	  
	  {
	    JScrollPane scroll = new JScrollPane(tree);
	    
	    scroll.setMinimumSize(new Dimension(500, 100));
	    scroll.setPreferredSize(new Dimension(500, 500));
	    
	    scroll.setHorizontalScrollBarPolicy
	      (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	    scroll.setVerticalScrollBarPolicy
	      (ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
	    
	    vbox.add(scroll);
	  }
	  
	  vbox.add(Box.createRigidArea(new Dimension(0, 20)));
	  
	  panel.add(vbox);
	}
      }
      
      panel.add(Box.createRigidArea(new Dimension(4, 0)));
      
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
	
	panel.add(vbox);
      }
      
      panel.add(Box.createRigidArea(new Dimension(4, 0)));
	
      {
	Box vbox = new Box(BoxLayout.Y_AXIS);
	
	vbox.add(Box.createRigidArea(new Dimension(0, 20)));
	
	vbox.add(UIFactory.createPanelLabel(title + " Plugins:"));
	
	vbox.add(Box.createRigidArea(new Dimension(0, 4)));
	
	{
	  DefaultMutableTreeNode root = 
	    new DefaultMutableTreeNode(new PluginTreeData(), true);
	  DefaultTreeModel model = new DefaultTreeModel(root, true);
	  
	  JTree tree = new JFancyTree(model); 
	  pPluginTree = tree;
	  tree.setName("DarkTree");
	  
 	  tree.setCellRenderer(new JPluginTreeCellRenderer());
	  tree.getSelectionModel().setSelectionMode
	    (TreeSelectionModel.SINGLE_TREE_SELECTION);
	  tree.setExpandsSelectedPaths(true);
	  
	  tree.addTreeSelectionListener(this);
	  
	  {
	    JScrollPane scroll = new JScrollPane(tree);
	    pPluginScroll = scroll;

	    scroll.setMinimumSize(new Dimension(150, 100));
	    scroll.setPreferredSize(new Dimension(250, 500));
	    
	    scroll.setHorizontalScrollBarPolicy
	      (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	    scroll.setVerticalScrollBarPolicy
	      (ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
	    
	    vbox.add(scroll);
	  }
	  
	  vbox.add(Box.createRigidArea(new Dimension(0, 20)));
	  
	  panel.add(vbox);
	}
      }

      panel.add(Box.createRigidArea(new Dimension(20, 0)));

      add(panel);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the menu layout tree node under the given mouse position.
   */ 
  public DefaultMutableTreeNode
  getDragMenuLayoutNode() 
  {
    return pDragMenuLayoutNode;
  }

  /**
   * Get the plugin menu layout specified by the UI components.
   */
  public PluginMenuLayout
  getMenuLayout() 
  {
    return pMenuLayout;
  }

  /**
   * Is the given plugin version supported by the toolset?
   */ 
  public boolean
  isPluginSupported
  (
   String name,
   VersionID vid, 
   String vendor
  ) 
  {
    Set<VersionID> vids = pPluginVersions.keySet(vendor, name);
    return ((vids != null) && vids.contains(vid));
  }


  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the UI components to display the plugin menu associated with a toolset.
   * 
   * @param toolset
   *   The toolset. 
   * 
   * @param privileges
   *   The details of the administrative privileges granted to the current user. 
   */ 
  public void 
  update
  (
   Toolset toolset, 
   PrivilegeDetails privileges
  ) 
    throws PipelineException
  {
    TripleMap<String,String,VersionID,TreeSet<OsType>> all = getAllPlugins(); 

    pPluginVersions = new TripleMap<String,String,VersionID,TreeSet<OsType>>();
    {
      int wk;
      for(wk=0; wk<toolset.getNumPackages(); wk++) {
	String pname = toolset.getPackageName(wk);
	VersionID pvid = toolset.getPackageVersionID(wk);
	
	DoubleMap<String,String,TreeSet<VersionID>> table = getPackagePlugins(pname, pvid);

	for(String vendor : table.keySet()) {
	  for(String name : table.keySet(vendor)) {
	    for(VersionID vid : table.get(vendor, name)) {
	      pPluginVersions.put(vendor, name, vid, all.get(vendor, name, vid));
	    }
	  }
	}
      }
    }

    pToolsetName = toolset.getName();      
    pMenuLayout = getLayout(pToolsetName); 
    pPrivilegeDetails = privileges; 

    rebuildMenuLayout();
    expandAllTreeNodes(pMenuLayoutTree);
    
    rebuildPluginVersions();
    expandAllTreeNodes(pPluginTree);
    
    pSetPluginButton.setEnabled(false);
    pClearPluginButton.setEnabled(false);
  }

  /**
   * Get the all of the plugins. 
   * 
   * @param pname
   *   The name of the toolset package.
   * 
   * @param pvid
   *   The version number of the package.
   */ 
  protected abstract TripleMap<String,String,VersionID,TreeSet<OsType>>
  getAllPlugins() 
    throws PipelineException;

  /**
   * Get the plugins associated with the given toolset package.
   * 
   * @param pname
   *   The name of the toolset package.
   * 
   * @param pvid
   *   The version number of the package.
   */ 
  protected abstract DoubleMap<String,String,TreeSet<VersionID>> 
  getPackagePlugins
  (
   String pname, 
   VersionID pvid
  )
    throws PipelineException;
    
  /**
   * Update the UI components with the default menu layout. 
   * 
   * @param layout
   *   The default plugin menu layout.
   */ 
  protected void 
  updateDefault
  (
   PluginMenuLayout layout
  ) 
  {
    pMenuLayout = layout;
    
    rebuildMenuLayout();
    expandAllTreeNodes(pMenuLayoutTree);

    rebuildPluginVersions();
    expandAllTreeNodes(pPluginTree);

    pSetPluginButton.setEnabled(false);
    pClearPluginButton.setEnabled(false);
  }

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
    PluginMenuLayout pml = getSelectedPluginMenuLayout();
    if(pml != null) {
      boolean isRoot = pml.getTitle().equals("Plugin Menu");
      pNewMenu.setEnabled(pml.isSubmenu());
      pRenameMenu.setEnabled(!isRoot);
      pDeleteMenu.setEnabled(true);
      pMoveUpMenu.setEnabled(!isRoot);
      pMoveDownMenu.setEnabled(!isRoot);
    }
    else {
      pNewMenu.setEnabled(true);
      pRenameMenu.setEnabled(false);
      pDeleteMenu.setEnabled(false);
      pMoveUpMenu.setEnabled(false);
      pMoveDownMenu.setEnabled(false);
    }
  }

  /**
   * Copy the plugin menus associated with one toolset to initialize the plugin menus 
   * associated with another toolset.
   * 
   * @param source
   *   The name of the source toolset package.
   * 
   * @param target
   *   The name of the target toolset package.
   */ 
  public void 
  clone
  (
   String source, 
   String target
  )
    throws PipelineException
  {
    setLayout(target, getLayout(source));
  }

  /**
   * Remove the plugins associated with the given working toolset.
   * 
   * @param tname
   *   The name of the toolset toolset.
   */ 
  public void 
  remove
  (
   String tname
  )
    throws PipelineException
  {
    setLayout(tname, null);
  }
   
  /**
   * Reset the layout to the default menu lauyout.
   */ 
  public abstract void 
  defaultLayout() 
    throws PipelineException;

  /**
   * Save the current menu layout as the default layout.
   */ 
  public abstract void 
  saveDefaultLayout() 
    throws PipelineException;

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the plugin menu layout associated with a toolset.
   * 
   * @param tname
   *   The name of the toolset.
   */ 
  protected abstract PluginMenuLayout
  getLayout
  (
   String tname
  )
    throws PipelineException; 

  /**
   * Set the plugin menu layout associated with a toolset.
   * 
   * @param tname
   *   The name of the toolset.
   * 
   * @param layout
   *   The plugin menu layout.
   */ 
  protected abstract void
  setLayout
  (
   String tname, 
   PluginMenuLayout layout
  )
    throws PipelineException;


  /*----------------------------------------------------------------------------------------*/

  /**
   * Save any changes. 
   */ 
  public void 
  saveChanges()
    throws PipelineException
  {
    setLayout(pToolsetName, pMenuLayout);
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
    PluginTreeData vdata = getSelectedPluginData();

    if(e.getSource() == pMenuLayoutTree) {
      if((pml != null) && pml.isMenuItem()) {
	selectPluginVersion(pml.getName(), pml.getVersionID(), pml.getVendor());
	vdata = getSelectedPluginData();

	pSetPluginButton.setEnabled(pPrivilegeDetails.isDeveloper());
	pClearPluginButton.setEnabled(pPrivilegeDetails.isDeveloper());
      }
      else {
	pSetPluginButton.setEnabled(false);
	pClearPluginButton.setEnabled(false);
      }
    }

    if((pml != null) && (pml.isMenuItem() || (pml.size() == 0)) &&
       (vdata != null) && (vdata.getName() != null) && (vdata.getVersionID() != null)) {
      pSetPluginButton.setEnabled(pPrivilegeDetails.isDeveloper());
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
    if(!pPrivilegeDetails.isDeveloper()) 
      return;

    int mods = e.getModifiersEx();
    switch(e.getButton()) {
    case MouseEvent.BUTTON2:
    case MouseEvent.BUTTON3:
      {
	int on1  = (MouseEvent.BUTTON2_DOWN_MASK);
	
	int off1 = (MouseEvent.BUTTON1_DOWN_MASK | 
		    MouseEvent.BUTTON3_DOWN_MASK);
	
	int on2  = (MouseEvent.BUTTON3_DOWN_MASK);
	
	int off2 = (MouseEvent.BUTTON1_DOWN_MASK | 
		    MouseEvent.BUTTON2_DOWN_MASK | 
		    MouseEvent.SHIFT_DOWN_MASK |
		    MouseEvent.ALT_DOWN_MASK |
		    MouseEvent.CTRL_DOWN_MASK);

	Point2i pos = new Point2i(e.getX(), e.getY());

	/* BUTTON2: menu drag */ 
	if((mods & (on1 | off1)) == on1) {
	  selectMenuLayoutNode(pos);
	}

	/* BUTTON3: popup menu */ 
	else if((mods & (on2 | off2)) == on2) {
	  updatePopupMenu(selectMenuLayoutNode(pos));
	  pPopup.show(e.getComponent(), e.getX(), e.getY());
	}
      }
    }
  }

  /**
   * Invoked when a mouse button has been released on a component. 
   */ 
  public void 
  mouseReleased
  (
   MouseEvent e
  )
  {
    if(!pPrivilegeDetails.isDeveloper()) 
      return;

    /* BUTTON2: menu drop */ 
    switch(e.getButton()) {
    case MouseEvent.BUTTON2:
      {
	PluginMenuLayout pslayout = null;
	PluginMenuLayout slayout = null;
	{
	  TreePath path = pMenuLayoutTree.getSelectionPath();
	  if(path != null) {
	    DefaultMutableTreeNode node = 
	      (DefaultMutableTreeNode) path.getLastPathComponent();
	    if(node != null) {
	      slayout = (PluginMenuLayout) node.getUserObject();
	      
	      DefaultMutableTreeNode pnode = (DefaultMutableTreeNode) node.getParent();
	      if(pnode != null) 
		pslayout = (PluginMenuLayout) pnode.getUserObject();
	    }
	  }
	}
	
	PluginMenuLayout ptlayout = null;
	PluginMenuLayout tlayout = null;
	{
	  TreePath path = pickMenuLayoutNode(new Point2i(e.getX(), e.getY()));
	  if(path != null) {
	    DefaultMutableTreeNode node = 
	      (DefaultMutableTreeNode) path.getLastPathComponent();
	    if(node != null)  {
	      tlayout = (PluginMenuLayout) node.getUserObject();
	      
	      DefaultMutableTreeNode pnode = (DefaultMutableTreeNode) node.getParent();
	      if(pnode != null) 
		ptlayout = (PluginMenuLayout) pnode.getUserObject();
	    }
	  }
	}
	
	if((pslayout != null) && 
	   (slayout != null) && (tlayout != null) && (slayout != tlayout)) {
	  if(tlayout.isMenuItem()) {
	    if(ptlayout != null) {
	      int idx = ptlayout.indexOf(tlayout);
	      if(idx > -1) {
		pslayout.remove(slayout);
		ptlayout.add(idx, slayout); 
		rebuildMenuLayout();
	      }
	    }
	  }
	  else {
	    pslayout.remove(slayout);
	    tlayout.add(0, slayout);  
	    rebuildMenuLayout();
	  }
	}
      }
    }

    pDragMenuLayoutNode = null;    
  }

  /*-- MOUSE MOTION LISTNER METHODS --------------------------------------------------------*/
  
  /**
   * Invoked when a mouse button is pressed on a component and then dragged. 
   */ 
  public void 	
  mouseDragged
  (
   MouseEvent e
  )
  {
    if(!pPrivilegeDetails.isDeveloper()) 
      return;

    int mods = e.getModifiersEx();

    int on1  = (MouseEvent.BUTTON2_DOWN_MASK);
	
    int off1 = (MouseEvent.BUTTON1_DOWN_MASK | 
		MouseEvent.BUTTON3_DOWN_MASK | 
		MouseEvent.SHIFT_DOWN_MASK |
		MouseEvent.ALT_DOWN_MASK |
		MouseEvent.CTRL_DOWN_MASK);

    pDragMenuLayoutNode = null;
    if((mods & (on1 | off1)) == on1) {
      TreePath spath = pMenuLayoutTree.getSelectionPath();
      TreePath tpath = pickMenuLayoutNode(new Point2i(e.getX(), e.getY()));
      if((tpath != null) && (spath != tpath)) 
	pDragMenuLayoutNode = (DefaultMutableTreeNode) tpath.getLastPathComponent();
    }

    pMenuLayoutTree.repaint();
  }

  /**
   * Invoked when the mouse cursor has been moved onto a component but no buttons have 
   * been pushed. 
   */ 
  public void 	
  mouseMoved(MouseEvent e) {} 


  

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
    if(cmd.equals("new-menu")) 
      doNewMenu();
    else if(cmd.equals("rename-menu")) 
      doRenameMenu();
    else if(cmd.equals("delete-menu")) 
      doDeleteMenu();
    else if(cmd.equals("move-up-menu")) 
      doMoveUpMenu();
    else if(cmd.equals("move-down-menu")) 
      doMoveDownMenu();
    else if(cmd.equals("set-plugin"))
      doSetPlugin();
    else if(cmd.equals("clear-plugin"))
      doClearPlugin();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Add a new plugin layout menu.
   */ 
  public void 
  doNewMenu()
  {
    JNewNameDialog diag = 
      new JNewNameDialog(pParent, "New Menu", "New Menu Name:", null, "Add");

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
	new JNewNameDialog(pParent, "Rename Menu", "Menu Name:", pml.getTitle(), "Add");

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
 
  /**
   * Move the selected menu one place earlier in its parent's list. 
   */ 
  public void 
  doMoveUpMenu()
  {
    DefaultMutableTreeNode mnode = getSelectedMenuLayoutNode();
    if(mnode != null) {
      PluginMenuLayout pml = (PluginMenuLayout) mnode.getUserObject();
      
      DefaultMutableTreeNode pnode = (DefaultMutableTreeNode) mnode.getParent();
      if(pnode != null) {
	PluginMenuLayout ppml = (PluginMenuLayout) pnode.getUserObject();

	int idx = ppml.indexOf(pml);
	if(idx > 0) {
	  Collections.swap(ppml, idx-1, idx);
	  rebuildMenuLayout();
	}
      }
    }
  }

  /**
   * Move the selected menu one place later in its parent's list. 
   */ 
  public void 
  doMoveDownMenu()
  {
    DefaultMutableTreeNode mnode = getSelectedMenuLayoutNode();
    if(mnode != null) {
      PluginMenuLayout pml = (PluginMenuLayout) mnode.getUserObject();
      
      DefaultMutableTreeNode pnode = (DefaultMutableTreeNode) mnode.getParent();
      if(pnode != null) {
	PluginMenuLayout ppml = (PluginMenuLayout) pnode.getUserObject();

	int idx = ppml.indexOf(pml);
	if((idx != -1) && (idx < (ppml.size()-1))) {
	  Collections.swap(ppml, idx, idx+1);
	  rebuildMenuLayout();
	}
      }
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
    PluginTreeData vdata = getSelectedPluginData();
    if((pml != null) && (pml.size() == 0) && 
       (vdata != null) && (vdata.getName() != null) && (vdata.getVersionID() != null)) {
      pml.setPlugin(vdata.getName(), vdata.getVersionID(), vdata.getVendor());
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
   * Expand all tree nodes.
   */ 
  private void 
  expandAllTreeNodes
  (
   JTree tree
  )
  {   
    DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();   
    Enumeration e = root.depthFirstEnumeration();
    if(e != null) {
      while(e.hasMoreElements()) {
	DefaultMutableTreeNode mnode = (DefaultMutableTreeNode) e.nextElement(); 
	TreePath tpath = new TreePath(mnode.getPath());
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
      String vendor = layout.getVendor();

      Set<VersionID> vids = pPluginVersions.keySet(vendor, name);
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
   * Get path to the menu layout tree node under the given mouse position.
   * 
   * @return 
   *   The path to the tree node.
   */ 
  private TreePath
  pickMenuLayoutNode
  (
   Point2i pos
  ) 
  {
    TreePath tpath = pMenuLayoutTree.getClosestPathForLocation(pos.x(), pos.y());
    if(tpath != null) {
      Rectangle bounds = pMenuLayoutTree.getPathBounds(tpath);
      if(!bounds.contains(pos.x(), pos.y()))
	tpath = null;
    }

    return tpath;
  }

  /**
   * Select the menu layout tree node under the given mouse position.
   * 
   * @return 
   *   The path to the selected tree node.
   */ 
  private TreePath
  selectMenuLayoutNode
  (
   Point2i pos
  ) 
  {
    TreePath tpath = pickMenuLayoutNode(pos);    
    if(tpath != null) {
      pMenuLayoutTree.clearSelection();
      pMenuLayoutTree.addSelectionPath(tpath);
    }
    
    return tpath;
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

    DefaultMutableTreeNode root = new DefaultMutableTreeNode(new PluginTreeData());
    
    for(String vendor : pPluginVersions.keySet()) {
      DefaultMutableTreeNode rnode = 
	new DefaultMutableTreeNode(new PluginTreeData(vendor), true);
      root.add(rnode);

      for(String name : pPluginVersions.keySet(vendor)) {
	DefaultMutableTreeNode pnode = 
	  new DefaultMutableTreeNode(new PluginTreeData(name), true);
	rnode.add(pnode);
	
	for(VersionID vid : pPluginVersions.keySet(vendor, name)) {	  
	  PluginTreeData pdata = 
	    new PluginTreeData(name, vid, vendor, pPluginVersions.get(vendor, name, vid));
	  DefaultMutableTreeNode vnode = new DefaultMutableTreeNode(pdata, false);
	  pnode.add(vnode);
	}
      }
    }
    
    DefaultTreeModel model = (DefaultTreeModel) pPluginTree.getModel();
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
   VersionID vid, 
   String vendor
  ) 
  {
    pPluginTree.clearSelection();

    DefaultTreeModel model = (DefaultTreeModel) pPluginTree.getModel();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();

    int rk;
    for(rk=0; rk<root.getChildCount(); rk++) {
      DefaultMutableTreeNode rnode = (DefaultMutableTreeNode) root.getChildAt(rk);  
	  
      int wk;
      for(wk=0; wk<rnode.getChildCount(); wk++) {
	DefaultMutableTreeNode pnode = (DefaultMutableTreeNode) rnode.getChildAt(wk);  
	
	int vk;
	for(vk=0; vk<pnode.getChildCount(); vk++) {
	  DefaultMutableTreeNode vnode = (DefaultMutableTreeNode) pnode.getChildAt(vk); 
	  PluginTreeData vdata = (PluginTreeData) vnode.getUserObject();
	  TreePath vpath = new TreePath(vnode.getPath());
	  
	  if(name.equals(vdata.getName()) && 
	     vid.equals(vdata.getVersionID()) && 
	     vendor.equals(vdata.getVendor())) {
	    pPluginTree.addSelectionPath(vpath);

// 	    Rectangle bounds = pPluginTree.getPathBounds(vpath);
// 	    if(bounds != null) 
// 	      pPluginScroll.getViewport().scrollRectToVisible(bounds);
	    return;
	  }
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
  getSelectedPluginNode() 
  {
    TreePath mpath = pPluginTree.getSelectionPath();
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
  private PluginTreeData
  getSelectedPluginData() 
  {
    DefaultMutableTreeNode mnode = getSelectedPluginNode();
    if(mnode != null) 
      return ((PluginTreeData) mnode.getUserObject());
    return null;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the current toolset.
   */ 
  protected String  pToolsetName; 
  
  /**
   * The plugin menu layout.
   */ 
  private PluginMenuLayout  pMenuLayout; 
  
  /**
   * The plugin versions. 
   */ 
  private TripleMap<String,String,VersionID,TreeSet<OsType>>  pPluginVersions;


  /**
   * The details of the administrative privileges granted to the current user. 
   */ 
  private PrivilegeDetails  pPrivilegeDetails; 


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
  private JMenuItem  pMoveUpMenu; 
  private JMenuItem  pMoveDownMenu; 
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * The master toolsets dialog.
   */ 
  protected JManageToolsetsDialog  pDialog;

  /**
   * The parent dialog.
   */ 
  private JManageToolsetPluginsDialog  pParent; 

  /**
   * The plugin menu layout tree.
   */ 
  private JTree  pMenuLayoutTree;
  
  /**
   * The menu layout tree node under the given mouse position.
   */ 
  private DefaultMutableTreeNode  pDragMenuLayoutNode;

  /**
   * Plugin buttons.
   */ 
  private JButton  pSetPluginButton; 
  private JButton  pClearPluginButton; 
     
  /**
   * The plugin versions tree.
   */ 
  private JTree  pPluginTree;

  /**
   * The scroll pane containing the plugin versions tree.
   */ 
  private JScrollPane  pPluginScroll; 

}
