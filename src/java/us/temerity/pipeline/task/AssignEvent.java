// $Id: AssignEvent.java,v 1.1 2004/10/12 23:21:13 jim Exp $

package us.temerity.pipeline.task;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   A S S I G N   E V E N T                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * This event causes a {@link Task Task} to be assigned to an artist which will be
 * responsible for completing the task.
 */
public
class AssignEvent
  extends LogMessage
  implements TaskEvent
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  AssignEvent()
  {}

  /**
   * Construct a new event.
   * 
   * @param owner
   *   The name of the user responsible for completing the task or <CODE>null</CODE>
   *   for no owner.
   * 
   * @param msg 
   *   A message containing the reason for the assignment and any new direction for 
   *   completing the task or <CODE>null</CODE> for no message.
   */ 
  public 
  AssignEvent
  (
   String owner, 
   String msg
  )
  {
    super(msg);

    pOwner = owner;
  } 

   
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the task event class type.
   */ 
  public TaskEventType
  getEventType()
  {
    return TaskEventType.Assign;
  }

  /**
   * Get the name of the user responsible for completing the task.
   */ 
  public String
  getOwner() 
  {
    return pOwner;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   E V E N T   A P P L I C A T I O N                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Modifies the given task by applying the changes represented by this event.
   * 
   * @param task
   *   The target task of the event.
   * 
   * @param isPrivileged
   *   Whether the current user is privileged.
   * 
   * @throws PipelineException
   *   If the author of the event does not have permission to apply this event to the task
   *   or the current task state does not allow this kind of event.
   */ 
  public void
  modifyTask
  (
   Task task,
   boolean isPrivileged
  )
    throws PipelineException
  {
    if(!isPrivileged && !task.getSupervisor().equals(getAuthor())) 
      throw new PipelineException
	("Only privileged users or the task supervisor (" + task.getSupervisor() + ") " + 
	 "may reassign the task (" + task + ")!");
    
    task.setOwner(pOwner);
    task.setState(TaskState.Inactive);
  }
     

  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public void 
  toGlue
  ( 
   GlueEncoder encoder  
  ) 
    throws GlueException
  {
    super.toGlue(encoder);

    encoder.encode("Owner", pOwner);
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    pOwner = (String) decoder.decode("Owner");
  }
 


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  //private static final long serialVersionUID = ;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the user responsible for completing the task.
   */ 
  private String  pOwner; 

}
