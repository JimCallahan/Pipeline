// $Id: JNodeViewerPanel.java,v 1.2 2004/05/07 15:06:06 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

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
      
      
      // DEBUG 
      {
 	BranchGroup branch = new BranchGroup();

// 	try {
// 	  TransformGroup tg = new TransformGroup();
// 	  tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
// 	  branch.addChild(tg);

// 	  AppearanceMgr amgr = AppearanceMgr.getInstance();
// 	  Appearance apr = amgr.getNodeAppearance(OverallNodeState.Pending, 
// 						  OverallQueueState.Queued, 
// 						  SelectionMode.Normal);
// 	  {
// 	    ColoringAttributes attr = 
// 	    new ColoringAttributes(1.0f, 1.0f, 0.0f, ColoringAttributes.SHADE_FLAT);
// 	    apr.setColoringAttributes(attr);
// 	  }
	  
// 	  Point3d pts[] = new Point3d[4];
// 	  TexCoord2f uvs[] = new TexCoord2f[4];
// 	  {
// 	    pts[0] = new Point3d(-1.25, -1.25, 0.0);
// 	    uvs[0] = new TexCoord2f(0.0f, 0.0f);
	    
// 	    pts[1] = new Point3d(1.25, -1.25, 0.0);
// 	    uvs[1] = new TexCoord2f(1.0f, 0.0f);
	    
// 	    pts[2] = new Point3d(1.25, 1.25, 0.0);
// 	    uvs[2] = new TexCoord2f(1.0f, 1.0f);
	  
// 	    pts[3] = new Point3d(-1.25, 1.25, 0.0);
// 	    uvs[3] = new TexCoord2f(0.0f, 1.0f);
// 	  }
	  
// 	  GeometryInfo gi = new GeometryInfo(GeometryInfo.QUAD_ARRAY);
// 	  gi.setCoordinates(pts);
// 	  gi.setTextureCoordinateParams(1, 2);
// 	  gi.setTextureCoordinates(0, uvs);
	  
// 	  NormalGenerator ng = new NormalGenerator();
// 	  ng.generateNormals(gi);
	
// 	  Stripifier st = new Stripifier();
// 	  st.stripify(gi);
	  
// 	  GeometryArray ga = gi.getGeometryArray();
// 	  Shape3D shape = new Shape3D(ga, apr);
	  
// 	  tg.addChild(shape);
// 	}
// 	catch(IOException ex) {
// 	  Logs.tex.severe(ex.getMessage());
// 	}
	

// 	try {
// 	  TransformGroup tg = 
// 	    ViewerLabels.createLabelGeometry("Hello", "CharterBTRoman", 
// 					     SelectionMode.Normal, 
// 					     0.0, new Point3d(0.0, 1.0, 0.0));
// 	  branch.addChild(tg); 
// 	}
// 	catch(IOException ex) {
// 	  Logs.tex.severe(ex.getMessage());
// 	}

// 	pUniverse.addBranchGraph(branch);

	try {
	  {
	    ViewerNode vnode = new ViewerNode();
	    pUniverse.addBranchGraph(vnode.getBranchGroup());
	    
	    NodeStatus status = 
	      UIMaster.getInstance().getNodeMgrClient().status
	        ("default", "/images/normal");
	    vnode.setStatus(status); 
	    vnode.setPosition(new Point2d(0.0, -2.0));
	    vnode.update();
	    
	    vnode.setVisible(true);
	  }

	  {
	    NodeStatus status = 
	      UIMaster.getInstance().getNodeMgrClient().status
	      ("default", "/animals/birds/eagle");

	      ViewerNode vnode = new ViewerNode();
	      pUniverse.addBranchGraph(vnode.getBranchGroup());

	      vnode.setStatus(status); 
	      vnode.setPosition(new Point2d(0.0, 0.0));
	      vnode.setCollapsed(true);
	      vnode.update();

	      vnode.setVisible(true);
	  }

	  {
	    NodeStatus status = 
	      UIMaster.getInstance().getNodeMgrClient().status
	        ("default", "/animals/insects/fly");
	      
	    ViewerNode vnode = new ViewerNode();
	    pUniverse.addBranchGraph(vnode.getBranchGroup());
	    
	    vnode.setStatus(status.getTarget("/animals/insects/dragonfly")); 
	    vnode.setPosition(new Point2d(0.0, 2.0));
	    vnode.setCollapsed(true);
	    vnode.update();
	    
	    vnode.setVisible(true);
	  }
	}
 	catch(Exception ex) {
	  ex.printStackTrace();
 	}
      }
      // DEBUG 

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
    updateUniverse();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the nodes being viewed.
   */ 
  private void 
  updateUniverse()
  {

    // ...

  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- COMPONENT LISTNER METHODS -----------------------------------------------------------*/

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



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 6047073003000120503L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The Java3D scene graph.
   */ 
  private SimpleUniverse  pUniverse;      

  /**
   * The 3D canvas used to render the Java3D universe.
   */ 
  private Canvas3D  pCanvas;       

  

}
