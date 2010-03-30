// $Id: QueueGetQueueControlsRsp.java,v 1.1 2006/12/01 18:33:41 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   Q U E U E   C O N T R O L S   R S P                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Get the current logging levels.
 */
public
class QueueGetQueueControlsRsp
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
   * @param controls
   *   The current queue runtime parameters.
   */ 
  public
  QueueGetQueueControlsRsp
  (
   TaskTimer timer, 
   QueueControls controls
  )
  { 
    super(timer);

    if(controls == null) 
      throw new IllegalArgumentException("The queue controls cannot be (null)!");
    pControls = controls;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the current queue runtime parameters.
   */
  public QueueControls
  getControls()
  {
    return pControls; 
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7464764781422027051L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The current queue runtime parameters.
   */ 
  private QueueControls  pControls; 

}
  
