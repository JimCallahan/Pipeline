// $Id: ViewerLinks.java,v 1.8 2004/10/03 17:07:26 jim Exp $

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

      pAppearanceChanged = true;

      pLineAntiAlias = true;
      pLineThickness = 1.0;
      pLineColorName = "LightGrey";

      pLinksChanged = true;
    }

    /* initialize the Java3D geometry */ 
    {
      /* the root branch group */ 
      pRoot = new BranchGroup();
      
      /* unpickable geometry group */ 
      {
	BranchGroup group = new BranchGroup();
	group.setPickable(false);

	/* the line visibility switch */ 
	{
	  pLineSwitch = new Switch();
	  pLineSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
	  
	  group.addChild(pLineSwitch);
	}
	
	/* the line geometry */ 
	{
	  pLines = new Shape3D();
	  pLines.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
	  pLines.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
	  
	  pLineSwitch.addChild(pLines);
	}
	
	/* the line visibility switch */ 
	{
	  pTriSwitch = new Switch();
	  pTriSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
	  
	  group.addChild(pTriSwitch);
	}
	
	/* the polygon geometry */ 
	{
	  pTris = new Shape3D();
	  pTris.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
	  pTris.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
	  
	  pTriSwitch.addChild(pTris);
	}

	pRoot.addChild(group);
      }

      /* the link relationship icons */ 
      {
	pLinkRelationshipPool = new ViewerLinkRelationshipPool();
	pRoot.addChild(pLinkRelationshipPool.getBranchGroup());
      }
    }    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
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
   * Prepare to update the links.
   */ 
  public void 
  updatePrep()
  {
    pUpstreamLinks.clear();
    pDownstreamLinks.clear();

    pLinksChanged = true;

    pLinkRelationshipPool.updatePrep();
  }
  
  /**
   * Update the geometry and appearance based on the current set of links.
   */ 
  public void 
  update() 
  {
    UserPrefs prefs = UserPrefs.getInstance();

    /* update the link appearance */ 
    try {  
      if(pAppearanceChanged || 
	 (pLineAntiAlias != prefs.getLinkAntiAlias()) || 
	 (pLineThickness != prefs.getLinkThickness()) ||
	 (!pLineColorName.equals(prefs.getLinkColorName()))) {

	pLineAntiAlias = prefs.getLinkAntiAlias();
	pLineThickness = prefs.getLinkThickness();
	pLineColorName = prefs.getLinkColorName();

	{
	  Appearance apr = new Appearance();
	  
	  apr.setTexture(TextureMgr.getInstance().getSimpleTexture(pLineColorName));
	  apr.setMaterial(null);
	  
	  {
	    LineAttributes la = new LineAttributes();
	    la.setLineAntialiasingEnable(pLineAntiAlias);
	    la.setLineWidth((float) pLineThickness);
	    apr.setLineAttributes(la);
	  }
	  
	  pLines.setAppearance(apr);
	}

	{
	  Appearance apr = new Appearance();
	  
	  apr.setTexture(TextureMgr.getInstance().getSimpleTexture(pLineColorName));
	  apr.setMaterial(null);

	  apr.setPolygonAttributes(new PolygonAttributes());
	    
	  pTris.setAppearance(apr);
	}	

	pAppearanceChanged = false;
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
      if(pUpstreamLinks.isEmpty() && pDownstreamLinks.isEmpty()) {
	pLineSwitch.setWhichChild(Switch.CHILD_NONE);
	pTriSwitch.setWhichChild(Switch.CHILD_NONE);
      }
      else {
	/* compute the number of vertices */ 
	int lvCnt = 0;
	int tvCnt = 0;
	{
	  for(NodePath path : pUpstreamLinks.keySet()) {
	    ArrayList<Link> links = pUpstreamLinks.get(path);
	    lvCnt += 2*links.size() + 4;
	    
 	    if(prefs.getDrawArrowHeads()) 
 	      tvCnt += 3;
	    
	    {
	      Link link = links.get(0);
	      NodeStatus status = link.getTargetNode().getNodeStatus(); 
	      if(status != null) {
		NodeDetails details = status.getDetails();
		if(details != null) {
		  NodeCommon com = details.getWorkingVersion();
		  if(com == null) 
		    com = details.getLatestVersion();
		  if((com != null) && (com.getAction() != null) && 
		     (!com.isActionEnabled()) && (prefs.getDrawDisabledAction()))
		    lvCnt += 2;
		}
	      }
	    }

	    for(Link link : links) {
	      if(prefs.getDrawLinkPolicy()) {
		switch(link.getLink().getPolicy()) {
		case Association:
		  lvCnt += 2;
		case Reference:
		  lvCnt += 2;
		}
	      }

	      if(prefs.getDrawLinkRelationship()) 
		lvCnt += 2;
	    }
	  }

	  for(NodePath path : pDownstreamLinks.keySet()) {
	    ArrayList<Link> links = pDownstreamLinks.get(path);
	    lvCnt += 2*links.size() + 4;

	    if(prefs.getDrawArrowHeads())
	      tvCnt += 3*links.size();
	  }
	}
	
	/* create a new line array */ 
	LineArray la = new LineArray(lvCnt, LineArray.COORDINATES);
	int lvi = 0;

	/* create a new triagle array */ 
	TriangleArray ta = null;
	if(prefs.getDrawArrowHeads()) 
	  ta = new TriangleArray(tvCnt, TriangleArray.COORDINATES);
	int tvi = 0;
	
	/* upstream links */ 
	{
	  for(NodePath path : pUpstreamLinks.keySet()) {
	    ArrayList<Link> links = pUpstreamLinks.get(path);
	    
	    double centerX = 0.0;
	    double minY = 0.0;
	    double maxY = 0.0;
	    {
	      Link link = links.get(0);
	      Point3d tpos = link.getTargetPos();
	      Point3d spos = link.getSourcePos();
	      
	      centerX = tpos.x + (spos.x - tpos.x)*prefs.getLinkVerticalCrossbar();
	      minY = maxY = tpos.y;
	      
	      tpos.x += 0.5 + prefs.getLinkGap();

	      {
		NodeStatus status = link.getTargetNode().getNodeStatus(); 
		if(status != null) {
		  NodeDetails details = status.getDetails();
		  if(details != null) {
		    NodeCommon com = details.getWorkingVersion();
		    if(com == null) 
		      com = details.getLatestVersion();
		    if((com != null) &&  (com.getAction() != null) && 
		       (!com.isActionEnabled()) && (prefs.getDrawDisabledAction())) {
		      double s = prefs.getDisabledActionSize();
		      la.setCoordinate(lvi, new Point3d(tpos.x, tpos.y-s, 0.0));  lvi++;
		      la.setCoordinate(lvi, new Point3d(tpos.x, tpos.y+s, 0.0));  lvi++;
		    }
		  }
		}
	      }

	      if(prefs.getDrawArrowHeads()) {
		double hx = prefs.getArrowHeadLength();
		double hy = prefs.getArrowHeadWidth();

		ta.setCoordinate(tvi, tpos);                                    tvi++;
		ta.setCoordinate(tvi, new Point3d(tpos.x+hx, tpos.y-hy, 0.0));  tvi++;
		ta.setCoordinate(tvi, new Point3d(tpos.x+hx, tpos.y+hy, 0.0));  tvi++;

		tpos.x += prefs.getArrowHeadLength();
	      }

	      la.setCoordinate(lvi, tpos);                               lvi++;
	      la.setCoordinate(lvi, new Point3d(centerX, tpos.y, 0.0));  lvi++;
	    }
	    
	    for(Link link : links) {
	      Point3d tpos = link.getTargetPos();
	      Point3d spos = link.getSourcePos();
	      
	      minY = Math.min(minY, spos.y);
	      maxY = Math.max(maxY, spos.y);
	      
	      spos.x -= 0.5 + prefs.getLinkGap();

	      if(prefs.getDrawLinkRelationship()) {
		ViewerLinkRelationship vlink = 
		  pLinkRelationshipPool.addIcon(link.getLink(), link.getTargetNode());

		double sp = spos.x;
		if(prefs.getDrawLinkPolicy())
		  sp -= prefs.getLinkPolicySize()*0.75;

		double rc = (sp + centerX) * 0.5;

		Point2d p = new Point2d(rc, spos.y);
		vlink.setPosition(p);

		la.setCoordinate(lvi, new Point3d(centerX, spos.y, 0.0));  lvi++;
		la.setCoordinate(lvi, new Point3d(rc-0.25, spos.y, 0.0)); lvi++;

		la.setCoordinate(lvi, new Point3d(rc+0.25, spos.y, 0.0)); lvi++;
		la.setCoordinate(lvi, spos);                               lvi++;
	      }
	      else {
		la.setCoordinate(lvi, new Point3d(centerX, spos.y, 0.0));  lvi++;
		la.setCoordinate(lvi, spos);                               lvi++;
	      }

	      if(prefs.getDrawLinkPolicy()) {
		double s = prefs.getLinkPolicySize();
		switch(link.getLink().getPolicy()) {
		case Association:
		  la.setCoordinate(lvi, new Point3d(spos.x-s*0.75, spos.y-s, 0.0));  lvi++;
		  la.setCoordinate(lvi, new Point3d(spos.x-s*0.75, spos.y+s, 0.0));  lvi++;
		  
		case Reference:
		  la.setCoordinate(lvi, new Point3d(spos.x, spos.y-s, 0.0));  lvi++;
		  la.setCoordinate(lvi, new Point3d(spos.x, spos.y+s, 0.0));  lvi++;
		}
	      }
	    }
	    
	    la.setCoordinate(lvi, new Point3d(centerX, minY, 0.0));  lvi++;
	    la.setCoordinate(lvi, new Point3d(centerX, maxY, 0.0));  lvi++;
	  }
	}

	/* downstream links */ 
	{
	  for(NodePath path : pDownstreamLinks.keySet()) {
	    ArrayList<Link> links = pDownstreamLinks.get(path);
	    
	    double centerX = 0.0;
	    double minY = 0.0;
	    double maxY = 0.0;
	    {
	      Link link = links.get(0);
	      Point3d tpos = link.getTargetPos();
	      Point3d spos = link.getSourcePos();
	      
	      centerX = tpos.x + (spos.x - tpos.x)*prefs.getLinkVerticalCrossbar();
	      minY = maxY = spos.y;

	      spos.x -= 0.5 + prefs.getLinkGap();

	      la.setCoordinate(lvi, spos);                               lvi++;
	      la.setCoordinate(lvi, new Point3d(centerX, spos.y, 0.0));  lvi++;
	    }
	    
	    for(Link link : links) {
	      Point3d tpos = link.getTargetPos();
	      Point3d spos = link.getSourcePos();
	      
	      minY = Math.min(minY, tpos.y);
	      maxY = Math.max(maxY, tpos.y);
	      
	      tpos.x += 0.5 + prefs.getLinkGap();

	      if(prefs.getDrawArrowHeads()) {
		double hx = prefs.getArrowHeadLength();
		double hy = prefs.getArrowHeadWidth();

		ta.setCoordinate(tvi, tpos);                                    tvi++;
		ta.setCoordinate(tvi, new Point3d(tpos.x+hx, tpos.y-hy, 0.0));  tvi++;
		ta.setCoordinate(tvi, new Point3d(tpos.x+hx, tpos.y+hy, 0.0));  tvi++;

		tpos.x += prefs.getArrowHeadLength();
	      }

	      la.setCoordinate(lvi, new Point3d(centerX, tpos.y, 0.0));  lvi++;
	      la.setCoordinate(lvi, tpos);                               lvi++;
	    }

	    la.setCoordinate(lvi, new Point3d(centerX, minY, 0.0));  lvi++;
	    la.setCoordinate(lvi, new Point3d(centerX, maxY, 0.0));  lvi++;
	  }
	}

	assert(lvi == lvCnt);
	pLines.setGeometry(la);
	pLineSwitch.setWhichChild(Switch.CHILD_ALL);

	if(prefs.getDrawArrowHeads()) {
	  assert(tvi == tvCnt);
	  pTris.setGeometry(ta);
	  pTriSwitch.setWhichChild(Switch.CHILD_ALL);	
	}
	else {
	  pTriSwitch.setWhichChild(Switch.CHILD_NONE);	
	}
      }
	
      pLinkRelationshipPool.update();      

      pLinksChanged = false;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  private Point3d
  to3d
  (
   Point2d p
  ) 
  {
    return new Point3d(p.x, p.y, 0.0);
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

    public ViewerNode
    getTargetNode()
    {
      return pTarget;
    }

    public Point3d
    getTargetPos() 
    {
      return to3d(pTarget.getPosition());
    }

    public Point3d
    getSourcePos()
    {
      return to3d(pSource.getPosition());
    }
    
    public LinkCommon
    getLink() 
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
   * Whether the line appearance has been changed.
   */ 
  private boolean  pAppearanceChanged; 

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
   * The switch group used to control line visbilty. 
   */ 
  private Switch  pLineSwitch;   

  /**
   * The line geometry.
   */ 
  private Shape3D  pLines;  


  /**
   * The switch group used to control triangle visbilty. 
   */ 
  private Switch  pTriSwitch;   

  /**
   * The triangle geometry.
   */ 
  private Shape3D  pTris;  


  /**
   * The reusable set of link relationship icons.
   */ 
  private ViewerLinkRelationshipPool  pLinkRelationshipPool;

}
