// $Id: SuccessRsp.java,v 1.8 2005/01/22 01:36:36 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;
import java.util.logging.*;

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

    LogMgr.getInstance().log
(LogMgr.Kind.Net, LogMgr.Level.Finest,
getTimer().toString()); 
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6183008361077247309L;

}
  
