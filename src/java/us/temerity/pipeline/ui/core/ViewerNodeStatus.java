// $Id: ViewerNodeStatus.java,v 1.3 2006/12/10 06:37:09 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;

import java.io.*;
import java.util.*;

import net.java.games.jogl.*;

/*------------------------------------------------------------------------------------------*/
/*   V I E W E R   N O D E   S T A T U S                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Renders tool tip hint graphic which describes the node's status.
 */
public 
class ViewerNodeStatus
  extends ViewerGraphic
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constuct a new node status hint.
   */ 
  public 
  ViewerNodeStatus()
  {
    super();

    pFileStates  = new TreeMap<FileState,Integer>();
    pQueueStates = new TreeMap<QueueState,Integer>();
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


  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the current node status being displayed.
   */ 
  public void 
  setNodeStatus
  (
   NodeStatus status
  ) 
  {              
    pVersionState  = null;
    pPropertyState = null;
    pLinkState     = null;
    pFileStates.clear();
    pQueueStates.clear();

    if(status == null)
      return;

    pDetails = status.getDetails();
    if(pDetails == null) 
      return;

    pBaseVersionDL   = null;
    pLatestVersionDL = null;

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

    pCountDLs = null;
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
	String titles[] = { "Version:", "Property:", "Link:", "File:", "Queue:" };
	pTitleDLs = new int[titles.length];

	int wk;
	for(wk=0; wk<titles.length; wk++) {
	  pTitleDLs[wk] = 
	    mgr.getTextDL(gl, PackageInfo.sGLFont, titles[wk],
			  GeometryMgr.TextAlignment.Right, 0.05);
	}
      }

      /* version state labels */ 
      if(pVersionStateDLs == null) {
	ArrayList<VersionState> states = VersionState.all();
	pVersionStateDLs = new int[states.size()];

	int wk;
	for(wk=0; wk<pVersionStateDLs.length; wk++) {
	  pVersionStateDLs[wk] = 
	    mgr.getTextDL(gl, PackageInfo.sGLFont, fixTitle(states.get(wk).toTitle()), 
			  GeometryMgr.TextAlignment.Left, 0.05);
	}
      }

      /* property state labels */ 
      if(pPropertyStateDLs == null) {
	ArrayList<PropertyState> states = PropertyState.all();
	pPropertyStateDLs = new int[states.size()];

	int wk;
	for(wk=0; wk<pPropertyStateDLs.length; wk++) {
	  pPropertyStateDLs[wk] = 
	    mgr.getTextDL(gl, PackageInfo.sGLFont, fixTitle(states.get(wk).toTitle()), 
			  GeometryMgr.TextAlignment.Left, 0.05);
	}
      }

      /* link state labels */ 
      if(pLinkStateDLs == null) {
	ArrayList<LinkState> states = LinkState.all();
	pLinkStateDLs = new int[states.size()];

	int wk;
	for(wk=0; wk<pLinkStateDLs.length; wk++) {
	  pLinkStateDLs[wk] = 
	    mgr.getTextDL(gl, PackageInfo.sGLFont, fixTitle(states.get(wk).toTitle()), 
			  GeometryMgr.TextAlignment.Left, 0.05);
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
	
	TreeSet<Integer> counts = new TreeSet<Integer>();
	counts.addAll(pFileStates.values());
	counts.addAll(pQueueStates.values());

	for(Integer cnt : counts) {
	  int dl = mgr.getTextDL(gl, PackageInfo.sGLFont, "(" + cnt + ")", 
				 GeometryMgr.TextAlignment.Left, 0.05);
	  pCountDLs.put(cnt, dl);
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

    if((pDetails != null) && !pFileStates.isEmpty() && !pQueueStates.isEmpty()) {

      gl.glPushMatrix();
      {
	gl.glTranslated(pPos.x(), pPos.y()-0.85, 0.0);
	gl.glScaled(sTextScale, sTextScale, sTextScale);
	
	{
	  double rows = (double) (4 + pFileStates.size() + pQueueStates.size());
	  
	  double x  = sWidth*0.5 + sBorder; 
	  double y1 = 0.8*sTextHeight + sBorder;
	  double y2 = y1 - (sTextHeight + sBorder*2.0); 
	  double y3 = y1 - (sTextHeight*rows + sBorder*4.0); 
	  
	  gl.glColor4d(0.45, 0.45, 0.45, 0.9);
	  gl.glBegin(GL.GL_QUADS);
	  {
	    gl.glVertex2d(-x, y1); 
	    gl.glVertex2d( x, y1); 
	    gl.glVertex2d( x, y3); 
	    gl.glVertex2d(-x, y3); 
	  }
	  gl.glEnd();
	  
	  gl.glColor4d(0.65, 0.65, 0.65, 1.0);
	  gl.glLineWidth(2.0f);
	  gl.glBegin(GL.GL_LINE_LOOP);
	  {
	    gl.glVertex2d(-x, y1); 
	    gl.glVertex2d( x, y1); 
	    gl.glVertex2d( x, y3); 
	    gl.glVertex2d(-x, y3); 
	  }
	  gl.glEnd();
	  
	  gl.glBegin(GL.GL_LINES);
	  {
	    gl.glVertex2d(-x, y2); 
	    gl.glVertex2d( x, y2); 
	    
	    gl.glVertex2d(0.0, y1); 
	    gl.glVertex2d(0.0, y2); 
	  }
	  gl.glEnd();
	}

	gl.glColor4d(1.0, 1.0, 1.0, 1.0); 
	
	{
	  double x = sWidth*0.25 + sBorder*0.5; 

	  if(pBaseVersionDL != null) {
	    gl.glPushMatrix();
	    {
	      gl.glTranslated(-x, 0.0, 0.0);
	      gl.glScaled(0.35, 0.35, 0.35);
	      gl.glCallList(pBaseVersionDL);
	    }
	    gl.glPopMatrix();
	  }

	  if(pLatestVersionDL != null) {
	    gl.glPushMatrix();
	    {
	      gl.glTranslated(x, 0.0, 0.0);
	      gl.glScaled(0.35, 0.35, 0.35);
	      gl.glCallList(pLatestVersionDL);
	    }
	    gl.glPopMatrix();
	  }
	}

	if(pTitleDLs != null) {
	  double y = -sTextHeight - sBorder*2.0; 
	  int wk;
	  for(wk=0; wk<pTitleDLs.length; wk++, y-=sTextHeight) {
	    if(wk == 4) 
	      y -= (pFileStates.size() - 1) * sTextHeight;

	    gl.glPushMatrix();
	    {
	      gl.glTranslated(-0.3, y, 0.0);
	      gl.glScaled(0.35, 0.35, 0.35);
	      gl.glCallList(pTitleDLs[wk]);
	    }
	    gl.glPopMatrix();
	  }
	}

	{
	  gl.glTranslated(-0.2, 0.0, 0.0);

	  double y = -sTextHeight-sBorder*2.0; 
	  if(pVersionStateDLs != null) {
	    gl.glPushMatrix();
	    {
	      gl.glTranslated(0.0, y, 0.0);
	      gl.glScaled(0.35, 0.35, 0.35);
	      gl.glCallList(pVersionStateDLs[pDetails.getVersionState().ordinal()]);
	    }
	    gl.glPopMatrix();
	  }
	  
	  y -= sTextHeight; 
	  if(pPropertyStateDLs != null) {
	    gl.glPushMatrix();
	    {
	      gl.glTranslated(0.0, y, 0.0);
	      gl.glScaled(0.35, 0.35, 0.35);
	      gl.glCallList(pPropertyStateDLs[pDetails.getPropertyState().ordinal()]);
	    }
	    gl.glPopMatrix();
	  }

	  y -= sTextHeight; 
	  if(pLinkStateDLs != null) {
	    gl.glPushMatrix();
	    {
	      gl.glTranslated(0.0, y, 0.0);
	      gl.glScaled(0.35, 0.35, 0.35);
	      gl.glCallList(pLinkStateDLs[pDetails.getLinkState().ordinal()]);
	    }
	    gl.glPopMatrix();
	  }

	  if((pFileStateDLs != null) && (pCountDLs != null)) {
	    boolean single = (pFileStates.size() == 1);
	    for(FileState state : pFileStates.keySet()) {
	      Integer cnt = pFileStates.get(state);

	      y -= sTextHeight; 
	      gl.glPushMatrix();
	      {
		gl.glTranslated(0.0, y, 0.0);
		gl.glScaled(0.35, 0.35, 0.35);
		gl.glCallList(pFileStateDLs[state.ordinal()]);
	      }
	      gl.glPopMatrix();

	      if(!single) {
		gl.glPushMatrix();
		{
		  double x = 0.05 + pFileWidths[state.ordinal()]*0.35;
		  gl.glTranslated(x, y, 0.0);
		  gl.glScaled(0.35, 0.35, 0.35);
		  gl.glCallList(pCountDLs.get(cnt));
		}
		gl.glPopMatrix();
	      }
	    }
	  }

	  if((pQueueStateDLs != null) && (pCountDLs != null)) {
	    boolean single = (pQueueStates.size() == 1);
	    for(QueueState state : pQueueStates.keySet()) {
	      Integer cnt = pQueueStates.get(state);

	      y -= sTextHeight; 
	      gl.glPushMatrix();
	      {
		gl.glTranslated(0.0, y, 0.0);
		gl.glScaled(0.35, 0.35, 0.35);
		gl.glCallList(pQueueStateDLs[state.ordinal()]);
	      }
	      gl.glPopMatrix();

	      if(!single) {
		gl.glPushMatrix();
		{
		  double x = 0.05 + pQueueWidths[state.ordinal()]*0.35;
		  gl.glTranslated(x, y, 0.0);
		  gl.glScaled(0.35, 0.35, 0.35);
		  gl.glCallList(pCountDLs.get(cnt));
		}
		gl.glPopMatrix();
	      }
	    }
	  }
	}
      }
      gl.glPopMatrix();
    }
  }  



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private final double  sWidth      = 3.8;
  private final double  sBorder     = 0.15;
  private final double  sTextHeight = 0.35;
  private final double  sTextScale  = 0.75;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the graphic is displayed.
   */ 
  private boolean  pIsVisible; 

  /**
   * The node status details. 
   */ 
  private NodeDetails  pDetails; 

  /**
   * The node states. 
   */ 
  private VersionState                pVersionState; 
  private PropertyState               pPropertyState; 
  private LinkState                   pLinkState;      
  private TreeMap<FileState,Integer>  pFileStates; 
  private TreeMap<QueueState,Integer> pQueueStates; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The OpenGL display list handle for the background geometry.
   */ 
  private Integer  pBackgroundDL; 
  
  /**
   * The OpenGL display list handle for version labels. 
   */ 
  private Integer  pBaseVersionDL; 
  private Integer  pLatestVersionDL; 

  /**
   * The OpenGL display list handle for state titles.
   */ 
  private int[]  pTitleDLs; 
  
  /**
   * The OpenGL display list handle for the state values.
   */ 
  private int[]  pVersionStateDLs;   
  private int[]  pPropertyStateDLs;  
  private int[]  pLinkStateDLs;      

  private int[]     pFileStateDLs; 	  
  private double[]  pFileWidths;

  private int[]     pQueueStateDLs; 	  
  private double[]  pQueueWidths;

  private TreeMap<Integer,Integer>  pCountDLs; 

}
