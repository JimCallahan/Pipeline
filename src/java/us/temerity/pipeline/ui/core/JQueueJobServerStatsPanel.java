// $Id: JQueueJobServerStatsPanel.java,v 1.1 2006/12/05 18:23:30 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.math.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import net.java.games.jogl.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   J O B   S E R V E R   S T A T S   P A N E L                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The viewer of the data contained in {@link QueueHostHistograms}.
 */ 
public  
class JQueueJobServerStatsPanel
  extends JBaseViewerPanel
  implements KeyListener, PopupMenuListener, ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new panel with the default working area view.
   */
  public 
  JQueueJobServerStatsPanel()
  {
    super();
    initUI();
  }

  /**
   * Construct a new panel with a working area view identical to the given panel.
   */
  public 
  JQueueJobServerStatsPanel
  (
   JTopLevelPanel panel
  )
  {
    super(panel);
    initUI();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the common user interface components.
   */ 
  private synchronized void 
  initUI()
  {  
    super.initUI(128.0, false);

    /* initialize fields */ 
    {
      pHistograms = new QueueHostHistograms(QueueHostHistogramSpecs.getDefault());
      pWorkGroups = new TreeSet<String>();
      pViewerPies = new TreeMap<String,ViewerPie>();
    }

    /* panel popup menu */ 
    {
      JMenuItem item;
      
      pPanelPopup = new JPopupMenu();  
      pPanelPopup.addPopupMenuListener(this);

      item = new JMenuItem("Update");
      pUpdateItem = item;
      item.setActionCommand("update");
      item.addActionListener(this);
      pPanelPopup.add(item);  

      pPanelPopup.addSeparator();

      item = new JMenuItem("Frame All");
      pFrameAllItem = item;
      item.setActionCommand("frame-all");
      item.addActionListener(this);
      pPanelPopup.add(item);  
    }
    
    /* chart popup menu */ 
    {
      JMenuItem item;
      
      pChartPopup = new JPopupMenu();  
      pChartPopup.addPopupMenuListener(this);

      item = new JMenuItem("Edit Chart...");
      pEditChartItem = item;
      item.setActionCommand("edit-chart");
      item.addActionListener(this);
      pChartPopup.add(item);  
    }

    updateMenuToolTips();

    /* initialize the panel components */ 
    {
      pCanvas.addKeyListener(this);
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Get the title of this type of panel.
   */
  public String 
  getTypeName() 
  {
    return "Queue Stats";
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the group ID. <P> 
   * 
   * Group ID values must be in the range: [1-9]
   * 
   * @param groupID
   *   The new group ID or (0) for no group assignment.
   */ 
  public void
  setGroupID
  (
   int groupID
  )
  {
    UIMaster master = UIMaster.getInstance();

    PanelGroup<JQueueJobServerStatsPanel> panels = master.getQueueJobServerStatsPanels();

    if(pGroupID > 0)
      panels.releaseGroup(pGroupID);

    pGroupID = 0;
    if((groupID > 0) && panels.isGroupUnused(groupID)) {
      panels.assignGroup(this, groupID);
      pGroupID = groupID;
    }

    master.updateOpsBar();
  }

  /**
   * Is the given group currently unused for this type of panel.
   */ 
  public boolean
  isGroupUnused
  (
   int groupID
  ) 
  {
    PanelGroup<JQueueJobServerStatsPanel> panels = 
      UIMaster.getInstance().getQueueJobServerStatsPanels();
    return panels.isGroupUnused(groupID);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Are the contents of the panel read-only. <P> 
   */ 
  public boolean
  isLocked() 
  {
    return (super.isLocked() && !pPrivilegeDetails.isQueueManaged(pAuthor));
  }
  
  /**
   * Set the author and view.
   */ 
  public synchronized void 
  setAuthorView
  (
   String author, 
   String view 
  ) 
  {
    super.setAuthorView(author, view);    

    updatePanels();
  }




  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Update all panels which share the current update channel.
   */ 
  private void 
  updatePanels() 
  {
    PanelUpdater pu = new PanelUpdater(this);
    pu.start();
  }

  /**
   * Apply the updated information to this panel.
   * 
   * @param author
   *   Owner of the current working area.
   * 
   * @param view
   *   Name of the current working area view.
   * 
   * @param hist
   *   The frequency distribution data for significant catagories of information shared 
   *    by all job server hosts.
   * 
   * @param workGroups
   *   The names of the user work groups.
   */
  public synchronized void 
  applyPanelUpdates
  (
   String author, 
   String view,
   QueueHostHistograms hist,
   Set<String> workGroups
  )
  {
    if(!pAuthor.equals(author) || !pView.equals(view)) 
      super.setAuthorView(author, view);    

    updateHistograms(hist, workGroups);
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the job server histograms.
   * 
   * @param hist
   *   The frequency distribution data for significant catagories of information shared 
   *    by all job server hosts.
   * 
   * @param workGroups
   *   The names of the user work groups.
   */ 
  public synchronized void
  updateHistograms
  (
   QueueHostHistograms hist,
   Set<String> workGroups
  ) 
  {
    if(UIMaster.getInstance().isRestoring())
      return;

    updatePrivileges();
    
    /* update the histogram data */ 
    pHistograms = hist;
    pNewHistogramSpecs = null;

    pWorkGroups.clear();
    pWorkGroups.addAll(workGroups);

    /* update the visualization graphics */ 
    updateUniverse();
  }


  /**
   * Get the current job server histogram specifications.
   */
  public synchronized QueueHostHistogramSpecs
  getHistogramSpecs() 
  {
    if(pNewHistogramSpecs != null) 
      return pNewHistogramSpecs;
    return new QueueHostHistogramSpecs(pHistograms);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the panel to reflect new user preferences.
   */ 
  public void 
  updateUserPrefs() 
  {
    updateUniverse();
    updateMenuToolTips();
  }

  /**
   * Update the menu item tool tips.
   */ 
  private void 
  updateMenuToolTips() 
  {
    UserPrefs prefs = UserPrefs.getInstance();
  
    /* panel menu */ 
    updateMenuToolTip
      (pUpdateItem, prefs.getUpdate(), 
       "Update the status of all queue statistics.");
    updateMenuToolTip
      (pFrameAllItem, prefs.getFrameAll(), 
       "Move the camera to frame all queue statistics charts.");

    /* chart menu */ 
    updateMenuToolTip
      (pEditChartItem, prefs.getJobServerStatsEditChart(), 
       "Edit the chart catagory ranges.");
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the visualization graphics.
   */
  private synchronized void 
  updateUniverse()
  {  
    /* remove all previous pie charts */ 
    pViewerPies.clear();

    UserPrefs prefs = UserPrefs.getInstance();
    if(pHistograms != null) {
      {
	ViewerPie pie = new ViewerPie(pHistograms.getStatusHist(), false);
	pViewerPies.put(pie.getName(), pie);

	Color3d[] colors = {
	  new Color3d(0.0, 0.75, 0.0), 
	  new Color3d(0.0, 0.5, 0.0), 
	  new Color3d(0.95, 0.95, 0.0), 
	  new Color3d(0.85, 0.85, 0.0), 
	  new Color3d(0.90, 0.0, 0.0), 
	  new Color3d(0.65, 0.0, 0.0),
	  new Color3d(0.08, 0.85, 0.85), 
	};
	pie.setColors(colors);
      }

      {
	ViewerPie pie = new ViewerPie(pHistograms.getOsTypeHist(), false);
	pViewerPies.put(pie.getName(), pie);

	pie.setColors(new Color3d(0.8, 0.58, 0.32), new Color3d(0.4, 0.29, 0.16));
      }

      {
	Color3d dark  = new Color3d(0.0, 0.24, 0.4); 
	Color3d light = new Color3d(0.0, 0.5, 0.9);

	{
	  ViewerPie pie = new ViewerPie(pHistograms.getLoadHist(), false);
	  pViewerPies.put(pie.getName(), pie);
	  
	  pie.setColors(dark, light); 
	}

	{
	  ViewerPie pie = new ViewerPie(pHistograms.getMemoryHist(), true);
	  pViewerPies.put(pie.getName(), pie);
	  
	  pie.setColors(light, dark); 
	}
	
	{
	  ViewerPie pie = new ViewerPie(pHistograms.getDiskHist(), true);
	  pViewerPies.put(pie.getName(), pie);
	  
	  pie.setColors(light, dark); 
	}
      }

      {
	Color3d dark  = new Color3d(0.0, 0.4, 0.0); 
	Color3d light = new Color3d(0.0, 0.8, 0.0);

	{
	  ViewerPie pie = new ViewerPie(pHistograms.getNumJobsHist(), false);
	  pViewerPies.put(pie.getName(), pie);
	  
	  pie.setColors(dark, light); 
	}
      
	{
	  ViewerPie pie = new ViewerPie(pHistograms.getSlotsHist(), false);
	  pViewerPies.put(pie.getName(), pie);
	  
	  pie.setColors(dark, light); 
	}
      }
      
      {
	Color3d dark  = new Color3d(0.4, 0.0, 0.37); 
	Color3d light = new Color3d(0.8, 0.0, 0.75);

	{
	  ViewerPie pie = 
	    new ReservedViewerPie(pHistograms.getReservationsHist(), pWorkGroups);
	  pViewerPies.put(pie.getName(), pie);
	  
	  pie.setColors(dark, light); 
	}

	{
	  ViewerPie pie = new ViewerPie(pHistograms.getOrderHist(), false);
	  pViewerPies.put(pie.getName(), pie);
	  
	  pie.setColors(dark, light); 
	}

	{
	  ViewerPie pie = new ViewerPie(pHistograms.getGroupsHist(), false);
	  pViewerPies.put(pie.getName(), pie);
	  
	  pie.setColors(dark, light); 
	}

	{
	  ViewerPie pie = new ViewerPie(pHistograms.getSchedulesHist(), false);
	  pViewerPies.put(pie.getName(), pie);
	  
	  pie.setColors(dark, light); 
	}
      }


      /* 6x2 layout */ 
      {
	Point2d pos = new Point2d();
	double dx = 7.25; 
	double dy = 5.0;

	{
	  String titles[] = {
	    "ServerStatus", 
	    "SystemLoad", 
	    "FreeMemory", 
	    "FreeDisk", 
	    "JobCounts", 
	    "JobSlots"
	  };

	  int wk;
	  for(wk=0; wk<titles.length; wk++) {
	    ViewerPie pie = pViewerPies.get(titles[wk]); 
	    pie.setPosition(pos);
	    pos.add(new Vector2d(dx, 0.0));
	  }
	}

	pos = new Point2d(dx*0.5, -dy); 
	
	{
	  String titles[] = {
	    "ServerOS", 
	    "Reserved", 
	    "Order", 
	    "Groups", 
	    "Schedules"
	  };

	  int wk;
	  for(wk=0; wk<titles.length; wk++) {
	    ViewerPie pie = pViewerPies.get(titles[wk]); 
	    pie.setPosition(pos);
	    pos.add(new Vector2d(dx, 0.0));
	  }
	}
      }
    }
   
    /* render the changes */ 
    refresh();
  }
  
  /**
   * Get the bounding box which contains all of the pie charts. <P> 
   * 
   * @return 
   *   The bounding box or <CODE>null</CODE> if no pie charts are displayed. 
   */ 
  private BBox2d
  getChartBounds() 
  {
    BBox2d bbox = null;
    for(ViewerPie vpie : pViewerPies.values()) {
      if(bbox == null) 
 	bbox = vpie.getFullBounds();
      else 
 	bbox.grow(vpie.getFullBounds());
    }
    
    return bbox;
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Compute world coordinates for mouse position.
   */ 
  private synchronized Point2d
  getWorldMousePos()
  {
    if(pMousePos == null) 
      return null;

    Point2d pos = new Point2d(pMousePos);

    Dimension size = pCanvas.getSize();
    Vector2d half = new Vector2d(size.getWidth()*0.5, size.getHeight()*0.5);
    
    double f = -pCameraPos.z() * pPerspFactor;
    Vector2d persp = new Vector2d(f * pAspect, f);
    
    Vector2d camera = new Vector2d(pCameraPos.x(), pCameraPos.y());
    
    pos.sub(half).mult(pCanvasToScreen).mult(persp).sub(camera);

    return pos;
  }

  /**
   * Get the pie chart under the current world position. <P> 
   */ 
  private synchronized ViewerPie
  pieAtWorldPos
  (
   Point2d pos
  ) 
  {
    if(pos == null) 
      return null;
    
    /* check each pie chart */ 
    for(ViewerPie vpie : pViewerPies.values()) {
      if(vpie.isInside(pos)) 
	return vpie; 
    }

    return null;
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
    super.init(drawable);
    GL gl = drawable.getGL();

    /* global OpenGL state */ 
    {
      gl.glEnable(GL.GL_LINE_SMOOTH);
      gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_DONT_CARE);
    }
  }

  /**
   * Called by the drawable to initiate OpenGL rendering by the client.
   */ 
  public void 
  display
  (
   GLDrawable drawable
  )
  {
    super.display(drawable); 
    GL gl = drawable.getGL();

    /* render the scene geometry */ 
    {
      if(pRefreshScene) {
	rebuildAll(gl);

	{
	  UIMaster master = UIMaster.getInstance(); 
	  master.freeDisplayList(pSceneDL.getAndSet(master.getDisplayList(gl)));
	}

	gl.glNewList(pSceneDL.get(), GL.GL_COMPILE_AND_EXECUTE);
	  renderAll(gl);
	gl.glEndList();

	pRefreshScene = false;
      }
      else {
	gl.glCallList(pSceneDL.get());
      }
    }    
  }
   
  /** 
   * Syncronized display list building helper.
   */ 
  private synchronized void
  rebuildAll
  (
   GL gl
  ) 
  {
    for(ViewerPie vpie : pViewerPies.values()) 
      vpie.rebuild(gl);
  }
  
  /** 
   * Syncronized rendering helper.
   */ 
  private synchronized void
  renderAll
  (
   GL gl
  ) 
  {
    for(ViewerPie vpie : pViewerPies.values()) 
      vpie.render(gl);
  }



  /*-- MOUSE LISTENER METHODS --------------------------------------------------------------*/

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

    /* manager panel popups */ 
    if(pManagerPanel.handleManagerMouseEvent(e)) 
      return;

    /* local mouse events */ 
    Point2d worldPos = getWorldMousePos();
    pCurrentViewerPie = pieAtWorldPos(worldPos);

    /* mouse press is over a pickable object viewer job */ 
    if(pCurrentViewerPie != null) {
      switch(e.getButton()) {
      case MouseEvent.BUTTON1:
	{
	  int on1  = (MouseEvent.BUTTON1_DOWN_MASK);
	  
	  int off1 = (MouseEvent.BUTTON2_DOWN_MASK | 
		      MouseEvent.BUTTON3_DOWN_MASK | 
		      MouseEvent.SHIFT_DOWN_MASK |
		      MouseEvent.ALT_DOWN_MASK |
		      MouseEvent.CTRL_DOWN_MASK);
	  
	  /* BUTTON1: toggle the selection */ 
	  if((mods & (on1 | off1)) == on1) {
	    Histogram hist = pCurrentViewerPie.getHistogram();
	    if(pCurrentViewerPie.isInsideCenter(worldPos)) 
	      hist.setIncluded(!hist.anyIncluded());
	    else {
	      Integer idx = pCurrentViewerPie.getSliceContaining(worldPos);
	      if(idx != null) 
		hist.toggleIncluded(idx);
	    }
	    
	    refresh();
	  }
	}
	break;

      case MouseEvent.BUTTON3:
	{
	  int on1  = (MouseEvent.BUTTON3_DOWN_MASK);
	  
	  int off1 = (MouseEvent.BUTTON1_DOWN_MASK | 
		      MouseEvent.BUTTON2_DOWN_MASK | 
		      MouseEvent.SHIFT_DOWN_MASK |
		      MouseEvent.ALT_DOWN_MASK |
		      MouseEvent.CTRL_DOWN_MASK);
	  
	  /* BUTTON3: chart popup menu */ 
	  if((mods & (on1 | off1)) == on1) {
 	    boolean enabled = pCurrentViewerPie.getName().equals("SystemLoad");
	    pEditChartItem.setEnabled(enabled); 

// 	    pChartPopup.show(e.getComponent(), e.getX(), e.getY());
	  }
	}
	break;
      }
    }
 
    /* mouse press is over an unused spot on the canvas */ 
    else {
      if(handleMousePressed(e)) 
	return;
      else {
	switch(e.getButton()) {
	case MouseEvent.BUTTON3:
	  {
	    int on1  = (MouseEvent.BUTTON3_DOWN_MASK);
	    
	    int off1 = (MouseEvent.BUTTON1_DOWN_MASK | 
			MouseEvent.BUTTON2_DOWN_MASK | 
			MouseEvent.SHIFT_DOWN_MASK |
			MouseEvent.ALT_DOWN_MASK |
			MouseEvent.CTRL_DOWN_MASK);
	    
	    /* BUTTON3: panel popup menu */ 
	    if((mods & (on1 | off1)) == on1) {
	      pPanelPopup.show(e.getComponent(), e.getX(), e.getY());
	    }
	  }
	}
      }
    }
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
    /* manager panel hotkeys */ 
    if(pManagerPanel.handleManagerKeyEvent(e)) 
      return;

    /* local hotkeys */ 
    Point2d worldPos = getWorldMousePos();
    pCurrentViewerPie = pieAtWorldPos(worldPos);
    UserPrefs prefs = UserPrefs.getInstance();
    boolean undefined = false;

    /* chart actions */
    if(pCurrentViewerPie != null) {
//       if((prefs.getJobServerStatsEditChart() != null) &&
//  	 prefs.getJobServerStatsEditChart().wasPressed(e)) 
//  	doEditChart();
//       else 
	undefined = true;
    }
    
    /* panel actions */
    else {
      if((prefs.getUpdate() != null) &&
	 prefs.getUpdate().wasPressed(e))
	updatePanels();
      else if((prefs.getFrameAll() != null) &&
	      prefs.getFrameAll().wasPressed(e))
	doFrameAll();
      else
	undefined = true;
    } 

    if(undefined) {
      switch(e.getKeyCode()) {
      case KeyEvent.VK_SHIFT:
      case KeyEvent.VK_ALT:
      case KeyEvent.VK_CONTROL:
	break;
      
      default:
	Toolkit.getDefaultToolkit().beep();
	refresh(); 
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


  /*-- POPUP MENU LISTNER METHODS ----------------------------------------------------------*/

  /**
   * This method is called when the popup menu is canceled. 
   */ 
  public void 
  popupMenuCanceled
  (
   PopupMenuEvent e
  )
  { 
    refresh();
  }
   
  /**
   * This method is called before the popup menu becomes invisible. 
   */ 
  public void
  popupMenuWillBecomeInvisible(PopupMenuEvent e) {} 
  
  /**
   * This method is called before the popup menu becomes visible. 
   */ 
  public void 	
  popupMenuWillBecomeVisible(PopupMenuEvent e) {} 



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
    /* panel menu events */ 
    String cmd = e.getActionCommand();
    if(cmd.equals("update"))
      updatePanels();
    else if(cmd.equals("frame-all"))
      doFrameAll();
    else if(cmd.equals("edit-chart"))
      doEditChart();
    else {
      refresh();
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Move the camera to frame all visible pie charts.
   */ 
  private synchronized void 
  doFrameAll() 
  {
    doFrameBounds(getChartBounds());
  }  

  /**
   * Edit the catagory ranges for the histogram under the mouse.
   */ 
  private synchronized void 
  doEditChart() 
  {
    if(pCurrentViewerPie == null) 
      return;

    Histogram hist = pCurrentViewerPie.getHistogram();
    String name = hist.getName();
    if(name.equals("SystemLoad")) {
//       JFloatHistogramSpecDialog diag = new JFloatHistogramSpecDialog(getTopFrame());
//       diag.updateHistogramSpec(pCurrentViewerPie.getHistogram()); 
      
//       diag.setVisible(true);
//       if(diag.wasConfirmed()) {
// 	HistogramSpec spec = diag.getHistogramSpec(); 
// 	if(spec != null) {
// 	  pNewHistogramSpecs = new QueueHostHistogramSpecs(pHistograms);
// 	  pNewHistogramSpecs.setLoadSpec(spec);

// 	  updatePanels();
// 	}
//       }
    }
    else if(name.equals("FreeMemory")) {

      // ...

    }  
    else if(name.equals("FreeMemory")) {

      // ...

    }  
  }



  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  public synchronized void 
  toGlue
  ( 
   GlueEncoder encoder   
  ) 
    throws GlueException
  {
    super.toGlue(encoder);

    if(pHistograms != null) 
      encoder.encode("HistogramSpecs", new QueueHostHistogramSpecs(pHistograms));
  }

  public synchronized void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    QueueHostHistogramSpecs spec = (QueueHostHistogramSpecs) decoder.decode("HistogramSpecs");
    if(spec != null) 
      pHistograms = new QueueHostHistograms(spec);

    super.fromGlue(decoder);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -8284602722640067098L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The frequency distribution data for significant catagories of information shared 
   * by all job server hosts.
   */ 
  private QueueHostHistograms  pHistograms; 

  /**
   * If not <CODE>null</CODE>, these are the modified histogram specifications to use
   * when updating.
   */ 
  private QueueHostHistogramSpecs  pNewHistogramSpecs; 

  /**
   * The names of the user work groups.
   */ 
  private TreeSet<String>  pWorkGroups; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The pie chart graphics which display the histogram data indexed by histogram name.
   */ 
  private TreeMap<String,ViewerPie>  pViewerPies;

  /**
   * The histogram associated with the chart under the mouse.
   */ 
  private ViewerPie  pCurrentViewerPie; 

  /**
   * The popup menus.
   */ 
  private JPopupMenu  pPanelPopup; 
  private JPopupMenu  pChartPopup; 

  /**
   * The panel popup menu items.
   */
  private JMenuItem  pUpdateItem;
  private JMenuItem  pFrameAllItem;
  private JMenuItem  pEditChartItem;
}
