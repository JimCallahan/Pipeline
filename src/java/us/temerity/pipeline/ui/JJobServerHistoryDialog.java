// $Id: JJobServerHistoryDialog.java,v 1.1 2004/08/01 15:48:53 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*;
import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.text.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   S E R V E R    H I S T O R Y   D I A L O G                                     */
/*------------------------------------------------------------------------------------------*/

/**
 * A dialog which displays the complete resource usage history for a job server.
 */ 
public  
class JJobServerHistoryDialog
  extends JBaseDialog
  implements ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new dialog.
   * 
   * @param host
   *   The job server host.
   * 
   * @param samples
   *   The resource usage samples.
   */ 
  public 
  JJobServerHistoryDialog
  (
   QueueHost host, 
   ArrayList<ResourceSample> samples
  ) 
  {
    super("Resource Usage History", false);

    {
      pHost    = host; 
      pSamples = samples;

      pZoom    = Zoom.ThirtySeconds; 
    }
    
    /* create dialog body components */ 
    {
      JPanel body = new JPanel();
      body.setName("MainDialogPanel");

      body.setLayout(new BoxLayout(body, BoxLayout.X_AXIS));

      /* resource labels */ 
      {
	Box vbox = new Box(BoxLayout.Y_AXIS);

	vbox.add(Box.createRigidArea(new Dimension(0, 61)));

	{
	  Dimension size = new Dimension(150, 80);
	  
	  {
	    JLabel label = new JLabel("System Load", JLabel.CENTER);
	    label.setName("BlueTableCellRenderer");
	    
	    label.setMinimumSize(size);
	    label.setMaximumSize(size);
	    label.setPreferredSize(size);
	    
	    vbox.add(label);
	  }
	  
	  vbox.add(Box.createRigidArea(new Dimension(0, 3)));
	  
	  {
	    JLabel label = new JLabel("Free Memory", JLabel.CENTER);
	    label.setName("BlueTableCellRenderer");
	    
	    label.setMinimumSize(size);
	    label.setMaximumSize(size);
	    label.setPreferredSize(size);
	    
	    vbox.add(label);
	  }
	  
	  vbox.add(Box.createRigidArea(new Dimension(0, 3)));
	  
	  {
	    JLabel label = new JLabel("Free Disk Space", JLabel.CENTER);
	    label.setName("BlueTableCellRenderer");
	    
	    label.setMinimumSize(size);
	    label.setMaximumSize(size);
	    label.setPreferredSize(size);
	    
	    vbox.add(label);
	  }
	  
	  vbox.add(Box.createRigidArea(new Dimension(0, 3)));
	  
	  {
	    JLabel label = new JLabel("Jobs", JLabel.CENTER);
	    label.setName("GreenTableCellRenderer");
	    
	    label.setMinimumSize(size);
	    label.setMaximumSize(size);
	    label.setPreferredSize(size);
	    
	    vbox.add(label);
	  }
	}

	vbox.add(Box.createRigidArea(new Dimension(0, 20)));
	vbox.add(Box.createVerticalGlue());
	  
	body.add(vbox);
      }

      body.add(Box.createRigidArea(new Dimension(3, 0)));

      {
	Box vbox = new Box(BoxLayout.Y_AXIS);

	JViewport headerViewport = null;
	{
  	  JPanel panel = new JPanel();
	  
  	  panel.setName("TitleValuePanel");
  	  panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

	  {
	    JViewport view = new JViewport();
	    pHeaderViewport = view; 

	    {
	      JPanel dummy = new JPanel(); 
	      view.setView(dummy); 
	    }
	    
	    panel.add(view);
	  }

	  panel.setMinimumSize(new Dimension(200, 57));
	  panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 57));
	  panel.setPreferredSize(new Dimension(200, 57));

  	  vbox.add(panel);
	}

	vbox.add(Box.createRigidArea(new Dimension(0, 1)));
	
	{
	  JPanel dummy = new JPanel(); 

	  {
	    JScrollPane scroll = new JScrollPane();
	    pScrollPane = scroll;
	    
	    scroll.setViewportView(dummy);
	    
	    scroll.setHorizontalScrollBarPolicy
	      (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
	    scroll.setVerticalScrollBarPolicy
	      (ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
	    
	    scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

	    scroll.setMinimumSize(new Dimension(200, 346));
	    scroll.setPreferredSize(new Dimension(800, 346));
	    
	    vbox.add(scroll);
	  }
	}
	
	body.add(vbox);
      }

      String extra[][] = {
	{ "Zoom In",  "zoom-in" }, 
	{ "Zoom Out", "zoom-out" }
      };

      JButton btns[] = 
	super.initUI("Resource Usage History:  " + pHost.getName(), false, body, 
		     null, null, extra, "Close");

      pZoomInButton  = btns[0];
      pZoomOutButton = btns[1];
    }

    rebuild();

    {
      AdjustLinkage linkage = 
	new AdjustLinkage(pScrollPane.getViewport(), pHeaderViewport);
      pScrollPane.getHorizontalScrollBar().addAdjustmentListener(linkage);
    }
  }
    

  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/


  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Rebuild the timeline header and graphs.
   */ 
  private void
  rebuild() 
  {
    if(pSamples.isEmpty()) {
      pHeaderViewport.setView(new JPanel());
      pScrollPane.setViewportView(new JPanel());
      
      pStartTime    = 0;
      pTimeInterval = 0;

      pLoad   = null;
      pMemory = null;
      pDisk   = null;
      pJobs   = null;
      
      return;
    }
    
    Date first = null;
    Date last  = null;
    for(ResourceSample sample : pSamples) {
      Date stamp = sample.getTimeStamp();
      if((first == null) || (first.compareTo(stamp) > 0)) 
	first = stamp;
      if((last == null) || (last.compareTo(stamp) < 0)) 
	last = stamp;
    }

    /* compute start time */ 
    {
      Calendar cal = Calendar.getInstance();
      cal.setTime(first);
      
      switch(pZoom) {
      case FifteenMinutes: 
	cal.set(Calendar.HOUR_OF_DAY, 0);

      case FifteenSeconds: 
      case ThirtySeconds:
	cal.set(Calendar.MILLISECOND, 0);
	cal.set(Calendar.SECOND, 0);
	cal.set(Calendar.MINUTE, 0); 	
      }
      
      pStartTime = cal.getTimeInMillis();
    }

    /* compute time interval */ 
    {
      Calendar cal = Calendar.getInstance();
      cal.setTime(last);
      
      switch(pZoom) {
      case FifteenMinutes: 
	cal.set(Calendar.HOUR_OF_DAY, 0);

      case FifteenSeconds: 
      case ThirtySeconds:
	cal.set(Calendar.MILLISECOND, 0);
	cal.set(Calendar.SECOND, 0);
	cal.set(Calendar.MINUTE, 0); 	
      }

      switch(pZoom) {
      case FifteenSeconds: 
      case ThirtySeconds:
	cal.add(Calendar.HOUR_OF_DAY, 1);
	break;
	
      case FifteenMinutes: 
	cal.add(Calendar.DAY_OF_MONTH, 1);
      }
      
      pTimeInterval = cal.getTimeInMillis() - pStartTime;
    }

    /* initialize time steps */ 
    {
      int steps = (int) (pTimeInterval / sInterval[pZoom.ordinal()]);
      pLoad   = new float[steps];
      pMemory = new float[steps];
      pDisk   = new float[steps]; 
      pJobs   = new float[steps]; 
    }
    
    /* accumilate samples */ 
    for(ResourceSample sample : pSamples) {
      int idx = 
	(int) ((sample.getTimeStamp().getTime() - pStartTime) / sInterval[pZoom.ordinal()]);

      pLoad[idx]   += sample.getLoad();
      pMemory[idx] += (float) sample.getMemory();
      pDisk[idx]   += (float) sample.getDisk();
      pJobs[idx]   += (float) sample.getNumJobs();
    }

    /* normalize time steps */ 
    {
      int idx;
      for(idx=0; idx<pLoad.length; idx++) {
	float scale = 15000.0f / ((float) sInterval[pZoom.ordinal()]);
	
	{
	  float v = 0.0f;
	  if(pHost.getNumProcessors() != null)
	    v = (pLoad[idx] * scale) / ((float) pHost.getNumProcessors());
	  pLoad[idx] = Math.max(0.0f, Math.min(1.0f, v));
	}
	
	{
	  float v = 0.0f; 
	  if(pHost.getTotalMemory() != null) 
	    v = (pMemory[idx] * scale) / ((float) pHost.getTotalMemory());
	  pMemory[idx] = Math.max(0.0f, Math.min(1.0f, v));
	}
	
	{
	  float v = 0.0f; 
	  if(pHost.getTotalDisk() != null) 
	    v = (pDisk[idx] * scale) / ((float) pHost.getTotalDisk());
	  pDisk[idx] = Math.max(0.0f, Math.min(1.0f, v));
	}
	
	{
	  float v = 0.0f; 
	  if(pHost.getJobSlots() > 0) 
	    v = (pJobs[idx] * scale) / ((float) pHost.getJobSlots());
	  pJobs[idx] = Math.max(0.0f, Math.min(1.0f, v));
	}
      }
    }

    /* rebuild header */ 
    rebuildHeader();

    /* rebuild graphs */ 
    {
      Box box = new Box(BoxLayout.X_AXIS);

      {
	Box vbox = new Box(BoxLayout.Y_AXIS);
	
	/* system load */ 
	{
	  JPanel graph = new JPanel();
	  graph.setLayout(new OverlayLayout(graph)); 
	  
	  {
	    JBarGraph bar = new JBarGraph();
	    
	    bar.setAlignmentX(0.0f);
	    
	    bar.setValues(pLoad, false);
	    
	    bar.setHighlight(new Color(1.0f, 1.0f, 0.0f)); 
	    bar.setForeground(new Color(0.0f, 0.59f, 1.0f));
	    
	    Dimension size = new Dimension(pLoad.length, 74);
	    bar.setMinimumSize(size);
	    bar.setMaximumSize(size);
	    bar.setPreferredSize(size);
	    
	    graph.add(bar);
	  }
	  
	  {
	    Box hbox = new Box(BoxLayout.X_AXIS);
	    
	    hbox.setAlignmentX(0.0f);

	    int idx; 
	    for(idx=0; idx<pLoad.length; idx+=sChunk[pZoom.ordinal()]) {
	      JPanel panel = new JPanel();
	      panel.setName("BlueGraphPanel");
	      
	      Dimension size = new Dimension(sChunk[pZoom.ordinal()], 80);
	      panel.setMinimumSize(size);
	      panel.setMaximumSize(size);
	      panel.setPreferredSize(size);
	      
	      hbox.add(panel);
	    }
	    
	    graph.add(hbox);
	  }
	  
	  vbox.add(graph);
	}
	
	vbox.add(Box.createRigidArea(new Dimension(0, 3)));
	
	/* free memory */ 
	{
	  JPanel graph = new JPanel();
	  graph.setLayout(new OverlayLayout(graph)); 
	  
	  {
	    JBarGraph bar = new JBarGraph();
	    
	    bar.setAlignmentX(0.0f);

	    bar.setValues(pMemory, false);
	    
	    bar.setHighlight(new Color(1.0f, 1.0f, 0.0f)); 
	    bar.setForeground(new Color(0.0f, 0.59f, 1.0f));
	    
	    Dimension size = new Dimension(pMemory.length, 74);
	    bar.setMinimumSize(size);
	    bar.setMaximumSize(size);
	    bar.setPreferredSize(size);
	    
	    graph.add(bar);
	  }
	  
	  {
	    Box hbox = new Box(BoxLayout.X_AXIS);
	    
	    hbox.setAlignmentX(0.0f);
	    
	    int idx; 
	    for(idx=0; idx<pMemory.length; idx+=sChunk[pZoom.ordinal()]) {
	      JPanel panel = new JPanel();
	      panel.setName("BlueGraphPanel");
	      
	      Dimension size = new Dimension(sChunk[pZoom.ordinal()], 80);
	      panel.setMinimumSize(size);
	      panel.setMaximumSize(size);
	      panel.setPreferredSize(size);
	      
	      hbox.add(panel);
	    }
	    
	    graph.add(hbox);
	  }
	  
	  vbox.add(graph);
	}
	
	vbox.add(Box.createRigidArea(new Dimension(0, 3)));
	
	/* free disk */ 
	{
	  JPanel graph = new JPanel();
	  graph.setLayout(new OverlayLayout(graph)); 
	  
	  {
	    JBarGraph bar = new JBarGraph();
	    
	    bar.setAlignmentX(0.0f);
	    
	    bar.setValues(pDisk, false);
	    
	    bar.setHighlight(new Color(1.0f, 1.0f, 0.0f)); 
	    bar.setForeground(new Color(0.0f, 0.59f, 1.0f));
	    
	    Dimension size = new Dimension(pDisk.length, 74);
	    bar.setMinimumSize(size);
	    bar.setMaximumSize(size);
	    bar.setPreferredSize(size);

	    graph.add(bar);
	  }
	  
	  {
	    Box hbox = new Box(BoxLayout.X_AXIS);
	    
	    hbox.setAlignmentX(0.0f);
	  
	    int idx; 
	    for(idx=0; idx<pDisk.length; idx+=sChunk[pZoom.ordinal()]) {
	      JPanel panel = new JPanel();
	      panel.setName("BlueGraphPanel");
	      
	      Dimension size = new Dimension(sChunk[pZoom.ordinal()], 80);
	      panel.setMinimumSize(size);
	      panel.setMaximumSize(size);
	      panel.setPreferredSize(size);
	      
	      hbox.add(panel);
	    }
	    
	    graph.add(hbox);
	  }
	  
	  vbox.add(graph);
	}

	vbox.add(Box.createRigidArea(new Dimension(0, 3)));
	
	/* jobs */ 
	{
	  JPanel graph = new JPanel();
	  graph.setLayout(new OverlayLayout(graph)); 
	  
	  {
	    JBarGraph bar = new JBarGraph();
	    
	    bar.setAlignmentX(0.0f);
	    
	    bar.setValues(pJobs, false);
	    
	    bar.setHighlight(new Color(1.0f, 0.0f, 0.0f));
	    bar.setForeground(new Color(0.0f, 1.0f, 0.0f));
	    
	    Dimension size = new Dimension(pJobs.length, 74);
	    bar.setMinimumSize(size);
	    bar.setMaximumSize(size);
	    bar.setPreferredSize(size);
	    
	    graph.add(bar);
	  }
	  
	  {
	    Box hbox = new Box(BoxLayout.X_AXIS);
	    
	    hbox.setAlignmentX(0.0f);
	    
	    int idx; 
	    for(idx=0; idx<pJobs.length; idx+=sChunk[pZoom.ordinal()]) {
	      JPanel panel = new JPanel();
	      panel.setName("GreenGraphPanel");
	      
	      Dimension size = new Dimension(sChunk[pZoom.ordinal()], 80);
	      panel.setMinimumSize(size);
	      panel.setMaximumSize(size);
	      panel.setPreferredSize(size);
	    
	      hbox.add(panel);
	    }
	    
	    graph.add(hbox);
	  }
	  
	  vbox.add(graph);
	}

	vbox.add(Box.createVerticalGlue());

	box.add(vbox);
      }
	
      {
	Box vbox = new Box(BoxLayout.Y_AXIS);
	
	int wk;
	for(wk=0; wk<4; wk++) {
	  {
	    JLabel label = new JLabel();
	    label.setName("HourBarLabel");
	    
	    label.setAlignmentX(0.0f);
	    
	    Dimension size = new Dimension(1, 80);
	    label.setMinimumSize(size);
	    label.setMaximumSize(size);
	    label.setPreferredSize(size);
	    
	    vbox.add(label);
	  }
	  
	  if(wk < 3)
	    vbox.add(Box.createRigidArea(new Dimension(0, 3)));
	}
	
	vbox.add(Box.createVerticalGlue());

	box.add(vbox);
      }
      
      box.add(Box.createHorizontalGlue());
    
      pScrollPane.setViewportView(box);
    }
  }


  private void 
  rebuildHeader() 
  {
    SimpleDateFormat dformat  = new SimpleDateFormat("yyyy-MM-dd"); 
    SimpleDateFormat dhformat = new SimpleDateFormat("yyyy-MM-dd  HH:mm"); 
    SimpleDateFormat hmformat = new SimpleDateFormat("HH:mm"); 

    Box box = new Box(BoxLayout.X_AXIS);
    
    long step = sChunk[pZoom.ordinal()] * sInterval[pZoom.ordinal()];
    long time;
    for(time=pStartTime; time<(pStartTime+pTimeInterval); time+=step) {
      Box vbox = new Box(BoxLayout.Y_AXIS);

      {
	String title = null;
	switch(pZoom) {
	case FifteenMinutes: 
	case ThirtySeconds:
	  title = dformat.format(new Date(time));
	  break;
	  
	case FifteenSeconds: 
	  title = dhformat.format(new Date(time));
	}
	
	JLabel label = new JLabel(title);
	label.setName("HourBarLabel");
	
	label.setAlignmentX(0.0f);

	Dimension size = new Dimension(sChunk[pZoom.ordinal()], 19);
	label.setMinimumSize(size);
	label.setMaximumSize(size);
	label.setPreferredSize(size);
	
	vbox.add(label);
      }

      {
	Box hbox = new Box(BoxLayout.X_AXIS);

	hbox.setAlignmentX(0.0f);

	switch(pZoom) {
	case FifteenMinutes: 
	  {
	    {
	      JLabel label = new JLabel("00:");
	      label.setName("HourBarLabel");
	      
	      Dimension size = new Dimension((int) (sChunk[pZoom.ordinal()]/2), 19);
	      label.setMinimumSize(size);
	      label.setMaximumSize(size);
	      label.setPreferredSize(size);
	      
	      hbox.add(label);
	    }
    
	    {
	      JLabel label = new JLabel("12:");
	      
	      Dimension size = new Dimension((int) (sChunk[pZoom.ordinal()]/2), 19);
	      label.setMinimumSize(size);
	      label.setMaximumSize(size);
	      label.setPreferredSize(size);
	      
	      hbox.add(label);
	    }
	  }
	  break;
	    
	case ThirtySeconds:
	  {
	    Date date = new Date(time); 

	    {
	      String title = hmformat.format(date);

	      JLabel label = new JLabel(title);
	      label.setName("HourBarLabel");
	      
	      Dimension size = new Dimension((int) (sChunk[pZoom.ordinal()]/2), 19);
	      label.setMinimumSize(size);
	      label.setMaximumSize(size);
	      label.setPreferredSize(size);
	      
	      hbox.add(label);
	    }

	    {
	      Calendar cal = Calendar.getInstance();
	      cal.setTime(date);
	      cal.add(Calendar.MINUTE, 30);
	      String title = hmformat.format(cal.getTime());

	      JLabel label = new JLabel(title);
	      
	      Dimension size = new Dimension((int) (sChunk[pZoom.ordinal()]/2), 19);
	      label.setMinimumSize(size);
	      label.setMaximumSize(size);
	      label.setPreferredSize(size);
	      
	      hbox.add(label);
	    }
	  }
	  break;

	case FifteenSeconds:
	  {
	    {
	      JLabel label = new JLabel(":00");
	      label.setName("HourBarLabel");
	      
	      Dimension size = new Dimension((int) (sChunk[pZoom.ordinal()]/4), 19);
	      label.setMinimumSize(size);
	      label.setMaximumSize(size);
	      label.setPreferredSize(size);
	      
	      hbox.add(label);
	    }

	    int wk;
	    for(wk=15; wk<60; wk+=15) {
	      JLabel label = new JLabel(":" + String.valueOf(wk));
	      
	      Dimension size = new Dimension((int) (sChunk[pZoom.ordinal()]/4), 19);
	      label.setMinimumSize(size);
	      label.setMaximumSize(size);
	      label.setPreferredSize(size);
	      
	      hbox.add(label);
	    }
	  }
	}

	vbox.add(hbox);
      }

      {
	JLabel label = new JLabel(sTimeLineIcons[pZoom.ordinal()]);
	
	label.setAlignmentX(0.0f);

	Dimension size = new Dimension((int) sChunk[pZoom.ordinal()], 11);
	label.setMinimumSize(size);
	label.setMaximumSize(size);
	label.setPreferredSize(size);
	
	vbox.add(label);
      }

      box.add(vbox);
    }

    {
      JLabel label = new JLabel();
      label.setName("HourBarLabel");
	
      label.setAlignmentX(0.0f);
      
      Dimension size = new Dimension(1, 47);
      label.setMinimumSize(size);
      label.setMaximumSize(size);
      label.setPreferredSize(size);
      
      box.add(label);
    }
      
    box.add(Box.createHorizontalGlue());
    
    pHeaderViewport.setView(box);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

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
    super.actionPerformed(e);

    String cmd = e.getActionCommand();
    if(cmd.equals("zoom-in")) 
      doZoomIn();
    else if(cmd.equals("zoom-out")) 
      doZoomOut();
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Zoom in.
   */ 
  public void 
  doZoomIn()
  {
    switch(pZoom) {
    case FifteenMinutes:
      pZoom = Zoom.ThirtySeconds;
      pZoomOutButton.setEnabled(true);
      pZoomInButton.setEnabled(true);
      rebuild();
      break;

    case ThirtySeconds: 
      pZoom = Zoom.FifteenSeconds;
      pZoomOutButton.setEnabled(true);
      pZoomInButton.setEnabled(false);
      rebuild();
    }
  }

  /**
   * Zoom out.
   */ 
  public void 
  doZoomOut()
  {
    switch(pZoom) {
    case ThirtySeconds: 
      pZoom = Zoom.FifteenMinutes;
      pZoomOutButton.setEnabled(false);
      pZoomInButton.setEnabled(true);
      rebuild();
      break;

    case FifteenSeconds:
      pZoom = Zoom.ThirtySeconds;
      pZoomOutButton.setEnabled(true);
      pZoomInButton.setEnabled(true);
      rebuild();
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The smallest time interval displayed by the graphs.
   */
  private 
  enum Zoom
  { 
    FifteenMinutes, ThirtySeconds, FifteenSeconds
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3782368762005034982L;

  /**
   * The time line rule graphics indexed by Zoom.
   */ 
  private static Icon sTimeLineIcons[] = {
    new ImageIcon(LookAndFeelLoader.class.getResource("FifteenMinutesTimeLineIcon.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("ThirtySecondsTimeLineIcon.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("FifteenSecondsTimeLineIcon.png"))
  }; 
  
  /**
   * The interval of time (in milliseconds) for each time step. 
   */ 
  private final long  sInterval[] = { 
    900000,   /* FifteenMinutes */  
    30000,    /* ThirtySeconds */ 
    15000     /* FifteenSeconds */ 
  };

  /**
   * The number of time steps in each graph chunk.
   */ 
  private final int  sChunk[] = { 
    96,   /* FifteenMinutes */  
    120,  /* ThirtySeconds */ 
    240   /* FifteenSeconds */ 
  };



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The host being displayed.
   */
  private QueueHost  pHost; 

  /**
   * The resource usage samples. 
   */ 
  private ArrayList<ResourceSample>  pSamples; 

  /**
   * The smallest time interval displayed by the graphs.
   */ 
  private Zoom pZoom; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The timestamp (in milliseconds) of the frist time step.
   */ 
  private long  pStartTime; 

  /**
   * The total time interval of (in milliseconds) all time steps.
   */ 
  private long  pTimeInterval; 

  
  /**
   * The normalized system load for each time step.
   */ 
  private float[]  pLoad;   

  /**
   * The normalized free memory for each time step.
   */ 
  private float[]  pMemory;  

  /**
   * The normalized free disk space for each time step.
   */  
  private float[]  pDisk; 

  /**
   * The normalized number of jobs for each time step.
   */   
  private float[]  pJobs; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The timeline header viewport.
   */ 
  private JViewport  pHeaderViewport;

  /**
   * The graph scroll pane.
   */ 
  private JScrollPane  pScrollPane; 

  /**
   * The panel buttons.
   */ 
  private JButton  pZoomInButton;
  private JButton  pZoomOutButton;
}
