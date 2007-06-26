// $Id: ViewerNode.java,v 1.13 2007/06/26 18:23:32 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;

import java.io.*;
import java.util.*;

import javax.media.opengl.*;

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

    NodeDetails details = pStatus.getDetails();
    if(details != null) {
      NodeMod mod = details.getWorkingVersion();
      pIsLocked = ((mod != null) && mod.isLocked());
    }
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
	    Path path = new Path(pStatus.getNodeID().getName());
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
	      Path path = new Path(pStatus.getNodeID().getName());
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
	  pLabelDLs[0] = mgr.getTextDL(gl, PackageInfo.sGLFont, text1, 
				       GeometryMgr.TextAlignment.Center, 0.05);

	if(text2 != null) 
	  pLabelDLs[1] = mgr.getTextDL(gl, PackageInfo.sGLFont, text2, 
				       GeometryMgr.TextAlignment.Center, 0.05);
      }

      if(pNodeStateDL == null) {
        NodeDetails details = pStatus.getDetails();
        if(details != null) {
          String nstate = null;
          if(details.isLightweight()) {
            if(details.getVersionState() == VersionState.CheckedIn) 
              nstate = "Node-CheckedIn";
          }
          else if(details.getOverallNodeState() == OverallNodeState.NeedsCheckOut) {
            VersionID wvid = details.getWorkingVersion().getWorkingID();
            VersionID lvid = details.getLatestVersion().getVersionID();
            switch(wvid.compareLevel(lvid)) {
            case Major:
              nstate = "Node-NeedsCheckOutMajor";
              break;
              
            case Minor:
              nstate = "Node-NeedsCheckOut";
              break;
              
            case Micro:
              nstate = "Node-NeedsCheckOutMicro";
            }
          }
          else {
            nstate = ("Node-" + details.getOverallNodeState()); 
          }
        
          if(nstate != null) 
            pNodeStateDL = mgr.getIconDL(gl, nstate);
        }
      }
      
      if(pRingDL == null) 
        pRingDL = mgr.getIconDL(gl, "Node-Ring");

      if(pCoreDL == null) 
        pCoreDL = mgr.getIconDL(gl, "Node-Core");

      if(pCollapsedDL == null) 
	pCollapsedDL = mgr.getNodeIconDL(gl, "Collapsed");

      if(pLockedDL == null) 
	pLockedDL = mgr.getNodeIconDL(gl, "Locked");
    }
    catch(PipelineException ex) {
      pLabelDLs    = null;
      pNodeStateDL = null;
      pRingDL      = null;
      pCoreDL      = null;
      pCollapsedDL = null;
      pLockedDL    = null;

      LogMgr.getInstance().log
	(LogMgr.Kind.Tex, LogMgr.Level.Severe,
	 ex.getMessage());
      LogMgr.getInstance().flush(); 
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
    UserPrefs prefs = UserPrefs.getInstance();

    Color3d white = new Color3d(1.0, 1.0, 1.0);

    Color3d modeColor  = NodeStyles.getSelectionColor3d(pMode);
    Color3d queueColor = prefs.getUndefinedCoreColor(); 
    Color3d iconColor  = prefs.getModifiableColor(); 
    {
      NodeDetails details = pStatus.getDetails();
      if(details != null) {
        if(details.isLightweight()) {
          if(details.getVersionState() != VersionState.CheckedIn) 
            queueColor = prefs.getLightweightCoreColor(); 
        }
        else {
          queueColor = NodeStyles.getQueueColor3d(details.getOverallQueueState());

          NodeMod mod = details.getWorkingVersion();
          if((mod != null) && mod.isFrozen()) {
            switch(details.getOverallQueueState()) {
            case Finished: 
              iconColor = prefs.getFrozenFinishedColor(); 
              break;

            case Stale: 
              iconColor = prefs.getFrozenStaleColor(); 
            }
          }
        }
      }
    }

    gl.glPushMatrix();
    {
      gl.glTranslated(pPos.x(), pPos.y(), 0.0);

      /* the selection ring */
      if(pRingDL != null) {
        gl.glColor3d(modeColor.r(), modeColor.g(), modeColor.b());
        gl.glCallList(pRingDL); 
      }

      /* the queue state colored node core */ 
      if(pCoreDL != null) {
        gl.glColor3d(queueColor.r(), queueColor.g(), queueColor.b());
        gl.glCallList(pCoreDL); 
      }   
      
      /* the node state icon */ 
      if(pNodeStateDL != null) {
        gl.glColor3d(iconColor.r(), iconColor.g(), iconColor.b());
        gl.glCallList(pNodeStateDL); 
      }   

      {
	NodeDetails details = pStatus.getDetails();

        /* extra symbols to the right of the node */ 
	if(pIsLocked && (pLockedDL != null)) {
	  gl.glTranslated(0.7, 0.0, 0.0);
	  gl.glCallList(pLockedDL);
	  gl.glTranslated(-0.7, 0.0, 0.0);
	}
	else if(pIsCollapsed && (pCollapsedDL != null)) {
	  double dx = (details == null) ? -0.8 : 0.8;
	  gl.glTranslated(dx, 0.0, 0.0);
	  gl.glCallList(pCollapsedDL);
	  gl.glTranslated(-dx, 0.0, 0.0);
	}

        /* the disabled action vertical bar */ 
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

      /* the node label */ 
      {
        gl.glColor3d(modeColor.r(), modeColor.g(), modeColor.b());
	
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
   * Whether to display the locked icon. 
   */
  protected boolean  pIsLocked;


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The OpenGL display list handle(s) for the label geometry.
   */ 
  private int[]  pLabelDLs; 

  /**
   * The OpenGL display list handle for the geometry of the selection ring around 
   * the outside of the node.
   */ 
  private Integer  pRingDL; 

  /**
   * The OpenGL display list handle for the geometry of the queue state colored 
   * core of the node.
   */ 
  private Integer  pCoreDL; 

  /**
   * The OpenGL display list handle for the node state symbols rendered on top of the 
   * colored core of the node.
   */ 
  private Integer  pNodeStateDL; 
  
  /**
   * The OpenGL display list handle for the collapsed icon geometry.
   */ 
  private Integer  pCollapsedDL; 

  /**
   * The OpenGL display list handle for the locked icon geometry.
   */ 
  private Integer  pLockedDL; 

}
