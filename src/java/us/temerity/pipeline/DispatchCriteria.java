// $Id: DispatchCriteria.java,v 1.2 2009/12/09 05:05:55 jesse Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   D I S P A T C H   C R I T E R I A                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The list of criteria that the dispatcher uses to order the jobs when it is figuring out
 * which one to give to a job server.
 */
public enum 
DispatchCriteria
{
  /**
   * The selection score of the job, generated from the selection group assigned to the slot
   * and the selection keys on the job.
   */
  SelectionScore,
  
  /**
   * Allows users to be assigned a certain share of machines in the queue, which they will be
   * given priority on.  When a user exceeds their alloted share in a group, they will not be 
   * assigned more machines in that group until their usage falls or unless there are no other
   * jobs with a better claim.  When a user exceeds their max share in a group, they will not
   * be assigned any more machines in that group at all. 
   */
  BalanceGroups,
  
  /**
   * The percentage of the jobs in the jobs which are completed or waiting.
   */
  JobGroupPercent,
  
  /**
   * The priority value in the Job Requirements.
   */
  JobPriority,

  /**
   * The time that the job was submitted at.
   */
  TimeStamp;
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of all possible criteria.
   */ 
  public static ArrayList<DispatchCriteria>
  all() 
  {
    DispatchCriteria values[] = values();
    ArrayList<DispatchCriteria> all = new ArrayList<DispatchCriteria>(values.length);
    int wk;
    for(wk=0; wk<values.length; wk++)
      all.add(values[wk]);
    return all;
  }

  /**
   * Get a short title for use in log messages.
   */
  public String 
  toShortTitle()
  {
    switch (this) {
    case BalanceGroups:
      return "balance";
    case JobGroupPercent:
      return "percent";
    case JobPriority:
      return "priority";
    case SelectionScore:
      return "score";
    case TimeStamp:
      return "time";
    default:
      return "notitle";
    }
  }
}
