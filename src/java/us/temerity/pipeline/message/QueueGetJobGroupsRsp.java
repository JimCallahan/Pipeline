// $Id: QueueGetJobGroupsRsp.java,v 1.3 2005/01/22 06:10:10 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   J O B   G R O U P S   R S P                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Get all of the existing the job groups.
 */
public
class QueueGetJobGroupsRsp
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
   * @param groups
   *   The job groups indexed by group ID. 
   */ 
  public
  QueueGetJobGroupsRsp
  (
   TaskTimer timer, 
   TreeMap<Long,QueueJobGroup> groups
  )
  { 
    super(timer);

    if(groups == null) 
      throw new IllegalArgumentException("The job groups cannot be (null)!");
    pJobGroups = groups;

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "QueueMgr.getJobGroups():\n  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets all of the existing job groups indexed by group ID. 
   */
  public TreeMap<Long,QueueJobGroup>
  getJobGroups() 
  {
    return pJobGroups;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6834532344201771352L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * All of the existing job groups indexed by group ID. 
   */ 
  private TreeMap<Long,QueueJobGroup>  pJobGroups;

}
  
