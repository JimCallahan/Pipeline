// $Id: JNodeViewerPanel.java,v 1.15 2004/05/23 19:59:01 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.glue.*; // DEBUG

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.vecmath.*;
import javax.media.j3d.*;
import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.geometry.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   V I E W E R   P A N E L                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The viewer of {@link NodeStatus NodeStatus} trees as graphs of state icons connected by
 * lines showing the upstream/downstream connectivity between nodes.
 */ 
public  
class JNodeViewerPanel
  extends JTopLevelPanel
  implements ComponentListener, MouseListener, MouseMotionListener, KeyListener, 
             PopupMenuListener, ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new panel with the default working area view.
   */
  public 
  JNodeViewerPanel()
  {
    super();
    initUI();
  }

  /**
   * Construct a new panel with a working area view identical to the given panel.
   */
  public 
  JNodeViewerPanel
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
  private synchronized void 
  initUI()
  {  
    /* initialize fields */ 
    {
      pRoots = new TreeMap<String,NodeStatus>();

      pShowDownstream = true;
      pLayoutPolicy   = LayoutPolicy.AutomaticExpand;

      pSelected = new HashMap<NodePath,ViewerNode>();

      pRemoveSecondarySeqs = new TreeMap<String,FileSeq>();
    }
  
    /* panel popup menu */ 
    {
      JMenuItem item;
      
      pPanelPopup = new JPopupMenu();  
      pPanelPopup.addPopupMenuListener(this);

      item = new JMenuItem("Center");
      item.setActionCommand("center");
      item.addActionListener(this);
      pPanelPopup.add(item);  
      
      item = new JMenuItem("Frame Selection");
      item.setActionCommand("frame-selection");
      item.addActionListener(this);
      pPanelPopup.add(item);  
      
      item = new JMenuItem("Frame All");
      item.setActionCommand("frame-all");
      item.addActionListener(this);
      pPanelPopup.add(item);  
      
      pPanelPopup.addSeparator();
       
      item = new JMenuItem("Automatic Expand");
      item.setActionCommand("automatic-expand");
      item.addActionListener(this);
      pPanelPopup.add(item);  

      item = new JMenuItem("Expand All");
      item.setActionCommand("expand-all");
      item.addActionListener(this);
      pPanelPopup.add(item);  

      item = new JMenuItem("Collapse All");
      item.setActionCommand("collapse-all");
      item.addActionListener(this);
      pPanelPopup.add(item);  

      pPanelPopup.addSeparator();

      item = new JMenuItem();
      pShowHideDownstreamItem = item;
      item.setActionCommand("show-hide-downstream");
      item.addActionListener(this);
      pPanelPopup.add(item);  
    }

    /* node popup menu */ 
    {
      JMenuItem item;
      JMenu sub;
      
      pNodePopup = new JPopupMenu();  
      pNodePopup.addPopupMenuListener(this);
       
      item = new JMenuItem("Make Root");
      item.setActionCommand("make-root");
      item.addActionListener(this);
      pNodePopup.add(item);  

      item = new JMenuItem("Add Root");
      item.setActionCommand("add-root");
      item.addActionListener(this);
      pNodePopup.add(item);  

      item = new JMenuItem("Replace Root");
      item.setActionCommand("replace-root");
      item.addActionListener(this);
      pNodePopup.add(item);  

      item = new JMenuItem("Remove Root");
      item.setActionCommand("remove-root");
      item.addActionListener(this);
      pNodePopup.add(item);  

      item = new JMenuItem("Remove All Roots");
      item.setActionCommand("remove-all-roots");
      item.addActionListener(this);
      pNodePopup.add(item);  
      
      pNodePopup.addSeparator();

      item = new JMenuItem("Edit");
      item.setActionCommand("edit");
      item.addActionListener(this);
      pNodePopup.add(item);

      {
	sub = new JMenu("Edit With");
	pNodePopup.add(sub);
	
	for(String editor : Plugins.getEditorNames()) {
	  item = new JMenuItem(editor);
	  item.setActionCommand("edit-with:" + editor);
	  item.addActionListener(this);
	  sub.add(item);
	}
      }
      
      pNodePopup.addSeparator();

      sub = new JMenu("Link");
      pLinkMenu = sub;
      sub.setEnabled(false);
      pNodePopup.add(sub);

      item = new JMenuItem("Unlink");
      item.setActionCommand("unlink");
      item.addActionListener(this);
      pNodePopup.add(item);
      
      pNodePopup.addSeparator();
      
      item = new JMenuItem("Add Secondary...");
      item.setActionCommand("add-secondary");
      item.addActionListener(this);
      pNodePopup.add(item);
      
      sub = new JMenu("Remove Secondary");
      pRemoveSecondaryMenu = sub;
      sub.setEnabled(false);
      pNodePopup.add(sub);

      pNodePopup.addSeparator();
      
      item = new JMenuItem("Make");
      item.setActionCommand("make");
      item.addActionListener(this);
      pNodePopup.add(item);
      
      item = new JMenuItem("Make Local");
      item.setActionCommand("make-local");
      item.addActionListener(this);
      pNodePopup.add(item);

      pNodePopup.addSeparator();
      
      item = new JMenuItem("Rename...");
      item.setActionCommand("rename");
      item.addActionListener(this);
      pNodePopup.add(item);

      item = new JMenuItem("Import...");
      item.setActionCommand("import");
      item.addActionListener(this);
      pNodePopup.add(item);

      item = new JMenuItem("Clone...");
      item.setActionCommand("clone");
      item.addActionListener(this);
      pNodePopup.add(item);

      pNodePopup.addSeparator();
      
      item = new JMenuItem("Check-In...");
      item.setActionCommand("check-in");
      item.addActionListener(this);
      pNodePopup.add(item);
  
      item = new JMenuItem("Check-Out...");
      item.setActionCommand("check-out");
      item.addActionListener(this);
      pNodePopup.add(item);
      
      pNodePopup.addSeparator();

      item = new JMenuItem("Release");
      item.setActionCommand("release");
      item.addActionListener(this);
      pNodePopup.add(item);
      
      item = new JMenuItem("Revoke");
      item.setActionCommand("revoke");
      item.addActionListener(this);
      pNodePopup.add(item);

      item = new JMenuItem("Destroy");
      item.setActionCommand("destroy");
      item.addActionListener(this);
      pNodePopup.add(item);
    }

    /* link popup menu */ 
    {
      JMenuItem item;
      JMenu sub;
      
      pLinkPopup = new JPopupMenu();  
      pLinkPopup.addPopupMenuListener(this);
       
      sub = new JMenu("Link Catagory");
      pLinkCatagoryMenu = sub;
      sub.setEnabled(false);
      pLinkPopup.add(sub);

      {
	sub = new JMenu("Link Relationship");
	pLinkPopup.add(sub);

	item = new JMenuItem("None");
	pLinkNoneRelationshipItem = item;
	item.setActionCommand("link-relationship:None");
	item.addActionListener(this);
	sub.add(item);

	item = new JMenuItem("One-to-One");
	pLinkOneToOneRelationshipItem = item;
	item.setActionCommand("link-relationship:OneToOne");
	item.addActionListener(this);
	sub.add(item);

	item = new JMenuItem("All");
	pLinkAllRelationshipItem = item;
	item.setActionCommand("link-relationship:All");
	item.addActionListener(this);
	sub.add(item);
      }

      item = new JMenuItem("Unlink");
      item.setActionCommand("link-unlink");
      item.addActionListener(this);
      pLinkPopup.add(item);
    }

    /* initialize the panel components */ 
    {
      setLayout(new BorderLayout());
      setMinimumSize(new Dimension(50, 50));
      setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
      
      /* canvas */ 
      {
	pCanvas = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
	pCanvas.addComponentListener(this); 
	pCanvas.addMouseListener(this); 
	pCanvas.addMouseMotionListener(this); 
	pCanvas.setFocusable(true);
	pCanvas.addKeyListener(this);
	
	add(pCanvas);
      }

      /* grey backgound */ 
      BranchGroup background = new BranchGroup();
      {
	Point3d origin = new Point3d(0, 0, 0);
	BoundingSphere bounds = new BoundingSphere(origin, Double.POSITIVE_INFINITY);
	
 	Background bg = new Background(0.5f, 0.5f, 0.5f);
	bg.setApplicationBounds(bounds);
	
	background.addChild(bg);
	background.compile();
      }
      
      /* the universe */ 
      {
	pUniverse = new SimpleUniverse(pCanvas);
	pUniverse.addBranchGraph(background);
      }
      
      /* initialialize camera position */ 
      {
	Viewer viewer = pUniverse.getViewer();
	TransformGroup tg = viewer.getViewingPlatform().getViewPlatformTransform();
	
	Transform3D xform = new Transform3D();
	xform.setTranslation(new Vector3d(0.0, 0.0, 10.0));
	tg.setTransform(xform);
	
	View view = viewer.getView();
	view.setFrontClipDistance(0.5);
	view.setBackClipDistance(1000.0); 
      }
      
      /* zoomer-paner */ 
      {
	Point3d origin = new Point3d(0, 0, 0);
	BoundingSphere bounds = new BoundingSphere(origin, Double.POSITIVE_INFINITY);
	
	ZoomPanBehavior zp = new ZoomPanBehavior(pUniverse.getViewer());
	zp.setSchedulingBounds(bounds);
	
	BranchGroup branch = new BranchGroup();
	branch.addChild(zp);
	
	pUniverse.addBranchGraph(branch);
      }

      /* rubber band geometry */ 
      {
	pRubberBand = new RubberBand();
	pUniverse.addBranchGraph(pRubberBand.getBranchGroup());
      }

      /* the node/link geometry */ 
      {
	pGeomBranch = new BranchGroup();
	
	/* the node pool */ 
	{
	  pNodePool = new ViewerNodePool();
	  pGeomBranch.addChild(pNodePool.getBranchGroup());
	}
	
	/* the node links */ 
	{
	  pLinks = new ViewerLinks();
	  pGeomBranch.addChild(pLinks.getBranchGroup());
	}

	pUniverse.addBranchGraph(pGeomBranch);
      }				 
    }
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
      master.releaseNodeViewerGroup(pGroupID);

    pGroupID = 0;
    if((groupID > 0) && master.isNodeViewerGroupUnused(groupID)) {
      master.assignNodeViewerGroup(this, groupID);
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
    return master.isNodeViewerGroupUnused(groupID);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the author and view.
   */ 
  public synchronized void 
  setAuthorView
  (
   String author, 
   String view 
  ) 
  {
    super.setAuthorView(author, view);    

    if(pRoots != null) {
      TreeSet<String> roots = new TreeSet<String>(pRoots.keySet());
      pRoots.clear();      
      setRoots(roots);
    }
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Set the root nodes displayed by the viewer. <P> 
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param names
   *   The fully resolved names of the root nodes.
   */
  public synchronized void 
  setRoots
  (
   String author, 
   String view,
   TreeSet<String> names
  )
  {
    for(ViewerNode vnode : clearSelection()) 
      vnode.update();

    if(!pAuthor.equals(author) || !pView.equals(view)) {
      super.setAuthorView(author, view);
      pRoots.clear();   
    }
    
    setRoots(names);
  }

  /**
   * Set the root nodes displayed by the viewer. <P> 
   * 
   * @param names
   *   The fully resolved names of the root nodes.
   */
  public synchronized void 
  setRoots
  (
   TreeSet<String> names
  )
  {
    for(ViewerNode vnode : clearSelection()) 
      vnode.update();

    /* are there any new nodes being added? */ 
    boolean refresh = false;
    for(String name : names) {
      if(!pRoots.containsKey(name)) {
	refresh = true;
	break;
      }
    }
    
    /* complete update */ 
    if(refresh) {
      pRoots.clear();
      for(String name : names) 
	pRoots.put(name, null);
    }
    
    /* filter out unselected nodes */ 
    else {
      TreeMap<String,NodeStatus> roots = new TreeMap<String,NodeStatus>();
      for(String name : pRoots.keySet()) {
	if(names.contains(name))
	  roots.put(name, pRoots.get(name));
      }
      pRoots = roots;
    } 
    
    updateNodeBrowserSelection();
    updateNodeStatus(); 
  }


  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the panel to reflect new user preferences.
   */ 
  public void 
  updateUserPrefs() 
  {
    updateUniverse();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the panel menu.
   */ 
  public void 
  updatePanelMenu() 
  {
    pShowHideDownstreamItem.setText((pShowDownstream ? "Hide" : "Show") + " Downstream");
  }

  /**
   * Update the node menu.
   */ 
  public void 
  updateNodeMenu() 
  {
    UIMaster master = UIMaster.getInstance(); 
    try {
      JMenuItem item;

      /* clear existing items */ 
      {
	pLinkMenu.removeAll();
	pLinkMenu.setEnabled(false);
	
	pRemoveSecondaryMenu.setEnabled(false);
	pRemoveSecondaryMenu.removeAll();
	pRemoveSecondarySeqs.clear();
      }

      /* rebuild items */ 
      {
	for(String catagory : master.getMasterMgrClient().getLinkCatagories().keySet()) {
	  item = new JMenuItem(catagory);
	  item.setActionCommand("link:" + catagory);
	  item.addActionListener(this);
	  pLinkMenu.add(item);
	}
	pLinkMenu.setEnabled(pLinkMenu.getItemCount() > 0);
	
	if(pPrimary != null) {
	  NodeDetails details = pPrimary.getNodeStatus().getDetails();
	  if(details != null) {
	    NodeMod mod = details.getWorkingVersion();
	    if(mod != null) {
	      for(FileSeq fseq : mod.getSecondarySequences()) {
		String fname = fseq.toString();

		item = new JMenuItem(fname);
		item.setActionCommand("remove-secondary:" + fname);
		item.addActionListener(this);
		pRemoveSecondaryMenu.add(item);
	      
		pRemoveSecondarySeqs.put(fname, fseq);
	      }
	    }
	  }
	}
	pRemoveSecondaryMenu.setEnabled(pRemoveSecondaryMenu.getItemCount() > 0);
      }
    }
    catch(PipelineException ex) {
      master.showErrorDialog(ex);
    }
  }

  /**
   * Update the link menu.
   */ 
  public void 
  updateLinkMenu() 
  {
    UIMaster master = UIMaster.getInstance(); 
    try {
      pLinkCatagoryMenu.removeAll();
      pLinkCatagoryMenu.setEnabled(false);
    
      for(String catagory : master.getMasterMgrClient().getLinkCatagories().keySet()) {
	JMenuItem item = new JMenuItem(catagory);
	item.setActionCommand("link-catagory:" + catagory);
	item.addActionListener(this);
	item.setEnabled(!pSelectedLink.getCatagory().getName().equals(catagory));

	pLinkCatagoryMenu.add(item);
      }
      pLinkCatagoryMenu.setEnabled(pLinkCatagoryMenu.getItemCount() > 0);
    }
    catch(PipelineException ex) {
      master.showErrorDialog(ex);
    }
    
    {
      LinkRelationship relationship = pSelectedLink.getRelationship();
      pLinkNoneRelationshipItem.setEnabled(relationship != LinkRelationship.None);
      pLinkOneToOneRelationshipItem.setEnabled(relationship != LinkRelationship.OneToOne);
      pLinkAllRelationshipItem.setEnabled(relationship != LinkRelationship.All);
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the selection in the node browser to be consistent with the root nodes
   * in the node viewer if they share the same group, author and view.
   */ 
  private synchronized void 
  updateNodeBrowserSelection() 
  {
    /* update the associated node viewer */ 
    UIMaster master = UIMaster.getInstance();
    if(pGroupID > 0) {
      JNodeBrowserPanel browser = master.getNodeBrowser(pGroupID);
      if(browser != null) {
	if(browser.getAuthor().equals(pAuthor) && browser.getView().equals(pView)) {
	  TreeSet<String> roots = new TreeSet<String>(pRoots.keySet());
	  browser.updateSelection(roots);
	}
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the nodes being viewed.
   */ 
  private void 
  updateNodeStatus()
  {
    if(pNodePool == null) 
      return;
    
    StatusTask task = new StatusTask(this);
    task.start();
  }

  /**
   * Update the visualization graphics.
   */
  private synchronized void 
  updateUniverse()
  {  
    pLinks.updatePrep();
    pNodePool.updatePrep();

    if(!pRoots.isEmpty()) {
      double anchorHeight = 0.0;
      for(String name : pRoots.keySet()) {
	NodeStatus status = pRoots.get(name);
	
	assert(status != null) : ("Missing Status for: " + name);
	
	NodePath path = new NodePath(name);
	Point2d anchor = new Point2d(0.0, anchorHeight);
	
	/* layout the upstream nodes */ 
	double uheight = 0.0;
	{
	  TreeSet<String> seen = new TreeSet<String>();
	  uheight = layoutNodes(true, true, status, path, anchor, seen);
	}
	
	/* layout the downstream nodes */ 
	double dheight = 0.0;  
	if(pShowDownstream) {
	  TreeSet<String> seen = new TreeSet<String>();
	  dheight = layoutNodes(true, false, status, path, anchor, seen);
	}
	
	/* shift the upstream/downstream nodes so that they line up vertically */ 
	if(uheight > dheight) {
	  shiftUpstreamNodes(true, status, path, 
			     anchorHeight + dheight*0.5, (dheight - uheight)*0.5);
	}
	else {
	  shiftUpstreamNodes(true, status, path, anchorHeight + uheight*0.5, 0.0);
	  if(pShowDownstream) 
	    shiftDownstreamNodes(true, status, path, (uheight - dheight)*0.5);
	}
	
	anchorHeight += Math.min(uheight, dheight);
      }
      
      /* preserve the current layout */ 
      pLayoutPolicy = LayoutPolicy.Preserve;
    }
  
    pLinks.update();
    pNodePool.update();
  }
  
  /**
   * Recursively layout the nodes.
   * 
   * @param isRoot
   *   Is this the root node?
   * 
   * @param upstream
   *   Whether to traverse the nodes in an upstream direction (or downstream).
   * 
   * @param status
   *   The status of the current node. 
   * 
   * @param path
   *   The path from the root node to the current node.
   * 
   * @param anchor
   *   The upper-left corner of the layout area for the current node.
   * 
   * @param seen
   *   The fully resolved node names of processed nodes.
   * 
   * @return 
   *   The height of the layout area of the viewer node including its children.
   */ 
  private double
  layoutNodes
  (
   boolean isRoot, 
   boolean upstream,
   NodeStatus status, 
   NodePath path, 
   Point2d anchor, 
   TreeSet<String> seen
  ) 
  {
    UserPrefs prefs = UserPrefs.getInstance();

    ViewerNode vnode = null;
    if(!isRoot || upstream) 
      vnode = pNodePool.lookupOrCreateViewerNode(status, path);
    else 
      vnode = pNodePool.getActiveViewerNode(path);
    assert(vnode != null);

    if((upstream && status.hasSources()) || 
       (!upstream && status.hasTargets() && !isRoot)) {
      switch(pLayoutPolicy) {
      case Preserve:
	if(!vnode.isReset()) 
	  break;
	
      case AutomaticExpand:
	vnode.setCollapsed(seen.contains(status.getName()));
	break;
	
      case ExpandAll:
	vnode.setCollapsed(false);
	break;
	
      case CollapseAll:
	vnode.setCollapsed(true);
      }
    }
    else if(!isRoot || upstream) {
      vnode.setCollapsed(false);
    }

    seen.add(status.getName());

    double height = 0.0;
    if(vnode.isCollapsed() && !(isRoot && !upstream)) {
      height = -prefs.getNodeSpaceY();
    }
    else {      
      Collection<NodeStatus> children = null;
      if(upstream && status.hasSources())
	children = status.getSources();
      else if(!upstream && status.hasTargets()) 
	children = status.getTargets();

      if(children != null) {
	for(NodeStatus cstatus : children) {
	  NodePath cpath = new NodePath(path, cstatus.getName());
	  
	  /* layout the child node */ 
	  {
	    double sign = upstream ? 1.0 : -1.0;
	    Point2d canchor = new Point2d(anchor.x + sign*prefs.getNodeSpaceX(), 
					  anchor.y + height);
	    
	    height += layoutNodes(false, upstream, cstatus, cpath, canchor, seen);
	  }

	  /* add a link between this node and the child node */ 
	  {
	    ViewerNode cvnode = pNodePool.getActiveViewerNode(cpath);
	    if(upstream) {
	      LinkCommon link = null;
	      {
		NodeDetails details = status.getDetails();
		if(details.getWorkingVersion() != null) 
		  link = details.getWorkingVersion().getSource(cstatus.getName());
		else if(details.getLatestVersion() != null)
		link = details.getLatestVersion().getSource(cstatus.getName());
	      }
	      assert(link != null);	    
	      
	      pLinks.addUpstreamLink(vnode, cvnode, link);
	    }
	    else {
	      pLinks.addDownstreamLink(cvnode, vnode);
	    }				   
	  }      
	}
      }
      else {
	height = -prefs.getNodeSpaceY();
      }
    }

    if(!isRoot) {
      double vdist  = 0.5*height;
      double offset = ((path.getNumNodes() % 2) == 0) ? prefs.getNodeOffset() : 0.0;
      vnode.setPosition(new Point2d(anchor.x, anchor.y + vdist + offset*vdist));
    }

    return height;
  }
  
  /**
   * Recursively shift the upstream nodes by the given vertical offset.
   * 
   * @param isRoot
   *   Is this the root node?
   * 
   * @param status
   *   The status of the current node. 
   * 
   * @param path
   *   The path from the root node to the current node.
   * 
   * @param ry
   *   The vertical position of the root node.
   * 
   * @param offset
   *   The vertical distance to shift all nodes except the root node.
   */ 
  private void
  shiftUpstreamNodes
  (
   boolean isRoot, 
   NodeStatus status, 
   NodePath path, 
   double ry, 
   double offset
  ) 
  {
    ViewerNode vnode = pNodePool.getActiveViewerNode(path);
    if(vnode == null) 
      return;

    if(isRoot) {
      vnode.setPosition(new Point2d(0.0, ry));
    }
    else {
      Point2d pos = vnode.getPosition();
      vnode.setPosition(new Point2d(pos.x, pos.y+offset));
    }

    for(NodeStatus cstatus : status.getSources()) {
      NodePath cpath = new NodePath(path, cstatus.getName());
      shiftUpstreamNodes(false, cstatus, cpath, ry, offset);
    }
  }

  /**
   * Recursively shift the downstream nodes by the given vertical offset.
   * 
   * @param isRoot
   *   Is this the root node?
   * 
   * @param status
   *   The status of the current node. 
   * 
   * @param path
   *   The path from the root node to the current node.
   * 
   * @param offset
   *   The table containing sets of child paths indexed by parent path.
   */ 
  private void
  shiftDownstreamNodes
  (
   boolean isRoot, 
   NodeStatus status, 
   NodePath path, 
   double offset
  ) 
  {
    ViewerNode vnode = pNodePool.getActiveViewerNode(path);
    if(vnode == null) 
      return;

    if(!isRoot) {
      Point2d pos = vnode.getPosition();
      vnode.setPosition(new Point2d(pos.x, pos.y+offset));
    }

    for(NodeStatus cstatus : status.getTargets()) {
      NodePath cpath = new NodePath(path, cstatus.getName());
      shiftDownstreamNodes(false, cstatus, cpath, offset);
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S E L E C T I O N                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the fully resolved name of the primary node selection.
   * 
   * @return 
   *   The node name or <CODE>null</CODE> if there is no primary selection.
   */ 
  public String
  getPrimarySelectionName() 
  {
    if(pPrimary != null) 
      return pPrimary.getNodeStatus().getName();
    return null;
  }

  /**
   * Get the fully resolved name of root node of the primary node selection.
   * 
   * @return 
   *   The node name or <CODE>null</CODE> if there is no primary selection.
   */ 
  public String
  getPrimarySelectionRootName() 
  {
    if(pPrimary != null) 
      return pPrimary.getNodePath().getRootName();
    return null;
  }

  /**
   * Get the fully resolved names of all selected nodes.
   */ 
  public TreeSet<String>
  getSelectedNames() 
  {
    TreeSet<String> names = new TreeSet<String>();

    for(ViewerNode vnode : pSelected.values()) 
      names.add(vnode.getNodeStatus().getName());

    return names;
  }
  


  /*----------------------------------------------------------------------------------------*/

  /**
   * Clear the current selection.
   * 
   * @return 
   *   The previously selected nodes.
   */ 
  public ArrayList<ViewerNode>
  clearSelection()
  {
    ArrayList<ViewerNode> changed = new ArrayList<ViewerNode>(pSelected.values());

    for(ViewerNode vnode : pSelected.values()) 
      vnode.setSelectionMode(SelectionMode.Normal);

    pSelected.clear();
    pPrimary = null;
    
    pSelectedLink = null;

    return changed;
  }
  
  /**
   * Make the given viewer node the primary selection.
   * 
   * @return 
   *   The viewer nodes who's selection state changed.
   */ 
  public ArrayList<ViewerNode>
  primarySelect
  (
   ViewerNode vnode
  ) 
  {
    ArrayList<ViewerNode> changed = new ArrayList<ViewerNode>();

    switch(vnode.getSelectionMode()) {
    case Normal:
      pSelected.put(vnode.getNodePath(), vnode);
      
    case Selected:
      if(pPrimary != null) {
	pPrimary.setSelectionMode(SelectionMode.Selected);
	changed.add(pPrimary);
      }
      pPrimary = vnode;
      vnode.setSelectionMode(SelectionMode.Primary);
      changed.add(vnode);
    }

    return changed;
  }

  /**
   * Add the given viewer node to the selection.
   * 
   * @return 
   *   The viewer nodes who's selection state changed.
   */ 
  public ArrayList<ViewerNode>
  addSelect
  (
   ViewerNode vnode
  ) 
  {
    ArrayList<ViewerNode> changed = new ArrayList<ViewerNode>();

    switch(vnode.getSelectionMode()) {
    case Primary:
      if(pPrimary != null) {
	pPrimary.setSelectionMode(SelectionMode.Selected);
	changed.add(pPrimary);
	pPrimary = null;
      }

    case Normal:
      vnode.setSelectionMode(SelectionMode.Selected);
      pSelected.put(vnode.getNodePath(), vnode);
      changed.add(vnode);
    }

    return changed;
  }

  /**
   * Toggle the selection of the given viewer node.
   * 
   * @return 
   *   The viewer nodes who's selection state changed.
   */ 
  public  ArrayList<ViewerNode> 
  toggleSelect
  (
   ViewerNode vnode
  ) 
  {
    ArrayList<ViewerNode> changed = new ArrayList<ViewerNode>();

    switch(vnode.getSelectionMode()) {
    case Primary:
      if(pPrimary != null) {
	pPrimary.setSelectionMode(SelectionMode.Selected);
	changed.add(pPrimary);
	pPrimary = null;
      }

    case Selected:
      vnode.setSelectionMode(SelectionMode.Normal);
      pSelected.remove(vnode.getNodePath());
      break;

    case Normal:
      vnode.setSelectionMode(SelectionMode.Selected);
      pSelected.put(vnode.getNodePath(), vnode);      
    }
    
    changed.add(vnode);
    return changed;
  }


  /**
   * Get the user-data of the Java3D object under given mouse position. <P> 
   * 
   * @return
   *   The picked object or <CODE>null</CODE> if no pickable object was under position.
   */ 
  private Object
  objectAtMousePos
  (
   int x, 
   int y
  ) 
  {
    PickRay ray = null;
    {
      Point3d eyePos = new Point3d();
      Point3d pos    = new Point3d();

      pCanvas.getCenterEyeInImagePlate(eyePos);
      pCanvas.getPixelLocationInImagePlate(x, y, pos);
      
      Transform3D motion = new Transform3D();
      pCanvas.getImagePlateToVworld(motion);
      motion.transform(eyePos);
      motion.transform(pos);
      
      Vector3d dir = new Vector3d(pos);
      dir.sub(eyePos);
      
      ray = new PickRay(eyePos, dir);
    }
    
    SceneGraphPath gpath = pGeomBranch.pickClosest(ray);
    if(gpath != null) 
      return gpath.getObject().getUserData();
    return null;
  }

  /**
   * Get the user-data of the Java3D object under given mouse position. <P> 
   * 
   * @return
   *   The picked object or <CODE>null</CODE> if no pickable object was under position.
   */ 
  private Object
  objectAtMousePos
  (
   Point pos
  ) 
  {
    return objectAtMousePos(pos.x, pos.y);
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
   * Invoked when the component's size changes. 
   */ 
  public void 	
  componentResized
  (
   ComponentEvent e
  )
  {
    /* adjust the minimum zoom distance based on the canvas size */ 
    {
      Viewer viewer = pUniverse.getViewer();
      TransformGroup tg = viewer.getViewingPlatform().getViewPlatformTransform();
      
      Transform3D xform = new Transform3D();
      tg.getTransform(xform);
      
      Vector3d trans = new Vector3d();
      xform.get(trans);
      
      double minZ = ((double) pCanvas.getWidth()) / 64.0;
      if(minZ > trans.z) {
	trans.z = minZ;
	
	xform.setTranslation(trans);
	tg.setTransform(xform);
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
  mouseEntered
  (
   MouseEvent e
  ) 
  {
    pCanvas.requestFocusInWindow();
  }
  
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
    Object under = objectAtMousePos(e.getX(), e.getY());

    /* mouse press is over a pickable object viewer node */ 
    if(under != null) {
      switch(e.getButton()) {
      case MouseEvent.BUTTON1:
	if(under instanceof ViewerNode) {
	  ViewerNode vunder = (ViewerNode) under;	

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

	  
	  HashMap<NodePath,ViewerNode> changed = new HashMap<NodePath,ViewerNode>();
	    
	  /* BUTTON1: replace selection */ 
	  if((mods & (on1 | off1)) == on1) {
	    for(ViewerNode vnode : clearSelection()) 
	      changed.put(vnode.getNodePath(), vnode);
	    
	    for(ViewerNode vnode : addSelect(vunder))
	      changed.put(vnode.getNodePath(), vnode);
	  }
	  
	  /* BUTTON1+SHIFT: toggle selection */ 
	  else if((mods & (on2 | off2)) == on2) {
	    for(ViewerNode vnode : toggleSelect(vunder)) 
	      changed.put(vnode.getNodePath(), vnode);
	  }
	    
	  /* BUTTON1+SHIFT+CTRL: add to the selection */ 
	  else if((mods & (on3 | off3)) == on3) {
	    for(ViewerNode vnode : addSelect(vunder))
	      changed.put(vnode.getNodePath(), vnode);
	  }
	    
	  /* update the appearance of all nodes who's selection state changed */ 
	  for(ViewerNode vnode : changed.values()) 
	    vnode.update();
	}
	break;

      case MouseEvent.BUTTON2:
	if(under instanceof ViewerNode) {
	  ViewerNode vunder = (ViewerNode) under;

	  int on1  = (MouseEvent.BUTTON2_DOWN_MASK);
	  
	  int off1 = (MouseEvent.BUTTON1_DOWN_MASK | 
		      MouseEvent.BUTTON3_DOWN_MASK | 
		      MouseEvent.SHIFT_DOWN_MASK |
		      MouseEvent.ALT_DOWN_MASK |
		      MouseEvent.CTRL_DOWN_MASK);
	  
	  /* BUTTON2: expand/collapse a node */ 
	  if((mods & (on1 | off1)) == on1) {
	    if(vunder.getNodeStatus().hasSources()) {
	      vunder.setCollapsed(!vunder.isCollapsed());
	      updateUniverse();
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
	  
	  /* BUTTON3: node/link popup menu */ 
	  if((mods & (on1 | off1)) == on1) {
	    if(under instanceof ViewerNode) {
	      ViewerNode vunder = (ViewerNode) under;

	      for(ViewerNode vnode : primarySelect(vunder)) 
		vnode.update();
	    
	      updateNodeMenu();
	      pNodePopup.show(e.getComponent(), e.getX(), e.getY());
	    }
	    else if(under instanceof ViewerLinkRelationship) {
	      ViewerLinkRelationship lunder = (ViewerLinkRelationship) under;

	      {
		HashMap<NodePath,ViewerNode> changed = new HashMap<NodePath,ViewerNode>();
		for(ViewerNode vnode : clearSelection()) 
		  changed.put(vnode.getNodePath(), vnode);
		for(ViewerNode vnode : primarySelect(lunder.getViewerNode())) 
		  changed.put(vnode.getNodePath(), vnode);
		for(ViewerNode vnode : changed.values()) 
		  vnode.update();
	      }

	      pSelectedLink = lunder.getLink();
	      updateLinkMenu();
	      pLinkPopup.show(e.getComponent(), e.getX(), e.getY());
	    }
	  }
	}
	break;
      }
    }
    
    /* mouse press is over an unused spot on the canvas */ 
    else {
      switch(e.getButton()) {
      case MouseEvent.BUTTON1:
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
		      MouseEvent.ALT_DOWN_MASK |
		      MouseEvent.CTRL_DOWN_MASK);


	  int on3  = (MouseEvent.BUTTON1_DOWN_MASK |
		      MouseEvent.SHIFT_DOWN_MASK |
		      MouseEvent.CTRL_DOWN_MASK);

	  int off3 = (MouseEvent.BUTTON2_DOWN_MASK | 
		      MouseEvent.BUTTON3_DOWN_MASK | 
		      MouseEvent.ALT_DOWN_MASK);

	  /* BUTTON1[+SHIFT[+CTRL]]: begin rubber band drag */ 
	  if(((mods & (on1 | off1)) == on1) || 
	     ((mods & (on2 | off2)) == on2) || 
	     ((mods & (on3 | off3)) == on3)) {
	    pRubberBand.beginDrag(new Point2d((double) e.getX(), (double) e.getY()));
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
	    updatePanelMenu();
	    pPanelPopup.show(e.getComponent(), e.getX(), e.getY());
	  }
	}
	break;
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
    int mods = e.getModifiersEx();

    switch(e.getButton()) {
    case MouseEvent.BUTTON1:
      if(pRubberBand.isDragging()) {
	BoundingBox bbox = pRubberBand.endDrag();
	if(bbox != null) {
	  SceneGraphPath gpaths[] = pGeomBranch.pickAll(new PickBounds(bbox));

	  int on1  = 0;
	  
	  int off1 = (MouseEvent.BUTTON1_DOWN_MASK | 
		      MouseEvent.BUTTON2_DOWN_MASK | 
		      MouseEvent.BUTTON3_DOWN_MASK | 
		      MouseEvent.SHIFT_DOWN_MASK |
		      MouseEvent.ALT_DOWN_MASK |
		      MouseEvent.CTRL_DOWN_MASK);
	  
	  
	  int on2  = (MouseEvent.SHIFT_DOWN_MASK);
	  
	  int off2 = (MouseEvent.BUTTON1_DOWN_MASK |
		      MouseEvent.BUTTON2_DOWN_MASK | 
		      MouseEvent.BUTTON3_DOWN_MASK | 
		      MouseEvent.SHIFT_DOWN_MASK |
		      MouseEvent.CTRL_DOWN_MASK);
	  
	  
	  int on3  = (MouseEvent.SHIFT_DOWN_MASK |
		      MouseEvent.CTRL_DOWN_MASK);
	  
	  int off3 = (MouseEvent.BUTTON1_DOWN_MASK |
		      MouseEvent.BUTTON2_DOWN_MASK | 
		      MouseEvent.BUTTON3_DOWN_MASK | 
		      MouseEvent.ALT_DOWN_MASK);
	  
	  HashMap<NodePath,ViewerNode> changed = new HashMap<NodePath,ViewerNode>();
	  
	  /* BUTTON1: replace selection */ 
	  if((mods & (on1 | off1)) == on1) {
	    for(ViewerNode vnode : clearSelection()) 
	      changed.put(vnode.getNodePath(), vnode);
	    
	    if(gpaths != null) {
	      int wk; 
	      for(wk=0; wk<gpaths.length; wk++) {
		Object picked = gpaths[wk].getObject().getUserData();
		if(picked instanceof ViewerNode) {
		  ViewerNode svnode = (ViewerNode) picked;
		  for(ViewerNode vnode : addSelect(svnode))
		    changed.put(vnode.getNodePath(), vnode);
		}
	      }
	    }
	  }
	  
	  /* BUTTON1+SHIFT: toggle selection */ 
	  else if((mods & (on2 | off2)) == on2) {
	    if(gpaths != null) {
	      int wk; 
	      for(wk=0; wk<gpaths.length; wk++) {
		Object picked = gpaths[wk].getObject().getUserData();
		if(picked instanceof ViewerNode) {
		  ViewerNode svnode = (ViewerNode) picked;		  
		  for(ViewerNode vnode : toggleSelect(svnode))
		    changed.put(vnode.getNodePath(), vnode);
		}
	      }
	    }
	  }
	  
	  /* BUTTON1+SHIFT+CTRL: add to selection */ 
	  else if((mods & (on3 | off3)) == on3) {
	    if(gpaths != null) {
	      int wk; 
	      for(wk=0; wk<gpaths.length; wk++) {
		Object picked = gpaths[wk].getObject().getUserData();
		if(picked instanceof ViewerNode) {
		  ViewerNode svnode = (ViewerNode) picked;
		  for(ViewerNode vnode : addSelect(svnode))
		    changed.put(vnode.getNodePath(), vnode);
		}
	      }
	    }
	  }
	  
	  /* update the appearance of all nodes who's selection state changed */ 
	  for(ViewerNode vnode : changed.values()) 
	    vnode.update();
	}

	/* drag started but never updated: clear the selection */ 
	else {
	  for(ViewerNode vnode : clearSelection()) 
	    vnode.update();
	}
      }
      break;
    }
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
    int mods = e.getModifiersEx();
    if(pRubberBand.isDragging()) {
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
		  MouseEvent.ALT_DOWN_MASK |
		  MouseEvent.CTRL_DOWN_MASK);

      
      int on3  = (MouseEvent.BUTTON1_DOWN_MASK |
		  MouseEvent.SHIFT_DOWN_MASK | 
		  MouseEvent.CTRL_DOWN_MASK);
      
      int off3 = (MouseEvent.BUTTON2_DOWN_MASK | 
		  MouseEvent.BUTTON3_DOWN_MASK | 
		  MouseEvent.ALT_DOWN_MASK);

      /* BUTTON1[+SHIFT[+CTRL]]: update rubber band drag */ 
      if(((mods & (on1 | off1)) == on1) || 
	 ((mods & (on2 | off2)) == on2) || 
	 ((mods & (on3 | off3)) == on3)) {
	pRubberBand.updateDrag(pCanvas, new Point2d((double) e.getX(), (double) e.getY()));
      }
      
      /* end rubber band drag */ 
      else {
	pRubberBand.endDrag();
      }
    }
  }

  /**
   * Invoked when the mouse cursor has been moved onto a component but no buttons have 
   * been pushed. 
   */ 
  public void 	
  mouseMoved 
  (
   MouseEvent e
  ) 
  {
    pMousePos = e.getPoint();
  }


  /*-- KEY LISTENER METHODS ----------------------------------------------------------------*/

  /**
   * voked when a key has been pressed.
   */   
  public void 
  keyPressed
  (
   KeyEvent e
  )
  {
    UserPrefs prefs = UserPrefs.getInstance();
    Object under = objectAtMousePos(pMousePos);

    /* node actions */
    if(under instanceof ViewerNode) {
      ViewerNode vunder = (ViewerNode) under;

      for(ViewerNode vnode : primarySelect(vunder)) 
	vnode.update();
      
      if((prefs.getNodeMakeRoot() != null) &&
	 prefs.getNodeMakeRoot().wasPressed(e))
	doMakeRoot();
      else if((prefs.getNodeAddRoot() != null) &&
	 prefs.getNodeAddRoot().wasPressed(e))
	doAddRoot();
      else if((prefs.getNodeReplaceRoot() != null) &&
	 prefs.getNodeReplaceRoot().wasPressed(e))
	doReplaceRoot();
      else if((prefs.getNodeRemoveRoot() != null) &&
	 prefs.getNodeRemoveRoot().wasPressed(e))
	doRemoveRoot();
      else if((prefs.getNodeRemoveAllRoots() != null) &&
	 prefs.getNodeRemoveAllRoots().wasPressed(e))
	doRemoveAllRoots();

      // ...

      else {
	for(ViewerNode vnode : clearSelection()) 
	  vnode.update();
      }
    }

    /* link actions */
    else if(under instanceof ViewerLinkRelationship) {
      ViewerLinkRelationship lunder = (ViewerLinkRelationship) under;
      
      {
	HashMap<NodePath,ViewerNode> changed = new HashMap<NodePath,ViewerNode>();
	for(ViewerNode vnode : clearSelection()) 
	  changed.put(vnode.getNodePath(), vnode);
	for(ViewerNode vnode : primarySelect(lunder.getViewerNode())) 
	  changed.put(vnode.getNodePath(), vnode);
	for(ViewerNode vnode : changed.values()) 
	  vnode.update();
      }
      
      pSelectedLink = lunder.getLink();

      // ...

//       else {
// 	for(ViewerNode vnode : clearSelection()) 
// 	  vnode.update();
//       }
    }
    
    /* panel actions */
    else {
      if((prefs.getCameraCenter() != null) &&
	 prefs.getCameraCenter().wasPressed(e))
	doCenter();
      else if((prefs.getCameraFrameSelection() != null) &&
	 prefs.getCameraFrameSelection().wasPressed(e))
	doFrameSelection();
      else if((prefs.getCameraFrameAll() != null) &&
	 prefs.getCameraFrameAll().wasPressed(e))
	doFrameAll();

      else if((prefs.getAutomaticExpandNodes() != null) &&
	 prefs.getAutomaticExpandNodes().wasPressed(e))
	doAutomaticExpand();
      else if((prefs.getCollapseAllNodes() != null) &&
	 prefs.getCollapseAllNodes().wasPressed(e))
	doCollapseAll();
      else if((prefs.getExpandAllNodes() != null) &&
	 prefs.getExpandAllNodes().wasPressed(e))
	doExpandAll();

      else if((prefs.getShowHideDownstreamNodes() != null) &&
	 prefs.getShowHideDownstreamNodes().wasPressed(e))
	doShowHideDownstream();

      else {
	for(ViewerNode vnode : clearSelection()) 
	  vnode.update();
      }
    }
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


  /*-- POPUP MENU LISTNER METHODS ----------------------------------------------------------*/

  /**
   * This method is called when the popup menu is canceled. 
   */ 
  public void 
  popupMenuCanceled
  (
   PopupMenuEvent e
  )
  { 
    for(ViewerNode vnode : clearSelection()) 
      vnode.update();
  }
   
  /**
   * This method is called before the popup menu becomes invisible. 
   */ 
  public void
  popupMenuWillBecomeInvisible(PopupMenuEvent e) {} 
  
  /**
   * This method is called before the popup menu becomes visible. 
   */ 
  public void 	
  popupMenuWillBecomeVisible(PopupMenuEvent e) {} 



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

    /* node menu events */ 
    String cmd = e.getActionCommand();
    if(cmd.equals("make-root"))
      doMakeRoot();
    else if(cmd.equals("add-root"))
      doAddRoot();
    else if(cmd.equals("replace-root"))
      doReplaceRoot();
    else if(cmd.equals("remove-root"))
      doRemoveRoot();
    else if(cmd.equals("remove-all-roots"))
      doRemoveAllRoots();

    // ...
    
    /* panel menu events */ 
    else if(cmd.equals("center"))
      doCenter();
    else if(cmd.equals("frame-selection"))
      doFrameSelection();
    else if(cmd.equals("frame-all"))
      doFrameAll();
    else if(cmd.equals("automatic-expand"))
      doAutomaticExpand();
    else if(cmd.equals("expand-all"))
      doExpandAll();
    else if(cmd.equals("collapse-all"))
      doCollapseAll();
    else if(cmd.equals("show-hide-downstream"))
      doShowHideDownstream();

    // ...

    else {
      for(ViewerNode vnode : clearSelection()) 
	vnode.update();
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Make the current primary selection the only root node.
   */ 
  private void
  doMakeRoot()
  {
    String prim = getPrimarySelectionName();

    TreeSet<String> roots = new TreeSet<String>();
    roots.add(prim);

    setRoots(roots);
  }

  /**
   * Add the current primary selection to the set of root nodes.
   */ 
  private synchronized void
  doAddRoot()
  {
    String prim = getPrimarySelectionName();

    TreeSet<String> roots = new TreeSet<String>(pRoots.keySet());
    roots.add(prim);

    setRoots(roots);
  }

  /**
   * Replace the root node of the current primary selection with the primary selection.
   */ 
  private synchronized void
  doReplaceRoot()
  {
    String prim = getPrimarySelectionName();
    String root = getPrimarySelectionRootName();

    TreeSet<String> roots = new TreeSet<String>(pRoots.keySet());
    roots.remove(root);
    roots.add(prim);

    setRoots(roots);
  }
  
  /**
   * Remove the root node of the current primary selection from the set of roots nodes.
   */ 
  private synchronized void
  doRemoveRoot()
  {
    String root = getPrimarySelectionRootName();

    TreeSet<String> roots = new TreeSet<String>(pRoots.keySet());
    roots.remove(root);

    setRoots(roots);
  }

  /**
   * Remove all of the roots nodes.
   */ 
  private synchronized void
  doRemoveAllRoots()
  {
    setRoots(new TreeSet<String>());
  }

  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Move the camera so that it is centered on current mouse position.
   */ 
  public void 
  doCenter() 
  {
    Point3d eyePos = new Point3d();
    Point3d pos    = new Point3d();

    pCanvas.getCenterEyeInImagePlate(eyePos);
    pCanvas.getPixelLocationInImagePlate(pMousePos.x, pMousePos.y, pos);

    Transform3D xform = new Transform3D();
    pCanvas.getImagePlateToVworld(xform);
    xform.transform(eyePos);
    xform.transform(pos);

    Vector3d dir = new Vector3d(pos);
    dir.sub(eyePos);
    dir.scale((eyePos.z-1.0) / dir.z);

    Point3d p = new Point3d(eyePos);
    p.sub(dir);

    centerOnPos(new Point2d(p.x, p.y));
  }

  /**
   * Move the camera so that it is centered on the given world space position. 
   */ 
  private void 
  centerOnPos
  (
   Point2d pos
  ) 
  {
    Viewer viewer = pUniverse.getViewer();
    TransformGroup tg = viewer.getViewingPlatform().getViewPlatformTransform();
      
    Transform3D xform = new Transform3D();
    tg.getTransform(xform);
    
    Vector3d trans = new Vector3d();
    xform.get(trans);

    trans.x = pos.x;
    trans.y = pos.y;

    xform.setTranslation(trans);
    tg.setTransform(xform);
  }


  /**
   * Move the camera to frame the bounds of the currently selected nodes.
   */ 
  public void 
  doFrameSelection() 
  {
    frameNodes(pSelected.values());
  }

  /**
   * Move the camera to frame all active nodes.
   */ 
  public void 
  doFrameAll() 
  {
    frameNodes(pNodePool.getActiveViewerNodes());
  }

  /**
   * Move the camera to frame the given set of nodes.
   */ 
  private void 
  frameNodes
  (
   Collection<ViewerNode> vnodes
  ) 
  {
    if(vnodes.isEmpty()) 
      return;

    Point2d minPos = new Point2d(Integer.MAX_VALUE, Integer.MAX_VALUE);
    Point2d maxPos = new Point2d(Integer.MIN_VALUE, Integer.MIN_VALUE);
    for(ViewerNode vnode : vnodes) {
      Point2d pos = vnode.getPosition();
      
      minPos.x = Math.min(minPos.x, pos.x);
      minPos.y = Math.min(minPos.y, pos.y);

      maxPos.x = Math.max(maxPos.x, pos.x);
      maxPos.y = Math.max(maxPos.y, pos.y);
    }

    {
      UserPrefs prefs = UserPrefs.getInstance();
      
      minPos.x -= prefs.getNodeSpaceX()*0.5;
      minPos.y -= prefs.getNodeSpaceY()*0.5;
      
      maxPos.x += prefs.getNodeSpaceX()*0.5;
      maxPos.y += prefs.getNodeSpaceY()*0.5;
    }

    frameBounds(minPos, maxPos);    
  }  

  /**
   * Move the camera to frame the given bounds.
   */ 
  private void 
  frameBounds
  (
   Point2d minPos,  
   Point2d maxPos   
  ) 
  {
    Viewer viewer = pUniverse.getViewer();
    TransformGroup tg = viewer.getViewingPlatform().getViewPlatformTransform();
      
    Transform3D xform = new Transform3D();
    tg.getTransform(xform);
    
    Vector3d trans = new Vector3d();
    xform.get(trans);

    Vector2d extent = new Vector2d(maxPos);
    extent.sub(minPos);
    assert(extent.x >= 0.0);
    assert(extent.y > 0.0);

    Vector2d center = new Vector2d(minPos);
    center.add(maxPos);
    center.scale(0.5);

    trans.x = center.x;
    trans.y = center.y;

    Vector2d cExtent = new Vector2d((double) pCanvas.getWidth(), 
				    (double) pCanvas.getHeight());

    Vector2d nExtent = new Vector2d(extent);

    double nRatio = nExtent.x / nExtent.y;
    double cRatio = cExtent.x / cExtent.y;

    if(nRatio > cRatio) 
      trans.z = nExtent.x;
    else 
      trans.z = nExtent.y * (cExtent.x / cExtent.y);

    trans.z *= 1.25;
    trans.z = Math.max(((double) pCanvas.getWidth()) / 64.0, trans.z);

    xform.setTranslation(trans);
    tg.setTransform(xform);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Change to layout policy to <CODE>AutomaticExpand</CODE> and relayout the nodes.
   */ 
  private void
  doAutomaticExpand()
  {
    for(ViewerNode vnode : clearSelection()) 
      vnode.update();

    pLayoutPolicy = LayoutPolicy.AutomaticExpand;
    updateUniverse();
  }

  /**
   * Change to layout policy to <CODE>ExpandAll</CODE> and relayout the nodes.
   */ 
  private void
  doExpandAll()
  {
    for(ViewerNode vnode : clearSelection()) 
      vnode.update();

    pLayoutPolicy = LayoutPolicy.ExpandAll;
    updateUniverse();
  }

  /**
   * Change to layout policy to <CODE>CollapseAll</CODE> and relayout the nodes.
   */ 
  private void
  doCollapseAll()
  {
    for(ViewerNode vnode : clearSelection()) 
      vnode.update();

    pLayoutPolicy = LayoutPolicy.CollapseAll;
    updateUniverse();
  }

  
  /**
   * Show/Hide the downstream node tree.
   */ 
  private void
  doShowHideDownstream()
  {
    for(ViewerNode vnode : clearSelection()) 
      vnode.update();

    pShowDownstream = !pShowDownstream;
    updateUniverse();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  public synchronized void 
  toGlue
  ( 
   GlueEncoder encoder   
  ) 
    throws GlueException
  {
    super.toGlue(encoder);

    /* root nodes */ 
    if(!pRoots.isEmpty()) 
      encoder.encode("Roots", new TreeSet<String>(pRoots.keySet()));
    
    /* camera position */ 
    {
      Viewer viewer = pUniverse.getViewer();
      TransformGroup tg = viewer.getViewingPlatform().getViewPlatformTransform();
    
      Transform3D xform = new Transform3D();
      tg.getTransform(xform);
      
      Vector3d trans = new Vector3d();
      xform.get(trans);

      encoder.encode("CameraX", trans.x);
      encoder.encode("CameraY", trans.y);
      encoder.encode("CameraZ", trans.z);
    }
  }

  public synchronized void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    /* root nodes */     
    TreeSet<String> roots = (TreeSet<String>) decoder.decode("Roots");
    if(roots != null) {
      pRoots = new TreeMap<String,NodeStatus>();
      for(String name : roots) 
	pRoots.put(name, null);
    }

    /* camera position */ 
    {
      Viewer viewer = pUniverse.getViewer();
      TransformGroup tg = viewer.getViewingPlatform().getViewPlatformTransform();
    
      Transform3D xform = new Transform3D();
      tg.getTransform(xform);
      
      Double cx = (Double) decoder.decode("CameraX");
      Double cy = (Double) decoder.decode("CameraY");
      Double cz = (Double) decoder.decode("CameraZ");

      if((cx == null) || (cy == null) || (cz == null)) 
	throw new GlueException("The camera position was incomplete!");
      Vector3d trans = new Vector3d(cx, cy, cz);

      xform.setTranslation(trans);	    
      tg.setTransform(xform);
    }

    super.fromGlue(decoder);
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The expand/collapse policy to use when laying out nodes.
   */
  private
  enum LayoutPolicy
  {  
    /**
     * Preserve the collapse mode of existing nodes and use an AutomaticExpand policy for 
     * any newly created nodes.
     */
    Preserve, 

    /**
     * Expand all nodes.
     */ 
    ExpandAll, 

    /**
     * Collapse all nodes.
     */ 
    CollapseAll, 
    
    /**
     * Expand the first occurance of a node and collapse all subsequence occurances.
     */
    AutomaticExpand;
  }

  
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the status of the root nodes.
   */ 
  private
  class StatusTask
    extends Thread
  {
    public 
    StatusTask
    (
     JNodeViewerPanel viewer
    ) 
    {
      pViewer = viewer;
    }
 
    public void 
    run() 
    {
      synchronized(pViewer) {
	UIMaster master = UIMaster.getInstance();
	if(master.beginPanelOp("Updating Node Status...")) {
	  try {
	    TreeSet<String> dead = new TreeSet<String>();
	    for(String name : pRoots.keySet()) {
	      if(pRoots.get(name) == null) {
		try {
		  System.out.print("Recomputing Status: " + name + " ");
		  
		  MasterMgrClient client = master.getMasterMgrClient();
		  NodeStatus status = client.status(pAuthor, pView, name);
		  pRoots.put(name, status);
		  
		  System.out.print("[DONE]\n");
		}
		catch(PipelineException ex) {
		  System.out.print("[undefined]\n");
		  dead.add(name);
		}
	      }
	    }
	    
	    for(String name : dead) 
	      pRoots.remove(name);
	    
	    updateUniverse();
	  }
	  finally {
	    master.endPanelOp("Done.");
	  }
	}
      }
    }
    
    private JNodeViewerPanel  pViewer;
  }


  // DEBUG
  private void 
  printStatusShortHelper
  (
   NodeStatus status,
   int level, 
   StringBuffer buf
  ) 
    throws GlueException
  {
    printStatusShortDownstreamHelper(status, level, buf);

    buf.append("->");
    int wk;
    for(wk=0; wk<level; wk++) 
      buf.append("  ");
    buf.append("---\n");

    printStatusShortUpstreamHelper(status, level, buf);
  }

  private void 
  printStatusShortDownstreamHelper
  (
   NodeStatus status,
   int level, 
   StringBuffer buf
  ) 
    throws GlueException
  {
    for(NodeStatus tstatus : status.getTargets()) 
      printStatusShortDownstreamHelper(tstatus, level+1, buf);

    buf.append("->");
    int wk;
    for(wk=0; wk<level; wk++) 
      buf.append("  ");
    buf.append(status.getNodeID().getName() + "\n");
  }
  
  private void 
  printStatusShortUpstreamHelper
  (
   NodeStatus status,
   int level, 
   StringBuffer buf
  ) 
    throws GlueException
  {
    buf.append("->");
    int wk;
    for(wk=0; wk<level; wk++) 
      buf.append("  ");
    buf.append(status.getNodeID().getName() + "\n");
    
    for(NodeStatus sstatus : status.getSources()) 
      printStatusShortUpstreamHelper(sstatus, level+1, buf);
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 6047073003000120503L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to display the downstream tree of nodes.
   */ 
  private boolean  pShowDownstream;

  /**
   * The expand/collapse policy to use when laying out nodes.
   */ 
  private LayoutPolicy  pLayoutPolicy;

  /**
   * The status of the root nodes and all of its upstream/downstream connections indexed
   * by the fully resolved names of the root nodes. <P> 
   *
   * If the status is <CODE>null</CODE> for a given root node, then it will be updated
   * by the <CODE>StatusTask</CODE> the next time it is run.
   */ 
  private TreeMap<String,NodeStatus>  pRoots;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The panel popup menu.
   */ 
  private JPopupMenu  pPanelPopup; 

  /**
   * The panel popup menu items.
   */
  private JMenuItem  pShowHideDownstreamItem;
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * The node popup menu.
   */ 
  private JPopupMenu  pNodePopup; 
  
  /**
   * The link node submenu.
   */ 
  private JMenu  pLinkMenu;
  
  /**
   * The remove secondary node submenu.
   */ 
  private JMenu  pRemoveSecondaryMenu;
  
  /** 
   * The table of secondary file sequences currently displayed in the remove 
   * secondary node submenu indexed by secondary sequence name.
   */ 
  private TreeMap<String,FileSeq>  pRemoveSecondarySeqs;
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * The link popup menu.
   */ 
  private JPopupMenu  pLinkPopup;

  /**
   * The link type submenu.
   */ 
  private JMenu  pLinkCatagoryMenu;

  /**
   * The link relationship items.
   */ 
  private JMenuItem  pLinkNoneRelationshipItem;
  private JMenuItem  pLinkOneToOneRelationshipItem;
  private JMenuItem  pLinkAllRelationshipItem;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The Java3D scene graph.
   */ 
  private SimpleUniverse  pUniverse;      

  /**
   * The 3D canvas used to render the Java3D universe.
   */ 
  private Canvas3D  pCanvas;       

  /**
   * The reuseable collection of ViewerNodes.
   */ 
  private ViewerNodePool  pNodePool;

  /**
   * The links between ViewerNodes.
   */ 
  private ViewerLinks  pLinks;


  /**
   * The selection rubber band geometry.
   */ 
  private RubberBand  pRubberBand;
  
  /**
   * The branch containing node/link geometry.
   */ 
  private BranchGroup  pGeomBranch;

  /**
   * The set of currently selected nodes indexed by <CODE>NodePath</CODE>.
   */ 
  private HashMap<NodePath,ViewerNode>  pSelected;

  /**
   * The primary selection.
   */ 
  private ViewerNode  pPrimary;

  /**
   * The currently selected link.
   */
  private LinkCommon  pSelectedLink;


  /**
   * The last known mouse position.
   */ 
  private Point pMousePos;

  /**
   * The bounds of the currently visible nodes.
   */ 
  private Point2d  pMinNodeBounds;
  private Point2d  pMaxNodeBounds;

}
