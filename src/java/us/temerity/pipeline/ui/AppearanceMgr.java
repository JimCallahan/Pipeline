// $Id: AppearanceMgr.java,v 1.3 2004/05/18 00:30:47 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.awt.*;
import java.io.*;
import java.util.*;
import javax.media.j3d.*;
import javax.vecmath.*;

/*------------------------------------------------------------------------------------------*/
/*   A P P E A R A N C E   M G R                                                            */
/*------------------------------------------------------------------------------------------*/

/** 
 * Manages a cached set of {@link Appearance Appearance} instances used to render      
 * Pipeline nodes, links and their labels in {@link JNodeViewerPanel JNodeViewerPanel}
 * instances. <P> 
 */ 
public
class AppearanceMgr
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct the sole instance.
   */ 
  private 
  AppearanceMgr()
  {
    pLineApprs = new HashMap<String,Appearance>();
    pFontApprs = new HashMap<String,EnumMap<SelectionMode,Appearance[]>>();
    pNodeApprs = new HashMap<String,Appearance>();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the AppearanceMgr instance.
   */ 
  public static AppearanceMgr
  getInstance() 
  {
    return sAppearanceMgr;
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get an simple color appearance suitable for rendering lines. <P> 
   * 
   * The returned <CODE>Appearance</CODE> will render anti-aliased lines of (1) pixel width.
   * 
   * @param name
   *   The symbolic color name of the line texture.
   * 
   * @throws IOException
   *   If unable to find the texture.
   */ 
  public synchronized Appearance
  getLineAppearance
  (
   String name
  ) 
    throws IOException
  {
    if(name == null)
      throw new IllegalArgumentException("The color name cannot be (null)!");

    Appearance apr = pLineApprs.get(name);
    if(apr != null) 
      return apr;

    apr = new Appearance();

    apr.setTexture(TextureMgr.getInstance().getSimpleTexture(name));
    apr.setMaterial(null);

    {
      LineAttributes la = new LineAttributes();
      la.setLineAntialiasingEnable(true);
      la.setLineWidth(1.0f);
      apr.setLineAttributes(la);
    }
    
    pLineApprs.put(name, apr);

    return apr;
  }

  /**
   * Get an appearance for a specific character of the given font.
   * 
   * @param name
   *   The name of the font.
   * 
   * @param code
   *   The character code.
   * 
   * @param mode 
   *   The selection state of the node/link.
   * 
   * @return 
   *   The appearance or <CODE>null</CODE> if the character was unprintable.
   * 
   * @throws IOException
   *   If unable to find the texture.
   */ 
  public synchronized Appearance
  getFontAppearance
  (
   String name, 
   char code, 
   SelectionMode mode
  ) 
    throws IOException    
  {
    if(name == null)
      throw new IllegalArgumentException("The font name cannot be (null)!");

    if((code < 0) || (code > 127))
      throw new IllegalArgumentException
	("The character code (" + ((int) code) + ") for character " + 
	 "\"" + code + "\" must be in the [0-127] range!");

    FontGeometry geom = TextureMgr.getInstance().getFontGeometry(name);
    if(geom == null) 
      throw new IllegalArgumentException("The font (" + name + ") was unregistered!");
    if(!geom.isPrintable(code))
      return null;
    
    Appearance[] apprs = null;
    {
      EnumMap<SelectionMode,Appearance[]> mapprs = pFontApprs.get(name);
      if(mapprs == null) {
	mapprs = new EnumMap<SelectionMode,Appearance[]>(SelectionMode.class);
	pFontApprs.put(name, mapprs);
      }
      else {
	apprs = mapprs.get(mode);
      }

      if(apprs == null) {
	apprs = new Appearance[128];
	mapprs.put(mode, apprs);
      }
      else {
	if(apprs[code] != null)
	  return apprs[code];
      }
    }

    Appearance apr = new Appearance();
    {
      Texture2D tex = TextureMgr.getInstance().getFontTexture(name, code);
      assert(tex != null);

      apr.setTexture(tex);
      apr.setMaterial(null);

      {
	ColoringAttributes attr = new ColoringAttributes();
	attr.setShadeModel(ColoringAttributes.SHADE_FLAT);
	switch(mode) {
	case Normal: 
	  attr.setColor(new Color3f(Color.white));
	  break;
	  
	case Selected: 
	  attr.setColor(new Color3f(Color.yellow));
	  break;

	case Primary: 
	  attr.setColor(new Color3f(Color.cyan));
	  break;

	default:
	  assert(false);
	}

	apr.setColoringAttributes(attr);
      }

      {
	TextureAttributes attr = new TextureAttributes();
	attr.setTextureMode(TextureAttributes.MODULATE);
	apr.setTextureAttributes(attr);	
      }

      {
	RenderingAttributes attr = new RenderingAttributes();
	attr.setAlphaTestFunction(RenderingAttributes.GREATER);
	attr.setIgnoreVertexColors(true);
	apr.setRenderingAttributes(attr);
      }
      
      {
	TransparencyAttributes attr = new TransparencyAttributes();
	attr.setTransparencyMode(TransparencyAttributes.BLENDED);
	apr.setTransparencyAttributes(attr);
      }

      apprs[code] = apr;
    }

    return apr;
  }

  /** 
   * Get a link relationship icon appearance.
   * 
   * @param relationship 
   *   The nature of the relationship between files associated with the source and 
   *   target nodes. 
   * 
   * @throws IOException
   *   If unable to find the texture.
   */ 
  public synchronized Appearance
  getLinkRelationshipAppearance
  (
   LinkRelationship relationship
  ) 
    throws IOException
  { 
    String aname = ("LinkRelationship-" + relationship);
    
    Appearance apr = pNodeApprs.get(aname);
    if(apr != null) 
      return apr;

    apr = new Appearance();
    apr.setTexture(TextureMgr.getInstance().getTexture(aname));
	  
    {
      RenderingAttributes attr = new RenderingAttributes();
      attr.setAlphaTestFunction(RenderingAttributes.GREATER);
      apr.setRenderingAttributes(attr);
    }
	  
    {
      TransparencyAttributes attr = new TransparencyAttributes();
      attr.setTransparencyMode(TransparencyAttributes.BLENDED);
      apr.setTransparencyAttributes(attr);
    }

    pNodeApprs.put(aname, apr);

    return apr;
  }

  /** 
   * Get a node appearance.
   * 
   * @param name
   *   The node appearance name.
   * 
   * @param mode
   *   The selection state of the node.
   * 
   * @throws IOException
   *   If unable to find the texture.
   */ 
  public synchronized Appearance
  getNodeAppearance
  (
   String name,
   SelectionMode mode
  ) 
    throws IOException
  { 
    String aname = (name + "-" + mode);
      
    Appearance apr = pNodeApprs.get(aname);
    if(apr != null) 
      return apr;

    apr = new Appearance();
    apr.setTexture(TextureMgr.getInstance().getTexture(aname));
	  
    {
      RenderingAttributes attr = new RenderingAttributes();
      attr.setAlphaTestFunction(RenderingAttributes.GREATER);
      apr.setRenderingAttributes(attr);
    }
	  
    {
      TransparencyAttributes attr = new TransparencyAttributes();
      attr.setTransparencyMode(TransparencyAttributes.BLENDED);
      apr.setTransparencyAttributes(attr);
    }
    
    pNodeApprs.put(aname, apr);

    return apr;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The sole instance of this class.
   */ 
  private static AppearanceMgr sAppearanceMgr = new AppearanceMgr();



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The line appearances indexed by symbolic color name.
   */ 
  private HashMap<String,Appearance>  pLineApprs;


  /**
   * The per-character font appearances indexed by font name and selection mode.
   */ 
  private HashMap<String,EnumMap<SelectionMode,Appearance[]>>  pFontApprs;

  /**
   * The node appearances indexed by combined state name.
   */ 
  private HashMap<String,Appearance>  pNodeApprs;
  
}
