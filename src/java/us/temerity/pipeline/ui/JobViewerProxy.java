// $Id: JobViewerProxy.java,v 1.1 2009/12/18 19:56:44 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   V I E W E R   P R O X Y                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * An interface for managing a Job Viewer panel.
 */ 
public 
interface JobViewerProxy
  extends PanelProxy
{
  /*----------------------------------------------------------------------------------------*/
  /*  C O N T E N T                                                                         */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get a particular job being displayed using its unique identifier.
   */ 
  public QueueJob
  getJob
  (
   long jobID
  ) 
    throws PipelineException;
    
  /**
   * Get all jobs being displayed.
   */ 
  public Map<Long,QueueJob> 
  getJobs() 
    throws PipelineException;
    

  /**
   * Get the execution information about a particular job being displayed using its unique 
   * identifier.
   */ 
  public QueueJobInfo
  getJobInfo
  (
   long jobID
  ) 
    throws PipelineException;
    
  /**
   * Get the execution information for all jobs being displayed.
   */ 
  public Map<Long,QueueJobInfo> 
  getJobInfos() 
    throws PipelineException;
    

  /**
   * Get a particular job group being displayed using its unique identifier.
   */ 
  public QueueJobGroup
  getJobGroup
  (
   long groupID
  ) 
    throws PipelineException;
    
  /**
   * Get all of the job groups being displayed. 
   */ 
  public Map<Long,QueueJobGroup> 
  getJobGroups() 
    throws PipelineException;


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the IDs of the job groups currently displayed.
   */ 
  public Set<Long> 
  getRoots()
    throws PipelineException;
    
  /**
   * Set the IDs of the job groups to display and update the panel.
   * 
   * @param groupIDs
   *   The unique identifiers of the job groups.
   */ 
  public void 
  setRoots
  (
   Set<Long> groupIDs
  ) 
    throws PipelineException;

  /**
   * Clear the display of all job groups.
   */ 
  public void 
  clearRoots() 
    throws PipelineException;

  

  /*----------------------------------------------------------------------------------------*/
  /*  S E L E C T I O N                                                                     */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the paths from the job group of each network to the currently selected jobs.<P> 
   * 
   * Note that the same job may be displayed multiple times but with different paths from 
   * the root job group.  The {@link JobPath} identifies this unique path for each job.
   */ 
  public Set<JobPath>
  getSelectedPaths() 
    throws PipelineException;

  /**
   * Get the IDs of the currently selected jobs.<P> 
   * 
   * Note that since this method only returns the IDs of the jobs and not the unique
   * {@link JobPath}, it cannot differentiate between different selection states for jobs
   * with the same IDs but different paths.  If any job with a given ID is selected it
   * will be included in the IDs returned.
   */ 
  public Set<Long> 
  getSelectedIDs() 
    throws PipelineException;

  /**
   * Get the IDs of the currently selected job groups.<P> 
   */ 
  public Set<Long> 
  getSelectedGroupIDs() 
    throws PipelineException;

  /**
   * Set the paths from the job group of each network to the jobs to select. <P> 
   * 
   * Note that the same job by be displayed multiple times but with different paths from 
   * the root job group. The {@link JobPath} identifies this unique path for each job.
   */ 
  public void 
  setSelectedPaths
  (
   Set<JobPath> paths
  ) 
    throws PipelineException;

  /**
   * Set the IDs of the jobs to select.<P> 
   * 
   * Note that since this method sets the IDs of the jobs to select and not the unique
   * {@link JobPath}, all jobs with the given IDs will be selected.
   */ 
  public void 
  setSelectedIDs
  (
   Set<Long> IDs
  ) 
    throws PipelineException;
    
  /**
   * Set the IDs of the job groups to select.
   */ 
  public void 
  setSelectedGroupIDs
  (
   Set<Long> IDs
  ) 
    throws PipelineException;
    
  /**
   * Deselect all jobs and job groups.
   */ 
  public void 
  clearSelection() 
    throws PipelineException;

    

  /*----------------------------------------------------------------------------------------*/
  /*  C A M E R A                                                                           */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Move the camera to frame the given bounding box.
   */ 
  public void 
  frameBounds
  (
   BBox2d bbox
  ) 
    throws PipelineException;

  /**
   * Move the camera to frame the bounds of the currently selected jobs and job groups.
   */ 
  public void 
  frameSelection() 
    throws PipelineException;

  /**
   * Move the camera to frame all displayed jobs and job groups.
   */                  
  public void 
  frameAll() 
    throws PipelineException;


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current position of the camera used to display the jobs.
   */ 
  public Point2d
  getCameraPosition()
    throws PipelineException;

  /**
   * Set the current position of the camera used to display the jobs.
   */ 
  public void 
  setCameraPosition
  (
   Point2d pos
  )
    throws PipelineException;


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current zoom factor of the camera used to display the jobs.
   */ 
  public double
  getCameraZoom()
    throws PipelineException;

  /**
   * Set the current zoom factor of the camera used to display the jobs.
   */ 
  public void 
  setCameraZoom
  (
   double zoom
  )
    throws PipelineException;



  /*----------------------------------------------------------------------------------------*/
  /*  J O B   D I S P L A Y                                                                 */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the bounding box which contains both the displayed jobs with the given paths and the 
   * job groups with the given IDs.
   */ 
  public BBox2d
  getJobBounds
  (
   Set<JobPath> paths,
   Set<Long> groupIDs
  ) 
    throws PipelineException;

  /**
   * Get the bounding box which contains all displayed jobs and job groups.
   */ 
  public BBox2d
  getJobBounds() 
    throws PipelineException;


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the display alignment of root nodes of each node network displayed.
   */ 
  public LayoutOrientation
  getOrientation() 
    throws PipelineException;

  /**
   * Set the display alignment of root nodes of each node network displayed.
   */ 
  public void 
  setOrientation
  (
   LayoutOrientation orient
  )
    throws PipelineException;

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Relayout the jobs collapsing all paths.
   */ 
  public void 
  collapseAll() 
    throws PipelineException;

  /**
   * Relayout the jobs expanding all paths.
   */ 
  public void 
  expandAll() 
    throws PipelineException;
    
  /**
   * Relayout the jobs expanding all paths to the given depth.
   */ 
  public void 
  expandToDepth
  (
   int depth
  ) 
    throws PipelineException;
    
  /**
   * Relayout the jobs expanding only the first occurance of a given job. 
   */ 
  public void 
  automaticExpand() 
    throws PipelineException;
    
  /**
   * Get whether the job identified by the given path is currently collapsed.
   */ 
  public boolean 
  getJobCollapsed
  (
   JobPath path
  ) 
    throws PipelineException;
    
  /**
   * Set whether the job identified by the given path should be collapsed and relayout.
   */ 
  public void 
  setJobCollapsed
  (
   JobPath path, 
   boolean collapse
  ) 
    throws PipelineException;
    
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get whether node detail hints should be displayed.
   */ 
  public boolean 
  getShowJobDetailHints() 
    throws PipelineException;

  /**
   * Set whether node detail hints should be displayed.
   */ 
  public void
  setShowJobDetailHints
  (
   boolean show
  ) 
    throws PipelineException;


  /**
   * Get whether toolset hints should be displayed.
   */ 
  public boolean 
  getShowToolsetHints() 
    throws PipelineException;

  /**
   * Set whether toolset hints should be displayed.
   */ 
  public void
  setShowToolsetHints
  (
   boolean show
  ) 
    throws PipelineException;

  
  /**
   * Get whether execution host hints should be displayed.
   */ 
  public boolean 
  getShowHostHints() 
    throws PipelineException;

  /**
   * Set whether execution host hints should be displayed.
   */ 
  public void
  setShowHostHints
  (
   boolean show
  ) 
    throws PipelineException;


  /**
   * Get whether execution timing hints should be displayed.
   */ 
  public boolean 
  getShowTimingHints() 
    throws PipelineException;

  /**
   * Set whether execution timing hints should be displayed.
   */ 
  public void
  setShowTimingHints
  (
   boolean show
  ) 
    throws PipelineException;


  /**
   * Get whether action plugin hints should be displayed.
   */ 
  public boolean 
  getShowActionHints() 
    throws PipelineException;

  /**
   * Set whether action plugin hints should be displayed.
   */ 
  public void
  setShowActionHints
  (
   boolean show
  ) 
    throws PipelineException;



  /*----------------------------------------------------------------------------------------*/
  /*  E D I T I N G                                                                         */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Edit/View the currently displayed job with the given ID using the Editor specified
   * by the job.
   */ 
  public void 
  edit
  (
   String jobID
  ) 
    throws PipelineException;
    
  /**
   * Edit/View the currently displayed job with the given ID using the default Editor 
   * for file type.
   */ 
  public void 
  editWithDefault
  (
   String jobID
  ) 
    throws PipelineException;

  /**
   * Edit/View the currently displayed job with the given ID using the default Editor 
   * for file type.
   * 
   * @param jobID
   *   The unique group identifier.
   * 
   * @param ename
   *   The name of the Editor plugin.
   * 
   * @param evid
   *   The revision number of the Editor plugin.
   * 
   * @param evendor 
   *   The vendor of the Editor plugin.
   */ 
  public void 
  editWith
  (
   String jobID, 
   String ename, 
   VersionID evid, 
   String evendor
  ) 
    throws PipelineException;


  /*----------------------------------------------------------------------------------------*/

  /**
   * Edit/View the currently displayed job group with the given ID using the Editor specified
   * by the jobs.
   */ 
  public void 
  editGroup
  (
   String groupID
  ) 
    throws PipelineException;
    
  /**
   * Edit/View the currently displayed job group with the given ID using the default Editor 
   * for file type.
   */ 
  public void 
  editGroupWithDefault
  (
   String groupID
  ) 
    throws PipelineException;

  /**
   * Edit/View the currently displayed job group with the given ID using the default Editor 
   * for file type.
   * 
   * @param groupID
   *   The unique job group identifier.
   * 
   * @param ename
   *   The name of the Editor plugin.
   * 
   * @param evid
   *   The revision number of the Editor plugin.
   * 
   * @param evendor 
   *   The vendor of the Editor plugin.
   */ 
  public void 
  editGroupWith
  (
   String groupID, 
   String ename, 
   VersionID evid, 
   String evendor
  ) 
    throws PipelineException;
}
