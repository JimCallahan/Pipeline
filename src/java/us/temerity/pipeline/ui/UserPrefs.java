// $Id: UserPrefs.java,v 1.4 2004/05/16 19:14:29 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*;
import us.temerity.pipeline.glue.*;

import java.awt.*;
import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   U S E R   P R E F S                                                                    */
/*------------------------------------------------------------------------------------------*/

/** 
 * 
 */ 
public
class UserPrefs
  implements Glueable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct the sole instance.
   */ 
  public 
  UserPrefs()
  {
    reset();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the UserPrefs instance.
   */ 
  public static UserPrefs
  getInstance() 
  {
    return sUserPrefs;
  }



  /*-- PANEL - NODE VIEWER - NODE - APPEARANCE ---------------------------------------------*/

  /**
   * Get the horizontal distance between nodes.
   */ 
  public double 
  getNodeSpaceX()
  {
    return pNodeSpaceX;
  }

  /**
   * Set the horizontal distance between nodes.
   */ 
  public void
  setNodeSpaceX
  (
   double v
  )
  {
    pNodeSpaceX = v;
  }


  /**
   * Get the vertical distance between nodes.
   */ 
  public double 
  getNodeSpaceY()
  {
    return pNodeSpaceY;
  }

  /**
   * Set the vertical distance between nodes.
   */ 
  public void 
  setNodeSpaceY
  (
   double v
  )
  {
    pNodeSpaceY = v;
  }


  /**
   * Get the vertical offset distance for nodes with an odd depth level.
   */ 
  public double 
  getNodeOffset()
  {
    return pNodeOffset;
  }

  /**
   * Set the vertical offset distance for nodes with an odd depth level.
   */ 
  public void 
  setNodeOffset
  (
   double v
  )
  {
    pNodeOffset = v;
  }


 
  /*-- PANEL - NODE VIEWER - LINKS - APPEARANCE --------------------------------------------*/
 
  /**
   * Get whether the link lines are antialiased.
   */ 
  public boolean
  getLinkAntiAlias()
  {
    return pLinkAntiAlias;
  }

  /**
   * Set whether the link lines are antialiased.
   */ 
  public void 
  setLinkAntiAlias
  (
   boolean tf
  )
  {
    pLinkAntiAlias = tf;
  }


  /**
   * Get the thickness of link lines.
   */ 
  public double 
  getLinkThickness()
  {
    return pLinkThickness;
  }

  /**
   * Set the vertical distance between nodes.
   */ 
  public void 
  setLinkThickness
  (
   double v
  )
  {
    pLinkThickness = v;
  }


  /**
   * Get the name of the simple color texture to use for link lines.
   */ 
  public String
  getLinkColorName()
  {
    return pLinkColorName;
  }

  /**
   * Set the name of the simple color texture to use for link lines.
   */ 
  public void 
  setLinkColorName
  (
   String name
  )
  {
    pLinkColorName = name;
  }

  
  /**
   * Get whether to draw arrow heads showing the direction of the link.
   */ 
  public boolean
  getDrawArrowHeads()
  {
    return pDrawArrowHeads;
  }

  /**
   * Set whether to draw arrow heads showing the direction of the link.
   */ 
  public void 
  setDrawArrowHeads
  (
   boolean tf
  )
  {
    pDrawArrowHeads = tf;
  }


  /**
   * Get the length of the link arrow head.
   */ 
  public double 
  getArrowHeadLength()
  {
    return pArrowHeadLength;
  }

  /**
   * Set the length of the link arrow head.
   */ 
  public void 
  setArrowHeadLength
  (
   double v
  )
  {
    pArrowHeadLength = v;
  }


  /**
   * Get the width of the link arrow head.
   */ 
  public double 
  getArrowHeadWidth()
  {
    return pArrowHeadWidth;
  }

  /**
   * Set the width of the link arrow head.
   */ 
  public void 
  setArrowHeadWidth
  (
   double v
  )
  {
    pArrowHeadWidth = v;
  }


  /**
   * Get the distance between node and the start/end of link.
   */ 
  public double 
  getLinkGap()
  {
    return pLinkGap;
  }

  /**
   * Set the distance between node and the start/end of link.
   */
  public void 
  setLinkGap
  (
   double v
  )
  {
    pLinkGap = v;
  }


  /*-- PANEL - NODE VIEWER - HOT KEYS ------------------------------------------------------*/
 
  /**
   * Get the automatic expand nodes hot key.
   */ 
  public HotKey
  getAutomaticExpandNodes() 
  {
    return pAutomaticExpandNodes;
  }
  
  /**
   * Set the automatic expand nodes hot key.
   */ 
  public void 
  setAutomaticExpandNodes
  (
   HotKey key
  ) 
  {
    pAutomaticExpandNodes = key;
  }


  /**
   * Get the collapse all nodes action hot key.
   */ 
  public HotKey
  getCollapseAllNodes() 
  {
    return pCollapseAllNodes;
  }
  
  /**
   * Set the collapse all nodes action hot key.
   */ 
  public void 
  setCollapseAllNodes
  (
   HotKey key
  ) 
  {
    pCollapseAllNodes = key;
  }


  /**
   * Get the expand all nodes action hot key.
   */ 
  public HotKey
  getExpandAllNodes() 
  {
    return pExpandAllNodes;
  }
  
  /**
   * Set the expand all nodes action hot key.
   */ 
  public void 
  setExpandAllNodes
  (
   HotKey key
  ) 
  {
    pExpandAllNodes = key;
  }


  /**
   * Get show/hide downstream nodes hot key.
   */ 
  public HotKey
  getShowHideDownstreamNodes() 
  {
    return pShowHideDownstreamNodes;
  }
  
  /**
   * Set show/hide downstream nodes hot key.
   */ 
  public void 
  setShowHideDownstreamNodes
  (
   HotKey key
  ) 
  {
    pShowHideDownstreamNodes = key;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Reset all preferences to the default values.
   */ 
  public void 
  reset() 
  {
    /* panel - node viewer - nodes - appearance */ 
    {
      pNodeSpaceX = 2.0;
      pNodeSpaceY = 2.0;
      pNodeOffset = 0.5;
    }
    
    /* panel - node viewer - links - appearance */ 
    {
      pLinkAntiAlias = true;
      pLinkThickness = 1.0;
      pLinkColorName = "LightGrey";
      
      pDrawArrowHeads = true;
      pArrowHeadLength    = 0.15;
      pArrowHeadWidth     = 0.05;
  
      pLinkGap = 0.1;
    }
     
    /* panel - node viewer - hot keys */ 
    {    
      pAutomaticExpandNodes = null;
      pCollapseAllNodes     = null;
      pExpandAllNodes       = null;
      
      pShowHideDownstreamNodes = null;
    }


    // ...

  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   I / O                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Save the preferences to disk.
   */ 
  public static void 
  save() 
    throws GlueException, GlueLockException
  {
    File file = new File(PackageInfo.sHomeDir, 
			 PackageInfo.sUser + "/.pipeline/preferences");
    LockedGlueFile.save(file, "UserPreferences", sUserPrefs);
  }
  
  /**
   * Load the preferences from disk.
   */ 
  public static void 
  load() 
    throws GlueException, GlueLockException
  {
    File file = new File(PackageInfo.sHomeDir, 
			 PackageInfo.sUser + "/.pipeline/preferences");
    sUserPrefs = (UserPrefs) LockedGlueFile.load(file);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  public void 
  toGlue
  ( 
   GlueEncoder encoder   
  ) 
    throws GlueException
  {
    /* panel - node viewer - nodes - appearance */ 
    {
      encoder.encode("NodeSpaceX", pNodeSpaceX);
      encoder.encode("NodeSpaceY", pNodeSpaceY);
      encoder.encode("NodeOffset", pNodeOffset);
    }

    /* panel - node viewer - links - appearance */ 
    {
      encoder.encode("LinkAntiAlias", pLinkAntiAlias);
      encoder.encode("LinkThickness", pLinkThickness);
      encoder.encode("LinkColorName", pLinkColorName);

      encoder.encode("DrawArrowHeads",  pDrawArrowHeads);
      encoder.encode("ArrowHeadLength", pArrowHeadLength);
      encoder.encode("ArrowHeadWidth",  pArrowHeadWidth);

      encoder.encode("LinkGap", pLinkGap);      
    }
    
    /* panel - node viewer - hot keys */ 
    {
      encoder.encode("AutomaticExpandNodes", pAutomaticExpandNodes);
      encoder.encode("ExpandAllNodes",       pExpandAllNodes);
      encoder.encode("CollapseAllNodes",     pCollapseAllNodes);

      encoder.encode("ShowHideDownstreamNodes", pShowHideDownstreamNodes);
    }


    // ...

  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    /* panel - node viewer - nodes - appearance */ 
    {
      Double spaceX = (Double) decoder.decode("NodeSpaceX");
      if(spaceX != null)
	pNodeSpaceX = spaceX;
      
      Double spaceY = (Double) decoder.decode("NodeSpaceY");
      if(spaceY != null)
	pNodeSpaceY = spaceY;
      
      Double offset = (Double) decoder.decode("NodeOffset");
      if(offset != null)
	pNodeOffset = offset;
    }
    
    /* panel - node viewer - links - appearance */ 
    {
      Boolean antialias = (Boolean) decoder.decode("LinkAntiAlias");
      if(antialias != null)
	pLinkAntiAlias = antialias;
      
      Double thickness = (Double) decoder.decode("LinkThickness");
      if(thickness != null)
	pLinkThickness = thickness;
      
      String colorName = (String) decoder.decode("LinkColorName");
      if(colorName != null)
	pLinkColorName = colorName;


      Boolean drawArrow = (Boolean) decoder.decode("DrawArrowHeads");
      if(drawArrow != null)
	pDrawArrowHeads = drawArrow;

      Double arrowLength = (Double) decoder.decode("ArrowHeadLength");
      if(arrowLength != null)
	pArrowHeadLength = arrowLength;
      
      Double arrowWidth = (Double) decoder.decode("ArrowHeadWidth");
      if(arrowWidth != null)
	pArrowHeadWidth = arrowWidth;

      Double linkGap = (Double) decoder.decode("LinkGap");
      if(linkGap != null)
	pLinkGap = linkGap;
    }
    
    /* panel - node viewer - hot keys */ 
    {
      HotKey autoExpand = (HotKey) decoder.decode("AutomaticExpandNodes");
      if(autoExpand != null)
	pAutomaticExpandNodes = autoExpand;
      
      HotKey collapse = (HotKey) decoder.decode("CollapseAllNodes");
      if(collapse != null)
	pCollapseAllNodes = collapse;
      
      HotKey expand = (HotKey) decoder.decode("ExpandAllNodes");
      if(expand != null)
    	  pExpandAllNodes = expand;
      
      HotKey showHide = (HotKey) decoder.decode("ShowHideDownstreamNodes");
      if(showHide != null)
	pShowHideDownstreamNodes = showHide;
    }

    
    // ...
    
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The sole instance of this class.
   */ 
  private static UserPrefs sUserPrefs = new UserPrefs();



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/


  /*-- PANEL - NODE VIEWER - NODE - APPEARANCE ---------------------------------------------*/

  /**
   * The horizontal distance between nodes.
   */ 
  private double  pNodeSpaceX;
  
  /**
   * The vertical distance between nodes.
   */ 
  private double  pNodeSpaceY;
  
  /**
   * The vertical offset distance for nodes with an odd depth level.
   */ 
  private double  pNodeOffset;



  /*-- PANEL - NODE VIEWER - LINKS - APPEARANCE --------------------------------------------*/
     
  /**
   * Whether to anti-alias link lines.
   */ 
  private boolean  pLinkAntiAlias;

  /** 
   * The thickness of link lines.
   */
  private double  pLinkThickness;

  /**
   * The name of the simple color texture to use for link lines.
   */ 
  private String  pLinkColorName;


  /**
   * Whether to draw arrow heads showing the direction of the link.
   */ 
  private boolean  pDrawArrowHeads;
  
  /**
   * The length of the link arrow head.
   */ 
  private double  pArrowHeadLength;

  /**
   * The width of the link arrow head.
   */ 
  private double  pArrowHeadWidth;
  

  /**
   * The distance between node and the start/end of link.
   */ 
  private double  pLinkGap;



  /*-- PANEL - NODE VIEWER - HOT KEYS ------------------------------------------------------*/
 
  /**
   * Automatically expand the first occurance of a node.
   */ 
  private HotKey  pAutomaticExpandNodes;
  
  /**
   * Expand all nodes. 
   */ 
  private HotKey  pExpandAllNodes;

  /**
   * Collapse all nodes. 
   */ 
  private HotKey  pCollapseAllNodes;

  /**
   * Show/Hide nodes downstream of the focus node.
   */ 
  private HotKey  pShowHideDownstreamNodes;

  
}
