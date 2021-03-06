// $Id: TextureLoader.java,v 1.4 2009/12/14 21:48:22 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*; 
import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.media.opengl.*;

/*------------------------------------------------------------------------------------------*/
/*   T E X T U R E   L O A D E R                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Uses an offscreen PBuffer to load the OpenGL textures into a GLContext which will be 
 * shared by all Pipeline OpenGL drawables.  If the OpenGL implementation does not support
 * PBuffers, a visible OpenGL window will be created during texture loading.
 */ 
public 
class TextureLoader
  extends Object
  implements GLEventListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new loader.
   * 
   * @param usePBuffers
   *   Whether to attempt to load textures offscreen using OpenGL pbuffers.
   * 
   * @param capabilties
   *   The OpenGL capabilities to use when creating the shared context.
   * 
   * @param finished
   *   The main application Swing thread to start once all textures have been loaded.
   */
  public 
  TextureLoader
  (
   boolean usePBuffers, 
   GLCapabilities capabilities,
   Thread finished
  ) 
  {
    pFinishedTask = finished;
        
    /* if pbuffers are supported... */ 
    pDrawable = null;
    if(usePBuffers) {
      GLDrawableFactory factory = GLDrawableFactory.getFactory();
      if(factory.canCreateGLPbuffer()) {
        try {
          GLCapabilitiesChooser chooser = new DefaultGLCapabilitiesChooser(); 
          pDrawable = factory.createGLPbuffer(capabilities, chooser, 1, 1, null); 
        }
        catch(GLException ex) {
          LogMgr.getInstance().logAndFlush
            (LogMgr.Kind.Ops, LogMgr.Level.Severe,
             "While attempting to create the OpenGL pbuffer needed to load the " + 
             "textures offscreen:\n  " + ex.getMessage());
        }
      }

      if(pDrawable == null) {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Ops, LogMgr.Level.Warning,
           "Unable to create the OpenGL pbuffer needed to load the textures " + 
           "offscreen, using an onscreen OpenGL window instead."); 
      }
    }

    /* initialize the splash screen */ 
    pSplash = SplashScreen.getSplashScreen();
    if(pSplash != null) {
      try {
        pSplashGraphics = pSplash.createGraphics();
      }
      catch(IllegalStateException ex) {
        // silently ignore, the splash window is already closed...
      }
    }
 
    /* othersiwise, create a visible window during the load... */ 
    if(pDrawable == null) {  
      JFrame frame = new JFrame("plui");
      pRootFrame = frame;

      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      frame.setResizable(false);
      frame.setUndecorated(true);

      {
        pCardPanel = new JPanel(new CardLayout());

        {
          JProgressLabel label = new JProgressLabel(); 
          pProgressLabel = label;
          pCardPanel.add(label, "Splash");
        }

        if(PackageInfo.sUseJava2dGLPipeline) { 
          GLJPanel panel = new GLJPanel(capabilities);
          pDrawable = panel;
          pCardPanel.add(panel, "OpenGL");
        }
        else {
          GLCanvas canvas = new GLCanvas(capabilities);
          pDrawable = canvas;
          pCardPanel.add(canvas, "OpenGL");
        }

        frame.setContentPane(pCardPanel);
      }

      frame.pack(); 

      {
        Rectangle bounds = frame.getGraphicsConfiguration().getBounds();
        frame.setLocation(bounds.x + bounds.width/2 - frame.getWidth()/2, 
                          bounds.y + bounds.height/2 - frame.getHeight()/2);
      }
    }
    
    /* hook up the texture loading GL code to whatever drawable we created */ 
    pDrawable.addGLEventListener(this);
    
    /* generate the names of textures to load */ 
    {
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
        
        pTextures32.add("Node-MissingSome");
        pIcons21.add("Node-MissingSome");
        
        pIcons21.add("Node-Added");
        pIcons21.add("Node-Obsolete");
        
        pTextures32.add("Collapsed");
        pTextures32.add("Locked");
        
        for(LinkRelationship rel : LinkRelationship.all())
          pTextures32.add("Link-" + rel);
        pTextures32.add("Link-Core");
        
        pTextures64.add("Job-Ring");
        pTextures64.add("Job-Core");
        pTextures32.add("ExternalJob-Ring");
        pTextures32.add("ExternalJob-Core");
        
        pTextures32.add("Cpu");
        pTextures32.add("Mem");
        pTextures32.add("Disk");
        pTextures32.add("Job");
      }
      
      pFontChars = new LinkedList<Character>(); 
      {
        FontGeometry geom = new CharcoalRegularFontGeometry();
        TextureMgr.getInstance().registerFont(PackageInfo.sGLFont, geom); 
        
        char code;
        for(code=0; code<sMaxChars; code++) {
          if(geom.isPrintable(code)) 
            pFontChars.add(code);
        }
      }
    }
    
    /* initiate the OpenGL display update which will load the textures */ 
    if(pRootFrame != null) {
      pRootFrame.setVisible(true);
      SwingUtilities.invokeLater(new UpdateTask());
    }
    else {
      pDrawable.display(); 
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Returns the shared OpenGL context in which the textures are loaded.
   */ 
  public GLContext 
  getContext()
  {
    return pDrawable.getContext(); 
  }
   
  /**
   * Hide any onscreen windows created during the texture loading process.
   */ 
  public void 
  hide() 
  {
    if(pRootFrame != null) 
      pRootFrame.setVisible(false); 
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

    /* load the textures */ 
    try {
      TextureMgr mgr = TextureMgr.getInstance();

      int done = 0;
      int total = pTextures32.size() + pTextures64.size() + 
                  pIcons21.size() + pFontChars.size();

      updateSplashProgress(total, done++);

      for(String tex : pTextures32) {
        mgr.verifyTexture(gl, tex, 32);
        updateSplashProgress(total, done++);
      }
      
      for(String tex : pTextures64) {
        mgr.verifyTexture(gl, tex, 64);
        updateSplashProgress(total, done++);
      }

      for(String tex : pIcons21) {
        mgr.verifyIcon21(tex);
        updateSplashProgress(total, done++);
      }
      
      mgr.cacheIconColors(); 
      mgr.rebuildNodeIcons();  
      mgr.rebuildExtraNodeIcons();  
      mgr.rebuildJobIcons();  

      {
        Integer[] fontDLs = new Integer[sMaxChars];
        for(char code : pFontChars) {
          fontDLs[code] = 
            new Integer(mgr.loadCharacterTexture(gl, PackageInfo.sGLFont, code));
          updateSplashProgress(total, done++);
        }

        mgr.setFontTextures(PackageInfo.sGLFont, fontDLs); 
      }

      SwingUtilities.invokeLater(pFinishedTask);
    }
    catch(IOException ex) {	
      LogMgr.getInstance().logAndFlush
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 "Unable to load the required textures:\n" + 
	 "  " + ex.getMessage());
      System.exit(1);	 
    }
  }
   
  private void 
  updateSplashProgress
  (
   int total, 
   int done
  ) 
  {
    if(pSplashGraphics != null) {
      drawSplashProgress(total, done, pSplashGraphics);

      if(pSplash != null) {
        try {
          pSplash.update();
        }
        catch(IllegalStateException ex) {
          // silently ignore, the splash window is already closed...
        }
      }
    }

    if(pProgressLabel != null) 
      pProgressLabel.setProgress(total, done);
  }

  private void 
  drawSplashProgress
  (
   int total, 
   int done, 
   Graphics g
  ) 
  {
    if(g == null) 
      return;

    float percentage = ((float) done) / ((float) total);
    
    g.setPaintMode();
    g.setColor(new Color(15, 90, 88));
    g.fillRect(156, 99, 136, 13);
    g.setColor(Color.WHITE);
    g.drawString(PackageInfo.sVersion, 158, 110);
    g.setColor(Color.WHITE);
    g.fillRect(12, 122, (int)(percentage * 394.0), 10);
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
  {}
 
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

  private
  class UpdateTask
    extends Thread
  { 
    UpdateTask() 
    {
      super("TextureLoader:UpdateTask");
    }

    public void 
    run() 
    { 
      pDrawable.display(); 
    }
  }

  private 
  class JProgressLabel
    extends JLabel
  {
    public 
    JProgressLabel() 
    {
      super(sTexturesSplashIcon);
    }

    public void 
    setProgress
    (
     int total, 
     int done
    ) 
    {
      pTotal = total;
      pDone  = done;
      Graphics g = pProgressLabel.getGraphics(); 
      drawSplashProgress(pTotal, pDone, g);       
    }

    private static final long serialVersionUID = -6124398218219177414L;

    private int pTotal;
    private int pDone;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  //private static final long serialVersionUID =

  private static final int sMaxChars = 128;

  private static final Icon sTexturesSplashIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("Splash.png"));


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The OpenGL drawable owning the context where the textures are loaded. 
   */ 
  private GLAutoDrawable  pDrawable;

  /**
   * The onscreen Swing rendering window used to show the OpenGL drawable if offscreen 
   * rendering via pbuffers is not available.  
   */ 
  private JFrame pRootFrame; 
  private JPanel pCardPanel; 

  /**
   * Shows loading progress when the onscreen OpenGL window is required.
   */ 
  private JProgressLabel pProgressLabel;

  /**
   * The splash screen and its graphics context for showing loading progress.
   */
  private SplashScreen   pSplash;
  private Graphics       pSplashGraphics;

  /**
   * The thread to start once all textures have been loaded.
   */ 
  private Thread  pFinishedTask; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The names of the mip-mapped textures to load.
   */ 
  private LinkedList<String>  pTextures32;
  private LinkedList<String>  pTextures64;
  
  /**
   * The names of the 21x21 pixel icons to load.
   */ 
  private LinkedList<String>  pIcons21;

  /**
   * The character codes for the font textures to load.
   */ 
  private LinkedList<Character>  pFontChars; 

}
