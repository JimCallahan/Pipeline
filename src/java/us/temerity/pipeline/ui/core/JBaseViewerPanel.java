// $Id: JBaseViewerPanel.java,v 1.22 2009/12/11 04:21:11 jesse Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*; 
import us.temerity.pipeline.glue.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   V I E W E R   P A N E L                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The common base class of all OpenGL based viewer panels.
 */ 
public abstract
class JBaseViewerPanel
  extends JTopLevelPanel
  implements MouseListener, MouseMotionListener, GLEventListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new panel with the default working area view.
   */
  public 
  JBaseViewerPanel()
  {
    super();
  }

  /**
   * Construct a new panel with a working area view identical to the given panel.
   */
  public 
  JBaseViewerPanel
  (
   JTopLevelPanel panel
  )
  {
    super(panel);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the common user interface components.
   */ 
  protected void 
  initUI
  (
   double maxFactor,
   boolean enableRubberband
  )
  {  
    /* initialize fields */ 
    {
      pSceneDL = new AtomicInteger(0);
      pRefreshScene = true;

      pLayoutPolicy = LayoutPolicy.Preserve;

      pCameraPos = new Point3d(0.0, 0.0, -20.0);
      setFOV(90.0);
      setClip(0.1, 500.0);

      pCanvasToScreen = new Vector2d(1.0, 1.0);

      pRbEnabled = enableRubberband;

      pZoomSpeed = 0.1;
      pMaxFactor = maxFactor;
    }
  
    /* initialize the panel components */ 
    {
      setLayout(new BorderLayout());
      setMinimumSize(new Dimension(50, 50));
      setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
      
      if(PackageInfo.sUseJava2dGLPipeline) {
        GLJPanel gpanel = UIMaster.getInstance().createGLJPanel(); 

        pGLDrawable = gpanel;
	pGLDrawable.addGLEventListener(this);

        pGLComponent = gpanel;
	pGLComponent.addMouseListener(this);
	pGLComponent.addMouseMotionListener(this);
	pGLComponent.setFocusable(true);	

	add(gpanel);
      }
      else {
	GLCanvas canvas = UIMaster.getInstance().createGLCanvas(); 
        
        pGLDrawable = canvas;
        pGLDrawable.addGLEventListener(this);

        pGLComponent = canvas;
	pGLComponent.addMouseListener(this);
	pGLComponent.addMouseMotionListener(this);
	pGLComponent.setFocusable(true);	

	add(canvas);
      }
    }
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether rubberband selection is enabled.
   */ 
  public boolean
  isRubberbandEnabled() 
  {
    return pRbEnabled;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the vertical field-of-view (in degrees) of the canvas camera.
   */ 
  public void 
  setFOV
  (
   double degrees
  ) 
  {
    if((degrees <= 0.0) || (degrees >= 180.0)) 
      throw new IllegalArgumentException
	("The camera field-of-view must be in the range: (0.0, 180.0)");

    pFOV = degrees;
    pPerspFactor = Math.tan(Math.toRadians(pFOV * 0.5));
  }

  /**
   * Set the clipping plane distances.
   */ 
  public void 
  setClip
  (
   double near, 
   double far
  ) 
  {
    pNear = near;
    pFar  = far;
  }
  
  /**
   * Get scaling factor to go from canvas coordinates to world space.
   */ 
  public double 
  getCanvasScale() 
  {
    return pCameraPos.z() * pPerspFactor * pCanvasToScreen.y();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Refresh the scene geometry display list.
   */ 
  protected void
  refresh()
  {
    pRefreshScene = true;
    
    pGLDrawable.repaint();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Perform any operations needed before an panel operation starts. <P> 
   * 
   * This method is run by the Swing Event thread.
   */ 
  public void 
  prePanelOp() 
  {
    super.prePanelOp(); 
  
    if(pGLComponent != null) 
      pGLComponent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); 
  }

  /**
   * Perform any operations needed after an panel operation has completed. <P> 
   * 
   * This method is run by the Swing Event thread.
   */ 
  public void 
  postPanelOp() 
  {
    if(pGLComponent != null) 
      pGLComponent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));  
    
    super.postPanelOp(); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- GL EVENT LISTENER METHODS -----------------------------------------------------------*/

  /**
   * Called by the drawable immediately after the OpenGL context is initialized for the 
   * first time.
   */ 
  public void 
  init
  (
   GLAutoDrawable drawable
  )
  {    
    if(UIMaster.getInstance().getDebugGL()) 
      drawable.setGL(new DebugGL(drawable.getGL()));

    if(UIMaster.getInstance().getTraceGL()) 
      drawable.setGL(new TraceGL(drawable.getGL(), System.err));

    GL gl = drawable.getGL();

    /* global OpenGL state */ 
    {
      gl.glClearColor(0.5f, 0.5f, 0.5f, 0.0f);
      gl.glEnable(GL.GL_BLEND);
      gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
    }
  }

  /**
   * Called by the drawable to initiate OpenGL rendering by the client.
   */ 
  public void 
  display
  (
   GLAutoDrawable drawable
  )
  {
    GL gl   = drawable.getGL();
    GLU glu = new GLU();

    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

    /* reset camera projection & clipping planes */ 
    {
      setClip(Math.max(0.001, -pCameraPos.z()-2.0), 
	      Math.max(2.0, -pCameraPos.z()+2.0));
      
      gl.glMatrixMode(GL.GL_PROJECTION);
      gl.glLoadIdentity();
      glu.gluPerspective(pFOV, pAspect, pNear, pFar);
    }

    /* set camera position */ 
    {
      gl.glMatrixMode(GL.GL_MODELVIEW);
      gl.glLoadIdentity();
      gl.glTranslated(pCameraPos.x(), pCameraPos.y(), pCameraPos.z());
    }

    /* draw rubber band geometry */ 
    if(pRbEnabled && (pRbStart != null) && (pRbEnd != null)) {

      /* compute world space coordinates */ 
      Point2d rs = new Point2d(pRbStart);
      Point2d re = new Point2d(pRbEnd);
      {
	Dimension size = pGLComponent.getSize();
	Vector2d half = new Vector2d(size.getWidth()*0.5, size.getHeight()*0.5);
	
	double f = -pCameraPos.z() * pPerspFactor;
	Vector2d persp = new Vector2d(f * pAspect, f);
	
	Vector2d camera = new Vector2d(pCameraPos.x(), pCameraPos.y());

	rs.sub(half).mult(pCanvasToScreen).mult(persp).sub(camera);
	re.sub(half).mult(pCanvasToScreen).mult(persp).sub(camera);
      }
	
      /* render the rubberband */ 
      {
	gl.glColor3d(1.0, 1.0, 0.0);

	gl.glBegin(GL.GL_LINE_LOOP);
	{
	  gl.glVertex3d(rs.x(), rs.y(), 0.0);
	  gl.glVertex3d(re.x(), rs.y(), 0.0);
	  gl.glVertex3d(re.x(), re.y(), 0.0);
	  gl.glVertex3d(rs.x(), re.y(), 0.0);
	}
	gl.glEnd();

	gl.glColor3d(1.0, 1.0, 1.0);
      }
    }
  }
   
  /**
   * Called by the drawable during the first repaint after the component has been resized.
   */ 
  public void 
  reshape
  (
   GLAutoDrawable drawable, 
   int x, 
   int y, 
   int width, 
   int height
  )
  {
    GL  gl  = drawable.getGL();
    GLU glu = new GLU(); 

    double w = (double) width;
    double h = (double) height;
    
    pAspect = w / h;
    pCanvasToScreen.set(2.0/w, -2.0/h);

    gl.glMatrixMode(GL.GL_PROJECTION);
    gl.glLoadIdentity();
    glu.gluPerspective(pFOV, pAspect, pNear, pFar);
    
    double dist = -pCameraPos.z();
    dist = Math.max(((double) pGLComponent.getHeight()) / pMaxFactor, dist); 
    if(Double.isNaN(dist) || Double.isInfinite(dist))
      dist = 20.0; 
    pCameraPos.z(-dist);
  }
 
  /** 
   * Called by the drawable when the display mode or the display device associated with 
   * the GLAutoDrawable has changed.
   */ 
  public void 
  displayChanged
  (
   GLAutoDrawable drawable, 
   boolean modeChanged, 
   boolean deviceChanged
  )
  {}

  /**
   * Return the previously allocated OpenGL display lists to the pool of display lists to be 
   * reused. 
   */ 
  public void 
  freeDisplayLists() 
  {
    UIMaster.getInstance().freeDisplayList(pSceneDL.getAndSet(0));
  }

  

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
  mouseEntered
  (
   MouseEvent e
  ) 
  {
    pGLComponent.requestFocusInWindow();

    {
      Point p = e.getPoint();
      pMousePos = new Point2d(p.getX(), p.getY());
    }
  }
  
  /**
   * Invoked when the mouse exits a component. 
   */ 
  public void 
  mouseExited
  (
   MouseEvent e
  ) 
  {
    KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
  }

  /**
   * Invoked when a mouse button has been pressed on a component. 
   */
  public void 
  mousePressed
  (
   MouseEvent e 
  ) 
  {
    handleMousePressed(e);
  }

  /**
   * Handle viewer related mouse press events.
   * 
   * @return 
   *   Whether the event was handled.
   */ 
  protected boolean
  handleMousePressed
  (
   MouseEvent e 
  ) 
  {
    int mods = e.getModifiersEx();

    {
      Point p = e.getPoint();
      pMousePos = new Point2d(p.getX(), p.getY());
    }

    boolean rb   = false;
    boolean pan  = false;
    boolean zoom = false;
    {
      /* BUTTON1[+SHIFT[+CTRL]]: rubber band start */ 
      {
	int on1  = (MouseEvent.BUTTON1_DOWN_MASK);
	
	int off1 = (MouseEvent.BUTTON2_DOWN_MASK | 
		    MouseEvent.BUTTON3_DOWN_MASK | 
		    MouseEvent.SHIFT_DOWN_MASK |
		    MouseEvent.ALT_DOWN_MASK |
		    MouseEvent.CTRL_DOWN_MASK);
	
	int on2  = (MouseEvent.BUTTON1_DOWN_MASK |
		    MouseEvent.SHIFT_DOWN_MASK);
	
	int off2 = (MouseEvent.BUTTON2_DOWN_MASK | 
		    MouseEvent.BUTTON3_DOWN_MASK | 
		    MouseEvent.ALT_DOWN_MASK |
		    MouseEvent.CTRL_DOWN_MASK);
	
	int on3  = (MouseEvent.BUTTON1_DOWN_MASK |
		    MouseEvent.SHIFT_DOWN_MASK |
		    MouseEvent.CTRL_DOWN_MASK);
	
	int off3 = (MouseEvent.BUTTON2_DOWN_MASK | 
		    MouseEvent.BUTTON3_DOWN_MASK | 
		    MouseEvent.ALT_DOWN_MASK);

	rb = (pRbEnabled && 
	      (((mods & (on1 | off1)) == on1) || 
	      ((mods & (on2 | off2)) == on2) || 
	      ((mods & (on3 | off3)) == on3)));
      }
      
      /* <BUTTON2+ALT>: pan start */ 
      {
	int on1  = (MouseEvent.BUTTON2_DOWN_MASK |
		    MouseEvent.ALT_DOWN_MASK);
	
	int off1 = (MouseEvent.BUTTON1_DOWN_MASK | 
		    MouseEvent.BUTTON3_DOWN_MASK | 
		    MouseEvent.SHIFT_DOWN_MASK |
		    MouseEvent.CTRL_DOWN_MASK);

	pan = ((mods & (on1 | off1)) == on1);
      }
	
      /* <BUTTON1+BUTTON2+ALT>: zoom start */ 
      switch(PackageInfo.sOsType) {
      case Unix:
      case Windows:
	{
	  int on1  = (MouseEvent.BUTTON1_DOWN_MASK |
		      MouseEvent.BUTTON2_DOWN_MASK | 
		      MouseEvent.ALT_DOWN_MASK);
	  
	  int off1 = (MouseEvent.BUTTON3_DOWN_MASK | 
		      MouseEvent.SHIFT_DOWN_MASK |
		      MouseEvent.CTRL_DOWN_MASK);
	  
	  zoom = ((mods & (on1 | off1)) == on1);
	}
	break;

      case MacOS:
	/* See 453 - Mac OS X Viewer Zoom Unresponsive */ 
	{
	  int on1  = (MouseEvent.BUTTON1_DOWN_MASK |
		      MouseEvent.ALT_DOWN_MASK);
	  
	  int off1 = (MouseEvent.BUTTON2_DOWN_MASK | 
		      MouseEvent.BUTTON3_DOWN_MASK | 
		      MouseEvent.SHIFT_DOWN_MASK |
		      MouseEvent.CTRL_DOWN_MASK);
	  
	  zoom = ((mods & (on1 | off1)) == on1);
	}
      }	
    }
      
    if(rb) {
      pRbStart = new Point2d(pMousePos);
      pRbEnd   = null;

      return true;
    }
    else if(pan || zoom) {
      if(!isPanelOpInProgress()) {
        if(pan) 
          pGLComponent.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        else 
          pGLComponent.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
      }
      
      pDragStart = new Point2d(pMousePos);

      return true;
    }

    return false;
  }

  /**
   * Invoked when a mouse button has been released on a component. 
   */ 
  public void 
  mouseReleased(MouseEvent e) {}


  /*-- MOUSE MOTION LISTNER METHODS --------------------------------------------------------*/
  
  /**
   * Invoked when a mouse button is pressed on a component and then dragged. 
   */ 
  public void 	
  mouseDragged
  (
   MouseEvent e
  )
  {
    int mods = e.getModifiersEx();

    boolean rb   = false;
    boolean pan  = false;
    boolean zoom = false;
    {
      /* BUTTON1[+SHIFT[+CTRL]]: rubber band start */ 
      {
	int on1  = (MouseEvent.BUTTON1_DOWN_MASK);
	
	int off1 = (MouseEvent.BUTTON2_DOWN_MASK | 
		    MouseEvent.BUTTON3_DOWN_MASK | 
		    MouseEvent.SHIFT_DOWN_MASK |
		    MouseEvent.ALT_DOWN_MASK |
		    MouseEvent.CTRL_DOWN_MASK);
	
	int on2  = (MouseEvent.BUTTON1_DOWN_MASK |
		    MouseEvent.SHIFT_DOWN_MASK);
	
	int off2 = (MouseEvent.BUTTON2_DOWN_MASK | 
		    MouseEvent.BUTTON3_DOWN_MASK | 
		    MouseEvent.ALT_DOWN_MASK |
		    MouseEvent.CTRL_DOWN_MASK);
	
	int on3  = (MouseEvent.BUTTON1_DOWN_MASK |
		    MouseEvent.SHIFT_DOWN_MASK |
		    MouseEvent.CTRL_DOWN_MASK);
	
	int off3 = (MouseEvent.BUTTON2_DOWN_MASK | 
		    MouseEvent.BUTTON3_DOWN_MASK | 
		    MouseEvent.ALT_DOWN_MASK);

	rb = (pRbEnabled && 
	      (((mods & (on1 | off1)) == on1) || 
	      ((mods & (on2 | off2)) == on2) || 
	      ((mods & (on3 | off3)) == on3)));
      }
      
      /* <BUTTON2+ALT>: pan start */ 
      {
	int on1  = (MouseEvent.BUTTON2_DOWN_MASK |
		    MouseEvent.ALT_DOWN_MASK);
	
	int off1 = (MouseEvent.BUTTON1_DOWN_MASK | 
		    MouseEvent.BUTTON3_DOWN_MASK | 
		    MouseEvent.SHIFT_DOWN_MASK |
		    MouseEvent.CTRL_DOWN_MASK);

	pan = ((mods & (on1 | off1)) == on1);
      }
	
      /* <BUTTON1+BUTTON2+ALT>: zoom start */ 
      switch(PackageInfo.sOsType) {
      case Unix:
      case Windows:
	{
	  int on1  = (MouseEvent.BUTTON1_DOWN_MASK |
		      MouseEvent.BUTTON2_DOWN_MASK | 
		      MouseEvent.ALT_DOWN_MASK);
	  
	  int off1 = (MouseEvent.BUTTON3_DOWN_MASK | 
		      MouseEvent.SHIFT_DOWN_MASK |
		      MouseEvent.CTRL_DOWN_MASK);
	  
	  zoom = ((mods & (on1 | off1)) == on1);
	}
	break;

      case MacOS:
	/* See 453 - Mac OS X Viewer Zoom Unresponsive */ 
	{
	  int on1  = (MouseEvent.BUTTON1_DOWN_MASK |
		      MouseEvent.ALT_DOWN_MASK);
	  
	  int off1 = (MouseEvent.BUTTON2_DOWN_MASK | 
		      MouseEvent.BUTTON3_DOWN_MASK | 
		      MouseEvent.SHIFT_DOWN_MASK |
		      MouseEvent.CTRL_DOWN_MASK);
	  
	  zoom = ((mods & (on1 | off1)) == on1);
	}
      }	
    }

    if((pRbStart != null) && rb) {
      Point p = e.getPoint();
      pRbEnd = new Point2d(p.getX(), p.getY());
    }
    else {
      pRbStart = null;
      pRbEnd   = null;
    }

    if(pDragStart != null) {
      Point p = e.getPoint();
      Point2d pos = new Point2d(p.getX(), p.getY());

      if(pan) {
	double f = -pCameraPos.z() * pPerspFactor;
	Vector2d persp = new Vector2d(f * pAspect, f);

	Vector2d delta = new Vector2d(pDragStart, pos);	
	delta.mult(pCanvasToScreen).mult(persp);
	
	pCameraPos.add(new Vector3d(delta.x(), delta.y(), 0.0));

        pCameraMovedSinceFramed = true;

	pDragStart = pos;
      }
      else if(zoom) {
	Vector2d delta = new Vector2d(pDragStart, pos);	

	double zm = delta.y();
	if(Math.abs(delta.x()) > Math.abs(delta.y())) 
	  zm = -delta.x();
	
	double dist = -pCameraPos.z();
	dist += pZoomSpeed*zm;
	dist = Math.max(((double) pGLComponent.getHeight()) / pMaxFactor, dist); 
        if(Double.isNaN(dist) || Double.isInfinite(dist))
          dist = 20.0; 
	pCameraPos.z(-dist);

        pCameraMovedSinceFramed = true;

	pDragStart = pos;
      } 
    }

    if(rb || pan || zoom) 
      pGLDrawable.repaint();     
  }

  /**
   * Invoked when the mouse cursor has been moved onto a component but no buttons have 
   * been pushed. 
   */ 
  public void 	
  mouseMoved 
  (
   MouseEvent e
  ) 
  {
    Point p = e.getPoint();
    pMousePos = new Point2d(p.getX(), p.getY());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Move the camera to frame the given bounding box.
   */ 
  protected void 
  doFrameBounds
  (
   BBox2d bbox
  ) 
  {
    if(bbox == null) 
      return; 

    Vector2d hrange = bbox.getRange();
    hrange.mult(0.5);

    double ftan = Math.tan(Math.toRadians(pFOV)*0.5);
    double distX = hrange.x() / (pAspect * ftan);
    double distY = hrange.y() / ftan;
    double z = Math.max(((double) pGLComponent.getHeight()) / pMaxFactor, 
                        Math.max(distX, distY));
    if(Double.isNaN(z) || Double.isInfinite(z))
      z = 20.0; 

    Point2d center = bbox.getCenter();
    pCameraPos.set(center.x(), center.y(), z);
    pCameraPos.negate();

    pCameraMovedSinceFramed = false;

    pGLDrawable.repaint();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  public synchronized void 
  toGlue
  ( 
   GlueEncoder encoder   
  ) 
    throws GlueException
  {
    super.toGlue(encoder);

    encoder.encode("CameraPos", pCameraPos);
  }

  public synchronized void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    Point3d pos = (Point3d) decoder.decode("CameraPos");
    if(pos != null) 
      pCameraPos = pos;

    super.fromGlue(decoder);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The expand/collapse policy to use when laying out icons.
   */
  protected 
  enum LayoutPolicy
  {  
    /**
     * Preserve the collapse mode of existing icons and use an AutomaticExpand policy for 
     * any newly created icons.
     */
    Preserve, 

    /**
     * Expand all icons.
     */ 
    ExpandAll, 

    /**
     * Collapse all icons.
     */ 
    CollapseAll, 
    
    /**
     * Expand the first occurance of a icon and collapse all subsequence occurances.
     */
    AutomaticExpand;
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  //private static final long serialVersionUID = 



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The OpenGL rendering area.<P> 
   * 
   * The two fields containg the same underlying instance of either JGLPanel or GLCanvas
   * depending on whether the Java2d OpenGL rendering pipeline is enabled.  These fields 
   * provide a common interface to the shared methods of these two types of instances 
   * even though they do not share any common superclasses.
   */ 
  protected GLAutoDrawable pGLDrawable;
  protected Component      pGLComponent;

  /**
   * The OpenGL display list handle for the scene geometry.
   */ 
  protected AtomicInteger  pSceneDL; 

  /**
   * Whether the OpenGL display list for the scene geometry needs to be rebuilt.
   */ 
  protected boolean  pRefreshScene;


  /**
   * A fixed depth of nodes to expand or <CODE>null</CODE> to use the LayoutPolicy to 
   * determine whether to expand/collapse.
   */ 
  protected Integer  pExpandDepth; 

  /**
   * The expand/collapse policy to use during layout.
   */ 
  protected LayoutPolicy  pLayoutPolicy;

  /**
   * The last known mouse position in canvas coordinates.
   */ 
  protected Point2d pMousePos;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The camera position in world space coordinates.
   */ 
  protected Point3d  pCameraPos; 

  /** 
   * Whether the camera has been manually moved since the last time a doFramBound() operation
   * was performed.
   */ 
  protected boolean  pCameraMovedSinceFramed; 

  /** 
   * The vertical field-of-view in degrees.
   */ 
  protected double pFOV; 

  /**
   * The perspective correction factor: tan(FOV / 2)
   */ 
  protected double pPerspFactor; 

  /**
   * The canvas aspect ratio: width / height
   */ 
  protected double pAspect;

  /**
   * The distance to the near clipping plane.
   */ 
  protected double pNear;

  /**
   * The distance to the far clipping plane.
   */ 
  protected double pFar;

  /**
   * Conversion factor from canvas coordinates (in pixels) to normalized screen space 
   * coordinates: [-1.0, 1.0] in both X and Y.
   */ 
  protected Vector2d  pCanvasToScreen;


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether rubberband selection is enabled.
   */ 
  private boolean pRbEnabled; 

  /**
   * The location of the start of a rubberband drag in canvas coordinates.
   */ 
  protected Point2d  pRbStart;  

  /**
   * The location of the end of a rubberband drag in canvas coordinates.
   */
  protected Point2d  pRbEnd;  


  /*----------------------------------------------------------------------------------------*/

  /**
   * The location of the start of a mouse drag in canvas coordinates.
   */ 
  private Point2d  pDragStart;  

  /**
   * The camera zoom speed scale factor. 
   */ 
  private double  pZoomSpeed;  

  /**
   * The maximum camera zoom factor. 
   */ 
  protected double  pMaxFactor; 


}
