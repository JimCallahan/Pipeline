// $Id: QueueGetJobGroupRsp.java,v 1.2 2005/01/22 01:36:36 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   J O B   G R O U P   R S P                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to {@link QueueGetJobGroupReq QueueGetJobGroupReq} request.
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
    pJobGroup = group;

    LogMgr.getInstance().log
(LogMgr.Kind.Net, LogMgr.Level.Finest,
"QueueMgr.getJobGroup():\n  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets job group.
   */
  public QueueJobGroup
  getJobGroup() 
  {
    return pJobGroup;
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
  private QueueJobGroup  pJobGroup;

}
  
