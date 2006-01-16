// $Id: GetUnfinishedJobsForNodeFilesRsp.java,v 1.1 2006/01/16 04:11:12 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   U N F I N I S H E D   J O B S   F O R   N O D E   F I L E S        */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to 
 * {@link GetUnfinishedJobsForNodeFilesReq GetUnfinishedJobsForNodeFilesReq} request.
 */
public
class GetUnfinishedJobsForNodeFilesRsp
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
  GetUnfinishedJobsForNodeFilesRsp
  (
   TaskTimer timer, 
   TreeSet<Long> jobIDs
  )
  { 
    super(timer);

    if(jobIDs == null) 
      throw new IllegalArgumentException("The jobIDs cannot be (null)!");
    pJobIDs = jobIDs;

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       getTimer().toString());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the unfinished job IDs indexed by node name.
   */
  public TreeSet<Long>
  getJobIDs() 
  {
    return pJobIDs;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6487370763747362954L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unfinished job IDs indexed by node name.
   */ 
  private TreeSet<Long>  pJobIDs; 

}
  
