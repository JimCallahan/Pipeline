// $Id: ViewerLinks.java,v 1.1 2004/05/16 19:21:38 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.io.*;
import java.util.*;
import javax.vecmath.*;
import javax.media.j3d.*;
import com.sun.j3d.utils.geometry.*;

/*------------------------------------------------------------------------------------------*/
/*   V I E W E R   L I N K S                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A Java3D based graphical representation of the links between Pipeline nodes.
 */
public 
class ViewerLinks
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constuct a new set of viewer links.
   */ 
  public 
  ViewerLinks() 
  {
    /* initialize state fields */ 
    {
      pUpstreamLinks   = new HashMap<NodePath,ArrayList<Link>>();
      pDownstreamLinks = new HashMap<NodePath,ArrayList<Link>>();

      pLineAntiAlias = true;
      pLineThickness = 1.0;
      pLineColorName = "LightGrey";

      pLinksChanged = true;
    }

    /* initialize the Java3D geometry */ 
    {
      /* the root branch group */ 
      pRoot = new BranchGroup();
      pRoot.setPickable(false);
      
      /* the link line geometry */ 
      {
	pShape = new Shape3D();
	pShape.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
	pShape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);

	pRoot.addChild(pShape);
      }
    }    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Clear the previous set of links.
   */
  public void 
  clear()
  {
    pUpstreamLinks.clear();
    pDownstreamLinks.clear();

    pLinksChanged = true;
  }
  
  /** 
   * Add a link between the given viewer nodes. <P> 
   * 
   * @param target
   *   The viewer node downstream of the link.
   * 
   * @param source 
   *   The viewer node upstream of the link.
   * 
   * @param link
   *   The link details.
   */ 
  public void
  addUpstreamLink
  (
   ViewerNode target, 
   ViewerNode source, 
   LinkCommon link
  ) 
  {
    NodePath path = target.getNodePath();
    ArrayList<Link> links = pUpstreamLinks.get(path);
    if(links == null) {
      links = new ArrayList<Link>();
      pUpstreamLinks.put(path, links);
    }
    links.add(new Link(target, source, link));    
    pLinksChanged = true;
  }

  /** 
   * Add a link between the given viewer nodes. <P> 
   * 
   * @param target
   *   The viewer node downstream of the link.
   * 
   * @param source 
   *   The viewer node upstream of the link.
   */ 
  public void
  addDownstreamLink
  (
   ViewerNode target, 
   ViewerNode source
  ) 
  {
    NodePath path = source.getNodePath();
    ArrayList<Link> links = pDownstreamLinks.get(path);
    if(links == null) {
      links = new ArrayList<Link>();
      pDownstreamLinks.put(path, links);
    }
    links.add(new Link(target, source, null));    
    pLinksChanged = true;
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the root group which contains all Java3D geometry.
   */ 
  public BranchGroup 
  getBranchGroup() 
  {
    return pRoot;
  }
  



  /*----------------------------------------------------------------------------------------*/
  /*   L I N K   S T A T E                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the geometry and appearance based on the current set of links.
   */ 
  public void 
  update() 
  {
    UserPrefs prefs = UserPrefs.getInstance();

    /* update the link appearance */ 
    try {  
      if((pLineAntiAlias != prefs.getLinkAntiAlias()) || 
	 (pLineThickness != prefs.getLinkThickness()) ||
	 (!pLineColorName.equals(prefs.getLinkColorName()))) {

	pLineAntiAlias = prefs.getLinkAntiAlias();
	pLineThickness = prefs.getLinkThickness();
	pLineColorName = prefs.getLinkColorName();

	Appearance apr = new Appearance();

	apr.setTexture(TextureMgr.getInstance().getSimpleTexture(pLineColorName));
	apr.setMaterial(null);
	
	{
	  LineAttributes la = new LineAttributes();
	  la.setLineAntialiasingEnable(pLineAntiAlias);
	  la.setLineWidth((float) pLineThickness);
	  apr.setLineAttributes(la);
	}
	
	pShape.setAppearance(apr);
      }
    }
    catch(IOException ex) {
      Logs.tex.severe("Internal Error:\n" + 
		      "  " + ex.getMessage());
      Logs.flush();
      System.exit(1);
    }
    
    /* update the line geometry */ 
    if(pLinksChanged) {
      // DEBUG 
      {
	System.out.print("Upstream Links:\n");
	for(NodePath parent : pUpstreamLinks.keySet()) {
	  System.out.print("  " + parent + "\n");
	  for(Link link : pUpstreamLinks.get(parent)) {
	    System.out.print("    TargetPos: " + link.getTargetPos() + "\n" + 
			     "    SourcePos: " + link.getSourcePos() + "\n" +
			     "      Details: " + 
			     link.getDetails().getCatagory().getName() + " [" + 
			     link.getDetails().getCatagory().getPolicy() + "] - " + 
			     link.getDetails().getRelationship() + "\n\n");
	  }
	  System.out.print("\n");
	}
	
	System.out.print("Downstream Links:\n");
	for(NodePath parent : pDownstreamLinks.keySet()) {
	  System.out.print("  " + parent + "\n");
	  for(Link link : pDownstreamLinks.get(parent)) {
	    System.out.print("    TargetPos: " + link.getTargetPos() + "\n" + 
			     "    SourcePos: " + link.getSourcePos() + "\n\n");
	  }
	  System.out.print("\n");
	}
      }
      // DEBUG 
      
	
			   
    
      // ...
      
      pLinksChanged = false;
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A link between viewer nodes.
   */ 
  private 
  class Link
  {
    public Link
    (
     ViewerNode target, 
     ViewerNode source, 
     LinkCommon details
    ) 
    {
      pTarget  = target;
      pSource  = source; 
      pDetails = details;
    }

    public Point2d
    getTargetPos() 
    {
      return pTarget.getPosition();
    }

    public Point2d
    getSourcePos()
    {
      return pSource.getPosition();
    }
    
    public LinkCommon
    getDetails() 
    {
      return pDetails;
    }

    private ViewerNode pTarget; 
    private ViewerNode pSource; 
    private LinkCommon pDetails; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The table of upstream links indexed by the path to the common downstream node.
   */ 
  private HashMap<NodePath,ArrayList<Link>>  pUpstreamLinks;

  /**
   * The table of downstream links indexed by the path to the common upstream node.
   */ 
  private HashMap<NodePath,ArrayList<Link>>  pDownstreamLinks;


  /**
   * Whether to anti-alias link lines.
   */ 
  private boolean  pLineAntiAlias;

  /** 
   * The thickness of link lines.
   */
  private double  pLineThickness;

  /**
   * The name of the simple color texture to use for link lines.
   */ 
  private String  pLineColorName;

  /**
   * Whether the link geometry has changed since the last update.
   */ 
  private boolean pLinksChanged; 


  /**
   * The root branch group. 
   */ 
  private BranchGroup  pRoot;     

  /**
   * The link line geometry.
   */ 
  private Shape3D  pShape;  


}
