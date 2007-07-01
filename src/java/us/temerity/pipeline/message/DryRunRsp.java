// $Id: DryRunRsp.java,v 1.1 2007/07/01 23:54:23 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   D R Y   R U N   R S P                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A response containing details of what might have been performed if an operation was 
 * actually executed.
 */
public
class DryRunRsp
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
   * 
   * @param msg
   *   A text message detailing how the operation would have been performed.
   */
  public
  DryRunRsp
  (
   TaskTimer timer, 
   String msg
  ) 
  {
    this(timer, timer.toString(), msg);
  }

  /** 
   * Constructs a success response.
   * 
   * @param timer 
   *   The timing statistics for a task.
   * 
   * @param logMsg
   *   The message to print for the "net:finest" logger.
   * 
   * @param msg
   *   A text message detailing how the operation would have been performed.
   */
  public
  DryRunRsp
  (
   TaskTimer timer, 
   String logMsg, 
   String msg
  ) 
  {
    super(timer);

    pMessage = msg;

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       logMsg); 
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the text message detailing how the operation would have been performed.
   */ 
  public String
  getMessage() 
  {
    return pMessage;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8954929739458752168L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A text message detailing how the operation would have been performed.
   */ 
  private String  pMessage; 

}
  
