// $Id: ViewerJob.java,v 1.4 2007/01/05 23:46:10 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;

import java.io.*;
import java.util.*;

import javax.media.opengl.*;

/*------------------------------------------------------------------------------------------*/
/*   V I E W E R   J O B                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Renders the current status of a job as OpenGL geometry. <P> 
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
  extends ViewerIcon
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constuct a new viewer job. 
   * 
   * @param path
   *   The path from the root job to the current job.
   * 
   * @param status
   *   The current job status.
   * 
   * @param isExternal
   *   Whether the job is external to the parent job group.
   */ 
  public 
  ViewerJob
  (
   JobPath path, 
   JobStatus status, 
   boolean isExternal
  )    
  {
    super();

    if(status == null) 
      throw new IllegalArgumentException("The job status cannot be (null)!");
    pPath = path;
    
    if(path == null) 
      throw new IllegalArgumentException("The job path cannot be (null)!");
    pStatus = status;

    pIsExternal = isExternal;
    pHeight = 1;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

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


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the job is external to the parent job group.
   */ 
  public boolean 
  isExternal() 
  {
    return pIsExternal;
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

  /**
   * Set the vertical job span of the icon.
   */
  public void 
  setHeight
  (
   int height
  ) 
  {
    pHeight = height;
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
   * Get the bounding box of the job icon.
   */ 
  public BBox2d
  getBounds()
  {
    BBox2d bbox = new BBox2d(pPos, pPos);
    bbox.bloat(new Vector2d(0.5, 0.1875 * ((double) pHeight)));
    return bbox;
  }

  /** 
   * Get the bounding box of all job geometry. 
   */ 
  public BBox2d
  getFullBounds()
  {
    BBox2d bbox = getBounds();
    if(pIsCollapsed) 
      bbox.setMax(Point2d.add(bbox.getMax(), new Vector2d(0.6, 0.0)));
    return bbox;
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
      if(pIconDL == null) {
	pIconDL = new int[3];
	for(SelectionMode mode : SelectionMode.all()) 
	  pIconDL[mode.ordinal()] = 
	    mgr.getJobIconDL(gl, pStatus.getState() + "-" + mode, pIsExternal, pHeight);
      }

      if(pCollapsedDL == null) 
	pCollapsedDL = mgr.getNodeIconDL(gl, "Collapsed");
    }
    catch(IOException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Tex, LogMgr.Level.Severe,
	 ex.getMessage());
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
      }
    }
    gl.glPopMatrix();
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The path from the root job to the current job.
   */ 
  private JobPath  pPath;

  /**
   * The current job status.
   */ 
  private JobStatus  pStatus;

  /**
   * Whether this is an external job.
   */
  private boolean  pIsExternal;

  /**
   * The vertical job span of the icon.
   */ 
  private int  pHeight;


  /*----------------------------------------------------------------------------------------*/
  
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
