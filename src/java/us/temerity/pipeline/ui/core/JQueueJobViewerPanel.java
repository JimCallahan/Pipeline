// $Id: JQueueJobViewerPanel.java,v 1.4 2005/01/09 23:23:09 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*; 
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
/*   Q U E U E   J O B   V I E W E R   P A N E L                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The viewer of {@link NodeStatus NodeStatus} trees as graphs of state icons connected by
 * lines showing the upstream/downstream connectivity between nodes.
 */ 
public  
class JQueueJobViewerPanel
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
    super.initUI(128.0);

    /* initialize fields */ 
    {
      pJobGroups = new TreeMap<Long,QueueJobGroup>(); 
      pJobStatus = new TreeMap<Long,JobStatus>();

      pViewerJobGroups = new TreeMap<Long,ViewerJobGroup>();
      pViewerJobs      = new HashMap<JobPath,ViewerJob>();

      pSelectedGroups = new TreeMap<Long,ViewerJobGroup>();
      pSelected       = new HashMap<JobPath,ViewerJob>();

      pEditorPlugins = PluginMgr.getInstance().getEditors();
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

      item = new JMenuItem("Queue Jobs");
      item.setActionCommand("queue-jobs");
      item.addActionListener(this);
      pJobPopup.add(item);
      
      item = new JMenuItem("Queue Jobs Special...");
      item.setActionCommand("queue-jobs-special");
      item.addActionListener(this);
      pJobPopup.add(item);

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
      
      item = new JMenuItem("View");
      item.setActionCommand("view");
      item.addActionListener(this);
      pGroupPopup.add(item);

      pGroupViewWithMenu = new JMenu("View With");
      pGroupPopup.add(pGroupViewWithMenu);

      pGroupPopup.addSeparator();

      item = new JMenuItem("Queue Jobs");
      item.setActionCommand("queue-jobs");
      item.addActionListener(this);
      pGroupPopup.add(item);
      
      item = new JMenuItem("Queue Jobs Special...");
      item.setActionCommand("queue-jobs-special");
      item.addActionListener(this);
      pGroupPopup.add(item);

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
    return "Job Viewer";
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
   * 
   * @param isPrivileged
   *   Does the current user have privileged status?
   */ 
  public synchronized void
  updateQueueJobs
  (
   String author, 
   String view, 
   TreeMap<Long,QueueJobGroup> groups, 
   TreeMap<Long,JobStatus> status, 
   boolean isPrivileged
  ) 
  {
    if(!pAuthor.equals(author) || !pView.equals(view)) 
      super.setAuthorView(author, view);

    updateQueueJobs(groups, status, isPrivileged);
  }


  /**
   * Update the job groups and status.
   * 
   * @param groups
   *   The queue job groups indexed by job group ID.
   * 
   * @param status
   *   The job status indexed by job ID.
   * 
   * @param isPrivileged
   *   Does the current user have privileged status?
   */ 
  public synchronized void
  updateQueueJobs
  (
   TreeMap<Long,QueueJobGroup> groups, 
   TreeMap<Long,JobStatus> status, 
   boolean isPrivileged
  ) 
  {
    pIsPrivileged = isPrivileged;
    if(UIMaster.getInstance().isRestoring())
      return;

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
   * Enable the update of the Job Details panel with the given job ID.
   */ 
  public synchronized void 
  enableDetailsUpdate
  (
   long jobID
  )
  {
    pLastJobID = jobID;
  }

  /**
   * Enable the update of the Job Details panel, but clear any last updated job ID.
   */ 
  public synchronized void 
  enableDetailsUpdate()
  {
    pLastJobID = null;
  }

  /**
   * Disable the update of the Job Details panel.
   */ 
  public synchronized void 
  disableDetailsUpdate()
  {
    pLastJobID = -1L;
  }
  
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
	if(jobID > 0) {
	  GetJobInfoTask task = new GetJobInfoTask(pGroupID, jobID);
	  task.start();
	}
	else {
	  return;
	}
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
   * Update the given view with menu. 
   */ 
  private void 
  updateViewMenu
  (
   JMenu menu
  ) 
  {
    menu.removeAll();
    
    for(String editor : pEditorPlugins.keySet()) {
      JMenuItem item = new JMenuItem(editor);
      item.setActionCommand("view-with:" + editor);
      item.addActionListener(this);
      menu.add(item);
    }
    
    menu.addSeparator();
    
    JMenu sub = new JMenu("All Versions");
    menu.add(sub);
    
    for(String editor : pEditorPlugins.keySet()) {
      JMenu esub = new JMenu(editor);
      sub.add(esub);
      
      for(VersionID vid : pEditorPlugins.get(editor)) {
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
   * Update the visualization graphics and any connected subpanels.
   */
  private synchronized void 
  updateUniverse()
  {
    updateUniverse(true);
  }

  /**
   * Update the visualization graphics.
   * 
   * @param updateSubPanels
   *   Whether to also update any connected subpanels.
   */
  private synchronized void 
  updateUniverse
  (
   boolean updateSubPanels
  )
  {  
    /* refresh the editor plugins */ 
    pEditorPlugins = PluginMgr.getInstance().getEditors();

    /* get the paths to the currently collapsed jobs */ 
    TreeSet<JobPath> wasCollapsed = new TreeSet<JobPath>();
    for(ViewerJob vjob : pViewerJobs.values()) {
      if(vjob.isCollapsed()) 
	wasCollapsed.add(vjob.getJobPath());
    }

    /* remove all previous jobs and job groups */ 
    pViewerJobGroups.clear();
    pViewerJobs.clear();

    UserPrefs prefs = UserPrefs.getInstance();
    boolean horzLayout = prefs.getJobViewerOrientation().equals("Horizontal");
    if(!pJobGroups.isEmpty()) {
      Point2d ganchor = new Point2d();
      for(QueueJobGroup group : pJobGroups.values()) {

	/* layout the jobs */ 
	int gheight = 0;
	ArrayList<ViewerJob> created = new ArrayList<ViewerJob>();
	{
	  Point2d anchor = Point2d.add(ganchor, new Vector2d(1.0, 0.0));
	  for(Long jobID : group.getRootIDs()) {
	    JobStatus status = pJobStatus.get(jobID);
	    if(status != null) {
	      JobPath path = new JobPath(jobID);
	      ViewerJob vjob = layoutJobs(true, status, path, anchor, 
					  group.getExternalIDs(), created, 
					  wasCollapsed, new TreeSet<Long>());
	      
	      anchor.y(anchor.y() - vjob.getBounds().getRange().y());
	      gheight += vjob.getHeight();
	    }
	  }
	}

	ViewerJobGroup vgroup = new ViewerJobGroup(group, created, gheight); 
	pViewerJobGroups.put(group.getGroupID(), vgroup);

	double vspan = 0.0;
	{
	  BBox2d bbox = vgroup.getBounds();
	  vspan = bbox.getRange().y(); 
	  Vector2d offset = Vector2d.mult(bbox.getRange(), new Vector2d(0.5, -0.5));
	  vgroup.setPosition(Point2d.add(ganchor, offset));
	}

	{
	  BBox2d bbox = vgroup.getFullBounds();
	  for(ViewerJob vjob : created) {
	    bbox.grow(vjob.getFullBounds()); 
	  }

	  if(horzLayout) 
	    ganchor.x(bbox.getMax().x() + prefs.getJobGroupSpace());
	  else 
	    ganchor.y(bbox.getMin().y() - 0.45 - prefs.getJobGroupSpace());
	}
      }
	
      /* preserve the current layout */ 
      pLayoutPolicy = LayoutPolicy.Preserve;
    }
   
    /* render the changes */ 
    refresh();

    /* update the connected job details panels */ 
    if(updateSubPanels) 
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
   * @param wasCollapsed
   *   The job paths of the previously collapsed jobs.
   * 
   * @param seen
   *   The IDs of the processed jobs.
   * 
   * @return 
   *   The root job. 
   */ 
  private ViewerJob
  layoutJobs
  (
   boolean isRoot, 
   JobStatus status, 
   JobPath path, 
   Point2d anchor, 
   SortedSet<Long> external, 
   ArrayList<ViewerJob> created, 
   TreeSet<JobPath> wasCollapsed, 
   TreeSet<Long> seen
  ) 
  {
    ViewerJob vjob = new ViewerJob(path, status, external.contains(status.getJobID()));
    pViewerJobs.put(path, vjob);
    created.add(vjob);

    if(status.hasSources() && !vjob.isExternal()) {
      switch(pLayoutPolicy) {
      case Preserve:
	vjob.setCollapsed(wasCollapsed.contains(path));
	break;
	
      case AutomaticExpand:
	vjob.setCollapsed(seen.contains(status.getJobID()));
	break;
	
      case CollapseAll:
	vjob.setCollapsed(true);
      }
    }

    seen.add(status.getJobID());

    /* layout the upstream jobs */ 
    if(status.hasSources() && !vjob.isExternal() && !vjob.isCollapsed()) { 
      Point2d canchor = Point2d.add(anchor, new Vector2d(1.0, 0.0));
      int cheight = 0;
      
      for(Long childID : status.getSourceJobIDs()) {
	JobStatus cstatus = pJobStatus.get(childID);
	if(cstatus != null) {
	  JobPath cpath = new JobPath(path, cstatus.getJobID());	 
	  
	  ViewerJob cvjob = layoutJobs(false, cstatus, cpath, canchor, 
				       external, created, wasCollapsed, seen);
	  
	  canchor.y(canchor.y() - cvjob.getBounds().getRange().y());
	  cheight += cvjob.getHeight();
	}
      }

      vjob.setHeight(cheight);
    }

    BBox2d bbox = vjob.getBounds();
    Vector2d offset = Vector2d.mult(bbox.getRange(), new Vector2d(0.5, -0.5));
    vjob.setPosition(Point2d.add(anchor, offset));

    return vjob;
  }


  /**
   * Get the bounding box which contains the given viewer jobs and job groups. <P> 
   * 
   * @return 
   *   The bounding box or <CODE>null</CODE> if no nodes are given.
   */ 
  private BBox2d
  getJobBounds
  (
   Collection<ViewerJob> jobs, 
   Collection<ViewerJobGroup> groups
  ) 
  {
    BBox2d bbox = null;
    for(ViewerJob vjob : jobs) {
      if(bbox == null) 
	bbox = vjob.getFullBounds();
      else 
	bbox.grow(vjob.getFullBounds());
    }
    
    for(ViewerJobGroup vgroup : groups) {
      if(bbox == null) 
	bbox = vgroup.getFullBounds();
      else 
	bbox.grow(vgroup.getFullBounds());
    }

    if(bbox != null) 
      bbox.bloat(UserPrefs.getInstance().getJobGroupSpace()); 

    return bbox;
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
   */ 
  public void 
  clearSelection()
  {
    for(ViewerJob vjob : pSelected.values()) 
      vjob.setSelectionMode(SelectionMode.Normal);
    
    pSelected.clear();
    pPrimary = null;

    for(ViewerJobGroup vgroup : pSelectedGroups.values()) 
      vgroup.setSelectionMode(SelectionMode.Normal);
      
    pSelectedGroups.clear();
    pPrimaryGroup = null;
  }
  

  /**
   * Make the given viewer job the primary selection.
   */ 
  public void
  primarySelect
  (
   ViewerJob vjob
  ) 
  {
    switch(vjob.getSelectionMode()) {
    case Normal:
      pSelected.put(vjob.getJobPath(), vjob);
      
    case Selected:
      if(pPrimary != null) 
	pPrimary.setSelectionMode(SelectionMode.Selected);
      pPrimary = vjob;
      vjob.setSelectionMode(SelectionMode.Primary);
    }

    if(pPrimaryGroup != null) 
      pPrimaryGroup.setSelectionMode(SelectionMode.Selected);
  }

  /**
   * Make the given viewer job group the primary selection.
   */ 
  public void 
  primarySelect
  (
   ViewerJobGroup vgroup
  ) 
  {
    switch(vgroup.getSelectionMode()) {
    case Normal:
      pSelectedGroups.put(vgroup.getGroup().getGroupID(), vgroup);
      for(ViewerJob vjob : vgroup.getViewerJobs()) 
	addSelect(vjob);
	
    case Selected:
      if(pPrimaryGroup != null) 
	pPrimaryGroup.setSelectionMode(SelectionMode.Selected);
      pPrimaryGroup = vgroup;
      vgroup.setSelectionMode(SelectionMode.Primary);
    }

    if(pPrimary != null) 
      pPrimary.setSelectionMode(SelectionMode.Selected);
  }


  /**
   * Add the given viewer job to the selection.
   */ 
  public void 
  addSelect
  (
   ViewerJob vjob
  ) 
  {
    switch(vjob.getSelectionMode()) {
    case Primary:
      if(pPrimary != null) {
	pPrimary.setSelectionMode(SelectionMode.Selected);
	pPrimary = null;
      }

    case Normal:
      vjob.setSelectionMode(SelectionMode.Selected);
      pSelected.put(vjob.getJobPath(), vjob);
    }
  }

  /**
   * Add the given viewer job group to the selection.
   */ 
  public void 
  addSelect
  (
   ViewerJobGroup vgroup
  ) 
  {
    switch(vgroup.getSelectionMode()) {
    case Primary:
      if(pPrimaryGroup != null) {
	pPrimaryGroup.setSelectionMode(SelectionMode.Selected);
	pPrimaryGroup = null;
      }

    case Normal:
      vgroup.setSelectionMode(SelectionMode.Selected);
      pSelectedGroups.put(vgroup.getGroup().getGroupID(), vgroup);
      for(ViewerJob vjob : vgroup.getViewerJobs()) 
	addSelect(vjob);
    }
  }

  /**
   * Toggle the selection of the given viewer job.
   */ 
  public void 
  toggleSelect
  (
   ViewerJob vjob
  ) 
  {
    switch(vjob.getSelectionMode()) {
    case Primary:
      if(pPrimary != null) {
	pPrimary.setSelectionMode(SelectionMode.Selected);
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
  }

  /**
   * Toggle the selection of the given viewer job group.
   */ 
  public void 
  toggleSelect
  (
   ViewerJobGroup vgroup
  ) 
  {
    switch(vgroup.getSelectionMode()) {
    case Primary:
      if(pPrimaryGroup != null) {
	pPrimaryGroup.setSelectionMode(SelectionMode.Selected);
	pPrimaryGroup = null;
      }

    case Selected:
      vgroup.setSelectionMode(SelectionMode.Normal);
      pSelectedGroups.remove(vgroup.getGroup().getGroupID());
      for(ViewerJob vjob : vgroup.getViewerJobs()) {
	vjob.setSelectionMode(SelectionMode.Normal);
	pSelected.remove(vjob.getJobPath());	
      }
      break;

    case Normal:
      vgroup.setSelectionMode(SelectionMode.Selected);
      pSelectedGroups.put(vgroup.getGroup().getGroupID(), vgroup);
      for(ViewerJob vjob : vgroup.getViewerJobs()) 
	addSelect(vjob);
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the ViewerJob or ViewerJobGroup under the current mouse position. <P> 
   */ 
  private Object
  objectAtMousePos() 
  {
    /* compute world coordinates */ 
    Point2d pos = new Point2d(pMousePos);
    {
      Dimension size = pCanvas.getSize();
      Vector2d half = new Vector2d(size.getWidth()*0.5, size.getHeight()*0.5);
      
      double f = -pCameraPos.z() * pPerspFactor;
      Vector2d persp = new Vector2d(f * pAspect, f);
      
      Vector2d camera = new Vector2d(pCameraPos.x(), pCameraPos.y());
      
      pos.sub(half).mult(pCanvasToScreen).mult(persp).sub(camera);
    }
    
    /* check job icons */ 
    for(ViewerJob vjob : pViewerJobs.values()) {
      if(vjob.isInside(pos)) 
	return vjob; 
    }

    /* check job group icons */ 
    for(ViewerJobGroup vgroup : pViewerJobGroups.values()) {
      if(vgroup.isInside(pos)) 
	return vgroup;
    }

    return null;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- GL EVENT LISTENER METHODS -----------------------------------------------------------*/

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
      if(pSceneDL.get() == 0) 
	pSceneDL.set(UIMaster.getInstance().getDisplayList(gl));
      
      if(pRefreshScene) {
	for(ViewerJob vjob : pViewerJobs.values()) 
	  vjob.rebuild(gl);

	for(ViewerJobGroup vgroup : pViewerJobGroups.values()) 
	  vgroup.rebuild(gl);

	gl.glNewList(pSceneDL.get(), GL.GL_COMPILE_AND_EXECUTE);
	{
	  for(ViewerJob vjob : pViewerJobs.values()) 
	    vjob.render(gl);

	  for(ViewerJobGroup vgroup : pViewerJobGroups.values()) 
	    vgroup.render(gl);
	}
	gl.glEndList();

	pRefreshScene = false;
      }
      else {
	gl.glCallList(pSceneDL.get());
      }
    }    
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
    Object under = objectAtMousePos();

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

	  if(e.getClickCount() == 1) {
	    /* BUTTON1: replace selection */ 
	    if((mods & (on1 | off1)) == on1) {
	      if(under instanceof ViewerJob) {
		ViewerJob vunder = (ViewerJob) under;	

		clearSelection();
		addSelect(vunder);
		refresh();
	      }
	      else if(under instanceof ViewerJobGroup) {
		ViewerJobGroup vunder = (ViewerJobGroup) under;	
	
		clearSelection();
		addSelect(vunder);
		refresh();
	      }
	    }
	    
	    /* BUTTON1+SHIFT: toggle selection */ 
	    else if((mods & (on2 | off2)) == on2) {
	      if(under instanceof ViewerJob) {
		ViewerJob vunder = (ViewerJob) under;

		toggleSelect(vunder); 
		refresh();
	      }
	      else if(under instanceof ViewerJobGroup) {
		ViewerJobGroup vunder = (ViewerJobGroup) under;	

		toggleSelect(vunder); 
		refresh();
	      }
	    }
	    
	    /* BUTTON1+SHIFT+CTRL: add to the selection */ 
	    else if((mods & (on3 | off3)) == on3) {
	      if(under instanceof ViewerJob) {
		ViewerJob vunder = (ViewerJob) under;

		addSelect(vunder);
		refresh();
	      }
	      else if(under instanceof ViewerJobGroup) {
		ViewerJobGroup vunder = (ViewerJobGroup) under;	

		addSelect(vunder);
		refresh();
	      }
	    }
	  }
	  else if(e.getClickCount() == 2) {
	    /* BUTTON1 (double click): send job status details panels */ 
	    if(under instanceof ViewerJob) {
	      ViewerJob vunder = (ViewerJob) under;
	      if((mods & (on1 | off1)) == on1) {
		primarySelect(vunder);
		refresh();
		
		doDetails();
	      }
	    }
	  }
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
	      vunder.toggleCollapsed();
	      updateUniverse(false);
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
	  
	  /* BUTTON3: job popup menu */ 
	  if((mods & (on1 | off1)) == on1) {
	    if(under instanceof ViewerJob) {
	      ViewerJob vunder = (ViewerJob) under;

	      addSelect(vunder);
	      primarySelect(vunder);

	      updateViewMenu(pViewWithMenu);
	      pJobPopup.show(e.getComponent(), e.getX(), e.getY());
	    }
	    else if(under instanceof ViewerJobGroup) {
	      ViewerJobGroup vunder = (ViewerJobGroup) under;	

	      addSelect(vunder);
	      primarySelect(vunder);
	      
	      updateViewMenu(pGroupViewWithMenu);
	      pGroupPopup.show(e.getComponent(), e.getX(), e.getY());
	    }
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
      if(pRbStart != null) {
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
	
	/* a rubberband drag has completed */ 
	if(pRbEnd != null) {
	  
	  /* compute world space bounding box of rubberband drag */ 
	  BBox2d bbox = null;
	  {
	    Point2d rs = new Point2d(pRbStart);
	    Point2d re = new Point2d(pRbEnd);
	    
	    Dimension size = pCanvas.getSize();
	    Vector2d half = new Vector2d(size.getWidth()*0.5, size.getHeight()*0.5);
	    
	    double f = -pCameraPos.z() * pPerspFactor;
	    Vector2d persp = new Vector2d(f * pAspect, f);
	    
	    Vector2d camera = new Vector2d(pCameraPos.x(), pCameraPos.y());
	    
	    rs.sub(half).mult(pCanvasToScreen).mult(persp).sub(camera);
	    re.sub(half).mult(pCanvasToScreen).mult(persp).sub(camera);
	    
	    bbox = new BBox2d(rs, re);
	  }
	  
	  /* BUTTON1: replace selection */ 
	  if((mods & (on1 | off1)) == on1) {
	    clearSelection();
	    
	    for(ViewerJob vjob : pViewerJobs.values()) {
	      if(vjob.isInsideOf(bbox)) 
		addSelect(vjob);
	    }
	    
	    for(ViewerJobGroup vgroup : pViewerJobGroups.values()) {
	      if(vgroup.isInsideOf(bbox)) 
		addSelect(vgroup);
	    }
	  }
	  
	  /* BUTTON1+SHIFT: toggle selection */ 
	  else if((mods & (on2 | off2)) == on2) {
	    for(ViewerJob vjob : pViewerJobs.values()) {
	      if(vjob.isInsideOf(bbox)) 
		toggleSelect(vjob);
	    }
	    
	    for(ViewerJobGroup vgroup : pViewerJobGroups.values()) {
	      if(vgroup.isInsideOf(bbox)) 
		toggleSelect(vgroup);
	    }
	  }
	  
	  /* BUTTON1+SHIFT+CTRL: add to selection */ 
	  else if((mods & (on3 | off3)) == on3) {
	    for(ViewerJob vjob : pViewerJobs.values()) {
	      if(vjob.isInsideOf(bbox)) 
		addSelect(vjob);
	    }
	    
	    for(ViewerJobGroup vgroup : pViewerJobGroups.values()) {
	      if(vgroup.isInsideOf(bbox)) 
		addSelect(vgroup);
	    }
	  }
	}
	
	/* rubberband drag started but never updated: 
	   clear the selection unless SHIFT or SHIFT+CTRL are down */ 
	else if(!(((mods & (on2 | off2)) == on2) || 
		  ((mods & (on3 | off3)) == on3))) {
	  clearSelection();
	}
      }
    }
   
    /* reinitialize for the next rubberband drag */ 
    pRbStart = null;
    pRbEnd   = null;
   
    /* refresh the view */ 
    pCanvas.setCursor(Cursor.getDefaultCursor());
    refresh();
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
    Object under = objectAtMousePos();
    UserPrefs prefs = UserPrefs.getInstance();
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
	primarySelect(vunder); 
	refresh();
      }

      if((prefs.getDetails() != null) &&
	 prefs.getDetails().wasPressed(e)) 
	doDetails();

      else if((prefs.getEdit() != null) &&
	 prefs.getEdit().wasPressed(e)) 
	doView();

      else if((prefs.getQueueJobs() != null) &&
	      prefs.getQueueJobs().wasPressed(e))
	doQueueJobs();
      else if((prefs.getQueueJobsSpecial() != null) &&
	      prefs.getQueueJobsSpecial().wasPressed(e))
	doQueueJobsSpecial();
      else if((prefs.getPauseJobs() != null) &&
	      prefs.getPauseJobs().wasPressed(e))
	doPauseJobs();
      else if((prefs.getResumeJobs() != null) &&
	      prefs.getResumeJobs().wasPressed(e))
	doResumeJobs();
      else if((prefs.getKillJobs() != null) &&
	      prefs.getKillJobs().wasPressed(e))
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
	primarySelect(vunder); 
	refresh();
      }

      if((prefs.getQueueJobs() != null) &&
	      prefs.getQueueJobs().wasPressed(e))
	doQueueJobs();
      else if((prefs.getQueueJobsSpecial() != null) &&
	      prefs.getQueueJobsSpecial().wasPressed(e))
	doQueueJobsSpecial();
      else if((prefs.getPauseJobs() != null) &&
	 prefs.getPauseJobs().wasPressed(e))
	doPauseJobs();
      else if((prefs.getResumeJobs() != null) &&
	      prefs.getResumeJobs().wasPressed(e))
	doResumeJobs();
      else if((prefs.getKillJobs() != null) &&
	      prefs.getKillJobs().wasPressed(e))
	doKillJobs();

      else if((prefs.getDeleteJobGroups() != null) &&
	      prefs.getDeleteJobGroups().wasPressed(e))
	doDeleteJobGroups();

      else 
	undefined = true;
    }
    
    /* panel actions */
    else {
      if((prefs.getUpdate() != null) &&
	 prefs.getUpdate().wasPressed(e))
	doUpdate();

      else if((prefs.getFrameSelection() != null) &&
	      prefs.getFrameSelection().wasPressed(e))
	doFrameSelection();
      else if((prefs.getFrameAll() != null) &&
	      prefs.getFrameAll().wasPressed(e))
	doFrameAll();
      
      else if((prefs.getAutomaticExpand() != null) &&
	      prefs.getAutomaticExpand().wasPressed(e))
	doAutomaticExpand();
      else if((prefs.getCollapseAll() != null) &&
	      prefs.getCollapseAll().wasPressed(e))
	doCollapseAll();
      else if((prefs.getExpandAll() != null) &&
	      prefs.getExpandAll().wasPressed(e))
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
	clearSelection();
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
    clearSelection();
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
      doUpdate();
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

    else if(cmd.equals("queue-jobs")) 
      doQueueJobs();
    else if(cmd.equals("queue-jobs-special")) 
      doQueueJobsSpecial();
    else if(cmd.equals("pause-jobs"))
      doPauseJobs();
    else if(cmd.equals("resume-jobs"))
      doResumeJobs();
    else if(cmd.equals("kill-jobs"))
      doKillJobs();
     else if(cmd.equals("delete-group"))
       doDeleteJobGroups();

    else {
      clearSelection();
      refresh();
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
    UIMaster master = UIMaster.getInstance();
    if((pGroupID > 0) && !master.isRestoring()) {
      PanelGroup<JQueueJobBrowserPanel> panels = master.getQueueJobBrowserPanels();
      JQueueJobBrowserPanel panel = panels.getPanel(pGroupID);
      if(panel != null) {
	panel.updateAll();
	return; 
      }
    }  
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Move the camera to frame the bounds of the currently selected jobs.
   */ 
  private void 
  doFrameSelection() 
  {
    doFrameJobs(pSelected.values(), pSelectedGroups.values());
  }

  /**
   * Move the camera to frame all active jobs.
   */ 
  private void 
  doFrameAll() 
  {
    doFrameJobs(pViewerJobs.values(), pViewerJobGroups.values());
  }

  /**
   * Move the camera to frame the given set of jobs.
   */ 
  private void 
  doFrameJobs
  (
   Collection<ViewerJob> vjobs, 
   Collection<ViewerJobGroup> vgroups
  ) 
  {
    doFrameBounds(getJobBounds(vjobs, vgroups));
  }  


  /*----------------------------------------------------------------------------------------*/

  /**
   * Change to layout policy to <CODE>AutomaticExpand</CODE> and relayout the jobs.
   */ 
  private void
  doAutomaticExpand()
  {
    clearSelection();
    pLayoutPolicy = LayoutPolicy.AutomaticExpand;
    updateUniverse();
  }

  /**
   * Change to layout policy to <CODE>ExpandAll</CODE> and relayout the jobs.
   */ 
  private void
  doExpandAll()
  {
    clearSelection();
    pLayoutPolicy = LayoutPolicy.ExpandAll;
    updateUniverse();
  }

  /**
   * Change to layout policy to <CODE>CollapseAll</CODE> and relayout the jobs.
   */ 
  private void
  doCollapseAll()
  {
    clearSelection();
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

    clearSelection();
    refresh(); 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * View the target files of the primary selected job.
   */ 
  private void 
  doView() 
  {
    if(pPrimary != null) {
      JobStatus status = pPrimary.getJobStatus();
      ViewTask task = new ViewTask(status.getNodeID(), status.getTargetSequence());
      task.start();
    }
    else if(pPrimaryGroup != null) {
      QueueJobGroup group = pPrimaryGroup.getGroup();
      ViewTask task = new ViewTask(group.getNodeID(), group.getRootSequence());
      task.start();
    }

    clearSelection();
    refresh(); 
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
      JobStatus status = pPrimary.getJobStatus();
      ViewTask task = new ViewTask(status.getNodeID(), status.getTargetSequence(), 
				   ename, evid);
      task.start();
    }
    else if(pPrimaryGroup != null) {
      QueueJobGroup group = pPrimaryGroup.getGroup();
      ViewTask task = new ViewTask(group.getNodeID(), group.getRootSequence(), ename, evid);
      task.start();
    }

    clearSelection();
    refresh(); 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Resubmit all aborted and failed selected jobs.
   */ 
  private void 
  doQueueJobs() 
  {
    TreeMap<NodeID,TreeSet<FileSeq>> targets = getQueuedFileSeqs();
    if(!targets.isEmpty()) {    
      QueueJobsTask task = new QueueJobsTask(targets);
      task.start();
    }

    clearSelection();
    refresh(); 
  }

  /**
   * Resubmit all aborted and failed selected jobs with special job requirements.
   */ 
  private void 
  doQueueJobsSpecial() 
  {
    TreeMap<NodeID,TreeSet<FileSeq>> targets = getQueuedFileSeqs();
    if(!targets.isEmpty()) {    
      JQueueJobsDialog diag = UIMaster.getInstance().showQueueJobsDialog();
      if(diag.wasConfirmed()) {
	Integer batchSize = null;
	if(diag.overrideBatchSize()) 
	  batchSize = diag.getBatchSize();
	
	Integer priority = null;
	if(diag.overridePriority()) 
	  priority = diag.getPriority();
	
	Integer interval = null;
	if(diag.overrideRampUp()) 
	  interval = diag.getRampUp();
	
	TreeSet<String> keys = null;
	if(diag.overrideSelectionKeys()) 
	  keys = diag.getSelectionKeys();

	QueueJobsTask task = new QueueJobsTask(targets, batchSize, priority, interval, keys);
	task.start();
      }
    }

    clearSelection();
    refresh(); 
  }

  /** 
   * Get the target file sequences of the aborted and failed selected root jobs.
   */ 
  private TreeMap<NodeID,TreeSet<FileSeq>> 
  getQueuedFileSeqs() 
  {
    /* get the aborted and failed selected jobs */ 
    TreeMap<Long,JobStatus> failed = new TreeMap<Long,JobStatus>();
    for(ViewerJob vjob : pSelected.values()) {
      JobStatus status = vjob.getJobStatus();
      if(status != null) {
	switch(status.getState()) {
	case Aborted:
	case Failed:
	  failed.put(status.getJobID(), status);
	}
      }
    }

    /* elimenate the non-root jobs */ 
    {
      TreeSet<Long> sourceIDs = new TreeSet<Long>();
      for(Long jobID : failed.keySet()) {
	boolean isSource = false;
	for(JobStatus status : failed.values()) {
	  if(status.getJobID() != jobID) {
	    if(isMemberJob(jobID, status)) {
	      isSource = true;
	      break;
	    }
	  }
	}
	
	if(isSource) 
	  sourceIDs.add(jobID);
      }
      
      for(Long jobID : sourceIDs) 
	failed.remove(jobID);
    }

    /* group the jobs by target node */ 
    TreeMap<NodeID,TreeSet<FileSeq>> targets = new TreeMap<NodeID,TreeSet<FileSeq>>();
    if(!failed.isEmpty()) {
      for(JobStatus status : failed.values()) {
	NodeID targetID = status.getNodeID();
	if(pIsPrivileged || targetID.getAuthor().equals(PackageInfo.sUser)) {
	  TreeSet<FileSeq> fseqs = targets.get(targetID);
	  if(fseqs == null) {
	    fseqs = new TreeSet<FileSeq>();
	    targets.put(targetID, fseqs);
	  }
	  
	  fseqs.add(status.getTargetSequence());
	}
      }
    }

    return targets; 
  }

  /**
   * Is the given jobID a member of the given tree of jobs.
   */ 
  private boolean
  isMemberJob
  (
   Long jobID, 
   JobStatus status
  ) 
  {
    if(jobID.equals(status.getJobID()))
      return true;

    for(Long sourceID : status.getSourceJobIDs()) {
      JobStatus sstatus = pJobStatus.get(sourceID);
      if(sstatus != null) {
	if(isMemberJob(jobID, sstatus)) 
	  return true;
      }
    }

    return false;
  }

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

    clearSelection();
    refresh(); 
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

    clearSelection();
    refresh(); 
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

    clearSelection();
    refresh(); 
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
    
    clearSelection();
    refresh(); 
  }


  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
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
      super("JQueueJobViewerPanel:GetJobInfoTask");

      pGroupID = groupID;
      pJobID   = jobID;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();      

      if(pGroupID > 0) {
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
      super("JQueueJobViewerPanel:UpdateDetailsPanelTask");

      pGroupID = groupID;
      pJob     = job; 
      pJobInfo = info; 
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();      

      if(pGroupID > 0) {
	PanelGroup<JQueueJobDetailsPanel> panels = master.getQueueJobDetailsPanels();
	JQueueJobDetailsPanel panel = panels.getPanel(pGroupID);
	if(panel != null) {
	  panel.updateJob(pAuthor, pView, pJob, pJobInfo);
	  panel.updateManagerTitlePanel();
	}
      }
    }    

    private int           pGroupID;
    private QueueJob      pJob; 
    private QueueJobInfo  pJobInfo; 
  }

 
  /*----------------------------------------------------------------------------------------*/

  /** 
   * View the given working file sequence with the given editor.
   */ 
  private
  class ViewTask
    extends Thread
  {
    public 
    ViewTask
    (
     NodeID nodeID, 
     FileSeq fseq
    ) 
    {
      this(nodeID, fseq, null, null);
    }

    public 
    ViewTask
    (
     NodeID nodeID, 
     FileSeq fseq,
     String ename,
     VersionID evid
    ) 
    {
      super("JQueueJobViewerPanel:ViewTask");

      pNodeID        = nodeID;
      pFileSeq       = fseq; 
      pEditorName    = ename;
      pEditorVersion = evid; 
    }

    public void 
    run() 
    {
      SubProcessLight proc = null;
      {
	UIMaster master = UIMaster.getInstance();
	if(master.beginPanelOp("Launching Node Editor...")) {
	  try {
	    MasterMgrClient client = master.getMasterMgrClient();

	    NodeMod mod = client.getWorkingVersion(pNodeID);
	    String author = pNodeID.getAuthor();
	    String view = pNodeID.getView();

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
	      env = client.getToolsetEnvironment(author, view, tname);

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
		File wpath = new File(PackageInfo.sWorkDir, 
				      author + "/" + view + "/" + mod.getName());
		path = wpath.getParent();
	      }

	      fseq = new FileSeq(path, pFileSeq);
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
	if(proc != null) {
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
    }
 
    private NodeID     pNodeID;
    private FileSeq    pFileSeq;
    private String     pEditorName;
    private VersionID  pEditorVersion; 
  }



  /*----------------------------------------------------------------------------------------*/

  /** 
   * Resubmit jobs to the queue for the given file sequences.
   */ 
  private
  class QueueJobsTask
    extends Thread
  {
    public 
    QueueJobsTask
    (
     TreeMap<NodeID,TreeSet<FileSeq>> targets
    ) 
    {
      this(targets, null, null, null, null);
    }
    
    public 
    QueueJobsTask
    (
     TreeMap<NodeID,TreeSet<FileSeq>> targets,
     Integer batchSize, 
     Integer priority, 
     Integer rampUp, 
     TreeSet<String> selectionKeys
    ) 
    {
      super("JQueueJobsViewerPanel:QueueJobsTask");

      pTargets       = targets;
      pBatchSize     = batchSize;
      pPriority      = priority; 
      pRampUp        = rampUp; 
      pSelectionKeys = selectionKeys;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp()) {
	try {
	  for(NodeID nodeID : pTargets.keySet()) {
	    master.updatePanelOp("Resubmitting Jobs to the Queue: " + nodeID.getName());
	    master.getMasterMgrClient().resubmitJobs(nodeID, pTargets.get(nodeID), 
						     pBatchSize, pPriority, pRampUp, 
						     pSelectionKeys);
	  }
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

    private TreeMap<NodeID,TreeSet<FileSeq>>  pTargets;
    private Integer                           pBatchSize;
    private Integer                           pPriority;
    private Integer                           pRampUp;
    private TreeSet<String>                   pSelectionKeys;
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
   * All displayed job groups indexed by group ID. 
   */ 
  private TreeMap<Long,QueueJobGroup>  pJobGroups;
  
  /**
   * The job status of the jobs which make up the displayed job groups indexed by job ID. 
   */ 
  private TreeMap<Long,JobStatus>  pJobStatus;

  /**
   * Does the current user have privileged status?
   */ 
  private boolean  pIsPrivileged;


  /*----------------------------------------------------------------------------------------*/

  /**
   * Cached names and version numbers of the loaded editor plugins. 
   */
  private TreeMap<String,TreeSet<VersionID>>  pEditorPlugins; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The currently displayed job groups indexed by unique job group ID.
   */ 
  private TreeMap<Long,ViewerJobGroup>  pViewerJobGroups;

  /**
   * The currently displayed jobs indexed by <CODE>JobPath</CODE>.
   */ 
  private HashMap<JobPath,ViewerJob>  pViewerJobs; 

  

  /**
   * The set of currently selected job groups indexed by group ID.
   */ 
  private TreeMap<Long,ViewerJobGroup>  pSelectedGroups;

  /**
   * The primary job group selection.
   */ 
  private ViewerJobGroup  pPrimaryGroup;

  
  /**
   * The set of currently selected jobs indexed by <CODE>JobPath</CODE>.
   */ 
  private HashMap<JobPath,ViewerJob>  pSelected;

  /**
   * The primary job selection.
   */ 
  private ViewerJob  pPrimary;


  /**
   * The ID of the last job sent to the job details panel.
   */ 
  private Long  pLastJobID; 


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

  /**
   * The view with group submenu.
   */ 
  private JMenu  pGroupViewWithMenu; 

}
