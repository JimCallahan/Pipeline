// $Id: QueueGetSelectionGroupsRsp.java,v 1.2 2009/08/19 22:48:06 jim Exp $

package us.temerity.pipeline.message.queue;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.*;

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

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "QueueMgr.getSelectionGroups():\n  " + getTimer());
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
  
