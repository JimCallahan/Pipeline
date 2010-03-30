// $Id: MiscSetMasterControlsReq.java,v 1.1 2006/12/01 18:33:41 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   S E T   M A S T E R   C O N T R O L S   R E Q                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to set the master runtime parameters.
 */
public
class MiscSetMasterControlsReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param controls
   *   The master runtime parameters.
   */
  public
  MiscSetMasterControlsReq
  (
   MasterControls controls
  )
  {
    super();

    if(controls == null) 
      throw new IllegalArgumentException
	("The master controls cannot be (null)!");
    pControls = controls;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the master runtime parameters.
   */ 
  public MasterControls
  getControls() 
  {
    return pControls;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4860482077423167714L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The master runtime parameters.
   */
  private MasterControls  pControls;  

}
  
