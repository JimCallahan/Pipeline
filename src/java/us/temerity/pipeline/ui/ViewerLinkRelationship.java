// $Id: ViewerLinkRelationship.java,v 1.1 2004/05/18 00:35:05 jim Exp $

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
 * A reusable Java3D based graphical representation of a 
 * {@link LinkRelationship LinkRelationship}. <P> 
 */
public 
class ViewerLinkRelationship
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constuct a new viewer link relationship. 
   */ 
  public 
  ViewerLinkRelationship() 
  {
    /* initialize state fields */ 
    {
      pIsVisible = false;
      pPos       = new Point2d();
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
      
      /* the transform group used to position the icon */ 
      {
	pXform = new TransformGroup();
	pXform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
	
	pSwitch.addChild(pXform);
      }
      
      /* the relationship icon */ 
      {
	Appearance apr = new Appearance();
	
	Point3d pts[] = new Point3d[4];
	TexCoord2f uvs[] = new TexCoord2f[4];
	{
	  pts[0] = new Point3d(-0.25, -0.25, 0.0);
	  uvs[0] = new TexCoord2f(0.0f, 0.0f);
	  
	  pts[1] = new Point3d(0.25, -0.25, 0.0);
	  uvs[1] = new TexCoord2f(1.0f, 0.0f);
	  
	  pts[2] = new Point3d(0.25, 0.25, 0.0);
	  uvs[2] = new TexCoord2f(1.0f, 1.0f);
	  
	  pts[3] = new Point3d(-0.25, 0.25, 0.0);
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
    }    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the parent link.
   */ 
  public LinkCommon
  getLink() 
  {
    return pLink;
  }

  /**
   * Get the target viewer node.
   */ 
  public ViewerNode
  getViewerNode() 
  {
    return pViewerNode;
  }

  /**
   * Get the status of the node which is the target of the link.
   */ 
  public NodeStatus
  getNodeStatus() 
  {
    return pViewerNode.getNodeStatus();
  }

  /**
   * Set the parent link and target node status.
   * 
   * @param link 
   *   The parent link.
   * 
   * @param vnode
   *   The target viewer node.
   */ 
  public void 
  setCurrentState
  (
   LinkCommon link,
   ViewerNode vnode
  ) 
  {
    if(link == null) 
      throw new IllegalArgumentException("The node link cannot be (null)!");

    if(vnode == null) 
      throw new IllegalArgumentException("The node vnode cannot be (null)!");

    if((pLink == null) || (pLink.getRelationship() != link.getRelationship())) 
      pIconAprChanged = true;
    
    pLink       = link;
    pViewerNode = vnode;
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
   * Get the 2D position of the center of the icon.
   */ 
  public Point2d
  getPosition()
  {
    return new Point2d(pPos);
  }

  /**
   * Set the 2D position of the center of the icon.
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
   * Update the position, geometry and appearance based on the current state.
   */ 
  public void 
  update() 
  {
    /* move the icon to the current position */ 
    {
      Transform3D xform = new Transform3D();
      xform.setTranslation(new Vector3d(pPos.x, pPos.y, 0.0));
      pXform.setTransform(xform);
    }

    try { 
      /* update the node icon appearance */ 
      if(pIconAprChanged) {
	AppearanceMgr mgr = AppearanceMgr.getInstance();
	Appearance apr = mgr.getLinkRelationshipAppearance(pLink.getRelationship());

	pShape.setAppearance(apr);

	pIconAprChanged = false;
      }
    }
    catch(IOException ex) {
      Logs.tex.severe("Internal Error:\n" + 
		      "  " + ex.getMessage());
      Logs.flush();
      System.exit(1);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent link.
   */ 
  private LinkCommon  pLink;

  /**
   * The target viewer node.
   */ 
  private ViewerNode  pViewerNode;

  /**
   * Whether this geometry is currently visible.
   */
  private boolean  pIsVisible;     

  /**
   * the 2D position of the node (center of icon).
   */ 
  private Point2d   pPos;         

  /**
   * Whether the icon appearance has changed.
   */ 
  private boolean  pIconAprChanged;


  /**
   * The root branch group. 
   */ 
  private BranchGroup  pRoot;     

  /**
   * The switch group used to control icon visbilty. 
   */ 
  private Switch  pSwitch;   

  /** 
   * The transform group used to position the icon as a whole.
   */ 
  private TransformGroup  pXform;      

  /**
   * The icon geometry.
   */ 
  private Shape3D  pShape;  

}
