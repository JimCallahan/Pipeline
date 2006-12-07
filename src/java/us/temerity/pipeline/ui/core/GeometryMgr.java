// $Id: GeometryMgr.java,v 1.4 2006/12/07 05:18:25 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;

import java.awt.*;
import java.io.*;
import java.util.*;

import net.java.games.jogl.*;

/*------------------------------------------------------------------------------------------*/
/*   G E O M E T R Y   M G R                                                                */
/*------------------------------------------------------------------------------------------*/

/** 
 * Manages a set of resuable OpenGL display lists for common geometry components.
 */ 
public
class GeometryMgr
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct the sole instance.
   */ 
  private 
  GeometryMgr()
  {
    pTextDLs     = new HashMap<String,Integer>();
    pLinkRelDLs  = new EnumMap<LinkRelationship,Integer>(LinkRelationship.class);
    pNodeIconDLs = new HashMap<String,Integer>();
    pJobIconDLs  = new HashMap<String,TreeMap<Integer,Integer>>();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the AppearanceMgr instance.
   */ 
  public static GeometryMgr
  getInstance() 
  {
    return sGeometryMgr;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Compute the width of the label geometry which would be generated if the given 
   * parameters were passed to {@link #getTextDL getTextDL}.
   * 
   * @param name
   *   The name of the font.
   * 
   * @param text
   *   The text to render. 
   * 
   * @param space
   *   The amount of space between characters.
   * 
   * @throws IOException
   *   If unable to lookup the font.
   */ 
  public double 
  getTextWidth
  (
   String name, 
   String text,
   double space
  ) 
    throws IOException   
  {
    double width = 0.0;
    {
      FontGeometry geom = TextureMgr.getInstance().getFontGeometry(name);
      if(geom == null) 
	throw new IOException 
	  ("No font named (" + name + ") has been registered with the TextureMgr!");
      
      int wk;
      for(wk=0; wk<text.length(); wk++) {
	char code = text.charAt(wk);
	if((code >= 0) && (code <128)) {
	  if(geom.isPrintable(code)) 
	    width += geom.getOrigin(code).x() + geom.getExtent(code).x() + space;
	  else 
	    width += space * 4.0;
	}
      }
    }

    return width;
  }

  /**
   * Get an OpenGL display list which renders a text string as textured quads. <P> 
   * 
   * Characters in the <CODE>text</CODE> argument which have ASCII character codes outside 
   * the [0, 127] range will be silently ignored.
   * 
   * The display list renders the per-character quads on the XY plane roughly (1.0) unit
   * high.  The text string is placed so that the character anchor is aligned vertically
   * with the origin.  The horizontal alignment of the string is determined by the 
   * <CODE>align</CODE> argument. 
   * 
   * @param gl
   *   The OpenGL interface.
   * 
   * @param name
   *   The name of the font.
   * 
   * @param text
   *   The text to render. 
   * 
   * @param align
   *   The horizontal text alignment.
   * 
   * @param space
   *   The amount of space between characters.
   * 
   * @return 
   *   The display list handle.
   * 
   * @throws IOException
   *   If unable to lookup or generate the display list.
   */ 
  public synchronized int
  getTextDL
  (
   GL gl,
   String name, 
   String text, 
   TextAlignment align, 
   double space
  ) 
    throws IOException    
  {
    String tag = (name + "|" + text + "|" + align + "|" + space);
    Integer dl = pTextDLs.get(tag);
    if(dl == null) {
      if(name == null)
	throw new IllegalArgumentException("The font name cannot be (null)!");
      
      TextureMgr texMgr = TextureMgr.getInstance();
      
      /* lookup the character textures and compute the total string width */ 
      ArrayList<Integer> texIDs = new ArrayList<Integer>();
      ArrayList<Double> offsets = new ArrayList<Double>();
      double width  = 0.0;
      {
	FontGeometry geom = texMgr.getFontGeometry(name);
	if(geom == null) 
	  throw new IOException 
	    ("No font named (" + name + ") has been registered with the TextureMgr!");
	
	int wk;
	for(wk=0; wk<text.length(); wk++) {
	  char code = text.charAt(wk);
	  if((code >= 0) && (code <128)) {
	    double w = 0.0;
	    if(geom.isPrintable(code)) {
	      w = geom.getOrigin(code).x() + geom.getExtent(code).x() + space;
	      texIDs.add(texMgr.getFontTexture(gl, name, code)); 
	    }
	    else {
	      w = space * 4.0;
	      texIDs.add(null);
	    }
	    
	    offsets.add(w);
	    width += w;
	  }
	}
      }

      /* generate display list */ 
      {
	double x = 0.0;
	switch(align) {
	case Center:
	  x = -width * 0.5;
	  break;
	  
	case Right:
	  x = -width;
	}	
	
	dl = gl.glGenLists(1);
	pTextDLs.put(tag, dl);

	gl.glNewList(dl, GL.GL_COMPILE);
	{
	  gl.glEnable(GL.GL_TEXTURE_2D);
	  
	  int wk;
	  for(wk=0; wk<texIDs.size(); wk++) {
	    Integer texID = texIDs.get(wk);
	    double dx = offsets.get(wk);
	    
	    if(texID != null) {
	      gl.glBindTexture(GL.GL_TEXTURE_2D, texID);
	      
	      gl.glBegin(GL.GL_QUADS);
	      {
		gl.glTexCoord2d(0.0,  1.0);
		gl.glVertex2d(x-0.1, -0.3);
		
		gl.glTexCoord2d(1.0,  1.0);
		gl.glVertex2d(x+1.0, -0.3);
		
		gl.glTexCoord2d(1.0, 0.0);	
		gl.glVertex2d(x+1.0, 0.8);
		
		gl.glTexCoord2d(0.0, 0.0);
		gl.glVertex2d(x-0.1, 0.8);
	      }
	      gl.glEnd();
	    }
	    
	    x += dx; 
	  }
	  
	  gl.glDisable(GL.GL_TEXTURE_2D); 
	}
	gl.glEndList();
      }
    }
      
    return dl;
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get an OpenGL display list which renders a link relationship icon. <P> 
   * 
   * The display list renders a single square quad on the XY plane which is (0.5) units wide
   * and centered around the origin.
   * 
   * @param gl
   *   The OpenGL interface.
   * 
   * @param rel
   *   The nature of the relationship between source and target node files.
   * 
   * @return 
   *   The display list handle.
   * 
   * @throws IOException
   *   If unable to lookup or generate the display list.
   */ 
  public synchronized int
  getLinkRelationshipDL
  (
   GL gl,
   LinkRelationship rel
  ) 
    throws IOException
  { 
    Integer dl = pLinkRelDLs.get(rel);
    if(dl == null) {
      int texID = TextureMgr.getInstance().getTexture(gl,"LinkRelationship-" + rel); 

      dl = gl.glGenLists(1);
      pLinkRelDLs.put(rel, dl);

      gl.glNewList(dl, GL.GL_COMPILE);
      {
	gl.glEnable(GL.GL_TEXTURE_2D);
	gl.glBindTexture(GL.GL_TEXTURE_2D, texID);
	
	gl.glColor3d(1.0, 1.0, 1.0);

	gl.glBegin(GL.GL_QUADS);
	{
	  gl.glTexCoord2d(0.0, 1.0);
	  gl.glVertex2d(-0.25, -0.25);
	  
	  gl.glTexCoord2d(1.0, 1.0);
	  gl.glVertex2d(0.25, -0.25);
	  
	  gl.glTexCoord2d(1.0, 0.0);	
	  gl.glVertex2d(0.25, 0.25);
	  
	  gl.glTexCoord2d(0.0, 0.0);
	  gl.glVertex2d(-0.25, 0.25);
	}
	gl.glEnd();

	gl.glDisable(GL.GL_TEXTURE_2D); 
      }
      gl.glEndList();
    }

    return dl;
  }

  /** 
   * Get an OpenGL display list which renders an icon without setting the color. <P> 
   * 
   * The display list renders a single square quad on the XY plane which is (1.0) units wide
   * and centered around the origin. 
   * 
   * @param gl
   *   The OpenGL interface.
   * 
   * @param name
   *   The icon name. 
   * 
   * @return 
   *   The display list handle.
   * 
   * @throws IOException
   *   If unable to lookup or generate the display list.
   */ 
  public synchronized int
  getIconDL
  (
   GL gl,
   String name
  ) 
    throws IOException
  { 
    Integer dl = pNodeIconDLs.get(name);
    if(dl == null) {
      int texID = TextureMgr.getInstance().getTexture(gl, name);

      dl = gl.glGenLists(1);
      pNodeIconDLs.put(name, dl);

      gl.glNewList(dl, GL.GL_COMPILE);
      {
	gl.glEnable(GL.GL_TEXTURE_2D);
	gl.glBindTexture(GL.GL_TEXTURE_2D, texID);
	
	gl.glBegin(GL.GL_QUADS);
	{
	  gl.glTexCoord2d(0.0, 1.0);
	  gl.glVertex2d(-0.5, -0.5);
	  
	  gl.glTexCoord2d(1.0, 1.0);
	  gl.glVertex2d(0.5, -0.5);
	  
	  gl.glTexCoord2d(1.0, 0.0);	
	  gl.glVertex2d(0.5, 0.5);
	  
	  gl.glTexCoord2d(0.0, 0.0);
	  gl.glVertex2d(-0.5, 0.5);
	}
	gl.glEnd();

	gl.glDisable(GL.GL_TEXTURE_2D); 
      }
      gl.glEndList();
    }

    return dl;
  }

  /** 
   * Get an OpenGL display list which renders a node icon. <P> 
   * 
   * The display list renders a single square quad on the XY plane which is (1.0) units wide
   * and centered around the origin. 
   * 
   * @param gl
   *   The OpenGL interface.
   * 
   * @param name
   *   The combined node state name.
   * 
   * @return 
   *   The display list handle.
   * 
   * @throws IOException
   *   If unable to lookup or generate the display list.
   */ 
  public synchronized int
  getNodeIconDL
  (
   GL gl,
   String name
  ) 
    throws IOException
  { 
    Integer dl = pNodeIconDLs.get(name);
    if(dl == null) {
      int texID = TextureMgr.getInstance().getTexture(gl, name);

      dl = gl.glGenLists(1);
      pNodeIconDLs.put(name, dl);

      gl.glNewList(dl, GL.GL_COMPILE);
      {
	gl.glEnable(GL.GL_TEXTURE_2D);
	gl.glBindTexture(GL.GL_TEXTURE_2D, texID);
	
	gl.glColor3d(1.0, 1.0, 1.0);

	gl.glBegin(GL.GL_QUADS);
	{
	  gl.glTexCoord2d(0.0, 1.0);
	  gl.glVertex2d(-0.5, -0.5);
	  
	  gl.glTexCoord2d(1.0, 1.0);
	  gl.glVertex2d(0.5, -0.5);
	  
	  gl.glTexCoord2d(1.0, 0.0);	
	  gl.glVertex2d(0.5, 0.5);
	  
	  gl.glTexCoord2d(0.0, 0.0);
	  gl.glVertex2d(-0.5, 0.5);
	}
	gl.glEnd();

	gl.glDisable(GL.GL_TEXTURE_2D); 
      }
      gl.glEndList();
    }

    return dl;
  }

  /** 
   * Get an OpenGL display list which renders a job icon. <P> 
   * 
   * The display list renders one or more quads on the XY plane which are (1.0) units wide
   * and (0.375 * height) units high and centered around the origin. 
   * 
   * @param gl
   *   The OpenGL interface.
   * 
   * @param name
   *   The combined job state name.
   * 
   * @param external
   *   Whether the job is external to the job group.
   * 
   * @param height
   *   The vertical job span of the icon.
   * 
   * @return 
   *   The display list handle.
   * 
   * @throws IOException
   *   If unable to lookup or generate the display list.
   */ 
  public synchronized int
  getJobIconDL
  (
   GL gl,
   String name, 
   boolean external,
   int height
  ) 
    throws IOException
  { 
    if(height < 1) 
      throw new IllegalArgumentException
	("The height (" + height + ") must be greater-than zero!");

    if(external && (height > 1)) 
      throw new IllegalArgumentException
	("External jobs must have a height of one!");

    String sname = ((external ? "ExternalJob" : "Job") + "-" + name);
    TreeMap<Integer,Integer> dls = pJobIconDLs.get(sname);
    if(dls == null) {
      dls = new TreeMap<Integer,Integer>();
      pJobIconDLs.put(sname, dls);
    }

    Integer dl = dls.get(height);
    if(dl == null) {
      int texID = TextureMgr.getInstance().getTexture(gl, sname);

      dl = gl.glGenLists(1);
      dls.put(height, dl);

      gl.glNewList(dl, GL.GL_COMPILE);
      {
	gl.glEnable(GL.GL_TEXTURE_2D);
	gl.glBindTexture(GL.GL_TEXTURE_2D, texID);
	
	gl.glColor3d(1.0, 1.0, 1.0);

	if(height == 1) {
	  gl.glBegin(GL.GL_QUADS);
	  {
	    gl.glTexCoord2d(0.0, 0.6875);
	    gl.glVertex2d(-0.5, -0.1875);
	    
	    gl.glTexCoord2d(1.0, 0.6875);
	    gl.glVertex2d(0.5, -0.1875);
	    
	    gl.glTexCoord2d(1.0, 0.3125);	
	    gl.glVertex2d(0.5, 0.1875);
	    
	    gl.glTexCoord2d(0.0, 0.3125);
	    gl.glVertex2d(-0.5, 0.1875);
	  }
	  gl.glEnd();
	}
	else {
	  double dy = 0.1875 * ((double) height);
	  
	  gl.glBegin(GL.GL_QUAD_STRIP);
	  {
	    gl.glTexCoord2d(0.0, 0.6875);
	    gl.glVertex2d(-0.5, -dy);
	    
	    gl.glTexCoord2d(1.0, 0.6875);
	    gl.glVertex2d(0.5, -dy);
	    
	    
	    gl.glTexCoord2d(0.0, 0.5625);
	    gl.glVertex2d(-0.5, -dy+0.125);
	    
	    gl.glTexCoord2d(1.0, 0.5625);
	    gl.glVertex2d(0.5, -dy+0.125);
	    
	    
	    gl.glTexCoord2d(0.0, 0.4375);
	    gl.glVertex2d(-0.5, dy-0.125);
	    
	    gl.glTexCoord2d(1.0, 0.4375);	
	    gl.glVertex2d(0.5, dy-0.125);
	    
	    
	    gl.glTexCoord2d(0.0, 0.3125);
	    gl.glVertex2d(-0.5, dy);
	    
	    gl.glTexCoord2d(1.0, 0.3125);	
	    gl.glVertex2d(0.5, dy);
	  }
	  gl.glEnd();
	}

	gl.glDisable(GL.GL_TEXTURE_2D); 
      }
      gl.glEndList();
    }

    return dl;
  }



  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Create an OpenGL display list for rendering a rectangle with rounded corners.
   * 
   * @param gl
   *   The OpenGL interface.
   * 
   * @param width
   *   The horizontal dimensions of the rectangle.
   * 
   * @param height
   *   The vertical dimensions of the rectangle.
   * 
   * @param radius
   *   The corner radius. 
   * 
   * @param fill
   *   The color used to fill the rectangle.
   * 
   * @param border 
   *   The color of the rectangle border.
   * 
   * @param borderWidth
   *   The line width of the border.
   */ 
  public synchronized int
  getRoundedRectDL
  (
   GL gl,
   double width, 
   double height, 
   double radius, 
   Color4d fill, 
   Color4d border, 
   float borderWidth 
  ) 
  { 
    int dl = gl.glGenLists(1);

    gl.glNewList(dl, GL.GL_COMPILE);
    {
      double x = width * 0.5; 
      double y = height * 0.5;
      
      gl.glColor4d(fill.r(), fill.g(), fill.b(), fill.a());
      gl.glBegin(GL.GL_QUADS);
      {
	gl.glVertex2d(-x,  y); 
	gl.glVertex2d( x,  y); 
	gl.glVertex2d( x, -y); 
	gl.glVertex2d(-x, -y); 
      }
      gl.glEnd();
      
      gl.glColor4d(border.r(), border.g(), border.b(), border.a());
      gl.glLineWidth(borderWidth);
      gl.glBegin(GL.GL_LINE_LOOP);
      {
	gl.glVertex2d(-x,  y); 
	gl.glVertex2d( x,  y); 
	gl.glVertex2d( x, -y); 
	gl.glVertex2d(-x, -y); 
      }
      gl.glEnd();
    }
    gl.glEndList();	  

    return dl;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The horizontal alignment of rendered text.
   */ 
  public enum
  TextAlignment
  {
    /**
     * Left justfied text.
     */ 
    Left,

    /** 
     * Centered text.
     */ 
    Center,

    /**
     * Right justified text.
     */ 
    Right;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The sole instance of this class.
   */ 
  private static GeometryMgr sGeometryMgr = new GeometryMgr();



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The OpenGL display lists which render text labels indexed by a tag which includs the 
   * font name, label text, horizontal alignment and inter-character spacing.
   */ 
  private HashMap<String,Integer>  pTextDLs;

  /**
   * The OpenGL display lists which render the link relationship icons indexed by the 
   * link relationship name. 
   */ 
  private EnumMap<LinkRelationship,Integer>  pLinkRelDLs;

  /**
   * The OpenGL display lists which render the node icons indexed by the combined node state
   * name.
   */ 
  private HashMap<String,Integer>  pNodeIconDLs;
  
  /**
   * The OpenGL display lists which render the job icons indexed by the job state name
   * and job height.
   */ 
  private HashMap<String,TreeMap<Integer,Integer>>  pJobIconDLs;
  
}
