// $Id: QueueGetSelectionSchedulesRsp.java,v 1.1 2005/12/31 20:42:59 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   S E L E C T I O N   S C H E D U L E S   R S P                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Get the existing selection schedules.  
 */
public
class QueueGetSelectionSchedulesRsp
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
   * @param schedules
   *   The selection schedules indexed by schedule name. 
   */ 
  public
  QueueGetSelectionSchedulesRsp
  (
   TaskTimer timer, 
   TreeMap<String,SelectionSchedule> schedules
  )
  { 
    super(timer);

    if(schedules == null) 
      throw new IllegalArgumentException("The selection schedules cannot be (null)!");
    pSelectionSchedules = schedules;

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "QueueMgr.getSelectionSchedules():\n  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the current selection bias schedules indexed by schedule name. 
   */
  public TreeMap<String,SelectionSchedule>
  getSelectionSchedules() 
  {
    return pSelectionSchedules;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3039342408784333331L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The current selection bias schedules indexed by schedule name. 
   */ 
  private TreeMap<String,SelectionSchedule>  pSelectionSchedules;

}
  
