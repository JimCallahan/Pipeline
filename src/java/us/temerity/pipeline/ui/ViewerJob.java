// $Id: ViewerJob.java,v 1.2 2004/08/30 02:55:55 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.io.*;
import java.util.*;
import javax.vecmath.*;
import javax.media.j3d.*;
import com.sun.j3d.utils.geometry.*;

/*------------------------------------------------------------------------------------------*/
/*   V I E W E R   J O B                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A reusable Java3D based graphical representation of the status of a Pipeline job. <P> 
 * 
 * A <CODE>ViewerJob</CODE> is associated with a single <CODE>JobStatus</CODE>.  However, 
 * a <CODE>JobStatus</CODE> may be represented by more than one <CODE>ViewerJob</CODE>
 * if the job is reachable via multiple paths.  For this reason, each instance of this class 
 * maintains a {@link JobPath JobPath} field in addition to the current <CODE>JobStatus</CODE>
 * to identify the unique path from the root job of the 
 * {@link JQueueJobViewerPanel JQueueJobViewerPanel} to the <CODE>JobStatus</CODE> associated
 * with this <CODE>ViewerJob</CODE> instance.
 */
public 
class ViewerJob
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constuct a new viewer job. 
   */ 
  public 
  ViewerJob() 
  {
    /* initialize state fields */ 
    {
      pIsReset = true;

      pMode = SelectionMode.Normal;

      pLineAntiAlias = true;
      pLineThickness = 1.0;

      pIsVisible   = false;
      pIsCollapsed = false;
      pIsExternal  = false;

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
      
      /* the job icon */ 
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
      
      /* the job border lines */ 
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
      
      /* the collapsed upstream icon visibility switch */ 
      {
	pCollapsedSwitch = new Switch();
	pCollapsedSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
	pCollapsedSwitch.setPickable(false);

	pXform.addChild(pCollapsedSwitch);
      }
      
      /* the collapsed lines */ 
      {
	Appearance apr = new Appearance();

	LineArray la = new LineArray(6, LineArray.COORDINATES);

	la.setCoordinate(0, new Point3d(0.6, -0.3, 0.0));
	la.setCoordinate(1, new Point3d(0.9, 0.0, 0.0));

	la.setCoordinate(2, new Point3d(0.9, 0.0, 0.0));
	la.setCoordinate(3, new Point3d(0.6, 0.3, 0.0));

	la.setCoordinate(4, new Point3d(0.6, 0.3, 0.0));
	la.setCoordinate(5, new Point3d(0.6, -0.3, 0.0));
	
	pCollapsedLines = new Shape3D(la, apr);
	pCollapsedLines.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);

	pCollapsedSwitch.addChild(pCollapsedLines);
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
  }


  /**
   * Get the current job status. 
   */ 
  public JobStatus
  getJobStatus() 
  {
    return pStatus;
  }

  /**
   * Get the path from the root job to the current job.
   */ 
  public JobPath
  getJobPath() 
  {
    return pPath;
  }

  /**
   * Set the current job status and path.
   * 
   * @param status
   *   The current job status.
   * 
   * @param path
   *   The path from the root job to the current job.
   */ 
  public void 
  setCurrentState
  (
   JobStatus status, 
   JobPath path  
  ) 
  {
    if(status == null) 
      throw new IllegalArgumentException("The job status cannot be (null)!");

    if(path == null) 
      throw new IllegalArgumentException("The job path cannot be (null)!");

    pStatus = status;
    pPath   = path;

    {
      String name = pStatus.getState().toString();
      if(!name.equals(pIconAprName)) 
	pIconAprNameChanged = true;

      pIconAprName = name;
    }
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
   * Are upstream jobs collapsed?
   */ 
  public boolean 
  isCollapsed() 
  {
    return pIsCollapsed;
  }

  /**
   * Set whether upstream jobs are collapsed.
   */ 
  public void 
  setCollapsed
  (
   boolean tf  
  ) 
  {    
    pIsCollapsed = tf;
    pCollapsedSwitch.setWhichChild(pIsCollapsed ? Switch.CHILD_ALL : Switch.CHILD_NONE);
  }
  

  /**
   * Are upstream jobs external?
   */ 
  public boolean 
  isExternal() 
  {
    return pIsExternal;
  }

  /**
   * Set whether upstream jobs are external.
   */ 
  public void 
  setExternal
  (
   boolean tf  
  ) 
  {    
    pIsExternal = tf;
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
   * Get the maximum bounds of (2D position) of the job including collapsed geometry.
   */ 
  public Point2d
  getMaxCollapsedBounds()
  {
    if(!pIsCollapsed) 
      return getMaxBounds();

    double scale  = pMaxBounds.x - pMinBounds.x;
    double center = (pMinBounds.x + pMaxBounds.x) * 0.5;

    Point2d bounds = new Point2d(pMaxBounds);
    bounds.x = center + 0.9*scale;

    return bounds;
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

    /* move/scale the job so that it covers the bounds area */ 
    {
      Transform3D xform = new Transform3D();

      double factor = (pIsExternal ? 0.6 : 1.0);
      Vector3d scale = new Vector3d((pMaxBounds.x - pMinBounds.x) * factor, 
				    (pMaxBounds.y - pMinBounds.y) * factor, 1.0);
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

      /* update job border color */
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

      /* update the job icon appearance */ 
      if(pIconAprNameChanged) {
	AppearanceMgr mgr = AppearanceMgr.getInstance();
	Appearance apr = mgr.getLineAppearance(pIconAprName);
	pShape.setAppearance(apr);

	pIconAprNameChanged = false;
      }

      /* update the collapsed line appearances */ 
      if(lineAprChanged) {
	Appearance apr = new Appearance();
	
	apr.setTexture(TextureMgr.getInstance().getSimpleTexture("White"));
	apr.setMaterial(null);
	  
	{
	  LineAttributes la = new LineAttributes();
	  la.setLineAntialiasingEnable(pLineAntiAlias);
	  la.setLineWidth((float) (pLineThickness * 0.6));
	  apr.setLineAttributes(la);
	}
	  
	pCollapsedLines.setAppearance(apr);
      }
    }
    catch(IOException ex) {
      Logs.tex.severe("Internal Error:\n" + 
		      "  " + ex.getMessage());
      Logs.flush();
      System.exit(1);
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
   * The current job status.
   */ 
  private JobStatus  pStatus;

  /**
   * The path from the root job to the current job.
   */ 
  private JobPath  pPath;

  /**
   * The UI selection mode.
   */
  private SelectionMode  pMode;  

  /**
   * Whether this geometry is currently visible.
   */
  private boolean  pIsVisible;     

  /**
   * Whether upstream jobs are collapsed.
   */
  private boolean  pIsCollapsed;

  /**
   * Whether this is an external job.
   */
  private boolean  pIsExternal;

  /**
   * the minimum/maximum bounds (2D position) of the job.
   */ 
  private Point2d   pMinBounds; 
  private Point2d   pMaxBounds; 

  /**
   * The name of the icon appearance associated with the current job state.
   */ 
  private String  pIconAprName;

  /**
   * Whether the icon appearance name is no longer valid for the current job status.
   */ 
  private boolean  pIconAprNameChanged;

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

  /**
   * The switch group used to control collapsed icon visbilty. 
   */ 
  private Switch  pCollapsedSwitch; 

  /** 
   * The transform group used to position the collapsed icon.
   */ 
  private TransformGroup  pCollapsedXform;     

  /**
   * The collapsed lines geometry.
   */ 
  private Shape3D  pCollapsedLines;            

}
