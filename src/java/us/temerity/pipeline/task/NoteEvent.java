// $Id: NoteEvent.java,v 1.1 2004/10/18 04:00:00 jim Exp $

package us.temerity.pipeline.task;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O T E   E V E N T                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A general message containing information regarding the {@link Task Task} which should be 
 * brought to the attention of the Owner and/or Supervisor of the Task.
 */
public
class NoteEvent
  extends BaseEvent
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  NoteEvent()
  {}

  /**
   * Construct a new event.
   * 
   * @param msg 
   *   The message text.
   */ 
  public 
  NoteEvent
  (
   String msg
  )
  {
    super(msg);

    if(msg == null) 
      throw new IllegalArgumentException
	("The message cannot be (null)!");
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
    return true;
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
  {}
  


  
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

  private static final long serialVersionUID = -7127420979284535256L;



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
