// $Id: FailureRsp.java,v 1.4 2004/03/23 07:40:37 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F A I L U R E   R S P                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A response which signifies that the previous request failed for some reason.
 */
public
class FailureRsp
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a failure response without timing information.
   * 
   * @param msg 
   *   The error message explaining the failure.
   */
  public
  FailureRsp
  ( 
   String msg
  )
  { 
    if(msg == null) 
      throw new IllegalArgumentException("The failure message cannot (null)!");
    pMsg = msg;
  }

  /** 
   * Constructs a failure response during the wait on locks.
   * 
   * @param task 
   *   The name of the request task that failed to be fufilled.
   * 
   * @param msg 
   *   The error message explaining the failure.
   * 
   * @param start 
   *   The timestamp of when the wait on locks was started.
   */
  public
  FailureRsp
  (
   String task, 
   String msg,
   Date start
  )
  { 
    if(task == null) 
      throw new IllegalArgumentException("The request task cannot (null)!");
    pTask = task;

    if(msg == null) 
      throw new IllegalArgumentException("The failure message cannot (null)!");
    pMsg = msg;

    pWait = (new Date()).getTime() - start.getTime();
  
    Logs.net.finest(pTask + ": " + pWait + " (msec) wait");
  }

  /** 
   * Constructs a failure response while fufilling the request.
   * 
   * @param task 
   *   The name of the request task that failed to be fufilled.
   * 
   * @param msg 
   *   The error message explaining the failure.
   * 
   * @param wait 
   *   The number of milliseconds spent waiting to aquire the needed locks.
   * 
   * @param start 
   *   The timestamp of when the request started to be fufilled.
   */
  public
  FailureRsp
  (
   String task, 
   String msg,
   long wait, 
   Date start
  )
  { 
    if(task == null) 
      throw new IllegalArgumentException("The request task cannot (null)!");
    pTask = task;

    if(msg == null) 
      throw new IllegalArgumentException("The failure message cannot (null)!");
    pMsg = msg;

    pWait   = wait;
    pActive = (new Date()).getTime() - start.getTime();

    Logs.net.finest(pTask + ": " + pWait + "/" + pActive + " (msec) wait/active");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the name of the request task that failed to be fufilled.
   */
  public String 
  getTaskName() 
  {
    return pTask;
  }

  /**
   * Gets error message explaining the failure.
   */
  public String
  getMessage() 
  {
    return pMsg;
  }

    
  /**
   * Gets the number of milliseconds spent waiting to aquire the needed locks.
   */
  public long 
  getWaitTime() 
  {
    return pWait;
  }

  /**
   * Gets the number of milliseconds spent fufilling the request before failure.
   */
  public long
  getActiveTime() 
  {
    return pActive;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7351749431473465787L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*
   * The name of the request task that failed to be fufilled.
   */ 
  private String  pTask;

  /**
   * The error message explaining the failure.
   */ 
  private String  pMsg;


  /*
   * The number of milliseconds spent waiting to aquire the needed locks.
   */ 
  private long  pWait;

  /**
   * The number of milliseconds spent fufilling the request before failure.
   */ 
  private long  pActive; 
}
  
