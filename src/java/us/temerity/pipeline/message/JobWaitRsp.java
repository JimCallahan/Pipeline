// $Id: JobWaitRsp.java,v 1.3 2005/01/16 00:38:31 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   W A I T   R S P                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link JobWaitReq JobWaitReq} request.
 */ 
public
class JobWaitRsp
  extends TimedRsp
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new response.
   *
   * @param jobID
   *   The unique job identifier.
   *
   * @param timer 
   *   The timing statistics for a task.
   * 
   * @param results
   *   The execution results.
   */ 
  public
  JobWaitRsp
  (
   long jobID,
   TaskTimer timer, 
   QueueJobResults results
  )
  { 
    super(timer);

    if(results == null) 
      throw new IllegalArgumentException
	("The job results cannot be (null)!");
    pResults = results; 

    Logs.net.finest("JobMgr.wait(): " + jobID + "\n  " + getTimer());
    if(Logs.net.isLoggable(Level.FINEST))
      Logs.flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the execution results.
   */
  public QueueJobResults
  getResults()
  {
    return pResults;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7864068873410009351L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The the execution results.
   */ 
  private QueueJobResults  pResults; 

}
  
