// $Id: CreateEvent.java,v 1.1 2004/10/12 23:21:13 jim Exp $

package us.temerity.pipeline.task;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   C R E A T E   E V E N T                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * This event causes a new {@link Task Task} to be created. 
 */
public
class CreateEvent
  extends LogMessage
  implements TaskEvent
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  CreateEvent()
  {}

  /**
   * Construct a new event.
   * 
   * @param name
   *   The name of the new task.
   * 
   * @param supervisor
   *   The user name of the initial supervisor of the task. 
   * 
   * @param startDate
   *   The estimated date when work should begin on the task.
   * 
   * @param completionDate
   *   The estimated date when the task should be complete.
   * 
   * @param msg 
   *   A message containing the initial requirements for completion of the task.
   */ 
  public 
  CreateEvent
  (
   String name, 
   String supervisor, 
   Date startDate, 
   Date completionDate, 
   String msg
  )
  {
    super(msg);

    if(name == null) 
      throw new IllegalArgumentException
	("The name cannot be (null)!");
    pName = name;

    if(supervisor == null) 
      throw new IllegalArgumentException
	("The supervisor cannot be (null)!");
    pSupervisor = supervisor;

    if(startDate == null) 
      throw new IllegalArgumentException
	("The start date cannot be (null)!");
    if(completionDate == null) 
      throw new IllegalArgumentException
	("The completion date cannot be (null)!");
    if(startDate.compareTo(completionDate) >= 0) 
      throw new IllegalArgumentException
	("The start date (" + Dates.format(startDate) + ") must be earlier than the " + 
	 "completion date (" + Dates.format(completionDate) + ")!"); 
    pStartDate      = startDate;
    pCompletionDate = completionDate;
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
    return TaskEventType.Create;
  }

  /**
   * Get the name of the task.
   */
  public String
  getName()
  {
    return pName;
  }

  /**
   * Get the user name of the initial supervisor of the task.
   */ 
  public String
  getSupervisor() 
  {
    return pSupervisor;
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
    if(!isPrivileged) 
      throw new PipelineException
	("Only privileged users may create new tasks!");

    if(task.getState() != null)
      throw new PipelineException
	("Cannot create a task (" + task.getName() + ") which already exists!");
    
    task.setSupervisor(pSupervisor);
    task.setDates(pStartDate, pCompletionDate);
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

    encoder.encode("Name",           pName);
    encoder.encode("Supervisor",     pSupervisor);
    encoder.encode("StartDate",      pStartDate);
    encoder.encode("CompletionDate", pCompletionDate);    
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    String name = (String) decoder.decode("Name");
    if(name == null) 
      throw new GlueException("The \"Name\" was missing or (null)!");
    pName = name;

    String supervisor = (String) decoder.decode("Supervisor");
    if(supervisor == null) 
      throw new GlueException("The \"Supervisor\" was missing or (null)!");
    pSupervisor = supervisor;

    Date startDate = (Date) decoder.decode("StartDate");
    if(startDate == null) 
      throw new GlueException("The \"StartDate\" was missing or (null)!");
    pStartDate = startDate;

    Date completionDate = (Date) decoder.decode("CompletionDate");
    if(completionDate == null) 
      throw new GlueException("The \"CompletionDate\" was missing or (null)!");
    pCompletionDate = completionDate;
  }
 


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8197797602269956237L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the task.
   */ 
  private String  pName; 

  /**
   * The user name of the initial supervisor of the task.
   */ 
  private String  pSupervisor; 

  /**
   * The estimated date when work should begin on the task.
   */ 
  private Date  pStartDate; 

  /**
   * The estimated date when the task should be complete.
   */ 
  private Date  pCompletionDate; 

}
