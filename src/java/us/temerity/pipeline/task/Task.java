// $Id: Task.java,v 1.1 2004/10/12 23:21:13 jim Exp $

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
   * @param event
   *   The task creation event. 
   * 
   * @param isPrivileged
   *   Whether the current user is privileged.
   */ 
  public
  Task
  (
   CreateEvent event,
   boolean isPrivileged
  ) 
    throws PipelineException
  {
    super(event.getName());
    
    pTags        = new TreeMap<String,TreeSet<String>>();
    //pConstraints = new TreeMap<String,TaskConstraint>();
    pNodes       = new TreeMap<String,VersionID>();
    pEvents      = new TreeMap<Date,TaskEvent>();
    
    applyEvent(event, isPrivileged);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the user name of the current supervisor of the task.
   */ 
  public String
  getSupervisor() 
  {
    return pSupervisor;
  }

  /**
   * Set the user name of the current supervisor of the task.
   */ 
  public void
  setSupervisor
  (
   String supervisor
  ) 
  {
    if(supervisor == null) 
      throw new IllegalArgumentException
	("The supervisor cannot be (null)!");
    pSupervisor = supervisor;
  }


  /**
   * Get the name of the current user responsible for completing the task.
   */
  public String
  getOwner() 
  {
    return pOwner;
  }

  /**
   * Set the name of the current user responsible for completing the task.
   */
  public void 
  setOwner
  (
   String owner
  ) 
  {
    if(owner == null) 
      throw new IllegalArgumentException
	("The owner cannot be (null)!");
    pOwner = owner;
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the estimated date when work should begin.
   */ 
  public Date
  getStartDate() 
  {
    return pStartDate;
  }

  /**
   * Set the estimated date when work should begin.
   */ 
  public void
  setStartDate
  (
   Date date
  ) 
  {
    if(date == null) 
      throw new IllegalArgumentException
	("The start date cannot be (null)!");

    if(date.compareTo(pCompletionDate) >= 0) 
      throw new IllegalArgumentException
	("The start date (" + Dates.format(date) + ") must be earlier than the " + 
	 "completion date (" + Dates.format(pCompletionDate) + ")!"); 

    pStartDate = date;
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
   * Get the estimated date when all work should be complete.
   */ 
  public void
  setCompletionDate
  (
   Date date
  ) 
  {
    if(date == null) 
      throw new IllegalArgumentException
	("The completion date cannot be (null)!");

    if(pStartDate.compareTo(date) >= 0) 
      throw new IllegalArgumentException
	("The start date (" + Dates.format(pStartDate) + ") must be earlier than the " + 
	 "completion date (" + Dates.format(date) + ")!"); 

    pCompletionDate = date;
  }

  /**
   * Set the start and completion dates.
   * 
   * @param startDate
   *   The estimated date when work should begin on the task.
   * 
   * @param completionDate
   *   The estimated date when the task should be complete.
   */ 
  public void
  setDates
  (
   Date startDate, 
   Date completionDate
  ) 
  {
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
  
  /**
   * Get the names of the supported tags.
   */ 
  public Set<String>
  getTagNames()
  {
    return Collections.unmodifiableSet(pTags.keySet());
  }

  /**
   * Get the selected values of the given tag.
   * 
   * @param tname
   *   The name of the tag.
   *
   * @return 
   *   The selected values or <CODE>null</CODE> if the given tag is unsupported.
   */ 
  public SortedSet<String>
  getTagValues
  (
   String tname
  )
  {
    TreeSet<String> values = pTags.get(tname);
    if(values != null) 
      return Collections.unmodifiableSortedSet(values);
    return null;
  }

  /**
   * Select a tag value.
   * 
   * @param tname
   *   The name of the tag.
   *
   * @param value
   *   The value to select.
   */ 
  public void 
  selectTag
  (
   String tname, 
   String value
  ) 
  {
    TreeSet<String> values = pTags.get(tname);
    if(values == null) {
      values = new TreeSet<String>();
      pTags.put(tname, values);
    }
      
    values.add(value);
  }

  /**
   * Deselect a tag value.
   * 
   * @param tname
   *   The name of the tag.
   *
   * @param value
   *   The value to deselect.
   */ 
  public void 
  deselectTag
  (
   String tname, 
   String value
  ) 
  {
    TreeSet<String> values = pTags.get(tname);
    if(values != null) {
      values.remove(value);
      if(values.isEmpty())
	pTags.remove(tname);
    }
  }
  
  /**
   * Deselected all values of the given tag.
   * 
   * @param tname
   *   The name of the tag.
   */ 
  public void 
  clearTag
  (
   String tname
  ) 
  {
    pTags.remove(tname);
  }

  /**
   * Is the given value selected for the given tag? 
   */ 
  public boolean
  isTagSelected
  (
   String tname, 
   String value
  ) 
  {
    TreeSet<String> values = pTags.get(tname);
    if(values != null) 
      return values.contains(value);
    return false;
  }


  /*----------------------------------------------------------------------------------------*/
 
  // constraints...

  
  
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
  public TaskEvent
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
  public Collection<TaskEvent>
  getEvents() 
  {
    return Collections.unmodifiableCollection(pEvents.values());
  }

  /**
   * Get the most recent task event.
   */
  public TaskEvent
  getLatestEvent() 
  {
    return pEvents.get(pEvents.lastKey());
  }
  
  

  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Get the current task state.
   */ 
  public TaskState 
  getState() 
  {
    return pState; 
  }

  /**
   * Set the current task state.
   */ 
  public void 
  setState
  (
   TaskState state
  ) 
  {
    pState = state;
  }

   
  /*----------------------------------------------------------------------------------------*/
  /*   E V E N T   A P P L I C A T I O N                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Modifies the task by applying the changes represented by the given event.
   * 
   * @param event
   *   The event to apply.
   * 
   * @param isPrivileged
   *   Whether the current user is privileged.
   * 
   * @throws PipelineException
   *   If the author of the event does not have permission to apply this event to the task
   *   or the current task state does not allow this kind of event.
   */ 
  public void 
  applyEvent
  (
   TaskEvent event, 
   boolean isPrivileged
  ) 
    throws PipelineException
  {
    event.modifyTask(this, isPrivileged);
    pEvents.put(event.getTimeStamp(), event);
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

    if(pOwner != null) 
      encoder.encode("Owner", pOwner);

    encoder.encode("StartDate",      pStartDate);
    encoder.encode("CompletionDate", pCompletionDate);    
    
    if(!pTags.isEmpty()) 
      encoder.encode("Tags", pTags);

    //if(!pContraints.isEmpty()) 
    //  encoder.encode("Contraints", pTags);
    
    if(!pNodes.isEmpty()) 
      encoder.encode("Nodes", pNodes);

    encoder.encode("Events", pEvents);
    encoder.encode("State",  pState);
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

    String owner = (String) decoder.decode("Owner");
    if(owner != null) 
      pOwner = owner;

    Date startDate = (Date) decoder.decode("StartDate");
    if(startDate == null) 
      throw new GlueException("The \"StartDate\" was missing or (null)!");
    pStartDate = startDate;

    Date completionDate = (Date) decoder.decode("CompletionDate");
    if(completionDate == null) 
      throw new GlueException("The \"CompletionDate\" was missing or (null)!");
    pCompletionDate = completionDate;

    TreeMap<String,TreeSet<String>> tags = 
      (TreeMap<String,TreeSet<String>>) decoder.decode("Tags");
    if(tags == null) 
      pTags = new TreeMap<String,TreeSet<String>>();
    else 
      pTags = tags;

//     TreeMap<String,TaskConstraint> contraints = 
//       (TreeMap<String,TaskConstraint>) decoder.decode("Contraints");
//     if(contraints == null) 
//       pContraints = new TreeMap<String,TaskConstraint>();
//     else 
//       pContraints = contraints;

    TreeMap<String,VersionID> nodes = (TreeMap<String,VersionID>) decoder.decode("Nodes");
    if(nodes == null) 
      pNodes = new TreeMap<String,VersionID>();
    else 
      pNodes = nodes;

    TreeMap<Date,TaskEvent> events = (TreeMap<Date,TaskEvent>) decoder.decode("Events");
    if(events == null) 
      throw new GlueException("The \"Events\" was missing or (null)!");
    pEvents = events;

    TaskState state = (TaskState) decoder.decode("State");
    if(state == null) 
      throw new GlueException("The \"State\" was missing or (null)!");
    pState = state;
  }
 


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1691787610806763597L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The user name of the current supervisor of the task.
   */ 
  private String  pSupervisor; 

  /**
   * The name of the current user responsible for completing the task.
   */ 
  private String  pOwner; 

  /**
   * The estimated date when work should begin.
   */ 
  private Date  pStartDate; 

  /**
   * The estimated date when all work should be complete.
   */ 
  private Date  pCompletionDate; 

  /**
   * The currently selected tag values associated with the task indexed by tag name.
   */ 
  private TreeMap<String,TreeSet<String>>  pTags;

  /**
   * The current constraints of this task indexed by the name of the constraining tasks.
   */ 
  //private TreeMap<String,TaskConstraint>  pConstraints; 

  /**
   * The revision numbers of the nodes currently associated with the task indexed by 
   * fully resolved node name.
   */ 
  private TreeMap<String,VersionID>  pNodes; 
  
  /**
   * The history of task events indexed by event timestamp.
   */ 
  private TreeMap<Date,TaskEvent>  pEvents; 
  
  /**
   * The current task state.
   */ 
  private TaskState  pState; 

}
