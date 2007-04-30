// $Id: ViewerNodeHint.java,v 1.8 2007/04/30 08:19:10 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;

import java.io.*;
import java.util.*;

import javax.media.opengl.*;

/*------------------------------------------------------------------------------------------*/
/*   V I E W E R   N O D E   S T A T U S                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Renders tool tip hint graphic which describes the node's status.
 */
public 
class ViewerNodeHint
  extends ViewerGraphic
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constuct a new node status hint.
   */ 
  public 
  ViewerNodeHint
  (
   JNodeViewerPanel parent, 
   boolean showToolset, 
   boolean showEditor, 
   boolean showAction, 
   boolean showEditing
  ) 
  {
    super();

    pParent = parent; 

    pFileStates  = new TreeMap<FileState,Integer>();
    pQueueStates = new TreeMap<QueueState,Integer>();

    pViewsEditing = new TreeMap<String,TreeSet<String>>();

    pShowToolset = showToolset; 
    pShowAction  = showAction; 
    pShowEditor  = showEditor; 
    pShowEditing = showEditing; 
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the graphic is displayed.
   */ 
  public boolean
  isVisible()
  {
    return pIsVisible;
  }

  /**
   * Whether to display the Toolset hint.
   */ 
  public boolean
  showToolset()
  {
    return pShowToolset; 
  }

  /**
   * Whether to display the Editor hint.
   */ 
  public boolean
  showEditor()
  {
    return pShowEditor; 
  }

  /**
   * Whether to display the Action hint.
   */ 
  public boolean
  showAction()
  {
    return pShowAction; 
  }

  /**
   * Whether to display the list of working areas editing the node.
   */ 
  public boolean
  showEditing()
  {
    return pShowEditing;
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set whether the graphic should be displayed.
   */ 
  public void 
  setVisible
  (
   boolean tf
  ) 
  {
    pIsVisible = tf;
  }

  /**
   * Set whether to display the Toolset hint.
   */ 
  public void 
  setShowToolset
  (
   boolean tf
  ) 
  {
    pShowToolset = tf;
  }

  /**
   * Set whether to display the Editor hint.
   */ 
  public void 
  setShowEditor
  (
   boolean tf
  ) 
  {
    pShowEditor = tf;
  }

  /**
   * Set whether to display the Action hint.
   */ 
  public void 
  setShowAction
  (
   boolean tf
  ) 
  {
    pShowAction = tf;
  }

  /**
   * Set whether to display the list of working areas editing the node.
   */ 
  public void
  setShowEditing
  (
   boolean tf
  ) 
  {
    pShowEditing = tf;
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the node status information being displayed in the hint.
   */ 
  public void 
  updateHint
  (
   NodeStatus status, 
   TreeMap<String,TreeSet<String>> editing
  ) 
  {              
    /* clear any current settings */ 
    {
      pVersionState  = null;
      pPropertyState = null;
      pLinkState     = null;
      pFileStates.clear();
      pQueueStates.clear();
      
      pToolset = null;
      pEditor  = null; 
      pAction  = null;
      
      pViewsEditing.clear();
    }

    /* skip nodes without detailed status */ 
    {
      if(status == null)
        return;
      
      pDetails = status.getDetails();
      if(pDetails == null) 
        return;
    }

    /* reinitialize the cached node properties and display lists */ 
    {
      pBaseVersionDL   = null;
      pLatestVersionDL = null;
      
      if(!pDetails.isLightweight()) {
        for(FileSeq fseq : pDetails.getFileStateSequences()) {
          FileState fs[] = pDetails.getFileState(fseq);
          int wk;
          for(wk=0; wk<fs.length; wk++) {
            Integer fcnt = pFileStates.get(fs[wk]);
            if(fcnt == null)
              fcnt = new Integer(0);
            pFileStates.put(fs[wk], fcnt+1);
          }
        }
        
        {
          QueueState qs[] = pDetails.getQueueState();
          int wk;
          for(wk=0; wk<qs.length; wk++) {
            Integer qcnt = pQueueStates.get(qs[wk]);
            if(qcnt == null)
              qcnt = new Integer(0);
            pQueueStates.put(qs[wk], qcnt+1);
          }
        }
      }

      pCountDLs = null;
      
      {
        NodeCommon com = pDetails.getWorkingVersion();
        if(com == null) 
          com = pDetails.getLatestVersion();
        
        if(com != null) {
          pToolset = com.getToolset();
          pEditor  = com.getEditor();
          pAction  = com.getAction();
        }
      }
      
      pToolsetDL = null; 
      pEditorDLs = null;
      pActionDLs = null;

      if(editing != null) 
        pViewsEditing.putAll(editing);
      
      pEditingDLs = null;
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
    if(!pIsVisible) 
      return;

    GeometryMgr mgr = GeometryMgr.getInstance();
    try {
      if(pDetails != null) {
	/* base version */ 
	if(pBaseVersionDL == null) {
	  NodeVersion vsn = pDetails.getBaseVersion(); 
	  if(vsn != null) {
	    VersionID vid = vsn.getVersionID();
	    pBaseVersionDL = 
	      mgr.getTextDL(gl, PackageInfo.sGLFont, "v" + vid, 
			    GeometryMgr.TextAlignment.Center, 0.05);
	  }
	}

	/* latest version */ 
	if(pLatestVersionDL == null) {
	  NodeVersion vsn = pDetails.getLatestVersion(); 
	  if(vsn != null) {
	    VersionID vid = vsn.getVersionID();
	    pLatestVersionDL = 
	      mgr.getTextDL(gl, PackageInfo.sGLFont, "v" + vid, 
			    GeometryMgr.TextAlignment.Center, 0.05);
	  }
	}
      }

      /* state titles */ 
      if(pTitleDLs == null) {
	String titles[] = { "Version:", "Props:", "Link:", "File:", "Queue:" };
	pTitleDLs = new int[titles.length];

	int wk;
	for(wk=0; wk<titles.length; wk++) {
	  pTitleDLs[wk] = 
	    mgr.getTextDL(gl, PackageInfo.sGLFont, titles[wk],
			  GeometryMgr.TextAlignment.Right, 0.05);    
	}

        pTitleWidth = mgr.getTextWidth(PackageInfo.sGLFont, "Version:", 0.05) * sTextHeight;
      }

      /* version state labels */ 
      if(pVersionStateDLs == null) {
	ArrayList<VersionState> states = VersionState.all();
	pVersionStateDLs = new int[states.size()];
	pVersionWidths   = new double[states.size()];

	int wk;
	for(wk=0; wk<pVersionStateDLs.length; wk++) {
          String title = fixTitle(states.get(wk).toTitle()); 

	  pVersionStateDLs[wk] = 
	    mgr.getTextDL(gl, PackageInfo.sGLFont, fixTitle(states.get(wk).toTitle()), 
			  GeometryMgr.TextAlignment.Left, 0.05);

	  pVersionWidths[wk] = mgr.getTextWidth(PackageInfo.sGLFont, title, 0.05);
	}
      }

      /* property state labels */ 
      if(pPropertyStateDLs == null) {
	ArrayList<PropertyState> states = PropertyState.all();
	pPropertyStateDLs = new int[states.size()];
	pPropertyWidths   = new double[states.size()];

	int wk;
	for(wk=0; wk<pPropertyStateDLs.length; wk++) {
          String title = fixTitle(states.get(wk).toTitle()); 

	  pPropertyStateDLs[wk] = 
	    mgr.getTextDL(gl, PackageInfo.sGLFont, title, 
			  GeometryMgr.TextAlignment.Left, 0.05);

	  pPropertyWidths[wk] = mgr.getTextWidth(PackageInfo.sGLFont, title, 0.05);
	}
      }

      /* link state labels */ 
      if(pLinkStateDLs == null) {
	ArrayList<LinkState> states = LinkState.all();
	pLinkStateDLs = new int[states.size()];
	pLinkWidths   = new double[states.size()];

	int wk;
	for(wk=0; wk<pLinkStateDLs.length; wk++) { 
          String title = fixTitle(states.get(wk).toTitle()); 

	  pLinkStateDLs[wk] = 
            mgr.getTextDL(gl, PackageInfo.sGLFont, title, 
                          GeometryMgr.TextAlignment.Left, 0.05);

	  pLinkWidths[wk] = mgr.getTextWidth(PackageInfo.sGLFont, title, 0.05);
	}
      }

      /* file state labels */ 
      if(pFileStateDLs == null) {
	ArrayList<FileState> states = FileState.all();
	pFileStateDLs = new int[states.size()];
	pFileWidths   = new double[states.size()];

	int wk;
	for(wk=0; wk<pFileStateDLs.length; wk++) {
	  String title = fixTitle(states.get(wk).toTitle());

	  pFileStateDLs[wk] = 
	    mgr.getTextDL(gl, PackageInfo.sGLFont, title, 
			  GeometryMgr.TextAlignment.Left, 0.05);

	  pFileWidths[wk] = mgr.getTextWidth(PackageInfo.sGLFont, title, 0.05);
	}
      }

      /* queue state labels */ 
      if(pQueueStateDLs == null) {
	ArrayList<QueueState> states = QueueState.all();
	pQueueStateDLs = new int[states.size()];
	pQueueWidths   = new double[states.size()];

	int wk;
	for(wk=0; wk<pQueueStateDLs.length; wk++) {
	  String title = fixTitle(states.get(wk).toTitle());

	  pQueueStateDLs[wk] = 
	    mgr.getTextDL(gl, PackageInfo.sGLFont, title, 
			  GeometryMgr.TextAlignment.Left, 0.05);

	  pQueueWidths[wk] = mgr.getTextWidth(PackageInfo.sGLFont, title, 0.05);
	}
      }

      /* count labels */ 
      if(pCountDLs == null) {
	pCountDLs = new TreeMap<Integer,Integer>();
        pCountWidths = new TreeMap<Integer,Double>();
	
	TreeSet<Integer> counts = new TreeSet<Integer>();
	counts.addAll(pFileStates.values());
	counts.addAll(pQueueStates.values());

	for(Integer cnt : counts) {
	  int dl = mgr.getTextDL(gl, PackageInfo.sGLFont, "(" + cnt + ")", 
				 GeometryMgr.TextAlignment.Left, 0.05);
	  pCountDLs.put(cnt, dl);

          pCountWidths.put(cnt, mgr.getTextWidth(PackageInfo.sGLFont, "(" + cnt + ")", 0.05));
	}
      }
      
      /* toolset title */ 
      if(pToolsetTitleDL == null) {
	pToolsetTitleDL = 
	  mgr.getTextDL(gl, PackageInfo.sGLFont, "Toolset:", 
			GeometryMgr.TextAlignment.Right, 0.05);
      }

      /* toolset */ 
      if(pShowToolset && (pToolset != null)) {
	if(pToolsetDL == null) {
	  pToolsetDL = 
	    mgr.getTextDL(gl, PackageInfo.sGLFont, pToolset, 
			  GeometryMgr.TextAlignment.Left, 0.05);
        }

        pToolsetWidth = mgr.getTextWidth(PackageInfo.sGLFont, pToolset, 0.05);
      }
      
      /* plugin titles */ 
      if(pPluginTitleDLs == null) {
	pPluginTitleDLs = new int[4];
	
	pPluginTitleDLs[0] = 
	  mgr.getTextDL(gl, PackageInfo.sGLFont, "Editor:", 
			GeometryMgr.TextAlignment.Right, 0.05);
	
	pPluginTitleDLs[1] = 
	  mgr.getTextDL(gl, PackageInfo.sGLFont, "Action:", 
			GeometryMgr.TextAlignment.Right, 0.05);
	
	pPluginTitleDLs[2] = 
	  mgr.getTextDL(gl, PackageInfo.sGLFont, "Version:", 
			GeometryMgr.TextAlignment.Right, 0.05);
	
	pPluginTitleDLs[3] = 
	  mgr.getTextDL(gl, PackageInfo.sGLFont, "Vendor:", 
			GeometryMgr.TextAlignment.Right, 0.05);
      }

      /* null value */ 
      if(pNullDL == null) {
	pNullDL = 
	  mgr.getTextDL(gl, PackageInfo.sGLFont, "-", 
			GeometryMgr.TextAlignment.Center, 0.05);
      }

      /* editor */ 
      if(pShowEditor && (pEditor != null)) {
	if(pEditorDLs == null) {
	  pEditorDLs = new int[3];

	  pEditorDLs[0] = 
	    mgr.getTextDL(gl, PackageInfo.sGLFont, pEditor.getName(), 
			  GeometryMgr.TextAlignment.Left, 0.05);

	  pEditorDLs[1] = 
	    mgr.getTextDL(gl, PackageInfo.sGLFont, "v" + pEditor.getVersionID(), 
			  GeometryMgr.TextAlignment.Left, 0.05);

	  pEditorDLs[2] = 
	    mgr.getTextDL(gl, PackageInfo.sGLFont, pEditor.getVendor(), 
			  GeometryMgr.TextAlignment.Left, 0.05);
        }

        pEditorWidth = mgr.getTextWidth(PackageInfo.sGLFont, pEditor.getName(), 0.05);

        pEditorWidth = 
          Math.max(pEditorWidth, 
                   mgr.getTextWidth(PackageInfo.sGLFont, "v" + pEditor.getVersionID(), 0.05));

        pEditorWidth = 
          Math.max(pEditorWidth, 
                   mgr.getTextWidth(PackageInfo.sGLFont, pEditor.getVendor(), 0.05));
      }

      /* action */ 
      if(pShowAction && (pAction != null)) {
	if(pActionDLs == null) {
	  pActionDLs = new int[3];

	  pActionDLs[0] = 
	    mgr.getTextDL(gl, PackageInfo.sGLFont, pAction.getName(), 
			  GeometryMgr.TextAlignment.Left, 0.05);

	  pActionDLs[1] = 
	    mgr.getTextDL(gl, PackageInfo.sGLFont, "v" + pAction.getVersionID(), 
			  GeometryMgr.TextAlignment.Left, 0.05);

	  pActionDLs[2] = 
	    mgr.getTextDL(gl, PackageInfo.sGLFont, pAction.getVendor(), 
			  GeometryMgr.TextAlignment.Left, 0.05);
        }

        pActionWidth = mgr.getTextWidth(PackageInfo.sGLFont, pAction.getName(), 0.05);

        pActionWidth = 
          Math.max(pActionWidth, 
                   mgr.getTextWidth(PackageInfo.sGLFont, "v" + pAction.getVersionID(), 0.05));

        pActionWidth = 
          Math.max(pActionWidth, 
                   mgr.getTextWidth(PackageInfo.sGLFont, pAction.getVendor(), 0.05));
      }

      /* views editing title */ 
      if(pEditingTitleDL == null) {
	pEditingTitleDL = 
	  mgr.getTextDL(gl, PackageInfo.sGLFont, "Editing:", 
			GeometryMgr.TextAlignment.Right, 0.05);
      }

      /* views editing */ 
      if(pShowEditing && !pViewsEditing.isEmpty()) {
	if(pEditingDLs == null) {
          int cnt = 0;
          for(String author : pViewsEditing.keySet()) {
            for(String view : pViewsEditing.get(author)) 
              cnt++;
          }

	  pEditingDLs = new int[cnt];
          pEditingWidth = 0.0;

          cnt = 0;
          for(String author : pViewsEditing.keySet()) {
            for(String view : pViewsEditing.get(author)) {
              String title = author + " | " + view;

              pEditingDLs[cnt] = 
                mgr.getTextDL(gl, PackageInfo.sGLFont, title, 
                              GeometryMgr.TextAlignment.Left, 0.05);

              pEditingWidth = 
                Math.max(pEditingWidth, 
                         mgr.getTextWidth(PackageInfo.sGLFont, title, 0.05));

              cnt++;
            }
          }
        }
      }
    }
    catch(IOException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Tex, LogMgr.Level.Severe,
	 ex.getMessage());
    }
  }

  /**
   * Modify the title string.
   */ 
  private String
  fixTitle
  (
   String title
  ) 
  {
    if(title.equals("Needs Check-Out"))
       return "Needs CO";
    return title;
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
    if(!pIsVisible) 
      return;

    UserPrefs prefs = UserPrefs.getInstance();
    GeometryMgr mgr = GeometryMgr.getInstance();

    if(pDetails != null) {

      /* compute the size of the hint box and title/value alignments */ 
      double valueWidth = 0.0;
      {
        if(pVersionStateDLs != null) {
          valueWidth = 
            Math.max(valueWidth, 
                     pVersionWidths[pDetails.getVersionState().ordinal()]);
        }

        if(pPropertyStateDLs != null) {
          valueWidth = 
            Math.max(valueWidth, 
                     pPropertyWidths[pDetails.getPropertyState().ordinal()]);
        }
        
        if(pLinkStateDLs != null) {
          valueWidth = 
            Math.max(valueWidth, 
                     pLinkWidths[pDetails.getLinkState().ordinal()]);
        }

        if(!pFileStates.isEmpty()) {
          if((pFileStateDLs != null) && (pCountDLs != null)) {
            if(pFileStates.size() == 1) {
              valueWidth = 
                Math.max(valueWidth, 
                         pFileWidths[pFileStates.firstKey().ordinal()]);
            }
            else {
              for(FileState state : pFileStates.keySet()) {
                Integer cnt = pFileStates.get(state);
                valueWidth = 
                  Math.max(valueWidth, 
                           pFileWidths[state.ordinal()] + 0.05 + pCountWidths.get(cnt));
              }
            }
          }
        }

        if(!pQueueStates.isEmpty()) {
          if((pQueueStateDLs != null) && (pCountDLs != null)) {
            if(pQueueStates.size() == 1) {
              valueWidth = 
                Math.max(valueWidth, 
                         pQueueWidths[pQueueStates.firstKey().ordinal()]);
            }
            else {
              for(QueueState state : pQueueStates.keySet()) {
                Integer cnt = pQueueStates.get(state);
                valueWidth = 
                  Math.max(valueWidth, 
                           pQueueWidths[state.ordinal()] + 0.05 + pCountWidths.get(cnt));
              }
            }
          }
        }
         
	if((pShowToolset) && (pToolsetDL != null)) 
          valueWidth = Math.max(valueWidth, pToolsetWidth);                  

	if((pShowEditor) && (pEditorDLs != null)) 
          valueWidth = Math.max(valueWidth, pEditorWidth);         
        
	if((pShowAction) && (pActionDLs != null)) 
          valueWidth = Math.max(valueWidth, pActionWidth);  

	if((pShowEditing) && (pEditingDLs != null)) 
          valueWidth = Math.max(valueWidth, pEditingWidth);  

        valueWidth *= sTextHeight;
      }
      double width = pTitleWidth + 0.1 + valueWidth;
      double toffset = pTitleWidth - width*0.5;
      double voffset = toffset + 0.1;
      double noffset = voffset + valueWidth*0.35;

      gl.glPushMatrix();
      {
	{
          double scale;
	  String style = prefs.getDetailHintStyle();
	  if(style.equals("Scales with Nodes")) 
	    scale = sScaleFactor * prefs.getDetailHintSize();
	  else 
	    scale = 48.0 * pParent.getCanvasScale() * prefs.getDetailHintSize();

          gl.glTranslated(pPos.x(), pPos.y()-0.6, 0.0);
          gl.glScaled(scale, scale, scale); 
          gl.glTranslated(0.0, -0.8*sTextHeight-sBorder, 0.0);
	}

	/* background and borders */ 
	{
	  double srows    = (double) (3 + pFileStates.size() + pQueueStates.size());
	  double sborders = 2.0;

	  double orows    = 0.0;
	  double oborders = 0.0;
	  if(pShowToolset) {
	    orows += 1.0;
	    oborders += 2.0;
	  }

	  if(pShowEditor) {
	    orows += (pEditorDLs != null) ? 3.0 : 1.0;
	    oborders += 2.0;
	  }

	  if(pShowAction) {
	    orows += (pActionDLs != null) ? 3.0 : 1.0;
	    oborders += 2.0;
	  }
          
	  if(pShowEditing) {
	    orows += (pEditingDLs != null) ? pEditingDLs.length : 1.0;
	    oborders += 2.0;
	  }
	  
	  double x  = width*0.5 + sBorder; 
	  double y1 = 0.8*sTextHeight + sBorder;
	  double y2 = y1 - (sTextHeight + sBorder*2.0); 
	  double y3 = y1 - (sTextHeight*(1.0+srows+orows) + sBorder*(2.0+sborders+oborders)); 

	  {
	    BBox2d extent = new BBox2d(new Point2d(-x, y1), new Point2d(x, y3));
	    Color4d bgColor = new Color4d(0.45, 0.45, 0.45, 0.9); 
	    Color4d lineColor = new Color4d(0.65, 0.65, 0.65, 1.0); 

	    String look = prefs.getDetailHintLook();
	    if(look.equals("Rounded")) {
	      mgr.renderOutlinedRoundedBox
		(gl, 0.5*sTextHeight + sBorder, 15, extent, bgColor, 2.0f, lineColor); 
	    }
	    else {
	      mgr.renderOutlinedBox
		(gl, extent, bgColor, 2.0f, lineColor); 
	    }
	  }

	  gl.glBegin(GL.GL_LINES);
	  {
	    gl.glVertex2d(-x, y2); 
	    gl.glVertex2d( x, y2); 
	    
	    gl.glVertex2d(0.0, y1); 
	    gl.glVertex2d(0.0, y2); 

	    double y = y2 - (sTextHeight*srows + sBorder*sborders); 
	    if(pShowToolset) {
	      gl.glVertex2d(-x, y); 
	      gl.glVertex2d( x, y); 

	      y -= sTextHeight + sBorder*2.0;
	    }

	    if(pShowEditor) {
	      gl.glVertex2d(-x, y); 
	      gl.glVertex2d( x, y); 

	      if(pEditorDLs == null) 
		y -= sTextHeight + sBorder*2.0;
	      else 
		y -= sTextHeight*3.0 + sBorder*2.0;
	    }
	    
	    if(pShowAction) {
	      gl.glVertex2d(-x, y); 
	      gl.glVertex2d( x, y); 

	      if(pActionDLs == null) 
		y -= sTextHeight + sBorder*2.0;
	      else 
		y -= sTextHeight*3.0 + sBorder*2.0;
	    }

	    if(pShowEditing) {
	      gl.glVertex2d(-x, y); 
	      gl.glVertex2d( x, y); 
	    }
	  }
	  gl.glEnd();
	}

	gl.glColor4d(1.0, 1.0, 1.0, 1.0); 
	

	/* versions */ 
	{
	  double x = width*0.25 + sBorder*0.5; 

	  if(pBaseVersionDL != null) {
	    gl.glPushMatrix();
	    {
	      gl.glTranslated(-x, 0.0, 0.0);
	      gl.glScaled(sTextHeight, sTextHeight, sTextHeight);
	      gl.glCallList(pBaseVersionDL);
	    }
	    gl.glPopMatrix();
	  }

	  if(pLatestVersionDL != null) {
	    gl.glPushMatrix();
	    {
	      gl.glTranslated(x, 0.0, 0.0);
	      gl.glScaled(sTextHeight, sTextHeight, sTextHeight);
	      gl.glCallList(pLatestVersionDL);
	    }
	    gl.glPopMatrix();
	  }
	}

	/* state titles */ 
	if(pTitleDLs != null) {
	  double y = -sTextHeight - sBorder*2.0; 
	  int wk;
          int size = pFileStates.isEmpty() ? 3 : 5;
	  for(wk=0; wk<size; wk++, y-=sTextHeight) {
            if(wk == 4) 
              y -= (pFileStates.size() - 1) * sTextHeight;

	    gl.glPushMatrix();
	    {
	      gl.glTranslated(toffset, y, 0.0);
	      gl.glScaled(sTextHeight, sTextHeight, sTextHeight);
	      gl.glCallList(pTitleDLs[wk]);
	    }
	    gl.glPopMatrix();
	  }
	}

	/* state values */ 
	double y = -sTextHeight-sBorder*2.0; 
	{
	  gl.glPushMatrix();
	  {
            gl.glTranslated(voffset, 0.0, 0.0);

	    if(pVersionStateDLs != null) {
	      gl.glPushMatrix();
	      {
		gl.glTranslated(0.0, y, 0.0);
		gl.glScaled(sTextHeight, sTextHeight, sTextHeight);
		gl.glCallList(pVersionStateDLs[pDetails.getVersionState().ordinal()]);
	      }
	      gl.glPopMatrix();
	    }
	  
	    y -= sTextHeight; 
	    if(pPropertyStateDLs != null) {
	      gl.glPushMatrix();
	      {
		gl.glTranslated(0.0, y, 0.0);
		gl.glScaled(sTextHeight, sTextHeight, sTextHeight);
		gl.glCallList(pPropertyStateDLs[pDetails.getPropertyState().ordinal()]);
	      }
	      gl.glPopMatrix();
	    }

	    y -= sTextHeight; 
	    if(pLinkStateDLs != null) {
	      gl.glPushMatrix();
	      {
		gl.glTranslated(0.0, y, 0.0);
		gl.glScaled(sTextHeight, sTextHeight, sTextHeight);
		gl.glCallList(pLinkStateDLs[pDetails.getLinkState().ordinal()]);
	      }
	      gl.glPopMatrix();
	    }

            if(!pFileStates.isEmpty()) {
              if((pFileStateDLs != null) && (pCountDLs != null)) {
                boolean single = (pFileStates.size() == 1);
                for(FileState state : pFileStates.keySet()) {
                  Integer cnt = pFileStates.get(state);
                  
                  y -= sTextHeight; 
                  gl.glPushMatrix();
                  {
                    gl.glTranslated(0.0, y, 0.0);
                    gl.glScaled(sTextHeight, sTextHeight, sTextHeight);
                    gl.glCallList(pFileStateDLs[state.ordinal()]);
                  }
                  gl.glPopMatrix();
                  
                  if(!single) {
                    gl.glPushMatrix();
                    {
                      double x = 0.05 + pFileWidths[state.ordinal()]*sTextHeight;
                      gl.glTranslated(x, y, 0.0);
                      gl.glScaled(sTextHeight, sTextHeight, sTextHeight);
                      gl.glCallList(pCountDLs.get(cnt));
                    }
                    gl.glPopMatrix();
                  }
                }
              }
            }

            if(!pQueueStates.isEmpty()) {
              if((pQueueStateDLs != null) && (pCountDLs != null)) {
                boolean single = (pQueueStates.size() == 1);
                for(QueueState state : pQueueStates.keySet()) {
                  Integer cnt = pQueueStates.get(state);
                  
                  y -= sTextHeight; 
                  gl.glPushMatrix();
                  {
                    gl.glTranslated(0.0, y, 0.0);
                    gl.glScaled(sTextHeight, sTextHeight, sTextHeight);
                    gl.glCallList(pQueueStateDLs[state.ordinal()]);
                  }
                  gl.glPopMatrix();
                  
                  if(!single) {
                    gl.glPushMatrix();
                    {
                      double x = 0.05 + pQueueWidths[state.ordinal()]*sTextHeight;
                      gl.glTranslated(x, y, 0.0);
                      gl.glScaled(sTextHeight, sTextHeight, sTextHeight);
                      gl.glCallList(pCountDLs.get(cnt));
                    }
                    gl.glPopMatrix();
                  }
                }
              }
            }
              
            y -= sTextHeight + sBorder*2.0;
          }
          gl.glPopMatrix();
        }

	/* toolset */ 
	if(pShowToolset) {
	  if(pToolsetTitleDL != null) {
	    gl.glPushMatrix();
	    {
	      gl.glTranslated(toffset, y, 0.0);
	      gl.glScaled(sTextHeight, sTextHeight, sTextHeight);
	      gl.glCallList(pToolsetTitleDL); 
	    }
	    gl.glPopMatrix();
	  }

	  if(pToolsetDL != null) {
	    gl.glPushMatrix();
	    {
              gl.glTranslated(voffset, y, 0.0);
	      gl.glScaled(sTextHeight, sTextHeight, sTextHeight);
	      gl.glCallList(pToolsetDL); 
	    }
	    gl.glPopMatrix();
	  }
	  else if(pNullDL != null) {
	    gl.glPushMatrix();
	    {
	      gl.glTranslated(noffset, y, 0.0);
	      gl.glScaled(sTextHeight, sTextHeight, sTextHeight);
	      gl.glCallList(pNullDL); 
	    }
	    gl.glPopMatrix();
	  }

	  y -= sTextHeight + sBorder*2.0;
	}

	/* editor */ 
	if(pShowEditor) {
	  if(pPluginTitleDLs != null) {
	    double py = y;
	    gl.glPushMatrix();
	    {
	      gl.glTranslated(toffset, py, 0.0);
	      gl.glScaled(sTextHeight, sTextHeight, sTextHeight);
	      gl.glCallList(pPluginTitleDLs[0]); 
	    }
	    gl.glPopMatrix();

	    if(pEditorDLs != null) {
	      py -= sTextHeight;
	      gl.glPushMatrix();
	      {
                gl.glTranslated(toffset, py, 0.0);
		gl.glScaled(sTextHeight, sTextHeight, sTextHeight);
		gl.glCallList(pPluginTitleDLs[2]); 
	      }
	      gl.glPopMatrix();

	      py -= sTextHeight;
	      gl.glPushMatrix();
	      {
                gl.glTranslated(toffset, py, 0.0);
		gl.glScaled(sTextHeight, sTextHeight, sTextHeight);
		gl.glCallList(pPluginTitleDLs[3]); 
	      }
	      gl.glPopMatrix();	      
	    }
	  }

	  if(pEditorDLs != null) {
	    int wk;
	    for(wk=0; wk<pEditorDLs.length; wk++) {
	      gl.glPushMatrix();
	      {
                gl.glTranslated(voffset, y, 0.0);
		gl.glScaled(sTextHeight, sTextHeight, sTextHeight);
		gl.glCallList(pEditorDLs[wk]); 
	      }
	      gl.glPopMatrix();
	      
	      y -= sTextHeight;
	    }
	  }
	  else if(pNullDL != null) {
	    gl.glPushMatrix();
	    {
	      gl.glTranslated(noffset, y, 0.0);
	      gl.glScaled(sTextHeight, sTextHeight, sTextHeight);
	      gl.glCallList(pNullDL); 
	    }
	    gl.glPopMatrix();

	    y -= sTextHeight;
	  }
	  else {
	    y -= sTextHeight;
	  }

	  y -= sBorder*2.0;
	}

	/* action */ 
	if(pShowAction) {
	  if(pPluginTitleDLs != null) {
	    double py = y;
	    gl.glPushMatrix();
	    {
	      gl.glTranslated(toffset, py, 0.0);
	      gl.glScaled(sTextHeight, sTextHeight, sTextHeight);
	      gl.glCallList(pPluginTitleDLs[1]); 
	    }
	    gl.glPopMatrix();

	    if(pActionDLs != null) {
	      py -= sTextHeight;
	      gl.glPushMatrix();
	      {
                gl.glTranslated(toffset, py, 0.0);
		gl.glScaled(sTextHeight, sTextHeight, sTextHeight);
		gl.glCallList(pPluginTitleDLs[2]); 
	      }
	      gl.glPopMatrix();

	      py -= sTextHeight;
	      gl.glPushMatrix();
	      {
                gl.glTranslated(toffset, py, 0.0);
		gl.glScaled(sTextHeight, sTextHeight, sTextHeight);
		gl.glCallList(pPluginTitleDLs[3]); 
	      }
	      gl.glPopMatrix();	      
	    }
	  }

	  if(pActionDLs != null) {
	    int wk;
	    for(wk=0; wk<pActionDLs.length; wk++) {
	      gl.glPushMatrix();
	      {
                gl.glTranslated(voffset, y, 0.0);
		gl.glScaled(sTextHeight, sTextHeight, sTextHeight);
		gl.glCallList(pActionDLs[wk]); 
	      }
	      gl.glPopMatrix();
	      
	      y -= sTextHeight;
	    }
	  }
	  else if(pNullDL != null) {
	    gl.glPushMatrix();
	    {
	      gl.glTranslated(noffset, y, 0.0);
	      gl.glScaled(sTextHeight, sTextHeight, sTextHeight);
	      gl.glCallList(pNullDL); 
	    }
	    gl.glPopMatrix();

	    y -= sTextHeight;
	  }
	  else {
	    y -= sTextHeight;
	  }

	  y -= sBorder*2.0;
	}

	/* editing */ 
	if(pShowEditing) {
	  if(pEditingTitleDL != null) {
	    double py = y;
	    gl.glPushMatrix();
	    {
	      gl.glTranslated(toffset, py, 0.0);
	      gl.glScaled(sTextHeight, sTextHeight, sTextHeight);
	      gl.glCallList(pEditingTitleDL); 
	    }
	    gl.glPopMatrix();
	  }

	  if(pEditingDLs != null) {
	    int wk;
	    for(wk=0; wk<pEditingDLs.length; wk++) {
	      gl.glPushMatrix();
	      {
                gl.glTranslated(voffset, y, 0.0);
		gl.glScaled(sTextHeight, sTextHeight, sTextHeight);
		gl.glCallList(pEditingDLs[wk]); 
	      }
	      gl.glPopMatrix();
	      
	      y -= sTextHeight;
	    }
	  }
	  else if(pNullDL != null) {
	    gl.glPushMatrix();
	    {
	      gl.glTranslated(noffset, y, 0.0);
	      gl.glScaled(sTextHeight, sTextHeight, sTextHeight);
	      gl.glCallList(pNullDL); 
	    }
	    gl.glPopMatrix();

	    y -= sTextHeight;
	  }
	  else {
	    y -= sTextHeight;
	  }
        }
      }
      gl.glPopMatrix();
    }
  }  



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private final double  sBorder      = 0.15;
  private final double  sTextHeight  = 0.35;
  private final double  sScaleFactor = 0.75;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The parent panel.
   */ 
  private JNodeViewerPanel  pParent; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the graphic is displayed.
   */ 
  private boolean  pIsVisible; 

  /**
   * Whether to display the optional hint components. 
   */ 
  private boolean  pShowToolset; 
  private boolean  pShowEditor; 
  private boolean  pShowAction; 
  private boolean  pShowEditing; 

  /**
   * The node status details. 
   */ 
  private NodeDetails  pDetails; 
  private String       pToolset; 
  private BaseEditor   pEditor; 
  private BaseAction   pAction; 

  /**
   * The node states. 
   */ 
  private VersionState                pVersionState; 
  private PropertyState               pPropertyState; 
  private LinkState                   pLinkState;      
  private TreeMap<FileState,Integer>  pFileStates; 
  private TreeMap<QueueState,Integer> pQueueStates; 

  /**
   * Working area views editing/containig the target node.
   */ 
  private TreeMap<String,TreeSet<String>>  pViewsEditing; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The OpenGL display list handle for the background geometry.
   */ 
  private Integer  pBackgroundDL; 
  
  /**
   * The OpenGL display list handle for revision number labels. 
   */ 
  private Integer  pBaseVersionDL; 
  private Integer  pLatestVersionDL; 

  /**
   * The OpenGL display list handle for state titles.
   */ 
  private int[]   pTitleDLs; 
  private double  pTitleWidth; 
  
  /**
   * The OpenGL display list handle for the state values.
   */ 
  private int[]     pVersionStateDLs;  	  
  private double[]  pVersionWidths; 

  private int[]     pPropertyStateDLs; 	  
  private double[]  pPropertyWidths;
 
  private int[]     pLinkStateDLs;   	  
  private double[]  pLinkWidths;   

  private int[]     pFileStateDLs; 	  
  private double[]  pFileWidths;

  private int[]     pQueueStateDLs; 	  
  private double[]  pQueueWidths;

  private TreeMap<Integer,Integer>  pCountDLs; 
  private TreeMap<Integer,Double>   pCountWidths; 

  /**
   * The OpenGL display list handles for the optional properties. 
   */ 
  private Integer  pToolsetTitleDL;
  private int[]    pPluginTitleDLs; 
  private Integer  pNullDL; 

  private Integer  pToolsetDL;
  private double   pToolsetWidth; 

  private int[]    pEditorDLs; 
  private double   pEditorWidth;

  private int[]    pActionDLs; 
  private double   pActionWidth;

  /**
   * The OpenGL display list handles for the working areas editing the node.
   */ 
  private Integer  pEditingTitleDL;
  private int[]    pEditingDLs; 
  private double   pEditingWidth; 
  
}
