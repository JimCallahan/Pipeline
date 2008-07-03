// $Id: QueueGetJobRsp.java,v 1.4 2008/07/03 19:50:02 jesse Exp $

package us.temerity.pipeline.message;

import java.util.*;

import us.temerity.pipeline.*;

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
   * @param jobs
   *   The list of jobs indexed by jobID.
   */ 
  public
  QueueGetJobRsp
  (
   TaskTimer timer, 
   TreeMap<Long, QueueJob> jobs
  )
  { 
    super(timer);

    if(jobs == null || jobs.isEmpty()) 
      throw new IllegalArgumentException("The jobs cannot be (null) or empty!");
    pJobs = new TreeMap<Long, QueueJob>(jobs);

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "QueueMgr.getJobs():\n  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }
  
  /** 
   * Constructs a new response.
   * 
   * @param timer 
   *   The timing statistics for a task.
   * 
   * @param job
   *   The queue job.
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
    pJobs = new TreeMap<Long, QueueJob>();
    pJobs.put(job.getJobID(), job);

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "QueueMgr.getJob():\n  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the first queue job. 
   */
  public QueueJob
  getJob() 
  {
    Long id = pJobs.firstKey();
    return pJobs.get(id);
  }
  
  /**
   * Gets the queue jobs. 
   */
  public Map<Long, QueueJob>
  getJobs() 
  {
    return Collections.unmodifiableMap(pJobs);
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
  private TreeMap<Long, QueueJob>  pJobs;

}
  
