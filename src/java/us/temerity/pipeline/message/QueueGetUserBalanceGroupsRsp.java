// $Id: QueueGetUserBalanceGroupsRsp.java,v 1.2 2009/12/09 05:05:55 jesse Exp $

package us.temerity.pipeline.message;

import java.util.TreeMap;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   U S E R   B A L A N C E   G R O U P S   R S P                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Get all the current dispatch controls.  
 */
public
class QueueGetUserBalanceGroupsRsp
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
   * @param groups
   *   The balance groups indexed by group name. 
   */ 
  public
  QueueGetUserBalanceGroupsRsp
  (
    TaskTimer timer, 
    TreeMap<String, UserBalanceGroup> groups
  )
  { 
    super(timer);

    if(groups == null) 
      throw new IllegalArgumentException("The user balance groups cannot be (null)!");
    pUserBalanceGroups = groups;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "QueueMgr.getUserBalanceGroups():\n  " + getTimer());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the current user balance groups indexed by group name. 
   */
  public TreeMap<String, UserBalanceGroup>
  getUserBalanceGroups() 
  {
    return pUserBalanceGroups;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8626063505873641592L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The current user balance groups indexed by group name. 
   */ 
  private TreeMap<String, UserBalanceGroup>  pUserBalanceGroups;
}