// $Id: TextureMgr.java,v 1.1 2005/01/03 06:56:25 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.nio.*;
import java.net.*;

import javax.imageio.*;
import javax.swing.*;

import net.java.games.jogl.*;

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
    pFontTextures   = new HashMap<String,Integer[]>();
    pFontGeometry   = new HashMap<String,FontGeometry>();

    pIconTextures   = new HashMap<String,Integer>();
    pIconImages     = new HashMap<String,ImageIcon[]>();
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
	int icode = (int) code;

	if(geom.isPrintable(code)) {
	  Logs.tex.fine("Loading Font Texture: " + name + " \"" + code + "\"");

	  int handle[] = new int[1];
	  gl.glGenTextures(1, handle); 
	  texs[code] = handle[0];

	  gl.glBindTexture(GL.GL_TEXTURE_2D, handle[0]);
	  
	  int level, size; 
	  for(level=0, size=sMaxFontRes; size>=1; level++, size/=2) {
	    Logs.tex.finer("Loading MipMap: " + size + "x" + size);
	    Logs.flush();
	    
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
	}
      }

      pFontTextures.put(name, texs);
    }
  }

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
   * @throws IOException
   *   If unable to retrieve the font textures.
   */ 
  public synchronized Integer
  getFontTexture
  (
   GL gl, 
   String name, 
   char code
  ) 
    throws IOException 
  { 
    if((code < 0) || (code > 127))
      throw new IllegalArgumentException
	("The character code (" + ((int) code) + ") for character " + 
	 "\"" + code + "\" must be in the [0-127] range!");

    verifyFontTextures(gl, name);
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
    if(name == null)
      throw new IllegalArgumentException("The texture name cannot be (null)!");

    /* make sure they haven't already been loaded */ 
    if(pIconTextures.containsKey(name)) {
      assert(pIconImages.containsKey(name));
      return;
    }

    /* build the texture and icon */ 
    Logs.tex.fine("Loading Texture: " + name);
    {
      int handle[] = new int[1];
      gl.glGenTextures(1, handle); 

      gl.glBindTexture(GL.GL_TEXTURE_2D, handle[0]);

      int level, size; 
      for(level=0, size=sMaxTexRes; size>=1; level++, size/=2) {
	Logs.tex.finer("Loading MipMap: " + size + "x" + size);
	Logs.flush();

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
	
   	if(size == sIconRes[0]) {
	  ImageIcon icons[] = pIconImages.get(name);
	  if(icons == null) {
	    icons = new ImageIcon[sIconRes.length];
	    pIconImages.put(name, icons);
	  }

	  icons[0] = new ImageIcon(bi);
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
  verifyIcon
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
    ImageIcon icons[] = pIconImages.get(name);
    if((icons != null) && (icons[idx] != null))
      return;

    int size = sIconRes[idx];
	  
    Logs.tex.fine("Loading Icon: " + name + " " + size + "x" + size);
    Logs.flush();
    
    String path = ("textures/" + name + "/texture." + size + ".png");
    URL url = LookAndFeelLoader.class.getResource(path);
    if(url == null) 
      throw new IOException("Unable to find: " + path);
    BufferedImage bi = ImageIO.read(url);
      
    if(icons == null) {
      icons = new ImageIcon[sIconRes.length];
      pIconImages.put(name, icons);
    }

    icons[idx] = new ImageIcon(bi);
  } 	

  /**
   * Get the OpenGL texture object handle for the given combination of node states. <P> 
   * 
   * @param gl
   *   The OpenGL interface.
   * 
   * @param name
   *   The name of the texture.
   * 
   * @throws IOException
   *   If unable to retrieve the texture.
   */ 
  public synchronized Integer
  getTexture
  (
   GL gl, 
   String name   
  ) 
    throws IOException 
  {
    verifyTexture(gl, name);
    return pIconTextures.get(name);
  }

  /**
   * Get the 32x32 icon for the given combination of node states. <P> 
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
    verifyIcon(name);

    ImageIcon icons[] = pIconImages.get(name);
    if((icons == null) || (icons[0] == null)) 
      throw new IOException("Unable to find a 32x32 icon for (" + name + ")!");

    return icons[0];
  }
  
  /**
   * Get the 21x21 icon for the given combination of node states. <P> 
   * 
   * @param name
   *   The name of the texture.
   * 
   * @throws IOException
   *   If unable to retrieve the icon.
   */ 
  public ImageIcon
  getIcon21
  (
   String name
  ) 
    throws IOException 
  {
    verifyIcon21(name);

    ImageIcon icons[] = pIconImages.get(name);
    if((icons == null) || (icons[1] == null)) 
      throw new IOException("Unable to find a 21x21 icon for (" + name + ")!");

    return icons[1];
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
   * The resolutions of the icon images.
   */ 
  private static final int  sIconRes[] = { 32, 21 };

  


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


  /**
   * The OpenGL texture object handles indexed by texture name.
   */ 
  private HashMap<String,Integer>  pIconTextures;

  /**
   * The node state icons at sIconRes resolutions indexed by texture name.
   */ 
  private HashMap<String,ImageIcon[]>  pIconImages;
  
}
