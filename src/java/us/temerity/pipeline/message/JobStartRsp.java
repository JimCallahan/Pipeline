// $Id: JobStartRsp.java,v 1.1 2004/08/23 03:07:10 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   S T A R T   R S P                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link JobStartReq JobStartReq} request.
 */ 
public
class JobStartRsp
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
   * @param jobs
   *   The number of currently running jobs.
   */ 
  public
  JobStartRsp
  (
   long jobID, 
   TaskTimer timer, 
   int jobs
  )
  { 
    super(timer);

    pNumJobs = jobs;

    Logs.net.finest("JobMgr.start(): " + jobID + "\n  " + getTimer());
    if(Logs.net.isLoggable(Level.FINEST))
      Logs.flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the number of jobs running on the host.
   */ 
  public int
  getNumJobs() 
  {
    return pNumJobs; 
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 672080887635869383L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The number of currently running jobs.
   */ 
  private int  pNumJobs; 

}
  
