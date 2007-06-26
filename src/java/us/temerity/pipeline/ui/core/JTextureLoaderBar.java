// $Id: JTextureLoaderBar.java,v 1.14 2007/06/26 05:18:57 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*; 

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import javax.swing.*;
import javax.media.opengl.*;

/*------------------------------------------------------------------------------------------*/
/*   T E X T U R E   L O A D E R   B A R                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * An progress bar component which displays the progress of loads the OpenGL textures 
 * used by Pipeline.
 */ 
public 
class JTextureLoaderBar
  extends JPanel
  implements GLEventListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new panel.
   */
  public 
  JTextureLoaderBar
  (
   GLCanvas canvas, 
   Thread finished
  ) 
  {
    setName("TextureLoaderBar");
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

    {
      pCanvas = canvas;
      pCanvas.setSize(499, 4);
      pCanvas.addGLEventListener(this);

      add(canvas);
    }

    pFinishedTask = finished;
    pLaunched = new AtomicBoolean(false);
    pFirst = true;
    
    pTextures32 = new LinkedList<String>();
    pTextures64 = new LinkedList<String>();
    pIcons21    = new LinkedList<String>();
    {
      pTextures32.add("Node-Ring");
      pIcons21.add("Node-Ring");

      pTextures32.add("Node-Core");
      pIcons21.add("Node-Core");

      pTextures32.add("Node-InstRing");
      pIcons21.add("Node-InstRing");

      pTextures32.add("Node-InstCore");
      pIcons21.add("Node-InstCore");

      for(OverallNodeState nstate : OverallNodeState.all()) {
        pTextures32.add("Node-" + nstate);
        pIcons21.add("Node-" + nstate);
      }

      pTextures32.add("Node-NeedsCheckOutMicro");
      pIcons21.add("Node-NeedsCheckOutMicro");

      pTextures32.add("Node-NeedsCheckOutMajor");
      pIcons21.add("Node-NeedsCheckOutMajor");

      pIcons21.add("Node-Added");
      pIcons21.add("Node-Obsolete");

      pTextures32.add("Collapsed");
      pTextures32.add("Locked");

      for(LinkRelationship rel : LinkRelationship.all())
	pTextures32.add("Link-" + rel);
      pTextures32.add("Link-Core");

      pTextures64.add("Job-Ring");
      pIcons21.add("Job-Ring");

      pTextures64.add("Job-Core");
      pIcons21.add("Job-Core");
      
      pTextures32.add("ExternalJob-Ring");
      pIcons21.add("ExternalJob-Ring");

      pTextures32.add("ExternalJob-Core");
      pIcons21.add("ExternalJob-Core");
      
      pTextures32.add("Cpu");
      pTextures32.add("Mem");
      pTextures32.add("Disk");
      pTextures32.add("Job");
    }

    int total = pTextures32.size()*25 + pTextures64.size()*25 + pIcons21.size()*10 + 1050;
    pInc = 1.0 / ((double) (total)); 
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
    gl.glClearColor(0.0f, 0.5f, 0.5f, 0.0f);
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

    /* set camera position */ 
    {
      gl.glMatrixMode(GL.GL_MODELVIEW);
      gl.glLoadIdentity();
      gl.glTranslated(0.0, 0.0, -1.0);
    }

    /* draw the progress bar */ 
    {
      double x = pPercent*2.0 - 1.0;

      gl.glColor3d(0.0, 1.0, 1.0);
      gl.glBegin(GL.GL_QUADS);
      {
	gl.glVertex2d(-1.0, -1.0);
	gl.glVertex2d(-1.0, 1.0); 
	gl.glVertex2d(x, 1.0);
	gl.glVertex2d(x, -1.0);
      }
      gl.glEnd();
    }
      
    /* load a texture */ 
    try {
      TextureMgr mgr = TextureMgr.getInstance();

      if(pFirst) {
	pFirst = false;
	pPercent += pInc * 50.0;  

	SwingUtilities.invokeLater(new RefreshTask());
      }
      else if(pTex32Idx < pTextures32.size()) {
	mgr.verifyTexture(gl, pTextures32.get(pTex32Idx), 32);
	
	pTex32Idx++;
	pPercent += pInc * 25.0;

	SwingUtilities.invokeLater(new RefreshTask());
      }
      else if(pTex64Idx < pTextures64.size()) {
	mgr.verifyTexture(gl, pTextures64.get(pTex64Idx), 64);
	
	pTex64Idx++;
	pPercent += pInc * 25.0;

	SwingUtilities.invokeLater(new RefreshTask());
      }
      else if(pIcon21Idx < pIcons21.size()) {
	mgr.verifyIcon21(pIcons21.get(pIcon21Idx));

	pIcon21Idx++;
	pPercent += pInc * 10.0;

	SwingUtilities.invokeLater(new RefreshTask());
      }
      else if(!pIconsRebuilt) {
        mgr.rebuildIcons();  
        
        pIconsRebuilt = true;
	pPercent += pInc * 500.0;

	SwingUtilities.invokeLater(new RefreshTask());
      }
      else if(!pFontLoaded) {
	mgr.registerFont(PackageInfo.sGLFont, new CharcoalRegularFontGeometry());
	mgr.verifyFontTextures(gl, PackageInfo.sGLFont);

	pFontLoaded = true;
	pPercent += pInc * 500.0;

	SwingUtilities.invokeLater(new RefreshTask());
      }
      else if(!pLaunched.getAndSet(true)) {
	SwingUtilities.invokeLater(pFinishedTask);
      }
    }
    catch(IOException ex) {	
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 "Unable to load the required textures:\n" + 
	 "  " + ex.getMessage());
      LogMgr.getInstance().flush();
      System.exit(1);	 
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
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the progress bar.
   */ 
  private
  class RefreshTask
    extends Thread
  { 
    RefreshTask() 
    {
      super("JTextureLoaderBar:RefreshTask");
    }

    public void 
    run() 
    {
      pCanvas.repaint();
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7284438412972189542L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The OpenGL rendering canvas.
   */ 
  private GLCanvas  pCanvas;

  /**
   * The thread to start once all textures have been loaded.
   */ 
  private Thread  pFinishedTask; 

  /**
   * Whether the pFinishTask has been started.
   */ 
  private AtomicBoolean  pLaunched; 
  

  /**
   * Whether this is the first display pass.
   */ 
  private boolean  pFirst; 

  /**
   * The names of the mip-mapped textures.
   */ 
  private LinkedList<String>  pTextures32;
  private LinkedList<String>  pTextures64;
  
  /**
   * The current mip-mapped texture index.
   */ 
  private int  pTex32Idx; 
  private int  pTex64Idx; 


  /**
   * The names of the 21x21 pixel icons.
   */ 
  private LinkedList<String>  pIcons21;

  /**
   * The current 21x21 pixel icon index.
   */ 
  private int  pIcon21Idx; 


  /**
   * Whether the font textures have been loaded.
   */ 
  private boolean pFontLoaded;

  /**
   * Whether the Swing icons have been rebuilt. 
   */ 
  private boolean pIconsRebuilt; 

  
  /**
   * The percentage of startup tasks which have already been completed.
   */ 
  private double  pPercent; 

  /**
   * The base increment to the completion percentage for a task. 
   */ 
  private double  pInc; 

}
