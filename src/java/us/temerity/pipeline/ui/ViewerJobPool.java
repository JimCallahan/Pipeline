// $Id: ViewerJobPool.java,v 1.2 2004/08/30 06:52:15 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.util.*;
import javax.media.j3d.*;

/*------------------------------------------------------------------------------------------*/
/*   V I E W E R   J O B   P O O L                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * An efficient reuseable collection of {@link ViewerJob ViewerJob} objects located 
 * under a common {@link BranchGroup BranchGroup}. <P> 
 * 
 * {@link JQueueJobViewerPanel JQueueJobViewerPanel} instances use this class to maintain a 
 * set of <CODE>ViewerJob</CODE> instances which correspond to {@link JobStatus JobStatus} 
 * objects retrieved from <B>plqueuemgr</B>(1).  When the <CODE>JobStatus</CODE> of a job 
 * is updated, any existing associated <CODE>ViewerJob</CODE> simply has its appearance
 * updated to reflect the changes. <P>
 * 
 * When existing <CODE>ViewerJob</CODE> instances are no longer needed they are simply
 * hidden rather than being deallocated and removed from the Java3D scene.  When new 
 * <CODE>ViewerJob</CODE> instances are required to represent updated or new 
 * <CODE>JobStatus</CODE> instances, these hidden <CODE>ViewerJob</CODE> instances are 
 * resused instead of creating new instances.  Only when there are no remaining hidden 
 * <CODE>ViewerJob</CODE> are new instances allocated. <P> 
 */
public
class ViewerJobPool
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constuct a new viewer job pool. 
   */ 
  public
  ViewerJobPool()
  {
    BranchGroup bg = new BranchGroup();
    bg.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
    pRoot = bg;      

    pActive   = new HashMap<JobPath,ViewerJob>();
    pPrevious = new HashMap<JobPath,ViewerJob>();
    pReserve  = new Stack<ViewerJob>();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The root group containing all job groups. 
   */ 
  public BranchGroup
  getBranchGroup()
  {
    return pRoot;
  }


  /**
   * Get the job paths of the active viewer jobs.
   */ 
  public synchronized Set<JobPath>
  getActiveJobPaths() 
  {
    return Collections.unmodifiableSet(pActive.keySet());
  }

  /**
   * Get the active viewer job reachable by the given {@link JobPath JobPath}.
   * 
   * @param path
   *   The path from the focus job to the current job.
   */ 
  public synchronized ViewerJob
  getActiveViewerJob
  (
   JobPath path  
  ) 
  {
    return pActive.get(path);
  }

  /** 
   * Get all of the active viewer jobs.
   */ 
  public synchronized Collection<ViewerJob>
  getActiveViewerJobs()
  {
    return Collections.unmodifiableCollection(pActive.values());
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   N O D E   S T A T E                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Prepare to update the job status.
   */ 
  public synchronized void 
  updatePrep() 
  {
    /* hide the active jobs */ 
    for(ViewerJob vjob : pActive.values()) 
      vjob.setVisible(false);

    /* swap job tables */ 
    pPrevious = pActive;
    pActive   = new HashMap<JobPath,ViewerJob>(pPrevious.size());
  }

  /**
   * Lookup an existing or create a new {@link ViewerJob ViewerJob} instance to represent 
   * the given {@link JobStatus JobStatus} reachable by the given {@link JobPath JobPath}.
   * 
   * @param status
   *   The current job status.
   * 
   * @param path
   *   The path from the focus job to the current job.
   */ 
  public synchronized ViewerJob
  lookupOrCreateViewerJob
  (
   JobStatus status, 
   JobPath path  
  ) 
  {
    if(status == null) 
      throw new IllegalArgumentException("The job status cannot be (null)!");

    if(path == null) 
      throw new IllegalArgumentException("The job path cannot be (null)!");

    assert(status.getJobID() == path.getCurrentJobID());
    assert(!pActive.containsKey(path));

    /* look up the exact path from the previous viewer jobs */ 
    ViewerJob vjob = pPrevious.remove(path);
    if(vjob == null) {
      String text = status.toString();

      /* grab the first available viewer job in the reserve */ 
      if(!pReserve.isEmpty()) 
	vjob = pReserve.pop();

      /* the reserve is empty, create a new viewer job */ 
      if(vjob == null) {
	vjob = new ViewerJob();
 	pRoot.addChild(vjob.getBranchGroup());
      }
    }
    pActive.put(path, vjob);

    /* update state */ 
    vjob.setCurrentState(status, path);
    
    return vjob;
  }

  /**
   * Update the appearance of the active {@link ViewerJob ViewerJob} instances to 
   * reflect changes in their associated {@link JobStatus JobStatus} and the viewer 
   * layout scheme.
   */ 
  public synchronized void 
  update()
  {
    /* update and show the active jobs */ 
    for(ViewerJob vjob : pActive.values()) 
      vjob.update(); 
    for(ViewerJob vjob : pActive.values()) 
      vjob.setVisible(true);

    /* hide any previously active jobs which are no longer in use 
        and move them to the reserve */ 
    for(ViewerJob vjob : pPrevious.values()) {
      vjob.reset();
      pReserve.push(vjob);
    }

    pPrevious = null;
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The root branch group. 
   */ 
  private BranchGroup  pRoot; 


  /**
   * The table of currently active viewer jobs indexed by job path.
   */ 
  private HashMap<JobPath,ViewerJob>  pActive;

  /**
   * The table of previously active viewer jobs indexed by job path.
   */ 
  private HashMap<JobPath,ViewerJob>  pPrevious;

  /**
   * The reserve of inactive viewer jobs ready for reuse. 
   */ 
  private Stack<ViewerJob> pReserve;

}
