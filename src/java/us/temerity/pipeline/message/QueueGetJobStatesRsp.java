// $Id: QueueGetJobStatesRsp.java,v 1.1 2004/08/22 22:05:43 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   J O B   S T A T E S   R S P                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to {@link QueueGetJobStatesReq QueueGetJobStatesReq} request.
 */
public
class QueueGetJobStatesRsp
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
   * @param id
   *   The unique working version identifier.
   * 
   * @param jobIDs
   *   The unique job identifiers of the latest job which regenerates each file of the 
   *   primary file sequence.
   * 
   * @param states
   *   The JobState of each file of the primary file sequence.
   */ 
  public
  QueueGetJobStatesRsp
  (
   TaskTimer timer, 
   NodeID id, 
   ArrayList<Long> jobIDs, 
   ArrayList<JobState> states    
  )
  { 
    super(timer);

    if(jobIDs == null) 
      throw new IllegalArgumentException("The jobIDs cannot be (null)!");
    pJobIDs = jobIDs;

    if(states == null) 
      throw new IllegalArgumentException("The states cannot be (null)!");
    pStates = states;

    Logs.net.finest("QueueMgr.getJobStates(): " + id + "\n  " + getTimer());
    if(Logs.net.isLoggable(Level.FINEST))
      Logs.flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the unique job identifiers of the latest job which regenerates each file of the 
   * primary file sequence.
   */
  public ArrayList<Long>
  getJobIDs() 
  {
    return pJobIDs;
  }
  
  /**
   * Gets the JobState of each file of the primary file sequence.
   */ 
  public ArrayList<JobState>
  getStates() 
  {
    return pStates;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3933711769120280622L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique job identifiers of the latest job which regenerates each file of the 
   * primary file sequence.
   */ 
  private ArrayList<Long>  pJobIDs; 

  /**
   * The JobState of each file of the primary file sequence.
   */ 
  private ArrayList<JobState>  pStates;

}
  
