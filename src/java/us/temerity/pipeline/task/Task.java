// $Id: Task.java,v 1.2 2004/10/18 04:00:00 jim Exp $

package us.temerity.pipeline.task;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   T A S K                                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A high level production task.
 */
public
class Task
  extends Named
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  Task()
  {}

  /**
   * Construct a new task.
   * 
   * @param name
   *   The name of the task.
   * 
   * @param event
   *   The task creation event. 
   */ 
  public
  Task
  (
   String name, 
   CreateEvent event
  ) 
  {
    super(name);
    
    pEvents = new TreeMap<Date,BaseEvent>();
    pEvents.put(event.getTimeStamp(), event);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Get the timestamps of when task events have occurred.
   */ 
  public Set<Date>
  getEventTimeStamps() 
  {
    return Collections.unmodifiableSet(pEvents.keySet());
  }

  /**
   * Get the task event which occurred at the given time.
   * 
   * @return 
   *   The event or <CODE>null</CODE> if no event occured at the given time.
   */
  public BaseEvent
  getEvent
  (
   Date stamp
  ) 
  {
    return pEvents.get(stamp);
  }

  /**
   * Get all of the task events in oldest to newest order.
   */
  public Collection<BaseEvent>
  getEvents() 
  {
    return Collections.unmodifiableCollection(pEvents.values());
  }

  /**
   * Get the most recent task event.
   */
  public BaseEvent
  getLatestEvent() 
  {
    return pEvents.get(pEvents.lastKey());
  }


  /**
   * Add a new event in the life of the task.
   *
   * @param event
   *   The new event.
   * 
   * @param privileged
   *   The names of the privileged users.
   * 
   * @return
   *   The updated current status of the task post event.
   * 
   * @throw PipelineException
   *   If the event is not valid for the current state of the task.
   */ 
  public TaskStatus
  addEvent
  (
   BaseEvent event, 
   TreeSet<String> privileged
  ) 
    throws PipelineException 
  {
    if(event.getTimeStamp().compareTo(pEvents.lastKey()) <= 0) 
      throw new PipelineException
	("The new task event must be newer than all previous task events!");

    TaskStatus status = new TaskStatus(this, new Date());
    event.validate(status, privileged);
    status.update(event);
      
    pEvents.put(event.getTimeStamp(), event);

    return status;
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

    encoder.encode("Events", pEvents);
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    TreeMap<Date,BaseEvent> events = 
      (TreeMap<Date,BaseEvent>) decoder.decode("Events");
    if(events == null) 
      throw new GlueException("The \"Events\" was missing or (null)!");
    pEvents = events;
  }
 


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5920563735167541690L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The events in the life of the task.
   */ 
  private TreeMap<Date,BaseEvent>  pEvents;
  
}
