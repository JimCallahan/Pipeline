// $Id: ViewerPie.java,v 1.5 2007/06/26 05:18:57 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;

import java.io.*;
import java.util.*;

import javax.media.opengl.*;

/*------------------------------------------------------------------------------------------*/
/*   V I E W E R   P I E                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Renders a Histogram as a pie chart.                                       
 */
public 
class ViewerPie
  extends ViewerGraphic
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constuct a new viewer pie.
   * 
   * @param hist
   *   The histogram to visualize.
   * 
   * @param reverseOrder
   *   Whether to display the histogram catagories in reverse order.
   */ 
  public 
  ViewerPie
  (
   Histogram hist, 
   boolean reverseOrder
  ) 
  {
    super();

    if(hist == null) 
      throw new IllegalArgumentException("The histogram cannot be (null)!");
    pHistogram = hist;

    pReverseOrder = reverseOrder;

    /* compute relative sizes of non-zero slices */ 
    pSlices = new TreeMap<Integer,Double>();
    {
      int size = pHistogram.getNumCatagories(); 
      long total = 0L;
      int wk;
      for(wk=0; wk<size; wk++) 
	total += pHistogram.getCount(wk);
      
      if(total > 0L) {    
	double dtotal = (double) total;
	for(wk=0; wk<size; wk++) {
	  long count = pHistogram.getCount(wk);
	  if(count > 0) 
	    pSlices.put(wk, ((double) count) / dtotal);
	}
      }
    }
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the histogram being visualized.
   */ 
  public String
  getName() 
  {
    return pHistogram.getName();
  }

  /**
   * Get the histogram being visualized.. 
   */ 
  public Histogram
  getHistogram() 
  {
    return pHistogram;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the histogram catagory colors explicitly. 
   */ 
  public void 
  setColors
  (
   Color3d[] colors
  ) 
  {
    if(colors == null) 
      throw new IllegalArgumentException("The colors cannot be (null)!");

    if(pHistogram.getNumCatagories() != colors.length)
      throw new IllegalStateException
	("The number of colors (" + colors.length + ") must be the equal to the number " +
	 "of catagories (" + pHistogram.getNumCatagories() + ") in the histogram!"); 

    pColors = colors; 
  }

  /**
   * Set the histogram catagory colors by interpolating between the given colors.
   */ 
  public void 
  setColors
  (
   Color3d lowColor, 
   Color3d highColor
  ) 
  {
    pColors = new Color3d[pHistogram.getNumCatagories()];
    
    switch(pHistogram.getNumCatagories()) {
    case 0:
      break;

    case 1:
      pColors[0] = lowColor;
      break;

    default: 
      {
	int wk;
	for(wk=0; wk<pColors.length; wk++) {
	  double t = ((double) wk) / ((double) (pColors.length-1));
	  pColors[wk] = Color3d.lerp(lowColor, highColor, t);
	}
      }
    }
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the given position is inside the pie chart.
   */ 
  public boolean
  isInside
  (
   Point2d pos
  ) 
  {              
    return (pPos.distanceSquared(pos) < (sOuterRadius*sOuterRadius));
  }

  /**
   * Whether the given position is inside the center section of the pie chart.
   */ 
  public boolean
  isInsideCenter
  (
   Point2d pos
  ) 
  {              
    return (pPos.distanceSquared(pos) < (sInnerRadius*sInnerRadius));
  }

  /**
   * The index of the pie chart slice which contains the given position or 
   * <CODE>null</CODE> if the point is not inside any slice.
   */ 
  public Integer
  getSliceContaining
  (
   Point2d pos
  ) 
  {             
    if(!isInside(pos) || isInsideCenter(pos))
      return null;

    Vector2d delta = new Vector2d(pPos, pos);
    delta.normalize();

    double theta = Math.atan2(delta.x(), delta.y()); 
    if(theta < 0.0)
      theta += Math.PI * 2.0; 
    
    double t = (theta / Math.PI) * 0.5;
    if(pReverseOrder) 
      t = 1.0 - t; 
      
    double last = 0.0;
    for(Integer idx : pSlices.keySet()) {
      double portion = pSlices.get(idx);

      double next = last + portion;
      if((t >= last) && (t < next))
	return idx;

      last = next;
    }

    return null;
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the bounding box of the pie chart geometry.
   */ 
  public BBox2d
  getBounds()
  {
    BBox2d bbox = new BBox2d(pPos, pPos);
    bbox.bloat(new Vector2d(sOuterRadius, sOuterRadius)); 
    return bbox;
  }

  /**
   * Get the bounding box of all pie chart geometry and labels.
   */
  public BBox2d
  getFullBounds()
  {
    BBox2d bbox = new BBox2d(pPos, pPos);
    bbox.bloat(new Vector2d(3.5, 3.5));
    return bbox;
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
      /* chart title */ 
      if(pTitleDLs == null) {
	String text1 = null;
	String text2 = null;
	{
	  String name = getName();
	  text1 = name;

	  char c[] = name.toCharArray();
	  int wk;
	  for(wk=1; wk<(c.length-1); wk++) {
	    if(Character.isUpperCase(c[wk]) && Character.isLowerCase(c[wk-1])) {
	      text1 = name.substring(0, wk);
	      text2 = name.substring(wk, c.length);
	      break;
	    }
	  }
	}

	if(text2 != null) {
	  pTitleDLs = new int[2];
	  pTitleDLs[1] = mgr.getTextDL(gl, PackageInfo.sGLFont, text2,
				       GeometryMgr.TextAlignment.Center, 0.05);
	}
	else {
	  pTitleDLs = new int[1];
	}
	
	pTitleDLs[0] = mgr.getTextDL(gl, PackageInfo.sGLFont, text1,
				     GeometryMgr.TextAlignment.Center, 0.05);
      }

      /* catagory labels */ 
      if(pLabelDLs == null) {
	int size = pHistogram.getNumCatagories(); 
	pLabelDLs = new int[size];
	pLabelWidths = new double[size];
	int wk;
	for(wk=0; wk<size; wk++) {
	  String text = formatLabel(pHistogram.getRange(wk).toString());
	  pLabelDLs[wk] = mgr.getTextDL(gl, PackageInfo.sGLFont, text, 
			  GeometryMgr.TextAlignment.Center, 0.05);
	  
	  pLabelWidths[wk] = mgr.getTextWidth(PackageInfo.sGLFont, text, 0.05);
	}
      }

      /* the catagory filter rings */ 
      if(pIncludedDLs == null) {
	pIncludedDLs = new Integer[pHistogram.getNumCatagories()];
	if(!pSlices.isEmpty()) {
	  double irad = sInnerRadius - sFilterWidth;
	  double orad = sOuterRadius + sFilterWidth;
	  double last = 0.0;
	  for(Integer idx : pSlices.keySet()) {
	    double portion = pSlices.get(idx);
	    
	    double a = Math.PI * 2.0 * last;
	    double b = Math.PI * 2.0 * (last + portion);
	    
	    pIncludedDLs[idx] = gl.glGenLists(1);
	    gl.glNewList(pIncludedDLs[idx], GL.GL_COMPILE);
	    {
	      gl.glColor4d(0.25, 0.25, 0.25, 1.0); 
	      gl.glBegin(GL.GL_QUAD_STRIP);
	      {
		double inc = Math.toRadians(2.0);
		double theta;
		for(theta=a; theta<b; theta+=inc) {
		  double t = pReverseOrder ? (Math.PI*2.0 - theta) : theta;
		  double x = Math.sin(t);
		  double y = Math.cos(t);
		
		  gl.glVertex2d(x*irad, y*irad);
		  gl.glVertex2d(x*orad, y*orad); 
		}

		{
		  double t = pReverseOrder ? (Math.PI*2.0 - b) : b;
		  double x = Math.sin(t);
		  double y = Math.cos(t);
		  
		  gl.glVertex2d(x*irad, y*irad);
		  gl.glVertex2d(x*orad, y*orad); 
		}
	      }
	      gl.glEnd();

	      gl.glLineWidth(1.0f);
	      gl.glBegin(GL.GL_LINE_LOOP);
	      {
		double inc = Math.toRadians(2.0);
		double theta;

		{
		  double t = pReverseOrder ? (Math.PI*2.0 - a) : a;
		  double x = Math.sin(t);
		  double y = Math.cos(t);
		  
		  gl.glVertex2d(x*orad, y*orad);
		  gl.glVertex2d(x*irad, y*irad);
		}

		for(theta=a; theta<b; theta+=inc) {
		  double t = pReverseOrder ? (Math.PI*2.0 - theta) : theta;
		  double x = Math.sin(t);
		  double y = Math.cos(t);
		
		  gl.glVertex2d(x*irad, y*irad);
		}
		
		{
		  double t = pReverseOrder ? (Math.PI*2.0 - b) : b;
		  double x = Math.sin(t);
		  double y = Math.cos(t);
		  
		  gl.glVertex2d(x*irad, y*irad);
		  gl.glVertex2d(x*orad, y*orad);
		}

		for(theta=b; theta>a; theta-=inc) {
		  double t = pReverseOrder ? (Math.PI*2.0 - theta) : theta;
		  double x = Math.sin(t);
		  double y = Math.cos(t);
		
		  gl.glVertex2d(x*orad, y*orad);
		}
	      }
	      gl.glEnd();
	    }
	    gl.glEndList();

	    last += portion;
	  }
	}
      }

      /* the black color circle */ 
      if(pAnyIncludedDL == null) {
	pAnyIncludedDL = gl.glGenLists(1);

	gl.glNewList(pAnyIncludedDL, GL.GL_COMPILE);
	{
	  gl.glColor4d(0.4, 0.4, 0.4, 1.0); 
	  gl.glBegin(GL.GL_QUAD_STRIP);
	  {      
	    int i; 
	    for(i=0; i<=180; i+=2) {
	      double theta = Math.toRadians((double) i);
	      double x = Math.sin(theta);
	      double y = Math.cos(theta);
	      
	      gl.glVertex2d( x*sInnerRadius, y*sInnerRadius);
	      gl.glVertex2d(-x*sInnerRadius, y*sInnerRadius);
	    }
	  }
	  gl.glEnd();
	}
	gl.glEndList();	  
      }
      
      /* chart title, geometry and catagory labels */ 
      if(pMasterDL == null) {
	pMasterDL = gl.glGenLists(1);

	gl.glNewList(pMasterDL, GL.GL_COMPILE);
	{
	  /* the ring background */ 
	  {
	    gl.glColor4d(0.45, 0.45, 0.45, 1.0); 
	    gl.glBegin(GL.GL_QUAD_STRIP);
	    {      
	      int i; 
	      for(i=0; i<=360; i+=2) {
		double theta = Math.toRadians((double) i);
		double x = Math.sin(theta);
		double y = Math.cos(theta);
		
		gl.glVertex2d(x*sInnerRadius, y*sInnerRadius);
		gl.glVertex2d(x*sOuterRadius, y*sOuterRadius);
	      }
	    }
	    gl.glEnd();	  
	  }

	  /* pie slices */ 
 	  if(!pSlices.isEmpty()) {
 	    double last = 0.0;
 	    for(Integer idx : pSlices.keySet()) {
 	      double portion = pSlices.get(idx);
	      
 	      double a = Math.PI * 2.0 * last;
 	      double b = Math.PI * 2.0 * (last + portion);

 	      Color3d c = pColors[idx];
 	      gl.glColor4d(c.r(), c.g(), c.b(), 1.0); 

 	      gl.glBegin(GL.GL_QUAD_STRIP);
 	      {
 		double inc = Math.toRadians(2.0);
 		double theta;
 		for(theta=a; theta<b; theta+=inc) {
 		  double t = pReverseOrder ? (Math.PI*2.0 - theta) : theta;
 		  double x = Math.sin(t);
 		  double y = Math.cos(t);
		
 		  gl.glVertex2d(x*sInnerRadius, y*sInnerRadius);
 		  gl.glVertex2d(x*sOuterRadius, y*sOuterRadius);
 		}

 		{
 		  double t = pReverseOrder ? (Math.PI*2.0 - b) : b;
 		  double x = Math.sin(t);
 		  double y = Math.cos(t);
		  
 		  gl.glVertex2d(x*sInnerRadius, y*sInnerRadius);
 		  gl.glVertex2d(x*sOuterRadius, y*sOuterRadius);
 		}
 	      }
 	      gl.glEnd();

 	      last += portion;
 	    }
 	  }

	  /* the borders */ 
	  {
	    gl.glColor4d(1.0, 1.0, 1.0, 1.0); 
	    gl.glLineWidth(2.0f);
	    gl.glBegin(GL.GL_LINE_LOOP);
	    {      
	      int i; 
	      for(i=0; i<=360; i+=2) {
		double theta = Math.toRadians((double) i);
		double t = pReverseOrder ? (Math.PI*2.0 - theta) : theta;
		double x = Math.sin(t);
		double y = Math.cos(t);
		
		gl.glVertex2d(x*sInnerRadius, y*sInnerRadius);
	      }
	    }
	    gl.glEnd();	  
	    
	    gl.glBegin(GL.GL_LINE_LOOP);
	    {      
	      int i; 
	      for(i=0; i<=360; i+=2) {
		double theta = Math.toRadians((double) i);
		double t = pReverseOrder ? (Math.PI*2.0 - theta) : theta;
		double x = Math.sin(t);
		double y = Math.cos(t);
		
		gl.glVertex2d(x*sOuterRadius, y*sOuterRadius);
	      }
	    }
	    gl.glEnd();	  

	    if(!pSlices.isEmpty()) {
	      gl.glBegin(GL.GL_LINES);
	      {      
		double last = 0.0;
		for(Integer idx : pSlices.keySet()) {
		  double portion = pSlices.get(idx);

		  double theta = Math.PI * 2.0 * last;
		  double t = pReverseOrder ? (Math.PI*2.0 - theta) : theta;
		  double x = Math.sin(t);
		  double y = Math.cos(t);
		  
		  gl.glVertex2d(x*sInnerRadius, y*sInnerRadius);
		  gl.glVertex2d(x*sOuterRadius, y*sOuterRadius);

		  last += portion;
		}
	      }
	      gl.glEnd();	
	    }
	  }

	  /* char title */ 
	  {
	    gl.glColor4d(1.0, 1.0, 1.0, 1.0); 
	    double tscale = 0.475;
	    switch(pTitleDLs.length) {
	    case 1:
	      gl.glPushMatrix();
	      {
		gl.glTranslated(0.0, -0.125, 0.0);
		gl.glScaled(tscale, tscale, tscale);
		gl.glCallList(pTitleDLs[0]);
	      }
	      gl.glPopMatrix();
	      break;
	    
	    case 2:
	      gl.glPushMatrix();
	      {
		gl.glTranslated(0.0, 0.1, 0.0);
		gl.glScaled(tscale, tscale, tscale);
		gl.glCallList(pTitleDLs[0]);
	      }
	      gl.glPopMatrix();
	      
	      gl.glPushMatrix();
	      {
		gl.glTranslated(0.0, -0.40, 0.0);
		gl.glScaled(tscale, tscale, tscale);
		gl.glCallList(pTitleDLs[1]);
	      }
	      gl.glPopMatrix();
	    }
	  }

	  /* catagory labels */ 
	  if(!pSlices.isEmpty()) {
	    double gap = sFilterWidth + sLabelGap; 
	    double tscale = 0.325;
	    double last = 0.0;
	    for(Integer idx : pSlices.keySet()) {
	      double portion = pSlices.get(idx);
		
	      double theta = Math.PI * 2.0 * (last + portion*0.5);
	      double t = pReverseOrder ? (Math.PI*2.0 - theta) : theta;
	      double x = Math.sin(t);
	      double y = Math.cos(t);

	      double sx = sOuterRadius + gap + pLabelWidths[idx]*0.5*tscale;
	      double sy = sOuterRadius + gap + 0.4;

	      gl.glPushMatrix();
	      {
		gl.glTranslated(x*sx, y*sy, 0.0);
		gl.glScaled(tscale, tscale, tscale);
		gl.glTranslated(0.0, -0.325, 0.0);
 		gl.glCallList(pLabelDLs[idx]);	  
	      }
	      gl.glPopMatrix();
	      
	      last += portion;
	    }
	  }
	}
	gl.glEndList();
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
    gl.glPushMatrix();
    {
      gl.glTranslated(pPos.x(), pPos.y(), 0.0);

      /* any included background */ 
      if((pAnyIncludedDL != null) && pHistogram.anyIncluded()) 
	gl.glCallList(pAnyIncludedDL);

      /* the catagory filter graphics */ 
      if(pIncludedDLs != null) {
	for(Integer idx : pSlices.keySet()) {
	  if(pHistogram.isIncluded(idx))
	    gl.glCallList(pIncludedDLs[idx]); 
	}
      }

      /* chart title, geometry and catagory labels */ 
      if(pMasterDL != null) 
	gl.glCallList(pMasterDL);
    }
    gl.glPopMatrix();
  }
  
  
  /**
   * Perform any modifications of the histogram catagory label required.
   */ 
  protected String
  formatLabel
  (
   String text
  ) 
  {
    return text;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The inner/outer radius of the pie ring.
   */ 
  private static final  double  sInnerRadius = 1.2;
  private static final  double  sOuterRadius = 2.0;
  private static final  double  sLabelGap    = 0.15; 
  private static final  double  sFilterWidth = 0.10; 
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The histogram to visualize.
   */ 
  private Histogram  pHistogram; 

  /**
   * Whether to display the histogram catagories in reverse order.
   */ 
  private boolean  pReverseOrder; 

  /**
   * The relative sizes of non-zero slices in the [0,1] range by slice index.
   */ 
  private TreeMap<Integer,Double>  pSlices; 


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The OpenGL display list handle for everthing. 
   */ 
  private Integer  pMasterDL;
  
  /**
   * The OpenGL display list handle for the pie chart title. 
   */ 
  private int[]    pTitleDLs; 

  /**
   * The OpenGL display list handles and widths for the catagory labels.
   */ 
  private int[]     pLabelDLs; 
  private double[]  pLabelWidths;

  /**
   * The OpenGL display list handle for the any included icon.
   */ 
  private Integer pAnyIncludedDL; 

  /**
   * The OpenGL display list handles for geometry displayed for each catagory when included
   * in the matching set for the histogram.
   */ 
  private Integer[]  pIncludedDLs; 

  /**
   * The colors for each histogram catagory.
   */ 
  private Color3d[]  pColors;
  

}
