// $Id: SuccessRsp.java,v 1.2 2004/03/12 23:08:23 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   S U C C E S S   R S P                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A response which signifies that the previous request was successfully fulfilled.
 */
public
class SuccessRsp
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new response.
   * 
   * @param task [<B>in</B>]
   *   The name of the request task that succeeded.
   * 
   * @param wait [<B>in</B>]
   *   The number of milliseconds spent waiting to aquire the needed locks.
   * 
   * @param start [<B>in</B>]
   *   The timestamp of when the request started to be fufilled.
   */
  public
  SuccessRsp
  (
   String task, 
   long wait, 
   Date start
  ) 
  {
    if(task == null) 
      throw new IllegalArgumentException("The request task cannot (null)!");
    pTask = task;

    pWait   = wait;
    pActive = (new Date()).getTime() - start.getTime();

    Logs.net.finest(pTask + ": " + pWait + "/" + pActive + " (msec) wait/active");
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the name of the request task that succeeded.
   */
  public String 
  getTaskName() 
  {
    return pTask;
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
   * Gets the number of milliseconds spent fufilling the request.
   */
  public long
  getActiveTime() 
  {
    return pActive;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1872626103060304508L;


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*
   * The name of the request task that succeeded.
   */ 
  private String  pTask;

  /*
   * The number of milliseconds spent waiting to aquire the needed locks.
   */ 
  private long  pWait;

  /**
   * The number of milliseconds spent fufilling the request.
   */ 
  private long  pActive; 

}
  
