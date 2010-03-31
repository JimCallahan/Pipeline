// $Id: QueueGetJobInfoRsp.java,v 1.5 2009/08/19 22:48:06 jim Exp $

package us.temerity.pipeline.message.queue;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;

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
   * @param infos
   *   The current status information of the jobs.
   */ 
  public
  QueueGetJobInfoRsp
  (
   TaskTimer timer, 
   TreeMap<Long, QueueJobInfo> infos
  )
  { 
    super(timer);

    if(infos == null || infos.isEmpty()) 
      throw new IllegalArgumentException("The job infos cannot be (null) or empty!"); 

    pJobInfos = new TreeMap<Long, QueueJobInfo>(infos);
    
    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "QueueMgr.getJobInfos():\n  " + getTimer());
  }

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

    if(info == null ) 
      throw new IllegalArgumentException("The job info cannot be (null)"); 

    pJobInfos = new TreeMap<Long, QueueJobInfo>();
    pJobInfos.put(info.getJobID(), info);
    
    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "QueueMgr.getJobInfos():\n  " + getTimer());
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
    Long id = pJobInfos.firstKey();
    return pJobInfos.get(id);
  }
  
  /**
   * Gets the current status information of the jobs.
   */
  public Map<Long, QueueJobInfo>
  getJobInfos() 
  {
    return Collections.unmodifiableMap(pJobInfos);
  }  
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8231272971925425642L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The current status information of the jobs.
   */ 
  private TreeMap<Long, QueueJobInfo> pJobInfos;

}
  
