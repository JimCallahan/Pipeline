// $Id: JQueueJobViewerPanel.java,v 1.13 2004/09/23 23:12:46 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.glue.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.vecmath.*;
import javax.media.j3d.*;
import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.geometry.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   J O B   V I E W E R   P A N E L                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The viewer of {@link NodeStatus NodeStatus} trees as graphs of state icons connected by
 * lines showing the upstream/downstream connectivity between nodes.
 */ 
public  
class JQueueJobViewerPanel
  extends JTopLevelPanel
  implements ComponentListener, MouseListener, MouseMotionListener, KeyListener, 
             PopupMenuListener, ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new panel with the default working area view.
   */
  public 
  JQueueJobViewerPanel()
  {
    super();
    initUI();
  }

  /**
   * Construct a new panel with a working area view identical to the given panel.
   */
  public 
  JQueueJobViewerPanel
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
    /* initialize fields */ 
    {
      pJobGroups = new TreeMap<Long,QueueJobGroup>(); 
      pJobStatus = new TreeMap<Long,JobStatus>();

      pLayoutPolicy = LayoutPolicy.AutomaticExpand;

      pSelected       = new HashMap<JobPath,ViewerJob>();
      pSelectedGroups = new TreeMap<Long,ViewerJobGroup>();
    }

    /* panel popup menu */ 
    {
      JMenuItem item;
      
      pPanelPopup = new JPopupMenu();  
      pPanelPopup.addPopupMenuListener(this);

      item = new JMenuItem("Update");
      item.setActionCommand("update");
      item.addActionListener(this);
      pPanelPopup.add(item);  
      
      pPanelPopup.addSeparator();

      item = new JMenuItem("Center");
      item.setActionCommand("center");
      item.addActionListener(this);
      pPanelPopup.add(item);  
      
      item = new JMenuItem("Frame Selection");
      item.setActionCommand("frame-selection");
      item.addActionListener(this);
      pPanelPopup.add(item);  
      
      item = new JMenuItem("Frame All");
      item.setActionCommand("frame-all");
      item.addActionListener(this);
      pPanelPopup.add(item);  
      
      pPanelPopup.addSeparator();
       
      item = new JMenuItem("Automatic Expand");
      item.setActionCommand("automatic-expand");
      item.addActionListener(this);
      pPanelPopup.add(item);  

      item = new JMenuItem("Expand All");
      item.setActionCommand("expand-all");
      item.addActionListener(this);
      pPanelPopup.add(item);  

      item = new JMenuItem("Collapse All");
      item.setActionCommand("collapse-all");
      item.addActionListener(this);
      pPanelPopup.add(item);  
    }
    
    /* job popup menu */ 
    {
      JMenuItem item;
      
      pJobPopup = new JPopupMenu();  
      pJobPopup.addPopupMenuListener(this);
      
      item = new JMenuItem("Update Details");
      item.setActionCommand("details");
      item.addActionListener(this);
      pJobPopup.add(item);

      pJobPopup.addSeparator();

      item = new JMenuItem("View");
      item.setActionCommand("view");
      item.addActionListener(this);
      pJobPopup.add(item);
      
      pViewWithMenu = new JMenu("View With");
      pJobPopup.add(pViewWithMenu);

      pJobPopup.addSeparator();

      item = new JMenuItem("Pause Jobs");
      item.setActionCommand("pause-jobs");
      item.addActionListener(this);
      pJobPopup.add(item);
      
      item = new JMenuItem("Resume Jobs");
      item.setActionCommand("resume-jobs");
      item.addActionListener(this);
      pJobPopup.add(item);
      
      item = new JMenuItem("Kill Jobs");
      item.setActionCommand("kill-jobs");
      item.addActionListener(this);
      pJobPopup.add(item);
    }
    
    /* job group popup menu */ 
    {
      JMenuItem item;
      
      pGroupPopup = new JPopupMenu();  
      pGroupPopup.addPopupMenuListener(this);
      
      item = new JMenuItem("Pause Jobs");
      item.setActionCommand("pause-jobs");
      item.addActionListener(this);
      pGroupPopup.add(item);
      
      item = new JMenuItem("Resume Jobs");
      item.setActionCommand("resume-jobs");
      item.addActionListener(this);
      pGroupPopup.add(item);
      
      item = new JMenuItem("Kill Jobs");
      item.setActionCommand("kill-jobs");
      item.addActionListener(this);
      pGroupPopup.add(item);
      
      pGroupPopup.addSeparator();
      
      item = new JMenuItem("Delete Groups");
      item.setActionCommand("delete-group");
      item.addActionListener(this);
      pGroupPopup.add(item);
    }

    /* initialize the panel components */ 
    {
      setLayout(new BorderLayout());
      setMinimumSize(new Dimension(50, 50));
      setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
      
      /* canvas */ 
      {
	pCanvas = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
	pCanvas.addComponentListener(this); 
	pCanvas.addMouseListener(this); 
	pCanvas.addMouseMotionListener(this); 
	pCanvas.setFocusable(true);
	pCanvas.addKeyListener(this);
	
	add(pCanvas);
      }

      /* grey backgound */ 
      BranchGroup background = new BranchGroup();
      {
	Point3d origin = new Point3d(0, 0, 0);
	BoundingSphere bounds = new BoundingSphere(origin, Double.POSITIVE_INFINITY);
	
 	Background bg = new Background(0.5f, 0.5f, 0.5f);
	bg.setApplicationBounds(bounds);
	
	background.addChild(bg);
	background.compile();
      }
      
      /* the universe */ 
      {
	pUniverse = new SimpleUniverse(pCanvas);
	pUniverse.addBranchGraph(background);
      }
      
      /* initialialize camera position */ 
      {
	Viewer viewer = pUniverse.getViewer();
	TransformGroup tg = viewer.getViewingPlatform().getViewPlatformTransform();
	
	Transform3D xform = new Transform3D();
	xform.setTranslation(new Vector3d(0.0, 0.0, 10.0));
	tg.setTransform(xform);
	
	View view = viewer.getView();
	view.setFrontClipDistance(0.5);
	view.setBackClipDistance(1000.0); 
      }
      
      /* zoomer-paner */ 
      {
	Point3d origin = new Point3d(0, 0, 0);
	BoundingSphere bounds = new BoundingSphere(origin, Double.POSITIVE_INFINITY);
	
	ZoomPanBehavior zp = new ZoomPanBehavior(pUniverse.getViewer(), 24.0);
	zp.setSchedulingBounds(bounds);
	
	BranchGroup branch = new BranchGroup();
	branch.addChild(zp);
	
	pUniverse.addBranchGraph(branch);
      }

      /* rubber band geometry */ 
      {
	pRubberBand = new RubberBand();
	pUniverse.addBranchGraph(pRubberBand.getBranchGroup());
      }

      /* the job geometry */ 
      {
	pGeomBranch = new BranchGroup();
	
	/* the job pool */ 
	{
	  pJobPool = new ViewerJobPool();
	  pGeomBranch.addChild(pJobPool.getBranchGroup());
	}

	/* the job group pool */ 
	{
	  pJobGroupPool = new ViewerJobGroupPool();
	  pGeomBranch.addChild(pJobGroupPool.getBranchGroup());
	}

	pUniverse.addBranchGraph(pGeomBranch);
      }				 
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
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

    PanelGroup<JQueueJobViewerPanel> panels = master.getQueueJobViewerPanels();

    if(pGroupID > 0)
      panels.releaseGroup(pGroupID);

    pGroupID = 0;
    if((groupID > 0) && panels.isGroupUnused(groupID)) {
      panels.assignGroup(this, groupID);
      pGroupID = groupID;
    }
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
    PanelGroup<JQueueJobViewerPanel> panels = 
      UIMaster.getInstance().getQueueJobViewerPanels();
    return panels.isGroupUnused(groupID);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Update the job groups and status.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param groups
   *   The queue job groups indexe by job group ID.
   * 
   * @param status
   *   The job status indexed by job ID.
   */ 
  public synchronized void
  updateQueueJobs
  (
   String author, 
   String view, 
   TreeMap<Long,QueueJobGroup> groups, 
   TreeMap<Long,JobStatus> status
  ) 
  {
    if(!pAuthor.equals(author) || !pView.equals(view)) 
      super.setAuthorView(author, view);

    updateQueueJobs(groups, status);
  }


  /**
   * Update the job groups and status.
   * 
   * @param groups
   *   The queue job groups indexed by job group ID.
   * 
   * @param status
   *   The job status indexed by job ID.
   */ 
  public synchronized void
  updateQueueJobs
  (
   TreeMap<Long,QueueJobGroup> groups, 
   TreeMap<Long,JobStatus>     status
  ) 
  {
    /* update the job groups and status tables */ 
    {
      pJobGroups.clear();
      if(groups != null) 
	pJobGroups.putAll(groups);
      
      pJobStatus.clear();
      if(status != null) 
	pJobStatus.putAll(status);
    }

    /* clear the job details panel if the job is no longer a member of a visible group */ 
    if(pLastJobID != null) {
      boolean found = false;
      for(QueueJobGroup group : pJobGroups.values()) {
	if(group.getAllJobIDs().contains(pLastJobID)) {
	  found = true;
	  break;
	}
      }

      if(!found) 
	pLastJobID = null;
    }

    /* update the visualization graphics */ 
    updateUniverse();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the connected job details panel with the given job.
   */ 
  private synchronized void 
  updateJobDetails
  (
   Long jobID
  ) 
  {
    if(pGroupID > 0) {
      if(jobID != null) {
	GetJobInfoTask task = new GetJobInfoTask(pGroupID, jobID);
	task.start();
      }
      else {
	UpdateDetailsPanelTask task = new UpdateDetailsPanelTask(pGroupID, null, null);
	SwingUtilities.invokeLater(task);
      }
    }

    pLastJobID = jobID;
  }

  /**
   * Update the connected job details panel.
   */ 
  private synchronized void 
  updateJobDetails() 
  {
    updateJobDetails(pLastJobID);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the jobs menu.
   */ 
  private void 
  updateJobsMenu() 
  {
    TreeMap<String,TreeSet<VersionID>> editors = PluginMgr.getInstance().getEditors();
    
    pViewWithMenu.removeAll();
    
    for(String editor : editors.keySet()) {
      JMenuItem item = new JMenuItem(editor);
      item.setActionCommand("view-with:" + editor);
      item.addActionListener(this);
      pViewWithMenu.add(item);
    }
    
    pViewWithMenu.addSeparator();
    
    JMenu sub = new JMenu("All Versions");
    pViewWithMenu.add(sub);

    for(String editor : editors.keySet()) {
      JMenu esub = new JMenu(editor);
      sub.add(esub);
      
      for(VersionID vid : editors.get(editor)) {
	JMenuItem item = new JMenuItem(editor + " (v" + vid + ")");
	item.setActionCommand("view-with:" + editor + ":" + vid);
	item.addActionListener(this);
	esub.add(item);
      }
    }
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
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the visualization graphics.
   */
  private synchronized void 
  updateUniverse()
  {  
    UserPrefs prefs = UserPrefs.getInstance();

    pJobPool.updatePrep();
    pJobGroupPool.updatePrep();

    boolean horzLayout = prefs.getJobViewerOrientation().equals("Horizontal");
    if(!pJobGroups.isEmpty()) {
      Point2d uanchor = new Point2d();
      for(QueueJobGroup group : pJobGroups.values()) {
	Point2d ganchor = uanchor;

	/* layout the jobs */ 
	Vector2d gspan = new Vector2d();
	ArrayList<ViewerJob> created = new ArrayList<ViewerJob>();
	for(Long jobID : group.getRootIDs()) {
	  JobStatus status = pJobStatus.get(jobID);
	  if(status != null) {
	    JobPath path = new JobPath(jobID);
	    Point2d anchor = 
	      new Point2d(ganchor.x + prefs.getJobSizeX() + prefs.getJobSpace(), 
			  ganchor.y + gspan.y);
	    
	    Vector2d span = null; 
	    {
	      TreeSet<Long> seen = new TreeSet<Long>();
	      span = layoutJobs(true, status, path, anchor, 
				group.getExternalIDs(), created, seen);
	    }
	    
	    gspan.x = Math.max(span.x, gspan.x);
	    gspan.y += span.y;
	  }
	}

	ViewerJobGroup vgroup = pJobGroupPool.lookupOrCreateViewerJobGroup(group, created);
	vgroup.setBounds(new Point2d(ganchor.x, ganchor.y + gspan.y), 
			 new Point2d(ganchor.x + prefs.getJobSizeX(), ganchor.y));

	if(horzLayout) {
	  uanchor.x = ganchor.x + prefs.getJobSizeX() + gspan.x + prefs.getJobGroupSpace();
	  uanchor.y = ganchor.y;
	}
	else {
	  uanchor.x = ganchor.x; 
	  uanchor.y = ganchor.y + gspan.y - prefs.getJobGroupSpace();
	}
      }
	
      /* preserve the current layout */ 
      pLayoutPolicy = LayoutPolicy.Preserve;
    }
      
    pJobGroupPool.update();
    pJobPool.update();

    /* update the connected job details panels */ 
    updateJobDetails();
  }
  
  /**
   * Recursively layout the jobs.
   * 
   * @param isRoot
   *   Is this the root job?
   * 
   * @param status
   *   The status of the current job. 
   * 
   * @param path
   *   The path from the root job to the current job.
   * 
   * @param anchor
   *   The upper-left corner of the layout area for the current job.
   * 
   * @param external
   *   The IDs of the jobs which are external to the group.
   * 
   * @param created
   *   The created viewer jobs. 
   * 
   * @param seen
   *   The IDs of the processed jobs.
   * 
   * @return 
   *   The size of the layout area of the viewer job including its children.
   */ 
  private Vector2d
  layoutJobs
  (
   boolean isRoot, 
   JobStatus status, 
   JobPath path, 
   Point2d anchor, 
   SortedSet<Long> external, 
   ArrayList<ViewerJob> created, 
   TreeSet<Long> seen
  ) 
  {
    UserPrefs prefs = UserPrefs.getInstance();

    ViewerJob vjob = pJobPool.lookupOrCreateViewerJob(status, path);
    vjob.setExternal(external.contains(status.getJobID()));    
    created.add(vjob);

    if(status.hasSources() && !vjob.isExternal()) {
      switch(pLayoutPolicy) {
      case Preserve:
	if(!vjob.isReset()) 
	  break;
	
      case AutomaticExpand:
	vjob.setCollapsed(seen.contains(status.getJobID()));
	break;
	
      case ExpandAll:
	vjob.setCollapsed(false);
	break;
	
      case CollapseAll:
	vjob.setCollapsed(true);
      }
    }
    else {
      vjob.setCollapsed(false);
    }

    seen.add(status.getJobID());

    Vector2d mspan = new Vector2d(); 
    if(status.hasSources() && !vjob.isExternal() && !vjob.isCollapsed()) {
      for(Long childID : status.getSourceJobIDs()) {
	JobStatus cstatus = pJobStatus.get(childID);
	if(cstatus != null) {
	  JobPath cpath = new JobPath(path, cstatus.getJobID());	  
	  Point2d canchor = new Point2d(anchor.x + prefs.getJobSizeX(), anchor.y + mspan.y);
	
	  Vector2d span = layoutJobs(false, cstatus, cpath, canchor, external, created, seen);
	  mspan.x = Math.max(mspan.x, span.x);
	  mspan.y += span.y;
	}
      }
    }
    else {
      mspan.y = -prefs.getJobSizeY();
    }

    vjob.setBounds(new Point2d(anchor.x + prefs.getJobSpace(), 
			       anchor.y + mspan.y + prefs.getJobSpace()), 
		   new Point2d(anchor.x + prefs.getJobSizeX() - prefs.getJobSpace(), 
			       anchor.y - prefs.getJobSpace()));
    
    mspan.x += prefs.getJobSizeX();

    return mspan;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S E L E C T I O N                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the ID of the primary job selection.
   * 
   * @return 
   *   The jobID or <CODE>null</CODE> if there is no primary selection.
   */ 
  public Long
  getPrimarySelectionJobID() 
  {
    if(pPrimary != null) 
      return pPrimary.getJobStatus().getJobID();
    return null;
  }

  /**
   * Get the ID of the primary job group selection.
   * 
   * @return 
   *   The groupID or <CODE>null</CODE> if there is no primary selection.
   */ 
  public Long
  getPrimarySelectionGroupID() 
  {
    if(pPrimaryGroup != null) 
      return pPrimaryGroup.getGroup().getGroupID();
    return null;
  }


  /**
   * Get the job IDs of all selected jobs.
   */ 
  public TreeSet<Long> 
  getSelectedJobIDs() 
  {
    TreeSet<Long> jobIDs = new TreeSet<Long>();
    for(ViewerJob vjob : pSelected.values()) 
      jobIDs.add(vjob.getJobStatus().getJobID());

    return jobIDs;
  }

  /**
   * Get the group IDs of all selected job groups.
   */ 
  public TreeSet<Long> 
  getSelectedGroupIDs() 
  {
    TreeSet<Long> groupIDs = new TreeSet<Long>();
    for(ViewerJobGroup vgroup : pSelectedGroups.values()) 
      groupIDs.add(vgroup.getGroup().getGroupID());

    return groupIDs;
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Clear the current selection.
   * 
   * @return 
   *   The previously selected jobs.
   */ 
  public ArrayList<ViewerJob>
  clearSelection()
  {
    ArrayList<ViewerJob> changed = new ArrayList<ViewerJob>(pSelected.values());

    {
      for(ViewerJob vjob : pSelected.values()) 
	vjob.setSelectionMode(SelectionMode.Normal);
      
      pSelected.clear();
      pPrimary = null;
    }

    {
      for(ViewerJobGroup vgroup : pSelectedGroups.values()) {
	vgroup.setSelectionMode(SelectionMode.Normal);
	vgroup.update();
      }
      
      pSelectedGroups.clear();
      pPrimaryGroup = null;
    }

    return changed;
  }
  

  /**
   * Make the given viewer job the primary selection.
   * 
   * @return 
   *   The viewer jobs who's selection state changed.
   */ 
  public ArrayList<ViewerJob>
  primarySelect
  (
   ViewerJob vjob
  ) 
  {
    ArrayList<ViewerJob> changed = new ArrayList<ViewerJob>();

    switch(vjob.getSelectionMode()) {
    case Normal:
      pSelected.put(vjob.getJobPath(), vjob);
      
    case Selected:
      if(pPrimary != null) {
	pPrimary.setSelectionMode(SelectionMode.Selected);
	changed.add(pPrimary);
      }
      pPrimary = vjob;
      vjob.setSelectionMode(SelectionMode.Primary);
      changed.add(vjob);
    }

    if(pPrimaryGroup != null) {
      pPrimaryGroup.setSelectionMode(SelectionMode.Selected);
      pPrimaryGroup.update();
    }

    return changed;
  }

  /**
   * Make the given viewer job group the primary selection.
   * 
   * @return 
   *   The viewer jobs who's selection state changed.
   */ 
  public ArrayList<ViewerJob>
  primarySelect
  (
   ViewerJobGroup vgroup
  ) 
  {
    ArrayList<ViewerJob> changed = new ArrayList<ViewerJob>();

    switch(vgroup.getSelectionMode()) {
    case Normal:
      pSelectedGroups.put(vgroup.getGroup().getGroupID(), vgroup);

      for(ViewerJob vjob : vgroup.getViewerJobs()) 
	changed.addAll(addSelect(vjob));
	
    case Selected:
      if(pPrimaryGroup != null) {
	pPrimaryGroup.setSelectionMode(SelectionMode.Selected);
	pPrimaryGroup.update();
      }

      pPrimaryGroup = vgroup;
      vgroup.setSelectionMode(SelectionMode.Primary);
      vgroup.update();
    }

    if(pPrimary != null) {
      pPrimary.setSelectionMode(SelectionMode.Selected);
      changed.add(pPrimary);
    }

    return changed;
  }


  /**
   * Add the given viewer job to the selection.
   * 
   * @return 
   *   The viewer jobs who's selection state changed.
   */ 
  public ArrayList<ViewerJob>
  addSelect
  (
   ViewerJob vjob
  ) 
  {
    ArrayList<ViewerJob> changed = new ArrayList<ViewerJob>();

    switch(vjob.getSelectionMode()) {
    case Primary:
      if(pPrimary != null) {
	pPrimary.setSelectionMode(SelectionMode.Selected);
	changed.add(pPrimary);
	pPrimary = null;
      }

    case Normal:
      vjob.setSelectionMode(SelectionMode.Selected);
      pSelected.put(vjob.getJobPath(), vjob);
      changed.add(vjob);
    }

    return changed;
  }

  /**
   * Add the given viewer job group to the selection.
   * 
   * @return 
   *   The viewer jobs who's selection state changed.
   */ 
  public ArrayList<ViewerJob>
  addSelect
  (
   ViewerJobGroup vgroup
  ) 
  {
    ArrayList<ViewerJob> changed = new ArrayList<ViewerJob>();

    switch(vgroup.getSelectionMode()) {
    case Primary:
      if(pPrimaryGroup != null) {
	pPrimaryGroup.setSelectionMode(SelectionMode.Selected);
	pPrimaryGroup.update();
	pPrimaryGroup = null;
      }

    case Normal:
      vgroup.setSelectionMode(SelectionMode.Selected);
      vgroup.update();

      pSelectedGroups.put(vgroup.getGroup().getGroupID(), vgroup);
      
      for(ViewerJob vjob : vgroup.getViewerJobs()) 
	changed.addAll(addSelect(vjob));
    }

    return changed;
  }


  /**
   * Toggle the selection of the given viewer job.
   * 
   * @return 
   *   The viewer jobs who's selection state changed.
   */ 
  public  ArrayList<ViewerJob> 
  toggleSelect
  (
   ViewerJob vjob
  ) 
  {
    ArrayList<ViewerJob> changed = new ArrayList<ViewerJob>();

    switch(vjob.getSelectionMode()) {
    case Primary:
      if(pPrimary != null) {
	pPrimary.setSelectionMode(SelectionMode.Selected);
	changed.add(pPrimary);
	pPrimary = null;
      }

    case Selected:
      vjob.setSelectionMode(SelectionMode.Normal);
      pSelected.remove(vjob.getJobPath());
      break;

    case Normal:
      vjob.setSelectionMode(SelectionMode.Selected);
      pSelected.put(vjob.getJobPath(), vjob);      
    }
    
    changed.add(vjob);
    return changed;
  }

  /**
   * Toggle the selection of the given viewer job group.
   * 
   * @return 
   *   The viewer jobs who's selection state changed.
   */ 
  public  ArrayList<ViewerJob> 
  toggleSelect
  (
   ViewerJobGroup vgroup
  ) 
  {
    ArrayList<ViewerJob> changed = new ArrayList<ViewerJob>();

    switch(vgroup.getSelectionMode()) {
    case Primary:
      if(pPrimaryGroup != null) {
	pPrimaryGroup.setSelectionMode(SelectionMode.Selected);
	pPrimaryGroup.update();
	pPrimaryGroup = null;
      }

    case Selected:
      vgroup.setSelectionMode(SelectionMode.Normal);
      vgroup.update();

      pSelectedGroups.remove(vgroup.getGroup().getGroupID());

      for(ViewerJob vjob : vgroup.getViewerJobs()) {
	vjob.setSelectionMode(SelectionMode.Normal);
	pSelected.remove(vjob.getJobPath());	
	changed.add(vjob);
      }
      break;

    case Normal:
      vgroup.setSelectionMode(SelectionMode.Selected);
      vgroup.update();

      pSelectedGroups.put(vgroup.getGroup().getGroupID(), vgroup);
      
      for(ViewerJob vjob : vgroup.getViewerJobs()) 
	changed.addAll(addSelect(vjob));
    }

    return changed;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the user-data of the Java3D object under given mouse position. <P> 
   * 
   * @return
   *   The picked object or <CODE>null</CODE> if no pickable object was under position.
   */ 
  private Object
  objectAtMousePos
  (
   int x, 
   int y
  ) 
  {
    PickRay ray = null;
    {
      Point3d eyePos = new Point3d();
      Point3d pos    = new Point3d();

      pCanvas.getCenterEyeInImagePlate(eyePos);
      pCanvas.getPixelLocationInImagePlate(x, y, pos);
      
      Transform3D motion = new Transform3D();
      pCanvas.getImagePlateToVworld(motion);
      motion.transform(eyePos);
      motion.transform(pos);
      
      Vector3d dir = new Vector3d(pos);
      dir.sub(eyePos);
      
      ray = new PickRay(eyePos, dir);
    }
    
    SceneGraphPath gpath = pGeomBranch.pickClosest(ray);
    if(gpath != null) 
      return gpath.getObject().getUserData();
    return null;
  }

  /**
   * Get the user-data of the Java3D object under given mouse position. <P> 
   * 
   * @return
   *   The picked object or <CODE>null</CODE> if no pickable object was under position.
   */ 
  private Object
  objectAtMousePos
  (
   Point pos
  ) 
  {
    if(pos == null) 
      return null;

    return objectAtMousePos(pos.x, pos.y);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- COMPONENT LISTENER METHODS ----------------------------------------------------------*/

  /**
   * Invoked when the component has been made invisible. 
   */ 
  public void 	
  componentHidden
  (
   ComponentEvent e
  )
  {}
  
  /**
   * Invoked when the component's position changes. 
   */ 
  public void 	
  componentMoved
  (
   ComponentEvent e
  )
  {}

  /**
   * Invoked when the component's size changes. 
   */ 
  public void 	
  componentResized
  (
   ComponentEvent e
  )
  {
    /* adjust the minimum zoom distance based on the canvas size */ 
    {
      Viewer viewer = pUniverse.getViewer();
      TransformGroup tg = viewer.getViewingPlatform().getViewPlatformTransform();
      
      Transform3D xform = new Transform3D();
      tg.getTransform(xform);
      
      Vector3d trans = new Vector3d();
      xform.get(trans);
      
      double minZ = ((double) pCanvas.getWidth()) / 64.0;
      if(minZ > trans.z) {
	trans.z = minZ;
	
	xform.setTranslation(trans);
	tg.setTransform(xform);
      }    
    }
  }

  /**
   * Invoked when the component has been made visible. 
   */ 
  public void 	
  componentShown
  (
   ComponentEvent e
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
    pMousePos = e.getPoint();
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

    pMousePos = e.getPoint();
    Object under = objectAtMousePos(pMousePos);

    /* mouse press is over a pickable object viewer job */ 
    if(under != null) {
      switch(e.getButton()) {
      case MouseEvent.BUTTON1:
	if((under instanceof ViewerJob) || (under instanceof ViewerJobGroup)) {

	  int on1  = (MouseEvent.BUTTON1_DOWN_MASK);
	  
	  int off1 = (MouseEvent.BUTTON2_DOWN_MASK | 
		      MouseEvent.BUTTON3_DOWN_MASK | 
		      MouseEvent.SHIFT_DOWN_MASK |
		      MouseEvent.ALT_DOWN_MASK |
		      MouseEvent.CTRL_DOWN_MASK);
	    
	  
	  int on2  = (MouseEvent.BUTTON1_DOWN_MASK |
		      MouseEvent.SHIFT_DOWN_MASK);
	  
	  int off2 = (MouseEvent.BUTTON2_DOWN_MASK | 
		      MouseEvent.BUTTON3_DOWN_MASK | 
		      MouseEvent.SHIFT_DOWN_MASK |
		      MouseEvent.CTRL_DOWN_MASK);
	  
	  
	  int on3  = (MouseEvent.BUTTON1_DOWN_MASK |
		      MouseEvent.SHIFT_DOWN_MASK |
		      MouseEvent.CTRL_DOWN_MASK);
	  
	  int off3 = (MouseEvent.BUTTON2_DOWN_MASK | 
		      MouseEvent.BUTTON3_DOWN_MASK | 
		      MouseEvent.ALT_DOWN_MASK);

	  HashMap<JobPath,ViewerJob> changed = new HashMap<JobPath,ViewerJob>();
	  
	  if(e.getClickCount() == 1) {
	    /* BUTTON1: replace selection */ 
	    if((mods & (on1 | off1)) == on1) {
	      if(under instanceof ViewerJob) {
		ViewerJob vunder = (ViewerJob) under;	

		for(ViewerJob vjob : clearSelection()) 
		  changed.put(vjob.getJobPath(), vjob);
		
		for(ViewerJob vjob : addSelect(vunder))
		  changed.put(vjob.getJobPath(), vjob);
	      }
	      else if(under instanceof ViewerJobGroup) {
		ViewerJobGroup vunder = (ViewerJobGroup) under;	
		
		for(ViewerJob vjob : clearSelection()) 
		  changed.put(vjob.getJobPath(), vjob);
		
		for(ViewerJob vjob : addSelect(vunder))
		  changed.put(vjob.getJobPath(), vjob);		
	      }
	    }
	    
	    /* BUTTON1+SHIFT: toggle selection */ 
	    else if((mods & (on2 | off2)) == on2) {
	      if(under instanceof ViewerJob) {
		ViewerJob vunder = (ViewerJob) under;
		for(ViewerJob vjob : toggleSelect(vunder)) 
		  changed.put(vjob.getJobPath(), vjob);
	      }
	      else if(under instanceof ViewerJobGroup) {
		ViewerJobGroup vunder = (ViewerJobGroup) under;	
		for(ViewerJob vjob : toggleSelect(vunder)) 
		  changed.put(vjob.getJobPath(), vjob);
	      }
	    }
	    
	    /* BUTTON1+SHIFT+CTRL: add to the selection */ 
	    else if((mods & (on3 | off3)) == on3) {
	      if(under instanceof ViewerJob) {
		ViewerJob vunder = (ViewerJob) under;
		for(ViewerJob vjob : addSelect(vunder))
		  changed.put(vjob.getJobPath(), vjob);
	      }
	      else if(under instanceof ViewerJobGroup) {
		ViewerJobGroup vunder = (ViewerJobGroup) under;	
		for(ViewerJob vjob : addSelect(vunder))
		  changed.put(vjob.getJobPath(), vjob);
	      }
	    }
	  }
	  else if(e.getClickCount() == 2) {
	    /* BUTTON1 (double click): send job status details panels */ 
	    if(under instanceof ViewerJob) {
	      ViewerJob vunder = (ViewerJob) under;
	      if((mods & (on1 | off1)) == on1) {
		for(ViewerJob vjob : primarySelect(vunder)) 
		  vjob.update();
		
		doDetails();
	      }
	    }
	  }
	    
	  /* update the appearance of all jobs who's selection state changed */ 
	  for(ViewerJob vjob : changed.values()) 
	    vjob.update();
	}
	break;

      case MouseEvent.BUTTON2:
	if(under instanceof ViewerJob) {
	  ViewerJob vunder = (ViewerJob) under;

	  int on1  = (MouseEvent.BUTTON2_DOWN_MASK);
	  
	  int off1 = (MouseEvent.BUTTON1_DOWN_MASK | 
		      MouseEvent.BUTTON3_DOWN_MASK | 
		      MouseEvent.SHIFT_DOWN_MASK |
		      MouseEvent.ALT_DOWN_MASK |
		      MouseEvent.CTRL_DOWN_MASK);
	  
	  /* BUTTON2: expand/collapse a job */ 
	  if((mods & (on1 | off1)) == on1) {
	    if(vunder.getJobStatus().hasSources()) {
	      vunder.setCollapsed(!vunder.isCollapsed());
	      updateUniverse();
	    }
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
	  
	  HashMap<JobPath,ViewerJob> changed = new HashMap<JobPath,ViewerJob>();

	  /* BUTTON3: job popup menu */ 
	  if((mods & (on1 | off1)) == on1) {
	    if(under instanceof ViewerJob) {
	      ViewerJob vunder = (ViewerJob) under;

	      for(ViewerJob vjob : addSelect(vunder))
		changed.put(vjob.getJobPath(), vjob);
	    
	      for(ViewerJob vjob : primarySelect(vunder)) 
		changed.put(vjob.getJobPath(), vjob);

	      updateJobsMenu();
	      pJobPopup.show(e.getComponent(), e.getX(), e.getY());
	    }
	    else if(under instanceof ViewerJobGroup) {
	      ViewerJobGroup vunder = (ViewerJobGroup) under;	

	      for(ViewerJob vjob : addSelect(vunder))
		changed.put(vjob.getJobPath(), vjob);
	    
	      for(ViewerJob vjob : primarySelect(vunder)) 
		changed.put(vjob.getJobPath(), vjob);
	      
	      pGroupPopup.show(e.getComponent(), e.getX(), e.getY());
	    }
	  }

	  /* update the appearance of all jobs who's selection state changed */ 
	  for(ViewerJob vjob : changed.values()) 
	    vjob.update();
	}
	break;
      }
    }
    
    /* mouse press is over an unused spot on the canvas */ 
    else {
      switch(e.getButton()) {
      case MouseEvent.BUTTON1:
	{
	  int on1  = (MouseEvent.BUTTON1_DOWN_MASK);

	  int off1 = (MouseEvent.BUTTON2_DOWN_MASK | 
		      MouseEvent.BUTTON3_DOWN_MASK | 
		      MouseEvent.SHIFT_DOWN_MASK |
		      MouseEvent.ALT_DOWN_MASK |
		      MouseEvent.CTRL_DOWN_MASK);


	  int on2  = (MouseEvent.BUTTON1_DOWN_MASK |
		      MouseEvent.SHIFT_DOWN_MASK);

	  int off2 = (MouseEvent.BUTTON2_DOWN_MASK | 
		      MouseEvent.BUTTON3_DOWN_MASK | 
		      MouseEvent.ALT_DOWN_MASK |
		      MouseEvent.CTRL_DOWN_MASK);


	  int on3  = (MouseEvent.BUTTON1_DOWN_MASK |
		      MouseEvent.SHIFT_DOWN_MASK |
		      MouseEvent.CTRL_DOWN_MASK);

	  int off3 = (MouseEvent.BUTTON2_DOWN_MASK | 
		      MouseEvent.BUTTON3_DOWN_MASK | 
		      MouseEvent.ALT_DOWN_MASK);

	  /* BUTTON1[+SHIFT[+CTRL]]: begin rubber band drag */ 
	  if(((mods & (on1 | off1)) == on1) || 
	     ((mods & (on2 | off2)) == on2) || 
	     ((mods & (on3 | off3)) == on3)) {
	    pRubberBand.beginDrag(new Point2d((double) e.getX(), (double) e.getY()));
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
		  
	  /* BUTTON3: panel popup menu */ 
	  if((mods & (on1 | off1)) == on1) 
	    pPanelPopup.show(e.getComponent(), e.getX(), e.getY());
	}
	break;
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
    int mods = e.getModifiersEx();

    switch(e.getButton()) {
    case MouseEvent.BUTTON1:
      if(pRubberBand.isDragging()) {
	BoundingBox bbox = pRubberBand.endDrag();
	if(bbox != null) {
	  SceneGraphPath gpaths[] = pGeomBranch.pickAll(new PickBounds(bbox));

	  int on1  = 0;
	  
	  int off1 = (MouseEvent.BUTTON1_DOWN_MASK | 
		      MouseEvent.BUTTON2_DOWN_MASK | 
		      MouseEvent.BUTTON3_DOWN_MASK | 
		      MouseEvent.SHIFT_DOWN_MASK |
		      MouseEvent.ALT_DOWN_MASK |
		      MouseEvent.CTRL_DOWN_MASK);
	  
	  
	  int on2  = (MouseEvent.SHIFT_DOWN_MASK);
	  
	  int off2 = (MouseEvent.BUTTON1_DOWN_MASK |
		      MouseEvent.BUTTON2_DOWN_MASK | 
		      MouseEvent.BUTTON3_DOWN_MASK | 
		      MouseEvent.SHIFT_DOWN_MASK |
		      MouseEvent.CTRL_DOWN_MASK);
	  
	  
	  int on3  = (MouseEvent.SHIFT_DOWN_MASK |
		      MouseEvent.CTRL_DOWN_MASK);
	  
	  int off3 = (MouseEvent.BUTTON1_DOWN_MASK |
		      MouseEvent.BUTTON2_DOWN_MASK | 
		      MouseEvent.BUTTON3_DOWN_MASK | 
		      MouseEvent.ALT_DOWN_MASK);
	  
	  HashMap<JobPath,ViewerJob> changed = new HashMap<JobPath,ViewerJob>();
	  
	  /* BUTTON1: replace selection */ 
	  if((mods & (on1 | off1)) == on1) {
	    for(ViewerJob vjob : clearSelection()) 
	      changed.put(vjob.getJobPath(), vjob);
	    
	    if(gpaths != null) {
	      int wk; 
	      for(wk=0; wk<gpaths.length; wk++) {
		Object picked = gpaths[wk].getObject().getUserData();
		if(picked instanceof ViewerJob) {
		  ViewerJob svjob = (ViewerJob) picked;
		  for(ViewerJob vjob : addSelect(svjob))
		    changed.put(vjob.getJobPath(), vjob);
		}
	      }

	      for(wk=0; wk<gpaths.length; wk++) {
		Object picked = gpaths[wk].getObject().getUserData();
		if(picked instanceof ViewerJobGroup) {
		  ViewerJobGroup vgroup = (ViewerJobGroup) picked;
		  for(ViewerJob vjob : addSelect(vgroup))
		    changed.put(vjob.getJobPath(), vjob);
		}
	      }
	    }
	  }
	  
	  /* BUTTON1+SHIFT: toggle selection */ 
	  else if((mods & (on2 | off2)) == on2) {
	    if(gpaths != null) {
	      int wk; 
	      for(wk=0; wk<gpaths.length; wk++) {
		Object picked = gpaths[wk].getObject().getUserData();
		if(picked instanceof ViewerJob) {
		  ViewerJob svjob = (ViewerJob) picked;		  
		  for(ViewerJob vjob : toggleSelect(svjob))
		    changed.put(vjob.getJobPath(), vjob);
		}
	      }

	      for(wk=0; wk<gpaths.length; wk++) {
		Object picked = gpaths[wk].getObject().getUserData();
		if(picked instanceof ViewerJobGroup) {
		  ViewerJobGroup vgroup = (ViewerJobGroup) picked;
		  for(ViewerJob vjob : toggleSelect(vgroup))
		    changed.put(vjob.getJobPath(), vjob);
		}
	      }
	    }
	  }
	  
	  /* BUTTON1+SHIFT+CTRL: add to selection */ 
	  else if((mods & (on3 | off3)) == on3) {
	    if(gpaths != null) {
	      int wk; 
	      for(wk=0; wk<gpaths.length; wk++) {
		Object picked = gpaths[wk].getObject().getUserData();
		if(picked instanceof ViewerJob) {
		  ViewerJob svjob = (ViewerJob) picked;
		  for(ViewerJob vjob : addSelect(svjob))
		    changed.put(vjob.getJobPath(), vjob);
		}
	      }

	      for(wk=0; wk<gpaths.length; wk++) {
		Object picked = gpaths[wk].getObject().getUserData();
		if(picked instanceof ViewerJobGroup) {
		 ViewerJobGroup vgroup = (ViewerJobGroup) picked;
		  for(ViewerJob vjob : addSelect(vgroup))
		    changed.put(vjob.getJobPath(), vjob);
		}
	      }
	    }
	  }
	  
	  /* update the appearance of all jobs who's selection state changed */ 
	  for(ViewerJob vjob : changed.values()) 
	    vjob.update();
	}

	/* drag started but never updated: clear the selection */ 
	else {
	  for(ViewerJob vjob : clearSelection()) 
	    vjob.update();
	}
      }
      break;
    }
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
    if(pRubberBand.isDragging()) {
      int on1  = (MouseEvent.BUTTON1_DOWN_MASK);
      
      int off1 = (MouseEvent.BUTTON2_DOWN_MASK | 
		  MouseEvent.BUTTON3_DOWN_MASK | 
		  MouseEvent.SHIFT_DOWN_MASK |
		  MouseEvent.ALT_DOWN_MASK |
		  MouseEvent.CTRL_DOWN_MASK);

      
      int on2  = (MouseEvent.BUTTON1_DOWN_MASK |
		  MouseEvent.SHIFT_DOWN_MASK);
      
      int off2 = (MouseEvent.BUTTON2_DOWN_MASK | 
		  MouseEvent.BUTTON3_DOWN_MASK | 
		  MouseEvent.ALT_DOWN_MASK |
		  MouseEvent.CTRL_DOWN_MASK);

      
      int on3  = (MouseEvent.BUTTON1_DOWN_MASK |
		  MouseEvent.SHIFT_DOWN_MASK | 
		  MouseEvent.CTRL_DOWN_MASK);
      
      int off3 = (MouseEvent.BUTTON2_DOWN_MASK | 
		  MouseEvent.BUTTON3_DOWN_MASK | 
		  MouseEvent.ALT_DOWN_MASK);

      /* BUTTON1[+SHIFT[+CTRL]]: update rubber band drag */ 
      if(((mods & (on1 | off1)) == on1) || 
	 ((mods & (on2 | off2)) == on2) || 
	 ((mods & (on3 | off3)) == on3)) {
	pRubberBand.updateDrag(pCanvas, new Point2d((double) e.getX(), (double) e.getY()));
      }
      
      /* end rubber band drag */ 
      else {
	pRubberBand.endDrag();
      }
    }
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
    pMousePos = e.getPoint();
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
    Object under = objectAtMousePos(pMousePos);

    boolean undefined = false;

    /* job actions */
    if(under instanceof ViewerJob) {
      ViewerJob vunder = (ViewerJob) under;
      
      switch(e.getKeyCode()) {
      case KeyEvent.VK_SHIFT:
      case KeyEvent.VK_ALT:
      case KeyEvent.VK_CONTROL:
	break;

      default:
	for(ViewerJob vjob : primarySelect(vunder)) 
	  vjob.update();
      }

      if((prefs.getJobDetails() != null) &&
	 prefs.getJobDetails().wasPressed(e)) 
	doDetails();

      else if((prefs.getJobView() != null) &&
	 prefs.getJobView().wasPressed(e)) 
	doView();

      else if((prefs.getJobPauseJobs() != null) &&
	      prefs.getJobPauseJobs().wasPressed(e))
	doPauseJobs();
      else if((prefs.getJobResumeJobs() != null) &&
	      prefs.getJobResumeJobs().wasPressed(e))
	doResumeJobs();
      else if((prefs.getJobKillJobs() != null) &&
	      prefs.getJobKillJobs().wasPressed(e))
	doKillJobs();

      else 
	undefined = true;
    }
    
    /* group actions */
    else if(under instanceof ViewerJobGroup) {
      ViewerJobGroup vunder = (ViewerJobGroup) under;
      
      switch(e.getKeyCode()) {
      case KeyEvent.VK_SHIFT:
      case KeyEvent.VK_ALT:
      case KeyEvent.VK_CONTROL:
	break;

      default:
	for(ViewerJob vjob : primarySelect(vunder)) 
	  vjob.update();
      }

      if((prefs.getJobPauseJobs() != null) &&
	 prefs.getJobPauseJobs().wasPressed(e))
	doPauseJobs();
      else if((prefs.getJobResumeJobs() != null) &&
	      prefs.getJobResumeJobs().wasPressed(e))
	doResumeJobs();
      else if((prefs.getJobKillJobs() != null) &&
	      prefs.getJobKillJobs().wasPressed(e))
	doKillJobs();

      else if((prefs.getDeleteJobGroups() != null) &&
	      prefs.getDeleteJobGroups().wasPressed(e))
	doDeleteJobGroups();

      else 
	undefined = true;
    }
    
    /* panel actions */
    else {
      if((prefs.getJobViewerUpdate() != null) &&
	 prefs.getJobViewerUpdate().wasPressed(e))
	doUpdate();

      else if((prefs.getJobViewerCameraCenter() != null) &&
	      prefs.getJobViewerCameraCenter().wasPressed(e))
	doCenter();
      else if((prefs.getJobViewerCameraFrameSelection() != null) &&
		prefs.getJobViewerCameraFrameSelection().wasPressed(e))
	doFrameSelection();
      else if((prefs.getJobViewerCameraFrameAll() != null) &&
	      prefs.getJobViewerCameraFrameAll().wasPressed(e))
	doFrameAll();
      
      else if((prefs.getJobViewerAutomaticExpandJobs() != null) &&
	      prefs.getJobViewerAutomaticExpandJobs().wasPressed(e))
	doAutomaticExpand();
      else if((prefs.getJobViewerCollapseAllJobs() != null) &&
	      prefs.getJobViewerCollapseAllJobs().wasPressed(e))
	doCollapseAll();
      else if((prefs.getJobViewerExpandAllJobs() != null) &&
	      prefs.getJobViewerExpandAllJobs().wasPressed(e))
	doExpandAll();

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
	for(ViewerJob vjob : clearSelection()) 
	  vjob.update();
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
    for(ViewerJob vjob : clearSelection()) 
      vjob.update();
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
    System.out.print("Action: " + e.getActionCommand() + "\n");

    /* panel menu events */ 
    String cmd = e.getActionCommand();
    if(cmd.equals("update"))
      doUpdate();
    else if(cmd.equals("center"))
      doCenter();
    else if(cmd.equals("frame-selection"))
      doFrameSelection();
    else if(cmd.equals("frame-all"))
      doFrameAll();
    else if(cmd.equals("automatic-expand"))
      doAutomaticExpand();
    else if(cmd.equals("expand-all"))
      doExpandAll();
    else if(cmd.equals("collapse-all"))
      doCollapseAll();

    /* job/group events */ 
    else if(cmd.equals("details"))
      doDetails();

    else if(cmd.equals("view"))
      doView();
    else if(cmd.startsWith("view-with:"))
      doViewWith(cmd.substring(10));    

    else if(cmd.equals("pause-jobs"))
      doPauseJobs();
    else if(cmd.equals("resume-jobs"))
      doResumeJobs();
    else if(cmd.equals("kill-jobs"))
      doKillJobs();
     else if(cmd.equals("delete-group"))
       doDeleteJobGroups();

    else {
      for(ViewerJob vjob : clearSelection()) 
	vjob.update();
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the status of all jobs.
   */ 
  private void
  doUpdate()
  { 
    if(pGroupID > 0) {
      UIMaster master = UIMaster.getInstance();    
      PanelGroup<JQueueJobBrowserPanel> panels = master.getQueueJobBrowserPanels();
      JQueueJobBrowserPanel panel = panels.getPanel(pGroupID);
      if(panel != null) {
	panel.doUpdate();
	return; 
      }
    }

    TreeSet<Long> groupIDs = new TreeSet<Long>(pJobGroups.keySet());
    if(groupIDs.isEmpty()) {
      updateQueueJobs(null, null);
    }
    else {
      GetJobsTask task = new GetJobsTask(groupIDs);
      task.start();
    }
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Move the camera so that it is centered on current mouse position.
   */ 
  private void 
  doCenter() 
  {
    if(pMousePos != null) {
      Point3d eyePos = new Point3d();
      Point3d pos    = new Point3d();
      
      pCanvas.getCenterEyeInImagePlate(eyePos);
      pCanvas.getPixelLocationInImagePlate(pMousePos.x, pMousePos.y, pos);
      
      Transform3D xform = new Transform3D();
      pCanvas.getImagePlateToVworld(xform);
      xform.transform(eyePos);
      xform.transform(pos);
      
      Vector3d dir = new Vector3d(pos);
      dir.sub(eyePos);
      dir.scale((eyePos.z-1.0) / dir.z);
    
      Point3d p = new Point3d(eyePos);
      p.sub(dir);
      
      centerOnPos(new Point2d(p.x, p.y));
    }
  }

  /**
   * Move the camera so that it is centered on the given world space position. 
   */ 
  private void 
  centerOnPos
  (
   Point2d pos
  ) 
  {
    Viewer viewer = pUniverse.getViewer();
    TransformGroup tg = viewer.getViewingPlatform().getViewPlatformTransform();
      
    Transform3D xform = new Transform3D();
    tg.getTransform(xform);
    
    Vector3d trans = new Vector3d();
    xform.get(trans);

    trans.x = pos.x;
    trans.y = pos.y;

    xform.setTranslation(trans);
    tg.setTransform(xform);
  }


  /**
   * Move the camera to frame the bounds of the currently selected jobs.
   */ 
  private void 
  doFrameSelection() 
  {
    frameJobs(pSelected.values(), !pSelectedGroups.isEmpty());
  }

  /**
   * Move the camera to frame all active jobs.
   */ 
  private void 
  doFrameAll() 
  {
    frameJobs(pJobPool.getActiveViewerJobs(), true);
  }

  /**
   * Move the camera to frame the given set of jobs.
   */ 
  private void 
  frameJobs
  (
   Collection<ViewerJob> vjobs, 
   boolean frameGroups
  ) 
  {
    if(vjobs.isEmpty()) 
      return;

    Point2d minPos = new Point2d(Integer.MAX_VALUE, Integer.MAX_VALUE);
    Point2d maxPos = new Point2d(Integer.MIN_VALUE, Integer.MIN_VALUE);
    for(ViewerJob vjob : vjobs) {
      Point2d minB = vjob.getMinBounds();
      Point2d maxB = vjob.getMaxCollapsedBounds();
      
      minPos.x = Math.min(minPos.x, minB.x);
      minPos.y = Math.min(minPos.y, minB.y);

      maxPos.x = Math.max(maxPos.x, maxB.x);
      maxPos.y = Math.max(maxPos.y, maxB.y);
    }

    if(frameGroups) {
      minPos.x = 0.0;
      maxPos.y += 1.25;
    }

    frameBounds(minPos, maxPos);    
  }  

  /**
   * Move the camera to frame the given bounds.
   */ 
  private void 
  frameBounds
  (
   Point2d minPos,  
   Point2d maxPos   
  ) 
  {
    Viewer viewer = pUniverse.getViewer();
    TransformGroup tg = viewer.getViewingPlatform().getViewPlatformTransform();
      
    Transform3D xform = new Transform3D();
    tg.getTransform(xform);
    
    Vector3d trans = new Vector3d();
    xform.get(trans);

    Vector2d extent = new Vector2d(maxPos);
    extent.sub(minPos);
    assert(extent.x >= 0.0);
    assert(extent.y > 0.0);

    Vector2d center = new Vector2d(minPos);
    center.add(maxPos);
    center.scale(0.5);

    trans.x = center.x;
    trans.y = center.y;

    Vector2d cExtent = new Vector2d((double) pCanvas.getWidth(), 
				    (double) pCanvas.getHeight());

    Vector2d nExtent = new Vector2d(extent);

    double nRatio = nExtent.x / nExtent.y;
    double cRatio = cExtent.x / cExtent.y;

    if(nRatio > cRatio) 
      trans.z = nExtent.x;
    else 
      trans.z = nExtent.y * (cExtent.x / cExtent.y);

    trans.z *= 1.25;
    trans.z = Math.max(((double) pCanvas.getWidth()) / 24.0, trans.z);

    xform.setTranslation(trans);
    tg.setTransform(xform);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Change to layout policy to <CODE>AutomaticExpand</CODE> and relayout the jobs.
   */ 
  private void
  doAutomaticExpand()
  {
    for(ViewerJob vjob : clearSelection()) 
      vjob.update();

    pLayoutPolicy = LayoutPolicy.AutomaticExpand;
    updateUniverse();
  }

  /**
   * Change to layout policy to <CODE>ExpandAll</CODE> and relayout the jobs.
   */ 
  private void
  doExpandAll()
  {
    for(ViewerJob vjob : clearSelection()) 
      vjob.update();

    pLayoutPolicy = LayoutPolicy.ExpandAll;
    updateUniverse();
  }

  /**
   * Change to layout policy to <CODE>CollapseAll</CODE> and relayout the jobs.
   */ 
  private void
  doCollapseAll()
  {
    for(ViewerJob vjob : clearSelection()) 
      vjob.update();

    pLayoutPolicy = LayoutPolicy.CollapseAll;
    updateUniverse();
  }

  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the job details panels with the current primary selected job status.
   */ 
  private void
  doDetails()
  {
    if((pGroupID > 0) && (pPrimary != null))
      updateJobDetails(pPrimary.getJobStatus().getJobID());

    for(ViewerJob vjob : clearSelection()) 
      vjob.update();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * View the target files of the primary selected job.
   */ 
  private void 
  doView() 
  {
    if(pPrimary != null) {
      ViewTask task = new ViewTask(pPrimary.getJobStatus());
      task.start();
    }

    for(ViewerJob vjob : clearSelection()) 
      vjob.update();
  }

  /**
   * View the target files of the primary selected job with the given editor.
   */ 
  private void 
  doViewWith
  (
   String editor
  ) 
  {
    String ename = null;
    VersionID evid = null;
    String parts[] = editor.split(":");
    switch(parts.length) {
    case 1:
      ename = editor;
      break;

    case 2:
      ename = parts[0];
      evid = new VersionID(parts[1]);
      break;

    default:
      assert(false);
    }

    if(pPrimary != null) {
      ViewTask task = new ViewTask(pPrimary.getJobStatus(), ename, evid);
      task.start();
    }

    for(ViewerJob vjob : clearSelection()) 
      vjob.update();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Pause all waiting selected jobs.
   */ 
  private void 
  doPauseJobs() 
  {
    TreeSet<Long> paused = new TreeSet<Long>();
    for(ViewerJob vjob : pSelected.values()) {
      JobStatus status = vjob.getJobStatus();
      paused.add(status.getJobID());
    }

    if(!paused.isEmpty()) {
      PauseJobsTask task = new PauseJobsTask(paused);
      task.start();
    }

    for(ViewerJob vjob : clearSelection()) 
      vjob.update();
  }

  /**
   * Resume execution of all paused jobs associated with the selected nodes.
   */ 
  private void 
  doResumeJobs() 
  {
    TreeSet<Long> resumed = new TreeSet<Long>();
    for(ViewerJob vjob : pSelected.values()) {
      JobStatus status = vjob.getJobStatus();
      switch(status.getState()) {
      case Paused:
	resumed.add(status.getJobID());
      }
    }

    if(!resumed.isEmpty()) {
      ResumeJobsTask task = new ResumeJobsTask(resumed);
      task.start();
    }

    for(ViewerJob vjob : clearSelection()) 
      vjob.update();
  }

  /**
   * Kill all jobs associated with the selected nodes.
   */ 
  private void 
  doKillJobs() 
  {
    TreeSet<Long> dead = new TreeSet<Long>();
    for(ViewerJob vjob : pSelected.values()) {
      JobStatus status = vjob.getJobStatus();
      switch(status.getState()) {
      case Queued:
      case Paused:
      case Running:
	dead.add(status.getJobID());
      }
    }

    if(!dead.isEmpty()) {
      KillJobsTask task = new KillJobsTask(dead);
      task.start();
    }

    for(ViewerJob vjob : clearSelection()) 
      vjob.update();
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Delete the primary selected job group.
   */ 
  private void 
  doDeleteJobGroups() 
  {
    if(pPrimaryGroup != null) {
      TreeMap<Long,String> groupAuthors = new TreeMap<Long,String>();
      for(Long groupID : pSelectedGroups.keySet()) {
	QueueJobGroup group = pSelectedGroups.get(groupID).getGroup();
	groupAuthors.put(groupID, group.getNodeID().getAuthor());
      }

      if(!groupAuthors.isEmpty()) {
	DeleteJobGroupsTask task = new DeleteJobGroupsTask(groupAuthors);
	task.start();
      }
    }

    for(ViewerJob vjob : clearSelection()) 
      vjob.update();
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

    /* root jobs */ 
    if(!pJobGroups.isEmpty()) 
      encoder.encode("JobGroupIDs", new TreeSet<Long>(pJobGroups.keySet()));
    
    /* camera position */ 
    {
      Viewer viewer = pUniverse.getViewer();
      TransformGroup tg = viewer.getViewingPlatform().getViewPlatformTransform();
      
      Transform3D xform = new Transform3D();
      tg.getTransform(xform);
      
      Vector3d trans = new Vector3d();
      xform.get(trans);
      
      encoder.encode("CameraX", trans.x);
      encoder.encode("CameraY", trans.y);
      encoder.encode("CameraZ", trans.z);
    }
  }

  public synchronized void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    /* job groups */     
    TreeSet<Long> groupIDs = (TreeSet<Long>) decoder.decode("JobGroupIDs");
    if(groupIDs != null) {
//       pJobGroups = new TreeMap<Long,JobGroup>();
//       for(Long groupID : groupIDs) 
// 	pJobGroups.put(groupID, null);
    }

    /* camera position */ 
    {
      Viewer viewer = pUniverse.getViewer();
      TransformGroup tg = viewer.getViewingPlatform().getViewPlatformTransform();
    
      Transform3D xform = new Transform3D();
      tg.getTransform(xform);
      
      Double cx = (Double) decoder.decode("CameraX");
      Double cy = (Double) decoder.decode("CameraY");
      Double cz = (Double) decoder.decode("CameraZ");

      if((cx == null) || (cy == null) || (cz == null)) 
	throw new GlueException("The camera position was incomplete!");
      Vector3d trans = new Vector3d(cx, cy, cz);

      xform.setTranslation(trans);	    
      tg.setTransform(xform);
    }

    super.fromGlue(decoder);
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The expand/collapse policy to use when laying out jobs.
   */
  private
  enum LayoutPolicy
  {  
    /**
     * Preserve the collapse mode of existing jobs and use an AutomaticExpand policy for 
     * any newly created jobs.
     */
    Preserve, 

    /**
     * Expand all jobs.
     */ 
    ExpandAll, 

    /**
     * Collapse all jobs.
     */ 
    CollapseAll, 
    
    /**
     * Expand the first occurance of a job and collapse all subsequence occurances.
     */
    AutomaticExpand;
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current job information. 
   */ 
  private 
  class GetJobInfoTask
    extends Thread
  {
    public 
    GetJobInfoTask
    (
     int groupID,
     long jobID
    ) 
    {
      pGroupID = groupID;
      pJobID   = jobID;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();      

      QueueJob     job  = null;
      QueueJobInfo info = null; 
      if(master.beginPanelOp("Updating Job Details...")) {
	try {
	  QueueMgrClient client = master.getQueueMgrClient();
	  job  = client.getJob(pJobID);
	  info = client.getJobInfo(pJobID);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	}
	finally {
	  master.endPanelOp("Done.");
	}
      }

      UpdateDetailsPanelTask task = new UpdateDetailsPanelTask(pGroupID, job, info);
      SwingUtilities.invokeLater(task);
    }

    private int   pGroupID;
    private long  pJobID; 
  }


  /**
   * Update the job details panel.
   */
  private 
  class UpdateDetailsPanelTask
    extends Thread
  {
    public 
    UpdateDetailsPanelTask
    (
     int groupID, 
     QueueJob job, 
     QueueJobInfo info
    ) 
    {
      pGroupID = groupID;
      pJob     = job; 
      pJobInfo = info; 
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();      

      PanelGroup<JQueueJobDetailsPanel> panels = master.getQueueJobDetailsPanels();
      JQueueJobDetailsPanel panel = panels.getPanel(pGroupID);
      if(panel != null) {
	panel.updateJob(pAuthor, pView, pJob, pJobInfo);
	panel.updateManagerTitlePanel();
      }
    }    

    private int           pGroupID;
    private QueueJob      pJob; 
    private QueueJobInfo  pJobInfo; 
  }

 
  /*----------------------------------------------------------------------------------------*/

  /** 
   * View the target files of the job with the given editor.
   */ 
  private
  class ViewTask
    extends Thread
  {
    public 
    ViewTask
    (
     JobStatus jstatus
    ) 
    {
      super("JQueueJobViewerPanel:ViewTask");

      pJobStatus  = jstatus;
    }

    public 
    ViewTask
    (
     JobStatus jstatus,
     String ename,
     VersionID evid
    ) 
    {
      super("JQueueJobViewerPanel:ViewTask");

      pJobStatus     = jstatus;
      pEditorName    = ename;
      pEditorVersion = evid; 
    }

    public void 
    run() 
    {
      SubProcess proc = null;
      {
	UIMaster master = UIMaster.getInstance();
	if(master.beginPanelOp("Launching Node Editor...")) {
	  try {
	    MasterMgrClient client = master.getMasterMgrClient();

	    NodeID nodeID = pJobStatus.getNodeID();
	    NodeMod mod = client.getWorkingVersion(nodeID);

	    /* create an editor plugin instance */ 
	    BaseEditor editor = null;
	    {
	      String ename = pEditorName;
	      if(ename == null) 
		ename = mod.getEditor();
	      if(ename == null) 
		throw new PipelineException
		  ("No editor was specified for node (" + mod.getName() + ")!");
	      
	      editor = PluginMgr.getInstance().newEditor(ename, pEditorVersion);
	    }

	    /* lookup the toolset environment */ 
	    TreeMap<String,String> env = null;
	    {
	      String tname = mod.getToolset();
	      if(tname == null) 
		throw new PipelineException
		  ("No toolset was specified for node (" + mod.getName() + ")!");

	      /* passes pAuthor so that WORKING will correspond to the current view */ 
	      env = client.getToolsetEnvironment(nodeID.getAuthor(), nodeID.getView(), tname);

	      /* override these since the editor will be run as the current user */ 
	      env.put("HOME", PackageInfo.sHomeDir + "/" + PackageInfo.sUser);
	      env.put("USER", PackageInfo.sUser);
	    }
	    
	    /* get the primary file sequence */ 
	    FileSeq fseq = null;
	    File dir = null; 
	    {
	      String path = null;
	      {
		File wpath = 
		  new File(PackageInfo.sWorkDir, 
			   nodeID.getAuthor() + "/" + nodeID.getView() + "/" + mod.getName());
		path = wpath.getParent();
	      }

	      fseq = new FileSeq(path, pJobStatus.getTargetSequence());
	      dir = new File(path);
	    }
	    
	    /* start the editor */ 
	    proc = editor.launch(fseq, env, dir);
	  }
	  catch(PipelineException ex) {
	    master.showErrorDialog(ex);
	    return;
	  }
	  finally {
	    master.endPanelOp("Done.");
	  }
	}

	/* wait for the editor to exit */ 
	try {
	  proc.join();
	  if(!proc.wasSuccessful()) 
	    master.showSubprocessFailureDialog("Editor Failure:", proc);
	}
	catch(InterruptedException ex) {
	  master.showErrorDialog(ex);
	}
      }
    }
 
    private JobStatus  pJobStatus; 
    private String     pEditorName;
    private VersionID  pEditorVersion; 
  }



  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the current job groups and job states.
   */ 
  private
  class GetJobsTask
    extends Thread
  {
    public 
    GetJobsTask
    (
     TreeSet<Long> groupIDs
    ) 
    {
      super("JQueueJobViewerPanel:GetJobsTask");
      pGroupIDs = groupIDs;
    }
 
    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();

      TreeMap<Long,QueueJobGroup> groups = new TreeMap<Long,QueueJobGroup>();
      TreeMap<Long,JobStatus> status = null;
      if(master.beginPanelOp("Updating Jobs...")) {
	try {
	  QueueMgrClient client = master.getQueueMgrClient();
	  for(Long groupID : pGroupIDs) 
	    groups.put(groupID, client.getJobGroup(groupID));
	  status = client.getJobStatus(pGroupIDs);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	}
	finally {
	  master.endPanelOp("Done.");
	}
      }

      updateQueueJobs(groups, status);
    }

    private TreeSet<Long>  pGroupIDs; 
  }


  /** 
   * Pause the given jobs.
   */ 
  private
  class PauseJobsTask
    extends Thread
  {
    public 
    PauseJobsTask
    (
     TreeSet<Long> jobIDs
    ) 
    {
      super("JQueueJobsViewerPanel:PauseJobsTask");

      pJobIDs = jobIDs; 
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp("Pausing Jobs...")) {
	try {
	  master.getQueueMgrClient().pauseJobs(pAuthor, pJobIDs);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp("Done.");
	}

	doUpdate();
      }
    }

    private TreeSet<Long>  pJobIDs; 
  }

  /** 
   * Resume execution of the the given paused jobs.
   */ 
  private
  class ResumeJobsTask
    extends Thread
  {
    public 
    ResumeJobsTask
    (
     TreeSet<Long> jobIDs
    ) 
    {
      super("JQueueJobsViewerPanel:ResumeJobsTask");

      pJobIDs = jobIDs; 
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp("Resuming Paused Jobs...")) {
	try {
	  master.getQueueMgrClient().resumeJobs(pAuthor, pJobIDs);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp("Done.");
	}

	doUpdate();
      }
    }

    private TreeSet<Long>  pJobIDs; 
  }

  /** 
   * Kill the given jobs.
   */ 
  private
  class KillJobsTask
    extends Thread
  {
    public 
    KillJobsTask
    (
     TreeSet<Long> jobIDs
    ) 
    {
      super("JQueueJobsViewerPanel:KillJobsTask");

      pJobIDs = jobIDs; 
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp("Killing Jobs...")) {
	try {
	  master.getQueueMgrClient().killJobs(pAuthor, pJobIDs);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp("Done.");
	}

	doUpdate();
      }
    }

    private TreeSet<Long>  pJobIDs; 
  }

  /** 
   * Delete the completed job group.
   */ 
  private
  class DeleteJobGroupsTask
    extends Thread
  {
    public 
    DeleteJobGroupsTask
    (
     TreeMap<Long,String> groupAuthors
    ) 
    {
      super("JQueueJobsViewerPanel:DeleteJobGroupsTask");

      pGroupAuthors = groupAuthors;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp("Deleting Job Groups...")) {
	try {
	  master.getQueueMgrClient().deleteJobGroups(pGroupAuthors);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp("Done.");
	}

	doUpdate();
      }
    }

    private TreeMap<Long,String>  pGroupAuthors; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 2163433216852708047L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The expand/collapse policy to use when laying out jobs.
   */ 
  private LayoutPolicy  pLayoutPolicy;


  /**
   * All displayed job groups indexed by group ID. 
   */ 
  private TreeMap<Long,QueueJobGroup>  pJobGroups;
  
  /**
   * The job status of the jobs which make up the displayed job groups indexed by job ID. 
   */ 
  private TreeMap<Long,JobStatus>  pJobStatus;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The panel popup menu.
   */ 
  private JPopupMenu  pPanelPopup; 

  /**
   * The job popup menu.
   */ 
  private JPopupMenu  pJobPopup; 

  /**
   * The view with submenu.
   */ 
  private JMenu  pViewWithMenu; 

  /**
   * The job group popup menu.
   */ 
  private JPopupMenu  pGroupPopup; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The Java3D scene graph.
   */ 
  private SimpleUniverse  pUniverse;      

  /**
   * The 3D canvas used to render the Java3D universe.
   */ 
  private Canvas3D  pCanvas;       

  /**
   * The reuseable collection of ViewerJobs.
   */ 
  private ViewerJobPool  pJobPool;

  /**
   * The reuseable collection of ViewerJobGroups.
   */ 
  private ViewerJobGroupPool  pJobGroupPool;

  /**
   * The selection rubber band geometry.
   */ 
  private RubberBand  pRubberBand;
  
  /**
   * The branch containing job geometry.
   */ 
  private BranchGroup  pGeomBranch;


  /**
   * The set of currently selected jobs indexed by <CODE>JobPath</CODE>.
   */ 
  private HashMap<JobPath,ViewerJob>  pSelected;

  /**
   * The primary job selection.
   */ 
  private ViewerJob  pPrimary;


  /**
   * The set of currently selected job groups indexed by group ID.
   */ 
  private TreeMap<Long,ViewerJobGroup>  pSelectedGroups;

  /**
   * The primary job group selection.
   */ 
  private ViewerJobGroup  pPrimaryGroup;

  

  /**
   * The last known mouse position.
   */ 
  private Point pMousePos;

  /**
   * The bounds of the currently visible jobs.
   */ 
  private Point2d  pMinJobBounds;
  private Point2d  pMaxJobBounds;


  /**
   * The ID of the last job sent to the job details panel.
   */ 
  private Long  pLastJobID; 

}
