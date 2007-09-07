// $Id: JNodeBrowserPanel.java,v 1.19 2007/09/07 18:52:38 jim Exp $

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
 * The directory-like browser of the node name heirarchy used to select nodes for display
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
      pSelected = new TreeSet<String>();

      pFilter = new TreeMap<NodeTreeComp.State, Boolean>();
      pFilter.put(NodeTreeComp.State.WorkingCurrentCheckedInSome, true);
      pFilter.put(NodeTreeComp.State.WorkingOtherCheckedInSome,   true);
      pFilter.put(NodeTreeComp.State.WorkingNoneCheckedInSome,    true);
      pFilter.put(NodeTreeComp.State.WorkingCurrentCheckedInNone, true);
      pFilter.put(NodeTreeComp.State.WorkingOtherCheckedInNone,   true);
    }

    /* panel popup menu */ 
    {
      JMenuItem item;
      
      pPanelPopup = new JPopupMenu(); 
      
      pViewsContainingMenu = new JMenu("Views Containing");
      pPanelPopup.add(pViewsContainingMenu);

      pViewsEditingMenu = new JMenu("Views Editing");
      pPanelPopup.add(pViewsEditingMenu);

      pPanelPopup.addSeparator();

      item = new JMenuItem("Node Filter...");
      pNodeFilterItem = item;
      item.setActionCommand("node-filter");
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
  public boolean
  isLocked() 
  {
    return (super.isLocked() && !pPrivilegeDetails.isNodeManaged(pAuthor));
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

    PanelUpdater pu = new PanelUpdater(this, forceUpdate);
    pu.execute();
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

    updateSelection(selected);
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
  
  /**
   * Update the node filter.
   * 
   * @param filter
   *   Whether to show node components with the given states.
   */ 
  public void 
  updateFilter
  (
   TreeMap<NodeTreeComp.State, Boolean> filter
  ) 
  {
    pFilter.putAll(filter);
    updateNodeTree();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the node tree componenents based on the currently expanded paths.
   */ 
  private void 
  updateNodeTree()
  {
    updateNodeTree(getExpandedPaths(), null);
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
    updateNodeTree(getExpandedPaths(), deep);
  }

  /**
   * Update the node tree componenents based on the given set of expanded paths. <P> 
   * 
   * @param expanded
   *   The paths of the expanded tree nodes.
   * 
   * @param deep
   *   If not <CODE>null</CODE>, update and expand all node paths under this path.
   */ 
  private void 
  updateNodeTree
  (
   TreeSet<String> expanded, 
   String deep
  )
  {
    if(pTree == null) 
      return;

    /* get the updated node tree */ 
    NodeTreeComp comp = null;
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginSilentPanelOp(pGroupID)) {

	updatePrivileges();
    
	try { 
	  TreeMap<String,Boolean> paths = new TreeMap<String,Boolean>();
	  for(String path : expanded)
	    paths.put(path, (deep != null) && path.equals(deep));
	  for(String path : pSelected) 
	    paths.put(path, false);
	  
	  comp = master.getMasterMgrClient(pGroupID).updatePaths(pAuthor, pView, paths); 
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endSilentPanelOp(pGroupID);
	}
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

	  if((expanded.contains(cpath) || (deep != null) && cpath.startsWith(deep)) &&
	     child.isLeaf() && (comp.getState() == NodeTreeComp.State.Branch)) {
	    DefaultMutableTreeNode hidden = new DefaultMutableTreeNode(null, false);
	    child.add(hidden);
	  }
	}
	break;

      default:
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
 	NodeTreeComp comp = (NodeTreeComp) tnode.getUserObject();
 	if((comp != null) && !comp.isEmpty()) 
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
  public void 
  updateUserPrefs() 
  {
    TextureMgr.getInstance().rebuildIcons();

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
      (pNodeFilterItem, prefs.getNodeBrowserNodeFilter(), 
       "Show the node filter dialog."); 
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- TREE EXPANSION LISTENER METHODS -----------------------------------------------------*/

  /**
   *Called whenever an item in the tree has been collapsed.
   */ 
  public void
  treeCollapsed
  (
   TreeExpansionEvent e
  )
  {}

  /**
   * Called whenever an item in the tree has been expanded.
   */ 
  public void 	
  treeExpanded
  (
   TreeExpansionEvent e
  )
  {
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
	    }
	  }
	  
	  UIMaster master = UIMaster.getInstance();
	  master.rebuildWorkingAreaContainingMenu
	    (pGroupID, sname, pViewsContainingMenu, this);
	  master.rebuildWorkingAreaEditingMenu
	    (pGroupID, sname, pViewsEditingMenu, this);

	  pPanelPopup.show(e.getComponent(), e.getX(), e.getY());
	}
	else {
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
    
    else if((prefs.getNodeBrowserNodeFilter() != null) &&
       prefs.getNodeBrowserNodeFilter().wasPressed(e)) 
      doNodeFilter();

    else {
      switch(e.getKeyCode()) {
      case KeyEvent.VK_SHIFT:
      case KeyEvent.VK_CONTROL:
      case KeyEvent.VK_ALT:
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
    if(cmd.equals("node-filter"))
      doNodeFilter();
    else if(cmd.startsWith("author-view:")) 
      doChangeAuthorView(cmd.substring(12));    
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Modify the node filter. 
   */ 
  public void 
  doNodeFilter() 
  {
    if(pFilterDialog == null) 
      pFilterDialog = new JNodeBrowserFilterDialog(this);

    pFilterDialog.updateFilter(pFilter);
    pFilterDialog.setVisible(true);
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

    String author = parts[0];
    String view   = parts[1];
    if(!pAuthor.equals(author) || !pView.equals(view)) 
      setAuthorView(author, view);       
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
    super.toGlue(encoder);
    
    TreeSet<String> expanded = getExpandedPaths();
    if(!expanded.isEmpty()) 
      encoder.encode("ExpandedPaths", expanded);

    encoder.encode("Filter", pFilter);
  }

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

    TreeMap<NodeTreeComp.State, Boolean> filter = 
      (TreeMap<NodeTreeComp.State, Boolean>) decoder.decode("Filter");
    if(filter != null) 
      pFilter.putAll(filter);
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
  private JTree pTree;

  /**
   * The fully resolved names of the selected nodes.
   */ 
  private TreeSet<String> pSelected;

  /**
   * Whether to show node components with the given states.
   */ 
  private TreeMap<NodeTreeComp.State, Boolean>  pFilter;

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
   * The panel layout popup menu items.
   */ 
  private JMenuItem  pNodeFilterItem;


  /**
   * The editor dialog for node filters.
   */ 
  private JNodeBrowserFilterDialog  pFilterDialog; 


}
