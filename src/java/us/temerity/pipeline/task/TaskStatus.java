// $Id: TaskStatus.java,v 1.3 2008/02/13 19:22:34 jesse Exp $

package us.temerity.pipeline.task;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   T A S K   S T A T U S                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * An abreviated summary of the current status of a task. 
 */
public
class TaskStatus
  implements Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new task status.
   * 
   * @param task
   *   The complete task event history.
   * 
   * @param stamp
   *   The timestamp of when the task status should be evaluated in the history of the task.
   */ 
  public
  TaskStatus
  (
   Task task,
   Date stamp
  ) 
  {
    pName = task.getName();

    pTags = new TreeMap<String,TreeSet<String>>();
    // pConstraints = new TreeMap<String,TaskConstraint>();
    pNodes = new TreeMap<String,VersionID>();

    pCompletionHistory = new TreeMap<Long,CompletionState>();
    pActivityHistory   = new TreeMap<Long,ActivityState>();

    for(BaseEvent event : task.getEvents()) 
      update(event);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the task.
   */ 
  public String
  getName()
  {
    return pName;
  }

  /**
   * Get the user name of the current supervisor of the task.
   */ 
  public String
  getSupervisor() 
  {
    return pSupervisor;
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
   * Get the estimated start/completion time interval of the task.
   */ 
  public TimeInterval
  getTimeInterval()
  {
    return pTimeInterval; 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of all tags supported by the task. 
   */ 
  public Set<String> 
  getTagNames() 
  {
    return Collections.unmodifiableSet(pTags.keySet());
  }
  
  /**
   * Get the selected values for the given tag. 
   * 
   * @param name
   *   The name of the tag.
   * 
   * @return 
   *   The selected values or <CODE>null</CODE> if the tag is unsupported.
   */ 
  public SortedSet<String> 
  getTagValues
  (
   String name
  ) 
  {
    TreeSet<String> values = pTags.get(name);
    if(values != null) 
      return Collections.unmodifiableSortedSet(values);
    return null;
  }

  /**
   * Is the value selected by this task for the given tag?
   * 
   * @param name
   *   The name of the tag.
   *    
   * @param value
   *   The value of the tag to test.
   */ 
  public boolean
  isTagSelected
  (
   String name,
   String value
  ) 
  {
    TreeSet<String> values = pTags.get(name);
    if(values != null) 
      return values.contains(value);
    return false;    
  }

  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current constraints of this task indexed by the names of the constraining tasks.
   */ 
//   public TreeMap<String,TaskConstraint>
//   getConstraints() 
//   {
//     return null;
//   }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the fully resolved names of the nodes currently associated with the task.
   */ 
  public Set<String> 
  getNodeNames() 
  {
    return Collections.unmodifiableSet(pNodes.keySet());
  }
  
  /**
   * Get the revision number of the checked-in node version of the given node which is 
   * associated with the task.
   * 
   * @param name
   *   The fully resolved node name.
   * 
   * @return 
   *   The revision number or <CODE>null</CODE> if the node is not associated with the task.
   */ 
  public VersionID
  getNodeVersionID
  (
   String name
  ) 
  {
    return pNodes.get(name);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the timestamps of when the {@link CompletionState CompletionState} of the task
   * has changed.
   */ 
  public Set<Long>
  getCompletionStamps() 
  {
    return Collections.unmodifiableSet(pCompletionHistory.keySet());
  }

  /**
   * Get the completion state at the given point in time.
   */ 
  public CompletionState
  getCompletionState
  (
    Long stamp
  ) 
  {
    if(stamp == null) 
      throw new IllegalArgumentException
	("The timestamp cannot be (null)!");

    CompletionState state = pCompletionHistory.get(pCompletionHistory.firstKey());
    for(Long key : pCompletionHistory.keySet()) {
      if(key > stamp) 
	break;
      state = pCompletionHistory.get(key);
    }

    return state;
  }
  
  /**
   * Get the latest completion state.
   */ 
  public CompletionState
  getLatestCompletionState()
  {
    return pCompletionHistory.get(pCompletionHistory.lastKey());
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the timestamps of when the {@link ActivityState ActivityState} of the task
   * has changed.
   */ 
  public Set<Long>
  getActivityStamps() 
  {
    return Collections.unmodifiableSet(pActivityHistory.keySet());
  }

  /**
   * Get the activity state at the given point in time.
   */ 
  public ActivityState
  getActivityState
  (
    Long stamp
  ) 
  {
    if(stamp == null) 
      throw new IllegalArgumentException
	("The timestamp cannot be (null)!");

    ActivityState state = pActivityHistory.get(pActivityHistory.firstKey());
    for(Long key : pActivityHistory.keySet()) {
      if(key > stamp) 
	break;
      state = pActivityHistory.get(key);
    }

    return state;
  }

  /**
   * Get the latest activity state.
   */ 
  public ActivityState
  getLatestActivityState()
  {
    return pActivityHistory.get(pActivityHistory.lastKey());
  }
  
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   E V E N T S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the current status of the task by applying the changes caused by the given event.
   */ 
  public void 
  update
  (
   BaseEvent event
  ) 
  {
    String supervisor = event.getSupervisor();
    if(supervisor != null) 
      pSupervisor = supervisor;

    String owner = event.getOwner();
    if(owner != null) 
      pOwner = owner;
    
    TimeInterval timeInterval = event.getTimeInterval();
    if(timeInterval != null) 
      pTimeInterval = timeInterval;
    
    TreeMap<String,TreeSet<String>> tags = event.getTags();
    if(tags != null) 
      pTags = tags;
    
//     TreeMap<String,TaskConstraint> constraints = event.getConstraints();
//     if(constraints != null) 
//       pConstraints = constraints;
    
    TreeMap<String,VersionID> nodes = event.getNodes();
    if(nodes != null) 
      pNodes = nodes;
    
    CompletionState completionState = event.getCompletionState();
    if(completionState != null) 
      pCompletionHistory.put(event.getTimeStamp(), completionState);

    ActivityState activityState = event.getActivityState();
    if(activityState != null) 
      pActivityHistory.put(event.getTimeStamp(), activityState);
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   C O N V E R S I O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The string representation of the task.
   */ 
  public String
  toString() 
  {
    return pName;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8601721988465643486L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the task.
   */ 
  private String  pName; 

  /**
   * The user name of the current supervisor of the task.
   */ 
  private String  pSupervisor; 

  /**
   * The name of the current user responsible for completing the task.
   */ 
  private String  pOwner; 

  /**
   * The currently scheduled start/completion interval of the task.
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

  /**
   * The revision numbers of the nodes currently associated with the task indexed by 
   * fully resolved node name.
   */ 
  private TreeMap<String,VersionID>  pNodes; 


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The history of changes to the completion state of the task. 
   */ 
  private TreeMap<Long,CompletionState>  pCompletionHistory; 

  /**
   * The history of changes to the activity state of the task. 
   */ 
  private TreeMap<Long,ActivityState>  pActivityHistory; 

}
