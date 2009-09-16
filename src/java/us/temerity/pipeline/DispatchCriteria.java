// $Id: DispatchCriteria.java,v 1.1 2009/09/16 03:54:40 jesse Exp $

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
}
