// $Id: TaskTimer.java,v 1.1 2004/03/31 08:34:03 jim Exp $

package us.temerity.pipeline.core;

import java.io.*; 
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   T A S K   T I M E R                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A collector of timing statistics for a task.
 */
public
class TaskTimer
  implements Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new timer without a title and start a new active timing interval.
   */
  public
  TaskTimer() 
  {
    pStartWait   = -1;
    pStartActive = (new Date()).getTime();

    pWaitDuration   = 0;
    pActiveDuration = 0;
  }

  /**
   * Construct a new timer and start a new active timing interval.
   * 
   * @param title
   *   The title string use to identify the task.
   */
  public
  TaskTimer
  (
   String title
  ) 
  {
    if(title == null) 
      throw new IllegalArgumentException("The task title cannot (null)!");
    pTitle = title;

    pStartWait   = -1;
    pStartActive = (new Date()).getTime();

    pWaitDuration   = 0;
    pActiveDuration = 0;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the title string use to identify the task.
   */
  public String 
  getTitle() 
  {
    return pTitle;
  }
  
  /**
   * Get the total number of milliseconds waiting on the aquisition of locks.
   */
  public long
  getWaitDuration() 
  {
    return pWaitDuration;
  }

  /**
   * Get the total number of milliseconds actively completing the task.
   */
  public long
  getActiveDuration() 
  {
    return pActiveDuration;
  }

  /**
   * Get the total number of milliseconds from the start to the end of the task.
   */
  public long
  getTotalDuration() 
  {
    return (pWaitDuration + pActiveDuration);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   T I M E R   S T A T E S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Marks the start of an lock aquisition interval and the end of an active interval.
   */
  public void 
  aquire() 
  {
    assert(pStartWait == -1);
    pStartWait = (new Date()).getTime();

    assert(pStartActive > 0); 
    pActiveDuration += (new Date()).getTime() - pStartActive;
    pStartActive = -1;
  }

  /**
   * Marks the start of an active interval and the end of a lock aquisition interval.
   */
  public void 
  resume() 
  {
    assert(pStartActive == -1);
    pStartActive = (new Date()).getTime();

    assert(pStartWait > 0); 
    pWaitDuration += (new Date()).getTime() - pStartWait;
    pStartWait = -1;
  }

  /**
   * Suspend timing to complete a subtask or to mark the end of the task.
   */
  public void 
  suspend() 
  {
    if(pStartActive > 0)
      pActiveDuration += (new Date()).getTime() - pStartActive;
    pStartActive = -1;

    if(pStartWait > 0)
      pWaitDuration += (new Date()).getTime() - pStartWait;
    pStartWait = -1;
  }

  /**
   * Accumulate the timing from the subtask and start a new active interval.
   * 
   * @param timer
   *   The timer of the subtask.
   */
  public void 
  accum
  (
   TaskTimer timer
  ) 
  {
    assert(pStartWait == -1);
    assert(pStartActive == -1);

    pWaitDuration   += timer.getWaitDuration();
    pActiveDuration += timer.getActiveDuration(); 
    
    pStartActive = (new Date()).getTime();   
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O N V E R S I O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Convert the timer to a string representation.
   */
  public String
  toString() 
  {
    String stats = (pWaitDuration + "/" + pActiveDuration + " (msec) wait/active");
    if(pTitle != null) 
      return (pTitle + ":\n  " + stats);
    else 
      return stats;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  //private static final long serialVersionUID = -7351749431473465787L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The title string use to identify the task.
   */
  private String  pTitle;


  /**
   * The timestamp of the start of the most recent wait interval..
   */
  private long  pStartWait;

  /**
   * The timestamp of the start of the most recent active interval.
   */
  private long  pStartActive;


  /**
   * The total number of milliseconds waiting to aquire a lock.
   */
  private long  pWaitDuration;

  /**
   * The total number of milliseconds actively running the task.
   */
  private long  pActiveDuration;


}


