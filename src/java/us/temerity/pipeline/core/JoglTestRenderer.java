// $Id: JoglTestRenderer.java,v 1.3 2005/01/03 06:57:12 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.core.*;
import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*; 
import java.awt.geom.*; 
import java.net.*;
import java.util.*;
import java.util.logging.*;
import java.io.*;
import java.nio.*;

import javax.imageio.*;

import net.java.games.jogl.*;

/*------------------------------------------------------------------------------------------*/
/*   J O G L   T E S T   R E N D E R E R                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Does the actual OpenGL rendering.
 */ 
public class 
JoglTestRenderer 
  implements GLEventListener, MouseListener, MouseMotionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new renderer.
   */
  public
  JoglTestRenderer
  (
   GLCanvas canvas
  ) 
  {
    pCanvas   = canvas;
    pGeometry = -1;

    pPanSpeed  = 0.415;
    pZoomSpeed = 0.1;
    pMaxFactor = 100.0;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R                                                                      */
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
//     drawable.setGL(new TraceGL(drawable.getGL(), System.err));

    GL  gl  = drawable.getGL();
    GLU glu = drawable.getGLU();

    /* initial camera location */ 
    pCameraPos  = new Point2D.Double(0.0, 0.0);
    pCameraDist = 20.0;

    /* global OpenGL state */ 
    {
      gl.glClearColor(0.5f, 0.5f, 0.5f, 0.0f);
      gl.glEnable(GL.GL_BLEND);
      gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
    }

    Logs.tex.setLevel(Level.FINE);
   
    /* load the textures */ 
    try {
      TextureMgr mgr = TextureMgr.getInstance();
      
      for(SelectionMode mode : SelectionMode.all()) {
	for(OverallQueueState qstate : OverallQueueState.all()) {
	  for(OverallNodeState nstate : OverallNodeState.all()) {
	    mgr.verifyTexture(gl, nstate + "-" + qstate + "-" + mode);
	  }
	}
      }

      mgr.registerFont("CharterBTRoman", new CharterBTRomanFontGeometry());
      mgr.verifyFontTextures(gl, "CharterBTRoman");
    }
    catch(IOException ex) {
      Logs.tex.severe("Unable to load textures!\n" + 
		      "  " + ex.getMessage());
      Logs.flush();
      System.exit(1);
    }

    Logs.flush();
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
       gl.glTranslated(pCameraPos.x, pCameraPos.y, -pCameraDist);
    }

    /* draw geometry */ 
    if(pGeometry != -1) {
      gl.glCallList(pGeometry);
    }
    else {
      pGeometry = gl.glGenLists(1);
      gl.glNewList(pGeometry, GL.GL_COMPILE_AND_EXECUTE);

      gl.glEnable(GL.GL_TEXTURE_2D); 
      {
	Random rand = new Random((new Date()).getTime());
	int cnt = 0;

	for(SelectionMode mode : SelectionMode.all()) {
	  for(OverallQueueState qstate : OverallQueueState.all()) {
	    for(OverallNodeState nstate : OverallNodeState.all()) {
	      
	      Integer texture = null;
	      try {
		TextureMgr mgr = TextureMgr.getInstance();
		texture = mgr.getTexture(gl, nstate + "-" + qstate + "-" + mode);
	      }
	      catch(IOException ex) {
		Logs.tex.severe(ex.getMessage());
	      }
	      
	      gl.glBindTexture(GL.GL_TEXTURE_2D, texture);
	      
	      int wk;
	      for(wk=0; wk<150; wk++) {
		gl.glBegin(GL.GL_QUADS);
		{
		  float x = (rand.nextFloat() - 0.5f) * 250.0f; 
		  float y = (rand.nextFloat() - 0.5f) * 250.0f; 
		  float s = 1.0f;
		  float z = 0.0f;
		  
		  gl.glTexCoord2f(0.0f, 1.0f);
		  gl.glVertex3f(x, y, z);
		  
		  gl.glTexCoord2f(1.0f, 1.0f);
		  gl.glVertex3f(x+s, y, z);
		  
		  gl.glTexCoord2f(1.0f, 0.0f);	
		  gl.glVertex3f(x+s, y+s, z);
		  
		  gl.glTexCoord2f(0.0f, 0.0f);
		  gl.glVertex3f(x, y+s, z);
		}
		gl.glEnd();
		
		cnt++;
	      }
	      
	    }
	  }
	}

	System.out.print("Nodes = " + cnt + "\n");
      }
      gl.glDisable(GL.GL_TEXTURE_2D); 
      
      gl.glEndList();
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
    glu.gluPerspective(90.0, ((double) width) / ((double) height), 0.1, 500.0);
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
    int mods = e.getModifiersEx();
    
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

    /* <BUTTON2+ALT> (pan start) or <BUTTON1+BUTTON2+ALT> (zoom start) */ 
    if(((mods & (on1 | off1)) == on1) || ((mods & (on2 | off2)) == on2)) {
      
      /* set start point of the drag */ 
      pDragStart = e.getPoint();
      
      /* change cursor */ 
      if((mods & (on1 | off1)) == on1)
	pCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
      else 
	pCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
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
    pCanvas.setCursor(Cursor.getDefaultCursor());
  }


  /*-- MOUSE MOTION LISTENER METHODS -------------------------------------------------------*/

  /**
   * Invoked when a mouse button is pressed on a component and then dragged.
   */
  public void 
  mouseDragged
  (
   MouseEvent e
  )
  {
    if(pDragStart != null) {
      int mods = e.getModifiersEx();
    
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
      
//       /* <BUTTON2+ALT> (pan) */ 
//       if((mods & (on1 | off1)) == on1) {	
// 	Point3d startPos = new Point3d();
// 	canvas.getPixelLocationInImagePlate(pDragStart.x, pDragStart.y, startPos);
	
// 	Point3d endPos = new Point3d();
// 	canvas.getPixelLocationInImagePlate(e.getX(), e.getY(), endPos);
	
// 	Transform3D mx = new Transform3D();
// 	canvas.getImagePlateToVworld(mx);
// 	mx.transform(startPos);
// 	mx.transform(endPos);
	  
// 	Point3d delta = new Point3d(startPos);
// 	delta.sub(endPos);
// 	delta.scale(trans.z * pPanSpeed);
	
// 	trans.add(delta);
	
// 	xform.setTranslation(trans);
// 	tg.setTransform(xform);
//       }
      
      /* <BUTTON1+BUTTON2+ALT> (zoom) */ 
      if((mods & (on2 | off2)) == on2) {
	double dx = ((double) e.getX()) - pDragStart.x;
	double dy = ((double) e.getY()) - pDragStart.y;
	
	double zoom = dy;
	if(Math.abs(dx) > Math.abs(dy)) 
	  zoom = -dx;
	
	pCameraDist += pZoomSpeed*zoom;
	pCameraDist = Math.max(((double) pCanvas.getWidth()) / pMaxFactor, pCameraDist);
	
	pCanvas.repaint();
      }
    }

    pDragStart = e.getPoint();
  }

  /**
   * Invoked when the mouse cursor has been moved onto a component but no buttons have 
   * been pushed.
   */ 
  public void 
  mouseMoved(MouseEvent e) {}


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The associated OpenGL rendering canvas.
   */ 
  private GLCanvas  pCanvas;

  /**
   * The geometry display list handle.
   */ 
  private int  pGeometry; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The camera position.
   */ 
  private Point2D.Double pCameraPos; 

  /**
   * The camera distance.
   */ 
  private double pCameraDist;


  /**
   * The pan speed scale factor. 
   */ 
  private double  pPanSpeed;   

  /**
   * The zoom speed scale factor. 
   */ 
  private double  pZoomSpeed;  

  /**
   * The max zoom factor. 
   */ 
  private double  pMaxFactor; 


  /**
   * The pixel location of start of mouse drag. 
   */ 
  private Point  pDragStart;  

}
