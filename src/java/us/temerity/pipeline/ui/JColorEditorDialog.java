// $Id: JColorEditorDialog.java,v 1.1 2004/12/17 15:06:51 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import net.java.games.jogl.*;

/*------------------------------------------------------------------------------------------*/
/*   C O L O R   E D I T O R   D I A L O G                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A dialog for editing a color. 
 */ 
public 
class JColorEditorDialog
  extends JBaseDialog
  implements MouseListener, MouseMotionListener, GLEventListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JColorEditorDialog() 
  {
    super("Color Editor", true);
    
    /* initialize fields */ 
    {
      pColor = new Color3d();
    }

    /* create dialog body components */ 
    {
      JPanel body = new JPanel();

      {
	pCanvas = UIMaster.getInstance().createGLCanvas(); 
	pCanvas.setSize(500, 250);
	
	pCanvas.addGLEventListener(this);
	pCanvas.addMouseListener(this);
	pCanvas.addMouseMotionListener(this);

	body.add(pCanvas);
      }

      super.initUI("Color Editor:", true, body, "Confirm", null, null, "Cancel");
    }

    setResizable(false);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the current color.
   */ 
  public void
  setColor
  (
   Color3d color
  ) 
  {
    pColor.set(color);
    pCanvas.repaint();
  }

  /**
   * Get the current color.
   */ 
  public Color3d
  getColor() 
  {
    return new Color3d(pColor);
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
    
    Color3d c = new Color3d();

    double r1a = 0.45; 
    double r2a = 0.55; 

    double r1 = r1a+0.002;
    double r2 = r2a-0.002;

    /* the hue ring */ 
    if(pRingDL == null) {
      pRingDL = gl.glGenLists(1);
      
      gl.glNewList(pRingDL, GL.GL_COMPILE_AND_EXECUTE);
      {
	gl.glPushMatrix();
	{
	  gl.glTranslated(0.5, 0.5, 0.0);

	  gl.glBegin(GL.GL_QUAD_STRIP);
	  {      
	    int i; 
	    for(i=0; i<=360; i+=2) {
	      double h = (double) i;
	      if(i<360) 
		c.fromHSV(new Tuple3d(h, 1.0, 1.0));
	      else 
		c.set(1.0, 0.0, 0.0);
	      
	      double theta = Math.toRadians(h); 
	      double x = Math.sin(theta);
	      double y = Math.cos(theta);
	      
	      gl.glColor4d(c.r(), c.g(), c.b(), 0.5); 
	      gl.glVertex2d(x*r1a, -y*r1a);
	      gl.glVertex2d(x*r2a, -y*r2a);
	    }
	  }
	  gl.glEnd();	  

	  gl.glBegin(GL.GL_QUAD_STRIP);
	  {      
	    int i; 
	    for(i=0; i<=360; i+=2) {
	      double h = (double) i;
	      if(i<360) 
		c.fromHSV(new Tuple3d(h, 1.0, 1.0));
	      else 
		c.set(1.0, 0.0, 0.0);
	      
	      double theta = Math.toRadians(h); 
	      double x = Math.sin(theta);
	      double y = Math.cos(theta);
	      
	      gl.glColor3d(c.r(), c.g(), c.b()); 
	      gl.glVertex2d(x*r1, -y*r1);
	      gl.glVertex2d(x*r2, -y*r2);
	    }
	  }
	  gl.glEnd();
	}
	gl.glPopMatrix();
      }
      gl.glEndList();
    }
    else {
      gl.glCallList(pRingDL);
    }

    /* the HSV triangle */ 
    {
      Tuple3d hsv = pColor.toHSV();
      c.fromHSV(new Tuple3d(hsv.x(), 1.0, 1.0));

      double theta = Math.PI / 6.0;
      double x = Math.cos(theta);
      double y = Math.sin(theta);
      
      double r = r1a-0.003;

      gl.glPushMatrix();
      {
      	gl.glTranslated(0.5, 0.5, 0.0);
	gl.glRotated(hsv.x(), 0.0, 0.0, 1.0);

	gl.glBegin(GL.GL_TRIANGLES);
	{
	  gl.glColor4d(c.r(), c.g(), c.b(), 0.5); 
	  gl.glVertex2d(0.0, -r1a); 
	  
	  gl.glColor4d(1.0, 1.0, 1.0, 0.5);
	  gl.glVertex2d(x*r1a, y*r1a);

	  gl.glColor4d(0.0, 0.0, 0.0, 0.5);
	  gl.glVertex2d(-x*r1a, y*r1a);


 	  gl.glColor3d(c.r(), c.g(), c.b()); 
 	  gl.glVertex2d(0.0, -r); 
	  
 	  gl.glColor3d(1.0, 1.0, 1.0);
 	  gl.glVertex2d(x*r, y*r);

 	  gl.glColor3d(0.0, 0.0, 0.0);
 	  gl.glVertex2d(-x*r, y*r);
 	}
 	gl.glEnd();
      }
      gl.glPopMatrix();
    }

    /* the RGB swatch */ 
    {
      double s = 0.4;

      gl.glPushMatrix();
      {
	gl.glTranslated(1.75, 0.5, 0.0);
	
	gl.glBegin(GL.GL_QUADS);
	{
	  gl.glColor3d(pColor.r(), pColor.g(), pColor.b());
	  gl.glVertex2d( s,  s);
	  gl.glVertex2d( s, -s);
	  gl.glVertex2d(-s, -s);
	  gl.glVertex2d(-s,  s);
	}
	gl.glEnd();
      }
      gl.glPopMatrix();
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

    gl.glMatrixMode(GL.GL_PROJECTION);
    gl.glLoadIdentity();
    glu.gluOrtho2D(-0.1, 2.4, 1.1, -0.1);
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

  }

  /**
   * Invoked when a mouse button has been released on a component. 
   */ 
  public void 
  mouseReleased
  (
   MouseEvent e
  ) 
  {

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

  }


  /*-- ACTION LISTENER METHODS -------------------------------------------------------------*/

  /** 
   * Invoked when an action occurs. 
   */ 
  public void 
  actionPerformed
  (
   ActionEvent e
  ) 
  {
    super.actionPerformed(e);


  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8078376863798514480L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The current color.
   */ 
  private Color3d  pColor;


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The OpenGL rendering canvas.
   */ 
  private GLCanvas  pCanvas;

  /**
   * The OpenGL display list handle for the hue ring geometry. 
   */ 
  private Integer  pRingDL; 

}
