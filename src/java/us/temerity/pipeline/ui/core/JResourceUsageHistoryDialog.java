// $Id: JResourceUsageHistoryDialog.java,v 1.4 2005/01/30 08:50:30 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.ui.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
import java.text.*;
import javax.swing.*;
import javax.swing.event.*;

import net.java.games.jogl.*;

/*------------------------------------------------------------------------------------------*/
/*   R E S O U R C E   U S A G E   H I S T O R Y   D I A L O G                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A dialog which displays the complete resource usage history for a job server.
 */ 
public  
class JResourceUsageHistoryDialog 
  extends JDialog
  implements MouseListener, MouseMotionListener, GLEventListener, KeyListener, ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new dialog.
   * 
   * @param hostname
   *   The fully resolved name of the job server.
   * 
   * @param block
   *   The resource usage samples.
   */ 
  public 
  JResourceUsageHistoryDialog() 
  {
    super((JFrame) null, "Resource Usage History", false);

    /* initialize fields */ 
    {
      pSamples = new TreeMap<String,ResourceSampleBlock>();

      pRefreshTimeScale = true;
      pRefreshGraph     = true;

      pLoadDLs   = new TreeMap<String,Integer>();
      pLoadSpans = new TreeMap<String,Vector2d>();

      pTranslate = new Point2d();

      pMinScale = new Vector2d(0.1, 20.0);
      pMaxScale = new Vector2d(15.0, 200.0);
      pScale    = Vector2d.lerp(pMinScale, pMaxScale, 0.25);

      pBorder = new Vector2d(200.0, 56.0);
    }

    /* initialize the dialog components */     
    {
      JPanel root = new JPanel();
      root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));  

      {
	JPanel panel = new JPanel();
	panel.setName("DialogHeader");	
	panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
	
	{
	  JLabel label = new JLabel("Resource Usage History:");
	  label.setName("DialogHeaderLabel");	
	  
	  panel.add(label);	  
	}
	
	panel.add(Box.createHorizontalGlue());

	{
	  JToggleButton btn = new JToggleButton();		
	  pLoadButton = btn;
	  btn.setName("CpuButton");
		  
	  Dimension size = new Dimension(26, 19);
	  btn.setMinimumSize(size);
	  btn.setMaximumSize(size);
	  btn.setPreferredSize(size);
	  
	  btn.setSelected(true);
	  btn.setActionCommand("refresh");
	  btn.addActionListener(this);

	  btn.setToolTipText(UIFactory.formatToolTip
	    ("Toggle display of system load graphs."));

	  panel.add(btn);
	} 
	
	panel.add(Box.createRigidArea(new Dimension(15, 0)));
	
	{
	  JToggleButton btn = new JToggleButton();		
	  pMemButton = btn;
	  btn.setName("MemButton");
		  
	  Dimension size = new Dimension(26, 19);
	  btn.setMinimumSize(size);
	  btn.setMaximumSize(size);
	  btn.setPreferredSize(size);
	  
	  btn.setSelected(true);
	  btn.setActionCommand("refresh");
	  btn.addActionListener(this);

	  btn.setToolTipText(UIFactory.formatToolTip
	    ("Toggle display of availble free memory graphs."));

	  panel.add(btn);
	} 
	
	panel.add(Box.createRigidArea(new Dimension(15, 0)));
	
	{
	  JToggleButton btn = new JToggleButton();		
	  pDiskButton = btn;
	  btn.setName("DiskButton");
		  
	  Dimension size = new Dimension(26, 19);
	  btn.setMinimumSize(size);
	  btn.setMaximumSize(size);
	  btn.setPreferredSize(size);
	  
	  btn.setSelected(true);
	  btn.setActionCommand("refresh");
	  btn.addActionListener(this);

	  btn.setToolTipText(UIFactory.formatToolTip
	    ("Toggle display of available free temporary disk space graphs."));

	  panel.add(btn);
	} 
	
	panel.add(Box.createRigidArea(new Dimension(25, 0)));
	
	{
	  JToggleButton btn = new JToggleButton();		
	  pJobButton = btn;
	  btn.setName("JobButton");
		  
	  Dimension size = new Dimension(26, 19);
	  btn.setMinimumSize(size);
	  btn.setMaximumSize(size);
	  btn.setPreferredSize(size);
	  
	  btn.setSelected(true);
	  btn.setActionCommand("refresh");
	  btn.addActionListener(this);

	  btn.setToolTipText(UIFactory.formatToolTip
	    ("Toggle display of running job count graphs."));

	  panel.add(btn);
	} 
	
	panel.add(Box.createRigidArea(new Dimension(15, 0)));

	root.add(panel);
      }
    
      {
	JPanel panel = new JPanel();
	panel.setLayout(new BorderLayout());

	Dimension size = new Dimension((int) (pBorder.x()*5.0), (int) (pBorder.y()*5.0));

	{
	  GLCanvas canvas = UIMaster.getInstance().createGLCanvas(); 
	  pCanvas = canvas;
	  
	  canvas.addGLEventListener(this);
	  canvas.addMouseListener(this);
	  canvas.addMouseMotionListener(this);
	  canvas.setFocusable(true);	
	  canvas.addKeyListener(this);

	  canvas.setSize(size);
	  
	  panel.add(canvas);
	}

	panel.setMinimumSize(size);
	panel.setPreferredSize(size);
	panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
	
	root.add(panel);
      }

      setContentPane(root);
      pack();
    }
  }
    


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Set the graph scale factor.
   */ 
  private void 
  setScale
  (
   Vector2d scale
  ) 
  {
    pScale = scale;
    pScale.clamp(pMinScale, pMaxScale);
    pRefreshTimeScale = true;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the resource usage history samples.
   */ 
  public synchronized void 
  updateSamples
  (
   TreeMap<String,ResourceSampleBlock> samples   
  ) 
  {
    pSamples.clear();
    pSamples.putAll(samples);

    doRefresh();
  }

  /**
   * Update the panel to reflect new user preferences.
   */ 
  public void 
  updateUserPrefs() 
  {
    doRefresh();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- GL EVENT LISTENER METHODS -----------------------------------------------------------*/

  /**
   * Called by the drawable immediately after the OpenGL context is initialized for the 
   * first time.
   */ 
  public void 
  init
  (
   GLDrawable drawable
  )
  {    
    if(UIMaster.getInstance().getDebugGL()) 
      drawable.setGL(new DebugGL(drawable.getGL()));

    if(UIMaster.getInstance().getTraceGL()) 
      drawable.setGL(new TraceGL(drawable.getGL(), System.err));

    GL gl = drawable.getGL();

    /* global OpenGL state */ 
    {
      gl.glClearColor(0.5f, 0.5f, 0.5f, 0.0f);
      gl.glEnable(GL.GL_BLEND);
      gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
    }
  }

  /**
   * Called by the drawable to initiate OpenGL rendering by the client.
   */ 
  public synchronized void 
  display
  (
   GLDrawable drawable
  )
  {
    UserPrefs prefs = UserPrefs.getInstance();
    GL gl = drawable.getGL();

    gl.glMatrixMode(GL.GL_MODELVIEW);
    gl.glLoadIdentity();

    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

    /* build display lists */ 
    {
      /* 1-minute tick marks (1-hour block) */ 
      if(pOneMinDL == null) {
	pOneMinDL = UIMaster.getInstance().getDisplayList(gl);
	gl.glNewList(pOneMinDL, GL.GL_COMPILE_AND_EXECUTE);
	{
	  gl.glColor3d(1.0, 1.0, 1.0);
	  gl.glBegin(gl.GL_LINES);
	  {
	    int wk;
	    for(wk=1; wk<60; wk++) {
	      if((wk % 5) != 0) {
		gl.glVertex2d((double) wk, 0.0);
		gl.glVertex2d((double) wk, 1.0);
	      }	      
	    }
	  }
	  gl.glEnd();
	}
	gl.glEndList();
      }

      /* 5-minute tick marks (1-hour block) */ 
      if(pFiveMinDL == null) {
	pFiveMinDL = UIMaster.getInstance().getDisplayList(gl);
	gl.glNewList(pFiveMinDL, GL.GL_COMPILE_AND_EXECUTE);
	{
	  gl.glColor3d(1.0, 1.0, 1.0);
	  gl.glBegin(gl.GL_LINES);
	  {
	    int wk;
	    for(wk=5; wk<60; wk+=5) {
	      if((wk % 15) != 0) {
		gl.glVertex2d((double) wk, 0.0);
		gl.glVertex2d((double) wk, 1.0);
	      }	      
	    }
	  }
	  gl.glEnd();
	}
	gl.glEndList();
      }
      
      /* 15-minute tick marks (1-hour block) */ 
      if(pFifteenMinDL == null) {
	pFifteenMinDL = UIMaster.getInstance().getDisplayList(gl);
	gl.glNewList(pFifteenMinDL, GL.GL_COMPILE_AND_EXECUTE);
	{
	  gl.glColor3d(1.0, 1.0, 1.0);
	  gl.glBegin(gl.GL_LINES);
	  {
	    int wk;
	    for(wk=15; wk<60; wk+=15) {
	      if((wk % 60) != 0) {
		gl.glVertex2d((double) wk, 0.0);
		gl.glVertex2d((double) wk, 1.0);
	      }	      
	    }
	  }
	  gl.glEnd();
	}
	gl.glEndList();
      }
    }

    /* build the graph display lists */ 
    if(pRefreshGraph) {
      Stack<Integer> oldDLs = new Stack<Integer>();
      oldDLs.addAll(pLoadDLs.values());
      
      pLoadDLs.clear();
      pLoadSpans.clear();
      
      if(!pSamples.isEmpty()) {
	pMinTime = null;
	for(ResourceSampleBlock block : pSamples.values()) {
	  Date stamp = block.getStartTimeStamp();
	  if((pMinTime == null) || (stamp.compareTo(pMinTime) < 0))
	    pMinTime = stamp;
	}
	  
	double dy = 0.0;
	for(String hname : pSamples.keySet()) {
	  ResourceSampleBlock block = pSamples.get(hname);
	  if(block != null) {
	    Date first = block.getStartTimeStamp();
	    Date last  = block.getEndTimeStamp();
	      
	    /* system load graphs */ 
	    if(pLoadButton.isSelected() && (block.getNumSamples() > 0)) {
	      double offset = ((double) (first.getTime() - pMinTime.getTime())) / 60000.0;
	      double range  = ((double) (last.getTime() - first.getTime())) / 60000.0;
		
	      Vector2d span = new Vector2d(offset, range+1.0);
	      pLoadSpans.put(hname, span);
		
	      Integer dl = null;
	      if(oldDLs.isEmpty()) 
		dl = UIMaster.getInstance().getDisplayList(gl);
	      else 
		dl = oldDLs.pop();
	      pLoadDLs.put(hname, dl);			    
	      
	      double maxLoad = (double) prefs.getSystemLoadRange();

	      gl.glNewList(dl, GL.GL_COMPILE);
	      {
		gl.glBegin(gl.GL_QUADS);
		{
		  /* background */ 
		  {
		    Color3d color = prefs.getSystemLoadBgColor();
		    gl.glColor3d(color.r(), color.g(), color.b());
		    
		    gl.glVertex2d(0.0,      0.0-sGraphBorder);
		    gl.glVertex2d(span.y(), 0.0-sGraphBorder);
		    gl.glVertex2d(span.y(), 1.0+sGraphBorder);
		    gl.glVertex2d(0.0,      1.0+sGraphBorder);
		  }

		  /* graph */ 
		  {
		    Color3d fg = prefs.getSystemLoadFgColor();
		    Color3d oc = prefs.getSystemLoadOverflowColor();

		    boolean over = false;
		    gl.glColor3d(fg.r(), fg.g(), fg.b());

		    int wk;
		    for(wk=0; wk<block.getNumSamples(); wk++) {
		      double load = ((double) block.getLoad(wk)) / maxLoad;
		      if(load > 0.0) {
			if(load > 1.0) {
			  if(!over) {
			    gl.glColor3d(oc.r(), oc.g(), oc.b());
			    over = true;
			  }
			}
			else if(over) {
			  gl.glColor3d(fg.r(), fg.g(), fg.b());
			  over = false;
			}

			load = Math.min(load, 1.0);

			gl.glVertex2d((double) wk,     0.0);
			gl.glVertex2d((double) (wk+1), 0.0);
			gl.glVertex2d((double) (wk+1), load);
			gl.glVertex2d((double) wk,     load);
		      }
		    }
		  }
		}
		gl.glEnd();

		/* number of procs level */ 
		{
		  // MOVE THIS TO GRAPH LABEL SECITON...

		  double procs = (double) block.getNumProcessors();
		  double v = procs / maxLoad; 
		  if(v <= 1.0) {
		    gl.glBegin(gl.GL_LINES);
		    {
		      Color3d color = prefs.getFullLoadColor();
		      gl.glColor3d(color.r(), color.g(), color.b());
		      
		      gl.glVertex2d(0.0,      v);
		      gl.glVertex2d(span.y(), v);
		    }
		    gl.glEnd();
		  }
		}
	      }
	      gl.glEndList();
	    }
	      
	      
	      
	    // ...

	    dy += 1.0 + sGraphBorder*2.0 + sGraphGap; 
	  }
	}
      }
	
      while(!oldDLs.isEmpty()) 
	UIMaster.getInstance().freeDisplayList(oldDLs.pop());
	
      pRefreshGraph = false;
    }
  
    /* compute the bounding box and time range of all graph geometry */ 
    {
      Date startStamp = null;
      Date endStamp   = null;
      pBBox = null;
      double dy = 0.0;
      for(String hname : pSamples.keySet()) {
	Integer dl = pLoadDLs.get(hname);
	Vector2d span = pLoadSpans.get(hname);
	if((dl != null) && (span != null)) {
	  BBox2d bbox = new BBox2d(new Point2d(span.x(),          dy+0.0-sGraphBorder), 
				   new Point2d(span.x()+span.y(), dy+1.0+sGraphBorder));
	  if(pBBox == null) 
	    pBBox = bbox;
	  else 
	    pBBox.grow(bbox);	

	  dy += 1.0 + sGraphBorder*2.0 + sGraphGap; 
	}
	
	ResourceSampleBlock block = pSamples.get(hname);
	if((startStamp == null) || (block.getStartTimeStamp().compareTo(startStamp) < 0)) 
	  startStamp = block.getStartTimeStamp();
	if((endStamp == null) || (block.getEndTimeStamp().compareTo(endStamp) > 0)) 
	  endStamp = block.getEndTimeStamp();
      }

      {
	Calendar cal = Calendar.getInstance();
	cal.setTime(startStamp);
	cal.set(Calendar.MILLISECOND, 0);
	cal.set(Calendar.SECOND, 0);
	cal.set(Calendar.MINUTE, 0);
	cal.set(Calendar.HOUR_OF_DAY, 0);
	pStartDay = cal.getTime();
      }

      {
	Calendar cal = Calendar.getInstance();
	cal.setTime(endStamp);
	cal.set(Calendar.MILLISECOND, 0);
	cal.set(Calendar.SECOND, 0);
	cal.set(Calendar.MINUTE, 0);
	cal.set(Calendar.HOUR_OF_DAY, 0);
	pEndDay = cal.getTime();
      }
    }

    /* render the graph geometry */ 
    if(pBBox != null) {
      gl.glPushMatrix();
      {
	gl.glTranslated(pTranslate.x(), pTranslate.y(), 0.0);

	Point2d center = pBBox.getCenter();
	gl.glScaled(pScale.x(), pScale.y(), 1.0);
	gl.glTranslated(-center.x(), -center.y(), 0.0);
	
	double dy = 0.0;
	for(String hname : pSamples.keySet()) {
	  Integer dl = pLoadDLs.get(hname);
	  Vector2d span = pLoadSpans.get(hname);
	  if((dl != null) && (span != null)) {
	    gl.glPushMatrix();
	    {
	      gl.glTranslated(span.x(), dy, 0.0);
	      gl.glCallList(dl);
	    }
	    gl.glPopMatrix();
	    
	    dy += 1.0 + sGraphBorder*2.0 + sGraphGap; 
	  }
	}
      }
      gl.glPopMatrix();
    }

    /* render the time scale */ 
    gl.glPushMatrix();
    {
      /* background */ 
      {
	Point2d a = new Point2d(-pGraphArea.x(), pGraphArea.y());
	Point2d b = new Point2d(pGraphArea.x(), pGraphArea.y()+pBorder.y());
	Vector2d s = new Vector2d(a, b);
	
	gl.glBegin(gl.GL_QUADS);
	{
	  gl.glColor3d(0.4, 0.4, 0.4);
	  
	  gl.glVertex2d(a.x(), b.y()); 
	  gl.glVertex2d(b.x(), b.y()); 
	  gl.glVertex2d(b.x(), a.y()); 
	  gl.glVertex2d(a.x(), a.y()); 
	}
	gl.glEnd();
	
	gl.glBegin(gl.GL_LINES);
	{
	  gl.glColor3d(1.0, 1.0, 1.0);
	  
	  gl.glVertex2d(a.x(), a.y());
	  gl.glVertex2d(b.x(), a.y());
	}
	gl.glEnd();
      }

      gl.glTranslated(pTranslate.x(), pGraphArea.y(), 0.0);
      
      if(pBBox != null) {
	if(pRefreshTimeScale) {
	  /* offset and range of tick mark geometry */ 
	  double offset = ((double) (pStartDay.getTime() - pMinTime.getTime())) / 60000.0;
	  double range  = ((double) (pEndDay.getTime() - pStartDay.getTime())) / 60000.0;
	
	  /* scale of tick marks */ 
	  boolean sized = false;
	  double sfive    = 0.25;
	  double sfifteen = 0.5;
	  double shour    = 0.5;
	  double sthree   = 0.5;
	  double ssix     = 0.5;
	  double stwelve  = 0.5;
	
	  /* compute the level of detail for tick marks and labels */ 
	  TickLevel level = TickLevel.OneDay;
	  int binc = 1440;
	  if(pScale.x() > (10.0/1.0)) {
	    level = TickLevel.OneMinute;
	    binc = 15;
	  }
	  else if(pScale.x() > (10.0/2.5)) {
	    level = TickLevel.FiveMinutes;
	    binc = 30;	  
	  }
	  else if(pScale.x() > (10.0/5.0)) {
	    level = TickLevel.FiveMinutes;
	    binc = 60;
	  }
	  else if(pScale.x() > (10.0/15.0)) {
	    level = TickLevel.FifteenMinutes;
	    binc = 180;
	  }
	  else if(pScale.x() > (10.0/60.0)) {
	    level = TickLevel.OneHour;
	    binc = 360;
	  }
	  else if(pScale.x() > (10.0/180.0)) {
	    level = TickLevel.ThreeHours;
	    binc = 720;
	  }
	
	  /* build label display lists */ 
	  ArrayList<Integer> dateLabelDLs = new ArrayList<Integer>();
	  ArrayList<Integer> timeLabelDLs = new ArrayList<Integer>();
	  double timeLabelInc = 0.0;
	  try {
	    GeometryMgr mgr = GeometryMgr.getInstance();

	    Date endDate = null;
	    {
	      Calendar cal = Calendar.getInstance();
	      cal.setTime(pEndDay);
	      cal.add(Calendar.DAY_OF_YEAR, 1);
	      endDate = cal.getTime();
	    }
	  
	    /* date labels */ 
	    {
	      Calendar cal = Calendar.getInstance();
	      cal.setTime(pStartDay);
	    
	      while(cal.getTime().compareTo(endDate) < 0) {
		String text = sDateFormat.format(cal.getTime());
		int dl = mgr.getTextDL(gl, "CharterBTRoman", text, 
				       GeometryMgr.TextAlignment.Left, 0.05);
		dateLabelDLs.add(dl);
		cal.add(Calendar.DAY_OF_YEAR, 1);
	      }
	    } 

	    /* time labels */ 
	    {
	      int minc;
	      switch(level) {
	      case OneMinute: 
		minc = 15;
		break;
	      
	      case FiveMinutes: 
		minc = 60;
		break;
	      
	      case FifteenMinutes:
		minc = 180;
		break;
	      
	      case OneHour:
		minc = 360;
		break;
	      
	      default:
		minc = 720;
	      }
	    
	      timeLabelInc = (double) minc;

	      Calendar cal = Calendar.getInstance();
	      cal.setTime(pStartDay);
	    
	      while(cal.getTime().compareTo(endDate) < 0) {
		String text = sTimeFormat.format(cal.getTime());
		int dl = mgr.getTextDL(gl, "CharterBTRoman", text, 
				       GeometryMgr.TextAlignment.Left, 0.05);
		timeLabelDLs.add(dl);
		cal.add(Calendar.MINUTE, minc);
	      }
	    }
	  }
	  catch(IOException ex) {
	    UIMaster.getInstance().showErrorDialog(ex);
	  }
	
	  if(pTimeScaleDL == null) 
	    pTimeScaleDL = UIMaster.getInstance().getDisplayList(gl);
	  
	  gl.glNewList(pTimeScaleDL, GL.GL_COMPILE_AND_EXECUTE);
	  {
	    /* tick marks and graph bars */ 
	    gl.glPushMatrix();
	    {
	      Point2d center = pBBox.getCenter();
	      gl.glScaled(pScale.x(), pBorder.y(), 1.0);
	      gl.glTranslated(-center.x(), 0.0, 0.0);
	
	      int mins = ((int) range) + 1440;

	      /* graph time bars */ 
	      {	  
		gl.glColor3d(0.8, 0.8, 0.8);
		gl.glBegin(gl.GL_LINES);
		{
		  double y = (pGraphArea.y() * -2.0) / pBorder.y();

		  int wk;
		  for(wk=0; wk<=mins; wk+=binc) {
		    double x = ((double) wk) + offset;
		    gl.glVertex2d(x, 0.0);
		    gl.glVertex2d(x, y);
		  }
		}
		gl.glEnd();
	      }

	      switch(level) {
		/* 1-minute tick marks */ 
	      case OneMinute:     
		{
		  sized = true;

		  gl.glPushMatrix();
		  {
		    gl.glScaled(1.0, 0.125, 1.0);
		    gl.glTranslated(offset, 0.0, 0.0);
	      
		    int wk;
		    for(wk=0; wk<mins; wk+=60) {
		      gl.glCallList(pOneMinDL);
		      gl.glTranslated(60.0, 0.0, 0.0);
		    }
		  }
		  gl.glPopMatrix();
		}

		/* 5-minute tick marks */ 
	      case FiveMinutes:
		{
		  if(!sized) {
		    sfive    = 0.125;
		    sfifteen = 0.25;
		  }

		  sized = true;

		  gl.glPushMatrix();
		  {
		    gl.glScaled(1.0, sfive, 1.0);
		    gl.glTranslated(offset, 0.0, 0.0);
	      
		    int wk;
		    for(wk=0; wk<mins; wk+=60) {
		      gl.glCallList(pFiveMinDL);
		      gl.glTranslated(60.0, 0.0, 0.0);
		    }
		  }
		  gl.glPopMatrix();
		}
	
		/* 15-minute tick marks */ 
	      case FifteenMinutes:
		{   
		  if(!sized) {
		    sfifteen = 0.125;
		    shour    = 0.25;
		  }

		  sized = true;

		  gl.glPushMatrix();
		  {
		    gl.glScaled(1.0, sfifteen, 1.0);
		    gl.glTranslated(offset, 0.0, 0.0);
	      
		    int wk;
		    for(wk=0; wk<mins; wk+=60) {
		      gl.glCallList(pFifteenMinDL);
		      gl.glTranslated(60.0, 0.0, 0.0);
		    }
		  }
		  gl.glPopMatrix();
		}	  

		/* 1-hour tick marks */ 
	      case OneHour:
	      case ThreeHours:
		{
		  int hinc = 60;
		  if(!sized) {
		    switch(level) {
		    case OneHour:
		      shour   = 0.125;
		      sthree  = 0.25;
		      hinc    = 60;
		      break;

		    case ThreeHours:
		      shour   = 0.125;
		      sthree  = 0.125;
		      ssix    = 0.25;
		      hinc    = 180;
		    }
		  }
	    
		  sized = true;
	    
		  gl.glBegin(gl.GL_LINES);
		  {
		    gl.glColor3d(1.0, 1.0, 1.0);

		    int wk;
		    for(wk=0; wk<mins; wk+=hinc) {
		      int hour = wk / 60;
		
		      double y = shour;
		      if((hour % 12) == 0) 
			y = stwelve;
		      else if((hour % 6) == 0) 
			y = ssix;
		      else if((hour % 3) == 0) 
			y = sthree;

		      double x = ((double) wk) + offset;
		      gl.glVertex2d(x, 0.0);
		      gl.glVertex2d(x, y);
		    }
		  }
		  gl.glEnd();
		}
	      }

	      /* 1-day tick marks */ 
	      {
		gl.glLineWidth(2.0f);
		gl.glBegin(gl.GL_LINES);
		{
		  gl.glColor3d(1.0, 1.0, 1.0);

		  int wk;
		  for(wk=0; wk<=mins; wk+=1440) {
		    double x = ((double) wk) + offset;
		    gl.glVertex2d(x, 0.0);
		    gl.glVertex2d(x, 1.0);
		  }
		}
		gl.glEnd();
		gl.glLineWidth(1.0f);
	      }
	    }
	    gl.glPopMatrix();

	    /* time scale labels */
	    gl.glPushMatrix();
	    {
	      Point2d center = pBBox.getCenter();
	      gl.glScaled(pScale.x(), pBorder.y(), 1.0);
	      gl.glTranslated(-center.x(), 0.0, 0.0);
	
	      /* date labels */ 
	      if(!dateLabelDLs.isEmpty()) {
		gl.glPushMatrix();
		{
		  gl.glTranslated(offset, 0.775, 0.0);
		
		  for(Integer dl : dateLabelDLs) {
		    gl.glPushMatrix();
		    {
		      double ts = 0.275*pBorder.y();
		      gl.glScaled(ts/pScale.x(), ts/pBorder.y(), 1.0);
		      gl.glCallList(dl);
		    }
		    gl.glPopMatrix();
		  
		    gl.glTranslated(1440.0, 0.0, 0.0);
		  }
		}
		gl.glPopMatrix();
	      }

	      /* time labels */ 
	      if(!timeLabelDLs.isEmpty()) {
		gl.glPushMatrix();
		{
		  gl.glTranslated(offset, 0.525, 0.0);
		
		  for(Integer dl : timeLabelDLs) {
		    gl.glPushMatrix();
		    {
		      double ts = 0.275*pBorder.y();
		      gl.glScaled(ts/pScale.x(), ts/pBorder.y(), 1.0);
		      gl.glCallList(dl);
		    }
		    gl.glPopMatrix();
		
		    gl.glTranslated(timeLabelInc, 0.0, 0.0);
		  }
		}
		gl.glPopMatrix();
	      }
	    }
	    gl.glPopMatrix();
	  }
	  gl.glEndList();

	  pRefreshTimeScale = false;
	}
	else {
	  gl.glCallList(pTimeScaleDL);
	}
      }
    }
    gl.glPopMatrix();

    /* corner area */ 
    {
      Point2d a = new Point2d(-pGraphArea.x()-pBorder.x(), pGraphArea.y());
      Point2d b = new Point2d(-pGraphArea.x(), pGraphArea.y()+pBorder.y());

      gl.glBegin(gl.GL_QUADS);
      {
	gl.glColor3d(0.4, 0.4, 0.4);
	
	gl.glVertex2d(a.x(), b.y()); 
	gl.glVertex2d(b.x(), b.y()); 
	gl.glVertex2d(b.x(), a.y()); 
	gl.glVertex2d(a.x(), a.y());	
      }
      gl.glEnd();
    }

    /* render the graph labels */ 
    {
      gl.glBegin(gl.GL_QUADS);
      {
	gl.glColor3d(0.4, 0.4, 0.4);
	
	gl.glVertex2d(-pGraphArea.x()-pBorder.x(),  pGraphArea.y());
	gl.glVertex2d(-pGraphArea.x(),              pGraphArea.y());
	gl.glVertex2d(-pGraphArea.x(),             -pGraphArea.y());
	gl.glVertex2d(-pGraphArea.x()-pBorder.x(), -pGraphArea.y());
      }
      gl.glEnd();
    }

    /* divider line */ 
    {
      gl.glLineWidth(2.0f);
      gl.glBegin(gl.GL_LINES);
      {
	gl.glColor3d(1.0, 1.0, 1.0);

 	gl.glVertex2d(-pGraphArea.x(), pGraphArea.y()+pBorder.y());
 	gl.glVertex2d(-pGraphArea.x(), -pGraphArea.y());
      }
      gl.glEnd();
      gl.glLineWidth(1.0f);
    }
  }

   
  /**
   * Called by the drawable during the first repaint after the component has been resized.
   */ 
  public void 
  reshape
  (
   GLDrawable drawable, 
   int x, 
   int y, 
   int width, 
   int height
  )
  {
    GL gl = drawable.getGL();

    Vector2d screen = new Vector2d((double) width, (double) height);
    pGraphArea = Vector2d.max(Vector2d.sub(screen, pBorder), new Vector2d());
    pGraphArea.mult(0.5);

    Vector2d bl = new Vector2d(-pGraphArea.x()-pBorder.x(), -pGraphArea.y());
    Vector2d tr = Vector2d.add(bl, screen);

    gl.glMatrixMode(GL.GL_PROJECTION);
    gl.glLoadIdentity();
    gl.glOrtho(bl.x(), tr.x(), bl.y(), tr.y(), -1.0, 1.0);

    pRefreshTimeScale = true;
  }
 
  /** 
   * Called by the drawable when the display mode or the display device associated with 
   * the GLDrawable has changed.
   */ 
  public void 
  displayChanged
  (
   GLDrawable drawable, 
   boolean modeChanged, 
   boolean deviceChanged
  )
  {}


  /*-- MOUSE LISTENER METHODS --------------------------------------------------------------*/

  /**
   * Invoked when the mouse button has been clicked (pressed and released) on a component. 
   */ 
  public void 
  mouseClicked(MouseEvent e) {}
   
  /**
   * Invoked when the mouse enters a component. 
   */
  public void 
  mouseEntered
  (
   MouseEvent e
  ) 
  {
    pCanvas.requestFocusInWindow();

    {
      Point p = e.getPoint();
      pMousePos = new Point2d(p.getX(), p.getY());
    }
  }
  
  /**
   * Invoked when the mouse exits a component. 
   */ 
  public void 
  mouseExited
  (
   MouseEvent e
  ) 
  {
    KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
  }

  /**
   * Invoked when a mouse button has been pressed on a component. 
   */
  public void 
  mousePressed
  (
   MouseEvent e 
  ) 
  {
    int mods = e.getModifiersEx();

    {
      Point p = e.getPoint();
      pMousePos = new Point2d(p.getX(), p.getY());
    }   

    /* <BUTTON2+ALT>: pan start */ 
    {
      int on1  = (MouseEvent.BUTTON2_DOWN_MASK |
		  MouseEvent.ALT_DOWN_MASK);
      
      int off1 = (MouseEvent.BUTTON1_DOWN_MASK | 
		  MouseEvent.BUTTON3_DOWN_MASK | 
		  MouseEvent.SHIFT_DOWN_MASK |
		  MouseEvent.CTRL_DOWN_MASK);
      
      if((mods & (on1 | off1)) == on1) {
	pCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
	pDragStart = new Point2d(pMousePos);
      }
    }
	
    /* <BUTTON1+BUTTON2+ALT>: zoom start */ 
    {
      int on1  = (MouseEvent.BUTTON1_DOWN_MASK |
		  MouseEvent.BUTTON2_DOWN_MASK | 
		  MouseEvent.ALT_DOWN_MASK);
      
      int off1 = (MouseEvent.BUTTON3_DOWN_MASK | 
		  MouseEvent.SHIFT_DOWN_MASK |
		  MouseEvent.CTRL_DOWN_MASK);
      
      if((mods & (on1 | off1)) == on1) {
	pCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
	pDragStart = new Point2d(pMousePos);
      }
    }
  }

  /**
   * Invoked when a mouse button has been released on a component. 
   */ 
  public void 
  mouseReleased
  (
   MouseEvent e
  ) 
  {
    pCanvas.setCursor(Cursor.getDefaultCursor());
  }


  /*-- MOUSE MOTION LISTNER METHODS --------------------------------------------------------*/
  
  /**
   * Invoked when a mouse button is pressed on a component and then dragged. 
   */ 
  public void 	
  mouseDragged
  (
   MouseEvent e
  )
  {
    int mods = e.getModifiersEx();

    boolean pan  = false;
    boolean zoom = false;
    {
      /* <BUTTON2+ALT>: pan start */ 
      {
	int on1  = (MouseEvent.BUTTON2_DOWN_MASK |
		    MouseEvent.ALT_DOWN_MASK);
	
	int off1 = (MouseEvent.BUTTON1_DOWN_MASK | 
		    MouseEvent.BUTTON3_DOWN_MASK | 
		    MouseEvent.SHIFT_DOWN_MASK |
		    MouseEvent.CTRL_DOWN_MASK);
	
	pan = ((mods & (on1 | off1)) == on1);
      }
      
      /* <BUTTON1+BUTTON2+ALT>: zoom start */ 
      {
	int on1  = (MouseEvent.BUTTON1_DOWN_MASK |
		    MouseEvent.BUTTON2_DOWN_MASK | 
		    MouseEvent.ALT_DOWN_MASK);
	
	int off1 = (MouseEvent.BUTTON3_DOWN_MASK | 
		    MouseEvent.SHIFT_DOWN_MASK |
		    MouseEvent.CTRL_DOWN_MASK);
	
	zoom = ((mods & (on1 | off1)) == on1);
      }
    }
    
    if(pDragStart != null) {
      Point p = e.getPoint();
      Point2d pos = new Point2d(p.getX(), p.getY());

      Vector2d delta = new Vector2d(pDragStart, pos);
      delta.y(delta.y() * -1.0);

      if(pan) {	     
	pTranslate.add(delta);

	pDragStart = pos;
      }
      else if(zoom) {
	Vector2d n = new Vector2d(delta);
	n.div(400.0);
	n.add(new Vector2d(1.0, 1.0)); 

	Vector2d oscale = pScale;
	setScale(Vector2d.mult(pScale, n));

	Vector2d factor = Vector2d.div(pScale, oscale);
	pTranslate.mult(factor);

	pDragStart = pos;
      } 
    }

    if(pan || zoom) 
      pCanvas.repaint();     
  }

  /**
   * Invoked when the mouse cursor has been moved onto a component but no buttons have 
   * been pushed. 
   */ 
  public void 	
  mouseMoved 
  (
   MouseEvent e
  ) 
  {
    Point p = e.getPoint();
    pMousePos = new Point2d(p.getX(), p.getY());
  }


  /*-- KEY LISTENER METHODS ----------------------------------------------------------------*/

  /**
   * invoked when a key has been pressed.
   */   
  public void 
  keyPressed
  (
   KeyEvent e
  )
  {
    UserPrefs prefs = UserPrefs.getInstance();
    if((prefs.getUpdate() != null) &&
       prefs.getUpdate().wasPressed(e))
      doUpdate(); 
    else if((prefs.getFrameAll() != null) &&
       prefs.getFrameAll().wasPressed(e))
      doFrameAll();

    else {
      switch(e.getKeyCode()) {
      case KeyEvent.VK_SHIFT:
      case KeyEvent.VK_ALT:
      case KeyEvent.VK_CONTROL:
	break;
      
      default:
	Toolkit.getDefaultToolkit().beep();
	pCanvas.repaint();
      }
    }
  }

  /**
   * Invoked when a key has been released.
   */ 
  public void 	
  keyReleased(KeyEvent e) {}

  /**
   * Invoked when a key has been typed.
   */ 
  public void 	
  keyTyped(KeyEvent e) {} 


  /*-- ACTION LISTENER METHODS -------------------------------------------------------------*/

  /** 
   * Invoked when an action occurs. 
   */ 
  public void 
  actionPerformed
  (
   ActionEvent e
  ) 
  {
    String cmd = e.getActionCommand();
    if(cmd.equals("update")) 
      doUpdate();
    if(cmd.equals("refresh")) 
      doRefresh();
    else {
      pCanvas.repaint();
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the latest resource samples and update the display.
   */ 
  private synchronized void 
  doUpdate()
  {
    UpdateTask task = new UpdateTask(pSamples.keySet());
    task.start();
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Rebuild the OpenGL graphics for the current sample.
   */ 
  private void 
  doRefresh() 
  {
    pRefreshGraph     = true;
    pRefreshTimeScale = true;
      
    pCanvas.repaint();  
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the latest resource samples and update the display.
   */ 
  private void 
  doFrameAll()
  {
    if(pBBox != null) {
      Vector2d area = Vector2d.mult(pGraphArea, new Vector2d(2.0, 2.0));
      area.sub(new Vector2d(20.0, 20.0));
      setScale(Vector2d.div(area, pBBox.getRange()));

      pTranslate.set(0.0, 0.0);

      pCanvas.repaint();   
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The interval of time represented by the smallest displayed tick marks.
   */ 
  private 
  enum TickLevel
  {
    OneDay,
    ThreeHours, 
    OneHour,
    FifteenMinutes,
    FiveMinutes,
    OneMinute;
  }


  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Update the resource usage history.
   */ 
  private
  class UpdateTask
    extends Thread
  {
    public 
    UpdateTask
    (
     Set<String> hostnames
    ) 
    {
      super("JResourceUsageHistoryDialog:UpdateTask");
      pHostnames = hostnames; 
    }
    
    public void
    run()
    {
      UIMaster master = UIMaster.getInstance();
      QueueMgrClient qclient = master.getQueueMgrClient();
      
      TreeMap<String,ResourceSampleBlock> samples = new TreeMap<String,ResourceSampleBlock>();
      if(master.beginPanelOp()) {
	try {
	  for(String hname : pHostnames) {
	    try {
	      master.updatePanelOp("Getting History: " + hname);
	      ResourceSampleBlock block = qclient.getHostResourceSamples(hname);
	      if(block != null) 
		samples.put(hname, block);
	    }
	    catch(PipelineException ex) {
	      master.showErrorDialog(ex);
	    }
	  }
	}
	finally {
	  master.endPanelOp("Done.");
	}
      }
      updateSamples(samples);
    }

    private Set<String>  pHostnames;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6400771997536712644L;

  /**
   * The vertical space between the min/max values of a graph and the background rectangle.
   */ 
  private static final double  sGraphBorder = 0.05;

  /**
   * The vertical space between graph background rectangles.
   */ 
  private static final double  sGraphGap = 0.05;

  /**
   * A standardized date formatters.
   */ 
  private static SimpleDateFormat  sDateFormat = new SimpleDateFormat(" yyyy-MM-dd"); 
  private static SimpleDateFormat  sTimeFormat = new SimpleDateFormat(" HH:mm"); 



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The resource usage samples indexed by hostname.
   */ 
  private TreeMap<String,ResourceSampleBlock>  pSamples;

	
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to display system load graphs.
   */ 
  private JToggleButton  pLoadButton; 

  /**
   * Whether to display free memory graphs.
   */ 
  private JToggleButton  pMemButton; 

  /**
   * Whether to display free temporary disk space graphs.
   */ 
  private JToggleButton  pDiskButton; 

  /**
   * Whether to display running job count graphs.
   */ 
  private JToggleButton  pJobButton; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The OpenGL rendering canvas.
   */ 
  private GLCanvas  pCanvas;


  /**
   * The OpenGL display list handle for the 1-minute tick marks (15-minute block).
   */ 
  private Integer  pOneMinDL;

  /**
   * The OpenGL display list handle for the 5-minute tick marks (1-hour block).
   */ 
  private Integer  pFiveMinDL;

  /**
   * The OpenGL display list handle for the 15-minute tick marks (6-hour block).
   */ 
  private Integer  pFifteenMinDL;

  /**
   * Whether the OpenGL display list for time scale geometry need to be rebuilt.
   */ 
  private boolean  pRefreshTimeScale;

  /**
   * The OpenGL display list handle for time scale geometry.
   */ 
  private Integer  pTimeScaleDL;

  

  /**
   * Whether the OpenGL display lists for graph geometry needs to be rebuilt.
   */ 
  private boolean  pRefreshGraph;

  /**
   * The OpenGL display list handles for the system load graph geometry indexed by hostname.
   */ 
  private TreeMap<String,Integer>  pLoadDLs; 

  /**
   * The world space horizontal bounds of the system load graph geometry indexed by hostname.
   */ 
  private TreeMap<String,Vector2d> pLoadSpans; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The timestamp of the earliest sample. 
   */ 
  private Date  pMinTime;

  /**
   * The timestamp of the first/last days contining visible graph geometry.
   */ 
  private Date  pStartDay;
  private Date  pEndDay; 
  
  /**
   * The pre-transform bounding box of all visible graph geometry.
   */ 
  private BBox2d  pBBox; 

  /**
   * The world space translation of the graph geometry.
   */ 
  private Point2d  pTranslate; 

  /** 
   * The world space scale of the graph geometry.
   */ 
  private Vector2d  pScale; 

  /**
   * The minimum/maximum world space scale of the graph geometry.
   */ 
  private Vector2d  pMinScale; 
  private Vector2d  pMaxScale; 


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The world space sizes of the left and top border areas.
   */ 
  private Vector2d  pBorder; 
  
  /**
   * The world space size of half the graph area.
   */ 
  private Vector2d  pGraphArea; 
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * The last known mouse position in canvas coordinates.
   */ 
  private Point2d pMousePos;

  /**
   * The location of the start of a mouse drag in canvas coordinates.
   */ 
  private Point2d  pDragStart;  
}
