// $Id: ActivityState.java,v 1.1 2004/10/18 04:00:00 jim Exp $

package us.temerity.pipeline.task;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   A C T I V I T Y   S T A T E                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The current state of activity toward the completion of a {@link Task Task}.
 */
public
enum ActivityState
{  
  /**
   * There is no current Owner of the task, therefore no work can be done.
   */
  Unassigned, 

  /**
   * No work is currently being done by the Owner to complete the task.
   */ 
  Inactive, 

  /**
   * The Owner of the task is actively working to complete the task.
   */ 
  Active;



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of all possible states.
   */ 
  public static ArrayList<ActivityState>
  all() 
  {
    ActivityState values[] = values();
    ArrayList<ActivityState> all = new ArrayList<ActivityState>(values.length);
    int wk;
    for(wk=0; wk<values.length; wk++)
      all.add(values[wk]);
    return all;
  }

  /**
   * Get the list of human friendly string representation for all possible states.
   */ 
  public static ArrayList<String>
  titles() 
  {
    ArrayList<String> titles = new ArrayList<String>();
    for(CompletionState state : CompletionState.all()) 
      titles.add(state.toTitle());
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
    return toString();
  }

}
