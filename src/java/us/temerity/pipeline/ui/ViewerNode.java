// $Id: ViewerNode.java,v 1.2 2004/05/07 18:10:50 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.io.*;
import java.util.*;
import javax.vecmath.*;
import javax.media.j3d.*;
import com.sun.j3d.utils.geometry.*;

/*------------------------------------------------------------------------------------------*/
/*   V I E W E R   N O D E                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A reusable Java3D based graphical representation of the status of a Pipeline node. <P> 
 * 
 * Textured geometry is used to visualizing {@link NodeStatus NodeStatus} and associated 
 * {@link NodeDetails NodeDetails} instances. This class also maintains the 2D layout 
 * position and the collapsed/expanded state of this geometry.  <P> 
 * 
 * A <CODE>ViewerNode</CODE> is associated with a single <CODE>NodeStatus</CODE>.  However, 
 * a <CODE>NodeStatus</CODE> may be represented by more than one <CODE>ViewerNode</CODE>
 * if the node is reachable via multiple upstream/downstream paths.  For this reason, each
 * instance of this class maintains a {@link NodePath NodePath} field in addition to the 
 * current <CODE>NodeStatus</CODE> to identify the unique path from the focus node of the 
 * {@link JNodeViewerPanel JNodeViewerPanel} to the <CODE>NodeStatus</CODE> associated
 * with this <CODE>ViewerNode</CODE> instance.
 */
public 
class ViewerNode
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constuct a new viewer node. 
   */ 
  public 
  ViewerNode() 
  {
    /* initialzie state fields */ 
    {
      pIsReset     = true;
      pMode        = SelectionMode.Normal;
      pIsVisible   = false;
      pIsCollapsed = false;
      pPos         = new Point2d();
    }    

    /* initialize the Java3D geometry */ 
    {
      /* the root branch group */ 
      pRoot = new BranchGroup();
      
      /* the visibility switch */ 
      {
	pSwitch = new Switch();
	pSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);

	pRoot.addChild(pSwitch);
      }
      
      /* the transform group used to position the node icon */ 
      {
	pXform = new TransformGroup();
	pXform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
	
	pSwitch.addChild(pXform);
      }
      
      /* the text label */ 
      {
	pLabelSwitch = new Switch(0);
	pLabelSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
	pLabelSwitch.setCapability(Switch.ALLOW_CHILDREN_WRITE);

	for(SelectionMode mode : SelectionMode.all()) {
	  BranchGroup group = new BranchGroup();
	  group.setCapability(BranchGroup.ALLOW_DETACH);

	  pLabelSwitch.addChild(group);
	}

	pXform.addChild(pLabelSwitch);
      }
      
      /* the node icon */ 
      {
	Appearance apr = new Appearance();
	
	Point3d pts[] = new Point3d[4];
	TexCoord2f uvs[] = new TexCoord2f[4];
	{
	  pts[0] = new Point3d(-0.5, -0.5, 0.0);
	  uvs[0] = new TexCoord2f(0.0f, 0.0f);
	  
	  pts[1] = new Point3d(0.5, -0.5, 0.0);
	  uvs[1] = new TexCoord2f(1.0f, 0.0f);
	  
	  pts[2] = new Point3d(0.5, 0.5, 0.0);
	  uvs[2] = new TexCoord2f(1.0f, 1.0f);
	  
	  pts[3] = new Point3d(-0.5, 0.5, 0.0);
	  uvs[3] = new TexCoord2f(0.0f, 1.0f);
	}
	
	GeometryInfo gi = new GeometryInfo(GeometryInfo.QUAD_ARRAY);
	gi.setCoordinates(pts);
	gi.setTextureCoordinateParams(1, 2);
	gi.setTextureCoordinates(0, uvs);
	
	NormalGenerator ng = new NormalGenerator();
	ng.generateNormals(gi);
	
	Stripifier st = new Stripifier();
	st.stripify(gi);
	
	GeometryArray ga = gi.getGeometryArray();
	
	pShape = new Shape3D(ga, apr);
	pShape.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
	pShape.setCapability(Shape3D.ALLOW_PICKABLE_WRITE);
	pShape.setPickable(true);
	pShape.setUserData(this);
	
	pXform.addChild(pShape);
      }
      
      /* the collapsed upstream icon visibility switch */ 
      {
	pCollapsedSwitch = new Switch();
	pCollapsedSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);

	pXform.addChild(pCollapsedSwitch);
      }
      
      /* the transform group used to position the collapsed icon */ 
      {
	pCollapsedXform = new TransformGroup();
	pCollapsedXform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
	
	pCollapsedSwitch.addChild(pCollapsedXform);
      }
      
      /* the icon */ 
      try {
	AppearanceMgr mgr = AppearanceMgr.getInstance();
	Appearance apr = mgr.getNodeAppearance("Collapsed", SelectionMode.Normal);
	
	Point3d pts[] = new Point3d[4];
	TexCoord2f uvs[] = new TexCoord2f[4];
	{
	  pts[0] = new Point3d(-0.5, -0.5, 0.0);
	  uvs[0] = new TexCoord2f(0.0f, 0.0f);
	  
	  pts[1] = new Point3d(0.5, -0.5, 0.0);
	  uvs[1] = new TexCoord2f(1.0f, 0.0f);
	  
	  pts[2] = new Point3d(0.5, 0.5, 0.0);
	  uvs[2] = new TexCoord2f(1.0f, 1.0f);
	  
	  pts[3] = new Point3d(-0.5, 0.5, 0.0);
	  uvs[3] = new TexCoord2f(0.0f, 1.0f);
	}
	
	GeometryInfo gi = new GeometryInfo(GeometryInfo.QUAD_ARRAY);
	gi.setCoordinates(pts);
	gi.setTextureCoordinateParams(1, 2);
	gi.setTextureCoordinates(0, uvs);
	
	NormalGenerator ng = new NormalGenerator();
	ng.generateNormals(gi);
	
	Stripifier st = new Stripifier();
	st.stripify(gi);
	
	GeometryArray ga = gi.getGeometryArray();
	
	Shape3D shape = new Shape3D(ga, apr);
	pCollapsedXform.addChild(shape);
      }
      catch(IOException ex) {
	Logs.tex.severe("Internal Error:\n" + 
			"  " + ex.getMessage());
	Logs.flush();
	System.exit(1);
      }
    }    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether this instance is newly created or recycled.
   */ 
  public boolean 
  isReset() 
  {
    return pIsReset;
  }
  
  /**
   * Indicate that this instance has been newly created or recycled.
   */ 
  public void
  reset() 
  {
    pIsReset = true;

    System.out.print("ViewerNode.reset(): " + pStatus + " [" + pPath + "]\n");
  }


  /**
   * Get the current node status. 
   */ 
  public NodeStatus
  getNodeStatus() 
  {
    return pStatus;
  }

  /**
   * Get the path from the focus node to the current node.
   */ 
  public NodePath
  getNodePath() 
  {
    return pPath;
  }

  /**
   * Set the current node status and path.
   * 
   * @param status
   *   The current node status.
   * 
   * @param path
   *   The path from the focus node to the current node.
   */ 
  public void 
  setCurrentState
  (
   NodeStatus status, 
   NodePath path  
  ) 
  {
    if(status == null) 
      throw new IllegalArgumentException("The node status cannot be (null)!");

    if((pStatus == null) || (!pStatus.toString().equals(status.toString()))) 
      pLabelTextChanged = true;

    pStatus = status;

    System.out.print("ViewerNode.setCurrentState(): " + status + " [" + path + "]\n");
  }

  
  /**
   * Get the current selection mode.
   */ 
  public SelectionMode
  getSelectionMode() 
  {
    return pMode;
  }

  /**
   * Set the current selection mode.
   */ 
  public void 
  setSelectionMode
  (
   SelectionMode mode
  ) 
  {
    pMode = mode;
  }


  /**
   * Is this geometry currently visible?
   */ 
  public boolean 
  isVisible() 
  {
    return pIsVisible;
  }

  /**
   * Set whether this geometry is currently visible.
   */ 
  public void 
  setVisible
  (
   boolean tf  
  ) 
  {    
    pIsVisible = tf;

    if(pIsVisible) 
      pSwitch.setWhichChild(Switch.CHILD_ALL);
    else 
      pSwitch.setWhichChild(Switch.CHILD_NONE);

    pShape.setPickable(pIsVisible);
  }

  
  /**
   * Are upstream nodes collapsed?
   */ 
  public boolean 
  isCollapsed() 
  {
    return pIsCollapsed;
  }

  /**
   * Set whether upstream nodes are collapsed.
   */ 
  public void 
  setCollapsed
  (
   boolean tf  
  ) 
  {    
    pIsCollapsed = tf;

    if(pIsCollapsed) 
      pCollapsedSwitch.setWhichChild(Switch.CHILD_ALL);
    else 
      pCollapsedSwitch.setWhichChild(Switch.CHILD_NONE);
  }
  
  
  /** 
   * Get the 2D position of the node (center of icon).
   */ 
  public Point2d
  getPosition()
  {
    return new Point2d(pPos);
  }

  /**
   * Set the 2D position of the node (center of icon). 
   */
  public void 
  setPosition
  (
   Point2d pos    
  ) 
  {
    pPos.set(pos);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the root group which contains all Java3D geometry.
   */ 
  public BranchGroup 
  getBranchGroup() 
  {
    return pRoot;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   N O D E   S T A T E                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the position, geometry and appearance based on the current state of the node.
   */ 
  public void 
  update() 
  {
    NodeDetails details = pStatus.getDetails();

    /* move the node to the current position */ 
    {
      Transform3D xform = new Transform3D();
      xform.setTranslation(new Vector3d(pPos.x, pPos.y, 0.0));
      pXform.setTransform(xform);
    }

    try { 
      /* update the label */ 
      {
	if(pLabelTextChanged) {	  
	  Point3d pos = new Point3d(0.0, 1.7, 0.0);
	  
	  Transform3D xform = new Transform3D();
	  xform.setScale(0.35);
	  
	  String title = pStatus.toString();
	  
	  System.out.print("Updating Label = " + title + "\n");
	  for(SelectionMode mode : SelectionMode.all()) {
	    TransformGroup label = 
	      ViewerLabels.createLabelGeometry(title, "CharterBTRoman", mode, 0.05, pos);
	    label.setTransform(xform);
	    
	    BranchGroup group = new BranchGroup();
	    group.setCapability(BranchGroup.ALLOW_DETACH);
	    group.addChild(label);

	    pLabelSwitch.setChild(group, mode.ordinal());
	  }
	  
	  pLabelTextChanged = false;
	}
	
	pLabelSwitch.setWhichChild(pMode.ordinal());
      }
      
      /* update the node icon appearance */ 
      {
	String aname = "Blank";
	if(details != null) 
	  aname = (details.getOverallNodeState() + "-" + details.getOverallQueueState());
	
	System.out.print("Updating Node Icon = " + aname + "\n");

	AppearanceMgr mgr = AppearanceMgr.getInstance();
	Appearance apr = mgr.getNodeAppearance(aname, pMode);
	pShape.setAppearance(apr);
      }
    }
    catch(IOException ex) {
      Logs.tex.severe("Internal Error:\n" + 
		      "  " + ex.getMessage());
      Logs.flush();
      System.exit(1);
    }

    /* move the collapsed icon to the correct side of the node icon */ 
    {
      Transform3D xform = new Transform3D();
      xform.setTranslation(new Vector3d((details == null) ? -0.8 : 0.8, 0.0, 0.0));
      pCollapsedXform.setTransform(xform);
    } 

    /* no longer reset once it has been updated */ 
    pIsReset = false;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether this instance is newly created or recycled.
   */ 
  private boolean  pIsReset;

  /**
   * The current node status.
   */ 
  private NodeStatus  pStatus;

  /**
   * The path from the focus node to the current node.
   */ 
  private NodePath  pPath;

  /**
   * The UI selection mode.
   */
  private SelectionMode  pMode;  

  /**
   * Whether this geometry is currently visible.
   */
  private boolean  pIsVisible;     

  /**
   * Whether upstream nodes are collapsed.
   */
  private boolean  pIsCollapsed;

  /**
   * the 2D position of the node (center of icon).
   */ 
  private Point2d   pPos;         

  /**
   * Whether the text label geometry is no longer valid for the current node status.
   */ 
  private boolean  pLabelTextChanged;




  /**
   * The root branch group. 
   */ 
  private BranchGroup  pRoot;     

  /**
   * The switch group used to control node icon visbilty. 
   */ 
  private Switch  pSwitch;   

  /** 
   * The transform group used to position the node as a whole.
   */ 
  private TransformGroup  pXform;      

  /**
   * The switch group which contains the label branch groups.
   */ 
  private Switch  pLabelSwitch;     

  /**
   * The node icon geometry.
   */ 
  private Shape3D  pShape;  

  /**
   * The switch group used to control collapsed icon visbilty. 
   */ 
  private Switch  pCollapsedSwitch; 

  /** 
   * The transform group used to position the collapsed icon.
   */ 
  private TransformGroup  pCollapsedXform;               
}
