// $Id: ViewerJobGroup.java,v 1.6 2004/12/31 08:56:25 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;

import java.io.*;
import java.util.*;

import net.java.games.jogl.*;

/*------------------------------------------------------------------------------------------*/
/*   V I E W E R   J O B   G R O U P                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Renders the current status of a job group as OpenGL geometry. <P> 
 */
public 
class ViewerJobGroup
  extends ViewerIcon  
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constuct a new viewer job. 
   * 
   * @param group
   *   The job group. 
   * 
   * @param vjobs
   *   The viewer jobs.
   * 
   * @param height
   *   The vertical job span of the icon.
   */ 
  public 
  ViewerJobGroup
  (
   QueueJobGroup group,
   ArrayList<ViewerJob> vjobs, 
   int height
  ) 
  {
    super();

    if(group == null) 
      throw new IllegalArgumentException("The job group cannot be (null)!");
    pJobGroup = group;

    if(vjobs == null) 
      throw new IllegalArgumentException("The viewer jobs cannot be (null)!");
    pViewerJobs = vjobs;

    pHeight = height;

    pLabelText = ("[" + group.getGroupID() + "]  " + group.getRootPattern());

    try {
      GeometryMgr mgr = GeometryMgr.getInstance();
      pLabelWidth = 0.35 * mgr.getTextWidth("CharterBTRoman", pLabelText, 0.05);
    }
    catch(IOException ex) {
      throw new IllegalArgumentException(ex.getMessage());
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


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the vertical job span of the icon.
   */ 
  public int
  getHeight()
  {
    return pHeight; 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the given position is inside the node icon.
   */ 
  public boolean
  isInside
  (
   Point2d pos
  ) 
  {       
    return getBounds().isInside(pos);      
  }

  /**
   * Whether any portion of the node icon is inside the given bounding box.
   */ 
  public boolean
  isInsideOf
  (
   BBox2d bbox
  ) 
  {
    return bbox.intersects(getBounds());
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the bounding box of the job group icon.
   */ 
  public BBox2d
  getBounds()
  {
    BBox2d bbox = new BBox2d(pPos, pPos);
    bbox.bloat(new Vector2d(0.5, 0.1875 * ((double) pHeight)));
    return bbox;
  }

  /**
   * Get the bounding box of all job group geometry. 
   */
  public BBox2d
  getFullBounds()
  {
    BBox2d bbox = getBounds();
    Point2d bmin = new Point2d(bbox.getMin().x(), bbox.getMax().y());
    bbox.grow(Point2d.add(bmin, new Vector2d(pLabelWidth, 0.45)));
    return bbox;
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the width of the label geometry.
   */ 
  public double
  getLabelWidth() 
  {
    return pLabelWidth;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   R E N D E R I N G                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Rebuild any OpenGL display list needed to render the node.
   *
   * @param gl
   *   The OpenGL interface.
   */ 
  public void 
  rebuild
  (
   GL gl
  )
  {
    GeometryMgr mgr = GeometryMgr.getInstance();
    try {
      if(pLabelDL == null) 
	pLabelDL = mgr.getTextDL(gl, "CharterBTRoman", pLabelText, 
				 GeometryMgr.TextAlignment.Left, 0.05);

      if(pIconDL == null) {
	pIconDL = new int[3];
	for(SelectionMode mode : SelectionMode.all()) 
	  pIconDL[mode.ordinal()] = 
	    mgr.getJobIconDL(gl, "Undefined-" + mode, false, pHeight);
      }

      if(pCollapsedDL == null) 
	pCollapsedDL = mgr.getNodeIconDL(gl, "Collapsed");
    }
    catch(IOException ex) {
      Logs.tex.severe(ex.getMessage());
    }
  }

  
  /**
   * Render the OpenGL geometry for the node.
   *
   * @param gl
   *   The OpenGL interface.
   */ 
  public void 
  render
  (
   GL gl
  )
  {
    gl.glPushMatrix();
    {
      gl.glTranslated(pPos.x(), pPos.y(), 0.0);
      gl.glCallList(pIconDL[pMode.ordinal()]);
      
      if(pIsCollapsed) {
	gl.glTranslated(0.8, 0.0, 0.0);
	gl.glCallList(pCollapsedDL);
	gl.glTranslated(-0.8, 0.0, 0.0);
      }

      {
	switch(pMode) {
	case Normal:
	  gl.glColor3d(1.0, 1.0, 1.0);
	  break;
	  
	case Selected:
	  gl.glColor3d(1.0, 1.0, 0.0);
	  break;
	  
	case Primary:
	  gl.glColor3d(0.0, 1.0, 1.0);
	}
	
	double dy = (0.1875 * ((double) pHeight)) + 0.1;
	gl.glTranslated(-0.5, dy, 0.0);
	gl.glScaled(0.35, 0.35, 0.35);
	gl.glCallList(pLabelDL);
      }
    }
    gl.glPopMatrix();
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
   * The vertical job span of the icon.
   */ 
  private int  pHeight;


  /**
   * The label text.
   */ 
  private String  pLabelText; 

  /**
   * The width of the label geometry.
   */ 
  private double  pLabelWidth;


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The OpenGL display list handle for the label geometry.
   */ 
  private Integer  pLabelDL; 

  /**
   * The OpenGL display list handles for the icon geometry. <P> 
   * 
   * The array contains the display lists corresponding to the Normal, Selected and Primary
   * selection modes.
   */ 
  private int[]  pIconDL; 
  
  /**
   * The OpenGL display list handle for the collapsed icon geometry.
   */ 
  private Integer  pCollapsedDL; 

}
