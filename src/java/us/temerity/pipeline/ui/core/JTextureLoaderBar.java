// $Id: JTextureLoaderBar.java,v 1.6 2005/01/31 23:02:01 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*; 

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import javax.swing.*;

import net.java.games.jogl.*;

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
    
    pTextures = new LinkedList<String>();
    pIcon21s  = new LinkedList<String>();
    {
      for(SelectionMode mode : SelectionMode.all()) {
	for(OverallQueueState qstate : OverallQueueState.all()) {
	  for(OverallNodeState nstate : OverallNodeState.all()) {
	    String name = (nstate + "-" + qstate + "-" + mode);
	    pTextures.add(name);
	    pIcon21s.add(name);
	  }
	  
	  {
	    String name = ("NeedsCheckOutMajor-" + qstate + "-" + mode);
	    pTextures.add(name);
	    pIcon21s.add(name);
	  }
	  
	  {
	    String name = ("NeedsCheckOutMicro-" + qstate + "-" + mode);
	    pTextures.add(name);
	    pIcon21s.add(name);
	  }
	  
	  pIcon21s.add("Added-" + qstate + "-" + mode);
	  pIcon21s.add("Obsolete-" + qstate + "-" + mode);
	}

	{
	  String name = ("Identical-Finished-Frozen-" + mode);
	  pTextures.add(name);
	  pIcon21s.add(name);
	}
	
	{
	  String name = ("Identical-Stale-Frozen-" + mode);
	  pTextures.add(name);
	  pIcon21s.add(name);
	}
	
	{
	  String name = ("ModifiedLinks-Finished-Frozen-" + mode);
	  pTextures.add(name);
	  pIcon21s.add(name);
	}
	
	{
	  String name = ("ModifiedLinks-Stale-Frozen-" + mode);
	  pTextures.add(name);
	  pIcon21s.add(name);
	}
	
	{
	  String name = ("Conflicted-Finished-Frozen-" + mode);
	  pTextures.add(name);
	  pIcon21s.add(name);
	}
	
	{
	  String name = ("Conflicted-Stale-Frozen-" + mode);
	  pTextures.add(name);
	  pIcon21s.add(name);
	}
	
	{
	  String name = ("NeedsCheckOutMicro-Finished-Frozen-" + mode);
	  pTextures.add(name);
	  pIcon21s.add(name);
	}
	
	{
	  String name = ("NeedsCheckOutMicro-Stale-Frozen-" + mode);
	  pTextures.add(name);
	  pIcon21s.add(name);
	}
	
	{
	  String name = ("NeedsCheckOut-Finished-Frozen-" + mode);
	  pTextures.add(name);
	  pIcon21s.add(name);
	}
	
	{
	  String name = ("NeedsCheckOut-Stale-Frozen-" + mode);
	  pTextures.add(name);
	  pIcon21s.add(name);
	}
	
	{
	  String name = ("NeedsCheckOutMajor-Finished-Frozen-" + mode);
	  pTextures.add(name);
	  pIcon21s.add(name);
	}
	
	{
	  String name = ("NeedsCheckOutMajor-Stale-Frozen-" + mode);
	  pTextures.add(name);
	  pIcon21s.add(name);
	}
      }

      for(SelectionMode mode : SelectionMode.all()) 
	pTextures.add("Blank-" + mode);

      pTextures.add("Collapsed");
      pTextures.add("ColorCircle");

      for(LinkRelationship rel : LinkRelationship.all())
	pTextures.add("LinkRelationship-" + rel);

      for(SelectionMode mode : SelectionMode.all()) {
	pTextures.add("Job-Queued-" + mode);
	pTextures.add("Job-Running-" + mode);
	pTextures.add("Job-Aborted-" + mode);
	pTextures.add("Job-Failed-" + mode);
	pTextures.add("Job-Finished-" + mode);
	pTextures.add("Job-Paused-" + mode);
	pTextures.add("Job-Undefined-" + mode);
      }

      for(SelectionMode mode : SelectionMode.all()) {
	pTextures.add("ExternalJob-Queued-" + mode);
	pTextures.add("ExternalJob-Running-" + mode);
	pTextures.add("ExternalJob-Aborted-" + mode);
	pTextures.add("ExternalJob-Failed-" + mode);
	pTextures.add("ExternalJob-Finished-" + mode);
	pTextures.add("ExternalJob-Paused-" + mode);
      }

      pTextures.add("Cpu");
      pTextures.add("Mem");
      pTextures.add("Disk");
      pTextures.add("Job");
    }

    pInc = 1.0 / ((double) (20 + pTextures.size()*3 + pIcon21s.size()));
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
   GLDrawable drawable
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
	pPercent += pInc * 5.0;
	pCanvas.repaint();
      }
      else if(!pFontLoaded) {
	mgr.registerFont("CharterBTRoman", new CharterBTRomanFontGeometry());
	mgr.verifyFontTextures(gl, "CharterBTRoman");

	pFontLoaded = true;
	pPercent += pInc * 15.0;
	pCanvas.repaint();
      }
      else if(pTexIdx < pTextures.size()) {
	mgr.verifyTexture(gl, pTextures.get(pTexIdx));
	
	pTexIdx++;
	pPercent += pInc * 3.0;
	pCanvas.repaint();
      }
      else if(pIconIdx < pIcon21s.size()) {
	mgr.verifyIcon21(pIcon21s.get(pIconIdx));

	pIconIdx++;
	pPercent += pInc;
	pCanvas.repaint();
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
   GLDrawable drawable, 
   int x, 
   int y, 
   int width, 
   int height
  )
  {
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
   * The names of the textures.
   */ 
  private LinkedList<String>  pTextures;
  
  /**
   * The current texture index.
   */ 
  private int  pTexIdx; 


  /**
   * The names of the 21x21 pixel icons.
   */ 
  private LinkedList<String>  pIcon21s;

  /**
   * The current 21x21 pixel icon index.
   */ 
  private int  pIconIdx; 


  /**
   * Have the font textures been loaded.
   */ 
  private boolean pFontLoaded;

  
  /**
   * The percentage of texture which have already been loaded.
   */ 
  private double  pPercent; 

  /**
   * The increment to the completion percentage for loading one texture.
   */ 
  private double  pInc; 

}
