// $Id: CompletionState.java,v 1.1 2004/10/18 04:00:00 jim Exp $

package us.temerity.pipeline.task;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   C O M P L E T I O N   S T A T E                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * The current state of completion of a {@link Task Task}.
 */
public
enum CompletionState
{  
  /**
   * The Supervisor has determined that there are outstanding requirements which should be
   * addressed by the task's Owner before this task can be considered completed.
   */ 
  Unfinished, 

  /**
   * The Owner of the task has suspended work until the Supervisor has reviewed the task.
   */ 
  PendingReview, 

  /**
   * The Supervisor has requested that all work should temporarily cease on this task until 
   * furthor notice.
   */ 
  OnHold, 

  /**
   * The Supervisor has decided that this task is no longer neccessary and all work should
   * be abandoned.
   */ 
  Cancelled, 

  /**
   * The Supervisor has determined that the current state of the task is sufficient to 
   * consider the task to be complete. However, there still remain some areas which should 
   * be improved if there is time remaining after all other tasks have reached at least this
   * level of completion.
   */ 
  CouldBeBetter,

  /**
   * The Supervisor has determined that this task is complete.
   */ 
  Finalled;


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of all possible states.
   */ 
  public static ArrayList<CompletionState>
  all() 
  {
    CompletionState values[] = values();
    ArrayList<CompletionState> all = new ArrayList<CompletionState>(values.length);
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
    return sTitles[ordinal()];
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static String sTitles[] = {
    "Unfinished", 
    "Pending Review", 
    "On Hold", 
    "Cancelled", 
    "Could Be Better", 
    "Finalled"
  };

}
