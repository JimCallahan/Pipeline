// $Id: JBalanceGroupHistPanel.java,v 1.2 2009/12/16 04:13:34 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.media.opengl.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;

/*------------------------------------------------------------------------------------------*/
/*   B A L A N C E   G R O U P   H I S T   P A N E L                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Panel that displays the histograms in the {@link JManageBalanceGroupsDialog}.
 */
public 
class JBalanceGroupHistPanel
  extends JBaseViewerPanel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new panel with the default working area view.
   */
  public 
  JBalanceGroupHistPanel()
  {
    super();
    initUI();
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Initialize the common user interface components.
   */ 
  private synchronized void 
  initUI()
  {  
    super.initUI(128.0, false);
    
    {
      pUserUseHistogram = null;
      pUserShareHistogram = null;
      pUsersByShareHistogram = null;
      
      pViewerPies = new ArrayList<ViewerPie>();
    }
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   U P D A T E                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the histograms.
   */
  public synchronized void
  updateHistograms
  (
    Histogram userShareHist,
    Histogram userUseHist,
    Histogram usersByShareHist
  )
  {
    pUserShareHistogram    = userShareHist;
    pUserUseHistogram      = userUseHist;
    pUsersByShareHistogram = usersByShareHist;
    
    /* remove all previous pie charts */ 
    pViewerPies.clear();
    {
      Point2d pos = new Point2d();
      double dx = 7.5; 
      {
        ViewerPie pie = new ViewerPie(pUsersByShareHistogram, false);
        pViewerPies.add(pie);
        pie.setColors(new Color3d(0d, .75d, 0d), new Color3d(.1, .2, .1));
        pie.setPosition(pos);
        pos.add(new Vector2d(dx, 0.0));
      }
      {
        ViewerPie pie = new ViewerPie(pUserShareHistogram, false);
        pViewerPies.add(pie);
        pie.setColors(new Color3d(0d, .75d, 0d), new Color3d(.1, .2, .1));
        pie.setPosition(pos);
        pos.add(new Vector2d(dx, 0.0));
      }
      {
        ViewerPie pie = new ViewerPie(pUserUseHistogram, false);
        pViewerPies.add(pie);
        pie.setColors(new Color3d(0d, .75d, 0d), new Color3d(.1, .2, .1));
        pie.setPosition(pos);
        pos.add(new Vector2d(dx, 0.0));
      }
    }
    
    /* render the changes */ 
    refresh();
  }
  
  /**
   * Get the bounding box which contains all of the pie charts. <P> 
   * 
   * @return 
   *   The bounding box or <CODE>null</CODE> if no pie charts are displayed. 
   */ 
  private BBox2d
  getChartBounds() 
  {
    BBox2d bbox = null;
    for(ViewerPie vpie : pViewerPies) {
      if(bbox == null) 
        bbox = vpie.getFullBounds();
      else 
        bbox.grow(vpie.getFullBounds());
    }
    
    return bbox;
  }
  
  /**
   * Move the camera to frame all visible pie charts.
   */ 
  public synchronized void 
  frameAll() 
  {
    doFrameBounds(getChartBounds());
  }  
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- GL EVENT LISTENER METHODS -----------------------------------------------------------*/

  /**
   * Called by the drawable immediately after the OpenGL context is initialized for the 
   * first time.
   */ 
  @Override
  public void 
  init
  (
   GLAutoDrawable drawable
  )
  {    
    super.init(drawable);
    GL gl = drawable.getGL();

    /* global OpenGL state */ 
    {
      gl.glEnable(GL.GL_LINE_SMOOTH);
      gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_DONT_CARE);
    }
  }
  
  /**
   * Called by the drawable to initiate OpenGL rendering by the client.
   */ 
  @Override
  public void 
  display
  (
   GLAutoDrawable drawable
  )
  {
    super.display(drawable); 
    GL gl = drawable.getGL();

    /* render the scene geometry */ 
    {
      if(pRefreshScene) {
        rebuildAll(gl);

        {
          UIMaster master = UIMaster.getInstance(); 
          master.freeDisplayList(pSceneDL.getAndSet(master.getDisplayList(gl)));
        }

        gl.glNewList(pSceneDL.get(), GL.GL_COMPILE_AND_EXECUTE);
          renderAll(gl);
        gl.glEndList();

        pRefreshScene = false;
      }
      else {
        gl.glCallList(pSceneDL.get());
      }
    }    
  }
  
  /** 
   * Synchronized display list building helper.
   */ 
  private synchronized void
  rebuildAll
  (
   GL gl
  ) 
  {
    for(ViewerPie vpie : pViewerPies) 
      vpie.rebuild(gl);
  }
  
  /** 
   * Synchronized rendering helper.
   */ 
  private synchronized void
  renderAll
  (
   GL gl
  ) 
  {
    for(ViewerPie vpie : pViewerPies) 
      vpie.render(gl);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /*-- MOUSE LISTENER METHODS --------------------------------------------------------------*/
  
  @Override
  public void 
  mouseReleased
  (
    MouseEvent e
  )
  {
    super.mouseReleased(e);
    
    pGLComponent.setCursor(Cursor.getDefaultCursor());
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -2934611020498546824L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Histogram that displays each user's share of the queue.
   */
  private Histogram pUserShareHistogram;
  
  /**
   * Histogram that displays each user's current use of the queue.
   */
  private Histogram pUserUseHistogram;
  
  /**
   * Histogram that displays the number of users who have each share.
   */
  private Histogram pUsersByShareHistogram; 
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * The pie chart graphics which display the histogram data in the order they should be 
   * displayed.
   */ 
  private ArrayList<ViewerPie>  pViewerPies;
}
