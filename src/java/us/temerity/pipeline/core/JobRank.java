// $Id: JobRank.java,v 1.6 2009/12/11 04:21:10 jesse Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   R A N K                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A comparable object that encapsulates all factors which contribute to the ranking of jobs
 * with respect to a particular slot. <P> 
 * 
 * The natural order of JobRank is the order in which jobs should be processed, which makes
 * it convenient during sorting the qualified jobs.
 */ 
public 
class JobRank
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new rank.
   */
  public
  JobRank()
  {}


  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the unique job identifier.
   */ 
  public final long
  getJobID() 
  {
    return pJobID; 
  }
  
  /**
   * Get the job's selection score.
   */
  public final int 
  getScore()
  {
    return pScore;
  }
  
  /**
   * Get the job group percentage done.
   */
  public final double 
  getFavorGroupPercent()
  {
    return pFavorGroupPercent;
  }
  
  /**
   * Get the percentage of the user's allotment that has currently been used. <p>
   * 
   * Lower numbers indicate that the user has a great claim to available machines.
   */
  public final double
  getBalanceGroupPercent()
  {
    return pBalanceGroupPercent;
  }
  
  /**
   * Get the job's priority.
   */
  public final int 
  getPriority()
  {
    return pPriority;
  }
  
  /**
   * Get the job submission time.
   */
  public final long 
  getTimeStamp()
  {
    return pTimeStamp;
  }

  /**
   * Set the complete set of values used to rank jobs.
   * 
   * @param jobID
   *   The unique job identifier.
   * 
   * @param score
   *   The job selection score. 
   * 
   * @param favorGroupPercent
   *   The percentage of jobs engaged/pending within the jobs group.
   *   
   * @param balanceGroupPercent
   *   The percentage of their total share that the owner of this job has used.
   *   
   * @param balanceGroupUse
   *   The number of jobs that this user has dispatched in this balance group during 
   *   the current cycle.
   * 
   * @param priority
   *   The relative job priority.
   * 
   * @param stamp
   *   The timestamp of when the job was submitted.
   */ 
  public void
  update
  (
   long jobID, 
   int score, 
   double favorGroupPercent,
   double balanceGroupPercent,
   int priority, 
   long stamp
  ) 
  {
    pJobID     = jobID;
    pScore     = score; 
    pPriority  = priority; 
    pTimeStamp = stamp;
    
    pFavorGroupPercent   = favorGroupPercent;
    pBalanceGroupPercent = balanceGroupPercent;
  }

  

  /**
   * Generate selection logging message.
   */
  public String
  selectionLogMsg
  (
    DispatchCriteria[] crits,
    int rank
  ) 
  {
    StringBuffer toReturn = new StringBuffer(pJobID + "(" + rank + "): ");
    
    for (DispatchCriteria crit: crits) {
      switch(crit) {
      case SelectionScore:
        toReturn.append(pScore + " ");
        break;
      case BalanceGroups:
        toReturn.append(String.format("%1$.4f", pBalanceGroupPercent) + " ");
        break;
      case JobGroupPercent:
        toReturn.append(String.format("%1$.4f", pFavorGroupPercent) + " ");
        break;
      case JobPriority:
        toReturn.append(pPriority + " ");
        break;
      case TimeStamp:
        toReturn.append(TimeStamps.format(pTimeStamp) + " ");
      }
    }
    
    return toReturn.toString(); 
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique job identifier.
   */ 
  private long pJobID; 

  /**
   * The job selection score. 
   */ 
  private int pScore; 

  /**
   * The percentage of jobs engaged/pending within the jobs group.
   */ 
  private double pFavorGroupPercent; 
  
  /**
   * The percentage of the user's balance group allotment that is currently being used.  This
   * will be {@link Double#MAX_VALUE} if the user does not have an allotment, zero if the none
   * of the allotment is being used or the percent used (which can be greater than 1, but will
   * not be negative).
   */
  private double pBalanceGroupPercent;
  
  /**
   * The relative job priority.
   */ 
  private int pPriority; 

  /**
   * The timestamp of when the job was submitted.
   */ 
  private long pTimeStamp; 

}

