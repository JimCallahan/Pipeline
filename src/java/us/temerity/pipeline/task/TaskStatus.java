// $Id: TaskStatus.java,v 1.1 2004/10/12 23:21:13 jim Exp $

package us.temerity.pipeline.task;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   T A S K   S T A T U S                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * An abreviated summary of the current status of a task. 
 */
public
class TaskStatus
  implements Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new task status.
   * 
   * @param task
   *   The complete task. 
   */ 
  public
  TaskStatus
  (
   Task task
  ) 
  {
    pName           = task.getName();
    pStartDate      = (Date) task.getStartDate().clone();
    pCompletionDate = (Date) task.getCompletionDate().clone();

    int size = task.getEventTimeStamps().size();
    pTimeStamps = new Date[size];
    pEventTypes = new TaskEventType[size];
    
    int wk = 0;
    for(Date stamp : task.getEventTimeStamps()) {
      pTimeStamps[wk] = stamp;
      pEventTypes[wk] = task.getEvent(stamp).getEventType();
      wk++;
    }

    pState = task.getState();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the task.
   */ 
  public String
  getName()
  {
    return pName;
  }


  /**
   * Get the estimated date when work should begin.
   */ 
  public Date
  getStartDate() 
  {
    return pStartDate;
  }

  /**
   * Get the estimated date when all work should be complete.
   */ 
  public Date
  getCompletionDate() 
  {
    return pCompletionDate;
  }


  /**
   * Get the timestamps of when the the task events occurred.
   */ 
  public Date[]
  getEventTimeStamps() 
  {
    return pTimeStamps;
  }

  /**
   * Get the types of the task events.
   */ 
  public TaskEventType[]
  getEventTypes() 
  {
    return pEventTypes; 
  }


  /**
   * Get the current task state.
   */ 
  public TaskState 
  getState() 
  {
    return pState; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O N V E R S I O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The string representation of the task.
   */ 
  public String
  toString() 
  {
    return pName;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5695488273757747308L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the task.
   */ 
  private String  pName; 


  /**
   * The estimated date when work should begin.
   */ 
  private Date  pStartDate; 

  /**
   * The estimated date when all work should be complete.
   */ 
  private Date  pCompletionDate; 


  /**
   * The timestamps of when the the task events occurred.
   */ 
  private Date[]  pTimeStamps;
    
  /**
   * The types of the task events.
   */ 
  private TaskEventType[]  pEventTypes; 


  /**
   * The current task state.
   */ 
  private TaskState  pState; 

}
