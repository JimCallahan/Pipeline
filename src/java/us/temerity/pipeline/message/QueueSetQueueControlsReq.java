// $Id: QueueSetQueueControlsReq.java,v 1.1 2006/12/01 18:33:41 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   S E T   Q U E U E   C O N T R O L S   R E Q                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to set the queue runtime parameters.
 */
public
class QueueSetQueueControlsReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param controls
   *   The queue runtime parameters.
   */
  public
  QueueSetQueueControlsReq
  (
   QueueControls controls
  )
  {
    super();

    if(controls == null) 
      throw new IllegalArgumentException
	("The queue controls cannot be (null)!");
    pControls = controls;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the queue runtime parameters.
   */ 
  public QueueControls
  getControls() 
  {
    return pControls;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4528567638384431762L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The queue runtime parameters.
   */
  private QueueControls  pControls;  

}
  
