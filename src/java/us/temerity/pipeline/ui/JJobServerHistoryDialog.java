// $Id: JJobServerHistoryDialog.java,v 1.3 2004/10/05 17:52:09 jim Exp $

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
   * @param hostname
   *   The fully resolved name of the job server.
   * 
   * @param block
   *   The resource usage samples.
   */ 
  public 
  JJobServerHistoryDialog
  (
   String hostname, 
   ResourceSampleBlock block
  ) 
  {
    super("Resource Usage History", false);

    {
      pHostname = hostname;
      pBlock    = block; 

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
	super.initUI("Resource Usage History:  " + pHostname, false, body, 
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
    Date first = pBlock.getTimeStamp(0);
    Date last  = pBlock.getTimeStamp(pBlock.getNumSamples()-1);

    /* compute start time */ 
    {
      Calendar cal = Calendar.getInstance();
      cal.setTime(first);
      
      switch(pZoom) {
      case FifteenMinutes: 
      case SixMinutes:
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
      case SixMinutes:
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
      case SixMinutes:
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
    {
      int wk;
      for(wk=0; wk<pBlock.getNumSamples(); wk++) {
	
	int idx = (int) ((pBlock.getTimeStamp(wk).getTime() - pStartTime) / 
			 sInterval[pZoom.ordinal()]);

	if((idx < 0) || (idx >= pLoad.length)) {
	  System.out.print
	    ("TimeStamp[" + wk + "] = " + pBlock.getTimeStamp(wk).getTime() + "  " +
	     "Index = " + idx + "\n");
	}
	else {
	  pLoad[idx]   += pBlock.getLoad(wk);
	  pMemory[idx] += (float) pBlock.getMemory(wk);
	  pDisk[idx]   += (float) pBlock.getDisk(wk);
	  pJobs[idx]   += (float) pBlock.getNumJobs(wk);
	}
      }
    }

    /* normalize time steps */ 
    {
      int idx;
      for(idx=0; idx<pLoad.length; idx++) {
	float scale = 15000.0f / ((float) sInterval[pZoom.ordinal()]);
	
	{
	  float v = (pLoad[idx] * scale) / ((float) pBlock.getNumProcessors());
	  pLoad[idx] = Math.max(0.0f, Math.min(1.0f, v));
	}
	
	{
	  float v = (pMemory[idx] * scale) / ((float) pBlock.getTotalMemory());
	  pMemory[idx] = Math.max(0.0f, Math.min(1.0f, v));
	}
	
	{
	  float v = (pDisk[idx] * scale) / ((float) pBlock.getTotalDisk());
	  pDisk[idx] = Math.max(0.0f, Math.min(1.0f, v));
	}
	
	{
	  float v = 0.0f; 
	  if(pBlock.getJobSlots() > 0) 
	    v = (pJobs[idx] * scale) / ((float) pBlock.getJobSlots());
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
	case SixMinutes: 
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
	    JLabel label = new JLabel();
	    label.setName("HourBarLabel");
	    
	    Dimension size = new Dimension((int) (sChunk[pZoom.ordinal()]), 19);
	    label.setMinimumSize(size);
	    label.setMaximumSize(size);
	    label.setPreferredSize(size);
	    
	    hbox.add(label);
	  }
	  break;
	    
	case SixMinutes: 
	  {
	    {
	      JLabel label = new JLabel("00:00");
	      label.setName("HourBarLabel");
	      
	      Dimension size = new Dimension((int) (sChunk[pZoom.ordinal()]/4), 19);
	      label.setMinimumSize(size);
	      label.setMaximumSize(size);
	      label.setPreferredSize(size);
	      
	      hbox.add(label);
	    }
	    
	    int wk;
	    for(wk=6; wk<24; wk+=6) {
	      JLabel label = new JLabel(String.valueOf(wk) + ":00");
	      
	      Dimension size = new Dimension((int) (sChunk[pZoom.ordinal()]/4), 19);
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
	    String title = hmformat.format(date);
	    
	    JLabel label = new JLabel(title);
	    label.setName("HourBarLabel");
	    
	    Dimension size = new Dimension((int) (sChunk[pZoom.ordinal()]), 19);
	    label.setMinimumSize(size);
	    label.setMaximumSize(size);
	    label.setPreferredSize(size);
	    
	    hbox.add(label);
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
   * Zoom out.
   */ 
  public void 
  doZoomOut()
  {
    switch(pZoom) {
    case FifteenMinutes:
      return;

    case SixMinutes:
      pZoom = Zoom.FifteenMinutes;
      pZoomOutButton.setEnabled(false);
      break;

    case ThirtySeconds:
      pZoom = Zoom.SixMinutes;
      break;

    case FifteenSeconds: 
      pZoom = Zoom.ThirtySeconds; 
    }

    pZoomInButton.setEnabled(true);      
    rebuild();
  }

  /**
   * Zoom in.
   */ 
  public void 
  doZoomIn()
  {
    switch(pZoom) {
    case FifteenSeconds: 
      return;

    case ThirtySeconds: 
      pZoom = Zoom.FifteenSeconds;
      pZoomInButton.setEnabled(false);
      break;

    case SixMinutes:
      pZoom = Zoom.ThirtySeconds;
      break;

    case FifteenMinutes: 
      pZoom = Zoom.SixMinutes; 
    }

    pZoomOutButton.setEnabled(true);      
    rebuild();
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
    FifteenMinutes, SixMinutes, ThirtySeconds, FifteenSeconds
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
    new ImageIcon(LookAndFeelLoader.class.getResource("SixMinutesTimeLineIcon.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("ThirtySecondsTimeLineIcon.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("FifteenSecondsTimeLineIcon.png"))
  }; 
  
  /**
   * The interval of time (in milliseconds) for each time step. 
   */ 
  private final long  sInterval[] = { 
    900000,   /* FifteenMinutes */  
    360000,   /* SixMinutes */  
    30000,    /* ThirtySeconds */ 
    15000     /* FifteenSeconds */ 
  };

  /**
   * The number of time steps in each graph chunk.
   */ 
  private final int  sChunk[] = { 
    96,   /* FifteenMinutes */  
    240,  /* SixMinutes */  
    120,  /* ThirtySeconds */ 
    240   /* FifteenSeconds */ 
  };



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolve name of the job server.
   */
  private String pHostname; 
  
  /**
   * The resource usage samples. 
   */ 
  private ResourceSampleBlock  pBlock; 

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
