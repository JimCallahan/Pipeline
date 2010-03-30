// $Id: QueueGetBalanceGroupUsageRsp.java,v 1.1 2009/12/09 05:05:55 jesse Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   B A L A N C E   G R O U P   U S A G E   R S P                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Get the current user usage per balance group.  
 */
public
class QueueGetBalanceGroupUsageRsp
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
   * @param usage
   *   The current balance group usage. 
   */ 
  public
  QueueGetBalanceGroupUsageRsp
  (
    TaskTimer timer, 
    DoubleMap<String, String, Double> usage
  )
  { 
    super(timer);

    if (usage == null) 
      throw new IllegalArgumentException("The balance group usage cannot be (null)!");
    pBalanceGroupUsage = usage;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "QueueMgr.getBalanceGroupUsage():\n  " + getTimer());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current balance group usage. 
   */
  public DoubleMap<String, String, Double>
  getBalanceGroupUsage() 
  {
    return pBalanceGroupUsage;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5939158510513221508L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The current user balance group usage. 
   */ 
  private DoubleMap<String, String, Double> pBalanceGroupUsage;
}