// $Id: GeometryMgr.java,v 1.1 2004/12/16 15:41:03 jim Exp $

package us.temerity.pipeline.ui;

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
 * Manages a set of resuable OpenGL display lists for common geometry components used in 
 * the {@link JNodeViewerPanel JNodeViewerPanel} and 
 * {@link JQueueJobViewerPanel.JQueueJobViewerPanel}.
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
   * Get an OpenGL display list which renders a text string as textures quads. <P> 
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
      double width = 0.0;
      {
	FontGeometry geom = texMgr.getFontGeometry(name);
	if(geom == null) 
	  throw new IOException 
	    ("No font named (" + name + ") has been registered with the TextureMgr!");
	
	Point2d pos = new Point2d();
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
	
	gl.glBegin(GL.GL_QUADS);
	{
	  gl.glTexCoord2d(0.0, 1.0);
	  gl.glVertex3d(-0.25, -0.25, 0.0);
	  
	  gl.glTexCoord2d(1.0, 1.0);
	  gl.glVertex3d(0.25, -0.25, 0.0);
	  
	  gl.glTexCoord2d(1.0, 0.0);	
	  gl.glVertex3d(0.25, 0.25, 0.0);
	  
	  gl.glTexCoord2d(0.0, 0.0);
	  gl.glVertex3d(-0.25, 0.25, 0.0);
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
	
	gl.glBegin(GL.GL_QUADS);
	{
	  gl.glTexCoord2d(0.0, 1.0);
	  gl.glVertex3d(-0.5, -0.5, 0.0);
	  
	  gl.glTexCoord2d(1.0, 1.0);
	  gl.glVertex3d(0.5, -0.5, 0.0);
	  
	  gl.glTexCoord2d(1.0, 0.0);	
	  gl.glVertex3d(0.5, 0.5, 0.0);
	  
	  gl.glTexCoord2d(0.0, 0.0);
	  gl.glVertex3d(-0.5, 0.5, 0.0);
	}
	gl.glEnd();

	gl.glDisable(GL.GL_TEXTURE_2D); 
      }
      gl.glEndList();
    }

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
  
}
