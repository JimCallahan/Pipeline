// $Id: JBaseViewerPanel.java,v 1.7 2004/12/31 22:38:20 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.math.*; 
import us.temerity.pipeline.glue.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import net.java.games.jogl.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   V I E W E R   P A N E L                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The commoing base class of all OpenGL based viewer panels.
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
   double maxFactor
  )
  {  
    /* initialize fields */ 
    {
      pLayoutPolicy = LayoutPolicy.AutomaticExpand;

      pCameraPos = new Point3d(0.0, 0.0, -20.0);
      setFOV(90.0);
      setClip(0.1, 500.0);

      pCanvasToScreen = new Vector2d(1.0, 1.0);

      pZoomSpeed = 0.1;
      pMaxFactor = maxFactor;
    }
  
    /* initialize the panel components */ 
    {
      setLayout(new BorderLayout());
      setMinimumSize(new Dimension(50, 50));
      setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
      
      /* canvas */ 
      {
	pCanvas = UIMaster.getInstance().createGLCanvas(); 
	pCanvas.addGLEventListener(this);
	pCanvas.addMouseListener(this);
	pCanvas.addMouseMotionListener(this);
	pCanvas.setFocusable(true);	

	add(pCanvas);
      }
    }
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
    pCanvas.repaint();
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
   GLDrawable drawable
  )
  {    
    drawable.setGL(new DebugGL(drawable.getGL()));
    //drawable.setGL(new TraceGL(drawable.getGL(), System.err));

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
   GLDrawable drawable
  )
  {
    GL gl = drawable.getGL();

    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

    /* set camera position */ 
    {
      gl.glMatrixMode(GL.GL_MODELVIEW);
      gl.glLoadIdentity();
      gl.glTranslated(pCameraPos.x(), pCameraPos.y(), pCameraPos.z());
    }

    /* draw rubber band geometry */ 
    if((pRbStart != null) && (pRbEnd != null)) {

      /* compute world space coordinates */ 
      Point2d rs = new Point2d(pRbStart);
      Point2d re = new Point2d(pRbEnd);
      {
	Dimension size = pCanvas.getSize();
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
   GLDrawable drawable, 
   int x, 
   int y, 
   int width, 
   int height
  )
  {
    GL  gl  = drawable.getGL();
    GLU glu = drawable.getGLU();

    double w = (double) width;
    double h = (double) height;
    
    pAspect = w / h;
    pCanvasToScreen.set(2.0/w, -2.0/h);

    gl.glMatrixMode(GL.GL_PROJECTION);
    gl.glLoadIdentity();
    glu.gluPerspective(pFOV, pAspect, pNear, pFar);
    
    double dist = -pCameraPos.z();
    dist = Math.max(((double) pCanvas.getHeight()) / pMaxFactor, dist); 
    pCameraPos.z(-dist);
  }
 
  /** 
   * Called by the drawable when the display mode or the display device associated with 
   * the GLDrawable has changed.
   */ 
  public void 
  displayChanged
  (
   GLDrawable drawable, 
   boolean modeChanged, 
   boolean deviceChanged
  )
  {}
  

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
    pCanvas.requestFocusInWindow();

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

	rb = (((mods & (on1 | off1)) == on1) || 
	      ((mods & (on2 | off2)) == on2) || 
	      ((mods & (on3 | off3)) == on3));
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
      {
	int on1  = (MouseEvent.BUTTON1_DOWN_MASK |
		    MouseEvent.BUTTON2_DOWN_MASK | 
		    MouseEvent.ALT_DOWN_MASK);
	
	int off1 = (MouseEvent.BUTTON3_DOWN_MASK | 
		    MouseEvent.SHIFT_DOWN_MASK |
		    MouseEvent.CTRL_DOWN_MASK);

	zoom = ((mods & (on1 | off1)) == on1);
      }
    }
      
    if(rb) {
      pRbStart = new Point2d(pMousePos);
      pRbEnd   = null;

      return true;
    }
    else if(pan || zoom) {
      if(pan) 
	pCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
      else 
	pCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
      
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

	rb = (((mods & (on1 | off1)) == on1) || 
		   ((mods & (on2 | off2)) == on2) || 
		   ((mods & (on3 | off3)) == on3));
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
      {
	int on1  = (MouseEvent.BUTTON1_DOWN_MASK |
		    MouseEvent.BUTTON2_DOWN_MASK | 
		    MouseEvent.ALT_DOWN_MASK);
	
	int off1 = (MouseEvent.BUTTON3_DOWN_MASK | 
		    MouseEvent.SHIFT_DOWN_MASK |
		    MouseEvent.CTRL_DOWN_MASK);

	zoom = ((mods & (on1 | off1)) == on1);
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

	pDragStart = pos;
      }
      else if(zoom) {
	Vector2d delta = new Vector2d(pDragStart, pos);	

	double zm = delta.y();
	if(Math.abs(delta.x()) > Math.abs(delta.y())) 
	  zm = -delta.x();
	
	double dist = -pCameraPos.z();
	dist += pZoomSpeed*zm;
	dist = Math.max(((double) pCanvas.getHeight()) / pMaxFactor, dist); 
	pCameraPos.z(-dist);

	pDragStart = pos;
      } 
    }

    if(rb || pan || zoom) 
      pCanvas.repaint();     
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
    double z = Math.max(((double) pCanvas.getHeight()) / pMaxFactor, Math.max(distX, distY));

    Point2d center = bbox.getCenter();
    pCameraPos.set(center.x(), center.y(), z);
    pCameraPos.negate();

    pCanvas.repaint();
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

  /*----------------------------------------------------------------------------------------*/

  /**
   * The OpenGL rendering canvas.
   */ 
  protected GLCanvas  pCanvas;


  /**
   * The OpenGL display list handle for the scene geometry.
   */ 
  protected Integer  pSceneDL; 

  /**
   * Whether the OpenGL display list for the scene geometry needs to be rebuilt.
   */ 
  protected boolean  pRefreshScene;


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
