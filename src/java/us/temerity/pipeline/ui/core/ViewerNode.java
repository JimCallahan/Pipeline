// $Id: ViewerNode.java,v 1.4 2005/02/09 18:22:33 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;

import java.io.*;
import java.util.*;

import net.java.games.jogl.*;

/*------------------------------------------------------------------------------------------*/
/*   V I E W E R   N O D E                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Renders the current status of a node as OpenGL geometry. <P> 
 * 
 * The rendered node is composed of textured quads on the XY plane including a node icon, 
 * node label and optionally a collapsed upstream/downstream icon.  This class also maintains 
 * the 2D layout position and the collapsed/expanded state of this geometry. <P> 
 * 
 * A <CODE>ViewerNode</CODE> is associated with a single <CODE>NodeStatus</CODE>.  However, 
 * a <CODE>NodeStatus</CODE> may be represented by more than one <CODE>ViewerNode</CODE>
 * if the node is reachable via multiple upstream/downstream paths.  For this reason, each
 * instance of this class maintains a {@link NodePath NodePath} field in addition to the 
 * current <CODE>NodeStatus</CODE> to identify the unique path from the root node of the 
 * {@link JNodeViewerPanel JNodeViewerPanel} to the <CODE>NodeStatus</CODE> associated
 * with this <CODE>ViewerNode</CODE> instance.
 */
public 
class ViewerNode
  extends ViewerIcon
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constuct a new viewer node. 
   * 
   * @param path
   *   The path from the root node to this node.
   * 
   * @param status
   *   The current node status.
   * 
   * @param isCollapsed
   *   Whether the collapsed upstream/downstream icon should be displayed.
   */ 
  public 
  ViewerNode
  (
   NodeStatus status, 
   NodePath path
  ) 
  {
    super();

    if(path == null) 
      throw new IllegalArgumentException("The node path cannot be (null)!");
    pPath = path;

    if(status == null) 
      throw new IllegalArgumentException("The node status cannot be (null)!");
    pStatus = status; 
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current node status. 
   */ 
  public NodeStatus
  getNodeStatus() 
  {
    return pStatus;
  }

  /**
   * Get the path from the root node to the current node.
   */ 
  public NodePath
  getNodePath() 
  {
    return pPath;
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the given position is inside the node icon.
   */ 
  public boolean
  isInside
  (
   Point2d pos
  ) 
  {              
    return (pPos.distanceSquared(pos) < 0.2025);
  }

  /**
   * Whether any portion of the node icon is inside the given bounding box.
   */ 
  public boolean
  isInsideOf
  (
   BBox2d bbox
  ) 
  {
    Point2d minC = bbox.getMin();
    Point2d maxC = bbox.getMax();

    if(pPos.x() < minC.x()) {
      if(pPos.y() < minC.y()) 
	return isInside(minC);
      else if(pPos.y() > maxC.y()) 
	return isInside(new Point2d(minC.x(), maxC.y()));
      else 
	return ((minC.x() - pPos.x()) < 0.45);
    }
    else if(pPos.x() > maxC.x()) {
      if(pPos.y() < minC.y()) 
	return isInside(new Point2d(maxC.x(), minC.y()));
      else if(pPos.y() > maxC.y()) 
	return isInside(maxC);
      else 
	return ((pPos.x() - maxC.x()) < 0.45);
    }
    else {
      if(pPos.y() < minC.y()) 
	return ((minC.y() - pPos.y()) < 0.45);
      else if(pPos.y() > maxC.y()) 
	return ((pPos.y() - maxC.y()) < 0.45);
      else 
	return true;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   R E N D E R I N G                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Rebuild any OpenGL display list needed to render the node.
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
    GeometryMgr mgr = GeometryMgr.getInstance();
    try {
      if(pLabelDLs == null) {
	String text1 = null;
	String text2 = null;
	{
	  UserPrefs prefs = UserPrefs.getInstance();
	  String style = prefs.getNodeLabelStyle();
	  if(style.equals("Name Only")) {
	    File path = new File(pStatus.getNodeID().getName());
	    text1 = path.getName();
	    pLabelDLs = new int[1];
	  }
	  else if(style.equals("Pattern & Range")) {
	    text1 = pStatus.toString();
	    pLabelDLs = new int[1];
	  }
	  else if(style.equals("Pattern & Range Below")) {
	    NodeDetails details = pStatus.getDetails();
	    if(details == null) {
	      File path = new File(pStatus.getNodeID().getName());
	      text1 = path.getName();
	    }
	    else {
	      NodeCommon com = details.getWorkingVersion();
	      if(com == null) 
		com = details.getLatestVersion();

	      FileSeq fseq = com.getPrimarySequence();
	      if(fseq.isSingle()) 
		text1 = fseq.toString();
	      else if(fseq.hasFrameNumbers()) {
		text1 = fseq.getFilePattern().toString();
		text2 = fseq.getFrameRange().toString();
	      }
	    }

	    pLabelDLs = new int[(text2 != null) ? 2 : 1];
	  }
	  else {
	    pLabelDLs = new int[0];
	  }
	}

	if(text1 != null) 
	  pLabelDLs[0] = mgr.getTextDL(gl, "CharterBTRoman", text1, 
				       GeometryMgr.TextAlignment.Center, 0.05);

	if(text2 != null) 
	  pLabelDLs[1] = mgr.getTextDL(gl, "CharterBTRoman", text2, 
				       GeometryMgr.TextAlignment.Center, 0.05);
      }

      if(pIconDL == null) {
	String name = "Blank";
	NodeDetails details = pStatus.getDetails();
	if(details != null) {
	  if(details.getOverallNodeState() == OverallNodeState.NeedsCheckOut) {
	    VersionID wvid = details.getWorkingVersion().getWorkingID();
	    VersionID lvid = details.getLatestVersion().getVersionID();
	    switch(wvid.compareLevel(lvid)) {
	    case Major:
	      name = ("NeedsCheckOutMajor-" + details.getOverallQueueState());
	      break;
	      
	    case Minor:
	      name = ("NeedsCheckOut-" + details.getOverallQueueState());
	      break;
	      
	    case Micro:
	      name = ("NeedsCheckOutMicro-" + details.getOverallQueueState());
	    }
	  }
	  else {
	    name = (details.getOverallNodeState() + "-" + details.getOverallQueueState());
	  }
	  
	  NodeMod mod = details.getWorkingVersion();
	  if((mod != null) && mod.isFrozen()) 
	    name = (name + "-Frozen");
	}

	pIconDL = new int[3];
	for(SelectionMode mode : SelectionMode.all()) 
	  pIconDL[mode.ordinal()] = mgr.getNodeIconDL(gl, name + "-" + mode);
      }
      
      if(pCollapsedDL == null) 
	pCollapsedDL = mgr.getNodeIconDL(gl, "Collapsed");
    }
    catch(IOException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Tex, LogMgr.Level.Severe,
	 ex.getMessage());
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
    gl.glPushMatrix();
    {
      gl.glTranslated(pPos.x(), pPos.y(), 0.0);
      gl.glCallList(pIconDL[pMode.ordinal()]);

      {
	NodeDetails details = pStatus.getDetails();

	if(pIsCollapsed) {
	  double dx = (details == null) ? -0.8 : 0.8;
	  gl.glTranslated(dx, 0.0, 0.0);
	  gl.glCallList(pCollapsedDL);
	  gl.glTranslated(-dx, 0.0, 0.0);
	}

	if(details != null) {
	  NodeCommon com = null;
	  boolean hasSources = false;
	  {
	    NodeMod mod = details.getWorkingVersion();
	    if(mod != null) {
	      com = mod;
	      hasSources = mod.hasSources();
	    }
	    else {
	      NodeVersion vsn = details.getLatestVersion();
	      if(vsn != null) {
		com = vsn;
		hasSources = vsn.hasSources();
	      }
	    }
	  }

	  UserPrefs prefs = UserPrefs.getInstance();
	  if((com != null) &&  (com.getAction() != null) && 
	     (!com.isActionEnabled()) && (prefs.getDrawDisabledAction()) && 
	     (pIsCollapsed || !hasSources)) {
	    
	    gl.glBegin(gl.GL_LINES);
	    {
	      Color3d color = prefs.getLinkColor();
	      gl.glColor3d(color.r(), color.g(), color.b());

	      double s = prefs.getDisabledActionSize();
	      gl.glVertex2d(0.55, -s);
	      gl.glVertex2d(0.55,  s);
	    }
	    gl.glEnd();
	  }
	}
      }

      {
	switch(pMode) {
	case Normal:
	  gl.glColor3d(1.0, 1.0, 1.0);
	  break;
	  
	case Selected:
	  gl.glColor3d(1.0, 1.0, 0.0);
	  break;
	  
	case Primary:
	  gl.glColor3d(0.0, 1.0, 1.0);
	}	
	
	switch(pLabelDLs.length) {
	case 1:
	  {
	    gl.glTranslated(0.0, 0.55, 0.0);
	    gl.glScaled(0.35, 0.35, 0.35);
	    gl.glCallList(pLabelDLs[0]);
	  }
	  break;

	case 2:
	  {
	    gl.glPushMatrix();
	    {
	      gl.glTranslated(0.0, 1.0, 0.0);
	      gl.glScaled(0.35, 0.35, 0.35);
	      gl.glCallList(pLabelDLs[0]);
	    }
	    gl.glPopMatrix();
	    
	    gl.glTranslated(0.0, 0.55, 0.0);
	    gl.glScaled(0.35, 0.35, 0.35);
	    gl.glCallList(pLabelDLs[1]);
	  }
	}
      }
    }
    gl.glPopMatrix();
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The path from the root node to the current node.
   */ 
  private NodePath  pPath;

  /**
   * The current node status.
   */ 
  private NodeStatus  pStatus;


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The OpenGL display list handle(s) for the label geometry.
   */ 
  private int[]  pLabelDLs; 

  /**
   * The OpenGL display list handles for the icon geometry. <P> 
   * 
   * The array contains the display lists corresponding to the Normal, Selected and Primary
   * selection modes.
   */ 
  private int[]  pIconDL; 
  
  /**
   * The OpenGL display list handle for the collapsed icon geometry.
   */ 
  private Integer  pCollapsedDL; 

}
