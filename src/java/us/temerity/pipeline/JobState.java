// $Id: JobState.java,v 1.2 2004/08/30 01:30:27 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   S T A T E                                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The current status of a job in the Pipeline queue. <P> 
 * 
 * @see QueueState
 */
public
enum JobState
{  
  /**
   * The queue job has been submitted, but has not started running. 
   */
  Queued,
  
  /**
   * The queue job has been submitted, but was paused by the user before if began execution.
   */
  Paused,

  /**
   * The queue job was aborted (cancelled) by the user before it began execution.
   */
  Aborted,

  /**
   * The queue job is currently running on one of the job servers. 
   */
  Running,

  /**
   * The queue job has completed successfully.  
   */
  Finished,

  /**
   * The queue job failed to execute successfully. 
   */
  Failed;



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of all possible states.
   */ 
  public static ArrayList<JobState>
  all() 
  {
    JobState values[] = values();
    ArrayList<JobState> all = new ArrayList<JobState>(values.length);
    int wk;
    for(wk=0; wk<values.length; wk++)
      all.add(values[wk]);
    return all;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   C O N V E R S I O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Convert to a more human friendly string representation.
   */ 
  public String
  toTitle() 
  {
    return toString();
  }

}
