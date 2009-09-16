// $Id: JobRank.java,v 1.3 2009/09/16 03:54:40 jesse Exp $

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
public class 
JobRank
  implements Comparable<JobRank>   
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
  
  

  

  /*----------------------------------------------------------------------------------------*/
  /*   O B J E C T   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Indicates whether some other object is "equal to" this one.
   * 
   * Note that the jobID is not considered by this comparison.
   * 
   * @param obj 
   *   The reference object with which to compare.
   */
  public boolean
  equals
  (
   Object obj
  )
  {
    if((obj != null) && (obj instanceof JobRank)) {
      JobRank rank = (JobRank) obj;
    
      return ((pScore == rank.pScore) && 
              (Math.abs(pPercent - rank.pPercent) < sEpsilon) && 
              (pPriority == rank.pPriority) && 
              (pTimeStamp == rank.pTimeStamp)); 
    }
    return false;
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
  /*   C O M P A R A B L E                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Compares this <CODE>JobRank</CODE> with the given <CODE>JobRank</CODE> for order.
   * 
   * @param rank
   *   The <CODE>JobRank</CODE> to be compared.
   */
  public int
  compareTo
  (
   JobRank rank
  )
  {
    /* selection score is in descending order */ 
    if(pScore > rank.pScore) 
      return -1; 
    else if(pScore < rank.pScore) 
      return 1; 
    else {
      /* percentage engaged/pending is in descending order */ 
      if((pPercent - rank.pPercent) >= sEpsilon) 
        return -1;
      else if((rank.pPercent - pPercent) >= sEpsilon) 
        return 1;
      else {
        /* job priority is in descending order */ 
        if(pPriority > rank.pPriority) 
          return -1; 
        else if(pPriority < rank.pPriority) 
          return 1; 
        else {
          /* time stamp is ascending order */ 
          if(pTimeStamp > rank.pTimeStamp) 
            return 1; 
          else if(pTimeStamp < rank.pTimeStamp) 
            return -1; 
          else
            return 0;
        }
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The smallest difference in floating point value considered to be different.
   */ 
  public static final double sEpsilon = 0.000001;

  
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

