// $Id: JobRank.java,v 1.4 2009/09/29 20:44:41 jesse Exp $

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
  getPercent()
  {
    return pPercent;
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
   * @param percent
   *   The percentage of jobs engaged/pending within the jobs group.
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
   double percent, 
   int priority, 
   long stamp
  ) 
  {
    pJobID     = jobID;
    pScore     = score; 
    pPercent   = percent; 
    pPriority  = priority; 
    pTimeStamp = stamp;
  }

  

  /**
   * Generate selection logging message.
   */
  public String
  selectionLogMsg
  (
   int rank
  ) 
  {
    return (pJobID + "(" + rank + "): " + pScore + " " + 
            String.format("%1$.4f", pPercent) + " " + pPriority + " " + 
            TimeStamps.format(pTimeStamp)); 
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
  private double pPercent; 

  /**
   * The relative job priority.
   */ 
  private int pPriority; 

  /**
   * The timestamp of when the job was submitted.
   */ 
  private long pTimeStamp; 

}

