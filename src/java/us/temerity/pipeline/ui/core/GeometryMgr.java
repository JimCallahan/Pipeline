// $Id: GeometryMgr.java,v 1.8 2007/06/26 05:18:57 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;

import java.awt.*;
import java.io.*;
import java.util.*;

import javax.media.opengl.*;

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
    pLinkIconDLs = new TreeMap<String,Integer>(); 
    pNodeIconDLs = new TreeMap<String,Integer>();
    pJobIconDLs  = new DoubleMap<String,Integer,Integer>();
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
   * @throws PipelineException
   *   If unable to lookup the font.
   */ 
  public double 
  getTextWidth
  (
   String name, 
   String text,
   double space
  ) 
    throws PipelineException   
  {
    double width = 0.0;
    {
      FontGeometry geom = TextureMgr.getInstance().getFontGeometry(name);
      if(geom == null) 
	throw new PipelineException 
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
   * @throws PipelineException
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
    throws PipelineException    
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
	  throw new PipelineException 
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
   * @param name
   *   The component name.
   * 
   * @return 
   *   The display list handle.
   * 
   * @throws PipelineException
   *   If unable to lookup or generate the display list.
   */ 
  public synchronized int
  getLinkIconDL
  (
   GL gl,
   String name
  ) 
    throws PipelineException
  { 
    Integer dl = pLinkIconDLs.get(name);
    if(dl == null) {
      int texID = TextureMgr.getInstance().getTexture(gl, name); 

      dl = gl.glGenLists(1);
      pLinkIconDLs.put(name, dl);

      gl.glNewList(dl, GL.GL_COMPILE);
      {
	gl.glEnable(GL.GL_TEXTURE_2D);
	gl.glBindTexture(GL.GL_TEXTURE_2D, texID);
	
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
   *   The component name. 
   * 
   * @return 
   *   The display list handle.
   * 
   * @throws PipelineException
   *   If unable to lookup or generate the display list.
   */ 
  public synchronized int
  getIconDL
  (
   GL gl,
   String name
  ) 
    throws PipelineException
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
   * and centered around the origin. <P> 
   * 
   * The base color for the texture is set in the display list to a constant white.
   * 
   * @param gl
   *   The OpenGL interface.
   * 
   * @param name
   *   The component name.
   * 
   * @return 
   *   The display list handle.
   * 
   * @throws PipelineException
   *   If unable to lookup or generate the display list.
   */ 
  public synchronized int
  getNodeIconDL
  (
   GL gl,
   String name
  ) 
    throws PipelineException
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
   *   The component name.
   * 
   * @param height
   *   The vertical job span of the icon.
   * 
   * @return 
   *   The display list handle.
   * 
   * @throws PipelineException
   *   If unable to lookup or generate the display list.
   */ 
  public synchronized int
  getJobIconDL
  (
   GL gl,
   String name, 
   int height
  ) 
    throws PipelineException
  { 
    if(height < 1) 
      throw new IllegalArgumentException
	("The height (" + height + ") must be greater-than zero!");

    Integer dl = pJobIconDLs.get(name, height);
    if(dl == null) {
      int texID = TextureMgr.getInstance().getTexture(gl, name);

      dl = gl.glGenLists(1);
      pJobIconDLs.put(name, height, dl);

      gl.glNewList(dl, GL.GL_COMPILE);
      {
	gl.glEnable(GL.GL_TEXTURE_2D);
	gl.glBindTexture(GL.GL_TEXTURE_2D, texID);
	
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
   * Renders an outlined 2D box using OpenGL.
   * 
   * @param gl
   *   The OpenGL interface.
   * 
   * @param boxExtent
   *   The bounds of the box.
   * 
   * @param boxColor
   *   The color to render the box background.
   * 
   * @param outlineWidth
   *   The width of the outline.
   * 
   * @param outlineColor
   *   The color to render the box outline. 
   */ 
  public void 
  renderOutlinedBox
  (
   GL gl,
   BBox2d boxExtent, 
   Color4d boxColor, 
   float outlineWidth, 
   Color4d outlineColor
  ) 
  { 
    Point2d bmin = boxExtent.getMin();
    Point2d bmax = boxExtent.getMax();

    gl.glColor4d(boxColor.r(), boxColor.g(), boxColor.b(), boxColor.a());
    gl.glBegin(GL.GL_QUADS);
    {
      gl.glVertex2d(bmin.x(), bmin.y()); 
      gl.glVertex2d(bmax.x(), bmin.y()); 
      gl.glVertex2d(bmax.x(), bmax.y()); 
      gl.glVertex2d(bmin.x(), bmax.y()); 
    }
    gl.glEnd();
    
    gl.glColor4d(outlineColor.r(), outlineColor.g(), outlineColor.b(), outlineColor.a());
    gl.glLineWidth(outlineWidth);
    gl.glBegin(GL.GL_LINE_LOOP);
    {
      gl.glVertex2d(bmin.x(), bmin.y()); 
      gl.glVertex2d(bmax.x(), bmin.y()); 
      gl.glVertex2d(bmax.x(), bmax.y()); 
      gl.glVertex2d(bmin.x(), bmax.y()); 
    }
    gl.glEnd();
  }

  /** 
   * Renders an outlined 2D box using OpenGL.
   * 
   * @param gl
   *   The OpenGL interface.
   * 
   * @param cornerRadius
   *   The radius of the rounded corners.
   * 
   * @param cornerDivs
   *   The number of divisions of the corner radius.
   * 
   * @param boxExtent
   *   The bounds of the box.
   * 
   * @param boxColor
   *   The color to render the box background.
   * 
   * @param outlineWidth
   *   The width of the outline.
   * 
   * @param outlineColor
   *   The color to render the box outline. 
   */ 
  public void 
  renderOutlinedRoundedBox
  (
   GL gl,
   double cornerRadius, 
   int cornerDivs, 
   BBox2d boxExtent, 
   Color4d boxColor, 
   float outlineWidth, 
   Color4d outlineColor
  ) 
  { 
    Vector2d range = boxExtent.getRange();
    if(cornerRadius >= (Math.min(range.x(), range.y()) * 2.0))
      throw new IllegalArgumentException
	("The corner radius (" + cornerRadius + ") is too large for a box of size " + 
	 range + "!");

    if(cornerDivs < 1) 
      throw new IllegalArgumentException
	("The number of divisions of the corner radius (" + cornerDivs + ") must be at " +
	 "least (1)!");

    Point2d bmin = boxExtent.getMin();
    Point2d bmax = boxExtent.getMax();

    Point2d cs[] = new Point2d[cornerDivs+1];
    int wk;
    for(wk=0; wk<cs.length; wk++) {
      double t = Math.PI * 0.5 * (((double) wk) / ((double) cornerDivs));
      cs[wk] = new Point2d(Math.sin(t)*cornerRadius, Math.cos(t)*cornerRadius);
    }

    double x1 = bmin.x(); 
    double x2 = bmin.x() + cornerRadius;
    double x3 = bmax.x() - cornerRadius; 
    double x4 = bmax.x();
      
    double y1 = bmin.y(); 
    double y2 = bmin.y() + cornerRadius;
    double y3 = bmax.y() - cornerRadius; 
    double y4 = bmax.y();

    gl.glColor4d(boxColor.r(), boxColor.g(), boxColor.b(), boxColor.a());
    gl.glBegin(GL.GL_QUADS);
    {
      gl.glVertex2d(x1, y2); 
      gl.glVertex2d(x2, y2); 
      gl.glVertex2d(x2, y3); 
      gl.glVertex2d(x1, y3); 

      gl.glVertex2d(x2, y1); 
      gl.glVertex2d(x3, y1); 
      gl.glVertex2d(x3, y4); 
      gl.glVertex2d(x2, y4); 

      gl.glVertex2d(x3, y2); 
      gl.glVertex2d(x4, y2); 
      gl.glVertex2d(x4, y3); 
      gl.glVertex2d(x3, y3); 
    }
    gl.glEnd();
    
    gl.glBegin(GL.GL_TRIANGLE_FAN);
    {
      gl.glVertex2d(x2, y2); 
      for(wk=0; wk<cs.length; wk++) 
	gl.glVertex2d(x3+cs[wk].x(), y3+cs[wk].y()); 
    }
    gl.glEnd();

    gl.glBegin(GL.GL_TRIANGLE_FAN);
    {
      gl.glVertex2d(x3, y2); 
      for(wk=0; wk<cs.length; wk++) 
	gl.glVertex2d(x3+cs[wk].x(), y2-cs[wk].y()); 
    }
    gl.glEnd();
    
    gl.glBegin(GL.GL_TRIANGLE_FAN);
    {
      gl.glVertex2d(x2, y2); 
      for(wk=0; wk<cs.length; wk++) 
	gl.glVertex2d(x2-cs[wk].x(), y2-cs[wk].y()); 
    }
    gl.glEnd();
    
    gl.glBegin(GL.GL_TRIANGLE_FAN);
    {
      gl.glVertex2d(x2, y2); 
      for(wk=0; wk<cs.length; wk++) 
	gl.glVertex2d(x2-cs[wk].x(), y3+cs[wk].y()); 
    }
    gl.glEnd();


    gl.glColor4d(outlineColor.r(), outlineColor.g(), outlineColor.b(), outlineColor.a());
    gl.glLineWidth(outlineWidth);
    gl.glEnable(GL.GL_LINE_SMOOTH); 
    gl.glBegin(GL.GL_LINE_LOOP);
    {
      for(wk=0; wk<cs.length; wk++) 
	gl.glVertex2d(x3+cs[wk].x(), y3+cs[wk].y()); 

      gl.glVertex2d(x4, y3); 
      gl.glVertex2d(x4, y2); 

      for(wk=cs.length-1; wk>=0; wk--) 
	gl.glVertex2d(x3+cs[wk].x(), y2-cs[wk].y()); 

      gl.glVertex2d(x3, y1); 
      gl.glVertex2d(x2, y1); 
      
      for(wk=0; wk<cs.length; wk++) 
	gl.glVertex2d(x2-cs[wk].x(), y2-cs[wk].y()); 

      gl.glVertex2d(x1, y2); 
      gl.glVertex2d(x1, y3);
      
      for(wk=cs.length-1; wk>=0; wk--) 
	gl.glVertex2d(x2-cs[wk].x(), y3+cs[wk].y()); 
      
      gl.glVertex2d(x2, y4); 
      gl.glVertex2d(x3, y4);
    }
    gl.glEnd();
    gl.glDisable(GL.GL_LINE_SMOOTH);
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
   * The OpenGL display lists which render the link relationship icons indexed by 
   * component name. 
   */ 
  private TreeMap<String,Integer>  pLinkIconDLs;

  /**
   * The OpenGL display lists which render the node icons indexed by the component name. 
   * name.
   */ 
  private TreeMap<String,Integer>  pNodeIconDLs;
  
  /**
   * The OpenGL display lists which render the job icons indexed by component name
   * and job height.
   */ 
  private DoubleMap<String,Integer,Integer>  pJobIconDLs;
  
}
