// $Id: TaskEventType.java,v 1.1 2004/10/12 23:21:13 jim Exp $

package us.temerity.pipeline.task;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   T A S K   E V E N T   T Y P E                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Shorthand for the class types which implement the TaskEvent interface. <P> 
 * 
 * Used in switch statements where efficiency is critical and as a more lightweight 
 * representation of TaskEvent instances.
 */
public
enum TaskEventType
{  
  /**
   * The {@link CreateEvent CreateEvent} class.
   */ 
  Create,
  
  /**
   * The {@link AssignEvent AssignEvent} class.
   */ 
  Assign, 

  /**
   * The {@link ManageEvent ManageEvent} class.
   */ 
  Manage, 

  /**
   * The {@link StartEvent StartEvent} class.
   */ 
  Start, 

  /**
   * The {@link StopEvent StopEvent} class.
   */ 
  Stop, 

  /**
   * The {@link AddNodeEvent AddNodeEvent} class.
   */ 
  AddNode, 

  /**
   * The {@link RemoveNodeEvent RemoveNodeEvent} class.
   */ 
  RemoveNode, 

  /**
   * The {@link UpdateEvent UpdateEvent} class.
   */ 
  Update, 

  /**
   * The {@link SubmitEvent SubmitEvent} class.
   */ 
  Submit, 

  /** 
   * The {@link ApproveEvent ApproveEvent} class.
   */ 
  Approve, 

  /**
   * The {@link DirectEvent DirectEvent} class.
   */ 
  Direct, 

  /**
   * The {@link HoldEvent HoldEvent} class.
   */ 
  Hold, 

  /**
   * The {@link CouldBeBetterEvent CouldBeBetterEvent} class.
   */ 
  CouldBeBetter, 

  /**
   * The {@link FinalledEvent FinalledEvent} class.
   */ 
  Finalled;


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of all possible event types.
   */ 
  public static ArrayList<TaskEventType>
  all() 
  {
    TaskEventType values[] = values();
    ArrayList<TaskEventType> all = new ArrayList<TaskEventType>(values.length);
    int wk;
    for(wk=0; wk<values.length; wk++)
      all.add(values[wk]);
    return all;
  }

  /**
   * Get the list of human friendly string representation for all possible event types.
   */ 
  public static ArrayList<String>
  titles() 
  {
    ArrayList<String> titles = new ArrayList<String>();
    for(TaskEventType et : TaskEventType.all()) 
      titles.add(et.toTitle());
    return titles;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O N V E R S I O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Convert to a more human friendly string representation.
   */ 
  public String
  toTitle() 
  {
    return sTitles[ordinal()];
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static String sTitles[] = {
    "Create",
    "Assign", 
    "Manage", 
    "Start", 
    "Stop", 
    "Add Node", 
    "Remove Node", 
    "Update", 
    "Submit", 
    "Approve", 
    "Direct", 
    "Hold", 
    "Could Be Better", 
    "Finalled"
  };
}
