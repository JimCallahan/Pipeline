// $Id: QueueGetJobStateDistributionRsp.java,v 1.2 2009/08/19 22:48:06 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   J O B   S T A T E   D I S T R I B U T I O N   R S P                */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a @{link QueueGetJobStateDistributionReq} request.
 */
public
class QueueGetJobStateDistributionRsp
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
   * @param dist
   *   The distribution of job states for the jobs associated with each job group.
   */ 
  public
  QueueGetJobStateDistributionRsp
  (
   TaskTimer timer, 
   TreeMap<Long,double[]> dist
  )
  { 
    super(timer);

    if(dist == null) 
      throw new IllegalArgumentException("The state distribution be (null)!");
    pStateDistribution = dist;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "QueueMgr.getJobStateDistribution():\n  " + getTimer());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Gets the distribution of job states for the jobs associated with each job group.
   */ 
  public TreeMap<Long,double[]>
  getStateDistribution() 
  {
    return pStateDistribution;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2553535244598796766L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The distribution of job states for the jobs associated with each job group. 
   */ 
  private TreeMap<Long,double[]>  pStateDistribution;

}
  
