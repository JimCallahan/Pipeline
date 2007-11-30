// $Id: QueueGetHardwareGroupsRsp.java,v 1.1 2007/11/30 20:06:25 jesse Exp $

package us.temerity.pipeline.message;

import java.util.TreeMap;

import us.temerity.pipeline.*;

/*---------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   H A R D W A R E   G R O U P S   R S P                                 */
/*---------------------------------------------------------------------------------------------*/

/**
 * Get the current hardware values for all hardware groups.  
 */
public
class QueueGetHardwareGroupsRsp
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
   *   The hardware groups indexed by group name. 
   */ 
  public
  QueueGetHardwareGroupsRsp
  (
   TaskTimer timer, 
   TreeMap<String,HardwareGroup> groups
  )
  { 
    super(timer);

    if(groups == null) 
      throw new IllegalArgumentException("The selection groups cannot be (null)!");
    pHardwareGroups = groups;

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "QueueMgr.getHardwareGroups():\n  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the current hardware key groups indexed by group name. 
   */
  public TreeMap<String,HardwareGroup>
  getHardwareGroups() 
  {
    return pHardwareGroups;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3826899040727610751L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The current hardware key groups indexed by group name. 
   */ 
  private TreeMap<String,HardwareGroup>  pHardwareGroups;

}
  
