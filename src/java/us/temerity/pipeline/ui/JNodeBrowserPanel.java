// $Id: JNodeBrowserPanel.java,v 1.11 2004/05/07 21:12:32 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

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
    /* initialize the panel components */ 
    {
      setLayout(new BorderLayout());

      {
	DefaultMutableTreeNode root = new DefaultMutableTreeNode("root", true);
	DefaultTreeModel model = new DefaultTreeModel(root, true);

	JTree tree = new JTree(model); 
	pTree = tree;

	tree.setShowsRootHandles(true);
	tree.setRootVisible(false);
	tree.setCellRenderer(new JTreeCellRenderer());
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
    updateNodeTree();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the node tree componenents.
   */ 
  private void 
  updateNodeTree()
  {
    if(pTree == null) 
      return;

    UIMaster master = UIMaster.getInstance();
 
    /* the paths of the currently expanded branch nodes */ 
    TreeSet<String> paths = new TreeSet<String>();
    {
      DefaultMutableTreeNode root = (DefaultMutableTreeNode) pTree.getModel().getRoot();
      Enumeration e = pTree.getExpandedDescendants(new TreePath(root.getPath()));
      if(e != null) {
	while(e.hasMoreElements()) {
	  TreePath tpath = (TreePath) e.nextElement(); 
	  paths.add(treePathToNodeName(tpath));
	}
      }
      
      if(paths.isEmpty()) 
	paths.add("/");
    }

    /* get the updated node tree */ 
    NodeTreeComp comp = null;
    {
      if(!master.beginPanelOp()) 
	return;
      try { 
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
      rebuildTreeModel(comp, root);
      
      DefaultTreeModel model = (DefaultTreeModel) pTree.getModel();
      model.setRoot(root);
      
      pTree.removeTreeExpansionListener(this);
      rexpandPaths(root);
      pTree.addTreeExpansionListener(this);
    }
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
   NodeTreeComp cnode, 
   DefaultMutableTreeNode tnode
  )
  { 
    for(NodeTreeComp comp : cnode.values()) {
      DefaultMutableTreeNode child = 
	new DefaultMutableTreeNode(comp, (comp.getState() == NodeTreeComp.State.Branch));
      tnode.add(child);

      rebuildTreeModel(comp, child);
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
    TreePath tpath = pTree.getPathForLocation(e.getX(), e.getY());
    if(tpath != null) {
      DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) tpath.getLastPathComponent();
      NodeTreeComp comp = (NodeTreeComp) tnode.getUserObject();

      switch(comp.getState()) {
      case Pending:
      case Working:
      case CheckedIn: 
	{
	  UIMaster master = UIMaster.getInstance();
	    
	  if(pGroupID == 0) {
	    Toolkit.getDefaultToolkit().beep();
	    return;
	  }

	  JNodeViewerPanel viewer = master.getNodeViewer(pGroupID);
	  if(viewer == null) {
	    Toolkit.getDefaultToolkit().beep();
	    return;
	  }

	  System.out.print("Selected Node: " + comp.getState() + 
			   "  (" + e.getClickCount() + " clicks)\n" + 
			   "  Author = " + pAuthor + "\n" +
			   "    View = " + pView + "\n" + 
			   "    Name = " + treePathToNodeName(tpath) + "\n");
	  
	  viewer.setFocus(pAuthor, pView, treePathToNodeName(tpath));
	  viewer.updateManagerTitlePanel();
	}
      }
    }
  }

  public void 
  mouseReleased(MouseEvent e) {}



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


}
