// $Id: QueueGetJobRsp.java,v 1.1 2004/08/22 22:05:43 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   J O B   R S P                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to {@link QueueGetJobReq QueueGetJobReq} request.
 */
public
class QueueGetJobRsp
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
  QueueGetJobRsp
  (
   TaskTimer timer, 
   QueueJob job
  )
  { 
    super(timer);

    if(job == null) 
      throw new IllegalArgumentException("The job cannot be (null)!");
    pJob = job;

    Logs.net.finest("QueueMgr.getJob():\n  " + getTimer());
    if(Logs.net.isLoggable(Level.FINEST))
      Logs.flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the queue job. 
   */
  public QueueJob
  getJob() 
  {
    return pJob;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 825707884039959915L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The queue job. 
   */ 
  private QueueJob  pJob;

}
  
