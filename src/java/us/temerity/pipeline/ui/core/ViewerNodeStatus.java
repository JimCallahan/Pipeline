// $Id: ViewerNodeStatus.java,v 1.1 2006/12/07 05:18:25 jim Exp $

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
    pFileState     = null; 
    pLinkState     = null;

    if(status == null)
      return;

    NodeDetails details = status.getDetails();
    if(details == null) 
      return;

    pVersionState  = details.getVersionState();
    pPropertyState = details.getPropertyState();
    pLinkState     = details.getLinkState();

    pFileState = FileState.Identical;
    switch(details.getVersionState()) {
    case Pending:
      pFileState = FileState.Pending;
      break;

    case CheckedIn:
      pFileState = FileState.CheckedIn;
      break;

    default:
      {
	boolean anyNeedsCheckOut = false;
	boolean anyObsolete      = false;
	boolean anyModified      = false;
	boolean anyAdded         = false;
	boolean anyConflicted    = false;
	boolean anyMissing       = false;

	for(FileSeq fseq : details.getFileStateSequences()) {
	  FileState fs[] = details.getFileState(fseq);
	  int wk;
	  for(wk=0; wk<fs.length; wk++) {
	    switch(fs[wk]) {
	    case NeedsCheckOut:
	      anyNeedsCheckOut = true;
	      break;
	      
	    case Obsolete:
	      anyObsolete = true;
	      break;

	    case Modified:
	      anyModified = true;
	      break; 

	    case Added:
	      anyAdded = true;
	      break; 
	      
	    case Conflicted:
	      anyConflicted = true;	
	      break;
	      
	    case Missing: 
	      anyMissing = true;
	    }
	  }
	}
	
	if(anyMissing) 
	  pFileState = FileState.Missing;
	else if(anyConflicted) 
	  pFileState = FileState.Conflicted;
	else if(anyModified) 
	  pFileState = FileState.Modified;
	else if(anyAdded) 
	  pFileState = FileState.Added;
	else if(anyObsolete) 
	  pFileState = FileState.Obsolete;
	else if(anyNeedsCheckOut) 
	  pFileState = FileState.NeedsCheckOut;
      }
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
      /* background */ 
      if(pBackgroundDL == null) 
	pBackgroundDL = 
	  mgr.getRoundedRectDL(gl, 3.4+sBorder*2.0, sTextHeight*4.0+sBorder*2.0, sBorder, 
			       new Color4d(0.45, 0.45, 0.45, 0.85), 
			       new Color4d(0.65, 0.65, 0.65, 1.0), 2.0f); 

      /* state titles */ 
      if(pTitlesDL == null) {
	pTitlesDL = gl.glGenLists(1);

	String titles[] = { "Version:", "Property:", "File:", "Link:" };
	int titleDLs[] = new int[titles.length];

	int wk;
	for(wk=0; wk<titles.length; wk++) {
	  titleDLs[wk] = 
	    mgr.getTextDL(gl, PackageInfo.sGLFont, titles[wk],
			  GeometryMgr.TextAlignment.Right, 0.05);
	}

	gl.glNewList(pTitlesDL, GL.GL_COMPILE);
	{
	  double y;
	  for(wk=0, y=0.0; wk<titles.length; wk++, y-=sTextHeight) {
	    gl.glPushMatrix();
	    {
	      gl.glTranslated(-0.1, y, 0.0);
	      gl.glScaled(0.35, 0.35, 0.35);
	      gl.glCallList(titleDLs[wk]);
	    }
	    gl.glPopMatrix();
	  }
	}
	gl.glEndList();	  
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

      /* file state labels */ 
      if(pFileStateDLs == null) {
	ArrayList<FileState> states = FileState.all();
	pFileStateDLs = new int[states.size()];

	int wk;
	for(wk=0; wk<pFileStateDLs.length; wk++) {
	  pFileStateDLs[wk] = 
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

    if((pVersionState != null) &&  
       (pPropertyState != null) && 
       (pFileState != null) &&  
       (pLinkState != null)) {

      gl.glPushMatrix();
      {
	gl.glTranslated(pPos.x(), pPos.y()-0.85, 0.0);
	gl.glScaled(sTextScale, sTextScale, sTextScale);
	
	if(pBackgroundDL != null) {
	  gl.glPushMatrix();
	  {
	    gl.glTranslated(0.0, -1.2*sTextHeight, 0.0);
	    gl.glCallList(pBackgroundDL);
	  }
	  gl.glPopMatrix();
	}

	gl.glColor4d(1.0, 1.0, 1.0, 1.0); 
	if(pTitlesDL != null)
	  gl.glCallList(pTitlesDL);
	
	gl.glTranslated(0.1, 0, 0.0);

	if(pVersionStateDLs != null) {
	  gl.glPushMatrix();
	  {
	    gl.glTranslated(-0.1, 0.0, 0.0);
	    gl.glScaled(0.35, 0.35, 0.35);
	    gl.glCallList(pVersionStateDLs[pVersionState.ordinal()]);
	  }
	  gl.glPopMatrix();
	}

	if(pPropertyStateDLs != null) {
	  gl.glPushMatrix();
	  {
	    gl.glTranslated(-0.1, -sTextHeight, 0.0);
	    gl.glScaled(0.35, 0.35, 0.35);
	    gl.glCallList(pPropertyStateDLs[pPropertyState.ordinal()]);
	  }
	  gl.glPopMatrix();
	}

	if(pFileStateDLs != null) {
	  gl.glPushMatrix();
	  {
	    gl.glTranslated(-0.1, -sTextHeight*2.0, 0.0);
	    gl.glScaled(0.35, 0.35, 0.35);
	    gl.glCallList(pFileStateDLs[pFileState.ordinal()]);
	  }
	  gl.glPopMatrix();
	}

	if(pLinkStateDLs != null) {
	  gl.glPushMatrix();
	  {
	    gl.glTranslated(-0.1, -sTextHeight*3.0, 0.0);
	    gl.glScaled(0.35, 0.35, 0.35);
	    gl.glCallList(pLinkStateDLs[pLinkState.ordinal()]);
	  }
	  gl.glPopMatrix();
	}
      }
      gl.glPopMatrix();
    }
    else {

      // ...
      
    }
  }  



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

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
   * The node states. 
   */ 
  private VersionState   pVersionState; 
  private PropertyState  pPropertyState; 
  private FileState      pFileState; 
  private LinkState      pLinkState;      


  /*----------------------------------------------------------------------------------------*/

  /**
   * The OpenGL display list handle for the background geometry.
   */ 
  private Integer  pBackgroundDL; 

  /**
   * The OpenGL display list handle for state titles.
   */ 
  private Integer  pTitlesDL; 
  
  /**
   * The OpenGL display list handle for the state values.
   */ 
  private int[]  pVersionStateDLs;   
  private int[]  pPropertyStateDLs;  
  private int[]  pFileStateDLs; 	  
  private int[]  pLinkStateDLs;      

}
