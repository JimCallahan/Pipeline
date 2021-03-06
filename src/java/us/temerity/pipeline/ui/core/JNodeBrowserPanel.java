// $Id: JNodeBrowserPanel.java,v 1.31 2009/11/02 03:27:40 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.ui.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   B R O W S E R   P A N E L                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The directory-like browser of the node name hierarchy used to select nodes for display
 * in the {@link JNodeViewerPanel JNodeViewerPanel}.
 */ 
public  
class JNodeBrowserPanel
  extends JTopLevelPanel
  implements TreeExpansionListener, MouseListener, KeyListener, ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new panel with the default working area view.
   */
  public 
  JNodeBrowserPanel()
  {
    super();
    initUI();
  }

  /**
   * Construct a new panel with a working area view identical to the given panel.
   */
  public 
  JNodeBrowserPanel
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
      pToolsetCache = new TreeMap<String,String>();

      pSelected = new TreeSet<String>();

      pFilter = new TreeMap<NodeTreeComp.State,Boolean>();
      pFilter.put(NodeTreeComp.State.WorkingCurrentCheckedInSome, true);
      pFilter.put(NodeTreeComp.State.WorkingOtherCheckedInSome,   true);
      pFilter.put(NodeTreeComp.State.WorkingNoneCheckedInSome,    true);
      pFilter.put(NodeTreeComp.State.WorkingCurrentCheckedInNone, true);
      pFilter.put(NodeTreeComp.State.WorkingOtherCheckedInNone,   true);

      pDisplayPrimarySuffix = UserPrefs.getInstance().getDisplayPrimarySuffix();
    }

    /* panel popup menu */ 
    {
      JMenuItem item;
      JRadioButtonMenuItem ritem;
      JCheckBoxMenuItem citem;
      
      pPanelPopup = new JPopupMenu(); 
      
      pViewsContainingMenu = new JMenu("Views Containing");
      pPanelPopup.add(pViewsContainingMenu);

      pViewsEditingMenu = new JMenu("Views Editing");
      pPanelPopup.add(pViewsEditingMenu);

      pPanelPopup.addSeparator();

      item = new JMenuItem("Register...");
      pRegisterItem = item;
      item.setActionCommand("register");
      item.addActionListener(this);
      pPanelPopup.add(item);

      item = new JMenuItem("Clone...");
      pCloneItem = item;
      item.setActionCommand("clone");
      item.addActionListener(this);
      pPanelPopup.add(item);

      {
	JMenu sub;

	sub = new JMenu("Tools");
	pToolMenu = sub;
	pPanelPopup.add(sub);
	sub.setEnabled(false);
	sub.setVisible(false);

	sub = new JMenu("Tools");
	pDefaultToolMenu = sub;
	pPanelPopup.add(sub);
	sub.setEnabled(false);
	sub.setVisible(false);

	pRefreshDefaultToolMenu = true;
      }

      pPanelPopup.addSeparator();

      pFilterItems = new TreeMap<NodeFilter,JRadioButtonMenuItem>();
      pFilterGroup = new ButtonGroup();
      for(NodeFilter filter : NodeFilter.all()) {
        ritem = new JRadioButtonMenuItem(filter.toTitle());
        pFilterItems.put(filter, ritem);
        ritem.setActionCommand("Filter" + filter.toString());
        ritem.addActionListener(this);
        pFilterGroup.add(ritem);
        pPanelPopup.add(ritem);
      }          
      pFilterGroup.setSelected(pFilterItems.get(NodeFilter.AllNodes).getModel(), true);

      pPanelPopup.addSeparator();
        
      item  = new JMenuItem("Show/Hide Path");   
      pShowHideItem = item;
      item.setActionCommand("show-hide");
      item.addActionListener(this);
      pPanelPopup.add(item);

      citem = new JCheckBoxMenuItem("Show Hidden");   
      pShowHiddenItem = citem;
      citem.setActionCommand("show-hidden");
      citem.addActionListener(this);
      pPanelPopup.add(citem);

      pPanelPopup.addSeparator();

      item = new JMenuItem("Expand Selected");
      pExpandSelectedItem = item;
      item.setActionCommand("expand-selected");
      item.addActionListener(this);
      pPanelPopup.add(item);

      updateMenuToolTips();
    }

    /* initialize the panel components */ 
    {
      setLayout(new BorderLayout());

      {
	DefaultMutableTreeNode root = new DefaultMutableTreeNode("root", true);
	DefaultTreeModel model = new DefaultTreeModel(root, true);

	JTree tree = new JFancyTree(model); 
	pTree = tree;

	tree.setCellRenderer(new JNodeBrowserTreeCellRenderer(this));
	tree.setSelectionModel(null);
	tree.addTreeExpansionListener(this);
	tree.addMouseListener(this);
	tree.setFocusable(true);
	tree.addKeyListener(this);
      }

      {
	JScrollPane scroll = 
          UIFactory.createScrollPane
          (pTree, 
           ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED, 
           ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
           new Dimension(230, 120), new Dimension(230, 500), null);

	add(scroll);
      }
    }

    /* update the tree */ 
    updateNodeTree();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Get the title of this type of panel.
   */
  @Override
  public String 
  getTypeName() 
  {
    return "Node Browser";
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
  @Override
  public void
  setGroupID
  (
   int groupID
  )
  {
    UIMaster master = UIMaster.getInstance();

    PanelGroup<JNodeBrowserPanel> panels = master.getNodeBrowserPanels();

    if(pGroupID > 0)
      panels.releaseGroup(pGroupID);

    pGroupID = 0;
    if((groupID > 0) && panels.isGroupUnused(groupID)) {
      panels.assignGroup(this, groupID);
      pGroupID = groupID;
    }

    master.updateOpsBar();
  }

  /**
   * Is the given group currently unused for this type of panel.
   */ 
  @Override
  public boolean
  isGroupUnused
  (
   int groupID
  ) 
  {
    PanelGroup<JNodeBrowserPanel> panels = UIMaster.getInstance().getNodeBrowserPanels();
    return panels.isGroupUnused(groupID);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Are the contents of the panel read-only. <P> 
   */ 
  @Override
  public boolean
  isLocked() 
  {
    return (super.isLocked() && !pPrivilegeDetails.isNodeManaged(pAuthor));
  }
  
  /**
   * Set the author and view.
   */ 
  @Override
  public void 
  setAuthorView
  (
   String author, 
   String view 
  ) 
  {
    super.setAuthorView(author, view);

    if(pTree != null) 
      updatePanels(true); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Update all panels which share the current update channel.
   */ 
  private void 
  updatePanels() 
  {
    updatePanels(false);
  }

  /**
   * Update all panels which share the current update channel.
   */ 
  private void 
  updatePanels
  (
   boolean forceUpdate
  ) 
  {
    pSelectionModified = false;

    if (pGroupID != 0) {
      PanelUpdater pu = new PanelUpdater(this, forceUpdate);
      pu.execute();
    }
  }

  /**
   * Apply the updated information to this panel.
   * 
   * @param author
   *   Owner of the current working area.
   * 
   * @param view
   *   Name of the current working area view.
   * 
   * @param selected
   *   The nodes which should be currently selected in the browser.
   */
  public synchronized void 
  applyPanelUpdates
  (
   String author, 
   String view,
   TreeSet<String> selected
  )
  {
    if(!pAuthor.equals(author) || !pView.equals(view)) 
      super.setAuthorView(author, view);

    pToolsetCache.clear();

    updateSelection(selected);
  }

  /**
   * Warn about unsaved changes in dependent node detail panels prior to an panel operation.
   * 
   * @return 
   *   Whether previously unsaved changes where applied.
   */ 
  public boolean 
  warnUnsavedDetailPanelChangesBeforeOp
  (
   String opname
  ) 
  {
    UIMaster master = UIMaster.getInstance();

    {
      JNodeDetailsPanel panel = master.getNodeDetailsPanels().getPanel(getGroupID());
      if((panel != null) && panel.warnUnsavedChangesBeforeOp(opname))
        return true;
    }
    
    {
      JNodeHistoryPanel panel = master.getNodeHistoryPanels().getPanel(getGroupID());
      if((panel != null) && panel.warnUnsavedChangesBeforeOp(opname))
        return true;
    }
    
    {
      JNodeFilesPanel panel = master.getNodeFilesPanels().getPanel(getGroupID());
      if((panel != null) && panel.warnUnsavedChangesBeforeOp(opname))
        return true;
    }
    
    {
      JNodeLinksPanel panel = master.getNodeLinksPanels().getPanel(getGroupID());
      if((panel != null) && panel.warnUnsavedChangesBeforeOp(opname))
        return true;
    }
    
    return false; 
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the fully resolved names of the nodes associated with the current tree selection.
   */ 
  public TreeSet<String>
  getSelected() 
  {
    return new TreeSet<String>(pSelected);
  }

  /**
   * Is the given tree path selected?
   */ 
  public boolean 
  isSelected
  (
   TreePath tpath
  ) 
  {
    return pSelected.contains(treePathToNodeName(tpath));
  }


  /**
   * Replace the current tree node selection with the given set of nodes. <P> 
   * 
   * Does not cause a multi-panel update.
   * 
   * @param names
   *   The fully resolved names of the nodes to select.
   */
  public void 
  updateSelection
  ( 
   TreeSet<String> names
  ) 
  {
    pSelected.clear();
    pSelected.addAll(names);

    updateNodeTree(getExpandedPaths(), null);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Replace the current tree node selection with the given set of nodes. <P> 
   *
   * Does not cause a multi-panel update and does not make a call to updateNodeTree.
   *
   * @param names
   *   The fully resolved names of the nodes to select.
   */
  private void
  setSelected
  (
   TreeSet<String> names
  )
  {
    pSelected.clear();
    pSelected.addAll(names);
  }

  /**
   * Replace the current tree node selection with the given set of nodes. <P> 
   *
   * Does not cause a multi-panel update and does not make a call to updateNodeTree.
   *
   * @param author
   *
   * @param view
   *
   * @param names
   *   The fully resolved names of the nodes to select.
   */
  private void
  setSelected
  (
   String author, 
   String view, 
   TreeSet<String> names
  )
  {
    super.setAuthorView(author, view);

    pSelected.clear();
    pSelected.addAll(names);
  }

  /**
   * Update the node tree componenents based on the currently expanded paths.
   */ 
  private void 
  updateNodeTree()
  {
    updateNodeTree(getExpandedPaths(), null, false);
  }

  private void 
  updateNodeTree
  (
   boolean expandSelected
  )
  {
    updateNodeTree(getExpandedPaths(), null, expandSelected);
  }

  /**
   * Update the node tree componenents based on the currently expanded paths.
   * 
   * @param deepExpanded
   *   If not <CODE>null</CODE>, update and expande all node paths under this path.
   */ 
  private void 
  updateNodeTree
  (    
   String deep
  )
  {
    updateNodeTree(getExpandedPaths(), deep, false);
  }

  private void 
  updateNodeTree
  (    
   String deep, 
   boolean expandSelected
  )
  {
    updateNodeTree(getExpandedPaths(), deep, expandSelected);
  }

  /**
   * Update the node tree componenents based on the given set of expanded paths. <P> 
   * 
   * @param expanded
   *   The paths of the expanded tree nodes.
   * 
   * @param deep
   *   If not <CODE>null</CODE>, update and expand all node paths under this path.
   *
   * @param expandSelected
   *   If true overrides the preference for expanding the node browser for 
   *   selected nodes.
   */ 
  private void 
  updateNodeTree
  (
   TreeSet<String> expanded, 
   String deep 
  )
  {
    updateNodeTree(expanded, deep, false);
  }

  private void 
  updateNodeTree
  (
   TreeSet<String> expanded, 
   String deep, 
   boolean expandSelected
  )
  {
    if(pTree == null) 
      return;

    /* get the updated node tree */ 
    NodeTreeComp comp = null;
    {
      updatePrivileges();

      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.acquireMasterMgrClient();
      try { 
        TreeMap<String,Boolean> paths = new TreeMap<String,Boolean>();
        {
          for(String path : expanded)
            paths.put(path, (deep != null) && path.equals(deep));
          
          for(String path : pSelected)
            if(expandSelected || UserPrefs.getInstance().getExpandSelected())
              paths.put(path, false);
        }
        
        comp = client.updatePaths(pAuthor, pView, paths, pShowHiddenItem.getState()); 
      }
      catch(PipelineException ex) {
        master.showErrorDialog(ex);
        return;
      }
      finally {
        master.releaseMasterMgrClient(client);
      }
    }

    /* rebuild the tree model based on the updated node tree */ 
    if(comp != null) {
      DefaultMutableTreeNode root = new DefaultMutableTreeNode(new NodeTreeComp());
      rebuildTreeModel("", comp, root, expanded, deep);
      
      DefaultTreeModel model = (DefaultTreeModel) pTree.getModel();
      model.setRoot(root);
      
      pTree.removeTreeExpansionListener(this);
      rexpandPaths(root);
      pTree.addTreeExpansionListener(this);
    }
  }

  /**
   * Get the Pipeline node paths of the currently expanded branch nodes 
   */ 
  private TreeSet<String>
  getExpandedPaths() 
  {
    TreeSet<String> paths = new TreeSet<String>();

    if(pTree != null) {
      DefaultMutableTreeNode root = (DefaultMutableTreeNode) pTree.getModel().getRoot();
      Enumeration e = pTree.getExpandedDescendants(new TreePath(root.getPath()));
      if(e != null) {
	while(e.hasMoreElements()) {
	  TreePath tpath = (TreePath) e.nextElement(); 

	  String name = treePathToNodeName(tpath);

	  if(name.length() > 0)
	    paths.add(name);
	}
      }
      
      if(paths.isEmpty()) 
	paths.add("/");
    }

    return paths;
  }

  /** 
   * Convert a Swing tree path into a fully resolved Pipeline node path (or name).
   */ 
  private String
  treePathToNodeName
  (
   TreePath tpath
  ) 
  {
    StringBuilder buf = new StringBuilder();
    Object[] path = tpath.getPath();
    int wk;
    for(wk=1; wk<path.length; wk++) 
      buf.append("/" + path[wk]);
    return (buf.toString());
  }

  /**
   * Recursively reconstruct the node tree.
   */ 
  private void 
  rebuildTreeModel
  (
   String path,
   NodeTreeComp cnode, 
   DefaultMutableTreeNode tnode,
   TreeSet<String> expanded,
   String deep
  )
  { 
    for(NodeTreeComp comp : cnode.values()) {
      String cpath = (path + "/" + comp);
      
      switch(comp.getState()) {
      case Branch:
	{
	  DefaultMutableTreeNode child = new DefaultMutableTreeNode(comp, true);
	  tnode.add(child);
      
	  rebuildTreeModel(cpath, comp, child, expanded, deep); 
          
	  if((expanded.contains(cpath) || ((deep != null) && cpath.startsWith(deep))) &&
             child.isLeaf() && (comp.getState() == NodeTreeComp.State.Branch)) {
	    DefaultMutableTreeNode hidden = new DefaultMutableTreeNode(null, false);
	    child.add(hidden);
	  }
	}
	break;
      }
    }

    for(NodeTreeComp comp : cnode.values()) {
      String cpath = (path + "/" + comp);
      
      switch(comp.getState()) {
      case WorkingCurrentCheckedInSome:
      case WorkingOtherCheckedInSome:
      case WorkingNoneCheckedInSome:
      case WorkingCurrentCheckedInNone:
      case WorkingOtherCheckedInNone:
	{
	  Boolean show = pFilter.get(comp.getState());
	  if(show) {
	    switch(comp.getState()) {
	    case WorkingOtherCheckedInNone:
	      pSelected.remove(cpath);
	    }
	    
	    DefaultMutableTreeNode child = new DefaultMutableTreeNode(comp, false);
	    tnode.add(child);
	  }
	  else {
	    pSelected.remove(cpath);
	  }
	}
      }
    }
  }

  /**
   * Expand the reconstructed branch nodes.
   */ 
  private void 
  rexpandPaths
  (
   DefaultMutableTreeNode root
  ) 
  {
    Enumeration e = root.depthFirstEnumeration();
    if(e != null) {
      while(e.hasMoreElements()) {
	DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) e.nextElement(); 
        if(tnode.getChildCount() > 0) 
          pTree.expandPath(new TreePath(tnode.getPath()));
      }
    }    
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Refocus keyboard events on this panel if it contains the mouse.
   * 
   * @return
   *   Whether the panel has received the focus.
   */ 
  @Override
  public boolean 
  refocusOnPanel() 
  {
    if(pTree.getMousePosition(true) != null) {
      pTree.requestFocusInWindow();
      return true;
    }

    return false;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the panel to reflect new user preferences.
   */ 
  @Override
  public void 
  updateUserPrefs() 
  {
    TextureMgr.getInstance().rebuildIcons();

    updateMenuToolTips();

    {
      UserPrefs prefs = UserPrefs.getInstance();

      if(pDisplayPrimarySuffix != prefs.getDisplayPrimarySuffix()) {
        pDisplayPrimarySuffix = prefs.getDisplayPrimarySuffix();
        updateNodeTree();
      }
    }
  }


  /**
   * Update the menu item tool tips.
   */ 
  private void 
  updateMenuToolTips() 
  {
    UserPrefs prefs = UserPrefs.getInstance();

    updateMenuToolTip
      (pRegisterItem, null,
       "Register a new node.");

    updateMenuToolTip
      (pCloneItem, null,
       "Register a new node which is a clone of the current primary selection."); 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Reset the caches of toolset plugins and plugin menu layouts.
   */ 
  @Override
  public void 
  clearPluginCache()
  {
    pToolMenuToolset = null;
    pRefreshDefaultToolMenu = true;
  }

  /**
   * Update the tool plugin menus (over a node).
   */ 
  private synchronized void 
  updateToolMenu()
  {
    if(pPrimaryNodeComp == null)
      return;
    else {
      switch(pPrimaryNodeComp.getState()) {
	case Branch:
	case WorkingOtherCheckedInNone:
	  return;
      }
    }

    String toolset = pToolsetCache.get(pPrimaryNodePath);

    if(toolset == null) {
      NodeCommon node = null;
      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.acquireMasterMgrClient();
      try {
	try {
	  node = client.getWorkingVersion(pAuthor, pView, pPrimaryNodePath);
	}
	catch(PipelineException ex2) {
	}

	if(node == null) {
	  try {
	    node = client.getCheckedInVersion(pPrimaryNodePath, null);
	  }
	  catch(PipelineException ex2) {
	  }
	}

	if(node == null)
	  throw new PipelineException
	    ("Unable to obtain a NodeCommon for (" + node.getName() + ")!");
      }
      catch(PipelineException ex) {
	master.showErrorDialog(ex);
	return;
      }
      finally {
	master.releaseMasterMgrClient(client);
      }

      if(node != null) {
	toolset = node.getToolset();
	pToolsetCache.put(pPrimaryNodePath, toolset);
      }
    }

    if((toolset != null) && !toolset.equals(pToolMenuToolset)) {
      UIMaster master = UIMaster.getInstance();
      master.rebuildToolMenu(pPanelPopup, pGroupID, toolset, pToolMenu, this);

      pToolMenuToolset = toolset;
    }
  }

  /**
   * Update the default tool plugin menus (nothing under the mouse).
   */ 
  private synchronized void 
  updateDefaultToolMenu()
  {
    if(pRefreshDefaultToolMenu) {
      UIMaster master = UIMaster.getInstance();
      master.rebuildDefaultToolMenu(pPanelPopup, pGroupID, pDefaultToolMenu, this);
      
      pRefreshDefaultToolMenu = false; 
    }    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- TREE EXPANSION LISTENER METHODS -----------------------------------------------------*/

  /**
   * Called whenever an item in the tree has been collapsed.
   */ 
  public void
  treeCollapsed
  (
   TreeExpansionEvent e
  )
  {
    {
      TreePath tpath = e.getPath();
      String   tname = treePathToNodeName(tpath);
    }
  }

  /**
   * Called whenever an item in the tree has been expanded.
   */ 
  public void 	
  treeExpanded
  (
   TreeExpansionEvent e
  )
  {
    {
      TreePath tpath = e.getPath();
      String   tname = treePathToNodeName(tpath);
    }

    updateNodeTree();
  }


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
  mouseEntered
  (
   MouseEvent e
  ) 
  {
    pTree.requestFocusInWindow();
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

    if(pSelectionModified) 
      updatePanels();
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

    TreePath tpath = pTree.getPathForLocation(e.getX(), e.getY());

    /* local mouse events */ 
    int mods = e.getModifiersEx();
    switch(e.getButton()) {
    case MouseEvent.BUTTON1:
      {
	if(tpath != null) {
	  DefaultMutableTreeNode tnode = 
	    (DefaultMutableTreeNode) tpath.getLastPathComponent();
	  NodeTreeComp comp = (NodeTreeComp) tnode.getUserObject();
	  if(comp != null) {
	    switch(comp.getState()) {
	    case Branch:
	      {
		int on1  = (MouseEvent.BUTTON1_DOWN_MASK);
		
		int off1 = (MouseEvent.BUTTON2_DOWN_MASK | 
			    MouseEvent.BUTTON3_DOWN_MASK | 
			    MouseEvent.SHIFT_DOWN_MASK |
			    MouseEvent.ALT_DOWN_MASK |
			    MouseEvent.CTRL_DOWN_MASK);
		
		
		int on2  = (MouseEvent.BUTTON1_DOWN_MASK |
			    MouseEvent.SHIFT_DOWN_MASK);
		
		int off2 = (MouseEvent.BUTTON2_DOWN_MASK | 
			    MouseEvent.BUTTON3_DOWN_MASK | 
			    MouseEvent.SHIFT_DOWN_MASK |
			    MouseEvent.CTRL_DOWN_MASK);
		
		
		/* BUTTON1: expand/collapse */ 
		if((mods & (on1 | off1)) == on1) {
		}
		
		/* BUTTON1+SHIFT: deep expand/collapse */ 
		else if((mods & (on2 | off2)) == on2) {
		  updateNodeTree(treePathToNodeName(tpath));
		}
		
		/* UNSUPPORTED */ 
		else {
		  if(UIFactory.getBeepPreference())
		    Toolkit.getDefaultToolkit().beep();
		}
	      }
	      break;
	      
	    case WorkingCurrentCheckedInSome:
	    case WorkingOtherCheckedInSome:
	    case WorkingNoneCheckedInSome:
	    case WorkingCurrentCheckedInNone:
	      if(!isPanelOpInProgress()) {
		int on1  = (MouseEvent.BUTTON1_DOWN_MASK);
		
		int off1 = (MouseEvent.BUTTON2_DOWN_MASK | 
			    MouseEvent.BUTTON3_DOWN_MASK | 
			    MouseEvent.SHIFT_DOWN_MASK |
			    MouseEvent.ALT_DOWN_MASK |
			    MouseEvent.CTRL_DOWN_MASK);
		
		
		int on2  = (MouseEvent.BUTTON1_DOWN_MASK |
			    MouseEvent.SHIFT_DOWN_MASK);
		
		int off2 = (MouseEvent.BUTTON2_DOWN_MASK | 
			    MouseEvent.BUTTON3_DOWN_MASK | 
			    MouseEvent.SHIFT_DOWN_MASK |
			    MouseEvent.CTRL_DOWN_MASK);
		
		
		int on3  = (MouseEvent.BUTTON1_DOWN_MASK |
			    MouseEvent.SHIFT_DOWN_MASK |
			    MouseEvent.CTRL_DOWN_MASK);
		
		int off3 = (MouseEvent.BUTTON2_DOWN_MASK | 
			    MouseEvent.BUTTON3_DOWN_MASK | 
			    MouseEvent.ALT_DOWN_MASK);
		
		
		String sname = treePathToNodeName(tpath);
		
		/* BUTTON1: replace selection */ 
		if((mods & (on1 | off1)) == on1) {
		  pSelected.clear();
		  pSelected.add(sname);
		  repaint();
		  
		  updatePanels();
		}
		
		/* BUTTON1+SHIFT: toggle selection */ 
		else if((mods & (on2 | off2)) == on2) {
		  if(pSelected.contains(sname))
		    pSelected.remove(sname);
		  else 
		    pSelected.add(sname);
		  repaint();
		  pSelectionModified = true;
		}
		
		/* BUTTON1+SHIFT+CTRL: add to the selection */ 
		else if((mods & (on3 | off3)) == on3) {
		  pSelected.add(sname);
		  repaint();
		  pSelectionModified = true;
		}
		
		else {
		  if(UIFactory.getBeepPreference())
		    Toolkit.getDefaultToolkit().beep();
		}
	      }
	    }
	  }
	}
      }
      break;

    case MouseEvent.BUTTON3:
      {
	int on1  = (MouseEvent.BUTTON3_DOWN_MASK);
	
	int off1 = (MouseEvent.BUTTON1_DOWN_MASK | 
		    MouseEvent.BUTTON2_DOWN_MASK | 
		    MouseEvent.SHIFT_DOWN_MASK |
		    MouseEvent.ALT_DOWN_MASK |
		    MouseEvent.CTRL_DOWN_MASK);

	pPrimaryNodeComp = null;
	pPrimaryNodePath = null;

	{
	  pCloneItem.setEnabled(false);
	  pCloneItem.setVisible(false);

	  pToolMenu.setEnabled(false);
	  pToolMenu.setVisible(false);

	  pDefaultToolMenu.setEnabled(false);
	  pDefaultToolMenu.setVisible(false);
	}
	
	/* BUTTON3: panel popup menu */ 
	if((mods & (on1 | off1)) == on1) {
	  
	  String sname = null;
	  if(tpath != null) {
	    DefaultMutableTreeNode tnode = 
	      (DefaultMutableTreeNode) tpath.getLastPathComponent();
	    NodeTreeComp comp = (NodeTreeComp) tnode.getUserObject();
	    if(comp != null) {
	      switch(comp.getState()) {
	      case WorkingCurrentCheckedInSome:
	      case WorkingOtherCheckedInSome:
	      case WorkingCurrentCheckedInNone:
	      case WorkingOtherCheckedInNone:
		sname = treePathToNodeName(tpath);
	      }

	      pPrimaryNodeComp = comp;
	      pPrimaryNodePath = treePathToNodeName(tpath);
	    }
	  }
	  
	  UIMaster master = UIMaster.getInstance();
	  master.rebuildWorkingAreaContainingMenu
	    (pGroupID, sname, pViewsContainingMenu, this);
	  master.rebuildWorkingAreaEditingMenu
	    (pGroupID, sname, pViewsEditingMenu, this);

	  boolean isOverNode = true;
	  boolean isOverPath = false;
	  if(pPrimaryNodeComp != null) {
	    switch(pPrimaryNodeComp.getState()) {
	    case Branch:
	      isOverNode = false;
	    }

	    isOverPath = true;
	  }
	  else
	    isOverNode = false;

          pRegisterItem.setText(isOverPath ? "Register In..." : "Register...");
          
          if(isOverPath) {
            pShowHideItem.setEnabled(true);
            pShowHideItem.setText(pPrimaryNodeComp.isHidden() ? "Show Path" : "Hide Path");
          }
          else {
            pShowHideItem.setEnabled(false);
            pShowHideItem.setText("Show/Hide Path"); 
          }

	  boolean isDefaultToolMenu = true;
	  if(isOverNode) {
	    pCloneItem.setEnabled(true);
	    pCloneItem.setVisible(true);

	    switch(pPrimaryNodeComp.getState()) {
	    case WorkingCurrentCheckedInSome:
	    case WorkingOtherCheckedInSome:
	    case WorkingNoneCheckedInSome:
	    case WorkingCurrentCheckedInNone:
	      isDefaultToolMenu = false;
	      break;
	    }
	  }

	  if(isDefaultToolMenu) {
	    updateDefaultToolMenu();
	    pDefaultToolMenu.setEnabled(true);
	    pDefaultToolMenu.setVisible(true);
	  }
	  else {
	    updateToolMenu();
	    pToolMenu.setEnabled(true);
	    pToolMenu.setVisible(true);
	  }

	  pPanelPopup.show(e.getComponent(), e.getX(), e.getY());
	}
	else {
	  if(UIFactory.getBeepPreference())
	    Toolkit.getDefaultToolkit().beep();
	}
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
    if((prefs.getUpdate() != null) &&
       prefs.getUpdate().wasPressed(e)) 
      updatePanels(true);

    else {
      switch(e.getKeyCode()) {
      case KeyEvent.VK_SHIFT:
      case KeyEvent.VK_CONTROL:
      case KeyEvent.VK_ALT:
	break;

      default:
	if(UIFactory.getBeepPreference())
	  Toolkit.getDefaultToolkit().beep();
      }
    }
  }

  /**
   * Invoked when a key has been released.
   */ 
  public void 	
  keyReleased
  (
   KeyEvent e
  ) 
  {
    int mods = e.getModifiersEx();

    if(pSelectionModified) {
      switch(e.getKeyCode()) {
      case KeyEvent.VK_SHIFT:
      case KeyEvent.VK_CONTROL:
	{
	  int on1  = 0;
	  
	  int off1 = (MouseEvent.SHIFT_DOWN_MASK |
		      MouseEvent.CTRL_DOWN_MASK);

	  if((mods & (on1 | off1)) == on1) 
	    updatePanels();
	}      
      }
    }
  }

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
    if(cmd.startsWith("expand-selected"))
      doExpandSelected();
    else if(cmd.startsWith("author-view:")) 
      doChangeAuthorView(cmd.substring(12));
    else if(cmd.startsWith("register"))
      doRegister();
    else if(cmd.startsWith("clone"))
      doClone();

    else if(cmd.equals("FilterAllNodes"))
      doFilterAllNodes();
    else if(cmd.equals("FilterCurrentView"))
      doFilterCurrentView();
    else if(cmd.equals("FilterAnyViews"))
      doFilterAnyViews();
    else if(cmd.equals("FilterNoViews"))
      doFilterNoViews();

    else if(cmd.equals("show-hide")) 
      doShowHide();
    else if(cmd.equals("show-hidden")) 
      updateNodeTree();

    /* tool menu events */ 
    else if(cmd.startsWith("run-tool:")) 
      doRunTool(cmd.substring(9));
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Expand the node browser tree to display all the selected nodes.
   */
  public void
  doExpandSelected()
  {
    updateNodeTree(true);
  }

  /**
   * Change working area view.
   */ 
  private void 
  doChangeAuthorView
  (
   String workingArea
  ) 
  {
    String parts[] = workingArea.split(":");
    assert(parts.length == 2);

    pSelected.add(pPrimaryNodePath); 

    String author = parts[0];
    String view   = parts[1];
    if(!pAuthor.equals(author) || !pView.equals(view)) 
      setAuthorView(author, view);       
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Register a new node.
   */ 
  private synchronized void 
  doRegister() 
  {
    if(warnUnsavedDetailPanelChangesBeforeOp("Register")) 
      return;

    if(pRegisterDialog == null) 
      pRegisterDialog = new JRegisterDialog(pGroupID, getTopFrame());

    String prefix = null;
    if(pPrimaryNodeComp != null) {
      switch(pPrimaryNodeComp.getState()) {
      case Branch:
	prefix = pPrimaryNodePath + "/";
	break;
      default:
	prefix = pPrimaryNodePath.substring(0, pPrimaryNodePath.lastIndexOf('/') + 1);
      }
    }

    pRegisterDialog.updateNode(pAuthor, pView, prefix);
    pRegisterDialog.setVisible(true); 

    TreeSet<String> names = pRegisterDialog.getRegistered();
    if(!names.isEmpty()) {
      pSelected.addAll(names);
      updatePanels();
    }
  }

  /**
   * Register a new node based on the primary selected node.
   */ 
  private synchronized void 
  doClone() 
  {
    if(warnUnsavedDetailPanelChangesBeforeOp("Clone")) 
      return;

    boolean selectionModified = false;

    if(pPrimaryNodeComp != null) {
      switch(pPrimaryNodeComp.getState()) {
      case Branch:
	return;
      }

      ClonePrepTask task = new ClonePrepTask();
      task.start();
    }
  }

  /**
   * Obtain a NodeTreeComp for the sources of the primary selected node prior
   * to displaying the Clone dialog.
   */
  private
  class ClonePrepTask
    extends Thread
  {
    public
    ClonePrepTask()
    {
      super("JNodeViewerPanel:ClonePrepTask");
    }

    @Override
    public void
    run()
    {
      NodeCommon node = null;
      NodeTreeComp workingSources = null;
      boolean hasWorkingVersion = false;

      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.acquireMasterMgrClient();
      try {
	try {
	  node = client.getWorkingVersion(pAuthor, pView, pPrimaryNodePath);
	}
	catch(PipelineException ex2) {
	}

	if(node == null) {
	  try {
	    node = client.getCheckedInVersion(pPrimaryNodePath, null);
	  }
	  catch(PipelineException ex2) {
	  }
	}
	else
	  hasWorkingVersion = true;

	if(node == null)
	  throw new PipelineException
	    ("Unable to obtain a NodeCommon for (" + pPrimaryNodePath + ")!");

	TreeMap<String,Boolean> paths = new TreeMap<String,Boolean>();
	  for(String sname : node.getSourceNames())
	    paths.put(sname, false);
	      
	workingSources = client.updatePaths(pAuthor, pView, paths);
	if(workingSources == null)
	  throw new PipelineException
	    ("Unable to obtain a NodeTreeComp for the sources of " + 
	     "(" + node.getName() + ")!");
      }
      catch(PipelineException ex) {
	master.showErrorDialog(ex);
	return;
      }
      finally {
	master.releaseMasterMgrClient(client);
      }

      CloneTask task = new CloneTask(node, workingSources, hasWorkingVersion);
      SwingUtilities.invokeLater(task);
    }
  }

  /**
   * Display the Clone dialog and update the Node Viewer after the dialog
   * is dismissed.
   */
  private
  class CloneTask
    extends Thread
  {
    public
    CloneTask
    (
     NodeCommon node, 
     NodeTreeComp workingSources, 
     boolean hasWorkingVersion
    )
    {
      pNode = node;
      pWorkingSources = workingSources;
      pHasWorkingVersion = hasWorkingVersion;
    }

    @Override
    public void
    run()
    {
      if(pCloneDialog == null) 
	pCloneDialog = new JCloneDialog(pGroupID, getTopFrame());

      pCloneDialog.updateNode(pAuthor, pView, pNode, pWorkingSources, pHasWorkingVersion);
      pCloneDialog.setVisible(true);

      TreeSet<String> names = pCloneDialog.getRegistered();
      if(!names.isEmpty()) {
	pSelected.addAll(names);
	updateNodeTree(getExpandedPaths(), null);
	updatePanels();
      }
    }

    private NodeCommon  pNode;
    private NodeTreeComp  pWorkingSources;
    private boolean  pHasWorkingVersion;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the node filters for All Nodes.
   */ 
  public void 
  doFilterAllNodes() 
  {
    pFilter.put(NodeTreeComp.State.WorkingCurrentCheckedInSome, true);
    pFilter.put(NodeTreeComp.State.WorkingOtherCheckedInSome,   true);
    pFilter.put(NodeTreeComp.State.WorkingNoneCheckedInSome,    true);
    pFilter.put(NodeTreeComp.State.WorkingCurrentCheckedInNone, true);
    pFilter.put(NodeTreeComp.State.WorkingOtherCheckedInNone,   true);

    updateNodeTree();
  }

  /**
   * Set the node filters for Current View.
   */ 
  public void 
  doFilterCurrentView() 
  {
    pFilter.put(NodeTreeComp.State.WorkingCurrentCheckedInSome, true);
    pFilter.put(NodeTreeComp.State.WorkingOtherCheckedInSome,   false);
    pFilter.put(NodeTreeComp.State.WorkingNoneCheckedInSome,    false);
    pFilter.put(NodeTreeComp.State.WorkingCurrentCheckedInNone, true);
    pFilter.put(NodeTreeComp.State.WorkingOtherCheckedInNone,   false);

    updateNodeTree();
  }

  /**
   * Set the node filters for Any Views.
   */ 
  public void 
  doFilterAnyViews() 
  {
    pFilter.put(NodeTreeComp.State.WorkingCurrentCheckedInSome, true);
    pFilter.put(NodeTreeComp.State.WorkingOtherCheckedInSome,   true);
    pFilter.put(NodeTreeComp.State.WorkingNoneCheckedInSome,    false);
    pFilter.put(NodeTreeComp.State.WorkingCurrentCheckedInNone, true);
    pFilter.put(NodeTreeComp.State.WorkingOtherCheckedInNone,   true);

    updateNodeTree();
  }

  /**
   * Set the node filters for No Views.
   */ 
  public void 
  doFilterNoViews() 
  {
    pFilter.put(NodeTreeComp.State.WorkingCurrentCheckedInSome, false);
    pFilter.put(NodeTreeComp.State.WorkingOtherCheckedInSome,   false);
    pFilter.put(NodeTreeComp.State.WorkingNoneCheckedInSome,    true);
    pFilter.put(NodeTreeComp.State.WorkingCurrentCheckedInNone, false);
    pFilter.put(NodeTreeComp.State.WorkingOtherCheckedInNone,   false);

    updateNodeTree();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Show or hide the given node or path from being displayed in the browser.
   */ 
  public void 
  doShowHide() 
  {
    if((pPrimaryNodePath != null) && (pPrimaryNodeComp != null)) {
      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.acquireMasterMgrClient();
      try { 
        client.setPathHidden(pPrimaryNodePath, !pPrimaryNodeComp.isHidden());
      }
      catch(PipelineException ex) {
        master.showErrorDialog(ex);
        return;
      }
      finally {
        master.releaseMasterMgrClient(client);
      }
    }

    updateNodeTree();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Run the given tool plugin.
   */ 
  private synchronized void 
  doRunTool
  (
   String name 
  ) 
  {
    if(warnUnsavedDetailPanelChangesBeforeOp("Run Tool")) 
      return;

    String parts[] = name.split(":");
    assert(parts.length == 3);
    
    String tname   = parts[0];
    VersionID tvid = new VersionID(parts[1]);
    String tvendor = parts[2];
    
    String primary = null;
    String prefix = null;
    if(pPrimaryNodeComp != null) {
      switch(pPrimaryNodeComp.getState()) {
      case Branch:
	prefix = pPrimaryNodePath + "/";
	break;
      default:
	primary = pPrimaryNodePath;
	prefix  = pPrimaryNodePath.substring(0, pPrimaryNodePath.lastIndexOf('/') + 1);
      }
    }

    ToolPrepTask task = new ToolPrepTask(tname, tvid, tvendor, primary, prefix);
    task.start();
  }

  /**
   * Retrieve the NodeStatus for the node under the mouse position in another 
   * thread to avoid jamming up the Swing Event Dispatch Thread.
   */
  private
  class ToolPrepTask
    extends Thread
  {
    public
    ToolPrepTask
    (
     String name, 
     VersionID vid, 
     String vendor, 
     String primary, 
     String prefix
    )
    {
      super("JNodeBrowserPanel:ToolPrepTask");

      pName = name;
      pVid = vid;
      pVendor = vendor;
      pPrimary = primary;
      pPrefix = prefix;
    }

    @Override
    public
    void run()
    {
      UIMaster master = UIMaster.getInstance();
      try {
	TreeMap<String,NodeStatus> selected = new TreeMap<String,NodeStatus>();
	NodeStatus status = null;
	if(pPrimary != null) {
	  MasterMgrClient client = master.acquireMasterMgrClient();
	  try {
	    status = client.status(pAuthor, pView, pPrimary, true, DownstreamMode.All);
	  }
	  finally {
	    master.releaseMasterMgrClient(client);
	  }

	  if(status == null)
	    throw new PipelineException
	      ("Unable to obtain status for (" + pPrimary + ")!");

	  selected.put(pPrimary, status);
	}

	TreeSet<String> roots = new TreeSet<String>(pSelected);
	BaseTool tool = PluginMgrClient.getInstance().newTool(pName, pVid, pVendor);

	tool.initExecution(pAuthor, pView, pPrimary, pPrefix, selected, roots);

	ToolOpTask task = new ToolOpTask(tool, pGroupID);
	task.start();
      }
      catch(PipelineException ex) {
	master.showErrorDialog(ex);
	return;
      }
    }

    private String  pName;
    private VersionID  pVid;
    private String  pVendor;
    private String  pPrimary;
    private String  pPrefix;
  }

  /** 
   * Acquire the channel operation lock and release it when the tool is done.
   */ 
  private
  class ToolOpTask
    extends Thread
  {
    public 
    ToolOpTask
    (
     BaseTool tool, 
     int groupID
    ) 
    {
      super("JNodeBrowserPanel:ToolOpTask");

      pTool = tool;
      pGID  = groupID;
      pLock = new Object(); 
    }

    public synchronized void 
    endTool
    (
     boolean success
    ) 
    {
      synchronized(pLock) {
	pSuccess = success;
	pMessage = pTool.getName() + (pSuccess ? " Done." : " Aborted.");
	pLock.notify();
      }
    }

    public void 
    updateTool
    (
     String msg
    ) 
    {
      synchronized(pLock) {
	pMessage = msg;
	pLock.notify();
      }
    }

    @Override
    public void 
    run() 
    {	
      UIMaster master = UIMaster.getInstance();
      if(!master.beginPanelOp(pGID, "Running " + pTool.getName() + pMessage))
	return;

      try {
	SwingUtilities.invokeLater(new ToolInputTask(pTool, pGID, this));

        if(pTool.showLogHistory()) 
          SwingUtilities.invokeLater(new ToolShowLogsTask());

	synchronized(pLock) {
	  while(pSuccess == null) {
	    pLock.wait();
	    
	    if(pMessage != null) 
	      master.updatePanelOp(pGID, pMessage);
	  }

          master.endPanelOp(pGID, pMessage);
	}

        if((pSuccess) && pTool.updateOnExit()) {
          String author = pTool.authorOnExit();
          String view = pTool.viewOnExit();
	  TreeSet<String> roots = pTool.rootsOnExit();

          if(!pAuthor.equals(author) || !pView.equals(view))
	    setSelected(author, view, roots);
          else
	    setSelected(roots);

	  updatePanels(true);
        }
      }
      catch(Exception ex) {
	pMessage = "Unexpected Failure!";
	master.showErrorDialog(ex);
	master.endPanelOp(pGID, pMessage);
      }
    }

    private BaseTool  pTool;
    private int       pGID; 
    private Object    pLock; 
    private Boolean   pSuccess;
    private String    pMessage; 
  }

  /** 
   * Show the Log History dialog with tool output enabled.
   */ 
  private
  class ToolShowLogsTask
    extends Thread
  {
    public 
    ToolShowLogsTask() 
    {
      super("JNodeBrowserPanel:ToolShowLogsTask");
    }

    
    @Override
    public void 
    run() 
    {	
      UIMaster master = UIMaster.getInstance();
      master.showLogsDialog(true);
    }
  }

  /** 
   * Collect user input for the next tool phase.
   */ 
  private
  class ToolInputTask
    extends Thread
  {
    public 
    ToolInputTask
    (
     BaseTool tool, 
     int groupID,
     ToolOpTask task
    ) 
    {
      super("JNodeBrowserPanel:ToolInputTask");

      pTool   = tool;
      pGID    = groupID;
      pOpTask = task;
    }

    @Override
    public void 
    run() 
    {	
      UIMaster master = UIMaster.getInstance();
      try {
	String msg = pTool.collectPhaseInput();
	if(msg != null) {
	  RunToolTask task = new RunToolTask(pTool, pGID, pOpTask, msg);
	  task.start();	
	}
	else {
	  pOpTask.endTool(true); 
	}
      }
      catch(Exception ex) {
	pOpTask.endTool(false); 
	master.showErrorDialog(ex);
      }
      catch(LinkageError er) {
        pOpTask.endTool(false); 
        master.showErrorDialog(er);
      }
    }

    private BaseTool    pTool;
    private int         pGID; 
    private ToolOpTask  pOpTask; 
  }

  /** 
   * Run the next tool phase. 
   */ 
  private
  class RunToolTask
    extends Thread
  {
    public 
    RunToolTask
    (
     BaseTool tool, 
     int groupID,
     ToolOpTask task, 
     String msg
    ) 
    {
      super("JNodeBrowserPanel:RunToolTask");

      pTool    = tool;
      pGID     = groupID;
      pOpTask  = task;
      pMessage = msg; 
    }

    @Override
    public void 
    run() 
    {
      pOpTask.updateTool("Running " + pTool.getName() + pMessage); 

      UIMaster master = UIMaster.getInstance();
      MasterMgrClient mclient = master.acquireMasterMgrClient(); 
      QueueMgrClient  qclient = master.acquireQueueMgrClient(); 
      try {
	if(pTool.executePhase(mclient, qclient)) {
	  pOpTask.updateTool("Completed " + pTool.getName() + " Phase.");
	  SwingUtilities.invokeLater(new ToolInputTask(pTool, pGID, pOpTask));
	}
	else {
	  pOpTask.endTool(true);
	}
      }
      catch(Exception ex) {
	pOpTask.endTool(false); 
	master.showErrorDialog(ex);
      }
      finally {
        master.releaseMasterMgrClient(mclient);
        master.releaseQueueMgrClient(qclient);
      }
    }

    private BaseTool    pTool;
    private int         pGID; 
    private ToolOpTask  pOpTask; 
    private String      pMessage; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  @Override
  public void 
  toGlue
  ( 
   GlueEncoder encoder   
  ) 
    throws GlueException
  {
    super.toGlue(encoder);
    
    TreeSet<String> expanded = getExpandedPaths();
    if(!expanded.isEmpty()) 
      encoder.encode("ExpandedPaths", expanded);
  }

  @Override
  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    TreeSet<String> expanded = (TreeSet<String>) decoder.decode("ExpandedPaths");
    if(expanded != null) 
      updateNodeTree(expanded, null);
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The 
   */
  public static
  enum NodeFilter
  {
    AllNodes, CurrentView, AnyViews, NoViews;
    
    public String
    toTitle() 
    {
      return sTitles[ordinal()];
    }

    public static ArrayList<NodeFilter>
    all() 
    {
      NodeFilter values[] = values();
      ArrayList<NodeFilter> all = new ArrayList<NodeFilter>(values.length);
      int wk;
      for(wk=0; wk<values.length; wk++)
        all.add(values[wk]);
      return all;
    }
    
    private static String sTitles[] = {
      "All Nodes", "Current View", "Any Views", "No Views"
    };
  }




  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -6904393553836010999L;


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The node tree component.
   */ 
  private JTree  pTree;

  /**
   * The fully resolved names of the selected nodes.
   */ 
  private TreeSet<String>  pSelected;

  /**
   * Whether to show node components with the given states.
   */ 
  private TreeMap<NodeTreeComp.State,Boolean>  pFilter;

  /**
   * Whether the selection was modified since the first SHIFT or CTRL key down event.
   */ 
  private boolean  pSelectionModified;  
  
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * The panel popup menu.
   */ 
  private JPopupMenu  pPanelPopup; 

  /**
   * The dynamic working area submenu.
   */ 
  private JMenu  pViewsContainingMenu;
  private JMenu  pViewsEditingMenu;

  /**
   * The panel popup menu items.
   */ 
  private JMenuItem  pRegisterItem;
  private JMenuItem  pCloneItem;
  private JMenuItem  pShowHideItem;
  private JMenuItem  pExpandSelectedItem; 

  private TreeMap<NodeFilter,JRadioButtonMenuItem>  pFilterItems; 
  private ButtonGroup pFilterGroup;

  private JCheckBoxMenuItem pShowHiddenItem; 

  /**
   * The tool plugin menu.
   */
  private JMenu  pToolMenu;
  private JMenu  pDefaultToolMenu;

  /**
   * The register node dialog.
   */ 
  private JRegisterDialog  pRegisterDialog;

  /**
   * The clone node dialog.
   */ 
  private JCloneDialog  pCloneDialog;

  /**
   * The NodeTreeComp of the tree element right clicked over.
   */
  private NodeTreeComp  pPrimaryNodeComp;

  /**
   * The node path of the tree element right clicked over.
   */
  private String  pPrimaryNodePath;

  /**
   * The toolset used to build the tool menu.
   */ 
  private String  pToolMenuToolset;

  /**
   * Whether the default toolset menu needs to be rebuilt.
   */ 
  private boolean  pRefreshDefaultToolMenu;

  /**
   * Table of toolsets indexed by node name.  Cleared during updates.
   */
  private TreeMap<String,String>  pToolsetCache;

  /**
   * Whether to display the primary sequence suffix.
   */
  private boolean  pDisplayPrimarySuffix;

}
