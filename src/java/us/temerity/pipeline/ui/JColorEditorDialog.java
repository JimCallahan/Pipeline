// $Id: JColorEditorDialog.java,v 1.2 2004/12/29 17:31:45 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
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
      pHSV = new Tuple3d(0.0, 1.0, 1.0);

      /* the space of the color triangle */ 
      {
	double theta = Math.PI / 6.0;
	double x = Math.cos(theta);
	double y = Math.sin(theta);
	
	Point2d origin = new Point2d(-x, y);
	Vector2d bx = new Vector2d(origin, new Point2d(x, y));
        Vector2d by = new Vector2d(origin, new Point2d(-x, -1.0));
	
	pTriSpace = new CoordSys2d(bx, by, origin);
      }
    }

    /* create dialog body components */ 
    {
      JPanel body = new JPanel();

      {
	pCanvas = UIMaster.getInstance().createGLCanvas(); 
	pCanvas.setSize(520, 250);
	
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
   * Set header title.
   */ 
  public void 
  setHeaderTitle
  (
   String title
  ) 
  {
    pHeaderLabel.setText(title);
  }

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
    pHSV = color.toHSV();
    pCanvas.repaint();
  }

  /**
   * Get the current color.
   */ 
  public Color3d
  getColor() 
  {
    Color3d color = new Color3d();
    color.fromHSV(pHSV);
    return color;
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
    
    double r1a = sInnerRadius; 
    double r2a = sOuterRadius; 

    double r1 = r1a+0.002;
    double r2 = r2a-0.002;

    Color3d c = new Color3d();

    /* build the color circle display lists */ 
    if((pWhiteDL == null) || (pBlackDL == null)) {
      Integer texID = null;
      try {
	texID = TextureMgr.getInstance().getTexture(gl, "ColorCircle");
      }
      catch(IOException ex) {
	Logs.tex.severe(ex.getMessage());
      }
	
      /* the white color circle */ 
      if(pWhiteDL == null) {
	pWhiteDL = gl.glGenLists(1);
	
	gl.glNewList(pWhiteDL, GL.GL_COMPILE);
	{
	  gl.glEnable(GL.GL_TEXTURE_2D);
	  gl.glBindTexture(GL.GL_TEXTURE_2D, texID);
	  
	  gl.glColor3d(1.0, 1.0, 1.0);
	  gl.glBegin(GL.GL_QUADS);
	  {
	    gl.glTexCoord2d(0.0, 1.0);
	    gl.glVertex3d(-0.5, -0.5, 0.0);
	    
	    gl.glTexCoord2d(1.0, 1.0);
	    gl.glVertex3d(0.5, -0.5, 0.0);
	    
	    gl.glTexCoord2d(1.0, 0.0);	
	    gl.glVertex3d(0.5, 0.5, 0.0);
	    
	    gl.glTexCoord2d(0.0, 0.0);
	    gl.glVertex3d(-0.5, 0.5, 0.0);
	  }
	  gl.glEnd();

	  gl.glDisable(GL.GL_TEXTURE_2D); 
	}
	gl.glEndList();
      }
      
      /* the black color circle */ 
      if(pBlackDL == null) {
	pBlackDL = gl.glGenLists(1);
	
	gl.glNewList(pBlackDL, GL.GL_COMPILE);
	{
	  gl.glEnable(GL.GL_TEXTURE_2D);
	  gl.glBindTexture(GL.GL_TEXTURE_2D, texID);

	  gl.glColor3d(0.0, 0.0, 0.0);
	  gl.glBegin(GL.GL_QUADS);
	  {
	    gl.glTexCoord2d(0.0, 1.0);
	    gl.glVertex3d(-0.5, -0.5, 0.0);
	    
	    gl.glTexCoord2d(1.0, 1.0);
	    gl.glVertex3d(0.5, -0.5, 0.0);
	    
	    gl.glTexCoord2d(1.0, 0.0);	
	    gl.glVertex3d(0.5, 0.5, 0.0);
	    
	    gl.glTexCoord2d(0.0, 0.0);
	    gl.glVertex3d(-0.5, 0.5, 0.0);
	  }
	  gl.glEnd();

	  gl.glDisable(GL.GL_TEXTURE_2D); 
	}
	gl.glEndList();
      }
    }

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

    /* the hue ring color circle */ 
    {
      gl.glPushMatrix();
      {
	double r = (sInnerRadius+sOuterRadius)*0.5;
	Point2d p = polarToRing(new Point2d(pHSV.x(), r));
	p.add(0.5);
	gl.glTranslated(p.x(), p.y(), 0.0);

 	double s = (sOuterRadius - sInnerRadius) * 0.65;
 	gl.glScaled(s, s, s);
	
	gl.glCallList(((pHSV.x() > 30.0) && (pHSV.x() < 210.0)) ? pBlackDL : pWhiteDL);
      }
      gl.glPopMatrix();
    }

    /* the HSV triangle */ 
    {
      c.fromHSV(new Tuple3d(pHSV.x(), 1.0, 1.0));

      double theta = Math.PI / 6.0;
      double x = Math.cos(theta);
      double y = Math.sin(theta);
      
      double r = r1a-0.003;

      gl.glPushMatrix();
      {
      	gl.glTranslated(0.5, 0.5, 0.0);
	gl.glRotated(pHSV.x(), 0.0, 0.0, 1.0);

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

      /* the HSV triangle color circle */ 
      {
	Point2d p = Point2d.lerp(new Point2d(-x*r1a, y*r1a), 
				 Point2d.lerp(new Point2d(x*r1a, y*r1a), 
					      new Point2d(0.0, -r1a), 
					      pHSV.y()), 
				 pHSV.z());
	
	gl.glTranslated(p.x(), p.y(), 0.0);
	
	double s = (sOuterRadius - sInnerRadius) * 0.65;
	gl.glScaled(s, s, s);
	  
	gl.glCallList((pHSV.z() > 0.5) ? pBlackDL : pWhiteDL);
      }

      gl.glPopMatrix();
    }

    /* the RGB swatch */ 
    {
      double s = 0.4;

      gl.glPushMatrix();
      {
	gl.glTranslated(1.8, 0.5, 0.0);
	
	gl.glBegin(GL.GL_QUADS);
	{
	  c = getColor();
	  gl.glColor3d(c.r(), c.g(), c.b());
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
  mouseEntered(MouseEvent e) {}
  
  /**
   * Invoked when the mouse exits a component. 
   */ 
  public void 
  mouseExited(MouseEvent e) {}

  /**
   * Invoked when a mouse button has been pressed on a component. 
   */
  public void 
  mousePressed
  (
   MouseEvent e 
  ) 
  {
    Point2d p  = canvasToRing(e.getPoint());
    Point2d hr = ringToPolar(p);

    System.out.print("Press: C = " + e.getPoint() + 
		     "  Pos = " + p + "  Hue/Radius = " + hr + "\n\n");

    if((hr.y() > 1.0) && (hr.y() < (sOuterRadius/sInnerRadius))) {
      pHSV.x(hr.x()); 
      pCanvas.repaint();
      pEditingHue = true;
    }
    else {
      //CoordSys2d space = pTriSpace.mult(CoordSys2d.newRotate(Math.toRadians(pHSV.x())));
      CoordSys2d rot = CoordSys2d.newRotate(Math.toRadians(-pHSV.x()));
      CoordSys2d space = rot.mult(pTriSpace);
      CoordSys2d inv = space.inverse(0.000001);
      
      System.out.print("Tri:\n" + pTriSpace + "\n" + 
		       "Rot:\n" + rot + "\n" + 
		       "Space:\n" + space + "\n" + 
		       "Inv:\n" + inv + "\n\n");		       

      if(inv != null) {
	Point2d tp = inv.xform(p);
	System.out.print("  WP = " + p + "  TP = " + tp + "\n");

	Point2d tb = new Point2d(0.0, 0.0);
	Point2d wb = space.xform(tb);
	Point2d tb2 = inv.xform(wb);
	System.out.print("  TB = " + tb + "  WB = " + wb + "  TB2 = " + tb2 +"\n");

	Point2d ts = new Point2d(0.5, 1.0);
	Point2d ws = space.xform(ts);      
	Point2d ts2 = inv.xform(ws);
	System.out.print("  TS = " + ts + "  WS = " + ws + "  TS2 = " + ts2 + "\n");
	
	Point2d tw = new Point2d(1.0, 0.0);
	Point2d ww = space.xform(tw);
	Point2d tw2 = inv.xform(ww);
	System.out.print("  TW = " + tw + "  WW = " + ww + "  TW2 = " + tw2 + "\n\n");
      }
    }
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
    Point2d p  = canvasToRing(e.getPoint());
    Point2d hr = ringToPolar(p);

    if(pEditingHue) {
      pHSV.x(hr.x()); 
      pCanvas.repaint();
      pEditingHue = false;
    }
  }

  /**
   * Invoked when the mouse cursor has been moved onto a component but no buttons have 
   * been pushed. 
   */ 
  public void 	
  mouseMoved(MouseEvent e) {}


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
    Point2d p = canvasToRing(e.getPoint());
    Point2d hr = ringToPolar(p);
    
    if(pEditingHue) {
      pHSV.x(hr.x()); 
      pCanvas.repaint();
    }
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
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Convert from canvas coordinates to color ring coordinates. 
   */ 
  private Point2d
  canvasToRing
  (
   Point p
  ) 
  {
    Vector2d v = new Vector2d(new Point2d(125.0, 125.0), new Point2d(p.getX(), p.getY()));
    v.div((sInnerRadius/1.2)*250.0); 
    return new Point2d(v);
  }

  /**
   * Convert ring coordinates to polar coordinates [hue, radius].
   */ 
  private Point2d
  ringToPolar
  (
   Point2d p
  ) 
  {
    double hue = Math.toDegrees(Math.atan2(p.x(), -p.y()));
    if(hue < 0) 
      hue += 360.0;
    Vector2d v = new Vector2d(p);
    return new Point2d(hue, v.length());
  }

  /**
   * Convert polar coordinates [hue, radius] to ring coordinates.
   */ 
  private Point2d
  polarToRing
  (
   Point2d p
  ) 
  {
    double theta = Math.toRadians(p.x());
    Point2d pos = new Point2d(Math.sin(theta), -Math.cos(theta));
    pos.mult(p.y());
    return pos;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8078376863798514480L;

  /**
   * The inner radius of the hue ring.
   */ 
  private static final  double  sInnerRadius = 0.45;

  /**
   * The outer radius of the hue ring.
   */ 
  private static final  double  sOuterRadius = 0.55; 

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The current color in HSV representation.
   */ 
  private Tuple3d  pHSV;


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The OpenGL rendering canvas.
   */ 
  private GLCanvas  pCanvas;

  /**
   * The OpenGL display list handle for the hue ring geometry. 
   */ 
  private Integer  pRingDL; 

  /**
   * The OpenGL display list handle for white color circle.
   */
  private Integer  pWhiteDL;  

  /**
   * The OpenGL display list handle for black color circle.
   */ 
  private Integer  pBlackDL; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether a mouse drag which changes the hue is currently in progress.
   */ 
  private boolean  pEditingHue; 

  /**
   * The affine space defined by the color triangle. <P> 
   * 
   * The origin is at the black corner. <BR> 
   * The X basis vector is from the black corner to the white corner. <BR> 
   * The Y basis vector is from the black corner to the saturated corner. 
   */ 
  private CoordSys2d  pTriSpace; 
  
}
