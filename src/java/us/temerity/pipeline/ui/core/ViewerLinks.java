// $Id: ViewerLinks.java,v 1.7 2007/06/21 16:40:50 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import javax.media.opengl.*;

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
    pUpstreamLinks   = new HashMap<NodePath,ArrayList<Link>>();
    pDownstreamLinks = new HashMap<NodePath,ArrayList<Link>>();

    pLinkRels = new LinkedList<ViewerLinkRelationship>();

    pLinksDL = new AtomicInteger(0);
    pRefresh = true;
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
   * 
   * @param isStale
   *   Whether the link propogates staleness.
   */ 
  public void
  addUpstreamLink
  (
   ViewerNode target, 
   ViewerNode source, 
   LinkCommon link, 
   boolean isStale
  ) 
  {
    NodePath path = target.getNodePath();
    ArrayList<Link> links = pUpstreamLinks.get(path);
    if(links == null) {
      links = new ArrayList<Link>();
      pUpstreamLinks.put(path, links);
    }
    links.add(new Link(target, source, link, isStale));    
    pRefresh = true;
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
    links.add(new Link(target, source, null, false));       
    pRefresh = true;
  }

  /**
   * Remove all links.
   */ 
  public void 
  clear() 
  {
    pUpstreamLinks.clear();
    pDownstreamLinks.clear();
    pRefresh = true;    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P I C K I N G                                                                        */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Get the link relationship icon under the given mouse position.
   * 
   * @param
   *   The pick position.
   * 
   * @return 
   *   The link relationship icon or <CODE>null</CODE> if none are under the given position.
   */ 
  public ViewerLinkRelationship
  pickLinkRelationship
  (
   Point2d pos
  ) 
  {
    for(ViewerLinkRelationship vrel : pLinkRels) {
      if(vrel.isInside(pos)) 
	return vrel;
    }
    return null;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   R E N D E R I N G                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Rebuild any OpenGL display list needd to render the node.
   *
   * @param gl
   *   The OpenGL interface.
   */ 
  public void 
  rebuild
  (
   GL gl
  )
  {
    if(pRefresh) { 
      UserPrefs prefs = UserPrefs.getInstance();

      /* get the link relationship icon display lists */ 
      try {
	GeometryMgr mgr = GeometryMgr.getInstance();
	if(prefs.getDrawLinkRelationship() && (pLinkRelDLs == null)) {
	  pLinkRelDLs = new int[3];
	  for(LinkRelationship rel : LinkRelationship.all()) 
	    pLinkRelDLs[rel.ordinal()] = mgr.getLinkRelationshipDL(gl, rel);
	}
      }
      catch(IOException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Tex, LogMgr.Level.Severe,
	   ex.getMessage());
	pLinkRelDLs = null;
      }
	
      /* rebuild the link geometry */ 
      {
	UIMaster master = UIMaster.getInstance(); 
	master.freeDisplayList(pLinksDL.getAndSet(master.getDisplayList(gl)));
      }

      gl.glNewList(pLinksDL.get(), GL.GL_COMPILE);
      {
	pLinkRels.clear();

	Color3d color = prefs.getLinkColor();
	Color3d stale = prefs.getStaleLinkColor();

	gl.glLineWidth((float) prefs.getLinkThickness());

 	/* upstream links */ 
	if(!pUpstreamLinks.isEmpty()) {
	  LinkedList<Point2d[]> arrows = new LinkedList<Point2d[]>();
	  LinkedList<Point2d[]> sarrows = new LinkedList<Point2d[]>();

	  EnumMap<LinkRelationship,LinkedList<ViewerLinkRelationship>> linkRels = 
	    new EnumMap<LinkRelationship,
	                LinkedList<ViewerLinkRelationship>>(LinkRelationship.class);

	  for(LinkRelationship rel : LinkRelationship.all()) 
	    linkRels.put(rel, new LinkedList<ViewerLinkRelationship>());
	  
	  gl.glEnable(GL.GL_LINE_SMOOTH); 
	  gl.glBegin(GL.GL_LINES);
	  {
	    for(NodePath path : pUpstreamLinks.keySet()) {
	      ArrayList<Link> links = pUpstreamLinks.get(path);

	      boolean anyStale = false;
	      for(Link link : links) {
		if(link.isStale()) {
		  anyStale = true;
		  break;
		}
	      }

	      double centerX = 0.0;
	      double minY = 0.0;
	      double maxY = 0.0;
	      double targetY = 0.0;
	      {
		Link link = links.get(0);
		Point2d tpos = link.getTargetPos();
		Point2d spos = link.getSourcePos();
		
		centerX = tpos.x() + (spos.x() - tpos.x())*prefs.getLinkVerticalCrossbar();
		minY = maxY = tpos.y();
		
		tpos.x(tpos.x() + (0.5 + prefs.getLinkGap()));
		
		targetY = tpos.y();
		
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
			
			Point2d a = new Point2d(tpos.x(), tpos.y()-s);
			Point2d b = new Point2d(tpos.x(), tpos.y()+s);
			
			if(anyStale) 
			  gl.glColor3d(stale.r(), stale.g(), stale.b());
			else 
			  gl.glColor3d(color.r(), color.g(), color.b());
			  
			gl.glVertex2d(a.x(), a.y());
			gl.glVertex2d(b.x(), b.y());
		      }
		    }
		  }
		}

		if(prefs.getDrawArrowHeads()) {
		  double hx = prefs.getArrowHeadLength();
		  double hy = prefs.getArrowHeadWidth();
		
		  Point2d top = new Point2d(tpos.x()+hx, tpos.y()-hy);
		  Point2d btm = new Point2d(tpos.x()+hx, tpos.y()+hy);
		  
		  Point2d verts[] = new Point2d[3];
		  verts[0] = new Point2d(tpos.x(), tpos.y());
		  verts[1] = new Point2d(top.x(), top.y());
		  verts[2] = new Point2d(btm.x(), btm.y());

		  if(anyStale) 
		    sarrows.add(verts);
		  else 
		    arrows.add(verts);

		  tpos.x(tpos.x() + prefs.getArrowHeadLength());
		}
		
		{
		  Point2d a = new Point2d(centerX, tpos.y());
		  
		  if(anyStale) 
		    gl.glColor3d(stale.r(), stale.g(), stale.b());
		  else 
		    gl.glColor3d(color.r(), color.g(), color.b());
		  
		  gl.glVertex2d(tpos.x(), tpos.y());
		  gl.glVertex2d(a.x(), a.y());
		}
	      }
	      
	      double minStaleY = targetY;
	      double maxStaleY = targetY;
	      for(Link link : links) {
		Point2d tpos = link.getTargetPos();
		Point2d spos = link.getSourcePos();
		
		minY = Math.min(minY, spos.y());
		maxY = Math.max(maxY, spos.y());
		
		spos.x(spos.x() - (0.5 + prefs.getLinkGap()));
		
		if(link.isStale()) {
		  minStaleY = Math.min(minStaleY, spos.y());
		  maxStaleY = Math.max(maxStaleY, spos.y());
		}
		
		if(prefs.getDrawLinkRelationship()) {
		  double sp = spos.x();
		  if(prefs.getDrawLinkPolicy())
		    sp -= prefs.getLinkPolicySize()*0.75;
		
		  double rc = (sp + centerX) * 0.5;
		  
		  {
		    LinkedList<ViewerLinkRelationship> vrels = 
		      linkRels.get(link.getLink().getRelationship());

		    ViewerLinkRelationship vrel = 
		      new ViewerLinkRelationship(link.getLink(), link.getTargetNode(), 
						 new Point2d(rc, spos.y()));
		    vrels.add(vrel);
		  }

		  Point2d a = new Point2d(centerX, spos.y());
		  Point2d b = new Point2d(rc-0.25, spos.y());
		  Point2d c = new Point2d(rc+0.25, spos.y());

		  if(link.isStale()) 
		    gl.glColor3d(stale.r(), stale.g(), stale.b());
		  else 
		    gl.glColor3d(color.r(), color.g(), color.b());
		  
		  gl.glVertex2d(a.x(), a.y());
		  gl.glVertex2d(b.x(), b.y());

		  gl.glVertex2d(c.x(), c.y());
		  gl.glVertex2d(spos.x(), spos.y());
		}
		else {
		  Point2d a = new Point2d(centerX, spos.y());
		  
		  if(link.isStale()) 
		    gl.glColor3d(stale.r(), stale.g(), stale.b());
		  else 
		    gl.glColor3d(color.r(), color.g(), color.b());

		  gl.glVertex2d(a.x(), a.y());
		  gl.glVertex2d(spos.x(), spos.y());
		}

		if(prefs.getDrawLinkPolicy()) {
		  double s = prefs.getLinkPolicySize();
		  switch(link.getLink().getPolicy()) {
                  case Association:
		  case Reference:
		    {
		      Point2d a = new Point2d(spos.x(), spos.y()-s);
		      Point2d b = new Point2d(spos.x(), spos.y()+s);

		      if(link.isStale()) 
			gl.glColor3d(stale.r(), stale.g(), stale.b());
		      else 
			gl.glColor3d(color.r(), color.g(), color.b());

		      gl.glVertex2d(a.x(), a.y());
		      gl.glVertex2d(b.x(), b.y());

                      switch(link.getLink().getPolicy()) {
                      case Association:
                        {
                          double sx = a.x() - prefs.getLinkGap();
                          gl.glVertex2d(sx, a.y());
                          gl.glVertex2d(sx, b.y());
                        }
                      }
		    }
		  }
		}
	      }
	    
	      {
		Point2d a = new Point2d(centerX, minY);
		Point2d b = new Point2d(centerX, minStaleY);
		Point2d c = new Point2d(centerX, targetY);
	      
		if(minStaleY < targetY) {
		  gl.glColor3d(stale.r(), stale.g(), stale.b());
		  
		  gl.glVertex2d(b.x(), b.y());
		  gl.glVertex2d(c.x(), c.y());
	      
		  if(minY < minStaleY) {
		    gl.glColor3d(color.r(), color.g(), color.b());

		    gl.glVertex2d(a.x(), a.y());
		    gl.glVertex2d(b.x(), b.y());
		  }
		}
		else {
		  gl.glColor3d(color.r(), color.g(), color.b());
		  
		  gl.glVertex2d(a.x(), a.y());
		  gl.glVertex2d(c.x(), c.y());
		}
	      }

	      {
		Point2d a = new Point2d(centerX, maxY);
		Point2d b = new Point2d(centerX, maxStaleY);
		Point2d c = new Point2d(centerX, targetY);

		if(maxStaleY > targetY) {
		  gl.glColor3d(stale.r(), stale.g(), stale.b());
		  
		  gl.glVertex2d(b.x(), b.y());
		  gl.glVertex2d(c.x(), c.y());
	      
		  if(maxY > maxStaleY) {
		    gl.glColor3d(color.r(), color.g(), color.b());

		    gl.glVertex2d(a.x(), a.y());
		    gl.glVertex2d(b.x(), b.y());
		  }
		}
		else {
		  gl.glColor3d(color.r(), color.g(), color.b());
		  
		  gl.glVertex2d(a.x(), a.y());
		  gl.glVertex2d(c.x(), c.y());
		}
	      }
	    }
	  }
	  gl.glEnd();
	  gl.glDisable(GL.GL_LINE_SMOOTH);

	  if(!arrows.isEmpty() || !sarrows.isEmpty()) {
	    gl.glEnable(GL.GL_POLYGON_SMOOTH);
	    gl.glBegin(GL.GL_TRIANGLES);
	    {
	      gl.glColor3d(color.r(), color.g(), color.b());
		
	      for(Point2d verts[] : arrows) {
		gl.glVertex2d(verts[0].x(), verts[0].y());
		gl.glVertex2d(verts[1].x(), verts[1].y());
		gl.glVertex2d(verts[2].x(), verts[2].y());
	      }

	      gl.glColor3d(stale.r(), stale.g(), stale.b());
		
	      for(Point2d verts[] : sarrows) {
		gl.glVertex2d(verts[0].x(), verts[0].y());
		gl.glVertex2d(verts[1].x(), verts[1].y());
		gl.glVertex2d(verts[2].x(), verts[2].y());
	      }
	    }
	    gl.glEnd();
	    gl.glDisable(GL.GL_POLYGON_SMOOTH);
	  }

	  for(LinkRelationship rel : linkRels.keySet()) {
	    int dl = pLinkRelDLs[rel.ordinal()];
	    for(ViewerLinkRelationship vrel : linkRels.get(rel)) {
	      pLinkRels.add(vrel);

	      gl.glPushMatrix();
	      {
		Point2d p = vrel.getPosition();
		gl.glTranslated(p.x(), p.y(), 0.0);
		gl.glCallList(dl);
	      }
	      gl.glPopMatrix();	      
	    }
	  }
	}

	/* downstream links */ 
	if(!pDownstreamLinks.isEmpty()) {
	  LinkedList<Point2d[]> arrows = new LinkedList<Point2d[]>();
	  
	  gl.glEnable(GL.GL_LINE_SMOOTH); 
	  gl.glBegin(GL.GL_LINES);
	  {
	    gl.glColor3d(color.r(), color.g(), color.b());
	    
	    for(NodePath path : pDownstreamLinks.keySet()) {
	      ArrayList<Link> links = pDownstreamLinks.get(path);
	      
	      double centerX = 0.0;
	      double minY = 0.0;
	      double maxY = 0.0;
	      {
		Link link = links.get(0);
		Point2d tpos = link.getTargetPos();
		Point2d spos = link.getSourcePos();
		
		centerX = tpos.x() + (spos.x() - tpos.x())*prefs.getLinkVerticalCrossbar();
		minY = maxY = spos.y();
		
		spos.x(spos.x() - (0.5 + prefs.getLinkGap()));
		
		gl.glVertex2d(spos.x(), spos.y());
		gl.glVertex2d(centerX, spos.y());
	      }
	      
	      for(Link link : links) {
		Point2d tpos = link.getTargetPos();
		Point2d spos = link.getSourcePos();
		
		minY = Math.min(minY, tpos.y());
		maxY = Math.max(maxY, tpos.y());
		
		tpos.x(tpos.x() + (0.5 + prefs.getLinkGap()));
		
		if(prefs.getDrawArrowHeads()) {
		  double hx = prefs.getArrowHeadLength();
		  double hy = prefs.getArrowHeadWidth();
		  
		  Point2d verts[] = new Point2d[3];
		  verts[0] = new Point2d(tpos);
		  verts[1] = new Point2d(tpos.x()+hx, tpos.y()-hy);
		  verts[2] = new Point2d(tpos.x()+hx, tpos.y()+hy);
		  arrows.add(verts);
		  
		  tpos.x(tpos.x() + prefs.getArrowHeadLength());
		}
		
		gl.glVertex2d(centerX, tpos.y());
		gl.glVertex2d(tpos.x(), tpos.y());
	      }
	      
	      gl.glVertex2d(centerX, minY);
	      gl.glVertex2d(centerX, maxY);
	    }
	  }
	  gl.glEnd();
	  gl.glDisable(GL.GL_LINE_SMOOTH);
	 
	  if(!arrows.isEmpty()) {
	    gl.glEnable(GL.GL_POLYGON_SMOOTH);
	    gl.glBegin(GL.GL_TRIANGLES);
	    {
	      gl.glColor3d(color.r(), color.g(), color.b());
		
	      for(Point2d verts[] : arrows) {
		gl.glVertex2d(verts[0].x(), verts[0].y());
		gl.glVertex2d(verts[1].x(), verts[1].y());
		gl.glVertex2d(verts[2].x(), verts[2].y());
	      }
	    }
	    gl.glEnd();
	    gl.glDisable(GL.GL_POLYGON_SMOOTH);
	  }
	}
      }
      gl.glEndList();
    }
  }
   
  /**
   * Render the OpenGL geometry for the node.
   *
   * @param gl
   *   The OpenGL interface.
   */ 
  public void 
  render
  (
   GL gl
  )
  {
    gl.glCallList(pLinksDL.get());
  }
  
  /**
   * Return the previously allocated OpenGL display lists to the pool of display lists to be 
   * reused. 
   */ 
  public void 
  freeDisplayLists() 
  {
    UIMaster.getInstance().freeDisplayList(pLinksDL.getAndSet(0));
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
     LinkCommon details, 
     boolean isStale
    ) 
    {
      pTarget  = target;
      pSource  = source;
      pDetails = details;
      pIsStale = isStale;
    }

    public ViewerNode
    getTargetNode()
    {
      return pTarget;
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
    getLink() 
    {
      return pDetails;
    }

    public boolean
    isStale() 
    {
      return pIsStale;
    }

    private ViewerNode pTarget; 
    private ViewerNode pSource; 
    private LinkCommon pDetails; 
    private boolean    pIsStale; 
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
   * The rendered link relationship icons.
   */ 
  private LinkedList<ViewerLinkRelationship>  pLinkRels; 


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The OpenGL display list handle for the links geometry.
   */ 
  private AtomicInteger  pLinksDL; 

  /**
   * The OpenGL display list handles for the link relationship icon geometry.
   */ 
  private int[] pLinkRelDLs;

  /**
   * Whether the OpenGL display list for the links geometry needs to be rebuilt.
   */ 
  private boolean  pRefresh;

}
