// $Id: GetUnfinishedJobsForNodesRsp.java,v 1.3 2009/12/14 03:20:56 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   G E T   J O B   S T A T E S   R S P                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to 
 * {@link GetUnfinishedJobsForNodesReq GetUnfinishedJobsForNodesReq} request.
 */
public
class GetUnfinishedJobsForNodesRsp
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
   * @param jobIDs
   *   The unfinished job IDs indexed by node name.
   */ 
  public
  GetUnfinishedJobsForNodesRsp
  (
   TaskTimer timer, 
   MappedSet<String,Long> jobIDs
  )
  { 
    super(timer);

    if(jobIDs == null) 
      throw new IllegalArgumentException("The jobIDs cannot be (null)!");
    pJobIDs = jobIDs;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       getTimer().toString());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the unfinished job IDs indexed by node name.
   */
  public MappedSet<String,Long>
  getJobIDs() 
  {
    return pJobIDs;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6678303316566091754L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unfinished job IDs indexed by node name.
   */ 
  private MappedSet<String,Long> pJobIDs; 

}
  
