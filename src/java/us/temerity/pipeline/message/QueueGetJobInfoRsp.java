// $Id: QueueGetJobInfoRsp.java,v 1.2 2005/01/22 01:36:36 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   J O B   I N F O   R S P                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to {@link QueueGetJobInfoReq QueueGetJobInfoReq} request.
 */
public
class QueueGetJobInfoRsp
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
   * @param info
   *   The current status information of a job.
   */ 
  public
  QueueGetJobInfoRsp
  (
   TaskTimer timer, 
   QueueJobInfo info
  )
  { 
    super(timer);

    if(info == null) 
      throw new IllegalArgumentException("The job info cannot be (null)!");
    pJobInfo = info;

    LogMgr.getInstance().log
(LogMgr.Kind.Net, LogMgr.Level.Finest,
"QueueMgr.getJobInfo():\n  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the current status information of a job.
   */
  public QueueJobInfo
  getJobInfo() 
  {
    return pJobInfo;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8231272971925425642L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The current status information of a job.
   */ 
  private QueueJobInfo  pJobInfo;

}
  
