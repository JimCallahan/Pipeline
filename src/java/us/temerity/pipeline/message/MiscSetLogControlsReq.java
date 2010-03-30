// $Id: MiscSetLogControlsReq.java,v 1.1 2006/11/10 21:57:23 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   S E T   L O G   C O N T R O L S   R E Q                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to 
 */
public
class MiscSetLogControlsReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param controls
   *   The work controls.
   */
  public
  MiscSetLogControlsReq
  (
   LogControls controls
  )
  {
    super();

    if(controls == null) 
      throw new IllegalArgumentException
	("The log controls cannot be (null)!");
    pControls = controls;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the 
   */ 
  public LogControls
  getControls() 
  {
    return pControls;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7693887251557544633L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The 
   */
  private LogControls  pControls;  

}
  
