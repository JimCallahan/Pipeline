// $Id: CreateEvent.java,v 1.3 2004/10/18 04:00:00 jim Exp $

package us.temerity.pipeline.task;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   C R E A T E   E V E N T                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * This event sets the required initial properties of a {@link Task Task}. 
 */
public
class CreateEvent
  extends BaseEvent
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
   * @param supervisor
   *   The user name of the initial supervisor of the task. 
   * 
   * @param interval
   *   The estimated start/completion time interval of the task.
   * 
   * @param msg 
   *   A message containing the initial requirements for completion of the task.
   */ 
  public 
  CreateEvent
  (
   String supervisor, 
   TimeInterval interval, 
   String msg
  )
  {
    super(msg);

    if(supervisor == null) 
      throw new IllegalArgumentException
	("The supervisor cannot be (null)!");
    pSupervisor = supervisor;

    if(interval == null) 
      throw new IllegalArgumentException
	("The interval cannot be (null)!");
    pTimeInterval = interval;
  } 

   
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the user name of the new supervisor of the task.
   */ 
  public String
  getSupervisor() 
  {
    return pSupervisor;
  }

  /**
   * Get the new estimated start/completion time interval of the task.
   */ 
  public TimeInterval
  getTimeInterval()
  {
    return pTimeInterval;
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
    return CompletionState.Unfinished;
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
    return ActivityState.Unassigned;
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
  public boolean
  isValid
  (
   TaskStatus status, 
   TreeSet<String> privileged
  )
  {
    assert(false) : "This should never be called!";
    return false;
  }

  /**
   * Verify that kind of event is valid given the current status of the task.
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
  public void
  validate
  (
   TaskStatus status, 
   TreeSet<String> privileged
  )
    throws PipelineException
  {
    throw new PipelineException
      ("The task (" + status + ") can only be created once!");
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

    encoder.encode("Supervisor",     pSupervisor);
    encoder.encode("TimeInterval",   pTimeInterval);
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

    TimeInterval interval = (TimeInterval) decoder.decode("TimeInterval");
    if(interval == null) 
      throw new GlueException("The \"TimeInterval\" was missing or (null)!");
    pTimeInterval = interval;
  }
 


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4836641251610542123L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The user name of the initial supervisor of the task.
   */ 
  private String  pSupervisor; 

  /**
   * The estimated start/completion time interval of the task.
   */ 
  private TimeInterval  pTimeInterval; 

}
