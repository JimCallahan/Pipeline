// $Id: JNodeBrowserPanel.java,v 1.15 2004/05/19 19:07:08 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

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
  implements TreeExpansionListener, MouseListener
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
    pSelected = new TreeSet<String>();

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
      }

      {
	JScrollPane scroll = new JScrollPane(pTree);
	
	scroll.setMinimumSize(new Dimension(230, 120));
	scroll.setPreferredSize(new Dimension(200, 500));
	
	scroll.setHorizontalScrollBarPolicy
	  (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	scroll.setVerticalScrollBarPolicy
	  (ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

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

    if(pGroupID > 0)
      master.releaseNodeBrowserGroup(pGroupID);

    pGroupID = 0;
    if((groupID > 0) && master.isNodeBrowserGroupUnused(groupID)) {
      master.assignNodeBrowserGroup(this, groupID);
      pGroupID = groupID;
    }
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
    UIMaster master = UIMaster.getInstance();
    return master.isNodeBrowserGroupUnused(groupID);
  }


  /*----------------------------------------------------------------------------------------*/

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

    if(pTree != null) {
      updateNodeTree();

      UIMaster master = UIMaster.getInstance();
      if(pGroupID > 0) {
	JNodeViewerPanel viewer = master.getNodeViewer(pGroupID);
	if(viewer != null) {
	  viewer.setRoots(pAuthor, pView, pSelected);
	  viewer.updateManagerTitlePanel();
	}
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the fully resolved names of the nodes associated with the current tree selection.
   */ 
  public TreeSet<String>
  getSelected() 
  {
    return pSelected;
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
   * Clear the current tree node selection. <P> 
   * 
   * Does not notify any associated node viewer of the change.
   */ 
  public void 
  clearSelection() 
  {
    pSelected.clear();
  }

  /**
   * Replace the current tree node selection with the given set of nodes. <P> 
   * 
   * Does not notify any associated node viewer of the change.
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

    updateNodeTree(getExpandedPaths());
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the node tree componenents based on the currently expanded paths.
   */ 
  private void 
  updateNodeTree()
  {
    updateNodeTree(getExpandedPaths());
  }

  /**
   * Update the node tree componenents based on the given set of expanded paths. <P> 
   * 
   * @param expanded
   *   The paths of the expanded tree nodes.
   */ 
  private void 
  updateNodeTree
  (
   TreeSet<String> expanded
  )
  {
    if(pTree == null) 
      return;

    UIMaster master = UIMaster.getInstance();

    /* get the updated node tree */ 
    NodeTreeComp comp = null;
    {
      if(!master.beginPanelOp()) 
	return;
      try { 
	TreeSet<String> paths = new TreeSet<String>(expanded);
	paths.addAll(pSelected);

	comp = master.getNodeMgrClient().updatePaths(pAuthor, pView, paths); 
      }
      catch(PipelineException ex) {
	master.showErrorDialog(ex);
	return;
      }
      finally {
	master.endPanelOp();
      }
    }

    /* rebuild the tree model based on the updated node tree */ 
    {
      DefaultMutableTreeNode root = new DefaultMutableTreeNode(new NodeTreeComp());
      rebuildTreeModel("", comp, root);
      
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
    StringBuffer buf = new StringBuffer();
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
   DefaultMutableTreeNode tnode
  )
  { 
    for(NodeTreeComp comp : cnode.values()) {
      String cpath = (path + "/" + comp);
      switch(comp.getState()) {
      case OtherPending:
      case OtherWorking:
	pSelected.remove(cpath);
      }

      DefaultMutableTreeNode child = 
	new DefaultMutableTreeNode(comp, (comp.getState() == NodeTreeComp.State.Branch));
      tnode.add(child);

      rebuildTreeModel(cpath, comp, child);
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
 	if(!comp.isEmpty()) 
 	  pTree.expandPath(new TreePath(tnode.getPath()));
      }
    }    
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
    TreePath tpath = pTree.getPathForLocation(e.getX(), e.getY());
    if(tpath != null) {
      DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) tpath.getLastPathComponent();
      NodeTreeComp comp = (NodeTreeComp) tnode.getUserObject();

      switch(comp.getState()) {
      case Pending:
      case Working:
      case CheckedIn: 
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
	  }
	  
	  /* BUTTON1+SHIFT: toggle selection */ 
	  else if((mods & (on2 | off2)) == on2) {
	    if(pSelected.contains(sname))
	      pSelected.remove(sname);
	    else 
	      pSelected.add(sname);
	    repaint();
	  }
	    
	  /* BUTTON1+SHIFT+CTRL: add to the selection */ 
	  else if((mods & (on3 | off3)) == on3) {
	    pSelected.add(sname);
	    repaint();
	  }

	  /* update any associated node viewer */ 
	  {
	    UIMaster master = UIMaster.getInstance();
	    
	    if(pGroupID > 0) {
	      JNodeViewerPanel viewer = master.getNodeViewer(pGroupID);
	      if(viewer != null) {
		viewer.setRoots(pAuthor, pView, pSelected);
		viewer.updateManagerTitlePanel();
		return;
	      }
	    }
	    
	    clearSelection();
	    repaint();

	    Toolkit.getDefaultToolkit().beep();
	  }
	}
      }
    }
  }

  /**
   * Invoked when a mouse button has been released on a component. 
   */ 
  public void 
  mouseReleased(MouseEvent e) {}



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

    if(!pSelected.isEmpty()) 
      encoder.encode("Selected", pSelected);
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    TreeSet<String> selected = (TreeSet<String>) decoder.decode("Selected");
    pSelected.clear();
    if(selected != null) 
      pSelected.addAll(selected);

    TreeSet<String> expanded = (TreeSet<String>) decoder.decode("ExpandedPaths");
    if(expanded != null) 
      updateNodeTree(expanded);
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
}
