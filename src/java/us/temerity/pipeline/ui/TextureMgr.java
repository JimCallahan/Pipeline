// $Id: TextureMgr.java,v 1.3 2004/05/07 15:06:51 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;

import javax.imageio.*;
import javax.swing.*;
import javax.media.j3d.*;

/*------------------------------------------------------------------------------------------*/
/*   T E X T U R E   M G R                                                                  */
/*------------------------------------------------------------------------------------------*/

/** 
 * Manages a cached set of textures used by {@link JNodeViewerPanel JNodeViewerPanel} to
 * render Pipeline nodes. <P> 
 * 
 * Node textures are cached as {@link Texture2D Texture2D} and {@link ImageIcon ImageIcon} 
 * instances loaded from image files which are used to graphicly represent permutations of 
 * {@link OverallNodeState OverallNodeState} and {@link OverallQueueState OverallQueueState}
 * of Pipeline nodes. <P> 
 * 
 * Font textures are also cached as <CODE>Texture2D</CODE> which are used to render node 
 * and link labels in <CODE>JNodeViewerPanel</CODE> instances. <P> 
 * 
 * Finally, a small cacbe of simple 2x2 unfiltered monocolor textures are also managed by
 * this class for use in rendering lines.
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
    pSimpleTextures = new HashMap<String,Texture2D>();

    pFontTextures   = new HashMap<String,Texture2D[]>();
    pFontGeometry   = new HashMap<String,FontGeometry>();

    pIconTextures   = new HashMap<String,Texture2D>();
    pIconImages     = new HashMap<String,ImageIcon>();
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

  /** 
   * Verify that the simple unfiltered 2x2 monocolor texture with the given name is 
   * currently loaded.
   * 
   * If the simple color texture is not currently loaded then read the texture image 
   * generate the texture.
   * 
   * @param name
   *   The symbolic color name of the texture.
   * 
   * @throws IOException
   *   If unable to load the source images.
   */ 
  public synchronized void
  verifySimpleTexture
  (
   String name
  ) 
    throws IOException
  {
    if(name == null)
      throw new IllegalArgumentException("The color name cannot be (null)!");

    if(pSimpleTextures.containsKey(name))
      return;

    /* otherwise, load it */ 
    Logs.tex.fine("Loading Simple Texture: " + name);
    Logs.flush();
    {
      String path = (name + ".png");
      URL url = LookAndFeelLoader.class.getResource(path);
      if(url == null) 
	throw new IOException("Unable to find: " + path);
      BufferedImage bi = ImageIO.read(url);

      Texture2D tex = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA, 2, 2);
      tex.setImage(0, new ImageComponent2D(ImageComponent2D.FORMAT_RGBA, bi));
      
      tex.setMinFilter(Texture.FASTEST);
      tex.setMagFilter(Texture.FASTEST);
      tex.setBoundaryModeS(Texture.CLAMP_TO_EDGE);
      tex.setBoundaryModeT(Texture.CLAMP_TO_EDGE);

      pSimpleTextures.put(name, tex);
    }
  }

  /**
   * Get the simple unfiltered 2x2 monocolor texture with the given name. 
   * 
   * @param name
   *   The symbolic color name of the texture.
   * 
   * @throws IOException
   *   If unable to retrieve the texture.
   */ 
  public synchronized Texture2D
  getSimpleTexture
  (
   String name  
  ) 
    throws IOException 
  { 
    verifySimpleTexture(name);
    return pSimpleTextures.get(name);
  }



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
  
  /** 
   * Verify that all of the per-character textures for the given font are currently loaded.
   * 
   * If the textures are not currently loaded then read the Mip-Map level images from 
   * disk and used them to generate the textures.
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
      Texture2D texs[] = new Texture2D[128];

      char code;
      for(code=0; code<texs.length; code++) {
	int icode = (int) code;

	if(geom.isPrintable(code)) {
	  Logs.tex.fine("Loading Font Texture: " + name + " \"" + code + "\"");

	  texs[code] = new Texture2D(Texture.MULTI_LEVEL_MIPMAP, Texture.RGBA, 
				     sMaxFontRes, sMaxFontRes);

	  int level, size; 
	  for(level=0, size=sMaxFontRes; size>=1; level++, size/=2) {
	    Logs.tex.finer("Loading MipMap: " + size + "x" + size);
	    Logs.flush();
	    
	    String path = ("fonts/" + name + "/" + icode + "/texture." + size + ".png");
	    URL url = LookAndFeelLoader.class.getResource(path);
	    if(url == null) 
	      throw new IOException("Unable to find: " + path);
	    BufferedImage bi = ImageIO.read(url);
	    
	    ImageComponent2D img = new ImageComponent2D(ImageComponent2D.FORMAT_RGBA, bi);
	    texs[code].setImage(level, img);
	  }
	  
	  texs[code].setMinFilter(Texture.MULTI_LEVEL_LINEAR);
	  texs[code].setMagFilter(Texture.BASE_LEVEL_LINEAR);
	}
      }

      pFontTextures.put(name, texs);
    }
  }

  /**
   * Get the texture for the given character of the given font.
   * 
   * @param name
   *   The symbolic name of the font.
   * 
   * @param code
   *   The character code.
   * 
   * @return 
   *   The texture or <CODE>null</CODE> if the given character is unprintable.
   * 
   * @throws IOException
   *   If unable to retrieve the font textures.
   */ 
  public synchronized Texture2D
  getFontTexture
  (
   String name, 
   char code
  ) 
    throws IOException 
  { 
    if((code < 0) || (code > 127))
      throw new IllegalArgumentException
	("The character code (" + ((int) code) + ") for character " + 
	 "\"" + code + "\" must be in the [0-127] range!");

    verifyFontTextures(name);
    return pFontTextures.get(name)[code];
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
 
  /** 
   * Verify that the texture/icon with the given name is currently loaded.
   * 
   * If the texture/icon is not currently loaded then read the Mip-Map level images from 
   * disk and used them to generate the texture and icon.
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
   String name
  ) 
    throws IOException
  {
    if(name == null)
      throw new IllegalArgumentException("The font name cannot be (null)!");

    /* make sure they haven't already been loaded */ 
    if(pIconTextures.containsKey(name)) {
      assert(pIconImages.containsKey(name));
      return;
    }

    /* build the texture and icon */ 
    Logs.tex.fine("Loading Texture: " + name);
    {
      Texture2D tex = 
	new Texture2D(Texture.MULTI_LEVEL_MIPMAP, Texture.RGBA, sMaxTexRes, sMaxTexRes);

      int level, size; 
      for(level=0, size=sMaxTexRes; size>=1; level++, size/=2) {
	Logs.tex.finer("Loading MipMap: " + size + "x" + size);
	Logs.flush();

	String path = ("textures/" + name + "/texture." + size + ".png");
	URL url = LookAndFeelLoader.class.getResource(path);
	if(url == null) 
	  throw new IOException("Unable to find: " + path);
	BufferedImage bi = ImageIO.read(url);
	   
	ImageComponent2D img = new ImageComponent2D(ImageComponent2D.FORMAT_RGBA, bi);
	tex.setImage(level, img);
	
	if(size == sIconRes) 
	  pIconImages.put(name, new ImageIcon(bi));
      }
 	 
      tex.setMinFilter(Texture.MULTI_LEVEL_LINEAR);
      tex.setMagFilter(Texture.BASE_LEVEL_LINEAR);

      pIconTextures.put(name, tex);
    }
  }

  /**
   * Get the texture for the given combination of node states. <P> 
   * 
   * @param name
   *   The name of the texture.
   * 
   * @throws IOException
   *   If unable to retrieve the texture.
   */ 
  public synchronized Texture2D
  getTexture
  (
   String name   
  ) 
    throws IOException 
  {
    verifyTexture(name);
    return pIconTextures.get(name);
  }

  /**
   * Get the icon for the given combination of node states. <P> 
   * 
   * @param name
   *   The name of the texture.
   * 
   * @throws IOException
   *   If unable to retrieve the icon.
   */ 
  public ImageIcon
  getIcon
  (
   String name 
  ) 
    throws IOException 
  {
    verifyTexture(name);
    return pIconImages.get(name);
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
   * The resolution of the icon images.
   */ 
  private static final int  sIconRes = 32;

  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Simple 2x2 unfiltered monocolor textures indexed by texture name.
   */ 
  private HashMap<String,Texture2D>  pSimpleTextures;


  /**
   * The Mip-Mapped per-character font texturesindexed by font name.
   */ 
  private HashMap<String,Texture2D[]>  pFontTextures;

  /**
   * The font geometries indexed by font name.
   */ 
  private HashMap<String,FontGeometry> pFontGeometry;


  /**
   * The Mip-Mapped node state textures indexed by texture name.
   */ 
  private HashMap<String,Texture2D>  pIconTextures;

  /**
   * The node state icons indexed by texture name.
   */ 
  private HashMap<String,ImageIcon>  pIconImages;
  
}
