// $Id: TaskState.java,v 1.1 2004/10/12 23:21:13 jim Exp $

package us.temerity.pipeline.task;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   T A S K   S T A T E                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The current state of completion of a {@link Task Task}.
 */
public
enum TaskState
{  
  /**
   * No work is currently being done to accomplish the Task by the task Owner.
   */
  Inactive,

  /**
   * The Owner of the Task in actively working to complete the Task. 
   */
  Active,

  /**
   * The Owner of the Task has completed an intermediate step toward the completion of 
   * the task which must be approved by the Supervisor before work can continue. <P> 
   * 
   * The Owner will not actively work on the Task until the step is approved or otherwise 
   * commented upon by the Supervisor.
   */
  NeedsApproval,

  /**
   * The Supervisor has reviewed and approved the previous intermediate step submitted 
   * by the Owner. 
   */
  Approved,

  /**
   * The Supervisor has reviewed the previous intermediate step submitted by the Owner 
   * and has submitted a message giving additional direction concerning the Task. <P> 
   * 
   * The Owner of the Task should not continue working on the task until they have read 
   * the associated message.
   */
  ChangesRequired,

  /**
   * The Supervisor or Owner has decided that all work on this Task should be suspended 
   * until further notice.
   */
  Held,

  /**
   * The Supervisor has reviewed the last approval request and has decided that the current 
   * state of the Task is good enough to be considered Finalled. <P> 
   * 
   * However, there are some remaining issues that could be addressed once all other Tasks 
   * reach a Could Be Better or Finalled state in order to improve the quality of the 
   * Project. No work should progress on this Task until further notice.
   */
  CouldBeBetter,

  /**
   * The Supervisor has decided that the Task has been completed and no further changes 
   * should take place. Once a Task has been Finalled, only the Supervisor may request 
   * further changes to the Task.
   */
  Finalled;



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of all possible states.
   */ 
  public static ArrayList<TaskState>
  all() 
  {
    TaskState values[] = values();
    ArrayList<TaskState> all = new ArrayList<TaskState>(values.length);
    int wk;
    for(wk=0; wk<values.length; wk++)
      all.add(values[wk]);
    return all;
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
    "Unassigned", 
    "Inactive", 
    "Needs Approval", 
    "Approved", 
    "Changes Required", 
    "Held", 
    "Could Be Better", 
    "Finalled"
  };

}
