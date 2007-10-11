// $Id: JQueueJobViewerPanel.java,v 1.46 2007/10/11 18:52:07 jesse Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.jesse.ChangeJobReqs;
import us.temerity.pipeline.math.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.media.opengl.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   J O B   V I E W E R   P A N E L                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The viewer of {@link QueueJobInfo} and {@link QueueJobGroup} data.
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
    super.initUI(128.0, true);

    UserPrefs prefs = UserPrefs.getInstance();

    /* initialize fields */ 
    {
      pHorizontalOrientation = 
        ((prefs.getOrientation() != null) && prefs.getOrientation().equals("Horizontal"));

      pShowDetailHints = prefs.getShowJobDetailHints();

      pViewerJobHint = 
	new ViewerJobHint(this, 
                          prefs.getShowJobToolsetHints(), 
                          prefs.getShowJobActionHints(), 
                          prefs.getShowJobHostHints(), 
                          prefs.getShowJobTimingHints());

      pJobGroups = new TreeMap<Long,QueueJobGroup>(); 
      pJobStatus = new TreeMap<Long,JobStatus>();
      
      pViewerJobGroups = new TreeMap<Long,ViewerJobGroup>();
      pViewerJobs      = new HashMap<JobPath,ViewerJob>();

      pSelectedGroups = new TreeMap<Long,ViewerJobGroup>();
      pSelected       = new HashMap<JobPath,ViewerJob>();
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

      item = new JMenuItem("Frame Selection");
      pFrameSelectionItem = item;
      item.setActionCommand("frame-selection");
      item.addActionListener(this);
      pPanelPopup.add(item);  
      
      item = new JMenuItem("Frame All");
      pFrameAllItem = item;
      item.setActionCommand("frame-all");
      item.addActionListener(this);
      pPanelPopup.add(item);  
      
      pPanelPopup.addSeparator();
       
      item = new JMenuItem("Automatic Expand");
      pAutomaticExpandItem = item;
      item.setActionCommand("automatic-expand");
      item.addActionListener(this);
      pPanelPopup.add(item);  

      item = new JMenuItem("Expand All");
      pExpandAllItem = item;
      item.setActionCommand("expand-all");
      item.addActionListener(this);
      pPanelPopup.add(item);  

      item = new JMenuItem("Collapse All");
      pCollapseAllItem = item;
      item.setActionCommand("collapse-all");
      item.addActionListener(this);
      pPanelPopup.add(item);  

      pPanelPopup.addSeparator();
      
      item = new JMenuItem("Toggle Orientation");
      pToggleOrientationItem = item;
      item.setActionCommand("toggle-orientation");
      item.addActionListener(this);
      pPanelPopup.add(item);  

      item = new JMenuItem("Hide All Groups");
      pHideAllItem = item;
      item.setActionCommand("hide-all");
      item.addActionListener(this);
      pPanelPopup.add(item);  
      pPanelPopup.addSeparator();

      item = new JMenuItem();
      pShowHideDetailHintsItem = item;
      item.setActionCommand("show-hide-detail-hints");
      item.addActionListener(this);
      pPanelPopup.add(item);   

      item = new JMenuItem();
      pShowHideToolsetHintItem = item;
      item.setActionCommand("show-hide-toolset-hint");
      item.addActionListener(this);
      pPanelPopup.add(item);  

      item = new JMenuItem();
      pShowHideActionHintItem = item;
      item.setActionCommand("show-hide-action-hint");
      item.addActionListener(this);
      pPanelPopup.add(item);   

      item = new JMenuItem();
      pShowHideHostHintItem = item;
      item.setActionCommand("show-hide-host-hint");
      item.addActionListener(this);
      pPanelPopup.add(item);   

      item = new JMenuItem();
      pShowHideTimingHintItem = item;
      item.setActionCommand("show-hide-timing-hint");
      item.addActionListener(this);
      pPanelPopup.add(item);   
    }
    
    /* job popup menu */ 
    {
      JMenuItem item;
      
      pJobPopup = new JPopupMenu();  
      pJobPopup.addPopupMenuListener(this);
      
      item = new JMenuItem("Update Details");
      pUpdateDetailsItem = item;
      item.setActionCommand("details");
      item.addActionListener(this);
      pJobPopup.add(item);

      pJobPopup.addSeparator();

      item = new JMenuItem("View");
      pJobViewItem = item;
      item.setActionCommand("edit");
      item.addActionListener(this);
      pJobPopup.add(item);
      
      pViewWithMenu = new JMenu("View With");
      pJobPopup.add(pViewWithMenu);

      item = new JMenuItem("View With Default");
      pJobViewWithDefaultItem = item;
      item.setActionCommand("edit-with-default");
      item.addActionListener(this);
      pJobPopup.add(item);
      
      pJobPopup.addSeparator();

      item = new JMenuItem("Queue Jobs");
      pJobQueueJobsItem = item;
      item.setActionCommand("queue-jobs");
      item.addActionListener(this);
      pJobPopup.add(item);
      
      item = new JMenuItem("Queue Jobs Special...");
      pJobQueueJobsSpecialItem = item;
      item.setActionCommand("queue-jobs-special");
      item.addActionListener(this);
      pJobPopup.add(item);

      item = new JMenuItem("Pause Jobs");
      pJobPauseJobsItem = item;
      item.setActionCommand("pause-jobs");
      item.addActionListener(this);
      pJobPopup.add(item);
      
      item = new JMenuItem("Resume Jobs");
      pJobResumeJobsItem = item;
      item.setActionCommand("resume-jobs");
      item.addActionListener(this);
      pJobPopup.add(item);
      
      item = new JMenuItem("Preempt Jobs");
      pJobPreemptJobsItem = item;
      item.setActionCommand("preempt-jobs");
      item.addActionListener(this);
      pJobPopup.add(item);

      item = new JMenuItem("Kill Jobs");
      pJobKillJobsItem = item;
      item.setActionCommand("kill-jobs");
      item.addActionListener(this);
      pJobPopup.add(item);
      
      item = new JMenuItem("Change Job Reqs");
      pChangeJobReqsItem = item;
      item.setActionCommand("change-job-reqs");
      item.addActionListener(this);
      pJobPopup.add(item);      

      pJobPopup.addSeparator();

      item = new JMenuItem("Show Node");
      pJobShowNodeItem = item;
      item.setActionCommand("show-node");
      item.addActionListener(this);
      pJobPopup.add(item);
    }
    
    /* job group popup menu */ 
    {
      JMenuItem item;
      
      pGroupPopup = new JPopupMenu();  
      pGroupPopup.addPopupMenuListener(this);
      
      item = new JMenuItem("View");
      pGroupViewItem = item;
      item.setActionCommand("edit");
      item.addActionListener(this);
      pGroupPopup.add(item);

      pGroupViewWithMenu = new JMenu("View With");
      pGroupPopup.add(pGroupViewWithMenu);

      item = new JMenuItem("View With Default");
      pGroupViewWithDefaultItem = item;
      item.setActionCommand("edit-with-default");
      item.addActionListener(this);
      pGroupPopup.add(item);

      pGroupPopup.addSeparator();

      item = new JMenuItem("Queue Jobs");
      pGroupQueueJobsItem = item;
      item.setActionCommand("queue-jobs");
      item.addActionListener(this);
      pGroupPopup.add(item);
      
      item = new JMenuItem("Queue Jobs Special...");
      pGroupQueueJobsSpecialItem = item;
      item.setActionCommand("queue-jobs-special");
      item.addActionListener(this);
      pGroupPopup.add(item);

      item = new JMenuItem("Pause Jobs");
      pGroupPauseJobsItem = item;
      item.setActionCommand("pause-jobs");
      item.addActionListener(this);
      pGroupPopup.add(item);
      
      item = new JMenuItem("Resume Jobs");
      pGroupResumeJobsItem = item;
      item.setActionCommand("resume-jobs");
      item.addActionListener(this);
      pGroupPopup.add(item);
      
      item = new JMenuItem("Preempt Jobs");
      pGroupPreemptJobsItem = item;
      item.setActionCommand("preempt-jobs");
      item.addActionListener(this);
      pGroupPopup.add(item);
      
      item = new JMenuItem("Kill Jobs");
      pGroupKillJobsItem = item;
      item.setActionCommand("kill-jobs");
      item.addActionListener(this);
      pGroupPopup.add(item);
      
      item = new JMenuItem("Change Job Reqs");
      pGroupKillJobsItem = item;
      item.setActionCommand("change-job-reqs");
      item.addActionListener(this);
      pGroupPopup.add(item);
      
      pGroupPopup.addSeparator();
      
      item = new JMenuItem("Hide Groups");
      pGroupHideGroupsItem = item;
      item.setActionCommand("hide-groups");
      item.addActionListener(this);
      pGroupPopup.add(item);  

      item = new JMenuItem("Delete Groups");
      pGroupDeleteGroupsItem = item;
      item.setActionCommand("delete-group");
      item.addActionListener(this);
      pGroupPopup.add(item);

      pGroupPopup.addSeparator();

      item = new JMenuItem("Show Node");
      pGroupShowNodeItem = item;
      item.setActionCommand("show-node");
      item.addActionListener(this);
      pGroupPopup.add(item);
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
    PanelGroup<JQueueJobViewerPanel> panels = 
      UIMaster.getInstance().getQueueJobViewerPanels();
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

  /**
   * Get the ID of job displayed in the job details panel.
   */ 
  public Long
  getDetailedJobID()
  {
    return pDetailedJobID; 
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
    updatePanels(false);
  }

  /**
   * Update all panels which share the current update channel.
   */ 
  private void 
  updatePanels
  (
   boolean detailsOnly
  ) 
  {
    PanelUpdater pu = new PanelUpdater(this, detailsOnly);
    pu.execute();
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
   * @param detailedID
   *   The ID of the job displayed in the job details panel.
   * 
   * @param groups
   *   The selected queue job groups indexed by job group ID.
   * 
   * @param status
   *   The job status indexed by job ID.
   */
  public synchronized void 
  applyPanelUpdates
  (
   String author, 
   String view,
   Long detailedID, 
   TreeMap<Long,QueueJobGroup> groups, 
   TreeMap<Long,JobStatus> status
  )
  {
    if(!pAuthor.equals(author) || !pView.equals(view)) 
      super.setAuthorView(author, view);    

    updateQueueJobs(detailedID, groups, status);
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the job groups and status.
   * 
   * @param detailedID
   *   The ID of the job displayed in the job details panel.
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
   Long detailedID, 
   TreeMap<Long,QueueJobGroup> groups, 
   TreeMap<Long,JobStatus> status
  ) 
  {
    if(UIMaster.getInstance().isRestoring())
      return;

    updatePrivileges();
    
    /* update the job groups and status tables */ 
    {
      pDetailedJobID = detailedID; 

      pJobGroups.clear();
      if(groups != null) 
	pJobGroups.putAll(groups);
      
      pJobStatus.clear();
      if(status != null) 
	pJobStatus.putAll(status);
    }

    /* update the visualization graphics */ 
    updateUniverse();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the job menu.
   */ 
  public void 
  updateJobMenu() 
  {
    pJobQueueJobsItem.setEnabled(!isLocked());
    pJobQueueJobsSpecialItem.setEnabled(!isLocked());
    pJobPauseJobsItem.setEnabled(!isLocked());
    pJobResumeJobsItem.setEnabled(!isLocked());
    pJobPreemptJobsItem.setEnabled(!isLocked());
    pJobKillJobsItem.setEnabled(!isLocked());
    pChangeJobReqsItem.setEnabled(!isLocked());

    updateEditorMenus();
  }

  /**
   * Update the group menu.
   */ 
  public void 
  updateGroupMenu() 
  {
    pGroupQueueJobsItem.setEnabled(!isLocked());
    pGroupQueueJobsSpecialItem.setEnabled(!isLocked());
    pGroupPauseJobsItem.setEnabled(!isLocked());
    pGroupResumeJobsItem.setEnabled(!isLocked());
    pGroupPreemptJobsItem.setEnabled(!isLocked());
    pGroupKillJobsItem.setEnabled(!isLocked());

    updateEditorMenus();
  }

  /**
   * Reset the caches of toolset plugins and plugin menu layouts.
   */ 
  public void 
  clearPluginCache()
  {
    pEditorMenuToolset = null;
  }

  /**
   * Update the editor plugin menus.
   */ 
  private void 
  updateEditorMenus()
  {
    String toolset = null;
    if(pPrimary != null) 
      toolset = pPrimary.getJobStatus().getToolset();
    else if(pPrimaryGroup != null) 
      toolset = pPrimaryGroup.getGroup().getToolset();

    if((toolset != null) && !toolset.equals(pEditorMenuToolset)) {
      UIMaster master = UIMaster.getInstance();
      master.rebuildEditorMenu(pGroupID, toolset, pViewWithMenu, this);
      master.rebuildEditorMenu(pGroupID, toolset, pGroupViewWithMenu, this);
      
      pEditorMenuToolset = toolset;
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
    UserPrefs prefs = UserPrefs.getInstance();
    pShowDetailHints = prefs.getShowJobDetailHints();

    TextureMgr.getInstance().rebuildIcons();

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
       "Update the status of all jobs.");
    updateMenuToolTip
      (pFrameSelectionItem, prefs.getFrameSelection(), 
       "Move the camera to frame the bounds of the currently selected jobs.");
    updateMenuToolTip
      (pFrameAllItem, prefs.getFrameAll(), 
       "Move the camera to frame all active jobs.");
    updateMenuToolTip
      (pAutomaticExpandItem, prefs.getAutomaticExpand(), 
       "Automatically expand the first occurance of a job.");
    updateMenuToolTip
      (pExpandAllItem, prefs.getExpandAll(), 
       "Expand all jobs.");
    updateMenuToolTip
      (pCollapseAllItem, prefs.getCollapseAll(), 
       "Collapse all jobs.");
    updateMenuToolTip
      (pToggleOrientationItem, prefs.getToggleOrientation(), 
       "Toggle the job group orientation between Horizontal and Vertical.");
    updateMenuToolTip
      (pHideAllItem, prefs.getHideAll(), 
       "Hide all of the job groups.");

    updateMenuToolTip
      (pShowHideDetailHintsItem, prefs.getShowHideDetailHints(), 
       "Show/hide job detail hints.");
    updateMenuToolTip
      (pShowHideToolsetHintItem, prefs.getShowHideToolsetHint(), 
       "Show/hide the Toolset property as part of the job detail hints."); 
    updateMenuToolTip
      (pShowHideActionHintItem, prefs.getShowHideActionHint(), 
       "Show/hide the Action property as part of the job detail hints."); 
    updateMenuToolTip
      (pShowHideHostHintItem, prefs.getJobViewerShowHideHostHint(), 
       "Show/hide job server host information as part of the job detail hints."); 
    updateMenuToolTip
      (pShowHideTimingHintItem, prefs.getJobViewerShowHideTimingHint(), 
       "Show/hide job timing information as part of the job detail hints."); 

    /* job menu */ 
    updateMenuToolTip
      (pUpdateDetailsItem, prefs.getDetails(), 
       "Update connected job details panels.");
    updateMenuToolTip
      (pJobViewItem, prefs.getEdit(), 
       "View the target files of the primary selected job.");
    updateMenuToolTip
      (pJobViewWithDefaultItem, prefs.getEdit(), 
       "View the target files of the primary selected job using the default" + 
       "editor for the file type.");
    updateMenuToolTip
      (pJobQueueJobsItem, prefs.getQueueJobs(), 
       "Resubmit all aborted and failed selected jobs.");
    updateMenuToolTip
      (pJobQueueJobsSpecialItem, prefs.getQueueJobsSpecial(), 
       "Resubmit all aborted and failed selected with special job requirements.");
    updateMenuToolTip
      (pJobPauseJobsItem, prefs.getPauseJobs(), 
       "Pause all jobs associated with the selected jobs.");
    updateMenuToolTip
      (pJobResumeJobsItem, prefs.getResumeJobs(), 
       "Resume execution of all jobs associated with the selected jobs.");
    updateMenuToolTip
      (pJobPreemptJobsItem, prefs.getPreemptJobs(), 
       "Preempt all jobs associated with the selected jobs."); 
    updateMenuToolTip
      (pJobKillJobsItem, prefs.getKillJobs(), 
       "Kill all jobs associated with the selected jobs.");
    updateMenuToolTip
      (pChangeJobReqsItem, null, 
       "Changes the job requirements for the selected jobs.");
    updateMenuToolTip
      (pJobShowNodeItem, prefs.getShowNode(), 
       "Show the node which created the primary selected job in the Node Viewer.");
    
    /* job group menu */ 
    updateMenuToolTip
      (pGroupViewItem, prefs.getEdit(), 
       "View the target files of the primary selected job group.");
    updateMenuToolTip
      (pGroupQueueJobsItem, prefs.getQueueJobs(), 
       "Resubmit all aborted and failed selected jobs.");
    updateMenuToolTip
      (pGroupQueueJobsSpecialItem, prefs.getQueueJobsSpecial(), 
       "Resubmit all aborted and failed selected with special job requirements.");
    updateMenuToolTip
      (pGroupPauseJobsItem, prefs.getPauseJobs(), 
       "Pause all jobs associated with the selected jobs.");
    updateMenuToolTip
      (pGroupResumeJobsItem, prefs.getResumeJobs(), 
       "Resume execution of all jobs associated with the selected jobs.");
    updateMenuToolTip
      (pGroupPreemptJobsItem, prefs.getPreemptJobs(), 
       "Preempt all jobs associated with the selected jobs.");
    updateMenuToolTip
      (pGroupKillJobsItem, prefs.getKillJobs(), 
       "Kill all jobs associated with the selected jobs.");
    updateMenuToolTip
      (pGroupHideGroupsItem, prefs.getHideSelected(), 
       "Hide the selected job groups.");
    updateMenuToolTip
      (pGroupDeleteGroupsItem, prefs.getDeleteJobGroups(), 
       "Delete the selected completed job groups.");  
    updateMenuToolTip
      (pGroupShowNodeItem, prefs.getShowNode(), 
       "Show the node which created the primary selected job group in the Node Viewer.");
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the panel menu.
   */ 
  public void 
  updatePanelMenu() 
  {
    pShowHideDetailHintsItem.setText
      ((pShowDetailHints ? "Hide" : "Show") + " Detail Hints");
    pShowHideDetailHintsItem.setEnabled(true);

    if(pViewerJobHint != null) {
      pShowHideToolsetHintItem.setText
	((pViewerJobHint.showToolset() ? "Hide" : "Show") + " Toolset Hint");
      pShowHideToolsetHintItem.setEnabled(true);

      pShowHideActionHintItem.setText
	((pViewerJobHint.showAction() ? "Hide" : "Show") + " Action Hint");
      pShowHideActionHintItem.setEnabled(true);

      pShowHideHostHintItem.setText
	((pViewerJobHint.showHost() ? "Hide" : "Show") + " Host Hint");
      pShowHideHostHintItem.setEnabled(true);

      pShowHideTimingHintItem.setText
	((pViewerJobHint.showTiming() ? "Hide" : "Show") + " Timing Hint");
      pShowHideTimingHintItem.setEnabled(true);
    }
    else {
      pShowHideToolsetHintItem.setText("Show Toolset Hint");
      pShowHideToolsetHintItem.setEnabled(false);

      pShowHideActionHintItem.setText("Show Action Hint");
      pShowHideActionHintItem.setEnabled(false);

      pShowHideHostHintItem.setText("Show Host Hint");
      pShowHideHostHintItem.setEnabled(false);

      pShowHideTimingHintItem.setText("Show Timing Hint");
      pShowHideTimingHintItem.setEnabled(false);
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the visualization graphics.
   */
  private synchronized void 
  updateUniverse()
  {  
    /* get the paths to the currently collapsed jobs */ 
    TreeSet<JobPath> wasCollapsed = new TreeSet<JobPath>();
    for(ViewerJob vjob : pViewerJobs.values()) {
      if(vjob.isCollapsed()) 
	wasCollapsed.add(vjob.getJobPath());
    }

    /* remove all previous jobs and job groups */ 
    pViewerJobGroups.clear();
    pViewerJobs.clear();
    pLastJobHintID = null;

    UserPrefs prefs = UserPrefs.getInstance();
    if(!pJobGroups.isEmpty()) {
      Point2d ganchor = new Point2d();
      for(QueueJobGroup group : pJobGroups.values()) {

	/* layout the jobs */ 
	int gheight = 0;
	ArrayList<ViewerJob> created = new ArrayList<ViewerJob>();
	{
	  Point2d anchor = Point2d.add(ganchor, new Vector2d(1.0, 0.0));
          TreeSet<Long> seen = new TreeSet<Long>(); 
	  for(Long jobID : group.getRootIDs()) {
	    JobStatus status = pJobStatus.get(jobID);
	    if(status != null) {
	      JobPath path = new JobPath(jobID);
	      ViewerJob vjob = layoutJobs(true, status, path, anchor, 
					  group.getExternalIDs(), created, 
					  wasCollapsed, seen); 
	      
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

	  if(pHorizontalOrientation) 
	    ganchor.x(bbox.getMax().x() + prefs.getJobGroupSpace());
	  else 
	    ganchor.y(bbox.getMin().y() - 0.45 - prefs.getJobGroupSpace());
	}
      }
	
      /* preserve the current layout */ 
      pExpandDepth  = null; 
      pLayoutPolicy = LayoutPolicy.Preserve;
    }
   
    /* render the changes */ 
    refresh();
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
      if(pExpandDepth != null) {
	vjob.setCollapsed(path.getNumJobs() >= pExpandDepth);
      }
      else {
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
   * Add the viewer jobs and job groups inside the given bounding box to the selection.
   */ 
  public synchronized void 
  addSelect
  (
   BBox2d bbox
  ) 
  {
    for(ViewerJob vjob : pViewerJobs.values()) {
      if(vjob.isInsideOf(bbox)) 
	toggleSelect(vjob);
    }
    
    for(ViewerJobGroup vgroup : pViewerJobGroups.values()) {
      if(vgroup.isInsideOf(bbox)) 
	toggleSelect(vgroup);
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

  /**
   * Toggle the selection of the viewer jobs and job groups inside the given bounding box.
   */ 
  public synchronized void 
  toggleSelect
  (
   BBox2d bbox
  ) 
  {
    for(ViewerJob vjob : pViewerJobs.values()) {
      if(vjob.isInsideOf(bbox)) 
	toggleSelect(vjob);
    }
    
    for(ViewerJobGroup vgroup : pViewerJobGroups.values()) {
      if(vgroup.isInsideOf(bbox)) 
	toggleSelect(vgroup);
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the ViewerJob or ViewerJobGroup under the current mouse position. <P> 
   */ 
  private synchronized Object
  objectAtMousePos() 
  {
    if(pMousePos == null) 
      return null;

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
   GLAutoDrawable drawable
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

      if(pViewerJobHint.isVisible()) {
        pViewerJobHint.rebuild(gl);
        pViewerJobHint.render(gl);
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
    for(ViewerJob vjob : pViewerJobs.values()) 
      vjob.rebuild(gl);
    
    for(ViewerJobGroup vgroup : pViewerJobGroups.values()) 
      vgroup.rebuild(gl);
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
    for(ViewerJob vjob : pViewerJobs.values()) 
      vjob.render(gl);
    
    for(ViewerJobGroup vgroup : pViewerJobGroups.values()) 
      vgroup.render(gl);
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
	  
	  /* BUTTON3: job popup menu */ 
	  if((mods & (on1 | off1)) == on1) {
	    if(under instanceof ViewerJob) {
	      ViewerJob vunder = (ViewerJob) under;

	      addSelect(vunder);
	      primarySelect(vunder);

	      updateJobMenu();
	      pJobPopup.show(e.getComponent(), e.getX(), e.getY());
	    }
	    else if(under instanceof ViewerJobGroup) {
	      ViewerJobGroup vunder = (ViewerJobGroup) under;	

	      addSelect(vunder);
	      primarySelect(vunder);
	      
	      updateGroupMenu();
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
	      updatePanelMenu();
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
	    addSelect(bbox);
	  }
	  
	  /* BUTTON1+SHIFT: toggle selection */ 
	  else if((mods & (on2 | off2)) == on2) {
	    toggleSelect(bbox);
	  }
	  
	  /* BUTTON1+SHIFT+CTRL: add to selection */ 
	  else if((mods & (on3 | off3)) == on3) {
	    addSelect(bbox);
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
    if(!isPanelOpInProgress()) 
      pCanvas.setCursor(Cursor.getDefaultCursor());
    refresh();
  }


  /*-- MOUSE MOTION LISTNER METHODS --------------------------------------------------------*/
 
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
    super.mouseMoved(e);

    boolean hideHint = true;
    if(pShowDetailHints) {  
      Object under = objectAtMousePos();
      if((under != null) && (under instanceof ViewerJob)) {
	ViewerJob vunder = (ViewerJob) under;
        Long jobID = vunder.getJobPath().getCurrentJobID();
    
	if(pLastJobHintID != jobID) {
          UIMaster master = UIMaster.getInstance();
          if(master.beginSilentPanelOp(0)) {
            try {
              QueueMgrClient qclient = master.getQueueMgrClient(0);
              QueueJob job = qclient.getJob(jobID);
              QueueJobInfo info = qclient.getJobInfo(jobID);
              
              pViewerJobHint.updateHint(job, info); 
              pViewerJobHint.setPosition(vunder.getPosition());
              pViewerJobHint.setVisible(true);
              
              pLastJobHintID = jobID;
              hideHint = false;              
            }
            catch(PipelineException ex) {
            }
            finally {
              master.endSilentPanelOp(0);
            }
          }
	}
        else {
          hideHint = false;
        }
      }
    }

    if(hideHint) {
      pLastJobHintID = null;
      pViewerJobHint.setVisible(false);
    } 

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
      else if((prefs.getEditWithDefault() != null) &&
	      prefs.getEditWithDefault().wasPressed(e))
	doViewWithDefault();

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
      else if((prefs.getPreemptJobs() != null) &&
	      prefs.getPreemptJobs().wasPressed(e))
	doPreemptJobs();
      else if((prefs.getKillJobs() != null) &&
	      prefs.getKillJobs().wasPressed(e))
	doKillJobs();

      else if((prefs.getShowNode() != null) &&
	      prefs.getShowNode().wasPressed(e))
	doShowNode();

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

      if((prefs.getEdit() != null) &&
	 prefs.getEdit().wasPressed(e)) 
	doView();
      else if((prefs.getEditWithDefault() != null) &&
	      prefs.getEditWithDefault().wasPressed(e))
	doViewWithDefault();

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
      else if((prefs.getPreemptJobs() != null) &&
	      prefs.getPreemptJobs().wasPressed(e))
	doPreemptJobs();
      else if((prefs.getKillJobs() != null) &&
	      prefs.getKillJobs().wasPressed(e))
	doKillJobs();

      else if((prefs.getHideSelected() != null) &&
	      prefs.getHideSelected().wasPressed(e))
	doHideGroups();
      else if((prefs.getDeleteJobGroups() != null) &&
	      prefs.getDeleteJobGroups().wasPressed(e))
	doDeleteJobGroups();

      else if((prefs.getShowNode() != null) &&
	      prefs.getShowNode().wasPressed(e))
	doShowNode();

      else 
	undefined = true;
    }
    
    /* panel actions */
    else {
      if((prefs.getUpdate() != null) &&
	 prefs.getUpdate().wasPressed(e))
	updatePanels();

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

      else if((prefs.getExpand1Level() != null) &&
	      prefs.getExpand1Level().wasPressed(e))
	doExpandDepth(1);
      else if((prefs.getExpand2Levels() != null) &&
	      prefs.getExpand2Levels().wasPressed(e))
	doExpandDepth(2);
      else if((prefs.getExpand3Levels() != null) &&
	      prefs.getExpand3Levels().wasPressed(e))
	doExpandDepth(3);
      else if((prefs.getExpand4Levels() != null) &&
	      prefs.getExpand4Levels().wasPressed(e))
	doExpandDepth(4);
      else if((prefs.getExpand5Levels() != null) &&
	      prefs.getExpand5Levels().wasPressed(e))
	doExpandDepth(5);
      else if((prefs.getExpand6Levels() != null) &&
	      prefs.getExpand6Levels().wasPressed(e))
	doExpandDepth(6);
      else if((prefs.getExpand7Levels() != null) &&
	      prefs.getExpand7Levels().wasPressed(e))
	doExpandDepth(7);
      else if((prefs.getExpand8Levels() != null) &&
	      prefs.getExpand8Levels().wasPressed(e))
	doExpandDepth(8);
      else if((prefs.getExpand9Levels() != null) &&
	      prefs.getExpand9Levels().wasPressed(e))
	doExpandDepth(9);      

      else if((prefs.getToggleOrientation() != null) &&
              prefs.getToggleOrientation().wasPressed(e))
	doToggleOrientation();     
      else if((prefs.getHideAll() != null) &&
	      prefs.getHideAll().wasPressed(e))
	doHideAll();

      else if((prefs.getShowHideDetailHints() != null) &&
	      prefs.getShowHideDetailHints().wasPressed(e))
	doShowHideDetailHints();
      else if((prefs.getShowHideToolsetHint() != null) &&
	      prefs.getShowHideToolsetHint().wasPressed(e))
	doShowHideToolsetHint();
      else if((prefs.getShowHideActionHint() != null) &&
	      prefs.getShowHideActionHint().wasPressed(e))
	doShowHideActionHint();
      else if((prefs.getJobViewerShowHideHostHint() != null) &&
	      prefs.getJobViewerShowHideHostHint().wasPressed(e))
	doShowHideHostHint();
      else if((prefs.getJobViewerShowHideTimingHint() != null) &&
	      prefs.getJobViewerShowHideTimingHint().wasPressed(e))
	doShowHideTimingHint();

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
      updatePanels();
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
    else if(cmd.equals("toggle-orientation"))
      doToggleOrientation();
    else if(cmd.equals("hide-all"))
      doHideAll();
    else if(cmd.equals("show-hide-detail-hints"))
      doShowHideDetailHints();
    else if(cmd.equals("show-hide-toolset-hint"))
      doShowHideToolsetHint();
    else if(cmd.equals("show-hide-action-hint"))
      doShowHideActionHint();
    else if(cmd.equals("show-hide-host-hint"))
      doShowHideHostHint();
    else if(cmd.equals("show-hide-timing-hint"))
      doShowHideTimingHint();

    /* job/group events */ 
    else if(cmd.equals("details"))
      doDetails();

    else if(cmd.equals("edit"))
      doView();
    else if(cmd.equals("edit-with-default"))
      doViewWithDefault();
    else if(cmd.startsWith("edit-with:"))
      doViewWith(cmd.substring(10));    

    else if(cmd.equals("queue-jobs")) 
      doQueueJobs();
    else if(cmd.equals("queue-jobs-special")) 
      doQueueJobsSpecial();
    else if(cmd.equals("pause-jobs"))
      doPauseJobs();
    else if(cmd.equals("resume-jobs"))
      doResumeJobs();
    else if(cmd.equals("preempt-jobs"))
      doPreemptJobs();
    else if(cmd.equals("kill-jobs"))
      doKillJobs();
    else if(cmd.equals("hide-groups"))
      doHideGroups();
    else if(cmd.equals("delete-group"))
      doDeleteJobGroups();
    else if(cmd.equals("show-node"))
      doShowNode();
    else if (cmd.equals("change-job-reqs"))
      doChangeJobReqs();

    else {
      clearSelection();
      refresh();
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Move the camera to frame the bounds of the currently selected jobs.
   */ 
  private synchronized void 
  doFrameSelection() 
  {
    doFrameJobs(pSelected.values(), pSelectedGroups.values());
  }

  /**
   * Move the camera to frame all active jobs.
   */ 
  private synchronized void 
  doFrameAll() 
  {
    doFrameJobs(pViewerJobs.values(), pViewerJobGroups.values());
  }

  /**
   * Move the camera to frame the given set of jobs.
   */ 
  private synchronized void 
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
   * Set a fixed job expansion depth.
   */
  private synchronized void 
  doExpandDepth
  (
   int depth
  ) 
  {
    pExpandDepth = depth;
    updateUniverse();
  }

  /**
   * Change to layout policy to <CODE>AutomaticExpand</CODE> and relayout the jobs.
   */ 
  private synchronized void
  doAutomaticExpand()
  {
    clearSelection();
    pExpandDepth  = null;
    pLayoutPolicy = LayoutPolicy.AutomaticExpand;
    updateUniverse();
  }

  /**
   * Change to layout policy to <CODE>ExpandAll</CODE> and relayout the jobs.
   */ 
  private synchronized void
  doExpandAll()
  {
    clearSelection();
    pExpandDepth  = null;
    pLayoutPolicy = LayoutPolicy.ExpandAll;
    updateUniverse();
  }

  /**
   * Change to layout policy to <CODE>CollapseAll</CODE> and relayout the jobs.
   */ 
  private synchronized void
  doCollapseAll()
  {
    clearSelection();
    pExpandDepth  = null;
    pLayoutPolicy = LayoutPolicy.CollapseAll;
    updateUniverse();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Show/Hide the job detail hints.
   */ 
  private synchronized void
  doShowHideDetailHints()
  {
    clearSelection();
    pShowDetailHints = !pShowDetailHints;
    updateUniverse();
  }
  
  /**
   * Show/Hide the Toolset property as part of the job detail hints.
   */ 
  private synchronized void
  doShowHideToolsetHint()
  {
    clearSelection();
    if(pViewerJobHint != null) 
      pViewerJobHint.setShowToolset(!pViewerJobHint.showToolset());
    updateUniverse();
  }
  
  /**
   * Show/Hide the Action property as part of the job detail hints.
   */ 
  private synchronized void
  doShowHideActionHint()
  {
    clearSelection();
    if(pViewerJobHint != null) 
      pViewerJobHint.setShowAction(!pViewerJobHint.showAction());
    updateUniverse();
  }
  
  /**
   * Show/Hide job server host information as part of the job detail hints.
   */ 
  private synchronized void
  doShowHideHostHint()
  {
    clearSelection();
    if(pViewerJobHint != null) 
      pViewerJobHint.setShowHost(!pViewerJobHint.showHost());
    updateUniverse();
  }
  
  /**
   * Show/Hide job timing information as part of the job detail hints.
   */ 
  private synchronized void
  doShowHideTimingHint()
  {
    clearSelection();
    if(pViewerJobHint != null) 
      pViewerJobHint.setShowTiming(!pViewerJobHint.showTiming());
    updateUniverse();
  }
  
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Hide all job groups.
   */ 
  private synchronized void 
  doHideAll() 
  {
    UIMaster master = UIMaster.getInstance();
    JQueueJobBrowserPanel panel = master.getQueueJobBrowserPanels().getPanel(pGroupID);
    if(panel != null) 
      panel.deselectAllGroups();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Toggle the job group orientation between Horizontal and Vertical.
   */ 
  private synchronized void
  doToggleOrientation() 
  {
    clearSelection();
    pHorizontalOrientation = !pHorizontalOrientation;
    updateUniverse();
  }

  /**
   * Update the job details panels with the current primary selected job status.
   */ 
  private synchronized void
  doDetails()
  {
    if(pPrimary != null)
      pDetailedJobID = pPrimary.getJobStatus().getJobID();

    updatePanels(true);

    clearSelection();
    refresh(); 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * View the target files of the primary selected job.
   */ 
  private synchronized void 
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
   * View the target files of the primary selected job using the default editor for 
   * the file type.
   */ 
  private synchronized void 
  doViewWithDefault() 
  {
    if(pPrimary != null) {
      JobStatus status = pPrimary.getJobStatus();
      ViewTask task = new ViewTask(status.getNodeID(), status.getTargetSequence(), true);
      task.start();
    }
    else if(pPrimaryGroup != null) {
      QueueJobGroup group = pPrimaryGroup.getGroup();
      ViewTask task = new ViewTask(group.getNodeID(), group.getRootSequence(), true);
      task.start();
    }

    clearSelection();
    refresh(); 
  }

  /**
   * View the target files of the primary selected job with the given editor.
   */ 
  private synchronized void 
  doViewWith
  (
   String editor
  ) 
  {
    String parts[] = editor.split(":");
    assert(parts.length == 3);
    
    String ename   = parts[0];
    VersionID evid = new VersionID(parts[1]);
    String evendor = parts[2];

    if(pPrimary != null) {
      JobStatus status = pPrimary.getJobStatus();
      ViewTask task = 
	new ViewTask(status.getNodeID(), status.getTargetSequence(), false, 
		     ename, evid, evendor);
      task.start();
    }
    else if(pPrimaryGroup != null) {
      QueueJobGroup group = pPrimaryGroup.getGroup();
      ViewTask task = 
	new ViewTask(group.getNodeID(), group.getRootSequence(), false, 
		     ename, evid, evendor);
      task.start();
    }

    clearSelection();
    refresh(); 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Resubmit all aborted and failed selected jobs.
   */ 
  private synchronized void 
  doQueueJobs() 
  {
    DoubleMap<NodeID, Long, TreeSet<FileSeq>> targets = getQueuedFileSeqs();
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
  private synchronized void 
  doQueueJobsSpecial() 
  {
    DoubleMap<NodeID, Long, TreeSet<FileSeq>> targets = getQueuedFileSeqs();
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
	
	TreeSet<String> selectionKeys = null;
	if(diag.overrideSelectionKeys()) 
	  selectionKeys = diag.getSelectionKeys();

	TreeSet<String> licenseKeys = null;
	if(diag.overrideLicenseKeys()) 
	  licenseKeys = diag.getLicenseKeys();

	QueueJobsTask task = 
	  new QueueJobsTask(targets, batchSize, priority, interval,
			    selectionKeys, licenseKeys, true);
	task.start();
      }
    }

    clearSelection();
    refresh(); 
  }

  /** 
   * Get the target file sequences of the aborted and failed selected root jobs.
   */ 
  private synchronized DoubleMap<NodeID, Long, TreeSet<FileSeq>> 
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
    DoubleMap<NodeID, Long, TreeSet<FileSeq>> targets = new DoubleMap<NodeID, Long, TreeSet<FileSeq>>();
    if(!failed.isEmpty()) {
      for(JobStatus status : failed.values()) {
	long jobID = status.getJobID();
	NodeID targetID = status.getNodeID();
	String author = targetID.getAuthor();
	if(author.equals(PackageInfo.sUser) || pPrivilegeDetails.isQueueManaged(author)) {
	  TreeSet<FileSeq> fseqs = targets.get(targetID, jobID);
	  if(fseqs == null) {
	    fseqs = new TreeSet<FileSeq>();
	    targets.put(targetID, jobID, fseqs);
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
  private synchronized void 
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
  private synchronized void 
  doResumeJobs() 
  {
    TreeSet<Long> resumed = new TreeSet<Long>();
    for(ViewerJob vjob : pSelected.values()) {
      JobStatus status = vjob.getJobStatus();
      resumed.add(status.getJobID());
    }

    if(!resumed.isEmpty()) {
      ResumeJobsTask task = new ResumeJobsTask(resumed);
      task.start();
    }

    clearSelection();
    refresh(); 
  }

  /**
   * Preempt all jobs associated with the selected nodes.
   */ 
  private synchronized void 
  doPreemptJobs() 
  {
    TreeSet<Long> preempt = new TreeSet<Long>();
    for(ViewerJob vjob : pSelected.values()) {
      JobStatus status = vjob.getJobStatus();
      switch(status.getState()) {
      case Running:
	preempt.add(status.getJobID());
      }
    }

    if(!preempt.isEmpty()) {
      PreemptJobsTask task = new PreemptJobsTask(preempt);
      task.start();
    }

    clearSelection();
    refresh(); 
  }
  
  /**
   * Change the jobs requirements associated with the selected jobs.
   */ 
  private synchronized void 
  doChangeJobReqs() 
  {
    if (pSelected.size() > 0) {
      JChangeJobReqsDialog diag = UIMaster.getInstance().showChangeJobReqDialog();
      if(diag.wasConfirmed()) {
	
	Integer priority = null;
	Integer rampUp = null;
	Float maxLoad = null;
	Long minMemory = null;
	Long minDisk = null;
	Set<String> licenseKeys = null;
	Set<String> selectionKeys = null;
	
	if (diag.overridePriority())
	  priority = diag.getPriority();
	if (diag.overrideRampUp())
	  rampUp = diag.getRampUp();
	if (diag.overrideMaxLoad())
	  maxLoad = diag.getMaxLoad();
	if (diag.overrideMinMemory())
	  minMemory = diag.getMinMemory();
	if (diag.overrideMinDisk())
	  minDisk = diag.getMinDisk();
	if (diag.overrideLicenseKeys())
	  licenseKeys = diag.getLicenseKeys();
	if (diag.overrideSelectionKeys())
	  selectionKeys = diag.getSelectionKeys();
	
	LinkedList<JobReqsDelta> change = new LinkedList<JobReqsDelta>();
	for(ViewerJob vjob : pSelected.values()) {
	  JobStatus status = vjob.getJobStatus();
	  long jobID = status.getJobID();
	  switch(status.getState()) {
	  case Queued:
	  case Paused:
	  case Preempted:
	    JobReqsDelta newReq = new JobReqsDelta
	      (jobID, priority, rampUp, maxLoad, minMemory, minDisk, 
	       licenseKeys, selectionKeys);
	    change.add(newReq);
	    break;
	  }
	}

	if(!change.isEmpty()) {
	  ChangeJobReqsTask task = new ChangeJobReqsTask(change);
	  task.start();
	}
      } //if(diag.wasConfirmed())
    }
    clearSelection();
    refresh();
  }

  /**
   * Kill all jobs associated with the selected nodes.
   */ 
  private synchronized void 
  doKillJobs() 
  {
    TreeSet<Long> dead = new TreeSet<Long>();
    for(ViewerJob vjob : pSelected.values()) {
      JobStatus status = vjob.getJobStatus();
      switch(status.getState()) {
      case Queued:
      case Preempted:
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
   * Hide the selected job groups.
   */ 
  private synchronized void 
  doHideGroups() 
  {
    UIMaster master = UIMaster.getInstance();
    JQueueJobBrowserPanel panel = master.getQueueJobBrowserPanels().getPanel(pGroupID);
    if(panel != null) 
      panel.deselectGroups(new TreeSet<Long>(pSelectedGroups.keySet()));
  }

  /**
   * Delete the primary selected job group.
   */ 
  private synchronized void 
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

  /**
   * Show the node which created the primary selected job/group in the Node Viewer. 
   */ 
  private synchronized void 
  doShowNode() 
  {
    NodeID nodeID = null;
    if(pPrimaryGroup != null) {
      QueueJobGroup group = pPrimaryGroup.getGroup();
      nodeID = group.getNodeID();
    }
    else if(pPrimary != null) {
      JobStatus status = pPrimary.getJobStatus();
      nodeID = status.getNodeID();
    }

    if(pGroupID > 0) {
      PanelGroup<JNodeViewerPanel> panels = UIMaster.getInstance().getNodeViewerPanels();
      JNodeViewerPanel panel = panels.getPanel(pGroupID);
      if(panel != null) { 
	panel.addRoot(nodeID.getAuthor(), nodeID.getView(), nodeID.getName());
	panel.updateManagerTitlePanel();
      }
    }    
    
    clearSelection();
    refresh(); 
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

    /* job detail hints */
    encoder.encode("ShowDetailHints", pShowDetailHints);
    if(pViewerJobHint != null) {
      encoder.encode("ShowToolsetHints", pViewerJobHint.showToolset());
      encoder.encode("ShowActionHints", pViewerJobHint.showAction());
      encoder.encode("ShowHostHints", pViewerJobHint.showHost());
      encoder.encode("ShowTimingHints", pViewerJobHint.showTiming());
    }

    /* initial layout orientation */
    encoder.encode("HorizontalOrientation", pHorizontalOrientation);
  }

  public synchronized void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    /* whether to show the job status detail hints */    
    {
      Boolean show = (Boolean) decoder.decode("ShowDetailHints");
      if(show != null) 
	pShowDetailHints = show; 

      if(pViewerJobHint != null) {
	Boolean tset = (Boolean) decoder.decode("ShowToolsetHints");
	if(tset != null) 
	  pViewerJobHint.setShowToolset(tset);
	
	Boolean act = (Boolean) decoder.decode("ShowActionHints");
	if(act != null) 
	  pViewerJobHint.setShowAction(act);

	Boolean host = (Boolean) decoder.decode("ShowHostHints");
	if(host != null) 
	  pViewerJobHint.setShowHost(host);

	Boolean timing = (Boolean) decoder.decode("ShowTimingHints");
	if(timing != null) 
	  pViewerJobHint.setShowTiming(timing);
      }
    }

    /* whether to orient and align job groups horizontally */    
    {
      Boolean horz = (Boolean) decoder.decode("HorizontalOrientation");
      if(horz != null) 
	pHorizontalOrientation = horz; 
    }
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
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
      this(nodeID, fseq, false, null, null, null);
    }

    public 
    ViewTask
    (
     NodeID nodeID, 
     FileSeq fseq, 
     boolean useDefault
    ) 
    {
      this(nodeID, fseq, useDefault, null, null, null);
    }

    public 
    ViewTask
    (
     NodeID nodeID, 
     FileSeq fseq,
     boolean useDefault,
     String ename,
     VersionID evid, 
     String evendor
    ) 
    {
      super("JQueueJobViewerPanel:ViewTask");

      pNodeID        = nodeID;
      pFileSeq       = fseq; 
      pUseDefault    = useDefault;
      pEditorName    = ename;
      pEditorVersion = evid; 
      pEditorVendor  = evendor;       
    }

    @SuppressWarnings("deprecation")
    public void 
    run() 
    {
      MasterMgrClient client = null;
      SubProcessLight proc = null;
      Long editID = null;
      {
 	UIMaster master = UIMaster.getInstance();
        boolean ignoreExitCode = false;
 	if(master.beginPanelOp(pGroupID, "Launching Node Editor...")) {
 	  try {
	    client = master.getMasterMgrClient(pGroupID);

 	    NodeMod mod = client.getWorkingVersion(pNodeID);
 	    String author = pNodeID.getAuthor();
 	    String view = pNodeID.getView();

 	    /* create an editor plugin instance */ 
 	    BaseEditor editor = null;
 	    {
	      if(pEditorName != null) {
		PluginMgrClient pclient = PluginMgrClient.getInstance();
		editor = pclient.newEditor(pEditorName, pEditorVersion, pEditorVendor);
	      }
	      else if (pUseDefault) {
		FilePattern fpat = mod.getPrimarySequence().getFilePattern();
		String suffix = fpat.getSuffix();
		if(suffix != null) 
		  editor = client.getEditorForSuffix(suffix);
	      }

	      if(editor == null) 
		editor = mod.getEditor();
		
	      if(editor == null) 
		throw new PipelineException
		  ("No Editor plugin was specified for node (" + mod.getName() + ")!");

              if(!editor.supports(PackageInfo.sOsType)) 
                throw new PipelineException
                  ("The Editor plugin (" + editor.getName() + " v" + 
                   editor.getVersionID() + ") from the vendor (" + editor.getVendor() + ") " +
                   "does not support the " + PackageInfo.sOsType.toTitle() + " operating " + 
                   "system!");

              ignoreExitCode = editor.ignoreExitCode();
 	    }

 	    /* lookup the toolset environment */ 
 	    TreeMap<String,String> env = null;
 	    {
 	      String tname = mod.getToolset();
 	      if(tname == null) 
 		throw new PipelineException
 		  ("No toolset was specified for node (" + mod.getName() + ")!");

 	      /* passes author so that WORKING will correspond to the current view */ 
 	      env = client.getToolsetEnvironment(author, view, tname, PackageInfo.sOsType);
 	    }
	    
	    /* get the primary file sequence */ 
	    FileSeq fseq = null;
	    File dir = null; 
	    {
	      Path path = null;
	      {
		 Path wpath = new Path(PackageInfo.sWorkPath, 
				       author + "/" + view + "/" + mod.getName());
		 path = wpath.getParentPath();
 	      }

 	      fseq = new FileSeq(path.toString(), pFileSeq);
 	      dir = path.toFile();
 	    }
	    
 	    /* start the editor */ 
	    editor.makeWorkingDirs(dir);
	    proc = editor.prep(PackageInfo.sUser, fseq, env, dir);
	    if(proc != null) 
	      proc.start();
	    else 
	      proc = editor.launch(fseq, env, dir);

	    editID = client.editingStarted(pNodeID, editor);
 	  }
 	  catch(PipelineException ex) {
 	    master.showErrorDialog(ex);
 	    return;
 	  }
 	  finally {
 	    master.endPanelOp(pGroupID, "Done.");
 	  }
 	}
	
 	/* wait for the editor to exit */ 
 	if(proc != null) {
 	  try {
 	    proc.join();
	    if(!proc.wasSuccessful() && !ignoreExitCode) 
 	      master.showSubprocessFailureDialog("Editor Failure:", proc);

	    if((client != null) && (editID != null))
	      client.editingFinished(editID);
 	  }
 	  catch(Exception ex) {
 	    master.showErrorDialog(ex);
 	  }
 	}
      }
    }

    private NodeID     pNodeID;
    private FileSeq    pFileSeq;
    private boolean    pUseDefault; 
    private String     pEditorName;
    private VersionID  pEditorVersion; 
    private String     pEditorVendor; 
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
      DoubleMap<NodeID, Long, TreeSet<FileSeq>> targets
    ) 
    {
      this(targets, null, null, null, null, null, false);
    }
    
    public 
    QueueJobsTask
    (
     DoubleMap<NodeID, Long, TreeSet<FileSeq>> targets,
     Integer batchSize, 
     Integer priority, 
     Integer rampUp, 
     TreeSet<String> selectionKeys,
     TreeSet<String> licenseKeys,
     boolean special
    ) 
    {
      super("JQueueJobsViewerPanel:QueueJobsTask");

      pTargets       = targets;
      pBatchSize     = batchSize;
      pPriority      = priority; 
      pRampUp        = rampUp; 
      pSelectionKeys = selectionKeys;
      pLicenseKeys   = licenseKeys;
      pSpecial       = special;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID)) {
	try {
	  for(NodeID nodeID : pTargets.keySet()) {
	    master.updatePanelOp(pGroupID, 
				 "Resubmitting Jobs to the Queue: " + nodeID.getName());
	    MasterMgrClient client = master.getMasterMgrClient(pGroupID);
	    TreeMap<Long, TreeSet<FileSeq>> targets = pTargets.get(nodeID);
	    long jobID = targets.firstKey();
	    if (pSpecial) 
	      client.resubmitJobs
	        (nodeID, targets.get(jobID), pBatchSize, pPriority, pRampUp, 
	         pSelectionKeys, pLicenseKeys);
	    else {
	      
	      QueueMgrClient queue = master.getQueueMgrClient(pGroupID);
	      QueueJob job = queue.getJob(jobID);
	      JobReqs reqs = job.getJobRequirements();
	      client.resubmitJobs
	        (nodeID, targets.get(jobID), pBatchSize, reqs.getPriority(), reqs.getRampUp(), 
	         reqs.getSelectionKeys(), reqs.getLicenseKeys());

	    }
	  }
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp(pGroupID, "Done.");
	}

	updatePanels();
      }
    }

    private DoubleMap<NodeID, Long, TreeSet<FileSeq>>  pTargets;
    private Integer                                    pBatchSize;
    private Integer                                    pPriority;
    private Integer                                    pRampUp;
    private TreeSet<String>                            pSelectionKeys;
    private TreeSet<String>                            pLicenseKeys;
    private boolean                                    pSpecial;
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
      if(master.beginPanelOp(pGroupID, "Pausing Jobs...")) {
	try { 
	  master.getQueueMgrClient(pGroupID).pauseJobs(pJobIDs);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp(pGroupID, "Done.");
	}

	updatePanels();
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
      if(master.beginPanelOp(pGroupID, "Resuming Paused Jobs...")) {
	try {
	  master.getQueueMgrClient(pGroupID).resumeJobs(pJobIDs);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp(pGroupID, "Done.");
	}

	updatePanels();
      }
    }

    private TreeSet<Long>  pJobIDs;
  }

  /** 
   * Preempt the given jobs.
   */ 
  private
  class PreemptJobsTask
    extends Thread
  {
    public 
    PreemptJobsTask
    (   
     TreeSet<Long> jobIDs
    ) 
    {
      super("JQueueJobsViewerPanel:PreemptJobsTask");

      pJobIDs = jobIDs;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Preempting Jobs...")) {
	try {
	  master.getQueueMgrClient(pGroupID).preemptJobs(pJobIDs);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp(pGroupID, "Done.");
	}

	updatePanels();
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
      if(master.beginPanelOp(pGroupID, "Killing Jobs...")) {
	try {
	  master.getQueueMgrClient(pGroupID).killJobs(pJobIDs);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp(pGroupID, "Done.");
	}

	updatePanels();
      }
    }

    private TreeSet<Long>  pJobIDs;
  }
  
  /** 
   * Change the job requirements for the given jobs.
   */ 
  private
  class ChangeJobReqsTask
    extends Thread
  {
    public 
    ChangeJobReqsTask
    (   
     LinkedList<JobReqsDelta> jobReqChanges
    ) 
    {
      super("JQueueJobsViewerPanel:ChangeJobReqsTask");

      pJobReqChanges = jobReqChanges;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Changing Job Reqs...")) {
	try {
	  master.getQueueMgrClient(pGroupID).changeJobReqs(pJobReqChanges);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp(pGroupID, "Done.");
	}

	updatePanels();
      }
    }

    private LinkedList<JobReqsDelta> pJobReqChanges;
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
      if(master.beginPanelOp(pGroupID, "Deleting Job Groups...")) {
	try {
	  master.getQueueMgrClient(pGroupID).deleteJobGroups(pGroupAuthors);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp(pGroupID, "Done.");
	}

	updatePanels();
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
   * Whether to display the job detail hints.
   */ 
  private boolean  pShowDetailHints;

  /**
   * Whether to orient and align job groups horizontally (true) or vertically (false).
   */ 
  private boolean pHorizontalOrientation; 

  /**
   * The toolset used to build the editor menu.
   */ 
  private String  pEditorMenuToolset;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The job status hint.
   */ 
  private ViewerJobHint  pViewerJobHint; 

  /**
   * The ID of the job last used to update the job hint.
   */ 
  private Long  pLastJobHintID; 


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
   * The ID of job displayed in the job details panel.
   */ 
  private Long  pDetailedJobID; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The panel popup menu.
   */ 
  private JPopupMenu  pPanelPopup; 

  /**
   * The panel popup menu items.
   */
  private JMenuItem  pUpdateItem;
  private JMenuItem  pFrameAllItem;
  private JMenuItem  pFrameSelectionItem;
  private JMenuItem  pAutomaticExpandItem;
  private JMenuItem  pExpandAllItem;
  private JMenuItem  pCollapseAllItem;
  private JMenuItem  pHideAllItem; 
  private JMenuItem  pToggleOrientationItem;
  private JMenuItem  pShowHideDetailHintsItem;
  private JMenuItem  pShowHideToolsetHintItem;
  private JMenuItem  pShowHideEditorHintItem;
  private JMenuItem  pShowHideActionHintItem;
  private JMenuItem  pShowHideHostHintItem;
  private JMenuItem  pShowHideTimingHintItem;

  /**
   * The job popup menu.
   */ 
  private JPopupMenu  pJobPopup; 

  /**
   * The job popup menu items.
   */ 
  private JMenuItem  pUpdateDetailsItem;
  private JMenuItem  pJobViewItem;
  private JMenuItem  pJobViewWithDefaultItem;
  private JMenuItem  pJobQueueJobsItem;
  private JMenuItem  pJobQueueJobsSpecialItem;
  private JMenuItem  pJobPauseJobsItem;
  private JMenuItem  pJobResumeJobsItem;
  private JMenuItem  pJobPreemptJobsItem;
  private JMenuItem  pJobKillJobsItem;
  private JMenuItem  pChangeJobReqsItem;
  private JMenuItem  pJobShowNodeItem;

  /**
   * The view with submenu.
   */ 
  private JMenu  pViewWithMenu; 


  /**
   * The job group popup menu.
   */ 
  private JPopupMenu  pGroupPopup; 

  /**
   * The group popup menu items.
   */ 
  private JMenuItem  pGroupViewItem;
  private JMenuItem  pGroupViewWithDefaultItem;
  private JMenuItem  pGroupQueueJobsItem;
  private JMenuItem  pGroupQueueJobsSpecialItem;
  private JMenuItem  pGroupPauseJobsItem;
  private JMenuItem  pGroupResumeJobsItem;
  private JMenuItem  pGroupPreemptJobsItem;
  private JMenuItem  pGroupKillJobsItem;
  private JMenuItem  pGroupHideGroupsItem;
  private JMenuItem  pGroupDeleteGroupsItem;
  private JMenuItem  pGroupChangeJobReqsItem;
  private JMenuItem  pGroupShowNodeItem;

  /**
   * The view with group submenu.
   */ 
  private JMenu  pGroupViewWithMenu; 

}
