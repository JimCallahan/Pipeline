// $Id: PanelUpdater.java,v 1.1 2006/10/18 06:34:22 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;

/*------------------------------------------------------------------------------------------*/
/*   P A N E L   U P D A T E R                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Performs a general update operation for all panels belonging to the same update channel.
 */ 
public 
class PanelUpdater
  extends Thread
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A panel update originating from the Node Browser panel. <P> 
   * 
   * Could be due to a change in the selected nodes or a simple update.
   */ 
  public
  PanelUpdater
  (
   JNodeBrowserPanel panel,
   boolean forceUpdate
  ) 
  {
    initPanels(panel);

    pNodeBrowserSelection = panel.getSelected(); 

    if(pNodeViewerPanel != null) {
      pNodeViewerRoots = pNodeViewerPanel.getRoots();

      /* have any selections been added which aren't already root nodes? */ 
      boolean anyNew = forceUpdate;	
      if(!anyNew) {
	for(String name : pNodeBrowserSelection) {
	  if(!pNodeViewerRoots.containsKey(name)) {
	    anyNew = true;
	    break;
	  }
	}
      }
	
      /* determine the names of any root nodes which are no longer selected */ 
      TreeSet<String> dead = new TreeSet<String>();
      for(String name : pNodeViewerRoots.keySet()) {
	if(!pNodeBrowserSelection.contains(name)) 
	  dead.add(name);
      }

      /* prep for a full update */ 
      if(anyNew) {
	pNodeViewerRoots.clear();
	for(String name : pNodeBrowserSelection) 
	  pNodeViewerRoots.put(name, null);
      }
      /* just get rid of the obsolete roots, no update will be performed */ 
      else {
	for(String name : dead) {
	  pNodeViewerRoots.remove(name);
	  pNodeStatusModified = true;
	}
      }
    }
  }

  /**
   * A panel update originating from the Node Viewer panel. <P> 
   * 
   * The update could be due to a change in the root nodes or a simple update.
   */ 
  public
  PanelUpdater
  (
   JNodeViewerPanel panel,
   boolean detailsOnly
  ) 
  {
    pNodeDetailsOnly = detailsOnly; 
    initPanels(panel);
    pNodeViewerRoots = panel.getRoots();
  }

  /**
   * A panel update originating from the Node Details panel. 
   */ 
  public
  PanelUpdater
  (
   JNodeDetailsPanel panel
  ) 
  {
    initPanels(panel);
  }

  /**
   * A panel update originating from the Node Files panel. 
   */ 
  public
  PanelUpdater
  (
   JNodeFilesPanel panel
  ) 
  {
    initPanels(panel);
  }

  /**
   * A panel update originating from the Node Links panel. 
   */ 
  public
  PanelUpdater
  (
   JNodeLinksPanel panel
  ) 
  {
    initPanels(panel);
  }

  /**
   * A panel update originating from the Node History panel. 
   */ 
  public
  PanelUpdater
  (
   JNodeHistoryPanel panel
  ) 
  {
    initPanels(panel);
  }


  /**
   * A panel update originating from the Job Servers panel. 
   */ 
  public
  PanelUpdater
  (
   JQueueJobServersPanel panel
  ) 
  {
    initPanels(panel);
  }
  
  /**
   * A panel update originating from the Job Slots panel. 
   */ 
  public
  PanelUpdater
  (
   JQueueJobSlotsPanel panel, 
   Long selected
  ) 
  {
    pJobSlotsSelectionOnly = (selected != null); 
    initPanels(panel);
    pSlotsSelectedJobID = selected;
  }

  /**
   * A panel update originating from the Job Browser panel. 
   */ 
  public
  PanelUpdater
  (
   JQueueJobBrowserPanel panel,
   boolean selectionOnly
  ) 
  {
    pJobBrowserSelectionOnly = selectionOnly; 
    initPanels(panel);
    pJustSelectedJobGroupID = panel.getJustSelectedGroupID();
  }

  /**
   * A panel update originating from the Job Viewer panel. 
   */ 
  public
  PanelUpdater
  (
   JQueueJobViewerPanel panel,
   boolean detailsOnly
  ) 
  {
    pJobDetailsOnly = detailsOnly; 
    initPanels(panel);
  }

  /**
   * A panel update originating from the Job Details panel. 
   */ 
  public
  PanelUpdater
  (
   JQueueJobDetailsPanel panel
  ) 
  {
    initPanels(panel);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the panels which share the same update channel group.
   */ 
  private void 
  initPanels
  (
   JTopLevelPanel panel
  ) 
  { 
    UIMaster master = UIMaster.getInstance();  
    pIsRestoring = master.isRestoring();
    
    pAuthor = panel.getAuthor();
    pView   = panel.getView();

    pGroupID = panel.getGroupID();
    if(pGroupID > 0) {
      pNodeBrowserPanel     = master.getNodeBrowserPanels().getPanel(pGroupID);    
      pNodeViewerPanel      = master.getNodeViewerPanels().getPanel(pGroupID);  
      pNodeDetailsPanel     = master.getNodeDetailsPanels().getPanel(pGroupID); 
      pNodeHistoryPanel     = master.getNodeHistoryPanels().getPanel(pGroupID); 
      pNodeFilesPanel       = master.getNodeFilesPanels().getPanel(pGroupID);   
      pNodeLinksPanel       = master.getNodeLinksPanels().getPanel(pGroupID);   
      pQueueJobServersPanel = master.getQueueJobServersPanels().getPanel(pGroupID);
      pQueueJobSlotsPanel   = master.getQueueJobSlotsPanels().getPanel(pGroupID);
      pQueueJobBrowserPanel = master.getQueueJobBrowserPanels().getPanel(pGroupID);
      pQueueJobViewerPanel  = master.getQueueJobViewerPanels().getPanel(pGroupID);
      pQueueJobDetailsPanel = master.getQueueJobDetailsPanels().getPanel(pGroupID);
    }

    if(pNodeViewerPanel != null) {
      pDetailedNodeName = pNodeViewerPanel.getDetailedNodeName();

      if(!(panel instanceof JNodeViewerPanel) && 
	 !pJobDetailsOnly && !pJobBrowserSelectionOnly && !pJobSlotsSelectionOnly) {

	pNodeViewerRoots  = pNodeViewerPanel.getRoots();
	for(String name : pNodeViewerRoots.keySet()) 
	  pNodeViewerRoots.put(name, null);
      }
    }

    if(pQueueJobBrowserPanel != null) 
      pSelectedJobGroupIDs = pQueueJobBrowserPanel.getSelectedGroupIDs();

    if(pQueueJobViewerPanel != null) 
      pDetailedJobID = pQueueJobViewerPanel.getDetailedJobID();
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Perform the update in a seperate thread.
   */ 
  public void 
  run()
  {
    if(pIsRestoring) 
      return;

    boolean success = true;
    UIMaster master = UIMaster.getInstance();
    if(master.beginPanelOp(pGroupID)) {
      TaskTimer timer = new TaskTimer();
      try {
	MasterMgrClient mclient = master.getMasterMgrClient(pGroupID);
	QueueMgrClient qclient  = master.getQueueMgrClient(pGroupID);
	
	/* clear privileges cache */ 
	master.getMasterMgrClient(pGroupID).invalidateCachedPrivilegeDetails();

	if(!pJobDetailsOnly && !pJobBrowserSelectionOnly && !pJobSlotsSelectionOnly) {

	  /* node viewer panel */ 
	  if(pNodeViewerRoots != null) {

	    /* update node status */ 
	    if(!pNodeDetailsOnly) {
	      TreeSet<String> dead = new TreeSet<String>();
	      for(String name : pNodeViewerRoots.keySet()) {
		if(pNodeViewerRoots.get(name) == null) {
		  master.updatePanelOp(pGroupID, "Updating Node Status...");
		  
		  NodeStatus status = mclient.status(pAuthor, pView, name);
		  pNodeViewerRoots.put(name, status);
		  
		  pNodeStatusModified = true;
		}
	      }	 
	      
	      for(String name : dead) 
		pNodeViewerRoots.remove(name);
	    }
	    
	    /* find the current status of detailed node */ 
	    if(pDetailedNodeName != null) {
	      for(NodeStatus root : pNodeViewerRoots.values()) {
		pDetailedNode = findNodeStatus(root, pDetailedNodeName);
		if(pDetailedNode != null) 
		  break;
	      }
	    }
	  }

	  /* node details panels */ 
	  if(pDetailedNode != null) {
	    
	    /* file novelty */ 
	    if(pNodeFilesPanel != null) {
	      master.updatePanelOp(pGroupID, "Updating File Novelty...");
	      pFileNovelty = mclient.getCheckedInFileNovelty(pDetailedNodeName);
	    }
	    
	    /* checked-in links */ 
	    if(pNodeLinksPanel != null) {
	      master.updatePanelOp(pGroupID, "Updating Node Links...");
	      pCheckedInLinks = mclient.getCheckedInLinks(pDetailedNodeName);
	    }
	    
	    /* node check-in history */ 
	    if(pNodeHistoryPanel != null) {
	      master.updatePanelOp(pGroupID, "Updating Node History...");
	      pNodeHistory = mclient.getHistory(pDetailedNodeName);
	    }
	    
	    /* online version IDs */ 
	    if((pNodeFilesPanel != null) || 
	       (pNodeLinksPanel != null) || 
	       (pNodeHistoryPanel != null)) {
	      
	      master.updatePanelOp(pGroupID, "Updating Offlined Versions...");
	      pOfflineVersionIDs = mclient.getOfflineVersionIDs(pDetailedNodeName);
	    }
	  }
	}

	/* license & selection keys */ 
	if((pNodeDetailsPanel != null) || (pQueueJobDetailsPanel != null)) {
	  master.updatePanelOp(pGroupID, "Updating License/Selection Keys...");
	  pLicenseKeys   = qclient.getLicenseKeys();
	  pSelectionKeys = qclient.getSelectionKeys();
	}

	if(!pNodeDetailsOnly) {
	  if(!pJobDetailsOnly) {

	    /* job browser/viewer panel related */ 
	    if((pQueueJobBrowserPanel != null) || (pQueueJobViewerPanel != null)) {
	      master.updatePanelOp(pGroupID, "Updating Jobs...");
	      pJobGroups = qclient.getJobGroups(); 
	      pJobStatus = qclient.getJobStatus(new TreeSet<Long>(pJobGroups.keySet()));
	      
	      if(pSelectedJobGroupIDs != null) {
		pSelectedJobGroups = new TreeMap<Long,QueueJobGroup>();
		for(Long groupID : pSelectedJobGroupIDs) {
		  QueueJobGroup group = pJobGroups.get(groupID);
		  if(group != null) 
		    pSelectedJobGroups.put(groupID, group);
		}
	      }
	    }
	    
	    if(!pJobBrowserSelectionOnly && !pJobSlotsSelectionOnly) {

	      /* job servers panel related */ 
	      if(pQueueJobServersPanel != null) {
		master.updatePanelOp(pGroupID, "Updating Job Servers...");
		pHosts              = qclient.getHosts();
		pSelectionGroups    = qclient.getSelectionGroupNames();
		pSelectionSchedules = qclient.getSelectionScheduleNames();
	      }
	      
	      /* job slots panel related */
	      if(pQueueJobSlotsPanel != null) {
		master.updatePanelOp(pGroupID, "Updating Job Slots...");
		if(pJobStatus == null) 
		  pJobStatus = qclient.getRunningJobStatus(); 
		
		if(pHosts == null) 
		  pHosts = qclient.getHosts();
		
		pJobInfo = qclient.getRunningJobInfo();
	      }
	    }
	  }

	  /* job details panel related */ 
	  if(pQueueJobDetailsPanel != null) {

	    /* selection from the job slots panel */ 
	    if(pSlotsSelectedJobID != null) {
	      pDetailedJobID = pSlotsSelectedJobID;
	    }
	    /* selection from the job viewer panel */ 
	    else if(pSelectedJobGroups != null) {

	      /* automatically pick the only job of a just selected single job group */ 
	      if(pJustSelectedJobGroupID != null) {
		QueueJobGroup group = pSelectedJobGroups.get(pJustSelectedJobGroupID);
		if(group != null) {
		  SortedSet<Long> jobIDs = group.getJobIDs();
		  if(jobIDs.size() == 1)
		    pDetailedJobID = jobIDs.first();
		}
	      }
	      /* otherwise, make sure the last detailed job is still a member of the 
		 selected groups */ 
	      else if(pDetailedJobID != null) {
		boolean found = false;
		for(QueueJobGroup group : pSelectedJobGroups.values()) {
		  if(group.getJobIDs().contains(pDetailedJobID) ||
		     group.getExternalIDs().contains(pDetailedJobID)) {
		    found = true;
		    break;
		  }
		}
	      
		if(!found) 
		  pDetailedJobID = null;
	      }
	    }

	    /* use the last job selected */ 
	    if(pDetailedJobID != null) {
	      master.updatePanelOp(pGroupID, "Updating Job Details...");
	    
	      pDetailedJob = qclient.getJob(pDetailedJobID);
	    
	      if(pJobInfo != null) 
		pDetailedJobInfo = pJobInfo.get(pDetailedJobID);
	    
	      if(pDetailedJobInfo == null) 
		pDetailedJobInfo = qclient.getJobInfo(pDetailedJobID);
	    
	      if(pDetailedJobInfo != null) {
		String hostname = pDetailedJobInfo.getHostname();
		if(hostname != null) {
		  JobMgrClient jclient = new JobMgrClient(hostname);
		  pDetailedJobExecDetails = jclient.getExecDetails(pDetailedJobID);
		}
	      }
	    }
	  }
	}
      }
      catch(PipelineException ex) {
	master.showErrorDialog(ex);
	success = false;
      }
      finally {
	master.endPanelOp(pGroupID, success ? "Done." : "Failed!");
      }
    }

    if(success) {
      UpdateTask task = new UpdateTask();
      SwingUtilities.invokeLater(task);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Recursively search the given node status and all upstream nodes for a node status 
   * with the given name.
   * 
   * @return 
   *   The found node status or <CODE>null</CODE> if not found.
   */ 
  private NodeStatus
  findNodeStatus
  (
   NodeStatus root, 
   String name
  ) 
  {
    if(root != null) {
      if(root.getName().equals(name)) 
 	return root;
      
      for(NodeStatus status : root.getSources()) {
 	NodeStatus found = findNodeStatus(status, name);
 	if(found != null) 
 	  return found;
      }
    }
    
    return null;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Update the user interface.
   */
  private 
  class UpdateTask
    extends Thread
  {
    public 
    UpdateTask() 
    {
      super("PanelUpdate:UpdateTask");
    }
  
    public void 
    run()
    {	
      /* full update... */ 
      if(!pJobDetailsOnly && !pJobBrowserSelectionOnly) {

	/* the selected/root nodes have changed or been updated */ 
	if(pNodeStatusModified) {

	  /* node browser */ 
	  if(pNodeBrowserPanel != null) {
	    TreeSet<String> selected = null;
	    if(pNodeBrowserSelection != null)
	      selected = pNodeBrowserSelection; 
	    else if(pNodeViewerRoots != null) 
	      selected = new TreeSet<String>(pNodeViewerRoots.keySet());
	    
	    if(selected != null) 
	      pNodeBrowserPanel.applyPanelUpdates(pAuthor, pView, selected); 
	  }
	  
	  /* node viewer */ 
	  if(pNodeViewerPanel != null) {
	    if(pNodeViewerRoots != null) 
	      pNodeViewerPanel.applyPanelUpdates(pAuthor, pView, pNodeViewerRoots);
	  }
	}
	
	/* node details */ 
	if(pNodeDetailsPanel != null) 
	  pNodeDetailsPanel.applyPanelUpdates
	    (pAuthor, pView, pDetailedNode, pLicenseKeys, pSelectionKeys);
	
	/* node files */ 
	if(pNodeFilesPanel != null) 
	  pNodeFilesPanel.applyPanelUpdates
	    (pAuthor, pView, pDetailedNode, pFileNovelty, pOfflineVersionIDs);
	
	/* node links */ 
	if(pNodeLinksPanel != null) 
	  pNodeLinksPanel.applyPanelUpdates
	    (pAuthor, pView, pDetailedNode, pCheckedInLinks, pOfflineVersionIDs);
	
	/* node history */ 
	if(pNodeHistoryPanel != null) 
	  pNodeHistoryPanel.applyPanelUpdates
	    (pAuthor, pView, pDetailedNode, pNodeHistory, pOfflineVersionIDs);
      }

      /* full update... */ 
      if(!pNodeDetailsOnly) {
	if(!pJobDetailsOnly) {
	  if(!pJobBrowserSelectionOnly) {

	    /* job servers */ 
	    if(pQueueJobServersPanel != null) 
	      pQueueJobServersPanel.applyPanelUpdates
		(pAuthor, pView, pHosts, pSelectionGroups, pSelectionSchedules);
	    
	    /* job slots */ 
	    if(pQueueJobSlotsPanel != null) 
	      pQueueJobSlotsPanel.applyPanelUpdates
		(pAuthor, pView, pJobStatus, pJobInfo, pHosts); 
	  }
	  
	  /* job browser */ 
	  if(pQueueJobBrowserPanel != null) 
	    pQueueJobBrowserPanel.applyPanelUpdates
	      (pAuthor, pView, pJobGroups, pJobStatus);
	  
	  /* job viewer */ 
	  if(pQueueJobViewerPanel != null) 
	    pQueueJobViewerPanel.applyPanelUpdates
	      (pAuthor, pView, pDetailedJobID, pSelectedJobGroups, pJobStatus);	
	}
	
	/* job details */ 
	if(pQueueJobDetailsPanel != null) 
	  pQueueJobDetailsPanel.applyPanelUpdates
	    (pAuthor, pView, pDetailedJob, pDetailedJobInfo, pDetailedJobExecDetails, 
	     pLicenseKeys, pSelectionKeys);
      }
    }
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Whether a panel layout is currently being restored.
   */
  private boolean  pIsRestoring; 


  /** 
   * The name of user which owns the working area view associated with the source panel.
   */
  private String  pAuthor;

  /** 
   * The name of the working area view associated with the source panel.
   */
  private String  pView;

  /**
   * The group ID of the source panel or (0) if unassigned.
   */ 
  private int pGroupID;


  /**
   * Panels sharing the group or <NULL> if not in the group.
   */ 
  private JNodeBrowserPanel      pNodeBrowserPanel;
  private JNodeViewerPanel       pNodeViewerPanel;  
  private JNodeDetailsPanel      pNodeDetailsPanel;
  private JNodeHistoryPanel      pNodeHistoryPanel; 
  private JNodeFilesPanel        pNodeFilesPanel;     
  private JNodeLinksPanel        pNodeLinksPanel;     
  private JQueueJobServersPanel  pQueueJobServersPanel;
  private JQueueJobSlotsPanel    pQueueJobSlotsPanel;
  private JQueueJobBrowserPanel  pQueueJobBrowserPanel;
  private JQueueJobViewerPanel   pQueueJobViewerPanel;
  private JQueueJobDetailsPanel  pQueueJobDetailsPanel;



  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether the selected/root nodes have changed or been updated.
   */ 
  private boolean  pNodeStatusModified; 
  

  /**
   * The names of the new changed selected nodes from the Node Browser panel.
   */ 
  private TreeSet<String>  pNodeBrowserSelection; 


  /**
   * The status of all currently displayed roots indexed by root node name from the 
   * Node Viewer panel.
   */ 
  private TreeMap<String,NodeStatus>  pNodeViewerRoots;
  
  /**
   * The names of the root nodes from the Node Viewer panel which require updating.
   */ 
  private TreeSet<String>  pNodeViewerRootsToUpdate;

  /**
   * Whether to update only the node details panels.
   */
  private boolean  pNodeDetailsOnly; 
 
  /**
   * The name of the node currently displayed in the node details panels. 
   */ 
  private String  pDetailedNodeName; 

  /**
   * The node status for the node currently displayed in the node details panels. 
   */ 
  private NodeStatus  pDetailedNode; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether each file associated with each checked-in version of a node 
   * contains new data not present in the previous checked-in versions.
   */ 
  private TreeMap<VersionID,TreeMap<FileSeq,boolean[]>>  pFileNovelty;

  /**
   * The upstream links of all checked-in versions of a node.
   */ 
  private TreeMap<VersionID,TreeMap<String,LinkVersion>>  pCheckedInLinks;

  /**
   * The log messages associated with all checked-in versions of a node.
   */ 
  private TreeMap<VersionID,LogMessage>  pNodeHistory;

  /**
   * The revision nubers of all offline checked-in versions of the given node.
   */ 
  private TreeSet<VersionID>  pOfflineVersionIDs;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The current license keys.
   */
  private ArrayList<LicenseKey>  pLicenseKeys; 

  /**
   * The current selection keys.
   */
  private ArrayList<SelectionKey>  pSelectionKeys; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The current job groups.
   */
  private TreeMap<Long,QueueJobGroup>  pJobGroups; 

  /**
   * The currently selected job groups.
   */
  private TreeMap<Long,QueueJobGroup>  pSelectedJobGroups; 

  /**
   * The abreviated status of all jobs associated with the current job groups.
   */
  private TreeMap<Long,JobStatus>  pJobStatus; 

  /**
   * More detailed timing and exit status information for running jobs.
   */
  private TreeMap<Long,QueueJobInfo>  pJobInfo;

  /**
   * The current status of job servers in the queue.
   */
  private TreeMap<String,QueueHostInfo>  pHosts; 

  /**
   * The current selection groups
   */
  private TreeSet<String>  pSelectionGroups;

  /**
   * The current selection keys.
   */
  private TreeSet<String>  pSelectionSchedules;

  
  /**
   * The IDs of all selected job groups; 
   */ 
  private TreeSet<Long>  pSelectedJobGroupIDs; 

  /**
   * The ID of a single just selected job group, otherwise <CODE>null</CODE>.
   */ 
  private Long  pJustSelectedJobGroupID; 

  /**
   * The ID of the job last selected in the job slots panel. 
   */ 
  private Long pSlotsSelectedJobID; 

  /**
   * Whether the update is due only to a change in job slots selection.
   */
  private boolean  pJobSlotsSelectionOnly; 
 
  /**
   * Whether the update is due only to a change in job browser selection.
   */
  private boolean  pJobBrowserSelectionOnly; 
 
  /**
   * Whether to update only the job details panel.
   */
  private boolean  pJobDetailsOnly; 
 
  /**
   * The ID of the job currently displayed in the job details panel. 
   */ 
  private Long  pDetailedJobID; 

  /**
   * The job displayed in the job details panel.
   */ 
  private QueueJob  pDetailedJob; 

  /**
   * More detailed timing and exit status inforation for the job displayed in the 
   * job details panel.
   */ 
  private QueueJobInfo  pDetailedJobInfo; 

  /**
   * The full execution details of a job subprocess displayed in the job details panel.
   */ 
  private SubProcessExecDetails  pDetailedJobExecDetails;


}
