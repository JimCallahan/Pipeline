// $Id: MiscGetLogControlsRsp.java,v 1.1 2006/11/10 21:57:23 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   L O G   C O N T R O L S   R S P                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Get the current logging levels.
 */
public
class MiscGetLogControlsRsp
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
   *   The current logging levels.
   */ 
  public
  MiscGetLogControlsRsp
  (
   TaskTimer timer, 
   LogControls controls
  )
  { 
    super(timer);

    if(controls == null) 
      throw new IllegalArgumentException("The log controls cannot be (null)!");
    pControls = controls;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the current logging levels.
   */
  public LogControls
  getControls()
  {
    return pControls; 
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8530711237578427747L; 

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The current logging levels.
   */ 
  private LogControls  pControls; 

}
  
