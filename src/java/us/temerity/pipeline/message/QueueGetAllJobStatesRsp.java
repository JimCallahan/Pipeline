// $Id: QueueGetAllJobStatesRsp.java,v 1.1 2004/08/23 04:29:01 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   A L L   J O B   S T A T E S   R S P                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Get the JobStates of all existing jobs.
 */
public
class QueueGetAllJobStatesRsp
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
   * @param states
   *   The JobStates of each job indexed by job ID. 
   */ 
  public
  QueueGetAllJobStatesRsp
  (
   TaskTimer timer, 
   TreeMap<Long,JobState> states
  )
  { 
    super(timer);

    if(states == null) 
      throw new IllegalArgumentException("The states cannot be (null)!");
    pStates = states;

    Logs.net.finest("QueueMgr.getAllJobStates():\n  " + getTimer());
    if(Logs.net.isLoggable(Level.FINEST))
      Logs.flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Gets the JobStates of each job indexed by job ID. 
   */ 
  public TreeMap<Long,JobState>
  getStates() 
  {
    return pStates;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  //private static final long serialVersionUID = 

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The JobStates of each job indexed by job ID. 
   */ 
  private TreeMap<Long,JobState>  pStates;

}
  
