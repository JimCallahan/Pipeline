// $Id: QueueGetUserBalanceGroupRsp.java,v 1.1 2009/12/09 05:05:55 jesse Exp $

package us.temerity.pipeline.message.queue;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   U S E R   B A L A N C E   G R O U P   R S P                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Get all the current dispatch controls.  
 */
public
class QueueGetUserBalanceGroupRsp
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
   * @param group
   *   The balance group. 
   */ 
  public
  QueueGetUserBalanceGroupRsp
  (
    TaskTimer timer, 
    UserBalanceGroup group
  )
  { 
    super(timer);

    if (group == null) 
      throw new IllegalArgumentException("The user balance group cannot be (null)!");
    pUserBalanceGroup = group;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "QueueMgr.getUserBalanceGroup():\n  " + getTimer());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the user balance group. 
   */
  public UserBalanceGroup
  getUserBalanceGroup() 
  {
    return pUserBalanceGroup;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7875101965534136630L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The named user balance group. 
   */ 
  private UserBalanceGroup  pUserBalanceGroup;
}