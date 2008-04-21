// $Id: TextureMgr.java,v 1.8 2008/04/21 06:15:11 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.nio.*;
import java.net.*;

import javax.imageio.*;
import javax.swing.*;
import javax.media.opengl.*;

/*------------------------------------------------------------------------------------------*/
/*   T E X T U R E   M G R                                                                  */
/*------------------------------------------------------------------------------------------*/

/** 
 * Manages a cached set of textures used by {@link JNodeViewerPanel JNodeViewerPanel} to
 * render Pipeline nodes. <P> 
 * 
 * Node textures are cached as OpenGL Texture Objects and {@link ImageIcon ImageIcon} 
 * instances loaded from image files which are used to graphically represent permutations of 
 * {@link OverallNodeState OverallNodeState} and {@link OverallQueueState OverallQueueState}
 * of Pipeline nodes. <P> 
 * 
 * Font textures are also cached as OpenGL Texture Objects which are used to render node 
 * and link labels in <CODE>JNodeViewerPanel</CODE> instances. <P> 
 */ 
public
class TextureMgr
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct the sole instance.
   */ 
  private 
  TextureMgr()
  {
    pFontTextures = new HashMap<String,Integer[]>();
    pFontGeometry = new HashMap<String,FontGeometry>();

    pIconTextures = new HashMap<String,Integer>();
    pIcons        = new HashMap<String,ImageIcon[]>();
    pIconImages   = new HashMap<String,BufferedImage[]>();
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the TextureMgr instance.
   */ 
  public static TextureMgr
  getInstance() 
  {
    return sTextureMgr;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   F O N T S                                                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Register a font for loading.
   * 
   * @param name
   *   The name of the font.
   * 
   * @param geom
   *   The font geometry.
   */ 
  public synchronized void
  registerFont
  (
   String name, 
   FontGeometry geom
  ) 
  {
    if(name == null)
      throw new IllegalArgumentException("The font name cannot be (null)!");
    
    if(geom == null)
      throw new IllegalArgumentException("The font geometry cannot be (null)!");
      
    pFontGeometry.put(name, geom);
  }
   

  /*----------------------------------------------------------------------------------------*/

  /** 
   * Verify that all of the per-character textures for the given font are currently loaded.
   * 
   * If the textures are not currently loaded then read the Mip-Map level images from 
   * disk and used them to generate the textures.
   * 
   * @param gl
   *   The OpenGL interface.
   * 
   * @param name
   *   The name of the font.
   * 
   * @throws IOException
   *   If unable to load the source images.
   */ 
  public synchronized void
  verifyFontTextures
  (
   GL gl, 
   String name
  ) 
    throws IOException
  {
    if(name == null)
      throw new IllegalArgumentException("The font name cannot be (null)!");

    /* make sure they haven't already been loaded */ 
    if(pFontTextures.containsKey(name)) 
      return;

    /* verify that the font has been registered */ 
    FontGeometry geom = pFontGeometry.get(name);
    if(geom == null)
      throw new IllegalArgumentException
	("The font (" + name + ") has not been registered!");
    
    /* build the texture and icon */ 
    {
      Integer texs[] = new Integer[128];

      char code;
      for(code=0; code<texs.length; code++) {
	if(geom.isPrintable(code)) 
          texs[code] = loadCharacterTexture(gl, name, code);
      }

      setFontTextures(name, texs); 
    }
  }

  /** 
   * Set the OpenGL texture handles for a given font.
   * 
   * This method should only be called by the {@link #verifyFontTextures verifyFontTextures} 
   * method or by {@link JTextureLoaderBar} during plui(1) startup.
   */ 
  public synchronized void
  setFontTextures
  (
   String name, 
   Integer[] handles
  ) 
  {
    if(name == null)
      throw new IllegalArgumentException("The font name cannot be (null)!");

    /* make sure they haven't already been loaded */ 
    if(pFontTextures.containsKey(name)) 
      return;

    pFontTextures.put(name, handles);
  }
  
  /** 
   * Load the Mip-Map level images from disk and used them to generate the textures for 
   * a specific character in the given font.
   * 
   * This method should only be called by the {@link #verifyFontTextures verifyFontTextures} 
   * method or by {@link JTextureLoaderBar} during plui(1) startup.
   * 
   * @return 
   *   The OpenDL texture handle for the character.
   */ 
  public synchronized int
  loadCharacterTexture
  (
   GL gl, 
   String name, 
   char code
  ) 
    throws IOException
  {
    LogMgr.getInstance().log
      (LogMgr.Kind.Tex, LogMgr.Level.Fine,
       "Loading Font Texture: " + name + " \"" + code + "\"");

    int handle[] = new int[1];
    gl.glGenTextures(1, handle, 0); 
    
    gl.glBindTexture(GL.GL_TEXTURE_2D, handle[0]);
    
    int level, size; 
    for(level=0, size=sMaxFontRes; size>=1; level++, size/=2) {
      LogMgr.getInstance().log
        (LogMgr.Kind.Tex, LogMgr.Level.Finer,
         "Loading MipMap: " + size + "x" + size);
      LogMgr.getInstance().flush();
	    
      int icode = (int) code;
      String path = ("fonts/" + name + "/" + icode + "/texture." + size + ".png");
      URL url = LookAndFeelLoader.class.getResource(path);
      if(url == null) 
        throw new IOException("Unable to find: " + path);
      BufferedImage bi = ImageIO.read(url);

      if((bi.getWidth() != size) || (bi.getHeight() != size)) 
        throw new IOException
          ("The image size (" + bi.getWidth() + "x" + bi.getHeight() + ") of " + 
           "texture (" + path + ") does not match the expected size " + 
           "(" + size + "x" + size + ")!");

      if(bi.getType() != BufferedImage.TYPE_CUSTOM) 
        throw new IOException
          ("The image format (" + bi.getType() + ") of texture (" + path + ") is " +
           "not supported!");	    
	    
      byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
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

    return handle[0];
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the OpenGL texture object handle for the given character of the given font.
   * 
   * @param gl
   *   The OpenGL interface.
   * 
   * @param name
   *   The symbolic name of the font.
   * 
   * @param code
   *   The character code.
   * 
   * @return 
   *   The handle or <CODE>null</CODE> if the given character is unprintable.
   * 
   * @throws PipelineException
   *   If unable to retrieve the font textures.
   */ 
  public synchronized Integer
  getFontTexture
  (
   GL gl, 
   String name, 
   char code
  ) 
    throws PipelineException 
  { 
    if((code < 0) || (code > 127))
      throw new IllegalArgumentException
	("The character code (" + ((int) code) + ") for character " + 
	 "\"" + code + "\" must be in the [0-127] range!");

    Integer dl = pFontTextures.get(name)[code];
    if(dl == null)
      throw new PipelineException
        ("Unable to find an OpenGL texture for font (" + name + "), code = " + code + "!");
    
    return dl; 
  }

  /**
   * Get the geometric description of the characters in the given font.
   * 
   * @param name
   *   The symbolic name of the font.
   * 
   * @return
   *   The geometric description or <CODE>null</CODE> if the font is unregistered.
   */ 
  public synchronized FontGeometry
  getFontGeometry
  (
   String name
  ) 
  {
    return pFontGeometry.get(name);
  }
    

 
  /*----------------------------------------------------------------------------------------*/
  /*   T E X T U R E S                                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  /** 
   * Verify that the texture/icon with the given name is currently loaded.
   * 
   * If the texture/icon is not currently loaded then read the Mip-Map level images from 
   * disk and used them to generate the texture and icon.
   * 
   * @param gl
   *   The OpenGL interface.
   * 
   * @param name
   *   The name of the texture.
   * 
   * @throws IOException
   *   If unable to load the source images.
   */ 
  public synchronized void
  verifyTexture
  (
   GL gl, 
   String name
  ) 
    throws IOException
  {
    verifyTexture(gl, name, 32);
  }

  /** 
   * Verify that the texture/icon with the given name is currently loaded.
   * 
   * If the texture/icon is not currently loaded then read the Mip-Map level images from 
   * disk and used them to generate the texture and icon.
   * 
   * @param gl
   *   The OpenGL interface.
   * 
   * @param name
   *   The name of the texture.
   * 
   * @param iconSize
   *   The size of Swing icon images to cache while loading the textures for OpenGL. 
   * 
   * @throws IOException
   *   If unable to load the source images.
   */ 
  public synchronized void
  verifyTexture
  (
   GL gl, 
   String name, 
   int iconSize
  ) 
    throws IOException
  {
    if(name == null)
      throw new IllegalArgumentException("The texture name cannot be (null)!");

    /* make sure they haven't already been loaded */ 
    if(pIconTextures.containsKey(name)) {
      assert(pIconImages.containsKey(name));
      return;
    }

    /* build the texture and icon */ 
    LogMgr.getInstance().log
      (LogMgr.Kind.Tex, LogMgr.Level.Fine,
       "Loading Texture: " + name);
    {
      int handle[] = new int[1];
      gl.glGenTextures(1, handle, 0); 

      gl.glBindTexture(GL.GL_TEXTURE_2D, handle[0]);

      int level, size; 
      for(level=0, size=sMaxTexRes; size>=1; level++, size/=2) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Tex, LogMgr.Level.Finer,
	   "Loading MipMap: " + size + "x" + size);
	LogMgr.getInstance().flush();

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
	
	if(bi.getType() != BufferedImage.TYPE_CUSTOM) 
	  throw new IOException
	    ("The image format (" + bi.getType() + ") of texture (" + path + ") is " +
	     "not supported!");	    
	
	byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
	ByteBuffer buf = ByteBuffer.allocateDirect(data.length);
	buf.order(ByteOrder.nativeOrder());
	buf.put(data, 0, data.length);
	buf.rewind();
	
	gl.glTexImage2D(GL.GL_TEXTURE_2D, level, GL.GL_RGBA, size, size, 
			0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, buf);
	
        /* cache the Swing icon while we're at it... */ 
        if(size == iconSize) {
          int wk; 
          for(wk=0; wk<sIconRes.length; wk++) {
            if(size == sIconRes[wk]) {
              BufferedImage images[] = pIconImages.get(name);
              if(images == null) {
                images = new BufferedImage[sIconRes.length];
                pIconImages.put(name, images);
              }

              images[wk] = bi; 
            }
          }
        }
      }
	    
      gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP);
      gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP);
      
      gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, 
			 GL.GL_LINEAR_MIPMAP_LINEAR);
      gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);

      pIconTextures.put(name, handle[0]);
    }
  }

  /** 
   * Verify that the 64x64 icon with the given name is currently loaded.
   * 
   * If the 64x64 icon is not currently loaded then read icon image from disk.
   * 
   * @param name
   *   The name of the texture.
   * 
   * @throws IOException
   *   If unable to load the sourc image.
   */ 
  public synchronized void
  verifyIcon64
  (
   String name
  )
    throws IOException 
  {
    verifyIconHelper(name, 2);
  } 	

  /** 
   * Verify that the 32x32 icon with the given name is currently loaded.
   * 
   * If the 32x32 icon is not currently loaded then read icon image from disk.
   * 
   * @param name
   *   The name of the texture.
   * 
   * @throws IOException
   *   If unable to load the sourc image.
   */ 
  public synchronized void
  verifyIcon32
  (
   String name
  )
    throws IOException 
  {
    verifyIconHelper(name, 0);
  } 	

  /** 
   * Verify that the 21x21 icon with the given name is currently loaded.
   * 
   * If the 21x21 icon is not currently loaded then read icon image from disk.
   * 
   * @param name
   *   The name of the texture.
   * 
   * @throws IOException
   *   If unable to load the sourc image.
   */ 
  public synchronized void
  verifyIcon21
  (
   String name
  )
    throws IOException 
  {
    verifyIconHelper(name, 1);
  } 	

  /** 
   * Verify that the icon with the given index and name is currently loaded.
   * 
   * If the icon is not currently loaded then read icon image from disk.
   * 
   * @param name
   *   The name of the texture.
   * 
   * @param index
   *   The icon resolution index.
   * 
   * @throws IOException
   *   If unable to load the source image.
   */ 
  private synchronized void
  verifyIconHelper
  (
   String name, 
   int idx
  )
    throws IOException 
  {
    if(name == null)
      throw new IllegalArgumentException("The icon name cannot be (null)!");

    /* make it hasn't already been loaded */ 
    BufferedImage images[] = pIconImages.get(name);
    if((images != null) && (images[idx] != null))
      return;

    int size = sIconRes[idx];
	  
    LogMgr.getInstance().log
      (LogMgr.Kind.Tex, LogMgr.Level.Fine,
       "Loading Icon: " + name + " " + size + "x" + size);
    LogMgr.getInstance().flush();
    
    String path = ("textures/" + name + "/texture." + size + ".png");
    URL url = LookAndFeelLoader.class.getResource(path);
    if(url == null) 
      throw new IOException("Unable to find: " + path);
    BufferedImage bi = ImageIO.read(url);
      
    if(images == null) {
      images = new BufferedImage[sIconRes.length];
      pIconImages.put(name, images);
    }

    images[idx] = bi; 
  } 	


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Composite a new set of Swing icons using the current user preferences for selection
   * and queue state colors.
   */ 
  public synchronized void
  rebuildIcons() 
  {
    /* if no colors have changes, no need to rebuild... */ 
    UserPrefs prefs = UserPrefs.getInstance();
    if((pNormalRingColor != null) &&
       pNormalRingColor.equiv(prefs.getNormalRingColor()) &&
       (pSelectedRingColor != null) && 
       pSelectedRingColor.equiv(prefs.getSelectedRingColor()) &&
       (pPrimaryRingColor != null) && 
       pPrimaryRingColor.equiv(prefs.getPrimaryRingColor()) &&
       (pFinishedCoreColor != null) && 
       pFinishedCoreColor.equiv(prefs.getFinishedCoreColor()) &&
       (pStaleCoreColor != null) && 
       pStaleCoreColor.equiv(prefs.getStaleCoreColor()) &&
       (pQueuedCoreColor != null) && 
       pQueuedCoreColor.equiv(prefs.getQueuedCoreColor()) &&
       (pPausedCoreColor != null) && 
       pPausedCoreColor.equiv(prefs.getPausedCoreColor()) &&
       (pRunningCoreColor != null) && 
       pRunningCoreColor.equiv(prefs.getRunningCoreColor()) &&
       (pAbortedCoreColor != null) && 
       pAbortedCoreColor.equiv(prefs.getAbortedCoreColor()) &&
       (pFailedCoreColor != null) && 
       pFailedCoreColor.equiv(prefs.getFailedCoreColor()) &&
       (pPreemptedCoreColor != null) && 
       pPreemptedCoreColor.equiv(prefs.getPreemptedCoreColor()) &&
       (pLightweightCoreColor != null) && 
       pLightweightCoreColor.equiv(prefs.getLightweightCoreColor()) &&
       (pUndefinedCoreColor != null) && 
       pUndefinedCoreColor.equiv(prefs.getUndefinedCoreColor()) &&
       (pModifiableColor != null) && 
       pModifiableColor.equiv(prefs.getModifiableColor()) &&
       (pFrozenFinishedColor != null) && 
       pFrozenFinishedColor.equiv(prefs.getFrozenFinishedColor()) && 
       (pFrozenStaleColor != null) && 
       pFrozenStaleColor.equiv(prefs.getFrozenStaleColor()))
      return; 

    cacheIconColors(); 

    pIcons.clear();

    rebuildNodeIcons(); 
    rebuildExtraNodeIcons(); 
    rebuildJobIcons(); 
  }

  /**
   * Cache the current user preferences for Swing icon colors.<P> 
   * 
   * This method should only be called by the {@link #rebuildIcons rebuildIcons} method
   * or by {@link JTextureLoaderBar} during plui(1) startup.
   */ 
  public synchronized void
  cacheIconColors() 
  {
    UserPrefs prefs = UserPrefs.getInstance();

    pNormalRingColor      = prefs.getNormalRingColor();      
    pSelectedRingColor    = prefs.getSelectedRingColor();    
    pPrimaryRingColor     = prefs.getPrimaryRingColor();     
    pFinishedCoreColor    = prefs.getFinishedCoreColor();    
    pStaleCoreColor       = prefs.getStaleCoreColor();       
    pQueuedCoreColor      = prefs.getQueuedCoreColor();      
    pPausedCoreColor      = prefs.getPausedCoreColor();      
    pRunningCoreColor     = prefs.getRunningCoreColor();     
    pAbortedCoreColor     = prefs.getAbortedCoreColor();     
    pFailedCoreColor      = prefs.getFailedCoreColor();      
    pPreemptedCoreColor   = prefs.getPreemptedCoreColor();   
    pLightweightCoreColor = prefs.getLightweightCoreColor(); 
    pUndefinedCoreColor   = prefs.getUndefinedCoreColor();   
    pModifiableColor      = prefs.getModifiableColor();      
    pFrozenFinishedColor  = prefs.getFrozenFinishedColor();          
    pFrozenStaleColor     = prefs.getFrozenStaleColor();          
  }

  /**
   * Composite a new set of Swing icons using the current user preferences for selection
   * and queue state colors.<P> 
   * 
   * This method should only be called by the {@link #rebuildIcons rebuildIcons} method
   * or by {@link JTextureLoaderBar} during plui(1) startup.
   */ 
  public synchronized void
  rebuildNodeIcons() 
  {
    LinkedList<SelectionMode> modes = new LinkedList<SelectionMode>(); 
    modes.add(SelectionMode.Normal); 
    modes.add(SelectionMode.Selected); 

    /* node icons */ 
    {
      BufferedImage ringImgs[] = pIconImages.get("Node-Ring");
      BufferedImage coreImgs[] = pIconImages.get("Node-Core");

      /* 
       * 21x21: <OverallNodeState>-<OverallQueueState>-Normal
       *        <OverallNodeState>-<OverallQueueState>-Selected
       * 
       * 32x32: <OverallNodeState>-<OverallQueueState>-Normal             
       */ 
      for(OverallNodeState nstate : OverallNodeState.all()) {
        BufferedImage nodeStateImgs[] = pIconImages.get("Node-" + nstate);

        for(OverallQueueState qstate : OverallQueueState.all()) {
          Color3d coreColor = NodeStyles.getQueueColor3d(qstate);

          int idx;
          for(idx=0; idx < 2; idx++) {  // 32x32, 21x21
            int size = sIconRes[idx];

            for(SelectionMode mode : modes) {
              if((mode == SelectionMode.Normal) || (size == 21)) {
                Color3d ringColor = NodeStyles.getSelectionColor3d(mode);

                /* normal */ 
                {
                  String name = (nstate + "-" + qstate + "-" + mode);   
                  ImageIcon icons[] = pIcons.get(name); 
                  if(icons == null) {
                    icons = new ImageIcon[sIconRes.length];
                    pIcons.put(name, icons);
                  }
                  
                  LogMgr.getInstance().log
                    (LogMgr.Kind.Tex, LogMgr.Level.Fine,
                     "Building Icon: " + name + " " + size + "x" + size);
                  LogMgr.getInstance().flush();
                  
                  {
                    BufferedImage result = 
                      compositeNodeImages(ringImgs[idx], ringColor, 
                                          coreImgs[idx], coreColor, 
                                          nodeStateImgs[idx], pModifiableColor);
                    
                    icons[idx] = new ImageIcon(result);
                  }
                }
                
                /* frozen */ 
                switch(nstate) {
                case Identical:
                case Missing:
                case MissingNewer:
                case ModifiedLinks:
                case TrivialMod:
                case Conflicted: 
                case NeedsCheckOut: 
                  switch(qstate) {
                  case Finished:
                  case Stale:
                    {
                      String name = (nstate + "-" + qstate + "-Frozen-" + mode);   
                      ImageIcon icons[] = pIcons.get(name); 
                      if(icons == null) {
                        icons = new ImageIcon[sIconRes.length];
                        pIcons.put(name, icons);
                      }
                  
                      LogMgr.getInstance().log
                        (LogMgr.Kind.Tex, LogMgr.Level.Fine,
                         "Building Icon: " + name + " " + size + "x" + size);
                      LogMgr.getInstance().flush();
                      
                      {
                        Color3d frozen = pFrozenFinishedColor; 
                        if(qstate == OverallQueueState.Stale)
                          frozen = pFrozenStaleColor; 

                        BufferedImage result = 
                          compositeNodeImages(ringImgs[idx], ringColor, 
                                              coreImgs[idx], coreColor, 
                                              nodeStateImgs[idx], frozen);
                        
                        icons[idx] = new ImageIcon(result);
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
      
  /**
   * Composite a new set of Swing icons using the current user preferences for selection
   * and queue state colors.<P> 
   * 
   * This method should only be called by the {@link #rebuildIcons rebuildIcons} method
   * or by {@link JTextureLoaderBar} during plui(1) startup.
   */ 
  public synchronized void
  rebuildExtraNodeIcons() 
  {
    LinkedList<SelectionMode> modes = new LinkedList<SelectionMode>(); 
    modes.add(SelectionMode.Normal); 
    modes.add(SelectionMode.Selected); 

    /* node icons */ 
    {
      BufferedImage ringImgs[] = pIconImages.get("Node-Ring");
      BufferedImage coreImgs[] = pIconImages.get("Node-Core");

      /**
       * 21x21: Blank-Normal
       *        Blank-Selected 
       *        Lightweight-Normal
       *        Lightweight-Selected
       * 
       * 32x32: Blank-Normal 
       *        Blank-Selected
       *        Lightweight-Normal
       *        Lightweight-Selected
       */ 
      {
        String[] prefix = { "Blank", "Lightweight" };
        Color3d[] colors = { pUndefinedCoreColor, pLightweightCoreColor };
        
        int wk;
        for(wk=0; wk<prefix.length; wk++) {

          int idx;
          for(idx=0; idx < 2; idx++) { // 32x32, 21x21
            int size = sIconRes[idx];

            for(SelectionMode mode : modes) {
              if((mode == SelectionMode.Normal) || (size == 21)) {
                Color3d ringColor = NodeStyles.getSelectionColor3d(mode);
                
                String name = (prefix[wk] + "-" + mode);   
                ImageIcon icons[] = pIcons.get(name); 
                if(icons == null) {
                  icons = new ImageIcon[sIconRes.length];
                  pIcons.put(name, icons);
                }

                LogMgr.getInstance().log
                  (LogMgr.Kind.Tex, LogMgr.Level.Fine,
                   "Building Icon: " + name + " " + size + "x" + size);
                LogMgr.getInstance().flush();
                
                {
                  BufferedImage result = 
                    compositeNodeImages(ringImgs[idx], ringColor, 
                                        coreImgs[idx], colors[wk]);
                  
                  icons[idx] = new ImageIcon(result);
                }
              }
            }
          }                
        }
      }

      /**
       * 32x32: NeedsCheckOutMajor-<OverallQueueState>-Normal
       *        NeedsCheckOutMicro-<OverallQueueState>-Normal
       */
      {
        String[] prefix = { "NeedsCheckOutMajor", "NeedsCheckOutMicro" };
        
        SelectionMode mode = SelectionMode.Normal;
        Color3d ringColor = NodeStyles.getSelectionColor3d(mode);

        int size = sIconRes[0]; // 32x32

        int wk;
        for(wk=0; wk<prefix.length; wk++) {
          BufferedImage nodeStateImgs[] = pIconImages.get("Node-" + prefix[wk]);

          for(OverallQueueState qstate : OverallQueueState.all()) {
            Color3d coreColor = NodeStyles.getQueueColor3d(qstate);
            
            {
              String name = (prefix[wk] + "-" + qstate + "-Normal");  
              ImageIcon icons[] = pIcons.get(name); 
              if(icons == null) {
                icons = new ImageIcon[sIconRes.length];
                pIcons.put(name, icons);
              }
              
              LogMgr.getInstance().log
                (LogMgr.Kind.Tex, LogMgr.Level.Fine,
                 "Building Icon: " + name + " " + size + "x" + size);
              LogMgr.getInstance().flush();
              
              {
                BufferedImage result = 
                  compositeNodeImages(ringImgs[0], ringColor, 
                                      coreImgs[0], coreColor, 
                                      nodeStateImgs[0], pModifiableColor);
                
                icons[0] = new ImageIcon(result);
              }
            }
            
            /* frozen */ 
            switch(qstate) {
            case Finished:
            case Stale:
              {
                String name = (prefix[wk] + "-" + qstate + "-Frozen-Normal");  
                ImageIcon icons[] = pIcons.get(name); 
                if(icons == null) {
                  icons = new ImageIcon[sIconRes.length];
                  pIcons.put(name, icons);
                }
              
                LogMgr.getInstance().log
                  (LogMgr.Kind.Tex, LogMgr.Level.Fine,
                   "Building Icon: " + name + " " + size + "x" + size);
                LogMgr.getInstance().flush();
                
                {
                  Color3d frozen = pFrozenFinishedColor; 
                  if(qstate == OverallQueueState.Stale)
                    frozen = pFrozenStaleColor; 

                  BufferedImage result = 
                    compositeNodeImages(ringImgs[0], ringColor, 
                                        coreImgs[0], coreColor, 
                                        nodeStateImgs[0], frozen);
                  
                  icons[0] = new ImageIcon(result);
                }
              }
            }
          }
        }
      }

      /**
       * 21x21: <FileState>-<OverallQueueState>-Normal             
       *        <FileState>-<OverallQueueState>-Selected            
       */
      {
        String[] prefix = { "Added", "Obsolete" };

        int size = sIconRes[1]; // 21x21

        int wk;
        for(wk=0; wk<prefix.length; wk++) {
          BufferedImage nodeStateImgs[] = pIconImages.get("Node-" + prefix[wk]);
          
          for(OverallQueueState qstate : OverallQueueState.all()) {
            Color3d coreColor = NodeStyles.getQueueColor3d(qstate);
            
            for(SelectionMode mode : modes) {
              Color3d ringColor = NodeStyles.getSelectionColor3d(mode);
              
              /* normal */ 
              {
                String name = (prefix[wk] + "-" + qstate + "-" + mode); 
                ImageIcon icons[] = pIcons.get(name); 
                if(icons == null) {
                  icons = new ImageIcon[sIconRes.length];
                  pIcons.put(name, icons);
                }

                LogMgr.getInstance().log
                  (LogMgr.Kind.Tex, LogMgr.Level.Fine,
                   "Building Icon: " + name + " " + size + "x" + size);
                LogMgr.getInstance().flush();
                  
                {
                  BufferedImage result = 
                    compositeNodeImages(ringImgs[1], ringColor, 
                                        coreImgs[1], coreColor, 
                                        nodeStateImgs[1], pModifiableColor);
                    
                  icons[1] = new ImageIcon(result);
                }
              }

              /* frozen */ 
              switch(qstate) {
              case Finished:
              case Stale:
                {
                  String name = (prefix[wk] + "-" + qstate + "Frozen-" + mode); 
                  ImageIcon icons[] = pIcons.get(name); 
                  if(icons == null) {
                    icons = new ImageIcon[sIconRes.length];
                    pIcons.put(name, icons);
                  }
                  
                  LogMgr.getInstance().log
                    (LogMgr.Kind.Tex, LogMgr.Level.Fine,
                     "Building Icon: " + name + " " + size + "x" + size);
                  LogMgr.getInstance().flush();
                  
                  {
                    Color3d frozen = pFrozenFinishedColor; 
                    if(qstate == OverallQueueState.Stale)
                      frozen = pFrozenStaleColor; 

                    BufferedImage result = 
                      compositeNodeImages(ringImgs[1], ringColor, 
                                          coreImgs[1], coreColor, 
                                          nodeStateImgs[1], frozen);
                    
                    icons[1] = new ImageIcon(result);
                  }
                }
              }
            }
          }
        }
      }
    }

    /* node instance icons */ 
    {
      BufferedImage ringImgs[] = pIconImages.get("Node-InstRing");
      BufferedImage coreImgs[] = pIconImages.get("Node-InstCore");

      /**
       * 32x32: <OverallNodeState>-Instance-Normal   
       */ 
      {
        String[] prefix = { 
          "CheckedIn", "Pending", "Identical", "Modified", "NeedsCheckOut", "Conflicted"
        };

        Color3d ringColor = NodeStyles.getSelectionColor3d(SelectionMode.Normal); 
        Color3d coreColor = NodeStyles.getQueueColor3d(OverallQueueState.Finished);

        int size = sIconRes[0];  // 32x32

        int wk;
        for(wk=0; wk<prefix.length; wk++) {  
          BufferedImage nodeStateImgs[] = pIconImages.get("Node-" + prefix[wk]);
            
          String name = (prefix[wk] + "-Instance-Normal");   
          ImageIcon icons[] = pIcons.get(name); 
          if(icons == null) {
            icons = new ImageIcon[sIconRes.length];
            pIcons.put(name, icons);
          }
            
          LogMgr.getInstance().log
            (LogMgr.Kind.Tex, LogMgr.Level.Fine,
             "Building Icon: " + name + " " + size + "x" + size);
          LogMgr.getInstance().flush();
          
          {
            BufferedImage result = 
              compositeNodeImages(ringImgs[0], ringColor, 
                                  coreImgs[0], coreColor, 
                                  nodeStateImgs[0], pModifiableColor);
            
            icons[0] = new ImageIcon(result);
          }
        }
      }
    }
  }

  /**
   * Composite a new set of Swing icons using the current user preferences for selection
   * and queue state colors.<P> 
   * 
   * This method should only be called by the {@link #rebuildIcons rebuildIcons} method
   * or by {@link JTextureLoaderBar} during plui(1) startup.
   */ 
  public synchronized void
  rebuildJobIcons() 
  {
    BufferedImage ringImgs[] = pIconImages.get("Job-Ring");
    BufferedImage coreImgs[] = pIconImages.get("Job-Core");
    
    Color3d ringColor = NodeStyles.getSelectionColor3d(SelectionMode.Normal);
    
    int size = sIconRes[2]; // 64x32
    
    /**
     * 64x32: Job-<JobState>-Normal
     */ 
    {
      for(JobState jstate : JobState.all()) {
        Color3d coreColor = NodeStyles.getJobColor3d(jstate);
        
        String name = ("Job-" + jstate + "-Normal"); 
        ImageIcon icons[] = pIcons.get(name); 
        if(icons == null) {
          icons = new ImageIcon[sIconRes.length];
          pIcons.put(name, icons);
        }
                  
        LogMgr.getInstance().log
          (LogMgr.Kind.Tex, LogMgr.Level.Fine,
           "Building Icon: " + name + " " + size + "x" + size);
        LogMgr.getInstance().flush();
          
        {
          BufferedImage result = 
            compositeNodeImages(ringImgs[2], ringColor, 
                                coreImgs[2], coreColor); 
    
          WritableRaster raster = 
            result.getRaster().createWritableChild(0, 16, 64, 32, 0, 0, null); 
          BufferedImage bi = new BufferedImage(result.getColorModel(), raster, true, null);

          icons[2] = new ImageIcon(bi);
        }
      }
    }

    /**
     * 64x32: Job-Undefined-Normal
     */ 
    {
      JobState jstate = null;
      Color3d coreColor = NodeStyles.getJobColor3d(jstate);
        
      String name = ("Job-Undefined-Normal"); 
      ImageIcon icons[] = pIcons.get(name); 
      if(icons == null) {
        icons = new ImageIcon[sIconRes.length];
        pIcons.put(name, icons);
      }
        
      LogMgr.getInstance().log
        (LogMgr.Kind.Tex, LogMgr.Level.Fine,
         "Building Icon: " + name + " " + size + "x" + size);
      LogMgr.getInstance().flush();
        
      {
        BufferedImage result = 
          compositeNodeImages(ringImgs[2], ringColor, 
                              coreImgs[2], coreColor); 
          
        WritableRaster raster = 
          result.getRaster().createWritableChild(0, 16, 64, 32, 0, 0, null); 
        BufferedImage bi = new BufferedImage(result.getColorModel(), raster, true, null);

        icons[2] = new ImageIcon(bi);
      }
    }
  }
   
  /**
   * Composite a node icon out of two seperately colored layer images. <P> 
   */ 
  private BufferedImage
  compositeNodeImages
  (
   BufferedImage ring,
   Color3d ringColor, 
   BufferedImage core, 
   Color3d coreColor
  ) 
  {
    return compositeNodeImages(ring, ringColor, core, coreColor, null, null); 
  }

  /**
   * Composite a node icon out of two or three seperately colored layer images. <P> 
   * 
   * The last layer (symbol) can be <CODE>null</CODE>.
   */ 
  private BufferedImage
  compositeNodeImages
  (
   BufferedImage ring,
   Color3d ringColor, 
   BufferedImage core, 
   Color3d coreColor, 
   BufferedImage symbol, 
   Color3d symbolColor
  ) 
  {
    Raster ringRaster = rescaleRaster(ring.getRaster(), ringColor);
    Raster coreRaster = rescaleRaster(core.getRaster(), coreColor);

    Raster symbolRaster = null;
    if(symbol != null) 
      symbolRaster = rescaleRaster(symbol.getRaster(), symbolColor); 
    
    WritableRaster out = ringRaster.createCompatibleWritableRaster();

    RenderingHints hints = new RenderingHints(RenderingHints.KEY_RENDERING, 
                                              RenderingHints.VALUE_RENDER_QUALITY);
    
    AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);
    ColorModel colorModel = ring.getColorModel(); 
    CompositeContext context = ac.createContext(colorModel, colorModel, hints);

    context.compose(coreRaster, ringRaster, out);
    if(symbolRaster != null) 
      context.compose(symbolRaster, out, out); 

    context.dispose(); 

    return new BufferedImage(colorModel, out, true, null);
  }

  /**
   * Create a new Raster by multiplying every pixel in the source Raster by a constant color.
   */ 
  private WritableRaster
  rescaleRaster
  (
   Raster in, 
   Color3d c
  ) 
  {
    WritableRaster out = in.createCompatibleWritableRaster();

    float offsets[] = { 0.0f, 0.0f, 0.0f, 0.0f };
    float factors[] = { (float) c.r(), (float) c.g(), (float) c.b(), 1.0f };
      
    RescaleOp ringOp = new RescaleOp(factors, offsets, null); 
    ringOp.filter(in, out);

    return out; 
  }


  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Get the OpenGL texture object handle for the given combination of node states. <P> 
   * 
   * @param gl
   *   The OpenGL interface.
   * 
   * @param name
   *   The name of the texture.
   * 
   * @throws PipelineException
   *   If unable to retrieve the texture.
   */ 
  public synchronized Integer
  getTexture
  (
   GL gl, 
   String name   
  ) 
    throws PipelineException 
  {
    Integer dl = pIconTextures.get(name);
    if(dl == null)
      throw new PipelineException("Unable to find an OpenGL texture for (" + name + ")!");
    
    return dl; 
  }

  /**
   * Get the 64x32 icon for the given combination of node states. <P> 
   * 
   * @param name
   *   The name of the texture.
   * 
   * @throws PipelineException
   *   If unable to retrieve the icon.
   */ 
  public ImageIcon
  getIcon64
  (
   String name 
  ) 
    throws PipelineException 
  {
    ImageIcon icons[] = pIcons.get(name);
    if((icons == null) || (icons[2] == null)) 
      throw new PipelineException("Unable to find a 64x64 icon for (" + name + ")!");

    return icons[2];
  }

  /**
   * Get the 32x32 icon for the given combination of node states. <P> 
   * 
   * @param name
   *   The name of the texture.
   * 
   * @throws PipelineException
   *   If unable to retrieve the icon.
   */ 
  public ImageIcon
  getIcon32
  (
   String name 
  ) 
    throws PipelineException 
  {
    ImageIcon icons[] = pIcons.get(name);
    if((icons == null) || (icons[0] == null)) 
      throw new PipelineException("Unable to find a 32x32 icon for (" + name + ")!");

    return icons[0];
  }
    
  /**
   * Get the 21x21 icon for the given combination of node states. <P> 
   * 
   * @param name
   *   The name of the texture.
   * 
   * @throws PipelineException
   *   If unable to retrieve the icon.
   */ 
  public ImageIcon
  getIcon21
  (
   String name
  ) 
    throws PipelineException 
  {
    ImageIcon icons[] = pIcons.get(name);
    if((icons == null) || (icons[1] == null)) 
      throw new PipelineException("Unable to find a 21x21 icon for (" + name + ")!");

    return icons[1];
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * 
   */ 
  private void
  printBufferedImageInfo
  (
   BufferedImage bi
  ) 
  {
    StringBuilder buf = new StringBuilder();
      
    buf.append("Texture BufferedImage Info:\n" + 
               "  Image Type = ");
    switch(bi.getType()) {
    case BufferedImage.TYPE_INT_RGB:
      buf.append("INT_RGB");
      break;
      
    case BufferedImage.TYPE_INT_ARGB:
      buf.append("INT_ARGB");
      break;
      
    case BufferedImage.TYPE_INT_ARGB_PRE:
      buf.append("INT_ARGB_PRE");
      break;
      
    case BufferedImage.TYPE_INT_BGR:
      buf.append("INT_BGR");
      break;
      
    case BufferedImage.TYPE_3BYTE_BGR:
      buf.append("3BYTE_BGR");
      break;
      
    case BufferedImage.TYPE_4BYTE_ABGR:
      buf.append("4BYTE_ABGR");
      break;
        
    case BufferedImage.TYPE_4BYTE_ABGR_PRE:
      buf.append("4BYTE_ABGR_PRE");
      break;
        
    case BufferedImage.TYPE_BYTE_GRAY:
      buf.append("BYTE_GRAY");
      break;
        
    case BufferedImage.TYPE_BYTE_BINARY:
      buf.append("BYTE_BINARY");
      break;
        
    case BufferedImage.TYPE_BYTE_INDEXED:
      buf.append("BYTE_INDEXED");
      break;
        
    case BufferedImage.TYPE_USHORT_GRAY:
      buf.append("USHORT_GRAY");
      break;
        
    case BufferedImage.TYPE_USHORT_565_RGB:
      buf.append("USHORT_565_RGB");
      break;
        
    case BufferedImage.TYPE_USHORT_555_RGB:
      buf.append("USHORT_555_RGB");
      break;
        
    case BufferedImage.TYPE_CUSTOM:
      buf.append("CUSTOM");
      break;
        
    default:
      buf.append("(unknown)");
    }

    buf.append("\n" + 
               "  " + bi.getColorModel() + "\n");

    buf.append("  Properties:\n");
    {
      String[] pnames = bi.getPropertyNames();
      if(pnames != null) {
        int wk;
        for(wk=0; wk<pnames.length; wk++) 
          buf.append("    " + pnames[wk] + " = " + bi.getProperty(pnames[wk]) + "\n");
      }
      else {
        buf.append("  (none)\n");
      }
    }

    LogMgr.getInstance().log
      (LogMgr.Kind.Tex, LogMgr.Level.Fine,
       buf.toString()); 

    LogMgr.getInstance().flush();
  }
  
  /**
   * 
   */ 
  private void 
  printRasterInfo
  (
   Raster raster
  ) 
  {
    SampleModel model = raster.getSampleModel(); 

    StringBuilder buf = new StringBuilder();
    
    buf.append("Texture Raster Info:\n" + 
               "  Image Type = ");
    switch(model.getDataType()) {
    case DataBuffer.TYPE_BYTE:
      buf.append("TYPE_BYTE");
      break;
        
    case DataBuffer.TYPE_DOUBLE:
      buf.append("TYPE_DOUBLE");
      break;        

    case DataBuffer.TYPE_FLOAT:
      buf.append("TYPE_FLOAT");
      break;        

    case DataBuffer.TYPE_INT:
      buf.append("TYPE_INT");
      break;        

    case DataBuffer.TYPE_SHORT:
      buf.append("TYPE_SHORT");
      break;        

    case DataBuffer.TYPE_UNDEFINED:
      buf.append("TYPE_UNDEFINED");
      break;    
    
    case DataBuffer.TYPE_USHORT:
      buf.append("TYPE_USHORT");
      break;
    
    default:
      buf.append("(unknown)");
    }

    buf.append("\n" + 
               "  TransferType = ");      
    switch(model.getTransferType()) {
    case DataBuffer.TYPE_BYTE:
      buf.append("TYPE_BYTE");
      break;
        
    case DataBuffer.TYPE_DOUBLE:
      buf.append("TYPE_DOUBLE");
      break;        

    case DataBuffer.TYPE_FLOAT:
      buf.append("TYPE_FLOAT");
      break;        

    case DataBuffer.TYPE_INT:
      buf.append("TYPE_INT");
      break;        

    case DataBuffer.TYPE_SHORT:
      buf.append("TYPE_SHORT");
      break;        

    case DataBuffer.TYPE_UNDEFINED:
      buf.append("TYPE_UNDEFINED");
      break;    
    
    case DataBuffer.TYPE_USHORT:
      buf.append("TYPE_USHORT");
      break;
    
    default:
      buf.append("(unknown)");
    }

    buf.append("\n" + 
               "  Width = " + model.getWidth() + "\n" +
               "  Height = " + model.getHeight() + "\n" +
               "  Bands = " + model.getNumBands());
                 
    LogMgr.getInstance().log
      (LogMgr.Kind.Tex, LogMgr.Level.Fine,
       buf.toString());
    LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The sole instance of this class.
   */ 
  private static TextureMgr sTextureMgr = new TextureMgr();


  /**
   * The resolution of the largest Mip-Map level texture image.
   */ 
  private static final int  sMaxTexRes = 64;

  /**
   * The resolution of the largest Mip-Map level font texture image.
   */ 
  private static final int  sMaxFontRes = 32;


  /**
   * The resolutions of the icon images: 32x32, 21x21, 64x32
   */ 
  private static final int  sIconRes[] = { 32, 21, 64 };

  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The OpenGL texture object handles for the per-character font textures indexed by 
   * font name and ASCII character code.  Per-character entries which are <CODE>null</CODE>
   * contain unprintable characters without textures.
   */ 
  private HashMap<String,Integer[]>  pFontTextures;

  /**
   * The font geometries indexed by font name.
   */ 
  private HashMap<String,FontGeometry> pFontGeometry;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The OpenGL texture object handles indexed by texture name.
   */ 
  private HashMap<String,Integer>  pIconTextures;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The Swing format icons at all (sIconRes) resolutions indexed by icon name.<P> 
   * 
   * These icons are constructed by compositing a set of source rasters stored in 
   * pIconImages with user defined colors for each layer.  The resulting icons are indexed
   * by a name which includes descriptive components for each of these layers.  For example, 
   * a icon for a node might be called "Pending-Finished-Selected" to describe the combination
   * of OverallNodeState, OverallQueueState and SelectionMode for the icon. <P> 
   */ 
  private HashMap<String,ImageIcon[]>  pIcons;
  
  /**
   * The raw image data used to construct the pIcons indexed by texture name.
   */ 
  private HashMap<String,BufferedImage[]>  pIconImages;

  /**
   * The colors used to rebuild the current icons.
   */ 
  private Color3d  pNormalRingColor;      
  private Color3d  pSelectedRingColor;    
  private Color3d  pPrimaryRingColor;     
  private Color3d  pFinishedCoreColor;    
  private Color3d  pStaleCoreColor;       
  private Color3d  pQueuedCoreColor;      
  private Color3d  pPausedCoreColor;      
  private Color3d  pRunningCoreColor;     
  private Color3d  pAbortedCoreColor;     
  private Color3d  pFailedCoreColor;      
  private Color3d  pPreemptedCoreColor;   
  private Color3d  pLightweightCoreColor; 
  private Color3d  pUndefinedCoreColor;   
  private Color3d  pStaleLinkColor;       
  private Color3d  pModifiableColor;  
  private Color3d  pFrozenColor;      
  private Color3d  pFrozenFinishedColor; 
  private Color3d  pFrozenStaleColor;     
  
}

