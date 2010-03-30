// $Id: MiscGetMasterControlsRsp.java,v 1.1 2006/12/01 18:33:41 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   M A S T E R   C O N T R O L S   R S P                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Get the current logging levels.
 */
public
class MiscGetMasterControlsRsp
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
   *   The current master runtime parameters.
   */ 
  public
  MiscGetMasterControlsRsp
  (
   TaskTimer timer, 
   MasterControls controls
  )
  { 
    super(timer);

    if(controls == null) 
      throw new IllegalArgumentException("The master controls cannot be (null)!");
    pControls = controls;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the current master runtime parameters.
   */
  public MasterControls
  getControls()
  {
    return pControls; 
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4541243241801379477L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The current master runtime parameters.
   */ 
  private MasterControls  pControls; 

}
  
