// $Id: ManageEvent.java,v 1.2 2004/10/13 03:23:56 jim Exp $

package us.temerity.pipeline.task;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M A N A G E   E V E N T                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * This event causes a {@link Task Task} to be manageed to an artist which will be
 * responsible for completing the task.
 */
public
class ManageEvent
  extends LogMessage
  implements TaskEvent
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  ManageEvent()
  {}

  /**
   * Construct a new event.
   * 
   * @param supervisor
   *   The user name of the new supervisor of the task.
   * 
   * @param msg 
   *   An message containing the reason for the change of supervisor or <CODE>null</CODE> 
   *   for no message.
   */ 
  public 
  ManageEvent
  (
   String supervisor,
   String msg
  )
  {
    super(msg);

    if(supervisor == null) 
      throw new IllegalArgumentException
	("The supervisor cannot be (null)!");
    pSupervisor = supervisor;
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
    return TaskEventType.Manage;
  }

  /**
   * Get the user name of the new supervisor of the task.
   */ 
  public String
  getSupervisor() 
  {
    return pSupervisor;
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
	("Only privileged users may change the supervisor of the task (" + task + ")!");
    
    task.setSupervisor(pSupervisor);
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

    encoder.encode("Supervisor", pSupervisor);
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    String supervisor = (String) decoder.decode("Supervisor");
    if(supervisor == null) 
      throw new GlueException("The \"Supervisor\" was missing or (null)!");
    pSupervisor = supervisor;
  }
 


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5832548208127748394L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The user name of the new supervisor of the task.
   */ 
  private String  pSupervisor; 
}
