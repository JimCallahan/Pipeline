// $Id: ViewerJobHint.java,v 1.2 2007/06/26 05:18:57 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;

import java.io.*;
import java.util.*;

import javax.media.opengl.*;

/*------------------------------------------------------------------------------------------*/
/*   V I E W E R   J O B   S T A T U S                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Renders tool tip hint graphic which describes the job's status.
 */
public 
class ViewerJobHint
  extends ViewerGraphic
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constuct a new node status hint.
   */ 
  public 
  ViewerJobHint
  (
   JQueueJobViewerPanel parent, 
   boolean showToolset, 
   boolean showAction, 
   boolean showHost, 
   boolean showTiming 
  ) 
  {
    super();

    pParent = parent; 

    pShowToolset = showToolset; 
    pShowAction  = showAction; 
    pShowHost    = showHost; 
    pShowTiming  = showTiming; 
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
   * Whether to display the Action hint.
   */ 
  public boolean
  showAction()
  {
    return pShowAction; 
  }

  /**
   * Whether to display the Host hint.
   */ 
  public boolean
  showHost()
  {
    return pShowHost; 
  }

  /**
   * Whether to display the Timing hint.
   */ 
  public boolean
  showTiming()
  {
    return pShowTiming; 
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
   * Set whether to display the Host hint.
   */ 
  public void 
  setShowHost
  (
   boolean tf
  ) 
  {
    pShowHost = tf;
  }

  /**
   * Set whether to display the Timing hint.
   */ 
  public void 
  setShowTiming
  (
   boolean tf
  ) 
  {
    pShowTiming = tf;
  }

  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the node status information being displayed in the hint.
   */ 
  public void 
  updateHint
  (
   QueueJob job, 
   QueueJobInfo info
  ) 
  {              
    pJob     = job;
    pJobInfo = info; 

    /* reinitialize the cached node properties and display lists */ 
    {
      pTargetDL  = null;
      pToolsetDL = null; 
      pActionDLs = null;
      pHostDLs   = null;
      pTimingDLs = null;
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

    UserPrefs prefs = UserPrefs.getInstance();
    GeometryMgr mgr = GeometryMgr.getInstance();

    try {	
      ActionAgenda agenda = null;
      if(pJob != null)
        agenda = pJob.getActionAgenda();

      /* target file sequence */ 
      if((pTargetDL == null) && (agenda != null)) {
        String text = agenda.getPrimaryTarget().toString(); 

        pTargetDL = mgr.getTextDL(gl, PackageInfo.sGLFont, text, 
                                  GeometryMgr.TextAlignment.Center, 0.05);

        pTargetWidth = mgr.getTextWidth(PackageInfo.sGLFont, text, 0.05); 
      }
      
      /* null value */ 
      if(pNullDL == null) {
	pNullDL = 
	  mgr.getTextDL(gl, PackageInfo.sGLFont, "-", 
			GeometryMgr.TextAlignment.Center, 0.05);
      }

      /* toolset title */ 
      if(pToolsetTitleDL == null) {
	pToolsetTitleDL = 
	  mgr.getTextDL(gl, PackageInfo.sGLFont, "Toolset:", 
			GeometryMgr.TextAlignment.Right, 0.05);
      }

      /* toolset */ 
      if(pShowToolset && (agenda != null)) {
	if(pToolsetDL == null) {
	  pToolsetDL = 
	    mgr.getTextDL(gl, PackageInfo.sGLFont, agenda.getToolset(), 
			  GeometryMgr.TextAlignment.Left, 0.05);
        }

        pToolsetWidth = mgr.getTextWidth(PackageInfo.sGLFont, agenda.getToolset(), 0.05);
      }
      
      /* action titles */ 
      if(pActionTitleDLs == null) {
	pActionTitleDLs = new int[3];
	
	pActionTitleDLs[0] = 
	  mgr.getTextDL(gl, PackageInfo.sGLFont, "Action:", 
			GeometryMgr.TextAlignment.Right, 0.05);
	
	pActionTitleDLs[1] = 
	  mgr.getTextDL(gl, PackageInfo.sGLFont, "Version:", 
			GeometryMgr.TextAlignment.Right, 0.05);
	
	pActionTitleDLs[2] = 
	  mgr.getTextDL(gl, PackageInfo.sGLFont, "Vendor:", 
			GeometryMgr.TextAlignment.Right, 0.05);
      }

      /* action */ 
      BaseAction action = null;
      if(pJob != null) 
        action = pJob.getAction();

      if(pShowAction && (action != null)) {
	if(pActionDLs == null) {
	  pActionDLs = new int[3];

	  pActionDLs[0] = 
	    mgr.getTextDL(gl, PackageInfo.sGLFont, action.getName(), 
			  GeometryMgr.TextAlignment.Left, 0.05);

	  pActionDLs[1] = 
	    mgr.getTextDL(gl, PackageInfo.sGLFont, "v" + action.getVersionID(), 
			  GeometryMgr.TextAlignment.Left, 0.05);

	  pActionDLs[2] = 
	    mgr.getTextDL(gl, PackageInfo.sGLFont, action.getVendor(), 
			  GeometryMgr.TextAlignment.Left, 0.05);
        }

        pActionWidth = mgr.getTextWidth(PackageInfo.sGLFont, action.getName(), 0.05);

        pActionWidth = 
          Math.max(pActionWidth, 
                   mgr.getTextWidth(PackageInfo.sGLFont, "v" + action.getVersionID(), 0.05));

        pActionWidth = 
          Math.max(pActionWidth, 
                   mgr.getTextWidth(PackageInfo.sGLFont, action.getVendor(), 0.05));
      }
         
      /* host titles */ 
      if(pHostTitleDLs == null) {
	pHostTitleDLs = new int[2];
	
	pHostTitleDLs[0] = 
	  mgr.getTextDL(gl, PackageInfo.sGLFont, "Host:", 
			GeometryMgr.TextAlignment.Right, 0.05);
	
	pHostTitleDLs[1] = 
	  mgr.getTextDL(gl, PackageInfo.sGLFont, "OS:", 
			GeometryMgr.TextAlignment.Right, 0.05);
      }
      
      /* host info */ 
      String hostname = null;
      if(pJobInfo != null) {
        if(prefs.getShowFullHostnames()) 
          hostname = pJobInfo.getHostname();
        else 
          hostname = pJobInfo.getShortHostname();
      }

      OsType os = null; 
      if(pJobInfo != null) 
        os = pJobInfo.getOsType();

      if(pShowHost && (hostname != null) && (os != null)) {
	if(pHostDLs == null) {
          pHostDLs = new int[2];

	  pHostDLs[0] = 
	    mgr.getTextDL(gl, PackageInfo.sGLFont, hostname, 
			  GeometryMgr.TextAlignment.Left, 0.05);

	  pHostDLs[1] = 
	    mgr.getTextDL(gl, PackageInfo.sGLFont, os.toString(), 
			  GeometryMgr.TextAlignment.Left, 0.05);
        }

        pHostWidth = mgr.getTextWidth(PackageInfo.sGLFont, hostname, 0.05);

        pHostWidth = 
          Math.max(pHostWidth, 
                   mgr.getTextWidth(PackageInfo.sGLFont, os.toString(), 0.05));
      }
 
      /* timing titles */ 
      if(pTimingTitleDLs == null) {
	pTimingTitleDLs = new int[2];
	
	pTimingTitleDLs[0] = 
	  mgr.getTextDL(gl, PackageInfo.sGLFont, "Waiting:", 
			GeometryMgr.TextAlignment.Right, 0.05);
	
	pTimingTitleDLs[1] = 
	  mgr.getTextDL(gl, PackageInfo.sGLFont, "Running:", 
			GeometryMgr.TextAlignment.Right, 0.05);

        pTitleWidth = mgr.getTextWidth(PackageInfo.sGLFont, "Running:", 0.05) * sTextHeight;
      }
      
      /* timing info */ 
      Long submitted = null;
      if(pJobInfo != null) 
	submitted = pJobInfo.getSubmittedStamp();

      Long started = null;
      if(pJobInfo != null) 
	started = pJobInfo.getStartedStamp();

      Long completed = null;
      if(pJobInfo != null) 
	completed = pJobInfo.getCompletedStamp();

      if(pShowTiming) {
        if(pTimingDLs == null) {
          pTimingDLs = new int[2];
          pTimingWidth = 0.0;

          pTimingDLs[0] = -1;
          if(submitted != null) {
            String text = null;
            if(started != null)
              text = TimeStamps.formatInterval(started - submitted);
            else if(completed != null)
              text = TimeStamps.formatInterval(completed - submitted);
            else 
              text = TimeStamps.formatInterval(TimeStamps.now() - submitted);

            pTimingDLs[0] = 
              mgr.getTextDL(gl, PackageInfo.sGLFont, text, 
                            GeometryMgr.TextAlignment.Left, 0.05);

            pTimingWidth = mgr.getTextWidth(PackageInfo.sGLFont, text, 0.05);
          }

          pTimingDLs[1] = -1;
          if(started != null) {
            String text = null;
            if(completed != null)
              text = TimeStamps.formatInterval(completed - started);
            else 
              text = TimeStamps.formatInterval(TimeStamps.now() - started);

            pTimingDLs[1] = 
              mgr.getTextDL(gl, PackageInfo.sGLFont, text, 
                            GeometryMgr.TextAlignment.Left, 0.05);

            pTimingWidth = 
              Math.max(pTimingWidth, 
                       mgr.getTextWidth(PackageInfo.sGLFont, text, 0.05));
          }
        }
      }
    }
    catch(PipelineException ex) {
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
    if(!pIsVisible) 
      return;

    UserPrefs prefs = UserPrefs.getInstance();
    GeometryMgr mgr = GeometryMgr.getInstance();

    if((pJob != null) && (pJobInfo != null)) {

      /* compute the size of the hint box and title/value alignments */ 
      double valueWidth = 0.0; 
      {  
	if((pShowToolset) && (pToolsetDL != null)) 
          valueWidth = Math.max(valueWidth, pToolsetWidth);                  

	if((pShowAction) && (pActionDLs != null)) 
          valueWidth = Math.max(valueWidth, pActionWidth);  

	if((pShowHost) && (pHostDLs != null)) 
          valueWidth = Math.max(valueWidth, pHostWidth);  

	if((pShowTiming) && (pTimingDLs != null)) 
          valueWidth = Math.max(valueWidth, pTimingWidth);  

        valueWidth *= sTextHeight;
      }
      double width   = Math.max(pTargetWidth*sTextHeight, pTitleWidth + 0.1 + valueWidth);
      double toffset = pTitleWidth - width*0.5;
      double voffset = toffset + 0.1;
      double noffset = voffset + valueWidth*0.35;

      gl.glPushMatrix();
      {
        {
          double scale;
          String style = prefs.getJobDetailHintStyle();
          if(style.equals("Scales with Jobs")) 
            scale = sScaleFactor * prefs.getJobDetailHintSize();
          else 
            scale = 48.0 * pParent.getCanvasScale() * prefs.getJobDetailHintSize();
          
          gl.glTranslated(pPos.x(), pPos.y(), 0.0);
          gl.glScaled(scale, scale, scale); 
          gl.glTranslated(0.0, -0.8*sTextHeight-sBorder, 0.0);
        }

	/* background and borders */ 
	{
	  double orows    = 0.0;
	  double oborders = 0.0;
	  if(pShowToolset) {
	    orows += 1.0;
	    oborders += 2.0;
	  }

	  if(pShowAction) {
	    orows += (pActionDLs != null) ? 3.0 : 1.0;
	    oborders += 2.0;
	  }
          
	  if(pShowHost) {
	    orows += (pHostDLs != null) ? 2.0 : 1.0;
	    oborders += 2.0;
	  }

	  if(pShowTiming) {
	    orows += (pTimingDLs != null) ? 2.0 : 1.0;
	    oborders += 2.0;
	  }
	  
	  double x  = width*0.5 + sBorder; 
	  double y1 = 0.8*sTextHeight + sBorder;
	  double y2 = y1 - (sTextHeight + sBorder*2.0); 
	  double y3 = y1 - (sTextHeight*(1.0+orows) + sBorder*(2.0+oborders)); 
          
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
	    
	    double y = y2;
	    if(pShowToolset) {
	      gl.glVertex2d(-x, y); 
	      gl.glVertex2d( x, y); 

	      y -= sTextHeight + sBorder*2.0;
	    }

	    if(pShowAction) {
	      gl.glVertex2d(-x, y); 
	      gl.glVertex2d( x, y); 

	      if(pActionDLs == null) 
		y -= sTextHeight + sBorder*2.0;
	      else 
		y -= sTextHeight*3.0 + sBorder*2.0;
	    }

	    if(pShowHost) {
	      gl.glVertex2d(-x, y); 
	      gl.glVertex2d( x, y); 

	      if(pHostDLs == null) 
		y -= sTextHeight + sBorder*2.0;
	      else 
		y -= sTextHeight*2.0 + sBorder*2.0;
	    }

	    if(pShowTiming) {
	      gl.glVertex2d(-x, y); 
	      gl.glVertex2d( x, y); 
	    }
	  }
	  gl.glEnd();
	}

	gl.glColor4d(1.0, 1.0, 1.0, 1.0); 
	

	/* target file sequence */ 
	double y = 0.0;
        if(pTargetDL != null) {
          gl.glPushMatrix();
          {
            gl.glTranslated(0.0, 0.0, 0.0);
            gl.glScaled(sTextHeight, sTextHeight, sTextHeight);
            gl.glCallList(pTargetDL);
          }
          gl.glPopMatrix();

	  y -= sTextHeight + sBorder*2.0;
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

	/* action */ 
	if(pShowAction) {
	  if(pActionTitleDLs != null) {
	    double py = y;
	    gl.glPushMatrix();
	    {
	      gl.glTranslated(toffset, py, 0.0);
	      gl.glScaled(sTextHeight, sTextHeight, sTextHeight);
	      gl.glCallList(pActionTitleDLs[0]); 
	    }
	    gl.glPopMatrix();

	    if(pActionDLs != null) {
	      py -= sTextHeight;
	      gl.glPushMatrix();
	      {
                gl.glTranslated(toffset, py, 0.0);
		gl.glScaled(sTextHeight, sTextHeight, sTextHeight);
		gl.glCallList(pActionTitleDLs[1]); 
	      }
	      gl.glPopMatrix();

	      py -= sTextHeight;
	      gl.glPushMatrix();
	      {
                gl.glTranslated(toffset, py, 0.0);
		gl.glScaled(sTextHeight, sTextHeight, sTextHeight);
		gl.glCallList(pActionTitleDLs[2]); 
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

	/* host */ 
	if(pShowHost) {
	  if(pHostTitleDLs != null) {
	    double py = y;
	    gl.glPushMatrix();
	    {
	      gl.glTranslated(toffset, py, 0.0);
	      gl.glScaled(sTextHeight, sTextHeight, sTextHeight);
	      gl.glCallList(pHostTitleDLs[0]); 
	    }
	    gl.glPopMatrix();

	    if(pHostDLs != null) {
	      py -= sTextHeight;
	      gl.glPushMatrix();
	      {
                gl.glTranslated(toffset, py, 0.0);
		gl.glScaled(sTextHeight, sTextHeight, sTextHeight);
		gl.glCallList(pHostTitleDLs[1]); 
	      }
	      gl.glPopMatrix();
            }
	  }

	  if(pHostDLs != null) {
	    int wk;
	    for(wk=0; wk<pHostDLs.length; wk++) {
	      gl.glPushMatrix();
	      {
                gl.glTranslated(voffset, y, 0.0);
		gl.glScaled(sTextHeight, sTextHeight, sTextHeight);
		gl.glCallList(pHostDLs[wk]); 
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
        
	/* timing */ 
	if(pShowTiming) {
	  if(pTimingTitleDLs != null) {
	    double py = y;
	    int wk;
	    for(wk=0; wk<pTimingDLs.length; wk++) {
              gl.glPushMatrix();
              {
                gl.glTranslated(toffset, py, 0.0);
                gl.glScaled(sTextHeight, sTextHeight, sTextHeight);
                gl.glCallList(pTimingTitleDLs[wk]); 
              }
              gl.glPopMatrix();

	      py -= sTextHeight;
            }
	  }

          if((pTimingDLs != null) || (pNullDL != null)) {
	    int wk;
	    for(wk=0; wk<2; wk++) {
              gl.glPushMatrix();
              if((pTimingDLs != null) && (pTimingDLs[wk] != -1)) {
                gl.glTranslated(voffset, y, 0.0);
                gl.glScaled(sTextHeight, sTextHeight, sTextHeight);
                gl.glCallList(pTimingDLs[wk]); 
              }
              else if(pNullDL != null) { 
                gl.glTranslated(noffset, y, 0.0);
                gl.glScaled(sTextHeight, sTextHeight, sTextHeight);
                gl.glCallList(pNullDL); 
              }
              gl.glPopMatrix();
              
	      y -= sTextHeight;
            }
          }
	  else {
	    y -= sTextHeight*2.0;
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
  private JQueueJobViewerPanel  pParent; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the graphic is displayed.
   */ 
  private boolean  pIsVisible; 

  /**
   * Whether to display the optional hint components. 
   */ 
  private boolean  pShowToolset; 
  private boolean  pShowAction; 
  private boolean  pShowHost; 
  private boolean  pShowTiming; 

  /**
   * The job details. 
   */ 
  private QueueJob     pJob; 
  private QueueJobInfo pJobInfo; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The OpenGL display list handle for the background geometry.
   */ 
  private Integer  pBackgroundDL; 

  /**
   * The OpenGL display list handle for the target file sequence label. 
   */ 
  private Integer pTargetDL; 
  private double  pTargetWidth; 
  
  /**
   * The OpenGL display list handles for the optional properties. 
   */ 
  private Integer  pNullDL; 
  private double   pTitleWidth; 

  private Integer  pToolsetTitleDL;
  private Integer  pToolsetDL;
  private double   pToolsetWidth; 

  private int[]    pActionTitleDLs; 
  private int[]    pActionDLs; 
  private double   pActionWidth;

  private int[]    pHostTitleDLs; 
  private int[]    pHostDLs; 
  private double   pHostWidth; 
  
  private int[]    pTimingTitleDLs; 
  private int[]    pTimingDLs; 
  private double   pTimingWidth; 
  
}
