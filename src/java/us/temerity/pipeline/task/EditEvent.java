// $Id: EditEvent.java,v 1.1 2004/10/18 04:00:00 jim Exp $

package us.temerity.pipeline.task;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   E D I T   E V E N T                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * This 
 */
public
class EditEvent
  extends BaseEvent
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  EditEvent()
  {}

  /**
   * Construct a new event.
   * 
   * @param supervisor
   *   The user name of the new supervisor of the task or <CODE>null</CODE> to leave
   *   unchanged.
   * 
   * @param owner
   *   The user name of the new owner of the task or <CODE>null</CODE> to leave
   *   unchanged.
   * 
   * @param interval
   *   The estimated start/completion time interval of the task or <CODE>null</CODE> to 
   *   leave unchanged.
   * 
   * @param tags
   *   The new set of tags values associated with the task. or <CODE>null</CODE> to 
   *   leave unchanged.
   * 
   * @param msg 
   *   A message containing the initial requirements for completion of the task.
   */ 
  public 
  EditEvent
  (
   String supervisor, 
   String owner, 
   TimeInterval interval, 
   TreeMap<String,TreeSet<String>> tags,
   
   String msg
  )
  {
    super(msg);

    if(supervisor != null) 
      pSupervisor = supervisor;

    if(owner != null) 
      pOwner = owner;

    if(interval != null) 
      pTimeInterval = interval;

    if(tags != null) 
      pTags = tags;

//     if(constraints != null) 
//       pConstraints = constraints;
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
    return pSupervisor;
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
    return pTimeInterval;
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
    return pTags;
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
//     return pConstraints; 
//   }


    
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
    return (getAuthor().equals(status.getSupervisor()) ||
	    privileged.contains(getAuthor()));
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
    if(!getAuthor().equals(status.getSupervisor()) &&
       !privileged.contains(getAuthor()))
      throw new PipelineException 
	("The user (" + getAuthor() + ") is not authorized to edit the properties of " + 
	 "task (" + status + ")!");
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
    
    if(pSupervisor != null) 
      encoder.encode("Supervisor", pSupervisor);

    if(pOwner != null) 
      encoder.encode("Owner", pOwner);

    if(pTimeInterval != null) 
      encoder.encode("TimeInterval", pTimeInterval);

    if(pTags != null) 
      encoder.encode("Tags", pTags);

//     if(pConstraints != null) 
//       encoder.encode("Constraints", pConstraints);
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
    if(supervisor != null) 
      pSupervisor = supervisor;

    String owner = (String) decoder.decode("Owner");
    if(owner != null) 
      pOwner = owner;

    TimeInterval interval = (TimeInterval) decoder.decode("TimeInterval");
    if(interval != null) 
      pTimeInterval = interval;
    
    TreeMap<String,TreeSet<String>> tags = 
      (TreeMap<String,TreeSet<String>>) decoder.decode("Tags");
    if(tags != null) 
      pTags = tags;

//     TreeMap<String,TaskConstraint> constraints = 
//       (TreeMap<String,TaskConstraint>) decoder.decode("Constraints");
//     if(constraints != null) 
//       pConstraints = constraints;
  }
 


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6040565923887133284L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The user name of the initial supervisor of the task.
   */ 
  private String  pSupervisor; 

  /**
   * The name of the current user responsible for completing the task.
   */ 
  private String  pOwner; 

  /**
   * The estimated start/completion time interval of the task.
   */ 
  private TimeInterval  pTimeInterval; 

  /**
   * The currently selected tag values associated with the task indexed by tag name.
   */ 
  private TreeMap<String,TreeSet<String>>  pTags;

  /**
   * The current constraints of this task indexed by the name of the constraining tasks.
   */ 
  //private TreeMap<String,TaskConstraint>  pConstraints; 

}
