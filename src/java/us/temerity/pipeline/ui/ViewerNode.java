// $Id: ViewerNode.java,v 1.14 2004/12/30 02:47:58 jim Exp $

package us.temerity.pipeline.ui;

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
    if(path == null) 
      throw new IllegalArgumentException("The node path cannot be (null)!");
    pPath = path;

    if(status == null) 
      throw new IllegalArgumentException("The node status cannot be (null)!");
    pStatus = status; 

    pMode = SelectionMode.Normal;

    pPos = new Point2d();
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
   * Whether the upstream/downstream nodes are collapsed.
   */ 
  public boolean 
  isCollapsed() 
  {
    return pIsCollapsed;
  }

  /**
   * Set whether the upstream/downstream nodes are collapsed.
   */ 
  public void 
  setCollapsed
  (
   boolean tf
  ) 
  {
    pIsCollapsed = tf;
  }

  /**
   * Toggle whether the upstream/downstream nodes are collapsed.
   */ 
  public void 
  toggleCollapsed() 
  {
    pIsCollapsed = !pIsCollapsed;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current selection mode.
   */ 
  public SelectionMode
  getSelectionMode() 
  {
    return pMode;
  }

  /**
   * Set the current selection mode.
   */ 
  public void 
  setSelectionMode
  (
   SelectionMode mode
  ) 
  {
    pMode = mode;
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

  /** 
   * Get the 2D position of the node (center of icon).
   */ 
  public Point2d
  getPosition()
  {
    return new Point2d(pPos);
  }

  /**
   * Set the 2D position of the node (center of icon). 
   */
  public void 
  setPosition
  (
   Point2d pos    
  ) 
  {
    pPos.set(pos);
  }

  /**
   * Move the node position by the given amount.
   */ 
  public void 
  movePosition
  (
   Vector2d delta
  ) 
  {
    pPos.add(delta);
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
      Logs.tex.severe(ex.getMessage());
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
      
      if(pIsCollapsed) {
	double dx = (pStatus.getDetails() == null) ? -0.8 : 0.8;
	gl.glTranslated(dx, 0.0, 0.0);
	gl.glCallList(pCollapsedDL);
	gl.glTranslated(-dx, 0.0, 0.0);
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

  /**
   * The UI selection mode.
   */
  private SelectionMode  pMode;  

  /**
   * Whether upstream/downstream nodes are collapsed.
   */
  private boolean  pIsCollapsed;


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The 2D position of the node (center of icon).
   */ 
  private Point2d  pPos;         

  /**
   * The OpenGL display list handle(s) for the label geometry.
   */ 
  private int  pLabelDLs[]; 

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
