// $Id: TaskEvent.java,v 1.1 2004/10/12 23:21:13 jim Exp $

package us.temerity.pipeline.task;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   T A S K   E V E N T                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * An event in the life of a {@link Task Task}.
 */
public
interface TaskEvent
  extends Glueable
{     
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the task event class type.
   */ 
  public TaskEventType
  getEventType();    


  /**
   * Get when the event was generated. 
   */ 
  public Date
  getTimeStamp();

  /**
   * Get the name of the user who generated the event.
   */ 
  public String
  getAuthor();

  /**
   * Get the event message text. 
   * 
   * @return 
   *   The message or <CODE>null</CODE> if there is no event message.
   */ 
  public String
  getMessage();
  

   
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
    throws PipelineException;
     
}
