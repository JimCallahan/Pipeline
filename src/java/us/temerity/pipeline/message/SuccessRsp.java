// $Id: SuccessRsp.java,v 1.5 2004/03/31 08:34:56 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   S U C C E S S   R S P                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A response which signifies that the request was successfully fulfilled.
 */
public
class SuccessRsp
  extends TimedRsp
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a success response.
   * 
   * @param timer 
   *   The timing statistics for a task.
   */
  public
  SuccessRsp
  (
   TaskTimer timer
  ) 
  {
    super(timer);

    Logs.net.finest(getTimer().toString());
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  //private static final long serialVersionUID = 1872626103060304508L;

}
  
