// $Id: JResourceUsageHistoryDialog.java,v 1.11 2005/04/02 20:57:31 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.ui.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;
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
      UserPrefs prefs = UserPrefs.getInstance();
      pShowFullLoadBar = prefs.getShowFullLoadBar();
      pShowJobSlotsBar = prefs.getShowJobSlotsBar();

      pSamples = new TreeMap<String,ResourceSampleBlock>();

      pRefreshGraphBars = true; 
      pRefreshLabels    = true;
      pRefreshTimeScale = true;
      pRefreshGraph     = true;
      
      pLabelsDL    = new AtomicInteger(0);
      pTimeScaleDL = new AtomicInteger(0);
      pGraphBarsDL = new AtomicInteger(0);

      pIntegerLabelDLs = new TreeMap<Integer,Integer>();
      pDoubleLabelDLs  = new TreeMap<String,Integer>();

      pGraphSpans = new TreeMap<String,Vector2d>();

      pLoadDLs = new TreeMap<String,Integer>();
      pMemDLs  = new TreeMap<String,Integer>();
      pDiskDLs = new TreeMap<String,Integer>();
      pJobDLs  = new TreeMap<String,Integer>();

      pTranslate = new Point2d();

      pMinScale = new Vector2d(0.1, 20.0);
      pMaxScale = new Vector2d(15.0, 200.0);
      pScale    = Vector2d.lerp(pMinScale, pMaxScale, 0.25);

      pBorder = new Vector2d(200.0, 56.0);
    }

    
    /* popup menu */ 
    {
      JMenuItem item;
      
      pPopup = new JPopupMenu();  

      item = new JMenuItem("Update");
      pUpdateItem = item;
      item.setActionCommand("update");
      item.addActionListener(this);
      pPopup.add(item);  
      
      pPopup.addSeparator();
      
      item = new JMenuItem("Frame All");
      pFrameAllItem = item;
      item.setActionCommand("frame-all");
      item.addActionListener(this);
      pPopup.add(item);  
      
      pPopup.addSeparator();

      item = new JMenuItem();
      pShowHideLoadItem = item;
      item.setActionCommand("show-hide-load");
      item.addActionListener(this);
      pPopup.add(item);  

      item = new JMenuItem();
      pShowHideMemItem = item;
      item.setActionCommand("show-hide-mem");
      item.addActionListener(this);
      pPopup.add(item);  

      item = new JMenuItem();
      pShowHideDiskItem = item;
      item.setActionCommand("show-hide-disk");
      item.addActionListener(this);
      pPopup.add(item);  

      item = new JMenuItem();
      pShowHideJobItem = item;
      item.setActionCommand("show-hide-job");
      item.addActionListener(this);
      pPopup.add(item);  
      
      pPopup.addSeparator();

      item = new JMenuItem();
      pShowHideFullLoadBarItem = item;
      item.setActionCommand("show-hide-load-bar");
      item.addActionListener(this);
      pPopup.add(item);  
      
      item = new JMenuItem();
      pShowHideJobSlotsBarItem = item;
      item.setActionCommand("show-hide-slots-bar");
      item.addActionListener(this);
      pPopup.add(item);  
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
		  
	  Dimension size = new Dimension(30, 10);
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
		  
	  Dimension size = new Dimension(30, 10);
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
		  
	  Dimension size = new Dimension(30, 10);
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
		  
	  Dimension size = new Dimension(30, 10);
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

      {
	Rectangle bounds = getGraphicsConfiguration().getBounds();
	setLocation(bounds.x + bounds.width/2 - getWidth()/2, 
		    bounds.y + bounds.height/2 - getHeight()/2);		    
      }
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
    Vector2d oscale = pScale;
    pScale = scale;
    pScale.clamp(pMinScale, pMaxScale);

    if(!ExtraMath.equiv(oscale.y(), pScale.y())) {
      pBorderResized    = true;
      pRefreshGraphBars = true;
      pRefreshLabels    = true;
    }

    if(!ExtraMath.equiv(oscale.x(), pScale.x())) {
      pRefreshTimeScale = true;
    }
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

    pBorderResized = true;

    doRefresh();
  }

  /**
   * Update the panel to reflect new user preferences.
   */ 
  public void 
  updateUserPrefs() 
  {
    doRefresh();
    updateMenuToolTips();
  }

  /**
   * Update the menu item tool tips.
   */ 
  private void 
  updateMenuToolTips() 
  {
    UserPrefs prefs = UserPrefs.getInstance();

    updateMenuToolTip
      (pUpdateItem, prefs.getUpdate(), 
       "Update the resource usage history graphs.");

    updateMenuToolTip
      (pFrameAllItem, prefs.getFrameAll(), 
       "Move the camera to frame all resource usage history graphs.");

    updateMenuToolTip
      (pShowHideLoadItem, prefs.getToggleSystemLoad(), 
       "Toggle the display of the system load graphs.");
    updateMenuToolTip
      (pShowHideMemItem, prefs.getToggleFreeMemory(), 
       "Toggle the display of the free memory graphs.");
    updateMenuToolTip
      (pShowHideDiskItem, prefs.getToggleFreeDisk(), 
       "Toggle the display of the free temporary disk space graphs.");
    updateMenuToolTip
      (pShowHideJobItem, prefs.getToggleJobCount(), 
       "Toggle the display of the job count graphs.");

    updateMenuToolTip
      (pShowHideFullLoadBarItem, prefs.getToggleFullLoadBar(), 
       "Toggle the display of the full load bar.");
    updateMenuToolTip
      (pShowHideJobSlotsBarItem, prefs.getToggleJobSlotsBar(), 
       "Toggle the display of the job slots level bar.");
  }

  /**
   * Update the tool tip for the given menu item.
   */   
  protected void 
  updateMenuToolTip
  (
   JMenuItem item, 
   HotKey key,
   String desc
  ) 
  {
    String text = null;
    if(UserPrefs.getInstance().getShowMenuToolTips()) {
      if(desc != null) {
	if(key != null) 
	  text = (desc + "<P>Hot Key = " + key);
	else 
	  text = desc;
      }
      else {
	text = ("Hot Key = " + key);
      }
    }
    
    if(text != null) 
      item.setToolTipText(UIFactory.formatToolTip(text));
    else 
      item.setToolTipText(null);
  }

  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the popup menu.
   */ 
  public void 
  updatePopupMenu() 
  {
    pShowHideLoadItem.setText
      ((pLoadButton.isSelected() ? "Hide" : "Show") + " System Load (CPU)");

    pShowHideMemItem.setText
      ((pMemButton.isSelected() ? "Hide" : "Show") + " Free Memory (MEM)");

    pShowHideDiskItem.setText
      ((pDiskButton.isSelected() ? "Hide" : "Show") + " Free Disk (DISK)");

    pShowHideJobItem.setText
      ((pJobButton.isSelected() ? "Hide" : "Show") + " Job Count (JOB)");

    pShowHideFullLoadBarItem.setText
      ((pShowFullLoadBar ? "Hide" : "Show") + " Full Load Bar");

    pShowHideJobSlotsBarItem.setText
      ((pShowJobSlotsBar ? "Hide" : "Show") + " Job Slots Bar");
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Resize the OpenGL viewport to match the current window and border sizes.
   */ 
  private void 
  resizeViewport
  (
   GL gl, 
   int width, 
   int height
  ) 
  {
    Vector2d screen = new Vector2d((double) width, (double) height);
    pGraphArea = Vector2d.max(Vector2d.sub(screen, pBorder), new Vector2d());
    pGraphArea.mult(0.5);

    Vector2d bl = new Vector2d(-pGraphArea.x()-pBorder.x(), -pGraphArea.y());
    Vector2d tr = Vector2d.add(bl, screen);

    gl.glMatrixMode(GL.GL_PROJECTION);
    gl.glLoadIdentity();
    gl.glOrtho(bl.x(), tr.x(), bl.y(), tr.y(), -1.0, 1.0);
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
    GeometryMgr mgr = GeometryMgr.getInstance();
    GL gl = drawable.getGL();

    if(pBorderResized) {  
      try {
	double bx = 0.0;
	for(String hname : pSamples.keySet()) {
	  double x = mgr.getTextWidth("CharterBTRoman", hname, 0.05) * sHostnameSize;
	  bx = Math.max(bx, x);
	}

	pShowGraphBrackets = (((sGraphBorder*2.0 + sGraphGap) * pScale.y()) >= 10.0);
	double lx = pShowGraphBrackets ? 0.0 : -31.0;

	pBorder.x(bx + 20.0 + sLabelTickWidth + lx);

	resizeViewport(gl, pCanvas.getWidth(), pCanvas.getHeight());      

	pBorderResized = false;
      }
      catch(IOException ex) {
	UIMaster.getInstance().showErrorDialog(ex);	
      }
    }

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
      for(Integer dl : pLoadDLs.values()) 
	UIMaster.getInstance().freeDisplayList(dl);
      pLoadDLs.clear();
      
      for(Integer dl : pMemDLs.values()) 
	UIMaster.getInstance().freeDisplayList(dl);
      pMemDLs.clear();
      
      for(Integer dl : pDiskDLs.values()) 
	UIMaster.getInstance().freeDisplayList(dl);
      pDiskDLs.clear();
      
      for(Integer dl : pJobDLs.values()) 
	UIMaster.getInstance().freeDisplayList(dl);
      pJobDLs.clear();
  
      pGraphSpans.clear();      

      if(!pSamples.isEmpty()) {
	pMinTime = null;
	for(ResourceSampleBlock block : pSamples.values()) {
	  Date stamp = block.getStartTimeStamp();
	  if((pMinTime == null) || (stamp.compareTo(pMinTime) < 0))
	    pMinTime = stamp;
	}
	  
	for(String hname : pSamples.keySet()) {
	  ResourceSampleBlock block = pSamples.get(hname);
	  if(block != null) {
	    Date first = block.getStartTimeStamp();
	    Date last  = block.getEndTimeStamp();
	      
	    double offset = ((double) (first.getTime() - pMinTime.getTime())) / 60000.0;
	    double range  = ((double) (last.getTime() - first.getTime())) / 60000.0;

	    Vector2d span = new Vector2d(offset, range+1.0);
	    pGraphSpans.put(hname, span);

	    /* system load graphs */ 
	    if(block.getNumSamples() > 0) {
	      Integer dl = UIMaster.getInstance().getDisplayList(gl);
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

		/* full load bar */ 
		if(pShowFullLoadBar) {
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
	      
	    /* free memory */ 
	    if(pMemButton.isSelected()) {
	      Integer dl = UIMaster.getInstance().getDisplayList(gl);
	      pMemDLs.put(hname, dl);			    
	      
	      double totalMem = (double) block.getTotalMemory();

	      gl.glNewList(dl, GL.GL_COMPILE);
	      {
		gl.glBegin(gl.GL_QUADS);
		{
		  /* background */ 
		  {
		    Color3d color = prefs.getFreeMemoryBgColor();
		    gl.glColor3d(color.r(), color.g(), color.b());
		    
		    gl.glVertex2d(0.0,      0.0-sGraphBorder);
		    gl.glVertex2d(span.y(), 0.0-sGraphBorder);
		    gl.glVertex2d(span.y(), 1.0+sGraphBorder);
		    gl.glVertex2d(0.0,      1.0+sGraphBorder);
		  }

		  /* graph */ 
		  {
		    Color3d fg = prefs.getFreeMemoryFgColor();
		    gl.glColor3d(fg.r(), fg.g(), fg.b());

		    int wk;
		    for(wk=0; wk<block.getNumSamples(); wk++) {
		      double mem = ((double) block.getMemory(wk)) / totalMem;

		      gl.glVertex2d((double) wk,     0.0);
		      gl.glVertex2d((double) (wk+1), 0.0);
		      gl.glVertex2d((double) (wk+1), mem);
		      gl.glVertex2d((double) wk,     mem);
		    }
		  }
		}
		gl.glEnd();
	      }
	      gl.glEndList();
	    }

	    /* free temporary disk space */ 
	    if(pDiskButton.isSelected()) {
	      Integer dl = UIMaster.getInstance().getDisplayList(gl);
	      pDiskDLs.put(hname, dl);			    
	      
	      double totalDisk = (double) block.getTotalDisk();

	      gl.glNewList(dl, GL.GL_COMPILE);
	      {
		gl.glBegin(gl.GL_QUADS);
		{
		  /* background */ 
		  {
		    Color3d color = prefs.getFreeDiskBgColor();
		    gl.glColor3d(color.r(), color.g(), color.b());
		    
		    gl.glVertex2d(0.0,      0.0-sGraphBorder);
		    gl.glVertex2d(span.y(), 0.0-sGraphBorder);
		    gl.glVertex2d(span.y(), 1.0+sGraphBorder);
		    gl.glVertex2d(0.0,      1.0+sGraphBorder);
		  }

		  /* graph */ 
		  {
		    Color3d fg = prefs.getFreeDiskFgColor();
		    gl.glColor3d(fg.r(), fg.g(), fg.b());

		    int wk;
		    for(wk=0; wk<block.getNumSamples(); wk++) {
		      double disk = ((double) block.getDisk(wk)) / totalDisk;

		      gl.glVertex2d((double) wk,     0.0);
		      gl.glVertex2d((double) (wk+1), 0.0);
		      gl.glVertex2d((double) (wk+1), disk);
		      gl.glVertex2d((double) wk,     disk);
		    }
		  }
		}
		gl.glEnd();
	      }
	      gl.glEndList();
	    }
	
	    /* job count */ 
	    if(pJobButton.isSelected()) {
	      Integer dl = UIMaster.getInstance().getDisplayList(gl);
	      pJobDLs.put(hname, dl);			    
	      
	      double totalJobs = (double) prefs.getJobCountRange();

	      gl.glNewList(dl, GL.GL_COMPILE);
	      {
		gl.glBegin(gl.GL_QUADS);
		{
		  /* background */ 
		  {
		    Color3d color = prefs.getJobCountBgColor();
		    gl.glColor3d(color.r(), color.g(), color.b());
		    
		    gl.glVertex2d(0.0,      0.0-sGraphBorder);
		    gl.glVertex2d(span.y(), 0.0-sGraphBorder);
		    gl.glVertex2d(span.y(), 1.0+sGraphBorder);
		    gl.glVertex2d(0.0,      1.0+sGraphBorder);
		  }

		  /* graph */ 
		  {
		    Color3d fg = prefs.getJobCountFgColor();
		    Color3d oc = prefs.getJobCountOverflowColor();

		    boolean over = false;		    
		    gl.glColor3d(fg.r(), fg.g(), fg.b());

		    int wk;
		    for(wk=0; wk<block.getNumSamples(); wk++) {
		      double jobs = ((double) block.getNumJobs(wk)) / totalJobs;
		      if(jobs > 0.0) {
			if(jobs > 1.0) {
			  if(!over) {
			    gl.glColor3d(oc.r(), oc.g(), oc.b());
			    over = true;
			  }
			}
			else if(over) {
			  gl.glColor3d(fg.r(), fg.g(), fg.b());
			  over = false;
			}
			
			jobs = Math.min(jobs, 1.0);
		      
			gl.glVertex2d((double) wk,     0.0);
			gl.glVertex2d((double) (wk+1), 0.0);
			gl.glVertex2d((double) (wk+1), jobs);
			gl.glVertex2d((double) wk,     jobs);
		      }
		    }
		  }
		}
		gl.glEnd();

		/* job slots level */ 
		if(pShowJobSlotsBar) {
		  double slots = (double) block.getJobSlots();
		  double v = slots / totalJobs; 
		  if(v <= 1.0) {
		    gl.glBegin(gl.GL_LINES);
		    {
		      Color3d color = prefs.getJobSlotsColor();
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
	  }
	}
      }
	
      pRefreshGraph = false;
    }
  
    /* compute the bounding box and time range of all graph geometry */ 
    {
      Date startStamp = null;
      Date endStamp   = null;
      pBBox = null;
      double dy = 0.0;
      for(String hname : pSamples.keySet()) {
	if(pLoadButton.isSelected() && growGraphBBox(hname, pLoadDLs, dy)) 
	  dy -= 1.0 + sGraphBorder*2.0 + sGraphGap; 

	if(pMemButton.isSelected() && growGraphBBox(hname, pMemDLs, dy)) 
	  dy -= 1.0 + sGraphBorder*2.0 + sGraphGap; 

	if(pDiskButton.isSelected() && growGraphBBox(hname, pDiskDLs, dy)) 
	  dy -= 1.0 + sGraphBorder*2.0 + sGraphGap; 

	if(pJobButton.isSelected() && growGraphBBox(hname, pJobDLs, dy)) 
	  dy -= 1.0 + sGraphBorder*2.0 + sGraphGap; 

	/* time range */ 
	if(pLoadButton.isSelected() || pMemButton.isSelected()  || 
	   pDiskButton.isSelected() || pJobButton.isSelected()) {

	  ResourceSampleBlock block = pSamples.get(hname);

	  if((startStamp == null) || (block.getStartTimeStamp().compareTo(startStamp) < 0)) 
	    startStamp = block.getStartTimeStamp();

	  if((endStamp == null) || (block.getEndTimeStamp().compareTo(endStamp) > 0)) 
	    endStamp = block.getEndTimeStamp();
	}
	  
	dy -= sGraphGap*2.0; 
      }

      pStartDay = null;
      if(startStamp != null) {
	Calendar cal = Calendar.getInstance();
	cal.setTime(startStamp);
	cal.set(Calendar.MILLISECOND, 0);
	cal.set(Calendar.SECOND, 0);
	cal.set(Calendar.MINUTE, 0);
	cal.set(Calendar.HOUR_OF_DAY, 0);
	pStartDay = cal.getTime();
      }

      pEndDay = null;
      if(endStamp != null) {
	Calendar cal = Calendar.getInstance();
	cal.setTime(endStamp);
	cal.set(Calendar.MILLISECOND, 0);
	cal.set(Calendar.SECOND, 0);
	cal.set(Calendar.MINUTE, 0);
	cal.set(Calendar.HOUR_OF_DAY, 0);
	pEndDay = cal.getTime();
      }
    }

    /* render graph bars */ 
    if(pBBox != null) {
      gl.glPushMatrix();
      {
	gl.glTranslated(0.0, pTranslate.y(), 0.0);
	
	/* build graph bars display list */ 
	if(pRefreshGraphBars) {
	  {
	    UIMaster master = UIMaster.getInstance(); 
	    master.freeDisplayList(pGraphBarsDL.getAndSet(master.getDisplayList(gl)));
	  }
	  
	  gl.glNewList(pGraphBarsDL.get(), GL.GL_COMPILE_AND_EXECUTE);
	  {		
	    gl.glPushMatrix();
	    {
	      Point2d center = pBBox.getCenter();
	      gl.glScaled(1.0, pScale.y(), 1.0);
	      gl.glTranslated(1.0, -center.y(), 0.0);
	      
	      gl.glColor3d(0.8, 0.8, 0.8);
	      
	      double top = 1.0;
	      for(String hname : pSamples.keySet()) {
		ResourceSampleBlock block = pSamples.get(hname);
		
		if(pLoadButton.isSelected() && renderGraphBars(gl, top)) {
		  if(pShowFullLoadBar) {
		    double v = (((double) block.getNumProcessors()) / 
				((double) prefs.getSystemLoadRange()));
		    if((v > 0.0) && (v < 1.0)) {
		      Color3d color = prefs.getFullLoadColor();
		      color.mult(0.8);
		      gl.glColor3d(color.r(), color.g(), color.b());
		      
		      gl.glBegin(gl.GL_LINES);
		      {	
			gl.glVertex2d(-pGraphArea.x(), top-1.0+v);
			gl.glVertex2d( pGraphArea.x(), top-1.0+v);
		      }
		      gl.glEnd();
		      
		      gl.glColor3d(0.8, 0.8, 0.8);
		    }
		  }
		  
		  top -= 1.0 + sGraphBorder*2.0 + sGraphGap; 
		}
		
		if(pMemButton.isSelected() && renderGraphBars(gl, top))
		  top -= 1.0 + sGraphBorder*2.0 + sGraphGap; 
		
		if(pDiskButton.isSelected() && renderGraphBars(gl, top))
		  top -= 1.0 + sGraphBorder*2.0 + sGraphGap; 
		
		if(pJobButton.isSelected() && renderGraphBars(gl, top)) {
		  if(pShowJobSlotsBar) {
		    double v = (((double) block.getJobSlots()) / 
				((double) prefs.getJobCountRange()));
		    if((v > 0.0) && (v < 1.0)) {
		      Color3d color = prefs.getJobSlotsColor();
		      color.mult(0.8);
		      gl.glColor3d(color.r(), color.g(), color.b());
		      
		      gl.glBegin(gl.GL_LINES);
		      {	
			gl.glVertex2d(-pGraphArea.x(), top-1.0+v);
			gl.glVertex2d( pGraphArea.x(), top-1.0+v);
		      }
		      gl.glEnd();
		      
		      gl.glColor3d(0.8, 0.8, 0.8);
		    }
		  }
		  
		  top -= 1.0 + sGraphBorder*2.0 + sGraphGap; 
		}
		
		top -= sGraphGap*2.0; 
	      }
	    }
	    gl.glPopMatrix();
	  }
	  gl.glEndList();
	  
	  pRefreshGraphBars = false;
	}
	else {
	  gl.glCallList(pGraphBarsDL.get());
	}
      }
      gl.glPopMatrix();

      /* render the graph geometry */ 
      gl.glPushMatrix();
      {
	gl.glTranslated(pTranslate.x(), pTranslate.y(), 0.0);

	Point2d center = pBBox.getCenter();
	gl.glScaled(pScale.x(), pScale.y(), 1.0);
	gl.glTranslated(-center.x(), -center.y(), 0.0);
	
	double dy = 0.0;
	for(String hname : pSamples.keySet()) {
	  if(pLoadButton.isSelected() && renderGraph(gl, hname, pLoadDLs, dy)) 
	    dy -= 1.0 + sGraphBorder*2.0 + sGraphGap; 

	  if(pMemButton.isSelected() && renderGraph(gl, hname, pMemDLs, dy))
	    dy -= 1.0 + sGraphBorder*2.0 + sGraphGap; 

	  if(pDiskButton.isSelected() && renderGraph(gl, hname, pDiskDLs, dy))
	    dy -= 1.0 + sGraphBorder*2.0 + sGraphGap; 

	  if(pJobButton.isSelected() && renderGraph(gl, hname, pJobDLs, dy))
	    dy -= 1.0 + sGraphBorder*2.0 + sGraphGap; 

	  dy -= sGraphGap*2.0; 
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
	
	  {
	    UIMaster master = UIMaster.getInstance(); 
	    master.freeDisplayList(pTimeScaleDL.getAndSet(master.getDisplayList(gl)));
	  }

	  gl.glNewList(pTimeScaleDL.get(), GL.GL_COMPILE_AND_EXECUTE);
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
	  gl.glCallList(pTimeScaleDL.get());
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
      /* background */ 
      gl.glBegin(gl.GL_QUADS);
      {
	gl.glColor3d(0.4, 0.4, 0.4);
	
	gl.glVertex2d(-pGraphArea.x()-pBorder.x(),  pGraphArea.y());
	gl.glVertex2d(-pGraphArea.x(),              pGraphArea.y());
	gl.glVertex2d(-pGraphArea.x(),             -pGraphArea.y());
	gl.glVertex2d(-pGraphArea.x()-pBorder.x(), -pGraphArea.y());
      }
      gl.glEnd();

      
      gl.glPushMatrix();
      {
	gl.glTranslated(-pGraphArea.x(), pTranslate.y(), 0.0);
	
	if(pBBox != null) {
	  if(pRefreshLabels) {
	    
	    /* build hostname label display lists */ 
	    TreeMap<String,Integer> labelDLs = new TreeMap<String,Integer>();
	    try {
	      for(String hname : pSamples.keySet()) {
		int dl = mgr.getTextDL(gl, "CharterBTRoman", hname, 
				       GeometryMgr.TextAlignment.Right, 0.05);
		labelDLs.put(hname, dl);
	      }
	    }
	    catch(IOException ex) {
	      UIMaster.getInstance().showErrorDialog(ex);
	    }

	    /* build graph label display lists */ 
	    Integer cpuDL  = null;
	    Integer memDL  = null;
	    Integer diskDL = null;
	    Integer jobDL  = null;
	    try {
	      cpuDL  = mgr.getNodeIconDL(gl, "Cpu");
	      memDL  = mgr.getNodeIconDL(gl, "Mem");
	      diskDL = mgr.getNodeIconDL(gl, "Disk");
	      jobDL  = mgr.getNodeIconDL(gl, "Job");
	    }
	    catch(IOException ex) {
	      UIMaster.getInstance().showErrorDialog(ex);
	    }

	    /* build graph tick label display lists */ 
	    try {
	      pIntegerLabelDLs.clear();
	      pDoubleLabelDLs.clear();

	      if(pShowGraphBrackets) {
		{
		  int v = 0;
		  int dl = mgr.getTextDL(gl, "CharterBTRoman", String.valueOf(v), 
					 GeometryMgr.TextAlignment.Center, 0.05);
		  pIntegerLabelDLs.put(v, dl);
		}

		{
		  int v = prefs.getSystemLoadRange();
		  int dl = mgr.getTextDL(gl, "CharterBTRoman", String.valueOf(v), 
					 GeometryMgr.TextAlignment.Center, 0.05);
		  pIntegerLabelDLs.put(v, dl);
		}

		{
		  int v = prefs.getJobCountRange();
		  int dl = mgr.getTextDL(gl, "CharterBTRoman", String.valueOf(v), 
					 GeometryMgr.TextAlignment.Center, 0.05);
		  pIntegerLabelDLs.put(v, dl);
		}
		
		{
		  String text = "0.0";
		  int dl = mgr.getTextDL(gl, "CharterBTRoman", text, 
					 GeometryMgr.TextAlignment.Center, 0.05);
		  pDoubleLabelDLs.put(text, dl);
		}
		
		for(String hname : pSamples.keySet()) {
		  ResourceSampleBlock block = pSamples.get(hname);
		  
		  {
		    int v = block.getNumProcessors();
		    int dl = mgr.getTextDL(gl, "CharterBTRoman", String.valueOf(v), 
					   GeometryMgr.TextAlignment.Center, 0.05);
		    pIntegerLabelDLs.put(v, dl);
		  }

		  {
		    int v = block.getJobSlots();
		    int dl = mgr.getTextDL(gl, "CharterBTRoman", String.valueOf(v), 
					   GeometryMgr.TextAlignment.Center, 0.05);
		    pIntegerLabelDLs.put(v, dl);
		  }	

		  {
		    double v = ((double) block.getTotalMemory()) / 1073741824.0;
		    String text = String.format("%1$.1f", v);
		    int dl = mgr.getTextDL(gl, "CharterBTRoman", text, 
					   GeometryMgr.TextAlignment.Center, 0.05);
		    pDoubleLabelDLs.put(text, dl);
		  }
		  
		  {
		    double v = ((double) block.getTotalDisk()) / 1073741824.0;
		    String text = String.format("%1$.1f", v);
		    int dl = mgr.getTextDL(gl, "CharterBTRoman", text, 
					   GeometryMgr.TextAlignment.Center, 0.05);
		    pDoubleLabelDLs.put(text, dl);
		  }
		}
	      }
	    }
	    catch(IOException ex) {
	      UIMaster.getInstance().showErrorDialog(ex);
	    }

	    {
	      UIMaster master = UIMaster.getInstance(); 
	      master.freeDisplayList(pLabelsDL.getAndSet(master.getDisplayList(gl)));
	    }

	    gl.glNewList(pLabelsDL.get(), GL.GL_COMPILE_AND_EXECUTE);
	    {		
	      double lx = pShowGraphBrackets ? 0 : 31.0; 

	      gl.glColor3d(1.0, 1.0, 1.0);
	      
	      gl.glPushMatrix();
	      {
		Point2d center = pBBox.getCenter();
		gl.glScaled(1.0, pScale.y(), 1.0);
		gl.glTranslated(0.0, -center.y(), 0.0);

		/* hostname labels */ 
		{
		  gl.glLineWidth(2.0f);
		  gl.glPointSize(2.0f);

		  double top  = 1.0;
		  double ntop = 1.0;
		  for(String hname : pSamples.keySet()) {
		  
		    if(pLoadButton.isSelected()) 
		      ntop -= 1.0 + sGraphBorder*2.0 + sGraphGap; 
		  
		    if(pMemButton.isSelected())
		      ntop -= 1.0 + sGraphBorder*2.0 + sGraphGap; 
		  
		    if(pDiskButton.isSelected())
		      ntop -= 1.0 + sGraphBorder*2.0 + sGraphGap; 
		  
		    if(pJobButton.isSelected())
		      ntop -= 1.0 + sGraphBorder*2.0 + sGraphGap; 
		  
		    if(ntop < top) {
		      double btm = ntop + sGraphBorder*2.0 + sGraphGap; 
		      
		      gl.glPushMatrix();
		      {
			double y = ExtraMath.lerp(top, btm, 0.5);
			gl.glTranslated(-10.0-sLabelTickWidth+lx, y, 0.0);
			gl.glScaled(sHostnameSize, sHostnameSize/pScale.y(), 1.0);
			gl.glTranslated(0.0, -0.28, 0.0);
			gl.glCallList(labelDLs.get(hname));
		      }
		      gl.glPopMatrix();

		      gl.glBegin(gl.GL_LINES);
		      {
			gl.glVertex2d(10.0-sLabelTickWidth+lx, top);
			gl.glVertex2d(    -sLabelTickWidth+lx, top);

			gl.glVertex2d(-sLabelTickWidth+lx, top);
			gl.glVertex2d(-sLabelTickWidth+lx, btm);

			gl.glVertex2d(10.0-sLabelTickWidth+lx, btm);
			gl.glVertex2d(    -sLabelTickWidth+lx, btm);
		      }
		      gl.glEnd();

		      gl.glBegin(gl.GL_POINTS);
		      {
			gl.glVertex2d(-sLabelTickWidth+lx, top);
			gl.glVertex2d(-sLabelTickWidth+lx, btm);
		      }
		      gl.glEnd();		    
		    }
		  
		    ntop -= sGraphGap*2.0;
		    top = ntop;
		  }

		  gl.glLineWidth(1.0f);
		  gl.glPointSize(1.0f);	    
		}
	      
		/* graph labels */ 
		{
		  double top = 1.0;
		  for(String hname : pSamples.keySet()) {
		    ResourceSampleBlock block = pSamples.get(hname);

		    if(pLoadButton.isSelected()) {
		      renderGraphLabel(gl, cpuDL, top, lx);
		      renderGraphTickLabels
			(gl, block.getNumProcessors(), prefs.getFullLoadColor(), 
			 pShowFullLoadBar, prefs.getSystemLoadRange(), top);

		      top -= 1.0 + sGraphBorder*2.0 + sGraphGap; 
		    }
		  
		    if(pMemButton.isSelected()) {
		      renderGraphLabel(gl, memDL, top, lx);
		      renderGraphTickLabels(gl, (double) block.getTotalMemory(), top);
		      top -= 1.0 + sGraphBorder*2.0 + sGraphGap; 
		    }
		  
		    if(pDiskButton.isSelected()) {
		      renderGraphLabel(gl, diskDL, top, lx);
		      renderGraphTickLabels(gl, (double) block.getTotalDisk(), top);
		      top -= 1.0 + sGraphBorder*2.0 + sGraphGap; 
		    }
		  
		    if(pJobButton.isSelected()) {
		      renderGraphLabel(gl, jobDL, top, lx);
		      renderGraphTickLabels
			(gl, block.getJobSlots(), prefs.getJobSlotsColor(), 
			 pShowJobSlotsBar, prefs.getJobCountRange(), top);
		      top -= 1.0 + sGraphBorder*2.0 + sGraphGap; 
		    }

		    top -= sGraphGap*2.0;
		  }
		}
	      }
	      gl.glPopMatrix();
	    }
	    gl.glEndList();
	    
	    pRefreshLabels = false;
	  }
	  else {
	    gl.glCallList(pLabelsDL.get());
	  }
	}
      }
      gl.glPopMatrix();	
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
   * Grow the bounding box to include the given geometry.
   */ 
  private boolean 
  growGraphBBox
  (
   String hname, 
   TreeMap<String,Integer> dls,
   double dy
  ) 
  {  
    Integer dl = dls.get(hname);
    Vector2d span = pGraphSpans.get(hname);
    if((dl != null) && (span != null)) {
      BBox2d bbox = new BBox2d(new Point2d(span.x(),          dy+0.0-sGraphBorder), 
			       new Point2d(span.x()+span.y(), dy+1.0+sGraphBorder));
      if(pBBox == null) 
	pBBox = bbox;
      else 
	pBBox.grow(bbox);	

      return true;
    }
    
    return false;
  }

  /**
   * Render the graph geometry. 
   */ 
  private boolean 
  renderGraph
  (
   GL gl, 
   String hname, 
   TreeMap<String,Integer> dls,
   double dy
  ) 
  {
    Integer dl = dls.get(hname);
    Vector2d span = pGraphSpans.get(hname);
    if((dl != null) && (span != null)) {
      gl.glPushMatrix();
      {
	gl.glTranslated(span.x(), dy, 0.0);
	gl.glCallList(dl);
      }
      gl.glPopMatrix();

      return true;
    }

    return false;
  }

  /**
   * Render the graph bars. 
   */ 
  private boolean 
  renderGraphBars
  (
   GL gl, 
   double top
  ) 
  {
    gl.glBegin(gl.GL_LINES);
    {	
      gl.glVertex2d(-pGraphArea.x(), top);
      gl.glVertex2d( pGraphArea.x(), top);

      gl.glVertex2d(-pGraphArea.x(), top-1.0);
      gl.glVertex2d( pGraphArea.x(), top-1.0);
    }
    gl.glEnd();

    return true;
  }

  /**
   * Render the graph label and bracket.
   */ 
  private void 
  renderGraphLabel
  (
   GL gl, 
   int dl, 
   double top, 
   double lx
  ) 
  {
    gl.glPushMatrix();
    {
      gl.glTranslated(26.0-sLabelTickWidth+lx, top-0.5, 0.0);
      gl.glScaled(32.0, 32.0/pScale.y(), 1.0);
      gl.glCallList(dl);
    }
    gl.glPopMatrix();
    
    gl.glBegin(gl.GL_LINES);
    {
      {
	gl.glVertex2d(52.0-sLabelTickWidth+lx, top);
	gl.glVertex2d(46.0-sLabelTickWidth+lx, top);
	
	gl.glVertex2d(46.0-sLabelTickWidth+lx, top);
	gl.glVertex2d(46.0-sLabelTickWidth+lx, top-1.0);
	
	gl.glVertex2d(52.0-sLabelTickWidth+lx, top-1.0);
	gl.glVertex2d(46.0-sLabelTickWidth+lx, top-1.0);
      }

      {
	gl.glVertex2d( 0.0, top);
	gl.glVertex2d(-8.0, top);
	
	gl.glVertex2d( 0.0, top-1.0);
	gl.glVertex2d(-8.0, top-1.0);
      }
    }
    gl.glEnd();    
      
    gl.glBegin(gl.GL_POINTS);
    {
      gl.glVertex2d(46.0-sLabelTickWidth+lx, top);
      gl.glVertex2d(46.0-sLabelTickWidth+lx, top-1.0);
    }
    gl.glEnd();
  }
  
  /**
   * Render the graph label and bracket.
   */ 
  private void 
  renderGraphTickLabels
  (
   GL gl, 
   int level, 
   Color3d lcolor, 
   boolean drawBar, 
   int range, 
   double top
  ) 
  {
    double x = ExtraMath.lerp(-33.0, -8.0, 0.5);
    double y = ((double) level) / ((double) range);
    
    if(pShowGraphBrackets) {
      double s = 12.0;
      double sy = y * pScale.y();
      double sp = s*1.5;

      {
	Integer dl = pIntegerLabelDLs.get(range);
	if(dl != null) {
	  gl.glPushMatrix();
	  {
	    gl.glColor3d(1.0, 1.0, 1.0);

	    gl.glTranslated(x, top, 0.0);
	    gl.glScaled(s, s/pScale.y(), 1.0);
	    gl.glTranslated(0.0, -0.35, 0.0);
	    gl.glCallList(dl);
	  }
	  gl.glPopMatrix();
	}
      }

      {
	Integer dl = pIntegerLabelDLs.get(0);
	if(dl != null) {
	  gl.glPushMatrix();
	  {
	    gl.glColor3d(1.0, 1.0, 1.0);

	    gl.glTranslated(x, top-1.0, 0.0);
	    gl.glScaled(s, s/pScale.y(), 1.0);
	    gl.glTranslated(0.0, -0.35, 0.0);
	    gl.glCallList(dl);
	  }
	  gl.glPopMatrix();
	}
      }

      if(drawBar && ((y*pScale.y()) > sp) && (((1.0-y)*pScale.y()) > sp)) {
	Integer dl = pIntegerLabelDLs.get(level);
	if(dl != null) {
	  gl.glPushMatrix();
	  {
	    gl.glColor3d(lcolor.r(), lcolor.g(), lcolor.b());
	  
	    gl.glTranslated(x, top-1.0+y, 0.0);
	    gl.glScaled(s, s/pScale.y(), 1.0);
	    gl.glTranslated(0.0, -0.35, 0.0);
	    gl.glCallList(dl);
	  }
	  gl.glPopMatrix();
	}
      }
    }

    if(drawBar) {
      gl.glBegin(gl.GL_LINES);
      {
	gl.glColor3d(lcolor.r(), lcolor.g(), lcolor.b());
	
	gl.glVertex2d( 0.0, top-1.0+y);
	gl.glVertex2d(-6.0, top-1.0+y);
      }
      gl.glEnd();    
    }
  }

  /**
   * Render the graph label and bracket.
   */ 
  private void 
  renderGraphTickLabels
  (
   GL gl, 
   double range, 
   double top
  ) 
  {
    double x = ExtraMath.lerp(-33.0, -8.0, 0.5);
    double s = 12.0;

    if(pShowGraphBrackets) {
      {
	String text = String.format("%1$.1f", range / 1073741824.0);
	Integer dl = pDoubleLabelDLs.get(text);
	if(dl != null) {
	  gl.glPushMatrix();
	  {
	    gl.glColor3d(1.0, 1.0, 1.0);
	    
	    gl.glTranslated(x, top, 0.0);
	    gl.glScaled(s, s/pScale.y(), 1.0);
	    gl.glTranslated(0.0, -0.35, 0.0);
	    gl.glCallList(dl);
	  }
	  gl.glPopMatrix();
	}
      }
      
      {
	Integer dl = pDoubleLabelDLs.get("0.0");
	if(dl != null) {
	  gl.glPushMatrix();
	  {
	    gl.glColor3d(1.0, 1.0, 1.0);
	    
	    gl.glTranslated(x, top-1.0, 0.0);
	    gl.glScaled(s, s/pScale.y(), 1.0);
	    gl.glTranslated(0.0, -0.35, 0.0);
	    gl.glCallList(dl);
	  }
	  gl.glPopMatrix();
	}
      }
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
    resizeViewport(gl, width, height);

    pRefreshLabels    = true;
    pRefreshGraphBars = true; 
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

    {
      int on1  = (MouseEvent.BUTTON2_DOWN_MASK |
		  MouseEvent.ALT_DOWN_MASK);
      
      int off1 = (MouseEvent.BUTTON1_DOWN_MASK | 
		  MouseEvent.BUTTON3_DOWN_MASK | 
		  MouseEvent.SHIFT_DOWN_MASK |
		  MouseEvent.CTRL_DOWN_MASK);

      
      int on2  = (MouseEvent.BUTTON1_DOWN_MASK |
		  MouseEvent.BUTTON2_DOWN_MASK | 
		  MouseEvent.ALT_DOWN_MASK);
      
      int off2 = (MouseEvent.BUTTON3_DOWN_MASK | 
		  MouseEvent.SHIFT_DOWN_MASK |
		  MouseEvent.CTRL_DOWN_MASK);


      int on3  = (MouseEvent.BUTTON3_DOWN_MASK);
      
      int off3 = (MouseEvent.BUTTON1_DOWN_MASK | 
		  MouseEvent.BUTTON2_DOWN_MASK | 
		  MouseEvent.SHIFT_DOWN_MASK |
		  MouseEvent.ALT_DOWN_MASK |
		  MouseEvent.CTRL_DOWN_MASK);

      /* <BUTTON2+ALT>: pan start */ 
      if((mods & (on1 | off1)) == on1) {
	pCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
	pDragStart = new Point2d(pMousePos);
      }

      /* <BUTTON1+BUTTON2+ALT>: zoom start */ 
      else if((mods & (on2 | off2)) == on2) {
	pCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
	pDragStart = new Point2d(pMousePos);
      }

      /* BUTTON3: popup menu */ 
      else if((mods & (on3 | off3)) == on3) {
	updatePopupMenu();
	pPopup.show(e.getComponent(), e.getX(), e.getY());
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

    else if((prefs.getToggleSystemLoad() != null) &&
       prefs.getToggleSystemLoad().wasPressed(e))
      pLoadButton.doClick();
    else if((prefs.getToggleFreeMemory() != null) &&
       prefs.getToggleFreeMemory().wasPressed(e))
      pMemButton.doClick();
    else if((prefs.getToggleFreeDisk() != null) &&
       prefs.getToggleFreeDisk().wasPressed(e))
      pDiskButton.doClick();
    else if((prefs.getToggleJobCount() != null) &&
       prefs.getToggleJobCount().wasPressed(e))
      pJobButton.doClick();

    else if((prefs.getToggleFullLoadBar() != null) &&
       prefs.getToggleFullLoadBar().wasPressed(e))
      doToggleLoadBar();
    else if((prefs.getToggleJobSlotsBar() != null) &&
       prefs.getToggleJobSlotsBar().wasPressed(e))
      doToggleSlotsBar();

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
    else if(cmd.equals("refresh")) 
      doRefresh();
    else if(cmd.equals("frame-all")) 
      doFrameAll();
    else if(cmd.equals("show-hide-load")) 
      pLoadButton.doClick();
    else if(cmd.equals("show-hide-mem")) 
      pMemButton.doClick();
    else if(cmd.equals("show-hide-disk")) 
      pDiskButton.doClick();
    else if(cmd.equals("show-hide-job")) 
      pJobButton.doClick();
    else if(cmd.equals("show-hide-load-bar")) 
      doToggleLoadBar();
    else if(cmd.equals("show-hide-slots-bar")) 
      doToggleSlotsBar();
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
    pRefreshGraphBars = true; 
    pRefreshLabels    = true;
    pRefreshTimeScale = true;
    pRefreshGraph     = true;
      
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
  
  /**
   * Toggle the display of the full load bars.
   */ 
  private void 
  doToggleLoadBar()
  {
    pShowFullLoadBar = !pShowFullLoadBar;
    doRefresh();
  }

  /**
   * Toggle the display of the job slots bars.
   */ 
  private void 
  doToggleSlotsBar()
  {
    pShowJobSlotsBar = !pShowJobSlotsBar;
    doRefresh();
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
	      master.updatePanelOp("Loading History: " + hname);
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
   * The vertical size of hostname labels.
   */ 
  private static final double  sHostnameSize = 18.0;

  /**
   * The horizontal size of graph label and tick mark geometry.
   */ 
  private static final double sLabelTickWidth = 85.0;
 

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

  /**
   * Whether to display the full load bars.
   */ 
  private boolean pShowFullLoadBar; 

  /**
   * Whether to display the job slots bars.
   */ 
  private boolean pShowJobSlotsBar; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The popup menu.
   */ 
  private JPopupMenu  pPopup;

  /**
   * The popup menu items.
   */ 
  private JMenuItem  pUpdateItem;
  private JMenuItem  pFrameAllItem;
  private JMenuItem  pShowHideLoadItem;
  private JMenuItem  pShowHideMemItem;
  private JMenuItem  pShowHideDiskItem;
  private JMenuItem  pShowHideJobItem;
  private JMenuItem  pShowHideFullLoadBarItem;
  private JMenuItem  pShowHideJobSlotsBarItem;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The OpenGL rendering canvas.
   */ 
  private GLCanvas  pCanvas;

 
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the OpenGL display list for hostname and graph labels needs to be rebuilt.
   */ 
  private boolean  pRefreshLabels;

  /**
   * The OpenGL display list handle for hostname and graph labels.
   */ 
  private AtomicInteger  pLabelsDL;

  /**
   * Whether to render the graph brackets and tick labels.
   */ 
  private boolean  pShowGraphBrackets; 

  /**
   * The OpenGL display list handles for integer graph tick labels indexed by value.
   */ 
  private TreeMap<Integer,Integer>  pIntegerLabelDLs; 

  /**
   * The OpenGL display list handles for double graph tick labels indexed by value.
   */ 
  private TreeMap<String,Integer>  pDoubleLabelDLs; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the OpenGL display list for time scale geometry needs to be rebuilt.
   */ 
  private boolean  pRefreshTimeScale;

  /**
   * The OpenGL display list handle for time scale geometry.
   */ 
  private AtomicInteger  pTimeScaleDL;


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


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the OpenGL display lists for graph bars geometry needs to be rebuilt.
   */ 
  private boolean  pRefreshGraphBars;

  /**
   * The OpenGL display list handle for graph bars geometry.
   */ 
  private AtomicInteger  pGraphBarsDL;


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the OpenGL display lists for graph geometry needs to be rebuilt.
   */ 
  private boolean  pRefreshGraph;


  /**
   * The world space horizontal bounds of the graph geometry indexed by hostname.
   */ 
  private TreeMap<String,Vector2d>  pGraphSpans; 


  /**
   * The OpenGL display list handles for the system load graph geometry indexed by hostname.
   */ 
  private TreeMap<String,Integer>  pLoadDLs; 

  /**
   * The OpenGL display list handles for the free memory graph geometry indexed by hostname.
   */ 
  private TreeMap<String,Integer>  pMemDLs; 

  /**
   * The OpenGL display list handles for the free disk graph geometry indexed by hostname.
   */ 
  private TreeMap<String,Integer>  pDiskDLs; 

  /**
   * The OpenGL display list handles for the job count graph geometry indexed by hostname.
   */ 
  private TreeMap<String,Integer>  pJobDLs; 



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
   * Whether the OpenGL viewport needs to be resized to account for a change in the 
   * border size.
   */
  private boolean  pBorderResized; 

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
