// $Id: QueueGetJobStatusRsp.java,v 1.4 2009/08/19 22:48:06 jim Exp $

package us.temerity.pipeline.message.queue;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   J O B   S T A T U S   R S P                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a @{link QueueGetJobStatusReq QueueGetJobStatusReq} request.
 */
public
class QueueGetJobStatusRsp
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
   * @param status
   *   The JobStatus of each job indexed by job ID. 
   */ 
  public
  QueueGetJobStatusRsp
  (
   TaskTimer timer, 
   TreeMap<Long,JobStatus> status
  )
  { 
    super(timer);

    if(status == null) 
      throw new IllegalArgumentException("The status cannot be (null)!");
    pStatus = status;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "QueueMgr.getJobStatus():\n  " + getTimer());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Gets the JobStatus of each job indexed by job ID. 
   */ 
  public TreeMap<Long,JobStatus>
  getStatus() 
  {
    return pStatus;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6054747055753320836L; 

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The JobStatus of each job indexed by job ID. 
   */ 
  private TreeMap<Long,JobStatus>  pStatus;

}
  
