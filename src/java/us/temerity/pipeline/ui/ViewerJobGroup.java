// $Id: ViewerJobGroup.java,v 1.1 2004/08/30 06:52:46 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.io.*;
import java.util.*;
import javax.vecmath.*;
import javax.media.j3d.*;
import com.sun.j3d.utils.geometry.*;

/*------------------------------------------------------------------------------------------*/
/*   V I E W E R   J O B   G R O U P                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A reusable Java3D based graphical representation of the status of a Pipeline job group. <P>
 */
public 
class ViewerJobGroup
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constuct a new viewer job. 
   */ 
  public 
  ViewerJobGroup() 
  {
    /* initialize state fields */ 
    {
      pViewerJobs = new ArrayList<ViewerJob>();

      pMode = SelectionMode.Normal;

      pLineAntiAlias = true;
      pLineThickness = 1.0;

      pIsVisible   = false;

      pMinBounds = new Point2d();
      pMaxBounds = new Point2d();
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
      
      /* the transform group used to position the job icon */ 
      {
	pXform = new TransformGroup();
	pXform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
	
	pSwitch.addChild(pXform);
      }
      
      /* the job group icon */ 
      try { 
	AppearanceMgr mgr = AppearanceMgr.getInstance();
	Appearance apr = mgr.getLineAppearance("DarkGrey");
	
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
      catch(IOException ex) {
	Logs.tex.severe("Internal Error:\n" + 
			"  " + ex.getMessage());
	Logs.flush();
	System.exit(1);
      }
      
      /* the border lines */ 
      {
	Appearance apr = new Appearance();

	LineArray la = new LineArray(8, LineArray.COORDINATES);

	la.setCoordinate(0, new Point3d(-0.5, -0.5, 0.0));
	la.setCoordinate(1, new Point3d(0.5, -0.5, 0.0));

	la.setCoordinate(2, new Point3d(0.5, -0.5, 0.0));
	la.setCoordinate(3, new Point3d(0.5, 0.5, 0.0));

	la.setCoordinate(4, new Point3d(0.5, 0.5, 0.0));
	la.setCoordinate(5, new Point3d(-0.5, 0.5, 0.0));

	la.setCoordinate(6, new Point3d(-0.5, 0.5, 0.0));
	la.setCoordinate(7, new Point3d(-0.5, -0.5, 0.0));

	pBorderLines = new Shape3D(la, apr);
	pBorderLines.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
	pBorderLines.setPickable(true);
	pBorderLines.setUserData(this);

	pXform.addChild(pBorderLines);
      }
    }    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the associated job group. 
   */ 
  public QueueJobGroup
  getGroup() 
  {
    return pJobGroup;
  }

  /**
   * Get the viewer jobs which are members of this group.
   */ 
  public Collection<ViewerJob>
  getViewerJobs() 
  {
    return Collections.unmodifiableCollection(pViewerJobs);
  }

  /**
   * Set the current job group and viewer jobs.
   * 
   * @param group
   *   The job group. 
   * 
   * @param vjobs
   *   The viewer jobs.
   */ 
  public void 
  setCurrentState
  (
   QueueJobGroup group,
   ArrayList<ViewerJob> vjobs
  ) 
  {
    if(group == null) 
      throw new IllegalArgumentException("The job group cannot be (null)!");
    pJobGroup = group;

    if(vjobs == null) 
      throw new IllegalArgumentException("The viewer jobs cannot be (null)!");
    pViewerJobs = vjobs;
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
    pModeChanged = true;
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
    pSwitch.setWhichChild(pIsVisible ? Switch.CHILD_ALL : Switch.CHILD_NONE);
    pShape.setPickable(pIsVisible);
  }

  
  /** 
   * Get the minimum bounds of (2D position) of the job.
   */ 
  public Point2d
  getMinBounds()
  {
    return new Point2d(pMinBounds);
  }

  /** 
   * Get the maximum bounds of (2D position) of the job.
   */ 
  public Point2d
  getMaxBounds()
  {
    return new Point2d(pMaxBounds);
  }

  /**
   * Set the 2D position of the job (center of icon). 
   */
  public void 
  setBounds
  (
   Point2d minB,
   Point2d maxB   
  ) 
  {
    pMinBounds.set(minB);
    pMaxBounds.set(maxB);
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
   * Update the position, geometry and appearance based on the current state of the job.
   */ 
  public void 
  update() 
  {
    UserPrefs prefs = UserPrefs.getInstance();

    /* move/scale the job group so that it covers the bounds area */ 
    {
      Transform3D xform = new Transform3D();

      Vector3d scale = new Vector3d((pMaxBounds.x - pMinBounds.x), 
				    (pMaxBounds.y - pMinBounds.y), 1.0);
      xform.setScale(scale);

      Vector3d center = 
	new Vector3d((pMinBounds.x + pMaxBounds.x) * 0.5, 
		     (pMinBounds.y + pMaxBounds.y) * 0.5, 0.0);
      xform.setTranslation(center);

      pXform.setTransform(xform);
    }

    try { 
      /* have the line preferences changed? */ 
      boolean lineAprChanged = false;
      if((pLineAntiAlias != prefs.getJobViewerLineAntiAlias()) ||
	 (pLineThickness != prefs.getJobViewerLineThickness())) {

	pLineAntiAlias = prefs.getJobViewerLineAntiAlias();
	pLineThickness = prefs.getJobViewerLineThickness();

	lineAprChanged = true;
      }

      /* update border color */
      if(pModeChanged || lineAprChanged) {
	String cname = null;
	switch(pMode) {
	case Normal:
	  cname = "White";
	  break;

	case Selected:
	  cname = "Yellow";
	  break;

	case Primary:
	  cname = "Cyan";
	}

	Appearance apr = new Appearance();

	apr.setTexture(TextureMgr.getInstance().getSimpleTexture(cname));
	apr.setMaterial(null);
 
	{
	  LineAttributes la = new LineAttributes();
	  la.setLineAntialiasingEnable(pLineAntiAlias); 
	  la.setLineWidth((float) pLineThickness);
	  apr.setLineAttributes(la);
	}

	pBorderLines.setAppearance(apr);
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
   * The job group. 
   */ 
  private QueueJobGroup  pJobGroup; 


  /**
   * The set of viewer jobs which are members of this group indexed by <CODE>JobPath</CODE>.
   */ 
  private ArrayList<ViewerJob>  pViewerJobs; 


  /**
   * The UI selection mode.
   */
  private SelectionMode  pMode;  

  /**
   * Whether this geometry is currently visible.
   */
  private boolean  pIsVisible;     

  /**
   * the minimum/maximum bounds (2D position) of the job.
   */ 
  private Point2d   pMinBounds; 
  private Point2d   pMaxBounds; 

  /**
   * Whether the mode border color no longer valid for the current job status.
   */ 
  private boolean  pModeChanged;

  /**
   * Whether to anti-alias lines.
   */ 
  private boolean  pLineAntiAlias;

  /** 
   * The thickness of lines.
   */
  private double  pLineThickness;



  /**
   * The root branch group. 
   */ 
  private BranchGroup  pRoot;     

  /**
   * The switch group used to control job icon visbilty. 
   */ 
  private Switch  pSwitch;   

  /** 
   * The transform group used to position the job as a whole.
   */ 
  private TransformGroup  pXform;      

  /**
   * The job icon geometry.
   */ 
  private Shape3D  pShape;  

  /**
   * The job border lines geometry.
   */ 
  private Shape3D  pBorderLines;  

}
