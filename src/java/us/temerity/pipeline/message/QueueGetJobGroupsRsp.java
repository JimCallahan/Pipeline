// $Id: QueueGetJobGroupsRsp.java,v 1.1 2004/08/22 22:05:43 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;
import java.util.logging.*;

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

    Logs.net.finest("QueueMgr.getJobGroups():\n  " + getTimer());
    if(Logs.net.isLoggable(Level.FINEST))
      Logs.flush();
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
  
