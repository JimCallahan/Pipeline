// $Id: TextureMgr.java,v 1.1 2004/05/05 20:57:24 jim Exp $

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
 * Manages a cached set of {@link Texture2D Texture2D} and {@link ImageIcon ImageIcon} 
 * instances loaded from image files which are used to graphicly represent permutations of 
 * {@link OverallNodeState OverallNodeState} and {@link OverallQueueState OverallQueueState}
 * of Pipeline nodes. <P> 
 * 
 * Also maintains a small cacbe of simple 2x2 unfiltered monocolor textures which are 
 * looked up by symbolic color names.
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
    pTextures       = new HashMap<String,Texture2D>();
    pIcons          = new HashMap<String,ImageIcon>();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the UIMaster instance.
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
  verifySimple
  (
   String name
  ) 
    throws IOException
  {
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
    verifySimple(name);
    return pSimpleTextures.get(name);
  }


  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Verify that the texture/icon withe the given name is currently loaded.
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
  verify
  (
   String name
  ) 
    throws IOException
  {
    /* make sure they haven't already been loaded */ 
    if(pTextures.containsKey(name)) {
      assert(pIcons.containsKey(name));
      return;
    }

    /* build the texture and icon */ 
    Logs.tex.fine("Loading Texture: " + name);
    {
      Texture2D tex = 
	new Texture2D(Texture.MULTI_LEVEL_MIPMAP, Texture.RGBA, sMaxRes, sMaxRes);

      int level, size; 
      for(level=0, size=sMaxRes; size>=1; level++, size/=2) {
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
	  pIcons.put(name, new ImageIcon(bi));
      }
 	 
      tex.setMinFilter(Texture.MULTI_LEVEL_LINEAR);
      tex.setMagFilter(Texture.BASE_LEVEL_LINEAR);

      pTextures.put(name, tex);
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
    verify(name);
    return pTextures.get(name);
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
    verify(name);
    return pIcons.get(name);
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The sole instance of this class.
   */ 
  private static TextureMgr sTextureMgr = new TextureMgr();


  /**
   * The resolution of the largest Mip-Map level image.
   */ 
  private static final int  sMaxRes = 64;

  /**
   * The resolution of the icon images.
   */ 
  private static final int  sIconRes = 32;

  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Simple 2x2 unfiltered monocolor textures indexed by name.
   */ 
  private HashMap<String,Texture2D>  pSimpleTextures;

  /**
   * The Mip-Mapped node state textures.
   */ 
  private HashMap<String,Texture2D>  pTextures;

  /**
   * The node state icons.
   */ 
  private HashMap<String,ImageIcon>  pIcons;

  
  
}
