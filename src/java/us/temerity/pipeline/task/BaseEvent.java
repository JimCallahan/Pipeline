// $Id: BaseEvent.java,v 1.2 2004/10/30 13:42:20 jim Exp $

package us.temerity.pipeline.task;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   E V E N T                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The abstract base class of all events in the life of a {@link Task Task}.
 */
public abstract
class BaseEvent
  extends SimpleLogMessage
{     
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  protected 
  BaseEvent()
  {}

  /**
   * Construct a new event.
   * 
   * @param msg 
   *   A message associated with the event or <CODE>null</CODE> for no message.
   */ 
  protected 
  BaseEvent 
  (
   String msg  
  ) 
  {
    super(msg);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the user name of the new supervisor of the task.
   * 
   * @return
   *   The supervisor or <CODE>null</CODE> if the supervisor is unchanged.
   */ 
  public String
  getSupervisor() 
  {
    return null;
  }

  /**
   * Get the name of the new user responsible for completing the task.
   * 
   * @return
   *   The owner or <CODE>null</CODE> if the owner is unchanged.
   */
  public String
  getOwner() 
  {
    return null;
  }

  /**
   * Get the new estimated start/completion time interval of the task.
   * 
   * @return
   *   The time interval or <CODE>null</CODE> if the time interval is unchanged.
   */ 
  public TimeInterval
  getTimeInterval()
  {
    return null;
  }
  
  /**
   * Get the new set of tags values associated with the task.
   * 
   * @return
   *   The tag values or <CODE>null</CODE> if the tags are unchanged.
   */ 
  public TreeMap<String,TreeSet<String>>
  getTags() 
  {
    return null;
  }

  /**
   * Get the new constraints of this task indexed by the names of the constraining tasks.
   * 
   * @return
   *   The constraints or <CODE>null</CODE> if the constraints are unchanged.
   */ 
//   public TreeMap<String,TaskConstraint>
//   getConstraints() 
//   {
//     return null;
//   }

  /**
   * Get the new revision numbers of the nodes currently associated with the task indexed by 
   * fully resolved node name.
   * 
   * @return
   *   The node revisions or <CODE>null</CODE> if the nodes are unchanged.
   */ 
  public TreeMap<String,VersionID>
  getNodes() 
  {
    return null;
  }

  /**
   * Get the new completion state of the task.
   * 
   * @return
   *   The completion state or <CODE>null</CODE> if the state is unchanged.
   */ 
  public CompletionState
  getCompletionState() 
  {
    return null;
  }

  /**
   * Get the new activity state of the task.
   * 
   * @return
   *   The activity state or <CODE>null</CODE> if the state is unchanged.
   */ 
  public ActivityState
  getActivityState() 
  {
    return null;
  }


    
  /*----------------------------------------------------------------------------------------*/
  /*   V A L I D A T I O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Is the this event is valid given the current status of the task.
   * 
   * @param status
   *   The current task status.
   * 
   * @param privileged
   *   The names of the privileged users.
   */ 
  public abstract boolean
  isValid
  (
   TaskStatus status, 
   TreeSet<String> privileged
  );


  /**
   * Verify that this event is valid given the current status of the task.
   * 
   * @param status
   *   The current task status.
   * 
   * @param privileged
   *   The names of the privileged users.
   * 
   * @throws PipelineException
   *   If the event is not valid with a message explaining why.
   */ 
  public abstract void
  validate
  (
   TaskStatus status, 
   TreeSet<String> privileged
  )
    throws PipelineException;

}
