// $Id: RubberBand.java,v 1.1 2004/05/16 19:21:38 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.io.*;
import java.util.*;
import javax.vecmath.*;
import javax.media.j3d.*;
import com.sun.j3d.utils.geometry.*;

/*------------------------------------------------------------------------------------------*/
/*   R U B B E R   B A N D                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The selection rubber band.
 */
public 
class RubberBand
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constuct a new rubber band. 
   */ 
  public 
  RubberBand() 
  {
    /* initialize the Java3D geometry */ 
    {
      /* the root branch group */ 
      pRoot = new BranchGroup();
      
      /* the visibility switch */ 
      {
	pSwitch = new Switch();
	pSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
	pSwitch.setWhichChild(Switch.CHILD_NONE);

	pRoot.addChild(pSwitch);
      }
      
      /* the outline geometry */ 
      try {
	Appearance apr = new Appearance();
	{
	  apr.setTexture(TextureMgr.getInstance().getSimpleTexture("Yellow"));
	  apr.setMaterial(null);
	  
	  LineAttributes la = new LineAttributes();
	  la.setLineAntialiasingEnable(true);
	  la.setLineWidth(1.0f);
	  apr.setLineAttributes(la);
	}

	int cnts[] = { 5 };
	pLineGeom = new LineStripArray(5, LineArray.COORDINATES, cnts);
	pLineGeom.setCoordinate(0, new Point3d(-0.5, -0.5, 0.0));
	pLineGeom.setCoordinate(1, new Point3d( 0.5, -0.5, 0.0));
	pLineGeom.setCoordinate(2, new Point3d( 0.5,  0.5, 0.0));
	pLineGeom.setCoordinate(3, new Point3d(-0.5,  0.5, 0.0));
	pLineGeom.setCoordinate(4, new Point3d(-0.5, -0.5, 0.0));
	pLineGeom.setCapability(LineStripArray.ALLOW_COORDINATE_WRITE);
      
	Shape3D shape = new Shape3D(pLineGeom, apr);

	pSwitch.addChild(shape);
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
   * Get the root group which contains all Java3D geometry.
   */ 
  public BranchGroup 
  getBranchGroup() 
  {
    return pRoot;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   D R A G   O P E R A T I O N                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Begin a new drag operation at the given position in canvas coordinates.
   * 
   * @param pos
   *   The start position of the rubber band.
   */
  public void 
  beginDrag
  (
   Point2d pos    
  ) 
  {
    pStartPos = pos;
    pEndPos   = null;

    pBBox = null;
  }

  /**
   * Is a drag operation in progress?
   */ 
  public boolean
  isDragging() 
  {
    return (pStartPos != null);
  }

  /** 
   * Update the drag end position in canvas coordinates.
   * 
   * @param canvas
   *   The canvas upon which the rubber band is drawn.
   * 
   * @param pos
   *   The current end position of the rubber band.
   */ 
  public void 
  updateDrag
  (
   Canvas3D canvas, 
   Point2d pos    
  ) 
  {
    pEndPos = pos;
    
    /* common geometry info */ 
    Point3d eyePos   = new Point3d();
    Point3d startPos = new Point3d();
    Point3d endPos   = new Point3d();
    
    canvas.getCenterEyeInImagePlate(eyePos);
    canvas.getPixelLocationInImagePlate(pStartPos, startPos);
    canvas.getPixelLocationInImagePlate(pEndPos, endPos);
    
    Transform3D motion = new Transform3D();
    canvas.getImagePlateToVworld(motion);
    motion.transform(eyePos);
    motion.transform(startPos);
    motion.transform(endPos);

    /* update the geometry */ 
    {
      Vector3d startDir = new Vector3d(startPos);
      startDir.sub(eyePos);
      startDir.scale((eyePos.z-1.0) / startDir.z);
      
      Vector3d endDir = new Vector3d(endPos);
      endDir.sub(eyePos);
      endDir.scale((eyePos.z-1.0) / endDir.z);
      
      Point3d startCorner = new Point3d(eyePos);
      startCorner.sub(startDir);
      
      Point3d endCorner = new Point3d(eyePos);
      endCorner.sub(endDir);
      
      pLineGeom.setCoordinate(0, startCorner);
      pLineGeom.setCoordinate(1, new Point3d(startCorner.x, endCorner.y, 1.0));
      pLineGeom.setCoordinate(2, endCorner);
      pLineGeom.setCoordinate(3, new Point3d(endCorner.x, startCorner.y, 1.0));
      pLineGeom.setCoordinate(4, startCorner);
    }
    
    /* recompute the bounding box */ 
    {
      Vector3d startDir = new Vector3d(startPos);
      startDir.sub(eyePos);
      startDir.scale(eyePos.z / startDir.z);
      
      Vector3d endDir = new Vector3d(endPos);
      endDir.sub(eyePos);
      endDir.scale(eyePos.z / endDir.z);
      
      Point3d startCorner = new Point3d(eyePos);
      startCorner.sub(startDir);
      startCorner.z = -1.0;
      
      Point3d endCorner = new Point3d(eyePos);
      endCorner.sub(endDir);
      endCorner.z = 1.0;
      
      pBBox = new BoundingBox();
      pBBox.setLower(startCorner);
      pBBox.setUpper(startCorner);
      pBBox.combine(endCorner);      
    }
    
    /* show the geometry */ 
    if(!pIsVisible) {
      pIsVisible = true;
      pSwitch.setWhichChild(Switch.CHILD_ALL);
    }
  }

  /** 
   * End the current drag operation.
   * 
   * @return 
   *   The world space selection bounding box defined by the drag operation.
   */
  public BoundingBox
  endDrag() 
  {
    pStartPos = null;
    pEndPos   = null;

    if(pIsVisible) {
      pIsVisible = false;
      pSwitch.setWhichChild(Switch.CHILD_NONE);
    }

    return pBBox;
  }




  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the rubber band outline is visible.
   */
  private boolean  pIsVisible;     

  /**
   * The start position of a rubber band drag in canvas coordinates.
   */ 
  private Point2d  pStartPos; 

  /**
   * The end position of a rubber band drag in canvas coordinates.
   */ 
  private Point2d  pEndPos; 

  /**
   * The world space bounding box enclosed by the rubber band drag operation.
   */ 
  private BoundingBox  pBBox;       


  /**
   * The root branch group. 
   */ 
  private BranchGroup  pRoot;     

  /**
   * The switch group used to control rubber band outline visbilty. 
   */ 
  private Switch  pSwitch;   

  /**
   * The rubber band outline geometry.
   */ 
  private LineStripArray  pLineGeom;

}
