// $Id: ViewerJobGroupPool.java,v 1.1 2004/08/30 06:52:46 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.util.*;
import javax.media.j3d.*;

/*------------------------------------------------------------------------------------------*/
/*   V I E W E R   J O B   G R O U P   P O O L                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * An efficient reuseable collection of {@link ViewerJobGroup ViewerJobGroup} objects located 
 * under a common {@link BranchGroup BranchGroup}. <P> 
 * 
 * {@link JQueueJobViewerPanel JQueueJobViewerPanel} instances use this class to maintain a 
 * set of <CODE>ViewerJobGroup</CODE> instances which correspond to 
 * @{link QueueJobGroup QueueJobGroup} objects retrieved from <B>plqueuemgr</B>(1).  When 
 * the <CODE>QueueJobGroup</CODE> is updated, any existing associated 
 * <CODE>ViewerJobGroup</CODE> simply has its appearance updated to reflect the changes. <P>
 * 
 * When existing <CODE>ViewerJobGroup</CODE> instances are no longer needed they are 
 * hidden rather than being deallocated and removed from the Java3D scene.  When new 
 * <CODE>ViewerJobGroup</CODE> instances are required to represent updated or new 
 * <CODE>QueueJobGroup</CODE> instances, these hidden <CODE>ViewerJobGroup</CODE> instances 
 * are resused instead of creating new instances.  Only when there are no remaining hidden 
 * <CODE>ViewerJobGroup</CODE> are new instances allocated. <P> 
 */
public
class ViewerJobGroupPool
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constuct a new viewer job pool. 
   */ 
  public
  ViewerJobGroupPool()
  {
    BranchGroup bg = new BranchGroup();
    bg.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
    pRoot = bg;      

    pActive   = new TreeMap<Long,ViewerJobGroup>();
    pPrevious = new TreeMap<Long,ViewerJobGroup>();
    pReserve  = new Stack<ViewerJobGroup>();
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
  public synchronized Set<Long>
  getActiveGroupIDs() 
  {
    return Collections.unmodifiableSet(pActive.keySet());
  }

  /**
   * Get the active viewer job reachable by the given {@link JobPath JobPath}.
   * 
   * @param path
   *   The path from the focus job to the current job.
   */ 
  public synchronized ViewerJobGroup
  getActiveViewerJobGroup
  (
   JobPath path  
  ) 
  {
    return pActive.get(path);
  }

  /** 
   * Get all of the active viewer jobs.
   */ 
  public synchronized Collection<ViewerJobGroup>
  getActiveViewerJobGroups()
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
    for(ViewerJobGroup vjob : pActive.values()) 
      vjob.setVisible(false);

    /* swap job tables */ 
    pPrevious = pActive;
    pActive   = new TreeMap<Long,ViewerJobGroup>();
  }

  /**
   * Lookup an existing or create a new {@link ViewerJobGroup ViewerJobGroup} instance to 
   * represent the given {@link QueueJobGroup QueueJobGroup}.
   * 
   * @param group
   *   The current job group. 
   * 
   * @param vjobs
   *   The viewer jobs.
   */ 
  public synchronized ViewerJobGroup
  lookupOrCreateViewerJobGroup
  (
   QueueJobGroup group, 
   ArrayList<ViewerJob> vjobs
  ) 
  {
    if(group == null) 
      throw new IllegalArgumentException("The job group cannot be (null)!");

    Long groupID = group.getGroupID();

    /* look up the eprevious viewer job group (if any) */ 
    ViewerJobGroup vgroup = pPrevious.remove(groupID);
    if(vgroup == null) {
      /* grab the first available viewer job group in the reserve */ 
      if(!pReserve.isEmpty()) 
	vgroup = pReserve.pop();

      /* the reserve is empty, create a new viewer job group */ 
      if(vgroup == null) {
	vgroup = new ViewerJobGroup();
 	pRoot.addChild(vgroup.getBranchGroup());
      }
    }
    pActive.put(groupID, vgroup);

    /* update */ 
    vgroup.setCurrentState(group, vjobs); 
    
    return vgroup;
  }

  /**
   * Update the appearance of the active {@link ViewerJobGroup ViewerJobGroup} instances to 
   * reflect changes in their associated {@link QueueJobGroup QueueJobGroup}.
   */ 
  public synchronized void 
  update()
  {
    /* update and show the active job groups */ 
    for(ViewerJobGroup vgroup : pActive.values()) 
      vgroup.update(); 
    for(ViewerJobGroup vgroup : pActive.values()) 
      vgroup.setVisible(true);

    /* hide any previously active job groups which are no longer in use 
        and move them to the reserve */ 
    for(ViewerJobGroup vgroup : pPrevious.values()) 
      pReserve.push(vgroup);

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
   * The table of currently active viewer jobs indexed by job group ID.
   */ 
  private TreeMap<Long,ViewerJobGroup>  pActive;

  /**
   * The table of previously active viewer jobs indexed by job group ID.
   */ 
  private TreeMap<Long,ViewerJobGroup>  pPrevious;

  /**
   * The reserve of inactive viewer jobs ready for reuse. 
   */ 
  private Stack<ViewerJobGroup> pReserve;

}
