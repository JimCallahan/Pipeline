// $Id: FailureRsp.java,v 1.9 2005/01/22 06:10:09 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

/*------------------------------------------------------------------------------------------*/
/*   F A I L U R E   R S P                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A response which signifies that the request failed for some reason.
 */
public
class FailureRsp
  extends TimedRsp
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a failure response.
   * 
   * @param timer 
   *   The timing statistics for a task.
   * 
   * @param msg 
   *   The error message explaining the failure.
   */
  public
  FailureRsp
  (
   TaskTimer timer, 
   String msg
  )
  { 
    super(timer);

    if(msg == null) 
      throw new IllegalArgumentException("The failure message cannot (null)!");
    pMsg = msg;
  
    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       getTimer().toString());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets error message explaining the failure.
   */
  public String
  getMessage() 
  {
    return pMsg;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4477761893835848346L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The error message explaining the failure.
   */ 
  private String  pMsg;

}
  
