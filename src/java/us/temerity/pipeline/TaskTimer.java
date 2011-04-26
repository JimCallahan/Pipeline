// $Id: TaskTimer.java,v 1.3 2009/06/04 09:00:05 jim Exp $

package us.temerity.pipeline;

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
    pStartActive = System.currentTimeMillis();

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
    pTitle = title;

    pStartWait   = -1;
    pStartActive = System.currentTimeMillis();

    pWaitDuration   = 0;
    pActiveDuration = 0;
  }

  /**
   * Copy constructor. <P> 
   * 
   * The newly created copy will accumilate all currently running wait/active timers into 
   * the total durations and reset the timers.
   * 
   * @param timer
   *   The timer to copy.
   */
  public
  TaskTimer
  (
   TaskTimer timer
  ) 
  {
    if(timer == null) 
      throw new IllegalArgumentException("The timer cannot (null)!");

    pTitle = timer.getTitle();

    pWaitDuration = timer.pWaitDuration;
    if(timer.pStartWait > 0) 
      pWaitDuration += System.currentTimeMillis() - timer.pStartWait;

    pActiveDuration = timer.pActiveDuration;
    if(timer.pStartActive > 0) 
      pActiveDuration += System.currentTimeMillis() - timer.pStartActive;

    pStartWait   = -1;
    pStartActive = -1; 
  }

  /**
   * Copy constructor. <P> 
   * 
   * The newly created copy will accumilate all currently running wait/active timers into 
   * the total durations and reset the timers.
   * 
   * @param timer
   *   The timer to copy.
   *
   * @param title
   *   A replacement task title.
   */
  public
  TaskTimer
  (
   TaskTimer timer, 
   String title
  ) 
  {
    this(timer); 
    pTitle = title; 
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
   * Get the total number of milliseconds waiting on the acquisition of locks.
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
   * Marks the start of an lock acquisition interval and the end of an active interval.
   */
  public void 
  acquire() 
  {
    if(pStartWait != -1)
      throw new IllegalStateException(); 
    pStartWait = System.currentTimeMillis();

    if(pStartActive <= 0)
      throw new IllegalStateException(); 
    pActiveDuration += System.currentTimeMillis() - pStartActive;
    pStartActive = -1;
  }

  /**
   * Marks the start of an lock acquisition interval and the end of an active interval.
   * 
   * @deprecated
   *   The method was unfortuneately misspelled but is now corrected.  This method exists, 
   *   solely to allow code with the misspelled method name to continue to temporarily work.
   *   All existing code should be changed to use the correctly spelled {@link #acquire}.
   */
  @Deprecated
  public void 
  aquire() 
  {
    acquire();
  }

  /**
   * Marks the start of an active interval and the end of a lock acquisition interval.
   */
  public void 
  resume() 
  {
    if(pStartActive != -1)
      throw new IllegalStateException(); 
    pStartActive = System.currentTimeMillis();

    if(pStartWait <= 0)
      throw new IllegalStateException(); 
    pWaitDuration += System.currentTimeMillis() - pStartWait;
    pStartWait = -1;
  }

  /**
   * Suspend timing to complete a subtask or to mark the end of the task.
   */
  public void 
  suspend() 
  {
    if(pStartActive > 0)
      pActiveDuration += System.currentTimeMillis() - pStartActive;
    pStartActive = -1;

    if(pStartWait > 0)
      pWaitDuration += System.currentTimeMillis() - pStartWait;
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
    if((pStartWait != -1) || (pStartActive != -1)) 
      throw new IllegalStateException(); 

    pWaitDuration   += timer.getWaitDuration();
    pActiveDuration += timer.getActiveDuration(); 
    
    pStartActive = System.currentTimeMillis();   
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
    String stats = (pWaitDuration + "/" + pActiveDuration + " (ms) wait/active");
    if(pTitle != null) 
      return (pTitle + ":\n  " + stats);
    else 
      return stats;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S E R I A L I Z A B L E                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the serializable fields to the object stream. <P> 
   */ 
//   private void 
//   writeObject
//   (
//    java.io.ObjectOutputStream out
//   )
//     throws IOException
//   {
//     out.writeObject(pTitle);
//     out.writeObject(pStartWait);
//     out.writeObject(pStartActive);
//     out.writeObject(pWaitDuration);
//     out.writeObject(pActiveDuration);
    
//     System.out.println("WRITE: " + this);
//   }

  /**
   * Read the serializable fields from the object stream. <P> 
   */ 
//   private void 
//   readObject
//   (
//     java.io.ObjectInputStream in
//   )
//     throws IOException, ClassNotFoundException
//   {
//     pTitle          = (String) in.readObject();
//     pStartWait      = (Long) in.readObject();
//     pStartActive    = (Long) in.readObject();
//     pWaitDuration   = (Long) in.readObject();
//     pActiveDuration = (Long) in.readObject();

//     System.out.println("READ: " + this);
//   }
 


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1450470433454399228L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The title string use to identify the task.
   */
  private String  pTitle;


  /**
   * The timestamp of the start of the most recent wait interval.
   */
  private long  pStartWait;

  /**
   * The timestamp of the start of the most recent active interval.
   */
  private long  pStartActive;


  /**
   * The total number of milliseconds waiting to acquire a lock.
   */
  private long  pWaitDuration;

  /**
   * The total number of milliseconds actively running the task.
   */
  private long  pActiveDuration;


}


