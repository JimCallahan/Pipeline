// $Id: TimedRsp.java,v 1.1 2004/04/11 19:31:31 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   T I M E D   R S P                                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The base class for all server responses which include timining statistics.
 */
public
class TimedRsp
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a response.
   * 
   * @param timer 
   *   The timing statistics for a task.
   */
  protected
  TimedRsp
  ( 
   TaskTimer timer
  )
  { 
    if(timer == null) 
      throw new IllegalArgumentException("The timer cannot (null)!");
    pTimer = timer;

    pTimer.suspend();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the timing statistics.
   */
  public TaskTimer
  getTimer() 
  {
    return pTimer;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8742509747666843924L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The timing statistics for a task.
   */ 
  private TaskTimer  pTimer;
}
  
