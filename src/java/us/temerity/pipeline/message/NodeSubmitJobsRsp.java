// $Id: NodeSubmitJobsRsp.java,v 1.1 2004/08/04 01:43:45 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;
import java.util.logging.*;

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
   * @param group
   *   The submitted job group.
   */
  public
  NodeSubmitJobsRsp
  (
   TaskTimer timer, 
   QueueJobGroup group
  )
  { 
    super(timer);

    if(group == null) 
      throw new IllegalArgumentException("The job group cannot be (null)!");
    pJobGroup = group;

    Logs.net.finest("MasterMgr.submitJobs(): " + group.getGroupID() + ":\n  " + getTimer());
    if(Logs.net.isLoggable(Level.FINEST))
      Logs.flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the submitted job group.
   */
  public QueueJobGroup
  getJobGroup()
  {
    return pJobGroup;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3487681805656431925L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The ubmitted job group.
   */ 
  private QueueJobGroup  pJobGroup; 

}
  
