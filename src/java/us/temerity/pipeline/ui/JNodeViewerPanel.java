// $Id: JNodeViewerPanel.java,v 1.9 2004/05/13 21:26:40 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
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
  implements ComponentListener
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
  private void 
  initUI()
  {
    /* initialize the panel components */ 
    {
      setLayout(new BorderLayout());
      setMinimumSize(new Dimension(50, 50));
      setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
      
      /* canvas */ 
      {
	pCanvas = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
	pCanvas.addComponentListener(this); 
	//pCanvas.addMouseListener(this); 
	//pCanvas.addMouseMotionListener(this); 
	
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
	// ...
      }

      /* the node pool */ 
      {
	pNodePool = new ViewerNodePool();
	pUniverse.addBranchGraph(pNodePool.getBranchGroup());
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
  public void 
  setAuthorView
  (
   String author, 
   String view 
  ) 
  {
    super.setAuthorView(author, view);
    updateNodeStatus();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the focus of the viewer to the given node in the working area view owned by 
   * the given author.
   */ 
  public void 
  setFocus
  (
   String author, 
   String view,
   String name
  )  
  {
    super.setAuthorView(author, view);
    pFocus = name;
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

  /**
   * Update the nodes being viewed.
   */ 
  private void 
  updateNodeStatus()
  {
    if(pNodePool == null) 
      return;
    
    StatusTask task = new StatusTask();
    task.start();
  }

  /**
   * Update the visualization graphics.
   */
  private synchronized void 
  updateUniverse()
  {  
    pNodePool.updatePrep();
      
    if(pStatus != null) {

      /* layout the upstream nodes */ 
      {
	NodePath path = new NodePath(pStatus.getName());
	HashMap<NodePath,ViewerNode> upstream = new HashMap<NodePath,ViewerNode>();
	layoutUpstreamNodes(pStatus, path, new Point2d(0.0, 0.0), upstream);

	/* shift the nodes so that the root node is at the origin */ 
	{
	  ViewerNode root = upstream.get(path);
	  double offset = -root.getPosition().y;
	  for(ViewerNode vnode : upstream.values()) {
	    Point2d pos = vnode.getPosition();
	    vnode.setPosition(new Point2d(pos.x, pos.y+offset));
	  }
	}
      }
    }
    
    pNodePool.update();
  }
  
  /**
   * Recursively layout the upstream nodes.
   */ 
  private double
  layoutUpstreamNodes
  (
   NodeStatus status, 
   NodePath path, 
   Point2d anchor, 
   HashMap<NodePath,ViewerNode> table
  ) 
  {
    UserPrefs prefs = UserPrefs.getInstance();

    assert(!table.containsKey(path));
    ViewerNode vnode = pNodePool.getViewerNode(status, path);
    table.put(path, vnode);

    double height = 0.0;
    if(status.hasSources()) {
      for(NodeStatus ustatus : status.getSources()) {
	NodePath upath = new NodePath(path, ustatus.getName());
	Point2d uanchor = new Point2d(anchor.x + prefs.getNodeSpaceX(), anchor.y + height);
	height += layoutUpstreamNodes(ustatus, upath, uanchor, table);
      }      
    }
    else {
      height = prefs.getNodeSpaceY();
    }

    double offset = ((path.getNumNodes() % 2) == 0) ? prefs.getNodeOffset() : 0.0;
    vnode.setPosition(new Point2d(anchor.x, anchor.y + 0.5*height + offset));

    return height;
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

    /* focus node */ 
    if(pFocus != null) 
      encoder.encode("Focus", pFocus);
    
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

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    /* focus node */     
    String focus = (String) decoder.decode("Focus");
    if(focus != null) 
      pFocus = focus;

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
   * Get the status of the focused node.
   */ 
  private
  class StatusTask
    extends Thread
  { 
    public void 
    run() 
    {  
      pStatus = null;
      if(pFocus != null) {
	UIMaster master = UIMaster.getInstance();

	if(master.beginPanelOp("Updating Node Status...")) {
	  String msg = "Failed!";
	  try {
	    pStatus = master.getNodeMgrClient().status(pAuthor, pView, pFocus);
	    msg = "Done.";
	  }
	  catch(PipelineException ex) {
	    master.showErrorDialog(ex);
	  }
	  finally {
	    master.endPanelOp(msg);
	  }
	}
      }

//     try {
//       StringBuffer buf = new StringBuffer();
//       printStatusShortHelper(pStatus, 2, buf);
//       System.out.print("Node Status: \n" + buf.toString() + "\n");
//     }
//     catch(Exception ex) {
//     }


      updateUniverse();
    }
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
   * The fully resolved name of the focus node.
   */ 
  private String  pFocus;
  
  /**
   * The status of the focus node and all of its upstream/downstream connections.
   */ 
  private NodeStatus pStatus;

  
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

}
