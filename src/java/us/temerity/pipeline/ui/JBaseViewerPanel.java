// $Id: JBaseViewerPanel.java,v 1.1 2004/12/14 20:33:57 jim Exp $

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

      pPanSpeed  = 0.415;
      pZoomSpeed = 0.1;
      pMaxFactor = maxFactor;

      pRubberBand = new RubberBand();
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
    //    drawable.setGL(new TraceGL(drawable.getGL(), System.err));

    GL  gl  = drawable.getGL();
    GLU glu = drawable.getGLU();

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
    GL  gl  = drawable.getGL();
    GLU glu = drawable.getGLU();

    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

    /* set camera position */ 
    {
      gl.glMatrixMode(GL.GL_MODELVIEW);
      gl.glLoadIdentity();
      gl.glTranslated(pCameraPos.x(), pCameraPos.y(), pCameraPos.z());
    }

    /* draw rubber band geometry */ 
    pRubberBand.display(drawable, pCameraPos);
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
  mousePressed(MouseEvent e) {}

  /**
   * Invoked when a mouse button has been released on a component. 
   */ 
  public void 
  mouseReleased(MouseEvent e) {}

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

    /* begin rubber band drag */ 
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
    
    /* pan start */ 
    int on4  = (MouseEvent.BUTTON2_DOWN_MASK |
		MouseEvent.ALT_DOWN_MASK);
    
    int off4 = (MouseEvent.BUTTON1_DOWN_MASK | 
		MouseEvent.BUTTON3_DOWN_MASK | 
		MouseEvent.SHIFT_DOWN_MASK |
		MouseEvent.CTRL_DOWN_MASK);
    
    /* zoom start */ 
    int on5  = (MouseEvent.BUTTON1_DOWN_MASK |
		MouseEvent.BUTTON2_DOWN_MASK | 
		MouseEvent.ALT_DOWN_MASK);
    
    int off5 = (MouseEvent.BUTTON3_DOWN_MASK | 
		MouseEvent.SHIFT_DOWN_MASK |
		MouseEvent.CTRL_DOWN_MASK);

    /* BUTTON1[+SHIFT[+CTRL]]: begin rubber band drag */ 
    if(((mods & (on1 | off1)) == on1) || 
       ((mods & (on2 | off2)) == on2) || 
       ((mods & (on3 | off3)) == on3)) {
      pRubberBand.beginDrag(new Point2d((double) e.getX(), (double) e.getY()));
      return true;
    }
    
    /* <BUTTON2+ALT> (pan start) or <BUTTON1+BUTTON2+ALT> (zoom start) */ 
    if(((mods & (on4 | off4)) == on4) || ((mods & (on5 | off5)) == on5)) {

      /* set start point of the drag */ 
      {
	Point p = e.getPoint();
	pDragStart = new Point2d(p.getX(), p.getY());
      }
      
      /* change cursor */ 
      if((mods & (on1 | off1)) == on1)
	pCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
      else 
	pCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
      
      return true;
    }

    return false;
  }

  /**
   * Handle viewer related mouse release events.
   *
   * @return 
   *   Whether the event was handled.
   */ 
  protected boolean
  handleMouseReleased
  (
   MouseEvent e
  ) 
  {
    pCanvas.setCursor(Cursor.getDefaultCursor());

    int mods = e.getModifiersEx();
    switch(e.getButton()) {
    case MouseEvent.BUTTON1:
      if(pRubberBand.isDragging()) {
	pRubberBand.endDrag();
	return true;
      }
    }

    return false;
  }


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

    /* rubber band drag */ 
    if(pRubberBand.isDragging()) {
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

      /* BUTTON1[+SHIFT[+CTRL]]: update rubber band drag */ 
      if(((mods & (on1 | off1)) == on1) || 
	 ((mods & (on2 | off2)) == on2) || 
	 ((mods & (on3 | off3)) == on3)) {
	pRubberBand.updateDrag(new Point2d((double) e.getX(), (double) e.getY()));
      }
   
      /* end rubber band drag */ 
      else {
	pRubberBand.endDrag();
      }

      pCanvas.repaint();
    }
    
    /* zoom/pan drag */ 
    else {
      if(pDragStart != null) {
	int on1  = (MouseEvent.BUTTON2_DOWN_MASK |
		    MouseEvent.ALT_DOWN_MASK);
	
	int off1 = (MouseEvent.BUTTON1_DOWN_MASK | 
		    MouseEvent.BUTTON3_DOWN_MASK | 
		    MouseEvent.SHIFT_DOWN_MASK |
		    MouseEvent.CTRL_DOWN_MASK);
	
	
	int on2  = (MouseEvent.BUTTON1_DOWN_MASK |
		    MouseEvent.BUTTON2_DOWN_MASK | 
		    MouseEvent.ALT_DOWN_MASK);
	
	int off2 = (MouseEvent.BUTTON3_DOWN_MASK | 
		    MouseEvent.SHIFT_DOWN_MASK |
		    MouseEvent.CTRL_DOWN_MASK);
      
	/* <BUTTON2+ALT> (pan) */ 
	if((mods & (on1 | off1)) == on1) {
	  Point p = e.getPoint();
	  Vector2d delta = new Vector2d(pDragStart, new Point2d(p.getX(), p.getY()));

	  Vector2d persp = new Vector2d(pCameraPos.z() * pPerspFactor * pAspect, 
					pCameraPos.z() * pPerspFactor);
	  delta.mult(pCanvasToScreen).mult(persp);

	  pCameraPos.add(new Vector3d(-delta.x(), -delta.y(), 0.0));
	  
	  pCanvas.repaint();
	}
      
	/* <BUTTON1+BUTTON2+ALT> (zoom) */ 
	if((mods & (on2 | off2)) == on2) {
	  Point p = e.getPoint();
	  Vector2d delta = new Vector2d(pDragStart, new Point2d(p.getX(), p.getY()));

 	  double zoom = delta.y();
 	  if(Math.abs(delta.x()) > Math.abs(delta.y())) 
 	    zoom = -delta.x();
	  
	  double dist = -pCameraPos.z();
	  dist += pZoomSpeed*zoom;
	  dist = Math.max(((double) pCanvas.getWidth()) / pMaxFactor, dist); 
	  pCameraPos.z(-dist);

	  pCanvas.repaint();
	}
      }

      {
	Point p = e.getPoint();
	pDragStart = new Point2d(p.getX(), p.getY());
      }
    }
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
   * Move the camera to frame the given world space bounding box on the XY plane.
   */ 
  protected void 
  frameBounds
  (
   BBox2d bbox
  ) 
  {
    if(bbox == null) 
      return; 

    // ....
    
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
   * The expand/collapse policy to use when laying out nodes.
   */
  protected 
  enum LayoutPolicy
  {  
    /**
     * Preserve the collapse mode of existing nodes and use an AutomaticExpand policy for 
     * any newly created nodes.
     */
    Preserve, 

    /**
     * Expand all nodes.
     */ 
    ExpandAll, 

    /**
     * Collapse all nodes.
     */ 
    CollapseAll, 
    
    /**
     * Expand the first occurance of a node and collapse all subsequence occurances.
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
   * The associated OpenGL rendering canvas.
   */ 
  protected GLCanvas  pCanvas;

  /**
   * The expand/collapse policy to use during layout.
   */ 
  protected LayoutPolicy  pLayoutPolicy;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The camera position in world space coordinates.
   */ 
  private Point3d  pCameraPos; 

  /** 
   * The vertical field-of-view in degrees.
   */ 
  private double pFOV; 

  /**
   * The perspective correction factor: tan(FOV / 2)
   */ 
  private double pPerspFactor; 

  /**
   * The canvas aspect ratio: width / height
   */ 
  private double pAspect;

  /**
   * The distance to the near clipping plane.
   */ 
  private double pNear;

  /**
   * The distance to the far clipping plane.
   */ 
  private double pFar;

  /**
   * Conversion factor from canvas coordinates (in pixels) to normalized screen space 
   * coordinates: [-1.0, 1.0] in both X and Y.
   */ 
  private Vector2d  pCanvasToScreen;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The camera pan speed scale factor. 
   */ 
  private double  pPanSpeed;   

  /**
   * The camera zoom speed scale factor. 
   */ 
  private double  pZoomSpeed;  

  /**
   * The maximum camera zoom factor. 
   */ 
  private double  pMaxFactor; 


  /**
   * The pixel location of start of mouse drag. 
   */ 
  protected Point2d  pDragStart;  


  /*----------------------------------------------------------------------------------------*/

  /**
   * The last known mouse position in canvas coordinates.
   */ 
  protected Point2d pMousePos;

  /**
   * The selection rubberband.
   */ 
  protected RubberBand  pRubberBand;
}
