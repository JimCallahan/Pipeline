// $Id: QueueGetRunningJobInfoRsp.java,v 1.3 2005/01/22 06:10:10 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   R U N N I N G   J O B   I N F O   R S P                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Get information about the currently running jobs. <P> 
 */
public
class QueueGetRunningJobInfoRsp
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
   * @param jobInfo
   *   The information about running jobs indexed by job ID.
   */ 
  public
  QueueGetRunningJobInfoRsp
  (
   TaskTimer timer, 
   TreeMap<Long,QueueJobInfo> jobInfo
  )
  { 
    super(timer);

    if(jobInfo == null) 
      throw new IllegalArgumentException("The job info cannot be (null)!");
    pJobInfo = jobInfo;

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "QueueMgr.getRunningJobInfo():\n  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the information about running jobs indexed by job ID.
   */
  public TreeMap<Long,QueueJobInfo>
  getJobInfo() 
  {
    return pJobInfo;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5539202938133328825L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The information about running jobs indexed by fully resolved hostname and job ID.
   */ 
  private TreeMap<Long,QueueJobInfo>  pJobInfo;

}
  
