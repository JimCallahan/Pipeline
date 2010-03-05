// $Id: JColorEditorDialog.java,v 1.14 2009/12/14 21:48:22 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.nio.*;
import java.net.*;

import javax.imageio.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;

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
   * 
   * @param owner
   *   The parent frame.
   */ 
  public 
  JColorEditorDialog
  (
   Frame owner
  ) 
  {
    super(owner, "Color Editor");
    initUI();
  }

  /**
   * Construct a new dialog.
   * 
   * @param owner
   *   The parent dialog.
   */ 
  public 
  JColorEditorDialog
  (
   Dialog owner
  ) 
  {
    super(owner, "Color Editor");
    initUI();
  }


  /*----------------------------------------------------------------------------------------*/
  
  private void 
  initUI() 
  {
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
	GLCapabilities glc = new GLCapabilities();  
	glc.setDoubleBuffered(true);
	pGLJPanel = new GLJPanel(glc);
        
        Dimension size = new Dimension(520, 250);
        pGLJPanel.setPreferredSize(size);
        pGLJPanel.setMinimumSize(size); 
        pGLJPanel.setMaximumSize(size); 

	pGLJPanel.addGLEventListener(this);
	pGLJPanel.addMouseListener(this);
	pGLJPanel.addMouseMotionListener(this);

	body.add(pGLJPanel);
      }

      super.initUI("Color Editor:", body, "Confirm", null, null, "Cancel");
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
    pGLJPanel.repaint();
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
   GLAutoDrawable drawable
  )
  {    
    // drawable.setGL(new DebugGL(drawable.getGL()));
    // drawable.setGL(new TraceGL(drawable.getGL(), System.err));

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
    GL gl = drawable.getGL();

    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
    
    double r1a = sInnerRadius; 
    double r2a = sOuterRadius; 

    double r1 = r1a+0.002;
    double r2 = r2a-0.002;

    Color3d c = new Color3d();

    /* build the color circle display lists */ 
    if((pWhiteDL == null) || (pBlackDL == null)) {
      loadColorCircleTexture(gl);
	
      /* the white color circle */ 
      if(pWhiteDL == null) {
	pWhiteDL = gl.glGenLists(1);
	
	gl.glNewList(pWhiteDL, GL.GL_COMPILE);
	{
	  gl.glEnable(GL.GL_TEXTURE_2D);
 	  if(pTexID != null) 
	    gl.glBindTexture(GL.GL_TEXTURE_2D, pTexID);
	  
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
 	  if(pTexID != null) 
	    gl.glBindTexture(GL.GL_TEXTURE_2D, pTexID);

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

      gl.glPushMatrix();
      {
	gl.glTranslated(0.5, 0.5, 0.0);
	gl.glRotated(pHSV.x(), 0.0, 0.0, 1.0);
	
	gl.glEnable(GL.GL_POLYGON_SMOOTH);
	gl.glBegin(GL.GL_TRIANGLES);
	{
	  gl.glColor3d(c.r(), c.g(), c.b()); 
	  gl.glVertex2d(0.0, -r1a); 
	  
	  gl.glColor3d(1.0, 1.0, 1.0);
	  gl.glVertex2d(x*r1a, y*r1a);
	  
	  gl.glColor3d(0.0, 0.0, 0.0);
	  gl.glVertex2d(-x*r1a, y*r1a);
	  }
	gl.glEnd();
	gl.glDisable(GL.GL_POLYGON_SMOOTH);
      
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
      }
      gl.glPopMatrix();
    }

    /* the RGB swatch */ 
    {
      double s  = 0.4;

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

	gl.glBegin(GL.GL_LINE_LOOP);
	{
	  gl.glColor3d(1.0, 1.0, 1.0);
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
   GLAutoDrawable drawable, 
   int x, 
   int y, 
   int width, 
   int height
  )
  {
    GL  gl  = drawable.getGL();
    GLU glu = new GLU();

    gl.glMatrixMode(GL.GL_PROJECTION);
    gl.glLoadIdentity();
    glu.gluOrtho2D(-0.1, 2.4, 1.1, -0.1);
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
    Point2d pos = canvasToRing(e.getPoint());
    Point2d hr  = ringToPolar(pos);

    if(hr.y() < 1.0) {
      CoordSys2d rot = CoordSys2d.newRotate(Math.toRadians(-pHSV.x()));
      CoordSys2d space = rot.mult(pTriSpace);
      CoordSys2d inv = space.inverse(0.000001);
      
      Point2d p = inv.xform(pos);
      if(!computeSatVal(p)) 
	return;

      pGLJPanel.repaint();
      pEditingSatVal = true;
    }
    else if((hr.y() > 1.0) && (hr.y() < (sOuterRadius/sInnerRadius))) {
      pHSV.x(hr.x()); 
      pGLJPanel.repaint();
      pEditingHue = true;
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
    pEditingSatVal = false;
    pEditingHue    = false;
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
    Point2d pos = canvasToRing(e.getPoint());
    Point2d hr = ringToPolar(pos);
    
    if(pEditingSatVal) {
      CoordSys2d rot = CoordSys2d.newRotate(Math.toRadians(-pHSV.x()));
      CoordSys2d space = rot.mult(pTriSpace);
      CoordSys2d inv = space.inverse(0.000001);
      
      Point2d p = inv.xform(pos);
      if(!computeSatVal(p)) {
	double sy = Math.sin(Math.PI / 6.0);
	Point2d m = new Point2d(0.5, sy / (1.0 + sy));

	Point2d a = new Point2d(0.5, 1.0);
	Point2d b = new Point2d();
	Point2d c = new Point2d(1.0, 0.0);

	Vector2d ma = new Vector2d(m, a);
	Vector2d mb = new Vector2d(m, b);
	Vector2d mc = new Vector2d(m, c);

	Vector2d pa = new Vector2d(-ma.y(), ma.x()); 
	Vector2d pb = new Vector2d(-mb.y(), mb.x());
	Vector2d pc = new Vector2d(-mc.y(), mc.x());

	Vector2d mp = new Vector2d(m, p);
	double da = mp.dot(pa);
	double db = mp.dot(pb);
	double dc = mp.dot(pc);

	int region = -1;
	if(da > 0.0) {
	  if(db > 0.0) 
	    region = 2;
	  else 
	    region = 0;
	}
	else {
	  if(dc > 0.0) 
	    region = 1;
	  else 
	    region = 2;
	}

	switch(region) {
	case 0:
	  {
	    double value = (((m.y() * (0.5 - p.x())) + (0.5 * (p.y() - m.y()))) / 
			    ((0.5 - p.x()) + (0.5 * (p.y() - m.y())))); 
	    pHSV.y(1.0);
	    pHSV.z(ExtraMath.clamp(value, 0.0, 1.0));
	  }
	  break;

	case 1:
	  {	    
	    double sat = 1.0 - (((p.x() - 0.5) * (1.0 - m.y())) /
				((0.5 * (p.y() - m.y())) + (p.x() - 0.5)));
	    pHSV.y(ExtraMath.clamp(sat, 0.0, 1.0));
	    pHSV.z(1.0);
	  }
	  break;

	case 2:
	  {
	    double value = (((m.y() * (0.5 - p.x())) + (0.5 * (p.y() - m.y()))) / 
			    (p.y() - m.y()));
	    pHSV.y(0.0);
	    pHSV.z(ExtraMath.clamp(value, 0.0, 1.0));
	  }
	}
      }
      
      pGLJPanel.repaint();
    }
    else if(pEditingHue) {
      pHSV.x(hr.x()); 
      pGLJPanel.repaint();
    }
  }



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


  /** 
   * Determine whether the given triangle space position is inside the triangle
   * and if it is compute and set the saturation and value.
   * 
   * @return 
   *   Whether the position was inside the triangle.
   */ 
  private boolean
  computeSatVal
  (
   Point2d p
  ) 
  {
    if(p.anyLt(0.0) || p.anyGe(1.0)) 
      return false;
    
    double sat = 1.0 - ((p.x() - 0.5*p.y()) / (p.x() + 0.5*p.y()));
    if(sat > 1.0)
      return false;
    
    Point2d a = new Point2d(0.5, 1.0);
    Point2d b = new Point2d(1.0, 0.0);
    Point2d c = new Point2d();
    
    Point2d q = Point2d.lerp(b, a, sat);
    
    Vector2d cp = new Vector2d(c, p);
    double lcp = cp.length();
    
    Vector2d cq = new Vector2d(c, q);
    double lcq = cq.length();
    
    if(lcp > lcq) 
      return false;
    
    double value = lcp / lcq;
    
    pHSV.y(ExtraMath.clamp(sat, 0.0, 1.0));
    pHSV.z(ExtraMath.clamp(value, 0.0, 1.0));

    return true;
  }


  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Make sure the OpenGL texture for the color circle is loaded.
   * 
   * @param gl
   *   The OpenGL interface.
   */ 
  public void 
  loadColorCircleTexture
  (
   GL gl
  ) 
  {
    /* make sure they haven't already been loaded */ 
    if(pTexID != null) 
      return; 

    /* build the texture object */    
    String name = "ColorCircle";
    LogMgr.getInstance().log
      (LogMgr.Kind.Tex, LogMgr.Level.Fine,
       "Loading Texture: " + name);
    try {
      int handle[] = new int[1];
      gl.glGenTextures(1, handle, 0); 

      gl.glBindTexture(GL.GL_TEXTURE_2D, handle[0]);

      int level, size; 
      for(level=0, size=sMaxTexRes; size>=1; level++, size/=2) {
	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Tex, LogMgr.Level.Finer,
	   "Loading MipMap: " + size + "x" + size);

	String path = ("textures/" + name + "/texture." + size + ".png");
	URL url = LookAndFeelLoader.class.getResource(path);
	if(url == null) 
	  throw new IOException("Unable to find: " + path);
	BufferedImage bi = ImageIO.read(url);
	
	if((bi.getWidth() != size) || (bi.getHeight() != size)) 
	  throw new IOException
	    ("The image size (" + bi.getWidth() + "x" + bi.getHeight() + ") of " + 
	     "texture (" + path + ") does not match the expected size " + 
	     "(" + size + "x" + size + ")!");
	
        byte[] data = null;
        switch(bi.getType()) {
        case BufferedImage.TYPE_CUSTOM:
          /* leave as-is: RGBA */ 
          data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
          break;

        case BufferedImage.TYPE_4BYTE_ABGR:
          { /* swizzle in place: ABGR -> RGBA */ 
            data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
            int i = 0;
            for(i=0; (i+3)<data.length; i+=4) {
              byte a = data[i];
              byte b = data[i+1];
              byte g = data[i+2];
              byte r = data[i+3];
              
              data[i]   = r;
              data[i+1] = g;
              data[i+2] = b;
              data[i+3] = a;
            }
          }
          break;

        default:
          throw new IOException
            ("The image format (" + bi.getType() + ") of texture (" + path + ") is " +
             "not supported!");	    
        }

	ByteBuffer buf = ByteBuffer.allocateDirect(data.length);
	buf.order(ByteOrder.nativeOrder());
	buf.put(data, 0, data.length);
	buf.rewind();
	
	gl.glTexImage2D(GL.GL_TEXTURE_2D, level, GL.GL_RGBA, size, size, 
			0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, buf);
      }
	    
      gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP);
      gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP);
      
      gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, 
			 GL.GL_LINEAR_MIPMAP_LINEAR);
      gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
      
      pTexID = handle[0];
    }
    catch(Exception ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Tex, LogMgr.Level.Severe,
	 ex.getMessage());
    }
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
  
  /**
   * The resolution of the largest Mip-Map level texture image.
   */ 
  private static final int  sMaxTexRes = 64;



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
  private GLJPanel  pGLJPanel;

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

  /**
   * The OpenGL texture object handle for the color circle.
   */ 
  private Integer  pTexID; 
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether a mouse drag which changes the saturation/value is currently in progress.
   */ 
  private boolean  pEditingSatVal; 

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
