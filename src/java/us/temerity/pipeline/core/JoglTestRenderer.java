// $Id: JoglTestRenderer.java,v 1.1 2004/12/11 13:41:32 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;
import us.temerity.pipeline.ui.jogl.TextureMgr;
import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*; 
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
  implements GLEventListener, MouseListener
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
    pCanvas = canvas;
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

    GL gl   = drawable.getGL();
    GLU glu = drawable.getGLU();

    /* global OpenGL state */ 
    {
      gl.glClearColor(0.5f, 0.5f, 0.5f, 0.0f);
      gl.glEnable(GL.GL_BLEND);
      gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
    }

    Logs.tex.setLevel(Level.FINEST);
   
    /* load the textures */ 
    try {
      us.temerity.pipeline.ui.jogl.TextureMgr mgr = 
	us.temerity.pipeline.ui.jogl.TextureMgr.getInstance();
      
      for(SelectionMode mode : SelectionMode.all()) {
	for(OverallQueueState qstate : OverallQueueState.all()) {
	  for(OverallNodeState nstate : OverallNodeState.all()) {
	    mgr.verifyTexture(gl, nstate + "-" + qstate + "-" + mode);
	  }
	}

	mgr.registerFont("CharterBTRoman", new CharterBTRomanFontGeometry());
	mgr.verifyFontTextures(gl, "CharterBTRoman");
      }
    }
    catch(IOException ex) {
      Logs.tex.severe("Unable to load textures!\n" + 
		      "  " + ex.getMessage());
      Logs.flush();
      System.exit(1);
    }


//     /* load a texture */ 
//     try {
//       pTextures = new int[1];
//       gl.glGenTextures(pTextures.length, pTextures);
      
      
//      String path = ("textures/ModifiedLinks-Finished-Normal/texture.64.png");
//      URL url = LookAndFeelLoader.class.getResource(path);
//      if(url == null) 
// 	throw new IOException("Unable to find: " + path);
//      BufferedImage bi = ImageIO.read(url);
  
//      switch(bi.getType()) {
//      case BufferedImage.TYPE_CUSTOM:
// 	{
// 	  byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
// 	  ByteBuffer buf = ByteBuffer.allocateDirect(data.length);
// 	  buf.order(ByteOrder.nativeOrder());
// 	  buf.put(data, 0, data.length);
// 	  buf.rewind();

// 	  gl.glBindTexture(GL.GL_TEXTURE_2D, pTextures[0]);
// 	  glu.gluBuild2DMipmaps(GL.GL_TEXTURE_2D, GL.GL_RGBA, bi.getWidth(), bi.getHeight(), 
// 				GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, buf);

// 	}
// 	break;
	
//      default:
// 	throw new IOException
// 	  ("Texture image (" + path + ") has an unsupported format (" + bi.getType() + ")!");
//      }


//        {
//  	gl.glBindTexture(GL.GL_TEXTURE_2D, pTextures[0]);
	
//  	String name = "ModifiedLinks-Finished-Normal";
//  	Logs.tex.info("Loading Texture: " + name );
//  	Logs.flush();
	
//  	int level, size; 
//  	for(level=0, size=64; size>=1; level++, size/=2) {
//  	  Logs.tex.info("Loading MipMap: " + size + "x" + size);
//  	  Logs.flush();
	  
//  	  String path = ("textures/" + name + "/texture." + size + ".png");
//  	  URL url = LookAndFeelLoader.class.getResource(path);
//  	  if(url == null) 
//  	    throw new IOException("Unable to find: " + path);

//  	  BufferedImage bi = ImageIO.read(url);
//  	  if((bi.getWidth() != size) || (bi.getHeight() != size)) 
//  	    throw new IOException
//  	      ("The image size (" + bi.getWidth() + "x" + bi.getHeight() + ") of " + 
//  	       "texture (" + path + ") does not match the expected size " + 
//  	       "(" + size + "x" + size + ")!");
//  	  if(bi.getType() != BufferedImage.TYPE_CUSTOM) 
//  	    throw new IOException
//  	      ("The image format (" + bi.getType() + ") of texture (" + path + ") is " +
//  	       "not supported!");	    
	  
//  	  byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
//  	  ByteBuffer buf = ByteBuffer.allocateDirect(data.length);
//  	  buf.order(ByteOrder.nativeOrder());
//  	  buf.put(data, 0, data.length);
//  	  buf.rewind();
	  
//  	  gl.glTexImage2D(GL.GL_TEXTURE_2D, level, GL.GL_RGBA, size, size, 
//  			  0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, buf);
//  	}
	  
//  	gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP);
//  	gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP);
	
//  	gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, 
//  			   GL.GL_LINEAR_MIPMAP_LINEAR);
//  	gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
//        }
//     }   
//     catch(IOException ex) {
//        Logs.tex.severe(ex.getMessage());
//     }
    
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
    GL gl = drawable.getGL();
    Random rand = new Random((new Date()).getTime());

    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
    gl.glLoadIdentity();

    Integer texture = null;
    try {
      us.temerity.pipeline.ui.jogl.TextureMgr mgr = 
	us.temerity.pipeline.ui.jogl.TextureMgr.getInstance();
      texture = mgr.getTexture(gl, "ModifiedLinks-Stale-Normal");
    }
    catch(IOException ex) {
      Logs.tex.severe(ex.getMessage());
    }
    
    gl.glEnable(GL.GL_TEXTURE_2D); 
    {
      gl.glBindTexture(GL.GL_TEXTURE_2D, texture);

      gl.glBegin(GL.GL_QUADS);
      {
	float x = rand.nextFloat()*2.0f - 1.0f;  
	float y = rand.nextFloat()*2.0f - 1.0f;  
	float s = 0.25f + rand.nextFloat()*0.25f;
	
	gl.glTexCoord2f(0.0f, 1.0f);
	gl.glVertex3f(x, y, 0.0f);
	
	gl.glTexCoord2f(1.0f, 1.0f);
	gl.glVertex3f(x+s, y, 0.0f);
	
	gl.glTexCoord2f(1.0f, 0.0f);	
	gl.glVertex3f(x+s, y+s, 0.0f);
	
	gl.glTexCoord2f(0.0f, 0.0f);
	gl.glVertex3f(x, y+s, 0.0f);
      }
      gl.glEnd();
    }
    gl.glDisable(GL.GL_TEXTURE_2D); 
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
  {}
 
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
    System.out.print("Mouse Pressed!\n");
    pCanvas.repaint();
  }

  /**
   * Invoked when a mouse button has been released on a component. 
   */ 
  public void 
  mouseReleased(MouseEvent e) {}



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The associated OpenGL rendering canvas.
   */ 
  private GLCanvas  pCanvas;

  /**
   * OpenGL texture objects handles.
   */ 
  private int[]  pTextures; 
}
