// $Id: NodeSubmitJobsRsp.java,v 1.6 2009/08/19 22:48:06 jim Exp $

package us.temerity.pipeline.message.node;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   S U B M I T   J O B S   R S P                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link NodeSubmitJobsReq NodeSubmitJobsReq} request.
 */
public
class NodeSubmitJobsRsp
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
   *   The list of newly submitted job groups.
   */
  public
  NodeSubmitJobsRsp
  (
   TaskTimer timer, 
   LinkedList<QueueJobGroup> groups
  )
  { 
    super(timer);

    if(groups == null) 
      throw new IllegalArgumentException("The job groups cannot be (null)!");
    pJobGroups = groups;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.submitJobs():\n  " + getTimer());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the list of newly submitted job groups.
   */
  public LinkedList<QueueJobGroup> 
  getJobGroups()
  {
    return pJobGroups;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3487681805656431925L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The list of newly submitted job groups.
   */ 
  private LinkedList<QueueJobGroup> pJobGroups; 

}
  
