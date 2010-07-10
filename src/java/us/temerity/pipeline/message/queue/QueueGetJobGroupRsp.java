// $Id: QueueGetJobGroupRsp.java,v 1.4 2009/08/19 22:48:06 jim Exp $

package us.temerity.pipeline.message.queue;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   J O B   G R O U P   R S P                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link QueueGetJobGroupReq QueueGetJobGroupReq} request.
 */
public
class QueueGetJobGroupRsp
  extends TimedRsp
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new response.
   * 
   * @param timer 
   *   The timing statistics for a task.
   * 
   * @param group
   *   The job group. 
   */ 
  public
  QueueGetJobGroupRsp
  (
   TaskTimer timer, 
   QueueJobGroup group
  )
  { 
    super(timer);

    if(group == null) 
      throw new IllegalArgumentException("The job group cannot be (null)!");
    pJobGroups = new TreeMap<Long, QueueJobGroup>();
    pJobGroups.put(group.getGroupID(), group);

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "QueueMgr.getJobGroup():\n  " + getTimer());
  }
  
  /** 
   * Constructs a new response.
   * 
   * @param timer 
   *   The timing statistics for a task.
   * 
   * @param groups
   *   The job groups. 
   */ 
  public
  QueueGetJobGroupRsp
  (
   TaskTimer timer, 
   TreeMap<Long, QueueJobGroup> groups
  )
  { 
    super(timer);

    if(groups == null || groups.isEmpty()) 
      throw new IllegalArgumentException("The job group map cannot be (null) or empty!");
    pJobGroups = new TreeMap<Long, QueueJobGroup>(groups);

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "QueueMgr.getJobGroups():\n  " + getTimer());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get job group.
   */
  public QueueJobGroup
  getJobGroup() 
  {
    return pJobGroups.firstEntry().getValue();
  }
  
  /**
   * Get the map of job groups indexed by job group id.
   */
  public TreeMap<Long, QueueJobGroup>
  getJobGroups()
  {
    return pJobGroups;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4278766000629970363L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The job group. 
   */ 
  private TreeMap<Long, QueueJobGroup>  pJobGroups;
}
  
