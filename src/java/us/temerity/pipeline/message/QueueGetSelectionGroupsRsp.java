// $Id: QueueGetSelectionGroupsRsp.java,v 1.1 2005/12/31 20:42:59 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*-----------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   S E L E C T I O N   G R O U P S   R S P                                 */
/*-----------------------------------------------------------------------------------------------*/

/**
 * Get the current selection biases for all selection groups.  
 */
public
class QueueGetSelectionGroupsRsp
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
   *   The selection groups indexed by group name. 
   */ 
  public
  QueueGetSelectionGroupsRsp
  (
   TaskTimer timer, 
   TreeMap<String,SelectionGroup> groups
  )
  { 
    super(timer);

    if(groups == null) 
      throw new IllegalArgumentException("The selection groups cannot be (null)!");
    pSelectionGroups = groups;

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "QueueMgr.getSelectionGroups():\n  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the current selection bias groups indexed by group name. 
   */
  public TreeMap<String,SelectionGroup>
  getSelectionGroups() 
  {
    return pSelectionGroups;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5714712168416594430L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The current selection bias groups indexed by group name. 
   */ 
  private TreeMap<String,SelectionGroup>  pSelectionGroups;

}
  
